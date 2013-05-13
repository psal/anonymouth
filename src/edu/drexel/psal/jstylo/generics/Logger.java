package edu.drexel.psal.jstylo.generics;

import java.io.*;
import java.text.*;
import java.util.*;

public class Logger {
	
	public static final boolean loggerFlag = true;
	public static boolean logFile = false;
	
	// time
	private static SimpleDateFormat tf = new SimpleDateFormat("HH-mm-ss");
	private static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
	private static Calendar cal = null;
	
	// file
	private static String fileDirPath = "log";
	private static String filePrefix = "jstylo";
	private static String out;
	private static BufferedWriter bw = null;
	
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

	public static void initLogFile() {
		if (loggerFlag && logFile) {
			out = fileDirPath+"/"+filePrefix+"_"+date()+"_"+time()+".txt";
			String msg = "Started log "+out+"\n===================================================\n";
			try {
				if (logFile) {
					bw = new BufferedWriter(new FileWriter(out));
					bw.write(msg);
				}
			} catch (IOException e) {
				System.err.println("Failed opening log file!");
				System.exit(0);
			}
			System.out.println(msg);
		}
	}
	
	/**
	 * Enumerator for logger output.
	 */
	public enum LogOut {
		STDOUT,
		STDERR
	}
	
	public static void log(String msg) {
		if (loggerFlag) {
			if (bw == null) initLogFile();
			String timedMsg = time()+": "+msg;

			// write to screen
			System.out.print(timedMsg);
			// write to file
			try {
				if (logFile) {
					bw.write(timedMsg);
					bw.flush();
				}
			} catch (IOException e) {
				System.err.println("Failed writing to log file!");
			}
		}
	}
	
	public static void logln(String msg) {
		if (loggerFlag) {
			log(msg);
			System.out.println();
			try {
				if (logFile) {
					bw.write("\n");
					bw.flush();
				}
			} catch (IOException e) {
				System.err.println("Failed writing to log file!");
			}
		}
	}
	
	public static void log(String msg, LogOut target) {
		if (loggerFlag) {
			if (bw == null)	initLogFile();
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
				}
			} catch (IOException e) {
				System.err.println("Failed writing to log file!");
			}
		}
	}
	
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

			// write to file
			try {
				if (logFile) {
					bw.write("\n");
					bw.flush();
				}
			} catch (IOException e) {
				System.err.println("Failed writing to log file!");
			}
		}
	}

	public static void close() {
		if (loggerFlag) {
			try {
				bw.close();
			} catch (IOException e) {
				System.err.println("Failed closing log file!");
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
