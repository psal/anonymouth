package edu.drexel.psal.anonymouth.engine;

import java.util.Stack;

import edu.drexel.psal.anonymouth.gooie.DriverEditor;
import edu.drexel.psal.anonymouth.gooie.DriverMenu;
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
	private boolean ready;
	private Stack<TaggedDocument> undo;
	private Stack<TaggedDocument> redo;
	private Stack<Integer> indicesUndo;
	private Stack<Integer> indicesRedo;
	
	/**
	 * Constructor
	 * @param main - Instance of GUIMain
	 */
	public VersionControl(GUIMain main) {
		this.main = main;
		ready = true;
		undo = new Stack<TaggedDocument>();
		redo = new Stack<TaggedDocument>();
		indicesUndo = new Stack<Integer>();
		indicesRedo = new Stack<Integer>();
	}
	
	public boolean isReady() {
		return ready;
	}
	
	/**
	 * Must be called in DriverEditor or wherever you want a "version" to be backed up in the undo stack.
	 * @param taggedDoc - The TaggedDocument instance you want to capture.
	 */
	
	public void addVersion(TaggedDocument taggedDoc, int offset) {
		ready = false;
		if (undo.size() >= SIZECAP) {
			undo.remove(0);
		}

		for (int i = 0; i < taggedDoc.getNumSentences(); i++) {
			taggedDoc.getTaggedSentences().get(i).getTranslations().clear();
		}
		
		undo.push(new TaggedDocument(taggedDoc));
		indicesUndo.push(offset);
		
		main.enableUndo(true);
		main.enableRedo(false);
		
		redo.clear();
		indicesRedo.clear();
		ready = true;
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
		ready = false;
		
//		SwingUtilities.invokeLater(new Runnable() {
//			@Override
//			public void run() {
//				redo.push(new TaggedDocument(DriverEditor.taggedDoc));
//			}
//		});
		redo.push(new TaggedDocument(DriverEditor.taggedDoc));
		indicesRedo.push(main.getDocumentPane().getCaret().getDot());
		
		DriverEditor.ignoreVersion = true;
		DriverEditor.taggedDoc = undo.pop();
		DriverEditor.update(main, true);
		main.getDocumentPane().getCaret().setDot(indicesUndo.pop());
		DriverEditor.ignoreVersion = false;
		
		main.enableRedo(true);
		
		if (undo.size() == 0) {
			main.enableUndo(false);
		}
		
		synchronized(DriverMenu.class) {
		    //set ready flag to true (so isReady returns true)
		    ready = true;
		    DriverMenu.class.notifyAll();
		}
	}
	
	/**
	 * Should be called in the program whenever you want a redo action to occur.
	 * 
	 * Swaps the current taggedDoc in DriverEditor with the version on the top of the redo stack, updates the document text pane with
	 * the new taggedDoc, and pushed the taggedDoc that was just on the redo stack to the undo one. 
	 */
	public void redo() {
		ready = false;
		
//		SwingUtilities.invokeLater(new Runnable() {
//			@Override
//			public void run() {
//				undo.push(new TaggedDocument(DriverEditor.taggedDoc));
//			}
//		});
		undo.push(new TaggedDocument(DriverEditor.taggedDoc));
		indicesUndo.push(main.getDocumentPane().getCaret().getDot());
		
		DriverEditor.ignoreVersion = true;
		DriverEditor.taggedDoc = redo.pop();
		DriverEditor.update(main, true);
		main.getDocumentPane().getCaret().setDot(indicesRedo.pop());
		DriverEditor.ignoreVersion = false;

		main.enableUndo(true);	
		
		if (redo.size() == 0) {
			main.enableRedo(false);
		}
		
		synchronized(DriverMenu.class) {
		    //set ready flag to true (so isReady returns true)
		    ready = true;
		    DriverMenu.class.notifyAll();
		}
	}
	
	/**
	 * Clears all stacks, should be used only for pre-processed documents
	 */
	public void reset() {
		undo.clear();
		redo.clear();
		ready = false;
		indicesUndo.clear();
		indicesRedo.clear();
	}
	
	public boolean isUndoEmpty() {
		return undo.isEmpty();
	}
}