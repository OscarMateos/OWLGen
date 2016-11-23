package owlgenerator.tests.ontoTag;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import javax.management.InstanceAlreadyExistsException;

import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.xadisk.filesystem.exceptions.FileNotExistsException;

import owlgenerator.java.core.CoreConfiguration;
import owlgenerator.java.core.webode.WebODEExtension;
import owlgenerator.java.footnotes.FootNotes;

public class OIO {
	public static void main(String[] args) throws FileNotExistsException {
		String baseUrl = "http://localhost/";
		String filesPath = "C:\\outaspace\\Tablas OntoTag\\Regeneradas 09052016\\The OntoTag Integration Ontology (OIO)_POST_REFACT_CAMBIOS";
		String ontologyName = "OIO - OntoTag";

		try {	
			File fp = new File(filesPath);
			
			if (fp.exists()) { 
				CoreConfiguration.build(baseUrl, filesPath, ontologyName);
				WebODEExtension tests = new WebODEExtension();

				//Concepts
				tests.addConceptsFromGlossary("Table 9 - OIO glossary of terms (extracted from WebODE) – concepts.txt");
				//Taxonomy
				tests.addDisjointDecompositionsFromTable("Table 15 - OIO Disjoint-Decompositions (extracted from WebODE).txt");
				tests.addExhaustiveDecompositionsFromTable("Table 16 - OIO Exhaustive-Decompositions (extracted from WebODE).txt");
				tests.addSubClassOfAxiomsFromTables("Table 14 - OIO Subclass-Of relations (extracted from WebODE).txt");
				tests.addPartOfAxiomsFromTable("Table 17 - OIO Part-Of relations (extracted from WebODE).txt");		
				
				//Class/Instance Attributes
				tests.addAttributesFromGlosary("Table 11 - OIO glossary of terms (extracted from WebODE) – attributes.txt");
				tests.addAttributeAxiomsFromTable("Table 20 - OIO instance attribute table (extracted from WebODE).txt");
				tests.addAttributeAxiomsFromTable("Table 21 - OIO class attribute table (extracted from WebODE).txt");
				
				//AdHoc Relations
				tests.addAdHocRelationsFromGlosary("Table 12 - OIO glossary of terms (extracted from WebODE) – ad hoc relations.txt");
				tests.addAdHocRelationAxiomsFromTable("Table 19 - OIO ad hoc relationships (extracted from WebODE).txt");
				
				//Instances
				tests.addInstancesFromGlossary("Table 13 - Instances of the OIO Linguistic Annotation Tool Instance Set (extracted from WebODE).txt");
				tests.addInstanceAssertionsFromTable("Table 24 - OIO instance table.txt");				
				
				//Footnotes
				FootNotes fn = new FootNotes(); 
				fn.addAxiomsFromFootNotes("Table FN - FootNotes related to The OntoTag Integration Ontology (OIO).txt", tests);		
				///////////////////
				String fileName = ontologyName + " - " + tests.getCfg().getOntology_Version() + ".owl";
				OutputStream os = new FileOutputStream(new File("C:\\outaspace\\Out\\" + fileName));
				tests.getManager().saveOntology(tests.getOntology(), os);	
			}	
			else 
				throw new FileNotExistsException(filesPath);
		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
		catch (OWLOntologyStorageException e) {
			e.printStackTrace();
		} 
		catch (InstanceAlreadyExistsException e) {
			e.printStackTrace();
		}
	}
}
