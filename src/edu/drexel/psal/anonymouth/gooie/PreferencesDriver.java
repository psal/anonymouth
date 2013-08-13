package edu.drexel.psal.anonymouth.gooie;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.drexel.psal.jstylo.generics.Logger;

/**
 * Exists to help organize the previously growing GeneralSettingsFrame into two main classes following the structure the rest of Anonymouth
 * Follows (NAMEwindow/frame/Pane and NAMEDriver), where the first sets up all the components, frames, and panels of the window, panel, or
 * tab and the second handles all the component listeners. 
 * @author Marc Barrowclift
 *
 */
public class PreferencesDriver {
	
	//Constants
	private static final String NAME = "( PreferencesDriver ) - ";
	private final String TRANSWARNING = 
			"<html><left>"+
			"<center><b><font color=\"#FF0000\" size = 6>WARNING:</font></b></center>" +
			"Anonymouth provides translations functionality that will help obsure your<br>" +
			"style by translating your document into multiple languages and back again.<br>" +
			"THIS MEANS THAT YOUR SENTENCES WILL BE SENT OFF REMOTELY TO<br>" +
			"MICROSOFT BING.<br><br>" +
			"This feature is turned off by default, and if you desire to use this feature<br>" +
			"and understand the risks you may turn it on by...<br><br>" +
			"FOR MAC:<br>" +
			"     <center><code>Anonymouth > Preferences > Tick the translations option</code></center>" +
			"FOR ALL OTHER OPERATING SYSTEMS:<br>" + 
			"     <center><code>Settings > Preferences > Tick the translations option</code></center>" +
			"</left></div></html>";
	
	//various variables
	private GUIMain main;
	private PreferencesWindow prefWin;
	private int prevFeatureValue;
	private int prevThreadValue;

	//Listeners
	private ActionListener autoSaveListener;
	private ActionListener warnQuitListener;
	private ChangeListener maxFeaturesListener;
	private ChangeListener numOfThreadsListener;
	private ActionListener resetListener;
	private ActionListener translationsListener;
	private ChangeListener tabbedPaneListener;
	private ActionListener fontSizeListener;
	private ActionListener highlightColorListener;
	private ActionListener highlightSentsListener;
	private KeyListener maxFeaturesBoxListener;
	private KeyListener numOfThreadsBoxListener;
	private ActionListener showWarningsListener;
	private ActionListener highlightElemsListener;
	private ActionListener versionAutoSaveListener;
	private ActionListener filterAddWordsListener;
	
	public PreferencesDriver(GUIMain main, PreferencesWindow prefWin) {
		this.main = main;
		this.prefWin = prefWin;
		
		prevFeatureValue = PropertiesUtil.getMaximumFeatures();
	}

	/**
	 * Initializes and adds all preferences window listeners
	 */
	public void initListeners() {
		filterAddWordsListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (prefWin.filterAddWords.isSelected()) {
					PropertiesUtil.setFilterAddSuggestions(true);
					main.wordSuggestionsDriver.setFilterWordsToAdd(true);
					main.wordSuggestionsDriver.placeSuggestions();
					
					Logger.logln(NAME+"Filter Words to Add checkbox checked");
				} else {
					PropertiesUtil.setFilterAddSuggestions(false);
					main.wordSuggestionsDriver.setFilterWordsToAdd(false);
					main.wordSuggestionsDriver.placeSuggestions();
					
					Logger.logln(NAME+"Filter Words to Add checkbox unchecked");
				}
			}
		};
		prefWin.filterAddWords.addActionListener(filterAddWordsListener);
		
		highlightSentsListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (prefWin.highlightSent.isSelected()) {
					PropertiesUtil.setHighlightSents(true);
					EditorDriver.doHighlight = true;
					if (EditorDriver.highlightEngine.isSentenceHighlighted())
						EditorDriver.moveHighlight(main, EditorDriver.selectedSentIndexRange);
					
					Logger.logln(NAME+"Highlight Sents checkbox checked");
				} else {
					PropertiesUtil.setHighlightSents(false);
					EditorDriver.doHighlight = false;
					if (EditorDriver.highlightEngine.isSentenceHighlighted())
						EditorDriver.highlightEngine.removeSentenceHighlight();
					
					Logger.logln(NAME+"Highlight Sents checkbox unchecked");
				}
			}
		};
		prefWin.highlightSent.addActionListener(highlightSentsListener);
		
		highlightColorListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int color = prefWin.sentHighlightColors.getSelectedIndex();
				PropertiesUtil.setHighlightColor(color);
				
				if (EditorDriver.taggedDoc != null) {
					EditorDriver.highlightEngine.setSentHighlightColor(PropertiesUtil.getHighlightColor());
					EditorDriver.moveHighlight(main, EditorDriver.selectedSentIndexRange);
				}
			}
		};
		prefWin.sentHighlightColors.addActionListener(highlightColorListener);
		
		fontSizeListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				PropertiesUtil.setFontSize(prefWin.fontSizes.getSelectedItem().toString());
				main.normalFont = new Font("Ariel", Font.PLAIN, PropertiesUtil.getFontSize());
				main.documentPane.setFont(main.normalFont);
			}
		};
		prefWin.fontSizes.addActionListener(fontSizeListener);
		
		tabbedPaneListener = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (prefWin.preferencesWindow == null)
					return;
				
				if (prefWin.tabbedPane.getSelectedIndex() == 0) {
					resize(prefWin.generalHeight);
					assertValues();
				} else if (prefWin.tabbedPane.getSelectedIndex() == 1) {
					resize(prefWin.defaultsHeight);
					assertValues();
				} else {
					resize(prefWin.advancedHeight);
				}
			}
		};
		prefWin.tabbedPane.addChangeListener(tabbedPaneListener);
		
		autoSaveListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {			
				if (prefWin.autoSave.isSelected()) {
					PropertiesUtil.setAutoSave(true);
					prefWin.warnQuit.setSelected(false);
					PropertiesUtil.setWarnQuit(false);
					prefWin.warnQuit.setEnabled(false);
					Logger.logln(NAME+"Auto-save checkbox checked");
				} else {
					PropertiesUtil.setAutoSave(false);
					prefWin.warnQuit.setEnabled(true);
					Logger.logln(NAME+"Auto-save checkbox unchecked");
				}
			}
		};
		prefWin.autoSave.addActionListener(autoSaveListener);
		
		warnQuitListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (prefWin.warnQuit.isSelected()) {
					PropertiesUtil.setWarnQuit(true);
					Logger.logln(NAME+"Warn on quit checkbox checked");
				} else {
					PropertiesUtil.setWarnQuit(false);
					Logger.logln(NAME+"Warn on quit checkbox unchecked");
				}
			}
		};
		prefWin.warnQuit.addActionListener(warnQuitListener);
		
		translationsListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Logger.logln(NAME+"Translations checkbox clicked");
				
				if (prefWin.translations.isSelected()) {
					Object[] buttons = {"Ok", "Cancel"};
					int answer = JOptionPane.showOptionDialog(main.preferencesWindow,
							TRANSWARNING,
							"Please Be Aware!",
							JOptionPane.OK_CANCEL_OPTION,
							JOptionPane.WARNING_MESSAGE,
							null, buttons, 1);
					if (answer == 0) {
						if (main.processed)
							main.resetTranslator.setEnabled(true);
						PropertiesUtil.setDoTranslations(true);
						
						if (BackendInterface.processed) {
							answer = JOptionPane.showOptionDialog(main.preferencesWindow,
									"Being translating now?",
									"Begin Translations",
									JOptionPane.YES_NO_OPTION,
									JOptionPane.QUESTION_MESSAGE,
									null, null, null);
							
							if (answer == JOptionPane.YES_OPTION) {
								main.translationsDriver.translator.load(EditorDriver.taggedDoc.getTaggedSentences());
								main.translationsPanel.showTranslations(EditorDriver.taggedDoc.getSentenceNumber(EditorDriver.sentToTranslate));
								
								main.startTranslations.setEnabled(false);
								main.stopTranslations.setEnabled(true);
							} else {
								main.startTranslations.setEnabled(true);
								main.stopTranslations.setEnabled(false);
								main.translationsPanel.showTranslations(EditorDriver.taggedDoc.getSentenceNumber(EditorDriver.sentToTranslate));
							}
						} else {
							main.notTranslated.setText("Please process your document to recieve translation suggestions.");
							main.translationsHolderPanel.add(main.notTranslated, "");
						}
					}					
				} else {
					main.resetTranslator.setEnabled(false);
					main.translationsDriver.translator.reset();
					PropertiesUtil.setDoTranslations(false);
					main.notTranslated.setText("You have turned translations off.");
					main.translationsHolderPanel.add(main.notTranslated, "");
					main.startTranslations.setEnabled(false);
					main.stopTranslations.setEnabled(false);
				}
			}
		};
		prefWin.translations.addActionListener(translationsListener);
		
		maxFeaturesListener = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				PropertiesUtil.setMaximumFeatures(prefWin.maxFeaturesSlider.getValue());
				prefWin.maxFeaturesBox.setText(Integer.toString(PropertiesUtil.getMaximumFeatures()));
				prevFeatureValue = prefWin.maxFeaturesSlider.getValue();
			}	
		};
		prefWin.maxFeaturesSlider.addChangeListener(maxFeaturesListener);
		
		numOfThreadsListener = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				PropertiesUtil.setThreadCount(prefWin.numOfThreadsSlider.getValue());
				prefWin.numOfThreadsBox.setText(Integer.toString(PropertiesUtil.getThreadCount()));
				prevThreadValue = prefWin.numOfThreadsSlider.getValue();
			}
		};
		prefWin.numOfThreadsSlider.addChangeListener(numOfThreadsListener);
		
		resetListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Logger.logln(NAME+"resetAll button clicked");
				
				int answer = 0;
				
				answer = JOptionPane.showConfirmDialog(null,
						"Are you sure you want to resetAll all preferences?\nThis will override your changes.",
						"resetAll Preferences",
						JOptionPane.WARNING_MESSAGE,
						JOptionPane.YES_NO_CANCEL_OPTION);
				
				if (answer == 0) {
					try {
						Logger.logln(NAME+"resetAll progressing...");
						//resets everything in the prop file to their default values
						PropertiesUtil.reset();
						
						//updating the GUI to reflect the changes
						//general
						prefWin.warnQuit.setSelected(PropertiesUtil.getWarnQuit());
						prefWin.autoSave.setSelected(PropertiesUtil.getAutoSave());
						prefWin.translations.setSelected(PropertiesUtil.getDoTranslations());
						prefWin.showWarnings.setSelected(PropertiesUtil.getWarnAll());
						
						//editor
						prefWin.highlightElems.setSelected(PropertiesUtil.getAutoHighlight());
						prefWin.highlightSent.setSelected(PropertiesUtil.getHighlightSents());
						prefWin.fontSizes.setSelectedItem(PropertiesUtil.getFontSize());
						
						//advanced
						prefWin.numOfThreadsSlider.setValue(PropertiesUtil.getThreadCount());
						prefWin.maxFeaturesSlider.setValue(PropertiesUtil.getMaximumFeatures());
						prefWin.versionAutoSave.setSelected(PropertiesUtil.getVersionAutoSave());
						Logger.logln(NAME+"resetAll complete");
					} catch (Exception e) {
						Logger.logln(NAME+"Error occurred during resetAll");
					}
				} else {
					Logger.logln(NAME+"User cancelled resetAll");
				}
			}
		};
		prefWin.resetAll.addActionListener(resetListener);
		
		maxFeaturesBoxListener = new KeyListener() {			
			@Override
			public void keyTyped(KeyEvent e) {}
			@Override
			public void keyPressed(KeyEvent e) {}
			
			@Override
			public void keyReleased(KeyEvent e) {
				int number = -1;
				try {
					if (prefWin.maxFeaturesBox.getText().equals("")) {
						prefWin.maxFeaturesBox.setText("");
					} else {
						number = Integer.parseInt(prefWin.maxFeaturesBox.getText());
						
						if (number > 1000) {
							prefWin.maxFeaturesBox.setText(Integer.toString(prevFeatureValue));
							number = prevFeatureValue;
						} else {
							prefWin.maxFeaturesBox.setText(Integer.toString(number));
						}
					}
				} catch (Exception e1) {
					prefWin.maxFeaturesBox.setText(Integer.toString(prevFeatureValue));
				}
				
				if (number != -1) {
					prevFeatureValue = number;
				}
				
				if (prevFeatureValue >= 300) {
					PropertiesUtil.setMaximumFeatures(prevFeatureValue);
					prefWin.maxFeaturesSlider.setValue(prevFeatureValue);
				}
			}
		};
		prefWin.maxFeaturesBox.addKeyListener(maxFeaturesBoxListener);
		
		numOfThreadsBoxListener = new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {}
			@Override
			public void keyPressed(KeyEvent e) {}
			
			@Override
			public void keyReleased(KeyEvent e) {
				int number = -1;
				try {
					if (prefWin.numOfThreadsBox.getText().equals("")) {
						prefWin.numOfThreadsBox.setText("");
					} else {
						number = Integer.parseInt(prefWin.numOfThreadsBox.getText());
						
						if (number > 8) {
							prefWin.numOfThreadsBox.setText(Integer.toString(prevThreadValue));
							number = prevThreadValue;
						} else {
							prefWin.numOfThreadsBox.setText(Integer.toString(number));
						}
					}
				} catch (Exception e1) {
					prefWin.numOfThreadsBox.setText(Integer.toString(prevThreadValue));
				}
				
				if (number != -1) {
					prevThreadValue = number;
				}
				
				if (prevThreadValue >= 1) {
					PropertiesUtil.setMaximumFeatures(prevThreadValue);
					prefWin.numOfThreadsSlider.setValue(prevThreadValue);
				}
			}
		};
		prefWin.numOfThreadsBox.addKeyListener(numOfThreadsBoxListener);
		
		showWarningsListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (prefWin.showWarnings.isSelected()) {
					PropertiesUtil.setWarnAll(true);
					Logger.logln(NAME+"Show all warnings checkbox checked");
				} else {
					PropertiesUtil.setWarnAll(false);
					Logger.logln(NAME+"Show all warnings checkbox unchecked");
				}
			}
		};
		prefWin.showWarnings.addActionListener(showWarningsListener);
		
		highlightElemsListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (prefWin.highlightElems.isSelected()) {
					PropertiesUtil.setAutoHighlight(true);
					EditorDriver.autoHighlight = true;
					EditorDriver.highlightEngine.addAutoRemoveHighlights(EditorDriver.selectedSentIndexRange[0], EditorDriver.selectedSentIndexRange[1]);
					Logger.logln(NAME+"Auto highlights checkbox checked");
				} else {
					PropertiesUtil.setAutoHighlight(false);
					EditorDriver.autoHighlight = false;
					EditorDriver.highlightEngine.removeAutoRemoveHighlights();
					Logger.logln(NAME+"Auto highlights checkbox unchecked");
				}
			}
		};
		prefWin.highlightElems.addActionListener(highlightElemsListener);
		
		versionAutoSaveListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (prefWin.versionAutoSave.isSelected()) {
					PropertiesUtil.setVersionAutoSave(true);
					ThePresident.should_Keep_Auto_Saved_Anonymized_Docs = true;
					Logger.logln(NAME+"Version auto save checkbox checked");
				} else {
					PropertiesUtil.setVersionAutoSave(false);
					ThePresident.should_Keep_Auto_Saved_Anonymized_Docs = false;
					Logger.logln(NAME+"Version auto save checkbox unchecked");
				}
			}
		};
		prefWin.versionAutoSave.addActionListener(versionAutoSaveListener);
	}
	
	/**
	 * Provides a nice animation when resizing the window
	 * @param newSize - The new height of the window
	 */
	public void resize(int newSize) {
		int curHeight = prefWin.getHeight();
		
		//If the new height is larger we need to grow the window height
		if (newSize >= curHeight) {
			for (int h = curHeight; h <= newSize; h+=10) {
				prefWin.setSize(new Dimension(500, h));
			}
		} else { //If the new height is smaller we need to shrink the window height
			for (int h = curHeight; h >= newSize; h-=10) {
				prefWin.setSize(new Dimension(500, h));
			}
		}

		prefWin.setSize(new Dimension(500, newSize)); //This is to ensure that our height is the desired height.
	}
	
	/**
	 * Used to assert that the values entered in the text fields for the advanced tab are valid, and fixing them if not.
	 */
	protected void assertValues() {
		int feat = PropertiesUtil.getMaximumFeatures();
		int thread = PropertiesUtil.getThreadCount();
		if (feat < 300 || feat > 1000)
			PropertiesUtil.setMaximumFeatures(PropertiesUtil.defaultFeatures);
		if (thread < 1 || thread > 8)
			PropertiesUtil.setThreadCount(PropertiesUtil.defaultThreads);
	}
}
