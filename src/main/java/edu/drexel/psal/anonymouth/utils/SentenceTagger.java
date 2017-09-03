package edu.drexel.psal.anonymouth.utils;

import java.io.IOException;

import com.jgaap.JGAAPConstants;

import edu.drexel.psal.jstylo.generics.Logger;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class SentenceTagger {
	
	private final String NAME = "( "+this.getClass().getName()+" ) - ";
	MaxentTagger mt;
	

	public SentenceTagger(){
		try {
			mt = new MaxentTagger("."+JGAAPConstants.JGAAP_RESOURCE_PACKAGE+"models/postagger/english-left3words-distsim.tagger");
		} catch (IOException e) {
			Logger.logln(NAME+"IOException in SentenceTagger. Can't open 'english-left3words-distim.tagger'.");
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		
	}
	
	public String tag(String text){
		return mt.tagString(text);
	}
	
	

}
