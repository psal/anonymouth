package edu.drexel.psal.jstylo.generics;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * A class for handling multiple print streams, e.g. System.out + logging to file.
 * 
 * @author Ariel Stolerman
 *
 */
public class MultiplePrintStream {
	
	private static SimpleDateFormat tf = new SimpleDateFormat("HH-mm-ss");
	private static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
	private static Calendar cal;

	/**
	 * @return The current date.
	 */
	public static String date() {
		cal = Calendar.getInstance();
		return df.format(cal.getTime());
	}
	
	/**
	 * @return The current time.
	 */
	public static String time() {
		cal = Calendar.getInstance();
		return tf.format(cal.getTime());
	}
	
	/**
	 * @return A path for a new log with the date and time of creation.
	 */
	public static String getLogFilename() {
		return "log_" + date() + "_" + time() + ".txt";
	}
	
	protected PrintStream[] psArr;
	
	public MultiplePrintStream(PrintStream... ps) {
		this.psArr = new PrintStream[ps.length];
		for (int i = 0; i < ps.length; i++)
			this.psArr[i] = ps[i];
	}
	
	public void println() {
		for (PrintStream ps: psArr)
			if (ps != null)
				ps.println();
	}
	
	public void println(Object o) {
		for (PrintStream ps: psArr)
			if (ps != null)
				ps.println(o);
	}
	
	public void println(String s) {
		for (PrintStream ps: psArr)
			if (ps != null)
				ps.println(s);
	}
	
	public void println(String s, int maxTokensPerLine) {
		String[] tokens = s.split("\\s+");
		String modified = "";
		for (int i = 0; i < tokens.length; i++) {
			modified += tokens[i] + " ";
			if ((i + 1) % maxTokensPerLine == 0)
				modified += "\n";
		}
		println(modified);
	}

	public void print(String s) {
		for (PrintStream ps: psArr)
			if (ps != null)
				ps.print(s);
	}
	
	public void printf(String s, Object... args) {
		for (PrintStream ps: psArr)
			if (ps != null)
				ps.printf(s, args);
	}
	
	public void close() {
		for (PrintStream ps: psArr)
			if (ps != null)
				ps.close();
	}
}
