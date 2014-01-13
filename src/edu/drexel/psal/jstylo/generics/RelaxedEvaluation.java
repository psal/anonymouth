package edu.drexel.psal.jstylo.generics;

import weka.classifiers.CostMatrix;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import java.util.Comparator;

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

}
