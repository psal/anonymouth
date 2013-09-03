package edu.drexel.psal;

import edu.drexel.psal.anonymouth.helpers.ExtFilter;

/**
 * Anonymouth Constants and such (To help clean up ThePresident, which now focuses solely on starting up Anonymouth).
 * Any global booleans, paths, directories, or widely used constants should go in here.
 * 
 * @author Marc Barrowclift
 *
 */

public class ANONConstants {
	//=====================================================================
	//						PATHS / DIRECTORIES
	//=====================================================================
	
	//DO NOT DELETE, for use when bundling Anonymouth in an OS X app
	//public final static String WORKING_DIR = System.getProperty("java.library.path") + "/";
	public final static String WORKING_DIR = "./";
	//DO NOT DELETE, for use when bundling Anonymouth in an OS X app.
	//public static final String LOG_DIR = System.getProperty("user.home")+"/Desktop/anonymouth_log";
	public static final String LOG_DIR = WORKING_DIR + "anonymouth_log";
	public static final String RESOURCE_PACKAGE = "/edu/drexel/psal/resources/";
	public static final String GRAPHICS_PREFIX = RESOURCE_PACKAGE+"graphics/";
	public static final String EXTERNAL_RESOURCE_PACKAGE = ANONConstants.WORKING_DIR + "jsan_resources/";
	public static final String CORPORA_PREFIX = EXTERNAL_RESOURCE_PACKAGE+"corpora/";
	public static final String PROBLEMSETS_PREFIX = EXTERNAL_RESOURCE_PACKAGE+"problem_sets/";
	public static final String FEATURESETS_PREFIX = EXTERNAL_RESOURCE_PACKAGE+"feature_sets/";
	public static final String JGAAP_RESOURCE_WORDNET = "/com/jgaap/resources/wordnet/";
	public static final String DOC_MAGICIAN_WRITE_DIR = WORKING_DIR + ".edited_documents/";
	public static final String KOPPEL_FUNCTION_WORDS = EXTERNAL_RESOURCE_PACKAGE+"koppel_function_words.txt";
	public static final String SER_DIR = WORKING_DIR + ".serialized_objects/";
	public static final String GRAPHICS = "/edu/drexel/psal/resources/graphics/";
	public static final String PATH_TO_CLASSIFIER = SER_DIR+"saved_classifier.model";
	public static final String ABBREVIATIONS_FILE = EXTERNAL_RESOURCE_PACKAGE+"abbreviations.txt";
	
	//=====================================================================
	//							BOOLEAN FLAGS
	//=====================================================================
	
	public static final boolean IS_MAC = System.getProperty("os.name").toLowerCase().contains("mac");
	public static final boolean IS_USER_STUDY = false; //whether or not to show the session name dialog
	public static final boolean SHOULD_KEEP_AUTO_SAVED_ORIGINAL_DOC = false;
	public static final boolean SAVE_TAGGED_DOCUMENTS = true; // TODO: Put in Preferences and create implementation for it
	
	//Whether or not to show certain components in the GUIs
	public static final boolean SHOW_ADVANCED_SETTINGS = true; //In Anonymouth's Start Window
	public static final boolean SHOW_TRANSLATION_NAME_LABELS = false; //For translations in the translations holder scroll pane
	
	//=====================================================================
	//						VARIOUS / UNCATEGORIZED
	//=====================================================================
	
	public static final String DUMMY_NAME = "~* you *~"; // NOTE DO NOT CHANGE THIS unless you have a very good reason to do so.
	
	public static final int EXPECTED_NUM_OF_SENTENCES = 100;
	
	public static final ExtFilter TXT = new ExtFilter("Text files (*.txt)", ".txt");
	public static final ExtFilter XML = new ExtFilter("XML files (*.xml)", ".xml");
	
	//=====================================================================
	//							ENUM TYPES
	//=====================================================================
	
	/**
	 * Used for the Hide/Show menu items<br><br>
	 * 
	 * Keeps track of whether or not certain components are visible to the user.
	 * Please not this is NOT the same thing as isVisible(), we are still keeping and updating
	 * those components in the background and they are still "visible" ready to go, the only
	 * difference is we don't actually have them added to the frame or not (depending on which
	 * state they are in).
	 * 
	 * @author Marc Barrowclift
	 */
	public enum STATE {
		VISIBLE, HIDDEN
	}
	
	/**
	 * Used for determining what state the currently focused sentence is in in terms of translations
	 * <ul>
	 * <li>EMPTY: No translations have been done yet (show the translate button)</li>
	 * <li>PROCESSING: The sentence is in the process of being translated (show the progress bar)</li>
	 * <li>DONE: The sentence has recieved all translations (hide completely)</li>
	 * </ul>
	 * @author Marc Barrowclift
	 */
	public enum TRANSLATIONS {
		EMPTY, PROCESSING, DONE
	}
}
