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

public class CambiosLAO {
/////////////////Cambios LAO - OntoTag-OntoLing//////////////////
	public static void main(String[] args) throws FileNotExistsException {
		String baseUrl = "http://localhost/";
		String filesPath = "C:\\outaspace\\Tablas OntoTag\\Regeneradas 09052016\\Ficheros de OntoLingAnnot\\Cambios LAO - OntoTag-OntoLing";
		String ontologyName = "Cambios LAO - OntoTag-OntoLing"; 

		try {
			File fp = new File(filesPath);
			
			if (fp.exists()) { 
				CoreConfiguration.build(baseUrl, filesPath, ontologyName);
				Core tests = new Core();

			//CAMBIOS LAO
				//MORPHOLOGICAL CONCEPTS
				tests.addSubClassOfAxiomsFromTables("Table 1 - The morphological concepts in the LAO and the taxonomical relations holding between them.txt");
				//INSTANCES:
				tests.addClassIndividualsFromTable("Table 2 - The instances of morphological attributes in the LAO.txt");

				//SYNTACTIC CONCEPTS
				tests.addSubClassOfAxiomsFromTables("Table 3 - The syntactic concepts in the LAO and the taxonomical relations holding between them.txt");
				//INSTANCES:
				tests.addClassIndividualsFromTable("Table 4 - The instances of syntactic attributes in the LAO.txt");

				//SEMANTIC CONCEPTS					
				tests.addSubClassOfAxiomsFromTables("Table 5 - The semantic concepts in the LAO and the taxonomical relations holding between them.txt");	
				//INSTANCES:			
				tests.addClassIndividualsFromTable("Table 6 - The instances of semantic attributes in the LAO.txt");
				
				//DISCOURSE CONCEPTS					
				tests.addSubClassOfAxiomsFromTables("Table 7 - The discourse concepts in the LAO and the taxonomical relations holding between them.txt");	
				//INSTANCES:			
				tests.addClassIndividualsFromTable("Table 8 - The instances of discourse attributes in the LAO.txt");
				
				//PRAGMATIC CONCEPTS					
				tests.addSubClassOfAxiomsFromTables("Table 9 - The pragmatic concepts in the LAO and the taxonomical relations holding between them.txt");	
				//INSTANCES:			
				tests.addClassIndividualsFromTable("Table 10 - The instances of pragmatic attributes in the LAO.txt");
		
				// FOOTNOTES
				FootNotes fn = new FootNotes(); 
				fn.addAxiomsFromFootNotes("Table FN - FootNotes related to Cambios LAO - OntoTag-OntoLing.txt", tests);						
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
