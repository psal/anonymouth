package edu.drexel.psal.anonymouth.gooie;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;

import edu.drexel.psal.JSANConstants;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.*;
import javax.swing.tree.*;

import net.miginfocom.swing.MigLayout;

public class PreProcessSettingsFrame extends JDialog {

	private static final long serialVersionUID = 1L;
	//private final String NAME = "( "+this.getClass().getName()+" ) - ";
	// features tab
	protected static int cellPadding = 5;

	protected JLabel featuresToolsJLabel;
	protected JButton featuresNextJButton;
	protected JButton featuresBackJButton;
	protected JLabel featuresFeatureConfigJLabel;

	protected JLabel featuresFeatureExtractorContentJLabel;
	protected JLabel featuresFeatureExtractorJLabel;
	protected JLabel featuresFactorJLabel;
	protected JLabel featuresNormJLabel;
	protected JLabel featuresFeatureDescJLabel;

	protected JLabel featuresFeatureNameJLabel;
	protected JLabel featuresCullJLabel;
	protected JLabel featuresCanonJLabel;
	protected JButton featuresEditJButton;
	protected JButton featuresRemoveJButton;
	protected JButton featuresAddJButton;
	protected JList<String> featuresJList;
	protected DefaultComboBoxModel<String> featuresJListModel;
	protected JLabel featuresFeaturesJLabel;
	protected JTextPane featuresSetDescJTextPane;
	protected JScrollPane featuresSetDescJScrollPane;
	protected JLabel featuresSetDescJLabel;
	protected JTextField featuresSetNameJTextField;
	protected JLabel featuresSetNameJLabel;
	protected JButton featuresNewSetJButton;
	protected JButton featuresSaveSetJButton;
	protected JButton featuresLoadSetFromFileJButton;
	protected JButton featuresAddSetJButton;
	protected JComboBox<String> featuresSetJComboBox;
	protected DefaultComboBoxModel<String> featuresSetJComboBoxModel;
	protected JLabel featuresSetJLabel;
	protected JButton featuresAboutJButton;
	protected JLabel featuresListLabel;
	protected JLabel featuresInfoLabel;
	//
	// Calssifiers tab
	protected JTextField classAvClassArgsJTextField;
	protected JLabel classAvClassArgsJLabel;
	protected JComboBox<String> classClassJComboBox;
	protected JLabel classAvClassJLabel;
	protected JButton classAddJButton;

	protected JTextField classSelClassArgsJTextField;
	protected JLabel classSelClassArgsJLabel;
	protected JScrollPane classSelClassJScrollPane;
	protected DefaultListModel<String> classSelClassJListModel;
	protected JScrollPane classTreeScrollPane;
	protected JScrollPane classDescJScrollPane;
	protected JTextPane classDescJTextPane;
	protected JLabel classDescJLabel;
	protected JButton classBackJButton;
	protected JButton classNextJButton;
	protected JLabel classSelClassJLabel;
	protected JButton classRemoveJButton;
	protected JButton classAboutJButton;
	
	//-------------- HELP TAB PANE STUFF ---------
	protected JTabbedPane editorHelpTabPane;
	
	protected JPanel editorHelpPrepPanel;
	protected JButton prepAdvButton;
		protected JPanel prepDocumentsPanel;
			protected JPanel prepMainDocPanel;
				protected JLabel prepDocLabel;
				protected JButton loadProblemSetJButton;
				protected JButton saveProblemSetJButton;
				protected JList<String> prepMainDocList;
				protected JScrollPane prepMainDocScrollPane;
				protected JPanel mainDocSettingsPanel;
					protected JLabel mainDocSettingsFullPathLabel;
					protected JLabel mainDocSettingsSizeLabel;
					protected JLabel mainDocSettingsLastModifiedLabel;
				protected JButton removeTestDocJButton;
				protected JButton addTestDocJButton;
			protected JPanel prepSampleDocsPanel;
				protected JList<String> prepSampleDocsList;
				protected JScrollPane prepSampleDocsScrollPane;
				protected JPanel sampleDocSettingsPanel;
					protected JLabel sampleDocSettingsFullPathLabel;
					protected JLabel sampleDocSettingsSizeLabel;
					protected JLabel sampleDocSettingsLastModifiedLabel;
				protected JButton adduserSampleDocJButton;
				protected JButton removeuserSampleDocJButton;
			protected JPanel prepTrainDocsPanel;
				protected JTree trainCorpusJTree;
				protected JScrollPane trainCorpusJTreeScrollPane;
				protected JPanel trainDocSettingsPanel;
					protected JLabel trainDocSettingsFullPathLabel;
					protected JLabel trainDocSettingsSizeLabel;
					protected JLabel trainDocSettingsLastModifiedLabel;
				protected JButton removeTrainDocsJButton;
				protected JButton addTrainDocsJButton;
		protected JPanel prepFeaturesPanel;
			protected JLabel prepFeatLabel;
		protected JPanel prepClassifiersPanel;
			protected JLabel prepClassLabel;
			protected JPanel prepAvailableClassPanel;
				protected JTree classJTree;
				protected JScrollPane prepAvailableClassScrollPane;
			protected JPanel prepSelectedClassPanel;
				protected JList<String> classJList;
				protected JScrollPane prepSelectedClassScrollPane;
	
	protected JPanel editorHelpSugPanel;
		protected JPanel elementsPanel;
		protected JPanel elementsToAddPanel;
		protected JLabel elementsToAddLabel;
		protected JTextPane elementsToAddPane;
		protected JScrollPane elementsToAddScrollPane;
		protected JPanel elementsToRemovePanel;
		protected JLabel elementsToRemoveLabel;
		protected JTextPane elementsToRemovePane;
		protected JScrollPane elementsToRemoveScrollPane;
		
	protected JPanel editorHelpTransPanel;
		protected JPanel translationsPanel;
		protected JLabel translationsLabel;
		protected JTable translationsTable;
		protected JScrollPane translationsScrollPane;
		protected JComboBox<String> translationsComboBox;
	
	protected JPanel editorHelpInfoPanel;
		protected JLabel sentenceEditorLabel;
		protected JLabel documentViewerLabel;
		protected JLabel classificationResultsLabel;
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
	
	protected GUIMain main;
	public boolean panelsAreMade = false;
	
	protected JSplitPane splitPane;
	protected JScrollPane treeScrollPane;
	protected JScrollPane mainScrollPane;
	protected JScrollPane bottomScrollPane;
	
	protected JPanel treePanel;
	protected JTree tree;
	protected DefaultMutableTreeNode top;
	
	protected JPanel mainPanel;
	
	protected JPanel docPanel;
	protected JPanel docMainPanel;
	protected JButton clearProblemSetJButton;
	
	protected JPanel featPanel;
	protected JPanel featMainPanel;
	protected JTextPane featuresFeatureNameJTextPane;
	protected JTextPane featuresFeatureDescJTextPane;
	protected JTextPane featuresNormContentJTextPane;
	protected JTextPane featuresFactorContentJTextPane;
	protected JTable featuresFeatureExtractorContentJTable;
	protected JTable featuresFeatureExtractorConfigJTable;
	protected JTable featuresCanonJTable;
	protected JTable featuresCanonConfigJTable;
	protected JTable featuresCullJTable;
	protected JTable featuresCullConfigJTable;
	protected DefaultTableModel featuresFeatureExtractorContentJTableModel;
	protected DefaultTableModel featuresFeatureExtractorConfigJTableModel;
	protected DefaultTableModel featuresCanonJTableModel;
	protected DefaultTableModel featuresCanonConfigJTableModel;
	protected DefaultTableModel featuresCullJTableModel;
	protected DefaultTableModel featuresCullConfigJTableModel;
	
	protected JPanel classPanel;
	protected JPanel classMainPanel;
	
	protected JTabbedPane tabbedPane;
	
	public PreProcessSettingsFrame(GUIMain main)
	{
		super(main, "Advanced Configuration", Dialog.ModalityType.APPLICATION_MODAL);
		init(main);
		setVisible(false);
	}
	
	private void init(GUIMain main)
	{
		this.main = main;
		this.setMinimumSize(new Dimension(800, 578));
		this.setIconImage(new ImageIcon(getClass().getResource(JSANConstants.JSAN_GRAPHICS_PREFIX+ThePresident.ANONYMOUTH_LOGO)).getImage());
		initPanels();
		
		getContentPane().setLayout(new MigLayout(
				"fill, wrap 1, ins 0, gap 0 0",
				"fill, grow",
				"[grow][grow]"));
		tabbedPane = new JTabbedPane();
		tabbedPane.add("Documents", docPanel);
		tabbedPane.add("Features", featPanel);
		tabbedPane.add("Classifiers", classPanel);

		getContentPane().add(tabbedPane, "grow");
		
		Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
		this.setSize(new Dimension((int)(screensize.width*.8), (int)(screensize.height*.8)));
		this.setLocationRelativeTo(null); // makes it form in the center of the screen
	}
	
	public void openWindow()
	{
		this.setLocationRelativeTo(null); // makes it form in the center of the screen
		this.setVisible(true);
	}
	
	public void closeWindow() 
	{
		//main.setEnabled(true);
        WindowEvent wev = new WindowEvent(this, WindowEvent.WINDOW_CLOSING);
        Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(wev);
	}
	
	/**
	 * Initializes all of the panels on the tree. Must be called before the tree is created, because when the tree
	 * is initialized it selects the first leaf in the tree which causes the panel to be shown.
	 */
	private void initPanels()
	{
		//==========================================================================================
		//================================ Documents Panel =========================================
		//==========================================================================================
		docPanel = new JPanel();
		
		MigLayout docLayout = new MigLayout(
				"wrap",
				"grow, fill",
				"[30][grow, fill]");
		docPanel.setLayout(docLayout);
		{
			// Documents Label-----------------------------
			prepDocLabel = new JLabel("Documents:");
			prepDocLabel.setFont(new Font("Ariel", Font.BOLD, 15));
			prepDocLabel.setHorizontalAlignment(SwingConstants.CENTER);
			prepDocLabel.setBorder(GUIMain.rlborder);
			prepDocLabel.setOpaque(true);
			if (main.documentsAreReady())
				prepDocLabel.setBackground(main.ready);
			else
				prepDocLabel.setBackground(main.notReady);
			
			docMainPanel = new JPanel();
			docMainPanel.setLayout(new MigLayout(
					"wrap 3",
					"grow, fill",
					"[20][20]0[grow][20][grow]"));
			{
				JPanel docMainTopPanel = new JPanel();
				docMainTopPanel.setLayout(new MigLayout());
				{
					// combo box label---------------------------------------------
					JLabel problemSetJLabel = new JLabel("Problem Set:");
					docMainTopPanel.add(problemSetJLabel);
					
					// Save Problem Set button--------------------------------
					saveProblemSetJButton = new JButton("Save");
					docMainTopPanel.add(saveProblemSetJButton);
					
					// load problem set button-------------------------------
					loadProblemSetJButton = new JButton("Load");
					docMainTopPanel.add(loadProblemSetJButton);
					
					clearProblemSetJButton = new JButton("Clear");
					docMainTopPanel.add(clearProblemSetJButton);
				}
				
				// main label-----------------------------------
				JLabel mainLabel = new JLabel("Main:");
				mainLabel.setHorizontalAlignment(SwingConstants.CENTER);
				mainLabel.setFont(new Font("Ariel", Font.BOLD, 12));
				mainLabel.setOpaque(true);
				mainLabel.setBackground(main.blue);
				mainLabel.setBorder(BorderFactory.createRaisedBevelBorder());
				mainLabel.setBorder(GUIMain.rlborder);
				
				// sample label--------------------------------
				JLabel sampleLabel = new JLabel("Sample:");
				sampleLabel.setHorizontalAlignment(SwingConstants.CENTER);
				sampleLabel.setFont(new Font("Ariel", Font.BOLD, 12));
				sampleLabel.setOpaque(true);
				sampleLabel.setBackground(main.blue);
				//sampleLabel.setBorder(BorderFactory.createRaisedBevelBorder());
				sampleLabel.setBorder(GUIMain.rlborder);
				
				// train label----------------------------------
				JLabel trainLabel = new JLabel("Other Authors:");
				trainLabel.setHorizontalAlignment(SwingConstants.CENTER);
				trainLabel.setFont(new Font("Ariel", Font.BOLD, 12));
				trainLabel.setOpaque(true);
				trainLabel.setBackground(main.blue);
				trainLabel.setBorder(BorderFactory.createRaisedBevelBorder());
				trainLabel.setBorder(GUIMain.rlborder);
				
				
				// main documents list---------------------------------------------
				DefaultListModel<String> mainDocListModel = new DefaultListModel<String>();
				prepMainDocList = new JList<String>(mainDocListModel);
				prepMainDocList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				prepMainDocScrollPane = new JScrollPane(prepMainDocList);
				
				// sample documents list-----------------------------------------
				DefaultListModel<String> sampleDocsListModel = new DefaultListModel<String>();
				prepSampleDocsList = new JList<String>(sampleDocsListModel);
				prepSampleDocsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				prepSampleDocsScrollPane = new JScrollPane(prepSampleDocsList);
				
				// train tree---------------------------------------------------
				DefaultMutableTreeNode top = new DefaultMutableTreeNode(main.ps.getTrainCorpusName(), true);
				trainCorpusJTree = new JTree(top, true);
				trainCorpusJTreeScrollPane = new JScrollPane(trainCorpusJTree);
				
				// main add button----------------------------------------------
				addTestDocJButton = new JButton("Add");
				
				// main delete button-------------------------------------------
				removeTestDocJButton = new JButton("Delete");
				
				// sample add button-------------------------------------------
				adduserSampleDocJButton = new JButton("Add");
				
				// sample delete button------------------------------------------
				removeuserSampleDocJButton = new JButton("Delete");
				
				// train add button-------------------------------------------
				addTrainDocsJButton = new JButton("Add");
				
				// train delete button----------------------------------------
				removeTrainDocsJButton = new JButton("Delete");
				
				// main doc settings panel-------------------------------------------
				mainDocSettingsPanel = new JPanel();
				//mainDocSettingsPanel.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK));
				mainDocSettingsPanel.setLayout(new MigLayout("wrap 1", "grow", "top"));
				{
//					mainDocSettingsFullPathLabel = new JLabel("Location:");
//					mainDocSettingsPanel.add(mainDocSettingsFullPathLabel);
//					
//					mainDocSettingsSizeLabel = new JLabel("Size:");
//					mainDocSettingsPanel.add(mainDocSettingsSizeLabel);
//					
//					mainDocSettingsLastModifiedLabel = new JLabel("Last Modified:");
//					mainDocSettingsPanel.add(mainDocSettingsLastModifiedLabel);
				}
				
				// sample doc settings panel-------------------------------------------
				sampleDocSettingsPanel = new JPanel();
				//sampleDocSettingsPanel.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK));
				sampleDocSettingsPanel.setLayout(new MigLayout("wrap 1", "grow", "top"));
				{
//					sampleDocSettingsFullPathLabel = new JLabel("Location:");
//					sampleDocSettingsPanel.add(sampleDocSettingsFullPathLabel);
//					
//					sampleDocSettingsSizeLabel = new JLabel("Size:");
//					sampleDocSettingsPanel.add(sampleDocSettingsSizeLabel);
//					
//					sampleDocSettingsLastModifiedLabel = new JLabel("Last Modified:");
//					sampleDocSettingsPanel.add(sampleDocSettingsLastModifiedLabel);
				}
				
				// train doc settings panel-------------------------------------------
				trainDocSettingsPanel = new JPanel();
				//trainDocSettingsPanel.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK));
				trainDocSettingsPanel.setLayout(new MigLayout("wrap 1", "grow", "top"));
				{
//					trainDocSettingsFullPathLabel = new JLabel("Location:");
//					trainDocSettingsPanel.add(trainDocSettingsFullPathLabel);
//					
//					trainDocSettingsSizeLabel = new JLabel("Size:");
//					trainDocSettingsPanel.add(trainDocSettingsSizeLabel);
//					
//					trainDocSettingsLastModifiedLabel = new JLabel("Last Modified:");
//					trainDocSettingsPanel.add(trainDocSettingsLastModifiedLabel);
				}
				
				docMainPanel.add(docMainTopPanel, "span");
				docMainPanel.add(mainLabel);
				docMainPanel.add(sampleLabel);
				docMainPanel.add(trainLabel);
				docMainPanel.add(prepMainDocScrollPane, "grow, h 50:500:, w 50:100:"); //w 100::
				docMainPanel.add(prepSampleDocsScrollPane, "grow, h 50:500:, w 50:100"); //w 100::
				docMainPanel.add(trainCorpusJTreeScrollPane, "grow, h 50:500:, w 50:100"); //w 100::
				docMainPanel.add(addTestDocJButton, "split 2, w 50:100:");
				docMainPanel.add(removeTestDocJButton, "w 50:100:");
				docMainPanel.add(adduserSampleDocJButton, "split 2, w 50:100:");
				docMainPanel.add(removeuserSampleDocJButton, "w 50:100:");
				docMainPanel.add(addTrainDocsJButton, "split 2, w 50:100:");
				docMainPanel.add(removeTrainDocsJButton, "w 50:100:");
				docMainPanel.add(mainDocSettingsPanel, "grow, w 50:100:");
				docMainPanel.add(sampleDocSettingsPanel, "grow, w 50:100:");
				docMainPanel.add(trainDocSettingsPanel, "grow, w 50:100:");
			}
			docPanel.add(prepDocLabel, "h 30!");
			docPanel.add(docMainPanel);
		}
		
		//==========================================================================================
		//================================ Features Panel =========================================
		//==========================================================================================
		
		featPanel = new JPanel();
		
		MigLayout featLayout = new MigLayout(
				"wrap, gap 0 0",
				"[grow, fill]",
				"[30][grow, fill]");
		featPanel.setLayout(featLayout);
		{
			// Features Label---------------------------------------------------
			prepFeatLabel = new JLabel("Features:");
			prepFeatLabel.setFont(new Font("Ariel", Font.BOLD, 15));
			prepFeatLabel.setHorizontalAlignment(SwingConstants.CENTER);
			prepFeatLabel.setBorder(GUIMain.rlborder);
			prepFeatLabel.setOpaque(true);
			if (main.featuresAreReady())
				prepFeatLabel.setBackground(main.ready);
			else
				prepFeatLabel.setBackground(main.notReady);
			
			// Main Features area---------------------------------------------
			featMainPanel = new JPanel();
			featMainPanel.setLayout(new MigLayout(
					"fill, wrap 4, gap 0 0",
					"[200!]20[left][145:40%:, fill][240:60%:, fill]",
					"[][][20!][40!][20!][20!]20[33%, fill][33%, fill][33%, fill]"));
			{
				JPanel featMainTopPanel = new JPanel();
				featMainTopPanel.setLayout(new MigLayout(
						"wrap 6",
						"[][grow, fill][][][][]"));
				{
					// combo box label---------------------------------------------
					featuresSetJLabel = new JLabel("Feature Set:");
					featMainTopPanel.add(featuresSetJLabel);
					
					// Combo box----------------------------------------------------------
					String[] presetCFDsNames = new String[main.presetCFDs.size()];
					for (int i=0; i<main.presetCFDs.size(); i++)
						presetCFDsNames[i] = main.presetCFDs.get(i).getName();
			
					featuresSetJComboBoxModel = new DefaultComboBoxModel<String>(presetCFDsNames);
					featuresSetJComboBox = new JComboBox<String>();
					featuresSetJComboBox.setModel(featuresSetJComboBoxModel);
					featMainTopPanel.add(featuresSetJComboBox, "grow");
					
					featuresAddSetJButton = new JButton("Add");
					featMainTopPanel.add(featuresAddSetJButton);
					
					featuresLoadSetFromFileJButton = new JButton("Import");
					featMainTopPanel.add(featuresLoadSetFromFileJButton);
					
					featuresSaveSetJButton = new JButton("Export");
					featMainTopPanel.add(featuresSaveSetJButton);
					
					featuresNewSetJButton = new JButton("New");
					featMainTopPanel.add(featuresNewSetJButton);
					
					// description label-----------------------------------------------
					featuresSetDescJLabel = new JLabel("Description:");
					featMainTopPanel.add(featuresSetDescJLabel);
					
					// description pane--------------------------------------------------
					featuresSetDescJTextPane = new JTextPane();
					featuresSetDescJScrollPane = new JScrollPane(featuresSetDescJTextPane);
					featMainTopPanel.add(featuresSetDescJScrollPane, "span, grow");
				}
				
				featuresListLabel = new JLabel("Features:");
				featuresListLabel.setHorizontalAlignment(JLabel.CENTER);
				featuresListLabel.setFont(new Font("Ariel", Font.BOLD, 12));
				featuresListLabel.setOpaque(true);
				featuresListLabel.setBackground(main.blue);
				featuresListLabel.setBorder(GUIMain.rlborder);
				
				featuresInfoLabel = new JLabel("Feature Information:");
				featuresInfoLabel.setFont(new Font("Ariel", Font.BOLD, 12));
				featuresInfoLabel.setHorizontalAlignment(JLabel.CENTER);
				featuresInfoLabel.setOpaque(true);
				featuresInfoLabel.setBackground(main.blue);
				featuresInfoLabel.setBorder(GUIMain.rlborder);
				
				// features list--------------------------------------------------
				featuresJListModel = new DefaultComboBoxModel<String>();
				featuresJList = new JList<String>(featuresJListModel);
				JScrollPane featuresListJScrollPane = new JScrollPane(featuresJList);
				
				// feature name label--------------------------------------------------
				featuresFeatureNameJLabel = new JLabel("Name:");
				
				// feature name field--------------------------------------------------
				featuresFeatureNameJTextPane = new JTextPane();
				featuresFeatureNameJTextPane.setEditable(false);
				
				// feature description label--------------------------------------------------
				featuresFeatureDescJLabel = new JLabel("Description:");
				
				// feature description pane--------------------------------------------------
				featuresFeatureDescJTextPane = new JTextPane();
				//featuresFeatureDescJScrollPane = new JScrollPane(featuresFeatureDescJTextPane);
				featuresFeatureDescJTextPane.setEditable(false);
				
				// feature description pane--------------------------------------------------
				featuresNormJLabel = new JLabel("Normalization:");
				
				// feature description pane--------------------------------------------------
				featuresNormContentJTextPane = new JTextPane();
				featuresNormContentJTextPane.setEditable(false);
				
				// feature description pane--------------------------------------------------
				featuresFactorJLabel = new JLabel("Factor:");
				
				// feature description pane--------------------------------------------------
				featuresFactorContentJTextPane = new JTextPane();
				featuresFactorContentJTextPane.setEditable(false);
				
				// feature description pane--------------------------------------------------
				String[][] toolsTableFiller = new String[1][1];
				toolsTableFiller[0] = new String[] {"N/A"};
	        	String[] toolsTableHeaderFiller = {"Tools:"};
	        	
	        	String[][] configTableFiller = new String[1][1];
				configTableFiller[0] = new String[] {"N/A", "N/A"};
	        	String[] configTableHeaderFiller = {"Tool:", "Parameter:", "Value:"};
				
				// feature description pane--------------------------------------------------
				featuresFeatureExtractorJLabel = new JLabel("Extractor:");
				
				// feature description pane--------------------------------------------------
				featuresFeatureExtractorContentJTableModel = new DefaultTableModel(toolsTableFiller, toolsTableHeaderFiller){
					private static final long serialVersionUID = 1L;

					public boolean isCellEditable(int rowIndex, int mColIndex) {
				        return false;
				    }
				};
				
				featuresFeatureExtractorContentJTable = new JTable(featuresFeatureExtractorContentJTableModel);
				featuresFeatureExtractorContentJTable.setRowSelectionAllowed(false);
				featuresFeatureExtractorContentJTable.setColumnSelectionAllowed(false);
				
				// feature description pane--------------------------------------------------
				featuresFeatureExtractorConfigJTableModel = new DefaultTableModel(configTableFiller, configTableHeaderFiller){
					private static final long serialVersionUID = 1L;

					public boolean isCellEditable(int rowIndex, int mColIndex) {
				        return false;
				    }
				};
				
				featuresFeatureExtractorConfigJTable = new JTable(featuresFeatureExtractorConfigJTableModel);
				featuresFeatureExtractorConfigJTable.setRowSelectionAllowed(false);
				featuresFeatureExtractorConfigJTable.setColumnSelectionAllowed(false);
				
				// feature description pane--------------------------------------------------
				featuresCanonJLabel = new JLabel("Pre-Processing:");
				
				// feature description pane--------------------------------------------------
				featuresCanonJTableModel = new DefaultTableModel(toolsTableFiller, toolsTableHeaderFiller){
					private static final long serialVersionUID = 1L;

					public boolean isCellEditable(int rowIndex, int mColIndex) {
				        return false;
				    }
				};
				
				featuresCanonJTable = new JTable(featuresCanonJTableModel);
				featuresCanonJTable.setRowSelectionAllowed(false);
				featuresCanonJTable.setColumnSelectionAllowed(false);
				
				// feature description pane--------------------------------------------------
				featuresCanonConfigJTableModel = new DefaultTableModel(configTableFiller, configTableHeaderFiller){
					private static final long serialVersionUID = 1L;

					public boolean isCellEditable(int rowIndex, int mColIndex) {
				        return false;
				    }
				};
				
				featuresCanonConfigJTable = new JTable(featuresCanonConfigJTableModel);
				featuresCanonConfigJTable.setRowSelectionAllowed(false);
				featuresCanonConfigJTable.setColumnSelectionAllowed(false);
				
				// feature description pane--------------------------------------------------
				featuresCullJLabel = new JLabel("Post-Processing:");
				
				// feature description pane--------------------------------------------------
				featuresCullJTableModel = new DefaultTableModel(toolsTableFiller, toolsTableHeaderFiller){
					private static final long serialVersionUID = 1L;

					public boolean isCellEditable(int rowIndex, int mColIndex) {
				        return false;
				    }
				};
				
				featuresCullJTable = new JTable(featuresCullJTableModel);
				featuresCullJTable.setRowSelectionAllowed(false);
				featuresCullJTable.setColumnSelectionAllowed(false);
				
				// feature description pane--------------------------------------------------
				featuresCullConfigJTableModel = new DefaultTableModel(configTableFiller, configTableHeaderFiller){
					private static final long serialVersionUID = 1L;

					public boolean isCellEditable(int rowIndex, int mColIndex) {
				        return false;
				    }
				};
				
				featuresCullConfigJTable = new JTable(featuresCullConfigJTableModel);
				featuresCullConfigJTable.setRowSelectionAllowed(false);
				featuresCullConfigJTable.setColumnSelectionAllowed(false);
				
				featMainPanel.add(featMainTopPanel, "spanx, growx, gapbottom 20");
				featMainPanel.add(featuresListLabel, "w 50:200:");
				featMainPanel.add(featuresInfoLabel, "spanx, growx");
				featMainPanel.add(featuresListJScrollPane, "spany, growy, w 50:200:");
				featMainPanel.add(featuresFeatureNameJLabel);
				featMainPanel.add(new JScrollPane(featuresFeatureNameJTextPane), "span 2, grow");
				featMainPanel.add(featuresFeatureDescJLabel);
				featMainPanel.add(new JScrollPane(featuresFeatureDescJTextPane), "span 2, grow");
				featMainPanel.add(featuresNormJLabel);
				featMainPanel.add(new JScrollPane(featuresNormContentJTextPane), "span 2, grow");
				featMainPanel.add(featuresFactorJLabel);
				featMainPanel.add(new JScrollPane(featuresFactorContentJTextPane), "span 2, grow");
				featMainPanel.add(featuresFeatureExtractorJLabel);
				featMainPanel.add(new JScrollPane(featuresFeatureExtractorContentJTable));
				featMainPanel.add(new JScrollPane(featuresFeatureExtractorConfigJTable));
				featMainPanel.add(featuresCanonJLabel);
				featMainPanel.add(new JScrollPane(featuresCanonJTable));
				featMainPanel.add(new JScrollPane(featuresCanonConfigJTable));
				featMainPanel.add(featuresCullJLabel);
				featMainPanel.add(new JScrollPane(featuresCullJTable));
				featMainPanel.add(new JScrollPane(featuresCullConfigJTable));
				
				
				JTable[] configTableArray = {featuresFeatureExtractorConfigJTable, featuresCanonConfigJTable, featuresCullConfigJTable};
				
				for (final JTable table: configTableArray)
				{
					table.getModel().addTableModelListener(new TableModelListener() {
			            public void tableChanged(TableModelEvent e) {
			                GUIMain.ColumnsAutoSizer.sizeColumnsToFit(table);
			            }
			        });
				}
			}
			
			featPanel.add(prepFeatLabel, "span, h 30!");
			featPanel.add(featMainPanel);
		}

		
		//==========================================================================================
		//================================ Classifiers Panel =========================================
		//==========================================================================================
				
		classPanel = new JPanel();
		
		MigLayout classLayout = new MigLayout(
				"wrap",
				"grow, fill",
				"[30][grow, fill]");
		classPanel.setLayout(classLayout);
		{
			// Features Label---------------------------------------------------
			prepClassLabel = new JLabel("Classifiers:");
			prepClassLabel.setFont(new Font("Ariel", Font.BOLD, 15));
			prepClassLabel.setHorizontalAlignment(SwingConstants.CENTER);
			prepClassLabel.setBorder(GUIMain.rlborder);
			prepClassLabel.setOpaque(true);
			if (main.classifiersAreReady())
				prepClassLabel.setBackground(main.ready);
			else
				prepClassLabel.setBackground(main.notReady);
			
			// Main Features area---------------------------------------------
			classMainPanel = new JPanel();
			classMainPanel.setLayout(new MigLayout(
					"fill, wrap 2, ins 0 0",
					"grow, fill",
					"[20]0[65%, fill][20][20]20[20]0[40%, fill]"));
			{

				classAvClassJLabel = new JLabel("Available WEKA Classifiers:");
				classAvClassJLabel.setHorizontalAlignment(JLabel.CENTER);
				classAvClassJLabel.setFont(new Font("Ariel", Font.BOLD, 12));
				classAvClassJLabel.setOpaque(true);
				classAvClassJLabel.setBackground(main.blue);
				classAvClassJLabel.setBorder(GUIMain.rlborder);
				
				classSelClassJLabel = new JLabel("Selected WEKA Classifiers:");
				classSelClassJLabel.setHorizontalAlignment(JLabel.CENTER);
				classSelClassJLabel.setFont(new Font("Ariel", Font.BOLD, 12));
				classSelClassJLabel.setOpaque(true);
				classSelClassJLabel.setBackground(main.blue);
				classSelClassJLabel.setBorder(GUIMain.rlborder);
				
				classJTree = new JTree();
				classJTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
				classTreeScrollPane = new JScrollPane(classJTree);
				DriverPreProcessTabClassifiers.initAdvWekaClassifiersTree(this);
				
				classSelClassJListModel = new DefaultListModel<String>();
				classJList = new JList<String>(classSelClassJListModel);
				classJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				classSelClassJScrollPane = new JScrollPane(classJList);
				
				classAvClassArgsJLabel = new JLabel("Arguments:");
				
				classSelClassArgsJLabel = new JLabel("Arguments:");
				
				classAvClassArgsJTextField = new JTextField();
				
				classSelClassArgsJTextField = new JTextField();
				
				classAddJButton = new JButton("Add");
				
				classRemoveJButton = new JButton("Remove");
					
				classDescJLabel = new JLabel("Classifier Description");
				classDescJLabel.setHorizontalAlignment(JLabel.CENTER);
				classDescJLabel.setFont(new Font("Ariel", Font.BOLD, 12));
				classDescJLabel.setOpaque(true);
				classDescJLabel.setBackground(main.blue);
				classDescJLabel.setBorder(GUIMain.rlborder);
				
				classDescJTextPane = new JTextPane();
				classDescJTextPane.setEditable(false);
				classDescJScrollPane = new JScrollPane(classDescJTextPane);
				
				classMainPanel.add(classAvClassJLabel);
				classMainPanel.add(classSelClassJLabel);
				classMainPanel.add(classTreeScrollPane);
				classMainPanel.add(classSelClassJScrollPane);
				classMainPanel.add(classAvClassArgsJLabel, "split 2, grow 0");
				classMainPanel.add(classAvClassArgsJTextField);
				classMainPanel.add(classSelClassArgsJLabel, "split 2, grow 0");
				classMainPanel.add(classSelClassArgsJTextField);
				classMainPanel.add(classAddJButton);
				classMainPanel.add(classRemoveJButton);
				classMainPanel.add(classDescJLabel, "span");
				classMainPanel.add(classDescJScrollPane, "span");
			}
			classPanel.add(prepClassLabel, "h 30!");
			classPanel.add(classMainPanel);
		}
	}
}