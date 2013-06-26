package edu.drexel.psal.anonymouth.gooie;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;

import edu.drexel.psal.JSANConstants;
import edu.drexel.psal.jstylo.generics.*;
import edu.drexel.psal.anonymouth.gooie.DriverPreProcessTabDocuments.ExtFilter;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;

/**
 * The main preferences window for Anonymouth. Stores under a different pull down menu depending on the system (if Mac, it's put under
 * Anonymouth > Preferences to keep with the Mac L&F, and if Windows/Linux it's put under Settings > Preferences.
 * @author Marc Barrowclift
 *
 */
public class GeneralSettingsFrame extends JFrame implements WindowListener {	

	private static final long serialVersionUID = 1L;
	private final String NAME = "( GeneralSettingsFrame ) - ";

	protected GUIMain main;
	public boolean panelsAreMade = false;
	protected JTabbedPane tabbedPane;
	protected GeneralSettingsFrame generalSettingsFrame;
	
	//General tab
	protected JPanel general;
	protected JCheckBox autoSave;
	protected JLabel autoSaveNote;
	protected JCheckBox warnQuit;
	protected JCheckBox showWarnings;
	protected JCheckBox translations;
	protected JLabel fontSize;
	protected JComboBox<String> fontSizes;
	protected String[] sizes = {"9", "10", "11", "12", "13", "14", "18"};
	protected int generalHeight;
	
	//Defaults tab
	protected JPanel defaults;
	protected JLabel defaultClassifier;
	protected JLabel defaultFeature;
	protected JLabel defaultProbSet;
	protected JComboBox<String> classComboBox;
	protected JComboBox<String> featComboBox;
	protected JButton selectProbSet;
	protected JTextField probSetTextPane;
	protected JScrollPane probSetScrollPane;
	protected int defaultsHeight;
	
	//Advanced tab
	protected JPanel advanced;
	protected JLabel maxFeatures;
	protected JSlider maxFeaturesSlider;
	protected JTextField maxFeaturesBox;
	protected JLabel numOfThreads;
	protected JTextField numOfThreadsBox;
	protected JSlider numOfThreadsSlider;
	protected JLabel numOfThreadsNote;
	protected JButton resetAll;
	protected int advancedHeight;
	
	//various variables
	private int prevFeatureValue;
	private int prevThreadValue;
	
	/**
	 * CONSTRUCTOR
	 * @param main - Instance of GUIMain
	 */
	public GeneralSettingsFrame(GUIMain main) {
		init(main);
		setVisible(false);
	}
	
	/**
	 * Readies the window.
	 * @param main
	 */
	private void init(final GUIMain main) {
		this.setTitle("Preferences");
		this.main = main;
		this.setIconImage(new ImageIcon(getClass().getResource(JSANConstants.JSAN_GRAPHICS_PREFIX+ThePresident.ANONYMOUTH_LOGO)).getImage());
		initTabs();

		generalSettingsFrame = this;
		
		this.add(tabbedPane);
		this.setSize(new Dimension(500, generalHeight));
		this.setResizable(false);
		this.setLocationRelativeTo(null); // makes it form in the center of the screen
	}
	
	/**
	 * Displays the window
	 */
	public void openWindow() {
		probSetTextPane.setText(PropertiesUtil.getProbSet());
		this.setLocationRelativeTo(null); // makes it form in the center of the screen
		this.setVisible(true);
	}
	
	/**
	 * Initializes all of the panels on the tree. Must be called before the tree is created, because when the tree
	 * is initialized it selects the first leaf in the tree which causes the panel to be shown.
	 */
	private void initTabs() {
		//==========================================================================================
		//================================ Tabs Location Panel =========================================
		//==========================================================================================
		tabbedPane = new JTabbedPane();
		general = new JPanel();
		defaults= new JPanel();
		advanced = new JPanel();

		MigLayout generalLayout = new MigLayout("fill");
		
		general.setLayout(generalLayout);
		{
			warnQuit = new JCheckBox();
			warnQuit.setText("Warn about unsaved changes upon exit");
			if (PropertiesUtil.getWarnQuit())
				warnQuit.setSelected(true);
			
			showWarnings = new JCheckBox();
			showWarnings.setText("Displays all warnings");
			if (PropertiesUtil.getWarnAll())
				showWarnings.setSelected(true);
			
			autoSave = new JCheckBox();
			autoSave.setText("Auto-Save anonymized documents upon exit");
			if (PropertiesUtil.getAutoSave()) {
				autoSave.setSelected(true);
				warnQuit.setEnabled(false);
			}
			
			translations = new JCheckBox();
			translations.setText("Send sentences to Microsoft Bing© for translation");
			if (PropertiesUtil.getDoTranslations())
				translations.setSelected(true);
			
			autoSaveNote = new JLabel("<html><center>Note: Will overwrite original document with changes.<br>THIS ACTION CANNOT BE UNDONE</center></html>");
			autoSaveNote.setForeground(Color.GRAY);
			
			fontSize = new JLabel ("Font Size: ");
			
			fontSizes = new JComboBox<String>();
			for (int i = 0; i < sizes.length; i++)
				fontSizes.addItem(sizes[i]);
			fontSizes.setSelectedItem(Integer.toString(PropertiesUtil.getFontSize()));
			
			general.add(autoSave, "wrap");
			general.add(autoSaveNote, "alignx 50%, wrap");
			general.add(warnQuit, "wrap");
			general.add(showWarnings, "wrap");
			general.add(translations, "wrap");
			general.add(fontSize, "split 2");
			general.add(fontSizes);
			
			generalHeight = 260;
		}
		
		MigLayout defaultLayout = new MigLayout();
		defaults.setLayout(defaultLayout);
		{
			defaultClassifier = new JLabel("Set Default Classifier:");
			classComboBox = new JComboBox<String>();
			for (int i = 0; i < main.classChoice.getItemCount(); i++)
				classComboBox.addItem(main.classChoice.getItemAt(i).toString());
			classComboBox.setSelectedItem(PropertiesUtil.getClassifier());

			defaultFeature = new JLabel("Set Default Feature:");
			featComboBox = new JComboBox<String>();
			for (int i = 0; i < main.featuresSetJComboBox.getItemCount(); i++)
				featComboBox.addItem(main.featuresSetJComboBox.getItemAt(i).toString());
			featComboBox.setSelectedItem(PropertiesUtil.getFeature());

			defaultProbSet = new JLabel("Set Default Problem Set:");
			selectProbSet = new JButton("Select");
			probSetTextPane = new JTextField();
			probSetTextPane.setEditable(false);
			probSetTextPane.setText(PropertiesUtil.getProbSet());
			probSetTextPane.setPreferredSize(new Dimension(420, 20));
			
			defaults.add(defaultClassifier, "wrap");
			defaults.add(classComboBox, "wrap");
			defaults.add(defaultFeature, "wrap");
			defaults.add(featComboBox, "wrap");
			defaults.add(defaultProbSet, "wrap");
			defaults.add(selectProbSet, "split 2");
			defaults.add(probSetTextPane, "wrap");
			
			defaultsHeight = 240;
		}
		
		MigLayout advancedLayout = new MigLayout();
		advanced.setLayout(advancedLayout);
		{
			maxFeatures = new JLabel("Maximum Features Used = ");
			maxFeaturesSlider = new JSlider();
			maxFeaturesSlider.setPreferredSize(new Dimension(300, 20));
			maxFeaturesSlider.setMajorTickSpacing(1);
			maxFeaturesSlider.setMinorTickSpacing(1);
			maxFeaturesSlider.setMaximum(1000);
			maxFeaturesSlider.setMinimum(200);
			maxFeaturesSlider.setSnapToTicks(true);
			maxFeaturesSlider.setValue(PropertiesUtil.getMaximumFeatures());
			maxFeaturesSlider.setOrientation(SwingConstants.HORIZONTAL);
			
			prevFeatureValue = PropertiesUtil.getMaximumFeatures();
			
			maxFeaturesBox = new JTextField();
			maxFeaturesBox.setPreferredSize(new Dimension(50, 20));
			maxFeaturesBox.setText(Integer.toString(PropertiesUtil.getMaximumFeatures()));
			
			numOfThreads = new JLabel("Number of Threads for Features Extraction = ");
			
			numOfThreadsBox = new JTextField();
			numOfThreadsBox.setPreferredSize(new Dimension(25, 20));
			numOfThreadsBox.setText(Integer.toString(PropertiesUtil.getThreadCount()));
			
			numOfThreadsSlider = new JSlider();
			numOfThreadsSlider.setPreferredSize(new Dimension(300, 20));
			numOfThreadsSlider.setMajorTickSpacing(1);
			numOfThreadsSlider.setMaximum(8);
			numOfThreadsSlider.setMinimum(1);
			numOfThreadsSlider.setMinorTickSpacing(1);
			numOfThreadsSlider.setOrientation(SwingConstants.HORIZONTAL);
			numOfThreadsSlider.setSnapToTicks(true);
			numOfThreadsSlider.setValue(PropertiesUtil.getThreadCount());
			
			numOfThreadsNote = new JLabel("Note: Expirimental, the current recommended number of threads is 4");
			numOfThreadsNote.setForeground(Color.GRAY);
			
			resetAll = new JButton("resetAll Preferences");
			resetAll.setToolTipText("resetAll all user preferences back to their default values");
			
			advanced.add(maxFeatures, "split");
			advanced.add(maxFeaturesBox, "wrap");
			advanced.add(maxFeaturesSlider, "alignx 50%, wrap");
			advanced.add(numOfThreads, "split");
			advanced.add(numOfThreadsBox, "wrap");
			advanced.add(numOfThreadsSlider, "alignx 50%, wrap");
			advanced.add(numOfThreadsNote, "alignx 50%, wrap");
			
			JSeparator test = new JSeparator(JSeparator.HORIZONTAL);
			test.setPreferredSize(new Dimension(484, 15));
			advanced.add(test, "gaptop 5, alignx 50%, wrap");
			advanced.add(resetAll, "gaptop 5, alignx 50%");
			
			advancedHeight = 300;
		}

		initListeners();
		tabbedPane.add("General", general);
		tabbedPane.add("Defaults", defaults);
		tabbedPane.add("Advanced", advanced);
	}
	
	/**
	 * Provides a nice animation when resizing the window
	 * @param newSize - The new height of the window
	 */
	public void resize(int newSize) {
		int curHeight = this.getHeight();
		
		//If the new height is larger we need to grow the window height
		if (newSize >= curHeight) {
			for (int h = curHeight; h <= newSize; h+=7) {
				this.setSize(new Dimension(500, h));
			}
		} else { //If the new height is smaller we need to shrink the window height
			for (int h = curHeight; h >= newSize; h-=7) {
				this.setSize(new Dimension(500, h));
			}
		}

		this.setSize(new Dimension(500, newSize)); //This is to ensure that our height is the desired height.
	}
	
	/**
	 * Initializes all the listeners needed for each tab of the preferences window.
	 */
	public void initListeners() {
		ActionListener classifierListener;
		ActionListener featureListener;
		ActionListener probSetListener;
		ActionListener autoSaveListener;
		ActionListener warnQuitListener;
		ChangeListener maxFeaturesListener;
		ChangeListener numOfThreadsListener;
		ActionListener resetListener;
		ActionListener translationsListener;
		ChangeListener tabbedPaneListener;
		ActionListener fontSizeListener;
		KeyListener maxFeaturesBoxListener;
		KeyListener numOfThreadsBoxListener;
		ActionListener showWarningsListener;
		
		fontSizeListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				PropertiesUtil.setFontSize(fontSizes.getSelectedItem().toString());
				main.normalFont = new Font("Ariel", Font.PLAIN, PropertiesUtil.getFontSize());
				main.getDocumentPane().setFont(main.normalFont);
			}
		};
		fontSizes.addActionListener(fontSizeListener);
		
		tabbedPaneListener = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (generalSettingsFrame == null)
					return;
				
				if (tabbedPane.getSelectedIndex() == 0) {
					resize(generalHeight);
					assertValues();
				} else if (tabbedPane.getSelectedIndex() == 1) {
					resize(defaultsHeight);
					assertValues();
				} else {
					resize(advancedHeight);
				}
			}
		};
		tabbedPane.addChangeListener(tabbedPaneListener);
		
		classifierListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				PropertiesUtil.setClassifier(classComboBox.getSelectedItem().toString());
			}
		};
		classComboBox.addActionListener(classifierListener);
		
		featureListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				PropertiesUtil.setFeature(featComboBox.getSelectedItem().toString());
			}
		};
		featComboBox.addActionListener(featureListener);
		
		probSetListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					Logger.logln(NAME+"'Select' Problem Set button clicked on the Preferences window");

					int answer = 0;
					
					PropertiesUtil.load.addChoosableFileFilter(new ExtFilter("XML files (*.xml)", "xml"));
					if (PropertiesUtil.getProbSet() != null) {
						String absPath = PropertiesUtil.propFile.getAbsolutePath();
						String problemSetDir = absPath.substring(0, absPath.indexOf("anonymouth_prop")-1) + "\\problem_sets\\";
						PropertiesUtil.load.setCurrentDirectory(new File(problemSetDir));
						PropertiesUtil.load.setSelectedFile(new File(PropertiesUtil.prop.getProperty("recentProbSet")));
					}
					
					answer = PropertiesUtil.load.showDialog(main, "Load Problem Set");

					if (answer == JFileChooser.APPROVE_OPTION) {
						String path = PropertiesUtil.load.getSelectedFile().getAbsolutePath();
						PropertiesUtil.setProbSet(path);
						
						probSetTextPane.setText(path);
					} else {
						Logger.logln(NAME+"Set default problem set canceled");
					}
				} catch (NullPointerException arg)
				{
					arg.printStackTrace();
				}
			}
		};
		selectProbSet.addActionListener(probSetListener);
		
		autoSaveListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {			
				if (autoSave.isSelected()) {
					PropertiesUtil.setAutoSave(true);
					warnQuit.setSelected(false);
					PropertiesUtil.setWarnQuit(false);
					warnQuit.setEnabled(false);
					Logger.logln(NAME+"Auto-save checkbox checked");
				} else {
					PropertiesUtil.setAutoSave(false);
					warnQuit.setEnabled(true);
					Logger.logln(NAME+"Auto-save checkbox unchecked");
				}
			}
		};
		autoSave.addActionListener(autoSaveListener);
		
		warnQuitListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (warnQuit.isSelected()) {
					PropertiesUtil.setWarnQuit(true);
					Logger.logln(NAME+"Warn on quit checkbox checked");
				} else {
					PropertiesUtil.setWarnQuit(false);
					Logger.logln(NAME+"Warn on quit checkbox unchecked");
				}
			}
		};
		warnQuit.addActionListener(warnQuitListener);
		
		translationsListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Logger.logln(NAME+"Translations checkbox clicked");
				
				if (translations.isSelected()) {
					if (GUIMain.processed)
						main.resetTranslator.setEnabled(true);
					PropertiesUtil.setDoTranslations(true);
					
					if (BackendInterface.processed) {
						int answer = JOptionPane.showOptionDialog(null,
								"Being translating now?",
								"Begin Translations",
								JOptionPane.YES_NO_OPTION,
								JOptionPane.QUESTION_MESSAGE,
								null, null, null);
						
						if (answer == JOptionPane.YES_OPTION) {
							GUIMain.GUITranslator.load(DriverEditor.taggedDoc.getTaggedSentences());
							DriverTranslationsTab.showTranslations(DriverEditor.taggedDoc.getSentenceNumber(DriverEditor.sentToTranslate));
						}
					} else {
						main.notTranslated.setText("Please process your document to recieve translation suggestions.");
						main.translationsHolderPanel.add(main.notTranslated, "");
					}
				} else {
					main.resetTranslator.setEnabled(false);
					GUIMain.GUITranslator.reset();
					PropertiesUtil.setDoTranslations(false);
					main.notTranslated.setText("You have turned translations off.");
					main.translationsHolderPanel.add(main.notTranslated, "");
				}
			}
		};
		translations.addActionListener(translationsListener);
		
		maxFeaturesListener = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				PropertiesUtil.setMaximumFeatures(maxFeaturesSlider.getValue());
				maxFeaturesBox.setText(Integer.toString(PropertiesUtil.getMaximumFeatures()));
				prevFeatureValue = maxFeaturesSlider.getValue();
			}	
		};
		maxFeaturesSlider.addChangeListener(maxFeaturesListener);
		
		numOfThreadsListener = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				PropertiesUtil.setThreadCount(numOfThreadsSlider.getValue());
				numOfThreadsBox.setText(Integer.toString(PropertiesUtil.getThreadCount()));
				prevThreadValue = numOfThreadsSlider.getValue();
			}
		};
		numOfThreadsSlider.addChangeListener(numOfThreadsListener);
		
		resetListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Logger.logln(NAME+"resetAll button clicked");
				
				int answer = 0;
				
				answer = JOptionPane.showConfirmDialog(null,
						"Are you sure you want to resetAll all preferences?\nThis will override your changes.",
						"resetAll Preferences",
						JOptionPane.WARNING_MESSAGE,
						JOptionPane.YES_NO_CANCEL_OPTION);
				
				if (answer == 0) {
					try {
						Logger.logln(NAME+"resetAll progressing...");
						PropertiesUtil.reset();
						numOfThreadsSlider.setValue(PropertiesUtil.getThreadCount());
						maxFeaturesSlider.setValue(PropertiesUtil.getMaximumFeatures());
						warnQuit.setSelected(PropertiesUtil.getWarnQuit());
						autoSave.setSelected(PropertiesUtil.getAutoSave());
						probSetTextPane.setText(PropertiesUtil.getProbSet());
						featComboBox.setSelectedItem(PropertiesUtil.getFeature());
						classComboBox.setSelectedItem(PropertiesUtil.getClassifier());
						Logger.logln(NAME+"resetAll complete");
					} catch (Exception e) {
						Logger.logln(NAME+"Error occurred during resetAll");
					}
				} else {
					Logger.logln(NAME+"User cancelled resetAll");
				}
			}
		};
		resetAll.addActionListener(resetListener);
		
		maxFeaturesBoxListener = new KeyListener() {			
			@Override
			public void keyTyped(KeyEvent e) {}
			@Override
			public void keyPressed(KeyEvent e) {}
			
			@Override
			public void keyReleased(KeyEvent e) {
				int number = -1;
				try {
					if (maxFeaturesBox.getText().equals("")) {
						maxFeaturesBox.setText("");
					} else {
						number = Integer.parseInt(maxFeaturesBox.getText());
						
						if (number > 1000) {
							maxFeaturesBox.setText(Integer.toString(prevFeatureValue));
							number = prevFeatureValue;
						} else {
							maxFeaturesBox.setText(Integer.toString(number));
						}
					}
				} catch (Exception e1) {
					maxFeaturesBox.setText(Integer.toString(prevFeatureValue));
				}
				
				if (number != -1) {
					prevFeatureValue = number;
				}
				
				if (prevFeatureValue >= 200) {
					PropertiesUtil.setMaximumFeatures(prevFeatureValue);
					maxFeaturesSlider.setValue(prevFeatureValue);
				}
			}
		};
		maxFeaturesBox.addKeyListener(maxFeaturesBoxListener);
		
		numOfThreadsBoxListener = new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {}
			@Override
			public void keyPressed(KeyEvent e) {}
			
			@Override
			public void keyReleased(KeyEvent e) {
				int number = -1;
				try {
					if (numOfThreadsBox.getText().equals("")) {
						numOfThreadsBox.setText("");
					} else {
						number = Integer.parseInt(numOfThreadsBox.getText());
						
						if (number > 8) {
							numOfThreadsBox.setText(Integer.toString(prevThreadValue));
							number = prevThreadValue;
						} else {
							numOfThreadsBox.setText(Integer.toString(number));
						}
					}
				} catch (Exception e1) {
					numOfThreadsBox.setText(Integer.toString(prevThreadValue));
				}
				
				if (number != -1) {
					prevThreadValue = number;
				}
				
				if (prevThreadValue >= 1) {
					PropertiesUtil.setMaximumFeatures(prevThreadValue);
					numOfThreadsSlider.setValue(prevThreadValue);
				}
			}
		};
		numOfThreadsBox.addKeyListener(numOfThreadsBoxListener);
		
		showWarningsListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (showWarnings.isSelected()) {
					PropertiesUtil.setWarnAll(true);
					Logger.logln(NAME+"Show all warnings checkbox checked");
				} else {
					PropertiesUtil.setWarnAll(false);
					Logger.logln(NAME+"Show all warnings checkbox unchecked");
				}
			}
		};
		showWarnings.addActionListener(showWarningsListener);
	}

	private void assertValues() {
		int feat = PropertiesUtil.getMaximumFeatures();
		int thread = PropertiesUtil.getThreadCount();
		if (feat < 200 || feat > 1000)
			PropertiesUtil.setMaximumFeatures(PropertiesUtil.defaultFeatures);
		if (thread < 1 || thread > 8)
			PropertiesUtil.setThreadCount(PropertiesUtil.defaultThreads);
	}
	
	/**
	 * We want to be absolutely sure that when Preferences is closed that the set values are within
	 * acceptable boundaries, and if they aren't change them back to their defaults.
	 */
	@Override
	public void windowClosing(WindowEvent e) {
		assertValues();
	}
	
	@Override
	public void windowOpened(WindowEvent e) {}
	@Override
	public void windowClosed(WindowEvent e) {}
	@Override
	public void windowIconified(WindowEvent e) {}
	@Override
	public void windowDeiconified(WindowEvent e) {}
	@Override
	public void windowActivated(WindowEvent e) {}
	@Override
	public void windowDeactivated(WindowEvent e) {}
}