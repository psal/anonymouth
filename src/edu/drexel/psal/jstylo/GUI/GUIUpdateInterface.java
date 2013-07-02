package edu.drexel.psal.jstylo.GUI;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import edu.drexel.psal.JSANConstants;
import edu.drexel.psal.jstylo.generics.Analyzer;
import edu.drexel.psal.jstylo.generics.CumulativeFeatureDriver;
import edu.drexel.psal.jstylo.generics.FeatureDriver;
import edu.drexel.psal.jstylo.generics.Logger;
import edu.drexel.psal.jstylo.generics.FeatureDriver.ParamTag;

import com.jgaap.generics.*;

import java.awt.GridLayout;
import java.util.*;

public class GUIUpdateInterface {

	// about dialog
	// ============
	
	protected static String version = "1.1";
	
	protected static void showAbout(GUIMain main) {
		ImageIcon logo = new ImageIcon(Thread.currentThread().getClass().getResource(JSANConstants.JSAN_GRAPHICS_PREFIX+"logo.png"), "JStylo Logo");
		String content =
				"<html><p>" +
				"<h3>JStylo</h3><br>" +
				"Version "+version+"<br>" +
				"Authors: Ariel Stolerman and Travis Dutko<br>" +
				"Privacy, Security and Automation Lab (PSAL)<br>" +
				"Drexel University<br>" +
				"<br>" +
				"Powered by: Weka, JGAAP" +
				"</p></html>";
		
		JOptionPane.showMessageDialog(main, 
				content,
				"About JStylo",
				JOptionPane.INFORMATION_MESSAGE,
				logo);
	}
	
	/* ========================
	 * documents tab operations
	 * ========================
	 */
	
	/**
	 * Updates the documents tab view with the current problem set.
	 */
	protected static void updateProblemSet(GUIMain main) {
		Logger.logln("GUI Update: update documents tab with current problem set started");
		
		// update test documents table
		updateTestDocTree(main);
		
		// update training corpus tree
		updateTrainDocTree(main);
		
		// update preview box
		clearDocPreview(main);
	}
	
	/**
	 * Updates the test documents table with the current problem set. 
	 */
	/*
	protected static void updateTestDocTable(GUIMain main) {
		JTable testDocsTable = main.testDocsJTable;
		DefaultTableModel testTableModel = main.testDocsTableModel;
		testDocsTable.clearSelection();
		testTableModel.setRowCount(0);
		List<Document> testDocs = main.ps.getTestDocs();
		Collections.sort(testDocs,new Comparator<Document>() {
			public int compare(Document o1, Document o2) {
				return o1.getTitle().compareTo(o2.getTitle());
			}
		});
		for (int i=0; i<testDocs.size(); i++)
			testTableModel.addRow(new Object[]{
					testDocs.get(i).getTitle(),
					testDocs.get(i).getFilePath()
			});
	}*/
	protected static void updateTestDocTree(GUIMain main) {
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Test Docs");
		Map<String,List<Document>> testDocsMap = main.ps.getTestAuthorMap();
		DefaultMutableTreeNode authorNode, docNode;
		List<String> authorsSorted = new ArrayList<String>(testDocsMap.keySet());
		Collections.sort(authorsSorted);
		for (String author: authorsSorted) {
			authorNode = new DefaultMutableTreeNode(author);
			root.add(authorNode);
			List<Document> docs = testDocsMap.get(author);
			Collections.sort(docs,new Comparator<Document>() {
				public int compare(Document o1, Document o2) {
					return o1.getTitle().compareTo(o2.getTitle());
				}
			});
			for (Document doc: docs){
				docNode = new DefaultMutableTreeNode(doc.getTitle());
				authorNode.add(docNode);
			}
		}
		
		if (root.getChildCount()==0){
			DefaultMutableTreeNode _Unknown_ = new DefaultMutableTreeNode("_Unknown_");
			root.add(_Unknown_);
		}
		
		DefaultTreeModel testTreeModel = new DefaultTreeModel(root);
		main.testDocsJTree.setModel(testTreeModel);
	}
	
	
	/**
	 * Updates the training corpus tree with the current problem set. 
	 */
	protected static void updateTrainDocTree(GUIMain main) {
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(main.ps.getTrainCorpusName());
		Map<String,List<Document>> trainDocsMap = main.ps.getAuthorMap();
		DefaultMutableTreeNode authorNode, docNode;
		List<String> authorsSorted = new ArrayList<String>(trainDocsMap.keySet());
		Collections.sort(authorsSorted);
		for (String author: authorsSorted) {
			authorNode = new DefaultMutableTreeNode(author);
			root.add(authorNode);
			List<Document> docs = trainDocsMap.get(author);
			Collections.sort(docs,new Comparator<Document>() {
				public int compare(Document o1, Document o2) {
					return o1.getTitle().compareTo(o2.getTitle());
				}
			});
			for (Document doc: docs){
				docNode = new DefaultMutableTreeNode(doc.getTitle());
				authorNode.add(docNode);
			}
		}
		DefaultTreeModel trainTreeModel = new DefaultTreeModel(root);
		main.trainCorpusJTree.setModel(trainTreeModel);
	}
	
	/**
	 * Clears the text from the document preview text box.
	 */
	protected static void clearDocPreview(GUIMain main) {
		main.docPreviewJTextPane.setText("");
		main.docPreviewNameJLabel.setText("");
	}
	
	
	/*
	 * =======================
	 * features tab operations
	 * =======================
	 */
	
	/**
	 * Updates the feature set view when a new feature set is selected / created.
	 */
	@SuppressWarnings("unchecked")
	protected static void updateFeatureSetView(GUIMain main) {
		CumulativeFeatureDriver cfd = main.cfd;
		
		// update name
		main.featuresSetNameJTextField.setText(cfd.getName() == null ? "" : cfd.getName());
		
		// update description
		main.featuresSetDescJTextPane.setText(cfd.getDescription() == null ? "" : cfd.getDescription());
		
		// update list of features
		clearFeatureView(main);
		main.featuresJListModel.removeAllElements();
		for (int i=0; i<cfd.numOfFeatureDrivers(); i++) 
			main.featuresJListModel.addElement(cfd.featureDriverAt(i).getName());
	}
	
	/**
	 * Updates the feature view when a feature is selected in the features tab.
	 */
	@SuppressWarnings("unchecked")
	protected static void updateFeatureView(GUIMain main, int selected) {
		// clear all
		clearFeatureView(main);
		// unselected
		if (selected == -1)
			return;
		
		// selected
		FeatureDriver fd = main.cfd.featureDriverAt(selected);
		
		// name and description
		main.featuresFeatureNameJTextField.setText(fd.getName());
		main.featuresFeatureDescJTextPane.setText(fd.getDescription());
		
		// update feature extractor
		main.featuresFeatureExtractorContentJLabel.setText(fd.getUnderlyingEventDriver().displayName());
		main.featuresFeatureExtractorConfigJScrollPane.setViewportView(getParamPanel(fd.getUnderlyingEventDriver()));
		
		// update canonicizers
		List<Canonicizer> canons = fd.getCanonicizers();
		if (canons != null) {
			for (int i=0; i<canons.size(); i++)
				main.featuresCanonJListModel.addElement(canons.get(i).displayName());
		}
		
		// update cullers
		List<EventCuller> cullers = fd.getCullers();
		if (cullers != null) {
			for (int i=0; i<cullers.size(); i++)
				main.featuresCullJListModel.addElement(cullers.get(i).displayName());
		}

		// update normalization
		main.featuresNormContentJLabel.setText(fd.getNormBaseline().getTitle());
		main.featuresFactorContentJLabel.setText(fd.getNormFactor().toString());
	}
	
	/**
	 * Resets the feature view in the features tab.
	 */
	protected static void clearFeatureView(GUIMain main) {
		main.featuresFeatureNameJTextField.setText("");
		main.featuresFeatureDescJTextPane.setText("");
		main.featuresFeatureExtractorContentJLabel.setText("");
		main.featuresFeatureExtractorConfigJScrollPane.setViewportView(null);
		main.featuresCanonJListModel.removeAllElements();
		main.featuresCanonConfigJScrollPane.setViewportView(null);
		main.featuresCullJListModel.removeAllElements();
		main.featuresCullConfigJScrollPane.setViewportView(null);
		main.featuresNormContentJLabel.setText("");
		main.featuresFactorContentJLabel.setText("");
	}
	
	/**
	 * Creates a panel with parameters and their values for the given event driver / canonicizer / culler.
	 */
	protected static JPanel getParamPanel(Parameterizable p) {
		List<Pair<String,ParamTag>> params = FeatureDriver.getClassParams(p.getClass().getName());
		
		JPanel panel = new JPanel(new GridLayout(params.size(),2,1,1));
		for (Pair<String,ParamTag> param: params) {
			String temp;
			JLabel name = new JLabel(param.getFirst()+": ");
			name.setVerticalAlignment(JLabel.TOP);
			panel.add(name);
			temp = p.getParameter(param.getFirst());
			if (temp.length()>40){
				String tmp = "<HTML>"+temp.substring(0,24)+"<br>"+temp.substring(24,48)
						+"<br>"+temp.substring(48)+"</HTML>";
				temp=tmp;
			} else if (temp.length()>24){
				String tmp = "<HTML>"+temp.substring(0,12)+"<br>"+temp.substring(12)+"</HTML>";
				temp=tmp;
			}
			JLabel value = new JLabel(temp);
			value.setVerticalAlignment(JLabel.TOP);
			panel.add(value);
		}
		
		return panel;
	}
	
	
	/* ===============
	 * Classifiers tab
	 * ===============
	 */
	
	/**
	 * Updates the list of selected classifiers with respect to the list of classifiers.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected static void updateClassList(GUIMain main) {
		DefaultComboBoxModel model = main.classSelClassJListModel;
		List<Analyzer> analyzers = main.analyzers;
		
		model.removeAllElements();
		for (Analyzer a: analyzers) {		
			model.addElement(a.getName());	
		}
	}
}

















