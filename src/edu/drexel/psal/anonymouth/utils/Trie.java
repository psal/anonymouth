package edu.drexel.psal.anonymouth.utils;

/**
 * Implements a Trie. Currenly only supports lowercase letters (upper case gets converted to lowercase), and apostrophes.
 * Appends a '{' onto the end of each word. Functionalty can be extended to all ASCII characters easily by decreasing the offset,
 * and increasing the size of the TrieNode array. Trie doesn't save actual characters/strings, only the characters' positions:
 * 
 * a TrieNode only contains an array of TrieNodes. When adding a word, if the first letter is 'a', the char value of 'a' is 97, and the offset
 * is 96, so 97 minus 96 = 1 => create a new TrieNode at index 1 in the root's TrieNode child array, and so on. 
 * @author Andrew W.E. McDonald
 *
 */
public class Trie{
	
	private final String NAME = "( "+this.getClass().getName()+" ) - ";
	TrieNode trie=new TrieNode();
	private final char lastChar='{';
	private final int OFFSET = 96;
	
	/**
	 * Adds a word to the trie.
	 * @param word word to add
	 */
	public void addWord(String word){
		char[] theWord = word.toLowerCase().toCharArray();
		TrieNode t = this.trie;
		for(char c:theWord){
			t = addLetter(t,c);
		}
		addLetter(t,lastChar);
	}
	
	/**
	 * Adds a letter to the Trie
	 * @param t TrieNode
	 * @param c letter to add
	 * @return
	 * The newly created child TrieNode of the input TrieNode in the index of the input character's value minus the offset.
	 */
	public TrieNode addLetter(TrieNode t,char c){
		c=checkOddChars(c);
		if(t.children[c-OFFSET] == null)
			t.children[c - OFFSET] = new TrieNode();
		return t.children[c - OFFSET];
	}
	
		
	/**
	 * finds a word in the Trie
	 * @param word word to find
	 * @return
	 * true if found, false otherwise
	 */
	public boolean find(String word){
		char[] chars = word.toLowerCase().toCharArray();
		TrieNode t = this.trie;
		for(char c:chars){
			c=checkOddChars(c);
			try {
				if(t.children[c-OFFSET] == null)
						return false;
				t = t.children[c-OFFSET];
			} catch (ArrayIndexOutOfBoundsException e) {
				//An unsupported character slipped through, assume it's not in the trie
				return false;
			}
		}
		if(t.children[27]==null)
			return false;
		return true;
	}
	
	/**
	 * Adds all words in the input String array
	 * @param words words to add
	 */
	public void addWords(String[] words){
		for(String word:words){
			addWord(word);
		}
	}
	
	public static void main(String[] args){
		Trie tt = new Trie();
		tt.trie = new TrieNode();
		tt.addWords(new String[]{"and","soup","coffee","basketball"});
		System.out.println(tt.find("and"));
		System.out.println(tt.find("was"));
	}
	/**
	 * checks the char for odd values that throw the function off
	 * @param c character to check.
	 * @return a char that does not break the program.
	 */
	private char checkOddChars(char c){
		if(c=='\'')
			c='`';
		else if (c=='-')
			c='~';
		else if(c=='%'){
			c='}';
		}
		else if(c=='2'||c=='7')
			c='|';
		else if(c==' ')
			c='_';
		
		return c;
	}
}

/**
 * The TrieNode. Uses an empty constructor, and only contains an array of TrieNodes. No node ever has a 'value'. Each 
 * child node (each node in the index), is either equal to 'null', or contains a new TrieNode. 
 * @author Andrew W.E. McDonald
 *
 */
class TrieNode {
	protected TrieNode[] children = new TrieNode[32];

}
