package edu.drexel.psal.anonymouth.gooie;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Dialog.ModalityType;
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
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import edu.drexel.psal.JSANConstants;
import edu.drexel.psal.ANONConstants;
import edu.drexel.psal.jstylo.generics.Logger;
import edu.drexel.psal.jstylo.generics.Logger.LogOut;
import edu.drexel.psal.jstylo.generics.ProblemSet;

/**
 * The starting window of Anonymouth that asks for a session name and allows the user to do one of three actions:
 * 	1.Start Anonymouth (Only available if they have previous document set saved, otherwise it will be greyed out, default action if otherwise).
 * 	2.Create a new problem set (Default action if no previous problem set in records, load and start will be greyed out in this case).
 * 	3.Load a new problem set, action always available.
 * 	4.quitButton, action always available.
 * 
 * This is created in an effort to help guide the user along and not throw too much at them at the beginning.
 * @author Marc Barrowclift
 *
 */
public class StartingWindows extends JFrame {

	private static final long serialVersionUID = 1L;
	private final String NAME = "( StartingWindows ) - ";
	
	private GUIMain main;
	private int width = 520, height = 135;
	private FileDialog load;
	private StartingWindows startingWindows;
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
	private JPanel buttonPanel;
	//private JSeparator separator;
	private JButton loadDocSetButton;
	protected JButton newDocSetButton;
		
	//Listeners
	private ActionListener startListener;
	private ActionListener newDocSetListener;
	protected ActionListener loadDocSetListener;
	
	/**
	 * Constructor
	 * @param main - Instance of GUIMain
	 */
	public StartingWindows(GUIMain main) {	
		initGUI();
		initWindow(main);
		initListeners();
		
		load = new FileDialog(this);
		load.setModalityType(ModalityType.DOCUMENT_MODAL);
		
		userStudySessionName = new UserStudySessionName(this);
	}
	
	/**
	 * Initializes the GUI (visible = false)
	 */
	private void initGUI() {
		startingWindows = this;
		
		loadDocSetButton = new JButton("Load Document Set");
		newDocSetButton = new JButton("New Document Set");
		
		topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
		textPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		textLabel = new JLabel();
		textLabel.setFont(new Font("Helvetica", Font.BOLD, 24));
		textPanel.add(textLabel);
		startPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		startButton = new JButton("Start");
		startButton.setMinimumSize(new Dimension(100, 35));
		startButton.setPreferredSize(new Dimension(100, 35));
		startButton.setMaximumSize(new Dimension(100, 35));
		startButton.setSize(new Dimension(100, 35));
		startPanel.add(startButton);
		
		if (ThePresident.canDoQuickStart) {
			textLabel.setText("<html><center>Resume with previously used document set</center></html>");
		} else {
			textLabel.setText("<html><center>No previous document set found</center></html>");
			textLabel.setForeground(Color.LIGHT_GRAY);
			startButton.setEnabled(false);
		}
		
		topPanel.add(textPanel);
		topPanel.add(Box.createRigidArea(new Dimension(0,5)));
		topPanel.add(startPanel);
		topPanel.setBorder(new EmptyBorder(5, 0, 0, 0));
		
		buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
		buttonPanel.add(loadDocSetButton);
		buttonPanel.add(newDocSetButton);
		
		//separator = new JSeparator();
		//separator.setMaximumSize(new Dimension(480, 0));
		
		bottomPanel = new JPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
		//JSeparator separator
		//bottomPanel.add(separator);
		bottomPanel.add(buttonPanel);
		bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 3, 0));
		//Color Separator
		/*
		bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, -5));
		bottomPanel.add(quitButton);
		bottomPanel.add(loadDocSetButton);
		bottomPanel.add(newDocSetButton);
		bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
		bottomPanel.setPreferredSize(new Dimension(520, 35));
		bottomPanel.setBackground(new Color(180, 143, 186));
		*/

		completePanel = new JPanel(new BorderLayout());
		completePanel.add(topPanel, BorderLayout.NORTH);
		completePanel.add(bottomPanel, BorderLayout.SOUTH);
		this.add(completePanel);
		
		if (PropertiesUtil.getProbSet().equals("")) {
			startButton.setEnabled(false);
		}
	}
	
	private void initWindow(GUIMain main) {
		this.setSize(width, height);
		this.setResizable(false);
		this.setLocationRelativeTo(null);
		this.setVisible(false);
		this.main = main;
		this.setTitle("Anonymouth Start Window");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	/**
	 * Initializes the listeners
	 */
	private void initListeners() {
		startListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				startingWindows.setVisible(false);
				main.showMainGUI();
			}
		};
		startButton.addActionListener(startListener);
		
		loadDocSetListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Logger.logln(NAME+"'Load Problem Set' button clicked on the documents tab");

					load.setTitle("Load A Previous Document Set");
					if (PropertiesUtil.prop.getProperty("recentProbSet") != null) {
						Logger.logln(NAME+"Chooser root directory set to: " + PropertiesUtil.prop.getProperty("recentProbSet"));
						load.setDirectory(PropertiesUtil.prop.getProperty("recentProbSet"));
					} else {
						load.setDirectory(JSANConstants.JSAN_PROBLEMSETS_PREFIX);
					}		
					load.setMode(FileDialog.LOAD);
					load.setMultipleMode(false);
					load.setFilenameFilter(ANONConstants.XML);
					load.setLocationRelativeTo(null);
					load.setVisible(true);

					File[] files = load.getFiles();
					if (files.length != 0) {
						String path = files[0].getAbsolutePath();

						Logger.logln(NAME+"Trying to load problem set at: " + path);
						try {
							main.preProcessWindow.ps = new ProblemSet(path);
							main.ppAdvancedWindow.setClassifier(PropertiesUtil.getClassifier());
							main.ppAdvancedWindow.featureChoice.setSelectedItem(PropertiesUtil.getFeature());
							main.preProcessWindow.driver.updateAllComponents();
							PropertiesUtil.setProbSet(path);
						} catch (Exception exc) {
							exc.printStackTrace();
							Logger.logln(NAME+"Failed loading "+path, LogOut.STDERR);
							Logger.logln(NAME+exc.toString(),LogOut.STDERR);
							JOptionPane.showMessageDialog(null,
									"Failed loading problem set from:\n"+path,
									"Load Problem Set Failure",
									JOptionPane.ERROR_MESSAGE);
							PropertiesUtil.setProbSet("");
						}
					} else {
						Logger.logln(NAME+"Load problem set canceled");
					}
				} catch (NullPointerException arg) {
					arg.printStackTrace();
				}
			}
		};
		loadDocSetButton.addActionListener(loadDocSetListener);
		
		newDocSetListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {				
				main.preProcessWindow.showWindow();
			}
		};
		newDocSetButton.addActionListener(newDocSetListener);
	}
	
	protected void setReadyToStart(boolean ready) {
		if (ready) {
			if (!PropertiesUtil.getProbSet().equals(""))
				textLabel.setText("Start with document set \"" + new File(PropertiesUtil.getProbSet()).getName() + "\"");
			else
				textLabel.setText("Start with completed document set");
			
			textLabel.setForeground(Color.BLACK);
			startButton.setEnabled(true);
			this.getRootPane().setDefaultButton(startButton);
			startButton.requestFocusInWindow();
		} else {
			textLabel.setText("Please finish your document set to start");
			textLabel.setForeground(Color.LIGHT_GRAY);
			startButton.setEnabled(false);
			this.getRootPane().setDefaultButton(newDocSetButton);
			newDocSetButton.requestFocusInWindow();
		}
	}
	
	/**
	 * Makes the prepared window visible
	 */
	public void showStartingWindow() {
		if (ANONConstants.IS_USER_STUDY && ThePresident.sessionName.equals("")) {
			userStudySessionName.showSessionWindow();
		} else {
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
}

class UserStudySessionName extends JFrame {

	private static final long serialVersionUID = 1L;
	private final String NAME = "( UserStudySessionName ) - ";
	
	private int width = 520, height = 135;
	private UserStudySessionName sessionWindow;
	private StartingWindows startingWindows;
	
	private JLabel inputMessage;
	private JTextField textBox;
	private JButton continueButton;
	private JPanel textBoxAndNextPanel;
	private JPanel mainSessionNamePanel;
	
	private ActionListener continueListener;
	private FocusListener textBoxListener;
	
	public UserStudySessionName(StartingWindows startingWindows) {
		this.startingWindows = startingWindows;
		sessionWindow = this;
		
		initGUI();
		initListeners();
	}
	
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
		this.setSize(width, height);
		this.setResizable(false);
		this.setLocationRelativeTo(null);
		this.setVisible(false);
	}
	
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
				
				startingWindows.showStartingWindow();
			}
		};
		continueButton.addActionListener(continueListener);
	}
}