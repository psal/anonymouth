package edu.drexel.psal.anonymouth.helpers;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import edu.drexel.psal.ANONConstants;
import edu.drexel.psal.jstylo.generics.Logger;
import edu.drexel.psal.jstylo.generics.Logger.LogOut;

/**
 * Provides a convenient interface to load many different kinds of Images that may be needed by Anonymouth 
 * @author Marc Barrowclift
 *
 */

public class ImageLoader {

	private static final String NAME = "( ImageHandler ) - ";

	/**
	 * Loads and returns an Image Icon with the specified name
	 * 
	 * @param name
	 * 		The name of the Image Icon to load
	 * @return
	 */
	public static ImageIcon getImageIcon(String name) {
		ImageIcon icon = null;

		try {
			icon = new ImageIcon(ImageLoader.class.getClass().getResource(ANONConstants.GRAPHICS+name));
		} catch (Exception e) {
			Logger.logln(NAME+"Error loading Image Icon " + name, LogOut.STDERR);
		}

		return icon;
	}

	/**
	 * Loads and returns an Image with the specified name
	 * 
	 * @param name
	 * 		The name of the Image to load
	 * @return
	 */
	public static Image getImage(String name) {
		Image image = null;

		try {
			image = ImageIO.read(ImageLoader.class.getClass().getResource(ANONConstants.GRAPHICS+name));
		} catch (IOException e) {
			e.printStackTrace();
			Logger.logln(NAME+"Error loading Image " + name, LogOut.STDERR);
		}

		return image;
	}

	/**
	 * Loads and returns a Buffered Image with the specified name
	 * 
	 * @param name
	 * 		The name of the Buffered Image to load
	 * @return
	 */
	public static BufferedImage getBufferedImage(String name) {
		BufferedImage bufImage = null;

		try {
			bufImage = ImageIO.read(ImageLoader.class.getClass().getResource(ANONConstants.GRAPHICS+name));
		} catch (IOException e) {
			Logger.logln(NAME+"Error loading the Buffered Image " + name, LogOut.STDERR);
		}

		return bufImage;
	}

	/**
	 * Loads and returns an Icon with the specified name
	 * 
	 * @param name
	 * 		The name of the Icon you want to load (Must be in resources/graphics)
	 * @return
	 * 		The javax.swing.Icon instance of the file you wanted to load
	 */
	public static Icon getIcon(String name) {
		Icon icon = null;

		try {
			icon = new ImageIcon(ImageLoader.class.getClass().getResource(ANONConstants.GRAPHICS+name));
		} catch (Exception e) {
			Logger.logln(NAME+"Error loading Image Icon " + name, LogOut.STDERR);
		}

		return icon;
	}
}
