package edu.drexel.psal.anonymouth.gooie;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.jgaap.generics.Document;

import edu.drexel.psal.ANONConstants;
import edu.drexel.psal.anonymouth.engine.DocumentMagician;
import edu.drexel.psal.anonymouth.helpers.FileHelper;
import edu.drexel.psal.anonymouth.utils.ConsolidationStation;
import edu.drexel.psal.jstylo.generics.Logger;

/**
 * Handles (nearly) all listeners relating to the Suggestions tab, as well as handles the big update method "placeSuggestions()", which
 * refreshes both the words to add and words to remove table's contents.
 * 
 * @author ? (Most likely Andrew McDonald or Jeff Ulman)
 * @author Marc Barrowclift
 */
public class WordSuggestionsDriver {

	//Constants
	private final static String NAME = "( WordSuggestionsDriver ) - ";
	private final String PUNCTUATION = "?!,.\"`'";
	private final String DICTIONARY = ANONConstants.EXTERNAL_RESOURCE_PACKAGE+"words.txt";

	//Listeners
	private ListSelectionListener elementsToAddListener;
	private ActionListener clearRemoveListener;
	private ActionListener highlightAllRemoveListener;
	
	//Instances and misc. variables
	private GUIMain main;
	private DocumentMagician magician;
	private ArrayList<String[]> topToRemove;
	private ArrayList<String> topToAdd;
	private int addSize = 0, removeSize = 0;
	private HashSet<String> words;
	private boolean filterWordsToAdd;

	/**
	 * Constructor
	 * 
	 * @param main
	 * 		GUIMain instance
	 */
	public WordSuggestionsDriver(GUIMain main) {
		this.main = main;
		
		//We want this to be a Hashset because we want looking up to see if a single word's a dictionary word to be constant time
		words = FileHelper.hashSetFromFile(DICTIONARY);
		setFilterWordsToAdd(PropertiesUtil.getFilterAddSuggestions());
		
		initListeners();
	}

	/**
	 * Sets whether or not to filter out "words" such as:<br>
	 * <ul>
	 * <li>Email addresses</li>
	 * <li>Random dates and numbers</li>
	 * <li>Websites</li>
	 * </ul>
	 * 
	 * @param filter
	 * 		Whether or not to turn the filter on
	 */
	public void setFilterWordsToAdd(boolean filter) {
		filterWordsToAdd = filter;
	}
	
	/**
	 * Gets and returns the words to remove
	 * @return
	 */
	public ArrayList<String[]> getTopToRemove() {
		return topToRemove;
	}

	/**
	 * Gets and returns the words to add size
	 * @return
	 */
	public int getAddSize() {
		return addSize;
	}

	/**
	 * Gets and returns the words to remove size
	 * @return
	 */
	public int getRemoveSize() {
		return removeSize;
	}

	/**
	 * Sets the DocumentMagician instance used to obtain words to remove/add
	 * 
	 * @param magician
	 * 		DOcumentMagician instance
	 */
	public void setMagician(DocumentMagician magician) {
		this.magician = magician;
	}
	
	/**
	 * Updates the lists of words to remove and words to add and then refreshes both
	 * components in GUIMain to reflect the new values
	 */
	public void placeSuggestions() {
		//We must first clear any existing highlights the user has and remove all existing suggestions		
		main.editorDriver.highlighterEngine.removeAllAddHighlights();
		main.editorDriver.highlighterEngine.removeAllRemoveHighlights();

		//If the user had a word highlighted and we're updating the list, we want to keep the word highlighted if it's in the updated list
		String prevSelectedElement = "";
		if (main.elementsToAddPane.getSelectedValue() != null)
			prevSelectedElement = main.elementsToAddPane.getSelectedValue();
		if (main.elementsToRemoveTable.getSelectedRow() != -1)
			prevSelectedElement = (String)main.elementsToRemoveTable.getModel().getValueAt(main.elementsToRemoveTable.getSelectedRow(), 0);

		if (main.elementsToRemoveTable.getRowCount() > 0)
			main.elementsToRemoveTable.removeAllElements();
		if (main.elementsToAdd.getSize() > 0)
			main.elementsToAdd.clear();

		//Adding new suggestions
		List<Document> documents = magician.getDocumentSets().get(1); //all the user's sample documents (written by them)
		documents.add(magician.getDocumentSets().get(2).get(0)); //we also want to count the user's test document

		topToRemove = ConsolidationStation.getPriorityWordsToRemove(documents, .1);
		try {
			if (filterWordsToAdd)
				topToAdd = ConsolidationStation.getPriorityWordsToAdd(ConsolidationStation.otherSampleTaggedDocs, .05);
			else
				topToAdd = ConsolidationStation.getPriorityWordsToAdd(ConsolidationStation.otherSampleTaggedDocs, .06);

			//-----------------HANDLING WORDS TO REMOVE-------------------
			removeSize = topToRemove.size();

			for (int i = 0; i < removeSize; i++) {
				if (!topToRemove.get(i).equals("''") && !topToRemove.get(i).equals("``")) {
					String left, right;

					//The element to remove
					if (PUNCTUATION.contains(topToRemove.get(i)[0].trim()))
						left = "Reduce " + topToRemove.get(i)[0] + "'s";
					else
						left = topToRemove.get(i)[0];

					//The number of occurrences
					if (topToRemove.get(i)[1].equals("0"))
						right = "None";
					else if (topToRemove.get(i)[1].equals("1"))
						right = "1 time";
					else
						right = topToRemove.get(i)[1] + " times";

					main.elementsToRemoveModel.insertRow(i, new String[] {left, right});

					if (topToRemove.get(i)[0].trim().equals(prevSelectedElement)) {
						main.elementsToRemoveTable.setRowSelectionInterval(i, i);
					}
				}		
			}

			//-----------------HANDLING WORDS TO ADD-------------------
			addSize = topToAdd.size();
			int tableIndex = 0;
			ArrayList<String> good = new ArrayList<String>();
			ArrayList<String> bad = new ArrayList<String>();

			for (int i = 0; i < addSize; i++) {
				boolean add = false;
				String curWord = topToAdd.get(i);
				
				if (filterWordsToAdd) {
					if (words.contains(curWord)) {
						//If the word's a word, don't even question it, just add it.
						add = true;
					} else if (curWord.matches("^[a-z-'A-Z\\.]*$")) { //^[A-Z][a-zA-Z]*$
						//If the word's sane, basically like We'd, need-based, but may not exactly show up in a dictionary, add it
						//This also accounts for made up words.
						add = true;
					} else if (curWord.equals("...")) {
						//We don't want to leave this out as a suggestion if possible
						add = true;
					}
				} else {
					add = true;
				}
				
				if (add) {
					good.add(topToAdd.get(i));
					main.elementsToAdd.add(tableIndex, topToAdd.get(i));
					tableIndex++;

					if (topToAdd.get(i).equals(prevSelectedElement)) {
						main.elementsToAddPane.setSelectedValue(topToAdd.get(i), true);
					}
				} else {
					bad.add(topToAdd.get(i));
				}
			}
		} catch (Exception e) {
			Logger.logln(NAME+"An error occured while obtaining and placing Word Suggestions");
			Logger.logln(e);
		}
	}

	/**
	 * Initializes (nearly) all listeners needed for the suggestions tab
	 */
	protected void initListeners() {
		elementsToAddListener = new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					Logger.logln(NAME+"Elements to add value changed");

					if (main.elementsToAddPane.getSelectedIndex() != -1) {
						main.editorDriver.highlighterEngine.removeAllAddHighlights();

						if (main.elementsToAddPane.getSelectedIndex() == -1)
							return;

						main.editorDriver.highlighterEngine.addAllAddHighlights(main.elementsToAddPane.getSelectedValue());
					}
				}
			}
		};
		main.elementsToAddPane.addListSelectionListener(elementsToAddListener);

		clearRemoveListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {	
				main.editorDriver.highlighterEngine.removeAllRemoveHighlights();
				main.elementsToRemoveTable.clearSelection();
			}
		};
		main.clearRemoveHighlights.addActionListener(clearRemoveListener);
		
		highlightAllRemoveListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				main.editorDriver.highlighterEngine.removeAllRemoveHighlights();
				for (int i = 0; i < removeSize; i++) {
					main.editorDriver.highlighterEngine.addAllRemoveHighlights(topToRemove.get(i)[0]);
				}
				main.elementsToRemoveTable.clearSelection();
			}
		};
		main.highlightAllRemoveHighlights.addActionListener(highlightAllRemoveListener);
	}
}