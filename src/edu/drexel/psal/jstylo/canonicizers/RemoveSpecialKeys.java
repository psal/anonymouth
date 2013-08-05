package edu.drexel.psal.jstylo.canonicizers;

import java.io.*;
import java.util.*;

import com.jgaap.generics.Canonicizer;

/** 
 * Removes special keys from the text, represented by unique greek letters.
 */
public class RemoveSpecialKeys extends Canonicizer {

	protected static String SPECIAL_KEYS_PATH =
			"edu/drexel/psal/resources/special_keys.txt";
	
	protected SortedMap<String,String> map = null;
	
	/*
	 * Constructors
	 */
	
	public RemoveSpecialKeys() {
		// initialize map
		if (map == null)
			try {
				InputStream in = getClass().getClassLoader().
						getResourceAsStream(SPECIAL_KEYS_PATH);
				Scanner scan = new Scanner(new InputStreamReader(in));
				map = new TreeMap<String, String>();

				String[] pair;
				while (scan.hasNext())
				{
					pair = scan.nextLine().split(",");
					map.put(pair[0].trim(), pair[1].trim());
				}
			} catch (Exception e) {}
	}
	
    @Override
    public String displayName(){
    	return "Remove special keys";
    }
    
    @Override
    public String tooltipText(){
    	String res = "Remove all special keystokes in the document, " +
    			"like backspace, return, delete, page-up etc., represented by " +
    			"unique greek letter identifiers as follows:\n";
    	if (map == null)
    		res += "unable to load map; do not use this event driver.";
    	else
    	{
    		for (String key: map.keySet())
    			res += key + " -> " + map.get(key) + "\n";
    	}
    	return res;
    }
    
    @Override
    public boolean showInGUI(){
    	return true;
    }

    /**
     * Remove all special keys from input characters
     * @param procText Array of characters to be processed.
     * @return Array of processed characters.
     */
    @Override
    public char[] process(char[] procText) {
    	if (map == null) return procText;
    	Set<String> values = new HashSet<String>(map.values());

    	String procString = "";
    	for (char c: procText)
    	{
    		if (!values.contains(c + ""))
    			procString += c;
    	}
    	return procString.toCharArray();
    }
    
    
   /*
    * Main for testing
    */
    public static void main(String[] args) {
    	String test =
    			"βββββRebecca	Bailey	rebphotography@yahoo.com	\n" +
    			"rebphotographpyΒβββhy@yahoo.com	ΔΔ		\n" +
    			"eβrebphotography@yahoo.com	rebphotography@yahoo.com\n" +			
    			"mail.uahβββyahoo.cm\n" +
    			"ββom\n" +
    			"rebphotography	cricket01\n" +
    			"mail.yahoo.com\n" +
    			"rebphotography	cricket01\n";
    	System.out.println("before");
    	System.out.println(test);
    	System.out.println();
    	System.out.println("after");
    	System.out.println(new String(new RemoveSpecialKeys().process(
    			test.toCharArray())));
	}
}
