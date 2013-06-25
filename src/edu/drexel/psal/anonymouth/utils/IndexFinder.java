package edu.drexel.psal.anonymouth.utils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.drexel.psal.jstylo.generics.Logger;

/**
 * Finds the indices of sections of the Author's document to be modified that need to be highlighted
 * @author Andrew W.E. McDonald
 *
 */
public class IndexFinder {
	
	/**
	 * Finds indices of the words in LinkedList<String> theList in the String theDoc
	 * @param theDoc
	 * @param theList
	 * @return
	 * 	ArrayList<int[]> with the starting and ending indices of all words in 'theList' that were found in 'theDoc'
	 */
	public static ArrayList<int[]> findIndices(String theDoc, String theWord){
		ArrayList<int[]> theIndices = new ArrayList<int[]>();
		int i;
		int j;
		int start;
		int end;
		int length = theDoc.length();
		String spaces=" ";
		theDoc = theDoc.replaceAll("\\p{Cf}", " ");
		try{
			while (true) {
				Pattern wordToFind = Pattern.compile("((\\s|\\b)("+theWord+")(\\s|\\b)){1}+");
				Matcher theMatch = wordToFind.matcher(theDoc);
				
				if (theMatch.find() == false)
					break;

				if( theMatch.group(2).matches("\\s"))
					start = theMatch.start()+1;
				else
					start = theMatch.start();
				if(theMatch.group(4).matches("\\s"))
					end = theMatch.end()-1;
				else
					end = theMatch.end();
				theIndices.add(new int[]{start,end});
				spaces = " ";
				for(j=1;j<theWord.length();j++)
					spaces = spaces +" ";
				String theDocOne = theDoc.substring(0,start);
				String theDocTwo = theDoc.substring(end);
				theDoc = theDocOne+spaces+theDocTwo;
			}
		}catch(IllegalStateException e){
			Logger.logln("'"+theWord+"' - was not matched. Are there symbols or spaces between the single quotes? If so, that may be why.");
		}
		return theIndices;
	}
	
	/**
	 * Finds indices of symbols in the document
	 * @param theDoc 
	 * @param theList
	 * @return
	 * 	ArrayList of indices
	 */
	public static ArrayList<int[]> findSymbolIndices(String theDoc, LinkedList<String> theList){
		ArrayList<int[]> theIndices = new ArrayList<int[]>();
		int listLength = theList.size();
		int i;
		int j;
		int start;
		int end;
		String temp;
		String spaces=" ";
		theDoc = theDoc.replaceAll("\\p{C}", " ");
		for(i=0;i<listLength;i++){
			temp = theList.pop().replaceAll("\\p{C}", " ");
			if(temp.contains("."))
				temp =temp.replace(".", "\\.");
			if(temp.contains("?"))
				temp = temp.replace("?","\\?"); 
			try{
			Pattern wordToFind = Pattern.compile(temp);
			Matcher theMatch = wordToFind.matcher(theDoc);
			theMatch.find();
			start = theMatch.start();
			end = theMatch.end();
			theIndices.add(new int[]{start,end});
			spaces = " ";
			for(j=1;j<temp.length();j++)
				spaces = spaces +" ";
			String theDocOne = theDoc.substring(0,start);
			String theDocTwo = theDoc.substring(end);
			theDoc = theDocOne+spaces+theDocTwo;
			}catch(IllegalStateException e){
				Logger.logln("'"+temp+"' - was not matched. Are there symbols or spaces between the single quotes? If so, that may be why.");

			}
		}
		return theIndices;
		}
		
	/**
	 * Finds indices of the starting and ending locations of each place the given regular expression is found in the given string
	 * @param stringToTest the string/document to test the regex on
	 * @param theRegEx the expression to find
	 * @return
	 * 	ArrayList containing the starting and ending indices of each match.
	 */
	public static ArrayList<int[]> findRegEx(String stringToTest, String theRegEx){	
		ArrayList<int[]> toHighlight = new ArrayList<int[]>();
		//int lengthRegEx = theRegEx.length();
		stringToTest = stringToTest.replaceAll("\\p{C}", " ");
		Pattern p = Pattern.compile(theRegEx);
		Matcher m = p.matcher(stringToTest);
		int startIndex=-1;
		while(true){
		boolean found = m.find(startIndex+1);
		if(found == true){
			int start = m.start();
			int end = m.end();
			//System.out.println(index);
			toHighlight.add(new int[]{start,end});
			startIndex=start;
			m.reset();
		}
		else
			break;
		}
		return toHighlight;
		
	}
	
	/**
	 * Finds indices of word bigrams in the input string (document), and returns an Arraylist of integer arrays of starting and ending indices.
	 * @param theDoc
	 * @param theList
	 * @return
	 */
	public static ArrayList<int[]> findWordBigramIndices(String theDoc, LinkedList<String> theList){
		ArrayList<int[]> theIndices = new ArrayList<int[]>();
		int listLength = theList.size();
		int i;
		int j;
		int start;
		int end;
		String wordOne;
		String wordTwo;
		int listLengthDivTwo = listLength/2; // unless something odd happens, this number will always be a multiple of 2
		String spaces=" ";
		theDoc = theDoc.replaceAll("\\p{C}", " ");
		for(i=0;i<listLengthDivTwo;i++){
			wordOne = theList.pollLast().replaceAll("\\p{C}", " ");
			wordTwo = theList.pollLast().replaceAll("\\p{C}", " ");
			//System.out.println("wordOne: '"+wordOne+"' and wordTwo: '"+wordTwo+"'");
			if(wordOne.contains("."))
				wordOne = wordOne.replace(".","\\.");
			if(wordOne.contains("?"))
				wordOne = wordOne.replace("?","\\?");
			if(wordTwo.contains("."))
				wordTwo = wordTwo.replace(".","\\.");
			if(wordTwo.contains("?"))
				wordTwo = wordTwo.replace("?","\\?");
			try{

			Pattern wordToFind = Pattern.compile("((\\s|\\b)("+wordOne+"\\s*"+wordTwo+")(\\s|\\b)){1}+");

			Matcher theMatch = wordToFind.matcher(theDoc);
			theMatch.find();
			if( theMatch.group(2).matches("\\s"))
				start = theMatch.start()+1;
			else
				start = theMatch.start();
			if(theMatch.group(4).matches("\\s"))
				end = theMatch.end()-1;
			else
				end = theMatch.end();
			theIndices.add(new int[]{start,end});
			spaces = " ";
			int lenSpaces = end-start;
			for(j=1;j<lenSpaces;j++)
				spaces = spaces +" ";
			String theDocOne = theDoc.substring(0,start);
			String theDocTwo = theDoc.substring(end);
			theDoc = theDocOne+spaces+theDocTwo;
			}catch(IllegalStateException e){
				Logger.logln("'"+wordOne+" "+wordTwo+"' -was not matched. if there is more than 1 space or symbols exist within the single quotes, that may be why.");
			}
		}
		return theIndices;
		}

	/**
	 * Finds word trigram indices in the input string (document), and returns an Arraylist of integer arrays of starting and ending indices.
	 * @param theDoc
	 * @param theList
	 * @return
	 */
	public static ArrayList<int[]> findWordTrigramIndices(String theDoc, LinkedList<String> theList){
		ArrayList<int[]> theIndices = new ArrayList<int[]>();
		int listLength = theList.size();
		int i;
		int j;
		int start;
		int end;
		String wordOne;
		String wordTwo;
		String wordThree;
		int listLengthDivThree = listLength/3; // unless something odd happened, this number will always be a multiple of three. 
		String spaces=" ";
		theDoc = theDoc.replaceAll("\\p{C}", " ");
		for(i=0;i<listLengthDivThree;i++){
			wordOne = theList.pollLast().replaceAll("\\p{C}", " ");
			wordTwo = theList.pollLast().replaceAll("\\p{C}", " ");
			wordThree = theList.pollLast().replaceAll("\\p{C}", " ");
			if(wordOne.contains("."))
				wordOne = wordOne.replace(".","\\.");
			if(wordOne.contains("?"))
				wordOne = wordOne.replace("?","\\?");
			if(wordTwo.contains("."))
				wordTwo = wordTwo.replace(".","\\.");
			if(wordTwo.contains("?"))
				wordTwo = wordTwo.replace("?","\\?");
			if(wordThree.contains("."))
				wordThree = wordThree.replace(".","\\.");
			if(wordThree.contains("."))
				wordThree = wordThree.replace("?","\\?");
			try{
			Pattern wordToFind = Pattern.compile("((\\s|\\b)("+wordOne+"\\s*"+wordTwo+"\\s*"+wordThree+")(\\s|\\b)){1}+");
			Matcher theMatch = wordToFind.matcher(theDoc);
			theMatch.find();
			if( theMatch.group(2).matches("\\s"))
				start = theMatch.start()+1;
			else
				start = theMatch.start();
			if(theMatch.group(4).matches("\\s"))
				end = theMatch.end()-1;
			else
				end = theMatch.end();
			theIndices.add(new int[]{start,end});
			spaces = " ";
			int lenSpaces = end-start;
			for(j=1;j<lenSpaces;j++)
				spaces = spaces +" ";
			String theDocOne = theDoc.substring(0,start);
			String theDocTwo = theDoc.substring(end);
			theDoc = theDocOne+spaces+theDocTwo;
			}catch(IllegalStateException e){
				Logger.logln("'"+wordOne+" "+wordTwo+"  "+wordThree+"'  - was not matched. if there is more than 1 space or symbols exist within the single quotes, that may be why.");
			}
		}
		return theIndices;
		}
}
