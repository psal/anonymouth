package edu.drexel.psal.anonymouth.engine;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.drexel.psal.jstylo.generics.Logger;

/**
 * The Attribute class holds all information for each of the top 'n' features as determined by the result of the information gain calculation.
 * @author Andrew W.E. McDonald
 *
 */
public class Attribute {
	
	private final String NAME = "( "+this.getClass().getSimpleName()+" ) - ";
	private int indexNumberInInstancesObject;
	private int featuresOriginalInstancesIndexNumber;
	private FeatureList genericName;
	private String concatGenNameAndStrInBraces;
	private String fullName;
	private double infoGain;
	private double[] clusters;
	private double targetValue;
	private int requiredDirectionOfChange; // 0 for no change, -1 to decrease, 1 to increase;
	private double targetCentroid;
	private double targetAvgAbsDev;
	private boolean calcHist;
	private boolean partOfSpeech;
	
	/**
	 * this is only set IF calcHist == true. In that case, the actual 'feature' is the string in the braces that replaces
	 * the dash: "{-}" => "{'string'}"
	 */
	private String stringInBraces = "";
	private double authorAvg;
	private double authorStdDev;
	private double[] trainVals;
	private double trainMin;
	private double trainMax;
	private double[] authorVals;
	private double toModifyValue;
	private String trimmedAttrib;
	private double authorConfidence;
	//private ArrayList<Cluster> orderedClusters;
	private Cluster[] orderedClusters;
	private double targetClusterMin;
	private double targetClusterMax;
	private boolean directionSet = false;
	private double baselinePercentChangeNeeded = 0; // only for this specific feature
	private boolean haveSetBaselinePercentChangeNeeded = false;
	
	/**
	 * Constructor for Attribute class.
	 * @param indexNumberInAttributeArray the attributes index number (this may not be used anymore, though it is still being set)
	 * @param fullName the full attribute name taken directly from the Instances object (@attribute...)
	 * @param calcHist boolean, true if histogram was calculated (in full name, {-} will be replaced by a string if this is true / should be set as true)
	 */
	public Attribute(int indexNumberInInstancesObject, String fullName, String stringInBraces, boolean calcHist){
		this.indexNumberInInstancesObject = indexNumberInInstancesObject;
		this.fullName = fullName;
		this.stringInBraces = stringInBraces;
		this.calcHist = calcHist;
		
		if (fullName.contains("POS"))
			partOfSpeech = true;
		else
			partOfSpeech = false;

		Logger.logln(NAME+"Attribute created for feature: "+fullName);
		setGenericName();
	}
	
	public void setFeaturesOriginalInstancesIndexNumber(int featuresOriginalInstancesIndexNumber){
		this.featuresOriginalInstancesIndexNumber = featuresOriginalInstancesIndexNumber;
	}
	
	/**
	 * Strips the fullName to an enumerated generic name. This name is used to call suggestors and calculators by class name.
	 */
	public void setGenericName(){
		// manipulate fullName to match the feature's value in it's FeatureList representation. calcHist is a big factor in how this is done. 
		trimmedAttrib = fullName.substring(fullName.indexOf("'")+1,fullName.indexOf("{"));// saves ONLY the actual feature name
		String tempFeatName = trimmedAttrib;	
		try{
			Pattern findDigit = Pattern.compile("\\d");// find a digit (shoudnt be in the genericName);
			Matcher m = findDigit.matcher(fullName);
			m.find();
			tempFeatName= fullName.substring(fullName.indexOf("'")+1,m.start()-1);
			System.out.println(fullName.substring(fullName.indexOf("'")+1,m.start()-1));
		}
		catch (Exception e){
		}
		
		
		Logger.logln(NAME+"Generic name: "+tempFeatName.replace("-","_").toUpperCase());
		genericName = FeatureList.valueOf(tempFeatName.replace("-","_").toUpperCase());
		
	}
	
		
	/**
	 * returns the index number of the feature represented by this attribute in the original Instance from the documentToAnonymize's features
	 * @return
	 * 	the index number
	 */
	public int getFeaturesOriginalInstancesIndexNumber(){
		return featuresOriginalInstancesIndexNumber;
	}
	
	
	/**
	 * returns the index number of this attribute in the Attribute array it is being held in
	 * @return
	 * 	the index number
	 */
	public int getIndexNumberInInstancesObject(){
		return indexNumberInInstancesObject;
	}
	
	/**
	 * returns the enumerated generic feature name
	 * @return
	 *  generic feature name
	 */
	public FeatureList getGenericName(){
		return genericName;
	}
	
	/**
	 * Primarily used for @ClusterAnalyzer, this returns the concatenation of the generic name as a String with the string in braces. If there is no string in braces,
	 * the generic name is simply returned as a String.
	 * @return
	 */
	public String getConcatGenNameAndStrInBraces(){
		concatGenNameAndStrInBraces = genericName.toString()+stringInBraces;
		return concatGenNameAndStrInBraces;
	}
	
	/**
	 * sets the information gain for this attribute; relative to the rest of the attributes in the instances object.
	 * @param infoGain
	 */
	public void setInfoGain(double infoGain){
		this.infoGain = infoGain;
	}
	
	/**
	 * returns the info gain for this attribute
	 * @return
	 * 	info gain
	 */
	public double getInfoGain(){
		return infoGain;
	}
	
	/**
	 * Sets the clusters created with 'TargetExtractor' using all instances of this attribute
	 * @param clusters
	 */
	public void setClusters(double[] clusters){
		this.clusters = clusters;
	}
	
	/**
	 * returns the clusters of this attributes instances/features
	 * @return
	 * 	clusters
	 */
	public double[] getClusters(){
		return clusters;
	}
	
	/**
	 * Sets this attributes (features) target value
	 * @param targetValue
	 */
	public void setTargetValue(double targetValue){
		if(!directionSet){
			this.targetValue = targetValue;
			if(this.targetValue > toModifyValue)
				requiredDirectionOfChange = 1;
			else if (this.targetValue == toModifyValue){
				requiredDirectionOfChange = 0;
			}
			else
				requiredDirectionOfChange = -1;
			directionSet = true;
		}
		
	}
	
	/**
	 * sets the Cluster Array of ordered clusters for the feature defined by this Attribute
	 * @param orderedClusters
	 */
	public void setOrderedClusters(Cluster[] orderedClusters){
		this.orderedClusters = orderedClusters;
	}
	
	
	/**
	 * returns Cluster[] of ordered Clusters
	 * @return
	 */
	public Cluster[] getOrderedClusters(){
		return orderedClusters;
	}
	
	/**
	 * Sets this attribute's target centroid (median value)
	 * @param targetCentroid
	 */
	public void setTargetCentroid(double targetCentroid){
		this.targetCentroid = targetCentroid;
	}
	
	/**
	 * Sets this attribute's target centroid's elements' average absolute deviation from the targetCentroid
	 * @param targetAvgAbsDev
	 */
	public void setTargetAvgAbsDev(double targetAvgAbsDev){
		this.targetAvgAbsDev = targetAvgAbsDev;
	}
	
	/**
	 * returns this attribute's target centroid's elements' average absolute deviation from the targetCentroid
	 * @return
	 */
	public double getTargetAvgAbsDev(){
		return targetAvgAbsDev;
	}
	
	/**
	 * returns this attribute's target centroid
	 * @return
	 */
	public double getTargetCentroid(){
		return targetCentroid;
	}
	
	/**
	 * returns the target value
	 * @return
	 * 	target value
	 */
	public double getTargetValue(){
		return targetValue;
	}
	
	
	/**
	 * sets average of user's sample documents for this attribute
	 * @param authorAvg
	 */
	public void setAuthorAvg(double authorAvg){
		this.authorAvg = authorAvg;
	}
	
	/**
	 * returns average of user's sample documents
	 * @return
	 */
	public double getAuthorAvg(){
		return authorAvg;
	}
	
	/**
	 * sets standard deviation from the mean of user's sample documents
	 * @param authorStdDev
	 */
	public void setAuthorStdDev(double authorStdDev){
		this.authorStdDev = authorStdDev;
	}
	
	/**
	 * returns standard deviation from the mean of user's sample documents
	 * @return
	 */
	public double getAuthorStdDev(){
		return authorStdDev;
	}

	/**
	 * Sets the values of all sample (not including user's) documents for this attribute
	 * @param trainVals
	 */
	public void setTrainVals(double[] trainVals){
		//System.out.println("length of trainVals is: "+trainVals.length);
		this.trainVals = trainVals;
	}
	
	/**
	 * Sets the minimum value of the "other" sample documents
	 * @param trainMin
	 */
	public void setTrainMin(double trainMin){
		this.trainMin = trainMin;
	}
	
	/**
	 * Sets the maximum value of the "other" sample documents
	 * @param trainMax
	 */
	public void setTrainMax(double trainMax){
		this.trainMax = trainMax;
	}
	
	/**
	 * returns the minimum value for this attribute across "other" sample documents
	 * @return
	 */
	public double getTrainMin(){
		return trainMin;
	}
	
	/**
	 * returns the maximum value for this attribute across "other" sample documents
	 * @return
	 */
	public double getTrainMax(){
		return trainMax;
	}
	
	/**
	 * Sets the values for the user's sample documents for this attribute/feature
	 * @param authorVals
	 */
	public void setAuthorVals(double[] authorVals){
		this.authorVals = authorVals;
	}
	
	/**
	 * Sets the "present value", or the value of the document to modify for this attribute
	 * @param toModifyValue
	 */
	public void setToModifyValue(double toModifyValue){
		this.toModifyValue = toModifyValue;
	}
	
	/**
	 * returns the values for all "other" sample documents for this attribute
	 * @return
	 */
	public double[] getTrainVals(){
		return trainVals;
	}
	
	/**
	 * returns the user's sample document's values for this attribute
	 * @return
	 */
	public double[] getAuthorVals(){
		return authorVals;
	}
	
	/**
	 * returns the 'present value', or value of the document to modify for this attribute
	 * @return
	 */
	public double getToModifyValue(){
		return toModifyValue;
	}
	
	/**
	 * returns true if the feature driver for this attribute returns true for 'isCalcHist"
	 * @return
	 * 	calcHist
	 */
	public boolean getCalcHist(){
		return calcHist;
	}
	
	/**
	 * If 'calcHist' is true, this will return the value in the braces of the 'attribute' (Weka). (e.g. a character bigram feature may contain 'ab' in the braces)
	 * @return
	 */
	public String getStringInBraces(){
		return stringInBraces;
	}
	
	/**
	 * returns the full name of the attribute, as taken from Weka's Instances object.
	 * @return
	 */
	public String getFullName(){
		return fullName;
	}
	
	/**
	 * returns something halfway between the full name and generic name of the attribute. 
	 * @return
	 */
	public String getTrimmedAttrib(){
		return trimmedAttrib;
	}
	
	private double delta;
	/**
	 *	change in feature: targetValue - toModifyValue 
	 * @return
	 */
	public double getDeltaValue(){
		if(targetValue != -1){
			delta = targetValue - toModifyValue;
			return delta;
		}
		else
			return -1;
	}
	
	public double getChangeNeeded() {
		return getPercentChangeNeeded(false, false, false);
	}
	
	/**
	 * returns the percent change needed for the feature contained by this Attribute. Signed, so a negative number indicates the feature needs to be removed, 
	 * and vice versa. Percent change needed is calculated at the time of function call, so the returned value will always be current. 
	 * @param normalizeToBaseline if true, method will return the percent change that is needed normalized to the percent change that was needed initially (so, 100% is the greatest change you should see).
	 * if false, method will return the absolute percent change needed, which does not normalize to the baseline (meaning if you have 5 'a' characters, and you need 15, then it would return 200%).
	 * @param invertPercentage
	 * @param takeAbsVal
	 * @return
	 */
	public double getPercentChangeNeeded(boolean normalizeToBaseline, boolean invertPercentage, boolean takeAbsVal){
		double temp = 0;
		double perm = 0;
		double minimumPercentChangeUnit = 0;
		double halfOfMin;
		double theModulus;
		if(toModifyValue != 0){
			minimumPercentChangeUnit = ((100/toModifyValue)/100);
			halfOfMin =minimumPercentChangeUnit / 2;
			temp = (targetValue - toModifyValue)/toModifyValue;// signedness matters, don't take abs. value
			theModulus = temp % minimumPercentChangeUnit;
			if ((temp*requiredDirectionOfChange  < 0)  && (Math.abs(temp) <minimumPercentChangeUnit)  ) // if the required direction of change is not the same sign (or 0) as temp,
				// and the percent change needed is less than a minimumPercentChangeUnit,  (i.e., if it wants to move you backward past the target value after having already passed it), temp = 0
				temp = 0;
			else if ( (theModulus/2) < halfOfMin) //  otherwise, if percent change needed is less than halfway between a minimumPercentChangeUnit,
				temp = temp - theModulus; // round down to the closest minimumPercentChangeUnit
			else // otherwise, if percent change needed is greater than or exactly halfway between a minimumPercentChangeUnit,
				temp = temp + (minimumPercentChangeUnit - theModulus); // round up to the next highest minimumPercentChangeUnit
				
		}
		else
			temp = Math.ceil(this.targetValue); // if value doesnt exist in document, set percent change needed to the ceil value of the  target value (e.g. add 5 occurrences of 'if': 500%)
			// XXX NOTE: I am rounding this up because if the feature doesn't exist, and it should be present, it seems that it would be
			// fairly important to add. However, taking the actual percent change that it would need is impossible (div. by zero)...
		if(!haveSetBaselinePercentChangeNeeded){
			baselinePercentChangeNeeded = temp*100;
			haveSetBaselinePercentChangeNeeded = true;
		}
		if(normalizeToBaseline){
			perm = (((temp*100)-baselinePercentChangeNeeded)/baselinePercentChangeNeeded);
			if(invertPercentage)
				perm = 100 - perm;
		}
		else{
			perm = temp;
			if(invertPercentage)
				perm = baselinePercentChangeNeeded - perm;
		}
		if(takeAbsVal)
			return Math.abs(perm); 
		else
			return perm;
	}

	public double getFeatureBaselinePercentChangeNeeded(){
		return baselinePercentChangeNeeded;
	}
		
	
	
	public void setAuthorConfidence(double authorConfidence){
		this.authorConfidence = authorConfidence;
	}
	
	public double getAuthorConfidence(){
		return authorConfidence;
	}
	
	public void setRangeForTarget(double min, double max){
		this.targetClusterMin = min;
		this.targetClusterMax = max;
		
	}
	
	/**
	 * checks the current value of this feature against the range of acceptable values as determined by the user's ClusterGroup selection
	 * @param currentValue Attribute's current feature value
	 * @return
	 * 	a negative number if currentValue is too high (indicating how much it needs to come down), positive number if currentValue is too low 
	 * 	(indicating how much it needs to go up), or '0' if the current value falls within the acceptable range. 
	 */
	public double getRangeCheck(double currentValue){
		if(currentValue > targetClusterMax)
			return targetClusterMax - currentValue;
		else if (currentValue < targetClusterMin)
			return targetClusterMin - currentValue;
		else
			return 0;
		
	}

	public Boolean isPartofSpeech() {
		return partOfSpeech;
	}
}
