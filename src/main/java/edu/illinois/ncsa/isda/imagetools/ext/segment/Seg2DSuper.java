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
 * Seg2DSuper.java
 *
 */

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImPoint;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.imagetools.core.datatype.LimitValues;
import edu.illinois.ncsa.isda.imagetools.ext.math.GeomOper;

////////////////////////////////////////////////////////////////////
/**
 * <B>The class Seg2DSuper provides a tool for supervised region-growing
 * segmentation by selecting points and growing regions one by one. </B>
 *
 * <BR>
 * <BR>
 * <B>Description: </B> <BR>
 *
 * @author Peter Bajcsy, Young-Jin Lee
 * @version 1.0
 *
 */

public class Seg2DSuper extends SegObject {
	protected boolean _debugSeg2DSuper = true;

	public Threshold myThresh = new Threshold();

	protected double[] suma = null;

	protected int[] temp1 = null;

	protected int[] labeling = null;

	protected int _sizeSuma = -1;

	protected int _sizeLabeling = -1;

	protected int _maskMaxLabel = 1;

	protected int _unlabeled = 1;

	protected int _numRemoved = 0;

	protected int _halfWindow = 1;

	protected double _percent = 50.0;// percentage of (halfWindow*2 +1)^2

	protected int _sizeThresh = 9;

	// defines whether the noise removal should be applied till numRemoval =0
	protected boolean _removeAll = false;

	//constructor
	public Seg2DSuper() {
		_debugSeg2DSuper = true;
	}

	// Getters
	/**
	 * @deprecated Use getSegObject()
	 */
	public SegObject GetSegObject() {
		return (SegObject) this;
	}

	public SegObject getSegObject() {
		return (SegObject) this;
	}

	/**
	 *
	 * @return
	 *
	 * @deprecated Use getDebugFlag()
	 */
	public boolean GetDebugFlag() {
		return _debugSeg2DSuper;
	}

	public boolean getDebugFlag() {
		return _debugSeg2DSuper;
	}

	/**
	 *
	 * @return
	 *
	 * @deprecated Use getMaskMaxLabel()
	 */
	public int GetMaskMaxLabel() {
		return (_maskMaxLabel - 1);
	}

	public int getMaskMaxLabel() {
		return (_maskMaxLabel - 1);
	}

	/**
	 *
	 * @return
	 *
	 * @deprecated Use getNumRemoved()
	 */
	public int GetNumRemoved() {
		return (_numRemoved);
	}

	public int getNumRemoved() {
		return _numRemoved;
	}

	/**
	 *
	 * @return
	 *
	 * @deprecated Use getRemoveAll()
	 */
	public boolean GetRemoveAll() {
		return _removeAll;
	}

	public boolean getRemoveAll() {
		return _removeAll;
	}

	/**
	 *
	 * @param val
	 *
	 * @deprecated Use setRemoveAll()
	 */
	public void SetRemoveAll(boolean val) {
		_removeAll = val;
	}

	public void setRemoveAll(boolean val) {
		_removeAll = val;
	}

	/**
	 *
	 * @param val
	 * @return
	 *
	 * @deprecated Use setUnlabeled()
	 */
	public boolean SetUnlabeled(int val) {
		if (val <= 0)
			return false;
		_unlabeled = val;
		return true;
	}

	public boolean setUnlabeled(int val) {
		if (val <= 0)
			return false;
		_unlabeled = val;
		return true;
	}

	/**
	 *
	 * @return
	 *
	 * @deprecated Use getUnlabeled()
	 */
	public int GetUnlabeled() {
		return (_unlabeled);
	}

	public int getUnlabeled() {
		return (_unlabeled);
	}

	/**
	 *
	 * @param val
	 * @return
	 * @deprecated Use setPercent()
	 */
	public boolean SetPercent(double val) {
		if (val < 0 || val > 100)
			return false;
		_percent = val;
		return true;
	}

	public boolean setPercent(double val) {
		if (val < 0 || val > 100)
			return false;

		_percent = val;

		return true;
	}

	/**
	 *
	 * @return
	 * @deprecated Use getPercent()
	 */
	public double GetPercent() {
		return (_percent);
	}

	public double getPercent() {
		return (_percent);
	}

	/**
	 *
	 * @param val
	 * @return
	 * @deprecated Use setHalfWindow()
	 */
	public boolean SetHalfWindow(int val) {
		if (val <= 0)
			return false;
		_halfWindow = val;
		return true;
	}

	public boolean setHalfWindow(int val) {
		if (val <= 0)
			return false;

		_halfWindow = val;

		return true;
	}

	/**
	 *
	 * @return
	 * @deprecated Use getHalfWindow()
	 */
	public int GetHalfWindow() {
		return (_halfWindow);
	}

	public int getHalfWindow() {
		return (_halfWindow);
	}

	/**
	 *
	 * @param val
	 * @return
	 * @deprecated Use setSizeThresh()
	 */
	public boolean SetSizeThresh(int val) {
		if (val <= 1)// the size makes sense if it is 2 and larger
			return false;
		_sizeThresh = val;
		return true;
	}

	public boolean setSizeThresh(int val) {
		if (val <= 1)// the size makes sense if it is 2 and larger
			return false;

		_sizeThresh = val;

		return true;
	}

	/**
	 *
	 * @return
	 * @deprecated Use getSizeThresh()
	 */
	public int GetSizeThresh() {
		return (_sizeThresh);
	}

	public int getSizeThresh() {
		return (_sizeThresh);
	}

	/**
	 *
	 * @param flag
	 * @deprecated Use setDebugFlag()
	 */
	public void SetDebugFlag(boolean flag) {
		_debugSeg2DSuper = flag;
	}

	public void setDebugFlag(boolean flag) {
		_debugSeg2DSuper = flag;
	}

	/**
	 *
	 * @param imgOrig
	 * @param mask
	 * @param unlabeled
	 * @param pts
	 * @param delta
	 * @param isMeandDistReady
	 * @return
	 * @deprecated use nonTexture_Sphere()
	 */
	public boolean NonTexture_Sphere(ImageObject imgOrig, ImageObject mask,
			int unlabeled, ImPoint pts, double delta, boolean isMeandDistReady) {
		return nonTexture_Sphere(imgOrig, mask, unlabeled, pts, delta, isMeandDistReady);
	}

	/**
	 * segmentation into homogeneous regions using a Sphere model
	 * (or Euclidean distance model)
	 * @param imgOrig input image
	 * @param mask should be initialized to a positive number larger than 0
	 * @param unlabeled should be equal to minimum label and the labels
	 * should be only positive
	 * @param pts is the location from where we start region growing
	 * @param delta is the radius of a sphere from the value of a given pts
	 * @param isMeandDistReady is used for efficient execution during
	 * multiple entries
	 * @return
	 */
	public boolean nonTexture_Sphere(ImageObject imgOrig, ImageObject mask,
			int unlabeled, ImPoint pts, double delta, boolean isMeandDistReady) {
		//sanity check
		if (imgOrig == null) {
			System.out.println("ERROR: missing image data");

			return false;
		}

		//TODO
//		if (!imgOrig.sampType.equalsIgnoreCase("BYTE")
//				&& !imgOrig.sampType.equalsIgnoreCase("SHORT")
//				&& !imgOrig.sampType.equalsIgnoreCase("FLOAT")
//				&& !imgOrig.sampType.equalsIgnoreCase("DOUBLE")) {
//			System.out.println("ERROR: other than BYTE, SHORT, FLOAT or DOUBLE data type is not supported");
//
//			return false;
//		}

		if (pts == null || (pts.x < 0 ||
				pts.y < 0 || pts.x >= imgOrig.getNumRows() ||
				pts.y >= imgOrig.getNumCols())) {
			System.out.println("ERROR: missing pts or pts is out of image area");

			return false;
		}

		if (delta < 0) {
			System.out.println("ERROR: delta is smaller than 0");

			return false;
		}

		if (unlabeled < 1) {
			System.out.println("ERROR: unlabeled is smaller than 1");

			return false;
		}

		int i;
		_maskMaxLabel = 1;
		_numRemoved = 0;

		if (mask == null) {
			mask = null;

			try {
				mask = ImageObject.createImage(imgOrig.getNumRows(),
						imgOrig.getNumCols(), 1, ImageObject.TYPE_INT);
			} catch(Exception e) {
				e.printStackTrace();
			}

			// default values
			unlabeled = 1;
			isMeandDistReady = false;

			// init the mask
			for (i = 0; i < mask.getSize(); i++) {
				mask.setInt(i, unlabeled);
			}

			_maskMaxLabel = 2;
		} else {
			_maskMaxLabel = -1;

			for (i = 0; i < mask.getSize(); i++) {
				if (mask.getInt(i) > _maskMaxLabel)
					_maskMaxLabel = mask.getInt(i);
			}

			if (_maskMaxLabel == -1) {
				System.out.println("WARNING: _maskMaxLabel = -1");
				_maskMaxLabel = 1;
			} else {
				_maskMaxLabel++;
				System.out.println("TEST: before region growing: _maskMaxLabel ="
					+ _maskMaxLabel);
			}
		}

		_unlabeled = unlabeled;

		// threshold image by Euclidean distance of given point to the origin +/- delta

		int indexLab = (int) pts.x * imgOrig.getNumCols() + (int) pts.y;
		int index = indexLab * imgOrig.getNumBands();

		// computation has to be done only if the point has not been assigned
		// already
		if (mask.getInt(indexLab) != unlabeled) {
			System.out.println("INFO: pixel has been already labeled with value="
				+ mask.getInt(indexLab));

			return true;
		}

		ImageObject origin = null;
		try {
			origin = ImageObject.createImage(1, 1, imgOrig.getNumBands(), ImageObject.TYPE_DOUBLE);
		} catch(Exception e) {
			e.printStackTrace();

		}

		for (i = 0; i < imgOrig.getNumBands(); i++) {
			origin.setDouble(i, 0.0);
		}

		double dist = -1.0;
		GeomOper myGeomOper = new GeomOper();

		double[] point1 = new double[imgOrig.getNumBands()];
		double[] point2 = new double[imgOrig.getNumBands()];
		for (int k=0;k< imgOrig.getNumBands();k++){
			point1[k] = origin.getDouble(k);
			point2[k] = imgOrig.getDouble(index+k);			
		}
		dist = myGeomOper.euclidDist(imgOrig.getNumBands(), point1, point2);
			
		
	/*	if (imgOrig.getType() == ImageObject.TYPE_BYTE) {
			dist = myGeomOper.euclidDist(imgOrig.getNumBands(),
					(double[]) origin.getData(), 0,
					(byte[]) imgOrig.getData(), index);
		} else if (imgOrig.getType() == ImageObject.TYPE_SHORT) {
			dist = myGeomOper.euclidDist(imgOrig.getNumBands(),
					(double[]) origin.getData(), 0,
					(short[]) imgOrig.getData(), index);
		} else if (imgOrig.getType() == ImageObject.TYPE_FLOAT || imgOrig.getType() == ImageObject.TYPE_USHORT || imgOrig.getType() == ImageObject.TYPE_LONG) {
			dist = myGeomOper.euclidDist(imgOrig.getNumBands(),
					(double[]) origin.getData(), 0,
					(float[]) imgOrig.getData(), index);
		} else if (imgOrig.getType() == ImageObject.TYPE_DOUBLE) {
			dist = myGeomOper.euclidDist(imgOrig.getNumBands(),
					(double[]) origin.getData(), 0,
					(double[]) imgOrig.getData(), index);
		}
*/
		double lowerThresh = dist - delta;

		if (lowerThresh < 0)
			lowerThresh = 0;

		double upperThresh = dist + delta;

		if (upperThresh < lowerThresh) {
			System.out.println("WARNING: no update because the distance upperThresh < lowerThresh");
			System.out.println("WARNING: distance=" + dist + ",upperThresh="
					+ upperThresh + ",lowerThresh=" + lowerThresh);

			return true;
		}

		//test
		System.out.println("TEST: distance=" + dist + ",upperThresh="
				+ upperThresh + ",lowerThresh=" + lowerThresh);

		ImageObject threshImage = null;

		if (!myThresh.binarizeImage(imgOrig, lowerThresh, upperThresh,
				isMeandDistReady,false)) {
			System.out.println("ERROR: could not compute the thresholded image");

			return false;
		}

		threshImage = myThresh.getImageObject();
		if (threshImage == null) {
			System.out.println("ERROR: thresholded image is null");

			return false;
		}

		for (i = 0; i < mask.getSize(); i++) {
			if (mask.getInt(i) != unlabeled) {
				threshImage.setByte(i, LimitValues.BIN_ONE);//byteVal;//_lim.BIN_ZERO;
			}
		}

		ConnectAnal myConAnal = new ConnectAnal();

		if (!myConAnal.binary_CA(threshImage)) {
			System.out.println("ERROR: connectivity analysis failed");

			return false;
		}

		ImageObject imLabels = myConAnal.getImLabels();

		if (imLabels == null || imLabels.getData() == null) {
			System.out.println("ERROR: label image is null");

			return false;
		}

		int selectLabel = imLabels.getInt(indexLab);

		for (i = 0; i < imLabels.getSize(); i++) {
			if (imLabels.getInt(i) == selectLabel) {
				mask.setInt(i, _maskMaxLabel);
			}
		}

		_maskMaxLabel++;

		//test
		System.out.println("TEST: after region growing: _maskMaxLabel ="
				+ _maskMaxLabel);

		return true;
	}

	/**
	 * segmentation into homogeneous regions using a Rectangular Box model
	 * @param imgOrig input image
	 * @param mask should be initialized to a positive number larger than 0
	 * @param unlabeled should be equal to minimum label and the labels should be only
	 * positive
	 * @param pts is the location from where we start region growing
	 * @param delta contains the half dimensions of box around the value for a given
	 * pts
	 * @return
	 */
	public boolean nonTexture_Box(ImageObject imgOrig, ImageObject mask,
			int unlabeled, ImPoint pts, ImageObject delta) {

		//sanity check
		if (imgOrig == null) {
			System.out.println("ERROR: missing image data");

			return false;
		}

		if (imgOrig.getType() != ImageObject.TYPE_BYTE
				&& imgOrig.getType() != ImageObject.TYPE_SHORT
				&& imgOrig.getType() != ImageObject.TYPE_USHORT
				&& imgOrig.getType() != ImageObject.TYPE_LONG
				&& imgOrig.getType() != ImageObject.TYPE_FLOAT
				&& imgOrig.getType() != ImageObject.TYPE_DOUBLE) {
			System.out.println("ERROR: other than BYTE, SHORT, FLOAT or DOUBLE data type is not supported");

			return false;
		}

		if (pts == null || (pts.x < 0 || pts.y < 0 ||
				pts.x >= imgOrig.getNumRows() || pts.y >= imgOrig.getNumCols())) {
			System.out.println("ERROR: missing pts or pts is out of image area");

			return false;
		}

		if (delta == null || delta.getData() == null) {
			System.out.println("ERROR: delta vector is missing or it is not DOUBLE");

			return false;
		}

		if (unlabeled < 1) {
			System.out.println("ERROR: unlabeled is smaller than 1");

			return false;
		}

		int i;
		_maskMaxLabel = 1;
		_numRemoved = 0;

		if (mask == null) {
			mask = null;

			try {
				mask = ImageObject.createImage(imgOrig.getNumRows(),
						imgOrig.getNumCols(), 1, ImageObject.TYPE_INT);
			} catch(Exception e) {
				e.printStackTrace();
			}

			// default values
			unlabeled = 1;

			// init the mask
			for (i = 0; i < mask.getSize(); i++) {
				mask.setInt(i, unlabeled);
			}

			_maskMaxLabel = 2;
		} else {
			_maskMaxLabel = -1;

			for (i = 0; i < mask.getSize(); i++) {
				if (mask.getInt(i) > _maskMaxLabel)
					_maskMaxLabel = mask.getInt(i);
			}

			if (_maskMaxLabel == -1) {
				System.out.println("WARNING: _maskMaxLabel = -1");
				_maskMaxLabel = 1;
			} else {
				_maskMaxLabel++;
				System.out.println("TEST: _maskMaxLabel =" + _maskMaxLabel);
			}
		}

		_unlabeled = unlabeled;


		// threshold image by +/-delta around the value of a given point in each
		// dimension

		int indexLab = (int) pts.x * imgOrig.getNumCols() + (int) pts.y;
		int index = indexLab * imgOrig.getNumBands();

		// computation has to be done only if the point has not been assigned
		// already
		if (mask.getInt(indexLab) != unlabeled) {
			System.out.println("INFO: pixel has been already labeled with value="
							+ mask.getInt(indexLab));

			return true;
		}

		ImageObject lowerThresh = null;
		ImageObject	upperThresh = null;

		try {
			lowerThresh = ImageObject.createImage(1, 1,
					imgOrig.getNumBands(), ImageObject.TYPE_DOUBLE);
			upperThresh = ImageObject.createImage(1, 1,
					imgOrig.getNumBands(), ImageObject.TYPE_DOUBLE);
		} catch(Exception e) {
			e.printStackTrace();
		}


		for (i = 0; i < imgOrig.getNumBands(); i++) {
			lowerThresh.setDouble(i, (imgOrig.getByte(index + i) & 0xff)
					- delta.getDouble(i));
			upperThresh.setDouble(i, (imgOrig.getByte(index + i) & 0xff)
					+ delta.getDouble(i));

			if (lowerThresh.getDouble(i) < 0)
				lowerThresh.setDouble(i, 0.0);

			if (upperThresh.getDouble(i) < lowerThresh.getDouble(i))
				upperThresh.setDouble(i, lowerThresh.getDouble(i));

			if (upperThresh.getDouble(i) > 255)
				upperThresh.setDouble(i, 255.0);
		}

		//TODO
//		if (imgOrig.sampType.equalsIgnoreCase("BYTE")) {
//			for (i = 0; i < imgOrig.sampPerPixel; i++) {
//				lowerThresh.imageDouble[i] = (imgOrig.image[index + i] & 0xff)
//						- delta.imageDouble[i];
//				upperThresh.imageDouble[i] = (imgOrig.image[index + i] & 0xff)
//						+ delta.imageDouble[i];
//				if (lowerThresh.imageDouble[i] < 0)
//					lowerThresh.imageDouble[i] = 0.0;
//				if (upperThresh.imageDouble[i] < lowerThresh.imageDouble[i])
//					upperThresh.imageDouble[i] = lowerThresh.imageDouble[i];
//				if (upperThresh.imageDouble[i] > 255)
//					upperThresh.imageDouble[i] = 255.0;
//			}
//		}
//		if (imgOrig.sampType.equalsIgnoreCase("SHORT")) {
//			for (i = 0; i < imgOrig.sampPerPixel; i++) {
//				lowerThresh.imageDouble[i] = (imgOrig.imageShort[index + i] & 0xffff)
//						- delta.imageDouble[i];
//				upperThresh.imageDouble[i] = (imgOrig.imageShort[index + i] & 0xffff)
//						+ delta.imageDouble[i];
//				if (lowerThresh.imageDouble[i] < 0)
//					lowerThresh.imageDouble[i] = 0.0;
//				if (upperThresh.imageDouble[i] < lowerThresh.imageDouble[i])
//					upperThresh.imageDouble[i] = lowerThresh.imageDouble[i];
//				if (upperThresh.imageDouble[i] > 65536)
//					upperThresh.imageDouble[i] = 65536.0;
//			}
//		}
//		if (imgOrig.sampType.equalsIgnoreCase("FLOAT")) {
//			for (i = 0; i < imgOrig.sampPerPixel; i++) {
//				lowerThresh.imageDouble[i] = imgOrig.imageFloat[index + i]
//						- delta.imageDouble[i];
//				upperThresh.imageDouble[i] = imgOrig.imageFloat[index + i]
//						+ delta.imageDouble[i];
//				if (lowerThresh.imageDouble[i] < _lim.MIN_FLOAT)
//					lowerThresh.imageDouble[i] = _lim.MIN_FLOAT;
//				if (upperThresh.imageDouble[i] < lowerThresh.imageDouble[i])
//					upperThresh.imageDouble[i] = lowerThresh.imageDouble[i];
//				if (upperThresh.imageDouble[i] > _lim.MAX_FLOAT)
//					upperThresh.imageDouble[i] = _lim.MAX_FLOAT;
//			}
//
//		}
//		if (imgOrig.sampType.equalsIgnoreCase("DOUBLE")) {
//			for (i = 0; i < imgOrig.sampPerPixel; i++) {
//				lowerThresh.imageDouble[i] = imgOrig.imageDouble[index + i]
//						- delta.imageDouble[i];
//				upperThresh.imageDouble[i] = imgOrig.imageDouble[index + i]
//						+ delta.imageDouble[i];
//				if (upperThresh.imageDouble[i] < lowerThresh.imageDouble[i])
//					upperThresh.imageDouble[i] = lowerThresh.imageDouble[i];
//			}
//
//		}

		ImageObject threshImage = null;
		if (!myThresh.binarizeImageLowerUpper(imgOrig, lowerThresh, upperThresh)) {
			System.err.println("ERROR: could not compute the thresholded image");

			return false;
		}

		threshImage = myThresh.getImageObject();
		if (threshImage == null) {
			System.err.println("ERROR: thresholded image is null");

			return false;
		}

		// incorporate the mask values that were already used so that
		// the connectivity analysis uses only unlabeled pixels
		for (i = 0; i < mask.getSize(); i++) {
			if (mask.getInt(i) != unlabeled) {
				threshImage.setByte(i, LimitValues.BIN_ONE);
			}
		}

		ConnectAnal myConAnal = new ConnectAnal();
		if (!myConAnal.binary_CA(threshImage)) {
			System.out.println("ERROR: connectivity analysis failed");

			return false;
		}

		ImageObject imLabels = myConAnal.getImLabels();
		if (imLabels == null || imLabels.getData() == null) {
			System.out.println("ERROR: label image is null");

			return false;
		}

		int selectLabel = imLabels.getInt(indexLab);

		for (i = 0; i < imLabels.getSize(); i++) {
			if (imLabels.getInt(i) == selectLabel) {
				mask.setInt(i, _maskMaxLabel);
			}
		}

		_maskMaxLabel++;

		return true;
	}

	/**
	 *
	 * @param imgOrig
	 * @param mask
	 * @param unlabeled
	 * @param pts
	 * @param delta
	 * @return
	 * @deprecated Use nonTexture_Box()
	 */
	public boolean NonTexture_Box(ImageObject imgOrig, ImageObject mask,
			int unlabeled, ImPoint pts, ImageObject delta) {
		return nonTexture_Box(imgOrig, mask, unlabeled, pts, delta);
	}

	private void mergefast(int first, int second, ImageObject region,
			int[] temp1) {
		int pom, pom1;
		pom = temp1[region.getInt(second)];

		while (temp1[pom] != pom)
			pom = temp1[pom];

		pom1 = temp1[region.getInt(first)];

		while (temp1[pom1] != pom1)
			pom1 = temp1[pom1];

		if (pom1 > pom) {
			temp1[pom1] = pom;
		} else {
			if (pom1 < pom) {
				temp1[pom] = pom1;
			}
		}
	}

	/**
	 * This is a method for cleaning up small regions (< areaThresh) that are
	 * surrounded by a given label (see the argument list - label) in a mask image
	 * Elimination is based on a percentage (= 50%) of the window of size 3x3
	 * (see window)
	 * @param mask
	 * @param label
	 * @return
	 */
	public boolean noiseRemoval(ImageObject mask, int label) {
		return noiseRemoval(mask, label, _sizeThresh, _halfWindow, _percent,
				_removeAll);
	}

	/**
	 *
	 * @param mask
	 * @param label
	 * @return
	 * @deprecated Use noiseRemoval()
	 */
	public boolean NoiseRemoval(ImageObject mask, int label) {
		return noiseRemoval(mask, label);
	}

	public boolean noiseRemoval(ImageObject mask, int label, int sizeThresh,
			int halfWindow, double percentInput, boolean removeAll) {
		//sanity check
		if (mask == null || mask.getData() == null || mask.getNumBands() != 1) {
			System.err.println("ERROR: missing mask or mask != INT " +
					"or mask.sampPerPixel !=1");

			return false;
		}

		if (sizeThresh < 1) {
			System.err.println("ERROR: sizeThresh <1");

			return false;
		}

		if (halfWindow < 1) {
			System.err.println("ERROR: halfWindow <1");

			return false;
		}

		if (percentInput < 0 || percentInput > 100) {
			System.err.println("ERROR: percent is out of [0,100] range");

			return false;
		}

		ConnectAnal myConAnal = new ConnectAnal();
		int[] areaSize = null;
		int numLabels;
		int i, j, i1, j1;

		int index, lab, count, temp;

		// // 50% of the window size rounded up
		int percent = (int) (percentInput / 100.0 * ((halfWindow << 1) + 1)
				* ((halfWindow << 1) + 1) + 0.5);
		//test
		System.out.println("TEST: label = " + label + ",halfWindow="
				+ halfWindow + ", _percent=" + _percent + ",areaThresh="
				+ sizeThresh + ", removeAll=" + removeAll);

		int[] rowCoord = new int[(mask.getNumCols() << 1)];
		int[] colCoord = new int[(mask.getNumCols() << 1)];

		int idxMask;
		int idxCoord = 0;
		boolean repeat = true;
		_numRemoved = 0;
		int iterRemoved = 10;
		int numIter = 0, maxNumIter = 20;
		ImageObject conLabels = null;

		while (repeat && iterRemoved > 0 && numIter < maxNumIter) {
			repeat = false;

			if (removeAll) {
				repeat = true;
			}

			iterRemoved = 0;

			if (!myConAnal.binary_CA(mask)) {
				System.err.println("ERROR: connectivity analysis failed");

				return false;
			}

			areaSize = myConAnal.getAreaS();
			conLabels = myConAnal.getImLabels();

			numLabels = myConAnal.getNFoundS();
			if (areaSize == null || conLabels == null) {
				System.err.println("ERROR: areaSize or connectivity Labels is null");

				return false;
			}

			idxMask = halfWindow * mask.getNumCols() + halfWindow;

			for (i = halfWindow; i < mask.getNumRows() - halfWindow; i++) {
				for (j = halfWindow; j < mask.getNumCols() - halfWindow; j++) {
					lab = conLabels.getInt(idxMask);

					if (lab <= 0 || lab > numLabels) {
						System.err.print("ERROR: label for area array is out of bounds, lab="
										+ lab + ",numLabels = " + numLabels);
						lab = 0;
					}

					if (areaSize[lab] < sizeThresh) {
						// check if the surrounding labels
						//// index of the upper left corner of the window
						index = idxMask - (halfWindow) * mask.getNumCols()
								- halfWindow;
						count = 0; // count of a particular input label in the
								   // nbh

						for (i1 = 0; i1 < (halfWindow << 1) + 1; i1++) {
							for (j1 = 0; j1 < (halfWindow << 1) + 1; j1++) {
								if (mask.getInt(index) == label) {
									count++;
								}

								index++;
							}

							index = index - ((halfWindow << 1) + 1)
									+ mask.getNumCols();
						}

						if (count >= percent) {
							rowCoord[idxCoord] = i;
							colCoord[idxCoord] = j;
							idxCoord++;
							_numRemoved++;
							iterRemoved++;
						}
					}

					idxMask++;
				} // end of j loop
				idxMask += (halfWindow << 1);

				if (i > halfWindow) {
					// insert labels into the found locations
					count = 0;
					temp = (i - 1) * mask.getNumCols();

					for (index = 0; index < idxCoord; index++) {
						if (rowCoord[index] == i - 1) {
							mask.setInt(temp + colCoord[index], label);
							count++;
						}
					}

					// move the values
					for (index = 0; index < idxCoord - count; index++) {
						rowCoord[index] = rowCoord[index + count];
						colCoord[index] = colCoord[index + count];
					}

					idxCoord -= count;
				}
			} // end of i loop

			// remove the the last processed row
			if (idxCoord > 0) {
				// insert labels into the found locations
				for (index = 0; index < idxCoord; index++) {
					mask.setInt(rowCoord[index] * mask.getNumCols()
							+ colCoord[index], label);
				}

				idxCoord = 0;
			}
			numIter++;
		}// end of while loop

		//test
		System.out.println("TEST: numIter=" + numIter + ", numRemoved="
				+ _numRemoved);

		return true;
	}

	/**
	 *
	 * @param mask
	 * @param label
	 * @param sizeThresh
	 * @param halfWindow
	 * @param percentInput
	 * @param removeAll
	 * @return
	 * @deprecated Use noiseRemoval(ImageObject mask, int label, int sizeThresh,
			int halfWindow, double percentInput, boolean removeAll)
	 */
	public boolean NoiseRemoval(ImageObject mask, int label, int sizeThresh,
			int halfWindow, double percentInput, boolean removeAll) {
		return noiseRemoval(mask, label, sizeThresh,
				halfWindow, percentInput, removeAll);
	}

	public boolean noiseRemoval1(int maxpetox, int maxitera, int minsize,
			ImageObject imageMean, double[] imageS, ImageObject imgprocessed,
			ImageObject region, int iterLayers, int[] newsize) {

		//sanity check
		if (imgprocessed == null || imgprocessed.getData() == null) {
			System.out.println("ERROR: imgprocessed != DOUBLE");

			return false;
		}

		int i, j, i1, j1, k;
		boolean signal = true;
		int window, percent, maxhist;
		int[] hist = null;
		int[] histi1j1 = null;
		double[] histattrib = null;
		double[] image1 = null;
		int[] histcount = null;

		int count = 0, index, indexLabel, indexLabel1, max, peto;
		double minvalue, mindif, tmpvalue;
		int itera;
		int change;
		int here, here1, top, maxindex;
		int pom;

		maxpetox++; // this is due to the fact that we pass _NFoundS =
					// maxpetox-1;

		// initial setup
		window = _filterWindow;//1;
		percent = (int) (_percentFilterWindow * ((window << 1) + 1)
				* ((window << 1) + 1) + 0.5);//5; // 0.64 x ( (2 x window +1)^2
											 // -1)

		if (maxitera > 0) {
			pom = (2 * window + 1) * (2 * window + 1);
			top = imageMean.getNumRows() * imageMean.getNumCols();//xsz*ysz;
			hist = new int[pom];
			histi1j1 = new int[pom];

			histcount = new int[pom];
			histattrib = new double[pom];

			image1 = new double[top];

			if (suma == null || maxpetox > _sizeSuma) {
				suma = new double[maxpetox];
			}

			if (labeling == null || maxpetox > _sizeLabeling) {
				labeling = new int[maxpetox];
			}

			if (temp1 == null || maxpetox >= _sizeLabeling) {
				temp1 = new int[maxpetox];
			}
		} else
			return true;

		change = 120;
		itera = 0;

		while (change > 0 && itera < maxitera) {
			change = 0;
			for (i = 1; i < maxpetox; i++)
				temp1[i] = i;

			here = 0;
			indexLabel = iterLayers;

			for (i = 0; i < imageMean.getNumRows(); i++) {
				for (j = 0; j < imageMean.getNumCols(); j++) {
					image1[here] = imageMean.getDouble(here);

					if (newsize[temp1[region.getInt(indexLabel)]] < minsize) {
						// calculate the histogram of region labels
						maxhist = 0;
						i1 = i - window;
						j1 = j - window;

						while (i1 < imageMean.getNumRows() && i1 < (i + window + 1)) {
							if (i1 >= 0) {
								j1 = j - window;
								here1 = i1 * imageMean.getNumCols() + j1;
								indexLabel1 = here1 * region.getNumBands() + iterLayers;

								while (j1 < imageMean.getNumCols()
										&& j1 < (j + window + 1)) {
									if ((i1 == i && j1 == j) || j1 < 0) {
									} else {
										signal = false;

										for (k = 0; k < maxhist; k++) {
											if (hist[k] == region.getInt(indexLabel1)) {
												signal = true;
												histcount[k] += 1;
												k = maxhist;
											}
										}

										if (!signal) {
											hist[maxhist] = region.getInt(indexLabel1);
											histcount[maxhist] = 1;
											histattrib[maxhist] = imageMean.getDouble(here1);
											histi1j1[maxhist] = indexLabel1;//here1;
											maxhist += 1;
										}
									}

									j1 += 1;
									here1++;
									indexLabel1 += region.getNumBands();
								}
							}

							i1 += 1;
						}

						//if the histogram at (i,j) contains a bin with a value
						// larger
						// than percent then (i,J) merges with the label of max
						max = 0;
						index = maxindex = 0;
						mindif = Double.MAX_VALUE;// (float)MaxPGM;
						minvalue = Double.MAX_VALUE;//(float)MaxPGM;

						for (k = 0; k < maxhist; k++) {
							signal = false;

							if (histcount[k] >= percent) {
								if (temp1[region.getInt(indexLabel)] != temp1[hist[k]]) {
									mergefast(indexLabel, histi1j1[k], region,
											temp1);
									tmpvalue = newsize[region.getInt(indexLabel)];
									newsize[region.getInt(indexLabel)] += newsize[region.getInt(histi1j1[k])];

									newsize[region.getInt(histi1j1[k])] += tmpvalue;
									image1[here] = histattrib[k];
									change += 1;
								}

								signal = true;
								k = maxhist;
							} else {
								if (histcount[k] > max) {
									max = histcount[k];
									maxindex = histi1j1[k];
									index = k;
								}

								// if two bins have equal max values then merge
								// to the one
								// having closer avg gray value
								if (histcount[k] == max) {
									if (Math.abs(histattrib[index]
											- imgprocessed.getDouble(here)) > Math
											.abs(histattrib[k]
													- imgprocessed.getDouble(here))) {
										max = histcount[k];
										maxindex = histi1j1[k];
										index = k;
									}
								}

								// special care is needed around the image
								// border
								if ((i == 0 || i == (imageMean.getNumRows() - 1)
										|| j == 0 || j == (imageMean.getNumCols() - 1))
										&& Math
												.abs(histattrib[k]
														- imgprocessed.getDouble(here)) <= mindif) {
									mindif = Math.abs(histattrib[k]
											- imgprocessed.getDouble(here));
									minvalue = histattrib[k];
								}
							}
						}
					}

					here++;
					indexLabel += region.getNumBands();
				}
			}

			// minimum label for the regions
			for (peto = 1; peto < maxpetox; peto++) {
				if (temp1[peto] != peto) {
					pom = temp1[peto];
					while (temp1[pom] != pom)
						pom = temp1[pom];

					temp1[peto] = pom;
				}
			}

			// initialize newsize and new labels
			for (peto = 1; peto < maxpetox; peto++) {
				newsize[peto] = 0;
				labeling[peto] = temp1[peto];
				temp1[peto] = 0;
			}

			// count size of new regions
			//for(i=0;i<top;i++){
			for (indexLabel = iterLayers; indexLabel < region.getSize(); indexLabel += region.getNumBands()) {
				peto = region.getInt(indexLabel);
				peto = labeling[peto];
				newsize[peto] += 1;
			}

			// relabel new regions
			count = 0;
			for (peto = 1; peto < maxpetox; peto++) {
				if (newsize[peto] != 0) {
					count += 1;
					temp1[peto] = count;
					newsize[count] = newsize[peto];
				}
			}

			// update region labels and image values
			indexLabel = iterLayers;
			for (i = 0; i < top; i++) {
				peto = region.getInt(indexLabel);
				peto = temp1[labeling[peto]];
				region.setInt(indexLabel, peto);
				imageMean.setDouble(i, image1[i]);
				indexLabel += region.getNumBands();
			}

			maxpetox = count + 1;
			itera += 1;
			System.out.println("Test:\t itera=" + itera + "\t change=" + change);
		}

		// update the max number of regions
		_NFoundS = count;

		// update the avg. image values
		for (i = 1; i < maxpetox; i++)
			suma[i] = 0.0;

		indexLabel = iterLayers;

		for (i = 0; i < top; i++) {
			suma[region.getInt(indexLabel)] += imgprocessed.getDouble(i);
			indexLabel += region.getNumBands();
		}

		for (i = 1; i < maxpetox; i++) {
			if (newsize[i] != 0)
				imageS[i] = suma[i] / newsize[i];
			else {
				System.out.println("ERROR at suma[" + i + "]= " + suma[i]);
				imageS[i] = 0;
			}
		}

		indexLabel = iterLayers;
		for (i = 0; i < top; i++) {
			imageMean.setDouble(i, imageS[region.getInt(indexLabel)]);
			indexLabel += region.getNumBands();
		}

		if (_debugSeg2DSuper) {
			System.out.println("INFO: after noise removal NFOUNDS=" + _NFoundS);
		}

		hist = null;
		histi1j1 = null;
		histcount = null;
		histattrib = null;

		image1 = null;

		suma = null;
		temp1 = null;
		labeling = null;

		return true;
	}

	/**
	 *
	 * @param maxpetox
	 * @param maxitera
	 * @param minsize
	 * @param imageMean
	 * @param imageS
	 * @param imgprocessed
	 * @param region
	 * @param iterLayers
	 * @param newsize
	 * @return
	 * @deprecated Use noiseRemoval1(int maxpetox, int maxitera, int minsize,
			ImageObject imageMean, double[] imageS, ImageObject imgprocessed,
			ImageObject region, int iterLayers, int[] newsize)
	 */
	public boolean NoiseRemoval1(int maxpetox, int maxitera, int minsize,
			ImageObject imageMean, double[] imageS, ImageObject imgprocessed,
			ImageObject region, int iterLayers, int[] newsize) {
		return noiseRemoval1(maxpetox, maxitera, minsize,
				imageMean, imageS, imgprocessed,
				region, iterLayers, newsize);
	}
}
