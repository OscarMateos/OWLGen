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

public class LRO {
/////////////////LRO - OntoLing//////////////////
	public static void main(String[] args) throws FileNotExistsException {
		String baseUrl = "http://localhost/";
		String filesPath = "C:\\outaspace\\Tablas OntoTag\\Regeneradas 09052016\\Ficheros de OntoLingAnnot\\Chapter 4 - LRO - 20090105 - OntoLing";
		String ontologyName = "LRO - OntoLing";

		try {	
			File fp = new File(filesPath);
			
			if (fp.exists()) { 
				CoreConfiguration.build(baseUrl, filesPath, ontologyName);
				Core tests = new Core();

			// LRO			
				// MAIN CONCEPTS
				// Taxonomy
				// Subclass-Of
				tests.addSubClassOfAxiomsFromTables("Table 156 - The top-level concepts in the LRO and the taxonomical relations holding between them.txt");
				// Part-Of	
				tests.addPartOfAxiomsFromTable("Table 157 - The Part-Of relations that hold between the top-level concepts in the LRO.txt");
	
				// MORPHOLOGICAL CONCEPTS
				// Taxonomy
				// Subclass-Of
				tests.addSubClassOfAxiomsFromTables("Table 158 - The morphological concepts in the LRO and the taxonomical relations holding between them.txt");
				// Part-Of	
				tests.addPartOfAxiomsFromTable("Table 159 - The Part-Of relations that hold between the morphological concepts in the LRO.txt");
				
				// SYNTACTIC CONCEPTS
				// Taxonomy
				// Subclass-Of
				tests.addSubClassOfAxiomsFromTables("Table 160 - The syntactic concepts in the LRO and the taxonomical relations holding between them.txt");				
				// Part-Of	
				tests.addPartOfAxiomsFromTable("Table 162 - The Part-Of relations that hold between the syntactic concepts in the LRO.txt");
				// INSTANCES:
				tests.addClassIndividualsFromTable("Table 161 - The syntactic instances in the LRO.txt");		
				
				// SEMANTIC CONCEPTS
				// Taxonomy
				// Subclass-Of
				tests.addSubClassOfAxiomsFromTables("Table 163 - The semantic concepts in the LRO and the taxonomical relations holding between them.txt");											
				// Part-Of	
				tests.addPartOfAxiomsFromTable("Table 165 - The Part-Of relations that hold between the semantic concepts in the LRO.txt");													
				// INSTANCES:
				tests.addClassIndividualsFromTable("Table 164 - The semantic instances in the LRO.txt");
				
				//DISCOURSE CONCEPTS	
				// Taxonomy
				// Disjoint Decompositions
				tests.addDisjointDecompositionsFromTable("Table 169 - The Disjoint-Decomposition of the concepts Circumstance Relation and Ideational Sequence.txt");
				tests.addDisjointDecompositionsFromTable("Table 170 - The Disjoint-Decomposition of the concepts Comparative Relation and General Condition Relation.txt");
				// Subclass-Of
				tests.addSubClassOfAxiomsFromTables("Table 166 - The discourse super-concepts in the LRO and their taxonomical relations.txt");	
				// Part-Of
				tests.addPartOfAxiomsFromTable("Table 173 - The Part-Of relations that hold between the discourse concepts in the LRO.txt");
				
				// PRAGMATIC CONCEPTS
				// Taxonomy
				// Subclass-Of
				tests.addSubClassOfAxiomsFromTables("Table 174 - The pragmatic super-concepts in the LRO and their taxonomical relations.txt");	
				// Part-Of
				tests.addPartOfAxiomsFromTable("Table 180 - The Part-Of relations that hold between the pragmatic concepts in the LRO.txt");	
				
				//Atributos Ontologicos (DataProperties)
				tests.addAttributesFromTable("Table 181 - Attributes included within the LRO.txt");
				//Ad Hoc Relations (ObjectProperties)
				tests.addAdHocRelationsFromTable("Table 182 - Secondary ad hoc relations in the LRO.txt");
	
				// FOOTNOTES
				FootNotes fn = new FootNotes();
				fn.addAxiomsFromFootNotes("Table FN - FootNotes related to Chapter 4 - LRO - 20090105 - OntoLing.txt", tests);		
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
