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
import owlgenerator.java.core.webode.WebODEExtension;

public class OIO {
	public static void main(String[] args) throws FileNotExistsException {
		String baseUrl = "http://localhost/";
		String filesPath = "C:\\outaspace\\Tablas OntoTag\\Regeneradas 09052016\\Ficheros de OntoLingAnnot\\Chapter 4 - OIO - 20091123 - OntoLing";
		String ontologyName = "OIO - OntoLing";		

		try {	
			File fp = new File(filesPath);
			
			if (fp.exists()) { 
				CoreConfiguration.build(baseUrl, filesPath, ontologyName);
				Core tests = new Core();
				WebODEExtension testsWE = new WebODEExtension();

			// OIO
			// MAIN CONCEPTS
				// Taxonomía de los conceptos principales
				testsWE.addConceptsFromGlossary("Table 11 - The OIO concepts.txt");														//WO
				tests.changeOntology(testsWE.getOntology());
				tests.addSubClassOfAxiomsFromTables("Table 12 - The taxonomical relations holding between the OIO concepts.txt");		//C	
				// Part-Of
				tests.addPartOfAxiomsFromTable("Table 13 - The Part-Of relations that hold for the concepts in the OIO.txt");			//C		
				// Atributos Ontologicos (DataProperties)
				tests.addAttributesFromTable("Table 14 - The instance attributes of the concepts within the OIO.txt");					//C
				tests.addAttributesFromTable("Table 15 - The class attributes of the concepts within the OIO.txt");						//C
				// Ad Hoc Relations (ObjectProperties)
				tests.addAdHocRelationsFromTable("Table 16 - The ad hoc relations holding between the concepts of the OIO.txt");		//C				
				// Attribute-Value Rules (Restrictions)
				tests.addAttributeRulesFromTable("Table 18 - Rules associated to the attribute values of the OIO concepts.txt");		//C		
				//INSTANCES:
				tests.addClassIndividualsFromTable("Table 19 - The OIO instances.txt");													//C				
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
