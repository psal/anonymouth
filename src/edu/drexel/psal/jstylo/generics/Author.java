package edu.drexel.psal.jstylo.generics;

public class Author {

	private String name;
	private int numberOfDocuments;
	private int truePositiveCount;
	private int falsePositiveCount;
	private int trueNegativeCount;
	private int falseNegativeCount;
	
	public Author(String n){
		name = n;
		numberOfDocuments =0;
		truePositiveCount =0;
		falsePositiveCount=0;
	}
	
	/////Increment methods
	
	public void incrementNumberOfDocuments(){
		numberOfDocuments++;
	}
	
	public void incrementTruePositiveCount(){
		truePositiveCount++;
	}
	
	public void incrementFalsePositiveCount(){
		falsePositiveCount++;
	}
	
	public void incrementTrueNegativeCount(){
		trueNegativeCount++;
	}
	
	public void incrementFalseNegativeCount(){
		falseNegativeCount++;
	}
	
	/////Get methods
	public String getName(){
		return name;
	}
	
	public int getNumberOfDocuments(){
		return numberOfDocuments;
	}
	
	public int getTruePositiveCount(){
		return truePositiveCount;
	}
	
	public int getTrueNegativeCount(){
		return trueNegativeCount;
	}
	
	public int getFalsePositiveCount(){
		return falsePositiveCount;
	}
	
	public int getFalseNegativeCount(){
		return falseNegativeCount;
	}
	
	public double getTruePositiveRate(){
		double result = ((double) truePositiveCount)/(truePositiveCount+falseNegativeCount);
		if (Double.isNaN(result))
			return 0.0;
		else
			return result;
	}
	
	public double getFalsePositiveRate(){
		double result = ((double) falsePositiveCount)/(falsePositiveCount+trueNegativeCount);
		if (Double.isNaN(result))
			return 0.0;
		else
			return result;
	}
	
	public double getTrueNegativeRate(){
		double result = ((double) trueNegativeCount)/(trueNegativeCount+falsePositiveCount);
		if (Double.isNaN(result))
			return 0.0;
		else
			return result;
	}
	
	public double getFalseNegativeRate(){
		double result = ((double) falseNegativeCount)/(falseNegativeCount+truePositiveCount);
		if (Double.isNaN(result))
			return 0.0;
		else
			return result;
	}
	
	public double getPrecision(){
		double result = ((double) truePositiveCount)/(truePositiveCount+falsePositiveCount);
		if (Double.isNaN(result))
			return 0.0;
		else
			return result;
	}
	
	public double getRecall(){
		double result = ((double)truePositiveCount)/(truePositiveCount+falseNegativeCount);
		if (Double.isNaN(result))
			return 0.0;
		else
			return result;
	}
	
	public double getF1Measure(){
		double result = 2.0*((getPrecision()*getRecall())/(getPrecision()+getRecall()));
		if (Double.isNaN(result))
			return 0.0;
		else
			return result;
	}
	
	public double getMCC(){
		double result = ((double)((truePositiveCount*trueNegativeCount)-(falsePositiveCount*falseNegativeCount)))/
					(Math.sqrt(((double) (truePositiveCount+falsePositiveCount)*(truePositiveCount+falseNegativeCount)*
						(trueNegativeCount+falsePositiveCount)*(trueNegativeCount+falseNegativeCount))));
		if (Double.isNaN(result))
			return 0.0;
		else
			return result;
	}
	
}
