package edu.drexel.psal.anonymouth.gooie;

import javax.swing.JOptionPane;

import edu.drexel.psal.jstylo.generics.Logger;
import edu.drexel.psal.jstylo.generics.Logger.LogOut;

public class ErrorHandler {
	
	private final static String NAME = "( ErrorHandler ) - ";

	
	private static final int FATAL_ERROR = 666;
	
	public static void fatalError(){
		int ans = JOptionPane.showConfirmDialog(null, "Anonymouth has encountered a fatal error.\n" +
				"The best thing to do is to examine the stack trace, and terminate the program.\n" +
				"If you would like to save your work/problem set prior to closing Anonymouth,\n" +
				"click \"no\" below. Otherwise, click \"yes\" (this will close Anonymouth)","Fatal error encountered. Terminate?",JOptionPane.YES_NO_OPTION);
		if(ans == 0){
			Logger.logln(NAME+"Fatal error encountered, termination requested.",LogOut.STDERR);
			System.exit(FATAL_ERROR);
		}
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
