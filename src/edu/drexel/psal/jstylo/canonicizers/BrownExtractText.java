package edu.drexel.psal.jstylo.canonicizers;

import java.util.Scanner;
import com.jgaap.generics.Canonicizer;

/** 
 * Extracts words from the Brown corpus (omits all POS-tags).
 * 
 * @author Ariel Stolerman
 */
public class BrownExtractText extends Canonicizer {

	@Override
	public String displayName(){
		return "Brown Corpus - extract words";
	}

	@Override
	public String tooltipText(){
		return "Extract all words (omit POS-tags) from the Brown corpus documents.";
	}
	
	@Override
	public boolean showInGUI(){
		return true;
	}

	/**
	 * Extract only words (omit POS-tags) from the Brown corpus documents.
	 * @param procText Array of characters to be processed.
	 * @return Array of processed characters.
	 */
	public char[] process(char[] procText) {
		String procString = new String(procText);
		Scanner scan = new Scanner(procString);
		String resString = "";
		while (scan.hasNext()) {
			String line = scan.nextLine();
			resString += line.replaceAll("/\\S+", "")+"\n";
		}
		return resString.toCharArray();
	}
}
