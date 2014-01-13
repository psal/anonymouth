package edu.drexel.psal.anonymouth.utils;

/**
 * class for TargetExtractor that holds (document title, feature value) pairs to allow the clustered features to facilitate picking target clusters for each feature,
 * based on document. This way, when all features have been clustered, it is possible to easily see where the features of each document are going; which solves the 
 * problem of picking an unrealistic group of target clusters (e.g. the user won't be told to set a high avg. sentence length while also increasing sentence count). 
 * @author Andrew W.E. McDonald
 *
 */
public class Pair {
	
	private final String NAME = "( "+this.getClass().getName()+" ) - ";
	public final String doc;
	public final double value;
	
	/**
	 * Constructor for Pair class
	 * @param doc document title
	 * @param value feature value being clustered
	 */
	public Pair(String doc, double value){
		this.doc = doc;
		this.value = value;
	}
	
	/**
	 * returns a string displaying the pair: [doc,value]
	 * @return
	 * 	string representing contents of Pair
	 */
	public String toString(){
		return "["+doc+" , "+value+"]";
	}
}
