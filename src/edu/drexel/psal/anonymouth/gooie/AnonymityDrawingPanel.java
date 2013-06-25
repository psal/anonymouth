package edu.drexel.psal.anonymouth.gooie;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;

import javax.imageio.ImageIO;
import javax.swing.JLabel;
import javax.swing.JPanel;

import edu.drexel.psal.JSANConstants;
import edu.drexel.psal.jstylo.generics.Logger;
import edu.drexel.psal.jstylo.generics.Logger.LogOut;

import net.miginfocom.swing.MigLayout;

/**
 * 
 * Paints the information gathered from "ConsolidationStation.toModifyTaggedDocs.get(0).getAvgPercentChangeNeeded(false)"
 * into a nice "intensity" bar for the user to see how anonymous their document is (and therefore how much of it needs to
 * be changed)
 * 
 * Will be added as part of the "Anonymity" tab on the left-hand side of the main Anonymouth window
 * @author Marc Barrowclift
 */
public class AnonymityDrawingPanel extends JPanel {
	
	private final String NAME = "( "+this.getClass().getName()+" ) - ";
	private static final long serialVersionUID = 1L;
	private final int MINY = 50;
	private final int MAXY = 470; //700 without results part
	private final String[] PERCENTTEXT = {"100%", "75%", "50%", "25%", "0%"};
	private GUIMain main;
	private JLabel anonymous;
	private JLabel notAnonymous;
	private Boolean showPointer;
	private Font percFont;
	private static Pointer pointer;
	protected Image bar;
	protected Image barFull;
	protected Image barEmpty;
	protected double percentToGoal;
	
	//Pointer to show the user how anonymous their document is on the scale.
	/**
	 * Manages all the data associated with the intensity bar pointer, which is simply a little arrow that will point
	 * to where the user's test document stands on the not anonymous <-> scale
	 */
	class Pointer {
		private final int X = 50;
		private int y;
		private int maxPercent;
		private int curPercent;
		private double ratio;
		
		public Pointer() {
			y = MAXY + MINY;
		}
		
		/**
		 * Calculates what the arrow's new y position should be, should not be called by other methods.
		 * If the y value needs to be changed, call "setPercentage()" instead
		 */
		private void setValue() {
			this.y = (int)(MAXY * getRatio() + MINY * getRatio() + getMaxPercentage() * (.5 - getRatio()));
		}
		
		public int getY() {
			return y;
		}
		
		public int getX() {
			return X;
		}
		
		/**
		 * Sets the new anonymity percentage, the panel must be repainted for changes to be seen.
		 * @param perc must be integer representation of percentage (e.g., 50 for 50% instead of .5)
		 */
		public void setPercentages(int percentage, int maxPercentage) {
			if (maxPercentage >= 0 && percentage >= 0) {
				if (percentage <= maxPercentage)
					setRatio(percentage, maxPercentage);
				else
					setRatio(1, 1);
				
				curPercent = (int)(getRatio() * 100 + .5);
				maxPercent = 100;
				
				setValue();
			}
		}
		
		public int getPercentage() {
			return curPercent;
		}
		
		public int getMaxPercentage() {
			return maxPercent;
		}
		
		private void setRatio(int percentage, int maxPercentage) {
			ratio = (double)percentage / (double)maxPercentage;
		}
		
		public double getRatio() {
			return ratio;
		}
	}
	
	public AnonymityDrawingPanel(GUIMain main) {
		this.setLayout(new MigLayout());
		this.main = main;
		
		anonymous = new JLabel("Goal");
		notAnonymous = new JLabel("Start");
		
		showPointer = false;
		
		anonymous.setFont(new Font("Helvatica", Font.BOLD, 16));
		notAnonymous.setFont(new Font("Helvatica", Font.BOLD, 16));
		this.add(anonymous, "pos 69 15"); //40
		this.add(notAnonymous, "pos 68 485"); //24
		
		try {
			bar = ImageIO.read(getClass().getResource(JSANConstants.JSAN_GRAPHICS_PREFIX+"bar.png"));
			barFull = ImageIO.read(getClass().getResource(JSANConstants.JSAN_GRAPHICS_PREFIX+"barFull.png"));
			barEmpty = ImageIO.read(getClass().getResource(JSANConstants.JSAN_GRAPHICS_PREFIX+"barEmpty.png"));
		} catch (Exception e) {
			Logger.logln(NAME+"Error loading anonymity bar pictures (See line 123).", LogOut.STDERR);
		}
		
		pointer = new Pointer();
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		this.setBackground(Color.WHITE);
		this.setSize(220, 504);
		
		Graphics2D g2d = (Graphics2D)g;
		g2d.setStroke(new BasicStroke(1f));
		
		if (!showPointer)
			g2d.drawImage(barEmpty, (232 / 2) - 90 + 3, MINY-5, null);
		else if (percentToGoal >= 100)
			g2d.drawImage(barFull, (232 / 2) - 90 + 3, MINY-5, null);
		else
			g2d.drawImage(bar, (232 / 2) - 90 + 3, MINY-5, null);
		
		Color startingColor = Color.GREEN;
		Color endingColor = Color.RED;
		
		//Drawing gradient "intensity" line 647
		if (showPointer) {
			for (int y = MAXY; y > 58; y--) {
				float ratio = (float) (y - 58) / (float) (MAXY - 58);
				int red = (int)(endingColor.getRed() * ratio + startingColor.getRed() * (1 - ratio));
				int green = (int)(endingColor.getGreen() * ratio + startingColor.getGreen() * (1 - ratio));
				int blue = (int)(endingColor.getBlue() * ratio + startingColor.getBlue() * (1 - ratio));
				
				Color stepColor = new Color(red, green, blue);
				g2d.setColor(stepColor);

				if (y <= MAXY - pointer.getY() + 50)
					break;
				else
					g2d.drawLine((232 / 2) - 72, y, (232 / 2) - 42, y);
			}
		}
		
		g2d.setColor(Color.BLACK);
		
		percFont = new Font("Helvatica", Font.PLAIN, 14);
		g2d.setFont(percFont);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
		
		//Drawing Percentages
		for (int i = 0 ; i < PERCENTTEXT.length; i++) {
			g2d.drawString(PERCENTTEXT[i], 110, MINY + 8 + (((MAXY + MINY * 2) / PERCENTTEXT.length) * i - (i * 9)));
		}
	}
	
	/**
	 * Should be called whenever the designer wants the anonymity bar's current gauge value to be shown
	 * @param show - true or false depending on whether or not you want the gauge to be visible
	 */
	public void showPointer(Boolean show) {
		showPointer = show;
		repaint();
	}
	
	/**
	 * This should be called whenever there have been changes to the test document (or the test document's being processed
	 * for the first time) so that the arrow may move accordingly
	 */
	public void updateAnonymityBar() {
		int current = (int)(DriverEditor.taggedDoc.getAnonymityIndex() + .5);
		int max = (int)(DriverEditor.taggedDoc.getTargetAnonymityIndex() + .5);

		percentToGoal = ((double)current/max)*100;
		pointer.setPercentages(current,max);
		
		if (percentToGoal > 100)
			percentToGoal = 100;
		
		main.anonymityDescription.setText("<html><center>You are "+((int)percentToGoal)+"%<br>of the way to<br>your goal</center><html>");
		repaint();
	}
	
	/**
	 * Created for a quick and easy way to get the percent that the pointer uses for use in the text description below the bar
	 */
	public int getAvgPercentChangeNeeded() {
		return pointer.getMaxPercentage() - pointer.getPercentage();
	}
	
	/**
	 * Resets the bar to it's default values and image, used for reprocessing.
	 */
	public void reset() {
		showPointer(false);
		pointer.setPercentages(0, 100);
		main.anonymityDescription.setText("<html><center>Re-processing...<br>Please Wait</center></html>");
		repaint();
	}
}
