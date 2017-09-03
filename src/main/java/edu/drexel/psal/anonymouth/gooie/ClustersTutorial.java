package edu.drexel.psal.anonymouth.gooie;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;

import javax.swing.JFrame;
import javax.swing.JPanel;

import edu.drexel.psal.anonymouth.helpers.ImageLoader;
import edu.drexel.psal.jstylo.generics.Logger;

/**
 * A window displaying a simple tutorial of the Clusters
 * @author Marc Barrowclift
 *
 */
public class ClustersTutorial extends JFrame {

	private static final long serialVersionUID = 1L;
	private final String TUTORIAL = "clustersTutorial.png";
	private final String NAME = "( "+this.getClass().getSimpleName()+" ) - ";
	private Image tutorial;
	private JPanel panel;
	
	/**
	 * Constructor
	 */
	public ClustersTutorial() {
		init();
		this.setVisible(false);
	}
	
	/**
	 * Displays the window.
	 */
	public void openWindow() {
		this.setLocationRelativeTo(null); // makes it form in the center of the screen
		this.setVisible(true);
	}
	
	/**
	 * Initializes all the data for the window
	 */
	private void init() {
		Logger.logln(NAME+"Initializing clusters tutorial...");
		tutorial = ImageLoader.getImage(TUTORIAL);
		
		panel = new JPanel() {
			private static final long serialVersionUID = 1L;

			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2d = (Graphics2D)g;
				g2d.drawImage(tutorial, 0, 0, null);
			}
		};
		
		this.add(panel);
		this.setSize(500, 522);
		this.setResizable(false);
		this.setTitle("Clusters Tutorial");
		this.setLocationRelativeTo(null);
	}
}
