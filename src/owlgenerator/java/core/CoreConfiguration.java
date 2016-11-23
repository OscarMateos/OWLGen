/*
 * The Class CoreConfiguration.
 */
package owlgenerator.java.core;

import javax.management.InstanceAlreadyExistsException;

import owlgenerator.java.util.Util;

/**
 * The Class CoreConfiguration.
 *  
 * @author Oscar Mateos Lopez
 * @version: 20161103
 *
 */
public class CoreConfiguration {
	
	/** The instance. */
	private static volatile CoreConfiguration instance;
	
	/** The base url. */
	private final String BASE_URL;
	
	/** The files path. */
	private final String FILES_PATH;	
	
	/** The ontology name. */
	private final String ONTOLOGY_NAME;	
	
	/** The ontology version. */
	private final String ONTOLOGY_VERSION;

	/** The Constant NOTHING. value */
	public static final String NOTHING = "--";
	
	/** The Constant SEPARATOR. value */
	public static final String SEPARATOR = "~~";	
	
	/** The Constant INNER_SEPARATOR. value */
	public static final String INNER_SEPARATOR = "%";
	
	/** The Constant STOP. value */
	public static final String STOP = "$TOP";
	
	/** The Constant CARRIAGE_RETURN_SEPARATOR. value */
	public static final String CARRIAGE_RETURN_SEPARATOR = "$CRLF";

	// CONSTRUCTOR
//	private CoreConfiguration() {
//	      // Exists only to defeat instantiation.
//	}
	
	/**
	 * Instantiates a new core configuration.
	 *
	 * @param baseUrl, the base url for the Core ontology
	 * @param filesPath, the source formatted text files path
	 * @param ontologyName, the Core ontology name
	 */
	private CoreConfiguration(String baseUrl, String filesPath, String ontologyName) {
		BASE_URL = baseUrl;
		FILES_PATH = filesPath;
		ONTOLOGY_NAME = ontologyName;
		ONTOLOGY_VERSION = Util.getCurrentTimeStamp();
	}
	
	/**
	 * Gets the read-only single instance of CoreConfiguration.
	 *
	 * @return single instance of CoreConfiguration
	 */
	// THREAD SAFE SINGLETON PATTERN INSTANCER
    public static CoreConfiguration getInstance() {
        if (instance == null ) {
            synchronized (CoreConfiguration.class) {
                if (instance == null) 
                    throw new ExceptionInInitializerError("BASE_URL, FILES_PATH and ONTOLOGY_NAME are not initialized: Must build CoreConfiguration instance first.");
            }
        }
        return instance;
    }
    
    /**
     * Builds the single instance of CoreConfiguration, prior getting its instance.
     *
     * @param baseUrl, the base url for the Core ontology
     * @param filesPath, the source formatted text files path
     * @param ontologyName, the Core ontology name
     * @throws InstanceAlreadyExistsException, if the instance already exists
     */
    // BUILDER
    public static void build(String baseUrl, String filesPath, String ontologyName) throws InstanceAlreadyExistsException {
        if (instance == null ) {
            synchronized (CoreConfiguration.class) {
                if (instance == null) {
                    instance = new CoreConfiguration(baseUrl, filesPath, ontologyName);
                    return;
                }
            }
        }
        else
        	throw new InstanceAlreadyExistsException("CoreConfiguration already exists and cannot be updated nor changed.");
    }

	/**
	 * Gets the base url for the Core ontology.
	 *
	 * @return The Core ontology base url
	 */
	// GETTERS & SETTERS
	public String getBase_Url() {
		return BASE_URL;
	}

	/**
	 * Gets the source formatted text files path.
	 *
	 * @return The files path
	 */
	public String getFiles_Path() {
		return FILES_PATH;
	}

	/**
	 * Gets the Core ontology name.
	 *
	 * @return The Core ontology name
	 */
	public String getOntology_Name() {
		return ONTOLOGY_NAME;
	}

	/**
	 * Gets the Core ontology version.
	 *
	 * @return the Core ontology version
	 */
	public String getOntology_Version() {
		return ONTOLOGY_VERSION;
	}	
}
