package edu.drexel.psal.anonymouth.utils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.drexel.psal.jstylo.generics.Logger;

/**
 * Finds the number of word bigrams and word trigrams in an input sentence. 
 * @author Andrew W.E. McDonald
 *
 */
public class GramMatcher {
	
	private final String NAME = "( "+this.getClass().getName()+" ) - ";

/*	
	public static void main(String args[]){
		GramMatcher gm = new GramMatcher();
		String bg1 = "(.)-(said)";
		String bg2 = "(then)-(.)";
		String tg1 = "(Oh)-(,)-(well)";
		String tg2 = "(good)-(though)-(.)";
		String sent1 = "\"Hello there!\", he said. I was thinking, \"Oh, well, I guess I'll talk to him then.\" This is good though. said"; 
		int num_bg1 = gm.getOccurrencesOfBigram(bg1,sent1);
		int num_bg2 = gm.getOccurrencesOfBigram(bg2,sent1);
		int num_tg1 = gm.getOccurrencesOfTrigram(tg1,sent1);
		int num_tg2 = gm.getOccurrencesOfTrigram(tg2,sent1);
		System.out.printf("The number of each feature found is:\nbg1 => %d\nbg2 => %d\ntg1 => %d\ntg2 => %d\n",num_bg1,num_bg2,num_tg1,num_tg2);
	}
	*/
	
	/**
	 * 
	 * @param wordBigram
	 * @param stringToSearch
	 * @return
	 */
	public int getOccurrencesOfBigram(String wordBigram, String stringToSearch){
		//String theDoc = document;
		//theDoc = theDoc.replaceAll("[“”‘’„˚˙‚’‘`*$%@#~\\r\\n\\t.?!\",;:()\\[\\]\\\\]"," ");

		String theWords = wordBigram.replaceAll("\\p{C}", " ");
		//System.out.println(theWords);
		
		String[] split = wordBigram.split("-");
		int numTokes = split.length;
		int tokeLen;
		int numTokesMinusOne = numTokes-1;
		String[] tokens = new String[numTokes];
		int totalBigramLen = 0; // the total length includes one space between the words/characters (because there had to be a space in the document for it to be considered a bigram and not a word)
		boolean sent_end = false;
		int i;
		for(i=0; i < numTokes; i++){
			tokeLen = split[i].length();
			totalBigramLen += tokeLen;
				
			tokens[i] = split[i].substring(1,tokeLen-1);
			if(i != numTokesMinusOne)
				totalBigramLen += 1; // add a space to the count 
			if(tokens[i].contains(".") && i != numTokesMinusOne)
				tokens[i] = tokens[i].replace(".", "\\.");
			else if( tokens[i].contains(".") && i == numTokesMinusOne){
				tokens[i] = tokens[i].replace(".", "\\.");
				sent_end = true;
			}
			if(tokens[i].contains("?") && i != numTokesMinusOne)
				tokens[i] = tokens[i].replace("?", "\\?");
			else if( tokens[i].contains("?") && i == numTokesMinusOne){
				tokens[i] = tokens[i].replace("?", "\\?");
				sent_end = true;
			}
		}
		
		if(totalBigramLen > stringToSearch.length())
			return 0;
	
		if(sent_end)
			return findIndices(stringToSearch,"\\b"+tokens[0]+"\\s*"+tokens[1]).size();
		else
			return findIndices(stringToSearch,"\\b"+tokens[0]+"\\s*"+tokens[1]+"(\\s|\\b){1}+").size();
			
		
	}
	
	
	/**
	 * Finds indices of the starting and ending locations of each place the given regular expression is found in the given string
	 * @param stringToTest the string/document to test the regex on
	 * @param theRegEx the expression to find
	 * @return
	 * 	ArrayList containing the starting and ending indices of each match.
	 */

	public static ArrayList<int[]> findIndices(String stringToTest, String theRegEx){	
		ArrayList<int[]> toHighlight = new ArrayList<int[]>();
		//int lengthRegEx = theRegEx.length();
		stringToTest = stringToTest.replaceAll("\\p{C}", " ");// replaces all unicode control / invisible  characters and unused code points
		Pattern p = Pattern.compile(theRegEx);
		Matcher m = p.matcher(stringToTest);
		int startIndex=-1;
		while(true){
		boolean found = m.find(startIndex+1);
		if(found == true){
			int start = m.start();
			int end = m.end();
//			System.out.println("Found match for: '"+theRegEx+"' => "+stringToTest.substring(start,end));
			toHighlight.add(new int[]{start,end});
			startIndex=start;
			m.reset();
		}
		else
			break;
		}
		return toHighlight;
		
	}
	

	
	
	
	public int getOccurrencesOfTrigram(String wordTrigram, String stringToSearch){
		//String theDoc = document;
		//theDoc = theDoc.replaceAll("[“”‘’„˚˙‚’‘`*$%@#~\\r\\n\\t.?!\",;:()\\[\\]\\\\]"," ");

		String theWords = wordTrigram.replaceAll("\\p{C}", " ");
		//System.out.println("Finding trigram: "+theWords);
		String[] split = wordTrigram.split("-");
		int numTokes = split.length;
		int tokeLen;
		int numTokesMinusOne = numTokes-1;
		String[] tokens = new String[numTokes];
		int totalTrigramLen = 0; // this count includes 1 space between words/characters (otherwise the feature wouldn't be a trigram)
		boolean sent_end = false;
		int i;
		for(i=0; i < numTokes; i++){
			tokeLen = split[i].length();
			totalTrigramLen += tokeLen;
			if(i != numTokesMinusOne)
				totalTrigramLen += 1;// add a space to the count
			tokens[i] = split[i].substring(1,tokeLen-1);
			if(tokens[i].contains(".") && i != numTokesMinusOne)
				tokens[i] = tokens[i].replace(".", "\\.");
			else if( tokens[i].contains(".") && i == numTokesMinusOne){
				tokens[i] = tokens[i].replace(".", "\\.");
				sent_end = true;
			}
			if(tokens[i].contains("?") && i != numTokesMinusOne)
				tokens[i] = tokens[i].replace("?", "\\?");
			else if( tokens[i].contains("?") && i == numTokesMinusOne){
				tokens[i] = tokens[i].replace("?", "\\?");
				sent_end = true;
			}
		}	
		
		if(totalTrigramLen > stringToSearch.length())
			return 0;
		
		if(sent_end)
			return findIndices(stringToSearch,"\\b"+tokens[0]+"\\s*"+tokens[1]+"\\s*"+tokens[2]).size();
		else
			return findIndices(stringToSearch,"\\b"+tokens[0]+"\\s*"+tokens[1]+"\\s*"+tokens[2]+"\\b{1}+").size();
	}
	

	

}
