package edu.drexel.psal.anonymouth.gooie;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

import edu.drexel.psal.anonymouth.utils.SentenceTools;
import edu.drexel.psal.anonymouth.utils.TaggedDocument;
import edu.drexel.psal.anonymouth.utils.TaggedSentence;

/**
 * Provides the framework for a right-click menu in the editor.
 * 
 * @author Marc Barrowclift
 */
public class RightClickMenu extends JPopupMenu {

	private JMenuItem cut;
	private JMenuItem copy;
	private JMenuItem paste;
	private JSeparator separator;
	private JMenuItem combineSentences;
	private JMenuItem resetHighlighter;
	private GUIMain main;
	private ActionListener cutListener;
	private ActionListener copyListener;
	private ActionListener pasteListener;
	private ActionListener combineSentencesListener;
	private ActionListener resetHighlighterListener;
	private MouseListener popupListener;
	public ArrayList<String[]> sentences;

	private static final long serialVersionUID = 1L;

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
				int size = sentences.size();
				int pastLength = 0;
				int length = 0;
				ArrayList<TaggedSentence> taggedSentences = new ArrayList<TaggedSentence>();
				
				//Goes through the selected sentences and for each EOS character we find (EXCLUDING the EOS character at the end of the last sentence) marks them as ignorable.
				for (int i = 0; i < size; i++) {
					length = sentences.get(i)[0].length();
					char character = main.documentPane.getText().charAt(length-1+PopupListener.mark+pastLength);

					if ((character == '.' || character == '!' || character == '?') && size-1 != i) {
						EditorDriver.taggedDoc.specialCharTracker.setIgnore(length - 1 + PopupListener.mark+pastLength, true);
					}
										
					taggedSentences.add(EditorDriver.taggedDoc.getTaggedSentenceAtIndex(length + PopupListener.mark + pastLength));
					pastLength += length;
				}
				
				//We're borrowing a variable used by translations to solve a similar purpose, we do not want the InputFilter to fire removeReplaceAndUpdate in the DriverEditor.
				InputFilter.ignoreTranslation = true;
				
				TaggedSentence replacement = EditorDriver.taggedDoc.concatSentences(taggedSentences);
				EditorDriver.taggedDoc.removeMultipleAndReplace(taggedSentences, replacement);
				EditorDriver.update(main, true);
				
				int[] selectedSentInfo = EditorDriver.calculateIndicesOfSentences(PopupListener.mark)[0];

				//We want to make sure we're setting the caret at the actual start of the sentence and not in white space (so it gets highlighted)
				int space = 0;
				String text = main.documentPane.getText();
				while (text.charAt(selectedSentInfo[1] + space)  == ' ' || text.charAt(selectedSentInfo[1] + space) == '\n') {
					space++;
				}
				
				main.documentPane.getCaret().setDot(selectedSentInfo[1]+space);
				EditorDriver.selectedSentIndexRange[0] = selectedSentInfo[1];
				EditorDriver.selectedSentIndexRange[1] = selectedSentInfo[2];
				EditorDriver.moveHighlight(main, EditorDriver.selectedSentIndexRange);
			}
		};
		combineSentences.addActionListener(combineSentencesListener);
		
		resetHighlighterListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				EditorDriver.taggedDoc.specialCharTracker.resetEOSCharacters();
				EditorDriver.taggedDoc = new TaggedDocument(main.documentPane.getText());
				EditorDriver.isFirstRun = true;
				
				int[] selectedSentInfo = EditorDriver.calculateIndicesOfSentences(EditorDriver.currentCaretPosition)[0];
				EditorDriver.selectedSentIndexRange[0] = selectedSentInfo[1];
				EditorDriver.selectedSentIndexRange[1] = selectedSentInfo[2];
				EditorDriver.moveHighlight(main, EditorDriver.selectedSentIndexRange);
			}
		};
		resetHighlighter.addActionListener(resetHighlighterListener);
		
		popupListener = new PopupListener(this, main, this);
		main.documentPane.addMouseListener(popupListener);
	}
	
	/**
	 * Enables or disables the combine sentences action.
	 * @param b - Whether or not to enable to menu item.
	 */
	public void enableCombineSentences(boolean b) {
		combineSentences.setEnabled(b);
	}
	
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
	
	private final String EOS = ".!?";
	private JPopupMenu popup;
	private GUIMain main;
	private SentenceTools sentenceTools;
	private RightClickMenu rightClickMenu;
	public static int mark;

	/**
	 * CONSTRUCTOR
	 * @param popupMenu - An instance of the menu desired to present when the user right clicks.
	 * @param main - An instance of GUIMain.
	 * @param rightClickMenu - An instance of RightClickMenu.
	 */
	public PopupListener(JPopupMenu popupMenu, GUIMain main, RightClickMenu rightClickMenu) {
		popup = popupMenu;
		this.main = main;
		sentenceTools = new SentenceTools();
		this.rightClickMenu = rightClickMenu;
	}

	@Override
	public void mousePressed(MouseEvent e) {
		maybeShowPopup(e);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		maybeShowPopup(e);
	}
	
	/**
	 * Displays the right-click menu. Also checks whether or not the user has selected acceptable text and enables/disables combining sentences based on that.
	 * @param e - MouseEvent
	 */
	private void maybeShowPopup(MouseEvent e) {
		/*
		 * While it does seem a bit silly, we need a check to make sure the documentPane is enabled since this method will get called during the processing stage and
		 * we don't want anything below to be fired during so. This checks to make sure the user has selected appropriate text for the combine sentences option to be
		 * enabled.
		 */
		if (e.isPopupTrigger() && main.documentPane.isEnabled()) {
			mark = main.documentPane.getCaret().getMark();
			int dot = main.documentPane.getCaret().getDot();
			
			if (dot == mark) {
				rightClickMenu.enableCombineSentences(false);
				rightClickMenu.setEnabled(false, false, true);
			} else {
				String text = main.documentPane.getText();
				
				int padding = 0;
				int length = text.length();
				while (!EOS.contains(text.substring(dot-1+padding, dot+padding))) {
					padding++;
					
					if (dot+padding >= length)
						break;
				}
				
				text = text.substring(mark, dot+padding);
				rightClickMenu.sentences = sentenceTools.makeSentenceTokens(text);
				
				if (rightClickMenu.sentences.size() > 1)
					rightClickMenu.enableCombineSentences(true);
				else
					rightClickMenu.enableCombineSentences(false);
				rightClickMenu.setEnabled(true, true, true);
			}
			
			popup.show(e.getComponent(), e.getX(), e.getY());
		}
	}
}
