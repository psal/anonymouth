package edu.drexel.psal.jstylo.GUI;

import edu.drexel.psal.ANONConstants;
import edu.drexel.psal.jstylo.generics.FeatureDriver;
import edu.drexel.psal.jstylo.generics.NormBaselineEnum;

import java.awt.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.tree.*;

import com.jgaap.generics.*;

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
@SuppressWarnings({ "unchecked", "rawtypes" })
public class FeatureWizard extends javax.swing.JFrame {
	private static final long serialVersionUID = 1L;
	protected Font defaultLabelFont = new Font("Verdana",0,14); 
	protected static int cellPadding = 5;
	protected static Border defaultBorder = BorderFactory.createLineBorder(Color.BLACK);
	
	// data
	protected static String[] normTitles = NormBaselineEnum.getAllTitles();
	protected java.util.List<JPanel> canonParamList = new ArrayList<JPanel>();
	protected JPanel edParamList;
	protected java.util.List<JPanel> cullParamList = new ArrayList<JPanel>();
	protected FeatureDriver fd;
	protected GUIMain parent;
	protected boolean editMode;
	protected int originalIndex;
	
	// main tabbed pane
	protected JTabbedPane mainJTabbedPane;
	
	// name and description tab
	protected JPanel nameTab;
	protected JLabel nameIntroJLabel;
	protected JButton nameCancelJButton;
	protected JButton nameNextJButton;
	protected JTextField nameJTextField;
	protected JLabel nameJLabel;
	protected JLabel descJLabel;
	protected JTextPane descJTextPane;
	
	// canonicizers tab
	protected JPanel canonTab;
	protected JScrollPane selCanonJScrollPane;
	protected JList selCanonJList;
	protected DefaultComboBoxModel selCanonJListModel;
	protected JLabel canonConfigJLabel;
	protected JLabel selCanonJLabel;
	protected JLabel avCanonJLabel;
	protected JScrollPane canonConfigJScrollPane;
	protected JButton selCanonRemoveJButton;
	protected JLabel canonDescContentJLabel;
	protected JButton canonBackJButton;
	protected JButton canonCancelJButton;
	protected JButton canonNextJButton;
	protected JLabel canonDescJLabel;
	protected JButton avCanonAddJButton;
	protected JList avCanonJList;
	protected JScrollPane avCanonJScrollPane;
	
	// features tab
	protected JPanel featuresTab;
	protected JLabel featuresJLabel;
	protected JScrollPane featuresJScrollPane;
	protected JTree featuresJTree;
	protected DefaultTreeModel featuresTreeModel;
	protected JLabel featuresConfigJLabel;
	protected JScrollPane featuresConfigJScrollPane;
	protected JLabel featureDescJLabel;
	protected JLabel featureDescContentJLabel;
	protected JButton featuresBackJButton;
	protected JButton featuresCancelJButton;
	protected JButton featuresNextJButton;

	// cullers tab
	protected JPanel cullersTab;
	protected JScrollPane selCullJScrollPane;
	protected JList selCullJList;
	protected DefaultComboBoxModel selCullJListModel;
	protected JLabel cullConfigJLabel;
	protected JLabel selCullJLabel;
	protected JLabel avCullJLabel;
	protected JScrollPane cullConfigJScrollPane;
	protected JButton selCullRemoveJButton;
	protected JLabel cullDescContentJLabel;
	protected JTextField normFactorJTextField;
	protected JComboBox normChooserJComboBox;
	protected JButton cullBackJButton;
	protected JButton cullCancelJButton;
	protected JButton cullNextJButton;
	protected JLabel cullDescJLabel;
	protected JButton avCullAddJButton;
	protected JList avCullJList;
	protected JScrollPane avCullJScrollPane;
	
	
	// normalization tab
	protected JPanel normTab;
	protected JLabel normJLabel;
	protected JLabel normFactorJLabel;
	protected JLabel normDescJLabel;
	protected JLabel normDescContentJLabel;
	protected JButton normBackJButton;
	protected JButton normCancelJButton;
	protected JButton normAddFeatureJButton;
	
	/**
	* Auto-generated main method to display this JFrame
	*/
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
				} catch (Exception e) {
					System.err.println("Look-and-Feel error!");
				}
				FeatureWizard inst = new FeatureWizard(new GUIMain());
				inst.setLocationRelativeTo(null);
				inst.setVisible(true);
			}
		});
	}
	
	/**
	 * Default constructor.
	 */
	public FeatureWizard(GUIMain parent) {
		super();
		this.parent = parent;
		initGUI();
		setLocationRelativeTo(parent);
	}
	
	/**
	 * Constructor for editing a selected feature driver.
	 */
	public FeatureWizard(GUIMain parent, FeatureDriver fd, int originalIndex) {
		super();
		this.parent = parent;
		this.editMode = true;
		this.originalIndex = originalIndex;
		initGUI();
		setLocationRelativeTo(parent);
		
		// set view to given feature driver
		// ================================
		// name and description
		nameJTextField.setText(fd.getName());
		descJTextPane.setText(fd.getDescription());
		
		// canonicizers
		List<Canonicizer> canons = fd.getCanonicizers();
		if (canons != null) {
			for (Canonicizer c: canons) {
				selCanonJListModel.addElement(c.displayName());
				String className = c.getClass().getName();
				JPanel paramPanel = FeatureWizardDriver.getConfigPanel(this,1,className);
				canonParamList.add(paramPanel);

				// update parameter values
				if (paramPanel != null) {
					for (Component paramComp: paramPanel.getComponents()) {
						JPanel param = (JPanel) paramComp;
						String name = ((JLabel) param.getComponent(0)).getText();
						String value = c.getParameter(name);
						if (param.getComponent(1) instanceof JTextField) {
							((JTextField) param.getComponent(1)).setText(value);
						} else if (param.getComponent(1) instanceof JComboBox) {
							((JComboBox) param.getComponent(1)).setSelectedItem(value);
						}
					}
				}

			}
		}
		
		// feature extractor
		EventDriver ed = fd.getUnderlyingEventDriver();
		for (int i=0; i<featuresJTree.getRowCount(); i++) {
			featuresJTree.setSelectionRow(i);
			Object[] path = featuresJTree.getSelectionPath().getPath();
			String elem = ((DefaultMutableTreeNode) path[path.length-1]).toString();
			if (elem.equals(ed.displayName()))
				break;
		}
		// update parameter values
		String edClassName = ed.getClass().getName();
		JPanel edParamPanel = FeatureWizardDriver.getConfigPanel(this,1,edClassName);
		edParamList = edParamPanel;

		// update parameter values
		if (edParamPanel != null) {
			for (Component paramComp: edParamPanel.getComponents()) {
				JPanel param = (JPanel) paramComp;
				String name = ((JLabel) param.getComponent(0)).getText();
				String value = ed.getParameter(name);
				if (param.getComponent(1) instanceof JTextField) {
					((JTextField) param.getComponent(1)).setText(value);
				} else if (param.getComponent(1) instanceof JComboBox) {
					JComboBox cb = ((JComboBox) param.getComponent(1));
					for (int i=0; i<cb.getModel().getSize(); i++) {
						cb.setSelectedIndex(i);
						if (((String) cb.getSelectedItem()).equals(value))
							break;
					}
				}
			}
		}
		featuresConfigJScrollPane.setViewportView(edParamList);

		// cullers
		List<EventCuller> cullers = fd.getCullers();
		if (cullers != null) {
			for (EventCuller ec: cullers) {
				selCullJListModel.addElement(ec.displayName());
				String className = ec.getClass().getName();
				JPanel paramPanel = FeatureWizardDriver.getConfigPanel(this,1,className);
				cullParamList.add(paramPanel);

				// update parameter values
				if (paramPanel != null) {
					for (Component paramComp: paramPanel.getComponents()) {
						JPanel param = (JPanel) paramComp;
						String name = ((JLabel) param.getComponent(0)).getText();
						String value = ec.getParameter(name);
						if (param.getComponent(1) instanceof JTextField) {
							((JTextField) param.getComponent(1)).setText(value);
						} else if (param.getComponent(1) instanceof JComboBox) {
							((JComboBox) param.getComponent(1)).setSelectedItem(value);
						}
					}
				}

			}
		}
		
		// normalization
		normChooserJComboBox.setSelectedItem(fd.getNormBaseline().getTitle());
		normFactorJTextField.setText(fd.getNormFactor().toString());
		
		// set add feature button text
		normAddFeatureJButton.setText("Update Feature");
	}
	
	protected void initGUI() {
		
		try {
			setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			{
				setTitle("Feature Configuration");
				setIconImage(new ImageIcon(Thread.currentThread().getClass().getResource(ANONConstants.GRAPHICS_PREFIX+"icon32.jpg")).getImage());
				
				mainJTabbedPane = new JTabbedPane();
				setPreferredSize(new Dimension(800, 500));
				getContentPane().add(mainJTabbedPane);
				
				/* ========
				 * name tab
				 * ========
				 */
				{
					nameTab = new JPanel(new GridLayout(2,1,cellPadding,cellPadding));
					mainJTabbedPane.addTab("Feature Name and Description", nameTab);
					
					{
						// top - description of wizard
						{
							nameIntroJLabel = new JLabel();
							JScrollPane scrollPane = new JScrollPane();
							scrollPane.setViewportView(nameIntroJLabel);
							nameTab.add(scrollPane);
							nameIntroJLabel.setBorder(BorderFactory.createCompoundBorder(
									defaultBorder,BorderFactory.createEmptyBorder(cellPadding,cellPadding,cellPadding,cellPadding)));
							nameIntroJLabel.setVerticalAlignment(JLabel.TOP);
							nameIntroJLabel.setText(
									"<html><p>" +
											"<font size=12pt><b>Creating a Feature</b></font><br>" +
											"To create a feature the following need to be set:" +
											"<ul>" +
											"	<li><b>Name</b>: The name of the feature. Should be unique among all other configured features.</li>" +
											"	<li><b>Description</b>: The description of the feature.</li>" +
											"	<li><b>Text Pre-processing</b> (optional): Pre-processing to apply on the documents prior to feature extraction<br>" +
											"(e.g. lowering case, stripping punctuation etc.).</li>" +
											"	<li><b>Feature Extractors</b>: The feature extractor to be used for parsing. Determines which features are to be extracted<br>" +
											"(e.g. letter bigrams, function words etc.).</li>" +
											"	<li><b>Feature Post-processing</b> (optional): Post processing to apply on the sets of extracted features across all documents<br>" +
											"(e.g. taking only the most n frequent features in the entire training set).</li>" +
											"	<li><b>Normalization</b> (optional): determine a normalization baseline, i.e. some value to divide the absolute frequencies by<br>" +
											"(e.g. the sum of frequencies of the feature across all documents, the number of words in the document etc.). Default is none (i.e.<br>" +
											"non-normalized absolute values). Another optional value is the factoring - a value to multiply the results by for better accuracy. Default is 1.</li>" +
											"</ul>" +
											"</p></html>"
									);
						}
					}
					
					{
						// bottom - feature name and description
						JPanel bottom = new JPanel(new BorderLayout());
						nameTab.add(bottom);
						{
							JPanel panel = new JPanel(new BorderLayout(cellPadding,cellPadding));
							bottom.add(panel,BorderLayout.NORTH);
							{
								nameJLabel = new JLabel();
								panel.add(nameJLabel,BorderLayout.WEST);
								nameJLabel.setText("Feature Name");
								nameJLabel.setFont(defaultLabelFont);
								nameJLabel.setPreferredSize(new java.awt.Dimension(150, 18));
							}
							{
								nameJTextField = new JTextField();
								panel.add(nameJTextField,BorderLayout.CENTER);
								nameJTextField.setPreferredSize(new java.awt.Dimension(247, 18));
							}
						}
						{
							JPanel panel = new JPanel(new BorderLayout(cellPadding,cellPadding));
							bottom.add(panel,BorderLayout.CENTER);
							{
								descJLabel = new JLabel();
								descJLabel.setVerticalAlignment(JLabel.TOP);
								panel.add(descJLabel,BorderLayout.WEST);
								descJLabel.setText("Feature Description");
								descJLabel.setFont(defaultLabelFont);
								descJLabel.setPreferredSize(new java.awt.Dimension(150, 18));
							}
							{
								descJTextPane = new JTextPane();
								JScrollPane scrollPane = new JScrollPane();
								scrollPane.setViewportView(descJTextPane);
								panel.add(scrollPane,BorderLayout.CENTER);
								descJTextPane.setPreferredSize(new java.awt.Dimension(452, 133));
							}
						}
						{
							JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
							bottom.add(panel,BorderLayout.SOUTH);
							{
								nameCancelJButton = new JButton();
								panel.add(nameCancelJButton);
								nameCancelJButton.setText("Cancel");
							}
							{
								nameNextJButton = new JButton();
								panel.add(nameNextJButton);
								nameNextJButton.setText("Next ->");
							}
						}
						
					}
				}
				
				/* ================
				 * canonicizers tab
				 * ================
				 */
				{
					canonTab = new JPanel(new GridLayout(2,1,cellPadding,cellPadding));
					mainJTabbedPane.addTab("Text Pre-processing", canonTab);

					{
						JPanel topPanel = new JPanel(new GridLayout(1,3,cellPadding,cellPadding));
						canonTab.add(topPanel);
						
						// available canonicizers
						// ======================
						{
							JPanel panel = new JPanel(new BorderLayout(cellPadding,cellPadding));
							topPanel.add(panel);
							{
								avCanonJLabel = new JLabel();
								panel.add(avCanonJLabel,BorderLayout.NORTH);
								avCanonJLabel.setText("Available Text Pre-Processing");
								avCanonJLabel.setFont(defaultLabelFont);
							}
							{
								avCanonJScrollPane = new JScrollPane();
								panel.add(avCanonJScrollPane,BorderLayout.CENTER);
								{
									Set<String> canonSet = FeatureWizardDriver.getCanonicizers().keySet();
									String[] avCanonArr = new String[canonSet.size()];
									int i=0;
									for (String displayName: canonSet) {
										avCanonArr[i++] = displayName;
									}
									ListModel avCanonJListModel = 
											new DefaultComboBoxModel(avCanonArr);
									avCanonJList = new JList();
									avCanonJScrollPane.setViewportView(avCanonJList);
									avCanonJList.setModel(avCanonJListModel);
									avCanonJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
								}
							}
							{
								JPanel btnPanel = new JPanel(new GridLayout(1,1,cellPadding,0));
								panel.add(btnPanel,BorderLayout.SOUTH);
								{
									avCanonAddJButton = new JButton();
									btnPanel.add(avCanonAddJButton,BorderLayout.SOUTH);
									avCanonAddJButton.setText("Add->");
								}
							}
						}

						// selected canonicizers
						// =====================
						{
							JPanel panel = new JPanel(new BorderLayout(cellPadding,cellPadding));
							topPanel.add(panel);
							{
								selCanonJLabel = new JLabel();
								panel.add(selCanonJLabel,BorderLayout.NORTH);
								selCanonJLabel.setText("Selected Text Pre-Processing");
								selCanonJLabel.setFont(defaultLabelFont);
							}
							{
								selCanonJScrollPane = new JScrollPane();
								panel.add(selCanonJScrollPane,BorderLayout.CENTER);
								{
									selCanonJListModel = 
											new DefaultComboBoxModel(new String[] {});
									selCanonJList = new JList();
									selCanonJScrollPane.setViewportView(selCanonJList);
									selCanonJList.setModel(selCanonJListModel);
									selCanonJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
								}
							}
							{
								JPanel btnPanel = new JPanel(new GridLayout(1,1,cellPadding,0));
								panel.add(btnPanel,BorderLayout.SOUTH);
								{
									selCanonRemoveJButton = new JButton();
									btnPanel.add(selCanonRemoveJButton);
									selCanonRemoveJButton.setText("Remove");
								}
							}
						}

						// canonicizer configuration
						// =========================
						{
							JPanel panel = new JPanel(new BorderLayout(cellPadding,cellPadding));
							topPanel.add(panel);
							{
								canonConfigJLabel = new JLabel();
								panel.add(canonConfigJLabel,BorderLayout.NORTH);
								canonConfigJLabel.setText("Configuration");
								canonConfigJLabel.setFont(defaultLabelFont);
							}
							{
								canonConfigJScrollPane = new JScrollPane();
								panel.add(canonConfigJScrollPane,BorderLayout.CENTER);
								canonConfigJScrollPane.setBorder(defaultBorder);
							}
						}
					}
					
					{
						JPanel bottomPanel = new JPanel(new BorderLayout(cellPadding,cellPadding));
						canonTab.add(bottomPanel);
						{
							canonDescJLabel = new JLabel();
							canonDescJLabel.setFont(defaultLabelFont);
							bottomPanel.add(canonDescJLabel, BorderLayout.NORTH);
							canonDescJLabel.setText("Description");
						}
						{
							canonDescContentJLabel = new JLabel();
							canonDescContentJLabel.setBorder(defaultBorder);
							canonDescContentJLabel.setVerticalAlignment(JLabel.TOP);
							bottomPanel.add(canonDescContentJLabel, BorderLayout.CENTER);
						}
						{
							JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
							{
								canonBackJButton = new JButton();
								btnPanel.add(canonBackJButton);
								canonBackJButton.setText("<- Back");
							}
							{
								canonCancelJButton = new JButton();
								btnPanel.add(canonCancelJButton);
								canonCancelJButton.setText("Cancel");
							}
							{
								canonNextJButton = new JButton();
								btnPanel.add(canonNextJButton);
								canonNextJButton.setText("Next ->");
								
							}
							bottomPanel.add(btnPanel, BorderLayout.SOUTH);
						}
					}
				}

				/* ============
				 * features tab
				 * ============
				 */
				{
					featuresTab = new JPanel(new GridLayout(2,1,cellPadding,cellPadding));
					mainJTabbedPane.addTab("Feature Extractors", featuresTab);

					{
						JPanel topPanel = new JPanel(new GridLayout(1,2,cellPadding,cellPadding));
						featuresTab.add(topPanel);

						// features
						// ========
						{
							JPanel panel = new JPanel(new BorderLayout(cellPadding,cellPadding));
							topPanel.add(panel);
							{
								featuresJLabel = new JLabel();
								panel.add(featuresJLabel,BorderLayout.NORTH);
								featuresJLabel.setText("Feature Extractor");
								featuresJLabel.setFont(defaultLabelFont);
							}
							{
								featuresJScrollPane = new JScrollPane();
								panel.add(featuresJScrollPane,BorderLayout.CENTER);
								{
									featuresJTree = new JTree();
									featuresJTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
									featuresJScrollPane.setViewportView(featuresJTree);
									
									DefaultMutableTreeNode root = new DefaultMutableTreeNode("Feature Extractors");
									Map<String,Map<String,String>> edMap = FeatureWizardDriver.getEventDrivers();
									DefaultMutableTreeNode typeNode, edNode;
									for (String type: edMap.keySet()) {
										typeNode = new DefaultMutableTreeNode(type);
										root.add(typeNode);
										for (String ed: edMap.get(type).keySet()){
											edNode = new DefaultMutableTreeNode(ed);
											typeNode.add(edNode);
										}
									}
									featuresTreeModel = new DefaultTreeModel(root);
									featuresJTree.setModel(featuresTreeModel);
									int row = 0;
									while (row < featuresJTree.getRowCount())
										featuresJTree.expandRow(row++);
								}
							}
						}

						// feature configuration
						// =====================
						{
							JPanel panel = new JPanel(new BorderLayout(cellPadding,cellPadding));
							topPanel.add(panel);
							{
								featuresConfigJLabel = new JLabel();
								panel.add(featuresConfigJLabel,BorderLayout.NORTH);
								featuresConfigJLabel.setText("Configuration");
								featuresConfigJLabel.setFont(defaultLabelFont);
							}
							{
								featuresConfigJScrollPane = new JScrollPane();
								panel.add(featuresConfigJScrollPane);
								featuresConfigJScrollPane.setBorder(defaultBorder);
							}
						}
					}
					
					{
						JPanel bottomPanel = new JPanel(new BorderLayout(cellPadding,cellPadding));
						featuresTab.add(bottomPanel);
						{
							featureDescJLabel = new JLabel();
							featureDescJLabel.setFont(defaultLabelFont);
							bottomPanel.add(featureDescJLabel, BorderLayout.NORTH);
							featureDescJLabel.setText("Description");
						}
						{
							featureDescContentJLabel = new JLabel();
							featureDescContentJLabel.setBorder(defaultBorder);
							featureDescContentJLabel.setVerticalAlignment(JLabel.TOP);
							bottomPanel.add(featureDescContentJLabel, BorderLayout.CENTER);
						}
						{
							JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
							{
								featuresBackJButton = new JButton();
								btnPanel.add(featuresBackJButton);
								featuresBackJButton.setText("<- Back");
							}
							{
								featuresCancelJButton = new JButton();
								btnPanel.add(featuresCancelJButton);
								featuresCancelJButton.setText("Cancel");
							}
							{
								featuresNextJButton = new JButton();
								btnPanel.add(featuresNextJButton);
								featuresNextJButton.setText("Next ->");
							}
							bottomPanel.add(btnPanel, BorderLayout.SOUTH);
						}
					}
				}
				
				/* ===========
				 * cullers tab
				 * ===========
				 */
				{
					cullersTab = new JPanel(new GridLayout(2,1,cellPadding,cellPadding));
					mainJTabbedPane.addTab("Feature Post-Processing", cullersTab);

					{
						JPanel topPanel = new JPanel(new GridLayout(1,3,cellPadding,cellPadding));
						cullersTab.add(topPanel);

						// available cullers
						// =================
						{
							JPanel panel = new JPanel(new BorderLayout(cellPadding,cellPadding));
							topPanel.add(panel);
							{
								avCullJLabel = new JLabel();
								panel.add(avCullJLabel,BorderLayout.NORTH);
								avCullJLabel.setText("Available Feature Post-Processing");
								avCullJLabel.setFont(defaultLabelFont);
							}
							{
								avCullJScrollPane = new JScrollPane();
								panel.add(avCullJScrollPane,BorderLayout.CENTER);
								{
									Set<String> cullSet = FeatureWizardDriver.getCullers().keySet();
									String[] avCullArr = new String[cullSet.size()];
									int i=0;
									for (String displayName: cullSet) {
										avCullArr[i++] = displayName;
									}
									ListModel avCullJListModel = 
											new DefaultComboBoxModel(avCullArr);
									avCullJList = new JList();
									avCullJScrollPane.setViewportView(avCullJList);
									avCullJList.setModel(avCullJListModel);
									avCullJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
								}
							}
							{
								JPanel btnPanel = new JPanel(new GridLayout(1,1,cellPadding,0));
								panel.add(btnPanel,BorderLayout.SOUTH);
								{
									avCullAddJButton = new JButton();
									btnPanel.add(avCullAddJButton,BorderLayout.SOUTH);
									avCullAddJButton.setText("Add->");
								}
							}
						}

						// selected cullers
						// ================
						{
							JPanel panel = new JPanel(new BorderLayout(cellPadding,cellPadding));
							topPanel.add(panel);
							{
								selCullJLabel = new JLabel();
								panel.add(selCullJLabel,BorderLayout.NORTH);
								selCullJLabel.setText("Selected Feature Post-Processing");
								selCullJLabel.setFont(defaultLabelFont);
							}
							{
								selCullJScrollPane = new JScrollPane();
								panel.add(selCullJScrollPane,BorderLayout.CENTER);
								{
									selCullJListModel = 
											new DefaultComboBoxModel();
									selCullJList = new JList();
									selCullJScrollPane.setViewportView(selCullJList);
									selCullJList.setModel(selCullJListModel);
									selCullJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
								}
							}
							{
								JPanel btnPanel = new JPanel(new GridLayout(1,1,cellPadding,0));
								panel.add(btnPanel,BorderLayout.SOUTH);
								{
									selCullRemoveJButton = new JButton();
									btnPanel.add(selCullRemoveJButton);
									selCullRemoveJButton.setText("Remove");
								}
							}
						}

						// cullers configuration
						// =====================
						{
							JPanel panel = new JPanel(new BorderLayout(cellPadding,cellPadding));
							topPanel.add(panel);
							{
								cullConfigJLabel = new JLabel();
								panel.add(cullConfigJLabel,BorderLayout.NORTH);
								cullConfigJLabel.setText("Configuration");
								cullConfigJLabel.setFont(defaultLabelFont);
							}
							{
								cullConfigJScrollPane = new JScrollPane();
								panel.add(cullConfigJScrollPane,BorderLayout.CENTER);
								cullConfigJScrollPane.setBorder(defaultBorder);
							}
						}
					}

					{
						JPanel bottomPanel = new JPanel(new BorderLayout(cellPadding,cellPadding));
						cullersTab.add(bottomPanel);
						{
							cullDescJLabel = new JLabel();
							cullDescJLabel.setFont(defaultLabelFont);
							bottomPanel.add(cullDescJLabel, BorderLayout.NORTH);
							cullDescJLabel.setText("Description");
						}
						{
							cullDescContentJLabel = new JLabel();
							cullDescContentJLabel.setBorder(defaultBorder);
							cullDescContentJLabel.setVerticalAlignment(JLabel.TOP);
							bottomPanel.add(cullDescContentJLabel, BorderLayout.CENTER);
						}
						{
							JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
							{
								cullBackJButton = new JButton();
								btnPanel.add(cullBackJButton);
								cullBackJButton.setText("<- Back");
							}
							{
								cullCancelJButton = new JButton();
								btnPanel.add(cullCancelJButton);
								cullCancelJButton.setText("Cancel");
							}
							{
								cullNextJButton = new JButton();
								btnPanel.add(cullNextJButton);
								cullNextJButton.setText("Next ->");
							}
							bottomPanel.add(btnPanel, BorderLayout.SOUTH);
						}
					}
				}
				
				/* =================
				 * normalization tab
				 * =================
				 */
				{
					normTab = new JPanel(new BorderLayout(cellPadding,cellPadding));
					mainJTabbedPane.addTab("Normalization", normTab);

					{
						JPanel topPanel = new JPanel(new GridLayout(1,2,cellPadding,cellPadding));
						normTab.add(topPanel,BorderLayout.NORTH);

						// normalization
						// =============
						{
							JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
							topPanel.add(panel);
							{
								normJLabel = new JLabel();
								panel.add(normJLabel);
								normJLabel.setText("Normalization");
								normJLabel.setFont(defaultLabelFont);
							}
							{
								ComboBoxModel normChooserJComboBoxModel = 
										new DefaultComboBoxModel(normTitles);
								normChooserJComboBox = new JComboBox();
								panel.add(normChooserJComboBox);
								normChooserJComboBox.setModel(normChooserJComboBoxModel);
							}
						}
						{
							JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
							topPanel.add(panel);
							{
								normFactorJLabel = new JLabel();
								panel.add(normFactorJLabel);
								normFactorJLabel.setText("Factoring");
								normFactorJLabel.setFont(defaultLabelFont);
							}
							{
								normFactorJTextField = new JTextField();
								panel.add(normFactorJTextField);
								normFactorJTextField.setText("1");
								normFactorJTextField.setPreferredSize(new java.awt.Dimension(200, 20));
							}

						}
					}
					
					{
						JPanel bottomPanel = new JPanel(new BorderLayout(cellPadding,cellPadding));
						normTab.add(bottomPanel,BorderLayout.CENTER);
						{
							normDescJLabel = new JLabel();
							normDescJLabel.setFont(defaultLabelFont);
							bottomPanel.add(normDescJLabel, BorderLayout.NORTH);
							normDescJLabel.setText("Description");
						}
						{
							normDescContentJLabel = new JLabel("<html><p>"+NormBaselineEnum.NONE.getDescription()+"</p></html>");
							normDescContentJLabel.setBorder(BorderFactory.createCompoundBorder(
									defaultBorder,BorderFactory.createEmptyBorder(cellPadding,cellPadding,cellPadding,cellPadding)));
							normDescContentJLabel.setVerticalAlignment(JLabel.TOP);
							bottomPanel.add(normDescContentJLabel, BorderLayout.CENTER);
						}
						{
							JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
							{
								normBackJButton = new JButton();
								btnPanel.add(normBackJButton);
								normBackJButton.setText("<- Back");
							}
							{
								normCancelJButton = new JButton();
								btnPanel.add(normCancelJButton);
								normCancelJButton.setText("Cancel");
							}
							{
								normAddFeatureJButton = new JButton();
								btnPanel.add(normAddFeatureJButton);
								normAddFeatureJButton.setText("Add Feature");
							}
							bottomPanel.add(btnPanel, BorderLayout.SOUTH);
						}
					}
				}
				
			}
			
			// initialzie listeners
			FeatureWizardDriver.initListeners(this);
			
			pack();
		} catch (Exception e) {
		    //add your error handling code here
			e.printStackTrace();
		}
	}
}
