package edu.drexel.psal.anonymouth.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.nlp.trees.Tree;

/**
 *	processes parse trees  
 * @author Andrew W.E. McDonald
 *
 */
public class TreeProcessor {
	
	private final String NAME = "( "+this.getClass().getName()+" ) - ";
	protected HashMap<String,TreeData> processedTrees = new HashMap<String,TreeData>();
	protected static HashMap<String,TreeData> singleDocMap  = new HashMap<String,TreeData>(); // must be cleared after each document (NOT after each call to ProcessTree,
	 // each call to processTree takes care of one sentence in a document.)
	protected ArrayList<TreeData> sortedTreeData;
	protected ArrayList<HashMap<String,TreeData>> loadedTreeDataMaps = new ArrayList<HashMap<String,TreeData>>();
	protected HashMap<String,TreeData> mergedMap;
	protected ArrayList<HashMap<String,TreeData>> unmergedMaps;
	
	/**
	 * splits each parse tree up into all of its subtrees, strips the leaves (all 'words'), and saves them into an ArrayList of TreeData Objects. 
	 * One TreeData object per unique subtree.
	 * @param tree
	 * @param minDepth the minimum depth of a subtree to include (if '0', single words will be omitted as they will be a tree of depth '0').
	 * @param useSingleDocMap if true, will also save results into a second HashMap that is meant to contain everything from a single document. 
	 * The use of this would be to clear it after every document (each call to processTree takes care of one sentence, as the input Tree is a parse tree for a sentence). Prior 
	 * to clearing the tree, you would use the {@link ObjectIO} class to save that document's hashMap.
	 * 
	 * If false, the map will be ignored. 
	 * 
	 * NOTE: It is left up to the user to clear and save the HashMap.
	 */
	public void processTree(Tree tree,int minDepth, boolean useSingleDocMap){
		List<Tree> subTreeList = tree.subTreeList();
		//System.out.println("Sub tree list: \n");
		Iterator<Tree> treeIter = subTreeList.iterator();
		//ArrayList<String> cleanTrees = new ArrayList<String>(subTreeList.size());
		Tree temp;
		String tempLeaves;
		String leaves;
		String treeString;
		Pattern prePat = Pattern.compile("\\([A-Z[.,?!$-:;/&%#@~`'\"]]+\\s");
		Pattern postPat = Pattern.compile("\\)+\\s");
		Matcher matchPre;
		Matcher matchPost;
		boolean foundMatch;
		boolean hasNonLeaf = true;
		int start = 0;
		int tempStart = 0;
		String tempTreeString = "";
		String tempTempLeaves;
		String preString = "";
		while(treeIter.hasNext()){
			tempTreeString = "";
			temp = treeIter.next();
			int treeDepth = temp.depth();
			if(treeDepth <= minDepth)
				continue;
			tempTempLeaves = temp.getLeaves().toString();
			tempLeaves = tempTempLeaves.substring(1,tempTempLeaves.length()-1);
			leaves = tempLeaves.replaceAll(",","");
			//theLeaves.add(leaves);
			treeString = temp.toString()+" "; // add a space at the end for the postPat regex
			//System.out.println("THE TREE: "+treeString);
			matchPre = prePat.matcher(treeString);
			foundMatch = matchPre.find();
			while(foundMatch == true){
				start = matchPre.start();
				tempStart = matchPre.end();
				hasNonLeaf = true;
				while(hasNonLeaf){
					//System.out.println("char at next index: "+treeString.charAt(tempStart));
					if(treeString.charAt(tempStart) == '('){
						//System.out.println(treeString);
						System.out.println((treeString));
						if(matchPre.find(tempStart) == true);
							tempStart = matchPre.end();
					}
					else{
						hasNonLeaf = false;
						preString = treeString.substring(start);
						//System.out.println("preString: "+preString);
						matchPost = postPat.matcher(preString);
						matchPost.find(tempStart-start);
						tempTreeString += preString.substring(0,tempStart-start)+preString.substring(matchPost.start(),matchPost.end());
						//System.out.println("tempTreeString: "+tempTreeString);
					}
				}
				foundMatch = matchPre.find(tempStart);
			}
			
			if(tempTreeString.equals("") == false){
				TreeData td = new TreeData(tempTreeString);
				td.treeDepth = treeDepth;
				//System.out.println(tempTreeString+" ==> "+leaves);
				if(processedTrees.containsKey(tempTreeString))
					processedTrees.get(tempTreeString).addOccurrence(leaves);
				else{
					td.addOccurrence(leaves);
					processedTrees.put(tempTreeString,td);
				}
				if(useSingleDocMap == true){
					if(singleDocMap.containsKey(tempTreeString))
							singleDocMap.get(tempTreeString).addOccurrence(leaves);
					else
						singleDocMap.put(tempTreeString,td);
					
				}
				
			}
		}
		//System.out.println(processedTrees.toString());
	}
	
	/**
	 * Clears the ArrayList holding all loaded TreeData HashMaps
	 */
	public void clearLoadedTreeDataMaps(){
		loadedTreeDataMaps.clear();
	}
	
	/**
	 * loads the specified TreeData HashMap and places it into the loadedTreeDataMaps ArrayList
	 * @param id name of HashMap (should NOT include the '.ser' extension)
	 * @param dir directory (SHOULD include a trailing (forward) slash (e.x. "./grammar_data/" )
	 * @param printData if true, will print the loaded map to stdout
	 * @return true if successful, false if the read method returned null.
	 */
	public boolean loadTreeDataMap(String id, String dir, boolean printData){
		HashMap<String,TreeData> loaded = ObjectIO.readTreeDataMap(id,dir, printData);
		if(loaded == null)
			return false;
		else{
			loadedTreeDataMaps.add(loaded);
			return true;
		}
	}
	
	
	/**
	 * merges an ArrayList of hashed TreeData objects into one HashMap of TreeData objects, accounting for repeated values
	 * @param tdMaps ArrayList of TreeData HashMaps to combine into one
	 * @return a single HashMap with the input HashMaps combined to a single HashMap
	 */
	public HashMap<String,TreeData> mergeTreeDataLists(ArrayList<HashMap<String,TreeData>> tdMaps){
		Iterator<HashMap<String,TreeData>> mapIter = tdMaps.iterator();
		HashMap<String,TreeData> combinedMap = mapIter.next();
		Iterator<String> keyIter;
		while(mapIter.hasNext()){
			HashMap<String,TreeData> temp = mapIter.next();
			Set<String> tempKeys = temp.keySet();
			keyIter = tempKeys.iterator();
			while(keyIter.hasNext()){
				String thisKey = keyIter.next();
				if(combinedMap.containsKey(thisKey) == true)
					combinedMap.get(thisKey).addSentences(temp.get(thisKey).sentences);
				else
					combinedMap.put(thisKey,temp.get(thisKey));
			}
		}
		mergedMap = combinedMap;
		return combinedMap;
	}
	
	public static ArrayList<TreeData> treeDataReverseQuickSort(ArrayList<TreeData> td){
		if (td.size() <= 1)
				return td;
		int tdSize = td.size();
		int pivotIndex = (int)((double)tdSize/2);
		TreeData pivot = td.remove(pivotIndex);
		ArrayList<TreeData> lessThan = new ArrayList<TreeData>(tdSize);
		ArrayList<TreeData> greaterThan = new ArrayList<TreeData>(tdSize);
		for ( TreeData elem : td){
			if (elem.numberOfOccurrences > pivot.numberOfOccurrences)
				greaterThan.add(elem);
			else
				lessThan.add(elem);
		}
		return makeOneArrayList(treeDataReverseQuickSort(lessThan),pivot,treeDataReverseQuickSort(greaterThan));
	}
	
	public static ArrayList<TreeData> makeOneArrayList(ArrayList<TreeData> less, TreeData pivot, ArrayList<TreeData> greater){
		int totalSize = less.size() + greater.size() + 1;
		ArrayList<TreeData> concatted = new ArrayList<TreeData>(totalSize);
		Iterator<TreeData> tdIter = greater.iterator();
		while(tdIter.hasNext())
			concatted.add(tdIter.next());
		concatted.add(pivot);
		tdIter = less.iterator();
		while(tdIter.hasNext())
			concatted.add(tdIter.next());
		return concatted;
		
	}
	
	/**
	 * performs a (reversed) quicksort on a HashMap of TreeData outputting an ArrayList with index '0' containing the most frequently used sentence structure 
	 * @param processedTrees the map of TreeData to sort
	 * @return sorted ArrayList of TreeData objects
	 */
	public ArrayList<TreeData> sortTreeData(HashMap<String,TreeData> processedTrees){
	    Object[] values = processedTrees.values().toArray();
	    int numVals = values.length;
	    ArrayList<TreeData> td = new ArrayList<TreeData>(numVals);
	    for(int i = 0; i < numVals; i++)
	    		td.add((TreeData)values[i]);
		ArrayList<TreeData> sorted = treeDataReverseQuickSort(td);
		//System.out.println(sorted.toString().replaceAll("\\]\\], \\(","\\]\\]\n\\("));
		sortedTreeData = sorted;
		return sorted;
	}
	
	public static boolean writeTreeDataToCSV(ArrayList<TreeData> sortedTreeData, String TDName) throws IOException{
		FileWriter fw = new FileWriter(new File("./grammar_tests/"+TDName+"_grammar_data.csv"));
		BufferedWriter buff = new BufferedWriter(fw);
		Iterator<TreeData> tdIter = sortedTreeData.iterator();
		String plusAuthorName = "\"";
		String authorHeader = "";
		buff.write(authorHeader+"Tree Structure,Tree Depth,Number of Occurrences,Number Uniqe Occurrences,Associated Strings\n");
		while(tdIter.hasNext()){
			TreeData temp = tdIter.next();
			//System.out.println(temp.treeStructure.replaceAll(",", "\",\""));
			//in.nextLine();
			buff.write(plusAuthorName+temp.treeStructure+"\","+temp.treeDepth+","+temp.numberOfOccurrences+","+temp.numUnique+","+temp.getOrderedStringsAsString(true)+"\n");
		}
		buff.close();
		return true;
	}

}


		
		
