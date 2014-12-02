package edu.drexel.psal.anonymouth.engine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import edu.drexel.psal.ANONConstants;
import edu.drexel.psal.anonymouth.gooie.ThePresident;
import edu.drexel.psal.anonymouth.helpers.ErrorHandler;
import edu.drexel.psal.anonymouth.helpers.FileHelper;
import edu.drexel.psal.jstylo.generics.CumulativeFeatureDriver;
import edu.drexel.psal.jstylo.generics.FullAPI;
import edu.drexel.psal.jstylo.generics.FullAPI.analysisType;
import edu.drexel.psal.jstylo.generics.InstancesBuilder;
import edu.drexel.psal.jstylo.generics.Logger;
import edu.drexel.psal.jstylo.generics.ProblemSet;

import com.jgaap.generics.*;

import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader.ArffReader;

/**
 * Constructs instances using Weka and JStylo. Features are extracted.
 * @author Andrew W.E. McDonald
 * @author Marc Barrowclift
 *
 */
public class InstanceConstructor {
	
	private final String NAME = "( "+this.getClass().getSimpleName()+" ) - ";

	/**
	 * private variable to hold the attributes of the training documents.
	 */
	private ArrayList<String> setAttributes;

	/**
	 * public method to retrieve the attributes of the training documents.
	 * @return
	 * 	String array containing attributes of training documents, each index of array holds one attribute, with indices corresponding to indices of String[] returned by @getTrainingInstances .
	 */
	public ArrayList<String> getAttributeSet(){
		return setAttributes;
	}
	
	/**
	 * private variable to hold the instances of the training documents.
	 */
	private Double[][] trainingInstances;
	
	/**
	 * public method to retrieve the instances of the training documents.
	 * @return
	 * 	double array containing instances of training documents.
	 */
	public Double[][] getTrainingInstances(){
		return trainingInstances;
	}
	
	
	/**
	 * private variable to hold the instances of the testing document(s).
	 */
	private Double[][] testingInstances;
	
	/**
	 * public method to retrieve the instances of the testing documents.
	 * @return
	 * 	double array containing instances of testing documents.
	 */
	public Double[][] getTestingInstances(){
		return testingInstances;
	}
	
	private CumulativeFeatureDriver theseFeaturesCfd;
	
	Instances trainingDat,testingDat;
	
	private boolean printStuff, useSparse;
	
	public FullAPI jstylo;
	
	/**
	 * Constructor for InstanceConstructor, accepts boolean variable that tells WekaInstancesBuilder whether to expect sparse data or not. (if unsure, set false) 
	 * @param isSparse - boolean, true if expecting sparse data, false otherwise or if unsure.
	 * @param cfd - cumulative feature driver. Contains all features that will be extracted from the documents
	 */
	public InstanceConstructor(boolean isSparse, CumulativeFeatureDriver cfd, boolean printStuff){
		//ib = new InstancesBuilder.Builder().cfd(cfd).isSparse(isSparse).numThreads(ThePresident.num_Tagging_Threads).build();
		//jstylo = new FullAPI.Builder().cfd(cfd).isSparse(isSparse).numThreads(ThePresident.num_Tagging_Threads).analysisType(analysisType.TRAIN_TEST_UNKNOWN).build();
		theseFeaturesCfd = cfd;
		useSparse = isSparse;
		this.printStuff =printStuff;
		Logger.logln(NAME+"InstanceConstuctor constructed");
	}
	
	
	/**
	 * method runInstanceBuilder uses an instance of WekaInstancesBuilder to extract the features of both the input
	 * trainDocs and testDoc(s).  
	 * @param trainDocs list of Document objects to train the Weka classifier on
	 * @param testDocs list (may be a single object list) of Document object(s) to classify.
	 * @return
	 * 	true if no errors
	 */
	public boolean runInstanceBuilder(List<Document> trainDocs,List<Document> testDocs){
		Logger.logln(NAME+"Running JStylo WekaInstancesBuilder from runInstanceBuilder in InstanceConstructor");
		int eye = 0;
		jstylo = null;
		if (printStuff == true) {
			char[] cRay = testDocs.get(0).getProcessedText();
			System.out.println("PRE-INSTANCE BUILDING:\n");
			for(eye = 0;eye<cRay.length;eye++)
				System.out.print(cRay[eye]);
			System.out.println();
		}
		
		File cacheDir = new File(ANONConstants.CACHE_PREFIX);
		Instances trainingCachedInstances;
		try {
			ProblemSet ps = new ProblemSet();
			HashMap<String, List<Document>> trainAuthorMap = DocumentMagician.mapToAuthor(trainDocs);
			cacheDir = new File(cacheDir, theseFeaturesCfd.getName());
			long cfdHash = theseFeaturesCfd.longHash();
			
			if (isCacheValid(cacheDir, cfdHash, useSparse)) {
				trainingCachedInstances = loadCachedFeatures(cacheDir, ps, trainAuthorMap);
			}
			else // the cache is invalid for all authors for this cfd. Delete the cache and continue.
			{
				resetCache(cacheDir, cfdHash, useSparse);
				for (Document d :  trainDocs){
					ps.addTrainDoc(d.getAuthor(), d);
				}
			}
			
			for (Document d : testDocs){
				ps.addTestDoc(d.getAuthor(), d);
			}
			buildJStylo(ps);
		} catch(Exception e) {
			ErrorHandler.StanfordPOSError();
		}

		// Initialize two new instances to hold training and testing instances (attributes and data)
		trainingDat=jstylo.getTrainingInstances();
		// save our updates to the cache (before adding our loaded features)
		saveExtractedFeatures(cacheDir);
		
		if (null != trainingCachedInstances) {
			for (Instance i : trainingCachedInstances) {
				trainingDat.add(i);
			}
			// TODO: How does this affect the "name" of the Instances, ie. what is the merged one set to?
		}
		testingDat=jstylo.getTestInstances();
		setAttributes=getAttributes(trainingDat);
		trainingInstances=getInstances(trainingDat);
		testingInstances=getInstances(testingDat);
		if(printStuff == true){
			char[] cRay = testDocs.get(0).getProcessedText();
			System.out.println("POST-INSTANCE BUILDING:\n");
			for(eye = 0;eye<cRay.length;eye++)
				System.out.print(cRay[eye]);
			System.out.println();
			System.out.println(testingDat.toString());
			System.exit(7);
		}
		return true;
	}


	private void saveExtractedFeatures(File cacheDir) {
		Map<String, Instances> instanceMap = jstylo.getTrainingMap();
		for (Map.Entry<String, Instances> entry : instanceMap.entrySet()) {
			File authorDir = new File(cacheDir, entry.getKey() + "/");
			File authorFeaturesFile = new File(authorDir,"features.arff");
			InstancesBuilder.writeToARFF(authorFeaturesFile, entry.getValue());
		}
	}

	/**
	 * Loads the cached features.
	 * If the cache is invalid for an author, it resets it, creating a timestamps file. It then adds
	 * 		documents by that author to the problem set.
	 * @param cacheDir
	 * @param ps
	 * @param trainAuthorMap
	 * @return
	 * @throws Exception
	 * @throws IOException
	 */
	private Instances loadCachedFeatures(File cacheDir, ProblemSet ps,
			HashMap<String, List<Document>> trainAuthorMap) throws Exception,
			IOException {
		Instances trainingCachedInstances = null;
		for (Map.Entry<String, List<Document>> entry : trainAuthorMap.entrySet()) {
			String author = entry.getKey();
			File authorDir = new File(cacheDir, author + "/");
			
			// Get a list of when the files in the directory were last modified
			long[] lastModified = getDocsLastModified(entry.getValue());
			if (isAuthorCacheValid(authorDir, author, lastModified)) {
				File authorFeatures = new File(authorDir,
						"features.arff");
				Instances toAdd = InstancesBuilder.readFromARFF(authorFeatures);
				
				if (trainingCachedInstances == null)
					trainingCachedInstances = toAdd;
				else {
					for (Instance i : toAdd) {
						trainingCachedInstances.add(i);
					}
				}
			} else { // cache is invalid. Delete it and set up a new one
				FileHelper.deleteRecursive(authorDir);
				authorDir.mkdir();
				File timeStamps = new File(authorDir, "timestamps.txt");
				BufferedWriter writer = new BufferedWriter(new FileWriter(timeStamps));
				for (int i=0; i < lastModified.length; i++) {
					writer.write(Long.toString(lastModified[i]) + "\n");
				}
				writer.close();
				
				for (Document d : trainAuthorMap.get(author)) {
					ps.addTrainDoc(author, d);
				}
			}
		}
		return trainingCachedInstances;
	}


	/**
	 * Returns a sorted list of entries of "last modified" values for a list of files.
	 * @param entry
	 * @return
	 */
	private long[] getDocsLastModified(List<Document> list) {
		long[] lastModified = new long[list.size()];
		File file;
		int i=0;
		for (Document d : list) {
			file = new File(d.getFilePath());
			lastModified[i] = file.lastModified();
			i++;
		}
		Arrays.sort(lastModified); // this is the current state of the documents
		return lastModified;
	}
	
	private void resetCache(File cacheDir, long cfdHash, boolean useSparse) throws Exception {
		FileHelper.deleteRecursive(cacheDir);
		cacheDir.mkdir();
		File hashFile = new File(cacheDir, "hash.txt");
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(hashFile));
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			writer.write(Long.toString(cfdHash) + "\n");
			writer.write(Boolean.toString(useSparse) + "\n");
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private boolean isCacheValid(File cacheDir, long cfdHash, boolean useSparse) {
		// TODO: check to see if this cache is valid based on cfd's hash and useSparse
		// TODO: performance may be improved if we make this return what is in the cache and check
		// 		isSparse first. If that is invalid, there's no need to get a hash of the cfd.
		
		// Hash file in cache directory should consist of two lines
		// 1) the hash
		// 2) useSparse (true/false)
		// Ex.	1230123867238238
		//		true
		File hashFile = new File(cacheDir, "hash.txt");
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(hashFile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}
		
		String cachedHash, cachedUseSparse;
		try {
			cachedHash = reader.readLine();
			cachedUseSparse = reader.readLine();
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return (Boolean.parseBoolean(cachedUseSparse) == useSparse) && (Long.parseLong(cachedHash) == cfdHash);
	}
	
	/**
	 * Check to see if the author cache is invalid.
	 * Works by comparing the sorted "last modified" list to the cached (sorted) list.
	 * @param authorDir
	 * @param author
	 * @param sortedLastModifiedList
	 * @return true if the lists are equal. false otherwise.
	 */
	private boolean isAuthorCacheValid(File authorDir, String author, long[] sortedLastModifiedList) {
		// Check to see if # files are equal & the timestamps are equal for each file (may need to sort first)
		// NOTE: I believe that if the number of timestamps are equal and the two sorted
		// 		 lists of timestamps are equal, then no documents were modified.
		//		 That's what I implemented below. May need to prove that at some point. (TODO)
		
		File timeStamps = new File(authorDir, "timestamps.txt");
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(timeStamps));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			return false;
		}
		String line;
		int i = 0;
		try {
			while ((line = reader.readLine()) != null) {
				if (line.isEmpty())
					continue;
				if (i > sortedLastModifiedList.length) {
					break; // there used to be more files. Cache is invalid. No need to keep reading.
				}
				if (!line.equals(Long.toString(sortedLastModifiedList[i]))) {
					reader.close();
					return false;
				}
				i++;
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return i == sortedLastModifiedList.length;
	}


	public void buildJStylo(ProblemSet ps){
		jstylo = new FullAPI.Builder().cfd(theseFeaturesCfd).isSparse(useSparse).ps(ps).
				numThreads(ThePresident.num_Tagging_Threads).analysisType(analysisType.TRAIN_TEST_UNKNOWN).build();
		jstylo.prepareInstances();
	}
	
	public boolean onlyBuildTrain(List<Document> trainDocs, boolean withAuthor) {
		jstylo = null;
		if (withAuthor)
			Logger.logln(NAME+"Only building train set");
		else
			Logger.logln(NAME+"Building train set with author");
		
		try {
			ProblemSet ps = new ProblemSet();
			for (Document d :  trainDocs){
				ps.addTrainDoc(d.getAuthor(), d);
			}
			jstylo = new FullAPI.Builder().cfd(theseFeaturesCfd).isSparse(useSparse).ps(ps).
					numThreads(ThePresident.num_Tagging_Threads).analysisType(analysisType.TRAIN_TEST_UNKNOWN).build();
			jstylo.prepareInstances();
		} catch(Exception e) {
			ErrorHandler.StanfordPOSError();
		}
		
		trainingDat=jstylo.getTrainingInstances();
		setAttributes=getAttributes(trainingDat);
		trainingInstances=getInstances(trainingDat);
		return true;
		
	}
	
	/**
	 * returns full set of training data in arff style formatting (contains list of attributes and data)
	 * @return
	 * 	Instances object containing training data
	 */
	public Instances getFullTrainData(){
		return trainingDat;
	}
	
	/**
	 * returns full set of test data in arff style formatting (contains list of attributes and data)
	 * @return
	 * 	Instances object containing testing data
	 */
	public Instances getFullTestData(){
		return testingDat;
	}
	
	
	/**
	 * Accepts Weka Instances object and returns the stripped attributes. Stripping performed by 'AttributeStripper'
	 * @param currentInstance - Weka Instances object (arff format)
	 * @return
	 */
	public ArrayList<String> getAttributes(Instances currentInstance){
		int i=0;
		String tempString;
		ArrayList<String> tempAttrib= new ArrayList<String>(currentInstance.numAttributes());
		for(i=0;i<currentInstance.numAttributes();i++){
			tempString = currentInstance.attribute(i).toString();
			if(tempString.contains("authorName")){
					tempAttrib.add(i,tempString);
					continue;
			}
			tempAttrib.add(i,AttributeStripper.strip(tempString));
		}
		return tempAttrib;
	}
	
	/**
	 * Accepts JSylo's Instances object and returns the instances (@data) portion of the ".arff file" (not really a file at this point though).
	 * @param currentInstance - JStylo's Instances object (arff format)
	 * @return
	 */
	public Double[][] getInstances(Instances currentInstance){
		int i=0;
		int j=0;
		int placeHolder;
		int numAttribs = currentInstance.numAttributes();
		int numInstances = currentInstance.numInstances();
		String tempString;
		String otherTempString;
		Double[][] tempInstance;
		
		tempInstance= new Double[numInstances][numAttribs];
		int skip = 0;
		for(i=0;i<numAttribs;i++){
			if(currentInstance.attribute(i).toString().contains("authorName")){
				skip=i;
				break;
			}
		}
		for(i=0;i<numInstances;i++){
				j=0;
				tempString =currentInstance.instance(i).toString()+",";
				while(!tempString.equals("")){
					placeHolder =tempString.indexOf(",");
					otherTempString = tempString.substring(0,placeHolder);
					if(j==skip){
						tempInstance[i][j] = null; // set author name/ID to null rather than simply omit - allows attribute lists indices to stay synchronized
					}
					else{
						tempInstance[i][j] = Double.valueOf(otherTempString).doubleValue(); 
					}
					tempString = tempString.substring(placeHolder+1);	
					j++;
				}
		}
		return tempInstance;
	}
	
}
