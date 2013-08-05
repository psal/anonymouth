package edu.drexel.psal.jstylo.eventDrivers;

import com.jgaap.generics.*;

/**
 * Abstract class for event drivers that designed to hold a single event of a numeric value, e.g. sentence count,
 * average syllables in word, etc.
 * 
 * @author Ariel Stolerman
 *
 */
public abstract class SingleNumericEventDriver extends EventDriver {
	
	/* ======
	 * fields
	 * ======
	 */
	
	/**
	 * Returns the numeric value of the single event in the event set extracted.
	 * @param doc
	 * 		The document to parse.
	 * @return
	 * 		The numeric value of the single event in the event set extracted.
	 */
	public abstract double getValue(Document doc) throws EventGenerationException;
	
	/**
	 * Default createEventSet method - based on the getValue calculation.
	 */
	public EventSet createEventSet(Document doc) throws EventGenerationException {
		EventSet res = new EventSet();
		res.addEvent(new Event(getValue(doc)+""));
		return res;
	}
}
