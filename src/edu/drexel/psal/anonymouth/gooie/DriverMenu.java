package edu.drexel.psal.anonymouth.gooie;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;

import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.drexel.psal.JSANConstants;
import edu.drexel.psal.anonymouth.utils.About;
import edu.drexel.psal.anonymouth.utils.ConsolidationStation;
import edu.drexel.psal.jstylo.eventDrivers.*;
import edu.drexel.psal.jstylo.generics.*;
import edu.drexel.psal.jstylo.generics.Logger.LogOut;
import edu.drexel.psal.jstylo.GUI.DocsTabDriver.ExtFilter;
import edu.drexel.psal.jstylo.canonicizers.*;

import com.jgaap.canonicizers.*;
import com.jgaap.generics.*;

/**
 * Handles all the menu bar listeners
 * @author Marc Barrowclift
 *
 */
public class DriverMenu {
	
	private final static String NAME = "( DriverMenu ) - ";

	protected static ActionListener generalListener;
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
	
	protected static void initListeners(final GUIMain main)
	{
		generalListener = new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				main.GSP.openWindow();
			}
        };
        String OS = System.getProperty("os.name").toLowerCase();
        if (!ThePresident.IS_MAC)
        	main.settingsGeneralMenuItem.addActionListener(generalListener);
        
        saveProblemSetListener = new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		DriverPreProcessTabDocuments.saveProblemSetAL.actionPerformed(e);
        	}
        };
        main.fileSaveProblemSetMenuItem.addActionListener(saveProblemSetListener);
        
        loadProblemSetListener = new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		DriverPreProcessTabDocuments.loadProblemSetAL.actionPerformed(e);
        	}
        };
        main.fileLoadProblemSetMenuItem.addActionListener(loadProblemSetListener);
        
        saveTestDocListener = new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		DriverEditor.save(main);
        	}
        };
        main.fileSaveTestDocMenuItem.addActionListener(saveTestDocListener);

        saveAsTestDocListener = new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		DriverEditor.saveAsTestDoc.actionPerformed(e);
        	}
        };
        main.fileSaveAsTestDocMenuItem.addActionListener(saveAsTestDocListener);
        
        aboutListener = new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		JOptionPane.showMessageDialog(null, 
						About.aboutAnonymouth,
						"About Anonymouth",
						JOptionPane.INFORMATION_MESSAGE,
						ThePresident.LOGO);
        	}
        };
        main.helpAboutMenuItem.addActionListener(aboutListener);
        
        viewClustersListener = new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		main.clustersWindow.openWindow();
        	}
        };
        main.viewClustersMenuItem.addActionListener(viewClustersListener);
        
        suggestionsListener = new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		main.suggestionsWindow.openWindow();
        	}
        };
        main.helpSuggestionsMenuItem.addActionListener(suggestionsListener);
        
        helpClustersListener = new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		main.clustersTutorial.openWindow();
        	}
        };
        main.helpClustersMenuItem.addActionListener(helpClustersListener);
        
        undoListener = new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		main.versionControl.undo();
        	}
        };
        main.editUndoMenuItem.addActionListener(undoListener);
        
        redoListener = new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		main.versionControl.redo();
        	}
        };
        main.editRedoMenuItem.addActionListener(redoListener);
        
        if (ThePresident.IS_MAC) {
        	fullScreenListener = new ActionListener() {
            	@Override
            	public void actionPerformed(ActionEvent e) {  		  		
            		ThePresident.app.requestToggleFullScreen(main);
            	}
            };
            main.viewEnterFullScreenMenuItem.addActionListener(fullScreenListener);
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