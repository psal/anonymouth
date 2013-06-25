package edu.drexel.psal.anonymouth.gooie;

import java.awt.Color;
import java.awt.event.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import javax.swing.*;
import javax.swing.filechooser.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import com.jgaap.generics.Document;

import edu.drexel.psal.jstylo.generics.Logger.LogOut;
import edu.drexel.psal.jstylo.generics.*;

public class DriverPreProcessTabDocuments {

	private final static String NAME = "( DriverPreProcessTabDocuments ) - ";

	protected static MouseListener documentLabelClickAL;
	protected static ActionListener clearProblemSetAL;
	protected static ActionListener loadProblemSetAL;
	protected static ActionListener saveProblemSetAL;
	protected static ActionListener addTestDocAL;
	protected static ActionListener removeTestDocAL;
	protected static ActionListener addUserSampleDocAL;
	protected static ActionListener removeUserSampleDocAL;
	protected static ActionListener addTrainDocsAL;
	protected static ActionListener removeTrainDocsAL;
	protected static HashSet<String> titles = new HashSet<String>();

	/* =======================
	 * Documents tab listeners
	 * =======================
	 */


	/**
	 * Initialize all documents tab listeners.
	 */
	protected static void initListeners(final GUIMain main) 
	{
		initMainListeners(main);
		initAdvListeners(main);
	}

	//=======================================================================================================================
	//=======================================================================================================================
	//+++++++++++++++++++++++++++++++++++++++ Main Listeners ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	//=======================================================================================================================
	//=======================================================================================================================
	protected static void initMainListeners(final GUIMain main) {
		documentLabelClickAL = new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent arg0) {}
			@Override
			public void mousePressed(MouseEvent arg0) {}

			@Override
			public void mouseEntered(MouseEvent arg0) {
				main.prepDocLabel.setBackground(Color.YELLOW);
			}

			@Override
			public void mouseExited(MouseEvent arg0) {
				if (main.documentsAreReady())
					main.prepDocLabel.setBackground(main.ready);
				else
					main.prepDocLabel.setBackground(main.notReady);

			}

			@Override
			public void mouseReleased(MouseEvent arg0) {
				main.PPSP.tabbedPane.setSelectedComponent(main.PPSP.docPanel);
				main.PPSP.openWindow();
				if (main.documentsAreReady())
					main.prepDocLabel.setBackground(main.ready);
				else
					main.prepDocLabel.setBackground(main.notReady);
			}

		};
		main.prepDocLabel.addMouseListener(documentLabelClickAL);

		// new problem set button
		clearProblemSetAL = new ActionListener() 
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				Logger.logln(NAME+"'Clear Problem Set' button clicked on the documents tab");

				int answer = -1;
				// ask if current problem set is not empty
				if (main.ps != null && (main.ps.hasAuthors() || main.ps.hasTestDocs())) 
				{
					answer = JOptionPane.showConfirmDialog(null,
							"Are you sure you want to clear the current problem set?",
							"Clear Current Problem Set",
							JOptionPane.WARNING_MESSAGE,
							JOptionPane.YES_NO_CANCEL_OPTION);
				}
				if (answer == 0) 
				{					
					main.ps = new ProblemSet();
					main.ps.setTrainCorpusName(main.defaultTrainDocsTreeName);
					GUIUpdateInterface.updateProblemSet(main);// todo This needs to be fixed.. someone screwed it up.. (see function for where it fails -- there's a note)
					PropertiesUtil.setProbSet("");
					main.addTestDocJButton.setEnabled(true);
					main.PPSP.addTestDocJButton.setEnabled(true);
				}
			}
		};
		main.clearProblemSetJButton.addActionListener(clearProblemSetAL);		


		// load problem set button
		loadProblemSetAL = new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				try
				{
					Logger.logln(NAME+"'Load Problem Set' button clicked on the documents tab");

					int answer = 0;
					// ask if current problem set is not empty
					if (main.ps != null && (main.ps.hasAuthors() || main.ps.hasTestDocs())) 
					{
						answer = JOptionPane.showConfirmDialog(null,
								"Loading Problem Set will override current. Continue?",
								"Load Problem Set",
								JOptionPane.WARNING_MESSAGE,
								JOptionPane.YES_NO_CANCEL_OPTION);
					}
					if (answer == 0) 
					{
						PropertiesUtil.load.addChoosableFileFilter(new ExtFilter("XML files (*.xml)", "xml"));
						if (PropertiesUtil.prop.getProperty("recentProbSet") != null)
						{
							String absPath = PropertiesUtil.propFile.getAbsolutePath();
							String problemSetDir = absPath.substring(0, absPath.indexOf("anonymouth_prop")-1) + "\\problem_sets\\";
							PropertiesUtil.load.setCurrentDirectory(new File(problemSetDir));
							PropertiesUtil.load.setSelectedFile(new File(PropertiesUtil.prop.getProperty("recentProbSet")));
						}
						answer = PropertiesUtil.load.showDialog(main, "Load Problem Set");

						if (answer == JFileChooser.APPROVE_OPTION) {
							String path = PropertiesUtil.load.getSelectedFile().getAbsolutePath();
							//String filename = PropertiesUtil.load.getSelectedFile().getName();
							//path = path.substring(path.indexOf("jsan_resources"));

							PropertiesUtil.setProbSet(path);

							Logger.logln(NAME+"Trying to load problem set at: " + path);
							try {
								main.ps = new ProblemSet(path);
								main.classChoice.setSelectedItem(PropertiesUtil.prop.getProperty("recentClass"));
								main.featuresSetJComboBox.setSelectedItem(PropertiesUtil.prop.getProperty("recentFeat"));
								GUIUpdateInterface.updateProblemSet(main);
							} catch (Exception exc) {
								Logger.logln(NAME+"Failed loading "+path, LogOut.STDERR);
								Logger.logln(NAME+exc.toString(),LogOut.STDERR);
								JOptionPane.showMessageDialog(null,
										"Failed loading problem set from:\n"+path,
										"Load Problem Set Failure",
										JOptionPane.ERROR_MESSAGE);
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
		main.loadProblemSetJButton.addActionListener(loadProblemSetAL);

		// save problem set button
		saveProblemSetAL = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Logger.logln(NAME+"'Save Problem Set' button clicked on the documents tab.");

				PropertiesUtil.save.addChoosableFileFilter(new ExtFilter("XML files (*.xml)", "xml"));
				if (PropertiesUtil.prop.getProperty("recentProbSet") != null) {
					PropertiesUtil.save.setSelectedFile(new File(PropertiesUtil.prop.getProperty("recentProbSet")));
					System.out.println(PropertiesUtil.save.getSelectedFile().getAbsolutePath());
				} else {
					PropertiesUtil.save.setSelectedFile(new File("problemSet.xml"));
				}

				int answer = PropertiesUtil.save.showSaveDialog(main);

				if (answer == JFileChooser.APPROVE_OPTION) {
					File f = PropertiesUtil.save.getSelectedFile();
					String path = f.getAbsolutePath();

					PropertiesUtil.setProbSet(PropertiesUtil.save.getSelectedFile().getAbsolutePath());

					if (!path.toLowerCase().endsWith(".xml"))
						path += ".xml";
					try {
						BufferedWriter bw = new BufferedWriter(new FileWriter(path));
						bw.write(main.ps.toXMLString());
						bw.flush();
						bw.close();
						Logger.log("Saved problem set to "+path+":\n"+main.ps.toXMLString());
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

		main.saveProblemSetJButton.addActionListener(saveProblemSetAL);

		// test documents
		// ==============

		// test documents table
		// -- none --

		// add test documents button
		addTestDocAL = new ActionListener() 
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				Logger.logln(NAME+"'Add Document(s)...' button clicked under the 'Test Documents' section on the documents tab.");

				DefaultListModel<String> dlm = (DefaultListModel<String>)main.prepMainDocList.getModel();
				DefaultListModel<String> dlm2 = (DefaultListModel<String>)main.PPSP.prepMainDocList.getModel();
				if (dlm.getSize() == 0 && dlm2.getSize() == 0) {
					JFileChooser open = new JFileChooser();
					open.setMultiSelectionEnabled(true); //false since we will only allow one test doc to be entered at a time
					File dir;
					try {
						dir = new File(new File(".").getCanonicalPath());
						open.setCurrentDirectory(dir);
					} catch (IOException e1) {
						e1.printStackTrace();
					}

					open.addChoosableFileFilter(new ExtFilter("Text files (*.txt)", "txt"));
					int answer = open.showOpenDialog(main);

					if (answer == JFileChooser.APPROVE_OPTION) {
						File[] files = open.getSelectedFiles();
						String msg = "Trying to load test documents:\n";
						for (File file: files)
							msg += "\t\t> "+file.getAbsolutePath()+"\n";
						Logger.log(msg);


						String path;
						ArrayList<String> allTestDocPaths = new ArrayList<String>();
						for (Document doc: main.ps.getTestDocs())
							allTestDocPaths.add(doc.getFilePath());
						for (File file: files) {
							path = file.getAbsolutePath();
							if (allTestDocPaths.contains(path))
								continue;
							main.ps.addTestDoc(new Document(path,ProblemSet.getDummyAuthor(),file.getName()));
						}
						GUIUpdateInterface.updateTestDocTable(main);
						main.addTestDocJButton.setEnabled(false);
						main.PPSP.addTestDocJButton.setEnabled(false);

					} else {
						Logger.logln(NAME+"Load test documents canceled");
					}
				} else {
					Logger.logln(NAME+"Attemted to add more than one test document - only one is allowed",LogOut.STDERR);
					JOptionPane.showMessageDialog(null,
							"You may only have one test document.",
							"If you wish to change it you must first remove the current one.",
							JOptionPane.WARNING_MESSAGE);
				}
			}
		};
		main.addTestDocJButton.addActionListener(addTestDocAL);

		// remove test documents button
		removeTestDocAL = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Logger.logln(NAME+"'Remove Document(s)...' button clicked under the 'Test Documents' section on the documents tab.");

				if (main.prepMainDocList.isSelectionEmpty()) {
					Logger.logln(NAME+"Failed removing test documents - no documents are selected",LogOut.STDERR);
					JOptionPane.showMessageDialog(null,
							"You must select test documents to remove.",
							"Remove Test Documents Failure",
							JOptionPane.WARNING_MESSAGE);
				} else {
					int answer = JOptionPane.showConfirmDialog(null,
							"Are you sure you want to remove the selected test documents?",
							"Remove Test Documents Confirmation",
							JOptionPane.YES_NO_OPTION);

					if (answer == 0) {
						int[] rows = main.prepMainDocList.getSelectedIndices();
						String msg = "Removed test documents:\n";
						for (int i=rows.length-1; i>=0; i--) 
						{
							msg += "\t\t> "+main.ps.testDocAt(rows[i]).getTitle()+"\n";
							main.ps.removeTestDocAt(rows[i]);
						}
						Logger.log(msg);

						GUIUpdateInterface.updateTestDocTable(main);
						GUIUpdateInterface.clearDocPreview(main);
						System.out.println("Should have cleared");
						main.addTestDocJButton.setEnabled(true);
						main.PPSP.addTestDocJButton.setEnabled(true);
					} 
					else 
					{
						Logger.logln(NAME+"Removing test documents canceled");
					}
				}
			}
		};
		main.removeTestDocJButton.addActionListener(removeTestDocAL);


		/////////////////// userSampleDocuments

		addUserSampleDocAL = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Logger.logln(NAME+"'Add Document(s)...' button clicked under the 'User Sample Documents' section on the documents tab.");

				boolean rename = false;
				JFileChooser open = new JFileChooser();
				File dir;
				try {
					dir = new File(new File(".").getCanonicalPath());
					open.setCurrentDirectory(dir);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				open.setMultiSelectionEnabled(true);
				open.addChoosableFileFilter(new ExtFilter("Text files (*.txt)", "txt"));
				int answer = open.showOpenDialog(main);

				if (answer == JFileChooser.APPROVE_OPTION) {
					File[] files = open.getSelectedFiles();
					String msg = "Trying to load User Sample documents:\n";
					for (File file: files)
						msg += "\t\t> "+file.getAbsolutePath()+"\n";
					Logger.log(msg);

					String path;
					ArrayList<String> allUserSampleDocPaths = new ArrayList<String>();
					for (Document doc: main.ps.getTestDocs())
						allUserSampleDocPaths.add(doc.getFilePath());
					for (Document doc: main.ps.getAllTrainDocs())
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

							main.ps.addTrainDoc(ProblemSet.getDummyAuthor(), new Document(path,ProblemSet.getDummyAuthor(),newTitle));	
							titles.add(newTitle);
						} else {
							main.ps.addTrainDoc(ProblemSet.getDummyAuthor(), new Document(path,ProblemSet.getDummyAuthor(),file.getName()));
							titles.add(file.getName());
						}
					}

					GUIUpdateInterface.updateUserSampleDocTable(main);
				} else {
					Logger.logln(NAME+"Load user sample documents canceled");
				}
			}
		};
		main.adduserSampleDocJButton.addActionListener(addUserSampleDocAL);

		// remove userSample documents button
		removeUserSampleDocAL = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Logger.logln(NAME+"'Remove Document(s)...' button clicked under the 'User Sample Documents' section on the documents tab.");

				if (main.prepSampleDocsList.isSelectionEmpty()) {
					Logger.logln(NAME+"Failed removing user sample documents - no documents are selected",LogOut.STDERR);
					JOptionPane.showMessageDialog(null,
							"You must select documents to remove.",
							"Remove Documents Failure",
							JOptionPane.WARNING_MESSAGE);
				} else {
					int answer = JOptionPane.showConfirmDialog(null,
							"Are you sure you want to remove the selected documents?",
							"Remove Documents Confirmation",
							JOptionPane.YES_NO_OPTION);

					if (answer == 0) {
						int[] rows = main.prepSampleDocsList.getSelectedIndices();
						String msg = "Removed test documents:\n";
						
						System.out.println("size = " + main.ps.getTrainDocs(ProblemSet.getDummyAuthor()).size());
						for (int i = 0; i < main.ps.getTrainDocs(ProblemSet.getDummyAuthor()).size(); i++) {
							System.out.println(main.ps.getTrainDocs(ProblemSet.getDummyAuthor()).get(i));
						}
						
						System.out.println("=======");
						System.out.println("size = " + titles.size());
						Object[] test = titles.toArray();
						for (int i = 0; i < titles.size(); i++) {
							System.out.println(test[i]);
						}
						
						for (int i = rows.length-1; i >= 0; i--) {
							System.out.println("\t\t> "+main.ps.trainDocAt(ProblemSet.getDummyAuthor(),rows[i]).getTitle()+"\n");
							msg += "\t\t> "+main.ps.trainDocAt(ProblemSet.getDummyAuthor(),rows[i]).getTitle()+"\n";
							titles.remove(main.ps.trainDocAt(ProblemSet.getDummyAuthor(),rows[i]).getTitle());
							main.ps.removeTrainDocAt(ProblemSet.getDummyAuthor(), rows[i]);
						}
						
						Logger.log(msg);

						GUIUpdateInterface.updateUserSampleDocTable(main);
					} else {
						Logger.logln(NAME+"Removing user sample documents canceled");
					}
				}
			}
		};
		main.removeuserSampleDocJButton.addActionListener(removeUserSampleDocAL);

		// add training documents button
		addTrainDocsAL = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Logger.logln(NAME+"'Add Document(s)...' button clicked under the 'Training Corpus' section on the documents tab.");
				
				boolean rename = false;
				String author = "no author entered";
				JFileChooser open = new JFileChooser();
				open.setMultiSelectionEnabled(true);
				File dir;

				try {
					dir = new File(new File(".").getCanonicalPath());
					open.setCurrentDirectory(dir);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				open.addChoosableFileFilter(new ExtFilter("Text files (*.txt)", "txt"));
				open.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				int answer = open.showOpenDialog(main);

				if (answer == JFileChooser.APPROVE_OPTION) {

					File[] files = open.getSelectedFiles();
					String msg = "Trying to load training documents for author \""+author+"\":\n";

					for (File file: files)
						msg += "\t\t> "+file.getAbsolutePath()+"\n";

					Logger.log(msg);

					String path = "";
					String skipList = "";
					ArrayList<String> allTrainDocPaths = new ArrayList<String>();
					ArrayList<String> allTestDocPaths = new ArrayList<String>();

					try {
						for (Document doc: main.ps.getTrainDocs(author)) {
							allTrainDocPaths.add(doc.getFilePath());
							Logger.logln(NAME+"Added to Train Docs: " + doc.getFilePath());
						}
					} catch(NullPointerException npe) {
						Logger.logln(NAME+"file '"+author+"' was not found. If name in single quotes is 'no author entered', this is not a problem.", LogOut.STDERR);
					}

					for (Document doc: main.ps.getTestDocs())
						allTestDocPaths.add(doc.getFilePath());
					if (main.ps.getTrainDocs(ProblemSet.getDummyAuthor()) != null) {
						for (Document doc: main.ps.getTrainDocs(ProblemSet.getDummyAuthor()))
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

									main.ps.addTrainDocs(author, new ArrayList<Document>());
									main.ps.addTrainDoc(author, new Document(path,author,newTitle));
									titles.add(newTitle);
								} else {
									main.ps.addTrainDocs(author, new ArrayList<Document>());
									main.ps.addTrainDoc(author, new Document(path,author,newFile.getName()));
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

								main.ps.addTrainDoc(author, new Document(path,ProblemSet.getDummyAuthor(),newTitle));
								titles.add(newTitle);
								
								JOptionPane.showMessageDialog(null,
										"Anonymouth doesn't support two files with the same name\n\nOnly the Anonymouth reference titles are renamed, your\noriginal files and their repective titles remain unchaged",
										"File Title Renamed",
										JOptionPane.INFORMATION_MESSAGE);
							} else {
								main.ps.addTrainDoc(author, new Document(path,ProblemSet.getDummyAuthor(),file.getName()));
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

					GUIUpdateInterface.updateTrainDocTree(main);
					//GUIUpdateInterface.clearDocPreview(main);
				} else {
					Logger.logln(NAME+"Load training documents canceled");
				}
			}

		};
		main.addTrainDocsJButton.addActionListener(addTrainDocsAL);

		// remove training documents and/or authors button
		removeTrainDocsAL = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Logger.logln(NAME+"'Remove Document(s)/Author(s)' button clicked under the 'Training Corpus' section on the documents tab.");

				boolean removingAuthor = false;
				boolean removingAll = false;
				int docCounter = 0;
				TreePath[] paths = main.trainCorpusJTree.getSelectionPaths();
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
					JOptionPane.showMessageDialog(null,
							"You must select training documents or authors to remove.",
							"Remove Training Documents Failure",
							JOptionPane.WARNING_MESSAGE);
				} else {
					int answer = 0;

					if (removingAuthor) {
						answer = JOptionPane.showConfirmDialog(null,
								"Are you sure you want to remove the selected author and all their documents?",
								"Remove Training Document's Author Confirmation",
								JOptionPane.YES_NO_OPTION);
					} else if (removingAll) {
						answer = JOptionPane.showConfirmDialog(null,
								"Are you sure you want to remove all authors and documents?",
								"Remove Training Document's Author Confirmation",
								JOptionPane.YES_NO_OPTION);
					}

					String msg;
					if (answer == 0) {
						if (removingAll) {
							msg = "Removed all:\n";
							for (DefaultMutableTreeNode authors: selectedDocs) {
								int size = main.ps.getTrainDocs(authors.toString()).size();
								for (int i = 0; i < size; i++) {
									titles.remove(main.ps.trainDocAt(authors.toString(), main.ps.getTrainDocs(authors.toString()).get(i).getTitle()).getTitle());
								}
								
								main.ps.removeAuthor(authors.toString());
								msg += "\t\t> " + authors.toString() + "\n";
							}
						} else if (removingAuthor) {
							msg = "Removed author:\n";
							for (DefaultMutableTreeNode author: selectedDocs) {
								int size = main.ps.getTrainDocs(author.toString()).size();
								for (int i = 0; i < size; i++) {
									titles.remove(main.ps.trainDocAt(author.toString(), main.ps.getTrainDocs(author.toString()).get(i).getTitle()).getTitle());
								}
								
								main.ps.removeAuthor(author.toString());
								msg += "\t\t> "+author.toString()+"\n";
							}
						} else {
							msg = "Removed training documents:\n";
							String author;
							for (DefaultMutableTreeNode doc: selectedDocs) {
								author = doc.getParent().toString();

								if (doc.getParent().getChildCount() == docCounter) {
									answer = JOptionPane.showConfirmDialog(null,
											"Are you sure you want to remove all of the selected author's documents?\n" +
													"All authors must have at least one document.",
													"Remove Training Documents Confirmation",
													JOptionPane.YES_NO_OPTION);

									if (answer == 1) {
										//										main.ps.removeAuthor(doc.getParent().toString());
										break;
									}

									docCounter = -1;
								}

								main.ps.removeTrainDocAt(author, doc.toString());
								titles.remove(main.ps.trainDocAt(author.toString(),doc.toString()).getTitle());
								msg += "\t\t> "+doc.toString()+"\n";
							}
						}

						//Check if there are files still in Author folder
						Logger.log(msg);
						GUIUpdateInterface.updateTrainDocTree(main);
						//GUIUpdateInterface.clearDocPreview(main);
					} else {
						Logger.logln(NAME+"Removing training documents/authors canceled");
					}
				}
			}
		};
		main.removeTrainDocsJButton.addActionListener(removeTrainDocsAL);
	}

	//=======================================================================================================================
	//=======================================================================================================================
	//+++++++++++++++++++++++++++++++++++++++ Advanced Listeners ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	//=======================================================================================================================
	//=======================================================================================================================

	protected static void initAdvListeners(final GUIMain main)
	{
		// problem set
		// ===========

		// new problem set button
		clearProblemSetAL = new ActionListener() 
		{

			@Override
			public void actionPerformed(ActionEvent e) {
				Logger.logln(NAME+"'Clear Problem Set' button clicked on the documents tab");

				int answer = 0;
				// ask if current problem set is not empty
				if (main.ps != null && (main.ps.hasAuthors() || main.ps.hasTestDocs())) {
					answer = JOptionPane.showConfirmDialog(null,
							"Are you sure you want to clear the current problem set?",
							"Clear Current Problem Set",
							JOptionPane.WARNING_MESSAGE,
							JOptionPane.YES_NO_CANCEL_OPTION);
				}
				if (answer == 0) {					
					main.ps = new ProblemSet();
					main.ps.setTrainCorpusName(main.defaultTrainDocsTreeName);
					GUIUpdateInterface.updateProblemSet(main);// todo This needs to be fixed.. someone screwed it up.. (see function for where it fails -- there's a note)
					main.addTestDocJButton.setEnabled(true);
					main.PPSP.addTestDocJButton.setEnabled(true);
				}
			}
		};
		main.PPSP.clearProblemSetJButton.addActionListener(clearProblemSetAL);

		// load problem set button
		loadProblemSetAL = new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e) {
				Logger.logln(NAME+"'Load Problem Set' button clicked on the documents tab");

				int answer = 0;
				// ask if current problem set is not empty
				if (main.ps != null && (main.ps.hasAuthors() || main.ps.hasTestDocs())) {
					answer = JOptionPane.showConfirmDialog(null,
							"Loading Problem Set will override current. Continue?",
							"Load Problem Set",
							JOptionPane.WARNING_MESSAGE,
							JOptionPane.YES_NO_CANCEL_OPTION);
				}
				if (answer == 0) {
					PropertiesUtil.load.addChoosableFileFilter(new ExtFilter("XML files (*.xml)", "xml"));
					if (PropertiesUtil.prop.getProperty("recentProbSet") != null)
					{
						String absPath = PropertiesUtil.propFile.getAbsolutePath();
						String problemSetDir = absPath.substring(0, absPath.indexOf("anonymouth_prop")-1) + "\\problem_sets\\";
						PropertiesUtil.load.setCurrentDirectory(new File(problemSetDir));
						PropertiesUtil.load.setSelectedFile(new File(PropertiesUtil.prop.getProperty("recentProbSet")));
					}
					answer = PropertiesUtil.load.showDialog(main, "Load Problem Set");

					if (answer == JFileChooser.APPROVE_OPTION) {
						String path = PropertiesUtil.load.getSelectedFile().getAbsolutePath();

						PropertiesUtil.setProbSet(path);

						Logger.logln(NAME+"Trying to load problem set from "+path);
						try {
							main.ps = new ProblemSet(path);
							GUIUpdateInterface.updateProblemSet(main);
						} catch (Exception exc) {
							Logger.logln(NAME+"Failed loading "+path, LogOut.STDERR);
							Logger.logln(NAME+exc.toString(),LogOut.STDERR);
							JOptionPane.showMessageDialog(null,
									"Failed loading problem set from:\n"+path,
									"Load Problem Set Failure",
									JOptionPane.ERROR_MESSAGE);
						}

					} else {
						Logger.logln(NAME+"Load problem set canceled");
					}
				}
			}
		};
		main.PPSP.loadProblemSetJButton.addActionListener(loadProblemSetAL);

		// save problem set button
		saveProblemSetAL = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Logger.logln(NAME+"'Save Problem Set' button clicked on the documents tab.");

				PropertiesUtil.save.addChoosableFileFilter(new ExtFilter("XML files (*.xml)", "xml"));
				if (PropertiesUtil.prop.getProperty("recentProbSet") != null)
					PropertiesUtil.save.setSelectedFile(new File(PropertiesUtil.prop.getProperty("recentProbSet")));
				int answer = PropertiesUtil.save.showSaveDialog(main);

				if (answer == JFileChooser.APPROVE_OPTION) {
					File f = PropertiesUtil.save.getSelectedFile();
					String path = f.getAbsolutePath();
					System.out.println(path);

					PropertiesUtil.setProbSet(path);

					if (!path.toLowerCase().endsWith(".xml"))
						path += ".xml";
					try {
						BufferedWriter bw = new BufferedWriter(new FileWriter(path));
						bw.write(main.ps.toXMLString());
						bw.flush();
						bw.close();
						Logger.log("Saved problem set to "+path+":\n"+main.ps.toXMLString());
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

		main.PPSP.saveProblemSetJButton.addActionListener(saveProblemSetAL);

		// test documents
		// ==============

		// test documents table
		// -- none --

		// add test documents button
		addTestDocAL = new ActionListener() 
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				Logger.logln(NAME+"'Add Document(s)...' button clicked under the 'Test Documents' section on the documents tab.");

				JFileChooser open = new JFileChooser();
				open.setMultiSelectionEnabled(true);
				File dir;
				try {
					dir = new File(new File(".").getCanonicalPath());
					open.setCurrentDirectory(dir);
				} catch (IOException e1) {
					e1.printStackTrace();
				}

				open.addChoosableFileFilter(new ExtFilter("Text files (*.txt)", "txt"));
				int answer = open.showOpenDialog(main);

				if (answer == JFileChooser.APPROVE_OPTION) {
					File[] files = open.getSelectedFiles();
					String msg = "Trying to load test documents:\n";
					for (File file: files)
						msg += "\t\t> "+file.getAbsolutePath()+"\n";
					Logger.log(msg);


					String path;
					ArrayList<String> allTestDocPaths = new ArrayList<String>();
					for (Document doc: main.ps.getTestDocs())
						allTestDocPaths.add(doc.getFilePath());
					for (File file: files) {
						path = file.getAbsolutePath();
						if (allTestDocPaths.contains(path))
							continue;
						main.ps.addTestDoc(new Document(path,ProblemSet.getDummyAuthor(),file.getName()));
					}

					GUIUpdateInterface.updateTestDocTable(main);
					main.addTestDocJButton.setEnabled(false);
					main.PPSP.addTestDocJButton.setEnabled(false);

				} else {
					Logger.logln(NAME+"Load test documents canceled");
				}
			}
		};
		main.PPSP.addTestDocJButton.addActionListener(addTestDocAL);

		// remove test documents button
		removeTestDocAL = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) 
			{
				Logger.logln(NAME+"'Remove Document(s)...' button clicked under the 'Test Documents' section on the documents tab.");

				if (main.PPSP.prepMainDocList.isSelectionEmpty()) 
				{
					Logger.logln(NAME+"Failed removing test documents - no documents are selected",LogOut.STDERR);
					JOptionPane.showMessageDialog(null,
							"You must select test documents to remove.",
							"Remove Test Documents Failure",
							JOptionPane.WARNING_MESSAGE);
				} 
				else 
				{
					int answer = JOptionPane.showConfirmDialog(null,
							"Are you sure you want to remove the selected test documents?",
							"Remove Test Documents Confirmation",
							JOptionPane.YES_NO_OPTION);

					if (answer == 0) 
					{
						int[] rows = main.PPSP.prepMainDocList.getSelectedIndices();
						String msg = "Removed test documents:\n";
						for (int i=rows.length-1; i>=0; i--) 
						{
							msg += "\t\t> "+main.ps.testDocAt(rows[i]).getTitle()+"\n";
							main.ps.removeTestDocAt(rows[i]);
						}
						Logger.log(msg);

						GUIUpdateInterface.updateTestDocTable(main);
						GUIUpdateInterface.clearDocPreview(main);
						main.addTestDocJButton.setEnabled(true);
						main.PPSP.addTestDocJButton.setEnabled(true);
					} 
					else 
					{
						Logger.logln(NAME+"Removing test documents canceled");
					}
				}
			}
		};
		main.PPSP.removeTestDocJButton.addActionListener(removeTestDocAL);

		// preview test document button
		//				main.testDocPreviewJButton.addActionListener(new ActionListener() {
		//					
		//					@Override
		//					public void actionPerformed(ActionEvent e) {
		//						Logger.logln(NAME+"'Preview Document' button clicked under the 'Test Documents' section on the documents tab.");
		//						
		//						int row = main.testDocsJTable.getSelectedRow();
		//						if (row == -1) {
		//							JOptionPane.showMessageDialog(null,
		//									"You must select a test document in order to show its preview.",
		//									"Show Test Document Preview Error",
		//									JOptionPane.ERROR_MESSAGE);
		//							Logger.logln(NAME+"No test document is selected for preview",LogOut.STDERR);
		//						} else {
		//							Document doc = main.ps.testDocAt(row);
		//							try {
		//								doc.load();
		//								main.docPreviewNameJLabel.setText("- "+doc.getTitle());
		//								main.docPreviewJTextPane.setText(doc.stringify());
		//							} catch (Exception exc) {
		//								JOptionPane.showMessageDialog(null,
		//										"Failed opening test document for preview:\n"+doc.getFilePath(),
		//										"Show Test Document Preview Error",
		//										JOptionPane.ERROR_MESSAGE);
		//								Logger.logln(NAME+"Failed opening test document for preview",LogOut.STDERR);
		//								Logger.logln(NAME+exc.toString(),LogOut.STDERR);
		//								GUIUpdateInterface.clearDocPreview(main);
		//							}
		//						}
		//					}
		//				});


		/////////////////// userSampleDocuments

		addUserSampleDocAL = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Logger.logln(NAME+"'Add Document(s)...' button clicked under the 'User Sample Documents' section on the documents tab.");

				boolean rename = false;
				JFileChooser open = new JFileChooser();
				File dir;
				try {
					dir = new File(new File(".").getCanonicalPath());
					open.setCurrentDirectory(dir);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				open.setMultiSelectionEnabled(true);
				open.addChoosableFileFilter(new ExtFilter("Text files (*.txt)", "txt"));
				int answer = open.showOpenDialog(main);

				if (answer == JFileChooser.APPROVE_OPTION) {
					File[] files = open.getSelectedFiles();
					String msg = "Trying to load User Sample documents:\n";
					for (File file: files)
						msg += "\t\t> "+file.getAbsolutePath()+"\n";
					Logger.log(msg);


					String path;
					ArrayList<String> allUserSampleDocPaths = new ArrayList<String>();
					for (Document doc: main.ps.getTestDocs())
						allUserSampleDocPaths.add(doc.getFilePath());
					for (Document doc: main.ps.getAllTrainDocs())
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

							main.ps.addTrainDoc(ProblemSet.getDummyAuthor(), new Document(path,ProblemSet.getDummyAuthor(),newTitle));
							titles.add(newTitle);
						} else {
							main.ps.addTrainDoc(ProblemSet.getDummyAuthor(), new Document(path,ProblemSet.getDummyAuthor(),file.getName()));
							titles.add(file.getName());
						}
					}

					GUIUpdateInterface.updateUserSampleDocTable(main);
				} else {
					Logger.logln(NAME+"Load user sample documents canceled");
				}
			}
		};
		main.PPSP.adduserSampleDocJButton.addActionListener(addUserSampleDocAL);

		// remove userSample documents button
		removeUserSampleDocAL = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Logger.logln(NAME+"'Remove Document(s)...' button clicked under the 'User Sample Documents' section on the documents tab.");

				if (main.PPSP.prepSampleDocsList.isSelectionEmpty()) {
					Logger.logln(NAME+"Failed removing user sample documents - no documents are selected",LogOut.STDERR);
					JOptionPane.showMessageDialog(null,
							"You must select documents to remove.",
							"Remove Documents Failure",
							JOptionPane.WARNING_MESSAGE);
				} else {
					int answer = JOptionPane.showConfirmDialog(null,
							"Are you sure you want to remove the selected documents?",
							"Remove Documents Confirmation",
							JOptionPane.YES_NO_OPTION);

					if (answer == 0) {
						int[] rows = main.PPSP.prepSampleDocsList.getSelectedIndices();
						String msg = "Removed test documents:\n";
						for (int i=rows.length-1; i>=0; i--) {
							msg += "\t\t> "+main.ps.trainDocAt(ProblemSet.getDummyAuthor(),rows[i]).getTitle()+"\n";
							titles.remove(main.ps.trainDocAt(ProblemSet.getDummyAuthor(),rows[i]).getTitle());
							main.ps.removeTrainDocAt(ProblemSet.getDummyAuthor(),rows[i]);
						}
						Logger.log(msg);

						GUIUpdateInterface.updateUserSampleDocTable(main);
					} else {
						Logger.logln(NAME+"Removing user sample documents canceled");
					}
				}
			}
		};
		main.PPSP.removeuserSampleDocJButton.addActionListener(removeUserSampleDocAL);

		// preview userSample document button
		//					main.userSampleDocPreviewJButton.addActionListener(new ActionListener() {
		//						
		//						@Override
		//						public void actionPerformed(ActionEvent e) {
		//							Logger.logln(NAME+"'Preview Document' button clicked under the 'User Sample Documents' section on the documents tab.");
		//							
		//							int row = main.userSampleDocsJTable.getSelectedRow();
		//							if (row == -1) {
		//								JOptionPane.showMessageDialog(null,
		//										"You must select a document in order to show its preview.",
		//										"Show Document Preview Error",
		//										JOptionPane.ERROR_MESSAGE);
		//								Logger.logln(NAME+"No user sample document is selected for preview",LogOut.STDERR);
		//							} else {
		//								Document doc = main.ps.trainDocAt(ProblemSet.getDummyAuthor(),row);
		//								try {
		//									doc.load();
		//									main.docPreviewNameJLabel.setText("- "+doc.getTitle());
		//									main.docPreviewJTextPane.setText(doc.stringify());
		//								} catch (Exception exc) {
		//									JOptionPane.showMessageDialog(null,
		//											"Failed opening document for preview:\n"+doc.getFilePath(),
		//											"Show Document Preview Error",
		//											JOptionPane.ERROR_MESSAGE);
		//									Logger.logln(NAME+"Failed opening user sample document for preview",LogOut.STDERR);
		//									Logger.logln(NAME+exc.toString(),LogOut.STDERR);
		//									GUIUpdateInterface.clearDocPreview(main);
		//								}
		//							}
		//						}
		//					});

		// training documents
		// ==================

		// training documents tree
		// -- none --

		// add author button
		//				main.addAuthorJButton.addActionListener(new ActionListener() {
		//					
		//					@Override
		//					public void actionPerformed(ActionEvent e) 
		//					{
		//						Logger.logln(NAME+"'Add Author...' button clicked under the 'Training Corpus' section on the documents tab.");
		//
		//						String answer = JOptionPane.showInputDialog(null,
		//								"Enter new author name:",
		//								"",
		//								JOptionPane.OK_CANCEL_OPTION);
		//						if (answer == null) 
		//						{
		//							Logger.logln(NAME+"Aborted adding new author");
		//						}
		//						else if (answer.isEmpty()) 
		//						{
		//							JOptionPane.showMessageDialog(null,
		//									"New author name must be a non-empty string.",
		//									"Add New Author Error",
		//									JOptionPane.ERROR_MESSAGE);
		//							Logger.logln(NAME+"tried to add new author with an empty string", LogOut.STDERR);
		//						} 
		//						else 
		//						{
		//							if (main.ps.getAuthorMap().keySet().contains(answer)) 
		//							{
		//								JOptionPane.showMessageDialog(null,
		//										"Author \""+answer+"\" already exists.",
		//										"Add New Author Error",
		//										JOptionPane.ERROR_MESSAGE);
		//								Logger.logln(NAME+"tried to add author that already exists: "+answer, LogOut.STDERR);
		//							} 
		//							else 
		//							{
		//								main.ps.addTrainDocs(answer, new ArrayList<Document>());
		//								GUIUpdateInterface.updateTrainDocTree(main);
		//								Logger.logln(NAME+"Added new author: "+answer);
		//							}
		//						}
		//					}
		//				});

		// add training documents button
		addTrainDocsAL = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Logger.logln(NAME+"'Add Document(s)...' button clicked under the 'Training Corpus' section on the documents tab.");

				boolean rename = false;
				String author = "no author entered";
				JFileChooser open = new JFileChooser();
				open.setMultiSelectionEnabled(true);
				File dir;

				try {
					dir = new File(new File(".").getCanonicalPath());
					open.setCurrentDirectory(dir);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				open.addChoosableFileFilter(new ExtFilter("Text files (*.txt)", "txt"));
				open.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				int answer = open.showOpenDialog(main);

				if (answer == JFileChooser.APPROVE_OPTION) {

					File[] files = open.getSelectedFiles();
					String msg = "Trying to load training documents for author \""+author+"\":\n";

					for (File file: files)
						msg += "\t\t> "+file.getAbsolutePath()+"\n";

					Logger.log(msg);

					String path = "";
					String skipList = "";
					ArrayList<String> allTrainDocPaths = new ArrayList<String>();
					ArrayList<String> allTestDocPaths = new ArrayList<String>();

					try {
						for (Document doc: main.ps.getTrainDocs(author)) {
							allTrainDocPaths.add(doc.getFilePath());
							Logger.logln(NAME+"Added to Train Docs: " + doc.getFilePath());
						}
					} catch(NullPointerException npe) {
						Logger.logln(NAME+"file '"+author+"' was not found. If name in single quotes is 'no author entered', this is not a problem.", LogOut.STDERR);
					}

					for (Document doc: main.ps.getTestDocs())
						allTestDocPaths.add(doc.getFilePath());
					if (main.ps.getTrainDocs(ProblemSet.getDummyAuthor()) != null) {
						for (Document doc: main.ps.getTrainDocs(ProblemSet.getDummyAuthor()))
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

								if (allTestDocPaths.contains(path)) {
									skipList += "\n"+path+" - already contained as a test document";
									continue;
								}

								if (titles.contains(newFile.getName())) {
									rename = true;
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

									main.ps.addTrainDocs(author, new ArrayList<Document>());
									main.ps.addTrainDoc(author, new Document(path,author,newTitle));
									titles.add(newTitle);
								} else {
									main.ps.addTrainDocs(author, new ArrayList<Document>());
									main.ps.addTrainDoc(author, new Document(path,author,newFile.getName()));
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

								main.ps.addTrainDoc(author, new Document(path,ProblemSet.getDummyAuthor(),newTitle));
								titles.add(newTitle);
							} else {
								main.ps.addTrainDoc(author, new Document(path,ProblemSet.getDummyAuthor(),file.getName()));
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

					GUIUpdateInterface.updateTrainDocTree(main);
					//GUIUpdateInterface.clearDocPreview(main);
				} else {
					Logger.logln(NAME+"Load training documents canceled");
				}
			}

		};
		main.PPSP.addTrainDocsJButton.addActionListener(addTrainDocsAL);

		// edit corpus name button
		//				main.trainNameJButton.addActionListener(new ActionListener() {
		//					
		//					@Override
		//					public void actionPerformed(ActionEvent e) {
		//						Logger.logln(NAME+"'Edit Name...' button clicked under the 'Training Corpus' section on the documents tab.");
		//						
		//						String answer = JOptionPane.showInputDialog(null,
		//								"Edit corpus name:",
		//								main.ps.getTrainCorpusName());
		//						if (answer == null) {
		//							Logger.logln(NAME+"Aborted editing corpus name");
		//						} else if (answer.isEmpty()) {
		//							JOptionPane.showMessageDialog(null,
		//									"Training corpus name must be a non-empty string.",
		//									"Edit Training Corpus Name Error",
		//									JOptionPane.ERROR_MESSAGE);
		//							Logger.logln(NAME+"tried to change training corpus name to an empty string", LogOut.STDERR);
		//						} else {
		//							main.ps.setTrainCorpusName(answer);
		//							GUIUpdateInterface.updateTrainDocTree(main);
		//						}
		//					}
		//				});


		// remove author button
		//				main.removeAuthorJButton.addActionListener(new ActionListener() {
		//					
		//					@Override
		//					public void actionPerformed(ActionEvent e) {
		//						Logger.logln(NAME+"'Remove Author(s)' button clicked under the 'Training Corpus' section on the documents tab.");
		//						
		//						TreePath[] paths = main.trainCorpusJTree.getSelectionPaths();
		//						List<DefaultMutableTreeNode> selectedAuthors = new ArrayList<DefaultMutableTreeNode>();
		//						if (paths != null)
		//							for (TreePath path: paths)
		//								if (path.getPath().length == 2)
		//									selectedAuthors.add((DefaultMutableTreeNode)path.getPath()[1]);
		//
		//						if (selectedAuthors.isEmpty()) {
		//							Logger.logln(NAME+"Failed removing authors - no authors are selected",LogOut.STDERR);
		//							JOptionPane.showMessageDialog(null,
		//									"You must select authors to remove.",
		//									"Remove Authors Failure",
		//									JOptionPane.WARNING_MESSAGE);
		//						} else {
		//							int answer = JOptionPane.showConfirmDialog(null,
		//									"Are you sure you want to remove the selected authors?",
		//									"Remove Authors Confirmation",
		//									JOptionPane.YES_NO_OPTION);
		//
		//							if (answer == 0) {
		//								String msg = "Removed authors:\n";
		//								for (DefaultMutableTreeNode author: selectedAuthors) {
		//									main.ps.removeAuthor(author.toString());
		//									msg += "\t\t> "+author.toString()+"\n";
		//								}
		//								Logger.log(msg);
		//								GUIUpdateInterface.updateTrainDocTree(main);
		//								GUIUpdateInterface.clearDocPreview(main);
		//							} else {
		//								Logger.logln(NAME+"Removing authors canceled");
		//							}
		//						}
		//					}
		//				});


		// remove training documents and/or authors button
		removeTrainDocsAL = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Logger.logln(NAME+"'Remove Document(s)/Author(s)' button clicked under the 'Training Corpus' section on the documents tab.");

				boolean removingAuthor = false;
				boolean removingAll = false;
				int docCounter = 0;
				TreePath[] paths = main.PPSP.trainCorpusJTree.getSelectionPaths();
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
					JOptionPane.showMessageDialog(null,
							"You must select training documents or authors to remove.",
							"Remove Training Documents Failure",
							JOptionPane.WARNING_MESSAGE);
				} else {
					int answer = 0;

					if (removingAuthor) {
						answer = JOptionPane.showConfirmDialog(null,
								"Are you sure you want to remove the selected author and all their documents?",
								"Remove Training Document's Author Confirmation",
								JOptionPane.YES_NO_OPTION);
					} else if (removingAll) {
						answer = JOptionPane.showConfirmDialog(null,
								"Are you sure you want to remove all authors and documents?",
								"Remove Training Document's Author Confirmation",
								JOptionPane.YES_NO_OPTION);
					}

					String msg;
					if (answer == 0) {
						if (removingAll) {
							msg = "Removed all:\n";
							for (DefaultMutableTreeNode authors: selectedDocs) {
								int size = main.ps.getTrainDocs(authors.toString()).size();
								for (int i = 0; i < size; i++) {
									titles.remove(main.ps.trainDocAt(authors.toString(), main.ps.getTrainDocs(authors.toString()).get(i).getTitle()).getTitle());
								}
								
								main.ps.removeAuthor(authors.toString());
								msg += "\t\t> " + authors.toString() + "\n";
							}
						} else if (removingAuthor) {
							msg = "Removed author:\n";
							for (DefaultMutableTreeNode author: selectedDocs) {
								int size = main.ps.getTrainDocs(author.toString()).size();
								for (int i = 0; i < size; i++) {
									titles.remove(main.ps.trainDocAt(author.toString(), main.ps.getTrainDocs(author.toString()).get(i).getTitle()).getTitle());
								}
								
								main.ps.removeAuthor(author.toString());
								msg += "\t\t> "+author.toString()+"\n";
							}
						} else {
							msg = "Removed training documents:\n";
							String author;
							for (DefaultMutableTreeNode doc: selectedDocs) {
								author = doc.getParent().toString();

								if (doc.getParent().getChildCount() == docCounter) {
									answer = JOptionPane.showConfirmDialog(null,
											"Are you sure you want to remove all of the selected author's documents?\n" +
													"All authors must have at least one document.",
													"Remove Training Documents Confirmation",
													JOptionPane.YES_NO_OPTION);

									if (answer == 1) {
										//												main.ps.removeAuthor(doc.getParent().toString());
										break;
									}

									docCounter = -1;
								}

								main.ps.removeTrainDocAt(author, doc.toString());
								titles.remove(main.ps.trainDocAt(author.toString(),doc.toString()).getTitle());
								msg += "\t\t> "+doc.toString()+"\n";
							}
						}
						Logger.log(msg);
						GUIUpdateInterface.updateTrainDocTree(main);
						GUIUpdateInterface.clearDocPreview(main);
					} else {
						Logger.logln(NAME+"Removing training documents/authors canceled");
					}
				}
			}
		};

		main.PPSP.removeTrainDocsJButton.addActionListener(removeTrainDocsAL);

		// preview training document button
		//				main.trainDocPreviewJButton.addActionListener(new ActionListener() {
		//					
		//					@Override
		//					public void actionPerformed(ActionEvent e) {
		//						Logger.logln(NAME+"'Preview Document' button clicked under the 'Training Corpus' section on the documents tab.");
		//						
		//						TreePath path = main.trainCorpusJTree.getSelectionPath();
		//						if (path == null || path.getPathCount() != 3) {
		//							JOptionPane.showMessageDialog(null,
		//									"You must select a training document in order to show its preview.",
		//									"Show Training Document Preview Error",
		//									JOptionPane.ERROR_MESSAGE);
		//							Logger.logln(NAME+"No training document is selected for preview",LogOut.STDERR);
		//						} else {
		//							String docTitle = path.getPath()[2].toString();
		//							Document doc = main.ps.trainDocAt(path.getPath()[1].toString(),docTitle);
		//							try {
		//								doc.load();
		//								main.docPreviewNameJLabel.setText("- "+doc.getTitle());
		//								main.docPreviewJTextPane.setText(doc.stringify());
		//							} catch (Exception exc) {
		//								JOptionPane.showMessageDialog(null,
		//										"Failed opening training document for preview:\n"+doc.getFilePath(),
		//										"Show Training Document Preview Error",
		//										JOptionPane.ERROR_MESSAGE);
		//								Logger.logln(NAME+"Failed opening training document for preview",LogOut.STDERR);
		//								Logger.logln(NAME+exc.toString(),LogOut.STDERR);
		//								GUIUpdateInterface.clearDocPreview(main);
		//							}
		//						}
		//					}
		//				});


		// document preview
		// ================

		// document preview clear button
		//				main.clearDocPreviewJButton.addActionListener(new ActionListener() {
		//					
		//					@Override
		//					public void actionPerformed(ActionEvent e) {
		//						Logger.logln(NAME+"'Clear Preview' button clicked on the documents tab.");
		//						
		//						GUIUpdateInterface.clearDocPreview(main);
		//					}
		//				});

		// button toolbar operations
		// =========================

		// about button
		// ============
		/*
				main.docsAboutJButton.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent arg0) {
						GUIUpdateInterface.showAbout(main);
					}
				});
		 */
		// next button
		//				main.docTabNextJButton.addActionListener(new ActionListener() {
		//					
		//					@Override
		//					public void actionPerformed(ActionEvent e) {
		//						Logger.logln(NAME+"'Next' button clicked on the documents tab.");
		//
		//						if (main.ps == null || !main.ps.hasAuthors() || !main.ps.hasTestDocs()) {
		//							JOptionPane.showMessageDialog(null,
		//									"You must set training corpus and test documents before continuing.",
		//									"Error",
		//									JOptionPane.ERROR_MESSAGE);
		//						} 
		//						
		//						else
		//							main.mainJTabbedPane.setSelectedIndex(1);
		//					}
		//				});
	}




	/*
	 * =====================
	 * Supporting operations
	 * =====================
	 */

	/**
	 * Extension File Filter
	 */
	public static class ExtFilter extends FileFilter {

		private String desc;
		private String[] exts;

		// constructors

		public ExtFilter(String desc, String[] exts) {
			this.desc = desc;
			this.exts = exts;
		}

		public ExtFilter(String desc, String ext) {
			this.desc = desc;
			this.exts = new String[] {ext};
		}

		// operations

		@Override
		public String getDescription() {
			return desc;
		}

		@Override
		public boolean accept(File f) {
			if (f.isDirectory()) return true;
			String path = f.getAbsolutePath().toLowerCase();
			for (String extension: exts) {
				if ((path.endsWith(extension) &&
						(path.charAt(path.length() - extension.length() - 1)) == '.'))
					return true;
			}
			return false;
		}
	}
}
