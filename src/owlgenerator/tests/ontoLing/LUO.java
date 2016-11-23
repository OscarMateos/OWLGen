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

public class LUO {
/////////////////LUO - OntoTag//////////////////
	public static void main(String[] args) throws FileNotExistsException {
		String baseUrl = "http://localhost/";
		String filesPath = "C:\\outaspace\\Tablas OntoTag\\Regeneradas 09052016\\Ficheros de OntoLingAnnot\\Chapter 4 - LUO - 20091129 - OntoLing";
		String ontologyName = "LUO - OntoLing";

		try {	
			File fp = new File(filesPath);
			
			if (fp.exists()) { 
				CoreConfiguration.build(baseUrl, filesPath, ontologyName);
				Core tests = new Core();

				// LUO
				// MAIN CONCEPTS
				// Taxonomía de los conceptos principales
				tests.addSubClassOfAxiomsFromTables("Table 37 - The main concepts in the LUO and the taxonomical relations holding between them.txt");
				// Atributos Ontologicos (DataProperties)
				tests.addAttributesFromTable("TablasExtra\\Table EXb - Attributes of the linguistic units within the LUO.txt");//(*)
				tests.addAttributesFromTable("Table 38 - (Ontological) Attributes associated to the concept Linguistic Unit.txt");			
					
				// MORPHOLOGICAL MODULE:
				// Taxonomía de los conceptos principales					
				tests.addSubClassOfAxiomsFromTables("Table 39 - The main morphological concepts in the LUO and the taxonomical relations holding between them.txt");		
				tests.addSubClassOfAxiomsFromTables("TablasExtra\\Table EXa - The morphological super-concepts in the LUO and their taxonomical relations.txt");//(*)
				// Part-Of
				tests.addPartOfAxiomsFromTable("Table 42 - The Part-Of relations that hold for the morphological concepts in the LUO.txt");
				// Atributos Ontologicos (DataProperties)
				tests.addAttributesFromTable("Table 43 - Attributes of the Morphological Level units of the LUO.txt");
				// Ad Hoc Relations (ObjectProperties)
				tests.addAdHocRelationsFromTable("Table 44 - The ad hoc relations holding between the concepts of the OIO.txt");
				tests.addAdHocRelationsFromTable("Table 46 - Rules associated to conditional Part-Of  relations between morphological units.txt");
				// Attribute-Value Rules (Restrictions)
				tests.addAttributeRulesFromTable("TablasExtra\\Table EXc - Rules associated to the attribute values of the LUO morphological units.txt");//(*)
				tests.addAttributeRulesFromTable("Table 45 - Rules associated to the attribute values of the LUO morphological units.txt");			

				// SYNTACTIC MODULE:
				// Taxonomía de los conceptos principales
				tests.addSubClassOfAxiomsFromTables("Table 48 - The syntactic super-concepts in the LUO and their taxonomical relations.txt");
				// Part-Of
				tests.addPartOfAxiomsFromTable("Table 63 - The Part-Of relations that hold for the syntactic concepts in the LUO.txt");	
				// Atributos (DataProperties)
				tests.addAttributesFromTable("Table 64 - Attributes of the Syntactic Level units within the LUO.txt");	
				// Ad Hoc Relations (ObjectProperties)
				tests.addAdHocRelationsFromTable("Table 65 - Syntactic ad hoc relations in the LUO.txt");
				tests.addAdHocRelationsFromTable("Table 67 - Rules associated to conditional Part-Of relations between morphological units.txt");
				// Attribute-Value Rules (Restrictions)
				tests.addAttributeRulesFromTable("Table 66 - Rules associated to the attribute values of the LUO syntactic units.txt");
				
				// SEMANTIC MODULE:
				// Taxonomía de los conceptos principales	
				tests.addSubClassOfAxiomsFromTables("Table 68 - The semantic super-concepts in the LUO and their taxonomical relations.txt");
				// Part-Of
				tests.addPartOfAxiomsFromTable("Table 86 - The Part-Of relations that hold for the semantic concepts in the LUO.txt");	
				// Atributos (DataProperties)
				tests.addAttributesFromTable("Table 87 - Attributes of the semantic units within the LUO.txt");
				// Ad Hoc Relations (ObjectProperties)
				tests.addAdHocRelationsFromTable("Table 88 - Semantic ad hoc relations in the LUO.txt");
				// Attribute-Value Rules (Restrictions)
				tests.addAttributeRulesFromTable("Table 89 - Rules associated to the attribute values of the LUO semantic units.txt");
				
				// DISCOURSE MODULE:
				// Taxonomía de los conceptos principales	
				tests.addDisjointDecompositionsFromTable("Table 94 - The Disjoint-Decomposition of the concepts Circumstance DFU and Ideational Sequence DFU.txt");
				tests.addDisjointDecompositionsFromTable("Table 95 - The Disjoint-Decomposition of the concepts Comparative DFU and General Condition DFU.txt");
				tests.addSubClassOfAxiomsFromTables("Table 90 - The main discourse concepts in the LUO and the taxonomical relations holding between them.txt");
				// Part-Of
				tests.addPartOfAxiomsFromTable("Table 97 - The Part-Of relations that hold for the discourse concepts in the LUO.txt");	
				// Atributos (DataProperties)
				tests.addAttributesFromTable("Table 98 - Attributes of the discourse units within the LUO.txt");
				// Attribute-Value Rules (Restrictions)
				tests.addAttributeRulesFromTable("Table 99 - Rules associated to the attribute values of the LUO discourse units.txt");
				
				// DISCOURSE MODULE:
				// Taxonomía de los conceptos principales	
				tests.addSubClassOfAxiomsFromTables("Table 100 - The pragmatic super-concepts in the LUO and their taxonomical relations.txt");
				// Part-Of
				tests.addPartOfAxiomsFromTable("Table 112 - The Part-Of relations that hold for the pragmatic concepts in the LUO.txt");	
				// Atributos (DataProperties)
				tests.addAttributesFromTable("Table 113 - Attributes of the pragmatic units within the LUO.txt");
				// Attribute-Value Rules (Restrictions)
				tests.addAttributeRulesFromTable("Table 114 - Rules associated to the attribute values of the LUO pragmatic units.txt");
				//(* Este incluye inconsistencias por restricciones a TRUE/FALSE que NO es boolean)
				
				// FOOTNOTES
				FootNotes fn = new FootNotes(); 
				fn.addAxiomsFromFootNotes("Table FN - FootNotes related to Chapter 4 - LUO - 20091129 - OntoLing.txt", tests);	
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
