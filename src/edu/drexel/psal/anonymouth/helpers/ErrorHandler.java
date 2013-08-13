package edu.drexel.psal.anonymouth.helpers;

import java.awt.Component;
import java.awt.Toolkit;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import edu.drexel.psal.jstylo.generics.Logger;
import edu.drexel.psal.jstylo.generics.Logger.LogOut;

/**
 * Handles all fatal error pop-ups, stack trace logging, and quitting (if chosen by the user or necessary).<br><br>
 * 
 * USAGE:<br>
 * 		Just call ErrorHandler.name_of_method(); depending on what error occurred.
 * 
 * @author Marc Barrowclift
 * @author Unknown (original version)
 */
public class ErrorHandler {
	
	//Constants
	private final static String NAME = "( ErrorHandler ) - ";
	private static final int FATAL_ERROR = 666;
	private static final int STANFORD_POS = 905; // 9 == P, 0 == O, 5 == S...
	private static final String[] RESTART_OPTIONS = {"Wait", "Quit"};
	
	/**
	 * Call this method when you have a fatal error anywhere after processing (meaning the user may have made changes to their
	 * document to Anonymize they want to save first). It will tell them there was a fatal error, and give them the option to
	 * wait so they can save their document or to immediately quit.
	 * 
	 * @param parent
	 * 		The parent of the error. null is an acceptable value if there is no parent
	 * @param e
	 * 		An exception (if there was one) for Logging the stack trace. null is an acceptable value if there is no exception
	 */
	public static void fatalError(Component parent, Exception e) {
		if (e != null)
			Logger.logln(e);
		
		int ans = JOptionPane.showOptionDialog(parent,
				"Anonymouth has encountered a fatar error and will have to close since it can no\n" +
						"longer run properly.\n\n" +
						"If you wish to save any current work before Anonymouth closes, you may click\n" +
						"\"Wait\" and close Anonymouth afterwards manually.",
				"Fatal Error", JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE, UIManager.getIcon("OptionPane.errorIcon"),
				RESTART_OPTIONS, RESTART_OPTIONS[0]);
		if (ans == 1) {
			Logger.logln(NAME+"Fatal error encountered and the user chose to immediately quit, will exit now...", LogOut.STDERR);
			System.exit(FATAL_ERROR);
		}
	}
	
	/**
	 * Call this method when you have a fatal error anywhere during processing (meaning the user couldn't have made any changes
	 * to their document to Anonymize, so they won't have anything to save). Therefore, it will only tell them there was a fatal
	 * error and allow the user to click "Ok" to quit.
	 * 
	 * @param e
	 * 		An exception (if there was one) for Logging the stack trace. null is an acceptable value if there is no exception.		
	 */
	public static void fatalProcessingError(Exception e) {
		if (e != null)
			Logger.logln(e);
		
		Toolkit.getDefaultToolkit().beep();
		JOptionPane.showMessageDialog(null,
				"Anonymouth has encountered a fatar error and will have to close since it can no\n" +
				"longer run properly.\n\n" +
				"We apologize for the inconvenience and are hard at work to solve the issue.",
				"Fatal Error", JOptionPane.ERROR_MESSAGE, UIManager.getIcon("OptionPane.errorIcon"));
		
		Logger.logln(NAME+"Fatal error encountered, will exit now...", LogOut.STDERR);
		System.exit(FATAL_ERROR);
	}
	
	/**
	 * Call this method when the Stanford POS error occurs and only for that. A popup will show telling the user what happened
	 * and quit when the user clicks "Ok"
	 */
	public static void StanfordPOSError() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				Toolkit.getDefaultToolkit().beep();
				JOptionPane.showMessageDialog(null,
						"The Stanford POS (Part of Speech) tagged has caused a fatal error. This happens\n" +
						"occasionally, and so far only on Mac OS X. Anonymouth will have to close to recover.\n\n" +
						"If you are running Mac OS X and this occurs often, try using less threads (1 or 2).\n" +
						"You can change this by navigating to Anonymouth > Preferences > Advanced and sliding\n" +
						"the thread slider down.",
						"Stanford Part-Of-Speech Tagger Fatal Error", JOptionPane.ERROR_MESSAGE, UIManager.getIcon("OptionPane.errorIcon"));
				
				Logger.logln(NAME+"Stanford POS Error occurred, will exit now...", LogOut.STDERR);
				System.exit(STANFORD_POS);
			}
		});
	}	
	
	/**
	 * Main method for testing
	 */
	public static void main(String[] args) {
		//==========TESTING STANDFORD POS ERROR===========
		System.out.println("StanfordPOSError()...");
		StanfordPOSError();
		
		//==========TESTING FATAL ERROR==========
		System.out.println("fatalError()...");
		fatalError(null, null);
		System.out.println("Didn't choose to quit");
		
		//==========TESTING FATAL PROCESSING ERROR==========
		System.out.println("fatalProcessingError()...");
		fatalProcessingError(null);
		System.out.println("Didn't choose to quit");
		
		System.out.println("Done!");
	}
}
