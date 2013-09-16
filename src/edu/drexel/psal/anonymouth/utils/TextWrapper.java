package edu.drexel.psal.anonymouth.utils;

import java.io.Serializable;

/**
 * Holds the TextWrapper at a given location within the document with respect to
 * the first index of the document (0).
 *
 * @author  Marc Barrowclift
 */
public class TextWrapper implements Serializable {

	private static final long serialVersionUID = 1166206447149377187L;
	public int startIndex;
	public int endIndex;
	public boolean closed;

	/**
	 * Constructor, assumes this TextWrapper is closed based
	 * on the fact that a quote close index is given.
	 * 
	 * @param  start
	 *         The index of the opening quote
	 * @param  end
	 *         The index of the closting quote
	 */
	public TextWrapper(int start, int end) {
		closed = true;
		startIndex = start;
		endIndex = end;
	}

	/**
	 * Constructor, assumes it's not yet closed based
	 * on the fact that no quote close index is given.
	 * 
	 * @param  start
	 *         The index of the opening quote
	 */
	public TextWrapper(int start) {
		closed = false;
		startIndex = start;
		endIndex = -1;
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
			toReturn = "[ Start : " + startIndex + " End : " + endIndex + " ]";
		} else {
			toReturn = "[ Start : " + startIndex + " End : NOT YET CLOSED ]";
		}

		return toReturn;
	}
	
	public void setClosingQuote(int closingQuote) {
		if (closingQuote > startIndex) {
			endIndex = closingQuote;
		} else {
			endIndex = startIndex;
			startIndex = closingQuote;
		}
		closed = true;
	}
}