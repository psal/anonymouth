package edu.drexel.psal.anonymouth.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import edu.drexel.psal.anonymouth.gooie.DriverClustersWindow;
import edu.drexel.psal.anonymouth.gooie.ThePresident;
import edu.drexel.psal.anonymouth.utils.Pair;
import edu.drexel.psal.anonymouth.utils.SmartIntegerArray;
import edu.drexel.psal.jstylo.generics.Logger;

/**
 * @author Andrew W.E. McDonald
 *
 */
public class ClusterAnalyzer {
	
	private final String NAME = "( "+this.getClass().getName()+" ) - ";

	private HashMap<SmartIntegerArray,Integer> commonClusterSetMap;
	ArrayList<String> theFeatures;
	ArrayList<String> theDocs;
	int numDocs;
	int numFeatures;
	int numClusters;
	ArrayList<Integer> cols = new ArrayList<Integer>(100);
	Cluster[][] clustersByDoc; 
	private static ClusterGroup[] clusterGroupArray;
	
	/**
	 * Constructor for ClusterAnalyzer
	 */
	public ClusterAnalyzer(ArrayList<String> featuresToUse,int maxClusters){
		
		Logger.logln(NAME+"Start construction of ClusterAnalyzer");
		theDocs = DocumentMagician.getTrainTitlesList();

		theFeatures = featuresToUse;
		//System.out.println("DEBUGGING: numDocs = " + theDocs.size());
		numDocs = theDocs.size();

		int num_docs_not_processed_by_jstylo =  numDocs % ThePresident.NUM_TAGGING_THREADS; // XXX XXX XXX XXX XXX WE ONLY NEED THIS UNTIL JSTYLO FIXES ITS THREAD DIVISION ISSUE
		numDocs -= num_docs_not_processed_by_jstylo; // XXX XXX XXX XXX XXX XXX XXX SEE COMMENT ABOVE

		numFeatures = featuresToUse.size();
		clustersByDoc = new Cluster[numDocs][numFeatures];
		int i,j;
		
		for (i=0; i< numFeatures;i++) {
			for (j=0; j< numDocs; j++) {
				clustersByDoc[j][i] = null;
			}
		}
	}
	
	/**
	 *	 
	 * 
	 * The clusters in the input Attribute are placed into their respective places in a 3d array based on feature, document, and Cluster number.
	 * @param attrib
	 * @return
	 * 	true if nothing went wrong
	 */
	public boolean addFeature(Attribute attrib) {
		int row = theFeatures.indexOf(attrib.getConcatGenNameAndStrInBraces());
//		System.out.printf("adding feature number %d: %s\n", row, attrib.getConcatGenNameAndStrInBraces());
		Cluster[] orderedClusters = attrib.getOrderedClusters();
		int lenClusterRay = orderedClusters.length;
		int i =0;
		int j = 0;
		int lenPairRay;
		int clusterNum;
		int col;
		
		for (i = 0; i < lenClusterRay; i++) {
			Pair[] pairRay = orderedClusters[i].getElements();
			clusterNum = i;
			lenPairRay = pairRay.length;

			for (j = 0; j < lenPairRay; j++) {
				col = theDocs.indexOf(pairRay[j].doc);
				if (cols.indexOf((Integer)col) == -1)
						cols.add((Integer)col);
				// the ordered clusters are ordered such that cluster '0' is at the beginning of the array of Clusters
				// we add one so that ... 
				orderedClusters[i].setClusterNumber(clusterNum + 1);
				clustersByDoc[col][row] = orderedClusters[i]; 
			}
			// So, now clustersByDoc is an array with each row containing all cluster numbers of all features in a given document. 
			// Each column corresponds to a certain feature. 
			
		}
		return true;
	}
	
	/**
	 * Runs analysis of the ClusterGroups, and orders them such that the ClusterGroup(s) most likely to be make the user anonymous, are at the top of
	 * the array returned by {@link #getClusterGroupArray()}
	 */
	public void analyzeNow(){ 
		Logger.logln(NAME+"Begin analysis of clusters in analyzeNow in ClusterAnalyzer");
		int i,j,k;
		commonClusterSetMap = new HashMap<SmartIntegerArray,Integer>(theDocs.size()); // worst case, no two documents fall in same set of clusters
		i=0;
		j=0;
		k=0;

		double[][] centroidsByDoc = new double[numDocs][];
		
		for (i = 0; i < numDocs; i++) {
			int numClusters = clustersByDoc[i].length;
			int[] clusterNumsByRow = new int[numClusters];
			double[] centroids = new double[numClusters];

			for (j = 0; j < numClusters; j ++) {
				//System.out.println("document "+i+", cluster number "+j+" => "+clustersByDoc[i][j]);
				clusterNumsByRow[j] = clustersByDoc[i][j].getClusterNumber();
				centroids[j] = clustersByDoc[i][j].getCentroid();
			}
			
			centroidsByDoc[i] = centroids;
			SmartIntegerArray tempKey = new SmartIntegerArray(clusterNumsByRow);
			
			if(commonClusterSetMap.containsKey(tempKey) == true)
				commonClusterSetMap.put(tempKey,commonClusterSetMap.get(tempKey)+1);
			else
				commonClusterSetMap.put(tempKey,1);
		}
		
		Set<SmartIntegerArray> clusterSetMapKeys = commonClusterSetMap.keySet();
		int numKeys = clusterSetMapKeys.size();
		SmartIntegerArray tempKey;
		Iterator<SmartIntegerArray> csmkIter = clusterSetMapKeys.iterator();
		i =0;
		j = 0; 
		int lenKey;
		int[] clusterGroupFreq = new int[numKeys];
		ClusterGroup[] clusterGroupArray = new ClusterGroup[numKeys];
		double tempSum = 0;
		
		while (csmkIter.hasNext()) {
			tempSum = 0;
			tempKey = csmkIter.next();
			clusterGroupFreq[j] = commonClusterSetMap.get(tempKey);
			lenKey = tempKey.length();
			int[] keyRay = tempKey.toIntArray();
			for(i=0; i< lenKey; i++){
				// this allows feature importance (as determined by information gain),  and cluster preference to influence the ordering of the cluster groups.
				tempSum += (keyRay[i]*((lenKey+1) - i))/lenKey;
			}	
			clusterGroupArray[j] = new ClusterGroup(tempKey,tempSum, centroidsByDoc[j]);
			j++;
		}
		
		Arrays.sort(clusterGroupArray);
		DriverClustersWindow.clusterGroupReady = true;
		this.clusterGroupArray = clusterGroupArray;

		Logger.logln(NAME+"ClusterAnalyzer analysis complete");
	}
	
	/**
	 * returns an array of ClusterGroups. Should only be called after {@link #analyzeNow()}
	 * @return
	 */
	public static ClusterGroup[] getClusterGroupArray(){
		return clusterGroupArray;
	}
}
