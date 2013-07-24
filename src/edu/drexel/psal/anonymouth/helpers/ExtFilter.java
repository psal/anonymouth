package edu.drexel.psal.anonymouth.helpers;

import java.io.File;
import java.io.FilenameFilter;

import javax.swing.filechooser.FileFilter;

/**
 * Filters what extensions JFileChoosers and FileDialogs will accept
 * @author Marc Barrowclift
 *
 */
public class ExtFilter extends FileFilter implements FilenameFilter {
	
	private String desc;
	private String[] exts;

	public ExtFilter(String desc, String[] exts) {
		this.desc = desc;
		this.exts = exts;
	}

	public ExtFilter(String desc, String ext) {
		this.desc = desc;
		this.exts = new String[] {ext};
	}

	/**
	 * Returns the description of the filter instance
	 */
	@Override
	public String getDescription() {
		return desc;
	}

	/**
	 * Used for JFileChoosers
	 */
	@Override
	public boolean accept(File f) {
		boolean result = false;
		String name = f.getName().toLowerCase();

		if (f.isDirectory()) {
			result = true;
		} else {
			for (String extension: exts) {
				if (name.endsWith(extension)) {
					result = true;
				}
			}
		}

		return result;
	}

	/**
	 * Used for FileDialogs
	 */
	@Override
	public boolean accept(File f, String name) {
		boolean result = false;
		String fileName = name.toLowerCase();

		for (String extension: exts) {
			if (fileName.endsWith(extension)) {
				result = true;
			}
		}
		
		return result;
	}
}