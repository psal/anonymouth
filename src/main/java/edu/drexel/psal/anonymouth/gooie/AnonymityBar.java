package edu.drexel.psal.anonymouth.gooie;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;

import javax.swing.JPanel;

import edu.drexel.psal.jstylo.generics.Logger;

/**
 * The template for the new and improved Anonymity bar!<br><br>
 * 
 * No more Photoshop pictures, the bar is 100% Java 2D Graphics and resizes
 * dynamically based on the size of the panel/frame the bar is being drawn in.
 * If you wish to make any changes to the Anonymouth Anonymity Bar, it would
 * be easier and quicker to test them out in the standalone AnonymityBar.java
 * file located in:<br>
 * 		<code>jsan_resources</code><br>
 * In there are test methods so you can quickly modify and change values and
 * easily see whether or not they work quickly.
 * 
 * @author Marc Barrowclift
 *
 */
public class AnonymityBar extends JPanel {

	private static final long serialVersionUID = 1L;
	/**
	 * The gap between the bottom of the drawing panel and the bottom of the
	 * bulb. Must be at least 20, otherwise, the bulb starts getting drawn off
	 * the bottom of the drawing panel
	 */
	private final String NAME = "( "+this.getClass().getSimpleName()+" ) - ";
	private final float BAR_STROKE = 5.0f; //The thinkness of the stroke used for the bar
	private final int BULB_BOTTOM_GAP = 60;
	private final int TUBE_VERTICAL_GAP = 13; //the gap you want the tub to have from the top and bottom of the drawing panel
	private final Color BORDER_COLOR = new Color(0.71f, 0.71f, 0.71f); //The color of the border.
	private final Color BACKGROUND_COLOR = Color.WHITE;
	private final Color START_COLOR = Color.RED;
	private final Color END_COLOR = Color.GREEN;

	//Drawing variables
	private int barBulbHeight; //The height of the bottom "Bulb" of the bar
	private int barBulbWidth; //The width of the bottom "Bulb" of the bar
	private int barBulbY; //The starting Y point of the bulb
	private int barBulbX; //The starting X point of the bulb

	private int bulbBarGap; //The decrease in width to be done on the tub with respect to the bar bulb width
	private int barTubeHeight; //The height of the main tube part of the bar
	private int barTubeWidth; //The width of the main tube part of the bar
	private int barTubeY; //The starting Y point of the tube
	private int barTubeX; //The starting X point of the tube

	//Fill Variables
	private Color color = START_COLOR; //The current color of the bar, default is Red.
	private int bottomY; //Where increases in the fill bar should start
	private int topY; //The max Y value for increases in the fill bar, nothing should exceed this
	private int curHeight; //The current height of the bar's fill
	private float curPercent = 0.0f; //The current percent to the goal (10 for 10%, 25 for 25%, etc.)
	private float newValue = 0.0f; //The new percent obtained from the tagged document (but not yet implemented)
	private float maxFill = 100.0f; //The maximum value the fill can reach, MUST ONLY BE SET ONCE!
	private boolean showFill;
	private String percentString = "";
	
	//Others
	private GUIMain main;

	/**
	 * Updates the panel width and height based on the new values of the frame
	 * height and width. In Anonymouth, the change we will most likely see is
	 * a change in height, but we want to update the width to make
	 * sure.<br><br>
	 * 
	 * Also automatically calls updateDrawingVariables() for you, because as
	 * the name suggests it updates all variables.
	 */
	public void updateForNewSize() {
		/**
		 * Only values we actually have to manually change, the rest use these updated values to update their respective values
		 */
		updateDrawingVariables();
		updateTubeFill();
		updateColor();
	}

	/**
	 * Updates all the drawing variables dynamically based on what the width
	 * and height of the frame (and thus the panel) currently are. For
	 * Anonymouth, we will not use the frame width and heights as the
	 * reference but rather the drawing panel width and height.
	 */
	private void updateDrawingVariables() {
		//=========================================================
		//***					BAR BULB						***
		//=========================================================
		barBulbWidth = (int)((main.anonymityWidth / 2) + 0.5);
		barBulbHeight = barBulbWidth; //We want the bottom to be a nice circle, so same dimension here
		barBulbY = main.anonymityHeight - barBulbHeight - BULB_BOTTOM_GAP - ((int)(BAR_STROKE + 0.5));
		barBulbX = (int)(((main.anonymityWidth / 2) - (barBulbWidth / 2)) + 0.5);

		/**
		 * We want to make sure that the bar tube width is always a nice ratio
		 * smaller than the bar bulb width. This is so we actually have it
		 * look a bit more like a thermometer. We want this to dynamically
		 * change based on what the new barBulbWidth value is (so that way the
		 * tube width is guaranteed to be the same nice ratio smaller than the
		 * bar bulb width).
		 */
		bulbBarGap = barBulbWidth - (int)((barBulbWidth / 1.1) + 0.5) + ((int)(BAR_STROKE + 0.5));


		//=========================================================
		//***					BAR TUBE						***
		//=========================================================
		/**
		 * These do not need to dynamically change using the panel width and
		 * height since it's already taken care of above and these variables
		 * are updated with respect to them (with the exception of
		 * barTubeHeight).
		 */
		barTubeWidth = barBulbWidth - (bulbBarGap * 2); //Times 2 because of left AND right for the gap
		barTubeY = TUBE_VERTICAL_GAP;
		barTubeX = barBulbX + bulbBarGap;
		/**
		 * Times 2 to the TUBE_VERTICAL_GAP because of top AND bottom for the
		 * gap and Times 2 to the BAR_STROKE To accommodate for the additional
		 * stroke size on the top AND bottom (so it's not drawn off the
		 * panel)<br><br>
		 * 
		 * Also subtracting it's starting barTubeY value.
		 */
		barTubeHeight = main.anonymityHeight - (TUBE_VERTICAL_GAP * 2) - (((int)(BAR_STROKE + 0.5)) * 2) - barTubeY;
		
		updateTubeFill();
	}
	
	/**
	 * Updates the fill gauge of the tube. This includes updating all
	 * variables relating to the fill, updating the color automatically for
	 * you depending on the fill amount, and also painting the changes.
	 */
	private void updateTubeFill() {
		bottomY = (int)(((barBulbHeight/5) + 0.5) + ((barBulbWidth/barBulbHeight)) + 0.5) + barBulbY-((int)(BAR_STROKE+0.5));
		topY = barTubeY;
		curPercent = newValue / maxFill; //In Anonymouth, this is where you get an calculate the percent (range 0-100%, you have to adjust it)
		curHeight = (int)((bottomY-topY)*curPercent + 0.5);

		updateColor();
		
		//Obtaining the string we will use for displaying the percent text
		BigDecimal bd = new BigDecimal(Float.toString(curPercent*100));
		bd = bd.setScale(4, BigDecimal.ROUND_HALF_UP);
		percentString = bd+"%";
		
		//Just in case the Anonymity bar is hidden, we want to also update the JLabel with the percent string
		if (main.anonymityPercent != null) {
			main.anonymityPercent.setText(percentString);
			main.anonymityPercent.setForeground(color);
		}
		
		repaint();
	}
	
	/**
	 * Just updates the color, shouldn't be called by the programmer any
	 * additional places.
	 */
	private void updateColor() {
		if (curPercent <= 0) {
			color = new Color(255, 0, 0);
		} else if (curPercent >= 100) {
			color = new Color(0, 255, 0);
		} else {
			int red = (int)(END_COLOR.getRed() * curPercent + START_COLOR.getRed() * (1 - curPercent));
			int green = (int)(END_COLOR.getGreen() * curPercent + START_COLOR.getGreen() * (1 - curPercent));
			int blue = (int)(END_COLOR.getBlue() * curPercent + START_COLOR.getBlue() * (1 - curPercent));

			color = new Color(red, green, blue);
		}
	}
	
	/**
	 * This is the method that should be called by any outside source
	 * when they want the bar to be updated. Anything else (not including
	 * window resizing and it's call to updateForNewSize()) is strictly
	 * for internal purposes.
	 */
	public void updateBar() {
		newValue = (float)main.editorDriver.taggedDoc.getCurrentChangeNeeded(); //Getting the new percent
		newValue = ((maxFill - newValue) / maxFill) * 100;

		updateTubeFill(); //update the other variables
		
		Logger.logln(NAME+"CurPercent = " + curPercent + ": " + newValue + " / " + maxFill);
	}

	/**
	 * The bar's paint component, where all updates are painted to the screen
	 * 
	 * @param g
	 *        Instance of Graphics
	 */
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D)g;
		
		//========================================================================
		//******************		BAR BACKGROUND		**************************
		//========================================================================
		/**
		 * Draws the background of the bar. The reason we're doing it this way instead of just painting
		 * the whole bloody thing is this way we can have the surrounding color of the bar be transparent
		 * while only the background color of the bar itself is painted.
		 * 
		 * Also must be done to g2d and NOT gbi, because otherwise it would get overridden.
		 */
		g2d.setColor(BACKGROUND_COLOR);
		g2d.fillRoundRect(barTubeX, barTubeY, barTubeWidth, barTubeHeight-BULB_BOTTOM_GAP, barTubeWidth, barTubeWidth);
		
		//========================================================================
		//******************		BAR OUTLINE		******************************
		//========================================================================
		/*
		 *  Creates the buffered image. We need to do this in order to clean up parts of the bar
		 *  (to make it seem like one solid shape). You must draw to gbi.
		 */
		BufferedImage buffImg = new BufferedImage(main.anonymityWidth, main.anonymityHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D gbi = buffImg.createGraphics();
		
		//Makes the text and lines not rough and hideous but smooth and clean
		gbi.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		gbi.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		gbi.setStroke(new BasicStroke(BAR_STROKE));
		gbi.setColor(BORDER_COLOR);

		//Draws all shapes that make up the bar shape into the buffered image
		//Drawing the tube
		gbi.setColor(BORDER_COLOR);
		gbi.drawRoundRect(barTubeX, barTubeY, barTubeWidth, barTubeHeight-BULB_BOTTOM_GAP, barTubeWidth, barTubeWidth);
		//Clearing the parts below where it meets the bulb
		if (!showFill) {
			gbi.setColor(BACKGROUND_COLOR);
			gbi.fillOval(barBulbX, barBulbY, barBulbWidth, barBulbHeight);
		}
		
		//Drawing the bulb
		if (showFill) {
			gbi.setColor(color);
			gbi.fillOval(barBulbX, barBulbY, barBulbWidth, barBulbHeight);
		}
		gbi.setColor(BORDER_COLOR);
		gbi.drawOval(barBulbX, barBulbY, barBulbWidth, barBulbHeight);
		//Clearing the top arc part of the bulb that's inside the tube (to get a solid outline shape with nothing inside)
		gbi.setComposite(AlphaComposite.Clear);
		gbi.fillRect(barTubeX+((int)((BAR_STROKE/2)+0.5)), barBulbY-((int)(BAR_STROKE+0.5)),
			barTubeWidth-((int)(BAR_STROKE + 0.5)),
			(int)(((barBulbHeight/5) + 0.5) + 3));
		
		//========================================================================
		//******************		BAR FILL (IF ANY)		**********************
		//========================================================================
		if (showFill) {
			gbi.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_OVER, 1.0f)); //Paint just inside existing border
			gbi.setColor(color);
			/**
			 * The division for the last two lines allow the fill top to slowly round to fit the top of the rounded tube as
			 * it ascends
			 */
			gbi.fillRoundRect(barTubeX, bottomY-curHeight,
					barTubeWidth, curHeight+(barTubeWidth/2),
					(int)(barTubeWidth*(double)((double)curHeight/(double)(bottomY-topY))),
					(int)(barTubeWidth*(double)((double)curHeight/(double)(bottomY-topY))));
		}

		//========================================================================
		//******************		BAR PERCENT LABEL		**********************
		//========================================================================
		gbi.setPaintMode();
		gbi.setColor(color);
		gbi.setFont(new Font("Helvetica", Font.BOLD, 16));

		char[] toDraw = percentString.toCharArray();
		int length = gbi.getFontMetrics().charsWidth(toDraw, 0, percentString.length());

		gbi.drawString(percentString, (int)(((int)((main.anonymityWidth/2)+0.5))-((int)((length/2)+0.5))),
				main.anonymityHeight-(int)(BULB_BOTTOM_GAP/1.75));
						
		//Draws the finished image to screen!
		g2d.drawImage(buffImg, null, 0, 0);
	}

	/**
	 * Constructor, initializes all variables
	 *
	 * @param main 
	 *        GUIMain instance
	 */
	public AnonymityBar(GUIMain main) {
		this.setDoubleBuffered(true);
		this.main = main;
		this.setSize(200, 400);
		showFill = false;

		updateDrawingVariables(); //readying the drawing variables for the default height and width
	}

	/**
	 * Shows or hides the bar's fill.
	 *
	 * @param  show
	 *         Whether or not you want the bar's fill to be visible
	 */
	public void showFill(boolean show) {
		showFill = show;
		repaint(); //no variables to update, just paint the fill
	}

	/**
	 * Sets the maximum value the bar's fill can achieve. NOTE: THIS MUST ONLY
	 * BE CALLED ONCE IN BACKEND INTERFACE.
	 * 
	 * @param maxPercent
	 *        The value you want the bar to treat as the max, basically as the
	 *        "100" in 0-100%.
	 */
	public void setMaxFill(double maxPercent) {
		maxFill = (float)maxPercent;
	}

	/**
	 * Resets the bar to it's default fill value and color, should be called
     * for reprocessing
     */
    protected void reset() {
    	showFill = false;
    	curPercent = 0.0f;
    	main.anonymityDescription.setText("<html><center>Re-processing...<br>Please Wait</center></html>");
    	updateTubeFill(); //Since the fill changed, we want to unsure all variables are updated before painting
	}
}
