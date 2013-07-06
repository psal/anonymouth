package edu.drexel.psal.anonymouth.gooie;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.Highlighter;

import edu.drexel.psal.anonymouth.utils.IndexFinder;
import edu.drexel.psal.jstylo.generics.Logger;
import edu.drexel.psal.jstylo.generics.Logger.LogOut;

/**
 * Handles most of the listeners and bigger objects for components in the suggestions tab (while all the of component creation and placement
 * is handled within GUIMain createSugTab())
 * @author Marc Barrowclift
 *
 */
public class DriverSuggestionsTab {

	private final static String NAME = "( DriverSuggestionsTab ) - ";
	private static ListSelectionListener elementsToAddListener;
	private static ActionListener clearAddListener;
	private static ActionListener clearRemoveListener;

	protected static void initListeners(final GUIMain main) {
		elementsToAddListener = new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					Logger.logln(NAME+"Elements to add value changed");

					if (main.elementsToAddPane.getSelectedIndex() != -1) {
						try {
							Highlighter highlight = main.getDocumentPane().getHighlighter();
							int highlightedObjectsSize = DriverEditor.selectedAddElements.size();

							for (int i = 0; i < highlightedObjectsSize; i++)
								highlight.removeHighlight(DriverEditor.selectedAddElements.get(i).getHighlightedObject());
							DriverEditor.selectedAddElements.clear();

							if (main.elementsToAddPane.getSelectedIndex() == -1)
								return;

							ArrayList<int[]> index = IndexFinder.findIndices(main.getDocumentPane().getText(), main.elementsToAddPane.getSelectedValue());

							int indexSize = index.size();

							for (int i = 0; i < indexSize; i++)
								DriverEditor.selectedAddElements.add(new HighlightMapper(index.get(i)[0], index.get(i)[1], highlight.addHighlight(index.get(i)[0], index.get(i)[1], DriverEditor.painterAdd)));
						} catch (Exception e1) {
							Logger.logln(NAME+"Error occured while getting selected word to add value and highlighting all instances.", LogOut.STDERR);
							e1.printStackTrace();
						}
					}
				}
			}
		};
		main.elementsToAddPane.addListSelectionListener(elementsToAddListener);

		clearAddListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Highlighter highlight = main.getDocumentPane().getHighlighter();
				int highlightedObjectsSize = DriverEditor.selectedAddElements.size();

				for (int i = 0; i < highlightedObjectsSize; i++)
					highlight.removeHighlight(DriverEditor.selectedAddElements.get(i).getHighlightedObject());
				DriverEditor.selectedAddElements.clear();
				main.elementsToAddPane.clearSelection();
			}
		};
		main.clearAddHighlights.addActionListener(clearAddListener);

		clearRemoveListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {	
				Highlighter highlight = main.getDocumentPane().getHighlighter();
				int highlightedObjectsSize = DriverEditor.selectedRemoveElements.size();

				for (int i = 0; i < highlightedObjectsSize; i++)
					highlight.removeHighlight(DriverEditor.selectedRemoveElements.get(i).getHighlightedObject());

				DriverEditor.selectedRemoveElements.clear();
				main.elementsToRemoveTable.clearSelection();
			}
		};
		main.clearRemoveHighlights.addActionListener(clearRemoveListener);
	}
}

/**
 * To be used for Elements to Remove so that we can display both the word to remove as well as the number of occurrences
 * in a nice way.
 * @author Marc Barrowclift
 *
 */
class ElementsTable extends JTable implements ListSelectionListener {

	private static final String NAME = "( ElementsTable ) - ";
	private static final long serialVersionUID = 1L;
	private GUIMain main;

	public ElementsTable(DefaultTableModel model, GUIMain main) {
		super(model);
		this.main = main;
	}

	/**
	 * Goes through each row of the table and removes them
	 */
	public void removeAllElements() {
		int size = this.getRowCount();
		DefaultTableModel tm = (DefaultTableModel)this.getModel();

		try {
			for (int i = 0; i < size; i++) {
				tm.removeRow(0);
			}
		} catch (Exception e) {
			Logger.logln(NAME+"Error occured while trying to remove elemenets to remove", LogOut.STDERR);
			e.printStackTrace();
		}
	}

	/**
	 * Whenever the user clicks another row we should highlight the words in the test document (like with elements to add)
	 * @param e
	 */
	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting()) { // added as sometimes, multiple events are fired while selection changes
			Logger.logln(NAME+"Elements to remove value changed");

			if (this.getSelectedRow() != -1) {
				try {
					Highlighter highlight = main.getDocumentPane().getHighlighter();
					int highlightedObjectsSize = DriverEditor.selectedRemoveElements.size();

					for (int i = 0; i < highlightedObjectsSize; i++)
						highlight.removeHighlight(DriverEditor.selectedRemoveElements.get(i).getHighlightedObject());
					DriverEditor.selectedRemoveElements.clear();

					//If the "word to remove" is punctuation and in the form of "Remove ...'s" for example, we want
					//to just extract the "..." for highlighting
					String wordToRemove = (String)this.getModel().getValueAt(this.getSelectedRow(), 0);
					String[] test = wordToRemove.split(" ");
					if (test.length >= 2) {
						wordToRemove = test[1].substring(0, test[1].length()-2);
					}
					
					ArrayList<int[]> index = IndexFinder.findIndices(main.getDocumentPane().getText(), wordToRemove);

					int indexSize = index.size();

					for (int i = 0; i < indexSize; i++)
						DriverEditor.selectedRemoveElements.add(new HighlightMapper(index.get(i)[0], index.get(i)[1], highlight.addHighlight(index.get(i)[0], index.get(i)[1], DriverEditor.painterRemove)));
				} catch (Exception e1) {
					Logger.logln(NAME+"Error occured while getting selected word to remove value and highlighting all instances.", LogOut.STDERR);
					e1.printStackTrace();
				}
			}
		}
		super.valueChanged(e);
	}
}