package edu.drexel.psal.jstylo.analyzers;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;

import com.jgaap.JGAAPConstants;
import com.jgaap.generics.*;

import weka.attributeSelection.InfoGainAttributeEval;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader.ArffReader;
import edu.drexel.psal.JSANConstants;
import edu.drexel.psal.jstylo.generics.*;
import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.SynsetType;
import edu.smu.tspell.wordnet.WordNetDatabase;

/**
 * Implementation of the Writeprints method (supervised).
 * For more details see:<br>
 * Abbasi, A., Chen, H. (2008). Writeprints: A stylometric approach to identity-level identification
 * and similarity detection in cyberspace. ACM Trans. Inf. Syst., 26(2), 129.
 * 
 * @author Ariel Stolerman
 *
 */
public class WriteprintsAnalyzer extends Analyzer {
	
	/* ======
	 * fields
	 * ======
	 */
	
	/**
	 * The prefix given to any author of a test document.
	 */
	public static final String TEST_AUTHOR_NAME_PREFIX = "_test_"; 
	
	/**
	 * The list of training author data, including feature, basis and writeprint matrices.
	 */
	private List<AuthorWPData> trainAuthorData = new ArrayList<AuthorWPData>();
	
	/**
	 * The list of training author data, including feature, basis and writeprint matrices.
	 */
	private List<AuthorWPData> testAuthorData = new ArrayList<AuthorWPData>();
	
	/**
	 * Whether to average all feature vectors per author ending up with one feature vector
	 * or not. Increases performance but may reduce accuracy.
	 */
	private boolean averageFeatureVectors = true;
	
	/**
	 * Local logger
	 */
	public static MultiplePrintStream log = new MultiplePrintStream();
	
	/* ===================
	 * Getters and Setters
	 * ===================
	 */
	
	public boolean averageFeatureVectors()
	{
		return averageFeatureVectors;
	}
	
	public void setAverageFeatureVectors(boolean averageFeatureVectors)
	{
		this.averageFeatureVectors = averageFeatureVectors;
	}
	
	
	/* ==========
	 * operations
	 * ==========
	 */
	
	@Override
	public Map<String,Map<String, Double>> classify(Instances trainingSet,
			Instances testSet, List<Document> unknownDocs) {
		Logger.logln(">>> classify started");
		
		/* ========
		 * LEARNING
		 * ========
		 */
		Logger.logln("> Learning");
		
		trainAuthorData.clear();
		testAuthorData.clear();
		//TODO if we get weird results somewhere the two lines below might be the reason. Testing didn't reveal anything though
				//this was added because authors weren't being cleared between one classification and the next ie different presses of the "run analysis" button. 
		if (authors!=null)
			authors.clear();
		
		// initialize features, basis and writeprint matrices
		Attribute classAttribute = trainingSet.classAttribute();
		int numAuthors = classAttribute.numValues();
		String authorName;
		AuthorWPData authorData;
		// training set
		Logger.logln("Initializing training authors data:");
		for (int i = 0; i < numAuthors; i++) {
			authorName = classAttribute.value(i);
			authorData = new AuthorWPData(authorName);
			if (authors==null)
				authors = new ArrayList<String>();
			authors.add(authorName);
			//Logger.logln("- " + authorName);
			authorData.initFeatureMatrix(trainingSet, averageFeatureVectors);
			trainAuthorData.add(authorData);
			authorData.initBasisAndWriteprintMatrices();
		}
		// test set
		int numTestInstances = testSet.numInstances();
		// train-test mode
		if (unknownDocs != null) {
			Logger.logln("Initializing test authors data (author per test document):");
			for (int i = 0; i < numTestInstances; i++) {
				authorName =
						TEST_AUTHOR_NAME_PREFIX +
						String.format("%03d", i) + "_" +
						unknownDocs.get(i).getTitle();
				authorData = new AuthorWPData(authorName);
				Logger.logln("- " + authorName);
				authorData.initFeatureMatrix(testSet, i, averageFeatureVectors);
				testAuthorData.add(authorData);				
				authorData.initBasisAndWriteprintMatrices();
			}
		}
		// CV mode
		else {
			Logger.logln("Initializing test authors data (CV mode):");
			for (int i = 0; i < numAuthors; i++) {
				authorName = classAttribute.value(i);
				authorData = new AuthorWPData(authorName);
				Logger.log("- " + authorName);
				authorData.initFeatureMatrix(testSet, averageFeatureVectors);
				testAuthorData.add(authorData);
				authorData.initBasisAndWriteprintMatrices();
			}
		}
		
		// initialize result set
		results = new HashMap<String,Map<String,Double>>(trainAuthorData.size());
		
		// calculate information-gain over only training authors
		Logger.logln("Calculating information gain over training authors data");
		double[] IG = null;
		int numFeatures = trainingSet.numAttributes() - 1;
		try {
			IG = calcInfoGain(trainingSet, numFeatures);
		} catch (Exception e) {
			System.err.println("Error evaluating information gain.");
			e.printStackTrace();
			return null;
		}
		
		// initialize synonym count mapping
		Logger.logln("Initializing word synonym count");
		Map<Integer,Integer> wordsSynCount = calcSynonymCount(trainingSet,numFeatures);
		
		/* =======
		 * TESTING
		 * =======
		 */
		Logger.logln("> Testing");
		
		Matrix testPattern, trainPattern;
		double dist1, dist2, totalDist;
		AuthorWPData testDataCopy, trainDataCopy;
		for (AuthorWPData testData: testAuthorData) {
			Map<String,Double> testRes = new HashMap<String,Double>();

			for (AuthorWPData trainData: trainAuthorData) {
				testDataCopy = testData.halfClone();
				trainDataCopy = trainData.halfClone();
				
				// compute pattern matrices BEFORE adding pattern disruption
				testPattern = AuthorWPData.generatePattern(trainData, testData);
				trainPattern = AuthorWPData.generatePattern(testData, trainData);
				
				// add pattern disruptions
				testDataCopy.addPatternDisruption(trainData, IG, wordsSynCount, trainPattern);
				trainDataCopy.addPatternDisruption(testData, IG, wordsSynCount, testPattern);
				
				// compute pattern matrices AFTER adding pattern disruption
				testPattern = AuthorWPData.generatePattern(trainDataCopy, testDataCopy);
				trainPattern = AuthorWPData.generatePattern(testDataCopy, trainDataCopy);
				
				// compute distances
				dist1 = sumEuclideanDistance(testPattern, trainDataCopy.writeprint);
				dist2 = sumEuclideanDistance(trainPattern, testDataCopy.writeprint);
				
				// save the inverse to maintain the smallest distance as the best fit
				//ORIGINAL 
				//totalDist = - (dist1 + dist2);
				totalDist = 100 / (dist1 + dist2);
				testRes.put(trainData.authorName, totalDist);
				//Logger.logln("- " + trainData.authorName + ": " + totalDist+ " actual: "+testData.authorName);
			}
			results.put(testData.authorName,testRes);
		}
		Logger.logln(">>> classify finished");
		return normalize(results);
	}

	public Map<String,Map<String,Double>> normalize(Map<String,Map<String,Double>> data){
		Map<String,Map<String,Double>> normalizedResults=data;
		
		double totalValue=0;
		
		for (String testDoc : data.keySet()){//iterates over the test docs
			Map<String,Double> dataSet = data.get(testDoc);
			for (String potentialAuthor : dataSet.keySet()){ //then over potential authors
				totalValue+=data.get(testDoc).get(potentialAuthor).doubleValue(); //and gets the total "value"
			}
		}
		
		//redo the iteration, normalizing results into a percentage of the total
		for (String testDoc : data.keySet()){//iterates over the test docs
			Map<String,Double> dataSet = data.get(testDoc);
			for (String potentialAuthor : dataSet.keySet()){ //then over potential authors
				normalizedResults.get(testDoc).put(potentialAuthor, data.get(testDoc).get(potentialAuthor).doubleValue()/totalValue);
			}
		}
		
		return normalizedResults;
	}
	
	@Override
	public Evaluation runCrossValidation(Instances data, int folds,
			long randSeed) {
		Logger.logln(">>> runCrossValidation started");
		// setup
		data.setClass(data.attribute("authorName"));
		
		Instances randData = new Instances(data);
		Random rand = new Random(randSeed);
		randData.randomize(rand);
		randData.stratify(folds);
		
		Attribute extractedAuthors = data.instance(0).attribute(data.instance(0).classIndex());
		
		// prepare folds
		Instances[] foldData = new Instances[folds];
		for (int i = 0; i < folds; i ++)
			foldData[i] = randData.testCV(folds, i);
		int half = (folds / 2) + (folds % 2);
		
		// run CV - use half the folds for training, half for testing
		// E.g. for 10 folds, use 1-5 for training, 6-10 for testing; 2-6 for training, 1 + 7-10 for testing, etc.
		Instances train = new Instances(data,0);
		Instances test = new Instances(data,0);
		Instances tmp;
		int tmpSize;
		Map<String,Map<String,Double>> results = null;
		Map<String,Double> instResults;
		double max;
		String selected;
		
		//initialize underlying evaluation object
		Evaluation eval = null;
		SMO smo = new SMO();
		Instances allInstances = null;
		Instances goodInstances = null;
		
		//start the ARFF string
		String stub = "@RELATION <stub>\n";
		stub+="@ATTRIBUTE value {";
		for (int i=0; i<extractedAuthors.numValues();i++){
			stub+=i+",";
		}
		stub=stub.substring(0,stub.length()-1); //removes the extra comma
		stub+="}\n";
		stub+=  "@ATTRIBUTE authors {";
		
		//Add all authors
		for (int i=0; i<extractedAuthors.numValues();i++){
			stub+=extractedAuthors.value(i)+",";
		}
		stub=stub.substring(0,stub.length()-1); //removes the extra comma
		stub+="}\n";

		stub+="@DATA\n";
		
		//Add the correct author/data pair
		for (int i=0; i<extractedAuthors.numValues();i++){
	 
			stub+=i+","+extractedAuthors.value(i)+"\n";
		}
		
		//add the incorrect Author/data pairs
		for (int i=0; i<extractedAuthors.numValues();i++){
			for (int j=0; j<extractedAuthors.numValues();j++){
				if (i!=j){
					stub+=j+","+extractedAuthors.value(i)+"\n";
				}
			}
		}
		
		try{
			StringReader sReader = new StringReader(stub);
			ArffReader aReader = new ArffReader(sReader);
			allInstances = aReader.getData();
			allInstances.setClassIndex(allInstances.numAttributes()-1);
			
			goodInstances = new Instances(allInstances,0,extractedAuthors.numValues());
			
			smo.buildClassifier(goodInstances);
			eval = new Evaluation(allInstances);
			
		} catch (Exception e){
			e.printStackTrace();
		}
		
		for (int i = 0; i < folds; i ++) {
			Logger.logln("Running experiment " + (i + 1) + " out of " + folds);
			
			// initialize
			train.delete();
			test.delete();
			
			// prepare training set
			for (int j = i; j < i + half; j++) {
				tmp = foldData[j % folds];
				tmpSize = tmp.numInstances();
				for (int k = 0; k < tmpSize; k++)
					train.add(tmp.instance(k));
			}
			// prepare test set
			for (int j = i + half; j < i + folds; j++) {
				tmp = foldData[j % folds];
				tmpSize = tmp.numInstances();
				for (int k = 0; k < tmpSize; k++)
					test.add(tmp.instance(k));
			}
			
			// classify
			results = classify(train, test, null);
			selected = null;
			for (String testInstAuthor: results.keySet()) {
				max = Double.NEGATIVE_INFINITY;
				instResults = results.get(testInstAuthor);
				for (String key: instResults.keySet()) {
					if (max < instResults.get(key)) {
						max = instResults.get(key);
						selected = key;
					}
				}

				/* Loggers below are useful for watching the classification step-by-step*/
				
				if (testInstAuthor.equals(selected)){ //train the eval with a food instance
					int correctIndex = extractedAuthors.indexOfValue(testInstAuthor);
					
					try {
						//Logger.logln("Attempting to add correct instance at: "+correctIndex);
						//Logger.logln("\t "+"correctAuthor: "+testInstAuthor+" guess: "+selected);
						eval.evaluateModelOnce(smo,goodInstances.instance(correctIndex));
						//Logger.logln(eval.toMatrixString());
					} catch (Exception e) {
						e.printStackTrace();
					}								
									
				} else { //train it with a bad instance
					int correctIndex = extractedAuthors.indexOfValue(testInstAuthor);
					int incorrectIndex = extractedAuthors.indexOfValue(selected);

					try {

						//Logger.logln("Attempting to add incorrect instance at: "+(correctIndex*extractedAuthors.numValues()+incorrectIndex));
						//Logger.logln("\t "+"correctAuthor: "+testInstAuthor+" guess: "+selected);
						
						int index = extractedAuthors.numValues()-1; //moves the index past the good instances
						index+=extractedAuthors.numValues()*correctIndex; //moves to the correct "row"
						index+=incorrectIndex; //moves to correct "column"
						index-=correctIndex; //adjusts for the fact that there are numAuthors-1 cells per row in the bad instances part of the instance list					
						if (incorrectIndex<correctIndex)
							index+=1;
						
						eval.evaluateModelOnce(smo,allInstances.instance(index));
						//Logger.logln(eval.toMatrixString());
						
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		// Build and return results string
		return eval;
	}
	
	
	@Override
	/**
	 * TODO
	 */
	public Evaluation runCrossValidation(Instances data, int folds, long randSeed,
			int relaxFactor) {
		if (relaxFactor==1)
			return runCrossValidation(data,folds,randSeed);
		else {
			Logger.logln("runCrossValidation with relaxation factor not yet implemented for writeprints Analyzer.");
			return null;
		}
	}
	
	protected double fMeasure(double precision,double recall){
		return (2.0*(precision*recall)/(precision+recall));
	}
	
	/**
	 * Calculates and returns a mapping of authors to the number of documents of
	 * the corresponding author in the first dataset, the second dataset, and 
	 * the distances measured between them.<br>
	 * Both datasets must have the same features, including the same set of
	 * authors in the class attribute.
	 * @param dataset1
	 * @param dataset2
	 * @return
	 */
	public SortedMap<String, double[]> getCrossDatasetsDistances(
			Instances dataset1, Instances dataset2) {
		Logger.logln(">>> getCrossDatasetsDistances started");
		
		/* ========
		 * LEARNING
		 * ========
		 */
		Logger.logln("> Learning");
		
		List<AuthorWPData> dataset1AuthorData = new LinkedList<AuthorWPData>();
		List<AuthorWPData> dataset2AuthorData = new LinkedList<AuthorWPData>();
				
		// initialize features, basis and writeprint matrices
		Attribute classAttribute = dataset1.classAttribute();
		int numAuthors = classAttribute.numValues();
		String authorName;
		AuthorWPData authorData;
		
		// dataset1
		Logger.logln("Initializing dataset1 authors data:");
		for (int i = 0; i < numAuthors; i++) {
			authorName = classAttribute.value(i);
			authorData = new AuthorWPData(authorName);
			//Logger.logln("- " + authorName);
			authorData.initFeatureMatrix(dataset1, averageFeatureVectors);
			dataset1AuthorData.add(authorData);
			authorData.initBasisAndWriteprintMatrices();
		}
		
		// dataset2
		Logger.logln("Initializing dataset2 authors data:");
		for (int i = 0; i < numAuthors; i++) {
			authorName = classAttribute.value(i);
			authorData = new AuthorWPData(authorName);
			//Logger.logln("- " + authorName);
			authorData.initFeatureMatrix(dataset2, averageFeatureVectors);
			dataset2AuthorData.add(authorData);
			authorData.initBasisAndWriteprintMatrices();
		}
		
		// initialize result set
		SortedMap<String, double[]> results = new TreeMap<String,double[]>();
		
		// calculate information-gain
		Logger.logln("Calculating information gain over dataset1");
		double[] IG1 = null;
		int numFeatures = dataset1.numAttributes() - 1;
		try {
			IG1 = calcInfoGain(dataset1, numFeatures);
		} catch (Exception e) {
			System.err.println("Error evaluating information gain.");
			e.printStackTrace();
			return null;
		}
		Logger.logln("Calculating information gain over dataset1");
		double[] IG2 = null;
		try {
			IG2 = calcInfoGain(dataset2, numFeatures);
		} catch (Exception e) {
			System.err.println("Error evaluating information gain.");
			e.printStackTrace();
			return null;
		}
		
		// initialize synonym count mapping
		Logger.logln("Initializing word synonym count");
		Map<Integer,Integer> wordsSynCount =
				calcSynonymCount(dataset1,numFeatures);
		
		
		/* =====================
		 * CALCULATING DISTANCES
		 * =====================
		 */
		Logger.logln("> Calculating distances");
		
		Matrix dataset1Pattern, dataset2Pattern;
		double dist1, dist2, avgDist;
		AuthorWPData dataset1DataCopy, dataset2DataCopy;
		double min = Double.MAX_VALUE;
		String minAuthor = "";
		for (AuthorWPData dataset1Data: dataset1AuthorData) {
			Logger.logln("dataset1 author: " + dataset1Data.authorName);
			for (AuthorWPData dataset2Data: dataset2AuthorData)
			{
				//log.print("- dataset2 author: " + dataset2Data.authorName + ": ");
				
				dataset1DataCopy = dataset1Data.halfClone();
				dataset2DataCopy = dataset2Data.halfClone();

				// compute pattern matrices BEFORE adding pattern disruption
				dataset1Pattern = AuthorWPData.generatePattern(dataset2Data, dataset1Data);
				dataset2Pattern = AuthorWPData.generatePattern(dataset1Data, dataset2Data);

				// add pattern disruptions
				dataset1DataCopy.addPatternDisruption(dataset2Data, IG2, wordsSynCount, dataset2Pattern);
				dataset2DataCopy.addPatternDisruption(dataset1Data, IG1, wordsSynCount, dataset1Pattern);

				// compute pattern matrices AFTER adding pattern disruption
				dataset1Pattern = AuthorWPData.generatePattern(dataset2DataCopy, dataset1DataCopy);
				dataset2Pattern = AuthorWPData.generatePattern(dataset1DataCopy, dataset2DataCopy);

				// compute distances
				dist2 = sumEuclideanDistance(dataset1Pattern, dataset2DataCopy.writeprint);
				dist1 = sumEuclideanDistance(dataset2Pattern, dataset1DataCopy.writeprint);

				// compute the average distance
				avgDist = (dist1 + dist2) / 2;
				results.put(dataset1Data.authorName + "," + dataset2Data.authorName,
						new double[]{
						dataset1Data.numAuthorInstances,
						dataset2Data.numAuthorInstances,
						avgDist
				});
				if (min > avgDist)
				{
					min = avgDist;
					minAuthor = dataset2Data.authorName;
				}
				//Logger.logln(avgDist);
			}
			Logger.logln("minimum distance author: " + minAuthor +
					", distance: " + min);
		}
		Logger.logln(">>> getCrossDatasetsDistances finished");
		return results;
	}
	
	/* ===============
	 * utility methods
	 * ===============
	 */
	
	private static WordNetDatabase wndb = null;
	
	/**
	 * Initializes the Wordnet database.
	 * @throws IOException 
	 */
	private static void initWordnetDB() {
		URL url = Thread.currentThread().getClass().getResource(
				JGAAPConstants.JGAAP_RESOURCE_PACKAGE+"wordnet");
		System.setProperty("wordnet.database.dir", url.getPath());
		wndb = WordNetDatabase.getFileInstance();
	}

	/**
	 * Used to identify word-based features.
	 */
	private static String[] wordFeatures = {
		"Function-Words",
		"Words",
		"Word-Bigrams",
		"Word-Trigrams",
		"Misspelled-Words"
	};
	
	/**
	 * Constructs a mapping from all word-based feature indices to the number of their synonyms.
	 * The synonym counted are only those belonging to synsets of the most common part-of-speech
	 * synset-type. If the feature is an n-gram feature, the synonym count is the multiplication
	 * of synonym count values of each word in the n-gram.
	 * @param trainingSet
	 * 		The training set from which to extract the features.
	 * @param numFeatures
	 * 		The number of features.
	 * @return
	 * 		A mapping from the word feature indices of the given training set to the synonym count.
	 */
	private static Map<Integer,Integer> calcSynonymCount(Instances trainingSet, int numFeatures) {
		
		// initialize
		Map<Integer,Integer> synCountMap = new HashMap<Integer,Integer>(numFeatures);
		if (wndb == null)
			initWordnetDB();
		
		boolean isWordFeature;
		Attribute feature;
		String featureName;
		String[] words;
		int synCount;
		Synset[] synsets, tmpSynsets;
		SynsetType[] allTypes = SynsetType.ALL_TYPES;
		Set<String> synonyms;
		for (int j = 0; j < numFeatures; j ++) {
			feature = trainingSet.attribute(j);
			featureName = feature.name();
			
			// check whether it is a word feature, else continue
			isWordFeature = false;
			for (String wordFeature: wordFeatures)
				if (featureName.startsWith(wordFeature)) {
					isWordFeature = true;
					break;
				}
			if (!isWordFeature)
				continue;
			
			// find synonym count for all word features
			// multiply synonym-count for n-gram features
			synCount = 1;
			words = getWordsFromFeatureName(featureName);
			for (String word: words) {
				// find the SynsetType with the maximum number of synsets
				synsets = wndb.getSynsets(word, allTypes[0]);
				for (int i = 1; i < allTypes.length; i++) {
					tmpSynsets = wndb.getSynsets(word, allTypes[i]);
					if (tmpSynsets.length > synsets.length)
						synsets = tmpSynsets;
				}

				// count synonyms
				synonyms = new HashSet<String>();
				for (Synset synset: synsets)
					synonyms.addAll(Arrays.asList(synset.getWordForms()));
				if (!synonyms.isEmpty())
					synCount *= synonyms.size();
			}
			synCountMap.put(j, synCount);
		}

		return synCountMap;
	}
	
	/**
	 * Extracts the words from the given feature name and returns them in an
	 * array.
	 * @param featureName
	 * 		The feature name, of the form <code>FEATURE-TYPE-{WORDS}</code>.
	 * @return
	 */
	private static String[] getWordsFromFeatureName(String featureName) {
		String content = featureName.replaceAll(".*\\{", "").replace("}", "");
		if (!featureName.contains("grams"))
			return new String[]{content};
		else {
			content = content.substring(1, content.length() - 1);
			return content.split("\\)-\\(");
		}
	}
	
	/**
	 * Calculates and returns the information gain vector for all features
	 * based on the given training set.
	 * @param trainingSet
	 * 		The training set to calculate information gain on.
	 * @param numFeatures
	 * 		The number of features.
	 * @return
	 * 		The information gain vector for all features based on the given
	 * 		training set.
	 * @throws Exception
	 * 		If an error is encountered during information gain evaluation.
	 */
	private static double[] calcInfoGain(Instances trainingSet, int numFeatures) throws Exception {		
		InfoGainAttributeEval ig = new InfoGainAttributeEval();
		ig.buildEvaluator(trainingSet);
		double[] IG = new double[numFeatures];
		for (int j = 0; j < numFeatures; j++)
			IG[j] = ig.evaluateAttribute(j);
		return IG;
	}
	
	/**
	 * Returns the average of the Euclidean distance between every 
	 * pair of columns (corresponding to document feature values)
	 * of the given matrices.
	 * @param a
	 * 		The first matrix.
	 * @param b
	 * 		The second matrix.
	 * @return
	 */
	private static double sumEuclideanDistance(Matrix a, Matrix b) {
		double sum = 0;
		double colsDiff, tmp;
		int numACols = a.getColumnDimension();
		int numBCols = b.getColumnDimension();
		int total = numACols * numBCols;
		int numFeatures = a.getRowDimension();
		for (int i = 0; i < numACols; i++) {
			for (int j = 0; j < numBCols; j++) {
				colsDiff = 0;
				for (int k = 0; k < numFeatures; k++) {
					tmp = a.get(k,i) - b.get(k, j);
					colsDiff += tmp * tmp;
				}
				sum += Math.sqrt(colsDiff) / total;
			}
		}
		return sum;
	}
	
	//In this case, the analyzer itself is doing the heavy lifting
	@Override
	public String getName(){
		return "edu.drexel.psal.jstylo.analyzers.writeprints.WriteprintsAnalyzer";
	}

	//does not have args to describe
	@Override
	public String[] optionsDescription() {
		return null;
	}

	//TODO add lengthy-ish description of what this analyzer does and its +/-
	@Override
	public String analyzerDescription() {
		String description =
				"Writeprints Analyzer\n" +
				"An experimental analyzer that is not fully completed, but has multiple usable functionalities.\n" +
				"\n" +
				"This classifier can run cross validation and test/classification.\n" +
				"However, it can not run cross validation with a relaxation factor.\n'" +
				"At this time, WriteprintsAnalyzer does not accept any args.\n" +
				"Performance has not yet been optimized.\n" +
				"\n"
				;
		return description;
	}
	
	// ============================================================================================
	// ============================================================================================
	
	
	/**
	 * Main for testing.
	 * @param args
	 */
	/*
	public static void main(String[] args) throws Exception {
		// initialize log
		PrintStream logPS = new PrintStream(new File("./log/" + MultiplePrintStream.getLogFilename()));
		log = new MultiplePrintStream(System.out, logPS);
		
		WriteprintsAnalyzer wa = new WriteprintsAnalyzer();
		
		//ProblemSet ps = new ProblemSet(JSANConstants.JSAN_PROBLEMSETS_PREFIX + "drexel_1_train_test.xml");
		ProblemSet ps = new ProblemSet(JSANConstants.JSAN_PROBLEMSETS_PREFIX + "drexel_1.xml");
		//ProblemSet ps = new ProblemSet(JSANConstants.JSAN_PROBLEMSETS_PREFIX + "amt.xml");
		
		CumulativeFeatureDriver cfd =
				new CumulativeFeatureDriver(JSANConstants.JSAN_FEATURESETS_PREFIX + "writeprints_feature_set_limited.xml");
		WekaInstancesBuilder wib = new WekaInstancesBuilder(false);
		List<Document> trainingDocs = ps.getAllTrainDocs();
		List<Document> testDocs = ps.getTestDocs();
		int numTrainDocs = trainingDocs.size();
		int numTestDocs = testDocs.size();
		
		// extract features
		System.out.println("feature pre extraction");
		trainingDocs.addAll(testDocs);
		System.out.println("feature extraction");
		wib.prepareTrainingSet(trainingDocs, cfd);
		System.out.println("feature post extraction");
		Instances trainingSet = wib.getTrainingSet();
		Instances testSet = new Instances(
				trainingSet,
				numTrainDocs,
				numTestDocs);
		wib.setTestSet(testSet);
		int total = numTrainDocs + numTestDocs;
		for (int i = total - 1; i >= numTrainDocs; i--)
			trainingSet.delete(i);
		System.out.println("done!");
	
		Instances train = wib.getTrainingSet();
		Instances test = wib.getTestSet();
	//	WekaInstancesBuilder.writeSetToARFF("d:/tmp/drexel_1_tt_train.arff", train);
		//WekaInstancesBuilder.writeSetToARFF("d:/tmp/drexel_1_tt_test.arff", test);
		//System.exit(0);
		
		//Instances train = new Instances(new FileReader(new File("c:/tmp/drexel_1_train.arff")));
		train.setClassIndex(train.numAttributes() - 1);
		//Instances test = new Instances(new FileReader(new File("d:/tmp/drexel_1_tt_test.arff")));
		//test.setClassIndex(test.numAttributes() - 1);
		
		// classify
		
		System.out.println("classification");
		Map<String,Map<String, Double>> res = wa.classify(train, test, ps.getTestDocs());
		System.out.println("done!");
		Map<String,Double> docMap;
		String selectedAuthor = null;
		double maxValue;
		double success = 0;
		for (String doc: res.keySet()) {
			maxValue = Double.NEGATIVE_INFINITY;
			docMap = res.get(doc);
			System.out.println(doc+":");
			for (String key: docMap.keySet())
				if (maxValue < docMap.get(key)) {
					selectedAuthor = key;
					maxValue = docMap.get(key);
				}
			System.out.println("- "+selectedAuthor+": "+maxValue);
			success += doc.replaceFirst(TEST_AUTHOR_NAME_PREFIX + "\\d+_", "").startsWith(selectedAuthor) ? 1 : 0;
		}
		success = 100 * success / res.size();
		System.out.printf("Total accuracy: %.2f\n",success);
		
		
		//cross-validation
		wa.runCrossValidation(train, 10, 0);
	}*/
}

/**
 * Representation of an author (identity) data required for Writeprints.
 * Includes the feature matrix, basis matrix, writeprint matrix etc.
 * 
 * @author Ariel Stolerman
 *
 */
class AuthorWPData {

	// fields
	
	/**
	 * Local logger
	 */
	public static MultiplePrintStream log = new MultiplePrintStream();
	
	/**
	 * The constant for pattern disruption calculation:<br>
	 * <code>d_p = IG(c,p) * K * (syn_total + 1) * (syn_used + 1)</code>
	 */
	protected static final int K = 2;
	
	protected String authorName;
	protected int numInstances;
	protected int numAuthorInstances;
	protected int numFeatures;
	protected Matrix featureMatrix;
	protected double[] featureAverages;
	protected List<Integer> zeroFeatures;
	protected Matrix basisMatrix;
	protected Matrix writeprint;
	
	// constructor
	
	/**
	 * Constructs a new author Writeprints data object with the given author name.
	 * @param authorName
	 * 		The name of the author.
	 */
	public AuthorWPData(String authorName) {
		this.authorName = authorName;
	}
	
	
	// methods
	
	/**
	 * Extracts the feature matrix from the given training data (set of all
	 * extracted features) for this author, identified by the
	 * <code>authorName</code> value.<br>
	 * If the <code>average</code> parameter is <code>true</code>, sets the
	 * matrix to be a single vector which is the average values across all
	 * the author's feature vectors.<br>
	 * In addition records the list of features that have 0 frequency.
	 * @param trainingData
	 * 		The ARFF training data representing feature vectors of various
	 * 		authors, including the current author, to extract the feature
	 * 		values from.
	 * @param average
	 * 		Whether to save only one feature vector, which will be the average
	 * 		of all extracted feature vectors.
	 */
	public void initFeatureMatrix(Instances trainingData, boolean average) {
		// isolate only relevant feature-vectors in a new Instances object
		numInstances = trainingData.numInstances();
		int classIndex = trainingData.classIndex();
		Instances data = new Instances(trainingData, 0);
		for (int i = 0; i < numInstances; i++)
			if (trainingData.instance(i).stringValue(classIndex).equals(authorName))
			{
				data.add(trainingData.instance(i));
				numAuthorInstances++;
			}
		initFeatureMatrixHelper(data, average);
	}
	
	/**
	 * Extracts the feature matrix from the given training data (set of all
	 * extracted features), using only the given instance index.
	 * If the <code>average</code> parameter is <code>true</code>, sets the
	 * matrix to be a single vector which is the average values across all
	 * the author's feature vectors.<br>
	 * In addition records the list of features that have 0 frequency.
	 * @param trainingData
	 * 		The ARFF training data representing various feature vectors
	 * 		to extract the feature values from.
	 * @param instanceIndex
	 * 		The index of the instance to be used.
	 * @param average
	 * 		Whether to save only one feature vector, which will be the average
	 * 		of all extracted feature vectors.
	 */
	public void initFeatureMatrix(Instances trainingData, int instanceIndex,
			boolean average) {
		Instances data = new Instances(trainingData,1);
		data.add(trainingData.instance(instanceIndex));
		initFeatureMatrixHelper(data, average);
	}
	
	/**
	 * Extracts the feature matrix from the given training data.
	 * If the <code>average</code> parameter is <code>true</code>, sets the
	 * matrix to be a single vector which is the average values across all
	 * the author's feature vectors.<br>
	 * In addition records the list of features that have 0 frequency.
	 * @param data
	 * 		The ARFF training data representing various feature vectors
	 * 		to extract the feature values from.
	 * @param average
	 * 		Whether to save only one feature vector, which will be the average
	 * 		of all extracted feature vectors.
	 */
	private void initFeatureMatrixHelper(Instances data, boolean average) {
		int numInstances = data.numInstances();
		numFeatures = data.numAttributes() - 1; // exclude class attribute

		/*
		 * initialize a matrix of features (each row represents an instance, each
		 * column represents a feature).
		 */
		double[][] matrix = new double[numInstances][numFeatures];
		Instance inst;
		for (int i = 0; i < numInstances; i++) {
			inst = data.instance(i);
			for (int j = 0; j < numFeatures; j++)
				matrix[i][j] = inst.value(j);
		}

		// calculate feature averages (for later use)
		featureAverages = new double[numFeatures];
		for (int j = 0; j < numFeatures; j++) {
			for (int i = 0; i < numInstances; i++)
				featureAverages[j] += matrix[i][j];
			featureAverages[j] /= numInstances;
		}

		// save feature matrix
		if (average)
			featureMatrix = new Matrix(new double[][] {featureAverages});
		else
			featureMatrix = new Matrix(matrix);

		// record zero-frequency features for author
		zeroFeatures = new ArrayList<Integer>();
		boolean isZero;
		for (int j = 0; j < numFeatures; j++) {
			isZero = true;
			for (int i = 0; i < numInstances; i++)
				if (matrix[i][j] != 0) {
					isZero = false;
					break;
				}
			if (isZero)
				zeroFeatures.add(j);
		}
	}
	
	/**
	 * Derives the basis matrix (set of eigenvectors) from feature usage
	 * covariance matrix using Karhunen-Loeve transform (PCA) as described in
	 * {@link http://isa.umh.es/asignaturas/cscs/PR/3%20-%20Feature%20extraction.pdf}.
	 * In addition computes the author's writeprint pattern.
	 */
	public void initBasisAndWriteprintMatrices() {
		int numInstances = featureMatrix.getRowDimension();
		int numFeatures = featureMatrix.getColumnDimension();
		
		/* (1) calculate the covariance matrix */
		
		// calculate X, the (#features)x(#instances) matrix
		Matrix X = featureMatrix.transpose();
		// calculate MU, the (#features)x(#instances) matrix of feature means
		// where each cell i,j equals mean(feature_i)
		double[][] MU_matrix_values = new double[numFeatures][numInstances];
		for (int i = 0; i < numFeatures; i++)
			for (int j = 0; j < numInstances; j++)
				MU_matrix_values[i][j] = featureAverages[i];
		Matrix MU = new Matrix(MU_matrix_values);
		// calculate X - MU
		Matrix X_minus_MU = X.minus(MU);
		// finally, calculate the covariance matrix
		Matrix COV = X_minus_MU.times(X_minus_MU.transpose()).times(1 / ((double) numFeatures));
		
		/* (2)	calculate eigenvalues followed by eigenvectors - the basis matrix,
		 * 		and calculate the principal component matrix - the author's writeprint*/
		EigenvalueDecomposition eigenvalues = COV.eig();
		basisMatrix = eigenvalues.getV();
		writeprint = basisMatrix.transpose().times(X_minus_MU);
	}
	
	/**
	 * Generates a pattern for the target author using the target author's feature values
	 * and the basis author's basis matrix (in contrast with the paper, there's no particular
	 * use for the basis author's feature set, as it is the same for all authors in this
	 * implementation).
	 * @param basisAuthor
	 * 		The basis author (the one supplying the basis matrix).
	 * @param targetAuthor
	 * 		The target author (the one to generate the pattern for).
	 * @return
	 * 		The pattern matrix for the target author.
	 */
	public static Matrix generatePattern(AuthorWPData basisAuthor, AuthorWPData targetAuthor) {
		Matrix targetValuesTransposed = targetAuthor.featureMatrix.transpose();
		Matrix basisTransposed = basisAuthor.basisMatrix.transpose();
		return basisTransposed.times(targetValuesTransposed);
	}
	
	/**
	 * Adds pattern disruption values with respect to the given author data.
	 * That is, for any zero-frequency feature of this author, that is not a
	 * zero-frequency feature for the other author, adds pattern disruption values
	 * calculated as follows:<br>
	 * <code>d_p = IG(c,p) * K * (syn_total + 1) * (syn_used + 1)</code>
	 * @param other
	 * 		The other author data to add the pattern disruption with respect to.
	 * @param IG
	 * 		Information gain for all features.
	 * @param wordsSynCount
	 * 		Mapping from all word-based feature indices to their synonym-count.
	 * @param otherPattern
	 * 		The pattern of the other author generated with this author's basis matrix.
	 */
	public void addPatternDisruption(AuthorWPData other, double[] IG,
			Map<Integer,Integer> wordsSynCount, Matrix otherPattern) {
		
		// set pattern disruption values
		int synUsed, synTotal;
		double patternDisruption;
		double thisWPAvg, otherPatternAvg;
		int basisNumRows = basisMatrix.getRowDimension();
		for (int j: zeroFeatures) {
			if (!other.zeroFeatures.contains(j)) {
				if (wordsSynCount.keySet().contains(j)) {
					synUsed = 1; // simplifying synonym usage count
					synTotal = wordsSynCount.get(j);
				}
				else {
					synUsed = 0;
					synTotal = 0;
				}
				patternDisruption = IG[j] * K * (synTotal + 1) * (synUsed + 1);
				
				// update pattern disruption sign
				thisWPAvg = avgForRow(writeprint,j);
				otherPatternAvg = avgForRow(otherPattern, j);
				if (thisWPAvg > otherPatternAvg)
					patternDisruption *= -1;
				
				// update basis matrix with pattern disruption value
				for (int i = 0; i < basisNumRows; i++)
					basisMatrix.set(j, i, patternDisruption);
			}
		}
		
		// update writeprint
		writeprint = generatePattern(this, this);
	}
	
	/**
	 * Calculates and returns the average over all columns of the given matrix
	 * for the given row index.
	 * @param m
	 * 		The matrix.
	 * @param row
	 * 		The row index with respect to which calculate the average.
	 * @return
	 * 		The average over all columns of the given matrix for the given column index.
	 */
	private static double avgForRow(Matrix m, int row) {
		int numCols = m.getColumnDimension();
		double sum = 0;
		for (int i = 0; i < numCols; i ++)
			sum += m.get(row,i);
		return sum / numCols;
	}
	
	@Override
	protected AuthorWPData clone() {
		AuthorWPData cloned = new AuthorWPData(authorName);
		cloned.basisMatrix = new Matrix(basisMatrix.getArrayCopy());
		cloned.featureAverages = Arrays.copyOf(featureAverages, featureAverages.length);
		cloned.featureMatrix = new Matrix(featureMatrix.getArrayCopy());
		cloned.numFeatures = numFeatures;
		cloned.writeprint = new Matrix(writeprint.getArrayCopy());
		cloned.zeroFeatures = new ArrayList<Integer>(zeroFeatures);
		return cloned;
	}
	
	/**
	 * Clones only parts of this author data and shallow copies the rest.
	 * Only the basis and writeprint matrices are deep-copied.
	 * @return
	 * 		The half-cloned author data.
	 */
	protected AuthorWPData halfClone() {
		AuthorWPData halfCloned = new AuthorWPData(authorName);
		halfCloned.basisMatrix = new Matrix(basisMatrix.getArrayCopy());
		halfCloned.featureAverages = featureAverages;
		halfCloned.featureMatrix = featureMatrix;
		halfCloned.numFeatures = numFeatures;
		halfCloned.writeprint = new Matrix(writeprint.getArrayCopy());
		halfCloned.zeroFeatures = zeroFeatures;
		return halfCloned;
	}
	
	// ============================================================================================
	// ============================================================================================
	
	/**
	 * Main for testing
	 * @param args
	 */
	public static void main(String[] args) {
		AuthorWPData a = new AuthorWPData("a");
		double[][] d = new double[][]{
				{1,2,3},
				{4,5,6}
		};
		a.featureAverages = new double[] {2.5,3.5,4.5};
		a.featureMatrix = new Matrix(d);
		a.initBasisAndWriteprintMatrices();
		a.writeprint.print(4,4);
	}
}
