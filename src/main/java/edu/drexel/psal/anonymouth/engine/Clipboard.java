package edu.drexel.psal.anonymouth.engine;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.TransferHandler;

import edu.drexel.psal.ANONConstants;
import edu.drexel.psal.anonymouth.gooie.GUIMain;

/**
 * Adds standard clipboard actions to the Edit pull-down menu and supplies the corresponding methods for each.
 * @author Marc Barrowclift
 *
 */
public class Clipboard {
	private JMenuItem cut;
	private JMenuItem copy;
	private JMenuItem paste;
	protected GUIMain main;
	
	/**
	 * CONSTRUCTOR
	 * @param main - An instance of GUIMain
	 * @param editMenu - The edit pull-down menu (or the pull-down menu you want the clipboard actions to reside)
	 */
	public Clipboard(GUIMain main, JMenuItem editMenu) {
		this.main = main;
		initMenu(editMenu);
	}
	
	/**
	 * Readies the menu items and adds them to the menu.
	 * @param editMenu - The edit pull-down menu passed from the constructor.
	 */
	private void initMenu(JMenuItem editMenu) {
		editMenu.add(new JSeparator());
		
		cut = new JMenuItem("Cut");
		cut.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cut();
			}
		});
		editMenu.add(cut);
		
		copy = new JMenuItem("Copy");
		copy.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				copy();
			}
		});
		editMenu.add(copy);
		
		paste = new JMenuItem("Paste");
		paste.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				paste();
			}
		});
		editMenu.add(paste);
		
		//Readying the keyboard shortcuts for each, depending on what OS they are running
		int modKey = 0;
		if (ANONConstants.IS_MAC)
			modKey = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
		else
			modKey = InputEvent.CTRL_DOWN_MASK;
		
		cut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, modKey));
		copy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, modKey));
		paste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, modKey));
	}
	
	/**
	 * Enables or disables all the menu items
	 * @param b
	 */
	public void setEnabled(boolean b) {
		cut.setEnabled(b);
		copy.setEnabled(b);
		paste.setEnabled(b);
	}
	
	/**
	 * Enables or disables each menu item depending on the passed values
	 * @param cut
	 * @param copy
	 * @param paste
	 */
	public void setEnabled(boolean cut, boolean copy, boolean paste) {
		this.cut.setEnabled(cut);
		this.copy.setEnabled(copy);
		this.paste.setEnabled(paste);
	}
	
	public void cut() {
		copy();
		main.documentPane.replaceSelection("");
	}
	
	public void copy() {
		java.awt.datatransfer.Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		TransferHandler transferHandler = main.documentPane.getTransferHandler();
		transferHandler.exportToClipboard(main.documentPane, clipboard, TransferHandler.COPY);
	}
	
	public void paste() {
		java.awt.datatransfer.Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		TransferHandler transferHandler = main.documentPane.getTransferHandler();
		transferHandler.importData(main.documentPane, clipboard.getContents(null));
	}
}
