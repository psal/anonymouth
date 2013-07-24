package edu.drexel.psal.anonymouth.gooie;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

import edu.drexel.psal.ANONConstants;
import edu.drexel.psal.JSANConstants;
import edu.drexel.psal.jstylo.generics.*;
import edu.drexel.psal.jstylo.generics.Logger.LogOut;
import edu.drexel.psal.anonymouth.engine.Clipboard;
import edu.drexel.psal.anonymouth.engine.VersionControl;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.StyledDocument;

import net.miginfocom.swing.MigLayout;

import com.jgaap.generics.Document;

import edu.drexel.psal.jstylo.analyzers.WekaAnalyzer;

import com.apple.eawt.AppEvent.FullScreenEvent;
import com.apple.eawt.FullScreenListener;


/**
 * This code was edited or generated using CloudGarden's Jigloo
 * SWT/Swing GUI Builder, which is free for non-commercial
 * use. If Jigloo is being used commercially (ie, by a corporation,
 * company or business for any purpose whatever) then you
 * should purchase a license for each developer using Jigloo.
 * Please visit www.cloudgarden.com for details.
 * Use of Jigloo implies acceptance of these licensing terms.
 * A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
 * THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
 * LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
 */
/**
 * JStylo main GUI class.
 * 
 * @author Andrew W.E. McDonald
 * @author Marc Barrowclift
 */
//This is a comment from Joe Muoio to see if he can commit changes.
public class GUIMain extends javax.swing.JFrame {

	private static final long serialVersionUID = 1L;
	private final String NAME = "( "+this.getClass().getSimpleName()+" ) - ";

	{
		@SuppressWarnings("unused")
		boolean lookAndFeelSet = false;
		//Set Look & Feel 
		// -- start patch submitted by Sebastian Pipping
		// Try best natives looks explicitly
		// (or we might still end up with Metal but GTK+ on Linux)
		final String[] themeClassesToTry = {
			"com.sun.java.swing.plaf.windows.WindowsLookAndFeel",
			"com.sun.java.swing.plaf.gtk.GTKLookAndFeel",
		};
		
		for (String themeClassName : themeClassesToTry) {
			try {
				UIManager.setLookAndFeel(themeClassName);
				lookAndFeelSet = true; // added by AweM
			} catch (Exception e) {
				lookAndFeelSet = false; // added by AweM
				
			}
		}
		// -- end patch
		if(lookAndFeelSet = false){
			try { 
				javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

	// main instance
	public static GUIMain inst;
	protected JPanel mainPanel;

	// ------------------------

	// data
	protected List<CumulativeFeatureDriver> presetCFDs;
	protected WekaInstancesBuilder wib;
	protected WekaAnalyzer wad;
	protected Thread analysisThread;
	protected List<String> results;

	protected static PreferencesWindow preferencesWindow;

	protected Font defaultLabelFont = new Font("Verdana",0,16);
	protected static int cellPadding = 5;

	protected final Color ready = new Color(0,255,128);
	protected final Color notReady = new Color(255,102,102);
	protected final Color blue = new Color(136,166,233,200);
	//protected final Color blue = new Color((float)0.62f, (float)0.71, (float)0.93); //without alpha

	// tabs
	protected JTabbedPane mainJTabbedPane;
	protected JPanel docsTab;
	protected JPanel featuresTab;
	protected JPanel classTab;
	protected JPanel editorTab;
	
	public PreProcessWindow preProcessWindow;
	public PreProcessAdvancedWindow ppAdvancedWindow;

	// Classifiers tab
	protected JTextField classAvClassArgsJTextField;
	protected JLabel classAvClassArgsJLabel;
	protected JLabel classAvClassJLabel;
	protected JButton classAddJButton;

	protected JTextField classSelClassArgsJTextField;
	protected JLabel classSelClassArgsJLabel;
	protected JScrollPane classSelClassJScrollPane;
	protected JScrollPane classTreeScrollPane;
	protected JScrollPane classDescJScrollPane;
	protected JTextPane classDescJTextPane;
	protected JLabel classDescJLabel;
	protected JButton classBackJButton;
	protected JButton classNextJButton;
	protected JLabel classSelClassJLabel;
	protected JButton classRemoveJButton;
	protected JButton classAboutJButton;

	// Editor tab
	protected JScrollPane theEditorScrollPane;
	protected JTable suggestionTable;
	protected JPanel editorRowTwoButtonBufferPanel;
	protected JPanel buttonBufferJPanel;
	protected JPanel editorBottomRowButtonPanel;
	protected JPanel editorTopRowButtonsPanel;
	protected JPanel editorButtonJPanel;
	protected JPanel editorInteractionWestPanel;
	protected JPanel editorInteractionJPanel;
	protected JPanel jPanel2;
	protected JPanel dummyPanelUpdatorLeftSide;
	protected JPanel elementsToAddBoxLabelJPanel;
	protected JPanel suggestionBoxLabelJPanel;
	protected JPanel jPanel1;
	protected JPanel valueLabelJPanel;
	protected JPanel valueBoxPanel;
	protected JPanel updaterJPanel;
	//-------------- HELP TAB PANE STUFF ---------
	protected JTabbedPane leftTabPane;

	protected JPanel suggestionsPanel;
	protected JPanel elementsPanel;
	protected JPanel elementsToAddPanel;
	protected JLabel elementsToAddLabel;
	protected JList<String> elementsToAddPane;
	protected JScrollPane elementsToAddScrollPane;
	protected JPanel elementsToRemovePanel;
	protected JLabel elementsToRemoveLabel;
	protected DefaultTableModel elementsToRemoveModel;
	protected ElementsTable elementsToRemoveTable;
	protected JScrollPane elementsToRemoveScrollPane;
	protected DefaultListModel<String> elementsToAdd;
	protected DefaultListModel<String> elementsToRemove;
	protected JButton clearAddHighlights;
	protected JButton clearRemoveHighlights;

	protected JPanel translationsPanel;
	protected JLabel translationsLabel;
	protected JButton resetTranslator;
	protected JButton stopTranslations;
	protected JButton startTranslations;
	protected ScrollablePanel translationsHolderPanel;
	protected JScrollPane translationsScrollPane;
	protected JPanel progressPanel;
	protected JLabel translationsProgressLabel;
	protected JProgressBar translationsProgressBar;
	protected JTextPane notTranslated;

	protected JPanel informationPanel;
	protected JLabel sentenceEditorLabel;
	protected JLabel documentViewerLabel;
	protected JLabel resultsLabel;
	protected JTextPane descriptionPane;

	protected JPanel instructionsPanel;
	protected JLabel instructionsLabel;
	protected JTextPane instructionsPane;
	protected JScrollPane instructionsScrollPane;
	protected JPanel synonymsPanel;
	protected JLabel synonymsLabel;
	protected JTextPane synonymsPane;
	protected JScrollPane synonymsScrollPane;
	//--------------------------------------------

	//--------------- Editor Tab Pane stuff ----------------------
	protected JTabbedPane topTabPane;
	protected JPanel documentsPanel;
	protected JPanel sentenceAndDocumentPanel;
	protected JPanel sentenceLabelPanel;

	protected JPanel sentenceEditingPanel;
	protected JPanel documentPanel;
	protected StyledDocument theDocument;

	protected JButton removeWordsButton;
	protected JButton shuffleButton;
	protected JButton SaveChangesButton;
	protected JButton copyToSentenceButton;
	protected JButton restoreSentenceButton;
	protected JLabel documentLabel;
	private JTextPane documentPane;
	protected JScrollPane documentScrollPane;

	protected JPanel sentenceAndSentenceLabelPanel;
	protected JLabel translationsBoxLabel;
	protected JScrollPane translationPane;
	protected JTextPane translationEditPane;

	private boolean tabMade = false;
	protected int resultsMaxIndex;
	protected String chosenAuthor;

	protected JButton saveButton;
	protected JButton processButton;
	//---------------------------------------------------------------------
	protected JTabbedPane bottomTabPane;
	protected JPanel resultsOptionsPanel;
	protected JPanel resultsMainPanel;
	protected JScrollPane resultsScrollPane;
	protected JTextArea displayTextArea;
	protected JPanel resultsBoxPanel_InnerBottomPanel;
	protected JTable resultsTable;
	protected DefaultTableModel resultsTableModel;
	protected JScrollPane resultsTablePane;
	protected JPanel resultsBoxPanel;
	protected JLabel resultsTableLabel;
	protected JPanel resultsTableLabelPanel;
	protected JPanel resultsBoxAndResultsLabelPanel;
	//---------------------------------------------------------------------

	protected JTabbedPane rightTabPane;
	protected JPanel anonymityPanel;
	protected JLabel anonymityLabel;
	public AnonymityDrawingPanel anonymityDrawingPanel;
	protected JLabel anonymityDescription;

	//--------------------------------------------------------------------

	protected JPanel featuresPanel;
	protected JLabel legendLabel;
	protected JPanel legendPanel;

	protected JPanel topPanel;
	protected JButton refreshButton;
	protected JPanel secondPanel;

	//----------------------------------------------------------------------

	protected JPanel editorInfoJPanel;
	protected JScrollPane editorInteractionScrollPane;
	protected JScrollPane EditorInfoScrollPane;
	protected JTabbedPane editTP;

	protected JScrollPane wordsToAddPane;
	protected JTextField searchInputBox;
	protected JLabel highlightLabel;
	protected JPanel jPanel_IL3;
	protected JButton clearHighlightingButton;
	protected JLabel featureNameLabel;
	protected JLabel targetValueLabel;
	protected JLabel presentValueLabel;
	protected JTextField targetValueField;
	protected JTextField presentValueField;
	protected JLabel suggestionListLabel;
	protected JButton verboseButton;
	protected JScrollPane suggestionListPane;

	// Analysis tab
	protected JCheckBox analysisOutputAccByClassJCheckBox;
	protected JCheckBox analysisOutputConfusionMatrixJCheckBox;
	protected ButtonGroup analysisTypeButtonGroup;

	protected JMenuBar menuBar;
	protected JMenuItem settingsGeneralMenuItem;
	protected JMenuItem fileSaveProblemSetMenuItem;
	protected JMenuItem fileLoadProblemSetMenuItem;
	protected JMenuItem fileSaveTestDocMenuItem;
	protected JMenuItem fileSaveAsTestDocMenuItem;
	protected JMenuItem helpAboutMenuItem;
	protected JMenuItem helpSuggestionsMenuItem;
	protected JMenuItem helpClustersMenuItem;
	protected JMenuItem viewMenuItem;
	protected JMenuItem viewClustersMenuItem;
	public static JMenuItem viewEnterFullScreenMenuItem;
	protected JMenuItem helpMenu;
	protected JMenuItem fileMenu;
	protected JMenuItem editMenu;
	public JMenuItem editUndoMenuItem;
	public JMenuItem editRedoMenuItem;
	//	protected JMenuItem filePrintMenuItem;

	// random useful variables
	protected static Border rlborder = BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(), BorderFactory.createLoweredBevelBorder());
	protected static Font titleFont = new Font("Ariel", Font.BOLD, 12);
	protected static String titleHeight = "25";
	protected static Boolean saved = true;
	protected static Boolean processed = false;

	// used for translation of sentences
	protected static Translator GUITranslator;

	// not yet used, may be used to minimize the document, features, or classifiers part of the preprocess panel
	protected boolean docPPIsShowing = true;
	protected boolean featPPIsShowing = true;
	protected boolean classPPIsShowing = true;
	protected ClustersWindow clustersWindow;
	protected FAQWindow suggestionsWindow;
	protected ClustersTutorial clustersTutorial;
	protected VersionControl versionControl;
	protected ResultsWindow resultsWindow;
	protected RightClickMenu rightClickMenu;
	protected Clipboard clipboard;
	protected StartingWindows startingWindows;
	protected static Runnable mainThread;
	private SplashScreen splashScreen;
	protected JPanel anonymityHoldingPanel;
	protected JScrollPane anonymityScrollPane;
	protected Font normalFont;
	protected Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();

	private int resultsHeight;
	protected Map<Integer, ArrayList<int[]>> highlights = new HashMap<Integer, ArrayList<int[]>>();

	//used mostly for loading the main document without having to alter the main.ps.testDocAt(0) directly
	Document mainDocPreview;

	/**
	 * Auto-generated main method to display this JFrame
	 */
	public static void startGooie(final SplashScreen splash) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				mainThread = this;
				Logger.initLogFile();
				
				inst = new GUIMain(splash);
				GUITranslator = new Translator(inst);

				WindowListener exitListener = new WindowListener() {
					@Override
					public void windowClosing(WindowEvent e) {
						if (PropertiesUtil.getWarnQuit() && !saved) {
							inst.toFront();
							inst.requestFocus();
							int confirm = JOptionPane.showOptionDialog(GUIMain.inst,
									"Close Application?\nYou will lose all unsaved changes.",
									"Unsaved Changes Warning",
									JOptionPane.YES_NO_OPTION,
									JOptionPane.QUESTION_MESSAGE,
									UIManager.getIcon("OptionPane.warningIcon"), null, null);
							if (confirm == 0) {
								System.exit(0);
							}
						} else if (PropertiesUtil.getAutoSave()) {
							DriverEditor.save(inst);
							System.exit(0);
						} else {
							System.exit(0);
						}
					}
					@Override
					public void windowActivated(WindowEvent arg0) {}
					@Override
					public void windowClosed(WindowEvent arg0) {}
					@Override
					public void windowDeactivated(WindowEvent arg0) {}
					@Override
					public void windowDeiconified(WindowEvent arg0) {}
					@Override
					public void windowIconified(WindowEvent arg0) {}
					@Override
					public void windowOpened(WindowEvent arg0) {}
				};

				if (ANONConstants.IS_MAC) {
					enableOSXFullscreen(inst);
				}
				
				ToolTipManager.sharedInstance().setDismissDelay(20000); //To keep tooltips from disappearing so fast
				inst.addWindowListener(exitListener);
				inst.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
				inst.setLocationRelativeTo(null);
			}
		});
	}

	/**
	 * (Thanks to Dyorgio at StackOverflow for the code)
	 * If the user is on OS X, we will allow them to enter full screen in Anonymouth using OS X's native full screen functionality.
	 * As of right now it doesn't actually resize the components that much and doesn't add much to the application, but the structure's
	 * there to allow someone to come in and optimize Anonymouth when in full screen (not to mention just having the functionality makes
	 * it seem more like a native OS X application).
	 * 
	 * This enables full screen for the particular window and also adds a full screen listener so that we may chance components as needed
	 * depending on what state we are in.
	 * @param window
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	public static void enableOSXFullscreen(Window window) {
		try {
			Class util = Class.forName("com.apple.eawt.FullScreenUtilities");
			Class params[] = new Class[]{Window.class, Boolean.TYPE};
			Method method = util.getMethod("setWindowCanFullScreen", params);
			method.invoke(util, window, true);

			com.apple.eawt.FullScreenUtilities.addFullScreenListenerTo(window, new FullScreenListener () {
				@Override
				public void windowEnteredFullScreen(FullScreenEvent arg0) {
					GUIMain.viewEnterFullScreenMenuItem.setText("Exit Full Screen");
				}
				@Override
				public void windowEnteringFullScreen(FullScreenEvent arg0) {}
				@Override
				public void windowExitedFullScreen(FullScreenEvent arg0) {
					GUIMain.viewEnterFullScreenMenuItem.setText("Enter Full Screen");
				}
				@Override
				public void windowExitingFullScreen(FullScreenEvent arg0) {}
			});
		} catch (ClassNotFoundException e1) {
			Logger.logln("( GUIMain ) - Failed initializing Anonymouth for full-screen", LogOut.STDERR);
		} catch (Exception e) {
			Logger.logln("( GUIMain ) - Failed initializing Anonymouth for full-screen", LogOut.STDERR);
		}
	}

	public GUIMain(SplashScreen splash) {
		super();	
		splashScreen = splash;
		if (!splashScreen.isVisible()) {
			splashScreen.showSplashScreen();
		}
		startingWindows = new StartingWindows(this);
		
		initData();
		initGUI();

		splashScreen.hideSplashScreen();
		startingWindows.showStartingWindow();
	}
	
	public GUIMain() {
		this(new SplashScreen("Beginning Anonymouth Class Test"));
	}
	
	public void showMainGUI() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				setExtendedState(MAXIMIZED_BOTH);
				GUIMain.inst.setSize(new Dimension((int)(screensize.width*.75), (int)(screensize.height*.75)));
				GUIMain.inst.setLocationRelativeTo(null);
				GUIMain.inst.setMinimumSize(new Dimension(800, 578));
				GUIMain.inst.setTitle("Anonymouth");
				GUIMain.inst.setIconImage(new ImageIcon(getClass().getResource(JSANConstants.JSAN_GRAPHICS_PREFIX+ThePresident.ANONYMOUTH_LOGO)).getImage());
				GUIMain.inst.setVisible(true);
			}
		});
	}

	private void initData() {
		ProblemSet.setDummyAuthor(ANONConstants.DUMMY_NAME);
		wib = new WekaInstancesBuilder(true);
		results = new ArrayList<String>();

		// properties file -----------------------------------
		BufferedReader propReader = null;

		if (!PropertiesUtil.propFile.exists()) {
			try {
				PropertiesUtil.propFile.getParentFile().mkdirs();
				PropertiesUtil.propFile.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

		try {
			propReader = new BufferedReader (new FileReader(PropertiesUtil.propFileName));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		try {
			PropertiesUtil.prop.load(propReader);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates the menu bar used by Anonymouth
	 */
	private void initMenuBar() {
		menuBar = new JMenuBar();

		fileMenu = new JMenu("File");
		fileSaveProblemSetMenuItem = new JMenuItem("Save Document Set");
		fileLoadProblemSetMenuItem = new JMenuItem("Load Document Set");
		fileSaveTestDocMenuItem = new JMenuItem("Save");
		fileSaveAsTestDocMenuItem = new JMenuItem("Save As...");

		fileMenu.add(fileSaveProblemSetMenuItem);
		fileMenu.add(fileLoadProblemSetMenuItem);
		fileMenu.add(new JSeparator());
		fileMenu.add(fileSaveTestDocMenuItem);
		fileMenu.add(fileSaveAsTestDocMenuItem);

		menuBar.add(fileMenu);

		editMenu = new JMenu("Edit");
		editUndoMenuItem = new JMenuItem("Undo");
		editUndoMenuItem.setEnabled(false);
		editMenu.add(editUndoMenuItem);
		editRedoMenuItem = new JMenuItem("Redo");
		editRedoMenuItem.setEnabled(false);
		editMenu.add(editRedoMenuItem);
		clipboard = new Clipboard(this, editMenu);
		menuBar.add(editMenu);

		viewMenuItem = new JMenu("View");
		viewClustersMenuItem = new JMenuItem("Clusters");
		viewMenuItem.add(viewClustersMenuItem);

		menuBar.add(viewMenuItem);

		if (ANONConstants.IS_MAC) {
			fileSaveAsTestDocMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,InputEvent.SHIFT_DOWN_MASK | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
			fileSaveTestDocMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
			editUndoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
			editRedoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.SHIFT_DOWN_MASK | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

			viewMenuItem.add(new JSeparator());
			viewEnterFullScreenMenuItem = new JMenuItem("Enter Full Screen");
			viewMenuItem.add(viewEnterFullScreenMenuItem);
			viewEnterFullScreenMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		} else {
			fileSaveAsTestDocMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK));
			fileSaveTestDocMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
			editUndoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK));
			editRedoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK));
		}

		if (!ANONConstants.IS_MAC) {
			JMenu settingsMenu = new JMenu("Settings");
			settingsGeneralMenuItem = new JMenuItem("Preferences");
			settingsMenu.add(settingsGeneralMenuItem);
			menuBar.add(settingsMenu);
		}

		helpMenu = new JMenu("Help");
		helpAboutMenuItem = new JMenuItem("About Anonymouth");
		helpClustersMenuItem = new JMenuItem("Clusters Tutorial");
		if (!ANONConstants.IS_MAC) {
			helpMenu.add(helpAboutMenuItem);
			helpMenu.add(new JSeparator());
		}
		helpSuggestionsMenuItem = new JMenuItem("FAQ");
		helpMenu.add(helpSuggestionsMenuItem);
		helpMenu.add(new JSeparator());
		helpMenu.add(helpClustersMenuItem);

		menuBar.add(helpMenu);

		this.setJMenuBar(menuBar);
	}

	private void initGUI() {
		try {
			splashScreen.updateText("Setting up class instances");
			//Initializes the menu bar
			initMenuBar();
			
			//Creates all tabs based on saved location in the Prop file
			leftTabPane = new JTabbedPane();
			topTabPane = new JTabbedPane();
			rightTabPane = new JTabbedPane();
			bottomTabPane = new JTabbedPane();
			createSugTab();
			createTransTab();
			createDocumentTab();
			createAnonymityTab();

			setUpContentPane();
			DriverEditor.setAllDocTabUseable(false, this);

			preProcessWindow = new PreProcessWindow(this);
			ppAdvancedWindow = preProcessWindow.advancedWindow;
			setDefaultValues();
			preferencesWindow = new PreferencesWindow(this);
			clustersWindow = new ClustersWindow();
			suggestionsWindow = new FAQWindow();
			clustersTutorial = new ClustersTutorial();
			versionControl = new VersionControl(this);
			resultsWindow = new ResultsWindow(this);
			rightClickMenu = new RightClickMenu(this);

			splashScreen.updateText("Initializing listeners");
			
			//Initialize GUIMain listeners
			DriverMenu.initListeners(this);
			DriverEditor.initListeners(this);
			DriverResultsTab.initListeners(this);
			DriverSuggestionsTab.initListeners(this);
			DriverClustersWindow.initListeners(this);
			DriverResultsWindow.initListeners(this);
			DriverTranslationsTab.initListeners(this);
			DictionaryBinding.init();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Loads up the last saved prop file (if able) and sets all the documents from that prop file automatically. It also checks
	 * what the last used feature and classifier were and (if able) set them over the default values.
	 * @throws Exception - if any of these values are not found in the prop file, we instead set them to the defaults
	 */
	protected void setDefaultValues() throws Exception {
		if (!PropertiesUtil.getProbSet().equals("")) {
			String problemSetPath = PropertiesUtil.getProbSet();
			Logger.logln(NAME+"Trying to load problem set at: " + problemSetPath);

			try {
				preProcessWindow.ps = new ProblemSet(problemSetPath);
				preProcessWindow.driver.updateAllComponents();
				ThePresident.canDoQuickStart = true;
			} catch (Exception exc) {
				Logger.logln(NAME+"Failed loading problemSet path \""+problemSetPath+"\"", LogOut.STDERR);
				PropertiesUtil.setProbSet("");
			}
		} else {
			Logger.logln(NAME+"No default problem set saved from last run, will continue without.", LogOut.STDOUT);
		}

		ppAdvancedWindow.featureChoice.setSelectedItem(PropertiesUtil.getFeature());
		ppAdvancedWindow.cfd = presetCFDs.get(ppAdvancedWindow.featureChoice.getSelectedIndex());
		preProcessWindow.driver.updateFeatureSetView(this);
		ppAdvancedWindow.advancedDriver.updateFeatPrepColor(this);

		Logger.logln(NAME+"Attempting to load classifier: "+PropertiesUtil.getClassifier());
		ppAdvancedWindow.setClassifier(PropertiesUtil.getClassifier());
		
		ppAdvancedWindow.classifiers.add(ppAdvancedWindow.advancedDriver.tmpClassifier);
		ppAdvancedWindow.classDescJTextPane.setText(ppAdvancedWindow.advancedDriver.getDesc(ppAdvancedWindow.classifiers.get(0)));
		ResultsWindow.updateResultsPrepColor(this);
		ppAdvancedWindow.advancedDriver.tmpClassifier = null;
	}

	/**
	 * Adds everything to the content pane.
	 * @throws Exception 
	 */
	protected void setUpContentPane() throws Exception {
		getContentPane().removeAll();

		// ------- initialize PARALLEL arrays for the panels, their names, and their locations
		ArrayList<String> panelNames = new ArrayList<String>();
		panelNames.add("Suggestions");
		panelNames.add("Translations");
		panelNames.add("Document");
		panelNames.add("Anonymity");
		panelNames.add("Results");

		HashMap<String, JPanel> panels = new HashMap<String, JPanel>(6);
		panels.put("Suggestions", suggestionsPanel);
		panels.put("Translations", translationsPanel);
		panels.put("Document", documentsPanel);
		panels.put("Anonymity", anonymityPanel);

		ArrayList<PropertiesUtil.Location> panelLocations = new ArrayList<PropertiesUtil.Location>();
		panelLocations.add(PropertiesUtil.getSuggestionsTabLocation());
		panelLocations.add(PropertiesUtil.getTranslationsTabLocation());
		panelLocations.add(PropertiesUtil.getDocumentsTabLocation());
		panelLocations.add(PropertiesUtil.getAnonymityTabLocation());
		panelLocations.add(PropertiesUtil.getResultsTabLocation());

		// ----- form the column specifications
		String columnString = "";
		int columnNumber = 0;
		if (panelLocations.contains(PropertiesUtil.Location.LEFT)) {
			columnString = columnString.concat("[]");
			columnNumber++;
		}
		if (panelLocations.contains(PropertiesUtil.Location.TOP) || panelLocations.contains(PropertiesUtil.Location.BOTTOM)) {
			columnString = columnString.concat("[grow, fill]");
			columnNumber++;
		}
		if (panelLocations.contains(PropertiesUtil.Location.RIGHT)) {
			columnString = columnString.concat("[]");
			columnNumber++;
		}

		// ----- form the row specifications
		String rowString = "";
		if (panelLocations.contains(PropertiesUtil.Location.TOP))
			rowString = rowString.concat("[grow, fill]");
		if (panelLocations.contains(PropertiesUtil.Location.BOTTOM))
			rowString = rowString.concat("[150:25%:]");

		// ------ set the content pane layout based on the tab locations
		getContentPane().setLayout(new MigLayout(
				"wrap " + columnNumber + ", gap 10 10", // layout constraints
				columnString, // column constraints
				rowString)); // row constraints)

		// ------ add all tabs to their correct tab panes
		for (int i = 0; i < panels.size(); i++) {
			if (panelLocations.get(i) == PropertiesUtil.Location.LEFT)
				leftTabPane.add(panelNames.get(i), panels.get(panelNames.get(i)));
			else if (panelLocations.get(i) == PropertiesUtil.Location.TOP)
				topTabPane.add(panelNames.get(i), panels.get(panelNames.get(i)));
			else if (panelLocations.get(i) == PropertiesUtil.Location.RIGHT)
				rightTabPane.add(panelNames.get(i), panels.get(panelNames.get(i)));
			else if (panelLocations.get(i) == PropertiesUtil.Location.BOTTOM)
				bottomTabPane.add(panelNames.get(i), panels.get(panelNames.get(i)));
			else
				throw new Exception();
		}

		// ------ add all tab panes, if they need to be added
		if (panelLocations.contains(PropertiesUtil.Location.LEFT))
			getContentPane().add(leftTabPane, "width 200!, spany, shrinkprio 1");
		if (panelLocations.contains(PropertiesUtil.Location.TOP))
			getContentPane().add(topTabPane, "width 100:400:, grow, shrinkprio 3");
		if (panelLocations.contains(PropertiesUtil.Location.RIGHT))
			getContentPane().add(rightTabPane, "width :353:353, spany, shrinkprio 2");
		if (panelLocations.contains(PropertiesUtil.Location.BOTTOM))
			getContentPane().add(bottomTabPane, "width 600:100%:, height 150:25%:, shrinkprio 3");

		getContentPane().revalidate();
		getContentPane().repaint();
	}

	public boolean resultsAreReady() {
		boolean ready = true;

		try {
			if (!resultsWindow.isReady())
				ready = false;
		} catch (Exception e) {
			ready = false;
		}

		return ready;
	}

	private JPanel createSugTab() {
		suggestionsPanel = new JPanel();
		MigLayout settingsLayout = new MigLayout(
				"fill, wrap 1, ins 0, gap 0 0",
				"grow, fill",
				"[][grow, fill][][grow, fill]");
		suggestionsPanel.setLayout(settingsLayout);
		{//================ Suggestions Tab =====================
			//--------- Elements to Add Label ------------------
			elementsToAddLabel = new JLabel("Elements To Add:");
			elementsToAddLabel.setHorizontalAlignment(SwingConstants.CENTER);
			elementsToAddLabel.setFont(titleFont);
			elementsToAddLabel.setOpaque(true);
			elementsToAddLabel.setBackground(blue);
			elementsToAddLabel.setBorder(rlborder);
			elementsToAddLabel.setToolTipText("<html><center>Words in your document that Anonymouth believes may<br>" +
											  "be helpful in masking your identity and that you should<br>" +
											  "consider using more often (where applicable)</center></html>");

			//--------- Elements to Add Text Pane ------------------
			elementsToAddPane = new JList<String>();
			elementsToAddScrollPane = new JScrollPane(elementsToAddPane);
			elementsToAddPane.setBorder(BorderFactory.createEmptyBorder(1,3,1,3));
			elementsToAdd = new DefaultListModel<String>();
			elementsToAdd.add(0, "Please process your document to receive suggestions");
			elementsToAddPane.setModel(elementsToAdd);
			elementsToAddPane.setEnabled(false);
			elementsToAddPane.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			elementsToAddPane.setDragEnabled(false);
			elementsToAddPane.setFocusable(false);
			
			clearAddHighlights = new JButton("Clear \"Add\" Highlights");

			//--------- Elements to Remove Label  ------------------
			elementsToRemoveLabel = new JLabel("Elements To Remove:");
			elementsToRemoveLabel.setHorizontalAlignment(SwingConstants.CENTER);
			elementsToRemoveLabel.setFont(titleFont);
			elementsToRemoveLabel.setOpaque(true);
			elementsToRemoveLabel.setBackground(blue);
			elementsToRemoveLabel.setBorder(rlborder);
			elementsToRemoveLabel.setToolTipText("<html><center>Words that Anonymouth believes may be more likely to expose<br>" +
												 "you and you should consider replacing.</center></html>");

			//--------- Elements to Remove Table ------------------			
			String[][] removeTableFiller = new String[1][1];
			removeTableFiller[0] = new String[] {"N/A", "N/A"};
			String[] removeTableHeaderFiller = {"Word to Remove", "Occurrences"};
			elementsToRemoveModel = new DefaultTableModel(removeTableFiller, removeTableHeaderFiller) {
				private static final long serialVersionUID = 1L;
				public boolean isCellEditable(int rowIndex, int mColIndex) {
			        return false;
			    }
			};
			
			elementsToRemoveTable = new ElementsTable(elementsToRemoveModel, this);
			elementsToRemoveTable.getTableHeader().setToolTipText("<html><b>Occurrances:</b> The number of times each word appears<br>" +
																"in all given docs written by the user.<br>" +
													"<br><b>Word To Remove:</b> The words you should consider<br>" +
																"removing or using less of in your document<br>" +
																"(sorted by most revealing from top to bottom)." +
																"</html>");
			elementsToRemoveTable.setToolTipText("<html><b>Occurrances:</b> The number of times each word appears<br>" +
																"in all given docs written by the user.<br>" +
													"<br><b>Word To Remove:</b> The words you should consider<br>" +
																"removing or using less of in your document<br>" +
																"(sorted by most revealing from top to bottom)." +
																"</html>");
			elementsToRemoveTable.setRowSelectionAllowed(true);
			elementsToRemoveTable.setColumnSelectionAllowed(false);
			elementsToRemoveTable.removeAllElements();
			elementsToRemoveModel.addRow(new String[] {"Please process to receive suggestions"});
			elementsToRemoveTable.setShowGrid(false);
			elementsToRemoveTable.getColumn("Occurrences").setMaxWidth(90);
			elementsToRemoveTable.getColumn("Occurrences").setMinWidth(90);
			elementsToRemoveTable.setEnabled(false);
			elementsToRemoveTable.setFocusable(false);
			elementsToRemoveScrollPane = new JScrollPane(elementsToRemoveTable);
			elementsToRemoveTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			
			clearRemoveHighlights = new JButton("Clear \"Remove\" Highlights");

			suggestionsPanel.add(elementsToAddLabel, "h " + titleHeight + "!");
			suggestionsPanel.add(elementsToAddScrollPane, "growx, height 50%");
			suggestionsPanel.add(clearAddHighlights, "growx");
			suggestionsPanel.add(elementsToRemoveLabel, "h " + titleHeight + "!");
			suggestionsPanel.add(elementsToRemoveScrollPane, "growx, height 50%");
			suggestionsPanel.add(clearRemoveHighlights, "growx");
		}//============ End Suggestions Tab =================
		return suggestionsPanel;
	}

	@SuppressWarnings("serial")
	private JPanel createTransTab() {
		translationsPanel = new JPanel();
		translationsPanel.setLayout(new MigLayout(
				"wrap, ins 0, gap 0 0",
				"grow, fill",
				"[][grow, fill][]"));
		{ // --------------translation panel components
			translationsLabel = new JLabel("Translations:");
			translationsLabel.setHorizontalAlignment(SwingConstants.CENTER);
			translationsLabel.setFont(titleFont);
			translationsLabel.setOpaque(true);
			translationsLabel.setBackground(blue);
			translationsLabel.setBorder(rlborder);

			translationsHolderPanel = new ScrollablePanel() {
				@Override
				public boolean getScrollableTracksViewportWidth() {
					return true;
				}
			};
			translationsHolderPanel.setScrollableUnitIncrement(SwingConstants.VERTICAL, ScrollablePanel.IncrementType.PIXELS, 10);
			translationsHolderPanel.setAutoscrolls(false);
			translationsHolderPanel.setOpaque(true);
			translationsHolderPanel.setLayout(new MigLayout(
					"wrap, ins 0, gap 0",
					"grow, fill",
					""));

			notTranslated = new JTextPane();

			if (PropertiesUtil.getDoTranslations())
				notTranslated.setText("Please process your document to receive translation suggestions.");
			else
				notTranslated.setText("You have turned translations off.");

			notTranslated.setBorder(BorderFactory.createEmptyBorder(1,3,1,3));
			notTranslated.setDragEnabled(false);
			notTranslated.setEditable(false);
			notTranslated.setFocusable(false);
			translationsHolderPanel.add(notTranslated);

			translationsScrollPane = new JScrollPane(translationsHolderPanel);
			translationsScrollPane.setOpaque(true);
			translationsScrollPane.setAutoscrolls(false);

			progressPanel = new JPanel();
			progressPanel.setLayout(new MigLayout(
					"wrap, fill, ins 0",
					"grow, fill",
					"[][][20]"));
			{
				JLabel translationsProgressTitleLabel = new JLabel("Progress:");
				translationsProgressTitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
				translationsProgressTitleLabel.setFont(titleFont);
				translationsProgressTitleLabel.setOpaque(true);
				translationsProgressTitleLabel.setBackground(blue);
				translationsProgressTitleLabel.setBorder(rlborder);

				translationsProgressLabel = new JLabel("No Translations Pending.");
				translationsProgressLabel.setHorizontalAlignment(SwingConstants.CENTER);

				translationsProgressBar = new JProgressBar();

				progressPanel.add(translationsProgressTitleLabel, "grow, h 25!");
				progressPanel.add(translationsProgressLabel, "grow");
				progressPanel.add(translationsProgressBar, "grow");
			}

			resetTranslator = new JButton("Reset Translator");
			resetTranslator.setEnabled(false);
			
			stopTranslations = new JButton("Stop");
			stopTranslations.setEnabled(false);
			startTranslations = new JButton("Start");
			startTranslations.setEnabled(false);
			
			translationsPanel.add(translationsLabel, "grow, h 25!, split 1");
			translationsPanel.add(stopTranslations, "split, h 30!");
			translationsPanel.add(startTranslations, "h 30!, wrap");
			translationsPanel.add(translationsScrollPane, "grow, h :100%:, wrap");
			translationsPanel.add(resetTranslator, "h 30!, wrap");
			translationsPanel.add(progressPanel, "grow");
		}
		return translationsPanel;
	}

	private JPanel createDocumentTab() {
		Logger.logln(NAME+"Creating Documents Tab...");
		if(tabMade == false) {
			normalFont = new Font("Ariel", Font.PLAIN, PropertiesUtil.getFontSize());
			
			documentsPanel = new JPanel();
			MigLayout EBPLayout = new MigLayout(
					"fill, wrap, ins 0, gap 0 0",
					"[grow, fill]",
					"[][grow, fill][]");
			documentsPanel.setLayout(EBPLayout);
			{
				documentLabel = new JLabel("Document:");
				documentLabel.setHorizontalAlignment(SwingConstants.CENTER);
				documentLabel.setFont(titleFont);
				documentLabel.setOpaque(true);
				documentLabel.setBackground(blue);
				documentLabel.setBorder(rlborder);

				documentScrollPane = new JScrollPane();
				setDocumentPane(new JTextPane());
				getDocumentPane().setDragEnabled(false);
				getDocumentPane().setText("This is where the latest version of your document will be.");
				getDocumentPane().setFont(normalFont);
				getDocumentPane().setEnabled(false);
				getDocumentPane().setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.GRAY), BorderFactory.createEmptyBorder(1,3,1,3)));

				InputFilter documentFilter = new InputFilter();
				((AbstractDocument)documentPane.getDocument()).setDocumentFilter(documentFilter);

				documentScrollPane.setViewportView(getDocumentPane());

				saveButton = new JButton("Save As...");
				saveButton.setToolTipText("Saves the modified document above to a new file.");

				processButton = new JButton("Process");
				processButton.setToolTipText("Processes the document.");

				documentsPanel.add(documentLabel, "grow, h " + titleHeight + "!");
				documentsPanel.add(documentScrollPane, "grow");
				documentsPanel.add(processButton, "right, split");
				documentsPanel.add(saveButton, "right");
			}
			tabMade = true;
		}
		return documentsPanel;
	}

	private JPanel createAnonymityTab() throws Exception {
		PropertiesUtil.Location location = PropertiesUtil.getAnonymityTabLocation();
		anonymityPanel = new JPanel();
		if (location == PropertiesUtil.Location.LEFT || location == PropertiesUtil.Location.RIGHT)
			anonymityPanel.setLayout(new MigLayout(
					"wrap, ins 0, gap 0 0",
					"grow, fill",
					"[][grow, fill][]"));
		else if (location == PropertiesUtil.Location.TOP)
			anonymityPanel.setLayout(new MigLayout(
					"wrap 2, fill, ins 0, gap 0",
					"[70%][30%]",
					"[][][grow, fill]"));
		else
			throw new Exception();

		{ // --------------anonymity panel components
			anonymityLabel = new JLabel("Anonymity:");
			anonymityLabel.setHorizontalAlignment(SwingConstants.CENTER);
			anonymityLabel.setFont(titleFont);
			anonymityLabel.setOpaque(true);
			anonymityLabel.setBackground(blue);
			anonymityLabel.setBorder(rlborder);

			anonymityDrawingPanel = new AnonymityDrawingPanel(this);

			anonymityDescription = new JLabel();
			anonymityDescription.setFont(new Font("Helvatica", Font.PLAIN, 15));
			anonymityDescription.setText("<html><center>Test document must<br>be processed to<br>receive results</center></html>");
			anonymityDescription.setHorizontalAlignment(SwingConstants.CENTER);

			anonymityHoldingPanel = new JPanel();
			anonymityHoldingPanel.setBackground(Color.WHITE);
			anonymityHoldingPanel.setLayout(new MigLayout(
					"wrap, ins 0, gap 0 0",
					"grow, fill",
					"[][grow, fill][]"));
			anonymityHoldingPanel.add(anonymityDrawingPanel, "h 494!, pad -10 0");
			anonymityHoldingPanel.add(anonymityDescription, "alignx 50%, growy, w ::150, gap 0 5"); //growy

			anonymityScrollPane = new JScrollPane(anonymityHoldingPanel);
			anonymityScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

			resultsTableLabel = new JLabel("Ownership Certainty:");
			resultsTableLabel.setHorizontalAlignment(SwingConstants.CENTER);
			resultsTableLabel.setFont(titleFont);
			resultsTableLabel.setOpaque(true);
			resultsTableLabel.setBackground(blue);
			resultsTableLabel.setBorder(rlborder);

			makeResultsPanel();
			resultsMainPanel.setLayout(new MigLayout(
					"aligny 50%"));
			resultsMainPanel.setBackground(Color.WHITE);

			if (location== PropertiesUtil.Location.LEFT || location == PropertiesUtil.Location.RIGHT) {
				anonymityPanel.add(anonymityLabel, "h " + titleHeight + "!, width 100:220:220");
				anonymityPanel.add(anonymityScrollPane, "h 200::, width 100:220:220");
				anonymityPanel.add(resultsTableLabel, "h " + titleHeight + "!, width 100:220:220");
				anonymityPanel.add(resultsScrollPane, "h 80:150:200, width 100:220:220");
			}
		}
		return anonymityPanel;
	}

	@SuppressWarnings("serial")
	private void makeResultsPanel() {
		resultsMainPanel = new JPanel() {
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2d = (Graphics2D)g;

				if (resultsAreReady()) {
					resultsLabel.setText("");

					if (resultsWindow.getAuthorSize() <= 20) {
						resultsHeight = 25 * resultsWindow.getAuthorSize();
					}

					resultsMainPanel.setPreferredSize(new Dimension(160, resultsHeight));
					g2d.drawImage(resultsWindow.getPanelChart(170, resultsHeight), -10, -6, null);
				} else {
					if (DriverEditor.isFirstRun)
						resultsLabel.setText("<html><center>Please process your<br>document to<br>receive results.</center></html>");
					else
						resultsLabel.setText("<html><center>Please wait while<br>re-processing</center></html>");
				}
			}
		};
		resultsLabel = new JLabel("");
		resultsLabel.setFont(new Font("Helvatica", Font.PLAIN, 15));
		resultsLabel.setHorizontalAlignment(SwingConstants.CENTER);

		resultsMainPanel.setPreferredSize(new Dimension(175, 110));
		resultsMainPanel.setToolTipText("Process your document to see ownership probability");
		resultsMainPanel.add(resultsLabel);

		resultsScrollPane = new JScrollPane(resultsMainPanel);
		resultsScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		resultsScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
	}

	public JTextPane getDocumentPane() {
		return documentPane;
	}

	public void setDocumentPane(JTextPane documentPane) {
		this.documentPane = documentPane;
	}
	
	public void enableUndo(boolean b) {
		editUndoMenuItem.setEnabled(b);
	}
	
	public void enableRedo(boolean b) {
		editRedoMenuItem.setEnabled(b);
	}
	
	public void updateDocLabel(String title) {
		documentLabel.setText("Document: " + title);
	}

	/**\
	 * Aligns the table header and cells to the specified alignment.
	 * @param table - The table you want to apply this too.
	 * @param alignment - the alignment you want. E.G. JLabel.CENTER or JLabel.RIGHT
	 * @param type - String, either "cell" to make the cells aligned, or "header" to make the header aligned
	 */
	public static class alignCellRenderer implements TableCellRenderer {

		DefaultTableCellRenderer defaultRenderer;
		DefaultTableCellRenderer headerRenderer;
		String type;

		public alignCellRenderer(JTable table, int alignment, String type) throws Exception {
			this.type = type;
			if (type == "cell") {
				defaultRenderer = (DefaultTableCellRenderer)table.getDefaultRenderer(String.class);
				defaultRenderer.setHorizontalAlignment(alignment);
			} else if (type == "header") {
				headerRenderer = (DefaultTableCellRenderer)table.getTableHeader().getDefaultRenderer();
				headerRenderer.setHorizontalAlignment(alignment);
			} else
				throw new Exception();
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,boolean hasFocus, int row, int col) {
			// bad input is caught in constructor
			if (type == "cell")
				return defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
			else
				return headerRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
		}
	}

	/**
	 * Class for resizing table columns to fit the data/header.
	 * Call ColumnsAutoSizer.sizeColumnsToFit(table); in tabledChanged when adding a TableModelListener.
	 * @author Jeff
	 *
	 */

	//http://bosmeeuw.wordpress.com/2011/08/07/java-swing-automatically-resize-table-columns-to-their-contents/
	public static class ColumnsAutoSizer {
		public static void sizeColumnsToFit(JTable table) {
			sizeColumnsToFit(table, 5);
		}

		public static void sizeColumnsToFit(JTable table, int columnMargin) {
			JTableHeader tableHeader = table.getTableHeader();
			if (tableHeader == null) {
				// can't auto size a table without a header
				return;
			}
			
			FontMetrics headerFontMetrics = tableHeader.getFontMetrics(tableHeader.getFont());
			int[] minWidths = new int[table.getColumnCount()];
			int[] maxWidths = new int[table.getColumnCount()];
			
			for (int columnIndex = 0; columnIndex < table.getColumnCount(); columnIndex++) {
				int headerWidth = headerFontMetrics.stringWidth(table.getColumnName(columnIndex));
				minWidths[columnIndex] = headerWidth + columnMargin;
				int maxWidth = getMaximalRequiredColumnWidth(table, columnIndex, headerWidth);
				maxWidths[columnIndex] = Math.max(maxWidth, minWidths[columnIndex]) + columnMargin;
			}
			
			adjustMaximumWidths(table, minWidths, maxWidths);
			
			for(int i = 0; i < minWidths.length; i++) {
				if (minWidths[i] > 0) 
					table.getColumnModel().getColumn(i).setMinWidth(minWidths[i]);
				if (maxWidths[i] > 0) {
					table.getColumnModel().getColumn(i).setMaxWidth(maxWidths[i]);
					table.getColumnModel().getColumn(i).setWidth(maxWidths[i]);
				}
			}
		}
		
		private static void adjustMaximumWidths(JTable table, int[] minWidths, int[] maxWidths) {
			if (table.getWidth() > 0) {
				// to prevent infinite loops in exceptional situations
				int breaker = 0;
				// keep stealing one pixel of the maximum width of the highest column until we can fit in the width of the table
				while(sum(maxWidths) > table.getWidth() && breaker < 10000) {
					int highestWidthIndex = findLargestIndex(maxWidths);
					maxWidths[highestWidthIndex] -= 1;
					maxWidths[highestWidthIndex] = Math.max(maxWidths[highestWidthIndex], minWidths[highestWidthIndex]);
					breaker++;
				}
			}
		}
		private static int getMaximalRequiredColumnWidth(JTable table, int columnIndex, int headerWidth) {
			int maxWidth = headerWidth;
			TableColumn column = table.getColumnModel().getColumn(columnIndex);
			TableCellRenderer cellRenderer = column.getCellRenderer();
			
			if(cellRenderer == null) 
				cellRenderer = new DefaultTableCellRenderer();
			
			for(int row = 0; row < table.getModel().getRowCount(); row++) {
				Component rendererComponent = cellRenderer.getTableCellRendererComponent(table,
						table.getModel().getValueAt(row, columnIndex),
						false,
						false,
						row,
						columnIndex);
				double valueWidth = rendererComponent.getPreferredSize().getWidth();
				maxWidth = (int) Math.max(maxWidth, valueWidth);
			}
			
			return maxWidth;
		}
		
		private static int findLargestIndex(int[] widths) {
			int largestIndex = 0;
			int largestValue = 0;
			
			for(int i = 0; i < widths.length; i++) {
				if (widths[i] > largestValue) {
					largestIndex = i;
					largestValue = widths[i];
				}
			}
			return largestIndex;
		}

		private static int sum(int[] widths) {
			int sum = 0;
			
			for(int width : widths) {
				sum += width;
			}
			
			return sum;
		}
	}
}
