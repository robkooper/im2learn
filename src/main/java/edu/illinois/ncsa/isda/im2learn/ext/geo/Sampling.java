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
package edu.illinois.ncsa.isda.im2learn.ext.geo;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageException;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.im2learn.core.geo.GeoException;
import edu.illinois.ncsa.isda.im2learn.core.geo.projection.Projection;

public class Sampling {
	/*
	 * The purpose of this class is to take in an image and perform sub-, up-
	 * and down-sampling After sampling, adjust the georeferencing information
	 * 
	 * @author Peter Bajcsy
	 * 
	 * @version 2.0
	 */

	// protected ImageObject _geoObj;
	private static Log	logger	= LogFactory.getLog(Sampling.class);

	// /////////////////////////////////////////////////////////////////////
	// this is a simple nearest neighbor sampling approach
	public static ImageObject SamplingNNbh(ImageObject geo, double scaleCol, double scaleRow) throws ImageException,
			GeoException {

		// /sanity checks
		if (geo == null) {
			logger.error("ERROR: Missing Input ImageObject");
			return null;
		}
		if (scaleCol <= 0 || scaleRow <= 0) {
			logger.error("ERROR: scaleCol or scaleRow are less than equal to zero");
			return null;
		}

		ImageObject newgeo = null;

		newgeo = geo.scale(scaleCol, scaleRow);

		if (newgeo != null && newgeo.getProperty(ImageObject.GEOINFO) != null) {
			Projection proj = (Projection) newgeo.getProperty(ImageObject.GEOINFO);
			proj.scale(scaleCol, scaleRow);

		}
		return newgeo;
	}

	// /////////////////////////////////////////////////////////////////////
	// this is a linear interpolation sampling approach
	public static ImageObject SamplingLinearInt(ImageObject geo, double scaleCol, double scaleRow)
			throws ImageException, GeoException {

		// /sanity checks
		if (geo == null) {
			logger.error("ERROR: Missing Input ImageObject");
			throw (new IllegalArgumentException("scale has to be larger than 0."));
			// return null;
		}
		if (scaleCol <= 0 || scaleRow <= 0) {
			logger.error("ERROR: scaleCol or scaleRow are less than equal to zero");
			throw (new IllegalArgumentException("scale has to be larger than 0."));
			// return null;
		}

		int w = (int) (geo.getNumCols() * scaleCol);
		int h = (int) (geo.getNumRows() * scaleRow);
		int idx1, idx2, r, c, band;
		double a, b, res, val; // multipliers and temp values
		int k, l;// floor row and col

		double myscaleCol = 1 / scaleCol;
		double myscaleRow = 1 / scaleRow;

		ImageObject geonew = null;
		try {
			geonew = ImageObject.createImage(h, w, geo.getNumBands(), geo.getType());
		} catch (ImageException e) {
			throw (new IllegalArgumentException("ERROR: could not create an image object."));
		}

		// bilinear interpolation
		// based on the formula presented at
		// http://ct.radiology.uiowa.edu/~jiangm/courses/dip/html/node67.html
		if (geonew != null) {
			double row, col;
			idx2 = 0;
			for (r = 0; r < h; r++) {
				// idx1 = (int) (r * myscaleRow) * geo.getNumCols() *
				// geo.getNumBands();
				row = (r * myscaleRow);// * geo.getNumCols() *
										// geo.getNumBands();
				k = (int) row;
				b = row - k;
				// idx1 = (int)row;

				for (c = 0; c < w; c++) {
					col = (c * myscaleCol);// * geo.getNumBands();
					l = (int) col;
					a = col - l;
					for (band = 0; band < geo.getNumBands() && k + 1 < geo.getNumRows() && l + 1 < geo.getNumCols(); band++) {
						val = geo.getDouble(k, l, band);
						res = val
								+ a
								* (geo.getDouble(k, l + 1, band) - val)
								+ b
								* (geo.getDouble(k + 1, l, band) - val)
								+ a
								* b
								* (val + geo.getDouble(k + 1, l + 1, band) - geo.getDouble(k + 1, l, band) - geo.getDouble(
										k, l + 1, band));
						geonew.set(idx2 + band, res);
					}

					// System.arraycopy(src, idx1 + offset, dst, idx2,
					// geo.getNumBands());
					idx2 += geo.getNumBands();
				}
			}
		}

		// copy properties
		geonew.setProperties(geo.cloneProperties());

		if (geonew != null && geonew.getProperty(ImageObject.GEOINFO) != null) {
			Projection proj = (Projection) geonew.getProperty(ImageObject.GEOINFO);
			proj.scale(scaleCol, scaleRow);

		}
		return geonew;
	}

	// /////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////
	// image downsampling by factor of sampRow and sampCol
	public static ImageObject ImDownSamp(ImageObject imObject, int sampRow, int sampCol) {
		// sanity check
		if (sampRow < 1 || sampCol < 1) {
			System.out.println("Error: sampRow and sampCol are not set ");
			return null;
		}
		if (sampRow == 1 && sampCol == 1) {
			// System.out.println("Warning: sampRow=1 and sampCol=1 ");
			try {
				ImageObject geonew = (ImageObject) imObject.clone();
				// copy properties
				geonew.setProperties(imObject.cloneProperties());
				return geonew;
			} catch (ImageException e1) {
				throw (new IllegalArgumentException("ERROR: could not create an image object." + e1));
			} catch (CloneNotSupportedException e2) {
				throw (new IllegalArgumentException("ERROR: could not clone an image object." + e2));
			}

		}

		// determine the new size
		int outcols, outrows;
		if ((imObject.getNumCols() % sampCol) != 0) {
			outcols = (int) (imObject.getNumCols() / (float) sampCol) + 1;
		} else {
			outcols = (int) (imObject.getNumCols() / (float) sampCol);
		}
		if ((imObject.getNumRows() % sampRow) != 0) {
			outrows = (int) (imObject.getNumRows() / (float) sampRow) + 1;
		} else {
			outrows = (int) (imObject.getNumRows() / (float) sampRow);
		}

		System.out.println("Test: New dim: rows=" + outrows + " cols=" + outcols);

		if (outcols == 0 || outrows == 0) {
			System.out.println("ERROR: zero new size Decrease DownSampling factors");
			return null;
		}

		// alocate memory for output files
		ImageObject subim = null;
		try {
			// create either FLOAT type or the same type as the input
			subim = ImageObject.createImage(outrows, outcols, imObject.getNumBands(), imObject.getType());
		} catch (ImageException e) {
			throw (new IllegalArgumentException("ERROR: could not create an image object."));
		}

		// Downsampling
		int row, col, i, j, k;
		int in_index, out_index, temp_index, count;
		int store_index, shift;
		double sumMag[];
		sumMag = new double[imObject.getNumBands()];
		double valDouble, val;

		int nextRow = (imObject.getNumCols() - sampCol) * imObject.getNumBands();
		int nextSubIm = sampCol * subim.getNumBands();
		out_index = in_index = 0;
		count = sampRow * sampCol;
		shift = sampRow * imObject.getNumCols() * imObject.getNumBands();

		// ///////////////////////////////////
		for (row = 0; row < imObject.getNumRows(); row += sampRow) {
			store_index = in_index;
			for (col = 0; col < imObject.getNumCols(); col += sampCol) {
				temp_index = in_index;
				for (k = 0; k < imObject.getNumBands(); k++) {
					sumMag[k] = 0.0;
				}

				for (i = row; i < row + sampRow && i < imObject.getNumRows(); i++) {
					for (j = col; j < col + sampCol && j < imObject.getNumCols(); j++) {
						for (k = 0; k < imObject.getNumBands(); k++) {
							// val = imObject.getDouble(temp_index);
							// if(val < 0 ){
							// sumMag[k] += val+ _lim.MAXPOS_BYTE;
							// }else{
							// sumMag[k] += val;
							// }
							// sumMag[k] += val;//imObject.image[temp_index];
							sumMag[k] += imObject.getDouble(temp_index);
							temp_index++;
						}
					}
					temp_index += nextRow;
				}
				for (k = 0; k < imObject.getNumBands(); k++) {
					valDouble = sumMag[k] / count;

					subim.set(out_index, valDouble);
					/*
					 * if(valDouble <= _lim.MAX_BYTE) subim.image[out_index] =
					 * (byte)(valDouble); else subim.image[out_index] =
					 * (byte)(valDouble - _lim.MAXPOS_BYTE);
					 */
					// subim.image[out_index]= (byte)(
					// (sumMag[k]/(double)count)+0.5);
					out_index++;
				}
				in_index += nextSubIm;
			}
			in_index = store_index + shift;
		}

		// copy properties
		try {
			subim.setProperties(imObject.cloneProperties());
		} catch (ImageException e2) {
			throw (new IllegalArgumentException("ERROR: could not clone properties of an image object." + e2));
		}

		if (subim != null && subim.getProperty(ImageObject.GEOINFO) != null) {
			Projection proj = (Projection) subim.getProperty(ImageObject.GEOINFO);
			proj.scale(sampCol, sampRow);

		}

		// System.out.println("Test: out_index=" + out_index + " newsize="+
		// subim.size );
		return (subim);
	}

}
