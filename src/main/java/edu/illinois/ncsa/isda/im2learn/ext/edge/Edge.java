package edu.illinois.ncsa.isda.im2learn.ext.edge;

/*
 * Edge.java
 *
 */

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageException;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.im2learn.core.datatype.LimitValues;
import edu.illinois.ncsa.isda.im2learn.ext.math.GeomOper;


/*
 * import SubArea; import ImageObject; import LimitValues;
 */

/**
 * this class is not public !!!!!!
 * 
 * @author Peter Bajcsy
 * @version 1.0
 * 
 */

class EdgeObject {
	// Edge related data
	protected float	_edgeMaxVal, _edgeMinVal;

	// data related values
	protected float	_MinDataVal;
	protected float	_MaxDataVal;
	protected float	_threshEdgeIm;

	ImageObject		_res	= null;
	// classes
	LimitValues		_lim	= new LimitValues();
	//GeomObject _myGeom = new GeomObject();
	GeomOper		_myGeom	= new GeomOper();

	public EdgeObject() {
		ResetEdgeObject();
		// default values
	}

	public void ResetEdgeObject() {
		// default values
		_edgeMinVal = 0.0F;
		_edgeMaxVal = 255.0F;
		_threshEdgeIm = 0.0F;
		_res = null;
	}

	//Getters
	// Data values
	public float GetEdgeMinVal() {
		return _edgeMinVal;
	}

	public float GetEdgeMaxVal() {
		return _edgeMaxVal;
	}

	public ImageObject GetEdgeImageObject() {
		return _res;
	}

}

/**
 * <B> The class Edge provides a tool for computing edge pixels of
 * two-dimensional multivariate (multi-band) images. </B> <BR>
 * <BR>
 * <B>Description:</B> <BR>
 * <BR>
 * <B>Run:</B> <BR>
 * <B> Release notes: </B> <BR>
 * The current release does not save out any text of the Edge plots in the "Save
 * As Image" mode.
 * 
 * @author Peter Bajcsy
 * @version 1.0
 * 
 */

////////////////////////////////////////////////////////////////////
// this class performs Edge computation
// and statistical evaluation of Edge data
// This is a Edge of a byte array !!
////////////////////////////////////////////////////////////////////
public class Edge extends EdgeObject {

	private boolean		_debugEdge;
	private static Log	logger	= LogFactory.getLog(Edge.class);

	//constructor
	public void Edge() {
		_debugEdge = true;
	}

	// Getters
	public EdgeObject GetEdgeObject() {
		return this;
	}

	//Setters
	public void SetDebugFlag(boolean flag) {
		_debugEdge = flag;
	}

	//////////////////////////////////////////////////////////////
	// doers

	/**
	 * This method detect all pixels that have any value change in a four-pixel
	 * neighborhood
	 * 
	 * @return one band BYTE image with white pixels of the edges and other
	 *         pixels are black
	 */
	public ImageObject EdgeForLocalDifference(ImageObject im) {

		//sanity check
		if ((im == null) || (im.getNumRows() <= 0) || (im.getNumCols() <= 0)) {
			System.out.println("Error: no image \n");
			return null;
		}

		// init the output local difference image
		ImageObject resultImage = null;
		try {
			resultImage = ImageObject.createImage(im.getNumRows(), im.getNumCols(), 1, ImageObject.TYPE_BYTE);
		} catch (ImageException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		int index, indexOneRow;
		int i, j;

		resultImage.setData(_lim.BIN_ZERO);
		//index = (resultImage.getNumCols() + 1) * resultImage.getNumBands();
		indexOneRow = im.getNumCols() * im.getNumBands();

		index = 0;
		if (im.getNumBands() == 1) {
			for (i = 0; i < im.getNumRows(); i++) {
				for (j = 0; j < im.getNumCols(); j++) {
					if (((j > 0) && (im.getDouble(index) != im.getDouble(index - 1)))) {
						resultImage.set(index, _lim.BIN_ONE);
					} else {
						if ((j < im.getNumCols() - 1) && (im.getDouble(index) != im.getDouble(index + 1))) {
							resultImage.set(index, _lim.BIN_ONE);
						} else {
							if ((i > 0) && (im.getDouble(index) != im.getDouble(index - indexOneRow))) {
								resultImage.set(index, _lim.BIN_ONE);
							} else {
								if ((i < im.getNumRows() - 1) && (im.getDouble(index) != im.getDouble(index + indexOneRow))) {
									resultImage.set(index, _lim.BIN_ONE);
								}
							}
						}
					}
					index++;
				}
			}
		} else {
			logger.debug("not supported multiple band images");
			return null;

		}
		return resultImage;

	}

	// Edge computation
	/**
	 * This method generates two band image of type Float with the magnitude and
	 * orientation of Sobel edge detector the output 2-band image can be used as
	 * a visualization of edges using embossment
	 * 
	 * @param im -
	 *            input image
	 * @return boolean outcome of the computation The result is obtained by
	 *         calling a getter
	 */

	public boolean Sobel(ImageObject im) {
		//sanity check
		if ((im == null) || (im.getNumRows() <= 0) || (im.getNumCols() <= 0)) {
			System.out.println("Error: no image \n");
			return false;
		}

		// init the output edge image
		try {
			_res = ImageObject.createImage(im.getNumRows(), im.getNumCols(), 2, ImageObject.TYPE_FLOAT);
		} catch (ImageException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		int index, indexIm;
		int offset = (im.getNumCols() + 1) * im.getNumBands();
		int offset1 = (im.getNumCols() - 1) * im.getNumBands();
		int offsetCenter = im.getNumCols() * im.getNumBands();

		int offsetIndex = _res.getNumBands() << 1;
		int offsetIndexIm = im.getNumBands() << 1;
		int i, j, k;
		double val, D1, D2;

		// define offsets

		ImageObject vec1 = null;
		try {
			vec1 = ImageObject.createImage(1, 1, im.getNumBands(), ImageObject.TYPE_FLOAT);
		} catch (ImageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ImageObject vec2 = null;
		try {
			vec2 = ImageObject.createImage(1, 1, im.getNumBands(), ImageObject.TYPE_FLOAT);
		} catch (ImageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		index = (_res.getNumCols() + 1) * _res.getNumBands();
		indexIm = (im.getNumCols() + 1) * im.getNumBands();
		if (im.getNumBands() == 1) {
			for (i = 1; i < im.getNumRows() - 1; i++) {
				for (j = 1; j < im.getNumCols() - 1; j++) {
					val = im.getDouble(indexIm - offset) + (im.getDouble(indexIm - 1) * 2) + im.getDouble(indexIm + offset1);
					D1 = im.getDouble(indexIm - offset1) + (im.getDouble(indexIm + 1) * 2) + im.getDouble(indexIm + offset);
					D1 -= val;
					val = im.getDouble(indexIm - offset) + (im.getDouble(indexIm - im.getNumCols()) * 2) + im.getDouble(indexIm - offset1);
					D2 = im.getDouble(indexIm + offset1) + (im.getDouble(indexIm + im.getNumCols()) * 2) + im.getDouble(indexIm + offset);
					D2 -= val;
					indexIm++;
					//magnitude
					val = Math.sqrt(D1 * D1 + D2 * D2);
					_res.set(index, (float) val);
					index++;
					// direction
					_res.set(index, (float) Math.atan2(D2, D1));
					index++;
				}
				index += offsetIndex;
				indexIm += offsetIndexIm;
			}
			// end of if(sampPerPixel == 1)
		} else {

			vec1.setData(0);
			vec2.setData(0);

			for (i = 1; i < im.getNumRows() - 1; i++) {
				for (j = 1; j < im.getNumCols() - 1; j++) {
					for (k = 0; k < im.getNumBands(); k++) {
						vec1.set(k, (float) ((im.getDouble(indexIm + k - offset) + (im.getDouble(indexIm + k - 1) * 2) + im.getDouble(indexIm + k + offset1))));
						vec2.set(k, (float) ((im.getDouble(indexIm + k - offset1) + (im.getDouble(indexIm + k + 1) * 2) + im.getDouble(indexIm + k + offset))));
					}
					D1 = _myGeom.euclidDist(im.getNumBands(), (float[]) vec1.getData(), 0, (float[]) vec2.getData(), 0);
					for (k = 0; k < im.getNumBands(); k++) {
						vec2.set(k, (float) ((im.getDouble(indexIm + k - offset) + (im.getDouble(indexIm + k - offsetCenter) * 2) + im.getDouble(indexIm + k - offset1))));
						vec2.set(k, (float) ((im.getDouble(indexIm + k + offset1) + (im.getDouble(indexIm + k + offsetCenter) * 2) + im.getDouble(indexIm + k + offset))));
					}
					D2 = _myGeom.euclidDist(im.getNumBands(), (float[]) vec1.getData(), 0, (float[]) vec2.getData(), 0);
					indexIm += im.getNumBands();
					//magnitude
					val = Math.sqrt(D1 * D1 + D2 * D2);
					_res.set(index, (float) val);
					index++;
					// direction
					_res.set(index, (float) Math.atan2(D2, D1));
					index++;
				}
				index += offsetIndex;
				indexIm += offsetIndexIm;
			}
			// end of high dimensional data
		}
		// take care of borders
		_edgeMaxVal = _lim.MIN_FLOAT;
		_edgeMinVal = _lim.MAX_FLOAT;
		float valFloat;
		//find  magnitude max and min values
		for (index = 0; index < _res.getSize(); index += 2) {
			valFloat = _res.getFloat(index);
			if (valFloat > _edgeMaxVal) {
				_edgeMaxVal = valFloat;
			}
			if (valFloat < _edgeMinVal) {
				_edgeMinVal = valFloat;
			}
		}

		int oneRow = _res.getNumCols() * _res.getNumBands();
		//first row
		for (index = 0; index < oneRow; index += _res.getNumBands()) {
			_res.set(index, _edgeMinVal);
			_res.set(index + 1, 0);
		}
		//last row
		for (index = _res.getSize() - oneRow; index < _res.getSize(); index += _res.getNumBands()) {
			_res.set(index, _edgeMinVal);
			_res.set(index + 1, 0);
		}
		//first col
		for (index = oneRow; index < _res.getSize() - oneRow; index += oneRow) {
			_res.set(index, _edgeMinVal);
			_res.set(index + 1, 0);
		}
		//last col
		for (index = oneRow - 1; index < _res.getSize() - oneRow; index += oneRow) {
			_res.set(index, _edgeMinVal);
			_res.set(index + 1, 0);
		}

		return true;

	}

	/**
	 * This method generates one band image of type Float with the magnitude of
	 * Sobel edge detector
	 * 
	 * @param im -
	 *            input image
	 * @return boolean outcome of the computation The result is obtained by
	 *         calling a getter
	 */
	public boolean SobelMag(ImageObject im) {

		//sanity check
		if ((im == null) || (im.getNumRows() <= 0) || (im.getNumCols() <= 0)) {
			System.out.println("Error: no image \n");
			return false;
		}

		// init the output edge image
		try {
			_res = ImageObject.createImage(im.getNumRows(), im.getNumCols(), 1, ImageObject.TYPE_FLOAT);
		} catch (ImageException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		int index, indexIm;
		int offset = (im.getNumCols() + 1) * im.getNumBands();
		int offset1 = (im.getNumCols() - 1) * im.getNumBands();
		int offsetCenter = im.getNumCols() * im.getNumBands();

		int offsetIndex = _res.getNumBands() << 1;
		int offsetIndexIm = im.getNumBands() << 1;
		int i, j, k;
		double val, D1, D2;

		// define offsets

		ImageObject vec1 = null;
		try {
			vec1 = ImageObject.createImage(1, 1, im.getNumBands(), ImageObject.TYPE_FLOAT);
		} catch (ImageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ImageObject vec2 = null;
		try {
			vec2 = ImageObject.createImage(1, 1, im.getNumBands(), ImageObject.TYPE_FLOAT);
		} catch (ImageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		index = (_res.getNumCols() + 1) * _res.getNumBands();
		indexIm = (im.getNumCols() + 1) * im.getNumBands();
		if (im.getNumBands() == 1) {
			for (i = 1; i < im.getNumRows() - 1; i++) {
				for (j = 1; j < im.getNumCols() - 1; j++) {
					val = im.getDouble(indexIm - offset) + (im.getDouble(indexIm - 1) * 2) + im.getDouble(indexIm + offset1);
					D1 = im.getDouble(indexIm - offset1) + (im.getDouble(indexIm + 1) * 2) + im.getDouble(indexIm + offset);
					D1 -= val;
					val = im.getDouble(indexIm - offset) + (im.getDouble(indexIm - im.getNumCols()) * 2) + im.getDouble(indexIm - offset1);
					D2 = im.getDouble(indexIm + offset1) + (im.getDouble(indexIm + im.getNumCols()) * 2) + im.getDouble(indexIm + offset);
					D2 -= val;
					indexIm++;
					//magnitude
					val = Math.sqrt(D1 * D1 + D2 * D2);
					_res.set(index, (float) val);
					index++;
					/*
					 * // direction _res.set(index, (float) Math.atan2(D2, D1));
					 * index++;
					 */
				}
				index += offsetIndex;
				indexIm += offsetIndexIm;
			}
			// end of if(sampPerPixel == 1)
		} else {

			vec1.setData(0);
			vec2.setData(0);

			for (i = 1; i < im.getNumRows() - 1; i++) {
				for (j = 1; j < im.getNumCols() - 1; j++) {
					for (k = 0; k < im.getNumBands(); k++) {
						vec1.set(k, (float) ((im.getDouble(indexIm + k - offset) + (im.getDouble(indexIm + k - 1) * 2) + im.getDouble(indexIm + k + offset1))));
						vec2.set(k, (float) ((im.getDouble(indexIm + k - offset1) + (im.getDouble(indexIm + k + 1) * 2) + im.getDouble(indexIm + k + offset))));
					}
					D1 = _myGeom.euclidDist(im.getNumBands(), (float[]) vec1.getData(), 0, (float[]) vec2.getData(), 0);
					for (k = 0; k < im.getNumBands(); k++) {
						vec1.set(k, (float) ((im.getDouble(indexIm + k - offset) + (im.getDouble(indexIm + k - offsetCenter) * 2) + im.getDouble(indexIm + k - offset1))));
						vec2.set(k, (float) ((im.getDouble(indexIm + k + offset1) + (im.getDouble(indexIm + k + offsetCenter) * 2) + im.getDouble(indexIm + k + offset))));
					}
					D2 = _myGeom.euclidDist(im.getNumBands(), (float[]) vec1.getData(), 0, (float[]) vec2.getData(), 0);
					indexIm += im.getNumBands();
					//magnitude
					val = Math.sqrt(D1 * D1 + D2 * D2);
					_res.set(index, (float) val);
					index++;
/*
 * // direction _res.set(index, (float) Math.atan2(D2, D1)); index++;
 */
				}
				index += offsetIndex;
				indexIm += offsetIndexIm;
			}
			// end of high dimensional data
		}
		// take care of borders
		_edgeMaxVal = _lim.MIN_FLOAT;
		_edgeMinVal = _lim.MAX_FLOAT;
		float valFloat;
		//find  magnitude max and min values
		for (index = 0; index < _res.getSize(); index += 2) {
			valFloat = _res.getFloat(index);
			if (valFloat > _edgeMaxVal) {
				_edgeMaxVal = valFloat;
			}
			if (valFloat < _edgeMinVal) {
				_edgeMinVal = valFloat;
			}
		}

		int oneRow = _res.getNumCols() * _res.getNumBands();
		//first row
		for (index = 0; index < oneRow; index += _res.getNumBands()) {
			_res.set(index, _edgeMinVal);
			//_res.set(index + 1, 0);
		}
		//last row
		for (index = _res.getSize() - oneRow; index < _res.getSize(); index += _res.getNumBands()) {
			_res.set(index, _edgeMinVal);
			//_res.set(index + 1, 0);
		}
		//first col
		for (index = oneRow; index < _res.getSize() - oneRow; index += oneRow) {
			_res.set(index, _edgeMinVal);
			//_res.set(index + 1, 0);
		}
		//last col
		for (index = oneRow - 1; index < _res.getSize() - oneRow; index += oneRow) {
			_res.set(index, _edgeMinVal);
			//_res.set(index + 1, 0);
		}

		return true;

	}

	////////////////////////////////////////////////////////////////////////////
	// Edge computation
	// Variation of Roberts operator
	// computes only magnitude
	public boolean RobertsVar(ImageObject im) {
		//sanity check
		if ((im == null) || (im.getNumRows() <= 0) || (im.getNumCols() <= 0)) {
			System.out.println("Error: no image \n");
			return false;
		}
		// init the output edge image
		try {
			_res = ImageObject.createImage(im.getNumRows(), im.getNumCols(), 1, ImageObject.TYPE_FLOAT);
		} catch (ImageException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// define offsets
		int index, indexIm1, indexIm2, indexIm3, indexIm4;
		int offsetIndex = 2;//_res.getNumBands()<<1;
		int offsetIndexIm = im.getNumBands() << 1;
		int i, j, k;
		double val, D1, D2, drow, dcol;

		ImageObject vec1 = null;
		try {
			vec1 = ImageObject.createImage(1, 1, im.getNumBands(), ImageObject.TYPE_FLOAT);
		} catch (ImageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ImageObject vec2 = null;
		try {
			vec2 = ImageObject.createImage(1, 1, im.getNumBands(), ImageObject.TYPE_FLOAT);
		} catch (ImageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		index = (_res.getNumCols() + 1);//*_res.getNumBands();
		indexIm1 = 0;
		indexIm2 = im.getNumBands() << 1;
		i = im.getNumBands() * (im.getNumCols() << 1);
		indexIm3 = i;
		indexIm4 = i + indexIm2;

		if (im.getNumBands() == 1) {
			for (i = 2; i < im.getNumRows(); i++) {
				for (j = 2; j < im.getNumCols(); j++) {
					D1 = im.getDouble(indexIm4) - im.getDouble(indexIm1);
					D2 = im.getDouble(indexIm3) - im.getDouble(indexIm2);
					indexIm1++;
					indexIm2++;
					indexIm3++;
					indexIm4++;
					//magnitude
					drow = D1 + D2;
					dcol = D1 - D2;
					_res.set(index, (float) Math.sqrt(drow * drow + dcol * dcol));
					index++;
				}
				index += offsetIndex;
				indexIm1 += offsetIndexIm;
				indexIm2 += offsetIndexIm;
				indexIm3 += offsetIndexIm;
				indexIm4 += offsetIndexIm;
			}
			// end of if(sampPerPixel == 1)
		} else {

			for (i = 2; i < im.getNumRows(); i++) {
				for (j = 2; j < im.getNumCols(); j++) {
					for (k = 0; k < im.getNumBands(); k++) {
						vec1.set(k, (float) (im.getDouble(indexIm4) - im.getDouble(indexIm1)));
						vec2.set(k, (float) (im.getDouble(indexIm3) - im.getDouble(indexIm2)));
						indexIm1++;
						indexIm2++;
						indexIm3++;
						indexIm4++;
					}
					// vec 1 - vec2
					dcol = _myGeom.euclidDist(im.getNumBands(), (float[]) vec1.getData(), 0, (float[]) vec2.getData(), 0);
					// vec1 + vec2
					for (k = 0; k < vec1.getNumBands(); k++) {
						vec1.set(k, vec1.getFloat(k) + vec2.getFloat(k));
					}
					drow = _myGeom.vectorMag(im.getNumBands(), (float[]) vec1.getData(), 0);
					//magnitude
					_res.set(index, (float) Math.sqrt(drow * drow + dcol * dcol));
					;
					index++;
				}
				index += offsetIndex;
				indexIm1 += offsetIndexIm;
				indexIm2 += offsetIndexIm;
				indexIm3 += offsetIndexIm;
				indexIm4 += offsetIndexIm;
			}
			// end of high dimensional data
		}
		// take care of borders
		_edgeMaxVal = _lim.MIN_FLOAT;
		_edgeMinVal = _lim.MAX_FLOAT;
		float valFloat;
		//find  magnitude max and min values
		for (index = 0; index < _res.getSize(); index++) {
			valFloat = _res.getFloat(index);
			if (valFloat > _edgeMaxVal) {
				_edgeMaxVal = valFloat;
			}
			if (valFloat < _edgeMinVal) {
				_edgeMinVal = valFloat;
			}
		}

		int oneRow = _res.getNumCols();//*_res.getNumBands();
		//first row
		for (index = 0; index < oneRow; index += _res.getNumBands()) {
			_res.set(index, _edgeMinVal);
		}
		//last row
		for (index = _res.getSize() - oneRow; index < _res.getSize(); index += _res.getNumBands()) {
			_res.set(index, _edgeMinVal);
		}
		//first col
		for (index = oneRow; index < _res.getSize() - oneRow; index += oneRow) {
			_res.set(index, _edgeMinVal);
		}
		//last col
		for (index = oneRow - 1; index < _res.getSize() - oneRow; index += oneRow) {
			_res.set(index, _edgeMinVal);
		}

		return true;

	}//end of Roberts1

	/////////////////////////////////////////////////////////////////////
	// this function estimates the threshold that will give the maxPts
	// edge points with the largest magnitude
	// it should be improved by hard coding a table of Gauss cummulative pdf.
	///////////////////////////////////////////////////////////////
	public float FindThreshEdge(ImageObject edgeIm, int maxPts) {
		if ((edgeIm == null) || (edgeIm.getType() != ImageObject.TYPE_FLOAT)) {
			System.out.println(" Error: only FLOAT edgeImage data are supported");
			return 0.0F;
		}

		int idx, numpts;
		float val, thresh;
		double mean, stdev, sum, sum2;
		float minVal, maxVal;
		minVal = _lim.MAX_FLOAT;
		maxVal = _lim.MIN_FLOAT;
		sum = 0.0;
		sum2 = 0.0;
		for (idx = 0; idx < edgeIm.getSize(); idx += edgeIm.getNumBands()) {
			val = edgeIm.getFloat(idx);
			sum += val;
			sum2 += val * val;
			if (minVal > val) {
				minVal = val;
			}
			if (maxVal < val) {
				maxVal = val;
			}
		}
		numpts = edgeIm.getNumRows() * edgeIm.getNumCols();
		mean = sum / numpts;
		stdev = Math.sqrt(sum2 / numpts - mean * mean);
		//System.out.println("Info: mean = " + mean + " stdev = "+ stdev);

		double percentile = (double) maxPts / numpts;
		//test
		System.out.println("Info: percentile = " + percentile);
		double z = 0.0;
		if (percentile < 0.9) {
			z = _lim.findGaussPr((1.0 - percentile));
			if ((1.0 - percentile) < 0.5) {
				z = -z;
			}
			z *= 0.9;
			thresh = (float) (z * stdev + mean);
			//this does not work
			//thresh = (float)( (maxVal - minVal)*percentile + minVal);
			System.out.println("Info: final thresh = " + thresh);
		} else {
			thresh = minVal - 1.0F;
			System.out.println("Info: minVal thresh = " + thresh);
		}

		System.out.println("Info: percentile = " + (1.0 - percentile) + " z=" + z);
		// System.out.println("Info: final thresh = " + thresh);

		/*
		 * // find the real number of pts numpts = 0; for(idx=0;idx<edgeIm.getSize();idx+=
		 * edgeim.getNumBands()){ if(edgeIm.imageFloat[idx] > thresh) numpts ++; } //
		 * cannot do correction anymore if(numpts < maxPts){
		 * System.out.println("Info: thresh correction: numpts > thresh is " +
		 * numpts + " requested =" + maxPts); System.out.println("Info: mult " +
		 * z + " mean =" + mean +" stdev=" +stdev); // TODO z *= 0.9; thresh =
		 * (float)( z*stdev + mean); System.out.println("Info: final thresh = " +
		 * thresh); }
		 */

		return thresh;
	}

	////////////////////////////////////////////////////////////////////////////
	// this method creats a list of points = (row,col,vec(x,y)) from an image.
	// it is used by SchDerModel
	////////////////////////////////////////////////////////////////////////////
	public ImageObject CreateEdgeList(ImageObject im, int maxPts) {
		// sanity check
		if (maxPts <= 0) {
			System.out.println("Error: maxPts <=0 ");
			return null;
		}
		// compute edge image
		if (!RobertsVar(im)) {
			System.out.println("Error: could not find edges");
			return null;
		}
		// check resulting edge image
		if (_res == null) {
			System.out.println("ERROR: null edge ImageObject");
			return null;
		}
		// compute thresh
		float thresh;
		thresh = FindThreshEdge(_res, maxPts);
		_threshEdgeIm = thresh;

		ImageObject edgeList = null;
		edgeList = EdgeIm2List(im, _res, thresh);
		if (edgeList == null) {
			System.out.println("ERROR: could not compute edgeList");
			return null;
		}

		return (edgeList);
	}

	////////////////////////////////////////////////////////////////////////////
	// This method updates the edge list but preserves locations
	public boolean UpdateEdgeList(ImageObject im, ImageObject edgeList) {
		// sanity check
		if ((im == null) || (edgeList == null)) {
			System.out.println("ERROR: missing input data");
			return false;
		}
/*
 * if ((im.sampType.equalsIgnoreCase("BYTE") == false) &&
 * (im.sampType.equalsIgnoreCase("SHORT") == false)) { System.out.println("
 * Error: only BYTE or SHORT Input data are supported"); return false; }
 */		if (im.getNumBands() != (edgeList.getNumBands() - 2)) {
			System.out.println("ERROR: input data dimension does not match model dimension");
			return false;
		}

		int i, j, idx;
		for (i = 0; i < edgeList.getSize(); i += edgeList.getNumBands()) {
			// if row, col are inside of image area
			if ((edgeList.getInt(i) >= 0) && (edgeList.getInt(i) < im.getNumRows()) && (edgeList.getInt(i + 1) >= 0) && (edgeList.getInt(i + 1) < im.getNumCols())) {
				idx = (edgeList.getInt(i) * im.getNumCols() + edgeList.getInt(i + 1)) * im.getNumBands();
				for (j = 0; j < im.getNumBands(); j++) {
					edgeList.set((i + j + 2), im.getDouble(idx + j));
				}
			} else {
				System.out.println("ERROR: could not update (" + edgeList.getDouble(i) + ", " + edgeList.getDouble(i + 1) + ")");
			}
		}

/*
 * if (im.sampType.equalsIgnoreCase("BYTE")) { for (i = 0; i <
 * edgeList.getSize(); i += edgeList.getNumBands()) { // if row, col are inside
 * of image area if ((edgeList.imageInt[i] >= 0) && (edgeList.imageInt[i] <
 * im.getNumRows()) && (edgeList.imageInt[i + 1] >= 0) && (edgeList.imageInt[i +
 * 1] < im.getNumCols())) { idx = (edgeList.imageInt[i] * im.getNumCols() +
 * edgeList.imageInt[i + 1]) * im.getNumBands(); for (j = 0; j <
 * im.getNumBands(); j++) { edgeList.imageInt[i + j + 2] = im.image[idx + j]; } }
 * else { System.out.println("ERROR: could not update (" + edgeList.imageInt[i] + ", " +
 * edgeList.imageInt[i + 1] + ")"); } } }// end of BYTE if
 * (im.sampType.equalsIgnoreCase("SHORT")) { for (i = 0; i < edgeList.getSize();
 * i += edgeList.getNumBands()) { // if row, col are inside of image area if
 * ((edgeList.imageInt[i] >= 0) && (edgeList.imageInt[i] < im.getNumRows()) &&
 * (edgeList.imageInt[i + 1] >= 0) && (edgeList.imageInt[i + 1] <
 * im.getNumCols())) { idx = (edgeList.imageInt[i] * im.getNumCols() +
 * edgeList.imageInt[i + 1]) * im.getNumBands(); for (j = 0; j <
 * im.getNumBands(); j++) { edgeList.imageInt[i + j + 2] = im.imageShort[idx +
 * j]; } } else { System.out.println("ERROR: could not update (" +
 * edgeList.imageInt[i] + ", " + edgeList.imageInt[i + 1] + ")"); } } }// end of
 * SHORT
 */
		return true;
	}

	////////////////////////////////////////////////////////////////////////////
	// this method thresholds an edge image into a binary edge map, then converts the
	// the edge pixels into a list of points = (row,col,vec(x,y)). If the maxPts >
	// number of edge points then a subsampling takes place.
	////////////////////////////////////////////////////////////////////////////
	private ImageObject EdgeIm2List(ImageObject im, ImageObject edgeIm, float thresh) {
		if ((edgeIm == null) || (edgeIm.getType() != ImageObject.TYPE_FLOAT)) {
			System.out.println(" Error: only FLOAT edgeImage data are supported");
			return null;
		}
/*
 * if ((im.sampType.equalsIgnoreCase("BYTE") == false) &&
 * (im.sampType.equalsIgnoreCase("SHORT") == false)) { System.out.println("
 * Error: only BYTE or SHORT Input data are supported"); return null; }
 */		if ((im.getNumRows() != edgeIm.getNumRows()) || (im.getNumCols() != edgeIm.getNumCols())) {
			System.out.println(" Error: image and egde image have different dimensions");
			return null;
		}

		int idxIm = 0, idxRes = 0, idx = 0;
		int row, col, k;
		int numPts = 0; // number of final edge points in the list

		numPts = 0;
		for (idx = 0; idx < edgeIm.getSize(); idx += edgeIm.getNumBands()) {
			if (edgeIm.getFloat(idx) > thresh) {
				numPts++;
			}
		}
		//test
		System.out.println("Test: numpts > thresh is " + numPts + " thresh =" + thresh);

		// currently only byte, short and int images can be converted
		// into an edge list !!
		ImageObject res = null;
		try {
			res = ImageObject.createImage(1, numPts, 2 + im.getNumBands(), ImageObject.TYPE_DOUBLE);
		} catch (ImageException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
/*
 * ImageObject res = new ImageObject(); res.getNumRows() = 1; res.getNumCols() =
 * numPts; res.getNumBands() = 2 + im.getNumBands(); res.sampType = "INT";
 * res.getSize() = res.getNumCols() * res.getNumBands(); res.imageInt = new
 * int[(int) res.getSize()];
 */		//test
		//System.out.println("Test: edge list object");
		//res.PrintImageObject();
		idxIm = 0;
		idxRes = 0;
		idx = 0;
		numPts = 0;

		// the first value in edgeIm is assumed to be the magnitude value
		for (row = 0; row < edgeIm.getNumRows(); row++) {
			for (col = 0; col < edgeIm.getNumCols(); col++) {
				if (edgeIm.getFloat(idx) > thresh) {
					/*
					 * if(idxRes+res.getNumBands()> res.getSize()){
					 * System.out.println("ERROR: idxRes is out of bounds: row
					 * ="+row+", col="+col); System.out.println("ERROR:
					 * idxRes="+idxRes+" maxidx="+res.getSize()+"
					 * idxSub="+idxSub); System.out.println("ERROR:
					 * numpts="+numPts); break; }
					 */
					res.set(idxRes, row);
					res.set(idxRes + 1, col);
					idxRes += 2;
					for (k = 0; k < im.getNumBands(); k++) {
						res.set(idxRes, im.getDouble(idxIm + k));
						idxRes++;
					}
					numPts++;
					/*
					 * if(numPts > maxPts){ System.out.println("Info: there are
					 * more edge pts than allowed dim");
					 * System.out.println("Info: stop at row = " + row + " col = " +
					 * col); row = edgeIm.getNumRows(); col =
					 * edgeim.getNumCols(); break; }
					 */
				}// end if(edgeIm > thresh)
				idx += edgeIm.getNumBands();
				idxIm += im.getNumBands();
			}// end of for(col)
		}// end of for(row)

/*
 * if (im.sampType.equalsIgnoreCase("BYTE")) { // the first value in edgeIm is
 * assumed to be the magnitude value for (row = 0; row < edgeIm.getNumRows();
 * row++) { for (col = 0; col < edgeim.getNumCols(); col++) { if
 * (edgeIm.imageFloat[idx] > thresh) {
 * 
 * if(idxRes+res.getNumBands()> res.getSize()){ System.out.println("ERROR:
 * idxRes is out of bounds: row ="+row+", col="+col); System.out.println("ERROR:
 * idxRes="+idxRes+" maxidx="+res.getSize()+" idxSub="+idxSub);
 * System.out.println("ERROR: numpts="+numPts); break; }
 * 
 * res.imageInt[idxRes] = row; res.imageInt[idxRes + 1] = col; idxRes += 2; for
 * (k = 0; k < im.getNumBands(); k++) { res.imageInt[idxRes] = im.image[idxIm +
 * k]; idxRes++; } numPts++;
 * 
 * if(numPts > maxPts){ System.out.println("Info: there are more edge pts than
 * allowed dim"); System.out.println("Info: stop at row = " + row + " col = " +
 * col); row = edgeIm.getNumRows(); col = edgeim.getNumCols(); break; }
 * 
 * }// end if(edgeIm > thresh) idx += edgeim.getNumBands(); idxIm +=
 * im.getNumBands(); }// end of for(col) }// end of for(row) }// end BYTE if
 * (im.sampType.equalsIgnoreCase("SHORT")) { // the first value in edgeIm is
 * assumed to be the magnitude value for (row = 0; row < edgeIm.getNumRows();
 * row++) { for (col = 0; col < edgeim.getNumCols(); col++) { if
 * (edgeIm.imageFloat[idx] > thresh) { res.imageInt[idxRes] = row;
 * res.imageInt[idxRes + 1] = col; idxRes += 2; for (k = 0; k <
 * im.getNumBands(); k++) { res.imageInt[idxRes] = im.imageShort[idxIm + k];
 * idxRes++; } numPts++; }// end if(edgeIm > thresh) idx +=
 * edgeim.getNumBands(); idxIm += im.getNumBands(); }// end of for(col) }// end
 * of for(row) }// end Short
 */
		//test
		System.out.println("Test: number of edge pts=" + numPts);

		return (res);
	}

	// test utility that allows to display an edge list as an image
	public ImageObject EdgeList2Image(ImageObject edgeList, int numrows, int numcols) {
		// sanity check
		if ((edgeList == null) || (edgeList.getType() != ImageObject.TYPE_DOUBLE)) {
			System.out.println("Error: only DOUBLE edge list data is supported");
			return null;
		}

		ImageObject res = null;
		try {
			res = ImageObject.createImage(numrows, numcols, 1, ImageObject.TYPE_BYTE);
		} catch (ImageException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
/*
 * ImageObject res = new ImageObject(); res.getNumRows() = numrows;
 * res.getNumCols() = numcols; res.getNumBands() = 1; res.sampType = "BYTE";
 * res.getSize() = res.getNumRows() * res.getNumCols() * res.getNumBands();
 * res.image = new byte[res.getSize()];
 */

		int idx, idx1;
		int row, col;
		// init the image
		for (idx = 0; idx < res.getSize(); idx++) {
			res.set(idx, _lim.BIN_ZERO);
		}
		// assign one to the pixels in the edge list
		for (idx = 0; idx < edgeList.getSize(); idx += edgeList.getNumBands()) {
			row = (int) edgeList.getDouble(idx);
			col = (int) edgeList.getDouble(idx + 1);

			//if(row>=9 ){
			//   System.out.println("Value 2 show in the list: row ="+row+", col="+col);
			//}

			idx1 = row * res.getNumCols() + col;
			//test
			//if(_res.imageFloat[idx1] <= _threshEdgeIm){
			//   System.out.println("ERROR: edge point (row,col)="+row+","+col+")="+_res.imageFloat[idx1]+" is below thresh="+_threshEdgeIm);
			//}

			if ((idx1 >= 0) && (idx1 < res.getSize())) {
				res.set(idx1, _lim.BIN_ONE);
			} else {
				System.out.println("ERROR: edge point (row,col)=" + row + "," + col + ") is out of (numrows,numcols)=" + numrows + "," + numcols + ")");
			}

		}

		return (res);

	}

}
