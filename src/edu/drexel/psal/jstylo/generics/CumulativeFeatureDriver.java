package edu.drexel.psal.jstylo.generics;

import edu.drexel.psal.jstylo.GUI.FeatureWizardDriver;

import java.io.*;
import java.util.*;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.jgaap.generics.*;

/**
 * The cumulative feature driver class is designed to create a concatenated result of several feature drivers.
 * For details about a feature driver, look into the documentation of {@see FeatureDriver}.
 * 
 * @author Ariel Stolerman
 *
 */
public class CumulativeFeatureDriver {
	/* ======
	 * fields
	 * ======
	 */
	
	/**
	 * List of feature drivers.
	 */
	private List<FeatureDriver> features;
	
	/**
	 * Name of the feature set.
	 */
	private String name;
	
	/**
	 * Description of the feature set.
	 */
	private String description;
	
	/* ============
	 * constructors
	 * ============
	 */

	/**
	 * Creates a new cumulative feature driver.
	 */
	public CumulativeFeatureDriver() {
		features = new ArrayList<FeatureDriver>();
	}
	
	/**
	 * Copy constructor.
	 * @param other to copy.
	 */
	public CumulativeFeatureDriver(CumulativeFeatureDriver other) throws Exception
	{
		String path = "tmp.xml";
		File xml = new File(path);
		PrintWriter pw = new PrintWriter(xml);
		pw.print(other.toXMLString());
		pw.flush();
		pw.close();
		XMLParser parser = new XMLParser(path);
		CumulativeFeatureDriver generated = parser.cfd;
		this.name = generated.name;
		this.description = generated.description;
		this.features = generated.features;
		xml.delete();
	}

	/**
	 * Creates a new cumulative feature driver, with the given list of feature drivers.
	 * @param featureDrivers
	 * 		The list of feature drivers to be set.
	 */
	public CumulativeFeatureDriver(List<FeatureDriver> featureDrivers) {
		this.features = featureDrivers;
	}
	
	/**
	 * Constructor for CumulativeFeatureDriver from a given XML file.
	 * @param filename
	 * 		The name of the XML file to generate the feature set from.
	 * @throws Exception
	 */
	public CumulativeFeatureDriver(String filename) throws Exception {
		Logger.logln("Reading CumulativeFeatureDriver from "+filename);
		XMLParser parser = new XMLParser(filename);
		CumulativeFeatureDriver generated = parser.cfd;
		this.name = generated.name;
		this.description = generated.description;
		this.features = generated.features;
	}
	
	/* ==========
	 * operations
	 * ==========
	 */
	
	/**
	 * Returns a list of all event sets extracted per each feature driver.
	 * @param doc
	 * 		Input document.
	 * @return
	 * 		List of all event sets extracted per each event driver.
	 * @throws Exception 
	 */
	public List<EventSet> createEventSets(Document doc) throws Exception {
		List<EventSet> esl = new ArrayList<EventSet>();
		for (int i=0; i<features.size(); i++) {
			EventDriver ed = features.get(i).getUnderlyingEventDriver();
			Document currDoc = doc instanceof StringDocument ?
					new StringDocument((StringDocument) doc) :
					new Document(doc.getFilePath(),doc.getAuthor(),doc.getTitle());
			
			// apply canonicizers
			try {
				for (Canonicizer c: features.get(i).getCanonicizers())
					currDoc.addCanonicizer(c);
			} catch (NullPointerException e) {
				// no canonicizers
			}
			currDoc.load();
			currDoc.processCanonicizers();
			
			// extract event set
			String prefix = features.get(i).displayName().replace(" ", "-");
			EventSet tmpEs = ed.createEventSet(currDoc);
			EventSet es = new EventSet();
			es.setAuthor(doc.getAuthor());
			es.setDocumentName(doc.getTitle());
			es.setEventSetID(tmpEs.getEventSetID());
			for (Event e: tmpEs)
				es.addEvent(new Event(prefix+"{"+e.getEvent()+"}"));
			esl.add(es);
		}
		return esl;
	}
	
	
	/* =======
	 * queries
	 * =======
	 */
	
	/**
	 * Returns the feature driver at the given index from the list of feature drivers.
	 * @param i
	 * 		The index of the desired feature driver in the list of feature drivers.
	 * @return
	 * 		The feature driver at the given index in the list of feature drivers.
	 */
	public FeatureDriver featureDriverAt(int i) {
		return features.get(i);
	}
	
	/**
	 * Returns the display name of the cumulative feature driver.
	 * @return
	 * 		The display name of the cumulative feature driver.
	 */
	public String displayName() {
		String res = "Cumulative feature driver: ";
		for (FeatureDriver fd: features) {
			res += fd.getUnderlyingEventDriver().displayName()+", ";
		}
		return res.substring(0, res.length()-2);
	}
	
	/**
	 * Returns the size of the feature drivers list.
	 * @return
	 * 		The size of the feature drivers list.
	 */
	public int numOfFeatureDrivers() {
		return features.size();
	}
	
	
	/* =======
	 * setters
	 * =======
	 */
	
	/**
	 * Sets the name of the feature set.
	 * @param name
	 * 		The name to be set.
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Sets the description of the feature set.
	 * @param description
	 * 		The description to be set.
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * Adds the given feature driver to the list of feature drivers and returns its position in the list.
	 * If the add operation failed, returns -1.
	 * @param ed
	 * 		The feature driver to be added to the list of feature drivers.
	 * @return
	 * 		The position of the given feature driver in the list of feature drivers. If the add operation
	 * 		failed, returns -1.
	 */
	public int addFeatureDriver(FeatureDriver fd) {
		if (features.add(fd))
			return features.indexOf(fd);
		else return -1;
		
	}
	
	/**
	 * Replaces the feature driver at the given index with the given feature driver and returns the old one.
	 * @param i
	 * 		The index to replace old feature driver at.
	 * @param fd
	 * 		The new feature driver to be placed at the given index. 
	 * @return
	 * 		The old feature driver at the given index.
	 */
	public FeatureDriver replaceFeatureDriverAt(int i, FeatureDriver fd) {
		FeatureDriver old = featureDriverAt(i);
		features.set(i, fd);
		return old;
	}
	
	/**
	 * Removes the feature driver in the given index.
	 * @param i
	 * 		The index to remove the feature driver from.
	 * @return
	 * 		The removed feature driver, or null if the index is out of bound.
	 */
	public FeatureDriver removeFeatureDriverAt(int i) {
		if (i >= 0 && i < features.size()) {
			return features.remove(i);
		} else {
			return null;
		}
	}
	
	
	/* =======
	 * getters
	 * =======
	 */
	
	/**
	 * Returns the name of the feature set.
	 * @return
	 * 		The name of the feature set.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns the description of the feature set.
	 * @return
	 * 		The description of the feature set.
	 */
	public String getDescription() {
		return description;
	}
	
	
	/* ===========
	 * XML parsing
	 * ===========
	 */
	
	/**
	 * Writes the cumulative feature driver in XML format to the given path.
	 * @param path
	 * 		The path of the output XML file.
	 * @return
	 * 		True iff the write succeeded.
	 */
	public boolean writeToXML(String path) {
		if (!path.endsWith(".xml"))
			path = path+".xml";
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(path));
			bw.write(toXMLString());
			bw.close();
		} catch (IOException e) {
			if (bw != null)
				try {
					bw.close();
				} catch (IOException ex) {}
			return false;
		}
		return true;
	}
	
	/**
	 * Saves the cumulative feature driver in XML format to the given path.
	 * @param path
	 * 		The path to save to.
	 */
	public String toXMLString() {
		String res = "";

		res += "<?xml version=\"1.0\"?>\n";
		res += "<feature-set name=\""+name+"\">\n";
		res += "\t<description value=\""+description+"\"/>\n";

		FeatureDriver fd;
		for (int i=0; i<features.size(); i++) {
			fd = features.get(i);
			res += "\t<feature name=\""+fd.getName()+"\" calc_hist=\""+fd.isCalcHist()+"\">\n";

			// description
			res += "\t\t<description value=\""+fd.getDescription()+"\"/>\n";

			// event driver
			EventDriver ed = fd.getUnderlyingEventDriver();
			res += "\t\t<event-driver class=\""+ed.getClass().getName()+"\">\n";
			// parameters
			for (Pair<String,FeatureDriver.ParamTag> param: FeatureDriver.getClassParams(ed.getClass().getName())) {
				res += "\t\t\t<param name=\""+param.getFirst()+"\" value=\""+ed.getParameter(param.getFirst())+"\"/>\n";
			}
			res += "\t\t</event-driver>\n";

			// canonicizers
			res += "\t\t<canonicizers>\n";
			if (fd.getCanonicizers() != null) {
				for (Canonicizer c: fd.getCanonicizers()) {
					res += "\t\t\t<canonicizer class=\""+c.getClass().getName()+"\">\n";
					for (Pair<String,FeatureDriver.ParamTag> param: FeatureDriver.getClassParams(c.getClass().getName())) {
						res += "\t\t\t\t<param name=\""+param.getFirst()+"\" value=\""+c.getParameter(param.getFirst())+"\"/>\n";
					}
					res += "\t\t\t</canonicizer>\n";
				}
			}
			res += "\t\t</canonicizers>\n";

			// cullers
			res += "\t\t<cullers>\n";
			if (fd.getCullers() != null) {
				for (EventCuller ec: fd.getCullers()) {
					res += "\t\t\t<culler class=\""+ec.getClass().getName()+"\">\n";
					for (Pair<String,FeatureDriver.ParamTag> param: FeatureDriver.getClassParams(ec.getClass().getName())) {
						res += "\t\t\t\t<param name=\""+param.getFirst()+"\" value=\""+ec.getParameter(param.getFirst())+"\"/>\n";
					}
					res += "\t\t\t</culler>\n";
				}
			}
			res += "\t\t</cullers>\n";

			// normalization
			res += "\t\t<norm value=\""+fd.getNormBaseline()+"\"/>\n";
			res += "\t\t<factor value=\""+fd.getNormFactor()+"\"/>\n";

			res += "\t</feature>\n";
		}
		res += "</feature-set>\n";
		
		return res;
	}
	
	/**
	 * Tag to indicate the current scope of the XML.
	 */
	private enum Tag{
		FEATURE_SET,
		FEATURE,
		EVENT_DRIVER,
		CANONICIZERS,
		CANONICIZER,
		CULLERS,
		CULLER,
		NORM,
		FACTOR,
	}
	
	/**
	 * XML parser to create a cumulative feature driver out of a XML file.
	 */
	private class XMLParser extends DefaultHandler {
		
		/* ======
		 * fields
		 * ======
		 */
		private CumulativeFeatureDriver cfd;
		private String filename;
		
		private Tag currTag;
		private FeatureDriver fd;
		private EventDriver ed;
		private Canonicizer c;
		private EventCuller ec;
		
		
		/* ============
		 * constructors
		 * ============
		 */
		public XMLParser(String filename) throws Exception {
			cfd = new CumulativeFeatureDriver();
			this.filename = filename;
			parse();
		}
		
		
		/* ==========
		 * operations
		 * ==========
		 */

		/**
		 * Parses the XML input file into a problem set.
		 * @throws Exception
		 * 		SAXException, ParserConfigurationException, IOException
		 */
		public void parse() throws Exception {
			//get a factory
			SAXParserFactory spf = SAXParserFactory.newInstance();
			//get a new instance of parser
			SAXParser sp = spf.newSAXParser();
			//parse the file and also register this class for call backs
			sp.parse(filename, this);
		}
		
		/*
		 * Returns the generated cumulative feature driver.
		 * @return
		 * 		The generated cumulative feature driver.
		 */
		/*
		public CumulativeFeatureDriver getCumulativeFeatureDriver() {
			return cfd;
		}
		*/
		
		// event handlers
		
		/**
		 * Handler for opening tags.
		 */
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			if (qName.equalsIgnoreCase("feature-set")) {
				currTag = Tag.FEATURE_SET;
				cfd.setName(attributes.getValue("name"));
			
			} else if (qName.equalsIgnoreCase("feature")) {
				currTag = Tag.FEATURE;
				fd = new FeatureDriver();
				fd.setName(attributes.getValue("name"));
				fd.setCalcHist((Boolean.parseBoolean(attributes.getValue("calc_hist"))));
				
			} else if (qName.equalsIgnoreCase("event-driver")) {
				currTag = Tag.EVENT_DRIVER;
				try {
					ed = (EventDriver) Class.forName(attributes.getValue("class")).newInstance();
				} catch (Exception e) {
					System.out.println("failed!");
					ed = null;
				}
			
			} else if (qName.equalsIgnoreCase("canonicizers")) {
				currTag = Tag.CANONICIZERS;
			
			} else if (qName.equalsIgnoreCase("canonicizer")) {
				currTag = Tag.CANONICIZER;
				try {
					c = (Canonicizer) Class.forName(attributes.getValue("class")).newInstance();
				} catch (Exception e) {
					System.out.println("failed!");
					c = null;
				}
			
			} else if (qName.equalsIgnoreCase("cullers")) {
				currTag = Tag.CULLERS;
			
			} else if (qName.equalsIgnoreCase("culler")) {
				currTag = Tag.CULLER;
				try {
					ec = (EventCuller) Class.forName(attributes.getValue("class")).newInstance();
				} catch (Exception e) {
					System.out.println("failed!");
					ec = null;
				}
			
			} else if (qName.equalsIgnoreCase("norm")) {
				currTag = Tag.NORM;
				fd.setNormBaseline(NormBaselineEnum.valueOf(attributes.getValue("value")));
			
			} else if (qName.equalsIgnoreCase("factor")) {
				currTag = Tag.FACTOR;
				fd.setNormFactor(Double.valueOf(attributes.getValue("value")));
			
			} else if (qName.equalsIgnoreCase("description")) {
				if (currTag == Tag.FEATURE_SET) {
					cfd.setDescription(attributes.getValue("value"));
				} else if (currTag == Tag.FEATURE) {
					fd.setDescription(attributes.getValue("value"));
				} else {
					throw new SAXException("description tag not under feature-set or feature tag: "+qName);
				}
				
			} else if (qName.equalsIgnoreCase("param")) {
				if (currTag == Tag.EVENT_DRIVER) {
					ed.setParameter(attributes.getValue("name"), attributes.getValue("value"));
				} else if (currTag == Tag.CANONICIZER) {
					c.setParameter(attributes.getValue("name"), attributes.getValue("value"));
				} else if (currTag == Tag.CULLER) {
					ec.setParameter(attributes.getValue("name"), attributes.getValue("value"));
				} else {
					throw new SAXException("param tag not under event-driver, canonicizer or culler tag: "+qName);
				}
				
			} else {
				throw new SAXException("unrecognized opening tag: "+qName);
			}
		}
		
		/**
		 * Handler for text between open and close tags.
		 */
		public void characters(char ch[], int start, int length) throws SAXException {}
		
		/**
         * Handler for closing tags.
         */
        public void endElement(String uri, String localName,String qName) throws SAXException {
        	if (qName.equalsIgnoreCase("feature-set")) {
			
			} else if (qName.equalsIgnoreCase("feature")) {
				cfd.addFeatureDriver(fd);
				
			} else if (qName.equalsIgnoreCase("event-driver")) {
				fd.setUnderlyingEventDriver(ed);
			
			} else if (qName.equalsIgnoreCase("canonicizers")) {
			
			} else if (qName.equalsIgnoreCase("canonicizer")) {
				fd.addCanonicizer(c);
			
			} else if (qName.equalsIgnoreCase("cullers")) {
			
			} else if (qName.equalsIgnoreCase("culler")) {
				fd.addEventCuller(ec);
			
			} else if (qName.equalsIgnoreCase("norm")) {
			
			} else if (qName.equalsIgnoreCase("factor")) {
			
			} else if (qName.equalsIgnoreCase("description")) {
				
			} else if (qName.equalsIgnoreCase("param")) {
				
			} else {
				throw new SAXException("unrecognized opening tag: "+qName);
			}
        }
	}

	
	public static void main(String[] args) throws Exception {
		FeatureWizardDriver.populateAll();
		CumulativeFeatureDriver cfd = new CumulativeFeatureDriver(new File("feature_sets/example_feature_set.xml").getAbsolutePath());
		cfd = new CumulativeFeatureDriver();
		FeatureDriver fd = new FeatureDriver("word-lengths", true, new edu.drexel.psal.jstylo.eventDrivers.WordLengthEventDriver());
		cfd.addFeatureDriver(fd);
		ProblemSet ps = new ProblemSet("./problem_sets/sample_problem_set.xml");
		WekaInstancesBuilder w = new WekaInstancesBuilder(false);
		w.prepareTrainingSet(ps.getTrainDocs("aa"), cfd);
		System.out.println(w.getTrainingSet());
	}
}
