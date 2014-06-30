package edu.drexel.psal.anonymouth.engine;

import java.awt.event.ActionEvent;
import java.util.Stack;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.undo.UndoManager;

import edu.drexel.psal.anonymouth.gooie.EditorDriver;
import edu.drexel.psal.anonymouth.gooie.GUIMain;
import edu.drexel.psal.anonymouth.gooie.MenuDriver;
import edu.drexel.psal.anonymouth.utils.ConsolidationStation;
import edu.drexel.psal.anonymouth.utils.TaggedDocument;
import edu.drexel.psal.jstylo.generics.Logger;

/**
 * Adds undo/redo functionality to the documents pane.
 * 
 * @author Marc Barrowclift
 */
public class VersionControl {
	
	private final String NAME = "( " + this.getClass().getSimpleName() + " ) - ";
	/**
	 * The absolute max allowed stack size for either undo or redo.<br><br>
	 * 
	 * TODO: This could use some testing, we're not sure how large a given
	 * TaggedDocument is. If it turns out it doesn't use up that much space,
	 * then we can most certainly increase this cap.
	 */
	private final int SIZECAP = 30;
	/**
	 * The number of characters to accept before "backing up" a new version to
	 * undo/redo
	 */
	private final int CHARS_TIL_BACKUP = 15;
	/**
	 * The current number of characters inserted or removed since a version was
	 * added to the undo stack
	 */
	protected int curCharBackupBuffer;
	/**
	 * The past version of the taggedDocument
	 */
	public TaggedDocument pastTaggedDoc;

	private GUIMain main;
	private EditorDriver editor;

	private boolean ready;
	private Stack<TaggedDocument> undo;
	private Stack<TaggedDocument> redo;
	private Stack<Integer> indicesUndo;
	private Stack<Integer> indicesRedo;
	private UndoManager undoManager;//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	/**
	 * Constructor
	 * 
	 * @param main
	 *        GUIMain instance
	 */
	public VersionControl(GUIMain main) {
		this.main = main;

		ready = true;
		curCharBackupBuffer = CHARS_TIL_BACKUP-1;
		
		undo = new Stack<TaggedDocument>();
		redo = new Stack<TaggedDocument>();
		indicesUndo = new Stack<Integer>();
		indicesRedo = new Stack<Integer>();
//		undoManager = new UndoManager();
//		main.documentPane.getDocument().addUndoableEditListener(undoManager);
		
//		Action undoAction = new UndoAction(undoManager);
		
	}
	
	/**
	 * Should be called after processing to set up the initial backups and
	 * prepare undo/redo. Can't be handled by the constructor since we don't
	 * yet have tagged documents ready when that happens, we have to wait.
	 */
	public void init() {
		/*
		undo.clear();
		redo.clear();
		indicesUndo.clear();
		indicesRedo.clear();
		main.enableRedo(false);
		main.enableUndo(false);
		*/
		
		editor = main.editorDriver;
		pastTaggedDoc = new TaggedDocument(editor.taggedDoc);
	}

	/**
	 * Returns whether or not the undo or redo threads are done
	 * and ready for another action
	 * 
	 * @return
	 * 		True or false, depending on whether or not it's ready
	 */
	public boolean isReady() {
		return ready;
	}
	
	/**
	 * Automatically handles whether or not it's appropriate to add a copy of
	 * the current taggedDoc to the undo stack, simply call this every
	 * caretListener update from editorDriver
	 *
	 * @param taggedDoc
	 *        The TaggedDocument instance you want to capture.
	 * @param priorCaretPosition
	 *        The position the caret was at for this particular backup
	 * @param forceBackup 
	 *        Whether or not to bypass and reset the curCharBackupBuffer and immediately backup
	 */
	public void updateUndoRedo(TaggedDocument taggedDoc, int priorCaretPosition, boolean forceBackup) {
		if (editor.ignoreChanges) {
			pastTaggedDoc = new TaggedDocument(taggedDoc);
			return;
		}

		curCharBackupBuffer++;

		if (forceBackup) {
			pastTaggedDoc = new TaggedDocument(taggedDoc);
			addVersion(pastTaggedDoc, priorCaretPosition);
			curCharBackupBuffer = 0;
		} else if (curCharBackupBuffer >= CHARS_TIL_BACKUP) {
			pastTaggedDoc = new TaggedDocument(taggedDoc);
			addVersion(pastTaggedDoc, priorCaretPosition);
			curCharBackupBuffer = 0;
		}
	}

	/**
	 * Adds a version to the undo stack, should only be called by
	 * updateUndoRedo() when it deems appropriate to do so.
	 * 
	 * @param taggedDoc
	 *        The TaggedDocument instance you want to capture.
	 * @param priorCaretPosition
	 *        The position the caret was at for this particular backup
	 */
	private void addVersion(TaggedDocument taggedDoc, int priorCaretPosition) {
		Logger.logln(NAME+"A new backup has been added to the Undo stack");
		ready = false;
		if (undo.size() >= SIZECAP) {
			undo.remove(0);
		}

		if (redo.size() == 0) {
			main.enableRedo(false);
		}
		if (undo.size() == 0) {
			main.enableUndo(false);
		}
		
		taggedDoc.clearAllTranslations();
		undo.push(new TaggedDocument(taggedDoc));
		indicesUndo.push(priorCaretPosition);
		
		main.enableUndo(true);
		main.enableRedo(false);
		
		redo.clear();
		indicesRedo.clear();
		ready = true;
	}
	
	/**
	 * Should be called whenever you want a undo action to occur (so in Undo listeners)
	 * 
	 * Swaps the current taggedDoc in DriverEditor with the version on the top
	 * of the undo stack, updates the document text pane with the new
	 * taggedDoc, and pushed the taggedDoc that was just on the undo stack to
	 * the redo one.
	 */
	
	
	public void undo() {
		Logger.logln(NAME+"UNDO ----------------------------------");
		ready = false;
		
		redo.push(new TaggedDocument(editor.taggedDoc));
		indicesRedo.push(editor.newCaretPosition[0]);
		
		ConsolidationStation.toModifyTaggedDocs.set(0,undo.pop());
		editor.newCaretPosition[0] = indicesUndo.pop();
		editor.newCaretPosition[1]= editor.newCaretPosition[0];
		editor.syncTextPaneWithTaggedDoc();
		
		main.enableRedo(true);
		
		if (redo.size() == 0) {
			System.out.println("!!!!Redo stack size is 0 - Redo is disabled");
			main.enableRedo(false);
		}
		if (undo.size() == 0) {
			System.out.println("!!!!Undo stack size is 0 - Undo is disabled");
			main.enableUndo(false);
			curCharBackupBuffer = CHARS_TIL_BACKUP - 1;
		}
		
//		synchronized(MenuDriver.class) {
//		    //set ready flag to true (so isReady returns true)
//		    ready = true;
//		    MenuDriver.class.notifyAll();
//		}
		
	}
	
	
	/**
	 * Should be called whenever you want a redo action to occur (so in Redo listeners)
	 * 
	 * Swaps the current taggedDoc in DriverEditor with the version on the top
	 * of the redo stack, updates the document text pane with the new
	 * taggedDoc, and pushed the taggedDoc that was just on the redo stack to
	 * the undo one.
	 */
	public void redo() {
		Logger.logln(NAME+"REDO ----------------------------------");
		ready = false;

		undo.push(new TaggedDocument(editor.taggedDoc));
		indicesUndo.push(editor.newCaretPosition[0]);
		
		ConsolidationStation.toModifyTaggedDocs.set(0,redo.pop());
		editor.newCaretPosition[0] = indicesRedo.pop();
		editor.newCaretPosition[1] = editor.newCaretPosition[0];
		editor.syncTextPaneWithTaggedDoc();

		main.enableUndo(true);	
		
		if (redo.size() == 0) {
			main.enableRedo(false);
		}
		if (undo.size() == 0) {
			main.enableUndo(false);
			curCharBackupBuffer = CHARS_TIL_BACKUP - 1;
		}
		
//		synchronized(MenuDriver.class) {
//		    //set ready flag to true (so isReady returns true)
//		    ready = true;
//		    MenuDriver.class.notifyAll();
//		}
	}
}