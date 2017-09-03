package edu.drexel.psal.anonymouth.gooie;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import edu.drexel.psal.ANONConstants;
import edu.drexel.psal.anonymouth.helpers.FileHelper;
import edu.drexel.psal.jstylo.generics.Logger;
import edu.drexel.psal.jstylo.generics.Logger.LogOut;
import edu.drexel.psal.jstylo.generics.ProblemSet;

/**
 * The starting window of Anonymouth that allows the user to load, modify, or start a new document set,
 * Make changes in advanced configuration, or start Anonymouth.<br><br>
 * 
 * This is created in an effort to help guide the user along and not throw too much at them at the beginning.
 * @author Marc Barrowclift
 *
 */
public class StartWindow extends JFrame {

	private static final long serialVersionUID = 1L;
	private final String NAME = "( StartWindow ) - ";
	
	private GUIMain main;
	private int width = 520, height = 135;
	private StartWindow startingWindows;
	private UserStudySessionName userStudySessionName;
	
	//Swing Components
	private JPanel completePanel;	
	//Top
	private JPanel topPanel;
	private JLabel textLabel;
	private JButton startButton;
	private JPanel startPanel;
	private JPanel textPanel;
	//Bottom
	private JPanel bottomPanel;
	private JPanel rightButtonsPanel;
	private JPanel buttonPanel;
	private JSeparator separator;
	private JButton loadDocSetButton;
	private JButton newDocSetButton;
	protected JButton modifyDocSetButton;
	protected JButton advancedConfigButton;
		
	//Listeners
	private ActionListener startListener;
	private ActionListener newDocSetListener;
	private ActionListener modifyDocSetListener;
	protected ActionListener loadDocSetListener;
	protected ActionListener advancedConfigListener;
	
	/**
	 * Constructor
	 * @param main
	 * 		Instance of GUIMain
	 */
	public StartWindow(GUIMain main) {	
		initGUI();
		initWindow(main);
		initLookAndFeel();
		initListeners();
		
		FileHelper.goodLoad = new FileDialog(this);
		
		userStudySessionName = new UserStudySessionName(this);
		setLastDocumentSet();
	}
	
	/**
	 * Initializes the GUI (visible = false)
	 */
	private void initGUI() {
		startingWindows = this;
		
		newDocSetButton = new JButton("New");
		newDocSetButton.setAlignmentX(Container.RIGHT_ALIGNMENT);
		loadDocSetButton = new JButton("Load");
		loadDocSetButton.setAlignmentX(Container.RIGHT_ALIGNMENT);
		modifyDocSetButton = new JButton("Modify");
		modifyDocSetButton.setAlignmentX(Container.RIGHT_ALIGNMENT);
		
		advancedConfigButton = new JButton("Advanced Configuration");
		advancedConfigButton.setAlignmentX(Container.LEFT_ALIGNMENT);
		
		topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
		textPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		textLabel = new JLabel();
		textLabel.setFont(new Font("Helvetica", Font.BOLD, 24));
		textPanel.add(textLabel);
		startPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		startButton = new JButton("Start");
		startButton.setPreferredSize(new Dimension(100, 30));
		startPanel.add(startButton);

		if (ThePresident.canDoQuickStart) {
			textLabel.setText("Start with previously used document set");
		} else {
			textLabel.setText("No previous document set found");
			textLabel.setForeground(Color.LIGHT_GRAY);
			startButton.setEnabled(false);
		}
		
		topPanel.add(textPanel);
		topPanel.add(Box.createRigidArea(new Dimension(0,5)));
		topPanel.add(startPanel);
		topPanel.setBorder(new EmptyBorder(5, 0, 0, 0));
		
		//===========================================
		//*****		Setting up bottom buttons	*****
		//===========================================
		buttonPanel = new JPanel();
		separator = new JSeparator();
		separator.setMaximumSize(new Dimension(480, 0));
		{
			if (ANONConstants.SHOW_ADVANCED_SETTINGS) {
				buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
				buttonPanel.setBorder(new EmptyBorder(0,14,0,15));
				buttonPanel.add(advancedConfigButton);
				
				rightButtonsPanel = new JPanel();
				rightButtonsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
				rightButtonsPanel.add(newDocSetButton);
				rightButtonsPanel.add(loadDocSetButton);
				rightButtonsPanel.add(modifyDocSetButton);
				buttonPanel.add(rightButtonsPanel);
			} else {
				buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
				buttonPanel.add(newDocSetButton);
				buttonPanel.add(loadDocSetButton);
				buttonPanel.add(modifyDocSetButton);
			}
		}
		bottomPanel = new JPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
		bottomPanel.add(separator);
		bottomPanel.add(buttonPanel);
		bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 3, 0));
		//Color Separator
		/*
		bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, -5));
		bottomPanel.add(quitButton);
		bottomPanel.add(loadDocSetButton);
		bottomPanel.add(modifyDocSetButton);
		bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
		bottomPanel.setPreferredSize(new Dimension(520, 35));
		bottomPanel.setBackground(new Color(180, 143, 186));
		*/

		completePanel = new JPanel(new BorderLayout());
		completePanel.add(topPanel, BorderLayout.NORTH);
		completePanel.add(bottomPanel, BorderLayout.SOUTH);
		this.add(completePanel);
	}
	
	/**
	 * Sets all the window attributes (like size, location, etc)
	 * @param main - GUIMain instance
	 */
	private void initWindow(GUIMain main) {
		this.setSize(width, height);
		this.setResizable(false);
		
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
		
		this.setVisible(false);
		this.main = main;
		this.setTitle("Anonymouth Start Window");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	/**
	 * Sets the look and feel for Anonymouth based on the system look and feel
	 */
	private void initLookAndFeel() {
		//For Sebastian Pipping's GTK look and feel
		final String[] themeClassesToTry = {
			"com.sun.java.swing.plaf.windows.WindowsLookAndFeel",
			"com.sun.java.swing.plaf.gtk.GTKLookAndFeel",
		};
		
		for (String themeClassName : themeClassesToTry) {
			try {
				UIManager.setLookAndFeel(themeClassName);
			} catch (Exception e) {
				//If setting the GTK look and feel fails, then default to the system look and feel
				try { 
					javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
				} catch(Exception e1) {
					e1.printStackTrace();
				}
				
				break;
			}
		}
	}
	
	/**
	 * Initializes the listeners
	 */
	private void initListeners() {
		startListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				startingWindows.setVisible(false);
				
				/**
				 * makes sure to internally rename files by different authors that have the same name.
				 * It Used to be handed upon adding, but better for the user if they never have to worry about it
				 */
				main.preProcessWindow.assertUniqueTitles();
				main.documentProcessor.process();
			}
		};
		startButton.addActionListener(startListener);
		
		loadDocSetListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Logger.logln(NAME+"'Load' button clicked on the documents tab");

					/**
					 * In case something starts to go wrong with the FileDialogs (they are older and
					 * may be deprecated as some point). If this be the case, just swap in this code instead
					 */
					/*
					FileHelper.load.setName("Load Saved Document Set Set");
					FileHelper.load.setCurrentDirectory(new File(JSANConstants.JSAN_PROBLEMSETS_PREFIX));
					FileHelper.load.setFileFilter(ANONConstants.XML);
					FileHelper.load.setFileSelectionMode(JFileChooser.FILES_ONLY);
					FileHelper.load.setMultiSelectionEnabled(false);
					FileHelper.load.setVisible(true);
					int answer = FileHelper.load.showOpenDialog(startingWindows);
					
					if (answer == JFileChooser.APPROVE_OPTION) {
						File file = FileHelper.load.getSelectedFile();
						*/
					
					FileHelper.goodLoad.setTitle("Load Your Document To Anonymize");
					if (PropertiesUtil.getProbSet() != "") {
						FileHelper.goodLoad.setDirectory(PropertiesUtil.getProbSet());
					} else {
						FileHelper.goodLoad.setDirectory(ANONConstants.PROBLEMSETS_PREFIX);
					}
					FileHelper.goodLoad.setMultipleMode(false);
					FileHelper.goodLoad.setFilenameFilter(ANONConstants.XML);
					FileHelper.goodLoad.setLocationRelativeTo(null);
					FileHelper.goodLoad.setVisible(true);

					String fileName = FileHelper.goodLoad.getFile();
					if (fileName != null) {
						File file = new File(FileHelper.goodLoad.getDirectory()+fileName);
						loadProblemSet(file.getAbsolutePath());
					} else {
						Logger.logln(NAME+"Load document set canceled");
					}
				} catch (NullPointerException arg) {
					arg.printStackTrace();
				}
			}
		};
		loadDocSetButton.addActionListener(loadDocSetListener);
		
		modifyDocSetListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {				
				main.preProcessWindow.showWindow();
			}
		};
		modifyDocSetButton.addActionListener(modifyDocSetListener);
		
		newDocSetListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				main.preProcessDriver.resetAllComponents();
				setReadyToStart(false, false);
				main.preProcessWindow.switchingToTest();
				main.preProcessWindow.showWindow();
			}
		};
		newDocSetButton.addActionListener(newDocSetListener);
		
		advancedConfigListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				main.ppAdvancedWindow.showWindow();
			}
		};
		advancedConfigButton.addActionListener(advancedConfigListener);
	}
	
	/**
	 * If there was a recently used/saved document set in the properties file, Anonymouth will load up and initialize
	 * itself around that problem set so Anonymouth's ready to immediately start if the user wants.
	 */
	protected void setLastDocumentSet() {		
		try {
			if (!PropertiesUtil.getProbSet().equals("")) {
				String problemSetPath = PropertiesUtil.getProbSet();

				try {
					loadProblemSet(problemSetPath);
				} catch (Exception exc) {
					setReadyToStart(false, true);
					throw new Exception("Failed loading problemSet path \""+problemSetPath+"\"");
				}
			} else {
				setReadyToStart(false, false);
				throw new Exception("No default problem set saved from last run, will continue without.");
			}
		} catch (Exception e) {
			Logger.logln(NAME+"Problem loading last used document set: " + PropertiesUtil.getProbSet(), LogOut.STDERR);
			PropertiesUtil.setProbSet("");
			
			String feature = PropertiesUtil.getFeature();
			main.ppAdvancedWindow.setFeature(feature);

			String classifier = PropertiesUtil.getClassifier();
			main.ppAdvancedWindow.setClassifier(classifier);
		}
	}
	
	/**
	 * Determines whether or not the user has an acceptable document set built and updates components accordingly
	 * @param ready
	 */
	public void setReadyToStart(boolean ready, boolean loaded) {
		if (ready) {
			if (loaded)
				textLabel.setText("Start with loaded document set");
			else
				textLabel.setText("Start with finished document set");
			modifyDocSetButton.setEnabled(true);
			textLabel.setForeground(Color.BLACK);
			startButton.setEnabled(true);
			this.getRootPane().setDefaultButton(startButton);
			main.preProcessWindow.saved = true;
		} else {
			if (loaded) {
				textLabel.setText("Please finish incomplete document set");
				modifyDocSetButton.setEnabled(true);
				this.getRootPane().setDefaultButton(modifyDocSetButton);
			} else {
				textLabel.setText("No previous document set found");
				this.getRootPane().setDefaultButton(newDocSetButton);
				modifyDocSetButton.setEnabled(false);
			}
			textLabel.setForeground(Color.LIGHT_GRAY);
			startButton.setEnabled(false);
			main.preProcessWindow.saved = false;
		}
	}
	
	/**
	 * Makes the prepared window visible
	 */
	@SuppressWarnings("unused") //Eclipse lies, it's being used, it just doesn't like my ANONConstants flag
	public void showStartWindow() {
		if (ANONConstants.IS_USER_STUDY && ThePresident.sessionName.equals("")) {
			userStudySessionName.showSessionWindow();
		} else {
			ThePresident.sessionName = "Anonymouth";
			Logger.setFilePrefix(ThePresident.sessionName);
			Logger.logFile = true;
			Logger.initLogFile();
			Logger.logln(NAME+"Logger initialized, session name = " + ThePresident.sessionName);
			Logger.logln(NAME+"Displaying Anonymouth Start Window");

			if (ThePresident.canDoQuickStart) {
				this.getRootPane().setDefaultButton(startButton);
			} else {
				this.getRootPane().setDefaultButton(newDocSetButton);
			}
			
			this.setVisible(true);
			
			if (ThePresident.canDoQuickStart) {
				startButton.requestFocusInWindow();
			} else {
				newDocSetButton.requestFocusInWindow();
			}
		}
	}
	
	/**
	 * Loads the problem set from the given path, attempts to load everything, then checks to make sure everything's ready. Updates
	 * Components accordingly
	 * @param path - The absolute path to the problem set we want to load
	 */
	public void loadProblemSet(String path) {
		Logger.logln(NAME+"Trying to load problem set at: " + path);
		try {
			main.preProcessWindow.ps = new ProblemSet(path);
			main.ppAdvancedWindow.setClassifier(PropertiesUtil.getClassifier());
			main.ppAdvancedWindow.setFeature(PropertiesUtil.getFeature());
			main.preProcessDriver.titles.clear();
			
			boolean probSetReady = main.preProcessWindow.documentsAreReady();
			if (main.preProcessDriver.updateAllComponents() && probSetReady) {
				setReadyToStart(true, true);
				ThePresident.canDoQuickStart = true;
				
				//main.updateDocLabel(main.preProcessWindow.ps.getTestDoc().getTitle(), 0);
				main.preProcessDriver.updateOpeningDir(main.preProcessWindow.ps.getTestDoc().getFilePath(), false);
				main.preProcessDriver.updateOpeningDir(main.preProcessWindow.ps.getAllTrainDocs().get(0).getFilePath(), true);
			} else {
				Logger.logln(NAME+"Some issue was detected constructing the saved Document set, will verify " +
						"if there's still enough documents to move forward");
				if (probSetReady) {
					JOptionPane.showMessageDialog(startingWindows,
							"Anonymouth encountered a few problems loading your document set,\n" +
							"some documents may have not been added in the process. Some\n" +
							"possible causes of this may be:\n\n" +
							"   -The document no longer exists in it's original path\n" +
							"   -The document no longer has read permissions\n" +
							"   -The document is not empty, where it wasn't in the past",
							"Problems with Loading Document Set",
							JOptionPane.WARNING_MESSAGE, ThePresident.dialogLogo);
					setReadyToStart(true, true);
					ThePresident.canDoQuickStart = true;
				} else {
					JOptionPane.showMessageDialog(startingWindows,
							"Anonymouth encountered a few problems loading your document set\n" +
							"and now there isn't enough loaded documents to proceed. Some\n" +
							"possible causes of this may be:\n\n" +
							"   -The document no longer exists in it's original path\n" +
							"   -The document no longer has read permissions\n" +
							"   -The document is not empty, where it wasn't in the past",
							"Problems with Loading Document Set",
							JOptionPane.WARNING_MESSAGE, ThePresident.dialogLogo);
					Logger.logln(NAME+"One or more parts of the loaded doc set are insufficient now to begin due to" +
							"loading problems, cannot quick start");
					setReadyToStart(false, true);
					ThePresident.canDoQuickStart = false;
				}
			}
			
			PropertiesUtil.setProbSet(path);
			main.preProcessDriver.updateTitles();
		} catch (Exception exc) {
			Logger.logln(NAME+"Failed loading problem set at path: "+path, LogOut.STDERR);
			setReadyToStart(false, false);
			ThePresident.canDoQuickStart = false;
			
			String feature = PropertiesUtil.getFeature();
			main.ppAdvancedWindow.setFeature(feature);

			String classifier = PropertiesUtil.getClassifier();
			main.ppAdvancedWindow.setClassifier(classifier);
			
			PropertiesUtil.setProbSet("");
			main.preProcessDriver.updateTitles();
			revalidate();
			repaint();
		}
	}
}

/**
 * Simple Frame to ask the user for their name so we can name the log files. This is intended for use in user case studies, and (at least
 * from last discussion) should not be shown or used in a release version of Anonymouth. Whether or not to display this can be easily flipped
 * via the constant boolean "IS_USER_STUDY" in ANONConstants.
 * @author Marc Barrowclift
 *
 */
class UserStudySessionName extends JFrame {

	//Constants
	private static final long serialVersionUID = 1L;
	private final String NAME = "( UserStudySessionName ) - ";
	private final int WIDTH = 520, HEIGHT = 135;
	
	//Class instances
	private UserStudySessionName sessionWindow;
	private StartWindow startingWindows;
	
	//Swing Components
	private JLabel inputMessage;
	private JTextField textBox;
	private JButton continueButton;
	private JPanel textBoxAndNextPanel;
	private JPanel mainSessionNamePanel;
	
	//Listeners
	private ActionListener continueListener;
	private FocusListener textBoxListener;
	
	/**
	 * Constructor
	 * @param startingWindows - StartingWindows instance
	 */
	public UserStudySessionName(StartWindow startingWindows) {
		this.startingWindows = startingWindows;
		sessionWindow = this;
		
		initGUI();
		initListeners();
	}
	
	/**
	 * Displays the session window prompt to the user with a nice fade in effect
	 */
	protected void showSessionWindow() {
		Logger.logln(NAME+"Displaying Session Window");
		mainSessionNamePanel.getRootPane().setDefaultButton(continueButton);
		continueButton.requestFocusInWindow();
		
		this.setOpacity((float)0/(float)100);
		this.setVisible(true);
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
	
	/**
	 * Initializes the swing components and adds them all to the frame
	 */
	private void initGUI() {		
		inputMessage = new JLabel("<html><center>Please enter your name:</center></html>");
		inputMessage.setHorizontalAlignment(SwingConstants.CENTER);
		inputMessage.setHorizontalTextPosition(SwingConstants.CENTER);
		inputMessage.setFont(new Font("Helvetica", Font.BOLD, 24));
		
		textBox = new JTextField();
		textBox.setPreferredSize(new Dimension(150, 23));
		
		continueButton = new JButton("Continue");
		
		textBoxAndNextPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		textBoxAndNextPanel.add(textBox);
		textBoxAndNextPanel.add(continueButton);
		
		mainSessionNamePanel = new JPanel(new BorderLayout(0, 0));
		mainSessionNamePanel.setBorder(new EmptyBorder(35, 20, 35, 20));
		mainSessionNamePanel.add(inputMessage, BorderLayout.NORTH);
		mainSessionNamePanel.add(textBoxAndNextPanel, BorderLayout.SOUTH);
		
		this.add(mainSessionNamePanel);
		this.setUndecorated(true);
		this.setSize(WIDTH, HEIGHT);
		this.setResizable(false);
		this.setLocationRelativeTo(null);
		this.setVisible(false);
	}
	
	/**
	 * Initialzes all listeners used by the frame and adds them to their respective components
	 */
	private void initListeners() {
		textBoxListener = new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				textBox.setText("");
			}
			@Override
			public void focusLost(FocusEvent e) {
				if (textBox.getText().equals("")) {
					textBox.setText("Anonymouth");
				}
			}
		};
		textBox.addFocusListener(textBoxListener);
		
		continueListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String sessionName = textBox.getText();
				if (sessionName.equals("")) {
					sessionName = "Anonymouth";
				}
				sessionName = sessionName.replaceAll("['.?!()<>#\\\\/|\\[\\]{}*\":;`~&^%$@+=,]", "");
				String tempName = sessionName.replaceAll(" ", "_");
				if (tempName != null)
					sessionName = tempName;
				ThePresident.sessionName = sessionName;
				
				Logger.setFilePrefix("Anonymouth_"+ThePresident.sessionName);
				Logger.logFile = true;
				Logger.initLogFile();
				Logger.logln(NAME+"Logger initialized, GUIMain init complete");
				Logger.logln(NAME+"Session name: " + ThePresident.sessionName);
				
				Logger.logln(NAME+"Closing Session Window");
				
				for (int i = 100; i >= 0; i-=2) {
					sessionWindow.setOpacity((float)i/(float)100);
					
					try {
						Thread.sleep(3);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
				sessionWindow.setVisible(false);
				
				startingWindows.showStartWindow();
			}
		};
		continueButton.addActionListener(continueListener);
	}
}
