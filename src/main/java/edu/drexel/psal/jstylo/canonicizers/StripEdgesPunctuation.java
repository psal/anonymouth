package edu.drexel.psal.jstylo.canonicizers;

import java.util.Scanner;

import com.jgaap.generics.Canonicizer;

/** 
 * Removes punctuation that is at an end or beginning of a word.
 */
public class StripEdgesPunctuation extends Canonicizer {

	@Override
	public String displayName() {
		return "Word-edges Punctuation Stripper";
	}

	@Override
	public String tooltipText() {
		return "Removes all punctuation that is at a beginning or an end of a word";
	}

	@Override
	public String longDescription() {
		return tooltipText();
	}

	@Override
	public boolean showInGUI() {
		return false;
	}

    /**
     * Remove punctuation at edges of words.
     * @param procText
     *            Array of Characters to be processed
     * @return Array of Characters after removing punctuation at word edges.
     */
	@Override
	public char[] process(char[] procText) {
		String text = String.valueOf(procText)+"\n";
		String res = "";
		Scanner scan = new Scanner(text);
		Scanner lineScan;
		String line;
		String token, newToken;
		while (scan.hasNext()) {
			line = scan.nextLine()+"\n";
			lineScan = new Scanner(line);
			while (lineScan.hasNext()) {
				token = lineScan.next();
				newToken = token;
				newToken = newToken.replaceFirst("^\\p{Punct}+", "");
				newToken = newToken.replaceFirst("\\p{Punct}+$", "");		
				line = line.replace(token, newToken);
			}
			res += line;
		}
		return res.toCharArray();
	}
	
	public static void main(String[] args) {
		String s = " Hi there! how are you, man?. I haven't been , here before, have I? hi .again.";
		String res = String.valueOf((new StripEdgesPunctuation()).process(s.toCharArray()));
		System.out.println(s);
		System.out.println(res);
	}
}
