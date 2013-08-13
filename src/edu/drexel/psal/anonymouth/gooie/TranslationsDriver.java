package edu.drexel.psal.anonymouth.gooie;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import edu.drexel.psal.anonymouth.utils.TaggedDocument;
import edu.drexel.psal.anonymouth.utils.TranslatorThread;
import edu.drexel.psal.jstylo.generics.Logger;

/**
 * Fetches the translations for the selected sentence (if available) and displays them to the user as they come.
 * 
 * @author Marc Barrowclift
 * @author Unknown
 */

public class TranslationsDriver implements ActionListener {
	
	private final String NAME = "( TranslationsDriver ) - ";

	private ActionListener resetTranslatorListener;
	private ActionListener stopTranslationsListener;
	private ActionListener startTranslationsListener;
	
	private GUIMain main;
	protected TranslatorThread translator;
	private TranslationsPanel translationsPanel;

	/**
	 * Constructor
	 *
	 * @param
	 * 		GUIMain instance
	 */
	public TranslationsDriver(TranslationsPanel translationsPanel, GUIMain main) {
		this.main = main;
		this.translationsPanel = translationsPanel;

		initListeners();
	}

	/**
	 * Initializes all listeners relating to the translations tab
	 */
	public void initListeners() {
		stopTranslationsListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				translator.reset();
				main.startTranslations.setEnabled(true);
				main.stopTranslations.setEnabled(false);
			}
		};
		main.stopTranslations.addActionListener(stopTranslationsListener);
		
		startTranslationsListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				main.startTranslations.setEnabled(false);
				main.stopTranslations.setEnabled(true);
				translator.load(DriverEditor.taggedDoc.getTaggedSentences());
				translationsPanel.showTranslations(DriverEditor.taggedDoc.getSentenceNumber(DriverEditor.sentToTranslate));
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
					main.translationsHolderPanel.removeAll();
					main.notTranslated.setText("Sentence has not been translated yet, please wait or work on already translated sentences.");
					main.translationsHolderPanel.add(main.notTranslated, "");
					main.stopTranslations.setEnabled(true);
					main.startTranslations.setEnabled(false);
					translator.reset();
					DriverEditor.taggedDoc.deleteTranslations();
					
					try {
						Thread.sleep(500);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					
					translator.load(DriverEditor.taggedDoc.getTaggedSentences());
				}
			}
		};
		main.resetTranslator.addActionListener(resetTranslatorListener);
	}

	/**
	 * The user clicked the translation swap-in arrow button.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		Logger.logln(NAME+"User clicked the Translation swap-in arrow button");
		DriverEditor.backedUpTaggedDoc = new TaggedDocument(DriverEditor.taggedDoc);
		main.versionControl.addVersion(DriverEditor.backedUpTaggedDoc, main.getDocumentPane().getCaret().getDot());
		
		GUIMain.saved = false;
		InputFilter.ignoreTranslation = true;
		DriverEditor.removeReplaceAndUpdate(main, DriverEditor.sentToTranslate, translationsPanel.translationsMap.get(e.getActionCommand()).getUntagged(false), true);
		translator.replace(DriverEditor.taggedDoc.getSentenceNumber(DriverEditor.sentToTranslate), translationsPanel.current);
		
		main.anonymityBar.updateBar();
		main.suggestionsTabDriver.placeSuggestions();
		
		main.translationsHolderPanel.removeAll();
		main.notTranslated.setText("Sentence has not been translated yet, please wait or work on already translated sentences.");
		main.translationsHolderPanel.add(main.notTranslated, "");
		main.translationsHolderPanel.revalidate();
		main.translationsHolderPanel.repaint();
	}
}