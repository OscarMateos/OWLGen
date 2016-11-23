/*
 * Core Class of the OWLGenerator, includes the main methods to generate OWL ontologies from formatted text files.
 */
package owlgenerator.java.core;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataOneOf;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDataUnionOf;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLQuantifiedObjectRestriction;
import org.semanticweb.owlapi.model.OWLRestriction;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.model.SetOntologyID;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.vocab.XSDVocabulary;
import org.xadisk.filesystem.exceptions.FileNotExistsException;

import com.google.common.base.Optional;

import edu.stanford.nlp.util.Pair;
import owlgenerator.java.inverserelations.InferInverseRelations;
import owlgenerator.java.util.Util;

/**
 * Core Class of the OWLGenerator, includes the main methods to generate OWL ontologies from formatted text files.
 *
 * @author Oscar Mateos Lopez
 * @version: 20161103
 */
public class Core {
	
	/** The cfg. */
	private static CoreConfiguration cfg = null;
	
	/** The ontology. */
	private static OWLOntology ontology = null;
	
	/** The manager. */
	private static OWLOntologyManager manager = null;
	
	/** The pm. */
	private static PrefixManager pm = null;
	
	/** The processed tables. */
	private static Set<String> processedTables = null;	
	
	// CONSTRUCTOR
	static {
		cfg = CoreConfiguration.getInstance();
		manager = OWLManager.createOWLOntologyManager();
		pm = new DefaultPrefixManager();
		processedTables = new HashSet<String>();
		IRI ontologyIRI = IRI.create(cfg.getBase_Url() + cfg.getOntology_Name().replace(" ", "_"));
		IRI versionIRI = IRI.create(ontologyIRI + "/" + Util.getCurrentTimeStamp());
		pm.setDefaultPrefix(ontologyIRI.toString() + "#");	
		try {
			ontology = manager.createOntology(ontologyIRI);
			Optional<IRI> optTargetOntologyIRI = Optional.of(ontologyIRI);
			Optional<IRI> optVersionIRI = Optional.of(versionIRI); 
			OWLOntologyID newOntologyID = new OWLOntologyID(optTargetOntologyIRI, optVersionIRI);
			SetOntologyID setOntologyID = new SetOntologyID(ontology, newOntologyID);
			manager.applyChange(setOntologyID);
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}		
		// Annotations
		// Ontology Name
		OWLDataFactory factory = manager.getOWLDataFactory();
		OWLAnnotation lblName = factory.getOWLAnnotation(factory.getRDFSLabel(), factory.getOWLLiteral(cfg.getOntology_Name(), "en"));
		OWLAxiom axiom = factory.getOWLAnnotationAssertionAxiom(ontologyIRI , lblName);
		manager.applyChange(new AddAxiom(ontology, axiom));			
		// Version
		OWLAnnotation version = factory.getOWLAnnotation(factory.getOWLVersionInfo(), factory.getOWLLiteral(cfg.getOntology_Version(), "en"));
		axiom = factory.getOWLAnnotationAssertionAxiom(ontologyIRI , version);
		manager.applyChange(new AddAxiom(ontology, axiom));	
	}
	
	// GETTERS 	
	/**
	 * Gets the cfg.
	 *
	 * @return The cfg
	 */
	public CoreConfiguration getCfg() {
		return cfg;
	}
	
	/**
	 * Gets the ontology.
	 *
	 * @return The ontology
	 */
	public OWLOntology getOntology() {
		return ontology;
	}
	
	/**
	 * Gets the manager.
	 *
	 * @return The manager
	 */
	public OWLOntologyManager getManager() {
		return manager;
	}

	/**
	 * Gets the pm.
	 *
	 * @return The pm
	 */
	public PrefixManager getPm() {
		return pm;
	}
	
	/**
	 * Gets the processed tables.
	 *
	 * @return The processed tables
	 */
	public Set<String> getProcessedTables() {
		return processedTables;
	}

	/**
	 * Reset processed tables.
	 */
	// UTILITY 
	public void resetProcessedTables() {
		processedTables.clear();
	}
	
	/**
	 * Change the Core ontology.
	 *
	 * @param ontology, The ontology
	 */
	public void changeOntology(OWLOntology ontology) {
		Core.ontology = ontology;
	}
	
	
	// MAIN METHODS
	// GENERAL OWL ENTITIES	
	/**
	 * Gets an OWL Class from the provided concept.
	 *
	 * @param concept, The concept
	 * @return The generated OWL class
	 */
	public OWLClass getOWLClassFromName(String concept) {
		OWLDataFactory factory = manager.getOWLDataFactory();	
		if (StringUtils.upperCase(concept, Locale.ENGLISH).equals(concept))
			// For all uppercase literals, separate with "_"
			return factory.getOWLClass(":" + concept.replaceAll(" ", "_"), pm);
		else
			return factory.getOWLClass(":" + concept.replaceAll(" ", ""), pm);
	}	
	
	
	/**
	 * Gets an OWL Object Property from the provided concept.
	 *
	 * @param concept, The concept
	 * @return The generated OWL Object Property
	 */
	public OWLObjectProperty getOWLObjectPropertyFromName(String concept) {	
		OWLDataFactory factory = manager.getOWLDataFactory();
		return factory.getOWLObjectProperty(":" + StringUtils.uncapitalize(concept.replaceAll(" ", "")), pm);
	}		
	
	
	/**
	 * Gets an OWL Data Property from the provided concept.
	 *
	 * @param concept, The concept
	 * @return The OWL Data Property
	 */
	public OWLDataProperty getOWLDataPropertyFromName(String concept) {
		OWLDataFactory factory = manager.getOWLDataFactory();	
		return factory.getOWLDataProperty(":" + StringUtils.uncapitalize(concept.replaceAll(" ", "")), pm);
	}	
	
	/**
	 * Gets an OWL Individual from the provided concept.
	 *
	 * @param concept, The concept
	 * @return The OWL Individual
	 */
	public OWLIndividual getOWLIndividualFromName(String concept) {
		OWLDataFactory factory = manager.getOWLDataFactory();		
		if (StringUtils.upperCase(concept, Locale.ENGLISH).equals(concept))
			// For all uppercase literals, separate with "_"
			return factory.getOWLNamedIndividual(":" + concept.replaceAll(" ", "_"), pm);
		else
			return factory.getOWLNamedIndividual(":" + concept.replaceAll(" ", ""), pm);
	}
	
	
	/**
	 * Gets the equivalent classes axioms set.
	 *
	 * @param sourceConcept, the source concept
	 * @param detail, the synonyms
	 * @return The equivalent classes axioms set 
	 */
	// EQUIVALENCE AXIOMS
	public Set<OWLAxiom> getEquivalentClassesAxioms(String sourceConcept, String detail) {
		OWLDataFactory factory = manager.getOWLDataFactory();
		Set<OWLAxiom> axiomSet = new HashSet<OWLAxiom>();
		OWLClass sourceClass = getOWLClassFromName(sourceConcept);
		String[] synonyms = detail.split(",");
		Set<OWLClass> equivalentClasses = new HashSet<OWLClass>();
		
		// Class definition axioms
		equivalentClasses.add(sourceClass);
		
		// Synonym Classes
		for (String synonym : synonyms){
			OWLClass synonymClass = getOWLClassFromName(synonym);
			equivalentClasses.add(synonymClass);	
			OWLDeclarationAxiom declaration = factory.getOWLDeclarationAxiom(synonymClass);
			axiomSet.add(declaration);	
			
			// Label
			OWLAnnotation lblSynonymClass = factory.getOWLAnnotation(factory.getRDFSLabel(), factory.getOWLLiteral(synonym.trim(), "en"));
			OWLAnnotationAssertionAxiom lblSynonymClassAxiom = factory.getOWLAnnotationAssertionAxiom(synonymClass.getIRI(), lblSynonymClass);
			axiomSet.add(lblSynonymClassAxiom);			
		}
		axiomSet.add(factory.getOWLEquivalentClassesAxiom(equivalentClasses));
		return axiomSet;
	}

	/**
	 * Gets the equivalent object property axioms set.
	 *
	 * @param sourceConcept, the source concept
	 * @param detail, the synonyms
	 * @return The equivalent object property axioms set
	 */
	public Set<OWLAxiom> getEquivalentObjectPropertyAxioms(String sourceConcept, String detail) {
		OWLDataFactory factory = manager.getOWLDataFactory();
		Set<OWLAxiom> axiomSet = new HashSet<OWLAxiom>();
		OWLObjectProperty sourceObjProp = getOWLObjectPropertyFromName(sourceConcept);
		String[] synonyms = detail.split(",");
		Set<OWLObjectProperty> equivalentClasses = new HashSet<OWLObjectProperty>();
		
		// Object Property definition axioms
		equivalentClasses.add(sourceObjProp);
		
		//	Synonym Object Properties 
		for (String synonym : synonyms){
			OWLObjectProperty synonymObjProp = getOWLObjectPropertyFromName(synonym);
			equivalentClasses.add(synonymObjProp);
			OWLDeclarationAxiom declaration = factory.getOWLDeclarationAxiom(synonymObjProp);
			axiomSet.add(declaration);	
			
			// Label
			OWLAnnotation lblSynonymObjProp = factory.getOWLAnnotation(factory.getRDFSLabel(), factory.getOWLLiteral(synonym.trim(), "en"));
			OWLAnnotationAssertionAxiom lblSynonymClassAxiom = factory.getOWLAnnotationAssertionAxiom(synonymObjProp.getIRI(), lblSynonymObjProp);
			axiomSet.add(lblSynonymClassAxiom);			
		}
		axiomSet.add(factory.getOWLEquivalentObjectPropertiesAxiom(equivalentClasses));
		return axiomSet;
	}	
	
	/**
	 * Gets the equivalent Data Property axioms set.
	 *
	 * @param sourceConcept the source concept
	 * @param detail, the synonyms
	 * @return The equivalent Data Property axioms set
	 */
	public Set<OWLAxiom> getEquivalentDataPropertyAxioms(String sourceConcept, String detail) {
		OWLDataFactory factory = manager.getOWLDataFactory();
		Set<OWLAxiom> axiomSet = new HashSet<OWLAxiom>();
		OWLDataProperty sourceObjProp = getOWLDataPropertyFromName(sourceConcept);
		String[] synonyms = detail.split(",");
		Set<OWLDataProperty> equivalentDataProps = new HashSet<OWLDataProperty>();
		
		// Data Property definition axioms
		equivalentDataProps.add(sourceObjProp);
		
		// Synonym Data Properties 
		for (String synonym : synonyms){
			OWLDataProperty synonymObjProp = getOWLDataPropertyFromName(synonym);
			equivalentDataProps.add(synonymObjProp);
			OWLDeclarationAxiom declaration = factory.getOWLDeclarationAxiom(synonymObjProp);
			axiomSet.add(declaration);	
			
			// Label
			OWLAnnotation lblSynonymObjProp = factory.getOWLAnnotation(factory.getRDFSLabel(), factory.getOWLLiteral(synonym.trim(), "en"));
			OWLAnnotationAssertionAxiom lblSynonymClassAxiom = factory.getOWLAnnotationAssertionAxiom(synonymObjProp.getIRI(), lblSynonymObjProp);
			axiomSet.add(lblSynonymClassAxiom);			
		}	
		axiomSet.add(factory.getOWLEquivalentDataPropertiesAxiom(equivalentDataProps));
		return axiomSet;
	}	
	
	/**
	 * Gets the equivalent individuals axioms set.
	 *
	 * @param sourceConcept the source concept
	 * @param detail, the synonyms
	 * @return The equivalent individuals axioms set
	 */
	public Set<OWLAxiom> getEquivalentIndividualsAxioms(String sourceConcept, String detail) {
		OWLDataFactory factory = manager.getOWLDataFactory();
		Set<OWLAxiom> axiomSet = new HashSet<OWLAxiom>();
		OWLIndividual sourceIndividual = getOWLIndividualFromName(sourceConcept);
		String[] synonyms = detail.split(",");
		Set<OWLIndividual> equivalentIndividuals = new HashSet<OWLIndividual>();
		
		// Individual definition axioms
		equivalentIndividuals.add(sourceIndividual);
		
		// Synonym Individuals
		for (String synonym : synonyms){
			// OWLIndividual
			OWLIndividual synonymInd = getOWLIndividualFromName(synonym);
			equivalentIndividuals.add(synonymInd);
			OWLDeclarationAxiom declaration = factory.getOWLDeclarationAxiom((OWLEntity) synonymInd);
			axiomSet.add(declaration);	
			
			// Label
			OWLAnnotation lblSynonymInd = factory.getOWLAnnotation(factory.getRDFSLabel(), factory.getOWLLiteral(synonym.trim(), "en"));
			OWLAnnotationAssertionAxiom lblSynonymIndAxiom = factory.getOWLAnnotationAssertionAxiom(synonymInd.asOWLNamedIndividual().getIRI(), lblSynonymInd);		
			axiomSet.add(lblSynonymIndAxiom);			
		}
		axiomSet.add(factory.getOWLSameIndividualAxiom(equivalentIndividuals));
		return axiomSet;
	}
	
	
	// TAXONOMY
	/**
	 * Gets the Subclass-Of axioms from a single line of the formatted text file as input.
	 *
	 * @param fileLine, a single file line from the source file
	 * @return The Subclass-Of axioms set
	 * @throws FileNotExistsException if the jump file not exists
	 */
	// SubclassOf
	public Set<OWLAxiom> getSubClassOfAxiomsFromLine(String fileLine) throws FileNotExistsException {
		OWLDataFactory factory = manager.getOWLDataFactory();
		String generalization = null, specialization = null;
		OWLClass generalizationClass = null, specializationClass = null;
		OWLAnnotation generalizationClassLabel = null, specializationClassLabel = null;
		OWLAxiom subclassAxiom = null, generalizationClassLabelAxiom = null, specializationClassLabelAxiom = null;
		Set<OWLAxiom> axiomSet = new HashSet<OWLAxiom>();
		
		String[] parts = fileLine.split(CoreConfiguration.SEPARATOR);
		for (int i = 0; i < parts.length - 1; i++) {
			generalization = parts[i];
			specialization = parts[i + 1];
			
			// Jump to another table
			Pattern jumpPattern = Pattern.compile("\\[\\s*[Cc]ontinue[sd]?\\s*in\\s*([Tt]able\\s*\\d+\\w?)\\s*\\]{1}");
			Matcher jumpMatcher = jumpPattern.matcher(specialization);
			if (jumpMatcher.matches()) {
				// Seek the file
				File continueTable = Util.getContinuedTable(specialization);
				if (continueTable == null)
					throw new FileNotExistsException("File related to " + jumpMatcher.group(1));	
				
				// Process the file			
				String fileName = continueTable.getName();

				// Pruning mechanism, for tables with several jumps to the same table
				if (!processedTables.contains(fileName)) {
					addSubClassOfAxiomsFromTables(fileName);
					processedTables.add(fileName);
				}
			}
			// Case without jumps
			else if (!specialization.equals(CoreConfiguration.STOP)) {		
				Pattern pattern = Pattern.compile("([\\s*\\w+\\-\\/]+)\\(([\\w+\\,\\-\\/\\s*]+)\\)");				
				Matcher matcherGeneralization = pattern.matcher(generalization.trim());
				Matcher matcherespecialization = pattern.matcher(specialization.trim());
				
				// Generalization Synonyms
				if (matcherGeneralization.matches()) {
					generalization = matcherGeneralization.group(1).trim();
					String detail = matcherGeneralization.group(2).trim();
					axiomSet.addAll(getEquivalentClassesAxioms(generalization, detail));
				}			
				// Specialization Synonyms 
				if (matcherespecialization.matches()) {
					specialization = matcherespecialization.group(1).trim();
					String detail = matcherespecialization.group(2).trim();
					
					// Special case, other detail
					if (detail.equalsIgnoreCase("Open") || detail.equalsIgnoreCase("Close")) {
						specialization = specialization + "_" + detail;
					}
					else{
						// Synonyms			
						axiomSet.addAll(getEquivalentClassesAxioms(specialization, detail));
					}
				}
				// Get OWL Classes
				generalizationClass = getOWLClassFromName(generalization.replace("/", "-"));
				specializationClass = getOWLClassFromName(specialization.replace("/", "-"));

				// Get SubclassOf Axiom
				subclassAxiom = factory.getOWLSubClassOfAxiom(specializationClass, generalizationClass);
				axiomSet.add(subclassAxiom);

				// Labels
				generalizationClassLabel = factory.getOWLAnnotation(factory.getRDFSLabel(), factory.getOWLLiteral(specialization.trim(), "en"));
				generalizationClassLabelAxiom = factory.getOWLAnnotationAssertionAxiom(specializationClass.getIRI(), generalizationClassLabel);
				axiomSet.add(generalizationClassLabelAxiom);
				specializationClassLabel = factory.getOWLAnnotation(factory.getRDFSLabel(), factory.getOWLLiteral(generalization.trim(), "en"));
				specializationClassLabelAxiom = factory.getOWLAnnotationAssertionAxiom(generalizationClass.getIRI(), specializationClassLabel);
				axiomSet.add(specializationClassLabelAxiom);
			}
		}
		return axiomSet;
	}
	
	/**
	 * Adds the Subclass-Of axioms from table file to the Core ontology.
	 *
	 * @param tableFileName, the table file name
	 */
	public void addSubClassOfAxiomsFromTables(String tableFileName) {
		String fileLine = null;
		Set<OWLAxiom> axiomSet = new HashSet<OWLAxiom>();
		try {
			File fp = new File(cfg.getFiles_Path() + "\\" + tableFileName);
			FileReader inputFile = new FileReader(fp);
			LineNumberReader lnr = new LineNumberReader(inputFile);
			boolean discardLast = false;
			
			while ((fileLine = lnr.readLine()) != null) {
				String[] parts = fileLine.split(CoreConfiguration.SEPARATOR);
				
				// Header
				if (lnr.getLineNumber() == 2) {	
					// Discard Examples column if exists
					for (int i = 0; i < parts.length; i++) {
						if (StringUtils.containsIgnoreCase(parts[i], "EXAMPLE") && i == parts.length - 1)
							discardLast = true;							
					}
				}
				// Data
				if (lnr.getLineNumber() > 2) {					
					if (fileLine.contains(CoreConfiguration.CARRIAGE_RETURN_SEPARATOR)) {
						String[] lines = fileLine.split("\\" + CoreConfiguration.CARRIAGE_RETURN_SEPARATOR + CoreConfiguration.SEPARATOR);
						for (String line : lines)
							axiomSet.addAll(getSubClassOfAxiomsFromLine(line));
					} else {
						if (discardLast) {
							fileLine = StringUtils.substringBeforeLast(StringUtils.stripEnd(fileLine, CoreConfiguration.SEPARATOR), CoreConfiguration.SEPARATOR) + CoreConfiguration.SEPARATOR; 
							axiomSet.addAll(getSubClassOfAxiomsFromLine(fileLine));
						}
						else 				
							axiomSet.addAll(getSubClassOfAxiomsFromLine(fileLine));	
					}						
				}
			}
			lnr.close();
			manager.addAxioms(this.getOntology(), axiomSet);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (FileNotExistsException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Generate Part-Of relation set.
	 * 
	 * @see http://www.w3.org/2001/sw/BestPractices/OEP/SimplePartWhole
	 */
	// PartOf
	public void generatePartOfRelationSet() {
		OWLDataFactory factory = manager.getOWLDataFactory();
		
		// PartOf Relation
		OWLObjectProperty partOf = factory.getOWLObjectProperty(":partOf", pm);
		OWLAxiom partOf_TransitiveAxiom = factory.getOWLTransitiveObjectPropertyAxiom(partOf);
		manager.applyChange(new AddAxiom(ontology, partOf_TransitiveAxiom));
		
		// Label
		OWLAnnotation lbl = factory.getOWLAnnotation(factory.getRDFSLabel(), factory.getOWLLiteral("partOf", "en"));
		OWLAnnotationAssertionAxiom lblAxiom = factory.getOWLAnnotationAssertionAxiom(partOf.getIRI(), lbl);
		manager.applyChange(new AddAxiom(ontology, lblAxiom));

		// Inverse relation (HasPart)
		OWLObjectProperty hasPart = factory.getOWLObjectProperty(":hasPart", pm);
		OWLInverseObjectPropertiesAxiom inversePartOf = factory.getOWLInverseObjectPropertiesAxiom(partOf, hasPart);
		manager.applyChange(new AddAxiom(ontology, inversePartOf));
		
		// Label
		lbl = factory.getOWLAnnotation(factory.getRDFSLabel(), factory.getOWLLiteral("hasPart", "en"));
		lblAxiom = factory.getOWLAnnotationAssertionAxiom(hasPart.getIRI(), lbl);
		manager.applyChange(new AddAxiom(ontology, lblAxiom));

		// W3C recommendation: In order to avoid problems with the reasoner, define as transitive only one
		OWLAxiom hasPart_TransitiveAxiom = factory.getOWLTransitiveObjectPropertyAxiom(hasPart);
		manager.applyChange(new AddAxiom(ontology, hasPart_TransitiveAxiom));

		// Lower level relations
		// partOf_directly
		OWLObjectProperty partOf_directly = factory.getOWLObjectProperty(":partOf_directly", pm);
		OWLAxiom partOf_directlyAxiom = factory.getOWLSubObjectPropertyOfAxiom(partOf_directly.asOWLObjectProperty(), partOf.asOWLObjectProperty());
		manager.applyChange(new AddAxiom(ontology, partOf_directlyAxiom));
		
		// Label
		lbl = factory.getOWLAnnotation(factory.getRDFSLabel(), factory.getOWLLiteral("partOf directly", "en"));
		lblAxiom = factory.getOWLAnnotationAssertionAxiom(partOf_directly.getIRI(), lbl);
		manager.applyChange(new AddAxiom(ontology, lblAxiom));

		// hasPart_directly
		OWLObjectProperty hasPart_directly = factory.getOWLObjectProperty(":hasPart_directly", pm);
		OWLAxiom hasPart_directlyAxiom = factory.getOWLSubObjectPropertyOfAxiom(hasPart_directly.asOWLObjectProperty(), hasPart.asOWLObjectProperty());
		manager.applyChange(new AddAxiom(ontology, hasPart_directlyAxiom));
		
		// Label
		lbl = factory.getOWLAnnotation(factory.getRDFSLabel(), factory.getOWLLiteral("hasPart directly", "en"));
		lblAxiom = factory.getOWLAnnotationAssertionAxiom(hasPart_directly.getIRI(), lbl);
		manager.applyChange(new AddAxiom(ontology, lblAxiom));
		
		OWLInverseObjectPropertiesAxiom inversePartOf_directly = factory.getOWLInverseObjectPropertiesAxiom(partOf_directly, hasPart_directly);
		manager.applyChange(new AddAxiom(ontology, inversePartOf_directly));
	}
	
	/**
	 * Adds the Part-Of axioms from table file to the Core ontology.
	 *
	 * @param tableFileName, the Part-Of relations table file name
	 */
	public void addPartOfAxiomsFromTable(String tableFileName) {
		OWLDataFactory factory = manager.getOWLDataFactory();
		String fileLine = null;
		List<String[]> relations = new ArrayList<String[]>();
		Set<String> holonyms = new HashSet<String>();
		try {
			File fp = new File(cfg.getFiles_Path() + "\\" + tableFileName);
			FileReader inputFile = new FileReader(fp);
			LineNumberReader lnr = new LineNumberReader(inputFile);
			int meronymCol = -1, holonymCol = -1;
			
			while ((fileLine = lnr.readLine()) != null) {
				String[] parts = fileLine.split(CoreConfiguration.SEPARATOR);			
				// Header
				if (lnr.getLineNumber() == 2) {
					String part = null;				
					// Infer column order (array indexes)
					for (int i = 0; i < parts.length; i++) {
						part = parts[i];
						if (StringUtils.containsIgnoreCase(part, "HOLONYM")) 
							holonymCol = i;
						else if (StringUtils.containsIgnoreCase(part, "MERONYM")) 
							meronymCol = i;
					}
				}		
				// Data
				else if ((lnr.getLineNumber() > 2) && (meronymCol != -1) && (holonymCol != -1)) {
					parts = fileLine.split(CoreConfiguration.SEPARATOR);
					holonyms.add(parts[holonymCol]);
					relations.add(parts);
				}
			}			
			lnr.close();
			
			// Generate basic meronymy relations (partOf/hasPart):
			generatePartOfRelationSet();

			// Classify the relation by its holonym
			int holonymsSize = holonyms.size();
			List<String> holonymsAL = new ArrayList<String>(holonyms);
			List<List<String>> classifier = new ArrayList<List<String>>(holonymsSize);
			for (int i = 0; i < holonymsSize; i++) {
				ArrayList<String> innerArrayList = new ArrayList<String>();
				classifier.add(innerArrayList);
			}
			for (String[] relation : relations)
				classifier.get(holonymsAL.indexOf(relation[holonymCol])).add(relation[meronymCol]);

			// hasPart:
			// Process 'allValuesFrom' restrictions for each holonym
			Pattern pattern = Pattern.compile("([\\s*\\w+\\-\\/]+)\\(([\\w+\\.\\-\\/\\s*]+)\\)");
			for (String holonym : holonymsAL) {
				List<String> meronymLiterals = classifier.get(holonymsAL.indexOf(holonym));
				Set<OWLClass> meronymsAllValuesFrom = new HashSet<OWLClass>();
				for (String meronym : meronymLiterals) {
					Matcher matcherSource = pattern.matcher(meronym);
					if (matcherSource.matches()) 
						meronym = matcherSource.group(1).replace("/", "-").trim();
					OWLClass meronymClass = factory.getOWLClass(":" + meronym.replaceAll(" ", ""), pm);
					// If the concept wasn't previously defined (Import), define labels
					if (!ontology.containsClassInSignature(pm.getIRI(meronym.replace(" ", "")))) {
						// Label
						OWLAnnotation lblNewClass = factory.getOWLAnnotation(factory.getRDFSLabel(), factory.getOWLLiteral(meronym, "en"));
						manager.applyChange(new AddAxiom(ontology, factory.getOWLAnnotationAssertionAxiom(meronymClass.getIRI(), lblNewClass)));
					}	
					meronymsAllValuesFrom.add(meronymClass);
				}
				// Closure axiom
				OWLObjectIntersectionOf intersectionOf = null;
				OWLQuantifiedObjectRestriction aRestriction = null;
				OWLObjectProperty hasPart = getOWLObjectPropertyFromName("hasPart");
				if (meronymsAllValuesFrom.size() > 1) {
					intersectionOf = factory.getOWLObjectIntersectionOf(meronymsAllValuesFrom);
					aRestriction = factory.getOWLObjectAllValuesFrom(hasPart, intersectionOf);
				} else if (meronymsAllValuesFrom.size() == 1)
					aRestriction = factory.getOWLObjectAllValuesFrom(hasPart, meronymsAllValuesFrom.iterator().next());
				
				// Meronym
				Matcher matcherTarget = pattern.matcher(holonym);
				if (matcherTarget.matches())
					holonym = matcherTarget.group(1).replace("/", "-").trim();				
				OWLClass claseHolonym = getOWLClassFromName(holonym);
				// If the concept wasn't previously defined (Import), define labels
				if (!ontology.containsClassInSignature(pm.getIRI(holonym.replace(" ", "")))) {
					// Label
					OWLAnnotation lblNewClass = factory.getOWLAnnotation(factory.getRDFSLabel(), factory.getOWLLiteral(holonym, "en"));
					manager.applyChange(new AddAxiom(ontology, factory.getOWLAnnotationAssertionAxiom(claseHolonym.getIRI(), lblNewClass)));
				}				
				manager.applyChange(new AddAxiom(ontology, factory.getOWLSubClassOfAxiom(claseHolonym, aRestriction)));
			}
//			 	// PartOf: (to be asserted for the Individuals)
//			 	// Process 'someValuesFrom' restrictions for each holonym
//				OWLObjectSomeValuesFrom sRestriction = null;
//				for (String[] relation : relations) {
//				String holonym = relation[holonymCol];
//				String meronym = relation[meronymCol];
//				
//				Matcher matcherSource = pattern.matcher(meronym); 
//				if (matcherSource.matches())
//				meronym = matcherSource.group(1).replace("/", "-");
//				
//				OWLClass claseMeronym = factory.getOWLClass(":"+holonym.replaceAll(" ", ""), pm);	
//				Matcher matcherTarget = pattern.matcher(holonym);
//				if (matcherTarget.matches())
//				holonym = matcherTarget.group(1).replace("/", "-");
//				
//				OWLClass claseHolonym = factory.getOWLClass(":"+holonym.replaceAll(" ", ""), pm);
//				OWLObjectProperty partOf_directly = getOWLObjectPropertyFromName("partOf_directly");
//				sRestriction = factory.getOWLObjectSomeValuesFrom(partOf_directly, claseHolonym);
//				manager.applyChange(new AddAxiom(ontology, factory.getOWLSubClassOfAxiom(claseMeronym, sRestriction)));
//			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Disjoint Decompositions
	/**
	 * Adds the Class Disjoint-Decompositions from table file to the Core ontology.
	 *
	 * @param tableFileName, the Disjoint-Decompositions table file name
	 * @see http://www.w3.org/TR/owl-ref/#disjointWith-def
	 */ 
	public void addDisjointDecompositionsFromTable(String tableFileName) {
		OWLDataFactory factory = this.getManager().getOWLDataFactory();
		String fileLine = null;
		Set<OWLClass> components = new HashSet<OWLClass>();
		try {
			File fp = new File(this.getCfg().getFiles_Path() + "\\" + tableFileName);
			FileReader inputFile = new FileReader(fp);
			LineNumberReader lnr = new LineNumberReader(inputFile);
			int targetCol = 0, componentsCol = 1;	
			String target = null, targetRef = null;
			boolean firstRef = true;
			
			// Preprocessing: Shuffle & Sort
			List <Pair<String, String>> decompositions = new ArrayList <Pair<String, String>>();
			while((fileLine = lnr.readLine()) != null) {		
				// Data	
				if (lnr.getLineNumber() > 2) {		
					if (fileLine.contains("$CRLF")) {
						String[] lines = fileLine.split("\\$CRLF~~");
						
						for (String line : lines) {
							String[] parts = line.split(CoreConfiguration.SEPARATOR);
							decompositions.add(new Pair<String, String>(parts[targetCol], parts[componentsCol]));
						}
					}
				}
			}
			lnr.close();
			
			// Sort decompositions by target
			Collections.sort(decompositions, Comparator.comparing(p -> p.first()));		

			// Process the decompositions list
			for (Pair<String, String> decomposition : decompositions) {
				String disjointComponent = decomposition.second();
				target = decomposition.first();				
				if (firstRef) { 
					targetRef = target;
					firstRef = false;
				}				
				// Same group
				if (target.compareTo(targetRef) == 0) {
					OWLClass disjointClass = this.getOWLClassFromName(disjointComponent);
					components.add(disjointClass);
				}
				// Different group
				else{
					OWLDisjointClassesAxiom disjointAxiom = factory.getOWLDisjointClassesAxiom(components);
					this.getManager().applyChange(new AddAxiom(this.getOntology(), disjointAxiom));						
					OWLObjectUnionOf unionOf = factory.getOWLObjectUnionOf(components);
					OWLClass targetClass = getOWLClassFromName(targetRef);
					OWLEquivalentClassesAxiom equivalentClasses = factory.getOWLEquivalentClassesAxiom(targetClass, unionOf);
					this.getManager().applyChange(new AddAxiom(this.getOntology(), equivalentClasses));
					
					// SubclassOf
					for (OWLClass component : components) {
						OWLSubClassOfAxiom subClassOf = factory.getOWLSubClassOfAxiom(component, targetClass);
						this.getManager().applyChange(new AddAxiom(this.getOntology(), subClassOf));
					}							
					components.clear();				
					targetRef = target;					
					OWLClass disjointClass = this.getOWLClassFromName(disjointComponent);
					components.add(disjointClass);
				}	
			}
			// Disjointness
			OWLDisjointClassesAxiom disjointAxiom = factory.getOWLDisjointClassesAxiom(components);
			this.getManager().applyChange(new AddAxiom(this.getOntology(), disjointAxiom));
			
			// Union & Equivalence
			OWLObjectUnionOf unionOf = factory.getOWLObjectUnionOf(components);
			OWLClass targetClass = this.getOWLClassFromName(target);
			OWLEquivalentClassesAxiom equivalentClasses = factory.getOWLEquivalentClassesAxiom(targetClass, unionOf);
			this.getManager().applyChange(new AddAxiom(this.getOntology(), equivalentClasses));

			// SubclassOf					
			for (OWLClass component : components) {
				OWLSubClassOfAxiom subClassOf = factory.getOWLSubClassOfAxiom(component, targetClass);
				this.getManager().applyChange(new AddAxiom(this.getOntology(), subClassOf));
			}
			components.clear();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Adds the exhaustive decompositions from table file to the Core ontology.
	 *
	 * @param tableFileName, the Exhaustive-Decompositions table file name
	 */
	// Exhaustive Decompositions
	public void addExhaustiveDecompositionsFromTable(String tableFileName) {
		OWLDataFactory factory = this.getManager().getOWLDataFactory();
		String fileLine = null;
		Set<OWLClass> components = new HashSet<OWLClass>();		
		try {
			File fp = new File(this.getCfg().getFiles_Path() + "\\" + tableFileName);
			FileReader inputFile = new FileReader(fp);
			LineNumberReader lnr = new LineNumberReader(inputFile);
			int targetCol = 0, componentsCol = 1;	
			String target = null, targetRef = null, component = null;
			OWLClass targetClass = null, unionClass = null; 
			boolean firstRef = true;
			
			// Preprocessing: Shuffle & Sort
			List <Pair<String, String>> decompositions = new ArrayList <Pair<String, String>>();
			while((fileLine = lnr.readLine()) != null) {		
				// Data	
				if (lnr.getLineNumber() > 2) {		
					if (fileLine.contains("$CRLF")) {
						String[] lines = fileLine.split("\\$CRLF~~");
						
						for (String line : lines) {
							String[] parts = line.split(CoreConfiguration.SEPARATOR);
							decompositions.add(new Pair<String, String>(parts[targetCol], parts[componentsCol]));
						}
					}
				}
			}
			lnr.close();
			
			// Sort decompositions by target
			Collections.sort(decompositions, Comparator.comparing(p -> p.first()));		

			// Process the decompositions list
			for (Pair<String, String> decomposition : decompositions) {
				component = decomposition.second();
				target = decomposition.first();	
				if (firstRef) {
					targetRef = target;
					firstRef = false;
					targetClass = getOWLClassFromName(target);
				}				
				// Same group
				if (target.compareTo(targetRef) == 0) {
					unionClass = this.getOWLClassFromName(component);
					components.add(unionClass);
				}			
				// Different group
				else{
					///owl:equivalentClass(owl:unionOf)
					OWLObjectUnionOf unionOf = factory.getOWLObjectUnionOf(components);
					OWLEquivalentClassesAxiom equivalentClasses = factory.getOWLEquivalentClassesAxiom(targetClass, unionOf);
					this.getManager().applyChange(new AddAxiom(this.getOntology(), equivalentClasses));		
					components.clear();
					targetRef = target;				
					targetClass = this.getOWLClassFromName(target);
					unionClass = this.getOWLClassFromName(component);
					components.add(unionClass);
				}	
			}
			OWLObjectUnionOf unionOf = factory.getOWLObjectUnionOf(components);
			OWLEquivalentClassesAxiom equivalentClasses = factory.getOWLEquivalentClassesAxiom(targetClass, unionOf);
			this.getManager().applyChange(new AddAxiom(this.getOntology(), equivalentClasses));		
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	

	
	// DOMAINS & RANGES
	/**
	 * Gets the domain classes for the given Attribute (DataProperty) or AdHoc Relation (Object Property) from the Attributes or AdHoc Relations table file.
	 *
	 * @param tableFileName, Attributes or AdHoc relations table file name
	 * @param propertyName, the Attribute (DataProperty) or AdHoc Relation (Object Property) name
	 * @return The domain classes set
	 */
	// Attribute (DataProperty) | AdHoc Relation (Object Property) Domain
	public Set<OWLClass> getDomainClassesFromTable(String tableFileName, String propertyName) {
		OWLDataFactory factory = manager.getOWLDataFactory();
		String fileLine = null;
		Set<OWLClass> domains = new HashSet<OWLClass>();
		try {		
			File fp = new File(cfg.getFiles_Path() + "\\" + tableFileName);
			FileReader inputFile = new FileReader(fp);
			LineNumberReader lnr = new LineNumberReader(inputFile);
			int conceptsCol = -1;
			
			while ((fileLine = lnr.readLine()) != null) {
				String[] parts = fileLine.split(CoreConfiguration.SEPARATOR);
				// Header
				if (lnr.getLineNumber() == 2) {
					// Infer column order (array indexes)
					for (int i = 0; i < parts.length; i++) {
						if (StringUtils.containsIgnoreCase(parts[i], "CONCEPT")) {
							// For the AdHoc relation tables, there are 2 column names containing the literal 'CONCEPT': 
							// SURCE CONCEPT and TARGET CONCEPT, discard TARGET CONCEPT 
							if (StringUtils.containsIgnoreCase(parts[i], "TARGET"))
								continue;
							conceptsCol = i;
							break;
						}
					}
				}
				// Data
				if ((lnr.getLineNumber() > 2) && (conceptsCol != -1)) {
					if (fileLine.contains(propertyName)) {
						Pattern pattern = Pattern.compile("([\\s*\\w+\\-\\/]+)\\(([\\w+\\,\\-\\/\\s*]+)\\)");
						String domain = parts[conceptsCol].trim().replace("/", "-");
						Matcher matcher = pattern.matcher(domain);

						// Keep only the name
						if (matcher.matches())
							domain = matcher.group(1);
						OWLClass domainClass = getOWLClassFromName(domain);
						
						// If the concept wasn't previously defined (Import), define labels
						if (!ontology.containsClassInSignature(pm.getIRI(domain.replace(" ", "")))) {
							// Label
							OWLAnnotation lblNewClass = factory.getOWLAnnotation(factory.getRDFSLabel(), factory.getOWLLiteral(domain, "en"));
							manager.applyChange(new AddAxiom(ontology, factory.getOWLAnnotationAssertionAxiom(domainClass.getIRI(), lblNewClass)));
						}	
						domains.add(domainClass);
					}
				}
			}
			lnr.close();
			return domains;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Gets the range classes for the given AdHoc Relation (Object Property) from the AdHoc Relations table file.
	 *
	 * @param tableFileName, the AdHoc Relations table file name
	 * @param propertyName, the AdHoc Relation (Object Property) name
	 * @return The range classes set
	 */
	// AdHoc Relation (Object Property) Range
	public Set<OWLClass> getRangeClassesFromTable(String tableFileName, String propertyName) {
		OWLDataFactory factory = manager.getOWLDataFactory();
		String fileLine = null;
		Set<OWLClass> ranges = new HashSet<OWLClass>();		
		try {
			File fp = new File(cfg.getFiles_Path() + "\\" + tableFileName);
			FileReader inputFile = new FileReader(fp);
			LineNumberReader lnr = new LineNumberReader(inputFile);
			int conceptsCol = -1;

			while ((fileLine = lnr.readLine()) != null) {
				String[] parts = fileLine.split(CoreConfiguration.SEPARATOR);		
				// Header
				if (lnr.getLineNumber() == 2) {
					// Infer column order (array indexes)
					for (int i = 0; i < parts.length; i++) {
						if (StringUtils.containsIgnoreCase(parts[i], "TARGET")) {
							conceptsCol = i;
							break;
						}
					}
				}
				// Data
				if ((lnr.getLineNumber() > 2) && (conceptsCol != -1)) {
					if (fileLine.contains(propertyName)) {
						Pattern pattern = Pattern.compile("([\\s*\\w+\\-\\/]+)\\(([\\w+\\,\\-\\/\\s*]+)\\)");					
						String range = parts[conceptsCol].trim().replace("/", "-");
						Matcher matcher = pattern.matcher(range);

						// Keep only the name
						if (matcher.matches())
							range = matcher.group(1);
						OWLClass rangeClass = getOWLClassFromName(range);
						
						// If the concept wasn't previously defined (Import), define labels
						if (!ontology.containsClassInSignature(pm.getIRI(range.replace(" ", "")))) {
							// Label
							OWLAnnotation lblNewClass = factory.getOWLAnnotation(factory.getRDFSLabel(), factory.getOWLLiteral(range, "en"));
							manager.applyChange(new AddAxiom(ontology, factory.getOWLAnnotationAssertionAxiom(rangeClass.getIRI(), lblNewClass)));
						}			
						ranges.add(rangeClass);
					}
				}
			}
			lnr.close();
			return ranges;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}	

	/**
	 * Gets the range axioms for the given Attribute (DataProperty) from the Attributes table file.
	 *
	 * @param tableFileName, the Attributes table file name
	 * @param propertyName, the Attribute (DataProperty) name
	 * @return The range classes set
	 */	
	// Data Property Values (DataTypes)
	public Set<OWLDataPropertyRangeAxiom> getDataRangeAxiomsFromTable(String tableFileName, OWLDataProperty dataProperty) {
		OWLDataFactory factory = manager.getOWLDataFactory();
		String fileLine = null;
		Set<OWLDataPropertyRangeAxiom> rangeAxioms = new HashSet<OWLDataPropertyRangeAxiom>();	
		try {
			File fp = new File(cfg.getFiles_Path() + "\\" + tableFileName);
			FileReader inputFile = new FileReader(fp);
			LineNumberReader lnr = new LineNumberReader(inputFile);
			int valuesCol = -1;	

			while ((fileLine = lnr.readLine()) != null) {
				String[] parts = fileLine.split(CoreConfiguration.SEPARATOR);		
				// Header
				if (lnr.getLineNumber() == 2) {		
					// Infer column order (array indexes)
					for (int i = 0; i < parts.length; i++) {
						if (StringUtils.containsIgnoreCase(parts[i], "VALUE TYPE") || 
							StringUtils.containsIgnoreCase(parts[i], "VALUES") ||
							StringUtils.containsIgnoreCase(parts[i], "COMPUTATIONAL TYPE") ) {
							valuesCol = i;
							break;
						}
					}
				}
				// Data
				if ((lnr.getLineNumber() > 2) && (valuesCol != -1)) {
					OWLDataPropertyRangeAxiom rangeAxiom = null;
					String propName = WordUtils.capitalize(dataProperty.getIRI().getShortForm());
					if (fileLine.contains(propName) || fileLine.contains(dataProperty.getIRI().getShortForm()) ) {
						HashSet<OWLLiteral> valuesSet = new HashSet<OWLLiteral>();						
						Pattern pattern = Pattern.compile("\\{([\\w+\\-\\/[\\,\\s]*]+)\\}");							
						boolean isBooleanExtension = false;							
						OWLDatatype dataType = null;
						
						String valueType = parts[valuesCol];
						switch (valueType.toLowerCase()) {
							case ("boolean"):
								dataType = factory.getBooleanOWLDatatype();
								break;
	
							case ("cardinal"):
								dataType = factory.getIntegerOWLDatatype();
								break;
	
							case ("string"):
								dataType = factory.getOWLDatatype(XSDVocabulary.parseShortName("xsd:string").getIRI());
								break;
	
							default:
								Matcher matcher = pattern.matcher(valueType);
								if (matcher.matches()) {
									String values = matcher.group(1).replaceAll(", ", ",");
									String[] rangeValues = values.split("\\,");																			

									// Boolean Extension
									List<String> rangeValuesList = Arrays.asList(rangeValues);	
									rangeValuesList = new ArrayList<String>(rangeValuesList);	
									if (rangeValuesList.contains("TRUE") || rangeValuesList.contains("FALSE") ||
									    rangeValuesList.contains("true") || rangeValuesList.contains("false")) {
										
										rangeValuesList.removeIf(p -> p.equalsIgnoreCase("TRUE") || p.equalsIgnoreCase("FALSE"));
										rangeValues= rangeValuesList.toArray(new String[rangeValuesList.size()]);
										isBooleanExtension = true;
									}				
									for (String value : rangeValues) 
										valuesSet.add(factory.getOWLLiteral(value.replace(" ", "_")));								
								}
						}		
						if (valuesSet.size() != 0) {
							OWLDataOneOf valuesRange = factory.getOWLDataOneOf(valuesSet);	
							rangeAxiom = factory.getOWLDataPropertyRangeAxiom(dataProperty, valuesRange);
							valuesSet.clear();	
							if (isBooleanExtension) {
								OWLDataUnionOf unionOf = factory.getOWLDataUnionOf(rangeAxiom.getRange(), factory.getBooleanOWLDatatype());
								rangeAxiom = factory.getOWLDataPropertyRangeAxiom(dataProperty, unionOf);
							}
						} 
						else 
							rangeAxiom = factory.getOWLDataPropertyRangeAxiom(dataProperty, dataType);						
						rangeAxioms.add(rangeAxiom);
					}							
				}
			}	
			lnr.close();
			return rangeAxioms;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	 }

	/**
	 * Adds the Attributes (Data Properties) from table file to the Core ontology.
	 *
	 * @param tableFileName, the Attributes (Data Properties) table file name
	 */
	// DATA PROPERTIES
	public void addAttributesFromTable(String tableFileName) {
		OWLDataFactory factory = manager.getOWLDataFactory();
		String fileLine = null;
		Set<OWLAxiom> axiomSet = new HashSet<OWLAxiom>();	
		try {
			File fp = new File(cfg.getFiles_Path() + "\\" + tableFileName);
			FileReader inputFile = new FileReader(fp);
			LineNumberReader lnr = new LineNumberReader(inputFile);
			List<String> header = null, processedAttributes = new ArrayList<String>();

			while ((fileLine = lnr.readLine()) != null) {
				String[] parts = fileLine.split(CoreConfiguration.SEPARATOR);			
				// Header
				if (lnr.getLineNumber() == 2) {
					header = new ArrayList<String>();			
					for (String part : parts) {
						header.add(part);
					}
				}
				// Data
				else if (lnr.getLineNumber() > 2) {
					// DataProperty
					OWLDataProperty dataProperty = null;
					String attributeName = null;
					if (header.contains("ATTRIBUTE")) {
						attributeName = parts[header.indexOf("ATTRIBUTE")];
						dataProperty = getOWLDataPropertyFromName(attributeName);
						
						// Label
						OWLAnnotation lbl = factory.getOWLAnnotation(factory.getRDFSLabel(), factory.getOWLLiteral(attributeName.trim().replace("_", " "), "en"));
						OWLAnnotationAssertionAxiom lblAxiom = factory.getOWLAnnotationAssertionAxiom(dataProperty.getIRI(), lbl);
						axiomSet.add(lblAxiom);	
					}
					// Domain
					if (header.contains("CONCEPT")) {
						Set<OWLClass> domains = getDomainClassesFromTable(tableFileName, attributeName);
						OWLDataPropertyDomainAxiom domainAxiom = null;
						if (domains.size() > 1) {
							OWLObjectUnionOf unionOf = factory.getOWLObjectUnionOf(domains);
							domainAxiom = factory.getOWLDataPropertyDomainAxiom(dataProperty, unionOf);
						} else 
							domainAxiom = factory.getOWLDataPropertyDomainAxiom(dataProperty, domains.iterator().next());				
						axiomSet.add(domainAxiom);
					}
					// Range
					if (header.contains("VALUE TYPE") || header.contains("VALUES") || header.contains("COMPUTATIONAL TYPE")) {
						Set<OWLDataPropertyRangeAxiom> rangeAxioms = getDataRangeAxiomsFromTable(tableFileName, dataProperty);
						if (rangeAxioms.size() > 1) {
							OWLDataPropertyRangeAxiom rangeAxiom = null;
							Set <OWLDataRange> rangesIn = new HashSet<OWLDataRange>();		
							for (OWLDataPropertyRangeAxiom rangeAxiom_ : rangeAxioms) {
								rangesIn.add(rangeAxiom_.getRange());
							}		
							OWLDataUnionOf unionOf = factory.getOWLDataUnionOf(rangesIn);
							rangeAxiom = factory.getOWLDataPropertyRangeAxiom(dataProperty, unionOf);
							axiomSet.add(rangeAxiom);
						} else
							axiomSet.add(rangeAxioms.iterator().next());		
					}
					// Description
					if (header.contains("DESCRIPTION")) {
						String description = parts[header.indexOf("DESCRIPTION")];
						
						if (!description.equals(CoreConfiguration.NOTHING)) {
							OWLAnnotation commentName = factory.getOWLAnnotation(factory.getRDFSComment(), factory.getOWLLiteral(description, "en"));
							OWLAnnotationAssertionAxiom axiom = factory.getOWLAnnotationAssertionAxiom(dataProperty.getIRI() , commentName);
							manager.applyChange(new AddAxiom(ontology, axiom));
						}
					}					
					// Prune iterations
					processedAttributes.add(attributeName);
				}
			}
			lnr.close();
			manager.addAxioms(this.getOntology(), axiomSet);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Adds the Ad-Hoc Relations (Object Properties) from table file to the Core ontology.
	 *
	 * @param tableFileName, the Ad-Hoc Relations (Object Properties) table file name
	 */
	// OBJECT PROPERTIES
	public void addAdHocRelationsFromTable(String tableFileName) {
		OWLDataFactory factory = manager.getOWLDataFactory();
		String fileLine = null;	
		try {
			File fp = new File(cfg.getFiles_Path() + "\\" + tableFileName);
			FileReader inputFile = new FileReader(fp);
			LineNumberReader lnr = new LineNumberReader(inputFile);
			int sourceCol = -1, relationCol = -1, targetCol = -1, inverseCol = -1;
			
			while ((fileLine = lnr.readLine()) != null) {
				String[] parts = fileLine.split(CoreConfiguration.SEPARATOR);				
				// Header
				if (lnr.getLineNumber() == 2) {
					// Infer column order (array indexes)
					for (int i = 0; i < parts.length; i++) {
						if (StringUtils.containsIgnoreCase(parts[i], "SOURCE"))
							sourceCol = i;
						else if (StringUtils.containsIgnoreCase(parts[i], "RELATION"))
							relationCol = i;
						else if (StringUtils.containsIgnoreCase(parts[i], "TARGET"))
							targetCol = i;
						else if (StringUtils.containsIgnoreCase(parts[i], "INVERSE"))
							inverseCol = i;							
					}
				}			
				// Data
				if ((lnr.getLineNumber() > 2) && (sourceCol != -1) && (relationCol != -1) && (targetCol != -1)) {
					parts = fileLine.split(CoreConfiguration.SEPARATOR);
					// Relation
					String relation = parts[relationCol];
					OWLObjectProperty objectProp = getOWLObjectPropertyFromName(relation);				
					// Label
					OWLAnnotation lbl = factory.getOWLAnnotation(factory.getRDFSLabel(), factory.getOWLLiteral(relation.trim().replace("_", " "), "en"));
					OWLAnnotationAssertionAxiom lblAxiom = factory.getOWLAnnotationAssertionAxiom(objectProp.getIRI(), lbl);
					manager.applyChange(new AddAxiom(ontology, lblAxiom));

					// Domain
					Set<OWLClass> domains = getDomainClassesFromTable(tableFileName, relation);
					OWLObjectPropertyDomainAxiom domainAxiom = null;
					if (domains.size() > 1) {
						OWLObjectUnionOf unionOf = factory.getOWLObjectUnionOf(domains);
						domainAxiom = factory.getOWLObjectPropertyDomainAxiom(objectProp, unionOf);
					} else 
						domainAxiom = factory.getOWLObjectPropertyDomainAxiom(objectProp, domains.iterator().next());
					manager.applyChange(new AddAxiom(ontology, domainAxiom));

					// Range
					Set<OWLClass> ranges = getRangeClassesFromTable(tableFileName, relation);
					OWLObjectPropertyRangeAxiom rangeAxiom = null;
					if (ranges.size() > 1) {
						OWLObjectUnionOf unionOf = factory.getOWLObjectUnionOf(ranges);
						rangeAxiom = factory.getOWLObjectPropertyRangeAxiom(objectProp, unionOf);
					} else 
						rangeAxiom = factory.getOWLObjectPropertyRangeAxiom(objectProp, ranges.iterator().next());
					manager.applyChange(new AddAxiom(ontology, rangeAxiom));					
				}
			}
			lnr.close();
			
			// If no inverse column exists, try to infer them (Only trivial cases)
			if (inverseCol == -1){
				InferInverseRelations generator = new InferInverseRelations();
				generator.setOntology(ontology);
				generator.inferInverseAdHocs(this);
			}  
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Adds the attribute rules (Data Property Value restrictions) from table file to the Core ontology.
	 *
	 * @param tableFileName, the rules table file name
	 */
	// RULES (Data Property Value restrictions)
	public void addAttributeRulesFromTable(String tableFileName) {
		OWLDataFactory factory = manager.getOWLDataFactory();
		String fileLine = null;	
		try {
			File fp = new File(cfg.getFiles_Path() + "\\" + tableFileName);
			FileReader inputFile = new FileReader(fp);
			LineNumberReader lnr = new LineNumberReader(inputFile);
			int sourceCol = -1, attributeCol = -1, targetCol = -1;
			
			while ((fileLine = lnr.readLine()) != null) {
				String[] parts = fileLine.split(CoreConfiguration.SEPARATOR);
				
				// Header
				if (lnr.getLineNumber() == 2) {
					// Infer column order (array indexes)
					for (int i = 0; i < parts.length; i++) {
						if (StringUtils.containsIgnoreCase(parts[i], "CONCEPT"))
							sourceCol = i;
						else if (StringUtils.containsIgnoreCase(parts[i], "ATTRIBUTE"))
							attributeCol = i;
						else if (StringUtils.containsIgnoreCase(parts[i], "VALUE") &&
								!StringUtils.containsIgnoreCase(parts[i], "TYPE"))
							targetCol = i;
					}
				}			
				// Data
				if ((lnr.getLineNumber() > 2) && (sourceCol != -1) && (attributeCol != -1) && (targetCol != -1)) {
					// Regex to skip what is in brackets for the Domain
					Pattern patternDomain = Pattern.compile("([\\s*\\w+\\-\\/]+)\\(([\\w+\\,\\/\\s*]+)\\)");

					// Attribute
					String attribute = parts[attributeCol].trim();
					OWLDataProperty dataProperty = getOWLDataPropertyFromName(attribute);

					// Domain
					String domain = parts[sourceCol].trim().replace("/", "-");
					Matcher matcherSource = patternDomain.matcher(domain);
					if (matcherSource.matches())
						domain = matcherSource.group(1);
					OWLClass domainClass = getOWLClassFromName(domain);

					// Value
					OWLRestriction hasValue = null;
					OWLSubClassOfAxiom attributeValueRestrictionAxiom = null;
					String value = parts[targetCol].trim().replace("/", "-");
					switch (value.toLowerCase()) {
						case "true":
							hasValue = factory.getOWLDataHasValue(dataProperty, factory.getOWLLiteral(true));
							break;
							
						case "false":
							hasValue = factory.getOWLDataHasValue(dataProperty, factory.getOWLLiteral(false));
							break;
	
						default:						
							Set<OWLLiteral> valuesSet = new HashSet<OWLLiteral>();						
							Pattern patternValues = Pattern.compile("\\{([\\w+\\-\\/[\\,\\s\\/]*]+)\\}");								
							Matcher matcherValues = patternValues.matcher(value);
							
							// Multiple values
							if (matcherValues.matches() || value.contains(CoreConfiguration.INNER_SEPARATOR)) {
								String valuesString = null;
								String[] values = null;
								OWLDataOneOf valuesRange = null;

								if(value.contains(CoreConfiguration.INNER_SEPARATOR))
									values = value.split(CoreConfiguration.INNER_SEPARATOR);
								else {
									valuesString = matcherValues.group(1).replaceAll(", ", ",");
									values = valuesString.split("\\,");
								}	
								for (String value_ : values) 
									valuesSet.add(factory.getOWLLiteral(value_.replace(" ", "_")));			
								if (valuesSet.size() != 0) {
									if (valuesSet.size() > 1) {
										valuesRange = factory.getOWLDataOneOf(valuesSet);	
										hasValue = factory.getOWLDataExactCardinality(valuesSet.size(), dataProperty, valuesRange); 
									} 
									// If by mistake it specified a set with one element
									else 
										hasValue = factory.getOWLDataHasValue(dataProperty, valuesSet.iterator().next());
									valuesSet.clear();								
								}
							}
							// One value
							else
								hasValue = factory.getOWLDataHasValue(dataProperty, factory.getOWLLiteral(value.replace(" ", "_")));
					}
					if (hasValue != null)
						attributeValueRestrictionAxiom = factory.getOWLSubClassOfAxiom(domainClass, hasValue);
					manager.applyChange(new AddAxiom(ontology, attributeValueRestrictionAxiom));
				}
			}
			lnr.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Adds the class individuals from table file to the Core ontology.
	 *
	 * @param tableFileName, the Class Individuals table file name
	 */
	// INDIVIDUALS
	public void addClassIndividualsFromTable(String tableFileName) {
		OWLDataFactory factory = manager.getOWLDataFactory();
		String fileLine = null;		
		try {	
			File fp = new File(cfg.getFiles_Path() + "\\" + tableFileName);
			FileReader inputFile = new FileReader(fp);
			LineNumberReader lnr = new LineNumberReader(inputFile);
			int classCol = -1, individualCol = -1, descriptionCol = -1;
			
			while ((fileLine = lnr.readLine()) != null) {
				String[] parts = fileLine.split(CoreConfiguration.SEPARATOR);
				
				// Header
				if (lnr.getLineNumber() == 2) {
					// Infer column order (array indexes)
					for (int i = 0; i < parts.length; i++) {
						if (StringUtils.containsIgnoreCase(parts[i], "CONCEPTS") ||
							StringUtils.containsIgnoreCase(parts[i], "CONCEPT NAME")	)
							classCol = i;
						else if (StringUtils.containsIgnoreCase(parts[i], "INSTANCES") ||
								 StringUtils.containsIgnoreCase(parts[i], "INSTANCE NAME") )
							individualCol = i;
						else if (StringUtils.containsIgnoreCase(parts[i], "DESCRIPTION"))
							descriptionCol = i;
					}
				}
				// Data
				if ((lnr.getLineNumber() > 2) && (classCol != -1) && (individualCol != -1)) {
					// Class (Concept)
					String className = parts[classCol].trim();
					Pattern patternClass = Pattern.compile("([\\s*\\w+\\-\\/]+)\\%{0,1}\\s*\\(([\\w+\\-\\/{0,1}\\s*]+)\\)");
					Matcher matcherConceptClass = patternClass.matcher(className.trim());
					if (matcherConceptClass.matches())
						className = matcherConceptClass.group(1).trim();
					OWLClass conceptClass = getOWLClassFromName(className);

					// Individual (Instance)
					String instanceLbl = parts[individualCol].trim(), instanceName = null;
					OWLIndividual conceptInstance = null;

					// Synonyms in brackets
					Pattern patternInstance = Pattern.compile("([\\s*\\w+\\-{{0,1}}]+)\\(([\\w+\\-{0,1}\\s*]+)\\)");
					Matcher matcherConceptInstance = patternInstance.matcher(instanceLbl);
					if (matcherConceptInstance.matches()) {
						String detail = matcherConceptInstance.group(2);
						if (detail.equalsIgnoreCase("open") || detail.equalsIgnoreCase("close")) {
							instanceName = matcherConceptInstance.group(1) + "_" + detail;
						}
						else {
							instanceName = matcherConceptInstance.group(1);
							instanceLbl = instanceName;
						}
					} else
						instanceName = instanceLbl;
					conceptInstance = getOWLIndividualFromName(instanceName);				
					OWLAxiom axiom = factory.getOWLClassAssertionAxiom(conceptClass, conceptInstance);
					manager.applyChange(new AddAxiom(ontology, axiom));

					// Subscript special characters
					if (instanceLbl.contains("_csub"))
						instanceLbl = instanceLbl.replace("_csub", "(") + ")";
					
					// Label
					OWLAnnotation labelName = factory.getOWLAnnotation(factory.getRDFSLabel() , factory.getOWLLiteral(instanceLbl, "en"));
					axiom = factory.getOWLAnnotationAssertionAxiom(conceptInstance.asOWLNamedIndividual().getIRI(), labelName);
					manager.applyChange(new AddAxiom(ontology, axiom));
					
					// Description
					if (descriptionCol != -1) {
						String description = parts[descriptionCol];						
						if (!description.equals(CoreConfiguration.NOTHING)) {
							OWLAnnotation commentName = factory.getOWLAnnotation(factory.getRDFSComment(), factory.getOWLLiteral(description, "en"));
							axiom = factory.getOWLAnnotationAssertionAxiom(conceptInstance.asOWLNamedIndividual().getIRI() , commentName);
							manager.applyChange(new AddAxiom(ontology, axiom));
						}
					}
				}
			}
			lnr.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
