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

public class LVO {
/////////////////LVO - OntoTag//////////////////
	public static void main(String[] args) throws FileNotExistsException {
		String baseUrl = "http://localhost/";
		String filesPath = "C:\\outaspace\\Tablas OntoTag\\Regeneradas 09052016\\The Linguistic Value Ontology (LVO)";
		String ontologyName = "LVO - OntoTag";

		try {
			File fp = new File(filesPath);
			
			if (fp.exists()) { 
				CoreConfiguration.build(baseUrl, filesPath, ontologyName);
				Core tests = new Core();
				
			//LVO
			//MAIN CONCEPTS
				//Taxonimia de los conceptos principales
				tests.addSubClassOfAxiomsFromTables("Table 73 - The main concepts in the LVO and the taxonomical relations holding between them.txt");
				
			//SYNTACTIC CONCEPTS
				tests.addSubClassOfAxiomsFromTables("Table 74 - The syntactic concepts in the LVO and the taxonomical relations holding between them.txt");
				//INSTANCES:
				tests.addClassIndividualsFromTable("Table 75 - The instances of the subclasses of Morphosyntactic Value in the LVO.txt");
				tests.addClassIndividualsFromTable("Table 76 - The instances of Syntactic Dependency Value in the LVO.txt");
				tests.addClassIndividualsFromTable("Table 77 - The instances of Morphosyntactic Function Value in the LVO.txt");
				tests.addClassIndividualsFromTable("Table 78 - The instances of Phrase Function Value in the LVO.txt");
				tests.addClassIndividualsFromTable("Table 79 - The instances of Syntactic Function Value in the LVO.txt");
				tests.addClassIndividualsFromTable("Table 80 - The instances of Lexical Function Value in the LVO.txt");			
				tests.addClassIndividualsFromTable("Table 81 - The instances of the subclasses of Other Syntactic Value in the LVO.txt");

			//SEMANTIC CONCEPTS					
				tests.addSubClassOfAxiomsFromTables("Table 82 - The semantic concepts in the LVO and the taxonomical relations holding between them.txt");					
				//INSTANCES:			
				tests.addClassIndividualsFromTable("Table 83 - The instances of semantic values in the LVO.txt");					
				//Atributos Ontologicos (DataProperties)
				tests.addAttributesFromTable("Table 84 - Attributes of the Pragmatic Level concepts defined within the LVO.txt");				
				//Attribute-Value Rules (Restrictions)
				tests.addAttributeRulesFromTable("Table 85 - Rules associated to the attribute values of the concepts defined within the LVO.txt");					

			// FOOTNOTES
				FootNotes fn = new FootNotes(); 
				fn.addAxiomsFromFootNotes("Table FN - FootNotes related to The Linguistic Value Ontology (LVO).txt", tests);												
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
