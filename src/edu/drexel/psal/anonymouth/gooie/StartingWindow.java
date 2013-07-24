package edu.drexel.psal.anonymouth.gooie;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;

import javax.swing.BorderFactory;
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
	public String sessionName;
	private Image background;
	private int width = 520, height = 155;
	private FileDialog load;
	
	//Swing Components
	private JPanel completePanel;
		//Top
		private JLabel inputMessage;
		private JTextField textBox;
		private StartingWindow startingWindow;
		private JPanel messageAndBox;
		
		//Middle
		private JPanel middlePanel;
		private JLabel middleLabel;
		private JButton startButton;
		
		//Bottom
		private JButton quitButton;
		private JButton loadDocSetButton;
		protected JButton newDocSetButton;
		private JPanel bottomButtons;
		
	//Listeners
	private ActionListener startListener;
	private ActionListener quitListener;
	private ActionListener newDocSetListener;
	protected ActionListener loadDocSetListener;
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
		this.setTitle("Anonymouth Start Window");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		load = new FileDialog(this);
		load.setModalityType(ModalityType.DOCUMENT_MODAL);
	
		initGUI();
		initListeners();
		
		if (PropertiesUtil.getProbSet().equals("")) {
			startButton.setEnabled(false);
		}
	}
	
	protected void setReadyToStart(boolean ready) {
		if (ready) {
			if (!PropertiesUtil.getProbSet().equals(""))
				middleLabel.setText("Start with document set \"" + new File(PropertiesUtil.getProbSet()).getName() + "\"");
			else
				middleLabel.setText("Start with completed document set");
			
			middleLabel.setForeground(Color.BLACK);
			startButton.setEnabled(true);
			this.getRootPane().setDefaultButton(startButton);
			startButton.requestFocusInWindow();
		} else {
			middleLabel.setText("Please finish your document set to start");
			middleLabel.setForeground(Color.LIGHT_GRAY);
			startButton.setEnabled(false);
			this.getRootPane().setDefaultButton(newDocSetButton);
			newDocSetButton.requestFocusInWindow();
		}
	}

	/**
	 * Initializes the GUI (visible = false)
	 */
	private void initGUI() {		
		inputMessage = new JLabel("Please name your session:");
		
		quitButton = new JButton("Quit");
		loadDocSetButton = new JButton("Load Document Set");
		newDocSetButton = new JButton("New Document Set");
		
		textBox = new JTextField();
		textBox.setPreferredSize(new Dimension(300, 23));
		textBox.setText("Anonymouth");
		
		startingWindow = this;
		
		messageAndBox = new JPanel(new FlowLayout());
		messageAndBox.add(inputMessage);
		messageAndBox.add(textBox);
		messageAndBox.setMinimumSize(new Dimension(600, 50));
		messageAndBox.setBackground(new Color(180, 143, 186));
		messageAndBox.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 0));
		
		middlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
		middleLabel = new JLabel();
		middleLabel.setFont(new Font("Helvetica", Font.PLAIN, 16));
		startButton = new JButton("Start");
		
		if (ThePresident.canDoQuickStart) {
			middleLabel.setText("Resume with previously used document set");
		} else {
			middleLabel.setText("No previous document set found");
			middleLabel.setForeground(Color.LIGHT_GRAY);
			startButton.setEnabled(false);
		}
		
		middlePanel.add(middleLabel);
		middlePanel.add(startButton);
		
		JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		bottomPanel.add(quitButton);
		bottomPanel.add(loadDocSetButton);
		bottomPanel.add(newDocSetButton);
		bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
		
		bottomButtons = new JPanel(new BorderLayout(0, 0));
		bottomButtons.add(middlePanel, BorderLayout.NORTH);
		bottomButtons.add(bottomPanel, BorderLayout.SOUTH);
		bottomButtons.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
		
		background = ImageLoader.getImage("session_name.png");
		completePanel = new JPanel(new BorderLayout()) {
			private static final long serialVersionUID = 1L;
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				
				Graphics2D g2d = (Graphics2D)g;
				g2d.drawImage(background, 0, -15, null);
			}
		};
		completePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
		completePanel.add(messageAndBox, BorderLayout.NORTH);
		completePanel.add(bottomButtons, BorderLayout.SOUTH);
		this.add(completePanel);
	}
	
	/**
	 * Initializes the listeners
	 */
	private void initListeners() {
		startListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
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
			public void focusLost(FocusEvent e) {
				if (textBox.getText().equals("")) {
					textBox.setText("Anonymouth");
				}
			}
		};
		textBox.addFocusListener(textBoxListener);
		
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
				String sessionName = textBox.getText();
				sessionName = sessionName.replaceAll("['.?!()<>#\\\\/|\\[\\]{}*\":;`~&^%$@+=,]", "");
				String tempName = sessionName.replaceAll(" ", "_");
				if (tempName != null)
					sessionName = tempName;
				ThePresident.sessionName = sessionName;
				
				main.preProcessWindow.showWindow();
			}
		};
		newDocSetButton.addActionListener(newDocSetListener);
	}
	
	/**
	 * Makes the prepared window visible
	 */
	public void showStartingWindow() {
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
	 * XXX TODO XXX DOESN'T ACTUALLY WORK RIGHT NOW, Look into it later, probably concurrency problem.
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