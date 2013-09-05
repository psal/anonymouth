package edu.drexel.psal.anonymouth.gooie;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

import edu.drexel.psal.anonymouth.utils.TaggedDocument;
import edu.drexel.psal.jstylo.generics.Logger;

/**
 * Provides the framework for a right-click menu in the editor.
 * 
 * @author Marc Barrowclift
 */
public class RightClickMenu extends JPopupMenu {

	//Constants
	private static final long serialVersionUID = 1L;
	private final String NAME = "( " + this.getClass().getSimpleName() + " ) - ";
	private final HashSet<Character> EOS;
	
	//Variables
	private JMenuItem cut;
	private JMenuItem copy;
	private JMenuItem paste;
	private JSeparator separator;
	private JMenuItem combineSentences;
	private JMenuItem resetHighlighter;
	private GUIMain main;
	
	//Listeners
	private ActionListener cutListener;
	private ActionListener copyListener;
	private ActionListener pasteListener;
	private ActionListener combineSentencesListener;
	private ActionListener resetHighlighterListener;
	private PopupListener popupListener;

	/**
	 * CONSTRUCTOR
	 */
	public RightClickMenu(GUIMain main) {
		cut = new JMenuItem("Cut");
		copy = new JMenuItem("Copy");
		paste = new JMenuItem("Paste");
		separator = new JSeparator();
		combineSentences = new JMenuItem("Make a single sentence");
		resetHighlighter = new JMenuItem("Reset Highlighter");
		
		EOS = new HashSet<Character>(3);
		EOS.add('.');
		EOS.add('!');
		EOS.add('?');
		
		this.add(cut);
		this.add(copy);
		this.add(paste);
		this.add(separator);
		this.add(combineSentences);
		this.add(resetHighlighter);
		this.main = main;
		
		initListeners();
	}

	/**
	 * Readies all the listeners for each menu item
	 */
	public void initListeners() {
		cutListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				main.clipboard.cut();
			}
		};
		cut.addActionListener(cutListener);
		
		copyListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				main.clipboard.copy();
			}
		};
		copy.addActionListener(copyListener);
		
		pasteListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				main.clipboard.paste();
			}
		};
		paste.addActionListener(pasteListener);
		
		combineSentencesListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String text = main.documentPane.getText();
				main.editorDriver.taggedDoc = new TaggedDocument(main, text, true);
				
				for (int i = popupListener.start; i < popupListener.stop; i++) {
					if (EOS.contains(text.charAt(i))) {
						main.editorDriver.taggedDoc.eosTracker.setIgnore(i, true);
					}
				}
				

				main.editorDriver.newCaretPosition[0] = popupListener.start;
				main.editorDriver.newCaretPosition[1] = main.editorDriver.newCaretPosition[0];
				main.editorDriver.sentIndices[0] = 0;
				
				main.editorDriver.taggedDoc.makeAndTagSentences(text, true);
				
				main.editorDriver.syncTextPaneWithTaggedDoc();
			}
		};
		combineSentences.addActionListener(combineSentencesListener);
		
		resetHighlighterListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					main.editorDriver.taggedDoc.eosTracker.resetEOSCharacters();
					main.editorDriver.taggedDoc = new TaggedDocument(main, main.documentPane.getText(), false);
					main.editorDriver.syncTextPaneWithTaggedDoc();
					main.versionControl.init();
				} catch (Exception e1) {
					Logger.logln(NAME+"Editor reset FAILED");
					Logger.logln(e1);
				}
			}
		};
		resetHighlighter.addActionListener(resetHighlighterListener);
		
		popupListener = new PopupListener(this, main, this);
		main.documentPane.addMouseListener(popupListener);
	}
	
	/**
	 * Enables or disables the combine sentences action.
	 * 
	 * @param b
	 * 		Whether or not to enable to menu item.
	 */
	public void enableCombineSentences(boolean b) {
		combineSentences.setEnabled(b);
	}
	
	/**
	 * Allows us to enable or disable all our cut, copy, and paste
	 * items.
	 * 
	 * @param cut
	 * 		Whether or not to enable the cut right click menu item.
	 * @param copy
	 * 		Whether or not to enable the copy right click menu item.
	 * @param paste
	 * 		Whether or not to enable the paste right click menu item.
	 */
	public void setEnabled(boolean cut, boolean copy, boolean paste) {
		this.cut.setEnabled(cut);
		this.copy.setEnabled(copy);
		this.paste.setEnabled(paste);
	}
}

/**
 * The MouseAdapter that handles displaying the right-click menu and decides whether or not it's appropriate to enable the combining sentences option (i.e., if the user is only
 * selecting one sentence we can't possible combine it).
 * @author Marc Barrowclift
 *
 */
class PopupListener extends MouseAdapter {
	
	private final String NAME = "( " + this.getClass().getSimpleName() + " ) - ";
	private final String EOS = ".!?";
	private JPopupMenu popup;
	private GUIMain main;
	private RightClickMenu rightClickMenu;
	protected int start;
	protected int stop;

	/**
	 * CONSTRUCTOR
	 * 
	 * @param popupMenu
	 * 		An instance of the menu desired to present when the user right clicks.
	 * @param main
	 * 		An instance of GUIMain.
	 * @param rightClickMenu
	 * 		An instance of RightClickMenu.
	 */
	public PopupListener(JPopupMenu popupMenu, GUIMain main, RightClickMenu rightClickMenu) {
		popup = popupMenu;
		this.main = main;
		this.rightClickMenu = rightClickMenu;
	}

	@Override
	public void mousePressed(MouseEvent e) {
		try {
			showPopup(e);
		} catch (Exception e1) {
			Logger.logln(NAME+"Error occurred while attempting to show popup, will force show");
			Logger.logln(e1);
			
			rightClickMenu.enableCombineSentences(false);
			popup.show(e.getComponent(), e.getX(), e.getY());
		}	
	}
	
	/**
	 * Displays the right-click menu. Also checks whether or not the user has
	 * selected acceptable text and enables/disables combining sentences based on that.
	 * @param e
	 */
	private void showPopup(MouseEvent e) {
		/*
		 * While it does seem a bit silly, we need a check to make sure the
		 * documentPane is enabled since this method will get called during
		 * the processing stage and we don't want anything below to be fired
		 * during so. This checks to make sure the user has selected
		 * appropriate text for the combine sentences option to be enabled.
		 */
		if (e.isPopupTrigger() && main.documentPane.isEnabled()) {
			if (main.editorDriver.newCaretPosition[0] > main.editorDriver.newCaretPosition[1]) {
				start = main.editorDriver.newCaretPosition[1];
				stop = main.editorDriver.newCaretPosition[0];
			} else {
				start = main.editorDriver.newCaretPosition[0];
				stop = main.editorDriver.newCaretPosition[1];
			}

			if (start == stop) {
				rightClickMenu.enableCombineSentences(false);
				rightClickMenu.setEnabled(false, false, true);
			} else {				
				int numOfEOSes = 0;
				String text = main.documentPane.getText();
				for (int i = start; i < stop-1; i++) {
					if (EOS.contains(text.substring(i, i+1))) {
						numOfEOSes++;
					}
				}

				if (numOfEOSes >= 1)
					rightClickMenu.enableCombineSentences(true);
				else
					rightClickMenu.enableCombineSentences(false);
				rightClickMenu.setEnabled(true, true, true);
			}
			
			popup.show(e.getComponent(), e.getX(), e.getY());
		}
	}
}
