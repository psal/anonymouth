package edu.drexel.psal.anonymouth.utils;

import com.memetix.mst.language.Language;
import java.util.ArrayList;

import edu.drexel.psal.anonymouth.gooie.GUIMain;
import edu.drexel.psal.anonymouth.gooie.PropertiesUtil;
import edu.drexel.psal.jstylo.generics.Logger;
import edu.drexel.psal.jstylo.generics.Logger.LogOut;

/**
 * Manages fetching translations for every sentence in a thread and
 * adding/removing sentences from the translation "queue".
 * 
 * @author Marc Barrowclift
 * @author Unknown
 */

public class TranslatorThread implements Runnable {
	
	private final String NAME = "( TranslatorThread ) - ";

	//Static variables
	public static Boolean finished = false;
	public static Boolean noInternet = false;
	public static Boolean accountsUsed = false;
	public static Boolean stop = false;
	public static boolean addSent = false;

	//Variables
	private ArrayList<TaggedSentence> sentences = new ArrayList<TaggedSentence>(); // essentially the priority queue
	private TranslationFetcher translationFetcher;
	private GUIMain main;
	private TaggedSentence oldSentence;
	private TaggedSentence newSentence;
	private int currentSentNum = 1;
	private int currentLangNum = 1;
	private int stoppedLangNum = 0;
	private Thread transThread;

	/**
	 * Class that handles the 2-way translation of a given sentence. It starts
     * a new thread so the main thread doesn't freeze up. This allows it to
     * update the user about the progress of the translations.
     * 
	 * @param main 
	 *        GUIMain instance
	 */
	public TranslatorThread (GUIMain main) {
		this.main = main;
		translationFetcher = new TranslationFetcher(main);
	}

	/**
	 * Replaces the given "old" sentence in the translation sentence queue
     * with the new sentence and puts the newsentence first in the
     * translations queue.
     * 
	 * @param newSentence
	 *        The new sentence to get translations for.
	 * @param oldSentence
	 *        The old sentence the new one is replacing.
	 */
	public void replace(TaggedSentence newSentence, TaggedSentence oldSentence) {
		if (sentences.size() >= 1) {
			addSent = true;
			this.newSentence = newSentence;
			this.oldSentence = oldSentence;
		} else {
			if (accountsUsed == false) {
				load(newSentence);
			}
		}
	}

	/**
	 * Loads sentences into the translation queue. Newly added sentences take
     * priority. If translations arent running, it starts running. If
     * translations are already running, adds new sentences into the front of
     * the queue.
	 */
	public void load(TaggedSentence loaded)  {
		if (PropertiesUtil.getDoTranslations()) {
			sentences.add(loaded);
			transThread = new Thread(this);
			transThread.start(); // calls run below
		}
	}
	
	/**
	 * To be called for re-processing and when the user turns off the
     * translations. This essentially "freezes" the translations so they
	 * can be picked up right where they left off.
	 */
	//TODO Boolean value to completely reset translations for re-processing? Talk to Andrew.
	public void reset() {
		stop = true;
		
		sentences.clear();
		
		/**
		 * Not sure if not resetting these will break something down the line,
		 * it seems to work fine without it. Commented them out so that if the
		 * user turns off translations but changes their mind the translations
		 * will pick off where they left off AND Upon re-processing the
		 * document translations will not re-translate sentences that didn't
		 * change.
		 */
		currentSentNum = 1;
		stoppedLangNum = 0;
		currentLangNum = 1;
		finished = false;
		noInternet = false;
		accountsUsed = false;
		addSent = false;
	}

	/**
	 * Main translation thread
	 */
	@Override
	public void run() {
		stop = false; //Just making sure stop is now false since this is a new document translation (if it the code below that turns it off wasn't run).
		
		// set up the progress bar
		main.translationsProgressBar.setIndeterminate(false);
		main.translationsProgressBar.setMaximum(sentences.size() * translationFetcher.getUsedLangs().length);

		// translate all languages for each sentence, sorting the list based on anon index after each translation
		while (!sentences.isEmpty() && currentSentNum <= sentences.size()) {			
			if (!sentences.get(currentSentNum - 1).isTranslated()) {
				// Translate the sentence for each language				
				for (Language lang: translationFetcher.getUsedLangs()) {
					if (currentLangNum >= stoppedLangNum) {
						if (sentences.size() == 0) {
							stop = false;
							translationsEnded();
							main.translationsPanel.updateTranslationsPanel(new TaggedSentence(""));
							return;
						}
						
						String translation = translationFetcher.getTranslation(sentences.get(currentSentNum-1).getUntagged(false), lang);
						
						if (translation.equals("internet")) {
							Logger.logln(NAME+"No internet connection", LogOut.STDERR);
							noInternet = true;
							translationsEnded();
							main.translationsPanel.updateTranslationsPanel(new TaggedSentence(""));
							return;
						} else if (translation.equals("account")) {
							Logger.logln(NAME+"Account used up", LogOut.STDERR);
							reset();
							accountsUsed = true;
							translationsEnded();
							main.translationsPanel.updateTranslationsPanel(new TaggedSentence(""));
							return;
						} else if (stop) {
							stop = false;
							translationsEnded();
							main.translationsPanel.updateTranslationsPanel(new TaggedSentence(""));
							return;
						}
						
						currentLangNum++;
						TaggedSentence taggedTrans = new TaggedSentence(translation);
						taggedTrans.tagAndGetFeatures();
						sentences.get(currentSentNum-1).getTranslations().add(taggedTrans);
						sentences.get(currentSentNum-1).getTranslationNames().add(translationFetcher.getName(lang));
						sentences.get(currentSentNum-1).sortTranslations();
						String one = main.editorDriver.taggedDoc.getUntaggedSentences(false).get(main.editorDriver.sentNum).trim();
						String two = sentences.get(currentSentNum-1).getUntagged(false).trim();

						if (one.equals(two))
							main.translationsPanel.updateTranslationsPanel(sentences.get(currentSentNum-1));
						
						if (main.translationsProgressBar.getValue() + 1 <= main.translationsProgressBar.getMaximum())
							main.translationsProgressBar.setValue(main.translationsProgressBar.getValue() + 1);
						
						if (addSent) {
							addSent = false;
							sentences.add(currentSentNum, newSentence);
							if (sentences.contains(oldSentence))
								sentences.remove(oldSentence);
							else
								main.translationsProgressBar.setMaximum(sentences.size() * translationFetcher.getUsedLangs().length);
						
							if (currentSentNum - 1 >= 1)
								currentSentNum -= 1;
						}
					} else {
						currentLangNum++;
					}
				}
				stoppedLangNum = 0;
			}
			
			currentLangNum = 1;
			currentSentNum++;
		}
		translationsEnded();
	}
	
	/**
	 * Cleans up resources used by the translator at the end of translating all sentences.
	 */
	private void translationsEnded() {
		/**
		 * We're making another outside call to a "switchTo" method since we want the progress bar to immediately disappear
		 * when all translations are obtained (instead of having to wait a second or two for the TranslationsPanel to get to
		 * it).
		 */
		main.translationsPanel.switchToEmptyPanel();
		
		finished = true;
		sentences.clear();
		currentSentNum = 1;
		main.translationsProgressBar.setIndeterminate(false);
		main.translationsProgressBar.setValue(0);
		main.reProcessButton.setEnabled(true);
		
		transThread.interrupt();
	}
}
