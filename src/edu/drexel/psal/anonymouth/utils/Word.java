package edu.drexel.psal.anonymouth.utils;

import java.io.Serializable;
import java.util.ArrayList;

import edu.drexel.psal.anonymouth.engine.Attribute;
import edu.drexel.psal.anonymouth.engine.DataAnalyzer;
import edu.drexel.psal.anonymouth.utils.POS.TheTags;
import edu.drexel.psal.jstylo.generics.Logger;
import edu.drexel.psal.jstylo.generics.Logger.LogOut;

/**
 * Holds a 'word' as a String, and retains its rank and the collective information gain of all features that have been found within the 'word'
 * Can also hold two or three 'words' in 'word' field. 
 * @author Andrew W.E. McDonald
 *
 */
public class Word implements Comparable<Word>, Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2134159624348701323L;

	private final String NAME = "( "+this.getClass().getName()+" ) - ";

	protected String word;
	protected ArrayList<String>partOfSpeech;
	protected SparseReferences wordLevelFeaturesFound; 
	
	/**
	 * Constructor for Word
	 * @param word the word to construct a Word for
	 */
	public Word(String word){
		wordLevelFeaturesFound = new SparseReferences(10);// probably won't find > 10 features in a word (wild guess)
		partOfSpeech = new ArrayList<String>(); // is an array list because it is possible to have one word as more than one part of speech. It doesn't seem to make sense at this point to count them as different words.
		this.word = word;
	}
	
	/**
	 * Constructor for Word. Essentially does a deep copy of the input word.
	 * @param word
	 */
	public Word(Word word){
		this.word = word.word;
		wordLevelFeaturesFound = new SparseReferences(word.wordLevelFeaturesFound);
		partOfSpeech = word.partOfSpeech;
		
	}
	
	/**
	 * Constructor for Word
	 * @param word the Word to construct a Word for
	 * @param POS the part of speech of the word
	 */
	public Word(String word, String POS){
		wordLevelFeaturesFound = new SparseReferences(10);// probably won't find > 10 features in a word (wild guess)
		partOfSpeech = new ArrayList<String>(); // is an array list because it is possible to have one word as more than one part of speech. It doesn't seem to make sense at this point to count them as different words.
		this.word = word;
		partOfSpeech.add(POS);
	}

	public void addToPOSList(String string){
		partOfSpeech.add(string);
	}
	
	/**
	 * Computes the AnonymityIndex of this Word: SUM ((#appearancesOfFeature[i]/numFeaturesFoundInWord)*(infoGainOfFeature[i])*(%changeNeededOfFeature[i])). These numbers are
	 * determined by the percent change needed and information gain for each feature found in the "Word" from each feature's respective Attribute object in the static Attribute array 'topAttributes'
	 * 
	 * @return anonymityIndex
	 */
	public double getAnonymityIndex(){
		double anonymityIndex=0;
		double numFeatures = wordLevelFeaturesFound.length();
		int i;
		Attribute currentAttrib;
		for (i = 0;i<numFeatures;i++){
			Reference tempFeature = wordLevelFeaturesFound.references.get(i);
			currentAttrib = DataAnalyzer.topAttributes[tempFeature.index];

			double value=tempFeature.value;
			anonymityIndex += (value)*(currentAttrib.getInfoGain())*(currentAttrib.getChangeNeeded());
		}
		return anonymityIndex;
	}

	/**
	 * Slightly modified version of getAnonymityIndex() such that the call to getPercentChangeNeeded() inside will return negative values
	 * as well. This is used for us to better gauge what words should be removed.
	 * @return
	 */
	public double getWordAnonymity() {
		double anonymityIndex=0;
		double numFeatures = wordLevelFeaturesFound.length();
		int i;
		Attribute currentAttrib;
		for (i = 0;i<numFeatures;i++){
			Reference tempFeature = wordLevelFeaturesFound.references.get(i);
			currentAttrib = DataAnalyzer.topAttributes[tempFeature.index];

			double value=tempFeature.value;
			anonymityIndex += (value)*(currentAttrib.getInfoGain())*(currentAttrib.getPercentChangeNeeded(false,false,false));// for 'getPercentChangeNeeded', the first boolean says not to normalize the result to the baslinePercentChangeNeeded, the second says to invert the percentage, and the third says to take the absolute value (ignore direction of change)
		}
		return anonymityIndex;
	}
	
	/**
	 * Adds another feature to the SparseReferences instance
	 * @param ref
	 * @return
	 */
	public boolean addFoundFeature(Reference ref){
		return wordLevelFeaturesFound.addNewReference(ref);
	}
	
	public String getUntagged(){
		return word;
	}
	
	/**
	 * Merges two words, provided that the 'word' (string) inside are equivalent (case sensitive), and that both 'word' strings have been determined to be of 
	 * the same part of speech.
	 * @param newWord 
	 */
	public void mergeWords(Word newWord){
		if(newWord.equals(this)){
			this.wordLevelFeaturesFound.merge(newWord.wordLevelFeaturesFound);
			this.partOfSpeech.addAll(newWord.partOfSpeech);
		}
		else
			Logger.logln(NAME+"Cannot merge inequivalent  Words!",LogOut.STDERR);
	}
	
		
	/**
	 * defines two Word objects to be equal if they contain the same 'word' String object.
	 * @return
	 * 	true if equal
	 */
	public boolean equals(Object obj){
			if(word.equals(((Word)obj).word))
					return true;
				else 
					return false;
	}
	
	/**
	 * generates a hashcode for Word, modulus 987643211 (an arbitrary large prime number) to mitigate risk of integer overflow. Multiplier is 31,
	 * hash value starts at 7, and iteratively multiplies itself by the product of all preceding characters in 'word'.
	 * @return
	 * 	hashcode
	 */
	public int hashCode(){
		final int thePrime = 31;
		final int arbitraryLargePrime = 987643211;
		long longHash = 7;
		int i = 0;
		if(word != null){
			char[] theWord = word.toCharArray();
			int len = theWord.length;
			for(i=0; i < len; i++){
				longHash = longHash*theWord[i]*thePrime;
				longHash = longHash % arbitraryLargePrime;// to eliminate wrap-around / overflow
			}
		}
		int hash = (int)longHash;
		return hash;
	}
	
	
	/**
	 * returns the size of the word in this Word
	 * @return
	 */
	public int size(){
		return word.length();
	}
	

	/**
	 * toString method
	 */
	public String toString(){
		return "[ WORD: "+word+" ||| Anonymity Index: "+getAnonymityIndex()+" ||| wordLevelFeaturesFound: "+wordLevelFeaturesFound+"]";
	}

	/**
	 * compares Words based upon Anonymity Index
	 */
	public int compareTo(Word notThisWord) {
		double thisAnonIndex = this.getAnonymityIndex();
		double thatAnonIndex = notThisWord.getAnonymityIndex();
		if(thisAnonIndex< thatAnonIndex)
			return -1;
		else if (thisAnonIndex == thatAnonIndex)
			return 0;
		else
			return 1;
	}
	
	
}
