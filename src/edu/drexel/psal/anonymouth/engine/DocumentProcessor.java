package edu.drexel.psal.anonymouth.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import com.jgaap.generics.Document;

import edu.drexel.psal.ANONConstants;
import edu.drexel.psal.anonymouth.gooie.ClustersDriver;
import edu.drexel.psal.anonymouth.gooie.DictionaryBinding;
import edu.drexel.psal.anonymouth.gooie.EditorDriver;
import edu.drexel.psal.anonymouth.gooie.GUIMain;
import edu.drexel.psal.anonymouth.gooie.ProgressWindow;
import edu.drexel.psal.anonymouth.gooie.ThePresident;
import edu.drexel.psal.anonymouth.helpers.ErrorHandler;
import edu.drexel.psal.anonymouth.utils.ConsolidationStation;
import edu.drexel.psal.anonymouth.utils.TaggedDocument;
import edu.drexel.psal.anonymouth.utils.Tagger;
import edu.drexel.psal.jstylo.generics.Logger;
import edu.drexel.psal.jstylo.generics.Logger.LogOut;
import edu.drexel.psal.jstylo.generics.ProblemSet;

/**
 * The class that manages all document processing and all relating classes.
 * Whenever you want to process you should make a call to this class and let
 * it handle the rest.
 *
 * @author Andrew W.E. McDonald
 * @author Marc Barrowclift
 */

public class DocumentProcessor {

	private final String NAME = "( " + this.getClass().getSimpleName() + " ) - ";
	
	private GUIMain main;
	private EditorDriver editorDriver;
	public ProgressWindow pw;
	private DataAnalyzer dataAnalyzer;
	public DocumentMagician documentMagician;
	private SwingWorker<Void, Void> processing;

	/**
	 * Constructor
	 * 
	 * @param  main
	 *         GUIMain instance
	 */
	public DocumentProcessor(GUIMain main) {
		this.main = main;
	}
	
	/**
	 * To be called before whenever you want to process or reprocess the
	 * document since by design SwingWorker instances may only be executed
	 * once (meaning we need to initiate a new instance every time)
	 */
	private void readyProcessingThread() {
		processing = new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				processDocuments();
				return null;
			}	
		};
	}

	/**
	 * The process call that any outside class should call when we want to
	 * process.
	 */
	public void process() {
		this.editorDriver = main.editorDriver;
		readyProcessingThread();
		processing.execute();
	}

	/**
	 * If it's the first time the documents are being processed we
	 * have a few additional steps that need only be executed once.
	 */
	private void prepareForFirstProcess() {
		/*
		 * Create the main document and add it to the appropriate array list.
		 * May not need the ArrayList in the future since you only really can
		 * have one at a time.
		 */
		TaggedDocument taggedDocument = new TaggedDocument(main);
		ConsolidationStation.toModifyTaggedDocs = new ArrayList<TaggedDocument>();
		ConsolidationStation.toModifyTaggedDocs.add(taggedDocument);
		editorDriver.taggedDoc = ConsolidationStation.toModifyTaggedDocs.get(0);
		Logger.logln(NAME+"Initial processing starting...");

		//Initialize all arraylists needed for feature processing
		int sizeOfCfd = main.ppAdvancedDriver.cfd.numOfFeatureDrivers();
		ArrayList<String> featuresInCfd = new ArrayList<String>(sizeOfCfd);
		ArrayList<FeatureList> yesCalcHistFeatures = new ArrayList<FeatureList>(sizeOfCfd);

		for (int i = 0; i < sizeOfCfd; i++) {
			String theName = main.ppAdvancedDriver.cfd.featureDriverAt(i).getName();

			//Capitalize the name and replace all " " and "-" with "_"
			theName = theName.replaceAll("[ -]","_").toUpperCase(); 
			main.ppAdvancedDriver.cfd.featureDriverAt(i).isCalcHist();
			yesCalcHistFeatures.add(FeatureList.valueOf(theName));
			featuresInCfd.add(i,theName);
		}

		dataAnalyzer = new DataAnalyzer(main.preProcessWindow.ps);
		//FIXME
		documentMagician = new DocumentMagician(false);
		//need to find and fill in the Classifier
		try {
			dataAnalyzer.runInitial(documentMagician, main.ppAdvancedDriver.cfd, main.ppAdvancedWindow.classifiers.get(0));
		} catch (Exception e) {
			e.printStackTrace();
		}
		Logger.logln(NAME+"Beginning main process...");
	}
	
	/**
	 * Main process method
	 */
	private void processDocuments() {
		pw = new ProgressWindow("Processing...", main);
		pw.run();
		
		if (!main.processed) {
			Logger.logln(NAME+"Process button pressed for first time (initial run) in editor tab");
			pw.setText("Extracting and Clustering Features...");
			prepareForFirstProcess();
		}
		
		try {
			DocumentMagician.numProcessRequests++;
			String tempDoc = "";
			if (!main.processed) {
				tempDoc = main.documentPane.getText();
				try {
					//TODO figure out why this isn't run earlier.
					//dataAnalyzer.runInitial(documentMagician, main.ppAdvancedDriver.cfd, main.ppAdvancedWindow.classifiers.get(0));
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
			} 
			else 
			{
				main.reprocessing = true;
				Logger.logln(NAME+"Process button pressed to re-process document to modify.");
				tempDoc = main.documentPane.getText();
				if(tempDoc.equals("") == true) 
				{
					JOptionPane.showMessageDialog(null,
							"It is not possible to process an empty document.",
							"Document processing error",
							JOptionPane.ERROR_MESSAGE,
							null);
				} 
				else 
				{
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
				List<Document> sampleDocs = null;

				sampleDocs = documentMagician.getDocumentSets().get(0);
				int size = sampleDocs.size();
				ConsolidationStation.otherSampleTaggedDocs = new ArrayList<TaggedDocument>();
				for (int i = 0; i < size; i++) {
					ConsolidationStation.otherSampleTaggedDocs.add(new TaggedDocument(main, sampleDocs.get(i).stringify(), false));
				}
			} else {
				ConsolidationStation.toModifyTaggedDocs.get(0).makeAndTagSentences(main.documentPane.getText(), false);
				// prepare to update Word Suggestion's lists and Anonymity Bar
				main.editorDriver.initThreads();
			}
			
			// prepare data for Result Window
			Map<String,Map<String,Double>> wekaResults = documentMagician.getWekaResultList();
			Logger.logln(NAME+" ****** WEKA RESULTS for session '"+ThePresident.sessionName+" process number : "+DocumentMagician.numProcessRequests);
			Logger.logln(NAME+wekaResults.toString());
			sendResultsToResultsChart(wekaResults);
			
			// update Anonymity Bar
			editorDriver.updateBarThread.execute();
			
			//Wait for Anonymity Bar to finish updating
			editorDriver.updateBarThread.get();
			
			main.anonymityBar.showFill(true);
			
			// get data for Word Suggestions lists
			pw.setText("Loading Word Suggestions");
			editorDriver.updateSuggestionsThread.execute();
					
			//Wait for the Word Suggestions thread to finish
			editorDriver.updateSuggestionsThread.get();
			
			main.enableEverything(true);	
			
			int caret = editorDriver.getWhiteSpaceBuffer(0);
			editorDriver.newCaretPosition[0] = caret;
			editorDriver.newCaretPosition[1]= caret;
			editorDriver.syncTextPaneWithTaggedDoc();
			main.versionControl.init();

			//initializes the dictionary for wordNEt
			DictionaryBinding.init();
			
			Logger.logln(NAME+"Finished in DocumentProcessor - postTargetSelection");
			main.resultsWindow.resultsLabel.setText("Re-Process your document to get updated ownership probability");
			main.documentScrollPane.getViewport().setViewPosition(new java.awt.Point(0, 0));
			main.processed = true;
			main.reprocessing = false;

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
			Logger.logln(NAME+"ERROR WHILE PROCESSING. Here are the total, max, and free heap sizes:", LogOut.STDERR);
			Logger.logln(NAME+"Total: "+heapSize+" Max: "+heapMaxSize+" Free: "+heapFreeSize, LogOut.STDERR);
			e.printStackTrace();
			ErrorHandler.fatalProcessingError(e);
		}
	}

	/**
	 * Parses through the passed WEKA results map and passes relevant
	 * information on to the results window for displaying.
	 * 
	 * @param Map<String,Map<String,resultMap
	 *        WEKA results map from processing
	 */
	public void sendResultsToResultsChart(Map<String,Map<String,Double>> resultMap) {

		Iterator<String> mapKeyIter = resultMap.keySet().iterator();
		Map<String,Double> tempMap = resultMap.get(mapKeyIter.next()); 

		int numAuthors = DocumentMagician.numSampleAuthors+2; //Used to be +1, workaround for JStylo adding an "_Unknown_" author to the Weka instances

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
				main.startingPoint = tempVal;
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