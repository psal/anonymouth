package edu.drexel.psal.jstylo.canonicizers;

import java.util.Scanner;

import com.jgaap.generics.Canonicizer;

/** 
 * Adds spaces before and after punctuation that is at an end or beginning of a word.
 */
public class WordEndsPunctSeparator extends Canonicizer {

	@Override
	public String displayName() {
		return "Word-Edges Punctuation Separator";
	}

	@Override
	public String tooltipText() {
		return "Whitespace pad all punctuation that is at a beginning or an end of a word";
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
     * Separate punctuation by inserting  whitespace into argument -
     * only at beginning or end of word
     *
     * @param procText
     *            Array of Characters to be processed
     * @return Array of Characters after separating punctuation with 
     *         single space.
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
				if (token.matches("\\S+\\p{Punct}"))
					newToken = newToken.substring(0, newToken.length()-1)+" "+newToken.substring(newToken.length()-1);
				if (token.matches("\\p{Punct}\\S+"))
					newToken = newToken.substring(0, 1)+" "+newToken.substring(1,newToken.length());
				if (!token.equals(newToken))
					line = line.replace(token, newToken);
			}
			res += line;
		}
		return res.toCharArray();
	}
	
	public static void main(String[] args) {
		String s = " Hi there! how are you, man? I haven't been here before, have I? hi .again.";
		String res = String.valueOf((new WordEndsPunctSeparator()).process(s.toCharArray()));
		System.out.println(s);
		System.out.println(res);
	}
}
