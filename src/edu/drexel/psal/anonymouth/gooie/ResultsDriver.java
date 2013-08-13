package edu.drexel.psal.anonymouth.gooie;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Holds any listeners relating to the results graph/button/window.
 * Any future listeners for this should go here.
 * 
 * @author Marc Barrowclift
 *
 */
public class ResultsDriver {
	
	private static ActionListener resultsListener;
	
	public static void initListeners(final GUIMain main) {
		resultsListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				main.resultsWindow.showResultsWindow();
			}
		};
		main.resultsButton.addActionListener(resultsListener);
	}
}
