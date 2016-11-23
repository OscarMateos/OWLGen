/* 
 *  DEDUCCION INVERSAS
 */
package owlgenerator.java.inverserelations;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.search.EntitySearcher;

import owlgenerator.java.core.Core;

/**
 * 	DEDUCCION INVERSAS.
 *
 * @author Oscar Mateos Lopez
 * @version: 20161103
 */

public class InferInverseRelations {
	
	/** The ontology. */
	private OWLOntology ontology = null;
	
	/**
	 * Gets the ontology.
	 *
	 * @return the ontology
	 */
	//	GETTERS & SETTERS
	public OWLOntology getOntology() {
		return ontology;
	}
	
	/**
	 * Sets the ontology.
	 *
	 * @param ontology the new ontology
	 */
	public void setOntology(OWLOntology ontology) {
		this.ontology = ontology;
	}
	
	/**
	 * Gets the stem.
	 *
	 * @param input the input
	 * @return the stem
	 */
	// MAIN METHODS
	public static String getStem(String input) {
	 	Stemmer s = new Stemmer();
	 	return s.stem(input).toLowerCase();
	}

	/**
	 * Infer inverse ad hocs.
	 *
	 * @param generator the generator
	 * @return the int
	 */
	public int inferInverseAdHocs(Core generator) {
		OWLOntology ontology = generator.getOntology();
		OWLOntologyManager manager = generator.getManager();
		OWLDataFactory factory = manager.getOWLDataFactory();
		
		// A set with the Object Properties is created, removing those that already have defined inverse relation.
		Set<OWLObjectProperty> objectPropertiesSet = ontology.getObjectPropertiesInSignature();
		objectPropertiesSet.removeIf(HAS_INVERSE);
		int i = 0;

		// For each remaining property, it is compared to the inverse candidates
		for(OWLObjectProperty op_ : objectPropertiesSet){
			Set<OWLObjectProperty> auxSet =  new HashSet<OWLObjectProperty>(objectPropertiesSet);
			auxSet.remove(op_);
			auxSet.removeIf(CONTAINS_STEM(op_).negate());
			auxSet.removeIf(INVERTED_DOMAIN_AND_RANGE(op_).negate());

			// MAX CORRECTION vs RECALL: 
			// 	Inverse to be admitted must have a single property candidate to inverse so that there is no ambiguity.
			if (auxSet.size() == 1){
				// Inverse relation
				OWLInverseObjectPropertiesAxiom inverseOf = factory.getOWLInverseObjectPropertiesAxiom(op_, auxSet.iterator().next());
				manager.applyChange(new AddAxiom(this.getOntology(), inverseOf));

				// The Object Properties Set is updated, updating inverses
				objectPropertiesSet = ontology.getObjectPropertiesInSignature();
				objectPropertiesSet.removeIf(HAS_INVERSE);
				i++;
			}
		}
		// Returns the number of inferred inverse relations
		return i;
	}

	/** The has inverse. */
	public Predicate<OWLObjectProperty> HAS_INVERSE = new Predicate<OWLObjectProperty>() {
		//@Override
		  public boolean test(OWLObjectProperty op) {
			  if (EntitySearcher.getInverses(op, ontology).size() > 0)
					return true;
			  return false;		 
		  }
	};

	//opSource = ObjectProperty reference
	/**
	 * Inverted domain and range.
	 *
	 * @param opSource the op source
	 * @return the predicate
	 */
	//input = ObjectProperty destination, which iterates the set of all ObjectProperties
	private Predicate<OWLObjectProperty> INVERTED_DOMAIN_AND_RANGE(final OWLObjectProperty opSource) {
	    return new Predicate<OWLObjectProperty>() {
	        @Override
	        public boolean test(OWLObjectProperty input) {
	        	//Collection<OWLClassExpression> sourceDomain = EntitySearcher.getDomains(opSource, ontology);
	        	//Collection<OWLClassExpression> sourceRange = EntitySearcher.getRanges(opSource, ontology);       	
	        	//if (sourceDomain.size() > 0 && sourceRange.size() > 0)
	        		return EntitySearcher.getDomains(opSource, ontology).equals(EntitySearcher.getRanges(input, ontology)) 
	        			&& EntitySearcher.getRanges(opSource, ontology).equals(EntitySearcher.getDomains(input, ontology)); 
	        	//return false;
	        }
	    };
	}

	/**
	 * Contains stem.
	 *
	 * @param opSource the op source
	 * @return the predicate
	 */
	private static Predicate<OWLObjectProperty> CONTAINS_STEM(final OWLObjectProperty opSource) {
	    return new Predicate<OWLObjectProperty>() {
	        @Override
	        public boolean test(OWLObjectProperty input) {
	        	String opSourceName = opSource.getIRI().getShortForm();
	        	String inputName = input.getIRI().getShortForm();
	        	String preS = null, postS = null, rootS[] = null, preI = null, postI = null, rootI[] = null; 

	        	// Regexes to extract the "key concept" of the relation
	        	Pattern rootSource = Pattern.compile("(has|use(?:s|dTo))?(\\w+?)(At)?(_directly)?\\b");
				Matcher matcherSource = rootSource.matcher(opSourceName);				
				if (matcherSource.matches()){
					preS = matcherSource.group(1);
					rootS = matcherSource.group(2).split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");
					postS = matcherSource.group(3);
				}	
				Pattern rootInput = Pattern.compile("(is)?(\\w+?)(Of|To|By|In|With)?(_directly)?\\b");
				Matcher matcherInput = rootInput.matcher(inputName);
				if (matcherInput.matches()){
					preI = matcherInput.group(1);
					rootI = matcherInput.group(2).split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");
					postI = matcherInput.group(3);
				}			
				//Roots of both properties are obtained
				//It is divided into words, if there are compound words such as MarkedUp -> mark up
				String stemSource = "";
				for (String stemS_ : rootS){
					stemSource += getStem(stemS_)+" ";	
				}		
				String stemInput = "";
				for (String stemI_ : rootI){
					stemInput += getStem(stemI_)+" ";	
				}			 
				// Will be candidate if they have the same stem and also some of both properties has prefix and/or suffix part
				return (preS != null || postS != null || preI != null || postI != null)
						&& stemSource.equals(stemInput) ;
	        }
	    };
	}		
}
