package edu.drexel.psal.anonymouth.engine;

import java.util.Stack;

import edu.drexel.psal.anonymouth.gooie.MenuDriver;
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
		addVersion(taggedDoc, main.documentPane.getCaret().getDot());
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
		redo.push(new TaggedDocument(main.editorDriver.taggedDoc));
		indicesRedo.push(main.documentPane.getCaret().getDot());
		
		main.editorDriver.ignoreBackup = true;
		main.editorDriver.taggedDoc = undo.pop();
		main.editorDriver.newCaretPosition[0] = indicesUndo.pop();
		main.editorDriver.newCaretPosition[1]= main.editorDriver.newCaretPosition[0];
		main.editorDriver.refreshEditor();
		main.editorDriver.updateSentence(main.editorDriver.sentNum, main.documentPane.getText().substring(main.editorDriver.sentIndices[0], main.editorDriver.sentIndices[1]));
		main.editorDriver.ignoreBackup = false;
		
		main.enableRedo(true);
		
		if (undo.size() == 0) {
			main.enableUndo(false);
		}
		
		synchronized(MenuDriver.class) {
		    //set ready flag to true (so isReady returns true)
		    ready = true;
		    MenuDriver.class.notifyAll();
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
		undo.push(new TaggedDocument(main.editorDriver.taggedDoc));
		indicesUndo.push(main.documentPane.getCaret().getDot());
		
		main.editorDriver.ignoreBackup = true;
		main.editorDriver.taggedDoc = redo.pop();
		main.editorDriver.newCaretPosition[0] = indicesRedo.pop();
		main.editorDriver.newCaretPosition[1]= main.editorDriver.newCaretPosition[0];
		main.editorDriver.refreshEditor();
		main.editorDriver.updateSentence(main.editorDriver.sentNum, main.documentPane.getText().substring(main.editorDriver.sentIndices[0], main.editorDriver.sentIndices[1]));
		main.editorDriver.ignoreBackup = false;

		main.enableUndo(true);	
		
		if (redo.size() == 0) {
			main.enableRedo(false);
		}
		
		synchronized(MenuDriver.class) {
		    //set ready flag to true (so isReady returns true)
		    ready = true;
		    MenuDriver.class.notifyAll();
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