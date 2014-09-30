package edu.drexel.psal.anonymouth.engine;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.drexel.psal.jstylo.generics.Logger;

/**
 * Strips the undesirable 'extra' characters in the attribute's fullName (as taken from Weka's Instances object)
 * @author Andrew W.E. McDonald
 *
 */
public class AttributeStripper {
	
	private final String NAME = "( "+this.getClass().getName()+" ) - ";
	
	private static Pattern bracketPat = Pattern.compile("\\{[^-]+\\}");
	private static Pattern someString = Pattern.compile("\\{.*\\}"); // use this pattern, and if an exception is thrown, 
	private static Pattern attribNumber = Pattern.compile("-\\d+\\{"); //used to strip out attribute numbers
	
	/**
	 * strips the input string
	 * @param input
	 * @return
	 *  stripped input string
	 */
	public static String strip(String input){
		String output = "";
		Matcher histTest = bracketPat.matcher(input);
		
		if (histTest.find() == true) {
			if (input.contains("{")) {
				//Matcher m = someString.matcher(input);
				//m.find();
				int n = input.indexOf('{');
				String inBraces = input.substring(input.indexOf('{')+1, input.indexOf('}'));
				if (input.contains("'")) {
					//Matcher n = attribNumber.matcher(input);
					//n.find();
					String attribName = input.substring(input.indexOf(" '") + 2, n);
					output = attribName + " " + inBraces;
				} else {
					output = inBraces;
				}
			} else {
				Matcher m = someString.matcher(input);
				m.find();
				Matcher n = attribNumber.matcher(input);
				n.find();
				String attribName = input.substring(input.indexOf("'") + 1, n.start());
				output = attribName;
			}
			
		} else {
			if (!input.contains("authorName")){
				
				if (input.contains("{")){
					output = input.substring(input.indexOf("'")+1,input.indexOf("{"));
				} else {
					output = input.substring(input.indexOf("'")+1);
				}
			}
		}
		return output;
	}

}
