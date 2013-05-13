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

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.CharBuffer;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Utility functions.
 *
 * @author Richard Eckart de Castilho
 */
public final
class Util
{
	private
	Util()
	{
		// No instances
	}

	/**
	 * Get the search paths for a model or executable. Using the {@code aSubPath}
	 * argument, executables and models can be searched for in different
	 * locations, e.g. executables in {@literal executable} and models in
	 * {@literal models}.
	 * <br/>
	 * The returned list contains the additional search paths, the value of the
	 * system property {@literal treetagger.home} and the
	 * environment variables {@literal TREETAGGER_HOME} and {@literal TAGDIR}
	 * in this order.
	 *
	 * @param aAdditionalPaths additional paths to search in.
	 * @param aSubPath search in the given sub-directory of the search paths.
	 * @return a list of search paths.
	 */
	public static
	List<String> getSearchPaths(
			final List<String> aAdditionalPaths,
			final String aSubPath)
	{
		final List<String> paths = new ArrayList<String>();
		paths.addAll(aAdditionalPaths);
		if (System.getProperty("treetagger.home") != null) {
			paths.add(System.getProperty("treetagger.home")+separator+aSubPath);
		}
		if (System.getenv("TREETAGGER_HOME") != null) {
			paths.add(System.getenv("TREETAGGER_HOME")+separator+aSubPath);
		}
		if (System.getenv("TAGDIR") != null) {
			paths.add(System.getenv("TAGDIR")+separator+aSubPath);
		}
//		String path = System.getenv("PATH");
//		if (path != null) {
//			paths.addAll(asList(path.split(File.pathSeparator)));
//		}
		return paths;
	}

    /**
     * Join the given strings into a single string separated by the given
     * separator.
     *
     * @param aStrings strings to join.
     * @param aSeparator a separator.
     * @return the joined string.
     */
    public static
	String join(
			final String[] aStrings,
			final String aSeparator)
	{
		final StringBuilder sb = new StringBuilder();

		for (int i = 0; i < aStrings.length; i++) {
			sb.append(aStrings[i]);
			if (i < aStrings.length - 1) {
				sb.append(aSeparator);
			}
		}

		return sb.toString();
	}

    /**
     * Join the given strings into a single string separated by the given
     * separator.
     *
     * @param aStrings strings to join.
     * @param aSeparator a separator.
     * @return the joined string.
     */
    public static
	String join(
			final Collection<String> aStrings,
			final String aSeparator)
	{
		final StringBuilder sb = new StringBuilder();

		Iterator<String> i = aStrings.iterator();
		while (i.hasNext()) {
			sb.append(i.next());
			if (i.hasNext()) {
				sb.append(aSeparator);
			}
		}

		return sb.toString();
	}

    /**
     * Checks if a token returned by TreeTagger corresponds to the token sent to TreeTagger. This
     * method does not check for strict equality, because if TreeTagger does not know a character,
     * it will return "?" instead. So this method interprets an "?" returned by TreeTagger as a
     * single-character wildcard.
     *
     * @param tokenSent token sent to TreeTagger
     * @param tokenReturned token returned from TreeTagger
     * @return if the token returned matches the token sent.
     */
    public static
    boolean matches(
    		final String tokenSent,
    		final String tokenReturned)
    {
    	if (tokenSent == null && tokenReturned == null) {
    		return true;
    	}
    	if (tokenSent == null || tokenReturned == null) {
    		return false;
    	}
    	if (tokenSent.length() != tokenReturned.length()) {
    		return false;
    	}
    	for (int i = 0; i < tokenSent.length(); i ++) {
    		if (tokenReturned.charAt(i) == '?') {
    			continue;
    		}
    		if (tokenSent.charAt(i) == tokenReturned.charAt(i)) {
    			continue;
    		}
    		return false;
    	}
    	return true;
    }

    /**
     * Close the given {@link Closeable}.
     *
     * @param aClosable a closable object.
     */
    public static
    void close(
    		final Closeable aClosable)
    {
    	if (aClosable != null) {
	    	try {
	    		aClosable.close();
	    	}
	    	catch (final IOException e) {
	    		// Ignore
	    	}
    	}
    }
    
    /**
     * For tests only.
     */
    protected static
    String readFile(
    		final File aFile, 
    		final String aEncoding)
    throws IOException
    {
    	Reader reader = null;
    	try {
        	StringBuilder sb = new StringBuilder();
        	CharBuffer buffer = CharBuffer.allocate(65535);
    		reader = new InputStreamReader(new FileInputStream(aFile), aEncoding);
    		while (reader.ready()) {
    			reader.read(buffer);
    			buffer.flip();
    			sb.append(buffer.toString());
    		}
    		return sb.toString();
    	}
    	finally {
    		close(reader);
    	}
    }

    /**
     * For tests only.
     */
    protected static
    void writeFile(
    		final String aText, 
    		final File aFile, 
    		final String aEncoding)
    throws IOException
    {
    	Writer writer = null;
    	try {
    		aFile.getParentFile().mkdirs();
        	writer = new OutputStreamWriter(new FileOutputStream(aFile), aEncoding);
        	writer.write(aText);
    	}
    	finally {
    		close(writer);
    	}
    }

    /**
     * For tests only.
     */
    protected static
    String[] tokenize(
    		final String aText, 
    		final Locale aLocale)
    {
    	List<String> tokens = new ArrayList<String>();
    	BreakIterator bi = BreakIterator.getWordInstance(aLocale);
    	bi.setText(aText);
    	int begin = 0;
    	while (bi.next() != BreakIterator.DONE) {
    		tokens.add(aText.substring(begin, bi.current()));
    		begin = bi.current();
    	}
    	return tokens.toArray(new String[tokens.size()]);
    }
}
