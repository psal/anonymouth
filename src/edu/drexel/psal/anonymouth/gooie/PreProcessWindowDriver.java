package edu.drexel.psal.anonymouth.gooie;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import com.jgaap.generics.Document;

import edu.drexel.psal.ANONConstants;
import edu.drexel.psal.JSANConstants;
import edu.drexel.psal.anonymouth.helpers.ExtFilter;
import edu.drexel.psal.jstylo.generics.CumulativeFeatureDriver;
import edu.drexel.psal.jstylo.generics.Logger;
import edu.drexel.psal.jstylo.generics.ProblemSet;
import edu.drexel.psal.jstylo.generics.Logger.LogOut;

public class PreProcessWindowDriver {
	
	//Constants
	private final static String NAME = "( PreProcessWindowDriver ) - ";

	//Variables
	protected HashSet<String> titles = new HashSet<String>();
	private GUIMain main;
	private File lastDirectory;
	private File trainDocsDirectory;
	private PreProcessWindow preProcessWindow;
	private PreProcessAdvancedWindow advancedWindow;
	
	//Swing Components
	protected ActionListener clearProblemSetListener;
	protected ActionListener loadProblemSetListener;
	protected ActionListener saveProblemSetListener;
	protected ActionListener addTestDocListener;
	protected ActionListener removeTestDocListener;
	protected ActionListener addUserSampleDocListener;
	protected ActionListener removeUserSampleDocListener;
	protected ActionListener addTrainDocsListener;
	protected ActionListener removeTrainDocsListener;
	
	public PreProcessWindowDriver(PreProcessWindow preProcessWindow, PreProcessAdvancedWindow advancedWindow, GUIMain main) {
		this.main = main;
		this.advancedWindow = advancedWindow;
		this.preProcessWindow = preProcessWindow;
	}
	
	public void initListeners() {	
		loadProblemSetListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Logger.logln(NAME+"'Load Problem Set' button clicked on the Pre-process window");

					int answer = 0;
					if (preProcessWindow.ps != null && (preProcessWindow.ps.hasAuthors() || preProcessWindow.ps.hasTestDocs())) {
						if (PropertiesUtil.getWarnAll()) {
							answer = JOptionPane.showConfirmDialog(preProcessWindow,
									"Loading Problem Set will override current. Continue?",
									"Load Problem Set",
									JOptionPane.WARNING_MESSAGE,
									JOptionPane.YES_NO_CANCEL_OPTION);
						}
					}

					if (answer == JOptionPane.YES_OPTION) {
						PropertiesUtil.load.addChoosableFileFilter(new ExtFilter("XML files (*.xml)", "xml"));

						if (PropertiesUtil.prop.getProperty("recentProbSet") != null) {
							String absPath = PropertiesUtil.propFile.getAbsolutePath();
							String problemSetDir = absPath.substring(0, absPath.indexOf("anonymouth_prop")-1) + "\\problem_sets\\";
							PropertiesUtil.load.setCurrentDirectory(new File(problemSetDir));
							PropertiesUtil.load.setSelectedFile(new File(PropertiesUtil.prop.getProperty("recentProbSet")));
						} else {
							PropertiesUtil.load.setCurrentDirectory(new File(JSANConstants.JSAN_PROBLEMSETS_PREFIX));
						}

						answer = PropertiesUtil.load.showDialog(preProcessWindow, "Load Problem Set");

						if (answer == JFileChooser.APPROVE_OPTION) {
							String path = PropertiesUtil.load.getSelectedFile().getAbsolutePath();

							Logger.logln(NAME+"Trying to load problem set at: " + path);
							try {
								preProcessWindow.ps = new ProblemSet(path);
								updateAllComponents();
								PropertiesUtil.setProbSet(path);
							} catch (Exception exc) {
								exc.printStackTrace();
								Logger.logln(NAME+"Failed loading "+path, LogOut.STDERR);
								Logger.logln(NAME+exc.toString(),LogOut.STDERR);
								JOptionPane.showMessageDialog(null,
										"Failed loading problem set from:\n"+path,
										"Load Problem Set Failure",
										JOptionPane.ERROR_MESSAGE);
								PropertiesUtil.setProbSet("");
							}
						} else {
							Logger.logln(NAME+"Load problem set canceled");
						}
					}
				} catch (NullPointerException arg)
				{
					arg.printStackTrace();
				}
			}
		};
		preProcessWindow.loadProblemSetJButton.addActionListener(loadProblemSetListener);

		saveProblemSetListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Logger.logln(NAME+"'Save Problem Set' button clicked on the Pre-process window");

				PropertiesUtil.save.addChoosableFileFilter(new ExtFilter("XML files (*.xml)", "xml"));
				if (PropertiesUtil.prop.getProperty("recentProbSet") != null) {
					PropertiesUtil.save.setSelectedFile(new File(PropertiesUtil.prop.getProperty("recentProbSet")));
					Logger.logln(NAME+"Chooser root directory: " + PropertiesUtil.save.getSelectedFile().getAbsolutePath());
				} else {
					PropertiesUtil.save.setSelectedFile(new File(JSANConstants.JSAN_PROBLEMSETS_PREFIX+"problemSet.xml"));
				}

				int answer = PropertiesUtil.save.showSaveDialog(preProcessWindow);

				if (answer == JFileChooser.APPROVE_OPTION) {
					File f = PropertiesUtil.save.getSelectedFile();
					String path = f.getAbsolutePath();

					if (!path.toLowerCase().endsWith(".xml"))
						path += ".xml";
					try {
						BufferedWriter bw = new BufferedWriter(new FileWriter(path));
						bw.write(preProcessWindow.ps.toXMLString());
						bw.flush();
						bw.close();
						Logger.log("Saved problem set to "+path+":\n"+preProcessWindow.ps.toXMLString());
						PropertiesUtil.setProbSet(path);
					} catch (IOException exc) {
						Logger.logln(NAME+"Failed opening "+path+" for writing",LogOut.STDERR);
						Logger.logln(NAME+exc.toString(),LogOut.STDERR);
						JOptionPane.showMessageDialog(null,
								"Failed saving problem set into:\n"+path,
								"Save Problem Set Failure",
								JOptionPane.ERROR_MESSAGE);
					}
				} else {
					Logger.logln(NAME+"Save problem set canceled");
				}
			}
		};

		preProcessWindow.saveProblemSetJButton.addActionListener(saveProblemSetListener);

		addTestDocListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Logger.logln(NAME+"'Add Document(s)...' button clicked under the 'Test Documents' section on the Pre-process window.");

				DefaultListModel<String> dlm = (DefaultListModel<String>)preProcessWindow.prepMainDocList.getModel();
				if (dlm.getSize() == 0) {
					JFileChooser load = new JFileChooser();
					load = setOpeningDir(load, false);
					load.setMultiSelectionEnabled(true); //false since we will only allow one test doc to be entered at a time
					load.addChoosableFileFilter(new ExtFilter("Text files (*.txt)", "txt"));
					
					int answer = load.showOpenDialog(preProcessWindow);

					if (answer == JFileChooser.APPROVE_OPTION) {
						File[] files = load.getSelectedFiles();
						String msg = "Trying to load test documents:\n";
						for (File file: files)
							msg += "\t\t> "+file.getAbsolutePath()+"\n";
						Logger.log(msg);


						String path = "";
						ArrayList<String> allTestDocPaths = new ArrayList<String>();
						for (Document doc: preProcessWindow.ps.getAllTestDocs())
							allTestDocPaths.add(doc.getFilePath());
						for (File file: files) {
							path = file.getAbsolutePath();
							if (allTestDocPaths.contains(path))
								continue;
							preProcessWindow.ps.addTestDoc(ProblemSet.getDummyAuthor(), new Document(path,ProblemSet.getDummyAuthor(),file.getName()));
						}
						
						updateAllComponents();
						preProcessWindow.addTestDocJButton.setEnabled(false);
						updateOpeningDir(path, false);
					} else {
						Logger.logln(NAME+"Load test documents canceled");
					}
				} else {
					Logger.logln(NAME+"Attemted to add more than one test document - only one is allowed",LogOut.STDERR);
					JOptionPane.showMessageDialog(preProcessWindow,
							"You may only have one test document.",
							"If you wish to change it you must first remove the current one.",
							JOptionPane.WARNING_MESSAGE);
				}
			}
		};
		preProcessWindow.addTestDocJButton.addActionListener(addTestDocListener);

		removeTestDocListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Logger.logln(NAME+"'Remove Document(s)...' button clicked under the 'Test Documents' section on the Pre-process window");

				if (preProcessWindow.prepMainDocList.isSelectionEmpty()) {
					Logger.logln(NAME+"Failed removing test documents - no documents are selected",LogOut.STDERR);
					JOptionPane.showMessageDialog(preProcessWindow,
							"You must select test documents to remove.",
							"Remove Test Documents Failure",
							JOptionPane.WARNING_MESSAGE);
				} else {
					int answer = 0;
					if (PropertiesUtil.getWarnAll()) {
						answer = JOptionPane.showConfirmDialog(preProcessWindow,
								"Are you sure you want to remove your document to anonymize?\nDoing so will erase all current progress.",
								"Remove Document to Anonymize?",
								JOptionPane.YES_NO_OPTION);
					}
				
					if (answer == JOptionPane.YES_OPTION) {
						int[] rows = preProcessWindow.prepMainDocList.getSelectedIndices();
						String msg = "Removed test documents:\n";

						for (int i=rows.length-1; i>=0; i--) {
							msg += "\t\t> "+preProcessWindow.ps.getAllTestDocs().get(rows[i]).getTitle()+"\n";
							preProcessWindow.ps.removeTestDocFromList(rows[i]);
						}
						
						Logger.log(msg);
						updateTestDocTable();
					} else {
						Logger.logln(NAME+"Removing test documents canceled");
					}
				}
			}
		};
		preProcessWindow.removeTestDocJButton.addActionListener(removeTestDocListener);

		addUserSampleDocListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Logger.logln(NAME+"'Add Document(s)...' button clicked under the 'User Sample Documents' section on the Pre-process window");

				boolean rename = false;
				JFileChooser load = new JFileChooser();
				load = setOpeningDir(load, false);
				load.setMultiSelectionEnabled(true);
				load.addChoosableFileFilter(new ExtFilter("Text files (*.txt)", "txt"));
				
				int answer = load.showOpenDialog(preProcessWindow);
				if (answer == JFileChooser.APPROVE_OPTION) {
					File[] files = load.getSelectedFiles();
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
						if (titles.contains(file.getName()))
							rename = true;

						if (rename) {
							rename = false;
							int addNum = 1;

							String newTitle = file.getName();
							while (titles.contains(newTitle)) {
								newTitle = newTitle.replaceAll("_\\d*.[Tt][Xx][Tt]|.[Tt][Xx][Tt]", "");
								newTitle = newTitle.concat("_"+Integer.toString(addNum)+".txt");
								addNum++;
							}

							preProcessWindow.ps.addTrainDoc(ProblemSet.getDummyAuthor(), new Document(path,ProblemSet.getDummyAuthor(),newTitle));	
							titles.add(newTitle);
						} else {
							preProcessWindow.ps.addTrainDoc(ProblemSet.getDummyAuthor(), new Document(path,ProblemSet.getDummyAuthor(),file.getName()));
							titles.add(file.getName());
						}
					}

					updateOpeningDir(path, false);
					updateUserSampleDocTable();
				} else {
					Logger.logln(NAME+"Load user sample documents canceled");
				}
			}
		};
		preProcessWindow.addUserSampleDocJButton.addActionListener(addUserSampleDocListener);

		removeUserSampleDocListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Logger.logln(NAME+"'Remove Document(s)...' button clicked under the 'User Sample Documents' section on the Pre-process window");

				if (preProcessWindow.prepSampleDocsList.isSelectionEmpty()) {
					Logger.logln(NAME+"Failed removing user sample documents - no documents are selected",LogOut.STDERR);
					JOptionPane.showMessageDialog(preProcessWindow,
							"You must select documents to remove.",
							"Remove Documents Failure",
							JOptionPane.WARNING_MESSAGE);
				} else {
					int answer = 0;

					if (PropertiesUtil.getWarnAll()) {
						JOptionPane.showConfirmDialog(preProcessWindow,
								"Are you sure you want to remove the selected documents?",
								"Remove Documents Confirmation",
								JOptionPane.YES_NO_OPTION);
					}

					if (answer == JOptionPane.YES_OPTION) {
						int[] rows = preProcessWindow.prepSampleDocsList.getSelectedIndices();
						String msg = "Removed test documents:\n";

						System.out.println("size = " + preProcessWindow.ps.getTrainDocs(ProblemSet.getDummyAuthor()).size());
						for (int i = 0; i < preProcessWindow.ps.getTrainDocs(ProblemSet.getDummyAuthor()).size(); i++) {
							System.out.println(preProcessWindow.ps.getTrainDocs(ProblemSet.getDummyAuthor()).get(i));
						}

						System.out.println("=======");
						System.out.println("size = " + titles.size());
						Object[] test = titles.toArray();
						for (int i = 0; i < titles.size(); i++) {
							System.out.println(test[i]);
						}

						for (int i = rows.length-1; i >= 0; i--) {
							System.out.println("\t\t> "+preProcessWindow.ps.trainDocAt(ProblemSet.getDummyAuthor(),rows[i]).getTitle()+"\n");
							msg += "\t\t> "+preProcessWindow.ps.trainDocAt(ProblemSet.getDummyAuthor(),rows[i]).getTitle()+"\n";
							titles.remove(preProcessWindow.ps.trainDocAt(ProblemSet.getDummyAuthor(),rows[i]).getTitle());
							preProcessWindow.ps.removeTrainDocAt(ProblemSet.getDummyAuthor(), rows[i]);
						}

						Logger.log(msg);
						updateUserSampleDocTable();
					} else {
						Logger.logln(NAME+"Removing user sample documents canceled");
					}
				}
			}
		};
		preProcessWindow.removeUserSampleDocJButton.addActionListener(removeUserSampleDocListener);

		addTrainDocsListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Logger.logln(NAME+"'Add Document(s)...' button clicked under the 'Training Corpus' section on the Pre-process window");

				boolean rename = false;
				String author = "no author entered";
				JFileChooser load = new JFileChooser();
				load = setOpeningDir(load, true);
				load.setMultiSelectionEnabled(true);				
				//load.addChoosableFileFilter(new ExtFilter("Text files (*.txt)", "txt"));
				load.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				
				int answer = load.showOpenDialog(preProcessWindow);
				if (answer == JFileChooser.APPROVE_OPTION) {

					File[] files = load.getSelectedFiles();
					String msg = "Trying to load training documents for author \""+author+"\":\n";

					for (File file: files)
						msg += "\t\t> "+file.getAbsolutePath()+"\n";

					Logger.log(msg);

					String path = "";
					String skipList = "";
					ArrayList<String> allTrainDocPaths = new ArrayList<String>();
					ArrayList<String> allTestDocPaths = new ArrayList<String>();

					try {
						for (Document doc: preProcessWindow.ps.getTrainDocs(author)) {
							allTrainDocPaths.add(doc.getFilePath());
							Logger.logln(NAME+"Added to Train Docs: " + doc.getFilePath());
						}
					} catch(NullPointerException npe) {
						Logger.logln(NAME+"file '"+author+"' was not found. If name in single quotes is 'no author entered', this is not a problem.", LogOut.STDERR);
					}

					for (Document doc: preProcessWindow.ps.getAllTestDocs())
						allTestDocPaths.add(doc.getFilePath());
					if (preProcessWindow.ps.getTrainDocs(ProblemSet.getDummyAuthor()) != null) {
						for (Document doc: preProcessWindow.ps.getTrainDocs(ProblemSet.getDummyAuthor()))
							allTestDocPaths.add(doc.getFilePath());
					}
					for (File file: files) {
						if (file.isDirectory()) {
							String[] theDocsInTheDir = file.list();
							author = file.getName();
							String pathFirstHalf = file.getAbsolutePath();

							for (String otherFile: theDocsInTheDir) {
								File newFile = new File(otherFile);
								path = pathFirstHalf+File.separator+otherFile;
								System.out.println(path);

								if (allTrainDocPaths.contains(path)) {
									skipList += "\n"+path+" - already contained for author "+author;
									continue;
								}

								if (titles.contains(newFile.getName())) {
									rename = true;
								}

								if (allTestDocPaths.contains(path)) {
									skipList += "\n"+path+" - already contained as a test document";
									continue;
								}

								if(path.contains(".svn") || path.contains("imitation") || path.contains("verification") || path.contains("obfuscation") || path.contains("demographics"))
									continue;

								if (rename) {
									rename = false;
									int addNum = 1;

									String newTitle = newFile.getName();
									while (titles.contains(newTitle)) {
										newTitle = newTitle.replaceAll("_\\d*.[Tt][Xx][Tt]|.[Tt][Xx][Tt]", "");
										newTitle = newTitle.concat("_"+Integer.toString(addNum)+".txt");
										addNum++;
									}

									preProcessWindow.ps.addTrainDocs(author, new ArrayList<Document>());
									preProcessWindow.ps.addTrainDoc(author, new Document(path,author,newTitle));
									titles.add(newTitle);
								} else {
									preProcessWindow.ps.addTrainDocs(author, new ArrayList<Document>());
									preProcessWindow.ps.addTrainDoc(author, new Document(path,author,newFile.getName()));
									titles.add(newFile.getName());
								}
							}
						} else {
							path = file.getAbsolutePath();
							if (allTrainDocPaths.contains(path)) {
								skipList += "\n"+path+" - already contained for author "+author;
								continue;
							}
							if (allTestDocPaths.contains(path)) {
								skipList += "\n"+path+" - already contained as a test document";
								continue;
							}
							if (titles.contains(file.getName())) {
								rename = true;
							}

							if (rename) {
								rename = false;
								int addNum = 1;

								String newTitle = file.getName();
								while (titles.contains(newTitle)) {
									newTitle = newTitle.replaceAll("_\\d*.[Tt][Xx][Tt]|.[Tt][Xx][Tt]", "");
									newTitle = newTitle.concat("_"+Integer.toString(addNum)+".txt");
									addNum++;
								}

								preProcessWindow.ps.addTrainDoc(author, new Document(path,ProblemSet.getDummyAuthor(),newTitle));
								titles.add(newTitle);

								JOptionPane.showMessageDialog(null,
										"Anonymouth doesn't support two files with the same name\n\nOnly the Anonymouth reference titles are renamed, your\noriginal files and their repective titles remain unchaged",
										"File Title Renamed",
										JOptionPane.INFORMATION_MESSAGE);
							} else {
								preProcessWindow.ps.addTrainDoc(author, new Document(path,ProblemSet.getDummyAuthor(),file.getName()));
								titles.add(file.getName());
							}
						}
					}

					if (!skipList.equals("")) {
						JOptionPane.showMessageDialog(null,
								"Skipped the following documents:"+skipList,
								"Add Training Documents",
								JOptionPane.WARNING_MESSAGE);
						Logger.logln(NAME+"skipped the following training documents:"+skipList);
					}

					updateOpeningDir(path, true);
					updateTrainDocTree();
				} else {
					Logger.logln(NAME+"Load training documents canceled");
				}
			}

		};
		preProcessWindow.addTrainDocsJButton.addActionListener(addTrainDocsListener);

		removeTrainDocsListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Logger.logln(NAME+"'Remove Document(s)/Author(s)' button clicked under the 'Training Corpus' section on the Pre-process window");

				boolean removingAuthor = false;
				boolean removingAll = false;
				int docCounter = 0;
				TreePath[] paths = preProcessWindow.trainCorpusJTree.getSelectionPaths();
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
					JOptionPane.showMessageDialog(preProcessWindow,
							"You must select training documents or authors to remove.",
							"Remove Training Documents Failure",
							JOptionPane.WARNING_MESSAGE);
				} else {
					int answer = 0;
					if (PropertiesUtil.getWarnAll()) {
						if (removingAuthor) {
							answer = JOptionPane.showConfirmDialog(preProcessWindow,
									"Are you sure you want to remove the selected author and all their documents?",
									"Remove Training Document's Author Confirmation",
									JOptionPane.YES_NO_OPTION);
						} else if (removingAll) {
							answer = JOptionPane.showConfirmDialog(preProcessWindow,
									"Are you sure you want to remove all authors and documents?",
									"Remove Training Document's Author Confirmation",
									JOptionPane.YES_NO_OPTION);
						}
					}
					
					String msg;
					if (answer == JOptionPane.YES_OPTION) {
						if (removingAll) {
							msg = "Removed all:\n";
							for (DefaultMutableTreeNode authors: selectedDocs) {
								int size = preProcessWindow.ps.getTrainDocs(authors.toString()).size();
								for (int i = 0; i < size; i++) {
									titles.remove(preProcessWindow.ps.trainDocAt(authors.toString(), preProcessWindow.ps.getTrainDocs(authors.toString()).get(i).getTitle()).getTitle());
								}

								preProcessWindow.ps.removeAuthor(authors.toString());
								msg += "\t\t> " + authors.toString() + "\n";
							}
						} else if (removingAuthor) {
							msg = "Removed author:\n";
							for (DefaultMutableTreeNode author: selectedDocs) {
								int size = preProcessWindow.ps.getTrainDocs(author.toString()).size();
								for (int i = 0; i < size; i++) {
									titles.remove(preProcessWindow.ps.trainDocAt(author.toString(), preProcessWindow.ps.getTrainDocs(author.toString()).get(i).getTitle()).getTitle());
								}

								preProcessWindow.ps.removeAuthor(author.toString());
								msg += "\t\t> "+author.toString()+"\n";
							}
						} else {
							msg = "Removed training documents:\n";
							String author;
							for (DefaultMutableTreeNode doc: selectedDocs) {
								author = doc.getParent().toString();

								if (doc.getParent().getChildCount() == docCounter) {
									if (PropertiesUtil.getWarnAll()) {
										answer = JOptionPane.showConfirmDialog(preProcessWindow,
												"Are you sure you want to remove all of the selected author's documents?\n" +
														"All authors must have at least one document.",
														"Remove Training Documents Confirmation",
														JOptionPane.YES_NO_OPTION);
									}

									if (answer == JOptionPane.NO_OPTION) {
										//preProcessWindow.ps.removeAuthor(doc.getParent().toString());
										break;
									}

									docCounter = -1;
								}

								preProcessWindow.ps.removeTrainDocAt(author, doc.toString());
								titles.remove(preProcessWindow.ps.trainDocAt(author.toString(),doc.toString()).getTitle());
								msg += "\t\t> "+doc.toString()+"\n";
							}
						}

						Logger.log(msg);
						updateTrainDocTree();
					} else {
						Logger.logln(NAME+"Removing training documents/authors canceled");
					}
				}
			}
		};
		preProcessWindow.removeTrainDocsJButton.addActionListener(removeTrainDocsListener);
	}
	
	protected void updateAllComponents() {
		Logger.logln(NAME+"Updating components to reflect new problem set.");

		updateTestDocTable();
		updateTrainDocTree();
		updateUserSampleDocTable();
		updateDocPrepColor();
	}
	
	public void updateDocPrepColor() {
		if (preProcessWindow.documentsAreReady()) {
			preProcessWindow.prepDocLabel.setBackground(main.ready);
		} else {
			preProcessWindow.prepDocLabel.setBackground(main.notReady);
		}	
	}
	
	/**
	 * Updates the training corpus tree with the current problem set. 
	 */
	protected void updateTrainDocTree() {
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(preProcessWindow.ps.getTrainCorpusName());
		Map<String,List<Document>> trainDocsMap = preProcessWindow.ps.getAuthorMap();
		DefaultMutableTreeNode authorNode, docNode;
		for (String author: trainDocsMap.keySet()) {
			if(author.equals(ProblemSet.getDummyAuthor()))
					continue;
			authorNode = new DefaultMutableTreeNode(author, true);
			root.add(authorNode);
			for (Document doc: trainDocsMap.get(author)){
				docNode = new DefaultMutableTreeNode(doc.getTitle(), false);
				authorNode.add(docNode);
			}
		}
		DefaultTreeModel trainTreeModel = new DefaultTreeModel(root, true);
		preProcessWindow.trainCorpusJTree.setModel(trainTreeModel);
		
		updateDocPrepColor();
	}
	
	/**
	 * Updates the test documents table with the current problem set. 
	 */
	protected void updateTestDocTable() {
		DefaultListModel<String> dlm = (DefaultListModel<String>)preProcessWindow.prepMainDocList.getModel();
		dlm.removeAllElements();
		if (preProcessWindow.mainDocReady()) {
			List<Document> testDocs = preProcessWindow.ps.getAllTestDocs();
			
			for (int i=0; i<testDocs.size(); i++) {
				dlm.addElement(testDocs.get(i).getTitle());
				main.mainDocPreview = preProcessWindow.ps.getAllTestDocs().get(0);
				
				try {
					main.mainDocPreview.load();
				} catch (Exception e) {
					Logger.logln(NAME+"Error updating test doc table", LogOut.STDERR);
					JOptionPane.showOptionDialog(main,
							"An error occurred while tring to load your test document\n" +
							"from the previous problem set (most likely the original\nd" +
							"ocument has been moved).\n\nPlease replace the test document by hand.",
							"Error While Adding Test File",
							JOptionPane.DEFAULT_OPTION,		
							JOptionPane.ERROR_MESSAGE, null, null, null);
					dlm.removeAllElements();
					preProcessWindow.ps.removeTestAuthor(ANONConstants.DUMMY_NAME);
					updateDocPrepColor();
					return;
				}
				
				try {
					main.getDocumentPane().setText(main.mainDocPreview.stringify());
				} catch (Exception e) {
					Logger.logln(NAME+"Error setting text of test document in editor", LogOut.STDERR);
					JOptionPane.showOptionDialog(main,
							"An error occurred while tring to display the test document\n" +
							"in the document editor. Please verify the file's sane or\n" +
							"use a different file.</html>",
							"Error While Adding Test File",
							JOptionPane.DEFAULT_OPTION,		
							JOptionPane.ERROR_MESSAGE, null, null, null);
					dlm.removeAllElements();
					preProcessWindow.ps.removeTestAuthor(ANONConstants.DUMMY_NAME);
					updateDocPrepColor();
					return;
				}
			}
			
			main.updateDocLabel(testDocs.get(0).getTitle());
		}
		
		updateDocPrepColor();
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
					trainDocsDirectory = new File(new File(JSANConstants.JSAN_CORPORA_PREFIX).getCanonicalPath());
					load.setCurrentDirectory(trainDocsDirectory);
				} catch (IOException e1) {
					Logger.logln(NAME+"Something went wrong while trying to set the opening directory for the JFileChooser", LogOut.STDERR);
				}
			} else {
				load.setCurrentDirectory(trainDocsDirectory);
			}
		} else {
			if (lastDirectory == null) {
				try {
					lastDirectory = new File(new File(".").getCanonicalPath());
					load.setCurrentDirectory(lastDirectory);
				} catch (IOException e1) {
					Logger.logln(NAME+"Something went wrong while trying to set the opening directory for the JFileChooser", LogOut.STDERR);
				}
			} else {
				load.setCurrentDirectory(lastDirectory);
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
		if (trainDocs) {
			String backupPath = ".";
			try {
				backupPath = trainDocsDirectory.getAbsolutePath();
			} catch (Exception e) {}
			
			try {
				int file = absPath.lastIndexOf('/');
				absPath = absPath.substring(0, file);
				file = absPath.lastIndexOf('/');
				absPath = absPath.substring(0, file);
				trainDocsDirectory = new File(absPath);
				
				if (!trainDocsDirectory.exists() || !trainDocsDirectory.canWrite()) {
					trainDocsDirectory = new File(backupPath);
					Logger.logln(NAME+"Something went wrong trying to remember the last accessed directory, check it still exists and is writable", LogOut.STDERR);
				}
			} catch (Exception e) {
				trainDocsDirectory = new File(backupPath);
				Logger.logln(NAME+"Something went wrong trying to remember the last accessed directory, check it still exists and is writable", LogOut.STDERR);
			}
		} else {
			String backupPath = ".";
			try {
				backupPath = lastDirectory.getAbsolutePath();
			} catch (Exception e) {}
			
			try {
				int file = absPath.lastIndexOf('/');
				absPath = absPath.substring(0, file);
				lastDirectory = new File(absPath);
				
				if (!lastDirectory.exists() || !lastDirectory.canWrite()) {
					lastDirectory = new File(backupPath);
					Logger.logln(NAME+"Something went wrong trying to remember the last accessed directory, check it still exists and is writable", LogOut.STDERR);
				}
			} catch (Exception e) {
				lastDirectory = new File(backupPath);
				Logger.logln(NAME+"Something went wrong trying to remember the last accessed directory, check it still exists and is writable", LogOut.STDERR);
			}
		}
	}
	
	/**
	 * Updates the User Sample documents table with the current problem set. 
	 */
	protected void updateUserSampleDocTable() {
		DefaultListModel<String> dlm = (DefaultListModel<String>)preProcessWindow.prepSampleDocsList.getModel();
		dlm.removeAllElements();
		
		if (preProcessWindow.sampleDocsReady()) {
			List<Document> userSampleDocs = preProcessWindow.ps.getTrainDocs(ProblemSet.getDummyAuthor());
			for (int i=0; i<userSampleDocs.size(); i++) {
				dlm.addElement(userSampleDocs.get(i).getTitle());
			}
		}
		
		updateDocPrepColor();
	}
	
	/**
	 * Updates the feature set view when a new feature set is selected / created.
	 */
	protected void updateFeatureSetView(GUIMain main) {
		CumulativeFeatureDriver featureDriver = advancedWindow.cfd;
		
		advancedWindow.featuresSetDescJTextPane.setText(featureDriver.getDescription() == null ? "" : featureDriver.getDescription());
		
		advancedWindow.advancedDriver.clearFeatureView(main);
		advancedWindow.featuresJListModel.removeAllElements();
		for (int i = 0; i < featureDriver.numOfFeatureDrivers(); i++) 
			advancedWindow.featuresJListModel.addElement(featureDriver.featureDriverAt(i).getName());
	}
}
