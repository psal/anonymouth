package edu.drexel.psal.anonymouth.utils;

/**
 * Sentence class to hold data relating to a sentence
 * @author Andrew W.E. McDonald
 *
 */
public class Sentence implements Comparable<Sentence>{
		
		private final String NAME = "( "+this.getClass().getName()+" ) - ";
		protected String text;
		protected int count = 0;
		protected int absStart;
		protected int absStop;
		protected boolean shouldIgnoreCase = true;
		
		/**
		 * constructor for sentence class
		 * @param s the sentence
		 */
		public Sentence(String s){
			text = s;
			count++;
		}
		
		/**
		 * another constructor for sentence class. 
		 * @param s the sentence
		 * @param start the (absolute) start index (with respect to the document it came from)
		 * @param stop the (absolute) stop index (with respect to the document it came from)
		 */
		public Sentence(String s, int start, int stop){
			text = s;
			absStart = start;
			absStop = stop;
			count ++;
		}
		
		/**
		 * adds the numToAdd to the number of appearances of this sentence
		 * @param numToAdd the amount to add to the count
		 */
		public void addToCount(int numToAdd){
			count += numToAdd;
		}
		
		/**
		 * increments the count of this sentence
		 */
		public void increment(){
			count++;
		}
		
		/**
		 * 
		 * @return the number of times this sentence appeared
		 */
		public int getCount(){
			return count;
		}
		
		/**
		 * the sentence (text)
		 * @return
		 */
		public String getSentence(){
			return text;
		}
		
		/**
		 *@return [[ text ](count)] 
		 */
		public String toString(){
			return "[[ "+text+" ]("+count+")]"; 
		}
		
		
		/**
		 *@param escapeChars if 'true' will return a string with commas escaped and newlines changed (good for CSV formatting)
		 *@return [[ text ](count)] with all commas escaped if escapeComma is set to true, normal string otherwise
		 */
		public String toString(boolean escapeComma){
			if(escapeComma == true){
				String temp =  "[[ "+text.replaceAll(",", "\",\"")+" ]("+count+")]"; 
				return temp.replaceAll("\\n", "<NL>");
			}
			else
				return "[[ "+text+" ]("+count+")]"; 
		}
		
		/**
		 * sets the absolute start and stop indices of the sentence with respect to the document it came from.
		 * @param start the beginning
		 * @param stop the end
		 */
		public void setAbsBounds(int start, int stop){
			absStart = start;
			absStop = stop;
		}
		
		/**
		 * returns the absolute start and stop indices of the sentence with respect to the document it came from.
		 * @return
		 * 	integer array with [start,stop]
		 */
		public int[] getAbsBounds(){
			return new int[]{absStart,absStop};
		}
		
		/**
		 * sets whether or not case will be ignored in the equals method comparison. 'true' to ignore case, 'false' to take case into account.
		 * default is true (case is unimportant). NOTE: this effects the Sentence class hashCode method.
		 * @param shouldIgnore
		 */
		public void setIgnoreCase(boolean shouldIgnore){
			shouldIgnoreCase = shouldIgnore;
		}
		
		/**
		 * default is true. 
		 * @return returns true if equals method ignores case, false if case is important.
		 */
		public boolean isIgnoreCase(){
			return shouldIgnoreCase;
		}
		
		/**
		 * defines two Sentence objects to be equal if they contain the same text. 
		 * @return
		 * 	true if equal
		 */
		public boolean equals(Object obj){
			boolean isSame = true;
			String otherText =((Sentence)obj).text;
			if(shouldIgnoreCase == true){
				if(text.toLowerCase().equals(otherText.toLowerCase()) == false)
					isSame = false;
			}
			else{
				if(text.equals(otherText) == false)
					isSame = false;
			}
			return isSame;
		}
		
		/**
		 * generates a hashcode for Sentence,  by calling the String hashCode method on the sentence text. Unless instructed to do otherwise 
		 * by the 'setIgnoreCase' method, hashCodes are generated after converting the text string to lowercase.
		 * @return
		 * 	hashcode
		 */
		public int hashCode(){
			if(shouldIgnoreCase == true)
				return text.toLowerCase().hashCode();
			else
				return text.hashCode();
		}
		
		

		@Override
		public int compareTo(Sentence s) {
			if(this.count < s.count)
				return 1;
			else if (this.count == s.count)
				return 0;
			else
				return -1;
		}
		
}


