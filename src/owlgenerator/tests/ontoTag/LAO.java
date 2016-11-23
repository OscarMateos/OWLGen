package owlgenerator.tests.ontoTag;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import javax.management.InstanceAlreadyExistsException;

import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.xadisk.filesystem.exceptions.FileNotExistsException;

import owlgenerator.java.core.Core;
import owlgenerator.java.core.CoreConfiguration;
import owlgenerator.java.footnotes.FootNotes;

public class LAO {
/////////////////LAO - OntoTag//////////////////
	public static void main(String[] args) throws FileNotExistsException {
		String baseUrl = "http://localhost/";
		String filesPath = "C:\\outaspace\\Tablas OntoTag\\Regeneradas 09052016\\The Linguistic Attribute Ontology (LAO)";
		String ontologyName = "LAO - OntoTag";

		try {
			File fp = new File(filesPath);
			
			if (fp.exists()) { 
				CoreConfiguration.build(baseUrl, filesPath, ontologyName);
				Core tests = new Core();

			//LAO
				//MAIN CONCEPTS
				//Taxonimia de los conceptos principales
				tests.addSubClassOfAxiomsFromTables("Table 64 - The main concepts in the LAO and the taxonomical relations holding between them.txt");	
				//SYNTACTIC CONCEPTS
				tests.addSubClassOfAxiomsFromTables("Table 65 - The syntactic concepts in the LAO and the taxonomical relations holding between them.txt");
				//INSTANCES:
				tests.addClassIndividualsFromTable("Table 66 - The instances of syntactic attributes in the LAO.txt");		
				//SEMANTIC CONCEPTS					
				tests.addSubClassOfAxiomsFromTables("Table 67 - The semantic concepts in the LAO and the taxonomical relations holding between them.txt");				
				//INSTANCES:			
				tests.addClassIndividualsFromTable("Table 68 - The instances of semantic attributes in the LAO.txt");				
				//Atributos Ontologicos (DataProperties)
				tests.addAttributesFromTable("Table 69 - Attributes associated to the concepts within the LAO.txt");			
				//Attribute-Value Rules (Restrictions)
				tests.addAttributeRulesFromTable("Table 70 - Rules associated to the attribute values of the concepts within the LAO.txt");								
				// FOOTNOTES
				FootNotes fn = new FootNotes(); 
				fn.addAxiomsFromFootNotes("Table FN - FootNotes related to The Linguistic Attribute Ontology (LAO).txt", tests);	
			///////////////////
				String fileName = ontologyName + " - " + tests.getCfg().getOntology_Version() + ".owl";
				OutputStream os = new FileOutputStream(new File("C:\\outaspace\\Out\\" + fileName));
				tests.getManager().saveOntology(tests.getOntology(), os);	
			}	
			else 
				throw new FileNotExistsException(filesPath);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (OWLOntologyStorageException e) {
			e.printStackTrace();
		} catch (InstanceAlreadyExistsException e) {
			e.printStackTrace();
		} 
	}
}
