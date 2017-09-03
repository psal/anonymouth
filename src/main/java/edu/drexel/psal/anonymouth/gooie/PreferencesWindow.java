package edu.drexel.psal.anonymouth.gooie;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import edu.drexel.psal.anonymouth.helpers.DisableFocus;
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
	protected JCheckBox translations;
	protected JCheckBox filterAddWords;
	protected JLabel translationsNote;
	protected int generalHeight;
	
	//Editor tab
	protected JPanel editor;
	protected JCheckBox highlightElems;
	protected JCheckBox highlightSent;
	protected JLabel fontSize;
	protected JComboBox<String> fontSizes;
	protected final String[] SIZES = {"9", "10", "11", "12", "13", "14", "18"};
	protected JLabel highlightColor;
	protected JComboBox<String> sentHighlightColors;
	protected final String[] COLORS = {"Yellow", "Orange", "Blue", "Purple"};
	protected int defaultsHeight;
	
	//Advanced tab
	protected JPanel advanced;
	protected JLabel maxFeatures;
	protected JSlider maxFeaturesSlider;
	protected JTextField maxFeaturesBox;
	protected JLabel numOfThreads;
	protected JTextField numOfThreadsBox;
	protected JSlider numOfThreadsSlider;
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
		this.setIconImage(ThePresident.logo);
		
		preferencesDriver = new PreferencesDriver(main, this);
		preferencesWindow = this;
		initTabs();
		
		this.add(tabbedPane);
		this.setSize(new Dimension(500, generalHeight));
		this.setResizable(false);
		this.setLocationRelativeTo(null); // makes it form in the center of the screen
		
		DisableFocus.removeAllFocus(this);
	}
	
	/**
	 * Displays the window
	 */
	public void showWindow() {
		Logger.logln(NAME+"Preferences window opened");
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
		editor= new JPanel();
		advanced = new JPanel();

		MigLayout generalLayout = new MigLayout("fill");
		
		general.setLayout(generalLayout);
		{
			warnQuit = new JCheckBox();
			warnQuit.setText("Warn about unsaved changes upon exit");
			if (PropertiesUtil.getWarnQuit())
				warnQuit.setSelected(true);
			
			autoSave = new JCheckBox();
			autoSave.setText("Auto-Save anonymized documents upon exit");
			if (PropertiesUtil.getAutoSave()) {
				autoSave.setSelected(true);
				warnQuit.setEnabled(false);
			}
			
			autoSaveNote = new JLabel("<html><center>" +
					"Note: Will only save over previously saved Anonymized document,<br>" +
					"if one exists. Will not overwrite original document." +
					"</center></html>");
			autoSaveNote.setForeground(Color.GRAY);
			
			JSeparator sep1 = new JSeparator(JSeparator.HORIZONTAL);
			sep1.setPreferredSize(new Dimension(484, 15));
			
			filterAddWords = new JCheckBox();
			filterAddWords.setText("Filter out non-words from \"Words to Add\" Suggestions");
			filterAddWords.setToolTipText("<html><center>" +
					"Filtered out words include but are not limited to:<br>" +
					"	-Email addresses<br>"+
					"	-Dates and other numbers<br>"+
					"	-Web addresses"+
					"</center></html>");
			if (PropertiesUtil.getFilterAddSuggestions())
				filterAddWords.setSelected(true);
			
			translations = new JCheckBox();
			translations.setText("Enable sending sentences to Microsoft Bing© for translation");
			if (PropertiesUtil.getDoTranslations())
				translations.setSelected(true);
			
			translationsNote = new JLabel("<html><link rel=\"stylesheet\" type=\"text/css\" href=\"mystyles.css\" media=\"screen\" /><center>" +
					"Note: Once checked, simply click the Start button in the translations tab to " +
					"begin translations. This button will not be clickable unless permission is granted via this checkbox." +
					"</center></html>");
			translationsNote.setForeground(Color.GRAY);
			
			JSeparator sep2 = new JSeparator(JSeparator.HORIZONTAL);
			sep2.setPreferredSize(new Dimension(484, 15));
			
			general.add(autoSave, "wrap");
			general.add(autoSaveNote, "alignx 50%, wrap");
			general.add(warnQuit, "wrap");
			general.add(sep1, "alignx 50%, wrap");
			general.add(filterAddWords, "wrap");
			general.add(translations, "wrap");
			general.add(translationsNote, "alignx 50%, wrap");
			general.add(sep2, "alignx 50%");
			
			generalHeight = 340;
		}
		
		MigLayout defaultLayout = new MigLayout();
		editor.setLayout(defaultLayout);
		{	
			highlightSent = new JCheckBox();
			highlightSent.setText("Highlight current sentence");
			if (PropertiesUtil.getHighlightSents()) {
				highlightSent.setSelected(true);
			}
			
			highlightElems = new JCheckBox();
			highlightElems.setText("Automatically highlight words to remove for selected sentence");
			if (PropertiesUtil.getAutoHighlight()) {
				highlightElems.setSelected(true);
			}
			
			JSeparator sep = new JSeparator(JSeparator.HORIZONTAL);
			sep.setPreferredSize(new Dimension(484, 15));
			
			fontSize = new JLabel ("Font Size: ");
			
			fontSizes = new JComboBox<String>();
			for (int i = 0; i < SIZES.length; i++)
				fontSizes.addItem(SIZES[i]);
			fontSizes.setSelectedItem(Integer.toString(PropertiesUtil.getFontSize()));
			
			highlightColor = new JLabel("Sentence Highlight Color: ");
			
			sentHighlightColors = new JComboBox<String>();
			for (int i = 0; i < COLORS.length; i++)
				sentHighlightColors.addItem(COLORS[i]);
			sentHighlightColors.setSelectedIndex(PropertiesUtil.getHighlightColorIndex());
			
			editor.add(highlightSent, "wrap");
			editor.add(highlightElems, "wrap");
			editor.add(sep, "alignx 50%, wrap");
			editor.add(fontSize, "split 4");
			editor.add(fontSizes);
			editor.add(highlightColor, "gapbefore 20");
			editor.add(sentHighlightColors);
			
			defaultsHeight = 190;
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
			maxFeaturesSlider.setMinimum(300);
			maxFeaturesSlider.setSnapToTicks(true);
			maxFeaturesSlider.setValue(PropertiesUtil.getMaximumFeatures());
			maxFeaturesSlider.setOrientation(SwingConstants.HORIZONTAL);
			
			maxFeaturesBox = new JTextField();
			maxFeaturesBox.setPreferredSize(new Dimension(50, 20));
			maxFeaturesBox.setText(Integer.toString(PropertiesUtil.getMaximumFeatures()));
			
			numOfThreads = new JLabel("Number of Threads for Part of Speech Tagging = ");
			
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
			
			JSeparator sep1 = new JSeparator(JSeparator.HORIZONTAL);
			sep1.setPreferredSize(new Dimension(484, 15));
			advanced.add(sep1, "gaptop 5, alignx 50%, wrap");
			advanced.add(versionAutoSave, "wrap");
			advanced.add(versionAutoSaveNote, "alignx 50%, wrap");
			JSeparator sep2 = new JSeparator(JSeparator.HORIZONTAL);
			sep2.setPreferredSize(new Dimension(484, 15));
			advanced.add(sep2, "gaptop 5, alignx 50%, wrap");
			advanced.add(resetAll, "gaptop 5, alignx 50%");
			
			advancedHeight = 370;
		}

		preferencesDriver.initListeners();
		tabbedPane.add("General", general);
		tabbedPane.add("Editor", editor);
		tabbedPane.add("Advanced", advanced);
	}
	
	/**
	 * We want to be absolutely sure that when Preferences is closed that the set values are within
	 * acceptable boundaries, and if they aren't change them back to their editor.
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