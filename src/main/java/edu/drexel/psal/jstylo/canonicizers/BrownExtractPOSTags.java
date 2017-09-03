package edu.drexel.psal.jstylo.canonicizers;

import java.util.Scanner;
import com.jgaap.generics.Canonicizer;

/** 
 * Extracts POS-tags from the brown corpus.
 * 
 * @author Ariel Stolerman
 */
public class BrownExtractPOSTags extends Canonicizer {

	@Override
	public String displayName(){
		return "Brown Corpus - extract POS tags";
	}

	@Override
	public String tooltipText(){
		return "Extract all POS tags from the Brown corpus documents.";
	}
	
	@Override
	public boolean showInGUI(){
		return true;
	}

	/**
	 * Extract only POS tags (omit words) from the Brown corpus documents.
	 * @param procText Array of characters to be processed.
	 * @return Array of processed characters.
	 */
	@Override
	public char[] process(char[] procText) {
		String procString = new String(procText);
		Scanner scan = new Scanner(procString);
		String resString = "";
		while (scan.hasNext()) {
			String line = scan.nextLine();
			resString += line.replaceAll("\\S+/", "")+"\n";
		}
		resString = resString.toUpperCase();
		return resString.toCharArray();
	}
	
	/*
	public static void main(String[] args) throws Exception {
		Canonicizer c = new BrownExtractPOSTags();
		c = new BrownExtractText();
		Scanner scan = new Scanner(new File("./ca09"));
		Canonicizer punct = new StripPunctuation();
		String text = "";
		while (scan.hasNext())
			text += scan.nextLine()+"\n";
		
		char[] raw = text.toCharArray();
		char[] rawParsed = c.process(raw);
		String parsed = String.valueOf(rawParsed);
		//parsed = String.valueOf(punct.process(parsed.toCharArray()));
		
		System.out.println("text");
		System.out.println("===========================");
		System.out.println(text);
		System.out.println();
		System.out.println("parsed");
		System.out.println("===========================");
		System.out.println(parsed);
	}
	*/
}
