package edu.drexel.psal.anonymouth.gooie;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Shape;
import java.awt.TextField;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
import edu.drexel.psal.anonymouth.engine.DocumentProcessor;
import edu.drexel.psal.anonymouth.engine.VersionControl;
import edu.drexel.psal.anonymouth.helpers.DisableFocus;
import edu.drexel.psal.anonymouth.helpers.ImageLoader;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.table.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.Highlighter.HighlightPainter;
import javax.swing.undo.UndoManager;

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

public class GUIMain extends JFrame implements DocumentListener {

	private final String icon = "icon48.jpg";
	private static final long serialVersionUID = 1L;
	private final String NAME = "( "+this.getClass().getSimpleName()+" ) - ";
	private final int MIN_WIDTH = 800;
	private final int MIN_HEIGHT = 578;
	
	public static GUIMain inst; //Our main instance for easy access anywhere in the code.
	private GUIMain main;

	//=====================================================================
	//						VARIOUS STUFF
	//=====================================================================
	
	//Banners / Titles
	protected final Border BANNER_BORDER = BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(),
			BorderFactory.createLoweredBevelBorder()); //The border of the banners used for translations and suggestions
	protected final Font BANNER_FONT = new Font("Ariel", Font.BOLD, 12); //The font of the banners used for translations and suggestions
	protected final String BANNER_HEIGHT = "25"; //The height of the banners used for translations and suggestions
	protected final Color BANNER_BACKGROUND_COLOR = new Color(212,212,212); //The background color of the banner
	protected final Color BANNER_FOREGROUND_COLOR = new Color(100,100,100); //The text color of the banner
	private final Color ADD_COLOR = new Color(0,190,0,200); //The background color of the banner used for words to add
	private final Color REMOVE_COLOR = new Color(190,0,0,200); //The background color of the banner used for words to remove
	
	//Pre Processing windows
	public PreProcessWindow preProcessWindow;
	public PreProcessAdvancedWindow ppAdvancedWindow;
	protected PreProcessDriver preProcessDriver;
	public PreProcessAdvancedDriver ppAdvancedDriver;
	public MenuDriver menuDriver;
	public ClustersDriver clustersDriver; 
	
	//Other stuff
	protected Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
	private WindowListener exitListener; //To know when the window is being closed
	private ComponentListener resizeListener; //To know when the window is resizing (so we can repaint the anonymity bar)
	public ANONConstants.STATE anonymityBarState = ANONConstants.STATE.VISIBLE;
	public ANONConstants.STATE suggestionTabsState = ANONConstants.STATE.VISIBLE;
	
	//=====================================================================
	//				LEFT TAB (anonymity bar, results, etc.)
	//=====================================================================
	
	protected JTabbedPane AnonymityBarTabPane;
	
	//Anonymity Bar
	public AnonymityBar anonymityBar;		//The actual bar
	protected JPanel anonymityHoldingPanel;	//The "main" anonymity bar panel, holds the bar, label, and anything else we want to add
	protected JPanel anonymityPanel;		//The entire left-hand tab of Anonymouth
	protected JLabel anonymityDescription;	//The Anonymity percentage/description label
	protected int anonymityWidth = 200;		//The current height of the panel
	protected int anonymityHeight = 450;	//The current width of the panel
	protected JLabel anonymityPercent;		//Used only when the Anonymity bar is hidden
	public double initChange = 1;		//in testing process
	public double startingPoint;
	
	//Results
	public ResultsWindow resultsWindow;
	protected ResultsDriver	resultsDriver;
	protected JButton resultsButton;
	
	//=====================================================================
	//						TOP TAB (middle editor)
	//=====================================================================
	
	protected JTabbedPane editorTabPane;
	
	//Editor
	protected JPanel documentPanel;
	protected JPanel originalDocumentPanel;
	public JTextPane documentPane;
	protected JTextPane originalDocPane;
	public JScrollPane documentScrollPane;
	protected JScrollPane originalDocScrollPane;
	public Boolean documentSaved = true;
	protected Font normalFont; //The editor's font
	public Document mainDocPreview; //Allows us to load the main document without having to alter the main.ps.testDocAt(0) directly
	public EditorDriver editorDriver;
	public DocumentProcessor documentProcessor;
	
	//Bottom Button
	public JButton reProcessButton;
	
	public Boolean processed = false;
	public Boolean reprocessing = false;
	
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
	protected DefaultListModel<String> elementsToAdd;
	protected JLabel elementsToRemoveLabel;
	protected DefaultTableModel elementsToRemoveModel;
	protected WordsToRemoveTable elementsToRemoveTable;
	protected JScrollPane elementsToRemoveScrollPane;
	protected JButton clearRemoveHighlights;
	protected JButton highlightAllRemoveHighlights;
	protected JButton refreshSuggestions;
	
	//Translations
	public TranslationsPanel translationsPanel;
	public TranslationsDriver translationsDriver;
	protected JPanel translationsMainPanel;
	protected JButton resetTranslator;
	public JButton translateSentenceButton;
	public JButton translationHelpButton;
	public ScrollablePanel translationsHolderPanel;
	protected JScrollPane translationsScrollPane;
	public JProgressBar translationsProgressBar;
	public JTextPane notTranslated;
	public JPanel translationsTopPanel;
	public ANONConstants.TRANSLATIONS translationsTopPanelShown;
	
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
	protected JMenuItem viewHideAnonymityBar;
	protected JMenuItem viewHideSuggestions;
	protected JMenuItem viewClustersMenuItem;
	protected JMenuItem viewDictionary;
	public JMenuItem viewEnterFullScreenMenuItem;
	protected JMenuItem helpMenu;
	protected JMenuItem fileMenu;
	protected JMenu windowMenu;
	protected JMenu editMenu;
	public JMenuItem editUndoMenuItem;
	public JMenuItem editRedoMenuItem;
	
	
	//Pull down selections
	protected PreferencesWindow preferencesWindow;
	protected ClustersWindow clustersWindow;
	protected DictionaryConsole dictionaryConsole;
	protected FAQWindow faqWindow;
	protected ClustersTutorial clustersTutorial;
	public VersionControl versionControl; //Undo/Redo
	protected RightClickMenu rightClickMenu; //Not really menu bar, but it's a menu
	protected Clipboard clipboard; //Edit > Copy/Paste/Cut
	public UndoManager undoManager;
	protected JTextField searchBar;
	protected JButton searchButton;
	//=====================================================================
	//---------------------------METHODS-----------------------------------
	//=====================================================================
	// ------------Undo / Redo class definition- --------------------------
	private Highlighter hilit;
	private DefaultHighlightPainter painter;
	
	
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
		//NOTE, better way to do this? This was the way it was when I came in, not very keen on static references though...
		inst = this; //Initializing our GUIMain static instance so we can reference the class instances from anywhere.
		main = this;
		
		initPropertiesUtil();		//Open the preferences file for reading and writing
		initWindow();				//Initializes The actual frame
		initMenuBar();				//Initializes the menu bar
		initComponents();			//All of the window's Swing Components
		initClassesAndListeners();	//Other class instances and their listeners/drivers
				
	//	DisableFocus.removeAllFocus(this); //Now that everything's added, let's disable focus traversal
	}
	
	/**
	 * Displays Anonymouth's main GUI.
	 */
	public void showGUIMain() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				setExtendedState(MAXIMIZED_BOTH);
				setLocationRelativeTo(null);
				main.setVisible(true);
				documentPane.requestFocusInWindow();
			}
		});
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				resultsWindow.showResultsWindow();
			}
		});
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
			enableOSXFullscreen(this);
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
		dictionaryConsole = new DictionaryConsole();
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
		
		
		//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		
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
		
		//"View" is a very OS X/(Linux?) menu bar, thus it's only available for it
		viewMenuItem = new JMenu("View");
		
		viewHideAnonymityBar = new JMenuItem("Hide Anonymity Bar");
		viewHideSuggestions = new JMenuItem("Hide Suggestion Tabs");
		viewMenuItem.add(viewHideAnonymityBar);
		viewMenuItem.add(viewHideSuggestions);
		viewMenuItem.add(new JSeparator());
		
		//We want the "Enter Full Screen" Menu Item to stay consistent with the Lion+ look and feel
		viewEnterFullScreenMenuItem = new JMenuItem("Enter Full Screen");
		viewMenuItem.add(viewEnterFullScreenMenuItem);
		
		//--------------------------------------------
		
		JMenu windowMenu = new JMenu("Window");
		if (!ANONConstants.IS_MAC) {
			settingsGeneralMenuItem = new JMenuItem("Preferences");
			windowMenu.add(settingsGeneralMenuItem);
			windowMenu.add(new JSeparator());
		}

		viewClustersMenuItem = new JMenuItem("Clusters");
		windowMenu.add(viewClustersMenuItem);
		
		viewDictionary = new JMenuItem("Dictionary");
		windowMenu.add(viewDictionary);
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

			viewEnterFullScreenMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));	
		} else {
			fileSaveAsTestDocMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK));
			fileSaveTestDocMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
			editUndoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK));
			editRedoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK));
		}
		
		//Adding everything to the menu bar, and finally adding that to the frame.
		menuBar.add(fileMenu);
		menuBar.add(editMenu);
		menuBar.add(viewMenuItem);
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
		editorTabPane = new JTabbedPane();
		initEditorTab();
		
		//RIGHT TAB (All helpers, including word to remove/add, translations, etc.)
		helpersTabPane = new JTabbedPane();
		initWordSuggestionsTab();
		initTranslationsTab();
		
		
		//Now we organize all these tabs and fit them into the window
		//Adding multiple tabs to areas where it is needed (i.e., same location)
		//Top
		editorTabPane.add("Document to Anonymize", documentPanel);
		editorTabPane.add("Original Document", originalDocumentPanel);
		//Right
		helpersTabPane.add("Word Suggestions", wordSuggestionsPanel);
		helpersTabPane.add("Translations", translationsMainPanel);
		
		this.setLayout(new MigLayout(
				"wrap 3, gap 10 10",//layout constraints
				"[][grow, fill][shrink]", //column constraints
				"[grow, fill]"));	// row constraints
		this.add(anonymityPanel, "width 80!, spany, shrinkprio 1");		//LEFT 		(Anonymity bar, results)
		this.add(editorTabPane, "width 100:400:, grow, shrinkprio 3");	//MIDDLE	(Editor)
		this.add(helpersTabPane, "width :353:353, spany, shrinkprio 1");//RIGHT		(Word Suggestions, Translations, etc.)
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
		documentProcessor = new DocumentProcessor(this);				//The processing God
		editorDriver = new EditorDriver(this);						//The main text editor driver
		
		main.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		
		//So we can intercept the window close if they didn't save changes
		exitListener = new WindowListener() {
			@Override
			public void windowClosing(WindowEvent e) {
				
				if (!main.documentSaved && PropertiesUtil.getWarnQuit()) {
					int confirm2 = JOptionPane.showOptionDialog(main, 
							"Are you sure you want to close this Application?\n", 
							"Unsaved Changes Warning", 
							JOptionPane.YES_NO_OPTION, 
							JOptionPane.QUESTION_MESSAGE, 
							UIManager.getIcon("OptionPane.warningIcon"), null, null);
					
					if (confirm2 == 0) {   //PropertiesUtil.getWarnQuit() && 
						main.toFront();
						main.requestFocus();
						menuDriver.save(main);
/*
						confirm = JOptionPane.showOptionDialog(main,							//showOptionalDialog (main,
							"Close Application?\nYou will lose all unsaved changes.", //Change to "Would you like to save to a specific document before closing?
																					  //If not, the document will be autoSaved.
							"Unsaved Changes Warning",
							JOptionPane.YES_NO_CANCEL_OPTION,
							JOptionPane.QUESTION_MESSAGE, 
							UIManager.getIcon("OptionPane.warningIcon"), null, null);
*/									
						//JFileChooser chooser = new JFileChooser("c:/users/denisaqori/My Documents/GitHub/anonymouth/jsan_resources/problem_sets");
						//int returnVal = chooser.showSaveDialog(main);
						//if (returnVal == JFileChooser.APPROVE_OPTION) {
						//	System.out.println("You chose to save the file " + chooser.getSelectedFile().getName());
						//	menuDriver.save(main);
						//}
						System.exit(0);
					}
/*					
				} 
				else if (main.documentSaved) {  //PropertiesUtil.getAutoSave() && 

					if (confirm2 == 0) {
						//Logger.logln(NAME+"Auto-saving document");
						//menuDriver.save(main);
						System.exit(0);
					}
*/
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
		
		//So we can repaint the anonymity bar accordingly
		resizeListener = new ComponentListener() {
			@Override
			public void componentResized(ComponentEvent e) {
				main.updateSizeVariables();
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
			elementsToAddLabel = new JLabel("+");
			elementsToAddLabel.setHorizontalAlignment(SwingConstants.CENTER);
			elementsToAddLabel.setFont(BANNER_FONT);
			elementsToAddLabel.setOpaque(true);
			elementsToAddLabel.setBackground(ADD_COLOR);
			elementsToAddLabel.setToolTipText("<html><center>Words in your document that Anonymouth believes may<br>" +
											  "be helpful in masking your identity and that you should<br>" +
											  "consider using more often (where applicable)</center></html>");

			//--------- Elements to Add Text Pane ------------------
			elementsToAddPane = new JList<String>();
			elementsToAddScrollPane = new JScrollPane(elementsToAddPane);
			elementsToAddPane.setBorder(BorderFactory.createEmptyBorder(1,3,1,3));
			elementsToAddScrollPane.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
			elementsToAdd = new DefaultListModel<String>();
			elementsToAddPane.setModel(elementsToAdd);
			elementsToAddPane.setEnabled(false);
			elementsToAddPane.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			elementsToAddPane.setDragEnabled(false);
			elementsToAddPane.setFocusable(false);

			//--------- Elements to Remove Label  ------------------
			elementsToRemoveLabel = new JLabel("-");
			elementsToRemoveLabel.setHorizontalAlignment(SwingConstants.CENTER);
			elementsToRemoveLabel.setFont(BANNER_FONT);
			elementsToRemoveLabel.setOpaque(true);
			elementsToRemoveLabel.setBackground(REMOVE_COLOR);
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
			
			elementsToRemoveTable = new WordsToRemoveTable(this, elementsToRemoveModel);
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
			elementsToRemoveScrollPane.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
			elementsToRemoveTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			
			highlightAllRemoveHighlights = new JButton("Highlight All");
			clearRemoveHighlights = new JButton("Clear All");
			refreshSuggestions = new JButton("Refresh");
		}
		
		//Adding everything in...
		wordSuggestionsPanel.add(elementsToAddLabel, "h " + BANNER_HEIGHT + "!");
		wordSuggestionsPanel.add(elementsToAddScrollPane, "growx, height 50%");
		wordSuggestionsPanel.add(elementsToRemoveLabel, "h " + BANNER_HEIGHT + "!");
		wordSuggestionsPanel.add(elementsToRemoveScrollPane, "growx, height 50%");
		wordSuggestionsPanel.add(highlightAllRemoveHighlights, "split, w 33%");
		wordSuggestionsPanel.add(clearRemoveHighlights, "w 33%");
		wordSuggestionsPanel.add(refreshSuggestions, "w 33%");
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
			"[][grow, fill]"));
		{
			//----------- Top Portion (Translate button, progress bar, etc.) -------------
			translateSentenceButton = new JButton("Translate Sentence");
			translationHelpButton = new JButton("?");
			resetTranslator = new JButton("Reset Translator");
			
			if (!PropertiesUtil.getDoTranslations())
				translateSentenceButton.setEnabled(false);
			
			translationsProgressBar = new JProgressBar();
			
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

			translationsScrollPane = new JScrollPane(translationsHolderPanel);
			translationsScrollPane.setOpaque(true);
			translationsScrollPane.setAutoscrolls(false);

			//------------ Text Pane displayed when no translations ------------------
			notTranslated = new JTextPane();
			notTranslated.setBorder(BorderFactory.createEmptyBorder(1,3,1,3));
			notTranslated.setDragEnabled(false);
			notTranslated.setEditable(false);
			translationsHolderPanel.add(notTranslated);
		}
		
		//The Class that handles obtaining and displaying all the different translations.
		translationsPanel = new TranslationsPanel(this);
		translationsTopPanel = new JPanel();
		translationsPanel.switchToButtonPanel();
		
		translationsMainPanel.add(translationsTopPanel);
		translationsMainPanel.add(translationsScrollPane, "grow, h :100%:, wrap");
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
		
		hilit = new DefaultHighlighter();
		painter = new DefaultHighlightPainter(Color.lightGray); 
		
		
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
			documentPane.setEnabled(true); // Original was false!!!!
			documentPane.setBorder(BorderFactory.createEmptyBorder(1,3,1,3));
			documentPane.setHighlighter(hilit);
			
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

			reProcessButton = new JButton("Reprocess");
			reProcessButton.setToolTipText("<html><center>Reprocesses any changes you've made your document to anonymize<br>" +
											"and updates the results graph with the new results.</center></html>");

			documentPanel.add(documentScrollPane, "grow");
			documentPanel.add(reProcessButton, "right");
			
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
			anonymityBar = new AnonymityBar(this);
			
			resultsButton = new JButton();
			resultsButton.setBackground(Color.WHITE);
			resultsButton.setIcon(resultsWindow.getButtonIcon());
			resultsButton.setToolTipText("Click to reopen process results");

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
			
			anonymityPercent = new JLabel();

			anonymityPanel.add(anonymityHoldingPanel, "width 80!");
			anonymityPanel.add(resultsButton, "dock south, gapbottom 9");
			
//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!			
			
			searchButton = new JButton("Search");
			searchButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					editorDriver.highlighterEngine.removeAllSearchHighlights();
					String s = searchBar.getText();
					if (s.length() <= 0) {
						System.out.println("Nothing to search!");
						return;
					}
					editorDriver.highlighterEngine.addAllSearchHighlights(s);
				}
				
			});
			
			JButton clearButton = new JButton("Clear");
			clearButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					editorDriver.highlighterEngine.removeAllSearchHighlights();
				}
			});
			
			searchBar = new JTextField();
			searchBar.setText("enter word...");
			searchBar.setForeground(Color.gray);
			searchBar.setEnabled(true);
			searchBar.setEditable(true);
			searchBar.setVisible(true);

			
			searchBar.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					if (searchBar.getText().equals("enter word...")) {
						searchBar.setText("");
					}
					searchBar.setForeground(Color.black);
				}
			});
				
			searchBar.setFocusAccelerator('f');
			
			anonymityPanel.add(searchBar, "dock north, width 40:60:80");
			anonymityPanel.add(searchButton, "dock north, width 40:60:80");
			anonymityPanel.add(clearButton, "dock north , width 40:60:80");

		}
	}
	
	
	/**
	 * Updates the anonymity bar variables for the new width and height of the anonymityPanel
	 * and anonymityHoldingPanel, respectively, so that it can be drawn to fit nicely with whatever
	 * size the window is (basically, it resizes like any javax swing component)
	 */
	protected void updateSizeVariables() {
		anonymityHeight = anonymityHoldingPanel.getHeight() + 15;
		anonymityWidth = anonymityPanel.getWidth();
		anonymityBar.updateForNewSize();
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
			editorTabPane.setTitleAt(index, title);
		} catch (Exception e) { //In case the index doesn't exit
			Logger.logln(NAME+"Tried to change the name of an editor tab that doesn't edit, index = " +
				index + ", name change to " + title);
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
	 * Sets nearly everything in GUIMain to be enabled or disable
	 * 
	 * @param enable
	 * 		Whether or not to enable all components
	 */
	public void enableEverything(final boolean enable) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				fileSaveTestDocMenuItem.setEnabled(enable);
				fileSaveAsTestDocMenuItem.setEnabled(enable);
				viewClustersMenuItem.setEnabled(enable);
				elementsToAddPane.setEnabled(enable);
				elementsToRemoveTable.setEnabled(enable);
				documentPane.setEnabled(enable);
				clipboard.setEnabled(enable);
				searchBar.setEnabled(enable);
				reProcessButton.setEnabled(enable);
				refreshSuggestions.setEnabled(enable);
				
				if (PropertiesUtil.getDoTranslations() && enable) {
					translateSentenceButton.setEnabled(true);
				} else {
					translateSentenceButton.setEnabled(false);
				}

				if (enable) {
					if (PropertiesUtil.getDoTranslations()) {
						main.resetTranslator.setEnabled(true);
					} else {
						main.resetTranslator.setEnabled(false);
					}
				}
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
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	public void enableOSXFullscreen(Window window) {
		try {
			Class util = Class.forName("com.apple.eawt.FullScreenUtilities");
			Class params[] = new Class[]{Window.class, Boolean.TYPE};
			Method method = util.getMethod("setWindowCanFullScreen", params);
			method.invoke(util, window, true);

			com.apple.eawt.FullScreenUtilities.addFullScreenListenerTo(window, new FullScreenListener () {
				@Override
				public void windowEnteredFullScreen(FullScreenEvent arg0) {
					main.viewEnterFullScreenMenuItem.setText("Exit Full Screen");
				}
				@Override
				public void windowEnteringFullScreen(FullScreenEvent arg0) {}
				@Override
				public void windowExitedFullScreen(FullScreenEvent arg0) {
					main.viewEnterFullScreenMenuItem.setText("Enter Full Screen");
				}
				@Override
				public void windowExitingFullScreen(FullScreenEvent arg0) {}
			});
		} catch (ClassNotFoundException e1) {
			Logger.logln(NAME+ "Failed initializing Anonymouth for full-screen", LogOut.STDERR);
		} catch (Exception e) {
			Logger.logln(NAME + "Failed initializing Anonymouth for full-screen", LogOut.STDERR);
		}
	}

	@Override
	public void changedUpdate(DocumentEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void insertUpdate(DocumentEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeUpdate(DocumentEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}
