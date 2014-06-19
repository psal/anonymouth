package edu.drexel.psal.anonymouth.utils;

import java.io.Serializable;

/**
 * Holds the TextWrapper at a given location within the document with respect to
 * the first index of the document (0).
 *
 * TextWrappers are the name I gave to any sort of character that are considered to
 * "wrap" or surround text in such a way that it makes all the text within them part of
 * the existing sentence. The perfect example for this are quotations since, with the
 * TextWrapper code utilitized,<br>
 * 		<code>I said "Do tell! How cool." to my friend.</code><br>
 * will be highlighted and treated on the EDITOR side as a whole sentence (as it should
 * be), while on the TaggedSentence backend side the sentence is split as:<br>
 * 		I said "Do tell!<br>
 * 		 How cool.<br>
 * 		" to my friend.<br>
 * It would look awful to highlight it this way, which is why this exists.<br><br>
 *
 * Currently, the TextWrappers that are recognized and supported by SpecialCharTracker are:
 * <ul>
 * 	<li>"" - Quotes</li>
 * 	<li>() - Parenthesis</li>
 * 	<li>[] - Brackets<li>
 * 	<li>{} - Squigglies<li>
 * </ul>
 *
 * @author  Marc Barrowclift
 */

public class TextWrapper implements Serializable {

	private static final long serialVersionUID = 1166206447149377187L;
	private final String CLOSING_CHARS = ")]}";

	/**
	 * The start index of this text wrapper
	 */
	public int startIndex;
	/**
	 * The end index of this text wrapper
	 */
	public int endIndex;
	/**
	 * Whether or not the text wrapper is closed (meaning that both startIndex
	 * and endIndex are set and not equal to -1)
	 */
	public boolean closed;

	/**
	 * Constructor, assumes this TextWrapper is closed based
	 * on the fact that a wrapper close index is given.
	 * 
	 * @param  start
	 *         The index of the opening wrapper
	 * @param  end
	 *         The index of the closting wrapper
	 */
	public TextWrapper(int start, int end) {
		closed = true;
		startIndex = start;
		endIndex = end;
	}

	/**
	 * Constructor, assumes it's not yet closed based
	 * on the fact that no wrapper close index is given.
	 * 
	 * @param  newIndex
	 *         The index of the opening or closing wrapper
	 * @param  newChar 
	 *         The char we are adding to the tracker
	 */
	public TextWrapper(int newIndex, char newChar) {
		closed = false;

		if (CLOSING_CHARS.charAt(0) == newChar || CLOSING_CHARS.charAt(1) == newChar || CLOSING_CHARS.charAt(2) == newChar) {
			startIndex = -1;
			endIndex = newIndex;
		} else {
			startIndex = newIndex;
			endIndex = -1;
		}
	}
	
	/**
	 * Constructor
	 * 
	 * @param textWrapper
	 * 		TextWrapper instance you want to deep copy
	 */
	public TextWrapper(TextWrapper textWrapper) {
		closed = textWrapper.closed;
		startIndex = textWrapper.startIndex;
		endIndex = textWrapper.endIndex;
	}

	/**
	 * Our custom toString() method to make printing these
	 * things easier for debugging
	 * 
	 * @return
	 * 		Our modified string representation of the TextWrapper
	 */
	@Override
	public String toString() {
		String toReturn = "";

		if (closed) {
			toReturn = "[ Start : " + startIndex + ", End : " + endIndex + " ]";
		} else if (endIndex == -1) {
			toReturn = "[ Start : " + startIndex + ", End : NOT YET CLOSED ]";
		} else {
			toReturn = "[ Start : NOT YET CLOSED, End : " + endIndex + " ]";
		}

		return toReturn;
	}
	
	/**
	 * Sets the closing textWrapper object for this instance. It will either
	 * set it as the start or end closer, depending on which one has yet to be
	 * set.
	 * 
	 * @param closingWrapper
	 *        The index of the closing text wrapper
	 */
	public void setClosingWrapper(int closingWrapper) {
		if (endIndex == -1) {
			endIndex = closingWrapper;
		} else {
			startIndex = closingWrapper;
		}

		closed = true;
	}
}