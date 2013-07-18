package edu.drexel.psal.anonymouth.gooie;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import edu.drexel.psal.JSANConstants;
import edu.drexel.psal.jstylo.generics.CumulativeFeatureDriver;
import edu.drexel.psal.jstylo.generics.Logger;
import edu.drexel.psal.jstylo.generics.Logger.LogOut;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.*;
import javax.swing.tree.*;

import weka.classifiers.Classifier;
import weka.core.OptionHandler;

import net.miginfocom.swing.MigLayout;

public class PreProcessAdvancedWindow extends JDialog {

	//Constants
	private static final long serialVersionUID = 1L;
	private static final String NAME = "( PreProcessAdvancedWindow ) - ";
	
	//Variables
	protected static int cellPadding = 5;
	protected PreProcessAdvancedDriver advancedDriver;
	protected PreProcessWindow preProcessWindow;
	protected GUIMain main;
	public boolean panelsAreMade = false;
	protected List<Classifier> classifiers;
	protected CumulativeFeatureDriver cfd;
	private String[] classifierNames;
	protected String selectedClassName;
	protected Hashtable<String, String> fullClassPath;
	protected Hashtable<String, String> shortClassName;
	
	//Swing components
	protected JTabbedPane tabbedPane;
	//========Features=========
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
		protected JComboBox<String> featureChoice;
		protected DefaultComboBoxModel<String> featuresSetJComboBoxModel;
		protected JLabel featuresSetJLabel;
		protected JButton featuresAboutJButton;
		protected JLabel featuresListLabel;
		protected JLabel featuresInfoLabel;
		
		protected JPanel prepFeaturesPanel;
		protected JLabel prepFeatLabel;
		
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

	//========Classifier=========
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
	
		protected JPanel prepClassifiersPanel;
		protected JLabel prepClassLabel;
		protected JPanel prepAvailableClassPanel;
		protected JTree classJTree;
		protected JScrollPane prepAvailableClassScrollPane;
		protected JPanel prepSelectedClassPanel;
		protected JList<String> classJList;
		protected JScrollPane prepSelectedClassScrollPane;
	
		protected JSplitPane splitPane;
		protected JScrollPane treeScrollPane;
		protected JScrollPane mainScrollPane;
		protected JScrollPane bottomScrollPane;
	
		protected JPanel treePanel;
		protected JTree tree;
		protected DefaultMutableTreeNode top;
	
		protected JPanel mainPanel;
		
		protected JPanel docPanel;
		protected JButton clearProblemSetJButton;
	
		protected JPanel classPanel;
		protected JPanel classMainPanel;
	
	/**
	 * Constructor
	 */
	public PreProcessAdvancedWindow(PreProcessWindow preProcessWindow, GUIMain main) {
		super(preProcessWindow, "Advanced Options", Dialog.ModalityType.APPLICATION_MODAL);
		Logger.logln(NAME+"Initializing the pre-process advanced settings window");
		this.preProcessWindow = preProcessWindow;
		this.main = main;
		initData();
		initComponents();
		initWindow();
		advancedDriver = new PreProcessAdvancedDriver(preProcessWindow, this, main);
		initClassifiersTree(this);
		setVisible(false);
	}
	
	private void initData() {
		cfd = new CumulativeFeatureDriver();
		classifiers = new ArrayList<Classifier>();
		FeatureWizardDriver.populateAll();
		initPresetCFDs(main);
	}
	
	/**
	 * Initializes window and it's attributes
	 */
	private void initWindow() {
		Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
		this.setSize(new Dimension((int)(screensize.width*.8), (int)(screensize.height*.8)));
		this.setLocationRelativeTo(null); // makes it form in the center of the screen
		this.setMinimumSize(new Dimension(800, 578));
		this.setIconImage(ThePresident.logo);
	}
	
	/**
	 * Displays the window
	 */
	public void showWindow() {
		Logger.logln(NAME+"Advanced Settings window displayed");
		this.setLocationRelativeTo(null); // makes it form in the center of the screen
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
		//==============FEATURES=================
		featPanel = new JPanel();
		MigLayout featLayout = new MigLayout(
				"wrap, gap 0 0",
				"[grow, fill]",
				"[30][grow, fill]");
		featPanel.setLayout(featLayout);
		{
			prepFeatLabel = new JLabel("Features:");
			prepFeatLabel.setFont(new Font("Ariel", Font.BOLD, 15));
			prepFeatLabel.setHorizontalAlignment(SwingConstants.CENTER);
			prepFeatLabel.setBorder(GUIMain.rlborder);
			prepFeatLabel.setOpaque(true);
			if (featuresAreReady())
				prepFeatLabel.setBackground(main.ready);
			else
				prepFeatLabel.setBackground(main.notReady);
			
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
					featuresSetJLabel = new JLabel("Feature Set:");
					featMainTopPanel.add(featuresSetJLabel);

					String[] presetCFDsNames = new String[main.presetCFDs.size()];
					for (int i=0; i<main.presetCFDs.size(); i++)
						presetCFDsNames[i] = main.presetCFDs.get(i).getName();
			
					featuresSetJComboBoxModel = new DefaultComboBoxModel<String>(presetCFDsNames);
					featureChoice = new JComboBox<String>();
					featureChoice.setModel(featuresSetJComboBoxModel);
					featMainTopPanel.add(featureChoice, "grow");
					
					featuresAddSetJButton = new JButton("Add");
					featMainTopPanel.add(featuresAddSetJButton);
					
					featuresLoadSetFromFileJButton = new JButton("Import");
					featMainTopPanel.add(featuresLoadSetFromFileJButton);
					
					featuresSaveSetJButton = new JButton("Export");
					featMainTopPanel.add(featuresSaveSetJButton);
					
					featuresNewSetJButton = new JButton("New");
					featMainTopPanel.add(featuresNewSetJButton);

					featuresSetDescJLabel = new JLabel("Description:");
					featMainTopPanel.add(featuresSetDescJLabel);

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

				featuresJListModel = new DefaultComboBoxModel<String>();
				featuresJList = new JList<String>(featuresJListModel);
				JScrollPane featuresListJScrollPane = new JScrollPane(featuresJList);
	
				featuresFeatureNameJLabel = new JLabel("Name:");

				featuresFeatureNameJTextPane = new JTextPane();
				featuresFeatureNameJTextPane.setEditable(false);

				featuresFeatureDescJLabel = new JLabel("Description:");

				featuresFeatureDescJTextPane = new JTextPane();
				featuresFeatureDescJTextPane.setEditable(false);

				featuresNormJLabel = new JLabel("Normalization:");

				featuresNormContentJTextPane = new JTextPane();
				featuresNormContentJTextPane.setEditable(false);

				featuresFactorJLabel = new JLabel("Factor:");

				featuresFactorContentJTextPane = new JTextPane();
				featuresFactorContentJTextPane.setEditable(false);

				String[][] toolsTableFiller = new String[1][1];
				toolsTableFiller[0] = new String[] {"N/A"};
	        	String[] toolsTableHeaderFiller = {"Tools:"};
	        	
	        	String[][] configTableFiller = new String[1][1];
				configTableFiller[0] = new String[] {"N/A", "N/A"};
	        	String[] configTableHeaderFiller = {"Tool:", "Parameter:", "Value:"};
				
				featuresFeatureExtractorJLabel = new JLabel("Extractor:");
				
				//Feature descriptions
				featuresFeatureExtractorContentJTableModel = new DefaultTableModel(toolsTableFiller, toolsTableHeaderFiller){
					private static final long serialVersionUID = 1L;

					public boolean isCellEditable(int rowIndex, int mColIndex) {
				        return false;
				    }
				};
				
				featuresFeatureExtractorContentJTable = new JTable(featuresFeatureExtractorContentJTableModel);
				featuresFeatureExtractorContentJTable.setRowSelectionAllowed(false);
				featuresFeatureExtractorContentJTable.setColumnSelectionAllowed(false);

				featuresFeatureExtractorConfigJTableModel = new DefaultTableModel(configTableFiller, configTableHeaderFiller){
					private static final long serialVersionUID = 1L;

					public boolean isCellEditable(int rowIndex, int mColIndex) {
				        return false;
				    }
				};
				
				featuresFeatureExtractorConfigJTable = new JTable(featuresFeatureExtractorConfigJTableModel);
				featuresFeatureExtractorConfigJTable.setRowSelectionAllowed(false);
				featuresFeatureExtractorConfigJTable.setColumnSelectionAllowed(false);

				featuresCanonJLabel = new JLabel("Pre-Processing:");

				featuresCanonJTableModel = new DefaultTableModel(toolsTableFiller, toolsTableHeaderFiller){
					private static final long serialVersionUID = 1L;

					public boolean isCellEditable(int rowIndex, int mColIndex) {
				        return false;
				    }
				};
				
				featuresCanonJTable = new JTable(featuresCanonJTableModel);
				featuresCanonJTable.setRowSelectionAllowed(false);
				featuresCanonJTable.setColumnSelectionAllowed(false);

				featuresCanonConfigJTableModel = new DefaultTableModel(configTableFiller, configTableHeaderFiller){
					private static final long serialVersionUID = 1L;

					public boolean isCellEditable(int rowIndex, int mColIndex) {
				        return false;
				    }
				};
				
				featuresCanonConfigJTable = new JTable(featuresCanonConfigJTableModel);
				featuresCanonConfigJTable.setRowSelectionAllowed(false);
				featuresCanonConfigJTable.setColumnSelectionAllowed(false);

				featuresCullJLabel = new JLabel("Post-Processing:");

				featuresCullJTableModel = new DefaultTableModel(toolsTableFiller, toolsTableHeaderFiller){
					private static final long serialVersionUID = 1L;

					public boolean isCellEditable(int rowIndex, int mColIndex) {
				        return false;
				    }
				};
				
				featuresCullJTable = new JTable(featuresCullJTableModel);
				featuresCullJTable.setRowSelectionAllowed(false);
				featuresCullJTable.setColumnSelectionAllowed(false);

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

		//==============Classifiers=================	
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
			if (classifiersAreReady())
				prepClassLabel.setBackground(main.ready);
			else
				prepClassLabel.setBackground(main.notReady);

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
		
		getContentPane().setLayout(new MigLayout(
				"fill, wrap 1, ins 0, gap 0 0",
				"fill, grow",
				"[grow][grow]"));
		tabbedPane = new JTabbedPane();
		tabbedPane.add("Documents", docPanel);
		tabbedPane.add("Features", featPanel);
		tabbedPane.add("Classifiers", classPanel);

		getContentPane().add(tabbedPane, "grow");
	}
	
	protected void initPresetCFDs(GUIMain main) {
		//Initialize list of preset CFDs
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
		} catch (Exception e) {
			Logger.logln(NAME+"Failed to read feature set files.",LogOut.STDERR);
			e.printStackTrace();
		}
	}
	
	protected void setClassifier(String className) {
		Classifier tmpClassifier;
		
		try {
			tmpClassifier = (Classifier)Class.forName(className).newInstance();
		} catch (Exception e) {
			Logger.logln(NAME+"Could not create classifier out of class: "+className);
			JOptionPane.showMessageDialog(this,
					"Could not generate classifier for selected class:\n"+className,
					"Classifier Selection Error",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			return;
		}
		
		//Add an -M option for SMO classifier
		String dashM = "";
		if(className.toLowerCase().contains("smo"))
			dashM = " -M";

		selectedClassName = getNameFromClassPath(className);
		
		//Show options and description
		classAvClassArgsJTextField.setText(advancedDriver.getOptionsStr(((OptionHandler)tmpClassifier).getOptions())+dashM);
		classDescJTextPane.setText(advancedDriver.getDesc(tmpClassifier));
		classifiers.add(tmpClassifier);
		advancedDriver.updateClassList(main);
		advancedDriver.updateClassPrepColor(main);
		classJTree.clearSelection();
	}
	
	protected String[] getClassifierNames() {
		return classifierNames;
	}
	
	protected String getClassName() {
		return selectedClassName;
	}
	
	protected boolean classifiersAreReady() {
		boolean ready = true;

		try {
			if (classifiers.isEmpty())
				ready = false;
		} catch (Exception e) {
			return false;
		}

		return ready;
	}
	
	protected boolean featuresAreReady() {
		boolean ready = true;

		try {
			if (cfd.numOfFeatureDrivers() == 0)
				ready = false;
		} catch (Exception e) {
			return false;
		}

		return ready;
	}
	
	/**
	 * Returns true iff the given cumulative feature driver is effectively empty
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
	 * Initialize available classifiers tree
	 */
	protected void initClassifiersTree(PreProcessAdvancedWindow PPSP) {
		//Create root and set to tree
		DefaultMutableTreeNode wekaNode = new DefaultMutableTreeNode("weka");
		DefaultMutableTreeNode classifiersNode = new DefaultMutableTreeNode("classifiers");
		wekaNode.add(classifiersNode);
		DefaultTreeModel model = new DefaultTreeModel(wekaNode);
		PPSP.classJTree.setModel(model);
		classifierNames = new String[wekaClassNames.length];
		fullClassPath = new Hashtable<String, String>();
		shortClassName = new Hashtable<String, String>();
		int curClassifier = 0;

		//Add all classes
		DefaultMutableTreeNode currNode, child;
		for (String className: wekaClassNames) {
			String[] nameArr = className.split("\\.");
			currNode = classifiersNode;

			for (int i = 2; i < nameArr.length; i++) {
				//Look for node
				@SuppressWarnings("unchecked")
				Enumeration<DefaultMutableTreeNode> children = currNode.children();
				while (children.hasMoreElements()) {
					child = children.nextElement();
					if (child.getUserObject().toString().equals(nameArr[i])) {
						currNode = child;
						classifierNames[curClassifier] = nameArr[i];
						fullClassPath.put(nameArr[i], className);
						shortClassName.put(className, nameArr[i]);
						break;
					}
				}

				//If not found, create a new one
				if (!currNode.getUserObject().toString().equals(nameArr[i])) {
					child = new DefaultMutableTreeNode(nameArr[i]);
					currNode.add(child);
					classifierNames[curClassifier] = nameArr[i];
					fullClassPath.put(nameArr[i], className);
					shortClassName.put(className, nameArr[i]);
					currNode = child;
				}
			}
			curClassifier++;
		}

		// expand tree
		int row = 0;
		while (row < PPSP.classJTree.getRowCount())
			PPSP.classJTree.expandRow(row++);
	}
	
	/**
	 * build classifiers tree from list of class names
	 */
	public String[] wekaClassNames = new String[] {
			//Bayes
			//"weka.classifiers.bayes.BayesNet",
			"weka.classifiers.bayes.NaiveBayes",
			"weka.classifiers.bayes.NaiveBayesMultinomial",
			//"weka.classifiers.bayes.NaiveBayesMultinomialUpdateable",
			//"weka.classifiers.bayes.NaiveBayesUpdateable",

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
	
	protected String getNameFromClassPath(String path) {
		String[] classPath = path.split("\\.");
		return classPath[classPath.length - 1];
	}
}