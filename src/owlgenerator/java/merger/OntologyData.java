/*
 * The Class OntologyData.
 */
package owlgenerator.java.merger;

import static owlgenerator.java.merger.StringSimilarityDistance.SIMILARITY_THRESHOLD;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.search.EntitySearcher;

import edu.stanford.nlp.util.Pair;

/**
 * The Class OntologyData.
 *
 * @author Oscar Mateos Lopez
 * @version: 20161103
 */

public class OntologyData {
	// LEAF OBJECT|DATA PROPERTIES (Instances Transformed into Properties)
	/**
	 * Gets the leaf data properties.
	 *
	 * @param ontology the ontology
	 * @return the leaf data properties
	 */
	// Data Properties
	public static Set<OWLDataProperty> getLeafDataProperties(OWLOntology ontology) {
		Set<OWLDataProperty> dataPropertys = ontology.getDataPropertiesInSignature();
		for(OWLDataProperty dataProperty_ : dataPropertys){
			Set<OWLSubDataPropertyOfAxiom> subDataProp = ontology.getDataSubPropertyAxiomsForSuperProperty(dataProperty_);
			if(!subDataProp.isEmpty() ||(!StringUtils.startsWithAny((CharSequence) dataProperty_.getIRI().getShortForm(), new String[] {"has","is", "uses"})))
				dataPropertys.remove(dataProperty_);
		}
		return dataPropertys;
	}
	
	/**
	 * Gets the leaf object properties.
	 *
	 * @param ontology the ontology
	 * @return the leaf object properties
	 */
	// Object Properties
	public static Set<OWLObjectProperty> getLeafObjectProperties(OWLOntology ontology) {
		Set<OWLObjectProperty> objectProps = ontology.getObjectPropertiesInSignature();
		for(OWLObjectProperty objectProp_ : objectProps){
			Set<OWLSubObjectPropertyOfAxiom> subObjectProp = ontology.getObjectSubPropertyAxiomsForSuperProperty(objectProp_);
			if(!subObjectProp.isEmpty())
				objectProps.remove(objectProp_);
		}		
		return objectProps;
	}

	
	// STRING SANITIZERS
	/**
	 * Gets the sanitized leaf data property name.
	 *
	 * @param leafDataProperty the leaf data property
	 * @return the sanitized leaf data property name
	 */
	// Leaf Data Properties Name
	public static String getSanitizedLeafDataPropertyName(OWLDataProperty leafDataProperty) {
		String leafDPName = leafDataProperty.getIRI().getShortForm();
		if(leafDPName.startsWith("has"))
			leafDPName = StringUtils.removeStart(leafDPName, "has");
		else if (leafDPName.startsWith("is"))
			leafDPName = StringUtils.removeStart(leafDPName, "is");
		else if (leafDPName.startsWith("uses"))
			leafDPName = StringUtils.removeStart(leafDPName, "uses");
		if (leafDPName.endsWith("Value"))
			leafDPName = StringUtils.removeEnd(leafDPName, "Value");	
		return leafDPName;
	}
	
	/**
	 * Gets the sanitized datatype name.
	 *
	 * @param customDatatype the custom datatype
	 * @return the sanitized datatype name
	 */
	// Custom Datatypes Name
	public static String getSanitizedDatatypeName(OWLDatatype customDatatype) {
		String dtName = customDatatype.getIRI().getShortForm();
		return StringUtils.removeEnd(dtName, "ValueDatatype");
	}

	
	/**
	 * Gets the ontology name.
	 *
	 * @param ontology the ontology
	 * @return the ontology name
	 */
	// ONTOLOGY NAME
	public static String getOntologyName (OWLOntology ontology) {
		Set<OWLAnnotation>  ontologyAnnotations = ontology.getAnnotations();
		for (OWLAnnotation annotation_ : ontologyAnnotations) {
			OWLAnnotationProperty ap = annotation_.getProperty();
			if (ap.isLabel()) {
				OWLAnnotationValue val = annotation_.getValue();			
				if(val instanceof OWLLiteral) 
					return val.toString().replaceAll("@en|\"", "");
			}
		}
		return null;
	}
	
	
	// ONTOLOGY ENTITY LABELS
	/**
	 * Gets the ontology class labels as string.
	 *
	 * @param ontology the ontology
	 * @return the ontology class labels as string
	 */
	// Class Labels
	public static Set<String> getOntologyClassLabelsAsString(OWLOntology ontology) {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory();
		Set <String> classLabels = new HashSet<String>();
		
		// Get the labels for each Class
		for (OWLClass class_ : ontology.getClassesInSignature()) {
			for(OWLAnnotation a : EntitySearcher.getAnnotations(class_, ontology, factory.getRDFSLabel())) {
			    OWLAnnotationValue val = a.getValue();
			    if(val instanceof OWLLiteral) {  
					String value = a.getValue().toString().replaceAll("@en|\"", "");
					classLabels.add(value);
			    }
			}		
		}
		return classLabels;
	}
	
	/**
	 * Gets the ontology ad hoc relation labels as string.
	 *
	 * @param ontology the ontology
	 * @return the ontology ad hoc relation labels as string
	 */
	// AdHoc Relation Lables
	public static Set<String> getOntologyAdHocRelationLabelsAsString(OWLOntology ontology) {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory();
		Set <String> relationLabels = new HashSet<String>();
		
		// Get the labels for each Relation
		for (OWLObjectProperty relation_ : ontology.getObjectPropertiesInSignature()) {
			for(OWLAnnotation a : EntitySearcher.getAnnotations(relation_, ontology, factory.getRDFSLabel())) {
			    OWLAnnotationValue val = a.getValue();
			    if(val instanceof OWLLiteral) {  
					String value = a.getValue().toString().replaceAll("@en|\"", "");
					relationLabels.add(value);
			    }
			}		
		}
		return relationLabels;
	}
	
	/**
	 * Gets the ontology attribute labels as string.
	 *
	 * @param ontology the ontology
	 * @return the ontology attribute labels as string
	 */
	// Attribute Lables
	public static Set<String> getOntologyAttributeLabelsAsString(OWLOntology ontology) {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory();
		Set <String> attributeLabels = new HashSet<String>();
		
		// Get the labels for each Attribute
		for (OWLDataProperty attribute_ : ontology.getDataPropertiesInSignature()) {
			for(OWLAnnotation a : EntitySearcher.getAnnotations(attribute_, ontology, factory.getRDFSLabel())) {
			    OWLAnnotationValue val = a.getValue();
			    if(val instanceof OWLLiteral) {  
					String value = a.getValue().toString().replaceAll("@en|\"", "");
					attributeLabels.add(value);
			    }
			}		
		}
		return attributeLabels;
	}	
	
	/**
	 * Gets the ontology instance labels as string.
	 *
	 * @param ontology the ontology
	 * @return the ontology instance labels as string
	 */
	// Instance Lables
	public static Set<String> getOntologyInstanceLabelsAsString(OWLOntology ontology) {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory();
		Set <String> instanceLabels = new HashSet<String>();
		
		// Get the labels for each Instance
		for (OWLNamedIndividual instance_ : ontology.getIndividualsInSignature()) {
			for(OWLAnnotation a : EntitySearcher.getAnnotations(instance_, ontology, factory.getRDFSLabel())) {
			    OWLAnnotationValue val = a.getValue();
			    if(val instanceof OWLLiteral) {  
					String value = a.getValue().toString().replaceAll("@en|\"", "");
					instanceLabels.add(value);
			    }
			}		
		}
		return instanceLabels;
	}
	
	// ENTITY ANNOTATIONS
	/**
	 * Gets the asserted concept annotations.
	 *
	 * @param ontology the ontology
	 * @return the asserted concept annotations
	 */
	// Class Annotations:
	public static Set<OWLAnnotationAssertionAxiom> getAssertedConceptAnnotations(OWLOntology ontology) {
		Set <OWLAnnotationAssertionAxiom> classAnnotationAssertions = new HashSet<OWLAnnotationAssertionAxiom>();		
		// Get the AnnotationAssertions for each Class
		for(OWLClass class_ : ontology.getClassesInSignature())
			classAnnotationAssertions.addAll(EntitySearcher.getAnnotationAssertionAxioms(class_, ontology));
		return classAnnotationAssertions;
	}
	
	/**
	 * Gets the asserted ad hoc relation annotations.
	 *
	 * @param ontology the ontology
	 * @return the asserted ad hoc relation annotations
	 */
	// AdHoc Relation Annotations:
	public static Set<OWLAnnotationAssertionAxiom> getAssertedAdHocRelationAnnotations(OWLOntology ontology) {
		Set <OWLAnnotationAssertionAxiom> relationAnnotationAssertions = new HashSet<OWLAnnotationAssertionAxiom>();
		// Get the AnnotationAssertions for each Object Property
		for(OWLObjectProperty op_ : ontology.getObjectPropertiesInSignature())
			relationAnnotationAssertions.addAll(EntitySearcher.getAnnotationAssertionAxioms(op_, ontology));		
		return relationAnnotationAssertions;
	}
	
	/**
	 * Gets the asserted attribute annotations.
	 *
	 * @param ontology the ontology
	 * @return the asserted attribute annotations
	 */
	// Attribute Annotations:
	public static Set<OWLAnnotationAssertionAxiom> getAssertedAttributeAnnotations(OWLOntology ontology) {
		Set <OWLAnnotationAssertionAxiom> attributeAnnotationAssertions = new HashSet<OWLAnnotationAssertionAxiom>();
		// Get the AnnotationAssertions for each Data Property
		for(OWLDataProperty dp_ : ontology.getDataPropertiesInSignature())
			attributeAnnotationAssertions.addAll(EntitySearcher.getAnnotationAssertionAxioms(dp_, ontology));
		return attributeAnnotationAssertions;
	}	
	
	/**
	 * Gets the asserted individual annotations.
	 *
	 * @param ontology the ontology
	 * @return the asserted individual annotations
	 */
	// Instance Annotations:
	public static Set<OWLAnnotationAssertionAxiom> getAssertedIndividualAnnotations(OWLOntology ontology) {
		Set <OWLAnnotationAssertionAxiom> individualAnnotationAssertions = new HashSet<OWLAnnotationAssertionAxiom>();
		// Get the AnnotationAssertions for each Individual
		for(OWLNamedIndividual ind_ : ontology.getIndividualsInSignature())
			individualAnnotationAssertions.addAll(EntitySearcher.getAnnotationAssertionAxioms(ind_, ontology));
		return individualAnnotationAssertions;		
	}

	
	// NAME-ANNOTATION PROPERTY PAIRS
	// AnnotationProperty: factory.getRDFSComment(), factory.getRDFSLabel(), factory.getRDFSSeeAlso()
	/**
	 * Gets the ontology class annotation pairs.
	 *
	 * @param ontology the ontology
	 * @param annotationProperty the annotation property
	 * @return the ontology class annotation pairs
	 */
	// Class-Annotation Pairs
	public static Set<Pair<String, String>> getOntologyClassAnnotationPairs(OWLOntology ontology, OWLAnnotationProperty annotationProperty) {
		Set<Pair<String, String>> naPairs = new HashSet<Pair<String, String>>();

		// Get the labels for each Class
		for(OWLClass class_ : ontology.getClassesInSignature()) {
			for(OWLAnnotation a : EntitySearcher.getAnnotations(class_, ontology, annotationProperty)) {
			    OWLAnnotationValue val = a.getValue();
			    if(val instanceof OWLLiteral) { 
			    	String name = class_.getIRI().getShortForm();
					String lbl = a.getValue().toString().replaceAll("@en|\"", "");
					Pair<String, String> p = new Pair<String, String>(name, lbl);
					naPairs.add(p);										
			    }
			}		
		}	
		return naPairs;
	}
	
	/**
	 * Gets the ontology ad hoc relation annotation pairs.
	 *
	 * @param ontology the ontology
	 * @param annotationProperty the annotation property
	 * @return the ontology ad hoc relation annotation pairs
	 */
	// AdHoc Relation-Annotation Pairs(Object Properties)
	public static Set<Pair<String, String>> getOntologyAdHocRelationAnnotationPairs(OWLOntology ontology, OWLAnnotationProperty annotationProperty) {
		Set<Pair<String, String>> naPairs = new HashSet<Pair<String, String>>();
		
		// Get the labels for each Object Property
		for(OWLObjectProperty op_ : ontology.getObjectPropertiesInSignature()) {
			for(OWLAnnotation a : EntitySearcher.getAnnotations(op_, ontology, annotationProperty)) {
			    OWLAnnotationValue val = a.getValue();
			    if(val instanceof OWLLiteral) { 
			    	String name = op_.getIRI().getShortForm();
					String lbl = a.getValue().toString().replaceAll("@en|\"", "");
					Pair<String, String> p = new Pair<String, String>(name, lbl);
					naPairs.add(p);										
			    }
			}		
		}	
		return naPairs;
	}
	
	/**
	 * Gets the ontology attribute annotation pairs.
	 *
	 * @param ontology the ontology
	 * @param annotationProperty the annotation property
	 * @return the ontology attribute annotation pairs
	 */
	// Attribute-Annotation Pairs(Data Properties)
	public static Set<Pair<String, String>> getOntologyAttributeAnnotationPairs(OWLOntology ontology, OWLAnnotationProperty annotationProperty) {
		Set<Pair<String, String>> naPairs = new HashSet<Pair<String, String>>();
		
		// Get the labels for each Data Property
		for(OWLDataProperty dp_ : ontology.getDataPropertiesInSignature()) {
			for(OWLAnnotation a : EntitySearcher.getAnnotations(dp_, ontology, annotationProperty)) {
			    OWLAnnotationValue val = a.getValue();
			    if(val instanceof OWLLiteral) { 
			    	String name = dp_.getIRI().getShortForm();
					String lbl = a.getValue().toString().replaceAll("@en|\"", "");
					Pair<String, String> p = new Pair<String, String>(name, lbl);
					naPairs.add(p);										
			    }
			}		
		}		
		return naPairs;
	}	
	
	/**
	 * Gets the ontology instance annotation pairs.
	 *
	 * @param ontology the ontology
	 * @param annotationProperty the annotation property
	 * @return the ontology instance annotation pairs
	 */
	// Instance-Annotation Pairs
	public static Set<Pair<String, String>> getOntologyInstanceAnnotationPairs(OWLOntology ontology, OWLAnnotationProperty annotationProperty) {
		Set<Pair<String, String>> naPairs = new HashSet<Pair<String, String>>();
		
		// Get the labels for each Individual
		for(OWLNamedIndividual ind_ : ontology.getIndividualsInSignature()) {
			for(OWLAnnotation a : EntitySearcher.getAnnotations(ind_, ontology, annotationProperty)) {
			    OWLAnnotationValue val = a.getValue();
			    if(val instanceof OWLLiteral) { 
			    	String name = ind_.getIRI().getShortForm();
					String lbl = a.getValue().toString().replaceAll("@en|\"", "");
					Pair<String, String> p = new Pair<String, String>(name, lbl);
					naPairs.add(p);										
			    }
			}		
		}		
		return naPairs;
	}	


	/**
	 * Gets the ontology subclass of pairs.
	 *
	 * @param ontology the ontology
	 * @return the ontology subclass of pairs
	 */
	// SUBCLASS-OF PAIRS
	public static Set<OWLSubClassOfAxiom> getOntologySubclassOfPairs(OWLOntology ontology) {
		Set<OWLSubClassOfAxiom> subclassOfAxioms = ontology.getAxioms(AxiomType.SUBCLASS_OF);
		// Filter anonymous superclasses (axioms whose superclass is a DataHasValue(OWLDataHasValue))
		subclassOfAxioms.removeIf(HAS_ANONYMOUS_SUPERCLASS);		
		return subclassOfAxioms;
	}		
	
	/** The has anonymous superclass. */
	private static Predicate<OWLSubClassOfAxiom> HAS_ANONYMOUS_SUPERCLASS = new Predicate<OWLSubClassOfAxiom>() {
		//@Override
		  public boolean test(OWLSubClassOfAxiom axiom) {
			  if(axiom.getSuperClass().isAnonymous())
					return true;
			  return false;		 
		  }
	};	
	
		
	/**
	 * Gets the instance set for class from assertions.
	 *
	 * @param assertions the assertions
	 * @param assertedClass the asserted class
	 * @return the instance set for class from assertions
	 */
	// CLASS ASSERTED INSTANCES
	public static Set<OWLNamedIndividual> getInstanceSetForClassFromAssertions(Set<OWLClassAssertionAxiom> assertions, OWLClass assertedClass) {
		Set<OWLNamedIndividual> assertedIndividuals = new HashSet<OWLNamedIndividual>();
		for(OWLClassAssertionAxiom assertion_ : assertions) {
			if(assertedClass.equals(assertion_.getClassesInSignature().iterator().next()))
				assertedIndividuals.addAll(assertion_.getIndividualsInSignature());
		}	
		return assertedIndividuals;
	}
	
	
	// CLASS SEARCHER BY NAME-SIMILARITY
	/**
	 * Find most similar class.
	 *
	 * @param concept the concept
	 * @param targetOntology the target ontology
	 * @return the OWL class
	 */
	// ClassNames similar to a given concept
	public static OWLClass findMostSimilarClass(String concept, OWLOntology targetOntology) {
		// get targetOntology Classes and perform matching
		String propertyNameSanitized = StringUtils.capitalize(StringUtils.removeEnd(concept, "Attribute"));
		propertyNameSanitized = StringUtils.removeEnd(propertyNameSanitized, "al");

		// get the Classes
		Set<OWLClass> ontologyClasses = targetOntology.getClassesInSignature();
		
		// Assess the distance for the propertyName with all the Classes (string-similar)
		List <Pair<OWLClass, Double>> measures = new ArrayList <Pair<OWLClass, Double>>();
		for (OWLClass class_ : ontologyClasses) {
			String className = class_.getIRI().getShortForm();
			String classNameSanitized = StringUtils.capitalize(StringUtils.removeEnd(className, "Unit"));
			double measure = StringSimilarityDistance.getSecuentialJaroWrinklerDistance(classNameSanitized, propertyNameSanitized);
			//System.out.print(className+" ,"+measure+" | ");
			measures.add(new Pair<OWLClass, Double>(class_, measure));
		}
		// Sort measures
		if(measures.size() > 0) {
			Collections.sort(measures, Comparator.comparing(p -> -p.second()));	
			// Return first (best measure, if passes the threshold)
			Pair<OWLClass, Double> best = measures.listIterator().next();
			if (best.second() >= SIMILARITY_THRESHOLD)
				return best.first();
		}
		return null;
	}	
	
	/**
	 * Find most similar class.
	 *
	 * @param propertyName the property name
	 * @param excludedNamespaces the excluded namespaces
	 * @param targetOntology the target ontology
	 * @return the OWL class
	 */
	// ClassNames similar to a given concept excluding Classes with specific Namespaces
	public static OWLClass findMostSimilarClass(String propertyName, Set<String> excludedNamespaces, OWLOntology targetOntology) {
		// get targetOntology Classes and perform matching
		String propertyNameSanitized = StringUtils.capitalize(StringUtils.removeEnd(propertyName, "Attribute"));
		propertyNameSanitized = StringUtils.removeEnd(propertyNameSanitized, "al");

		// get the Classes excluding the excludedNamespace Concepts
		Set<OWLClass> ontologyClasses = targetOntology.getClassesInSignature();
		for (OWLClass class_ : ontologyClasses)
			if (excludedNamespaces.contains(class_.getIRI().getNamespace()))
				ontologyClasses.remove(class_);
		
		// Assess the distance for the propertyName with all the Classes (string-similar)
		List <Pair<OWLClass, Double>> measures = new ArrayList <Pair<OWLClass, Double>>();
		for (OWLClass class_ : ontologyClasses) {
			String className = class_.getIRI().getShortForm();
			String classNameSanitized = StringUtils.capitalize(StringUtils.removeEnd(className, "Unit"));
			double measure = StringSimilarityDistance.getSecuentialJaroWrinklerDistance(classNameSanitized, propertyNameSanitized);
			measures.add(new Pair<OWLClass, Double>(class_, measure));
		}	
		// Sort secondMeasures
		if(measures.size() > 0) {
			Collections.sort(measures, Comparator.comparing(p -> -p.second()));
			// Return first (best measure, if passes the threshold)
			Pair<OWLClass, Double> best = measures.listIterator().next();
			if (best.second() >= SIMILARITY_THRESHOLD)
				return best.first();
		}
		return null;
	}
	
	
	// ONTOLOGY SAME-NAME-ENTITIES SETS
	/**
	 * Gets the same name concepts as string.
	 *
	 * @param sourceOntology1 the source ontology 1
	 * @param sourceOntology2 the source ontology 2
	 * @return the same name concepts as string
	 */
	// Same Name Classes (between)
	public static Set<String> getSameNameConceptsAsString(OWLOntology sourceOntology1, OWLOntology sourceOntology2) {
		// get Ontology1 Classes 
		Set<OWLClass> ontology1Classes = sourceOntology1.getClassesInSignature();
		Set<String> ontology1ClassNames = new HashSet<String>();
		for (OWLClass class_ : ontology1Classes) 
			ontology1ClassNames.add(class_.getIRI().getShortForm());

		// get Ontology2 Classes
		Set<OWLClass> ontology2Classes = sourceOntology2.getClassesInSignature();
		Set<String> ontology2ClassNames = new HashSet<String>();
		for (OWLClass class_ : ontology2Classes) 
			ontology2ClassNames.add(class_.getIRI().getShortForm());
		
		// get the intersection of both ClassNames Sets
		Set<String> intersection = new HashSet<String>(ontology1ClassNames);
		intersection.retainAll(ontology2ClassNames);	
		return intersection;		
	}
	
	/**
	 * Gets the same name concepts as string.
	 *
	 * @param ontology the ontology
	 * @return the same name concepts as string
	 */
	// Same Name Classes (within) 
	public static Set<String> getSameNameConceptsAsString(OWLOntology ontology) {
		// get Ontology Classes 
		Set<OWLClass> ontologyClasses = ontology.getClassesInSignature();
		
		// HashSets doesn't allow duplicates
		Set<String> ontologyClassNames = new HashSet<String>(), sameNameClasses = new HashSet<String>();
		for (OWLClass class_ : ontologyClasses) {
			String className = class_.getIRI().getShortForm();
			// failing adding it to the set, implies it's duplicated
			if (!ontologyClassNames.add(className))
				//and goes to the sameNameClasses set
				sameNameClasses.add(className);
		}		
		return sameNameClasses;		
	}
	
	/**
	 * Gets the same name attributes as string.
	 *
	 * @param sourceOntology1 the source ontology 1
	 * @param sourceOntology2 the source ontology 2
	 * @return the same name attributes as string
	 */
	// Same Name Data Properties (between)
	public static Set<String> getSameNameAttributesAsString(OWLOntology sourceOntology1, OWLOntology sourceOntology2) {
		// get Ontology1 Data Properties 
		Set<OWLDataProperty> ontology1DataProperties = sourceOntology1.getDataPropertiesInSignature();
		Set<String> ontology1DataPropertyNames = new HashSet<String>();
		for (OWLDataProperty class_ : ontology1DataProperties) 
			ontology1DataPropertyNames.add(class_.getIRI().getShortForm());

		// get Ontology2 Data Properties
		Set<OWLDataProperty> ontology2DataProperties = sourceOntology2.getDataPropertiesInSignature();
		Set<String> ontology2DataPropertyNames = new HashSet<String>();
		for (OWLDataProperty dp_ : ontology2DataProperties) 
			ontology2DataPropertyNames.add(dp_.getIRI().getShortForm());
		
		// get the intersection of both DataPropertyNames Sets
		Set<String> intersection = new HashSet<String>(ontology1DataPropertyNames);
		intersection.retainAll(ontology2DataPropertyNames);	
		return intersection;		
	}
	
	/**
	 * Gets the same name attributes as string.
	 *
	 * @param ontology the ontology
	 * @return the same name attributes as string
	 */
	// Same Name Data Properties (within) 
	public static Set<String> getSameNameAttributesAsString(OWLOntology ontology) {
		// get Ontology Data Properties 
		Set<OWLDataProperty> ontologyDataProperties = ontology.getDataPropertiesInSignature();
		
		// HashSets doesn't allow duplicates
		Set<String> ontologyClassNames = new HashSet<String>(), sameNameDataProperties = new HashSet<String>();
		for (OWLDataProperty dp_ : ontologyDataProperties) {
			String className = dp_.getIRI().getShortForm();
			// failing adding it to the set, implies it's duplicated
			if (!ontologyClassNames.add(className))
				//and goes to the sameNameDataProperties set
				sameNameDataProperties.add(className);
		}	
		return sameNameDataProperties;		
	}
	
	/**
	 * Gets the same name ad hoc relations as string.
	 *
	 * @param sourceOntology1 the source ontology 1
	 * @param sourceOntology2 the source ontology 2
	 * @return the same name ad hoc relations as string
	 */
	// Same Name Object Properties (between)
	public static Set<String> getSameNameAdHocRelationsAsString(OWLOntology sourceOntology1, OWLOntology sourceOntology2) {
		// get Ontology1 Object Properties 
		Set<OWLObjectProperty> ontology1ObjectProperties = sourceOntology1.getObjectPropertiesInSignature();
		Set<String> ontology1ObjectPropertyNames = new HashSet<String>();
		for (OWLObjectProperty class_ : ontology1ObjectProperties) 
			ontology1ObjectPropertyNames.add(class_.getIRI().getShortForm());

		// get Ontology2 Object Properties
		Set<OWLObjectProperty> ontology2ObjectProperties = sourceOntology2.getObjectPropertiesInSignature();
		Set<String> ontology2ObjectPropertyNames = new HashSet<String>();
		for (OWLObjectProperty op_ : ontology2ObjectProperties) 
			ontology2ObjectPropertyNames.add(op_.getIRI().getShortForm());
		
		// get the intersection of both ObjectPropertyNames Sets
		Set<String> intersection = new HashSet<String>(ontology1ObjectPropertyNames);
		intersection.retainAll(ontology2ObjectPropertyNames);	
		return intersection;		
	}
	
	/**
	 * Gets the same name ad hoc relations as string.
	 *
	 * @param ontology the ontology
	 * @return the same name ad hoc relations as string
	 */
	// Same Name Data Properties (within) 
	public static Set<String> getSameNameAdHocRelationsAsString(OWLOntology ontology) {
		// get Ontology Object Properties 
		Set<OWLObjectProperty> ontologyObjectProperties = ontology.getObjectPropertiesInSignature();
		
		// HashSets doesn't allow duplicates
		Set<String> ontologyClassNames = new HashSet<String>(), sameNameObjectProperties = new HashSet<String>();
		for (OWLObjectProperty op_ : ontologyObjectProperties) {
			String className = op_.getIRI().getShortForm();
			// failing adding it to the set, implies it's duplicated
			if (!ontologyClassNames.add(className))
				//and goes to the sameNameObjectProperties set
				sameNameObjectProperties.add(className);
		}	
		return sameNameObjectProperties;		
	}
	
	// ONTOLOGY PARTICULAR-NAME-ENTITY SETS
	/**
	 * Gets the particular name classes.
	 *
	 * @param className the class name
	 * @param ontology the ontology
	 * @return the particular name classes
	 */
	// Particular Name Classes (between)
	public static Set<OWLClass> getParticularNameClasses(String className, OWLOntology ontology) {
		Set<OWLClass> ontologyClasses = ontology.getClassesInSignature();
		Set<OWLClass> sameNameClasses = new HashSet<OWLClass>();
		for (OWLClass class_ : ontologyClasses) {
			String currentClassName = class_.getIRI().getShortForm();
			if (className.equals(currentClassName))
				sameNameClasses.add(class_);
		}		
		return sameNameClasses;				
	}
	
	/**
	 * Gets the particular name data properties.
	 *
	 * @param dataPropertyertyName the data propertyerty name
	 * @param ontology the ontology
	 * @return the particular name data properties
	 */
	// Particular Name Data Properties (between)
	public static Set<OWLDataProperty> getParticularNameDataProperties(String dataPropertyertyName, OWLOntology ontology) {
		Set<OWLDataProperty> ontologyDataProperties = ontology.getDataPropertiesInSignature();
		Set<OWLDataProperty> sameNameDataProperties = new HashSet<OWLDataProperty>();
		for (OWLDataProperty dp_ : ontologyDataProperties) {
			String currentClassName = dp_.getIRI().getShortForm();
			if (dataPropertyertyName.equals(currentClassName))
				sameNameDataProperties.add(dp_);
		}		
		return sameNameDataProperties;				
	}
	
	/**
	 * Gets the particular name object properties.
	 *
	 * @param dataPropertyertyName the data propertyerty name
	 * @param ontology the ontology
	 * @return the particular name object properties
	 */
	// Particular Name Object Properties (between)
	public static Set<OWLObjectProperty> getParticularNameObjectProperties(String dataPropertyertyName, OWLOntology ontology) {
		Set<OWLObjectProperty> ontologyObjectProperties = ontology.getObjectPropertiesInSignature();
		Set<OWLObjectProperty> sameNameObjectProperties = new HashSet<OWLObjectProperty>();
		for (OWLObjectProperty op_ : ontologyObjectProperties) {
			String currentClassName = op_.getIRI().getShortForm();
			if (dataPropertyertyName.equals(currentClassName))
				sameNameObjectProperties.add(op_);
		}	
		return sameNameObjectProperties;				
	}

	
	// ONTOLOGY DEBUG
	/**
	 * Gets the unlabeled classes.
	 *
	 * @param ontology the ontology
	 * @return the unlabeled classes
	 */
	// Find Classes without label (implies source error)
	public static Set<OWLClass> getUnlabeledClasses(OWLOntology ontology) { 
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory();
		Set<OWLClass> ontologyClasses = ontology.getClassesInSignature();
		Set<OWLClass> unlabeledClasses = new HashSet<OWLClass>();
		for (OWLClass class_ : ontologyClasses) 
			if(EntitySearcher.getAnnotations(class_, ontology, factory.getRDFSLabel()).isEmpty())
				unlabeledClasses.add(class_);
		return unlabeledClasses;		
	}
	
	/**
	 * Gets the unlabeled data properties.
	 *
	 * @param ontology the ontology
	 * @return the unlabeled data properties
	 */
	// Find DataProperties without label (implies source error)
	public static Set<OWLDataProperty> getUnlabeledDataProperties(OWLOntology ontology) { 
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory();
		Set<OWLDataProperty> ontologyDataProperties = ontology.getDataPropertiesInSignature();
		Set<OWLDataProperty> unlabeledDataProperties = new HashSet<OWLDataProperty>();
		for (OWLDataProperty dp_ : ontologyDataProperties) 
			if(EntitySearcher.getAnnotations(dp_, ontology, factory.getRDFSLabel()).isEmpty())
				unlabeledDataProperties.add(dp_);
		return unlabeledDataProperties;		
	}
	
	/**
	 * Gets the unlabeled object properties.
	 *
	 * @param ontology the ontology
	 * @return the unlabeled object properties
	 */
	// Find ObjectProperties without label (implies source error)
	public static Set<OWLObjectProperty> getUnlabeledObjectProperties(OWLOntology ontology) { 
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory();
		Set<OWLObjectProperty> ontologyObjectProperties = ontology.getObjectPropertiesInSignature();
		Set<OWLObjectProperty> unlabeledObjectProperties = new HashSet<OWLObjectProperty>();
		for (OWLObjectProperty op_ : ontologyObjectProperties) 
			if(EntitySearcher.getAnnotations(op_, ontology, factory.getRDFSLabel()).isEmpty())
				unlabeledObjectProperties.add(op_);
		return unlabeledObjectProperties;		
	}
	
	/**
	 * Gets the unlabeled named individuals.
	 *
	 * @param ontology the ontology
	 * @return the unlabeled named individuals
	 */
	// Find NamedIndividuals without label (implies source error)
	public static Set<OWLNamedIndividual> getUnlabeledNamedIndividuals(OWLOntology ontology) { 
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory();
		Set<OWLNamedIndividual> ontologyNamedIndividuals = ontology.getIndividualsInSignature();
		Set<OWLNamedIndividual> unlabeledNamedIndividuals = new HashSet<OWLNamedIndividual>();
		for (OWLNamedIndividual ni_ : ontologyNamedIndividuals) 
			if(EntitySearcher.getAnnotations(ni_, ontology, factory.getRDFSLabel()).isEmpty())
				unlabeledNamedIndividuals.add(ni_);
		return unlabeledNamedIndividuals;		
	}
}
