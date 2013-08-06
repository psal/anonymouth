package edu.drexel.psal.anonymouth.utils;

import java.util.ArrayList;

import edu.drexel.psal.ANONConstants;
import edu.drexel.psal.jstylo.generics.Logger;
import edu.drexel.psal.anonymouth.helpers.FileHelper;
import edu.drexel.psal.anonymouth.utils.Trie;

/**
 * "The WINFY PRUNKILMONGER from the GLIDGEMENT MOMINKLED and BRANGIFIED
 * all his LEVENSERS VEDEROUSLY"<br><br>
 * 
 * In the above sentence, even though the capitalized words make no sense,
 * we can still derive grammatical meaning from them (WINFY is most likely
 * an adjective, PRUNKILMONGER, GLIDGEMENT, and LEVENSERS most likely nouns,
 * etc.).<br><br>
 * 
 * In short, Function words are words that have more ambiguous meaning
 * (like another, below, there, this, etc.), but do express some grammatical
 * relationship. For more examples of FunctionWords, see 
 * "koppel_function_words.txt" in the jsan_resources directory.<br><br>
 * 
 * This class keeps track of the function words from that file and initializes
 * a Trie of the function words for searching. At this time this doesn't seem
 * to be implemented anywhere.<br><br>
 * 
 * TO USE:<br>
 * 		1) Initialize<br>
 * 		2) Call run()<br>
 * 
 * @author Marc Barrowclift
 * @author Joe Muoio (old version)
 */
public class FunctionWords implements Runnable {

	//Constants
	private final String NAME = "( FuncitonWords ) - ";
	private final int NUM_OF_WORDS=486; //The number of words we will add

	//Variables
	private ArrayList<String> functionWordList; //The initial list 
	private String[] functionWordArray;
	private Trie trie;

	/**
	 * Main run method for the class. Should be called when you want to initialize and
	 * fill the functionWordList, functionWordArray, and trie objects. Should, to my knowledge
	 * anyway, be only called once.
	 */
	@Override
	public void run() {
		Logger.logln(NAME+"Beginning to initialize function words...");

		functionWordList = FileHelper.ArrayListFromFile(ANONConstants.KOPPEL_FUNCTION_WORDS, 1000);
		functionWordArray = new String[NUM_OF_WORDS];

		//We only want to use a predetermined number of words from the ArrayList we obtained
		for (int i = 0; i < NUM_OF_WORDS; i++) {
			functionWordArray[i] = functionWordList.get(i);
		}

		trie = new Trie();
		trie.addWords(functionWordArray);

		Logger.logln(NAME+"Finished initializing function words");
	}

	/**
	 * Retrieves the function word at the given index.
	 * 
	 * @param index
	 * 		The index you want to get the function word at
	 * @return
	 * 		The function word at the given index. Returns null if not found or
	 * 		if index is not acceptable.
	 */
	protected String getWordAt(int index) {
		String word = null;

		try {
			word = functionWordArray[index];
		} catch (Exception e) { //Catching in case the index doesn't exist
			Logger.logln(NAME+"Tried to get function word at an unacceptable index: "
				+ index + " (functionWordArray.length = " + functionWordArray.length + ")");
		}

		return word;
	}

	/**
	 * Searches the function word trie for a given word.
	 * 
	 * @return
	 * 		True if the word is found, false if it is not
	 */
	protected boolean isWordInTrie(String word) {
		return trie.find(word);
	}

	/**
	 * ====================MAIN METHOD TEST====================
	 * To be used only for testing purposes. Tests all current methods
	 * in FunctionWords.java and displays results.
	 */
	public static void main(String[] args) {
		System.out.println("Testing initializing and running class");
		FunctionWords functionWords = new FunctionWords();
		functionWords.run();
		System.out.println("   >>> Success");

		System.out.println("Testing words added to Trie succesfully and isWordInTrie function");
		System.out.println("(All results should be true)");
		String wordToFind;
		boolean success = true;
		int size = functionWords.functionWordList.size();
		long endTime;
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < size; i++) {
			wordToFind = functionWords.getWordAt(i);
			success = functionWords.isWordInTrie(wordToFind);
			System.out.println("   >>> " + success);
			if (!success)
				break;
		}
		endTime = System.currentTimeMillis();
		System.out.println("Time to search Trie: " + (endTime - startTime));
		System.out.println("Was successful? " + success);
	}
}