package edu.drexel.psal.anonymouth.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Scanner;

import edu.drexel.psal.jstylo.generics.Logger;
import edu.drexel.psal.jstylo.generics.Logger.LogOut;


/**
 * A way to keep track of indices and values in an array
 * @author Andrew W.E. McDonald
 *
 */
public class SparseReferences implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2656940525259833825L;
	private final String NAME = "( "+this.getClass().getName()+" ) - ";
	protected ArrayList<Reference> references;
	
	
	/**
	 * constructor. 
	 * @param initialSizeOfReferenceArrayList
	 */
	public SparseReferences(int initialSizeOfReferenceArrayList){
		references = new ArrayList<Reference>(initialSizeOfReferenceArrayList);
	}
	
	/**
	 * Constructor for SparseReferences. Essentially does a deep copy of the input SparseReferences
	 * @param sr
	 */
	public SparseReferences(SparseReferences sr){
		references = new ArrayList<Reference>(sr.length());
		for(Reference r: sr.references)
			this.references.add(new Reference(r));
	}
	
	
	/**
	 * inserts 'value' at 'index' provided that index is within the range [0,sizeOfArray). if not, returns false. 
	 * NOTE: 'put' does not preserve data at location 'index'. Any value in position 'index' will be overwritten with 'value'
	 * @param index
	 * @param value
	 * @return
	 */
	public boolean addNewReference(int index,int value){
		if(index >= 0){
			Reference r = new Reference(index, value);
			if(references.contains(r)){
				Logger.logln(NAME+"Cannot add duplicate references, addNewReference failed.",LogOut.STDERR);
				return false;
			}
			references.add(r);
			return true;
		}
		Logger.logln(NAME+"Cannot add Reference with 'index' less than zero",LogOut.STDERR);
		return false;
	}
	
	/**
	 * Adds a Reference to the ArrayList of Reference objects, provided that the list does not already contain a Reference to the attribute that 
	 * is being referenced by the Reference reference, and that the input reference has an index (the attribute's index) greater than or equal to zero.
	 * @param reference
	 * @return
	 */
	public boolean addNewReference(Reference reference){
		if(reference.index >= 0){
			if(references.contains(reference)){
				Logger.logln(NAME+"Cannot add duplicate references, addNewReference failed.",LogOut.STDERR);
				return false;
			}
			references.add(new Reference(reference.index,reference.value));
			return true;
		}
		Logger.logln(NAME+"Cannot add Reference with 'index' less than zero",LogOut.STDERR);
		return false;
	}
	
	
	/**
	 * Merges the argument into this instance of SparseReferences
	 * @param notThis
	 * @return
	 */
	public void merge(SparseReferences notThis){
		for(Reference notThisEither:notThis.references){
			if(references.contains(notThisEither)){
				int thisIndex = references.indexOf(notThisEither);
				references.add(thisIndex,references.remove(thisIndex).merge(notThisEither));
			}
			else{
				references.add(notThisEither);
			}
		}
	}
	
	
	
	
	
	/**
	 * Subtracts the "argument" (right) SparseReferences' references from the "calling" (left) SparseReferences' references, i.e.:
	 * theLeftOne.leftMinusRight(theRightOne)
	 * 
	 * the newest version should call the function, and the old version should be passed in => new (minus) old => postive number if a feature's values increased, 
	 * and negative if they decreased. the resulting output SparseReferences object is used to update the Attribute values corresponding to the index held in each Reference.
	 * @param sia
	 * @return
	 */
	public SparseReferences leftMinusRight(SparseReferences sia){
		//sia=references of old sentence
		double tempValue;
		int tempIndex;
		int indexOfRef;
		SparseReferences adjustmentReferences = new SparseReferences((sia.references.size()+this.references.size())); // absolute max size
		Reference newRef;
		ArrayList<Reference> cloneOfThis = (ArrayList<Reference>) this.references.clone();

		for(Reference r: sia.references){
			if(cloneOfThis.contains(r)){
				indexOfRef = cloneOfThis.indexOf(r);
				tempValue = cloneOfThis.get(indexOfRef).value - r.value;
				tempIndex = r.index;
				newRef = new Reference(tempIndex,tempValue);
				cloneOfThis.remove(indexOfRef);
				//Logger.logln(NAME+"");
			}
			else{// There are zero appearances of the feature in the new SparseReferences, so just multiply the number found in the old SparseReferences by -1 (all were removed)
				newRef = new Reference(r.index,(-r.value));
				//Logger.logln(NAME+"Reference not in both lists");
			}
			//Logger.logln(NAME+"Left Minus Right addNewRef");
			adjustmentReferences.addNewReference(newRef);
		}
		if(cloneOfThis.isEmpty() == false){ //there are still values in the clone which means new attributes / features were added. These all went from a count of zero to whatever their value is now. So, positive change
			for(Reference r:cloneOfThis){
				newRef = new Reference(r.index,r.value);
				//Logger.logln(NAME+"Left Minus Right addNewRef new features added");
				adjustmentReferences.addNewReference(newRef);
			}
		}
		return adjustmentReferences; 
	}
	
	
	
	
	/**
	 * returns string representation of contained index and integer array in the form of [ index => [ value0, value1, ..., valuen]]
	 */
	public String toString(){
		String whatIsInside = "[";
		int i=0;
		int numRefs = references.size();
		for(i=0;i<numRefs;i++){
			whatIsInside += references.get(i).toString();
			if(i<numRefs-1) whatIsInside+= ",";
		}	
		whatIsInside +="]";
		return whatIsInside;
	}
	
	/**
	 * defines two sparse reference objects to be equal if they contain the same reference objects.
	 * @return
	 * 	true if equal
	 */
	public boolean equals(Object obj){
		boolean isEqual=false;
		int i,refSize=references.size();
		for(i=0;i<refSize;i++){
			if(!((SparseReferences)obj).references.contains(references.get(i)))
				return false;
		}
		return true;
	}
	
	/*
	 * generates a hashcode for Word, modulus 987643211 (an arbitrary large prime number) to mitigate risk of integer overflow. Multiplier is 31,
	 * hash value starts at 7, and iteratively multiplies itself by the product of all preceding characters in 'word'.
	 * @return
	 * 	hashcode
	 
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
	}*/
	/**
	 * returns the number of stored References 
	 * @return
	 */
	public int length(){
		return references.size();
	}
	
	
	
	
	/*
	public static void main(String[] args){
	
	
	}
	*/
	
}

