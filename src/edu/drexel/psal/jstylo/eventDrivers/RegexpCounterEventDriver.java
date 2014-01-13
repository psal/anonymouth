package edu.drexel.psal.jstylo.eventDrivers;

import java.util.regex.*;

import com.jgaap.generics.*;

public class RegexpCounterEventDriver extends SingleNumericEventDriver {

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
		return "Regular expression match counter";
	}

	public String tooltipText() {
		return "The frequency of matches of a given regular expression in the document.";
	}

	public boolean showInGUI() {
		return false;
	}

	public double getValue(Document doc) {
		// set regexp parameter
		regexp = getParameter("regexp");
		if (regexp.equals(""))
			regexp = ".";
		
		Pattern p = Pattern.compile(regexp);
		Matcher m = p.matcher(doc.stringify());
		int count = 0;
		while (m.find())
			count++;
		
		return count;
	}
}
