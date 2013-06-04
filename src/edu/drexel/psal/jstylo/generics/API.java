package edu.drexel.psal.jstylo.generics;

import java.util.ArrayList;
import java.util.List;

import weka.core.*;

import com.jgaap.generics.*;


public interface API {

	/*
	
	public void prepareTrainingSet(List<Document> knownDocs, CumulativeFeatureDriver cfd)
	public void prepareTestSet(List<Document> unknownDocs)
	public String applyInfoGain(boolean changeAttributes, int N)
	private void normInstances(Instances insts)
	
	 */
	
	//Note for version 2
		//try to reduce amount of data being passed around
		//ex by creating a method (or adding the functionality to a current method) to strip 
			//normalization baselines from a document so that the doc doesn't need to be carried
	
	// feature extraction - training set
	
	/**
	 * Extracts the List of EventSets from a document using the provided CumulativeFeatureDriver
	 * @param document the document to have features extracted and made into event sets
	 * @param cumulativeFeatureDriver the driver containing the features to be extracted and the functionality to do so
	 * @return the List\<EventSet\> for the document
	 */ 
	public List<EventSet> extractEventSets(Document document,
			CumulativeFeatureDriver cumulativeFeatureDriver) throws Exception;
	
	/**
	 * Determines which EventSets to use for the given documents
	 * @param eventSets A List which contains Lists of EventSets (represents a list of documents' EventSets0
	 * @param cumulativeFeatureDriver the driver with the culling functionality
	 * @return The culled List\<List\<EventSet\>\> created from eventSets
	 * @throws Exception
	 */
	public List<List<EventSet>> cull(List<List<EventSet>> eventSets,
			CumulativeFeatureDriver cumulativeFeatureDriver) throws Exception;
	
	/**
	 * Goes over the culled List<List<EventSet>> and determines which events are histograms and which have a single<br>
	 * numerical value. Uses the information to prepare a List\<EventSet\> to extract from the test document(s)
	 * @param culledEventSets The culled List<List<EventSet>>
	 * @param cumulativeFeatureDriver The driver used to extract the EventSets
	 * @return The List\<EventSet\> to extract from the test document(s) 
	 * @throws Exception
	 */
	public List<EventSet> getRelevantEvents(List<List<EventSet>> culledEventSets,
			CumulativeFeatureDriver cumulativeFeatureDriver) throws Exception;
	//Any time there is a single numeric value, use "null"
	
	/**
	 * Generates the List\<Attributes\> from the List\<List\<EventSet\>\> that will be used to create the Instances object.
	 * @param culledEventSets The culled list of EventSets that have been gathered from the document set
	 * @return A List\<Attribute\> which will be used to create the Instances object 
	 * @throws Exception
	 */
	public ArrayList<Attribute> getAttributeList(
			List<List<EventSet>> culledEventSets, List<EventSet> relevantEvents, CumulativeFeatureDriver cfd, boolean hasDocTitles) throws Exception;
	
	/**
	 * Takes various parameters related to a document and creates an Instance object from that data.
	 * @param attributes the data used to construct the Instance object
	 * @param cumulativeFeatureDriver driver used to determine the type of feature being added
	 * @param documentData used to determine the values to assign each attribute
	 * @return The instance object representing this document
	 * @throws Exception
	 */
	public Instance createInstance(List<Attribute> attributes,
			List<EventSet> relevantEvents,
			CumulativeFeatureDriver cumulativeFeatureDriver,
			List<EventSet> documentData,
			Document document, 
			boolean isSparse, boolean hasDocTitles) throws Exception;
	//create and use histograms here
	
	/**
	 * Normalizes all of the features of the specified instance.
	 * Does not support global normalization baselines!
	 * @param cumulativeFeatureDriver the driver used to tell what to normalize and in what fashion. 
	 * @param instance the object to be normalized
	 * @throws Exception
	 */
	public void normInstance(CumulativeFeatureDriver cumulativeFeatureDriver,
			Instance instance, Document document, boolean hasDocTitles) throws Exception;
	
	// the full training Instances object is generated
	
	/**
	 * Calculates InfoGain on the instances to provide information on how useful each feature was to identifying the documents.
	 * returns list of indices of the top N features
	 * @param insts the instances to calculate over
	 * @param N The top N features to calculate the infogain over
	 * @return a list of the top N features
	 * @throws Exception
	 */
	public List<Integer> calcInfoGain(Instances insts, int N) throws Exception;
	
	/**
	 * Removes all but the top N features (as returned by calcInfoGain) from the instances
	 * @param chosenFeatures the indices of the features to keep
	 * @param insts the instances to remove infoGain from
	 * @throws Exception
	 */
	public void applyInfoGain(List<Integer> chosenFeatures, Instances insts)
			throws Exception;
	
	
	// feature extraction - test set
	
	// extractEventSets - same as for training documents
	
	/**
	 * Culls the test set using the List\<EventSet\> of the training set
	 * @param relevantEvents the features from the EventSets which are going to be evaluated
	 * @param eventSetsToCull The test documents to be culled
	 * @return the culled test documents
	 * @throws Exception
	 */
	public List<EventSet> cullWithRespectToTraining(
			List<EventSet> relevantEvents, List<EventSet> eventSetsToCull,
			CumulativeFeatureDriver cumulativeFeatureDriver)
			throws Exception;
	
	// createInstance - same as for training documents
	
	// normInstance - same as for training documents
	
	// applyInfoGain - same as for training documents
}