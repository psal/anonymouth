package edu.drexel.psal.anonymouth.gooie;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.bayes.NaiveBayesMultinomial;
import weka.classifiers.functions.Logistic;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.functions.SMO;
import weka.classifiers.lazy.IBk;
import weka.classifiers.rules.ZeroR;
import weka.classifiers.trees.J48;
import weka.core.OptionHandler;

import com.jgaap.generics.Canonicizer;
import com.jgaap.generics.EventCuller;
import com.jgaap.generics.Pair;
import com.jgaap.generics.Parameterizable;

import edu.drexel.psal.jstylo.GUI.DocsTabDriver.ExtFilter;
import edu.drexel.psal.jstylo.generics.CumulativeFeatureDriver;
import edu.drexel.psal.jstylo.generics.FeatureDriver;
import edu.drexel.psal.jstylo.generics.Logger;
import edu.drexel.psal.jstylo.generics.FeatureDriver.ParamTag;
import edu.drexel.psal.jstylo.generics.Logger.LogOut;

public class PreProcessAdvancedDriver {

	//Constants
	private final String NAME = "( PreProcessAdvancedDriver ) - ";

	//Class Instances
	private PreProcessWindow preProcessWindow;
	private PreProcessAdvancedWindow advancedWindow;
	private GUIMain main;

	//Variables
	protected Classifier tmpClassifier;
	protected Hashtable<String, String> fullClassPath;
	protected Hashtable<String, String> shortClassName;

	public PreProcessAdvancedDriver(PreProcessWindow preProcessWindow, PreProcessAdvancedWindow advancedWindow, GUIMain main) {
		this.preProcessWindow = preProcessWindow;
		this.advancedWindow = advancedWindow;
		this.main = main;
		initListeners();
	}

	private void initListeners() {
		initFeatureListeners();
		initClassifierListeners();
	}

	public void updateFeatPrepColor(GUIMain main) {
		if (preProcessWindow.featuresAreReady()) {
			preProcessWindow.prepFeatLabel.setBackground(main.ready);
			advancedWindow.prepFeatLabel.setBackground(main.ready);
		} else {
			preProcessWindow.prepFeatLabel.setBackground(main.notReady);
			advancedWindow.prepFeatLabel.setBackground(main.notReady);
		}	
	}

	public void updateClassPrepColor(GUIMain main) {
		if (preProcessWindow.classifiersAreReady()) {
			preProcessWindow.prepClassLabel.setBackground(main.ready);
			advancedWindow.prepClassLabel.setBackground(main.ready);
		} else {
			preProcessWindow.prepClassLabel.setBackground(main.notReady);
			advancedWindow.prepClassLabel.setBackground(main.notReady);
		}	
	}

	private void initFeatureListeners() {
		advancedWindow.featuresSetJComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Logger.logln(NAME+"Preset feature set selected in the features tab.");

				int selected = advancedWindow.featuresSetJComboBox.getSelectedIndex() - 1;
				preProcessWindow.featureDrivers = main.presetCFDs.get(selected+1);
				Logger.logln(NAME+"loaded preset feature set: "+preProcessWindow.featureDrivers.getName());
				
				preProcessWindow.featuresSetJComboBox.setSelectedIndex(selected+1);
				advancedWindow.featuresSetJComboBox.setSelectedIndex(selected+1);
				preProcessWindow.driver.updateFeatureSetView(main);
				advancedWindow.advancedDriver.updateFeatPrepColor(main);
			}
		});

		advancedWindow.featuresNewSetJButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Logger.logln(NAME+"'New Feature Set' button clicked in the features tab.");

				//Check if the current FeatureDrivers CFD is not empty
				int answer;
				if (!preProcessWindow.isFeatureDriversEmpty(preProcessWindow.featureDrivers) && PropertiesUtil.getWarnAll()) {
					answer = JOptionPane.showConfirmDialog(main,
							"Are you sure you want to override current feature set?",
							"New Feature Set",
							JOptionPane.YES_NO_OPTION);
				} else {
					answer = JOptionPane.YES_OPTION;
				}

				if (answer == JOptionPane.YES_OPTION) {
					preProcessWindow.featureDrivers = new CumulativeFeatureDriver();
					advancedWindow.featuresSetJComboBox.setSelectedIndex(0);
					preProcessWindow.driver.updateFeatureSetView(main);
				}
			}
		});

		advancedWindow.featuresAddSetJButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Logger.logln(NAME+"'Add Feature Set' button clicked in the features tab.");

				if (preProcessWindow.featureDrivers.getName() == null || preProcessWindow.featureDrivers.getName().matches("\\s*")) {
					JOptionPane.showMessageDialog(main,
							"Feature set must have at least a name to be added.",
							"Add Feature Set",
							JOptionPane.ERROR_MESSAGE);
					return;
				}

				//Check that doesn't exist
				for (int i=0; i<advancedWindow.featuresSetJComboBoxModel.getSize(); i++) {
					if (preProcessWindow.featureDrivers.getName().equals(advancedWindow.featuresSetJComboBoxModel.getElementAt(i))) {
						JOptionPane.showMessageDialog(main,
								"Feature set with the given name already exists.",
								"Add Feature Set",
								JOptionPane.INFORMATION_MESSAGE);
						return;
					}
				}

				main.presetCFDs.add(preProcessWindow.featureDrivers);
				advancedWindow.featuresSetJComboBoxModel.addElement(preProcessWindow.featureDrivers.getName());
				advancedWindow.featuresSetJComboBox.setSelectedIndex(advancedWindow.featuresSetJComboBoxModel.getSize()-1);
			}
		});

		advancedWindow.featuresLoadSetFromFileJButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Logger.logln(NAME+"'Import from XML' button clicked in the features tab.");

				JFileChooser load = new JFileChooser(new File("."));
				load.addChoosableFileFilter(new ExtFilter("XML files (*.xml)", "xml"));
				int answer = load.showOpenDialog(main);

				if (answer == JFileChooser.APPROVE_OPTION) {
					String path = load.getSelectedFile().getAbsolutePath();
					Logger.logln(NAME+"Trying to load cumulative feature driver from "+path);
					try {
						CumulativeFeatureDriver cfd = new CumulativeFeatureDriver(path);
						for (int i=0; i<advancedWindow.featuresSetJComboBoxModel.getSize(); i++) {
							if (cfd.getName().equals(advancedWindow.featuresSetJComboBoxModel.getElementAt(i))) {
								advancedWindow.featuresSetJComboBoxModel.removeElementAt(i);
							}
						}

						preProcessWindow.featureDrivers = cfd;
						main.presetCFDs.add(cfd);
						advancedWindow.featuresSetJComboBoxModel.addElement(cfd.getName());
						advancedWindow.featuresSetJComboBox.setSelectedIndex(advancedWindow.featuresSetJComboBoxModel.getSize()-1);
						preProcessWindow.driver.updateFeatureSetView(main);
					} catch (Exception exc) {
						Logger.logln(NAME+"Failed loading "+path, LogOut.STDERR);
						Logger.logln(NAME+exc.toString(),LogOut.STDERR);
						JOptionPane.showMessageDialog(null,
								"Failed loading feature set from:\n"+path,
								"Load Feature Set Failure",
								JOptionPane.ERROR_MESSAGE);
					}
				} else {
					Logger.logln(NAME+"Load cumulative feature driver canceled");
				}
			}
		});

		advancedWindow.featuresSaveSetJButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Logger.logln(NAME+"'Save Feature Set...' button clicked in the features tab.");

				JFileChooser save = new JFileChooser(new File("."));
				save.addChoosableFileFilter(new ExtFilter("XML files (*.xml)", "xml"));
				int answer = save.showSaveDialog(main);

				if (answer == JFileChooser.APPROVE_OPTION) {
					File f = save.getSelectedFile();
					String path = f.getAbsolutePath();
					if (!path.toLowerCase().endsWith(".xml"))
						path += ".xml";
					try {
						BufferedWriter bw = new BufferedWriter(new FileWriter(path));
						bw.write(preProcessWindow.featureDrivers.toXMLString());
						bw.flush();
						bw.close();
						Logger.log("Saved cumulative feature driver to "+path+":\n"+preProcessWindow.featureDrivers.toXMLString());
					} catch (IOException exc) {
						Logger.logln(NAME+"Failed opening "+path+" for writing",LogOut.STDERR);
						Logger.logln(NAME+exc.toString(),LogOut.STDERR);
						JOptionPane.showMessageDialog(null,
								"Failed saving feature set set into:\n"+path,
								"Save Feature Set Failure",
								JOptionPane.ERROR_MESSAGE);
					}
				} else {
					Logger.logln(NAME+"Save cumulative feature driver canceled");
				}
			}
		});

		advancedWindow.featuresSetDescJTextPane.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent arg0) {
				Logger.logln(NAME+"Feature set description edited in the features tab.");
				preProcessWindow.featureDrivers.setDescription(advancedWindow.featuresSetDescJTextPane.getText());
			}

			@Override
			public void focusGained(FocusEvent arg0) {}
		});

		advancedWindow.featuresJList.addListSelectionListener(new ListSelectionListener() {
			int lastSelected = -2;
			
			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				int selected = advancedWindow.featuresJList.getSelectedIndex();
				//Skip if already processed
				if (selected == lastSelected)
					return;
				Logger.logln(NAME+"Feature selected in the features tab: "+advancedWindow.featuresJList.getSelectedValue());
				advancedWindow.advancedDriver.updateFeatureView(main, selected);
				lastSelected = selected;
			}
		});

		advancedWindow.featuresCanonJTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			int lastSelected = -2;
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				int selected = advancedWindow.featuresCanonJTable.getSelectedRow();
				
				if (selected == lastSelected) {
					return;
				} else if (selected == -1) {
					Logger.logln(NAME+"Canonicizer unselected in features tab.");
					advancedWindow.featuresCanonConfigJTableModel.getDataVector().removeAllElements();
				} else {
					Canonicizer c = preProcessWindow.featureDrivers.featureDriverAt(advancedWindow.featuresJList.getSelectedIndex()).canonicizerAt(selected);
					Logger.logln(NAME+"Canonicizer '"+c.displayName()+"' selected in features tab.");
					advancedWindow.advancedDriver.populateTableWithParams(c, advancedWindow.featuresCanonConfigJTableModel);
				}

				lastSelected = selected;
			}
		});

		advancedWindow.featuresCullJTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			int lastSelected = -2;
			@Override
			public void valueChanged(ListSelectionEvent e) {
				int selected = advancedWindow.featuresCullJTable.getSelectedRow();

				if (selected == lastSelected) {
					return;
				} else if (selected == -1) {
					Logger.logln(NAME+"Culler unselected in features tab.");
					advancedWindow.featuresCullConfigJTableModel.getDataVector().removeAllElements();
				} else {
					EventCuller ec = preProcessWindow.featureDrivers.featureDriverAt(advancedWindow.featuresJList.getSelectedIndex()).cullerAt(selected);
					Logger.logln(NAME+"Culler '"+ec.displayName()+"' selected in features tab.");
					populateTableWithParams(ec, advancedWindow.featuresCullConfigJTableModel);
				}

				lastSelected = selected;
			}
		});
	}

	private void initClassifierListeners() {
		advancedWindow.classJTree.addTreeSelectionListener(new TreeSelectionListener() {	
			@Override
			public void valueChanged(TreeSelectionEvent arg0) {
				//If unselected
				if (advancedWindow.classJTree.getSelectionCount() == 0) {
					Logger.logln(NAME+"Classifier tree unselected in the classifiers tab.");
					//ResetAvClassSelection(main);
					tmpClassifier = null;
					return;
				}

				//Unselect selected list
				advancedWindow.classJList.clearSelection();

				Object[] path = advancedWindow.classJTree.getSelectionPath().getPath();
				DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)path[path.length-1];

				//If selected a classifier
				if (selectedNode.isLeaf()) {
					Logger.logln(NAME+"Classifier selected in the available classifiers tree in the classifiers tab: "+selectedNode.toString());

					String className = getClassNameFromPath(path);
					tmpClassifier = null;
					try {
						tmpClassifier = (Classifier)Class.forName(className).newInstance();
					} catch (Exception e) {
						Logger.logln(NAME+"Could not create classifier out of class: "+className);
						JOptionPane.showMessageDialog(advancedWindow,
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


					//Show options and description
					advancedWindow.classAvClassArgsJTextField.setText(getOptionsStr(((OptionHandler)tmpClassifier).getOptions())+dashM);
					advancedWindow.classDescJTextPane.setText(getDesc(tmpClassifier));
				} else {
					tmpClassifier = null;
				}
			}
		});

		advancedWindow.classAddJButton.addActionListener(new ActionListener() {	
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Logger.logln(NAME+"'Add' button clicked in the analysis tab.");

				//Check if classifier is selected
				if (tmpClassifier == null) {
					if (PropertiesUtil.getWarnAll()) {
						JOptionPane.showMessageDialog(advancedWindow,
								"You must select a classifier to be added.",
								"Add Classifier Error",
								JOptionPane.ERROR_MESSAGE);
					}
					return;
				} else if (preProcessWindow.classifiers.size() > 0) {
					JOptionPane.showMessageDialog(advancedWindow,
							"It is only possible to select one classifier at a time.",
							"Add Classifier Error",
							JOptionPane.ERROR_MESSAGE);	
					return;
				} else {
					//Check classifier options
					try {
						((OptionHandler)tmpClassifier).setOptions(advancedWindow.classAvClassArgsJTextField.getText().split(" "));
					} catch (Exception e) {
						Logger.logln(NAME+"Invalid options given for classifier.",LogOut.STDERR);
						JOptionPane.showMessageDialog(advancedWindow,
								"The classifier arguments entered are invalid.\n"+
										"Restoring original options.",
										"Classifier Options Error",
										JOptionPane.ERROR_MESSAGE);
						return;
					}

					//Add classifier
					preProcessWindow.classifiers.add(tmpClassifier);
					preProcessWindow.classChoice.setSelectedItem(shortClassName.get(tmpClassifier.getClass().getName()));
					advancedWindow.advancedDriver.updateClassList(main);
					advancedWindow.advancedDriver.updateClassPrepColor(main);
					tmpClassifier = null;
					advancedWindow.classJTree.clearSelection();
				}
			}
		});

		advancedWindow.classJList.addListSelectionListener(new ListSelectionListener() {
			int lastSelected = -2;

			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				int selected = advancedWindow.classJList.getSelectedIndex();
				if (selected == lastSelected)
					return;
				lastSelected = selected;

				//If unselected
				if (selected == -1) {
					Logger.logln(NAME+"Classifier list unselected in the classifiers tab.");
					tmpClassifier = null;
					return;
				}

				//Unselect available classifiers tree
				advancedWindow.classJTree.clearSelection();

				String className = advancedWindow.classJList.getSelectedValue().toString();
				Logger.logln(NAME+"Classifier selected in the selected classifiers list in the classifiers tab: "+className);

				//Show options and description
				if(className.toLowerCase().contains("smo"))
					advancedWindow.classSelClassArgsJTextField.setText(getOptionsStr(((OptionHandler)preProcessWindow.classifiers.get(selected)).getOptions())+" -M");
				else
					advancedWindow.classSelClassArgsJTextField.setText(getOptionsStr(((OptionHandler)preProcessWindow.classifiers.get(selected)).getOptions()));
				advancedWindow.classDescJTextPane.setText(getDesc(preProcessWindow.classifiers.get(selected)));
				advancedWindow.classAvClassArgsJTextField.setText("");
			}
		});

		advancedWindow.classRemoveJButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Logger.log("'Remove' button clicked in the classifiers tab.");
				int selected = advancedWindow.classJList.getSelectedIndex();

				//Check if selected
				if (selected == -1) {
					if (PropertiesUtil.getWarnAll()) {
						JOptionPane.showMessageDialog(main,
								"You must select a classifier to be removed.",
								"Remove Classifier Error",
								JOptionPane.ERROR_MESSAGE);
					}
					return;
				}

				//Remove classifier
				preProcessWindow.classifiers.remove(selected);

				advancedWindow.classSelClassArgsJTextField.setText("");
				advancedWindow.classDescJTextPane.setText("");
				advancedWindow.classAvClassArgsJTextField.setText("");
				preProcessWindow.classChoice.setSelectedIndex(-1);
				advancedWindow.advancedDriver.updateClassList(main);
				advancedWindow.advancedDriver.updateClassPrepColor(main);
			}
		});
	}

	/**
	 * Resets the feature view in the features tab.
	 */
	protected void clearFeatureView(GUIMain main) {
		advancedWindow.featuresFeatureNameJTextPane.setText("");
		advancedWindow.featuresFeatureDescJTextPane.setText("");
		advancedWindow.featuresFeatureExtractorContentJTableModel.getDataVector().removeAllElements();
		advancedWindow.featuresFeatureExtractorConfigJTableModel.getDataVector().removeAllElements();
		advancedWindow.featuresCanonJTableModel.getDataVector().removeAllElements();
		advancedWindow.featuresCanonConfigJTableModel.getDataVector().removeAllElements();
		advancedWindow.featuresCullJTableModel.getDataVector().removeAllElements();
		advancedWindow.featuresCullConfigJTableModel.getDataVector().removeAllElements();
		advancedWindow.featuresNormContentJTextPane.setText("");
		advancedWindow.featuresFactorContentJTextPane.setText("");
	}

	/**
	 * Updates the feature view when a feature is selected in the features tab.
	 */
	protected void updateFeatureView(GUIMain main, int selected) {
		clearFeatureView(main);

		//If nothing's selected, return
		if (selected == -1)
			return;

		FeatureDriver fd = preProcessWindow.featureDrivers.featureDriverAt(selected);

		//Name and description
		advancedWindow.featuresFeatureNameJTextPane.setText(fd.getName());
		advancedWindow.featuresFeatureDescJTextPane.setText(fd.getDescription());

		//Update normalization
		advancedWindow.featuresNormContentJTextPane.setText(fd.getNormBaseline().getTitle());
		advancedWindow.featuresFactorContentJTextPane.setText(fd.getNormFactor().toString());

		//Update feature extractor
		advancedWindow.featuresFeatureExtractorContentJTableModel.addRow(new String[] {fd.getUnderlyingEventDriver().displayName()});
		populateTableWithParams(fd.getUnderlyingEventDriver(), advancedWindow.featuresFeatureExtractorConfigJTableModel);

		//Update canonicizers
		List<Canonicizer> canons = fd.getCanonicizers();
		if (canons != null) {
			for (int i = 0; i < canons.size(); i++) {
				advancedWindow.featuresCanonJTableModel.addRow(new String[]{canons.get(i).displayName()});
				populateTableWithParams(canons.get(i), advancedWindow.featuresCanonConfigJTableModel);
			}
		} else {
			advancedWindow.featuresCanonJTableModel.addRow(new String[]{"N/A"});
			advancedWindow.featuresCanonConfigJTableModel.addRow(new String[]{"N/A", "N/A", "N/A"});
		}

		// update cullers
		List<EventCuller> cullers = fd.getCullers();
		if (cullers != null) {
			for (int i = 0; i < cullers.size(); i++) {
				advancedWindow.featuresCullJTableModel.addRow(new String[]{cullers.get(i).displayName()});
				populateTableWithParams(cullers.get(i), advancedWindow.featuresCullConfigJTableModel);
			}
		} else {
			advancedWindow.featuresCullJTableModel.addRow(new String[]{"N/A"});
			advancedWindow.featuresCullConfigJTableModel.addRow(new String[]{"N/A", "N/A", "N/A"});
		}
	}

	/**
	 * Populates the given tableModel with parameters and their values for the given event driver / canonicizer / culler.
	 * Assumes the table is set to have three columns.
	 */
	protected void populateTableWithParams(Parameterizable p, DefaultTableModel tm) {
		String fullname = p.getClass().getName();
		List<Pair<String,ParamTag>> params = FeatureDriver.getClassParams(fullname);

		boolean allParamsNull = true;

		for (Pair<String,ParamTag> param: params) {
			if (param != null)
				allParamsNull = false;
			else
				continue;
		}

		if (!allParamsNull) {
			for (Pair<String,ParamTag> param: params) 
				tm.addRow(new String[] {fullname.substring(fullname.lastIndexOf(".")+1), param.getFirst(), p.getParameter(param.getFirst())});
		} else {
			tm.addRow(new String[] {fullname.substring(fullname.lastIndexOf(".")+1), "N/A", "N/A"});
		}
	}

	/**
	 * Updates the list of selected classifiers with respect to the list of classifiers.
	 */
	protected void updateClassList(GUIMain main) {
		DefaultListModel<String> model2 = (DefaultListModel<String>)advancedWindow.classJList.getModel();
		List<Classifier> classifiers = preProcessWindow.classifiers;

		model2.removeAllElements();
		for (Classifier c: classifiers) {
			String className = c.getClass().getName();
			model2.addElement(className);
		}
	}

	/**
	 * Clears the GUI when no available classifier is selected.
	 */
	protected void resetAvClassSelection(GUIMain main) {
		// clear everything
		tmpClassifier = null;
		main.classAvClassArgsJTextField.setText("");
		main.classDescJTextPane.setText("");
	}

	/**
	 * Clears the GUI when no selected classifier is selected.
	 */
	protected void resetSelClassSelection(GUIMain main) {
		// clear everything
		main.classSelClassArgsJTextField.setText("");
		main.classDescJTextPane.setText("");
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


	/**
	 * Constructs the class name out of a tree path.
	 */
	protected String getClassNameFromPath(Object[] path) {
		String res = "";
		for (Object o: path) {
			res += o.toString()+".";
		}
		res = res.substring(0,res.length()-1);
		return res;
	}

	/**
	 * build classifiers tree from list of class names
	 */
	public String[] classNames = new String[] {
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

	/**
	 * Initialize available classifiers tree
	 */
	protected void initMainWekaClassifiersTree(GUIMain main) {	
		Boolean shouldAdd = false;
		fullClassPath = new Hashtable<String, String>();
		shortClassName = new Hashtable<String, String>();

		for (String className: classNames) {
			String[] nameArr = className.split("\\.");

			for (int i = 2; i < nameArr.length; i++) {
				if (shouldAdd) {
					preProcessWindow.classChoice.addItem(nameArr[i]);
					fullClassPath.put(nameArr[i], className);
					shortClassName.put(className, nameArr[i]);
					shouldAdd = false;
				} else {
					shouldAdd = true;
				}
			}
		}
	}

	/**
	 * Initialize available classifiers tree
	 */
	protected void initAdvWekaClassifiersTree(PreProcessAdvancedWindow PPSP) {
		//Create root and set to tree
		DefaultMutableTreeNode wekaNode = new DefaultMutableTreeNode("weka");
		DefaultMutableTreeNode classifiersNode = new DefaultMutableTreeNode("classifiers");
		wekaNode.add(classifiersNode);
		DefaultTreeModel model = new DefaultTreeModel(wekaNode);
		PPSP.classJTree.setModel(model);

		//Add all classes
		DefaultMutableTreeNode currNode, child;
		for (String className: classNames) {
			String[] nameArr = className.split("\\.");
			currNode = classifiersNode;

			for (int i=2; i<nameArr.length; i++) {
				// look for node
				@SuppressWarnings("unchecked")
				Enumeration<DefaultMutableTreeNode> children = currNode.children();
				while (children.hasMoreElements()) {
					child = children.nextElement();
					if (child.getUserObject().toString().equals(nameArr[i])) {
						currNode = child;
						break;
					}
				}

				// if not found, create a new one
				if (!currNode.getUserObject().toString().equals(nameArr[i])) {
					child = new DefaultMutableTreeNode(nameArr[i]);
					currNode.add(child);
					currNode = child;
				}
			}
		}

		// expand tree
		int row = 0;
		while (row < PPSP.classJTree.getRowCount())
			PPSP.classJTree.expandRow(row++);
	}

	/**
	 * Initialize map of classifier class-name to its description.
	 */
	protected String getDesc(Classifier c) {
		// bayes
		if (c instanceof NaiveBayes) {
			return ((NaiveBayes) c).globalInfo();
		} else if (c instanceof NaiveBayesMultinomial) {
			return ((NaiveBayesMultinomial) c).globalInfo();
		}

		// functions
		else if (c instanceof Logistic) {
			return ((Logistic) c).globalInfo();
		}
		else if (c instanceof MultilayerPerceptron) {
			return ((MultilayerPerceptron) c).globalInfo();
		}
		else if (c instanceof SMO) {
			return ((SMO) c).globalInfo();
		}

		// lazy
		else if (c instanceof IBk) {
			return ((IBk) c).globalInfo();
		}

		// rules
		else if (c instanceof ZeroR) {
			return ((ZeroR) c).globalInfo();
		}

		// trees
		else if (c instanceof J48) {
			return ((J48) c).globalInfo();
		}

		else {
			return "No description available.";
		}
	}
}
