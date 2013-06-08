/*******************************************************************************
 * University of Illinois/NCSA
 * Open Source License according to
 * http://www.otm.uiuc.edu/faculty/forms/opensource.asp
 * 
 * Copyright (c) 2006,    NCSA/UIUC.  All rights reserved.
 * 
 * Developed by:
 * 
 * Name of Development Groups:
 * Image Spatial Data Analysis Group (ISDA Group)
 * http://isda.ncsa.uiuc.edu/
 * 
 * Name of Institutions:
 * National Center for Supercomputing Applications (NCSA)
 * http://www.ncsa.uiuc.edu/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the 
 * "Software"), to deal with the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimers.
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimers in the
 *   documentation and/or other materials provided with the distribution.
 *   Neither the names of University of Illinois/NCSA, nor the names
 *   of its contributors may be used to endorse or promote products
 *   derived from this Software without specific prior written permission.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
 *******************************************************************************/
package edu.illinois.ncsa.isda.im2learn.core.io.object;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageException;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.im2learn.core.datatype.SubArea;
import edu.illinois.ncsa.isda.im2learn.core.io.ImageLoader;
import edu.illinois.ncsa.isda.im2learn.core.io.ImageReader;
import edu.illinois.ncsa.isda.im2learn.core.io.ImageWriter;
import edu.illinois.ncsa.isda.im2learn.core.io.ProgressInputStream;

/**
 * This class encapsulates the imageio loader from SUN.
 */
public class ObjectLoader implements ImageReader, ImageWriter {
	private static Log	logger	= LogFactory.getLog(ObjectLoader.class);

	/**
	 * Try and find a reader, if one found we can actually read the file.
	 * 
	 * @param filename
	 *            of file to be read.
	 * @param hdr
	 *            used for magic number
	 * @return true if image can be read.
	 */
	public boolean canRead(String filename, byte[] hdr) {
		return (filename.endsWith(".ser") || filename.endsWith(".ser.gz"));
	}

	/**
	 * Loads an image and returns the imageobject containing the image.
	 * 
	 * @param filename
	 *            of the file to load.
	 * @param subarea
	 *            of the file to load, or null to load full image.
	 * @param sampling
	 *            is the sampling that needs to be done.
	 * @return the imageobject containing the loaded image
	 * @throws java.io.IOException
	 *             if an error occurrs reading the file.
	 */
	public ImageObject readImage(String filename, SubArea subarea, int sampling) throws IOException {
		return readImage(filename, subarea, sampling, false);
	}

	/**
	 * This function will read the file and return an imageobject that contains
	 * the information of the image but not the imagedata itself.
	 * 
	 * @param filename
	 *            of the file to be read
	 * @return the file as an imageobject except of the imagedata.
	 * @throws java.io.IOException
	 *             if the file could not be read.
	 */
	public ImageObject readImageHeader(String filename) throws IOException {
		return readImage(filename, null, 1, true);
	}

	/**
	 * Return a list of extentions this class can read.
	 * 
	 * @return a list of extentions that are understood by this class.
	 */
	public String[] readExt() {
		return new String[] { "ser", "ser.gz" };
	}

	/**
	 * Loads an image and returns the imageobject containing the image.
	 * 
	 * @param filename
	 *            of the file to load.
	 * @param subarea
	 *            of the file to load, or null to load full image.
	 * @param sampling
	 *            is the sampling that needs to be done.
	 * @param header
	 *            true if only header needs to be read.
	 * @return the imageobject containing the loaded image
	 * @throws java.io.IOException
	 *             if an error occurrs reading the file.
	 */
	public ImageObject readImage(String filename, SubArea subarea, int sampling, boolean header) throws IOException {
		ProgressInputStream fis = new ProgressInputStream(new FileInputStream(filename));
		ObjectInputStream inp;
		if (filename.endsWith("gz")) {
			inp = new ObjectInputStream(new GZIPInputStream(fis));
		} else {
			inp = new ObjectInputStream(fis);
		}
		ImageObject result;
		try {
			result = (ImageObject) inp.readObject();
		} catch (ClassNotFoundException exc) {
			logger.debug("Could not load file.", exc);
			throw (new IOException(exc.toString()));
		} catch (ClassCastException exc) {
			logger.debug("Could not load file.", exc);
			throw (new IOException(exc.toString()));
		}
		inp.close();
		fis.close();

		// subsample and subarea
		if (subarea != null) {
			try {
				result = result.crop(subarea);
			} catch (ImageException exc) {
				logger.debug("Could not crop file.", exc);
				throw (new IOException(exc.toString()));
			}
		}
		if (sampling != 1) {
			try {
				result = result.scale(sampling);
			} catch (ImageException exc) {
				logger.debug("Could not sample file.", exc);
				throw (new IOException(exc.toString()));
			}
		}

		return result;
	}

	/**
	 * Return the description of the reader (or writer).
	 * 
	 * @return decription of the reader (or writer)
	 */
	public String getDescription() {
		return "Java Serialized Loader";
	}

	/**
	 * Returns true if the class can write the file.
	 * 
	 * @param filename
	 *            of the file to be written.
	 * @return true if the file can be written by this class.
	 */
	public boolean canWrite(String filename) {
		String ext = ImageLoader.getExtention(filename);
		return "ser".equalsIgnoreCase(ext) || "ser.gz".equalsIgnoreCase(ext);
	}

	/**
	 * This function will write the imageobject to a file.
	 * 
	 * @param filename
	 *            of the file to be written.
	 * @param imageobject
	 *            the image image to be written.
	 * @throws java.io.IOException
	 *             if the file could not be written.
	 */
	public void writeImage(String filename, ImageObject imageobject) throws IOException {
		OutputStream os;
		// if it ends with gz, set up a GZIPOutputStream
		if (filename.toLowerCase().endsWith("gz")) {
			FileOutputStream fos = new FileOutputStream(filename);
			os = new GZIPOutputStream(fos);
		}
		// set up a standard file output stream
		else {
			os = new FileOutputStream(filename);
		}
		// FileOutputStream fos = new FileOutputStream(filename);
		ObjectOutput out = new ObjectOutputStream(os);
		out.writeObject(imageobject);
		out.close();
		os.close();
	}

	/**
	 * Return a list of extentions this class can write.
	 * 
	 * @return a list of extentions that are understood by this class.
	 */
	public String[] writeExt() {
		return new String[] { "ser", "ser.gz" };
	}

	public int getImageCount(String filename) {
		return 1;
	}

	public ImageObject readImage(String filename, int index, SubArea subarea, int sampling) throws IOException,
			ImageException {
		return readImage(filename, subarea, sampling);
	}

	public ImageObject readImageHeader(String filename, int index) throws IOException, ImageException {
		return readImageHeader(filename);
	}
}
