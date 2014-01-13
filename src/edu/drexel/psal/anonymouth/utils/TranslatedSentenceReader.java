package edu.drexel.psal.anonymouth.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Scanner;


/**
 * 
 * @author Andrew W.E. McDonald
 *
 */
public class TranslatedSentenceReader {
	
// a, b,c,cm,d,e,f,g,h,k,m,p,s	
	
/*
 * open "anonymouth_translations/translated/", and read directory (ignore hidden files)
 * 		open the file, read by line. For each line, find the first occurrence of ':', remove anything before that. Put each of these sentences in an ArrayList.
 * 			repeat for all files such that the start (first two characters of name) of the file is the same as the last file	
 * 				put the ArrayList for each file into another ArrayList
 * 
 * This should either be done for only the current author of interest, or, this should be done one time, and the results serialized. 
 */
	
	private final String NAME = "( "+this.getClass().getName()+" ) - ";
	String translated_location = "anonymouth_translations/translated/";
	String[] authors = {"a","b","c","cm","d","e","f","g","h","k","m","p","s"};
	int numAuths = authors.length;
	File transedDir;
	
	public File[] readDir(String author){
		int i;
		String endOfPath = "";
		for(i = 0; i < numAuths; i++){
			if(authors[i].equals(author)){
				endOfPath = authors[i];
				break;
			}
		}
		if(endOfPath.equals(""))
			return null;// if an author is requested that doesn't exist, we are done.
		String desiredDir = translated_location+endOfPath;
		transedDir = new File(desiredDir);
		File[] docs = transedDir.listFiles();
		System.out.println("pre-sort");
		int numDocs = docs.length;
		for(i=0; i < numDocs; i++)
			System.out.println(docs[i]);
		// must sort the array so that the files are read in in the order that the corresponding sentences are in in the document.
		Arrays.sort(docs, new Comparator<File>(){
			public int compare(final File f1, final File f2){
				String name1 = f1.getName();
				String name2 = f2.getName();
				int num1 = Integer.parseInt(name1.substring(name1.lastIndexOf('_')+1, name1.lastIndexOf('.')));
				int num2 = Integer.parseInt(name2.substring(name2.lastIndexOf('_')+1, name2.lastIndexOf('.')));
				return ((Integer)num1).compareTo((Integer)num2);
			}
		});
		System.out.println("post-sort");
		for(i=0; i < numDocs; i++)
			System.out.println(docs[i]);
		return docs;
	}
	
	
	public ArrayList<ArrayList<String>> getReplacements(String author){
		ArrayList<ArrayList<String>> theReplacements = new ArrayList<ArrayList<String>>(50);// 50 sentences should be adequate for now. 
		File[] docs = readDir(author);
		int i;
		int numDocs = docs.length;
		int optionNumber;
		for(i=0; i < numDocs; i++){
			File doc = docs[i];
			if(doc.getName().charAt(0) == '.'){
				//System.out.printf("The hidden file we are skipping is: %s\n",doc.getName());
				continue;
			}
			ArrayList<String> thisSentsReplacements = new ArrayList<String>(12);
			try {
				BufferedReader bufRead = new BufferedReader(new FileReader(doc));
				String currentLine;
				optionNumber = 0;
				while((currentLine = bufRead.readLine()) != null){
					if(currentLine.contains("Original:")){
						//System.out.println("Found 'Original:'...");
						continue;
					}
					//System.out.printf("Replacement pre-trim: %s\n",currentLine);
					currentLine = " "+currentLine.substring(currentLine.indexOf(':')+1);
					//System.out.printf("Replacement post-trim: %s\n",currentLine);
					thisSentsReplacements.add(optionNumber,currentLine);
					optionNumber += 1;
				}
				bufRead.close();
				while(thisSentsReplacements.size() > 0 && thisSentsReplacements.get(thisSentsReplacements.size()-1).trim().equals(""))// we don't want  blank lines at the end.
					thisSentsReplacements.remove(thisSentsReplacements.size()-1);
				//System.out.println("Num sents in ArrayList: "+thisSentsReplacements.size());
				theReplacements.add(i,thisSentsReplacements);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		// test code to print out all replacements for this document
		for(i=0; i < numDocs; i++){
			Iterator<String> thisSent = theReplacements.get(i).iterator();
			System.out.printf("The replacements for sentence number '%d' are:\n",i);
			while(thisSent.hasNext())
				System.out.printf("%s\n",thisSent.next());
		}
		return theReplacements;
	}
	
	
	public static void main(String args[]){
		TranslatedSentenceReader tsr = new TranslatedSentenceReader();
		tsr.getReplacements("a");
		
	}

}

class TranslatedSentence{

	
	
	
}
