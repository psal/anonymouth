package edu.drexel.psal.anonymouth.gooie;

import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;

import edu.drexel.psal.ANONConstants;
import edu.drexel.psal.anonymouth.helpers.FileHelper;
import edu.drexel.psal.anonymouth.utils.About;
import edu.drexel.psal.jstylo.generics.*;
import edu.drexel.psal.jstylo.generics.Logger.LogOut;

/**
 * Handles all the menu bar listeners
 * @author Marc Barrowclift
 *
 */
public class MenuDriver {
	
	//Constants
	private final String NAME = "( DriverMenu ) - ";

	//Listeners
	protected ActionListener preferencesListener;
	protected ActionListener saveProblemSetListener;
	protected ActionListener saveTestDocListener;
	protected ActionListener saveAsTestDocListener;
	protected ActionListener aboutListener;
	protected ActionListener viewClustersListener;
	protected ActionListener faqListener;
	protected ActionListener helpClustersListener;
	protected ActionListener undoListener;
	protected ActionListener redoListener;
	protected ActionListener fullScreenListener;
	//protected ActionListener printMenuItemListener;
	
	//Variables
	private String savedPath = "";
	private GUIMain main;
	
	/**
	 * Constructor, takes care of everything, all you need to do is initialize it and you're listeners
	 * are good to go.
	 * 
	 * @param main
	 * 		GUIMain instance
	 */
	public MenuDriver(GUIMain main) {
		this.main = main;
		
		FileHelper.goodSave = new FileDialog(main);
		
		initListeners();
	}
	
	/**
	 * Initializes all menu bar listeners. Should be called sometime during startup before main window
	 * is displayed. Any additional menu bar items should have their listeners be placed here.
	 * 
	 * @param main
	 * 		GUIMain instance
	 */
	protected void initListeners() {	
		preferencesListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				main.preferencesWindow.showWindow();
			}
        };
        if (!ANONConstants.IS_MAC)
        	main.settingsGeneralMenuItem.addActionListener(preferencesListener);
        
        saveProblemSetListener = new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		Logger.logln(NAME+"Save problem set menu item clicked");
        		main.preProcessDriver.doneSaveListener.actionPerformed(e);
        	}
        };
        main.fileSaveProblemSetMenuItem.addActionListener(saveProblemSetListener);
        
        saveTestDocListener = new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		Logger.logln(NAME+"Save document menu item clicked");
        		save(main);
        	}
        };
        main.fileSaveTestDocMenuItem.addActionListener(saveTestDocListener);

        saveAsTestDocListener = new ActionListener() {
        	@Override
			public void actionPerformed(ActionEvent e) {
				Logger.logln(NAME+"Save As menu item clicked.");
				
				/**
				 * In case something starts to go wrong with the FileDialogs (they are older and
				 * may be deprecated as some point). If this be the case, just swap in this code instead
				 */
				/*
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
					*/
				
				if (savedPath != "") {
					FileHelper.goodSave.setDirectory(savedPath);
				} else {
					try {
						FileHelper.goodSave.setDirectory(new File(main.preProcessWindow.ps.getTestDocs().get(ANONConstants.DUMMY_NAME).get(0).getFilePath()).getCanonicalPath());
					} catch (IOException e1) {
						Logger.logln(NAME+"Something went wrong while trying to set the opening directory for the JFileChooser", LogOut.STDERR);
					}
				}
				
				FileHelper.goodSave.setFile("anonymizedDoc.txt");
				FileHelper.goodSave.setFilenameFilter(ANONConstants.TXT);
				FileHelper.goodSave.setLocationRelativeTo(null);
				FileHelper.goodSave.setVisible(true);
				
				File[] files = FileHelper.goodSave.getFiles();
				if (files.length != 0) {		
					String path = files[0].getAbsolutePath();
					
					if (!path.toLowerCase().endsWith(".txt"))
						path += ".txt";
					
					FileHelper.writeToFile(path, main.documentPane.getText());
					main.saved = true;
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
        
        faqListener = new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		Logger.logln(NAME+"Suggestions menu item clicked");
        		main.faqWindow.openWindow();
        	}
        };
        main.helpSuggestionsMenuItem.addActionListener(faqListener);
        
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
            main.viewEnterFullScreenMenuItem.addActionListener(fullScreenListener);
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
	public void save(GUIMain main) {
		if (savedPath == "") {
			saveAsTestDocListener.actionPerformed(new ActionEvent(main.fileSaveAsTestDocMenuItem, ActionEvent.ACTION_PERFORMED, "Save As..."));
		} else {
			FileHelper.writeToFile(savedPath, main.documentPane.getText());
			main.saved = true;
		}
	}
}