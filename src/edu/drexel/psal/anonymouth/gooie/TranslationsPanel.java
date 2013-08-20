package edu.drexel.psal.anonymouth.gooie;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;

import net.miginfocom.swing.MigLayout;
import edu.drexel.psal.ANONConstants;
import edu.drexel.psal.anonymouth.utils.TaggedSentence;
import edu.drexel.psal.anonymouth.utils.TranslatorThread;
import edu.drexel.psal.jstylo.generics.Logger;

/**
 * Handles all swing components relating to the Translations Tab's 
 * 
 * @author Marc Barrowclift
 */
public class TranslationsPanel {
	
	private final String NAME = "( TranslationsPanel ) - ";
	protected final int SWAP_WIDTH = 30;
	
	//Variables
	private GUIMain main;
	protected TranslationsDriver driver;
	protected TaggedSentence current;
	protected Map<String, TaggedSentence> translationsMap;
	protected int numTranslations;
	
	//Swing components
	protected JTextPane[] translationTextAreas;
	protected SwapButtonPanel[] translationButtons;
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
		
		//If any of these conditions are met, we just want to clear things that need to be cleared and return.
		if (TranslatorThread.noInternet || TranslatorThread.accountsUsed || !PropertiesUtil.getDoTranslations()) {
			if (TranslatorThread.noInternet) {
				main.notTranslated.setText("Translations unavailable: No Internet connection\n\n" +
						"If you wish to recieve translation suggestions you must connect to the internet" +
						"and re-process your document.");
				main.translationsHolderPanel.add(main.notTranslated, "");
				main.translateSentenceButton.setEnabled(false);
			} else if (TranslatorThread.accountsUsed) {
				main.notTranslated.setText("The account used for translations has expired.\n\n" +
						"In order to continue recieving translations, you must restart in order for the " +
						"account change to be reflected.");
				main.translationsHolderPanel.add(main.notTranslated, "");
				main.translateSentenceButton.setEnabled(false);
				main.resetTranslator.setEnabled(false);
			} else {
				main.notTranslated.setText("");
				main.translationsHolderPanel.add(main.notTranslated, "");
				main.translateSentenceButton.setEnabled(false);
			}
				
			// revalidates and repaints so the GUI updates
			main.translationsHolderPanel.revalidate();
			main.translationsHolderPanel.repaint();
			return;
		}
		
		if (sentence.hasTranslations()) {
			Logger.logln(NAME+"Showing translations for sentence: " + sentence.getUntagged(false));
			
			boolean translated = sentence.isTranslated();
			if (translated && main.translationsTopPanelShown != ANONConstants.TRANSLATIONS.DONE) {
				switchToEmptyPanel();
			}
			if (!translated && main.translationsTopPanelShown != ANONConstants.TRANSLATIONS.PROCESSING) {
				switchToProgressPanel();
			}

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
			translationButtons = new SwapButtonPanel[numTranslations];
			
			// for each translation, initialize a title label, and a text area that will hold the translation
			// then add those two to a final panel, which will be added to the translation list panel.
			for (int i = 0; i < numTranslations; i++) {
				// set up translation text area
				translationTextAreas[i] = new JTextPane();
				translationTextAreas[i].setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.GRAY), BorderFactory.createEmptyBorder(1,3,1,3)));
				translationTextAreas[i].setText(translations.get(i).getUntagged(false).trim());
				translationTextAreas[i].setEditable(false);

				translationButtons[i] = new SwapButtonPanel(this);
				translationButtons[i].setColor(i, numTranslations);
				translationButtons[i].setToolTipText("Click to replace selected sentence with this translation");
				translationButtons[i].setActionCommand(translationNames.get(i));
				//translationButtons[i].addActionListener(driver);

				// set up final panel, which will hold the previous two components
				MigLayout layout = new MigLayout(
						"wrap, ins 0",
						"",
						"");
				finalPanels[i] = new JPanel(layout);
				finalPanels[i].add(translationButtons[i], "west, w " + SWAP_WIDTH + "!");
				finalPanels[i].add(translationTextAreas[i], "east, w 100:283:283");

				// add final panel to the translations list panel
				main.translationsHolderPanel.add(finalPanels[i], "");

				System.out.println(finalPanels[i].getPreferredSize().height);
				translationButtons[i].updateSize(finalPanels[i].getPreferredSize().height - 30);
			}
		} else {
			switchToButtonPanel();
		}
		
		// revalidates and repaints so the GUI updates
		main.translationsHolderPanel.revalidate();
		main.translationsHolderPanel.repaint();
	}
	
	public void switchToEmptyPanel() {
		main.translationsTopPanel.removeAll();
		main.translationsTopPanel.revalidate();
		main.translationsTopPanel.repaint();
		
		main.translationsTopPanelShown = ANONConstants.TRANSLATIONS.DONE;
	}
	
	public void switchToProgressPanel() {
		main.translationsTopPanel.removeAll();
		main.translationsTopPanel.setLayout(new MigLayout(
				"wrap, ins 0, gap 0",
				"[grow, fill]",
				"[]"));
		main.translationsTopPanel.add(main.translationsProgressBar, "grow");
		main.translationsTopPanel.revalidate();
		main.translationsTopPanel.repaint();
		
		main.translationsTopPanelShown = ANONConstants.TRANSLATIONS.PROCESSING;
	}
	
	public void switchToButtonPanel() {
		main.translationsTopPanel.removeAll();
		main.translationsTopPanel.setLayout(new MigLayout(
				"wrap, ins 0, gap 0",
				"[grow, fill][]",
				"[]"));
		main.translationsTopPanel.add(main.translateSentenceButton, "split, grow");
		main.translationsTopPanel.add(main.translationHelpButton, "wrap");
		main.translationsTopPanel.revalidate();
		main.translationsTopPanel.repaint();
		
		main.translationsTopPanelShown = ANONConstants.TRANSLATIONS.EMPTY;
	}
	
	/**
	 * Resets all variables and clears panel of all translations, to be used for re-processing
	 */
	public void reset(boolean reprocessing) {
		Logger.logln(NAME+"Reset");
		switchToEmptyPanel();
	}
}

class SwapButtonPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private final int[] ARROW_X = {3, 27, 27};
	private int[] arrow_Y = {this.getHeight()/2, (this.getHeight()/2)-15, (this.getHeight()/2)+15};
	private final Color START_COLOR = Color.RED;
	private final Color END_COLOR = Color.GREEN;
	
	private Color arrowColor;
	private Color backgroundColor;
	
	private String actionCommand;
	private TranslationsPanel translationsPanel;
	
	public SwapButtonPanel(TranslationsPanel translationsPanel) {
		this.translationsPanel = translationsPanel;
		this.setPreferredSize(new Dimension(translationsPanel.SWAP_WIDTH, 50));
		arrowColor = new Color(0,0,0);
		backgroundColor = new Color(0,0,0,0);
	}
	
	public void setActionCommand(String command) {
		actionCommand = command;
	}
	
	public String getActionCommand() {
		return actionCommand;
	}
	
	public void updateSize(int height) {
		arrow_Y[0] = height/2;
		arrow_Y[1] = arrow_Y[0]-10;
		arrow_Y[2] = arrow_Y[0]+10;
		
		this.setPreferredSize(new Dimension(translationsPanel.SWAP_WIDTH, height));
		this.setMaximumSize(new Dimension(translationsPanel.SWAP_WIDTH, height));
		this.setMinimumSize(new Dimension(translationsPanel.SWAP_WIDTH, height));
	}
	
	public void setColor(int position, int numTranslations) {
		double curPercent = (double)position / (double)numTranslations;

		if (curPercent <= 0) {
			arrowColor = new Color(0, 255, 0);
			backgroundColor = new Color(0, 255, 0, 200);
		} else if (curPercent >= 100) {
			arrowColor = new Color(255, 0, 0);
			backgroundColor = new Color(255, 0, 0, 0);
		} else {
			int r = (int)(START_COLOR.getRed() * curPercent + END_COLOR.getRed() * (1 - curPercent));
			int g = (int)(START_COLOR.getGreen() * curPercent + END_COLOR.getGreen() * (1 - curPercent));
			int b = (int)(START_COLOR.getBlue() * curPercent + END_COLOR.getBlue() * (1 - curPercent));
			
			arrowColor = new Color(r, g, b);
			backgroundColor = new Color(r, g, b, 200);
		}
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		g2d.setBackground(backgroundColor);
		g2d.setColor(arrowColor);
		g2d.drawPolygon(ARROW_X, arrow_Y, 3);
	}
}