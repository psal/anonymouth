package edu.drexel.psal.anonymouth.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;

import edu.drexel.psal.anonymouth.gooie.GUIMain;
import edu.drexel.psal.anonymouth.utils.TextWrapper;
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
 * actually and EOS.<br><br>
 *
 * Now also tracks Quotes and Parenthesis both in the similar way of tracking
 * their start and end (if exists) indices.
 *
 * @author  Andrew W.E. McDonald
 * @author  Marc Barrowclift
 */
public class SpecialCharTracker implements Serializable {
	
	public final String NAME = "( " + this.getClass().getSimpleName() +" ) - ";
	private static final long serialVersionUID = -5900337779583604917L;
	
	private GUIMain main;

	/**
	 * The characters we acknowledge as possible was to end a sentence
	 */
	private final HashSet<Character> EOS;
	/**
	 * The character we acknowledge as a quote
	 */
	private final char QUOTE = '"';
	/**
	 * The characters we acknowledge as parenthesis
	 */
	private final HashSet<Character> PARENTHESIS;
	/**
	 * Our array list of EOS character objects
	 */
	private ArrayList<EOS> eoses;
	/**
	 * Our array list of quotes
	 */
	public ArrayList<TextWrapper> quotes;
	/**
	 * Our array list of parenthesis
	 */
	private ArrayList<TextWrapper> parenthesis;
	/**
	 * The number of EOSes we are currently tracking
	 */
	protected int eosSize;
	/**
	 * The number of quotes we're currently tracking
	 */
	public int quoteSize;
	/**
	 * The number of parenthesis we're currently tracking
	 */
	protected int parenthesisSize;
	/**
	 * Whether or not the user is typing in between a text wrapper or not. We
	 * have this in addition to the calculating method because we don't want
	 * to have to check all indices every single document change (costly),
	 * especially since we know if the user was already proven to be typing
	 * inbetween a wrapper there's no need to reCalculate it every time since
	 * we know that until they move away or type a closer character they're
	 * still in it, which is what this flag represents
	 */
	public int[] indicesAlreadyAdjusted;

	/**
	 * Constructor
	 */
	public SpecialCharTracker(GUIMain main) {
		this.main = main;
		EOS = new HashSet<Character>(3);
		EOS.add('.');
		EOS.add('!');
		EOS.add('?');

		PARENTHESIS = new HashSet<Character>(2);
		PARENTHESIS.add('(');
		PARENTHESIS.add(')');
		PARENTHESIS.add('[');
		PARENTHESIS.add(']');
		PARENTHESIS.add('{');
		PARENTHESIS.add('}');
		
		eoses = new ArrayList<EOS>(100);
		quotes = new ArrayList<TextWrapper>(50);
		parenthesis = new ArrayList<TextWrapper>(50);

		eosSize = 0;
		quoteSize = 0;
		parenthesisSize = 0;
		indicesAlreadyAdjusted = null;
	}
	
	/**
	 * Constructor, takes another instance of SpecialCharTracker and does a deep copy
	 * of it's contents.
	 * 
	 * @param eosCT
	 *        SpecialCharTracker instance
	 */
	public SpecialCharTracker(SpecialCharTracker specialCharTracker) {
		EOS = new HashSet<Character>(3);
		EOS.add('.');
		EOS.add('!');
		EOS.add('?');

		PARENTHESIS = new HashSet<Character>(2);
		PARENTHESIS.add('(');
		PARENTHESIS.add(')');
		PARENTHESIS.add('[');
		PARENTHESIS.add(']');
		PARENTHESIS.add('{');
		PARENTHESIS.add('}');
		
		int tempSize = specialCharTracker.eosSize;
		eoses = new ArrayList<EOS>(tempSize);
		eosSize = 0;
		for (int i = 0; i < tempSize; i++) {
			if (specialCharTracker.eoses.get(i) == null) {
				return;
			}
			eosSize++;
			eoses.add(new EOS(specialCharTracker.eoses.get(i)));
		}

		tempSize = specialCharTracker.quoteSize;
		quotes = new ArrayList<TextWrapper>(tempSize);
		quoteSize = 0;
		for (int i = 0; i < tempSize; i++) {
			if (specialCharTracker.quotes.get(i) == null) {
				return;
			}
			quoteSize++;
			quotes.add(new TextWrapper(specialCharTracker.quotes.get(i)));
		}

		tempSize = specialCharTracker.parenthesisSize;
		parenthesis = new ArrayList<TextWrapper>(tempSize);
		parenthesisSize= 0;
		for (int i = 0; i < tempSize; i++) {
			if (specialCharTracker.parenthesis.get(i) == null) {
				return;
			}
			parenthesisSize++;
			parenthesis.add(new TextWrapper(specialCharTracker.parenthesis.get(i)));
		}

		indicesAlreadyAdjusted = specialCharTracker.indicesAlreadyAdjusted;
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

		//EOS characters
		for (int i = 0; i < eosSize; i++) {
			if (eoses.get(i).location >= index)
				eoses.get(i).location += shiftAmount;
		}

		//Quotes
		for (int i = 0; i < quoteSize; i++) {
			if (quotes.get(i).startIndex >= index)
				quotes.get(i).startIndex += shiftAmount;
			if (quotes.get(i).closed == true && quotes.get(i).endIndex >= index)
				quotes.get(i).endIndex += shiftAmount;
		}

		//Parenthesis
		for (int i = 0; i < parenthesisSize; i++) {
			if (parenthesis.get(i).startIndex >= index)
				parenthesis.get(i).startIndex += shiftAmount;
			if (parenthesis.get(i).closed == true && parenthesis.get(i).endIndex >= index)
				parenthesis.get(i).endIndex += shiftAmount;
		}
		
		if (indicesAlreadyAdjusted != null) {
			for (int i = 0; i < indicesAlreadyAdjusted.length; i++) {
				if (indicesAlreadyAdjusted[i] >= index) {
					indicesAlreadyAdjusted[i] += shiftAmount;
				}
			}
		}
	}

	//================ RESET METHODS ================

	/**
	 * Clears all EOS characters so we can recalculate them and reset the tracker
	 */
	public void resetEOSCharacters() {
		eoses.clear();
		eosSize = 0;
	}

	/**
	 * Clears all Quote so we can recalculate them and reset the tracker
	 */
	public void resetQuotes() {
		quotes.clear();
		quoteSize = 0;
	}

	/**
	 * Clears all Parenthesis so we can recalculate them and reset the tracker
	 */
	public void resetParenthesis() {
		parenthesis.clear();
		parenthesisSize = 0;
	}

	/**
	 * Resets the entire tracker by clearing all tracked characters
	 */
	public void resetAll() {
		resetEOSCharacters();
		resetQuotes();
		resetParenthesis();
	}
	
	//================ ASSORTED ================

	/**
	 * Returns a string representation of this SpecialCharTracker
	 */
	public String toString() {
		String toReturn = NAME+"EOSES:\n";
		for (int i = 0; i < eosSize; i++) {
			toReturn += NAME+ "   " + eoses.get(i) + "\n";
		}
		
		toReturn += NAME+"QUOTES:\n";
		for (int i = 0; i < quoteSize; i++) {
			toReturn += NAME+ "   " + quotes.get(i) + "\n";
		}
		
		toReturn += NAME+"PARENTHESIS:\n";
		for (int i = 0; i < parenthesisSize; i++) {
			toReturn += NAME+ "   " + quotes.get(i) + "\n";
		}

		return toReturn;
	}

	//=======================================================================
	//*					QUOTE AND PARENTHESIS METHODS						*	
	//=======================================================================
	
	/**
	 * Adds a Quote to the tracker, should be called EVERY TIME a new quote is
	 * typed, regardless of whether or not it's the starting quote or a
	 * closing one.
	 *
	 * addQuote() checks to see if there there is a non-closed quote prior to
	 * this one's index, and if there is it will treat this ass the closing
	 * index and add that index as endIndex to that TextWrapper object,
	 * otherwise it will create a new Quote object with this passed index as
	 * the startIndex, endIndex = -1, and closed = false
	 * 
	 * @param index
	 *        The index of the newly added Quote
	 */
	public void addQuote(int index) {
		int closingQuote = -1;
		for (int i = 0; i < quoteSize; i++) {
			if (!quotes.get(i).closed) {
				System.out.println("QUOTE NOT CLOSED");
				closingQuote = i;
				break;
			}
		}

		//It's a new quote, create a new TextWrapper Object with this as start index
		if (closingQuote == -1) {
			quotes.add(new TextWrapper(index));
			indicesAlreadyAdjusted = new int[2];
			indicesAlreadyAdjusted[0] = main.editorDriver.sentIndices[0];
			System.out.println("THIS LENGTH 301 = " + + main.editorDriver.taggedDoc.length);
			indicesAlreadyAdjusted[1] = main.editorDriver.taggedDoc.length-1;
			quoteSize++;
		//It's a closing quote, simply add this as the endIndex of that Object
		} else {
			quotes.get(closingQuote).closed = true;
			quotes.get(closingQuote).endIndex = index;
			indicesAlreadyAdjusted = null;
		}
	}

	/**
	 * Checks if a given character is a quote.
	 * 
	 * @param  unknownChar
	 *      The character you want to test
	 *         
	 * @return
	 * 		True or false, depending on whether or not it's a quote
	 */
	public boolean isQuote(char unknownChar) {
		boolean result = false;
		
		if (QUOTE == unknownChar) {
			result = true;
		}
		
		return result;
	}

	/**
	 * Checks if a given character is a parenthesis
	 * 
	 * @param  unknownChar
	 *      The character you want to test
	 *         
	 * @return
	 * 		True or false, depending on whether or not it's a parenthesis
	 */
	public boolean isParenthesis(char unknownChar) {
		boolean result = false;

		if (PARENTHESIS.contains(unknownChar)) {
			result = true;
		}

		return result;
	}

	/**
	 * Adds a Parentehsis to the tracker, should be called EVERY TIME a new quote is
	 * typed, regardless of whether or not it's the starting quote or a
	 * closing one.
	 *
	 * addQuote() checks to see if there there is a non-closed quote prior to
	 * this one's index, and if there is it will treat this ass the closing
	 * index and add that index as endIndex to that TextWrapper object,
	 * otherwise it will create a new Quote object with this passed index as
	 * the startIndex, endIndex = -1, and closed = false
	 * 
	 * @param index
	 *        The index of the newly added Quote
	 */
	public void addParenthesis(int index) {
		int closingParenthesis = -1;
		for (int i = 0; i < parenthesisSize; i++) {
			if (!parenthesis.get(i).closed) {
				closingParenthesis = i;
				break;
			}
		}

		//It's a new quote, create a new TextWrapper Object with this as start index
		if (closingParenthesis == -1) {
			parenthesis.add(new TextWrapper(index));
			indicesAlreadyAdjusted = new int[2];
			indicesAlreadyAdjusted[0] = main.editorDriver.sentIndices[0];
			System.out.println("THIS LENGTH 378 = "+ main.editorDriver.taggedDoc.length);
			indicesAlreadyAdjusted[1] = main.editorDriver.taggedDoc.length-1;
			parenthesisSize++;
		//It's a closing quote, simply add this as the endIndex of that Object
		} else {
			parenthesis.get(closingParenthesis).closed = true;
			parenthesis.get(closingParenthesis).endIndex = index;
			indicesAlreadyAdjusted = null;
		}
	}

	/**
	 * Determines whether or not the given index (most likely the caret position)
	 * is inbetween a given pair of quotes or parenthesis.
	 * @param  index [description]
	 * @return       [description]
	 */
	public int[] adjustIndicesToIgnoreWrappers(int start, int end) {
		int[] indices = {start, end};

//		if (indicesAlreadyAdjusted != null) {
//			indices = indicesAlreadyAdjusted;
//		} else {
//			for (int i = 0; i < quoteSize; i++) {
//				/**
//				 * We want to check if:
//				 * 		Test sentence "Test.
//				 * If not, then:
//				 * 		Sentence." End of actual sentence.
//				 */
//				if (start < quotes.get(i).startIndex && end > quotes.get(i).startIndex) {
//					if (quotes.get(i).closed) {
//						//Just this sentence, no EOS characters between quotes, just use passed start and end indices
//						if (quotes.get(i).endIndex < end) {
//							continue;
//						//Get the closing quote index, calculate sentence indices of that index position, then highlight
//						} else {
//							System.out.println(">>>>> SENTENCE END <<<<<< = " + quotes.get(i).sentenceEnd);
//							indices[1] = quotes.get(i).sentenceEnd;
//							break;
//						}
//					//Not closed, highlight EVERYTHING past this point (because it's all enclosed in the wrapper until it's closed)
//					} else {
//						System.out.println("THIS LENGTH 420 = " + main.editorDriver.taggedDoc.length);
//						indices[1] = main.editorDriver.taggedDoc.length;
//						indices[0] = quotes.get(i).sentenceStart;
//						break;
//					}
//				} else if (quotes.get(i).closed) {
//					//Get starting quote index, calculate sentence indices of that index position, then highlight
//					if (start <= quotes.get(i).endIndex && end > quotes.get(i).endIndex) {
//						System.out.println("FIND ME = " + quotes.get(i).sentenceLength + ", " + quotes.get(i).sentenceStart);
//						indices[0] = quotes.get(i).sentenceStart;
//						break;
//					}
//				} else {
//					indices[0] = quotes.get(i).sentenceStart;
//					indices[1] = main.editorDriver.taggedDoc.length;
//					break;
//				}
//			}
//
//			indicesAlreadyAdjusted = indices;
//		}

		return indices;
	}

	/**
	 * Removes any Text Wrapper Objects between the given indices as [5, 10),
	 * meaning inclusive for the first and exclusive for the last.
	 * 
	 * @param  lowerBound
	 *         The beginning of hte range you want to remove from (includes this position)
	 * @param  upperBound
	 *         The end fo the range you want to remove from (does not include this position)
	 *         
	 * @return
	 * 		Whether or not any Text Wrapper Objects were removed from the given range
	 */
	public boolean removeTextWrappersInRange(int lowerBound, int upperBound) {
		Logger.logln(NAME+"Removing Text Wrappers in range " + lowerBound + " - " + upperBound);
		boolean removed = false;

		for (int i = 0; i < quoteSize; i++) {
			if (quotes.get(i).startIndex >= lowerBound && quotes.get(i).startIndex < upperBound) {
				if (quotes.get(i).closed) {
					if (quotes.get(i).endIndex >= lowerBound && quotes.get(i).endIndex < upperBound) {
						Logger.logln(NAME+"Removed Quote TextWrapper Object from " + quotes.get(i).startIndex + " - " + quotes.get(i).endIndex);
						quotes.remove(i);
						i--; // decrement 'i' so that we don't miss the object that shifts down into the spot just freed.
						quoteSize--; // also decrement quoteSize
						removed = true;
					} else {
						quotes.get(i).startIndex = quotes.get(i).endIndex;
						quotes.get(i).endIndex = -1;
						quotes.get(i).closed = false;
						removed = true;
					}
				} else {
					Logger.logln(NAME+"Removed Quote TextWrapper Object from " + quotes.get(i).startIndex + " - " + quotes.get(i).endIndex);
					quotes.remove(i);
					i--; // decrement 'i' so that we don't miss the object that shifts down into the spot just freed.
					quoteSize--; // also decrement quoteSize
					removed = true;
				}
			} else if (quotes.get(i).closed) {
				if (quotes.get(i).endIndex >= lowerBound && quotes.get(i).endIndex < upperBound) {
					quotes.get(i).endIndex = -1;
					quotes.get(i).closed = false;
					removed = true;
				}
			}
		}

		for (int i = 0; i < parenthesisSize; i++) {
			if (parenthesis.get(i).startIndex >= lowerBound && parenthesis.get(i).startIndex < upperBound) {
				if (parenthesis.get(i).closed) {
					if (parenthesis.get(i).endIndex >= lowerBound && parenthesis.get(i).endIndex < upperBound) {
						Logger.logln(NAME+"Removed Parenthesis TextWrapper Object from " + parenthesis.get(i).startIndex + " - " + parenthesis.get(i).endIndex);
						parenthesis.remove(i);
						i--; // decrement 'i' so that we don't miss the object that shifts down into the spot just freed.
						parenthesisSize--; // also decrement parenthesisSize
						removed = true;
					} else {
						parenthesis.get(i).startIndex = parenthesis.get(i).endIndex;
						parenthesis.get(i).endIndex = -1;
						parenthesis.get(i).closed = false;
						removed = true;
					}
				} else {
					Logger.logln(NAME+"Removed Parenthesis TextWrapper Object from " + parenthesis.get(i).startIndex + " - " + parenthesis.get(i).endIndex);
					parenthesis.remove(i);
					i--; // decrement 'i' so that we don't miss the object that shifts down into the spot just freed.
					parenthesisSize--; // also decrement parenthesisSize
					removed = true;
				}
			} else if (parenthesis.get(i).closed) {
				if (parenthesis.get(i).endIndex >= lowerBound && parenthesis.get(i).endIndex < upperBound) {
					parenthesis.get(i).endIndex = -1;
					parenthesis.get(i).closed = false;
					removed = true;
				}
			}
		}
		
		return removed;
	}

	//=======================================================================
	//*							EOS METHODS									*	
	//=======================================================================

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
		Logger.logln(NAME+"EOS character added at " + location);
		eoses.add(new EOS(eosChar, location, ignore));
		eosSize++;
	}
	

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
	public boolean setIgnoreEOS(int index, boolean ignore) {
		boolean found = false;
		
		for (int i = 0; i < eosSize; i++) {
			if (index == eoses.get(i).location) {
				Logger.logln(NAME+"Will ignore EOS character at " + index + ": " + ignore);
				eoses.get(i).ignore = ignore;
				found = true;
				break;
			}
		}

		return found;
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
		
		if (EOS.contains(unknownChar)) {
			result = true;
		}
		
		return result;
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
	public boolean isSentenceEndAtIndex(int index) {
		boolean result = false;

		/**
		 * For first processing (TODO and possibly reprocessing, if this is
		 * the case then this check will only work for the first process), we
		 * don't want the code below to run and check everything, just return
		 * true every time. (Just trust me, it works).
		 */
		if (eosSize == 0) {
			result = true;
		} else {
			for (int i = 0; i < eosSize; i++) {
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

		for (int i = 0; i < eosSize; i++) {
			location = eoses.get(i).location;
			if (location >= lowerBound && location < upperBound) {
				Logger.logln(NAME+"EOS removed at " + location);
				eoses.remove(i);
				i--; // decrement 'i' so that we don't miss the object that shifts down into the spot just freed.
				eosSize--; // also decrement eosSize
				removed = true;
			}
		}

		return removed;		
	}
}

/**
 * Holds the EOS characters at a given location within the document with
 * respect to the first index of the document (0).
 *
 * @author  Andrew W.E. McDonald
 * @author  Marc Barrowclift
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
	 * 
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
		return "[ "+eos+" : "+location+", is end of sentence = " + ignore + " ]";
	}
}