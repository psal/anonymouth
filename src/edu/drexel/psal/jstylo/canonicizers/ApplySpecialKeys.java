package edu.drexel.psal.jstylo.canonicizers;

import java.io.*;
import java.util.*;

import com.jgaap.generics.Canonicizer;
import com.jgaap.generics.Document;

/** 
 * Applies special keys from the text onto it.
 * Special keys are represented by unique Greek letters.
 */
public class ApplySpecialKeys extends Canonicizer {

	private static char CAPSLOCK;
	private static char ENTER;
	private static char SPACE;
	private static char BACKSPACE;
	private static char TAB;
	private static char DEL;
	private static char LEFT;
	private static char RIGHT;
	private static char UP;
	private static char DOWN;
	private static char ESC;
	private static char SHIFT;
	private static char ALT;
	private static char CTRL;
	private static char PRINTSCREEN;
	private static char HOME;
	private static char END;
	private static char PGUP;
	private static char PGDN;
	private static char INSERT;
	private static char VOLUME_UP;
	private static char VOLUME_DOWN;
	
	private SortedMap<String,String> map = null;
	private boolean applyShifts = false;

	public ApplySpecialKeys()
	{
		if (map == null)
		{
			RemoveSpecialKeys rsk = new RemoveSpecialKeys();
			map = rsk.map;
			CAPSLOCK = map.get("CAPSLOCK").charAt(0);
			ENTER = map.get("ENTER").charAt(0);
			SPACE = map.get("SPACE").charAt(0);
			BACKSPACE = map.get("BACKSPACE").charAt(0);
			TAB = map.get("TAB").charAt(0);
			DEL = map.get("DEL").charAt(0);
			LEFT = map.get("LEFT").charAt(0);
			RIGHT = map.get("RIGHT").charAt(0);
			UP = map.get("UP").charAt(0);
			DOWN = map.get("DOWN").charAt(0);
			ESC = map.get("ESC").charAt(0);
			SHIFT = map.get("SHIFT").charAt(0);
			ALT = map.get("ALT").charAt(0);
			CTRL = map.get("CTRL").charAt(0);
			PRINTSCREEN = map.get("PRINTSCREEN").charAt(0);
			HOME = map.get("HOME").charAt(0);
			END = map.get("END").charAt(0);
			PGUP = map.get("PGUP").charAt(0);
			PGDN = map.get("PGDN").charAt(0);
			INSERT = map.get("INSERT").charAt(0);
			VOLUME_UP = map.get("VOLUME_UP").charAt(0);
			VOLUME_DOWN = map.get("VOLUME_DOWN").charAt(0);
		}
	}
	
	public ApplySpecialKeys(boolean applyShifts)
	{
		this();
		this.applyShifts = applyShifts;
	}

	@Override
	public String displayName(){
		return "Apply special keys (" + (applyShifts ? "in" : "ex") + 
				"cluding shifts)";
	}

	@Override
	public String tooltipText(){
		String res = "Apply all special keystokes in the document onto it, " +
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
		// map initialization error
		if (map == null)
			return procText;
		
		LinkedList<Character> res =
				new LinkedList<Character>();
		String s;

		// position indicators
		int maxLen = procText.length;
		int len = 0;
		int pos = 0;

		// flags
		boolean capslock = false;
		boolean insert = false;
		boolean shift = false;

		for (char c: procText)
		{
			if (c == CAPSLOCK)
			{
				capslock = !capslock;
			}
			else if (c == ENTER)
			{
				res.add('\n');
				len++;
				pos++;
			}
			else if (c == SPACE)
			{
				res.add(' ');
				len++;
				pos++;
			}
			else if (c == BACKSPACE)
			{
				if (pos > 0)
				{
					pos--;
					res.remove(pos);
					len--;
				}
			}
			else if (c == TAB)
			{
				res.add('\t');
				len++;
				pos++;
			}
			else if (c == DEL)
			{
				if (pos < len)
				{
					res.remove(pos);
					len--;
				}
			}
			else if (c == LEFT)
			{
				if (pos > 0)
					pos--;
			}
			else if (c == RIGHT)
			{
				if (pos < len)
					pos++;
			}
			else if (c == UP)
			{
				// ignore
			}
			else if (c == DOWN)
			{
				// ignore
			}
			else if (c == ESC)
			{
				// ignore
			}
			else if (c == SHIFT)
			{
				shift = !shift;
			}
			else if (c == ALT)
			{
				// ignore
			}
			else if (c == CTRL)
			{
				// ignore
			}
			else if (c == PRINTSCREEN)
			{
				// ignore
			}
			else if (c == HOME)
			{
				// ignore 
				/*
				while (0 < pos && pos < len && res.get(pos) != '\n')
					pos--;
				if (pos > 0)
					pos++;
				*/
			}
			else if (c == END)
			{
				// ignore
				/*
				while (pos < len && res.get(pos) != '\n')
					pos++;
				if (pos < len)
					pos++;
				*/
			}
			else if (c == PGUP)
			{
				// ignore
			}
			else if (c == PGDN)
			{
				// ignore
			}
			else if (c == INSERT)
			{
				insert = !insert;
			}
			else if (c == VOLUME_UP)
			{
				// ignore
			}
			else if (c == VOLUME_DOWN)
			{
				// ignore
			}
			else
			{
				s = c + "";
				// shift
				if (shift && applyShifts)
				{
					s = switchCase(s);
					shift = false;
				}
				// capslock
				if (capslock)
					s = switchCase(s);
				// insert
				if (pos < len)
					if (insert)
						res.set(pos, c);
					else
					{
						res.add(c);
						len++;
					}
				else
				{
					res.add(pos, c);
					len++;
				}
				pos++;
			}
			if (pos > len)
			{
				System.out.println(">>> pos: " + pos + ", len: " + len + ", c: " + c);
			}
		}

		return toCharArr(res);
	}
	
	/**
	 * Switches lower case to upper and vice versa.
	 * @param s
	 * @return
	 */
	private static String switchCase(String s)
	{
		if (s.equals(s.toLowerCase()))
			return s.toUpperCase();
		else
			return s.toLowerCase();
	}
	
	private static char[] toCharArr(List<Character> list)
	{
		char[] res = new char[list.size()];
		int i = 0;
		for (char c: list)
		{
			res[i] = c;
			i++;
		}
		return res;
	}


	/*
	 * Main for testing
	 */
	public static void main(String[] args) throws Exception
	{
		ApplySpecialKeys a = new ApplySpecialKeys();
		Document d = new Document(
				"d:\\dev\\active-auth\\data_redivided\\" +
				"timed_ks_3600000-ms-windows\\user02\\user02_day3_28800000","");
		d.load();
		String text = d.stringify();
		System.out.println("before");
		System.out.println(text);
		System.out.println();
		System.out.println("after");
		System.out.println(new String(a.process(text.toCharArray())));
	}
}











