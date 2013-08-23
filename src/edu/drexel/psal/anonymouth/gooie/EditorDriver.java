package edu.drexel.psal.anonymouth.gooie;

import edu.drexel.psal.anonymouth.engine.HighlighterEngine;
import edu.drexel.psal.anonymouth.utils.TaggedDocument;
import edu.drexel.psal.jstylo.generics.Logger;
import edu.drexel.psal.jstylo.generics.Logger.LogOut;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/** 
 * Handles all the listeners for the Document tabs, but mainly handles all the
 * backend bookkeeping that's needed for the Editor itself (since the editor
 * isn't actually editing text but rather editing the TaggedDocument, which
 * has TaggedSentences and their TaggedWords)
 *
 * @author Andrew W.E. McDonald
 * @author Marc Barrowclift
 */
public class EditorDriver {

	//============Assorted===========
	private final String NAME = "( " + this.getClass().getSimpleName() + " )- ";
	private ActionListener reProcessListener;
	private CaretListener caretListener;
	private DocumentListener documentListener;
	private MouseListener mouseListener;
	private GUIMain main;
	private Runnable placeWordSuggestions;
	private Runnable updateAnonymityBar;
	private Runnable moveHighlightTread;

	//============Highlighters============	
	protected HighlighterEngine highlighterEngine;
	protected boolean doHighlight = PropertiesUtil.getHighlightSents();
	protected boolean autoHighlight = PropertiesUtil.getAutoHighlight();
	private boolean updateHighlight;

	//============Undo/Redo "Version Control"===========
	private final int CHARS_TIL_BACKUP = 20;
	//The current character since last version was added to the undo stack
	private int curCharBackupBuffer;
	public boolean ignoreBackup;
	protected TaggedDocument pastTaggedDoc;

	//================EDITOR================

	public TaggedDocument taggedDoc;

	//Caret position
	public int caretPosition;	//Where the caret is right now
	/**
	 * We can only have inserted or removed, we can't have both happening
	 * at the same time. Therefore, we inheritely account for one of them
	 * always being 0 here and avoid having to have two seperate variables
	 * for past caret position (one for removed and one for insterted)
	 */
	private int pastCaretPosition;

	/**
	 * The indices of a selection (if any, both are the same number if none)
	 */
	public int[] selectionIndices;
	private int[] oldSelectionIndices;

	//Sentence variables
	public int sentNum;			//The current sentence number (0, 1, 2, ...)
	private int[] sentIndices;		//The indices of the sentence
	private int pastSentNum;		//The past sentence number
	private boolean wholeLastSentDeleted;
	private boolean wholeBeginningSentDeleted;

	//Editing variables
	private int charsInserted;		//The number of characters inserted
	private int charsRemoved;		//The number of characters removed

	/**
	 * Whether or not to process changes made to the text. Exists because in
	 * some instances we manipulate the data in the editor but we don't want
	 * the change listeners to fire and start running their code, otherwise
	 * everything will break.
	 */
	protected boolean ignoreChanges;
	/**
	 * In some cases with EOS deletion we aren't actually deleting a sentence
	 * and we want to therefore ignore the standard EOS code (which assumes
	 * deleting a sentence). This is triggered by InputFilter
	 */
	protected boolean ignoreEOSDeletion;
	/**
	 * In some cases (much of which are explained in InputFilter), we want to
	 * have the listener code be executed but DON'T want to update the backend
	 * immediately. This is used as a way to control whether or not that
	 * happens and when.
	 */
	protected boolean updateBackend;

	//=======================================================================
	//*							INITIALIZATION								*	
	//=======================================================================

	/** 
	 * Constructor, readies all listeners used in the Document tabs.
	 */
	public EditorDriver(GUIMain main) {
		this.main = main;
		highlighterEngine = new HighlighterEngine(main);

		initListeners();
		initThreads();
		resetToDefaults();
	}

	/**
	 * Initializes all listeners used by the Document tabs
	 */
	private void initListeners() {
		caretListener = new CaretListener() {
			@Override
			public void caretUpdate(CaretEvent e) {
				//If we are not supposed to update backend, don't update
				if (ignoreChanges) {
					return;
				}

				System.out.println("============================================");
				caretPosition = e.getDot();	//Current caret position
				pastCaretPosition = caretPosition - charsInserted + charsRemoved; //see above

				if (charsRemoved > 0) {
					deletion(); //We MUST make sure we handle any TaggedSentence deletion if needed
					main.saved = false;
					taggedDoc.specialCharTracker.shiftAllEOSChars(false, pastCaretPosition, charsRemoved);
				} else if (charsInserted > 0) {
					insertion(); //We MUST make sure to handle any EOS characters being added
					main.saved = false;
					taggedDoc.specialCharTracker.shiftAllEOSChars(true, pastCaretPosition, charsInserted);
				}

				int[] selectionInfo;
				try {
					selectionInfo = getSentencesIndices(caretPosition)[0];

					if (selectionInfo == null)
						return;
				} catch (ArrayIndexOutOfBoundsException e1) {
					/**
					 * Not a problem if this gets thrown, just means they tried
					 * clicking in a place that's outside the text bounds.
					 */
					Logger.logln(NAME+"User tried moving caret outside text scope, will ignore action.");
					return;
				}

				//Update the position variables
				pastSentNum = sentNum; 		//The current sent number is now the past sent number
				sentNum = selectionInfo[0]; //Set the new sent number
				selectionIndices[0] = selectionInfo[1];
				selectionIndices[1] = selectionInfo[2];

				/**
				 * check to see if the current caret location is
				 * within the selectedSentIndexRange ([0] is min, [1] is max)
				 */
				//IN SENTENCE
				if (pastCaretPosition >= sentIndices[0] && pastCaretPosition < sentIndices[1]) {
					if (charsInserted > 0) {
						sentIndices[1] += charsInserted;
						charsInserted = 0;
					} else if (charsRemoved > 0) {
						sentIndices[1] -= charsRemoved;
						charsRemoved = 0;
					}
				//OUT OF SENTENCE
				} else {
					main.translationsPanel.updateTranslationsPanel(taggedDoc.getSentenceNumber(sentNum));
				}

				oldSelectionIndices[0] = selectionIndices[0];
				oldSelectionIndices[1] = selectionIndices[1];

				/**
				 * Apologizes to whoever is trying to figure this shit out, I did this all so long
				 * ago now that I completely forgot the precise purpose this was supposed to serve
				 * and I wasn't good about documenting stuff then.
				 * 
				 * I can tell you that this seems to trigger whenever one's deleting a chunk of text
				 * including a newly added EOS character and that without this things break very
				 * quickly.
				 */
				if ((caretPosition - 1 != pastCaretPosition && charsRemoved == 0 && charsInserted != 0) ||
					(caretPosition != pastCaretPosition - 1 && charsRemoved != 0 && charsInserted == 0)) {
					updateBackend = true;
				}

				if (updateHighlight) {
					moveHighlights();
				}

				if (updateBackend && !ignoreBackup && (curCharBackupBuffer >= CHARS_TIL_BACKUP)) {
					curCharBackupBuffer = 0;
					pastTaggedDoc = new TaggedDocument(taggedDoc);
					main.versionControl.addVersion(pastTaggedDoc, oldSelectionIndices[0]);
				}

				if (updateBackend) {
					updateBackend = false;
					updateSentence(pastSentNum, main.documentPane.getText().substring(oldSelectionIndices[0],oldSelectionIndices[1]));
				}
			}
		};
		main.documentPane.addCaretListener(caretListener);

		documentListener = new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				if (!main.processed || ignoreChanges) {
					return;
				}

				charsInserted = e.getLength();
				curCharBackupBuffer += e.getLength();

				if (main.versionControl.isUndoEmpty() && !ignoreBackup) {
					main.versionControl.addVersion(pastTaggedDoc, e.getOffset());
					pastTaggedDoc = new TaggedDocument(taggedDoc);
				} else if (ignoreBackup) {
					pastTaggedDoc = new TaggedDocument(taggedDoc);
					return;
				}

				if (e.getLength() > 1) {
					main.versionControl.addVersion(pastTaggedDoc, e.getOffset());
					pastTaggedDoc = new TaggedDocument(taggedDoc);
				} else {
					if (InputFilter.shouldBackup) {
						main.versionControl.addVersion(pastTaggedDoc, e.getOffset());
						pastTaggedDoc = new TaggedDocument(pastTaggedDoc);
					}
				}
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				if (!main.processed || ignoreChanges) {
					return;
				}

				if (InputFilter.ignoreDeletion) {
					InputFilter.ignoreDeletion = false;
				} else {
					charsRemoved = e.getLength();
					curCharBackupBuffer += e.getLength();
				}

				if (main.versionControl.isUndoEmpty() && !ignoreBackup) {
					main.versionControl.addVersion(pastTaggedDoc, e.getOffset());
					pastTaggedDoc = new TaggedDocument(taggedDoc);
				} else if (ignoreBackup) {
					pastTaggedDoc = new TaggedDocument(taggedDoc);
					return;
				}

				if (e.getLength() > 1) {
					main.versionControl.addVersion(pastTaggedDoc, e.getOffset());
					pastTaggedDoc = new TaggedDocument(taggedDoc);
				} else {
					if (InputFilter.shouldBackup) {
						main.versionControl.addVersion(pastTaggedDoc, e.getOffset());
						pastTaggedDoc = new TaggedDocument(taggedDoc);
					}
				}
			}
			@Override
			public void changedUpdate(DocumentEvent e) {}
		};
		main.documentPane.getDocument().addDocumentListener(documentListener);

		mouseListener = new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent me) {
				if (main.documentPane.getCaret().getDot() == main.documentPane.getCaret().getMark())
					main.clipboard.setEnabled(false, false, true);
				else
					main.clipboard.setEnabled(true);
			}
			@Override
			public void mouseClicked(MouseEvent me) {}
			@Override
			public void mousePressed(MouseEvent me) {}
			@Override
			public void mouseEntered(MouseEvent me) {}
			@Override
			public void mouseExited(MouseEvent me) {}
		};
		main.documentPane.addMouseListener(mouseListener);

		reProcessListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Logger.logln(NAME+"Beginning Reprocess...");
				
				prepareReprocessing();
				main.enableEverything(false);

				taggedDoc = new TaggedDocument(main.documentPane.getText());
				main.backendInterface.process();
			}
		};
		main.reProcessButton.addActionListener(reProcessListener);
	}

	/**
	 * Prepares all threads used by the editor or related to it with
	 * their respective tasks
	 */
	private void initThreads() {
		placeWordSuggestions = new Runnable() {
			@Override
			public void run() {
				main.wordSuggestionsDriver.placeSuggestions();
			}
		};

		updateAnonymityBar = new Runnable() {
			@Override
			public void run() {
				main.anonymityBar.updateBar();
			}
		};

		moveHighlightTread = new Runnable() {
			@Override
			public void run() {
				//Clearing all sentences so they can be highlighted somewhere else
				highlighterEngine.removeAutoRemoveHighlights();
				highlighterEngine.removeSentenceHighlight();

				//If user is selecting text, don't make new highlights in this case
				if (selectionIndices[0] != selectionIndices[1]) {
					return;
				}

				Logger.logln(NAME+"Moving highlight to " + sentIndices[0] + "-" + sentIndices[1]);

				int whiteSpace = 0;
				while (Character.isWhitespace(
					main.documentPane.getText().charAt(sentIndices[0]+whiteSpace))) {
					whiteSpace++;
				}

				if (sentIndices[0]+whiteSpace <= caretPosition) {
					if (doHighlight)
						highlighterEngine.addSentenceHighlight(sentIndices[0]+whiteSpace,
							sentIndices[1]);
					if (autoHighlight)
						highlighterEngine.addAutoRemoveHighlights(sentIndices[0]+whiteSpace,
							sentIndices[1]);
				}
			}
		};
	}

	//=======================================================================
	//*						EDITOR BOOKKEEPING								*	
	//=======================================================================

	private void deletion() {
		if (ignoreEOSDeletion) {
			ignoreEOSDeletion = false;
			return;
		}

		//If we removed an EOS in the given range...
		if (taggedDoc.specialCharTracker.removeEOSesInRange(caretPosition-1, pastCaretPosition-1)) {
			/**
			 * This is usually where we discover that something
			 * was broken somewhere in the editor, which is why we
			 * encased the entire thing in a try catch and provide
			 * detailed stack traces and error messages in the
			 * catch so we can document what happened and
			 * hopefully patch it in the future.
			 */
			int[] leftSentInfo = new int[0];
			int[] rightSentInfo = new int[0];
			try {
				int[][] allSentInfo = getSentencesIndices(caretPosition, pastCaretPosition);
				leftSentInfo = allSentInfo[0];
				rightSentInfo = allSentInfo[1];

				/**
				 * If I recall correctly, this is one last ditch
				 * effort to ensure that this code isn't executed
				 * if it's not actually two different sentences
				 * but rather a rouge, undocumentented EOS
				 * character like in Ph.D., Mr., test..., etc.
				 *
				 * TODO: Double check this is what it does
				 */
				if (rightSentInfo[0] != leftSentInfo[0]) {
					/**
					 * Add '1' because we don't want to count the
					 * lower bound (e.g. if midway through
					 * sentence '6' down to midway through
					 * sentence '3' was deleted, we want to delete
					 * "6 - (3+1) = 2" TaggedSentences.
					 */
					int sentsBetweenToDelete = rightSentInfo[0] - (leftSentInfo[0]+1);
					int[] taggedSentsToDelete;

					/**
					 * Now we obtain the indices of sentences
					 * that need to be removed, which are the ones
					 * between the left and right sentence (though
					 * not including either the left or the right
					 * sentence).
					 */
					int j = 0;
					if (wholeLastSentDeleted) {
						/**
						 * We want to ignore the rightmost sentence from our deletion 
						 * process since we didn't actually delete anything from it.
						 */
						taggedSentsToDelete = new int[sentsBetweenToDelete-1];
						for (int i = (leftSentInfo[0] + 1); i < rightSentInfo[0]-1; i++) { 
							taggedSentsToDelete[j] = leftSentInfo[0] + 1;
							j++;
						}
					} else {
						/**
						 * We want to include the rightmost sentence in our deletion
						 * process since we are partially deleting some of it.
						 */
						taggedSentsToDelete = new int[sentsBetweenToDelete];
						for (int i = (leftSentInfo[0] + 1); i < rightSentInfo[0]; i++) { 
							taggedSentsToDelete[j] = leftSentInfo[0] + 1;
							j++;
						}
					}

					/**
					 * First delete every sentence the user has deleted wholly
					 * (there's no extra concatenation or tricks, just delete).
					 */
					taggedDoc.removeTaggedSentences(taggedSentsToDelete);
					Logger.logln(NAME+"Ending whole sentence deletion, now handling left and right deletion (if needed)");

					/**
					 * Then read the remaining strings from "left" and "right" sentence:
					 * 
					 * for left:
					 * read from 'leftSentInfo[1]' (the beginning of the sentence) to
					 * 'caretPosition' (where the "sentence" now ends)
					 * 
					 * for right:
					 * read from 'pastCaretPotion' (where the "sentence"
					 * now begins) to 'rightSentInfo[2]' (the end of the sentence)
					 * 
					 * Once we have the string, we call removeAndReplace, once for each
					 * sentence (String)
					 */
					String docText = main.documentPane.getText();
					String leftSentCurrent = docText.substring(leftSentInfo[1], caretPosition);
					taggedDoc.removeAndReplace(leftSentInfo[0], leftSentCurrent);

					/**
					 * Needed so that we don't delete more than we should if that be the
					 * case
					 * 
					 * TODO integrate this better with wholeLastSentDeleted and
					 * wholeBeginningSentDeleted so it's cleaner, right now this is
					 * pretty sloppy and confusing
					 */
					if (TaggedDocument.userDeletedSentence) {
						rightSentInfo[0] = rightSentInfo[0]-1;
					}
					//we need to shift our indices over by the number of characters removed.
					String rightSentCurrent = docText.substring((pastCaretPosition-charsRemoved), (rightSentInfo[2]-charsRemoved));

					if (wholeLastSentDeleted && wholeBeginningSentDeleted) {
						wholeLastSentDeleted = false;
						wholeBeginningSentDeleted = false;
						taggedDoc.removeAndReplace(leftSentInfo[0], "");
					} else if (wholeLastSentDeleted && !wholeBeginningSentDeleted) {
						wholeLastSentDeleted = false;
						taggedDoc.removeAndReplace(leftSentInfo[0]+1, "");
						taggedDoc.concatRemoveAndReplace(taggedDoc.getTaggedDocument().get(leftSentInfo[0]),leftSentInfo[0], taggedDoc.getTaggedDocument().get(leftSentInfo[0]+1), leftSentInfo[0]+1);
					} else {
						try {
							taggedDoc.removeAndReplace(leftSentInfo[0]+1, rightSentCurrent);
							taggedDoc.concatRemoveAndReplace(taggedDoc.getTaggedDocument().get(leftSentInfo[0]),leftSentInfo[0], taggedDoc.getTaggedDocument().get(leftSentInfo[0]+1), leftSentInfo[0]+1);
						} catch (Exception e1) {
							taggedDoc.removeAndReplace(leftSentInfo[0], rightSentCurrent);
						}
					}

					TaggedDocument.userDeletedSentence = false; //Resetting for next time
				}
			} catch (Exception e) {
				Logger.logln(NAME+"Error occurred while attempting to delete an EOS character.", LogOut.STDERR);
				Logger.logln(NAME+"-> leftSentInfo", LogOut.STDERR);
				if (leftSentInfo != null) {
					for (int i = 0; i < leftSentInfo.length; i++)
						Logger.logln(NAME+"\tleftSentInfo["+i+"] = " + leftSentInfo[i], LogOut.STDERR);
				} else {
					Logger.logln(NAME+"\tleftSentInfo was null!", LogOut.STDERR);
				}
				
				Logger.logln(NAME+"-> rightSentInfo", LogOut.STDERR);
				if (rightSentInfo != null) {
					for (int i = 0; i < leftSentInfo.length; i++)
						Logger.logln(NAME+"\trightSentInfo["+i+"] = " + rightSentInfo[i], LogOut.STDERR);
				} else {
					Logger.logln(NAME+"\rightSentInfo was null!", LogOut.STDERR);
				}
				
				Logger.logln(NAME+"->Document Text (What the user sees)", LogOut.STDERR);
				Logger.logln(NAME+"\t" + main.documentPane.getText(), LogOut.STDERR);
				
				Logger.logln(NAME+"->Tagged Document Text (The Backend)", LogOut.STDERR);
				int size = taggedDoc.getNumSentences();
				for (int i = 0; i < size; i++) {
					Logger.logln(NAME+"\t" + taggedDoc.getUntaggedSentences(false).get(i));
				}

				Logger.logln(e);
				return;
			}

			/** 
			 * This needs to be reset since we don't want the standard, ordinatry remove character
			 * code to be executed (since it's already taken care of here since)
			 */
			charsRemoved = 0;
		}
	}

	private void insertion() {
		/**
		 * Yet another thing that seems somewhat goofy but serves a distinct and
		 * important purpose. Since we're supposed to wait in InputFilter when the user
		 * types an EOS character since they may type more and we're not sure yet if
		 * they are actually done with the sentence, nothing will be removed, replaced,
		 * or updated until we have confirmation that it's the end of the sentence. This
		 * means that if they click/move away from the sentence after typing a period
		 * INSTEAD of finishing the sentence with a space or continuing the EOS
		 * characters, the sentence replacement will get all screwed up. This is to
		 * ensure that no matter what, when a sentence is created and we know it's a
		 * sentence it gets processed.
		 */
		if (caretPosition != pastCaretPosition && InputFilter.isEOS) {
			InputFilter.isEOS = false;
			updateBackend = true;
		}
	}

	//=======================================================================
	//*							MAIN TASKS									*	
	//=======================================================================

	/**
	 * Updates the sentence highlight indices with the new indices in
	 * sentIndices. It may seem silly to have it in it's own method like here,
	 * but it's so other external classes can call a method instead of having
	 * to invoke the runnable themselves
	 */
	protected void moveHighlights() {
		SwingUtilities.invokeLater(moveHighlightTread);
	}

	/**
	 * Calculates the indices of all passed "positions". This can ether be a
	 * since int value (meaning it's just a single caret position in the
	 * document and we're finding out it's indices), or it can be multiple int
	 * values (meaning there's a selection and we're finding out it's indices)
	 */
	protected int[][] getSentencesIndices(int ... positions) {
		int[] sentenceLengths = taggedDoc.getSentenceLengths();
		int numSents = sentenceLengths.length;
		int positionNumber;
		int numPositions = positions.length;
		int currentPosition;
		int[][] results = new int[numPositions][3];

		for (positionNumber = 0; positionNumber < numPositions; positionNumber++) {
			int i = 0;
			int length = 0;
			int selectedSentence = 0; //The sentence the user has their cursor in

			/**
			 * allSentIndices[0]:
			 * 		Length of sentence '0'
			 * allSentIndices[1]:
			 * 		Length of sentence '0' plus '1'
			 * etc...
			 */
			int[] allSentIndices = new int[numSents];
			currentPosition = positions[positionNumber];

			if (currentPosition > 0) {
				while (length <= currentPosition && i < numSents) {
					length += sentenceLengths[i];
					allSentIndices[i] = length;
					i++;
				}

				//After exiting the loop, i will be 1 more than it should
				selectedSentence = i - 1;
			}

			/** 
			 * The start and end indices of the new sentence. This could be as
             * simple as the previous sentence minus a character or as drastic
             * as two previous sentences combined
			 */
			int startIndex = 0;
			int endIndex = 0;
			
			if (selectedSentence >= numSents)
				return null; //Should never be greater than or equal to the number of sents
			else if (selectedSentence <= 0)
				endIndex = sentenceLengths[0]; //The start of the first sentence, 0.
			else {
				//Start highlighting JUST after the previous sentence stops
				startIndex = allSentIndices[selectedSentence-1];
				//Stop highlighting when the current sentence stops
				endIndex = allSentIndices[selectedSentence];
			}
			//This new sentence's number and start and end indices.
			results[positionNumber] = new int[]{selectedSentence, startIndex, endIndex};
		}

		/**
		 * We need to check if the user deleted a whole sentence at BOTH the
		 * end and beginning of their selection. This changes how we handle
		 * deleting the TaggedSentences.
		 */
		if (results.length > 1) {
			if ((results[1][0] - results[0][0]) >= 4 &&
				(results[1][2] == oldSelectionIndices[0] + (results[1][2] - results[1][1]) ||
				results[1][2] == oldSelectionIndices[1] + (results[1][2] - results[1][1]))) {
				wholeLastSentDeleted = true;
			}
			
			if (results[1][0] - results[0][0] >= 4 &&
				(results[0][2] == oldSelectionIndices[0] + (results[0][2] - results[0][1]) ||
				results[0][2] == oldSelectionIndices[1] + (results[0][2] - results[0][1]))) {
				wholeBeginningSentDeleted = true;
			}
		}

		return results;
	}

	/**
	 * Doesn't change ANYTHING in the backend (meaning the TaggedDocument),
	 * all it does is completely replace the current text in the JTextPane
	 * with the backend contents. This means we're just scanning through all
	 * the TaggedSentences and pasting all their untagged strings into the
	 * main JTextPane.<br><br>
	 *
	 * The highlighter and all the sentence, selection, and caret bookkeeping
	 * variables will also be udated to reflect any changes.
	 */
	public void refreshEditor() {
		//Updating the JTextPane
		ignoreChanges = true;
		main.documentPane.setText(taggedDoc.getUntaggedDocument(false));
		main.documentPane.getCaret().setDot(caretPosition);
		main.documentPane.setCaretPosition(caretPosition);
		ignoreChanges = false;

		//Bookkeeping
		int[] sentenceInfo = getSentencesIndices(caretPosition)[0];
		sentNum = sentenceInfo[0];			//The sentence number
		sentIndices[0] = sentenceInfo[1];	//The start of the sentence
		sentIndices[0] = sentenceInfo[2];	//The end of the sentence

		//Move the highlight to fit around any sentence changes
		moveHighlights();
	}

	/**
	 * Relaces a given sentence number with a new sentence. This DOES update
	 * the backend (meaning the TaggedDocument) by updating the given
	 * taggedSentence with the new string and bookkeeping all the information
	 * that needs it in TaggedSentence and TaggedWord to reflect the change
	 * (handled within TaggedDocument's removeAndReplace).<br><br>
	 *
	 * This also calls refreshEditor, so that means the highlighter and all
	 * the sentence, selection, and caret bookkeeeping variables will also be
	 * updated to reflect any changes. In addition, the word suggestions and
	 * anonymity bar are also updated to reflect changes.
	 * 
	 * @param sentNumToRemove 
	 *        The sentence number to update (0, 1, 2, ...)
	 * @param updatedText
	 *        The updated sentence text
	 */
	protected void updateSentence(int sentNumToRemove, String updatedText) {
		taggedDoc.removeAndReplace(sentNumToRemove, updatedText);

		refreshEditor();

		SwingUtilities.invokeLater(placeWordSuggestions);
		SwingUtilities.invokeLater(updateAnonymityBar);
	}

	/**
	 * Stops any translation threads going on (if any) and also resets
	 * the EditorDriver instance to it's default values
	 */
	public void prepareReprocessing() {
		resetToDefaults();
		main.translationsDriver.translator.reset();
		main.translationsPanel.reset();
	}

	/** 
	 * Resets all relevant variables in EditorDriver to their defaults.
	 * Used for initialization and reProcessing.
	 */
	private void resetToDefaults() {
		//Caret position
		caretPosition = 0;
		pastCaretPosition = 0;

		//Selection indices
		selectionIndices = new int[]{0,0};

		//Sentence variables
		sentNum = 0;
		sentIndices = new int[]{0,0};
		pastSentNum = 0;
		wholeLastSentDeleted = false;
		wholeBeginningSentDeleted = false;

		//Editing variables
		charsInserted = 0;
		charsRemoved = 0;

		ignoreChanges = false;
		updateBackend = false;

		curCharBackupBuffer = 0;
		ignoreBackup = false;

		highlighterEngine.clearAll();
		updateHighlight = false;
	}
}