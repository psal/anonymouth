package edu.drexel.psal.jstylo.analyzers;

import edu.drexel.psal.jstylo.generics.Analyzer;
import edu.drexel.psal.jstylo.generics.Logger;
import edu.drexel.psal.jstylo.generics.RelaxedEvaluation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import com.jgaap.generics.Document;

import weka.classifiers.*;
import weka.classifiers.bayes.*;
import weka.classifiers.functions.*;
import weka.classifiers.lazy.*;
import weka.classifiers.meta.*;
import weka.classifiers.misc.*;
import weka.classifiers.rules.*;
import weka.classifiers.trees.*;
import weka.core.*;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

/**
 * Designed as Weka-classifier-based analyzer. 
 * 
 * @author Ariel Stolerman
 */
public class WekaAnalyzer extends Analyzer {

	/* ======
	 * fields
	 * ======
	 */
	
	/**
	 * The underlying Weka classifier to be used.
	 */
	private Classifier classifier;
	
	/* ============
	 * constructors
	 * ============
	 */
	
	/**
	 * Default constructor with SMO SVM as default classifier.
	 */
	public WekaAnalyzer() {
		classifier = new weka.classifiers.functions.SMO();
	}
	
	public WekaAnalyzer(Classifier classifier) {
		this.classifier = classifier;
	}
	
	public WekaAnalyzer(Object obj){
		this.classifier = (Classifier) obj;
	}
	
	/* ==========
	 * operations
	 * ==========
	 */
		
	/**
	 * Trains the Weka classifier using the given training set, and then classifies all instances in the given test set.
	 * Returns list of distributions of classification probabilities per instance.
	 * @param trainingSet
	 * 		The Weka Instances dataset of the training instances.
	 * @param testSet
	 * 		The Weka Instances dataset of the test instances.
	 * @param unknownDocs
	 * 		The test documents to be deanonymized.
	 * @return
	 * 		The mapping of test documents to distributions of classification probabilities per instance, or null if prepare was
	 * 		not previously called. Each result in the list is a mapping from the author to its corresponding
	 * 		classification probability.
	 */
	@Override
	public Map<String, Map<String, Double>> classify(Instances trainingSet,	
			Instances testSet, List<Document> unknownDocs) {
		this.trainingSet = trainingSet;					
		this.testSet = testSet;
		// initialize authors (extract from training set)
		List<String> authors = new ArrayList<String>();
		Attribute authorsAttr = trainingSet.attribute("authorName");
		for (int i=0; i< authorsAttr.numValues(); i++)
			authors.add(i,authorsAttr.value(i));
		this.authors = authors;
		
		int numOfInstances = testSet.numInstances();
		int numOfAuthors = authors.size();
		
		Map<String,Map<String, Double>> res = new HashMap<String,Map<String,Double>>(numOfInstances);
		for (int i=0; i<numOfInstances; i++)
			res.put(unknownDocs.get(i).getTitle(), new HashMap<String,Double>(numOfAuthors));
		
		// train classifier
		trainingSet.setClass(authorsAttr);
		try {
			classifier.buildClassifier(trainingSet);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// classify test cases
		Map<String,Double> map;
		double[] currRes;
		for (int i=0; i<testSet.numInstances(); i++) {
			Instance test = testSet.instance(i);

			test.setDataset(trainingSet);

			map = res.get(unknownDocs.get(i).getTitle());
			try {
				currRes = classifier.distributionForInstance(test);
				for (int j=0; j<numOfAuthors; j++) {
					map.put(authors.get(j), currRes[j]);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		results = res;
		return res;
	}
	
	/**
	 * Trains the Weka classifier using the given training set, and then classifies all instances in the given test set.
	 * Before classifying, it removes the second attribute in both sets, that is the document title. This should be used when
	 * the WekaInstancesBuilder object used to create the sets has hasDocNames set to true.
	 * Returns list of distributions of classification probabilities per instance.
	 * @param trainingSet
	 * 		The Weka Instances dataset of the training instances.
	 * @param testSet
	 * 		The Weka Instances dataset of the test instances.
	 * @param unknownDocs
	 * 		The test documents to be deanonymized.
	 * @return
	 * 		The list of distributions of classification probabilities per instance, or null if prepare was
	 * 		not previously called. Each result in the list is a mapping from the author to its corresponding
	 * 		classification probability.
	 */
	public Map<String, Map<String, Double>> classifyRemoveTitle(Instances trainingSet,
			Instances testSet, List<Document> unknownDocs) {
		// remove titles
		Remove remove = new Remove();
		remove.setAttributeIndicesArray(new int[]{1});
		try {
			remove.setInputFormat(trainingSet);
			this.trainingSet = Filter.useFilter(trainingSet, remove);
			this.testSet = Filter.useFilter(testSet, remove);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		return classify(this.trainingSet,this.testSet,unknownDocs);
	}
	
	/**
	 * Runs cross validation with given number of folds on the given Instances object.
	 * @param data
	 * 		The data to run CV over.
	 * @param folds
	 * 		The number of folds to use.
	 * @param randSeed
	 * 		Random seed to be used for fold generation.
	 *  @return
	 * 		The evaluation object with cross-validation results, or null if failed running.
	 */
	@Override
	public Evaluation runCrossValidation(Instances data, int folds, long randSeed) {
		// setup
		data.setClass(data.attribute("authorName"));
		Instances randData = new Instances(data);
		Random rand = new Random(randSeed);
		randData.randomize(rand);
		randData.stratify(folds);

		// run CV
		Evaluation eval = null;
		try {
			eval = new Evaluation(randData);
			for (int n = 0; n < folds; n++) {
				Instances train = randData.trainCV(folds, n);
							
				Instances test = randData.testCV(folds, n);
				// build and evaluate classifier
				Classifier clsCopy = AbstractClassifier.makeCopy(classifier);
				clsCopy.buildClassifier(train);
				eval.evaluateModel(clsCopy, test);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return eval;
	}
	
	protected String resultsString(Evaluation eval){
		String results = "";
		
		results+=eval.toSummaryString(false)+"\n";
		try {
			results+=eval.toClassDetailsString()+"\n";
			results+=eval.toMatrixString()+"\n";
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return results;
	}
	
	@Override
	public Evaluation runCrossValidation(Instances data, int folds, long randSeed,
			int relaxFactor) {
		
		if (relaxFactor==1)
			return runCrossValidation(data,folds,randSeed);
		
		// setup
		data.setClass(data.attribute("authorName"));
		Instances randData = new Instances(data);
		Random rand = new Random(randSeed);
		randData.randomize(rand);
		randData.stratify(folds);

		// run CV
		RelaxedEvaluation eval = null;
		try {
			eval = new RelaxedEvaluation(randData, relaxFactor);
			for (int n = 0; n < folds; n++) {
				Instances train = randData.trainCV(folds, n);
				Instances test = randData.testCV(folds, n);
				// build and evaluate classifier
				Classifier clsCopy =  AbstractClassifier.makeCopy(classifier);
				clsCopy.buildClassifier(train);
				eval.evaluateModel(clsCopy, test);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return eval; 
	}
	
	/**
	 * Runs cross validation with given number of folds on the given Instances object.
	 * Uses 0 as default random seed for fold generation.
	 * @param data
	 * 		The data to run CV over.
	 * @param folds
	 * 		The number of folds to use.
	 * @return
	 * 		The evaluation object with cross-validation results, or null if did not succeed running.
	 */
	public Evaluation runCrossValidation(Instances data, int folds) {
		long randSeed = 0;
		return runCrossValidation(data, folds, randSeed);
	}
	
	
	/* =======
	 * getters
	 * =======
	 */
	
	/**
	 * Returns the underlying classifier.
	 * @return
	 * 		The underlying Weka classifier.
	 */
	@Override
	public Classifier getClassifier() {
		return classifier;
	}
	
	/**
	 * Returns the weka classifier's args
	 */
	@Override
	public String[] getOptions(){
		return ((OptionHandler) classifier).getOptions();
	}
	
	/**
	 * Returns the analyzer's classifier--so the classifier's name.
	 */
	@Override
	public String getName(){
		return classifier.getClass().getName();
	}
	
	/**
	 * Produces the string array of the flags and descriptions for all of the arguments the classifier can take.
	 */
	@Override
	public String[] optionsDescription() {
		ArrayList<String> optionsDesc= new ArrayList<String>();
		String[] optionsDescToReturn = null;
		
		Enumeration<Option> opts = ((OptionHandler) classifier).listOptions();
		Option nextOpt = null;
		while (opts.hasMoreElements()){
			nextOpt = opts.nextElement();
			optionsDesc.add(nextOpt.name()+"<ARG>"+nextOpt.description());
		}
		
		optionsDescToReturn = new String[optionsDesc.size()];
		
		int i=0;
		for (String s: optionsDesc){
			optionsDescToReturn[i]=s;
			i++;
		}
		
		return optionsDescToReturn;
	}
	
	/** 
	 * returns the description of the analyzer itself.
	 */
	@Override
	public String analyzerDescription() {
		return ((TechnicalInformationHandler) classifier).getTechnicalInformation().toString();
	}
	
	/* =======
	 * setters
	 * =======
	 */
	
	/**
	 * Sets the underlying Weka classifier to the given one.
	 * @param classifier
	 * 		The Weka classifier to set to.
	 */
	public void setClassifier(Classifier classifier) {
		this.classifier = classifier;
	}

	/**
	 * Sets the weka classifier's args.
	 */
	@Override
	public void setOptions(String[] ops){
		try {
			((OptionHandler) classifier).setOptions(ops);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
