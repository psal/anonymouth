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

/**
 * Adapter to extract a token from the list of objects passed to
 * {@link TreeTaggerWrapper#process(java.util.Collection)}.
 *
 * @author Richard Eckart de Castilho
 *
 * @param <O> the type of object containing the token information.
 */
public
interface TokenAdapter<O>
{
	/**
	 * Extract the token string from the given object.
	 *
	 * @param object and object containing token information.
	 * @return the token string.
	 */
	String getText(
			O object);
}
