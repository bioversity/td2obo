import java.io.IOException;

import org.obolibrary.oboformat.model.Clause;
import org.obolibrary.oboformat.model.Frame;
import org.obolibrary.oboformat.model.FrameMergeException;
import org.obolibrary.oboformat.model.OBODoc;
import org.obolibrary.oboformat.model.Frame.FrameType;
import org.obolibrary.oboformat.parser.OBOFormatParser;
import org.obolibrary.oboformat.parser.OBOFormatParserException;
import org.obolibrary.oboformat.writer.OBOFormatWriter;


public class coModelTdToOBO {

	private OBODoc m = new OBODoc();
		
	protected static String NL = System.getProperty("line.separator") ;
	
	
	public coModelTdToOBO(){
		Frame frameHeader = new Frame(FrameType.HEADER);
		m.setHeaderFrame(frameHeader);
		frameHeader.addClause(new Clause("default-namespace","cco"));
		frameHeader.addClause(new Clause("auto-generated-by","java"));
		
	}
	

	public void save(String file){
		
		OBOFormatWriter output = new OBOFormatWriter();
		try {
			output.write(m, file);
			System.out.println("save!");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	// for superclasses 
	public Frame setConcept(String trait, String id, String def, String crop, String cropNum, String type, boolean b) throws FrameMergeException{
		id=id.trim().replaceAll("\"", "");
		trait=trait.trim().replaceAll("\"", "");
		crop=crop.trim().replaceAll("\"", "");
		id=id.replaceAll(" ", "");
		
		
		Frame frameTest = new Frame(FrameType.TERM);
		//System.out.println(cropNum);
		frameTest.setId(cropNum.replaceAll("\"", "")+id);
		frameTest.addClause(new Clause("id", cropNum.replaceAll("\"", "")+id));
		frameTest.addClause(new Clause("name", trait));
		frameTest.addClause(new Clause("namespace", crop+type));
		
		//definition
		if(def!=null && !def.isEmpty()){
			def=def.trim().replaceAll("\"", "");
			frameTest.addClause(new Clause("def", def));	
		}
		
		if(b)
			frameTest.addClause(new Clause("is_a", cropNum.replaceAll("\"", "")+type));
		
		m.addTermFrame(frameTest);

		return frameTest;
	}
	
	public Frame setConcept(String trait, String id, String def, String categoryId, String crop, String lang) throws FrameMergeException{
		lang = lang.toLowerCase().replaceAll("\"", "");
		id=id.trim().replaceAll("\"", "");;
		trait=trait.trim().replaceAll("\"", "");;
		crop=crop.trim().replaceAll("\"", "");;
		id=id.replaceAll(" ", "");

		Frame frameTest = new Frame(FrameType.TERM);

		
		frameTest.setId(id);
		frameTest.addClause(new Clause("id", id));
		if(!trait.isEmpty()){
			frameTest.addClause(new Clause("name", trait));
		}
		frameTest.addClause(new Clause("namespace", crop+"Trait"));
		
		//definition
		if(def!=null && !def.replaceAll("\"", "").isEmpty()){
			def=def.trim().replaceAll("\"", "");;
			frameTest.addClause(new Clause("def", def));	
		}
		
		if(categoryId!=null && !categoryId.replaceAll("\"", "").isEmpty()){
			frameTest.addClause(new Clause("is_a", categoryId.replace(" ", "_").replaceAll("\"", "")));
		}
		
	
		//System.out.println(frameTest);
		
		m.addTermFrame(frameTest);
		//System.out.println(m);
		
		return frameTest;
	}
	
	public Clause setCreator(String name){
		Clause person = new Clause("created_by", name.trim().replaceAll("\"", ""));
		
		return person;
	}
			
	
	public void addCreatorToConcept (Clause creator, Frame concept){
		concept.addClause(creator);
	}
	
	public void addAbbrevToConcept (String abbrev, Frame concept){
		Clause syn = new Clause("synonym", abbrev.trim().replaceAll("\"", ""));
		syn.addValue("EXACT");
		concept.addClause(syn);
	}
	
	public void addAltLabel(String syn , Frame concept){
		Clause syno = new Clause("synonym", syn.trim().replaceAll("\"", ""));
		syno.addValue("EXACT");
		concept.addClause(syno);	}
	
	public Frame setConceptMethod(String trait, String id, String crop, String def, String lang, String source, String Traitconcept) throws FrameMergeException{
		lang = lang.toLowerCase().replaceAll("\"", "");
		id=id.trim().replaceAll("\"", "");
		trait=trait.trim().replaceAll("\"", "");
		crop=crop.trim().replaceAll("\"", "");
		id=id.replaceAll(" ", "");
		
		Frame concept = new Frame(FrameType.TERM);
		
		//label info
		if(trait.isEmpty()){
			trait = "Method of "+Traitconcept.replaceAll("\"", "");
		}
	
		concept.setId(id);
		concept.addClause(new Clause("id", id));
		if(!trait.isEmpty())
			concept.addClause(new Clause("name", trait));
		concept.addClause(new Clause("namespace", crop+"Method"));
		
		//definition
		if(def!=null && !def.replaceAll("\"", "").isEmpty()){
			concept.addClause(new Clause("def", def.replaceAll("\"", "")));	
		}
		
		//source
		if(source!=null &&(!source.isEmpty() || !source.replaceAll("\"", "").isEmpty())){
			concept.addClause(new Clause("xref", source.replaceAll("\"", "")));	
		}
		Clause rel = new Clause("relationship", "method_of");
		rel.addValue(Traitconcept.replaceAll("\"", ""));
		concept.addClause(rel);

		m.addTermFrame(concept);

		
		return concept;
	}
	
	public Frame setExistingMethod(String id, String Traitconcept){
		Frame concept = m.getTermFrame(id.replaceAll("\"", ""));
		
		Clause rel = new Clause("relationship", "method_of");
		rel.addValue(Traitconcept.replaceAll("\"", ""));
		concept.addClause(rel);
		
		return concept;
	}
	
	public void setProperty (String prop) throws FrameMergeException{
		Frame frame = new Frame(FrameType.TYPEDEF);
		frame.setId(prop);
		frame.addClause(new Clause("id",prop));
		frame.addClause(new Clause("name",prop));
		m.addTypedefFrame(frame);
	}
	
	public Frame setConceptScale(String id, String scheme, String unit, String idMethod) throws FrameMergeException{
		id=id.trim().replaceAll("\"", "");
		id=id.replaceAll(" ", "");
		
		Frame concept = new Frame(FrameType.TERM);
		
		concept.setId(id);
		concept.addClause(new Clause("id", id));
		
		concept.addClause(new Clause("namespace", scheme.replaceAll("\"", "")+"Scale"));
		
		//unit
		if(unit!=null){
			if(unit.isEmpty() || unit.replaceAll("\"", "").isEmpty()){
				concept.addClause(new Clause("name", "scale_of "+idMethod.replaceAll("\"", "")));
			}else{
				concept.addClause(new Clause("name", unit.replaceAll("\"", "")));
			}
		}else{
			concept.addClause(new Clause("name", id));
		}
		
		Clause rel = new Clause("relationship", "scale_of");
		rel.addValue(idMethod.replaceAll("\"", ""));
		concept.addClause(rel);
		

		m.addTermFrame(concept);
		
		return concept;
	}
	
	public Frame setExistingScale(String id, String Traitconcept){
		Frame concept = m.getTermFrame(id.replaceAll("\"", ""));
		
		Clause rel = new Clause("relationship", "scale_of");
		rel.addValue(Traitconcept.replaceAll("\"", ""));
		concept.addClause(rel);
		

		return concept;
	}
	
	public Frame setConceptScale(String id, String scheme, String scaleName, String[] tab, String idMethod) throws FrameMergeException{
		id=id.trim().replaceAll("\"", "");
		id=id.replaceAll(" ", "");
		Frame concept = new Frame(FrameType.TERM);
		
		concept.setId(id);
		concept.addClause(new Clause("id", id));
		
		//scale name
		if(scaleName!=null ){
			if(scaleName.isEmpty() || scaleName.replaceAll("\"", "").isEmpty()){
				concept.addClause(new Clause("name", "scale_of "+idMethod.replaceAll("\"", "")));
			}else{
				concept.addClause(new Clause("name", scaleName.replaceAll("\"", "")));
			}
			
		}else{
			concept.addClause(new Clause("name", id));
		}
		
		concept.addClause(new Clause("namespace", scheme.replaceAll("\"", "")+"Scale"));
					
		Clause rel = new Clause("relationship", "scale_of");
		rel.addValue(idMethod.replaceAll("\"", ""));
		concept.addClause(rel);

		m.addTermFrame(concept);
		
		for(int i=0;i<tab.length;i++){
			if(tab[i]!=null && !tab[i].isEmpty()){
				if(tab[i].split("=").length>=2){
					Frame cat = new Frame(FrameType.TERM);
					
					cat.setId(id+":"+tab[i].split("=")[0].replaceAll("\"", ""));
					cat.addClause(new Clause("id", id+"/"+tab[i].split("=")[0].replaceAll("\"", "")));
					
					cat.addClause(new Clause("namespace", scheme.replaceAll("\"", "")+"Scale"));
					cat.addClause(new Clause("is_a", id));
					
					String[] scaleSyn = tab[i].split("=");
					if(scaleSyn.length==2){
						if(!tab[i].split("=")[1].trim().isEmpty() ||
								!tab[i].split("=")[1].trim().replaceAll("\"", "").isEmpty()){
							cat.addClause(new Clause("name", tab[i].split("=")[1].trim().replaceAll("\"", "")));
						}
						if(!tab[i].split("=")[0].trim().isEmpty() ||
								!tab[i].split("=")[0].trim().replaceAll("\"", "").isEmpty()){
							Clause syn = new Clause("synonym", tab[i].split("=")[0].trim().replaceAll("\"", ""));
							syn.addValue("EXACT");
							cat.addClause(syn);
						}
						
					}else if(scaleSyn.length>=3){
						if(!tab[i].split("=")[2].trim().isEmpty() ||
								!tab[i].split("=")[2].trim().replaceAll("\"", "").isEmpty()){
							cat.addClause(new Clause("name", tab[i].split("=")[2].trim().replaceAll("\"", "")));
						}
						if(!tab[i].split("=")[0].trim().isEmpty() ||
								tab[i].split("=")[0].trim().replaceAll("\"", "").isEmpty()){
							Clause syn = new Clause("synonym", tab[i].split("=")[0].trim().replaceAll("\"", ""));
							syn.addValue("EXACT");
							cat.addClause(syn);
						}
						if(!tab[i].split("=")[1].trim().isEmpty() || 
								!tab[i].split("=")[1].trim().replaceAll("\"", "").isEmpty()){
							Clause syn2 = new Clause("synonym", tab[i].split("=")[1].trim().replaceAll("\"", ""));
							syn2.addValue("EXACT");
							cat.addClause(syn2);
						}
						
					}else{
						if(!tab[i].split("=")[0].trim().isEmpty() ||
								!tab[i].split("=")[0].trim().replaceAll("\"", "").isEmpty())
							cat.addClause(new Clause("name", tab[i].split("=")[0].trim().replaceAll("\"", "")));
					}
					
					
					m.addTermFrame(cat);			
					}
			}
		}
		
		

		return concept;
	}

	
	public Frame setVariable(String var, String id, String trait, String method, String scale, String crop, String lang) throws FrameMergeException{
		lang = lang.toLowerCase().replaceAll("\"", "");
		id=id.trim().replaceAll("\"", "");
		var=var.trim().replaceAll("\"", "");
		crop=crop.trim().replaceAll("\"", "");
		id=id.replaceAll(" ", "");
		
		Frame frameTest = new Frame(FrameType.TERM);
		
		frameTest.setId(id);
		frameTest.addClause(new Clause("id", id));
		if(!var.isEmpty() || !var.replaceAll("\"", "").isEmpty()){
			frameTest.addClause(new Clause("name", var));
		}
		
		frameTest.addClause(new Clause("namespace", crop+"Variable"));
		
		Clause rel = new Clause("relationship", "variable_of");
		rel.addValue(trait.replaceAll("\"", ""));		frameTest.addClause(rel);
		rel = new Clause("relationship", "variable_of");
		rel.addValue(method.replaceAll("\"", ""));
		frameTest.addClause(rel);
		rel = new Clause("relationship", "variable_of");
		rel.addValue(scale.replaceAll("\"", ""));
		frameTest.addClause(rel);
		
		m.addTermFrame(frameTest);

		return frameTest;
	}
	
}
