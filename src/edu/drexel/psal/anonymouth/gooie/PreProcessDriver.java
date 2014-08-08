package edu.drexel.psal.anonymouth.gooie;

import java.awt.Dialog.ModalityType;
import java.awt.FileDialog;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import com.jgaap.generics.Document;

import edu.drexel.psal.ANONConstants;
import edu.drexel.psal.anonymouth.helpers.FileHelper;
import edu.drexel.psal.anonymouth.helpers.ScrollToTop;
import edu.drexel.psal.jstylo.generics.Logger;
import edu.drexel.psal.jstylo.generics.ProblemSet;
import edu.drexel.psal.jstylo.generics.Logger.LogOut;

/**
 * The corresponding "Driver" class for the "Window" PreProcessWindow.
 * Handles all listeners and most update methods relating to the window
 * 
 * @author Marc Barrowclift
 *
 */
public class PreProcessDriver {
	
	//Constants
	private final static String NAME = "( PreProcessDriver ) - ";
	private final int REPLACE = 0;
	private final int KEEP_BOTH = 1;
	
	private char systemSpecify;//to fix the problem when loading problem set
	private boolean initialProcess;//to fix the problem when loading problem set

	//Variables
	protected HashMap<String, List<String>> titles;
	private GUIMain main;
	private String lastDirectory;
	private String trainDocsDirectory;
	private PreProcessWindow preProcessWindow;
	
	private String[] duplicateName = {"Replace", "Keep Both", "Stop"};
	
	//Swing Components
	protected WindowListener preProcessListener;
	//Main Doc
	protected ActionListener testAddListener;
	protected ActionListener testRemoveListener;
	protected ActionListener testNextListener;
	//Sample Documents
	protected ActionListener sampleAddListener;
	protected ActionListener sampleRemoveListener;
	protected ActionListener samplePreviousListener;
	protected ActionListener sampleNextListener;
	protected KeyListener sampleDeleteListener;
	//Other Authors and Their documents
	protected ActionListener trainAddListener;
	protected ActionListener trainRemoveListener;
	protected ActionListener trainPreviousListener;
	protected ActionListener trainNextListener;
	protected CellEditorListener trainCellListener;
	protected KeyListener trainDeleteListener;
	//Problem set
	protected ActionListener doneSaveListener;
	protected ActionListener donePreviousListener;
	protected ActionListener doneDoneListener;
	
	/**
	 * Constructor
	 * @param preProcessWindow - PreProcessWindow instance
	 * @param advancedWindow - PreProcessAdvancedWindow instance
	 * @param main - GUIMain instance
	 */
	public PreProcessDriver(PreProcessWindow preProcessWindow, GUIMain main) {
		this.main = main;
		this.preProcessWindow = preProcessWindow;
		
		titles = new HashMap<String, List<String>>();
		
		FileHelper.goodLoad = new FileDialog(preProcessWindow);
		FileHelper.goodLoad.setModalityType(ModalityType.DOCUMENT_MODAL);
		FileHelper.goodLoad.setMode(FileDialog.LOAD);
		FileHelper.goodSave = new FileDialog(preProcessWindow);
		FileHelper.goodSave.setModalityType(ModalityType.DOCUMENT_MODAL);
		FileHelper.goodSave.setMode(FileDialog.SAVE);
		
		initListeners();	
	}
	
	public void updateTitles() {
		if (PropertiesUtil.getProbSet().equals("")) {
			titles = new HashMap<String, List<String>>();
		} else {
			titles = preProcessWindow.ps.getTitles();
		}
	}
	
	/**
	 * initializes all the listeners for the various panels in PreProcessWindow
	 */
	public void initListeners() {
		initTestDocPanelListeners();
		initSampleDocPanelListeners();
		initTrainDocPanelListeners();
		initDonePanelListeners();
		
		preProcessListener = new WindowListener() {
			@Override
			public void windowClosing(WindowEvent e) {
				//if the user is closing the window via "X", we should check to see if they have completed the doc set or not
				//and update accordingly
				if (preProcessWindow.documentsAreReady()) {
					if (preProcessWindow.saved)
						ThePresident.startWindow.setReadyToStart(true, true);
					else
						ThePresident.startWindow.setReadyToStart(true, false);
				} else {
					ThePresident.startWindow.setReadyToStart(false, true);
				}
			}
			
			@Override
			public void windowOpened(WindowEvent e) {}
			@Override
			public void windowClosed(WindowEvent e) {}
			@Override
			public void windowIconified(WindowEvent e) {}
			@Override
			public void windowDeiconified(WindowEvent e) {}
			@Override
			public void windowActivated(WindowEvent e) {}
			@Override
			public void windowDeactivated(WindowEvent e) {}
		};
		preProcessWindow.addWindowListener(preProcessListener);
	}
	
	/**
	 * Initializes all the listeners corresponding to the "Test" panel in PreProcessWindow
	 */
	private void initTestDocPanelListeners() {	
		testAddListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Logger.logln(NAME+"'+' button clicked in the Test Doc section of the set-up wizard");
				preProcessWindow.saved = false;
				
				/**
				 * In case something starts to go wrong with the FileDialogs (they are older and
				 * may be deprecated as some point). If this be the case, just swap in this code instead
				 */
				/*
				FileHelper.load = setOpeningDir(FileHelper.load, false);
				FileHelper.load.setName("Load Your Document To Anonymize");
				FileHelper.load.setFileFilter(ANONConstants.TXT);
				FileHelper.load.setFileSelectionMode(JFileChooser.FILES_ONLY);
				FileHelper.load.setMultiSelectionEnabled(false);
				FileHelper.load.setVisible(true);
				int answer = FileHelper.load.showOpenDialog(preProcessWindow);
				
				if (answer == JFileChooser.APPROVE_OPTION) {
					File file = FileHelper.load.getSelectedFile();
					*/
				
				FileHelper.goodLoad.setTitle("Load Your Document To Anonymize");
				FileHelper.goodLoad = setOpeningDir(FileHelper.goodLoad, false);
				FileHelper.goodLoad.setMultipleMode(false);
				FileHelper.goodLoad.setFilenameFilter(ANONConstants.TXT);
				FileHelper.goodLoad.setLocationRelativeTo(null);
				FileHelper.goodLoad.setVisible(true);

				String fileName = FileHelper.goodLoad.getFile();
				if (fileName != null) {
					File file = new File(FileHelper.goodLoad.getDirectory()+fileName);
					Logger.log(NAME+"Trying to load test documents: \n"+"\t\t> "+file.getAbsolutePath()+"\n");
					
					String path = file.getAbsolutePath();
					
					preProcessWindow.ps.addTestDoc(ProblemSet.getDummyAuthor(), new Document(path, ProblemSet.getDummyAuthor(), file.getName()));
					boolean noIssue = updateTestDocPane();
					updateOpeningDir(path, false);
					updateBar(preProcessWindow.testBarPanel);
					preProcessWindow.revalidate();
					preProcessWindow.repaint();	
					
					if (noIssue) {
						preProcessWindow.testAddButton.setEnabled(false);
						preProcessWindow.testRemoveButton.setEnabled(true);
						preProcessWindow.testNextButton.setEnabled(true);
						preProcessWindow.getRootPane().setDefaultButton(preProcessWindow.testNextButton);
						preProcessWindow.testNextButton.requestFocusInWindow();
						
						//main.updateDocLabel(file.getName(), 0);
					}
				} else {
					Logger.logln(NAME+"Load test documents canceled");
				}
			}
		};
		preProcessWindow.testAddButton.addActionListener(testAddListener);

		testRemoveListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Logger.logln(NAME+"'-' button clicked in the Test Doc section of the set-up wizard");
				preProcessWindow.saved = false;
				
				String msg = "Removed test documents:\n";

				msg += "\t\t> "+preProcessWindow.testDocPane.getText()+"\n";
				preProcessWindow.ps.removeTestDocFromList(0);
				preProcessWindow.testDocPane.setText("");

				Logger.log(msg);
				boolean noIssue = updateTestDocPane();
				updateBar(preProcessWindow.testBarPanel);
				preProcessWindow.revalidate();
				preProcessWindow.repaint();	
				
				if (noIssue) {
					preProcessWindow.testAddButton.setEnabled(true);
					preProcessWindow.testRemoveButton.setEnabled(false);
					preProcessWindow.testNextButton.setEnabled(false);
					preProcessWindow.getRootPane().setDefaultButton(preProcessWindow.testAddButton);
					preProcessWindow.testAddButton.requestFocusInWindow();
				}
			}
		};
		preProcessWindow.testRemoveButton.addActionListener(testRemoveListener);
		
		testNextListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				preProcessWindow.switchingToSample();
				updateBar(preProcessWindow.sampleBarPanel);
				preProcessWindow.revalidate();
				preProcessWindow.repaint();	
			}
		};
		preProcessWindow.testNextButton.addActionListener(testNextListener);
	}
	
	/**
	 * Initializes all the listeners corresponding to the "Sample" panel in PreProcessWindow
	 */
	private void initSampleDocPanelListeners() {
		sampleAddListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Logger.logln(NAME+"'+' button clicked in the Sample Docs section of the set-up wizard");
				preProcessWindow.saved = false;
				
				/**
				 * In case something starts to go wrong with the FileDialogs (they are older and
				 * may be deprecated as some point). If this be the case, just swap in this code instead
				 */
				/*
				FileHelper.load = setOpeningDir(FileHelper.load, false);
				FileHelper.load.setName("Load Other Documents Written By You");
				FileHelper.load.setFileFilter(ANONConstants.TXT);
				FileHelper.load.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				FileHelper.load.setMultiSelectionEnabled(true);
				FileHelper.load.setVisible(true);
				int answer = FileHelper.load.showOpenDialog(preProcessWindow);

				if (answer == JFileChooser.APPROVE_OPTION) {
					File[] files = FileHelper.load.getSelectedFiles();
					*/
				
				FileHelper.goodLoad.setTitle("Load Other Documents Written By You");
				FileHelper.goodLoad = setOpeningDir(FileHelper.goodLoad, false);
				FileHelper.goodLoad.setMultipleMode(true);
				FileHelper.goodLoad.setFilenameFilter(ANONConstants.TXT);
				FileHelper.goodLoad.setLocationRelativeTo(null);
				FileHelper.goodLoad.setVisible(true);
				
				File[] files = FileHelper.goodLoad.getFiles();
				if (files.length != 0) {
					String msg = "Trying to load User Sample documents:\n";
					
					for (File file: files)
						msg += "\t\t> "+file.getAbsolutePath()+"\n";
					Logger.log(msg);

					String path = "";
					ArrayList<String> allUserSampleDocPaths = new ArrayList<String>();
					for (Document doc: preProcessWindow.ps.getAllTestDocs())
						allUserSampleDocPaths.add(doc.getFilePath());
					for (Document doc: preProcessWindow.ps.getAllTrainDocs())
						allUserSampleDocPaths.add(doc.getFilePath());
					for (File file: files) {
						path = file.getAbsolutePath();
						if (allUserSampleDocPaths.contains(path))
							continue;

						if (isEmpty(path, file.getName())) {
							continue;
						}
						
						if (titles.get(ProblemSet.getDummyAuthor()) == null) {
							titles.put(ProblemSet.getDummyAuthor(), new ArrayList<String>());
						}
						
						if (titles.get(ProblemSet.getDummyAuthor()).contains(file.getName())) {
							int response = JOptionPane.showOptionDialog(preProcessWindow,
									"An older file named \""+file.getName()+"\" already exists in your\n" +
									"documents. Do you want to replace it with the new one you're moving?",
									"Duplicate Name",
									JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, ThePresident.dialogLogo,
									duplicateName, "Replace");
							
							if (response == REPLACE) {
								preProcessWindow.ps.removeTrainDocAt(ProblemSet.getDummyAuthor(), file.getName());
								preProcessWindow.ps.addTrainDoc(ProblemSet.getDummyAuthor(), new Document(path,ProblemSet.getDummyAuthor(),file.getName()));
								addSampleDoc(file.getName());
							} else if (response == KEEP_BOTH) {
								int addNum = 1;

								String newTitle = file.getName();
								while (titles.get(ProblemSet.getDummyAuthor()).contains(newTitle)) {
									newTitle = newTitle.replaceAll(" copy_\\d*.[Tt][Xx][Tt]|.[Tt][Xx][Tt]", "");
									newTitle = newTitle.concat(" copy_"+Integer.toString(addNum)+".txt");
									addNum++;
								}

								preProcessWindow.ps.addTrainDoc(ProblemSet.getDummyAuthor(), new Document(path, ProblemSet.getDummyAuthor(), newTitle));	
								titles.get(ProblemSet.getDummyAuthor()).add(newTitle);
								addSampleDoc(newTitle);
							} else {
								return;
							}
						} else {
							preProcessWindow.ps.addTrainDoc(ProblemSet.getDummyAuthor(), new Document(path,ProblemSet.getDummyAuthor(),file.getName()));
							titles.get(ProblemSet.getDummyAuthor()).add(file.getName());
							addSampleDoc(file.getName());
						}
					}

					updateOpeningDir(path, false);
					updateBar(preProcessWindow.sampleBarPanel);
					
					if (preProcessWindow.sampleDocsReady()) {
						preProcessWindow.sampleRemoveButton.setEnabled(true);
						preProcessWindow.sampleNextButton.setEnabled(true);
						preProcessWindow.getRootPane().setDefaultButton(preProcessWindow.sampleNextButton);
						preProcessWindow.sampleNextButton.requestFocusInWindow();
					}
					preProcessWindow.revalidate();
					preProcessWindow.repaint();	
				} else {
					Logger.logln(NAME+"Load user sample documents canceled");
				}
			}
		};
		preProcessWindow.sampleAddButton.addActionListener(sampleAddListener);

		sampleRemoveListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Logger.logln(NAME+"'-' button clicked in the Sample Docs section of the set-up wizard");

				if (preProcessWindow.sampleDocsList.isSelectionEmpty()) {
					Logger.logln(NAME+"Failed removing user sample documents - no documents are selected",LogOut.STDERR);
				} else {
					int answer = 0;

					if (answer == JOptionPane.YES_OPTION) {
						sampleRemove();
					} else {
						Logger.logln(NAME+"Removing user sample documents canceled");
					}
				}
			}
		};
		preProcessWindow.sampleRemoveButton.addActionListener(sampleRemoveListener);
		
		samplePreviousListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				preProcessWindow.switchingToTest();
				updateBar(preProcessWindow.testBarPanel);
				preProcessWindow.revalidate();
				preProcessWindow.repaint();
			}
		};
		preProcessWindow.samplePreviousButton.addActionListener(samplePreviousListener);
		
		sampleNextListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				preProcessWindow.switchingToTrain();
				updateBar(preProcessWindow.trainBarPanel);
				preProcessWindow.revalidate();
				preProcessWindow.repaint();	
			}
		};
		preProcessWindow.sampleNextButton.addActionListener(sampleNextListener);
		
		//We want to allow the user to delete list items with their delete key if they so desire
		sampleDeleteListener = new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (KeyEvent.getKeyText(e.getKeyCode()).equals("Backspace")) {
					sampleRemove();
				}
			}
			
			@Override
			public void keyTyped(KeyEvent e) {}
			@Override
			public void keyReleased(KeyEvent e) {}
		};
		preProcessWindow.sampleDocsList.addKeyListener(sampleDeleteListener);
	}
	
	/**
	 * Initializes all the listeners corresponding to the "Train" panel in PreProcessWindow
	 */
	private void initTrainDocPanelListeners() {
		trainAddListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Logger.logln(NAME+"'+' button clicked in the Train Docs section of the set-up wizard");
				preProcessWindow.saved = false;
				
				String author = "no author entered";
				/**
				 * Not happen with Java right now, the only way it seems we can have the chooser accept directories is if we use
				 * the shitty JFileChooser class instead of FileDialog. I'm going for function over form here, but keeping the old
				 * Code below in case we can find a way around it.
				 */
				FileHelper.load.setName("Load Documents By Other Authors");
				FileHelper.load.setFileFilter(ANONConstants.TXT);
				FileHelper.load = setOpeningDir(FileHelper.load, true);
				FileHelper.load.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				FileHelper.load.setMultiSelectionEnabled(true);
				FileHelper.load.setVisible(true);
				int answer = FileHelper.load.showOpenDialog(preProcessWindow);
				
				/*
				FileHelper.goodLoad.setTitle("Load Documents By Other Authors");
				FileHelper.goodLoad = setOpeningDir(FileHelper.goodLoad, true);
				FileHelper.goodLoad.setMultipleMode(true);
				FileHelper.goodLoad.setFilenameFilter(TXT);
				FileHelper.goodLoad.setLocationRelativeTo(null);
				FileHelper.goodLoad.setVisible(true);
				
				File[] files = FileHelper.goodLoad.getFiles();
				if (files.length != 0) {
					*/
				if (answer == JFileChooser.APPROVE_OPTION) {
					File[] files = FileHelper.load.getSelectedFiles();
					String msg = "Trying to load Training documents:\n";

					for (File file: files)
						msg += "\t\t> "+file.getAbsolutePath()+"\n";

					Logger.log(msg);

					String path = "";
					String skipList = "";
					ArrayList<String> allTrainDocPaths = new ArrayList<String>();
					ArrayList<String> allTestDocPaths = new ArrayList<String>();
					boolean directoryMessageShown = false;

					try {
						for (Document doc: preProcessWindow.ps.getTrainDocs(author)) {
							allTrainDocPaths.add(doc.getFilePath());
							Logger.logln(NAME+"Added to Train Docs: " + doc.getFilePath());
						}
					} catch(NullPointerException npe) {
						if (!author.equals("no author entered")) {
							Logger.logln(NAME+"file '"+author+"' was not found.", LogOut.STDERR);
						}
					}

					for (Document doc: preProcessWindow.ps.getAllTestDocs())
						allTestDocPaths.add(doc.getFilePath());
					if (preProcessWindow.ps.getTrainDocs(ProblemSet.getDummyAuthor()) != null) {
						for (Document doc: preProcessWindow.ps.getTrainDocs(ProblemSet.getDummyAuthor()))
							allTestDocPaths.add(doc.getFilePath());
					}
					for (File file: files) {
						boolean isDirectory = false;
						try {
							isDirectory = new File(file.getAbsolutePath()).getCanonicalFile().isDirectory();
						} catch (IOException e1) {
							Logger.logln(NAME+"Failed determining whether directory or not");
							return;
						}

						if (isDirectory) {
							String[] theDocsInTheDir = file.list();
							author = file.getName();
							String pathFirstHalf = file.getAbsolutePath();

							for (String otherFile: theDocsInTheDir) {
								path = pathFirstHalf+File.separator+otherFile;
								File newFile = new File(path);
								
								if (allTrainDocPaths.contains(path)) {
									skipList += "\n"+path+" - Already contained for author "+author;
									continue;
								}

								if (allTestDocPaths.contains(path)) {
									skipList += "\n"+path+" - Already contained as a test document";
									continue;
								}
								
								if (newFile.isDirectory()) {
									if (!directoryMessageShown) {
										Logger.logln("Tried adding directory inside directory, Anonymouth doesn't support this, will skip", LogOut.STDERR);
										JOptionPane.showMessageDialog(preProcessWindow,
												"One or more of the files inside the directory you selected are\n" +
												"also directories and were skipped. Please only select directories\n" +
												"containing just txt files",
												"Directories Skipped", 
												JOptionPane.WARNING_MESSAGE,
												ThePresident.dialogLogo);
										directoryMessageShown = true;
									}
									
									continue;
								}
								
								if (isEmpty(path, newFile.getName())) {
									skipList += "\n"+path+" - File is empty";
									continue;
								}

								if (otherFile.equals(".DS_Store") || path.contains(".svn") ||
										path.contains("imitation") || path.contains("verification") ||
										path.contains("obfuscation") || path.contains("demographics"))
									continue;
								
								if (titles.get(author) == null) {
									titles.put(author, new ArrayList<String>());
									preProcessWindow.ps.addTrainDocs(author, new ArrayList<Document>());
									addTrainNode(author, null, false);
								}

								if (titles.get(author).contains(file.getName())) {
									keepBothOrReplace(author, path, file.getName());
								} else {
									preProcessWindow.ps.addTrainDoc(author, new Document(path,author,newFile.getName()));
									addTrainNode(newFile.getName(), author, true);
									titles.get(author).add(newFile.getName());
								}
							}
						} else {
							path = file.getAbsolutePath();
							if (allTrainDocPaths.contains(path)) {
								skipList += "\n"+path+" - Already contained for author "+author;
								continue;
							}
							if (allTestDocPaths.contains(path)) {
								skipList += "\n"+path+" - Already contained as a test document";
								continue;
							}
							
							if (isEmpty(path, file.getName())) {
								skipList += "\n"+path+" - File is empty";
								continue;
							}

							if (author.equals("no author entered")) {
								author = file.getParentFile().getName();
							}
							
							if (titles.get(author) == null) {
								titles.put(author, new ArrayList<String>());
								addTrainNode(author, null, false);
							}
							
							if (titles.get(author).contains(file.getName())) {
								keepBothOrReplace(author, path, file.getName());
							} else {
								preProcessWindow.ps.addTrainDoc(author, new Document(path,ProblemSet.getDummyAuthor(),file.getName()));
								addTrainNode(file.getName(), author, true);
								titles.get(author).add(file.getName());
							}
						}
					}

					if (!skipList.equals("")) {
						JOptionPane.showMessageDialog(null,
								"Didn't load the following documents:"+skipList,
								"Documents Skipped",
								JOptionPane.WARNING_MESSAGE, ThePresident.dialogLogo);
						Logger.logln(NAME+"Skipped the following training documents:"+skipList);
					}

					updateOpeningDir(path, true);
					updateBar(preProcessWindow.trainBarPanel);
					preProcessWindow.trainRemoveButton.setEnabled(true);
					
					if (preProcessWindow.trainDocsReady()) {
						preProcessWindow.trainNextButton.setEnabled(true);
						preProcessWindow.getRootPane().setDefaultButton(preProcessWindow.trainNextButton);
						preProcessWindow.trainNextButton.requestFocusInWindow();
					}
					preProcessWindow.revalidate();
					preProcessWindow.repaint();	
				} else {
					Logger.logln(NAME+"Load training documents canceled");
				}
			}
		};
		preProcessWindow.trainAddButton.addActionListener(trainAddListener);

		trainRemoveListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Logger.logln(NAME+"'-' button clicked in the Train Docs section of the set-up wizard");
				trainRemove();
			}
		};
		preProcessWindow.trainRemoveButton.addActionListener(trainRemoveListener);
		
		trainPreviousListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				preProcessWindow.switchingToSample();
				updateBar(preProcessWindow.sampleBarPanel);
				preProcessWindow.revalidate();
				preProcessWindow.repaint();
			}
		};
		preProcessWindow.trainPreviousButton.addActionListener(trainPreviousListener);
		
		/**
		 * We want to allow the user to rename authors if they so desire.
		 * NOTE: While we allow them to change the name of any node in the tree, ONLY the author nodes will actually be updated in the
		 * backend to reflect this. This is because there's no real reason to update the backend for file names since the user never sees
		 * them again, and reflecting a renamed root node may break something (not sure). We want to allow them to rename authors though
		 * since they will in fact see them again with the classification results (aka ownership certainty results).
		 */
		trainCellListener = new CellEditorListener() {
			@Override
			public void editingStopped(ChangeEvent e) {
				preProcessWindow.saved = false;
				TreePath path = preProcessWindow.trainDocsTree.getSelectionPath();
				Object[] test = path.getPath();
				String renamedNode = preProcessWindow.trainCellEditor.getCellEditorValue().toString();

				if (test.length == 2) { //renaming author
					String author = ((DefaultMutableTreeNode)test[test.length-1]).toString();
					List<Document> dstrain = preProcessWindow.ps.getTrainDocs(author);
					List<Document> dstest = preProcessWindow.ps.getTestAuthorMap().get(author);
					preProcessWindow.ps.removeAuthor(author);
					for (Document d : dstrain){
						d.setAuthor(renamedNode);
						preProcessWindow.ps.addTrainDoc(renamedNode,d);
					}
					for (Document d : dstest){
						d.setAuthor(renamedNode);
						preProcessWindow.ps.addTestDoc(renamedNode,d);
					}
					List<String> docs = titles.remove(author);
					titles.put(renamedNode, docs);
				}
				//In case we ever do want to let them rename files, here's the code (should work, not sure)
				/*
				else if (test.length == 3) { //renaming a file
					DefaultMutableTreeNode fileNode = (DefaultMutableTreeNode)test[test.length-1];
					DefaultMutableTreeNode authorNode = (DefaultMutableTreeNode)test[test.length-2];
					String file = fileNode.toString();
					String author = authorNode.toString();
					
					if (!renamedNode.matches(".*.[Tt][Xx][Tt]")) {
						renamedNode = renamedNode.concat(".txt");
					}

					((DefaultMutableTreeNode)preProcessWindow.trainCellEditor.getCellEditorValue()).setUserObject(renamedNode);
					preProcessWindow.ps.renameTrainDoc(file, renamedNode, author);
					List<String> docs = titles.get(author);
					int size = docs.size();
					
					for (int i = 0; i < size; i++) {
						if (docs.get(i).equals(file)) {
							docs.remove(i);
							docs.add(i, renamedNode);
							break;
						}
					}
				}
				*/
			}

			@Override
			public void editingCanceled(ChangeEvent e) {}
		};
		preProcessWindow.trainCellEditor.addCellEditorListener(trainCellListener);
		
		//We want to allow the user to delete tree nodes with the delete key if they so desire/expect that functionality
		trainDeleteListener = new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (KeyEvent.getKeyText(e.getKeyCode()).equals("Backspace")) {
					trainRemove();
				}
			}
			
			@Override
			public void keyTyped(KeyEvent e) {}
			@Override
			public void keyReleased(KeyEvent e) {}
		};
		preProcessWindow.trainDocsTree.addKeyListener(trainDeleteListener);
		
		trainNextListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				preProcessWindow.switchingToDone();
				updateBar(preProcessWindow.doneBarPanel);
				preProcessWindow.revalidate();
				preProcessWindow.repaint();
			}
		};
		preProcessWindow.trainNextButton.addActionListener(trainNextListener);
	}
	
	/**
	 * Initializes all the listeners corresponding to the "Done" panel in PreProcessWindow
	 */
	private void initDonePanelListeners() {
		doneSaveListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Logger.logln(NAME+"'Save' button clicked in the Done section of the set-up wizard");

				/**
				 * Neither this nor the save dialog below actually work with the file filter. It seems to only
				 * be when we're saving something, so this very well may be an OS X Java related issue. Regardless,
				 * Here's the JFileChooser version of the code below in case you can get it to work using this one.
				 * 
				 * Basically, whichever one you manage to get the extensions accepted for all major OS's, go with that one.
				 */
				/*
				FileHelper.save.setDialogType(JFileChooser.SAVE_DIALOG);
				FileHelper.save.setFileFilter(ANONConstants.XML);
				FileHelper.save.addChoosableFileFilter(ANONConstants.XML);
				FileHelper.save.setFileSelectionMode(JFileChooser.FILES_ONLY);
				if (!PropertiesUtil.getProbSet().equals("")) {
					FileHelper.save.setSelectedFile(new File(PropertiesUtil.prop.getProperty("recentProbSet")));
					Logger.logln(NAME+"Chooser root directory: " + FileHelper.save.getSelectedFile().getAbsolutePath());
				} else {
					File directory = new File(JSANConstants.JSAN_PROBLEMSETS_PREFIX);
					FileHelper.save.setCurrentDirectory(directory);
				}
				FileHelper.save.setSelectedFile(new File(ThePresident.sessionName+"_docSet.xml"));
				
				int answer = FileHelper.save.showSaveDialog(preProcessWindow);

				if (answer == JFileChooser.APPROVE_OPTION) {
					File file = FileHelper.save.getSelectedFile();
					String path = file.getAbsolutePath();
					*/
				
				FileHelper.goodSave.setTitle("Save Document Set");
				FileHelper.goodSave.setMode(FileDialog.SAVE);
				if (!PropertiesUtil.getProbSet().equals("")) {
					Logger.logln(NAME+"Chooser root directory: " + PropertiesUtil.getProbSet());
					FileHelper.goodSave.setDirectory(PropertiesUtil.getProbSet());
				} else {
					FileHelper.goodSave.setDirectory(new File(ANONConstants.PROBLEMSETS_PREFIX).getAbsolutePath());
					Logger.logln(NAME+"Chooser root directory: " + ANONConstants.PROBLEMSETS_PREFIX);
				}
				FileHelper.goodSave.setFile(ThePresident.sessionName+"_docSet.xml");
				FileHelper.goodSave.setMultipleMode(false);
				FileHelper.goodSave.setFilenameFilter(ANONConstants.XML);
				FileHelper.goodSave.setLocationRelativeTo(null);
				FileHelper.goodSave.setVisible(true);
				
				File[] files = FileHelper.goodSave.getFiles();
				if (files.length != 0) {		
					String path = files[0].getAbsolutePath();
						
					if (!path.toLowerCase().endsWith(".xml"))
						path += ".xml";
					try {
						BufferedWriter bw = new BufferedWriter(new FileWriter(path));
						bw.write(preProcessWindow.ps.toXMLString());
						bw.flush();
						bw.close();
						Logger.logln("Saved problem set to "+path);
						PropertiesUtil.setProbSet(path);
						
						preProcessWindow.saved = true;
						preProcessWindow.getRootPane().setDefaultButton(preProcessWindow.doneDoneButton);
						preProcessWindow.doneDoneButton.requestFocusInWindow();
					} catch (IOException exc) {
						Logger.logln(NAME+"Failed opening "+path+" for writing",LogOut.STDERR);
						Logger.logln(NAME+exc.toString(),LogOut.STDERR);
						JOptionPane.showMessageDialog(preProcessWindow,
								"Anonymouth ran into an issue saving your\n" +
								"document set to the path:\n"+
								path+"\n"+
								"Please verify that you have write\n"+
								"permissions to that directory and try again.",
								"Problem Saving Document Set",
								JOptionPane.ERROR_MESSAGE);
					}
				} else {
					Logger.logln(NAME+"Save problem set cancelled");
				}
			}
		};
		preProcessWindow.doneSaveButton.addActionListener(doneSaveListener);
		
		donePreviousListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				preProcessWindow.switchingToTrain();
				updateBar(preProcessWindow.trainBarPanel);
				preProcessWindow.revalidate();
				preProcessWindow.repaint();	
			}
		};
		preProcessWindow.donePreviousButton.addActionListener(donePreviousListener);
		
		doneDoneListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				preProcessWindow.setVisible(false);
				
				if (preProcessWindow.documentsAreReady()) {
					if (preProcessWindow.saved)
						ThePresident.startWindow.setReadyToStart(true, true);
					else
						ThePresident.startWindow.setReadyToStart(true, false);
				} else {
					ThePresident.startWindow.setReadyToStart(false, true);
				}
			}
		};
		preProcessWindow.doneDoneButton.addActionListener(doneDoneListener);
	}
	
	/**
	 * The code to remove a list item from the Samples JList. It's separate here since both the keyListener and "-" button need to run the
	 * same code 
	 */
	private void sampleRemove() {
		preProcessWindow.saved = false;
		int[] rows = preProcessWindow.sampleDocsList.getSelectedIndices();
		String msg = "Removed test documents:\n";

		for (int i = rows.length-1; i >= 0; i--) {
			msg += "\t\t> "+preProcessWindow.ps.trainDocAt(ProblemSet.getDummyAuthor(),rows[i]).getTitle()+"\n";
			
			String title = preProcessWindow.ps.trainDocAt(ProblemSet.getDummyAuthor(),rows[i]).getTitle();
			titles.get(ProblemSet.getDummyAuthor()).remove(title);
			preProcessWindow.ps.removeTrainDocAt(ProblemSet.getDummyAuthor(), rows[i]);

			removeSampleDoc(title);
		}

		Logger.log(msg);
		updateBar(preProcessWindow.sampleBarPanel);
		
		if (preProcessWindow.sampleDocsEmpty()) {
			preProcessWindow.sampleRemoveButton.setEnabled(false);
		} else if (!preProcessWindow.sampleDocsReady()) {
			preProcessWindow.getRootPane().setDefaultButton(preProcessWindow.sampleAddButton);
			preProcessWindow.sampleAddButton.requestFocusInWindow();
			preProcessWindow.sampleNextButton.setEnabled(false);
		}
		preProcessWindow.revalidate();
		preProcessWindow.repaint();	
	}
	
	/**
	 * The code to remove a node from the Training docs JTREE. It's separate here since both the keyListener and "-" button need to run the
	 * same code 
	 */
	private void trainRemove() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				preProcessWindow.saved = false;
				boolean removingAuthor = false;
				boolean removingAll = false;
				int docCounter = 0;
				TreePath[] paths = preProcessWindow.trainDocsTree.getSelectionPaths();
				List<DefaultMutableTreeNode> selectedDocs = new ArrayList<DefaultMutableTreeNode>();
				
				if (paths != null) {
					if (paths[0].getPath().length == 1) { //Deleting everything
						removingAll = true;
						DefaultMutableTreeNode root = (DefaultMutableTreeNode)paths[0].getPath()[0];
						@SuppressWarnings("unchecked")
						Enumeration<DefaultMutableTreeNode> authors = root.children();
						while (authors.hasMoreElements())
							selectedDocs.add(authors.nextElement());
					} else if (paths[0].getPath().length == 2) { //Deleting author and all their documents
						removingAuthor = true;
						for (TreePath path: paths)
							if (path.getPath().length == 2)
								selectedDocs.add((DefaultMutableTreeNode)path.getPath()[1]);
					} else if (paths[0].getPath().length == 3) { //Deleting document(s)
						for (TreePath path: paths)
							if (path.getPath().length == 3) {
								selectedDocs.add((DefaultMutableTreeNode)path.getPath()[2]);
								docCounter++;
							}
					}
				}

				if (selectedDocs.isEmpty()) {
					Logger.logln(NAME+"Failed removing training documents/authors - no documents/authors are selected",LogOut.STDERR);
				} else {
					String msg;
					
					if (removingAll) {
						msg = "Removed all:\n";
						for (DefaultMutableTreeNode curAuthor: selectedDocs) {
							titles.remove(curAuthor.toString());
							
							preProcessWindow.ps.removeAuthor(curAuthor.toString());
							removeTrainNode(curAuthor.toString(), false);
							msg += "\t\t> " + curAuthor.toString() + "\n";
						}
					} else if (removingAuthor) {
						msg = "Removed author:\n";
						for (DefaultMutableTreeNode author: selectedDocs) {
							titles.remove(author.toString());

							preProcessWindow.ps.removeAuthor(author.toString());
							removeTrainNode(author.toString(), false);
							msg += "\t\t> "+author.toString()+"\n";
						}
					} else {
						msg = "Removed training documents:\n";
						String author;
						for (DefaultMutableTreeNode doc: selectedDocs) {
							author = doc.getParent().toString();

							if (doc.getParent().getChildCount() == docCounter) {
								docCounter = -1;
							}

							String title = preProcessWindow.ps.trainDocAt(author, doc.toString()).getTitle();
							titles.get(author).remove(title);
							
							preProcessWindow.ps.removeTrainDocAt(author, doc.toString());
							removeTrainNode(doc.toString(), true);
							msg += "\t\t> "+doc.toString()+"\n";
						}
					}

					Logger.log(msg);
					updateBar(preProcessWindow.trainBarPanel);
					
					if (preProcessWindow.trainDocsEmpty()) {
						preProcessWindow.trainRemoveButton.setEnabled(false);
					} else if (!preProcessWindow.trainDocsReady()) {
						preProcessWindow.trainNextButton.setEnabled(false);
						preProcessWindow.getRootPane().setDefaultButton(preProcessWindow.trainAddButton);
					}
					
					preProcessWindow.revalidate();
					preProcessWindow.repaint();
				}
			}
		});
	}
	
	/**
	 * Code which supplies a popup to allow the user to replace, keep both files, or cancel when they try adding a file to an author
	 * when one with the same name and different path already exist.
	 * @param author - The name of the author they are trying to add the file to.
	 * @param path - The path of the file
	 * @param name - The name of the file
	 */
	private void keepBothOrReplace(String author, String path, String name) {
		int confirm = JOptionPane.showOptionDialog(preProcessWindow,
				"An older file named \""+name+"\" already exists in author\n" +
				"\""+author+"\"'s documents. Do you want to replace it with the new one you're moving?",
				"Duplicate Name",
				JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, ThePresident.dialogLogo,
				duplicateName, "Replace");
		
		if (confirm == REPLACE) { //Replace
			preProcessWindow.ps.removeTrainDocAt(author, name);
			preProcessWindow.ps.addTrainDoc(author, new Document(path, author, name));
		} else if (confirm == KEEP_BOTH) { //Keep both
			int addNum = 1;

			String newTitle = name;
			while (titles.get(author).contains(newTitle)) {
				newTitle = newTitle.replaceAll(" copy_\\d*.[Tt][Xx][Tt]|.[Tt][Xx][Tt]", "");
				newTitle = newTitle.concat(" copy_"+Integer.toString(addNum)+".txt");
				addNum++;
			}

			if (preProcessWindow.ps.getAuthorMap().get(author) == null) {
				preProcessWindow.ps.addTrainDocs(author, new ArrayList<Document>());
				addTrainNode(author, null, false);
			}
			preProcessWindow.ps.addTrainDoc(author, new Document(path, author, name));
			addTrainNode(name, author, true);
			titles.get(author).add(newTitle);
		} else { //Stop
			return;
		}
	}
	
	/**
	 * Updates the Set-up wizard "Progress" bar to the correct status
	 * @param curBarPanel
	 */
	protected void updateBar(JPanel curBarPanel) {
		JLabel curBar = new JLabel();
		curBarPanel.removeAll();
		int gauge = 0;
		
		if (preProcessWindow.mainDocReady())
			gauge++;
		if (preProcessWindow.sampleDocsReady())
			gauge++;
		if (preProcessWindow.trainDocsReady())
			gauge++;
		
		if (gauge == 3) {
			curBar = preProcessWindow.fullBarLabel;
		} else if (gauge == 2) {
			curBar = preProcessWindow.twoThirdBarLabel;
		} else if (gauge == 1) {
			curBar = preProcessWindow.thirdBarLabel;
		} else {
			curBar = preProcessWindow.emptyBarLabel;
		}
		
		curBarPanel.add(curBar);
	}
	
	/**
	 * Force-updates all GUI tables and text fields. This entails removing all elements from tables and refilling them with
	 * everything from the problem set
	 */
	protected boolean updateAllComponents() {
		Logger.logln(NAME+"Updating components to reflect new document set.");
		/**
		 * 	
		 * know how to make this easier on the eyes please go ahead.
		 */
		boolean result;
		int passed = 0;
		
		if (updateTestDocPane())
			passed++;
		if (updateSampleDocList())
			passed++;
		if (updateTrainDocTree())
			passed++;

		if (passed == 3)
			result = true;
		else
			result = false;
		
		return result;
	}
	
	/**
	 * Clears all components to their default, empty states.
	 * @return
	 */
	protected void resetAllComponents() {
		preProcessWindow.saved = false;
		preProcessWindow.ps = new ProblemSet();
		preProcessWindow.ps.setTrainCorpusName(preProcessWindow.DEFAULT_TRAIN_TREE_NAME);
		PropertiesUtil.setProbSet("");
		
		//Resets the test document textPane
		preProcessWindow.testDocPane.setText("");
		main.mainDocPreview = new Document();
		
		//Reset the sample document list
		DefaultListModel<String> dlm = (DefaultListModel<String>)preProcessWindow.sampleDocsList.getModel();
		dlm.removeAllElements();
		titles.clear();
		
		//Reset the training document table
		preProcessWindow.trainTreeTop = new DefaultMutableTreeNode(preProcessWindow.ps.getTrainCorpusName());
		preProcessWindow.trainTreeModel = new DefaultTreeModel(preProcessWindow.trainTreeTop, true);
		preProcessWindow.trainDocsTree.setModel(preProcessWindow.trainTreeModel);
	}
	
	/**
	 * Updates the test documents table with a new test doc
	 */
	protected boolean updateTestDocPane() {
		boolean passed = true;
		
		if (preProcessWindow.mainDocReady()) {
			main.mainDocPreview = preProcessWindow.ps.getAllTestDocs().get(0);

			try {
				main.mainDocPreview.load();
			} catch (Exception e) {
				if (e.getMessage().equals("Empty Document Error")) {
					Logger.logln(NAME+"User tried to load empty document, will not allow", LogOut.STDERR);
					JOptionPane.showOptionDialog(preProcessWindow,
							"You can't anonymize a blank document, please only\n" +
							"choose a document that contains text.",
							"Empty Document",
									JOptionPane.DEFAULT_OPTION,		
									JOptionPane.ERROR_MESSAGE, null, null, null);
				} else {
					Logger.logln(NAME+"Error updating test doc table", LogOut.STDERR);
					JOptionPane.showOptionDialog(preProcessWindow,
							"An error occurred while tring to load your test document\n" +
									"Perhaps Anonymouth doesn't have correct permissions,\n" +
									"try moving the document to somewhere else and trying again.",
									"Error While Adding Document to Anonymize",
									JOptionPane.DEFAULT_OPTION,		
									JOptionPane.ERROR_MESSAGE, null, null, null);
				}
				
				preProcessWindow.testDocPane.setText("");
				preProcessWindow.ps.removeTestAuthor(ANONConstants.DUMMY_NAME);
				preProcessWindow.testAddButton.setEnabled(true);
				preProcessWindow.testRemoveButton.setEnabled(false);
				preProcessWindow.testNextButton.setEnabled(false);
				preProcessWindow.getRootPane().setDefaultButton(preProcessWindow.testAddButton);
				preProcessWindow.testAddButton.requestFocusInWindow();
				preProcessWindow.ps.removeTestDocAt(main.mainDocPreview.getAuthor(), main.mainDocPreview.getTitle());
				main.mainDocPreview = new Document();
				passed = false;
			}

			try {
				String testDocText = main.mainDocPreview.stringify();
				main.editorDriver.ignoreChanges = true;
				main.documentPane.setText(testDocText);
				main.editorDriver.ignoreChanges = false;
				main.originalDocPane.setText(testDocText);
				preProcessWindow.testDocPane.setText(testDocText);
				if (titles.get(ANONConstants.DUMMY_NAME) == null)
					titles.put(ANONConstants.DUMMY_NAME, new ArrayList<String>());
				titles.get(ANONConstants.DUMMY_NAME).add(main.mainDocPreview.getTitle());
				
				//To Set the Scroll pane vertical bar to the top instead of bottom
				SwingUtilities.invokeLater(new ScrollToTop(new Point(0, 0), preProcessWindow.testDocScrollPane));
			} catch (Exception e) {
				e.printStackTrace();
				Logger.logln(NAME+"Error setting text of test document in editor", LogOut.STDERR);
				JOptionPane.showOptionDialog(preProcessWindow,
						"An error occurred while tring to load your test document\n" +
								"Perhaps Anonymouth doesn't have correct permissions,\n" +
								"try moving the document to somewhere else and trying again.",
								"Error While Adding Document to Anonymize",
								JOptionPane.DEFAULT_OPTION,		
								JOptionPane.ERROR_MESSAGE, null, null, null);
				preProcessWindow.testDocPane.setText("");
				preProcessWindow.ps.removeTestAuthor(ANONConstants.DUMMY_NAME);
				preProcessWindow.testAddButton.setEnabled(true);
				preProcessWindow.testRemoveButton.setEnabled(false);
				preProcessWindow.testNextButton.setEnabled(false);
				preProcessWindow.getRootPane().setDefaultButton(preProcessWindow.testAddButton);
				preProcessWindow.testAddButton.requestFocusInWindow();
				preProcessWindow.ps.removeTestDocAt(main.mainDocPreview.getAuthor(), main.mainDocPreview.getTitle());
				main.mainDocPreview = new Document();
				passed = false;
			}
		}
		
		return passed;
	}
	
	/**
	 * Updates the User Sample documents table for the new problem set (should not be called by directly by programmer, instead call
	 * updateAllComponents(), and only when loading a new problem set)
	 */
	protected boolean updateSampleDocList() {
		boolean passed = true;
		DefaultListModel<String> dlm = (DefaultListModel<String>)preProcessWindow.sampleDocsList.getModel();
		dlm.removeAllElements();

		if (!preProcessWindow.sampleDocsEmpty()) {
			List<Document> userSampleDocs = preProcessWindow.ps.getTrainDocs(ProblemSet.getDummyAuthor());
			if (userSampleDocs == null) {
				passed = false;
			} else {
				int size = userSampleDocs.size();
				
				for (int i = 0; i < size; i++) {
					if (isEmpty(userSampleDocs.get(i).getFilePath(), userSampleDocs.get(i).getTitle())) {
						passed = false;
						dlm.removeElementAt(i);
						preProcessWindow.ps.removeTrainDocAt(ProblemSet.getDummyAuthor(), userSampleDocs.get(i).getTitle());
					} else {
						dlm.addElement(userSampleDocs.get(i).getTitle());
						if (titles.get(ANONConstants.DUMMY_NAME) == null)
							titles.put(ANONConstants.DUMMY_NAME, new ArrayList<String>());
						titles.get(ANONConstants.DUMMY_NAME).add(userSampleDocs.get(i).getTitle());
					}
				}
			}
		}
		
		if (!passed) {
			Logger.logln(NAME+"Problem reading the sample docs from the saved problem set. " +
					"Ether it doesn't exist, we don't have permissions, or it's empty");
		}
		return passed;
	}
	
	/**
	 * Updates the Train docs tree for the new problem set (should not be called by directly by programmer, instead call
	 * updateAllComponents(), and only when loading a new problem set)
	 */
	protected boolean updateTrainDocTree() {
		boolean passed = true;
		preProcessWindow.trainTreeTop = new DefaultMutableTreeNode(preProcessWindow.ps.getTrainCorpusName());
		Map<String,List<Document>> trainDocsMap = preProcessWindow.ps.getAuthorMap();
		Set<String> authors = trainDocsMap.keySet();
		DefaultMutableTreeNode authorNode, docNode;

		for (String author: authors) {
			//We don't want to count the user's sample documents
			if(author.equals(ProblemSet.getDummyAuthor()))
					continue;
			
			authorNode = new DefaultMutableTreeNode(author, true);
			preProcessWindow.trainTreeTop.add(authorNode);
			
			for (Document doc: trainDocsMap.get(author)) {
				if (isEmpty(doc.getFilePath(), doc.getTitle())) {
					passed = false;
					preProcessWindow.ps.removeTrainDocAt(author, doc.getTitle());
				} else {
					docNode = new DefaultMutableTreeNode(doc.getTitle(), false);
					authorNode.add(docNode);
					
					if (titles.get(author) == null)
						titles.put(author, new ArrayList<String>());
					titles.get(author).add(doc.getTitle());
				}
			}
			
			//If all of the documents once attributed to this author when the doc set was saved no longer exist in the saved path,
			//don't have read permissions, or are now empty, then we will remove the author entirely
			if (authorNode.getChildCount() == 0) {
				preProcessWindow.trainTreeTop.remove(authorNode);
			}
		}

		preProcessWindow.trainTreeModel = new DefaultTreeModel(preProcessWindow.trainTreeTop, true);
		preProcessWindow.trainDocsTree.setModel(preProcessWindow.trainTreeModel);
		
		return passed;
	}
	
	/**
	 * Adds a single item to the Sample docs list
	 * @param title - The name of the new item
	 */
	private void addSampleDoc(String title) {		
		preProcessWindow.sampleDocsListModel.addElement(title);
		preProcessWindow.sampleDocsList.setSelectedIndex(preProcessWindow.sampleDocsListModel.size()-1);
	}
	
	/**
	 * Removes a single item from the Sample docs list
	 * @param title - The name of the item to remove
	 */
	private void removeSampleDoc(String title) {
		int[] selectedIndex = preProcessWindow.sampleDocsList.getSelectedIndices();
		int min = -1;
		for (int i = 0; i < selectedIndex.length; i++) {
			if (min == -1 || selectedIndex[i] < min)
				min = selectedIndex[i];
		}
		
		preProcessWindow.sampleDocsListModel.removeElement(title);
		
		int size = preProcessWindow.sampleDocsListModel.getSize();
		while (min >= size) {
			min--;
		}
		
		preProcessWindow.sampleDocsList.setSelectedIndex(min);
	}
	
	/**
	 * Adds a single node to the Train docs tree
	 * @param nodeTitle - The title of the new node
	 * @param nodeParent - The parent of the new node
	 * @param file - Whether or not the node should be an author's document, or is an author
	 */
	private void addTrainNode(String nodeTitle, String nodeParent, boolean file) {
		if (file) {
			int numAuthors = preProcessWindow.trainTreeTop.getChildCount();
			
			for (int a = 0; a < numAuthors; a++) {
				DefaultMutableTreeNode curAuthor = (DefaultMutableTreeNode)preProcessWindow.trainTreeModel.getChild(preProcessWindow.trainTreeTop, a);
				String curAuthorName = curAuthor.toString();
				
				if (curAuthorName.equals(nodeParent)) {
					curAuthor.add(new DefaultMutableTreeNode(nodeTitle, false));
					preProcessWindow.trainTreeModel.reload(curAuthor);
					return;
				}
			}
		} else {
			preProcessWindow.trainTreeTop.add(new DefaultMutableTreeNode(nodeTitle, true));
			preProcessWindow.trainTreeModel.reload(preProcessWindow.trainTreeTop);
		}
	}
	
	/**
	 * Removes a given node from the Train docs tree
	 * @param nodeTitle - The title of the node to remove
	 * @param file - Whether or not the node to remove is an author or a document
	 */
	private void removeTrainNode(String nodeTitle, boolean file) {
		int[] selectedIndex = preProcessWindow.trainDocsTree.getSelectionRows();
		int min = -1;
		for (int i = 0; i < selectedIndex.length; i++) {
			if (min == -1 || selectedIndex[i] < min)
				min = selectedIndex[i];
		}
		
		int numAuthors = preProcessWindow.trainTreeTop.getChildCount();
		
		for (int a = 0; a < numAuthors; a++) {
			DefaultMutableTreeNode curAuthor = (DefaultMutableTreeNode)preProcessWindow.trainTreeModel.getChild(preProcessWindow.trainTreeTop, a);
			String curAuthorName = curAuthor.toString();
			
			if (file) {
				int numDocs = curAuthor.getChildCount();
				DefaultMutableTreeNode curDoc;
				String curDocTitle;
				
				for (int d = 0; d < numDocs; d++) {
					curDoc = (DefaultMutableTreeNode)curAuthor.getChildAt(d);
					curDocTitle = curDoc.toString();
					
					if (curDocTitle.equals(nodeTitle)) {
						preProcessWindow.trainTreeModel.removeNodeFromParent(curDoc);
						preProcessWindow.trainTreeModel.reload(curAuthor);
						break;
					}
				}
			} else {
				if (curAuthorName.equals(nodeTitle)) {
					preProcessWindow.trainTreeModel.removeNodeFromParent(curAuthor);
					preProcessWindow.trainTreeModel.reload(preProcessWindow.trainTreeTop);
					break;
				}
			}
		}
		
		int size = preProcessWindow.trainDocsTree.getRowCount();
		while (min >= size) {
			min--;
		}
		preProcessWindow.trainDocsTree.setSelectionRow(min);
	}
	
	/**
	 * Sets the opening directory and returns the prepared JFileChooser.
	 * @param load
	 * @param trainDocs
	 * @return
	 */
	public FileDialog setOpeningDir(FileDialog load, boolean trainDocs) {
		if (trainDocs) {
			if (trainDocsDirectory == null) {
				try {
					trainDocsDirectory = new File(ANONConstants.CORPORA_PREFIX).getCanonicalPath();
					load.setDirectory(trainDocsDirectory);
				} catch (IOException e1) {
					Logger.logln(NAME+"Something went wrong while trying to set the opening directory for the JFileChooser", LogOut.STDERR);
				}
			} else {
				load.setDirectory(trainDocsDirectory);
			}
		} else {
			if (lastDirectory == null) {
				try {
					lastDirectory = new File(".").getCanonicalPath();
					load.setDirectory(lastDirectory);
				} catch (IOException e1) {
					Logger.logln(NAME+"Something went wrong while trying to set the opening directory for the JFileChooser", LogOut.STDERR);
				}
			} else {
				load.setDirectory(lastDirectory);
			}
		}
		
		return load;
	}
	
	/**
	 * Sets the opening directory and returns the prepared JFileChooser.
	 * @param load
	 * @param trainDocs
	 * @return
	 */
	public JFileChooser setOpeningDir(JFileChooser load, boolean trainDocs) {
		if (trainDocs) {
			if (trainDocsDirectory == null) {
				try {
					trainDocsDirectory = new File(ANONConstants.CORPORA_PREFIX).getCanonicalPath();
					load.setCurrentDirectory(new File(trainDocsDirectory));
				} catch (IOException e1) {
					Logger.logln(NAME+"Something went wrong while trying to set the opening directory for the JFileChooser", LogOut.STDERR);
				}
			} else {
				load.setCurrentDirectory(new File(trainDocsDirectory));
			}
		} else {
			if (lastDirectory == null) {
				try {
					lastDirectory = new File(".").getCanonicalPath();
					load.setCurrentDirectory(new File(lastDirectory));
				} catch (IOException e1) {
					Logger.logln(NAME+"Something went wrong while trying to set the opening directory for the JFileChooser", LogOut.STDERR);
				}
			} else {
				load.setCurrentDirectory(new File(lastDirectory));
			}
		}
		
		return load;
	}
	
	/**
	 * Simply updates the opening directory based on the last accessed directory
	 * @param absPath
	 * @param trainDocs
	 */
	public void updateOpeningDir(String absPath, boolean trainDocs) {
		if (ANONConstants.IS_MAC)
			systemSpecify = '/';
		else
			systemSpecify = '\\';
		if (trainDocs) {
			String backupPath = ".";
			try {
				backupPath = trainDocsDirectory;
			} catch (Exception e) {}
			
			try {
				int file = absPath.lastIndexOf(systemSpecify);
				absPath = absPath.substring(0, file);
				file = absPath.lastIndexOf(systemSpecify);
				absPath = absPath.substring(0, file);
				trainDocsDirectory = absPath;
				
				File dir = new File(trainDocsDirectory);
				
				if (!dir.exists() || !dir.canWrite()) {
					trainDocsDirectory = backupPath;
					Logger.logln(NAME+"Something went wrong trying to remember the last accessed directory, check it still exists and is writable", LogOut.STDERR);
				}
			} catch (Exception e) {
				trainDocsDirectory = backupPath;
				Logger.logln(NAME+"Something went wrong trying to remember the last accessed directory, check it still exists and is writable", LogOut.STDERR);
			}
		} else {
			String backupPath = lastDirectory;
			try {
				int file = absPath.lastIndexOf(systemSpecify);
				absPath = absPath.substring(0, file);
				lastDirectory = absPath;
				File dir = new File(lastDirectory);
				if (!dir.exists() || !dir.canWrite()) {
					lastDirectory = backupPath;
					Logger.logln(NAME+"Something went wrong trying to remember the last accessed directory, check it still exists and is writable", LogOut.STDERR);
				}
			} catch (Exception e) {
				e.printStackTrace();
				lastDirectory = backupPath;
				Logger.logln(NAME+"Something went wrong trying to remember the last accessed directory, check it still exists and is writable", LogOut.STDERR);
			}
		}
	}
	
	/**
	 * Verifies that a document file added by the user actually contains something, we don't want blank docs in Anonymouth
	 * @param path - The path of the file to check
	 * @param name - The name of the file to check
	 * @return
	 */
	private boolean isEmpty(String path, String name) {
		boolean result = false;

		try {
			@SuppressWarnings("resource")
			BufferedReader br = new BufferedReader(new FileReader(path));

			try {
				if (br.readLine() == null) {
					result = true;
					Logger.logln(NAME+"File "+name+" is empty, will not add");
					JOptionPane.showOptionDialog(main,
							"You can't use blank documents with Anonymouth,\n" +
									"please only choose documents that contain text.",
									"Empty Document",
									JOptionPane.DEFAULT_OPTION,		
									JOptionPane.ERROR_MESSAGE, null, null, null);
				}
			} catch (IOException e) {
				result = true;
				Logger.logln(NAME+"File "+name+" is empty, will not add");
				JOptionPane.showOptionDialog(main,
						"You can't use blank documents with Anonymouth,\n" +
								"please only choose documents that contain text.",
								"Empty Document",
								JOptionPane.DEFAULT_OPTION,		
								JOptionPane.ERROR_MESSAGE, null, null, null);
			}
		} catch (FileNotFoundException e) {
			result = true;
			Logger.logln(NAME+"File "+name+" is empty, will not add");
			JOptionPane.showOptionDialog(main,
					"You can't use blank documents with Anonymouth,\n" +
							"please only choose documents that contain text.",
							"Empty Document",
							JOptionPane.DEFAULT_OPTION,		
							JOptionPane.ERROR_MESSAGE, null, null, null);
		}     

		return result;
	}
}