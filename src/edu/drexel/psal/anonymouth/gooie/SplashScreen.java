package edu.drexel.psal.anonymouth.gooie;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import edu.drexel.psal.anonymouth.helpers.ImageLoader;
import edu.drexel.psal.jstylo.generics.Logger;

/**
 * A simple splash screen displayed on start up to serve two main purposes:
 * 
 * 	1. Since Anonymouth on first load takes a while to load, we want to show some indication that it is doing so (like Photoshop)
 * 	2. Help give Anonymouth a professional sheen.
 * 
 * @author Marc Barrowclift
 *
 */
public class SplashScreen extends JFrame {

	private static final long serialVersionUID = 1L;
	private final String NAME = "( SplashScreen) - ";
	private final String SPLASH_NAME = "anonymouth_SPLASH.png";
	
	private int width = 520, height = 135;
	private Image splashImage;
	public JLabel progressLabel;
	public String newText;
	private JPanel panel;

	/**
	 * Constructor, initializes the splash screen
	 */
	public SplashScreen() {
		this.setSize(width, height);
		this.setLocationRelativeTo(null);
		this.setResizable(false);
		this.setUndecorated(true);
		this.setVisible(false);
		
		splashImage = ImageLoader.getImage(SPLASH_NAME);
		panel = new JPanel(new BorderLayout()) {
			private static final long serialVersionUID = 1L;
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2d = (Graphics2D)g;
				g2d.drawImage(splashImage, 0, 0, null);
			}
		};
		
		progressLabel = new JLabel("Beginning Anonymouth...");
		progressLabel.setFont(new Font("Helvetica", Font.PLAIN, 18));
		progressLabel.setBorder(new EmptyBorder(5, 5, 0, 20));
		progressLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		progressLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
		
		panel.setLayout(new BorderLayout());
		panel.add(progressLabel, BorderLayout.SOUTH);
		this.add(panel);
	}

	/**
	 * Updates the message text with the new status
	 * 
	 * @param newText
	 * 		The new message you want to display. A "..." will be appended to it before updating.
	 */
	public void updateText(final String newText) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				progressLabel.setText(newText+"...");	
			}
		});
	}

	/**
	 * Displays the splash screen
	 */
	public void showSplashScreen() {
		Logger.logln(NAME+"Displaying Splash Screen");
		this.setOpacity((float)0/(float)100);
		this.setVisible(true);
		for (int i = 0; i <= 100; i+=2) {
			this.setOpacity((float)i/(float)100);
			
			try {
				Thread.sleep(3);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		this.setOpacity((float)1.0);
	}
	
	/**
	 * Trashes the splash screen
	 */
	public void hideSplashScreen() {
		Logger.logln(NAME+"Closing Splash Screen");
		for (int i = 100; i >= 0; i-=2) {
			this.setOpacity((float)i/(float)100);
			
			try {
				Thread.sleep(3);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		this.setVisible(false);
	}
}