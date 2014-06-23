package edu.drexel.psal.anonymouth.engine;

import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.Icon;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.Border;

public class SearchBar extends JTextField {

	private static final long serialVersionUID = 1L;
	private Icon icon;
	private Insets box;
	
	public SearchBar() {
		super();
		this.icon = null;
		
		Border border = UIManager.getBorder("TextField.border");
		JTextField textField = new JTextField();
		this.box = border.getBorderInsets(textField);
	}
	
	public void setIcon (Icon icon) {
		this.icon = icon;
	}
	
	public Icon getIcon () {
		return this.icon;
	}
	
	public void paintComponent (Graphics g) {
		int textX = 2;
		
		if (this.icon!=null) {
			int iconWidth = icon.getIconWidth();
			System.out.println("!!!!!!Search bar Width " + iconWidth);
			int iconHeight = icon.getIconHeight();
			System.out.println("!!!!!!Search bar Height " + iconHeight);
			int x = box.left + 5;
			System.out.println("The value of x is " + x);
			textX = x + iconWidth + 2; // where the text inside starts
			
			int y = (this.getHeight() - iconHeight)/2;
			System.out.println("The value of y is " + y);
			icon.paintIcon(this, g, x, y);
		}
		
		setMargin (new Insets(2,textX, 2, 2));
	}
}
