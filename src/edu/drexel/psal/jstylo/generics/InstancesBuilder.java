package edu.drexel.psal.jstylo.generics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.jgaap.generics.Document;
import com.jgaap.generics.EventSet;

import edu.drexel.psal.jstylo.generics.Logger.LogOut;
import edu.drexel.psal.jstylo.generics.WekaInstancesBuilder.CalcThread;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

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

		// initalize empty List<List<EventSet>>
		eventList = new ArrayList<List<EventSet>>();

		List<Document> knownDocs = ps.getAllTrainDocs();
		int knownDocsSize = knownDocs.size();

		// if the num of threads is bigger then the number of docs, set it to
		// the max number of docs (extract each document's features in its own
		// thread
		
		/*
		if (numThreads > knownDocsSize) {
			numThreads = knownDocsSize;
		}

		int div = knownDocsSize / numThreads;

		if (div % knownDocsSize != 0)
			div++;
		
		
		FeatureExtractionThread[] calcThreads = new FeatureExtractionThread[numThreads];
		for (int thread = 0; thread < numThreads; thread++)
			calcThreads[thread] = new FeatureExtractionThread(div, thread,
					knownDocsSize, knownDocs, new CumulativeFeatureDriver(cfd));
		for (int thread = 0; thread < numThreads; thread++)
			calcThreads[thread].start();
		for (int thread = 0; thread < numThreads; thread++)
			calcThreads[thread].join();
		for (int thread = 0; thread < numThreads; thread++)
			eventList.addAll(calcThreads[thread].list);
		for (int thread = 0; thread < numThreads; thread++)
			calcThreads[thread] = null;
		calcThreads = null;
		*/
		for (Document doc : knownDocs) {
			List<EventSet> events = extractEventSets(doc, cfd);
			eventList.add(events);
		}
		
		System.out.println("EventListSize pre cull: "+eventList.size());
		List<List<EventSet>> temp = cull(eventList,cfd);
		eventList = temp;
		System.out.println("EventListSize post cull: "+eventList.size());
		
	}

	public void initializeRelevantEvents() throws Exception{
		System.out.println("eventListSizeRel: "+eventList.size());
		relevantEvents = getRelevantEvents(eventList,cfd);
		System.out.println("relEvents.size: "+relevantEvents.size());
	}
	
	public void initializeAttributes() throws Exception{
		System.out.println("eventListSizeRel: "+eventList.size());
		System.out.println("relEvents.size: "+relevantEvents.size());
		attributes = getAttributeList(eventList,relevantEvents,cfd,useDocTitles);
		System.out.println("attributesSize: "+attributes.size());
	}
	
	public void createTrainingInstancesThreaded()
			throws Exception {

		// create instances objects from the lists lists of event sets
		// TODO perhaps parallelize this as well? build a list of Instances
		// objects and then add go through each list (in order) and add the
		// instance objects to Instances?
		trainingInstances = new Instances("Instances", attributes, 100);
		int i = 0;
		for (List<EventSet> documentData : eventList) {
			Document doc = ps.getAllTrainDocs().get(i);
			Instance instance = createInstance(attributes, relevantEvents, cfd,
					documentData, doc, isSparse, useDocTitles);
			instance.setDataset(trainingInstances);
			normInstance(cfd, instance, doc, useDocTitles);
			i++;
			trainingInstances.add(instance);
		}

	}

	public void createTestInstancesThreaded() throws Exception {
		// create the empty Test instances object
		testInstances = new Instances("TestInstances", attributes, 100);

		// generate the test instance objects from the list of list of event
		// sets and add them to the Instances object
		// TODO perhaps parallelize this as well? build a list of Instances
		// objects and then add go through each list (in order) and add the
		// instance objects to Instances?
		boolean found = false;
		for (Document doc : ps.getTestDocs()) {
			found = true;
			List<EventSet> events = extractEventSets(doc, cfd);
			events = cullWithRespectToTraining(relevantEvents, events, cfd);
			Instance instance = createInstance(attributes, relevantEvents, cfd,
					events, doc, isSparse, useDocTitles);
			instance.setDataset(testInstances);
			normInstance(cfd, instance, doc, useDocTitles);
			testInstances.add(instance);
		}

		// if there are no test documents, set the Instances object to null
		if (!found)
			testInstances = null;
	}

	// Thread Definitions

	public class FeatureExtractionThread extends Thread {

		ArrayList<List<EventSet>> list = new ArrayList<List<EventSet>>();
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
					* (threadId + 1)); i++)
				try {
					list.add(cfd.createEventSets(ps.getAllTrainDocs().get(i)));
				} catch (Exception e) {
					Logger.logln("Error extracting features!", LogOut.STDERR);
					Logger.logln(e.getMessage(), LogOut.STDERR);
				}
		}
	}

	
	//TODO add new thread definitions for test and training instances creation
	
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
	 * @param nct number of calculation threads to use.
	 */
	public void setNumThreads(int nct)
	{
		numThreads = nct;
		
		File jProps = new File("./jsan_resources/JStylo_prop.prop");
		if (jProps.exists()){ //write numCalcThreads to the file
	
			try {
				ArrayList<String> contents = new ArrayList<String>();
				FileReader fileReader = new FileReader(jProps);
				BufferedReader reader = new BufferedReader(fileReader);
				
				//read the file into memory and update the numCalcThreads variable
				String nextLine = reader.readLine();
				while (nextLine!=null){
					String temp = nextLine;
					
					if (temp.contains("numCalcThreads")){
						temp="numCalcThreads="+numThreads;
					}
					contents.add(temp);
					nextLine = reader.readLine();
				}
				reader.close();
				fileReader.close();
				//Write to the file
				FileWriter cleaner = new FileWriter(jProps,false);
				cleaner.write("");
				cleaner.close();
				
				FileWriter writer = new FileWriter(jProps,true);
				for(String s:contents){
					writer.write(s+"\n");
				}
				writer.close();
				
			} catch (FileNotFoundException e) {
				Logger.logln("Failed to read properties file! numCalcThreads defaulting to 1! Generating new prop file...",Logger.LogOut.STDERR);
				e.printStackTrace();
				numThreads=1;
			} catch (IOException e) {
				Logger.logln("Prop file empty! numCalcThreads defaulting to 1! Generating new prop file...",Logger.LogOut.STDERR);
				e.printStackTrace();
				numThreads=1;
			}
		} else {
			numThreads=1;
		}
	}

	public int getNumThreads() {
		return numThreads;
	}

	public boolean isSparse(){
		return isSparse;
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
