package edu.drexel.psal.anonymouth.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import com.jgaap.generics.Document;

import edu.stanford.nlp.ling.TaggedWord;
import edu.drexel.psal.ANONConstants;
import edu.drexel.psal.anonymouth.engine.Attribute;
import edu.drexel.psal.anonymouth.engine.DataAnalyzer;
import edu.drexel.psal.anonymouth.engine.InstanceConstructor;
import edu.drexel.psal.anonymouth.gooie.GUIMain;
import edu.drexel.psal.anonymouth.gooie.PropertiesUtil;
import edu.drexel.psal.anonymouth.helpers.ErrorHandler;
import edu.drexel.psal.jstylo.generics.CumulativeFeatureDriver;
import edu.drexel.psal.jstylo.generics.InstancesBuilder;
import edu.drexel.psal.jstylo.generics.ProblemSet;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.TreebankLanguagePack;

/**
 * 
 * @author Joe Muoio
 * @author Andrew W.E. McDonald
 */

public class TaggedSentence implements Comparable<TaggedSentence>, Serializable {

	private static final long serialVersionUID = -8793730374516462574L;
	//private final String NAME = "( "+this.getClass().getName()+" ) - ";
	protected SparseReferences sentenceLevelFeaturesFound;

	protected String untagged;
	protected ArrayList<Word> wordsInSentence;
	protected ArrayList<String> translationNames = new ArrayList<String>();
	protected ArrayList<TaggedSentence> translations = new ArrayList<TaggedSentence>();
	protected ArrayList<Double> translationAnonymity = new ArrayList<Double>();

	private final int PROBABLE_MAX = 3;

	protected ArrayList<TENSE> tense = new ArrayList<TENSE>(PROBABLE_MAX);
	protected ArrayList<POV> pointOfView = new ArrayList<POV>(PROBABLE_MAX);
	protected ArrayList<CONJ> conjugations = new ArrayList<CONJ>(PROBABLE_MAX);

	protected transient List<? extends HasWord> sentenceTokenized;
	protected transient Tokenizer<? extends HasWord> toke;
	protected transient TreebankLanguagePack tlp = new PennTreebankLanguagePack(); 
	
	private InstanceConstructor instance;
	private boolean done = false;
	public TranslatorThread translator;


	/*
	private TaggedSentence(int numWords, int numTranslations) {
		wordsInSentence = new ArrayList<Word>(numWords);
		translationNames = new ArrayList<String>(numTranslations);
		translations = new ArrayList<TaggedSentence>(numTranslations);
	}
	*/

	/**
	 * Constructor -- accepts an untagged string.
	 * @param untagged
	 */
	public TaggedSentence(String untagged) {
		sentenceLevelFeaturesFound = new SparseReferences(10); // probably won't find more than 10 features in the sentence.
		wordsInSentence = new ArrayList<Word>(10);
		translator = new TranslatorThread(GUIMain.inst);
		this.untagged = untagged;
	}
	
	/**
	 * Constructor for TaggedSentence. Essentially does a deep copy of the input TaggedSentence
	 * @param ts 
	 */
	public TaggedSentence(TaggedSentence ts){
		int i;
		int numElements;
		int numWords = ts.wordsInSentence.size();
		int numTranslations = ts.translations.size();
		wordsInSentence = new ArrayList<Word>(numWords);
		translations = new ArrayList<TaggedSentence>(numTranslations);
		// copy the SparseReferences for the sentence level features
		sentenceLevelFeaturesFound = new SparseReferences(ts.sentenceLevelFeaturesFound);

		// copy the untagged string
		untagged = ts.untagged;
		
		// Next copy the Word objects that make up the sentence
		for(i = 0; i < numWords; i++)
			wordsInSentence.add(new Word(ts.wordsInSentence.get(i)));
		
		// Then copy the translation information (language names and translations). Since translationNames is an ArrayList of Strings -- and strings are immutable -- we can just do the equal thing
		for(i = 0; i < numTranslations; i++)
			translations.add(new TaggedSentence(ts.translations.get(i)));
		translationNames = ts.translationNames;
		translationAnonymity = ts.translationAnonymity;
		translator = ts.translator;

		// copy the ArrayLists of ArrayLists of grammar related concepts
		// first tenses
		numElements = ts.tense.size();
		for(i = 0; i < numElements; i++)
			tense.add(ts.tense.get(i));
		// then points of view
		numElements = ts.pointOfView.size();
		for(i = 0; i < numElements; i++)
			pointOfView.add(ts.pointOfView.get(i));
		// and then conjugations
		numElements = ts.conjugations.size();
		for(i = 0; i < numElements; i++)
			conjugations.add(ts.conjugations.get(i));
	}
	

	/**
	 * Gets the translations for this tagged sentence.
	 * @return
	 */
	public ArrayList<TaggedSentence> getTranslations() {
		return translations;
	}

	/**
	 * Sets the translation for this tagged sentence to the given ArrayList.
	 * @param set - ArrayList of translations
	 */
	public void setTranslations(ArrayList<TaggedSentence> set) {
		translations = set;
	}

	/**
	 * Gets the translation names for each corresponding translation for this tagged sentence.
	 * @return
	 */
	public ArrayList<String> getTranslationNames() {
		return translationNames;
	}

	/**
	 * Sets the translation names for this tagged sentence to the given ArrayList.
	 * (E.G. "French", "German", "Italian")
	 * @param set - ArrayList of language names
	 */
	public void setTranslationNames(ArrayList<String> set) {
		translationNames = set;
	}
	
	/**
	 * Gets the Anonymity Index for each corresponding translation for this tagged sentence.
	 */
	public ArrayList<Double> getTranslationAnonymity() {
		return translationAnonymity;
	}
	
	/**
	 * Sets the Anonymity Index each translation in this tagged sentence
	 * @param set - ArrayList of anonymity indices
	 */
	public void setTranslationAnonymity(ArrayList<Double> set) {
		translationAnonymity = set;
	}

	/**
	 * Sorts the translations of this tagged sentence by Anonymity Index.
	 */
	public void sortTranslations() {
		int numTranslations = translations.size();
		double[][]  toSort = new double[translations.size()][2]; // [Anonymity Index][index of specific translation] => will sort by col 1 (AI)
		int i;
		instance = GUIMain.inst.documentProcessor.documentMagician.getInstanceConstructor();
		
		InstancesBuilder builder = instance.jstylo.getUnderlyingInstancesBuilder();
		if (builder.isUsingCache())
			builder.validateCFDCache();
		for(i = 0; i < numTranslations; i++){
			String doc; 
			do {
				doc = GUIMain.inst.editorDriver.taggedDoc.getUntaggedDocument();
				String translatedSent = translations.get(i).getUntagged();
				doc = doc.replaceFirst(untagged, translatedSent);
			} while(doc.isEmpty());
			String pathToTempModdedDoc = ANONConstants.DOC_MAGICIAN_WRITE_DIR + "Trans" + ".txt";
			List<Document> toModifySet = new LinkedList<Document>();
				try {
					File tempModdedDoc;
					tempModdedDoc = new File(pathToTempModdedDoc);
					tempModdedDoc.deleteOnExit();
					FileWriter writer = new FileWriter(tempModdedDoc,false);
					writer.write(doc);
					writer.close(); 
				} catch (IOException e) {
					e.printStackTrace();
					try {
						File tempModdedDoc;
						tempModdedDoc = new File(pathToTempModdedDoc);
						tempModdedDoc.deleteOnExit();
						FileWriter writer = new FileWriter(tempModdedDoc,false);
						writer.write(doc);
						writer.close();
					} catch (IOException ex) {}
				}
				Document newModdedDoc = new Document(pathToTempModdedDoc,"","Trans");
				toModifySet.add(newModdedDoc);
				try {
					toModifySet.get(0).load();
				} catch (Exception e) {
					e.printStackTrace();
				}
			
			try {
				ProblemSet ps = builder.getProblemSet();
				
				//reset test data
				List<String> toRemove = new ArrayList<String>();
				for (String author :  ps.getTestAuthorMap().keySet()){
					toRemove.add(author);
				}
				for (String s : toRemove){
					ps.removeTestAuthor(s);
				}
				
				//add in new test data
				for (Document d : toModifySet) {
					d.setAuthor(ANONConstants.DUMMY_NAME);
					ps.addTestDoc(d.getAuthor(), d);
				}
				builder.setProblemSet(ps);
				builder.createTestInstancesThreaded();
			} catch(Exception e) {
				e.printStackTrace();
				ErrorHandler.StanfordPOSError();
			}
			toSort[i][0] = GUIMain.inst.documentProcessor.documentMagician.getAuthorAnonimity(instance.jstylo.getTestInstances())[0];
			toSort[i][1] = i;
		}

	    Arrays.sort(toSort, new Comparator<double[]>() {
	        public int compare(double[] a, double[] b) {
	            return Double.compare(a[0], b[0]);
	        }
	    });

		ArrayList<TaggedSentence> sortedTrans = new ArrayList<TaggedSentence>(numTranslations);
		ArrayList<String> sortedTranNames = new ArrayList<String>(numTranslations);
		ArrayList<Double> sortedTranAnonymity = new ArrayList<Double>(numTranslations);
		for(i = 0; i<numTranslations; i++){
			sortedTrans.add(i,translations.get((int)toSort[i][1]));
			sortedTranNames.add(i,translationNames.get((int)toSort[i][1]));
			sortedTranAnonymity.add(i,toSort[i][0]);
		}

		translations = sortedTrans; // set translations to be the same list of translated sentences, but now in order of Anonymity Index
		translationNames = sortedTranNames; // set translations to be the same list of translated sentences, but now in order of Anonymity Index
		translationAnonymity = sortedTranAnonymity;
	}

	public void resetTranslations() {
		translator.reset();
		translations.clear();
		translationAnonymity.clear();
	}

	/**
	 * Returns true if the translations ArrayList for this tagged sentence has a size > 0, false if size == 0
	 * @return
	 */
	public boolean hasTranslations() {
		return (translations.size() > 0);
	}

	public boolean isTranslated() {
		return (translations.size() >= 15);
	}
	
	/**
	 * Tags the untagged sentence in this TaggedSentence, and finds the features present in it. 
	 * For use when <i>not</i> storing TaggedSentence in a TaggedDocument
	 */
	public void tagAndGetFeatures() {
		toke = tlp.getTokenizerFactory().getTokenizer(new StringReader(untagged));
		sentenceTokenized = toke.tokenize();
		setTaggedSentence(Tagger.mt.tagSentence(sentenceTokenized));
		ConsolidationStation.featurePacker(this);
	}


	/**
	 * Set's the TaggedSentence which is an ArrayList of Word objects
	 * @param tagged the tagged sentence as output by the Standford POS Tagger
	 * @return
	 */
	public boolean setTaggedSentence(ArrayList<TaggedWord> tagged){
		int numTagged = tagged.size();
		for (int i=0;i< numTagged;i++){
			Word newWord=new Word(tagged.get(i).word(),tagged.get(i).tag());
			//newWord=ConsolidationStation.getWordFromString(tagged.get(i).word());
			//newWord.setPOS(tagged.get(i).tag());
			//addToWordList(tagged.get(i).word(),newWord);
			wordsInSentence.add(newWord);
		}	
		//setGrammarStats();

		//Logger.logln(NAME+"WordList"+wordList.toString());

		return true;
	}
	

	/**
	 * Retrieves all Reference objects associated with each word in the sentence, and merges them into a single instance of SparseReferences
	 * @return
	 */
	public SparseReferences getReferences(){
		int numWords = this.size();
		SparseReferences allRefs = new SparseReferences((numWords*5)+sentenceLevelFeaturesFound.length()); // just a guess - I don't think well have more than 5 distinct features per word (as an average)
		// merge all "word level" features
		for(Word w: wordsInSentence){
			allRefs.merge(w.wordLevelFeaturesFound);
		}
		// merge all "sentence level" features
		allRefs.merge(sentenceLevelFeaturesFound);
		return allRefs;
	}

	/**
	 * returns the number of Words in the sentence
	 * @return
	 */
	public int size() {
		return wordsInSentence.size();
	}
	
	/**
	 * Clears all translations and translations names associated with the TaggedSentence instance.
	 */
	public void deleteTranslations() {
		translationNames.clear();
		translations.clear();
	}
	
	/**
	 * Returns the length of the sentence (number of characters).
	 * @return
	 */
	public int getLength() {
		return untagged.length();
	}
	
	public ArrayList<Word> getWordsInSentence(){
		return wordsInSentence;
	}
	
	/**
	 * Clears all variables tied to this specific taggedSentence before finally setting itself to null
	 */
	public void delete() {
		sentenceLevelFeaturesFound = null;
		untagged = null;
		wordsInSentence = null;
		translationNames = null;
		translations = null;
		tense = null;
		pointOfView = null;
		conjugations = null;
		sentenceTokenized = null;
		toke = null;
		tlp = null;
		translator = null;
		
		//update the translations panel to reflect the deletion/replacement, in case the sentence had translations in progress
		if (PropertiesUtil.getDoTranslations()) {
			GUIMain.inst.translationsPanel.updateTranslationsPanel(new TaggedSentence(""));
		}
	}

	/**
	 * Allows comparing two TaggedSentence objects based upon anonymity index
	 */
	public int compareTo( TaggedSentence notThisSent){
		double thisAnonIndex = this.getSentenceAnonymityIndex();
		double thatAnonIndex = notThisSent.getSentenceAnonymityIndex();
		if(thisAnonIndex< thatAnonIndex)
			return -1;
		else if (thisAnonIndex == thatAnonIndex)
			return 0;
		else
			return 1;	
	}

	/**
	 * returns a SparseReference object containing the index of each attribute who's value needs to be updated, along with the amount
	 * it must be changed by (if positive, the present value should increase, if negative, it should decrease. Therefore, you only need to 
	 * add the 'value' of each Reference to the 'index'th Attribute's presentValue in the Attribute[] array.  
	 * 
	 * note: the reason this is done at the sentence level rather than the document level, is that users will generally only edit one sentence at a time; so only that part 
	 * of the document can change.
	 * @param oldOne
	 * @return
	 */
	public SparseReferences getOldToNewDeltas(TaggedSentence oldOne){
		SparseReferences oldRefs = oldOne.getReferences();
		return this.getReferences().leftMinusRight(oldRefs); 	
	}

	/**
	 * Computes the AnonymityIndex of this sentence: SUM ((#appearancesOfFeature[i]/numFeaturesFoundInWord)*(infoGainOfFeature[i])*(%changeNeededOfFeature[i])). 
	 * 
	 * This is only done directly for sentence level features (word bigrams, trigrams, punctuation, etc.)
	 * 
	 * The individual anonymity indices of all words within the sentence are also added to the sum computed above, which is what gets returned
	 * 
	 * @return sentenceAnonymityIndex
	 */
	public double getSentenceAnonymityIndex() {
		double sentenceAnonymityIndex=0;
		double numFeatures = sentenceLevelFeaturesFound.length();
		int i;
		Attribute currentAttrib;
//		int totalFeatureOccurrences = 0;
//		for (i = 0; i < numFeatures; i++)
//			totalFeatureOccurrences += sentenceLevelFeaturesFound.references.get(i).value;
//		if (totalFeatureOccurrences == 0)
//				totalFeatureOccurrences = 1; // can't divide by zero..
		// Compute each "sentence level" feature's contribution to the anonymity index
		for (i=0;i<numFeatures;i++){
			Reference tempFeature = sentenceLevelFeaturesFound.references.get(i);
			currentAttrib = DataAnalyzer.topAttributes[tempFeature.index];
			double value=tempFeature.value;
			sentenceAnonymityIndex += (value)*(currentAttrib.getInfoGain())*(currentAttrib.getPercentChangeNeeded(false, false, false));
		}
		int numWords = wordsInSentence.size();
		// then add the contribution of each individual word
		for(i = 0; i < numWords; i++)
			sentenceAnonymityIndex += wordsInSentence.get(i).getWordAnonymity();
		
		return sentenceAnonymityIndex;
	}

	public ArrayList<TENSE> getTense(){
		return tense;
	}
	public ArrayList<POV> getPov(){
		return pointOfView;
	}
	public ArrayList<CONJ> getConj(){
		return conjugations;
	}

	public void setWordList(){

	}

	/*
	private void addTowordListMap(String str,Word word){
		if(wordListMap.containsKey(str)){
			word.adjustVals(0, wordListMap.get(str).infoGainSum,wordListMap.get(str).percentChangeNeededSum);//check on this
			wordListMap.put(str,word);
		}
		else {
			wordListMap.put(str, word);
		}
	}
	*/

/*	
	
	 * sets the ArrayLists, Tense, Pow, and Conj.
	 * @param tagged
	 
	public void setGrammarStats(){
		//setwordListMap();
		FunctionWord fWord=new FunctionWord();
		MisspelledWords mWord=new MisspelledWords();
		for (int twCount=0;twCount<tagged.size();twCount++){
			TaggedWord temp=tagged.get(twCount);
			//System.out.println(temp.tag());
			if(temp.word().matches("[\\w&&\\D]+")){//fixes the error with sentenceAppend button
				if(fWord.searchListFor(temp.word())){
					//functionWords.add(temp.word());
				}
				else if(mWord.searchListFor(temp.word())){
					misspelledWords.add(temp.word());
				}
				java.util.regex.Matcher wordToSearch=punctuationRegex.matcher(temp.word());
				if(wordToSearch.find()){
					punctuation.add(temp.word().substring(wordToSearch.start(), wordToSearch.end()));
				}
				//adds digits
				wordToSearch=digit.matcher(temp.word());
				if(wordToSearch.find()){
					String digitSubStr=temp.word().substring(wordToSearch.start(), wordToSearch.end());
					for (int count=0;count<digitSubStr.length();count++){
						if(count-2>=0){
							digits.add(digitSubStr.substring(count-2, count));
							digits.add(digitSubStr.substring(count-1, count));
						}
						else if(count-1>=0){
							digits.add(digitSubStr.substring(count-1, count));
						}//not sure if necessary...digits bi/trigrams
						digits.add(digitSubStr.substring(count, count));
					}	
				}	
				wordToSearch=specialCharsRegex.matcher(temp.word());
				if(wordToSearch.find()){
					specialChars.add(temp.word().substring(wordToSearch.start(), wordToSearch.end()));
				}
				
				wordLengths.add(temp.word().length());
				//setHashMap(POS,temp.tag());
				//setHashMap(words,temp.word());
				
				/*if(twCount-2>=0){//addsTrigrams&Bigrams
					setHashMap(POSTrigrams,tagged.get(twCount-2).tag()+tagged.get(twCount-1).tag()+tagged.get(twCount).tag());
					setHashMap(wordTrigrams,tagged.get(twCount-2).word()+tagged.get(twCount-1).word()+tagged.get(twCount).word());
					setHashMap(POSBigrams,tagged.get(twCount-1).tag()+tagged.get(twCount).tag());//I feel that doing it this way with if/elif would speed up code
					setHashMap(wordBigrams,tagged.get(twCount-1).word()+tagged.get(twCount).word());
				}
				else if(twCount-1>=0){//addsBigrams
					setHashMap(POSBigrams,tagged.get(twCount-1).tag()+tagged.get(twCount).tag());
					setHashMap(wordBigrams,tagged.get(twCount-1).word()+tagged.get(twCount).word());
				}
				char[] untaggedWord=temp.word().toLowerCase().toCharArray();
				for(int letterIndex=0;letterIndex<untaggedWord.length;letterIndex++){
					setHashMap(letters,untaggedWord[letterIndex]+"");
					if(letterIndex-2>=0){
						setHashMap(letterBigrams,untaggedWord[letterIndex-1]+untaggedWord[letterIndex]+"");
						setHashMap(letterTrigrams,untaggedWord[letterIndex-2]+untaggedWord[letterIndex-1]+untaggedWord[letterIndex]+"");
					}
					else if(letterIndex-1>=0){
						setHashMap(letterBigrams,untaggedWord[letterIndex-1]+untaggedWord[letterIndex]+"");
					}
				}
				
				
			}	//This somehow overwrite the taggedDocument.
				
				
		}
			
	}
	*/

	/*
	private void setHashMap(HashMap <String,Integer> hashMap, String key){
		if(hashMap.containsKey(key)){
			hashMap.put(key, (hashMap.get(key).intValue()+1));
		}
		else {
			hashMap.put(key, 1);
		}
	}
	*/


	/**
	 * if returnWithEOSSubs
	 * @param returnWithEOSSubs 
	 * @return
	 */
	public String getUntagged() {
		return untagged;
	}

	public String toString(){
		return "[ untagged: <"+untagged+"> ||| tagged: "+wordsInSentence.toString()+" ||| SparseReferences Object: "+getReferences().toString()+" ]";
		//||| tense: "+tense.toString()+" ||| point of view: "+pointOfView.toString()+" conjugation(s): "+conj.toString()+" ]";// ||| functionWords : "+functionWords.toString()+" ]";
	}

	/* TODO: 'tagged' no longer holds tagged words.
	public ArrayList<String> getWordsWithTag(TheTags tag){
		wordsToReturn = new ArrayList<String>(tagged.size());// Can't return more words than were tagged
		tagIter = tagged.iterator();
		while (tagIter.hasNext()){
			taggedWord = tagIter.next();
			System.out.println(taggedWord.value());
			System.out.println(taggedWord.tag());
		}
		return wordsToReturn;
	}
	*/

}

/*Stuff for tenses
if(temp.tag().startsWith("VB")){
	//it is a verb 
	switch(TheTags.valueOf((temp.tag()))){
	case VB: conj.add(CONJ.SIMPLE);//"Verb, base form";
	case VBD: tense.add(TENSE.PAST);
				conj.add(CONJ.SIMPLE); // "Verb, past tense";
	//case "VBG": // "Verb, gerund or present participle";
	//case "VBN": // "Verb, past participle";
	case VBP: tense.add(TENSE.PRESENT);// "Verb, non-3rd person singular present";
	case VBZ: tense.add(TENSE.PRESENT);// "Verb, 3rd person singular present";
	}
}
else if (temp.tag().startsWith("PR")){//this is a pronoun
	String tempWord=temp.word();
	for(int j=0;j<firstPersonPronouns.length;j++){
		if(firstPersonPronouns[j].equalsIgnoreCase(tempWord)){
			if(!pointOfView.contains(POV.FIRST_PERSON))//will not add POVs twice
				pointOfView.add(POV.FIRST_PERSON);
		}
	}
	for(int j=0;j<secondPersonPronouns.length;j++){
		if(secondPersonPronouns[j].equalsIgnoreCase(tempWord)){
			if(!pointOfView.contains(POV.SECOND_PERSON))
				pointOfView.add(POV.SECOND_PERSON);
		}
	}
	for(int j=0;j<thirdPersonPronouns.length;j++){
		if(thirdPersonPronouns[j].equalsIgnoreCase(tempWord)){
			if(!pointOfView.contains(POV.THIRD_PERSON))
				pointOfView.add(POV.THIRD_PERSON);
		}
	}
}
/*else if(temp.word().equalsIgnoreCase("shall")||temp.word().equalsIgnoreCase("will")){
	tense.add(TENSE.FUTURE);
}actually, this is not necessarily true.*/
