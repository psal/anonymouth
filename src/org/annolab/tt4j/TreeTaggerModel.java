/*******************************************************************************
 * Copyright (c) 2012 Richard Eckart de Castilho.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 *
 * Contributors:
 *     Richard Eckart de Castilho - initial API and implementation
 ******************************************************************************/
package org.annolab.tt4j;

import java.nio.ByteOrder;
import java.util.List;

/**
 * TreeTagger model data. This class currently supports only a subset of the information contained
 * in the TreeTagger model files.
 * 
 * @author Richard Eckart de Castilho
 */
public class TreeTaggerModel
{
	public static final int VERSION_3_1 = 0x1F;
	public static final int VERSION_3_2 = 0x20;
	
	private String source;
	private ByteOrder byteOrder;
	private int version;
	private List<String> tags;
	private List<String> lemmas;
	private List<String> tokens;

	public String getSource()
	{
		return source;
	}

	public void setSource(String aSource)
	{
		source = aSource;
	}

	public ByteOrder getByteOrder()
	{
		return byteOrder;
	}

	public void setByteOrder(ByteOrder aByteOrder)
	{
		byteOrder = aByteOrder;
	}

	public int getVersion()
	{
		return version;
	}

	public void setVersion(int aVersion)
	{
		version = aVersion;
	}

	public List<String> getTags()
	{
		return tags;
	}

	public void setTags(List<String> aTags)
	{
		tags = aTags;
	}

	public List<String> getLemmas()
	{
		return lemmas;
	}

	public void setLemmas(List<String> aLemmas)
	{
		lemmas = aLemmas;
	}

	public List<String> getTokens()
	{
		return tokens;
	}

	public void setTokens(List<String> aTokens)
	{
		tokens = aTokens;
	}
}
