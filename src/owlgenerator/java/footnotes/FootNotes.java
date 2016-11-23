/*
 * The Class FootNotes, main methods to process footnotes table files
 */
package owlgenerator.java.footnotes;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataOneOf;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLRestriction;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Pair;
import owlgenerator.java.core.Core;
import owlgenerator.java.core.CoreConfiguration;
import owlgenerator.java.merger.OntologyData;

/**
 * The Class FootNotes, main methods to process footnotes table files
 * 
 * @author Oscar Mateos Lopez
 * @version: 20161103
 */
public class FootNotes {
	
	/** The Constant FN_INDEX value. */
	// FootNote Constants
	private static final int FN_INDEX = 0;
	
	/** The Constant FN_PROCCESS value. */
	private static final int FN_PROCCESS = 1;
	
	/** The Constant FN_REF value. */
	private static final int FN_REF = 2;
	
	/** The Constant FN_TEXT value. */
	private static final int FN_TEXT = 3;

	/** The StanfordCoreNLP nlp instance. */
	// Global to save initialization time on the same task
	private StanfordCoreNLP nlp = null;
	
	/** The Subclass-Of Footnotes set. */
	private Set<String> subclassOfFNs = new HashSet<String>();
	
	/** The rdfs:seeAlso Footnotes set. */
	private Set<String> seeAlsoFNs = new HashSet<String>();
	
	/** The rdfs:comment Footnotes set. */
	private Set<String> commentFNs = new HashSet<String>();
	
	/** The also referred (synonyms) Footnotes set. */
	private Set<String> alsoReferredFNs = new HashSet<String>();
	
	/** The takes value (rules) Footnotes set. */
	private Set<String> takesValueFNs = new HashSet<String>();

	// BUILDER
	/**
	 * Builds the StanfordCoreNLP object, with POS tagging, lemmatization and NER.
	 *
	 * @param generator, the Core OWL generator instance
	 */
	// parsing, using regexNER file
	private void buildNLP(Core generator) {
		// If regexNER file doesn't exists, is created
		String fileName = "src\\owlgenerator\\resources\\" + generator.getCfg().getOntology_Name() + " - "
				+ generator.getCfg().getOntology_Version() + ".rgx";
		File regexNER = new File(fileName);
		if (!regexNER.exists())
			this.addRegexNER(generator);

		// Configuration
		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, lemma, ner, regexner");
		// props.put("ner.model",
		// "src\\resources\\english.all.3class.distsim.crf.ser.gz");
		// props.put("ner.model", "english.muc.7class.distsim.crf.ser.gz");
		// props.put("ner.model", "english.conll.4class.distsim.crf.ser.gz");
		props.put("regexner.mapping", fileName);
		nlp = new StanfordCoreNLP(props);
	}

	// MAIN METHODS
	/**
	 * Adds the regex NER.
	 *
	 * @param generator, the Core OWL generator instance
	 */
	// RegexNER
	private void addRegexNER(Core generator) {
		List<String> regexNERs = new ArrayList<String>();

		// Get the Regex patterns for each Class
		for (String class_ : OntologyData.getOntologyClassLabelsAsString(generator.getOntology()))
			regexNERs.add(class_ + "\tONTOLOGY_CLASS\tORGANIZATION,PERSON,LOCATION,NUMBER,MISC\n");
		Collections.sort(regexNERs);

		// Write the file
		Writer out = null;
		try {
			String fileName = "src\\owlgenerator\\resources\\" + generator.getCfg().getOntology_Name() + " - "
					+ generator.getCfg().getOntology_Version() + ".rgx";
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "UTF-8"));
			for (String regexNER_ : regexNERs) {
				try {
					out.write(regexNER_);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (UnsupportedEncodingException | FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Extract the designated Named Entities and their classification from a given piece of text.
	 *
	 * @param text, the piece of text 
	 * @param generator, the Core OWL generator instance
	 * @return the set of Pairs<Named Entity,Class>
	 * @see http://nlp.stanford.edu/nlp/javadoc/javanlp/edu/stanford/nlp/ie/regexp/RegexNERSequenceClassifier.html
	 */
	private Set<Pair<String, String>> extractDesignatedNEs(String text, Core generator) {
		Set<Pair<String, String>> tokens = new HashSet<Pair<String, String>>();

		// Get the NLP
		if (nlp == null)
			buildNLP(generator);

		// Run all Annotators on the passed-in text
		Annotation document = new Annotation(text);
		nlp.annotate(document);
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		StringBuilder sb = new StringBuilder();

		// To detect multi-literal Named Entities, look at the token of the
		// previous word
		for (CoreMap sentence : sentences) {
			// 'O' initialization,
			// 'O's are not interesting (Classless Words)
			String prevNeToken = "O";
			String currNeToken = "O";
			boolean newToken = true;

			// Classify the entities
			for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
				currNeToken = token.get(NamedEntityTagAnnotation.class);
				String word = token.get(TextAnnotation.class);

				// Strip out "O"s completely,
				if (currNeToken.equals("O")) {
					if (!prevNeToken.equals("O") && (sb.length() > 0)) {
						tokens.add(new Pair<String, String>(sb.toString(), prevNeToken));
						sb.setLength(0);
						newToken = true;
					}
					continue;
				}
				if (newToken) {
					prevNeToken = currNeToken;
					newToken = false;
					sb.append(word);
					continue;
				}
				// Multi-literal Entities
				if (currNeToken.equals(prevNeToken)) {
					sb.append(" " + word);
				} else {
					// We're done with the current entity
					tokens.add(new Pair<String, String>(sb.toString(), prevNeToken));
					sb.setLength(0);
					newToken = true;
				}
				prevNeToken = currNeToken;
			}
		}
		return tokens;
	}

	/**
	 * Extract the ontology entities from a set of designated Named Entities.
	 *
	 * @param designatedNEs, the designated Named Entities Pair set
	 * @param generator, the Core OWL generator instance
	 * @return the set of extracted ontology entities
	 */
	private Set<String> extractOntologyNEs(Set<Pair<String, String>> designatedNEs, Core generator) {
		Set<String> ontologyNEs = new HashSet<String>();
		for (Pair<String, String> ne : designatedNEs) {
			// Wil return only NEs classified as ONTOLOGY_CLASS
			if (ne.second().equals("ONTOLOGY_CLASS"))
				ontologyNEs.add(ne.first());

			// Check if NEs with different class exists as concepts
			// (misclassified)
			else {
				char[] delimiters = { '-', '/', ' ' };
				String standarizedName = WordUtils.capitalize(Inflector.singularize(ne.first()), delimiters);
				IRI ontologyClass = generator.getPm().getIRI(standarizedName.replaceAll(" ", ""));
				if (generator.getOntology().containsClassInSignature(ontologyClass))
					ontologyNEs.add(standarizedName);
			}
		}
		return ontologyNEs;
	}

	/**
	 * Gets the ontology entities from a foot note reference text.
	 *
	 * @param referenceText, the foot note reference text
	 * @param generator, the Core OWL generator instance
	 * @return the set of extracted ontology entities in the reference text
	 */
	// Extracts NEs from expression
	private Set<String> getNEsFromReference(String referenceText, Core generator) {
		Set<Pair<String, String>> referenceNEs = extractDesignatedNEs(referenceText, generator);
		return extractOntologyNEs(referenceNEs, generator);
	}

	/**
	 * Gets the foot note type for a given foot note text.
	 *
	 * @param footNote, the foot note text
	 * @return the foot note type
	 */
	private String getFootNoteType(String footNote) {
		List<String> tokenList = new ArrayList<String>(Arrays.asList(footNote.split("\\s+")));
		// Exclude FootNotes that references sections of the source document,
		// are useless outside the document
		Pattern validSectionReference = Pattern.compile("[Ss]ection\\s(?:\\d\\.*)+");
		if (!validSectionReference.matcher(footNote.toString()).find()) {
			// Type Subclass-Of : Contains "is" + ["also"] + "Subclass-Of"
			Pattern pattern = Pattern.compile("[Ss]ubclass-?[Oo]f");
			Matcher subClassOfMatcher = pattern.matcher(footNote);
			if (subClassOfMatcher.find()) {
				Pattern validSubclassOf = Pattern.compile(
						"([\\s*\\w+\\-\\/]+)((?:is\\s+|are\\s+)(?:also\\s+)?a?\\s*Subclass-Of\\s+)([\\s*\\w+\\-\\/]+)");
				Matcher matcherSubclassOf = validSubclassOf.matcher(footNote);
				if (matcherSubclassOf.find())
					return "Subclass-Of";
			}
			// SPECIAL: Also referred to as... [synonyms + seeAlso]
			else if (StringUtils.containsIgnoreCase(footNote, "also referred to as")
					|| StringUtils.containsIgnoreCase(footNote, "also known as"))
				return "alsoReferred";

			// SPECIAL: takesValue [Rules]
			else if (StringUtils.containsIgnoreCase(footNote, "takes the value")) {
				Pattern validTakesValue = Pattern.compile(
						"([\\w\\s\\-\\/]+)(\\s+takes\\s+the\\s+value\\s+)([\\w\\s\\-\\/]+)\\s+for\\s+([\\w\\s\\-\\/]+(?:,[\\w\\s\\-\\/]*)*)\\s*.*");
				Matcher matcherTakesValue = validTakesValue.matcher(footNote);
				if (matcherTakesValue.matches())
					return "takesValue";
				else
					return "comment";
			}
			// Type rdfs:seeAlso : Contains "See" + URL
			else if (tokenList.contains("See") || tokenList.contains("see")) {
				Pattern validFootnoteReference = Pattern.compile("([Pp]revious|[Nn]ext)\\s+[Ff]ootnote");
				Matcher matcherFootnoteReference = validFootnoteReference.matcher(footNote);
				// SPECIAL: Note that references another
				if (matcherFootnoteReference.find())
					return "seeAlso";
				// Detect if contains an URL
				for (String token_ : tokenList) {
					// http://commons.apache.org/proper/commons-validator/apidocs/org/apache/commons/validator/routines/UrlValidator.html
					UrlValidator URLvalid = UrlValidator.getInstance();
					if (URLvalid.isValid(token_))
						return "seeAlso";
				}
				// Others containing literal "see" as rdfs:comment
				return "comment";
			}
			// Type rdfs:comment
			else
				return "comment";
		}
		return null;
	}

	/**
	 * Gets the foot note set type.
	 *
	 * @param fNSet, the foot note set
	 * @return the foot note set type
	 */
	private String getFNSetType(Set<String> fNSet) {
		String fN = fNSet.iterator().next();
		String[] parts = fN.split(CoreConfiguration.SEPARATOR);
		String noteText = parts[FN_TEXT];
		return getFootNoteType(noteText);
	}

	/**
	 * Classify footnotes.
	 *
	 * @param tableFileName the footnotes table file name
	 */
	private void classifyFootNotes(String tableFileName) {
		try {
			CoreConfiguration cfg = CoreConfiguration.getInstance();
			File fp = new File(cfg.getFiles_Path() + "\\" + tableFileName);
			FileReader inputFile = new FileReader(fp);
			LineNumberReader lnr = new LineNumberReader(inputFile);
			String fileLine = null, type = null;

			// FIRST: Classify based on footnote text
			while ((fileLine = lnr.readLine()) != null) {
				// Data
				if (lnr.getLineNumber() > 2) {
					String[] parts = fileLine.split(CoreConfiguration.SEPARATOR);
					String noteText = parts[FN_TEXT];
					if (noteText.contains(CoreConfiguration.INNER_SEPARATOR)) {
						String[] innerFNs = noteText.split(CoreConfiguration.INNER_SEPARATOR);
						for (String subnote : innerFNs) {
							String innerFN = StringUtils.substringBefore(fileLine, noteText) + subnote;

							// If something changes, update note and add to the
							// corresponding set
							type = getFootNoteType(subnote);
							if (type != null)
								switch (type) {
								case "Subclass-Of":
									subclassOfFNs.add(innerFN);
									break;

								case "seeAlso":
									seeAlsoFNs.add(innerFN);
									break;

								case "comment":
									commentFNs.add(innerFN);
									break;

								case "alsoReferred":
									alsoReferredFNs.add(innerFN);
									break;

								case "takesValue":
									takesValueFNs.add(innerFN);
									break;
								}
						}
					} else {
						// If something changes, the note gets updated and
						// stored in the corresponding set
						type = getFootNoteType(noteText);
						if (type != null)
							switch (type) {
							case "Subclass-Of":
								subclassOfFNs.add(fileLine);
								break;

							case "seeAlso":
								seeAlsoFNs.add(fileLine);
								break;

							case "comment":
								commentFNs.add(fileLine);
								break;

							case "alsoReferred":
								alsoReferredFNs.add(fileLine);
								break;

							case "takesValue":
								takesValueFNs.add(fileLine);
								break;
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

	/**
	 * Preprocess a footnotes set.
	 * 
	 * MAX CORRECCION vs RECALL: Extracts Named Entities from the reference and FootNote text, discard the FootNote if no entities found or exist ambiguity
	 *
	 * @param fNs, the footnotes set
	 * @param tableFileName the footnotes table file name
	 * @param generator, the Core OWL generator instance
	 * @return the pre-processed footnotes set
	 */
	private Set<Pair<String, Object>> preprocessFootNotes(Set<String> fNs, String tableFileName, Core generator) {
		Set<Pair<String, Object>> refinedSet = new HashSet<Pair<String, Object>>();
		String type = getFNSetType(fNs);
		if (type != null)
			switch (type) {
			case "Subclass-Of":
				for (String fN_ : fNs) {
					Set<Pair<String, Object>> auxSet = preSubclassOfFN(fN_, generator);
					if (auxSet != null)
						refinedSet.addAll(auxSet);
				}
				break;

			case "seeAlso":
				for (String fN_ : fNs) {
					Set<Pair<String, Object>> auxSet = preSeeAlsoFN(fN_, tableFileName, generator);
					if (auxSet != null)
						refinedSet.addAll(auxSet);
				}
				break;

			case "comment":
				for (String fN_ : fNs) {
					Set<Pair<String, Object>> auxSet = preCommentFN(fN_, generator);
					if (auxSet != null)
						refinedSet.addAll(auxSet);
				}
				break;

			case "alsoReferred":
				for (String fN_ : fNs) {				
					Set<Pair<String, Object>> auxSet = preAlsoReferredFN(fN_);
					if (auxSet != null)
						refinedSet.addAll(auxSet);
				}
				break;

			case "takesValue":
				for (String fN_ : fNs) {
					Pair<String, Object> rule = preTakesValueFN(fN_);
					if (rule != null)
						refinedSet.add(rule);
				}
				break;
			}
		refinedSet.remove(null);
		return refinedSet;
	}

	// PREPROCESS:
	/**
	 * Preprocess step for a Subclass-Of type foot note. Generates the Subclass-Of pair(s) <DerivedClass, Superclass> for a given foot note.
	 * Extract ontology entities from the reference or discard, idem on note text and generates the related Subclass-Of relations.
	 * This type may generate several entries for each FootNote.
	 *
	 * @param fN, the foot note text
	 * @param generator, the Core OWL generator instance
	 * @return the set of generated Subclass-Of pairs <DerivedClass, Superclass> from the foot note
	 */
	private Set<Pair<String, Object>> preSubclassOfFN(String fN, Core generator) {
		Set<Pair<String, Object>> refinedFNs = null;
		String[] parts = fN.split(CoreConfiguration.SEPARATOR);

		// Parts of the entry
		boolean refNeedsProcess = Boolean.parseBoolean(parts[FN_PROCCESS]);
		String reference = parts[FN_REF];
		String noteText = StringUtils.substringBefore(parts[FN_TEXT].trim(), ".") + ".";
		if (StringUtils.isNotBlank(reference) && StringUtils.isNotBlank(noteText)) {
			Set<String> refNEs = null;
			// Refine reference
			// Process the reference if requires processing (1st field is true)
			// It happens for references whose reference wasn't extracted from a
			// table,
			// so has to extract the subject entity/-ies from the reference
			// text.
			if (refNeedsProcess) {
				refNEs = getNEsFromReference(reference, generator);
				// Discard the FootNote if no entities were found or exist
				// ambiguity.
				// Only should find one Named Entity for each note unless they
				// are synonyms.
				if (refNEs.size() != 1) {
					boolean test = areAllSynonyms(refNEs, generator);
					if (!test)
						return null;
				}
			} else {
				// Refine reference
				// Discard synonyms
				if (reference.contains("(") && reference.contains(")"))
					reference = StringUtils.substringBefore(reference, "(");

				// Refine FootNote text
				// Text after "Subclass-Of" should contain the superclass(es)
				String superclassesText = noteText
						.substring(noteText.lastIndexOf("Subclass-Of") + "Subclass-Of".length()).trim();
				superclassesText = superclassesText
						.replaceAll("(\\(\\s*[Aa]\\s*[Tt]ype\\s*[Oo]f(?:(?:\\s+\\w+\\-*)+)\\))", "");
				Set<String> superclasses = getNEsFromReference(superclassesText, generator);

				// If several superclasses are found, generate one
				// refined-FootNote for each
				if (superclasses.size() > 0) {
					refinedFNs = new HashSet<Pair<String, Object>>();
					for (String superclass : superclasses)
						refinedFNs.add(new Pair<String, Object>(reference, superclass));
				}
			}
			return refinedFNs;
		} else
			return null;
	}

	/**
	 * Gets the nth foot note from a foot note text file.
	 *
	 * @param tableFileName, the foot note table file name
	 * @param index, the foot note index
	 * @return the nth foot note
	 */
	private String getNthFootNote(String tableFileName, int index) {
		try {
			CoreConfiguration cfg = CoreConfiguration.getInstance();
			File fp = new File(cfg.getFiles_Path() + "\\" + tableFileName);
			FileReader inputFile = new FileReader(fp);
			LineNumberReader lnr = new LineNumberReader(inputFile);
			String fileLine = null, noteText = null;

			while ((fileLine = lnr.readLine()) != null) {
				if (lnr.getLineNumber() > 2
						&& fileLine.startsWith(Integer.toString(index) + CoreConfiguration.SEPARATOR)) {
					String[] parts = fileLine.split(CoreConfiguration.SEPARATOR);
					noteText = parts[FN_TEXT];
				}
			}
			lnr.close();
			return noteText;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Preprocess step for a rdfs:seeAlso type foot note. 
	 * Generates the rdfs:seeAlso annotation pair(s) <Refined reference, Foot note text> for a given foot note.
	 * This type may generate several entries for each FootNote.
	 *
	 * @param fN, the foot note text
	 * @param tableFileName, the foot note table file name
	 * @param generator, the Core OWL generator instance
	 * @return the set of generated annotation pair(s) <Refined reference, Foot note text>
	 */
	private Set<Pair<String, Object>> preSeeAlsoFN(String fN, String tableFileName, Core generator) {
		Set<Pair<String, Object>> refinedFNs = null;
		String[] parts = fN.split(CoreConfiguration.SEPARATOR);
		Set<String> refNEs = null;

		// Parts of the entry
		int entryIndex = Integer.parseInt(parts[FN_INDEX]);
		boolean refNeedsProcess = Boolean.parseBoolean(parts[FN_PROCCESS]);
		String referenceText = parts[FN_REF];
		String noteText = parts[FN_TEXT];
		if (StringUtils.isNotBlank(referenceText) && StringUtils.isNotBlank(noteText)) {
			// Refine reference
			// Process the reference if requires processing (1st field is true)
			// It happens for references whose reference wasn't extracted from a
			// table,
			// so has to extract the subject entity/-ies from the reference
			// text.
			if (refNeedsProcess) {
				refNEs = getNEsFromReference(referenceText, generator);
				// Discard the FootNote if no entities were found or exist
				// ambiguity.
				// Only should find one Named Entity for each note unless they
				// are synonyms
				if (refNEs.size() != 1) {
					boolean test = areAllSynonyms(refNEs, generator);
					if (!test)
						return null;
				}
			} else {
				// Reference contains synonyms
				if (referenceText.contains("(") && referenceText.contains(")"))
					refNEs = getNEsFromReference(referenceText, generator);
				else {
					Pattern pattern = Pattern.compile("\\{([\\w+\\-\\/[\\,\\s]*]+)\\}");
					Matcher matcher = pattern.matcher(referenceText);
					if (matcher.matches())
						return null;
					else {
						refNEs = new HashSet<String>();
						refNEs.add(referenceText);
					}
				}
			}
			// Refine FootNote text
			if (tableFileName != null) {
				// SPECIAL: Note that references another
				Pattern otherRef = Pattern.compile("([Pp]revious|[Nn]ext)\\s+[Ff]ootnote");
				Matcher matcherOtherRef = otherRef.matcher(noteText);
				if (matcherOtherRef.find()) {
					if (StringUtils.containsIgnoreCase(noteText, "PREVIOUS"))
						noteText = getNthFootNote(tableFileName, entryIndex - 1);
					else if (StringUtils.containsIgnoreCase(noteText, "NEXT"))
						noteText = getNthFootNote(tableFileName, entryIndex + 1);
				}
			}
			// Put together the refined reference and FN's text
			refinedFNs = new HashSet<Pair<String, Object>>();
			for (String refNE : refNEs)
				refinedFNs.add(new Pair<String, Object>(refNE, noteText));
			return refinedFNs;
		} else
			return null;
	}

	/**
	 * Returns true if all the given names from a set are all synonyms in the Core ontology.
	 *
	 * @param names, the ontology entity names
	 * @param generator, the Core OWL generator instance
	 * @return true, if successful
	 */
	private boolean areAllSynonyms(Set<String> names, Core generator) {
		if (names.size() > 1) {
			OWLClass claseEq = generator.getOWLClassFromName(names.iterator().next());
			Set<OWLEquivalentClassesAxiom> eqClasses = generator.getOntology().getEquivalentClassesAxioms(claseEq);
			if (eqClasses.size() == 0)
				return false;
			else {
				// If the set is not empty, check if the classes belong to the
				// synonyms
				Set<OWLClass> classes = eqClasses.iterator().next().getClassesInSignature();
				for (String name : names) {
					OWLClass claseTest = generator.getOWLClassFromName(name);
					if (!classes.contains(claseTest))
						return false;
				}
				return true;
			}
		} else
			return false;
	}

	/**
	 * Preprocess step for a comment foot note type. 
	 * Generates the rdfs:comment annotation pair(s) <Refined reference, Foot note text> for a given foot note.
	 * This type may generate several entries for each FootNote.
	 *
	 * @param fN, the foot note text
	 * @param tableFileName, the foot note table file name
	 * @param generator, the Core OWL generator instance
	 * @return the set of generated annotation pair(s) <Refined reference, Foot note text>
	 */
	private Set<Pair<String, Object>> preCommentFN(String fN, Core generator) {
		return preSeeAlsoFN(fN, null, generator);
	}

	/**
	 * Preprocess step for an also referred (synonyms) type foot note. 
	 * Generates the rules to generate OWL synonym axioms asa set of <Refined reference, Synonyms> pairs 
	 * This type may generate a synonym rule for each FootNote [ and optionally +1 seeAlso ]
	 *
	 * @param fN, the foot note text
	 * @return the set of generated synonym rule pairs
	 */
	private Set<Pair<String, Object>> preAlsoReferredFN(String fN) {
		Set<Pair<String, Object>> refinedFNs = null;
		String[] parts = fN.split(CoreConfiguration.SEPARATOR);

		// Parts of the entry
		boolean refNeedsProcess = Boolean.parseBoolean(parts[FN_PROCCESS]);
		String referenceText = parts[FN_REF];
		String noteText = parts[FN_TEXT];
		String entities = null;
		
		if (StringUtils.isNotBlank(referenceText) && StringUtils.isNotBlank(noteText)) {
			// Refine reference
			// Process the reference if NOT requires processing
			if (!refNeedsProcess) {
				Pattern pattern = Pattern.compile(
						"(?:[Aa]lso\\s+referred\\s+to\\s+as|[Aa]lso\\s+known\\s+as)((?:\\s+|\\w+|\\-*)+)(?:(\\s*\\(\\s*or\\s*\\w+\\-*\\)))?((?:\\s+\\w+\\-*)+)?");
				Matcher matcherEntities = pattern.matcher(noteText);
				if (matcherEntities.find()) {
					String g1 = (matcherEntities.group(1) != null) ? matcherEntities.group(1).trim() : null;
					String g2 = (matcherEntities.group(2) != null) ? matcherEntities.group(2).trim() : null;
					String g3 = (matcherEntities.group(3) != null) ? matcherEntities.group(3).trim() : null;

					// OR Composition
					if (g2 != null) {
						Pattern inner = Pattern.compile("\\(\\s*or\\s+(\\w+\\-*\\s*)+\\)");
						Matcher matcherInner = inner.matcher(g2);
						if (matcherInner.matches()) {
							String innerG2 = matcherInner.group(1);
							String e1 = g1 + " " + g3;
							String e2 = innerG2 + " " + g3;
							entities = e1 + "," + e2;
						}
					} else
						entities = matcherEntities.group(1).replaceAll("\\s+and\\s+|\\s+or\\s+|\\s*,\\s*", ",").trim();

					// "Sanitize" the extracted entities by removing the extra
					// text that could remain
					String[] saninitzedEntities = entities.split(",");
					for (int index = 0; index < saninitzedEntities.length; index++)
						saninitzedEntities[index] = sanitize(saninitzedEntities[index]);
					entities = StringUtils.join(saninitzedEntities, ",");
				} else
					return null;
			}
			refinedFNs = new HashSet<Pair<String, Object>>();

			// Include rule to generate synonyms
			refinedFNs.add(new Pair<String, Object>(referenceText, entities.trim()));

			// Include rules for seeAlso if contains quotes
			if (noteText.contains("See ") || noteText.contains("see "))
				refinedFNs.add(new Pair<String, Object>(referenceText, noteText));
			return refinedFNs;
		} else
			return null;
	}

	/**
	 * Sanitizes text.
	 *
	 * @param entityAndMore, the entity and other accompanying text
	 * @return the sanitized string
	 */
	private String sanitize(String entityAndMore) {
		String[] literals = entityAndMore.split("\\s+");
		if (StringUtils.isNotBlank(entityAndMore)) {
			if (literals.length > 1) {
				String firstLiteral = entityAndMore.substring(0, entityAndMore.indexOf(" "));
				String sanitizedEntity = "";

				// If the first literal is capital: It is Concept or Instance
				// and takes all the next ones starting capital
				if (Character.isUpperCase(firstLiteral.charAt(0))) {
					for (String literal_ : literals) {
						if (Character.isUpperCase(literal_.charAt(0)))
							sanitizedEntity += literal_ + " ";
						else
							return sanitizedEntity.trim();
					}
					return sanitizedEntity.trim();
				}
				// If not, it's Attribute, Relation or Instance and is single
				// literal, rest is discarded
				else
					return firstLiteral.trim();
			} else
				return entityAndMore;
		}
		return null;
	}
  
	/**
	 * Preprocess step for a takes value type foot note.
	 * Generates restriction value triple rule(s) for attributes (Data Properties) as a triple implemented as Pair<Attribute, Pair<Domain(s), Value>>
	 *
	 * @param fN, the foot note text
	 * @return the rule triple implemented as Pair<Attribute, Pair<Domain(s), Value>>
	 */
	private Pair<String, Object> preTakesValueFN(String fN) {
		Pair<String, Object> t = null;
		String[] parts = fN.split(CoreConfiguration.SEPARATOR);

		// Parts of the entry
		boolean refNeedsProcess = Boolean.parseBoolean(parts[FN_PROCCESS]);
		String referenceText = parts[FN_REF];
		String noteText = parts[FN_TEXT];

		if (StringUtils.isNotBlank(referenceText) && StringUtils.isNotBlank(noteText)) {
			// Refine reference
			// Process the reference if NOT requires processing
			if (!refNeedsProcess) {
				Pattern validTakesValue = Pattern.compile(
						"([\\w\\s\\-\\/]+)(\\s+takes\\s+the\\s+value\\s+)([\\w\\s\\-\\/]+)\\s+for\\s+([\\w\\s\\-\\/]+(?:,[\\w\\s\\-\\/]*)*)\\s*.*");
				Matcher matcherTakesValue = validTakesValue.matcher(noteText);
				if (matcherTakesValue.matches()) {
					String attribute = matcherTakesValue.group(1);
					String value = matcherTakesValue.group(3);
					String domain = matcherTakesValue.group(4).replaceAll("\\s+and\\s+|\\s*\\,\\s*", ",");

					// "Sanitize" the extracted entities by removing the extra text that could remain
					String[] saninitzedEntities = domain.split(",");
					for (int index = 0; index < saninitzedEntities.length; index++)
						saninitzedEntities[index] = sanitize(saninitzedEntities[index]);
					domain = StringUtils.join(saninitzedEntities, ",");
					t = new Pair<String, Object>(attribute, new Pair<String, String>(domain, value));
				}
			}
		}
		return t;
	}

	/**
	 * Gets the Subclass-Of axiom(s) for a given preprocessed Subclass-Of footnotes set.
	 *
	 * @param pairSet, the preprocessed Subclass-Of footnotes set
	 * @param generator, the Core OWL generator instance
	 * @return the OWL Subclass-Of of axiom(s) set
	 */
	// POSTPROCESS: Getting the axioms
	private Set<OWLAxiom> getFootNoteSubclassOfAxioms(Set<Pair<String, Object>> pairSet, Core generator) {
		if (pairSet.size() > 0) {
			OWLDataFactory factory = generator.getManager().getOWLDataFactory();
			OWLClass subClass = null, superClass = null;
			OWLSubClassOfAxiom axiom = null;
			Set<OWLAxiom> axiomSet = new HashSet<OWLAxiom>();
			for (Pair<String, Object> p_ : pairSet) {
				subClass = generator.getOWLClassFromName(p_.first().replace("/", "-"));
				superClass = generator.getOWLClassFromName(p_.second().toString().replace("/", "-"));
				axiom = factory.getOWLSubClassOfAxiom(subClass, superClass);
				axiomSet.add(axiom);
			}
			return axiomSet;
		} else
			return null;
	}

	/**
	 * Gets the rdfs:seeAlso annotation axiom(s) for a given preprocessed rdfs:seeAlso footnotes set.
	 *
	 * @param pairSet, the preprocessed rdfs:seeAlso footnotes set
	 * @param generator, the Core OWL generator instance
	 * @return the rdfs:seeAlso annotation axiom(s) set
	 */
	private Set<OWLAxiom> getFootNoteSeeAlsoAxioms(Set<Pair<String, Object>> pairSet, Core generator) {
		if (pairSet.size() > 0) {
			OWLDataFactory factory = generator.getManager().getOWLDataFactory();
			OWLEntity entity = null;
			OWLAnnotation annotation = null;
			OWLAnnotationAssertionAxiom axiom = null;
			Set<OWLAxiom> axiomSet = new HashSet<OWLAxiom>();
			for (Pair<String, Object> p_ : pairSet) {
				String entityName = p_.first().replace(" ", "");

				// Generate entity depending on type
				if (generator.getOntology()
						.containsClassInSignature(generator.getPm().getIRI(entityName.replace("/", "-"))))
					entity = generator.getOWLClassFromName(entityName.replace("/", "-"));
				else if (generator.getOntology().containsObjectPropertyInSignature(
						generator.getPm().getIRI(StringUtils.uncapitalize(entityName))))
					entity = generator.getOWLObjectPropertyFromName(entityName.replace("/", "-"));
				else if (generator.getOntology().containsDataPropertyInSignature(
						generator.getPm().getIRI(StringUtils.uncapitalize(entityName))))
					entity = generator.getOWLDataPropertyFromName(entityName.replace("/", "-"));
				else if (generator.getOntology().containsIndividualInSignature(generator.getPm().getIRI(entityName)))
					entity = (OWLEntity) generator.getOWLIndividualFromName(entityName.replace("/", "-"));
				else
					return null;

				annotation = factory.getOWLAnnotation(factory.getRDFSSeeAlso(),
						factory.getOWLLiteral(p_.second().toString(), "en"));
				axiom = factory.getOWLAnnotationAssertionAxiom(entity.getIRI(), annotation);
				axiomSet.add(axiom);
			}
			return axiomSet;
		} else
			return null;
	}

	/**
	 * Gets the foot note rdfs:comment annotation axiom(s) for a given preprocessed rdfs:comment footnotes set.
	 *
	 * @param pairSet, the preprocessed rdfs:comment footnotes set
	 * @param generator, the Core OWL generator instance
	 * @return the rdfs:comment axiom(s) set
	 */
	private Set<OWLAxiom> getFootNoteCommentAxioms(Set<Pair<String, Object>> pairSet, Core generator) {
		if (pairSet.size() > 0) {
			OWLDataFactory factory = generator.getManager().getOWLDataFactory();
			OWLEntity entity = null;
			OWLAnnotation annotation = null;
			OWLAnnotationAssertionAxiom axiom = null;
			Set<OWLAxiom> axiomSet = new HashSet<OWLAxiom>();
			for (Pair<String, Object> p_ : pairSet) {
				String entityName = null;
				if (StringUtils.upperCase(p_.first(), Locale.ENGLISH).equals(p_.first()))
					// All-Capital literals, separate with "_"
					entityName = p_.first().replace(" ", "_");
				else
					entityName = p_.first().replace(" ", "");

				// Determine type
				if (generator.getOntology()
						.containsClassInSignature(generator.getPm().getIRI(entityName.replace("/", "-"))))
					entity = generator.getOWLClassFromName(entityName.replace("/", "-"));
				else if (generator.getOntology().containsObjectPropertyInSignature(
						generator.getPm().getIRI(StringUtils.uncapitalize(entityName))))
					entity = generator.getOWLObjectPropertyFromName(entityName.replace("/", "-"));
				else if (generator.getOntology().containsDataPropertyInSignature(
						generator.getPm().getIRI(StringUtils.uncapitalize(entityName))))
					entity = generator.getOWLDataPropertyFromName(entityName.replace("/", "-"));
				else if (generator.getOntology().containsIndividualInSignature(generator.getPm().getIRI(entityName)))
					entity = (OWLEntity) generator.getOWLIndividualFromName(entityName.replace("/", "-"));
				else
					entity = null;

				if (entity != null) {
					annotation = factory.getOWLAnnotation(factory.getRDFSComment(),
							factory.getOWLLiteral(p_.second().toString(), "en"));
					axiom = factory.getOWLAnnotationAssertionAxiom(entity.getIRI(), annotation);
					axiomSet.add(axiom);
				}
			}
			return axiomSet;
		} else
			return null;
	}

	/**
	 * Gets the attribute rule(s) (value restriction axioms) from a given preprocessed "takes value" footnotes set.
	 *
	 * @param pairSet, the preprocessed "takes value" footnotes set
	 * @param generator, the Core OWL generator instance
	 * @return the value restriction axiom(s) set
	 */
	private Set<OWLAxiom> getFootNoteAttributeRules(Set<Pair<String, Object>> pairSet, Core generator) {
		if (pairSet.size() > 0) {
			OWLDataFactory factory = generator.getManager().getOWLDataFactory();
			Set<OWLAxiom> axiomSet = new HashSet<OWLAxiom>();

			for (Pair<String, Object> p_ : pairSet) {
				@SuppressWarnings("unchecked")
				Pair<String, String> innerPair = (Pair<String, String>) p_.second();

				// Extract the elements
				String attribute = p_.first();
				String domain = innerPair.first();
				String value = innerPair.second();

				// Create the restrictions (Axioms)
				if (generator.getOntology().containsDataPropertyInSignature(generator.getPm().getIRI(attribute))) {
					String[] domainClassess = domain.split(",");
					for (String domainClass_ : domainClassess) {
						if (generator.getOntology()
								.containsClassInSignature(generator.getPm().getIRI(domainClass_.replace(" ", "")))) {
							OWLRestriction hasValue = null;
							OWLSubClassOfAxiom attributeValueRestrictionAxiom = null;
							OWLDataProperty dataProperty = generator.getOWLDataPropertyFromName(attribute);
							OWLClass domainClass = generator.getOWLClassFromName(domainClass_);

							// Value
							if (StringUtils.upperCase(value, Locale.ENGLISH).equals(value))
								// All-Capital literals, separate with "_"
								value = value.replace(" ", "_");
							else
								value = value.replace(" ", "");

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

								// Multi-valued processing
								if (matcherValues.matches()) {
									String valuesString = matcherValues.group(1).replaceAll(" ", "");
									String[] values = valuesString.split("\\,");
									for (String value_ : values)
										valuesSet.add(factory.getOWLLiteral(value_));

									OWLDataOneOf valuesRange = null;
									if (valuesSet.size() != 0) {
										if (valuesSet.size() > 1) {
											valuesRange = factory.getOWLDataOneOf(valuesSet);
											// OWLDataSomeValuesFrom some =
											// factory.getOWLDataSomeValuesFrom(dataProperty,
											// valuesRange);
											hasValue = factory.getOWLDataExactCardinality(valuesSet.size(),
													dataProperty, valuesRange);
										}
										// Si por error se ha especificado un
										// conjunto con un solo elemento
										else
											hasValue = factory.getOWLDataHasValue(dataProperty,
													valuesSet.iterator().next());
										valuesSet.clear();
									}
								}
								// Single value processing
								else
									hasValue = factory.getOWLDataHasValue(dataProperty, factory.getOWLLiteral(value));
							}
							if (hasValue != null) {
								attributeValueRestrictionAxiom = factory.getOWLSubClassOfAxiom(domainClass, hasValue);
								axiomSet.add(attributeValueRestrictionAxiom);
							}
						}
					}
				}
			}
			return axiomSet;
		} else
			return null;
	}

	/**
	 * Gets the foot note equivalent (synonymy) axioms from a given preprocessed "also referred" footnotes set.
	 *
	 * @param pairSet, the preprocessed "also referred" footnotes set
	 * @param generator, the Core OWL generator instance
	 * @return the equivalent axioms set
	 */
	private Set<OWLAxiom> getFootNoteEquivalentAxioms(Set<Pair<String, Object>> pairSet, Core generator) {
		if (pairSet.size() > 0) {
			Set<OWLAxiom> axiomSet = new HashSet<OWLAxiom>();
			for (Pair<String, Object> p_ : pairSet) {
				String entityName = null;
				if (StringUtils.upperCase(p_.first(), Locale.ENGLISH).equals(p_.first()))
					// All-Capital literals, separate with "_"
					entityName = p_.first().replace(" ", "_");
				else
					entityName = p_.first().replace(" ", "");

				// Determine type
				if (generator.getOntology()
						.containsClassInSignature(generator.getPm().getIRI(entityName.replace("/", "-"))))
					axiomSet.addAll(generator.getEquivalentClassesAxioms(p_.first(), p_.second().toString()));
				else if (generator.getOntology().containsObjectPropertyInSignature(
						generator.getPm().getIRI(StringUtils.uncapitalize(entityName))))
					axiomSet.addAll(generator.getEquivalentObjectPropertyAxioms(p_.first(), p_.second().toString()));
				else if (generator.getOntology().containsDataPropertyInSignature(
						generator.getPm().getIRI(StringUtils.uncapitalize(entityName))))
					axiomSet.addAll(generator.getEquivalentDataPropertyAxioms(p_.first(), p_.second().toString()));
				else if (generator.getOntology().containsIndividualInSignature(generator.getPm().getIRI(entityName)))
					axiomSet.addAll(generator.getEquivalentIndividualsAxioms(p_.first(), p_.second().toString()));
				else
					return null;
			}
			return axiomSet;
		} else
			return null;
	}

	/**
	 * Adds the axioms generated from analyzing the footnotes.
	 *
	 * @param tableFileName, the footnotes table file name
	 * @param generator, the Core OWL generator instance
	 */
	public void addAxiomsFromFootNotes(String tableFileName, Core generator) {
		// Classify the FootNotes
		classifyFootNotes(tableFileName);

		// PREPROCESSING: Find Entities and sanitize text.
		Set<Pair<String, Object>> preSubclassOf = null, preSeeAlso = null, preComment = null, preAlsoReferred = null,
				preTakesValue = null;
		if (subclassOfFNs.size() > 0)
			preSubclassOf = preprocessFootNotes(subclassOfFNs, tableFileName, generator);
		if (seeAlsoFNs.size() > 0)
			preSeeAlso = preprocessFootNotes(seeAlsoFNs, tableFileName, generator);
		if (commentFNs.size() > 0)
			preComment = preprocessFootNotes(commentFNs, tableFileName, generator);
		if (alsoReferredFNs.size() > 0) {
			// preAlsoReferred will contain the rules for synonyms and seeAlso
			// notes, prior to obtaining
			// the axioms must be separated, adding seeAlso notes if any, to the
			// preSeeAlso notes set
			preAlsoReferred = preprocessFootNotes(alsoReferredFNs, tableFileName, generator);
			// Pull apart notes that also contain a seeAlso {"See"|"see"} note
			// within its text.
			Iterator<Pair<String, Object>> iterator = preAlsoReferred.iterator();
			while (iterator.hasNext()) {
				Pair<String, Object> p_ = iterator.next();
				if (p_.second().toString().contains("See ") || p_.second().toString().contains("see ")) {
					preSeeAlso.add(p_);
					iterator.remove();
				}
			}
		}
		if (takesValueFNs.size() > 0)
			preTakesValue = preprocessFootNotes(takesValueFNs, tableFileName, generator);

		// PROCESSING: Getting the axioms
		Set<OWLAxiom> subclassAxioms = null, seeAlsoAxioms = null, commentAxioms = null, synonymAxioms = null,
				hasValueAxioms = null;
		if (preSubclassOf != null)
			subclassAxioms = getFootNoteSubclassOfAxioms(preSubclassOf, generator);
		if (preSeeAlso != null)
			seeAlsoAxioms = getFootNoteSeeAlsoAxioms(preSeeAlso, generator);
		if (preComment != null)
			commentAxioms = getFootNoteCommentAxioms(preComment, generator);
		if (preAlsoReferred != null)
			synonymAxioms = getFootNoteEquivalentAxioms(preAlsoReferred, generator);
		if (preTakesValue != null)
			hasValueAxioms = getFootNoteAttributeRules(preTakesValue, generator);

		// Join the sets and add the axioms to the ontology
		Set<OWLAxiom> allAxioms = new HashSet<OWLAxiom>();
		if (subclassAxioms != null)
			allAxioms.addAll(subclassAxioms);
		if (seeAlsoAxioms != null)
			allAxioms.addAll(seeAlsoAxioms);
		if (commentAxioms != null)
			allAxioms.addAll(commentAxioms);
		if (synonymAxioms != null)
			allAxioms.addAll(synonymAxioms);
		if (hasValueAxioms != null)
			allAxioms.addAll(hasValueAxioms);
		generator.getManager().addAxioms(generator.getOntology(), allAxioms);
	}
}
