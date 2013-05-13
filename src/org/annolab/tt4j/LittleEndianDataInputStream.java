/*******************************************************************************
 * Public Domain
 * 
 * Source: http://www.peterfranza.com/2008/09/26/little-endian-input-stream/
 *
 * You just saved me tons of time, can we consider this code public domain?
 *      Comment by Matt on April 21, 2012 @ 7:08 am
 *
 * Yes. Please use it however you wish.
 *      Comment by pfranza on April 21, 2012 @ 7:13 am
 *
 * Contributors:
 *     Peter Franza               - initial implementation
 ******************************************************************************/
package org.annolab.tt4j;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public 
class LittleEndianDataInputStream
extends InputStream
implements DataInput
{
	// to get at high level readFully methods of DataInputStream
	private DataInputStream d;
	
	// to get at the low-level read methods of InputStream
	private InputStream in;
	
	private byte w[]; // work array for buffering input
	
	public 
	LittleEndianDataInputStream(
			final InputStream aIn)
	{
		in = aIn;
		d = new DataInputStream(in);
		w = new byte[8];
	}

	@Override
	public 
	int available()
	throws IOException
	{
		return d.available();
	}

	public final 
	short readShort()
	throws IOException
	{
		d.readFully(w, 0, 2);
		return (short) ((w[1] & 0xff) << 8 | (w[0] & 0xff));
	}

	/**
	 * Note, returns int even though it reads a short.
	 */
	public final 
	int readUnsignedShort()
	throws IOException
	{
		d.readFully(w, 0, 2);
		return ((w[1] & 0xff) << 8 | (w[0] & 0xff));
	}

	/**
	 * like DataInputStream.readChar except little endian.
	 */
	public final 
	char readChar()
	throws IOException
	{
		d.readFully(w, 0, 2);
		return (char) ((w[1] & 0xff) << 8 | (w[0] & 0xff));
	}

	/**
	 * like DataInputStream.readInt except little endian.
	 */
	public final 
	int readInt()
	throws IOException
	{
		d.readFully(w, 0, 4);
		return (w[3]) << 24 | (w[2] & 0xff) << 16 | (w[1] & 0xff) << 8 | (w[0] & 0xff);
	}

	/**
	 * like DataInputStream.readLong except little endian.
	 */
	public final 
	long readLong()
	throws IOException
	{
		d.readFully(w, 0, 8);
		return (long) (w[7]) << 56 | (long) (w[6] & 0xff) << 48 | (long) (w[5] & 0xff) << 40
				| (long) (w[4] & 0xff) << 32 | (long) (w[3] & 0xff) << 24
				| (long) (w[2] & 0xff) << 16 | (long) (w[1] & 0xff) << 8 | (long) (w[0] & 0xff);
	}

	public final 
	float readFloat()
	throws IOException
	{
		return Float.intBitsToFloat(readInt());
	}

	public final 
	double readDouble()
	throws IOException
	{
		return Double.longBitsToDouble(readLong());
	}

	@Override
	public final 
	int read(
			final byte b[], 
			final int off, 
			final int len)
	throws IOException
	{
		return in.read(b, off, len);
	}

	public final 
	void readFully(
			final byte b[])
	throws IOException
	{
		d.readFully(b, 0, b.length);
	}

	public final 
	void readFully(
			final byte b[], 
			final int off, 
			final int len)
	throws IOException
	{
		d.readFully(b, off, len);
	}

	public final 
	int skipBytes(
			final int n)
	throws IOException
	{
		return d.skipBytes(n);
	}

	public final 
	boolean readBoolean()
	throws IOException
	{
		return d.readBoolean();
	}

	public final 
	byte readByte()
	throws IOException
	{
		return d.readByte();
	}

	@Override
	public 
	int read()
	throws IOException
	{
		return in.read();
	}

	public final 
	int readUnsignedByte()
	throws IOException
	{
		return d.readUnsignedByte();
	}

	@Deprecated
	public final 
	String readLine()
	throws IOException
	{
		return d.readLine();
	}

	public final 
	String readUTF()
	throws IOException
	{
		return d.readUTF();
	}

	@Override
	public final
	void close()
	throws IOException
	{
		d.close();
	}
}