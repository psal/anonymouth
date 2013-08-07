package edu.drexel.psal;

import edu.drexel.psal.anonymouth.helpers.ExtFilter;

/**
 * Anonymouth Constants and such (To help clean up ThePresident, which now focuses solely on starting up Anonymouth)
 * @author Marc Barrowclift
 *
 */

public class ANONConstants {
	//==============DIRECTORIES==================
	//DO NOT DELETE, for use when bundling Anonymouth in an OS X app
	//public final static String WORKING_DIR = System.getProperty("java.library.path") + "/";
	public final static String WORKING_DIR = "./";
	//DO NOT DELETE, for use when bundling Anonymouth in an OS X app.
	//public static final String LOG_DIR = System.getProperty("user.home")+"/Desktop/anonymouth_log";
	public static final String LOG_DIR = WORKING_DIR + "anonymouth_log";
	public static final String DOC_MAGICIAN_WRITE_DIR = WORKING_DIR + ".edited_documents/";
	public static final String KOPPEL_FUNCTION_WORDS = JSANConstants.JSAN_EXTERNAL_RESOURCE_PACKAGE+"koppel_function_words.txt";
	public static final String SER_DIR = WORKING_DIR + ".serialized_objects/";
	public static final String GRAPHICS = "/edu/drexel/psal/resources/graphics/";
	public static final String PATH_TO_CLASSIFIER = SER_DIR+"saved_classifier.model";
	public static final String DUMMY_NAME = "~* you *~"; // NOTE DO NOT CHANGE THIS unless you have a very good reason to do so.
	
	public static final boolean IS_MAC = System.getProperty("os.name").toLowerCase().contains("mac");
	public static final boolean IS_USER_STUDY = false; //whether or not to show the session name dialog
	public static final boolean SHOULD_KEEP_AUTO_SAVED_ORIGINAL_DOC = false;
	public static final boolean SAVE_TAGGED_DOCUMENTS = true; // TODO: Put in Preferences and create implementation for it
	public static final boolean SHOW_ADVANCED_SETTINGS = false;
	public static final boolean DEBUGGING = true;
	
	public static final ExtFilter TXT = new ExtFilter("Text files (*.txt)", ".txt");
	public static final ExtFilter XML = new ExtFilter("XML files (*.xml)", ".xml");
}
