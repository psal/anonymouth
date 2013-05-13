package edu.drexel.psal.jstylo.eventDrivers;

import com.jgaap.generics.*;
import com.jgaap.generics.Document;

import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.tagger.maxent.MaxentTagger; 

import java.io.*;
import java.util.*;

/**
 * This changes words into their parts of speech in a document, based on Stanford's MaxentTagger.
 * 
 * @author Ariel Stolerman
 */

public class MaxentPOSNGramsEventDriver extends EventDriver {

	@Override
	public String displayName() {
		return "Maxent POS N-Grams";
	}

	@Override
	public String tooltipText() {
		return "Stanford Log-linear Part-Of-Speech Tagger for POS N-grams";
	}

	@Override
	public boolean showInGUI() {
		return false;
	}

	private static MaxentTagger tagger = null;
	protected static String taggerPath = "";
	
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

		// use MaxentPOSTagsEventDriver's tagger
		// initialize tagger and return empty event set if encountered a problem
		if (tagger == null) {
			tagger = MaxentPOSTagsEventDriver.initTagger();
			if (tagger == null) return es;
		}
		

		List<List<HasWord>> sentences = tagger.tokenizeText(new BufferedReader(new StringReader(stringText)));
		ArrayList<TaggedWord> tagged = new ArrayList<TaggedWord>();
		for (List<HasWord> sentence : sentences)
			tagged.addAll(tagger.tagSentence(sentence));
		
		int i,j,n;
		try {
			n = Integer.parseInt(getParameter("N"));
		} catch (NumberFormatException e) {
			n = 2;
		}
		String curr;
		for (i=0; i<tagged.size()-n+1; i++) {
			curr = "("+tagged.get(i).tag()+")";
			for (j=1; j<n; j++) {
				curr += "-("+tagged.get(i+j).tag()+")";
			}
			es.addEvent(new Event(curr));
		}
		
		return es;
	}
	
	/*
	// main for testing
	public static void main(String[] args) throws Exception {
		Document doc = new Document("./corpora/drexel_1/a/a_01.txt","a","a_01.txt");
		doc.load();
		MaxentPOSNGramsEventDriver m = new MaxentPOSNGramsEventDriver();
		m.setParameter("N", 2);
		EventSet es = m.createEventSet(doc);
		for (int i=0; i<es.size(); i++) {
			System.out.print(es.eventAt(i)+" ");
			if (i % 30 == 0) System.out.println();
		}
	}
	*/
}
