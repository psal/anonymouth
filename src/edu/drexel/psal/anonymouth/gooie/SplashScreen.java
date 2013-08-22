package edu.drexel.psal.anonymouth.gooie;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
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
 */

public class SplashScreen extends JFrame {

	private static final long serialVersionUID = 1L;
	private final String NAME = "( SplashScreen) - ";
	private final String SPLASH_NAME = "anonymouth_SPLASH.png";
	private final int ANIMATION_SPEED = 3;
	private final int ANIMATION_FRAMES = 50;
	
	private int width = 520, height = 135;
	private Image splashImage;
	public JLabel progressLabel;
	public String newText;
	private JPanel panel;
	private SplashScreen splashScreen;
	
	private SwingWorker<Void, Void> fadeIn;

	/**
	 * Constructor, initializes the splash screen
	 */
	public SplashScreen() {
		this.setSize(width, height);
		/**
		 * Undecorated and setLocationRelativeTo(null) don't actual set it to the middle since setLocationRelativeTo(null)
		 * still counts the menu bar of the undecorated from when making it's calculation even if it's not shown since it's
		 * undecorated. Thus, we calculate it ourselves to get a true center of the screen.
		 */
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(new Point((screen.width/2)-(width/2), (screen.height/2)-(height/2)));
		this.setResizable(false);
		this.setUndecorated(true);
		this.setVisible(false);
		
		splashScreen = this;
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

		panel.setPreferredSize(new Dimension());
		progressLabel = new JLabel("Beginning Anonymouth...");
		progressLabel.setFont(new Font("Helvetica", Font.PLAIN, 18));
		progressLabel.setBorder(new EmptyBorder(5, 5, 0, 20));
		progressLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		progressLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
		
		panel.setLayout(new BorderLayout());
		panel.add(progressLabel, BorderLayout.SOUTH);
		this.getContentPane().add(panel);

		readyFadeInWorker();
	}
	
	/**
	 * Initializes the fade in Swing Worker for the splash screen. Should only be called once by the
	 * constructor since the splash screen need only be shown once (meaning only one fade in).
	 */
	private void readyFadeInWorker() {
		fadeIn = new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				Logger.logln(NAME+"Displaying Splash Screen");
				splashScreen.setOpacity(0.0f);
				splashScreen.setVisible(true);
				for (int i = 0; i <= ANIMATION_FRAMES; i++) {
					splashScreen.setOpacity((float)i/(float)ANIMATION_FRAMES);
					
					try {
						Thread.sleep(ANIMATION_SPEED);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				splashScreen.setOpacity(1.0f);
				
				return null;
			}
		};
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
	 * Displays the splash screen by beginning the Swing Worker fadeIn.
	 */
	public void showSplashScreen() {
		fadeIn.execute();		
	}
	
	/**
	 * Trashes the splash screen. This is NOT put in a Swing Worker like the fade in
	 * because with the fade in we don't want the fading in animation to lock up the
	 * application from continuing to load.<br><br>
	 * 
	 * But here, we don't want the start window to appear right over the splash screen
	 * before it can have a chance to fade out nicely, so that's why we quickly do so
	 * in the EDT before it displays the start window.
	 */
	public void hideSplashScreen() {
		Logger.logln(NAME+"Closing Splash Screen");
		for (int i = ANIMATION_FRAMES; i >= 0; i--) {
			splashScreen.setOpacity((float)i/(float)ANIMATION_FRAMES);

			try {
				Thread.sleep(ANIMATION_SPEED);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		splashScreen.setVisible(false);
	}
}