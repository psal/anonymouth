package edu.drexel.psal.jstylo.eventDrivers;

import com.jgaap.eventDrivers.*;
import com.jgaap.generics.*;

public class UniqueWordsCounterEventDriver extends SingleNumericEventDriver {

	/* ======
	 * fields
	 * ======
	 */
	
	/**
	 * Event driver to be used for word count.
	 */
	private EventDriver wordCounter;
	
	
	/* ============
	 * constructors
	 * ============
	 */
	
	/**
	 * Default unique words counter event driver constructor.
	 */
	public UniqueWordsCounterEventDriver() {
		wordCounter = new NaiveWordEventDriver();
	}
	
	/* ==================
	 * overriding methods
	 * ==================
	 */
	
	public String displayName() {
		return "Unique words count";
	}

	public String tooltipText() {
		return "The total number of unique words";
	}

	public boolean showInGUI() {
		return false;
	}

	public double getValue(Document doc) throws EventGenerationException {
		EventSet words = wordCounter.createEventSet(doc);		
		return (new EventHistogram(words)).getNTypes();
	}
}
