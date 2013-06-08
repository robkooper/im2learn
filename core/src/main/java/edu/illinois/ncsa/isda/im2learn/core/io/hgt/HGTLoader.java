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
package edu.illinois.ncsa.isda.im2learn.core.io.hgt;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageException;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObjectUShort;
import edu.illinois.ncsa.isda.im2learn.core.datatype.SubArea;
import edu.illinois.ncsa.isda.im2learn.core.geo.projection.Projection;
import edu.illinois.ncsa.isda.im2learn.core.io.ImageLoader;
import edu.illinois.ncsa.isda.im2learn.core.io.ImageReader;

/**
 * This class will read HGT images. Based on the filename it will determine
 * where the height values are from.
 * 
 * @author Rob Kooper
 */
public class HGTLoader implements ImageReader {
	private static final int	sampleread	= 10000;
	private static Log			logger		= LogFactory.getLog(HGTLoader.class);

	/**
	 * Returns true if the file starts with "ENVI" as the first four characters.
	 * 
	 * @param filename
	 *            ignored.
	 * @param hdr
	 *            the first 100 bytes of the file.
	 * @return true if the file can be read by this class.
	 */
	public boolean canRead(String filename, byte[] hdr) {
		return filename.endsWith(".hgt");
	}

	/**
	 * This function will read the file and return an imageobject that contains
	 * the file.
	 * 
	 * @param filename
	 *            of the file to be read
	 * @param subarea
	 *            of the file to be read, null if full image is to be read.
	 * @param sampling
	 *            is the subsampling that needs to be done on the image as it is
	 *            loaded.
	 * @return the file as an imageobject.
	 * @throws IOException
	 *             if the file could not be read.
	 */
	public ImageObject readImage(String filename, SubArea subarea, int sampling) throws IOException {
		return loadImage(filename, subarea, sampling, false);
	}

	/**
	 * This function will read the file and return an imageobject that contains
	 * the information of the image but not the imagedata itself.
	 * 
	 * @param filename
	 *            of the file to be read
	 * @return the file as an imageobject except of the imagedata.
	 * @throws IOException
	 *             if the file could not be read.
	 */
	public ImageObject readImageHeader(String filename) throws IOException {
		return loadImage(filename, null, 1, true);
	}

	/**
	 * Return a list of extensions this class can read.
	 * 
	 * @return a list of extensions that are understood by this class.
	 */
	public String[] readExt() {
		return new String[] { "hgt" };
	}

	/**
	 * Return the description of the reader (or writer).
	 * 
	 * @return decription of the reader (or writer)
	 */
	public String getDescription() {
		return "HGT files";
	}

	/**
	 * This function will read the image and return the imageobject.
	 * 
	 * @param filename
	 *            of the file to read.
	 * @param subarea
	 *            of the file to read.
	 * @param sampling
	 *            the subsampling to use.
	 * @param header
	 *            true if only the header should be read.
	 * @return an imageobject containting the file.
	 * @throws IOException
	 *             if the file could not be read correctly.
	 */
	private ImageObject loadImage(String filename, SubArea subarea, int sampling, boolean header) throws IOException {
		// try to figure out what the header and datafile is.
		EnviHeader enviheader = new EnviHeader();
		enviheader.sampling = sampling;
		enviheader.subarea = subarea;
		enviheader.filebands = 1;
		enviheader.typesize = 2;
		File datafile = new File(filename);

		// retrieve the size of the data
		int size = (int) Math.sqrt(datafile.length() / 2);
		enviheader.filelines = size;
		enviheader.filesamples = size;

		// store some info
		enviheader.sampling = sampling;
		enviheader.subarea = subarea;

		// fix for subarea and subsample
		if (enviheader.subarea != null) {
			enviheader.samples = enviheader.subarea.width;
			enviheader.lines = enviheader.subarea.height;
			if (enviheader.subarea.getNumBands() == 0) {
				enviheader.bands = enviheader.filebands;
			} else {
				enviheader.bands = enviheader.subarea.getNumBands();
			}
		} else {
			enviheader.samples = enviheader.filesamples;
			enviheader.lines = enviheader.filelines;
			enviheader.bands = enviheader.filebands;
		}
		if (enviheader.sampling != 1) {
			enviheader.samples = (int) Math.ceil(enviheader.samples / (double) enviheader.sampling);
			enviheader.lines = (int) Math.ceil(enviheader.lines / (double) enviheader.sampling);
		}

		enviheader.maxdata = enviheader.samples * enviheader.lines * enviheader.bands;
		enviheader.maxfile = enviheader.filesamples * enviheader.filelines * enviheader.filebands;

		double degrees = 0.00083333 / (enviheader.filesamples / 1200);
		System.out.println(degrees);

		ImageObject imageobject = null;
		imageobject = new ImageObjectUShort(enviheader.lines, enviheader.samples, enviheader.bands, true);

		String latlon = datafile.getName();
		double lat = Double.valueOf(latlon.substring(1, 3)) - (0.5 * degrees);
		if (datafile.getName().charAt(0) == 'S') {
			lat = -lat;
		}
		double lon = Double.valueOf(latlon.substring(4, 7)) - (0.5 * degrees);
		if (latlon.charAt(3) == 'W') {
			lon = -lon;
		}

		Projection geo = new Projection(0, enviheader.filelines, lon, lat, degrees, degrees, enviheader.filesamples,
				enviheader.filelines);

		imageobject.setProperty(ImageObject.GEOINFO, geo);
		imageobject.setInvalidData(32768);

		logger.debug("samples  = " + enviheader.samples + " (of " + enviheader.filesamples + ")");
		logger.debug("lines    = " + enviheader.lines + " (of " + enviheader.filelines + ")");
		logger.debug("bands    = " + enviheader.bands + " (of " + enviheader.filebands + ")");
		logger.debug("typesize = " + enviheader.typesize);
		logger.debug("size     = " + enviheader.maxdata * enviheader.typesize + " (of " + enviheader.maxfile
				* enviheader.typesize + ")");

		if (imageobject.getProperty(ImageObject.GEOINFO) != null) {
			Projection proj = (Projection) imageobject.getProperty(ImageObject.GEOINFO);
			logger.debug("proj     = " + proj.toString());
		}

		if (!header) {
			loadImageData(datafile, enviheader, imageobject);
		}
		return imageobject;
	}

	/**
	 * Load the image data file.
	 * 
	 * @param datafile
	 *            containing the image data.
	 * @param enviheader
	 *            the header information.
	 * @param imageobject
	 *            that will contain the image data.
	 * @throws IOException
	 *             if an error occurred reading the data.
	 */
	private void loadImageData(File datafile, EnviHeader enviheader, ImageObject imageobject) throws IOException {
		FileInputStream fi = new FileInputStream(datafile);
		DataInputStream inp = new DataInputStream(fi);

		imageobject.createArray();
		int count = 0;
		byte[] filedata = new byte[sampleread * enviheader.typesize];
		while (count < enviheader.maxfile) {
			int bytesread = inp.read(filedata);
			if (bytesread < 0) {
				throw (new IOException("Error reading data from file."));
			}
			if ((bytesread < filedata.length) && (bytesread != (enviheader.maxfile - count) * enviheader.typesize)) {
				throw (new IOException("Error reading data from file."));
			}
			count = fixSample(imageobject, enviheader, count, filedata, bytesread);
			ImageLoader.fireProgress(count, enviheader.maxfile);
		}
		fi.close();

		// calculate min and max
		imageobject.computeMinMax();

		logger.debug("maxValue = " + imageobject.getMax());
		logger.debug("minValue = " + imageobject.getMin());
	}

	/**
	 * Put the sample in the right location in the imageobject.
	 * 
	 * @param imageobject
	 *            that will contain the image data.
	 * @param enviheader
	 *            the header information of the image.
	 * @param start
	 *            where in the file this data was found.
	 * @param filedata
	 *            the data that was read.
	 * @param length
	 *            the amount of data that was read.
	 * @return the new start in the filedata.
	 * @throws IOException
	 *             if an error occurred parsing the data.
	 */
	private int fixSample(ImageObject imageobject, EnviHeader enviheader, int start, byte[] filedata, int length)
			throws IOException {
		int x, y, b, m, t;

		for (int i = 0; i < length; i = i + enviheader.typesize, start++) {
			m = enviheader.filesamples * enviheader.filebands;
			t = start % m;
			y = (start - t) / m;
			x = t % enviheader.filesamples;
			b = (t - x) / enviheader.filesamples;

			// make sure data is inside the subarea
			boolean oktogo = true;
			if (oktogo && (enviheader.subarea != null)) {
				x = x - enviheader.subarea.x;
				y = y - enviheader.subarea.y;
				if ((x < 0) || (x >= enviheader.subarea.width) || (y < 0) || (y >= enviheader.subarea.height)) {
					oktogo = false;
				}
				if (enviheader.subarea.getNumBands() != 0) {
					b = b - enviheader.subarea.getFirstBand();
					if ((b < 0) || (b >= enviheader.subarea.getNumBands())) {
						oktogo = false;
					}
				}
			}
			// are we on a subsampel pixel
			if (enviheader.sampling != 1) {
				if (oktogo && (x % enviheader.sampling == 0) && (y % enviheader.sampling == 0)) {
					x = x / enviheader.sampling;
					y = y / enviheader.sampling;
				} else {
					oktogo = false;
				}
			}

			if (oktogo) {
				// imageobject.set(y, x, b, (filedata[i + 1] << 8) +
				// (filedata[i] & 0xff));
				imageobject.set(y, x, b, (filedata[i] << 8) + (filedata[i + 1] & 0xff));
			}
		}

		return start;
	}

	/**
	 * Class to hold all image specific variables, this way the loader itself is
	 * reentrant.
	 */
	class EnviHeader {
		public int		samples		= 0;
		public int		lines		= 0;
		public int		bands		= 0;
		public int		filesamples	= 0;
		public int		filelines	= 0;
		public int		filebands	= 0;
		public int		typesize	= 0;
		public int		xstart		= 0;
		public int		ystart		= 0;
		public String[]	mapinfo		= null;
		public SubArea	subarea		= null;
		public int		sampling	= 1;

		public int		maxdata		= 0;
		public int		maxfile		= 0;
	}

	public int getImageCount(String filename) throws IOException, ImageException {
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
