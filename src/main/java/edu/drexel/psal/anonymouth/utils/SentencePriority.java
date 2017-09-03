package edu.drexel.psal.anonymouth.utils;

/**
 * Sentence priority tagger for the Translation's priority queue.
 * @author Marc Barrowclift
 *
 */

public class SentencePriority implements Comparable<SentencePriority> {
	
	private final String NAME = "( "+this.getClass().getName()+" ) - ";
	public enum Priority {HIGH, LOW};
	private Priority priority;
	protected String untagged;
	
	
	/**
	 * Constructor - accepts an unprioritized string
	 * @param untagged
	 */
	public SentencePriority(String untagged, Priority priority) {
		this.untagged = untagged;
		this.priority = priority;
	}
	
	/**
	 * Compares two SentencePriority objects and returns:
	 * 0: if they have the same priority
	 * -1: if the passed object has a greater priority than the calling object
	 * 1: if the passed object has a lower priority than the calling object
	 */
	@Override
	public int compareTo(SentencePriority sentence) {
		int result = 0;
		
		if (this.getSentencePriority() == sentence.getSentencePriority())
			result = 0;
		else if (this.getSentencePriority() == Priority.HIGH)
			result = 1;
		else
			result = -1;
		
		return result;
	}
	
	/**
	 * Returns the sentence's priority
	 * @return priority - the sentence's priority
	 */
	public Priority getSentencePriority() {
		return priority;
	}
}
