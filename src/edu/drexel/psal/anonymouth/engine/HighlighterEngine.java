package edu.drexel.psal.anonymouth.engine;

import java.awt.Color;
import java.util.ArrayList;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;

import edu.drexel.psal.anonymouth.gooie.GUIMain;
import edu.drexel.psal.anonymouth.gooie.HighlightMapper;
import edu.drexel.psal.anonymouth.gooie.PropertiesUtil;
import edu.drexel.psal.anonymouth.utils.IndexFinder;
import edu.drexel.psal.jstylo.generics.Logger;
import edu.drexel.psal.jstylo.generics.Logger.LogOut;

/**
 * Manages all highlighter objects including the words to remove and add and sentence highlighting.
 * @author Marc Barrowclift
 *
 */
public class HighlighterEngine {
	
	private final String NAME = "( HighlighterEngine ) - ";
	
	private GUIMain main;
	
	private Highlighter mainHighlight;
	protected Object currentHighlight;
	private ArrayList<HighlightMapper> selectedAddElements = new ArrayList<HighlightMapper>();
	private ArrayList<HighlightMapper> selectedRemoveElements = new ArrayList<HighlightMapper>();
	private ArrayList<HighlightMapper> elementsToRemoveInSentence = new ArrayList<HighlightMapper>();
	
	private DefaultHighlighter.DefaultHighlightPainter painterRemove = new DefaultHighlighter.DefaultHighlightPainter(new Color(255,0,0,128));
	private DefaultHighlighter.DefaultHighlightPainter painterAdd = new DefaultHighlighter.DefaultHighlightPainter(new Color(0,255,0,128));
	private DefaultHighlighter.DefaultHighlightPainter painterHighlight = new DefaultHighlighter.DefaultHighlightPainter(PropertiesUtil.getHighlightColor());
	
	/**
	 * Constructor
	 * @param main - GUIMain instance
	 */
	public HighlighterEngine(GUIMain main) {
		this.main = main;
		mainHighlight = main.documentPane.getHighlighter();
	}
	
	/**
	 * ===========================================================================================================
	 * ------------------------------------CLEAR METHODS AND VARIOUS OTHERS---------------------------------------
	 * ===========================================================================================================
	 */
	
	/**
	 * Clears all words to and and words to remove highlights.
	 */
	public void clearSuggestions() {
		int addSize = selectedAddElements.size();
		for (int i = 0; i < addSize; i++) {
			mainHighlight.removeHighlight(selectedAddElements.get(i).getHighlightedObject());
		}
		int removeSize = selectedRemoveElements.size();
		for (int i = 0; i < removeSize; i++) {
			mainHighlight.removeHighlight(selectedRemoveElements.get(i).getHighlightedObject());
		}
		
		selectedAddElements.clear();
		selectedRemoveElements.clear();
	}
	
	public void clearAll() {
		clearSuggestions();
		removeAutoRemoveHighlights();
		removeSentenceHighlight();
	}
	
	/**
	 * Determines whether or not currentHighlight is initialized or not (Basically just used to check if the document's been processd)
	 * @return
	 */
	public boolean isSentenceHighlighted() {
		return currentHighlight != null;
	}
	
	/**
	 * ===========================================================================================================
	 * ---------------------------------------SENTENCE HIGHLIGHTER METHODS----------------------------------------
	 * ===========================================================================================================
	 */
	
	/**
	 * Sets the sentence highlight color to the passed color.
	 * NOTE, does not refresh the current highlight (if any), you must also call moveHighlight to see the changes.
	 * @param newColor
	 */
	public void setSentHighlightColor(Color newColor) {
		painterHighlight = new DefaultHighlighter.DefaultHighlightPainter(newColor);
	}
	
	/**
	 * "Adds" a sentence highlight to the start and end indices passed. I say "adds" because it's actually more of a "move", but technically
	 * it's always removing and adding a highlight so I went with that to stay consistent.
	 * @param start
	 * @param end
	 */
	public void addSentenceHighlight(int start, int end) {
		try {
			currentHighlight = mainHighlight.addHighlight(start, end, painterHighlight);
		} catch (BadLocationException e) {
			Logger.logln(NAME+"Encountered issue moving sentence highlight to indices "+start+"-"+end, LogOut.STDERR);
		}
	}
	
	/**
	 * Removes the sentence highlight from the editor.
	 */
	public void removeSentenceHighlight() {
		if (currentHighlight != null)
			mainHighlight.removeHighlight(currentHighlight);
	}
	
	/**
	 * ===========================================================================================================
	 * ------------------------------------"WORDS TO ADD" HIGHLIGHTER METHODS-------------------------------------
	 * ===========================================================================================================
	 */
	
	/**
	 * Removes all "Words to add" highlights, if any, from the editor.s
	 */
	public void removeAllAddHighlights() {
		int size = selectedAddElements.size();
		
		for (int i = 0; i < size; i++)
			mainHighlight.removeHighlight(selectedAddElements.get(i).getHighlightedObject());
		selectedAddElements.clear();
	}
	
	/**
	 * Adds all "word to add" highlights for the given word
	 * @param wordToHighlight - The word you want to highlight
	 */
	public void addAllAddHighlights(String wordToHighlight) {
		ArrayList<int[]> index = IndexFinder.findIndices(main.documentPane.getText(), wordToHighlight);
		int indexSize = index.size();		
		
		for (int i = 0; i < indexSize; i++) {
			try {
				selectedAddElements.add(new HighlightMapper(index.get(i)[0], index.get(i)[1], mainHighlight.addHighlight(index.get(i)[0], index.get(i)[1], painterAdd)));
			} catch (BadLocationException e) {
				Logger.logln(NAME+"Problem occurred while trying to highlight word to add \""+wordToHighlight+"\" at indices "+index.get(i)[0]+"-"+index.get(i)[i], LogOut.STDERR);
			}
		}
	}
	
	/**
	 * ===========================================================================================================
	 * ----------------------------------"WORDS TO REMOVE" HIGHLIGHTER METHODS------------------------------------
	 * ===========================================================================================================
	 */
	
	/**
	 * Removes all "Word to remove" highlights from the editor
	 * NOTE: This does NOT remove words to remove highlights that were automatically highlighted within a highlighted sentence by design,
	 * to also clear those you need to call removeAutoRemoveHighlights() as well.
	 */
	public void removeAllRemoveHighlights() {
		int size = selectedRemoveElements.size();
		
		for (int i = 0; i < size; i++)
			mainHighlight.removeHighlight(selectedRemoveElements.get(i).getHighlightedObject());
		selectedRemoveElements.clear();
	}
	
	/**
	 * Adds all "word to remove" highlights to the editor for the given word
	 * @param wordToHighlight
	 */
	public void addAllRemoveHighlights(String wordToHighlight) {
		//If the "word to remove" is punctuation and in the form of "Remove ...'s" for example, we want
		//to just extract the "..." for highlighting
		String[] test = wordToHighlight.split(" ");
		if (test.length >= 2) {
			wordToHighlight = test[1].substring(0, test[1].length()-2);
		}
		
		ArrayList<int[]> index = IndexFinder.findIndices(main.documentPane.getText(), wordToHighlight);
		int indexSize = index.size();

		for (int i = 0; i < indexSize; i++) {
			try {
				selectedRemoveElements.add(new HighlightMapper(index.get(i)[0], index.get(i)[1], mainHighlight.addHighlight(index.get(i)[0], index.get(i)[1], painterRemove)));
			} catch (BadLocationException e) {
				Logger.logln(NAME+"Problem occurred while trying to highlight word to remove at indices "+index.get(i)[0]+"-"+index.get(i)[i], LogOut.STDERR);
			}
		}
	}
	
	/**
	 * ===========================================================================================================
	 * ---------------------------AUTOMATICALLY HIGHLIGHTED "WORDS TO REMOVE" METHODS-----------------------------
	 * ===========================================================================================================
	 */
	
	/**
	 * Removes all word to remove that were automatically highlighted within a selected sentence.
	 */
	public void removeAutoRemoveHighlights() {
		int size = elementsToRemoveInSentence.size();
		
		for (int i = 0; i < size; i++)
			mainHighlight.removeHighlight(elementsToRemoveInSentence.get(i).getHighlightedObject());
		elementsToRemoveInSentence.clear();
	}
	
	/**
	 * Adds all highlights for all words to remove currently within a selected sentence
	 * @param start
	 * @param end
	 */
	public void addAutoRemoveHighlights(int start, int end) {
		//if we don't increment by one, it gets the previous sentence.
		String[] words = main.editorDriver.taggedDoc.getWordsInSentenceNoDups(main.editorDriver.taggedDoc.getTaggedSentenceAtIndex(start+1));
		int removeSize = main.wordSuggestionsDriver.getRemoveSize();
		ArrayList<int[]> index = new ArrayList<int[]>(removeSize);
		ArrayList<String[]> topToRemove = main.wordSuggestionsDriver.getTopToRemove();
		
		/**
		 * We have this surrounded by a try catch to protect against the possibility of the updateSuggestionsThread is EditorDriver
		 * and the automatic highlight code happening at the same time. These two running at the same time is no good.
		 * 
		 * We DO have safety measures in place that should in all cases prevent this from happening, but just in the off-chance
		 * we missed something or if a freak occurrence happens and our safety measures aren't enacted for whatever reason, we
		 * want to at least be prepared for it.
		 */
		try {
			int sentenceSize = words.length;
			for (int i = 0; i < sentenceSize; i++) {
				if (words[i] != null) {
					for (int x = 0; x < removeSize; x++) {
						String wordToRemove = topToRemove.get(x)[0];

						//If the "word to remove" is punctuation and in the form of "Remove ...'s" for example, we want
						//to just extract the "..." for highlighting
						String[] test = wordToRemove.split(" ");
						if (test.length > 2) {
							wordToRemove = test[1].substring(0, test.length-2);
							System.out.println("\"" + wordToRemove + "\"" + ", and \"" + words[i] + "\"");
						}

						if (words[i].equals(wordToRemove)) {
							index.addAll(IndexFinder.findIndicesInSection(main.documentPane.getText(), wordToRemove, start, end));
						}
					}
				}
			}
		} catch (Exception e) {
			Logger.logln(NAME+"Threading issue occurred in addAutoRemoveHighlights, should not have happened.", LogOut.STDERR);
			Logger.logln(e);
		}

		int indexSize = index.size();
		for (int i = 0; i < indexSize; i++) {
			//Since words may or may have not been skipped (don't want to highlight twice, the list might not be completely full, in which
			//case we got to check to make sure each index not null before proceeding
			if (index.get(i) != null) {
				try {
					elementsToRemoveInSentence.add(new HighlightMapper(index.get(i)[0], index.get(i)[1], mainHighlight.addHighlight(index.get(i)[0], index.get(i)[1], painterRemove)));
				} catch (BadLocationException e) {
					Logger.logln(NAME+"Problem occurred while trying to auto highlight word to remove at indices "+index.get(i)[0]+"-"+index.get(i)[i], LogOut.STDERR);
				}
			}
		}
	}
}