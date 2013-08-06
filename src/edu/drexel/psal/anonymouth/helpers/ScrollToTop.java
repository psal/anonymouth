package edu.drexel.psal.anonymouth.helpers;

import java.awt.Point;
import javax.swing.JScrollPane;

/**
 * ==========Made with help from Carl Smotricz from StackOverflow==========<br>
 * Simple class to ensure that the scroll pane's scroll position is ACTUALLY
 * set. The reason it seems behind simply making the call doesn't work is that
 * if done so anywhere near setting text in it, the scroll adjust would happen
 * first BEFORE the text is inserted. This defeats the purpose of the call since
 * the text being inserted also scrolls the pane to the bottom, thus overwriting
 * our changes.<br><br>
 * 
 * In order to solve this, we need a sort of buffer to ensure this happens AFTER
 * the text is inserted into the scroll pane. Simply call:<br><br>
 * 
 * SwingUtilities.invokeLater(new ScrollToTop(new Point(0, 0), myScrollPane);<br><br>
 * 
 * To activate after you have set the new text in the respective pane. You may of
 * course feel free to adjust to scroll pane to anywhere you want, not just the top,
 * by changing the numerical values in the Point declaration (however, this is a
 * very awful way to do it, it would be better in this case to update this class
 * to better support abilities like "Scroll to center", "Scroll to Left", etc., but
 * as of now we have no need for that functionality.
 * 
 * @author Carl Smotricz from StackOverflow.com
 * @author Marc Barrowclift
 */
public class ScrollToTop implements Runnable {
	
	private Point point;
	private JScrollPane scrollPane;
	
	public ScrollToTop(Point point, JScrollPane scrollPane) {
		this.point = point;
		this.scrollPane = scrollPane;
	}
	
	/**
	 * DO NOT CALL DIRECTLY, in order to adjust your scroll pane use:<br><br>
	 * 
	 * SwingUtilities.invokeLater(new ScrollToTop(new Point(0, 0), myScrollPane);<br><br>
	 * 
	 * For more information see class documentation.
	 */
	public void run() {
		scrollPane.getViewport().setViewPosition(point);
	}
}