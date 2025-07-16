package edu.drexel.psal.anonymouth.utils;

import java.io.IOException;

import edu.drexel.psal.ANONConstants;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Tagger {

	private static final Logger logger = LogManager.getLogger(Tagger.class);
	@SuppressWarnings("unused")
	private final String NAME = "( " + this.getClass().getSimpleName() + " ) - ";
	public static MaxentTagger mt = null;

	public Tagger() {
		initTagger();
	}

	/**
	 * Initializes MaxentTagger (thread-safe)
	 * 
	 * @return true if successful, false otherwise
	 */
	public static synchronized boolean initTagger() {
		if (mt != null) {
			return true; // Already initialized
		}
		
		String taggerPath = ANONConstants.EXTERNAL_RESOURCE_PACKAGE + "english-left3words-distsim.tagger";
		logger.info("( Tagger ) - Attempting to initialize Stanford POS tagger from: " + taggerPath);
		
		java.io.File taggerFile = new java.io.File(taggerPath);
		if (!taggerFile.exists()) {
			logger.error("( Tagger ) - ERROR: Stanford POS tagger model file not found at: " + taggerPath);
			return false;
		}
		
		try {
			mt = new MaxentTagger(taggerPath);
			logger.info("( Tagger ) - Stanford POS tagger initialized successfully");
			return true;
		} catch (IOException e) {
			logger.error("( Tagger ) - IOException while initializing Stanford POS tagger: " + e.getMessage(), e);
		} catch (ClassNotFoundException e) {
			logger.error("( Tagger ) - ClassNotFoundException while initializing Stanford POS tagger: " + e.getMessage(), e);
		} catch (Exception e) {
			logger.error("( Tagger ) - Unexpected error while initializing Stanford POS tagger: " + e.getMessage(), e);
		}
		return false;
	}
	
	/**
	 * Returns the initialized Stanford POS tagger (thread-safe)
	 * 
	 * @return MaxentTagger instance or null if not initialized
	 */
	public static synchronized MaxentTagger getTagger() {
		return mt;
	}

}
