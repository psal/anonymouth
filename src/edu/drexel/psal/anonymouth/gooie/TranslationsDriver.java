package edu.drexel.psal.anonymouth.gooie;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
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
	private ActionListener translateSentenceListener;
	
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
		translator = new TranslatorThread(main);
		
		initListeners();
	}

	/**
	 * Initializes all listeners relating to the translations tab
	 */
	public void initListeners() {		
		translateSentenceListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				/**
				 * We're forcing the translations progress panel here instead of letting the the TranslationsPanel
				 * eventually change it since this sometimes takes a second or two but we want the progress bar to
				 * replace the button near immediately so the user knows something's happening (and also to prevent
				 * them from clicking on it numerous times because they don't think it did anything)
				 */
				SwingUtilities.invokeLater(new Runnable() {
					//If it's not in a invoke later thread, it doesn't immediately update
					@Override
					public void run() {
						translationsPanel.switchToProgressPanel();
					}
				});
				
				translator.load(EditorDriver.taggedDoc.getSentenceNumber(EditorDriver.sentToTranslate));
				translationsPanel.showTranslations(EditorDriver.taggedDoc.getSentenceNumber(EditorDriver.sentToTranslate));
			}
		};
		main.translateSentenceButton.addActionListener(translateSentenceListener);
		
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
					main.translateSentenceButton.setEnabled(true);
					translator.reset();
					EditorDriver.taggedDoc.deleteTranslations();
					
					try {
						Thread.sleep(500);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
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
		EditorDriver.backedUpTaggedDoc = new TaggedDocument(EditorDriver.taggedDoc);
		main.versionControl.addVersion(EditorDriver.backedUpTaggedDoc, main.documentPane.getCaret().getDot());
		
		main.saved = false;
		InputFilter.ignoreTranslation = true;
		EditorDriver.removeReplaceAndUpdate(main, EditorDriver.sentToTranslate, translationsPanel.translationsMap.get(e.getActionCommand()).getUntagged(false), true);
		translator.replace(EditorDriver.taggedDoc.getSentenceNumber(EditorDriver.sentToTranslate), translationsPanel.current);
		
		main.anonymityBar.updateBar();
		main.wordSuggestionsDriver.placeSuggestions();
		
		main.translationsHolderPanel.removeAll();
		main.notTranslated.setText("Sentence has not been translated yet, please wait or work on already translated sentences.");
		main.translationsHolderPanel.add(main.notTranslated, "");
		main.translationsHolderPanel.revalidate();
		main.translationsHolderPanel.repaint();
	}
}