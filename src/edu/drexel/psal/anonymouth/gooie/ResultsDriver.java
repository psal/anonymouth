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
	
	private ActionListener resultsListener;
	private GUIMain main;
	
	/**
	 * Constructor, initializes and sets all listeners for you
	 * 
	 * @param main
	 * 		GUIMain instance
	 */
	public ResultsDriver(GUIMain main) {
		this.main = main;
		initListeners();
	}
	
	/**
	 * Initializes all Results listeners
	 */
	private void initListeners() {
		resultsListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				main.resultsWindow.showResultsWindow();
			}
		};
		main.resultsButton.addActionListener(resultsListener);
	}
}
