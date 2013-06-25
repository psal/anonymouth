package edu.drexel.psal.anonymouth.utils;

import java.io.Serializable;

import edu.drexel.psal.anonymouth.engine.DataAnalyzer;
import edu.drexel.psal.jstylo.generics.Logger;
import edu.drexel.psal.jstylo.generics.Logger.LogOut;


/**
 * Holds an index that corresponds to an array, and a single value. The index is an attribute/feature's position in the Attribute[], and the value 
 * holds the number of times that feature appears (in a Word, TaggedSentence, etc.)
 * @author Andrew W.E. McDonald
 *
 */
public class Reference implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5842100664246784998L;
	private final String NAME = "( "+this.getClass().getName()+" ) - ";
	protected int index;
	protected double value;
	
	/**
	 * Constructor
	 * @param index
	 * @param value
	 */
	public Reference(int index, double value){
		this.index = index;
		this.value = value;
	}
	
	/**
	 * Constructor
	 * @param r
	 */
	public Reference(Reference r){
		this.index = r.index;
		this.value = r.value;
	}
	
	/**
	 * Merges two Reference objects if they have matching 'index' values
	 * @param ref
	 * @return
	 */
	public Reference merge(Reference ref){
		if(this.index == ref.index){
			double newValue = this.value + ref.value;
			Reference mergedRef = new Reference(this.index,newValue);
			return mergedRef;
		}
		else{
			Logger.logln(NAME+"Cannot merge two References with different index values",LogOut.STDERR);
			return null;
		}
		
	}
	
	/**
	 * Equals method to set two References equal to eachother if they hold the same 'index' (i.e. if they 'point' to the same attribute)
	 */
	public boolean equals(Object o){
		//Logger.logln(NAME+"Using the Reference equals.");
		if(index == ((Reference)o).index)
			return true;
		else
			return false;
	}
	
		
	/**
	 * generates a hashcode for Reference, modulus 987643211 (an arbitrary large prime number) to mitigate risk of integer overflow. Multiplier is 31,
	 * hash value starts at 7, and multiplies itself by 'index', and then multiplies itself by 'value' and adds that to the previous product; at which point, the modulus is taken.
	 * @return
	 * 	hashcode
	 */
	public int hashCode(){
		Logger.logln(NAME+"This is the hashcode being called",LogOut.STDERR);
		final int thePrime = 31;
		final int arbitraryLargePrime = 987643211;
		long longHash = 7;
		longHash =(longHash *thePrime * index ) % arbitraryLargePrime;// to eliminate wrap-around
		longHash = (long)((value *longHash)+longHash) % arbitraryLargePrime;
		int hash = (int)longHash;
		return hash;
	}
	
	/**
	 * returns a string: "[index => value]"
	 */
	public String toString(){
		return "["+index+" => "+value+" percentChange: "+DataAnalyzer.topAttributes[index].getPercentChangeNeeded(false,false,false)+"]";
	}
	
}