package owlgenerator.tests.ontoTag.merge;
import java.io.File;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.xadisk.filesystem.exceptions.FileNotExistsException;

import owlgenerator.java.merger.OntologyData;

// ONTOLOGY DEBUG
// Entidades sin label es porque aparecen en el doc original posteriormente y no estan definidas, 
// se debe a una discrepancia en el consenso terminologico en el momento de escribir la tesis, 
// ya que se refieren a algun termino ya definido y no se han renombrado del nombre inicial a ese definido.

// Sirve para investigar en el documento fuente el origen del problema.
public class preTests {
/////////////////Merger - OntoLing//////////////////
	@SuppressWarnings("unused")
	public static void main(String[] args) throws FileNotExistsException {
		try {
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			String location = "C:\\outaspace\\Out\\Merge\\";
			
			// INPUT ONTOLOGIES
			OWLOntology sourceAttributeOntology = null, sourceIntegrationOntology = null, sourceLevelOntology = null, 
					sourceRelationsOntology = null, sourceUnitOntology = null, sourceValueOntology = null;

			// LOAD & EQUIVALENCES
			// OIO:
			String fileOIO = "OIO - OntoLing - 25092016.owl";
			File fileBaseOIO = new File(location + fileOIO);			
			if (fileBaseOIO.exists()) 
				sourceIntegrationOntology = manager.loadOntologyFromOntologyDocument(fileBaseOIO);
			else 
				throw new FileNotExistsException(fileOIO);	

			// LLO:
			String fileLLO = "LLO - OntoLing - 25092016.owl";
			File fileBaseLLO = new File(location + fileLLO);			
			if (fileBaseLLO.exists())
				sourceLevelOntology = manager.loadOntologyFromOntologyDocument(fileBaseLLO);		
			else 
				throw new FileNotExistsException(fileLLO);	
			
			// LUO:
			String fileLUO = "LUO - OntoLing - 25092016.owl";
			File fileBaseLUO = new File(location + fileLUO);			
			if (fileBaseLUO.exists())  
				sourceUnitOntology = manager.loadOntologyFromOntologyDocument(fileBaseLUO);		
			else 
				throw new FileNotExistsException(fileLUO);	

			// LAO:
			String fileLAO = "LAO - OntoLing - 25092016.owl";
			File fileBaseLAO = new File(location + fileLAO);			
			if (fileBaseLAO.exists())
				sourceAttributeOntology = manager.loadOntologyFromOntologyDocument(fileBaseLAO);
			else 
				throw new FileNotExistsException(fileLAO);	
			
			// LVO: 
			String fileLVO = "LVO - OntoLing - 25092016.owl";
			File fileBaseLVO = new File(location + fileLVO);			
			if (fileBaseLVO.exists()) 
				sourceValueOntology = manager.loadOntologyFromOntologyDocument(fileBaseLVO);
			else 
				throw new FileNotExistsException(fileLVO);

			// LRO: 
			String fileLRO = "LRO - OntoLing - 25092016.owl";
			File fileBaseLRO = new File(location + fileLRO);			
			if (fileBaseLRO.exists())
				sourceRelationsOntology = manager.loadOntologyFromOntologyDocument(fileBaseLRO);
			else 
				throw new FileNotExistsException(fileLRO);	

			// Classes
			Set<OWLClass> laoClassProblems1 = OntologyData.getUnlabeledClasses(sourceAttributeOntology);//OK
			Set<OWLClass> oioClassProblems2 = OntologyData.getUnlabeledClasses(sourceIntegrationOntology);//OK
			Set<OWLClass> lloClassProblems3 = OntologyData.getUnlabeledClasses(sourceLevelOntology);//OK
			Set<OWLClass> lroClassProblems4 = OntologyData.getUnlabeledClasses(sourceRelationsOntology);//OK
			Set<OWLClass> luoClassProblems5 = OntologyData.getUnlabeledClasses(sourceUnitOntology);//NOOK
			Set<OWLClass> lvoClassProblems6 = OntologyData.getUnlabeledClasses(sourceValueOntology);//NOOK
			
			// Data Properties
			Set<OWLDataProperty> laoDataPropertyProblems1 = OntologyData.getUnlabeledDataProperties(sourceAttributeOntology);//OK
			Set<OWLDataProperty> oioDataPropertyProblems2 = OntologyData.getUnlabeledDataProperties(sourceIntegrationOntology);//OK
			Set<OWLDataProperty> lloDataPropertyProblems3 = OntologyData.getUnlabeledDataProperties(sourceLevelOntology);//OK
			Set<OWLDataProperty> lroDataPropertyProblems4 = OntologyData.getUnlabeledDataProperties(sourceRelationsOntology);//OK
			Set<OWLDataProperty> luoDataPropertyProblems5 = OntologyData.getUnlabeledDataProperties(sourceUnitOntology);//NOOK
			Set<OWLDataProperty> lvoDataPropertyProblems6 = OntologyData.getUnlabeledDataProperties(sourceValueOntology);//NOOK
			
			// Object Properties
			Set<OWLObjectProperty> laoObjectPropertyProblems1 = OntologyData.getUnlabeledObjectProperties(sourceAttributeOntology);//
			Set<OWLObjectProperty> oioObjectPropertyProblems2 = OntologyData.getUnlabeledObjectProperties(sourceIntegrationOntology);//
			Set<OWLObjectProperty> lloObjectPropertyProblems3 = OntologyData.getUnlabeledObjectProperties(sourceLevelOntology);//
			Set<OWLObjectProperty> lroObjectPropertyProblems4 = OntologyData.getUnlabeledObjectProperties(sourceRelationsOntology);//
			Set<OWLObjectProperty> luoObjectPropertyProblems5 = OntologyData.getUnlabeledObjectProperties(sourceUnitOntology);//
			Set<OWLObjectProperty> lvoObjectPropertyProblems6 = OntologyData.getUnlabeledObjectProperties(sourceValueOntology);//
			
			// Individuals
			Set<OWLNamedIndividual> laoIndividualProblems1 = OntologyData.getUnlabeledNamedIndividuals(sourceAttributeOntology);//
			Set<OWLNamedIndividual> oioIndividualProblems2 = OntologyData.getUnlabeledNamedIndividuals(sourceIntegrationOntology);//
			Set<OWLNamedIndividual> lloIndividualProblems3 = OntologyData.getUnlabeledNamedIndividuals(sourceLevelOntology);//
			Set<OWLNamedIndividual> lroIndividualProblems4 = OntologyData.getUnlabeledNamedIndividuals(sourceRelationsOntology);//
			Set<OWLNamedIndividual> luoIndividualProblems5 = OntologyData.getUnlabeledNamedIndividuals(sourceUnitOntology);//
			Set<OWLNamedIndividual> lvoIndividualProblems6 = OntologyData.getUnlabeledNamedIndividuals(sourceValueOntology);//
		
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		} 
	}
}
