package edu.drexel.psal.anonymouth.helpers;

import edu.drexel.psal.jstylo.generics.Logger;
import edu.drexel.psal.jstylo.generics.Logger.LogOut;
import java.awt.FileDialog;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

/**
 * Exactly what the name says, it makes loading files less of a pain in the ass.
 *
 * @author Marc Barrowclift
 */
public class FileHelper {

	private static final String NAME = "( FileHelper ) - ";
	/**
	 * Default, standard, kinda shitty file chooser for Java. Use only when
	 * necessary.
	 */
	public static JFileChooser load = new JFileChooser();

	public static JFileChooser save = new JFileChooser();
	/**
	 * To use the FileDialogs, you should initialize them in the constructors of the
	 * classes you desire them to be used in since they take a second or two to be
	 * created (so this will be taken care of on start up and not slow the user down
	 * on run time) and since they require you to pass the parent frame or dialog
	 * upon initialization (so it can be modular).
	 *
	 * <p>
	 * Because of this, if you require more than one load or save FileDialog in a
	 * specific class you will have to declare your own for each additional one.
	 */
	public static FileDialog goodLoad; // Whenever you want to load something, use this

	public static FileDialog goodSave; // Whenever you want to save something, use this

	/**
	 * Validates that the required jsan_resources directory and critical files
	 * exist. Shows user-friendly error message and exits if resources are missing.
	 *
	 * @return true if all required resources exist, false otherwise
	 */
	public static boolean validateRequiredResources() {
		String[] requiredFiles = {"./jsan_resources/words.txt", "./jsan_resources/abbreviations.txt",
				"./jsan_resources/feature_sets", "./jsan_resources/corpora",};

		ArrayList<String> missingFiles = new ArrayList<String>();

		for (String filePath : requiredFiles) {
			File file = new File(filePath);
			if (!file.exists()) {
				missingFiles.add(filePath);
			}
		}

		if (!missingFiles.isEmpty()) {
			StringBuilder errorMessage = new StringBuilder();
			errorMessage.append("Anonymouth cannot start because required resources are missing:\n\n");

			for (String missingFile : missingFiles) {
				errorMessage.append("• ").append(missingFile).append("\n");
			}

			errorMessage.append("\nPlease ensure you are running Anonymouth from the correct directory\n");
			errorMessage.append("where the 'jsan_resources' folder is located.\n\n");
			errorMessage.append("Expected structure:\n");
			errorMessage.append("  your-directory/\n");
			errorMessage.append("  ├── anonymouth-0.5.0-jar-with-dependencies.jar\n");
			errorMessage.append("  └── jsan_resources/\n");
			errorMessage.append("      ├── words.txt\n");
			errorMessage.append("      ├── abbreviations.txt\n");
			errorMessage.append("      ├── feature_sets/\n");
			errorMessage.append("      └── corpora/\n");

			Logger.logln(NAME + "Missing required resources - application cannot continue", LogOut.STDERR);

			JOptionPane.showMessageDialog(null, errorMessage.toString(), "Missing Required Resources",
					JOptionPane.ERROR_MESSAGE, UIManager.getIcon("OptionPane.errorIcon"));

			return false;
		}

		return true;
	}

	/**
	 * Creates and returns a ready BufferedReader instance to read. Returns null if
	 * file was not found or another error occurred.
	 *
	 * @param path
	 *            - The absolute path to the file you want to read
	 * @return
	 */
	public static BufferedReader loadFile(String path) {
		BufferedReader reader = null;

		try {
			reader = new BufferedReader(new FileReader(path));
		} catch (FileNotFoundException e) {
			Logger.logln(NAME + "Error loading file at path: " + path + ", file was not found", LogOut.STDERR);
		} catch (Exception e) {
			Logger.logln(NAME + "Unknown error occurred loading file at path: " + path);
		}

		return reader;
	}

	/**
	 * Reads through a given file and returns a string version of it (null if file
	 * was not found or another error occurred).
	 *
	 * @param path
	 *            The absolute path to the file
	 * @return
	 */
	public static String readFile(String path) {
		String contents = null;

		try {
			BufferedReader reader = new BufferedReader(new FileReader(path));

			String line = "";
			while ((line = reader.readLine()) != null) {
				contents = contents + line;
			}
			reader.close();
		} catch (FileNotFoundException e) {
			Logger.logln(NAME + "Error loading file at path: " + path + ", file was not found", LogOut.STDERR);
		} catch (Exception e) {
			Logger.logln(NAME + "Unknown error occurred loading file at path: " + path);
		}

		return contents;
	}

	/**
	 * Serves a pretty specific purpose now, but hopefully it will prove useful in
	 * the future. This reads a given file line by line, and for each line it reads
	 * it in as a string and adds it to a HashSet of strings and then returns the
	 * final result at end of file.
	 *
	 * <p>
	 * This is to be used for when you want to look up stuff (In my case, dictionary
	 * words) in constant time, and that stuff is in a file that needs to be read
	 * in. If no document is found at the path or there's another error an empty
	 * HashSet is returned.
	 *
	 * @param path
	 *            The absolute path to the file
	 * @return
	 */
	public static HashSet<String> hashSetFromFile(String path) {
		HashSet<String> tokens = new HashSet<String>();

		try {
			BufferedReader reader = new BufferedReader(new FileReader(path));

			String line = "";
			while ((line = reader.readLine()) != null) {
				tokens.add(line);
			}
			reader.close();
		} catch (FileNotFoundException e) {
			Logger.logln(NAME + "Error loading file at path: " + path + ", file was not found", LogOut.STDERR);
		} catch (Exception e) {
			Logger.logln(NAME + "Unknown error occurred loading file at path: " + path);
		}

		return tokens;
	}

	/**
	 * Serves a pretty specific purpose now, but hopefully it will prove useful in
	 * the future. This reads a given file line by line, and for each line it reads
	 * it in as a string and adds it to an ArrayList of String and returns the list
	 * at the end of the file.
	 *
	 * @param path
	 *            The absolute path to the file
	 * @param expectedSize
	 *            The expected number of lines the file contains (if you want to
	 *            ensure the list has no null indices pass 1, otherwise just pass
	 *            some arbitrarily large number, depending on how many lines you
	 *            expect).
	 * @return
	 */
	public static ArrayList<String> arrayListFromFile(String path, int expectedSize) {
		ArrayList<String> lines = new ArrayList<String>(expectedSize);

		try {
			BufferedReader reader = new BufferedReader(new FileReader(path));

			String line = "";
			while ((line = reader.readLine()) != null) {
				lines.add(line);
			}
			reader.close();
		} catch (FileNotFoundException e) {
			Logger.logln(NAME + "Error loading file at path: " + path + ", file was not found", LogOut.STDERR);
		} catch (Exception e) {
			Logger.logln(NAME + "Unknown error occurred loading file at path: " + path);
		}

		return lines;
	}

	/**
	 * Writes the contents of the passed String to the given path. If something goes
	 * wrong, it will display an error message and print problem to the Logger.
	 *
	 * @param path
	 *            The absolute path you want to save your file to
	 * @param textToSave
	 *            The text you want to write out in the form of a String
	 */
	public static void writeToFile(String path, String textToSave) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(path));
			bw.write(textToSave);
			bw.flush();
			bw.close();
			Logger.log(NAME + "Saved contents of document to " + path);
		} catch (IOException exc) {
			Logger.logln(NAME + "Failed opening " + path + " for writing", LogOut.STDERR);
			JOptionPane.showMessageDialog(null,
					"Anonymouth ran into an issue saving document to:\n" + path
							+ "\nBe sure that you have proper permissions or try\n"
							+ "saving to a different directory instead.",
					"Save Error", JOptionPane.ERROR_MESSAGE, UIManager.getIcon("OptionPane.errorIcon"));
		}
	}
}
