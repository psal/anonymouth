package edu.drexel.psal.jstylo.eventDrivers;

import com.jgaap.eventDrivers.*;
import com.jgaap.generics.*;

public class SentenceCounterEventDriver extends SingleNumericEventDriver {

	/* ======
	 * fields
	 * ======
	 */
	
	/**
	 * Event driver to be used for word count.
	 */
	private EventDriver sentencesDriver;
	
	
	/* ============
	 * constructors
	 * ============
	 */
	
	/**
	 * Default sentence counter event driver constructor.
	 */
	public SentenceCounterEventDriver() {
		sentencesDriver = new SentenceEventDriver();
	}
	
	/* ==================
	 * overriding methods
	 * ==================
	 */
	
	public String displayName() {
		return "Sentence count";
	}

	public String tooltipText() {
		return "The total number of sentences";
	}

	public boolean showInGUI() {
		return false;
	}

	public double getValue(Document doc) throws EventGenerationException {
		return sentencesDriver.createEventSet(doc).size();
	}
}
