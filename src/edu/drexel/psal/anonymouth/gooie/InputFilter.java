package edu.drexel.psal.anonymouth.gooie;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import edu.drexel.psal.anonymouth.utils.SpecialCharacterTracker;
import edu.drexel.psal.jstylo.generics.Logger;

/**
 * @author Marc Barrowclift
 * @author Andrew W.E. McDonald
 * 
 * Supported actions:
 * 
 * 1) Adding/removing ellipsis (along with more than one EOS character and variations like "???", "!?", "....", "...", etc)
 * 		-TODO: "This...... another sentence" will break up the sentence into two. It used to break it up into three but
 * 				I modified the regEx "EOS_chars" slightly in SentenceTools to just split into two as if it was "This.... another sentence"
 * 				instead. I sadly can't figure out how to have it keep it a full sentence though while keeping the "This.... another sentence"
 * 				splitting functionality.
 * 2) Adding/removing abbreviations.
 * 3) Adding/removing quotes (handled inherently by a combination of the two checks above and by existing code in SentenceTools)
 * 4) Adding/removing parentheses (handled primarily by SentenceTools)
 */
public class InputFilter extends DocumentFilter {
	
	//Constants
	private final String NAME = "( InputFilter ) - ";
	public final static int UNDOCHARACTERBUFFER = 5;
	//we only need to worry about these kinds of abbreviations since SentenceTools takes care of the others
	private final String[] ABBREVIATIONS = {"U.S.","R.N.","M.D.","i.e.","e.x.","e.g.","D.C.","B.C.","B.S.","Ph.D.","B.A.","A.B.","A.D.","A.M.","P.M.","r.b.i.","V.P."};
	//Quick and dirty way to identify EOS characters.
	private final String EOS = ".?!";
	
	public static int currentCharacterBuffer = 0;
	public static boolean isEOS = false; //keeps track of whether or not the current character is an EOS character.
	public static boolean ignoreTranslation = false;
	public static boolean ignoreDeletion = false;
	public static boolean shouldBackup = false;
	private boolean watchForEOS = false; //Lets us know if the previous character(s) were EOS characters.
	private boolean addingAbbreviation = false;
	private EditorDriver driver;
	
	public InputFilter(EditorDriver driver) {
		this.driver = driver;
	}
	
	/**
	 * If the user types a character or pastes in text this will get called BEFORE updating the documentPane and firing the listeners.
	 */
	public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String text, AttributeSet attr) throws BadLocationException {	
		if (text.length() == 1) { //If the user is just typing (single character)
			driver.updateBackend = false;
			
			checkAddingEllipses(text);
			checkAddingAbbreviations(text);
			
			if (driver.selectionIndices[0] != driver.selectionIndices[1])
				driver.ignoreEOSDeletion = true; 
		} else { //If the user pasted in text of length greater than a single character
			driver.updateBackend = true; //If the user pasted in a massive chunk of text we want to update no matter what.
			Logger.logln(NAME + "User pasted in text, will update");
		}
				
		fb.replace(offset, length, text, attr);
	}
	
	/**
	 * Keeps track of whether or not the user may be typing ellipses and only removeReplaceAndUpdate's when we are sure they have completed
	 * Typing EOS characters and are beginning a new sentence.
	 * @param text - The text the user typed
	 */
	private void checkAddingEllipses(String text) {
		isEOS = EOS.contains(text); //Checks to see if the character is an EOS character.

		if (isEOS && !addingAbbreviation) {
			watchForEOS = true;
			//For whatever reason, startSelection must be subtracted by 1, and refuses to work otherwise.
			//TODO Is it supposed to be selectionIndices[1] here? The comment says start, so it should be selectionIndices[0] according
			//to that, look into it.
			driver.taggedDoc.specialCharTracker.addEOS(SpecialCharacterTracker.replacementEOS[0], driver.selectionIndices[1]-1, false);
		} else if (!isEOS && !watchForEOS) { //If the user isn't typing an EOS character and they weren't typing one previously, then it's just a normal character, update.
			driver.updateBackend = true;
		} else if (isEOS && addingAbbreviation) {
			driver.ignoreEOSDeletion = true;
			addingAbbreviation = false;
		}

		//if the user previously entered an EOS character and the new character is not an EOS character, then we should update
		if (watchForEOS && !isEOS) {
			shouldBackup = true;
			watchForEOS = false;
			/**
			 * NOTE: We must NOT call removeReplaceAndUpdate() directly since the currentSentenceString variable that's used for the
			 * call's parameter is not updated yet (for example, the text here in InputFilter my read "TEST.... A sentence", but the
			 * currentSentenceString variable, and the documentPane, only read TEST....A sentence. The quickest and easiest way to fix
			 * this is just have a little flag at the end of the caret listener that calls removeReplaceAndUpdate only when we command
			 * it to from the InputFilter.
			 */
			driver.updateBackend = true;
		}
	}
	
	/**
	 * Keeps track of whether or not the user is entering an abbreviation or not and will only call removeReplaceAndUpdate when we are sure they are in fact
	 * not typing an abbreviation and want to end the sentence.
	 * @param text - The text the user typed
	 */
	private void checkAddingAbbreviations(String text) {
		try {
			boolean isAdding = false;
			
			String textBeforePeriod = GUIMain.inst.documentPane.getText().substring(driver.selectionIndices[0]-2, driver.selectionIndices[0]);
			if (textBeforePeriod.substring(1, 2).equals(".") && !EOS.contains(text)) {
				for (int i = 0; i < ABBREVIATIONS.length; i++) {
					if (ABBREVIATIONS[i].endsWith(textBeforePeriod)) {
						int length = ABBREVIATIONS[i].length();
						textBeforePeriod = GUIMain.inst.documentPane.getText().substring(driver.selectionIndices[0]-length, driver.selectionIndices[0]);
						
						System.out.println (textBeforePeriod + " = " + ABBREVIATIONS[i]);
						if (textBeforePeriod.equals(ABBREVIATIONS[i])) {
							driver.updateBackend = false;
							addingAbbreviation = true;
							isAdding = true;
							break;
						}
					} else if (ABBREVIATIONS[i].contains(textBeforePeriod)) {
						System.out.println(ABBREVIATIONS[i]);
						driver.updateBackend = false;
						addingAbbreviation = true;
						isAdding = true;
						break;
					}
				}
			}
			
			/**
			 * If we are no longer adding abbreviations (meaning that it didn't match any of the ones of the list), then we will reset
			 * addingAbbreviation to false. We have to do it here since we only know here whether or not the user is actually done writing
			 * one or not (it may be one with multiple EOS characters, you don't know.
			 */
			if (!isAdding)
				addingAbbreviation = false;
			
		} catch(StringIndexOutOfBoundsException e) {} //most likely the user is typing at the very beginning of the document, move on.
	}
	
	/**
	 * If the user deletes a character or a section of text this will get called BEFORE updating the documentPane and firing the listeners.
	 */
	public void remove(DocumentFilter.FilterBypass fb, int offset, int length) throws BadLocationException {
		if (length == 1) { //If the user is just deleting character by character
			driver.updateBackend = false;
			driver.ignoreEOSDeletion = false;

			checkRemoveEllipses(offset);
			checkRemoveAbbreviations(offset);
		} else { //If the user selected and deleted a section of text greater than a single character
			/**
			 * I know this looks goofy, but without some sort of check to make sure that the document is done processing, this would fire
			 * removeReplaceAndUpdate() in DriverEditor and screw all the highlighting up. There may be a better way to do this...
			 */
			if (GUIMain.inst.processed && !ignoreTranslation) {
				driver.updateBackend = true; //We want to update no matter what since the user is dealing with a chunk of text
				Logger.logln(NAME + "User deleted multiple characters in text, will update");
			} else
				ignoreTranslation = false;
		}

		fb.remove(offset, length);
	}

	/**
	 * Pretty much the same thing as checkAddingEllipses only it's not receiving text, but checking the document pane at the indices given for
	 * the text instead of getting it as a parameter. Essentially checkAddingEllipses but backwards.
	 * @param offset
	 */
	private void checkRemoveEllipses(int offset) {
		isEOS = EOS.contains(GUIMain.inst.documentPane.getText().substring(offset, offset+1)); //checks to see if the deleted character is an EOS character
		
		if (isEOS && EOS.contains(GUIMain.inst.documentPane.getText().substring(offset-1, offset))) { //if it was AND the character before it is ALSO an EOS character...
			watchForEOS = true;
		} else if (!isEOS && !watchForEOS) { //The user deleted a character and didn't delete one previously, nothing to do, update.
			driver.updateBackend = true;
		}
		
		if (watchForEOS && !isEOS) { //if the user previously deleted an EOS character AND the one they just deleted is not an EOS character, we should update.
			watchForEOS = false;
			shouldBackup = true;
			driver.updateBackend = true;
		}
	}
	
	/**
	 * Checks to see if the text we're deleting is an abbreviation, and only updates when ready.
	 * @param offset
	 */
	private void checkRemoveAbbreviations(int offset) {
		try {
			String textBeforeDeletion = GUIMain.inst.documentPane.getText().substring(offset-2, offset+1);

			for (int i = 0; i < ABBREVIATIONS.length; i++) {
				if (ABBREVIATIONS[i].contains(textBeforeDeletion))
					driver.updateBackend = false;
			}
		} catch(StringIndexOutOfBoundsException e) {} //most likely the user is deleting at the first index of their document, move on
	}
}