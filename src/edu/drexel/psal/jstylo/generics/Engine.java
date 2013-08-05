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

	
	/*
	 * Metadata Event format:
	 * 
	 * EventSetID: "<DOCUMENT METADATA>"
	 * Event at Index:
	 * 		0 : author
	 * 		1 : title
	 * 		2 : Sentences in document
	 * 		3 : Words in document
	 * 		4 : Characters in document
	 * 		5 : Letters in document
	 */	
	
	// Done
	@Override
	public List<EventSet> extractEventSets(Document document,
			CumulativeFeatureDriver cumulativeFeatureDriver) throws Exception {
		
		//Extract the Events from the documents
		List<EventSet> generatedEvents = cumulativeFeatureDriver.createEventSets(document);
		
		//create metadata event to store document information
		EventSet documentInfo = new EventSet();
		documentInfo.setEventSetID("<DOCUMENT METADATA>");
		
		//Extract document title and author
		Event authorEvent = new Event(document.getAuthor());
		Event titleEvent = new Event(document.getTitle());
		documentInfo.addEvent(authorEvent);
		documentInfo.addEvent(titleEvent);
		
		//Extract normalization baselines
		// initialize normalization baselines
		
			//Feature Class in Doc //Dunno if we actually want this or not. Makes everything more complicated
			/*	int sum = 0;
				featureClassPerInst.put(instance, new int[numOfFeatureClasses]);
				for (k = start; k < end; k++)
					sum += instance.value(k);
				featureClassPerInst.get(instance)[i] = sum;
					*/
		
			//Sentences in doc
			{
				Document doc = null;
				SingleNumericEventDriver counter = new SentenceCounterEventDriver();
				doc = document;
				doc.load();
				Event tempEvent = new Event(""+(int) counter.getValue(doc));
				documentInfo.addEvent(tempEvent);
			}
				
			//Words in doc
			{
				Document doc = null;
				SingleNumericEventDriver counter = new WordCounterEventDriver();
				doc = document;
				doc.load();
				Event tempEvent = new Event(""+(int) counter.getValue(doc));
				documentInfo.addEvent(tempEvent);
			}
				
			//Characters in doc
			{
				Document doc = null;
				SingleNumericEventDriver counter = new CharCounterEventDriver();
				doc = document;
				doc.load();
				Event tempEvent = new Event(""+(int) counter.getValue(doc));
				documentInfo.addEvent(tempEvent);
			}
				
			//Letters in doc
			{
				Document doc = null;
				SingleNumericEventDriver counter = new LetterCounterEventDriver();
				doc = document;
				doc.load();
				Event tempEvent = new Event(""+(int) counter.getValue(doc));
				documentInfo.addEvent(tempEvent);
			}
		
		//add the metadata EventSet to the List<EventSet>
		generatedEvents.add(documentInfo);
		
		//return the List<EventSet>
		return generatedEvents;
	}

	// Done
	@Override
	public List<List<EventSet>> cull(List<List<EventSet>> eventSets,
			CumulativeFeatureDriver cumulativeFeatureDriver) throws Exception {

		// a hacky workaround for the bug in the eventCuller. Fix that
		// later then remove these
		ArrayList<String> IDs = new ArrayList<String>();
		for (EventSet es : eventSets.get(0)) {
			IDs.add(es.getEventSetID());
		}
		
		//remove the metdata prior to culling
		ArrayList<EventSet> docMetaData = new ArrayList<EventSet>();
		for (List<EventSet> les : eventSets){
			docMetaData.add(les.remove(les.size()-1));
		}
		
		//cull the events
		List<List<EventSet>> culledEventSets = CumulativeEventCuller.cull(
				eventSets, cumulativeFeatureDriver);

		//add the metadata back in
		int index = 0;
		for (List<EventSet> les : culledEventSets){
			les.add(docMetaData.get(index));
			index++;
		}
		
		// a hacky workaround for the bug in the eventCuller. Fix that
		// later then remove these
		for (int j1 = 0; j1 < culledEventSets.size(); j1++) {
			for (int iterator = 0; iterator < culledEventSets.get(j1).size(); iterator++) {
				culledEventSets.get(j1).get(iterator)
						.setEventSetID(IDs.get(iterator));
			}
		}
		
		//return culled events
		return culledEventSets;
	}

	// Done.
	@Override
	public List<EventSet> getRelevantEvents(
			List<List<EventSet>> culledEventSets,
			CumulativeFeatureDriver cumulativeFeatureDriver) throws Exception {

		//remove the metadata prior to generating the relevantEvents
		ArrayList<EventSet> docMetaData = new ArrayList<EventSet>();
		for (List<EventSet> les : culledEventSets){
			docMetaData.add(les.remove(les.size()-1));
		}
		
		List<EventSet> relevantEvents = new LinkedList<EventSet>();

		// iterate over the List of Lists
		for (List<EventSet> l : culledEventSets) {
			// iterate over each inner list's eventSets
			int featureIndex = 0;
			for (EventSet esToAdd : l) {
				// whether or not to add the event set to the list (if false, it
				// is already on the list)
				boolean add = true;;

				for (EventSet esl : relevantEvents) {
					// this should compare the category/name of the event set
					if (esToAdd.getEventSetID().equals(esl.getEventSetID())) {
						add = false;
						break;
					}
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
		
		//add the metadata back in
		int index = 0;
		for (List<EventSet> les : culledEventSets){
			les.add(docMetaData.get(index));
			index++;
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

		//remove the metdata prior to generating attribute list
		ArrayList<EventSet> docMetaData = new ArrayList<EventSet>();
		for (List<EventSet> les : culledEventSets){
			docMetaData.add(les.remove(les.size()-1));
		}
		
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
		FastVector<String> authorNames = new FastVector();
		authorNames.addElement("_Unknown_");
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
		
		// Add the attribute for document title if enabled
		if (hasDocTitles) {
			Attribute docTitle = new Attribute("Document Title", (FastVector) null);
			attributes.add(docTitle);
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

		//add the metadata back in
		int index = 0;
		for (List<EventSet> les : culledEventSets){
			les.add(docMetaData.get(index));
			index++;
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
			List<EventSet> documentData, boolean isSparse,
			boolean hasDocTitles) throws Exception {

		// initialize vector size (including authorName and title if required)
		// and first indices of feature classes array
		int vectorSize = attributes.size();
		if (hasDocTitles)
			vectorSize++;

		// generate training instances
		Instance inst = null;
		if (isSparse)
			inst = new SparseInstance(vectorSize);
		else
			inst = new DenseInstance(vectorSize);
		
		int start = 0;
		
		//add the document title if need be
		if (hasDocTitles){
			start = 1;
			inst.setValue(attributes.get(0),(documentData.get(documentData.size()-1).eventAt(1).getEvent().replaceAll("\\\\","/")));
		}
		
		// add the document author
		if (!(documentData.get(documentData.size()-1).eventAt(0).getEvent() == null)) {
			inst.setValue((Attribute) attributes.get(attributes.size() - 1),
					documentData.get(documentData.size()-1).eventAt(0).getEvent());
		}
		
		//remove metadata event
		EventSet metadata = documentData.remove(documentData.size()-1);
		
		//-1 for indexing
		for (int i=start; i<attributes.size()-1;i++){
			inst.setValue((attributes.get(i)), 0);
		}
		
		//go through all eventSets in the document
		for (EventSet es: documentData){
			
			ArrayList<Integer> indices = new ArrayList<Integer>();
			ArrayList<Event> events = new ArrayList<Event>();
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
						int currIndex=0;
						if (hasDocTitles){
							currIndex++;
						}
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
								if (found){
									break;
								}
								currIndex++;
							}
							if (found){
								break;
							}
							if (!hasInner){
								currIndex++;
							}
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
				if (hasDocTitles)
					nonHistIndex++;
				
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
				String eventString = es.eventAt(0).getEvent();
				int startIndex = eventString.indexOf("{");
				int endIndex = eventString.indexOf("}");
				eventString = eventString.substring(startIndex+1,endIndex);

				double value = Double.parseDouble(eventString);
				inst.setValue((Attribute) attributes.get(nonHistIndex), value);
			}
		}
		//add metadata back. Not sure if necessary
		documentData.add(metadata);
		
		return inst;
	}

	// Done
	@Override
	public void normInstance(CumulativeFeatureDriver cfd, Instance instance,
			List<EventSet> documentData, boolean hasDocTitles) throws Exception {

		int i;
		int numOfFeatureClasses = cfd.numOfFeatureDrivers();

		//HashMap<Instance, int[]> featureClassPerInst = null;
		int sentencesPerInst = Integer.parseInt(documentData.get(documentData.size()-1).eventAt(2).getEvent());
		int wordsPerInst = Integer.parseInt(documentData.get(documentData.size()-1).eventAt(3).getEvent());
		int charsPerInst = Integer.parseInt(documentData.get(documentData.size()-1).eventAt(4).getEvent());
		int lettersPerInst = Integer.parseInt(documentData.get(documentData.size()-1).eventAt(5).getEvent());
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

		// normalizes features
		for (i = 0; i < numOfFeatureClasses; i++) {

			NormBaselineEnum norm = cfd.featureDriverAt(i).getNormBaseline();
			double factor = cfd.featureDriverAt(i).getNormFactor();
			int start = featureClassAttrsFirstIndex[i], end = featureClassAttrsFirstIndex[i + 1], k;

		/*	if (norm == NormBaselineEnum.FEATURE_CLASS_IN_DOC) { //TODO decide if we want to keep this or remove it
				
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
			} else */
			if (norm == NormBaselineEnum.SENTENCES_IN_DOC) {
				// use wordsInDoc
				if (!cfd.featureDriverAt(i).isCalcHist()) {
					instance.setValue(start, instance.value(start) * factor
							/ ((double) sentencesPerInst));
				} else {
					for (k = start; k < end; k++)
						instance.setValue(k, instance.value(k) * factor
								/ ((double) sentencesPerInst));
				}
			} else if (norm == NormBaselineEnum.WORDS_IN_DOC) {
				// use wordsInDoc
				if (!cfd.featureDriverAt(i).isCalcHist()) {
					instance.setValue(start, instance.value(start) * factor
							/ ((double) wordsPerInst));
				} else {
					for (k = start; k < end; k++)
						instance.setValue(k, instance.value(k) * factor
								/ ((double) wordsPerInst));
				}

			} else if (norm == NormBaselineEnum.CHARS_IN_DOC) {
				// use charsInDoc
				if (!cfd.featureDriverAt(i).isCalcHist()) {
					instance.setValue(start, instance.value(start) * factor
							/ ((double) charsPerInst));
				} else {
					for (k = start; k < end; k++)
						instance.setValue(k, instance.value(k) * factor
								/ ((double) charsPerInst));
				}
			} else if (norm == NormBaselineEnum.LETTERS_IN_DOC) {
				// use charsInDoc
				if (!cfd.featureDriverAt(i).isCalcHist()) {
					instance.setValue(start, instance.value(start) * factor
							/ ((double) lettersPerInst));
				} else {
					for (k = start; k < end; k++)
						instance.setValue(k, instance.value(k) * factor
								/ ((double) lettersPerInst));
				}
			}
		}
	}

	// Done
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
				;
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

	// Done
	@Override 
	public double[][] applyInfoGain(double[][] sortedFeatures, Instances insts, int n)
			throws Exception {
		
		//find out how many values to remove
		int valuesToRemove =-1;
		if (n>sortedFeatures.length)
			return sortedFeatures;
		else
			valuesToRemove = sortedFeatures.length-n;
		
		double[][] keepArray = new double[n][2]; //array to be returned
		double[][] removeArray = new double[valuesToRemove][2]; //array to be sorted and be removed from the Instances object
		
		//populate the arrays
		for (int i =0; i<sortedFeatures.length; i++){
			if (i<n){
				keepArray[i][0] = sortedFeatures[i][0];
				keepArray[i][1] = sortedFeatures[i][1];
			} else {
				removeArray[i-n][0] = sortedFeatures[i][0];
				removeArray[i-n][1] = sortedFeatures[i][1];
			}
		}
		
		//sort based on index
		Arrays.sort(removeArray, new Comparator<double[]>() {
			@Override
			public int compare(final double[] first, final double[] second) {
				return -1 * ((Double) first[1]).compareTo(((Double) second[1]));
			}
		});
		
		//for all of the values to remove
		for (int i=0; i<removeArray.length;i++){
			
			//get the index to remove
			int indexToRemove = (int)Math.round(removeArray[i][1]);
			
			//remove from the Instances object
			insts.deleteAttributeAt(indexToRemove);
			
			//adjust all of the indices in the keepArray to compensate for the removal
			for (int j=0; j<keepArray.length;j++){
				if (indexToRemove <= (int)Math.round(keepArray[j][1])){
					keepArray[j][1] = keepArray[j][1]-1;
				}
			}
		}
		
		//return the array consisting only of the top n values
		return keepArray;
	}

	// Done
	@Override 
	public void applyInfoGain(double[][] sortedFeatures, Instance inst, int n)
			throws Exception {
		
		// find out how many values to remove
		int valuesToRemove = -1;
		if (n > sortedFeatures.length)
			;
		else
			valuesToRemove = sortedFeatures.length - n;

		if (!(valuesToRemove == -1)) {
			double[][] removeArray = new double[valuesToRemove][2]; // array to be sorted and be removed from the Instances object

			// populate the arrays
			for (int i = 0; i < sortedFeatures.length; i++) {
				if (i < n) {
					;
				} else {
					removeArray[i - n][0] = sortedFeatures[i][0];
					removeArray[i - n][1] = sortedFeatures[i][1];
				}
			}

			// sort based on index
			Arrays.sort(removeArray, new Comparator<double[]>() {
				@Override
				public int compare(final double[] first, final double[] second) {
					return -1 * ((Double) first[1]).compareTo(((Double) second[1]));
				}
			});

			// for all of the values to remove
			for (int i = 0; i < removeArray.length; i++) {

				// get the index to remove
				int indexToRemove = (int) Math.round(removeArray[i][1]);

				// remove from the Instances object
				inst.deleteAttributeAt(indexToRemove);
			}
		}
	}
	
	// Done
	@Override
	public List<EventSet> cullWithRespectToTraining(
			List<EventSet> relevantEvents, List<EventSet> eventSetsToCull,
			CumulativeFeatureDriver cfd) throws Exception {
		List<EventSet> relevant = relevantEvents;
		int numOfFeatureClasses = eventSetsToCull.size()-1; //-1 to compensate for removing metadata
		int i;
		List<EventSet> culledUnknownEventSets = new LinkedList<EventSet>();

		//remove the metadata prior to culling
		EventSet metadata = eventSetsToCull.remove(eventSetsToCull.size()-1);
		
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
		
		eventSetsToCull.add(metadata);
		culledUnknownEventSets.add(metadata);
		
		return culledUnknownEventSets;
	}

}
