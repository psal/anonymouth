package edu.drexel.psal.anonymouth.helpers;

import java.util.HashMap;
import java.util.Iterator;

import edu.drexel.psal.anonymouth.utils.Word;

/**
 * 
 * @author Joe Muoio
 *
 */

public class ConsolidationStationHelper implements Runnable {

	private final String NAME = "( "+this.getClass().getName()+" ) - ";

	private HashMap<String,Word> finalHashMap;
	private HashMap<String,Word> substrHashMap;
	private HashMap<Integer,Word> intHashMap;
	private HashMap<String,Word> tagsHashMap;
	private boolean tags=false,intHash=false;
	
	public ConsolidationStationHelper(HashMap<String,Word> finalHashMap,HashMap<String,Word>substrHashMap){//, boolean tags){
		//this.tags=tags;
		this.finalHashMap=finalHashMap;
		this.substrHashMap=substrHashMap;
	}
	public ConsolidationStationHelper(HashMap<String,Word> finalHashMap,HashMap<Integer,Word>intHashMap,boolean b){
		intHash=true;
		this.finalHashMap=finalHashMap;
		this.intHashMap=intHashMap;
	}
	
	public void run(){
		if(intHash){
			scanHashMapLengths(finalHashMap,intHashMap);
		}
		else{
			scanHashMap(finalHashMap,substrHashMap);
		}
	}
	
	private void scanHashMap(HashMap<String,Word> finalHashMap,HashMap<String,Word>substrHashMap){
		Iterator iter1=substrHashMap.keySet().iterator();
		Iterator iter2=finalHashMap.keySet().iterator();
		while(iter1.hasNext()){
			String subStr=(String)iter1.next();
			while(iter2.hasNext()){
				String fullstring=(String)iter2.next();
				synchronized(this){
					//if(fullstring.matches(".*"+subStr+".*")){
					for(int i=0;i<fullstring.length();i++){
						if(fullstring.subSequence(i, i+subStr.length()).equals(subStr)){
							Word updatedWord=finalHashMap.get(fullstring);
							//updatedWord.adjustVals(substrHashMap.get(subStr).rank, substrHashMap.get(subStr).infoGainSum);
							finalHashMap.put(fullstring, updatedWord);
						}
					}
				}
			}
		}
	}
	private void scanHashMapLengths(HashMap<String,Word> finalHashMap,HashMap<Integer,Word>intHashMap){
		Iterator iter1=intHashMap.keySet().iterator();
		Iterator iter2=finalHashMap.keySet().iterator();
		while(iter1.hasNext()){
			Integer integer=(Integer)iter1.next();
			while(iter2.hasNext()){
				String fullstring=(String)iter2.next();
				synchronized(this){
					if(fullstring.length()==integer.intValue()){
						Word updatedWord=finalHashMap.get(fullstring);
						//updatedWord.adjustVals(intHashMap.get(integer).rank, intHashMap.get(integer).infoGainSum);
						finalHashMap.put(fullstring, updatedWord);
					}
				}
			}
		}
	}
	
}
