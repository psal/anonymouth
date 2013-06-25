package edu.drexel.psal.anonymouth.engine;

import java.util.ArrayList;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import edu.drexel.psal.anonymouth.utils.Pair;
import edu.drexel.psal.jstylo.generics.Logger;

/**
 * Extracts targets 
 * @author Andrew W.E. McDonald
 *
 */
public class TargetExtractor {
	
	private final String NAME = "( "+this.getClass().getSimpleName()+" ) - ";
	
	private static ArrayList<Integer> previousInitialization;
	private int numMeans;
	private int numAuthors;
	private int additionalPartitions = 0;
	private double min;
	private double max;
	private double spread;
	private int numPartitions; // same as number of clusters (1 partition == 1 cluster)
	private int originalNumMeans;
	private boolean isFinished;
	private double authorAvg;
	private double authorStdDev;
	private double authorMin;
	private double authorMax;
	private boolean targetSet=false;
	private double presentValue;
	private double targetDev;
	ArrayList<Cluster> thisFeaturesClusters; 
	private ArrayList<String> trainTitlesList;
	private Pair[] thePairs;
	private boolean maxCentroidsFound = false;
	private String featName;
	
	/**
	 * Constructor
	 * @param numAuthors the number of authors (not including the user). This defines the starting number of centroids
	 * @param attrib the Attribute to extract a target for
	 */
	public TargetExtractor(int numAuthors, Attribute attrib){//, boolean usePreviousInitialization){
		this.featName = attrib.getConcatGenNameAndStrInBraces();
		//Logger.logln(NAME+"In TargetExtractor extracting targets for "+featName);
		this.trainTitlesList = DocumentMagician.getTrainTitlesList();
		this.numAuthors = numAuthors;
		this.numMeans = numAuthors-2;// todo maybe remove this. (xxx xxx xxx try n-2, n-3, n-1, n, etc xxx xxx xxx)
		double[] thisFeature = attrib.getTrainVals();
		int lenThisFeature = thisFeature.length;
		int i=0;
		this.thePairs = new Pair[lenThisFeature];
		for(i=0;i<lenThisFeature;i++){
			thePairs[i] = new Pair(trainTitlesList.get(i),thisFeature[i]);
		}
		this.min = attrib.getTrainMin();
		this.max = attrib.getTrainMax();
		this.spread = max-min;
		isFinished = false;
		this.authorAvg = attrib.getAuthorAvg();
		this.authorStdDev = attrib.getAuthorStdDev();
		this.presentValue = attrib.getToModifyValue();
		
	}
	
	/**
	 * Empty constructor for testing purposes
	 */
	public TargetExtractor(){
		thisFeaturesClusters = new ArrayList<Cluster>();
	}
	
	/**
	 * Implementation of k-means++ initialization (seeding) algorithm for k-means
	 */
	public void kPlusPlusPrep(){//double[] thisFeature){ // parameter just for testing
		previousInitialization = new ArrayList<Integer>();
		thisFeaturesClusters.clear();
		int numFeatures = thePairs.length;
		MersenneTwisterFast mtfGen = new MersenneTwisterFast();
		int firstPick = mtfGen.nextInt(numFeatures);
		thisFeaturesClusters.add(0,new Cluster(thePairs[firstPick].value));
		previousInitialization.add(firstPick);
		int numClusters = thisFeaturesClusters.size();
		int i =0;
		int j = 0;
		int k = 0;
		double smallestDSquared = Integer.MAX_VALUE; // large initial value
		double tempSmallestDSquared;
		double currentValue;
		double currentCentroid;
		double[] dSquaredRay = new double[numFeatures];
		double dSquaredRaySum = 0;
		double[] probabilities = new double[numFeatures];
		double randomChoice;
		boolean notFound = true;
		boolean tooManyTries = false;
		Set<Double> skipSet = new HashSet<Double>();
		int numLoops = 0;
		
		for(i=0;i<numMeans-1;i++){
			for(j=0;j<numFeatures;j++){
				currentValue = thePairs[j].value;
				smallestDSquared = Integer.MAX_VALUE;
				for(k=0;k<numClusters;k++){
					currentCentroid = thisFeaturesClusters.get(k).getCentroid();
					tempSmallestDSquared = (currentValue-currentCentroid)*(currentValue-currentCentroid);
					if(tempSmallestDSquared < smallestDSquared)
						smallestDSquared = tempSmallestDSquared;
				}
				dSquaredRay[j]=smallestDSquared;
			}
			dSquaredRaySum = 0;
			for(k=0;k<numFeatures;k++)
				dSquaredRaySum += dSquaredRay[k];
			if(dSquaredRaySum== 0){// this will occur if we have multiple duplicate values. In that case, we can't choose more centroids because the remaining data points are equal.
				maxCentroidsFound = true; 
				numMeans = i+1; // need to add one because it starts counting from '0', and even if every document has the same value for this feature, there 
				// will still be one centroid chosen because we randomly select the first one before the loop (if ALL values are the same, this will break out of the loop at i==0, and we'll have a single centroid, so, we need to account for it in numMeans)
				break;
			}
			for(k=0;k<numFeatures;k++)
				probabilities[k]=dSquaredRay[k]/dSquaredRaySum;
			
			notFound = true;
			
			ArrayList<Double> badRandomsTesting = new ArrayList<Double>();
			double thisProb = 0;
			while(notFound == true){
				numLoops += 1;
				randomChoice = mtfGen.nextDouble(true,true);
				thisProb = 0;
				for(k=0;k<numFeatures;k++){
					thisProb = thisProb +probabilities[k];
					if((randomChoice <= thisProb) && (!skipSet.contains(thePairs[k].value))){
						thisFeaturesClusters.add(new Cluster(thePairs[k].value));
						skipSet.add(thePairs[k].value);
						previousInitialization.add(k);
						notFound = false;
						break;
					}
				}
				if(notFound == true){	
					if(skipSet.size() > 10000 || numLoops > 1000){
						Logger.logln(NAME+"kPlusPlusPrep reached 10k tries.");
						tooManyTries = true;
						break;
					}
					Set<Double> cSkipSet = new HashSet<Double>(skipSet);
					int preSize = cSkipSet.size();
					for(k=0;k<numFeatures;k++){
						cSkipSet.add(thePairs[k].value);
					}
					int postSize = cSkipSet.size();
					if(preSize == postSize){
						maxCentroidsFound = true;
						numMeans = thisFeaturesClusters.size();
					}
					badRandomsTesting.add(randomChoice);
				}
				if(maxCentroidsFound==true || tooManyTries == true)
					break;
			}
			if(maxCentroidsFound==true)
				break;
			if(tooManyTries == true){
				kPlusPlusPrep();
				break;
			}
			
		}
	}
	
	/**
	 * 
	 * Initializes the clustering algorithm by evenly spacing 'numMeans' centroids between the features [min,max],
	 * and assigns features to partitions based upon Euclidean distance from centroids (single dimension)
	 */
	public boolean initialize(){
		//Logger.logln(NAME+"Initializing Clustering, will call kPlusPlusPrep.");
		kPlusPlusPrep();
		int i;
		int j;
		double[] temp = new double[2];// temp[0] <=> parition number && temp[1] <=> difference value
		int partitionToGoTo;
		
		numPartitions = numMeans;
			
		// create list of all centroids
		double[] allCentroids = getAllCentroids();
	
		// Initialize cluster element sets based on distance from each centroid
		double[][] differences = new double[numMeans][thePairs.length];
		for(i=0;i<numMeans;i++){
			double  tempCentroid = allCentroids[i];
			for(j=0;j<thePairs.length;j++){
				//TODO: squared??
				differences[i][j] =Math.abs(thePairs[j].value-tempCentroid); 
			}
		}
		for(i=0;i<differences[0].length;i++){//differences array's columns (correspond to 'thisFeature' indices (feature events per document)
			j=0;
			temp[0] = j;
			temp[1] = differences[j][i];
			for(j=1;j<differences.length;j++){// differences array's rows (correspond to 'thisFeaturesClusters' cluster indices)
				if (temp[1]>differences[j][i]){
					temp[0] = j;
					temp[1] =differences[j][i];
				}
			}
			partitionToGoTo = (int)temp[0];
			thisFeaturesClusters.get(partitionToGoTo).addElement(thePairs[i]);
		}
		//Logger.logln(NAME+"Initial positions for elements found. Updating Centroids.");
		return updateCentroids();
	}
	
	
	/**
	 * Updates the centroids to be the average of the values contained within their respective partitions. 
	 */
	public boolean updateCentroids(){
		//Logger.logln(NAME+"Begin updating centroids.");
		// update centroids to be the averages of their respective element lists
		int i=0;
		int j = 0;
		for(i=0;i<numMeans;i++){
			double sum= 0;
			double avg = 0;
			int count = 0;
			Pair[] someElements = thisFeaturesClusters.get(i).getElements();
			int someElementsLen = someElements.length;
			for(j=0; j<someElementsLen;j++){
				double temp = someElements[j].value;
				sum+=temp;
				count += 1;
			}
			if (count == 0)
				avg = -1; // don't divide by zero. just set avg to -1 and deal with it later.
			else
				avg = sum/count;
			thisFeaturesClusters.get(i).updateCentroid(avg);
			
		}
		// Once all centroids have been updated, re-organize
		//Logger.logln(NAME+"Updating centroids complete, will reOrganize");
		return reOrganize();
	}
	
	public double[] getAllCentroids(){
		int i;
		int numClusters = thisFeaturesClusters.size();
		double[] allCentroids = new double[numClusters];
		for(i=0;i<numClusters;i++)
			allCentroids[i] = thisFeaturesClusters.get(i).getCentroid();
		return allCentroids;
	}
	
	
	/**
	 * Moves the features to their new nearest centroids
	 */
	public boolean reOrganize(){
		//Logger.logln(NAME+"Starting reOrganize");
		// need to go through all elements, extract data, and check distance agains new centroids
		// create list of all centroids
		int i;
		int j;
		int k;
		int m;
		double[] temp = new double[2];// index '0' holds the centroid number that corresponds to the difference value in index '1'
		int bestCentroid;
		boolean movedElement = false;
		//System.out.println("all centroids: "+getAllCentroids().toString());
		//TODO: maybe there is a better way to go about this than casting from Object to Double
		double[] allCentroids = getAllCentroids();
		double[] diffs = new double[allCentroids.length];
		Pair[] elementHolder;
		for(i=0;i<numMeans;i++){// for each cluster
			elementHolder = thisFeaturesClusters.get(i).getElements(); //get the element list, and change it to a Double[] (from ArrayList<Double>)
			for(j=0;j<elementHolder.length;j++){
				for(k=0;k<numMeans;k++){
					//TODO: squared??
					diffs[k] = Math.abs(elementHolder[j].value-(Double)allCentroids[k]);
				}
				temp[0]=0;
				temp[1]=diffs[0];
				for(m=1;m<diffs.length;m++){
					if(temp[1]>diffs[m]){
						temp[0]=m;
						temp[1]=diffs[m];
					}
				}
				bestCentroid = (int)temp[0];
				if(!  (bestCentroid  ==   i)   ){// if a more fitting centroid was found for the element in question...
					thisFeaturesClusters.get(i).removeElement((Pair)elementHolder[j]);
					thisFeaturesClusters.get(bestCentroid).addElement((Pair)elementHolder[j]);
					movedElement = true;
				}
				
			}
		}
		boolean noProblems = true;
		if(movedElement == false ){
			//Logger.logln(NAME+"Elements stopped moving - algorithm converged.");
			int numClusters = thisFeaturesClusters.size();
			if(numClusters < 2 && maxCentroidsFound == false){
				additionalPartitions++;
				numMeans = originalNumMeans+additionalPartitions;
				noProblems = false;
				
			}
			else{
				for(i=0;i<numClusters;i++){
					if(thisFeaturesClusters.get(i).getElements().length < 3 && maxCentroidsFound == false){
						numMeans--;
						noProblems = false;
						break;
					}
				}
			}
			if(noProblems == true){
				Logger.logln(NAME+"All is well, clustering complete.");
				isFinished=true;
			}
			else{
				noProblems = true;
				thisFeaturesClusters.clear();
				return true;
			}
		}
		else{
			//Logger.logln(NAME+"Updating Centroids... something moved");
			return updateCentroids();
		}
		return false;
		
	}


	
	/**
	 * returns the average absolute deviation of the elements in the target cluster from the centroid
	 * @return
	 */
	public double getTargetAvgAbsDev(){
		if(targetSet==true)
			return targetDev;
		else
			return -1;
	}
	
	
	
	/**
	 * Method that runs the modified k-means clustering algorithm, initialized via the k-means++ algorithm
	 */
	public void aMeansCluster(){ // a-means-cluster vs k-means-cluster
		Logger.logln(NAME+"Entered aMeansCluster");
		thisFeaturesClusters = new ArrayList<Cluster>(numPartitions);
		boolean mustRestart = true;
		while (mustRestart)
			mustRestart = initialize();	
		double avgAbsDev;
		Cluster thisOne;
		int numRemoved = 0;
		int i;
		for(i = 0; i< numMeans; i++){
			thisOne = thisFeaturesClusters.get(i);
			if ((thisOne.getElements().length == 0) && (thisOne.getCentroid() == -1)){ // this may be redundant.. we can probably just pick one (I'd keep the first one)
				thisFeaturesClusters.remove(thisOne); // no reason keeping empty clusters around
				numMeans--; // if we remove a cluster, we need to reduce the number of means, AND we need to decrement 'i' so that we don't miss the cluster that moves forward into the place of the cluster that was just deleted.
				i--;
			}
		}
		ArrayList<String> holderForLogger = new ArrayList<String>(10);
		Iterator<Cluster> clusterIter = thisFeaturesClusters.iterator();
		int clusterNumber = 0;
		Logger.logln(NAME+"Clusters for:  "+featName);
		while(clusterIter.hasNext()){
			thisOne = clusterIter.next();
			holderForLogger.clear();
			Pair[] somePairs = thisOne.getElements();
			int numSomePairs = somePairs.length;
			for(i=0;i<numSomePairs;i++){
				holderForLogger.add(somePairs[i].toString());
			}
			Logger.logln(NAME+"Cluster "+clusterNumber+" has its centroid at "+thisOne.getCentroid()+" and has "+thisOne.getElements().length+" elements. They are: "+holderForLogger.toString());
			clusterNumber+=1;
		}
		Logger.logln(NAME+featName+" has: "+thisFeaturesClusters.size()+" clusters... leaving aMeansCluster");
	}	
	
	/**
	 * Orders the clusters with respect to 'preference'. 
	 * 
	 * preference = (number of elements in cluster)*(positive distance between cluster centroid and user's average)
	 * 
	 * @return
	 */
	public Cluster[] getPreferredOrdering(){
		Logger.logln(NAME+"Getting preferred ordering for clusters");
		int i=0;
		int sizeSum =0;
		double sizeAvg = 0;
		double	tempClustCent;
		double tempClustDev;
		double clustMin;
		double clustMax;
		int numClusters = thisFeaturesClusters.size();
		int[] sizes = new int[numClusters];
		double[] dists = new double[numClusters];
		Double[][] preferences = new Double[numClusters][2]; // highest number => most ideal cluster 
		double distSum = 0 ;
		double distAvg = 0;
		
		// collect sizes of all clusters
		for(i=0;i<numClusters;i++){
			sizes[i] = thisFeaturesClusters.get(i).getElements().length;
			sizeSum += sizes[i]; 
		}
		
		sizeAvg = (double)sizeSum/numClusters;
		
		for(i=0; i<numClusters;i++){

			Cluster tempCluster = thisFeaturesClusters.get(i); 
			
			tempClustCent = tempCluster.getCentroid();
			if(tempClustCent< authorAvg)
				dists[i] = authorAvg - tempClustCent;
			else if (tempClustCent > authorMax)
				dists[i] = tempClustCent - authorAvg;
			else
				dists[i] = 0;
			
			distSum += dists[i];
		}
		
		distAvg = distSum/numClusters;
		
		for(i = 0; i < numClusters; i++){
			preferences[i][0] =(Double)(double)i;
			preferences[i][1] = (dists[i])*(sizes[i]/sizeAvg); //  ( distance)*(cluster size/ average cluster size)
			
		}
		
		Arrays.sort(preferences, new Comparator<Double[]>(){
			public int compare(Double one[], Double two[]){
				return one[1].compareTo(two[1]);
			}
		});	
		
		Cluster[] targets = new Cluster[numClusters]; // can't be more than this. 
		i= 0;
		for(i=0;i<numClusters;i++){
			targets[i]= thisFeaturesClusters.get(preferences[i][0].intValue());
		}	
		Logger.logln(NAME+"finished ordering clusters");
		return targets;
	}
		
}

