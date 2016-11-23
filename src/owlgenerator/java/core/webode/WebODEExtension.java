/*
 * The Class WebODEExtension extends Core to process WebODE formatted source tables.
 */
package owlgenerator.java.core.webode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataAllValuesFrom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLDatatypeRestriction;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectCardinalityRestriction;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyCharacteristicAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.vocab.XSDVocabulary;

import owlgenerator.java.core.Core;
import owlgenerator.java.core.CoreConfiguration;

/**
 * The Class WebODEExtension extends Core to process WebODE formatted source tables.
 * 
 * @author Oscar Mateos Lopez
 * @version: 20161103
 */
public class WebODEExtension extends Core {
	// GLOSSARY -> NAME, LABEL, SYNONYMS, DESCRIPTION 
	/**
	 * Adds the concepts (OWL Classes) details, such as name; label; synonyms and description from WebODE glossary tables to the ontology.
	 *
	 * @param tableFileName, the Glossary-Concepts table file name
	 */
	// Concepts -> Classes
	public void addConceptsFromGlossary(String tableFileName) {
		OWLDataFactory factory = this.getManager().getOWLDataFactory();
		String fileLine = null;
		Set<OWLAxiom> axiomSet = new HashSet<OWLAxiom>();
		try {
			File fp = new File(this.getCfg().getFiles_Path() + "\\" + tableFileName);
			FileReader inputFile = new FileReader(fp);
			LineNumberReader lnr = new LineNumberReader(inputFile);
			int nameCol = -1, synonymsCol = -1, descriptionCol = -1;
			
			while((fileLine = lnr.readLine())!=null) {
				String[] parts = fileLine.split(CoreConfiguration.SEPARATOR);			
				// Header
				if (lnr.getLineNumber() == 2) {
					// Infer column order (array indexes)
					for (int i = 0; i < parts.length; i++) {
						String part = parts[i];
						if (StringUtils.containsIgnoreCase(part, "NAME")) 
							nameCol = i;
						else if (StringUtils.containsIgnoreCase(part, "SYNONYM")) 
							synonymsCol = i;
						else if (StringUtils.containsIgnoreCase(part, "DESCRIPTION")) 
							descriptionCol = i;
					}
				}				
				// Data
				else if (lnr.getLineNumber() > 2) {
					if (nameCol != -1) {
						//Name
						String name = parts[nameCol];
						OWLClass clase = this.getOWLClassFromName(name);
						OWLDeclarationAxiom declaration = factory.getOWLDeclarationAxiom(clase);
						this.getManager().addAxiom(this.getOntology(), declaration);
						
						// Label
						OWLAnnotation labelName = factory.getOWLAnnotation(factory.getRDFSLabel() , factory.getOWLLiteral(name, "en"));
						OWLAxiom axiom = factory.getOWLAnnotationAssertionAxiom(clase.getIRI() , labelName);
						axiomSet.add(axiom);	
						
						//Synonyms
						if (synonymsCol != -1) {
							String synonyms = parts[synonymsCol];	
							if (!synonyms.equals(CoreConfiguration.NOTHING))
								axiomSet.addAll(this.getEquivalentClassesAxioms(name, synonyms.replace(CoreConfiguration.INNER_SEPARATOR, ",")));
						}						
						//Description
						if (descriptionCol != -1) {
							String description = parts[descriptionCol];
							
							if (!description.equals(CoreConfiguration.NOTHING)) {
								OWLAnnotation commentName = factory.getOWLAnnotation(factory.getRDFSComment(), factory.getOWLLiteral(description, "en"));
								axiom = factory.getOWLAnnotationAssertionAxiom(clase.getIRI() , commentName);
								axiomSet.add(axiom);
							}
						}
					}
				}		
			}	
			lnr.close();
			this.getManager().addAxioms(this.getOntology(), axiomSet);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	
	
	/**
	 * Adds the attributes (OWL Data Properties) details, such as name; label; synonyms and description from WebODE glossary tables to the ontology.
	 *
	 * @param tableFileName, the Glossary-Attributes table file name
	 */	
	// Class/Instance Attributes -> Data Properties
	public void addAttributesFromGlosary(String tableFileName) {
		OWLDataFactory factory = this.getManager().getOWLDataFactory();
		String fileLine = null;
		Set<OWLAxiom> axiomSet = new HashSet<OWLAxiom>();
		try {
			File fp = new File(this.getCfg().getFiles_Path() + "\\" + tableFileName);
			FileReader inputFile = new FileReader(fp);
			LineNumberReader lnr = new LineNumberReader(inputFile);
			int nameCol = -1, synonymsCol = -1, descriptionCol = -1;
			
			while((fileLine = lnr.readLine())!=null) {
				String[] parts = fileLine.split(CoreConfiguration.SEPARATOR);		
				// Header
				if (lnr.getLineNumber() == 2) {
					// Infer column order (array indexes)
					for (int i = 0; i < parts.length; i++) {
						String part = parts[i];
						if (StringUtils.containsIgnoreCase(part, "NAME")) 
							nameCol = i;
						else if (StringUtils.containsIgnoreCase(part, "SYNONYM")) 
							synonymsCol = i;
						else if (StringUtils.containsIgnoreCase(part, "DESCRIPTION")) 
							descriptionCol = i;
					}
				}			
				// Data
				else if (lnr.getLineNumber() > 2) {
					if (nameCol != -1) {
						// Name
						String attribute = parts[nameCol];							
						OWLDataProperty dataProperty = null; 
						Pattern pattern = Pattern.compile("(\\w*\\s*\\w+)\\s*:\\s*(\\w+)" + CoreConfiguration.INNER_SEPARATOR + "{0,1}");
						Matcher matcher = pattern.matcher(attribute);
						OWLAnnotation labelName = null;
						
						// CLASS ATTRIBUTES
						if (matcher.find()) {
							String name = matcher.group(2);						
							dataProperty = this.getOWLDataPropertyFromName(name);
							labelName = factory.getOWLAnnotation(factory.getRDFSLabel() , factory.getOWLLiteral(name, "en"));
						}
						// INSTANCE ATTRIBUTES
						else {
							dataProperty = this.getOWLDataPropertyFromName(attribute); 
							labelName = factory.getOWLAnnotation(factory.getRDFSLabel() , factory.getOWLLiteral(attribute, "en"));
						}
						// Label
						OWLAxiom axiom = factory.getOWLAnnotationAssertionAxiom(dataProperty.getIRI() , labelName);
						axiomSet.add(axiom);
						
						//Synonyms
						if (synonymsCol != -1) {
							String synonyms = parts[synonymsCol];	
							if (!synonyms.equals(CoreConfiguration.NOTHING))
								axiomSet.addAll(this.getEquivalentDataPropertyAxioms(attribute, synonyms.replace(CoreConfiguration.INNER_SEPARATOR, ",")));
						}						
						//Description
						if (descriptionCol != -1) {
							String description = parts[descriptionCol];	
							if (!description.equals(CoreConfiguration.NOTHING)) {
								OWLAnnotation commentName = factory.getOWLAnnotation(factory.getRDFSComment(), factory.getOWLLiteral(description, "en"));
								axiom = factory.getOWLAnnotationAssertionAxiom(dataProperty.getIRI() , commentName);
								axiomSet.add(axiom);
							}
						}						
					}
				}	
			}		
			lnr.close();
			this.getManager().addAxioms(this.getOntology(), axiomSet);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Adds the ad-Hoc Relations (OWL Object Properties) details, such as name; label; synonyms and description from WebODE glossary tables to the ontology.
	 *
	 * @param tableFileName, the Glossary-AdHocRelations table file name
	 */	
	// Ad-Hoc Relations -> Object Properties
	public void addAdHocRelationsFromGlosary(String tableFileName) {
		OWLDataFactory factory = this.getManager().getOWLDataFactory();
		String fileLine = null;
		Set<OWLAxiom> axiomSet = new HashSet<OWLAxiom>();
		try {
			File fp = new File(this.getCfg().getFiles_Path() + "\\" + tableFileName);
			FileReader inputFile = new FileReader(fp);
			LineNumberReader lnr = new LineNumberReader(inputFile);
			int nameCol = -1, synonymsCol = -1, descriptionCol = -1;
			
			while((fileLine = lnr.readLine())!=null) {
				String[] parts = fileLine.split(CoreConfiguration.SEPARATOR);			
				// Header
				if (lnr.getLineNumber() == 2) {
					// Infer column order (array indexes)
					for (int i = 0; i < parts.length; i++) {
						String part = parts[i];
						if (StringUtils.containsIgnoreCase(part, "NAME")) 
							nameCol = i;
						else if (StringUtils.containsIgnoreCase(part, "SYNONYM")) 
							synonymsCol = i;
						else if (StringUtils.containsIgnoreCase(part, "DESCRIPTION")) 
							descriptionCol = i;
					}
				}				
				// Data
				else if (lnr.getLineNumber() > 2)  {
					if (nameCol != -1) {
						//Name
						String relation = parts[nameCol];
						String name = null; 
						Pattern pattern = Pattern.compile("(\\w+)\\s*\\((\\w+\\-*[\\s\\w+]*)\\,\\s(\\w+\\-*[\\s\\w+]*)\\)");
						Matcher matcher = pattern.matcher(relation);
						if (matcher.matches()) 
							name = matcher.group(1);
						OWLObjectProperty objectProp = this.getOWLObjectPropertyFromName(name);

						// Label
						OWLAnnotation labelName = factory.getOWLAnnotation(factory.getRDFSLabel() , factory.getOWLLiteral(name, "en"));
						OWLAxiom axiom = factory.getOWLAnnotationAssertionAxiom(objectProp.getIRI() , labelName);
						axiomSet.add(axiom);	
						
						//Synonyms
						if (synonymsCol != -1) {
							String synonyms = parts[synonymsCol];	
							if (!synonyms.equals(CoreConfiguration.NOTHING))
								axiomSet.addAll(this.getEquivalentObjectPropertyAxioms(relation, synonyms.replace(CoreConfiguration.INNER_SEPARATOR, ",")));
						}
						//Description
						if (descriptionCol != -1) {
							String description = parts[descriptionCol];					
							if (!description.equals(CoreConfiguration.NOTHING)) {
								OWLAnnotation commentName = factory.getOWLAnnotation(factory.getRDFSComment(), factory.getOWLLiteral(description, "en"));
								axiom = factory.getOWLAnnotationAssertionAxiom(objectProp.getIRI() , commentName);
								axiomSet.add(axiom);
							}
						}						
					}
				}	
			}		
			lnr.close();
			this.getManager().addAxioms(this.getOntology(), axiomSet);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	
					
	/**
	 * Adds the instances (OWL Individuals) details, such as name; label; synonyms and description from WebODE glossary tables to the ontology.
	 *
	 * @param tableFileName, the Glossary-Individuals table file name
	 */
	// Instances -> Individuals
	public void addInstancesFromGlossary(String tableFileName) {
		OWLDataFactory factory = this.getManager().getOWLDataFactory();
		String fileLine = null;
		try {
			File fp = new File(this.getCfg().getFiles_Path() + "\\" + tableFileName);
			FileReader inputFile = new FileReader(fp);
			LineNumberReader lnr = new LineNumberReader(inputFile);
			int nameCol = -1, synonymsCol = -1, descriptionCol = -1;
			
			while((fileLine = lnr.readLine())!=null) {
				String[] parts = fileLine.split(CoreConfiguration.SEPARATOR);			
				// Header
				if (lnr.getLineNumber() == 2) {
					// Infer column order (array indexes)
					for (int i = 0; i < parts.length; i++) {
						String part = parts[i];
						if (StringUtils.containsIgnoreCase(part, "NAME")) 
							nameCol = i;
						else if (StringUtils.containsIgnoreCase(part, "SYNONYM")) 
							synonymsCol = i;
						else if (StringUtils.containsIgnoreCase(part, "DESCRIPTION")) 
							descriptionCol = i;
					}
				}					
				// Data
				else if ((lnr.getLineNumber() > 2)) {
					if (nameCol != -1) {
						//Name
						String individualName = parts[nameCol];		
						OWLIndividual namedIndividual = getOWLIndividualFromName(individualName);						
						
						// Label
						OWLAnnotation labelName = factory.getOWLAnnotation(factory.getRDFSLabel() , factory.getOWLLiteral(individualName, "en"));
						OWLAxiom axiom = factory.getOWLAnnotationAssertionAxiom(namedIndividual.asOWLNamedIndividual().getIRI() , labelName);
						this.getManager().applyChange(new AddAxiom(this.getOntology(), axiom));									

						//Synonyms
						if (synonymsCol != -1) {
							String synonyms = parts[synonymsCol];	
							if (!synonyms.equals(CoreConfiguration.NOTHING))
							    this.getManager().addAxioms(this.getOntology(), this.getEquivalentIndividualsAxioms(individualName, synonyms.replace(CoreConfiguration.INNER_SEPARATOR, ",")));
						}
						//Description
						if (descriptionCol != -1) {
							String description = parts[descriptionCol];							
							if (!description.equals(CoreConfiguration.NOTHING)) {
								OWLAnnotation commentName = factory.getOWLAnnotation(factory.getRDFSComment(), factory.getOWLLiteral(description, "en"));
								axiom = factory.getOWLAnnotationAssertionAxiom(namedIndividual.asOWLNamedIndividual().getIRI() , commentName);
								this.getManager().applyChange(new AddAxiom(this.getOntology(), axiom));
							}
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
	 * Adds the Instace or Class attributes (OWL Data Properties) details such as domain, range, value-range and cardinality from the Instace or Class attributes WebODE formatted table to the ontology.
	 *
	 * @param tableFileName, the table file name
	 */
	// Instance Attribute Table -> Domain, Range, Value Range, Cardinality
	// Class Attribute Table -> Domain, Range, Attribute Rules (Value Restrictions), Cardinality
	public void addAttributeAxiomsFromTable(String tableFileName) {
		String fileLine = null;
		Set<String> attributeNames = new HashSet<String>();
		try {
			File fp = new File(this.getCfg().getFiles_Path() + "\\" + tableFileName);
			FileReader inputFile = new FileReader(fp);
			LineNumberReader lnr = new LineNumberReader(inputFile);
			int nameCol = -1, valuesCol = -1;
			
			while((fileLine = lnr.readLine())!=null) {
				String[] parts = fileLine.split(CoreConfiguration.SEPARATOR);			
				// Header
				if (lnr.getLineNumber() == 2) {
					// Infer column order (array indexes)
					for (int i = 0; i < parts.length; i++) {
						String part = parts[i];
						if (StringUtils.containsIgnoreCase(part, "ATTRIBUTE NAME")) 
							nameCol = i;
						else if (StringUtils.containsIgnoreCase(part, "VALUES")) 
							valuesCol = i;
					}
				}				
				// Data
				else if ((lnr.getLineNumber() > 2) && (nameCol != -1)) {
					//Name
					String name = parts[nameCol];
					attributeNames.add(name);
				}		
			}			
			lnr.close();
			
			for (String attributeName : attributeNames) {
				//Domain & Range
				OWLDataPropertyDomainAxiom domainAxiom = getAttributeDomainFromTable(tableFileName, attributeName);
				if (domainAxiom != null)
					this.getManager().applyChange(new AddAxiom(this.getOntology(), domainAxiom));			
				OWLDataPropertyRangeAxiom rangeAxiom = getAttributeRangeFromTable(tableFileName, attributeName);
				if (rangeAxiom != null)
					this.getManager().applyChange(new AddAxiom(this.getOntology(), rangeAxiom));

				//Cardinality
				OWLSubClassOfAxiom cardinality = getAttributeCardinalityFromTable(tableFileName, attributeName, domainAxiom, rangeAxiom);
				if (cardinality != null)
					this.getManager().applyChange(new AddAxiom(this.getOntology(), cardinality));
				
				//Attribute Rules (Value Restrictions)
				if (valuesCol != -1)
					this.addAttributeRulesFromTable(tableFileName);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

	/**
	 * Gets the domain axiom for the given attribute (OWL Data Property) from attributes WebODE formatted table.
	 *
	 * @param tableFileName, the attributes WebODE formatted table file name
	 * @param attributeName, the attribute name
	 * @return the attribute domain axiom
	 */
	public OWLDataPropertyDomainAxiom getAttributeDomainFromTable(String tableFileName, String attributeName) {
		OWLDataFactory factory = this.getManager().getOWLDataFactory();
		String fileLine = null;
		Set<OWLClass> domains = new HashSet<OWLClass>();		
		try {
			File fp = new File(this.getCfg().getFiles_Path() + "\\" + tableFileName);		
			FileReader inputFile = new FileReader(fp);
			LineNumberReader lnr = new LineNumberReader(inputFile);
			int nameCol = -1, domainCol = -1;
			OWLDataProperty dataProperty = this.getOWLDataPropertyFromName(attributeName); 
			
			while((fileLine = lnr.readLine())!=null) {
				String[] parts = fileLine.split(CoreConfiguration.SEPARATOR);			
				// Header
				if ((lnr.getLineNumber() == 2)) {
					// Infer column order (array indexes)
					for (int i = 0; i < parts.length; i++) {
						String part = parts[i];
						if (StringUtils.containsIgnoreCase(part, "ATTRIBUTE NAME")) 
							nameCol = i;
						else if (StringUtils.containsIgnoreCase(part, "CONCEPT NAME")) 
							domainCol = i;
					}
				}				
				// Data
				else if (lnr.getLineNumber() > 2 && (nameCol != -1) && (domainCol != -1)) {
					String currentAttribute = parts[nameCol].trim();				
					if (currentAttribute.equals(attributeName)) {
						String domain = parts[domainCol].trim();		
						OWLClass domainClass = this.getOWLClassFromName(domain);
						domains.add(domainClass);
					}
				}			
			}			
			lnr.close();
			
			OWLObjectUnionOf unionOf = null;
			OWLDataPropertyDomainAxiom domainAxiom = null;			
			if (domains.size() > 1) {
				unionOf = factory.getOWLObjectUnionOf(domains);
				domainAxiom = factory.getOWLDataPropertyDomainAxiom(dataProperty, unionOf);
			}
			else if (domains.size() == 1)
				domainAxiom = factory.getOWLDataPropertyDomainAxiom(dataProperty, domains.iterator().next() );									
			return domainAxiom;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}				
	}	
	
	/**
	 * Gets the range axiom for the given attribute (OWL Data Property) from attributes WebODE formatted table.
	 *
	 * @param tableFileName, the attributes WebODE formatted table file name
	 * @param attributeName, the attribute name
	 * @return the attribute range axiom
	 */	
	public OWLDataPropertyRangeAxiom getAttributeRangeFromTable(String tableFileName, String attributeName) {
		OWLDataFactory factory = this.getManager().getOWLDataFactory();
		String fileLine = null;
		Set<OWLLiteral> valuesSet = new HashSet<OWLLiteral>();
		Boolean exit = false;		
		try {
			File fp = new File(this.getCfg().getFiles_Path() + "\\" + tableFileName);		
			FileReader inputFile = new FileReader(fp);
			LineNumberReader lnr = new LineNumberReader(inputFile);
			int nameCol = -1, valueTypeCol = -1, valueRangeCol = -1, valuesCol = -1;		
			OWLDataProperty dataProperty = this.getOWLDataPropertyFromName(attributeName);
			String valueType = null;
			OWLDataRange dataType = null;
			
			while((fileLine = lnr.readLine()) != null && (!exit)) {
				String[] parts = fileLine.split(CoreConfiguration.SEPARATOR);			
				// Header
				if ((lnr.getLineNumber() == 2)) {
					// Infer column order (array indexes)
					for (int i = 0; i < parts.length; i++) {
						String part = parts[i];
						if (StringUtils.containsIgnoreCase(part, "ATTRIBUTE NAME")) 
							nameCol = i;
						else if (StringUtils.containsIgnoreCase(part, "VALUE TYPE")) 
							valueTypeCol = i;
						else if (StringUtils.containsIgnoreCase(part, "VALUE RANGE")) 
							valueRangeCol = i;
						else if (StringUtils.containsIgnoreCase(part, "VALUES")) 
							valuesCol = i;
					}
				}			
				// Data
				else if (lnr.getLineNumber() > 2 && (nameCol != -1) && (valueTypeCol != -1)) {
					String currentAttribute = parts[nameCol].trim();			
					if (currentAttribute.equals(attributeName)) {
						valueType = parts[valueTypeCol].trim().toLowerCase();				
						switch (valueType) {
						case ("boolean"):
							dataType = factory.getBooleanOWLDatatype();
							exit = true;
							break;		
							
						case ("cardinal"):
							// INSTANCE ATTRIBUTES
							if (valueRangeCol != -1) {							
								// If Value Range column belongs to the table, range definition is included
								String valueRange = parts[valueRangeCol].trim();		
								if (valueRange.compareTo(CoreConfiguration.NOTHING) != 0) {	
									//xsd:integer comes implicit for type of argument
									dataType = getValueRangeRestrictionFromLiteral(valueRange);
									exit = true;
									break;
								}
							}					
							dataType = factory.getIntegerOWLDatatype();							
							exit = true;
							break;
							
						case ("date"):
							dataType = factory.getOWLDatatype(XSDVocabulary.parseShortName("xsd:date").getIRI());
							exit = true;
							break;
							
						case ("string"):
							// CLASS ATTRIBUTES
							if (valuesCol != -1) {
								String[] values =  parts[valuesCol].trim().split(CoreConfiguration.INNER_SEPARATOR);
								for (String value : values) {
									valuesSet.add(factory.getOWLLiteral(value));
								}
								break;
							}
							else {
								dataType = factory.getOWLDatatype(XSDVocabulary.parseShortName("xsd:string").getIRI());
								exit = true;
								break;
							}
						
						case ("url"):
							dataType = factory.getOWLDatatype(XSDVocabulary.parseShortName("xsd:anyURI").getIRI());
							exit = true;
							break;
						}
					}
				}			
			}		
			lnr.close();
			
			// Generate the range with the literal values
			if (valueType.equalsIgnoreCase("string") && (valuesCol != -1))
				dataType = factory.getOWLDataOneOf(valuesSet);
			return factory.getOWLDataPropertyRangeAxiom(dataProperty, dataType);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}		
	}	

	/**
	 * Gets the value range restriction from a given literal.
	 *
	 * @param literalValueRange, the literal value range
	 * @return the value range restriction
	 */
	// Restrictions for Value Range column from Instance Attributes Table
	public OWLDatatypeRestriction getValueRangeRestrictionFromLiteral(String literalValueRange){
		OWLDatatypeRestriction valueRangeRestriction = null;	
		if (!literalValueRange.equals(CoreConfiguration.NOTHING)) {
			OWLDataFactory factory = this.getManager().getOWLDataFactory();
			String lowerBound = null, upperBound = null;
			Pattern pattern = Pattern.compile("(\\d+)\\s+\\.{2}\\s+(\\d+)");
			Matcher matcher = pattern.matcher(literalValueRange);
			if (matcher.matches()) {
				lowerBound = matcher.group(1);
				upperBound = matcher.group(2);
			}	
			valueRangeRestriction = factory.getOWLDatatypeMinMaxInclusiveRestriction(Integer.parseInt(lowerBound), Integer.parseInt(upperBound));	
		} 
		return valueRangeRestriction;
	}
	
	/**
	 * Gets the attribute cardinality from the attributes WebODE formatted table file.
	 *
	 * @param tableFileName, the attributes WebODE formatted table file name
	 * @param attributeName, the attribute name
	 * @param domainAxiom, the domain axiom for the given attribute
	 * @param rangeAxiom, the range axiom for the given attribute
	 * @return the attribute cardinality axiom
	 */
	public OWLSubClassOfAxiom getAttributeCardinalityFromTable(String tableFileName, String attributeName, OWLDataPropertyDomainAxiom domainAxiom, OWLDataPropertyRangeAxiom rangeAxiom) {
		OWLDataFactory factory = this.getManager().getOWLDataFactory();
		String fileLine = null;	
		try {
			File fp = new File(this.getCfg().getFiles_Path() + "\\" + tableFileName);		
			FileReader inputFile = new FileReader(fp);
			LineNumberReader lnr = new LineNumberReader(inputFile);
			int nameCol = -1, cardinalityCol = -1;	
			OWLDataProperty dataProperty = this.getOWLDataPropertyFromName(attributeName); 
			String literalCardinality = null;
			
			// Get the literal with cardinality
			while((fileLine = lnr.readLine())!=null) {
				String[] parts = fileLine.split(CoreConfiguration.SEPARATOR);		
				// Header
				if ((lnr.getLineNumber() == 2)) {
					// Infer column order (array indexes)
					for (int i = 0; i < parts.length; i++) {
						String part = parts[i];
						if (StringUtils.containsIgnoreCase(part, "ATTRIBUTE NAME")) 
							nameCol = i;
						else if (StringUtils.containsIgnoreCase(part, "CARDINALITY")) 
							cardinalityCol = i;
					}
				}					
				// Data
				else if (lnr.getLineNumber() > 2 && (nameCol != -1) && (cardinalityCol != -1)) {
					String currentAttribute = parts[nameCol].trim();				
					if (currentAttribute.equals(attributeName)) {
						literalCardinality = parts[cardinalityCol];
						break;
					}
				}			
			}		
			lnr.close();
			
			// Gettning the axiom
			String lowerBound = null, upperBound = null;
			OWLSubClassOfAxiom subclassOfAxiom = null;
			Pattern pattern = Pattern.compile("\\((\\d+)\\,\\s*(\\d+|n|N)\\)");
			Matcher matcher = pattern.matcher(literalCardinality);
		
			if (matcher.matches()) {
				lowerBound = matcher.group(1);
				upperBound = matcher.group(2);
				
				//VALUE CONSTRAINTS
				if (upperBound.compareToIgnoreCase("n") == 0) {
					// (1, n): SomeValuesFrom
					if (lowerBound.compareTo("1") == 0){
						OWLDataSomeValuesFrom someValuesFrom = factory.getOWLDataSomeValuesFrom(dataProperty, rangeAxiom.getRange());
						subclassOfAxiom = factory.getOWLSubClassOfAxiom(domainAxiom.getDomain(), someValuesFrom);
						return subclassOfAxiom;
					}
					// (0, n): AllValuesFrom 
					else if (lowerBound.compareTo("0") == 0) {
						OWLDataAllValuesFrom allValuesFrom = factory.getOWLDataAllValuesFrom(dataProperty, rangeAxiom.getRange());
						subclassOfAxiom = factory.getOWLSubClassOfAxiom(domainAxiom.getDomain(), allValuesFrom);
						return subclassOfAxiom;
					}					
				}			
				//CARDINALITY CONSTRAINTS	
				// (0, 1) and other numeric:		
				else if (NumberUtils.isNumber(lowerBound) && NumberUtils.isNumber(upperBound)) {
					HashSet<OWLClassExpression> cardinalities = new HashSet<OWLClassExpression>();
					int lower = Integer.parseInt(lowerBound);
					int upper = Integer.parseInt(upperBound);
					
					if (lower != upper) {
						// MinCardinality
						OWLClassExpression minCardinality = factory.getOWLDataMinCardinality(lower, dataProperty);
						cardinalities.add(minCardinality);
						// MaxCardinality
						OWLClassExpression maxCardinality = factory.getOWLDataMaxCardinality(upper, dataProperty);
						cardinalities.add(maxCardinality);	
						// Intersection
						OWLObjectIntersectionOf intersectionOf = factory.getOWLObjectIntersectionOf (cardinalities);
						subclassOfAxiom = factory.getOWLSubClassOfAxiom(domainAxiom.getDomain(), intersectionOf);
					}
					else {
						// ExactCardinality
						OWLClassExpression exactCardinality = factory.getOWLDataExactCardinality(lower, dataProperty);
						subclassOfAxiom = factory.getOWLSubClassOfAxiom(domainAxiom.getDomain(), exactCardinality);
					}
				}
			}
			return subclassOfAxiom;				
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	// Rules (Data Property restrictions)
	// Use Core.getAttributeRulesFromTable(String tableFileName)
	
	/**
	 * Adds the ad-hoc relations (OWL Object Properties) details such as domain, range, mathematical properties and cardinality from the adHoc relations WebODE formatted table to the ontology.
	 *
	 * @param tableFileName, the ad hoc relations WebODE formatted table file name
	 */
	// AD HOC BINARY RELATION TABLE -> DOMAIN, RANGE, MATH PROPERTIES, CARDINALITY
	public void addAdHocRelationAxiomsFromTable(String tableFileName) {
		OWLDataFactory factory = this.getManager().getOWLDataFactory();
		String fileLine = null;
		Set<String> relationNames = new HashSet<String>();
		try {
			File fp = new File(this.getCfg().getFiles_Path() + "\\" + tableFileName);
			FileReader inputFile = new FileReader(fp);
			LineNumberReader lnr = new LineNumberReader(inputFile);
			int nameCol = -1;
			
			while((fileLine = lnr.readLine())!=null) {
				String[] parts = fileLine.split(CoreConfiguration.SEPARATOR);
				
				// Header
				if (lnr.getLineNumber() == 2) {
					// Infer column order (array indexes)
					for (int i = 0; i < parts.length; i++) {
						String part = parts[i];
						if (StringUtils.containsIgnoreCase(part, "RELATION NAME")) 
							nameCol = i;
					}
				}			
				// Data
				else if ((lnr.getLineNumber() > 2) && (nameCol != -1)) {
					//Name
					String name = parts[nameCol];
					relationNames.add(name);
				}		
			}	
			lnr.close();
			
			for (String relationName : relationNames) {
				//Domain & Range	
				OWLObjectPropertyDomainAxiom domainAxiom = getAdHocRelationDomainFromTable(tableFileName, relationName);
				if (domainAxiom != null)
					this.getManager().applyChange(new AddAxiom(this.getOntology(), domainAxiom));
				OWLObjectPropertyRangeAxiom rangeAxiom = getAdHocRelationRangeFromTable(tableFileName, relationName);
				if (rangeAxiom != null)
					this.getManager().applyChange(new AddAxiom(this.getOntology(), rangeAxiom));
				
				//Source Cardinality
				OWLObjectCardinalityRestriction cardinalityRestriction = getAdHocRelationCardinalityFromTable(tableFileName, relationName);
				if (cardinalityRestriction != null) {
					OWLObjectProperty objectProp = getOWLObjectPropertyFromName(relationName);
					OWLObjectPropertyDomainAxiom cardinalityDomainRestriciton = factory.getOWLObjectPropertyDomainAxiom(objectProp, cardinalityRestriction);
					this.getManager().applyChange(new AddAxiom(this.getOntology(), cardinalityDomainRestriciton));		
				}
				
				//Math Properties
				OWLObjectPropertyCharacteristicAxiom properties = getAdHocRelationMathPropertiesFromTable(tableFileName, relationName);
				if (properties != null)
					this.getManager().applyChange(new AddAxiom(this.getOntology(), properties));
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	/**
	 * Gets the domain axiom for the given ad hoc relation (OWL Object Property) from ad hoc relations WebODE formatted table.
	 *
	 * @param tableFileName, the ad hoc relations WebODE formatted table file name
	 * @param relationName, the relation name
	 * @return the attribute domain axiom
	 */
	public OWLObjectPropertyDomainAxiom getAdHocRelationDomainFromTable(String tableFileName, String relationName) {
		OWLDataFactory factory = this.getManager().getOWLDataFactory();
		String fileLine = null;
		try {
			File fp = new File(this.getCfg().getFiles_Path() + "\\" + tableFileName);		
			FileReader inputFile = new FileReader(fp);
			LineNumberReader lnr = new LineNumberReader(inputFile);
			int nameCol = -1, domainCol = -1;
			OWLObjectProperty objectProp = this.getOWLObjectPropertyFromName(relationName); 
			OWLObjectPropertyDomainAxiom domainAxiom = null;
			
			while((fileLine = lnr.readLine())!=null) {
				String[] parts = fileLine.split(CoreConfiguration.SEPARATOR);
				
				// Header
				if ((lnr.getLineNumber() == 2)) {
					// Infer column order (array indexes)
					for (int i = 0; i < parts.length; i++) {
						String part = parts[i];
						if (StringUtils.containsIgnoreCase(part, "RELATION NAME")) 
							nameCol = i;
						else if (StringUtils.containsIgnoreCase(part, "SOURCE CONCEPT")) 
							domainCol = i;
					}
				}			
				// Data
				else if (lnr.getLineNumber() > 2 && (nameCol != -1) && (domainCol != -1)) {
					String currentRelation = parts[nameCol].trim();				
					if (currentRelation.equals(relationName)) {
						String domain = parts[domainCol].trim();					
						OWLClass domainClass = this.getOWLClassFromName(domain);
						domainAxiom = factory.getOWLObjectPropertyDomainAxiom(objectProp, domainClass);					
					}
				}			
			}	
			lnr.close();
			return domainAxiom;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}				
	}		

	/**
	 * Gets the range axiom for the given ad hoc relation (OWL Object Property) from ad hoc relations WebODE formatted table.
	 *
	 * @param tableFileName, the ad hoc relations WebODE formatted table file name
	 * @param relationName, the ad hoc relation name
	 * @return the ad hoc relation range axiom
	 */
	public OWLObjectPropertyRangeAxiom getAdHocRelationRangeFromTable(String tableFileName, String relationName) {
		OWLDataFactory factory = this.getManager().getOWLDataFactory();
		String fileLine = null;	
		try {
			File fp = new File(this.getCfg().getFiles_Path() + "\\" + tableFileName);		
			FileReader inputFile = new FileReader(fp);
			LineNumberReader lnr = new LineNumberReader(inputFile);
			int nameCol = -1, rangeCol = -1;
			OWLObjectProperty objectProp = this.getOWLObjectPropertyFromName(relationName); 
			OWLObjectPropertyRangeAxiom rangeAxiom = null;
			
			while((fileLine = lnr.readLine())!=null) {
				String[] parts = fileLine.split(CoreConfiguration.SEPARATOR);		
				// Header
				if ((lnr.getLineNumber() == 2)) {
					// Infer column order (array indexes)
					for (int i = 0; i < parts.length; i++) {
						String part = parts[i];
						if (StringUtils.containsIgnoreCase(part, "RELATION NAME")) 
							nameCol = i;
						else if (StringUtils.containsIgnoreCase(part, "TARGET CONCEPT")) 
							rangeCol = i;
					}
				}			
				// Data
				else if (lnr.getLineNumber() > 2 && (nameCol != -1) && (rangeCol != -1)) {
					String currentRelation = parts[nameCol].trim();					
					if (currentRelation.equals(relationName)) {
						String range = parts[rangeCol].trim();					
						OWLClass rangeClass = this.getOWLClassFromName(range);
						rangeAxiom = factory.getOWLObjectPropertyRangeAxiom(objectProp, rangeClass);					
					}
				}			
			}	
			lnr.close();
			return rangeAxiom;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}				
	}

	/**
	 * Gets the ad hoc relation cardinality from the attributes WebODE formatted table file.
	 *
	 * @param tableFileName, the ad hoc relation WebODE formatted table file name
	 * @param relationName the relation name
	 * @return the ad hoc relation cardinality
	 */
	public OWLObjectCardinalityRestriction getAdHocRelationCardinalityFromTable(String tableFileName, String relationName) {
		OWLDataFactory factory = this.getManager().getOWLDataFactory();
		String fileLine = null;		
		try {
			File fp = new File(this.getCfg().getFiles_Path() + "\\" + tableFileName);		
			FileReader inputFile = new FileReader(fp);
			LineNumberReader lnr = new LineNumberReader(inputFile);
			int nameCol = -1, domainCol = -1, cardinalityCol = -1;		
			OWLObjectProperty objectProp = this.getOWLObjectPropertyFromName(relationName); 
			OWLObjectCardinalityRestriction cardinalityRestriction = null;
			
			while((fileLine = lnr.readLine())!=null) {
				String[] parts = fileLine.split(CoreConfiguration.SEPARATOR);			
				// Header
				if ((lnr.getLineNumber() == 2)) {
					// Infer column order (array indexes)
					for (int i = 0; i < parts.length; i++) {
						String part = parts[i];
						if (StringUtils.containsIgnoreCase(part, "RELATION NAME")) 
							nameCol = i;
						else if (StringUtils.containsIgnoreCase(part, "SOURCE CONCEPT")) 
							domainCol = i;
						else if (StringUtils.containsIgnoreCase(part, "SOURCE CARDINALITY")) 
							cardinalityCol = i;
					}
				}			
				// Data
				else if (lnr.getLineNumber() > 2 && (nameCol != -1) && (domainCol != -1) && (cardinalityCol != -1)) {
					String currentRelation = parts[nameCol].trim();			
					if (currentRelation.equals(relationName)) {
						String domain = parts[domainCol].trim();
						OWLClass domainClass = this.getOWLClassFromName(domain);				
						String cardinality = parts[cardinalityCol].trim();
						int cardinalityBound = 0;				
						if(cardinality.trim().toUpperCase().compareTo("N") == 0){
							lnr.close();
							return null;
						}
						else{
							cardinalityBound = Integer.parseInt(cardinality);
							if (cardinalityBound < 0) {
								lnr.close();
								return null;
							}
						}
						cardinalityRestriction = factory.getOWLObjectMaxCardinality(cardinalityBound, objectProp, domainClass);				
					}
				}			
			}			
			lnr.close();
			return cardinalityRestriction;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}				
	}	

	/**
	 * Gets the ad hoc relation math properties from the ad hoc relations WebODE formatted table file.
	 *
	 * @param tableFileName, the ad hoc relation WebODE formatted table file name
	 * @param relationName the relation name
	 * @return the ad hoc relation math properties
	 */
	public OWLObjectPropertyCharacteristicAxiom getAdHocRelationMathPropertiesFromTable(String tableFileName, String relationName) {
		OWLDataFactory factory = this.getManager().getOWLDataFactory();
		String fileLine = null;
		try {
			File fp = new File(this.getCfg().getFiles_Path() + "\\" + tableFileName);		
			FileReader inputFile = new FileReader(fp);
			LineNumberReader lnr = new LineNumberReader(inputFile);
			int nameCol = -1, mathCol = -1;	
			OWLObjectProperty objectProp = this.getOWLObjectPropertyFromName(relationName);
			OWLObjectPropertyCharacteristicAxiom property = null;
			while((fileLine = lnr.readLine())!=null) {
				String[] parts = fileLine.split(CoreConfiguration.SEPARATOR);		
				// Header
				if ((lnr.getLineNumber() == 2)) {
					// Infer column order (array indexes)
					for (int i = 0; i < parts.length; i++) {
						String part = parts[i];
						if (StringUtils.containsIgnoreCase(part, "RELATION NAME")) 
							nameCol = i;
						else if (StringUtils.containsIgnoreCase(part, "MATHEMATIC PROPERTIES")) 
							mathCol = i;
					}
				}			
				// Data
				else if (lnr.getLineNumber() > 2 && (nameCol != -1) && (mathCol != -1)) {
					String currentRelation = parts[nameCol].trim();			
					if (currentRelation.equals(relationName)) {
						if (fileLine.contains(relationName)) {
							String mathProperty = parts[mathCol].trim();				
							switch (mathProperty.trim().toLowerCase()) {
							case ("functional"):
								property = factory.getOWLFunctionalObjectPropertyAxiom(objectProp);
								break;
								
							case ("inverse functional"):
								property = factory.getOWLInverseFunctionalObjectPropertyAxiom(objectProp);		
								break;
								
							case ("inverse-functional"):
								property = factory.getOWLInverseFunctionalObjectPropertyAxiom(objectProp);
								break;
								
							case ("transitive"):
								property = factory.getOWLTransitiveObjectPropertyAxiom(objectProp);	
								break;
								
							case ("symmetrical"):
								property = factory.getOWLSymmetricObjectPropertyAxiom(objectProp);
								break;
								
							case ("symmetric"):
								property = factory.getOWLSymmetricObjectPropertyAxiom(objectProp);						
								break;
						
							case ("asymmetrical"):
								property = factory.getOWLAsymmetricObjectPropertyAxiom(objectProp);
								break;
								
							case ("asymmetric"):
								property = factory.getOWLAsymmetricObjectPropertyAxiom(objectProp);
								break;
								
							case ("reflexive"):
								property = factory.getOWLReflexiveObjectPropertyAxiom(objectProp);						
								break;
								
							case ("irreflexive"):
								property = factory.getOWLIrreflexiveObjectPropertyAxiom(objectProp);
								break;
							}
						}				
					}
				}			
			}				
			lnr.close();
			return property;	
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}				
	}	

	
	/**
	 * Adds the instance class and attribute assertions from WebODE formatted instance table file.
	 *
	 * @param tableFileName, WebODE formatted instance table file name
	 */
	// INSTANCE TABLE -> CLASS AND ATTRIBUTE/RELATION ASSERTIONS	
	public void addInstanceAssertionsFromTable(String tableFileName) {
		String fileLine = null;
		Set<String> instanceNames = new HashSet<String>();
		try {
			File fp = new File(this.getCfg().getFiles_Path() + "\\" + tableFileName);
			FileReader inputFile = new FileReader(fp);
			LineNumberReader lnr = new LineNumberReader(inputFile);
			int nameCol = -1;
			
			while((fileLine = lnr.readLine())!=null) {
				String[] parts = fileLine.split(CoreConfiguration.SEPARATOR);			
				// Header
				if (lnr.getLineNumber() == 2) {
					// Infer column order (array indexes)
					for (int i = 0; i < parts.length; i++) {
						String part = parts[i];
						if (StringUtils.containsIgnoreCase(part, "INSTANCE NAME")) 
							nameCol = i;
					}
				}			
				// Data
				else if ((lnr.getLineNumber() > 2) && (nameCol != -1)) {
					//Name
					String name = parts[nameCol];
					instanceNames.add(name);
				}		
			}	
			lnr.close();		
			for (String instanceName : instanceNames) {
				this.getManager().addAxioms(this.getOntology(), getInstanceClassAssertionsFromTable(tableFileName, instanceName));
				this.getManager().addAxioms(this.getOntology(), getInstanceAttributeAssertionsFromTable(tableFileName, instanceName));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	/**
	 * Gets the instance class assertions from WebODE formatted instance table file for a given instance.
	 *
	 * @param tableFileName, WebODE formatted instance table file name
	 * @param instanceName, the given instance name
	 * @return the instance class assertions set
	 */
	public HashSet<OWLAxiom> getInstanceClassAssertionsFromTable(String tableFileName, String instanceName) {
		OWLDataFactory factory = this.getManager().getOWLDataFactory();
		String fileLine = null;
		HashSet<OWLAxiom> assertions = new HashSet<OWLAxiom>();	
		try {
			File fp = new File(this.getCfg().getFiles_Path() + "\\" + tableFileName);		
			FileReader inputFile = new FileReader(fp);
			LineNumberReader lnr = new LineNumberReader(inputFile);
			int nameCol = -1, conceptCol = -1;		
			OWLIndividual individual = this.getOWLIndividualFromName(instanceName);
			
			while((fileLine = lnr.readLine())!=null) {
				String[] parts = fileLine.split(CoreConfiguration.SEPARATOR);			
				// Header
				if ((lnr.getLineNumber() == 2)) {
					// Infer column order (array indexes)
					for (int i = 0; i < parts.length; i++) {
						String part = parts[i];
						if (StringUtils.containsIgnoreCase(part, "INSTANCE NAME")) 
							nameCol = i;
						else if (StringUtils.containsIgnoreCase(part, "CONCEPT NAME")) 
							conceptCol = i;
					}
				}				
				// Data						
				if (lnr.getLineNumber() > 2 && (nameCol != -1) && (conceptCol != -1)) {	
					String currentInstance = parts[nameCol].trim();
					if (currentInstance.equals(instanceName)) {
						OWLClass instancedClass = getOWLClassFromName(parts[conceptCol]);						
						OWLClassAssertionAxiom classAssertion = factory.getOWLClassAssertionAxiom(instancedClass, individual);
						assertions.add(classAssertion);	
					}
				}			
			}		
			lnr.close();
			return assertions;
		} catch (FileNotFoundException e) { 
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Gets the instance attribute assertions from WebODE formatted instance table file for a given instance.
	 *
	 * @param tableFileName, WebODE formatted instance table file name
	 * @param instanceName, the given instance name
	 * @return the instance attribute assertions set
	 */
	public HashSet<OWLAxiom> getInstanceAttributeAssertionsFromTable(String tableFileName, String instanceName) {
		OWLDataFactory factory = this.getManager().getOWLDataFactory();
		String fileLine = null;
		HashSet<OWLAxiom> assertions = new HashSet<OWLAxiom>();	
		try {
			File fp = new File(this.getCfg().getFiles_Path() + "\\" + tableFileName);		
			FileReader inputFile = new FileReader(fp);
			LineNumberReader lnr = new LineNumberReader(inputFile);
			int nameCol = -1, attributeCol = -1, valueCol = -1;		
			OWLIndividual individual = this.getOWLIndividualFromName(instanceName);
			
			while((fileLine = lnr.readLine())!=null) {
				String[] parts = fileLine.split(CoreConfiguration.SEPARATOR);			
				// Header
				if ((lnr.getLineNumber() == 2)) {
					// Infer column order (array indexes)
					for (int i = 0; i < parts.length; i++) {
						String part = parts[i];
						if (StringUtils.containsIgnoreCase(part, "INSTANCE NAME")) 
							nameCol = i;
						else if (StringUtils.containsIgnoreCase(part, "ATTRIBUTE") ||
								 StringUtils.containsIgnoreCase(part, "INSTANCED RELATION")) 
							attributeCol = i;
						else if (StringUtils.containsIgnoreCase(part, "VALUE") ||
								 StringUtils.containsIgnoreCase(part, "TARGET INSTANCE")) 
							valueCol = i;
					}
				}			
				// Data						
				if (lnr.getLineNumber() > 2 && (nameCol != -1) && (attributeCol != -1) && (valueCol != -1)) {	
					String currentInstance = parts[nameCol].trim();
					if (currentInstance.equals(instanceName)) {
						String attribute = parts[attributeCol].trim();
						String value = parts[valueCol].trim();				
						if(value.compareTo(CoreConfiguration.NOTHING) != 0) {
							//ObjectProperty
							if(Character.isUpperCase(attribute.charAt(0))) {
								OWLObjectProperty objectProp = this.getOWLObjectPropertyFromName(attribute); 
								OWLNamedIndividual propertyValue = factory.getOWLNamedIndividual(":"+value.replaceAll(" ", ""), this.getPm());
								OWLObjectPropertyAssertionAxiom propertyAssertion = factory.getOWLObjectPropertyAssertionAxiom(objectProp, individual, propertyValue); 
								assertions.add(propertyAssertion);
							}
							//DataProperty
							else {
								OWLDataProperty dataProperty = this.getOWLDataPropertyFromName(attribute);					
								OWLLiteral propertyValue = factory.getOWLLiteral(Boolean.getBoolean(value));
								OWLDataPropertyAssertionAxiom propertyAssertion = factory.getOWLDataPropertyAssertionAxiom(dataProperty, individual, propertyValue); 
								assertions.add(propertyAssertion);
							}		
						}
					}
				}			
			}			
			lnr.close();
			return assertions;
		} catch (FileNotFoundException e) { 
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}				
	}	
	

	// TAXONOMY
	// SubclassOf	
	/**
	 * Gets the Subclass-Of axioms from a single line of the formatted text file as input.
	 *
	 * @param fileLine, a single file line from the source file
	 * @return The Subclass-Of axioms set
	 */	
	//@Override
	public Set<OWLAxiom> getSubClassOfAxiomsFromLine(String fileLine) {
		OWLDataFactory factory = this.getManager().getOWLDataFactory();
		Set<OWLAxiom> axiomSet = new HashSet<OWLAxiom>();
		String[] parts = fileLine.split(CoreConfiguration.SEPARATOR);
		int specializationCol = 0, generalizationCol = 1;
		String specialization = parts[specializationCol], generalization = parts[generalizationCol];

		// If there is some detail in brackets with the name
		Pattern pattern = Pattern.compile("([\\s*\\w+\\-\\/]+)\\(([\\w+\\,\\-\\/\\s*]+)\\)");				
		Matcher matcherGeneralization = pattern.matcher(generalization.trim());
		Matcher matcherSpecialization = pattern.matcher(specialization.trim());	
		if (matcherGeneralization.matches()) {
			generalization = matcherGeneralization.group(1).trim();
			String detail = matcherGeneralization.group(2).trim();		
			// Special case: Not synonyms
			if (detail.equalsIgnoreCase("Open") || detail.equalsIgnoreCase("Close")) 
				generalization = generalization + "_" + detail;
		}			
		if (matcherSpecialization.matches()) {
			specialization = matcherSpecialization.group(1).trim();
			String detail = matcherSpecialization.group(2).trim();	
			// Special case: Not synonyms
			if (detail.equals("Open") || detail.equals("Close")) 
				specialization = specialization + "_" + detail;
		}
		// Getting OWL Classes
		OWLClass generalizationClass = getOWLClassFromName(generalization.replace("/", "-"));
		OWLClass specializationClass = getOWLClassFromName(specialization.replace("/", "-"));
		OWLSubClassOfAxiom subclassAxiom = factory.getOWLSubClassOfAxiom(specializationClass, generalizationClass);
		axiomSet.add(subclassAxiom);
		
		// Labels 
		OWLAnnotation lblClaseEspecializacion = factory.getOWLAnnotation(factory.getRDFSLabel(), factory.getOWLLiteral(specialization.trim(), "en"));
		OWLAnnotationAssertionAxiom lblClaseEspecializAxiom = factory.getOWLAnnotationAssertionAxiom(specializationClass.getIRI(), lblClaseEspecializacion);
		axiomSet.add(lblClaseEspecializAxiom);
		OWLAnnotation lblClaseGeneralizacion = factory.getOWLAnnotation(factory.getRDFSLabel(), factory.getOWLLiteral(generalization.trim(), "en"));
		OWLAnnotationAssertionAxiom lblClaseGeneralAxiom = factory.getOWLAnnotationAssertionAxiom(generalizationClass.getIRI(), lblClaseGeneralizacion);
		axiomSet.add(lblClaseGeneralAxiom);	
		return axiomSet;
	}	

	// Disjoint Decompositions
	/**
	 * Adds the Class Disjoint-Decompositions from table file to the ontology.
	 *
	 * @param tableFileName, the Disjoint-Decompositions table file name
	 * @see http://www.w3.org/TR/owl-ref/#disjointWith-def
	 */ 
	@Override
	public void addDisjointDecompositionsFromTable(String tableFileName) {
		OWLDataFactory factory = this.getManager().getOWLDataFactory();
		String fileLine = null;
		Set<OWLClass> components = new HashSet<OWLClass>();
		try {
			File fp = new File(this.getCfg().getFiles_Path() + "\\" + tableFileName);
			FileReader inputFile = new FileReader(fp);
			LineNumberReader lnr = new LineNumberReader(inputFile);
			int groupCol = -1, componentsCol = -1, targetCol = -1;	
			String group = null, target = null; 	
			String groupRef = null, targetRef = null;
			boolean firstRef = true;
			
			while((fileLine = lnr.readLine()) != null) {
				String[] parts = fileLine.split(CoreConfiguration.SEPARATOR);		
				// Header
				if (lnr.getLineNumber() == 2) {
					// Infer column order (array indexes)
					for (int i = 0; i < parts.length; i++) {
						if (StringUtils.containsIgnoreCase(parts[i], "GROUP COMPONENTS"))
							componentsCol = i;
						else if (StringUtils.containsIgnoreCase(parts[i], "GROUP"))
							groupCol = i;
						else if (StringUtils.containsIgnoreCase(parts[i], "TARGET"))
							targetCol = i;					
					}
				}		
				// Data	
				if ((lnr.getLineNumber() > 2) && (groupCol != -1) && (componentsCol != -1) && (targetCol != -1)) {			
					parts = fileLine.split(CoreConfiguration.SEPARATOR); 
					group = parts[groupCol];
					String disjointComponent = parts[componentsCol];
					target = parts[targetCol];				
					if (firstRef) {
						groupRef = group; 
						targetRef = target;
						firstRef = false;
					}			
					// Same group
					if ((group.compareTo(groupRef) == 0) && (target.compareTo(targetRef) == 0)) {
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
						groupRef = group; 
						targetRef = target;					
						OWLClass disjointClass = this.getOWLClassFromName(disjointComponent);
						components.add(disjointClass);
					}	
				}
			}
			lnr.close();
			
			// Ultima line del fichero de entrada
			OWLDisjointClassesAxiom disjointAxiom = factory.getOWLDisjointClassesAxiom(components);
			this.getManager().applyChange(new AddAxiom(this.getOntology(), disjointAxiom));		
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

	// Exhaustive Decompositions
	/**
	 * Adds the Class Exhaustive-Decompositions from table file to the ontology.
	 *
	 * @param tableFileName, the Exhaustive-Decompositions table file name
	 */
	@Override
	public void addExhaustiveDecompositionsFromTable(String tableFileName) {
		OWLDataFactory factory = this.getManager().getOWLDataFactory();
		String fileLine = null;
		Set<OWLClass> components = new HashSet<OWLClass>();
		try {
			File fp = new File(this.getCfg().getFiles_Path() + "\\" + tableFileName);
			FileReader inputFile = new FileReader(fp);
			LineNumberReader lnr = new LineNumberReader(inputFile);
			int groupCol = -1, componentsCol = -1, targetCol = -1;	
			String group = null, target = null, component = null; 	
			String groupRef = null, targetRef = null;
			OWLClass targetClass = null, unionClass = null;
			boolean firstRef = true;
			
			while((fileLine = lnr.readLine()) != null) {
				String[] parts = fileLine.split(CoreConfiguration.SEPARATOR);		
				// Header
				if (lnr.getLineNumber() == 2) {
					// Infer column order (array indexes)
					for (int i = 0; i < parts.length; i++) {
						if (StringUtils.containsIgnoreCase(parts[i], "GROUP COMPONENTS"))
							componentsCol = i;
						else if (StringUtils.containsIgnoreCase(parts[i], "GROUP"))
							groupCol = i;
						else if (StringUtils.containsIgnoreCase(parts[i], "TARGET"))
							targetCol = i;					
					}
				}		
				// Data	
				if ((lnr.getLineNumber() > 2) && (groupCol != -1) && (componentsCol != -1) && (targetCol != -1)) {	
					group = parts[groupCol];
					component = parts[componentsCol];
					target = parts[targetCol];		
					if (firstRef) {
						groupRef = group; 
						targetRef = target;
						firstRef = false;
						targetClass = getOWLClassFromName(target);
					}		
					// Same group
					if ((group.compareTo(groupRef) == 0) && (target.compareTo(targetRef) == 0)) {
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
						groupRef = group; 
						targetRef = target;
						targetClass = this.getOWLClassFromName(target);
						unionClass = this.getOWLClassFromName(component);
						components.add(unionClass);
					}	
				}
			}	
			lnr.close();		
			OWLObjectUnionOf unionOf = factory.getOWLObjectUnionOf(components);
			OWLEquivalentClassesAxiom equivalentClasses = factory.getOWLEquivalentClassesAxiom(targetClass, unionOf);
			this.getManager().applyChange(new AddAxiom(this.getOntology(), equivalentClasses));		
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	
	
	// Taxonomy (Part-Of)
	// Use Core.getPartOfAxiomsFromTable(String tableFileName)
}
