package edu.drexel.psal.anonymouth.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import edu.drexel.psal.ANONConstants;
import edu.drexel.psal.anonymouth.gooie.ClustersDriver;
import edu.drexel.psal.anonymouth.gooie.ThePresident;
import edu.drexel.psal.jstylo.generics.*;

import java.util.HashMap;

import com.jgaap.generics.Document;

import weka.attributeSelection.InfoGainAttributeEval;
import weka.classifiers.Classifier;
import weka.core.Instances;

/**
 * Performs general calculations relating to analyzing the data, including (but not limited to):
 * averages, standard deviations, feature information gain, and controlling the clustering and cluster analytics.
 * @author Andrew W.E. McDonald
 *
 */
public class DataAnalyzer{
	
	private final String NAME = "( "+this.getClass().getSimpleName()+" ) - ";
	private int numFeaturesToReturn = ThePresident.max_Features_To_Consider;
	public static Attribute[] topAttributes;
	public static int lengthTopAttributes;
	private String[] importantAttribs;
	private ArrayList<String> allAttribs;
	private ArrayList<String> trainAttribs;
	private ArrayList<String> authorAttribs;
	private String[] strippedAttributeNames;
	/**
	 * Set of all input documents
	 * */
	private ProblemSet pSet;
	private Double[][] trainingInstancesArray;	
	private Double[][] authorInstancesArray;
	private Double[][] toModifyInstancesArray;
	double[] authorAverages;
	double[] authorStdDevs;
	double[][] minsAndMaxes;
	private ArrayList<String> featuresForClusterAnalyzer;
	public static int[] selectedTargets;
	private HashMap<String,Double> holderForLogger = new HashMap<String,Double>();
	private FeatureSwapper featureSwapper;
	private static ClusterGroup bestClusterGroup;
	private int maxClusters;
	
	/**
	 * constructor for DataAnalyzer
	 */
	public DataAnalyzer(ProblemSet pSet) {
		Logger.logln(NAME+"Created DataAnalyzer");
		this.pSet = pSet;
	}
	
	/**
	 * returns the array of top Attributes (features with the highest information gain)
	 * @return
	 */
	public Attribute[] getAttributes() {
		return topAttributes;
	}
	
	/**
	 * returns the bestClusterGroup that contains the target values for each feature
	 * @return
	 */
	public ClusterGroup getBestClusterGroup() {
		return bestClusterGroup;
	}
	
	/**
	 * takes average of each feature across all instances specified by "instancesArray" 
	 */
	public void authorAverageFinder() {
		Logger.logln(NAME+"called authorAverageFinder");
		int i;
		int j;
		int numInstances = topAttributes[0].getAuthorVals().length;
		double sum;
		double[] currentFeature;
		double average;
		for (i=0;i<lengthTopAttributes;i++) {
			currentFeature = topAttributes[i].getAuthorVals();
			sum = 0;
			
			for (j=0;j<numInstances;j++) {
				sum=sum+currentFeature[j];
			}
			
			average = (sum/numInstances);
			topAttributes[i].setAuthorAvg(average);
		}
		Logger.logln(NAME+"found author averages");
	}
	
	/**
	 * finds the minimum and maximum values of each feature across all instances given
	 */
	public void minsAndMaxes() {
		Logger.logln(NAME+"called minsAndMaxes - finding min and max for each feature");
		int numFeatures = topAttributes[0].getTrainVals().length;
		int j=0;
		int i=0;
		double theMin;
		double theMax;
		double[] featureRay;
		
		for (i=0;i<lengthTopAttributes;i++) { // i runs through each instance (row)
			j=0;
			featureRay = topAttributes[i].getTrainVals();
			theMin=featureRay[j];
			theMax=featureRay[j];
			
			for (j=0;j<numFeatures;j++) { // j runs though each feature (column)
				if(theMin>featureRay[j])
					theMin=featureRay[j];
				if(theMax<featureRay[j])
					theMax=featureRay[j];
			}
			topAttributes[i].setTrainMin(theMin);
			topAttributes[i].setTrainMax(theMax);
		}
		Logger.logln(NAME+"finished finding mins and maxes");
	}
	
	/**
	 * Finds standard deviation of author's values
	 */
	public void authorStdDevFinder() {
		Logger.logln(NAME+"called authorStdDevFinder");
		int i;
		int j;
		int numInstances = topAttributes[0].getAuthorVals().length; 
		double tempAvg;
		double stdDev;
		double sum;
		double [] tempFeatRay;
		
		for (i=0;i<lengthTopAttributes;i++) {
			tempAvg = topAttributes[i].getAuthorAvg();
			tempFeatRay = topAttributes[i].getAuthorVals();
			sum = 0;
			
			for (j=0;j<numInstances;j++) {
				sum=sum+((tempFeatRay[j]-tempAvg)*(tempFeatRay[j]-tempAvg));
			}
			stdDev = Math.sqrt((sum/numInstances));
			topAttributes[i].setAuthorStdDev(stdDev);
			topAttributes[i].setAuthorConfidence(1.96*stdDev);
		}
		Logger.logln(NAME+"finished finding author standard deviations");
	}
	
	/**
	 * Finds the top 'numFeatures' after information gain has been calculated for them all
	 * @param theArffFile the instances
	 * @param numFeatures the number of features to return (starts from highest information gain)
	 * @return
	 * 	 Array of Attribute objects, one Attribute per feature
	 * @throws Exception
	 */
	public Attribute[] findMostInfluentialEvents(Instances theArffFile, int numFeatures) throws Exception {
		Logger.logln(NAME+"called findMostInfluentialEvents... for "+numFeatures+" features");
		int i=0;
		int numAttributes= theArffFile.numAttributes();
		double[][] tempAllInfoGain= new double[numAttributes][2];//
		InfoGainAttributeEval IGAE = new InfoGainAttributeEval();
		theArffFile.setClass(theArffFile.attribute("authorName"));
		IGAE.buildEvaluator(theArffFile);
		int index = 0;
		for (i=0;i<numAttributes;i++)
			if (IGAE.evaluateAttribute(i) > 0) {
				tempAllInfoGain[index][0]=IGAE.evaluateAttribute(i);
				tempAllInfoGain[index][1]=i;
				index++;
			}
		double[][] allInfoGain = new double[index][2];
		for (i = 0; i < index; i++) {
			allInfoGain[i][0] = tempAllInfoGain[i][0];
			allInfoGain[i][1] = tempAllInfoGain[i][1];
		}
		
		//Sort array of info gains from greatest to least
		Arrays.sort(allInfoGain, new Comparator<double[]>() {
			@Override
			public int compare(final double[] first, final double[] second) {
				return ((-1)*((Double)first[0]).compareTo(((Double)second[0]))); // multiplying by -1 will sort from greatest to least, which saves work.
			}
		});
		
		//Construct array of Attributes, save full attribute name and info gain into 
		featuresForClusterAnalyzer = new ArrayList<String>(numFeatures);
		Attribute[] topAttribs = new Attribute[numFeatures];
		strippedAttributeNames = new String[numFeatures];
		int j = 0;
		String strippedAttrib;
		
		int numAvailableFeatures=0;
		if (numFeatures < allInfoGain.length) 
			numAvailableFeatures = numFeatures;
		else numAvailableFeatures = allInfoGain.length;
		
		for (i = 0; i < numAvailableFeatures;i++) {
			String attrib = (theArffFile.attribute((int) allInfoGain[i][1]).toString());
			strippedAttrib = AttributeStripper.strip(attrib);	
			int toModifyIndex = allAttribs.indexOf(strippedAttrib);
			if (attrib.contains("authorName")) {
					numFeatures++;
					continue;
			}
			
			// TODO do we want this??
			//if (toModifyIndex == -1) {
			//	numFeatures++;
			//	continue;
			//}
		
			String stringInBraces;
			boolean calcHist = false;
			if (attrib.contains("{") && attrib.contains("}")) {
				stringInBraces = attrib.substring(attrib.indexOf('{')+1,attrib.indexOf('}'));
			} else { // if no match is found, set 'stringInBraces == to an empty string
				stringInBraces = "";
			}
			
			if (stringInBraces.equals("") || stringInBraces.equals("-")) {
				stringInBraces = "";
				calcHist = false;
			} else {
				calcHist = true;
				
			}
			//try {
			topAttribs[j] = new Attribute((int)allInfoGain[i][1],attrib,stringInBraces,calcHist);
			/*} catch (Exception e){
				System.out.println("i: "+i+" j: "+j);
				System.out.println("stringInBraces:"+stringInBraces+":");
				System.out.println("allInfoGain: "+allInfoGain[i][1]);
				System.out.println("calcHist: "+calcHist);
				e.printStackTrace();
				System.exit(0);
			}*/
			topAttribs[j].setInfoGain(allInfoGain[j][0]);
			topAttribs[j].setToModifyValue(toModifyInstancesArray[0][toModifyIndex]);
			featuresForClusterAnalyzer.add(topAttribs[j].getConcatGenNameAndStrInBraces());
			strippedAttributeNames[j] = strippedAttrib;	
			Logger.logln(NAME+topAttribs[j].getFullName()+" info gain for this feature is: "+topAttribs[j].getInfoGain()+", calcHist is: "+topAttribs[j].getCalcHist()+" string in the braces (if applicable): "+topAttribs[j].getStringInBraces()+" toModify value is: "+topAttribs[j].getToModifyValue()); 
			j++;
			
		}
		Logger.logln(NAME+"found all top attributes");
		
		return topAttribs;	
	}
	
	
	/**
	 * computes the information gain from the top 'numFeaturesToReturn' number of features specified by the constructor
	 * @param presentSet - an unmodified Instances object to compute information gain from
	 * @return
	 *  Array of Attribute objects, one Attribute for each feature
	 * @throws Exception 
	 */
	public Attribute[] computeInfoGain(Instances presentSet) throws Exception {
		Logger.logln(NAME+"called computeInfoGain");
		int i;
		int j;
		presentSet.setClass(presentSet.attribute("authorName"));
		Attribute[] topAttribs;
		topAttribs = findMostInfluentialEvents(presentSet,numFeaturesToReturn);
		int numAttribs = topAttribs.length;
		
		int numTrainInstances = trainingInstancesArray.length;
		int numAuthInstances = authorInstancesArray.length;
		double[][]	relevantTrainFeats = new double[numAttribs][numTrainInstances];
		double [][] relevantAuthorFeats = new double[numAttribs][numAuthInstances];
		importantAttribs = new String[numAttribs];
		
		// Extract the top "n" important features and place them into an array in order of info gain (greatest -> least). This happens per instance to allow for std. dev. calculations.
		int tempTrainIndex =0;
		int tempAuthorIndex =0;
		boolean trainOk = true;
		boolean authorOk = true;
		for (i=0;i<numAttribs;i++) {
			String tempAttrib = strippedAttributeNames[i];
			
			if (trainAttribs.contains(tempAttrib) == true) {
				tempTrainIndex = trainAttribs.indexOf(tempAttrib);
				trainOk = true;
			} else {
				trainOk = false;
				tempTrainIndex = 0; // basically re-setting... this wont be used in this case until the next iteration.
			}
			
			if (authorAttribs.contains(tempAttrib)) {
				tempAuthorIndex = authorAttribs.indexOf(tempAttrib);
				authorOk = true;
			} else {
				authorOk = false;
				tempAuthorIndex = 0;
			}
			
			for (j=0;j<numTrainInstances;j++) {
					if(trainOk == true)
						relevantTrainFeats[i][j]=trainingInstancesArray[j][tempTrainIndex]; // transpose and filter the instancesArray
					else
						relevantTrainFeats[i][j]=0;
				if (j < numAuthInstances) {
					if(authorOk == true)
						relevantAuthorFeats[i][j] = authorInstancesArray[j][tempAuthorIndex];
					else
						relevantAuthorFeats[i][j] = 0;
				}
			}
		}
		
		int actualNumAttribs = 0; //number of non-null attributes
		for (int k=0; k<topAttribs.length;k++) {
			if (topAttribs[k] == null) 
				break;
			else actualNumAttribs++;
		}
		
		if (actualNumAttribs < topAttribs.length) {
			Attribute[] tempAttribs = new Attribute[actualNumAttribs];
			for (int n =0; n < actualNumAttribs; n++ ) {
				tempAttribs[n]=topAttribs[n];
			}
			topAttribs=tempAttribs;
		}
		
		String theSpacer = "";
		for (i=0;i<topAttribs.length;i++) {
			if(topAttribs[i].getStringInBraces().equals(""))
				theSpacer = "";
			else
				theSpacer = " ";
			importantAttribs[i] = topAttribs[i].getGenericName().toString()+theSpacer+topAttribs[i].getStringInBraces();
			topAttribs[i].setTrainVals(relevantTrainFeats[i]);
			topAttribs[i].setAuthorVals(relevantAuthorFeats[i]);
			double thisVal = topAttribs[i].getToModifyValue();
			holderForLogger.put(topAttribs[i].getConcatGenNameAndStrInBraces(), thisVal);
		}
		
		Logger.logln(NAME+"****** Current list of Present values for: "+ThePresident.sessionName+" process request number: "+DocumentMagician.numProcessRequests+" ******");
		Logger.logln(NAME+holderForLogger.entrySet().toString());
		return topAttribs;
		
	}
		
	
	/**
	 * runs clustering algorithm on all features, and saves information in each feature's 'Attribute' object.
	 * @return
	 * 	number of maximum clusters found, to be used with @ClusterAnalyzer
	 */
	public int runAllTopFeatures() {
		Logger.logln(NAME+"called runAllTopFeatures");
		int numAttribs = topAttributes.length;
		int numAuthors = DocumentMagician.numSampleAuthors;
		int maxClusters = 0;
		int tempMaxClusters;
		
		for (int sel = 0; sel < numAttribs; sel++) {
			Cluster[] orderedClusters;
			TargetExtractor extractor = new TargetExtractor(numAuthors, topAttributes[sel]);
			extractor.aMeansCluster();
			orderedClusters = extractor.getPreferredOrdering();
			topAttributes[sel].setOrderedClusters(orderedClusters);
			tempMaxClusters = orderedClusters.length;
			if(tempMaxClusters > maxClusters)
				maxClusters = tempMaxClusters;
		}
		Logger.logln(NAME+"Max number of clusters (after clustering all features): "+maxClusters);
		return maxClusters;
		
	}
	
	
	/**
	 * runs the @ClusterAnalyzer . Note that this method alone does <i>not</i> modify the clusters or the preference ordering.
	 * it simply analyzes the existing ones. 
	 * @param maxClusters the maximum number of clusters out of all of the features
	 */
	public void runClusterAnalysis(DocumentMagician magician) {
		Logger.logln(NAME+"called runClusterAnalysis");
		int lenTopAttribs = topAttributes.length;
		ClusterAnalyzer numberCruncher = new ClusterAnalyzer(featuresForClusterAnalyzer,maxClusters);
		int i =0;
		
		for (i=0;i<lenTopAttribs;i++) {
			numberCruncher.addFeature(topAttributes[i]);
		}
		numberCruncher.analyzeNow();
		featureSwapper = new FeatureSwapper(ClusterAnalyzer.getClusterGroupArray(), magician) ;
		bestClusterGroup = featureSwapper.getBestClusterGroup(-1);
		ClustersDriver.bestClusterGroup = bestClusterGroup;
		Logger.logln(NAME+"calling makeViewer");
		ClustersDriver.makePanels(topAttributes);
		Logger.logln(NAME+"viewer made");
		setSelectedTargets();
	}
	
	
	public void setSelectedTargets(){
		Logger.logln(NAME+"called setSelectedTargets after cluster group selection.");
		int i=0;
		int clusterNumber;
		Cluster tempCluster;
		double target;
		String targetSaver = "                  ~~~~~~~ Targets ~~~~~~~\n";
		
		int[] clusterNumbers = bestClusterGroup.getGroupKey().toIntArray();
		double[] centroidTest = new double[lengthTopAttributes];
		for (i=0;i<lengthTopAttributes;i++) {
			clusterNumber = clusterNumbers[i]-1; // From ClusterAnalyzer, all cluster numbers are "1" greater than their indices (to ease computation)
			tempCluster = topAttributes[i].getOrderedClusters()[clusterNumber];
			target = tempCluster.getCentroid();
			centroidTest[i] = target;
			targetSaver += "Attribute: "+topAttributes[i].getFullName()+"  ==> targetValue: "+target+"\n";
			topAttributes[i].setTargetCentroid(target);
			topAttributes[i].setTargetValue(target);
			topAttributes[i].setRangeForTarget(tempCluster.getMinValue(),tempCluster.getMaxValue()); // maybe this should be changed to avg. avs. dev.
		}
		Logger.logln(NAME+"Targets have been set, and they are:\n"+targetSaver);
	}
	
	/**
	 * Updates the toModifyValues of all attributes in the top attributes array, 
	 * so the current document's positions in the clusters window will update properly
	 */
	public void updateToModifyValues(DocumentMagician magician) {
		Logger.logln(NAME+"called updateToModifyValues");
		//Get new instance from DocumentMagician
		HashMap<String,Double[][]> attribsAndInstances = magician.getPackagedInstanceData();
		toModifyInstancesArray = attribsAndInstances.get("modify");
		for (int i = 0; i < lengthTopAttributes; i++) {
			String attribName = topAttributes[i].getFullName();
			int toModifyIndex = allAttribs.indexOf(AttributeStripper.strip(attribName));
			topAttributes[i].setToModifyValue(toModifyInstancesArray[0][toModifyIndex]);
		}
	}
	
	
	
	/**
	 * Runs the initial processing on all documents 
	 * @param magician
	 * @param cfd
	 * @param classifier
	 * @throws Exception
	 */
	public void runInitial(DocumentMagician magician, CumulativeFeatureDriver cfd, Classifier classifier) throws Exception {
		Logger.logln(NAME+"called runIntitial in DataAnalyzer");
		List<Document> tempTestDocs = pSet.getAllTestDocs(); // get the document(s) to anonymize
		pSet.removeTestDocAt((String) pSet.getTestAuthorMap().keySet().toArray()[0], tempTestDocs.get(0).getTitle());
		for (Document d:tempTestDocs) {
			d.setAuthor(ANONConstants.DUMMY_NAME);
			pSet.addTestDoc(ANONConstants.DUMMY_NAME, d);
		}
		
		magician.initialDocToData(pSet,cfd, classifier);
		
		HashMap<String,Double[][]> attribsAndInstances = magician.getPackagedInstanceData();
		HashMap<String,Instances> simplyInstances = magician.getPackagedFullInstances();
		trainingInstancesArray = attribsAndInstances.get("training");
		authorInstancesArray = attribsAndInstances.get("author");
		toModifyInstancesArray = attribsAndInstances.get("modify");
		ArrayList<ArrayList<String>> allAttribSets = magician.getAllAttributeSets();
		allAttribs = allAttribSets.get(0);
		trainAttribs = allAttribSets.get(1);
		authorAttribs = allAttribSets.get(2);
		
		topAttributes = computeInfoGain(simplyInstances.get("authorAndTrain")); 
		lengthTopAttributes = topAttributes.length;
		authorAverageFinder();
		authorStdDevFinder();
		minsAndMaxes();
		
		maxClusters =runAllTopFeatures();
		Logger.logln(NAME+"Initial has been run.");
	}
	
	/**
	 * Re-classifies the modified document
	 * @param magician
	 * @throws Exception
	 */
	public void reRunModified(DocumentMagician magician) throws Exception {
		magician.reRunModified();
		Logger.logln(NAME+"Updating toModify values for top attributes");
		updateToModifyValues(magician);
		Logger.logln(NAME+"Calling makeViewer in ClusterViewer after re-running modified.");
		ClustersDriver.makePanels(topAttributes);
		Logger.logln(NAME+"viewer made");
	}
	
	/**
	 * returns the names of the top 'numFeaturesToReturn' number of features
	 * @return
	 */
	public String[] getAllRelevantFeatures() {
		return importantAttribs;
	}
}