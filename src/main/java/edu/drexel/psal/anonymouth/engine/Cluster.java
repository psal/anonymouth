package edu.drexel.psal.anonymouth.engine;

import java.util.ArrayList;

import edu.drexel.psal.anonymouth.utils.Pair;

/**
 * Cluster class holds each cluster being utilized by 'TargetExtractor'. 
 * @author Andrew W.E. McDonald
 *
 */
public class Cluster {
	
	private final String NAME = "( "+this.getClass().getName()+" ) - ";
	
	private double centroid;
	private ArrayList<Pair> elements;
	private int clusterNumber = -2;
	
	public Cluster(double centroid){
		this.centroid = centroid;
		this.elements = new ArrayList<Pair>();
	}
	
	/**
	 * adds a "Pair" object to element set
	 * @param element - 'Pair' object to add to Cluster's element set
	 */
	public void addElement(Pair element){
		elements.add(element);
		
	}
	
	/**
	 * Set the cluster number with respect to the array of ordered clusters 
	 * @param clusterNumber
	 */
	public void setClusterNumber(int clusterNumber){
		this.clusterNumber = clusterNumber;
	}
	
	/**
	 * returns the set cluster number
	 * @return
	 */
	public int getClusterNumber(){
		return clusterNumber;
	}
	
	/**
	 * Moves the current value of the centroid to the new one, as specified by 'newCentroid'
	 * @param newCentroid - 'double' (primitive type) target value for updated centroid
	 */
	public void updateCentroid(double newCentroid){
		centroid = newCentroid;
	}
	
	/**
	 * Removes element from ArraList 'elements'
	 * @param element - 'Pair' value that has been previously added
	 * @return
	 * 	the input parameter if successfully removed, and 'null' otherwise. 
	 */
	public Pair removeElement(Pair element){
		if(elements.remove(element) == true)
			return element;
		else
			return null;
	}
	
	/**
	 * Returns the list of elements contained by the cluster object
	 * @return
	 * 	ArrayList of Pair values
	 */
	public Pair[] getElements(){
		Pair[] pairRay = new Pair[elements.size()];
		int i = 0;
		int pairRayLen = pairRay.length;
		for(i=0;i<pairRayLen;i++)
			pairRay[i] = elements.get(i);
		return pairRay;
	}
	
	/**
	 * Returns the current primitive type double value of the centroid
	 * @return
	 * 	double value of centroid
	 */
	public double getCentroid(){
		return centroid;
	}
	
	/**
	 * Calculates the average absolute deviation of the element's values from the centroid (average). This is a more robust calculation than standard deviation,
	 * and produces a more reliable statistic when the distribution is not expected to be normal. 
	 * @return
	 * 	double type, average absolute deviation
	 */
	public double avgAbsDev(){
		int i;
		double sum=0;
		double avgAbsDev;
		for(i=0;i<elements.size();i++)
			sum+=Math.abs(elements.get(i).value-centroid);
		avgAbsDev=(sum/elements.size());
		return avgAbsDev;
	}
	
	/**
	 * returns the maximum value stored in the cluster
	 * @return
	 */
	public double getMaxValue(){
		double max = -100000;
		double tempMax;
		int i =0;
		int numElems = elements.size();
		for(i=0; i<numElems;i++){
			tempMax = elements.get(i).value;
			if(tempMax > max)
				max = tempMax;
		}
		return max;
	}
	
	/**
	 * returns the minimum value stored in the cluster
	 * @return
	 */
	public double getMinValue(){
		double min = 1000000;
		double tempMin;
		int i = 0;
		int numElems = elements.size();
		for(i=0;i<numElems;i++){
			tempMin = elements.get(i).value;
			if(tempMin < min)
				min = tempMin;
		}
		return min;
	}
	
	public String toString(){
		return "[ centroid: "+centroid+", cluster number: "+clusterNumber+", number of documents: "+elements.size()+" ]";
		
	}

}
