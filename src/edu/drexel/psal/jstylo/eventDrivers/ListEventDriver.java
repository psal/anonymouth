package edu.drexel.psal.jstylo.eventDrivers;

import java.io.*;
import java.util.*;

import com.jgaap.eventDrivers.NaiveWordEventDriver;
import com.jgaap.generics.*;

import edu.drexel.psal.jstylo.eventCullers.FrequencyEventsExtended;


/**
 * Modification of the White / Black list event driver:
 * <br>
 * Given a filename to be used as lexicon and an event driver, it extracts all events using the given driver
 * and then removes all events extracted that appear / don't appear in the lexicon, based on whether it's a white
 * list or black list, accordingly.
 * <p>
 * <u>Parameters</u>:<br>
 * <ul>
 * 	<li>
 * 	<b>whiteList</b>: set to <i>true</i> for white-list and <i>false</i> for black-list. Default is <i>false</i>.
 * 	</li>
 * 	<li>
 * 	<b>underlyingEvents</b>: set to the full name of the base event driver to be used. Can also use <i>setUnderlyingEvents(EventDriver)</i> with
 * 	an EventDriver instance instead. Default is <i>NaiveWordEventDriver</i>. 
 * 	</li>
 * 	<li>
 * 	<b>filename</b>: set to the file path of the lexicon to be used. Default is empty, in that case will return all events extracted with no filtering.
 * 	</li>
 * 	<li>
 * 	<b>sort</b>: set to <i>true</i> to have the extracted events sorted before lexicon lookup. Increases performance when have a large lexicon, as it reduces
 * 	the number of event list plus lexicon lookup to linear, but requires the lexicon file to be <b>sorted</b> and additional sorting time for the extracted event set.
 * 	Default is <i>false</i>.
 * 	</li>
 * 	<li>
 * 	<b>KeepLexiconInMem</b>: set to <i>true</i> to have the extracted lexicon kept in memory, instead of extracting it from the file for each document.
 * 	Can reduce running time for a large set of documents, but increases memory usage. 
 * 	</li>
 * </ul>
 * 
 * @author Ariel Stolerman
 */
public class ListEventDriver extends EventDriver {

	@Override
	public String displayName() {
		return "List-based event driver";
	}

	@Override
	public String tooltipText() {
		return "Filtered Event Set with Named Events Kept";
	}
	
	@Override
	public String longDescription() {
		String res = 
				"<html>" +
				"Given a filename to be used as lexicon and an event driver, it extracts all events using the given driver " +
				"and then removes all events extracted that appear / don't appear in the lexicon, based on whether it's a white " +
				"list or black list, accordingly." +
				"<p>" +
				"<u>Parameters</u>:<br>" +
				"<ul>" +
				"	<li>" +
				"	<b>whiteList</b>: set to <i>true</i> for white-list and <i>false</i> for black-list. Default is <i>false</i>." +
				"	</li>" +
				"	<li>" +
				"	<b>underlyingEvents</b>: set to the full name of the base event driver to be used. Can also use <i>setUnderlyingEvents(EventDriver)</i> with" +
				"	an EventDriver instance instead. Default is <i>NaiveWordEventDriver</i>." + 
				"	</li>" +
				"	<li>" +
				"	<b>filename</b>: set to the file path of the lexicon to be used. Default is empty, in that case will return all events extracted with no filtering." +
				"	</li>" +
				"	<li>" +
				"	<b>sort</b>: set to <i>true</i> to have the extracted events sorted before lexicon lookup. Increases performance when have a large lexicon, as it reduces" +
				"	the number of event list plus lexicon lookup to linear, but requires the lexicon file to be <b>sorted</b> and additional sorting time for the extracted event set." +
				"	Default is <i>false</i>." +
				"	</li>" +
				"	<li>" +
				"	<b>KeepLexiconInMem</b>: set to <i>true</i> to have the extracted lexicon kept in memory, instead of extracting it from the file for each document." +
				"	Can reduce running time for a large set of documents, but increases memory usage." + 
				"	</li>" +
				"</ul>" +
				"</html>";
		return res;
	}

	@Override
	public boolean showInGUI() {
		return false;
	}

	private EventDriver underlyingEvents;

	private String filename;
	
	private boolean sort;
	
	private boolean whiteList;
	
	private boolean keepLexiconInMem;
	
	private List<String> lexicon;

	@Override
	public EventSet createEventSet(Document ds) {
		
		String param;

		if (!(param = (getParameter("sort"))).equals(""))
			sort = Boolean.parseBoolean(param);

		if (!(param = (getParameter("whiteList"))).equals(""))
			whiteList = Boolean.parseBoolean(param);
		
		if (!(param = (getParameter("keepListInMem"))).equals(""))
			keepLexiconInMem = Boolean.parseBoolean(param);

		if (underlyingEvents == null) {
			param = getParameter("underlyingEvents");
			try {
				underlyingEvents = (EventDriver) Class.forName(param).newInstance();
			} catch (Exception e) {
				underlyingEvents = new NaiveWordEventDriver();
			}
		}

		if (lexicon == null) {
			if (!(param = (getParameter("filename"))).equals("")) {
				filename = param;
			} else { // no underlyingfilename,
				filename = null;
			}

			BufferedReader br = null;
			String word;

			if (filename != null) {
				try {
					try {
						// look in file system
						//System.out.println(filename);
						br = new BufferedReader(new FileReader(filename));
					} catch (IOException e) {
						// look in resources
						
						InputStream in = getClass().getClassLoader().getResourceAsStream(filename);
						br = new BufferedReader(new InputStreamReader(in));
					}

					lexicon = new ArrayList<String>();
					while ((word = br.readLine()) != null) {
						lexicon.add(word.trim());
					}

				} catch (IOException e) {
					System.err.println("Error reading file "+filename);
					e.printStackTrace();
					
				} finally {
					// if the file opened okay, make sure we close it
					if (br != null) {
						try {
							br.close();
						} catch (IOException ioe) {
						}
					}
				}
			} else {
				lexicon = null;
			}
		}
		
		// extract event set and filter
		EventSet es = new EventSet();
		try {
			es = underlyingEvents.createEventSet(ds);
		} catch (EventGenerationException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}
		if (lexicon == null)
			return es;
		
		EventSet newEs = new EventSet();
		newEs.setAuthor(es.getAuthor());
		newEs.setNewEventSetID(es.getAuthor());

		// if needs to sort
		if (sort) {			
			// find frequencies of all events and sort list of unique events
			List<EventSet> tmpList = new ArrayList<EventSet>(1);
			tmpList.add(es);
			Map<String,Integer> numEvents = (new Freq()).getFrequency(tmpList);
			List<String> eventList = new ArrayList<String>(numEvents.keySet().size());
			for (String s: numEvents.keySet())
				eventList.add(s);
			Collections.sort(eventList);
			
			int i = 0, j = 0, c;
			String e1 = "", e2 = "";
			
			// first handle all events < list[0]
			int lexiconSize = lexicon.size();
			for (; i<eventList.size() && j<lexiconSize ;) {
				
				e1 = eventList.get(i);
				try {
					e2 = lexicon.get(j);
				} catch (NullPointerException e) {
					e.printStackTrace();
					e2 = avoidOccasionallyBug(e2,j);
				}
				c = e1.compareTo(e2);
				
				if (c < 0) {
					// if blacklist, add events
					if (!whiteList) {
						for (int k=0; k<numEvents.get(e1); k++)
							newEs.addEvent(new Event(e1));
					}
					// anyway advance index
					i++;
				} else if (c == 0) {
					// if whitelist, add events
					if (whiteList) {
						for (int k=0; k<numEvents.get(e1); k++)
							newEs.addEvent(new Event(e1));
					}
					// anyway advance index and lexicon
					i++;
					j++;
				} else if (c > 0) {
					// advance lexicon
					j++;
				}
			}
			
			// if blacklist and have anything left, add them too
			if (!whiteList) {
				for (; i<eventList.size(); i++) {
					e1 = eventList.get(i);
					for (int k=0; k<numEvents.get(e1); k++)
						newEs.addEvent(new Event(e1));
				}
			}
			
		} else {
			// unsorted
			for (Event e : es) {
				try {
					String s = e.toString();
					if (lexicon.contains(s) && whiteList) {
						// white-list
						newEs.addEvent(e);
					} else if (!lexicon.contains(s) && !whiteList) {
						// black-list
						newEs.addEvent(e);
					}
				} catch (NullPointerException ex) {
					ex.printStackTrace();
					newEs = avoidOccasionallyBug(newEs, e);
				}
			}
		}
		
		// clear list of memory if required
		if (!keepLexiconInMem)
			lexicon = null;
		
		return newEs;
	}
	
	private EventSet avoidOccasionallyBug (EventSet newEs, Event e) {
		BufferedReader br = null;
		String word;
		try {
			try {
				// look in file system
				//System.out.println(filename);
				br = new BufferedReader(new FileReader(filename));
			} catch (IOException ex) {
				// look in resources
				
				InputStream in = getClass().getClassLoader().getResourceAsStream(filename);
				br = new BufferedReader(new InputStreamReader(in));
			}

			lexicon = new ArrayList<String>();
			while ((word = br.readLine()) != null) {
				lexicon.add(word.trim());
			}

		} catch (IOException ex) {
			System.err.println("Error reading file "+filename);
			ex.printStackTrace();
			
		} finally {
			// if the file opened okay, make sure we close it
			if (br != null) {
				try {
					br.close();
				} catch (IOException ioe) {
				}
			}
		}
		try {
			String s = e.toString();
			if (lexicon.contains(s) && whiteList) {
				newEs.addEvent(e);
			} else if (!lexicon.contains(s) && !whiteList) {
				newEs.addEvent(e);
			}
		} catch (NullPointerException ex) {
			ex.printStackTrace();
			newEs = avoidOccasionallyBug(newEs, e);
		}
		
		return newEs;
	}
	
	private String avoidOccasionallyBug (String e2, int j) {
		BufferedReader br = null;
		String word;
		try {
			try {
				// look in file system
				//System.out.println(filename);
				br = new BufferedReader(new FileReader(filename));
			} catch (IOException e) {
				// look in resources
				
				InputStream in = getClass().getClassLoader().getResourceAsStream(filename);
				br = new BufferedReader(new InputStreamReader(in));
			}

			lexicon = new ArrayList<String>();
			while ((word = br.readLine()) != null) {
				lexicon.add(word.trim());
			}

		} catch (IOException e) {
			System.err.println("Error reading file "+filename);
			e.printStackTrace();
			
		} finally {
			// if the file opened okay, make sure we close it
			if (br != null) {
				try {
					br.close();
				} catch (IOException ioe) {
				}
			}
		}
		try {
			e2 = lexicon.get(j);
		} catch (NullPointerException ex) {
			ex.printStackTrace();
			e2 = avoidOccasionallyBug(e2 , j);
		}
		
		return e2;
	}

	public EventDriver getUnderlyingEvents() {
		return underlyingEvents;
	}

	public void setUnderlyingEvents(EventDriver underlyingEvents) {
		this.underlyingEvents = underlyingEvents;
	}

	public boolean isSort() {
		return sort;
	}

	public void setSort(boolean sort) {
		this.sort = sort;
	}

	public boolean isWhiteList() {
		return whiteList;
	}

	public void setWhiteList(boolean whiteList) {
		this.whiteList = whiteList;
	}

	public boolean isKeepLexiconInMem() {
		return keepLexiconInMem;
	}

	public void setKeepLexiconInMem(boolean keepListInMem) {
		this.keepLexiconInMem = keepListInMem;
	}
	
	// used only to get frequencies
	protected class Freq extends FrequencyEventsExtended {
		public List<EventSet> cull(List<EventSet> arg0) {
			return null;
		}
		public String displayName() {
			return null;
		}
		public boolean showInGUI() {
			return false;
		}
		public String tooltipText() {
			return null;
		}
	}
}
