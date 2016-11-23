/*
 * The Class OntologyMerger for merging and mapping ontologies.
 */
package owlgenerator.java.merger;

import static owlgenerator.java.merger.StringSimilarityDistance.SIMILARITY_THRESHOLD;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.OWLXMLDocumentFormat;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataOneOf;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDatatypeDefinitionAxiom;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.rdf.rdfxml.renderer.OWLOntologyXMLNamespaceManager;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semanticweb.owlapi.util.OWLEntityRenamer;

import edu.stanford.nlp.util.Pair;

/**
 * The Class OntologyMerger for merging and mapping ontologies.
 *
 * @author Oscar Mateos Lopez
 * @version: 20161103
 */

public class OntologyMerger {
	// TRANSFORMATIONS
	/**
	 * Import annotations for transformed entity.
	 *
	 * @param originalEntity the original entity
	 * @param transformedEntity the transformed entity
	 * @param sourceAttributeOntology the source attribute ontology
	 * @param targetOntology the target ontology
	 */
	// Annotations Seeker 
	private static void importAnnotationsForTransformedEntity(OWLEntity originalEntity, OWLEntity transformedEntity, OWLOntology sourceAttributeOntology, OWLOntology targetOntology) {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory();	
		
		// Annotations
		// rdfs:label
		OWLAnnotation label = EntitySearcher.getAnnotations(originalEntity, sourceAttributeOntology, factory.getRDFSLabel()).iterator().next();
		OWLAxiom labelAxiom = factory.getOWLAnnotationAssertionAxiom(transformedEntity.getIRI() , label);
		manager.applyChange(new AddAxiom(targetOntology, labelAxiom));	
		
		// rdfs:comment
		Collection<OWLAnnotation> comments = EntitySearcher.getAnnotations(originalEntity, sourceAttributeOntology, factory.getRDFSComment());
		if(comments.size() > 0) {
			for(OWLAnnotation comment_ : comments) {
				OWLAxiom commentAxiom = factory.getOWLAnnotationAssertionAxiom(transformedEntity.getIRI() , comment_);
				manager.applyChange(new AddAxiom(targetOntology, commentAxiom));	
			}
		}	
		// rdfs:seeAlso
		Collection<OWLAnnotation> seeAlsos = EntitySearcher.getAnnotations(originalEntity, sourceAttributeOntology, factory.getRDFSSeeAlso());	
		if(seeAlsos.size() > 0) {
			for(OWLAnnotation seeAlso_ : seeAlsos) {
				OWLAxiom seeAlsoAxiom = factory.getOWLAnnotationAssertionAxiom(transformedEntity.getIRI() , seeAlso_);
				manager.applyChange(new AddAxiom(targetOntology, seeAlsoAxiom));	
			}
		}
	}
		
	/**
	 * Adds the classes as data properties.
	 *
	 * @param sourceAttributeOntology the source attribute ontology
	 * @param targetOntology the target ontology
	 */
	// Classes -> Data Properties
	public static void addClassesAsDataProperties(OWLOntology sourceAttributeOntology, OWLOntology targetOntology) {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory();
		Set<String> processedProperties = new HashSet<String>();

		// Get targetOntology Namespace
		OWLXMLDocumentFormat owlFormat = new OWLXMLDocumentFormat();
		OWLOntologyXMLNamespaceManager namespaceManager = new OWLOntologyXMLNamespaceManager(targetOntology,  owlFormat);
		String targetOntologyNamespace = namespaceManager.getDefaultNamespace();
		
		// Get all SubclassOf relations 
		Set<OWLSubClassOfAxiom> namedSubclassOf = OntologyData.getOntologySubclassOfPairs(sourceAttributeOntology);
		for(OWLSubClassOfAxiom axiom_ : namedSubclassOf) {
			// Get Classes as Data Properties and subproperty assertions
			OWLClass subClass = axiom_.getSubClass().asOWLClass();
			String subClassName = subClass.getIRI().getShortForm();
			OWLDataProperty subProperty = factory.getOWLDataProperty(IRI.create(targetOntologyNamespace + StringUtils.uncapitalize(subClassName)));	
			
			OWLClass superClass = axiom_.getSuperClass().asOWLClass();
			String superClassName = superClass.getIRI().getShortForm();
			OWLDataProperty superProperty = factory.getOWLDataProperty(IRI.create(targetOntologyNamespace + StringUtils.uncapitalize(superClassName)));
			
			OWLSubDataPropertyOfAxiom axiom = factory.getOWLSubDataPropertyOfAxiom(subProperty, superProperty);
			manager.applyChange(new AddAxiom(targetOntology, axiom));	
			
			// Synonyms	
			addEquivalentClassesAsDataProperties(subClass, sourceAttributeOntology, targetOntology);
			addEquivalentClassesAsDataProperties(superClass, sourceAttributeOntology, targetOntology);
			
			// Annotations for subProperty
			if(!processedProperties.contains(subClassName)) {
				importAnnotationsForTransformedEntity(subClass, subProperty, sourceAttributeOntology, targetOntology);
				processedProperties.add(subClassName);
			}

			// Annotations for superProperty
			if(!processedProperties.contains(superClassName)) {
				importAnnotationsForTransformedEntity(superClass, superProperty, sourceAttributeOntology, targetOntology);		
				processedProperties.add(superClassName);
			}
		}
	}
	
	/**
	 * Adds the equivalent classes as data properties.
	 *
	 * @param sourceClass the source class
	 * @param sourceAttributeOntology the source attribute ontology
	 * @param targetOntology the target ontology
	 */
	private static void addEquivalentClassesAsDataProperties(OWLClass sourceClass, OWLOntology sourceAttributeOntology, OWLOntology targetOntology) {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory();
		Set<OWLDataProperty> eqProps = new HashSet<OWLDataProperty>();
		
		// Get Class synonyms Set
		Set<OWLEquivalentClassesAxiom> eqSubClasses = sourceAttributeOntology.getEquivalentClassesAxioms(sourceClass);
		if(!eqSubClasses.isEmpty()) {
			// Get targetOntology Namespace
			OWLXMLDocumentFormat owlFormat = new OWLXMLDocumentFormat();
			OWLOntologyXMLNamespaceManager namespaceManager = new OWLOntologyXMLNamespaceManager(targetOntology,  owlFormat);
			String targetOntologyNamespace = namespaceManager.getDefaultNamespace();

			// Perform transformations  
			Set<OWLClass> classes = eqSubClasses.iterator().next().getClassesInSignature();	
			for(OWLClass class_ : classes) {
				// Add Class synonym as Data Property
				String className = class_.getIRI().getShortForm();
				OWLDataProperty eqProperty = factory.getOWLDataProperty(IRI.create(targetOntologyNamespace + StringUtils.uncapitalize(className)));	
				eqProps.add(eqProperty);
				
				// Annotations
				importAnnotationsForTransformedEntity(class_, eqProperty, sourceAttributeOntology, targetOntology);
			}
			
			// Add synonymy
			OWLEquivalentDataPropertiesAxiom eqAxiom = factory.getOWLEquivalentDataPropertiesAxiom(eqProps);
			manager.applyChange(new AddAxiom(targetOntology, eqAxiom));	
			eqProps.clear();
		}
	}

	/**
	 * Adds the classes as object properties.
	 *
	 * @param sourceRelationsOntology the source relations ontology
	 * @param targetOntology the target ontology
	 */
	// Classes -> Object Properties	
	public static void addClassesAsObjectProperties(OWLOntology sourceRelationsOntology, OWLOntology targetOntology) {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory();
		Set<String> processedProperties = new HashSet<String>();

		// Get targetOntology Namespace
		OWLXMLDocumentFormat owlFormat = new OWLXMLDocumentFormat();
		OWLOntologyXMLNamespaceManager namespaceManager = new OWLOntologyXMLNamespaceManager(targetOntology,  owlFormat);
		String targetOntologyNamespace = namespaceManager.getDefaultNamespace();
		
		// Get all SubclassOf relations 
		Set<OWLSubClassOfAxiom> namedSubclassOf = OntologyData.getOntologySubclassOfPairs(sourceRelationsOntology);
		for(OWLSubClassOfAxiom axiom_ : namedSubclassOf) {
			// Get Classes as Object Properties and subproperty assertions
			OWLClass subClass = axiom_.getSubClass().asOWLClass();
			String subClassName = subClass.getIRI().getShortForm();
			OWLObjectProperty subProperty = factory.getOWLObjectProperty(IRI.create(targetOntologyNamespace + StringUtils.uncapitalize(subClassName)));	
			
			OWLClass superClass = axiom_.getSuperClass().asOWLClass();
			String superClassName = superClass.getIRI().getShortForm();
			OWLObjectProperty superProperty = factory.getOWLObjectProperty(IRI.create(targetOntologyNamespace + StringUtils.uncapitalize(superClassName)));
			
			OWLSubObjectPropertyOfAxiom axiom = factory.getOWLSubObjectPropertyOfAxiom(subProperty, superProperty);
			manager.applyChange(new AddAxiom(targetOntology, axiom));	
			
			// Synonyms	
			addEquivalentClassesAsObjectProperties(subClass, sourceRelationsOntology, targetOntology);
			addEquivalentClassesAsObjectProperties(superClass, sourceRelationsOntology, targetOntology);
			
			// Annotations for subProperty
			if(!processedProperties.contains(subClassName)) {
				importAnnotationsForTransformedEntity(subClass, subProperty, sourceRelationsOntology, targetOntology);
				processedProperties.add(subClassName);
			}

			// Annotations for superProperty
			if(!processedProperties.contains(superClassName)) {
				importAnnotationsForTransformedEntity(superClass, superProperty, sourceRelationsOntology, targetOntology);		
				processedProperties.add(superClassName);
			}
		}
	}
	
	/**
	 * Adds the equivalent classes as object properties.
	 *
	 * @param sourceClass the source class
	 * @param sourceAttributeOntology the source attribute ontology
	 * @param targetOntology the target ontology
	 */
	private static void addEquivalentClassesAsObjectProperties(OWLClass sourceClass, OWLOntology sourceAttributeOntology, OWLOntology targetOntology) {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory();
		Set<OWLObjectProperty> eqProps = new HashSet<OWLObjectProperty>();
		
		// Get Class synonyms Set
		Set<OWLEquivalentClassesAxiom> eqSubClasses = sourceAttributeOntology.getEquivalentClassesAxioms(sourceClass);
		if(!eqSubClasses.isEmpty()) {
			// Get targetOntology Namespace
			OWLXMLDocumentFormat owlFormat = new OWLXMLDocumentFormat();
			OWLOntologyXMLNamespaceManager namespaceManager = new OWLOntologyXMLNamespaceManager(targetOntology,  owlFormat);
			String targetOntologyNamespace = namespaceManager.getDefaultNamespace();

			// Perform transformations  
			Set<OWLClass> classes = eqSubClasses.iterator().next().getClassesInSignature();	
			for(OWLClass class_ : classes) {
				// Add Class synonym as Object Property
				String className = class_.getIRI().getShortForm();
				OWLObjectProperty eqProperty = factory.getOWLObjectProperty(IRI.create(targetOntologyNamespace + StringUtils.uncapitalize(className)));	
				eqProps.add(eqProperty);
				
				// Annotations
				importAnnotationsForTransformedEntity(class_, eqProperty, sourceAttributeOntology, targetOntology);
			}
			
			// Add synonymy
			OWLEquivalentObjectPropertiesAxiom eqAxiom = factory.getOWLEquivalentObjectPropertiesAxiom(eqProps);
			manager.applyChange(new AddAxiom(targetOntology, eqAxiom));	
			eqProps.clear();
		}
	}	
	
	/**
	 * Adds the individuals as leaf data properties.
	 *
	 * @param sourceAttributeOntology the source attribute ontology
	 * @param targetOntology the target ontology
	 */
	// Individuals -> Data Properties
	public static void addIndividualsAsLeafDataProperties(OWLOntology sourceAttributeOntology, OWLOntology targetOntology) {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory();

		// Get targetOntology Namespace
		OWLXMLDocumentFormat owlFormat = new OWLXMLDocumentFormat();
		OWLOntologyXMLNamespaceManager namespaceManager = new OWLOntologyXMLNamespaceManager(targetOntology,  owlFormat);
		String targetOntologyNamespace = namespaceManager.getDefaultNamespace();
		
		// Get the Class Assertions for the Instances
		Set<OWLClassAssertionAxiom> instancedOf = sourceAttributeOntology.getAxioms(AxiomType.CLASS_ASSERTION);;
		for(OWLClassAssertionAxiom assertion_ : instancedOf) {	
			OWLNamedIndividual instance =(OWLNamedIndividual) assertion_.getIndividual();
			OWLClass instancedClass = assertion_.getClassesInSignature().iterator().next();
			
			// Leaf Property
			String instanceName = instance.asOWLNamedIndividual().getIRI().getShortForm();
			OWLDataProperty subProperty = factory.getOWLDataProperty(IRI.create(targetOntologyNamespace + StringUtils.uncapitalize(instanceName)));
			
			// Superproperty
			String className = instancedClass.getIRI().getShortForm();
			OWLDataProperty superProperty = factory.getOWLDataProperty(IRI.create(targetOntologyNamespace + StringUtils.uncapitalize(className)));	
			
			OWLSubDataPropertyOfAxiom axiom = factory.getOWLSubDataPropertyOfAxiom(subProperty, superProperty);
			manager.applyChange(new AddAxiom(targetOntology, axiom));
			
			// Label
			OWLAnnotation label = EntitySearcher.getAnnotations(instance, sourceAttributeOntology, factory.getRDFSLabel()).iterator().next();
			OWLAxiom labelAxiom = factory.getOWLAnnotationAssertionAxiom(subProperty.getIRI() , label);
			manager.applyChange(new AddAxiom(targetOntology, labelAxiom));	
		}
	}
	
	/**
	 * Adds the individuals as leaf data property ranges.
	 *
	 * @param attributeOntology the attribute ontology
	 * @param valueOntology the value ontology
	 * @param targetOntology the target ontology
	 */
	// Individuals -> Data Property Values
	public static void addIndividualsAsLeafDataPropertyRanges(OWLOntology attributeOntology, OWLOntology valueOntology, OWLOntology targetOntology) {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory();

		// Get the value ranges as Datatypes and include them in the target ontology
		Set<OWLDatatype> datatypes = getInstanceSetsAsDataTypes(valueOntology, targetOntology);
		
		// Get the leaf-DataProperties to be matched 
		Set<OWLDataProperty> leafDataProps = OntologyData.getLeafDataProperties(targetOntology);
		
		// BOOLEAN VALUE
		Set<OWLDataPropertyRangeAxiom> specialValues = OntologyMerger.getSpecialValueRanges(attributeOntology, valueOntology, targetOntology, HAS_BOOLEAN_VALUE_SUBCLASS);
		
		// CARDINAL
		specialValues.addAll(getSpecialValueRanges(attributeOntology, valueOntology, targetOntology, HAS_CARDINAL_SUBCLASS));
		
		// Prune matched leaf-DataProperties
		Set<OWLDataProperty> matchedDataProps = new HashSet<OWLDataProperty>();
		for(OWLDataPropertyRangeAxiom specialRange : specialValues)
			matchedDataProps.add(specialRange.getDataPropertiesInSignature().iterator().next());
		leafDataProps.removeAll(matchedDataProps);
		
		// Matching
		Set<Pair<OWLDataProperty, OWLDatatype>> matches = new HashSet <Pair<OWLDataProperty, OWLDatatype>>();
		for(OWLDataProperty leafDataProp_ : leafDataProps) {
			List <Pair<OWLDatatype, Double>> measures = new ArrayList <Pair<OWLDatatype, Double>>();
			String leafDPNameSanitized = OntologyData.getSanitizedLeafDataPropertyName(leafDataProp_);

			// Assess the distance for the current leafDP with the DataTypes
			for(OWLDatatype dt_ : datatypes) {	
				String dtNameSanitized = OntologyData.getSanitizedDatatypeName(dt_);
				Double measure = StringSimilarityDistance.getSecuentialJaroWrinklerDistance(leafDPNameSanitized, dtNameSanitized);
				measures.add(new Pair<OWLDatatype, Double>(dt_, measure));
			}			
			if(measures.size() > 0) {
				// Sort firstMeasures
				Collections.sort(measures, Comparator.comparing(p -> -p.second()));
				
				// Take first
				Pair<OWLDatatype, Double> best = measures.listIterator().next();							
				if(best.second() >= SIMILARITY_THRESHOLD) {
					matches.add(new Pair<OWLDataProperty, OWLDatatype>(leafDataProp_, best.first()));
					datatypes.remove(best.first());
				}
			}
		}
		
		// Rest, lower similarity, prune search space
		for(Pair<OWLDataProperty, OWLDatatype> match : matches) 
			leafDataProps.remove(match.first());
			
		// Check if there is any correspondence with a single from the remaining Datatypes
		for(OWLDataProperty leafDataProp_ : leafDataProps) {
			String leafDPNameSanitized = OntologyData.getSanitizedLeafDataPropertyName(leafDataProp_);
			Set<OWLDatatype> candidates = new HashSet<OWLDatatype>();
			for(OWLDatatype dt_ : datatypes) {
				String dtNameSanitized = OntologyData.getSanitizedDatatypeName(dt_);
				if(StringUtils.containsIgnoreCase(leafDPNameSanitized, dtNameSanitized) || 
					StringUtils.containsIgnoreCase(dtNameSanitized, leafDPNameSanitized) ) 
					candidates.add(dt_);
			}	
			if(candidates.size() == 1)
				matches.add(new Pair<OWLDataProperty, OWLDatatype>(leafDataProp_, candidates.iterator().next()));
		}	
		// Add the range for the current leaf-Dataproperty
		for(Pair<OWLDataProperty, OWLDatatype> match_ : matches) {
			OWLDataPropertyRangeAxiom rangeAxiom = factory.getOWLDataPropertyRangeAxiom(match_.first(), match_.second());
			manager.applyChange(new AddAxiom(targetOntology, rangeAxiom));
		}
	}
	
	// Returns a set with the Datatypes included in the target ontology
	/**
	 * Gets the instance sets as data types.
	 *
	 * @param sourceOntology the source ontology
	 * @param targetOntology the target ontology
	 * @return the instance sets as data types
	 */
	// Individuals -> Datatypes
	public static Set<OWLDatatype> getInstanceSetsAsDataTypes(OWLOntology sourceOntology, OWLOntology targetOntology) {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory();		
		Set<OWLClassAssertionAxiom> assertions = sourceOntology.getAxioms(AxiomType.CLASS_ASSERTION);;
		Set<OWLClass> classes = new HashSet<OWLClass>();
		Set<OWLDatatype> includedDataTypes = new HashSet<OWLDatatype>();
		
		// Get the set with the asserted classes
		for(OWLClassAssertionAxiom assertion_ : assertions) 
			classes.add(assertion_.getClassesInSignature().iterator().next());
		
		// Get the instances for each class
		for(OWLClass class_ : classes) {	
			Set<OWLNamedIndividual> instances = OntologyData.getInstanceSetForClassFromAssertions(assertions, class_);
			Set<OWLLiteral> valuesSet = new HashSet<OWLLiteral>();
			
			// Get the Datarange with the instance set literals
			for(OWLNamedIndividual instance_ : instances) {
				Collection<OWLAnnotation> label = EntitySearcher.getAnnotations(instance_, sourceOntology, factory.getRDFSLabel()); 
				String value = label.iterator().next().getValue().toString().replaceAll("@en|\"", "");	
				valuesSet.add(factory.getOWLLiteral(value));
			}		
			OWLDataOneOf valuesRange = factory.getOWLDataOneOf(valuesSet);
			OWLDatatypeDefinitionAxiom dataTypeDef = getDataRangeAsDataType(valuesRange, class_.getIRI());
			manager.applyChange(new AddAxiom(targetOntology, dataTypeDef));
			includedDataTypes.add(dataTypeDef.getDatatype());
		}
		return includedDataTypes;
	}
	
	/**
	 * Gets the data range as data type.
	 *
	 * @param dataRange the data range
	 * @param classIRI the class IRI
	 * @return the data range as data type
	 */
	private static OWLDatatypeDefinitionAxiom getDataRangeAsDataType(OWLDataRange dataRange, IRI classIRI) {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory();		
		OWLDatatype datatype = factory.getOWLDatatype(IRI.create(classIRI.toString() + "Datatype"));
		OWLDatatypeDefinitionAxiom datatypeDef = factory.getOWLDatatypeDefinitionAxiom(datatype, dataRange);		
		return datatypeDef;
	}	
	
	/**
	 * Gets the special value ranges.
	 *
	 * @param attributeOntology the attribute ontology
	 * @param valueOntology the value ontology
	 * @param targetOntology the target ontology
	 * @param condition the condition
	 * @return the special value ranges
	 */
	// Boolean Value | Cardinal
	private static Set<OWLDataPropertyRangeAxiom> getSpecialValueRanges(OWLOntology attributeOntology, OWLOntology valueOntology, OWLOntology targetOntology, Predicate<OWLSubClassOfAxiom> condition) {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory();
		Set<Pair<OWLClass, Object>> matches = new HashSet <Pair<OWLClass, Object>>();
		Set<OWLDataPropertyRangeAxiom> rangeAxioms = new HashSet <OWLDataPropertyRangeAxiom>();
		
		// Get targetOntology Namespace 
		OWLXMLDocumentFormat owlFormat = new OWLXMLDocumentFormat();
		OWLOntologyXMLNamespaceManager namespaceManager = new OWLOntologyXMLNamespaceManager(targetOntology,  owlFormat);
		String targetOntologyNamespace = namespaceManager.getDefaultNamespace();
		
		// select BOOLEAN VALUE | CARDINAL SubclassOf Axioms from LVO
		Set<OWLSubClassOfAxiom> booleanSubclassOf = OntologyData.getOntologySubclassOfPairs(valueOntology);
		booleanSubclassOf.removeIf(condition);	
		Set<OWLClass> booleanSuperclasses = new HashSet<OWLClass>();
		for(OWLSubClassOfAxiom axiom_ : booleanSubclassOf)
			booleanSuperclasses.add(axiom_.getSuperClass().asOWLClass());

		// select Classes with Individuals from LAO
		Set<OWLClassAssertionAxiom> assertions = attributeOntology.getAxioms(AxiomType.CLASS_ASSERTION);;	
		Set<OWLClass> assertedClasses = new HashSet<OWLClass>();
		for(OWLClassAssertionAxiom assertion_ : assertions)
			assertedClasses.add(assertion_.getClassesInSignature().iterator().next());
		
		// First Matching LVO:LAO Concepts, several attributes for each LAO class
		for(OWLClass booleanSuperClass_ : booleanSuperclasses) {
			List <Pair<OWLClass, Double>> firstMeasures = new ArrayList <Pair<OWLClass, Double>>();
			String booleanSCName = booleanSuperClass_.getIRI().getShortForm();
			String booleanSCNameSanitized = StringUtils.removeEnd(booleanSCName, "Value");			

			// Assess the distance for the current Boolean-Value Super Class with the LAO classes(no need for the attributes to be string-similar)
			for(OWLClass assertedClass_ : assertedClasses) {	
				String assertedCName = assertedClass_.getIRI().getShortForm();
				String assertedCNameSanitized = StringUtils.removeEnd(assertedCName, "Attribute");		
				double measure = StringSimilarityDistance.getSecuentialJaroWrinklerDistance(booleanSCNameSanitized, assertedCNameSanitized);
				if(measure >= SIMILARITY_THRESHOLD)				
					firstMeasures.add(new Pair<OWLClass, Double>(assertedClass_, measure));
			}	
			if(firstMeasures.size() > 0) {
				// Sort firstMeasures
				Collections.sort(firstMeasures, Comparator.comparing(p -> -p.second()));
				
				// Take first
				Pair<OWLClass, Double> best = firstMeasures.listIterator().next();
				matches.add(new Pair<OWLClass, Object>(booleanSuperClass_, best.first()));
			}
		}
		
		// Get the leafDPs that are subproperties of the current property
		Set<OWLClass> matchedClasses = new HashSet<OWLClass>();
		for(Pair<OWLClass, Object> match_ : matches) {
			String attributeMatch = StringUtils.uncapitalize(((OWLClass)match_.second()).getIRI().getShortForm()); 
			matchedClasses.add(match_.first());
			
			// find the matched DataProperty to get its subproperties
			Set<OWLDataProperty> targetOntologyDPs = targetOntology.getDataPropertiesInSignature();
			for(OWLDataProperty dp_ : targetOntologyDPs) {				
				if(dp_.getIRI().getShortForm().equals(attributeMatch)) {
					Set<OWLSubDataPropertyOfAxiom> subDPAxioms = targetOntology.getDataSubPropertyAxiomsForSuperProperty(dp_);
					
					// Add the range for the current leaf Dataproperty
					for(OWLSubDataPropertyOfAxiom subDPAxiom_ : subDPAxioms) {
						OWLDataProperty leafDP = subDPAxiom_.getSubProperty().asOWLDataProperty();
						OWLDatatype dataRange = getDatatypeBasedOnCondition(condition);
						OWLDataPropertyRangeAxiom rangeAxiom = factory.getOWLDataPropertyRangeAxiom(leafDP, dataRange);
						manager.applyChange(new AddAxiom(targetOntology, rangeAxiom));
						rangeAxioms.add(rangeAxiom);
					}
				}
			}
		}

		// Second Matching LVO:target ontology leafDP, one attribute for each LVO class, string similarity between class and dataproperty
		matches.clear();
		booleanSuperclasses.removeAll(matchedClasses);
		for(OWLClass booleanSuperClass_ : booleanSuperclasses) {
			List <Pair<OWLDataProperty, Double>> secondMeasures = new ArrayList <Pair<OWLDataProperty, Double>>();
			String booleanSCName = booleanSuperClass_.getIRI().getShortForm();
			String booleanSCNameSanitized = StringUtils.removeEnd(booleanSCName, "Value");			
			
			// Assess the distance for the current Boolean-Value Super Class with target-ontology's leaf-dataproperties(string-similar)
			Set<OWLDataProperty> leafDataProps = OntologyData.getLeafDataProperties(targetOntology);
			for(OWLDataProperty leafDataProp_ : leafDataProps) {
				// Restricted to transformed leafDPs
				if (targetOntologyNamespace.equals(leafDataProp_.getIRI().getNamespace())) {
					String leafDPNameSanitized = OntologyData.getSanitizedLeafDataPropertyName(leafDataProp_);
					double measure = StringSimilarityDistance.getSecuentialJaroWrinklerDistance(booleanSCNameSanitized, leafDPNameSanitized);			
					secondMeasures.add(new Pair<OWLDataProperty, Double>(leafDataProp_, measure));
				}
			}		
			if(secondMeasures.size() > 0) {
				// Sort secondMeasures
				Collections.sort(secondMeasures, Comparator.comparing(p -> -p.second()));

				// Take first
				Pair<OWLDataProperty, Double> best = secondMeasures.listIterator().next();
				matches.add(new Pair<OWLClass, Object>(booleanSuperClass_, best.first()));
			}
		}			
		// Add the range for the leaf-dataproperties matched
		for(Pair<OWLClass, Object> match_ : matches) {
			OWLDataProperty leafDP =(OWLDataProperty)match_.second();
			OWLDatatype dataRange = getDatatypeBasedOnCondition(condition);
			OWLDataPropertyRangeAxiom rangeAxiom = factory.getOWLDataPropertyRangeAxiom(leafDP, dataRange);
			manager.applyChange(new AddAxiom(targetOntology, rangeAxiom));
			rangeAxioms.add(rangeAxiom);
		}
		return rangeAxioms;
	}

	/** The has boolean value subclass. */
	private static Predicate<OWLSubClassOfAxiom> HAS_BOOLEAN_VALUE_SUBCLASS = new Predicate<OWLSubClassOfAxiom>() {
		//@Override
		  public boolean test(OWLSubClassOfAxiom axiom) {
			  if(axiom.getSubClass().asOWLClass().asOWLClass().getIRI().getShortForm().equals("BOOLEAN_VALUE"))
					return false;
			  return true;		 
		  }
	};
	
	/** The has cardinal subclass. */
	private static Predicate<OWLSubClassOfAxiom> HAS_CARDINAL_SUBCLASS = new Predicate<OWLSubClassOfAxiom>() {
		//@Override
		  public boolean test(OWLSubClassOfAxiom axiom) {
			  if(axiom.getSubClass().asOWLClass().asOWLClass().getIRI().getShortForm().equals("CARDINAL"))
					return false;
			  return true;		 
		  }
	};
	
	/**
	 * Gets the datatype based on condition.
	 *
	 * @param condition the condition
	 * @return the datatype based on condition
	 */
	private static OWLDatatype getDatatypeBasedOnCondition(Predicate<OWLSubClassOfAxiom> condition) {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory();
		if(condition.equals(HAS_BOOLEAN_VALUE_SUBCLASS)) 
			return factory.getBooleanOWLDatatype();
		else if(condition.equals(HAS_CARDINAL_SUBCLASS)) 
			return factory.getIntegerOWLDatatype();
		else
			return null;
	}
	
	/**
	 * Adds the individuals as leaf object properties.
	 *
	 * @param sourceRelationsOntology the source relations ontology
	 * @param targetOntology the target ontology
	 */
	// Individuals -> Object Properties
	public static void addIndividualsAsLeafObjectProperties(OWLOntology sourceRelationsOntology, OWLOntology targetOntology) {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory();

		// Get targetOntology Namespace
		OWLXMLDocumentFormat owlFormat = new OWLXMLDocumentFormat();
		OWLOntologyXMLNamespaceManager namespaceManager = new OWLOntologyXMLNamespaceManager(targetOntology,  owlFormat);
		String targetOntologyNamespace = namespaceManager.getDefaultNamespace();
		
		// Get the Class Assertions for the Instances
		Set<OWLClassAssertionAxiom> instancedOf = sourceRelationsOntology.getAxioms(AxiomType.CLASS_ASSERTION);;
		for(OWLClassAssertionAxiom assertion_ : instancedOf) {	
			OWLNamedIndividual instance =(OWLNamedIndividual) assertion_.getIndividual();
			OWLClass instancedClass = assertion_.getClassesInSignature().iterator().next();
				
			// Leaf Property
			String instanceName = instance.asOWLNamedIndividual().getIRI().getShortForm();
			OWLObjectProperty subProperty = factory.getOWLObjectProperty(IRI.create(targetOntologyNamespace + StringUtils.uncapitalize(instanceName)));
			
			// Superproperty
			String className = instancedClass.getIRI().getShortForm();
			OWLObjectProperty superProperty = factory.getOWLObjectProperty(IRI.create(targetOntologyNamespace + StringUtils.uncapitalize(className)));			
			OWLSubObjectPropertyOfAxiom axiom = factory.getOWLSubObjectPropertyOfAxiom(subProperty, superProperty);
			manager.applyChange(new AddAxiom(targetOntology, axiom));
			
			// Label
			OWLAnnotation label = EntitySearcher.getAnnotations(instance, sourceRelationsOntology, factory.getRDFSLabel()).iterator().next();
			OWLAxiom labelAxiom = factory.getOWLAnnotationAssertionAxiom(subProperty.getIRI() , label);
			manager.applyChange(new AddAxiom(targetOntology, labelAxiom));	
		}
	}

	// Disjoint Classes -> Disjoint Object|Data Properties
	/**
	 * Adds the disjoint data properties from classes.
	 *
	 * @param sourceOntology the source ontology
	 * @param targetOntology the target ontology
	 */
	// Data Properties
	public static void addDisjointDataPropertiesFromClasses(OWLOntology sourceOntology, OWLOntology targetOntology) {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory();
		
		// Disjointness Axioms
		Set<OWLDisjointClassesAxiom> disjoinClassestAxioms = sourceOntology.getAxioms(AxiomType.DISJOINT_CLASSES);
		for(OWLDisjointClassesAxiom axiom_ : disjoinClassestAxioms) {
			Set<OWLClass> classes = axiom_  .getClassesInSignature();
			Set<OWLDataProperty> targetDataProperties = selectTargetTransformedDataProperties(classes, targetOntology);
			if(targetDataProperties.size() > 0) {
				OWLDisjointDataPropertiesAxiom disjointAxiom = factory.getOWLDisjointDataPropertiesAxiom(targetDataProperties);
				manager.applyChange(new AddAxiom(targetOntology, disjointAxiom));
			}
		}
	}
	
	/**
	 * Select target transformed data properties.
	 *
	 * @param classes the classes
	 * @param targetOntology the target ontology
	 * @return the sets the
	 */
	private static Set<OWLDataProperty> selectTargetTransformedDataProperties(Set<OWLClass> classes, OWLOntology targetOntology) {
		Set<OWLDataProperty> targetDataProperties = new HashSet<OWLDataProperty>();
		
		// Data Properties from Target Ontology
		Set<OWLDataProperty> dataPropertyertiesPool = targetOntology.getDataPropertiesInSignature();	
		for(OWLClass class_ : classes) {
			String className = class_.getIRI().getShortForm();	
			for(OWLDataProperty DataProperty_ : dataPropertyertiesPool) {
				String dataPropertyertyName = DataProperty_.getIRI().getShortForm();
				if(dataPropertyertyName.equals(className)) {
					targetDataProperties.add(DataProperty_);
					break;
				}
			}
		}
		return targetDataProperties;
	}
	
	/**
	 * Adds the disjoint object properties from classes.
	 *
	 * @param sourceOntology the source ontology
	 * @param targetOntology the target ontology
	 */
	// Object Properties
	public static void addDisjointObjectPropertiesFromClasses(OWLOntology sourceOntology, OWLOntology targetOntology) {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory();
		
		// Disjointness Axioms
		Set<OWLDisjointClassesAxiom> disjoinClassestAxioms = sourceOntology.getAxioms(AxiomType.DISJOINT_CLASSES);
		for(OWLDisjointClassesAxiom axiom_ : disjoinClassestAxioms) {
			Set<OWLClass> classes = axiom_  .getClassesInSignature();
			Set<OWLObjectProperty> targetObjectProperties = selectTargetTransformedObjectProperties(classes, targetOntology);
			if(targetObjectProperties.size() > 0) {
				OWLDisjointObjectPropertiesAxiom disjointAxiom = factory.getOWLDisjointObjectPropertiesAxiom(targetObjectProperties);
				manager.applyChange(new AddAxiom(targetOntology, disjointAxiom));
			}
		}
	}
	
	/**
	 * Select target transformed object properties.
	 *
	 * @param classes the classes
	 * @param targetOntology the target ontology
	 * @return the sets the
	 */
	private static Set<OWLObjectProperty> selectTargetTransformedObjectProperties(Set<OWLClass> classes, OWLOntology targetOntology) {
		Set<OWLObjectProperty> targetObjectProperties = new HashSet<OWLObjectProperty>();
		
		// Object Properties from Target Ontology
		Set<OWLObjectProperty> objectPropertiesPool = targetOntology.getObjectPropertiesInSignature();	
		for(OWLClass class_ : classes) {
			String className = class_.getIRI().getShortForm();		
			for(OWLObjectProperty objectProperty_ : objectPropertiesPool) {
				String objectPropertyName = objectProperty_.getIRI().getShortForm();
				if(objectPropertyName.equals(className)) {
					targetObjectProperties.add(objectProperty_);
					break;
				}
			}
		}
		return targetObjectProperties;
	}

	/**
	 * Adds the transformed data properties domains.
	 *
	 * @param excludedOntologies the excluded ontologies
	 * @param targetOntology the target ontology
	 */
	// Transformed Data Properties Domains
	public static void addTransformedDataPropertiesDomains(Set<OWLOntology> excludedOntologies, OWLOntology targetOntology) {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory();

		// Get targetOntology Namespace
		OWLXMLDocumentFormat owlFormat = new OWLXMLDocumentFormat();
		OWLOntologyXMLNamespaceManager namespaceManager = new OWLOntologyXMLNamespaceManager(targetOntology,  owlFormat);
		String targetOntologyNamespace = namespaceManager.getDefaultNamespace();
		
		// Get excludedOntologies Namespaces
		Set<String> excludedOntologiesNamespaces = new HashSet<String>();
		for(OWLOntology ont_ :excludedOntologies) {
			namespaceManager = new OWLOntologyXMLNamespaceManager(ont_,  owlFormat);
			excludedOntologiesNamespaces.add(namespaceManager.getDefaultNamespace());
		}		
		// Get transformed Data Properties
		Set<OWLDataProperty> transDataProperties = targetOntology.getDataPropertiesInSignature();	
		transDataProperties.removeAll(OntologyData.getLeafDataProperties(targetOntology));
		Iterator<OWLDataProperty> iterator = transDataProperties.iterator();
		while (iterator.hasNext()) {
			OWLDataProperty dp_ = iterator.next();		
			Pattern pattern = Pattern.compile("[Ll]inguistic(?:Attribute|Level|Relationship|Unit|Value)ConceptualDataProperty");
			Matcher conceptualPropertiesMatcher = pattern.matcher(dp_.getIRI().getShortForm());
			
			// Remove Conceptual Data Properties and imported Data Properties
			if (!dp_.getIRI().getNamespace().equals(targetOntologyNamespace) || (conceptualPropertiesMatcher.matches()))
				iterator.remove();
		}
		for (OWLDataProperty dp_ : transDataProperties)
			if (!dp_.getIRI().getNamespace().equals(targetOntologyNamespace))
				transDataProperties.remove(dp_);
		
		// Get Domain
		for (OWLDataProperty dp_ : transDataProperties) {
			OWLClass domain = OntologyData.findMostSimilarClass(dp_.getIRI().getShortForm(), excludedOntologiesNamespaces, targetOntology);
			if (domain != null) {
				OWLDataPropertyDomainAxiom domainAxiom = factory.getOWLDataPropertyDomainAxiom(dp_, domain);
				manager.applyChange(new AddAxiom(targetOntology, domainAxiom));	
			}
		}
	}
	
	/**
	 * Adds the transformed object properties domains.
	 *
	 * @param excludedOntologies the excluded ontologies
	 * @param targetOntology the target ontology
	 */
	// Transformed Object Properties Domains
	public static void addTransformedObjectPropertiesDomains(Set<OWLOntology> excludedOntologies, OWLOntology targetOntology) {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory();

		// Get targetOntology Namespace
		OWLXMLDocumentFormat owlFormat = new OWLXMLDocumentFormat();
		OWLOntologyXMLNamespaceManager namespaceManager = new OWLOntologyXMLNamespaceManager(targetOntology,  owlFormat);
		String targetOntologyNamespace = namespaceManager.getDefaultNamespace();
		
		// Get excludedOntologies Namespaces
		Set<String> excludedOntologiesNamespaces = new HashSet<String>();
		for(OWLOntology ont_ :excludedOntologies) {
			namespaceManager = new OWLOntologyXMLNamespaceManager(ont_,  owlFormat);
			excludedOntologiesNamespaces.add(namespaceManager.getDefaultNamespace());
		}		
		// Get transformed Object Properties
		Set<OWLObjectProperty> transObjectProperties = targetOntology.getObjectPropertiesInSignature();	
		transObjectProperties.removeAll(OntologyData.getLeafObjectProperties(targetOntology));
		Iterator<OWLObjectProperty> iterator = transObjectProperties.iterator();
		while (iterator.hasNext()) {
			OWLObjectProperty op_ = iterator.next();		
			Pattern pattern = Pattern.compile("[Ll]inguistic(?:Attribute|Level|Relationship|Unit|Value)ConceptualObjectProperty");
			Matcher conceptualPropertiesMatcher = pattern.matcher(op_.getIRI().getShortForm());
			
			// Remove Conceptual Object Properties and imported Object Properties
			if (!op_.getIRI().getNamespace().equals(targetOntologyNamespace) || (conceptualPropertiesMatcher.matches()))
				iterator.remove();
		}
		for (OWLObjectProperty dp_ : transObjectProperties)
			if (!dp_.getIRI().getNamespace().equals(targetOntologyNamespace))
				transObjectProperties.remove(dp_);
		
		// Get Domain
		for (OWLObjectProperty dp_ : transObjectProperties) {
			OWLClass domain = OntologyData.findMostSimilarClass(dp_.getIRI().getShortForm(), excludedOntologiesNamespaces, targetOntology);
			if (domain != null) {
				OWLObjectPropertyDomainAxiom domainAxiom = factory.getOWLObjectPropertyDomainAxiom(dp_, domain);
				manager.applyChange(new AddAxiom(targetOntology, domainAxiom));	
			}
		}
	}
	
	
	/**
	 * Load ontology.
	 *
	 * @param sourceOntology the source ontology
	 * @param targetOntology the target ontology
	 */
	// LOAD
	public static void loadOntology(OWLOntology sourceOntology, OWLOntology targetOntology){
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		
		Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
		Set<OWLImportsDeclaration> imports = new HashSet<OWLImportsDeclaration>();
    
		//source ontology's axiom and imports
        axioms.addAll(sourceOntology.getAxioms());
        imports.addAll(sourceOntology.getImportsDeclarations());
 
		//target ontology's axiom and imports
	    manager.addAxioms(targetOntology, axioms);

		//Adding the import declarations
		for(OWLImportsDeclaration decl_ : imports){
			manager.applyChange(new AddImport(targetOntology, decl_));
		}
	}
	
	/**
	 * Load ontology with conceptual properties.
	 *
	 * @param sourceOntology the source ontology
	 * @param targetOntology the target ontology
	 */
	public static void loadOntologyWithConceptualProperties(OWLOntology sourceOntology, OWLOntology targetOntology){
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		
		Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
		Set<OWLImportsDeclaration> imports = new HashSet<OWLImportsDeclaration>();
		
		// import source object and data properties 
		importConceptualDataProperties(sourceOntology, targetOntology);
		importConceptualObjectProperties(sourceOntology, targetOntology);
    
		// source ontology's other axiom and imports
		Set<OWLAxiom> allAxioms = sourceOntology.getAxioms();
		for (OWLAxiom axiom_ : allAxioms) {
			if (axiom_.isOfType(AxiomType.DECLARATION)) {
				if(!axiom_.getDataPropertiesInSignature().isEmpty() || !axiom_.getObjectPropertiesInSignature().isEmpty())
					allAxioms.remove(axiom_);
			}
		}
        axioms.addAll(allAxioms);
        imports.addAll(sourceOntology.getImportsDeclarations());
 
		//target ontology's axiom and imports
	    manager.addAxioms(targetOntology, axioms);

		//Adding the import declarations
		for(OWLImportsDeclaration decl_ : imports){
			manager.applyChange(new AddImport(targetOntology, decl_));
		}
	}	
	
	/**
	 * Rename IR is.
	 *
	 * @param newIRI the new IRI
	 * @param ontologies the ontologies
	 */
	//////////////////////////Quitar si finalmente no se usa
	public void renameIRIs (IRI newIRI, Set<OWLOntology> ontologies){
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLEntityRenamer renamer = new OWLEntityRenamer(manager, ontologies);

		for(OWLOntology ontology : ontologies){
			for (OWLEntity class_ : ontology.getClassesInSignature()){
				//replace the individual's old IRI with the new one E.g: http://ontologyOld#name becomes newIRI#name
				IRI className = IRI.create(class_.getIRI().toString().replaceFirst("[^*]+(?=#|;)", newIRI.toString()));
				manager.applyChanges(renamer.changeIRI(class_.getIRI(), className));
			}
			for (OWLEntity dataProp : ontology.getDataPropertiesInSignature()){
				//replace the individual's old IRI with the new one E.g: http://ontologyOld#name becomes newIRI#name
				IRI dataPropName = IRI.create(dataProp.getIRI().toString().replaceFirst("[^*]+(?=#|;)", newIRI.toString()));
				manager.applyChanges(renamer.changeIRI(dataProp.getIRI(), dataPropName));
			}
			for (OWLEntity dataProp : ontology.getDataPropertiesInSignature()){
				//replace the individual's old IRI with the new one E.g: http://ontologyOld#name becomes newIRI#name
				IRI dataPropName = IRI.create(dataProp.getIRI().toString().replaceFirst("[^*]+(?=#|;)", newIRI.toString()));
				manager.applyChanges(renamer.changeIRI(dataProp.getIRI(), dataPropName));
			}
			for (OWLEntity individual : ontology.getIndividualsInSignature()){
				//replace the individual's old IRI with the new one E.g: http://ontologyOld#name becomes newIRI#name
				IRI individualName = IRI.create(individual.getIRI().toString().replaceFirst("[^*]+(?=#|;)", newIRI.toString()));
				manager.applyChanges(renamer.changeIRI(individual.getIRI(), individualName));
			}
		} 
	}

	
	// EXTRACT
	/**
	 * Import class hierarchy.
	 *
	 * @param sourceOntology the source ontology
	 * @param targetOntology the target ontology
	 */
	// Class Hierarchy
	public static void importClassHierarchy(OWLOntology sourceOntology, OWLOntology targetOntology) {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
		// import Axioms
		axioms.addAll(sourceOntology.getAxioms(AxiomType.SUBCLASS_OF));
		axioms.addAll(sourceOntology.getAxioms(AxiomType.EQUIVALENT_CLASSES));
		axioms.addAll(sourceOntology.getAxioms(AxiomType.DISJOINT_CLASSES));
		axioms.addAll(sourceOntology.getAxioms(AxiomType.DISJOINT_UNION));
		axioms.addAll(OntologyData.getAssertedConceptAnnotations(sourceOntology));//Annotations
		if(axioms.size() > 0)
			manager.addAxioms(targetOntology, axioms);	
	}		
	
	/**
	 * Import conceptual data properties.
	 *
	 * @param sourceOntology the source ontology
	 * @param targetOntology the target ontology
	 */
	// Conceptual Data Properties
	public static void importConceptualDataProperties(OWLOntology sourceOntology, OWLOntology targetOntology) {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory();
		Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
		
		// Get the root Conceptual Data Property depending on sourceOntology
		OWLDataProperty conceptualDataProp = getConceptualRootDataProperty(sourceOntology, targetOntology);

		// import the original Data Properties as subproperties
		Set<OWLDataProperty> dataPropertys = sourceOntology.getDataPropertiesInSignature();
		for(OWLDataProperty dp_ : dataPropertys) {
			OWLSubDataPropertyOfAxiom subDPAxiom = factory.getOWLSubDataPropertyOfAxiom(dp_, conceptualDataProp);
			manager.addAxiom(targetOntology, subDPAxiom);
		}
		// import Axioms
		axioms.addAll(OntologyData.getAssertedAttributeAnnotations(sourceOntology));//Annotations
		axioms.addAll(sourceOntology.getAxioms(AxiomType.DATA_PROPERTY_DOMAIN));
		axioms.addAll(sourceOntology.getAxioms(AxiomType.DATA_PROPERTY_RANGE));
		axioms.addAll(sourceOntology.getAxioms(AxiomType.SUB_DATA_PROPERTY));
		axioms.addAll(sourceOntology.getAxioms(AxiomType.EQUIVALENT_DATA_PROPERTIES));
		axioms.addAll(sourceOntology.getAxioms(AxiomType.FUNCTIONAL_DATA_PROPERTY));
		axioms.addAll(sourceOntology.getAxioms(AxiomType.DISJOINT_DATA_PROPERTIES));
		if(axioms.size() > 0)
			manager.addAxioms(targetOntology, axioms);			
	}
	
	/**
	 * Gets the conceptual root data property.
	 *
	 * @param sourceOntology the source ontology
	 * @param targetOntology the target ontology
	 * @return the conceptual root data property
	 */
	private static OWLDataProperty getConceptualRootDataProperty(OWLOntology sourceOntology, OWLOntology targetOntology) {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory();
		
		// Conceptual Super Data Property
		String conceptualDataPropLbl = null;
		String ontologyName = OntologyData.getOntologyName(sourceOntology);
		if(ontologyName.contains("LAO") || ontologyName.contains("L.A.O") ||
		  (ontologyName.contains("Linguistic") && ontologyName.contains("Attribute"))) {
			conceptualDataPropLbl = "Linguistic Attribute Conceptual Data Property";
		}
		else if(ontologyName.contains("LLO") || ontologyName.contains("L.L.O") ||
				(ontologyName.contains("Linguistic") && ontologyName.contains("Level"))) {
			conceptualDataPropLbl = "Linguistic Level Conceptual Data Property";
		}
		else if(ontologyName.contains("LRO") || ontologyName.contains("L.R.O") ||
				(ontologyName.contains("Linguistic") && ontologyName.contains("Relation"))) {
			conceptualDataPropLbl = "Linguistic Relationship Conceptual Data Property";
		}
		else if(ontologyName.contains("LUO") || ontologyName.contains("L.U.O") ||
				(ontologyName.contains("Linguistic") && ontologyName.contains("Unit"))) {
			conceptualDataPropLbl = "Linguistic Unit Conceptual Data Property";
		}
		else if(ontologyName.contains("LVO") || ontologyName.contains("L.V.O") ||
				(ontologyName.contains("Linguistic") && ontologyName.contains("Value"))) {
			conceptualDataPropLbl = "Linguistic Value Conceptual Data Property";
		}		
		
		// Get targetOntology Namespace
		OWLXMLDocumentFormat owlFormat = new OWLXMLDocumentFormat();
		OWLOntologyXMLNamespaceManager namespaceManager = new OWLOntologyXMLNamespaceManager(targetOntology,  owlFormat);
		String targetOntologyNamespace = namespaceManager.getDefaultNamespace();
		
		//get the Data Property
		IRI conceptualDataPropIRI = IRI.create(targetOntologyNamespace + StringUtils.uncapitalize(conceptualDataPropLbl.replaceAll(" ", "")));
		OWLDataProperty conceptualDataProp = factory.getOWLDataProperty(conceptualDataPropIRI);

		// Label
		OWLAnnotation labelName = factory.getOWLAnnotation(factory.getRDFSLabel(), factory.getOWLLiteral(conceptualDataPropLbl, "en"));
		OWLAxiom labelAxiom = factory.getOWLAnnotationAssertionAxiom(conceptualDataProp.getIRI() , labelName);
		manager.applyChange(new AddAxiom(targetOntology, labelAxiom));			
		return conceptualDataProp;
	}

	/**
	 * Import conceptual object properties.
	 *
	 * @param sourceOntology the source ontology
	 * @param targetOntology the target ontology
	 */
	// Conceptual Object Properties
	public static void importConceptualObjectProperties(OWLOntology sourceOntology, OWLOntology targetOntology) {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory();
		Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
		
		// Get the root Conceptual Object Property depending on sourceOntology
		OWLObjectProperty conceptualObjectProp = getConceptualRootObjectProperty(sourceOntology, targetOntology);

		// import the original Data Properties as subproperties
		Set<OWLObjectProperty> objectProps = sourceOntology.getObjectPropertiesInSignature();
		for(OWLObjectProperty dp_ : objectProps) {
			OWLSubObjectPropertyOfAxiom subDPAxiom = factory.getOWLSubObjectPropertyOfAxiom(dp_, conceptualObjectProp);
			manager.addAxiom(targetOntology, subDPAxiom);
		}
		// import Axioms
		axioms.addAll(OntologyData.getAssertedAdHocRelationAnnotations(sourceOntology));//Annotations
		axioms.addAll(sourceOntology.getAxioms(AxiomType.OBJECT_PROPERTY_DOMAIN));
		axioms.addAll(sourceOntology.getAxioms(AxiomType.OBJECT_PROPERTY_RANGE));
		axioms.addAll(sourceOntology.getAxioms(AxiomType.SUB_OBJECT_PROPERTY));
		axioms.addAll(sourceOntology.getAxioms(AxiomType.EQUIVALENT_OBJECT_PROPERTIES));
		axioms.addAll(sourceOntology.getAxioms(AxiomType.FUNCTIONAL_OBJECT_PROPERTY));
		axioms.addAll(sourceOntology.getAxioms(AxiomType.INVERSE_FUNCTIONAL_OBJECT_PROPERTY));
		axioms.addAll(sourceOntology.getAxioms(AxiomType.DISJOINT_OBJECT_PROPERTIES));
		axioms.addAll(sourceOntology.getAxioms(AxiomType.INVERSE_OBJECT_PROPERTIES));
		axioms.addAll(sourceOntology.getAxioms(AxiomType.TRANSITIVE_OBJECT_PROPERTY));
		axioms.addAll(sourceOntology.getAxioms(AxiomType.SYMMETRIC_OBJECT_PROPERTY));
		axioms.addAll(sourceOntology.getAxioms(AxiomType.ASYMMETRIC_OBJECT_PROPERTY));
		axioms.addAll(sourceOntology.getAxioms(AxiomType.REFLEXIVE_OBJECT_PROPERTY));
		axioms.addAll(sourceOntology.getAxioms(AxiomType.IRREFLEXIVE_OBJECT_PROPERTY));
		if(axioms.size() > 0)
			manager.addAxioms(targetOntology, axioms);	
	}
	
	/**
	 * Gets the conceptual root object property.
	 *
	 * @param sourceOntology the source ontology
	 * @param targetOntology the target ontology
	 * @return the conceptual root object property
	 */
	private static OWLObjectProperty getConceptualRootObjectProperty(OWLOntology sourceOntology, OWLOntology targetOntology) {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory();
		
		// Conceptual Super Data Property
		String conceptualObjectPropLbl = null;
		String ontologyName = OntologyData.getOntologyName(sourceOntology);
		if(ontologyName.contains("LAO") || ontologyName.contains("L.A.O") ||
		  (ontologyName.contains("Linguistic") && ontologyName.contains("Attribute"))) {
			conceptualObjectPropLbl = "Linguistic Attribute Conceptual Object Property";
		}
		else if(ontologyName.contains("LLO") || ontologyName.contains("L.L.O") ||
				(ontologyName.contains("Linguistic") && ontologyName.contains("Level"))) {
			conceptualObjectPropLbl = "Linguistic Level Conceptual Object Property";
		}
		else if(ontologyName.contains("LRO") || ontologyName.contains("L.R.O") ||
				(ontologyName.contains("Linguistic") && ontologyName.contains("Relation"))) {
			conceptualObjectPropLbl = "Linguistic Relationship Conceptual Object Property";
		}
		else if(ontologyName.contains("LUO") || ontologyName.contains("L.U.O") ||
				(ontologyName.contains("Linguistic") && ontologyName.contains("Unit"))) {
			conceptualObjectPropLbl = "Linguistic Unit Conceptual Object Property";
		}
		else if(ontologyName.contains("LVO") || ontologyName.contains("L.V.O") ||
				(ontologyName.contains("Linguistic") && ontologyName.contains("Value"))) {
			conceptualObjectPropLbl = "Linguistic Value Conceptual Object Property";
		}

		// Get targetOntology Namespace
		OWLXMLDocumentFormat owlFormat = new OWLXMLDocumentFormat();
		OWLOntologyXMLNamespaceManager namespaceManager = new OWLOntologyXMLNamespaceManager(targetOntology,  owlFormat);
		String targetOntologyNamespace = namespaceManager.getDefaultNamespace();
		
		// Get the Object Property
		IRI conceptualObjectPropIRI = IRI.create(targetOntologyNamespace + StringUtils.uncapitalize(conceptualObjectPropLbl.replaceAll(" ", "")));
		OWLObjectProperty conceptualObjectProp = factory.getOWLObjectProperty(conceptualObjectPropIRI);
		
		// Label
		OWLAnnotation labelName = factory.getOWLAnnotation(factory.getRDFSLabel(), factory.getOWLLiteral(conceptualObjectPropLbl, "en"));
		OWLAxiom labelAxiom = factory.getOWLAnnotationAssertionAxiom(conceptualObjectProp.getIRI() , labelName);
		manager.applyChange(new AddAxiom(targetOntology, labelAxiom));			
		return conceptualObjectProp;
	}
	
	/**
	 * Import instances.
	 *
	 * @param sourceOntology the source ontology
	 * @param targetOntology the target ontology
	 */
	// Instances
	public static void importInstances(OWLOntology sourceOntology, OWLOntology targetOntology) {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
		// Import Axioms
		axioms.addAll(sourceOntology.getAxioms(AxiomType.CLASS_ASSERTION));
		axioms.addAll(sourceOntology.getAxioms(AxiomType.SAME_INDIVIDUAL));
		axioms.addAll(sourceOntology.getAxioms(AxiomType.DIFFERENT_INDIVIDUALS));
		axioms.addAll(sourceOntology.getAxioms(AxiomType.OBJECT_PROPERTY_ASSERTION));
		axioms.addAll(sourceOntology.getAxioms(AxiomType.NEGATIVE_OBJECT_PROPERTY_ASSERTION));
		axioms.addAll(sourceOntology.getAxioms(AxiomType.DATA_PROPERTY_ASSERTION));
		axioms.addAll(sourceOntology.getAxioms(AxiomType.NEGATIVE_DATA_PROPERTY_ASSERTION));
		axioms.addAll(OntologyData.getAssertedIndividualAnnotations(sourceOntology));//Annotations
		if(axioms.size() > 0)
			manager.addAxioms(targetOntology, axioms);	
	}		
}
