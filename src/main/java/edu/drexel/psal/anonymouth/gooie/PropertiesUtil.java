package edu.drexel.psal.anonymouth.gooie;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.*;

import edu.drexel.psal.ANONConstants;
import edu.drexel.psal.jstylo.generics.Logger.LogOut;
import edu.drexel.psal.jstylo.generics.*;

public class PropertiesUtil {
	
	private static final String NAME = "( PropertiesUtil ) - ";
	private static final int[][] COLORS = {
		{255,255,0,128},	//Yellow
		{255,128,0,80},	//Orange
		{0,128,255,80},		//Blue
		{128,0,255,80}};	//Purple

	protected static final String propFileName = ANONConstants.EXTERNAL_RESOURCE_PACKAGE+"anonymouth_prop.prop";
	protected static File propFile = new File(propFileName);
	protected static Properties prop = new Properties();
	protected static String defaultClass = "SMO";
	protected static String defaultFeat = "WritePrints (Limited)";
	protected static String defaultHighlightColor = "0";
	protected static int defaultClient = 0;
	protected static String defaultFontSize = "14";
	protected static String defaultProbSet = "";
	protected static Boolean defaultAutoSave = false;
	protected static Boolean defaultWarnQuit = false;
	protected static Boolean defaultBarTutorial = true;
	protected static Boolean defaultHighlightSents = true;
	protected static Boolean defaultAutoHighlight = true;
	protected static Boolean defaultVersionAutoSave = false;
	protected static Boolean defaultFilterAddSuggestions = true;
	protected static int defaultThreads = 4;
	protected static int defaultFeatures = 500;
	protected static Boolean defaultTranslation = false;
	
	/**
	 * Resets all values in the prop file to their default values, thereby erasing all the user's changes.
	 */
	protected static void reset() {
		//general
		setWarnQuit(defaultWarnQuit);
		setAutoSave(defaultAutoSave);
		setDoTranslations(defaultTranslation);
		
		//editor
		setFontSize(defaultFontSize);
		setAutoHighlight(defaultAutoHighlight);
		
		//advanced
		setThreadCount(defaultThreads);
		setMaximumFeatures(defaultFeatures);
		setVersionAutoSave(defaultVersionAutoSave);
	}
	
	/**
	 * Sets whether or not to filter words to add suggestions
	 * @param filter
	 */
	protected static void setFilterAddSuggestions(Boolean filter) {
		BufferedWriter writer;
		
		try {
			prop.setProperty("filterAddSuggestions", filter.toString());
			writer = new BufferedWriter(new FileWriter(propFileName));
			prop.store(writer, "User Preferences");
			writer.close();
		} catch (Exception e) {
			Logger.logln(NAME+"Failed setting words to remove filter preference", LogOut.STDERR);
		}
	}
	
	/**
	 * Gets whether or not to filter words to remove
	 * @return
	 */
	public static boolean getFilterAddSuggestions() {
		String filter;
		
		try {
			filter = prop.getProperty("filterAddSuggestions");
			if (filter == null) {
				prop.setProperty("filterAddSuggestions", defaultFilterAddSuggestions.toString());
				filter = prop.getProperty("filterAddSuggestions");
			}
		} catch (NullPointerException e) {
			prop.setProperty("filterAddSuggestions", defaultFilterAddSuggestions.toString());
			filter = prop.getProperty("filterAddSuggestions");
		}
		
		if (filter.equals("true"))
			return true;
		else
			return false;
	}
	
	/**
	 * Sets the color to highlight sentences with (must be the index, with respect to the COLORS array in PreferencesWindow)
	 * @param color
	 */
	protected static void setHighlightColor(int color) {
		BufferedWriter writer;
		
		try {
			prop.setProperty("highlightColor", String.valueOf(color));
			writer = new BufferedWriter(new FileWriter(propFileName));
			prop.store(writer, "User Preferences");
			writer.close();
		} catch (Exception e) {
			Logger.logln(NAME+"Failed setting highlight color", LogOut.STDERR);
		}
	}
	
	/**
	 * Gets a color object of the saved color to highlight sentences with (for use when actually creating the highlighter)
	 * @return
	 */
	public static Color getHighlightColor() {
		String highlightColor;
		
		try {
			highlightColor = prop.getProperty("highlightColor");
			if (highlightColor == null) {
				prop.setProperty("highlightColor", defaultHighlightColor);
				highlightColor = prop.getProperty("highlightColor");
			}
		} catch (NullPointerException e) {
			prop.setProperty("highlightColor", defaultHighlightColor);
			highlightColor = prop.getProperty("highlightColor");
		}
		
		int[] rgba = COLORS[Integer.parseInt(highlightColor)];
		return new Color(rgba[0],rgba[1],rgba[2],rgba[3]);
	}
	
	/**
	 * Gets the index of the the saved color to highlight sentences with (the index is with respect to the PreferencesWindow COLORS array)
	 * @return
	 */
	public static int getHighlightColorIndex() {
		String highlightColorIndex;
		
		try {
			highlightColorIndex = prop.getProperty("highlightColor");
			if (highlightColorIndex == null) {
				prop.setProperty("highlightColor", defaultHighlightColor);
				highlightColorIndex = prop.getProperty("highlightColor");
			}
		} catch (NullPointerException e) {
			prop.setProperty("highlightColor", defaultHighlightColor);
			highlightColorIndex = prop.getProperty("highlightColor");
		}
		
		return Integer.parseInt(highlightColorIndex);
	}
	
	/**
	 * Sets whether or not to highlight the currently selected sentence
	 * @param highlightSents
	 */
	protected static void setHighlightSents(Boolean highlightSents) {
		BufferedWriter writer;
		
		try {
			prop.setProperty("highlightSents", highlightSents.toString());
			writer = new BufferedWriter(new FileWriter(propFileName));
			prop.store(writer, "User Preferences");
			writer.close();
		} catch (Exception e) {
			Logger.logln(NAME + "Failed setting highlight sentences", LogOut.STDERR);
		}
	}
	
	/**
	 * Gets whether or not to highlight the currently selected sentence
	 * @return
	 */
	protected static boolean getHighlightSents() {
		String highlightSents;
		
		try {
			highlightSents = prop.getProperty("highlightSents");
			if (highlightSents == null) {
				prop.setProperty("highlightSents", defaultHighlightSents.toString());
				highlightSents = prop.getProperty("highlightSents");
			}
		} catch (NullPointerException e) {
			prop.setProperty("highlightSents", defaultHighlightSents.toString());
			highlightSents = prop.getProperty("highlightSents");
		}
		
		if (highlightSents.equals("true"))
			return true;
		else
			return false;
	}
	
	/**
	 * Sets whether or not to save a version of the document each time the user processes
	 * @param versionAutoSave
	 */
	protected static void setVersionAutoSave(Boolean versionAutoSave) {
		BufferedWriter writer;
		
		try {
			prop.setProperty("versionAutoSave", versionAutoSave.toString());
			writer = new BufferedWriter(new FileWriter(propFileName));
			prop.store(writer, "User Preferences");
			writer.close();
		} catch (Exception e) {
			Logger.logln(NAME + "Failed setting version auto save", LogOut.STDERR);
		}
	}
	
	/**
	 * Gets whether or not to save a version of the document each time the user processes
	 */
	protected static boolean getVersionAutoSave() {
		String versionAutoSave;
		
		try {
			versionAutoSave = prop.getProperty("versionAutoSave");
			if (versionAutoSave == null) {
				prop.setProperty("versionAutoSave", defaultVersionAutoSave.toString());
				versionAutoSave = prop.getProperty("versionAutoSave");
			}
		} catch (NullPointerException e) {
			prop.setProperty("versionAutoSave", defaultVersionAutoSave.toString());
			versionAutoSave = prop.getProperty("versionAutoSave");
		}
		
		if (versionAutoSave.equals("true"))
			return true;
		else
			return false;
	}
	
	/**
	 * Sets whether or not to automatically highlight words to remove in a given sentence
	 * @param highlight
	 */
	protected static void setAutoHighlight(Boolean highlight) {
		BufferedWriter writer;
		
		try {
			prop.setProperty("autoHighlight", highlight.toString());
			writer = new BufferedWriter(new FileWriter(propFileName));
			prop.store(writer, "User Preferences");
			writer.close();
		} catch (Exception e) {
			Logger.logln(NAME + "Failed setting automatic highlights", LogOut.STDERR);
		}
	}
	
	/**
	 * Gets whether or not to automatically highlight words to remove in a given sentence
	 * @return
	 */
	protected static boolean getAutoHighlight() {
		String highlight = "";
		
		try {
			highlight = prop.getProperty("autoHighlight");
			if (highlight == null) {
				prop.setProperty("autoHighlight", defaultAutoHighlight.toString());
				highlight = prop.getProperty("autoHighlight");
			}
		} catch (NullPointerException e) {
			prop.setProperty("autoHighlight", defaultAutoHighlight.toString());
			highlight = prop.getProperty("autoHighlight");
		}
		
		if (highlight.equals("true"))
			return true;
		else
			return false;
	}
	
	/**
	 * Sets whether or not to display the anonymity bar and results tutorial after processing (so that it's only shown once)
	 * @param barTutorial - whether or not to display the tutorial
	 */
	protected static void setBarTutorial(Boolean barTutorial) {
		BufferedWriter writer;
		
		try {
			prop.setProperty("barTutorial", barTutorial.toString());
			writer = new BufferedWriter(new FileWriter(propFileName));
			prop.store(writer, "User Preferences");
			writer.close();
		} catch (Exception e) {
			Logger.logln(NAME + "Failed setting bar tutorial", LogOut.STDERR);
		}
	}
	
	/**
	 * Gets whether or not to display the anonymity bar tutorial after processing
	 * @return
	 */
	protected static boolean showBarTutorial() {
		String barTutorial = "";
		
		try {
			barTutorial = prop.getProperty("barTutorial");
			if (barTutorial == null) {
				prop.setProperty("barTutorial", defaultBarTutorial.toString());
				barTutorial = prop.getProperty("barTutorial");
			}
		} catch (NullPointerException e) {
				prop.setProperty("barTutorial", defaultBarTutorial.toString());
				barTutorial = prop.getProperty("barTutorial");
		}
		
		if (barTutorial.equals("true"))
			return true;
		else
			return false;
	}
	
	/**
	 * Sets the font size for the document editor
	 * @param fontSize - The point-size desired for the font.
	 */
	protected static void setFontSize(String fontSize) {
		BufferedWriter writer;
		
		try {
			prop.setProperty("fontSize", fontSize);
			writer = new BufferedWriter(new FileWriter(propFileName));
			prop.store(writer, "User Preferences");
			writer.close();
		} catch (Exception e) {
			Logger.logln(NAME + "Failed setting the font size", LogOut.STDERR);
		}
	}
	
	/**
	 * Gets the font size used in the document editor
	 * @return
	 */
	protected static int getFontSize() {
		String fontSize;
		
		try {
			fontSize = prop.getProperty("fontSize");
			if (fontSize == null) {
				prop.setProperty("fontSize", defaultFontSize);
				fontSize = prop.getProperty("fontSize");
			}
		} catch (NullPointerException e) {
			prop.setProperty("fontSize", defaultFontSize);
			fontSize = prop.getProperty("fontSize");
		}
		
		return Integer.parseInt(fontSize);
	}
	
	/**
	 * Sets the index of the current client to use for the next time Anonymouth loads.
	 * @param client - the index of the client wanted.
	 */
	public static void setCurrentClient(int client) {
		BufferedWriter writer;
		
		try {
			prop.setProperty("curClient", Integer.toString(client));
			writer = new BufferedWriter(new FileWriter(propFileName));
			prop.store(writer, "User Preferences");
			writer.close();
		} catch (Exception e) {
			Logger.logln(NAME + "Failed setting the current client", LogOut.STDERR);
		}
	}
	
	/**
	 * Gets the index of the current client to use for translations
	 * @return
	 */
	public static int getCurrentClient() {
		String client;
		
		try {
			client = prop.getProperty("curClient");
			if (client == null) {
				prop.setProperty("curClient", Integer.toString(defaultClient));
				client = prop.getProperty("curClient");
			}
		} catch (NullPointerException e) {
			prop.setProperty("curClient", Integer.toString(defaultClient));
			client = prop.getProperty("curClient");
		}
		
		return Integer.parseInt(client);
	}
	
	/**
	 * Sets the client availability
	 * @param availability - An ArrayList<String> containing one of two values for each client: Either the string "ready" or a date,
	 * meaning that the client is not ready and will only be available after the specified date.
	 */
	public static void setClientAvailability(ArrayList<String> availability) {
		BufferedWriter writer;
		
		try {
			prop.setProperty("availability", availability.toString());
			writer = new BufferedWriter(new FileWriter(propFileName));
			prop.store(writer, "User Preferences");
			writer.close();
		} catch (Exception e) {
			Logger.logln(NAME + "Failed setting client availability", LogOut.STDERR);
		}
	}
	
	/**
	 * Returns the client availability list used for translations.
	 * @return Unlike every other get method here, this return value can be empty if the developer hadn't first set the availability (Since
	 * there is no "default" availability value to fall back on)
	 */
	public static ArrayList<String> getClientAvailability() {
		ArrayList<String> availability = new ArrayList<String>(10);
		
		try {
			String[] temp = prop.getProperty("availability").split(",");
			if (temp != null) {
				for (int i = 0; i < temp.length; i++) {
					if (i == 0) {
						String avail = temp[i].trim();
						avail = avail.substring(1, avail.length());
						availability.add(avail);
					} else if (i == temp.length-1) {
						String avail = temp[i].trim();
						avail = avail.substring(0, avail.length()-1);
						availability.add(avail);
					} else {
						availability.add(temp[i].trim());
					}
				}
			}
		} catch (NullPointerException e) {}
		
		return availability;
	}
	
	/**
	 * Sets the user's translate preference.
	 * @param translate - whether or not the user wants translations
	 */
	protected static void setDoTranslations(Boolean translate) {
		BufferedWriter writer;
		try {
			prop.setProperty("translate", translate.toString());
			writer = new BufferedWriter(new FileWriter(propFileName));
			prop.store(writer, "User Preferences");
			writer.close();
		} catch (Exception e) {
			Logger.logln(NAME + "Failed setting translations on/off", LogOut.STDERR);
		}
	}
	
	/**
	 * Gets the user's translate preference
	 * @return
	 */
	public static boolean getDoTranslations() {
		String translate = "";
		
		try {
			translate = prop.getProperty("translate");
			if (translate == null) {
				prop.setProperty("translate", defaultTranslation.toString());
				translate = prop.getProperty("translate");
			}
		} catch (NullPointerException e) {
			prop.setProperty("translate", defaultTranslation.toString());
			translate = prop.getProperty("translate");
		}
		
		if (translate.equals("true"))
			return true;
		else
			return false;
	}
	
	/**
	 * Sets the user's thread count preference.
	 * @param threads - the maximum number of threads the user wants used
	 */
	protected static void setThreadCount(int threads) {
		BufferedWriter writer;
		try {
			prop.setProperty("numOfThreads", Integer.toString(threads));
			writer = new BufferedWriter(new FileWriter(propFileName));
			prop.store(writer, "User Preferences");
			writer.close();
			
			ThePresident.num_Tagging_Threads = threads;
		} catch (Exception e) {
			Logger.logln(NAME + "Failed setting thread count", LogOut.STDERR);
		}
	}
	
	/**
	 * Gets the user's preferred thread count
	 * @return 
	 */
	protected static int getThreadCount() {
		String threads = "";
		try {
			threads = prop.getProperty("numOfThreads");
			if (threads == null) {
				prop.setProperty("numOfThreads", Integer.toString(defaultThreads));
				threads = prop.getProperty("numOfThreads");
			}
		} catch (NullPointerException e) {
			prop.setProperty("numOfThreads", Integer.toString(defaultThreads));
			threads = prop.getProperty("numOfThreads");
		}
		
		return Integer.parseInt(threads);
	}
	
	/**
	 * Sets the user's feature count preference.
	 * @param max - the maximum number of features the user wants used
	 */
	protected static void setMaximumFeatures(int max) {
		BufferedWriter writer;
		try {
			prop.setProperty("maxFeatures", Integer.toString(max));
			writer = new BufferedWriter(new FileWriter(propFileName));
			prop.store(writer, "User Preferences");
			writer.close();
			
			ThePresident.max_Features_To_Consider = max;
		} catch (Exception e) {
			Logger.logln(NAME + "Failed setting maximum features", LogOut.STDERR);
		}
	}
	
	/**
	 * Gets the user's preferred feature count.
	 * @return
	 */
	protected static int getMaximumFeatures() {
		String max = "";
		try {
			max = prop.getProperty("maxFeatures");
			if (max == null) {
				prop.setProperty("maxFeatures", Integer.toString(defaultFeatures));
				max = prop.getProperty("maxFeatures");
			}
		} catch (NullPointerException e) {
			prop.setProperty("maxFeatures", Integer.toString(defaultFeatures));
			max = prop.getProperty("maxFeatures");
		}
		
		return Integer.parseInt(max);
	}
	
	/**
	 * Sets the user's preference on having a warning upon exit.
	 * @param b - whether or not the user wants to be warned upon exit of unsaved changes
	 */
	protected static void setWarnQuit(Boolean b) {
		BufferedWriter writer;
		try {
			prop.setProperty("warnQuit", b.toString());
			writer = new BufferedWriter(new FileWriter(propFileName));
			prop.store(writer, "User Preferences");
			writer.close();
		} catch (Exception e) {
			Logger.logln(NAME+"Failed setting warn on quit", LogOut.STDERR);
		}
	}
	
	/**
	 * Gets the user's warn on quit preference
	 * @return
	 */
	public static boolean getWarnQuit() {
		String warnQuit = "";
		try {
			warnQuit = prop.getProperty("warnQuit");
			if (warnQuit == null) {
				prop.setProperty("warnQuit", defaultWarnQuit.toString());
				warnQuit = prop.getProperty("warnQuit");
			}
		} catch (NullPointerException e) {
			prop.setProperty("warnQuit", defaultWarnQuit.toString());
			warnQuit = prop.getProperty("warnQuit");
		}
		
		if (warnQuit.equals("true"))
			return true;
		else
			return false;
	}
	
	/**
	 * Sets the user's preference on auto-saving
	 * @param b - whether or not the user wants their document to be auto-saved
	 */
	protected static void setAutoSave(Boolean b) {
		BufferedWriter writer;
		try {
			prop.setProperty("autoSave", b.toString());
			writer = new BufferedWriter(new FileWriter(propFileName));
			prop.store(writer, "User Preferences");
			writer.close();
			
			ThePresident.autosave_Latest_Version = b;
		} catch (Exception e) {
			Logger.logln(NAME+"Failed setting auto save", LogOut.STDERR);
		}
	}
	
	/**
	 * Gets the user's preference on auto-saving
	 * @return
	 */
	public static boolean getAutoSave() {
		String autoSave = "";
		try {
			autoSave = prop.getProperty("autoSave");
			if (autoSave == null) {
				prop.setProperty("autoSave", defaultAutoSave.toString());
				autoSave = prop.getProperty("autoSave");
			}
		} catch (NullPointerException e) {
				prop.setProperty("autoSave", defaultAutoSave.toString());
				autoSave = prop.getProperty("autoSave");
		}
		
		if (autoSave.equals("true"))
			return true;
		else
			return false;
	}
	
	/**
	 * Sets the user's preference of problem set path.
	 * @param probSet - path to the default problem set.
	 */
	protected static void setProbSet(String probSet)
	{
		BufferedWriter writer;
		try {
			prop.setProperty("recentProbSet", probSet);
			writer = new BufferedWriter(new FileWriter(propFileName));
			prop.store(writer, "User Preferences");
			writer.close();
		} catch (Exception e) {
			Logger.logln(NAME+"Failed setting problem set \""+probSet+"\"", LogOut.STDERR);
		}
	}
	
	/**
	 * Gets the path to the default problem set.
	 * 
	 * @return probSet
	 * 		Path to the default problem set, returns "" if none exists.
	 */
	protected static String getProbSet() {
		String probSet = "";
		try {
			probSet = prop.getProperty("recentProbSet");
			if (probSet == null) {
				prop.setProperty("recentProbSet", defaultProbSet);
				probSet = prop.getProperty("recentProbSet");
			}
		} catch (NullPointerException e) {
			prop.setProperty("recentProbSet", defaultProbSet);
			probSet = prop.getProperty("recentProbSet");
		}
		
		return probSet;
	}
	
	/**
	 * Sets the user's feature preference that will load up on start up.
	 * @param feature - name of the desired feature.
	 */
	protected static void setFeature(String feature) {
		// saves the currently selected feature to the properties file
		BufferedWriter writer;
		try {
			prop.setProperty("recentFeat", feature);
			writer = new BufferedWriter(new FileWriter(propFileName));
			prop.store(writer, "User Preferences");
			writer.close();
			
			Logger.logln(NAME+"Saving new preference: feature = " + feature);
		} catch (Exception e) {
			Logger.logln(NAME+"Failed saving new Feature preference \"" + feature + "\"", LogOut.STDERR);
		}
	}
	
	/**
	 * Gets the user's feature preference.
	 * @return feature - name of the default feature.
	 */
	protected static String getFeature() {
		String feature = "";
		try {
			feature = prop.getProperty("recentFeat");
			if (feature == null) {
				prop.setProperty("recentFeat", defaultFeat);
				feature = prop.getProperty("recentFeat");
			}
		} catch (NullPointerException e) {
			Logger.logln(NAME+"RecentFeat not set, default value \"" + defaultFeat + "\" used", LogOut.STDOUT);
			prop.setProperty("recentFeat", defaultFeat);
			feature = prop.getProperty("recentFeat", defaultFeat);
		}
		
		return feature;
	}
	
	/**
	 * Sets the user's classifier preference that will load up on start up.
	 * @param classifier - name of the desired classifier
	 */
	protected static void setClassifier(String classifier) {
		// saves the currently selected classifier to the properties file
		BufferedWriter writer;
		try {
			prop.setProperty("recentClass", classifier);
			writer = new BufferedWriter(new FileWriter(propFileName));
			prop.store(writer, "User Preferences");
			writer.close();
			
			Logger.logln(NAME+"Saving new preference: classifier = " + classifier);
		} catch (Exception e) {
			Logger.logln(NAME+"Failed saving new Classifier preference \"" + classifier + "\"", LogOut.STDERR);
		}
	}
	
	/**
	 * Gets the user's classifier preference.
	 * @return classifier - name of the default classifier.
	 */
	protected static String getClassifier() {
		String classifier = "";
		try {
			classifier = prop.getProperty("recentClass");
			if (classifier == null) {
				prop.setProperty("recentClass", defaultClass);
				classifier = prop.getProperty("recentClass");
			}
		} catch (NullPointerException e) {
			Logger.logln(NAME+"RecentClass not set, default value \"" + defaultClass + "\" used", LogOut.STDOUT);
			prop.setProperty("recentClass", defaultClass);
			classifier = prop.getProperty("recentClass", defaultClass);
		}
		
		return classifier;
	}
}
