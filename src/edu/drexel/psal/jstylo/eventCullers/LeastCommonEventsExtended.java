package edu.drexel.psal.jstylo.eventCullers;

import java.util.*;

import com.jgaap.generics.*;

public class LeastCommonEventsExtended extends FrequencyEventsExtended {

	@Override
	public List<EventSet> cull(List<EventSet> eventSets) {
		map = getFrequency(eventSets);

		// get number of events
		if(!getParameter("N").equals("")) {
			N = Integer.parseInt(getParameter("N"));
		}

		// if N is greater than the total number of unique events, return input
		if (N > map.keySet().size())
			return eventSets;

		// create list of event names, sorted in descending order by number of appearances
		List<String> events = new ArrayList<String>(map.keySet().size());
		for (String s: map.keySet())
			events.add(s);
		Collections.sort(events, this); // descending order

		// if one after last event has the same value, take it (although it exceeds N events)
		int size = N;
		while (size < events.size() &&
				map.get(events.get(size-1)) - map.get((events.get(size))) == 0)
			size++;

		// remove irrelevant events
		for (int i=events.size()-1; i >= size; i--)
			events.remove(i);
		Event e;
		for (EventSet es: eventSets) {
			for (int i=es.size()-1; i >= 0; i--) {
				e = es.eventAt(i); 
				if (!events.contains(e.toString()))
					es.removeEvent(e);
			}
		}

		return eventSets;
	}

	@Override
	public String displayName() {
		return "Least Common Events Extended";
	}

	@Override
	public String tooltipText() {
		return  "Analyze only the N least common events across all documents. Takes all events with the same frequency as the most frequent out of the N events.";
	}

	@Override
	public boolean showInGUI() {
		return false;
	}

	/*
	// main for testing
	public static void main(String[] args) throws Exception {
		EventDriver ed = new NaiveWordEventDriver();
		Document doc = new Document("./corpora/drexel_1/a/a_01.txt","a","a_01.txt");
		doc.load();
		doc.addCanonicizer(new UnifyCase());
		doc.processCanonicizers();
		EventSet es = ed.createEventSet(doc);
		List<EventSet> l = new ArrayList<EventSet>(1);
		l.add(es);
		EventCuller c = new LeastCommonEventsCuller();
		c.setParameter("N", 1);
		l = c.cull(l);
		es = l.get(0);
		System.out.println(es);
	}
	*/
}
