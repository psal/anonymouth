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

public class InstancesBuilder extends Engine {

	// These vars should be initialized in the constructor and stay the same
	// throughout the instances creation
	private boolean isSparse;
	private boolean useDocTitles;
	private int numThreads;

	// persistant data
	private ProblemSet ps;
	private CumulativeFeatureDriver cfd;
	private List<List<EventSet>> eventList;
	private List<EventSet> relevantEvents;
	private ArrayList<Attribute> attributes;

	// Data of interest
	private Instances trainingInstances;
	private Instances testInstances;
	private List<Integer> infoGain;

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

	public void extractEventsThreaded() throws Exception {

		//pull in documents and find out how many there are
		List<Document> knownDocs = ps.getAllTrainDocs();
		int knownDocsSize = knownDocs.size();

		// initalize empty List<List<EventSet>>
		eventList = new ArrayList<List<EventSet>>(knownDocsSize);
		ExecutorService pool = Executors.newCachedThreadPool();
		ConcurrentHashMap<Integer,List<EventSet>> eventSets = 
				new ConcurrentHashMap<Integer,List<EventSet>>(knownDocsSize);

		// if the num of threads is bigger then the number of docs, set it to
		// the max number of docs (extract each document's features in its own
		// thread
		
		int threadsToUse = numThreads;
		
		if (numThreads > knownDocsSize) {
			threadsToUse = knownDocsSize;
		}
		
		int div = knownDocsSize / threadsToUse;
		if (div % knownDocsSize != 0)
			div++;
		
		FeatureExtractionThread[] calcThreads = new FeatureExtractionThread[threadsToUse];
		for (int thread = 0; thread < threadsToUse; thread++)
			calcThreads[thread] = new FeatureExtractionThread(div, thread,
					knownDocsSize, knownDocs, new CumulativeFeatureDriver(cfd));
		for (int thread = 0; thread < threadsToUse; thread++)
			calcThreads[thread].start();
		for (int thread = 0; thread < threadsToUse; thread++)
			calcThreads[thread].join();
		for (int thread = 0; thread < threadsToUse; thread++)
			eventList.addAll(calcThreads[thread].list);
		for (int thread = 0; thread < threadsToUse; thread++)
			calcThreads[thread] = null;
		
		calcThreads = null;
		
		List<List<EventSet>> temp = cull(eventList, cfd);
		eventList = temp;
	}

	public void initializeRelevantEvents() throws Exception {
		relevantEvents = getRelevantEvents(eventList, cfd);
	}

	public void initializeAttributes() throws Exception {
		attributes = getAttributeList(eventList, relevantEvents, cfd,
				useDocTitles);
	}

	public void createTrainingInstancesThreaded() throws Exception {

		trainingInstances = new Instances("Instances", attributes,
				eventList.size());
		
		List<Instance> generatedInstances = new ArrayList<Instance>();
		int threadsToUse = numThreads;
		int numInstances = eventList.size();
		
		if (numThreads > numInstances) {
			threadsToUse = numInstances;
		}

		int div = numInstances / threadsToUse;

		if (div % numInstances != 0)
			div++;

		CreateTrainInstancesThread[] calcThreads = new CreateTrainInstancesThread[threadsToUse];
		for (int thread = 0; thread < threadsToUse; thread++)
			calcThreads[thread] = new CreateTrainInstancesThread(trainingInstances,div,thread,numInstances);
		for (int thread = 0; thread < threadsToUse; thread++)
			calcThreads[thread].start();
		for (int thread = 0; thread < threadsToUse; thread++)
			calcThreads[thread].join();
		for (int thread = 0; thread < threadsToUse; thread++)
			generatedInstances.addAll(calcThreads[thread].list);
		for (int thread = 0; thread < threadsToUse; thread++)
			calcThreads[thread] = null;
		calcThreads = null;
		
		for (Instance inst: generatedInstances){
			trainingInstances.add(inst);
		}
		
	}

	public void createTestInstancesThreaded() throws Exception {
		// create the empty Test instances object
		testInstances = new Instances("TestInstances", attributes, ps
				.getTestDocs().size());
		
		// generate the test instance objects from the list of list of event
		// sets and add them to the Instances object
		// TODO perhaps parallelize this as well? build a list of Instances
		// objects and then add go through each list (in order) and add the
		// instance objects to Instances?
		
		if (ps.getTestDocs().size()==0){
			testInstances=null;
		} else {
			
			List<Instance> generatedInstances = new ArrayList<Instance>();
			int threadsToUse = numThreads;
			int numInstances = ps.getTestDocs().size();
			
			if (numThreads > numInstances) {
				threadsToUse = numInstances;
			}

			int div = numInstances / threadsToUse;

			if (div % numInstances != 0)
				div++;

			CreateTestInstancesThread[] calcThreads = new CreateTestInstancesThread[threadsToUse];
			for (int thread = 0; thread < threadsToUse; thread++)
				calcThreads[thread] = new CreateTestInstancesThread(testInstances,div,thread,numInstances, new CumulativeFeatureDriver(cfd));
			for (int thread = 0; thread < threadsToUse; thread++)
				calcThreads[thread].start();
			for (int thread = 0; thread < threadsToUse; thread++)
				calcThreads[thread].join();
			for (int thread = 0; thread < threadsToUse; thread++)
				generatedInstances.addAll(calcThreads[thread].list);
			for (int thread = 0; thread < threadsToUse; thread++)
				calcThreads[thread] = null;
			calcThreads = null;
			
			for (Instance inst: generatedInstances){
				testInstances.add(inst);
			}		
		}
	}

	// Thread Definitions

	public class CreateTestInstancesThread extends Thread {
		
		Instances dataset;
		ArrayList<Instance> list;
		int div;
		int threadId;
		int numInstances;
		CumulativeFeatureDriver cfd;
		
		public CreateTestInstancesThread(Instances data, int d, int t, int n, CumulativeFeatureDriver cd){
			cfd=cd;
			dataset = data;
			list = new ArrayList<Instance>();
			div = d;
			threadId = t;
			numInstances = n;
		}
		
		public ArrayList<Instance> getList() {
			return list;
		}
		
		@Override
		public void run() {
			for (int i = div * threadId; i < Math.min(numInstances, div
					* (threadId + 1)); i++)
				try {
					Document doc = ps.getTestDocs().get(i);
					List<EventSet> events = extractEventSets(doc, cfd);
					events = cullWithRespectToTraining(relevantEvents, events, cfd);
					Instance instance = createInstance(attributes, relevantEvents, cfd,
							events, doc, isSparse, useDocTitles);
					instance.setDataset(testInstances);
					normInstance(cfd, instance, doc, useDocTitles);
					list.add(instance);
				} catch (Exception e) {
					Logger.logln("Error creating Test Instances!", LogOut.STDERR);
					Logger.logln(ps.getTestDocs().get(i).getFilePath());
					Logger.logln(e.getMessage(), LogOut.STDERR);
				}
		}
		
	}
	
	
	public class CreateTrainInstancesThread extends Thread {
		
		Instances dataset;
		ArrayList<Instance> list;
		int div;
		int threadId;
		int numInstances;
		
		public CreateTrainInstancesThread(Instances data, int d, int t, int n){
			dataset = data;
			list = new ArrayList<Instance>();
			div = d;
			threadId = t;
			numInstances = n;
		}
		
		public ArrayList<Instance> getList() {
			return list;
		}
		
		@Override
		public void run() {
			for (int i = div * threadId; i < Math.min(numInstances, div
					* (threadId + 1)); i++)
				try {
					Document doc = ps.getAllTrainDocs().get(i);
					Instance instance = createInstance(attributes, relevantEvents, cfd,
							eventList.get(i), doc, isSparse, useDocTitles);
					instance.setDataset(trainingInstances);
					normInstance(cfd, instance, doc, useDocTitles);
					list.add(instance);
				} catch (Exception e) {
					Logger.logln("Error creating Instances!", LogOut.STDERR);
					Logger.logln(e.getMessage(), LogOut.STDERR);
				}
		}
		
	}
	
	public class FeatureExtractionThread extends Thread {
		
		ArrayList<List<EventSet>> list = new ArrayList<List<EventSet>>();
		ConcurrentHashMap<Integer,List<EventSet>> eventSets;
		int div;
		int threadId;
		int knownDocsSize;
		List<Document> knownDocs;
		CumulativeFeatureDriver cfd;

		public ArrayList<List<EventSet>> getList() {
			return list;
		}

		public FeatureExtractionThread(int div, int threadId,
				int knownDocsSize, List<Document> knownDocs,
				CumulativeFeatureDriver cfd) {
			this.div = div;
			this.threadId = threadId;
			this.knownDocsSize = knownDocsSize;
			this.knownDocs = knownDocs;
			this.cfd = cfd;
		}

		@Override
		public void run() {
			for (int i = div * threadId; i < Math.min(knownDocsSize, div
					* (threadId + 1)); i++){
				try {
					List<EventSet> extractedEvents = cfd.createEventSets(ps.getAllTrainDocs().get(i));
					list.add(extractedEvents);
				} catch (Exception e) {
					Logger.logln("Error extracting features!", LogOut.STDERR);
					Logger.logln(e.getMessage(), LogOut.STDERR);
				}
			}
		}
		
	}

	/**
	 * Applies the infoGain information to the training and (if present) test
	 * Instances
	 * 
	 * @throws Exception
	 */
	public void applyInfoGain() throws Exception {
		applyInfoGain(infoGain, trainingInstances);
		if (testInstances != null) // Apply infoGain to test set if we have one
			applyInfoGain(infoGain, testInstances);
	}

	/**
	 * 
	 * @return Returns the infoGain value and stores it locally incase we decide
	 *         to apply it
	 * @throws Exception
	 */
	public List<Integer> getInfoGain(int n) throws Exception {
		infoGain = calcInfoGain(trainingInstances, n);
		return infoGain;
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

	public int getNumThreads() {
		return numThreads;
	}

	public boolean isSparse() {
		return isSparse;
	}
	
	public List<Document> getTrainDocs(){
		return ps.getAllTrainDocs();
	}

	/**
	 * Sets classification relevant data to null
	 */
	public void reset() {
		ps = null;
		cfd = null;
		infoGain = null;
		trainingInstances = null;
		testInstances = null;
	}

}
