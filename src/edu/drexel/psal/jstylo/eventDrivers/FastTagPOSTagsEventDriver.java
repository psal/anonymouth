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

public class FastTagPOSTagsEventDriver extends EventDriver {

	@Override
	public String displayName() {
		return "FastTag POS";
	}

	@Override
	public String tooltipText() {
		return "FastTag POS tagger, based on Brill's rule-based tagger";
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
		for (String tag: tags)
			es.addEvent(new Event(tag));
		
		return es;
	}
}
