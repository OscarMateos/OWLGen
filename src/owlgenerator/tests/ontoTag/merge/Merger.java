package owlgenerator.tests.ontoTag.merge;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.SetOntologyID;
import org.xadisk.filesystem.exceptions.FileNotExistsException;

import com.google.common.base.Optional;

import owlgenerator.java.merger.OntologyMerger;
import owlgenerator.java.merger.OntologyEquivalences;
import owlgenerator.java.util.Util;

public class Merger {
/////////////////Merger - OntoLing//////////////////
	public static void main(String[] args) throws FileNotExistsException {
		// OWL VALIDATOR
		// http://mowl-power.cs.man.ac.uk:8080/validator/validate
		String location = "C:\\outaspace\\Out\\Merge\\";
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory();
		
		//!\\ ORDER OF OPERATIONS MATTERS
		try {
			// INPUT ONTOLOGIES
			OWLOntology sourceAttributeOntology = null, sourceIntegrationOntology = null, sourceLevelOntology = null, 
					sourceRelationsOntology = null, sourceUnitOntology = null, sourceValueOntology = null;
			
			// OUTPUT ONTOLOGY
			String baseUrl = "http://localhost/";
			String ontologyName = "Merged - OntoLing";
			
			// Ontology IRI
			IRI targetOntologyIRI = IRI.create(baseUrl + ontologyName.replace(" ", "_"));		
			OWLOntology targetOntology = manager.createOntology(targetOntologyIRI);

			// Ontology versionIRI
			IRI versionIRI = IRI.create(targetOntologyIRI + "/" + Util.getCurrentTimeStamp());
			Optional<IRI> optTargetOntologyIRI = Optional.of(targetOntologyIRI);
			Optional<IRI> optVersionIRI = Optional.of(versionIRI); 
			OWLOntologyID newOntologyID = new OWLOntologyID(optTargetOntologyIRI, optVersionIRI);
			SetOntologyID setOntologyID = new SetOntologyID(targetOntology, newOntologyID);
			manager.applyChange(setOntologyID);

			// Annotations
			// Ontology Name
			OWLAnnotation lblName = factory.getOWLAnnotation(factory.getRDFSLabel(), factory.getOWLLiteral(ontologyName, "en"));
			OWLAxiom axiom = factory.getOWLAnnotationAssertionAxiom(targetOntologyIRI , lblName);
			manager.applyChange(new AddAxiom(targetOntology, axiom));	
			
			// Version
			OWLAnnotation version = factory.getOWLAnnotation(factory.getOWLVersionInfo(), factory.getOWLLiteral(Util.getCurrentTimeStamp(), "en"));
			axiom = factory.getOWLAnnotationAssertionAxiom(targetOntologyIRI , version);
			manager.applyChange(new AddAxiom(targetOntology, axiom));
			
			// LOAD & EQUIVALENCES
			// OIO:
			String fileOIO = "OIO - OntoLing - 20161104.owl";
			File fileBaseOIO = new File(location + fileOIO);			
			if (fileBaseOIO.exists()) { 
				sourceIntegrationOntology = manager.loadOntologyFromOntologyDocument(fileBaseOIO);
				OntologyMerger.loadOntology(sourceIntegrationOntology, targetOntology);
			}
			else 
				throw new FileNotExistsException(fileOIO);	

			// LLO:
			String fileLLO = "LLO - OntoLing - 20161104.owl";
			File fileBaseLLO = new File(location + fileLLO);			
			if (fileBaseLLO.exists()) { 
				sourceLevelOntology = manager.loadOntologyFromOntologyDocument(fileBaseLLO);		
				OntologyMerger.loadOntologyWithConceptualProperties(sourceLevelOntology, targetOntology);//////////CHECKEAR SI VA AQUI O PUEDE ABAJO
			}
			else 
				throw new FileNotExistsException(fileLLO);	
			
			// LUO:
			String fileLUO = "LUO - OntoLing - 20161104.owl";
			File fileBaseLUO = new File(location + fileLUO);			
			if (fileBaseLUO.exists()) { 
				sourceUnitOntology = manager.loadOntologyFromOntologyDocument(fileBaseLUO);
				OntologyMerger.loadOntologyWithConceptualProperties(sourceUnitOntology, targetOntology);//////////CHECKEAR SI VA AQUI O PUEDE ABAJO
			}
			else 
				throw new FileNotExistsException(fileLUO);	

			// TRANSFORMATIONS
			// LAO: Classes -> Attributes
			String fileLAO = "LAO - OntoLing - 20161104.owl";
			File fileBaseLAO = new File(location + fileLAO);			
			if (fileBaseLAO.exists()) { 
				sourceAttributeOntology = manager.loadOntologyFromOntologyDocument(fileBaseLAO);
				OntologyMerger.addClassesAsDataProperties(sourceAttributeOntology, targetOntology);
				OntologyMerger.addIndividualsAsLeafDataProperties(sourceAttributeOntology, targetOntology);
			}
			else 
				throw new FileNotExistsException(fileLAO);	
			
			// LVO: Classes -> Attribute-Values
			String fileLVO = "LVO - OntoLing - 20161104.owl";
			File fileBaseLVO = new File(location + fileLVO);			
			if (fileBaseLVO.exists()) { 
				sourceValueOntology = manager.loadOntologyFromOntologyDocument(fileBaseLVO);
				//	Obtener rangos para las propiedades hojas previamente obtenidas por transformacion 
				OntologyMerger.addIndividualsAsLeafDataPropertyRanges(sourceAttributeOntology, sourceValueOntology, targetOntology);
			}
			else 
				throw new FileNotExistsException(fileLVO);

			// LRO: Classes -> Attributes
			String fileLRO = "LRO - OntoLing - 20161104.owl";
			File fileBaseLRO = new File(location + fileLRO);			
			if (fileBaseLRO.exists()) { 
				sourceRelationsOntology = manager.loadOntologyFromOntologyDocument(fileBaseLRO);
				OntologyMerger.addClassesAsObjectProperties(sourceRelationsOntology, targetOntology);
				OntologyMerger.addIndividualsAsLeafObjectProperties(sourceRelationsOntology, targetOntology);
				OntologyMerger.addDisjointObjectPropertiesFromClasses(sourceRelationsOntology, targetOntology); 
			}
			else 
				throw new FileNotExistsException(fileLRO);	
			
			// Import other ontologies
			OntologyMerger.loadOntologyWithConceptualProperties(sourceAttributeOntology, targetOntology);
			OntologyMerger.loadOntologyWithConceptualProperties(sourceValueOntology, targetOntology);
			OntologyMerger.loadOntologyWithConceptualProperties(sourceRelationsOntology, targetOntology);

			// Relate equivalent entities 
			OntologyEquivalences.addEquivalentConcepts(targetOntology);
			OntologyEquivalences.addEquivalentAttributes(targetOntology);
			OntologyEquivalences.addEquivalentAdHocRelations(targetOntology);
			
			// Transformed Data Properties Domains
			Set<OWLOntology> excludedOntologies = new HashSet<OWLOntology>();
			excludedOntologies.add(sourceAttributeOntology);
			excludedOntologies.add(sourceIntegrationOntology);
			excludedOntologies.add(sourceLevelOntology);
			excludedOntologies.add(sourceRelationsOntology);
			excludedOntologies.add(sourceValueOntology);
			OntologyMerger.addTransformedDataPropertiesDomains(excludedOntologies, targetOntology);
			
			// Transformed Object Properties Domains
			OntologyMerger.addTransformedObjectPropertiesDomains(excludedOntologies, targetOntology);
			
			////////////////////////////////////////////////////////////////////////////////////////
			// Output
			String targetFileName = ontologyName + " - " + Util.getCurrentTimeStamp() + ".owl";
			OutputStream os = new FileOutputStream(new File(location + targetFileName));
			manager.saveOntology(targetOntology, os);		
			
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (OWLOntologyStorageException e) {
			e.printStackTrace();
		} 
	}
}
