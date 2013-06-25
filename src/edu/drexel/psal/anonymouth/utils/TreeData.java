package edu.drexel.psal.anonymouth.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;


/**
 * Stores data relating to a tree structure: how many times it appeared/occurred, the strings it was composed of, and how many times 
 * each of those strings appeared/occurred, and how many unique strings had the same structure.
 * 
 * @author Andrew W.E. McDonald
 *
 */
public class TreeData{
	
	private final String NAME = "( "+this.getClass().getName()+" ) - ";
	protected int numberOfOccurrences = 0;
	protected ArrayList<Sentence> sentences = new ArrayList<Sentence>(20);
	protected String treeStructure;
	protected int numUnique = 0;
	protected int treeDepth;
	
	/**
	 * constructor for TreeData. 
	 * @param treeStructure should be the processed skeleton of a tree (without the 'words', i.e. '(NP (DT ) (NN ))'  )
	 * This is made by the {@link processTree} method 
	 */
	public TreeData(String processedTreeSkeleton){
		this.treeStructure = processedTreeSkeleton;
	}
	
	/**
	 * constructor for TreeData. Accepts processed tree skeleton and the phrase/string of words found within that tree. 
	 * @param processedTreeSkeleton
	 * @param phrase
	 */
	public TreeData(String processedTreeSkeleton, String phrase){
		this.treeStructure = processedTreeSkeleton;
		addOccurrence(phrase);
	}

	
	/**
	 * returns the processed skeleton of the tree who's data is contained by this object.
	 * @return
	 */
	public String getTreeStructure(){
		return treeStructure;
	}
	
	/**
	 * adds an occurrence of a string to the TreeData object. Automatically updates number of occurrences of tree structure
	 * @param s the string to add
	 * @return
	 * 	true 
	 */
	public boolean addOccurrence(String s){
		Sentence newSent = new Sentence(s);
		int index;
		if((index = sentences.indexOf(newSent)) != -1)
			sentences.get(index).increment();
		else{
			sentences.add(newSent);
			numUnique++;
		}
		numberOfOccurrences++;
		return true;
	}

	
	/**
	 * if the sentences in 'als' are already in this TreeData object, their counts will be increased by the count of each respective sentence in 
	 * als. Any sentence that isn't already in the TreeData object will be added to the object's sentences along with their respective counts.
	 * @param als
	 * @return true
	 */
	public boolean addSentences(ArrayList<Sentence> als){
		Iterator<Sentence> sIter = als.iterator();
		int index;
		while(sIter.hasNext()){
			Sentence tempS = sIter.next();
			if((index = sentences.indexOf(tempS)) != -1)
				sentences.get(index).addToCount(tempS.count);
			else{
				sentences.add(tempS);
				numUnique++;
			}
		}
		return true;
	}
	
	/**
	 * @return returns the number of times the tree structure was seen / occurred
	 */
	public int getStructureOccurrences(){
		return numberOfOccurrences;
	}
	
	/**
	 * 
	 * @param s the string to receive an occurrence count for
	 * @return returns the number of occurrences of a specified string, 0 if no occurrences.
	 */
	public int getOccurrencesOfString(String s){
		Sentence newSent = new Sentence(s);
		int index;
		if((index = sentences.indexOf(newSent)) != -1)
			return sentences.get(index).count;
		else
			return 0;
	}
	
	/**
	 * returns an ArrayList with the text of sentences ordered from greatest to least by number of occurrences. 
	 * @return ordered sentences
	 */
	public ArrayList<String>  getOrderedStrings(){
		Sentence[] ordered = new Sentence[sentences.size()];
		ordered = (Sentence[])sentences.toArray();
		Arrays.sort(ordered);
		int max = ordered.length;
		int i = 0;
		ArrayList<String> orderedSentenceText = new ArrayList<String>(max);
		for(i=0;i<max;i++)
			orderedSentenceText.add(ordered[i].text);
		return orderedSentenceText;
	}
	
	/**
	 * returns a single string of the sentence text, ordered by number of occurrences of each string. 
	 * @param escapeChars if 'true', commas will be escaped and newlines will be changed 
	 * @return
	 */
	public String getOrderedStringsAsString(boolean escapeChars){
		Object[] ordered = sentences.toArray();
		int numOrdered = ordered.length;
		Sentence[] orderedSents = new Sentence[numOrdered];
		for(int i =0; i < numOrdered;i++)
			orderedSents[i] = (Sentence) ordered[i];
		Arrays.sort(orderedSents);
		int max = orderedSents.length;
		int i = 0;
		String orderedSentenceText = "";
		for(i=0;i<max;i++)
			orderedSentenceText += orderedSents[i].toString(escapeChars)+",";
		return orderedSentenceText;	
	}
	
	/**
	 * defines two TreeData objects to be equal if they contain the same processed tree skeleton (string). 
	 * @return
	 * 	true if equal
	 */
	public boolean equals(Object obj){
		String otherStruct =((TreeData)obj).treeStructure;
		return treeStructure.equals(otherStruct);
	}
	
	/**
	 * generates a hashcode for TreeData, by calling the String hashCode method on the processed tree skeleton string. Unless instructed to do otherwise 
	 * by the 'setIgnoreCase' method, hashCodes are generated after converting the text string to lowercase.
	 * @return
	 * 	hashcode
	 */
	public int hashCode(){
		return treeStructure.hashCode();
	}
	
	public String toString(){
		return "["+treeStructure+"] (total: "+numberOfOccurrences+") => "+sentences.toString();
	}
		
}