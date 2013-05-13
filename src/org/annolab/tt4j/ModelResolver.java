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
 * Resolve the location of the TreeTagger model.
 *
 * @author Richard Eckart de Castilho
 */
public
interface ModelResolver
{
	/**
	 * Set platform information.
	 *
	 * @param aPlatform the platform information.
	 */
	void setPlatformDetector(
			PlatformDetector aPlatform);

	/**
	 * Load the model with the given name.
	 *
	 * @param modelName the name of the model.
	 * @return the model.
	 * @throws IOException if the model can not be found.
	 */
	Model getModel(
			String modelName)
	throws IOException;
}
