package edu.drexel.psal.jstylo.generics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import weka.attributeSelection.InfoGainAttributeEval;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;

import com.jgaap.generics.Canonicizer;
import com.jgaap.generics.Document;
import com.jgaap.generics.Event;
import com.jgaap.generics.EventDriver;
import com.jgaap.generics.EventHistogram;
import com.jgaap.generics.EventSet;

import edu.drexel.psal.jstylo.eventDrivers.CharCounterEventDriver;
import edu.drexel.psal.jstylo.eventDrivers.LetterCounterEventDriver;
import edu.drexel.psal.jstylo.eventDrivers.SentenceCounterEventDriver;
import edu.drexel.psal.jstylo.eventDrivers.SingleNumericEventDriver;
import edu.drexel.psal.jstylo.eventDrivers.WordCounterEventDriver;

public class Engine implements API {

	// Done
	@Override
	public List<EventSet> extractEventSets(Document document,
			CumulativeFeatureDriver cumulativeFeatureDriver) throws Exception {
		return cumulativeFeatureDriver.createEventSets(document);
	}

	// Done
	@Override
	public List<List<EventSet>> cull(List<List<EventSet>> eventSets,
			CumulativeFeatureDriver cumulativeFeatureDriver) throws Exception {

		// FIXME a hacky workaround for the bug in the eventCuller. Fix that
		// later then remove these
		ArrayList<String> IDs = new ArrayList<String>();
		for (EventSet es : eventSets.get(0)) {
			IDs.add(es.getEventSetID());
		}

		List<List<EventSet>> culledEventSets = CumulativeEventCuller.cull(
				eventSets, cumulativeFeatureDriver);

		// FIXME a hacky workaround for the bug in the eventCuller. Fix that
		// later then remove these
		for (int j1 = 0; j1 < culledEventSets.size(); j1++) {
			for (int iterator = 0; iterator < culledEventSets.get(j1).size(); iterator++) {
				culledEventSets.get(j1).get(iterator)
						.setEventSetID(IDs.get(iterator));
			}
		}
		return culledEventSets;
	}

	// Done
	@Override
	public List<EventSet> getRelevantEvents(
			List<List<EventSet>> culledEventSets,
			CumulativeFeatureDriver cumulativeFeatureDriver) throws Exception {

		List<EventSet> relevantEvents = new LinkedList<EventSet>();

		// iterate over the List of Lists
		for (List<EventSet> l : culledEventSets) {
			// iterate over each inner list's eventSets
			int featureIndex = 0;
			for (EventSet esToAdd : l) {
				// whether or not to add the event set to the list (if false, it
				// is already on the list)
				boolean add = true;

				// the index of where the event set is--assuming it's in the
				// list of relevant events
				// if it is not in the list, this variable is not referenced
				int index = 0;

				for (EventSet esl : relevantEvents) {
					// this should compare the category/name of the event set
					if (esToAdd.getEventSetID().equals(esl.getEventSetID())) {
						add = false;
						break;
					}
					if (add)
						index++;
				}

				// this event set isn't on the list at all, just add it (which
				// also adds its internal events) to the list
				if (add) {
					EventSet temp = new EventSet();
					temp.setEventSetID(esToAdd.getEventSetID());

					for (Event e : esToAdd) {
						boolean absent = true;
						for (Event ae : temp) {
							if (ae.getEvent().equals(e.getEvent())) {
								absent = false;
								break;
							}
						}
						if (absent) {
							if (!cumulativeFeatureDriver.featureDriverAt(
									featureIndex).isCalcHist())
								temp.addEvent(new Event("{-}"));
							else
								temp.addEvent(e);
						}
					}

					relevantEvents.add(temp);
				} else {
					// go through this eventSet and add any events to the
					// relevant EventSet if they aren't already there.
					if (cumulativeFeatureDriver.featureDriverAt(featureIndex)
							.isCalcHist()) {
						for (Event e : esToAdd) {
							boolean toAdd = true;

							for (Event re : relevantEvents.get(featureIndex)) {
								if (e.getEvent().equals(re.getEvent())) {
									toAdd = false;
									break;
								}
							}
							if (toAdd) {
								relevantEvents.get(featureIndex).addEvent(e);
							}
						}
					}
				}
				featureIndex++;
			}
		}
		return relevantEvents;
	}

	// Done
	@Override
	public ArrayList<Attribute> getAttributeList(
			List<List<EventSet>> culledEventSets,
			List<EventSet> relevantEvents,
			CumulativeFeatureDriver cumulativeFeatureDriver,
			boolean hasDocTitles) throws Exception {

		int numOfFeatureClasses = relevantEvents.size();

		int numOfVectors = culledEventSets.size();
		List<EventSet> list;

		// initialize author name set
		LinkedList<String> authors = new LinkedList<String>();
		for (int i = 0; i < numOfVectors; i++) {
			String author = culledEventSets.get(i).get(0).getAuthor();
			if (!authors.contains(author))
				authors.add(author);
		}
		Collections.sort(authors);

		// initialize Weka attributes vector (but authors attribute will be
		// added last)
		FastVector attributeList = new FastVector(relevantEvents.size() + 1);
		FastVector authorNames = new FastVector();
		authorNames.addElement("_dummy_");
		for (String name : authors)
			authorNames.addElement(name);
		Attribute authorNameAttribute = new Attribute("authorName", authorNames);

		// initialize list of sets of events, which will eventually become the
		// attributes
		List<EventSet> allEvents = new ArrayList<EventSet>(numOfFeatureClasses);

		for (int currEventSet = 0; currEventSet < numOfFeatureClasses; currEventSet++){
			// initialize relevant list of event sets and histograms

			list = new ArrayList<EventSet>();
			for (int i = 0; i < numOfFeatureClasses; i++)
				list.add(relevantEvents.get(i));

			EventSet events = new EventSet();
			events.setEventSetID(relevantEvents.get(currEventSet)
					.getEventSetID());

			if (cumulativeFeatureDriver.featureDriverAt(currEventSet)
					.isCalcHist()) { //histogram feature

				// generate event histograms and unique event list
				EventSet eventSet = list.get(currEventSet);
				for (Event event : eventSet) {
					events.addEvent(event);
				}
				allEvents.add(events);

			} else { // one unique numeric event

				// generate sole event (give placeholder value)
				Event event = new Event("{-}");
				events.addEvent(event);
				allEvents.add(events);
			}
		}
		
		// Adds all of the events to the fast vector
		int featureIndex = 0;
		for (EventSet es : allEvents) {
			Iterator<Event> iterator = es.iterator();
			if (cumulativeFeatureDriver.featureDriverAt(featureIndex)
					.isCalcHist()) {
				Event nextEvent = (Event) iterator.next();
				while (iterator.hasNext()) {
					attributeList.addElement(nextEvent);
					nextEvent = (Event) iterator.next();
				}
				attributeList.addElement(nextEvent);
			} else {
				attributeList.addElement(es);
			}
			featureIndex++;
		}
		
		// The actual list of attributes to return
		ArrayList<Attribute> attributes = new ArrayList<Attribute>();
		
		
		// FIXME not sure what type of attribute to use here
		// I can't get the doc titles without passing the documents through here
		// which seems like a waste
		if (hasDocTitles) {
			// FastVector docTitles = new FastVector();
			attributes.add(new Attribute("Document Title"));
		}
			
		// here's where we create the new Attribute object and add it to the
		// attributes list to be returned
		for (int i = 0; i < attributeList.size(); i++) {

			// initialize parameters
			int index = -1;
			String eventString = "";

			Object temp = (Object) attributeList.elementAt(i);
			if (temp instanceof EventSet) {
				int tempIndex = 0;
				EventSet nonHist = (EventSet) attributeList.elementAt(i);

				boolean found = false;

				for (EventSet es : relevantEvents) {

					boolean hasInner = false;

					if (nonHist.getEventSetID().equals(es.getEventSetID())) {
						found = true;
						index = tempIndex;
						break;
					}

					for (Event e : es) {
						hasInner = true;
						tempIndex++;
					}

					if (!hasInner)
						tempIndex++;
				}

				if (found) {
					attributes
							.add(new Attribute(nonHist.getEventSetID(), index));
				}

			} else {

				// and current event to be transformed into an attribute
				Event tempEvent = (Event) attributeList.elementAt(i);
				int tempIndex = 0;
				// get the attribute string
				eventString = tempEvent.getEvent();

				for (EventSet es : relevantEvents) {

					boolean found = false; // if we've found the event, break
											// out of the loop

					// iterate over the histogram/eventset (if it is a
					// non-histogram, this will not occur)
					for (Event e : es) {
						boolean innerFound = false; // if we find the event,
													// break out of the loop

						// check all of the events
						if (e.getEvent().equals(tempEvent.getEvent())) {
							innerFound = true;
							found = true;
						}

						// break the loop if we found it
						if (innerFound) {
							index = tempIndex;
							break;
						}
						// otherwise increment the index and keep looking
						tempIndex++;
					}
					// break the loop if we found it
					if (found) {
						index = tempIndex;
						break;
					}
				}

				// if the feature is a relevant event, add it to the attribute
				// list
				if (index != -1) {
					attributes.add(new Attribute(eventString, index));
				}

			}
		}

		// add authors attribute as last attribute
		attributes.add(authorNameAttribute);
		return attributes;
	}

	// Done
	@Override
	public Instance createInstance(List<Attribute> attributes,
			List<EventSet> relevantEvents,
			CumulativeFeatureDriver cumulativeFeatureDriver,
			List<EventSet> documentData, Document document, boolean isSparse,
			boolean hasDocTitles) throws Exception {

		int numOfFeatureClasses = relevantEvents.size();

		// initialize vector size (including authorName and title if required)
		// and first indices of feature classes array

		int vectorSize = attributes.size();
		if (hasDocTitles)
			vectorSize++;

		// generate training instances
		Instance inst = null;
		int numOfVectors = documentData.size();

		if (isSparse)
			inst = new SparseInstance(vectorSize);
		else
			inst = new DenseInstance(vectorSize);
		
		//-1 for indexing -1 for skipping the author
		for (int i=0; i<attributes.size()-2;i++){
			inst.setValue(((Attribute)attributes.get(i)), 0);
		}
		
		//go through all eventSets in the document
		for (EventSet es: documentData){
			
			ArrayList<Integer> indices = new ArrayList<Integer>();
			ArrayList<Event> events = new ArrayList<Event>();
			//Set<Event> events = new HashSet<Event>();
			EventHistogram currHistogram = new EventHistogram();
			
			boolean eventSetIsRelevant = false;
			
			if (cumulativeFeatureDriver.featureDriverAt(
					documentData.indexOf(es)).isCalcHist()) {
				
				for (EventSet res : relevantEvents) {
					if (es.getEventSetID().equals(res.getEventSetID())) {
						eventSetIsRelevant = true;
						break;
					}
				}
				
				if (eventSetIsRelevant) {

					// find the indices of the events
					// and count all of the events
					for (Event e : es) {
						int currIndex=(hasDocTitles ? 1 : 0);
						boolean hasInner = false;

						for (EventSet res : relevantEvents) {
							boolean found = false;
							for (Event re : res) {
								hasInner = true;
								
								//if they are the same event
								if (e.getEvent().equals(re.getEvent())) {
									boolean inList = false;
									for (Event el : events) {
										if (el.getEvent().equals(e.getEvent())) {
											inList = true;
											break;
										}
									}
									
									if (!inList) {
										indices.add(currIndex);
										events.add(e);
									}
									//Old location revert if change breaks
									currHistogram.add(e);
									found = true;
								}
								//currHistogram.add(e);
								if (found)
									break;
								currIndex++;
							}
							if (found)
								break;
							if (!hasInner)
								currIndex++;
						}
					}

					//calculate/add the histograms
					
					int index = 0;
					for (Integer i: indices){
						inst.setValue((Attribute)attributes.get(i),currHistogram.getAbsoluteFrequency(events.get(index)));
						index++;
					}
					
				}
			} else { //non histogram feature
				
				int nonHistIndex = 0;
				//find the indices of the events
				//and count all of the events
				
				for (EventSet res : relevantEvents) {
					
					if (es.getEventSetID().equals(res.getEventSetID())){
						break;
					}
					
					boolean hasInner = false;
					for (Event re : res) {
						hasInner = true;
						nonHistIndex++;
					}
					
					if (!hasInner)
						nonHistIndex++;
				}

				//Extract and add the event
				FeatureDriver nonHistDriver = cumulativeFeatureDriver
						.featureDriverAt(documentData.indexOf(es));
				Document tempDoc = document;

				for (Canonicizer c : nonHistDriver.getCanonicizers()) {
					tempDoc.addCanonicizer(c);
				}

				EventDriver underlyingDriver = nonHistDriver
						.getUnderlyingEventDriver();

				tempDoc.load();
				tempDoc.processCanonicizers();

				EventSet nonHistES = underlyingDriver
						.createEventSet(tempDoc);

				double value = Double.parseDouble(nonHistES.eventAt(0)
						.getEvent());
				inst.setValue((Attribute) attributes.get(nonHistIndex), value);
				
			}
		}
		
		// if it's a test document, it won't have an author
		if (!(document.getAuthor() == null)) {
			inst.setValue((Attribute) attributes.get(attributes.size() - 1),
					document.getAuthor());
		}
		return inst;
	}

	// Done
	@Override
	public void normInstance(CumulativeFeatureDriver cfd, Instance instance,
			Document document, boolean hasDocTitles) throws Exception {

		int i;
		int numOfFeatureClasses = cfd.numOfFeatureDrivers();

		HashMap<Instance, int[]> featureClassPerInst = null;
		HashMap<Instance, Integer> sentencesPerInst = null;
		HashMap<Instance, Integer> wordsPerInst = null;
		HashMap<Instance, Integer> charsPerInst = null;
		HashMap<Instance, Integer> lettersPerInst = null;
		int[] featureClassAttrsFirstIndex = new int[numOfFeatureClasses + 1];

		// initialize vector size (including authorName and title if required)
		// and first indices of feature classes array
		int vectorSize = (hasDocTitles ? 1 : 0);
		for (i = 0; i < numOfFeatureClasses; i++) {
			String featureDriverName = cfd.featureDriverAt(i).displayName()
					.replace(" ", "-");
			String nextFeature = instance.attribute(vectorSize).name()
					.replace(" ", "-");
			featureClassAttrsFirstIndex[i] = vectorSize;
			while (nextFeature.contains(featureDriverName)) {
				vectorSize++;
				nextFeature = instance.attribute(vectorSize).name();
			}
		}

		// initialize normalization baselines
		for (i = 0; i < numOfFeatureClasses; i++) {
			NormBaselineEnum norm = cfd.featureDriverAt(i).getNormBaseline();
			int start = featureClassAttrsFirstIndex[i], end = featureClassAttrsFirstIndex[i + 1], k;

			if (norm == NormBaselineEnum.FEATURE_CLASS_IN_DOC
					|| norm == NormBaselineEnum.FEATURE_CLASS_ALL_DOCS) {
				// initialize
				if (featureClassPerInst == null)
					featureClassPerInst = new HashMap<Instance, int[]>();

				// accumulate feature class sum per document

				int sum = 0;
				featureClassPerInst.put(instance, new int[numOfFeatureClasses]);
				for (k = start; k < end; k++)
					sum += instance.value(k);
				featureClassPerInst.get(instance)[i] = sum;

			} else if (norm == NormBaselineEnum.SENTENCES_IN_DOC) {
				// initialize
				if (sentencesPerInst == null)
					sentencesPerInst = new HashMap<Instance, Integer>();

				// extract sentence count and update
				Document doc;
				SingleNumericEventDriver counter = new SentenceCounterEventDriver();

				doc = document;
				doc.load();
				sentencesPerInst.put(instance, (int) counter.getValue(doc));

			} else if (norm == NormBaselineEnum.WORDS_IN_DOC) {
				// initialize
				if (wordsPerInst == null)
					wordsPerInst = new HashMap<Instance, Integer>();

				// extract word count and update
				Document doc;
				SingleNumericEventDriver counter = new WordCounterEventDriver();
				doc = document;
				doc.load();
				wordsPerInst.put(instance, (int) counter.getValue(doc));

			} else if (norm == NormBaselineEnum.CHARS_IN_DOC) {
				// initialize
				if (charsPerInst == null)
					charsPerInst = new HashMap<Instance, Integer>();

				// extract character count and update
				Document doc;
				SingleNumericEventDriver counter = new CharCounterEventDriver();
				doc = document;
				doc.load();
				charsPerInst.put(instance, (int) counter.getValue(doc));

			} else if (norm == NormBaselineEnum.LETTERS_IN_DOC) {
				// initialize
				if (lettersPerInst == null)
					lettersPerInst = new HashMap<Instance, Integer>();

				// extract letter count and update
				Document doc;
				SingleNumericEventDriver counter = new LetterCounterEventDriver();
				doc = document;
				doc.load();
				lettersPerInst.put(instance, (int) counter.getValue(doc));
			}
		}

		// normalizes features
		for (i = 0; i < numOfFeatureClasses; i++) {

			NormBaselineEnum norm = cfd.featureDriverAt(i).getNormBaseline();
			double factor = cfd.featureDriverAt(i).getNormFactor();
			int start = featureClassAttrsFirstIndex[i], end = featureClassAttrsFirstIndex[i + 1], k;

			if (norm == NormBaselineEnum.FEATURE_CLASS_IN_DOC) {
				// use featureClassPerDoc
				if (!cfd.featureDriverAt(i).isCalcHist()) {
					instance.setValue(start, instance.value(start) * factor
							/ ((double) featureClassPerInst.get(instance)[i]));
				} else {
					for (k = start; k < end; k++)
						instance.setValue(
								k,
								instance.value(k)
										* factor
										/ ((double) featureClassPerInst
												.get(instance)[i]));
				}
			} else if (norm == NormBaselineEnum.SENTENCES_IN_DOC) {
				// use wordsInDoc
				if (!cfd.featureDriverAt(i).isCalcHist()) {
					instance.setValue(start, instance.value(start) * factor
							/ ((double) sentencesPerInst.get(instance)));
				} else {
					for (k = start; k < end; k++)
						instance.setValue(k, instance.value(k) * factor
								/ ((double) sentencesPerInst.get(instance)));
				}
			} else if (norm == NormBaselineEnum.WORDS_IN_DOC) {
				// use wordsInDoc
				if (!cfd.featureDriverAt(i).isCalcHist()) {
					instance.setValue(start, instance.value(start) * factor
							/ ((double) wordsPerInst.get(instance)));
				} else {
					for (k = start; k < end; k++)
						instance.setValue(k, instance.value(k) * factor
								/ ((double) wordsPerInst.get(instance)));
				}

			} else if (norm == NormBaselineEnum.CHARS_IN_DOC) {
				// use charsInDoc
				if (!cfd.featureDriverAt(i).isCalcHist()) {
					instance.setValue(start, instance.value(start) * factor
							/ ((double) charsPerInst.get(instance)));
				} else {
					for (k = start; k < end; k++)
						instance.setValue(k, instance.value(k) * factor
								/ ((double) charsPerInst.get(instance)));
				}
			}
		}
	}

	// Calculates the information gain for the Instances object
	@Override
	public double[][] calcInfoGain(Instances insts) throws Exception {

		//initialize values
		int len = 0;
		int n = insts.numAttributes();
		Attribute classAttribute = insts.attribute(insts.numAttributes() - 1);
		InfoGainAttributeEval ig = new InfoGainAttributeEval();
		insts.setClass(classAttribute);
		ig.buildEvaluator(insts);

		// extract and sort attributes by InfoGain
		double[][] infoArr = new double[n - 1][2];
		int j = 0;
		for (int i = 0; i < infoArr.length; i++) {
			if (insts.attribute(j).name().equals("authorName")) {
				i--;
			} else {
				len = (len > insts.attribute(j).name().length() ? len : insts
						.attribute(j).name().length());
				infoArr[i][0] = ig.evaluateAttribute(j);
				infoArr[i][1] = j;
			}
			j++;
		}
		//sort based on usefulness
		Arrays.sort(infoArr, new Comparator<double[]>() {
			@Override
			public int compare(final double[] first, final double[] second) {
				return -1 * ((Double) first[0]).compareTo(((Double) second[0]));
			}
		});

		return infoArr;
	}

	@Override
	public double[][] applyInfoGain(double[][] chosenFeatures, Instances insts, int n)
			throws Exception {
		
		double[][] infoArr = chosenFeatures;
		// create an array with the value of infoArr's [i][1] this array will be
		// shrunk and modified as needed
		double[][] tempArr = new double[infoArr.length][2];
		for (int i = 0; i < infoArr.length; i++) {
			tempArr[i][0] = new Double(infoArr[i][0]);
			tempArr[i][1] = new Double(infoArr[i][1]);
		}

		int valuesToKeep =-1;
		if (n>infoArr.length)
			return tempArr;
		else
			valuesToKeep = infoArr.length-n;
		// for all the values we need to delete
		for (int i = 0; i < valuesToKeep; i++) {

			//shrink the array
			double temp[][] = new double[tempArr.length - 1][2];
			for (int k = 0; k < temp.length; k++){
				temp[k][0] = tempArr[k][0];
				temp[k][1] = tempArr[k][1];
			}
			// update array
			tempArr = temp;
		}
		//return the array consisting only of the top n values
		return tempArr;
	}

	// Done
	@Override
	public List<EventSet> cullWithRespectToTraining(
			List<EventSet> relevantEvents, List<EventSet> eventSetsToCull,
			CumulativeFeatureDriver cfd) throws Exception {
		List<EventSet> relevant = relevantEvents;
		int numOfFeatureClasses = eventSetsToCull.size();
		int i;
		List<EventSet> culledUnknownEventSets = new LinkedList<EventSet>();

		// make sure all unknown sets would have only events that appear in the
		// known sets
		// UNLESS the event set contains a sole numeric value event - in that
		// case take it anyway
		for (i = 0; i < numOfFeatureClasses; i++) {
			if (cfd.featureDriverAt(i).isCalcHist()) {
				// initialize set of relevant events
				EventSet es = relevant.get(i);
				Set<String> relevantEventsString = new HashSet<String>(
						es.size());
				for (Event e : es)
					relevantEventsString.add(e.getEvent());

				// remove all non-relevant events from unknown event sets
				EventSet unknown;
				Event e;
				unknown = eventSetsToCull.get(i);
				Iterator<Event> iterator = unknown.iterator();
				Event next = null;

				// the test doc may not contain a given feature (ie it might not
				// have any semi-colons)
				if (iterator.hasNext())
					next = (Event) iterator.next();

				while (iterator.hasNext()) {
					e = next;
					boolean remove = true;

					for (int l = 0; l < unknown.size(); l++) {
						try {
							if (e.equals(relevantEvents.get(i).eventAt(l))) {
								remove = false;
								break;
							}
						} catch (IndexOutOfBoundsException iobe) {
							remove = true;
							break;
						}
					}

					if (remove) {
						iterator.remove();
					}
					next = iterator.next();
				}
				culledUnknownEventSets.add(unknown);

			} else { // one unique numeric event
				// add non-histogram if it is in the relevantEventSets list
				boolean isRelevant = false;
				
				for (EventSet res: relevantEvents){
					if (res.getEventSetID().equals(
							eventSetsToCull.get(i).getEventSetID())) {
						isRelevant = true;
						break;
					}
				}
				
				if (isRelevant)
					culledUnknownEventSets.add(eventSetsToCull.get(i));
			}
		}
		return culledUnknownEventSets;
	}

}
