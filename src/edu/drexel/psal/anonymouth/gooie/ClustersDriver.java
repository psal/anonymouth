package edu.drexel.psal.anonymouth.gooie;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.*;

import net.miginfocom.swing.MigLayout;

import edu.drexel.psal.anonymouth.engine.Attribute;
import edu.drexel.psal.anonymouth.engine.Cluster;
import edu.drexel.psal.anonymouth.engine.ClusterAnalyzer;
import edu.drexel.psal.anonymouth.engine.ClusterGroup;
import edu.drexel.psal.jstylo.generics.Logger;

public class ClustersDriver {
	
	private final static String NAME = "( ClustersDriver ) - ";

	private static int lenJPanels;
	public static boolean clusterGroupReady = false;
	public static ClusterGroup bestClusterGroup;
	private static ClusterGroup[] clusterGroupRay;
	private static int lenCGR;
	private static int[][] intRepresentation;
	private static String[] stringRepresentation;
	protected static JPanel[] finalPanels;
	protected static JLabel[] nameLabels;
	protected static JPanel[] clusterPanels;
	protected static int numFeatures;
	protected static int[] selectedClustersByFeature;

	public static int[][] getIntRep() {
		return intRepresentation;
	}

	public static String[] getStringRep() {
		return stringRepresentation;
	}

	public static boolean setClusterGroup() {
		Logger.logln(NAME+"Cluster group array retrieved from ClusterAnalyzer and brought to ClusterViewerDriver");
		if(clusterGroupReady) {
			clusterGroupRay = ClusterAnalyzer.getClusterGroupArray();
			lenCGR = clusterGroupRay.length;
			return true;
		} else
			return false;
	}

	public static void makePanels(Attribute[] theOnesYouWantToSee) {
		//System.out.println("length of theOnesYouWantToSee: "+theOnesYouWantToSee.length);
		int numFeatures = theOnesYouWantToSee.length;
		double[] minimums = new double[numFeatures]; 
		double[] maximums = new double[numFeatures];
		double[] authorMin = new double[numFeatures];
		double[] authorMax = new double[numFeatures];
		double[] presentValues = new double[numFeatures];
		String[] names = new String[numFeatures];

		int i = 0;
		ArrayList<Cluster[]> everySingleCluster = new ArrayList<Cluster[]>(numFeatures);
		double tempMinMax;
		String tempString;
		String dashes;
		selectedClustersByFeature = new int[numFeatures];
		double tempAuthorMinMax;
		
		for (i=0; i< numFeatures;i++) {
			selectedClustersByFeature[i] = -1; // initialize with no clusters selected;
			everySingleCluster.add(i,theOnesYouWantToSee[i].getOrderedClusters());
			authorMin[i] = theOnesYouWantToSee[i].getAuthorAvg() - theOnesYouWantToSee[i].getAuthorConfidence();
			if(authorMin[i] <  0)
				authorMin[i] = 0;
			authorMax[i] = theOnesYouWantToSee[i].getAuthorAvg() + theOnesYouWantToSee[i].getAuthorConfidence();	
			presentValues[i] = theOnesYouWantToSee[i].getToModifyValue();
			tempMinMax = theOnesYouWantToSee[i].getTrainMax();
			tempAuthorMinMax = authorMax[i];

			if(tempAuthorMinMax < presentValues[i])
				tempAuthorMinMax = presentValues[i];

			if(tempAuthorMinMax > tempMinMax)
				maximums[i] = tempAuthorMinMax;
			else
				maximums[i] = tempMinMax; 
			tempMinMax = theOnesYouWantToSee[i].getTrainMin();
			tempAuthorMinMax = authorMin[i];

			if(tempAuthorMinMax > presentValues[i])
				tempAuthorMinMax = presentValues[i];

			if(tempAuthorMinMax < tempMinMax)
				minimums[i] = tempAuthorMinMax;
			else
				minimums[i] = tempMinMax;
			//System.out.println(presentValues[i]);
			tempString = theOnesYouWantToSee[i].getStringInBraces();
			if(tempString == "")
				dashes = "";
			else
				dashes = "--";
			names[i] = theOnesYouWantToSee[i].getGenericName()+dashes+tempString;
		}

		Iterator<Cluster[]> outerLevel = everySingleCluster.iterator();
		clusterPanels = new JPanel[numFeatures];// everySingleCluster.size()
		nameLabels = new JLabel[numFeatures];
		finalPanels = new JPanel[numFeatures];
		i = 0;
		int[] initialLayoverVals = new int[numFeatures];
		String[] usedNames = new String[numFeatures];
		
		while(outerLevel.hasNext()) {
			nameLabels[i] = new JLabel(names[i]); // for if you want to edit the label in any way
			usedNames[i] = names[i];

			//if (authorMax[i] != 0 && presentValues[i] != 0 && authorMin[i] != 0) {
				//System.out.println(i + ", " + minimums[i] + ", " + maximums[i] + ", " + authorMin[i] + ", " + authorMax[i] + ", " + presentValues[i]);
				JPanel clusterPanel = new ClusterPainter(outerLevel.next(),i,minimums[i],maximums[i], authorMin[i],authorMax[i],presentValues[i]);
				clusterPanels[i] = clusterPanel;

				MigLayout layout = new MigLayout(
						"fill, wrap, ins 0",
						"fill, grow",
						"[20]0[grow, fill]");
				finalPanels[i] = new JPanel(layout);
				finalPanels[i].add(nameLabels[i], "grow");
				finalPanels[i].add(clusterPanels[i], "grow");
			//} else {
			//	outerLevel.next();
			//}

			initialLayoverVals[i] = 1;
			i++;
		}
	}

	public static void initializeClusterViewer(GUIMain main, boolean showMessage) {
		Logger.logln("Initializing ClusterViewer");
		int numPanels = clusterPanels.length;
		for (int i = 0; i < numPanels; i++) {
			//if (clusterPanels[i] == null) {
			//	continue;
			//}
			
			if (i == 0 || i % 2 == 0) {
				nameLabels[i].setBackground(Color.WHITE);
				clusterPanels[i].setBackground(Color.WHITE);
				finalPanels[i].setBorder(main.BANNER_BORDER);
			} else {
				nameLabels[i].setBackground(main.BANNER_BACKGROUND_COLOR);
				clusterPanels[i].setBackground(main.BANNER_BACKGROUND_COLOR);
				finalPanels[i].setBorder(main.BANNER_BORDER);
			}
			nameLabels[i].setHorizontalAlignment(SwingConstants.CENTER);
			nameLabels[i].setOpaque(true);
			clusterPanels[i].setPreferredSize(new Dimension(800,40));
			finalPanels[i].setPreferredSize(new Dimension(800,60));
			main.clustersWindow.clusterHolderPanel.add(finalPanels[i]);
		}

		setClusterGroup();

		intRepresentation = new int[lenCGR][clusterGroupRay[0].getGroupKey().length()];
		stringRepresentation = new String[1+lenCGR];
		stringRepresentation[0] = "Select Targets";
		for (int i = 0; i < lenCGR; i++) {
			intRepresentation[i] = clusterGroupRay[i].getGroupKey().toIntArray();
			stringRepresentation[i+1] = clusterGroupRay[i].getGroupKey().toString();
		}

		int[] theOne = intRepresentation[0];
		selectedClustersByFeature = theOne;
		lenJPanels = clusterPanels.length;
		for (int i = 0; i < lenJPanels; i++) {
			//if (clusterPanels[i] == null) {
			//	continue;
			//}
			clusterPanels[i].revalidate();
			clusterPanels[i].repaint();
		}
	}
	
	@SuppressWarnings("rawtypes")
	public static class alignListRenderer implements ListCellRenderer {

		int alignValue;
		protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

		public alignListRenderer(int value) {
			super();
			alignValue = value;
		}

		public Component getListCellRendererComponent(JList list, Object value, int index,
	      boolean isSelected, boolean cellHasFocus) {

		    JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index,
		        isSelected, cellHasFocus);

		    renderer.setHorizontalAlignment(alignValue);

		    return renderer;
	    }
	}
}