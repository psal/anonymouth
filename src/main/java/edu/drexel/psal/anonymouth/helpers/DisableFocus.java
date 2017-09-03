package edu.drexel.psal.anonymouth.helpers;

import java.awt.Component;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 * Because focus traversal is ugly, not many people use it, and it doesn't make any sense for
 * Anonymouth since it doesn't have account forms or places where it makes sense.
 * 
 * To use, simply pass in the JFrame via removeAllFocus() and that's all you need to do, it will
 * recursively traverse through the component hierarchy and turn off focus for each "leaf" component
 * 
 * @author Marc Barrowclift
 */
public class DisableFocus {
	
	/**
	 * Disables focus for all "leaf" components in the passed JFrame's component hierarchy
	 * (leaf meaning anything that can't hold another component like JButtons, JCheckbox, etc.)
	 * 
	 * @param frame
	 * 		The JFrame you want to disable all "leaf" component focus on
	 */
	public static void removeAllFocus(JFrame frame) {
		removeAllFocus(frame.getContentPane());
	}
	
	/**
	 * Disables focus for all "leaf" components in the passed JDialog's component hierarchy
	 * (leaf meaning anything that can't hold another component like JButtons, JCheckbox, etc).
	 * 
	 * @param dialog
	 * 		The JDialog you want to disable all "leaf" component focus on
	 */
	public static void removeAllFocus(JDialog dialog) {
		removeAllFocus(dialog.getContentPane());
	}
	
	/**
	 * Used internally to recursively traverse through the root pane of the JFrame until all
	 * "leaf" components have had focus disabled.
	 * 
	 * @param component
	 * 		The current JPanel or JTabbedPane in the traversal
	 */
	private static void removeAllFocus(Component component) {
		if (component.getClass().toString().contains("JTabbedPane")) {
			try {
				component.setFocusable(false);
				Component[] components = ((JTabbedPane)component).getComponents();
				
				//If it does have children, then we will call disableAllFocus on it as well
				for (int i = 0; i < components.length; i++) {
					removeAllFocus(components[i]);
				}
			//If we couldn't cast to JTabbedPane, assume it's a leaf (like a JButton, JCheckbox, etc.).
			} catch (ClassCastException e) {
				component.setFocusable(false);
			}
		} else {
			try {
				Component[] components = ((JPanel)component).getComponents();
				//If it does have children, then we will call disableAllFocus on it as well
				for (int i = 0; i < components.length; i++) {
					removeAllFocus(components[i]);
				}
			//If we couldn't cast to JPanel, assume it's a leaf (like a JButton, JCheckbox, etc.).
			} catch (ClassCastException e) {
				component.setFocusable(false);
			}
		}
	}
}
