package edu.drexel.psal.anonymouth.gooie;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import weka.classifiers.Classifier;

import edu.drexel.psal.jstylo.generics.*;
import edu.drexel.psal.jstylo.generics.FeatureDriver.ParamTag;

import com.jgaap.generics.*;

import java.util.*;

public class GUIUpdateInterface {
	
	@SuppressWarnings("unused")
	private final String NAME = "( "+this.getClass().getName()+" ) - ";

	// about dialog
	// ============
	
	protected static String version = "0.0.4";
/*
	protected static void showAbout(GUIMain main) {
		ImageIcon logo = new ImageIcon("./anonymouth_LOGO_v2.png", "Anonymouth Logo");
		String content =
				"<html><p>" +
				"<h3>Anonymouth</h3><br>" +
				"Version "+version+"<br>" +
				"Author: Andrew W.E. McDonald<br>" +
				"Privacy, Security and Automation Lab (PSAL)<br>" +
				"Drexel University<br>" +
				"<br>" +
				"Powered by: JSylo,Weka, JGAAP" +
				"</p></html>";
		
		JOptionPane.showMessageDialog(main, 
				content,
				"About JStylo",
				JOptionPane.INFORMATION_MESSAGE,
				logo);
	}
*/
	/* ========================
	 * documents tab operations
	 * ========================
	 */
	
	public static void updateDocPrepColor(GUIMain main)
	{
		if (main.documentsAreReady())
		{
			main.prepDocLabel.setBackground(main.ready);
			main.PPSP.prepDocLabel.setBackground(main.ready);
		}
		else
		{
			main.prepDocLabel.setBackground(main.notReady);
			main.PPSP.prepDocLabel.setBackground(main.notReady);
		}	
	}
	
	public static void updateDocLabel(GUIMain main, String title) {
		main.documentLabel.setText("Document: " + title);
	}
	
	/**
	 * Updates the documents tab view with the current problem set.
	 */
	protected static void updateProblemSet(GUIMain main) {
		Logger.logln("(GUIUpdateInterface) - GUI Update: update documents tab with current problem set started");
		
		// update test documents table
		updateTestDocTable(main);
		
		// update training corpus tree
		updateTrainDocTree(main);
		
		// update user sample documents table
		updateUserSampleDocTable(main);
		
		updateDocPrepColor(main);
	}
	
	/**
	 * Updates the test documents table with the current problem set. 
	 */
	protected static void updateTestDocTable(GUIMain main) 
	{
		DefaultListModel<String> dlm = (DefaultListModel<String>)main.prepMainDocList.getModel();
		DefaultListModel<String> dlm2 = (DefaultListModel<String>)main.PPSP.prepMainDocList.getModel();
		dlm.removeAllElements();
		dlm2.removeAllElements();
		if (main.mainDocReady())
		{
			List<Document>testDocs = main.ps.getTestDocs();
			for (int i=0; i<testDocs.size(); i++)
			{
				dlm.addElement(testDocs.get(i).getTitle());
				dlm2.addElement(testDocs.get(i).getTitle());
				System.out.println(testDocs.get(i).getTitle());
				System.out.println(main.ps.testDocAt(0));
				main.mainDocPreview = main.ps.testDocAt(0);
				try {
					main.mainDocPreview.load();
				} catch (Exception e) {
					System.err.println("ANOTHER PROBLEM!");
					e.printStackTrace();
				}
				try {
					System.out.println(main.mainDocPreview.stringify());
					main.getDocumentPane().setText(main.mainDocPreview.stringify());
				} catch (Exception e) {
					System.err.println("PROBLEM!");
					e.printStackTrace();
				}
			}
			updateDocLabel(main, testDocs.get(0).getTitle());
		}
		updateDocPrepColor(main);
	}
	
	/**
	 * Updates the User Sample documents table with the current problem set. 
	 */
	protected static void updateUserSampleDocTable(GUIMain main) {
		DefaultListModel<String> dlm = (DefaultListModel<String>)main.prepSampleDocsList.getModel();
		DefaultListModel<String> dlm2 = (DefaultListModel<String>)main.PPSP.prepSampleDocsList.getModel();
		dlm.removeAllElements();
		dlm2.removeAllElements();
		if (main.sampleDocsReady())
		{
			List<Document> userSampleDocs = main.ps.getTrainDocs(ProblemSet.getDummyAuthor());
			for (int i=0; i<userSampleDocs.size(); i++)
			{
				dlm.addElement(userSampleDocs.get(i).getTitle());// todo this is where it fails (from the note in DocsTabDriver).. it fails with a "NullPointerException".... (when "create new problem set" is clicked when there isn't a problem set there. [ i.e. as soon as Anonymouth starts up]) 
				dlm2.addElement(userSampleDocs.get(i).getTitle());
			}
		}
		updateDocPrepColor(main);
	}

	/**
	 * Updates the training corpus tree with the current problem set. 
	 */
	protected static void updateTrainDocTree(GUIMain main) {
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(main.ps.getTrainCorpusName());
		Map<String,List<Document>> trainDocsMap = main.ps.getAuthorMap();
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
		main.trainCorpusJTree.setModel(trainTreeModel);
		main.PPSP.trainCorpusJTree.setModel(trainTreeModel);
		
		updateDocPrepColor(main);
	}
	
	/**
	 * Clears the text from the document preview text box.
	 */
	protected static void clearDocPreview(GUIMain main) {
		main.getDocumentPane().setText("This is where the latest version of your document will be.");
		main.documentLabel.setText("Document:");
	}
	
	
	/*
	 * =======================
	 * features tab operations
	 * =======================
	 */
	
	public static void updateFeatPrepColor(GUIMain main)
	{
		if (main.featuresAreReady())
		{
			main.prepFeatLabel.setBackground(main.ready);
			main.PPSP.prepFeatLabel.setBackground(main.ready);
		}
		else
		{
			main.prepFeatLabel.setBackground(main.notReady);
			main.PPSP.prepFeatLabel.setBackground(main.notReady);
		}	
	}
	
	/**
	 * Updates the feature set view when a new feature set is selected / created.
	 */
	protected static void updateFeatureSetView(GUIMain main) {
		CumulativeFeatureDriver cfd = main.cfd;
		
		// update name
		//main.PPSP.featuresSetNameJTextField.setText(cfd.getName() == null ? "" : cfd.getName());
		
		// update description
		main.PPSP.featuresSetDescJTextPane.setText(cfd.getDescription() == null ? "" : cfd.getDescription());
		
		// update list of features
		clearFeatureView(main);
		main.PPSP.featuresJListModel.removeAllElements();
		for (int i=0; i<cfd.numOfFeatureDrivers(); i++) 
			main.PPSP.featuresJListModel.addElement(cfd.featureDriverAt(i).getName());
	}
	
	/**
	 * Updates the feature view when a feature is selected in the features tab.
	 */
	protected static void updateFeatureView(GUIMain main, int selected) {
		// clear all
		clearFeatureView(main);
		
		// unselected
		if (selected == -1)
			return;
		
		// selected
		FeatureDriver fd = main.cfd.featureDriverAt(selected);
		
		// name and description
		main.PPSP.featuresFeatureNameJTextPane.setText(fd.getName());
		main.PPSP.featuresFeatureDescJTextPane.setText(fd.getDescription());
		
		// update normalization
		main.PPSP.featuresNormContentJTextPane.setText(fd.getNormBaseline().getTitle());
		main.PPSP.featuresFactorContentJTextPane.setText(fd.getNormFactor().toString());
		
		// update feature extractor
		main.PPSP.featuresFeatureExtractorContentJTableModel.addRow(new String[] {fd.getUnderlyingEventDriver().displayName()});
		populateTableWithParams(fd.getUnderlyingEventDriver(), main.PPSP.featuresFeatureExtractorConfigJTableModel);
		
		// update canonicizers
		List<Canonicizer> canons = fd.getCanonicizers();
		if (canons != null) {
			for (int i=0; i<canons.size(); i++)
			{
				main.PPSP.featuresCanonJTableModel.addRow(new String[]{canons.get(i).displayName()});
				populateTableWithParams(canons.get(i), main.PPSP.featuresCanonConfigJTableModel);
			}
		}
		else
		{
			main.PPSP.featuresCanonJTableModel.addRow(new String[]{"N/A"});
			main.PPSP.featuresCanonConfigJTableModel.addRow(new String[]{"N/A", "N/A", "N/A"});
		}
		
		// update cullers
		List<EventCuller> cullers = fd.getCullers();
		if (cullers != null) {
			for (int i=0; i<cullers.size(); i++)
			{
				main.PPSP.featuresCullJTableModel.addRow(new String[]{cullers.get(i).displayName()});
				populateTableWithParams(cullers.get(i), main.PPSP.featuresCullConfigJTableModel);
			}
		}
		else
		{
			main.PPSP.featuresCullJTableModel.addRow(new String[]{"N/A"});
			main.PPSP.featuresCullConfigJTableModel.addRow(new String[]{"N/A", "N/A", "N/A"});
		}
	}
	
	/**
	 * Resets the feature view in the features tab.
	 */
	protected static void clearFeatureView(GUIMain main) {
		main.PPSP.featuresFeatureNameJTextPane.setText("");
		main.PPSP.featuresFeatureDescJTextPane.setText("");
		main.PPSP.featuresFeatureExtractorContentJTableModel.getDataVector().removeAllElements();
		main.PPSP.featuresFeatureExtractorConfigJTableModel.getDataVector().removeAllElements();
		main.PPSP.featuresCanonJTableModel.getDataVector().removeAllElements();
		main.PPSP.featuresCanonConfigJTableModel.getDataVector().removeAllElements();
		main.PPSP.featuresCullJTableModel.getDataVector().removeAllElements();
		main.PPSP.featuresCullConfigJTableModel.getDataVector().removeAllElements();
		main.PPSP.featuresNormContentJTextPane.setText("");
		main.PPSP.featuresFactorContentJTextPane.setText("");
	}
	
	/**
	 * Populates the given tableModel with parameters and their values for the given event driver / canonicizer / culler. Assumes the table is set to have three columns.
	 */
	protected static void populateTableWithParams(Parameterizable p, DefaultTableModel tm) {
		String fullname = p.getClass().getName();
		List<Pair<String,ParamTag>> params = FeatureDriver.getClassParams(fullname);
		
		boolean allParamsNull = true;
		
		for (Pair<String,ParamTag> param: params) 
		{
			if (param != null)
				allParamsNull = false;
			else
				continue;
		}
				
		if (!allParamsNull)
			for (Pair<String,ParamTag> param: params) 
				tm.addRow(new String[] {fullname.substring(fullname.lastIndexOf(".")+1), param.getFirst(), p.getParameter(param.getFirst())});
		else
			tm.addRow(new String[] {fullname.substring(fullname.lastIndexOf(".")+1), "N/A", "N/A"});
	}
	
//	/**
//	 * Creates a panel with parameters and their values for the given event driver / canonicizer / culler.
//	 */
//	protected static JPanel getParamPanel(Parameterizable p) {
//		List<Pair<String,ParamTag>> params = FeatureDriver.getClassParams(p.getClass().getName());
//		
//		JPanel panel = new JPanel(new GridLayout(params.size(),2,5,5));
//		for (Pair<String,ParamTag> param: params) {
//			JLabel name = new JLabel(param.getFirst()+": ");
//			name.setVerticalAlignment(JLabel.TOP);
//			panel.add(name);
//			JLabel value = new JLabel(p.getParameter(param.getFirst()));
//			value.setVerticalAlignment(JLabel.TOP);
//			panel.add(value);
//		}
//		
//		return panel;
//	}
	
	
	/* ===============
	 * Classifiers tab
	 * ===============
	 */
	
	public static void updateClassPrepColor(GUIMain main)
	{
		if (main.classifiersAreReady())
		{
			main.prepClassLabel.setBackground(main.ready);
			main.PPSP.prepClassLabel.setBackground(main.ready);
		}
		else
		{
			main.prepClassLabel.setBackground(main.notReady);
			main.PPSP.prepClassLabel.setBackground(main.notReady);
		}	
	}
	
	/**
	 * Updates the list of selected classifiers with respect to the list of classifiers.
	 */
	protected static void updateClassList(GUIMain main) {
//		DefaultListModel model = (DefaultListModel)main.classJList.getModel();
		DefaultListModel<String> model2 = (DefaultListModel<String>)main.PPSP.classJList.getModel();
		List<Classifier> classifiers = main.classifiers;
		
//		model.removeAllElements();
		model2.removeAllElements();
		for (Classifier c: classifiers) {
			String className = c.getClass().getName();
//			model.addElement(className.substring(className.lastIndexOf(".")+1));
			model2.addElement(className);
		}
	}
	
	protected static void updateResultsPrepColor(GUIMain main) {
		if (main.resultsAreReady())
			main.resultsTableLabel.setBackground(main.ready);
		else
			main.resultsTableLabel.setBackground(main.blue);
	}
}