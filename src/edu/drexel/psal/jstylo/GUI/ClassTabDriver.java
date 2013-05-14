package edu.drexel.psal.jstylo.GUI;

import edu.drexel.psal.jstylo.analyzers.WekaAnalyzer;
import edu.drexel.psal.jstylo.analyzers.WriteprintsAnalyzer;
import edu.drexel.psal.jstylo.generics.Analyzer;
import edu.drexel.psal.jstylo.generics.Logger;
import edu.drexel.psal.jstylo.generics.Logger.LogOut;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import weka.classifiers.*;
import weka.classifiers.bayes.*;
import weka.classifiers.functions.*;
import weka.classifiers.lazy.*;
import weka.classifiers.rules.ZeroR;
import weka.classifiers.trees.*;
import weka.core.Option;
import weka.gui.GenericObjectEditor;

public class ClassTabDriver {

	/* =========================
	 * Classifiers tab listeners
	 * =========================
	 */ 
	
	protected static Analyzer tmpAnalyzer;
	protected static Object tmpObject;
	protected static String loadedClassifiers;
	protected static ClassWizard cw;
	
	/**
	 * Initialize all documents tab listeners.
	 */
	protected static void initListeners(final GUIMain main) {
		
		// available classifiers tree
		// ==========================
		
		main.classJTree.addTreeSelectionListener(new TreeSelectionListener() {
			
			@Override
			public void valueChanged(TreeSelectionEvent arg0) {
				// if unselected
				if (main.classJTree.getSelectionCount() == 0) {
					Logger.logln("Classifier tree unselected in the classifiers tab.");
					resetAvClassSelection(main);
					return;
				}
				
				// unselect selected list
				main.classJList.clearSelection();
				
				Object[] path = main.classJTree.getSelectionPath().getPath();
				DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)path[path.length-1];
				
				// if selected a classifier
				if (selectedNode.isLeaf()) {
					Logger.logln("Classifier selected in the available classifiers tree in the classifiers tab: "+selectedNode.toString());

					// get classifier
					String className = getClassNameFromPath(path).substring(5);
					tmpAnalyzer = null;
					tmpObject = null;
					try {
						Logger.logln("classname: "+className);
						tmpObject = Class.forName(className).newInstance();
						
						if (tmpObject instanceof Classifier){	//TODO hopefully this is the only "instanceOf" I'll need
							tmpAnalyzer = new WekaAnalyzer(Class.forName(className).newInstance());
						} else if (tmpObject instanceof WriteprintsAnalyzer){
							tmpAnalyzer = new WriteprintsAnalyzer();
						} else {
							Logger.logln("Tried to add an Analyzer we do not yet support");
						}	
							
					} catch (Exception e) {
						Logger.logln("Could not create classifier out of class: "+className);
						JOptionPane.showMessageDialog(main,
								"Could not generate classifier for selected class:\n"+className,
								"Classifier Selection Error",
								JOptionPane.ERROR_MESSAGE);
						e.printStackTrace();
						return;
					}
					Logger.logln("looking at analyzer class "+tmpAnalyzer.getClass());
					Logger.logln("with a classifier of: "+tmpAnalyzer.getName());
					
					main.classAvClassArgsJTextField.setText(getOptionsStr( tmpAnalyzer.getOptions()));

				}
					// otherwise
				else {
					resetAvClassSelection(main);
				}
			}
		});
		
		// classAvClassArgsJTextField
		// =========
		main.classAvClassArgsJTextField.addMouseListener(new MouseListener(){

			@Override
			public void mouseClicked(MouseEvent arg0) {
				if (tmpAnalyzer!=null){
					Logger.logln("clicked in textfield with a classifier selected!");
					
					cw = new ClassWizard(main,tmpAnalyzer);
					cw.setVisible(true);
					
				} else {
					Logger.logln("clicked in textfield without a classifier selected!");
				}
			}

			//not used
			@Override
			public void mouseEntered(MouseEvent arg0) {
			}
			@Override
			public void mouseExited(MouseEvent arg0) {
			}
			@Override
			public void mousePressed(MouseEvent arg0) {
			}
			@Override
			public void mouseReleased(MouseEvent arg0) {
			}
			
		});
		
		
		// add button
		// ==========
		
		main.classAddJButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Logger.logln("'Add' button clicked in the analysis tab.");

				// check if classifier is selected
				if (tmpAnalyzer == null) {
					JOptionPane.showMessageDialog(main,
							"You must select a classifier to be added.",
							"Add Classifier Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				
				} else {
					// check classifier options
					try {
						tmpAnalyzer.setOptions(main.classAvClassArgsJTextField.getText().split(" "));
					} catch (Exception e) {
						Logger.logln("Invalid options given for classifier.",LogOut.STDERR);
						JOptionPane.showMessageDialog(main,
								"The classifier arguments entered are invalid.\n"+
										"Restoring original options.",
										"Classifier Options Error",
										JOptionPane.ERROR_MESSAGE);
						
						main.classAvClassArgsJTextField.setText(getOptionsStr(tmpAnalyzer.getOptions()));

					}
					//ensure that the classifier hasn't already been added.
					boolean add = true;
					for (int i=0; i<main.classJList.getModel().getSize();i++){
						Logger.logln("Checking for duplicates...");
						if (main.analyzers.get(i).getName().toString().equals((tmpAnalyzer.getName().toString()))){ //same classifier
							Logger.logln("already added: "+main.analyzers.get(i).getName().toString()+" attempting to add: "+tmpAnalyzer.getName().toString());
							if(Arrays.equals(main.analyzers.get(i).getOptions(),(tmpAnalyzer.getOptions()))){ //same arguments
								add=false; //so don't add
							}
						}
					}
					// add classifier
					if (add){
						Logger.logln("Adding classifier...");
						
						main.analyzers.add(tmpAnalyzer);
						
						GUIUpdateInterface.updateClassList(main);
						resetAvClassSelection(main);
						main.classJTree.clearSelection();
					} else {
						Logger.logln("Duplicate classifier entered.",LogOut.STDERR);
						JOptionPane.showMessageDialog(main,
								"The classifier has already been entered with these arguments.\n"+
										"Use a different classifier or change the args.",
										"Classifier Options Error",
										JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		
		// selected classifiers list
		// =========================
		
		main.classJList.addListSelectionListener(new ListSelectionListener() {
			int lastSelected = -2;
			
			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				int selected = main.classJList.getSelectedIndex();
				if (selected == lastSelected)
					return;
				lastSelected = selected;
				
				// if unselected
				if (selected == -1) {
					Logger.logln("Classifier list unselected in the classifiers tab.");
					resetSelClassSelection(main);
					return;
				}

				// unselect available classifiers tree
				main.classJTree.clearSelection();

				String className = main.classJList.getSelectedValue().toString();//.substring(5);
				Logger.logln("Classifier selected in the selected classifiers list in the classifiers tab: "+className);

				// show options and description
				main.classSelClassArgsJTextField.setText(getOptionsStr(main.analyzers.get(selected).getOptions()));	
			}
		});
		
		// remove button
		// =============
		
		main.classRemoveJButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Logger.log("'Remove' button clicked in the classifiers tab.");
				int selected = main.classJList.getSelectedIndex();
				
				// check if selected
				if (selected == -1) {
					JOptionPane.showMessageDialog(main,
							"You must select a classifier to be removed.",
							"Remove Classifier Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				// remove classifier
				main.analyzers.remove(selected);
				GUIUpdateInterface.updateClassList(main);
			}
		});
		
		// about button
		// ============

		main.classAboutJButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				GUIUpdateInterface.showAbout(main);
			}
		});
		
		// back button
		// ===========
		
		main.classBackJButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Logger.logln("'Back' button clicked in the classifiers tab");
				main.mainJTabbedPane.setSelectedIndex(1);
			}
		});
		
		// next button
		// ===========
		
		main.classNextJButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Logger.logln("'Next' button clicked in the classifiers tab");
				
				if (main.analyzers.isEmpty()) {
					JOptionPane.showMessageDialog(main,
							"You must add at least one classifier.",
							"Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				} else {
					main.mainJTabbedPane.setSelectedIndex(3);
				}
			}
		});
	}
	
	/**
	 * Clears the GUI when no available classifier is selected.
	 */
	protected static void resetAvClassSelection(GUIMain main) {
		// clear everything
		tmpAnalyzer = null;
		main.classAvClassArgsJTextField.setText("");
	}
	
	/**
	 * Clears the GUI when no selected classifier is selected.
	 */
	protected static void resetSelClassSelection(GUIMain main) {
		// clear everything
		main.classSelClassArgsJTextField.setText("");
	}
	
	/**
	 * Creates a classifier options string.
	 */
	public static String getOptionsStr(String[] options) {
		String optionStr = "";
		
		if (options==null)
			return optionStr;
					
					
		for (String option: options)
			optionStr += option+" ";
		return optionStr;
	}
	
	
	/**
	 * Constructs the class name out of a tree path.
	 */
	protected static String getClassNameFromPath(Object[] path) {
		String res = "";
		for (Object o: path) {
			res += o.toString()+".";
		}
		res = res.substring(0,res.length()-1);
		return res;
	}
	
	/* ======================
	 * initialization methods
	 * ======================
	 */
	
	/**
	 * Initialize available classifiers tree
	 */
	@SuppressWarnings("unchecked")
	protected static void initWekaClassifiersTree(GUIMain main) {
		
		ArrayList<Node> loadedClassifiers = generateClassifiers();
		
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("root");
		
		for (Node n : loadedClassifiers){
			String name = n.getName();
			String[] components = name.split("\\."); 
			boolean add = true;
			
			DefaultMutableTreeNode child;
			Enumeration<DefaultMutableTreeNode> children = rootNode.children();
			while (children.hasMoreElements()) {
				child = children.nextElement();
				if (child.toString().equals(components[0])){
					add=false;
					break;
				}
			}
			
			if (add){
				DefaultMutableTreeNode temp = new DefaultMutableTreeNode(components[0]);
				DefaultMutableTreeNode previous = temp;
				for (int k=1; k<components.length;k++){
					DefaultMutableTreeNode t = new DefaultMutableTreeNode(components[k]);
					previous.add(t);
					previous=t;
				}
				rootNode.add(temp);
			}
		}
		
		DefaultTreeModel model = new DefaultTreeModel(rootNode);
		main.classJTree.setModel(model);
		
		String[] loadedClasses = loadedClassifiers(loadedClassifiers);
		
		int j=0;
		for (String c : loadedClasses){
			
			if (c.substring(c.length()-6).equals(".class"))
				loadedClasses[j]=c.substring(0,c.length()-6);
			else {
				loadedClasses[j]=c;
			}
			j++;
		}
		
		// add all classes		
		DefaultMutableTreeNode currNode, child;	
		for (String className: loadedClasses) {
			String[] nameArr = className.split("\\.");
			currNode = rootNode;
			
			for (int i=0; i<nameArr.length; i++) {
				// look for node
				Enumeration<DefaultMutableTreeNode> children = currNode.children();
				while (children.hasMoreElements()) {
					child = children.nextElement();
					if (child.getUserObject().toString().equals(nameArr[i])) { //this is the adding of a subsequent child to a node				
						currNode = child;
						break;
					}
				}
				
				// if not found, create a new one
				if (!currNode.getUserObject().toString().equals(nameArr[i])) {
					child = new DefaultMutableTreeNode(nameArr[i]);
					currNode.add(child);
					currNode = child;
				}
			}
		}
		
		// expand tree
		int row = 0;
		while (row < main.classJTree.getRowCount())
			main.classJTree.expandRow(row++);
	}
	/**
	 * Strings which represent the "root" directories for a given set of classifiers<br>
	 * These directories can contain subdirectories <br>
	 * 
	 * 
	 *<p>      Current usable args/filters<br>
	 *      <br>
	 *      "-P str"  ignore package "str" and all of its sub packages and files<br>
	 *      "-F str"  ignore file "str" File names must end in ".class" also, they're case sensitive<br>
	 * <br>
	 * 		UNUSABLE args, but nifty ideas, I think<br>
	 * <br>
	 * 		"-FC str" ignore a file containing a given string  On its own, not very useful, but perhaps...<br>
	 * 		"-FPC str" ignore a file, in a package, containing the string. I could see this being used in, say jgaap where there's one package with <br>
	 *      a huge number of classifiers that we may not want. (I don't know if jgaap's weka classifiers are basically the same as WEKA's or not <br>
	 *      but if they were, they could be filtered out in this manner. <br>
	 * </p>
	 * 
	 */
	protected static String[] classifierGroups = new String[] { //TODO move this into the properties file for ease of editing
		"edu.drexel.psal.jstylo.analyzers -F AuthorWPdata.class -F WekaAnalyzer.class -F SynonymBasedClassifier.class",
		"weka.classifiers.bayes",
		"weka.classifiers.functions",
		"weka.classifiers.lazy",
		"weka.classifiers.meta",
		"weka.classifiers.misc",
		"weka.classifiers.rules",
		"weka.classifiers.trees"
	};
	
	/**
	 * Used for loading classifiers on startup.
	 */
	protected static class Node{
		
		private String name;
		private ArrayList<Node> children;
		
		//All nodes are initialized as leaves
		protected Node(String n){
			name=n;
			children = new ArrayList<Node>();
		}
		
		//tells us if the node is a leaf or not
		protected boolean hasChildren(){
			if (children.size()==0)
				return false;
			else
				return true;
		}
		
		//add a leaf node
		protected void addChild(String s){
			children.add(new Node(s));
		}
		
		protected void addChild(Node n){
			children.add(n);
		}
		
		//returns the children
		protected ArrayList<Node> getChildren(){
			return children;
		}
		
		protected String getName(){
			return name;
		}

		@Override
		public String toString(){
			String msg = getName()+"\n";
			for (Node child : children){
				if (child.hasChildren())
					msg+=child.toString()+"";
				else
					msg+=child.toString()+"";
			}
			return msg;
		}
		
		
	}
	/**
	 * 
	 * Rakes up the leaves of the loaded classifier tree and returns them.
	 * I don't really like how this is necessary.
	 * At some point, I might try to change something higher up to remove the need for this.
	 * However, right now it's needed because the algorithm to populate the tree using a string array
	 * 		and I can't just switch it thanks to the complications with weka
	 * 
	 * @param loadedClassifiers : the ArrayLists/trees which contain the loaded classifiers to be converted
	 * @return all of the leaf nodes in an array instead of a tree
	 */
	protected static String[] loadedClassifiers(ArrayList<Node> loadedClassifiers){
		ArrayList<Node> temp = loadedClassifiers;
		ArrayList<String> classifiers = new ArrayList<String>();
		for (Node n : temp){
			String[] t = n.toString().split("\n");
			for (String s: t){
				if (s.contains(".class"))
					classifiers.add(s);
			}
		}
		
		String[] converted = new String[classifiers.size()];
		int i =0;
		for (String c : classifiers){
			converted[i]=c;
			i++;
		}	
		return converted;
	}
	
	//TODO could use some cleanup
	/**
	*		Returns the a list of nodes for the tree of classifiers.
	**/
	protected static ArrayList<Node> generateClassifiers(){
		
		ArrayList<Node> modules = new ArrayList<Node>();	
		ArrayList<String> packagesToIgnore = new ArrayList<String>();
		ArrayList<String> filesToIgnore = new ArrayList<String>();
				
		//adds all of the classifier groups to the tree--these are all the "parents"
		for (String ID: classifierGroups){
			String[] components = ID.split(" ");

			if (components.length!=1){ //arg extraction if len==1 there are no args, skip and add the module
				
				for (int i=1; i<components.length;i++){ //skip the first component as that's the parent name
					
					//check which arg it has, if its invalid break the loop and complain.
					//if it is valid, increment i by another index, as each arg-flag is on even indices
					if (components[i].equalsIgnoreCase("-P")){
						Logger.logln("Adding directory to ignore: "+components[i+1]);
						packagesToIgnore.add(components[i+1]);
						i++; 
					} else if (components[i].equalsIgnoreCase("-F")){
						Logger.logln("Adding file to ignore: "+components[i+1]);
						filesToIgnore.add(components[i+1]);
						i++;
					} else {
						Logger.log("Invalid arguments in classifier loader source string!", LogOut.STDERR);
						break;
					}
				}
			}	
			
			modules.add(new Node(components[0]));
		}

		for (Node current: modules){
			populateNode(current,packagesToIgnore,filesToIgnore); //populates each node and all its subnodes
			//Logger.logln("\nClassifier Tree for "+current.toString()); // use this to see each tree ---I think it's pretty useful
		}

		return modules;	
	}
	
	/**
	 * 
	 * Populates the tree with all of the .class files that are located in the directories supplied by the source string.
	 * Not quite dynamically importing them, but a big improvement on doing it manually.
	 * 
	 * @param current		the current node to be populated
	 * @param packagesToIgnore		the blacklist containing packages we don't want in our tree
	 * @param filesToIgnore		the blacklist containing files we don't want in our tree
	 * @return
	 */
	protected static Node populateNode(Node current,ArrayList<String> packagesToIgnore,ArrayList<String> filesToIgnore){
		
		//Logger.logln("node to populate: "+current.getName());
		
		//non-leaf
		if (!current.getName().substring(current.getName().lastIndexOf(".")).equals(".class")){	//if it is not a .class, it is a directory
			
			URL resource = ClassLoader.getSystemClassLoader().getResource(new String(current.getName().replace(".","/")));
			File df = null;
			
			try { //loads the path to the resource. doubles as a test to see if we're in a jar
				df = new File(resource.toURI());
			} catch (Exception e) {
				//Logger.logln("Reading from jar file..."); 
			} 
			
			//for non-jars
			if (df!=null && df.exists()){
				
				//check to make sure this directory isn't on the ignore list
				for (String ignore : packagesToIgnore){
					if (df.toString().replace("/",".").contains(ignore)){
						return null;
					}
				}
				
				//since it's a directory, get all of its contents so we can add/populate them
				File directory = new File(resource.getPath());
				File[] files = directory.listFiles();
				
				//create and populate the child nodes
				for (File file : files){
					
					Node temp = new Node(current.getName()+"."+file.getName());
					boolean toAdd = true;		
					//check to see if the file is on the ignore list
					if (file.isFile()){
						for (String ignore : filesToIgnore){
							if (temp.getName().equalsIgnoreCase(ignore)|| file.getName().equalsIgnoreCase(ignore)){
								toAdd=false;
							}
						}
					}
					//check to see if directory is on ignore list
					else if (file.isDirectory()){
						for (String ignore : packagesToIgnore){
							if (temp.getName().equalsIgnoreCase(ignore)||file.getName().equalsIgnoreCase(ignore)){
								toAdd=false;
							}
						}
					}
					
					if (toAdd){
						populateNode(temp,packagesToIgnore,filesToIgnore);
						current.addChild(temp);
					}
				}
				
				//the node is now populated. add it to the above
				return current;
				
			} //end non-jars
			
			//here is where the weka/jar related stuff will be handled
			else { 
				try { 
					//set up jar path and information
					JarFile source = new JarFile(resource.getFile().replaceFirst("[.]jar[!].*",".jar").replaceFirst("file:",""));
					Enumeration<JarEntry> files = source.entries();
					
					//iterate over subfiles
					while(files.hasMoreElements()){
						JarEntry file = files.nextElement();
						String fileName = file.toString();
						
						if (fileName.contains(current.getName().replace(".","/"))){
							String[] splitFile = fileName.split("/");
							String[] splitCurrent = current.getName().split("\\.");
							
							if (!(splitFile.length<=splitCurrent.length) && splitFile[splitCurrent.length].endsWith(".class") && !splitFile[splitCurrent.length].contains("$")){
								
								String path = fileName.replaceAll("/",".");
								path+="..."; //kinda hacky way to separate the .class portion of the filename without removing class from inside the string
								path=path.replaceAll("\\.class\\.\\.\\.",""); 
		
								Class tempClass = Class.forName(path);
								boolean skip = false;
								
								//don't add the file if it's an abstract class
								if (Modifier.isAbstract(tempClass.getModifiers()))
									skip=true;
								
								//or if its an interface
								if (Modifier.isInterface(tempClass.getModifiers()))
									skip=true;
								
								//check to see if it's on the ignore list
								for (String ignore : filesToIgnore){
									if (path.equalsIgnoreCase(ignore)|| file.getName().equalsIgnoreCase(ignore)){
										skip=true;
									}
								}
								
								//add the file
								if (!skip)
									current.addChild(new Node(path));
							}
						} 
					}
					
				} catch (Exception e){ //if we're unsuccessful for some reason
					Logger.logln("could not load node "+current.getName());
					e.printStackTrace();
				}
				return null;
			}
			
			
		} //end directory
		
		//leaf node
		else if (current.getName().substring(current.getName().lastIndexOf(".")).equals(".class")){
			return null; //So it doesn't need to be to populated. We're done here.
		}
		
		//should be unreachable, but is here just in case.
		else {
			Logger.logln("Welp that wasn't supposed to happen");
			return null;
		}
	}
}
