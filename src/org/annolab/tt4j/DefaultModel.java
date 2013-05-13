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

public
class DefaultModel
implements Model
{
	private String _encoding;
	private File _file;
	// Issue 6 - We need the "()" to flush properly when using the chinese model
	private String _flushSequence = ".\n.\n.\n.\n(\n)\n";
	private String _name;

	public
	DefaultModel(
			final String aName,
			final File aFile,
			final String aEncoding)
	{
		_name = aName;
		_file = aFile;
		_encoding = aEncoding;
	}

	public
	void destroy()
	{
		// Do nothing
	}

	public
	String getEncoding()
	{
		return _encoding;
	}

	public
	File getFile()
	{
		return _file;
	}

	public
	String getFlushSequence()
	{
		return _flushSequence;
	}

	public
	String getName()
	{
		return _name;
	}

	public
	void install()
	throws IOException
	{
		// Do nothing
	}
}
