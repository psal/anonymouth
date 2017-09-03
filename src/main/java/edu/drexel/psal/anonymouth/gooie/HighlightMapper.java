package edu.drexel.psal.anonymouth.gooie;

/**
 * HighlightMapper stores a highlighted Object as well as its position in the string 
 * @author Andrew W.E. McDonald
 *
 */
public class HighlightMapper implements Comparable<HighlightMapper>{

	//private final String NAME = "( "+this.getClass().getName()+" ) - ";

	private int start;
	private int end;
	private int spread;
	private Object highlightedObject;

	/**
	 * Constructor
	 * @param start - starting index of the highlighted object
	 * @param end - ending index of the highlighted object
	 * @param highlightedObject - the highlighted object
	 */
	public HighlightMapper(int start, int end, Object highlightedObject){
		this.start = start;
		this.end = end;
		this.spread = end - start;
		this.highlightedObject = highlightedObject;
	}

	public int getStart(){
		return start;
	}

	public void decrement(int amount){
		start = start - amount;
		end = end - amount;
	}

	public void increment(int amount){
		start = start + amount;
		end = end + amount;
	}

	public int getEnd(){
		return end;
	}

	public int getSpread(){
		return spread;
	}

	public Object getHighlightedObject(){
		return highlightedObject;
	}

	@Override
	public int compareTo(HighlightMapper o) {
		return ((Integer)start).compareTo((Integer)o.start);
	}
}