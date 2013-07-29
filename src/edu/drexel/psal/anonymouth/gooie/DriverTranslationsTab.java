package edu.drexel.psal.anonymouth.gooie;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import net.miginfocom.swing.MigLayout;

import edu.drexel.psal.anonymouth.helpers.ImageLoader;
import edu.drexel.psal.anonymouth.utils.TaggedDocument;
import edu.drexel.psal.anonymouth.utils.TaggedSentence;

/**
 * Fetches the translations for the selected sentence (if available) and displays them to the user as they come.
 * 
 * @author Marc Barrowclift
 * @author Unknown
 */

public class DriverTranslationsTab implements ActionListener {
	
	public static final String ARROW_UP = "arrow_up.png";
	public static final String ARROW_DOWN = "arrow_down.png";
	private static ImageIcon arrow_up;
	private static ImageIcon arrow_down;
	
	private static ActionListener resetTranslatorListener;
	private static ActionListener stopTranslationsListener;
	private static ActionListener startTranslationsListener;
	
	private static GUIMain main;
	protected static JPanel[] finalPanels;
	protected static JLabel[] languageLabels;
	protected static Map<String, TaggedSentence> translationsMap;
	protected static JTextPane[] translationTextAreas;
	protected static JButton[] translationButtons;
	protected static int numTranslations;
	protected static TaggedSentence current;
	private static DriverTranslationsTab inst;

	public static void initListeners(GUIMain main) {
		DriverTranslationsTab.main = main;
		DriverTranslationsTab.inst = new DriverTranslationsTab();
		
		stopTranslationsListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GUIMain.GUITranslator.reset();
				GUIMain.inst.startTranslations.setEnabled(true);
				GUIMain.inst.stopTranslations.setEnabled(false);
			}
		};
		main.stopTranslations.addActionListener(stopTranslationsListener);
		
		startTranslationsListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GUIMain.inst.startTranslations.setEnabled(false);
				GUIMain.inst.stopTranslations.setEnabled(true);
				GUIMain.GUITranslator.load(DriverEditor.taggedDoc.getTaggedSentences());
				DriverTranslationsTab.showTranslations(DriverEditor.taggedDoc.getSentenceNumber(DriverEditor.sentToTranslate));
			}
		};
		main.startTranslations.addActionListener(startTranslationsListener);
		
		resetTranslatorListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int answer = JOptionPane.showOptionDialog(null,
						"Are you sure you want to reset the translator?\nYou should do so if it's exibiting strange\nbehavior or not translating certain sentences.",
						"Reset Translator",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE,
						UIManager.getIcon("OptionPane.warningIcon"), null, null);
				if (answer == JOptionPane.YES_OPTION) {
					DriverTranslationsTab.main.translationsHolderPanel.removeAll();
					GUIMain.inst.notTranslated.setText("Sentence has not been translated yet, please wait or work on already translated sentences.");
					GUIMain.inst.translationsHolderPanel.add(GUIMain.inst.notTranslated, "");
					GUIMain.inst.stopTranslations.setEnabled(true);
					GUIMain.inst.startTranslations.setEnabled(false);
					GUIMain.GUITranslator.reset();
					DriverEditor.taggedDoc.deleteTranslations();
					
					try {
						Thread.sleep(500);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					
					GUIMain.GUITranslator.load(DriverEditor.taggedDoc.getTaggedSentences());
				}
			}
		};
		main.resetTranslator.addActionListener(resetTranslatorListener);
		
		arrow_up = ImageLoader.getImageIcon(ARROW_UP);
		arrow_down = ImageLoader.getImageIcon(ARROW_DOWN);
	}
	
	/**
	 * Displays the translations of the given sentence in the translations holder panel.
	 * @param sentence - the TaggedSentence to show the translations of
	 */
	public static void showTranslations(TaggedSentence sentence) {		
		// remove all the current translations shown
		if (main.getDocumentPane().isEnabled()) {
			main.translationsHolderPanel.removeAll();
		}
		
		if (Translator.noInternet) {
			main.notTranslated.setText("Translations unavailable: No Internet connection\n\n" +
					"If you wish to recieve translation suggestions you must connect to the internet" +
					"and re-process your document.");
			main.translationsHolderPanel.add(main.notTranslated, "");
			main.stopTranslations.setEnabled(false);
		} else if (Translator.accountsUsed) {
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
				languageLabels[i].setFont(GUIMain.titleFont);
				languageLabels[i].setHorizontalAlignment(SwingConstants.CENTER);
				languageLabels[i].setBorder(GUIMain.rlborder);
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
				translationButtons[i].addActionListener(inst);

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
		} else if (main.getDocumentPane().isEnabled()) {
			main.notTranslated.setText("Sentence has not been translated yet, please wait or work on already translated sentences.");
			main.translationsHolderPanel.add(main.notTranslated, "");
		}
			
		// revalidates and repaints so the GUI updates
		main.translationsHolderPanel.revalidate();
		main.translationsHolderPanel.repaint();
	}

	/**
	 * The user clicked the translation-swap arrow button.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		DriverEditor.backedUpTaggedDoc = new TaggedDocument(DriverEditor.taggedDoc);
		main.versionControl.addVersion(DriverEditor.backedUpTaggedDoc, main.getDocumentPane().getCaret().getDot());
		
		GUIMain.saved = false;
		InputFilter.ignoreTranslation = true;
		DriverEditor.removeReplaceAndUpdate(main, DriverEditor.sentToTranslate, translationsMap.get(e.getActionCommand()).getUntagged(false), true);
		GUIMain.GUITranslator.replace(DriverEditor.taggedDoc.getSentenceNumber(DriverEditor.sentToTranslate), current);
		
		main.anonymityDrawingPanel.updateAnonymityBar();
		main.suggestionsTabDriver.placeSuggestions();
		
		main.translationsHolderPanel.removeAll();
		main.notTranslated.setText("Sentence has not been translated yet, please wait or work on already translated sentences.");
		main.translationsHolderPanel.add(main.notTranslated, "");
		main.translationsHolderPanel.revalidate();
		main.translationsHolderPanel.repaint();
	}
	
	/**
	 * Resets all variables and clears panel of all translations, to be used for re-processing
	 */
	public static void reset(boolean reprocessing) {
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
