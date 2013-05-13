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

import java.io.IOException;

/**
 * Resolve the location of the TreeTagger executable.
 *
 * @author Richard Eckart de Castilho
 */
public
interface ExecutableResolver
{
	/**
	 * Set platform information.
	 *
	 * @param aPlatform the platform information.
	 */
	void setPlatformDetector(
			PlatformDetector aPlatform);

	/**
	 * Destroy transient resources for the executable file. E.g. if the file
	 * was extracted to a temporary location from an archive/classpath, it can
	 * be deleted by this method.
	 */
	void destroy();

	/**
	 * Get the executable file. If necessary the file can be provided in a
	 * temporary location by this method.
	 *
	 * @return the executable file.
	 * @throws IOException if the file cannot be located/provided.
	 */
	String getExecutable()
	throws IOException ;
}
