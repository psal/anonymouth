package edu.drexel.psal.anonymouth.gooie;

import java.io.File;
import java.util.Scanner;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import edu.drexel.psal.JSANConstants;
import edu.drexel.psal.jstylo.generics.Logger;

public class SuggestionsWindow extends JFrame {

	private static final long serialVersionUID = 1L;
	private final String NAME = "( "+this.getClass().getSimpleName()+" ) - ";
	private final String filePath = JSANConstants.JSAN_EXTERNAL_RESOURCE_PACKAGE+"suggestions.txt";
	private String text = "";
	private JTextPane textPane;
	private JScrollPane textScrollPane;

	public SuggestionsWindow() {
		init();
	}
	
	public void init() {
		textPane = new JTextPane();
		
		readFile();
		textPane.setText(text);
		textPane.setBorder(BorderFactory.createEmptyBorder(1,3,1,3));
		textPane.setEditable(false);
		textPane.setFocusable(false);
		
		textScrollPane = new JScrollPane(textPane);
		
		this.add(textScrollPane);
		this.setSize(600, 640);
		this.setLocationRelativeTo(null);
		this.setTitle("General Suggestions");
		this.setVisible(false);
	}
	
	public void readFile() {
		try {
			File file = new File(filePath);
			Scanner scanner = new Scanner(file);
			
			while (scanner.hasNext()) {
				text = text.concat(scanner.nextLine() + "\n");
			}
			
			scanner.close();
		} catch (Exception e) {
			Logger.logln(NAME+"Error reading from suggestions file");
		}
	}
	
	public void openWindow() {
		this.setLocationRelativeTo(null); // makes it form in the center of the screen
		this.setVisible(true);
	}
}
