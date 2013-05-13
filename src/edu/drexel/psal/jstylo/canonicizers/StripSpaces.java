package edu.drexel.psal.jstylo.canonicizers;

import com.jgaap.generics.Canonicizer;

/** 
 * Strips any whitespace from the document.
 * Based on com.JGAAP.canonicizers.StripPunctuation.
 */
public class StripSpaces extends Canonicizer {
	
    @Override
    public String displayName(){
    	return "Strip Spaces";
    }
    
    @Override
    public String tooltipText(){
    	return "Strip all space characters from the text.";
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
    	String procString = new String(procText);
    	procString = procString.replaceAll("\\s+", "");
    	return procString.toCharArray();
    }
}
