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
package edu.illinois.ncsa.isda.im2learn.core.io.csv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;

import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageException;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.im2learn.core.datatype.SubArea;
import edu.illinois.ncsa.isda.im2learn.core.geo.GeoException;
import edu.illinois.ncsa.isda.im2learn.core.geo.projection.Projection;
import edu.illinois.ncsa.isda.im2learn.core.io.ImageLoader;
import edu.illinois.ncsa.isda.im2learn.core.io.ImageReader;
import edu.illinois.ncsa.isda.im2learn.core.io.ImageWriter;


// import org.apache.commons.logging.Log;
// import org.apache.commons.logging.LogFactory;

public class CSVLoader implements ImageWriter, ImageReader // (in the future).
{

	// private static Log logger = LogFactory.getLog( CSVLoader.class );

	final static String[]	suffix	= new String[] { "csv" };

	/**
	 * @param filename
	 *            of the file to be written.
	 * @return true if the file can be written by this class.
	 */
	public boolean canWrite(String filename) {
		String ext = ImageLoader.getExtention(filename);

		// treat the degenerate case first.
		if (ext == null) {
			return false;
		}

		// treat the non-degenerate case second.
		for (int i = 0; i < suffix.length; i++) {
			if (ext.toLowerCase().equals(suffix[i].toLowerCase())) {
				return true;
			}
		}

		// if here, did not match any extensions.
		return false;
	}

	/**
	 * Can't read any files.
	 * 
	 * @return false.
	 */
	public boolean canRead(String filename, byte[] array) {
		return false;
	}

	/**
	 * @return description of the reader (or writer).
	 */
	public String getDescription() {
		return "Comma Separated Values";
	}

	/**
	 * @return a list of extensions that are understood by this class.
	 */
	public String[] writeExt() {
		return suffix;
	}

	/**
	 * @return a list of extentions that are understood by this class.
	 */
	public String[] readExt() {
		return suffix;
	}

	/**
	 * Can't read any headers from CSV files.
	 * 
	 * @return null.
	 */
	public ImageObject readImageHeader(String filename) throws IOException, ImageException {
		File headerfile = new File(filename);
		if (!headerfile.exists()) {
			headerfile = null;
		}
		if (headerfile == null) {
			throw (new IOException("Could not find CSV file."));
		}

		// load the file
		BufferedReader r = new BufferedReader(new FileReader(headerfile));
		int numrows = 0;
		int numcols = 0;
		int numbands = 1;
		int type = ImageObject.TYPE_BYTE;
		String[] split = null;
		// load the file
		String line = r.readLine();
		String headerText = line;
		int countlines = 0;
		while (line != null) {
			countlines++;
			line = r.readLine();
		}
		r.close();

		numrows = countlines;

		headerText.trim();
		split = headerText.split(",]*");
		numcols = split.length;

		// sanity check
		if ((numrows <= 0) || (numcols <= 0)) {
			throw (new IOException("Dimensions of the CSV file are zero. numrows=" + numrows + ", numcols=" + numcols));
		}

		ImageObject imgobj = null;
		try {
			imgobj = ImageObject.createImage(numrows, numcols, numbands, type);
			imgobj.setProperty(ImageObject.COMMENT, headerText);
		} catch (Exception e) {
			System.err.println("ERROR: image object could not be created");
			return null;
		}
		// set all pixels to black
		for (int i = 0; i < imgobj.getSize(); i++) {
			imgobj.set(i, 0.0);
		}

		// test
		System.out.println("INFO: img dimensions" + imgobj.toString());

		return imgobj;

	}

	/**
	 * This method will return an image created from a csv file
	 * 
	 * The output image will have type INT if datumType = 1 otherwise type =
	 * DOUBLE The variable datumType is a hack to comply with the implementation
	 * of ImageReader datumType is used instead of sampling parameter
	 */
	public ImageObject readImage(String filename, SubArea subarea, int datumType) throws IOException, ImageException {
		if (filename == null) {
			throw (new IOException("filename cannot be null."));
		}
		System.out.println("Input CSV file name = " + filename);

		// ignore subarea !!!

		File headerfile = null;

		if (filename.toLowerCase().endsWith(".csv")) {
			headerfile = new File(filename);
			if (!headerfile.exists()) {
				headerfile = null;
			}
		}
		if (headerfile == null) {
			throw (new IOException("Could not find CSV file."));
		}

		ImageObject imgobj = readImageHeader(filename);
		if (imgobj == null) {
			return null;
		}
		ImageObject imgtemp = null;
		switch (datumType) {
		case 1:
			// load as int values of the CSV file
			imgtemp = loadCSVIntDataToImage(filename, imgobj.getNumRows(), imgobj.getNumCols());
			if (imgtemp == null) {
				throw (new IOException("Could not load data from CSV file."));
			}
			imgtemp.setProperty(ImageObject.COMMENT, imgobj.getProperty(ImageObject.COMMENT));
			imgobj = imgtemp;
			break;
		default:
			// load as a mask of the CSV file
			if (!loadCSVMaskToImage(filename, imgobj)) {
				throw (new IOException("Could not load data from CSV file."));
			}
			break;

		}

		imgobj.computeMinMax();
		return imgobj;
	}

	// //////////////////////////////
	// load mask of CSV entries into image
	// //////////////////////////////////
	private boolean loadCSVMaskToImage(String filename, ImageObject imgobj) throws IOException {

		// sanity check
		if ((imgobj == null) || (imgobj.getNumRows() <= 0) || (imgobj.getNumCols() <= 0) || (imgobj.getNumBands() <= 0)) {
			return false;
		}

		File headerfile = new File(filename);
		if (!headerfile.exists()) {
			headerfile = null;
		}
		if (headerfile == null) {
			throw (new IOException("Could not find CSV file."));
		}

		// load the file
		BufferedReader r = new BufferedReader(new FileReader(headerfile));
		int row = 0, col = 0, index, i, j;
		float temp;
		String[] split = null;
		// first line is assumed to be the heading
		int lineno = 1;
		String line = r.readLine();
		lineno++;
		line = r.readLine();

		String tempString = new String("");
		// test
		// while (line != null && count < 10) {
		while (line != null) {
			// read next lines
			line.trim();
			split = line.split(",]*");

			// split = line.split("[ ]*=[ ]*", 2);
			for (col = 0; col < split.length; col++) {
				System.out.println("split[" + col + "]=" + split[col]);
				if (split[col].trim().compareToIgnoreCase("") == 0) {
					imgobj.set(row, col, 0, 0);// value is missing
				} else {
					imgobj.set(row, col, 0, 255); // value is present
				}
			}
			row++;
			lineno++;
			line = r.readLine();

		}
		r.close();

		// test
		// System.out.print("TEST imgobj="+imgobj.toStringRow(0));
		return true;
	}

	// //////////////////////////////////
	// load int data into image
	// //////////////////////////////////////
	private ImageObject loadCSVIntDataToImage(String filename, int numrows, int numcols) throws IOException {

		// sanity check
		if ((numrows <= 0) || (numcols <= 0)) {
			throw (new IOException("numrows or numcols <=0 in loadCSVIntDataToImage."));
		}

		File headerfile = new File(filename);
		if (!headerfile.exists()) {
			headerfile = null;
		}
		if (headerfile == null) {
			throw (new IOException("Could not find CSV file."));
		}

		ImageObject imgobj = null;
		try {
			imgobj = ImageObject.createImage(numrows, numcols, 1, ImageObject.TYPE_INT);
		} catch (ImageException e) {
			throw (new IOException("Could not create ImageObject."));
		}
		int index = 0;
		for (index = 0; index < imgobj.getSize(); index++) {
			imgobj.set(index, 0);
		}

		// load the file
		BufferedReader r = new BufferedReader(new FileReader(headerfile));
		int row = 0, col = 0, i, j;
		int temp;
		String[] split = null;
		// first line is assumed to be the heading
		int lineno = 1;
		String line = r.readLine();
		lineno++;
		line = r.readLine();

		String tempString = new String("");
		// test
		// while (line != null && count < 10) {
		while (line != null) {
			// read next lines
			line.trim();
			split = line.split(",]*");

			// split = line.split("[ ]*=[ ]*", 2);
			for (col = 0; col < split.length; col++) {
				System.out.println("split[" + col + "]=" + split[col]);
				if (split[col].trim().compareToIgnoreCase("") == 0) {
					imgobj.set(row, col, 0, -1);// value is missing - set to
					// invalid
				} else {
					temp = Integer.parseInt(split[col]);
					imgobj.set(row, col, 0, temp); // value is present
				}
			}
			row++;
			lineno++;
			line = r.readLine();

		}
		r.close();

		imgobj.setInvalidData(-1.0);
		// test
		// System.out.print("TEST imgobj="+imgobj.toStringRow(0));
		return imgobj;
	}

	/**
	 * This function will write the raster to a CSV (Comma Separated Values)
	 * file.
	 * 
	 * Important note: will save only pixels with valid data in all bands.
	 * 
	 * @param filename
	 *            of the file to be written.
	 * @param imageobject
	 *            the image image to be written.
	 * @throws IOException
	 *             if the file could not be written.
	 */
	/*
	 * To generalize this method, make an interface that specifies a method like
	 * "rasterToX( col, row )". Classes conforming to that interface could be
	 * simple grid-like or complex projection-like.
	 */
	public void writeImage(String filename, ImageObject imageobject) throws IOException {
		// check whether we can use the projection.
		Projection proj = (Projection) imageobject.getProperty(ImageObject.GEOINFO);
		final boolean hasProjectionInfo = (proj != null);

		// figure out the number of labels, depending on the presence of the
		// grid.
		int numLabels = 2; // rows and cols
		if (hasProjectionInfo) {
			numLabels += 2; // projected coords
		}
		numLabels += imageobject.getNumBands(); // image data

		// the real labels given to the columns.
		String[] realLabels = new String[numLabels];
		int index = 0;

		// enter Row/Col labels.
		realLabels[index++] = "Row";
		realLabels[index++] = "Col";

		// enter grid labels, if any.
		if (hasProjectionInfo) {
			// enter generic projection labels (hopefully overwritten by the
			// real labels.
			realLabels[index++] = "Proj.x"; // TODO insert proj.getX()
			realLabels[index++] = "Proj.y"; // TODO insert proj.getY()
		}

		// enter the remaining labels.
		for (int i = index, k = 0; i < numLabels; i++, k++) {
			realLabels[i] = "band " + k;
		}

		// depending on whether we have the original labels, use them.
		String[] labels = (String[]) imageobject.getProperty(ImageObject.BAND_LABELS);
		for (int i = 2, k = 0; i < numLabels; i++, k++) // k is always i-2.
		{
			try {
				realLabels[i] = labels[k]; // check for the element being
				// there.
				labels[k].length(); // check for being non-null;
			} catch (Exception e) {
			}
		}

		// points to be used for mapping raster points to projected points.
		double[] rasterPoint = new double[2];
		double[] projPoint;

		// open the file into which the output will be written.
		FileOutputStream fileOut = new FileOutputStream(filename);
		OutputStreamWriter out = new OutputStreamWriter(fileOut);

		// output the header info.
		writeOut(out, realLabels);

		// an array that will hold the appropriate values (row, col, band
		// values).
		Object[] values = new Object[numLabels];

		// the type of the image, for potential optimization; use with "switch"
		// below.
		// final int imgType = imageobject.getType();

		// output pixels by going through tiles, rows, columns, and checking
		// pixels.
		for (SubArea area : imageobject.getTileBoxes()) {
			// check to see if the tile has useful data.
			if (!imageobject.hasValidData(area)) {
				continue;
			}

			// iterate on rows and cols in the area.
			for (int row = area.getRow(); row < area.getEndRow(); row++) {
				for (int col = area.getCol(); col < area.getEndCol(); col++) {
					// output the values only in the case there is valid data.

					// figure out whether all values are valid.
					if (!imageobject.allBandsValid(row, col)) {
						continue;
						// alternative: if( !someBandsValid( row, col ) )
					}

					index = 0;
					values[index++] = row; // fill in row.
					values[index++] = col; // fill in column.

					// fill in grid info, if available.
					if (hasProjectionInfo) {
						rasterPoint[0] = col;
						rasterPoint[1] = row;

						try {
							// TODO replace this transformation by a more
							// generic transformation.
							// that will use model projections and perhaps grids
							// to output data.
							projPoint = proj.rasterToEarth(rasterPoint);
						} catch (GeoException e) {
							projPoint = new double[] { imageobject.getInvalidData(), imageobject.getInvalidData() };
						}
						values[index++] = projPoint[0];
						values[index++] = projPoint[1];
					}

					// output the band entries.
					for (int band = 0; band < imageobject.getNumBands(); band++) {
						// the simple version which always gets a double.
						// for an alternative, use the switch below.
						values[index++] = imageobject.getDouble(row, col, band);

						/*
						 * // fill in the values in the output array regardless.
						 * switch( imgType ) // see imgType above. { case(
						 * ImageObject.TYPE_BYTE ): values[k] = getByte( row,
						 * col, band ); break; case TYPE_SHORT: case
						 * TYPE_USHORT: values[k] = getShort( row, col, band );
						 * break; case TYPE_INT: values[k] = getInt( row, col,
						 * band ); break; case TYPE_LONG: values[k] = getLong(
						 * row, col, band ); break; case TYPE_FLOAT: values[k] =
						 * getFloat( row, col, band ); break; case TYPE_DOUBLE:
						 * values[k] = getDouble( row, col, band ); break; }
						 */
					}

					// use the method below to write out the array.
					writeOut(out, values);
				}
			}
		}

		// flush out the buffer.
		out.flush();
	}

	/**
	 * Save the values as a row of the table (the last ends with a new line).
	 * 
	 * @param out
	 *            the file stream.
	 * @param values
	 *            to write out.
	 */
	public static void writeOut(OutputStreamWriter out, Object[] values) throws IOException {
		int length = values.length;
		for (int i = 0; i < length - 1; i++) {
			out.write(values[i] + ", ");
		}
		out.write(values[length - 1] + "\n");
	}

	/**
	 * Saving the raster into the file.
	 * 
	 * @param filename
	 *            of the file into which to save the table.
	 * @param imageobject
	 *            the raster to save into the file.
	 * @param labels
	 *            to give to the columns of the resulting table.
	 * @throws IOException
	 */
	public void writeImage(String filename, ImageObject imageobject, String[] labels) throws IOException {
		String[] oldLabels = (String[]) imageobject.getProperty(ImageObject.BAND_LABELS);
		imageobject.setProperty(ImageObject.BAND_LABELS, labels);
		writeImage(filename, imageobject);
		imageobject.setProperty(ImageObject.BAND_LABELS, oldLabels);
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
