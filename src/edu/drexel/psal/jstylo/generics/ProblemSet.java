package edu.drexel.psal.jstylo.generics;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import javax.xml.parsers.*;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Collections;

import com.jgaap.generics.*;

import edu.drexel.psal.ANONConstants;
import edu.drexel.psal.anonymouth.gooie.GUIMain;

public class ProblemSet {
	
	/* ======
	 * fields
	 * ======
	 */
	//private static final String NAME = "( ProblemSet ) - ";
	private SortedMap<String,List<Document>> trainDocsMap;
	
	private SortedMap<String,List<Document>> testDocsMap;
	
	private String trainCorpusName;	
	
	private static String dummyAuthor = ANONConstants.DUMMY_NAME; 
	
	// whether to use the dummy author name for test instances
	// or the default - an arbitrary author name from the training authors
	private boolean useDummyAuthor = false;
	
	/* ============
	 * constructors
	 * ============
	 */
	
	/**
	 * Default constructor for ProblemSet. Initializes training documents map, name, and test document map to be empty
	 */
	public ProblemSet() {
		trainDocsMap = new TreeMap<String,List<Document>>();
		//testDocs = new LinkedList<Document>();
		testDocsMap = new TreeMap<String,List<Document>>();
		trainCorpusName = "";
	}
	
	/**
	 * Constructor for ProblemSet. Initializes the training documents map and name to the given ones, and an empty test documents map.
	 * @param trainCorpusName
	 * 		The name of the training corpus.
	 * @param trainDocsMap
	 * 		The map of training documents to set to.
	 */
	public ProblemSet(String trainCorpusName, SortedMap<String,List<Document>> trainDocsMap) {
		this.trainDocsMap = trainDocsMap;
		//testDocs = new LinkedList<Document>();
		testDocsMap = new TreeMap<String,List<Document>>();
		this.trainCorpusName = trainCorpusName;
	}
	
	/**
	 * Constructor for ProblemSet. Initializes the training documents map and name to be empty, and the test documents map to
	 * the given one.
	 * @param testDocs
	 * 		The test documents map to set to.
	 */
	public ProblemSet(SortedMap<String,List<Document>> testDocs){
		trainDocsMap = new TreeMap<String,List<Document>>();
		//testDocs = new LinkedList<Document>();
		testDocsMap = testDocs;
		trainCorpusName = "";
	}
	
	/**
	 * Constructor for ProblemSet. Initializes the training documents map and the test documents map to the given ones.
	 * @param trainCorpusName
	 * 		The name of the training corpus.
	 * @param trainDocsMap
	 * 		The map of training documents to set to.
	 * @param testDocs
	 * 		The test documents map to set to.
	 */
	public ProblemSet(String trainCorpusName, SortedMap<String,List<Document>> trainDocsMap, SortedMap<String,List<Document>> testDocs) {
		this.trainDocsMap = trainDocsMap;
		//testDocs = new LinkedList<Document>();
		testDocsMap = testDocs;
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
		testDocsMap = generated.testDocsMap;
		
		GUIMain.inst.preProcessWindow.driver.updateOpeningDir(testDocsMap.get(ANONConstants.DUMMY_NAME).get(0).getFilePath(), false);
		GUIMain.inst.preProcessWindow.driver.updateOpeningDir(trainDocsMap.get(trainDocsMap.keySet().toArray()[0]).get(0).getFilePath(), true);
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
		this.testDocsMap = new TreeMap<String,List<Document>>();
		LinkedList<Document> testDocs;
		Document newTestDoc;
		for (String author: other.testDocsMap.keySet()) {
			testDocs = new LinkedList<Document>();
			for (Document doc: other.testDocsMap.get(author)) {
				newTestDoc = new Document(doc.getFilePath(),doc.getAuthor(),doc.getTitle());
				testDocs.add(newTestDoc);
			}
			this.testDocsMap.put(author, testDocs);
		}
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
	 * setters / adders
	 * =======
	 */
	
	/////////////////////// training documents
	
	/**
	 * Sets the name of the training corpus to the given one.
	 * @param name
	 * 		The name of the training corpus to set to.
	 */
	public void setTrainCorpusName(String name) {
		this.trainCorpusName = name;
	}
	
	/**
	 * Adds the given training document to the given training author. If no such author exists in the  training map, 
	 * creates a new entry for that author. Returns true iff the addition succeeded.
	 * @param author
	 * 		The training author to add the document to.
	 * @param doc
	 * 		The training document to be added.
	 * @return
	 * 		true iff the addition succeeded.
	 */
	public boolean addTrainDoc(String author, Document doc) {
		if (trainDocsMap.get(author) == null)
			trainDocsMap.put(author, new LinkedList<Document>());
		return trainDocsMap.get(author).add(doc);
	}

	
	/**
	 * Adds the given training documents to the given training author, or creates a new author with the given list of documents.
	 * Returns true iff the addition succeeded.
	 * @param author
	 * 		The training author to add the documents to / to create.
	 * @param docs
	 * 		The training documents to be added.
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
	 * 		The training author to set the list of documents to.
	 * @param docs
	 * 		The list of documents to set to.
	 */
	public void setTrainDocs(String author, List<Document> docs) {
		trainDocsMap.put(author, docs);
	}
	
	/**
	 * Removes the given training author and returns its list of documents, or null if the author does not exist in the
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
	 * 		The training author whose document is to be removed.
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
	 * 		The training author whose document is to be removed.
	 * @param doc
	 * 		The training document to remove.
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
	 * Removes the training document at the given index 
	 * @param i the index to remove
	 * @return true iff the document was removed successfully
	 */
	public boolean removeTrainDocFromList(int i){
		List<Document> docs = getAllTrainDocs();
		Document doc = docs.get(i);
		return removeTrainDocAt(doc.getAuthor(),doc);
	}
	
	/**
	 * Removes the training document with the given title from the list of training documents of the given author.
	 * Returns the training document that was removed, or null if no such document existed.
	 * @param author
	 * 		The training author whose document is to be removed.
	 * @param docTitle
	 * 		The title of the training document to be removed.
	 * @return
	 * 		The document that was removed, or null if no such document existed.
	 */
	public Document removeTrainDocAt(String author, String docTitle) {
		List<Document> docs = trainDocsMap.get(author);
		if (docs == null)
			return null;

		for (int i = 0; i < docs.size(); i++) {
			if (docs.get(i).getTitle().equals(docTitle)) {
				return docs.remove(i);
			}
		}
		
		return null;
	}
	
	////////////////////////// test documents
	
	/**
	 * Adds the given testing document to the given author. If no such testing author exists in the map,
	 * creates a new entry for that author. Returns true iff the addition succeeded.
	 * @param author
	 * 		The testing author to add the document to.
	 * @param doc
	 * 		The testing document to be added.
	 * @return
	 * 		true iff the addition succeeded.
	 */
	public boolean addTestDoc(String author, Document doc) {
		if (testDocsMap.get(author) == null)
			testDocsMap.put(author,new LinkedList<Document>());
		return testDocsMap.get(author).add(doc);
	}
	
	/**
	 * Adds the given testing document to the list of test documents. Returns true iff the addition succeeded.
	 * @param author
	 * 		The author to add the document to.
	 * @param doc
	 * 		The test document to be added.
	 * @return
	 * 		true iff the addition succeeded.
	 */
	public boolean addTestDocs(String author, List<Document> docs) {
		if (testDocsMap.get(author) == null) {
			testDocsMap.put(author,docs);
			return true;
		} else {
			return testDocsMap.get(author).addAll(docs);
		}	
	}
	
	/**
	 * Removes the given testing author and returns its list of documents, or null if the author does not exist in the
	 * training set.
	 * @param author
	 * 		The testing author to be removed.
	 * @return
	 * 		The documents of the removed author, or null if the author does not exist.
	 */
	public List<Document> removeTestAuthor(String author) {
		return testDocsMap.remove(author);
	}
	
	/**
	 * Sets the list of test documents to the given list of documents.
	 * @param author
	 * 		The author to set the list to
	 * @param docs
	 * 		The list of documents to set to.
	 */
	public void setTestDocs(String author, List<Document> docs) {
		testDocsMap.put(author, docs);
	}
	
	/**
	 * Removes the testing document at the given index 
	 * @param i the index to remove
	 * @return true iff the document was removed successfully
	 */
	public boolean removeTestDocFromList(int i) {
		List<Document> docs = getAllTestDocs();
		Document doc = docs.get(i);
		return removeTestDocAt(doc.getAuthor(),doc);
	}
	
	/**
	 * Removes the test document for the given author at the given index.<br>
	 * returns true if succesful, false if unsuccessful
	 * @param author
	 * 		The author whose document should be removed.
	 * @param i
	 * 		The index of the test document to be removed.
	 * @return
	 * 		The removed document, or null if the index is out of bounds.
	 */
	public boolean removeTestDocAt(String author, Document doc) {
		List<Document> docs = testDocsMap.get(author);
		if (docs == null)
			return false;

		docs.clear();
		return true;
	}
	
	/**
	 * Removes the test document with the given title from the given testing author.<br>
	 * Returns true iff the document appeared in the author's list.
	 * @param author
	 * 		The author whose document should be removed
	 * @param doc
	 * 		The test document to be removed.
	 * @return
	 * 		true iff the document appeared in the list.
	 */
	public Document removeTestDocAt(String author, String docTitle) {
		List<Document> docs = testDocsMap.get(author);
		if (docs == null)
			return null;
		for (int i=0; i<docs.size(); i++)
			if (docs.get(i).getTitle().equals(docTitle))
				return docs.remove(i);
		return null;
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
	 * Removes the authors with the given name from the problem set and then adds it back in with all their
	 * past documents under the given new name
	 * @param oldName - The name of the author you want to rename
	 * @param newName - The new name of the author you want to use
	 */
	public void renameAuthor(String oldName, String newName) {
		List<Document> docs = trainDocsMap.remove(oldName);
		trainDocsMap.put(newName, docs);
	}
	
	/**
	 * Removes the given document under the given author from the problem set and then adds it back in under
	 * it's new name
	 * @param oldName - The name of the document you want to rename
	 * @param newName - The new name of the document you want to use
	 * @param author - The name of author under which the document resides
	 */
	public void renameTrainDoc(String oldName, String newName, String author) {
		List<Document> docs = trainDocsMap.get(author);
		int size = docs.size();
		
		for (int i = 0; i < size; i++) {
			if (docs.get(i).equals(oldName)) {
				String path = docs.get(i).getFilePath();
				docs.remove(i);
				docs.add(i, new Document(path, author, newName));
				break;
			}
		}
	}
	
	/**
	 * Returns true iff the training set has any authors.
	 * @return
	 * 		true iff the training set has any authors.
	 */
	public boolean hasAuthors() {
		return !trainDocsMap.isEmpty();
	}
	
	/**
	 * Returns the set of the training authors, or null if it is empty.
	 * @return
	 * 		The set of the training authors, or null if it is empty.
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
	 * Returns the mapping from training authors to their list of training documents.
	 * @return
	 * 		The mapping from training authors to their list of training documents.
	 */
	public Map<String,List<Document>> getAuthorMap() {
		return trainDocsMap;
	}
	
	/**
	 * Returns the list of training documents for the given author.
	 * @param author
	 * 		The given training author whose list of documents are returned.
	 * @return
	 * 		The list of training documents for the given author.
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
	 * Returns the document in the given index of the given training author, or null if no such author or document at
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
	 * Returns the document with the given title of the given training author, or null if no such author or document with
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
		boolean result;
		
		if (testDocsMap.isEmpty())
			result = false;
		else
			result = !testDocsMap.get(ANONConstants.DUMMY_NAME).isEmpty();
		
		return result;
	}
	
	/**
	 * Returns the sorted map of the testing authors mapped to their list of documents
	 * @return
	 * 		testing authors mapped to their list of documents
	 */	
	public Map<String,List<Document>> getTestAuthorMap(){
		return testDocsMap;
	}
	
	/**
	 * Returns the list of test documents.
	 * @return
	 * 		The list of test documents.
	 */
	public SortedMap<String,List<Document>> getTestDocs() {
		return testDocsMap;
	}
	
	/**
	 * Returns the test document at the given index, or null if the index is out of bounds.
	 * @param author
	 * 		The name of the author to get the testing document from
	 * @param i
	 * 		The index of the desired test document.
	 * @return
	 * 		The test document at the given index, or null if the index is out of bounds.
	 */
	public Document testDocAt(String author, int i) {
		List<Document> docs = testDocsMap.get(author);
		if (docs == null)
			return null;
		try {
			return docs.get(i);
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}
	
	/**
	 * Returns the test document with the given title
	 * @param author
	 * 		The name of the author to get the document from
	 * @param docTitle
	 * 		The name of the document to return
	 * @return
	 * 		The specified document
	 */
	public Document testDocAt(String author, String docTitle) {
		List<Document> docs = testDocsMap.get(author);
		if (docs == null)
			return null;
		for (int i=0; i<docs.size(); i++)
			if (docs.get(i).getTitle().equals(docTitle))
				return docs.get(i);
		return null;
	}
	
	/**
	 * Returns the number of test documents. 
	 * @return
	 * 		The number of test documents.
	 */
	public int numTestDocs(String author) {
		if (testDocsMap.get(author) == null)
			return 0;
		else return testDocsMap.get(author).size();
	}
	
	/**
	 * Returns the list of all of the test documents
	 * @return
	 * 		The lost containing all test documents
	 */
	public List<Document> getAllTestDocs() {
		List<Document> allTestDocs = new LinkedList<Document>();
		for (String key: testDocsMap.keySet()){
			for (Document d:testDocsMap.get(key)){
				try {
					allTestDocs.add(new Document(d.getFilePath(),key,d.getTitle()));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return allTestDocs;
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
		for (String author: testDocsMap.keySet()) {
			res += "Author "+author+":\n";
			List<Document> docs = testDocsMap.get(author);
			for (Document doc: docs)
				res += "> "+doc.getTitle()+": "+doc.getFilePath()+"\n";
		}
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
				pw.println("\t\t\t<document title=\"" + doc.getTitle() + "\">"
						+ buildRelativePath(doc) + "</document>");
			}
			pw.println("\t\t</author>");
		}
		pw.println("\t</training>");
		pw.println("\t<test>");
		Set<String> sortedTestAuthors = testDocsMap.keySet();
		for (String author: sortedTestAuthors) {
			pw.println("\t\t<author name=\""+author+"\">");
			List<Document> docs = testDocsMap.get(author);
			for (Document doc : docs) {
				pw.println("\t\t\t<document title=\"" + doc.getTitle() + "\">"
						+ buildRelativePath(doc) + "</document>");
			}
			pw.println("\t\t</author>");
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
			
			org.w3c.dom.Document xmlDoc = null;
			try {
				xmlDoc = dom.parse(filename);	
			} catch (SAXParseException e) {
				return;
			}
			xmlDoc.getDocumentElement().normalize();
			NodeList items = xmlDoc.getElementsByTagName("document");
			problemSet.trainCorpusName = "Authors";
			HashSet<String> titles = new HashSet<String>();
			
			for (int i=0; i<items.getLength();i++){
				Node current = items.item(i);
			
				//test document (old format)
				if (current.getParentNode().getNodeName().equals("test")){
					Path testPath = Paths.get(current.getTextContent());
					String filePath = testPath.toAbsolutePath().toString().replaceAll("\\\\","/");
					filePath = filePath.replace("/./","/");
					Document testDoc = new Document(filePath, ANONConstants.DUMMY_NAME);
					
					if (titles.contains(testDoc.getTitle())) {
						int addNum = 1;

						String newTitle = testDoc.getTitle();
						while (titles.contains(newTitle)) {
							newTitle = newTitle.replaceAll("_\\d*.[Tt][Xx][Tt]|.[Tt][Xx][Tt]", "");
							newTitle = newTitle.concat("_"+Integer.toString(addNum)+".txt");
							addNum++;
						}
						
						testDoc.setTitle(newTitle);
					}
					
					titles.add(testDoc.getTitle());
					
					problemSet.addTestDoc(ANONConstants.DUMMY_NAME,testDoc);
					//Training document
				} else if (current.getParentNode().getParentNode().getNodeName().equals("training")){
					Element parent = (Element) xmlDoc.importNode(current.getParentNode(),false);
					Path trainPath = Paths.get(current.getTextContent());
					String filePath = trainPath.toAbsolutePath().toString().replaceAll("\\\\","/");
					filePath = filePath.replace("/./","/");
					Document trainDoc = new Document(filePath,parent.getAttribute("name"));
					
					if (titles.contains(trainDoc.getTitle())) {
						int addNum = 1;
						
						String newTitle = trainDoc.getTitle();
						while (titles.contains(newTitle)) {
							newTitle = newTitle.replaceAll("_\\d*.[Tt][Xx][Tt]|.[Tt][Xx][Tt]", "");
							newTitle = newTitle.concat("_"+Integer.toString(addNum)+".txt");
							addNum++;
						}
						
						trainDoc.setTitle(newTitle);
					}
					
					titles.add(trainDoc.getTitle());
					
					problemSet.addTrainDoc(parent.getAttribute("name"),trainDoc);
					//test document (new format)
				} else if (current.getParentNode().getParentNode().getNodeName().equals("test")){
					Element parent = (Element) xmlDoc.importNode(current.getParentNode(),false);
					Path testPath = Paths.get(current.getTextContent());
					String filePath = testPath.toAbsolutePath().toString().replaceAll("\\\\","/");
					filePath = filePath.replace("/./","/");
					Document testDoc = new Document(filePath,parent.getAttribute("name"));
					
					if (titles.contains(testDoc.getTitle())) {
						int addNum = 1;

						String newTitle = testDoc.getTitle();
						while (titles.contains(newTitle)) {
							newTitle = newTitle.replaceAll("_\\d*.[Tt][Xx][Tt]|.[Tt][Xx][Tt]", "");
							newTitle = newTitle.concat("_"+Integer.toString(addNum)+".txt");
							addNum++;
						}
						
						testDoc.setTitle(newTitle);
					}
					
					titles.add(testDoc.getTitle());
					
					problemSet.addTestDoc(parent.getAttribute("name"),testDoc);
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
		//Just use absolute paths in this case
		if (index==0){
			return doc.getFilePath();
		}
		
		//Gets the path from the current dir to the point where the current dir and the dest diverge
		//if we're going up, then just use absolute paths.
		if (dirComponents.length != index){
			int backTrack = dirComponents.length - index;
			
			if (backTrack>1){
				return docPath;
			}
		}
		
		//Assuming the documents are in a subfolder of the root where the program runs, this should return
		//the relative path
		relPath+="./"+docPath.substring(shared.length());
		
		return relPath;
	}
	
	/*
	public static void main(String[] args) throws Exception {
		ProblemSet ps = new ProblemSet("enron_demo.xml");
		System.out.println(ps.toXMLString());
		//ps.writeToXML("d:/tmp/a.xml");
	}*/
}