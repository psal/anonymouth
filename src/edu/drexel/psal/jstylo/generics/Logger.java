package edu.drexel.psal.jstylo.generics;

import java.io.*;
import java.text.*;
import java.util.*;

import edu.drexel.psal.ANONConstants;

/**
 * Takes output that would normally just be printing out via stdout and in addition to printing also writes the output to a log
 * file, and also provides functionality for setting the name and location for these logs.
 * 
 * @author Ariel Stolerman
 * @author Marc Barrowclift
 *
 */
public class Logger {
	
	private static String NAME = "( Logger ) - ";
	public static final boolean loggerFlag = true;
	public static boolean logFile = false;
	
	// time
	private static SimpleDateFormat tf = new SimpleDateFormat("HH-mm-ss");
	private static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
	private static Calendar cal = null;
	
	// file
	private static String fileDirPath = ANONConstants.LOG_DIR;
	private static String filePrefix = "anonymouth";
	private static String out;
	private static BufferedWriter bw = null;
	private static String printBuffer = ""; //So we can store output even before the logger file is made
	
	/**
	 * Reutrns the current time.
	 * @return
	 * 		The current time.
	 */
	public static String time() {
		cal = Calendar.getInstance();
		return tf.format(cal.getTime());
	}
	
	/**
	 * Returns the current date.
	 * @return
	 * 		The current date.
	 */
	public static String date() {
		cal = Calendar.getInstance();
		return df.format(cal.getTime());
	}

	/**
	 * Initializes the file we will be printing our output to
	 */
	public static void initLogFile() {
		if (loggerFlag && logFile) {
			out = fileDirPath+"/"+filePrefix+"_"+date()+"_"+time()+".txt";
			String msg = NAME+"Started log "+out+"\n" +
					"=======================ALL PREVIOUS OUTPUT WRITTEN TO LOG FILE============================\n";
			System.out.println(time()+": "+msg);
			
			try {
				if (logFile) {
					bw = new BufferedWriter(new FileWriter(out));
					bw.write(msg);
					
					if (!printBuffer.equals("")) {
						bw.write(msg);
						bw.flush();
						printBuffer = "";
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println(NAME+"Failed opening log file!");
				System.exit(0);
			}
		}
	}
	
	/**
	 * Enumerator for logger output.
	 */
	public enum LogOut {
		STDOUT,
		STDERR
	}
	
	/**
	 * Prints output (no new line) to the file and to standard output
	 * @param msg
	 */
	public static void log(String msg) {
		if (loggerFlag) {
			String timedMsg = time()+": "+msg;

			// write to screen
			System.out.print(timedMsg);
			// write to file
			try {
				if (logFile) {
					bw.write(timedMsg);
					bw.flush();
				} else {
					printBuffer.concat(timedMsg);
				}
			} catch (IOException e) {
				System.err.println("Failed writing to log file!");
			}
		}
	}
	
	/**
	 * Prints a single line to the file and to standard output
	 * @param msg
	 */
	public static void logln(String msg) {
		if (loggerFlag) {
			log(msg);
			System.out.println();
			
			try {
				if (logFile) {
					bw.write("\n");
					bw.flush();
				} else {
					printBuffer.concat("\n");
				}
			} catch (IOException e) {
				System.err.println(NAME+"Failed writing to log file!");
			}
		}
	}
	
	/**
	 * Prints output (no new line) to the file and to standard output OR standard error output, depending on passed value.
	 * @param msg
	 * @param target
	 */
	public static void log(String msg, LogOut target) {
		if (loggerFlag) {
			String timedMsg = time()+": "+msg;

			// write to logger
			switch (target) {
				case STDOUT:
					System.out.print(timedMsg);	
					break;
				case STDERR:
					System.err.print(timedMsg);
					break;
			}

			// write to file
			try {
				if (logFile) {
					bw.write(timedMsg);
					bw.flush();
				} else {
					printBuffer.concat(msg);
				}
			} catch (IOException e) {
				System.err.println(NAME+"Failed writing to log file!");
			}
		}
	}
	
	/**
	 * Prints output (no new line) to the file and to standard output or standard error output, depending on passed value
	 * @param msg
	 * @param target
	 */
	public static void logln(String msg, LogOut target) {
		if (loggerFlag) {
			log(msg,target);

			switch (target) {
				case STDOUT:
					System.out.println();	
					break;
				case STDERR:
					System.err.println();
					break;
			}

			//Write to file
			try {
				if (logFile) {
					bw.write("\n");
					bw.flush();
				} else {
					printBuffer.concat("\n");
				}
			} catch (IOException e) {
				System.err.println(NAME+"Failed writing to log file!");
			}
		}
	}
	
	/**
	 * Prints a stack trace for an exception to the log file as well as to Standard Error Output
	 * 
	 * @param e
	 * 		The StackTraceElement array from the thread where the error occurred. 
	 */
	public static void logln(Exception e) {
		if (loggerFlag) {
			StackTraceElement[] stack = e.getStackTrace();
			
			log(">>>>>>>>>>>>>>>>>>>>>>>   LOGGING STACK TRACE   <<<<<<<<<<<<<<<<<<<<<<<<<\n", LogOut.STDERR);
			log(e.toString()+"\n", LogOut.STDERR);
			for (int i = 0; i < stack.length; i++) {
				log(stack[i].toString()+"\n", LogOut.STDERR);
			}
			
			System.err.println();
			try {
				if (logFile) {
					bw.write("\n");
					bw.flush();
				} else {
					printBuffer.concat("\n");
				}
			} catch (IOException e1) {
				System.err.println(NAME+"Failed writing to log file!");
			}
		}
	}

	/**
	 * Safely closes the file
	 */
	public static void close() {
		if (loggerFlag) {
			try {
				bw.close();
			} catch (IOException e) {
				System.err.println(NAME+"Failed closing log file!");
			}
		}
	}

	public static String getFileDirPath() {
		return fileDirPath;
	}

	public static void setFileDirPath(String fileDirPath) {
		Logger.fileDirPath = fileDirPath;
	}

	public static String getFilePrefix() {
		return filePrefix;
	}

	public static void setFilePrefix(String filePrefix) {
		Logger.filePrefix = filePrefix;
	}
}
 