package edu.drexel.psal.anonymouth.gooie;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import edu.drexel.psal.jstylo.generics.Logger;
import edu.drexel.psal.jstylo.generics.Logger.LogOut;

/**
 * A simple splash screen displayed on start up to serve two main purposes:
 * 	1. Since Anonymouth on first load takes a while to load, we want to show some indication that it is doing so (like Photoshop)
 * 	2. Help give Anonymouth a professional sheen.
 * @author Marc Barrowclift
 *
 */
public class SplashScreen extends Thread {
	
	private final String NAME = "( SplashScreen) - ";
	private static final int WIDTH = 520, HEIGHT = 135;
	private static final Font HELVETICA = new Font("Helvetica", Font.PLAIN, 18);
	
	private static String message;
	private java.awt.SplashScreen splash;
	private boolean showSplash = true;

	/**
	 * Paints the changes on top of the splash screen image;
	 * @param g
	 */
	static void renderSplashFrame(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setComposite(AlphaComposite.Clear);
		g.fillRect(0,0,WIDTH,HEIGHT);
		g.setPaintMode();
		g.setColor(Color.BLACK);
		g.setFont(HELVETICA);
		g.drawString(message+"...", 240, 128);
	}
	
	/**
	 * Constructor
	 * @param message
	 */
	public SplashScreen() {
		super("SplashScreen");
	}

	/**
	 * Updates the message text with the new status
	 * @param newText
	 */
	public void updateText(String newText) {
		message = newText+"...";	
	}
	
	/**
	 * Trashes the splash screen
	 */
	public void hideSplashScreen() {
		showSplash = false;
		splash.close();
	}

	/**
	 * Updates the splash screen for as long as necessary
	 */
	@Override
	public void run() {
		message = "Starting Anonymouth";

		splash = java.awt.SplashScreen.getSplashScreen();
		if (splash == null) {
			Logger.logln(NAME+"SplashScreen.getSplashScreen() returned null", LogOut.STDERR);
			return;
		}
		Graphics2D g = splash.createGraphics();
		if (g == null) {
			Logger.logln(NAME+"Graphics returned null", LogOut.STDERR);
			return;
		}
		
		while (showSplash) {
			renderSplashFrame(g);
			try {
				splash.update();
			} catch (IllegalStateException e) {
				Logger.logln(NAME+"Splash screen not configured by Java properly and unavailable," +
						" happens every once in a while. As far as I know nothing I can do.", LogOut.STDERR);
				return;
			}
			
			try {
				Thread.sleep(90);
			}
			catch(InterruptedException e) {}
		}
	}
}