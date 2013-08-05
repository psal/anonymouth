package edu.drexel.psal.jstylo.eventDrivers;

import com.jgaap.eventDrivers.CharacterNGramEventDriver;
import com.jgaap.generics.*;

public class LetterNGramEventDriver extends CharacterNGramEventDriver {
	
	@Override
	public String displayName() {
		return "Letter N-Grams";
	}

	@Override
	public String tooltipText() {
		return "Groups of N successive letters";
	}

	@Override
	public String longDescription() {
		return "Groups of N successive letters (sliding window); N is given as a parameter.";
	}

	@Override
	public EventSet createEventSet(Document document) {
		char[] text = document.getProcessedText();
		int n;
		try {
			n = Integer.parseInt(getParameter("N"));
		} catch (NumberFormatException e) {
			n = 2;
		}
		EventSet eventSet = new EventSet(text.length);
		String curr;
		for (int i = 0; i <= text.length - n; i++) {
			curr = new String(text, i, n);
			if (curr.matches("[A-Za-z]+"))
				eventSet.addEvent(new Event(curr));
		}
		return eventSet;
	}
}
