package edu.drexel.psal.anonymouth.gooie;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import edu.drexel.psal.ANONConstants;
import edu.drexel.psal.anonymouth.helpers.ExtFilter;
import edu.drexel.psal.anonymouth.helpers.FileHelper;
import edu.drexel.psal.anonymouth.utils.About;
import edu.drexel.psal.jstylo.generics.*;
import edu.drexel.psal.jstylo.generics.Logger.LogOut;

/**
 * Handles all the menu bar listeners
 * @author Marc Barrowclift
 *
 */
public class DriverMenu {
	
	//Constants
	private final static String NAME = "( DriverMenu ) - ";

	//Listeners
	protected static ActionListener preferencesListener;
	protected static ActionListener saveProblemSetListener;
	protected static ActionListener loadProblemSetListener;
	protected static ActionListener saveTestDocListener;
	protected static ActionListener saveAsTestDocListener;
	protected static ActionListener aboutListener;
	protected static ActionListener viewClustersListener;
	protected static ActionListener suggestionsListener;
	protected static ActionListener helpClustersListener;
	protected static ActionListener undoListener;
	protected static ActionListener redoListener;
	protected static ActionListener fullScreenListener;
//	protected static ActionListener printMenuItemListener;
	
	//Variables
	private static String savedPath = "";
	
	/**
	 * Initializes all menu bar listeners. Should be called sometime during startup before main window
	 * is displayed. Any additional menu bar items should have their listeners be placed here.
	 * 
	 * @param main
	 * 		GUIMain instance
	 */
	protected static void initListeners(final GUIMain main) {	
		preferencesListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GUIMain.preferencesWindow.showWindow();
			}
        };
        if (!ANONConstants.IS_MAC)
        	main.settingsGeneralMenuItem.addActionListener(preferencesListener);
        
        saveProblemSetListener = new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		Logger.logln(NAME+"Save problem set menu item clicked");
        		main.preProcessWindow.driver.doneSaveListener.actionPerformed(e);
        	}
        };
        main.fileSaveProblemSetMenuItem.addActionListener(saveProblemSetListener);
        
        loadProblemSetListener = new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		Logger.logln(NAME+"Load problem set menu item clicked");
        		main.startingWindows.loadDocSetListener.actionPerformed(e);
        	}
        };
        main.fileLoadProblemSetMenuItem.addActionListener(loadProblemSetListener);
        
        saveTestDocListener = new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		Logger.logln(NAME+"Save document menu item clicked");
        		DriverMenu.save(main);
        	}
        };
        main.fileSaveTestDocMenuItem.addActionListener(saveTestDocListener);

        saveAsTestDocListener = new ActionListener() {
        	@Override
			public void actionPerformed(ActionEvent e) {
				Logger.logln(NAME+"Save As menu item clicked.");
				
				File dir;
				if (savedPath != "") {
					dir = new File(savedPath);
					FileHelper.save.setCurrentDirectory(dir);
				} else {
					try {
						dir = new File(new File(main.preProcessWindow.ps.getTestDocs().get(ANONConstants.DUMMY_NAME).get(0).getFilePath()).getCanonicalPath());
						FileHelper.save.setCurrentDirectory(dir);
					} catch (IOException e1) {
						Logger.logln(NAME+"Something went wrong while trying to set the opening directory for the JFileChooser", LogOut.STDERR);
					}
				}
				
				FileHelper.save.setSelectedFile(new File("anonymizedDoc.txt"));
				FileHelper.save.addChoosableFileFilter(new ExtFilter("txt files (*.txt)", "txt"));
				int answer = FileHelper.save.showSaveDialog(main);

				if (answer == JFileChooser.APPROVE_OPTION) {
					File f = FileHelper.save.getSelectedFile();
					String path = f.getAbsolutePath();
					
					if (!path.toLowerCase().endsWith(".txt"))
						path += ".txt";
					
					FileHelper.writeToFile(path, main.getDocumentPane().getText());
					savedPath = path;
				} else
					Logger.logln(NAME+"Save As contents of current tab canceled");
			}
        };
        main.fileSaveAsTestDocMenuItem.addActionListener(saveAsTestDocListener);
        
        aboutListener = new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		Logger.logln(NAME+"About menu item clicked");
        		JOptionPane.showMessageDialog(null, 
						About.aboutAnonymouth,
						"About Anonymouth",
						JOptionPane.INFORMATION_MESSAGE,
						ThePresident.aboutLogo);
        	}
        };
        main.helpAboutMenuItem.addActionListener(aboutListener);
        
        viewClustersListener = new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		Logger.logln(NAME+"View Clusters menu item clicked");
        		main.clustersWindow.openWindow();
        	}
        };
        main.viewClustersMenuItem.addActionListener(viewClustersListener);
        
        suggestionsListener = new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		Logger.logln(NAME+"Suggestions menu item clicked");
        		main.suggestionsWindow.openWindow();
        	}
        };
        main.helpSuggestionsMenuItem.addActionListener(suggestionsListener);
        
        helpClustersListener = new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		Logger.logln(NAME+"Clusters help menu item clicked");
        		main.clustersTutorial.openWindow();
        	}
        };
        main.helpClustersMenuItem.addActionListener(helpClustersListener);
        
        undoListener = new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		Logger.logln(NAME+"Undo menu item clicked");
        		synchronized(main.versionControl){
        		    while (!main.versionControl.isReady()){
        		        try {
							this.wait();
						} catch (InterruptedException e1) {
							Logger.logln(NAME+"Issue occurred while waiting for version control thread to sleep");
						}
        		    }
        		}
        		main.versionControl.undo();
        	}
        };
        main.editUndoMenuItem.addActionListener(undoListener);
        
        redoListener = new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		Logger.logln(NAME+"Redo menu item clicekd");
        		synchronized(main.versionControl){
        		    while (!main.versionControl.isReady()){
        		        try {
							this.wait();
						} catch (InterruptedException e1) {
							Logger.logln(NAME+"Issue occurred while waiting for version control thread to sleep");
						}
        		    }
        		}
        		main.versionControl.redo();
        	}
        };
        main.editRedoMenuItem.addActionListener(redoListener);
        
        if (ANONConstants.IS_MAC) {
        	fullScreenListener = new ActionListener() {
            	@Override
            	public void actionPerformed(ActionEvent e) { 
            		Logger.logln(NAME+"Fullscreen menu item clicked");
            		ThePresident.app.requestToggleFullScreen(main);
            	}
            };
            GUIMain.viewEnterFullScreenMenuItem.addActionListener(fullScreenListener);
        }
        
        //Print functionality, not yet implemented or complete, we'll get around to it at some point
        /*
        printMenuItemListener = new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		PrinterJob job = PrinterJob.getPrinterJob();
        		PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
        		PageFormat pf = job.pageDialog(aset);
        		job.setPrintable(new PrintDialogExample(), pf);
        		boolean ok = job.printDialog(aset);
        		
        	}
        };
        main.filePrintMenuItem.addActionListener(printMenuItemListener);
        */
	}
	
	/**
	 * Saves the document to anonymize. Treats this as a regular "Save" if the document has
	 * already been "Save As..."ed once or as a "Save As..." if the user hasn't yet (so we
	 * don't just write over their original, and we don't have another file to do a regular
	 * save to yet).
	 * 
	 * This is in a separate method to allow the save operation to be called outside for such
	 * features as auto-save.
	 * 
	 * @param main
	 * 		GUIMain instance
	 */
	public static void save(GUIMain main) {
		if (savedPath == "") {
			DriverMenu.saveAsTestDocListener.actionPerformed(new ActionEvent(main.fileSaveAsTestDocMenuItem, ActionEvent.ACTION_PERFORMED, "Save As..."));
		} else {
			FileHelper.writeToFile(savedPath, main.getDocumentPane().getText());
		}
	}
}