package edu.drexel.psal.jstylo.generics;

/**
 * Enumeration of all types of baselines for normalization for any feature. A feature is a specific entry in the
 * features vector, for instance the appearances of the digit '5'. A feature class is all features of the same type
 * derived from the same event driver, for instance appearances of all digist ('0', '1' etc.).
 * 
 * @author Ariel Stolerman
 * 
 */
public enum NormBaselineEnum {
	// output absolute values
	NONE("None",
			"No normalization - absolute values."),
	
	// feature class within the current document, e.g. all appearances of digits in the document
/*	FEATURE_CLASS_IN_DOC("Feature class frequency in the document",
			"Normalize over the total frequency of all features of the same class in the current document. For instance, when using " +
			"letter bigrams, the normalization value will be the sum of frequencies of all letter bigrams in the current document. " +
			"This normalization is in per document, i.e. does not take into account frequencies in other documents."),
	
	// same as above only across all documents in the training corpus
	FEATURE_CLASS_ALL_DOCS("Feature class frequency across all documents",
			"Normalize over the total frequency of all features of the same class across all documents in the training corpus. For instance, when using " +
			"letter bigrams, the normalization value will be the sum of frequencies of all letter bigrams in all documents in the training corpus."),
	
	// appearances of the specific feature across all documents in the training corpus
	FEATURE_ALL_DOCUMENTS("Feature frequency across all documents",
			"Normalize over the total frequency of the current feature across all documents in the training corpus. If the feature is actually a feature " +
			"class (e.g. letter bigrams that includes all possible pairs of letters where each is a unique feature), the normalization is done over each " +
			"feature separately. For instance, when using letter bigrams, the normalization value for each letter bigram (e.g. \"in\") will be the sum of " +
			"frequencies of that letter bigram in all documents in the training corpus."),
*/
	// number of sentences in the document
	SENTENCES_IN_DOC("Number of sentences in the document",
			"Normalize over the total number of sentences in the current document."),
	
	// number of words in the document
	WORDS_IN_DOC("Number of words in the document",
			"Normalize over the total number of words in the current document."),
	
	// number of characters in the document
	CHARS_IN_DOC("Number of characters in the document",
			"Normalize over the total number of characters in the current document."),
	
	// number of letters in the document
	LETTERS_IN_DOC("Number of (English) letters in the document",
			"Normalize over the total number of (English) letters in the current document.");
	
	private String title;
	private String description;
	
	private NormBaselineEnum(String title, String description) {
		this.title = title;
		this.description = description;
	}
	
	/*
	public String toString() {
		return title;
	}
	*/
	
	public String getTitle() {
		return title;
	}
	
	public String getDescription() {
		return description;
	}
	
	public static String[] getAllTitles() {
		NormBaselineEnum[] values = NormBaselineEnum.values();
		String[] res = new String[values.length];
		for (int i=0; i<res.length; i++) {
			res[i] = values[i].title;
		}
		return res;
	}
	
	public static String[] getAllDescriptions() {
		NormBaselineEnum[] values = NormBaselineEnum.values();
		String[] res = new String[values.length];
		for (int i=0; i<res.length; i++) {
			res[i] = values[i].description;
		}
		return res;
	}
	
	public static NormBaselineEnum[] getAllNormBaselines() {
		return NormBaselineEnum.values();
	}
	
	/**
	 * Returns the normalization baseline enum with the given title.
	 * @param title
	 * 		The title of the requested normalization baseline enum.
	 * @return
	 * 		The normalization baseline enum with the given title.
	 */
	public static NormBaselineEnum getNormBaselineFromTitle(String title) {
		for (NormBaselineEnum nbl: getAllNormBaselines()) {
			if (nbl.title.equals(title))
				return nbl;
		}
		return NONE;
	}
	
	public static void main(String[] args) {
		System.out.println(NormBaselineEnum.valueOf("NONE").getDescription());
	}
}
