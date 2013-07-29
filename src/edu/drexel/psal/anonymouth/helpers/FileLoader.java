package edu.drexel.psal.anonymouth.helpers;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashSet;

import edu.drexel.psal.jstylo.generics.Logger;
import edu.drexel.psal.jstylo.generics.Logger.LogOut;

/**
 * Exactly what the name says, it makes loading files less of a pain in the ass.
 * @author Marc Barrowclift
 *
 */
public class FileLoader {
	
	private static final String NAME = "( FileLoader ) - ";
	
	/**
	 * Creates and returns a ready BufferedReader instance to read. Returns null if file was not found or another error occurred.
	 * @param path - The absolute path to the file you want to read
	 * @return
	 */
	public static BufferedReader loadFile(String path) {
		BufferedReader reader = null;
		
		try {
			reader = new BufferedReader(new FileReader(path));
		} catch (FileNotFoundException e) {
			Logger.logln(NAME+"Error loading file at path: "+path+", file was not found", LogOut.STDERR);
		} catch (Exception e) {
			Logger.logln(NAME+"Unknown error occurred loading file at path: "+path);
		}
		
		return reader;
	}
	
	/**
	 * Reads through a given file and returns a string version of it (null if file was not found or another error occurred).
	 * @param path - The absolute path to the file
	 * @return
	 */
	public static String readFile(String path) {
		String contents = null;
		
		try {
			@SuppressWarnings("resource")
			BufferedReader reader = new BufferedReader(new FileReader(path));
			
			String line = "";
			while ((line = reader.readLine()) != null) {
				contents = contents + line;
			}
		} catch (FileNotFoundException e) {
			Logger.logln(NAME+"Error loading file at path: "+path+", file was not found", LogOut.STDERR);
		} catch (Exception e) {
			Logger.logln(NAME+"Unknown error occurred loading file at path: "+path);
		}
		
		return contents;
	}
	
	/**
	 * Serves a pretty specific purpose now, but hopefully it will prove useful in the future. This reads a given file line by line,
	 * and for each line it reads it in as a string and adds it to a HashSet of strings and then returns the final result at end of file.
	 * 
	 * This is to be used for when you want to look up stuff (In my case, dictionary words) in constant time, and that stuff is in a file that
	 * needs to be read in. If no document is found at the path or there's another error an empty HashSet is returned.
	 * @param path - The absolute path to the file
	 * @return
	 */
	public static HashSet<String> tokenizeLinesFromFile(String path) {
		HashSet<String> tokens = new HashSet<String>();
		
		try {
			@SuppressWarnings("resource")
			BufferedReader reader = new BufferedReader(new FileReader(path));
			
			String line = "";
			while ((line = reader.readLine()) != null) {
				tokens.add(line);
			}
		} catch (FileNotFoundException e) {
			Logger.logln(NAME+"Error loading file at path: "+path+", file was not found", LogOut.STDERR);
		} catch (Exception e) {
			Logger.logln(NAME+"Unknown error occurred loading file at path: "+path);
		}
		
		return tokens;
	}
}
