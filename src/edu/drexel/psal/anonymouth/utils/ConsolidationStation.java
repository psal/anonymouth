package edu.drexel.psal.anonymouth.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.locks.Lock;

import edu.drexel.psal.anonymouth.engine.Attribute;
import edu.drexel.psal.anonymouth.engine.DataAnalyzer;
import edu.drexel.psal.anonymouth.engine.FeatureList;
import edu.drexel.psal.anonymouth.gooie.DriverEditor;
import edu.drexel.psal.anonymouth.gooie.ThePresident;
import edu.drexel.psal.jstylo.generics.Logger;
import edu.drexel.psal.jstylo.generics.Logger.LogOut;
import edu.stanford.nlp.ling.TaggedWord;

/**
 * 
 * @author Andrew W.E. McDonald
 * @author Joe Muoio
 *
 */
public class ConsolidationStation {
	
	private final String NAME = "( "+this.getClass().getName()+" ) - ";
	
	static HashMap<String,ArrayList<TreeData>> parsed;
	static ArrayList<Triple> toAdd=new ArrayList<Triple>(400);
	static ArrayList<Triple> toRemove=new ArrayList<Triple>(400);
	public static ArrayList<TaggedDocument> otherSampleTaggedDocs;//initialized in backendInterfaces.
	public static ArrayList<TaggedDocument> authorSampleTaggedDocs;
	public static ArrayList<TaggedDocument> toModifyTaggedDocs;//init in editor Tab Driver
	private static boolean allDocsTagged = false;
	public static FunctionWord functionWords=new FunctionWord();
	
	private HashMap<String,Word>wordsToAdd;
	private HashMap<String,Word>newWordsToAdd;
	private HashMap<String,Word>wordsInDocToMod;
	private HashMap<String,Word>wordsToRemove;
	
	/**
	 * constructor for ConsolidationStation. Depends on target values, and should not be called until they have been selected.
	 * @param attribs
	 * @param parsed
	 */
	public ConsolidationStation(){
		this.parsed = parsed;
		toAdd = new ArrayList<Triple>(400);
		toRemove = new ArrayList<Triple>(400);
		wordsToAdd=new HashMap<String,Word>();
		newWordsToAdd=new HashMap<String,Word>();
		wordsInDocToMod=new HashMap<String,Word>();
		wordsToRemove=new HashMap<String,Word>();
		
	}
	
	
	
	public static void setAllDocsTagged(boolean allDocsTagged){
		ConsolidationStation.allDocsTagged = allDocsTagged;
	}
	
	/**
	 * Adds the features present to each word in the taggedSentence
	 * @param taggedSent the tagged Sentence with the Word List to update.
	 * @return the taggedSentence passed in.
	 */
	public static TaggedSentence featurePacker(TaggedSentence taggedSent){
		for(Word word:taggedSent.wordsInSentence){
			setWordFeatures(word);
		}
		setSentenceFeatures(taggedSent);
		return taggedSent;
	}
	
	
	
	/**
	 * Adds Reference objects to each Word objects' SparseReferences indicating which features were found in each word, and how many times that feature was found
	 * @param word
	 */
	public static void setWordFeatures(Word word){
		String wordString=word.word;
		int strSize=wordString.length(), tempNumber;
		int attribLen=DataAnalyzer.lengthTopAttributes;
		//for (Attribute attrib:attribs){
		for(int i=0;i<attribLen;i++){
			String stringInBrace=DataAnalyzer.topAttributes[i].getStringInBraces();
			int toAddLength=stringInBrace.length();
			if(toAddLength==0){
				//Logger.logln(NAME+"THIS IS BAD",Logger.LogOut.STDERR);
			}
			else if(toAddLength<=strSize){//checks for a possible match
				tempNumber=0;
				for(int j=0;j<strSize-toAddLength;j++){
					if(wordString.substring(j, j+toAddLength).equals(stringInBrace)){
						tempNumber++;
					}
				}
				if(tempNumber>0){
					//add the feature to the word and have it appearing tempNumber times.
					//Logger.logln(NAME+"AddNewReference from ConsolStation.featurePacker");
					//Logger.logln(NAME+"Value i: "+i+" Value indexOf Attrib: "+DataAnalyzer.topAttributes[i].getIndexNumber()+" Attribute: "+DataAnalyzer.topAttributes[i].getFullName()+" the word: "+wordString);
					word.wordLevelFeaturesFound.addNewReference(i, tempNumber);
					//Logger.logln(NAME+"Added a feature: "+word.wordLevelFeaturesFound.toString());
				}
			}
		}
	}
	
	
	/**
	 * Same as {@link #setWordFeatures(Word word)}, except on the sentence level. 
	 * 
	 * NOTE: Should be called AFTER {@link #setWordFeatures(Word word)}
	 *  
	 * @param word
	 */
	public static void setSentenceFeatures(TaggedSentence sent){
		// TODO -- We already found the 'word' level features, and they are stored differently/independently... so, we start with word bigrams, and move up (trigrams, possibly POS bi/trigrams, and punctutation)
		String sentString = sent.untagged;
		int strSize = sentString.length(); 
		int sibSize;
		int tempNumber;
		int attribLen = DataAnalyzer.lengthTopAttributes;
		int numFound = 0;
		GramMatcher gm = new GramMatcher();
		for(int i=0;i<attribLen;i++){
			
			String stringInBrace=DataAnalyzer.topAttributes[i].getStringInBraces();
			Attribute curAttribute = DataAnalyzer.topAttributes[i];
			
			if(!stringInBrace.equals("") && stringInBrace.charAt(0) == '(' && !curAttribute.isPartofSpeech()) {// if the string in braces begins with an opening parenthesis, it is a word or POS bigram or trigram feature
				//System.out.printf("Finding feature number '%d':  '%s' in sentence, '%s'\n",i,stringInBrace,sent.getUntagged());
				// Check the number of open parentheses -- if (1), continue, if (2), it's a bigram, if (3) it's a trigram
				int numOpenParens =0;
				sibSize = stringInBrace.length();
				for(int j = 0; j < sibSize; j++){
					if(stringInBrace.charAt(j) == '(')
						numOpenParens += 1;
				}
				if(numOpenParens <= 1)
					continue;// we want bi and tri grams. not unigrams that happen to have a parenthesis
				else if(numOpenParens == 2){
					// do bigram stuff
					numFound = gm.getOccurrencesOfBigram(stringInBrace, sentString);
				}
				else if(numOpenParens == 3){
					// do trigram stuff
					numFound = gm.getOccurrencesOfTrigram(stringInBrace,sentString);
					
				}
			
				if(numFound > 0){
					//add a reference to the feature and the number of times it appeared to the sentence 
					//Logger.logln(NAME+"AddNewReference from ConsolStation.featurePacker");
					//Logger.logln(NAME+"Value i: "+i+" Value indexOf Attrib: "+DataAnalyzer.topAttributes[i].getIndexNumber()+" Attribute: "+DataAnalyzer.topAttributes[i].getFullName()+" the word: "+wordString);
					sent.sentenceLevelFeaturesFound.addNewReference(i, numFound);
					//Logger.logln(NAME+"Added a feature: "+word.wordLevelFeaturesFound.toString());
				}
			}
		}
		//System.out.println("number of sentence level features found: "+sent.sentenceLevelFeaturesFound.length());
	}
	
	
	/**
	 * This is the test method to see if a bunch of translated sentences may be able to be pieced together to produce an anonymous document
	 * It replaces the original sentences with the best replacement out of the replacements given for each sentence (version 1)
	 * It outputs files containing the new document text comprised of the first, second, third, etc. best replacements. This exits Anonymouth when finished. (version 2)
	 * @param attributable The TaggedDocument (which must already be fully tagged, with all SparseReferences added [i.e. all features found])
	 * @param newSentences Basically a 2D array of sentences. Each inner ArrayList contains all potential sentence substitutions for the sentence number that corresponds to that ArrayList's index in the outer ArrayList.  
	 */
	public static void anonymizeDocument(TaggedDocument attributable, ArrayList<ArrayList<String>> newSentences, boolean doVersionTwo){
		int sentenceNumber, numOptions;
		int numSents = newSentences.size();
		String tempSent;
		ArrayList<ArrayList<TaggedSentence>> replacements = new ArrayList<ArrayList<TaggedSentence>>(numSents); 
		ArrayList<double[][]> allAnonymityIndices = new ArrayList<double[][]>(numSents);
		int optionNumber =0;
		// for each sentence in the document
		System.out.printf("Creating anonymous document...\n");
		for(sentenceNumber = 0; sentenceNumber < numSents; sentenceNumber++){
			Iterator<String> sentenceChoices = newSentences.get(sentenceNumber).iterator();
			numOptions = 12; 
			ArrayList<TaggedSentence> taggedOptions = new ArrayList<TaggedSentence>(numOptions);
			double[][] anonymityIndices = new double[numOptions][2];
			optionNumber = 0;
			// create a TaggedSentence for each alternative sentence for that sentence, calculate the Anonymity Index, and save both in parallel ArrayLists
			System.out.printf("For sentence number '%d':\n",sentenceNumber);
			while(sentenceChoices.hasNext()){
				tempSent = sentenceChoices.next();
				if(tempSent.trim().equals("")){
					anonymityIndices[optionNumber][0] = -999999; // very small number
					anonymityIndices[optionNumber][1] = optionNumber; 
					optionNumber += 1;
					continue;
				}
				System.out.println("The next sentence is: "+tempSent);
				TaggedSentence ts = new TaggedSentence(tempSent);
				ts.tagAndGetFeatures();
				System.out.println("Features tagged and gotten...");
				anonymityIndices[optionNumber][0] = ts.getSentenceAnonymityIndex();
				anonymityIndices[optionNumber][1] = optionNumber; // need a way to keep track of the indices of the actual replacement sentences
				taggedOptions.add(optionNumber,ts);
				System.out.printf("option '%d' (with AI '%f'): %s\n",optionNumber,anonymityIndices[optionNumber][0],ts.getUntagged(false));
				optionNumber += 1;
			}
			allAnonymityIndices.add(sentenceNumber,anonymityIndices); 
			replacements.add(sentenceNumber,taggedOptions);
		}
		// Now we know the Anonymity Index of each possible substitute for each sentence, so we find the sentence with the highest Anonymity
		for(sentenceNumber = 0; sentenceNumber < numSents; sentenceNumber++){
			double[][] thisSentenceOptionsAI = allAnonymityIndices.get(sentenceNumber);
			// sort the 2D array of doubles by the Anonymity Index, which is index '0'. Index '1' contains the original index (which corresponds to the actual sentence's index in its own array (the inner arrays in 'replacements')
			Arrays.sort(thisSentenceOptionsAI, new Comparator<double[]>(){
				public int compare(final double[] first, final double[] second){
					return ((-1)*((Double)first[0]).compareTo(((Double)second[0]))); // multiplying by -1 will sort from greatest to least, which saves work.
				}
			});
			// todo: for these tests, print out the AI ratings, along with each sentence.
			//bestReplacementIndices[sentenceNumber] = bestIndex; 
			allAnonymityIndices.set(sentenceNumber,thisSentenceOptionsAI);
		}
		System.out.printf("Replacement sentences analyzed...\n");
		// todo for the time being, just to test this out, I'm going to replace all sentences in the TaggedDocument with each sentences highest ranked replacement
		// BUT, this should do something more intelligent. 
		int actualSentenceIndex;
		if(!doVersionTwo){
			double ayEye;
			for(sentenceNumber = 0; sentenceNumber < numSents; sentenceNumber++){
				String untaggedOrig = attributable.taggedSentences.get(sentenceNumber).getUntagged(false);
				numOptions = replacements.get(sentenceNumber).size();
				if(numOptions < 1)
					continue;
				ayEye = allAnonymityIndices.get(sentenceNumber)[0][0]; // Since this was sorted in reverse order, index '0' contains the highest AI, and index '1' tells us the index of the corresponding sentence in its array.
				actualSentenceIndex = (int) allAnonymityIndices.get(sentenceNumber)[0][1]; // Since this was sorted in reverse order, index '0' contains the highest AI, and index '1' tells us the index of the corresponding sentence in its array.
				String untaggedNew = replacements.get(sentenceNumber).get(actualSentenceIndex).getUntagged(false);
				// test code to make sure that everything is working as planned
				System.out.printf("The original sentence: %s\n",untaggedOrig);	
				System.out.printf("Will be replaced with (AI of '%f'); %s\n",ayEye,untaggedNew);
				// now, we replace the original sentence with the old sentence.
				attributable.taggedSentences.set(sentenceNumber,replacements.get(sentenceNumber).get(actualSentenceIndex));
			}
			System.out.printf("Document anonymized.\n");
		}
		else{
			double[] ayEyeRay;
			int numTranslations = 12;
			String replacementSent;
			String replacementDoc;
			int qualityRank; // as ranked / determined by Anonymity Index
			String dirName = "translation_test_results/"+ThePresident.sessionName;
			File authorsDir = new File(dirName);
			if(!authorsDir.exists()){
				if(!authorsDir.mkdir()){
					Logger.log("Error! Failed creating directory to put replacement documents in.",LogOut.STDERR);
					System.exit(0);
				}
			}
			File anonIndexRecord = new File(dirName+"/Anonymity_Index_Records.txt");
			BufferedWriter anonIndexWriter = null;
			try {
				anonIndexWriter = new BufferedWriter(new FileWriter(anonIndexRecord));
				anonIndexWriter.write("Anonymity Index Record for test: "+ThePresident.sessionName+"\n");
				anonIndexWriter.write("\nNote: Each sentence in the original document had 12 possible alternatives (translations). Twelve new documents were assembled. \nEach document is comprised entirely of a single tier of sentences: so, of all possible choices for each sentence, the ones that had the highest anonymity index comprise the lowest ranked document, the ones with the second highest comprise the second lowest ranked document, etc.\n\n"+ThePresident.sessionName+"\n");
				anonIndexWriter.flush();
				for(qualityRank = 0; qualityRank < numTranslations; qualityRank ++){ // because these were sorted in reverse order, '0' is the highest quality.
					ayEyeRay = new double[numSents];
					replacementDoc = "";
					for(sentenceNumber = 0; sentenceNumber < numSents; sentenceNumber++){
						numOptions = replacements.get(sentenceNumber).size();
						if(numOptions < 1){
							replacementSent = "["+attributable.taggedSentences.get(sentenceNumber).getUntagged(false) +"]";
							ayEyeRay[sentenceNumber] = attributable.taggedSentences.get(sentenceNumber).getSentenceAnonymityIndex();
						}
						else{
							//System.out.println("sentenceNumber: "+sentenceNumber+" numOptions: "+numOptions+"+ qualityRank = "+qualityRank);
							ayEyeRay[sentenceNumber] = allAnonymityIndices.get(sentenceNumber)[qualityRank][0]; // Since this was sorted in reverse order, index '0' contains the highest AI, and index '1' tells us the index of the corresponding sentence in its array.
							actualSentenceIndex = (int) allAnonymityIndices.get(sentenceNumber)[qualityRank][1]; // Since this was sorted in reverse order, index '0' contains the highest AI, and index '1' tells us the index of the corresponding sentence in its array.
							replacementSent = replacements.get(sentenceNumber).get(actualSentenceIndex).getUntagged(false);
						}
						replacementDoc += replacementSent;
					}
					int indexNum = 0;
					int numSentsMinusOne = numSents - 1;
					anonIndexWriter.write("For document with rank '"+qualityRank+"', the anoymity indices were (left => sentence '0', right => last sentence):\n[");
					for(indexNum = 0; indexNum<numSents; indexNum++){
						if(indexNum != numSentsMinusOne)
							anonIndexWriter.write(" "+ayEyeRay[indexNum]+",");
						else
							anonIndexWriter.write(" "+ayEyeRay[indexNum]+"]\n\n");
					}
					anonIndexWriter.flush();
					File thisReplacementDoc = new File(dirName+"/"+ThePresident.sessionName+"_AnonIndex_"+qualityRank+".txt");
					try {
						BufferedWriter buffWrite = new BufferedWriter(new FileWriter(thisReplacementDoc));
						buffWrite.write(replacementDoc+"\n");
						buffWrite.close();
					} catch (IOException e) {
						Logger.log("Error writing anonymized document to file!",LogOut.STDERR);
						e.printStackTrace();
					}
				}
				anonIndexWriter.close();
			} catch (IOException e1) {
				Logger.log("Error! Failed creating directory to put replacement documents in.",LogOut.STDERR);
				System.exit(0);
				e1.printStackTrace();
			}
			
		}
		
		//return anonymousDoc;
	}
	
	
	
	/**
	 * Goes through all Words in all TaggedSentences in all TaggedDocuments, sorts them from least to greatest in terms of Anonymity Index, and returns either the lowest ranked or
	 * highest ranked percent as strings.
	 * NOTE:
	 * * percentToReturn, the percent of highest or lowest ranked words (String) to return, should be a number between 0 and 1.
	 * * if findTopToRemove is false, the highest ranked Words will be returned (as Strings) (these would then be the most important words to ADD to the documentToAnonymize)
	 * * if findTopToRemove is true, the lowest ranked Words will be returned (as String) (these would then be the most important words to REMOVE from the documentToAnonymize)
	 * @param docsToConsider the TaggedDocuments to extract Words from
	 * @param findTopToRemove true to find the top words to remove, false to find the top words to add
	 * @param percentToReturn the percent of words found to return (should probably be MUCH smaller if finding top words to add, because this will look at all otherSampleDocuments
	 * @return
	 */
	public static ArrayList<String> getPriorityWords(ArrayList<TaggedDocument> docsToConsider, boolean findTopToRemove, double percentToReturn){
		int totalWords = 0;
		ArrayList<Word> words = new ArrayList<Word>(totalWords);
			
		totalWords += DriverEditor.taggedDoc.getWordCount();
		words.addAll(DriverEditor.taggedDoc.getWords());
		
		int numToReturn = (int)(totalWords*percentToReturn);
		ArrayList<String> toReturn = new ArrayList<String>(numToReturn);
		words = removeDuplicateWords(words);
		
		/**
		 * NOTE: We MUST be using getAnonymity() for both Word's compareTo method and for retrieving the word's anonymity index.
		 * This is because the old method call to getAnonymityIndex() did not allow the index to be negative. This wouldn't work
		 * here since we use the more negative the index is to rate how important it is a word be removed. As a result, we are now
		 * using a slightly modified version of getAnonymityIndex() called getAnonymity() so that we get the negative range back.
		 */
		Collections.sort(words);// sort the words in INCREASING anonymityIndex

		int mergedNumWords = words.size();
		if (mergedNumWords <= numToReturn) {
			Logger.logln("(ConsolidationStation) - The number of priority words to return is greater than the number of words available. Only returning what is available");
			numToReturn = mergedNumWords;
		}
		
		
		Word tempWord;
		if(findTopToRemove){ // then start from index 0, and go up to index (numToReturn-1) words (inclusive)]
//			System.out.println("Finding top to remove");
			for(int i = 0; i < numToReturn; i++) {
//				System.out.println(words.get(i).word+" "+words.get(i).getAnonymity()); 	
				if((tempWord=words.get(i)).getAnonymity() <= 0)
					toReturn.add(tempWord.word);//+" ("+tempWord.getAnonymity()+")");
				else 
					break;
			}
		}
		else{ // start at the END of the list, and go down to (END-numToReturn) (inclusive)
//			System.out.println("Finding top to add");
			int startIndex = mergedNumWords - 1;
			int stopIndex = startIndex - numToReturn;
			for(int i = startIndex; i> stopIndex; i--) {
//				System.out.println(words.get(i).word+" "+words.get(i).getAnonymity());
				if((tempWord=words.get(i)).getAnonymity()>0)
					toReturn.add(tempWord.word);//+" ("+tempWord.getAnonymity()+")");
				else 
					break;
			}	
		}
		return toReturn;
	}
	
	
	public static ArrayList<Word> removeDuplicateWords(ArrayList<Word> unMerged){
		HashMap<String,Word> mergingMap = new HashMap<String,Word>((unMerged.size()));//Guessing there will be at least an average of 3 duplicate words per word -> 1/3 of the size is needed
		for(Word w: unMerged){
			if(mergingMap.containsKey(w.word) == true){
				//Word temp = mergingMap.get(w.word);
				//temp.mergeWords(w);
				//mergingMap.put(w.word,temp);
				if(w.equals(mergingMap.get(w.word))){
					//check is sparse ref the same
					if(!w.wordLevelFeaturesFound.equals(mergingMap.get(w.word).wordLevelFeaturesFound)){
						Logger.logln("(ConsolidationStation) - The wordLevelFeaturesFound in the words are not equal.",Logger.LogOut.STDERR);
					}
				}
				else{
					Logger.logln("(ConsolidationStation) - Problem in mergeWords--Words objects not equal",Logger.LogOut.STDERR);
				}
				
			}
			else{
				mergingMap.put(w.word,new Word(w));
			}
		}
		Set<String> mergedWordKeys = mergingMap.keySet();
		ArrayList<Word> mergedWords = new ArrayList<Word>(mergedWordKeys.size());
		for(String s: mergedWordKeys){
			mergedWords.add(mergingMap.get(s));
		}
		return mergedWords;
	}
	
	
	public static Word getWordFromString(String str){
		Word newWord=new Word(str);
		for (int i=0;i<toAdd.size();i++){//toaddList loop
			Logger.logln("(ConsolidationStation) - TOADD: "+toAdd.get(i).getStringInBraces());
			int toAddLength=toAdd.get(i).getStringInBraces().length();
			if(toAddLength<=str.length()){//checks if it can be a possible match
				int tempNumber=0;
				double featureInfoGain=toAdd.get(i).getInfoGain();
				double featurePercentChange = toAdd.get(i).getInfoGain();
				for(int j=0;j<str.length()-toAddLength;j++){//loops through word to check if/howManyTimes the stringInBraces is found in the word.
					if(str.substring(j, j+toAddLength).equals((String)toAdd.get(i).stringInBraces)){
						tempNumber++;
					}
				}
				//newWord.adjustVals(tempNumber, featureInfoGain,featurePercentChange);
			}
		}
		for (int i=0;i<toRemove.size();i++){//toaddList loop
			Logger.logln("(ConsolidationStation) - TOREMOVE: "+toRemove.get(i).getStringInBraces());
			int toAddLength=toRemove.get(i).getStringInBraces().length();
			if(toAddLength<=str.length()){//checks if it can be a possible match
				int tempNumber=0;
				double featureInfoGain=toRemove.get(i).getInfoGain();
				double featurePercentChange = toRemove.get(i).getInfoGain();
				for(int j=0;j<str.length()-toAddLength;j++){//loops through word to check if/howManyTimes the stringInBraces is found in the word.
					if(str.substring(j, j+toAddLength).equals((String)toRemove.get(i).stringInBraces)){
						tempNumber++;
					}
				}
				//newWord.adjustVals(tempNumber, featureInfoGain, featurePercentChange);//respresents a word to remove, so it should be negative
			}
		}
	//	Logger.logln(NAME+"NEW WORD"+newWord.toString());
		return newWord;
	}
	


	
}
