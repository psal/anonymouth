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

import static java.util.Arrays.asList;
import static org.annolab.tt4j.Util.join;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Main TreeTagger wrapper class. One TreeTagger process will be created and
 * maintained for each instance of this class. The associated process will be
 * terminated and restarted automatically if the model is changed
 * ({@link #setModel(String)}). Otherwise the process remains running,
 * in the background once it is started which saves a lot of time. The process
 * remains dormant while not used and only consumes some memory, but no CPU
 * while it is not used.
 * <p>
 * During analysis, two threads are used to communicate with the TreeTagger.
 * One process writes tokens to the TreeTagger process, while the other
 * receives the analyzed tokens.
 * <p>
 * For easy integration into application, this class takes any object containing
 * token information and either uses its {@link Object#toString()} method or
 * an {@link TokenAdapter} set using {@link #setAdapter(TokenAdapter)} to extract
 * the actual token. To receive the an analyzed token, set a custom
 * {@link TokenHandler} using {@link #setHandler(TokenHandler)}.
 * <p>
 * Per default the TreeTagger executable is searched for in the directories
 * indicated by the system property {@literal treetagger.home}, the
 * environment variables {@literal TREETAGGER_HOME} and {@literal TAGDIR}
 * in this order. A full path to a model file optionally appended by a
 * {@literal :} and the model encoding is expected by the {@link #setModel(String)}
 * method.
 * <p>
 * For additional flexibility, register a custom {@link ExecutableResolver}
 * using {@link #setExecutableProvider(ExecutableResolver)} or a custom
 * {@link ModelResolver} using {@link #setModelProvider(ModelResolver)}. Custom
 * providers may extract models and executable from archives or download them
 * from some location and temporarily or permanently install them in the file
 * system. A custom model resolver may also be used to resolve a language code
 * (e.g. {@literal en}) to a particular model.
 * <p>
 * A simple illustration of how to use this class:
 * <pre>
 * TreeTaggerWrapper tt = new TreeTaggerWrapper<String>();
 * try {
 *     tt.setModel("/treetagger/models/english.par:iso8859-1");
 *     tt.setHandler(new TokenHandler<String>() {
 *         void token(String token, String pos, String lemma) {
 *             System.out.println(token+"\t"+pos+"\t"+lemma);
 *         }
 *     });
 *     tt.process(asList(new String[] {"This", "is", "a", "test", "."}));
 * }
 * finally {
 *     tt.destroy();
 * }
 * </pre>
 * @author Richard Eckart de Castilho
 *
 * @param <O> the token type.
 */
public
class TreeTaggerWrapper<O>
{
	public static boolean TRACE = false;

    private final static Pattern RE_TAB			= Pattern.compile("[\\t]");
    private final static Pattern RE_WHITESPACE	= Pattern.compile(" ");

    // A tag to identify begin/end of a text in the data flow.
    // (avoid to restart TreeTagger process each time)
    private static final String STARTOFTEXT = "<This-is-the-start-of-the-text />";
    private static final String ENDOFTEXT = "<This-is-the-end-of-the-text />";

	/**
	 *  This is the maximal token size that TreeTagger on OS X supports (empirically determined).
	 */
    public static final int MAX_POSSIBLE_TOKEN_LENGTH = 99998;

	private static Model _model = null;

	private Process _proc = null;
	private String  _procCmd = null;

	private TokenHandler<O> _handler = null;
	private ProbabilityHandler<O> _probabilityHandler = null;
	private TokenAdapter<O> _adapter = null;
	private PlatformDetector _platform = null;
	private ModelResolver _modelResolver = null;
	private ExecutableResolver _exeResolver = null;

	private Double _probabilityThreshold = null;
	private Double _epsilon = null;
	private boolean _hyphenHeuristics = false;

	private String[] _ttArgs = { "-quiet", "-no-unknown", "-sgml", "-token", "-lemma" };

	private int _numTokens = 0;
	private int _tokensWritten = 0;
	private O _lastTokenWritten;
	private int _tokensRead = 0;
	private RingBuffer _lastInToken;
	private RingBuffer _lastOutToken;
	private String _lastOutRecord;
	private int _restartCount = 0;

	private boolean _performanceMode = false;
	private boolean _strictMode = true;
	private int _maximumTokenLength = 90000;

	{
		_modelResolver = new DefaultModelResolver();
		_exeResolver = new DefaultExecutableResolver();
		setPlatformDetector(new PlatformDetector());

		if (!"false".equals(System.getProperty(getClass().getName()+".TRACE", "false"))) {
			TRACE = true;
		}
	}

	/**
	 * Disable some sanity checks, e.g. whether tokens contain line breaks
	 * (which is not allowed). Turning this on will increase your performance,
	 * but the wrapper may throw exceptions if illegal data is provided.
	 *
	 * @param performanceMode on/off.
	 */
	public
	void setPerformanceMode(
			final boolean performanceMode)
	{
		_performanceMode = performanceMode;
	}

	/**
	 * Get performance mode state.
	 *
	 * @return performance mode state.
	 */
	public
	boolean getPerformanceMode()
	{
		return _performanceMode;
	}

	/**
	 * Set the maximal number of characters allowed in a token. The maximal supported token length
	 * is determined by {@link #MAX_POSSIBLE_TOKEN_LENGTH} and the length set is automatically
	 * capped to that number. Note that this is the size in byte, not the size in characters.
	 *
	 * @param maximumTokenLength the maximal number of bytes allowed in a token.
	 */
	public
	void setMaximumTokenLength(
			final int maximumTokenLength)
	{
		_maximumTokenLength = Math.min(maximumTokenLength, MAX_POSSIBLE_TOKEN_LENGTH);
	}

	/**
	 * Get the maximum number of bytes allowed in a token.
	 *
	 * @return the maximum number of bytes allowed in a token.
	 */
	public
	int getMaximumTokenLength()
	{
		return _maximumTokenLength;
	}

	/**
	 * Set the strict mode. In this mode an {@link IllegalArgumentException} is thrown when the
	 * token sent to TreeTagger and the token returned from it are not equal. Since the TreeTagger
	 * returns "?" for characters it does not know, the question mark is interpreted as a wild
	 * card when testing for equality.
	 *
	 * @param strictMode on/off.
	 */
	public
	void setStrictMode(
			final boolean strictMode)
	{
		_strictMode = strictMode;
	}

	/**
	 * Get the strict mode state.
	 *
	 * @return strict mode state.
	 */
	public
	boolean isStrictMode()
	{
		return _strictMode;
	}

	/**
	 * Set the arguments that are passed to the TreeTagger executable. A call
	 * to this method will cause a running TreeTagger process to be shut down
	 * and restarted with the new arguments.
	 *
	 * Using this method can cause TT4J to not work any longer. TTJ4 expects
	 * that TreeTagger prints a set of line each containing three tokens
	 * separated by spaces.
	 *
	 * @param aArgs the arguments.
	 */
	public
	void setArguments(
			String[] aArgs)
	{
		_ttArgs = aArgs;
		stopTaggerProcess();
	}

	public
	String[] getArguments() {
		return _ttArgs;
	}

	/**
	 * Set minimal tag frequency to {@code epsilon}
	 *
	 * @param aEpsilon epsilon.
	 */
	public
	void setEpsilon(
			final Double aEpsilon)
	{
		_epsilon = aEpsilon;
		stopTaggerProcess();
	}

	/**
	 * Get minimal tag frequency.
	 *
	 * @return epsilon.
	 */
	public
	Double getEpsilon()
	{
		return _epsilon;
	}
	
	public 
	Double getProbabilityThreshold()
    {
        return _probabilityThreshold;
    }

    /**
     * Print all tags of a word with a probability higher than X times the largest probability.
     * Setting this to {@code null} or to a negative value disables the output of probabilities.
     * Per default this is disabled.
     * 
     * @param aProbabilityThreshold threshold X.
     */
    public 
    void setProbabilityThreshold(
            final Double aThreshold)
    {
        if (aThreshold != null && aThreshold < 0.0) {
            _probabilityThreshold = null;
        }
        else {
            _probabilityThreshold = aThreshold;
        }
        stopTaggerProcess();
    }

    /**
	 * Turn on the heuristics fur guessing the parts of speech of unknown hyphenated words.
	 *
	 * @param hyphenHeuristics use hyphen heuristics.
	 */
	public
	void setHyphenHeuristics(
			boolean hyphenHeuristics)
	{
		_hyphenHeuristics = hyphenHeuristics;
		stopTaggerProcess();
	}

	/**
	 * Get hyphen heuristics mode setting.
	 *
	 * @return whether to use hyphen heuristics
	 */
	public
	boolean getHyphenHeuristics()
	{
		return _hyphenHeuristics;
	}

	/**
	 * Set a custom model resolver.
	 *
	 * @param aModelProvider a model resolver.
	 */
	public
	void setModelProvider(
			final ModelResolver aModelProvider)
	{
		_modelResolver = aModelProvider;
		_modelResolver.setPlatformDetector(_platform);
	}

	/**
	 * Get the current model resolver.
	 *
	 * @param aModelProvider a model resolver.
	 */
	public ModelResolver getModelResolver()
	{
		return _modelResolver;
	}

	/**
	 * Set a custom executable resolver.
	 *
	 * @param aExeProvider a executable resolver.
	 */
	public
	void setExecutableProvider(
			final ExecutableResolver aExeProvider)
	{
		_exeResolver = aExeProvider;
		_exeResolver.setPlatformDetector(_platform);
	}

	/**
	 * Get the current executable resolver.
	 *
	 * @return the current executable resolver.
	 */
	public
	ExecutableResolver getExecutableProvider()
	{
		return _exeResolver;
	}

	/**
	 * Set a {@link TokenHandler} to receive the analyzed tokens.
	 *
	 * @param aHandler a token handler.
	 */
	public
	void setHandler(
			final TokenHandler<O> aHandler)
	{
		_handler = aHandler;
		_probabilityHandler = aHandler instanceof ProbabilityHandler ? 
		        (ProbabilityHandler<O>) aHandler : null;
	}

	/**
	 * Get the current token handler.
	 *
	 * @return current token handler.
	 */
	public
	TokenHandler<O> getHandler()
	{
		return _handler;
	}

	/**
	 * Set a {@link TokenAdapter} used to extract the token string from
	 * a token objects passed to {@link #process(Collection)}. If no adapter
	 * is set, the {@link Object#toString()} method is used.
	 *
	 * @param aAdapter the adapter.
	 */
	public
	void setAdapter(
			final TokenAdapter<O> aAdapter)
	{
		_adapter = aAdapter;
	}

	/**
	 * Get the current token adapter.
	 *
	 * @return the current token adapter.
	 */
	public
	TokenAdapter<O> getAdapter()
	{
		return _adapter;
	}

	/**
	 * Set platform information. Also sets the platform information in
	 * the model resolver and the executable resolver.
	 *
	 * @param aPlatform the platform information.
	 */
	public
	void setPlatformDetector(
			final PlatformDetector aPlatform)
	{
		_platform = aPlatform;
		if (_modelResolver != null) {
			_modelResolver.setPlatformDetector(aPlatform);
		}
		if (_exeResolver != null) {
			_exeResolver.setPlatformDetector(aPlatform);
		}
	}

	/**
	 * Get platform information.
	 *
	 * @return the platform information.
	 */
	public
	PlatformDetector getPlatformDetector()
	{
		return _platform;
	}

    /**
	 * Load the model with the given name.
	 *
	 * @param modelName the name of the model.
	 * @throws IOException if the model can not be found.
	 */
	public
	void setModel(
			final String modelName)
	throws IOException
	{
		// If this model is already set, do nothing.
		if (_model != null && _model.getName().equals(modelName)) {
			return;
		}

		stopTaggerProcess();

		// If the previous model was temporary, we have to clean it up
		if (_model != null) {
			_model.destroy();
		}

		if (modelName != null) {
			_model = _modelResolver.getModel(modelName);
		}
		else {
			_model = null;
		}
	}

	/**
	 * Get the currently set model.
	 *
	 * @return the current model.
	 */
	public
	Model getModel()
	{
		return _model;
	}

	/**
	 * Stop the TreeTagger process and clean up the model and executable.
	 */
	public
	void destroy()
	{
		// Clear the model resources
		try {
			setModel(null);
		}
		catch (final IOException e) {
			// Ignore
		}

		// Clear the executable
    	if (_exeResolver != null) {
    		_exeResolver.destroy();
    	}
	}

	@Override
	protected
	void finalize()
	throws Throwable
	{
		destroy();
		super.finalize();
	}

	/**
	 * Process the given array of token objects.
	 *
	 * @param aTokens the token objects.
	 * @throws IOException if there is a problem providing the model or executable.
	 * @throws TreeTaggerException if there is a problem communication with TreeTagger.
	 */
	public
	void process(
			final O[] aTokenList)
	throws IOException, TreeTaggerException
	{
		process(asList(aTokenList));
	}
	
	/**
	 * Process the given list of token objects.
	 *
	 * @param aTokens the token objects.
	 * @throws IOException if there is a problem providing the model or executable.
	 * @throws TreeTaggerException if there is a problem communication with TreeTagger.
	 */
	public
	void process(
			final Collection<O> aTokenList)
	throws IOException, TreeTaggerException
	{
		// In normal more sort out all tokens that we cannot handle. In
		// particular line breaks and tabs cannot be handled by TreeTagger.
		Collection<O> aTokens;
		if (!_performanceMode) {
			aTokens = removeProblematicTokens(aTokenList);
		}
		else {
			aTokens = aTokenList;
		}

		// Remember the number of tokens we originally got.
		_numTokens = aTokens.size();
		_tokensRead = 0;
		_tokensWritten = 0;
		_lastInToken = new RingBuffer(10);
		_lastOutToken = new RingBuffer(10);
		_lastOutRecord = null;
		_lastTokenWritten = null;

		final Process taggerProc = getTaggerProcess();

		// One thread reads the output.
		final Reader reader = new Reader(
				taggerProc.getInputStream(), aTokens.iterator());
		final Thread readerThread = new Thread(reader);
		readerThread.setName("TT4J StdOut Reader");
		readerThread.start();

		// One thread consumes stderr so we do not get a deadlock.
		final StreamGobbler gob = new StreamGobbler(taggerProc.getErrorStream());
		Thread errorGobblerThread = new Thread(gob);
		errorGobblerThread.setName("TT4J StdErr Reader");
		errorGobblerThread.start();

		// Now we can start writing.
		final Writer writer = new Writer(aTokens.iterator());
		Thread writerThread = new Thread(writer);
		writerThread.setName("TT4J StdIn Writer");
		writerThread.start();

		// Wait for the processing to end. Every once in a while we check if an
		// exception has been thrown. When the Reader thread is complete, we can
		// stop.
		try {
			// Wait for the Reader thread to end.
			synchronized (reader) {
				while (readerThread.getState() != State.TERMINATED) {
					try {
						// If the reader or writer fail, we kill the TreeTagger and bail
						// out. This may be a bit harsh, but easier than coding the
						// Reader and Writer so that we can abort them. If the process
						// is dead, the streams die and then the threads will also die
						// with an IOException.
						checkThreads(reader, writer, gob);

						reader.wait(20);
					}
					catch (final InterruptedException e) {
						// Ignore
					}
				}
				// At the end make sure that no thread exited with an exception
				checkThreads(reader, writer, gob);
			}
		}
		finally {
			gob.done();
		}

//		info("Parsed " + count + " pos segments");
	}

	private
	void checkThreads(
			final Reader reader,
			final Writer writer,
			final StreamGobbler gobbler)
	throws TreeTaggerException
	{
		if (gobbler.getException() != null) {
			destroy();
			throw new TreeTaggerException(gobbler.getException());
		}

		if (writer.getException() != null) {
			destroy();
			throw new TreeTaggerException(writer.getException());
		}

		if (reader.getException() != null) {
			destroy();
			throw new TreeTaggerException(reader.getException());
		}
	}

	/**
	 * Filter out tokens that cause problems when communicating with the TreeTagger process.
	 *
	 * @param tokenList the original list of tokens.
	 * @return the filtered list of tokens.
	 */
	protected
	Collection<O> removeProblematicTokens(
			Collection<O> tokenList)
	throws UnsupportedEncodingException
	{
		Collection<O> filtered = new ArrayList<O>(tokenList.size());
		Iterator<O> i = tokenList.iterator();
		boolean skipped = true;
		String text = null;
		skipToken: while (i.hasNext()) {
			if (TRACE && skipped && text != null) {
				System.err.println("["+TreeTaggerWrapper.this+
						"|TRACE] Skipping illegal token ["+text+"]");
			}

			skipped = true;
			O token = i.next();
			text = getText(token);
			if (text == null) {
				continue;
			}
			// Check if the encoded string may be longer than the maximal allowed size. We expect
			// that the String might at worst grow to 4 times its size because a character in UTF-8
			// can become at most 4 bytes.
			if (text.length() > (_maximumTokenLength >> 2)) {
				if (text.getBytes(_model.getEncoding()).length >= _maximumTokenLength) {
					continue;
				}
			}

			boolean isUnicode = "UTF-8".equals(_model.getEncoding().toUpperCase(Locale.US));
			boolean onlyWhitespace = true;
			// Check if the token contains characters that break the communication with the
			// TreeTagger process
			for (int n = 0; n < text.length(); n++) {
				// If the model does not use a Unicode encoding, high unicode characters cause
				// problems.
				int cp = text.codePointAt(n);
				if (!isUnicode) {
					// Cannot deal with Unicode > 16 bit if not in Unicode mode
					if (cp >= 0x10000) continue skipToken; 
				}
				char c = text.charAt(n);
				if (c >= 0x0000 && c <= 0x001B) continue skipToken; // CONTROL CHARACTERS
				if (onlyWhitespace) {
					onlyWhitespace &= Character.isWhitespace(c);
				}
			}

			if (onlyWhitespace) {
				continue skipToken;
			}

			filtered.add(token);
			skipped = false;
		}
		return filtered;
	}

	/**
     * Start tagger process.
     *
     * @return
     * @throws IOException
     */
    private
    Process getTaggerProcess()
    throws IOException
    {
    	if (_proc == null) {
        	_model.install();

//			info("Starting treetagger: " + _procCmd);
			List<String> cmd = new ArrayList<String>();
			cmd.add(_exeResolver.getExecutable());
			for (String arg : _ttArgs) {
				cmd.add(arg);
			}

			if (_epsilon != null) {
				cmd.add("-eps");
				cmd.add(String.format(Locale.US, "%.12f", _epsilon));
			}

	         if (_probabilityThreshold != null) {
                cmd.add("-prob");
                cmd.add("-threshold");
                cmd.add(String.format(Locale.US, "%.12f", _probabilityThreshold));
            }

			if (_hyphenHeuristics) {
				cmd.add("-hyphen-heuristics");
			}

			cmd.add(_model.getFile().getAbsolutePath());
			_procCmd = join(cmd, " ");
			
			if (TRACE) {
				System.err.println("[" + TreeTaggerWrapper.this
						+ "|TRACE] Invoking TreeTagger [" + _procCmd + "]");
			}


			final ProcessBuilder pb = new ProcessBuilder();
			pb.command(cmd);
			_proc = pb.start();
			_restartCount++;
    	} else {
//    		info("Re-using treetagger: " + _procCmd);
    	}
    	return _proc;
    }

    /**
     * Kill tagger process.
     */
    private
    void stopTaggerProcess()
    {
    	if (_proc != null) {
	    	_proc.destroy();
	    	_proc = null;
	    	_procCmd = null;
	    	// getContext().getLogger().log(Level.INFO, "Stopped TreeTagger sub-process");
    	}
    }

    private
	String getText(
			final O o)
	{
		if (_adapter == null) {
			return o.toString();
		}
		else {
			return _adapter.getText(o);
		}
	}

    public
    String getStatus()
    {
		StringBuilder sb = new StringBuilder();
		try {
			int status = _proc.exitValue();
			sb.append("TreeTagger process: exited with status ").append(status).append('\n');
		}
		catch (IllegalThreadStateException e) {
			sb.append("TreeTagger process: still running.\n");
		}

		sb.append("Last " + _lastOutToken.size() + " tokens sent: ");
		if (_lastInToken != null) {
			sb.append("[").append(_lastOutToken).append("]");
		}
		else {
			sb.append("none");
		}
		sb.append('\n');
		
		sb.append("Last token sent (#").append(_tokensWritten).append("): ");
		if (_lastTokenWritten != null) {
			sb.append("[").append(getText(_lastTokenWritten)).append("]");
		}
		else {
			sb.append("none");
		}
		sb.append('\n');

		sb.append("Last " + _lastInToken.size() + " tokens read: ");
		if (_lastInToken != null) {
			sb.append("[").append(_lastInToken).append("]");
		}
		else {
			sb.append("none");
		}
		sb.append('\n');

		sb.append("Last record read (#").append(_tokensRead).append("): ");
		if (_lastOutRecord != null) {
			sb.append("[").append(_lastOutRecord+"]");
		}
		else {
			sb.append("none");
		}
		sb.append('\n');

		sb.append("Tokens originally recieved: ").append(_numTokens).append('\n');
		sb.append("Tokens written            : ").append(_tokensWritten).append('\n');
		sb.append("Tokens read               : ").append(_tokensRead).append('\n');

		return sb.toString();
    }

    /**
     * Get the number of times a TreeTagger process was started.
     *
     * @return the number of times a TreeTagger process was started.
     */
    public
    int getRestartCount()
	{
		return _restartCount;
	}

    private
    class StreamGobbler
    implements Runnable
    {
    	private final InputStream in;
    	private boolean done = false;
		private Throwable _exception;

    	public
    	StreamGobbler(
    			final InputStream aIn)
    	{
			in = aIn;
		}

    	public
    	void done()
    	{
    		done = true;
    	}

    	public
    	void run()
    	{
    		StringBuilder sb = new StringBuilder();
    		byte[] buffer = new byte[1024];
    		try {
	    		while(!done) {
    				while (in.available() > 0) {
    					in.read(buffer, 0, Math.min(buffer.length, in.available()));
    					sb.append(new String(buffer));
    				}
	    			Thread.sleep(100);
	    		}
    		}
    		catch (final Throwable e) {
    			System.out.println("Last seen from TreeTagger ["+sb+"]");
    			_exception = e;
    		}
    	}

    	public
    	Throwable getException() {
			return _exception;
		}
}

	private
    class Reader
    implements Runnable
    {
		private final Iterator<O> tokenIterator;
		private final BufferedReader in;
		private final InputStream ins;
		private Throwable _exception;

    	public
    	Reader(
    			final InputStream aIn,
    			final Iterator<O> aTokenIterator)
    	throws UnsupportedEncodingException
		{
    		ins = aIn;
    		in = new BufferedReader(new InputStreamReader(
    			    ins, _model.getEncoding()));
    		tokenIterator = aTokenIterator;
		}

    	public
    	void run()
    	{
    		try {
	    		String outRecord;
	    		boolean inText = false;
	    		while (true) {
	    			outRecord = in.readLine();

	    			if (outRecord == null) {
						throw new IOException(
								"The TreeTagger process has died:\n" + getStatus() +
								"\nMake sure the following comand (in parentheses) works when " +
								"running it from the command line: [echo \"test\" | " +
								_procCmd + "]");
	    			}

	    			outRecord = outRecord.trim();

	    			if (STARTOFTEXT.equals(outRecord)) {
	    				inText = true;
						if (TRACE) {
							System.err.println("["+TreeTaggerWrapper.this+
									"|TRACE] ("+_tokensRead+") START ["+outRecord+"]");
						}
	    				continue;
	    			}

	    			if (ENDOFTEXT.equals(outRecord)) {
						if (TRACE) {
							System.err.println("["+TreeTaggerWrapper.this+
									"|TRACE] ("+_tokensRead+") COMPLETE ["+outRecord+"]");
						}
	    				break;
	    			}

	    			if (inText) {
	    				// Get word and tag
	    				String outToken = null;

						// Sometimes TT seems to return odd lines, e.g. containing only a tag but no
						// token and no lemma. For such cases we only return the original token we
						// got, but lemma and pos will be null.
	    				String fields1[] = RE_TAB.split(outRecord, _probabilityThreshold != null ? 0 : 2);
	    				if (fields1.length > 0) {
	    					outToken = fields1[0];
	    				}
	    				
	                    // Record what we have sent - getNextToken uses this when throwing an
                        // exception.
                        _lastOutToken.add(outToken);
                        _lastOutRecord = outRecord;

                        // Get original token segment
                        O inToken = getNextToken(outToken);

	    				// If a pos and lemma is present, return them.
	    				for (int n = 1; n < fields1.length; n++) {
							String fields2[] = _probabilityThreshold != null ? RE_WHITESPACE
									.split(fields1[n]) : RE_TAB.split(fields1[n]);
		    				try {
	                            String posTag = fields2[0].trim().intern();
	                            String lemma  = fields2[1].trim();
	                            String prob = _probabilityThreshold != null ? fields2[2] : null;
	
	                            if (TRACE) {
	                                System.err.println(" -- POS: [" + posTag + "] -- LEMMA: ["
	                                        + lemma + "] -- PROBABILITY: [" + prob + "]");
	                            }
	                            
	                            // Notify the handler for the token and the best tag/lemma
	                            if (_handler != null && n == 1) {
	                                _handler.token(inToken, posTag, lemma);
	                            }
	                            
	                            // If probabilities are provided and a handler for them is present
	                            // then notify the probability handler
	                            if (prob != null && _probabilityHandler != null) {
	                                _probabilityHandler.probability(posTag, lemma, Double.valueOf(prob));
	                            }
	                            else {
	                            	// If there is no probability handler, then we do not have to
	                            	// process all the fields.
	                            	break;
	                            }
		    				}
		    				catch (Throwable e) {
								throw new TreeTaggerException(
										"Unable to parse pos/lemma/probability from [" + fields1[n]
												+ "] in [" + _lastOutRecord + "]", e);
		    				}
	    				}
	    			}
	    		}
    		}
    		catch (TreeTaggerException e) {
    			_exception = e;
    		}
    		catch (final IOException e) {
				_exception = e;
    		}
    		catch (final Throwable e) {
				_exception = new TreeTaggerException("Unable to process record [" + _lastOutRecord
						+ "]", e);
    		}

    		synchronized (this) {
        		notifyAll();
			}
    	}

    	private 
    	O getNextToken(
    	        final String aOutToken)
    	{
            // Get original token segment
            if (tokenIterator.hasNext()) {
                O inToken = tokenIterator.next();
                _tokensRead++;
                
                final String inTokenText = getText(inToken);
                _lastInToken.add(inTokenText);
 
                if (_strictMode) {
                    if (!Util.matches(inTokenText, aOutToken)) {
                        throw new IllegalStateException("[" + TreeTaggerWrapper.this
                                + "] Token stream out of sync.\n" + getStatus());
                    }
                }
                
                if (TRACE) {
                    System.err.print("[" + TreeTaggerWrapper.this + "|TRACE] ("
                            + _tokensRead + ") IN [" + inTokenText + "] -- OUT: ["
                            + aOutToken + "]");
                }
                
                return inToken;
            }
            else {
                throw new IllegalStateException("[" + TreeTaggerWrapper.this
                        + "] Have not seen ENDOFTEXT-marker but no more "
                        + "tokens are available.\n" + "TT returned: [" + _lastOutRecord
                        + "]\n" + getStatus());
            }
    	}
    	
    	public
    	Throwable getException() {
			return _exception;
		}
    }

	private
    class Writer
    implements Runnable
    {
		private final Iterator<O> tokenIterator;
		private Throwable _exception;
		private PrintWriter _pw;

    	public
    	Writer(
    			final Iterator<O> aTokenIterator)
		{
    		tokenIterator = aTokenIterator;
		}

    	public
    	void run()
    	{
    		try {
    			final OutputStream os = _proc.getOutputStream();

    			_pw = new PrintWriter(new BufferedWriter(
    			    new OutputStreamWriter(os, _model.getEncoding())));

    			send(STARTOFTEXT);

    			while (tokenIterator.hasNext()) {
    				O token = tokenIterator.next();
    				_lastTokenWritten = token;
    				_tokensWritten++;
    				send(getText(token));
    			}

    			send(ENDOFTEXT);
				send("\n.\n"+_model.getFlushSequence()+".\n.\n.\n.\n");
    		}
    		catch (final Throwable e) {
    			_exception = e;
    		}
    	}

    	private
    	void send(
    			final String line)
    	{
    		_pw.println(line);
//    		System.out.println("--> "+line);
    		_pw.flush();
    	}

    	public
    	Throwable getException()
		{
			return _exception;
		}
    }
}
