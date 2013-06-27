package edu.drexel.psal.jstylo.generics;

import java.io.StringReader;
import java.util.*;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.core.*;
import weka.core.converters.ArffLoader.ArffReader;

import com.jgaap.generics.*;

/**
 * Abstract class for analyzers - classification routines to be applied on test sets, given a training set.
 * The data representation is based on Weka's Instances object.
 * 
 * @author Ariel Stolerman
 */
public abstract class Analyzer{
	
	/* ======
	 * fields
	 * ======
	 */
	
	/**
	 * The Weka Instances dataset to hold the extracted training data.
	 */
	protected Instances trainingSet;
	
	/**
	 * The Weka Instances dataset to hold the extracted test data.
	 */
	protected Instances testSet;
	
	/**
	 * Mapping of test documents to distribution classification results for each unknown document.
	 */
	protected Map<String,Map<String, Double>> results;
	
	/**
	 * List of authors.
	 */
	protected List<String> authors;
	
	/**
	 * Array of options.
	 */
	protected String[] options;
	
	/* ============
	 * constructors
	 * ============
	 */
	
	// -- none --
	
	/* ==========
	 * operations
	 * ==========
	 */
	
	
	/**
	 * Classifies the given test set based on the given training set. Should update the following fields along the classification:
	 * trainingSet, testSet, results and authors.
	 * Returns list of distributions of classification probabilities per instance.
	 * @param trainingSet
	 * 		The Weka Instances dataset of the training instances.
	 * @param testSet
	 * 		The Weka Instances dataset of the test instances.
	 * @param unknownDocs
	 * 		The list of test documents to deanonymize.
	 * @return
	 * 		The mapping from test documents to distributions of classification probabilities per instance, or
	 * 		null if prepare was not previously called.
	 * 		Each result in the list is a mapping from the author to its corresponding
	 * 		classification probability.
	 */
	public abstract Map<String,Map<String, Double>> classify(
			Instances trainingSet, Instances testSet, List<Document> unknownDocs);
	
	/**
	 * Runs cross validation with given number of folds on the given Instances object.
	 * @param data
	 * 		The data to run CV over.
	 * @param folds
	 * 		The number of folds to use.
	 * @param randSeed
	 * 		Random seed to be used for fold generation.
	 *  @return
	 * 		Some object containing the cross validation results (e.g. Evaluation for Weka
	 * 		classifiers CV results), or null if failed running.		
	 */
	public abstract Evaluation runCrossValidation(Instances data, int folds, long randSeed);
	
	
	/**
	 * Runs a relaxed cross validation with given number of folds on the given Instances object.
	 * A classification result will be considered correct if the true class is
	 * one of the top <code>relaxFactor</code> results (where classes are ranked
	 * by the classifier probability they are the class).
	 * @param data
	 * 		The data to run CV over.
	 * @param folds
	 * 		The number of folds to use.
	 * @param randSeed
	 * 		Random seed to be used for fold generation.
	 * @param relaxFactor
	 * 		The relax factor for the classification.
	 * @return
	 * 		Some object containing the cross validation results (e.g. Evaluation for Weka
	 * 		classifiers CV results), or null if failed running.		
	 */
	public abstract Evaluation runCrossValidation(Instances data, int folds, long randSeed, int relaxFactor);
	
	/* =======
	 * getters
	 * =======
	 */
	
	/**
	 * Returns the string representation of the last classification results.
	 * @return
	 * 		The string representation of the classification results.
	 */
	public String getLastStringResults() {
		// if there are no results yet
		if (results == null)
			return "No results!";		
		
		String res = "";
		Formatter f = new Formatter();
		f.format("%-14s |", "doc \\ author");
		
		List<String> actualAuthors = new ArrayList<String>(authors);
		
		for (String author: actualAuthors)
			f.format(" %-14s |",author);

		res += f.toString()+"\n";
		for (int i=0; i<actualAuthors.size(); i++)
			res += "-----------------";
		res += "----------------\n";
		
		for (String testDocTitle: results.keySet()) {
			f = new Formatter();
			f.format("%-14s |", testDocTitle);

			Map<String,Double> currRes = results.get(testDocTitle);	
			
			String resAuthor = "";
			double maxProb = 0, oldMaxProb;

			for (String author: currRes.keySet()) { 
				oldMaxProb = maxProb;
				maxProb = Math.max(maxProb, currRes.get(author).doubleValue());
				if (maxProb > oldMaxProb)
					resAuthor = author;
			}
			
			for (String author: actualAuthors) {
				
				char c;
				if (author.equals(resAuthor))
					c = '+';
				else c = ' ';
				try{
					f.format(" %2.6f %c     |",currRes.get(author).doubleValue(),c);
				} catch (NullPointerException e){
					
				}
			}
			res += f.toString()+"\n";
		}
		
		res += "\n";
		return res;
	}
	
	/**
	 * A method intended for research purposes when testing and classifying large groups of documents.<br>
	 * Built with the intent on training on one set of documents (with given authors) and testing on another distinct
	 * set of documents (with the same author pool).<br>
	 * <br>
	 * At the moment it is limited in its functionality. It does need /some/ way to determine who the true author is. <br>
	 * For now, this is being checked via the document name, which must at least contain the author's name as it appears in the list of training document authors.<br>
	 * NOTE: this carries with it the problem of authors whose names are subsets of another author ie Robert and Rob--> Rob could mistakenly considered Robert.<br>
	 * &nbsp&nbsp&nbsp&nbsp&nbsp&nbsp To avoid this, make sure each author name is unique and not a subset of another, adding symbols or numbers if necessary.<br>
	 * @return an evaluation object representing the classification results
	 */
	public Evaluation getTrainTestEval(){
		
		Evaluation eval = null; //return object
		SMO smo = new SMO(); //dummy classifier to hold data
		Instances allInstances = null; //all of the instances
		Instances goodInstances = null; //just the good ones
		
		ArrayList<String> extractedAuthors = new ArrayList<String>();
		
		//use the results map to find all of the potential authors and add them to extractedAuthors
		for (String temp: results.keySet()){
			for (String s: results.get(temp).keySet()){
				extractedAuthors.add(s);
			}
			break;
		}
		
		//start the ARFF string
		String stub = "@RELATION <stub>\n";
		stub+="@ATTRIBUTE value {";
		for (int i=0; i<extractedAuthors.size();i++){
			stub+=i+",";
		}
		stub=stub.substring(0,stub.length()-1); //removes the extra comma
		stub+="}\n";
		stub+=  "@ATTRIBUTE authors {";
		//Add all authors
		for (int i=0; i<extractedAuthors.size();i++){
			stub+=extractedAuthors.get(i)+",";
		}
		stub=stub.substring(0,stub.length()-1); //removes the extra comma
		stub+="}\n";
		stub+="@DATA\n";
		//Add the correct author/data pair
		for (int i=0; i<extractedAuthors.size();i++){
			stub+=i+","+extractedAuthors.get(i)+"\n";	
		}	
		//add the incorrect Author/data pairs
		for (int i=0; i<extractedAuthors.size();i++){
			for (int j=0; j<extractedAuthors.size();j++){
				if (i!=j){ 
					stub+=j+","+extractedAuthors.get(i)+"\n";
				}
			}
		}
		
		//initialize the eval and classifier
		try{
			StringReader sReader = new StringReader(stub);
			ArffReader aReader = new ArffReader(sReader);
			allInstances = aReader.getData();
			allInstances.setClassIndex(allInstances.numAttributes()-1);
			goodInstances = new Instances(allInstances,0,extractedAuthors.size());
			smo.buildClassifier(goodInstances);
			eval = new Evaluation(allInstances);		
		} catch (Exception e){
			e.printStackTrace();
		}
		
		//we need SOME way to tell who the real author is. Right now I'm just going to arbitrarily decide that this is via the document title, as I can't really think of an
		//easy way to do it otherwise; no matter what we decide to use, the test set will need to be prepped before hand regardless.
		for (String testDoc: results.keySet()){
			
			String selectedAuthor = "";
			Double max =0.0;
			
			//find the most likely author
			for (String potentialAuthor:results.get(testDoc).keySet()){
				if (results.get(testDoc).get(potentialAuthor).doubleValue()>max){ //find which document has the highest probability of being selected
					max = results.get(testDoc).get(potentialAuthor).doubleValue();
					selectedAuthor=potentialAuthor;
				}
			}
			
			//check to see whether or not that author was correct, and evaluate the model accordingly.
			if (testDoc.contains(selectedAuthor)){ //classify with a good instance
				
				//find where the correct index is
				int correctIndex=-1;
				int i=0;
				for (String s: extractedAuthors){
					if (testDoc.contains(s)){
						correctIndex=i;
						break;
					}
					i++;
				}
				
				try {
					//Logger.logln("Attempting to add correct instance at: "+correctIndex);
					//Logger.logln("\t "+"correctAuthor: "+testInstAuthor+" guess: "+selected);
					eval.evaluateModelOnce(smo,goodInstances.instance(correctIndex));
					//Logger.logln(eval.toMatrixString());
				} catch (Exception e) {
					e.printStackTrace();
				}		
			} else { //classify with a bad instance
				
				//find where the correct index is
				int correctIndex=-1;
				int i=0;
				for (String s: extractedAuthors){
					if (testDoc.contains(s)){
						correctIndex=i;
						break;
					}
					i++;
				}
				int incorrectIndex = extractedAuthors.indexOf(selectedAuthor);
				
				if (!(correctIndex==-1)){ //if the author is listed
					try {

						//Logger.logln("Attempting to add incorrect instance at: "+(correctIndex*extractedAuthors.numValues()+incorrectIndex));
						//Logger.logln("\t "+"correctAuthor: "+testInstAuthor+" guess: "+selected);
					
						int index = extractedAuthors.size()-1; //moves the index past the good instances
						index+=extractedAuthors.size()*correctIndex; //moves to the correct "row"
						index+=incorrectIndex; //moves to correct "column"
						index-=correctIndex; //adjusts for the fact that there are numAuthors-1 cells per row in the bad instances part of the instance list					
						if (incorrectIndex<correctIndex)
							index+=1;
					
						eval.evaluateModelOnce(smo,allInstances.instance(index));
						//Logger.logln(eval.toMatrixString());
					
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					Logger.logln("author to be removed: "+testDoc);
				}
			}
		}
		return eval;
	}
	
	public List<Author> getAuthorStatistics(){
		
		ArrayList<String> extractedAuthors = new ArrayList<String>();
		
		//use the results map to find all of the potential authors and add them to extractedAuthors
		for (String temp: results.keySet()){
			for (String s: results.get(temp).keySet()){
				extractedAuthors.add(s);
			}
			break;
		}
		List<Author> authorStats = new ArrayList<Author>(extractedAuthors.size());
		for (String s: extractedAuthors){
			Author temp = new Author(s);
			authorStats.add(temp);
		}
		
		//we need SOME way to tell who the real author is. Right now I'm just going to arbitrarily decide that this is via the document title, as I can't really think of an
		//easy way to do it otherwise; no matter what we decide to use, the test set will need to be prepped before hand regardless.
		for (String testDoc: results.keySet()){
			
			String selectedAuthor = "";
			Double max =0.0;
			
			//find the most likely author
			for (String potentialAuthor:results.get(testDoc).keySet()){
				if (results.get(testDoc).get(potentialAuthor).doubleValue()>max){ //find which document has the highest probability of being selected
					max = results.get(testDoc).get(potentialAuthor).doubleValue();
					selectedAuthor=potentialAuthor;
				}
			}
			
			//check to see whether or not that author was correct, and evaluate the model accordingly.
			if (testDoc.contains(selectedAuthor)){ //correctly identified
				
				boolean added = false;
				for (Author a: authorStats){
					if (testDoc.contains(a.getName()) && !added){ //increase the real author's TP and numDocs
						a.incrementTruePositiveCount();
						a.incrementNumberOfDocuments();
						added=true;
						
					} else { //Increase everyone else's TN
						a.incrementTrueNegativeCount();
					}
				}
				
			} else { //incorrectly identified
				
				for (Author a: authorStats){
					if (testDoc.contains(a.getName())){ //increase the real author's FN and numDocs
						a.incrementFalseNegativeCount();
						a.incrementNumberOfDocuments();
					} else if (selectedAuthor.equals(a.getName())){ //increase the guess's FP
						a.incrementFalsePositiveCount();
					} else { //increase everyone else's TN
						a.incrementTrueNegativeCount();
					}
				}
			}
		}
		
		return authorStats;
	}
		
	/**
	 * @return String containing accuracy and confusion matrix from train/test.
	 */
	public String getTrainTestStatString() { //TODO implement ROC stuff
		
		try {
			
			String resultsString = "==========Accuracy==========\n\n";
			
			double weightedTPR = 0.0;
			double weightedFPR = 0.0;
			double weightedPre = 0.0;
			double weightedRec = 0.0;
			double weightedF1M = 0.0;
			double weightedMCC = 0.0;
			double weightedROC = 0.0;
			
			List<Author> stats = getAuthorStatistics();
			int correctDocs = 0;
			int totalDocs = 0;
			String authorInfoString="==========Detailed Accuracy by Author==========\n\n";
			authorInfoString+="\t\tTP Rate  FP Rate  Precision  Recall   F-Measure  MCC      Class\n";
			for (Author a: stats){

				if (!(a.getName().equals("_dummy_"))){
					correctDocs+=a.getTruePositiveCount();
					int numDocs = a.getNumberOfDocuments();
					totalDocs+=numDocs;
					authorInfoString+=String.format("\t\t%.3f    %.3f    %.3f      %.3f    %.3f      %.3f    %s\n",
						a.getTruePositiveRate(),a.getFalsePositiveRate(),a.getPrecision(),a.getRecall(),a.getF1Measure(),a.getMCC(),a.getName());
					
					//weighted statistics stuff
					
					weightedTPR+=a.getTruePositiveRate()*numDocs;
					weightedFPR+=a.getFalsePositiveRate()*numDocs;
					weightedPre+=a.getPrecision()*numDocs;
					weightedRec+=a.getRecall()*numDocs;
					weightedF1M+=a.getF1Measure()*numDocs;
					weightedMCC+=a.getMCC()*numDocs;
				}
			}
						
			resultsString+=String.format("\tCorrectly classified instances: %d\t%.3f %%\n",correctDocs,((double)correctDocs/totalDocs)*100.0);
			resultsString+=String.format("\tIncorrectly classified instances: %d\t%.3f %%\n\n",(totalDocs-correctDocs),
					((double)(totalDocs-correctDocs)/totalDocs)*100.0);
			
			resultsString+=authorInfoString;
			resultsString+=String.format("Weighted Avg.\t%.3f    %.3f    %.3f      %.3f    %.3f      %.3f\n\n",
					weightedTPR/totalDocs,weightedFPR/totalDocs,weightedPre/totalDocs,weightedRec/totalDocs,
					weightedF1M/totalDocs,weightedMCC/totalDocs);
			
			Evaluation eval = getTrainTestEval();
			resultsString += eval.toMatrixString() + "\n";
			
			return resultsString;
		
		} catch (Exception e) {
			System.out.println("Failed to get train/test statistics string");
			e.printStackTrace();
			return "";
		}
	}
	
	/**
	 * Returns the last training Weka Instances set that was used for classification.
	 * @return
	 * 		The last training Weka Instances set that was used for classification.
	 */
	public Instances getLastTrainingSet() {
		return trainingSet;
	}
	
	/**
	 * Returns the last test Weka Instances set that was used for classification.
	 * @return
	 * 		The last test Weka Instances set that was used for classification.
	 */
	public Instances getLastTestSet() {
		return testSet;
	}
	
	/**
	 * Returns the entire data in one Weka Instances set.
	 * @return
	 * 		The entire data in one Weka Instances set.
	 */
	public Instances getAllInstances() {
		Instances all = new Instances(trainingSet);
		for (int i=0; i<testSet.numInstances(); i++) {
			all.add(testSet.instance(i));
		}
		return all;
	}
	
	/**
	 * Returns the last list of author names.
	 * @return
	 * 		The last list of author names.
	 */
	public List<String> getLastAuthors() {
		return authors;
	}
	
	/**
	 * Returns the last classification results or null if no classification was applied.
	 * @return
	 * 		The classification results or null if no classification was applied.
	 */
	public Map<String,Map<String, Double>> getLastResults() {
		return results;
	}
	
	/**
	 * Returns an array containing each of the analyzer's/classifier's options
	 * @return the arguments the analyzer has or null if it doesn't have any
	 */
	public String[] getOptions(){
			return options;
	}
	
	/**
	 * Sets the option string
	 * @param ops array of strings (the arguments) for the analyzer/classifier
	 */
	public void setOptions(String[] ops){
		options = ops;
	}
	
	/**
	 * An array of 1-2 sentences per index describing each option the analyzer has and the flag used to invoke each argument.
	 * It is essential that each the flag comes prior to the description and that in between them, an \<ARG\> tag appears.
	 * The arg parser expects the description in this format, and failing to comply to it will result in the inability to edit the args.
	 * Example:
	 * -C\<ARG\>Enables some function c such that...
	 * 
	 * @return a description corresponding to each option the analyzer/classifier has
	 */
	public abstract String[] optionsDescription();
	
	/**
	 * A string describing the analyzer. Should be formatted and ready for display.
	 * @return the string describing how the analyzer/classifier functions and its benefits/drawbacks
	 */
	public abstract String analyzerDescription();
	
	/**
	 * Describes the underlying classifier (if there is one)
	 * @return returns the analyzer's driving classifier if it has one, otherwise returns null.
	 */
	public Classifier getClassifier() {
		return null;
	}
	
	/**
	 * Returns the name of whatever is doing the "heavy lifting" in terms of classification
	 * @return the name of the analyzer/classifier being used
	 */
	public abstract String getName();
	
}
