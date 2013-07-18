package edu.drexel.psal.anonymouth.gooie;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import edu.drexel.psal.ANONConstants;
import edu.drexel.psal.anonymouth.utils.About;
import edu.drexel.psal.jstylo.generics.*;

/**
 * Handles all the menu bar listeners
 * @author Marc Barrowclift
 *
 */
public class DriverMenu {
	
	private final static String NAME = "( DriverMenu ) - ";

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
        		main.preProcessWindow.driver.saveProblemSetListener.actionPerformed(e);
        	}
        };
        main.fileSaveProblemSetMenuItem.addActionListener(saveProblemSetListener);
        
        loadProblemSetListener = new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		Logger.logln(NAME+"Load problem set menu item clicked");
        		main.preProcessWindow.driver.loadProblemSetListener.actionPerformed(e);
        	}
        };
        main.fileLoadProblemSetMenuItem.addActionListener(loadProblemSetListener);
        
        saveTestDocListener = new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		Logger.logln(NAME+"Save document menu item clicked");
        		DriverEditor.save(main);
        	}
        };
        main.fileSaveTestDocMenuItem.addActionListener(saveTestDocListener);

        saveAsTestDocListener = new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		Logger.logln(NAME+"Save As menu item clicked");
        		DriverEditor.saveAsTestDoc.actionPerformed(e);
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
        		main.versionControl.undo();
        	}
        };
        main.editUndoMenuItem.addActionListener(undoListener);
        
        redoListener = new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		Logger.logln(NAME+"Redo menu item clicekd");
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
}