package edu.drexel.psal.anonymouth.utils;

import java.io.Serializable;
import java.util.ArrayList;

import edu.drexel.psal.jstylo.generics.Logger;

/**
 * Tracks all EOS characters in the document to anonymize. This includes ALL
 * instances of '.', '!', and '?' in a document, so it's not restricted to
 * just sentence ends but rather any time those appear (so abbreviations,
 * ellipses, etc. all are kept here as well). We use this to determine which
 * EOS characters are "real". Instead of using swap characters we are instead
 * opting to preserve the original text for the tagged sentences and backend
 * and instead keep an "ignore" boolean tied to each index of an EOS
 * character, and SentenceMaker will then skip over these since they aren't
 * actually and EOS.
 *
 * @author  Marc Barrowclift
 * @author  Andrew W.E. McDonald
 */
public class EOSTracker implements Serializable {
	
	public final String NAME = "( " + this.getClass().getSimpleName() +" ) - ";
	private static final long serialVersionUID = -5900337779583604917L;

	/**
	 * The characters we acknowledge as possible was to end a sentence
	 */
	public final char[] EOS = {'.', '!', '?'};
	/**
	 * Our array list of EOS character objects
	 */
	private ArrayList<EOS> eoses;
	/**
	 * The number of EOSes we are currently tracking
	 */
	private int size;

	/**
	 * Constructor
	 */
	public EOSTracker() {
		eoses = new ArrayList<EOS>(100);
	}
	
	/**
	 * Constructor, takes another instance of EOSTracker and does a deep copy
	 * of it's contents.
	 * 
	 * @param eosCT
	 *        EOSTracker instance
	 */
	public EOSTracker( EOSTracker eosTracker) {
		int tempSize = eosTracker.eoses.size();
		eoses = new ArrayList<EOS>(size);

		for (int i = 0; i < tempSize; i++) {
			if (eosTracker.eoses.get(i) == null) {
				return;
			}
			size++;
			eoses.add(new EOS(eosTracker.eoses.get(i)));
		}
	}

	/**
	 * Checks if a given character could be used to end a sentence.
	 * 
	 * @param  unknownChar
	 *      The character you want to test
	 *         
	 * @return
	 * 		True or false, depending on whether or not it's an end of sentence character
	 */
	public boolean isEOS(char unknownChar) {
		boolean result = false;
		
		if (unknownChar == EOS[0] ||
				unknownChar== EOS[1] ||
				unknownChar == EOS[2]) {
			result = true;
		}
		
		return result;
	}
	
	/**
	 * Adds the EOS character to the tracker, this should be called EVERY TIME
	 * a new EOS character is typed, regardless of whether or not it's part of
	 * an abbreviation, an end of a sentence, etc.<br><br>
	 *
	 * The default ignore value for every newly added EOS is true since we never
	 * know at first, EditorDriver's insert() method called from the caret event
	 * handles all of this.
	 * 
	 * @param eosChar
	 *        The EOS character you want to add to the tracker
	 * @param location 
	 *        The index of the EOS character you are adding
	 * @param ignore
	 * 		  Whether or not to skip the EOS character as an actual sentence end.
	 */
	public void addEOS(char eosChar, int location, boolean ignore) {
		eoses.add(new EOS(eosChar, location, ignore));
		size++;
	}
	
	/**
	 * Obtains the EOS character at the index given and sets whether or not to ignore it.
	 * @param index - The index of the EOS character to ignore/not ignore
	 * @param b - whether or not to ignore the EOS character
	 */
	/**
	 * Obtains the EOS character at the given index and sets whether or not to ignore it.
	 * 
	 * @param index
	 *        The index of the EOS character you want to edit
	 * @param ignore
	 *        Whether or not the EOS character represents the end of a sentence
	 *
	 * @return
	 * 		Whether or not an EOS character was found at the given index
	 */
	public boolean setIgnore(int index, boolean ignore) {
		boolean found = false;
		
		for (int i = 0; i < size; i++) {
			if (index == eoses.get(i).location) {
				Logger.logln(NAME+"Now recognizing EOS character at " + index);
				eoses.get(i).ignore = ignore;
				found = true;
				break;
			}
		}

		return found;
	}

	/**
	 * Used for debugging purposes, prints out all EOSes,
	 * their respective locations, and whether or not we're ignoring them.
	 */
	public void printEOSes() {
		for (int i = 0; i < size; i++)
			Logger.logln(NAME + "   " + eoses.get(i));
	}
	
	/**
	 * Checks the given index for an EOS character and returns true if it finds one and it is not set to be ignored.
	 * @param index - The index where we want to check if there is an EOS character present
	 * @return result - whether or not an EOS character that is NOT set to be ignored exists in the index given.
	 */
	/**
	 * Checks whether or not a sentence ends at the given index (meaning we
	 * are checking to see if an EOS character exists in our tracker at the
	 * given index AND if it's not being ignored).
	 * 
	 * @param  index 
	 *         The index you want to check for a sentence end.
	 *         
	 * @return
	 * 		Whether or not a sentence ends at the given index
	 */
	public boolean sentenceEndAtIndex(int index) {
		boolean result = false;

		/**
		 * For first processing (TODO and possibly reprocessing, if this is
		 * the case then this check will only work for the first process), we
		 * don't want the code below to run and check everything, just return
		 * true every time. (Just trust me, it works).
		 */
		if (size == 0) {
			result = true;
		} else {
			for (int i = 0; i < size; i++) {
				if (index == eoses.get(i).location) {
					if (!eoses.get(i).ignore) {
						result = true;
					}
					break;
				}
			}
		}

		return result;
	}
	
	/**
	 * Removes any EOS objects in between the given indices as [5, 10),
	 * meaning inclusive for the first and exclusive for the last.
	 * 
	 * @param  lowerBound
	 *         The beginning of the range you want to remove from (includes this position)
	 * @param  upperBound
	 *         The end of the range you want to remove from (does not include this position)
	 *         
	 * @return
	 * 		Whether or not any EOSes were removed from the given range
	 */
	public boolean removeEOSesInRange(int lowerBound, int upperBound) {
		Logger.logln(NAME+"Removing EOSes in range " + lowerBound + " - " + upperBound);
		int location;
		boolean removed = false;

		for (int i = 0; i < size; i++) {
			location = eoses.get(i).location;
			if (location >= lowerBound && location < upperBound) {
				Logger.logln(NAME+"EOS removed at " + location);
				eoses.remove(i);
				i--; // decrement 'i' so that we don't miss the object that shifts down into the spot just freed.
				size--; // also decrement size so that 
				removed = true;
			}
		}

		return removed;		
	}
	
	/**
	 * Shifts all EOS object indices by the given amount left from the given index
	 * 
	 * @param index
	 *        The index past which you want all EOS objects tracked to be shifted
	 * @param shiftAmount
	 *        The amount of spaces you want to shift them by (negative if you want left)
	 */
	public void shiftAll(int index, int shiftAmount) {
		Logger.logln(NAME+"Shifting all EOS character from " + index + " by " + shiftAmount);

		for (int i = 0; i < size; i++) {
			EOS eos = eoses.get(i);
			if (eos.location >= index)
				eoses.get(i).location += shiftAmount;
		}
	}
	
	/**
	 * clears all EOS characters so we can recalculate them and reset the backend
	 */
	public void resetEOSCharacters() {
		eoses.clear();
	}
	
	/**
	 * Returns a string representation of this EOSTracker
	 */
	public String toString() {
		String toReturn = "[ ";

		for (int i = 0; i < size; i++) {
			toReturn += eoses.get(i).toString() + ", ";
		}

		toReturn = toReturn.substring(0, toReturn.length()-1) + "]";

		return toReturn;
	}
}

/**
 * Holds the EOS characters at a given location within the document with
 * respect to the first index of the document (0).
 *
 * @author  Andrew W.E. McDonald
 */
class EOS implements Serializable {

	private static final long serialVersionUID = -3147071940148952343L;
	
	protected char eos;
	protected int location;
	protected boolean ignore;
	
	/**
	 * Constructor
	 * @param eos
	 *        The EOS you want to track
	 * @param location
	 *        The location of the EOS you want to track
	 */
	public EOS( char eos, int location, boolean ignore) {
		this.eos = eos;
		this.location = location;
		this.ignore = ignore;
	}
	
	/**
	 * Constructor
	 * @param  eosObj
	 *         An EOS instance
	 */
	public EOS( EOS eosObj) {
		this.eos = eosObj.eos;
		this.location = eosObj.location;
		this.ignore = eosObj.ignore;
	}

	/**
	 * Our custom toString() method to make printing these
	 * things easier for debugging
	 * 
	 * @return
	 * 		Our modified string representation of the EOS character
	 */
	@Override
	public String toString() {
		return "[ "+eos+" : "+location+" ]";
	}
}	