package edu.drexel.psal.jstylo.eventDrivers;

import com.jgaap.generics.*;

public class FleschReadingEaseScoreEventDriver extends SingleNumericEventDriver {

	/* ======
	 * fields
	 * ======
	 */
	
	/**
	 * Event drivers to be used.
	 */
	private SingleNumericEventDriver wordCounter;
	
	private SingleNumericEventDriver sentenceCounter;
	
	private SingleNumericEventDriver syllablesCounter;
	
	
	/* ============
	 * constructors
	 * ============
	 */
	
	/**
	 * Default Gunning-Fog readability index event driver constructor.
	 */
	public FleschReadingEaseScoreEventDriver() {
		wordCounter = new WordCounterEventDriver();
		sentenceCounter = new SentenceCounterEventDriver();
		syllablesCounter = new SyllableCounterEventDriver();
	}
	
	/* ==================
	 * overriding methods
	 * ==================
	 */
	
	public String displayName() {
		return "Flesch Reading Ease Score";
	}

	public String tooltipText() {
		return "Flesch Reading Ease Score";
	}

	public boolean showInGUI() {
		return false;
	}

	public double getValue(Document doc) throws EventGenerationException {
		double wordCount = wordCounter.getValue(doc);
		double sentenceCount = sentenceCounter.getValue(doc);
		double syllableCount = syllablesCounter.getValue(doc);
		return 206.835 - 1.015*wordCount/sentenceCount - 84.6*syllableCount/wordCount;
	}
}
