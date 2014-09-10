package edu.drexel.psal.anonymouth.gooie;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellEditor;

import com.jgaap.generics.Document;

import edu.drexel.psal.ANONConstants;
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
		sampleMainPanel = new JPanel(new BorderLayout());
		sampleMainPanel.setBorder(new EmptyBorder(10, 5, 5, 5));
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
		sampleLabel = new JLabel("<html><center>Enter at least 2 other documents<br>written by you</center></html>");
		sampleLabel.setHorizontalAlignment(SwingConstants.CENTER);
		sampleLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		sampleLabel.setFont(HELVETICA);
		sampleTopPanel.add(Box.createRigidArea(new Dimension(0,20)));
		sampleTextPanel.add(sampleLabel);
		sampleTopPanel.add(sampleTextPanel);
		
		samplePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
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
		
		sampleMiddlePanel = new JPanel(new BorderLayout());
		sampleMiddlePanel.add(samplePanel, BorderLayout.NORTH);
		sampleMiddlePanel.add(sampleAddRemovePanel, BorderLayout.SOUTH);
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
		
		sampleMainPanel.add(sampleTopPanel, BorderLayout.NORTH);
		sampleMainPanel.add(samplePrevNextPanel, BorderLayout.SOUTH);
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
			if (ps.getTrainDocs(ProblemSet.getDummyAuthor()).size() >= 2)
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