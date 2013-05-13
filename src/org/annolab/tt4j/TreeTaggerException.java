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
 * Exception throw if an error occurs while tagging is in process.
 *
 * @author Richard Eckart de Castilho
 */
public
class TreeTaggerException
extends Exception
{
	private static final long serialVersionUID = -862590343816183238L;

	/**
	 * New exception.
	 *
	 * @param aMessage a message.
	 */
	public
	TreeTaggerException(
			final String aMessage)
	{
		super(aMessage);
	}

	/**
	 * New exception.
	 *
	 * @param aCause a causing exception.
	 */
	public
	TreeTaggerException(
			final Throwable aCause)
	{
		super(aCause);
	}

	/**
	 * New exception.
	 *
	 * @param aMessage a message.
	 * @param aCause a causing exception.
	 */
	public
	TreeTaggerException(
			final String aMessage,
			final Throwable aCause)
	{
		super(aMessage, aCause);
	}
}
