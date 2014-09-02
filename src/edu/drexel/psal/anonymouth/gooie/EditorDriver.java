package edu.drexel.psal.anonymouth.gooie;

import edu.drexel.psal.anonymouth.engine.HighlighterEngine;
import edu.drexel.psal.anonymouth.utils.ConsolidationStation;
import edu.drexel.psal.anonymouth.utils.SentenceMaker;
import edu.drexel.psal.anonymouth.utils.TaggedDocument;
import edu.drexel.psal.jstylo.generics.Logger;
import edu.drexel.psal.jstylo.generics.Logger.LogOut;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
* @author Marc Barrowclift
* @author Andrew W.E. McDonald
*/
public class EditorDriver {

        //============Assorted=================================================

        private final String NAME = "( " + this.getClass().getSimpleName() + " )- ";
        private GUIMain main;
        public SentenceMaker sentenceMaker;

        //----------- Listeners -----------------------------------------------
        private ActionListener reProcessListener;
        private CaretListener caretListener;
        private DocumentListener documentListener;

        //----------- Threads -------------------------------------------------
        public SwingWorker<Void, Void> updateSuggestionsThread;
        public SwingWorker<Void, Void> updateBarThread;

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
        /**
         * In some cases, the updateSuggestionsThread will still be excuting (thus
         * updating the suggestions that auto highlights uses) when the call is
         * made to calculate the indices for the automatic highlights. When this
         * is the case, we switch this to true and the updateSuggestionsThread
         * will then do auto highlights upon completion.
         */
        private boolean placeAutoHighlightsWhenDone;
        /**
         * Whether or not any text within the editor has changed. This is used
         * because we don't want to call updateSentence and go through all that
         * hullabaloo in the backend if nothing actually changed. Thus, we have
         * this boolean to make sure we only do that when necessary.
         */
        private boolean textChanged;
        
        //================Editor=================================================

        public TaggedDocument taggedDoc;

        //----------- Caret Variables -------------------------------------------
        /**
         * An array of 2 values that holds what will be the new position of the
         * caret AND the end position of the selection (if there is one), and
         * another array that holds the what the previous "newCaretPosition"s
         * were.<br><br>
         *
         * This is NOT the sentence indices, which is the start and end of a given
         * sentence. Most of the time, since the user is just typing, the first
         * and second indices of these arrays will be equal.
         */
        public int[] newCaretPosition;
        /**
         * Holds the newCaretPosition values from the previous caret event.
         */
        private int[] oldCaretPosition;
        /**
         * NOT the same thing as oldCaretPosition! PriorCaretPosition is new caret
         * position PRIOR to having any characters inserted or removed. This means
         * that if the user's just moving the cursor around and not typing
         * anything it will be equal to newCaretPosition[0] since charsRemoved and
         * charsInserted are 0. However, if the user has typed or pasted text this
         * will reflect the length of those changes with respect to
         * newCaretPosition[0]
         */
        public int priorCaretPosition;
        
        //------------- Sentence Variables --------------------------------------
        /**
         * The current sentence number (0, 1, 2, ...)
         */
        public int sentNum;
        /**
         * The indices of the sentence (meaning the position immediately after the
         * previous sentence ended to this sentence's end.)
         */
        public int[] sentIndices;
        /**
         * Integer array with 4 indices that represent:<br>
         *                 [0] = Where the start of the highlight should be<br>
         *                 [1] = Where the end of the highlight should be<br>
         *                 [2] = The number of extra sentences included in the extended highlight to the LEFT<br>
         *                 [3] = The number of extra sentences included in the extended highlight to the RIGHT<br><br>
         *
         * It's sole purpose is to let us extend the sentence highlights beyond
         * what sentIndices so that sentences that should be considered one whole
         * sentence are combined to one full highlight.
         */                 
        private int[] highlightIndices;
        /**
         * The old sentNum value from the prievous caret event.
         */
        public int pastSentNum;
        /**
         * The old sentence indices from the previous caret event.
         */
        public int[] pastSentIndices;

        //--------------- Editing variables -------------------------------------
        /**
         * The number of characters inserted into the document this caret event
         * (default is 0 is none were added)
         */
        protected int charsInserted;
        /**
         * The number of characters removed from the document this caret event
         * (default is 0 if none were removed)
         */
        protected int charsRemoved;

        /**
         * Whether or not to process changes made to the JTextPane. Exists because
         * sometimes we remove all text from the pane and replace it with new text
         * (like in syncTextPaneWithTaggedDoc() and VersionControl's undo and
         * redo) but we don't want the changes to actually be processed since it
         * would break things.
         */
        public boolean ignoreChanges;
        
        //=======================================================================
        //*                                        INITIALIZATION AND LISTENERS                                                *        
        //=======================================================================

        /**
         * Constructor, readies all listeners used in the Document tabs.
         */
        public EditorDriver(GUIMain main) {
                this.main = main;
                highlighterEngine = new HighlighterEngine(main);
                sentenceMaker = new SentenceMaker(main);

                initListeners();
                initThreads();
                resetToDefaults();
        }

        private void initListeners() {
                /**
                 * The main document event listener, this fires whenever the user is
                 * typing or moving around in the document.
                 */
                caretListener = new CaretListener() {
                        @Override
                        public void caretUpdate(CaretEvent e) {
                                //Don't go any further if we're supposed to ignore the event, it would break things
                                if (ignoreChanges) {
                                        return;
                                }

                                Logger.logln(NAME+"=============================================================================");
                                textChanged = false;
                                pastSentNum = sentNum;
                                newCaretPosition[0] = e.getDot();
                                newCaretPosition[1] = e.getMark();
                                priorCaretPosition = newCaretPosition[0] - charsInserted + charsRemoved;

                                //================ SIDE UPDATES =======================================================================

                                //------------------------- Undo/Redo updates -------------------------                                
                                //If the user has pasted or deleted/cut a chunk of text, always immediately backup past version
                                if (charsInserted > 1 || charsRemoved > 1) {                                        
                                        main.versionControl.updateUndoRedo(taggedDoc, priorCaretPosition, true);
                                //Otherwise, do a standard update
                                } else if (charsInserted == 1 || charsRemoved == 1) {
                                        main.versionControl.updateUndoRedo(taggedDoc, priorCaretPosition, false);
                                }
                                
                                //------------------------- Copy/Cut/Paste updates -------------------------
                                if (newCaretPosition[0] == newCaretPosition[1])
                                        main.clipboard.setEnabled(false, false, true); //Cut, Copy = false, Paste = true
                                else
                                        main.clipboard.setEnabled(true); //All three enabled (cut copy paste)

                                //================ MAIN BACKEND UPDATES ================================================================
                                
                                /**
                                 * If the user had previous typed an EOS character and we were
                                 * unsure whether or not it was an actual EOS and not an
                                 * abbreviation, ellipses, etc. AND they have navigated away
                                 * from said EOS character (clicking somewhere else in the
                                 * document to edit another sentence), we will assume that
                                 * what was the end of the sentence and update it as such.
                                 */
                                if (taggedDoc.watchForEOS != -1 && charsInserted == 0 && charsRemoved == 0) {
                                        if (taggedDoc.specialCharTracker.isSentenceEndAtIndex(taggedDoc.watchForEOS+1)) {
                                                taggedDoc.watchForEOS = -1;
                                        } else {
                                                taggedDoc.specialCharTracker.setIgnoreEOS(taggedDoc.watchForEOS, false);
                                                updateSentence(pastSentNum, main.documentPane.getText().substring(sentIndices[0], sentIndices[1]));
                                                taggedDoc.watchForEOS = -1;
                                        }
                                }
                                //Same as above, but for EOS characters at the end of the document, which are tracked separately
                                if (taggedDoc.watchForLastSentenceEOS != -1 && charsInserted == 0 && charsRemoved == 0) {
                                    if (taggedDoc.specialCharTracker.isSentenceEndAtIndex(taggedDoc.watchForLastSentenceEOS+1)) {
                                            taggedDoc.watchForLastSentenceEOS = -1;
                                    } else {
                                            taggedDoc.specialCharTracker.setIgnoreEOS(taggedDoc.watchForLastSentenceEOS, false);
                                            updateSentence(pastSentNum, main.documentPane.getText().substring(sentIndices[0], sentIndices[1]));
                                            taggedDoc.watchForLastSentenceEOS = -1;
                                    }
                                }

                                if (charsRemoved > 0) {
                                        textChanged = true;
                                        main.documentSaved = false; //The document was changed, therefore not saved

                                        taggedDoc.incrementDocumentLength(charsRemoved * -1);

                                        //If we are unsure about an EOS character at the end of the doc, keep track of it.
                                        if (taggedDoc.watchForLastSentenceEOS != -1) {
                                                taggedDoc.watchForLastSentenceEOS -= charsRemoved;
                                        }

                                        deletion(); //We MUST make sure we handle any TaggedSentence deletion if needed

                                        taggedDoc.specialCharTracker.shiftAll(newCaretPosition[0], charsRemoved*-1);
                                } else if (charsInserted > 0) {
                                        textChanged = true;
                                        main.documentSaved = false; //The document was changed, therefore not saved

                                        taggedDoc.incrementDocumentLength(charsInserted);

                                        /**
                                         * Must be prior to insertion() call (unlike deletion above) since, if
                                         * the user is adding an eos character right before an existing one, it
                                         * would then add them both to the same index, not good. This way, they
                                         * get added the correct way and nothing get's screwed up.
                                         */
                                        taggedDoc.specialCharTracker.shiftAll(priorCaretPosition, charsInserted);
                                        
                                        //If we are unsure about an EOS character at the end of the doc, keep track of it.
                                        //But we only need to increase it if the character we're tracking is after the prior caret position
                                        if (taggedDoc.watchForLastSentenceEOS != -1  && taggedDoc.watchForLastSentenceEOS > priorCaretPosition) {
                                                taggedDoc.watchForLastSentenceEOS += charsInserted;
                                        }

                                        insertion(); //We MUST make sure to handle any EOS characters being added
                                }

                                /**
                                 * Updating the sentIndices depending on how much they've inserted or removed.
                                 */
                                sentIndices[1] += charsInserted;
                                sentIndices[1] -= charsRemoved;
                                charsRemoved = 0;
                                charsInserted = 0;

                                /**
                                 * Only update the sentence in the backend if it actually
                                 * changed (duh)
                                 */
                                if (textChanged) {
                                        /**
                                         * We need to do a little cleaning up with the end
                                         * sentence index sometimes with respect to the last
                                         * sentence since we don't want a string index out of
                                         * bounds exception for highlighting and other things
                                         * going wrong.
                                         */
                                        while (sentIndices[1] > taggedDoc.length) {
                                                sentIndices[1]--;
                                        }
                                        if (main.documentPane.getText() != "")
                                        	updateSentence(sentNum, main.documentPane.getText().substring(sentIndices[0], sentIndices[1]));
                                        
                                        updateEditorVariables();
                                        updateBarAndHighlight();
                                } else {
                                        updateEditorVariables();
                                        updateBarAndHighlight();
                                }
                                
                                /**
                                 * If we're selecting a different sentence now, we must update
                                 * the translations panel to display its translations, a blank
                                 * window with the translate button, or the progress bar with
                                 * the currently number of translations (all figured out
                                 * within updateTranslationsPanel())
                                 */
                                if (sentNum != pastSentNum) {
                                        main.translationsPanel.updateTranslationsPanel(taggedDoc.getSentenceNumber(sentNum));
                                }
                                
                                //Preparing everything for the next event
                                charsRemoved = 0;
                                charsInserted = 0;
                                oldCaretPosition[0] = newCaretPosition[0];
                                oldCaretPosition[1] = newCaretPosition[1];
                                pastSentIndices[0] = sentIndices[0];
                                pastSentIndices[1] = sentIndices[1];
                        }
                };
                main.documentPane.addCaretListener(caretListener);

                /**
                 * Needed so we can catch and updated the insert or remove variables for
                 * the caret event updates. This gets fired BEFORE the caret listener.
                 */
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
                                main.resultsWindow.reset();
                                main.enableEverything(false);
                                taggedDoc = new TaggedDocument(main, main.documentPane.getText(), false);
                                taggedDoc.setDocumentLength(main.documentPane.getText());
                                main.documentProcessor.process();
                        }
                };
                main.reProcessButton.addActionListener(reProcessListener);
        }

        /**
         * Prepares all threads used by the editor or related to it with
         * their respective tasks
         */
        public void initThreads() {
                prepareWordSuggestionsThread();
                prepareAnonymityBarThread();
        }
        
        /**
         * Prepares the updateSuggestionsThread SwingWorker for running. This
         * MUST be called before every new execution since by design a single
         * SwingWorker instance cannot be executed more than once.
         */
        private void prepareWordSuggestionsThread() {
                updateSuggestionsThread = new SwingWorker<Void, Void>() {
                        @Override
                        public Void doInBackground() throws Exception {
                        	System.out.println("In thread prepareWordSuggestions - doInBackGround!!!!!!!!!!!");
                        		//If we're not doing the first process or reprocessing, 
                        		//Then we're just refreshing, and we have to show the progress window
                        		if (main.processed && !main.reprocessing && !main.documentProcessor.pw.isVisible()) {
                        			main.documentProcessor.pw.run();
                        			main.documentProcessor.pw.setText("Loading Word Suggestions");
                        		}
                                main.enableEverything(false);
                                ignoreChanges = true;
                                main.wordSuggestionsDriver.placeSuggestions();
                                return null;
                        }
                        
                        @Override
                        public void done() {
                                if (placeAutoHighlightsWhenDone) {
                                        placeAutoHighlightsWhenDone = false;
                                        
                                        int whiteSpace = getWhiteSpaceBuffer(highlightIndices[0]);
                                        if (highlightIndices[0]+whiteSpace <= newCaretPosition[0]) {
                                                highlighterEngine.addAutoRemoveHighlights(highlightIndices[0]+whiteSpace, highlightIndices[1], highlightIndices[2], highlightIndices[3]);
                                        }
                                }
                                main.enableEverything(true);
                                ignoreChanges = false;
                                //If we're not processing or reprocessing, we're just refreshing
                                //and we have to stop the ProgressWindow here
                                if (main.processed && !main.reprocessing && main.documentProcessor.pw.isVisible()) {
                        			main.documentProcessor.pw.stop();
                        		}
                        }
                };
        }
        
        /**
         * Prepares the updateBarThread SwingWorker thread for running. This
         * MUST be called before every new execution since by design a single
         * SwingWorker instance cannot be executed more than once
         */
        public void prepareAnonymityBarThread() {
                updateBarThread = new SwingWorker<Void, Void>() {
                        @Override
                        protected Void doInBackground() throws Exception {
                                main.anonymityBar.updateBar();
                                return null;
                        }
                };
        }

        //=======================================================================
        //*                                                EDITOR BOOKKEEPING                                                                *        
        //=======================================================================

        /**
         * Handles all the Deletion bookkeeping by managing whether or not it was
         * an EOS character being deleted. If it was, it takes care of combining
         * the sentences into a new one for you and deleting any sentences in
         * between.
         *
         * This was basically all Andrew McDonald's work, this remained virtually
         * unchanged during the rewrite because it works so damn well (the only
         * times it breaks is when something ELSE has). So big shoutout to him for
         * making this a whole lot easier.
         */
        private void deletion() {
                //Remove all Quotes and Parenthesis in range
                taggedDoc.specialCharTracker.removeTextWrappersInRange(newCaretPosition[0], priorCaretPosition);
                
                //If we removed an EOS in the given range...
                if (taggedDoc.specialCharTracker.removeEOSesInRange(newCaretPosition[0], priorCaretPosition)) {
                        
                        
                        /**
                         * If the user is editing after the last tagged sentence and
                         * decides to delete their EOS character before it was set as a
                         * sentence end we want to reset the watch variable (otherwise
                         * we'd keep watching an EOS character that no longer exists)
                         */
                        if ((taggedDoc.watchForLastSentenceEOS+charsRemoved) >= newCaretPosition[0] &&
                                        (taggedDoc.watchForLastSentenceEOS+charsRemoved) < priorCaretPosition) {
                                taggedDoc.watchForLastSentenceEOS = -1;
                        }
                        
                        int[] leftSentInfo = new int[0];
                        int[] rightSentInfo = new int[0];
                        try {
                                int[][] allSentInfo = getSentencesIndices(newCaretPosition[0], priorCaretPosition);
                                // The order of sentences can be reversed in some cases
                                if (allSentInfo[0][0] < allSentInfo[1][0]) {
                                	leftSentInfo = allSentInfo[0];
                                    rightSentInfo = allSentInfo[1];
                                } else {
                                	leftSentInfo = allSentInfo[1];
                                    rightSentInfo = allSentInfo[0];
                                }

                                if (rightSentInfo[0] != leftSentInfo[0]) {
                                	
	                                	/**
	                                     * We're going to be handling all the TaggedDocument manipulation HERE
	                                     * instead of in the standard updateSentence() method call at the end
	                                     * of the listener since we have a lot more work to do than a standard
	                                     * change. Therefore, since we are already updating the backend here,
	                                     * we should NOT update it an additional time, otherwise it screws everything
	                                     * up.
	                                     */
	                                    textChanged = false;
	                                    
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
                                        taggedSentsToDelete = new int[sentsBetweenToDelete];
                                        for (int i = (leftSentInfo[0] + 1); i < rightSentInfo[0]; i++) {
                                                taggedSentsToDelete[j] = leftSentInfo[0] + 1;
                                                j++;
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
                                         * TODO integrate this better within EditorDriver so it's
                                         * cleaner, right now this is pretty sloppy and confusing
                                         */
                                        if (taggedDoc.userDeletedSentence) {
                                                rightSentInfo[0] = rightSentInfo[0]-1;
                                        }
                                        //we need to shift our indices over by the number of characters removed.
                                        String rightSentCurrent = docText.substring((priorCaretPosition-charsRemoved), (rightSentInfo[2]-charsRemoved));

                                        try {
                                                taggedDoc.removeAndReplace(leftSentInfo[0]+1, rightSentCurrent);
                                                taggedDoc.concatRemoveAndReplace(taggedDoc.getTaggedDocument().get(leftSentInfo[0]), leftSentInfo[0], taggedDoc.getTaggedDocument().get(leftSentInfo[0]+1), leftSentInfo[0]+1);
                                        } catch (Exception e1) {
                                                taggedDoc.removeAndReplace(leftSentInfo[0], rightSentCurrent);
                                        }

                                        /**
                                         * If the user is deleting a chunk of text at the end of a document
                                         * and stops just at an existing EOS character, editing at the end
                                         * of a document will no longer split up sentences properly. That's
                                         * what this check is for, if they delete and stop before an EOS
                                         * character at the end of a sentence then we will make THAT the new
                                         * EOS character we are watching for with watchForLastSentenceEOS
                                         */
                                        if (sentNum == taggedDoc.numOfSentences-1 && taggedDoc.length > 0) {
                                                if (taggedDoc.specialCharTracker.isEOS(main.documentPane.getText().charAt(taggedDoc.length-1))) {
                                                        taggedDoc.watchForLastSentenceEOS = taggedDoc.length - 1;
                                                }
                                        }
                                        
                                        /**
                                         * In the extreme case that the entire document was deleted
                                         * Make sure we have an empty sentence to type new text into
                                         */
                                        if  (taggedDoc.numOfSentences == 0) {
                                        	taggedDoc.makeNewEndSentence("");
                                        	taggedDoc.endSentenceExists = true;
                                        }

                                        taggedDoc.userDeletedSentence = false; //Resetting for next time
                                }
                        } catch (Exception e) {
                                Logger.logln(NAME+"Error occurred while attempting to delete an EOS character.", LogOut.STDERR);
                                Logger.logln(NAME+"Document length = " + main.documentPane.getText().length());
                                Logger.logln(NAME+"Tagged Document length = " + taggedDoc.getUntaggedDocument().length());
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
                                        try {
                                                Logger.logln(NAME+"\"" + main.documentPane.getText().substring(rightSentInfo[0], rightSentInfo[1]) + "\"");
                                        } catch (Exception e1) {}
                                } else {
                                        Logger.logln(NAME+"\rightSentInfo was null!", LogOut.STDERR);
                                }
                                
                                Logger.logln(NAME+"->Document Text (What the user sees)", LogOut.STDERR);
                                Logger.logln(NAME+"\t" + main.documentPane.getText(), LogOut.STDERR);
                                
                                Logger.logln(NAME+"->Tagged Document Text (The Backend)", LogOut.STDERR);
                                int size = taggedDoc.numOfSentences;
                                for (int i = 0; i < size; i++) {
                                        Logger.logln(NAME+"\t" + taggedDoc.getUntaggedSentences().get(i), LogOut.STDERR);
                                }

                                Logger.logln(e);
                                return;
                        }
                }
        }

        /**
         * Handles all the Insertion bookkeeping by checking to see if the user is
         * adding an EOS character. If they are, it makes the appropriate changes
         * such that the single sentence is then split in two in the rest of the
         * main document listener.
         */
        private void insertion() {
        		char newChar;
                /**
                 * If the user is pasting text in (meaning the charsInserted is greater
                 * than a single character) we will want to parse through the entire text
                 * and handle each EOS character we find like we would had the user typed
                 * it all in character by character
                 */
                if (charsInserted > 1) {
	                int tempIndex = priorCaretPosition;
	                while (tempIndex < newCaretPosition[0]) {
	                        newChar = main.documentPane.getText().charAt(tempIndex);
	                        if (taggedDoc.watchForEOS != -1) {
	                                if (Character.isWhitespace(newChar)) {
	                                        taggedDoc.specialCharTracker.setIgnoreEOS(taggedDoc.watchForEOS, false);
	                                }
	                                
	                                taggedDoc.watchForEOS = -1;
	                        } else if (taggedDoc.watchForLastSentenceEOS != -1 && newCaretPosition[0] == taggedDoc.length) {
	                                if (Character.isWhitespace(newChar)) {
	                                        taggedDoc.specialCharTracker.setIgnoreEOS(taggedDoc.watchForLastSentenceEOS, false);
	                                }
	                                
	                                taggedDoc.watchForLastSentenceEOS = -1;
	                        } else {
	                                if (taggedDoc.specialCharTracker.isEOS(newChar)) {
	                                        if (taggedDoc.length == priorCaretPosition+charsInserted) {
	                                                taggedDoc.watchForLastSentenceEOS = tempIndex;
	                                                taggedDoc.specialCharTracker.addEOS(newChar, taggedDoc.watchForLastSentenceEOS, true);
	                                        } else {
	                                                taggedDoc.watchForEOS = tempIndex;
	                                                taggedDoc.specialCharTracker.addEOS(newChar, taggedDoc.watchForEOS, true);
	                                        }
	                                } else {
	                                        trackCharIfSpecial(newChar, priorCaretPosition);
	                                }
	                        }                                        
	                        
	                        tempIndex++;
	                }
                        
                /**
                 * Otherwise the user's just entering a single character, in which case we
                 * just analyze the single character.
                 */
                } else {
                		newChar = main.documentPane.getText().charAt(priorCaretPosition);
                        //If we're keeping an eye out for a possible end of sentence...
                        if (taggedDoc.watchForEOS != -1) {
                                if (Character.isWhitespace(newChar)) {
                                        if (sentNum == taggedDoc.numOfSentences - 1) {
                                                taggedDoc.endSentenceExists = false;
                                        }
                                        taggedDoc.specialCharTracker.setIgnoreEOS(taggedDoc.watchForEOS, false);
                                }
                                
                                taggedDoc.watchForEOS = -1;
                        //If we're keeping an eye out for a possible end of sentence at the VERY END of the document
                        } else if (taggedDoc.watchForLastSentenceEOS != -1 && newCaretPosition[0] == taggedDoc.length) {        
                                if (Character.isWhitespace(newChar)) {
                                        taggedDoc.specialCharTracker.setIgnoreEOS(taggedDoc.watchForLastSentenceEOS, false);
                                }
                                
                                taggedDoc.watchForLastSentenceEOS = -1;
                        //If we're not currently watching for any end of sentence, then check the new character
                        } else {
                                if (taggedDoc.specialCharTracker.isEOS(newChar)) {
                                        if (sentNum == taggedDoc.numOfSentences-1) {
                                                taggedDoc.watchForLastSentenceEOS = priorCaretPosition;
                                                taggedDoc.specialCharTracker.addEOS(newChar, taggedDoc.watchForLastSentenceEOS, true);
                                        } else {
                                                taggedDoc.watchForEOS = priorCaretPosition;
                                                taggedDoc.specialCharTracker.addEOS(newChar, taggedDoc.watchForEOS, true);
                                        }
                                } else {
                                        trackCharIfSpecial(newChar, priorCaretPosition);
                                }
                        }
                }
        }

        //=======================================================================
        //*                                                        OTHER TASKS                                                                        *        
        //=======================================================================

        /**
         * Checks to see if the passed character is considered "special" and
         * tracks it with the appropriate object if it is.<br><br>
         *
         * This does NOT check EOS characters since in both instances where we
         * check for it above we run different code on the result, so I can't have
         * the general case here. I consider this to be pretty poor oragnization
         * as this should probably be in SpecialCharacterTracker instead
         *
         * @param newChar
         * The new character we want to determine if special or not
         * @param position
         * The index of the character we are testing
         */
        public void trackCharIfSpecial(char newChar, int position) {
                if (taggedDoc.specialCharTracker.isQuote(newChar)) {
                        taggedDoc.specialCharTracker.addQuote(position);
                } else if (taggedDoc.specialCharTracker.isParenthesis(newChar)) {
                        taggedDoc.specialCharTracker.addParenthesis(position, newChar);
                } else if (taggedDoc.specialCharTracker.isBracket(newChar)) {
                        taggedDoc.specialCharTracker.addBracket(position, newChar);
                } else if (taggedDoc.specialCharTracker.isSquiggly(newChar)) {
                        taggedDoc.specialCharTracker.addSquiggly(position, newChar);
                }
        }

        /**
         * Calculates the indices of all passed "positions". This can either be a
         * since int value (meaning it's just a single caret position in the
         * document and we're finding out it's indices), or it can be multiple int
         * values (meaning there's a selection and we're finding out it's indices)
         */
        protected int[][] getSentencesIndices(int... positions) {
                int[] sentenceLengths = taggedDoc.getSentenceLengths();
                int numSents = sentenceLengths.length;
                int numPositions = positions.length;
                int currentPosition;
                int[][] results = new int[numPositions][3];

                for (int positionNumber = 0; positionNumber < numPositions; positionNumber++) {
                        int i = 0;
                        int length = 0;
                        int selectedSentence = 0; //The sentence the user has their cursor in

                        /**
                         * allSentIndices[0]:
                         *                 Length of sentence '0'
                         * allSentIndices[1]:
                         *                 Length of sentence '0' plus '1'
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
                                
                                while (i < numSents) {
                                        length += sentenceLengths[i];
                                        allSentIndices[i] = length;
                                        i++;
                                }
                        }

                        /**
                         * The start and end indices of the new sentence. This could be as
                         * simple as the previous sentence minus a character or as drastic
                         * as two or more previous sentences deleted between two new
                         * combined sentences
                         */
                        int startIndex = 0;
                        int endIndex = 0;
                        try {
                                if (selectedSentence >= numSents)
                                        return null; //Should never be greater than or equal to the number of sents
                                else if (selectedSentence <= 0)
                                		//Possible for first sentence to also be the last, so need to check for this
	                                	if (!ignoreChanges && currentPosition >= taggedDoc.length + charsRemoved && taggedDoc.numOfSentences == 1) {
	                                        if (!taggedDoc.endSentenceExists && taggedDoc.watchForLastSentenceEOS == -1) {
	                                                if (charsInserted > 0) {
	                                                        taggedDoc.makeNewEndSentence(main.documentPane.getText().substring(priorCaretPosition, newCaretPosition[0]));
	                                                } else {
	                                                        taggedDoc.makeNewEndSentence("");
	                                                }
	                                                taggedDoc.endSentenceExists = true;
	                                                selectedSentence++;
	                                                startIndex = taggedDoc.length - charsInserted + charsRemoved;
	                                                endIndex = taggedDoc.length + charsRemoved;
	                                        } else {
                                                endIndex = allSentIndices[0]+charsInserted;
	                                        }
	                                	} else {
	                                		endIndex = sentenceLengths[0]; //The end of the first sentence, 0.
	                                	}
                                else if (!ignoreChanges && currentPosition >= taggedDoc.length + charsRemoved) {
                                        if (!taggedDoc.endSentenceExists && taggedDoc.watchForLastSentenceEOS == -1) {
                                                if (charsInserted > 0) {
                                                        taggedDoc.makeNewEndSentence(main.documentPane.getText().substring(priorCaretPosition, newCaretPosition[0]));
                                                } else {
                                                        taggedDoc.makeNewEndSentence("");
                                                }
                                                taggedDoc.endSentenceExists = true;
                                                selectedSentence++;
                                                startIndex = taggedDoc.length - charsInserted + charsRemoved;
                                                endIndex = taggedDoc.length + charsRemoved;
                                        } else {
                                                startIndex = allSentIndices[selectedSentence-1];
                                                endIndex = allSentIndices[selectedSentence]+charsInserted;
                                        }
                                } else {
                                        //Start highlighting JUST after the previous sentence stops
                                        startIndex = allSentIndices[selectedSentence-1];
                                        //Stop highlighting when the current sentence stops'
                                        if (selectedSentence == taggedDoc.numOfSentences-1) {
                                                endIndex = allSentIndices[selectedSentence]+charsInserted;
                                        } else {
                                                endIndex = allSentIndices[selectedSentence];
                                        }
                                }
                                
                                highlightIndices = taggedDoc.specialCharTracker.extendHighlights(startIndex, endIndex, allSentIndices, sentenceLengths, selectedSentence);
                                
                                //This new sentence's number and start and end indices.
                                Logger.logln(NAME+"Calculated sentence # = " + selectedSentence + ", " + startIndex + "-" + endIndex);
                                results[positionNumber] = new int[]{selectedSentence, startIndex, endIndex};
                        } catch (Exception e) {
                                Logger.logln(NAME+"Something went dreadfully wrong calculating the sentence indices");
                                Logger.logln(e);
                        }
                }

                return results;
        }

        /**
         * Calculates how much extra space should be added to the given index
         * before it hits the first actual character of the text (meaning we don't
         * want to "trim" the whitespace out of the highlight)
         *
         * @param index
         * The index you want to "pad"
         *
         * @return
         *                 The number of spaces you should add to the index to get the proper start of
         *                 the sentence
         */
        public int getWhiteSpaceBuffer(int index) {
                /**
                 * In try catch to prevent it breaking when the user it typing after the last sentence
                 */
                int whiteSpace = 0;
                try {
                        while (Character.isWhitespace(main.documentPane.getText().charAt(index+whiteSpace))) {
                                whiteSpace++;
                        }
                } catch (Exception e) {}

                return whiteSpace;
        }

        /**
         * Updates the sentence highlight indices with the new indices in
         * sentIndices.
         */
        public void moveHighlights() {
                try {
                        //Clearing all sentences so they can be highlighted somewhere else
                        highlighterEngine.removeAutoRemoveHighlights();
                        highlighterEngine.removeSentenceHighlight();
                        
                        //If user is selecting text, don't make new highlights in this case
                        if (newCaretPosition[0] != newCaretPosition[1] || sentIndices[0] == sentIndices[1] || taggedDoc.getSentenceNumber(sentNum).getUntagged().matches("\\s\\s*")) {
                                Logger.logln(NAME+"NOT highlighting, conditions not met");
                                return;
                        }

                        Logger.logln(NAME+"Moving highlight to " + highlightIndices[0] + "-" + highlightIndices[1]);

                        //Catch for the last sentence
                        int whiteSpace = getWhiteSpaceBuffer(highlightIndices[0]);
                        if (highlightIndices[0]+whiteSpace <= newCaretPosition[0]) {
                                if (doHighlight)
                                        highlighterEngine.addSentenceHighlight(highlightIndices[0]+whiteSpace, highlightIndices[1]);
                                
                                if (autoHighlight && updateSuggestionsThread.isDone())
                                        highlighterEngine.addAutoRemoveHighlights(highlightIndices[0]+whiteSpace, highlightIndices[1], highlightIndices[2], highlightIndices[3]);
                                else
                                        placeAutoHighlightsWhenDone = true;
                        }
                } catch (Exception e1) {
                        Logger.logln(NAME+"Problem moving highlight", LogOut.STDERR);
                        Logger.logln(e1);
                }
        }

        //=======================================================================
        //*                                                        UPDATE METHODS                                                                *        
        //=======================================================================

        /**
         * Doesn't change ANYTHING in the backend (meaning the TaggedDocument),
         * all it does is completely replace the current text in the JTextPane
         * with the backend contents. This means we're just scanning through all
         * the TaggedSentences and pasting all their untagged strings into the
         * main JTextPane.<br><br>
         *
         * The highlighter and all the sentence, selection, and caret bookkeeping
         * variables will also be updated automatically to reflect any changes.
         */
        public void syncTextPaneWithTaggedDoc() {
                //Updating the JTextPane
                ignoreChanges = true;
                // update taggedDoc in editorDriver, sincee for some reasons, it isn't done in the first process
                taggedDoc = ConsolidationStation.toModifyTaggedDocs.get(0);
                
                main.documentPane.setText(taggedDoc.getUntaggedDocument());
                if (newCaretPosition[0] > taggedDoc.length) {
                        newCaretPosition[0] = taggedDoc.length;
                        newCaretPosition[1] = newCaretPosition[0];
                }
                
                /**
                 * A small catch needed in case something goes wrong with the
                 * highlighting and sentence indices (which, while getting more
                 * and more unlikely, can still always happen). In these rare
                 * instances even reseting the editor won't work because newCaretPosition
                 * always ends up being -1, so here we prepare for that and so
                 * we always allow the editor to be reset so we can start fresh again
                 */
                if (newCaretPosition[0] < 0) {
                        newCaretPosition[0] = main.documentPane.getCaret().getMark();
                }
                
                main.documentPane.getCaret().setDot(newCaretPosition[0]);
                main.documentPane.setCaretPosition(newCaretPosition[0]);
                ignoreChanges = false;
                charsInserted = 0;
                charsRemoved = 0;
                updateEditorVariables();
                updateBarAndHighlight();
                charsInserted = 0;
                charsRemoved = 0;
        }

        /**
         * Updates a given sentence number with a new text. This DOES update
         * the backend (meaning the TaggedDocument) by updating the given
         * taggedSentence with the new string.
         *
         * @param sentNumToUpdate
         * The sentence number to update (0, 1, 2, ...)
         * @param updatedText
         * The updated sentence text
         */
        public void updateSentence(int sentNumToUpdate, String updatedText) {
                Logger.logln(NAME+"UPDATING sentence # = " + sentNumToUpdate + " with new string: \"" + updatedText + "\"");
                taggedDoc.removeAndReplace(sentNumToUpdate, updatedText);
        }

        /**
         * Updates all editor variables that may need changing at end of caret
         * event (say, if the user started a new sentence, split one into two,
         * deleted one, etc.). Should be called every caret event.
         */
        private void updateEditorVariables() {
        	
                int[] sentenceInfo = getSentencesIndices(newCaretPosition[0])[0];
                sentNum = sentenceInfo[0];                        //The sentence number
                sentIndices[0] = sentenceInfo[1];        //The start of the sentence
                sentIndices[1] = sentenceInfo[2];        //The end of the sentence
        }

        /**
         * Updates the Anonymity bar and highlights, should be called every caret event.
         */
        private void updateBarAndHighlight() {
                moveHighlights();
                if (updateBarThread.isDone()) {
                        prepareAnonymityBarThread();
                        updateBarThread.execute();
                }
        }

        /**
         * Updates the suggestions in a thread (if done from previous update)
         */
        protected void updateSuggestions() {
                if (updateSuggestionsThread.isDone()) {
                        prepareWordSuggestionsThread();
                        updateSuggestionsThread.execute();
                }
        }

        //=======================================================================
        //*                                                RESET / REPROCESSING METHODS                                        *        
        //=======================================================================
        

        /**
         * Stops any translation threads going on (if any) and also resets
         * the EditorDriver instance to it's default values
         */
        private void prepareForReprocessing() {
                resetToDefaults();
                for (int i = 0; i < taggedDoc.numOfSentences; i++) {
                	taggedDoc.getSentenceNumber(i).resetTranslations();
                }
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

                //Highlighters
                highlighterEngine.clearAll();
                placeAutoHighlightsWhenDone = false;
                highlightIndices = new int[]{0,0,0,0};

                //Editing variables
                charsInserted = 0;
                charsRemoved = 0;

                ignoreChanges = false;
                textChanged = false;
        }
}