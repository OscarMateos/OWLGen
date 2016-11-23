package owlgenerator.tests.ontoLing;
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

public class LLO {
/////////////////LLO - OntoTag/////////////////
	public static void main(String[] args) throws FileNotExistsException {
		String baseUrl = "http://localhost/";
		String filesPath = "C:\\outaspace\\Tablas OntoTag\\Regeneradas 09052016\\The Linguistic Level Ontology (LLO)";
		String ontologyName = "LLO - OntoLing";

		try {
			File fp = new File(filesPath);
			
			if (fp.exists()) { 
				CoreConfiguration.build(baseUrl, filesPath, ontologyName);
				Core tests = new Core();

			//LLO
				//MAIN CONCEPTS
				tests.addSubClassOfAxiomsFromTables("Table 20 - The subclassification of Linguistic Level within the LLO.txt");
				tests.addSubClassOfAxiomsFromTables("Table 21 - The subclassification of Linguistic Annotation Layer within the LLO.txt");						
				//Stratum
				tests.addSubClassOfAxiomsFromTables("Table 22 - The subclassification of Stratum within the LLO.txt");
				tests.addSubClassOfAxiomsFromTables("Table 23 - The subclassification of Morph Paradigmatic Labelling Stratum within the LLO.txt");
				tests.addSubClassOfAxiomsFromTables("Table 24 - The subclassification of Word Formation Relation Labelling Stratum within the LLO.txt");			
				tests.addSubClassOfAxiomsFromTables("Table 25 - The subclassification of Syntactic Unit Paradigmatic Labelling Stratum within the LLO.txt");
				tests.addSubClassOfAxiomsFromTables("Table 26 - The subclassification of Syntactic Relation Labelling Stratum within the LLO.txt");				
				tests.addSubClassOfAxiomsFromTables("Table 27 - The subclassification of Semantic Unit Labelling Stratum within the LLO.txt");
				tests.addSubClassOfAxiomsFromTables("Table 28 - The subclassification of Proposition Formation Relation Labelling Stratum within the LLO.txt");				
				tests.addSubClassOfAxiomsFromTables("Table 29 - The subclassification of Discourse Unit Paradigmatic Labelling Stratum within the LLO.txt");				
				tests.addSubClassOfAxiomsFromTables("Table 30 - The subclassification of Discourse Relation Labelling Stratum within the LLO.txt");
				tests.addSubClassOfAxiomsFromTables("Table 31 - The subclassification of Pragmatic Unit Paradigmatic Labelling Stratum within the LLO.txt");				
				tests.addSubClassOfAxiomsFromTables("Table 32 - The subclassification of Pragmatic Relation Labelling Stratum within the LLO.txt");		
				//Part-Of
				tests.addPartOfAxiomsFromTable("Table 33 - The Part-Of relations that hold for the concepts in the LLO.txt");							
				//Atributos Ontologicos (DataProperties)
				tests.addAttributesFromTable("Table 34 - Attributes of the concepts in the LLO.txt");		
				//Ad Hoc Relations (ObjectProperties)
				tests.addAdHocRelationsFromTable("Table 35 - The ad hoc relations in the LLO.txt");	
				//Attribute-Value Rules (Restrictions)
				tests.addAttributeRulesFromTable("Table 36 - Rules associated to the attribute values of the LLO concepts.txt");	
				// FOOTNOTES
				FootNotes fn = new FootNotes(); 
				fn.addAxiomsFromFootNotes("Table FN - FootNotes related to The Linguistic Level Ontology (LLO).txt", tests);
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