package edu.drexel.psal.jstylo.generics;

import java.io.*;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
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
	private final String NAME = "( CumulativeFeatureDriver ) - ";
	
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
		try {
		PrintWriter pw = new PrintWriter(xml);
		pw.print(other.toXMLString());
		pw.flush();
		pw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			xml = new File(path);
			PrintWriter pw = new PrintWriter(xml);
			pw.print(other.toXMLString());
			pw.flush();
			pw.close();
		}
		XMLParser parser = new XMLParser(path,other);
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
		Logger.logln(NAME+"Reading CumulativeFeatureDriver from "+filename);
		XMLParser parser = new XMLParser(filename,null);
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
		int size = features.size();
		for (int i = 0; i < size; i++) {
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
			tmpEs.setEventSetID(features.get(i).getName()); 
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
		
		private CumulativeFeatureDriver type;
		
		/* ============
		 * constructors
		 * ============
		 */
		public XMLParser(String filename, CumulativeFeatureDriver type) throws Exception {
			cfd = new CumulativeFeatureDriver();
			this.filename = filename;
			this.type = type;
			parse();
		}
		
		
		/* ==========
		 * operations
		 * ==========
		 */
		
		//TODO
		/**
		 * Parses the XML input file into a problem set.
		 * @throws Exception
		 * 		SAXException, ParserConfigurationException, IOException
		 */
		public void parse() {
			
			//intialize the parser, parse the document, and build the tree
			DocumentBuilderFactory builder = DocumentBuilderFactory.newInstance();
			DocumentBuilder dom = null;
			org.w3c.dom.Document xmlDoc = null;
			if (type == null) {
			try {
				dom = builder.newDocumentBuilder();
			} catch (ParserConfigurationException e1) {
				e1.printStackTrace();
			}
			try {
				xmlDoc = dom.parse(filename);
			} catch (SAXException | IOException e1) {
				e1.printStackTrace();
			}
			}
			else {
				try {
					dom = builder.newDocumentBuilder();
				} catch (ParserConfigurationException e1) {
					e1.printStackTrace();
				}
				try {
					xmlDoc = dom.parse(filename);
				} catch (SAXException | IOException e1) {
					e1.printStackTrace();
					xmlDoc = avoidAnnoyingBug(dom);
				}
			}
			xmlDoc.getDocumentElement().normalize();
			
			//create the feature set and intialize the cfd's name
			NodeList featureSet = xmlDoc.getElementsByTagName("feature-set");
			Element fs = (Element) xmlDoc.importNode(featureSet.item(0),false);
			cfd.setName(fs.getAttribute("name"));
			
			//load single value lists (ie lists where, any given feature set will have exactly one value
			NodeList descriptions = xmlDoc.getElementsByTagName("description");
			NodeList eventDrivers = xmlDoc.getElementsByTagName("event-driver");
			NodeList normValues = xmlDoc.getElementsByTagName("norm");
			NodeList normFactors = xmlDoc.getElementsByTagName("factor");
			
			//load the feature set description
			Element fsd = (Element) xmlDoc.importNode(descriptions.item(0),false);
			cfd.setDescription(fsd.getAttribute("value"));
			
			//get the list of features
			NodeList items = xmlDoc.getElementsByTagName("feature");
			//go over the nodes and get the information we want
			for (int i=0; i<items.getLength();i++){
				boolean successful = true;
				FeatureDriver fd = new FeatureDriver();
				try{
					//initialize this feature node, this feature element, and the feature driver
					Node currentNode = items.item(i);
					Element currentElement = (Element) xmlDoc.importNode(items.item(i), true);
					
					
					//add the information from this node to the feature driver
					fd.setName(currentElement.getAttribute("name"));
					if (currentElement.getAttribute("calc_hist").equals("false"))
						fd.setCalcHist(false);
					else
						fd.setCalcHist(true);
					
					//get all of the components
					
					//description
					Element currDesc = (Element) xmlDoc.importNode(descriptions.item(i+1), false);
					fd.setDescription(currDesc.getAttribute("value"));
				
					//event driver
					Node currEvNode = eventDrivers.item(i);
					Element currEvDriver = (Element) xmlDoc.importNode(eventDrivers.item(i), false);
					EventDriver ed = (EventDriver) Class.forName(currEvDriver.getAttribute("class")).newInstance();
					//check for args, adding them if necessary
					if (currEvNode.hasChildNodes()){
						NodeList params = currEvNode.getChildNodes();
						for (int k=0; k<params.getLength();k++){
							
							Node currPNode = params.item(k);
							if(!currPNode.getNodeName().equals("#text")){
								Element currParam = (Element) xmlDoc.importNode(params.item(k), false);
								if (currParam.hasAttribute("name")&&currParam.hasAttribute("value"))
									ed.setParameter(currParam.getAttribute("name"), currParam.getAttribute("value"));
							}
						}
					}
					fd.setUnderlyingEventDriver(ed);
					
					NodeList children = currentNode.getChildNodes(); //used for both canonicizers and cullers
					//canonicizer(s)
					//loop through the children until you find the canonicizers
					for (int j=0; j<children.getLength(); j++){
						Node current = children.item(j);
						if (!current.getNodeName().equals("#text")){
							if (current.getNodeName().equals("canonicizers")){
								if (current.hasChildNodes()){
									NodeList canonicizers = current.getChildNodes();
									//iterate over the canonicizers
									for (int k=0; k<canonicizers.getLength();k++){
										if (!canonicizers.item(k).getNodeName().equals("#text")){
											Element currCanon = (Element) xmlDoc.importNode(canonicizers.item(k), false);
											if (currCanon.hasAttribute("class"))
												fd.addCanonicizer((Canonicizer) Class.forName(currCanon.getAttribute("class")).newInstance());	
										}
									}
								}
								break; //once we're done with canonicizers we don't need to check anything else
							}
						}
					}
				
					//event culler(s)
					//look through the children of features until you find the cullers
					for (int j=0; j<children.getLength();j++){
						Node current = children.item(j);
						//then iterate over the individual cullers
						if (current.getNodeName().equals("cullers")){
							if (current.hasChildNodes()){ //if it has children, otherwise its just an empty tag
							
								//list of cullers to be passed to the fd
								LinkedList<EventCuller> cullers = new LinkedList<EventCuller>();
								NodeList evculls = current.getChildNodes();
							
								//go over the culler tags
								for (int k=0; k<evculls.getLength();k++){
									Node currEvCNode = evculls.item(k);
									if(!currEvCNode.getNodeName().equals("#text")){
										Element currEvCuller = (Element) xmlDoc.importNode(evculls.item(k), false);
										//if it is an actually culler and has contents, add it
										if (currEvCuller.hasAttribute("class")){
											//create the class
											EventCuller culler = (EventCuller) Class.forName(currEvCuller.getAttribute("class")).newInstance();
											//if it has children, they're arguments
											if (currEvCNode.hasChildNodes()){
												//take the list of args and iterate over it
												NodeList params = currEvCNode.getChildNodes();
												for (int m=0; m<params.getLength();m++){
													Node currPNode = params.item(m);
													//and if the args are useful
													if (!currPNode.getNodeName().equals("#text")){
														//add the args to the culler
														Element currParam = (Element) xmlDoc.importNode(params.item(m), true);
														if (currParam.hasAttribute("name") &&  currParam.hasAttribute("value")){
															culler.setParameter(currParam.getAttribute("name"), currParam.getAttribute("value"));
														}		
													}
												}
											}
											cullers.add(culler); //add the culler to the list
										}
									}
								}
								fd.setCullers(cullers);
							}
							break;
						}
					}
				
					//normalization value
					Element currNormV = (Element) xmlDoc.importNode(normValues.item(i), false);
					fd.setNormBaseline(NormBaselineEnum.valueOf(currNormV.getAttribute("value")));
					
					//normalization factor
					Element currNormF = (Element) xmlDoc.importNode(normFactors.item(i), false);
					fd.setNormFactor(Double.parseDouble(currNormF.getAttribute("value")));	
				} catch (Exception e) {
					successful = false;
					Element currentElement = (Element) xmlDoc.importNode(items.item(i), true);
					
					String[] ignore = {"dis legomena", "yule's k", "simpson's diversity index"};
					boolean print = true;
					for (int j = 0; j < ignore.length; j++)
						if (currentElement.getAttribute("name").equals(ignore[j])) {
							print = false;
						}
					
					if (print)
						Logger.logln(NAME+"Failed to load feature driver: "+currentElement.getAttribute("name"),Logger.LogOut.STDERR);
				}
				
				if (successful) {
					//add the feature driver
					cfd.addFeatureDriver(fd);
				}
			}
		}
		
		/**
		 * Returns the generated cumulative feature driver.
		 * @return
		 * 		The generated cumulative feature driver.
		 */
		public CumulativeFeatureDriver getCumulativeFeatureDriver() {
			return cfd;
		}
		
		private org.w3c.dom.Document avoidAnnoyingBug(DocumentBuilder dom) {
			org.w3c.dom.Document xmlDoc = null;
			
			String path = "tmp.xml";
			File xml = new File(path);
			PrintWriter pw;
			try {
				pw = new PrintWriter(xml);
				pw.print(type.toXMLString());
				pw.flush();
				pw.close();
				xmlDoc = dom.parse(path);
			} catch (SAXException | IOException e) {
				e.printStackTrace();
				avoidAnnoyingBug(dom);
			}
			
			return xmlDoc;
		}
	}

	/*
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
	}*/
}
