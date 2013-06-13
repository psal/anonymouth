package edu.drexel.psal.jstylo.generics;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import javax.xml.parsers.*;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

import java.util.Collections;

import com.jgaap.generics.*;

import edu.drexel.psal.jstylo.generics.Logger.LogOut;

public class ProblemSet {
	
	/* ======
	 * fields
	 * ======
	 */
	
	private SortedMap<String,List<Document>> trainDocsMap;
	
	private List<Document> testDocs;
	
	private String trainCorpusName;	
	
	private static String dummyAuthor = "_dummy_"; 
	
	// whether to use the dummy author name for test instances
	// or the default - an arbitrary author name from the training authors
	private boolean useDummyAuthor = false;
	
	/* ============
	 * constructors
	 * ============
	 */
	
	/**
	 * Default constructor for ProblemSet. Initializes training documents map, name, and test document list to be empty.
	 */
	public ProblemSet() {
		trainDocsMap = new TreeMap<String,List<Document>>();
		testDocs = new LinkedList<Document>();
		trainCorpusName = "";
	}
	
	/**
	 * Constructor for ProblemSet. Initializes the training documents map and name to the given ones, and an empty test documents list.
	 * @param trainCorpusName
	 * 		The name of the training corpus.
	 * @param trainDocsMap
	 * 		The map of training documents to set to.
	 */
	public ProblemSet(String trainCorpusName, SortedMap<String,List<Document>> trainDocsMap) {
		this.trainDocsMap = trainDocsMap;
		testDocs = new LinkedList<Document>();
		this.trainCorpusName = trainCorpusName;
	}
	
	/**
	 * Constructor for ProblemSet. Initializes the training documents map and name to be empty, and the test documents list to
	 * the given one.
	 * @param testDocs
	 * 		The test documents list to set to.
	 */
	public ProblemSet(List<Document> testDocs) {
		trainDocsMap = new TreeMap<String,List<Document>>();
		this.testDocs = testDocs;
		trainCorpusName = "";
	}
	
	/**
	 * Constructor for ProblemSet. Initializes the training documents map and the test documents list to the given ones.
	 * @param trainCorpusName
	 * 		The name of the training corpus.
	 * @param trainDocsMap
	 * 		The map of training documents to set to.
	 * @param testDocs
	 * 		The test documents list to set to.
	 */
	public ProblemSet(String trainCorpusName, SortedMap<String,List<Document>> trainDocsMap, List<Document> testDocs) {
		this.trainDocsMap = trainDocsMap;
		this.testDocs = testDocs;
		this.trainCorpusName = trainCorpusName;
	}
	
	/**
	 * Constructor for ProblemSet from a given XML file.
	 * @param filename
	 * 		The name of the XML file to generate the problem set from.
	 * @throws Exception
	 */
	public ProblemSet(String filename) throws Exception {
		XMLParser parser = new XMLParser(filename);
		ProblemSet generated = parser.problemSet;
		trainCorpusName = generated.trainCorpusName;
		trainDocsMap = generated.trainDocsMap;
		testDocs = generated.testDocs;
	}
	
	/**
	 * Copy constructor for ProblemSet.
	 * @param other
	 * 		The ProblemSet to copy from.
	 */
	public ProblemSet(ProblemSet other) {
		// corpus name
		this.trainCorpusName = other.trainCorpusName;
		
		// training docs
		this.trainDocsMap = new TreeMap<String,List<Document>>();
		LinkedList<Document> docs;
		Document newDoc;
		for (String author: other.trainDocsMap.keySet()) {
			docs = new LinkedList<Document>();
			for (Document doc: other.trainDocsMap.get(author)) {
				newDoc = new Document(doc.getFilePath(),doc.getAuthor(),doc.getTitle());
				docs.add(newDoc);
			}
			this.trainDocsMap.put(author, docs);
		}
		
		// test docs
		docs = new LinkedList<Document>();
		for (Document doc: other.testDocs) {
			newDoc = new Document(doc);
			docs.add(newDoc);
		}
		this.testDocs = docs;
	}
	
	
	/* ==========
	 * operations
	 * ==========
	 */
	
	/**
	 * Writes the problem set in XML format into the given filename.
	 * @param filename
	 * 		The filename to save the problem set XML format to.
	 * @throws IOException
	 */
	public void writeToXML(String filename) throws IOException {
		PrintWriter pw = new PrintWriter(new FileWriter(filename), true);
		writeXMLString(pw);
		pw.close();
	}
	
	
	/* =======
	 * setters
	 * =======
	 */
	
	// training documents
	
	/**
	 * Sets the name of the training corpus to the given one.
	 * @param name
	 * 		The name of the training corpus to set to.
	 */
	public void setTrainCorpusName(String name) {
		this.trainCorpusName = name;
	}
	
	/**
	 * Adds the given document to the given author. If no such author exists in the map, creates a new entry for
	 * that author. Returns true iff the addition succeeded.
	 * @param author
	 * 		The author to add the document to.
	 * @param doc
	 * 		The document to be added.
	 * @return
	 * 		true iff the addition succeeded.
	 */
	public boolean addTrainDoc(String author, Document doc) {
		if (trainDocsMap.get(author) == null)
			trainDocsMap.put(author,new LinkedList<Document>());
		return trainDocsMap.get(author).add(doc);
	}
	
	/**
	 * Adds the given documents to the given author, or creates a new author with the given list of documents.
	 * Returns true iff the addition succeeded.
	 * @param author
	 * 		The author to add the documents to / to create.
	 * @param docs
	 * 		The documents to be added.
	 * @return
	 * 		true iff the addition succeeded.
	 */
	public boolean addTrainDocs(String author, List<Document> docs) {
		if (trainDocsMap.get(author) == null) {
			trainDocsMap.put(author,docs);
			return true;
		} else {
			return trainDocsMap.get(author).addAll(docs);
		}	
	}
	
	/**
	 * Adds a random subset of size "docsToAdd" training documents by "author" to the training set.
	 * @param author
	 * 		The author to add the documents to / to create.
	 * @param docs
	 * 		The documents to be added.
	 * @param docsToAdd
	 * 		The number of documents by this author to add to the training set.
	 * @return
	 * 		true iff the addition succeeded.
	 */
	public boolean addRandomTrainDocs(String author, List<Document> docs, int docsToAdd) {
		
		if (trainDocsMap.get(author) == null) {
			
			Collections.shuffle(docs);
			List<Document> newDocsSet = new LinkedList<Document>();
			newDocsSet = docs.subList(0, docsToAdd);
			trainDocsMap.put(author,newDocsSet);
			return true;
		} else {
			// currently does not support adding random set of training documents to an author with pre-existing training docs
			System.out.println("Cannot add random set of training documents to pre-existing author document set.");
			return false;
		}	
	}
	
	/**
	 * Sets the training document list for the given author to be the given list of documents (whether he exists or new). 
	 * @param author
	 * 		The author to set the list of documents to.
	 * @param docs
	 * 		The list of documents to set to.
	 */
	public void setTrainDocs(String author, List<Document> docs) {
		trainDocsMap.put(author, docs);
	}
	
	/**
	 * Removes the given author and returns its list of documents, or null if the author does not exist in the
	 * training set.
	 * @param author
	 * 		The author to be removed.
	 * @return
	 * 		The documents of the removed author, or null if the author does not exist.
	 */
	public List<Document> removeAuthor(String author) {
		return trainDocsMap.remove(author);
	}
	
	/**
	 * Removes the document at the given index from the list of training documents of the given author.
	 * Returns the document that was removed, or null if no such document existed.
	 * @param author
	 * 		The author whose document is to be removed.	 * @param i
	 * @param i
	 * 		The index of the document to be removed.
	 * @return
	 * 		The document that was removed, or null if no such document existed.
	 */
	public Document removeTrainDocAt(String author, int i) {
		List<Document> docs = trainDocsMap.get(author);
		if (docs == null)
			return null;
		try {
			return docs.remove(i);
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}
	
	/**
	 * Removes the given document from the list of training documents of the given author. Returns true iff
	 * the author exists and the document appeared in his list.
	 * @param author
	 * 		The author whose document is to be removed.
	 * @param doc
	 * 		The document to remove.
	 * @return
	 * 		true iff the author exists and the document appeared in his list.
	 */
	public boolean removeTrainDocAt(String author, Document doc) {
		List<Document> docs = trainDocsMap.get(author);
		if (docs == null)
			return false;
		return docs.remove(doc);
	}
	
	/**
	 * Removes the document with the given title from the list of training documents of the given author.
	 * Returns the document that was removed, or null if no such document existed.
	 * @param author
	 * 		The author whose document is to be removed.	 * @param i
	 * @param docTitle
	 * 		The title of the document to be removed.
	 * @return
	 * 		The document that was removed, or null if no such document existed.
	 */
	public Document removeTrainDocAt(String author, String docTitle) {
		List<Document> docs = trainDocsMap.get(author);
		if (docs == null)
			return null;
		for (int i=0; i<docs.size(); i++)
			if (docs.get(i).getTitle().equals(docTitle))
				return docs.remove(i);
		return null;
	}
	
	
	// test documents
		
	/**
	 * Adds the given document to the list of test documents. Returns true iff the addition succeeded.
	 * @param doc
	 * 		The test document to be added.
	 * @return
	 * 		true iff the addition succeeded.
	 */
	public boolean addTestDoc(Document doc) {
		return testDocs.add(doc);
	}
	
	/**
	 * Sets the list of test documents to the given list of documents.
	 * @param docs
	 * 		The list of documents to set to.
	 */
	public void setTestDocs(List<Document> docs) {
		testDocs = docs;
	}
	
	/**
	 * Removes the test document at the given index. Returns the removed document, or null
	 * if no the index is out of bounds.
	 * @param i
	 * 		The index of the test document to be removed.
	 * @return
	 * 		The removed document, or null if the index is out of bounds.
	 */
	public Document removeTestDocAt(int i) {
		try {
			return testDocs.remove(i);
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}
	
	/**
	 * Removes the given document from the list of test documents. Returns true iff the document appeared in the list.
	 * @param doc
	 * 		The test document to be removed.
	 * @return
	 * 		true iff the document appeared in the list.
	 */
	public boolean removeTestDoc(Document doc) {
		return testDocs.remove(doc);
	}
	
	// other
	
	/**
	 * Sets the dummy author name to the given one.
	 * @param dummyAuthor
	 * 		The dummy author name to set to.
	 */
	public static void setDummyAuthor(String dummyAuthor) {
		ProblemSet.dummyAuthor = dummyAuthor;
	}
	
	/**
	 * Sets whether to use dummy author name for the test instances.
	 * @see ProblemSet#usesDummyAuthor()
	 * @param useDummyAuthor
	 * 		Whether to use a summy author name for the test instances.
	 */
	public void useDummyAuthor(boolean useDummyAuthor) {
		this.useDummyAuthor = useDummyAuthor;
	}
	
	
	/* =======
	 * getters
	 * =======
	 */
	
	// training documents
	
	/**
	 * Returns true iff the problem set has any authors.
	 * @return
	 * 		true iff the problem set has any authors.
	 */
	public boolean hasAuthors() {
		return !trainDocsMap.isEmpty();
	}
	
	/**
	 * Returns the set of the authors, or null if it is empty.
	 * @return
	 * 		The set of the authors, or null if it is empty.
	 */
	public Set<String> getAuthors() {
		if (trainDocsMap.keySet().isEmpty())
			return null;
		return trainDocsMap.keySet();
	}
	
	/**
	 * Returns the name of the training corpus.
	 * @return
	 * 		The name of the training corpus.
	 */
	public String getTrainCorpusName() {
		return trainCorpusName;
	}
	
	/**
	 * Returns the mapping from authors to their list of documents.
	 * @return
	 * 		The mapping from authors to their list of documents.
	 */
	public Map<String,List<Document>> getAuthorMap() {
		return trainDocsMap;
	}
	
	/**
	 * Returns the list of documents for the given author.
	 * @param author
	 * 		The given author whose list of documents are returned.
	 * @return
	 * 		The list of documents for the given author.
	 */
	public List<Document> getTrainDocs(String author) {
		return trainDocsMap.get(author);
	}
	
	/**
	 * Returns the number of training documents for the given author, or 0 if no such author exists. 
	 * @param author
	 * 		The input author name.
	 * @return
	 * 		The number of training documents for the given author, or 0 if no such author exists.
	 */
	public int numTrainDocs(String author) {
		if (trainDocsMap.get(author) == null)
			return 0;
		else return trainDocsMap.get(author).size();
	}
	
	/**
	 * Returns the list of all training documents.
	 * @return
	 * 		The list of all training documents.
	 */
	public List<Document> getAllTrainDocs() {
		List<Document> allTrainDocs = new LinkedList<Document>();
		for (String key: trainDocsMap.keySet()){
			for (Document d:trainDocsMap.get(key)){
				try {
					allTrainDocs.add(new Document(d.getFilePath(),key,d.getTitle()));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return allTrainDocs;
	}
	
	/**
	 * Returns the document in the given index of the given author, or null if no such author or document at
	 * that index exist.
	 * @param author
	 * 		The author.
	 * @param i
	 * 		The index of the desired training document. 
	 * @return
	 * 		The document in the given index of the given author, or null if no such author or document at
	 */
	public Document trainDocAt(String author, int i) {
		List<Document> docs = trainDocsMap.get(author);
		if (docs == null)
			return null;
		try {
			return docs.get(i);
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}
	
	/**
	 * Returns the document with the given title of the given author, or null if no such author or document with
	 * that title exist.
	 * @param author
	 * 		The author.
	 * @param docTitle
	 * 		The title of the desired training document. 
	 * @return
	 * 		The document in the given index of the given author, or null if no such author or document at
	 */
	public Document trainDocAt(String author, String docTitle) {
		List<Document> docs = trainDocsMap.get(author);
		if (docs == null)
			return null;
		for (int i=0; i<docs.size(); i++)
			if (docs.get(i).getTitle().equals(docTitle))
				return docs.get(i);
		return null;
	}
	
	// test documents
	
	/**
	 * Returns true iff the list of test documents is not empty.
	 * @return
	 * 		true iff the list of test documents is not empty.
	 */
	public boolean hasTestDocs() {
		return !testDocs.isEmpty();
	}
	
	/**
	 * Returns the list of test documents.
	 * @return
	 * 		The list of test documents.
	 */
	public List<Document> getTestDocs() {
		return testDocs;
	}
	
	/**
	 * Returns the test document at the given index, or null if the index is out of bounds.
	 * @param i
	 * 		The index of the desired test document.
	 * @return
	 * 		The test document at the given index, or null if the index is out of bounds.
	 */
	public Document testDocAt(int i) {
		try {
			return testDocs.get(i);
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}
	
	/**
	 * Returns the number of test documents. 
	 * @return
	 * 		The number of test documents.
	 */
	public int numTestDocs() {
		return testDocs.size();
	}
	
	// stringifiers
	
	/**
	 * Returns a readable String representation of the problem set.
	 */
	public String toString() {
		String res = "Training corpus: "+trainCorpusName+":\n";
		for (String author: trainDocsMap.keySet()) {
			res += "Author "+author+":\n";
			List<Document> docs = trainDocsMap.get(author);
			for (Document doc: docs)
				res += "> "+doc.getTitle()+": "+doc.getFilePath()+"\n";
		}
		res += "Test documents:\n";
		for (Document doc: testDocs)
			res += "> "+doc.getTitle()+": "+doc.getFilePath()+"\n";
		return res;
	}
	
	/**
	 * Returns the XML String representation of the problem set.
	 * @return
	 * 		The XML String representation of the problem set.
	 */
	public String toXMLString() {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		try{
			writeXMLString(pw);
		} catch (IOException e) {
			// should not happen!
		}
		String res = sw.toString();
		pw.close();
		return res;
	}
	
	public String writeXMLString(PrintWriter pw) throws IOException {
		String res = "<?xml version=\"1.0\"?>\n";
		pw.println("<problem-set>");
		pw.println("\t<training name=\""+trainCorpusName+"\">");
		Set<String> sortedAuthors = trainDocsMap.keySet();
		for (String author: sortedAuthors) {
			pw.println("\t\t<author name=\""+author+"\">");
			List<Document> docs = trainDocsMap.get(author);
			for (Document doc : docs) {
				pw.println("\t\t<document title=\"" + doc.getTitle() + "\">"
						+ buildRelativePath(doc) + "</document>");
/*
				pw.println("\t\t<document title=\"" + doc.getTitle() + "\">"
						+ doc.getFilePath().replace('\\', '/') + "</document>");
*/
			}
			pw.println("\t\t</author>");
		}
		pw.println("\t</training>");
		pw.println("\t<test>");
		for (Document doc : testDocs) {

/*
			pw.println("\t\t<document title=\"" + doc.getTitle() + "\">"
					+ doc.getFilePath().replace('\\', '/') + "</document>");
*/
			pw.println("\t\t<document title=\"" + doc.getTitle() + "\">"
					+ buildRelativePath(doc) + "</document>");
		}
		pw.println("\t</test>");
		pw.println("</problem-set>");
		
		return res;
	}
	
	// other
	
	/**
	 * Returns the dummy author name.
	 * @return
	 * 		The dummy author name.
	 */
	public static String getDummyAuthor() {
		return dummyAuthor;
	}
	
	/**
	 * Returns whether a dummy author name is used for the test instances.
	 * If it is not used, an arbitrary training author name is used.
	 * @return
	 * 		Whether a dummy author name is used for the test instances.
	 */
	public boolean usesDummyAuthor() {
		return useDummyAuthor;
	}
	
	
	/* ===========
	 * XML parsing
	 * ===========
	 */
	
	/**
	 * Tag to indicate the current scope of the XML.
	 */
	private enum Tag{
		PROBLEM_SET,
		TRAINING,
		TEST,
		AUTHOR,
		DOCUMENT,
		END
	}
	
	/**
	 * XML parser to create a problem set out of a XML file.
	 */
	private class XMLParser extends DefaultHandler {
		
		/* ======
		 * fields
		 * ======
		 */
		private ProblemSet problemSet;
		private String filename;
		
		/* ============
		 * constructors
		 * ============
		 */
		public XMLParser(String filename) throws Exception {
			problemSet = new ProblemSet();
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
			
			//intialize the parser, parse the document, and build the tree
			DocumentBuilderFactory builder = DocumentBuilderFactory.newInstance();
			DocumentBuilder dom = builder.newDocumentBuilder();
			org.w3c.dom.Document xmlDoc = dom.parse(filename);	
			xmlDoc.getDocumentElement().normalize();
			NodeList items = xmlDoc.getElementsByTagName("document");
			
			for (int i=0; i<items.getLength();i++){
				Node current = items.item(i);
			
				//test document (old format)
				if (current.getParentNode().getNodeName().equals("test")){
					Document testDoc = new Document(current.getTextContent(),null);
					problemSet.addTestDoc(testDoc);
				} 
				//training document
				else if (current.getParentNode().getParentNode().getNodeName().equals("training")){
					Element parent = (Element) xmlDoc.importNode(current.getParentNode(),false);
					Document trainDoc = new Document(current.getTextContent(),parent.getAttribute("name"));
					problemSet.addTrainDoc(parent.getAttribute("name"),trainDoc);
				}
				//test document (new format) <not yet implemented>
				else if (current.getParentNode().getParentNode().getNodeName().equals("test")){
					Logger.logln("Planned for next version of test document handling");
				} else {
					Logger.logln("Error loading document file. Incorrectly formatted XML: "+current.getNodeValue());
				}
			}	
		}
	}
	
	public String buildRelativePath(Document doc){
		String relPath = "";
		String filePath = doc.getFilePath();
		File docum = new File(filePath);
		String docPath = docum.getAbsolutePath();
		docPath = docPath.replaceAll("\\\\","/");
		File here = new File("");
		String dirPath = here.getAbsolutePath();
		dirPath = dirPath.replaceAll("\\\\","/");
		
		String[] docComponents = docPath.split("/");
		String[] dirComponents = dirPath.split("/");
		
		String shared = "";
		int index = 0;
		
		//record all of the common components
		//stop recording them once they diverge, as repeated dir names could appear
		//further down the line
		while (index<docComponents.length && index<dirComponents.length &&
				(docComponents[index].equals(dirComponents[index]))){
			shared+=dirComponents[index]+"/";
			index++;
		}
		
		//If this is 0, then they're on different drives or something like that.
		//Maybe we should try to find a way to get that to work? Perhaps allow people to toggle absolute versus relative paths?
		if (index==0){
			Logger.logln("Failed to build relative path between: "+docPath+" and "+dirPath,LogOut.STDERR);
		}
		
		//Gets the path from the current dir to the point where the current dir and the dest diverge
		if (dirComponents.length != index){
			int backTrack = dirComponents.length - index;
			
			for (int i=0; i<backTrack; i++){
				relPath+="../";
			}
		}
		
		relPath+=docPath.substring(shared.length());
		
		return relPath;
	}
	
	/*
	public static void main(String[] args) throws Exception {
		ProblemSet ps = new ProblemSet("enron_demo.xml");
		System.out.println(ps.toXMLString());
		//ps.writeToXML("d:/tmp/a.xml");
	}*/
}
