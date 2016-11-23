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

public class LAO {
/////////////////LAO - OntoLing//////////////////
	public static void main(String[] args) throws FileNotExistsException {
		String baseUrl = "http://localhost/";
		String filesPath = "C:\\outaspace\\Tablas OntoTag\\Regeneradas 09052016\\Ficheros de OntoLingAnnot\\Chapter 4 - LAO - 20091203 - OntoLing";
		String ontologyName = "LAO - OntoLing";

		try {	
			File fp = new File(filesPath);
			
			if (fp.exists()) { 
				CoreConfiguration.build(baseUrl, filesPath, ontologyName);
				Core tests = new Core();

			// LAO
				// MAIN CONCEPTS
				tests.addSubClassOfAxiomsFromTables("Table 90 - The main concepts in the LUO and the taxonomical relations holding between them.txt");

				// MORPHOLOGICAL CONCEPTS
				tests.addSubClassOfAxiomsFromTables("Table 91 - The morphological concepts in the LAO and the taxonomical relations holding between them.txt");
				// INSTANCES:
				tests.addClassIndividualsFromTable("Table 92 - The instances of morphological attributes in the LAO.txt");
				// Part-Of	
				tests.addPartOfAxiomsFromTable("Table 93 - The Part-Of relations that hold for the morphological concepts in the LAO.txt");

				// SYNTACTIC CONCEPTS
				tests.addSubClassOfAxiomsFromTables("Table 94 - The syntactic concepts in the LAO and the taxonomical relations holding between them.txt");
				// INSTANCES:
				tests.addClassIndividualsFromTable("Table 95 - The instances of syntactic attributes in the LAO.txt");
				// Part-Of
				tests.addPartOfAxiomsFromTable("Table 96 - The Part-Of relations that hold for the syntactic concepts in the LAO.txt");

				// SEMANTIC CONCEPTS					
				tests.addSubClassOfAxiomsFromTables("Table 97 - The semantic concepts in the LAO and the taxonomical relations holding between them.txt");	
				// INSTANCES:			
				tests.addClassIndividualsFromTable("Table 98 - The instances of semantic attributes in the LAO.txt");
				// Part-Of
				tests.addPartOfAxiomsFromTable("Table 99 - The Part-Of relations that hold for the semantic concepts in the LAO.txt");

				//DISCOURSE CONCEPTS					
				tests.addSubClassOfAxiomsFromTables("Table 126 - The discourse concepts in the LAO and the taxonomical relations holding between them.txt");	
				// INSTANCES:			
				tests.addClassIndividualsFromTable("Table 127 - The instances of discourse attributes in the LAO.txt");
				// Part-Of
				tests.addPartOfAxiomsFromTable("Table 128 - The Part-Of relations that hold for the discourse concepts in the LAO.txt");
					
				// PRAGMATIC CONCEPTS					
				tests.addSubClassOfAxiomsFromTables("Table 129 - The pragmatic concepts in the LAO and the taxonomical relations holding between them.txt");	
				//INSTANCES:			
				tests.addClassIndividualsFromTable("Table 130 - The instances of pragmatic attributes in the LAO.txt");
				// Part-Of
				tests.addPartOfAxiomsFromTable("Table 131 - The Part-Of relations that hold for the pragmatic concepts in the LAO.txt");
				
				//Atributos Ontologicos (DataProperties)
				tests.addAttributesFromTable("Table 132 - Attributes associated to the concepts within the LAO.txt");
				//Attribute-Value Rules (Restrictions)
				tests.addAttributeRulesFromTable("Table 133 - Rules associated to the attribute values of the concepts within the LAO.txt");

				// FOOTNOTES
				FootNotes fn = new FootNotes(); 
				fn.addAxiomsFromFootNotes("Table FN - FootNotes related to Chapter 4 - LAO - 20091203 - OntoLing.txt", tests);		
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
