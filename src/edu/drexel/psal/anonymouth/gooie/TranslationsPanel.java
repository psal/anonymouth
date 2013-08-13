package edu.drexel.psal.anonymouth.gooie;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;

import net.miginfocom.swing.MigLayout;
import edu.drexel.psal.anonymouth.helpers.ImageLoader;
import edu.drexel.psal.anonymouth.utils.TaggedSentence;
import edu.drexel.psal.anonymouth.utils.TranslatorThread;
import edu.drexel.psal.jstylo.generics.Logger;

/**
 * Handles all swing components relating to the Translations Tab's 
 * 
 * @author Marc Barrowclift
 *
 */
public class TranslationsPanel {
	
	private final String NAME = "( TranslationsPanel ) - ";
	
	//Images
	public final String ARROW_UP = "arrow_up.png";
	public final String ARROW_DOWN = "arrow_down.png";
	private ImageIcon arrow_up;
	private ImageIcon arrow_down;
	
	//Variables
	private GUIMain main;
	protected TranslationsDriver driver;
	protected TaggedSentence current;
	protected Map<String, TaggedSentence> translationsMap;
	protected int numTranslations;
	
	//Swing components
	protected JTextPane[] translationTextAreas;
	protected JButton[] translationButtons;
	protected JPanel[] finalPanels;
	protected JLabel[] languageLabels;
	
	/**
	 * Constructor
	 * 
	 * @param main
	 * 		GUIMain instance
	 */
	public TranslationsPanel(GUIMain main) {
		this.main = main;
		driver = new TranslationsDriver(this, main);
		
		initImages();
	}
	
	/**
	 * Loads all images used by the translations tab
	 */
	private void initImages() {
		arrow_up = ImageLoader.getImageIcon(ARROW_UP);
		arrow_down = ImageLoader.getImageIcon(ARROW_DOWN);
	}
	
	/**
	 * Displays the translations of the given sentence in the translations holder panel.
	 * 
	 * @param sentence
	 * 		The TaggedSentence to show the translations for
	 */
	public void showTranslations(TaggedSentence sentence) {		
		// remove all the current translations shown
		if (main.documentPane.isEnabled()) {
			main.translationsHolderPanel.removeAll();
		}
		
		if (TranslatorThread.noInternet) {
			main.notTranslated.setText("Translations unavailable: No Internet connection\n\n" +
					"If you wish to recieve translation suggestions you must connect to the internet" +
					"and re-process your document.");
			main.translationsHolderPanel.add(main.notTranslated, "");
			main.stopTranslations.setEnabled(false);
		} else if (TranslatorThread.accountsUsed) {
			main.notTranslated.setText("The account used for translations has expired.\n\n" +
					"In order to continue recieving translations, you must restart in order for the " +
					"account change to be reflected.");
			main.translationsHolderPanel.add(main.notTranslated, "");
			main.stopTranslations.setEnabled(false);
			main.resetTranslator.setEnabled(false);
		} else if (!PropertiesUtil.getDoTranslations()) {
			main.notTranslated.setText("You have turned translations off.");
			main.translationsHolderPanel.add(main.notTranslated, "");
			main.stopTranslations.setEnabled(false);
		} else if (sentence.hasTranslations()) {
			Logger.logln(NAME+"Showing translations for sentence: " + sentence.getUntagged(false));
			current = sentence;

			// retrieve the translation information
			ArrayList<String> translationNames = current.getTranslationNames();
			ArrayList<TaggedSentence> translations = current.getTranslations();
			translationsMap = new HashMap<String, TaggedSentence>();
			numTranslations = translations.size();

			// initialize the GUI components
			translationTextAreas = new JTextPane[numTranslations];// everySingleCluster.size()
			languageLabels = new JLabel[numTranslations];
			finalPanels = new JPanel[numTranslations];
			translationButtons = new JButton[numTranslations];
			
			// for each translation, initialize a title label, and a text area that will hold the translation
			// then add those two to a final panel, which will be added to the translation list panel.
			for (int i = 0; i < numTranslations; i++) {
				// set up title label
				languageLabels[i] = new JLabel(translationNames.get(i));
				translationsMap.put(translationNames.get(i), translations.get(i));
				languageLabels[i].setFont(main.titleFont);
				languageLabels[i].setHorizontalAlignment(SwingConstants.CENTER);
				languageLabels[i].setBorder(main.rlborder);
				languageLabels[i].setOpaque(true);
				languageLabels[i].setBackground(main.blue);

				// set up translation text area
				translationTextAreas[i] = new JTextPane();
				translationTextAreas[i].setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.GRAY), BorderFactory.createEmptyBorder(1,3,1,3)));
				translationTextAreas[i].setText(translations.get(i).getUntagged(false).trim());
				translationTextAreas[i].setEditable(false);

				translationButtons[i] = new JButton();
				translationButtons[i].setIcon(arrow_up); //TODO WHY does this throw NullPointerExceptions every so often?
				translationButtons[i].setPressedIcon(arrow_down);
				translationButtons[i].setToolTipText("Click to replace selected sentence with this translation");
				translationButtons[i].setBorderPainted(false);
				translationButtons[i].setContentAreaFilled(false);
				translationButtons[i].setActionCommand(translationNames.get(i));
				translationButtons[i].addActionListener(driver);

				// set up final panel, which will hold the previous two components
				MigLayout layout = new MigLayout(
						"wrap, ins 0",
						"",
						"");
				finalPanels[i] = new JPanel(layout);
				finalPanels[i].add(languageLabels[i], "grow, h 20!, north");
				finalPanels[i].add(translationButtons[i], "west, w 30!");
				finalPanels[i].add(translationTextAreas[i], "east, w 100:283:283");

				// add final panel to the translations list panel
				main.translationsHolderPanel.add(finalPanels[i], "");
			}
		} else if (PropertiesUtil.getDoTranslations() && main.startTranslations.isEnabled()) {
			main.notTranslated.setText("You granted access for translations to be obtained from Microsoft Bing in Preferences.\n\nTo begin, click the Start button");
			main.translationsHolderPanel.add(main.notTranslated, "");
		} else if (main.documentPane.isEnabled()) {
			main.notTranslated.setText("Sentence has not been translated yet, please wait or work on already translated sentences.");
			main.translationsHolderPanel.add(main.notTranslated, "");
		}
			
		// revalidates and repaints so the GUI updates
		main.translationsHolderPanel.revalidate();
		main.translationsHolderPanel.repaint();
	}
	
	/**
	 * Resets all variables and clears panel of all translations, to be used for re-processing
	 */
	public void reset(boolean reprocessing) {
		Logger.logln(NAME+"Reset");
		main.translationsHolderPanel.removeAll();
		if (PropertiesUtil.getDoTranslations()) {
			if (reprocessing)
				main.notTranslated.setText("Document re-processing, please wait.");
			else
				main.notTranslated.setText("");
			main.translationsHolderPanel.add(main.notTranslated, "");
		}
	}
}