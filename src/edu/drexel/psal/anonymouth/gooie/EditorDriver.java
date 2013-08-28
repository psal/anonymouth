package edu.drexel.psal.anonymouth.gooie;

import edu.drexel.psal.ANONConstants;
import edu.drexel.psal.anonymouth.engine.HighlighterEngine;
import edu.drexel.psal.anonymouth.helpers.FileHelper;
import edu.drexel.psal.anonymouth.utils.TaggedDocument;
import edu.drexel.psal.jstylo.generics.Logger;
import edu.drexel.psal.jstylo.generics.Logger.LogOut;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;

import javax.swing.SwingWorker;
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

	//============Assorted=======================================================
	private final String NAME = "( " + this.getClass().getSimpleName() + " )- ";
	
	private final HashSet<Character> EOS;
	private final char NEWLINE = System.lineSeparator().charAt(0);
	private final char TAB = '\t';
	private final char SPACE = ' ';
	public int watchForEOS;
	private int indexOfLastSpace;
	//we only need to worry about these kinds of abbreviations since SentenceTools takes care of the others
//	private final String[] ABBREVIATIONS = {"U.S.","R.N.","M.D.","i.e.","e.x.","e.g.","D.C.","B.C.","B.S.",
//			"Ph.D.","B.A.","A.B.","A.D.","A.M.","P.M.","r.b.i.","V.P."};
	private ActionListener reProcessListener;
	private CaretListener caretListener;
	private DocumentListener documentListener;
	private GUIMain main;
	protected SwingWorker<Void, Void> updateSuggestionsThread;
	protected SwingWorker<Void, Void> updateBarThread;

	//============Highlighters=============================================
	protected HighlighterEngine highlighterEngine;
	/**
	 * Whether or not to highlight the current sentence
	 */
	protected boolean doHighlight = PropertiesUtil.getHighlightSents();
	/**
	 * Whether or not to automatically highlight in red all words to remove
	 * in the current sentence
	 */
	protected boolean autoHighlight = PropertiesUtil.getAutoHighlight();
	private boolean placeAutoHighlightsWhenDone;

	//============Undo/Redo "Version Control"=================================
	private final int CHARS_TIL_BACKUP = 20;
	//The current character since last version was added to the undo stack
	protected int curCharBackupBuffer;
	public boolean ignoreBackup;
	protected TaggedDocument pastTaggedDoc;

	//================Editor=================================================

	public TaggedDocument taggedDoc;
	
	//Caret variables
	/**
	 * An array of 2 values that holds what will be the new position of the
	 * caret AND the end position of the selection (if there is one), and
	 * another array that holds the what the previous "newCaretPosition"s
	 * were.
	 *
	 * This is NOT the sentence indices, which is the start and end of a given
	 * sentence. Most of the time, since the user is just typing, the first
	 * and second indices of these arrays will be equal
	 */
	public int[] newCaretPosition;
	private int[] oldCaretPosition;
	public int priorCaretPosition;

	//Sentence variables
	public int sentNum;				//The current sentence number (0, 1, 2, ...)
	public int[] sentIndices;		//The indices of the sentence
	public int pastSentNum;		//The past sentence number
	private int[] pastSentIndices;
	private boolean wholeLastSentDeleted;		//Used for EOS character deletion
	private boolean wholeBeginningSentDeleted;	//Used for EOS character deletion

	//Editing variables
	protected int charsInserted;		//The number of characters inserted
	protected int charsRemoved;		//The number of characters removed

	/**
	 * Whether or not to process changes made to the text. Exists because in
	 * some instances we manipulate the data in the editor but we don't want
	 * the change listeners to fire and start running their code, otherwise
	 * everything will break.
	 */
	protected boolean ignoreChanges;
	/**
	 * In some cases (much of which are explained in InputFilter), we want to
	 * have the listener code be executed but DON'T want to update the backend
	 * immediately. This is used as a way to control whether or not that
	 * happens and when.
	 */
	public boolean updateTaggedSentence;

	//=======================================================================
	//*					INITIALIZATION AND LISTENERS						*	
	//=======================================================================

	/** 
	 * Constructor, readies all listeners used in the Document tabs.
	 */
	public EditorDriver(GUIMain main) {
		this.main = main;
		highlighterEngine = new HighlighterEngine(main);
		EOS = new HashSet<Character>();
		EOS.add('.');
		EOS.add('!');
		EOS.add('?');

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
				System.out.println("charsInserted = " + charsInserted + ", charsRemoved = " + charsRemoved);
				newCaretPosition[0] = e.getDot();
				newCaretPosition[1] = e.getMark();
				priorCaretPosition = newCaretPosition[0] - charsInserted + charsRemoved;
				updateTaggedSentence = false;

				//================ SIDE UPDATES =======================================================================
				
				//Undo/redo updates
				if (charsInserted > 0 || charsRemoved > 0) {
					updateTaggedSentence = true;
					updateUndoRedo();
				} else if (watchForEOS != -1) {
					System.out.println("MOVED AWAY, WILL TRY TO MAKE INDEPENDENT SENTENCE");
					
					taggedDoc.specialCharTracker.setIgnore(watchForEOS, false);
					watchForEOS = -1;
					updateSentence(pastSentNum, main.documentPane.getText().substring(sentIndices[0], sentIndices[1]));
				}
				
				//Copy/Cut/Paste updates
				if (newCaretPosition[0] == newCaretPosition[1])
					main.clipboard.setEnabled(false, false, true); //Cut, Copy = false, Paste = true
				else
					main.clipboard.setEnabled(true); //All three enabled (cut copy paste)

				//================ MAIN BACKEND UPDATES ================================================================

				if (charsRemoved > 0) {
					deletion(); //We MUST make sure we handle any TaggedSentence deletion if needed
					main.saved = false;
					taggedDoc.specialCharTracker.shiftAllEOSChars(false, newCaretPosition[0], charsRemoved);
				} else if (charsInserted > 0) {
					insertion(); //We MUST make sure to handle any EOS characters being added
					main.saved = false;
					taggedDoc.specialCharTracker.shiftAllEOSChars(true, newCaretPosition[0], charsInserted);
				}

				int[] selectionInfo;
				try {
					selectionInfo = getSentencesIndices(priorCaretPosition)[0];

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
				sentIndices[0] = selectionInfo[1];
				sentIndices[1] = selectionInfo[2];

				/**
				 * check to see if the current caret location is
				 * within the selectedSentIndexRange ([0] is min, [1] is max)
				 */
				//IN SENTENCE
				if (priorCaretPosition >= sentIndices[0] && priorCaretPosition < sentIndices[1]) {
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

				if (updateTaggedSentence) {
					updateTaggedSentence = false;
					updateSentence(sentNum, main.documentPane.getText().substring(sentIndices[0], sentIndices[1]));
				} else {
					//Bookkeeping
					int[] sentenceInfo = getSentencesIndices(newCaretPosition[0])[0];
					sentNum = sentenceInfo[0];			//The sentence number
					sentIndices[0] = sentenceInfo[1];	//The start of the sentence
					sentIndices[1] = sentenceInfo[2];	//The end of the sentence
					
					//Move the highlight to fit around any sentence changes
					moveHighlights();
				}

				oldCaretPosition[0] = newCaretPosition[0];
				oldCaretPosition[1] = newCaretPosition[1];
				pastSentIndices[0] = sentIndices[0];
				pastSentIndices[1] = sentIndices[1];
			}
		};
		main.documentPane.addCaretListener(caretListener);

		documentListener = new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				if (ignoreChanges) {
					charsInserted = 0;
				} else {
					charsInserted = e.getLength();
				}	
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				if (ignoreChanges) {
					charsRemoved = 0;
				} else {
					charsRemoved = e.getLength();
				}
			}

			@Override
			public void changedUpdate(DocumentEvent e) {}
		};
		main.documentPane.getDocument().addDocumentListener(documentListener);
		
		reProcessListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Logger.logln(NAME+"Beginning Reprocess...");
				
				prepareForReprocessing();
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
		prepareWordSuggestionsThread();
		prepareAnonymityBarThread();
	}
	
	/**
	 * Prepares the updateSuggestionsThread SwingWorker thread for running. This
	 * MUST be called before every new execution since by design a single
	 * SwingWorker instance cannot be executed more than once
	 */
	private void prepareWordSuggestionsThread() {
		updateSuggestionsThread = new SwingWorker<Void, Void>() {
			@Override
			public Void doInBackground() throws Exception {
				main.wordSuggestionsDriver.placeSuggestions();
				return null;
			}
			
			@Override
			public void done() {
				if (placeAutoHighlightsWhenDone) {
					placeAutoHighlightsWhenDone = false;
					
					int whiteSpace = 0;
					while (Character.isWhitespace(main.documentPane.getText().charAt(sentIndices[0]+whiteSpace))) {
						whiteSpace++;
					}

					if (sentIndices[0]+whiteSpace <= newCaretPosition[0]) {
						highlighterEngine.addAutoRemoveHighlights(sentIndices[0]+whiteSpace, sentIndices[1]);
					}
				}
			}
		};
	}
	
	/**
	 * Prepares the updateBarThread SwingWorker thread for running. This
	 * MUST be called before every new execution since by design a single
	 * SwingWorker instance cannot be executed more than once
	 */
	private void prepareAnonymityBarThread() {
		updateBarThread = new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				main.anonymityBar.updateBar();
				return null;
			}
		};
	}

	//=======================================================================
	//*						EDITOR BOOKKEEPING								*	
	//=======================================================================

	/**
	 * Handles all the Deletion bookkeeping by managing whether or not it was
	 * an EOS character being deleted. If it was, it takes care of combining
	 * the sentences into a new one for you and deleting any sentences in
	 * between.
	 */
	private void deletion() {
		//If we removed an EOS in the given range...
		if (taggedDoc.specialCharTracker.removeEOSesInRange(newCaretPosition[0], priorCaretPosition)) {
			/**
			 * This is usually where we discover that something
			 * was broken somewhere in the editor, which is why we
			 * encased the entire thing in a try catch and provide
			 * detailed stack traces and error messages in the
			 * catch so we can document what happened and
			 * hopefully patch it in the future.
			 */
			watchForEOS = newCaretPosition[0];
			updateTaggedSentence = false;
			int[] leftSentInfo = new int[0];
			int[] rightSentInfo = new int[0];
			try {
				int[][] allSentInfo = getSentencesIndices(newCaretPosition[0], priorCaretPosition);
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
					 * 'newCaretPosition' (where the "sentence" now ends)
					 * 
					 * for right:
					 * read from 'pastCaretPotion' (where the "sentence"
					 * now begins) to 'rightSentInfo[2]' (the end of the sentence)
					 * 
					 * Once we have the string, we call removeAndReplace, once for each
					 * sentence (String)
					 */
					String docText = main.documentPane.getText();
					String leftSentCurrent = docText.substring(leftSentInfo[1], newCaretPosition[0]);
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
					String rightSentCurrent = docText.substring((priorCaretPosition-charsRemoved), (rightSentInfo[2]-charsRemoved));

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
					Logger.logln(NAME+"\"" + main.documentPane.getText().substring(leftSentInfo[0], leftSentInfo[1]) + "\"");
				} else {
					Logger.logln(NAME+"\tleftSentInfo was null!", LogOut.STDERR);
				}
				
				Logger.logln(NAME+"-> rightSentInfo", LogOut.STDERR);
				if (rightSentInfo != null) {
					for (int i = 0; i < leftSentInfo.length; i++)
						Logger.logln(NAME+"\trightSentInfo["+i+"] = " + rightSentInfo[i], LogOut.STDERR);
					Logger.logln(NAME+"\"" + main.documentPane.getText().substring(rightSentInfo[0], rightSentInfo[1]) + "\"");
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

			Logger.logln(NAME+"\"" + main.documentPane.getText().substring(leftSentInfo[0], leftSentInfo[1]) + "\"");
			Logger.logln(NAME+"\"" + main.documentPane.getText().substring(rightSentInfo[0], rightSentInfo[1]) + "\"");
			
			/** 
			 * This needs to be reset since we don't want the standard, ordinatry remove character
			 * code to be executed (since it's already taken care of here since)
			 */
			charsRemoved = 0;
		} else {
			watchForEOS = -1;
			updateTaggedSentence = true;
		}
		watchForEOS = -1;
	}

	/**
	 * Handles all the Insertion bookkeeping by checking to see if the user is
	 * adding an EOS character. If they are, it makes the appropriate changes
	 * such that the single sentence is then split in two in the rest of the
	 * main document listener.
	 */
	private void insertion() {
		if (charsInserted > 1) {
			if (watchForEOS != -1) {
				char beginningChar = main.documentPane.getText().charAt(priorCaretPosition);
				
				if (beginningChar == SPACE || beginningChar == NEWLINE || beginningChar == TAB) {
					updateTaggedSentence = true;

					taggedDoc.specialCharTracker.setIgnore(watchForEOS, false);
					
					if (updateSuggestionsThread.isDone()) {
						prepareWordSuggestionsThread();
						updateSuggestionsThread.execute();
					}
					
				} else {
					updateTaggedSentence = true;
				}
				
				watchForEOS = -1;
			} else {
				updateTaggedSentence = true;
			}
		} else {
			if (watchForEOS != -1) {
				char newChar = main.documentPane.getText().charAt(priorCaretPosition);
				
				if (newChar == SPACE || newChar == NEWLINE || newChar == TAB) {
					System.out.println("ACTUALLY IS EOS CHARACTER");
					updateTaggedSentence = true;
					System.out.println("\"" + main.documentPane.getText().charAt(watchForEOS) + "\", " + watchForEOS);
					taggedDoc.specialCharTracker.setIgnore(watchForEOS, false);
					
					if (updateSuggestionsThread.isDone()) {
						prepareWordSuggestionsThread();
						updateSuggestionsThread.execute();
					}
				} else {
//					System.out.println("PROBLEM 1");
					updateTaggedSentence = true;
				}
				
				watchForEOS = -1;
			} else {
				char newChar = main.documentPane.getText().charAt(priorCaretPosition);
				if (EOS.contains(newChar)) {
					watchForEOS = priorCaretPosition;
					System.out.println("ENTERED EOS CHARACTER, WILL WAIT");
					System.out.println("\"" + main.documentPane.getText().charAt(watchForEOS) + "\", " + watchForEOS);
					
					taggedDoc.specialCharTracker.addEOS(newChar, watchForEOS, true);
					updateTaggedSentence = true;
				} else {
//					System.out.println("PROBLEM 2");
				}
			}
		}
		updateTaggedSentence = true;
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
	public void moveHighlights() {
		//Clearing all sentences so they can be highlighted somewhere else
		highlighterEngine.removeAutoRemoveHighlights();
		highlighterEngine.removeSentenceHighlight();
		System.out.println("TESTING TO MOVE HIGHLIGHT...");
		System.out.println(newCaretPosition[0] + ", " + newCaretPosition[1]);
		//If user is selecting text, don't make new highlights in this case
		if (newCaretPosition[0] != newCaretPosition[1]) {
			return;
		}

		Logger.logln(NAME+"Moving highlight to " + sentIndices[0] + "-" + sentIndices[1]);

		int whiteSpace = 0;
		while (Character.isWhitespace(main.documentPane.getText().charAt(sentIndices[0]+whiteSpace))) {
			System.out.println("\"" + main.documentPane.getText().charAt(sentIndices[0]+whiteSpace) + "\"");
			whiteSpace++;
		}

		System.out.println("HIGHLIGHT? " + (sentIndices[0]+whiteSpace) + " <= " + newCaretPosition[0]);
		if (sentIndices[0]+whiteSpace <= newCaretPosition[0]) {
			System.out.println("They're highlighting");
			if (doHighlight)
				highlighterEngine.addSentenceHighlight(sentIndices[0]+whiteSpace, sentIndices[1]);
			
			if (autoHighlight && updateSuggestionsThread.isDone())
				highlighterEngine.addAutoRemoveHighlights(sentIndices[0]+whiteSpace, sentIndices[1]);
			else
				placeAutoHighlightsWhenDone = true;
		} else {
			System.out.println("Should not be highlighting");
		}
	}

	/**
	 * Handles everything related to the undo/redo functionality. It
	 * automatically backs up an undo "version" when the buffer's ready or
	 * when the user is manipulating chunks of text. You should just have to
	 * call this once at the beginning of the main document listener.
	 */
	protected void updateUndoRedo() {	
		if (ignoreBackup) {
			pastTaggedDoc = new TaggedDocument(taggedDoc);
			return;
		}

		if (charsInserted > 1 || charsRemoved > 1) {
			main.versionControl.addVersion(pastTaggedDoc, priorCaretPosition);
			pastTaggedDoc = new TaggedDocument(taggedDoc);
		} else if (curCharBackupBuffer >= CHARS_TIL_BACKUP) {
			curCharBackupBuffer = 0;
			pastTaggedDoc = new TaggedDocument(taggedDoc);
			main.versionControl.addVersion(pastTaggedDoc, priorCaretPosition);
		}
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
					(results[1][2] == oldCaretPosition[0] + (results[1][2] - results[1][1]) ||
					results[1][2] == oldCaretPosition[1] + (results[1][2] - results[1][1]))) {
				wholeLastSentDeleted = true;
			}

			if (results[1][0] - results[0][0] >= 4 &&
					(results[0][2] == oldCaretPosition[0] + (results[0][2] - results[0][1]) ||
					results[0][2] == oldCaretPosition[1] + (results[0][2] - results[0][1]))) {
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
		main.documentPane.getCaret().setDot(newCaretPosition[0]);
		main.documentPane.setCaretPosition(newCaretPosition[0]);
		ignoreChanges = false;
		
		//Bookkeeping
		int[] sentenceInfo = getSentencesIndices(newCaretPosition[0])[0];
		sentNum = sentenceInfo[0];			//The sentence number
		sentIndices[0] = sentenceInfo[1];	//The start of the sentence
		sentIndices[1] = sentenceInfo[2];	//The end of the sentence
		
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
	public void updateSentence(int sentNumToRemove, String updatedText) {
		Logger.logln(NAME+"Updating sentence # = " + sentNumToRemove + " with new string: " + updatedText);
		taggedDoc.removeAndReplace(sentNumToRemove, updatedText);

		if (updateBarThread.isDone()) {
			prepareAnonymityBarThread();
			updateBarThread.execute();
		}
		
		//Bookkeeping
		int[] sentenceInfo = getSentencesIndices(newCaretPosition[0])[0];
		sentNum = sentenceInfo[0];			//The sentence number
		sentIndices[0] = sentenceInfo[1];	//The start of the sentence
		sentIndices[1] = sentenceInfo[2];	//The end of the sentence

		//Move the highlight to fit around any sentence changes
		moveHighlights();
	}

	/**
	 * Stops any translation threads going on (if any) and also resets
	 * the EditorDriver instance to it's default values
	 */
	public void prepareForReprocessing() {
		resetToDefaults();
		main.translationsDriver.translator.reset();
		main.translationsPanel.reset();
	}

	/** 
	 * Resets all relevant variables in EditorDriver to their defaults.
	 * Used for initialization and reProcessing.
	 */
	private void resetToDefaults() {
		//Selection indices
		newCaretPosition = new int[]{0,0};
		oldCaretPosition = new int[]{0,0};
		priorCaretPosition = 0;

		//Sentence variables
		sentNum = 0;
		sentIndices = new int[]{0,0};
		pastSentNum = 0;
		pastSentIndices = new int[]{0,0};
		wholeLastSentDeleted = false;
		wholeBeginningSentDeleted = false;

		//Editing variables
		charsInserted = 0;
		charsRemoved = 0;

		ignoreChanges = false;
		updateTaggedSentence = false;

		curCharBackupBuffer = 0;
		ignoreBackup = false;

		highlighterEngine.clearAll();
		watchForEOS = -1;
		placeAutoHighlightsWhenDone = false;
		indexOfLastSpace = 0;
	}
}