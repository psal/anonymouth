package edu.drexel.psal.jstylo.generics;

import weka.attributeSelection.InfoGainAttributeEval;
import weka.core.*;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVSaver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import com.jgaap.generics.*;

import edu.drexel.psal.anonymouth.gooie.GUIMain;
import edu.drexel.psal.anonymouth.gooie.ThePresident;
import edu.drexel.psal.jstylo.eventDrivers.CharCounterEventDriver;
import edu.drexel.psal.jstylo.eventDrivers.LetterCounterEventDriver;
import edu.drexel.psal.jstylo.eventDrivers.SentenceCounterEventDriver;
import edu.drexel.psal.jstylo.eventDrivers.SingleNumericEventDriver;
import edu.drexel.psal.jstylo.eventDrivers.WordCounterEventDriver;
import edu.drexel.psal.jstylo.generics.Logger.LogOut;

/**
 * The WekaInstancesBuilder class is designed to parse a list of known event sets representing the training corpus
 * and a list of unknown event sets representing a test corpus into a Weka Instances object.
 * It further allows training and classification based on the parsed data using an underlying Weka classifier, and
 * output the parsed Instances object into a standard ARFF file.
 * 
 * @author Ariel Stolerman
 *
 */
public class WekaInstancesBuilder {
	
	public final String NAME = "( WekaInstancesBuilder ) - ";
	
	/**
	 * Determines the number of threads to be used for features extraction.
	 */
	public int numCalcThreads = ThePresident.num_Tagging_Threads;
	
	/**
	 * Determines whether to use a set of SparseInstance or Instance.
	 */
	private boolean isSparse;
	
	/**
	 * Determines whether to include an attribute for document titles.
	 */
	private boolean hasDocTitles;
	
	/**
	 * To hold last used cumulative feature driver.
	 */
	private CumulativeFeatureDriver cfd;

	/**
	 * To hold last used list of known documents.
	 */
	private List<Document> knownDocs;

	/**
	 * To hold last used list of unknown documents.
	 */
	private List<Document> unknownDocs;
	
	/**
	 * To hold last used list of lists of event sets for the known documents. 
	 */
	private List<List<EventSet>> known;
	
	/**
	 * To hold last used list of lists of event sets for the unknown documents.
	 */
	private List<List<EventSet>> unknown;
	
	/**
	 * To hold last used list of authors.
	 */
	private List<String> authors;
	
	/**
	 * To hold last used list of Weka attributes.
	 */
	private FastVector attributeList;
	
	/**
	 * To hold last used list of sets of events, each set corresponds to a feature class (feature driver),
	 * i.e. each event corresponds to a feature in the Weka attribute list. 
	 */
	private List<Set<Event>> allEvents;
	
	/**
	 * To hold last used Weka Instances extracted from the training documents.
	 */
	private Instances trainingSet;
	
	/**
	 * To hold last used Weka Instances extracted from the test documents.
	 */
	private Instances testSet;
		
	/**
	 * To hold last used map of instances to their list of total appearances per feature class (feature driver). 
	 */
	private Map<Instance,int[]> featureClassPerInst;
	
	/**
	 * To hold last used list of total number of appearances of all features in a feature class across all TRAINNING documents.
	 */
	private int[] featureClassAllTrainDocs;
	
	/**
	 * To hold last used map of features to their total number of appearances across all TRAINING documents.
	 */
	private Map<String,Integer> featureAllTrainDocs;
	
	/**
	 * To hold last used list of total number of sentences per document.
	 */
	private Map<Instance,Integer> sentencesPerInst;
	
	/**
	 * To hold last used list of total number of words per document.
	 */
	private Map<Instance,Integer> wordsPerInst;
	
	/**
	 * To hold last used list of total number of characters per document.
	 */
	private Map<Instance,Integer> charsPerInst;
	
	/**
	 * To hold last used list of total number of English letters per document.
	 */
	private Map<Instance,Integer> lettersPerInst;
	
	/**
	 * To hold last used array of opening indices per feature class in the Weka attribute list.
	 * E.g. for feature classes 'digits' and 'letters', with features 0,1,...,9 and a,b,...,z it will hold
	 * [1, 11, 37] -- starts with 1 since the at 0 resides the author name class.
	 */
	private int[] featureClassAttrsFirstIndex;
	
	/**
	 * Used for workaround a Weka sparse representation bug.
	 */
	//private static String dummy = "___";
	
	/**
	 * Whether to use a dummy author name for the test instances.
	 */
	private boolean useDummyAuthor = false;
	
	/* ============
	 * constructors
	 * ============
	 */
	
	/**
	 * Constructor for Weka Instances builder.
	 * @param isSparse
	 * 		Set the representation of the data instances to be sparse (using SparseInstance instead of Instance).
	 */
	public WekaInstancesBuilder(boolean isSparse) {
		this.isSparse = isSparse;
	}
	
	/**
	 * Constructor for Weka Instances builder.
	 * @param isSparse
	 * 		Set the representation of the data instances to be sparse (using SparseInstance instead of Instance).
	 * @param useDummyAuthor
	 * 		Set the test instances author names to use a dummy author name. 
	 */
	public WekaInstancesBuilder(boolean isSparse, boolean useDummyAuthor) {
		this.isSparse = isSparse;
		this.useDummyAuthor = useDummyAuthor;
	}
	
	/* ==========
	 * operations
	 * ==========
	 */
	
	public static class CalcThread extends Thread {
		
		ArrayList<List<EventSet>> list = new ArrayList<List<EventSet>>();
		int div;
		int threadId;
		int knownDocsSize;
		List<Document> knownDocs;
		CumulativeFeatureDriver cfd;
		
		public ArrayList<List<EventSet>> getList() {
			return list;
		}
		
		public CalcThread(int div, int threadId, int knownDocsSize, List<Document> knownDocs, CumulativeFeatureDriver cfd) {
			this.div = div;
			this.threadId = threadId;
			this.knownDocsSize = knownDocsSize;
			this.knownDocs = knownDocs;
			this.cfd = cfd;
		}
		
		@Override
		public void run() {
			for (int i = div * threadId; i < Math.min(knownDocsSize, div * (threadId + 1)); i++) {
				try {
					list.add(cfd.createEventSets(knownDocs.get(i)));
				} catch (Exception e) {
					Logger.logln("Error extracting features!",LogOut.STDERR);
					Logger.logln(e.getMessage(),LogOut.STDERR);
				}
			}
		}
	}
	
	/**
	 * Prepares the training Weka Instances data: using the given cumulative feature driver, it extracts all features
	 * from the training documents, builds the corresponding attribute list and saves the data into trainingSet.
	 * @param knownDocs
	 * 		The list of the training documents.
	 * @param cfd
	 * 		The cumulative feature driver to use.
	 * @throws Exception
	 */
	@SuppressWarnings({ "deprecation", "unchecked", "rawtypes" })
	public void prepareTrainingSet(List<Document> knownDocs, CumulativeFeatureDriver cfd) throws Exception {
		
		int i, j;
		this.cfd = cfd;
		this.knownDocs = knownDocs;	
		
		// create event sets for known documents
		known = new ArrayList<List<EventSet>>(knownDocs.size());
		int knownDocsSize = knownDocs.size();
		
		int numCalcThreadsToUse = ThePresident.num_Tagging_Threads;
		numCalcThreads = numCalcThreadsToUse;
//		numCalcThreads = getNumCalcThreads();
//		
//		if (numCalcThreads>knownDocsSize){
//			numCalcThreadsToUse=knownDocsSize;
//		} else if (numCalcThreads>1){
//			numCalcThreadsToUse=numCalcThreads;
//		}
		
		Logger.logln(NAME+"Using N calc threads: "+numCalcThreadsToUse);
		int div = knownDocsSize / numCalcThreadsToUse;
		
		if ( knownDocsSize % numCalcThreadsToUse !=0 )
			div++;
		CalcThread[] calcThreads = new CalcThread[numCalcThreadsToUse];
		for (int thread = 0; thread < numCalcThreadsToUse; thread++)
			calcThreads[thread] = new CalcThread(
					div,
					thread,
					knownDocsSize,
					knownDocs,
					new CumulativeFeatureDriver(cfd));
		for (int thread = 0; thread < numCalcThreadsToUse; thread++)
			calcThreads[thread].start();
		for (int thread = 0; thread < numCalcThreadsToUse; thread++)
			calcThreads[thread].join();
		for (int thread = 0; thread < numCalcThreadsToUse; thread++)
			known.addAll(calcThreads[thread].list);
		for (int thread = 0; thread < numCalcThreadsToUse; thread++)
			calcThreads[thread] = null;
		calcThreads = null;
		
		//TODO make it so this isn't necessary
		ArrayList<String> IDs = new ArrayList<String>();
		for (EventSet es: known.get(0)){
			IDs.add(es.getEventSetID());
		} 
		// apply event cullers
		known = CumulativeEventCuller.cull(known, cfd);
		//culling gives event sets in wrong order. FIXME
		for (int j1 = 0; j1<known.size(); j1++){
			for (int iterator = 0; iterator<known.get(j1).size();iterator++){
				//	System.out.println("Trying to set "+known.get(j1).get(iterator).getEventSetID()+" to "+IDs.get(iterator));
				known.get(j1).get(iterator).setEventSetID(IDs.get(iterator));
				//	System.out.println("Post ES: "+known.get(j1).get(iterator).getEventSetID());
			} 
		}
		
		// initialize number of sets per document and number of vectors
		final int numOfFeatureClasses = known.get(0).size();
		final int numOfVectors = known.size();
		featureClassAttrsFirstIndex = new int[numOfFeatureClasses+1];
		// initialize author name set
		if (authors == null)
			authors = new LinkedList<String>();
		for (i=0; i<numOfVectors; i++) {
			String author = known.get(i).get(0).getAuthor();
			if (!authors.contains(author))
				authors.add(author);
		}
		Collections.sort(authors);
		//if (isSparse) authors.add(0,dummy);
		if (useDummyAuthor)
			authors.add(0,ProblemSet.getDummyAuthor());
		
		// initialize Weka attributes vector (but authors attribute will be added last)
		attributeList = new FastVector();
		FastVector authorNames = new FastVector();
		for (String name: authors)
			authorNames.addElement(name);
		Attribute authorNameAttribute = new Attribute("authorName", authorNames);
		
		// initialize document title attribute
		if (hasDocTitles)
			attributeList.addElement(new Attribute("title",(FastVector)null));
		
		// initialize list of lists of histograms
		List<List<EventHistogram>> knownEventHists = new ArrayList<List<EventHistogram>>(numOfVectors);
		for (i=0; i<numOfVectors; i++)
			knownEventHists.add(new ArrayList<EventHistogram>(numOfFeatureClasses));
		
		// initialize list of sets of events, which will eventually become the attributes
		allEvents = new ArrayList<Set<Event>>(numOfFeatureClasses);
			
		// collect all histograms for all event sets for all TRAINING documents
		// and update Weka attribute list
		List<EventSet> list;
		List<EventHistogram> histograms;
		for (int currEventSet=0; currEventSet<numOfFeatureClasses; currEventSet++) {
			// initialize relevant list of event sets and histograms
			list = new ArrayList<EventSet>();
			for (i=0; i<numOfVectors; i++)
				list.add(known.get(i).get(currEventSet));
			histograms = new ArrayList<EventHistogram>();
			
			Set<Event> events = new HashSet<Event>();
			
			if (cfd.featureDriverAt(currEventSet).isCalcHist()) {	// calculate histogram
			
				// generate event histograms and unique event list
				for (EventSet eventSet : list) {
					EventHistogram currHist = new EventHistogram();
					for (Event event : eventSet) {
						events.add(event);
						currHist.add(event);
					}
					histograms.add(currHist);
					allEvents.add(currEventSet,events);
				}
				
				// create attribute for each unique event
				
				int count = 0;
				for (Event event : events)
					attributeList.addElement(new Attribute(event.getEvent().replaceFirst("\\{", "-"+(++count)+"{")));
				
				// update histograms
				for (i=0; i<numOfVectors; i++)
					knownEventHists.get(i).add(currEventSet,histograms.get(i));
				
			} else {	// one unique numeric event
				
				// generate sole event (extract full event name and remove value)
				Event event = new Event(list.get(0).eventAt(0).getEvent().replaceAll("\\{.*\\}", "{-}"));
				events.add(event);
				allEvents.add(currEventSet,events);
				
				// create attribute for sole unique event
				attributeList.addElement(new Attribute(event.getEvent()));
				
				// update histogram to null at current position
				for (i=0; i<numOfVectors; i++)
					knownEventHists.get(i).add(currEventSet,null);
			}			
		}
		
		// add authors attribute as last attribute
		attributeList.addElement(authorNameAttribute);
		
		// initialize training Weka Instances object with authorName as class
		trainingSet = new Instances("JStylo", attributeList, numOfVectors);
		trainingSet.setClass(authorNameAttribute);
		
		// initialize vector size (including authorName and title if required) and first indices of feature classes array
		int vectorSize = (hasDocTitles ? 1 : 0);
		for (i=0; i<numOfFeatureClasses; i++) {
			featureClassAttrsFirstIndex[i] = vectorSize;
			vectorSize += allEvents.get(i).size();
		}
		featureClassAttrsFirstIndex[i] = vectorSize;
		vectorSize += 1; // one more for authorName
		
		// handle sparse instances removing first values of string attributes
		if (hasDocTitles && isSparse)
			trainingSet.attribute(0).addStringValue("_dummy_");
		
		// generate training instances
		Instance inst;
		for (i=0; i<numOfVectors; i++) {
			// initialize instance
			if (isSparse) inst = new SparseInstance(vectorSize);
			else inst = new DenseInstance(vectorSize);
			
			// update document title
			if (hasDocTitles)
				inst.setValue((Attribute) attributeList.elementAt(0), knownDocs.get(i).getTitle());
			
			// update values
			int index = (hasDocTitles ? 1 : 0);
			for (j=0; j<numOfFeatureClasses; j++) {
				Set<Event> events = allEvents.get(j);
				
				if (cfd.featureDriverAt(j).isCalcHist()) {
					
					// extract absolute frequency from histogram
					EventHistogram currHist = knownEventHists.get(i).get(j);
					for (Event e: events) {
						inst.setValue(
								(Attribute) attributeList.elementAt(index++),
								currHist.getAbsoluteFrequency(e));				// use absolute values, normalize later
					}
				} else {
					
					// extract numeric value from original sole event
					double value = Double.parseDouble(known.get(i).get(j).eventAt(0).toString().replaceAll(".*\\{", "").replaceAll("\\}", ""));
					inst.setValue(
							(Attribute) attributeList.elementAt(index++),
							value);	
				}
			}
			
			// update author
			inst.setValue((Attribute) attributeList.lastElement(), knownDocs.get(i).getAuthor());
			
			trainingSet.add(inst);
		}
		
		// normalization
		initNormTrainBaselines();
		normInstances(trainingSet);
	}
	
	
	/**
	 * Applies InfoGain (Weka) on the training set and returns a description of the results:
	 * List of attributes sorted by weights and feature-type breakdown. 
	 * @param changeAttributes Whether to change the number of attributes in the training set
	 * according to the results of the InfoGain.
	 * @param N The number of most effecting attributes to reduce the list to.
	 * @throws Exception
	 */
	public String applyInfoGain(boolean changeAttributes, int N) throws Exception {
		String res = "";
		int len = 0;
		int n = trainingSet.numAttributes();
		
		// apply InfoGain
		InfoGainAttributeEval ig = new InfoGainAttributeEval();
		ig.buildEvaluator(trainingSet);
		
		// extract and sort attributes by InfoGain
		double[][] infoArr = new double[n-1][2];
		int j = 0;
		for (int i=0; i<infoArr.length; i++) {
			if (trainingSet.attribute(j).name().equals("authorName")) {
				i--;
			} else {
				len = (len > trainingSet.attribute(j).name().length() ? len : trainingSet.attribute(j).name().length());
				infoArr[i][0] = ig.evaluateAttribute(j);
				infoArr[i][1] = j;
			}
			j++;
		}
		Arrays.sort(infoArr, new Comparator<double[]>(){
			@Override
			public int compare(final double[] first, final double[] second){
				return -1*((Double)first[0]).compareTo(((Double)second[0]));
			}
		});
		
		// add InfoGain results to result string
		res += 	"Features InfoGain score (non-zero only):\n" +
				"----------------------------------------\n";
		for (int i=0; i<n-1; i++) {
			if (infoArr[i][0] == 0)
				break;
			res += String.format("> %-"+len+"s   %f\n", trainingSet.attribute((int)infoArr[i][1]).name(), infoArr[i][0]);
		}
		res += "\n";
		
		// calculate and add feature-type breakdown to result string
		res +=	"Feature-type breakdown:\n" +
				"-----------------------\n";
		len = 0;
		final Map<String,Double> featureTypeBreakdown = new HashMap<String,Double>();
		double total = 0;
		String attrName;
		for (int i=0; i<n-1; i++) {
			attrName = trainingSet.attribute((int)infoArr[i][1]).name().replaceFirst("(-\\d+)?\\{.*\\}", "");
			if (featureTypeBreakdown.get(attrName) == null) {
				featureTypeBreakdown.put(attrName, infoArr[i][0]);
				if (len < attrName.length())
					len = attrName.length();
			} else {
				featureTypeBreakdown.put(attrName, featureTypeBreakdown.get(attrName)+infoArr[i][0]);
			}
			total += infoArr[i][0];
		}
		List<String> attrListBreakdown = new ArrayList<String>(featureTypeBreakdown.keySet());
		Collections.sort(attrListBreakdown,new Comparator<String>() {
			public int compare(String o1, String o2) {
				return (int) Math.floor(featureTypeBreakdown.get(o2) - featureTypeBreakdown.get(o1));
			}
		});
		for (String attr: attrListBreakdown)
			res += String.format("> %-"+len+"s   %f (%.2f%%)\n", attr, featureTypeBreakdown.get(attr), featureTypeBreakdown.get(attr)*100/total);
		res += "\n";
		
		// remove attributes if necessary
		if (changeAttributes) {
			if (N >= trainingSet.numAttributes() - 1) {
				res += "The number of attributes to reduce to is not less than the current number of documents. Skipping...\n";
				
			} else if (N > 0) { //TD bugfix InfoGain
								//the problem was twofold: 1) the Attributes were only being removed from the trainingSet
															//this meant that testSet didn't line up properly, and caused errors down the line
								//2) the incorrect features were being cut out. I've inclluded a fix--basically this entire chunk was rewritten.
									//should work with any feature set and any N

				//create an array with the value of infoArr's [i][1] this array will be shrunk and modified as needed
				double[] tempArr = new double[infoArr.length];
				for (int i=0; i<infoArr.length;i++){
					tempArr[i]=infoArr[i][1];
				}
				
				//for all the values we need to delete
				for (int i=0; i < infoArr.length-N; i++){
					//remove them from BOTH the trainingSet and testSet
					trainingSet.deleteAttributeAt((int)tempArr[tempArr.length-1]);
					testSet.deleteAttributeAt((int)tempArr[tempArr.length-1]);
					
					//Then shrink the array
					double temp[] = new double[tempArr.length-1];
					for (int k=0; k<temp.length;k++){
						temp[k]=tempArr[k];
					}			
					//AND change the values 
					for (int k=0; k<temp.length;k++){
						if (temp[k]>tempArr[tempArr.length-1]){
							temp[k]=temp[k]-1;
						}
					}						
					//update array
					tempArr=temp;
				
				}
					
				res += "Attributes reduced to top "+N+". The new list of attributes is:\n";
				for (int i=0; i<N; i++) {
					res += trainingSet.attribute(i)+"\n";
				}
				
			} else {
				res += "ERROR! could not apply InfoGain. Check that given value is a positive integer.\n";
			}
		}
		
		return res;
	}

	
	/**
	 * Prepares the test Weka Instances data: it extracts all features from the test documents based on the list
	 * of attributes derived from the training preparation, and saves the data into testSet.
	 * Should be called only after the training Instances data is initialized.
	 * @param unknownDocs
	 * 		The list of the test documents.
	 * @throws Exception
	 */
	@SuppressWarnings({ "deprecation", "unchecked" })
	public void prepareTestSet(List<Document> unknownDocs) throws Exception {
		int i, j;
		this.unknownDocs = unknownDocs;

		// create test documents event sets
		unknown = new ArrayList<List<EventSet>>(unknownDocs.size());
		if (cfd == null)
			prepareTrainingSet(GUIMain.inst.documentProcessor.documentMagician.getTrainSet(), GUIMain.inst.ppAdvancedDriver.cfd);
		for (i=0; i<unknownDocs.size(); i++)
			unknown.add(cfd.createEventSets(unknownDocs.get(i)));

		// initialize number of sets per document and number of vectors
		final int numOfFeatureClasses = unknown.get(0).size();
		final int numOfVectors = unknown.size();

		List<List<EventHistogram>> eventHists = new ArrayList<List<EventHistogram>>(numOfVectors);

		// initialize list of lists of histograms
		for (i=0; i<numOfVectors; i++)
			eventHists.add(new ArrayList<EventHistogram>(numOfFeatureClasses));

		// collect all histograms for all event sets for all TEST documents
		List<EventSet> list;
		List<EventHistogram> histograms;
		for (int currEventSet=0; currEventSet<numOfFeatureClasses; currEventSet++) {
			// initialize relevant list of event sets and histograms
			list = new ArrayList<EventSet>(numOfVectors);
			for (i=0; i<numOfVectors; i++)
					list.add(unknown.get(i).get(currEventSet));
			histograms = new ArrayList<EventHistogram>();

			if (cfd.featureDriverAt(currEventSet).isCalcHist()) {	// calculate histogram

				// generate only event histograms
				for (EventSet eventSet : list) {
					EventHistogram currHist = new EventHistogram();
					for (Event event : eventSet)
						currHist.add(event);
					histograms.add(currHist);
				}
				
				// update histograms
				for (i=0; i<numOfVectors; i++)
					eventHists.get(i).add(currEventSet,histograms.get(i));

			} else {	// one unique numeric event

				// generate sole event (extract full event name and remove value)
				//Event event = new Event(list.get(0).eventAt(0).getEvent().replaceAll("\\{.*\\}", "{-}"));
				
				// update histogram to null at current position
				for (i=0; i<numOfVectors; i++)
					eventHists.get(i).add(currEventSet,null);
			}
		}
		
		// initialize test Weka Instances object with authorName as class
		testSet = new Instances("JStylo", attributeList, numOfVectors);
		testSet.setClassIndex(attributeList.size()-1);

		// initialize vector size (including authorName)
		int vectorSize = testSet.numAttributes();

		// generate instances
		Instance inst;
		for (i=0; i<numOfVectors; i++) {
			// initialize instance
			if (isSparse) inst = new SparseInstance(vectorSize);
			else inst = new DenseInstance(vectorSize);
			
			// update document title
			if (hasDocTitles)
				inst.setValue((Attribute) attributeList.elementAt(0), unknownDocs.get(i).getTitle());

			// update values
			int index = (hasDocTitles ? 1 : 0);
			for (j=0; j<numOfFeatureClasses; j++) {
				Set<Event> events = allEvents.get(j);
				if (cfd.featureDriverAt(j).isCalcHist()) {
					// extract absolute frequency from histogram
					EventHistogram currHist = eventHists.get(i).get(j);
					for (Event e: events) {
						inst.setValue(
								(Attribute) attributeList.elementAt(index++),
								currHist.getAbsoluteFrequency(e));				// use absolute values, normalize later
					}
				} else {

					// extract numeric value from original sole event
					double value = Double.parseDouble(unknown.get(i).get(j).eventAt(0).toString().replaceAll(".*\\{", "").replaceAll("\\}", ""));
					inst.setValue(
							(Attribute) attributeList.elementAt(index++),
							value);	
				}
			}

				// update author
				if (useDummyAuthor) {
					// use dummy author name
					inst.setValue((Attribute) attributeList.lastElement(), ProblemSet.getDummyAuthor());
				} else {
					// set the author name to that of the document, or if not set,
					// to the author of the first training instance
					String name = unknownDocs.get(i).getAuthor();
					if (name == null)
						name = trainingSet.instance(0)
						.stringValue(trainingSet.classAttribute());
					inst.setValue((Attribute) attributeList.lastElement(), name);
				}

				testSet.add(inst);
		}

		// normalization
			initNormTestBaselines();
			normInstances(testSet);
	}
	/*
	 * Reduced version of prepareTestSet. Used to decrease runtime of Anonymouth
	 */
	@SuppressWarnings({ "deprecation", "unchecked" })
	public void prepareTestSetReducedVersion(List<Document> unknownDocs) throws Exception {
		this.unknownDocs = unknownDocs;
		unknown = new ArrayList<List<EventSet>>(1);
		if (cfd == null)
			prepareTrainingSet(GUIMain.inst.documentProcessor.documentMagician.getTrainSet(), GUIMain.inst.ppAdvancedDriver.cfd);
		unknown.add(cfd.createEventSets(unknownDocs.get(0)));
		
		final int numOfFeatureClasses = unknown.get(0).size();
		List<List<EventHistogram>> eventHists = new ArrayList<List<EventHistogram>>(1);
		List<EventHistogram> histograms;
		testSet = new Instances("JStylo", attributeList, 1);
		testSet.setClassIndex(attributeList.size()-1);
		int vectorSize = testSet.numAttributes();
		Instance inst;
		
		eventHists.add(new ArrayList<EventHistogram>(numOfFeatureClasses));
		if (isSparse) inst = new SparseInstance(vectorSize);
		else inst = new DenseInstance(vectorSize);
		int index = (hasDocTitles ? 1 : 0);
		for (int currEventSet=0; currEventSet<numOfFeatureClasses; currEventSet++) {
			if (cfd.featureDriverAt(currEventSet).isCalcHist()) {
				EventHistogram currHist = new EventHistogram();
				histograms = new ArrayList<EventHistogram>();
				EventSet eventSet = unknown.get(0).get(currEventSet);
				for (Event event : eventSet)
					currHist.add(event);
				histograms.add(currHist);
				eventHists.get(0).add(currEventSet,histograms.get(0));
				Set<Event> events = allEvents.get(currEventSet);
				currHist = eventHists.get(0).get(currEventSet);
				for (Event e: events)
					inst.setValue((Attribute) attributeList.elementAt(index++),	currHist.getAbsoluteFrequency(e));			
			} else {
				eventHists.get(0).add(currEventSet,null);
				double value = Double.parseDouble(unknown.get(0).get(currEventSet).eventAt(0).toString().replaceAll(".*\\{", "").replaceAll("\\}", ""));
				inst.setValue((Attribute) attributeList.elementAt(index++),value);	
			}
		}
		testSet.add(inst);
		initNormTestBaselines();
		normInstances(testSet);
	}
	
	
	/**
	 * Initializes the normalization baselines and values for the training documents.
	 * @throws Exception
	 */
	private void initNormTrainBaselines() throws Exception {
		
		int i, j;
		int numOfVectors = trainingSet.numInstances();
		int numOfFeatureClasses = cfd.numOfFeatureDrivers();
		
		for (i=0; i<numOfFeatureClasses; i++) {
			NormBaselineEnum norm = cfd.featureDriverAt(i).getNormBaseline();
			int start = featureClassAttrsFirstIndex[i], end = featureClassAttrsFirstIndex[i+1], k;
			
			if (norm == NormBaselineEnum.FEATURE_CLASS_IN_DOC || norm == NormBaselineEnum.FEATURE_CLASS_ALL_DOCS) {
				// initialize
				if (featureClassPerInst == null)
					featureClassPerInst = new HashMap<Instance, int[]>();
					
				// accumulate feature class sum per document
				for (j=0; j<numOfVectors; j++) {
					int sum = 0;
					Instance inst = trainingSet.instance(j);
					featureClassPerInst.put(inst,new int[numOfFeatureClasses]);
					for (k=start; k<end; k++)
						sum += inst.value(k);
					featureClassPerInst.get(inst)[i] = sum;
				}
				
			} else if (norm == NormBaselineEnum.FEATURE_ALL_DOCUMENTS) {
				// initialize
				if (featureAllTrainDocs == null)
					featureAllTrainDocs = new HashMap<String,Integer>();
				
				// accumulate feature sum across all TRAINING documents for each feature in this feature class
				int sum;
				for (k=start; k<end; k++) {
					sum = 0;
					for (j=0; j<numOfVectors; j++)
						sum += trainingSet.instance(j).value(k);
					featureAllTrainDocs.put(trainingSet.attribute(k).name(), sum);
				}
			
			} else if (norm == NormBaselineEnum.SENTENCES_IN_DOC) {
				// initialize
				if (sentencesPerInst == null)
					sentencesPerInst = new HashMap<Instance,Integer>();
				
				// extract sentence count and update
				Document doc;
				SingleNumericEventDriver counter = new SentenceCounterEventDriver();
				for (j=0; j<numOfVectors; j++) {
					doc = knownDocs.get(j);
					System.out.println(doc);
					doc.load();
					sentencesPerInst.put(trainingSet.instance(j),(int)counter.getValue(doc));
				}
				
				
			} else if (norm == NormBaselineEnum.WORDS_IN_DOC) {
				// initialize
				if (wordsPerInst == null)
					wordsPerInst = new HashMap<Instance,Integer>();
				
				// extract word count and update
				Document doc;
				SingleNumericEventDriver counter = new WordCounterEventDriver();
				for (j=0; j<numOfVectors; j++) {
					doc = knownDocs.get(j);
					doc.load();
					wordsPerInst.put(trainingSet.instance(j),(int)counter.getValue(doc));
				}
				
			} else if (norm == NormBaselineEnum.CHARS_IN_DOC) {
				// initialize
				if (charsPerInst == null)
					charsPerInst = new HashMap<Instance,Integer>();
				
				// extract character count and update
				Document doc;
				SingleNumericEventDriver counter = new CharCounterEventDriver();
				for (j=0; j<numOfVectors; j++) {
					doc = knownDocs.get(j);
					doc.load();
					charsPerInst.put(trainingSet.instance(j),(int)counter.getValue(doc));
				}
			} else if (norm == NormBaselineEnum.LETTERS_IN_DOC) {
				// initialize
				if (lettersPerInst == null)
					lettersPerInst = new HashMap<Instance,Integer>();
				
				// extract letter count and update
				Document doc;
				SingleNumericEventDriver counter = new LetterCounterEventDriver();
				for (j=0; j<numOfVectors; j++) {
					doc = knownDocs.get(j);
					doc.load();
					lettersPerInst.put(trainingSet.instance(j),(int)counter.getValue(doc));
				}
			}
			
			if (norm == NormBaselineEnum.FEATURE_CLASS_ALL_DOCS) {
				// initialize
				if (featureClassAllTrainDocs == null)
					featureClassAllTrainDocs = new int[numOfFeatureClasses];
				
				// accumulate appearances of all features in the feature class, across all TRAINING documents
				for (j=0; j<numOfVectors; j++)
					featureClassAllTrainDocs[i] += featureClassPerInst.get(trainingSet.instance(j))[i];
			}
		}
	}
	
	
	/**
	 * Initializes the normalization values for the test documents.
	 */
	private void initNormTestBaselines() throws Exception {
		int i, j;
		int numOfVectors = testSet.numInstances();
		int numOfFeatureClasses = cfd.numOfFeatureDrivers();
		
		for (i=0; i<numOfFeatureClasses; i++) {
			NormBaselineEnum norm = cfd.featureDriverAt(i).getNormBaseline();
			int start = featureClassAttrsFirstIndex[i], end = featureClassAttrsFirstIndex[i+1], k;
			
			if (norm == NormBaselineEnum.FEATURE_CLASS_IN_DOC || norm == NormBaselineEnum.FEATURE_CLASS_ALL_DOCS) {
				// accumulate feature class sum per document
				for (j=0; j<numOfVectors; j++) {
					int sum = 0;
					Instance inst = testSet.instance(j);
					featureClassPerInst.put(inst,new int[numOfFeatureClasses]);
					for (k=start; k<end; k++)
						sum += inst.value(k);
					featureClassPerInst.get(inst)[i] = sum;
				}
							
			} else if (norm == NormBaselineEnum.SENTENCES_IN_DOC) {
				// extract sentence count and update
				Document doc;
				SingleNumericEventDriver counter = new SentenceCounterEventDriver();
				for (j=0; j<numOfVectors; j++) {
					doc = unknownDocs.get(j);
					doc.load();
					sentencesPerInst.put(testSet.instance(j),(int)counter.getValue(doc));
				}
				
			} else if (norm == NormBaselineEnum.WORDS_IN_DOC) {
				// extract word count and update
				Document doc;
				SingleNumericEventDriver counter = new WordCounterEventDriver();
				for (j=0; j<numOfVectors; j++) {
					doc = unknownDocs.get(j);
					doc.load();
					wordsPerInst.put(testSet.instance(j),(int)counter.getValue(doc));
				}
				
			} else if (norm == NormBaselineEnum.CHARS_IN_DOC) {
				// extract character count and update
				Document doc;
				SingleNumericEventDriver counter = new CharCounterEventDriver();
				for (j=0; j<numOfVectors; j++) {
					doc = unknownDocs.get(j);
					doc.load();
					charsPerInst.put(testSet.instance(j),(int)counter.getValue(doc));
				}
			} else if (norm == NormBaselineEnum.LETTERS_IN_DOC) {
				// extract letter count and update
				Document doc;
				SingleNumericEventDriver counter = new LetterCounterEventDriver();
				for (j=0; j<numOfVectors; j++) {
					doc = unknownDocs.get(j);
					doc.load();
					lettersPerInst.put(testSet.instance(j),(int)counter.getValue(doc));
				}
			}
		}
	}
	
	/**
	 * Applies normalization over the given Weka Instances data.
	 * Should be called after normalization initialization.
	 * @param insts
	 * 		The Weka Instances data to normalize.
	 * @throws Exception
	 */
	private void normInstances(Instances insts) throws Exception {
		
		int i, j;
		int numOfFeatureClasses = cfd.numOfFeatureDrivers();
		int numOfVectors = insts.numInstances();
		
		for (i=0; i<numOfFeatureClasses; i++) {
			NormBaselineEnum norm = cfd.featureDriverAt(i).getNormBaseline();
			double factor = cfd.featureDriverAt(i).getNormFactor();
			int start = featureClassAttrsFirstIndex[i], end = featureClassAttrsFirstIndex[i+1], k;
			Instance currInst;

			if (norm == NormBaselineEnum.FEATURE_CLASS_IN_DOC) {
				// use featureClassPerDoc
				for (j=0; j<numOfVectors; j++) {
					currInst = insts.instance(j);
					for (k=start; k<end; k++)
						currInst.setValue(k, currInst.value(k)*factor/((double)featureClassPerInst.get(currInst)[i]));
				}

			} else if (norm == NormBaselineEnum.FEATURE_CLASS_ALL_DOCS) {
				// use featureClassAllDocs
				for (j=0; j<numOfVectors; j++) {
					currInst = insts.instance(j);
					for (k=start; k<end; k++)
						currInst.setValue(k, currInst.value(k)*factor/((double)featureClassAllTrainDocs[i]));
				}

			} else if (norm == NormBaselineEnum.FEATURE_ALL_DOCUMENTS) {
				// use featureAllDocs
				for (j=0; j<numOfVectors; j++) {
					currInst = insts.instance(j);
					for (k=start; k<end; k++)
						currInst.setValue(k, currInst.value(k)*factor/((double)featureAllTrainDocs.get(insts.attribute(k).name())));
				}

			} else if (norm == NormBaselineEnum.SENTENCES_IN_DOC) {
				// use wordsInDoc
				for (j=0; j<numOfVectors; j++) {
					currInst = insts.instance(j);
					for (k=start; k<end; k++)
						currInst.setValue(k, currInst.value(k)*factor/((double)sentencesPerInst.get(currInst)));
				}

			} else if (norm == NormBaselineEnum.WORDS_IN_DOC) {
				// use wordsInDoc
				for (j=0; j<numOfVectors; j++) {
					currInst = insts.instance(j);
					for (k=start; k<end; k++)
						currInst.setValue(k, currInst.value(k)*factor/((double)wordsPerInst.get(currInst)));
				}

			} else if (norm == NormBaselineEnum.CHARS_IN_DOC) {
				// use charsInDoc
				for (j=0; j<numOfVectors; j++) {
					currInst = insts.instance(j);
					for (k=start; k<end; k++)
						currInst.setValue(k, currInst.value(k)*factor/((double)charsPerInst.get(currInst)));
				}
			}
		}
	}
	
	
	/**
	 * Writes the given Instances set into an ARFF file in the given filename.
	 * @param filename
	 * 		The filename of the ARFF file to create.
	 * @param set
	 * 		The Weka Instances object from which to create an ARFF file.
	 * @return
	 * 		True iff the write succeeded.
	 */
	public static boolean writeSetToARFF(String filename, Instances set) {
		try {
			ArffSaver saver = new ArffSaver();
			 saver.setInstances(set);
			 saver.setFile(new File(filename));
			 saver.writeBatch();
			 return true;
		} catch (IOException ioe) {
			ioe.printStackTrace();
			return false;
		}
	}
	
	
	/**
	 * Writes the given Instances set into an CSV file in the given filename.
	 * @param filename
	 * 		The filename of the CSV file to create.
	 * @param set
	 * 		The Weka Instances object from which to create an ARFF file.
	 * @return
	 * 		True iff the write succeeded.
	 */
	public static boolean writeSetToCSV(String filename, Instances set) {
		try {
			CSVSaver saver = new CSVSaver();
			saver.setInstances(set);
			saver.setFile(new File(filename));
			saver.writeBatch();
			return true;
		} catch (IOException ioe) {
			ioe.printStackTrace();
			return false;
		}
	}
	
	
	/**
	 * Given Weka Instances object, returns the same dataset represented as a Weka sparse dataset.
	 * @param data
	 * 		The data to convert to sparse.
	 * @return
	 * 		The input data represented as a Weka sparse dataset.
	 */
	public static Instances toSparse(Instances data) {
		FastVector attribs = new FastVector(data.numAttributes());
		for (int i=0; i<data.numAttributes(); i++)
			attribs.addElement(data.attribute(i));
		Instances sparse = new Instances(data.relationName(),attribs,data.numInstances());
		sparse.setClass(sparse.attribute(0));
		SparseInstance si;
		for (int i=0; i<data.numInstances(); i++) {
			si = new SparseInstance(data.instance(i));
			sparse.add(si);
		}
		return sparse;
	}
	
	
	/*
	 * =======
	 * setters
	 * =======
	 */
	
	/**
	 * Sets the number of calculation threads to use for feature extraction.
	 * @param numCalcThreads number of calculation threads to use.
	 */
	public void setNumCalcThreads(int nct)
	{
		this.numCalcThreads = nct;
		
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
						temp="numCalcThreads="+numCalcThreads;
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
				numCalcThreads=1;
			} catch (IOException e) {
				Logger.logln("Prop file empty! numCalcThreads defaulting to 1! Generating new prop file...",Logger.LogOut.STDERR);
				e.printStackTrace();
				numCalcThreads=1;
			}
		} else {
			numCalcThreads=1;
		}
	}

	/**
	 * Sets the Instances representation to sparse if given true, and standard otherwise.
	 * @param isSparse
	 * 		Whether to set the Instances representation to sparse.
	 */
	public void setSparse(boolean isSparse) {
		this.isSparse = isSparse;
	}
	
	public void setTrainingSet(Instances trainingSet) {
		this.trainingSet = trainingSet;
	}
	
	public void setTestSet(Instances testSet) {
		this.testSet = testSet;
	}	
	
	/*
	 * =======
	 * getters
	 * =======
	 */
	
	/**
	 * @return the number of calculation threads to use for feature extraction.
	 */
	public int getNumCalcThreads()
	{	
		return numCalcThreads;
	}
	

	
	/**
	 * Returns true if the Instances representation is sparse, and false otherwise.
	 * @return
	 * 		True if the Instances representation is sparse, and false otherwise.
	 */
	public boolean isSparse() {
		return isSparse;
	}

	/**
	 * Returns the list of lists of event sets extracted from the known documents in the last call to prepare().
	 * @return
	 * 		The list of lists of event sets extracted from the known documents in the last call to prepare().
	 */
	public List<List<EventSet>> getKnown() {
		return known;
	}
	
	public FastVector getAttributeList() {
		return attributeList;
	}

	/**
	 * Returns the list of lists of event sets extracted from the unknown documents in the last call to prepare().
	 * @return
	 * 		The list of lists of event sets extracted from the unknown documents in the last call to prepare().
	 */
	public List<List<EventSet>> getUnknown() {
		return unknown;
	}

	/**
	 * Returns the list of authors extracted in the last call to prepare().
	 * @return
	 * 		The list of authors extracted in the last call to prepare().
	 */
	public List<String> getAuthors() {
		return authors;
	}

	/**
	 * Returns the Weka Instances object that represents the training set extracted in the last call to prepare().
	 * @return
	 * 		The Weka Instances object that represents the training set extracted in the last call to prepare().
	 */
	public Instances getTrainingSet() {
		return trainingSet;
	}

	/**
	 * Returns the Weka Instances object that represents the test set extracted in the last call to prepare().
	 * @return
	 * 		The Weka Instances object that represents the test set extracted in the last call to prepare().
	 */
	public Instances getTestSet() {
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
	 * Returns true iff it is set to include document titles as an additional attribute.
	 * @return
	 * 		True iff it is set to include document titles as an additional attribute.
	 */
	public boolean hasDocNames() {
		return hasDocTitles;
	}

	/**
	 * Set whether to include document titles as an additional attribute.
	 * @param hasDocNames
	 * 		Indicates whether to include document titles.
	 */
	public void setHasDocNames(boolean hasDocNames) {
		this.hasDocTitles = hasDocNames;
	}
	
	/**
	 * Returns the dummy author name used to bypass Weka bug when using sparse representation.
	 * @return
	 * 		The dummy author name used to bypass Weka bug when using sparse representation.
	 */
	/*
	public static String getDummy() {
		return dummy;
	}
	*/
	
	public void addAuthor(String author)
	{
		if (authors == null)
			authors = new LinkedList<String>();
		if (!authors.contains(author))
			authors.add(author);
	}
}