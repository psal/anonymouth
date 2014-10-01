package edu.drexel.psal.anonymouth.gooie;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellEditor;

import com.jgaap.generics.Document;

import edu.drexel.psal.ANONConstants;
import edu.drexel.psal.anonymouth.helpers.FileHelper;
import edu.drexel.psal.anonymouth.helpers.ImageLoader;
import edu.drexel.psal.jstylo.generics.Logger;
import edu.drexel.psal.jstylo.generics.ProblemSet;

/**
 * The new home for the preProces panel. It is now acting as a set-up wizard as opposed to a main component in GUIMain based on user
 * feedback.
 * 
 * This is the "Window" class for preProcessing, which just handles creating and adding swing components and "isSomething"/"hasSomething"
 * methods pertaining to the problem set. As a guideline, any sort of "update" methods should be handled in the "Driver" class though as a
 * rule all listeners/events should be handled in the "Driver" class. 
 * 
 * @author Marc Barrowclift
 *
 */
public class PreProcessWindow extends JDialog {

	//Constants
	private final static String NAME = "( PreProcessWindow ) - ";
	protected final String DEFAULT_TRAIN_TREE_NAME = "Authors";
	private final String EMPTY_BAR = "empty.png";
	private final String THIRD_BAR = "third.png";
	private final String TWO_THIRD_BAR = "twoThird.png";
	private final String FULL = "full.png";
	private final Font HELVETICA = new Font("Helvetica", Font.PLAIN, 22);
	
	//Variables
	protected PreProcessDriver driver;
	private static final long serialVersionUID = 1L;
	protected PreProcessAdvancedWindow advancedWindow;
	public ProblemSet ps;
	private int width = 500, height = 410;
	protected Container currentContainer;
	protected boolean saved = false;
	
	/*
	 * This is used to cache the words array of all user sample documents before chunking,
	 * so the user can know before hitting the Start button if there are not enough
	 * words. This cache should be updated in 3 places:
	 *     1) in setLastDocumentSet (in StartWindow)
	 *     2) in loadProblemSet (which is called by setLastDocumentSet, in StartWindow)
	 *     3) When user hits X button on preprocess window
	 */
	protected String[] sampleCache = null;
	
	//Doc to anonymize
	private JPanel testAddRemovePanel;
	protected JPanel testBarPanel;
	protected JLabel emptyBarLabel;
	protected JButton testAddButton;
	protected JButton testRemoveButton;
	private JPanel testMainPanel;
	private JPanel testTextPanel;
	private JPanel testTopPanel;
	private ImageIcon empty;
	private JLabel testLabel;
	private JPanel docPanel;
	protected JTextPane testDocPane;
	protected JScrollPane testDocScrollPane;
	private JPanel testMiddlePanel;
	private JPanel testPrevNextPanel;
	private JPanel testNextPanel;
	protected JButton testPreviousButton;
	protected JButton testNextButton;
	//Other sample documents written by the author
	protected JPanel sampleMainPanel;
	private JPanel sampleTopPanel;
	private JPanel sampleAddRemovePanel;
	protected JPanel sampleBarPanel;
	protected JLabel thirdBarLabel;
	private JLabel sampleLabel;
	protected JButton sampleAddButton;
	protected JButton sampleRemoveButton;
	protected JPanel sampleTextPanel;
	protected DefaultListModel<String> sampleDocsListModel;
	protected JList<String> sampleDocsList;
	private ImageIcon third;
	private JPanel sampleMiddlePanel;
	protected JScrollPane sampleDocsScrollPane;
	private JPanel samplePanel;
	private JPanel samplePrevNextPanel;
	private JPanel sampleNextPanel;
	protected JButton samplePreviousButton;
	protected JButton sampleNextButton;
	protected JPanel sampleStatusPanel;
	protected JLabel sampleStatusLabel;
	//Test documents by other authors
	protected JPanel trainMainPanel;
	protected JPanel trainTopPanel;
	protected JPanel trainAddRemovePanel;
	protected JPanel trainBarPanel;
	protected JTree trainDocsTree;
	protected JScrollPane trainDocsScrollPane;
	protected JLabel twoThirdBarLabel;
	protected JButton trainAddButton;
	protected JButton trainRemoveButton;
	protected JPanel trainTextPanel;
	protected JLabel trainLabel;
	private ImageIcon twoThird;
	private JPanel trainMiddlePanel;
	private JPanel trainPanel;
	private JPanel trainPrevNextPanel;
	private JPanel trainNextPanel;
	protected JButton trainPreviousButton;
	protected JButton trainNextButton;
	protected DefaultMutableTreeNode trainTreeTop;
	protected TreeCellEditor trainCellEditor;
	protected DefaultTreeModel trainTreeModel;
	//Done Panel
	private JPanel doneMainPanel;
	private JPanel doneTopPanel;
	protected JPanel doneBarPanel;
	protected JLabel fullBarLabel;
	protected ImageIcon full;
	private JPanel doneTextPanel;
	private JPanel doneSaveLabelPanel;
	private JPanel doneSaveButtonPanel;
	private JLabel doneLabel;
	private JLabel doneSaveLabel;
	private JPanel doneMiddlePanel;
	private JPanel donePrevDonePanel;
	protected JButton donePreviousButton;
	protected JButton doneDoneButton;
	private JPanel doneDonePanel;
	protected JButton doneSaveButton;
	protected JButton doneButton;
	
	/**
	 * Constructor
	 * @param main - Instance of GUIMain
	 */
	public PreProcessWindow(GUIMain main) {
		super(ThePresident.startWindow, "Anonymouth Set-Up Wizard", Dialog.ModalityType.DOCUMENT_MODAL);
		Logger.logln(NAME+"Preparing the Pre-process window for viewing");
		
		ps = new ProblemSet();
		ps.setTrainCorpusName(DEFAULT_TRAIN_TREE_NAME);
		
		initPanels();
		initWindow();
		
		driver = new PreProcessDriver(this, main);
	}

	/**
	 * Displays the prepared window for viewing
	 */
	public void showWindow() {
		driver.updateBar(this.testBarPanel);
		switchingToTest();
		this.setVisible(true);
	}
	
	/**
	 * Initializes and prepares the Pre-process window for viewing
	 */
	private void initWindow() {
		this.setResizable(false);
		this.setSize(width, height);
		this.setLocationRelativeTo(null);
		this.setVisible(false);
	}
	
	/**
	 * Initializes all the panels
	 */
	private void initPanels() {
		initTestDocPanel();
		initSampleDocPanel();
		initTrainDocPanel();
		initDonePanel();
		
		currentContainer = testMainPanel;
	}
	
	/**
	 * Initializes the Test doc panel, which allows the user to add the document they want to anonymize
	 */
	private void initTestDocPanel() {
		testMainPanel = new JPanel(new BorderLayout());
		testMainPanel.setBorder(new EmptyBorder(10, 5, 5, 5));
		testTopPanel = new JPanel();
		testTopPanel.setLayout(new BoxLayout(testTopPanel, BoxLayout.Y_AXIS));
		
		testBarPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		empty = ImageLoader.getImageIcon(EMPTY_BAR);
		emptyBarLabel = new JLabel(empty);
		emptyBarLabel.setHorizontalAlignment(SwingConstants.CENTER);
		emptyBarLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		testBarPanel.add(emptyBarLabel);
		testTopPanel.add(testBarPanel);
		
		testTextPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		testLabel = new JLabel("<html><center>Enter the document you wish to anonymize</center></html>");
		testLabel.setHorizontalAlignment(SwingConstants.CENTER);
		testLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		testLabel.setFont(HELVETICA);
		testTopPanel.add(Box.createRigidArea(new Dimension(0,20)));
		testTextPanel.add(testLabel);
		testTopPanel.add(testTextPanel);
		
		docPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		testDocPane = new JTextPane();
		testDocPane.setEditable(false);
		testDocScrollPane = new JScrollPane(testDocPane);
		testDocScrollPane.setPreferredSize(new Dimension((int)(width*.9), (int)(height*.558)));
		docPanel.add(testDocScrollPane);
		
		testAddRemovePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 5));
		testRemoveButton = new JButton("-");
		testAddRemovePanel.add(testRemoveButton);
		testAddButton = new JButton("+");
		testAddRemovePanel.add(testAddButton);
		
		testMiddlePanel = new JPanel(new BorderLayout());
		testMiddlePanel.add(docPanel, BorderLayout.NORTH);
		testMiddlePanel.add(testAddRemovePanel, BorderLayout.SOUTH);
		testTopPanel.add(testMiddlePanel);

		testNextButton = new JButton("Next");
		testPreviousButton = new JButton("Previous");
		testPreviousButton.setAlignmentX(Container.LEFT_ALIGNMENT);
		testPrevNextPanel = new JPanel();
		testPrevNextPanel.setLayout(new BoxLayout(testPrevNextPanel, BoxLayout.X_AXIS));
		testPrevNextPanel.add(testPreviousButton);
		testNextButton.setAlignmentX(Container.RIGHT_ALIGNMENT);
		testNextPanel = new JPanel();
		testNextPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		testNextPanel.add(testNextButton);
		testPrevNextPanel.add(testNextPanel);
		
		testMainPanel.add(testTopPanel, BorderLayout.NORTH);
		testMainPanel.add(testPrevNextPanel, BorderLayout.SOUTH);
		this.add(testMainPanel);
	}
	
	/**
	 * Initializes the Sample Docs panel, which allows the user to add other documents they have written to a list
	 */
	private void initSampleDocPanel() {
		sampleMainPanel = new JPanel();
		sampleMainPanel.setBorder(new EmptyBorder(10, 5, 5, 5));
		sampleMainPanel.setLayout(new BoxLayout(sampleMainPanel, BoxLayout.Y_AXIS));
		sampleTopPanel = new JPanel();
		sampleTopPanel.setLayout(new BoxLayout(sampleTopPanel, BoxLayout.Y_AXIS));
		
		sampleBarPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		third = ImageLoader.getImageIcon(THIRD_BAR);
		thirdBarLabel = new JLabel(third);
		thirdBarLabel.setHorizontalAlignment(SwingConstants.CENTER);
		thirdBarLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		sampleBarPanel.add(thirdBarLabel);
		sampleTopPanel.add(sampleBarPanel);
		
		sampleTextPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		sampleLabel = new JLabel("<html><center>Enter at least two other<br>documents you've written.</center></html>");
		sampleLabel.setHorizontalAlignment(SwingConstants.CENTER);
		sampleLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		sampleLabel.setFont(HELVETICA);
		sampleTopPanel.add(Box.createRigidArea(new Dimension(0,20)));
		sampleTextPanel.add(sampleLabel);
		sampleTopPanel.add(sampleTextPanel);
		
		samplePanel = new JPanel(); //new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		samplePanel.setLayout(new BoxLayout(samplePanel, BoxLayout.Y_AXIS));
		sampleDocsListModel = new DefaultListModel<String>();
		sampleDocsList = new JList<String>(sampleDocsListModel);
		sampleDocsScrollPane = new JScrollPane(sampleDocsList);
		sampleDocsScrollPane.setPreferredSize(new Dimension((int)(width*.9), (int)(height*.5)));
		samplePanel.add(sampleDocsScrollPane);
		
		sampleAddRemovePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 5));
		sampleRemoveButton = new JButton("-");
		sampleAddRemovePanel.add(sampleRemoveButton);
		sampleAddButton = new JButton("+");
		sampleAddRemovePanel.add(sampleAddButton);
		
		sampleMiddlePanel = new JPanel();
		sampleMiddlePanel.setLayout(new BoxLayout(sampleMiddlePanel, BoxLayout.Y_AXIS));
		sampleMiddlePanel.add(samplePanel);
		sampleMiddlePanel.add(sampleAddRemovePanel);
		sampleTopPanel.add(sampleMiddlePanel);
		
		sampleNextButton = new JButton("Next");
		samplePreviousButton = new JButton("Previous");
		samplePreviousButton.setAlignmentX(Container.LEFT_ALIGNMENT);
		samplePrevNextPanel = new JPanel();
		samplePrevNextPanel.setLayout(new BoxLayout(samplePrevNextPanel, BoxLayout.X_AXIS));
		samplePrevNextPanel.add(samplePreviousButton);
		sampleNextButton.setAlignmentX(Container.RIGHT_ALIGNMENT);
		sampleNextPanel = new JPanel();
		sampleNextPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		sampleNextPanel.add(sampleNextButton);
		samplePrevNextPanel.add(sampleNextPanel);
		
		sampleMainPanel.add(sampleTopPanel);
		sampleMainPanel.add(samplePrevNextPanel);
		
		sampleStatusPanel = new JPanel();
		sampleStatusPanel.setBorder(
				BorderFactory.createCompoundBorder(new EmptyBorder(5, 0, 0, 0), new BevelBorder(BevelBorder.LOWERED)));
		sampleStatusPanel.setLayout(new GridLayout());
		sampleStatusLabel = new JLabel("0/" + ANONConstants.REQUIRED_NUM_OF_WORDS + " words loaded.");
		sampleStatusLabel.setForeground(Color.RED);
		sampleStatusLabel.setHorizontalAlignment(SwingConstants.LEFT);
		sampleStatusPanel.add(sampleStatusLabel);
		sampleMainPanel.add(sampleStatusPanel);
	}
	
	/**
	 * Initializes the Train doc panel, which allows the user to add other user's documents in a tree for Anonymouth to test against
	 */
	private void initTrainDocPanel() {
		trainMainPanel = new JPanel(new BorderLayout());
		trainMainPanel.setBorder(new EmptyBorder(10, 5, 5, 5));
		trainTopPanel = new JPanel();
		trainTopPanel.setLayout(new BoxLayout(trainTopPanel, BoxLayout.Y_AXIS));
		
		trainBarPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		twoThird = ImageLoader.getImageIcon(TWO_THIRD_BAR);
		twoThirdBarLabel = new JLabel(twoThird);
		twoThirdBarLabel.setHorizontalAlignment(SwingConstants.CENTER);
		twoThirdBarLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		trainBarPanel.add(twoThirdBarLabel);
		trainTopPanel.add(trainBarPanel);
		
		trainTextPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		trainLabel = new JLabel("<html><center>Enter at least 3 different authors with<br>" +
				"at least 2 different documents each</center></html>");
		trainLabel.setHorizontalAlignment(SwingConstants.CENTER);
		trainLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		trainLabel.setFont(HELVETICA);
		trainTopPanel.add(Box.createRigidArea(new Dimension(0,20)));
		trainTextPanel.add(trainLabel);
		trainTopPanel.add(trainTextPanel);
		
		trainPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		trainTreeTop = new DefaultMutableTreeNode(ps.getTrainCorpusName(), true);
		trainTreeModel = new DefaultTreeModel(trainTreeTop, true);
		trainDocsTree = new JTree(trainTreeModel);
		trainDocsTree.setEditable(true);
		trainCellEditor = trainDocsTree.getCellEditor();
		trainDocsScrollPane = new JScrollPane(trainDocsTree);
		trainDocsScrollPane.setPreferredSize(new Dimension((int)(width*.9), (int)(height*.5)));
		trainPanel.add(trainDocsScrollPane);
		
		trainAddRemovePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 5));
		trainRemoveButton = new JButton("-");
		trainAddRemovePanel.add(trainRemoveButton);
		trainAddButton = new JButton("+");
		trainAddRemovePanel.add(trainAddButton);
		
		trainMiddlePanel = new JPanel(new BorderLayout());
		trainMiddlePanel.add(trainPanel, BorderLayout.NORTH);
		trainMiddlePanel.add(trainAddRemovePanel, BorderLayout.SOUTH);
		trainTopPanel.add(trainMiddlePanel);
		
		trainNextButton = new JButton("Next");
		trainPreviousButton = new JButton("Previous");
		trainPreviousButton.setAlignmentX(Container.LEFT_ALIGNMENT);
		trainPrevNextPanel = new JPanel();
		trainPrevNextPanel.setLayout(new BoxLayout(trainPrevNextPanel, BoxLayout.X_AXIS));
		trainPrevNextPanel.add(trainPreviousButton);
		trainNextButton.setAlignmentX(Container.RIGHT_ALIGNMENT);
		trainNextPanel = new JPanel();
		trainNextPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		trainNextPanel.add(trainNextButton);
		trainPrevNextPanel.add(trainNextPanel);
		
		trainMainPanel.add(trainTopPanel, BorderLayout.NORTH);
		trainMainPanel.add(trainPrevNextPanel, BorderLayout.SOUTH);
	}
	
	/**
	 * Initializes the "done" panel, which tells the user that their document set is complete and whether or not they'd like to save it
	 */
	private void initDonePanel() {
		doneMainPanel = new JPanel(new BorderLayout());
		doneMainPanel.setBorder(new EmptyBorder(10, 5, 5, 5));
		doneTopPanel = new JPanel();
		doneTopPanel.setLayout(new BoxLayout(doneTopPanel, BoxLayout.Y_AXIS));
		
		doneBarPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		full = ImageLoader.getImageIcon(FULL);
		fullBarLabel = new JLabel(full);
		fullBarLabel.setHorizontalAlignment(SwingConstants.CENTER);
		fullBarLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		doneBarPanel.add(fullBarLabel);
		doneTopPanel.add(doneBarPanel);
		
		doneTextPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		doneLabel = new JLabel("<html><center>Document set complete!</center></html>");
		doneLabel.setHorizontalAlignment(SwingConstants.CENTER);
		doneLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		doneLabel.setFont(HELVETICA);
		doneTopPanel.add(Box.createRigidArea(new Dimension(0,20)));
		doneTextPanel.add(doneLabel);
		doneTopPanel.add(doneTextPanel);
		doneTopPanel.add(Box.createRigidArea(new Dimension(0,80)));
		
		doneSaveLabelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		doneSaveLabel = new JLabel("<html><center>Save your document set for quick<br>loading and starting in the future</center></html>");
		doneSaveLabel.setHorizontalAlignment(SwingConstants.CENTER);
		doneSaveLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		doneSaveLabel.setFont(HELVETICA);
		doneSaveLabelPanel.add(doneSaveLabel);
		
		doneSaveButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 5));
		doneSaveButton = new JButton("Save");
		doneSaveButtonPanel.add(doneSaveButton);
		
		doneMiddlePanel = new JPanel(new BorderLayout());
		doneMiddlePanel.add(doneSaveLabelPanel, BorderLayout.NORTH);
		doneMiddlePanel.add(doneSaveButtonPanel, BorderLayout.SOUTH);
		doneTopPanel.add(doneMiddlePanel);
		
		doneDoneButton = new JButton("Done");
		donePreviousButton = new JButton("Previous");
		
		donePreviousButton.setAlignmentX(Container.LEFT_ALIGNMENT);
		donePrevDonePanel = new JPanel();
		donePrevDonePanel.setLayout(new BoxLayout(donePrevDonePanel, BoxLayout.X_AXIS));
		donePrevDonePanel.add(donePreviousButton);
		
		doneDonePanel = new JPanel();
		doneDonePanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		doneDonePanel.add(doneDoneButton);
		donePrevDonePanel.add(doneDonePanel);
		
		doneMainPanel.add(doneTopPanel, BorderLayout.NORTH);
		doneMainPanel.add(donePrevDonePanel, BorderLayout.SOUTH);
	}
	
	/**
	 * Get the cached array of words in the user's sample documents.
	 * @return An array of words within the user's sample documents.
	 */
	public String[] getSampleCache() {
		return sampleCache;
	}
	
	protected void updateSampleStatus() {
		String[] words = getSampleCache();
		int currentWordCount = words == null ? 0 : words.length;
		int requiredWordCount = ANONConstants.REQUIRED_NUM_OF_WORDS;
		sampleStatusLabel.setText(currentWordCount + "/" + requiredWordCount + " words loaded.");
		if (currentWordCount < requiredWordCount) {
			sampleStatusLabel.setForeground(Color.RED);
		} else {
			sampleStatusLabel.setForeground(Color.BLACK);
		}
	}
	
	/**
	 * Goes through all titles in the train docs part of the problem set and changes the Anonymouth-reference titles of those that
	 * Have the same title
	 * 
	 * This is needed since, while we do guide the user to rename files of the same name for a given author, we want to allow them
	 * To enter documents of the same name for two different authors (sort of like in file systems how I can have two documents of
	 * the same name in two different directories). This helps make things less confusing/irritating for the user
	 */
	protected void assertUniqueTitles() {
		List<Document> docs = ps.getAllTrainDocs();
		int size = docs.size();
		
		ArrayList<String> docTitles = new ArrayList<String>(size);
		
		for (int i = 0; i < size; i++) {
			docTitles.add(docs.get(i).getTitle());
		}
		
		//printTitles();
		for (int d = 0; d < size; d++) {
			
			String oldTitle = docs.get(d).getTitle();
			String newTitle = oldTitle;
			String author = docs.get(d).getAuthor();
			int addNum = 1;
			
			//we don't want to do anything below unless it's a duplicate
			if (docTitles.contains(newTitle)) {
				while (docTitles.contains(newTitle)) {
					newTitle = newTitle.replaceAll(" copy_\\d*.[Tt][Xx][Tt]|.[Tt][Xx][Tt]", "");
					newTitle = newTitle.concat(" copy_"+Integer.toString(addNum)+".txt");
					addNum++;
				}

				ps.removeTrainDocAt(author, oldTitle);
				if (ps.getAuthorMap().get(author) == null)
					ps.addTrainDocs(author, new ArrayList<Document>());
				ps.addTrainDoc(author, new Document(docs.get(d).getFilePath(), author, newTitle));
				
				docTitles.remove(oldTitle);
				docTitles.add(newTitle);
				driver.titles.get(author).remove(oldTitle);
				driver.titles.get(author).add(newTitle);
			}
		}

		//printTitles();
	}
	
	/**
	 * Sets the sample document word cache.
	 * Will contain an array of words within the user's sample documents
	 * 
	 * @return true if the user's sample documents have enough words to proceed.
	 */
	public boolean updateSampleCache() {
		StringBuilder docsCollective = new StringBuilder();
		List<Document> sampleDocs = ps.getSampleDocs();
		if (sampleDocs == null) {
			sampleCache = null;
			return false;
		}
		for (Document doc : sampleDocs) {
			docsCollective.append(FileHelper.readFile(doc.getFilePath()));
			docsCollective.append(" ");
		}
		String docsString = docsCollective.toString();
		if (docsString.isEmpty()) {
			sampleCache = null;
			return false;
		}
		sampleCache = docsString.split("\\s+");
		
		// Alert the user if there are not enough words in their sample documents.
		// Though it seems like it may be good to decouple this logic from any UI stuff..
		//  so return false and let the calling function decide what to do.
		if (sampleCache.length < ANONConstants.REQUIRED_NUM_OF_WORDS) {
			return false;
		}
		return true;
	}
	
	/**
	 * Chunk the docs into separate, temporary docs of 500 words each
	 * @param docs	The documents to split up
	 * @param words (can be null) cached array of words. If null, will recreate from docs.
	 * @return List of Documents pointing to these temp files, which will be deleted when the application terminates
	 */
	public void chunkSampleDocuments() {
		if (null == ps.getSampleDocs()) {
				// || 0 == words.length) {
			updateSampleCache();
		}
		String[] words = getSampleCache();
		if (words.length < ANONConstants.REQUIRED_NUM_OF_WORDS)
			return;
		ArrayList<Document> documentList = new ArrayList<Document>();
		Path tempDir = null;
		try {
			tempDir = Files.createTempDirectory("anonymouth_");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (tempDir == null) {
			Logger.logln("Failed to create temporary directory.");
			return;
		}
		Logger.logln("tempDir created: " + tempDir.toString());
		tempDir.toFile().deleteOnExit();
		
		int maxChunks = words.length / 500 + 1; // will have 1 less if the last chunk is less than 475 words
		for (int i = 0; i < maxChunks; i++) {
			// will chop off anything after the last multiple of 500 (if that last chunk is less than 475 words)
			int nWords = (i < maxChunks - 1) ? 500: words.length % 500;
			if (nWords < 475) // TODO: magic numbers
				break;
			StringBuilder sb = new StringBuilder();
			for (int j = 0; j < nWords; j++) {
				sb.append(words[500 * i + j]);
				sb.append(" ");
			}
			Path tempPath = null;
			try {
				tempPath = Files.createTempFile(tempDir, "sample_docs_", "_" + i + ".txt");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (tempPath == null) {
				Logger.logln("Failed to create temporary file.");
				return;
			}
			File tempFile = tempPath.toFile();
			// have to specify deletion of files in directory AFTER calling on directory (see javadocs)
			tempFile.deleteOnExit();
			
			FileHelper.writeToFile(tempPath.toString(), sb.toString().trim());
			documentList.add(new Document(tempPath.toAbsolutePath().toString(),ProblemSet.getDummyAuthor(),tempFile.getName()));
		}
		
		ps.setTrainDocs(ANONConstants.DUMMY_NAME, documentList);
		//return documentList;
	}
	
	/**
	 * Determines whether or not all the various parts of the document set are ready to begin
	 * @return
	 */
	protected boolean documentsAreReady() {
		boolean ready = true;
		try {
			if (!mainDocReady())
				ready = false;
			if (!sampleDocsReady())
				ready = false;
			if (!trainDocsReady())
				ready = false;
		} catch (Exception e) {
			return false;
		}

		return ready;
	}

	/**
	 * Determines whether or not the user has entered a test document or not
	 * @return
	 */
	protected boolean mainDocReady() {
		if (ps.hasTestDocs())
			return true;
		else
			return false;
	}

	/**
	 * Determines whether or not the user has entered enough sample documents to proceed
	 * @return
	 */
	protected boolean sampleDocsReady() {
		try {
			if (ps.getTrainDocs(ProblemSet.getDummyAuthor()).size() >= 2 && getSampleCache().length >= ANONConstants.REQUIRED_NUM_OF_WORDS)
				return true;
			else
				return false;
		} catch (Exception e) {
			return false;
		}
	}
	
	/**
	 * Determines whether or not the sample documents list is empty
	 * @return
	 */
	protected boolean sampleDocsEmpty() {
		try {
			if (ps.getTrainDocs(ProblemSet.getDummyAuthor()).isEmpty())
				return true;
			else
				return false;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Determines whether or not the user has entered enough train docs to move on
	 * @return
	 */
	protected boolean trainDocsReady() {
		try {
			boolean result = true;
			int size = ps.getAuthors().size();
			int numGoodAuthors = size;
			
			//4 because we want to adjust for the user as one of the authors (which we don't want to count)
			if (size == 0 || size < 2)//!!!! CHANGE BACK TO ORIGINAL (4) - imitation (2)
				result = false;
			else {
				Set<String> authors = ps.getAuthors();
				
				for (String curAuthor: authors) {
					//We don't want to count the main author
					if (curAuthor.equals(ANONConstants.DUMMY_NAME))
						continue;
					
					if (ps.numTrainDocs(curAuthor) < 2) { //!!!CHANGE BACK TO ORIGINAL (2) - imitation(4)
						numGoodAuthors--;
						if(ps.numTrainDocs(curAuthor) <= 0) {
							System.out.println("THE number of trained documents is less than or equal to 0");
							result = false;
							break;
						}
						if (numGoodAuthors < 2) { //!!CHANGE BACK TO ORIGINAL (4) - imitation (2)
							System.out.println("The nubmer of good authors is less than 2");
							result = false;
							break;
						}
					}
				}
			}
			System.out.println("The result is: " + result);
			return result;
		} catch (Exception e) {
			return false;
		}
	}
	
	/**
	 * Determines whether or not the user has entered any train docs
	 * @return
	 */
	protected boolean trainDocsEmpty() {
		try {
			//We don't want to count the author
			if (ps.getAuthors().size() <= 1)
				return true;
			else
				return false;
		} catch (Exception e) {
			return false;
		}
	}
	
	//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	/**
	 * Determines whether or not the user has entered at least three different authors for the train documents
	 * @return
	 */
	protected boolean hasAtLeastThreeOtherAuthors() {
		Set<String> trainAuthors = ps.getAuthors();
		
		if ((trainAuthors == null) || (trainAuthors.size() < 3)) // CHANGE BACK TO 3 !!!!!!!!!!!!!!!!!!!!!! - imitation (1)
			return false;
		else
			System.out.println("Done with the necessay number of authors");
			return true;
	}
	
	/**
	 * To be called only in PreProcessWindowDriver when we are switching to the "test" panel in the set-up wizard
	 */
	protected void switchingToTest() {
		this.remove(currentContainer);
		
		boolean areEnabled = false;
		if (mainDocReady()) {
			areEnabled = true;
			this.getRootPane().setDefaultButton(testNextButton);
		} else {
			this.getRootPane().setDefaultButton(testAddButton);
		}
		
		testPreviousButton.setEnabled(false);
		testRemoveButton.setEnabled(areEnabled);
		testAddButton.setEnabled(!areEnabled);
		testNextButton.setEnabled(areEnabled);
		
		this.add(testMainPanel);
		
		if (areEnabled) {
			testNextButton.requestFocusInWindow();
		} else {
			testAddButton.requestFocusInWindow();
		}
		
		currentContainer = testMainPanel;
	}
	
	/**
	 * To be called only in PreProcessWindowDriver when we are switching to the "sample" panel in the set-up wizard
	 */
	protected void switchingToSample() {
		this.remove(currentContainer);
		
		boolean areEnabled = false;
		if (sampleDocsReady()) {
			areEnabled = true;
			this.getRootPane().setDefaultButton(sampleNextButton);
		} else {
			this.getRootPane().setDefaultButton(sampleAddButton);
		}
		
		samplePreviousButton.setEnabled(true);
		sampleAddButton.setEnabled(true);
		sampleRemoveButton.setEnabled(!sampleDocsEmpty());
		sampleNextButton.setEnabled(areEnabled);
		
		this.add(sampleMainPanel);
		
		if (areEnabled) {
			sampleNextButton.requestFocusInWindow();
		} else {
			sampleAddButton.requestFocusInWindow();
		}
		
		currentContainer = sampleMainPanel;
	}
	
	/**
	 * To be called only in PreProcessWindowDriver when we are switching to the "train" panel in the set-up wizard
	 */
	protected void switchingToTrain() {
		this.remove(currentContainer);
		
		boolean areEnabled = false;
		if (trainDocsReady()) {
			areEnabled = true;
			this.getRootPane().setDefaultButton(trainNextButton);
		} else {
			this.getRootPane().setDefaultButton(trainAddButton);
		}
		
		trainPreviousButton.setEnabled(true);
		trainAddButton.setEnabled(true);
		trainRemoveButton.setEnabled(!trainDocsEmpty());
		trainNextButton.setEnabled(areEnabled);
		
		this.add(trainMainPanel);
		
		if (areEnabled) {
			trainNextButton.requestFocusInWindow();
		} else {
			trainAddButton.requestFocusInWindow();
		}
		
		currentContainer = trainMainPanel;
	}
	
	/**
	 * To be called only in PreProcessWindowDriver when we are switching to the "Done" panel in the set-up wizard
	 */
	protected void switchingToDone() {
		this.remove(currentContainer);

		if (saved) {
			/**
			 * TODO
			 * This doesn't work when the user has a loaded document set, clicks all the way through modify without changing anything,
			 * then clicks modify again and goes through it to the done panel. It doesn't set the default button that time
			 * 
			 * This is most likely due to the address/pointer value of doneDoneButton changing once for some reason:
			 * javax.swing.JButton[,0,0,0x0,invalid,alignmentX=0.0,...
			 * to
			 * javax.swing.JButton[,316,0,77x29,invalid,alignmentX=0.0,...
			 * 
			 * This change happens in the trainNextListener at the call "preProcessWindow.revalidate()". Keep in mind, this only
			 * changes the address/pointer value of the button ONCE for some reason, even though this gets called multiple times.
			 */
			this.getRootPane().setDefaultButton(doneDoneButton);
		} else {
			this.getRootPane().setDefaultButton(doneSaveButton);
		}
		
		this.add(doneMainPanel);

		if (saved) {
			doneDoneButton.requestFocusInWindow();
		} else {
			doneSaveButton.requestFocusInWindow();
		}
		
		currentContainer = doneMainPanel;
	}
	
	/**
	 * Debugging method to see the contents of "titles" to check and make sure it's contents are being updated properly
	 */
	protected void printTitles() {
		Set<String> test = driver.titles.keySet();
		Iterator<String> it = test.iterator();
		
		System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		while (it.hasNext()) {
			String name = (String)it.next();
			List<String> docs = driver.titles.get(name);
			System.out.println("Author = " + name + " Docs = " + Integer.toString(driver.titles.get(name).size()));
			
			for (int i = 0; i < docs.size(); i++) {
				System.out.println("---" + docs.get(i));
			}
		}
	}
}