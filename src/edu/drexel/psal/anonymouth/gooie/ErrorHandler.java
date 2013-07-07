package edu.drexel.psal.anonymouth.gooie;

import javax.swing.JOptionPane;

import edu.drexel.psal.jstylo.generics.Logger;
import edu.drexel.psal.jstylo.generics.Logger.LogOut;

public class ErrorHandler {
	
	private final static String NAME = "( ErrorHandler ) - ";

	
	private static final int FATAL_ERROR = 666;
	private static final int STANFORD_POS = 905; // 9 == P, 0 == O, 5 == S...
	
	public static void fatalError() {
		int ans = JOptionPane.showConfirmDialog(null, "Anonymouth has encountered a fatal error.\n" +
				"The best thing to do is to examine the stack trace, and terminate the program.\n" +
				"If you would like to save your work/problem set prior to closing Anonymouth,\n" +
				"click \"no\" below. Otherwise, click \"yes\" (this will close Anonymouth)","Fatal error encountered. Terminate?",JOptionPane.YES_NO_OPTION);
		if(ans == 0){
			Logger.logln(NAME+"Fatal error encountered, termination requested.",LogOut.STDERR);
			System.exit(FATAL_ERROR);
		}
	}
	
	public static void StanfordPOSError(){
		int ans = JOptionPane.showConfirmDialog(null, "The Stanford POS (Part of Speech) tagger\n" +
				"has caused an error. This seems to happen reasonably often,\n" +
				"especially while running on Mac OS X. It seems to be an issue with the Stanford parser,\n" +
				"but we aren't completely sure. In anycase, the best thing to do is to restart Anonymouth,\n" +
				"and hope it doesn't happen again. Generally, using less threads (in the 'advanced' section of 'Preferences')\n"+
				"helps reduce the frequency of this issue (1 or 2 is probably your best bet. 4 creates this error about 25% of the time,\n"+
				"while any more than that seems to peg the cpu [on a Mac, at least].\n\n" +
				"If you would like to save your work/problem set prior to closing Anonymouth,\n" +
				"click \"no\" below. Otherwise, click \"yes\" (this will close Anonymouth)","Stanford Tagger Error",JOptionPane.YES_NO_OPTION);
		if(ans == 0){
			Logger.logln(NAME+"Stanford POS Tagger error, termination requested.",LogOut.STDERR);
			System.exit(STANFORD_POS);
		}
	}
	
	public static void editorError(String title, String errorMessage) {
		Logger.logln(NAME+"An error was encountered in the editor.", LogOut.STDERR);
		JOptionPane.showConfirmDialog(null, errorMessage, title, JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE, null);
	}
	
	public static void incompleteSentence(){
		JOptionPane.showMessageDialog(null,
				"Please finish the current sentence before moving on to the next sentence.",
				"",
				JOptionPane.INFORMATION_MESSAGE,
				GUIMain.icon);
	}
			
	
	/**
	 * Main method for testing
	 */
	public static void main(String[] args){
		fatalError();
		System.out.println("Termination not requested.");
	}
}
