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
 * Now also tracks all TextWrappers in the similar way with a few additional
 * stuff like extend highlights and whatnot. For more details on what the hell
 * TextWrappers are and why they exist at all, see TextWrapper.java
 *
 * @author  Marc Barrowclift
 * @author  Andrew W.E. McDonald
 */
public class SpecialCharTracker implements Serializable {
	
	public final String NAME = "( " + this.getClass().getSimpleName() +" ) - ";
	private static final long serialVersionUID = -5900337779583604917L;

	/**
	 * The characters we acknowledge as possible was to end a sentence
	 */
	private final HashSet<Character> EOS;
	private GUIMain main;
	/**
	 * The character we acknowledge as a quote
	 */
	private final char QUOTE = '"';
	/**
	 * The characters we acknowledge as parenthesis
	 */
	private final String PARENTHESIS = "()";
	private final String BRACKETS = "[]";
	private final String SQUIGGLIES = "{}";
	/**
	 * Our array list of EOS character objects
	 */
	public ArrayList<EOS> eoses;
	/**
	 * Our array list of quotes
	 */
	public ArrayList<TextWrapper> quotes;
	/**
	 * Our array list of parenthesis
	 */
	private ArrayList<TextWrapper> parenthesis;
	private ArrayList<TextWrapper> brackets;
	private ArrayList<TextWrapper> squigglies;

	private ArrayList<ArrayList<TextWrapper>> allTextWrappers;
	private int[] allTextWrapperSizes;
	private final int NUM_OF_TEXT_WRAPPERS = 4;
	/**
	 * The number of EOSes we are currently tracking
	 */
	public int eosSize;

	/**
	 * Constructor
	 */
	public SpecialCharTracker(GUIMain main) {
		EOS = new HashSet<Character>(3);
		EOS.add('.');
		EOS.add('!');
		EOS.add('?');
		
		eoses = new ArrayList<EOS>(100);
		quotes = new ArrayList<TextWrapper>(50);
		parenthesis = new ArrayList<TextWrapper>(50);
		brackets = new ArrayList<TextWrapper>(50);
		squigglies = new ArrayList<TextWrapper>(50);

		eosSize = 0;

		initAllTextWrappers();
		initAllTextWrapperSizes();
		this.main = main;
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
		this.main = specialCharTracker.main;
		quotes = new ArrayList<TextWrapper>(50);
		parenthesis = new ArrayList<TextWrapper>(50);
		brackets = new ArrayList<TextWrapper>(50);
		squigglies = new ArrayList<TextWrapper>(50);
		initAllTextWrappers();
		initAllTextWrapperSizes();

		//EOS characters
		int tempSize = specialCharTracker.eosSize;
		eoses = new ArrayList<EOS>(tempSize);
		eosSize = 0;
		for (int i = 0; i < tempSize; i++) {
			if (specialCharTracker.eoses.get(i) == null) {
				break;
			}
			eosSize++;
			eoses.add(new EOS(specialCharTracker.eoses.get(i)));
		}

		//Text Wrappers
		for (int i = 0; i < NUM_OF_TEXT_WRAPPERS; i++) {
			deepCopy(specialCharTracker, i);
		}
	}

	/**
	 * Utilities method for the SpecialCharTracker constructor above and
	 * performs a deep copy for the current TextWrapper object ArrayList.
	 * 
	 * @param specialCharTracker
	 *        The instance of specialCharTracker we're deep copying
	 * @param curIndex
	 *        The current index we're on, representing the current
	 *        TextWrapper array and size we're working with (Quotes,
	 *        Parenthesis, Brackets, etc.)
	 */
	private void deepCopy(SpecialCharTracker specialCharTracker, int curIndex) {
		int tempSize = specialCharTracker.allTextWrapperSizes[curIndex];
		for (int i = 0; i < tempSize; i++) {
			if (specialCharTracker.allTextWrappers.get(curIndex) == null) {
				break;
			}
			
			allTextWrapperSizes[curIndex]++;
			allTextWrappers.get(curIndex).add(specialCharTracker.allTextWrappers.get(curIndex).get(i));
		}
	}
	
	/**
	 * Initialies the textWrappers array with empty
	 * TextWrapper Arrays
	 */
	private void initAllTextWrappers() {
		allTextWrappers = new ArrayList<ArrayList<TextWrapper>>(NUM_OF_TEXT_WRAPPERS);
		allTextWrappers.add(quotes);		//Quotes
		allTextWrappers.add(parenthesis);	//Parenthesis
		allTextWrappers.add(brackets);		//Brackets
		allTextWrappers.add(squigglies);	//Squigglies
	}
	
	/**
	 * Initializes the textWrapperSizes array with 0's for
	 * each one.
	 */
	private void initAllTextWrapperSizes() {
		allTextWrapperSizes = new int[NUM_OF_TEXT_WRAPPERS];
		allTextWrapperSizes[0] = 0;	//Quotes
		allTextWrapperSizes[1] = 0;	//Parenthesis
		allTextWrapperSizes[2] = 0;	//Brackets
		allTextWrapperSizes[3] = 0;	//Squigglies
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

		//Text Wrappers
		for (int i = 0; i < NUM_OF_TEXT_WRAPPERS; i++) {
			shift(allTextWrappers.get(i), allTextWrapperSizes[i], index, shiftAmount);
		}
	}

	private void shift(ArrayList<TextWrapper> curTextWrapper, int curSize, int index, int shiftAmount) {
		for (int i = 0; i < curSize; i++) {
			if (curTextWrapper.get(i).startIndex >= index)
				curTextWrapper.get(i).startIndex += shiftAmount;
			if (curTextWrapper.get(i).closed == true && curTextWrapper.get(i).endIndex >= index)
				curTextWrapper.get(i).endIndex += shiftAmount;
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
		allTextWrapperSizes[0] = 0;
	}

	/**
	 * Clears all Parenthesis so we can recalculate them and reset the tracker
	 */
	public void resetParenthesis() {
		parenthesis.clear();
		allTextWrapperSizes[1] = 0;
	}

	/**
	 * Clears all Brackets so we can recalculate them and reset the tracker
	 */
	public void resetBrackets() {
		brackets.clear();
		allTextWrapperSizes[2] = 0;
	}

	/**
	 * Clears all Squigglies so we can recalculate them and reset the tracker
	 */
	public void resetSquigglies() {
		squigglies.clear();
		allTextWrapperSizes[3] = 0;
	}

	/**
	 * Resets the entire tracker by clearing all tracked characters
	 */
	public void resetAll() {
		resetEOSCharacters();
		resetQuotes();
		resetParenthesis();
		resetBrackets();
		resetSquigglies();
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
		for (int i = 0; i < allTextWrapperSizes[0]; i++) {
			toReturn += NAME+ "   " + quotes.get(i) + "\n";
		}
		
		toReturn += NAME+"PARENTHESIS:\n";
		for (int i = 0; i < allTextWrapperSizes[1]; i++) {
			toReturn += NAME+ "   " + parenthesis.get(i) + "\n";
		}

		toReturn += NAME+"BRACKETS:\n";
		for (int i = 0; i < allTextWrapperSizes[2]; i++) {
			toReturn += NAME+ "   " + brackets.get(i) + "\n";
		}
		
		toReturn += NAME+"SQUIGGLIES:\n";
		for (int i = 0; i < allTextWrapperSizes[3]; i++) {
			toReturn += NAME+ "   " + squigglies.get(i) + "\n";
		}

		return toReturn;
	}

	//=======================================================================
	//*					TEXT WRAPPER GENERAL METHODS						*	
	//=======================================================================
	
	/**
	 * To be called by EditorDriver's getSentenceIndices() every single time
	 * the sentence highlight is being changed (new sentence, sentence
	 * changes, etc). This is to ensure that the highlight extends to what the
	 * user precieves as a full sentence to give the illusion of a complete
	 * one (while in actuality they are separate tagged sentence objects). We
	 * do it this way since, in the past, we were actually combining and
	 * splitting this sentences based on text wrapper positions but to be
	 * quite honest it sucked. This way, it's faster, smarter, and all around
	 * easier to understand.
	 *
	 * Say a sentence like "Hi."'s indices is passed. This will determine if
	 * it's in between text wrappers, like "She said "Nice to meet you. Hi. My
	 * name's Lucy." kind of loud." If this is the case, instead of just
	 * highlighting the sentence "Hi.", we extend the highlight to all the
	 * tagged sentences we deem to be part of a single one. The best way to
	 * understand how this is done is simply to read the code.
	 *
	 * @param  start
	 *         The start index of the sentence
	 * @param  end
	 *         The end index of the sentence
	 * @param  allSentIndices
	 *         The allSentIndices int array straight from getSentenceIndices()
	 * @param  selectedSentence
	 *         The selectedSentence int straight from getSentenceIndices()
	 *
	 * @return
	 * 	An integer array representing the new highlights. The indices represent:<br>
	 * 		[0] = Where the start of the highlight should be<br>
	 * 		[1] = Where the end of the highlight should be<br>
	 * 		[2] = The number of extra sentences included in the extended highlight to the LEFT<br>
	 * 		[3] = The number of extra sentences included in the extended highlight to the RIGHT<br><br>
	 *
	 *	The last two indices of the array are specifically for the automatic word to
	 *	remove highlights, as they require the sentence numbers to obtain the words
	 *	to remove. If that could be updated with no loss in performance to instead
	 *	use just indices then we could get ride of these "extra sentences" portion of
	 *	this return array.
	 */
	public int[] extendHighlights(int start, int end, int[] allSentIndices, int[] sentenceLengths, int selectedSentence) {
		int[] highlightIndices = {start, end, 0, 0};
		for (int i = 0; i < NUM_OF_TEXT_WRAPPERS; i++) {
			highlightIndices = extendHighlights(highlightIndices[0], highlightIndices[1], allSentIndices, sentenceLengths, selectedSentence, allTextWrappers.get(i), allTextWrapperSizes[i], highlightIndices);
		}
		
		return highlightIndices;
	}

	private int[] extendHighlights(int asdf, int endgf, int[] allSentIndices, int[] sentenceLengths,
			int selectedSentence, ArrayList<TextWrapper> curTextWrapper, int curSize, int[] highlightIndices) {
		for (int t = 0; t < curSize; t++) {
			if (curTextWrapper.get(t).closed) {
				//Full text wrapper within sentence
				if (curTextWrapper.get(t).startIndex > highlightIndices[0] && curTextWrapper.get(t).endIndex < highlightIndices[1]) {
					continue;
				//Whole sentence between text wrapper
				} else if (highlightIndices[0] >= curTextWrapper.get(t).startIndex && highlightIndices[1] <= curTextWrapper.get(t).endIndex) {
					highlightIndices[0] = curTextWrapper.get(t).startIndex;
					for (int sent = selectedSentence; sent >= 0; sent--) {
						if (highlightIndices[0] > (allSentIndices[sent] - sentenceLengths[sent]) && highlightIndices[0] < allSentIndices[sent]) {
							highlightIndices[0] = allSentIndices[sent] - sentenceLengths[sent];
							highlightIndices[2] = selectedSentence - sent;
							break;
						}
					}
					highlightIndices[1] = curTextWrapper.get(t).endIndex;
					for (int sent = selectedSentence; sent < main.editorDriver.taggedDoc.numOfSentences; sent++) {
						if (highlightIndices[1] > (allSentIndices[sent] - sentenceLengths[sent]) && highlightIndices[1] < allSentIndices[sent]) {
							highlightIndices[1] = allSentIndices[sent];
							highlightIndices[3] = sent - selectedSentence;
							break;
						}
					}
				//Start of text wrapper in sentence
				} else if (highlightIndices[0] < curTextWrapper.get(t).startIndex && highlightIndices[1] > curTextWrapper.get(t).startIndex) {
					highlightIndices[1] = curTextWrapper.get(t).endIndex;
					for (int sent = selectedSentence; sent < main.editorDriver.taggedDoc.numOfSentences; sent++) {
						if (highlightIndices[1] > (allSentIndices[sent] - sentenceLengths[sent]) && highlightIndices[1] < allSentIndices[sent]) {
							highlightIndices[1] = allSentIndices[sent];
							highlightIndices[3] = sent - selectedSentence;
							break;
						}
					}
				//End of text wrapper in sentence
				} else if (highlightIndices[0] <= curTextWrapper.get(t).endIndex && highlightIndices[1] > curTextWrapper.get(t).endIndex) {
					highlightIndices[0] = curTextWrapper.get(t).startIndex;
					for (int sent = selectedSentence; sent >= 0; sent--) {
						if (highlightIndices[0] > (allSentIndices[sent] - sentenceLengths[sent]) && highlightIndices[0] < allSentIndices[sent]) {
							highlightIndices[0] = allSentIndices[sent] - sentenceLengths[sent];
							highlightIndices[2] = selectedSentence - sent;
							break;
						}
					}
				}
			}
		}
		
		return highlightIndices;
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
	public void removeTextWrappersInRange(int lowerBound, int upperBound) {
		Logger.logln(NAME+"Removing Text Wrappers in range " + lowerBound + " - " + upperBound);

		for (int i = 0; i < NUM_OF_TEXT_WRAPPERS; i++) {
			removeTextWrappersInRange(lowerBound, upperBound, allTextWrappers.get(i), i);
		}
	}
	
	public void removeTextWrappersInRange(int lowerBound, int upperBound, ArrayList<TextWrapper> curTextWrapper, int curSize) {
		for (int i = 0; i < allTextWrapperSizes[curSize]; i++) {
			if (curTextWrapper.get(i).startIndex >= lowerBound && curTextWrapper.get(i).startIndex < upperBound) {
				if (curTextWrapper.get(i).closed) {
					if (curTextWrapper.get(i).endIndex >= lowerBound && curTextWrapper.get(i).endIndex < upperBound) {
						Logger.logln(NAME+"Removed TextWrapper Object at " + curTextWrapper.get(i).startIndex + " - " + curTextWrapper.get(i).endIndex);
						curTextWrapper.remove(i);
						i--; // decrement 'i' so that we don't miss the object that shifts down into the spot just freed.
						allTextWrapperSizes[curSize]--; // also decrement quoteSize
					} else {
						curTextWrapper.get(i).startIndex = -1;
						curTextWrapper.get(i).closed = false;
					}
				} else {
					Logger.logln(NAME+"Removed TextWrapper Object at " + curTextWrapper.get(i).startIndex + " - " + curTextWrapper.get(i).endIndex);
					curTextWrapper.remove(i);
					i--; // decrement 'i' so that we don't miss the object that shifts down into the spot just freed.
					allTextWrapperSizes[curSize]--; // also decrement quoteSize
				}
			} else if (curTextWrapper.get(i).closed) {
				if (curTextWrapper.get(i).endIndex >= lowerBound && curTextWrapper.get(i).endIndex < upperBound) {
					curTextWrapper.get(i).endIndex = -1;
					curTextWrapper.get(i).closed = false;
				}
			}
		}
	}

	//=======================================================================
	//*								QUOTES									*	
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
		for (int i = 0; i < allTextWrapperSizes[0]; i++) {
			if (!quotes.get(i).closed) {
				//If the text wrapper doesn't have an end index
				if (quotes.get(i).endIndex == -1) {
					//We need to check to make sure we are closing the closest available unclosed text wrapper
					if (closingQuote == -1) {
						closingQuote = i;
					} else {
						//If it's closer, then we're closing this text wrapper and not the one farther away
						if (quotes.get(i).startIndex > quotes.get(closingQuote).startIndex) {
							closingQuote = i;
						}
					}
				//If the text wrapper doesn't have a start index
				} else if (quotes.get(i).startIndex == -1) {
					//We need to check to make sure we are closing the closest available unclosed text wrapper
					if (closingQuote == -1) {
						closingQuote = i;
					} else {
						//If it's closer, then we're closing this text wrapper and not the one farther away
						if (quotes.get(i).endIndex < quotes.get(closingQuote).endIndex) {
							closingQuote = i;
						}
					}
				}
			}
		}

		//It's a new quote, create a new TextWrapper Object with this as start index
		if (closingQuote == -1) {
			quotes.add(new TextWrapper(index, '"'));
			allTextWrapperSizes[0]++;
		//It's a closing quote, simply add this as the endIndex of that Object
		} else {
			quotes.get(closingQuote).setClosingWrapper(index);
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

	//=======================================================================
	//*							PARENTHESIS									*	
	//=======================================================================

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

		if (PARENTHESIS.charAt(0) == unknownChar || PARENTHESIS.charAt(1) == unknownChar) {
			result = true;
		}

		return result;
	}

	/**
	 * Determines whether or not the passed paren is a closing
	 * one or not
	 * @param  paren
	 *         The paren char you want to check
	 *         
	 * @return
	 * 	True or false, depending on whether or not it's a closing
	 * 	paren
	 */
	private boolean isClosingParenthesis(char paren) {
		boolean result = false;

		if (PARENTHESIS.charAt(1) == paren) {
			result = true;
		}

		return result;
	}

	/**
	 * Adds a Parenthesis to the tracker, should be called EVERY TIME a new quote is
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
	public void addParenthesis(int index, char paren) {
		int parenthesisOfInterest = -1;
		for (int i = 0; i < allTextWrapperSizes[1]; i++) {			
			if (!parenthesis.get(i).closed) {
				//If the text wrapper doesn't have an end index and the new character is a closing one
				if (parenthesis.get(i).endIndex == -1 && isClosingParenthesis(paren)) {
					//We need to check to make sure we are closing the closest available unclosed text wrapper
					if (parenthesisOfInterest == -1) {
						parenthesisOfInterest = i;
					} else {
						//If it's closer, then we're closing this text wrapper and not the one farther away
						if (parenthesis.get(i).startIndex > parenthesis.get(parenthesisOfInterest).startIndex) {
							parenthesisOfInterest = i;
						}
					}
				//If the text wrapper doesn't have a start index and the new character is a starting one
				} else if (parenthesis.get(i).startIndex == -1 && !isClosingParenthesis(paren)) {
					//We need to check to make sure we are closing the closest available unclosed text wrapper
					if (parenthesisOfInterest == -1) {
						parenthesisOfInterest = i;
					} else {
						//If it's closer, then we're closing this text wrapper and not the one farther away
						if (parenthesis.get(i).endIndex < parenthesis.get(parenthesisOfInterest).endIndex) {
							parenthesisOfInterest = i;
						}
					}
				}
			}
		}

		//It won't fit into any existing objects, create a new one for it.
		if (parenthesisOfInterest == -1) {
			parenthesis.add(new TextWrapper(index, paren));
			allTextWrapperSizes[1]++;
		//It's closuing up (ether with a start or end) and existing wrapper object
		} else {
			parenthesis.get(parenthesisOfInterest).setClosingWrapper(index);
		}
	}

	//=======================================================================
	//*							BRACKETS									*	
	//=======================================================================
	
	/**
	 * Checks if a given character is a bracket
	 * 
	 * @param  unknownChar
	 *      The character you want to test
	 *         
	 * @return
	 * 		True or false, depending on whether or not it's a bracket
	 */
	public boolean isBracket(char unknownChar) {
		boolean result = false;

		if (BRACKETS.charAt(0) == unknownChar || BRACKETS.charAt(1) == unknownChar) {
			result = true;
		}

		return result;
	}

	/**
	 * Determines whether or not the passed bracket is a closing
	 * one or not
	 * @param  bracket
	 *         The bracket char you want to check
	 *         
	 * @return
	 * 	True or false, depending on whether or not it's a closing
	 * 	bracket
	 */
	private boolean isClosingBracket(char bracket) {
		boolean result = false;

		if (BRACKETS.charAt(1) == bracket) {
			result = true;
		}

		return result;
	}

	/**
	 * Adds a Bracket to the tracker, should be called EVERY TIME a new bracket is
	 * typed, regardless of whether or not it's the opening bracket or a
	 * closing one.
	 *
	 * addBracket() checks to see if there there is a non-closed bracket prior to
	 * this one's index, and if there is it will treat this ass the closing
	 * index and add that index as endIndex to that TextWrapper object,
	 * otherwise it will create a new Bracket object with this passed index as
	 * the startIndex, endIndex = -1, and closed = false
	 * 
	 * @param index
	 *        The index of the newly added Bracket
	 */
	public void addBracket(int index, char bracket) {
		int bracketOfInterest = -1;
		for (int i = 0; i < allTextWrapperSizes[2]; i++) {			
			if (!brackets.get(i).closed) {
				//If the text wrapper doesn't have an end index and the new character is a closing one
				if (brackets.get(i).endIndex == -1 && isClosingBracket(bracket)) {
					//We need to check to make sure we are closing the closest available unclosed text wrapper
					if (bracketOfInterest == -1) {
						bracketOfInterest = i;
					} else {
						//If it's closer, then we're closing this text wrapper and not the one farther away
						if (brackets.get(i).startIndex > brackets.get(bracketOfInterest).startIndex) {
							bracketOfInterest = i;
						}
					}
				//If the text wrapper doesn't have a start index and the new character is a starting one
				} else if (brackets.get(i).startIndex == -1 && !isClosingBracket(bracket)) {
					//We need to check to make sure we are closing the closest available unclosed text wrapper
					if (bracketOfInterest == -1) {
						bracketOfInterest = i;
					} else {
						//If it's closer, then we're closing this text wrapper and not the one farther away
						if (brackets.get(i).endIndex < brackets.get(bracketOfInterest).endIndex) {
							bracketOfInterest = i;
						}
					}
				}
			}
		}

		//It won't fit into any existing objects, create a new one for it.
		if (bracketOfInterest == -1) {
			brackets.add(new TextWrapper(index, bracket));
			allTextWrapperSizes[2]++;
		//It's closing up (either with a start or end) and existing wrapper object
		} else {
			brackets.get(bracketOfInterest).setClosingWrapper(index);
		}
	}

	//=======================================================================
	//*							SQUIGGLIES									*	
	//=======================================================================
	
	/**
	 * Checks if a given character is a squiggly
	 * 
	 * @param  unknownChar
	 *      The character you want to test
	 *         
	 * @return
	 * 		True or false, depending on whether or not it's a squiggly
	 */
	public boolean isSquiggly(char unknownChar) {
		boolean result = false;

		if (SQUIGGLIES.charAt(0) == unknownChar || SQUIGGLIES.charAt(1) == unknownChar) {
			result = true;
		}

		return result;
	}

	/**
	 * Determines whether or not the passed squiggly is a closing
	 * one or not.
	 * @param  squiggly
	 *         The squiggly char you want to check
	 *         
	 * @return
	 * 	True or false, depending on whether or not it's a closing
	 * 	squiggly
	 */
	private boolean isClosingSquiggly(char squiggly) {
		boolean result = false;

		if (SQUIGGLIES.charAt(1) == squiggly) {
			result = true;
		}

		return result;
	}

	/**
	 * Adds a squiggly to the tracker, should be called EVERY TIME a new squiggly is
	 * typed, regardless of whether or not it's the opening squiggly or a
	 * closing one.
	 *
	 * addsquiggly() checks to see if there there is a non-closed squiggly prior to
	 * this one's index, and if there is it will treat this ass the closing
	 * index and add that index as endIndex to that TextWrapper object,
	 * otherwise it will create a new squiggly object with this passed index as
	 * the startIndex, endIndex = -1, and closed = false
	 * 
	 * @param index
	 *        The index of the newly added Bracket
	 */
	public void addSquiggly(int index, char squiggly) {
		int squigglyOfInterest = -1;
		for (int i = 0; i < allTextWrapperSizes[3]; i++) {			
			if (!squigglies.get(i).closed) {
				//If the text wrapper doesn't have an end index and the new character is a closing one
				if (squigglies.get(i).endIndex == -1 && isClosingSquiggly(squiggly)) {
					//We need to check to make sure we are closing the closest available unclosed text wrapper
					if (squigglyOfInterest == -1) {
						squigglyOfInterest = i;
					} else {
						//If it's closer, then we're closing this text wrapper and not the one farther away
						if (squigglies.get(i).startIndex > squigglies.get(squigglyOfInterest).startIndex) {
							squigglyOfInterest = i;
						}
					}
				//If the text wrapper doesn't have a start index and the new character is a starting one
				} else if (squigglies.get(i).startIndex == -1 && !isClosingSquiggly(squiggly)) {
					//We need to check to make sure we are closing the closest available unclosed text wrapper
					if (squigglyOfInterest == -1) {
						squigglyOfInterest = i;
					} else {
						//If it's closer, then we're closing this text wrapper and not the one farther away
						if (squigglies.get(i).endIndex < squigglies.get(squigglyOfInterest).endIndex) {
							squigglyOfInterest = i;
						}
					}
				}
			}
		}

		//It won't fit into any existing objects, create a new one for it.
		if (squigglyOfInterest == -1) {
			squigglies.add(new TextWrapper(index, squiggly));
			allTextWrapperSizes[3]++;
		//It's closing up (ether with a start or end) and existing wrapper object
		} else {
			squigglies.get(squigglyOfInterest).setClosingWrapper(index);
		}
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
	
	public int getLocation(int index) {
		return eoses.get(index).location;
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
	
	public char eos;
	public int location;
	public boolean ignore;
	
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
		return "[ "+eos+" : "+location+", is end of sentence = " + !ignore + " ]";
	}
}