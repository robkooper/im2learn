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
package edu.illinois.ncsa.isda.im2learn.core.io.envi;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageException;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObjectByte;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObjectDouble;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObjectFloat;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObjectInt;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObjectLong;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObjectShort;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObjectUShort;
import edu.illinois.ncsa.isda.im2learn.core.datatype.SubArea;
import edu.illinois.ncsa.isda.im2learn.core.geo.projection.AlbersEqualAreaConic;
import edu.illinois.ncsa.isda.im2learn.core.geo.projection.Projection;
import edu.illinois.ncsa.isda.im2learn.core.geo.projection.UTMNorth;
import edu.illinois.ncsa.isda.im2learn.core.io.ImageLoader;
import edu.illinois.ncsa.isda.im2learn.core.io.ImageReader;
import edu.illinois.ncsa.isda.im2learn.core.io.ImageWriter;

/**
 * This class will read and write ENVI images. It will can read most keywords in the image header, but only write the
 * bare minimum.
 * 
 * @author Rob Kooper
 */
public class ENVILoader implements ImageReader, ImageWriter {
	private static final int	sampleread				= 10000;
	private static Log			logger					= LogFactory.getLog(ENVILoader.class);

	private static final int	BAND_SEQUENTIAL			= 0;
	private static final int	BAND_INTERLEAVE_PIXEL	= 1;
	private static final int	BAND_INTERLEAVE_LINE	= 2;

	/**
	 * 8 bit byte
	 */
	private static final int	BYTE					= 1;
	/**
	 * 16 bit signed integer
	 */
	private static final int	SIGNED_INTEGER			= 2;
	/**
	 * 32 bit signed long intgeger
	 */
	private static final int	SIGNED_LONG				= 3;
	/**
	 * 32 bit floating point
	 */
	private static final int	FLOAT					= 4;
	/**
	 * 64 bit double precision floating point
	 */
	private static final int	DOUBLE					= 5;
	/**
	 * 2x32 bit comple, real-imaginary pair of double precision
	 */
	private static final int	COMPLEX_FLOAT			= 6;
	/**
	 * 2x64 bit double precision complex, real-imaginary pair of double precision
	 */
	private static final int	COMPLEX_DOUBLE			= 9;
	/**
	 * 16 bit unsigned integer
	 */
	private static final int	UNSIGNED_INTEGER		= 12;
	/**
	 * 32 bit unsigned long integer
	 */
	private static final int	UNSIGNED_LONG			= 13;
	/**
	 * 64 bit unsigned integer
	 */
	private static final int	UNSIGNED_64INTEGER		= 14;
	/**
	 * 64 bit unsigned long integer
	 */
	private static final int	UNSIGNED_64LONG			= 15;

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
		// file should always start with ENVI
		if (hdr[0] == 'E' && hdr[1] == 'N' && hdr[2] == 'V' && hdr[3] == 'I') {
			return true;
		}

		// no matching on ext, if it does not start with IIP we can't read it.
		return false;
	}

	/**
	 * This function will read the file and return an imageobject that contains the file.
	 * 
	 * @param filename
	 *            of the file to be read
	 * @param subarea
	 *            of the file to be read, null if full image is to be read.
	 * @param sampling
	 *            is the subsampling that needs to be done on the image as it is loaded.
	 * @return the file as an imageobject.
	 * @throws IOException
	 *             if the file could not be read.
	 */
	public ImageObject readImage(String filename, SubArea subarea, int sampling) throws IOException {
		return loadImage(filename, subarea, sampling, false);
	}

	/**
	 * This function will read the file and return an imageobject that contains the information of the image but not the
	 * imagedata itself.
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
	 * Return a list of extentions this class can read.
	 * 
	 * @return a list of extentions that are understood by this class.
	 */
	public String[] readExt() {
		return new String[] { "hdr" };
	}

	/**
	 * Return the description of the reader (or writer).
	 * 
	 * @return decription of the reader (or writer)
	 */
	public String getDescription() {
		return "ENVI files";
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
		File headerfile = null;
		File datafile = null;

		if (filename.toLowerCase().endsWith(".hdr")) {
			headerfile = new File(filename);
			if (!headerfile.exists()) {
				headerfile = null;
			}
			String name = filename.substring(0, filename.length() - 4);
			datafile = new File(name + ".fla");
			if (!datafile.exists()) {
				File[] files = datafile.getParentFile().listFiles();
				datafile = null;
				for (int i = 0; i < files.length; i++) {
					if (files[i].getAbsolutePath().startsWith(name) && !files[i].getAbsolutePath().toLowerCase().endsWith(".hdr")) {
						datafile = files[i];
						break;
					}
				}
			}

		} else {
			String tmpname = filename;
			if (filename.lastIndexOf('.') > 0) {
				tmpname = filename.substring(0, filename.length() - filename.lastIndexOf('.') - 1);
			}
			headerfile = new File(tmpname + ".hdr");
			if (!headerfile.exists()) {
				headerfile = new File(filename + ".hdr");
				if (!headerfile.exists()) {
					headerfile = null;
				}
			}
			datafile = new File(filename);
			if (!datafile.exists()) {
				datafile = null;
			}
		}
		if (headerfile == null) {
			throw (new IOException("Could not find header file."));
		}
		if (datafile == null) {
			throw (new IOException("Could not find data file."));
		}

		// store some info
		enviheader.sampling = sampling;
		enviheader.subarea = subarea;
		ImageObject imgobj = loadImageHeader(headerfile, enviheader);

		if (!header) {
			loadImageData(datafile, enviheader, imgobj);
		}
		return imgobj;
	}

	/**
	 * Read the header of the image. The header has key = value pairs. The value can be stored between braces. If the
	 * value is an array, the array parts are seperated by a comma.
	 * 
	 * @param headerfile
	 *            the headerfile
	 * @param enviheader
	 *            the structure containing the header informatioon
	 * @return an imageobject based on the header information
	 * @throws IOException
	 *             if the file could not be read correctly.
	 */
	private ImageObject loadImageHeader(File headerfile, EnviHeader enviheader) throws IOException {

		// load the file
		BufferedReader r = new BufferedReader(new FileReader(headerfile));
		String line = r.readLine();
		int lineno = 1;
		while (line != null) {
			line.trim();
			String[] split = line.split("[ ]*=[ ]*", 2);

			// First line should always be ENVI
			if (lineno == 1) {
				if (split[0].trim().compareToIgnoreCase("ENVI") != 0) {
					System.err.println("First line in headerfile should be ENVI.");
				}
			}

			if (split[0].trim().compareToIgnoreCase("ENVI") == 0) {
				if (lineno != 1) {
					System.err.println("[" + lineno + "] First line in headerfile should be ENVI.");
				}

			} else if (split[0].compareToIgnoreCase("description") == 0) {
				if (split[1] != "{") {
					enviheader.description = split[1].substring(1).trim();
				}
				while (!enviheader.description.endsWith("}")) {
					lineno++;
					enviheader.description += " " + r.readLine().trim();
				}
				enviheader.description = enviheader.description.substring(0, enviheader.description.length() - 1).trim();

			} else if (split[0].compareToIgnoreCase("samples") == 0) {
				enviheader.filesamples = Integer.parseInt(split[1]);
				if ((enviheader.subarea != null) && (enviheader.subarea.width > (enviheader.filesamples - enviheader.subarea.x))) {
					enviheader.subarea.width = enviheader.filesamples - enviheader.subarea.x;
				}

			} else if (split[0].compareToIgnoreCase("lines") == 0) {
				enviheader.filelines = Integer.parseInt(split[1]);
				if ((enviheader.subarea != null) && (enviheader.subarea.height > (enviheader.filelines - enviheader.subarea.y))) {
					enviheader.subarea.height = enviheader.filelines - enviheader.subarea.y;
				}

			} else if (split[0].compareToIgnoreCase("bands") == 0) {
				enviheader.filebands = Integer.parseInt(split[1]);
				if ((enviheader.subarea != null) && (enviheader.subarea.getNumBands() > 0) && (enviheader.subarea.getNumBands() > (enviheader.filebands - enviheader.subarea.getFirstBand()))) {
					enviheader.subarea.width = enviheader.filesamples - enviheader.subarea.x;
				}

			} else if (split[0].compareToIgnoreCase("data type") == 0) {
				enviheader.type = Integer.parseInt(split[1]);
				switch (enviheader.type) {
				case BYTE:
					enviheader.typesize = 1;
					break;
				case SIGNED_INTEGER:
					enviheader.typesize = 2;
					break;
				case SIGNED_LONG:
					enviheader.typesize = 4;
					break;
				case FLOAT:
					enviheader.typesize = 4;
					break;
				case DOUBLE:
					enviheader.typesize = 8;
					break;
				// case COMPLEX_FLOAT:
				// enviheader.typesize = 8;
				// break;
				// case COMPLEX_DOUBLE:
				// enviheader.typesize = 8;
				// break;
				case UNSIGNED_INTEGER:
					enviheader.typesize = 2;
					break;
				case UNSIGNED_LONG:
					enviheader.typesize = 4;
					break;
				case UNSIGNED_64INTEGER:
					enviheader.typesize = 8;
					break;
				case UNSIGNED_64LONG:
					enviheader.typesize = 8;
					break;
				case 8:
					logger.debug("old ENVI saved float of type 8, assuming float.");
					enviheader.type = FLOAT;
					enviheader.typesize = 4;
					break;
				default:
					throw (new IOException("[" + lineno + "] not yet implemented [type=" + enviheader.type + "]"));
				}

			} else if (split[0].compareToIgnoreCase("byte order") == 0) {
				enviheader.endian = Integer.parseInt(split[1]);

			} else if (split[0].compareToIgnoreCase("header offset") == 0) {
				enviheader.headerOffset = Integer.parseInt(split[1]);

			} else if (split[0].compareToIgnoreCase("x start") == 0) {
				enviheader.xstart = Integer.parseInt(split[1]);

			} else if (split[0].compareToIgnoreCase("y start") == 0) {
				enviheader.ystart = Integer.parseInt(split[1]);

			} else if (split[0].compareToIgnoreCase("sensor type") == 0) {
				enviheader.sensorType = split[1];

			} else if (split[0].compareToIgnoreCase("file type") == 0) {
				if (!split[1].equals("ENVI Standard") && !split[1].equals("ENVI Classification")) {
					throw (new IOException("[" + lineno + "] Don't understand filetype [filetype=" + split[1] + "]"));
				}

			} else if (split[0].compareToIgnoreCase("filetype") == 0) {
				if (!split[1].equals("ENVI Standard")) {
					throw (new IOException("[" + lineno + "] Don't understand filetype [filetype=" + split[1] + "]"));
				}

			} else if (split[0].compareToIgnoreCase("wavelength") == 0) {
				String wavelengths = "";
				if (split[1] != "{") {
					wavelengths = split[1].substring(1).trim();
				}
				while (!wavelengths.endsWith("}")) {
					lineno++;
					wavelengths += " " + r.readLine().trim();
				}
				wavelengths = wavelengths.substring(0, wavelengths.length() - 1).trim();
				enviheader.wavelength = wavelengths.split(",[ ]*");
				if (enviheader.wavelength.length != enviheader.filebands) {
					System.err.println("[" + lineno + "] Not enough bands in wavelength [" + enviheader.wavelength.length + " != " + enviheader.filebands + "].");
				}

			} else if (split[0].compareToIgnoreCase("fwhm") == 0) {
				String fwhms = "";
				if (split[1] != "{") {
					fwhms = split[1].substring(1).trim();
				}
				while (!fwhms.endsWith("}")) {
					lineno++;
					fwhms += " " + r.readLine().trim();
				}
				fwhms = fwhms.substring(0, fwhms.length() - 1).trim();
				enviheader.fwhm = fwhms.split(",[ ]*");
				if (enviheader.fwhm.length != enviheader.filebands) {
					System.err.println("[" + lineno + "] Not enough bands in fwhm [" + enviheader.fwhm.length + " != " + enviheader.filebands + "].");
				}

			} else if (split[0].compareToIgnoreCase("band names") == 0) {
				String bandnames = "";
				if (split[1] != "{") {
					bandnames = split[1].substring(1).trim();
				}
				while (!bandnames.endsWith("}")) {
					lineno++;
					bandnames += " " + r.readLine().trim();
				}
				bandnames = bandnames.substring(0, bandnames.length() - 1).trim();
				enviheader.bandname = bandnames.split(",[ ]*");
				if (enviheader.bandname.length != enviheader.filebands) {
					System.err.println("[" + lineno + "] Not enough bands in band names [" + enviheader.bandname.length + " != " + enviheader.filebands + "].");
				}

			} else if (split[0].compareToIgnoreCase("map info") == 0) {
				String mapinfos = "";
				if (split[1] != "{") {
					mapinfos = split[1].substring(1).trim();
				}
				while (!mapinfos.endsWith("}")) {
					lineno++;
					mapinfos += " " + r.readLine().trim();
				}
				mapinfos = mapinfos.substring(0, mapinfos.length() - 1).trim();
				enviheader.mapinfo = mapinfos.split(",[ ]*");

			} else if (split[0].compareToIgnoreCase("default bands") == 0) {
				String defaultbands = "";
				if (split[1] != "{") {
					defaultbands = split[1].substring(1).trim();
				}
				while (!defaultbands.endsWith("}")) {
					lineno++;
					defaultbands += " " + r.readLine().trim();
				}
				defaultbands = defaultbands.substring(0, defaultbands.length() - 1).trim();
				String[] bands = defaultbands.split(",[ ]*");
				if (bands.length == 1) {
					enviheader.defaultGray = Integer.parseInt(bands[0]) - 1;
				} else if (bands.length == 3) {
					enviheader.defaultRed = Integer.parseInt(bands[0]) - 1;
					enviheader.defaultGreen = Integer.parseInt(bands[1]) - 1;
					enviheader.defaultBlue = Integer.parseInt(bands[2]) - 1;
				} else {
					throw (new IOException("[" + lineno + "] Don't know how to handle " + bands.length + " default bands."));
				}

			} else if (split[0].compareToIgnoreCase("interleave") == 0) {
				if (split[1].equalsIgnoreCase("bsq")) {
					enviheader.interleave = BAND_SEQUENTIAL;
				} else if (split[1].equalsIgnoreCase("bip")) {
					enviheader.interleave = BAND_INTERLEAVE_PIXEL;
				} else if (split[1].equalsIgnoreCase("bil")) {
					enviheader.interleave = BAND_INTERLEAVE_LINE;
				} else {
					throw (new IOException("[" + lineno + "] Don't understand interleave [interleave = " + split[1] + "]."));
				}

			} else {
				System.out.print("[" + lineno + "] Unimplemented token : [-");
				for (String element : split) {
					System.out.print(element + "-");
				}
				System.out.println("]");
			}

			lineno++;
			line = r.readLine();
		}
		r.close();

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

		ImageObject imageobject = null;
		switch (enviheader.type) {
		case BYTE:
			imageobject = new ImageObjectByte(enviheader.lines, enviheader.samples, enviheader.bands, true);
			break;

		case SIGNED_INTEGER:
			imageobject = new ImageObjectShort(enviheader.lines, enviheader.samples, enviheader.bands, true);
			break;

		case UNSIGNED_INTEGER:
			imageobject = new ImageObjectUShort(enviheader.lines, enviheader.samples, enviheader.bands, true);
			break;

		case SIGNED_LONG:
			imageobject = new ImageObjectInt(enviheader.lines, enviheader.samples, enviheader.bands, true);
			break;

		case UNSIGNED_LONG:
		case UNSIGNED_64INTEGER: // will be converted to signed, no type large enough to hold data
		case UNSIGNED_64LONG: // will be converted to signed, no type large enough to hold data
			imageobject = new ImageObjectLong(enviheader.lines, enviheader.samples, enviheader.bands, true);
			break;

		case FLOAT:
			imageobject = new ImageObjectFloat(enviheader.lines, enviheader.samples, enviheader.bands, true);
			break;

		case DOUBLE:
			imageobject = new ImageObjectDouble(enviheader.lines, enviheader.samples, enviheader.bands, true);
			break;

		default:
			throw (new IOException("Can't load this datatype."));
		}

		imageobject.setProperty(ImageObject.COMMENT, enviheader.description);
		if (enviheader.defaultRed != -1) {
			imageobject.setProperty(ImageObject.DEFAULT_RGB, new int[] { enviheader.defaultRed, enviheader.defaultGreen, enviheader.defaultBlue });
		}
		if (enviheader.defaultGray != -1) {
			imageobject.setProperty(ImageObject.DEFAULT_GRAY, new int[] { enviheader.defaultGray });
		}
		imageobject.setProperty(ImageObject.WAVELENGTH, enviheader.wavelength);
		imageobject.setProperty("sensortype", enviheader.sensorType);
		imageobject.setProperty(ImageObject.BAND_LABELS, enviheader.bandname);
		imageobject.setProperty("fwhm", enviheader.fwhm);

		if ((enviheader.mapinfo != null) && (enviheader.mapinfo.length != 0)) {
			Projection geo = null;
			;
			int len = enviheader.mapinfo.length;

			if (enviheader.mapinfo[0].equalsIgnoreCase("utm") && (len > 8)) {
				geo = new UTMNorth();
				geo.SetRasterSpaceI(Double.parseDouble(enviheader.mapinfo[1]));
				geo.SetRasterSpaceJ(Double.parseDouble(enviheader.mapinfo[2]));
				geo.SetRasterSpaceK(0.0);

				geo.setInsertionX(Double.parseDouble(enviheader.mapinfo[3]));
				geo.setInsertionY(Double.parseDouble(enviheader.mapinfo[4]));
				geo.setInsertionZ(0.0);

				geo.SetColumnResolution(Double.parseDouble(enviheader.mapinfo[5]));
				geo.SetRowResolution(Double.parseDouble(enviheader.mapinfo[6]));
				geo.setScaleZ(0.0);

				// geo.SetUTMZone(Integer.parseInt(enviheader.mapinfo[7]));

				if (enviheader.mapinfo[8].equalsIgnoreCase("north")) {
					// geo.SetModelType(Projection.UTM_NORTHERN_HEMISPHERE);
				} else if (enviheader.mapinfo[8].equalsIgnoreCase("south")) {
					// geo.SetModelType(2);
					// not supported - should be fixed
				} else {
					geo = null;
				}

			} else if (enviheader.mapinfo[0].equalsIgnoreCase("dem") && (len > 3)) {
				geo = new Projection();
				double cellsize = Double.parseDouble(enviheader.mapinfo[1]);
				double x11corner = Double.parseDouble(enviheader.mapinfo[2]);
				double y11corner = Double.parseDouble(enviheader.mapinfo[3]);
				// geo.SetModelType(Projection.GEOGRAPHIC);
				geo.SetRasterSpaceI(0);
				geo.SetRasterSpaceJ(0 + enviheader.lines);
				geo.SetColumnResolution(cellsize);
				geo.SetRowResolution(cellsize);
				geo.setScaleX(cellsize);
				geo.setScaleY(cellsize);
				// geo.SetMaxWestLng(x11corner);
				// geo.SetMaxSouthLat(y11corner);
				geo.SetEastingInsertionValue(x11corner);
				geo.SetNorthingInsertionValue(y11corner);

			} else if (enviheader.mapinfo[0].equalsIgnoreCase("ALBERS_EQUAL_AREA_CONIC") && (len > 14)) {

				geo = new AlbersEqualAreaConic();

				geo.SetRasterSpaceI(Double.parseDouble(enviheader.mapinfo[1]));
				geo.SetRasterSpaceJ(Double.parseDouble(enviheader.mapinfo[2]));
				geo.SetRasterSpaceK(0.0);

				geo.setInsertionX(Double.parseDouble(enviheader.mapinfo[3]));
				geo.setInsertionY(Double.parseDouble(enviheader.mapinfo[4]));
				geo.setInsertionZ(0.0);

				geo.setScaleX(Double.parseDouble(enviheader.mapinfo[5]));
				geo.setScaleY(Double.parseDouble(enviheader.mapinfo[6]));
				geo.setScaleZ(0.0);

				AlbersEqualAreaConic myProj = (AlbersEqualAreaConic) geo;

				myProj.setParallels(Double.parseDouble(enviheader.mapinfo[7]), Double.parseDouble(enviheader.mapinfo[8]));
				myProj.setOrigin(Double.parseDouble(enviheader.mapinfo[9]), Double.parseDouble(enviheader.mapinfo[10]));

				myProj.setRadius(Double.parseDouble(enviheader.mapinfo[11]));
				myProj.setEccentricSqure(Double.parseDouble(enviheader.mapinfo[12]));

				/*
				 * w.write(myProj.getFirstStandardParallel() + ", "); w.write(myProj.getSecondStandardParallel() + ",
				 * "); w.write(myProj.getLatOrigin() + ", "); w.write(myProj.getLngOrigin() + ", ");
				 * w.write(myProj.getRadius() + ", "); w.write(myProj.getEccentricitySquared() + ", ");
				 */

				// geo.SetUTMZone(Integer.parseInt(enviheader.mapinfo[7]));
			} else {
				geo = null;
			}

			if (geo != null) {
				// GeoConvert geoConv = new GeoConvert(geo);
				// geoConv.subSamplePrep(enviheader.sampling);

				// The current code does not support loading subarea and subsampling
				/*
				 * if (enviheader.subarea != null) { geoConv.subAreaPrep(enviheader.subarea, enviheader.filelines,
				 * enviheader.filesamples); }
				 */

				imageobject.setProperty(ImageObject.GEOINFO, geo);
			} else {
				String msg = "map info included, but how do I parse it? (";
				msg += enviheader.mapinfo[0];
				for (int i = 1; i < len; i++) {
					msg += " " + enviheader.mapinfo[i];
				}
				msg += ")";
				logger.warn(msg);
			}
		}

		logger.debug("samples  = " + enviheader.samples + " (of " + enviheader.filesamples + ")");
		logger.debug("lines    = " + enviheader.lines + " (of " + enviheader.filelines + ")");
		logger.debug("bands    = " + enviheader.bands + " (of " + enviheader.filebands + ")");
		logger.debug("type     = " + enviheader.type);
		logger.debug("typesize = " + enviheader.typesize);
		logger.debug("size     = " + enviheader.maxdata * enviheader.typesize + " (of " + enviheader.maxfile * enviheader.typesize + ")");

		if (imageobject.getProperty(ImageObject.GEOINFO) != null) {
			Projection proj = (Projection) imageobject.getProperty(ImageObject.GEOINFO);
			logger.debug("proj     = " + proj.toString());
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
		inp.skip(enviheader.headerOffset);

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
	private int fixSample(ImageObject imageobject, EnviHeader enviheader, int start, byte[] filedata, int length) throws IOException {
		int x, y, b, m, t;
		long l;

		for (int i = 0; i < length; i = i + enviheader.typesize, start++) {
			switch (enviheader.interleave) {
			case BAND_SEQUENTIAL:
				m = enviheader.filesamples * enviheader.filelines;
				t = start % m;
				b = (start - t) / m;
				x = t % enviheader.filesamples;
				y = (t - x) / enviheader.filesamples;
				break;

			case BAND_INTERLEAVE_PIXEL:
				m = enviheader.filebands * enviheader.filesamples;
				t = start % m;
				y = (start - t) / m;
				b = t % enviheader.filebands;
				x = (t - b) / enviheader.filebands;
				break;

			case BAND_INTERLEAVE_LINE:
				m = enviheader.filesamples * enviheader.filebands;
				t = start % m;
				y = (start - t) / m;
				x = t % enviheader.filesamples;
				b = (t - x) / enviheader.filesamples;
				break;

			default:
				throw (new IOException("Don't understand interleave method."));
			}

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
				switch (enviheader.type) {
				case BYTE:
					imageobject.set(y, x, b, filedata[i] & 0xff);
					break;

				case SIGNED_INTEGER:
					if (enviheader.endian == 0) {
						imageobject.set(y, x, b, (filedata[i + 1] << 8) + (filedata[i] & 0xff));
					} else {
						imageobject.set(y, x, b, (filedata[i] << 8) + (filedata[i + 1] & 0xff));
					}
					break;

				case SIGNED_LONG:
					if (enviheader.endian == 0) {
						imageobject.set(y, x, b, ((filedata[i + 3] & 0xff) << 24) | ((filedata[i + 2] & 0xff) << 16) | ((filedata[i + 1] & 0xff) << 8) | (filedata[i] & 0xff));
					} else {
						imageobject.set(y, x, b, ((filedata[i] & 0xff) << 24) | ((filedata[i + 1] & 0xff) << 16) | ((filedata[i + 2] & 0xff) << 8) | (filedata[i + 3] & 0xff));
					}
					break;

				case FLOAT:
					if (enviheader.endian == 0) {
						t = ((filedata[i + 3] & 0xff) << 24) | ((filedata[i + 2] & 0xff) << 16) | ((filedata[i + 1] & 0xff) << 8) | (filedata[i] & 0xff);
					} else {
						t = ((filedata[i] & 0xff) << 24) | ((filedata[i + 1] & 0xff) << 16) | ((filedata[i + 2] & 0xff) << 8) | (filedata[i + 3] & 0xff);
					}
					imageobject.set(y, x, b, Float.intBitsToFloat(t));
					break;

				case DOUBLE:
					if (enviheader.endian == 0) {
						l = ((long) (filedata[i + 7] & 0xff) << 56) | ((long) (filedata[i + 6] & 0xff) << 48) | ((long) (filedata[i + 5] & 0xff) << 40) | ((long) (filedata[i + 4] & 0xff) << 32)
								| ((long) (filedata[i + 3] & 0xff) << 24) | ((long) (filedata[i + 2] & 0xff) << 16) | ((long) (filedata[i + 1] & 0xff) << 8) | (long) (filedata[i] & 0xff);
					} else {
						l = ((long) (filedata[i] & 0xff) << 56) | ((long) (filedata[i + 1] & 0xff) << 48) | ((long) (filedata[i + 2] & 0xff) << 40) | ((long) (filedata[i + 3] & 0xff) << 32)
								| ((long) (filedata[i + 4] & 0xff) << 24) | ((long) (filedata[i + 5] & 0xff) << 16) | ((long) (filedata[i + 6] & 0xff) << 8) | (long) (filedata[i + 7] & 0xff);
					}
					imageobject.set(y, x, b, Double.longBitsToDouble(l));
					break;

				case UNSIGNED_INTEGER:
					if (enviheader.endian == 0) {
						imageobject.set(y, x, b, ((filedata[i + 1] & 0xff) << 8) + (filedata[i] & 0xff));
					} else {
						imageobject.set(y, x, b, ((filedata[i] & 0xff) << 8) + (filedata[i + 1] & 0xff));
					}
					break;

				case UNSIGNED_LONG:
					if (enviheader.endian == 0) {
						imageobject.set(y, x, b, ((filedata[i + 3] & 0xff) << 24) | ((filedata[i + 2] & 0xff) << 16) | ((filedata[i + 1] & 0xff) << 8) | (filedata[i] & 0xff));
					} else {
						imageobject.set(y, x, b, ((filedata[i] & 0xff) << 24) | ((filedata[i + 1] & 0xff) << 16) | ((filedata[i + 2] & 0xff) << 8) | (filedata[i + 3] & 0xff));
					}
					break;

				case UNSIGNED_64INTEGER:
					if (enviheader.endian == 0) {
						imageobject.set(y, x, b, ((long) (filedata[i + 7] & 0xff) << 56) | ((long) (filedata[i + 6] & 0xff) << 48) | ((long) (filedata[i + 5] & 0xff) << 40)
								| ((long) (filedata[i + 4] & 0xff) << 32) | ((long) (filedata[i + 3] & 0xff) << 24) | ((long) (filedata[i + 2] & 0xff) << 16) | ((long) (filedata[i + 1] & 0xff) << 8)
								| (long) (filedata[i] & 0xff));
					} else {
						imageobject.set(y, x, b, ((long) (filedata[i] & 0xff) << 56) | ((long) (filedata[i + 1] & 0xff) << 48) | ((long) (filedata[i + 2] & 0xff) << 40)
								| ((long) (filedata[i + 3] & 0xff) << 32) | ((long) (filedata[i + 4] & 0xff) << 24) | ((long) (filedata[i + 5] & 0xff) << 16) | ((long) (filedata[i + 6] & 0xff) << 8)
								| (long) (filedata[i + 7] & 0xff));
					}
					break;

				case UNSIGNED_64LONG:
					if (enviheader.endian == 0) {
						imageobject.set(y, x, b, ((long) (filedata[i + 7] & 0xff) << 56) | ((long) (filedata[i + 6] & 0xff) << 48) | ((long) (filedata[i + 5] & 0xff) << 40)
								| ((long) (filedata[i + 4] & 0xff) << 32) | ((long) (filedata[i + 3] & 0xff) << 24) | ((long) (filedata[i + 2] & 0xff) << 16) | ((long) (filedata[i + 1] & 0xff) << 8)
								| (long) (filedata[i] & 0xff));
					} else {
						imageobject.set(y, x, b, ((long) (filedata[i] & 0xff) << 56) | ((long) (filedata[i + 1] & 0xff) << 48) | ((long) (filedata[i + 2] & 0xff) << 40)
								| ((long) (filedata[i + 3] & 0xff) << 32) | ((long) (filedata[i + 4] & 0xff) << 24) | ((long) (filedata[i + 5] & 0xff) << 16) | ((long) (filedata[i + 6] & 0xff) << 8)
								| (long) (filedata[i + 7] & 0xff));
					}
					break;

				default:
					throw (new IOException("Don't understand type method."));
				}
			}
		}

		return start;
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
		if (ext == null) {
			return false;
		}
		return ext.equals("hdr");
	}

	/**
	 * This function will write the imageobject to a file.
	 * 
	 * @param filename
	 *            of the file to be written.
	 * @param imageobject
	 *            the image image to be written.
	 * @throws IOException
	 *             if the file could not be written.
	 */
	public void writeImage(String filename, ImageObject imageobject) throws IOException {
		if (!filename.toLowerCase().endsWith(".hdr")) {
			throw (new IOException("Can not save image."));
		}

		File headerfile = new File(filename);
		String name = filename.substring(0, filename.length() - 4);
		File datafile = new File(name + ".fla");

		saveHeader(headerfile, imageobject);
		saveData(datafile, imageobject);
	}

	/**
	 * Return a list of extentions this class can write.
	 * 
	 * @return a list of extentions that are understood by this class.
	 */
	public String[] writeExt() {
		return new String[] { "hdr" };
	}

	/**
	 * Write the header of the ENVI file. Write as key = val list, unlike a property file, this can have arrays. In case
	 * of arrays, values are seperated by a comma, and teh who array is encapsulaed in braces.
	 * 
	 * @param headerfile
	 *            the file to which to write the header.
	 * @param imageobject
	 *            the image whose properties to write.
	 * @throws IOException
	 *             if an error happened writing to file.
	 */
	private void saveHeader(File headerfile, ImageObject imageobject) throws IOException {
		// save the header file
		BufferedWriter w = new BufferedWriter(new FileWriter(headerfile));
		w.write("ENVI");
		w.newLine();
		if (imageobject.getProperty(ImageObject.COMMENT) != null) {
			w.write("description = {");
			w.newLine();
			w.write("    " + imageobject.getProperty(ImageObject.COMMENT));
			w.newLine();
			w.write("}");
			w.newLine();
		}
		w.write("samples = " + imageobject.getNumCols());
		w.newLine();
		w.write("lines = " + imageobject.getNumRows());
		w.newLine();
		w.write("bands = " + imageobject.getNumBands());
		w.newLine();
		w.write("header offset = 0");
		w.newLine();
		w.write("file type = ENVI Standard");
		w.newLine();
		w.write("interleave = BIP");
		w.newLine();
		// TODO use smaller types if possible?
		switch (imageobject.getType()) {
		case ImageObject.TYPE_BYTE:
			w.write("data type = " + BYTE);
			break;

		case ImageObject.TYPE_SHORT:
			w.write("data type = " + SIGNED_INTEGER);
			break;

		case ImageObject.TYPE_USHORT:
			w.write("data type = " + UNSIGNED_INTEGER);
			break;

		case ImageObject.TYPE_INT:
			w.write("data type = " + SIGNED_LONG);
			break;

		case ImageObject.TYPE_LONG:
			w.write("data type = " + UNSIGNED_64INTEGER); // data converted to unsigned 64 bit integer
			break;

		case ImageObject.TYPE_FLOAT:
			w.write("data type = " + FLOAT);
			break;

		case ImageObject.TYPE_DOUBLE:
			w.write("data type = " + DOUBLE);
			break;

		default:
			throw (new IOException("Can't save this type of image."));
		}
		w.newLine();
		w.write("byte order = 0");
		w.newLine();

		// write geo info if any.
		// GeoInformation geo = (GeoInformation) imageobject.getProperty(ImageObject.GEOINFO);
		Projection geo = (Projection) imageobject.getProperty(ImageObject.GEOINFO);

		// public static final int GEOGRAPHIC = 0;

		/** */
		// public static final int ALBERS_EQUAL_AREA_CONIC = 1;
		/** */
		// public static final int LAMBERT_AZIMUTHAL_EQUAL_AREA = 2;
		/** */
		// public static final int SINUSOIDAL = 3;
		/** */
		// public static final int UTM_NORTHERN_HEMISPHERE = 4;
		// 2 = Southern Hemisphere UTM - not supported
		// 3 = OSGB 1936 - not supported
		// 4 = USGS DEM data
		// 98 = testing model
		// 99 = testing model
		if (geo != null) {
			switch (geo.getType()) {
			case Projection.UTM_NORTHERN_HEMISPHERE:
				// 4 = Universal Transverse Mercator projection in the
				// Northern Hemisphere
				UTMNorth north = (UTMNorth) geo;
				w.write("map info = {UTM, ");
				w.write(north.getRasterSpaceI() + ", ");
				w.write(north.getRasterSpaceJ() + ", ");

				w.write(north.GetModelSpaceX() + ", ");
				w.write(north.GetModelSpaceY() + ", ");

				w.write(north.GetColumnResolution() + ", ");
				w.write(north.GetRowResolution() + ", ");

				w.write(north.getUTMZone() + ", North}");
				w.newLine();
				break;
			case Projection.ALBERS_EQUAL_AREA_CONIC:
				// Albers
				AlbersEqualAreaConic myProj = (AlbersEqualAreaConic) geo;
				w.write("map info = {ALBERS_EQUAL_AREA_CONIC, ");
				w.write(myProj.getRasterSpaceI() + ", ");
				w.write(myProj.getRasterSpaceJ() + ", ");

				w.write(myProj.GetModelSpaceX() + ", ");
				w.write(myProj.GetModelSpaceY() + ", ");

				w.write(myProj.GetColumnResolution() + ", ");
				w.write(myProj.GetRowResolution() + ", ");

				w.write(myProj.getFirstStandardParallel() + ", ");
				w.write(myProj.getSecondStandardParallel() + ", ");
				w.write(myProj.getLatOrigin() + ", ");
				w.write(myProj.getLngOrigin() + ", ");
				w.write(myProj.getRadius() + ", ");
				w.write(myProj.getEccentricitySquared() + ", ");

				// w.write(myProj.getUTMZone() + ", North}");
				w.newLine();
				break;

			/*
			 * case 2: w.write("map info = {UTM, "); w.write(geo.GetRasterSpaceI() + ", ");
			 * w.write(geo.GetRasterSpaceJ() + ", "); w.write(geo.GetModelSpaceX() + ", "); w.write(geo.GetModelSpaceY() + ",
			 * "); w.write(geo.GetColumnResolution() + ", "); w.write(geo.GetRowResolution() + ", ");
			 * w.write(geo.GetUTMZone() + ", South}"); w.newLine(); break;
			 */
			case Projection.GEOGRAPHIC:
				// USGS DEM data
				w.write("map info = {DEM, ");
				w.write(geo.GetGeoScaleX() + ", ");

				w.write(geo.GetMaxWestLng() + ", ");
				w.write(geo.GetMaxSouthLat() + "}");
				w.newLine();
				break;
			}
		}

		if (imageobject.getProperty("sensortype") != null) {
			w.write("sensor type = " + imageobject.getProperty("sensortype"));
			w.newLine();
		}
		if (imageobject.getProperty(ImageObject.DEFAULT_RGB) != null) {
			int[] rgb = (int[]) imageobject.getProperty(ImageObject.DEFAULT_RGB);
			w.write("default bands = {" + (rgb[0] + 1) + "," + (rgb[1] + 1) + "," + (rgb[2] + 1) + "}");
			w.newLine();
		}
		if (imageobject.getProperty(ImageObject.BAND_LABELS) != null) {
			String[] bandnames = (String[]) imageobject.getProperty(ImageObject.BAND_LABELS);
			w.write("band names = {");
			w.newLine();
			for (String element : bandnames) {
				w.write("    " + element + ",");
				w.newLine();
			}
			w.write("}");
			w.newLine();
		}
		if (imageobject.getProperty(ImageObject.WAVELENGTH) != null) {
			String[] wavelength = (String[]) imageobject.getProperty(ImageObject.WAVELENGTH);
			w.write("wavelength = {");
			w.newLine();
			for (String element : wavelength) {
				w.write("    " + element + ",");
				w.newLine();
			}
			w.write("}");
			w.newLine();
		}
		if (imageobject.getProperty("fwhm") != null) {
			String[] fwhm = (String[]) imageobject.getProperty("fwhm");
			w.write("fwhm = {");
			w.newLine();
			for (String element : fwhm) {
				w.write("    " + element + ",");
				w.newLine();
			}
			w.write("}");
			w.newLine();
		}

		w.close();
	}

	/**
	 * Write the data of the image. Converts the image to an array of bytes that is written to disk. The data is always
	 * written band interleave pixel, same way it is stored in memory, and little endian.
	 * 
	 * @param datafile
	 * @param imageobject
	 * @throws IOException
	 */
	private void saveData(File datafile, ImageObject imageobject) throws IOException {
		FileOutputStream w = new FileOutputStream(datafile);

		byte[] filedata;
		int maxsize = 0;
		int count = 0;
		int samples = imageobject.getSize();
		switch (imageobject.getType()) {
		case ImageObject.TYPE_BYTE:
			w.write((byte[]) imageobject.getData());
			break;

		case ImageObject.TYPE_SHORT:
		case ImageObject.TYPE_USHORT:
			maxsize = sampleread * 2;
			filedata = new byte[maxsize];
			short[] shortdata = (short[]) imageobject.getData();
			for (int i = 0; i < samples; i++) {
				filedata[count++] = (byte) (shortdata[i] & 0xff);
				filedata[count++] = (byte) ((shortdata[i] >> 8) & 0xff);
				if (count == maxsize) {
					w.write(filedata);
					count = 0;
				}
				ImageLoader.fireProgress(i, samples);
			}
			if (count > 0) {
				w.write(filedata, 0, count);
			}
			break;

		case ImageObject.TYPE_INT:
			maxsize = sampleread * 4;
			filedata = new byte[maxsize];
			int[] intdata = (int[]) imageobject.getData();
			for (int i = 0; i < samples; i++) {
				filedata[count++] = (byte) (intdata[i] & 0xff);
				filedata[count++] = (byte) ((intdata[i] >> 8) & 0xff);
				filedata[count++] = (byte) ((intdata[i] >> 16) & 0xff);
				filedata[count++] = (byte) ((intdata[i] >> 24) & 0xff);
				if (count == maxsize) {
					w.write(filedata);
					count = 0;
				}
				ImageLoader.fireProgress(i, samples);
			}
			if (count > 0) {
				w.write(filedata, 0, count);
			}
			break;

		case ImageObject.TYPE_LONG:
			maxsize = sampleread * 8;
			filedata = new byte[maxsize];
			long[] longdata = (long[]) imageobject.getData();
			for (int i = 0; i < samples; i++) {
				filedata[count++] = (byte) (longdata[i] & 0xff);
				filedata[count++] = (byte) ((longdata[i] >> 8) & 0xff);
				filedata[count++] = (byte) ((longdata[i] >> 16) & 0xff);
				filedata[count++] = (byte) ((longdata[i] >> 24) & 0xff);
				filedata[count++] = (byte) ((longdata[i] >> 32) & 0xff);
				filedata[count++] = (byte) ((longdata[i] >> 40) & 0xff);
				filedata[count++] = (byte) ((longdata[i] >> 48) & 0xff);
				filedata[count++] = (byte) ((longdata[i] >> 56) & 0xff);
				if (count == maxsize) {
					w.write(filedata);
					count = 0;
				}
				ImageLoader.fireProgress(i, samples);
			}
			if (count > 0) {
				w.write(filedata, 0, count);
			}
			break;

		case ImageObject.TYPE_FLOAT:
			maxsize = sampleread * 4;
			filedata = new byte[maxsize];
			float[] floatdata = (float[]) imageobject.getData();
			int t;
			for (int i = 0; i < samples; i++) {
				t = Float.floatToIntBits(floatdata[i]);
				filedata[count++] = (byte) (t & 0xff);
				filedata[count++] = (byte) ((t >> 8) & 0xff);
				filedata[count++] = (byte) ((t >> 16) & 0xff);
				filedata[count++] = (byte) ((t >> 24) & 0xff);
				if (count == maxsize) {
					w.write(filedata);
					count = 0;
				}
				ImageLoader.fireProgress(i, samples);
			}
			if (count > 0) {
				w.write(filedata, 0, count);
			}
			break;

		case ImageObject.TYPE_DOUBLE:
			maxsize = sampleread * 8;
			filedata = new byte[maxsize];
			double[] doubledata = (double[]) imageobject.getData();
			long l;
			for (int i = 0; i < samples; i++) {
				l = Double.doubleToLongBits(doubledata[i]);
				filedata[count++] = (byte) (l & 0xff);
				filedata[count++] = (byte) ((l >> 8) & 0xff);
				filedata[count++] = (byte) ((l >> 16) & 0xff);
				filedata[count++] = (byte) ((l >> 24) & 0xff);
				filedata[count++] = (byte) ((l >> 32) & 0xff);
				filedata[count++] = (byte) ((l >> 40) & 0xff);
				filedata[count++] = (byte) ((l >> 48) & 0xff);
				filedata[count++] = (byte) ((l >> 56) & 0xff);
				if (count == maxsize) {
					w.write(filedata);
					count = 0;
				}
				ImageLoader.fireProgress(i, samples);
			}
			if (count > 0) {
				w.write(filedata, 0, count);
			}
			break;

		default:
			throw (new IOException("Don't understand type."));
		}

		w.close();
	}

	public int getImageCount(String filename) {
		return 1;
	}

	public ImageObject readImage(String filename, int index, SubArea subarea, int sampling) throws IOException, ImageException {
		return readImage(filename, subarea, sampling);
	}

	public ImageObject readImageHeader(String filename, int index) throws IOException, ImageException {
		return readImageHeader(filename);
	}

	/**
	 * Class to hold all image specific variables, this way the loader itself is reentrant.
	 */
	class EnviHeader {
		public String	description		= null;
		public int		samples			= 0;
		public int		lines			= 0;
		public int		bands			= 0;
		public int		filesamples		= 0;
		public int		filelines		= 0;
		public int		filebands		= 0;
		public int		type			= 0;
		public int		typesize		= 0;
		public int		endian			= 0;
		public int		interleave		= 0;
		public String[]	wavelength		= null;
		public String[]	bandname		= null;
		public String[]	fwhm			= null;
		public int		defaultRed		= -1;
		public int		defaultGreen	= -1;
		public int		defaultBlue		= -1;
		public int		defaultGray		= -1;
		public int		headerOffset	= 0;
		public int		xstart			= 0;
		public int		ystart			= 0;
		public String	sensorType		= null;
		public String[]	mapinfo			= null;
		public SubArea	subarea			= null;
		public int		sampling		= 1;

		public int		maxdata			= 0;
		public int		maxfile			= 0;
	}
}
