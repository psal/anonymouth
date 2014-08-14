package edu.drexel.psal.anonymouth.engine;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import edu.drexel.psal.ANONConstants;
import edu.drexel.psal.anonymouth.gooie.ThePresident;
import edu.drexel.psal.jstylo.generics.*;

import com.jgaap.generics.Document;

/**
 * Does the magic* with the documents
 * 
 * 
 * * Creates Weka Instances objects, trains classifier, classifies, 
 * packages Instances data in arrays for {@link #DataAnalyzer}, and re-classifies.
 * 
 * @author Andrew W.E. McDonald
 *
 */
public class DocumentMagician {
	
	private final String NAME = "( "+this.getClass().getSimpleName()+" ) - ";
	
	private String writeDirectory = ANONConstants.DOC_MAGICIAN_WRITE_DIR;
	
	/**
	 * private boolean variable, true if sparse data is expected, false otherwise
	 */
	private boolean isSparse;
	
	/**
	 * Create instance of CumulativeFeatureDriver
	 */
	CumulativeFeatureDriver theseFeaturesCfd;
	public static int numProcessRequests = 0;
	
	public static String sep = File.separator;
	public static int numSampleAuthors;
	public static boolean classifier_saved = false;
	public static String classifier_path = "";
	private List<Document> trainSet;
	/**
	 * Set of document(s) to anonymize
	 * */
	private List<Document> toModifySet;
	private List<Document> noAuthorTrainSet;
	private List<Document> authorSamplesSet;
	private Map<String,Map<String,Double>> wekaResultMap;
	private InstanceConstructor instanceSet; 
	private InstanceConstructor authorInstanceConstructor;
	private InstanceConstructor noAuthorTrainInstanceConstructor;
	private ArrayList<String> attributeSet;
	private ArrayList<String> noAuthorTrainAttributeSet;
	private ArrayList<String> authorAttributeSet;
	private Double[][] authorAndTrainingInstances;
	private Double[][] trainingInstances;
	private Double[][] authorInstances;
	private Double[][] toModifyInstanceSet;
	private static ArrayList<String> trainTitlesList;
	private Set<String> trainSetAuthors;
	private ArrayList<String> toModifyTitlesList;
	private Instances authorAndTrainDat;
	private Instances toModifyDat;
	private Instances noAuthorTrainDat;
	private Instances authorOnlyDat;
	private String pathToModDoc;
	private String modifiedDocument;
	public static String authorToRemove;
	private Classifier theClassifier;
	private int authorNum = -1;
	private Classifier ABClassifier;
	private boolean firstTime = true;
	private Instances ABTrainingSet;
	
	public ArrayList<String> getAttributeSet(){
		return attributeSet;
	}
	
	public List<Document> getTrainSet(){
		return trainSet;
	}
	
	public Double[][] getAuthorAndTrainingInstances(){
		return authorAndTrainingInstances;
	}
	
	public Double[][] getTrainingInstances(){
		return trainingInstances;
	}
	
	public Double[][] getAuthorInstances(){
		return authorInstances;
	}
	
	public Double[][] getToModifyInstanceSet(){
		return toModifyInstanceSet;
	}
	
	public synchronized Map<String,Map<String,Double>> getWekaResultList(){
		return wekaResultMap;
	}
	
	/**
	 * Sets the weka analysis driver
	 * @param driver
	 * @return
	 */
	public String setWekaAnalysisDriver(String driver){// not used yet...
		Logger.logln(NAME+"called setWekaAnalysisDriver in DocumentMagician. Driver is: "+driver);
		return driver;
	}
	
	public static ArrayList<String> getTrainTitlesList(){
		return trainTitlesList;
	}
	
	public Set<String> getTrainSetAuthors(){
		return trainSetAuthors;
	}
	
	public ArrayList<String> getToModifyTitlesList(){
		return toModifyTitlesList;
	}
	
	public Instances getAuthorAndTrainDat(){
		return authorAndTrainDat;
	}
	
	public Instances getToModifyDat(){
		return toModifyDat;
	}
	
	public Instances getNoAuthorTrainDat(){
		return noAuthorTrainDat;
	}
	
	
	public Instances getAuthorOnlyDat(){
		return authorOnlyDat;
	}
	
	public void setModifiedDocument(String theDoc){
		modifiedDocument = theDoc;
	}
	
	
	/**
	 * Constructor for DocumentMagician
	 * @param isSparse 
	 */
	public DocumentMagician(boolean isSparse){
		Logger.logln(NAME+"Created new DocumentMagician, is sparse?  ... "+isSparse);
		this.isSparse = isSparse;
	}
	
// NOTE: "runPrimaryDocOps" and "runSecondaryDocOps" can be combined into one method!!!	
	
	/**
	 * Runs the primary document operations
	 * @param cfd
	 */
	public void runPrimaryDocOps(CumulativeFeatureDriver cfd){
		constructFeatureDrivers(cfd);
		buildTrainAndToModifyInstances();
		Logger.logln(NAME+"Done with runPrimaryDocOps");
	}
	
	/**
	 * Runs the secondary document operations
	 */
	public void runSecondaryDocOps(){
		buildAuthorAndNoAuthorTrainInstances();
		Logger.logln(NAME+"Done with runSecondaryDocOps");
	}
	
	/**
	 * Re-classifies the document that has been modified
	 */
	public void reRunModified() { 
		Logger.logln(NAME+"Called reRunModified (DocumentMagician)");
		InstanceConstructor oneAndDone = new InstanceConstructor(isSparse,theseFeaturesCfd,false);
		String pathToTempModdedDoc = writeDirectory+ThePresident.sessionName+"_"+numProcessRequests+".txt";
		Logger.logln(NAME+"Saving temporary file: "+pathToTempModdedDoc);
		try {
			File tempModdedDoc = new File(pathToTempModdedDoc);
			if(ThePresident.should_Keep_Auto_Saved_Anonymized_Docs == false)
				tempModdedDoc.deleteOnExit();
			FileWriter writer = new FileWriter(tempModdedDoc);
			writer.write(modifiedDocument);
			writer.close();
		} catch (IOException e) {
			Logger.logln(NAME+"Error saving temporary file to: "+pathToTempModdedDoc,Logger.LogOut.STDERR);
			e.printStackTrace();
		}
		Document newModdedDoc = new Document(pathToTempModdedDoc,toModifySet.get(0).getAuthor(), toModifySet.get(0).getTitle());
		Logger.logln(NAME+"Document opened");
		toModifySet.clear();
		toModifySet.add(0,newModdedDoc);
		oneAndDone.runInstanceBuilder(trainSet,toModifySet);
		authorAndTrainingInstances = oneAndDone.getTrainingInstances();
		toModifyInstanceSet = oneAndDone.getTestingInstances();
		authorAndTrainDat = oneAndDone.getFullTrainData();
		toModifyDat = oneAndDone.getFullTestData();
	}
	
	/**
	 * sets the CumulativeFeatureDriver
	 * @param cfd
	 */
	public void constructFeatureDrivers(CumulativeFeatureDriver cfd){
		Logger.logln(NAME+"Setting CumulativeFeatureDriver in DocumentMagician");
		theseFeaturesCfd = cfd;
	}
	
	/**
	 * Builds an instance set of the user's sample data and the 'other' sample data combined, and of the user's document to modify
	 */
	public void buildTrainAndToModifyInstances(){
		Logger.logln(NAME+"Building train (with author) and toModify instances");
		instanceSet = new InstanceConstructor(isSparse,theseFeaturesCfd,false);
		int i;
		int sizeTrainSet = trainSet.size();
		trainSetAuthors = new HashSet<String>(sizeTrainSet);
		for(i=0;i< sizeTrainSet ;i++){
			trainSetAuthors.add(trainSet.get(i).getAuthor());
		}
		String pathToTempModdedDoc = writeDirectory+ThePresident.sessionName+"_unmodified.txt";
		Logger.logln(NAME+"Saving original (unmodified) file: "+pathToTempModdedDoc);
		try {
			File tempModdedDoc = new File(pathToTempModdedDoc);
			if (!tempModdedDoc.exists())
				tempModdedDoc.createNewFile();
			FileWriter writer = new FileWriter(tempModdedDoc);
			if (!ANONConstants.SHOULD_KEEP_AUTO_SAVED_ORIGINAL_DOC)
				tempModdedDoc.deleteOnExit();
			writer.write(toModifySet.get(0).stringify());
			writer.close();
		} catch (IOException e) {
			Logger.logln(NAME+"Error saving temporary (unmodified) file to: "+pathToTempModdedDoc,Logger.LogOut.STDERR);
			e.printStackTrace();
		}
		toModifySet.get(0).setAuthor(authorToRemove);
		instanceSet.runInstanceBuilder(trainSet,toModifySet);
		attributeSet = instanceSet.getAttributeSet();
		authorAndTrainingInstances = instanceSet.getTrainingInstances();
		toModifyInstanceSet = instanceSet.getTestingInstances();
		authorAndTrainDat = instanceSet.getFullTrainData();
		toModifyDat = instanceSet.getFullTestData();
	}
	
	/**
	 * Builds instances of the user's sample documents, as well as the 'other' sample documents (each done separately) 
	 */
	public void buildAuthorAndNoAuthorTrainInstances(){
		Logger.logln(NAME+"Building author and no author train instances");
		authorInstanceConstructor = new InstanceConstructor(isSparse,theseFeaturesCfd,false);
		noAuthorTrainInstanceConstructor = new InstanceConstructor(isSparse,theseFeaturesCfd,false);
		int i;
		int authSampleSetSize = authorSamplesSet.size();
		noAuthorTrainInstanceConstructor.onlyBuildTrain(noAuthorTrainSet, false);
		noAuthorTrainAttributeSet = noAuthorTrainInstanceConstructor.getAttributeSet();
		trainingInstances = noAuthorTrainInstanceConstructor.getTrainingInstances();
		noAuthorTrainDat = noAuthorTrainInstanceConstructor.getFullTrainData();
		
		authorInstanceConstructor.onlyBuildTrain(authorSamplesSet, true);
		authorAttributeSet = authorInstanceConstructor.getAttributeSet();
		authorInstances = authorInstanceConstructor.getTrainingInstances();
		authorOnlyDat = authorInstanceConstructor.getFullTrainData();
		for(i=0;i<authSampleSetSize;i++){
			if(authorSamplesSet.get(i).getAuthor().equals(ANONConstants.DUMMY_NAME))
				authorSamplesSet.get(i).setAuthor(authorToRemove);
		}
	}
	
	/**
	 * Runs Weka and classifies the document to modify based on the selected classifier, with the selected options. The user's document to modify is classified 
	 * against all sample documents (user's samples, and 'other' samples)
	*/
	
	public synchronized void runWeka(){
		Logger.logln(NAME+"Called runWeka");
		Instances trainingSet = authorAndTrainDat;
		Instances testSet = toModifyDat;
		List<Document> unknownDocs = toModifySet;
		String saveClassifierAs = ANONConstants.PATH_TO_CLASSIFIER;
		Classifier classifier = theClassifier;
		List<String> authors = new ArrayList<String>();
		Attribute authorsAttr = trainingSet.attribute("authorName");
		
		for (int i=0; i< authorsAttr.numValues(); i++)
			authors.add(i,authorsAttr.value(i));
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
		for (int i=0; i<numOfInstances; i++) {
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
			
		wekaResultMap = res;
		Logger.logln(NAME+"Weka Done");
	}
	
	/**
	 * Provide current author's Anonymity, which is calculated the same way as what's shown in Result Window
	 * @param testSet set of author's document to be tested against
	 * @return author's Anonymity
	 */
	public double[] getAuthorAnonimity(Instances testSet) {
		int numOfInstances = testSet.numInstances();
		double[] res = new double[numOfInstances];
		if (firstTime) {
			ABTrainingSet = authorAndTrainDat;
			ABClassifier = theClassifier;
			String saveClassifierAs = ANONConstants.PATH_TO_CLASSIFIER;
			Attribute authorsAttr = ABTrainingSet.attribute("authorName");
		
			for (int i=0; i< authorsAttr.numValues(); i++)
				if (authorsAttr.value(i).equals(ANONConstants.DUMMY_NAME)) {
					authorNum = i;
					break;
				}
		
			// train classifier
			ABTrainingSet.setClass(authorsAttr);
			try {
				ABClassifier.buildClassifier(ABTrainingSet);
			} catch (Exception e) {
				e.printStackTrace();
			}
		
			// save classifier
			try {
				weka.core.SerializationHelper.write(saveClassifierAs, ABClassifier);
			} catch (Exception e1) {
				Logger.logln("ERROR! Could not save classifier to "+saveClassifierAs+"!",Logger.LogOut.STDERR);
				e1.printStackTrace();
			}
			for (int i=0; i<testSet.size(); i++) {
				Instance test = testSet.instance(i);
				test.setDataset(ABTrainingSet);
				try {
					res[i] = ABClassifier.distributionForInstance(test)[authorNum];
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			firstTime = false;
		}
		else
			for (int i=0; i<testSet.size(); i++) {
				Instance test = testSet.instance(i);
				test.setDataset(ABTrainingSet);
				try {
					res[i] = ABClassifier.distributionForInstance(test)[authorNum];
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		return res;
	}
	
	/**
	 * Performs operations that turn the documents into data, and calls other methods in this class in order to do this.
	 * @param pSet
	 * @param cfd
	 * @param classifier
	 */
	public void initialDocToData(ProblemSet pSet,CumulativeFeatureDriver cfd, Classifier classifier ){//,List<Map<String,Document>> forTraining, List<Document> forTesting){
		Logger.logln(NAME+"Entered initialDocToData in DocumentMagician");
		theClassifier = classifier;
		ProblemSet pSetCopy = pSet;
		//ProblemSet pSetCopy = new ProblemSet(pSet);
		trainSet = pSetCopy.getAllTrainDocs();
		
		toModifySet = pSetCopy.getAllTestDocs(); // docToModify is the test doc already
		Logger.logln(NAME+"True test doc author: "+toModifySet.get(0).getAuthor()); 
		
		authorToRemove = ProblemSet.getDummyAuthor(); 
		Logger.logln(NAME+"Dummy author: "+authorToRemove);
		authorSamplesSet = pSetCopy.removeAuthor(authorToRemove);
		authorSamplesSet.remove(toModifySet.get(0));
		noAuthorTrainSet = pSetCopy.getAllTrainDocs();
		int i = 0;
		int lenTSet = noAuthorTrainSet.size();
		trainTitlesList = new ArrayList<String>(lenTSet);
		loadDocs(toModifySet);
		loadDocs(trainSet);
		loadDocs(authorSamplesSet);
		loadDocs(noAuthorTrainSet);
		for (i=0;i<lenTSet;i++){
			trainTitlesList.add(i,noAuthorTrainSet.get(i).getTitle());
		}
		int lenTMSet = toModifySet.size();
		toModifyTitlesList = new ArrayList<String>(lenTMSet);
		for(i=0; i<lenTMSet; i++){
			toModifyTitlesList.add(i,toModifySet.get(i).getTitle());
		}
		numSampleAuthors = pSetCopy.getAuthors().size();
		Logger.logln(NAME+"Calling runPrimaryDocOps");
		runPrimaryDocOps(cfd);
		Logger.logln(NAME+"Calling runSecondaryDocOps");
		runSecondaryDocOps();
		Logger.logln(NAME+"Exiting initialDocToData in DocumentMagician");
	}
	
	/**
	 * Asks JGAAP's Document class to load the text of the documents.
	 * @param docs
	 */
	public void loadDocs(List<Document> docs){
		int i = 0;
		int numDocs = docs.size();
		for(i = 0; i < numDocs; i++){
			try {
				docs.get(i).load();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * returns a mapping of the name of the 2d array of instance data to the array; only done for transfer purposes.
	 * @return
	 */
	public HashMap<String,Double[][]> getPackagedInstanceData(){
		HashMap<String,Double[][]> dataForAnalysis = new HashMap<String,Double[][]>(3);
		dataForAnalysis.put("training",trainingInstances);
		dataForAnalysis.put("author",authorInstances);
		dataForAnalysis.put("modify",toModifyInstanceSet);
		return dataForAnalysis;
	}

	/**
	 * returns a mapping of the Instances objects names, to their respective unmodified Instances objects; only done for transfer purposes.
	 * @return
	 */
	public HashMap<String,Instances> getPackagedFullInstances(){
		HashMap<String,Instances> InstancesForAnalysis = new HashMap<String,Instances>(4);
		InstancesForAnalysis.put("authorAndTrain",authorAndTrainDat);
		InstancesForAnalysis.put("noAuthorTrain",noAuthorTrainDat);
		InstancesForAnalysis.put("toModify",toModifyDat);
		InstancesForAnalysis.put("authorOnly",authorOnlyDat);
		return InstancesForAnalysis;
		
	}
	
	/**
	 * Returns all three sets of documents used. 
	 * @return
	 */
	public ArrayList<List<Document>> getDocumentSets(){
		ArrayList<List<Document>> theDocs = new ArrayList<List<Document>>(3);
		theDocs.add(noAuthorTrainSet); // index 0
		theDocs.add(authorSamplesSet); // 1
		theDocs.add(toModifySet); // 2
		return theDocs;
	}
	
	/**
	 * Returns all three attribute sets. One attribute set is from the 'buildTrainAndToModify()' method, and the other two are from the 'buildAuthorAndNoAuthorTrainInstances()' method.
	 * There have to be three separate attribute sets because of the way that JStylo creates the Training and Test sets: If a given attribute does not appear in the training set of documents,
	 * it is ignored in the test set. To make sure all available/necessary attributes (features) are extracted, some of the extractions are done independently as training sets without test sets. 
	 * @return
	 */
	public ArrayList<ArrayList<String>> getAllAttributeSets(){
		ArrayList<ArrayList<String>> allAttributeSets = new ArrayList<ArrayList<String>>(3);
		allAttributeSets.add(0,attributeSet); //this attribute set is used for the authorAndTrain set and toModify set
		allAttributeSets.add(1,noAuthorTrainAttributeSet);
		allAttributeSets.add(2,authorAttributeSet);
		return allAttributeSets;
	}
	
	/**
	 * returns path to document to modify (this may not be used)
	 * @return
	 */
	public String getPathToModDoc(){
		return pathToModDoc;
	}
}


