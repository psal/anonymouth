package edu.drexel.psal.anonymouth.gooie;

import edu.drexel.psal.JSANConstants;
import edu.drexel.psal.jstylo.generics.Logger;

import java.io.File;
import java.util.Scanner;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import com.apple.eawt.Application;
import com.apple.eawt.ApplicationAdapter;
import com.apple.eawt.ApplicationEvent;

/**
 * ThePresident sets up the Application and System fields/preferences prior to calling 'GUIMain'
 * @author Andrew W.E. McDonald
 *
 */
@SuppressWarnings("deprecation")
public class ThePresident {

	/*
	 * Anonymouth constants and such
	 */
	//protected static ImageIcon buffImg;
	private final String NAME = "( "+this.getClass().getSimpleName()+" ) - ";
	//public final static String WORKING_DIR = System.getProperty("java.library.path") + "/";
	public final static String WORKING_DIR = "./";
	public static ImageIcon LOGO;
	public static ImageIcon ABOUTLOGO;
	public static String sessionName;
	public static final String DOC_MAGICIAN_WRITE_DIR = WORKING_DIR + ".edited_documents/";
	public static final String LOG_DIR = WORKING_DIR + "anonymouth_log";
	//public static final String LOG_DIR = System.getProperty("user.home")+"/Desktop/anonymouth_log";
	public static boolean IS_MAC = false;
	public static String SER_DIR = WORKING_DIR + ".serialized_objects/";
	public static boolean SHOULD_KEEP_AUTO_SAVED_ANONYMIZED_DOCS = PropertiesUtil.getAutoSave();
	public static boolean SAVE_TAGGED_DOCUMENTS = true; // TODO: put in "options
	public static int MAX_FEATURES_TO_CONSIDER = PropertiesUtil.getMaximumFeatures(); // todo: put in 'options', and figure out an optimal number (maybe based upon info gain, total #, etc.)... basically, when the processing time outweighs the benefit, that should be our cutoff.
	public static int NUM_TAGGING_THREADS = PropertiesUtil.getThreadCount();
	public static final String PATH_TO_CLASSIFIER = SER_DIR+"saved_classifier.model";
	public static boolean CLASSIFIER_SAVED = false;
	public static final String DUMMY_NAME = "~* you *~"; // NOTE DO NOT CHANGE THIS unless you have a very good reason to do so.
	public static int NUM_CLUSTER_GROUPS_TO_TEST = -1;
	public static Application app;
	public static Scanner in = new Scanner(System.in); // xxx just for testing. can be called anywhere in Anonymouth.

	public ImageIcon getLogo(String name) {
		ImageIcon icon = null;
		
		try{
			icon = new ImageIcon(getClass().getResource(name), "Anonymouth's Logo");
		} catch (Exception e){
			e.printStackTrace();
		}
		
		return icon;
	}
	
	/**
	 * TEST METHOD
	 * will print "sayThis" and then read and return a line from the user. Useful for stopping the progam at spots.
	 * @param sayThis
	 * @return
	 */
	public static String read(String sayThis){
		System.out.println(sayThis);
		return in.nextLine();
	}

	/**
	 * TEST METHOD
	 * will print "System waiting for user input:" and then read and return a line from the user. Useful for stopping the progam at spots.
	 * @return
	 */
	public static String read(){
		System.out.println("System waiting for user input:");
		return in.nextLine();
	}

	public static void main(String[] args){
		String OS = System.getProperty("os.name").toLowerCase();
		ThePresident leader = new ThePresident();
		if(OS.contains("mac")) {
			IS_MAC = true;
			Logger.logln(leader.NAME+"We're on a Mac!");
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			app = Application.getApplication();
			String logoName = JSANConstants.JSAN_GRAPHICS_PREFIX+"anonymouth_LOGO_large.png";
			String aboutName = JSANConstants.JSAN_GRAPHICS_PREFIX+"anonymouth_LOGO.png";
			try{
				LOGO = leader.getLogo(logoName);
				ABOUTLOGO = leader.getLogo(aboutName);
				app.setDockIconImage(LOGO.getImage());
			}catch(Exception e){
				Logger.logln("Error loading logos");
			}
			
			/**
			 * The best method I've found yet for handling the OS X menu look and feel, everything works perfectly.
			 */
			app.addApplicationListener(new ApplicationAdapter() {
				@Override
				public void handleQuit(ApplicationEvent e) {
					if (PropertiesUtil.getWarnQuit() && !GUIMain.saved) {
						GUIMain.inst.toFront();
						GUIMain.inst.requestFocus();
						int confirm = JOptionPane.showOptionDialog(null,
								"Are You Sure to Close Application?\nYou will lose all unsaved changes.",
								"Unsaved Changes Warning",
								JOptionPane.YES_NO_OPTION,
								JOptionPane.QUESTION_MESSAGE,
								null, null, null);
						if (confirm == 0) {
							System.exit(0);
						}
					} else if (PropertiesUtil.getAutoSave()) {
						DriverEditor.save(GUIMain.inst);
						System.exit(0);
					} else {
						System.exit(0);
					}
				}
				
				@Override
				public void handleAbout(ApplicationEvent e) {
					e.setHandled(true); //Tells the system to not display their own "About" window since we've got this covered.
					JOptionPane.showMessageDialog(null, 
							"Anonymouth, Version 0.5\n\nAuthors: Andrew W.E. McDonald\n   -Marc Barrowclift\n   -Joe Muoio\n   -Jeff Ulman\n\nDrexel University, PSAL, Dr. Rachel Greenstadt - P.I.",
							"About Anonymouth",
							JOptionPane.INFORMATION_MESSAGE,
							ABOUTLOGO);
				}
				
				@Override
				public void handlePreferences(ApplicationEvent e) {
					GUIMain.GSP.openWindow();
				}
			});
			
			app.setEnabledPreferencesMenu(true);
			app.requestForeground(true);
		}
		sessionName = "anonymous"; 
		String tempName = null;
		tempName = JOptionPane.showInputDialog("Please name your session:", sessionName);
		if(tempName == null)
			System.exit(665);
			
		tempName = tempName.replaceAll("['.?!()<>#\\\\/|\\[\\]{}*\":;`~&^%$@+=,]", "");
		tempName = tempName.replaceAll(" ", "_");
		if(tempName != null)
			sessionName = tempName;
		System.out.println(tempName+" "+sessionName);
		
		File log_dir = new File(LOG_DIR); // create log directory if it doesn't exist.
		if (!log_dir.exists()){
			System.out.println(leader.NAME+"Creating directory for DocumentMagician to write to...");
			log_dir = log_dir.getAbsoluteFile();
			log_dir.mkdir();
		}
		Logger.setFilePrefix("Anonymouth_"+sessionName);
		Logger.logFile = true;	
		Logger.initLogFile();
		File dm_write_dir = new File(DOC_MAGICIAN_WRITE_DIR);
		if (!dm_write_dir.exists()){
			Logger.logln(leader.NAME+"Creating directory for DocumentMagician to write to...");
			dm_write_dir.mkdir();
		}
		File ser_dir = new File(SER_DIR);
		if (!ser_dir.exists()){
			Logger.logln(leader.NAME+"Creating directory to save serialized objects to...");
			ser_dir.mkdir();
		}
		
		Logger.logln("Gooie starting...");
		GUIMain.startGooie();
	}
}
