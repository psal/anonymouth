package edu.drexel.psal.jstylo.eventDrivers;

import com.jgaap.eventDrivers.*;
import com.jgaap.generics.*;

public class GunningFogIndexEventDriver extends SingleNumericEventDriver {

	/* ======
	 * fields
	 * ======
	 */
	
	/**
	 * Event drivers to be used.
	 */
	private SingleNumericEventDriver wordCounter;
	
	private SingleNumericEventDriver sentenceCounter;
	
	private EventDriver syllablesDriver;
	
	
	/* ============
	 * constructors
	 * ============
	 */
	
	/**
	 * Default Gunning-Fog readability index event driver constructor.
	 */
	public GunningFogIndexEventDriver() {
		wordCounter = new WordCounterEventDriver();
		sentenceCounter = new SentenceCounterEventDriver();
		syllablesDriver = new WordSyllablesEventDriver();
	}
	
	/* ==================
	 * overriding methods
	 * ==================
	 */
	
	public String displayName() {
		return "Gunning-Fog Readability Index";
	}

	public String tooltipText() {
		return "Gunning-Fog Readability Index";
	}

	public boolean showInGUI() {
		return false;
	}

	public double getValue(Document doc) throws EventGenerationException {
		double wordCount = wordCounter.getValue(doc);
		double sentenceCount = sentenceCounter.getValue(doc);
		EventSet syllables = syllablesDriver.createEventSet(doc);
		for (int i=syllables.size()-1; i>=0; i--) {
			if (Integer.parseInt(syllables.eventAt(i).toString()) < 3)
				syllables.removeEvent(syllables.eventAt(i));
		}
		double complexWordsCount = syllables.size();
		return 0.4*(wordCount/sentenceCount + 100*complexWordsCount/wordCount);
	}
}
