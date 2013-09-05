package edu.drexel.psal.anonymouth.utils;

import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import edu.drexel.psal.ANONConstants;
import edu.drexel.psal.anonymouth.engine.Attribute;
import edu.drexel.psal.anonymouth.engine.DataAnalyzer;
import edu.drexel.psal.anonymouth.gooie.GUIMain;
import edu.drexel.psal.jstylo.generics.Logger;
import edu.drexel.psal.jstylo.generics.Logger.LogOut;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.TreebankLanguagePack;

enum TENSE {PAST,PRESENT,FUTURE};
enum POV {FIRST_PERSON,SECOND_PERSON,THIRD_PERSON};
enum CONJ {SIMPLE,PROGRESSIVE,PERFECT,PERFECT_PROGRESSIVE};

/**
 * Holds all instances of our TaggedSentences, which then makes this our
 * "TaggedDocument". Provides means to create whole backend tagged documents
 * based plain string text, methods to manipulate and access TaggedSentences,
 * etc.<br><br>
 *
 * Since the EOSTracker is specific to each individual TaggedDocument instance
 * (for example, a version on the undo stack may have EOS characters in a
 * different location than the one in the current TaggedDocument instance
 * does), we keep our EOSTracker instances here so they are backed up in
 * addition to the other variables for undo/redo.
 * 
 * @author Andrew W.E. McDonald
 * @author Marc Barrowclift
 * @author Joe Muoio
 */
public class TaggedDocument implements Serializable {
	
	private static final long serialVersionUID = 2258415935896292619L;
	private final String NAME = "( "+this.getClass().getSimpleName()+" ) - ";
	private final String DELETE_STRING = "///";
	
	/**
	 * Our "document" of TaggedSentences
	 */
	protected ArrayList<TaggedSentence> taggedSentences;
	
	public EOSTracker eosTracker;
	private GUIMain main;
	
	protected String documentTitle = "None";
	protected String documentAuthor = "None";

	protected transient TreebankLanguagePack tlp = new PennTreebankLanguagePack(); 
	protected transient List<? extends HasWord> sentenceTokenized;
	protected transient Tokenizer<? extends HasWord> toke;
	
	/**
	 * The way we have taggedSentences structured, while easy in most cases,
	 * breaks whenever the user tries to type after the last sentence. That's
	 * why we need to actively detect when they are doing so and create an
	 * empty tagged sentence for their new writing to go into every caret
	 * event that calls for it.<br><br>
	 * 
	 * We create this blank tagged sentence whenever they are typing at the
	 * end of a document OR when they click to the end of document from
	 * somewhere else and there isn't already a blank sentence in place ready
	 * to accept the new text. That's where this boolean comes in, it keeps
	 * track for us whether or not it's necessary to create a new extra tagged
	 * sentence.
	 */
	public boolean endSentenceExists;

	/**
	 * Instead of having to call size() on taggedSentences every time we want
	 * to know the size, we can keep track of it ourselves and keep things
	 * constant time.
	 */
	public int numOfSentences = 0;

	/**
	 * Instead of having to call length() on all taggedSentences every time we
	 * want to know the length of the document, we can keep track of it
	 * ourselves and keep things constant time.
	 */
	public int length = 0;
	
	/**
	 * Greater than -1 when we are supposed to be keeping an eye out for a
	 * specific EOS character that we are as of yet unsure whether or not it's
	 * an EOS or just an abbreviation, ellipses, etc. Should be equal to the
	 * index of the EOS character we're watching so we can use it to set ignore
	 * to false if necessary.
	 */
	public int watchForEOS = -1;
	
	/**
	 * Greater than -1 when we are supposed to be keeping an eye out for a
	 * specific EOS character at the very end of the document that we are as
	 * of yet unsure whether or not it's an EOS or just an abbreviation, ellispes,
	 * etc. Should be equal to the index of the EOS character (length of the
	 * document) we're watching so we can use it to set ignore to false if necessary.
	 */
	public int watchForLastSentenceEOS = -1;
	
	public boolean userDeletedSentence = false;

	//=======================================================================
	//*						CONSTRUCTORS / INITIALIZES						*	
	//=======================================================================
	

	/**
	 * Constructor, creates a blank taggedDocument.
	 */
	public TaggedDocument(GUIMain main) {
		this.main = main;
		eosTracker = new EOSTracker();
		taggedSentences = new ArrayList<TaggedSentence>(ANONConstants.EXPECTED_NUM_OF_SENTENCES);
		endSentenceExists = false;
	}
	
	/**
	 * Constructor, accepts an untagged string (a whole document), and makes
	 * sentence tokens out of it to create a full Tagged Document
	 * 
	 * @param main
	 * 		  GUIMain instance
	 * @param untaggedDocument
	 *        The String of the document you want to tag.
	 * @param waitToTag
	 * 		  Whether or not to immediately makeandTagSentences or to wait until directly called later
	 */
	public TaggedDocument(GUIMain main, String untaggedDocument, boolean waitToTag) {
		this.main = main;
		eosTracker = new EOSTracker();
		taggedSentences = new ArrayList<TaggedSentence>(ANONConstants.EXPECTED_NUM_OF_SENTENCES);
		setDocumentLength(untaggedDocument);
		endSentenceExists = false;
		
		if (waitToTag) {
			initEOSTracker(untaggedDocument);
		} else {
			makeAndTagSentences(untaggedDocument, true);
		}
	}

	/**
	 * Constructor, accepts an untagged string (a whole document), and makes
	 * sentence tokens out of it to create a full Tagged Document.
	 * 
	 * @param untaggedDocument
	 *        The String of the document you want to tag.
	 * @param docTitle
	 *        The title of the document.
	 * @param author
	 *        The author of the document.
	 */
	public TaggedDocument(GUIMain main, String untaggedDocument, String docTitle, String author){
		this.main = main;
		this.documentTitle = docTitle;
		this.documentAuthor = author;
		eosTracker = new EOSTracker();

		taggedSentences = new ArrayList<TaggedSentence>(ANONConstants.EXPECTED_NUM_OF_SENTENCES);
		setDocumentLength(untaggedDocument);
		endSentenceExists = false;
		makeAndTagSentences(untaggedDocument, true);
	}

	/**
	 * Constructor, accepts another TaggedDocument instance and initiates a deep copy of it
	 * (so we're not just creating another pointer like with == but creating an actual copy)
	 * 
	 * @param  td
	 *         The TaggedDocument you want to initiate a deep copy for
	 */
	public TaggedDocument(TaggedDocument td) {
		this.main = td.main;
		int numTaggedSents = td.taggedSentences.size();
		numOfSentences = numTaggedSents;
		
		taggedSentences = new ArrayList<TaggedSentence>(ANONConstants.EXPECTED_NUM_OF_SENTENCES);

		//Copy all TaggedSentences
		for (int i = 0; i < numTaggedSents; i++)
			taggedSentences.add(new TaggedSentence(td.taggedSentences.get(i)));
			
		//Copy document author and title (Strings are immutable)
		documentAuthor = td.documentAuthor;
		documentTitle = td.documentTitle;
		
		//Then the total number of sentences (could probably chuck ths)
		numOfSentences = td.numOfSentences;
		
		//Finally, copy the EOSTracker
		eosTracker = new EOSTracker(td.eosTracker);
		
		setDocumentLength(td.getUntaggedDocument());
		endSentenceExists = td.endSentenceExists;
	}

	/**
	 * Initializes the EOSTracker by adding all EOSes currently present in
	 * this instance.
	 */
	private void initEOSTracker() {
		char[] docToAnonymize = getUntaggedDocument().toCharArray();
		int numChars = docToAnonymize.length;

		for (int i = 0; i < numChars; i++) {
			if (eosTracker.isEOS(docToAnonymize[i])) {
				eosTracker.addEOS(docToAnonymize[i], i, false);
			}
		}
	}
	
	private void initEOSTracker(String document) {
		char[] docToAnonymize = document.toCharArray();
		int numChars = docToAnonymize.length;
		
		for (int i = 0; i < numChars; i++) {
			if (eosTracker.isEOS(docToAnonymize[i])) {
				eosTracker.addEOS(docToAnonymize[i], i, false);
			}
		}
	}

	//=======================================================================
	//*							BOOKKEEPING									*	
	//=======================================================================
	
	/**
	 * Increases or decreases the length of this document based on the given
	 * amount.
	 * 
	 * @param amount
	 *        The amount to change the length by (negative for
	 *        decrease, positive for increase)
	 */
	public void incrementDocumentLength(int amount) {
		length += amount;
	}

	/**
	 * Sets the initial length for this TaggedDocument, should be called only
	 * during initialization.
	 * 
	 * @param document
	 *        The string of the document you are tagging.
	 */
	public void setDocumentLength(String document) {
		length = document.length();
	}

	/**
	 * Clears all saved translations attributes to every TaggedSentence in
	 * this instance. This is to be used for undo/redo, as it's been pretty
	 * much proven we can't back that shit up without it grinding the editor
	 * to a halt.<br><br>
	 *
	 * TODO: Possiblity a way to thread it so this doesn't happen?
	 */
	public void clearAllTranslations() {
		for (int i = 0; i < numOfSentences; i++) {
			taggedSentences.get(i).getTranslations().clear();
		}
	}
	
	/**
	 * Consolidates features for an ArrayList of TaggedSentences (does both
     * word level and sentence level features)
     * 
	 * @param alts
	 */
	public void consolidateFeatures(ArrayList<TaggedSentence> alts){
		for (TaggedSentence ts:alts) {
			ConsolidationStation.featurePacker(ts);
		}
	}
		
	/**
	 * consolidates features for a single TaggedSentence object
	 * 
	 * @param ts
	 */
	public void consolidateFeatures(TaggedSentence ts){
		ConsolidationStation.featurePacker(ts);
	}
	
	//=======================================================================
	//*				MAIN TAGGEDSENTENCE MANIPULATINO METHODS				*	
	//=======================================================================
	
	//================ CREATE TAGGED SENTENCES ================
	/**
	 * Takes a String of sentences (can be an entire document), breaks it up
	 * into individual sentences (sentence tokens), breaks those up into
	 * tokens, and then tags them (via MaxentTagger). Each tagged sentence is
	 * saved into a TaggedSentence object, along with its untagged
	 * counterpart.
	 * 
	 * @param untagged
	 *        String containing sentences to tag
	 * @param appendTaggedSentencesToGlobalArrayList
	 *        if true, appends the TaggedSentence objects to the TaggedDocument's
	 *        arraylist of TaggedSentences
	 *        
	 * @return
	 * 		An ArrayList of the completed TaggedSentences
	 */
	public ArrayList<TaggedSentence> makeAndTagSentences(String untagged, boolean appendTaggedSentencesToGlobalArrayList) {
		if (length == 0 ) {
			setDocumentLength(untagged);
		}

		ArrayList<String> untaggedSents = main.editorDriver.sentenceMaker.makeSentences(untagged);
		ArrayList<TaggedSentence> taggedSentences = new ArrayList<TaggedSentence>(untaggedSents.size());
		Iterator<String> strRayIter = untaggedSents.iterator();
		String tempSent;
		
		if (untagged.matches("\\s\\s*")) {
			TaggedSentence taggedSentence = new TaggedSentence(untagged);
			taggedSentences.add(taggedSentence);
		} else if (untagged.matches("")) {
			TaggedSentence taggedSentence = new TaggedSentence(untagged);
			taggedSentences.add(taggedSentence);
		} else {
			while (strRayIter.hasNext()) {
				tempSent = strRayIter.next();
				
				TaggedSentence taggedSentence = new TaggedSentence(tempSent);
				toke = tlp.getTokenizerFactory().getTokenizer(new StringReader(tempSent));
				sentenceTokenized = toke.tokenize();
				taggedSentence.setTaggedSentence(Tagger.mt.tagSentence(sentenceTokenized));
				consolidateFeatures(taggedSentence);
				taggedSentence.untaggedWithEOSSubs = tempSent;
				
				// todo: put stuff here
				taggedSentences.add(taggedSentence);
				
			}
		}
		
		if (appendTaggedSentencesToGlobalArrayList == true) {
			int i = 0;
			int len = taggedSentences.size();
			for (i = 0; i < len; i++) {
				numOfSentences++;
				this.taggedSentences.add(taggedSentences.get(i)); 
			}
			
			if (eosTracker.size == 0)
				initEOSTracker();
		}
		return taggedSentences;
	}

	/**
	 * Adds sentToAdd at placeToAdd in this TaggedDocument
	 * 
	 * @param sentToAdd
	 *        The TaggedSentence you want to add to the TaggedDocument
	 * @param placeToAdd
	 *        The index in which you want to add it (0, 1, 2, etc.)
	 */
	public void addTaggedSentence(TaggedSentence sentToAdd, int placeToAdd) {
		taggedSentences.add(placeToAdd,sentToAdd);
	}

	/**
	 * Creates a new sentence at the very end of the document with the
	 * given text (usually whitespace or most likely "")
	 * 
	 * @param text
	 *        The text you want the new TaggedSentence to be made with
	 *        (most likey "" or whitespace)
	 */
	public void makeNewEndSentence(String text) {
		TaggedSentence newSentence = new TaggedSentence(text);
		taggedSentences.add(newSentence);
		numOfSentences++;
	}
	
	//================ CONCAT TAGGED SENTENCES ================

	/**
	 * Accepts a variable number of TaggedSentences and returns a single
	 * TaggedSentence, preserving all original Word objects.<br><br>
	 * 
	 * Note that the sentences will be concatenated together in the order that
	 * they are passed into the method.
	 * 
	 * @param taggedSentences
	 *        A variable number of TaggedSentences
	 *        
	 * @return
	 * 		A single tagged sentences with the properties of all the sentences in the list.
	 */
	public TaggedSentence concatSentences(TaggedSentence ... taggedSentences) {
		TaggedSentence toReturn = new TaggedSentence(taggedSentences[0]);
		int numSents = taggedSentences.length;
		
		for (int i = 1; i < numSents; i++) {
			toReturn.wordsInSentence.addAll(taggedSentences[i].wordsInSentence);
			toReturn.untagged += taggedSentences[i].untagged;
		}

		return toReturn;
	}
	
	/**
	 * Accepts an ArrayList of TaggedSentences and returns a single
	 * TaggedSentence, preserving all original Word objects.<br><br>
	 * 
	 * Note that the sentences will be concatenated together in the order that
	 * they are passed into the method.
	 * 
	 * @param taggedSentences
	 *        An ArrayList of TaggedSentences
	 *        
	 * @return
	 * 		A single tagged sentences with the properties of all the sentences in the list.
	 */
	public TaggedSentence concatSentences(ArrayList<TaggedSentence> taggedSentences) {
		TaggedSentence toReturn =new TaggedSentence(taggedSentences.get(0));
		TaggedSentence thisTaggedSent;
		int size = taggedSentences.size();

		for (int i = 1; i < size; i++) {
			thisTaggedSent = taggedSentences.get(i);
			toReturn.wordsInSentence.addAll(thisTaggedSent.wordsInSentence);
			toReturn.untagged += thisTaggedSent.untagged;
			toReturn.sentenceLevelFeaturesFound.merge(thisTaggedSent.sentenceLevelFeaturesFound);
		}

		return toReturn;
	}	
	
	/**
	 * Merges the TaggedSentences specified by the indices in
	 * 'taggedSentenceIndicesToConcat' into one TaggedSentence.<br><br>
	 * 
	 * Note that the sentences will be concatenated together in the order that
	 * they are passed into the method.
	 * 
	 * @param taggedSentenceIndicesToConcat
	 * 
	 * @return
	 * 		The TaggedSentence that resulted from the merging
	 */
	public TaggedSentence concatSentences(int[] taggedSentenceIndicesToConcat) {
		TaggedSentence toReturn =new TaggedSentence(taggedSentences.get(taggedSentenceIndicesToConcat[0]));
		TaggedSentence thisTaggedSent;

		for (int i = 1; i < numOfSentences; i++) {
			thisTaggedSent = taggedSentences.get(taggedSentenceIndicesToConcat[i]);
			toReturn.wordsInSentence.addAll(thisTaggedSent.wordsInSentence);
			toReturn.untagged += thisTaggedSent.untagged;
			toReturn.sentenceLevelFeaturesFound.merge(thisTaggedSent.sentenceLevelFeaturesFound);
		}

		return toReturn;
	}	
	
	/**
	 * Concatenates the two TaggedSentences (in order), removes the second
	 * TaggedSentence, and replaces the first with the concatenated
	 * TaggedSentence Takes care of bookkeeping.
	 * 
	 * @param taggedSentenceOne
	 *        The first TaggedSentence
	 * @param tsOneIndex
	 *        The first TaggedSentence's index in the TaggedDocument
	 * @param taggedSentenceTwo
	 *        The second TaggedSentence
	 * @param tsTwoIndex
	 *        The second TaggedSentence's index in the TaggedDocument
	 *        
	 * @return
	 * 		The completed and combined TaggedSentence
	 */
	public TaggedSentence concatRemoveAndReplace(TaggedSentence taggedSentenceOne, int tsOneIndex, TaggedSentence taggedSentenceTwo, int tsTwoIndex) {
		TaggedSentence replaceWith = concatSentences(taggedSentenceOne, taggedSentenceTwo);
		removeAndReplace(tsTwoIndex, DELETE_STRING); //Delete the second sentence
		Logger.logln(NAME+"*** Replacing: \""+taggedSentenceOne.getUntagged(false)+"\"\n" + NAME + "*** With: \""+replaceWith.getUntagged(false) + "\"");
		return removeAndReplace(tsOneIndex,replaceWith);
	}

	//================ REMOVE TAGGED SENTENCES ================

	/**
	 * removes TaggedSentence at indexToRemove from this TaggedDocument. Does
	 * NOT take care of any bookkeeping issues -- should only be called by
	 * methods that do (removeAndReplace)
	 * 
	 * @param indexToRemove
	 *        The index where you want to remove a taggedSentence from
	 *        
	 * @return
	 * 		The removed TaggedSentence. Returns a null TaggedSentence if
	 * 		no TaggedSentence existed at the given index or if the index
	 * 		was not within acceptable bounds (< 0 or >= numOfSentences)
	 */
	private TaggedSentence removeTaggedSentence(int indexToRemove) {
		TaggedSentence returnSentence = null;

		try {
			returnSentence = taggedSentences.remove(indexToRemove);
		} catch (Exception e) {
			Logger.logln(NAME+"Attemp to access TaggedSentence in an unacceptable index = " + indexToRemove, LogOut.STDERR);
		}

		return returnSentence;
	}
	
	/**
	 * Removes all tagged sentences at every given index.
	 * 
	 * @param indicesToRemove
	 *        An integer array of the indices you wish to
	 *        remove (0, 1, 2, etc.)
	 */
	public void removeTaggedSentences(int[] indicesToRemove) {
		int numToRemove = indicesToRemove.length;
		
		for (int i = 0; i < numToRemove; i++)
			removeAndReplace(indicesToRemove[i], DELETE_STRING);
	}

	/**
	 * Removes the existing TaggedSentence at the given index and replaces it
	 * with a new TaggedSentence made from the given text.
	 * 
	 * @param sentsToAdd
	 *        A String representing the sentence(s) from the editBox
	 */
	public void removeAndReplace(int sentNumber, String sentsToAdd) {//, int indexToRemove, int placeToAdd){
		TaggedSentence toReplace = taggedSentences.get(sentNumber);
		Logger.logln(NAME+"Removing: \""+toReplace.getUntagged(false) + "\"");
		Logger.logln(NAME+"Adding: \""+sentsToAdd + "\"");
		
		if (sentsToAdd.equals(DELETE_STRING)) {//checks to see if the user deleted the current sentence
			//CALL COMPARE
			TaggedSentence wasReplaced = removeTaggedSentence(sentNumber);

			Logger.logln(NAME+"User deleted a sentence.");
			updateReferences(toReplace,new TaggedSentence(""));//all features must be deleted
			userDeletedSentence = true;
			numOfSentences--;
			
			wasReplaced.delete();
			wasReplaced = null;
			return;
		}
		
		ArrayList<TaggedSentence> taggedSentsToAdd = makeAndTagSentences(sentsToAdd,false);

		TaggedSentence wasReplaced = removeTaggedSentence(sentNumber);
		
		numOfSentences--;
		//call compare
		int len = taggedSentsToAdd.size();
		for (int i = 0; i < len; i++) {
			addTaggedSentence(taggedSentsToAdd.get(i),sentNumber);
			sentNumber++;
			numOfSentences++;
		}
		TaggedSentence concatted = concatSentences(taggedSentsToAdd);

		updateReferences(toReplace,concatted);
		
		wasReplaced.delete();
		wasReplaced = null;
	}	
	
	/**
	 * Removes multiple sentences and replaces them with a single
	 * TaggedSentence. To be used with the right-click menu item "combine
	 * sentences".
	 * 
	 * @param sentsToRemove
	 *        An ArrayList of TaggedSentences to remove
	 * @param sentToAdd
	 *        The TaggedSentence to want to replace them all with.
	 *        
	 * @return
	 *        The index (0-based) of the first sentence removed.
	 */
	public int removeMultipleAndReplace(ArrayList<TaggedSentence> sentsToRemove, TaggedSentence sentToAdd) {
		int size = sentsToRemove.size();
		int startingSentence = 0;
		
		for (int i = 0; i < size; i++) {
			if (i == 0) {
				startingSentence = taggedSentences.indexOf(sentsToRemove.get(i));
			}
			taggedSentences.remove(sentsToRemove.get(i));
			numOfSentences--;
		}
		
		addTaggedSentence(sentToAdd, startingSentence);
		numOfSentences++;
		
		//TODO: check if this okay to do
		for (int j = 0; j < size; j++) {
			updateReferences(sentsToRemove.get(j), sentToAdd);
		}
		
		return startingSentence;
	}
	
	/**
	 * Removes the TaggedSentence at 'sentNumber', and switches in 'toAdd' in
     * its place. Takes care of all bookkeeping issues.
	 * 
	 * @param sentNumber
	 *        The number of the TaggedSentence to remove and replace
	 * @param toAdd
	 *        The TaggedSentence to want to replace the removed TaggedSentence with
	 *        
	 * @return
	 */
	public TaggedSentence removeAndReplace(int sentNumber, TaggedSentence toAdd) {
		TaggedSentence toReplace = taggedSentences.get(sentNumber);
		Logger.logln(NAME+"Removing: "+toReplace.toString());
		Logger.logln(NAME+"Adding: "+toAdd.getUntagged(false));

		if (toAdd.getUntagged(false).matches("^\\s*$")) {//checks to see if the user deleted the current sentence
			//CALL COMPARE
			TaggedSentence wasReplaced = removeTaggedSentence(sentNumber);
			Logger.logln(NAME+"User deleted a sentence.");
			updateReferences(toReplace,new TaggedSentence(""));//all features must be deleted
			numOfSentences--;
			return wasReplaced;
		}

		// no need to subtract one from numOfSentences when removing a sentence, because we are putting a new sentence in its place immediatly 
		TaggedSentence wasReplaced = removeTaggedSentence(sentNumber);
		addTaggedSentence(toAdd,sentNumber);
		
		Logger.logln(NAME+"TaggedSent to add: "+toAdd.toString());
		Logger.logln(NAME+"TaggedSent to remove: "+toReplace.toString());
		updateReferences(toReplace,toAdd);
		return wasReplaced;
	}

	//=======================================================================
	//*							ASSORTED									*	
	//=======================================================================
	
	/**
	 * Checks all sentences in the tagged document and returns whether or not they are all translated.
	 */
	public boolean isTranslated() {
		boolean result = true;
		
		for (int i = 0; i < numOfSentences; i++) {
			if (!taggedSentences.get(i).isTranslated()) {
				result = false;
				break;
			}
		}
		
		return result;
	}

	/**
	 * Our custom toString() method that allows us to print out a nice, formatted
	 * version of our tagged document when printed using standard output.
	 * 
	 * @return
	 * 		The formated string to print
	 */
	@Override
	public String toString() {
		String toReturn = "Document Title: "+documentTitle+" Author: "+documentAuthor+"\n";
		
		for (int i = 0; i < numOfSentences; i++){
			toReturn += taggedSentences.get(i).toString()+"\n";
		}

		return toReturn;
	}

	/**
	 * Updates the referenced Attributes 'toModifyValue's (present value) with
	 * the amount that must be added/subtracted from each respective value
	 * 
	 * @param oldSentence
	 *        The pre-editing version of the sentence(s)
	 * @param newSentence
	 *        The post-editing version of the sentence(s)
	 */
	private void updateReferences(TaggedSentence oldSentence, TaggedSentence newSentence){
		//Logger.logln(NAME+"Old Sentence: "+oldSentence.toString()+"\nNew Sentence: "+newSentence.toString());
		SparseReferences updatedValues = newSentence.getOldToNewDeltas(oldSentence);
		//Logger.logln(NAME+updatedValues.toString());
		for(Reference ref:updatedValues.references){
			//Logger.logln(NAME+"Attribute: "+DataAnalyzer.topAttributes[ref.index].getFullName()+" pre-update value: "+DataAnalyzer.topAttributes[ref.index].getToModifyValue());
			if(DataAnalyzer.topAttributes[ref.index].getFullName().contains("Percentage")){
				//then it is a percentage.
				Logger.logln(NAME+"Attribute: "+DataAnalyzer.topAttributes[ref.index].getFullName()+"Is a percentage! ERROR!",Logger.LogOut.STDERR);
			}
			else if(DataAnalyzer.topAttributes[ref.index].getFullName().contains("Average")){
				//then it is an average
				Logger.logln(NAME+"Attribute: "+DataAnalyzer.topAttributes[ref.index].getFullName()+"Is an average! ERROR!",Logger.LogOut.STDERR);
			}
			else{
				DataAnalyzer.topAttributes[ref.index].setToModifyValue((DataAnalyzer.topAttributes[ref.index].getToModifyValue() + ref.value));
				//Logger.logln(NAME+"Updated attribute: "+DataAnalyzer.topAttributes[ref.index].getFullName());
			}
				
			//Logger.logln(NAME+"Attribute: "+DataAnalyzer.topAttributes[ref.index].getFullName()+" post-update value: "+DataAnalyzer.topAttributes[ref.index].getToModifyValue());
		}
	}

	//=======================================================================
	//*							GET METHODS									*	
	//=======================================================================
	
	/**
	 * Returns the number of words in the tagged document.
	 * 
	 * @return
	 * 		The number of words (>= 0)
	 */
	public int getWordCount(){
		int wordCount = 0;
		for (TaggedSentence ts:taggedSentences) {
			wordCount += ts.size();
		}

		return wordCount;
	}
	
	/**
	 * Returns all words in the tagged document
	 * 
	 * @return
	 * 		An ArrayList of Word objects
	 */
	public ArrayList<Word> getWords() {
		int numWords = getWordCount();
		ArrayList<Word> theWords = new ArrayList<Word>(numWords);
		for (TaggedSentence ts: taggedSentences) {
			theWords.addAll(ts.wordsInSentence);
		}

		return theWords;
	}
	
	/**
	 * Returns all the words in the given tagged sentence
	 * 
	 * @param sentence
	 *        The TaggedSentence instance you want to obtain words
	 *        from
	 *        
	 * @return
	 * 		An array of strings representing all the words in the
	 * 		given sentence.
	 */
	public String[] getWordsInSentence(TaggedSentence sentence) {
		ArrayList<Word> theWords = sentence.getWordsInSentence();
		int size = theWords.size();
		
		String[] words = new String[size];
		
		for (int i = 0; i < size; i++) {
			words[i] = theWords.get(i).word;
		}
		return words;
	}
	
	/**
	 * Returns all words for each unique word in the given tagged sentence (no
	 * duplicates)
	 * 
	 * @param sentence
	 *        The TaggedSentence instance you want all unique words from the
	 *        TaggedSentence
	 *        
	 * @return
	 * 		An array of strings representing all unique words from the
	 * 		TaggedSentence
	 */
	public String[] getWordsInSentenceNoDups(TaggedSentence sentence) {
		ArrayList<Word> unfiltered = sentence.getWordsInSentence();
		int size = unfiltered.size();
		HashSet<String> wordList = new HashSet<String>(size);
		String curWord;
		String[] words = new String[size];
		
		for (int i = 0; i < size; i++) {
			curWord = unfiltered.get(i).word;
			
			if (wordList.contains(curWord)) {
				continue;
			}
			
			words[i] = unfiltered.get(i).word;
			wordList.add(words[i]);
		}
		
		return words;
	}

	/**
	 * Returns this TaggedDocument (The ArrayList of TaggedSentences)
	 * 	
	 * @return
	 * 		An ArrayList of TaggedSentences
	 */
	public ArrayList<TaggedSentence> getTaggedDocument() {
		return taggedSentences;
	}

	/**
	 * Returns this document (The ArrayList of untagged strings)
	 * 
	 * @return
	 * 		An ArrayList of strings
	 */
	public ArrayList<String> getUntaggedSentences() {
		ArrayList<String> sentences = new ArrayList<String>();
		for (int i=0;i<taggedSentences.size();i++)
			sentences.add(taggedSentences.get(i).getUntagged(false));

		return sentences;
	}

	/**
	 * Returns this document as a single untagged String
	 * 
	 * @return
	 * 		The complete String of the document (untagged)
	 */
	public String getUntaggedDocument() {
		String str = "";
		for (int i = 0; i < numOfSentences; i++){
			str += taggedSentences.get(i).getUntagged(false);
		}

		return str;	
	}

	/**
	 * Returns the lengths of each sentence. (0 for sentence 1 length, 1 for
	 * sentence 2 length, etc.)
	 *
	 * @return 
	 * 		An integer array representing the lengths for each sentence
	 */
	public int[] getSentenceLengths() {
		int numSents = taggedSentences.size();
		int[] lengthsToReturn = new int[numSents];
		for (int i = 0; i < numSents; i++) {
			lengthsToReturn[i] = taggedSentences.get(i).getLength();
		}

		return lengthsToReturn;
	}
	
	/**
	 * Returns TaggedSentence number at this index
	 * 
	 * @param number
	 *        The index for the TaggedSentence you want (0, 1, 2, etc.)
	 *        
	 * @return
	 * 		The TaggedSentence at that index. Returns a null TaggedSentence
	 * 		if none exist at the given index (or if it was an unacceptable
	 * 		position < 0 or >= size)
	 */
	public TaggedSentence getSentenceNumber(int number) {
		TaggedSentence returnSentence = null;

		try {
			returnSentence = taggedSentences.get(number);
		} catch (Exception e) {
			Logger.logln(NAME+"Attemp to access TaggedSentence in an unacceptable index = " + number, LogOut.STDERR);
		}

		return returnSentence;
	}
	
	/**
	 * Essentially the same thing as getSentenceNumAt(), except instead of
	 * accepting a sentence number and finding the taggedSentence that corresponds
	 * to that number it accepts an index (caret position), and finds the
	 * taggedSentence that corresponds to that index.
	 * 
	 * @param index
	 * 		The position in the document text.
	 * 
	 * @return
	 * 		The TaggedSentence found at the index. If none exists, null is returned.
	 */
	public TaggedSentence getTaggedSentenceAtIndex(int index) {
		int newIndex = 0;
		int pastIndex = 0;
		int length = 0;
		TaggedSentence returnValue = null;
		
		for (int i = 0; i < numOfSentences; i++) {
			length = taggedSentences.get(i).getUntagged(false).length();
			newIndex = length + pastIndex;
			
			if (index >= pastIndex && index <= newIndex) {
				returnValue = taggedSentences.get(i);
				break;
			} else {
				pastIndex = newIndex;
			}
		}
		
		return returnValue;
	}
	
	/**
	 * Calculates and returns the current change needed for the document to
	 * reach it's optimal feature values.
	 * 
	 * @return
	 * 		The double value for the current change needed, to get the actual
	 * 		"percent to the goal", simply take this return value, subtract it
	 * 		from the saved value from getMaxChangeNeeded(), then divide by
	 * 		the saved value from getMaxChangeNeeded(). This should all be handled
	 * 		within AnonymityBar.java
	 */
	public double getCurrentChangeNeeded() {
		int numAttribs = DataAnalyzer.topAttributes.length;
		double currentChangeNeeded = 0;
		Attribute tempAttrib;

		for(int i = 0; i < numAttribs; i++) {
			tempAttrib = DataAnalyzer.topAttributes[i];

			// not really sure how to handle this...
			if (tempAttrib.getFullName().contains("Percentage") || tempAttrib.getFullName().contains("Average"))
				continue;
			if (tempAttrib.getToModifyValue() <= 0)
				continue;

			currentChangeNeeded += tempAttrib.getPercentChangeNeeded(false,false,true);
		}

		return currentChangeNeeded;
	}	
	
	/**
	 * Calculates and returns the anonymity index of the document if all the
	 * document features were equal to their target values. This is the ideal
	 * scenario (though it never happens in the real world since features rely
	 * so much on one another and can easily change each other).<br><br>
	 *
	 * This should only need to be called ONCE, the return value should be saved
	 * and references for the future percent change calculations.
	 * 
	 * @return
	 * 		The beginning where getCurrentChangeNeeded() started from, acts as the
	 * 		denominator for the percent change needed calculations in AnonymityBar
	 */
	public double getMaxChangeNeeded() {
		int numAttribs = DataAnalyzer.topAttributes.length;
		double maxChange = 0;
		Attribute tempAttrib;

		for(int i = 0; i < numAttribs; i++) {
			tempAttrib = DataAnalyzer.topAttributes[i];

			// not really sure how to handle this...
			if(tempAttrib.getFullName().contains("Percentage") || tempAttrib.getFullName().contains("Average"))
				continue;
			if(tempAttrib.getToModifyValue() <= 0)
				continue;

			maxChange += Math.abs(tempAttrib.getFeatureBaselinePercentChangeNeeded()) / 100;
		}
		
		return maxChange;
	}
}
	
