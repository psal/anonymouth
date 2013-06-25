package edu.drexel.psal.anonymouth.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import edu.drexel.psal.jstylo.generics.Logger;

/**
 * Reader and writer for Objects.
 * @author Andrew W.E. McDonald
 *
 */
public class ObjectIO {
	
	private final static String NAME = "( ObjectIO ) - ";
	
	/**
	 * Generic object writer
	 * @param o the Object to write 
	 * @param id name of object
	 * @param dir directory to write the object to
	 * @return true if no errors, false otherwise
	 */
	public static boolean writeObject(Object o, String id, String dir){
		ObjectOutputStream outObject = null;
		System.out.println(NAME+"Place to write: "+dir+id+".ser");
		try {
			outObject = new ObjectOutputStream(new BufferedOutputStream( new FileOutputStream(dir+id+".ser")));
			try{
				outObject.writeObject(o);
			}
			finally{
				outObject.close();
			}
		} catch (FileNotFoundException e) {
			//Logger.logln(NAME+"FILE NOT FOUND saving object: "+o.toString());
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			//Logger.logln(NAME+"IO EXCEPTION saving object: "+o.toString());
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * Reads a saved serialized HashMap of TreeData objects in 'dir' named 'id' and returns the TreeContainer object. 
	 * @param id name of HashMap 
	 * @param dir location of saved .ser file
	 * @param printData if true, will print HashMap to string (via toString method)
	 * @return HashMap of TreeData objects specified by 'id' and 'dir', or null if no TreeContainer found
	 */
	@SuppressWarnings("unchecked")
	public static HashMap<String,TreeData> readTreeDataMap(String id, String dir, boolean printData){
		ObjectInput inputObject;
		HashMap<String,TreeData> tdHash = null;
		try {
			inputObject = new ObjectInputStream(new BufferedInputStream(new FileInputStream(dir+id+".ser")));
			try{
				tdHash = (HashMap<String,TreeData>) inputObject.readObject();
			} catch (ClassNotFoundException e) {
				tdHash = null;
				Logger.logln("Couldn't load ArrayList<TreeData>: "+id+", from: "+dir);
				e.printStackTrace();
			}
			finally{
				inputObject.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(printData == true && tdHash != null){
			System.out.println(tdHash.toString());
		}
		return tdHash;
		
	}
	
	public static TaggedDocument readTaggedDocument(String id, String dir, boolean printData){
		ObjectInput inputObject;
		TaggedDocument td = null;
		try {
			inputObject = new ObjectInputStream(new BufferedInputStream(new FileInputStream(dir+id+".ser")));
			try{
				td = (TaggedDocument) inputObject.readObject();
			} catch (ClassNotFoundException e) {
				td = null;
				Logger.logln("Couldn't load TaggedDocument: "+id+", from: "+dir);
				e.printStackTrace();
			}
			finally{
				inputObject.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(printData == true && td != null){
			System.out.println(td.toString());
		}
		return td;
		
	}
	
	public static boolean objectExists(String id, String dir){
		File f = new File(dir+id+".ser");
		if (f.exists())
			return true;
		else
			return false;
	}
}
