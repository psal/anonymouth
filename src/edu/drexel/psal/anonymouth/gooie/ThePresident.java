package edu.drexel.psal.anonymouth.gooie;

import edu.drexel.psal.ANONConstants;
import edu.drexel.psal.anonymouth.helpers.ImageLoader;
import edu.drexel.psal.anonymouth.utils.About;
import edu.drexel.psal.jstylo.generics.Logger;

import java.awt.Image;
import java.io.File;
import java.util.Scanner;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import com.apple.eawt.Application;
import com.apple.eawt.ApplicationAdapter;
import com.apple.eawt.ApplicationEvent;

/**
 * ThePresident sets up the Application and System fields/preferences prior to calling 'GUIMain'
 * @author Andrew W.E. McDonald
 * @author Marc Barrowclift
 *
 */
@SuppressWarnings("deprecation") //For the Apple Look and Feel code below (If you find something better, please replace)
public class ThePresident {
	
	//Constants
	private final String NAME = "( "+this.getClass().getSimpleName()+" ) - ";
	public final String ANONYMOUTH_LOGO = "anonymouth_LOGO.png";
	public final String ANONYMOUTH_LOGO_LARGE = "anonymouth_LOGO_large.png";
	public final String ANONYMOUTH_LOGO_SMALL = "anonymouth_gui_chooser.png";
	
	//Anonymouth Icons
	public static Image logo;
	public static ImageIcon aboutLogo;
	public static ImageIcon dialogLogo;
	public static Icon dialogIcon;
	protected static StartWindow startWindow;
	public GUIMain main;
	
	public static Application app; //For OS X
	public Scanner in = new Scanner(System.in); // xxx just for testing. can be called anywhere in Anonymouth.
	public static String sessionName = "";
	public static boolean classifier_Saved = false;
	public static int max_Features_To_Consider = PropertiesUtil.defaultFeatures;
	public static int num_Tagging_Threads = PropertiesUtil.defaultThreads;
	public static boolean should_Keep_Auto_Saved_Anonymized_Docs = PropertiesUtil.defaultVersionAutoSave;
	public static boolean autosave_Latest_Version = PropertiesUtil.defaultAutoSave;
	public static boolean canDoQuickStart = false;
	public static SplashScreen splash;

	public static void main(String[] args) {
		new ThePresident();
	}
	
	public ThePresident() {
		splash = new SplashScreen();
		splash.showSplashScreen();
		
		logo = ImageLoader.getImage(ANONYMOUTH_LOGO_LARGE);
		aboutLogo = ImageLoader.getImageIcon(ANONYMOUTH_LOGO);
		dialogLogo = ImageLoader.getImageIcon(ANONYMOUTH_LOGO_SMALL);
		dialogIcon = ImageLoader.getIcon(ANONYMOUTH_LOGO_SMALL);
		
		if (ANONConstants.IS_MAC) {
			System.setProperty("WEKA_HOME", "/dev/null");
			
			Logger.logln(NAME+"We're on a Mac!");
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			
			app = Application.getApplication();
			app.setDockIconImage(logo);
			
			/**
			 * The best method I've found yet for handling the OS X menu look and feel, everything works perfectly.
			 */
			app.addApplicationListener(new ApplicationAdapter() {
				@Override
				public void handleQuit(ApplicationEvent e) {
					if (PropertiesUtil.getWarnQuit() && !main.saved) {
						main.toFront();
						main.requestFocus();
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
						main.menuDriver.save(GUIMain.inst);
						System.exit(0);
					} else {
						System.exit(0);
					}
				}
				
				@Override
				public void handleAbout(ApplicationEvent e) {
					e.setHandled(true); //Tells the system to not display their own "About" window since we've got this covered.
					JOptionPane.showMessageDialog(null, 
							About.aboutAnonymouth,
							"About Anonymouth",
							JOptionPane.INFORMATION_MESSAGE,
							aboutLogo);
				}
				
				@Override
				public void handlePreferences(ApplicationEvent e) {
					main.preferencesWindow.showWindow();
				}
			});
			
			app.setEnabledPreferencesMenu(true);
			app.requestForeground(true);
		}
		
		File log_dir = new File(ANONConstants.LOG_DIR); // create log directory if it doesn't exist.
		if (!log_dir.exists()){
			Logger.logln(NAME+"Creating directory for DocumentMagician to write to...");
			log_dir = log_dir.getAbsoluteFile();
			log_dir.mkdir();
		}
		
		File dm_write_dir = new File(ANONConstants.DOC_MAGICIAN_WRITE_DIR);
		if (!dm_write_dir.exists()) {
			Logger.logln(NAME+"Creating directory for DocumentMagician to write to...");
			dm_write_dir.mkdir();
		}
		File ser_dir = new File(ANONConstants.SER_DIR);
		if (!ser_dir.exists()){
			Logger.logln(NAME+"Creating directory to save serialized objects to...");
			ser_dir.mkdir();
		}
		
		if (!ANONConstants.IS_USER_STUDY) {
			sessionName = "Anonymouth";
		}
		
		splash.updateText("Preparing Start Window");
		
		main = new GUIMain();
		startWindow = new StartWindow(main);
		
		splash.hideSplashScreen();
		startWindow.showStartWindow();
	}
	
	/**
	 * TEST METHOD
	 * will print "sayThis" and then read and return a line from the user. Useful for stopping the progam at spots.
	 * @param sayThis
	 * @return
	 */
	public String read(String sayThis){
		System.out.println(sayThis);
		return in.nextLine();
	}

	/**
	 * TEST METHOD
	 * will print "System waiting for user input:" and then read and return a line from the user. Useful for stopping the progam at spots.
	 * @return
	 */
	public String read(){
		System.out.println("System waiting for user input:");
		return in.nextLine();
	}
}