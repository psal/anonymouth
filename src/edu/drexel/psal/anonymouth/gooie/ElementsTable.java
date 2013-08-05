package edu.drexel.psal.anonymouth.gooie;

import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import edu.drexel.psal.jstylo.generics.Logger;
import edu.drexel.psal.jstylo.generics.Logger.LogOut;

/**
 * To be used for Elements to Remove so that we can display both the word to remove as well as the number of occurrences
 * in a nice way.
 * @author Marc Barrowclift
 *
 */
public class ElementsTable extends JTable implements ListSelectionListener {

	private static final String NAME = "( ElementsTable ) - ";
	private static final long serialVersionUID = 1L;

	public ElementsTable(DefaultTableModel model) {
		super(model);
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
				DriverEditor.highlightEngine.removeAllRemoveHighlights();
				DriverEditor.highlightEngine.addAllRemoveHighlights((String)this.getModel().getValueAt(this.getSelectedRow(), 0));
			}
		}
		super.valueChanged(e);
	}
}