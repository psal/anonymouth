package edu.drexel.psal.anonymouth.utils;

import edu.drexel.psal.jstylo.generics.Logger;
import edu.drexel.psal.jstylo.generics.Logger.LogOut;

/**
 * Wrapper for int[]. Allows using integer arrays as keys in HashMap, and does other generally intelligent things. 
 * @author Andrew W.E. McDonald
 *
 */
public class SmartIntegerArray{
	
	private final String NAME = "( "+this.getClass().getName()+" ) - ";
	private int[] numbers;
	private int len;
	
	/**
	 * constructor
	 * @param numbers the integer array to wrap
	 */
	public SmartIntegerArray(int[] numbers){
		this.numbers = numbers;
		this.len = numbers.length;
	}
	
	/**
	 * constructor, initializes an empty SmartIntegerArray of size 'size'
	 * @param size
	 */
	public SmartIntegerArray(int size){
		numbers = new int[size];
		len = numbers.length;
	}
	
	/**
	 * inserts 'value' at 'index' provided that index is within the range [0,sizeOfArray). if not, returns false. 
	 * NOTE: 'put' does not preserve data at location 'index'. Any value in position 'index' will be overwritten with 'value'
	 * @param index
	 * @param value
	 * @return
	 */
	public boolean put(int index,int value){
		if(index >= 0 && index < len){
			numbers[index] = value;
			return true;
		}
		Logger.logln(NAME+"Cannot 'put' value into SmartIntegerArray because 'index' out of range",LogOut.STDERR);
		return false;
	}
	
	/**
	 * returns the value at the specified index
	 * @param index
	 * @return
	 */
	public int get(int index){
		return numbers[index];
	}
	
	/**
	 * Subtracts the "argument" (right) SmartIntegerArray's array values from the "calling" (left) SmartInteger array, i.e.:
	 * theLeftOne.leftMinusRight(theRightOne), provided that their lengths are both the same. returns a new SmartIntegerArray with the difference,
	 * (done on a per index basis)
	 * @param sia
	 * @return
	 */
	public SmartIntegerArray leftMinusRight(SmartIntegerArray sia){
		if(this.len != sia.len){
			Logger.logln(NAME+"Cannot subtract SmartIntegerArrays of different length!",LogOut.STDERR);
			return null;
		}
		int i = 0;
		int[] newRay = new int[this.len];
		for(i = 0; i < this.len; i++){
			newRay[i] = this.numbers[i] - sia.numbers[i]; //NOTE: This is the SmartIntegerArray on the LEFT MINUS the SmartIntegerArray on the RIGHT,
			// because the function call would look like: theLeftOne.leftMinusRight(theRightOne)
		}
		return new SmartIntegerArray(newRay);
	}
	
	/**
	 * defines two SmartIntegerArray objects to be equal if they are the same length, and contain identical values in identical positions.
	 * @return
	 * 	true if equal
	 */
	public boolean equals(Object obj){
		int i = 0;
		boolean isSame = true;
		int otherLen =((SmartIntegerArray)obj).numbers.length;
		if(len != otherLen)
			isSame = false;
		else{
			for(i=0;i<len;i++){
				if(this.numbers[i] != ((SmartIntegerArray)obj).numbers[i]){
					isSame = false;
					break;
				}
			}
		}
		return isSame;
	}
	
	/**
	 * generates a hashcode for SmartIntegerArray, modulus 987643211 (an arbitrary large prime number) to mitigate risk of integer overflow. Multiplier is 31,
	 * hash value starts at 7, and multiplies itself by the product of 'numbers[i]'.
	 * @return
	 * 	hashcode
	 */
	public int hashCode(){
		final int thePrime = 31;
		final int arbitraryLargePrime = 987643211;
		long longHash = 7;
		int i = 0;
		if(numbers != null){
			for(i=0; i < len; i++){
				longHash = longHash*numbers[i]*thePrime;
				longHash = longHash % arbitraryLargePrime;// to eliminate wrap-around
			}
		}
		int hash = (int)longHash;
		return hash;
	}
	
	
	/**
	 * returns string representation of contained integer array
	 */
	public String toString(){
		String whatIsInside = "[";
		int i=0;
		for(i=0;i<len;i++){
			whatIsInside += numbers[i];
			if(i<len-1) whatIsInside+= ", ";
		}	
		whatIsInside +="]";
		return whatIsInside;
	}
	
	/**
	 * returns original input int[] 
	 * @return
	 */
	public int[] toIntArray(){
		return numbers;
	}
	
	/**
	 * returns the length of the stored int[]
	 * @return
	 */
	public int length(){
		return numbers.length;
	}
//	
//	public int[] getFreq(int ){
//		int i=0;
//		int tempMin;
//		int minVal = 1000000; // arbitrary large initial value
//		for(i=0;i<len;i++){
//			tempMin = numbers[i];
//			if(tempMin < minVal
//		}
//		return minVal;
//		
//	}
	/*
	public static void main(String[] args){
		int[] one = new int[]{1,2,3,4,5};
		int[] two = new int[]{1,2,3,4,5};
		SmartIntegerArray sia = new SmartIntegerArray(one);
		System.out.println(sia);
		SmartIntegerArray  siat = new SmartIntegerArray(two);
		System.out.println(siat.equals(sia));
		System.out.println(one.equals(two));
	}
	*/
	
}