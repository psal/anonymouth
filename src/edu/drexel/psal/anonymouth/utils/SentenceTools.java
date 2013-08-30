package edu.drexel.psal.anonymouth.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jgaap.generics.Document;

import edu.drexel.psal.ANONConstants;
import edu.drexel.psal.anonymouth.gooie.GUIMain;
import edu.drexel.psal.anonymouth.helpers.ErrorHandler;
import edu.drexel.psal.anonymouth.helpers.FileHelper;
import edu.drexel.psal.jstylo.generics.Logger;

/**
 * Retrieves sentences from a text and places them into an arraylist.
 * @author Andrew W.E. McDonald
 * @author Marc Barrowclift
 *
 */
public class SentenceTools implements Serializable  {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5007508872576011005L;
	private final String NAME = "( "+this.getClass().getName()+" ) - ";
	
	/**
	 * This pattern, "EOS_chars" matches:
	 * 		=> any number (but at least one) and combination of question marks and quotation marks, OR
	 *		=> EXACTLY four periods (because English dictates that if you end a sentence with ellipsis points, you must have four periods: one for the period, and three for the ellipsis points, OR
	 *		=> any period NOT behind another period AND NOT in front of another period (otherwise, ellipsis points will be matched)
	 ********
	 * (NOT YET, but will hopefully do something like this soon:
	 *	Then, it matches one or more spaces, followed by either a capital letter, or an end of line.
	 *
	 * something along these lines:
	 *	private static final Pattern EOS_chars = Pattern.compile("([?!]+)|([.]{4})|((?<!\\.)\\.(?!\\.))\\s+([A-Z]|$)"); 
	 *
	 *	)
	 *********
	 */
	private static final Pattern EOS_chars = Pattern.compile("([?!]+)|([.]{4,})|((?<!\\.)\\.(?!\\.))"); 
	private String EOS = ".?!";
	
	/**
	 * the "sentence_quote" pattern matches any number and combination of "?" and "!" characters, OR four periods, OR a <i>single</i> period (specifically, any period that isn't followed by, or that follows, another period). (because ellipses points don't indicate an end of sentence UNLESS there are 4 ellipses points [one for the period, and three for the ellipsis]),
	 * it then matches a single "double" quotation mark, followed by  the first group (see above line) (because some people think that: "The man said, "Hello!"." (using an EOS character pre and post quotation mark is acceptable...)
	 * Finally, it and will either match the end of the input, a capital letter (both which indicate that the current sentence is over), or a citation (the explanation of the citation regex is below... I just copied and pasted it onto the end of this one).
	 * 
	 * NOTE: in the written description above, space characters are not necessarily discussed.
	 */
	private static final Pattern sentence_quote = Pattern.compile("([?!]+|[.]{4}|(?<!\\.)\\.(?!\\.))\\s*\"\\s*([?!]+|[.]{4}|(?<!\\.)\\.(?!\\.))?\\s*($|[A-Z]|\\(((\\s*[A-Za-z.]*\\s*(et\\.?\\s*al\\.)?\\s*[0-9]*\\s*[-,]*\\s*[0-9]*\\s*)|(\\s*[0-9]*\\s*[-,]*\\s*[0-9]*\\s*[A-Za-z.]*\\s*(et\\.?\\s*al\\.)?\\s*))\\))"); 
	
	private static final Pattern sentence_parentheses = Pattern.compile("([?!]+|[.]{4}|(?<!\\.)\\.(?!\\.))\\s*\\(\\s*([?!]+|[.]{4}|(?<!\\.)\\.(?!\\.))?\\s*($|[A-Z]|\\(((\\s*[A-Za-z.]*\\s*(et\\.?\\s*al\\.)?\\s*[0-9]*\\s*[-,]*\\s*[0-9]*\\s*)|(\\s*[0-9]*\\s*[-,]*\\s*[0-9]*\\s*[A-Za-z.]*\\s*(et\\.?\\s*al\\.)?\\s*))\\))");
	/**
	 * the pattern 'citation' forces the match to begin at the start of the input (via the anchor), and matches zero or one occurrences EOS character, and then searches for citations that begin with an opening parenthesis,
	 * match either a word (a name) followed by "et al." [or et. al.", even though it's wrong] (or not) followed by a number, or two numbers separated by a dash, and finishing with a closing parenthesis. 
	 * It will also match a swapped version, where the number / two numbers separated by a dash come before the name (and "et. al.", if it exists.)
	 * 
	 * NOTE: in the written description above, space characters are not necessarily discussed.
	 */
	private static final Pattern citation = Pattern.compile("^[?!.]?\\s*\\(((\\s*[A-Za-z.]*\\s*(et\\.?\\s*al\\.)?\\s*[0-9]*\\s*[-,]*\\s*[0-9]*\\s*)|(\\s*[0-9]*\\s*[-,]*\\s*[0-9]*\\s*[A-Za-z.]*\\s*(et\\.?\\s*al\\.)?\\s*))\\)"); 
	
	private final HashSet<String> ABBREVIATIONS;
	private final Pattern abbreviationPattern = Pattern.compile("\\.\\s");
	
	private static int MAX_SENTENCES = 500;
	//private ArrayList<String> sentsToEdit = new ArrayList<String>(MAX_SENTENCES);
	private static int sentNumber = -1;
	private int totalSentences = 0;
	
	public SentenceTools() {
		ABBREVIATIONS = FileHelper.hashSetFromFile(ANONConstants.ABBREVIATIONS_FILE);
	}
	
	/**
	 * Takes a text (one String representing an entire document), and breaks it up into sentences. Tries to find true ends of sentences: shouldn't break up sentences containing quoted sentences, 
	 * checks for sentences ending in a quoted sentence (e.x. He said, "Hello." ), will not break sentences containing common abbreviations (such as Dr., Mr. U.S., etc.,e.x., i.e., and others), and 
	 * checks for ellipses points. However, It is probably not perfect.
	 * @param text
	 * @return
	 */
	public ArrayList<String[]> makeSentenceTokens(String text) {
		if (text.equals("")) {
			ArrayList<String[]> finalSents = new ArrayList<String[]>();
			finalSents.add(new String[]{"", ""});
			return finalSents;
		}
		
		ArrayList<String> sents = new ArrayList<String>(MAX_SENTENCES);
		ArrayList<String[]> finalSents = new ArrayList<String[]>(MAX_SENTENCES);
		boolean mergeNext=false;
		boolean mergeWithLast=false;
		boolean forceNoMerge=false;
		int currentStart = 1;
		int currentStop = 0;
		String temp;
		text = text.replaceAll("\u201C","\"");
		text = text.replaceAll("\u201D","\"");
		text = text.replaceAll("\\p{Cf}","");// replace unicode format characters that will ruin the regular expressions (because non-printable characters in the document still take up indices, but you won't know they're there untill you "arrow" though the document and have to hit the same arrow twice to move past a certain point.
		// Note that we must use "Cf" rather than "C". If we use "C" or "Cc" (which includes control characters), we remove our newline characters and this screws up the document.
		// "Cf" is "other, format". "Cc" is "other, control". Using "C" will match both of them.
		int lenText = text.length();
		int index = 0;
		int buffer = GUIMain.inst.editorDriver.sentIndices[0];
		String safeString = "";
		Matcher abbreviationFinder = abbreviationPattern.matcher(text);
		
		while (index < lenText-1 && abbreviationFinder.find(index)) {
			index = abbreviationFinder.start();
			
			try {
				int abbrevLength = index;
				try {
//				System.out.println("index = " + index + ", lenText-1 = " + (lenText-1) + ", char = " + text.charAt(abbrevLength));
				} catch(Exception e) {
//					System.out.println("index = " + index + ", lenText-1 = " + (lenText-1));
				}
				while (text.charAt(abbrevLength) != ' ') {
					abbrevLength--;
				}
				
//				System.out.println("ABBREVIATION = \"" + text.substring(abbrevLength+1, index+1) + "\"");
				if (ABBREVIATIONS.contains(text.substring(abbrevLength+1, index+1))) {
					GUIMain.inst.editorDriver.taggedDoc.specialCharTracker.setIgnore(index+buffer, true);
				}
			} catch (Exception e) {
//				System.out.println("STUPID");
			}
			
			
			index++;
		}		
		
		Matcher sent = EOS_chars.matcher(text);
		boolean foundEOS = sent.find(currentStart); // xxx TODO xxx take this EOS character, and if not in quotes, swap it for a permanent replacement, and create and add an EOS to the calling TaggedDocument's eosTracker.
		
		/*
		 * We want to check and make sure that the EOS character (if one was found) is not supposed to be ignored. If it is, we will act like we did not
		 * find it. If there are multiple sentences with multiple EOS characters passed it will go through each to check, foundEOS will only be true if
		 * an EOS exists in "text" that would normally be an EOS character and is not set to be ignored.
		 */
		
		index = 0;
		if (foundEOS) {	
			try {
				System.out.println("Beginning first check");
				while (index < lenText-1 && sent.find(index)) {
					index = sent.start();
					if (!GUIMain.inst.editorDriver.taggedDoc.specialCharTracker.EOSAtIndex(index+buffer)) {
						foundEOS = false;
					} else {
						foundEOS = true;
						break;
					}
					index++;
				}
			} catch (IllegalStateException e) {}
		}
		//We need to reset the Matcher for the code below
		sent = EOS_chars.matcher(text);
		sent.find(currentStart);
		
		Matcher sentEnd;
		Matcher citationFinder;
		boolean hasCitation = false;
		int charNum = 0;
		int lenTemp = 0;
		int lastQuoteAt = 0;
		int lastParenAt = 0;
		boolean foundQuote = false;
		boolean foundParentheses = false;
		boolean isSentence;
		boolean foundAtLeastOneEOS = foundEOS;
		
		/**
		 * Needed otherwise when the user has text like below:
		 * 		This is my sentence one. This is "My sentence?" two. This is the last sentence.
		 * and they begin to delete the EOS character as such:
		 * 		This is my sentence one. This is "My sentence?" two This is the last sentence.
		 * Everything gets screwed up. This is because the operations below operate as expected only when there actually is an EOS character
		 * at the end of the text, it expects it there in order to function properly. Now usually if there is no EOS character at the end it wouldn't
		 * matter since the while loop and !foundAtLeastOneEOS conditional are executed properly, BUT as you can see the quotes, or more notably the EOS character inside
		 * the quotes, triggers this initial test and thus the operation breaks. This is here just to make sure that does not happen.
		 */
		String trimmedText = text.trim();
		int trimmedTextLength = trimmedText.length();

		//We want to make sure that if there is an EOS character at the end that it is not supposed to be ignored
		System.out.println("Checking if EOS at sentence end");
		boolean EOSAtSentenceEnd = true;
		if (trimmedTextLength != 0) {
			EOSAtSentenceEnd = EOS.contains(trimmedText.substring(trimmedTextLength-1, trimmedTextLength)) && GUIMain.inst.editorDriver.taggedDoc.specialCharTracker.EOSAtIndex(GUIMain.inst.editorDriver.sentIndices[1]-2);
		} else {
			EOSAtSentenceEnd = false;
		}
		System.out.println("EOS At sentence end = " + EOSAtSentenceEnd);
//		boolean EOSAtSentenceEnd = EOS.contains(text.substring(lenText-1, lenText));

		//Needed so that if we are deleting abbreviations like "Ph.D." this is not triggered.
		if (!EOSAtSentenceEnd && (GUIMain.inst.editorDriver.watchForEOS == -1))
			EOSAtSentenceEnd = true;

		while (foundEOS == true) {
			currentStop = sent.end();
			
			//We want to make sure currentStop skips over ignored EOS characters and stops only when we hit a true EOS character
			try {
				while (!GUIMain.inst.editorDriver.taggedDoc.specialCharTracker.EOSAtIndex(currentStop+buffer-1) && currentStop != lenText) {
					System.out.println("SHOULD NOT HAVE EXECUTED");
					sent.find(currentStop+1);
					currentStop = sent.end();
				}
			} catch (Exception e) {}

			temp = text.substring(currentStart-1,currentStop);
			lenTemp = temp.length();
			lastQuoteAt = 0;
			lastParenAt = 0;
			foundQuote = false;
			foundParentheses = false;
			
			for(charNum = 0; charNum < lenTemp; charNum++){
				if (temp.charAt(charNum) == '\"') {
					lastQuoteAt = charNum;
					
					if (foundQuote == true)
						foundQuote = false;
					else
						foundQuote = true;
				}
				
				if (temp.charAt(charNum) == '(') {
					lastParenAt = charNum;
					
					if (foundParentheses)
						foundParentheses = false;
					else
						foundParentheses = true;
				}
			}
			
			if (foundQuote == true && ((temp.indexOf("\"",lastQuoteAt+1)) == -1)) { // then we found an EOS character that shouldn't split a sentence because it's within an open quote.
				if ((currentStop = text.indexOf("\"",currentStart +lastQuoteAt+1)) == -1) {
					currentStop = text.length(); // if we can't find a closing quote in the rest of the input text, then we assume the author forgot to put a closing quote, and act like it's at the end of the input text.
				}
				else{
					currentStop +=1;
					mergeNext=true;// the EOS character we are looking for is not in this section of text (section being defined as a substring of 'text' between two EOS characters.)
				}
			}
			safeString = text.substring(currentStart-1,currentStop);
			
			if (foundParentheses && ((temp.indexOf(")", lastParenAt+1)) == -1)) {
				if ((currentStop = text.indexOf(")", currentStart + lastParenAt + 1)) == -1)
					currentStop = text.length();
				else {
					currentStop += 1;
					mergeNext = true;
				}
			}
			safeString = text.substring(currentStart-1,currentStop);

			if (foundQuote) {
				sentEnd = sentence_quote.matcher(text);	
				isSentence = sentEnd.find(currentStop-2); // -2 so that we match the EOS character before the quotes (not -1 because currentStop is one greater than the last index of the string -- due to the way substring works, which is includes the first index, and excludes the end index: [start,end).)

				if (isSentence == true) { // If it seems that the text looks like this: He said, "Hello." Then she said, "Hi." 
					// Then we want to split this up into two sentences (it's possible to have a sentence like this: He said, "Hello.")
					//System.out.println("start: "+sentEnd.start()+" ... end: "+sentEnd.end());
					currentStop = text.indexOf("\"",sentEnd.start())+1;
					safeString = text.substring(currentStart-1,currentStop);
					forceNoMerge = true;
					mergeNext = false;
				}
			}
			
			if (foundParentheses) {
				sentEnd = sentence_parentheses.matcher(text);
				isSentence = sentEnd.find(currentStop-2);
				
				if (isSentence == true) {
					currentStop = text.indexOf(")", sentEnd.start()) + 1;
					safeString = text.substring(currentStart-1, currentStop);
					forceNoMerge = true;
					mergeNext = false;
				}
			}
			//System.out.println("POST quote finder: "+safeString);
			// now check to see if there is a citation after the sentence (doesn't just apply to quotes due to paraphrasing)
			// The rule -- at least as of now -- is if after the EOS mark there is a set of parenthesis containing either one word (name) or a name and numbers (name 123) || (123 name) || (123-456 name) || (name 123-456) || etc..
			citationFinder = citation.matcher(text.substring(currentStop));	
			hasCitation = citationFinder.find(); // -2 so that we match the EOS character before the quotes (not -1 because currentStop is one greater than the last index of the string -- due to the way substring works, which is includes the first index, and excludes the end index: [start,end).)
			
			if (hasCitation == true) { // If it seems that the text looks like this: He said, "Hello." Then she said, "Hi." 
				// Then we want to split this up into two sentences (it's possible to have a sentence like this: He said, "Hello.")
				currentStop = text.indexOf(")",citationFinder.start()+currentStop)+1;
				safeString = text.substring(currentStart-1,currentStop);
				mergeNext = false;
			}	
			
			if (mergeWithLast) {
				mergeWithLast=false;
				String prev=sents.remove(sents.size()-1);
				safeString=prev+safeString;
			}
			
			if (mergeNext && !forceNoMerge) {//makes the merge happen on the next pass through
				mergeNext=false;
				mergeWithLast=true;
			} else {
				forceNoMerge = false;
				finalSents.add(new String[]{safeString, safeString});
			}
		
			sents.add(safeString);
			
			//// xxx xxx xxx return the safeString_subbedEOS too!!!!
			if (currentStart < 0 || currentStop < 0) {
				Logger.logln(NAME+"Something went really wrong making sentence tokens.");
				ErrorHandler.fatalProcessingError(null);
			}

			currentStart = currentStop+1;
			if (currentStart >= lenText) {
				foundEOS = false;
				continue;
			}
			
			foundEOS = sent.find(currentStart);
		}

		if (!foundAtLeastOneEOS || !EOSAtSentenceEnd) {
			ArrayList<String[]> wrapper = new ArrayList<String[]>(1);
			wrapper.add(new String[]{text,text});
			return wrapper;
		}
		
		return finalSents;
	}
	
	public static int getSentNumb(){
		return sentNumber;
	}
	/**
	 * Checks whether or not there are more unchecked sentences from the intial input text or not. True if there are,
	 * false if not.
	 * @return
	 * 	True if there are more sentences to check, false if not.
	 */
	public boolean moreToCheck(){
		if(sentNumber < totalSentences)
			return true;
		else
			return false;
	}
	
	public void setSentenceCounter(int sentNumber){
		SentenceTools.sentNumber = sentNumber;
	}
	
	public static Document removeUnicodeControlChars(Document dirtyDoc){
		Document cleanDoc = new Document();
		try {
			dirtyDoc.load();
			cleanDoc.setText((dirtyDoc.stringify()).replaceAll("\\p{C}&&[^\\t\\n\\r]"," ").toCharArray());
			cleanDoc.setAuthor(dirtyDoc.getAuthor());
			cleanDoc.setTitle(dirtyDoc.getTitle());
			return cleanDoc;
		} catch (Exception e) {
			e.printStackTrace();
			Logger.logln("(SentenceTools) - ERROR! Could not load document: "+dirtyDoc.getTitle()+" (SentenceTools.removeUnicodeControlChars)");
			return dirtyDoc;
		}
	}
}