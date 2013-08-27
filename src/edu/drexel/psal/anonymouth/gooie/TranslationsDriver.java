package edu.drexel.psal.anonymouth.gooie;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import edu.drexel.psal.anonymouth.utils.TaggedDocument;
import edu.drexel.psal.anonymouth.utils.TranslatorThread;
import edu.drexel.psal.jstylo.generics.Logger;

/**
 * All listeners relating to the translations including:
 * <ul>
 * <li>Translate sentence button</li>
 * <li>Reset translator button (if shown)</li>
 * </ul>
 * This driver, unlike most others in Anonymouth, also acts as a mouse listener itself
 * (so it can be added to every translation swap button instance easily), so in order to
 * truly initialize all listeners for this driver it must be initilazed and ALSO added to
 * each translation swap button instance.
 * 
 * @author Marc Barrowclift
 */

public class TranslationsDriver implements MouseListener {
	
	//Constants
	private final String NAME = "( "+this.getClass().getSimpleName()+" ) - ";

	//Listeners
	private ActionListener resetTranslatorListener;
	private ActionListener translateSentenceListener;
	private ActionListener helpTranslationListener;
	
	//Anonymouth class instances
	private GUIMain main;
	protected TranslatorThread translator;
	private TranslationsPanel translationsPanel;
	
	//Others
	private String actionCommand;

	/**
	 * Constructor, automatically initializes all listeners associated with translations
	 * including:
	 * <ul>
	 * <li>Translate sentence button</li>
	 * <li>Reset translator button (if shown)</li>
	 * </ul>
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
				
				translator.load(main.editorDriver.taggedDoc.getSentenceNumber(main.editorDriver.sentNum));
				translationsPanel.updateTranslationsPanel(
						main.editorDriver.taggedDoc.getSentenceNumber(main.editorDriver.sentNum));
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
					main.editorDriver.taggedDoc.deleteTranslations();
					
					try {
						Thread.sleep(500);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
			}
		};
		main.resetTranslator.addActionListener(resetTranslatorListener);
		
		helpTranslationListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(main,
						"<html>" +
						"Sometimes it's difficult to think of different ways to phrase<br>" +
						"or structure a sentence even if you know what you're supposed<br>" +
						"to change.<br><br>" +
						
						"This is where translations can help. For sentences you have<br>" +
						"difficulty with click the \"Translate Sentence\" button and<br>" +
						"Anonymouth will translate your sentence into 15 different <br>" +
						"languages and back to English again. In some cases this can<br>" +
						"provide you with new ideas and help break through a writer's<br>" +
						"block (or if you're lucky, provides you with a perfect<br>" +
						"restructured sentence, in which case click the translation's<br>" +
						"arrow to swap it in automatically)" +
						"</html>",
						"Translations Help",
						JOptionPane.INFORMATION_MESSAGE,
						ThePresident.dialogIcon);
			}
		};
		main.translationHelpButton.addActionListener(helpTranslationListener);
	}
	
	/**
	 * Acts as the "ActionListener" to the translation swap arrow by immediately swapping
	 * the sentence in the working document with the relevant translation, clearing all
	 * translations from the translations holder scroll pane (since they were for the old sentence
	 * not the new one), and switching the translations top panel back to the default button panel
	 * with the "Translate Sentence", "?", and "Reset" button if shown.
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		Logger.logln(NAME+"User clicked the Translation swap-in arrow button");
		actionCommand = ((SwapButtonPanel)e.getComponent()).getActionCommand();
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				translationsPanel.switchToButtonPanel();
			}
		});
		
		main.editorDriver.pastTaggedDoc = new TaggedDocument(main.editorDriver.taggedDoc);
		main.versionControl.addVersion(main.editorDriver.pastTaggedDoc, main.documentPane.getCaret().getDot());

		main.saved = false;
		main.editorDriver.updateSentence(
				main.editorDriver.sentNum,
				translationsPanel.translationsMap.get(actionCommand).getUntagged(false));
		main.editorDriver.refreshEditor();

		main.translationsHolderPanel.removeAll();
		main.notTranslated.setText("");
		main.translationsHolderPanel.add(main.notTranslated, "");
		main.translationsHolderPanel.revalidate();
		main.translationsHolderPanel.repaint();
	}
	
	/**
	 * For when the mouse hovers over a particular translation's swap arrow, we want to
	 * immediately being the hoverOn Swing Worker thread to animate a nice "hover" animation
	 */
	@Override
	public void mouseEntered(final MouseEvent e) {
		SwapButtonPanel panel = ((SwapButtonPanel)e.getComponent());
		panel.readyHoverOnThread();
		panel.hoverOn.execute();
	}
	
	/**
	 * For when the mouse exits hovering over a particular translation's swap arrow, we want
	 * to immediately begin the hoverOff Swing Worker thread to animate a nice "hover" turn off
	 * animation 
	 */
	@Override
	public void mouseExited(final MouseEvent e) {
		SwapButtonPanel panel = ((SwapButtonPanel)e.getComponent());
		panel.readyHoverOffThread();
		panel.hoverOff.execute();
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {}
	@Override
	public void mousePressed(MouseEvent e) {}
}