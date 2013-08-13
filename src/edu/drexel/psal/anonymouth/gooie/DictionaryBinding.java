package edu.drexel.psal.anonymouth.gooie;

//import edu.drexel.psal.anonymouth.utils.POS;
import edu.drexel.psal.ANONConstants;
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;

import com.jgaap.JGAAPConstants;

/**
 * Provides the support needed for the DictionaryConsole to function - hense, its name. 
 * @author Andrew W.E. McDonald
 *
 */
public class DictionaryBinding {
	
	private final static String NAME = "( DictionaryBinding ) - ";

	protected static boolean wordSynSetUpdated = false;
	protected static String wordSynSetResult = "";
	protected static String gramFindings = "";
	protected static ViewerTabGenerator vtg; 	
	protected static String currentWord = "";
	protected static boolean isFirstGramSearch = true;
	protected static ArrayList<String> allWords = new ArrayList<String>();
	
	public static void init() {
		System.setProperty("wordnet.database.dir", ANONConstants.WORKING_DIR +"src"+JGAAPConstants.JGAAP_RESOURCE_PACKAGE+"wordnet");
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
						//String def = testSet[i].getDefinition();
						String [] use = testSet[i].getUsageExamples();
						int j;
						//wordSynSetResult = wordSynSetResult+"Synonym set "+(i+1)+" for entered search '"+currentWord+"' :\n";
						//if(def.equals("") == false)
							//wordSynSetResult= wordSynSetResult +"Definition of synonym set "+(i+1)+" is: "+def+"\n";
						//else
							//wordSynSetResult=wordSynSetResult+"Synonym set "+i+" does not appear to have a defintion attached\n";
						for (j = 0; j < wfs.length; j++) {
							try {
								//wordSynSetResult = wordSynSetResult+"Synonym number ("+(j+1)+"): "+wfs[j]+"  => usage (if specified): "+use[j]+"\n";
								wordSynSetResult = wordSynSetResult+"("+synNumber+"): "+wfs[j]+" => "+ use[j]+"\n";
								synNumber++;
							} catch (ArrayIndexOutOfBoundsException aioobe) {}
						}
						//wordSynSetResult = wordSynSetResult+"\n";
					}
					vtg = new ViewerTabGenerator().generateTab(wordSynSetResult);
					wordSynSetUpdated = true;
					dc.viewerTP.addTab("syn: "+currentWord, null, vtg.jScrollPane1, null);
					dc.viewerTP.setSelectedComponent(vtg.jScrollPane1);
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
				dc.wordSearchButton.setSelected(true);
				dc.gramSearchButton.setSelected(false);
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
				dc.gramSearchButton.setSelected(true);
				dc.wordSearchButton.setSelected(false);
			}
		});	

		dc.gramSearchButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Logger.logln(NAME+"Preparing to search for character grams");
				String theGram = dc.gramField.getText();
				if (theGram.trim().equals("") == false) {

					if (isFirstGramSearch == true) {
						try {
							readInAndScan(theGram);
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						isFirstGramSearch = false;
					} else {
						scanAllWords(theGram);
					}

					vtg = new ViewerTabGenerator().generateTab(gramFindings);
					dc.viewerTP.addTab("gram: "+theGram, null, vtg.jScrollPane1, null);
					dc.viewerTP.setSelectedComponent(vtg.jScrollPane1);
				}
			}
		});
		
		dc.closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Logger.logln(NAME+"Dictionary is being disposed of.. ");
				dc.dispose();
				EditorDriver.dictDead = true;
			}
		});
		
		dc.addWindowListener(new WindowListener() {
			@Override
			public void windowActivated(WindowEvent arg0) {}
			@Override
			public void windowClosed(WindowEvent arg0) {
				Logger.logln(NAME+"Window close killed poor dictionary");
				EditorDriver.dictDead = true;
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
	
	public static boolean readInAndScan(String nGram) throws IOException {
		Logger.logln(NAME+"reading in comprehensive word list");
		FileReader fr = new FileReader(new File("./allWords.txt"));
		BufferedReader buff = new BufferedReader(fr);
		String temp;
		gramFindings = "";
		
		while ((temp = buff.readLine()) != null) {
			StringTokenizer st = new StringTokenizer(temp);
			while (st.hasMoreTokens()) {
				String token = st.nextToken();
				if (token.contains(nGram))
					gramFindings = gramFindings+ token+"\n";
				allWords.add(token);
			}
		}
		buff.close();
		return true;
	}
	
	public static boolean scanAllWords(String nGram) {
		Logger.logln(NAME+"Scanning all words for occurances of '"+nGram+"'");
		int i = 0;
		int max = allWords.size();
		gramFindings = "";
		
		while (i < max) {
			String temp = allWords.get(i);
			if (temp.contains(nGram))
				gramFindings = gramFindings + temp +"\n";
			i++;
		}
		return true;
	}
	
	//NOTE: Someone had begun work on passing in an additional String that represented the part of speech, but was not yet implemented in
	//the body of the method. As such, I removed it from the parameters for now just to get the DriverEditor words to remove and add
	//back online
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
					//wordSynSetResult = wordSynSetResult+"Synonym number ("+(j+1)+"): "+wfs[j]+"  => usage (if specified): "+use[j]+"\n";
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
			//wordSynSetResult = wordSynSetResult+"\n";
			return wfs;
			
		}
		return null;//BIG PROBLEM
	}

//	 public static void main(String args[]) {
//		 String [] temp=getSynonyms("walk", "verb");
//		 for(String s:temp){
//			 System.out.println(s);
//		 }
//	 }
	
}
