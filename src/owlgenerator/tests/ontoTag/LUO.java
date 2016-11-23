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

public class LUO {
/////////////////LUO - OntoTag//////////////////
	public static void main(String[] args) throws FileNotExistsException {
		String baseUrl = "http://localhost/";
		String filesPath = "C:\\outaspace\\Tablas OntoTag\\Regeneradas 09052016\\The Linguistic Units Ontology (LUO)";
		String ontologyName = "LUO - OntoTag";

		try {
			File fp = new File(filesPath);
			
			if (fp.exists()) { 
				CoreConfiguration.build(baseUrl, filesPath, ontologyName);
				Core tests = new Core();

			// LUO
				// MAIN CONCEPTS
				tests.addSubClassOfAxiomsFromTables("Table 26 - The main concepts in the LUO and the taxonomical relations holding between them.txt");
				// Atributos Ontologicos (DataProperties)
				tests.addAttributesFromTable("TablasExtra Y Division39\\Table EXb - Attributes of the linguistic units within the LUO.txt");
				///(*)
					
				// MORPHOLOGICAL MODULE:
				///(*)
				// Taxonomía de los conceptos principales					
				tests.addSubClassOfAxiomsFromTables("TablasExtra Y Division39\\Table EXa - The morphological super-concepts in the LUO and their taxonomical relations.txt");
				///(*)
				// Attribute-Value Rules (Restrictions)
				tests.addAttributeRulesFromTable("TablasExtra Y Division39\\Table EXc - Rules associated to the attribute values of the LUO morphological units.txt");
				///(*)
				
				// SYNTACTIC MODULE:
				// Taxonomía de los conceptos principales
				tests.addSubClassOfAxiomsFromTables("Table 28 - The syntactic super-concepts in the LUO and their taxonomical relations.txt");
				// Part-Of
				tests.addPartOfAxiomsFromTable("Table 41 - The Part-Of relations that hold for the syntactic concepts in the LUO.txt");
				
				// Atributos (DataProperties)
				tests.addAttributesFromTable("Table 42 - Attributes of the Syntactic Level units within the LUO.txt");
				// Ad Hoc Relations (ObjectProperties)
				tests.addAdHocRelationsFromTable("Table 43 - Syntactic ad hoc relations in the LUO.txt");
				// Attribute-Value Rules (Restrictions)
				tests.addAttributeRulesFromTable("Table 44 - Rules associated to the attribute values of the LUO syntactic units.txt");
				// INSTANCES:
				tests.addClassIndividualsFromTable("TablasExtra Y Division39\\Table 39b - The Punctuation Mark instances.txt");

				// SEMANTIC MODULE:
				// Taxonomía de los conceptos principales
				tests.addSubClassOfAxiomsFromTables("Table 45 - The subclasses of Semantic Unit in the LUO and their taxonomical relations.txt");
				
				// Atributos (DataProperties)
				tests.addAttributesFromTable("Table 60 - Attributes of the semantic units within the LUO.txt");
				// Ad Hoc Relations (ObjectProperties)
				tests.addAdHocRelationsFromTable("Table 61 - Semantic ad hoc relations in the LUO.txt");
				// Attribute-Value Rules (Restrictions)
				tests.addAttributeRulesFromTable("Table 62 - Rules associated to the attribute values of the LUO semantic units.txt");

				// FOOTNOTES
				FootNotes fn = new FootNotes(); 
				fn.addAxiomsFromFootNotes("Table FN - FootNotes related to The Linguistic Units Ontology (LUO).txt", tests);							
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
