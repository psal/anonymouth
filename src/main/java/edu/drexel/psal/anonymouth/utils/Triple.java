package edu.drexel.psal.anonymouth.utils;

import edu.drexel.psal.anonymouth.engine.FeatureList;

/**
 *	Holds: string in braces, infogain, and featureName  
 * @author Andrew W.E. McDonald
 * @author Joe Muoio
 *
 */
public class Triple {
	
	private final String NAME = "( "+this.getClass().getName()+" ) - ";

	protected String stringInBraces;
	protected double percentChangeNeeded;
	protected FeatureList featureName;
	protected double infoGain;
	
	public Triple(String stringInBraces, double tempPercentChange, double infoGain){
		this.stringInBraces = stringInBraces;
		this.percentChangeNeeded = tempPercentChange;
	//	this.featureName=tempPercentChange;
		this.infoGain = infoGain;
	}
	
	public FeatureList getFeatureName(){
		return featureName;
	}
	public double getInfoGain(){
		return infoGain;
	}
	public String getStringInBraces(){
		return stringInBraces;
	}

	
	public String toString(){
		String str="SIB: "+stringInBraces+" %Change: "+percentChangeNeeded+" infoGain: "+infoGain;
		return str;
	}
	
}
