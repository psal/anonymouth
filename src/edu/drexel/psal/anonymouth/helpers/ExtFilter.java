package edu.drexel.psal.anonymouth.helpers;

import java.io.File;
import javax.swing.filechooser.FileFilter;

public class ExtFilter extends FileFilter {
	
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

	@Override
	public String getDescription() {
		return desc;
	}

	@Override
	public boolean accept(File f) {
		if (f.isDirectory()) return true;
		String path = f.getAbsolutePath().toLowerCase();
		for (String extension: exts) {
			if ((path.endsWith(extension) &&
					(path.charAt(path.length() - extension.length() - 1)) == '.'))
				return true;
		}
		return false;
	}
}
