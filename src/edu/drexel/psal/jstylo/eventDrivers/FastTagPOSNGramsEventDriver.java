package edu.drexel.psal.jstylo.eventDrivers;

import com.jgaap.generics.*;
import com.jgaap.generics.Document;
import com.knowledgebooks.nlp.fasttag.FastTag;
import com.knowledgebooks.nlp.util.Tokenizer;

import java.io.*;
import java.util.*;

/**
 * This changes words into their parts of speech in a document, based on FastTag.
 * 
 * @author Ariel Stolerman
 */

public class FastTagPOSNGramsEventDriver extends EventDriver {

	@Override
	public String displayName() {
		return "FastTag POS N-Grams";
	}

	@Override
	public String tooltipText() {
		return "FastTag POS tagger, based on Brill's rule-based tagger for POS N-grams";
	}

	@Override
	public boolean showInGUI() {
		return false;
	}

	private FastTag tagger = null;
	
	@Override
	public EventSet createEventSet(Document doc) {
		EventSet es = new EventSet(doc.getAuthor());
		char[] text = doc.getProcessedText();
		String stringText = new String(text);
		Scanner scan = new Scanner(new StringReader(stringText));

		if (tagger == null)
			tagger = new FastTag();

		String line;
		List<String> words = new ArrayList<String>(), tags;
		while (scan.hasNext()) {
			line = scan.nextLine();
			words.addAll(Tokenizer.wordsToList(line));
		}
		tags = tagger.tag(words);
		
		int i,j,n;
		try {
			n = Integer.parseInt(getParameter("N"));
		} catch (NumberFormatException e) {
			n = 2;
		}
		String curr;
		for (i=0; i<tags.size()-n+1; i++) {
			curr = "("+tags.get(i)+")";
			for (j=1; j<n; j++) {
				curr += "-("+tags.get(i+j)+")";
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
		FastTagPOSNGramsEventDriver m = new FastTagPOSNGramsEventDriver();
		m.setParameter("N", 2);
		EventSet es = m.createEventSet(doc);
		for (int i=0; i<es.size(); i++) {
			System.out.print(es.eventAt(i)+" ");
			if (i % 30 == 0) System.out.println();
		}
	}
	*/
}
