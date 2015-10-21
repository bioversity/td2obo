import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.obolibrary.oboformat.model.Frame;



public class TdV52Obo {

	private static List<ArrayList<String> > read(String dataFileName, ArrayList<String> oRowStruct) throws Exception{
		ArrayList dataset = new ArrayList();
		try{
			BufferedReader reader = new BufferedReader( new FileReader( dataFileName ));

			int lineNo = 0;
			String line;
			
			
			while( (line = reader.readLine()) != null ) {
				
				//System.out.println("lineno="+lineNo);
				
					if(lineNo==0){
						//the first row is the data structure
						String[] attNames = line.split(";");
						
						for(int i=0;i<attNames.length;i++){
							oRowStruct.add(attNames[i].trim().replaceAll("\"", ""));
						}
					}else{
						//the late rows are data content
						ArrayList<String> row = new ArrayList<String>();
						
							String[] rowValues =  line.split(";");
							for(int i=0;i<rowValues.length;i++){
								row.add(rowValues[i]);
							}
							while(row.size()<oRowStruct.size()){
								row.add(null);
							}	
					
					if(row.size()!=oRowStruct.size()){
						System.out.println("line no="+lineNo);
                        System.out.println("oRowStruct size="+oRowStruct.size()+":"+oRowStruct);
                        System.out.println("rowValues size="+row.size()+":"+row);
						throw new Exception("Data structure attribute number is different row value number.");
					}
					
					dataset.add(row);
					
					}
				//}
				lineNo++;
			}			
			reader.close();
		}catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
		return dataset;
	}
	
	public static void main(String[] args){		
		
		/// args[0] = excel file
		if(args.length != 2){
			
				System.out.println("invalid number of arguments. Fist argument must be the excel TD version 5 and the second argument should be the obo file");
		}else{
		
			//creation of the temp file: csv file
			File temp= null;
			try {
				temp = File.createTempFile("tempfiletest", ".tmp");
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} 
	        String path = temp.getAbsolutePath();
	        xlsx(new File(args[0]), temp);
						
			//read the data from data file to data structure
			ArrayList<String> rowStruct = new ArrayList<String>();
			List dataset = null;		
			try {
				dataset = TdV52Obo.read(path, rowStruct);
				//System.out.println(rowStruct);
				
				////build empty obo model
				coModelTdToOBO m = new coModelTdToOBO();
				
				///creation of specific properties
				m.setProperty("method_of");
				m.setProperty("scale_of");
				m.setProperty("variable_of");
				
				//Crop
				String crop= "";
	
				///for checking if id already exists
				ArrayList<String> ids = new ArrayList<String>();
				ArrayList<String> duplicate = new ArrayList<String>();
				
				////creation categories
				Iterator it = dataset.iterator();
				Set cat=new HashSet(); 
				Set catM=new HashSet();
				Set catS=new HashSet(); 
				
				/// get the crop id
				String cropID =  "";
				
				//foreach line of the file
				while(it.hasNext()){
					ArrayList<String> rowInfo = (ArrayList<String>) it.next();
					//System.out.println(rowInfo);
					
					//get the superclasses
					String category = rowInfo.get(rowStruct.indexOf("Trait class"));
	
					//get language
					String lang = rowInfo.get(rowStruct.indexOf("Language of submission"));
					
					if(rowInfo.get(rowStruct.indexOf("Trait ID"))==null){
						continue;
					}
					
					if (cropID.isEmpty())
						cropID = rowInfo.get(rowStruct.indexOf("Trait ID")).split(":")[0];
					
					try{
						if(crop.isEmpty()){
							///A remplacer avec le vrai TD
							crop = rowInfo.get(rowStruct.indexOf("Crop"));
						}
					}catch(Exception e){
						System.out.println(e + ": Crop is empty. You must complete the column Crop");
						return;
					}
					
					
					//let's say that language will be english if empty
					if(lang==null){
						lang="en";
					}
					
					//add category if english
					if(lang!=null && (lang.equalsIgnoreCase("\"en\"")||lang.equalsIgnoreCase("en")) 
							&& category!=null && !category.isEmpty() && !category.replaceAll("\"", "").isEmpty()){
							cat.add(category.replaceAll(" ", "_"));
					}
					
				}
				//creation of the "roots" terms for trait, method and scale
				Frame traitRootClass = m.setConcept("Trait", "Trait", "", crop, cropID+":", "Trait", false);
			
				
				Iterator itCat = cat.iterator();
				while(itCat.hasNext()){
					String category = (String) itCat.next();
					///Concept creation 
					Frame concept = m.setConcept(category, category, "", crop, cropID+":", "Trait", true);
				}
				
				ids.addAll(cat);
				ids.addAll(catM);
				ids.addAll(catS);
				
				///This time add the concepts
				it = dataset.iterator();
				
				////to remove duplicated method and scale
				 Map<String, String> methodMap = new HashMap<String, String>();
				 Map<String, String> scaleMap = new HashMap<String, String>();			
				 
				 ///to print line number when pb
				 int rowNumber=1;
				 
				////creation traits, methods, scales, variables
				while(it.hasNext()){
					rowNumber++;
					ArrayList<String> rowInfo = (ArrayList<String>) it.next();
					//System.out.println(rowInfo);
					
					//some variables can be obsolete
					String keep = rowInfo.get(rowStruct.indexOf("Trait status"));
					
					///CHANGE THAT LATER
					if (keep!=null && keep.equalsIgnoreCase("toto")){
						continue;
					}else{
						crop = rowInfo.get(rowStruct.indexOf("Crop"));
						String traitName = rowInfo.get(rowStruct.indexOf("Trait"));
						String coId = rowInfo.get(rowStruct.indexOf("Trait ID"));
						String def = rowInfo.get(rowStruct.indexOf("Trait description"));
						String category = rowInfo.get(rowStruct.indexOf("Trait class"));
						String lang = rowInfo.get(rowStruct.indexOf("Language of submission"));
						String creator = rowInfo.get(rowStruct.indexOf("Scientist"));
						//String organization = rowInfo.get(rowStruct.indexOf("Institution"));
						String date = rowInfo.get(rowStruct.indexOf("Date of submission"));
						String abbrev = rowInfo.get(rowStruct.indexOf("Trait abbreviation"));
						String synonym = rowInfo.get(rowStruct.indexOf("Trait synonyms"));
						
						if(lang!=null && (lang.equalsIgnoreCase("\"en\"")||lang.equalsIgnoreCase("en"))){
							if( coId!=null){
								//look for duplicate ids
								if(ids.contains(coId)){
									duplicate.add(coId);	
								} 
								if (coId.isEmpty() || coId.replace("\"", "").isEmpty()){
									System.out.println("Line "+ rowNumber + " has been ignored because Trait ID was empty. Fill in this column ans re-run the script to have this line info in the output obo file!");
									continue;
								}
								
								if(!ids.contains(coId)){
								ids.add(coId);
								
									 ///Concept creation 
									Frame concept = m.setConcept(traitName, coId, def, cropID+":"+category, crop, lang);
									
									///Add creator to concept
								    if(!creator.isEmpty()){
								    	m.addCreatorToConcept(m.setCreator(creator), concept);
								    }
							
								    //add abbreviation and synonyms
								    if(!abbrev.isEmpty() && !abbrev.replaceAll("\"", "").isEmpty()){
								    	m.addAbbrevToConcept(abbrev, concept);
								    }
								    
								    if(!synonym.isEmpty() && !synonym.replaceAll("\"", "").isEmpty()){
								    	m.addAltLabel(synonym, concept);
								    }
								}
							    
							    
							    ///////////////////////////////////////
							    //addMethod
							    ///////////////////////////////////////
							    
							    String method = rowInfo.get(rowStruct.indexOf("Method"));
							    String idMethod = rowInfo.get(rowStruct.indexOf("Method ID"));
							    String defMethod = rowInfo.get(rowStruct.indexOf("Method description"));
							    String sourceMethod = rowInfo.get(rowStruct.indexOf("Method reference"));
							    
							    Frame Method = null;
		
							    if(idMethod!=null){
								    //add method
							    	
							    	if(idMethod.isEmpty() || idMethod.replaceAll("\"", "").isEmpty()){
								    	System.out.println("Method hasn't been created because empty, line: "+rowNumber);
							    		continue;
							    	}
							    	
							    	if(methodMap.containsKey(idMethod)){
							    		duplicate.add(idMethod);
							    		m.setExistingMethod(idMethod, coId);
							    	}
							    	else{
							    		Method = m.setConceptMethod(method, idMethod, crop, defMethod, lang, sourceMethod, coId);
							    	}
							    	methodMap.put(idMethod, "");
							    }else{
							    	System.out.println("Method hasn't been created because empty, line: "+rowNumber);
							    	continue;
							    }
							    
							    
								///////////////////////////////////////
								//addMeasure
								///////////////////////////////////////
							    
							    String idMeasure = rowInfo.get(rowStruct.indexOf("Scale ID"));
								String scaleName = rowInfo.get(rowStruct.indexOf("Scale name"));
								String scaleClass = rowInfo.get(rowStruct.indexOf("Scale class"));
		
								if(idMeasure!=null ){
									if(idMeasure.isEmpty()){
								    	System.out.println("Scale hasn't been created because empty, line: "+rowNumber);
										continue;
									}
		
									if(scaleMap.containsKey(idMeasure)){
										duplicate.add(idMeasure);
										m.setExistingScale(idMeasure, idMethod);
									}else{
										if( scaleName!=null && (scaleClass.equalsIgnoreCase("\"Nominal\"")|| scaleClass.equalsIgnoreCase("Nominal") 
												|| scaleClass.equalsIgnoreCase("\"Ordinal\"") || scaleClass.equalsIgnoreCase("Ordinal"))){
											
											String [] scales = new String [100];
											///search how many scales exist
											int index = rowStruct.indexOf("Scale Xref")+1;
											int fin = rowStruct.indexOf("Variable ID")-1;
											int nbScale = 0;
											
											while(index<fin){
												if(!rowInfo.get(index).isEmpty()){
													scales[nbScale]=rowInfo.get(index);
												}
													
												nbScale++;
												index++;
											}
		 									Frame Scale = m.setConceptScale(idMeasure, crop, scaleName,scales,idMethod );
										}else if (scaleName!=null && !(scaleClass.equalsIgnoreCase("\"Nominal\"")|| scaleClass.equalsIgnoreCase("Nominal") 
												|| scaleClass.equalsIgnoreCase("\"Ordinal\"") || scaleClass.equalsIgnoreCase("Ordinal"))){
											//add scale
											String unit = "";
											if (scaleName==null  || scaleName.equalsIgnoreCase("")){
												//do nothing
											}else{
												unit=scaleName;
											}
											if(scaleMap.containsKey(unit)){
									    		Frame Scale = m.setExistingScale(scaleMap.get(unit), idMethod);
									    		idMeasure=scaleMap.get(unit);
									    		
									    				
									    	}else{
									    		Frame Scale = m.setConceptScale(idMeasure, crop, unit, idMethod);
									    		scaleMap.put(unit, idMeasure);
									    	}
										}
										scaleMap.put(idMeasure, "");
									}
								}else{
							    	System.out.println("Scale hasn't been created because empty, line: "+rowNumber);
							    	continue;
								}
								
								///////////////////////////////////////
								//addVariable
								///////////////////////////////////////
							
								
								String idVariable;
								String varName = rowInfo.get(rowStruct.indexOf("Variable name"));
								String varSyn = rowInfo.get(rowStruct.indexOf("Variable synonyms"));
								idVariable=rowInfo.get(rowStruct.indexOf("Variable ID"));
								try{
									Frame variable = m.setVariable(varName, idVariable, coId, idMethod, idMeasure, crop, lang);
									if(varSyn!= null && !varSyn.isEmpty() && !varSyn.replaceAll("\"", "").isEmpty()){
										   m.addAltLabel(varSyn, variable);
									 }
								}catch(Exception e){
									System.out.println("Variable hasn't been created for this line. Need to create the variable manually or fix the problem in the file and re-run the script, line: "+ rowNumber);
								}

							}else{
								System.out.println("Line "+ rowNumber + "has been ignored because Trait ID was empty. Fill in this column ans re-run the script to have this line info in the output obo file!");
								continue;
							}
						}
					}
				}
				
				m.save(args[1]);
				System.out.println("FYI: List of duplicate IDs found in the file: "+duplicate);
				boolean deleted = temp.delete();
				if(!deleted){
					temp.deleteOnExit();
				}
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	//xls to csv
	private static void xlsx(File inputFile, File outputFile) {
        // For storing data into CSV files
        StringBuffer data = new StringBuffer();

        try {
            FileOutputStream fos = new FileOutputStream(outputFile);
            // Get the workbook object for XLSX file
            Workbook wBook = WorkbookFactory.create(new FileInputStream(inputFile));
            // Get first sheet from the workbook
            Sheet sheet = wBook.getSheetAt(1);
           // Row row;
            //Cell cell;
            
            for(Row row : sheet) {
            	   for(int cn=0; cn<row.getLastCellNum(); cn++) {
            	       // If the cell is missing from the file, generate a blank one
            	       // (Works by specifying a MissingCellPolicy)
            	       Cell cell = row.getCell(cn, Row.CREATE_NULL_AS_BLANK);
            	       
            	       data.append("\""+cell.toString().trim() + "\""+ ";");
            	   }
            	   data.append("\r\n"); 
            	}

            fos.write(data.toString().getBytes());
            fos.close();
            

        } catch (Exception ioe) {
            ioe.printStackTrace();
        }
    }
}