package edu.drexel.psal.anonymouth.engine;

import edu.drexel.psal.anonymouth.utils.SmartIntegerArray;

/**
 * holds a group of clusters pertaining to one or more documents, and facilitates operations on it.
 * 
 * @author Andrew W.E. McDonald
 *
 */
public class ClusterGroup implements Comparable<ClusterGroup>{
	
	@SuppressWarnings("unused")
	private final String NAME = "( "+this.getClass().getSimpleName()+" ) - ";
	
	private SmartIntegerArray groupKey; 
	private double preferenceValue;
	private double[] centroids;
	
	/**
	 * Constructor for ClusterGroup
	 * @param groupKey the key containing cluster preference numbers by feature for this group of clusters
	 * @param preferenceValue the calculated value that identifies this clusterGroup's desirability as being the user's set of targets
	 */
	public ClusterGroup(SmartIntegerArray groupKey, double preferenceValue, double[] centroids) {
		this.groupKey=groupKey;
		this.preferenceValue = preferenceValue;
		this.centroids = centroids;
	}
	
	/**
	 * returns a string representation of this object, in the form of: 'groupKey' - {'preferenceValue}
	 */
	public String toString() {
		String centroids = "[ ";
		for(int i = 0; i < this.centroids.length; i++)
			centroids += this.centroids[i]+", ";
		return groupKey.toString()+" - {"+preferenceValue+"} "+centroids;
	}
	
	/**
	 * method to allow sorting (greatest to least) based on preferenceValue (as determined by @ClusterAnalyzer, SmartIntegerArray_one.compareTo(SmartIntegerArray_two);
	 * @param obj a @SmartIntegerArray
	 * @return
	 * 	1 if *_one < *_two
	 * 	-1 if *_one > *_two
	 * 	0 if *_one == *_two 
	 */
	public int compareTo(ClusterGroup cGroup) {
		if (this.preferenceValue > cGroup.preferenceValue)
			return -1;
		else if (this.preferenceValue < cGroup.preferenceValue)
			return 1;
		else
			return 0;
	}
	
	/**
	 * returns the cluster group represented as a SmartIntegerArray object
	 * @return
	 */
	public SmartIntegerArray getGroupKey() {
		return groupKey;
	}
	
	/**
	 * returns the array of centroids for this cluster group
	 * @return
	 */
	public double[] getCentroids() {
		return centroids;
	}

}
