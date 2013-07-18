package edu.drexel.psal.anonymouth.gooie;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import edu.drexel.psal.JSANConstants;
import edu.drexel.psal.anonymouth.helpers.ExtFilter;
import edu.drexel.psal.anonymouth.helpers.ImageLoader;
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
public class StartingWindow extends JFrame {

	private static final long serialVersionUID = 1L;
	private final String NAME = "( StartingWindow ) - ";
	private GUIMain main;
	private PreProcessWindow preProcessWindow;
	public String sessionName;
	private Image background;
	private int width = 520, height = 160;
	
	//Swing Components
	private JPanel completePanel;
		//Top
		private JLabel inputMessage;
		private JTextField textBox;
		private StartingWindow startingWindow;
		private JPanel messageAndBox;
		
		//Bottom
		private JPanel bottom;
		private JPanel wrapperPanel;
		private JPanel problemSetPanel;
		private JButton loadDocSetButton;
		private JButton newDocSetButton;
		private JButton startButton;
		private JButton quitButton;
		private JPanel bottomButtons;
		
	//Listeners
	private ActionListener startListener;
	private ActionListener quitListener;
	private ActionListener loadDocSetListener;
	private ActionListener newDocSetListener;
	private FocusListener textBoxListener;
	
	/**
	 * Constructor
	 * @param main - Instance of GUIMain
	 */
	public StartingWindow(GUIMain main) {
		this.setSize(width, height);
		this.setResizable(false);
		this.setLocationRelativeTo(null);
		this.setVisible(false);
		this.main = main;
		this.setTitle("Anonymouth Start Screen");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	
		initGUI();
		initListeners();
		
		preProcessWindow = new PreProcessWindow(main);
		
		if (PropertiesUtil.getProbSet().equals("")) {
			startButton.setEnabled(false);
		}
	}

	/**
	 * Initializes the GUI (visible = false)
	 */
	private void initGUI() {		
		inputMessage = new JLabel("Please name your session:");
		
		startButton = new JButton("Start Anonymouth");
		quitButton = new JButton("Quit");
		
		textBox = new JTextField();
		textBox.setPreferredSize(new Dimension(300, 23));
		textBox.setText("Anonymouth");
		
		startingWindow = this;
		
		messageAndBox = new JPanel(new FlowLayout());
		messageAndBox.add(inputMessage);
		messageAndBox.add(textBox);
		messageAndBox.setMinimumSize(new Dimension(600, 50));
		messageAndBox.setBackground(new Color(180, 143, 186));
		messageAndBox.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 0));
		
		problemSetPanel = new JPanel(new BorderLayout());
		loadDocSetButton = new JButton("Load Document Set");
		newDocSetButton = new JButton("New Document Set");
		problemSetPanel.add(loadDocSetButton, BorderLayout.NORTH);
		problemSetPanel.add(newDocSetButton, BorderLayout.SOUTH);
		
		bottomButtons = new JPanel(new FlowLayout(0, 0, 0));
		bottomButtons.add(quitButton);
		bottomButtons.add(startButton);
		
		wrapperPanel = new JPanel(new BorderLayout());
		wrapperPanel.add(bottomButtons, BorderLayout.SOUTH);
		
		bottom = new JPanel(new BorderLayout());
		bottom.add(problemSetPanel, BorderLayout.WEST);
		bottom.add(wrapperPanel, BorderLayout.EAST);
		bottom.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 0));
		
		background = ImageLoader.getImage("session_name.png");
		completePanel = new JPanel(new BorderLayout()) {
			private static final long serialVersionUID = 1L;
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				
				Graphics2D g2d = (Graphics2D)g;
				g2d.drawImage(background, 0, 0, null);
			}
		};
		completePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
		completePanel.add(messageAndBox, BorderLayout.NORTH);
		completePanel.add(bottom, BorderLayout.SOUTH);
		this.add(completePanel);
	}
	
	/**
	 * Initializes the listeners
	 */
	private void initListeners() {
		startListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (int i = 100; i >= 0; i-= 2) {
					sessionName = textBox.getText();
					startingWindow.setOpacity((float)i/(float)100);
					
					try {
						Thread.sleep(3);
					} catch (InterruptedException ie) {
						ie.printStackTrace();
					}
				}
				
				startingWindow.setVisible(false);
				main.showMainGUI();
			}
		};
		startButton.addActionListener(startListener);
		
		quitListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		};
		quitButton.addActionListener(quitListener);
		
		textBoxListener = new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				textBox.setText("");
			}
			@Override
			public void focusLost(FocusEvent e) {}
		};
		textBox.addFocusListener(textBoxListener);
		
		loadDocSetListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Logger.logln(NAME+"'Load Problem Set' button clicked on the documents tab");

					PropertiesUtil.load.addChoosableFileFilter(new ExtFilter("XML files (*.xml)", "xml"));

					if (PropertiesUtil.prop.getProperty("recentProbSet") != null) {
						String absPath = PropertiesUtil.propFile.getAbsolutePath();
						String problemSetDir = absPath.substring(0, absPath.indexOf("anonymouth_prop")-1) + "\\problem_sets\\";
						PropertiesUtil.load.setCurrentDirectory(new File(problemSetDir));
						PropertiesUtil.load.setSelectedFile(new File(PropertiesUtil.prop.getProperty("recentProbSet")));
					} else {
						PropertiesUtil.load.setCurrentDirectory(new File(JSANConstants.JSAN_PROBLEMSETS_PREFIX));
					}

					int answer = PropertiesUtil.load.showDialog(startingWindow, "Load Problem Set");

					if (answer == JFileChooser.APPROVE_OPTION) {
						String path = PropertiesUtil.load.getSelectedFile().getAbsolutePath();

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
				preProcessWindow.showWindow();
			}
		};
		newDocSetButton.addActionListener(newDocSetListener);
	}
	
	/**
	 * Makes the prepared window visible
	 */
	public void showStartingWindow() {
		String probSet = PropertiesUtil.getProbSet();
		
		if (probSet.equals("")) {
			this.getRootPane().setDefaultButton(newDocSetButton);
		} else {
			this.getRootPane().setDefaultButton(startButton);
		}
		
		this.setVisible(true);
		
		if (probSet.equals("")) {
			newDocSetButton.requestFocusInWindow();
		} else {
			startButton.requestFocusInWindow();
		}
	}
}

/**
 * A simple splash screen displayed on start up to serve two main purposes:
 * 	1. Since Anonymouth on first load takes a while to load, we want to show some indication that it is doing so (like Photoshop)
 * 	2. Help give Anonymouth a professional sheen.
 * @author Marc Barrowclift
 *
 */
class SplashScreen extends JFrame {

	private static final long serialVersionUID = 1L;
	private final String SPLASH_NAME = "anonymouth_SPLASH.png";
	private int width = 520, height = 135;
	private Image splashImage;
	public JLabel progressLabel;
	public String newText;
	private JPanel panel;

	/**
	 * Constructor
	 * @param message
	 */
	public SplashScreen(String message) {
		this.setSize(width, height);
		this.setLocationRelativeTo(null);
		this.setResizable(false);
		this.setUndecorated(true);
		this.setVisible(false);
		
		splashImage = ImageLoader.getImage(SPLASH_NAME);
		panel = new JPanel(new BorderLayout()) {
			private static final long serialVersionUID = 1L;
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2d = (Graphics2D)g;
				g2d.drawImage(splashImage, 0, 0, null);
			}
		};
		
		progressLabel = new JLabel(message);
		progressLabel.setFont(new Font("Helvetica", Font.PLAIN, 18));
		progressLabel.setBorder(new EmptyBorder(5, 5, 0, 20));
		progressLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		progressLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
		
		panel.setLayout(new BorderLayout());
		panel.add(progressLabel, BorderLayout.SOUTH);
		this.add(panel);
	}

	/**
	 * Updates the message text with the new status
	 * XXX TODO XXX DOESN'T ACTUALLY WORK RIGHT NOW, Look into it later, probably concurancy problem.
	 * @param newText
	 */
	public void updateText(String newText) {
		progressLabel.setText(newText+"...");	
	}

	/**
	 * Displays the splash screen
	 */
	public void showSplashScreen() {
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
	 * Trashes the splash screen
	 */
	public void hideSplashScreen() {
		for (int i = 100; i >= 0; i-=2) {
			this.setOpacity((float)i/(float)100);
			
			try {
				Thread.sleep(3);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		this.setVisible(false);
	}
}