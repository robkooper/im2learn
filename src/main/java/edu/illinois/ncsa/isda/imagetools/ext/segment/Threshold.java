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
package edu.illinois.ncsa.isda.imagetools.ext.segment;

/*
 * Threshold.java
 *
 */

import java.io.*;
import java.lang.Math;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.imagetools.core.datatype.LimitValues;
import edu.illinois.ncsa.isda.imagetools.core.io.ImageLoader;


/**
 * <B>The class Threshold is a tool for two-class clustering problems using
 * Euclidean distance, a hypercube or a hyper plane for separating clusters in
 * high-dimensional space. </B> <BR>
 * <BR>
 * <B>Description: </B> The class operates in three distinct modes that are set
 * in the ThreshType choice list. In these three modes, points in
 * high-dimensional space are classified based on (1) their Euclidean distance
 * from the origin (a scalar value), (2) a hypercube (a vector) or (3) a hyper
 * plane (two vectors). <BR>
 *
 * <img src="../../../../../../images/thresholdDialog.jpg" width="346"
 * height="415">
 *
 * <BR>
 * <B>Setup: </B> In the "Distance (scalar)" mode, the scalar value is set
 * either in the edit box or by moving the slider below the edit box denoted as
 * Value(Point). The value is also shown in the text area together with the
 * outcome of the thresholding operation. The outcome of the thresholding
 * operation is the number of points below the threshold denoted as "BlackCount"
 * and above the threshold denoted as "WhiteCount". <BR>
 * In the "Box (vector)" mode, it is possible to set the upper corner of a
 * hypercube by selecting one of the point dimensions in the choice list labeled
 * as "ThreshPoint". The coordinate can then be modified either in the edit box
 * or by moving the slider. The upper corner of a box (hypercube) is defined as
 * the point that encloses the maxima of all points denoted as "Black". The
 * lower corner is always set to the minimum coordinate in each dimension. <BR>
 * The last option "Plane (2 vectors)" of the "ThreshType" choice list defines a
 * hyper plane by setting one point in the plane (ValuePoint) and one point as
 * the tip of the normal to the plane (ThreshVecNormal). The settings can be
 * modified either in the edit boxes or by moving the sliders. <BR>
 * <BR>
 * <B>Run: </B> The text area reports input parameters (a scalar, one vector or
 * two vectors) and output parameters (BlackCount and WhiteCount). It is
 * possible to view the thresholded images by clicking "Show" and save them by
 * clicking "Save". Any changes in the input parameters will be automatically
 * updated in the frame showing the thresholded images. Although the default
 * values for each mode correspond to statistical thresholds (e.g., the sample
 * mean of the image), further input parameter tuning is facilitated by on-line
 * visualization of the thresholded image with the "Show" button. <BR>
 *
 * <img src="../../../../../../images/sampHyperTarp.jpg" width="415"
 * height="267">
 *
 * <img src="../../../../../../images/sampHyperTarp_thresh.jpg" width="425"
 * height="272">
 *
 * <BR>
 * <B>Release notes: </B> <BR>
 * Java sliders support only integer data type therefore any double precision
 * thresholds must be entered via edit boxes. The value that is shown in the
 * edit boxes will be the actual value used for thresholding.
 *
 *
 * @author Peter Bajcsy & Young-Jin Lee
 * @version 1.0
 *
 */

public class Threshold implements Serializable {
	protected boolean _debugThreshold;

	protected double _meanDistance, _minDistance, _maxDistance;

	protected int _numBands = 0;

	protected double[] _meanBand;

	protected double[] _minBand;

	protected double[] _maxBand;

	protected double[] _halfVolBand;

	protected double[] _hyperPlane;

	protected int _countB, _countW; // number of black and white pixels in the
									// final result

	protected ImageObject _thrImage;// = null;//new ImageObject();

	protected ImageObject _distanceImage;// = null;//new ImageObject();

	public Threshold() {
		_debugThreshold = true;
		resetMemory();
	}

	public void setImageObject(ImageObject thresholdImage) {
		_thrImage = thresholdImage;
	}

	/**
	 *
	 * @param thresholdImage
	 * @deprecated Use setImageObject()
	 */
	public void SetImageObject(ImageObject thresholdImage) {
		_thrImage = thresholdImage;
	}

	public ImageObject getImageObject() {
		return _thrImage;
	}

	/**
	 *
	 * @return
	 * @deprecated Use getImageObject()
	 */
	public ImageObject GetImageObject() {
		return _thrImage;
	}

	public ImageObject getDistanceImageObject() {
		return _distanceImage;
	}

	/**
	 *
	 * @return
	 * @deprecated Use getDistanceImageObject()
	 */
	public ImageObject GetDistanceImageObject() {
		return _distanceImage;
	}

	public double getMeanDistance() {
		return _meanDistance;
	}

	/**
	 *
	 * @return
	 * @deprecated Use getMeanDistance()
	 */
	public double GetMeanDistance() {
		return _meanDistance;
	}

	public double getMinDistance() {
		return _minDistance;
	}

	/**
	 *
	 * @return
	 * @deprecated Use getMinDistance()
	 */
	public double GetMinDistance() {
		return _minDistance;
	}

	public double getMaxDistance() {
		return _maxDistance;
	}

	/**
	 *
	 * @return
	 * @deprecated Use getMaxDistance()
	 */
	public double GetMaxDistance() {
		return _maxDistance;
	}

	public double[] getMeanBand() {
		return _meanBand;
	}

	/**
	 *
	 * @return
	 * @deprecated Use getMeanBand()
	 */
	public double[] GetMeanBand() {
		return _meanBand;
	}

	public double[] getMinBand() {
		return _minBand;
	}

	/**
	 *
	 * @return
	 * @deprecated Use getMinBand()
	 */
	public double[] GetMinBand() {
		return _minBand;
	}

	public double[] getMaxBand() {
		return _maxBand;
	}

	/**
	 *
	 * @return
	 * @deprecated Use getMaxBand()
	 */
	public double[] GetMaxBand() {
		return _maxBand;
	}

	public double getMeanBand(int index) {
		if (index >= 0 && index < _numBands)
			return _meanBand[index];
		else
			return ((double) -1.0);
	}

	/**
	 *
	 * @param index
	 * @return
	 * @deprecated Use getMeanBand()
	 */
	public double GetMeanBand(int index) {
		return getMeanBand(index);
	}

	public double getMinBand(int index) {
		if (index >= 0 && index < _numBands)
			return _minBand[index];
		else
			return -1;
	}

	/**
	 *
	 * @param index
	 * @return
	 * @deprecated Use getMinBand()
	 */
	public double GetMinBand(int index) {
		return getMinBand(index);
	}

	public double getMaxBand(int index) {
		if (index >= 0 && index < _numBands)
			return _maxBand[index];
		else
			return -1;
	}

	/**
	 *
	 * @param index
	 * @return
	 * @deprecated Use getMaxBand()
	 */
	public double GetMaxBand(int index) {
		return getMaxBand(index);
	}

	public double getHalfVolBand(int index) {
		if (index >= 0 && index < _numBands)
			return _halfVolBand[index];
		else
			return ((double) -1.0);
	}

	/**
	 *
	 * @param index
	 * @return
	 * @deprecated Use getHalfVolBand();
	 */
	public double GetHalfVolBand(int index) {
		return getHalfVolBand(index);
	}

	public int getBlackCount() {
		return _countB;
	}

	/**
	 *
	 * @return
	 * @deprecated Use getBlackCount()
	 */
	public int GetBlackCount() {
		return _countB;
	}

	public int getWhiteCount() {
		return _countW;
	}

	/**
	 *
	 * @return
	 * @deprecated Use getWhiteCount()
	 */
	public int GetWhiteCount() {
		return _countW;
	}

	//Setters
	public void setDebugFlag(boolean flag) {
		_debugThreshold = flag;
	}

	/**
	 *
	 * @param flag
	 * @deprecated Use setDebugFlag()
	 */
	public void SetDebugFlag(boolean flag) {
		_debugThreshold = flag;
	}

	/**
	 * initialize all arrays
	 */
	public void initMemory(ImageObject im) {
		_thrImage = null;

		try {
			_thrImage = ImageObject.createImage(im.getNumRows(),
					im.getNumCols(), 1, ImageObject.TYPE_BYTE);
		} catch(Exception e) {
			e.printStackTrace();
		}

		_hyperPlane = new double[im.getNumBands() + 1];
	}

	/**
	 *
	 * @param im
	 * @deprecated Use initMemory()
	 */
	public void InitMemory(ImageObject im) {
		initMemory(im);
	}

	// delete memory
	private void resetMemory() {
		_minDistance = Double.MAX_VALUE;
		_maxDistance = -Double.MAX_VALUE;
		_meanDistance = Double.MAX_VALUE;
		_meanBand = null;
		_minBand = _maxBand = null;
		_distanceImage = null;
		_hyperPlane = null;
		_halfVolBand = null;
		_thrImage = null;
	}

	/**
	 * @deprecated Use resetMemory()
	 *
	 */
	private void ResetMemory() {
		resetMemory();
	}

	/**
	 * threshold all bands by a hyperplane defined with a point in the plane
	 * and the normal vector
	 */
	public boolean binarizeImage(ImageObject im, ImageObject vectPlane,
			ImageObject vectNormal, boolean isMeanReady) {
		//sanity check
		if (im == null || vectPlane == null || vectNormal == null) {
			System.out.println("Error: no input data");

			return false;
		}

		if (vectPlane.getType() != ImageObject.TYPE_DOUBLE
				|| vectPlane.getData() == null) {
			System.out.println("Error: vectPlane must be double");

			return false;
		}

		if (vectNormal.getType() != ImageObject.TYPE_DOUBLE
				|| vectNormal.getData() == null) {
			System.out.println("Error: vectNormal must be double");

			return false;
		}

		if (im.getNumBands() != vectPlane.getNumBands()
				|| im.getNumBands() != vectNormal.getNumBands()) {
			System.out.println("Error: mismatch of image and vector size");

			return false;
		}

		//TODO
//		if (im.sampType.equalsIgnoreCase("BYTE") == false
//				&& im.sampType.equalsIgnoreCase("SHORT") == false
//				&& im.sampType.equalsIgnoreCase("INT") == false
//				&& im.sampType.equalsIgnoreCase("FLOAT") == false
//				&& im.sampType.equalsIgnoreCase("DOUBLE") == false) {
//			System.out
//					.println("Error: other than BYTE or SHORT or INT or FLOAT or DOUBLE image is not supported");
//			return false;
//		}

		if (isMeanReady == false || _hyperPlane == null) {
			_thrImage = null;

			try {
				_thrImage = ImageObject.createImage(im.getNumRows(),
						im.getNumCols(), 1, ImageObject.TYPE_BYTE);
			} catch(Exception e) {
				e.printStackTrace();
			}

			_hyperPlane = new double[im.getNumBands() + 1];
		}

		int i, j, idx;
		double val, sum;

		for (i = 0; i < vectNormal.getNumBands(); i++) {
			_hyperPlane[i] = vectNormal.getDouble(i)
					- vectPlane.getDouble(i);
		}

		_hyperPlane[im.getNumBands()] = 0.0;

		for (i = 0; i < vectNormal.getNumBands(); i++) {
			_hyperPlane[im.getNumBands()] -= _hyperPlane[i]
					* vectPlane.getDouble(i);
		}

		// threshold based on the hyperplane
		_countB = _countW = 0;

		for (i = 0, idx = 0; i < im.getSize(); i += im.getNumBands(), idx++) {
			sum = 0.0;

			for (j = 0; j < im.getNumBands(); j++) {
                            val = im.getDouble(i+j);
                            sum += _hyperPlane[j] * val;
			}

			sum += _hyperPlane[im.getNumBands()];

			if (sum > 0.0) {
				_thrImage.setByte(idx, LimitValues.BIN_ONE);
				_countW++;
			} else {
				_thrImage.setByte(idx, LimitValues.BIN_ZERO);
				_countB++;
			}
		}


		return true;
	}

	/**
	 *
	 * @param im
	 * @param vectPlane
	 * @param vectNormal
	 * @param isMeanReady
	 * @return
	 * @deprecated Use binarizeImage()
	 */
	public boolean BinarizeImage(ImageObject im, ImageObject vectPlane,
			ImageObject vectNormal, boolean isMeanReady) {
		return binarizeImage(im, vectPlane, vectNormal, isMeanReady);
	}

	/**
	 * threshold all bands by a hyperplane
	 */
	public boolean binarizeImage(ImageObject im, ImageObject vectPlane,
			ImageObject vectNormal, String outFileName) throws Exception {

		//ImageObject temp;
		boolean ret;
		ret = binarizeImage(im, vectPlane, vectNormal, false);


		try {
			ImageLoader.writeImage(outFileName, _thrImage);
		} catch(IOException e) {
			System.out.println("Error: error while saving a file");

			return false;
		}

		return true;
	}

	/**
	 *
	 * @param im
	 * @param vectPlane
	 * @param vectNormal
	 * @param OutFileName
	 * @return
	 * @throws Exception
	 * @deprecated Use binarizeImage()
	 */
	public boolean BinarizeImage(ImageObject im, ImageObject vectPlane,
			ImageObject vectNormal, String OutFileName) throws Exception {
		return binarizeImage(im, vectPlane, vectNormal, OutFileName);
	}

	/**
	 * threshold all bands by a box
	 */
	public boolean binarizeImage(ImageObject im, ImageObject vectThresh, boolean isLowerThresh,
			boolean isMeanReady) {
		//sanity check
		if (im == null || vectThresh == null) {
			System.out.println("Error: no input data");

			return false;
		}

		if (vectThresh.getType() != ImageObject.TYPE_DOUBLE
				|| vectThresh.getData() == null) {
			System.out.println("Error: vectThresh must be double");

			return false;
		}

		if (im.getNumBands() != vectThresh.getNumBands()) {
			System.out.println("Error: mismatch of image and threshold size");

			return false;
		}

		//TODO
//		if (im.sampType.equalsIgnoreCase("BYTE") == false
//				&& im.sampType.equalsIgnoreCase("SHORT") == false
//				&& im.sampType.equalsIgnoreCase("INT") == false
//				&& im.sampType.equalsIgnoreCase("FLOAT") == false
//				&& im.sampType.equalsIgnoreCase("DOUBLE") == false) {
//			System.out
//					.println("Error: other than BYTE or SHORT or INT or FLOAT or DOUBLE image is not supported");
//			return false;
//		}

		if (isMeanReady == false || _thrImage == null) {
			_thrImage = null;

			try {
				_thrImage = ImageObject.createImage(im.getNumRows(),
						im.getNumCols(), 1, ImageObject.TYPE_BYTE);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}

		int i, j, idx;
		boolean signal;
		double val;
		_countB = _countW = 0;

		for (i = 0, idx = 0; i < im.getSize(); i += im.getNumBands(), idx++) {
			signal = true;

			for (j = 0; signal && j < im.getNumBands(); j++) {
				if (im.getByte(i + j) < 0)
					val = im.getByte(i + j) + LimitValues.MAXPOS_BYTE;//256;
				else
					val = im.getByte(i + j);

				if (val < vectThresh.getDouble(j)) {
					signal = false;
				}
			}

			if (signal) {
				if(isLowerThresh){
					_thrImage.setByte(idx, LimitValues.BIN_ONE);
					_countW++;
				}else{
					_thrImage.setByte(idx, LimitValues.BIN_ZERO);
					_countB++;
				}
			} else {
				if(isLowerThresh){
					_thrImage.setByte(idx, LimitValues.BIN_ZERO);
					_countB++;
				}else{
					_thrImage.setByte(idx, LimitValues.BIN_ONE);
					_countW++;
				}
			}
		}
		return true;
	}
		
		public boolean binarizeImage(ImageObject im, ImageObject vectThreshLower, ImageObject vectThreshUpper,
				boolean isMeanReady, boolean isBetweenLowerAndUpper) {
			//sanity check
			if (im == null || vectThreshLower == null || vectThreshUpper == null) {
				System.out.println("Error: no input data");

				return false;
			}

			if (vectThreshLower.getType() != ImageObject.TYPE_DOUBLE || vectThreshUpper.getType() != ImageObject.TYPE_DOUBLE
					|| vectThreshLower.getData() == null || vectThreshUpper.getData() == null) {
				System.out.println("Error: vectThresh must be double");

				return false;
			}

			if (im.getNumBands() != vectThreshLower.getNumBands() || im.getNumBands() != vectThreshUpper.getNumBands()) {
				System.out.println("Error: mismatch of image and threshold size");

				return false;
			}

			//TODO
//			if (im.sampType.equalsIgnoreCase("BYTE") == false
//					&& im.sampType.equalsIgnoreCase("SHORT") == false
//					&& im.sampType.equalsIgnoreCase("INT") == false
//					&& im.sampType.equalsIgnoreCase("FLOAT") == false
//					&& im.sampType.equalsIgnoreCase("DOUBLE") == false) {
//				System.out
//						.println("Error: other than BYTE or SHORT or INT or FLOAT or DOUBLE image is not supported");
//				return false;
//			}

			if (isMeanReady == false || _thrImage == null) {
				_thrImage = null;

				try {
					_thrImage = ImageObject.createImage(im.getNumRows(),
							im.getNumCols(), 1, ImageObject.TYPE_BYTE);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}

			int i, j, idx;
			boolean signal;
			double val;
			_countB = _countW = 0;

			for (i = 0, idx = 0; i < im.getSize(); i += im.getNumBands(), idx++) {
				signal = true;

				for (j = 0; signal && j < im.getNumBands(); j++) {
					if (im.getByte(i + j) < 0)
						val = im.getByte(i + j) + LimitValues.MAXPOS_BYTE;//256;
					else
						val = im.getByte(i + j);

					if (val >= vectThreshLower.getDouble(j) && val <= vectThreshUpper.getDouble(j) ) {
						signal = false;
					}
				}

				if (signal) {
					if(isBetweenLowerAndUpper){
						_thrImage.setByte(idx, LimitValues.BIN_ZERO);
						_countB++;
					}else{
						_thrImage.setByte(idx, LimitValues.BIN_ONE);
						_countW++;
					}
				} else {
					if(isBetweenLowerAndUpper){
						_thrImage.setByte(idx, LimitValues.BIN_ONE);
						_countW++;
					}else{
						_thrImage.setByte(idx, LimitValues.BIN_ZERO);
						_countB++;
					}
				}
			}
			return true;
		}

		//TODO
//		if (im.sampType.equalsIgnoreCase("BYTE")) {
//			for (i = 0, idx = 0; i < im.size; i += im.sampPerPixel, idx++) {
//				signal = true;
//				for (j = 0; signal && j < im.sampPerPixel; j++) {
//					if (im.image[i + j] < 0)
//						val = im.image[i + j] + _lim.MAXPOS_BYTE;//256;
//					else
//						val = im.image[i + j];
//
//					if (val < vectThresh.imageDouble[j]) {
//						signal = false;
//					}
//				}
//				if (signal) {
//					_thrImage.image[idx] = _lim.BIN_ONE;
//					_countW++;
//				} else {
//					_thrImage.image[idx] = _lim.BIN_ZERO;
//					_countB++;
//				}
//			}
//		}
//
//		if (im.sampType.equalsIgnoreCase("SHORT")) {
//			for (i = 0, idx = 0; i < im.size; i += im.sampPerPixel, idx++) {
//				signal = true;
//				for (j = 0; signal && j < im.sampPerPixel; j++) {
//					//val = im.imageShort[i+j];
//
//					if (im.imageShort[i + j] < 0)
//						val = im.imageShort[i + j] + _lim.MAXPOS_SHORT;//65536;
//					else
//						val = im.imageShort[i + j];
//
//					if (val < vectThresh.imageDouble[j]) {
//						signal = false;
//						//test
//						// System.out.println("Info: i=" +i + " j=" + j+ "val="
//						// +val + " thr="+ vectThresh.imageDouble[j] );
//					}
//				}
//				if (signal) {
//					_thrImage.image[idx] = _lim.BIN_ONE;
//					_countW++;
//				} else {
//					_thrImage.image[idx] = _lim.BIN_ZERO;
//					_countB++;
//				}
//			}
//		}
//
//		if (im.sampType.equalsIgnoreCase("INT")) {
//			for (i = 0, idx = 0; i < im.size; i += im.sampPerPixel, idx++) {
//				signal = true;
//				for (j = 0; signal && j < im.sampPerPixel; j++) {
//					val = im.imageInt[i + j];
//					/*
//					 * if(im.imageInt[i+j] < 0) val = im.imageInt[i+j] +
//					 * _lim.MAXPOS_INT; else val = im.imageInt[i+j];
//					 */
//					if (val < vectThresh.imageDouble[j]) {
//						signal = false;
//					}
//				}
//				if (signal) {
//					_thrImage.image[idx] = _lim.BIN_ONE;
//					_countW++;
//				} else {
//					_thrImage.image[idx] = _lim.BIN_ZERO;
//					_countB++;
//				}
//			}
//		}
//		if (im.sampType.equalsIgnoreCase("FLOAT")) {
//			for (i = 0, idx = 0; i < im.size; i += im.sampPerPixel, idx++) {
//				signal = true;
//				for (j = 0; signal && j < im.sampPerPixel; j++) {
//					val = im.imageFloat[i + j];
//					/*
//					 * if(im.imageFloat[i+j] < 0) val = im.imageFloat[i+j] +
//					 * _lim.MAXPOS_FLOAT; else val = im.imageFloat[i+j];
//					 */
//					if (val < vectThresh.imageDouble[j]) {
//						signal = false;
//					}
//				}
//				if (signal) {
//					_thrImage.image[idx] = _lim.BIN_ONE;
//					_countW++;
//				} else {
//					_thrImage.image[idx] = _lim.BIN_ZERO;
//					_countB++;
//				}
//			}
//		}
//		if (im.sampType.equalsIgnoreCase("DOUBLE")) {
//			for (i = 0, idx = 0; i < im.size; i += im.sampPerPixel, idx++) {
//				signal = true;
//				for (j = 0; signal && j < im.sampPerPixel; j++) {
//					val = im.imageDouble[i + j];
//					if (val < vectThresh.imageDouble[j]) {
//						signal = false;
//					}
//				}
//				if (signal) {
//					_thrImage.image[idx] = _lim.BIN_ONE;
//					_countW++;
//				} else {
//					_thrImage.image[idx] = _lim.BIN_ZERO;
//					_countB++;
//				}
//			}
//		}



	/**
	 *
	 * @param im
	 * @param vectThresh
	 * @param isMeanReady
	 * @return
	 * @deprecated Use binarizeImage()
	 */
	/*
	public boolean BinarizeImage(ImageObject im, ImageObject vectThresh,
			boolean isMeanReady) {
		return binarizeImage(im, vectThresh, isMeanReady);
	}*/

	/**
	 * threshold all bands by a box defined by lowerThresh and upperThresh
	 * inside of the box is labeled as black (with the threshold values
	 * included)!!
	 */
	public boolean binarizeImageLowerUpper(ImageObject im,
			ImageObject lowerVectThresh, ImageObject upperVectThresh) {
		//sanity check
		if (im == null || lowerVectThresh == null || upperVectThresh == null) {
			System.out.println("Error: no input data");

			return false;
		}

		if (lowerVectThresh.getType() != ImageObject.TYPE_DOUBLE
				|| lowerVectThresh.getData() == null) {
			System.out.println("Error: lowerVectThresh must be double");

			return false;
		}

		if (upperVectThresh.getType() != ImageObject.TYPE_DOUBLE
				|| upperVectThresh.getData() == null) {
			System.out.println("Error: upperVectThresh must be double");

			return false;
		}

		if (im.getNumBands() != lowerVectThresh.getNumBands()
				|| im.getNumBands() != upperVectThresh.getNumBands()) {
			System.out.println("Error: mismatch of image and threshold vector size");

			return false;
		}

		//TODO
//		if (im.sampType.equalsIgnoreCase("BYTE") == false
//				&& im.sampType.equalsIgnoreCase("SHORT") == false
//				&& im.sampType.equalsIgnoreCase("INT") == false
//				&& im.sampType.equalsIgnoreCase("FLOAT") == false
//				&& im.sampType.equalsIgnoreCase("DOUBLE") == false) {
//			System.out
//					.println("Error: other than BYTE or SHORT or INT or FLOAT or DOUBLE image is not supported");
//			return false;
//		}

		if (_thrImage == null || _thrImage.getNumRows() != im.getNumRows()
				|| _thrImage.getNumCols() != im.getNumCols()) {
			_thrImage = null;

			try {
				_thrImage = ImageObject.createImage(im.getNumRows(),
						im.getNumCols(), 1, ImageObject.TYPE_BYTE);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}

		int i, j, idx;
		boolean signal;
		double val;
		_countB = _countW = 0;

		for (i = 0, idx = 0; i < im.getSize(); i += im.getNumBands(), idx++) {
			signal = true;

			for (j = 0; signal && j < im.getNumBands(); j++) {
				val = im.getByte(i + j) & 0xff;

				if (val < lowerVectThresh.getDouble(j)
						|| val > upperVectThresh.getDouble(j)) {
					signal = false;
				}
			}

			if (signal) {
				_thrImage.setByte(idx, LimitValues.BIN_ZERO);
				_countB++;
			} else {
				_thrImage.setByte(idx, LimitValues.BIN_ONE);
				_countW++;
			}
		}

		//TODO
//		if (im.sampType.equalsIgnoreCase("BYTE")) {
//			for (i = 0, idx = 0; i < im.size; i += im.sampPerPixel, idx++) {
//				signal = true;
//				for (j = 0; signal && j < im.sampPerPixel; j++) {
//					val = im.image[i + j] & 0xff;
//
//					if (val < lowerVectThresh.imageDouble[j]
//							|| val > upperVectThresh.imageDouble[j]) {
//						signal = false;
//					}
//				}
//				if (signal) {
//					_thrImage.image[idx] = _lim.BIN_ZERO;
//					_countB++;
//				} else {
//					_thrImage.image[idx] = _lim.BIN_ONE;
//					_countW++;
//				}
//			}
//		}
//
//		if (im.sampType.equalsIgnoreCase("SHORT")) {
//			for (i = 0, idx = 0; i < im.size; i += im.sampPerPixel, idx++) {
//				signal = true;
//				for (j = 0; signal && j < im.sampPerPixel; j++) {
//					val = im.imageShort[i + j] & 0xffff;
//
//					if (val < lowerVectThresh.imageDouble[j]
//							|| val > upperVectThresh.imageDouble[j]) {
//						signal = false;
//					}
//				}
//				if (signal) {
//					_thrImage.image[idx] = _lim.BIN_ZERO;
//					_countB++;
//				} else {
//					_thrImage.image[idx] = _lim.BIN_ONE;
//					_countW++;
//				}
//			}
//		}
//
//		if (im.sampType.equalsIgnoreCase("INT")) {
//			for (i = 0, idx = 0; i < im.size; i += im.sampPerPixel, idx++) {
//				signal = true;
//				for (j = 0; signal && j < im.sampPerPixel; j++) {
//					val = im.imageInt[i + j];
//					if (val < lowerVectThresh.imageDouble[j]
//							|| val > upperVectThresh.imageDouble[j]) {
//						signal = false;
//					}
//				}
//				if (signal) {
//					_thrImage.image[idx] = _lim.BIN_ZERO;
//					_countB++;
//				} else {
//					_thrImage.image[idx] = _lim.BIN_ONE;
//					_countW++;
//				}
//			}
//		}
//		if (im.sampType.equalsIgnoreCase("FLOAT")) {
//			for (i = 0, idx = 0; i < im.size; i += im.sampPerPixel, idx++) {
//				signal = true;
//				for (j = 0; signal && j < im.sampPerPixel; j++) {
//					val = im.imageFloat[i + j];
//					if (val < lowerVectThresh.imageDouble[j]
//							|| val > upperVectThresh.imageDouble[j]) {
//						signal = false;
//					}
//				}
//				if (signal) {
//					_thrImage.image[idx] = _lim.BIN_ZERO;
//					_countB++;
//				} else {
//					_thrImage.image[idx] = _lim.BIN_ONE;
//					_countW++;
//				}
//			}
//		}
//		if (im.sampType.equalsIgnoreCase("DOUBLE")) {
//			for (i = 0, idx = 0; i < im.size; i += im.sampPerPixel, idx++) {
//				signal = true;
//				for (j = 0; signal && j < im.sampPerPixel; j++) {
//					val = im.imageDouble[i + j];
//					if (val < lowerVectThresh.imageDouble[j]
//							|| val > upperVectThresh.imageDouble[j]) {
//						signal = false;
//					}
//				}
//				if (signal) {
//					_thrImage.image[idx] = _lim.BIN_ZERO;
//					_countB++;
//				} else {
//					_thrImage.image[idx] = _lim.BIN_ONE;
//					_countW++;
//				}
//			}
//		}

		return true;
	}

	/**
	 * Binarize by a multidimensional box
	 * @param im
	 * @param box box[bands] = {min, max}
	 * @return
	 */
	public boolean binarizeImage(ImageObject im, double[][] box) {
		//sanity check
		if (im == null || box == null) {
			System.out.println("Error: no input data");

			return false;
		}

		if (im.getNumBands() != box.length) {
			System.out.println("Error: mismatch of image and threshold vector size");

			return false;
		}

		//TODO
//		if (im.sampType.equalsIgnoreCase("BYTE") == false
//				&& im.sampType.equalsIgnoreCase("SHORT") == false
//				&& im.sampType.equalsIgnoreCase("INT") == false
//				&& im.sampType.equalsIgnoreCase("FLOAT") == false
//				&& im.sampType.equalsIgnoreCase("DOUBLE") == false) {
//			System.out
//					.println("Error: other than BYTE or SHORT or INT or FLOAT or DOUBLE image is not supported");
//			return false;
//		}

		if (_thrImage == null || _thrImage.getNumRows() != im.getNumRows()
				|| _thrImage.getNumCols() != im.getNumCols()) {
			_thrImage = null;

			try {
				_thrImage = ImageObject.createImage(im.getNumRows(),
						im.getNumCols(), 1, ImageObject.TYPE_BYTE);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}

		int i, j, idx;
		boolean signal;
		double val;
		_countB = _countW = 0;

		for (i = 0, idx = 0; i < im.getSize(); i += im.getNumBands(), idx++) {
			signal = true;

			for (j = 0; signal && j < im.getNumBands(); j++) {
				val = im.getDouble(i + j);

				if (val < box[j][0] || val > box[j][1]) {
					signal = false;
				}
			}

			if (signal) {
				_thrImage.setByte(idx, LimitValues.BIN_ZERO);
				_countB++;
			}
			else {
				_thrImage.setByte(idx, LimitValues.BIN_ONE);
				_countW++;
			}
		}

//		if (im.sampType.equalsIgnoreCase("DOUBLE")) {
//			for (i = 0, idx = 0; i < im.size; i += im.sampPerPixel, idx++) {
//				signal = true;
//				for (j = 0; signal && j < im.sampPerPixel; j++) {
//					val = im.imageDouble[i + j];
//					if (val < lowerVectThresh.imageDouble[j]
//							|| val > upperVectThresh.imageDouble[j]) {
//						signal = false;
//					}
//				}
//				if (signal) {
//					_thrImage.image[idx] = _lim.BIN_ZERO;
//					_countB++;
//				} else {
//					_thrImage.image[idx] = _lim.BIN_ONE;
//					_countW++;
//				}
//			}
//		}

		return true;
	}
	
	/**
	 *
	 * @param im
	 * @param lowerVectThresh
	 * @param upperVectThresh
	 * @return
	 * @deprecated Use binarizeImageLowerUpper()
	 */
	public boolean BinarizeImageLowerUpper(ImageObject im,
			ImageObject lowerVectThresh, ImageObject upperVectThresh) {
		return binarizeImageLowerUpper(im, lowerVectThresh, upperVectThresh);
	}

	////////////////////////////////////////////////////////////////
	// threshold all bands by a box defined with one point
	public boolean binarizeImage(ImageObject im, ImageObject vectThresh,
			String outFileName, boolean isLowerThresh) throws Exception {

		//ImageObject temp;
		boolean ret;
		ret = binarizeImage(im, vectThresh, isLowerThresh, false);

		try {
			ImageLoader.writeImage(outFileName, _thrImage);
		} catch(IOException e) {
			System.out.println("Error: error while saving a file");

			return false;
		}

		return true;
	}

	/**
	 *
	 * @param im
	 * @param vectThresh
	 * @param outFileName
	 * @return
	 * @throws Exception
	 * @deprecated Use binarizeImage()
	 */
	/*
	public boolean BinarizeImage(ImageObject im, ImageObject vectThresh,
			String outFileName) throws Exception {
		return binarizeImage(im, vectThresh, outFileName);
	}*/

	/**
	 * threshold all bands by two scalars in the distance space
	 * the white color is assigned to all pixels that are below ThreshLower and
	 * above ThreshUpper !!
	 * inside of the Euclidean distance interval is labeled as black (with the
	 * threshold values included)!!
	 */
	public boolean binarizeImage(ImageObject im, double threshLower,
			double threshUpper, boolean isMeanDist, boolean isBetweenLowerAndUpper) {
		//sanity check
		if (threshLower < 0 || threshLower > threshUpper) {
			System.out.println("Error: ThreshLower and ThreshUpper " +
					"cannot be smaller than 0 or reversed");

			return false;//null;
		}

		double meanThresh;

/*		if (im.getType() == ImageObject.TYPE_BYTE
				|| im.getType() == ImageObject.TYPE_SHORT
				|| im.getType() == ImageObject.TYPE_INT
				|| im.getType() == ImageObject.TYPE_FLOAT
				|| im.getType() == ImageObject.TYPE_DOUBLE) {*/
			if (_thrImage == null || _thrImage.getNumRows() != im.getNumRows()
					|| _thrImage.getNumCols() != im.getNumCols()) {
				_thrImage = null;

				try {
					_thrImage = ImageObject.createImage(im.getNumRows(),
							im.getNumCols(), 1, ImageObject.TYPE_BYTE);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}

			if (isMeanDist == false || _distanceImage == null
					|| _distanceImage.getData() == null
					|| _distanceImage.getNumRows() != im.getNumRows()
					|| _distanceImage.getNumCols() != im.getNumCols()) {
				meanThresh = findMeanDistance(im);
			}
/*		} else {
			System.out.println("Error: other than BYTE or SHORT or " +
					"INT or FLOAT or DOUBLE image is not supported");

			return false;
		}*/

		int idx;
		_countB = _countW = 0;

		for (idx = 0; idx < _thrImage.getSize(); idx++) {
			if (_distanceImage.getDouble(idx) >= threshLower
					&& _distanceImage.getDouble(idx) <= threshUpper) {
				if(isBetweenLowerAndUpper){
					_thrImage.setByte(idx, LimitValues.BIN_ONE);
					_countW++;
				}else{
					_thrImage.setByte(idx, LimitValues.BIN_ZERO);
					_countB++;
				}
			} else {
                if(isBetweenLowerAndUpper){
                	_thrImage.setByte(idx, LimitValues.BIN_ZERO);
    				_countB++;
				}else{
					_thrImage.setByte(idx, LimitValues.BIN_ONE);
					_countW++;
				}
			}
		}

		return true;
	}

	/**
	 *
	 * @param im
	 * @param threshLower
	 * @param threshUpper
	 * @param isMeanDist
	 * @return
	 * @deprecated Use binarizeImage()
	 */
	/*
	public boolean BinarizeImage(ImageObject im, double threshLower,
			double threshUpper, boolean isMeanDist) {
		return binarizeImage(im, threshLower, threshUpper, isMeanDist);
	}*/

	/**
	 * threshold all bands by a scalar in the distance space
	 * @param im
	 * @param thresh
	 * @param isMeanDist
	 * @return
	 */
	public boolean binarizeImage(ImageObject im, double thresh,boolean isLowerThresh,
			boolean isMeanDist) {
		//sanity check
		if (thresh < 0 || thresh > im.getMax()) {
			System.out.println("Error: Thresh cannot be smaller than 0 or larger than the maximum value of the image");

			return false;//null;
		}

		double meanThresh;

/*		if (im.getType() == ImageObject.TYPE_BYTE
				|| im.getType() == ImageObject.TYPE_SHORT
				|| im.getType() == ImageObject.TYPE_INT
				|| im.getType() == ImageObject.TYPE_FLOAT
				|| im.getType() == ImageObject.TYPE_DOUBLE) {*/
			// this is initialization of thrImage
			if (_thrImage == null || _thrImage.getNumRows() != im.getNumRows()
					|| _thrImage.getNumCols() != im.getNumCols()) {
				_thrImage = null;

				try {
					_thrImage = ImageObject.createImage(im.getNumRows(),
							im.getNumCols(), 1, ImageObject.TYPE_BYTE);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}

			if (isMeanDist == false || _distanceImage == null
					|| _distanceImage.getData() == null
					|| _distanceImage.getNumRows() != im.getNumRows()
					|| _distanceImage.getNumCols() != im.getNumCols()) {
				meanThresh = findMeanDistance(im);
			}
/*		} else {
			System.out.println("Error: other than BYTE or SHORT or INT " +
					"or FLOAT or DOUBLE image is not supported");

			return false;
		}
*/
		int idx;
		_countB = _countW = 0;
		for (idx = 0; idx < _thrImage.getSize(); idx++) {
			if (_distanceImage.getDouble(idx) < thresh) {
				if(isLowerThresh){
					_thrImage.setByte(idx, LimitValues.BIN_ZERO);
					_countB++;
				}else{
					_thrImage.setByte(idx, LimitValues.BIN_ONE);
					_countW++;
				}
				
			} else {
				if(isLowerThresh){
					_thrImage.setByte(idx, LimitValues.BIN_ONE);
					_countW++;
			    }else{
					_thrImage.setByte(idx, LimitValues.BIN_ZERO);
					_countB++;
				}
			}
		}

		return true;
	}

	/**
	 *
	 * @param im
	 * @param thresh
	 * @param isMeanDist
	 * @return
	 * @deprecated Use binarizeImage()
	 */
     /*
	public boolean BinarizeImage(ImageObject im, double thresh,
			boolean isMeanDist) {
		return binarizeImage(im, thresh, isMeanDist);
	}/*/

	/**
	 *
	 * @param im
	 * @param Thresh
	 * @param OutFileName
	 * @return
	 * @throws Exception
	 *
	 *  threshold all bands by a scalar in the distance space
	 */
	public boolean binarizeImage(ImageObject im, int thresh, String outFileName, boolean isLowerThresh)
			throws Exception {

		boolean ret;
		ret = binarizeImage(im, thresh, isLowerThresh, false);

		try {
			ImageLoader.writeImage(outFileName, _thrImage);
		} catch(IOException e) {
			System.out.println("Error: error while saving a file");

			return false;
		}

		return true;
	}

	/**
	 *
	 * @param im
	 * @param thresh
	 * @param outFileName
	 * @return
	 * @throws Exception
	 * @deprecated Use binarizeImage()
	 */
	/*
	public boolean BinarizeImage(ImageObject im, int thresh, String outFileName)
		throws Exception {
		return binarizeImage(im, thresh, outFileName);
	}*/

	public double findMeanDistance(ImageObject im) {
		//sanity check
		if (im == null) {
			System.out.println("Error: no image");

			return -1.0;
		}

		/*if (im.getType() != ImageObject.TYPE_BYTE
				&& im.getType() != ImageObject.TYPE_SHORT
				&& im.getType() != ImageObject.TYPE_USHORT
				&& im.getType() != ImageObject.TYPE_INT
				&& im.getType() != ImageObject.TYPE_LONG
				&& im.getType() != ImageObject.TYPE_FLOAT
				&& im.getType() != ImageObject.TYPE_DOUBLE) {
			System.out.println("Error: other than BYTE or SHORT " +
					"or USHORT or LONG or INT or FLOAT or DOUBLE image is not supported");

			return -1.0;
		}
*/
		if (im.getData() == null) {
			System.out.println("Error: Image is null.");

			return -1.0;
		}

		int i, j;
		double sum, sumAll = 0.0;
		int num_samples = im.getNumRows() * im.getNumCols();

		if (num_samples <= 0) {
			System.out.println("Error: image has zero size");

			return -1.0;
		}

		_minDistance = Double.MAX_VALUE;
		_maxDistance = -Double.MAX_VALUE;

		if (_distanceImage == null || _distanceImage.getNumRows() != im.getNumRows()
				|| _distanceImage.getNumCols() != im.getNumCols()) {
			_distanceImage = null;
			try {
				_distanceImage = ImageObject.createImage(im.getNumRows(),
						im.getNumCols(), 1, ImageObject.TYPE_DOUBLE);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}

		int idx;
		double val;

		if (im.getNumBands() != 1) {
			for (i = 0, idx = 0; i < im.getSize(); i += im.getNumBands(), idx++) {
				sum = 0;

				for (j = 0; j < im.getNumBands(); j++) {
	/*				if (im.getByte(i + j) < 0)
						val = im.getByte(i + j) + LimitValues.MAXPOS_BYTE;//256;
					else
						val = im.getByte(i + j);*/
					val = im.getDouble(i+j);

					sum += val * val;
				}

				sum = Math.sqrt(sum);
				_distanceImage.setDouble(idx, sum);

				if (sum < _minDistance)
					_minDistance = sum;
				if (sum > _maxDistance)
					_maxDistance = sum;

				sumAll += sum;
			}


		} else {
			for (i = 0; i < im.getSize(); i += im.getNumBands()) {
	/*			if (im.getByte(i) < 0)
					sum = im.getByte(i) + LimitValues.MAXPOS_BYTE;//256;
				else
					sum = im.getByte(i);*/
				sum = im.getDouble(i);
				_distanceImage.setDouble(i, sum);

				if (sum < _minDistance)
					_minDistance = sum;
				if (sum > _maxDistance)
					_maxDistance = sum;

				sumAll += sum;
			}

		}

		if (num_samples > 0)
			_meanDistance = (sumAll / num_samples);
		else
			_meanDistance = -1.0;

		return _meanDistance;
	}

	/**
	 *
	 * @param im
	 * @return
	 * @deprecated Use findMeanDistance()
	 */
	public double FindMeanDistance(ImageObject im) {
		return findMeanDistance(im);
	}

	/**
	 * find the min and max and mean of each band
	 * @param im
	 * @return
	 */
	public boolean findMeanBands(ImageObject im) {
		//sanity check
		if (im == null || im.getNumRows() <= 0 || im.getNumCols() <= 0) {
			System.out.println("Error: no image");

			return false;
		}

/*		if (im.getData() == null && im.getType() != ImageObject.TYPE_BYTE
				&& im.getType() != ImageObject.TYPE_SHORT
				&& im.getType() != ImageObject.TYPE_INT
				&& im.getType() != ImageObject.TYPE_FLOAT
				&& im.getType() != ImageObject.TYPE_DOUBLE) {
			System.out.println("Error: only BYTE or SHORT or INT " +
					"or FLOAT or DOUBLE image is supported");

			return false;
		}*/

		_numBands = im.getNumBands();
		_minBand = new double[im.getNumBands()];
		_maxBand = new double[im.getNumBands()];
		_meanBand = new double[im.getNumBands()];

		int i, j;

		for (i = 0; i < im.getNumBands(); i++) {
			_minBand[i] = Double.MAX_VALUE;
			_maxBand[i] = -Double.MAX_VALUE;
			_meanBand[i] = 0.0E1;
		}

		double sum = 0.0, mult = 1.0;
		double val, temp;
		int num_samples = im.getNumRows() * im.getNumCols();

		for (j = 0; j < im.getNumBands(); j++) {
			sum = 0.0;

			for (i = j; i < im.getSize(); i += im.getNumBands()) {
/*				val = im.getByte(i);

				if (val < 0)
					val += LimitValues.MAXPOS_BYTE;//256;
*/
				val = im.getDouble(i);
				sum += val;

				if (val < _minBand[j])
					_minBand[j] = val;
				if (val > _maxBand[j])
					_maxBand[j] = val;
			}

			_meanBand[j] = sum / (double) num_samples;
		}


		// find HalfVolBand values
		_halfVolBand = new double[im.getNumBands()];
		int[] tempIndex;
		tempIndex = new int[im.getNumBands()];

		// accommodate large volume number by dividing each number
		double div = 1.0;

		if (div > 1.0E300) {
			div = 1.0;
			System.out.println("ERROR: divisor had to modified=" + div);
		}

		// use log space to compute half volume
		sum = 0.0;
		for (j = 0; j < im.getNumBands(); j++) {
			sum += Math.log((_maxBand[j] - _minBand[j]));
			tempIndex[j] = j; // init indices
		}

		// divide the volume by eight (originally by two) in log space
		sum -= Math.log(8.0);

		double halfVol, oneSide;
		// now perform square root
		oneSide = sum / im.getNumBands();
		// convert from log space back
		oneSide = Math.exp(oneSide);

		// assign values
		for (j = 0; j < im.getNumBands(); j++) {
			if (_minBand[j] + oneSide <= _maxBand[j]) {
				_halfVolBand[j] = _minBand[j] + oneSide;
			} else {
				_halfVolBand[j] = _maxBand[j];
			}
		}

		return true;
	}

	/**
	 *
	 * @param im
	 * @return
	 * @deprecated Use findMeanBands()
	 */
	public boolean FindMeanBands(ImageObject im) {
		return findMeanBands(im);
	}
}
