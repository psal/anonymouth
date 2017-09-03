package edu.drexel.psal.jstylo.eventDrivers;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
public class ListRegexpEventDriver extends EventDriver {

	@Override
	public String displayName() {
		return "Regular Expression List-based event driver";
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
				"list or black list, accordingly. This is similar to the List-based event driver, only using a lexicon of regular-expressions " +
				"instead of plain words." +
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

	private String filename;
		
	private boolean whiteList;
	
	private boolean keepLexiconInMem;
	
	private List<String> lexicon;

	@Override
	public EventSet createEventSet(Document ds) {
		
		String param;

		if (!(param = (getParameter("whiteList"))).equals(""))
			whiteList = Boolean.parseBoolean(param);
		
		if (!(param = (getParameter("keepListInMem"))).equals(""))
			keepLexiconInMem = Boolean.parseBoolean(param);

		// create lexicon
		if (lexicon == null) {
			if (!(param = (getParameter("filename"))).equals("")) {
				filename = param;
			} else { // no underlyingfilename,
				filename = null;
			}

			BufferedReader br = null;
			lexicon = new ArrayList<String>();
			String word;

			if (filename != null) {
				try {
					br = new BufferedReader(new FileReader(filename));

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
		
		EventSet es = new EventSet();
		es.setAuthor(ds.getAuthor());
		es.setNewEventSetID(ds.getAuthor());
		
		Map<String,Integer> appearances = new HashMap<String,Integer>();
		
		// extract events
		String text = ds.stringify();
		String match;
		Pattern pattern;
		Matcher matcher;
		for (String regexp: lexicon) {
			pattern = Pattern.compile("\\s"+regexp+"\\s");
		    matcher = pattern.matcher(text);
		    while (matcher.find()) {
		    	match = matcher.group();
		    	if (appearances.get(match) == null)
		    		appearances.put(match, 1);
		    	else
		    		appearances.put(match, appearances.get(match)+1);
		    }
		}
		
		int n;
		if (whiteList) {
			for (String key: appearances.keySet()) {
				n = appearances.get(key);
				for (int i=0; i<n; i++)
					es.addEvent(new Event(key));
			}
		} else {
			for (String key: appearances.keySet())
				text.replaceAll(key, "");
			pattern = Pattern.compile("\\S+");
			matcher = pattern.matcher(text);
			while (matcher.find()) {
		    	es.addEvent(new Event(matcher.group()));
			}
		}
		
		
		// clear list of memory if required
		if (!keepLexiconInMem)
			lexicon = null;
		
		return es;
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
