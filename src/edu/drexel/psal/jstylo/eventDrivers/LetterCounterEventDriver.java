package edu.drexel.psal.jstylo.eventDrivers;

import com.jgaap.eventDrivers.*;
import com.jgaap.generics.*;

public class LetterCounterEventDriver extends SingleNumericEventDriver {

	/* ======
	 * fields
	 * ======
	 */
	
	/**
	 * Event driver to be used for character count.
	 */
	private EventDriver charDriver;
	

	/* ============
	 * constructors
	 * ============
	 */
	
	/**
	 * Default sentence counter event driver constructor.
	 */
	public LetterCounterEventDriver() {
		charDriver = new CharacterEventDriver();
	}
	
	/* ==================
	 * overriding methods
	 * ==================
	 */
	
	public String displayName() {
		return "Letter count";
	}

	public String tooltipText() {
		return "The total number of letters.";
	}

	public boolean showInGUI() {
		return false;
	}

	public double getValue(Document doc) throws EventGenerationException {
		EventSet chars = charDriver.createEventSet(doc);
		for (int i=chars.size()-1; i>=0; i--) {
			Event e = chars.eventAt(i);
			if (!e.toString().matches("[A-Za-z]"))
				chars.removeEvent(e);
		}
		return chars.size();
	}
}
