package edu.drexel.psal.jstylo.eventDrivers;

import com.jgaap.eventDrivers.WordSyllablesEventDriver;
import com.jgaap.generics.*;

public class SyllableCounterEventDriver extends SingleNumericEventDriver {

	/* ======
	 * fields
	 * ======
	 */
	
	/**
	 * Event driver to be used for syllable count.
	 */
	private EventDriver syllablesDriver;
	
	
	/* ============
	 * constructors
	 * ============
	 */
	
	/**
	 * Default syllables counter event driver constructor.
	 */
	public SyllableCounterEventDriver() {
		syllablesDriver = new WordSyllablesEventDriver();
	}
	
	/* ==================
	 * overriding methods
	 * ==================
	 */
	
	public String displayName() {
		return "Syllables count";
	}

	public String tooltipText() {
		return "The total number of syllables";
	}

	public boolean showInGUI() {
		return false;
	}

	public double getValue(Document doc) throws EventGenerationException {
		EventSet syllables = syllablesDriver.createEventSet(doc);
		int i,sum = 0;
		for (i=0; i<syllables.size(); i++)
			sum += Integer.parseInt(syllables.eventAt(i).toString());
		return sum; 
	}
}
