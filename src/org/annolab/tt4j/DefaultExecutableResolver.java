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

import static java.io.File.separator;
import static org.annolab.tt4j.Util.getSearchPaths;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Assume that TreeTagger is installed and available in the path.
 *
 * @author Richard Eckart de Castilho
 */
public
class DefaultExecutableResolver
implements ExecutableResolver
{
	protected PlatformDetector _platform;
	protected List<String> _additionalPaths = new ArrayList<String>();

	public
	void destroy()
	{
		// Do nothing
	}

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

	public
	String getExecutable()
	throws IOException
	{
		Set<String> searchedIn = new HashSet<String>();
		for (final String p : getSearchPaths(_additionalPaths, "bin")) {
			if (p == null) {
				continue;
			}

			final File exe = new File(p+separator+"tree-tagger"+_platform.getExecutableSuffix());
			searchedIn.add(exe.getAbsolutePath());
			if (exe.exists()) {
				return exe.getAbsolutePath();
			}
		}

		throw new IOException("Unable to locate tree-tagger binary in the following locations " +
				searchedIn + ". Make sure the environment variable 'TREETAGGER_HOME' or " +
				"'TAGDIR' or the system property 'treetagger.home' point to the TreeTagger " +
				"installation directory.");
	}

	/**
	 * Set platform information.
	 */
	public
	void setPlatformDetector(
			final PlatformDetector aPlatform)
	{
		_platform = aPlatform;
	}
}
