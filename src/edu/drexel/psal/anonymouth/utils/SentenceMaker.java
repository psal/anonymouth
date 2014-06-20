package edu.drexel.psal.anonymouth.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.drexel.psal.ANONConstants;
import edu.drexel.psal.anonymouth.gooie.EditorDriver;
import edu.drexel.psal.anonymouth.gooie.GUIMain;
import edu.drexel.psal.anonymouth.helpers.FileHelper;
import edu.drexel.psal.anonymouth.engine.DocumentProcessor;

/**
 * Receives a chunk of text or a seemingly single sentence and scans through
 * it and splits it up whenever we see EOS characters that DON'T have the
 * ignore flag. So far, the only instances we mess with the ignore flag are
 * abbreviations.
 * 
 * @author Marc Barrowclift
 * @author Andrew W.E. McDonald
 *
 */
public class SentenceMaker implements Serializable  {
	
	private static final long serialVersionUID = -5007508872576011005L;

	private GUIMain main;
	private EditorDriver editorDriver;
	private SpecialCharTracker specialCharTracker;
	private DocumentProcessor documentProcessor;
	
	private final HashSet<Character> EOS;
	private final HashSet<Character> WORD_END;
	private final char NEWLINE = System.lineSeparator().charAt(0);
	
	/**
	 * This pattern, "EOS_CHARS" matches:
	 * 		=> any number (but at least one) and combination of question marks and quotation marks, OR
	 *		=> EXACTLY four periods (because English dictates that if you end a sentence with
	 *		   ellipsis points, you must have four periods: one for the period, and three for
	 *		   the ellipsis points, OR
	 *		=> any period NOT behind another period AND NOT in front of another period
	 *		   (otherwise, ellipsis points will be matched)
	 *		
	 * (NOT YET, but will hopefully do something like this soon:
	 *	Then, it matches one or more spaces, followed by either a capital letter, or an end of line.
	 *
	 * something along these lines:
	 *	private final Pattern EOS_CHARS = Pattern.compile("([?!]+)|([.]{4})|((?<!\\.)\\.(?!\\.))\\s+([A-Z]|$)");
	 */
	private final Pattern EOS_CHARS = Pattern.compile("([?!]+)|([.]{4,})|((?<!\\.)\\.(?!\\.))"); 
	
	/**
	 * HashSet to hold all our abbreviations read from the text
	 * file in jsan_resources. We have it in a HashSet so we can
	 * have constant time lookup and checking, an absolute must
	 * if we want to keep the performance of the editor decent.
	 * 
	 * This also means that for each new abbreviation added you
	 * must manually also add every version of capitalization for
	 * that abbreviation so checking this HashSet "ignores
	 * capitalization" (or just have a script do it for you). While
	 * this approach takes up more computer memory, the memory increase
	 * is trivial since it's just small Strings (and currently not many
	 * of them, i.e. < 100), so it's better to do it this way
	 * than having linear lookup time with n = # of abbreviations
	 */
	private final HashSet<String> ABBREVIATIONS;
	
	/**
	 * Constructor
	 */
	public SentenceMaker(GUIMain main) {
		ABBREVIATIONS = FileHelper.hashSetFromFile(ANONConstants.ABBREVIATIONS_FILE);
		this.main = main;
		
		EOS = new HashSet<Character>();
		EOS.add('.');
		EOS.add('!');
		EOS.add('?');
		
		WORD_END = new HashSet<Character>();
		WORD_END.add(' ');
		WORD_END.add(NEWLINE);
		WORD_END.add('\t');
		WORD_END.add('\"');
		WORD_END.add('(');
		WORD_END.add('[');
		WORD_END.add('{');
		WORD_END.add(')');
		WORD_END.add(']');
		WORD_END.add('}');
	}
	
	/**
	 * Takes a chunk of text (anything from a single String representing an
	 * entire document to pasted in text, to simply a seemingly single
	 * sentence) and breaks it up into seperate sentences where necessary. It
	 * does this by:
	 * 
	 * <ul>
	 * 	<li>Finding the "true" end of sentence</li>
	 * 	<li>Not splitting up at EOS characters in quotes</li>
	 * 	<li>Checking for sentences ending in a quoted sentence</li>
	 * 	<li>Ignoring abbreviations as ends of sentences</li>
	 * 	<li>Ignoring ellipses as ends of sentences</li>
	 * </ul>
	 *
	 * @param text 
	 *        The text you want to parse and split into seperate sentences
	 *
	 * @return 
	 * 		An ArrayList of Strings (which are the sentence representation
	 * 		of the given text)
	 */
	public ArrayList<String> makeSentences(String text, boolean fullDocument) {
		//=======================================================================
		//*							PREPARATIONS								*	
		//=======================================================================

		//================ PRIOR CHECKS =========================================
		
		/*
		 * Quick check so we're not trying to split up an empty
		 * String. This only happens right before the user types
		 * at the end of a document and we don't have anything to
		 * split, so return.
		 */
		
		if (text.equals("")) {
			ArrayList<String> sents = new ArrayList<String>(1);
			sents.add("");
			return sents;
		}
		
		/**
		 * Because the specialCharTracker isn't initialized until the TaggedDocument is,
		 * it won't be ready until near the end of DocumentProcessor, in which
		 * case we want to set it to the correct address. We also want to make sure we
		 * have the specialCharTracker from the current taggedDoc, in case the taggedDoc
		 * was replaced by a copy from the undo or redo stacks.
		 */
		this.specialCharTracker = main.editorDriver.taggedDoc.specialCharTracker;
		this.editorDriver = main.editorDriver;
		
		//================ VARIABLES ============================================

		ArrayList<String> sents = new ArrayList<String>(1);

		int length = text.length();
		int pastIndex = 0;
		int index = 0;
		int indexBuffer = editorDriver.sentIndices[0];
		String pastText = "";

		/**
		 * Only picks EOS characters that are NOT ellipses, so we
		 * don't have to do an additional check down the line (big
		 * thanks to Andrew, really smart)
		 */
		Matcher sent = EOS_CHARS.matcher(text);
		boolean EOSFound = false;

		//=======================================================================
		//*						FINDING STARTING POINT							*	
		//=======================================================================

		/**
		 * If we find an EOS that's not being ignored and it doesn't work out
		 * (meaning it's an abbreviation), then continue searching for an EOS
		 * character until we reach the end of the document
		 */
		while (index < length - 1) {
			//================ FINDING EOS NOT YET BEING IGNORED ====================
			EOSFound = sent.find(index);
			/**
			 * If we didn't find an EOS character in the entire passed text,
			 * then we don't even have to do anything, it's all just a single
			 * (incomplete) sentence, so break and return the text as is.
			 */
			if (!EOSFound) {
				break;
			}

			/**
			 * Making sure the EOS character we just found is not being
			 * ignored. If it is, we will skip over it and try to find an EOS
			 * character that isn't an ignored one
			 */
			if (EOSFound) {
				try {
					while (index < length - 1 && EOSFound) {
						index = sent.start();
						if (!specialCharTracker.isSentenceEndAtIndex(index + indexBuffer)) {
							EOSFound = false;
						} else {
							EOSFound = true;
							break;
						}
						index++;
						EOSFound = sent.find(index);
					}
				} catch (IllegalStateException e) {}
			}
			/**
			 * If no EOS character was found that's not being ignored in the
			 * entire passed text, then we don't even have to do anything,
			 * it's all just a single (incomplete) sentence with ellipses, EOS
			 * characters in quotes or citations, and/or abbreviations but NOT
			 * REAL ENDS OF SENTENCES.
			 */
			if (!EOSFound && index > length - 1) {
				break;
			}

			//================ ENSURING IT'S NOT AN ABBREVIATION IF FOUND ============

			/**
			 * If we find an Abbreviation, all we do is set the flag on eos
			 * in the specialCharTracker to "true" because we are now ignoring it.
			 */
			if (EOSFound) {
				try {
					/**
					 * Obtaining the whole abbreviation from beginning to end
					 */
					int abbrevStart = index;
					while (!WORD_END.contains(text.charAt(abbrevStart))) {
						abbrevStart--;
					}
					int abbrevEnd = index;
					while (!WORD_END.contains(text.charAt(abbrevEnd))) {
						abbrevEnd++;
					}

					abbrevStart++;
					if (ABBREVIATIONS.contains(text.substring(abbrevStart, abbrevEnd))) {
						int temp = pastIndex;
						/**
						 * Since an abbreviation is found, we want to traverse through the
						 * entirety of the abbreviation to set any EOS characters we find
						 * in it to IGNORE (so ones with multiple EOS characters like Ph.D.
						 * are handled correctly)
						 */
						while (abbrevStart != abbrevEnd) {
							if (EOS.contains(text.charAt(abbrevStart))) {
								specialCharTracker.setIgnoreEOS(abbrevStart + indexBuffer, true);
							}

							abbrevStart++;
						}
						EOSFound = false;
						pastText = text.substring(temp, abbrevEnd);
						index = abbrevEnd;
						continue;
					}
				} catch (Exception e) {}
			}

			//================ ENSURING IT'S NOT IN QUOTES OR PARENTHESIS =============

			/**
			 * NOTE: NO NEED TO CHECK IF IN QUOTES OR PARENTHESIS<br><br>
			 *
			 * This is because TaggedDoc and all the backend stuff doesn't
			 * really care if we make a sentence break halfway through a quote
			 * or parenthesis. The only reason we were doing this is so it
			 * wouldn't look odd when highlighting these breaks for the user.
			 * The way around this is we just artificially extend the
			 * highlight past the currently focused sentence to where the
			 * actual end is.<br><br>
			 *
			 * This has a number of advantages. Firstly, it's significantly
			 * less prone to splitting up (or combining) sentences the user
			 * didn't want it to when deleting these text wrappers. This makes
			 * removing and adding them far more secure and natural since the
			 * minute your remove then all the sentences are ALREADY BROKEN UP
			 * THE WAY THEY WERE SUPPOSED TO BE SINCE WE NEVER COMBINED THEM
			 * ALL IN THE FIRST PLACE.<br><br>
			 *
			 * Another significant advantage is it makes the highlighter make
			 * conceptual sense. When I first type a '"', that would indicate
			 * that all text after it is part of the new quote until we close
			 * it, meaning it will highlight all following text until the user
			 * closes it.
			 */
			index++;
			if (EOSFound) {
				sents.add(text.substring(pastIndex, index));
				pastText = "";
			}
			pastIndex = index;
		}
		if (sents.isEmpty()) {
			sents.add(text);
		} else if (pastIndex < length) {
			//Add any remaining text as its own sentence
			sents.add(text.substring(pastIndex));
		}
		return sents;
	}
}