package edu.drexel.psal.anonymouth.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import edu.drexel.psal.ANONConstants;
import edu.drexel.psal.jstylo.generics.Logger;

public class MisspelledWords {

	@SuppressWarnings("unused")
	private final String NAME = "( "+this.getClass().getSimpleName()+" ) - ";
	protected String[] misspelledWordArray=new String[5513];//make this larger if more words are added
	private ArrayList<String> misspelledWordList;
	private static String filePath = ANONConstants.EXTERNAL_RESOURCE_PACKAGE+"writeprints_misspellings.txt";
	private static String filePath2 = ANONConstants.EXTERNAL_RESOURCE_PACKAGE+"wikipedia_misspellings_sequences.txt";//not sure if needed
	private Trie node;
	
	public MisspelledWords(){
		misspelledWordList=readMisspelledWords();
		for(int i=0;i<misspelledWordArray.length;i++){
			misspelledWordArray[i]=misspelledWordList.get(i).toLowerCase();
		}
		node = new Trie();
		node.addWords(misspelledWordArray);
	}
	
	public boolean searchListFor(String str){
		return node.find(str);
	}
	
	public String getWordAt(int index){
		return misspelledWordArray[index];
	}
	public static ArrayList<String> readMisspelledWords(){
		ArrayList<String> misspelledWords=new ArrayList<String>();
		
		 try {
			BufferedReader readIn  = new BufferedReader(new FileReader(filePath));
			String newLine;
			try {
				while((newLine=readIn.readLine())!=null){
					misspelledWords.add(newLine);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			readIn  = new BufferedReader(new FileReader(filePath2));
			try {
				while((newLine=readIn.readLine())!=null){
					misspelledWords.add(newLine);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			Logger.logln("(MisspelledWords) - Error opening reader: "+e.getMessage());
		}
		
		return misspelledWords;
	}
	
public static void main(String[] args){//times the execution of the search on the list of misspelled words.
		
		MisspelledWords mWord=new MisspelledWords();
		
		String findStr;
		Random randomGen = new Random(); 
		int num;
		long startTime;
		long endTime;	
		startTime = System.currentTimeMillis();
		for(int i=0;i<mWord.misspelledWordList.size();i++){
			num=Math.abs(randomGen.nextInt()%mWord.misspelledWordList.size());
			findStr=mWord.misspelledWordList.get(num);			
			mWord.searchListFor(findStr);
		}
		endTime = System.currentTimeMillis();
		System.out.println("Trie Test Time: " + (endTime-startTime));//strList.size());		
		startTime = System.currentTimeMillis();
		for(int i=0;i<mWord.misspelledWordList.size();i++){
			num=Math.abs(randomGen.nextInt()%mWord.misspelledWordList.size());
			findStr=mWord.misspelledWordList.get(num);			
			mWord.misspelledWordList.contains(findStr);
		}
		endTime = System.currentTimeMillis();
		System.out.println("Linear Search Time: " + (endTime-startTime));
		//findStr="scholarstic";
		//System.out.println(mWord.searchListFor(findStr));
		startTime = System.currentTimeMillis();
		for(int i=0;i<mWord.misspelledWordList.size();i++){
			num=Math.abs(randomGen.nextInt()%mWord.misspelledWordList.size());
			findStr=mWord.misspelledWordList.get(num);			
		}
		endTime = System.currentTimeMillis();
		System.out.println("Overhead Time: " + (endTime-startTime));
	}
}
