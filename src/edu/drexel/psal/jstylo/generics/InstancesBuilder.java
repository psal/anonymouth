package edu.drexel.psal.jstylo.generics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.jgaap.generics.Document;
import com.jgaap.generics.EventSet;

import edu.drexel.psal.jstylo.generics.Logger.LogOut;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;

/**
 * An API for the feature extraction process. Designed for running on a single machine
 * @author Travis Dutko
 */
public class InstancesBuilder extends Engine {

	//////////////////////////////////////////// Data
	
	// These vars should be initialized in the constructor and stay the same
	// throughout the entire process
	private boolean isSparse;	//sparse instances if true, dense if false
	private boolean useDocTitles;	//use doc titles as a feature? Not yet implemented
	private int numThreads;	//number of calc threads to use during parallelization

	// persistant data stored as we create it
	private ProblemSet ps;	//the documents
	private CumulativeFeatureDriver cfd;	//the driver used to extract features
	private List<List<EventSet>> eventList;	//the events/sets created from the extracted features
	private List<EventSet> relevantEvents;	//the events/sets to pay attention to
	private ArrayList<Attribute> attributes;	//the relevant events converted into attributes

	//ThreadArrays so that we can stop them if the user cancels something mid process
	FeatureExtractionThread[] featThreads;
	CreateTrainInstancesThread[] trainThreads;
	CreateTestInstancesThread[] testThreads;
	
	// Relevant data to spit out at the end
	private Instances trainingInstances;	//training doc Instances
	private Instances testInstances;	//testDoc Instances
	private double[][] infoGain;	//infoGain scores for all features

	//////////////////////////////////////////// Constructors
	
	/**
	 * Empty constructor. For use if you want to build it as you go, rather then
	 * all at once<br>
	 * Ensure not to try to do anything fancy with it until all required
	 * information is set
	 */
	public InstancesBuilder() {

	}

	/**
	 * Full path constructor (creates pset and cfd from strings)
	 * 
	 * @param probSetPath
	 *            the path to the XML file to read the ProblemSet from
	 * @param cfdPath
	 *            the path to the XML file to read the cumulativeFeatureDriver
	 *            from
	 * @param sparse
	 *            if sparse instances should be produced. If false, dense
	 *            instances will be produced
	 * @param udt
	 *            if document titles should be considered a part of
	 *            classification
	 * @param nt
	 *            number of threads to use whenever multithreading is possible
	 */
	public InstancesBuilder(String probSetPath, String cfdPath, boolean sparse,
			boolean udt, int nt) {

		// create the problemSet and cfd from files
		ps = null;
		cfd = null;
		try {
			ps = new ProblemSet(probSetPath);
			cfd = new CumulativeFeatureDriver(cfdPath);
		} catch (Exception e) {
			Logger.logln(
					"Failure loading ProblemSet and CumulativeFeatureDriver!",
					LogOut.STDERR);
			e.printStackTrace();
		}

		// read/initialize global variables
		isSparse = sparse;
		useDocTitles = udt;
		numThreads = nt;
	}

	/**
	 * Partial path constructor <br>
	 * booleans will be initialized to true and false, respectively
	 * 
	 * @param probSetPath
	 *            the path to the XML file to read the ProblemSet from
	 * @param cfdPath
	 *            the path to the XML file to read the cumulativeFeatureDriver
	 *            from
	 * @param nt
	 *            number of threads to use whenever multithreading is possible
	 */
	public InstancesBuilder(String probSetPath, String cfdPath, int nt) {

		// create the problemSet and cfd from files
		ps = null;
		cfd = null;
		try {
			ps = new ProblemSet(probSetPath);
			cfd = new CumulativeFeatureDriver(cfdPath);
		} catch (Exception e) {
			Logger.logln(
					"Failure loading ProblemSet and CumulativeFeatureDriver!",
					LogOut.STDERR);
			e.printStackTrace();
		}

		// read/initialize global variables
		isSparse = true;
		useDocTitles = false;
		numThreads = nt;

	}

	/**
	 * Full object constructor (Takes a pre-made problemSet and cfd)
	 * 
	 * @param probSetPath
	 *            the path to the XML file to read the ProblemSet from
	 * @param cfdPath
	 *            the path to the XML file to read the cumulativeFeatureDriver
	 *            from
	 * @param sparse
	 *            if sparse instances should be produced. If false, dense
	 *            instances will be produced
	 * @param udt
	 *            if document titles should be considered a part of
	 *            classification
	 * @param nt
	 *            number of threads to use whenever multithreading is possible
	 */
	public InstancesBuilder(ProblemSet probSet,
			CumulativeFeatureDriver cumulativeFeatureDriver, boolean sparse,
			boolean udt, int nt) {

		// copy the cfd and prob set
		ps = probSet;
		cfd = cumulativeFeatureDriver;

		// read/initialize global variables
		isSparse = sparse;
		useDocTitles = udt;
		numThreads = nt;

	}

	/**
	 * Partial object constructor <br>
	 * booleans will be initialized to true and false, respectively
	 * 
	 * @param probSetPath
	 *            the path to the XML file to read the ProblemSet from
	 * @param cfdPath
	 *            the path to the XML file to read the cumulativeFeatureDriver
	 *            from
	 * @param nt
	 *            number of threads to use whenever multithreading is possible
	 */
	public InstancesBuilder(ProblemSet probSet,
			CumulativeFeatureDriver cumulativeFeatureDriver, int nt) {

		// copy the cfd and prob set
		ps = probSet;
		cfd = cumulativeFeatureDriver;

		// read/initialize global variables
		isSparse = true;
		useDocTitles = false;
		numThreads = nt;

	}

	//////////////////////////////////////////// Methods
	
	/**
	 * Extracts the List\<EventSet\> from each document using a user-defined number of threads.
	 * Culls the eventSets as well.
	 * @throws Exception
	 */
	public void extractEventsThreaded() throws Exception {

		//pull in documents and find out how many there are
		List<Document> knownDocs = ps.getAllTrainDocs();
		int knownDocsSize = knownDocs.size();

		// initalize empty List<List<EventSet>>
		eventList = new ArrayList<List<EventSet>>(knownDocsSize);
		
		// if the num of threads is bigger then the number of docs, set it to
		// the max number of docs (extract each document's features in its own
		// thread
		int threadsToUse = numThreads;
		
		//if we have more threads than documents, that's a bit silly.
		if (numThreads > knownDocsSize) {
			threadsToUse = knownDocsSize;
		}
		
		//if some documents are leftover after divvying them up, increment the div
		int div = knownDocsSize / threadsToUse;
		if (div % knownDocsSize != 0)
			div++;
		
		//Parallelized feature extraction
		featThreads = new FeatureExtractionThread[threadsToUse];
		for (int thread = 0; thread < threadsToUse; thread++) //create the threads
			featThreads[thread] = new FeatureExtractionThread(div, thread,
					knownDocsSize, knownDocs, new CumulativeFeatureDriver(cfd));
		for (int thread = 0; thread < threadsToUse; thread++) //start them
			featThreads[thread].start();
		for (int thread = 0; thread < threadsToUse; thread++) //join them
			featThreads[thread].join();
		for (int thread = 0; thread < threadsToUse; thread++) //combine List<List<EventSet>>
			eventList.addAll(featThreads[thread].list);
		for (int thread = 0; thread < threadsToUse; thread++) //destroy threads
			featThreads[thread] = null;
		
		featThreads = null;
		
		//cull the List<List<EventSet>> before returning
		List<List<EventSet>> temp = cull(eventList, cfd);
		
		//return it now
		eventList = temp;
	}

	/**
	 * Initializes and stores the list of relevantEvents created from the culled List\<List\<EventSet\>\>
	 * @throws Exception
	 */
	public void initializeRelevantEvents() throws Exception {
		relevantEvents = getRelevantEvents(eventList, cfd);
	}

	/**
	 * Initializes and stores the list of attributes created from the eventSets and relevantEvents
	 * @throws Exception
	 */
	public void initializeAttributes() throws Exception {
		attributes = getAttributeList(eventList, relevantEvents, cfd,
				useDocTitles);
	}

	/**
	 * Threaded creation of training instances from gathered data
	 * @throws Exception
	 */
	public void createTrainingInstancesThreaded() throws Exception {

		//create dataset from attributes and numDocs
		trainingInstances = new Instances("Instances", attributes,
				eventList.size());
		
		//initialize/fetch data
		List<Instance> generatedInstances = new ArrayList<Instance>();
		int threadsToUse = numThreads;
		int numInstances = eventList.size();
		
		//if there are more threads than instances, that's just silly
		if (numThreads > numInstances) {
			threadsToUse = numInstances;
		}

		//initialize the div and make sure it captures everything
		int div = numInstances / threadsToUse;
		if (div % numInstances != 0)
			div++;

		//Parallelized magic
		trainThreads = new CreateTrainInstancesThread[threadsToUse];
		for (int thread = 0; thread < threadsToUse; thread++)
			trainThreads[thread] = new CreateTrainInstancesThread(trainingInstances,div,thread,numInstances,new CumulativeFeatureDriver(cfd));
		for (int thread = 0; thread < threadsToUse; thread++)
			trainThreads[thread].start();
		for (int thread = 0; thread < threadsToUse; thread++)
			trainThreads[thread].join();
		for (int thread = 0; thread < threadsToUse; thread++)
			generatedInstances.addAll(trainThreads[thread].list);
		for (int thread = 0; thread < threadsToUse; thread++)
			trainThreads[thread] = null;
		trainThreads = null;
		
		//add all of the generated instance objects into the Instances object.
		for (Instance inst: generatedInstances){
			trainingInstances.add(inst);
		}
		
	}

	/**
	 * Creates Test instances from all of the information gathered (if there are any)
	 * @throws Exception
	 */
	public void createTestInstancesThreaded() throws Exception {
		// create the empty Test instances object
		testInstances = new Instances("TestInstances", attributes, ps
				.getTestDocs().size());
		
		//if there are no test instances, set the instance object to null and move on with our lives
		if (ps.getTestDocs().size()==0){
			testInstances=null;
		} else { //otherwise go through the whole process
			
			//create/fetch data
			List<Instance> generatedInstances = new ArrayList<Instance>();
			int threadsToUse = numThreads;
			int numInstances = ps.getTestDocs().size();
		
			//make sure number of threads isn't silly
			if (numThreads > numInstances) {
				threadsToUse = numInstances;
			}

			//ensure the docs are divided correctly
			int div = numInstances / threadsToUse;
			if (div % numInstances != 0)
				div++;

			//Perform some parallelization magic
			testThreads = new CreateTestInstancesThread[threadsToUse];
			for (int thread = 0; thread < threadsToUse; thread++)
				testThreads[thread] = new CreateTestInstancesThread(testInstances,div,thread,numInstances, new CumulativeFeatureDriver(cfd));
			for (int thread = 0; thread < threadsToUse; thread++)
				testThreads[thread].start();
			for (int thread = 0; thread < threadsToUse; thread++)
				testThreads[thread].join();
			for (int thread = 0; thread < threadsToUse; thread++)
				generatedInstances.addAll(testThreads[thread].list);
			for (int thread = 0; thread < threadsToUse; thread++)
				testThreads[thread] = null;
			testThreads = null;
			
			//add all of the instance objects to the Instances object
			for (Instance inst: generatedInstances){
				testInstances.add(inst);
			}		
		}
	}

	//////////////////////////////////////////// InfoGain related things

	/**
	 * Applies the infoGain information to the training and (if present) test
	 * Instances. Overwrites our infoGain with the the revised version
	 * @throws Exception
	 */
	public void applyInfoGain(int n) throws Exception {
		setInfoGain(applyInfoGain(getInfoGain(), trainingInstances, n));
		if (testInstances != null) // Apply infoGain to test set if we have one
			applyInfoGain(getInfoGain(), testInstances, n);
	}

	/**
	 * Calculates infoGain across the trainingInstances
	 * @return a double[][] containing an entry for each feature denoting its index and how useful it is. Sorted via usefulness.
	 * @throws Exception
	 */
	public double[][] calculateInfoGain() throws Exception{
		setInfoGain(calcInfoGain(trainingInstances));
		return getInfoGain();
	}
	
	//////////////////////////////////////////// Setters/Getters
	
	/**
	 * @return Returns the infoGain value and stores it locally incase we decide
	 *         to apply it
	 */
	public double[][] getInfoGain() {
		return infoGain;
	}

	public void setInfoGain(double[][] doubles){
		infoGain = doubles;
	}
	
	/**
	 * @return Returns the problem set used by the InstancesBuilder
	 */
	public ProblemSet getProblemSet(){
		return ps;
	}
	
	/**
	 * A niche method for when you already have a training Instances object and
	 * only want to build test instances
	 * 
	 * @param ti
	 *            training Instances object
	 */
	public void setInstances(Instances ti) {
		trainingInstances = ti;
	}

	/**
	 * 
	 * @return The Instances object representing the training documents
	 */
	public Instances getTrainingInstances() {
		return trainingInstances;
	}

	/**
	 * 
	 * @return The Instances object representing the test document(s)
	 */
	public Instances getTestInstances() {
		return testInstances;
	}

	/**
	 * Sets the number of calculation threads to use for feature extraction.
	 * 
	 * @param nct
	 *            number of calculation threads to use.
	 */
	public void setNumThreads(int nct) {
		numThreads = nct;

		File jProps = new File("./jsan_resources/JStylo_prop.prop");
		if (jProps.exists()) { // write numCalcThreads to the file

			try {
				ArrayList<String> contents = new ArrayList<String>();
				FileReader fileReader = new FileReader(jProps);
				BufferedReader reader = new BufferedReader(fileReader);

				// read the file into memory and update the numCalcThreads
				// variable
				String nextLine = reader.readLine();
				while (nextLine != null) {
					String temp = nextLine;

					if (temp.contains("numCalcThreads")) {
						temp = "numCalcThreads=" + numThreads;
					}
					contents.add(temp);
					nextLine = reader.readLine();
				}
				reader.close();
				fileReader.close();
				// Write to the file
				FileWriter cleaner = new FileWriter(jProps, false);
				cleaner.write("");
				cleaner.close();

				FileWriter writer = new FileWriter(jProps, true);
				for (String s : contents) {
					writer.write(s + "\n");
				}
				writer.close();

			} catch (FileNotFoundException e) {
				Logger.logln(
						"Failed to read properties file! numCalcThreads defaulting to 1! Generating new prop file...",
						Logger.LogOut.STDERR);
				e.printStackTrace();
				numThreads = 1;
			} catch (IOException e) {
				Logger.logln(
						"Prop file empty! numCalcThreads defaulting to 1! Generating new prop file...",
						Logger.LogOut.STDERR);
				e.printStackTrace();
				numThreads = 1;
			}
		} else {
			numThreads = 1;
		}
	}
	
	/**
	 * @return the number of calculation threads we're using
	 */
	public int getNumThreads() {
		return numThreads;
	}

	/**
	 * @return true if we are using sparse instances, false if not
	 */
	public boolean isSparse() {
		return isSparse;
	}
	
	/**
	 * @return the list of training documents
	 */
	public List<Document> getTrainDocs(){
		return ps.getAllTrainDocs();
	}
	
	public List<Document> getTestDocs(){
		return ps.getTestDocs();
	}

	//////////////////////////////////////////// Utilities
	
	/**
	 * Sets classification relevant data to null
	 */
	public void reset() {
		ps = null;
		cfd = null;
		infoGain = null;
		trainingInstances = null;
		testInstances = null;
		killThreads();
	}

	/**
	 * For use when stopping analysis mid-way through it. Kills any processing threads
	 */
	public void killThreads() {
		
		if (!(featThreads==null)){
			for (int i=0; i<featThreads.length; i++){
				featThreads[i].stop();
			}
			for (int i=0; i<featThreads.length; i++){
				featThreads[i] = null;
			}
			featThreads=null;
		}
		
		if (!(trainThreads==null)){
			for (int i=0; i<trainThreads.length; i++){
				trainThreads[i].stop();
			}
			for (int i=0; i<trainThreads.length; i++){
				trainThreads[i] = null;
			}
			trainThreads=null;
		}
		
		if (!(testThreads==null)){
			for (int i=0; i<testThreads.length; i++){
				testThreads[i].stop();
			}
			for (int i=0; i<testThreads.length; i++){
				testThreads[i] = null;
			}
			testThreads=null;
		}
		
	}

	//////////////////////////////////////////// Thread Definitions
	
	/**
	 * A thread used to parallelize the creation of Test instances from a set of documents
	 * @param data the dataset (Instances object) that the test instance belongs to
	 * @param d The "div" or divide--how many documents each thread processes at most
	 * @param t The threadId. Keeps track of which thread is doing which div of documents
	 * @param n the total number of instances (used to put a cap on the last div so it doesn't try to process docs which don't exist
	 * @param cd A copy of the cfd to assess features with
	 * @author Travis Dutko
	 */
	public class CreateTestInstancesThread extends Thread {
		
		Instances dataset; //the dataset the test instances belong to
		ArrayList<Instance> list; //the list of test instances produced by this thread
		int div; //the number of instances to be created per thread
		int threadId; //the div this thread is dealing with
		int numInstances; //the total number of instances to be created
		CumulativeFeatureDriver cfd; //the cfd used to assess features
		
		//Constructor
		public CreateTestInstancesThread(Instances data, int d, int t, int n, CumulativeFeatureDriver cd){
			cfd=cd;
			dataset = data;
			list = new ArrayList<Instance>();
			div = d;
			threadId = t;
			numInstances = n;
		}
		
		//returns the list of instances created by this thread
		public ArrayList<Instance> getList() {
			return list;
		}
		
		//Run method
		@Override
		public void run() {
			//for all docs in this div
			for (int i = div * threadId; i < Math.min(numInstances, div
					* (threadId + 1)); i++)
				try {
					//grab the document
					Document doc = ps.getTestDocs().get(i);
					//extract its event sets
					List<EventSet> events = extractEventSets(doc, cfd);
					//cull the events/eventSets with respect to training events/sets
					events = cullWithRespectToTraining(relevantEvents, events, cfd);
					//build the instance
					Instance instance = createInstance(attributes, relevantEvents, cfd,
							events, doc, isSparse, useDocTitles);
					//add it to the dataset
					instance.setDataset(testInstances);
					//normalize it
					normInstance(cfd, instance, doc, useDocTitles);
					//add it to the collection of instances to be returned by the thread
					list.add(instance);
				} catch (Exception e) {
					Logger.logln("Error creating Test Instances!", LogOut.STDERR);
					Logger.logln(ps.getTestDocs().get(i).getFilePath());
					Logger.logln(e.getMessage(), LogOut.STDERR);
				}
		}
		
	}
	
	/**
	 * A thread used to parallelize the creation of Training instances from a set of documents
	 * @param data the dataset (Instances object) that the training instance belongs to
	 * @param d The "div" or divide--how many documents each thread processes at most
	 * @param t The threadId. Keeps track of which thread is doing which div of documents
	 * @param n the total number of instances (used to put a cap on the last div so it doesn't try to process docs which don't exist
	 * @param cd A copy of the cfd to assess features with
	 * @author Travis Dutko
	 */
	public class CreateTrainInstancesThread extends Thread {
		
		Instances dataset; //The dataset to map the instance to
		ArrayList<Instance> list; //the collection of instances for this div
		int div; //the number of instances to be created per thread
		int threadId; //the div for this thread
		int numInstances; //the total number of instances to be created
		CumulativeFeatureDriver cfd; //the cfd used to identify features/attributes
		
		//Constructor
		public CreateTrainInstancesThread(Instances data, int d, int t, int n,CumulativeFeatureDriver cd){
			dataset = data;
			list = new ArrayList<Instance>();
			div = d;
			threadId = t;
			numInstances = n;
			cfd=cd;
		}
		
		//Used to fetch this div's list of instances
		public ArrayList<Instance> getList() {
			return list;
		}
		
		//Run method
		@Override
		public void run() {
			//for all docs in this div
			for (int i = div * threadId; i < Math.min(numInstances, div
					* (threadId + 1)); i++)
				try {
					//grab the document
					Document doc = ps.getAllTrainDocs().get(i);
					//create the instance using it
					Instance instance = createInstance(attributes, relevantEvents, cfd,
							eventList.get(i), doc, isSparse, useDocTitles);
					//set it as a part of the dataset
					instance.setDataset(trainingInstances);
					//normalize it
					normInstance(cfd, instance, doc, useDocTitles);
					//add it to this div's list of completed instances
					list.add(instance);
				} catch (Exception e) {
					Logger.logln("Error creating Instances!", LogOut.STDERR);
					Logger.logln(e.getMessage(), LogOut.STDERR);
				}
		}
		
	}
	
	/**
	 * A thread used to extract features from documents in parallel. 
	 * @param d the divide--how many documents each thread processes at most
	 * @param threadId keeps track of which thread is doing which div of documents
	 * @param knownDocs a list of documents to be divvied up and have its features extracted
	 * @param cd A copy of the cfd to assess features with
	 * @author Travis Dutko
	 */
	public class FeatureExtractionThread extends Thread {
		
		ArrayList<List<EventSet>> list = new ArrayList<List<EventSet>>(); //The list of event sets for this division of docs
		int div; //the number of docs to be processed per thread
		int threadId; //the div index of this thread
		int knownDocsSize; //the number of docs total
		List<Document> knownDocs; //the docs to be extracted
		CumulativeFeatureDriver cfd; //the cfd to do the extracting with

		/**
		 * @return The list of extracted event sets for this division of documents
		 */
		public ArrayList<List<EventSet>> getList() {
			return list;
		}

		//Constructor
		public FeatureExtractionThread(int div, int threadId,
				int knownDocsSize, List<Document> knownDocs,
				CumulativeFeatureDriver cfd) {
			
			this.div = div;
			this.threadId = threadId;
			this.knownDocsSize = knownDocsSize;
			this.knownDocs = knownDocs;
			this.cfd = cfd;

		}

		//Runnable Method
		@Override
		public void run() {
			//for all the documents in this div to a maximum of the number of docs
			for (int i = div * threadId; i < Math.min(knownDocsSize, div
					* (threadId + 1)); i++){
				try {
					//try to extract the events
					List<EventSet> extractedEvents = cfd.createEventSets(ps.getAllTrainDocs().get(i));
					list.add(extractedEvents); //and add them to the list of list of eventsets
				} catch (Exception e) {
					Logger.logln("Error extracting features!", LogOut.STDERR);
					Logger.logln(e.getMessage(), LogOut.STDERR);
				}
			}
		}
	}
}
