package edu.drexel.psal.anonymouth.gooie;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;

import edu.drexel.psal.ANONConstants;
import edu.drexel.psal.jstylo.generics.*;
import edu.drexel.psal.jstylo.generics.Logger.LogOut;
import edu.drexel.psal.anonymouth.engine.Clipboard;
import edu.drexel.psal.anonymouth.engine.VersionControl;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.*;
import javax.swing.text.AbstractDocument;

import net.miginfocom.swing.MigLayout;

import com.jgaap.generics.Document;

import com.apple.eawt.AppEvent.FullScreenEvent;
import com.apple.eawt.FullScreenListener;

/**
 * The main window of Anonymouth. Initializes nearly all other
 * important classes and lays out the main window swing components
 * 
 * @author Andrew W.E. McDonald
 * @author Marc Barrowclift
 */

public class GUIMain extends javax.swing.JFrame {

	private static final long serialVersionUID = 1L;
	private final String NAME = "( "+this.getClass().getSimpleName()+" ) - ";
	private final int MIN_WIDTH = 800;
	private final int MIN_HEIGHT = 578;
	
	public static GUIMain inst; //Our main instance for easy access anywhere in the code.

	//=====================================================================
	//						VARIOUS STUFF
	//=====================================================================
	
	//Translation and suggestion banners
	protected Border rlborder = BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(),
			BorderFactory.createLoweredBevelBorder()); //The border of the banners used for translations and suggestions
	protected Font titleFont = new Font("Ariel", Font.BOLD, 12); //The font of the banners used for translations and suggestions
	protected String titleHeight = "25"; //The height of the banners used for translations and suggestions
	protected final Color blue = new Color(136,166,233,200); //The color of the banners used for translations and suggestions
	
	//Pre Processing windows
	public PreProcessWindow preProcessWindow;
	public PreProcessAdvancedWindow ppAdvancedWindow;
	protected PreProcessDriver preProcessDriver;
	protected PreProcessAdvancedDriver ppAdvancedDriver;
	public MenuDriver menuDriver;
	public ClustersDriver clustersDriver; 
	
	//Other stuff
	protected Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
	private WindowListener exitListener; //To know when the window is being closed
	private ComponentListener resizeListener; //To know when the window is resizing (so we can repaint the anonymity bar)
	
	//=====================================================================
	//				LEFT TAB (anonymity bar, results, etc.)
	//=====================================================================
	
	protected JTabbedPane AnonymityBarTabPane;
	
	//Anonymity Bar
	public AnonymityBar anonymityBar;		//The actual bar
	protected JPanel anonymityHoldingPanel;	//The "main" anonymity bar panel, holds the bar, label, and anything else we want to add
	protected JPanel anonymityPanel;		//The entire left-hand tab of Anonymouth
	protected JLabel anonymityLabel;		//The "Anonymity: " banner label
	protected JLabel anonymityDescription;	//The Anonymity percentage/description label
	protected int anonymityWidth = 200;			//The current height of the panel
	protected int anonymityHeight = 450;			//The current width of the panel
	
	//Results
	protected ResultsWindow resultsWindow;
	protected ResultsDriver	resultsDriver;
	protected JButton resultsButton;
	
	//=====================================================================
	//						TOP TAB (middle editor)
	//=====================================================================
	
	protected JTabbedPane EditorTabPane;
	
	//Editor
	protected JPanel documentPanel;
	protected JPanel originalDocumentPanel;
	public JTextPane documentPane;
	protected JTextPane originalDocPane;
	protected JScrollPane documentScrollPane;
	protected JScrollPane originalDocScrollPane;
	public Boolean saved = true;
	protected Font normalFont; //The editor's font
	public Document mainDocPreview; //Allows us to load the main document without having to alter the main.ps.testDocAt(0) directly
	
	//Bottom Button
	public JButton processButton;
	protected Boolean processed = false;
	
	//=====================================================================
	//			RIGHT TAB (words to remove/add, translations, etc.)
	//=====================================================================
	
	protected JTabbedPane helpersTabPane;
	
	//Words to Add/Remove
	public WordSuggestionsDriver wordSuggestionsDriver;
	protected JPanel wordSuggestionsPanel;
	protected JLabel elementsToAddLabel;
	protected JList<String> elementsToAddPane;
	protected JScrollPane elementsToAddScrollPane;
	protected JLabel elementsToRemoveLabel;
	protected DefaultTableModel elementsToRemoveModel;
	protected WordsToRemoveTable elementsToRemoveTable;
	protected JScrollPane elementsToRemoveScrollPane;
	protected DefaultListModel<String> elementsToAdd;
	protected DefaultListModel<String> elementsToRemove;
	protected JButton clearAddHighlights;
	protected JButton clearRemoveHighlights;
	
	//Translations
	public TranslationsPanel translationsPanel;
	public TranslationsDriver translationsDriver;
	protected JPanel translationsMainPanel;
	protected JLabel translationsLabel;
	protected JButton resetTranslator;
	public JButton stopTranslations;
	public JButton startTranslations;
	public ScrollablePanel translationsHolderPanel;
	protected JScrollPane translationsScrollPane;
	protected JPanel progressPanel;
	public JLabel translationsProgressLabel;
	public JProgressBar translationsProgressBar;
	public JTextPane notTranslated;
	private JLabel translationsProgressTitleLabel;
	
	//=====================================================================
	//			MENU BAR (Preferences, pull down menus, etc.)
	//=====================================================================
	
	//Pull down menus
	protected JMenuBar menuBar;
	protected JMenuItem settingsGeneralMenuItem;
	protected JMenuItem fileSaveProblemSetMenuItem;
	protected JMenuItem fileSaveTestDocMenuItem;
	protected JMenuItem fileSaveAsTestDocMenuItem;
	protected JMenuItem helpAboutMenuItem;
	protected JMenuItem helpSuggestionsMenuItem;
	protected JMenuItem helpClustersMenuItem;
	protected JMenuItem viewMenuItem;
	protected JMenuItem viewClustersMenuItem;
	public JMenuItem viewEnterFullScreenMenuItem;
	protected JMenuItem helpMenu;
	protected JMenuItem fileMenu;
	protected JMenu windowMenu;
	protected JMenuItem editMenu;
	public JMenuItem editUndoMenuItem;
	public JMenuItem editRedoMenuItem;
	
	//Pull down selections
	protected PreferencesWindow preferencesWindow;
	protected ClustersWindow clustersWindow;
	protected FAQWindow faqWindow;
	protected ClustersTutorial clustersTutorial;
	protected VersionControl versionControl; //Undo/Redo
	protected RightClickMenu rightClickMenu; //Not really menu bar, but it's a menu
	protected Clipboard clipboard; //Edit > Copy/Paste/Cut

	//=====================================================================
	//---------------------------METHODS-----------------------------------
	//=====================================================================

	/**
	 * Constructor, constructs nearly everything used by Anonymouth including
	 * all swing components, listeners, and other class instances.<br><br>
	 * 
	 * The main window is not visible by default after constructor is run, to
	 * show the window call "showGUIMain()"
	 */
	public GUIMain() {
		Logger.logln(NAME+"GUIMain being created...");
		ThePresident.splash.updateText("Initializing Anonymouth");
		inst = this;
		
		initPropertiesUtil();		//Open the preferences file for reading and writing
		initWindow();				//Initializes The actual frame
		initMenuBar();				//Initializes the menu bar
		initComponents();			//All of the window's Swing Components
		initClassesAndListeners();	//Other class instances and their listeners/drivers
	}
	
	/**
	 * Opens up the preferences file for reading and writing. Must be done first since a lot
	 * of component initialization after this uses the Properties File to get the saved preferences
	 * or information, and if the Properties file isn't actual initialized before then only
	 * the defaults will be used
	 */
	private void initPropertiesUtil() {
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
		
		if (!PropertiesUtil.getProbSet().equals("")) {
			ThePresident.canDoQuickStart = true;
		}
	}
	
	/**
	 * Initializes all settings specific to the Window itself (like default closer operation,
	 * size, etc.)
	 */
	private void initWindow() {
		//We want to enable the system-wide full screen functionality present in OS X if possible
		if (ANONConstants.IS_MAC) {
			enableOSXFullscreen();
		}
				
		ToolTipManager.sharedInstance().setDismissDelay(20000); //To keep Tool Tips from disappearing so fast
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); //We're letting the window listener take control of this
		this.setLocationRelativeTo(null); //Set for in the middle of the screen
		this.setTitle("Anonymouth");
		this.setIconImage(ThePresident.logo);
		this.setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
		this.setSize(new Dimension((int)(screensize.width*.75), (int)(screensize.height*.75)));
	}
	
	/**
	 * Creates the menu bar used by Anonymouth
	 */
	private void initMenuBar() {
		menuBar = new JMenuBar();
		preferencesWindow = new PreferencesWindow(this); //The Preferences Window (in "Anonymouth" for OS X, "Window" for others)
		clustersWindow = new ClustersWindow(); //The Clusters Viewer Window in the "View" pull-down menu for OS X, "Window" for others

		//--------------------------------------------
		fileMenu = new JMenu("File");
		fileSaveProblemSetMenuItem = new JMenuItem("Save Document Set");
		fileSaveTestDocMenuItem = new JMenuItem("Save");
		fileSaveAsTestDocMenuItem = new JMenuItem("Save As...");

		fileMenu.add(fileSaveProblemSetMenuItem);
		fileMenu.add(new JSeparator());
		fileMenu.add(fileSaveTestDocMenuItem);
		fileMenu.add(fileSaveAsTestDocMenuItem);

		//--------------------------------------------
		editMenu = new JMenu("Edit");
		editUndoMenuItem = new JMenuItem("Undo");
		editUndoMenuItem.setEnabled(false);
		editMenu.add(editUndoMenuItem);
		editRedoMenuItem = new JMenuItem("Redo");
		editRedoMenuItem.setEnabled(false);
		editMenu.add(editRedoMenuItem);
		clipboard = new Clipboard(this, editMenu); //The Class that handles copy/paste/cut functionality
		versionControl = new VersionControl(this); //The Class that handles undo/redo functionality
		
		//--------------------------------------------
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
		faqWindow = new FAQWindow();				//The FAQ window in the "Help" pull-down menu
		clustersTutorial = new ClustersTutorial();	//The clusters tutorial in the "Help" pull-down menu
		
		//--------------------------------------------

		/**
		 * The Menu location/text/keyboard shortcuts all change depending on the OS (and thus, the look and feel)
		 * currently being using to run Anonymouth, so we remain as consistent as we can with their OS as Java's
		 * swing allows
		 */
		if (ANONConstants.IS_MAC) {
			fileSaveAsTestDocMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,InputEvent.SHIFT_DOWN_MASK | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
			fileSaveTestDocMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
			editUndoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
			editRedoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.SHIFT_DOWN_MASK | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

			//"View" is a very OS X/(Linux?) menu bar, thus it's only available for it
			viewMenuItem = new JMenu("View");
			viewClustersMenuItem = new JMenuItem("Clusters");
			viewMenuItem.add(viewClustersMenuItem);
			
			//We also want the "Enter Full Screen" Menu Item to stay consistent with the Lion+ look and feel
			viewMenuItem.add(new JSeparator());
			viewEnterFullScreenMenuItem = new JMenuItem("Enter Full Screen");
			viewMenuItem.add(viewEnterFullScreenMenuItem);
			viewEnterFullScreenMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));	
		} else {
			fileSaveAsTestDocMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK));
			fileSaveTestDocMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
			editUndoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK));
			editRedoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK));
			
			/*
			 * "Window" is more predominant in Windows, and since "View" isn't really used for it we will stick our menu items such
			 * as Preferences and Clusters here instead
			 */
			JMenu windowMenu = new JMenu("Window");
			settingsGeneralMenuItem = new JMenuItem("Preferences");
			viewClustersMenuItem = new JMenuItem("Clusters");
			windowMenu.add(settingsGeneralMenuItem);
			windowMenu.add(viewClustersMenuItem);
		}

		//Adding everything to the menu bar, and finally adding that to the frame.
		menuBar.add(fileMenu);
		menuBar.add(editMenu);
		if (ANONConstants.IS_MAC)
			menuBar.add(viewMenuItem);
		else
			menuBar.add(windowMenu);
		menuBar.add(helpMenu);

		this.setJMenuBar(menuBar);
	}

	/**
	 * Initializes all swing components and organizes them in the window
	 */
	private void initComponents() {
		ThePresident.splash.updateText("Setting up main window");
		
		//LEFT TAB (Anonymity Bar, Results. Actually no longer a tab, just the left side)
		AnonymityBarTabPane = new JTabbedPane();
		resultsWindow = new ResultsWindow(this);
		initAnonymityTab();
		
		//TOP TAB (Actually middle tab, Editor)
		EditorTabPane = new JTabbedPane();
		initEditorTab();
		
		//RIGHT TAB (All helpers, including word to remove/add, translations, etc.)
		helpersTabPane = new JTabbedPane();
		initWordSuggestionsTab();
		initTranslationsTab();
		
		//Now we organize all these tabs and fit them into the window
		//Adding multiple tabs to areas where it is needed (i.e., same location)
		//Top
		EditorTabPane.add("Document to Anonymize", documentPanel);
		EditorTabPane.add("Original Document", originalDocumentPanel);
		//Right
		helpersTabPane.add("Word Suggestions", wordSuggestionsPanel);
		helpersTabPane.add("Translations", translationsMainPanel);
		
		this.setLayout(new MigLayout(
				"wrap 3, gap 10 10",//layout constraints
				"[][grow, fill][]", //column constraints
				"[grow, fill]"));	// row constraints
		this.add(anonymityPanel, "width 80!, spany, shrinkprio 1");		//LEFT 		(Anonymity bar, results)
		this.add(EditorTabPane, "width 100:400:, grow, shrinkprio 3");	//MIDDLE	(Editor)
		this.add(helpersTabPane, "width :353:353, spany, shrinkprio 2");//RIGHT		(Word Suggestions, Translations, etc.)

		/**
		 * This is needed so we can ensure that the Anonymity bar is getting the correct width and height needed
		 * to draw itself. pack() here calculates the component width and heights, which is what the anonymity
		 * bar uses to dynamically adjust itself.
		 */
		pack();
		updateSizeVariables();
	}
	
	/**
	 * Initializes all other Anonymouth instances and their respective listeners/drivers
	 */
	private void initClassesAndListeners() {
		ThePresident.splash.updateText("Preparing listeners");
		
		/**
		 * Initialize any remaining Anonymouth classes that will be used throughout the application
		 */
		preProcessWindow = new PreProcessWindow(this);								//The PreProcess "Set-up" wizard
		ppAdvancedWindow = new PreProcessAdvancedWindow(preProcessWindow, this);	//The PreProcess "advanced configuration" window
		rightClickMenu = new RightClickMenu(this);									//The right click menu for the editor
		
		/**
		 * Initialize all remaining Listeners/Drivers.
		 */
		wordSuggestionsDriver = new WordSuggestionsDriver(this);	//The Words to remove/add listeners
		menuDriver = new MenuDriver(this);							//The menu bar item listeners
		clustersDriver = new ClustersDriver();						//The Clusters Driver (no listeners currently)
		resultsDriver = new ResultsDriver(this);					//The Results Window listeners
		translationsDriver = translationsPanel.driver;				//Translations listeners
		preProcessDriver = preProcessWindow.driver;					//preProcess set-up wizard listeners
		ppAdvancedDriver = ppAdvancedWindow.driver;					//preProcess "Advanced Configuration" listeners
		
		EditorDriver.initListeners(this);
		
		exitListener = new WindowListener() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (PropertiesUtil.getWarnQuit() && !GUIMain.inst.saved) {
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
					Logger.logln(inst.NAME+"Auto-saving document");
					menuDriver.save(inst);
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
		this.addWindowListener(exitListener);
		
		resizeListener = new ComponentListener() {
			@Override
			public void componentResized(ComponentEvent e) {
				inst.updateSizeVariables();
			}
			
			@Override
			public void componentMoved(ComponentEvent e) {}
			@Override
			public void componentShown(ComponentEvent e) {}
			@Override
			public void componentHidden(ComponentEvent e) {}
		};
		this.addComponentListener(resizeListener);
	}
	
	/**
	 * Displays Anonymouth's main GUI.
	 */
	public void showGUIMain() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				setExtendedState(MAXIMIZED_BOTH);
				setLocationRelativeTo(null);
				GUIMain.inst.setVisible(true);
			}
		});
	}
	
	/**
	 * Updates the anonymity bar variables for the new width and height of the anonymityPanel
	 * and anonymityHoldingPanel, respectively, so that it can be drawn to fit nicely with whatever
	 * size the window is (basically, it resizes like any javax swing component)
	 */
	private void updateSizeVariables() {
		anonymityHeight = anonymityHoldingPanel.getHeight() + 15;
		anonymityWidth = anonymityPanel.getWidth();
		anonymityBar.updateForNewSize();
	}

	/**
	 * Creates and lays out all swing components in the word Suggestions tab
	 */
	private void initWordSuggestionsTab() {
		wordSuggestionsPanel = new JPanel();
		MigLayout settingsLayout = new MigLayout(
			"fill, wrap 1, ins 0, gap 0 0",
			"grow, fill",
			"[][grow, fill][][grow, fill]");
		wordSuggestionsPanel.setLayout(settingsLayout);
		{
			//--------- Elements to Add Label ------------------
			elementsToAddLabel = new JLabel("Words To Add:");
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
			elementsToAddPane.setModel(elementsToAdd);
			elementsToAddPane.setEnabled(false);
			elementsToAddPane.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			elementsToAddPane.setDragEnabled(false);
			elementsToAddPane.setFocusable(false);
			
			clearAddHighlights = new JButton("Clear \"Add\" Highlights");

			//--------- Elements to Remove Label  ------------------
			elementsToRemoveLabel = new JLabel("Words To Remove:");
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
			
			elementsToRemoveTable = new WordsToRemoveTable(elementsToRemoveModel);
			elementsToRemoveTable.getTableHeader().setToolTipText("<html><b>Occurrances:</b> The number of times each word appears<br>" +
																"in all given docs written by the user.<br>" +
													"<br><b>Word To Remove:</b> The words you should consider<br>" +
																"removing or using less of in your document<br>" +
																"(sorted by most revealing from top to bottom)." +
																"</html>");
			elementsToRemoveTable.setRowSelectionAllowed(true);
			elementsToRemoveTable.setColumnSelectionAllowed(false);
			elementsToRemoveTable.removeAllElements();
			elementsToRemoveTable.setShowGrid(false);
			elementsToRemoveTable.getColumn("Occurrences").setMaxWidth(90);
			elementsToRemoveTable.getColumn("Occurrences").setMinWidth(90);
			elementsToRemoveTable.setEnabled(false);
			elementsToRemoveTable.setFocusable(false);
			elementsToRemoveScrollPane = new JScrollPane(elementsToRemoveTable);
			elementsToRemoveTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			
			clearRemoveHighlights = new JButton("Clear \"Remove\" Highlights");
		}
		
		//Adding everything in...
		wordSuggestionsPanel.add(elementsToAddLabel, "h " + titleHeight + "!");
		wordSuggestionsPanel.add(elementsToAddScrollPane, "growx, height 50%");
		wordSuggestionsPanel.add(clearAddHighlights, "growx");
		wordSuggestionsPanel.add(elementsToRemoveLabel, "h " + titleHeight + "!");
		wordSuggestionsPanel.add(elementsToRemoveScrollPane, "growx, height 50%");
		wordSuggestionsPanel.add(clearRemoveHighlights, "growx");
	}

	/**
	 * Creates and lays out every swing component in the translations tab
	 */
	private void initTranslationsTab() {
		Logger.logln(NAME+"Creating Translations Tab...");
		
		translationsMainPanel = new JPanel();
		translationsMainPanel.setLayout(new MigLayout(
			"wrap, ins 0, gap 0 0",
			"grow, fill",
			"[][grow, fill][]"));
		{			
			//--------- Translations Label ------------------
			translationsLabel = new JLabel("Translations:");
			translationsLabel.setHorizontalAlignment(SwingConstants.CENTER);
			translationsLabel.setFont(titleFont);
			translationsLabel.setOpaque(true);
			translationsLabel.setBackground(blue);
			translationsLabel.setBorder(rlborder);

			//--------- Holds all translations ------------------
			translationsHolderPanel = new ScrollablePanel() {
				private static final long serialVersionUID = 1L;

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

			//------------ Text Pane displayed when no translations ------------------
			notTranslated = new JTextPane();

			if (PropertiesUtil.getDoTranslations()) {
				String text;
				if (ANONConstants.IS_MAC)
					text = "Anonymouth > Preferences";
				else
					text = "Window > Preferences";
				notTranslated.setText("Translations are off\n\nTo turn them on, navigate to " + text);
			}

			notTranslated.setBorder(BorderFactory.createEmptyBorder(1,3,1,3));
			notTranslated.setDragEnabled(false);
			notTranslated.setEditable(false);
			notTranslated.setFocusable(false);
			translationsHolderPanel.add(notTranslated);

			//----------- Scroll pane for all translations -------------
			translationsScrollPane = new JScrollPane(translationsHolderPanel);
			translationsScrollPane.setOpaque(true);
			translationsScrollPane.setAutoscrolls(false);
			
			//=============================================
			//The bottom of the translations, where we show the progress, allow them to reset, and so forth
			//=============================================
			
			progressPanel = new JPanel();
			progressPanel.setLayout(new MigLayout(
					"wrap, fill, ins 0",
					"grow, fill",
					"[][][20]"));
			{
				translationsProgressTitleLabel = new JLabel("Progress:");
				translationsProgressTitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
				translationsProgressTitleLabel.setFont(titleFont);
				translationsProgressTitleLabel.setOpaque(true);
				translationsProgressTitleLabel.setBackground(blue);
				translationsProgressTitleLabel.setBorder(rlborder);

				translationsProgressLabel = new JLabel("No Translations Pending.");
				translationsProgressLabel.setHorizontalAlignment(SwingConstants.CENTER);

				translationsProgressBar = new JProgressBar();
			}
			progressPanel.add(translationsProgressTitleLabel, "grow, h 25!");
			progressPanel.add(translationsProgressLabel, "grow");
			progressPanel.add(translationsProgressBar, "grow");

			//----------- All buttons (top start/stop, bottom reset) -------------
			resetTranslator = new JButton("Reset Translator");
			resetTranslator.setEnabled(false);
			
			stopTranslations = new JButton("Stop");
			stopTranslations.setEnabled(false);
			startTranslations = new JButton("Start");
			startTranslations.setEnabled(false);
		}
		
		//The Class that handles obtaining and displaying all the different translations.
		translationsPanel = new TranslationsPanel(this);
		
		translationsMainPanel.add(translationsLabel, "grow, h 25!, split 1");
		translationsMainPanel.add(stopTranslations, "split, h 30!");
		translationsMainPanel.add(startTranslations, "h 30!, wrap");
		translationsMainPanel.add(translationsScrollPane, "grow, h :100%:, wrap");
		translationsMainPanel.add(resetTranslator, "h 30!, wrap");
		translationsMainPanel.add(progressPanel, "grow");
	}

	/**
	 * Creates and lays out every swing component in the editor tabs
	 */
	private void initEditorTab() {
		Logger.logln(NAME+"Creating the Editor Tab...");

		//The Editor's font
		normalFont = new Font("Ariel", Font.PLAIN, PropertiesUtil.getFontSize());
		
		//The original document the user wants to anonymize (for reference) 
		originalDocumentPanel = new JPanel();
		originalDocumentPanel.setLayout(new MigLayout(
			"fill, wrap, ins 0, gap 0 0",
			"[grow, fill]",
			"[grow, fill]"
		));
		
		//The document the user will Anonymize
		documentPanel = new JPanel();
		MigLayout EBPLayout = new MigLayout(
				"fill, wrap, ins 0, gap 0 0",
				"[grow, fill]",
				"[grow, fill][]");
		documentPanel.setLayout(EBPLayout);
		{
			documentScrollPane = new JScrollPane();
			documentPane = new JTextPane();
			documentPane.setDragEnabled(false);
			documentPane.setText("This is where the latest version of your document will be.");
			documentPane.setFont(normalFont);
			documentPane.setEnabled(false);
			documentPane.setBorder(BorderFactory.createEmptyBorder(1,3,1,3));

			InputFilter documentFilter = new InputFilter();
			((AbstractDocument)documentPane.getDocument()).setDocumentFilter(documentFilter);

			documentScrollPane.setViewportView(documentPane);
			
			originalDocScrollPane = new JScrollPane();
			originalDocPane = new JTextPane();
			originalDocPane.setDragEnabled(false);
			originalDocPane.setText("This is where the original version of your document will be.");
			originalDocPane.setFont(normalFont);
			originalDocPane.setEnabled(true);
			originalDocPane.setEditable(false);
			originalDocPane.setBorder(BorderFactory.createEmptyBorder(1,3,1,3));
			originalDocScrollPane.setViewportView(originalDocPane);

			processButton = new JButton("Reprocess");
			processButton.setToolTipText("<html><center>Reprocesses any changes you've made your document to anonymize<br>" +
											"and updates the results graph with the new results.</center></html>");
			processButton.setFocusable(false);

			documentPanel.add(documentScrollPane, "grow");
			documentPanel.add(processButton, "right");
			
			originalDocumentPanel.add(originalDocScrollPane, "grow");
		}
	}

	/**
	 * Creates and lays out all components in the anonymity "tab" (there is no tab anymore, it's just
	 * the left-side of the window now.
	 */
	private void initAnonymityTab() {
		Logger.logln(NAME+"Creating the Anonymity Tab...");
		anonymityPanel = new JPanel();
		anonymityPanel.setLayout(new MigLayout(
			"wrap, ins 0, gap 0 0",
			"grow, fill",
			"[grow, fill]"));

		{
			anonymityLabel = new JLabel("Anonymity:");
			anonymityLabel.setHorizontalAlignment(SwingConstants.CENTER);
			anonymityLabel.setFont(titleFont);
			anonymityLabel.setOpaque(true);
			anonymityLabel.setBackground(blue);
			anonymityLabel.setBorder(rlborder);
	
			anonymityBar = new AnonymityBar(this);
			
			resultsButton = new JButton();
			resultsButton.setBackground(Color.WHITE);
			resultsButton.setIcon(resultsWindow.getButtonIcon());
			resultsButton.setToolTipText("Click to reopen process results");
			resultsButton.setFocusable(false);

			anonymityDescription = new JLabel();
			anonymityDescription.setFont(new Font("Helvatica", Font.PLAIN, 15));
			anonymityDescription.setText("<html><center>Test document must<br>be processed to<br>receive results</center></html>");
			anonymityDescription.setHorizontalAlignment(SwingConstants.CENTER);
			anonymityDescription.setHorizontalTextPosition(SwingConstants.CENTER);

			anonymityHoldingPanel = new JPanel();
			anonymityHoldingPanel.setBackground(Color.WHITE);
			anonymityHoldingPanel.setLayout(new MigLayout(
					"wrap, ins 0, gap 0 0",
					"grow, fill",
					"[grow, fill]"));
			anonymityHoldingPanel.add(anonymityBar, "grow");
			anonymityHoldingPanel.setBackground(Color.white);

			anonymityPanel.add(anonymityHoldingPanel, "width 80!");
			anonymityPanel.add(resultsButton, "dock south, gapbottom 9");
		}
	}
	
	/**
	 * Enables or disables the Edit > Undo menu item.
	 * 
	 * @param b
	 * 		Whether or not to enable the menu item
	 */
	public void enableUndo(boolean b) {
		editUndoMenuItem.setEnabled(b);
	}
	
	/**
	 * Enables or diables the Edit > Redo menu item.
	 * 
	 * @param b
	 * 		Whether or not to enable to menu item
	 */
	public void enableRedo(boolean b) {
		editRedoMenuItem.setEnabled(b);
	}
	
	/**
	 * Updates the given editor tab's title with a new one
	 * 
	 * @param title
	 * 		The new title you wish to use
	 * @param index
	 * 		The index of the editor tab you wish to change
	 */
	public void updateDocLabel(String title, int index) {
		try {
			title = title.replaceAll(".[Tt][Xx][Tt]$", "");
			EditorTabPane.setTitleAt(index, title);
		} catch (Exception e) { //In case the index doesn't exit
			Logger.logln(NAME+"Tried to change the name of an editor tab that doesn't edit, index = " +
				index + ", name change to " + title);
		}
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
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	public void enableOSXFullscreen() {
		try {
			Class util = Class.forName("com.apple.eawt.FullScreenUtilities");
			Class params[] = new Class[]{Window.class, Boolean.TYPE};
			Method method = util.getMethod("setWindowCanFullScreen", params);
			method.invoke(util, this, true);

			com.apple.eawt.FullScreenUtilities.addFullScreenListenerTo(this, new FullScreenListener () {
				@Override
				public void windowEnteredFullScreen(FullScreenEvent arg0) {
					GUIMain.inst.viewEnterFullScreenMenuItem.setText("Exit Full Screen");
				}
				@Override
				public void windowEnteringFullScreen(FullScreenEvent arg0) {}
				@Override
				public void windowExitedFullScreen(FullScreenEvent arg0) {
					GUIMain.inst.viewEnterFullScreenMenuItem.setText("Enter Full Screen");
				}
				@Override
				public void windowExitingFullScreen(FullScreenEvent arg0) {}
			});
		} catch (ClassNotFoundException e1) {
			Logger.logln(NAME+"Failed initializing Anonymouth for full-screen", LogOut.STDERR);
		} catch (Exception e) {
			Logger.logln(NAME+"Failed initializing Anonymouth for full-screen", LogOut.STDERR);
		}
	}
}
