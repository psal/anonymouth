package edu.drexel.psal.anonymouth.gooie;

//import edu.drexel.psal.anonymouth.utils.POS;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.JViewport;
import javax.swing.plaf.metal.MetalIconFactory;

import com.jgaap.JGAAPConstants;

import edu.drexel.psal.ANONConstants;
import edu.drexel.psal.anonymouth.helpers.ImageLoader;
import edu.drexel.psal.jstylo.generics.Logger;
/*
import com.wintertree.wthes.CompressedThesaurus;
import com.wintertree.wthes.LicenseKey;
import com.wintertree.wthes.TextThesaurus;
import com.wintertree.wthes.Thesaurus;
import com.wintertree.wthes.ThesaurusSession;
*/
import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.WordNetDatabase;

/**
 * Provides the support needed for the DictionaryConsole to function - hense, its name. 
 * @author Andrew W.E. McDonald
 * 
 */
public class DictionaryBinding {
	
	private final static String NAME = "( DictionaryBinding ) - ";

	protected static boolean wordSynSetUpdated = false;
	protected static String wordSynSetResult = "";
	protected static String currentWord = "";
	protected static boolean isFirstGramSearch = true;
	protected static ArrayList<String> allWords = new ArrayList<String>();
	
	public static void init() {
		System.setProperty("wordnet.database.dir", //ANONConstants.WORKING_DIR +"src"+JGAAPConstants.JGAAP_RESOURCE_PACKAGE+"wordnet" );
												   ANONConstants.WORKING_DIR +"src/edu/drexel/psal/resources/wordnet");
		try {
			readInAllWords();
		} catch (IOException e) {
			Logger.logln(NAME+"Failed to read in word list");
			e.printStackTrace();
		}
	}
	
	public static void initDictListeners (final DictionaryConsole dc) {
		init();
		
		dc.notFound.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Logger.logln(NAME+"Dictionary's 'not found' button clicked");
				JOptionPane.showMessageDialog(null,
						"If you entered a phrase, try an altered version if possible.\n" +
						"If your word wasn't found, try using an on-line thesaurus,\n" +
						"such as 'merriam-webster.com/dictionary/thesaurus'.\n" +
						"In the case that your character gram search failed to return\n" +
						"any usable words, the above two suggestions don't apply/appeal\n" +
						"to you, or a plethora of other possible issues arose:\n\n" +
						"just 'http://www.google.com' it.",
						"Google.",
						JOptionPane.PLAIN_MESSAGE,
						ThePresident.aboutLogo);			
			}
		});
		
		dc.wordSearchButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				currentWord = dc.wordField.getText();
				Logger.logln(NAME+"preparing to search for user string: '"+currentWord+"' in dictionary.");
				if (currentWord.trim().equals("") == false) {
					wordSynSetResult = "";
					WordNetDatabase wnd = WordNetDatabase.getFileInstance();
					Synset[] testSet = wnd.getSynsets(currentWord);
					int synNumber =1;
					int i;
					for(i = 0; i< testSet.length; i++){
						String [] wfs = testSet[i].getWordForms();
						String def = testSet[i].getDefinition();
						String [] use = testSet[i].getUsageExamples();
						int j;
						if(def.equals("") == false) {
							wordSynSetResult = wordSynSetResult + "(" + (i+1) + "): " + def + "\n"; 
						}
						else {
							wordSynSetResult=wordSynSetResult+"Synonym set "+i+" does not appear to have a defintion attached\n";
						}
						for (j = 0; j < wfs.length; j++) {
							try {
								//System.out.println("j = " + j);
								wordSynSetResult = wordSynSetResult + wfs[j]+" => "+ use[j]+"\n";
								synNumber++;
							} catch (ArrayIndexOutOfBoundsException aioobe) {}
						}
						wordSynSetResult = wordSynSetResult+"\n";
					}
					System.out.println(wordSynSetResult);
					wordSynSetUpdated = true;
					createTab("SynSet \""+ currentWord + "\"", wordSynSetResult,dc);
				}
			}			
		});
		
		dc.wordField.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent arg0) {}
			@Override
			public void keyReleased(KeyEvent arg0) {}
			@Override
			public void keyTyped(KeyEvent arg0) {
				Logger.logln(NAME+"User typing in word field");
				dc.wordField.setForeground(Color.black);
				dc.wordSearchButton.setSelected(true);
				dc.gramSearchButton.setSelected(false);
				dc.gramStartSearchButton.setSelected(false);
			}
		});
		
		dc.wordField.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				if (dc.wordField.getText().equals("word or phrase")) {
					dc.wordField.setText("");
				}
				dc.wordField.setForeground(Color.black);
			}
			
			public void focusLost(FocusEvent e) {
				if (dc.wordField.getText().isEmpty()) {
					dc.wordField.setText("word or phrase");
					dc.wordField.setForeground(Color.gray);
				}
			}
		});
		
		dc.gramField.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {}
			@Override
			public void keyReleased(KeyEvent e) {}
			@Override
			public void keyTyped(KeyEvent e) {
				Logger.logln(NAME+"User typing in gram field.");
				dc.gramField.setForeground(Color.black);
				dc.gramSearchButton.setSelected(true);
				dc.wordSearchButton.setSelected(false);
				dc.gramStartSearchButton.setSelected(false);
			}
		});
		
		dc.gramField.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				if (dc.gramField.getText().equals("char gram (e.g. ns )")) {
					dc.gramField.setText("");
				}
				dc.gramField.setForeground(Color.black);
			}
			
			public void focusLost(FocusEvent e) {
				if (dc.gramField.getText().isEmpty()) {
					dc.gramField.setText("char gram (e.g. ns )");
					dc.gramField.setForeground(Color.gray);
				}
			}
		});

		dc.gramStartField.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent arg0) {}
			@Override
			public void keyReleased(KeyEvent arg0) {}
			@Override
			public void keyTyped(KeyEvent e) {
				Logger.logln(NAME+"User typing in gram field.");
				dc.gramStartField.setForeground(Color.black);
				dc.gramStartSearchButton.setSelected(true);
				dc.wordSearchButton.setSelected(false);
				dc.gramSearchButton.setSelected(false);
			}
			
		});
		
		dc.gramStartField.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				if (dc.gramStartField.getText().equals("char gram (e.g. ns )")) {
					dc.gramStartField.setText("");
				}
				dc.gramStartField.setForeground(Color.black);
			}
			
			public void focusLost(FocusEvent e) {
				if (dc.gramStartField.getText().isEmpty()) {
					dc.gramStartField.setText("char gram (e.g. ns )");
					dc.gramStartField.setForeground(Color.gray);
				}
			}
		});
		
		dc.gramSearchButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Logger.logln(NAME+"Preparing to search for character grams");
				long startTime = System.currentTimeMillis();
				String theGram = dc.gramField.getText();
				if (!theGram.trim().equals("")) {
					scanAllWords(theGram,false,dc);
				}
				long endTime = System.currentTimeMillis();
				Logger.logln(NAME+"Search took " + (endTime - startTime) + "ms");
			}
		});
		
		dc.gramStartSearchButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				Logger.logln(NAME+"Preparing to search for character grams in the beginnning of the word");
				long startTime = System.currentTimeMillis();
				String theGram = dc.gramStartField.getText();
				if (!theGram.trim().equals("")) {
					scanAllWords(theGram,true,dc);
				}
				long endTime = System.currentTimeMillis();
				Logger.logln(NAME+"Search took " + (endTime - startTime) + "ms");
			}
			
		});
		
		//close console and return it to initial state for next time it is launched
		dc.closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Logger.logln(NAME+"Dictionary is being disposed of.. ");
				dc.dispose();
				dc.gramField.setText("char gram (e.g. ns )");
				dc.gramField.setForeground(Color.gray);
				dc.wordField.setText("word or phrase");
				dc.wordField.setForeground(Color.gray);
				dc.gramStartField.setText("char gram (e.g. ns )");
				dc.gramStartField.setForeground(Color.gray);
				dc.viewerTP.removeAll();
				//EditorDriver.dictDead = true;
			}
		});
		
		dc.addWindowListener(new WindowListener() {
			@Override
			public void windowActivated(WindowEvent arg0) {}
			@Override
			public void windowClosed(WindowEvent arg0) {
				Logger.logln(NAME+"Window close killed poor dictionary");
				//EditorDriver.dictDead = true;
			}
			@Override
			public void windowClosing(WindowEvent arg0) {}
			@Override
			public void windowDeactivated(WindowEvent arg0) {}
			@Override
			public void windowDeiconified(WindowEvent arg0) {}
			@Override
			public void windowIconified(WindowEvent arg0) {}
			@Override
			public void windowOpened(WindowEvent arg0) {}
		});
		
	}
	
	public static boolean readInAllWords() throws IOException {
		Logger.logln(NAME+"reading in comprehensive word list");
		FileReader fr = new FileReader(new File(ANONConstants.EXTERNAL_RESOURCE_PACKAGE + "words.txt"));
		BufferedReader buff = new BufferedReader(fr);
		String temp;
		
		while ((temp = buff.readLine()) != null) {
			StringTokenizer st = new StringTokenizer(temp);
			while (st.hasMoreTokens()) {
				String token = st.nextToken();
				allWords.add(token);
			}

		}
		buff.close();
		System.out.println("Done reading the word list");
		return true;
	}
	
	public static void createTab (String title, String resultsList, final DictionaryConsole dc) {
		
		//create the structure to hold the actual information of the tab
		JTextPane textPane = new JTextPane();
		textPane.setText(resultsList);
		textPane.setEditable(false);
		final JScrollPane content = new JScrollPane(textPane);
		
		//create an icon appropriate to later form a button based on it
		Icon icon = new ImageIcon(ImageLoader.class.getClass().getResource(ANONConstants.GRAPHICS+"closeIcon16.png"));
		
		//create a close button for each new tab and add a listener to it
		JButton button = new JButton(icon);	
		button.setPreferredSize(new Dimension(12,12));
		button.addActionListener(new ActionListener() {		
			@Override
			public void actionPerformed(ActionEvent e) {
				int closeTabNumber = dc.viewerTP.indexOfComponent(content);
				dc.viewerTP.removeTabAt(closeTabNumber);
			}	
		});
		
		//add the button and the title to each tab component
		final JPanel tab = new JPanel();
		tab.add(button);
		tab.add(new JLabel(title));
		tab.setOpaque(false);
		
		int tabCount = dc.viewerTP.getTabCount();
		//add each complete tab to the tabbed pane in the specified position 
		dc.viewerTP.addTab (title, content);
		dc.viewerTP.setTabComponentAt(tabCount, tab);
		dc.viewerTP.setSelectedIndex(tabCount);					//set the focus on the new tab

	}
	
	
	public static boolean scanAllWords(String nGram, boolean wordStart, DictionaryConsole dc) {
		Logger.logln(NAME+"Scanning all words for occurances of '"+nGram+"'");
		int i = 0;
		int max = allWords.size();
		String tabTitle;
		if (wordStart)
			tabTitle = "Starts with \"" + nGram +"\"";
		else
			tabTitle = "Contains \"" + nGram +"\"";
		
		StringBuilder builder = new StringBuilder();
		
		while (i < max) {
			String temp = allWords.get(i);
			if (wordStart) {
				if (temp.startsWith(nGram)) {
					builder.append(temp + "\n");
				}
			} else {
				if (temp.contains(nGram)) {
					builder.append(temp + "\n");
				}
			}
			i++;
		}
		
		createTab(tabTitle, builder.toString(), dc);
		return true;
	}
	
	//probably more refinement work needs to be done
	@SuppressWarnings("unused")
	public static String[] getSynonyms(String wordToFind) {
		wordSynSetResult = "";
		wordToFind = wordToFind.trim().toLowerCase();
		System.setProperty("wordnet.database.dir", "./bin/com/jgaap/resources/wordnet");
		WordNetDatabase wnd = WordNetDatabase.getFileInstance();
		Synset[] testSet = wnd.getSynsets(wordToFind);
		int synNumber =1;
		String [] wfs;
		
		for(int i = 0; i < testSet.length; i++) {
			wfs = testSet[i].getWordForms();
			
			//String [] use = testSet[i].getUsageExamples();
			int j;
			for (j = 0; j < wfs.length; j++) {
				try {
					if(!wordToFind.contains(wfs[j].toLowerCase())){
						wordSynSetResult = wordSynSetResult+"("+synNumber+"): "+wfs[j]+"\n";
						//Logger.logln(NAME+"Results for: "+wordToFind+"\n"+wordSynSetResult);
						synNumber++;
					}
				} catch(ArrayIndexOutOfBoundsException e) {
					e.printStackTrace();
					Logger.logln(NAME+"Caught an exception...");					
				}
			}
			return wfs;		
		}
		return null;//BIG PROBLEM
	}

	
}
