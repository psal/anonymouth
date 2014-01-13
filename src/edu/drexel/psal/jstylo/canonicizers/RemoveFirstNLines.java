package edu.drexel.psal.jstylo.canonicizers;

import com.jgaap.generics.Canonicizer;

/** 
 * Removes first N lines from the document.
 */
public class RemoveFirstNLines extends Canonicizer {

	public RemoveFirstNLines() {
		addParams("numLines", "N", "1", new String[]{"1","2","3","4","5","10"}, true);
	}
	
    @Override
    public String displayName(){
    	return "Remove first N lines";
    }
    
    @Override
    public String tooltipText(){
    	return "Remove first N lines from the text.";
    }
    
    @Override
    public boolean showInGUI(){
    	return true;
    }

    /**
     * Strip space from input characters
     * @param procText Array of characters to be processed.
     * @return Array of processed characters.
     */
    @Override
    public char[] process(char[] procText) {
    	int numLines;
    	if(!getParameter("numLines").equals(""))
    		numLines = Integer.parseInt(getParameter("numLines"));
    	else
    		numLines = 1;

    	String procString = new String(procText);
    	int i=0;
    	while (i<numLines && !procString.isEmpty()) {
    		procString = procString.replaceFirst(".*\\n", "");
    		i++;
    	}
    	return procString.toCharArray();
    }
}
