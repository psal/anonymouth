package edu.drexel.psal.anonymouth.gooie;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * The Driver for the results window, simply handles the mouse movements and clicks.
 * @author Marc Barrowclift
 *
 */
public class DriverResultsWindow {

	protected static MouseListener resultsLabelListener;
	protected static MouseListener resultsPanelListener;
	
	/**
	 * Initialize all documents tab listeners.
	 */
	protected static void initListeners(final GUIMain main) {
		initMainListeners(main);
	}
	
	/**
	 * Initializes the main listeners to respond to the user's mouse events.
	 * @param main - An instance of GUIMain
	 */
	protected static void initMainListeners(final GUIMain main) {
		resultsLabelListener = new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent arg0) {}
			@Override
			public void mousePressed(MouseEvent arg0) {}	
			
			@Override
			public void mouseEntered(MouseEvent arg0) {
				if (main.resultsAreReady())
					main.resultsTableLabel.setBackground(Color.YELLOW);
			}

			@Override
			public void mouseExited(MouseEvent arg0) {
				if (main.resultsAreReady())
					main.resultsTableLabel.setBackground(main.ready);
				else
					main.resultsTableLabel.setBackground(main.blue);		
			}
			
			@Override
			public void mouseReleased(MouseEvent arg0) {
				if (main.resultsAreReady()) {
					main.resultsWindow.openWindow();
					main.resultsTableLabel.setBackground(main.ready);
				} else
					main.resultsTableLabel.setBackground(main.blue);
			}		
		};
		main.resultsTableLabel.addMouseListener(resultsLabelListener);
		
		resultsPanelListener = new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {}
			@Override
			public void mousePressed(MouseEvent e) {}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (main.resultsAreReady()) {
					main.resultsWindow.openWindow();
					main.resultsTableLabel.setBackground(main.ready);
				} else
					main.resultsTableLabel.setBackground(main.blue);
			}
			
			@Override
			public void mouseEntered(MouseEvent arg0) {
				if (main.resultsAreReady())
					main.resultsTableLabel.setBackground(Color.YELLOW);
			}

			@Override
			public void mouseExited(MouseEvent arg0) {
				if (main.resultsAreReady())
					main.resultsTableLabel.setBackground(main.ready);
				else
					main.resultsTableLabel.setBackground(main.blue);		
			}
		};
		main.resultsMainPanel.addMouseListener(resultsLabelListener);
	}
	
	
}
