package edu.drexel.psal.anonymouth.gooie;

import edu.drexel.psal.JSANConstants;
import edu.drexel.psal.anonymouth.utils.About;
import edu.drexel.psal.jstylo.generics.Logger;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.util.Scanner;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

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
	//DO NOT DELETE, for use when bundling Anonymouth in an OS X app
	//public final static String WORKING_DIR = System.getProperty("java.library.path") + "/";
	public final static String WORKING_DIR = "./";
	public static ImageIcon LOGO;
	public static ImageIcon ABOUTLOGO;
	public static String sessionName;
	public static final String DOC_MAGICIAN_WRITE_DIR = WORKING_DIR + ".edited_documents/";
	public static final String LOG_DIR = WORKING_DIR + "anonymouth_log";
	//DO NOT DELETE, for use when bundling Anonymouth in an OS X app.
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
	public static final String ANONYMOUTH_LOGO = "anonymouth_LOGO.png";
	public static final String ANONYMOUTH_LOGO_LARGE = "anonymouth_LOGO_large.png";
	public static final String ARROW_UP = "arrow_up.png";
	public static final String ARROW_DOWN = "arrow_down.png";
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

	public static void main(String[] args) {
		String OS = System.getProperty("os.name").toLowerCase();
		ThePresident leader = new ThePresident();
		if(OS.contains("mac")) {
			System.setProperty("WEKA_HOME", "/dev/null");
			
			IS_MAC = true;
			Logger.logln(leader.NAME+"We're on a Mac!");
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			app = Application.getApplication();
			String logoName = JSANConstants.JSAN_GRAPHICS_PREFIX+ANONYMOUTH_LOGO_LARGE;
			String aboutName = JSANConstants.JSAN_GRAPHICS_PREFIX+ANONYMOUTH_LOGO;
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
							About.aboutAnonymouth,
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
		InputDialog inputDialog = new InputDialog(leader);
		inputDialog.showDialog();
	}
	
	public static void continueLoading(ThePresident leader, String tempName) {
		tempName = tempName.replaceAll("['.?!()<>#\\\\/|\\[\\]{}*\":;`~&^%$@+=,]", "");
		tempName = tempName.replaceAll(" ", "_");
		if(tempName != null)
			sessionName = tempName;
		
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
		if (!dm_write_dir.exists()) {
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

class InputDialog extends JFrame {

	private static final long serialVersionUID = 1L;
	private ThePresident leader;
	private JLabel warningMessage;
	private JLabel inputMessage;
	private JTextField textBox;
	private JButton ok;
	private JButton quit;
	private JPanel completePanel;
	private JPanel buttons;
	private JPanel messageAndBox;
	private ActionListener okListener;
	private ActionListener quitListener;
	private FocusListener textBoxListener;
	private InputDialog inputDialog;
	private int width = 520, height = 330;
	
	public InputDialog(ThePresident leader) {
		this.setSize(width, height);
		this.setResizable(false);
		this.setUndecorated(true);
		this.setBackground(Color.WHITE);
		this.setVisible(false);
		this.leader = leader;
		
		initGUI();
		initListeners();
	}
	
	private void initGUI() {		
		warningMessage = new JLabel(
				"<html><left>"+
				"<center><b><font color=\"#FF0000\" size = 6>WARNING:</font></b></center>" +
				"Anonymouth provides translations functionality that will help obsure your<br>" +
				"style by translating your document into multiple languages and back again.<br>" +
				"THIS MEANS THAT YOUR SENTENCES WILL BE SENT OFF REMOTELY TO<br>" +
				"MICROSOFT BING.<br><br>" +
				"This feature is turned off by default, and if you desire to use this feature<br>" +
				"and understand the risks you may turn it on by...<br><br>" +
				"FOR MAC:<br>" +
				"     <center><code>Anonymouth > Preferences > Tick the translations option</code></center>" +
				"FOR ALL OTHER OPERATING SYSTEMS:<br>" + 
				"     <center><code>Settings > Preferences > Tick the translations option</code></center>" +
				"</left></div></html>"
				);
		warningMessage.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
		inputMessage = new JLabel("Please name your session:");
		
		ok = new JButton("Start Anonymouth");
		quit = new JButton("Quit");
		
		textBox = new JTextField();
		textBox.setPreferredSize(new Dimension(300, 23));
		textBox.setText("Anonymouth");
		
		inputDialog = this;
		
		messageAndBox = new JPanel(new FlowLayout());
		messageAndBox.add(inputMessage);
		messageAndBox.add(textBox);
		
		buttons = new JPanel(new FlowLayout());
		buttons.add(quit);
		buttons.add(ok);
		
		completePanel = new JPanel(new BorderLayout());
		completePanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 0, 12));
		completePanel.add(messageAndBox, BorderLayout.NORTH);
		completePanel.add(warningMessage, BorderLayout.CENTER);
		completePanel.add(buttons, BorderLayout.SOUTH);
		this.add(completePanel);
		
		Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation((screensize.width/2)-(width/2), (screensize.height/2)-(height/2));
	}
	
	private void initListeners() {
		okListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (int i = 100; i >= 0; i-= 2) {
					inputDialog.setOpacity((float)i/(float)100);
					
					try {
						Thread.sleep(3);
					} catch (InterruptedException ie) {
						ie.printStackTrace();
					}
				}
				
				inputDialog.setVisible(false);
				ThePresident.continueLoading(leader, textBox.getText());
			}
		};
		ok.addActionListener(okListener);
		
		quitListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		};
		quit.addActionListener(quitListener);
		
		textBoxListener = new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				textBox.setText("");
			}
			@Override
			public void focusLost(FocusEvent e) {}
		};
		textBox.addFocusListener(textBoxListener);
	}
	
	public void showDialog() {
		this.getRootPane().setDefaultButton(ok);
		this.setOpacity((float)0/(float)100);
		this.setVisible(true);
		ok.requestFocusInWindow();
		for (int i = 0; i <= 100; i+=2) {
			this.setOpacity((float)i/(float)100);
			
			try {
				Thread.sleep(3);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		this.setOpacity((float)1.0);
	}
}