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

import static org.annolab.tt4j.TreeTaggerModel.VERSION_3_1;
import static org.annolab.tt4j.TreeTaggerModel.VERSION_3_2;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * Reader for TreeTagger model files. 
 * 
 * @author Richard Eckart de Castilho
 */
public class TreeTaggerModelReader
{
	private String charsetName = "UTF-8";
	private InputStream inStream;
	private DataInput in;
	
	private boolean readDictionary = true;
		
	/**
	 * Read the model from a stream.
	 * 
	 * @param aIn an input stream.
	 * @return the decoded model.
	 * @throws IOException
	 */
	public TreeTaggerModel read(InputStream aIn) throws IOException
	{
		TreeTaggerModel model = new TreeTaggerModel();
		
		inStream = aIn;
		in = new DataInputStream(inStream);
		
		try {
			int version = in.readInt();
			
			// Test big-endian
			if (!decodeVersion(model, version, ByteOrder.BIG_ENDIAN)) {
				// Test little-endian
				if (decodeVersion(model, Integer.reverseBytes(version), ByteOrder.LITTLE_ENDIAN)) {
					in = new LittleEndianDataInputStream(aIn);
				}
				else {
					throw new IllegalStateException("Unknown version or file format");
				}
			}
			
			// Read rest of the header data
			int numberOfTags = -1;
			
			switch (model.getVersion()) {
			case VERSION_3_1:
				in.readInt(); // Unknown
				numberOfTags = in.readInt(); // Number of tags
				break;
			case VERSION_3_2:
				in.readInt(); // Unknown
				in.readInt(); // Unknown
				numberOfTags = in.readInt(); // Number of tags
				break;
			}
			
			// Read tags
			model.setTags(readStrings(numberOfTags));
			
			if (readDictionary) {
				// Read lemma dictionary size
				int lemmaSize = in.readInt();
				model.setLemmas(readStrings(lemmaSize));

				// Read token dictionary size
				int tokenSize = in.readInt();
				
				assert 0xFFFFFFFE == in.readInt(); // Assert marker
				assert 0x00 == in.readByte(); // Assert end of block
	
				// Read unknown block
				int c1 = in.readInt(); // Read block size
				for (int c1i = 0; c1i < c1; c1i ++) {
					in.readInt(); // Unknown
					in.readInt(); // Unknown
					in.readInt(); // Unknown
				}
				in.readInt(); // Unknown
				assert 0x00 == in.readByte(); // Assert end of block
	
				// Read unknown block
				int c2 = in.readInt(); // Read block size
				in.readInt(); // Unknown
				for (int band = 0; band < 3; band ++) {
					for (int c2i = 0; c2i < c2; c2i ++) {
						in.readInt(); // Unknown
					}
				}
	
				List<String> tokens = new ArrayList<String>();
				for (int ct = 0; ct < tokenSize; ct ++) {
					String token = readZeroTerminatedString(charsetName);
					tokens.add(token);
					
					// Read token data
					int bsize = in.readInt(); // Block size size
					in.readInt(); // Unknown
					for (int cb = 0; cb < bsize; cb++) {
						in.readInt(); // Unknown
						in.readInt(); // Unknown
						in.readInt(); // Unknown
					}
				}
				model.setTokens(tokens);
			}

			return model;
		}
		finally {
			inStream = null;
			in = null;
		}
	}
	
	protected List<String> readStrings(int aCount) throws IOException
	{
		List<String> tags = new ArrayList<String>();
		
		for (int i = 0; i < aCount; i++) {
			String tag = readZeroTerminatedString(charsetName);
			tags.add(tag);
		}
		
		return tags;
	}
	
	protected boolean decodeVersion(TreeTaggerModel aModel, int aVersion, ByteOrder aByteOrder)
	{
		switch (aVersion) {
		case VERSION_3_1:
			// Fall-through
		case VERSION_3_2:
			aModel.setVersion(aVersion);
			aModel.setByteOrder(aByteOrder);
			return true;
		}
		
		return false;
	}
	
	protected String readZeroTerminatedString(String aCharsetName)
		throws IOException
	{
		return new String(readZeroTerminatedByteArray(), aCharsetName);
	}
	
	protected byte[] readZeroTerminatedByteArray() throws IOException
	{
		int bytesRead = 0;
		byte[] buffer = new byte[128];
		int b;
		int i = 0;
		while ((b = inStream.read()) != -1) {
			// Finished
			if (b == 0) {
				// Shrink buffer
				byte[] buf = buffer;
				buffer = new byte[bytesRead];
				System.arraycopy(buf, 0, buffer, 0, bytesRead);
				return buffer;
			}
			
			// Extend buffer
			if (i == buffer.length) {
				byte[] buf = buffer;
				buffer = new byte[buf.length + 128];
				System.arraycopy(buf, 0, buffer, 0, buf.length);
				i = 0;
			}
			
			buffer[i] = (byte) b;
			bytesRead++;
			i++;
		}
		
		throw new IOException("Unexpected end of file.");
	}

	/**
	 * Get the encoding used for reading the dictionary. This information need to be provided
	 * externally, it is not present in the TreeTagger model file. Per default, the UTF-8
	 * character set is used. 
	 */
	public String getEncoding()
	{
		return charsetName;
	}

	/**
	 * Set the encoding used by the dictionary.
	 */
	public void setEncoding(String aCharsetName)
	{
		charsetName = aCharsetName;
	}

	/**
	 * Check if the dictionary is read or skipped. Per default the dictionary is read. 
	 */
	public boolean isReadDictionary()
	{
		return readDictionary;
	}

	/**
	 * Set if the dictionary is read or skipped.
	 */
	public void setReadDictionary(boolean aReadDictionary)
	{
		readDictionary = aReadDictionary;
	}
}
