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

import java.nio.ByteOrder;

/**
 * Detect platform information and normalize it.
 *
 * @author Richard Eckart de Castilho
 */
public
class PlatformDetector
{
	public static String OS_WINDOWS   = "windows";
	public static String OS_OSX       = "osx";
	public static String OS_SOLARIS   = "solaris";
	public static String OS_LINUX     = "linux";

	public static String ARCH_PPC     = "ppc";
	public static String ARCH_X86_32  = "x86_32";
	public static String ARCH_X86_64  = "x86_64";
	public static String ARCH_SPARC   = "sparc";

	private String _arch = "";
	private String _os = "";
	private String _executableSuffix = "";
	private ByteOrder _byteOrder = ByteOrder.nativeOrder();
	private String[] _chmodCmd;

	{
		updatePlatform(
				System.getProperties().getProperty("os.name"),
				System.getProperties().getProperty("os.arch"),
				ByteOrder.nativeOrder());
	}

	/**
	 * Override the operating system name.
	 * This should only be used in test cases.
	 *
	 * @param aOs an OS name as could be found in the os.name system
	 * 		  property.
	 */
	public
	void setOs(
			final String aOs)
	{
		updatePlatform(aOs, _arch, _byteOrder);
	}

	/**
	 * Get the operating system.
	 *
	 * @return {@literal "windows"}, {@literal "osx"}, {@literal "linux"} or
	 *    {@literal "solaris"}.
	 */
	public
	String getOs()
	{
		return _os;
	}

	/**
	 * Override the architecture.
	 * This should only be used in test cases.
	 *
	 * @param aArch {@literal "big-endian"} for PowerPC or Sparc systems or
	 *      {@literal "little-endian"} for x86 systems.
	 */
	public
	void setArch(
			final String aArch)
	{
		updatePlatform(_os, aArch, _byteOrder);
	}

	/**
	 * Get the platform architecture.
	 *
	 * @return {@literal "ppc"}, {@literal "x86_32"}, {@literal "x86_64"} or
	 *    {"amd64"}
	 */
	public
	String getArch()
	{
		return _arch;
	}

	/**
	 * Set the byte order. TreeTagger models are sensitive to the byte order.
	 * This should only be used in test cases.
	 *
	 * @param aByteOrder the byte order.
	 */
	public
	void setByteOrder(
			final ByteOrder aByteOrder)
	{
		updatePlatform(_os, _arch, aByteOrder);
	}

	/**
	 * Get the file suffix used for executable files on the currently configured
	 * platform.
	 *
	 * @return the file suffix used for executable files.
	 */
	public
	String getExecutableSuffix()
	{
		return _executableSuffix;
	}

	/**
	 * Get the byte order.
	 *
	 * @return the byte order.
	 */
	public
	String getByteOrder()
	{
		return _byteOrder.toString().replace("_", "-").toLowerCase();
	}

	/**
	 * Get the platform ID which is {@link #getOs()} and {@link #getArch()}
	 * separated by a {@literal "-"} (dash).
	 *
	 * @return the platform ID.
	 */
	public
	String getPlatformId()
	{
		return _os+"-"+_arch;
	}

    /**
     * Updates the platform-specific settings and normalizes them.
     *
     * @param aOs the operating system string.
     * @param aArch the architecture string.
     * @param aByteOrder the byte-order string.
     */
    public
    void updatePlatform(
    		final String aOs,
    		final String aArch,
    		final ByteOrder aByteOrder)
    {
    	_os = aOs.toLowerCase();
    	_arch = aArch.toLowerCase();
    	String[] chmod = { "chmod", "755", null };

    	// Resolve arch "synonyms"
    	if (
    			_arch.equals("x86") ||
    			_arch.equals("i386") ||
    			_arch.equals("i486") ||
    			_arch.equals("i586") ||
    			_arch.equals("i686")
    	) {
    		_arch = ARCH_X86_32;
    	}
    	if (
    			_arch.equals("amd64")
    	) {
    		_arch = ARCH_X86_64;
    	}
    	if (_arch.equals("powerpc")) {
    		_arch = ARCH_PPC;
    	}

    	// Resolve name "synonyms"
    	if (_os.startsWith("windows")) {
    		_os = OS_WINDOWS;
    		_executableSuffix = ".exe";
    		chmod  = null;
    	}
    	if (_os.startsWith("mac")) {
    		_os = OS_OSX;
    	}
    	if (_os.startsWith("linux")) {
    		_os = OS_LINUX;
    	}
    	if (_os.startsWith("sun")) {
    		_os = OS_SOLARIS;
    	}

    	_chmodCmd = chmod;

    	_byteOrder = aByteOrder;
    }

    /**
     * Get the {@literal chmod} (change permissions) command for the current
     * platform (if one is necessary).
     *
     * @return the name of the {@literal chmod} command.
     */
    public
    String[] getChmodCmd()
    {
		return _chmodCmd;
	}
}
