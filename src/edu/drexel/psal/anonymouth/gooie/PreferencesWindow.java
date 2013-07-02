package edu.drexel.psal.anonymouth.gooie;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import edu.drexel.psal.JSANConstants;
import edu.drexel.psal.jstylo.generics.Logger;

import javax.swing.*;

import net.miginfocom.swing.MigLayout;

/**
 * The main preferences window for Anonymouth. Stores under a different pull down menu depending on the system (if Mac, it's put under
 * Anonymouth > Preferences to keep with the Mac L&F, and if Windows/Linux it's put under Settings > Preferences.
 * @author Marc Barrowclift
 *
 */
public class PreferencesWindow extends JFrame implements WindowListener {	

	private static final long serialVersionUID = 1L;
	private final String NAME = "( PreferencesWindow ) - ";

	protected GUIMain main;
	protected JTabbedPane tabbedPane;
	protected PreferencesWindow preferencesWindow;
	private PreferencesDriver preferencesDriver;
	
	//General tab
	protected JPanel general;
	protected JCheckBox autoSave;
	protected JLabel autoSaveNote;
	protected JCheckBox warnQuit;
	protected JCheckBox showWarnings;
	protected JCheckBox translations;
	protected JLabel fontSize;
	protected JCheckBox highlightElems;
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
	protected JCheckBox versionAutoSave;
	protected JLabel versionAutoSaveNote;
	protected JButton resetAll;
	protected int advancedHeight;
	
	/**
	 * CONSTRUCTOR
	 * @param main - Instance of GUIMain
	 */
	public PreferencesWindow(GUIMain main) {
		init(main);
		setVisible(false);
		Logger.logln(NAME+"Preferences window initialized successfully");
	}
	
	/**
	 * Readies the window.
	 * @param main
	 */
	private void init(final GUIMain main) {
		this.setTitle("Preferences");
		this.main = main;
		this.setIconImage(new ImageIcon(getClass().getResource(JSANConstants.JSAN_GRAPHICS_PREFIX+ThePresident.ANONYMOUTH_LOGO)).getImage());
		
		preferencesDriver = new PreferencesDriver(main, this);
		preferencesWindow = this;
		initTabs();
		
		this.add(tabbedPane);
		this.setSize(new Dimension(500, generalHeight));
		this.setResizable(false);
		this.setLocationRelativeTo(null); // makes it form in the center of the screen
	}
	
	/**
	 * Displays the window
	 */
	public void openWindow() {
		Logger.logln(NAME+"Preferences window opened");
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
			
			JSeparator sep = new JSeparator(JSeparator.HORIZONTAL);
			sep.setPreferredSize(new Dimension(484, 15));
			
			fontSize = new JLabel ("Font Size: ");
			
			fontSizes = new JComboBox<String>();
			for (int i = 0; i < sizes.length; i++)
				fontSizes.addItem(sizes[i]);
			fontSizes.setSelectedItem(Integer.toString(PropertiesUtil.getFontSize()));
			
			highlightElems = new JCheckBox();
			highlightElems.setText("Automatically highlight words to remove in selected sentence");
			if (PropertiesUtil.getAutoHighlight()) {
				highlightElems.setSelected(true);
			}
			
			general.add(autoSave, "wrap");
			general.add(autoSaveNote, "alignx 50%, wrap");
			general.add(warnQuit, "wrap");
			general.add(showWarnings, "wrap");
			general.add(translations, "wrap");
			general.add(sep, "alignx 50%, wrap");
			general.add(fontSize, "split 2");
			general.add(fontSizes, "wrap");
			general.add(highlightElems);
			
			generalHeight = 320;
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
			
			versionAutoSave = new JCheckBox();
			versionAutoSave.setText("Backup version of document to anonymize every reprocess");
			if (PropertiesUtil.getVersionAutoSave()) {
				versionAutoSave.setSelected(true);
			}
			
			versionAutoSaveNote = new JLabel("<html>Note: Backups saved to hidden directory \"./edited_documents\" in the<br>Anonymouth root directory</html>");
			versionAutoSaveNote.setForeground(Color.GRAY);
			
			resetAll = new JButton("Reset Preferences");
			resetAll.setToolTipText("Reset all user preferences back to their default values");
			
			
			advanced.add(maxFeatures, "split");
			advanced.add(maxFeaturesBox, "wrap");
			advanced.add(maxFeaturesSlider, "alignx 50%, wrap");
			advanced.add(numOfThreads, "split");
			advanced.add(numOfThreadsBox, "wrap");
			advanced.add(numOfThreadsSlider, "alignx 50%, wrap");
			advanced.add(numOfThreadsNote, "alignx 50%, wrap");
			
			JSeparator sep1 = new JSeparator(JSeparator.HORIZONTAL);
			sep1.setPreferredSize(new Dimension(484, 15));
			advanced.add(sep1, "gaptop 5, alignx 50%, wrap");
			advanced.add(versionAutoSave, "wrap");
			advanced.add(versionAutoSaveNote, "alignx 50%, wrap");
			JSeparator sep2 = new JSeparator(JSeparator.HORIZONTAL);
			sep2.setPreferredSize(new Dimension(484, 15));
			advanced.add(sep2, "gaptop 5, alignx 50%, wrap");
			advanced.add(resetAll, "gaptop 5, alignx 50%");
			
			advancedHeight = 390;
		}

		preferencesDriver.initListeners();
		tabbedPane.add("General", general);
		tabbedPane.add("Defaults", defaults);
		tabbedPane.add("Advanced", advanced);
	}
	
	/**
	 * We want to be absolutely sure that when Preferences is closed that the set values are within
	 * acceptable boundaries, and if they aren't change them back to their defaults.
	 */
	@Override
	public void windowClosing(WindowEvent e) {
		preferencesDriver.assertValues();
	}
	
	@Override
	public void windowClosed(WindowEvent e) {
		Logger.logln(NAME+"Preferences window closed");
	}
	
	@Override
	public void windowOpened(WindowEvent e) {}
	@Override
	public void windowIconified(WindowEvent e) {}
	@Override
	public void windowDeiconified(WindowEvent e) {}
	@Override
	public void windowActivated(WindowEvent e) {}
	@Override
	public void windowDeactivated(WindowEvent e) {}
}