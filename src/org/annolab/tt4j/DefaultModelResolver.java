/*******************************************************************************
 * Copyright (c) 2009-2010 Richard Eckart de Castilho.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 *
 * Contributors:
 *     Richard Eckart de Castilho - initial API and implementation
 ******************************************************************************/
package org.annolab.tt4j;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.io.File.separator;
import static org.annolab.tt4j.Util.*;

/**
 * Simple model provider. The model name is actually the path to the model.
 * The path has to be followed by a ":" and the name model encoding. Example
 * {@code /usr/lib/model.par:UTF-8}.
 *
 * @author Richard Eckart de Castilho
 */
public
class DefaultModelResolver
implements ModelResolver
{
	protected PlatformDetector _platform;

	protected List<String> _additionalPaths = new ArrayList<String>();

	// This is for debug purposes only!
	boolean _checkExistence = true;

	/**
	 * Set additional paths that will be used for searching the TreeTagger
	 * executable.
	 *
	 * @param aAdditionalPaths list of additional paths.
	 * @see Util#getSearchPaths(List, String)
	 */
	public
	void setAdditionalPaths(
			final List<String> aAdditionalPaths)
	{
		_additionalPaths.clear();
		_additionalPaths.addAll(aAdditionalPaths);
	}

	/**
	 * Get platform information.
	 *
	 * @return platform information.
	 */
	public
	PlatformDetector getPlatformDetector()
	{
		return _platform;
	}

	public
	Model getModel(
			final String aModelName)
	throws IOException
	{
		final String _encoding;
		final String _name;

		int lastColon = aModelName.lastIndexOf(':');
		// On Windows we can have paths like "C:\model.par". Checking this by
		// testing if the char following the colon is a slash or backslash. The
		// encoding should definitely not start with one of these. Should also
		// catch URLs.
		if (aModelName.length() > (lastColon+1)) {
			char ch = aModelName.charAt(lastColon+1);
			if (ch == '/' || ch == '\\') {
				lastColon = -1;
			}

			_encoding =  (lastColon != -1) ? aModelName.substring(lastColon+1) : "UTF-8";
			// The using the name as path
			_name = (lastColon != -1) ? aModelName.substring(0, lastColon) : aModelName;
		}
		else {
			_encoding = "UTF-8";
			// The using the name as path
			// Nothing is following the final colon, so we truncate it.
			_name = aModelName.substring(0, aModelName.length()-1);
		}

		return getModel(aModelName, _name, _encoding);
	}

	public
	Model getModel(
			final String aModelName,
			final String aLocation,
			final String aEncoding)
	throws IOException
	{
		File _file = new File(aLocation);

		if (_checkExistence && !_file.exists()) {
			boolean found = false;
			Set<String> searchedIn = new HashSet<String>();
			for (final String p : getSearchPaths(_additionalPaths, "models")) {
				if (p == null) {
					continue;
				}

				_file = new File(p+separator+aLocation);
				searchedIn.add(_file.getAbsolutePath());
				if (_file.exists()) {
					found = true;
					break;
				}
			}

			if (!found) {
				throw new IOException("Unable to locate model ["+aLocation+"] in the following " +
						"locations "+searchedIn+".  Make sure the environment variable " +
						"'TREETAGGER_HOME' or 'TAGDIR' or the system property 'treetagger.home' " +
						"point to the TreeTagger installation directory.");
			}
		}

		return new DefaultModel(aModelName, _file, aEncoding);
	}

	public
	void setPlatformDetector(
			final PlatformDetector aPlatform)
	{
		_platform = aPlatform;
	}
}
