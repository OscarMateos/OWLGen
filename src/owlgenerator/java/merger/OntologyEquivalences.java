/*
 * The Class OntologyEquivalences for ontology mapping
 */
package owlgenerator.java.merger;

import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.OWLXMLDocumentFormat;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.rdf.rdfxml.renderer.OWLOntologyXMLNamespaceManager;

/**
 * The Class OntologyEquivalences for ontology mapping
 * 
 * @author Oscar Mateos Lopez 
 * @version: 20161103
 */

public class OntologyEquivalences {
	// EQUIVALENCES
	/**
	 * Adds the equivalent concepts.
	 *
	 * @param sourceOntology1 the source ontology 1
	 * @param sourceOntology2 the source ontology 2
	 * @param targetOntology the target ontology
	 */
	// Equivalent Classes (between)
	public static void addEquivalentConcepts(OWLOntology sourceOntology1, OWLOntology sourceOntology2, OWLOntology targetOntology) {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory();
		
		// get the Set of the common Concepts
		Set<String> commonConcepts = OntologyData.getSameNameConceptsAsString(sourceOntology1, sourceOntology2);
		
		// Perform equivalences
		if(!commonConcepts.isEmpty()) {
			// get 1st Ontology Namespace
			OWLXMLDocumentFormat owlFormat = new OWLXMLDocumentFormat();
			OWLOntologyXMLNamespaceManager namespaceManager = new OWLOntologyXMLNamespaceManager(sourceOntology1,  owlFormat);
			String sourceOntology1Namespace = namespaceManager.getDefaultNamespace();
			
			// get 2nd Ontology Namespace
			namespaceManager = new OWLOntologyXMLNamespaceManager(sourceOntology2,  owlFormat);
			String sourceOntology2Namespace = namespaceManager.getDefaultNamespace();
			
			// Relate the common Concepts from the Set
			for(String commonConcept : commonConcepts) {
				// get the IRIs for the Concept in both Ontologies
				IRI ontology1Concept = IRI.create(sourceOntology1Namespace + commonConcept), ontology2Concept = IRI.create(sourceOntology2Namespace + commonConcept);
				
				// Relate both concepts on the Target Ontology
				OWLClass ontology1Class = factory.getOWLClass(ontology1Concept), ontology2Class = factory.getOWLClass(ontology2Concept);
				OWLEquivalentClassesAxiom equivalenceAxiom = factory.getOWLEquivalentClassesAxiom(ontology1Class, ontology2Class);			
				manager.applyChange(new AddAxiom(targetOntology, equivalenceAxiom));				
			}
		}
	}
	
	/**
	 * Adds the equivalent concepts.
	 *
	 * @param ontology the ontology
	 */
	// Equivalent Classes (within)
	public static void addEquivalentConcepts(OWLOntology ontology) {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory();
		
		// get the Set of the common Concepts
		Set<String> commonConcepts = OntologyData.getSameNameConceptsAsString(ontology);
		
		// Perform equivalences
		if(!commonConcepts.isEmpty()) {
			for(String commonConcept : commonConcepts) {
				// get the common Concepts as Classes
				Set<OWLClass> commonClasses = OntologyData.getParticularNameClasses(commonConcept, ontology);
				// Relate the common Classes from the Set
				OWLEquivalentClassesAxiom equivalenceAxiom = factory.getOWLEquivalentClassesAxiom(commonClasses);
				manager.applyChange(new AddAxiom(ontology, equivalenceAxiom));
			}
		}
	}
	
	/**
	 * Adds the equivalent attributes.
	 *
	 * @param sourceOntology1 the source ontology 1
	 * @param sourceOntology2 the source ontology 2
	 * @param targetOntology the target ontology
	 */
	// Equivalent Data Properties (between)
	public static void addEquivalentAttributes(OWLOntology sourceOntology1, OWLOntology sourceOntology2, OWLOntology targetOntology) {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory();
		
		// get the Set of the common Data Properties
		Set<String> commonDataProperties = OntologyData.getSameNameAttributesAsString(sourceOntology1, sourceOntology2);
		
		// Perform equivalences
		if(!commonDataProperties.isEmpty()) {
			// get 1st Ontology Namespace
			OWLXMLDocumentFormat owlFormat = new OWLXMLDocumentFormat();
			OWLOntologyXMLNamespaceManager namespaceManager = new OWLOntologyXMLNamespaceManager(sourceOntology1,  owlFormat);
			String sourceOntology1Namespace = namespaceManager.getDefaultNamespace();
			
			// get 2nd Ontology Namespace
			namespaceManager = new OWLOntologyXMLNamespaceManager(sourceOntology2,  owlFormat);
			String sourceOntology2Namespace = namespaceManager.getDefaultNamespace();
			
			// Relate the common Data Properties from the Set
			for(String commonDataProperty : commonDataProperties) {
				// get the IRIs for the Data Property in both Ontologies
				IRI ontology1DataProperty = IRI.create(sourceOntology1Namespace + commonDataProperty), 
						ontology2DataProperty = IRI.create(sourceOntology2Namespace + commonDataProperty);
				
				// Relate both concepts on the Target Ontology
				OWLDataProperty ontology1Class = factory.getOWLDataProperty(ontology1DataProperty), ontology2Class = factory.getOWLDataProperty(ontology2DataProperty);
				OWLEquivalentDataPropertiesAxiom equivalenceAxiom = factory.getOWLEquivalentDataPropertiesAxiom(ontology1Class, ontology2Class);			
				manager.applyChange(new AddAxiom(targetOntology, equivalenceAxiom));				
			}
		}		
	}

	/**
	 * Adds the equivalent attributes.
	 *
	 * @param ontology the ontology
	 */
	// Equivalent Data Properties (within)
	public static void addEquivalentAttributes(OWLOntology ontology) {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory();
		
		// get the Set of the common Attributes
		Set<String> commonAttributes = OntologyData.getSameNameAttributesAsString(ontology);
		
		// Perform equivalences
		if(!commonAttributes.isEmpty()) {
			for(String commonConcept : commonAttributes) {
				// get the common Attributes as Data Properties
				Set<OWLDataProperty> commonDataProperties = OntologyData.getParticularNameDataProperties(commonConcept, ontology);
				// Relate the common Data Properties from the Set
				OWLEquivalentDataPropertiesAxiom equivalenceAxiom = factory.getOWLEquivalentDataPropertiesAxiom(commonDataProperties);
				manager.applyChange(new AddAxiom(ontology, equivalenceAxiom));
			}
		}
	}	
	
	/**
	 * Generate equivalent ad hoc relations.
	 *
	 * @param sourceOntology1 the source ontology 1
	 * @param sourceOntology2 the source ontology 2
	 * @param targetOntology the target ontology
	 */
	// Equivalent Object Properties (between)
	public static void generateEquivalentAdHocRelations(OWLOntology sourceOntology1, OWLOntology sourceOntology2, OWLOntology targetOntology) {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory();
		
		// get the Set of the common Object Properties
		Set<String> commonObjectProperties = OntologyData.getSameNameAttributesAsString(sourceOntology1, sourceOntology2);
		
		// Perform equivalences
		if(!commonObjectProperties.isEmpty()) {
			// get 1st Ontology Namespace
			OWLXMLDocumentFormat owlFormat = new OWLXMLDocumentFormat();
			OWLOntologyXMLNamespaceManager namespaceManager = new OWLOntologyXMLNamespaceManager(sourceOntology1,  owlFormat);
			String sourceOntology1Namespace = namespaceManager.getDefaultNamespace();
			
			// get 2nd Ontology Namespace
			namespaceManager = new OWLOntologyXMLNamespaceManager(sourceOntology2,  owlFormat);
			String sourceOntology2Namespace = namespaceManager.getDefaultNamespace();
			
			// Relate the common Object Properties from the Set
			for(String commonObjectProperty : commonObjectProperties) {
				// get the IRIs for the Object Property in both Ontologies
				IRI ontology1ObjectProperty = IRI.create(sourceOntology1Namespace + commonObjectProperty), 
					ontology2ObjectProperty = IRI.create(sourceOntology2Namespace + commonObjectProperty);
				
				// Relate both concepts on the Target Ontology
				OWLObjectProperty ontology1Class = factory.getOWLObjectProperty(ontology1ObjectProperty), 
						ontology2Class = factory.getOWLObjectProperty(ontology2ObjectProperty);			
				OWLEquivalentObjectPropertiesAxiom equivalenceAxiom = factory.getOWLEquivalentObjectPropertiesAxiom(ontology1Class, ontology2Class);			
				manager.applyChange(new AddAxiom(targetOntology, equivalenceAxiom));				
			}
		}		
	}
	
	/**
	 * Adds the equivalent ad hoc relations.
	 *
	 * @param ontology the ontology
	 */
	// Equivalent Object Properties (within)
	public static void addEquivalentAdHocRelations(OWLOntology ontology) {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory();
		
		// get the Set of the common AdHocRelations
		Set<String> commonAdHocRelations = OntologyData.getSameNameAdHocRelationsAsString(ontology);
		
		// Perform equivalences
		if(!commonAdHocRelations.isEmpty()) {
			for(String commonConcept : commonAdHocRelations) {
				// get the common AdHocRelations as Object Properties
				Set<OWLObjectProperty> commonObjectProperties = OntologyData.getParticularNameObjectProperties(commonConcept, ontology);
				// Relate the common Object Properties from the Set
				OWLEquivalentObjectPropertiesAxiom equivalenceAxiom = factory.getOWLEquivalentObjectPropertiesAxiom(commonObjectProperties);
				manager.applyChange(new AddAxiom(ontology, equivalenceAxiom));
			}
		}
	}
}
