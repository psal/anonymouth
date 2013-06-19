package edu.drexel.psal.jstylo.generics;

import java.util.Map;

import edu.drexel.psal.jstylo.analyzers.WekaAnalyzer;
import edu.drexel.psal.jstylo.analyzers.WriteprintsAnalyzer;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;

/**
 * 
 * JStylo SimpleAPI Version .3<br>
 * 
 * A simple API for the inner JStylo functionality.<br>
 * Provides four constructors at the moment (eventually more) <br>
 * After the SimpleAPI is constructed, users need only call prepareInstances(),
 * (sometimes, depending on constructor) prepareAnalyzer(), and run().<br>
 * After fetch the relevant information with the correct get method<br>
 * @author Travis Dutko
 */
/*
 * TODO list:
 * 
 * 1) Add the ability to load pre-made objects (problem sets, cfds)
 * 
 */
public class SimpleAPI {

	///////////////////////////////// Data
	
	//which evaluation to perform enumeration
	enum analysisType {CROSS_VALIDATION,TRAIN_TEST,BOTH};
	
	//Persistant/necessary data
	InstancesBuilder ib; //does the feature extraction
	String classifierPath; //creates analyzer
	Analyzer analysisDriver; //does the train/test/crossVal
	analysisType selected; //type of evaluation
	int numFolds; //folds for cross val (defaults to 10)
	
	//Result Data
	double[][] infoGain; //how useful given features were
	Map<String,Map<String, Double>> trainTestResults;
	Evaluation crossValResults;
	
	///////////////////////////////// Constructors
	
	/**
	 * SimpleAPI constructor. Does not support classifier arguments
	 * @param psXML path to the XML containing the problem set
	 * @param cfdXML path to the XML containing the cumulativeFeatureDriver/feature set
	 * @param numThreads number of calculation threads to use for parallelization
	 * @param classPath path to the classifier to use (of format "weka.classifiers.functions.SMO")
	 * @param type type of analysis to perform
	 */
	public SimpleAPI(String psXML, String cfdXML, int numThreads, String classPath, analysisType type){
		
		ib = new InstancesBuilder(psXML,cfdXML,true,false,numThreads);
		classifierPath = classPath;
		selected = type;
		numFolds = 10;
	}
	
	/**
	 * SimpleAPI constructor. Does not support classifier arguments
	 * @param psXML path to the XML containing the problem set
	 * @param cfdXML path to the XML containing the cumulativeFeatureDriver/feature set
	 * @param numThreads number of calculation threads to use for parallelization
	 * @param classPath path to the classifier to use (of format "weka.classifiers.functions.SMO")
	 * @param type type of analysis to perform
	 * @param nf number of folds to use
	 */
	public SimpleAPI(String psXML, String cfdXML, int numThreads, String classPath, analysisType type, int nf){
		
		ib = new InstancesBuilder(psXML,cfdXML,true,false,numThreads);
		classifierPath = classPath;
		selected = type;
		numFolds = nf;
	}
	
	/**
	 * Constructor for use with a weka classifier. Do not call prepare Analyzer if using this constructor
	 * @param psXML
	 * @param cfdXML
	 * @param numThreads
	 * @param classifier
	 * @param type
	 */
	public SimpleAPI(String psXML, String cfdXML, int numThreads, Classifier classifier, analysisType type){
		ib = new InstancesBuilder(psXML,cfdXML,true,false,numThreads);
		analysisDriver = new WekaAnalyzer(classifier);
		selected = type;
		numFolds = 10;
	}
	
	/**
	 * Constructor for use with a weka classifier. Do not call prepare Analyzer if using this constructor
	 * @param psXML
	 * @param cfdXML
	 * @param numThreads
	 * @param classifier
	 * @param type
	 * @param nf number of folds to use
	 */
	public SimpleAPI(String psXML, String cfdXML, int numThreads, Classifier classifier, analysisType type, int nf){
		ib = new InstancesBuilder(psXML,cfdXML,true,false,numThreads);
		analysisDriver = new WekaAnalyzer(classifier);
		selected = type;
		numFolds = nf;
	}
	
	///////////////////////////////// Methods
	
	/**
	 * Prepares the instances objects (stored within the InstancesBuilder)
	 */
	public void prepareInstances() {

		try {
			ib.extractEventsThreaded(); //extracts events from documents
			ib.initializeRelevantEvents(); //creates the List<EventSet> to pay attention to
			ib.initializeAttributes(); //creates the attribute list to base the Instances on
			ib.createTrainingInstancesThreaded(); //creates train Instances
			ib.createTestInstancesThreaded(); //creates test Instances (if present)
			ib.calculateInfoGain(); //calculates infoGain
		} catch (Exception e) {
			System.out.println("Failed to prepare instances");
			e.printStackTrace();
		}

	}

	/**
	 * Prepares the analyzer for classification
	 */
	public void prepareAnalyzer() {
		try {
			Object tmpObject = null;
			tmpObject = Class.forName(classifierPath).newInstance(); //creates the object from the string

			if (tmpObject instanceof Classifier) { //if it's a weka classifier
				analysisDriver = new WekaAnalyzer(Class.forName(classifierPath) //make a wekaAnalyzer
						.newInstance());
			} else if (tmpObject instanceof WriteprintsAnalyzer) { //otherwise it's a writeprints analyzer
				analysisDriver = new WriteprintsAnalyzer(); 
			}
		} catch (Exception e) {
			System.out.println("Failed to prepare Analyzer");
			e.printStackTrace();
		}
	}
	
	/**
	 * Applies infoGain to the training and testing instances
	 * @param n the number of features/attributes to keep
	 */
	public void applyInfoGain(int n){
		try {
			ib.applyInfoGain(n);
		} catch (Exception e) {
			System.out.println("Failed to apply infoGain");
			e.printStackTrace();
		}
	}
	
	/**
	 * Perform the actual analysis
	 */
	public void run(){
		
		//switch based on the enum
		switch (selected) {
	
		//do a cross val
		case CROSS_VALIDATION:
			crossValResults = analysisDriver.runCrossValidation(ib.getTrainingInstances(), numFolds, 0);
			break;

		// do a train/test
		case TRAIN_TEST:
			trainTestResults = analysisDriver.classify(ib.getTrainingInstances(), ib.getTestInstances(), ib.getProblemSet().getTestDocs());
			break;

		//do both
		case BOTH:
			crossValResults = analysisDriver.runCrossValidation(ib.getTrainingInstances(), numFolds, 0);
			trainTestResults = analysisDriver.classify(ib.getTrainingInstances(), ib.getTestInstances(), ib.getProblemSet().getTestDocs());
			break;
		
		//should not occur
		default:
			System.out.println("Unreachable. Something went wrong somewhere.");
			break;
		}
		
	}
	
	///////////////////////////////// Setters/Getters
	
	/**
	 * Change the number of folds to use in cross validation
	 * @param n number of folds to use from now on
	 */
	public void setNumFolds(int n){
		numFolds = n;
	}
	
	/**
	 * @return the Instances object describing the training documents
	 */
	public Instances getTrainingInstances(){
		return ib.getTrainingInstances();
	}
	
	/**
	 * @return the Instances object describing the test documents
	 */
	public Instances getTestInstances(){
		return ib.getTestInstances();
	}
	
	/**
	 * @return the infoGain data (not in human readable form lists indices and usefulness)
	 */
	public double[][] getInfoGain(){
		return ib.getInfoGain();
	}
	
	/**
	 * Returns a string of features, in order of most to least useful, with their infogain values<br>
	 * @param showZeroes whether or not to show features that have a 0 as their infoGain value
	 * @return the string representing the infoGain
	 */
	public String getReadableInfoGain(boolean showZeroes){
		String infoString = ">-----InfoGain information: ";
		Instances trainingInstances = ib.getTrainingInstances();
		for (int i = 0; i<infoGain.length; i++){
			if (!showZeroes && (infoGain[i][0]==0))
				break;
			
			infoString+=String.format("> %-50s   %f\n",
					trainingInstances.attribute((int)infoGain[i][1]).name(),
					infoGain[i][0]);
		}
		
		return infoString;
	}
	
	/**
	 * @return Map containing train/test results
	 */
	public Map<String,Map<String, Double>> getTrainTestResults(){
		return trainTestResults;
	}
	
	/**
	 * @return Evaluation containing train/test statistics
	 */
	public Evaluation getTrainTestStatistics(){
		return analysisDriver.getClassificationStatistics();
	}
	
	/**
	 * @return Evaluation containing cross validation results
	 */
	public Evaluation getCrossValResults(){
		return crossValResults;
	}
	
	/**
	 * @return String containing accuracy, metrics, and confusion matrix from cross validation
	 */
	public String getCrossValStatString() {
		
		try {
			Evaluation eval = getCrossValResults();
			String resultsString = "";
			resultsString += eval.toSummaryString(false) + "\n";
			resultsString += eval.toClassDetailsString() + "\n";
			resultsString += eval.toMatrixString() + "\n";
			return resultsString;
		
		} catch (Exception e) {
			System.out
					.println("Failed to get cross validation statistics string");
			e.printStackTrace();
			return "";
		}
	}
	
	/**
	 * @return String containing accuracy and confusion matrix from train/test. other metrics may not be accurate.
	 */
	public String getTrainTestStatString() {
		
		try {
			Evaluation eval = getTrainTestStatistics();
			String resultsString = "";
			resultsString += eval.toSummaryString(false) + "\n";
			resultsString += eval.toClassDetailsString() + "\n";
			resultsString += eval.toMatrixString() + "\n";
			return resultsString;
		
		} catch (Exception e) {
			System.out.println("Failed to get train/test statistics string");
			e.printStackTrace();
			return "";
		}
	}
	
	///////////////////////////////// Main method for testing purposes
	/*
	public static void main(String[] args){
		SimpleAPI test = new SimpleAPI(
				"./jsan_resources/problem_sets/enron_demo.xml",
				"./jsan_resources/feature_sets/writeprints_feature_set_limited.xml",
				8, "weka.classifiers.functions.SMO",
				analysisType.CROSS_VALIDATION);

		test.prepareInstances();
		test.prepareAnalyzer();
		test.run();
		
		System.out.println("Results: " + "\n" + test.getCrossValStatString());

	}*/
}
