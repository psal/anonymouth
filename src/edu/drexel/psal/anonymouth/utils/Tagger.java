package edu.drexel.psal.anonymouth.utils;

import java.io.IOException;

import com.jgaap.JGAAPConstants;

import edu.drexel.psal.JSANConstants;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class Tagger {
	
	private final String NAME = "( "+this.getClass().getName()+" ) - ";
	public static MaxentTagger mt = null;
	
	public Tagger(){
		initTagger();
	}
	
	/**
	 * Initializes MaxentTagger
	 * @return true if successful, false otherwise
	 */
	public static boolean initTagger(){
		try {
			//mt = new MaxentTagger("."+JGAAPConstants.JGAAP_RESOURCE_PACKAGE+"models/postagger/english-left3words-distsim.tagger");
			mt = new MaxentTagger(JSANConstants.JSAN_EXTERNAL_RESOURCE_PACKAGE+"english-left3words-distsim.tagger");
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return false;
	}

}
