package edu.drexel.psal.anonymouth.gooie;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import com.jgaap.generics.Document;

import edu.drexel.psal.ANONConstants;
import edu.drexel.psal.anonymouth.engine.DataAnalyzer;
import edu.drexel.psal.anonymouth.engine.DocumentMagician;
import edu.drexel.psal.anonymouth.engine.FeatureList;
import edu.drexel.psal.anonymouth.helpers.ErrorHandler;
import edu.drexel.psal.anonymouth.utils.ConsolidationStation;
import edu.drexel.psal.anonymouth.utils.FunctionWords;
import edu.drexel.psal.anonymouth.utils.TaggedDocument;
import edu.drexel.psal.anonymouth.utils.Tagger;
import edu.drexel.psal.jstylo.generics.Logger;

public class BackendInterface {

	private final String NAME = "( " + this.getClass().getSimpleName() + " ) - ";
	
	private GUIMain main;
	private ProgressWindow pw;
	private FunctionWords functionWords;
	private DataAnalyzer dataAnalyzer;
	private DocumentMagician documentMagician;
	private SwingWorker<Void, Void> processing;

	public BackendInterface(GUIMain main) {
		this.main = main;
		readyProcessingThread();
	}
	
	private void readyProcessingThread() {
		processing = new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				processDocuments();
				return null;
			}	
		};
	}
	
	private void processDocuments() {
		if (!main.processed) {
			prepareForFirstProcess();
		}

		try {
			pw = new ProgressWindow("Processing...", main);
			pw.run();

			DocumentMagician.numProcessRequests++;
			String tempDoc = "";
			functionWords = new FunctionWords();

			if (main.processed != true) {
				functionWords.run();
				tempDoc = main.documentPane.getText();
				Logger.logln(NAME+"Process button pressed for first time (initial run) in editor tab");

				pw.setText("Extracting and Clustering Features...");
				try {
					dataAnalyzer.runInitial(documentMagician, main.ppAdvancedDriver.cfd, main.ppAdvancedWindow.classifiers.get(0));
					pw.setText("Initializing Tagger...");
					Tagger.initTagger();
					pw.setText("Classifying Documents...");
					documentMagician.runWeka();
					dataAnalyzer.runClusterAnalysis(documentMagician);
					ClustersDriver.initializeClusterViewer(main,false);
				} catch(Exception e) {
					pw.stop();
					ErrorHandler.fatalProcessingError(e);
				}
			} else {
				Logger.logln(NAME+"Process button pressed to re-process document to modify.");
				tempDoc = main.documentPane.getText();
				if(tempDoc.equals("") == true) {
					JOptionPane.showMessageDialog(null,
							"It is not possible to process an empty document.",
							"Document processing error",
							JOptionPane.ERROR_MESSAGE,
							null);
				} else {
					documentMagician.setModifiedDocument(tempDoc);

					pw.setText("Extracting and Clustering Features...");
					try {
						dataAnalyzer.reRunModified(documentMagician);
						pw.setText("Initialize Cluster Viewer...");
						ClustersDriver.initializeClusterViewer(main,false);
						pw.setText("Classifying Documents...");
						documentMagician.runWeka();
					} catch (Exception e) {
						pw.stop();
						ErrorHandler.fatalProcessingError(e);
					}
				}
			}

			if (!main.processed) {
				ConsolidationStation.toModifyTaggedDocs.get(0).makeAndTagSentences(main.documentPane.getText(), true);
				
				List<Document> sampleDocs = documentMagician.getDocumentSets().get(0);
				int size = sampleDocs.size();
				ConsolidationStation.otherSampleTaggedDocs = new ArrayList<TaggedDocument>();
				for (int i = 0; i < size; i++) {
					ConsolidationStation.otherSampleTaggedDocs.add(new TaggedDocument(sampleDocs.get(i).stringify()));
				}
			} else
				ConsolidationStation.toModifyTaggedDocs.get(0).makeAndTagSentences(main.documentPane.getText(), false);

			Map<String,Map<String,Double>> wekaResults = documentMagician.getWekaResultList();
			Logger.logln(NAME+" ****** WEKA RESULTS for session '"+ThePresident.sessionName+" process number : "+DocumentMagician.numProcessRequests);
			Logger.logln(NAME+wekaResults.toString());
			sendResultsToResultsChart(wekaResults);
			
			main.anonymityBar.updateBar();
			if (!main.processed)
				main.anonymityBar.setMaxFill(main.editorDriver.taggedDoc.getMaxChangeNeeded());
			
			main.anonymityBar.showFill(true);
			main.editorDriver.updateSuggestionsThread.execute();
			main.editorDriver.updateBarThread.execute();
			

			main.enableEverything(true);	
			
			//needed so if the user has some strange spacing for their first sentence we are placing the caret where the sentence actually begins (and thus highlighting it, otherwise it wouldn't)
			int caret = 0;
			while (Character.isWhitespace(main.documentPane.getText().charAt(caret))) {
				caret++;
			}

			main.editorDriver.newCaretPosition[0] = caret;
			main.editorDriver.newCaretPosition[1]= caret;
			main.editorDriver.refreshEditor();
			main.editorDriver.moveHighlights();
			main.editorDriver.pastTaggedDoc = new TaggedDocument(main.editorDriver.taggedDoc);

			DictionaryBinding.init();//initializes the dictionary for wordNEt

			Logger.logln(NAME+"Finished in BackendInterface - postTargetSelection");

			main.resultsWindow.resultsLabel.setText("Re-Process your document to get updated ownership probability");
			main.documentScrollPane.getViewport().setViewPosition(new java.awt.Point(0, 0));
			main.processed = true;
			pw.stop();
			main.showGUIMain();
		} catch (Exception e) {
			// Get current size of heap in bytes
			long heapSize = Runtime.getRuntime().totalMemory();

			// Get maximum size of heap in bytes. The heap cannot grow beyond this size.
			// Any attempt will result in an OutOfMemoryException.
			long heapMaxSize = Runtime.getRuntime().maxMemory();

			// Get amount of free memory within the heap in bytes. This size will increase
			// after garbage collection and decrease as new objects are created.
			long heapFreeSize = Runtime.getRuntime().freeMemory();
			Logger.logln(NAME+"ERROR WHILE PROCESSING. Here are the total, max, and free heap sizes:");
			Logger.logln(NAME+"Total: "+heapSize+" Max: "+heapMaxSize+" Free: "+heapFreeSize);
			
			ErrorHandler.fatalProcessingError(e);
		}
	}
	
	protected void process() {
		processing.execute();
	}
	
	private void prepareForFirstProcess() {
		// ----- create the main document and add it to the appropriate array list.
		// ----- may not need the arraylist in the future since you only really can have one at a time
		TaggedDocument taggedDocument = new TaggedDocument();
		ConsolidationStation.toModifyTaggedDocs = new ArrayList<TaggedDocument>();
		ConsolidationStation.toModifyTaggedDocs.add(taggedDocument);
		main.editorDriver.taggedDoc = ConsolidationStation.toModifyTaggedDocs.get(0);

		Logger.logln(NAME+"Initial processing starting...");

		// initialize all arraylists needed for feature processing
		int sizeOfCfd = main.ppAdvancedDriver.cfd.numOfFeatureDrivers();
		ArrayList<String> featuresInCfd = new ArrayList<String>(sizeOfCfd);
		ArrayList<FeatureList> yesCalcHistFeatures = new ArrayList<FeatureList>(sizeOfCfd);

		for(int i = 0; i < sizeOfCfd; i++) {
			String theName = main.ppAdvancedDriver.cfd.featureDriverAt(i).getName();

			// capitalize the name and replace all " " and "-" with "_"
			theName = theName.replaceAll("[ -]","_").toUpperCase(); 
			main.ppAdvancedDriver.cfd.featureDriverAt(i).isCalcHist();
			yesCalcHistFeatures.add(FeatureList.valueOf(theName));
			featuresInCfd.add(i,theName);
		}
		dataAnalyzer = new DataAnalyzer(main.preProcessWindow.ps);
		documentMagician = new DocumentMagician(false);
		main.wordSuggestionsDriver.setMagician(documentMagician);
		Logger.logln(NAME+"Beginning main process...");
	}

	public void sendResultsToResultsChart(Map<String,Map<String,Double>> resultMap) {

		Iterator<String> mapKeyIter = resultMap.keySet().iterator();
		Map<String,Double> tempMap = resultMap.get(mapKeyIter.next()); 

		int numAuthors = DocumentMagician.numSampleAuthors+1;

		Object[] authors = (tempMap.keySet()).toArray();

		Object[] keyRing = tempMap.values().toArray();
		Double biggest = .01;
		
		double[][] predictionMapArray = new double[authors.length][2];
		for(int i = 0; i < numAuthors; i++){
			Double tempVal = ((Double)keyRing[i])*100;
			// compare PRIOR to rounding.
			if(biggest < tempVal){
				biggest = tempVal;
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

		main.resultsWindow.makeChart();
	}
}