package edu.drexel.psal.anonymouth.engine;

import java.util.Stack;

import edu.drexel.psal.anonymouth.gooie.DriverEditor;
import edu.drexel.psal.anonymouth.gooie.GUIMain;
import edu.drexel.psal.anonymouth.utils.TaggedDocument;

/**
 * Adds undo/redo functionality to the documents pane.
 * @author Marc Barrowclift
 *
 */
public class VersionControl {
	
	private final int SIZECAP = 30;
	private GUIMain main;
	private Stack<TaggedDocument> undo;
	private Stack<TaggedDocument> redo;
	private Stack<Integer> indicesUndo;
	private Stack<Integer> indicesRedo;
	private int undoSize = 0;
	private int redoSize = 0;
	
	/**
	 * Constructor
	 * @param main - Instance of GUIMain
	 */
	public VersionControl(GUIMain main) {
		this.main = main;
		undo = new Stack<TaggedDocument>();
		redo = new Stack<TaggedDocument>();
		indicesUndo = new Stack<Integer>();
		indicesRedo = new Stack<Integer>();
	}
	
	/**
	 * Must be called in DriverEditor or wherever you want a "version" to be backed up in the undo stack.
	 * @param taggedDoc - The TaggedDocument instance you want to capture.
	 */
	
	public void addVersion(TaggedDocument taggedDoc, int offset) {
		if (undo.size() >= SIZECAP) {
			undo.remove(0);
		}

		for (int i = 0; i < taggedDoc.getNumSentences(); i++) {
			taggedDoc.getTaggedSentences().get(i).getTranslations().clear();
		}
		
		undo.push(new TaggedDocument(taggedDoc));
		indicesUndo.push(offset);
		undoSize++;
		
		main.enableUndo(true);
		main.enableRedo(false);
		
		redo.clear();
		indicesRedo.clear();
		redoSize = 0;
	}
	
	public void addVersion(TaggedDocument taggedDoc) {
		addVersion(taggedDoc, main.getDocumentPane().getCaret().getDot());
	}
	
	/**
	 * Should be called in the program whenever you want a undo action to occur.
	 * 
	 * Swaps the current taggedDoc in DriverEditor with the version on the top of the undo stack, updates the document text pane with
	 * the new taggedDoc, and pushed the taggedDoc that was just on the undo stack to the redo one. 
	 */
	public void undo() {
		redo.push(new TaggedDocument(DriverEditor.taggedDoc));
		indicesRedo.push(main.getDocumentPane().getCaret().getDot());
		
		DriverEditor.ignoreVersion = true;
		DriverEditor.taggedDoc = undo.pop();
		DriverEditor.update(main, true);
		main.getDocumentPane().getCaret().setDot(indicesUndo.pop());
		DriverEditor.ignoreVersion = false;
		
		main.enableRedo(true);
		undoSize--;
		redoSize++;
		
		if (undoSize == 0) {
			main.enableUndo(false);
		}
		
		main.anonymityDrawingPanel.updateAnonymityBar();
		DriverEditor.setSuggestions();
	}
	
	/**
	 * Should be called in the program whenever you want a redo action to occur.
	 * 
	 * Swaps the current taggedDoc in DriverEditor with the version on the top of the redo stack, updates the document text pane with
	 * the new taggedDoc, and pushed the taggedDoc that was just on the redo stack to the undo one. 
	 */
	public void redo() {
		undo.push(new TaggedDocument(DriverEditor.taggedDoc));
		indicesUndo.push(main.getDocumentPane().getCaret().getDot());
		
		DriverEditor.ignoreVersion = true;
		DriverEditor.taggedDoc = redo.pop();
		DriverEditor.update(main, true);
		main.getDocumentPane().getCaret().setDot(indicesRedo.pop());
		DriverEditor.ignoreVersion = false;

		main.enableUndo(true);	
		undoSize++;
		redoSize--;
		
		if (redoSize == 0) {
			main.enableRedo(false);
		}
		
		main.anonymityDrawingPanel.updateAnonymityBar();
		DriverEditor.setSuggestions();
	}
	
	/**
	 * Clears all stacks, should be used only for pre-processed documents
	 */
	public void reset() {
		undo.clear();
		redo.clear();
		indicesUndo.clear();
		indicesRedo.clear();
		undoSize = 0;
		redoSize = 0;
	}
	
	public boolean isUndoEmpty() {
		return undo.isEmpty();
	}
}