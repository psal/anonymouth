package edu.drexel.psal.jstylo.generics;

import java.util.Map;

import edu.drexel.psal.jstylo.analyzers.WekaAnalyzer;
import edu.drexel.psal.jstylo.analyzers.WriteprintsAnalyzer;
import edu.drexel.psal.jstylo.generics.Logger.LogOut;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;

/**
 * 
 * JStylo SimpleAPI Version .5<br>
 * 
 * A simple API for the inner JStylo functionality.<br>
 * Provides four constructors at the moment (eventually more) <br>
 * After the SimpleAPI is constructed, users need only call prepareInstances(),
 * (sometimes, depending on constructor) prepareAnalyzer(), and run().<br>
 * After fetch the relevant information with the correct get method<br>
 * @author Travis Dutko
 */

public class SimpleAPI {

	///////////////////////////////// Data
	
	//which evaluation to perform enumeration
	public static enum analysisType {CROSS_VALIDATION,TRAIN_TEST_UNKNOWN,TRAIN_TEST_KNOWN};
	
	//Persistant/necessary data
	InstancesBuilder ib; //does the feature extraction
	String classifierPath; //creates analyzer
	Analyzer analysisDriver; //does the train/test/crossVal
	analysisType selected; //type of evaluation
	int numFolds; //folds for cross val (defaults to 10)
	
	//Result Data
	Map<String,Map<String, Double>> trainTestResults;
	Evaluation trainTestEval;
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
		selected = type;
		numFolds = nf;
	}
	
	/**
	 * Constructor for use with a weka classifier that uses default params
	 * Do not call prepareAnalyzer if using this constructor
	 * @param ps 
	 * @param cfd
	 * @param classifier
	 */
	public SimpleAPI(ProblemSet ps, CumulativeFeatureDriver cfd, String classifier) {
		ib = new InstancesBuilder(ps,cfd);
		selected = analysisType.TRAIN_TEST_UNKNOWN;
		numFolds = 10;
		try {
			Object tmpObject = null;
			tmpObject = Class.forName(classifier).newInstance(); //creates the object from the string

			if (tmpObject instanceof Classifier) { //if it's a weka classifier
				analysisDriver = new WekaAnalyzer(Class.forName(classifier) //make a wekaAnalyzer
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
	 * Minimalistic constructor that takes only a problemset object and a CumulativeFeatureDriver.<br>
	 * Defaults to an SMO classifier performing cross validation with ten folds
	 * @param ps
	 * @param cfd
	 */
	public SimpleAPI(ProblemSet ps, CumulativeFeatureDriver cfd){
		ib = new InstancesBuilder(ps,cfd);
		selected = analysisType.CROSS_VALIDATION;
		numFolds = 10;
		analysisDriver = new WekaAnalyzer();
	}
	
	/**
	 *
	 * @param ps
	 * @param cfd
	 */
	public SimpleAPI(ProblemSet ps, CumulativeFeatureDriver cfd, int nf){
		ib = new InstancesBuilder(ps,cfd);
		selected = analysisType.CROSS_VALIDATION;
		numFolds = nf;
		analysisDriver = new WekaAnalyzer();
	}
	
	/**
	 * 
	 * @param ps
	 * @param cfd
	 * @param nf
	 * @param numThreads
	 */
	public SimpleAPI(ProblemSet ps, CumulativeFeatureDriver cfd, int nf, int numThreads){
		ib = new InstancesBuilder(ps,cfd);
		ib.setNumThreads(numThreads);
		selected = analysisType.CROSS_VALIDATION;
		numFolds = nf;
		analysisDriver = new WekaAnalyzer();
	}
	
	/**
	 * 
	 * @param ps
	 * @param cfd
	 * @param nf
	 * @param numThreads
	 * @param isSparse
	 */
	public SimpleAPI(ProblemSet ps, CumulativeFeatureDriver cfd, int nf, int numThreads, boolean isSparse){
		ib = new InstancesBuilder(ps,cfd);
		ib.setNumThreads(numThreads);
		ib.setUseSparse(isSparse);
		selected = analysisType.CROSS_VALIDATION;
		numFolds = nf;
		analysisDriver = new WekaAnalyzer();
	}
	
	/**
	 * 
	 * @param ps
	 * @param cfd
	 * @param nf
	 * @param numThreads
	 * @param isSparse
	 * @param useDocTitles
	 */
	public SimpleAPI(ProblemSet ps, CumulativeFeatureDriver cfd, int nf, int numThreads, boolean isSparse, boolean useDocTitles){
		ib = new InstancesBuilder(ps,cfd);
		ib.setNumThreads(numThreads);
		ib.setUseSparse(isSparse);
		selected = analysisType.CROSS_VALIDATION;
		numFolds = nf;
		analysisDriver = new WekaAnalyzer();
	}
	
	/**
	 * Basic constructor that takes a problemSet object, CFD, classifier, and analysis type<br>
	 * @param ps
	 * @param cfd
	 * @param classifier
	 * @param type
	 */
	public SimpleAPI(ProblemSet ps, CumulativeFeatureDriver cfd, Classifier classifier, analysisType type){
		ib = new InstancesBuilder(ps,cfd);
		selected = type;
		numFolds = 10;
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
	 * @param ps
	 * @param cfd
	 * @param classifier
	 * @param type
	 * @param isSparse
	 * @param useDocTitles
	 */
	public SimpleAPI(ProblemSet ps, CumulativeFeatureDriver cfd, Classifier classifier, analysisType type, boolean isSparse, boolean useDocTitles, int numThreads){
		ib = new InstancesBuilder(ps,cfd);
		selected = type;
		ib.setNumThreads(numThreads);
		ib.setUseSparse(isSparse);
		ib.setUseDocTitles(useDocTitles);
		numFolds = 10;
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
	 * 
	 * @param ps
	 * @param cfd
	 * @param classifier
	 * @param type
	 * @param isSparse
	 * @param useDocTitles
	 */
	public SimpleAPI(ProblemSet ps, CumulativeFeatureDriver cfd, Classifier classifier, analysisType type, boolean isSparse, boolean useDocTitles){
		ib = new InstancesBuilder(ps,cfd);
		selected = type;
		ib.setUseSparse(isSparse);
		ib.setUseDocTitles(useDocTitles);
		numFolds = 10;
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
	 * 
	 * @param ps
	 * @param cfd
	 * @param classifier
	 * @param type
	 * @param numThreads 
	 */
	public SimpleAPI(ProblemSet ps, CumulativeFeatureDriver cfd, Classifier classifier, analysisType type, int numThreads){
		ib = new InstancesBuilder(ps,cfd);
		selected = type;
		ib.setNumThreads(numThreads);
		numFolds = 10;
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
	 * Basic Constructor which takes a ProblemSet object, CFD, and analysis type.<br>
	 * Defaults to the basic SMO classifier.
	 * @param ps
	 * @param cfd
	 * @param type
	 */
	public SimpleAPI(ProblemSet ps, CumulativeFeatureDriver cfd, analysisType type){
		ib = new InstancesBuilder(ps,cfd);
		selected = type;
		numFolds = 10;
		analysisDriver = new WekaAnalyzer();
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
	 * Calculates and stores the infoGain for future use
	 */
	public void calcInfoGain(){
		try {
			ib.calculateInfoGain();
		} catch (Exception e) {
			Logger.logln("Failed to calculate infoGain",LogOut.STDERR);
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
		case TRAIN_TEST_UNKNOWN:
			trainTestResults = analysisDriver.classify(ib.getTrainingInstances(), ib.getTestInstances(), ib.getProblemSet().getAllTestDocs());
			break;

		//do a train/test where we know the answer and just want statistics
		case TRAIN_TEST_KNOWN:
			ib.getProblemSet().removeAuthor("_Unknown_");
			try {
				Instances train = ib.getTrainingInstances();
				Instances test = ib.getTestInstances();
				test.setClassIndex(test.numAttributes()-1);
				train.setClassIndex(train.numAttributes()-1);
				trainTestEval = analysisDriver.getTrainTestEval(train,test);
			} catch (Exception e) {
				Logger.logln("Failed to build trainTest Evaluation");
				e.printStackTrace();
			}
			break;
		
		//should not occur
		default:
			System.out.println("Unreachable. Something went wrong somewhere.");
			break;
		}
	}
	
	///////////////////////////////// Setters/Getters
	
	/**
	 * 
	 * @param useDocTitles
	 */
	public void setUseDocTitles(boolean useDocTitles){
		ib.setUseDocTitles(useDocTitles);
	}
	
	public void setUseSparse(boolean sparse){
		ib.setUseSparse(sparse);
	}
	
	/**
	 * Sets the training Instances object
	 * @param insts the Instances object to use as training data
	 */
	public void setTrainingInstances(Instances insts){
		ib.setTrainingInstances(insts);
	}
	
	/**
	 * Sets the testing Instances object
	 * @param insts the Instances object to use as testing data
	 */
	public void setTestingInstances(Instances insts){
		ib.setTestingInstances(insts);
	}
	
	/**
	 * Sets the type of experiment to run
	 * @param type enum value of either CROSS_VALIDATION, TRAIN_TEST_UNKNOWN, or TRAIN_TEST_KNOWN
	 */
	public void setExperimentType(analysisType type){
		selected = type;
	}
	
	/**
	 * Change the number of folds to use in cross validation
	 * @param n number of folds to use from now on
	 */
	public void setNumFolds(int n){
		numFolds = n;
	}
	
	/**
	 * Sets the number of calculation threads to use
	 * @param nt the number of calculation threads to use
	 */
	public void setNumThreads(int nt){
		ib.setNumThreads(nt);
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
		String infoString = ">-----InfoGain information: \n\n";
		Instances trainingInstances = ib.getTrainingInstances();
		double[][] infoGain = ib.getInfoGain();
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
	public Evaluation getTrainTestEval(){
		return trainTestEval;
	}
	
	/**
	 * @return Evaluation containing cross validation results
	 */
	public Evaluation getCrossValEval(){
		return crossValResults;
	}
	
	/**
	 * @return String containing accuracy, metrics, and confusion matrix from cross validation
	 */
	public String getCrossValStatString() {
		
		try {
			Evaluation eval = getCrossValEval();
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
	 * @return String containing accuracy and confusion matrix from train/test.
	 * @throws Exception 
	 */
	public String getTrainTestStatString() {
		
		Evaluation eval = getTrainTestEval();
		try {
			return eval.toSummaryString() + "\n" + eval.toClassDetailsString() + "\n" + eval.toMatrixString();
		} catch (Exception e) {
			Logger.logln("Failed to generate stat string!", LogOut.STDERR);
			return null;
		}
	}
	
	/**
	 * @return The accuracy of the given test in percentage format
	 */
	public String getClassificationAccuracy(){
		String results = "";
		
		if (selected == analysisType.CROSS_VALIDATION){
			
			Evaluation crossEval = getCrossValEval();
			String summary = crossEval.toSummaryString();
			int start = summary.indexOf("Correctly Classified Instances");
			int end = summary.indexOf("%");
			results+=summary.substring(start,end+1)+"\n";
			
		} else if (selected == analysisType.TRAIN_TEST_KNOWN ){
			String source = getTrainTestStatString();
					
			int start = source.indexOf("Correctly Classified");
			int end = source.indexOf("%");

			results += source.substring(start,end+1);
			
		}
		
		return results;
	}
	
	/**
	 * @return the weka clsasifier being used by the analyzer. Will break something if you try to call it on a non-weka analyzer
	 */
	public Classifier getUnderlyingClassifier(){
		return analysisDriver.getClassifier();
	}
	
	public void writeArff(String path, Instances insts){
		InstancesBuilder.writeToARFF(path,insts);
	}
	
	
	///////////////////////////////// Main method for testing purposes
	
	public static void main(String[] args){
		
		SimpleAPI test = new SimpleAPI(
				"./jsan_resources/problem_sets/enron_demo.xml",
				"./jsan_resources/feature_sets/writeprints_feature_set_limited.xml",
				8, "weka.classifiers.functions.SMO",
				analysisType.CROSS_VALIDATION);

		test.prepareInstances();
		test.prepareAnalyzer();
		test.run();
		
		//test.writeArff("./testing.arff",test.getTestInstances());
		
	}
}