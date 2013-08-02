package edu.drexel.psal.anonymouth.gooie;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import edu.drexel.psal.JSANConstants;
import edu.drexel.psal.jstylo.generics.CumulativeFeatureDriver;
import edu.drexel.psal.jstylo.generics.Logger;
import edu.drexel.psal.jstylo.generics.Logger.LogOut;
import edu.drexel.psal.anonymouth.helpers.ColumnsAutoSizer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.*;

import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.bayes.NaiveBayesMultinomial;
import weka.classifiers.functions.Logistic;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.functions.SMO;
import weka.classifiers.lazy.IBk;
import weka.classifiers.rules.ZeroR;
import weka.classifiers.trees.J48;

import net.miginfocom.swing.MigLayout;

/**
 * The "Advanced" parts of the PreProcess frame. The Advanced parts are anything relating to feature set and classification selection, as
 * any casual user who would want to use Anonymouth will have no idea what these things are and throwing these options at them will most
 * likely scare them away or confuse them. Thus, we want to obscure it from the normal view so the functionality's still there for research
 * purposes or advanced users but it won't get in the way for casual users.
 * 
 * This is the "Window" class for advanced preProcessing, which just handles creating and adding swing components and
 * "isSomething"/"hasSomething" methods pertaining to the problem set. As a guideline, any sort of "update" methods should be handled in
 * the corresponding "Driver" class though as a rule ALL listeners/events should be handled in the "Driver" class.
 * 
 * @author Marc Barrowclift
 *
 */
public class PreProcessAdvancedWindow extends JDialog {

	//Constants
	private static final long serialVersionUID = 1L;
	private static final String NAME = "( PreProcessAdvancedWindow ) - ";
	private final Font HELVETICA = new Font("Helvetica", Font.PLAIN, 22);
	private final String[] WEKACLASSNAMES = new String[] {
			//Bayes
			"weka.classifiers.bayes.NaiveBayes",
			"weka.classifiers.bayes.NaiveBayesMultinomial",
			//Functions
			"weka.classifiers.functions.Logistic",
			"weka.classifiers.functions.MultilayerPerceptron",
			"weka.classifiers.functions.SMO",
			//Lazy
			"weka.classifiers.lazy.IBk",
			//Rules
			"weka.classifiers.rules.ZeroR",
			//Trees
			"weka.classifiers.trees.J48",
	};

	//Variables
	protected PreProcessAdvancedDriver driver;
	protected PreProcessWindow preProcessWindow;
	protected GUIMain main;
	protected List<Classifier> classifiers;
	private String[] classifierNames;
	protected Hashtable<String, String> fullClassPath;
	protected Hashtable<String, String> shortClassName;

	//Swing components
	protected JTabbedPane tabbedPane;
	//========Features=========
	protected JPanel featureMainPanel;
	protected JLabel featureTitleLabel;
	protected JLabel featureNameLabel;
	protected JComboBox<String> featureChoice;
	protected DefaultComboBoxModel<String> featureChoiceModel;
	protected JButton featureViewInfoButton;
	protected JTextPane featureDescPane;
	protected JScrollPane featureDescScrollPane;
	protected JLabel featureDescLabel;
	protected JButton featureOKButton;
	protected JButton featureRestoreDefaultsButton;
	protected JPanel featureBottomPanel;
	protected JPanel featureMiddlePanel;
	protected JPanel featureMiddleFirstPanel;
	protected JPanel featureMiddleSecondPanel;
	protected JPanel featureCompleteBottomPanel;

		//===========View Info Window===========
		protected JDialog viewInfoFrame;
		protected JPanel viewInfoMainPanel;
		protected JPanel viewInfoBottomPanel;
		protected JLabel viewInfoListLabel;
		protected JLabel viewInfoExtractorLabel;
		protected JLabel viewInfoFactorLabel;
		protected JLabel viewInfoNormLabel;
		protected JLabel viewInfoDescLabel;
		protected JLabel viewInfoNameLabel;
		protected JLabel viewInfoInfoLabel;
		protected JLabel viewInfoCullLabel;
		protected JLabel viewInfoCanonLabel;
	
		protected JList<String> viewInfoList;
		protected DefaultComboBoxModel<String> viewInfoListModel;
		protected JScrollPane viewInfoListScrollPane;
	
		protected JTextPane viewInfoNameTextPane;
		protected JTextPane viewInfoDescTextPane;
		protected JTextPane viewInfoNormTextPane;
		protected JTextPane viewInfoFactorTextPane;
		protected JTextPane viewInfoExtractorPane;
		
		protected JTable viewInfoExtractorConfigTable;
		protected JTable viewInfoCanonTable;
		protected JTable viewInfoCanonConfigTable;
		protected JTable viewInfoCullTable;
		protected JTable viewInfoCullConfigTable;
		protected DefaultTableModel viewInfoExtractorConfigModel;
		protected DefaultTableModel viewInfoCanonTableModel;
		protected DefaultTableModel viewInfoCanonConfigTableModel;
		protected DefaultTableModel viewInfoCullTableModel;
		protected DefaultTableModel viewInfoCullConfigTableModel;

	//========Classifier=========
	private JPanel classMainPanel;
	private JPanel classMiddlePanel;
	private JLabel classTitleLabel;
	private JPanel classMiddleFirstPanel;
	private JPanel classMiddleSecondPanel;
	private JLabel classNameLabel;
	protected JComboBox<String> classChoice;
	protected DefaultComboBoxModel<String> classChoiceModel;
	protected JButton classEditButton;
	private JPanel classBottomPanel;
	protected JButton classOKButton;
	protected JButton classRestoreDefaultsButton;
	private JPanel classCompleteBottomPanel;
	private JLabel classDescLabel;
	protected JTextPane classDescPane;
	protected JScrollPane classDescScrollPane;
	
		//============Arguments Editor============
		protected JTextArea argsField;
		private JPanel argsBottomPanel;
		private JPanel argsMainPanel;
		protected JDialog argsFrame;
		protected JButton argsOKButton;
		protected JButton argsCancelButton;
		private JScrollPane argsScrollPane;

	/**
	 * Constructor
	 */
	public PreProcessAdvancedWindow(PreProcessWindow preProcessWindow, GUIMain main) {
		super(preProcessWindow, "Advanced Options", Dialog.ModalityType.APPLICATION_MODAL);
		Logger.logln(NAME+"Initializing the pre-process advanced settings window");
		this.preProcessWindow = preProcessWindow;
		this.main = main;

		initComponents();	
		initData();
		initWindow();

		setVisible(false);
	}

	/**
	 * Initializes all our data used in the window
	 */
	private void initData() {
		driver = new PreProcessAdvancedDriver(this, main);
		driver.cfd = new CumulativeFeatureDriver();
		classifiers = new ArrayList<Classifier>();
		FeatureWizardDriver.populateAll();
		initFeaturesList();
		initClassifiersList();
	}

	/**
	 * Initializes the window and it's attributes
	 */
	private void initWindow() {
		Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
		this.setSize(new Dimension((int)(screensize.width*.8), (int)(screensize.height*.8)));
		this.setLocationRelativeTo(null); // makes it form in the center of the screen
		this.setResizable(false);
		this.setSize(480, 310);
		this.setIconImage(ThePresident.logo);
	}

	/**
	 * Displays the window
	 */
	public void showWindow() {
		Logger.logln(NAME+"Advanced Settings window displayed");
		this.setLocationRelativeTo(null); // makes it form in the center of the screen

		if (tabbedPane.getSelectedIndex() == 0)
			this.getRootPane().setDefaultButton(featureOKButton);
		else
			this.getRootPane().setDefaultButton(classOKButton);
		
		this.setVisible(true);
	}

	/**
	 * Closes the window
	 */
	public void closeWindow() {
		Logger.logln(NAME+"Advanced Settings window closed");
		WindowEvent wev = new WindowEvent(this, WindowEvent.WINDOW_CLOSING);
		Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(wev);
	}
	
	/**
	 * Prepares all the Swing components for viewing
	 */
	private void initComponents() {
		initFeatureTab();
		initViewInfoFrame();
		initClassifierTab();
		initArgsFrame();

		getContentPane().setLayout(new MigLayout(
				"fill, ins 0, gap 0 0",
				"fill, grow",
				"fill, grow"));
		tabbedPane = new JTabbedPane();
		tabbedPane.add("Feature Set", featureMainPanel);
		tabbedPane.add("Classifier", classMainPanel);

		getContentPane().add(tabbedPane, "grow");
	}
	
	/**
	 * Initializes all swing components in the "Feature Set" tab
	 */
	private void initFeatureTab() {
		//==============FEATURES=================
		featureMainPanel = new JPanel();
		featureMainPanel.setLayout(new BorderLayout(0, 0));
		{
			featureTitleLabel = new JLabel("Current Feature Set");
			featureTitleLabel.setFont(HELVETICA);
			featureTitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
			featureTitleLabel.setHorizontalTextPosition(SwingConstants.CENTER);

			featureMiddlePanel = new JPanel();
			featureMiddlePanel.setLayout(new BorderLayout(0, 2));
			featureMiddlePanel.setBorder(new EmptyBorder(0, 10, 0, 10));
			{
				featureMiddleFirstPanel = new JPanel();
				featureMiddleFirstPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
				
				featureNameLabel = new JLabel("Name: ");
				featureChoice = new JComboBox<String>();
				featureViewInfoButton = new JButton("View Info");
				
				featureMiddleFirstPanel.add(featureNameLabel);
				featureMiddleFirstPanel.add(featureChoice);
				featureMiddleFirstPanel.add(featureViewInfoButton);
				featureMiddleFirstPanel.setAlignmentX(LEFT_ALIGNMENT);

				featureMiddleSecondPanel = new JPanel();
				featureMiddleSecondPanel.setLayout(new BorderLayout(0, 2));
				featureMiddleSecondPanel.setAlignmentX(LEFT_ALIGNMENT);
				
				featureDescLabel = new JLabel("Description: ");
				featureDescPane = new JTextPane();
				featureDescPane.setFocusable(false);
				featureDescPane.setEditable(false);
				featureDescScrollPane = new JScrollPane(featureDescPane);
				featureDescScrollPane.setPreferredSize(new Dimension(100, 100));
				featureDescScrollPane.setMaximumSize(new Dimension(100, 100));
				featureDescScrollPane.setMinimumSize(new Dimension(100, 100));
				featureDescScrollPane.setSize(new Dimension(100, 100));
				
				featureMiddleSecondPanel.add(featureDescLabel, BorderLayout.NORTH);
				featureMiddleSecondPanel.add(featureDescScrollPane, BorderLayout.SOUTH);
			}
			featureMiddlePanel.add(featureMiddleFirstPanel, BorderLayout.NORTH);
			featureMiddlePanel.add(featureMiddleSecondPanel, BorderLayout.SOUTH);

			featureBottomPanel = new JPanel();
			featureBottomPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
			
			featureOKButton = new JButton("Ok");
			featureRestoreDefaultsButton = new JButton("Restore Defaults");
			
			featureBottomPanel.add(featureRestoreDefaultsButton);
			featureBottomPanel.add(featureOKButton);
			
			featureCompleteBottomPanel = new JPanel();
			featureCompleteBottomPanel.setLayout(new BorderLayout(0, 10));
			featureCompleteBottomPanel.add(featureMiddlePanel, BorderLayout.NORTH);
			featureCompleteBottomPanel.add(featureBottomPanel, BorderLayout.SOUTH);
		}
		featureMainPanel.add(featureTitleLabel, BorderLayout.NORTH);
		featureMainPanel.add(featureCompleteBottomPanel, BorderLayout.SOUTH);		
	}
	
	/**
	 * Initializes all swing components in the "View ${FEATURE_NAME} Info" window as well as the window itself
	 */
	private void initViewInfoFrame() {
		viewInfoFrame = new JDialog(this);
		viewInfoFrame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		viewInfoFrame.setSize(815, 470);
		viewInfoFrame.setMinimumSize(new Dimension(660, 400));
		viewInfoFrame.setLocationRelativeTo(null);
		
		viewInfoMainPanel = new JPanel();
		viewInfoMainPanel.setLayout(new MigLayout(
				"fill, wrap 4, gap 0 0",
				"[200!]20[left, 100!][100%]",
				"[]5[40!]5[]5[]20![100%, fill]"));
		{
			viewInfoListLabel = new JLabel("Features:");
			viewInfoListLabel.setHorizontalAlignment(JLabel.CENTER);
			viewInfoListLabel.setFont(new Font("Helvetica", Font.BOLD, 12));
			viewInfoListLabel.setOpaque(true);
			viewInfoListLabel.setBackground(main.blue);
			viewInfoListLabel.setBorder(GUIMain.rlborder);

			viewInfoInfoLabel = new JLabel("Feature Information:");
			viewInfoInfoLabel.setFont(new Font("Ariel", Font.BOLD, 12));
			viewInfoInfoLabel.setHorizontalAlignment(JLabel.CENTER);
			viewInfoInfoLabel.setOpaque(true);
			viewInfoInfoLabel.setBackground(main.blue);
			viewInfoInfoLabel.setBorder(GUIMain.rlborder);

			viewInfoListModel = new DefaultComboBoxModel<String>();
			viewInfoList = new JList<String>(viewInfoListModel);
			viewInfoListScrollPane = new JScrollPane(viewInfoList);

			viewInfoNameLabel = new JLabel("Name:");

			viewInfoNameTextPane = new JTextPane();
			viewInfoNameTextPane.setEditable(false);

			viewInfoDescLabel = new JLabel("Description:");

			viewInfoDescTextPane = new JTextPane();
			viewInfoDescTextPane.setEditable(false);

			viewInfoNormLabel = new JLabel("Normalization:");

			viewInfoNormTextPane = new JTextPane();
			viewInfoNormTextPane.setEditable(false);

			viewInfoFactorLabel = new JLabel("Factor:");

			viewInfoFactorTextPane = new JTextPane();
			viewInfoFactorTextPane.setEditable(false);

			String[][] toolsTableFiller = new String[1][1];
			toolsTableFiller[0] = new String[] {"N/A"};
			String[] toolsTableHeaderFiller = {"Tools:"};

			String[][] configTableFiller = new String[1][1];
			configTableFiller[0] = new String[] {"N/A", "N/A"};
			String[] configTableHeaderFiller = {"Tool:", "Parameter:", "Value:"};

			viewInfoExtractorLabel = new JLabel("Extractor:");

			viewInfoExtractorPane = new JTextPane();
			viewInfoExtractorPane.setFocusable(false);
			viewInfoExtractorPane.setEditable(false);

			viewInfoExtractorConfigModel = new DefaultTableModel(configTableFiller, configTableHeaderFiller){
				private static final long serialVersionUID = 1L;

				public boolean isCellEditable(int rowIndex, int mColIndex) {
					return false;
				}
			};
			
			viewInfoExtractorConfigTable = new JTable(viewInfoExtractorConfigModel);
			viewInfoExtractorConfigTable.setRowSelectionAllowed(false);
			viewInfoExtractorConfigTable.setColumnSelectionAllowed(false);
			viewInfoExtractorConfigTable.doLayout();
			viewInfoExtractorConfigTable.setFocusable(true);
			viewInfoExtractorConfigTable.setRowSelectionAllowed(true);
			viewInfoExtractorConfigTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			
			viewInfoCanonLabel = new JLabel("Pre-Processing:");

			viewInfoCanonTableModel = new DefaultTableModel(toolsTableFiller, toolsTableHeaderFiller){
				private static final long serialVersionUID = 1L;

				public boolean isCellEditable(int rowIndex, int mColIndex) {
					return false;
				}
			};

			viewInfoCanonTable = new JTable(viewInfoCanonTableModel);
			viewInfoCanonTable.setRowSelectionAllowed(false);
			viewInfoCanonTable.setColumnSelectionAllowed(false);
			viewInfoCanonTable.doLayout();
			viewInfoCanonTable.setFocusable(true);
			viewInfoCanonTable.setRowSelectionAllowed(true);
			viewInfoCanonTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

			viewInfoCanonConfigTableModel = new DefaultTableModel(configTableFiller, configTableHeaderFiller){
				private static final long serialVersionUID = 1L;

				public boolean isCellEditable(int rowIndex, int mColIndex) {
					return false;
				}
			};

			viewInfoCanonConfigTable = new JTable(viewInfoCanonConfigTableModel);
			viewInfoCanonConfigTable.setRowSelectionAllowed(false);
			viewInfoCanonConfigTable.setColumnSelectionAllowed(false);
			viewInfoCanonConfigTable.doLayout();
			viewInfoCanonConfigTable.setFocusable(true);
			viewInfoCanonConfigTable.setRowSelectionAllowed(true);
			viewInfoCanonConfigTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

			viewInfoCullLabel = new JLabel("Post-Processing:");

			viewInfoCullTableModel = new DefaultTableModel(toolsTableFiller, toolsTableHeaderFiller){
				private static final long serialVersionUID = 1L;

				public boolean isCellEditable(int rowIndex, int mColIndex) {
					return false;
				}
			};

			viewInfoCullTable = new JTable(viewInfoCullTableModel);
			viewInfoCullTable.setRowSelectionAllowed(false);
			viewInfoCullTable.setColumnSelectionAllowed(false);
			viewInfoCullTable.doLayout();
			viewInfoCullTable.setFocusable(true);
			viewInfoCullTable.setRowSelectionAllowed(true);
			viewInfoCullTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			
			viewInfoCullConfigTableModel = new DefaultTableModel(configTableFiller, configTableHeaderFiller){
				private static final long serialVersionUID = 1L;

				public boolean isCellEditable(int rowIndex, int mColIndex) {
					return false;
				}
			};

			viewInfoCullConfigTable = new JTable(viewInfoCullConfigTableModel);
			viewInfoCullConfigTable.setRowSelectionAllowed(false);
			viewInfoCullConfigTable.setColumnSelectionAllowed(false);
			viewInfoCullConfigTable.doLayout();
			viewInfoCullConfigTable.setFocusable(true);
			viewInfoCullConfigTable.setRowSelectionAllowed(true);
			viewInfoCullConfigTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			
			JTable[] configTableArray = {viewInfoExtractorConfigTable, viewInfoCanonConfigTable, viewInfoCullConfigTable};

			for (final JTable table: configTableArray) {
				table.getModel().addTableModelListener(new TableModelListener() {
					public void tableChanged(TableModelEvent e) {
						ColumnsAutoSizer.sizeColumnsToFit(table);
					}
				});
			}
			
			viewInfoBottomPanel = new JPanel();
			viewInfoBottomPanel.setLayout(new MigLayout(
				"fill, wrap 2, gap 0 0 0 0, ins 0",
				"[150:45%:, fill][200:55%:, fill]",
				"[][fill][][fill][][fill]"));
			viewInfoBottomPanel.add(viewInfoExtractorLabel, "w 96!, split 2, span 2");
			viewInfoBottomPanel.add(new JScrollPane(viewInfoExtractorPane), "w 100%, growx");
			viewInfoBottomPanel.add(new JScrollPane(viewInfoExtractorConfigTable), "pad 3 0 0 0, span, grow, wrap");
			viewInfoBottomPanel.add(viewInfoCanonLabel, "wrap");
			viewInfoBottomPanel.add(new JScrollPane(viewInfoCanonTable), "grow");
			viewInfoBottomPanel.add(new JScrollPane(viewInfoCanonConfigTable), "grow");
			viewInfoBottomPanel.add(viewInfoCullLabel, "wrap");
			viewInfoBottomPanel.add(new JScrollPane(viewInfoCullTable), "grow");
			viewInfoBottomPanel.add(new JScrollPane(viewInfoCullConfigTable));

			viewInfoMainPanel.add(viewInfoListScrollPane, "spany, growy, w 50:200:");
			viewInfoMainPanel.add(viewInfoNameLabel);
			viewInfoMainPanel.add(new JScrollPane(viewInfoNameTextPane), "span 2, grow");
			viewInfoMainPanel.add(viewInfoDescLabel);
			viewInfoMainPanel.add(new JScrollPane(viewInfoDescTextPane), "span 2, grow");
			viewInfoMainPanel.add(viewInfoNormLabel);
			viewInfoMainPanel.add(new JScrollPane(viewInfoNormTextPane), "span 2, grow");
			viewInfoMainPanel.add(viewInfoFactorLabel);
			viewInfoMainPanel.add(new JScrollPane(viewInfoFactorTextPane), "span 2, grow");
			viewInfoMainPanel.add(viewInfoBottomPanel, "skip, span, grow");
		}
		viewInfoFrame.add(viewInfoMainPanel);
	}

	/**
	 * Initializes all swing components in the "Classifier" tab
	 */
	private void initClassifierTab() {			
		//==============Classifier=================	
		classMainPanel = new JPanel();
		classMainPanel.setLayout(new BorderLayout(0, 0));
		{
			classTitleLabel = new JLabel("Classifier");
			classTitleLabel.setFont(HELVETICA);
			classTitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
			classTitleLabel.setHorizontalTextPosition(SwingConstants.CENTER);

			classMiddlePanel = new JPanel();
			classMiddlePanel.setLayout(new BorderLayout(0, 2));
			classMiddlePanel.setBorder(new EmptyBorder(0, 10, 0, 10));
			{
				classMiddleFirstPanel = new JPanel();
				classMiddleFirstPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
				classMiddleFirstPanel.setAlignmentX(LEFT_ALIGNMENT);
				
				classNameLabel = new JLabel("Name: ");
				classChoice = new JComboBox<String>();
				classEditButton = new JButton("Edit");

				classMiddleFirstPanel.add(classNameLabel);
				classMiddleFirstPanel.add(classChoice);
				classMiddleFirstPanel.add(classEditButton);

				classMiddleSecondPanel = new JPanel();
				classMiddleSecondPanel.setLayout(new BorderLayout(0, 2));
				classMiddleSecondPanel.setAlignmentX(LEFT_ALIGNMENT);

				classDescLabel = new JLabel("Description: ");
				classDescPane = new JTextPane();
				classDescPane.setFocusable(false);
				classDescPane.setEditable(false);
				classDescScrollPane = new JScrollPane(classDescPane);
				classDescScrollPane.setPreferredSize(new Dimension(100, 100));
				classDescScrollPane.setMaximumSize(new Dimension(100, 100));
				classDescScrollPane.setMinimumSize(new Dimension(100, 100));
				classDescScrollPane.setSize(new Dimension(100, 100));
				
				classMiddleSecondPanel.add(classDescLabel, BorderLayout.NORTH);
				classMiddleSecondPanel.add(classDescScrollPane, BorderLayout.SOUTH);
			}
			classMiddlePanel.add(classMiddleFirstPanel, BorderLayout.NORTH);
			classMiddlePanel.add(classMiddleSecondPanel, BorderLayout.SOUTH);

			classBottomPanel = new JPanel();
			classBottomPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));

			classOKButton = new JButton("Ok");
			classRestoreDefaultsButton = new JButton("Restore Defaults");

			classBottomPanel.add(classRestoreDefaultsButton);
			classBottomPanel.add(classOKButton);

			classCompleteBottomPanel = new JPanel();
			classCompleteBottomPanel.setLayout(new BorderLayout(0, 10));
			classCompleteBottomPanel.add(classMiddlePanel, BorderLayout.NORTH);
			classCompleteBottomPanel.add(classBottomPanel, BorderLayout.SOUTH);
		}
		classMainPanel.add(classTitleLabel, BorderLayout.NORTH);
		classMainPanel.add(classCompleteBottomPanel, BorderLayout.SOUTH);
	}
	
	/**
	 * Initializes all swing components in the "Edit ${CLASSIFIER}'s Arguments" window as well as the window itself
	 */
	private void initArgsFrame() {
		argsFrame = new JDialog(this);
		argsFrame.setSize(350, 145);
		argsFrame.setResizable(true);
		argsFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		argsFrame.setLocationRelativeTo(null);
		argsFrame.setVisible(false);
		
		argsMainPanel = new JPanel();
		argsMainPanel.setLayout(new MigLayout("gap 0 0, ins 0", "grow", "[100%, fill][]"));
		argsMainPanel.setBorder(new EmptyBorder(8, 8, 3, 8));
		{
			argsField = new JTextArea();
			argsField.setLineWrap(true);
			argsScrollPane = new JScrollPane(argsField);
			argsScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			
			argsBottomPanel = new JPanel();
			argsBottomPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
			
			argsCancelButton = new JButton("Cancel");
			argsOKButton = new JButton("Ok");
			
			argsBottomPanel.add(argsCancelButton);
			argsBottomPanel.add(argsOKButton);
		}
		argsMainPanel.add(argsScrollPane, "grow, wrap, gapbottom 4");
		argsMainPanel.add(argsBottomPanel, "south");
		
		argsFrame.add(argsMainPanel);
		argsFrame.getRootPane().setDefaultButton(argsOKButton);
	}

	/**
	 * Initializes and fills the features combo box in the Features Tab with its respective data
	 * @param main - GUIMain instance
	 */
	protected void initFeaturesList() {
		main.presetCFDs = new ArrayList<CumulativeFeatureDriver>();

		try {
			File[] featureSetFiles = new File(JSANConstants.JSAN_FEATURESETS_PREFIX).listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.endsWith(".xml");
				}
			});

			String path;
			for (File f: featureSetFiles) {
				path = f.getAbsolutePath();
				main.presetCFDs.add(new CumulativeFeatureDriver(path));
			}

			String[] presetCFDsNames = new String[main.presetCFDs.size()];
			for (int i = 0; i < main.presetCFDs.size(); i++)
				presetCFDsNames[i] = main.presetCFDs.get(i).getName();

			featureChoiceModel = new DefaultComboBoxModel<String>(presetCFDsNames);
			featureChoice.setModel(featureChoiceModel);
		} catch (Exception e) {
			Logger.logln(NAME+"Failed to read feature set files.",LogOut.STDERR);
		}
	}

	/**
	* Initializes and fills the classifier combo box in the Classifier Tab with its respective data
	*/
	protected void initClassifiersList() {
		classifierNames = new String[WEKACLASSNAMES.length];
		fullClassPath = new Hashtable<String, String>();
		shortClassName = new Hashtable<String, String>();
		int curClassifier = 0;

		//Add all classes
		for (String className: WEKACLASSNAMES) {
			String[] nameArr = className.split("\\.");

			classifierNames[curClassifier] = nameArr[nameArr.length-1];
			fullClassPath.put(nameArr[nameArr.length-1], className);
			shortClassName.put(className,  nameArr[nameArr.length-1]);
			
			curClassifier++;
		}
		
		classChoiceModel = new DefaultComboBoxModel<String>(classifierNames);
		classChoice.setModel(classChoiceModel);
	}
	
	/**
	 * Selects the classifier you want in the Classifier combo box and updates all relevant components
	 * @param classifier - Short name of the classifier you want to select
	 */
	protected void setClassifier(String classifier) {
		classChoice.setSelectedItem(classifier);
		driver.updateClassifierTab();
	}
	
	/**
	 * Selects the feature you want in the Feature Set combo box and updates all relevant components
	 * @param feature - Short name of the feature you want to select
	 */
	protected void setFeature(String feauture) {
		featureChoice.setSelectedItem(feauture);
		driver.updateFeatureSetTab();
	}

	/**
	 * Checks to see if the classifier is set correctly.
	 * NOTE: This isn't necessarily needed anymore since you can't not select one from the combo box (this was rolled over from the old
	 * version with adding classifiers from a JTree), though I'm keeping it in as a precaution.
	 * @return
	 */
	protected boolean classifierIsReady() {
		boolean ready = true;

		try {
			if (classifiers.isEmpty())
				ready = false;
		} catch (Exception e) {
			return false;
		}

		return ready;
	}

	/**
	 * Checks to see if the feature set is ready
	 * NOTE: This isn't necessarily needed anymore since you can't not select one from the combo box (this was rolled over from the old
	 * version), though I'm keeping it in as a precaution.
	 * @return
	 */
	protected boolean featureSetIsReady() {
		boolean ready = true;

		try {
			if (driver.cfd.numOfFeatureDrivers() == 0)
				ready = false;
		} catch (Exception e) {
			return false;
		}

		return ready;
	}

	/**
	 * Returns true if the given cumulative feature driver is effectively empty
	 */
	protected boolean isFeatureDriversEmpty(CumulativeFeatureDriver featureDrivers) {
		if (featureDrivers == null)
			return true;
		else if ((featureDrivers.getName() == null || featureDrivers.getName().matches("\\s*")) &&
				(featureDrivers.getDescription() == null || featureDrivers.getDescription().matches("\\s*")) &&
				featureDrivers.numOfFeatureDrivers() == 0)
			return true;
		else
			return false;
	}

	/**
	 * Obtains the "Short name" of the classifier from the long full version
	 * @param path - The long "Full path" version of the classifier name
	 * @return
	 */
	protected String getNameFromClassPath(String path) {
		String[] classPath = path.split("\\.");
		return classPath[classPath.length - 1];
	}
	
	/**
	 * Initializes a map of classifier class-names to their respective descriptions.
	 */
	protected String getDesc(Classifier c) {
		if (c instanceof NaiveBayes) {
			return ((NaiveBayes) c).globalInfo();
		} else if (c instanceof NaiveBayesMultinomial) {
			return ((NaiveBayesMultinomial) c).globalInfo();
		} else if (c instanceof Logistic) {
			return ((Logistic) c).globalInfo();
		} else if (c instanceof MultilayerPerceptron) {
			return ((MultilayerPerceptron) c).globalInfo();
		} else if (c instanceof SMO) {
			return ((SMO) c).globalInfo();
		} else if (c instanceof IBk) {
			return ((IBk) c).globalInfo();
		} else if (c instanceof ZeroR) {
			return ((ZeroR) c).globalInfo();
		} else if (c instanceof J48) {
			return ((J48) c).globalInfo();
		} else {
			return "No description available.";
		}
	}
	
	/**
	 * Creates a classifier options string.
	 */
	public String getOptionsStr(String[] options) {
		String optionStr = "";
		for (String option: options)
			optionStr += option+" ";
		return optionStr;
	}
}