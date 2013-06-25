package edu.drexel.psal.jstylo.analyzers;

import edu.drexel.psal.anonymouth.gooie.DriverPreProcessTabClassifiers;
import edu.drexel.psal.anonymouth.gooie.ThePresident;
import edu.drexel.psal.jstylo.generics.Analyzer;
import edu.drexel.psal.jstylo.generics.Logger;
import edu.drexel.psal.jstylo.generics.RelaxedEvaluation;

import java.io.File;
import java.util.*;

import com.jgaap.generics.Document;

import weka.classifiers.*;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.bayes.NaiveBayesMultinomial;
import weka.classifiers.bayes.NaiveBayesMultinomialUpdateable;
import weka.classifiers.bayes.NaiveBayesUpdateable;
import weka.classifiers.functions.Logistic;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.functions.SMO;
import weka.classifiers.lazy.IBk;
import weka.classifiers.rules.ZeroR;
import weka.classifiers.trees.J48;
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
	
	public WekaAnalyzer(String pathToClassifier){
		try {
			classifier = (Classifier) weka.core.SerializationHelper.read(pathToClassifier);
		} catch (Exception e1) {
			Logger.logln("ERROR! Failed loading trained classifier from "+pathToClassifier+"!",Logger.LogOut.STDERR);
			e1.printStackTrace();
		}
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
	 * Trains and then saves the Weka classifier using the given training set, and then classifies all instances in the given test set.
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
	public Map<String, Map<String, Double>> classifyAndSaveClassifier(Instances trainingSet, Instances testSet, List<Document> unknownDocs, String saveClassifierAs) {
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
		
		// save classifier
		try {
			weka.core.SerializationHelper.write(saveClassifierAs, classifier);
		} catch (Exception e1) {
			Logger.logln("ERROR! Could not save classifier to "+saveClassifierAs+"!",Logger.LogOut.STDERR);
			e1.printStackTrace();
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
	 * Uses the pre-trained classifier to classify all instances in the given test set.
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
	public Map<String, Map<String, Double>> classifyWithPretrainedClassifier(Instances testSet, ArrayList<String> unknownDocTitles, Set<String> trainSetAuthors) {
		this.testSet = testSet;
		
		// initialize authors (extract from training set)
		Iterator<String> authIter = trainSetAuthors.iterator();
		List<String> authors = new ArrayList<String>(trainSetAuthors.size());
		while(authIter.hasNext())
			authors.add(authIter.next());
		this.authors = authors;
		
		int numOfInstances = testSet.numInstances();
		int numOfAuthors = authors.size();
		
		Map<String,Map<String, Double>> res = new HashMap<String,Map<String,Double>>(numOfInstances);
		for (int i=0; i<numOfInstances; i++)
			res.put(unknownDocTitles.get(i), new HashMap<String,Double>(numOfAuthors));
		
	//trainingSet.setClass(trainingSet.attribute("authorName"));
		
		// classify test cases
		Map<String,Double> map;
		double[] currRes;
		for (int i=0; i<testSet.numInstances(); i++) {
			Instance test = testSet.instance(i);

			//test.setDataset(trainingSet);

			map = res.get(unknownDocTitles.get(i));
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
				Classifier clsCopy = AbstractClassifier.makeCopy(classifier);
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
		return ((OptionHandler)classifier).getOptions();
	}
	
	/**
	 * Returns the analyzer's classifier--so the classifier's name.
	 */
	@Override
	public String getName(){
		return classifier.getClass().getName();
	}
	
	/**
	 * Produces the string array of descriptions of each option the classifier has.
	 * At the moment it only returns descriptions of arguments that are provided by the classifier's default getOptions() method.
	 */
	@Override
	public String[] optionsDescription() {
		String[] optionsDesc=null;
		Integer[] skipIndices = null;
		Option skipped = null;
		
		@SuppressWarnings("unchecked")
		List<Option> descriptions = Collections.list(((OptionHandler)classifier).listOptions());
		
		//this chunk is pretty hideous. It should only be present temporarily
		//once we get all weka args working it should vanish
		//in the meantime though, it works via explicitly stating which arg descriptions should be ignored
		//the reason that some numbers are the same is that when an arg is skipped, the index doesn't increase,
		//so if you have more then one arg in a row you want to skip you have to remove the "same" index more then once
		//these should be optimized for the given classes, so I recommend not touching them unless you really have to, as they're rather
		//confusing.
		if (classifier instanceof NaiveBayes || classifier instanceof NaiveBayesUpdateable || classifier instanceof NaiveBayesMultinomial || classifier instanceof NaiveBayesMultinomialUpdateable){
			optionsDesc = null;
		} else if (classifier instanceof MultilayerPerceptron){
			optionsDesc = new String[descriptions.size()-3];
			skipIndices = new Integer[8];
			skipIndices[0]=6;
			skipIndices[1]=6;
			skipIndices[2]=6;
			skipIndices[3]=7;
			skipIndices[4]=7;
			skipIndices[5]=7;
			skipIndices[6]=7;
			skipIndices[7]=-1;
		} else if (classifier instanceof IBk){
			optionsDesc = new String[descriptions.size()-5];
			skipIndices = new Integer[6];
			skipIndices[0]=0;
			skipIndices[1]=0;
			skipIndices[2]=1;
			skipIndices[3]=2;
			skipIndices[4]=2;
			skipIndices[5]=-1;
		} else if (classifier instanceof J48){
			optionsDesc = new String[descriptions.size()-8];
			skipIndices = new Integer[9];
			skipIndices[0]=0;
			skipIndices[1]=2;
			skipIndices[2]=2;
			skipIndices[3]=2;
			skipIndices[4]=2;
			skipIndices[5]=2;
			skipIndices[6]=2;
			skipIndices[7]=2;
			skipIndices[8]=-1;
		} else if (classifier instanceof SMO){
			optionsDesc = new String[descriptions.size()-3];
			skipIndices = new Integer[4];
			skipIndices[0]=0;
			skipIndices[1]=0;
			skipIndices[2]=1;
			skipIndices[3]=-1;		
		} else {
			optionsDesc = new String[descriptions.size()];
		}
		//End of the ugly chunk
		
		
		if (optionsDesc!=null){
			int i=0;
			int n=0;
			for (Option opt : descriptions){
				if(skipIndices==null || skipIndices[n]==null || skipIndices[n]!=i){
				//	Logger.logln("Adding opt... "+" i "+i+" n "+n+" skip[n]: "+skipIndices[n]);
					optionsDesc[i]=opt.description();
					i++;
				}else{
				//Logger.logln("Skipping opt... "+" i "+i+" n "+n+" skip[n]: "+skipIndices[n]);
					if (n<skipIndices.length-1)
						n++;
				}			
			}
		}


		
		//For some reasom LibSVM's arguments are in the wrong order, rather then trying to rearrange them intelligently, I'm
		//just going to hard code them for now, as that would be a lot more work for really not much gain, since this
		//section should be removed once the classifier's args are working properly anyway.
		if (classifier instanceof LibSVM) {
			optionsDesc = new String[10];
			optionsDesc[0]="Set type of SVM (default: 0) 0= C-SVC 1= nu-SVC 2= one-class SVM 3= epsilon-SVR 4= nu-SVR";
			optionsDesc[1]="Set type of kernel function (default: 2)0= linear: u'*v 1= polynomial: (gamma*u'*v + coef0)^degree 2= radial basis function: exp(-gamma*|u-v|^2) 3= sigmoid: tanh(gamma*u'*v + coef0)";
			optionsDesc[2]="Set degree in kernel function (default: 3)";
			optionsDesc[3]="Set gamma in kernel function (default: 1/k)";
			optionsDesc[4]="Set coef0 in kernel function (default: 0)";
			optionsDesc[5]="Set the parameter nu of nu-SVC, one-class SVM, and nu-SVR (default: 0.5)";
			optionsDesc[6]="Set cache memory size in MB (default: 40)";
			optionsDesc[7]="Set the parameter C of C-SVC, epsilon-SVR, and nu-SVR (default: 1)";
			optionsDesc[8]="Set tolerance of termination criterion (default: 0.001)";
			optionsDesc[9]="Set the epsilon in loss function of epsilon-SVR (default: 0.1)";
		}
		
		return optionsDesc;
	}
	/** TODO add the other classifiers
	 * returns the description of the analyzer itself. Due to the way weka is coded, the instanceofs are necessary, as "globalInfo"
	 * is not listed in the "Classifier" abstract class, so we have to cast to the subclass in order to get it.
	 */
	@Override
	public String analyzerDescription() {
	
		// bayes
				if (classifier instanceof NaiveBayes) {
					return ((NaiveBayes) classifier).globalInfo();
				} else if (classifier instanceof NaiveBayesMultinomial) {
					return ((NaiveBayesMultinomial) classifier).globalInfo();
				}
				
				// functions
				else if (classifier instanceof Logistic) {
					return ((Logistic) classifier).globalInfo();
				}
				else if (classifier instanceof MultilayerPerceptron) {
					return ((MultilayerPerceptron) classifier).globalInfo();
				}
				else if (classifier instanceof SMO) {
					return ((SMO) classifier).globalInfo();
				}
				else if (classifier instanceof LibSVM) {
					LibSVM s = (LibSVM) classifier;
					String res = s.globalInfo()+"\n\nOptions:\n";
					Enumeration e = s.listOptions();
					while (e.hasMoreElements()) {
						Option o = (Option) e.nextElement();
						res += "-"+o.name()+": "+o.description()+"\n\n";
					}
					return res;
				}
				
				// lazy
				else if (classifier instanceof IBk) {
					return ((IBk) classifier).globalInfo();
				}
				
				// meta

				// misc

				// rules
				else if (classifier instanceof ZeroR) {
					return ((ZeroR) classifier).globalInfo();
				}

				// trees
				else if (classifier instanceof J48) {
					return ((J48) classifier).globalInfo();
				}
				
				else {
					return "No description available.";
				}
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
			((OptionHandler)classifier).setOptions(ops);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
