package edu.drexel.psal.jstylo.eventDrivers;

import java.io.*;
import java.util.*;

import com.jgaap.generics.*;

public class WordLengthEventDriver extends EventDriver {
	
	/* ==================
	 * overriding methods
	 * ==================
	 */
	
	public String displayName() {
		return "Word Lengths";
	}

	public String tooltipText() {
		return "The frequencies of all distinct word lengths in the document.";
	}

	public boolean showInGUI() {
		return false;
	}
	
	@Override
	public EventSet createEventSet(Document doc) {
		EventSet es = new EventSet(doc.getAuthor());

		Scanner scan = new Scanner(new StringReader(doc.stringify()));
		while (scan.hasNext())
			es.addEvent(new Event(""+scan.next().length()));
		
		return es;
	}
}
