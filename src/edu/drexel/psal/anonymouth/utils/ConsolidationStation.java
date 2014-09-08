package edu.drexel.psal.anonymouth.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.jgaap.generics.Document;
import edu.drexel.psal.ANONConstants;
import edu.drexel.psal.anonymouth.engine.Attribute;
import edu.drexel.psal.anonymouth.engine.DataAnalyzer;
import edu.drexel.psal.anonymouth.engine.InstanceConstructor;
import edu.drexel.psal.anonymouth.gooie.GUIMain;
import edu.drexel.psal.anonymouth.gooie.ThePresident;
import edu.drexel.psal.anonymouth.helpers.ErrorHandler;
import edu.drexel.psal.jstylo.generics.CumulativeFeatureDriver;
import edu.drexel.psal.jstylo.generics.Logger;
import edu.drexel.psal.jstylo.generics.ProblemSet;
import edu.drexel.psal.jstylo.generics.Logger.LogOut;

/**
 * 
 * @author Andrew W.E. McDonald
 * @author Marc Barrowclift
 * @author Joe Muoio
 *
 */
public class ConsolidationStation {
	
	public static final String NAME = "( ConsolidationStation ) - ";
	static HashMap<String,ArrayList<TreeData>> parsed;
	static ArrayList<Triple> toAdd=new ArrayList<Triple>(400);
	static ArrayList<Triple> toRemove=new ArrayList<Triple>(400);
	public static ArrayList<TaggedDocument> otherSampleTaggedDocs;//initialized in backendInterfaces.
	public static ArrayList<TaggedDocument> authorSampleTaggedDocs;
	public static ArrayList<TaggedDocument> toModifyTaggedDocs;//init in editor Tab Driver
	private static boolean firstTime;
	private static double oldStartingValue = 0;
	private static ArrayList<Word> oldWords = new ArrayList<Word>();
	private static ArrayList<Double> oldDat = new ArrayList<Double>();
	private static FunctionWords functionWords = new FunctionWords();
	
	/**
	 * constructor for ConsolidationStation. Depends on target values, and should not be called until they have been selected.
	 * @param attribs
	 * @param parsed
	 */
	public ConsolidationStation() {
		toAdd = new ArrayList<Triple>(400);
		toRemove = new ArrayList<Triple>(400);		
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
		String wordString=word.word.toLowerCase();
		int attribLen=DataAnalyzer.lengthTopAttributes;
		for(int i=0;i<attribLen;i++){
				String stringInBrace=DataAnalyzer.topAttributes[i].getStringInBraces();
				String nameWithoutStringInBraces = DataAnalyzer.topAttributes[i].getTrimmedAttrib();
				if(wordString.contains(stringInBrace) && !stringInBrace.equals("")){//checks for a possible match
					if (nameWithoutStringInBraces.contains("Words"))
						if (!wordString.equals(stringInBrace))
							continue;
					int occurence = 0;
					int location = 0;
					while (location < wordString.length()) {
						location = wordString.indexOf(stringInBrace, location);
						if (location == -1)
							break;
						else {
							occurence++;
							location+=stringInBrace.length();
						}
					}
					if(occurence > 0)
						word.wordLevelFeaturesFound.addNewReference(i, occurence);
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
		int sibSize;
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
				System.out.printf("option '%d' (with AI '%f'): %s\n",optionNumber,anonymityIndices[optionNumber][0],ts.getUntagged());
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
				String untaggedOrig = attributable.taggedSentences.get(sentenceNumber).getUntagged();
				numOptions = replacements.get(sentenceNumber).size();
				if(numOptions < 1)
					continue;
				ayEye = allAnonymityIndices.get(sentenceNumber)[0][0]; // Since this was sorted in reverse order, index '0' contains the highest AI, and index '1' tells us the index of the corresponding sentence in its array.
				actualSentenceIndex = (int) allAnonymityIndices.get(sentenceNumber)[0][1]; // Since this was sorted in reverse order, index '0' contains the highest AI, and index '1' tells us the index of the corresponding sentence in its array.
				String untaggedNew = replacements.get(sentenceNumber).get(actualSentenceIndex).getUntagged();
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
							replacementSent = "["+attributable.taggedSentences.get(sentenceNumber).getUntagged() +"]";
							ayEyeRay[sentenceNumber] = attributable.taggedSentences.get(sentenceNumber).getSentenceAnonymityIndex();
						}
						else{
							//System.out.println("sentenceNumber: "+sentenceNumber+" numOptions: "+numOptions+"+ qualityRank = "+qualityRank);
							ayEyeRay[sentenceNumber] = allAnonymityIndices.get(sentenceNumber)[qualityRank][0]; // Since this was sorted in reverse order, index '0' contains the highest AI, and index '1' tells us the index of the corresponding sentence in its array.
							actualSentenceIndex = (int) allAnonymityIndices.get(sentenceNumber)[qualityRank][1]; // Since this was sorted in reverse order, index '0' contains the highest AI, and index '1' tells us the index of the corresponding sentence in its array.
							replacementSent = replacements.get(sentenceNumber).get(actualSentenceIndex).getUntagged();
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
	 * Obtain words-to-remove and words-to-add list from the current docToAnonymize
	 * @return list of words to remove/add
	 */
	
	public static HashMap<String,ArrayList<String[]>> getWordToRemoveAndWordToAdd () {
		TaggedDocument toModifyDoc = toModifyTaggedDocs.get(0);
		HashMap<String, ArrayList<String[]>> toReturn = new HashMap<String,ArrayList<String[]>>(2);
		ArrayList<Word> wordsSuggestion;
		ArrayList<Double> newDat = new ArrayList<Double>();
		Double newStartingValue = GUIMain.inst.anonymityBar.getAnonymityBarValue();
		
		if (oldStartingValue != newStartingValue) {// an attempt to reduce work done. If nothing change, do nothing
			// get word's list. Sometimes (yeah, sometimes) the list return empty so we do it this way
			ArrayList<Word> wordList;
			do {
				System.out.println("ConsolidationStation 320: wordlist is emplty");
				wordList = toModifyDoc.getWords();
				System.out.println("The wordlist is: " + wordList);
			} while (wordList.isEmpty());
			wordsSuggestion = getFilteredWordList(wordList);
		
			// what we do here is create a "ghost" document by eliminating a word in author's document, then evaluate the new document 
			//(most parts are copied from DocumentMagician with some modifications to reduce runtime to acceptable level without making the program broken down)
			String docToUse = toModifyDoc.getUntaggedDocument();
			List<Document> toModifySet = new LinkedList<Document>();
			InstanceConstructor instance = new InstanceConstructor(false,GUIMain.inst.ppAdvancedDriver.cfd,false);

			firstTime = true;
			int k = 0;
			while (k < wordsSuggestion.size()) {
				String doc = docToUse;
				doc = removeWord(doc,wordsSuggestion.get(k).getUntagged());
			
				toModifySet.clear();
				String pathToTempModdedDoc = ANONConstants.DOC_MAGICIAN_WRITE_DIR + "WTR.txt";
				try {
					File tempModdedDoc;
					tempModdedDoc = new File(pathToTempModdedDoc);
					tempModdedDoc.deleteOnExit();
					FileWriter writer = new FileWriter(tempModdedDoc,false);
					writer.write(doc);
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
					try {
						File tempModdedDoc;
						tempModdedDoc = new File(pathToTempModdedDoc);
						tempModdedDoc.deleteOnExit();
						FileWriter writer = new FileWriter(tempModdedDoc,false);
						writer.write(doc);
						writer.close();
					} catch (IOException ex) {}
				}
				Document newModdedDoc = new Document(pathToTempModdedDoc,"","WTR");
				toModifySet.add(newModdedDoc);
				try {
					toModifySet.get(0).load();
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				try {
					if (firstTime) {
						ProblemSet ps = new ProblemSet();
						for (Document d : GUIMain.inst.documentProcessor.documentMagician.getTrainSet()) {
							ps.addTrainDoc(d.getAuthor(), d);
						}
						for (Document d : toModifySet) {
							d.setAuthor(ANONConstants.DUMMY_NAME);
							ps.addTestDoc(d.getAuthor(), d);
						}
						if (instance.jstylo == null){
							instance.buildJStylo(ps);
						}
						//CumulativeFeatureDriver cfd = instance.jstylo.getUnderlyingInstancesBuilder().getCFD();
						//instance.jstylo.getUnderlyingInstancesBuilder().reset();
						//instance.jstylo.getUnderlyingInstancesBuilder().setProblemSet(ps);
						//instance.jstylo.getUnderlyingInstancesBuilder().setCumulativeFeatureDriver(cfd);
						//instance.jstylo.prepareInstances();
						firstTime = false;
					} else {
						ProblemSet ps = instance.jstylo.getUnderlyingInstancesBuilder().getProblemSet();
						
						//reset test data
						List<String> toRemove = new ArrayList<String>();
						for (String author :  ps.getTestAuthorMap().keySet()){
							toRemove.add(author);
						}
						for (String s : toRemove){
							ps.removeTestAuthor(s);
						}
						
						//add in new test data
						for (Document d : toModifySet) {
							d.setAuthor(ANONConstants.DUMMY_NAME);
							ps.addTestDoc(d.getAuthor(), d);
						}
						//CumulativeFeatureDriver cfd = instance.jstylo.getUnderlyingInstancesBuilder().getCFD();
						//instance.jstylo.getUnderlyingInstancesBuilder().reset();
						instance.jstylo.getUnderlyingInstancesBuilder().setProblemSet(ps);
						//instance.jstylo.getUnderlyingInstancesBuilder().setCumulativeFeatureDriver(cfd);
						instance.jstylo.getUnderlyingInstancesBuilder().createTestInstancesThreaded();
					}
				} catch(Exception e) {
					System.out.println("!!!!!!ConsolidationStation 374 - in the catch bolck after try and perptest....");
					e.printStackTrace();
					ErrorHandler.StanfordPOSError();
				}
				double currValue = GUIMain.inst.documentProcessor.documentMagician.getAuthorAnonimity(instance.jstylo.getTestInstances())[0];
				if (currValue == newStartingValue)
					wordsSuggestion.remove(k);
				else {
					newDat.add(currValue);
					k++;
				}
			}
			newStartingValue = GUIMain.inst.anonymityBar.getAnonymityBarValue();
			oldStartingValue = newStartingValue;
			oldWords = wordsSuggestion;
			oldDat = newDat;
		}
		else {
			wordsSuggestion = oldWords;
			newDat = oldDat;
		}
		
		//If there are no valid words (or we're using a feature set without word level features)
		//just return empty lists for words to add/remove
		if (wordsSuggestion.isEmpty() || newDat.isEmpty()) {
			toReturn.put("wordsToAdd", new ArrayList<String[]>());
			toReturn.put("wordsToRemove", new ArrayList<String[]>());
			return toReturn;
		}

		//sort values
		double[][] toSort = new double[newDat.size()][2];
		for (int i = 0; i < newDat.size(); i++) {
			toSort[i][0] = newDat.get(i);
			toSort[i][1] = i;
		}
	    Arrays.sort(toSort, new Comparator<double[]>() {
	        public int compare(double[] a, double[] b) {
	            return Double.compare(a[0], b[0]);
	        }
	    });

	    //get Words-To-Remove and Words-To-Add lists
	    ArrayList<String[]> returnWordsToRemove = new ArrayList<String[]>();
	    int h = 0;
	    while (h < newDat.size() && newDat.get((int)toSort[h][1]) < newStartingValue) {
	    	String word = wordsSuggestion.get((int)toSort[h][1]).getUntagged();
	    	int occurances = getWordOccurances(toModifyDoc.getUntaggedDocument(), word);
			if (occurances != 0)
				returnWordsToRemove.add(new String[] {word, Integer.toString(occurances)});
			h++;
	    }
		toReturn.put("wordsToRemove", returnWordsToRemove);
	    ArrayList<String[]> returnWordsToAdd = new ArrayList<String[]>();
	    h = newDat.size() - 1;
	    while (h >= 0 && newDat.get((int)toSort[h][1]) > newStartingValue) {
	    	String word = wordsSuggestion.get((int)toSort[h][1]).getUntagged();
	    	int occurances = getWordOccurances(toModifyDoc.getUntaggedDocument(), word);
			if (occurances != 0)
			    returnWordsToAdd.add(new String[] {word});
			h--;
	    }
		toReturn.put("wordsToAdd", returnWordsToAdd);
		
		return toReturn;
	}

	/**
	 * Returns the number of occurrences of a given word
	 * @param text - The text you want to find the word in
	 * @param word - The word you want to find
	 * @return result - The number of occurrences
	 */
	public static int getWordOccurances(String text, String word) {
		
		return findWord(text, word).size();
	}
	
	/**
	 * Remove given word based on its location extracted from the text by findWord(String, String). 
	 * Use this to avoid words being erased wrongly by default remove method(predict - predicting/predictor, etc.)
	 * @param text
	 * @param word
	 * @return
	 */
	public static String removeWord(String text, String word) {
		int wordLength = word.length();
		ArrayList<Integer> location = findWord (text, word);
		
		for (int k = 0; k < location.size(); k++) {
			int pos = location.get(k) - wordLength * k; // since we are modifying the text, positions will be changed as well
			String firstPart = text.substring(0, pos);
			String secondPart = text.substring(pos + wordLength, text.length());
			text = firstPart + secondPart;
		}
		
		return text;
	}
	
	/**
	 * Our customized algorithm to find a word. It avoids miscounting words (predict - predicting/predictor, etc.)
	 * @param text
	 * @param word
	 * @return list of word's locations
	 */
	public static ArrayList<Integer> findWord (String text, String word) {
		ArrayList<Integer> result = new ArrayList<Integer>();
		int location = 0;
		int wordLength = word.length();
		int textLength = text.length();
		word = word.trim();
		word = word.toLowerCase();
		text = text.toLowerCase();
	
		while (location != -1 && location < textLength) {
			location = text.indexOf(word, location);
			if (location == -1) {
				break;
			} else if (location == 0) { // if word's location is at the very beginning of the text
					if (text.equals(word)) { // if word is the text, location = 0 but there's no before or after char
						result.add(location);
						break;
					} else {
						char after = text.charAt(wordLength);
						if (!Character.isLetter(after) && after !='-') { 
								result.add(location);
								location = wordLength;
							}
						else location = wordLength;
					}
				} else if (location >= (textLength - wordLength)) { // if word's location is at the very end of the text
					char before = text.charAt(location - 1);
					if (!Character.isLetter(before) && before !='-') {
						result.add(location);
						location += wordLength;
					} 
					else location += wordLength;
				} else if ((location + wordLength) < textLength) { // if word's location is found at normal places
					char before = text.charAt(location - 1);
					char after = text.charAt(location + wordLength);
					if (!Character.isLetter(before) && !Character.isLetter(after) && after !='-' && before !='-') {
						result.add(location);
						location += wordLength;
					}
					else location += wordLength;
				}
		}
	
	return result;
	}
	
	public static ArrayList<Word> removeDuplicateWords(ArrayList<Word> unMerged){
		HashMap<String,Word> mergingMap = new HashMap<String,Word>((unMerged.size()));//Guessing there will be at least an average of 3 duplicate words per word -> 1/3 of the size is needed
		for(Word w: unMerged){
			Word wlc = new Word(w);
			wlc.setLowerCase(); //needed to catch upper case/lower case versions of the same word
			if(mergingMap.containsKey(wlc.word) == true) {
				if(wlc.equals(mergingMap.get(wlc.word))) {
					if(!wlc.wordLevelFeaturesFound.equals(mergingMap.get(wlc.word).wordLevelFeaturesFound)) //check is sparse ref the same
						Logger.logln("(ConsolidationStation) - The wordLevelFeaturesFound in the words are not equal.",Logger.LogOut.STDERR);
				}
				else
					Logger.logln("(ConsolidationStation) - Problem in mergeWords--Words objects not equal",Logger.LogOut.STDERR);
			}
			else
				mergingMap.put(wlc.word,new Word(wlc));
		}
		Set<String> mergedWordKeys = mergingMap.keySet();
		ArrayList<Word> mergedWords = new ArrayList<Word>(mergedWordKeys.size());
		for(String s: mergedWordKeys)
			mergedWords.add(mergingMap.get(s));
		return mergedWords;
	}
	
	/**
	 * Filter out words from docToAnonymize that don't satisfy certain requirements 
	 * @param wordsFromDoc
	 * @return
	 */
	public static ArrayList<Word> getFilteredWordList(ArrayList<Word> wordsFromDoc) {
		int newWordsSize = 0;
		ArrayList<Word> newWords = new ArrayList<Word>(newWordsSize);
		wordsFromDoc = removeDuplicateWords(wordsFromDoc);
		ArrayList<Word> tempWords = wordsFromDoc;
		
		//start searching for qualified words
		functionWords.run();
		for (Word w : tempWords) {
			if (includeWord(w))
				if (!w.wordLevelFeaturesFound.references.isEmpty()) { // not including words without features in question
					newWordsSize++;
					newWords.add(w);
				}
		}
		
		return newWords;
	}
	
	/**
	 * Helper function for getFilteredWordList
	 * Determines whether or not a word should be included in word suggestions
	 * @param w
	 * @return false if the word is a special character, function word, proper noun
	 *         true otherwise
	 */
	private static boolean includeWord(Word w) {
		if (w.getUntagged().matches("[\\W|\\d]*")) //Exclude non-word character sequences (e.g. punctuation) and numbers 
			return false;
		else if(functionWords.isWordInTrie(w.getUntagged())) //Exclude function words
			return false;
		else if (w.partOfSpeech.contains("NNP")) //Exclude proper nouns
			return false;
		else if (w.partOfSpeech.contains("NNPS"))  //Exclude plural proper nouns
			return false;
		else
			return true;
	}
}
