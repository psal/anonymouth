package edu.drexel.psal.jstylo.generics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import weka.attributeSelection.InfoGainAttributeEval;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;

import com.jgaap.generics.Document;
import com.jgaap.generics.Event;
import com.jgaap.generics.EventHistogram;
import com.jgaap.generics.EventSet;

import edu.drexel.psal.jstylo.eventDrivers.CharCounterEventDriver;
import edu.drexel.psal.jstylo.eventDrivers.LetterCounterEventDriver;
import edu.drexel.psal.jstylo.eventDrivers.SentenceCounterEventDriver;
import edu.drexel.psal.jstylo.eventDrivers.SingleNumericEventDriver;
import edu.drexel.psal.jstylo.eventDrivers.WordCounterEventDriver;

public class Engine implements API {

	//PassDocuments
	//hasDocTitles
	
	//Done
	@Override
	public List<EventSet> extractEventSets(Document document,
			CumulativeFeatureDriver cumulativeFeatureDriver) throws Exception {	
		return cumulativeFeatureDriver.createEventSets(document);
	}

	//Done
	@Override
	public List<List<EventSet>> cull(List<List<EventSet>> eventSets,
			CumulativeFeatureDriver cumulativeFeatureDriver) throws Exception {	
		return CumulativeEventCuller.cull(eventSets,cumulativeFeatureDriver);
	}

	//TODO this functionality was overlooked in the original wib, as the relevant events were just scraped from the first document
	//need to implement functionality for the first time.
	//Consider adding more detailed IDs in EventSet so as to make life easier here.
	//Doing this could allow eventSets to be matched even if their contents do not match.
	@Override
	public List<EventSet> getRelevantEvents( 
			List<List<EventSet>> culledEventSets,
			CumulativeFeatureDriver cumulativeFeatureDriver) throws Exception {
		
		List<EventSet> relevantEvents = new LinkedList<EventSet>();
		
		//iterate over the List of Lists
		for (List<EventSet> l: culledEventSets){
			//iterate over each inner list's eventSets
			for (EventSet es: l){
				
				boolean add = true;
				
				for (EventSet esl:relevantEvents){
					//eventSet check. If true
						//added equals false
						//break
					//else
						//do nothing
				}
				
				if (add){
					relevantEvents.add(es);
				} else {
					//go through this eventSet and add any events to the relevant EventSet if they aren't already there.
				}
				
				//TODO figure out how to match EventSets
				
				//it looks like Event s themselves can be matched via string matching and Event.getEvent()
				
			}
		}
		return relevantEvents;
	}

//	@Override //FIXME Had to add in a cfd in order to check if a feature was a histogram or not
	//also, it is currently taking the number of feature classes from the first List<EventSet>
	//I believe it was mentioned that this shouldn't be the case, should I give it another parameter: relevantEvents? or...?
	@Override
	public List<Attribute> getAttributeList(List<List<EventSet>> culledEventSets, CumulativeFeatureDriver cumulativeFeatureDriver)
			throws Exception {

		//FIXME this line is incorrect, but is what we are currently using elsewhere.
		//essentially, it uses the first List<EventSet> to determine the size of the relevant EventSet list
		//this shouldn't always be the case: the first list might be missing EventSets which other documents have
		//which are more popular ie a specific word or bigram. Change this once everything else is looking good.
		int numOfFeatureClasses = culledEventSets.get(0).size();
		
		int numOfVectors = culledEventSets.size();
		List<EventSet> list;
		List<EventHistogram> histograms;
		
		// initialize author name set
		LinkedList<String> authors = new LinkedList<String>();
		for (int i=0; i<numOfVectors; i++) {
			String author = culledEventSets.get(i).get(0).getAuthor();
			if (!authors.contains(author))
				authors.add(author);
		}
		Collections.sort(authors);
		
		// initialize Weka attributes vector (but authors attribute will be added last)
		FastVector attributeList = new FastVector();
		FastVector authorNames = new FastVector();
		for (String name: authors)
			authorNames.addElement(name);
		Attribute authorNameAttribute = new Attribute("authorName", authorNames);
		
		// initialize list of lists of histograms
		List<List<EventHistogram>> knownEventHists = new ArrayList<List<EventHistogram>>(numOfVectors);
		for (int i=0; i<numOfVectors; i++)
			knownEventHists.add(new ArrayList<EventHistogram>(numOfFeatureClasses));
		
		// initialize list of sets of events, which will eventually become the attributes
		List<Set<Event>> allEvents = new ArrayList<Set<Event>>(numOfFeatureClasses);
		
		for (int currEventSet=0; currEventSet<numOfFeatureClasses; currEventSet++) {
			// initialize relevant list of event sets and histograms

			list = new ArrayList<EventSet>(numOfVectors);
			for (int i=0; i<numOfVectors; i++)
				list.add(culledEventSets.get(i).get(currEventSet));
			histograms = new ArrayList<EventHistogram>();
			
			Set<Event> events = new HashSet<Event>();
			
			if (cumulativeFeatureDriver.featureDriverAt(currEventSet).isCalcHist()) {	// calculate histogram
			
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
				
				// update histograms
				for (int i=0; i<numOfVectors; i++)
					knownEventHists.get(i).add(currEventSet,histograms.get(i));
				
			} else {	// one unique numeric event
				
				// generate sole event (extract full event name and remove value)
				Event event = new Event(list.get(0).eventAt(0).getEvent().replaceAll("\\{.*\\}", "{-}"));
				events.add(event);
				allEvents.add(currEventSet,events);
				
				// update histogram to null at current position
				for (int i=0; i<numOfVectors; i++)
					knownEventHists.get(i).add(currEventSet,null);
			}
			// add authors attribute as last attribute
			attributeList.addElement(authorNameAttribute);
		}
		
		LinkedList<Attribute> attributes = new LinkedList<Attribute>();
		
		for (int i=0; i<attributeList.size();i++){
			attributes.add((Attribute) attributeList.elementAt(i));
		}
		
		return attributes;
	}

//	@Override //FIXME had to add a "hasDocTitles" parameter
	//Also, there are two references to a "Document" type parameter, with the goal of adding the author.
	//This sounds rather necessary, so perhaps it would be best to pass through the original document as well?
	public Instance createInstance(List<Attribute> attributes,
			CumulativeFeatureDriver cumulativeFeatureDriver,
			List<EventSet> documentData, boolean isSparse, boolean hasDocTitles) throws Exception {
			
		int numOfFeatureClasses = documentData.size();
		// initialize vector size (including authorName and title if required) and first indices of feature classes array
		int vectorSize = (hasDocTitles ? 1 : 0);
		for (int i=0; i<numOfFeatureClasses; i++) {
			vectorSize += documentData.size();
		}
		vectorSize += 1; // one more for authorName
		
		// generate training instances
		Instance inst = null;
		int numOfVectors = documentData.size();
		
		for (int i=0; i<numOfVectors; i++) {
			// initialize instance
			if (isSparse) inst = new SparseInstance(vectorSize);
			else inst = new Instance(vectorSize);
			
			// update document title FIXME either add this back in if its necessary or delete it if it isn't
			//the problem: we'd need a "Document" type arg to pass it, which we don't have.
			/*if (hasDocTitles)
				inst.setValue((Attribute) attributes.get(0), documentData.getTitle());
			*/	
			
				// update values
			int index = (hasDocTitles ? 1 : 0);
			for (int j=0; j<numOfFeatureClasses; j++) {

				//TODO I think this chunk will correctly grab all events and histograms from the document
				Set<Event> events = new HashSet<Event>();
				EventHistogram currHist = new EventHistogram();
				if (cumulativeFeatureDriver.featureDriverAt(j).isCalcHist()) {	// calculate histogram
					// generate event histograms and unique event list			
					for (i=0; i<documentData.size();i++) {
						for (Event event: documentData.get(i)){
							events.add(event);
							currHist.add(event);
						}
					}				
				}
				
				if (cumulativeFeatureDriver.featureDriverAt(j).isCalcHist()) {
					
					// extract absolute frequency from histogram
					for (Event e: events) {
						inst.setValue(
							(Attribute) attributes.get(index++),
							currHist.getAbsoluteFrequency(e));				// use absolute values, normalize later
					}
				} else {
					
					// extract numeric value from original sole event
					double value = Double.parseDouble(documentData.get(j).eventAt(0).toString().replaceAll(".*\\{", "").replaceAll("\\}", ""));
					inst.setValue(
							(Attribute) attributes.get(index++),
							value);	
				}
			}
				
			// update author FIXME find a way to fix if it is needed delete if not
			//inst.setValue((Attribute) attributes.get(attributes.size()-1), documentData.getAuthor());
				
		}
		return inst;
	}

//	@Override //FIXME needed to add document parameter so that we can read the document to get normalization baselines
	//Also, there's a "hasDocTitle" parameter which is used to initialize vector size. Either the parameter needs to be added or...?
	public void normInstance(CumulativeFeatureDriver cfd,
			Instance instance, Document document) throws Exception {

		int i, j;
		int numOfFeatureClasses = cfd.numOfFeatureDrivers();

		HashMap<Instance, int[]> featureClassPerInst = null;
		HashMap<Instance,Integer> sentencesPerInst =null;
		HashMap<Instance,Integer> wordsPerInst=null;
		HashMap<Instance,Integer> charsPerInst=null;
		HashMap<Instance,Integer> lettersPerInst=null;
		int[] featureClassAttrsFirstIndex = new int[numOfFeatureClasses+1];
		
		
		// initialize vector size (including authorName and title if required) and first indices of feature classes array
		//int vectorSize = (hasDocTitles ? 1 : 0);
		int vectorSize=0; //TODO figure out some way to load this like above. Perhaps add the hasDocTitle param to method
		for (i=0; i<numOfFeatureClasses; i++) {
			featureClassAttrsFirstIndex[i] = vectorSize;
			vectorSize += instance.attribute(i).numValues(); //not sure if this is the right thing to be counting or not
		}
		featureClassAttrsFirstIndex[i] = vectorSize;
		vectorSize += 1; // one more for authorName

		//initialize normalization baselines
		for (i=0; i<numOfFeatureClasses; i++) {
			NormBaselineEnum norm = cfd.featureDriverAt(i).getNormBaseline();
			int start = featureClassAttrsFirstIndex[i], end = featureClassAttrsFirstIndex[i+1], k;
					
			if (norm == NormBaselineEnum.FEATURE_CLASS_IN_DOC || norm == NormBaselineEnum.FEATURE_CLASS_ALL_DOCS) {
				// initialize
				if (featureClassPerInst == null)
					featureClassPerInst = new HashMap<Instance, int[]>();
					
				// accumulate feature class sum per document

					int sum = 0;
					featureClassPerInst.put(instance,new int[numOfFeatureClasses]);
					for (k=start; k<end; k++)
						sum += instance.value(k);
					featureClassPerInst.get(instance)[i] = sum;
				
				
			} else if (norm == NormBaselineEnum.SENTENCES_IN_DOC) {
				// initialize
				if (sentencesPerInst == null)
					sentencesPerInst = new HashMap<Instance,Integer>();
				
				// extract sentence count and update
				Document doc;
				SingleNumericEventDriver counter = new SentenceCounterEventDriver();
				
				doc = document;
				doc.load();
				sentencesPerInst.put(instance,(int)counter.getValue(doc));
				
				
			} else if (norm == NormBaselineEnum.WORDS_IN_DOC) {
				// initialize
				if (wordsPerInst == null)
					wordsPerInst = new HashMap<Instance,Integer>();
				
				// extract word count and update
				Document doc;
				SingleNumericEventDriver counter = new WordCounterEventDriver();
				doc = document;
				doc.load();
				wordsPerInst.put(instance,(int)counter.getValue(doc));
				
			} else if (norm == NormBaselineEnum.CHARS_IN_DOC) {
				// initialize
				if (charsPerInst == null)
					charsPerInst = new HashMap<Instance,Integer>();
				
				// extract character count and update
				Document doc;
				SingleNumericEventDriver counter = new CharCounterEventDriver();
				doc = document;
				doc.load();
				charsPerInst.put(instance,(int)counter.getValue(doc));
		
			} else if (norm == NormBaselineEnum.LETTERS_IN_DOC) {
				// initialize
				if (lettersPerInst == null)
					lettersPerInst = new HashMap<Instance,Integer>();
				
				// extract letter count and update
				Document doc;
				SingleNumericEventDriver counter = new LetterCounterEventDriver();
					doc = document;
					doc.load();
					lettersPerInst.put(instance,(int)counter.getValue(doc));
			}
		}
		
		//normalizes features
		for (i=0; i<numOfFeatureClasses; i++) {
			NormBaselineEnum norm = cfd.featureDriverAt(i).getNormBaseline();
			double factor = cfd.featureDriverAt(i).getNormFactor();
			int start = featureClassAttrsFirstIndex[i], end = featureClassAttrsFirstIndex[i+1], k;

			if (norm == NormBaselineEnum.FEATURE_CLASS_IN_DOC) {
				// use featureClassPerDoc
				for (k=start; k<end; k++)
						instance.setValue(k, instance.value(k)*factor/((double)featureClassPerInst.get(instance)[i]));
				
			}  else if (norm == NormBaselineEnum.SENTENCES_IN_DOC) {
				// use wordsInDoc
					for (k=start; k<end; k++)
						instance.setValue(k, instance.value(k)*factor/((double)sentencesPerInst.get(instance)));

			} else if (norm == NormBaselineEnum.WORDS_IN_DOC) {
				// use wordsInDoc
					for (k=start; k<end; k++)
						instance.setValue(k, instance.value(k)*factor/((double)wordsPerInst.get(instance)));

			} else if (norm == NormBaselineEnum.CHARS_IN_DOC) {
				// use charsInDoc
					for (k=start; k<end; k++)
						instance.setValue(k, instance.value(k)*factor/((double)charsPerInst.get(instance)));
			}
		}
	}

	//Done
	@Override 
	public List<Integer> calcInfoGain(Instances insts, int N) throws Exception {
		int len = 0;
		int n = insts.numAttributes();
		List<Integer> indicesToKeep = new LinkedList<Integer>();
		
		// apply InfoGain
		InfoGainAttributeEval ig = new InfoGainAttributeEval();
		ig.buildEvaluator(insts);
		
		// extract and sort attributes by InfoGain
		double[][] infoArr = new double[n-1][2];
		int j = 0;
		for (int i=0; i<infoArr.length; i++) {
			if (insts.attribute(j).name().equals("authorName")) {
				i--;
			} else {
				len = (len > insts.attribute(j).name().length() ? len : insts.attribute(j).name().length());
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
		
		len = 0;
		final Map<String,Double> featureTypeBreakdown = new HashMap<String,Double>();
		String attrName;
		for (int i=0; i<n-1; i++) {
			attrName = insts.attribute((int)infoArr[i][1]).name().replaceFirst("(-\\d+)?\\{.*\\}", "");
			if (featureTypeBreakdown.get(attrName) == null) {
				featureTypeBreakdown.put(attrName, infoArr[i][0]);
				if (len < attrName.length())
					len = attrName.length();
			} else {
				featureTypeBreakdown.put(attrName, featureTypeBreakdown.get(attrName)+infoArr[i][0]);
			}
		}
		List<String> attrListBreakdown = new ArrayList<String>(featureTypeBreakdown.keySet());
		Collections.sort(attrListBreakdown,new Comparator<String>() {
			public int compare(String o1, String o2) {
				return (int) Math.floor(featureTypeBreakdown.get(o2) - featureTypeBreakdown.get(o1));
			}
		});
		
		//create an array with the value of infoArr's [i][1] this array will be shrunk and modified as needed
		double[] tempArr = new double[infoArr.length];
		for (int i=0; i<infoArr.length;i++){
			tempArr[i]=infoArr[i][1];
			indicesToKeep.add((int)infoArr[i][1]);
		}
		
		//for all the values we need to delete
		for (int i=0; i < infoArr.length-N; i++){
			
			//remove the value
			indicesToKeep.remove((int)tempArr[tempArr.length-1]);
			
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
		
		return indicesToKeep;
	}

	//verify that this is how it should work; that the list<Integer> is of indices to keep.
	//Check to see if removing an item shifts the indices of all other items. If so, compensate for that.
	@Override 
	public void applyInfoGain(List<Integer> chosenFeatures, Instances insts)
			throws Exception {
		
		//for all attributes
		for (int i=0; i<insts.numAttributes();i++){
			
			//TODO make sure that removing the insts doesn't affect the indices. If it does, add something to compensate
				//such as iterate over the list to keep and modify as appropriate or instead setting the indices to remove to a sentinel
				//value and then looping through and removing all sentinels after they've all been marked
			
			boolean remove = true;
			
			//iterate over the list to see if that index should be removed or not
			ListIterator<Integer> featureIterator = chosenFeatures.listIterator();
			Integer nextIndex = featureIterator.next();
			while (featureIterator.hasNext()){
				if (nextIndex==i){
					remove=false;
					break;
				}
				nextIndex = featureIterator.next();
			}
			
			//delete the attribute if it wasn't found
			if (remove){
				insts.deleteAttributeAt(i);
			}
		}
	}

//	@Override //Needs cumulative feature driver to determine if an event is a histogram
	//Need to do an indices check
	public List<EventSet> cullWithRespectToTraining(
			List<EventSet> relevantEvents, List<EventSet> eventSetsToCull,CumulativeFeatureDriver cfd)
			throws Exception {
		List<EventSet> oneKnown = relevantEvents;
		int numOfFeatureClasses = oneKnown.size();
		int i;
		List<EventSet> culledUnknownEventSets = new LinkedList<EventSet>();
				
		// make sure all unknown sets would have only events that appear in the known sets
		// UNLESS the event set contains a sole numeric value event - in that case take it anyway
		for (i=0; i<numOfFeatureClasses; i++) {
			if (cfd.featureDriverAt(i).isCalcHist()) {
				// initialize set of relevant events
				EventSet es = oneKnown.get(i);
				Set<String> relevantEventsString = new HashSet<String>(es.size());
				for (Event e: es)
					relevantEventsString.add(e.getEvent());

				// remove all non-relevant events from unknown event sets
				EventSet unknown;
				int initSize;
				Event e;
				unknown = eventSetsToCull.get(i);
				initSize = unknown.size();
				for (int k=initSize-1; k>=0; k--) {
					e = unknown.eventAt(k);
					boolean remove = true;
					for (int l = 0; l<unknown.size();l++){
						if (e.equals(relevantEvents.get(i).eventAt(l))){
							remove=false;
							break;
						}
					}
					
					//TODO same thing here; need to check to see if this will screw with the indices
					//it may not, as this is an unordered set
					//at the same time, it may, as it is a set of a given size
					if (remove){
						unknown.removeEvent(e);
					}
					
				}
				culledUnknownEventSets.add(unknown);
			} else {	// one unique numeric event

				//add non-histogram if it is in the relevantEventSets list
				if (relevantEvents.contains(eventSetsToCull.get(i)))
					culledUnknownEventSets.add(eventSetsToCull.get(i));
			}
		}
		return culledUnknownEventSets;
	}

}
