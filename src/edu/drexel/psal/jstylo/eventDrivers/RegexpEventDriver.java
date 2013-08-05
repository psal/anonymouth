package edu.drexel.psal.jstylo.eventDrivers;

import java.util.regex.*;

import com.jgaap.generics.*;

public class RegexpEventDriver extends EventDriver {

	/* ======
	 * fields
	 * ======
	 */
	
	private String regexp;
	
	/* ==================
	 * overriding methods
	 * ==================
	 */
	
	public String displayName() {
		return "Regular expression matches";
	}

	public String tooltipText() {
		return "The frequencies of all distinct matches of a given regular expression in the document. For instance, the regular " +
				"expression \"\\d\" will generate the features: \"0\", \"1\", ... \"9\".";
	}

	public boolean showInGUI() {
		return false;
	}
	
	@Override
	public EventSet createEventSet(Document doc) {
		EventSet es = new EventSet(doc.getAuthor());

		// set regexp parameter
		regexp = getParameter("regexp");
		if (regexp.equals(""))
			regexp = ".";

		Pattern p = Pattern.compile(regexp);
		Matcher m = p.matcher(doc.stringify());
		while (m.find())
			es.addEvent(new Event(m.group()));
		
		return es;
	}
}
