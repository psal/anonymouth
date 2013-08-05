package edu.drexel.psal.jstylo.eventDrivers;

import com.jgaap.eventDrivers.*;
import com.jgaap.generics.*;

public class CharCounterEventDriver extends SingleNumericEventDriver {

	/* ======
	 * fields
	 * ======
	 */
	
	/**
	 * Event driver to be used for character count.
	 */
	private CharacterEventDriver charDriver;
	
	
	/* ============
	 * constructors
	 * ============
	 */
	
	/**
	 * Default sentence counter event driver constructor.
	 */
	public CharCounterEventDriver() {
		charDriver = new CharacterEventDriver();
	}
	
	/* ==================
	 * overriding methods
	 * ==================
	 */
	
	public String displayName() {
		return "Character count";
	}

	public String tooltipText() {
		return "The total number of characters.";
	}

	public boolean showInGUI() {
		return false;
	}

	public double getValue(Document doc) {
		return doc.stringify().length();
	}
}
