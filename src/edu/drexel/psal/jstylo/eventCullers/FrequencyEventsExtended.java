package edu.drexel.psal.jstylo.eventCullers;

import java.util.*;

import com.jgaap.generics.*;

public abstract class FrequencyEventsExtended extends EventCuller implements Comparator<String> {

	protected Map<String,Integer> map;
	
	protected int N = 10;
	
	public Map<String,Integer> getFrequency(List<EventSet> eventSets) {
		Map<String,Integer> map = new HashMap<String,Integer>();
		
		for (EventSet es: eventSets) {
			for (Event e: es) {
				Integer curr = map.get(e.toString());
				if (curr == null)
					map.put(e.toString(), 1);
				else
					map.put(e.toString(), curr+1);
			}
		}
		
		return map;
	}
	
	public int compare(String o1, String o2) {
		return map.get(o1) - map.get(o2);
	}

}
