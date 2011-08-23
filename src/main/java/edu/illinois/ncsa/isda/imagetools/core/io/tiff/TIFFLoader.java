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
package edu.illinois.ncsa.isda.imagetools.core.io.tiff;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageException;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.imagetools.core.datatype.SubArea;
import edu.illinois.ncsa.isda.imagetools.core.display.About;
import edu.illinois.ncsa.isda.imagetools.core.geo.AngularUnit;
import edu.illinois.ncsa.isda.imagetools.core.geo.Datum;
import edu.illinois.ncsa.isda.imagetools.core.geo.GeoException;
import edu.illinois.ncsa.isda.imagetools.core.geo.GeoGraphicCoordinateSystem;
import edu.illinois.ncsa.isda.imagetools.core.geo.LinearUnit;
import edu.illinois.ncsa.isda.imagetools.core.geo.PrimeMeridian;
import edu.illinois.ncsa.isda.imagetools.core.geo.Projection;
import edu.illinois.ncsa.isda.imagetools.core.geo.ProjectionConvert;
import edu.illinois.ncsa.isda.imagetools.core.geo.TiePoint;
import edu.illinois.ncsa.isda.imagetools.core.geo.Projection.ProjectionType;
import edu.illinois.ncsa.isda.imagetools.core.io.ImageLoader;
import edu.illinois.ncsa.isda.imagetools.core.io.ImageReader;
import edu.illinois.ncsa.isda.imagetools.core.io.ImageWriter;

public class TIFFLoader implements ImageReader, ImageWriter {
	private static Log		logger	= LogFactory.getLog(TIFFLoader.class);

	final static String[]	suffix	= new String[] { "tiff", "tif" };

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
		if ((hdr[0] == 'I') && (hdr[1] == 'I') && (hdr[2] == 42) && (hdr[3] == 0)) {
			return true;
		}
		if ((hdr[0] == 'M') && (hdr[1] == 'M') && (hdr[2] == 0) && (hdr[3] == 42)) {
			return true;
		}
		// bigtiff
		if ((hdr[0] == 'I') && (hdr[1] == 'I') && (hdr[2] == 43) && (hdr[3] == 0)) {
			return true;
		}
		if ((hdr[0] == 'M') && (hdr[1] == 'M') && (hdr[2] == 0) && (hdr[3] == 43)) {
			return true;
		}
		return false;
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
	 * @throws IOException
	 *             if an error occurrs reading the file.
	 */
	public ImageObject readImage(String filename, SubArea subarea, int sampling) throws IOException {
		return readImage(filename, subarea, sampling, false);
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
		return readImage(filename, null, 1, true);
	}

	/**
	 * Return a list of extentions this class can read.
	 * 
	 * @return a list of extentions that are understood by this class.
	 */
	public String[] readExt() {
		return suffix;
	}

	private ImageObject readImage(String filename, SubArea subarea, int sampling, boolean header) throws IOException {
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(filename, "r");
			boolean littleendian = false;
			boolean bigtiff = false;
			byte hdr[] = new byte[16];

			if (raf.read(hdr, 0, 16) != 16) {
				throw (new IOException("Could not read header."));
			}

			// determine endianess
			if ((hdr[0] == 'I') && (hdr[1] == 'I')) {
				littleendian = true;
			}

			// determine bigtiff
			bigtiff = (getWord(hdr, 2, littleendian) == 43);

			long offset = 0;
			if (bigtiff) {
				// bigtiff skips a few bytes
				if (getWord(hdr, 4, littleendian) != 8) {
					throw (new IOException("Invalid big tiff file."));
				}
				if (getWord(hdr, 6, littleendian) != 0) {
					throw (new IOException("Invalid big tiff file."));
				}
				// find offset to first directory
				offset = get8Byte(hdr, 8, littleendian);
			} else {
				// find offset to first directory
				offset = getLong(hdr, 4, littleendian);
			}

			// parse images. se while if want to read all images, with if only
			// first image is read.
			TIFFimage tiffimage = null;
			if (offset != 0) {
				tiffimage = new TIFFimage(filename, raf, bigtiff, littleendian, sampling, subarea);
				offset = tiffimage.readDirectory(offset);
				if (offset != 0) {
					logger.debug("Loading first image, ignoring sub IFD's.");
				}
				tiffimage.parseDirectory();
				try {
					tiffimage.createImage();
				} catch (ImageException e) {
					throw (new IOException("Can not read file."));
				}
				if (!header) {
					tiffimage.readImage();
				}

				// overwrite some projection values with those in .tfw file, if
				// it exists.
				try {
					tiffimage.updateProjectionFromTFW(filename);
				} catch (Exception e) {
					logger.debug("TFW file was not found.");
				}
			}

			if (tiffimage == null) {
				throw (new IOException("Can not read file."));
			}
			return tiffimage.getImage();
		} catch (IOException ex) {
			throw ex;
		} finally {
			if (raf != null) {
				raf.close();
			}
		}
	}

	private long getWord(byte[] hdr, int offset, boolean littleendian) {
		if (littleendian) {
			return (hdr[offset + 1] & 0xff) << 8 | (hdr[offset] & 0xff);
		} else {
			return (hdr[offset] & 0xff) << 8 | (hdr[offset + 1] & 0xff);
		}
	}

	private long getLong(byte[] hdr, int offset, boolean littleendian) {
		if (littleendian) {
			return (hdr[offset + 3] & 0xff) << 24 | (hdr[offset + 2] & 0xff) << 16 | (hdr[offset + 1] & 0xff) << 8 | (hdr[offset] & 0xff);
		} else {
			return (hdr[offset] & 0xff) << 24 | (hdr[offset + 1] & 0xff) << 16 | (hdr[offset + 2] & 0xff) << 8 | (hdr[offset + 3] & 0xff);
		}
	}

	private long get8Byte(byte[] hdr, int offset, boolean littleendian) {
		if (littleendian) {
			return (long) (hdr[offset + 7] & 0xff) << 56 | (long) (hdr[offset + 6] & 0xff) << 48 | (long) (hdr[offset + 5] & 0xff) << 40 | (long) (hdr[offset + 4] & 0xff) << 32
					| (long) (hdr[offset + 3] & 0xff) << 24 | (long) (hdr[offset + 2] & 0xff) << 16 | (long) (hdr[offset + 1] & 0xff) << 8 | (hdr[offset] & 0xff);
		} else {
			return (long) (hdr[offset] & 0xff) << 56 | (long) (hdr[offset + 1] & 0xff) << 48 | (long) (hdr[offset + 2] & 0xff) << 40 | (long) (hdr[offset + 3] & 0xff) << 32
					| (long) (hdr[offset + 4] & 0xff) << 24 | (long) (hdr[offset + 5] & 0xff) << 16 | (long) (hdr[offset + 6] & 0xff) << 8 | (hdr[offset + 7] & 0xff);
		}
	}

	/**
	 * Return the description of the reader (or writer).
	 * 
	 * @return decription of the reader (or writer)
	 */
	public String getDescription() {
		return "TIFF Loader";
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
		return ext.equals("tif") || ext.equals("tiff");
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
		if (imageobject.getNumBands() == 2) {
			throw (new IOException("Can not write a 2 band image as tiff."));
		}
		int i;

		// always write in intel format
		byte[] magic = new byte[] { 'I', 'I', 42, 0 };
		byte[] zero = new byte[] { 0 };

		int typesize = 0;
		String software = About.getApplication() + " " + About.getVersion();

		// find out the type of image
		switch (imageobject.getType()) {
		case ImageObject.TYPE_BYTE:
			typesize = 1;
			break;
		case ImageObject.TYPE_SHORT:
		case ImageObject.TYPE_USHORT:
			typesize = 2;
			break;
		case ImageObject.TYPE_LONG:
		case ImageObject.TYPE_INT:
		case ImageObject.TYPE_FLOAT:
		case ImageObject.TYPE_DOUBLE:
			typesize = 4;
			break;
		}

		// create the tags and calculate ifdstart
		long ifdloc = 8;
		Hashtable<Integer, byte[]> tags = new Hashtable<Integer, byte[]>();

		// ORDER IN WHICH TAGS ARE CREATED AND DATA IS WRITTEN IS IMPORTANT. IF
		// YOU INSERT A TAG THAT NEEDS TO WRITE DATA, YOU NEED TO WRITE THE DATA
		// AT THE SAME POINT WHEN DATA IS REALLY WRITTEN TO DISK!!!!!!!

		// imagedata
		long imageloc = ifdloc;
		ifdloc += imageobject.getSize() * typesize;

		// width x height x bands
		tags.put(new Integer(IFDEntry.TAG_ImageWidth), getIFD(IFDEntry.TYPE_LONG, IFDEntry.TAG_ImageWidth, 1, imageobject.getNumCols()));
		tags.put(new Integer(IFDEntry.TAG_ImageLength), getIFD(IFDEntry.TYPE_LONG, IFDEntry.TAG_ImageLength, 1, imageobject.getNumRows()));
		tags.put(new Integer(IFDEntry.TAG_SamplesPerPixel), getIFD(IFDEntry.TYPE_SHORT, IFDEntry.TAG_SamplesPerPixel, 1, imageobject.getNumBands()));

		// no compression
		tags.put(new Integer(IFDEntry.TAG_Compression), getIFD(IFDEntry.TYPE_SHORT, IFDEntry.TAG_Compression, 1, 1));

		// grayscale (1 band) or color (3 or more bands)
		tags.put(new Integer(IFDEntry.TAG_PhotometricInterpretation), getIFD(IFDEntry.TYPE_SHORT, IFDEntry.TAG_PhotometricInterpretation, 1, (imageobject.getNumBands() == 1) ? 1 : 2));

		// special case for float
		if ((imageobject.getType() == ImageObject.TYPE_FLOAT) || (imageobject.getType() == ImageObject.TYPE_DOUBLE)) {
			tags.put(new Integer(IFDEntry.TAG_SampleFormat), getIFD(IFDEntry.TYPE_SHORT, IFDEntry.TAG_SampleFormat, 1, 3));
		}

		// software tag
		tags.put(new Integer(IFDEntry.TAG_Software), getIFD(IFDEntry.TYPE_ASCII, IFDEntry.TAG_Software, software.length() + 1, ifdloc));
		ifdloc += software.length() + 1;

		// strip offset
		tags.put(new Integer(IFDEntry.TAG_StripOffsets), getIFD(IFDEntry.TYPE_LONG, IFDEntry.TAG_StripOffsets, imageobject.getNumRows(), ifdloc));
		ifdloc += imageobject.getNumRows() * 4;

		// strip size
		tags.put(new Integer(IFDEntry.TAG_StripByteCounts), getIFD(IFDEntry.TYPE_LONG, IFDEntry.TAG_StripByteCounts, imageobject.getNumRows(), ifdloc));
		ifdloc += imageobject.getNumRows() * 4;

		// rows per strip
		tags.put(new Integer(IFDEntry.TAG_RowsPerStrip), getIFD(IFDEntry.TYPE_LONG, IFDEntry.TAG_RowsPerStrip, 1, 1));

		// bits per sample
		if (imageobject.getNumBands() == 1) {
			tags.put(new Integer(IFDEntry.TAG_BitsPerSample), getIFD(IFDEntry.TYPE_SHORT, IFDEntry.TAG_BitsPerSample, 1, typesize * 8));
		} else {
			tags.put(new Integer(IFDEntry.TAG_BitsPerSample), getIFD(IFDEntry.TYPE_SHORT, IFDEntry.TAG_BitsPerSample, imageobject.getNumBands(), ifdloc));
			ifdloc += imageobject.getNumBands() * 2;
		}

		// resolution
		if (imageobject.getProperty("Resolution") != null) {
			if (imageobject.getProperty("Resolution").equals("Inch")) {
				tags.put(new Integer(IFDEntry.TAG_ResolutionUnit), getIFD(IFDEntry.TYPE_SHORT, IFDEntry.TAG_ResolutionUnit, 1, 2));
			} else if (imageobject.getProperty("Resolution").equals("Centimeter")) {
				tags.put(new Integer(IFDEntry.TAG_ResolutionUnit), getIFD(IFDEntry.TYPE_SHORT, IFDEntry.TAG_ResolutionUnit, 1, 3));
			} else {
				tags.put(new Integer(IFDEntry.TAG_ResolutionUnit), getIFD(IFDEntry.TYPE_SHORT, IFDEntry.TAG_ResolutionUnit, 1, 1));
			}
		} else {
			tags.put(new Integer(IFDEntry.TAG_ResolutionUnit), getIFD(IFDEntry.TYPE_SHORT, IFDEntry.TAG_ResolutionUnit, 1, 2));
		}
		tags.put(new Integer(IFDEntry.TAG_XResolution), getIFD(IFDEntry.TYPE_RATIONAL, IFDEntry.TAG_XResolution, 1, ifdloc));
		ifdloc += 8;
		tags.put(new Integer(IFDEntry.TAG_YResolution), getIFD(IFDEntry.TYPE_RATIONAL, IFDEntry.TAG_YResolution, 1, ifdloc));
		ifdloc += 8;

		// nodata
		String noval = null;
		if (imageobject.isInvalidDataSet()) {
			noval = Double.toString(imageobject.getInvalidData());
			tags.put(new Integer(IFDEntry.TAG_GDALNoData), getIFD(IFDEntry.TYPE_ASCII, IFDEntry.TAG_GDALNoData, noval.length() + 1, ifdloc));
			ifdloc += noval.length() + 1;
		}
		// COMMENT
		String comment = null;
		if (imageobject.getProperty(ImageObject.COMMENT) != null) {
			comment = imageobject.getProperty(ImageObject.COMMENT).toString();
			tags.put(new Integer(IFDEntry.TAG_ImageDescription), getIFD(IFDEntry.TYPE_ASCII, IFDEntry.TAG_ImageDescription, comment.length() + 1, ifdloc));
			ifdloc += comment.length() + 1;
		}

		//
		// GEO
		Projection proj = null;
		try {
			proj = ProjectionConvert.getNewProjection(imageobject.getProperty(ImageObject.GEOINFO));
		} catch (GeoException exc) {
			logger.warn("Could not write geo information.", exc);
			proj = null;
		}
		HashMap<Integer, Integer[]> GeoKeyDirectory = null;
		ArrayList<Double> GeoDoubleParams = null;
		String GeoAsciiParams = null;
		Integer intzero = new Integer(0);
		Integer intone = new Integer(1);
		if (proj != null) {
			GeoKeyDirectory = new HashMap<Integer, Integer[]>();
			GeoDoubleParams = new ArrayList<Double>();
			GeoAsciiParams = "";

			// projection type
			if (proj.getType().equals(ProjectionType.Geographic)) {
				GeoKeyDirectory.put(GeoEntry.GTModelTypeGeoKey, new Integer[] { intzero, intone, GeoEntry.ModelTypeGeographic });
			} else {
				GeoKeyDirectory.put(GeoEntry.GTModelTypeGeoKey, new Integer[] { intzero, intone, GeoEntry.ModelTypeProjected });
			}

			// pixel area size
			GeoKeyDirectory.put(GeoEntry.GTRasterTypeGeoKey, new Integer[] { intzero, intone, GeoEntry.RasterPixelIsArea });

			// name of projection
			if ((proj.getName() != null) && !proj.getName().equals("")) {
				GeoKeyDirectory.put(GeoEntry.GTCitationGeoKey, new Integer[] { IFDEntry.TAG_GeoAsciiParams, proj.getName().length() + 1, GeoAsciiParams.length() });
				GeoAsciiParams += proj.getName() + "|";
			}

			// geographic coordinate system, user defined by default
			GeoGraphicCoordinateSystem geogcs = proj.getGeographicCoordinateSystem();
			if (false) {
				GeoKeyDirectory.put(GeoEntry.GeographicTypeGeoKey, new Integer[] { intzero, intone, GeoEntry.GCS_WGS_84 });
			} else {
				// user defined geographi coordinate system
				GeoKeyDirectory.put(GeoEntry.GeographicTypeGeoKey, new Integer[] { intzero, intone, GeoEntry.UserDefined });

				// datum, user defined by default
				if (Datum.WGS_1984.equals(geogcs.getDatum())) {
					GeoKeyDirectory.put(GeoEntry.GeogGeodeticDatumGeoKey, new Integer[] { intzero, intone, GeoEntry.Datum_WGS84 });
				} else {
					// user defined datum
					GeoKeyDirectory.put(GeoEntry.GeogGeodeticDatumGeoKey, new Integer[] { intzero, intone, GeoEntry.UserDefined });

					// ellipsoid
					// if (Ellipsoid.WGS_1984.equals(geogcs.))
					logger.warn("Need to add more datums!");
				}

				// prime meridian
				if (PrimeMeridian.Greenwich.equals(geogcs.getPrimeMeridian())) {
					GeoKeyDirectory.put(GeoEntry.GeogPrimeMeridianGeoKey, new Integer[] { intzero, intone, GeoEntry.PM_Greenwich });
				} else {
					GeoKeyDirectory.put(GeoEntry.GeogPrimeMeridianGeoKey, new Integer[] { intzero, intone, GeoEntry.UserDefined });
					logger.warn("Need to add more prime meridians!");
				}
			}

			// name of geogcs
			if ((geogcs.getName() != null) && !geogcs.getName().equals("")) {
				GeoKeyDirectory.put(GeoEntry.GeogCitationGeoKey, new Integer[] { IFDEntry.TAG_GeoAsciiParams, geogcs.getName().length() + 1, GeoAsciiParams.length() });
				GeoAsciiParams += geogcs.getName() + "|";
			}

			// angular units
			if (AngularUnit.Radian.equals(geogcs.getAngularUnit())) {
				GeoKeyDirectory.put(GeoEntry.GeogAngularUnitsGeoKey, new Integer[] { intzero, intone, GeoEntry.Angular_Radian });
			} else if (AngularUnit.Decimal_Degree.equals(geogcs.getAngularUnit())) {
				GeoKeyDirectory.put(GeoEntry.GeogAngularUnitsGeoKey, new Integer[] { intzero, intone, GeoEntry.Angular_Degree });

			} else {
				logger.warn("Need to add more angular units!");
			}

			// this can be skipped in case of geographic projection
			if (!proj.getType().equals(ProjectionType.Geographic)) {
				// project, always user defined
				GeoKeyDirectory.put(GeoEntry.ProjectedCSTypeGeoKey, new Integer[] { intzero, intone, GeoEntry.UserDefined });

				// name of projection coordinate system
				// if ((proj.getName() != null) && !proj.getName().equals("")) {
				// GeoKeyDirectory.put(GeoEntry.PCSCitationGeoKey, new Integer[]
				// { IFDEntry.TAG_GeoAsciiParams,
				// proj.getName().length(), GeoAsciiParams.length() });
				// GeoAsciiParams += proj.getName() + "|";
				// }

				// project type, always user defined
				GeoKeyDirectory.put(GeoEntry.ProjectionGeoKey, new Integer[] { intzero, intone, GeoEntry.UserDefined });

				// projection coordinate transformation
				switch (proj.getType()) {
				case Albers:
					GeoKeyDirectory.put(GeoEntry.ProjCoordTransGeoKey, new Integer[] { intzero, intone, GeoEntry.CT_AlbersEqualArea });
					break;
				case Lambert_Azimuthal_Equal_Area:
					GeoKeyDirectory.put(GeoEntry.ProjCoordTransGeoKey, new Integer[] { intzero, intone, GeoEntry.CT_AlbersEqualArea });
					break;
				case Lambert_Conformal_Conic:
					GeoKeyDirectory.put(GeoEntry.ProjCoordTransGeoKey, new Integer[] { intzero, intone, GeoEntry.CT_LambertConfConic });
					break;
				case Sinusoidal:
					GeoKeyDirectory.put(GeoEntry.ProjCoordTransGeoKey, new Integer[] { intzero, intone, GeoEntry.CT_Sinusoidal });
					break;
				case Transverse_Mercator:
					GeoKeyDirectory.put(GeoEntry.ProjCoordTransGeoKey, new Integer[] { intzero, intone, GeoEntry.CT_TransverseMercator });
					break;
				case Mercator:
					GeoKeyDirectory.put(GeoEntry.ProjCoordTransGeoKey, new Integer[] { intzero, intone, GeoEntry.CT_Mercator });
					break;
				default:
					logger.warn(String.format("Projection %s is not defined.", proj.getType()));
				}

				// linear unit
				if (LinearUnit.Meter.equals(proj.getUnit())) {
					GeoKeyDirectory.put(GeoEntry.ProjLinearUnitsGeoKey, new Integer[] { intzero, intone, GeoEntry.Linear_Meter });
				} else {
					logger.warn("Need to add more linear units!");
				}

				// parameters
				if (proj.hasParameter(Projection.STANDARD_PARALLEL_1)) {
					try {
						GeoDoubleParams.add(proj.getParameterNumber(Projection.STANDARD_PARALLEL_1));
						GeoKeyDirectory.put(GeoEntry.ProjStdParallel1GeoKey, new Integer[] { IFDEntry.TAG_GeoDoubleParams, intone, GeoDoubleParams.size() - 1 });
					} catch (GeoException exc) {
						logger.warn("Could not retrieve STANDARD_PARALLEL_1.", exc);
					}
				}

				if (proj.hasParameter(Projection.STANDARD_PARALLEL_2)) {
					try {
						GeoDoubleParams.add(proj.getParameterNumber(Projection.STANDARD_PARALLEL_2));
						GeoKeyDirectory.put(GeoEntry.ProjStdParallel2GeoKey, new Integer[] { IFDEntry.TAG_GeoDoubleParams, intone, GeoDoubleParams.size() - 1 });
					} catch (GeoException exc) {
						logger.warn("Could not retrieve STANDARD_PARALLEL_2.", exc);
					}
				}

				if (proj.hasParameter(Projection.LONGITUDE_OF_ORIGIN)) {
					try {
						GeoDoubleParams.add(proj.getParameterNumber(Projection.LONGITUDE_OF_ORIGIN));
						GeoKeyDirectory.put(GeoEntry.ProjNatOriginLongGeoKey, new Integer[] { IFDEntry.TAG_GeoDoubleParams, intone, GeoDoubleParams.size() - 1 });
					} catch (GeoException exc) {
						logger.warn("Could not retrieve LONGITUDE_OF_ORIGIN.", exc);
					}
				}

				if (proj.hasParameter(Projection.LATITUDE_OF_ORIGIN)) {
					try {
						GeoDoubleParams.add(proj.getParameterNumber(Projection.LATITUDE_OF_ORIGIN));
						GeoKeyDirectory.put(GeoEntry.ProjNatOriginLatGeoKey, new Integer[] { IFDEntry.TAG_GeoDoubleParams, intone, GeoDoubleParams.size() - 1 });
					} catch (GeoException exc) {
						logger.warn("Could not retrieve LATITUDE_OF_ORIGIN.", exc);
					}
				}

				/*
				 * longitude_of_center The longitude that defines the center point of the map projection.
				 * latitude_of_center The latitude that defines the center point of the map projection.
				 * longitude_of_origin The longitude chosen as the origin of x-coordinates. latitude_of_origin The
				 * latitude chosen as the origin of y-coordinates. description is from
				 * http://publib.boulder.ibm.com/infocenter/db2luw
				 * /v8/index.jsp?topic=/com.ibm.db2.udb.doc/opt/rsbp4119.htm
				 */

				if (proj.hasParameter(Projection.LONGITUDE_OF_CENTER)) {
					try {
						GeoDoubleParams.add(proj.getParameterNumber(Projection.LONGITUDE_OF_CENTER));
						GeoKeyDirectory.put(GeoEntry.ProjCenterLongGeoKey, new Integer[] { IFDEntry.TAG_GeoDoubleParams, intone, GeoDoubleParams.size() - 1 });
					} catch (GeoException exc) {
						logger.warn("Could not retrieve LONGITUDE_OF_CENTER.", exc);
					}
				}

				if (proj.hasParameter(Projection.LATITUDE_OF_CENTER)) {
					try {
						GeoDoubleParams.add(proj.getParameterNumber(Projection.LATITUDE_OF_CENTER));
						GeoKeyDirectory.put(GeoEntry.ProjCenterLatGeoKey, new Integer[] { IFDEntry.TAG_GeoDoubleParams, intone, GeoDoubleParams.size() - 1 });
					} catch (GeoException exc) {
						logger.warn("Could not retrieve LATITUDE_OF_CENTER.", exc);
					}
				}

				if (proj.hasParameter(Projection.FALSE_EASTING)) {
					try {
						GeoDoubleParams.add(proj.getParameterNumber(Projection.FALSE_EASTING));
						GeoKeyDirectory.put(GeoEntry.ProjFalseEastingGeoKey, new Integer[] { IFDEntry.TAG_GeoDoubleParams, intone, GeoDoubleParams.size() - 1 });
					} catch (GeoException exc) {
						logger.warn("Could not retrieve FALSE_EASTING.", exc);
					}
				}

				if (proj.hasParameter(Projection.FALSE_NORTHING)) {
					try {
						GeoDoubleParams.add(proj.getParameterNumber(Projection.FALSE_NORTHING));
						GeoKeyDirectory.put(GeoEntry.ProjFalseNorthingGeoKey, new Integer[] { IFDEntry.TAG_GeoDoubleParams, intone, GeoDoubleParams.size() - 1 });
					} catch (GeoException exc) {
						logger.warn("Could not retrieve FALSE_NORTHING.", exc);
					}
				}
			}

			if (GeoDoubleParams.size() == 0) {
				GeoDoubleParams.add(new Double(0));
			}

			// store the geokeys in the regular tags
			int geokeys = (GeoKeyDirectory.size() + 1);
			tags.put(new Integer(IFDEntry.TAG_GeoKeyDirectory), getIFD(IFDEntry.TYPE_SHORT, IFDEntry.TAG_GeoKeyDirectory, 4 * geokeys, ifdloc));
			ifdloc += geokeys * 8;
			// assume 1 tiepoint
			tags.put(new Integer(IFDEntry.TAG_ModelTiepoint), getIFD(IFDEntry.TYPE_DOUBLE, IFDEntry.TAG_ModelTiepoint, 6, ifdloc));
			ifdloc += 6 * 8;
			tags.put(new Integer(IFDEntry.TAG_ModelPixelScale), getIFD(IFDEntry.TYPE_DOUBLE, IFDEntry.TAG_ModelPixelScale, 3, ifdloc));
			ifdloc += 3 * 8;
			// add the ascii params
			tags.put(new Integer(IFDEntry.TAG_GeoAsciiParams), getIFD(IFDEntry.TYPE_ASCII, IFDEntry.TAG_GeoAsciiParams, GeoAsciiParams.length() + 1, ifdloc));
			ifdloc += GeoAsciiParams.length() + 1;
			// add the double params
			tags.put(new Integer(IFDEntry.TAG_GeoDoubleParams), getIFD(IFDEntry.TYPE_DOUBLE, IFDEntry.TAG_GeoDoubleParams, GeoDoubleParams.size(), ifdloc));
			ifdloc += 8 * GeoDoubleParams.size();

		}

		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(filename);

			// Writing data to disk, see warning above.
			// write the magic number
			fos.write(magic);

			// write ifdloc
			write(fos, ifdloc);

			// write image as strip, single row per strip
			int r, c, b;
			long stripsize = imageobject.getNumCols() * typesize * imageobject.getNumBands();
			byte[] strip = new byte[(int) stripsize];

			switch (imageobject.getType()) {

			case ImageObject.TYPE_BYTE:
				for (r = 0; r < imageobject.getNumRows(); r++) {
					for (i = 0, c = 0; c < imageobject.getNumCols(); c++) {
						for (b = 0; b < imageobject.getNumBands(); b++) {
							strip[i++] = imageobject.getByte(r, c, b);
						}
					}
					fos.write(strip);
				}
				break;

			case ImageObject.TYPE_USHORT:
				logger.debug("Saving USHORT image as short.");

			case ImageObject.TYPE_SHORT:
				for (r = 0; r < imageobject.getNumRows(); r++) {
					for (i = 0, c = 0; c < imageobject.getNumCols(); c++) {
						for (b = 0; b < imageobject.getNumBands(); b++) {
							short value = imageobject.getShort(r, c, b);
							strip[i++] = (byte) ((value & 0x00ff));
							strip[i++] = (byte) ((value & 0xff00) >> 8);
						}
					}
					fos.write(strip);
				}
				break;

			case ImageObject.TYPE_LONG:
				logger.debug("Saving LONG image as int.");

			case ImageObject.TYPE_INT:
				for (r = 0; r < imageobject.getNumRows(); r++) {
					for (i = 0, c = 0; c < imageobject.getNumCols(); c++) {
						for (b = 0; b < imageobject.getNumBands(); b++) {
							int value = imageobject.getInt(r, c, b);
							strip[i++] = (byte) ((value & 0x000000ff));
							strip[i++] = (byte) ((value & 0x0000ff00) >> 8);
							strip[i++] = (byte) ((value & 0x00ff0000) >> 16);
							strip[i++] = (byte) ((value & 0xff000000) >> 24);
						}
					}
					fos.write(strip);
				}
				break;

			case ImageObject.TYPE_DOUBLE:
				logger.debug("Saving DOUBLE image as float.");

			case ImageObject.TYPE_FLOAT:
				int fltoi;
				for (r = 0; r < imageobject.getNumRows(); r++) {
					for (i = 0, c = 0; c < imageobject.getNumCols(); c++) {
						for (b = 0; b < imageobject.getNumBands(); b++) {
							fltoi = Float.floatToIntBits(imageobject.getFloat(r, c, b));
							strip[i++] = (byte) ((fltoi & 0x000000ff));
							strip[i++] = (byte) ((fltoi & 0x0000ff00) >> 8);
							strip[i++] = (byte) ((fltoi & 0x00ff0000) >> 16);
							strip[i++] = (byte) ((fltoi & 0xff000000) >> 24);
						}
					}
					fos.write(strip);
				}
				break;

			default:
				throw (new IOException("Can not save this image type."));
			}

			// write the software
			fos.write(software.getBytes());
			fos.write(zero);

			// write the strip offset
			for (i = 0; i < imageobject.getNumRows(); i++) {
				write(fos, imageloc);
				imageloc += typesize * imageobject.getNumCols() * imageobject.getNumBands();
			}

			// write the strip size
			for (i = 0; i < imageobject.getNumRows(); i++) {
				write(fos, stripsize);
			}

			// bits per sample
			if (imageobject.getNumBands() > 2) {
				byte[] bps = new byte[imageobject.getNumBands() * 2];
				for (i = 0; i < bps.length;) {
					bps[i++] = (byte) (typesize * 8);
					bps[i++] = 0;
				}
				fos.write(bps);
			}

			// write x and y resolution 1/72 by default
			write(fos, (Rational) imageobject.getProperty("XResolution"));
			write(fos, (Rational) imageobject.getProperty("YResolution"));

			// write noval if set
			if (noval != null) {
				fos.write(noval.getBytes());
				fos.write(zero);
			}

			// write comment if set
			if (comment != null) {
				fos.write(comment.getBytes());
				fos.write(zero);
			}

			// write the geo
			if (proj != null) {
				ArrayList<Integer> keys = new ArrayList<Integer>(GeoKeyDirectory.keySet());
				Collections.sort(keys);

				// write version of geokeys, and number of geokeys
				write(fos, 1);
				write(fos, 1);
				write(fos, 2);
				write(fos, keys.size());

				// next write the geokeys
				for (Integer key : keys) {
					Integer[] val = GeoKeyDirectory.get(key);
					write(fos, key.intValue());
					write(fos, val[0].intValue());
					write(fos, val[1].intValue());
					write(fos, val[2].intValue());
				}

				// write the tiepoint, raster, model, scale
				TiePoint tp = proj.getTiePoint();
				write(fos, tp.getRasterPoint().getX());
				write(fos, tp.getRasterPoint().getY());
				write(fos, tp.getRasterPoint().getZ());
				write(fos, tp.getModelPoint().getX());
				write(fos, tp.getModelPoint().getY());
				write(fos, tp.getModelPoint().getZ());
				write(fos, tp.getScaleX());
				write(fos, -tp.getScaleY());
				write(fos, tp.getScaleZ());

				// write the GeoAsciiParams
				fos.write(GeoAsciiParams.getBytes());
				fos.write(zero);

				// write the GeoDoubleParams
				for (double d : GeoDoubleParams) {
					write(fos, d);
				}
			}

			// write number of entries
			byte[] entries = new byte[2];
			entries[1] = (byte) ((tags.size() & 0xff00) >> 8);
			entries[0] = (byte) ((tags.size() & 0x00ff));
			fos.write(entries);

			// IFD entries must be sorted by number!
			Vector<Integer> v = new Vector<Integer>(tags.keySet());
			Collections.sort(v);
			for (Object element : v) {
				fos.write(tags.get(element));
			}

			// write final ifd entry
			fos.write(new byte[] { 0, 0, 0, 0 });
		} catch (IOException ex) {
			throw ex;
		} finally {
			if (fos != null) {
				fos.close();
			}
		}
	}

	private void write(FileOutputStream fos, int i) throws IOException {
		byte[] buf = new byte[2];
		buf[1] = (byte) ((i >> 8) & 0xff);
		buf[0] = (byte) (i & 0xff);
		fos.write(buf);
	}

	private void write(FileOutputStream fos, long l) throws IOException {
		byte[] buf = new byte[4];
		buf[3] = (byte) ((l >> 24) & 0xff);
		buf[2] = (byte) ((l >> 16) & 0xff);
		buf[1] = (byte) ((l >> 8) & 0xff);
		buf[0] = (byte) (l & 0xff);
		fos.write(buf);
	}

	private void write(FileOutputStream fos, double d) throws IOException {
		byte[] buf = new byte[8];
		long l = Double.doubleToLongBits(d);
		buf[7] = (byte) ((l >> 56) & 0xff);
		buf[6] = (byte) ((l >> 48) & 0xff);
		buf[5] = (byte) ((l >> 40) & 0xff);
		buf[4] = (byte) ((l >> 32) & 0xff);
		buf[3] = (byte) ((l >> 24) & 0xff);
		buf[2] = (byte) ((l >> 16) & 0xff);
		buf[1] = (byte) ((l >> 8) & 0xff);
		buf[0] = (byte) (l & 0xff);
		fos.write(buf);
	}

	private void write(FileOutputStream fos, Rational r) throws IOException {
		byte[] buf = new byte[] { 0x48, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00 };

		if (r != null) {
			buf[3] = (byte) ((r.numerator & 0xff000000) >> 24);
			buf[2] = (byte) ((r.numerator & 0x00ff0000) >> 16);
			buf[1] = (byte) ((r.numerator & 0x0000ff00) >> 8);
			buf[0] = (byte) (r.numerator & 0x000000ff);
			buf[7] = (byte) ((r.denominator & 0xff000000) >> 24);
			buf[6] = (byte) ((r.denominator & 0x00ff0000) >> 16);
			buf[5] = (byte) ((r.denominator & 0x0000ff00) >> 8);
			buf[4] = (byte) (r.denominator & 0x000000ff);
		}
		fos.write(buf);
	}

	/**
	 * Return a list of extentions this class can write.
	 * 
	 * @return a list of extentions that are understood by this class.
	 */
	public String[] writeExt() {
		return suffix;
	}

	private byte[] getIFD(int type, int tag, int count, long val) {
		byte[] msg = new byte[12];

		// tag
		msg[1] = (byte) ((tag & 0xff00) >> 8);
		msg[0] = (byte) ((tag & 0x00ff));

		// type
		msg[3] = 0;
		msg[2] = (byte) (type & 0xff);

		// count
		msg[7] = (byte) ((count & 0xff000000) >> 24);
		msg[6] = (byte) ((count & 0x00ff0000) >> 16);
		msg[5] = (byte) ((count & 0x0000ff00) >> 8);
		msg[4] = (byte) (count & 0x000000ff);

		// offset or value
		if ((type == IFDEntry.TYPE_SHORT) && (count == 1)) {
			msg[9] = (byte) ((val & 0xff00) >> 8);
			msg[8] = (byte) ((val & 0x00ff));
		} else {
			msg[11] = (byte) ((val & 0xff000000) >> 24);
			msg[10] = (byte) ((val & 0x00ff0000) >> 16);
			msg[9] = (byte) ((val & 0x0000ff00) >> 8);
			msg[8] = (byte) ((val & 0x000000ff));
		}

		// write the tag
		return msg;
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
}
