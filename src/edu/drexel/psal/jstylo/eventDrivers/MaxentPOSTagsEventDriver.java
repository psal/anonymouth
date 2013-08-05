package edu.drexel.psal.jstylo.eventDrivers;

import com.jgaap.generics.*;
import com.jgaap.generics.Document;

import edu.drexel.psal.jstylo.generics.Logger;
import edu.drexel.psal.jstylo.generics.Logger.LogOut;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.tagger.maxent.MaxentTagger; 

import java.io.*;
import java.util.*;

/**
 * This changes words into their parts of speech in a document, based on Stanford's MaxentTagger.
 * 
 * @author Ariel Stolerman
 */

public class MaxentPOSTagsEventDriver extends EventDriver {

	@Override
	public String displayName() {
		return "Maxent POS tags";
	}

	@Override
	public String tooltipText() {
		return "Stanford Log-linear Part-Of-Speech Tagger";
	}

	@Override
	public boolean showInGUI() {
		return false;
	}
	
	protected static MaxentTagger tagger = null;
    protected static String taggerPath = "com/jgaap/resources/models/postagger/english-left3words-distsim.tagger";
	//protected static String taggerPath = "com/jgaap/resources/models/postagger/german-fast.tagger";
	
	public static String getTaggerPath() {
		return taggerPath;
	}

	public static void setTaggerPath(String taggerPath) {
		MaxentPOSTagsEventDriver.taggerPath = taggerPath;
	}

	@SuppressWarnings("static-access")
	@Override
	public EventSet createEventSet(Document doc) {
		EventSet es = new EventSet(doc.getAuthor());
		char[] text = doc.getProcessedText();
		String stringText = new String(text);
		
		// initialize tagger and return empty event set if encountered a problem
		if (tagger == null) {
			tagger = initTagger();
			if (tagger == null) return es;
		}

		List<List<HasWord>> sentences = tagger.tokenizeText(new BufferedReader(new StringReader(stringText)));
		for (List<HasWord> sentence : sentences) {
			ArrayList<TaggedWord> tSentence = tagger.tagSentence(sentence);
			for (TaggedWord tw: tSentence)
				es.addEvent(new Event(tw.tag()));
		}
		
		return es;
	}
	
	/**
	 * Initialize the tagger.
	 * @return
	 */
	public static MaxentTagger initTagger() {
		MaxentTagger t = null;
		try {
			//tagger = new MaxentTagger();
			
			t = new MaxentTagger(taggerPath);
		} catch (Exception e) {
			Logger.logln("MaxentTagger failed to load tagger from ",LogOut.STDERR);
			e.printStackTrace();
		}
		return t;
	}
}
