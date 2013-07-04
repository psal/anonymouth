package edu.drexel.psal.anonymouth.utils;

import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.drexel.psal.anonymouth.engine.Attribute;
import edu.drexel.psal.anonymouth.engine.DataAnalyzer;
import edu.drexel.psal.jstylo.generics.Logger;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.TreebankLanguagePack;

enum TENSE {PAST,PRESENT,FUTURE};

enum POV {FIRST_PERSON,SECOND_PERSON,THIRD_PERSON};

enum CONJ {SIMPLE,PROGRESSIVE,PERFECT,PERFECT_PROGRESSIVE};

/**
 * 
 * @author Andrew W.E. McDonald
 * @author Marc Barrowclift
 * @author Joe Muoio
 * 
 */
public class TaggedDocument implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2258415935896292619L;
	private final String NAME = "( "+this.getClass().getSimpleName()+" ) - ";
	protected ArrayList<TaggedSentence> taggedSentences;
	
	protected String documentTitle = "None";
	protected String documentAuthor = "None";
	protected ArrayList<ArrayList<TENSE>> tenses;
	protected ArrayList<ArrayList<POV>> pointsOfView;
	protected ArrayList<ArrayList<CONJ>> conjugations;
	protected List<List<? extends HasWord>> sentencesPreTagging;
	protected transient Iterator<List<? extends HasWord>> preTagIterator;
	protected transient TreebankLanguagePack tlp = new PennTreebankLanguagePack(); 
	protected transient List<? extends HasWord> sentenceTokenized;
	protected transient Tokenizer<? extends HasWord> toke;
	protected final int PROBABLE_NUM_SENTENCES = 50;
	public static SentenceTools jigsaw;
	//protected transient Iterator<String> strIter;
	private String ID; 
	private int totalSentences=0;
	public SpecialCharacterTracker specialCharTracker;
	private double baseline_percent_change_needed = 0; // This may end up over 100%. That's unimportant. This is used to gauge the change that the rest of the document needs -- this is normalized to 100%, effectivley.
	private boolean can_set_baseline_percent_change_needed = true;
	public static boolean userDeletedSentence = false;

	/**
	 * Constructor for TaggedDocument
	 */
	public TaggedDocument(){
		jigsaw = new SentenceTools();
		specialCharTracker = new SpecialCharacterTracker();
		taggedSentences = new ArrayList<TaggedSentence>(PROBABLE_NUM_SENTENCES);
	}
	
	/**
	 * Constructor for TaggedDocument, accepts an untagged string (a whole document), and makes sentence tokens out of it.
	 * @param untaggedDocument
	 */
	public TaggedDocument(String untaggedDocument){
		jigsaw = new SentenceTools();
		specialCharTracker = new SpecialCharacterTracker();
		taggedSentences = new ArrayList<TaggedSentence>(PROBABLE_NUM_SENTENCES);
		makeAndTagSentences(untaggedDocument, true);
	}
	 
	/**
	 * 
	 * @param untaggedDocument
	 * @param docTitle
	 * @param author
	 */
	public TaggedDocument(String untaggedDocument, String docTitle, String author){
		this.documentTitle = docTitle;
		this.documentAuthor = author;
		specialCharTracker = new SpecialCharacterTracker();
		this.ID = documentTitle+"_"+documentAuthor;

		jigsaw = new SentenceTools();
		taggedSentences = new ArrayList<TaggedSentence>(PROBABLE_NUM_SENTENCES);
		makeAndTagSentences(untaggedDocument, true);
	}
	
	/**
	 * returns the number of Words in the TaggedDocument
	 * @return
	 */
	public int getWordCount(){
		int wordCount = 0;
		for(TaggedSentence ts:taggedSentences){
			wordCount += ts.size();
		}
		return wordCount;
	}
	
	/**
	 * returns all Words in the TaggedDocument
	 * @return
	 */
	public ArrayList<Word> getWords() {
		int numWords = getWordCount();
		ArrayList<Word> theWords = new ArrayList<Word>(numWords);
		for(TaggedSentence ts: taggedSentences){
			theWords.addAll(ts.wordsInSentence);
		}
		return theWords;
	}
	
	/**
	 * Returns the String word for each word in a given tagged sentence
	 * @param sentence
	 * @return
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
	 * consolidates features for an ArrayList of TaggedSentences (does both word level and sentence level features)
	 * @param alts
	 */
	public void consolidateFeatures(ArrayList<TaggedSentence> alts){
		
		for(TaggedSentence ts:alts){
			ConsolidationStation.featurePacker(ts);
		}
	}
		
	
	/**
	 * consolidates features for a single TaggedSentence object
	 * @param ts
	 */
	public void consolidateFeatures(TaggedSentence ts){
		ConsolidationStation.featurePacker(ts);
	}
	
	/**
	 * Takes a String of sentences (can be an entire document), breaks it up into individual sentences (sentence tokens), breaks those up into tokens, and then tags them (via MaxentTagger).
	 * Each tagged sentence is saved into a TaggedSentence object, along with its untagged counterpart.
	 * @param untagged String containing sentences to tag
	 * @param appendTaggedSentencesToGlobalArrayList if true, appends the TaggedSentence objects to the TaggedDocument's arraylist of TaggedSentences
	 * @return the TaggedSentences
	 */
	public ArrayList<TaggedSentence> makeAndTagSentences(String untagged, boolean appendTaggedSentencesToGlobalArrayList){
		ArrayList<String[]> untaggedSents = jigsaw.makeSentenceTokens(untagged);
		
		ArrayList<TaggedSentence> taggedSentences = new ArrayList<TaggedSentence>(untaggedSents.size());
		//sentencesPreTagging = new ArrayList<List<? extends HasWord>>();
		Iterator<String[]> strRayIter = untaggedSents.iterator();
		String[] tempRay; // 
		String tempSent;
		String tempSentWithEOSSubs;
		while(strRayIter.hasNext()){
			tempRay = strRayIter.next();
			tempSent = tempRay[0];
			tempSentWithEOSSubs = tempRay[1];
			
			TaggedSentence taggedSentence = new TaggedSentence(tempSent);
			toke = tlp.getTokenizerFactory().getTokenizer(new StringReader(tempSent));
			sentenceTokenized = toke.tokenize();
			taggedSentence.setTaggedSentence(Tagger.mt.tagSentence(sentenceTokenized));
			consolidateFeatures(taggedSentence);
			taggedSentence.untaggedWithEOSSubs = tempSentWithEOSSubs;
			
			// todo: put stuff here
			taggedSentences.add(taggedSentence); 
			
		}
		if(appendTaggedSentencesToGlobalArrayList == true){
			int i = 0;
			int len = taggedSentences.size();
			for(i=0;i<len;i++){
				totalSentences++;
				this.taggedSentences.add(taggedSentences.get(i)); 
			}
			initializeSpecialCharTracker();
		}
		return taggedSentences;
	}
	
	private void initializeSpecialCharTracker(){
		char[] EOSSubbedDoc = getUntaggedDocument(true).toCharArray();
		int numChars = EOSSubbedDoc.length;
		int i;
		for (i=0; i < numChars; i++){
			if (EOSSubbedDoc[i] == SpecialCharacterTracker.replacementEOS[0]){ // period replacement
				specialCharTracker.addEOS(EOSSubbedDoc[i],i-1,false);
			}
			else if (EOSSubbedDoc[i] == SpecialCharacterTracker.replacementEOS[1]){ // question mark replacement
				specialCharTracker.addEOS(EOSSubbedDoc[i],i-1,false);
			}
			else if (EOSSubbedDoc[i] == SpecialCharacterTracker.replacementEOS[2]){ // exclamation point replacement
				specialCharTracker.addEOS(EOSSubbedDoc[i],i-1,false);
			}
		}
	}
	
	/**
	 * Essentially the same thing as getSentenceNumAt(), except instead of accepting a sentence number and finding the taggedSentence
	 * that corresponds to that number it accepts an index (caret position), and finds the taggedSentence that corresponds to that index.
	 * @param index - the position in the document text.
	 * @return returnValue - the TaggedSentence found at the index. If none exists, null is returned.
	 */
	public TaggedSentence getTaggedSentenceAtIndex(int index) {
		int size = getNumSentences();
		int newIndex = 0;
		int pastIndex = 0;
		int length = 0;
		TaggedSentence returnValue = null;
		
		for (int i = 0; i < size; i++) {
			length = taggedSentences.get(i).getUntagged(false).length();
			newIndex = length + pastIndex;
			
			if (index >= pastIndex && index <= newIndex) {
				returnValue = taggedSentences.get(i);
				break;
			} else {
				//pastIndex += length;
				pastIndex = newIndex;
			}
		}
		
		return returnValue;
	}
	
	/**
	 * Deletes translations and translation names for each sentence in the given TaggedDocument instance.
	 */
	public void deleteTranslations() {
		int size = getNumSentences();
		for (int i = 0; i < size; i++) {
			taggedSentences.get(i).deleteTranslations();
		}
	}
	
	/**
	 * Checks all sentences in the tagged document and returns whether or not they are all translated.
	 */
	public boolean isTranslated() {
		boolean result = true;
		int size = getNumSentences();
		
		for (int i = 0; i < size; i++) {
			if (!taggedSentences.get(i).isTranslated()) {
				result = false;
				break;
			}
		}
		
		return result;
	}
	
	/**
	 * Uses a given index and calculates the sentence number of the index.
	 * @param index - The positions in the document text.
	 * @return returnValue - The sentence number. If none is found, -1 is returned.
	 */
	public int getSentenceNumAtIndex(int index) {
		int size = getNumSentences();
		int end = 0;
		int start = 0;
		int currentSentNum = 0;
		int returnValue = -1;
		
		
		for (int i = 0; i < size; i++) {
			end = taggedSentences.get(i).getUntagged(false).length() + start;

			if (index >= start && index < end) {
				returnValue = currentSentNum;
				break;
			} else {
				start = end;
				currentSentNum++;
			}
		}
		
		return returnValue;
	}
	
	/**
	 * returns the ArrayList of TaggedSentences
	 * @return
	 */
	public ArrayList<TaggedSentence> getTaggedDocument(){
		return taggedSentences;
	}
		

	/**
	 * returns the untagged sentences of the TaggedDocument
	 * @return
	 */
	public ArrayList<String> getUntaggedSentences(boolean returnWithEOSSubs){
		ArrayList<String> sentences = new ArrayList<String>();
		if (returnWithEOSSubs){
			for (int i=0;i<taggedSentences.size();i++)
				sentences.add(taggedSentences.get(i).getUntagged(true));
		}
		else{
			for (int i=0;i<taggedSentences.size();i++)
				sentences.add(taggedSentences.get(i).getUntagged(false));
		}
		return sentences;
	}
	
	
	/**
	 * updates the referenced Attributes 'toModifyValue's (present value) with the amount that must be added/subtracted from each respective value 
	 * @param oldSentence The pre-editing version of the sentence(s)
	 * @param newSentence The post-editing version of the sentence(s)
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
	
	
	
	/**
	 * accepts a variable number of TaggedSentences and returns a single TaggedSentence, preserving all original Word objects
	 * 
	 * Note that the sentences will be concatenated together in the order that they are passed into the method.
	 * @param taggedSentences a variable number of TaggedSentences
	 * @return returns a single tagged sentences with the properties of all the sentences in the list.
	 */
	public TaggedSentence concatSentences(TaggedSentence ... taggedSentences){//ArrayList<TaggedSentence> taggedList){
		TaggedSentence toReturn =new TaggedSentence(taggedSentences[0]);
		int numSents = taggedSentences.length;
		int i;
		for (i=1;i<numSents;i++){
				toReturn.wordsInSentence.addAll(taggedSentences[i].wordsInSentence);
				toReturn.untagged += taggedSentences[i].untagged;
		}
		return toReturn;
	}
	
	/**
	 * accepts an ArrayList of TaggedSentences and returns a single TaggedSentence, preserving all original Word objects
	 * 
	 * Note that the sentences will be concatenated together in the order that they are passed into the method.
	 * @param taggedSentences an ArrayList of TaggedSentences
	 * @return returns a single tagged sentences with the properties of all the sentences in the list.
	 */
	public TaggedSentence concatSentences(ArrayList<TaggedSentence> taggedSentences){//ArrayList<TaggedSentence> taggedList){
		TaggedSentence toReturn =new TaggedSentence(taggedSentences.get(0));
		int numSents = taggedSentences.size();
		int i;
		TaggedSentence thisTaggedSent;
		for (i=1;i<numSents;i++){
			thisTaggedSent = taggedSentences.get(i);
			toReturn.wordsInSentence.addAll(thisTaggedSent.wordsInSentence);
			toReturn.untagged += thisTaggedSent.untagged;
			toReturn.sentenceLevelFeaturesFound.merge(thisTaggedSent.sentenceLevelFeaturesFound);
		}
		return toReturn;
	}	
	
	
	/**
	 * Merges the TaggedSentences specified by the indices in 'taggedSentenceIndicesToConcat' into one TaggedSentence.  
	 * 
	 * Note that the sentences will be concatenated together in the order that they are passed into the method.
	 * @param taggedSentenceIndicesToConcat
	 * @return The TaggedSentence that resulted from the merging
	 */
	public TaggedSentence concatSentences(int[] taggedSentenceIndicesToConcat){//ArrayList<TaggedSentence> taggedList){
		TaggedSentence toReturn =new TaggedSentence(taggedSentences.get(taggedSentenceIndicesToConcat[0]));
		int numSents = taggedSentenceIndicesToConcat.length;
		int i;
		TaggedSentence thisTaggedSent;
		for (i=1;i<numSents;i++){
			thisTaggedSent = taggedSentences.get(taggedSentenceIndicesToConcat[i]);
			toReturn.wordsInSentence.addAll(thisTaggedSent.wordsInSentence);
			toReturn.untagged += thisTaggedSent.untagged;
			toReturn.sentenceLevelFeaturesFound.merge(thisTaggedSent.sentenceLevelFeaturesFound);
		}
		return toReturn;
	}	
	
	/**
	 * Concatenates the two TaggedSentences (in order), removes the second TaggedSentence, and replaces the first with the concatenated TaggedSentence 
	 * Takes care of bookkeeping.
	 * @param taggedSentenceOne the first TaggedSentence
	 * @param tsOneIndex the first TaggedSentence's index in the TaggedDocument
	 * @param taggedSentenceTwo the second TaggedSentence
	 * @param tsTwoIndex the second TaggedSentence's index in the TaggedDocument
	 * @return
	 */
	public TaggedSentence concatRemoveAndReplace(TaggedSentence taggedSentenceOne, int tsOneIndex, TaggedSentence taggedSentenceTwo, int tsTwoIndex){
		TaggedSentence replaceWith = concatSentences(taggedSentenceOne, taggedSentenceTwo);
		removeAndReplace(tsTwoIndex, "");// delete the second sentence
		System.out.println("*** Replacing: "+taggedSentenceOne.getUntagged(false)+"\n*** With: "+replaceWith.getUntagged(false));
		return removeAndReplace(tsOneIndex,replaceWith);
	}
	
	
	/**
	 * Returns an integer array with the lengths of each sentence (TaggedSentence) in this TaggedDocument. 
	 * Array indices are such that index '0' holds the length of the first sentence, index '1' holds the length of the second sentence, ect..
	 * @return
	 */
	public int[] getSentenceLengths(){
		int i =0;
		int numSents = taggedSentences.size();
		int[] lengthsToReturn = new int[numSents];
		for(i = 0; i < numSents; i++){
			lengthsToReturn[i] = taggedSentences.get(i).getLength();
		}
		return lengthsToReturn;
	}
	
	/**
	 * returns TaggedSentence number 'i' (first sentence is index '0')
	 * @param number the index of the sentence you want 
	 * @return
	 */
	public TaggedSentence getSentenceNumber(int number){
		return taggedSentences.get(number);
	}
		
	
	/**
	 * returns the size of the ArrayList holding the TaggedSentences (i.e. the number of sentences in the document)
	 * @return
	 */
	public int getNumSentences(){
		return taggedSentences.size();
	}

	/**
	 * Adds sentToAdd at placeToAdd in this TaggedDocument
	 * @param sentToAdd
	 * @param placeToAdd
	 */
	public void addTaggedSentence(TaggedSentence sentToAdd, int placeToAdd){
		taggedSentences.add(placeToAdd,sentToAdd);
	}
	
	/**
	 * removes TaggedSentence at indexToRemove from this TaggedDocument. Does NOT take care of any bookkeeping issues -- should only be called by methods that do (removeAndReplace)
	 * @param indexToRemove
	 * @return the removed TaggedSentence
	 */
	private TaggedSentence removeTaggedSentence(int indexToRemove){
		return taggedSentences.remove(indexToRemove);
	}
	
	/**
	 * removes the 'TaggedSentence's at the specified indicesToRemove from this TaggedDocument. Takes care of bookkeeping issues.
	 * @param indicesToRemove
	 * @return an array of the removed TaggedSentences
	 */
	public TaggedSentence[] removeTaggedSentences(int[] indicesToRemove){
		int i;
		int numToRemove = indicesToRemove.length;
		TaggedSentence[] removed = new TaggedSentence[numToRemove];
		for (i = 0; i < numToRemove; i++)
			removed[i] = removeAndReplace(indicesToRemove[i],"");
		return removed;
	}
	
	/**
	 * 
	 * @param sentsToAdd a String representing the sentence(s) from the editBox
	 * @return the TaggedSentence that was removed
	 */
	public TaggedSentence removeAndReplace(int sentNumber, String sentsToAdd){//, int indexToRemove, int placeToAdd){
		TaggedSentence toReplace = taggedSentences.get(sentNumber);
		Logger.logln(NAME+"removing: "+toReplace.getUntagged(false));
		Logger.logln(NAME+"adding: "+sentsToAdd);
		
		if (sentsToAdd.matches("^\\s*$")) {//checks to see if the user deleted the current sentence
			//CALL COMPARE
			TaggedSentence wasReplaced = removeTaggedSentence(sentNumber);
			Logger.logln(NAME+"User deleted a sentence.");
			updateReferences(toReplace,new TaggedSentence(""));//all features must be deleted
			totalSentences--;
			userDeletedSentence = true;
			return wasReplaced;
		}
		
		ArrayList<TaggedSentence> taggedSentsToAdd = makeAndTagSentences(sentsToAdd,false);

		TaggedSentence wasReplaced = removeTaggedSentence(sentNumber);
		totalSentences--;
		//call compare
		int i;
		int len = taggedSentsToAdd.size();
		for(i = 0; i < len; i++){
			//removeTaggedSentence(sentNumber);
			addTaggedSentence(taggedSentsToAdd.get(i),sentNumber);
			sentNumber++;
			totalSentences++;
		}
		TaggedSentence concatted = concatSentences(taggedSentsToAdd);

		updateReferences(toReplace,concatted);
		return wasReplaced;
	}
	
	/**
	 * Removes multiple sentences and replaces them with a single TaggedSentence. To be used with the right-click menu
	 * item "combine sentences".
	 * @param sentsToRemove - An ArrayList of TaggedSentences to remove
	 * @param sentToAdd - The TaggedSentence to want to replace them all with.
	 * @return startingSentence - The index (0-based) of the first sentence removed.
	 */
	public int removeMultipleAndReplace(ArrayList<TaggedSentence> sentsToRemove, TaggedSentence sentToAdd) {
		int size = sentsToRemove.size();
		int startingSentence = 0;
		
		for (int i = 0; i < size; i++) {
			if (i == 0) {
				startingSentence = taggedSentences.indexOf(sentsToRemove.get(i));
			}
			taggedSentences.remove(sentsToRemove.get(i));
			totalSentences--;
		}
		
		addTaggedSentence(sentToAdd, startingSentence);
		totalSentences++;
		
		//TODO is this okay?
		for (int j = 0; j < size; j++) {
			updateReferences(sentsToRemove.get(j), sentToAdd);
		}
		
		return startingSentence;
	}
	
	/**
	 * Removes the TaggedSentence at 'sentNumber', and switches in 'toAdd' in its place. Takes care of all bookkeeping issues.
	 * @param sentNumber
	 * @param toAdd
	 * @return
	 */
	public TaggedSentence removeAndReplace(int sentNumber, TaggedSentence toAdd){//, int indexToRemove, int placeToAdd){
		TaggedSentence toReplace = taggedSentences.get(sentNumber);
		Logger.logln(NAME+"removing: "+toReplace.toString());
		Logger.logln(NAME+"adding: "+toAdd.getUntagged(false));
		if(toAdd.getUntagged(false).matches("^\\s*$")){//checks to see if the user deleted the current sentence
			//CALL COMPARE
			TaggedSentence wasReplaced = removeTaggedSentence(sentNumber);
			Logger.logln(NAME+"User deleted a sentence.");
			updateReferences(toReplace,new TaggedSentence(""));//all features must be deleted
			totalSentences--;
			return wasReplaced;
		}
		// no need to subtract one from totalSentences when removing a sentence, because we are putting a new sentence in its place immediatly 
		TaggedSentence wasReplaced = removeTaggedSentence(sentNumber);
		addTaggedSentence(toAdd,sentNumber);
		
		System.out.println("TaggedSent to add: "+toAdd.toString());
		System.out.println("TaggedSent to remove: "+toReplace.toString());
		updateReferences(toReplace,toAdd);
		return wasReplaced;
	}
	
//	public void replaceTaggedSentence(int sentNumber, TaggedSentence sentToAdd) {
//		TaggedSentence toReplace = taggedSentences.get(sentNumber);
//		Logger.logln(NAME+"removing: "+toReplace.toString());
//		Logger.logln(NAME+"adding: "+ sentToAdd.getUntagged());
//		
//		if(sentToAdd.getUntagged().matches("\\s*")){//checks to see if the user deleted the current sentence
//			//CALL COMPARE
//			removeTaggedSentence(sentNumber);
//			Logger.logln(NAME+"User deleted a sentence.");
//			updateReferences(toReplace,new TaggedSentence(""));//all features must be deleted
//			totalSentences--;
//		}
//		
//		Scanner s = new Scanner(System.in);
//		removeTaggedSentence(sentNumber);
//		addTaggedSentence(sentToAdd, sentNumber);
//		
//		System.out.println("TaggedSent to add: " + sentToAdd.getUntagged());
//		System.out.println("TaggedSent to remove: " + toReplace.toString());
//		updateReferences(toReplace, sentToAdd);	
//	}
	
	/**
	 * Returns the ArrayList holding all TaggedSentences in this TaggedDocument
	 * @return
	 */
	public ArrayList<TaggedSentence> getTaggedSentences(){
		return taggedSentences;
	}
	
	
	public String getUntaggedDocument(boolean returnSubEOS){
		String str = "";
		if (returnSubEOS){
			for (int i=0;i<totalSentences;i++){
				str+=taggedSentences.get(i).getUntagged(true);
			}
		}
		else{
			for (int i=0;i<totalSentences;i++){
				str+=taggedSentences.get(i).getUntagged(false);
			}
		}
		return str;
			
	}
	
	
	/**
	 * Calculates and returns the document's anonymity index.
	 * @return
	 */
	public double getAnonymityIndex(){
		double totalAI = 0;
		int numSents = taggedSentences.size();
		int i;
		//System.out.println("NUMSENTS: " + numSents);
		for (i = 0; i < numSents; i++){
			totalAI += taggedSentences.get(i).getSentenceAnonymityIndex();
			//System.out.println("TOTALAI: " + totalAI);
		}
		return totalAI;
	}
	
	/**
	 * Calculates and returns the anonymity index of the document if the all of the document's features (that are in 'topAttributes') are equal to their target values.
	 * @return
	 */
	public double getTargetAnonymityIndex(){
		int i;
		int numAttribs = DataAnalyzer.topAttributes.length;
//		double totalFeatures = 0;
		double anonIndex = 0;
		Attribute tempAttrib;
//		for(i = 0; i < numAttribs; i++){
//			tempAttrib = DataAnalyzer.topAttributes[i];
//			if(tempAttrib.getFullName().contains("Percentage") || tempAttrib.getFullName().contains("Average"))
//				continue; // We don't want to add percentages into the mix of total features
//			totalFeatures += DataAnalyzer.topAttributes[i].getTargetValue();
//		}
		for(i = 0; i < numAttribs; i++){
			tempAttrib = DataAnalyzer.topAttributes[i];
			if(tempAttrib.getFullName().contains("Percentage") || tempAttrib.getFullName().contains("Average"))
				continue; // not really sure how to handle this...
			anonIndex += (tempAttrib.getTargetValue())*(tempAttrib.getInfoGain()*(Math.abs(tempAttrib.getFeatureBaselinePercentChangeNeeded())));
		}
		return anonIndex;
		
	}
	

	public String toString(){
		String toReturn = "Document Title: "+documentTitle+" Author: "+documentAuthor+"\n";
		int len = taggedSentences.size();
		int i =0;
		for(i=0;i<len;i++){
			toReturn += taggedSentences.get(i).toString()+"\n";
		}
		return toReturn;
	}
	
	
	/**
	 * Constructor for TaggedDocument. Essentially does a deep copy of the input TaggedDocument.
	 * @param td
	 */
	public TaggedDocument(TaggedDocument td){
		int i;
		int numTaggedSents = td.taggedSentences.size();
		
		taggedSentences = new ArrayList<TaggedSentence>(PROBABLE_NUM_SENTENCES);
		// copy TaggedSentences
		for (i = 0; i < numTaggedSents; i++)
			taggedSentences.add(new TaggedSentence(td.taggedSentences.get(i)));
		
		specialCharTracker = td.specialCharTracker;
			
		// copy document author and title (Strings are immutable)
		documentAuthor = td.documentAuthor;
		documentTitle = td.documentTitle;
		
		// copy the ArrayLists of ArrayLists of grammar related concepts
		// first tenses
//		numArrayLists = td.tenses.size();
//		ArrayList<ArrayList<TENSE>> tempTenses = new ArrayList<ArrayList<TENSE>>(numArrayLists);
//		for(i = 0; i < numArrayLists; i++){
//			ArrayList<TENSE> tempOld = td.tenses.get(i);
//			numValues = tempOld.size();
//			ArrayList<TENSE> tempPartialTenses = new ArrayList<TENSE>(numValues);
//			for(j = 0; j < numValues; j++){
//				tempPartialTenses.add(tempOld.get(j));
//			}
//			tempTenses.add(tempPartialTenses);
//		}
//		tenses = tempTenses;
		
		// then points of view
//		numArrayLists = td.pointsOfView.size();
//		ArrayList<ArrayList<POV>> tempPOVs = new ArrayList<ArrayList<POV>>(numArrayLists);
//		for(i = 0; i < numArrayLists; i++){
//			ArrayList<POV> tempOld = td.pointsOfView.get(i);
//			numValues = tempOld.size();
//			ArrayList<POV> tempPartialPOVs = new ArrayList<POV>(numValues);
//			for(j = 0; j < numValues; j++){
//				tempPartialPOVs.add(tempOld.get(j));
//			}
//			tempPOVs.add(tempPartialPOVs);
//		}
//		pointsOfView = tempPOVs;

		// and then conjugations
//		numArrayLists = td.conjugations.size();
//		ArrayList<ArrayList<CONJ>> tempConjugations = new ArrayList<ArrayList<CONJ>>(numArrayLists);
//		for(i = 0; i < numArrayLists; i++){
//			ArrayList<CONJ> tempOld = td.conjugations.get(i);
//			numValues = tempOld.size();
//			ArrayList<CONJ> tempPartialConjugations= new ArrayList<CONJ>(numValues);
//			for(j = 0; j < numValues; j++){
//				tempPartialConjugations.add(tempOld.get(j));
//			}
//			tempConjugations.add(tempPartialConjugations);
//		}
//		conjugations = tempConjugations;
		
		// Next copy the ID (not really sure what this is, but I don't see a good reason to throw it out)
		ID = td.ID;
		
		// Then the total number of sentences (could probably chuck ths)
		totalSentences = td.totalSentences;
		
		// Finally, copy the specialCharTracker (SpecialCharacterTracker)
		specialCharTracker = new SpecialCharacterTracker(td.specialCharTracker);
		
	}
	
	
	/*
	public static void main(String[] args){
		String text1 = "people enjoy coffee, especially in the mornings, because it helps to wake me up. My dog is fairly small, but she seems not to realize it when she is around bigger dogs. This is my third testing sentence. I hope this works well.";
		TaggedDocument testDoc = new TaggedDocument(text1);
		System.out.println(testDoc.toString());			
		//System.out.println(testDoc.getFunctionWords());
		
	}
	*/
	
	
	/**
	 * Loops through all topAttribute Attributes in DataAnalyzer, and returns the average percent change needed. This is a first stab at some
	 * way to deliver a general sense of the degree of anonymity achived at any given point. This method must be called before any changes are made to set 
	 * a baseline percent change. That number is what everything from that point on gets compared (normalized) to. 
	 * 
	 * It is important to note that this does not take into consideration the information gain of any feature. So, the less important features will have the same effect on this number
	 * as the most important features. This should probably change...
	 * @param is_initial 'true' if this is the first time the function is being called for this document (basically, if you are calling it to set the document's baseline percent change needed, this should be true. If you want to know how much the document has changed, this should be false. This will be false all the time, except for the first time it's called).
	 * @return
	 * The overall percent change that is needed. 
	 */
	public double getAvgPercentChangeNeeded(boolean is_initial){
		int total_attribs = 0;
		double total_percent_change = 0;
		for (Attribute attrib : DataAnalyzer.topAttributes) {
			total_percent_change += Math.abs(attrib.getPercentChangeNeeded(false,false,true));
			total_attribs ++;
		}
		double avg_percent_change = total_percent_change/total_attribs;
		if (is_initial)
			return avg_percent_change;
		else{
			double percent_change_needed = baseline_percent_change_needed - (Math.abs(avg_percent_change - baseline_percent_change_needed)/baseline_percent_change_needed);
			return percent_change_needed;
		}
	}
	
	/**
	 * Sets baseline_percent_change_needed. This is the ONLY time that 'getAvgPercentChangeNeeded' will be called with 'true'.
	 */
	public void setBaselinePercentChangeNeeded(){
		if (can_set_baseline_percent_change_needed){
			baseline_percent_change_needed = getAvgPercentChangeNeeded(true);
			can_set_baseline_percent_change_needed = false;
		}
	}

	
}
	
