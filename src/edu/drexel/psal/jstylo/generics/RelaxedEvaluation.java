package edu.drexel.psal.jstylo.generics;

import weka.classifiers.CostMatrix;
import weka.classifiers.Evaluation;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;


public class RelaxedEvaluation extends Evaluation {
	protected int relaxParam;

	/**
	 * Initializes all the counters for the evaluation. 
	 * Use <code>useNoPriors()</code> if the dataset is the test set and you
	 * can't initialize with the priors from the training set via 
	 * <code>setPriors(Instances)</code>.
	 *
	 * @param data 	set of training instances, to get some header 
	 * 			information and prior class distribution information
	 * @throws Exception 	if the class is not defined
	 * @see 		#useNoPriors()
	 * @see 		#setPriors(Instances)
	 */
	public RelaxedEvaluation(Instances data, int relaxParam) throws Exception {
		super(data);
		this.relaxParam = relaxParam;
	}

	/**
	 * Initializes all the counters for the evaluation and also takes a
	 * cost matrix as parameter.
	 * Use <code>useNoPriors()</code> if the dataset is the test set and you
	 * can't initialize with the priors from the training set via 
	 * <code>setPriors(Instances)</code>.
	 *
	 * @param data 	set of training instances, to get some header 
	 * 			information and prior class distribution information
	 * @param costMatrix 	the cost matrix---if null, default costs will be used
	 * @throws Exception 	if cost matrix is not compatible with 
	 * 			data, the class is not defined or the class is numeric
	 * @see 		#useNoPriors()
	 * @see 		#setPriors(Instances)
	 */
	public RelaxedEvaluation(Instances data, CostMatrix costMatrix, int relaxParam) 
			throws Exception {
		super(data, costMatrix);
		this.relaxParam = relaxParam;
	}

	/**
	 * Compares Doubles by ascending order
	 */
	static Comparator<Double> descendingDouble = new Comparator<Double>() {
		@Override
		public int compare(Double arg0, Double arg1) {
			return -1 * arg0.compareTo(arg1);
		}
	};

	static Comparator<Integer> descendingInteger = new Comparator<Integer>() {
		@Override
		public int compare(Integer arg0, Integer arg1) {
			return -1 * arg0.compareTo(arg1);
		}
	};

	/**
	 * Updates all the statistics about a classifiers performance for 
	 * the current test instance.
	 *
	 * @param predictedDistribution the probabilities assigned to 
	 * each class
	 * @param instance the instance to be classified
	 * @throws Exception if the class of the instance is not
	 * set
	 */
	//FIXME this method was broken upon switch to weka 3.7.9
	protected void updateStatsForClassifier(double [] predictedDistribution,
			Instance instance)
					throws Exception {

		int actualClass = (int)instance.classValue();

		if (!instance.classIsMissing()) {
			updateMargins(predictedDistribution, actualClass, instance.weight());

			// collect all predictions and their corresponding classes
			SortedMap<Double,Integer> predToClass =
					new TreeMap<Double,Integer>(descendingDouble);
			for(int i = 0; i < m_NumClasses; i++) {
				predToClass.put(predictedDistribution[i], i);
			}
			List<Integer> candidateClasses = new ArrayList<Integer>(relaxParam);
			int count = 0;
			for (Double pred: predToClass.keySet())
			{
				candidateClasses.add(predToClass.get(pred));
				count++;
				if (count == relaxParam)
					break;
			}
			// check if relaxed set of candidates contains actual, if so -
			// attribute that prediction
			// otherwise - take the to pprediction
			int predictedClass = -1;
			if (candidateClasses.contains(actualClass))
				predictedClass = actualClass;
			else
				predictedClass = candidateClasses.get(0);

			/*
			// Determine the predicted class (doesn't detect multiple 
			// classifications)
			int predictedClass = -1;
			double bestProb = 0.0;
			for(int i = 0; i < m_NumClasses; i++) {
				if (predictedDistribution[i] > bestProb) {
					predictedClass = i;
					bestProb = predictedDistribution[i];
				}
			}
			 */

			m_WithClass += instance.weight();

			// Determine misclassification cost
			if (m_CostMatrix != null) {
				if (predictedClass < 0) {
					// For missing predictions, we assume the worst possible cost.
					// This is pretty harsh.
					// Perhaps we could take the negative of the cost of a correct
					// prediction (-m_CostMatrix.getElement(actualClass,actualClass)),
					// although often this will be zero
					m_TotalCost += instance.weight()
							* m_CostMatrix.getMaxCost(actualClass, instance);
				} else {
					m_TotalCost += instance.weight() 
							* m_CostMatrix.getElement(actualClass, predictedClass,
									instance);
				}
			}

			// Update counts when no class was predicted
			if (predictedClass < 0) {
				m_Unclassified += instance.weight();
				return;
			}

			double predictedProb = Math.max(MIN_SF_PROB,
					predictedDistribution[actualClass]);
			double priorProb = Math.max(MIN_SF_PROB,
					m_ClassPriors[actualClass]
							/ m_ClassPriorsSum);
			if (predictedProb >= priorProb) {
				m_SumKBInfo += (Utils.log2(predictedProb) - 
						Utils.log2(priorProb))
						* instance.weight();
			} else {
				m_SumKBInfo -= (Utils.log2(1.0-predictedProb) - 
						Utils.log2(1.0-priorProb))
						* instance.weight();
			}

			m_SumSchemeEntropy -= Utils.log2(predictedProb) * instance.weight();
			m_SumPriorEntropy -= Utils.log2(priorProb) * instance.weight();

			updateNumericScores(predictedDistribution, 
					makeDistribution(instance.classValue()), 
					instance.weight());

			// Update other stats
			m_ConfusionMatrix[actualClass][predictedClass] += instance.weight();
			if (predictedClass != actualClass) {
				m_Incorrect += instance.weight();
			} else {
				m_Correct += instance.weight();
			}
		} else {
			m_MissingClass += instance.weight();
		}
	}
}
