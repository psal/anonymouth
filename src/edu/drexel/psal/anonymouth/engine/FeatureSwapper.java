package edu.drexel.psal.anonymouth.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import edu.drexel.psal.ANONConstants;
import edu.drexel.psal.anonymouth.helpers.ErrorHandler;
import edu.drexel.psal.jstylo.analyzers.WekaAnalyzer;
import edu.drexel.psal.jstylo.generics.Logger;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Swaps original feature values out of the original feature vector of the documen to anonymize, 
 * and replaces them with potential target values of the feature in question. 
 * All of these swaps are tested against the trained classifier, 
 * and the ClusterGroup with target values that confused the classifier the most is returned.
 * 
 * "Confused the classifier the most" means that the "other" authors (not the user) had ownership estimates
 * with the lowest variance with respect to a single test instance, and that the user had a (low, less than random chance..?)
 * @author Andrew W.E. McDonald
 *
 */
public class FeatureSwapper {
	
	private final String NAME = "( "+this.getClass().getName()+" ) - ";
	WekaAnalyzer waz;
	Instances toAnonymize;
	ClusterGroup[] clusterGroups;
	WekaResults[] wekaResultsArray;
	ArrayList<String> toAnonymizeTitlesList;
	Set<String> trainSetAuthors;
	double bestCaseClassification = 1; // set it to worst case, and we'll update as it gets better.
	
	public FeatureSwapper(ClusterGroup[] clusterGroups, DocumentMagician magician){
		toAnonymize = magician.getToModifyDat();
		toAnonymizeTitlesList = magician.getToModifyTitlesList();
		trainSetAuthors = magician.getTrainSetAuthors();
		waz = new WekaAnalyzer(ANONConstants.PATH_TO_CLASSIFIER);
		this.clusterGroups = clusterGroups;
		if (clusterGroups == null)
			Logger.logln(NAME+"Damn.");

	}
	
	/**
	 * Will test the topN_ClusterGroupsToTest by swapping the centroid (target) values into the document to anonymize's
	 * Instance object, and testing the modified Instances against the original classifier. 
	 * 
	 * It will return the best ClusterGroup (which is the one that would provide the most anonymity).
	 * 
	 * If "topN_ClusterGroupsToTest" is less than 1, or greater than the number of ClusterGroups available,
	 * all ClusterGroups will be tested.
	 * @param topN_ClusterGroupsToTest
	 * @return
	 */
	public ClusterGroup getBestClusterGroup(int topN_ClusterGroupsToTest){
		int numClusterGroups;
		if (topN_ClusterGroupsToTest <= 0 || topN_ClusterGroupsToTest > clusterGroups.length)
			numClusterGroups = clusterGroups.length;
		else
			numClusterGroups = topN_ClusterGroupsToTest;
		Instance alteredInstance = null;
		double[] tempCG_centroids;
		Attribute tempAttrib;
		Iterator<String> keyIter;
		int indexInInstance;
		int numTopFeatures = DataAnalyzer.topAttributes.length;
		wekaResultsArray = new WekaResults[numClusterGroups];
		int i,j;
		for(i = 0; i < numClusterGroups; i++){ // for each cluster group
			Instances hopefullyAnonymizedInstances = new Instances(toAnonymize);
			alteredInstance = hopefullyAnonymizedInstances.instance(0); // hardcoding in '0' because we only plan to have a single document (thus a single instance)
			hopefullyAnonymizedInstances.delete(0);// deleting the original from the copy -- it will be replaced by an Instance with substituted values
			tempCG_centroids = clusterGroups[i].getCentroids(); // these are the "target values" that will be substituted in
			for( j = 0; j < numTopFeatures; j++){
				// the attributes (features) in topAttributes are in the same order as in each ClusterGroup (clusterGroups[i]), 
				// AND in the same order as the centroids in the array returned by "clusterGroups[i].getCentroids()" above.
				tempAttrib = DataAnalyzer.topAttributes[j];
				indexInInstance = tempAttrib.getFeaturesOriginalInstancesIndexNumber();
				alteredInstance.setValue(indexInInstance, tempCG_centroids[j]);
			}
			hopefullyAnonymizedInstances.add(alteredInstance);
			Map<String,Map<String,Double>> wekaResultMap = waz.classifyWithPretrainedClassifier(hopefullyAnonymizedInstances, toAnonymizeTitlesList, trainSetAuthors);
			keyIter = (wekaResultMap.keySet()).iterator();
			System.out.println(wekaResultMap.keySet().toString()+" -- current cluster group num: "+i);
			if (keyIter.hasNext()){
				wekaResultsArray[i] = new WekaResults(wekaResultMap.get(keyIter.next()),i); // there should never be more that one key in this map. We only test one document.
			}
			else
				ErrorHandler.fatalProcessingError(null);
			
		}
				
		Arrays.sort(wekaResultsArray);
		
		/*
		 * Error check to make sure that Weka isn't being misused: 
		 * if ALL of the representativeValue's are equal, then something is wrong.. because that means that all of the classifications were identical.
		 */
		boolean allEqual = true;
		double lastValue = wekaResultsArray[0].representativeValue;
		int numVals = wekaResultsArray.length;
		for (i = 1; i < numVals; i++){
			if (lastValue != wekaResultsArray[i].representativeValue){
				allEqual = false;
				break;
			}
			//System.out.println("representative: "+wekaResultsArray[i].representativeValue+" ==> "+clusterGroups[wekaResultsArray[i].respectiveIndexInClusterGroupArray].toString());
		}
		Logger.logln("If all features are moved to their target values, the following classification will result: "+clusterGroups[wekaResultsArray[0].respectiveIndexInClusterGroupArray].toString());
		if (allEqual && i != 1)
			Logger.logln("Oops! Weka must have been called incorrectly. All ClusterGroup representativeValue's are the same!\nThis is known to happen when the SMO is run without the '-M' flag. Howev");
		return clusterGroups[wekaResultsArray[0].respectiveIndexInClusterGroupArray];
	}
	
}


/**
 * Takes care of parsing the results from Weka, and calculates a representative value that indicates how favorable a given result is,
 * with respect to the other results. Implements Comparable to allow for easy sorting via Arrays.sort(array).
 * @author Andrew W.E. McDonald
 *
 */
class WekaResults implements Comparable<WekaResults>{
	
	ResultPair[] results;
	ResultPair theUser;
	int numOtherAuthors;
	int respectiveIndexInClusterGroupArray;
	double representativeValue;
	
	public WekaResults(Map<String,Double> resultsMap, int indexInClusterGroupArray){
		respectiveIndexInClusterGroupArray = indexInClusterGroupArray;
		Set<String> keySet = resultsMap.keySet();
		numOtherAuthors = keySet.size() - 1;
		results = new ResultPair[numOtherAuthors];
		Iterator<String> strIter = keySet.iterator();
		String name;
		double d;
		int count = 0;
		while(strIter.hasNext()){
			name = strIter.next();
			d = resultsMap.get(name);
			if (name.equals(ANONConstants.DUMMY_NAME)){
				theUser = new ResultPair(name, d);
				continue;
			}
			results[count] = new ResultPair(name, d);
			count++;
		}
		representativeValue = getAverageOfUserResultAndCalculatedVariance();
		
	}
	
	/**
	 * Calculates the variance among ONLY the "other" authors (not the actual author).
	 * (we want to pick the result with the lowest variance (meaning it was hard to tell who wrote the document)
	 */
	public double getVariance(){
		int i;
		double total = 0;
		double average = 0;
		for(i = 0; i < numOtherAuthors; i++)
			total += results[i].value;
		average = total/numOtherAuthors;
		total = 0;
		for(i = 0; i < numOtherAuthors; i++)
			total += Math.pow((results[i].value - average),2); // sum the squares of each element minus the average.
		return total/numOtherAuthors; // the variance. 
	}
	
	
	/**
	 * This calculates the average between the variance of the other author's results (the degree of certainty that each other author wrote the document in question),
	 * and the degree of certainty that the user (who did write the document) wrote the document in question. 
	 * This is done because it seemed to be the best way to factor in a low variance between other authors (meaning it's hard to tell if any of the other authors wrote the document),
	 * and the fact that we also want it to look as different from the author's writing as possible. 
	 * 
	 * Taking the average of these two numbers means that the ClusterGroup that we select will have a value as close to zero as possible returned from this function.
	 * @return
	 */
	public double getAverageOfUserResultAndCalculatedVariance(){
		double variance = getVariance();
		double total = theUser.value + variance;
		return total/2;
	}

	@Override
	public int compareTo(WekaResults wr) {
		if(this.representativeValue < wr.representativeValue)
			return -1;
		else if(this.representativeValue > wr.representativeValue)
			return 1;
		else
			return 0;
	}

}


/**
 * Small wrapper for the name and degree of certaintly returned by Weka, as a result of the classification. 
 * @author Andrew W.E. McDonald
 *
 */
class ResultPair{
	
	String name;
	double value;
	
	public ResultPair(String name, double value){
		this.name = name;
		this.value = value;
	}
	
}
