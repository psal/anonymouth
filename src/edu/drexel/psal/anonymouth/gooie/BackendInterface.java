package edu.drexel.psal.anonymouth.gooie;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import com.jgaap.generics.Document;

import edu.drexel.psal.ANONConstants;
import edu.drexel.psal.anonymouth.engine.DataAnalyzer;
import edu.drexel.psal.anonymouth.engine.DocumentMagician;
import edu.drexel.psal.anonymouth.helpers.ErrorHandler;
import edu.drexel.psal.anonymouth.utils.ConsolidationStation;
import edu.drexel.psal.anonymouth.utils.FunctionWords;
import edu.drexel.psal.anonymouth.utils.TaggedDocument;
import edu.drexel.psal.anonymouth.utils.Tagger;
import edu.drexel.psal.jstylo.generics.Logger;
import edu.drexel.psal.jstylo.generics.ProblemSet;

public class BackendInterface {

	private final String NAME = "( BackendInterface ) - ";
	private ProgressWindow pw;
	public static Boolean processed = false;
	private FunctionWords functionWords;

	protected static BackendInterface bei = new BackendInterface();

	public class GUIThread implements Runnable {
		GUIMain main;

		public GUIThread(GUIMain main) {

			this.main = main;
		}

		public void run() {}
	}

	/* ========================
	 * documents tab operations
	 * ========================
	 */

	// -- none --
	// all operations are fast, so no backend threads are ran.


	/**
	 * documents tab >> create new problem set
	 */
	protected void docTabCreateNewProblemSet(GUIMain main) {
		Logger.logln(NAME+"Create new problem set");
		(new Thread(bei.new DocTabNewProblemSetButtonClick(main))).start();
	}

	public class DocTabNewProblemSetButtonClick extends GUIThread {

		public DocTabNewProblemSetButtonClick(GUIMain main) {
			super(main);
		}

		public void run() {
			Logger.logln(NAME+"Backend: create new problem set thread started.");

			// initialize probelm set
			main.preProcessWindow.ps = new ProblemSet();
			main.preProcessWindow.ps.setTrainCorpusName(main.preProcessWindow.DEFAULT_TRAIN_TREE_NAME);
			main.preProcessDriver.updateAllComponents();

			Logger.logln(NAME+"Backend: create new problem set thread finished.");
		}
	}

	protected void runVerboseOutputWindow(GUIMain main) {
		new Thread(bei.new RunVerboseOutputWindow(main)).start();
	}

	public class RunVerboseOutputWindow extends GUIThread {
		public RunVerboseOutputWindow(GUIMain main) {
			super(main);
		}

		public void run() {
			new Console();
		}
	}

	protected static void preTargetSelectionProcessing(GUIMain main,DataAnalyzer wizard, DocumentMagician magician) {
		(new Thread(bei.new PreTargetSelectionProcessing(main,wizard,magician))).start();
	}

	public class PreTargetSelectionProcessing extends GUIThread {
		private DataAnalyzer wizard;
		private DocumentMagician magician;		

		public PreTargetSelectionProcessing(GUIMain main,DataAnalyzer wizard, DocumentMagician magician) {
			super(main);
			this.wizard = wizard;
			this.magician = magician;
		}

		public String getDocFromCurrentTab()
		{
			return main.documentPane.getText();
		}

		public void run() {
			try {
				pw = new ProgressWindow("Processing...", main);
				pw.run();

				processed = true;
				DocumentMagician.numProcessRequests++;
				String tempDoc = "";
				functionWords = new FunctionWords();

				if (EditorDriver.isFirstRun == true) {
					functionWords.run();
					tempDoc = getDocFromCurrentTab();
					Logger.logln(NAME+"Process button pressed for first time (initial run) in editor tab");

					pw.setText("Extracting and Clustering Features...");
					try {
						wizard.runInitial(magician, main.ppAdvancedDriver.cfd, main.ppAdvancedWindow.classifiers.get(0));
						pw.setText("Initializing Tagger...");
						Tagger.initTagger();
						pw.setText("Classifying Documents...");
						magician.runWeka();
						wizard.runClusterAnalysis(magician);
						ClustersDriver.initializeClusterViewer(main,false);
					} catch(Exception e) {
						pw.stop();
						ErrorHandler.fatalProcessingError(e);
					}
				} else {
					Logger.logln(NAME+"Process button pressed to re-process document to modify.");
					tempDoc = getDocFromCurrentTab();
					if(tempDoc.equals("") == true) {
						JOptionPane.showMessageDialog(null,
								"It is not possible to process an empty document.",
								"Document processing error",
								JOptionPane.ERROR_MESSAGE,
								null);
					} else {
						magician.setModifiedDocument(tempDoc);

						pw.setText("Extracting and Clustering Features...");
						try {
							wizard.reRunModified(magician);
							pw.setText("Initialize Cluster Viewer...");
							ClustersDriver.initializeClusterViewer(main,false);
							pw.setText("Classifying Documents...");
							magician.runWeka();
						} catch (Exception e) {
							pw.stop();
							ErrorHandler.fatalProcessingError(e);
						}
					}
				}

				EditorDriver.theFeatures = wizard.getAllRelevantFeatures();
				Logger.logln(NAME+"The Features are: "+EditorDriver.theFeatures.toString());

				if (EditorDriver.isFirstRun) {
					ConsolidationStation.toModifyTaggedDocs.get(0).makeAndTagSentences(main.documentPane.getText(), true);
					
					List<Document> sampleDocs = magician.getDocumentSets().get(0);
					int size = sampleDocs.size();
					ConsolidationStation.otherSampleTaggedDocs = new ArrayList<TaggedDocument>();
					for (int i = 0; i < size; i++) {
						ConsolidationStation.otherSampleTaggedDocs.add(new TaggedDocument(sampleDocs.get(i).stringify()));
					}
				} else
					ConsolidationStation.toModifyTaggedDocs.get(0).makeAndTagSentences(main.documentPane.getText(), false);

				Map<String,Map<String,Double>> wekaResults = magician.getWekaResultList();
				Logger.logln(NAME+" ****** WEKA RESULTS for session '"+ThePresident.sessionName+" process number : "+DocumentMagician.numProcessRequests);
				Logger.logln(NAME+wekaResults.toString());
				makeResultsChart(wekaResults, main);

				
				main.anonymityBar.updateBar();
				if (EditorDriver.isFirstRun)
					main.anonymityBar.setMaxFill(EditorDriver.taggedDoc.getMaxChangeNeeded());
				main.anonymityBar.updateBar();
				main.anonymityBar.showFill(true);
				
				main.wordSuggestionsDriver.placeSuggestions();

				EditorDriver.setAllDocTabUseable(true, main);	
				
				//needed so if the user has some strange spacing for their first sentence we are placing the caret where the sentence actually begins (and thus highlighting it, otherwise it wouldn't)
				int caret = 0;
				while (Character.isWhitespace(main.documentPane.getText().charAt(caret))) {
					caret++;
				}

				EditorDriver.ignoreNumActions = 1; // must be set to 1, otherwise "....setDot(0)" (2 lines down) will screw things up when it fires the caretUpdate listener.
				if (!EditorDriver.isFirstRun)
					InputFilter.ignoreTranslation = true;
				main.documentPane.setText(EditorDriver.taggedDoc.getUntaggedDocument(false));// NOTE this won't fire the caretListener because (I THINK) this method isn't in a listener, because setting the text from within a listener (directly or indirectly) DOES fire the caretUpdate.
				main.documentPane.getCaret().setDot(caret); // NOTE However, THIS DOES fire the caretUpdate, because we are messing with the caret itself.
				main.documentPane.setCaretPosition(caret); // NOTE And then this, again, does not fire the caretUpdate
				EditorDriver.currentCaretPosition = caret;
				EditorDriver.ignoreNumActions = 0; //We MUST reset this to 0 because, for whatever reason, sometimes setDot() does not fire the caret listener, so ignoreNumActions is never reset. This is to ensure it is.
				
				int[] selectedSentInfo = EditorDriver.calculateIndicesOfSentences(0)[0];
				EditorDriver.selectedSentIndexRange[0] = selectedSentInfo[1];
				EditorDriver.selectedSentIndexRange[1] = selectedSentInfo[2];
				EditorDriver.moveHighlight(main, EditorDriver.selectedSentIndexRange);

				//no longer automatically translating even if checkmarked, only when the user clicks "start"
				//GUIMain.GUITranslator.load(DriverEditor.taggedDoc.getTaggedSentences());
				main.notTranslated.setText("");
				main.translationsHolderPanel.add(main.notTranslated, "");
				EditorDriver.charsInserted = 0; // this gets updated when the document is loaded.
				EditorDriver.charsRemoved = 0;	
				EditorDriver.caretPositionPriorToCharInsertion = 0;
				EditorDriver.isFirstRun = true;

				DictionaryBinding.init();//initializes the dictionary for wordNEt

				Logger.logln(NAME+"Finished in BackendInterface - postTargetSelection");

				main.resultsWindow.resultsLabel.setText("Re-Process your document to get updated ownership probability");
				main.documentScrollPane.getViewport().setViewPosition(new java.awt.Point(0, 0));

				EditorDriver.backedUpTaggedDoc = new TaggedDocument(EditorDriver.taggedDoc);

				main.processed = true;
				pw.stop();
				main.showGUIMain();
			} catch (Exception e) {
				e.printStackTrace();
				// Get current size of heap in bytes
				long heapSize = Runtime.getRuntime().totalMemory();

				// Get maximum size of heap in bytes. The heap cannot grow beyond this size.
				// Any attempt will result in an OutOfMemoryException.
				long heapMaxSize = Runtime.getRuntime().maxMemory();

				// Get amount of free memory within the heap in bytes. This size will increase
				// after garbage collection and decrease as new objects are created.
				long heapFreeSize = Runtime.getRuntime().freeMemory();
				Logger.logln(NAME+"Something happend. Here are the total, max, and free heap sizes:");
				Logger.logln(NAME+"Total: "+heapSize+" Max: "+heapMaxSize+" Free: "+heapFreeSize);
			}

			/*
			if (PropertiesUtil.showBarTutorial()) {
				//Needed, without it the Progress bar window just sort of chills around and doesn't close itself like it should (If showing
				//A JOptionPane, otherwise it's fine, weird.)
				/*
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				JOptionPane.showMessageDialog(null,
						"<html><left>" +
						"There are two main ways to identify how anonymous your document is, the <b>Anonymity Bar</b> and " +
						"the <b>Ownership<br>Certainty Graph</b>. While they may seem similar, they serve two different purposes:<br>" +
						"<ul><li><b>Anonymity Bar-</b> Tries to get you to move your document's features toward the best combination that<br>" +
						"it could find.This doesn't necessarily guarantee that you will be anonymous if full, and likewise that you will<br>" +
						"not be anonymous if low, but it will guarantee that the more full it is the closer your document will get to a<br>" +
						"lower classification (which is good!) while at the same time taking care to not simply \"copy\" a single author's<br>" +
						"style." +
						"<li><b>Ownership Certainty Graph-</b> This is Anonymouth making an educated guess as to who was the most likely<br>" +
						"author for the given test document with the given set of sample authors. This should give you a broad idea of<br>" +
						"where your document stands in comparison to these particular authors, and while it is helpful in getting a<br>" +
						"general idea it should not be used as an absolute confirmation.</ul>" +
						"</left></html>",
						"Understanding the Anonymity Bar and Ownership Certainity Graph",
						JOptionPane.INFORMATION_MESSAGE);
				PropertiesUtil.setBarTutorial(false);
			}
			*/
		}
	}

	public void makeResultsChart(Map<String,Map<String,Double>> resultMap, GUIMain main) {

		Iterator<String> mapKeyIter = resultMap.keySet().iterator();
		Map<String,Double> tempMap = resultMap.get(mapKeyIter.next()); 

		int numAuthors = DocumentMagician.numSampleAuthors+1;

		Object[] authors = (tempMap.keySet()).toArray();

		Object[] keyRing = tempMap.values().toArray();
		int maxIndex = 0;
		Double biggest = .01;
		
		double[][] predictionMapArray = new double[authors.length][2];
		for(int i = 0; i < numAuthors; i++){
			Double tempVal = ((Double)keyRing[i])*100;
			// compare PRIOR to rounding.
			if(biggest < tempVal){
				biggest = tempVal;
				maxIndex = i;
			}
			predictionMapArray[i][0] = tempVal;
			predictionMapArray[i][1] = i;
			
			if (((String)authors[i]).equals(ANONConstants.DUMMY_NAME)){
				authors[i] = "You";
			}
		}

		//Sort array of info gains from greatest to least
		Arrays.sort(predictionMapArray, new Comparator<double[]>() {
			@Override
			public int compare(final double[] first, final double[] second) {
				return ((-1)*((Double)first[0]).compareTo(((Double)second[0]))); // multiplying by -1 will sort from greatest to least, which saves work.
			}
		});

		for (int i = 0; i < numAuthors; i++){
			main.resultsWindow.addAttrib((String)authors[(int)predictionMapArray[i][1]], (int)(predictionMapArray[i][0] + .5));
		}

		EditorDriver.resultsMaxIndex = maxIndex;
		EditorDriver.chosenAuthor = (String)authors[maxIndex];
		EditorDriver.maxValue = (Object)biggest;

		main.resultsWindow.makeChart();
	}
}