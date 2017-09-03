package edu.drexel.psal.anonymouth.gooie;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import weka.classifiers.Classifier;
import weka.core.OptionHandler;

import com.jgaap.generics.Canonicizer;
import com.jgaap.generics.EventCuller;
import com.jgaap.generics.Pair;
import com.jgaap.generics.Parameterizable;

import edu.drexel.psal.anonymouth.helpers.ScrollToTop;
import edu.drexel.psal.jstylo.generics.CumulativeFeatureDriver;
import edu.drexel.psal.jstylo.generics.FeatureDriver;
import edu.drexel.psal.jstylo.generics.Logger;
import edu.drexel.psal.jstylo.generics.FeatureDriver.ParamTag;
import edu.drexel.psal.jstylo.generics.Logger.LogOut;

/**
 * The corresponding "Driver" class for the "Window" PreProcessAdvancedWindow.
 * Handles all listeners and most update methods relating to the window
 * 
 * @author Marc Barrowclift
 */
public class PreProcessAdvancedDriver {

	//Constants
	private final String NAME = "( PreProcessAdvancedDriver ) - ";

	//Variables
	private PreProcessAdvancedWindow advancedWindow;
	public CumulativeFeatureDriver cfd;
	private Classifier testClassifier;

	//Listeners
	private ChangeListener tabbedPaneListener;
	private WindowListener advancedWindowListener;
	//Feature Set
	private ActionListener featureChoiceListener;
	private ActionListener featureViewInfoListener;
	private ActionListener featureOKListener;
	private ActionListener featureRestoreDefaultsListener;
	//View Info Window
	private ListSelectionListener viewInfoListListener;
	private ListSelectionListener viewInfoCanonTableListener;
	private ListSelectionListener viewInfoCullTableListener;
	//Classifier
	private ActionListener classChoiceListener;
	private ActionListener classEditListener;
	private ActionListener classOKListener;
	private ActionListener classRestoreDefaultsListener;
	//Argument Window
	private ActionListener argsOKListener;
	private ActionListener argsCancelListener;
	private DocumentListener argsFieldListener;

	/**
	 * CONSTRUCTOR
	 * @param advancedWindow - PreProcessAdvancedWindow instance, so we can access the swing components
	 * @param main - GUIMain instance
	 */
	public PreProcessAdvancedDriver(PreProcessAdvancedWindow advancedWindow) {
		this.advancedWindow = advancedWindow;
		initListeners();
	}

	/**
	 * Initializes all the listeners and adds them to the respective components
	 */
	private void initListeners() {
		initFeatureListeners();
		initViewInfoListeners();
		initArgListeners();
		initClassifierListeners();
		initOtherListeners();
	}

	/**
	 * Initializes all the "Feature Set" tab listeners
	 */
	private void initFeatureListeners() {
		featureChoiceListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (advancedWindow.isVisible()) {
					Logger.logln(NAME+"Feature changed in the features tab.");
					updateFeatureSetTab();
				}
			}
		};
		advancedWindow.featureChoice.addActionListener(featureChoiceListener);
		
		featureViewInfoListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Logger.logln(NAME+"View Info button clicked for feature " + (String)advancedWindow.featureChoice.getSelectedItem());
				advancedWindow.viewInfoFrame.setTitle("Viewing " + (String)advancedWindow.featureChoice.getSelectedItem() + " Information");
				advancedWindow.viewInfoFrame.setVisible(true);
				advancedWindow.viewInfoList.setSelectedIndex(0);
			}
		};
		advancedWindow.featureViewInfoButton.addActionListener(featureViewInfoListener);
		
		featureOKListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Logger.logln(NAME+"OK button clicked in feature tab");
				advancedWindow.setVisible(false);
			}
		};
		advancedWindow.featureOKButton.addActionListener(featureOKListener);
		
		featureRestoreDefaultsListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Logger.logln(NAME+"Restore Defaults button clicked in the feature tab");
				advancedWindow.featureChoice.setSelectedItem(PropertiesUtil.defaultFeat);
				updateFeatureSetTab();
			}
		};
		advancedWindow.featureRestoreDefaultsButton.addActionListener(featureRestoreDefaultsListener);
	}
	
	/**
	 * Initializes all the "View ${FEATURE_SET} Info" window listeners
	 */
	private void initViewInfoListeners() {
		viewInfoListListener = new ListSelectionListener() {
			int lastSelected = -2;
			
			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				int selected = advancedWindow.viewInfoList.getSelectedIndex();
				//Skip if already processed
				if (selected == lastSelected)
					return;
				Logger.logln(NAME+"Feature selected in the view info window: "+advancedWindow.viewInfoList.getSelectedValue());
				updateViewInfoWindow(selected);
				lastSelected = selected;
			}
		};
		advancedWindow.viewInfoList.addListSelectionListener(viewInfoListListener);

		viewInfoCanonTableListener = new ListSelectionListener() {
			int lastSelected = -2;
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				int selected = advancedWindow.viewInfoCanonTable.getSelectedRow();

				if (selected == lastSelected) {
					return;
				}
				
				advancedWindow.viewInfoCanonConfigTableModel.getDataVector().removeAllElements();
				
				if (selected == -1) {
					Logger.logln(NAME+"Canonicizer unselected in features tab.");
				} else {
					Canonicizer c = cfd.featureDriverAt(advancedWindow.viewInfoList.getSelectedIndex()).canonicizerAt(selected);
					if (c == null)
						return;
					
					Logger.logln(NAME+"Canonicizer '"+c.displayName()+"' selected in features tab.");
					populateTableWithParams(c, advancedWindow.viewInfoCanonConfigTableModel);
				}

				advancedWindow.viewInfoCanonConfigTable.revalidate();
				advancedWindow.viewInfoCanonConfigTable.repaint();
				lastSelected = selected;
			}
		};
		advancedWindow.viewInfoCanonTable.getSelectionModel().addListSelectionListener(viewInfoCanonTableListener);
		
		viewInfoCullTableListener = new ListSelectionListener() {
			int lastSelected = -2;
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				int selected = advancedWindow.viewInfoCullTable.getSelectedRow();

				if (selected == lastSelected) {
					return;
				}
				
				advancedWindow.viewInfoCullConfigTableModel.getDataVector().removeAllElements();
				
				if (selected == -1) {
					Logger.logln(NAME+"Culler unselected in features tab.");
				} else {
					EventCuller ec = cfd.featureDriverAt(advancedWindow.viewInfoList.getSelectedIndex()).cullerAt(selected);
					Logger.logln(NAME+"Culler '"+ec.displayName()+"' selected in features tab.");
					populateTableWithParams(ec, advancedWindow.viewInfoCullConfigTableModel);
				}

				advancedWindow.viewInfoCullConfigTable.revalidate();
				advancedWindow.viewInfoCullConfigTable.repaint();
				lastSelected = selected;
			}
		};
		advancedWindow.viewInfoCullTable.getSelectionModel().addListSelectionListener(viewInfoCullTableListener);
	}

	/**
	 * Initializes all the "Classifier" tab listeners
	 */
	private void initClassifierListeners() {
		classChoiceListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (advancedWindow.isVisible()) {
					Logger.logln(NAME+"Classifier changed in the classifier tab.");
					updateClassifierTab();
				}
			}
		};
		advancedWindow.classChoice.addActionListener(classChoiceListener);
		
		classEditListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Logger.logln(NAME+"Edit button clicked in the classifier tab");
				advancedWindow.argsFrame.setTitle("Edit " + (String)advancedWindow.classChoice.getSelectedItem() + "'s Arguments");
				advancedWindow.argsOKButton.setEnabled(true);
				advancedWindow.argsFrame.setVisible(true);
				advancedWindow.argsOKButton.requestFocusInWindow();
			}
		};
		advancedWindow.classEditButton.addActionListener(classEditListener);
		
		classOKListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Logger.logln(NAME+"OK button clicked in classifier tab");
				advancedWindow.setVisible(false);
			}
		};
		advancedWindow.classOKButton.addActionListener(classOKListener);
		
		classRestoreDefaultsListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Logger.logln(NAME+"Restore Defaults button clicked in the classifier tab");
				advancedWindow.classChoice.setSelectedItem(PropertiesUtil.defaultClass);
				updateClassifierTab();
			}
		};
		advancedWindow.classRestoreDefaultsButton.addActionListener(classRestoreDefaultsListener);
	}
	
	/**
	 * Initializes all the "Edit ${CLASSIFIER}'s Arguments" window listeners
	 */
	private void initArgListeners() {
		argsFieldListener = new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				if (advancedWindow.argsFrame.isVisible())
					updateArgsOKButton();
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
				if (advancedWindow.argsFrame.isVisible())
					updateArgsOKButton();
			}
			@Override
			public void changedUpdate(DocumentEvent e) {}
		};
		advancedWindow.argsField.getDocument().addDocumentListener(argsFieldListener);
		
		argsOKListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Logger.logln(NAME+"Ok button clicked in the Arguments Editor");
				
				String options = advancedWindow.argsField.getText();
				try {
					((OptionHandler)advancedWindow.classifiers.get(0)).setOptions(options.split(" "));
				} catch (Exception e1) {
					Logger.logln(NAME+"Problem occured while trying to set user defined arguments, shouldn't have got here.", LogOut.STDERR);
				}
				
				advancedWindow.argsFrame.setVisible(false);
			}
		};
		advancedWindow.argsOKButton.addActionListener(argsOKListener);
		
		argsCancelListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Logger.logln(NAME+"Cancel Button clicked in the Arguments Editor, will scrap changes");
				
				advancedWindow.argsField.setText(advancedWindow.getOptionsStr(((OptionHandler)advancedWindow.classifiers.get(0)).getOptions()));
				advancedWindow.argsFrame.setVisible(false);
			}
		};
		advancedWindow.argsCancelButton.addActionListener(argsCancelListener);
	}

	/**
	 * Initialies any remaining listeners
	 */
	private void initOtherListeners() {
		tabbedPaneListener = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (advancedWindow.tabbedPane.getSelectedIndex() == 0) {
					advancedWindow.getRootPane().setDefaultButton(advancedWindow.featureOKButton);
					advancedWindow.featureOKButton.requestFocusInWindow();
				} else {
					advancedWindow.getRootPane().setDefaultButton(advancedWindow.classOKButton);
					advancedWindow.classOKButton.requestFocusInWindow();
				}
			}
		};
		advancedWindow.tabbedPane.addChangeListener(tabbedPaneListener);
		
		advancedWindowListener = new WindowListener() {
			@Override
			public void windowOpened(WindowEvent e) {
				if (advancedWindow.tabbedPane.getSelectedIndex() == 0)
					advancedWindow.featureOKButton.requestFocusInWindow();
				else
					advancedWindow.classOKButton.requestFocusInWindow();
			}
			@Override
			public void windowClosing(WindowEvent e) {}
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
		};
		advancedWindow.addWindowListener(advancedWindowListener);
	}
	
	/**
	 * Either enables or disables the ArgsOKButton based on whether or not the arguments from the argument text field are
	 * acceptable in Weka or not.
	 * 
	 * NOTE: This does not catch arguments that don't make sense or may break Weka, we are simply filtering out arguments
	 * that Weka won't even let us set. If there is a "deeper" way to ensure that the arguments are valid while still retaining
	 * good performance feel free to do so.
	 */
	private void updateArgsOKButton() {
		boolean good = true;
		
		String options = advancedWindow.argsField.getText();
		try {
			((OptionHandler)testClassifier).setOptions(options.split(" "));
		} catch (Exception e1) {
			good = false;
		}
		
		if (good) {
			advancedWindow.argsOKButton.setEnabled(true);
		} else {
			advancedWindow.argsOKButton.setEnabled(false);
		}
	}
	
	/**
	 * Clears all "View ${FEATURE_SET} Info" Text panes and tables
	 */
	protected void clearFeatureView() {
		advancedWindow.viewInfoNameTextPane.setText("");
		advancedWindow.viewInfoDescTextPane.setText("");
		advancedWindow.viewInfoNormTextPane.setText("");
		advancedWindow.viewInfoFactorTextPane.setText("");
		advancedWindow.viewInfoExtractorPane.setText("");
		
		advancedWindow.viewInfoExtractorConfigModel.getDataVector().removeAllElements();
		advancedWindow.viewInfoCanonTableModel.getDataVector().removeAllElements();
		advancedWindow.viewInfoCanonConfigTableModel.getDataVector().removeAllElements();
		advancedWindow.viewInfoCullTableModel.getDataVector().removeAllElements();
		advancedWindow.viewInfoCullConfigTableModel.getDataVector().removeAllElements();
		
		advancedWindow.viewInfoFrame.revalidate();
		advancedWindow.viewInfoFrame.repaint();
	}

	/**
	 * Updates the "View ${FEATURE_SET} Info" window's text panes and tables with data corresponding to the currently selected feature set
	 */
	protected void updateViewInfoWindow(int selected) {
		clearFeatureView();
		
		//Clearing All selections
		advancedWindow.viewInfoExtractorConfigTable.clearSelection();
		advancedWindow.viewInfoCanonTable.clearSelection();
		advancedWindow.viewInfoCanonConfigTable.clearSelection();
		advancedWindow.viewInfoCullTable.clearSelection();
		advancedWindow.viewInfoCullConfigTable.clearSelection();

		//If nothing's selected, return
		if (selected == -1)
			return;

		FeatureDriver fd = cfd.featureDriverAt(selected);

		//Name and description
		advancedWindow.viewInfoNameTextPane.setText(fd.getName());
		advancedWindow.viewInfoDescTextPane.setText(fd.getDescription());

		//Update normalization
		advancedWindow.viewInfoNormTextPane.setText(fd.getNormBaseline().getTitle());
		advancedWindow.viewInfoFactorTextPane.setText(fd.getNormFactor().toString());

		//Update feature extractor
		advancedWindow.viewInfoExtractorPane.setText(fd.getUnderlyingEventDriver().displayName());
		populateTableWithParams(fd.getUnderlyingEventDriver(), advancedWindow.viewInfoExtractorConfigModel);
		advancedWindow.viewInfoExtractorConfigTable.doLayout();

		//Update canonicizers
		List<Canonicizer> canons = fd.getCanonicizers();
		if (canons != null) {
			for (int i = 0; i < canons.size(); i++) {
				advancedWindow.viewInfoCanonTableModel.addRow(new String[]{canons.get(i).displayName()});
			}
			advancedWindow.viewInfoCanonTable.doLayout();
		} else {
			advancedWindow.viewInfoCanonTableModel.addRow(new String[]{"N/A"});
			advancedWindow.viewInfoCanonTable.doLayout();
		}

		// update cullers
		List<EventCuller> cullers = fd.getCullers();
		if (cullers != null) {
			for (int i = 0; i < cullers.size(); i++) {
				advancedWindow.viewInfoCullTableModel.addRow(new String[]{cullers.get(i).displayName()});
			}
			advancedWindow.viewInfoCullTable.doLayout();
		} else {
			advancedWindow.viewInfoCullTableModel.addRow(new String[]{"N/A"});
			advancedWindow.viewInfoCullTable.doLayout();
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
	 * Updates the "Feature Set" tab to reflect a newly selected feature set.
	 */
	protected void updateFeatureSetTab() {
		cfd = advancedWindow.presetCFDs.get(advancedWindow.featureChoice.getSelectedIndex());
		Logger.logln(NAME+"Successfully loaded feature set: "+cfd.getName());

		clearFeatureView();
		advancedWindow.featureDescPane.setText(cfd.getDescription() == null ? "" : cfd.getDescription());
		
		String feature = (String)advancedWindow.featureChoice.getSelectedItem();
		if (!PropertiesUtil.getFeature().equals(feature)) {
			PropertiesUtil.setFeature(feature);
		}
		
		advancedWindow.viewInfoListModel.removeAllElements();
		int size = cfd.numOfFeatureDrivers();
		for (int i = 0; i < size; i++) { 
			advancedWindow.viewInfoListModel.addElement(cfd.featureDriverAt(i).getName());
		}
	}
	
	/**
	 * Updates the "Classifier" tab to reflect a newly selected classifier.
	 */
	protected void updateClassifierTab() {
		Classifier tmpClassifier;

		String className = "";
		try {
			className = (String)advancedWindow.classChoice.getSelectedItem();
			tmpClassifier = (Classifier)Class.forName(advancedWindow.fullClassPath.get(className)).newInstance();
			testClassifier = (Classifier)Class.forName(advancedWindow.fullClassPath.get(className)).newInstance();
		} catch (Exception e) {
			Logger.logln(NAME+"Could not create classifier out of the selected class: "+className, LogOut.STDERR);
			return;
		}

		Logger.logln(NAME+"Successfully loaded classifier: "+className);
		//Add an -M option for SMO classifier
		if (className.toLowerCase().contains("smo")) {
			String[] optionsArray = ((OptionHandler) tmpClassifier).getOptions();
			optionsArray = Arrays.copyOf(optionsArray, optionsArray.length + 1);
			optionsArray[optionsArray.length - 1] = "-M";
			try {
				((OptionHandler)tmpClassifier).setOptions(optionsArray);
			} catch (Exception e) {
				e.printStackTrace();
				Logger.logln(NAME+"Problem occured while tried to add -M flag to SMO classifier", LogOut.STDERR);
			}
		}

		//Show options and description
		advancedWindow.argsField.setText(advancedWindow.getOptionsStr(((OptionHandler)tmpClassifier).getOptions()));
		advancedWindow.classDescPane.setText(advancedWindow.getDesc(tmpClassifier));

		advancedWindow.classifiers = new ArrayList<Classifier>();
		advancedWindow.classifiers.add(tmpClassifier);
		
		if (!PropertiesUtil.getClassifier().equals(className)) {
			PropertiesUtil.setClassifier(className);
		}
		
		SwingUtilities.invokeLater(new ScrollToTop(new Point(0, 0), advancedWindow.classDescScrollPane));
	}
}
