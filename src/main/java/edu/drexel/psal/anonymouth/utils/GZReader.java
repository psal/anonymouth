package edu.drexel.psal.anonymouth.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.*;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import edu.drexel.psal.jstylo.generics.Logger;

public class GZReader {
	
	private final String NAME = "( "+this.getClass().getName()+" ) - ";
	BufferedReader buff;

	public GZReader(){
		try {
			InputStreamReader isr = new InputStreamReader(new GZIPInputStream(new FileInputStream("/Users/lux/PSAL/paraphrasing/pre_made/paraphrase_maps.gz")),"UTF-8");//grammar_sorted.non_X_rules.interesting_paraphrases.gz")),"UTF-8");
			buff = new BufferedReader(isr);
			
		} catch (FileNotFoundException e) {
			Logger.logln("Paraphrase file not found...");
			e.printStackTrace();
		} catch (IOException e) {
			Logger.logln("IOException thrown while trying to read paraphrase file!");
			e.printStackTrace();
		}
			
		
	}
	
	public void gzSearch(String string) throws IOException{
		String temp;
		String [] tempSplit;
		int i = 0;
		int lenTempSplit;
		//OutputStreamWriter osw = new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream("/Users/lux/PSAL/paraphrasing/pre_made/small.gz")));
		Scanner in = new Scanner(System.in);
		int numLines = 0;
		int numTenThousands = 0;
		long start = System.currentTimeMillis();
		while( (temp = buff.readLine()) != null){
			if(temp.toLowerCase().contains(string.toLowerCase()))
				System.out.println(temp);
			//System.out.println(temp.substring(0,firstCut+1)+temp.substring(secondCut,thirdCut)+temp.substring(fourthCut));
			//osw.write(temp.substring(0,firstCut)+"\n");
			//System.out.println(temp.substring(0,firstCut));
			//in.nextLine();
			//numLines++;
			//if (numLines == 10000){
			//	numTenThousands ++;
			//	System.out.println((numTenThousands*10000)+" lines complete.");
			//	numLines = 0;
			//}
		}	
		long stop = System.currentTimeMillis();
		long elapsed = stop-start;
		System.out.println(elapsed);
		buff.close();
		//osw.close();
		
	}
	
	public void gzSearchCutSave(int[] columns, String[] strings) throws IOException{
		String temp;
		String [] tempSplit;
		int i = 0;
		int lenTempSplit;
		OutputStreamWriter osw = new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream("/Users/lux/PSAL/paraphrasing/pre_made/structure_maps.gz")));
		OutputStreamWriter osw_two = new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream("/Users/lux/PSAL/paraphrasing/pre_made/paraphrase_maps.gz")));
		Scanner in = new Scanner(System.in);
		int numLines = 0;
		int numMillions = 0;
		long start = System.currentTimeMillis();
		while( (temp = buff.readLine()) != null){
			//tempSplit = temp.split("\\|\\|\\|");
			//int firstCut =  temp.lastIndexOf("|||")+3;
			if(temp.contains(",1")){
			//int secondCut = temp.indexOf("Lex(");
			//int thirdCut = temp.indexOf("Lexical=")-1;
			//int fourthCut = temp.indexOf("WordCountDiff=");
			//lenTempSplit  = tempSplit.length;
			//System.out.println(temp.substring(0,firstCut+1)+temp.substring(secondCut,thirdCut)+temp.substring(fourthCut));
			osw.write(temp+"\n");//.substring(0,firstCut)+"\n");
			}
			else
				osw_two.write(temp+"\n");
			//System.out.println(temp);//.substring(0,firstCut));
			
			//in.nextLine();
			numLines++;
			if (numLines == 1000000){
				numMillions ++;
				System.out.println((numMillions*1000000)+" lines complete.");
				numLines = 0;
			}
		}	
		long stop = System.currentTimeMillis();
		long elapsed = stop-start;
		System.out.println(elapsed);
		buff.close();
		osw.close();
		osw_two.close();
	}
	
	public static void main(String[] args) throws IOException {
		String txtPath = "/Users/lux/PSAL/paraphrasing/pre_made/grammar_sorted.non_X_rules.interesting_paraphrases.txt";
		//File f = new File(txtPath);
		//grep(f);
		
		GZReader gzr = new GZReader();
		try {
			gzr.gzSearch("|");
			//gzr.gzSearchCutSave(new int[]{2,3},new String[]{"a","b"});
		} catch (IOException e) {
			System.out.println("Oh no. It isn't working.");
			e.printStackTrace();
		}
	/*	
		try {
			
			
			
			String temp;
			//System.out.println(fc.size());
			fc.
			long start = System.currentTimeMillis();
			while((temp = buff.readLine()) != null){
			}
			long stop = System.currentTimeMillis();
			System.out.println((stop-start)+" ms");
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		
		
	}
	
}
