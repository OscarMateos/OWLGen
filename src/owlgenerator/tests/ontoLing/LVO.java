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

public class LVO {
/////////////////LVO - OntoTag//////////////////
	public static void main(String[] args) throws FileNotExistsException {
		String baseUrl = "http://localhost/";
		String filesPath = "C:\\outaspace\\Tablas OntoTag\\Regeneradas 09052016\\Ficheros de OntoLingAnnot\\Chapter 4 - LVO - 20091203 - OntoLing";
		String ontologyName = "LVO - OntoLing";

		try {	
			File fp = new File(filesPath);
			
			if (fp.exists()) { 
				CoreConfiguration.build(baseUrl, filesPath, ontologyName);
				Core tests = new Core();
				
			//LVO
				//MAIN CONCEPTS
				tests.addSubClassOfAxiomsFromTables("Table 1 - The main concepts in the LUO and the taxonomical relations holding between them.txt");
				
				//SYNTACTIC CONCEPTS
				tests.addSubClassOfAxiomsFromTables("Table 2 - The morphological concepts in the LVO and the taxonomical relations holding between them.txt");
				//INSTANCES:
				tests.addClassIndividualsFromTable("Table 3 - The instances of morphological values in the LVO.txt");
				// Part-Of
				tests.addPartOfAxiomsFromTable("Table 4 - The Part-Of relations that hold for the morphological concepts in the LVO.txt");											
				
				//SYNTACTIC CONCEPTS
				tests.addSubClassOfAxiomsFromTables("Table 5 - The syntactic concepts in the LAO and the taxonomical relations holding between them.txt");
				//INSTANCES:
				tests.addClassIndividualsFromTable("Table 6 - The instances of the subclasses of Morphosyntactic Value in the LVO.txt");
				tests.addClassIndividualsFromTable("Table 7 - The instances of Syntactic Dependency Value in the LVO.txt");
				tests.addClassIndividualsFromTable("Table 8 - The instances of Morphosyntactic Function Value in the LVO.txt");
				tests.addClassIndividualsFromTable("Table 9 - The instances of Phrase Function Value in the LVO.txt");
				tests.addClassIndividualsFromTable("Table 10 - The instances of Syntactic Function Value in the LVO.txt");
				tests.addClassIndividualsFromTable("Table 11 - The instances of Lexical Function Value in the LVO.txt");
				tests.addClassIndividualsFromTable("Table 12 - The instances of the subclasses of Other Syntactic Value in the LVO.txt");
				// Part-Of
				tests.addPartOfAxiomsFromTable("Table 13 - The Part-Of relations that hold for the syntactic concepts in the LVO.txt");	
				
				//SEMANTIC CONCEPTS					
				tests.addSubClassOfAxiomsFromTables("Table 14 - The semantic concepts in the LVO and the taxonomical relations holding between them.txt");				
				//INSTANCES:			
				tests.addClassIndividualsFromTable("Table 15 - The instances of semantic values in the LVO.txt");
				// Part-Of
				tests.addPartOfAxiomsFromTable("Table 16 - The Part-Of relations that hold for the semantic concepts in the LVO.txt");	
				
				//DISCOURSE CONCEPTS					
				tests.addSubClassOfAxiomsFromTables("Table 17 - The discourse concepts in the LVO and the taxonomical relations holding between them.txt");				
				//INSTANCES:			
				tests.addClassIndividualsFromTable("Table 18 - The instances of discourse values in the LVO.txt");
				// Part-Of
				tests.addPartOfAxiomsFromTable("Table 19 - The Part-Of relations that hold for the discourse concepts in the LVO.txt");	
								
				//PRAGMATIC CONCEPTS						
				tests.addSubClassOfAxiomsFromTables("Table 20 - The pragmatic concepts in the LVO and the taxonomical relations holding between them.txt");				
				//instances:			
				tests.addClassIndividualsFromTable("Table 21 - The instances of pragmatic values in the LVO.txt");
				// part-of
				tests.addPartOfAxiomsFromTable("Table 22 - The Part-Of relations that hold for the pragmatic concepts in the LVO.txt");	
				//Atributos Ontologicos (DataProperties)
				tests.addAttributesFromTable("Table 23 - Attributes of the Pragmatic Level concepts within the LVO.txt");			
				//Attribute-Value Rules (Restrictions)
				tests.addAttributeRulesFromTable("Table 24 - Rules associated to the value values of the concepts within the LVO.txt");					
				
				// FOOTNOTES
				FootNotes fn = new FootNotes(); 
				fn.addAxiomsFromFootNotes("Table FN - FootNotes related to Chapter 4 - LVO - 20091203 - OntoLing.txt", tests);
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
