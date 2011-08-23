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
package edu.illinois.ncsa.isda.imagetools.ext.statistics;

/*
 * ImStats.java
 * 
 * 
 */

import java.io.Serializable;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImLine;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImPoint;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageException;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.imagetools.core.datatype.LimitValues;
import edu.illinois.ncsa.isda.imagetools.ext.math.GeomOper;

//import GeomObject;
/**
 * <B> The class ImStats provides a tool for computing central moments and
 * estimating a type of parametric probability distribution function to model
 * the input image data. </B> <BR>
 * <BR>
 * <B>Description:</B> There are six parameters computed in this class. Four of
 * the parameters are central moments, such as, mean, standard deviation, skew
 * and kurtosis. Two additional parameters, minimum and maximum values, are
 * useful indicators of a dynamic range per image band. The third and fourth
 * central moments are used to estimate an appropriate type of a parametric
 * distribution function for modeling purposes. <BR>
 * 
 * <img src="../../../../../../images/imStatsDialogMean.jpg" width="398"
 * height="396">
 * 
 * <img src="../../../../../../images/imStatsDialog.jpg" width="397"
 * height="400">
 * 
 * <img src="../../../../../../images/imStatsMeanPlot.jpg" width="605"
 * height="444">
 * 
 * <BR>
 * <B>Run:</B> Select "Min and Max Val" from a choice list to explore dynamic
 * range of bands. Click "Compute" or "ComputeMask" (if a mask should be used)
 * to perform the computation. The button "List" displays the values in the text
 * area labeled as Results. The button "ShowPlot" creates a new frame that
 * illustrates all values as a function of band index. <BR>
 * The same procedure can be applied when the StatsType list is set to "Mean and
 * Stdev Val". The visual display shows the mean, mean + stdev and mean - stdev
 * values per each band. <BR>
 * The visual output for the case of "Skew and Kurtosis Val" corresponds to the
 * probability distribution plane that is used for estimating the type of
 * suitable parametric distribution function. The types are displayed in the
 * text area labeled as Results when the button "List" is pressed. The following
 * parametric distribution models are derived from the central moments: Uniform,
 * Normal, Exponential, Beta (U-Shaped) Distribution, Beta (J-Shaped)
 * Distribution, Beta Distribution, Lognormal (Gamma, Weibull) Distribution and
 * Student's t-Distribution. <BR>
 * <BR>
 * <B> Release notes: </B> <BR>
 * The current release does not save out any text of the plots in the "Save As
 * Image" mode.
 * 
 * @author Peter Bajcsy
 * @version 1.0
 * 
 */

///////////////////////////////////////////////////////
// this is the class that computes statistics of images
///////////////////////////////////////////////////////
public class ImStats implements Serializable {

	// these values are vectors although using ImageObject structure !
	private ImageObject	_maxVal;
	private ImageObject	_minVal;

	// images of statistics for one label and many bans
	private double[]	_meanVal;
	private double[]	_stdevVal;
	private double[]	_skewVal;
	private double[]	_kurtosisVal;

	// images of statistics for many labels and many bands
	private float[]		_meanTable;
	private float[]		_stdevTable;
	//private ImageObject _skewTable;
	//private ImageObject _kurtosisTable;
	private long[]		_countTable;

	private int			_numLabelsInLUT;
	private int[]		_labelsInLUT;

	private ImageObject	_maskOrig;
	private long[]		_countSamp;
	private byte		_maskValByte	= -1;
	private int			_maskValInt		= 1;

	private boolean		_isMaskPresent	= false;

	LimitValues			_lim			= new LimitValues();

	private boolean		_debugImStats	= false;							//true;

	private boolean		_allStats		= false;

	// internal variables
	// if more distributions are added then modify these values
	private String[]	_strDist		= null;
	private final int	_numDistTypes	= 9;

	private static Log	logger			= LogFactory.getLog(ImStats.class);

	//constructors
	/**
	 * constructor
	 */
	public ImStats() {
		ResetImStats();
		PopulateStringDistribution();
	}

	/**
	 * initialization
	 */
	private void PopulateStringDistribution() {
		_strDist = new String[_numDistTypes];
		for (int i = 0; i < _numDistTypes; i++) {
			_strDist[i] = new String();
		}
		_strDist[0] = "Uniform Distribution";
		_strDist[1] = "Normal Distribution";
		_strDist[2] = "Exponential Distribution";
		_strDist[3] = "Impossible Distribution";
		_strDist[4] = "Beta (U-Shaped) Distribution";
		_strDist[5] = "Beta (J-Shaped) Distribution";
		_strDist[6] = "Beta Distribution";
		_strDist[7] = "Lognormal (Gamma, Weibull) Distribution";
		_strDist[8] = "Student's t-Distribution";
	}

	//setters and getters
	/**
	 * Setter for Mask computation
	 * 
	 * @param flag
	 *            boolean = is mask present?
	 */
	public void SetIsMaskPresent(boolean flag) {
		_isMaskPresent = flag;
	}

	/**
	 * Set the value of a byte mask
	 * 
	 * @param val
	 *            byte = mask byte value
	 */
	public void SetMaskValByte(byte val) {
		_maskValByte = val;
	}

	/**
	 * Set the value of an int mask
	 * 
	 * @param val
	 *            int = mask int value
	 */
	public void SetMaskValInt(int val) {
		_maskValInt = val;
	}

	/**
	 * Set mask image object
	 * 
	 * @param mask
	 *            ImageObject = predefined mask image object
	 * @throws ImageException
	 * @return boolean = success of operation
	 */
	public boolean SetMaskObject(ImageObject mask) throws ImageException {
		if ((mask == null) || (mask.getData() == null) || ((mask.getType() != ImageObject.TYPE_BYTE) && (mask.getType() != ImageObject.TYPE_INT))) {
			//System.out.println("Error: no mask data");
			//return false;
			throw (new ImageException("Hist Error: no mask data "));
		}
		if (mask.getNumBands() != 1) {
			System.out.println("Error: mask.getNumBands() != 1");
			return false;
		}
		_maskOrig = mask;
		return true;
	}

	/**
	 * Setter for debug flag
	 * 
	 * @param flag
	 *            boolean = debug flag
	 */
	public void SetDebug(boolean flag) {
		_debugImStats = flag;
	}

	/**
	 * Setter for computing all statistics
	 * 
	 * @param flag
	 *            boolean = all stats flag
	 */
	public void SetAllStats(boolean flag) {
		_allStats = flag;
	}

	//getters
	/**
	 * Getter for mask present
	 * 
	 * @return boolean = is mask present?
	 */
	public boolean GetIsMaskPresent() {
		return _isMaskPresent;
	}

	/**
	 * Getter for byte mask value
	 * 
	 * @return byte = mask value
	 */
	public byte GetMaskValByte() {
		return _maskValByte;
	}

	/**
	 * Getter for int mask value
	 * 
	 * @return int = mask value
	 */
	public int GetMaskValInt() {
		return _maskValInt;
	}

	/**
	 * Getter for mask object
	 * 
	 * @return ImageObject = mask object
	 */
	public ImageObject GetMaskObject() {
		return _maskOrig;
	}

	/**
	 * Getter for debug flag
	 * 
	 * @return boolean = debug flag
	 */
	public boolean GetDebug() {
		return _debugImStats;
	}

	/**
	 * Getter for all stats flag
	 * 
	 * @return boolean = all stats flag
	 */
	public boolean GetAllStats() {
		return _allStats;
	}

	/**
	 * Reports how many PDF distributions can be estimated
	 * 
	 * @return int = number of PDF models that can be estimated
	 */
	public int GetNumDistTypes() {
		return _numDistTypes;
	}

	////////////////////////////////////////////////
	/**
	 * Reset method for internal variables
	 */
	public void ResetImStats() {
		_debugImStats = true;// false;
		_minVal = null;
		_maxVal = null;

		_meanVal = null;
		_stdevVal = null;
		_skewVal = null;
		_kurtosisVal = null;

		_meanTable = null;
		_stdevTable = null;
		//_skewTable = null;
		//_kurtosisTable = null;
		_countTable = null;
		_numLabelsInLUT = -1;
		_labelsInLUT = null;

		_allStats = false;

		_maskOrig = null;
		_countSamp = null;
		_maskValByte = _lim.BIN_ONE;
		_maskValInt = 1;
		_isMaskPresent = false;
	}

	//////////////////////////////////////////////////////////////////////
	// Getters
	/**
	 * Getter for Min values per band
	 * 
	 * @return ImageObject = min values ordered according to the input bands
	 *         (type of input data)
	 */
	public ImageObject GetMinVal() {
		return _minVal;
	}

	/**
	 * Getter for Max values per band
	 * 
	 * @return ImageObject = max values ordered according to the input bands
	 *         (type of input data)
	 */
	public ImageObject GetMaxVal() {
		return _maxVal;
	}

	/**
	 * Getter for mean values per band
	 * 
	 * @return double[] = mean values ordered according to the input bands
	 */
	public double[] GetMeanVal() {
		return _meanVal;
	}

	/**
	 * Getter for stdev values per band
	 * 
	 * @return double[] = stdev values ordered according to the input bands
	 */
	public double[] GetStdevVal() {
		return _stdevVal;
	}

	/**
	 * Getter for skew values per band
	 * 
	 * @return double[] = skew values ordered according to the input bands
	 */
	public double[] GetSkewVal() {
		return _skewVal;
	}

	/**
	 * Getter for kurtosis values per band
	 * 
	 * @return double[] = kurtosis values ordered according to the input bands
	 */
	public double[] GetKurtosisVal() {
		return _kurtosisVal;
	}

/////////////////////////////////
	/**
	 * Getter for mean table computed per mask label and per band
	 * 
	 * @return float[] = mean table ordered according to the input band and mask
	 *         label index
	 */
	public float[] GetMeanTable() {
		return _meanTable;
	}

	/**
	 * Getter for stdev table computed per mask label and per band
	 * 
	 * @return float[] = stdev table ordered according to the input band and
	 *         mask label index
	 */
	public float[] GetStdevTable() {
		return _stdevTable;
	}

	/*
	 * public ImageObject GetSkewTable(){ return _skewTable; } public
	 * ImageObject GetKurtosisTable(){ return _kurtosisTable; }
	 */
	/**
	 * Getter for count table computed per mask label (number of pixels labeled
	 * identically)
	 * 
	 * @return long[] = count table ordered according to the input mask label
	 *         index
	 */
	public long[] GetCountTable() {
		return _countTable;
	}

///////////////////////////////
	/**
	 * Deep copy of Min Values
	 * 
	 * @return ImageObject = min values
	 */
	public ImageObject GetCopyMinVal() {
		if (_minVal != null) {
			//test
			//System.out.println("test:_minVal Ovject");
			//_minVal.PrintImageObject();
			ImageObject imo;
			try {
				imo = (ImageObject) _minVal.clone(); //CopyImageObject();
			} catch (Exception e) {
				logger.error("ERROR: _minVal.clone failed");
				return null;
			}
			//test
			//System.out.println("test: copied Object");
			//imo.PrintImageObject();

			return imo;//true;
		} else {
			return null;//false;
		}
	}

	/**
	 * Deep copy of Max Values
	 * 
	 * @return ImageObject = max values
	 */
	public ImageObject GetCopyMaxVal() {
		if (_maxVal != null) {
			ImageObject imo;
			try {
				imo = (ImageObject) _maxVal.clone(); //_maxVal.CopyImageObject();
			} catch (Exception e) {
				logger.error("ERROR: _maxVal.clone failed");
				return null;
			}
			return imo;
		} else {
			return null;
		}
	}

	/**
	 * Deep copy of mean Values
	 * 
	 * @return double [] = mean values
	 */
	public double[] GetCopyMeanVal() {
		if (_meanVal != null) {
			double[] imo;
			imo = _meanVal.clone(); //.clone();//_meanVal.CopyImageObject();
			return imo;
		} else {
			return null;
		}
	}

	/**
	 * Deep copy of stdev Values
	 * 
	 * @return double [] = stdev values
	 */
	public double[] GetCopyStdevVal() {
		if (_stdevVal != null) {
			double[] imo;
			imo = _stdevVal.clone();//_stdevVal.CopyImageObject();
			return imo;
		} else {
			return null;
		}

	}

	/**
	 * Deep copy of skew Values
	 * 
	 * @return double [] = skew values
	 */
	public double[] GetCopySkewVal() {
		if (_skewVal != null) {
			double[] imo;
			imo = _skewVal.clone();//_skewVal.CopyImageObject();
			return imo;
		} else {
			return null;
		}

	}

	/**
	 * Deep copy of kurtosis Values
	 * 
	 * @return double [] = kurtosis values
	 */
	public double[] GetCopyKurtosisVal() {
		if (_kurtosisVal != null) {
			double[] imo;
			imo = _kurtosisVal.clone();//_kurtosisVal.CopyImageObject();
			return imo;
		} else {
			return null;
		}
	}

	/**
	 * Deep copy of mean table per mask label and band
	 * 
	 * @throws CloneNotSupportedException
	 * @return float[] = mean table per band and mask label in that order
	 */
	public float[] GetCopyMeanTable() throws CloneNotSupportedException {
		if (_meanTable != null) {
			float[] imo;
			imo = _meanTable.clone();//_meanTable.CopyImageObject();
			return imo;
		} else {
			return null;
		}
	}

	/**
	 * Deep copy of stdev table per mask label and band
	 * 
	 * @throws CloneNotSupportedException
	 * @return float[] = stdev table per band and mask label in that order
	 */
	public float[] GetCopyStdevTable() throws CloneNotSupportedException {
		if (_stdevTable != null) {
			float[] imo;
			imo = _stdevTable.clone();//_stdevTable.CopyImageObject();
			return imo;
		} else {
			return null;
		}

	}

/*
 * public ImageObject GetCopySkewTable()throws CloneNotSupportedException{
 * if(_skewTable != null){ float [] imo; imo =
 * (ImageObject)_skewTable.clone();//_skewTable.CopyImageObject(); return imo;
 * }else return null; } public ImageObject GetCopyKurtosisTable()throws
 * CloneNotSupportedException{ if(_kurtosisTable != null){ ImageObject imo; imo =
 * (ImageObject)_kurtosisTable.clone();//_kurtosisTable.CopyImageObject();
 * return imo; }else return null; }
 */
	/**
	 * Deep copy of count table per mask label
	 * 
	 * @throws CloneNotSupportedException
	 * @return long[] = mean table per mask label
	 */
	public long[] GetCopyCountTable() throws CloneNotSupportedException {
		if (_countTable != null) {
			long[] imo;
			imo = _countTable.clone();//_countTable.CopyImageObject();
			return imo;
		} else {
			return null;
		}
	}

	//////////////////////////////////////////////////////////////////
	//doers
	////////////////////////////////////////////////
	///
	//////////////////////////////////////////////////
	/**
	 * this method computes statistics of each band without any mask
	 * 
	 * @param imo
	 *            ImageObject = input image object
	 * @return boolean = success of operation
	 */
	public boolean StatsVal(ImageObject imo) {

		SetIsMaskPresent(false);
		boolean answer, ret, local;
		answer = true;
		local = GetAllStats();
		SetAllStats(true);
		ret = MinMaxVal(imo);
		answer = answer && ret;
		if (ret == false) {
			System.out.println("Error: in MinMaxVal()");
			//return false;
		}
		ret = MeanStdevVal(imo);
		answer = answer && ret;
		if (ret == false) {
			System.out.println("Error: in MeanStdevVal()");
			//return false;
		}
		ret = SkewKurtosisVal(imo);
		answer = answer && ret;
		if (ret == false) {
			System.out.println("Error: in SkewKurtosisVal()");
			//return false;
		}

		SetAllStats(local);
		return answer;
	}

	///////////////////////////////////////////////////////////////////////
	/**
	 * computes all stats with a byte mask for a single label
	 * 
	 * @param imo
	 *            ImageObject = input image object
	 * @param mask
	 *            ImageObject = mask image object
	 * @param maskValByte
	 *            byte = byte mask value
	 * @throws ImageException
	 * @return boolean = success of operation
	 */
	public boolean StatsValMask(ImageObject imo, ImageObject mask, byte maskValByte) throws ImageException {
		if ((mask == null) || ((mask.getType() == ImageObject.TYPE_BYTE) && (mask.getData() == null))) {
			System.out.println("ERROR: missing mask");
			return false;
		}
		SetMaskValByte(maskValByte);
		return internalStatsValMask(imo, mask);
	}

	/**
	 * computes all stats with an int mask for a single label
	 * 
	 * @param imo
	 *            ImageObject = input object
	 * @param mask
	 *            ImageObject = input mask
	 * @param maskValInt
	 *            int = mask int value
	 * @throws ImageException
	 * @return boolean = success of operation
	 */
	public boolean StatsValMask(ImageObject imo, ImageObject mask, int maskValInt) throws ImageException {
		if ((mask == null) || ((mask.getType() == ImageObject.TYPE_BYTE) && (mask.getData() == null))) {
			System.out.println("ERROR: missing mask");
			return false;
		}
		SetMaskValInt(maskValInt);
		return internalStatsValMask(imo, mask);
	}

	///////////////////////////////////////////////////////////////////////
	/**
	 * computes all stats with a mask for multiple labels
	 * 
	 * @param imo
	 *            ImageObject = input image
	 * @param mask
	 *            ImageObject = mask image
	 * @return boolean = success of operation
	 */
	public boolean StatsImMask(ImageObject imo, ImageObject mask) {
		if ((mask == null) || ((mask.getType() == ImageObject.TYPE_BYTE) && (mask.getData() == null))) {
			System.out.println("ERROR: missing mask");
			return false;
		}
		return internalStatsImMask(imo, mask);
	}

	//////////////////////////////////////////////////////////////////////////////
	/**
	 * private method that performs StatsMask for byte and int mask images
	 * 
	 * @param imo
	 *            ImageObject = input image
	 * @param mask
	 *            ImageObject = mask image
	 * @throws ImageException
	 * @return boolean = success of operation
	 */
	private boolean internalStatsValMask(ImageObject imo, ImageObject mask) throws ImageException {
		// setup the mask
		if (!SetMaskObject(mask)) {
			return false;
		}
		SetIsMaskPresent(true);

		// compute stats
		boolean answer, ret, local;
		answer = true;
		local = GetAllStats();
		SetAllStats(true);
		ret = MinMaxVal(imo);
		answer = answer && ret;
		if (ret == false) {
			System.out.println("Error: in MinMaxVal()");
			//return false;
		}
		ret = MeanStdevVal(imo);
		answer = answer && ret;
		if (ret == false) {
			System.out.println("Error: in MeanStdevVal()");
			//return false;
		}
		ret = SkewKurtosisVal(imo);
		answer = answer && ret;
		if (ret == false) {
			System.out.println("Error: in SkewKurtosisVal()");
			//return false;
		}

		SetAllStats(local);
		return answer;
	}

	//////////////////////////////////////////////////////////////////////////////
	/**
	 * private method that performs StatsMask for byte and int mask images for
	 * all labels
	 * 
	 * @param imo
	 *            ImageObject = input image
	 * @param mask
	 *            ImageObject = mask image
	 * @return boolean = success of operation
	 */
	private boolean internalStatsImMask(ImageObject imo, ImageObject mask) {
		// setup the mask
		//if( !SetMaskObject(mask) ){
		// return false;
		//}
		//SetIsMaskPresent(true);

		// compute stats
		boolean answer, ret, local;
		answer = true;
		local = GetAllStats();
		SetAllStats(true);
		/*
		 * ret = MinMaxIm(imo); answer = answer && ret; if(ret == false){
		 * System.out.println("Error: in MinMaxIm()"); //return false; }
		 */
		ret = MeanStdevTable(imo, mask);
		answer = answer && ret;
		if (ret == false) {
			System.out.println("Error: in MeanStdevIm()");
			//return false;
		}
		/*
		 * ret = SkewKurtosisIm(imo); answer = answer && ret; if(ret == false){
		 * System.out.println("Error: in SkewKurtosisIm()"); //return false; }
		 */
		SetAllStats(local);
		return answer;
	}

	/////////////////////////////////////////////////////////
	/**
	 * compute min and max values
	 * 
	 * @param imo
	 *            ImageObject = input image
	 * @return boolean = success of operation
	 */
	public boolean MinMaxVal(ImageObject imo) {
		//sanity check
		if (imo == null) {
			System.out.println("Error: no image data");
			return false;
		}
		if (_isMaskPresent) {
			if ((_maskOrig == null) || (_maskOrig.getData() == null) || ((_maskOrig.getType() != ImageObject.TYPE_BYTE) && (_maskOrig.getType() != ImageObject.TYPE_INT))
					|| (_maskOrig.getNumBands() != 1)) {
				System.out.println("Error: no mask data or mask.getNumBands() != 1");
				return false;
			}
			if ((_maskOrig.getNumRows() != imo.getNumRows()) || (_maskOrig.getNumCols() != imo.getNumCols())) {
				System.out.println("Error: mismatch of mask and image dimensions");
				return false;
			}
		}

		String sampType = imo.getTypeString();
		int i, j, idx, idxMask;
		boolean signal = true;
		int val;
		if (sampType.equalsIgnoreCase("BYTE")) {
			//_minVal = new ImageObject(1,1,imo.getNumBands(),"INT");
			try {
				_minVal = _minVal.createImage(1, 1, imo.getNumBands(), "INT");
				//_maxVal = new ImageObject(1,1,imo.getNumBands(),"INT");
				_maxVal = _maxVal.createImage(1, 1, imo.getNumBands(), "INT");
			} catch (Exception e) {
				logger.error("ERROR: createImage failed");
				return false;
			}
			for (i = 0; i < _maxVal.getSize(); i++) {
				_minVal.setInt(i, _lim.MAXPOS_BYTE);
				_maxVal.setInt(i, -1);
			}
			if (_isMaskPresent) {
				// mask is present
				if (_maskOrig.getTypeString().equalsIgnoreCase("BYTE")) {
					for (idxMask = 0, idx = 0; idx < imo.getSize(); idx += imo.getNumBands(), idxMask++) {
						if (_maskOrig.getByte(idxMask) == _maskValByte) {
							for (j = 0; j < imo.getNumBands(); j++) {
								val = imo.getByte(idx + j);
								if (val < 0) {
									val += _lim.MAXPOS_BYTE;
								}
								if (_minVal.getInt(j) > val) {
									_minVal.setInt(j, val);
								}
								if (_maxVal.getInt(j) < val) {
									_maxVal.setInt(j, val);
								}
							}// end of j loop
						}
					}// end of idxMask
				}
				if (_maskOrig.getTypeString().equalsIgnoreCase("INT")) {
					for (idxMask = 0, idx = 0; idx < imo.getSize(); idx += imo.getNumBands(), idxMask++) {
						if (_maskOrig.getInt(idxMask) == _maskValInt) {
							for (j = 0; j < imo.getNumBands(); j++) {
								val = imo.getByte(idx + j);
								if (val < 0) {
									val += _lim.MAXPOS_BYTE;
								}
								if (_minVal.getInt(j) > val) {
									_minVal.setInt(j, val);
								}
								if (_maxVal.getInt(j) < val) {
									_maxVal.setInt(j, val);
								}
							}// end of j loop
						}
					}// end of idxMask
				}
			} else {
				idx = 0;
				while (idx < imo.getSize()) {
					for (j = 0; j < imo.getNumBands(); j++) {
						val = imo.getByte(idx);
						if (val < 0) {
							val += _lim.MAXPOS_BYTE;
						}
						if (_minVal.getInt(j) > val) {
							_minVal.setInt(j, val);
						}
						if (_maxVal.getInt(j) < val) {
							_maxVal.setInt(j, val);
						}
						idx++;
					}
				}
			}
			signal = false;
		}// end of BYTE

		if (signal && sampType.equalsIgnoreCase("SHORT")) {
			try {
				_minVal.createImage(1, 1, imo.getNumBands(), "INT");
				_maxVal.createImage(1, 1, imo.getNumBands(), "INT");
			} catch (Exception e) {
				logger.error("ERROR: createImage failed");
				return false;
			}

			for (i = 0; i < _maxVal.getSize(); i++) {
				_minVal.setInt(i, _lim.MAXPOS_SHORT);
				_maxVal.setInt(i, -1);
			}
			if (_isMaskPresent) {
				// mask is present
				if (_maskOrig.getTypeString().equalsIgnoreCase("BYTE")) {
					for (idxMask = 0, idx = 0; idx < imo.getSize(); idx += imo.getNumBands(), idxMask++) {
						if (_maskOrig.getByte(idxMask) == _maskValByte) {
							for (j = 0; j < imo.getNumBands(); j++) {
								val = imo.getShort(idx + j);
								if (val < 0) {
									val += _lim.MAXPOS_SHORT;
								}
								if (_minVal.getInt(j) > val) {
									_minVal.setInt(j, val);
								}
								if (_maxVal.getInt(j) < val) {
									_maxVal.setInt(j, val);
								}
							}
						}
					}
				}// end of BYTE
				if (_maskOrig.getTypeString().equalsIgnoreCase("INT")) {
					for (idxMask = 0, idx = 0; idx < imo.getSize(); idx += imo.getNumBands(), idxMask++) {
						if (_maskOrig.getInt(idxMask) == _maskValInt) {
							for (j = 0; j < imo.getNumBands(); j++) {
								val = imo.getShort(idx + j);
								if (val < 0) {
									val += _lim.MAXPOS_SHORT;
								}
								if (_minVal.getInt(j) > val) {
									_minVal.setInt(j, val);
								}
								if (_maxVal.getInt(j) < val) {
									_maxVal.setInt(j, val);
								}
							}
						}
					}
				}// end of INT
			} else {
				idx = 0;
				while (idx < imo.getSize()) {
					for (j = 0; j < imo.getNumBands(); j++) {
						val = imo.getShort(idx);
						if (val < 0) {
							val += _lim.MAXPOS_SHORT;
						}
						if (_minVal.getInt(j) > val) {
							_minVal.setInt(j, val);
						}
						if (_maxVal.getInt(j) < val) {
							_maxVal.setInt(j, val);
						}
						idx++;
					}
				}
			}
			signal = false;
		}// end of SHORT

		if (signal && sampType.equalsIgnoreCase("INT")) {
/*
 * _minVal = new ImageObject(); _minVal.getNumRows() = _minVal.numcols = 1;
 * _minVal.getNumBands() = imo.getNumBands(); _minVal.getSize() =
 * imo.getNumBands(); _minVal.sampType = "INT"; _minVal.imageInt = new
 * int[(int)_minVal.getSize()];
 * 
 * _maxVal = new ImageObject(); _maxVal.getNumRows() = _maxVal.numcols = 1;
 * _maxVal.getNumBands() = imo.getNumBands(); _maxVal.getSize() =
 * imo.getNumBands(); _maxVal.sampType = "INT"; _maxVal.imageInt = new
 * int[(int)_maxVal.getSize()];
 */
			try {
				_minVal.createImage(1, 1, imo.getNumBands(), "INT");
				_maxVal.createImage(1, 1, imo.getNumBands(), "INT");
			} catch (Exception e) {
				logger.error("ERROR: createImage failed");
				return false;
			}

			for (i = 0; i < _maxVal.getSize(); i++) {
				_minVal.setInt(i, _lim.MAX_INT);
				_maxVal.setInt(i, _lim.MIN_INT);
			}
			if (_isMaskPresent) {
				// mask is present
				if (_maskOrig.getTypeString().equalsIgnoreCase("BYTE")) {
					for (idxMask = 0, idx = 0; idx < imo.getSize(); idx += imo.getNumBands(), idxMask++) {
						if (_maskOrig.getByte(idxMask) == _maskValByte) {
							for (j = 0; j < imo.getNumBands(); j++) {
								if (_minVal.getInt(j) > imo.getInt(idx + j)) {
									_minVal.setInt(j, imo.getInt(idx + j));
								}
								if (_maxVal.getInt(j) < imo.getInt(idx + j)) {
									_maxVal.setInt(j, imo.getInt(idx + j));
								}
							}
						}
					}
				}// end of BYTE
				if (_maskOrig.getTypeString().equalsIgnoreCase("INT")) {
					for (idxMask = 0, idx = 0; idx < imo.getSize(); idx += imo.getNumBands(), idxMask++) {
						if (_maskOrig.getInt(idxMask) == _maskValInt) {
							for (j = 0; j < imo.getNumBands(); j++) {
								if (_minVal.getInt(j) > imo.getInt(idx + j)) {
									_minVal.setInt(j, imo.getInt(idx + j));
								}
								if (_maxVal.getInt(j) < imo.getInt(idx + j)) {
									_maxVal.setInt(j, imo.getInt(idx + j));
								}
							}
						}
					}
				}// end of INT
			} else {
				idx = 0;
				while (idx < imo.getSize()) {
					for (j = 0; j < imo.getNumBands(); j++) {
						if (_minVal.getInt(j) > imo.getInt(idx)) {
							_minVal.setInt(j, imo.getInt(idx));
						}
						if (_maxVal.getInt(j) < imo.getInt(idx)) {
							_maxVal.setInt(j, imo.getInt(idx));
						}
						idx++;
					}
				}
			}

			signal = false;
		}
		if (signal && sampType.equalsIgnoreCase("LONG")) {
/*
 * _minVal = new ImageObject(); _minVal.getNumRows() = _minVal.getNumCols() = 1;
 * _minVal.getNumBands() = imo.getNumBands(); _minVal.getSize() =
 * imo.getNumBands(); _minVal.sampType = "LONG"; _minVal.imageLong = new
 * long[(int)_minVal.getSize()];
 * 
 * _maxVal = new ImageObject(); _maxVal.getNumRows() = _maxVal.getNumCols() = 1;
 * _maxVal.getNumBands() = imo.getNumBands(); _maxVal.getSize() =
 * imo.getNumBands(); _maxVal.sampType = "LONG"; _maxVal.imageLong = new
 * long[(int)_maxVal.getSize()];
 */
			try {
				_minVal.createImage(1, 1, imo.getNumBands(), "LONG");
				_maxVal.createImage(1, 1, imo.getNumBands(), "LONG");
			} catch (Exception e) {
				logger.error("ERROR: createImage failed");
				return false;
			}

			for (i = 0; i < _maxVal.getSize(); i++) {
				_minVal.setLong(i, _lim.MAX_LONG);
				_maxVal.setLong(i, _lim.MIN_LONG);
			}
			if (_isMaskPresent) {
				// mask is present
				if (_maskOrig.getTypeString().equalsIgnoreCase("BYTE")) {
					for (idxMask = 0, idx = 0; idx < imo.getSize(); idx += imo.getNumBands(), idxMask++) {
						if (_maskOrig.getByte(idxMask) == _maskValByte) {
							for (j = 0; j < imo.getNumBands(); j++) {
								if (_minVal.getLong(j) > imo.getLong(idx + j)) {
									_minVal.setLong(j, imo.getLong(idx + j));
								}
								if (_maxVal.getLong(j) < imo.getLong(idx + j)) {
									_maxVal.setLong(j, imo.getLong(idx + j));
								}
							}
						}
					}
				}// end of BYTE
				if (_maskOrig.getTypeString().equalsIgnoreCase("INT")) {
					for (idxMask = 0, idx = 0; idx < imo.getSize(); idx += imo.getNumBands(), idxMask++) {
						if (_maskOrig.getInt(idxMask) == _maskValInt) {
							for (j = 0; j < imo.getNumBands(); j++) {
								if (_minVal.getLong(j) > imo.getLong(idx + j)) {
									_minVal.setLong(j, imo.getLong(idx + j));
								}
								if (_maxVal.getLong(j) < imo.getLong(idx + j)) {
									_maxVal.setLong(j, imo.getLong(idx + j));
								}
							}
						}
					}
				}// end of INT mask

			} else {
				idx = 0;
				while (idx < imo.getSize()) {
					for (j = 0; j < imo.getNumBands(); j++) {
						if (_minVal.getLong(j) > imo.getLong(idx)) {
							_minVal.setLong(j, imo.getLong(idx));
						}
						if (_maxVal.getLong(j) < imo.getLong(idx)) {
							_maxVal.setLong(j, imo.getLong(idx));
						}
						idx++;
					}
				}
			}
			signal = false;
		}
		if (signal && sampType.equalsIgnoreCase("FLOAT")) {
/*
 * _minVal = new ImageObject(); _minVal.getNumRows() = _minVal.getNumCols() = 1;
 * _minVal.getNumBands() = imo.getNumBands(); _minVal.getSize() =
 * imo.getNumBands(); _minVal.sampType = "FLOAT"; _minVal.imageFloat = new
 * float[(int)_minVal.getSize()];
 * 
 * _maxVal = new ImageObject(); _maxVal.getNumRows() = _maxVal.getNumCols() = 1;
 * _maxVal.getNumBands() = imo.getNumBands(); _maxVal.getSize() =
 * imo.getNumBands(); _maxVal.sampType = "FLOAT"; _maxVal.imageFloat = new
 * float[(int)_maxVal.getSize()];
 */
			try {
				_minVal.createImage(1, 1, imo.getNumBands(), ImageObject.TYPE_FLOAT);
				_maxVal.createImage(1, 1, imo.getNumBands(), ImageObject.TYPE_FLOAT);
			} catch (Exception e) {
				logger.error("ERROR: createImage failed");
				return false;
			}

			for (i = 0; i < _maxVal.getSize(); i++) {
				_minVal.setFloat(i, _lim.MAX_FLOAT);
				_maxVal.setFloat(i, _lim.MIN_FLOAT);
			}
			if (_isMaskPresent) {
				// mask is present
				if (_maskOrig.getTypeString().equalsIgnoreCase("BYTE")) {
					for (idxMask = 0, idx = 0; idx < imo.getSize(); idx += imo.getNumBands(), idxMask++) {
						if (_maskOrig.getByte(idxMask) == _maskValByte) {
							for (j = 0; j < imo.getNumBands(); j++) {
								if (_minVal.getFloat(j) > imo.getFloat(idx + j)) {
									_minVal.setFloat(j, imo.getFloat(idx + j));
								}
								if (_maxVal.getFloat(j) < imo.getFloat(idx + j)) {
									_maxVal.setFloat(j, imo.getFloat(idx + j));
								}
							}
						}
					}
				}// end of BYTE
				if (_maskOrig.getTypeString().equalsIgnoreCase("INT")) {
					for (idxMask = 0, idx = 0; idx < imo.getSize(); idx += imo.getNumBands(), idxMask++) {
						if (_maskOrig.getInt(idxMask) == _maskValInt) {
							for (j = 0; j < imo.getNumBands(); j++) {
								if (_minVal.getFloat(j) > imo.getFloat(idx + j)) {
									_minVal.setFloat(j, imo.getFloat(idx + j));
								}
								if (_maxVal.getFloat(j) < imo.getFloat(idx + j)) {
									_maxVal.setFloat(j, imo.getFloat(idx + j));
								}
							}
						}
					}
				}// end of INT

			} else {
				idx = 0;
				while (idx < imo.getSize()) {
					for (j = 0; j < imo.getNumBands(); j++) {
						if (_minVal.getFloat(j) > imo.getFloat(idx)) {
							_minVal.setFloat(j, imo.getFloat(idx));
						}
						if (_maxVal.getFloat(j) < imo.getFloat(idx)) {
							_maxVal.setFloat(j, imo.getFloat(idx));
						}
						idx++;
					}
				}
			}
			signal = false;
		}
		if (signal && sampType.equalsIgnoreCase("DOUBLE")) {
/*
 * _minVal = new ImageObject(); _minVal.getNumRows() = _minVal.getNumCols() = 1;
 * _minVal.getNumBands() = imo.getNumBands(); _minVal.getSize() =
 * imo.getNumBands(); _minVal.sampType = "DOUBLE"; _minVal.imageDouble = new
 * double[(int)_minVal.getSize()];
 * 
 * _maxVal = new ImageObject(); _maxVal.getNumRows() = _maxVal.getNumCols() = 1;
 * _maxVal.getNumBands() = imo.getNumBands(); _maxVal.getSize() =
 * imo.getNumBands(); _maxVal.sampType = "DOUBLE"; _maxVal.imageDouble = new
 * double[(int)_maxVal.getSize()];
 */
			try {
				_minVal.createImage(1, 1, imo.getNumBands(), "DOUBLE");
				_maxVal.createImage(1, 1, imo.getNumBands(), "DOUBLE");
			} catch (Exception e) {
				logger.error("ERROR: createImage failed");
				return false;
			}

			for (i = 0; i < _maxVal.getSize(); i++) {
				_minVal.setDouble(i, _lim.MAX_DOUBLE);
				_maxVal.setDouble(i, _lim.MIN_DOUBLE);
			}
			if (_isMaskPresent) {
				// mask is present
				if (_maskOrig.getTypeString().equalsIgnoreCase("BYTE")) {
					for (idxMask = 0, idx = 0; idx < imo.getSize(); idx += imo.getNumBands(), idxMask++) {
						if (_maskOrig.getByte(idxMask) == _maskValByte) {
							for (j = 0; j < imo.getNumBands(); j++) {
								if (_minVal.getDouble(j) > imo.getDouble(idx + j)) {
									_minVal.setDouble(j, imo.getDouble(idx + j));
								}
								if (_maxVal.getDouble(j) < imo.getDouble(idx + j)) {
									_maxVal.setDouble(j, imo.getDouble(idx + j));
								}
							}
						}
					}
				}// end of BYTE
				if (_maskOrig.getTypeString().equalsIgnoreCase("INT")) {
					for (idxMask = 0, idx = 0; idx < imo.getSize(); idx += imo.getNumBands(), idxMask++) {
						if (_maskOrig.getInt(idxMask) == _maskValInt) {
							for (j = 0; j < imo.getNumBands(); j++) {
								if (_minVal.getDouble(j) > imo.getDouble(idx + j)) {
									_minVal.setDouble(j, imo.getDouble(idx + j));
								}
								if (_maxVal.getDouble(j) < imo.getDouble(idx + j)) {
									_maxVal.setDouble(j, imo.getDouble(idx + j));
								}
							}
						}
					}
				}// end of INT

			} else {
				idx = 0;
				while (idx < imo.getSize()) {
					for (j = 0; j < imo.getNumBands(); j++) {
						if (_minVal.getDouble(j) > imo.getDouble(idx)) {
							_minVal.setDouble(j, imo.getDouble(idx));
						}
						if (_maxVal.getDouble(j) < imo.getDouble(idx)) {
							_maxVal.setDouble(j, imo.getDouble(idx));
						}
						idx++;
					}
				}
			}

			signal = false;
		}

		if (signal) {
			System.out.println("Error: MinMaxVal no sampType=" + sampType + " is supported");
			return false;
		}
		return true;

	}

	/**
	 * compute mean and stdev values
	 * 
	 * @param imo
	 *            ImageObject = input image
	 * @return boolean = success of operation
	 */
	public boolean MeanStdevVal(ImageObject imo) {
		//sanity check
		if (imo == null) {
			System.out.println("Error: no image data");
			return false;
		}
		int i;
		if (_isMaskPresent) {
			// _maskOrig has to be INT
			if ((_maskOrig == null) || (_maskOrig.getData() == null) || (_maskOrig.getNumBands() != 1)) {
				System.out.println("Error: no mask data or mask.getNumBands() != 1");
				return false;
			}
			if ((_maskOrig.getNumRows() != imo.getNumRows()) || (_maskOrig.getNumCols() != imo.getNumCols())) {
				System.out.println("Error: mismatch of mask and image dimensions");
				return false;
			}
			// allocate memory for counSamp
			/*
			 * _countSamp = new ImageObject(); _countSamp.getNumRows() =
			 * _countSamp.getNumCols() = 1; _countSamp.length =
			 * imo.getNumBands(); _countSamp.length = imo.getNumBands();
			 * _countSamp.sampType = "LONG"; _countSamp.imageLong = new
			 * long[(int)_countSamp.length];
			 */

			_countSamp = new long[imo.getNumBands()];

			// init values
			for (i = 0; i < _countSamp.length; i++) {
				_countSamp[i] = 0;
			}

		}
/*
 * _meanVal = new ImageObject(); _meanVal.numrows = _meanVal.getNumCols() = 1;
 * _meanVal.length = imo.getNumBands(); _meanVal.getSize() = imo.getNumBands();
 * _meanVal.sampType = "DOUBLE"; _meanVal.imageDouble = new
 * double[(int)_meanVal.getSize()];
 * 
 * _stdevVal = new ImageObject(); _stdevVal.numrows = _stdevVal.getNumCols() =
 * 1; _stdevVal.length = imo.getNumBands(); _stdevVal.getSize() =
 * imo.getNumBands(); _stdevVal.sampType = "DOUBLE"; _stdevVal.imageDouble = new
 * double[(int)_stdevVal.getSize()];
 */
		_meanVal = new double[imo.getNumBands()];
		_stdevVal = new double[imo.getNumBands()];

		String sampType = imo.getTypeString();
		int j, idx, idxMask;
		boolean signal = true;
		double val;

		// init values
		for (i = 0; i < _meanVal.length; i++) {
			_meanVal[i] = 0.0;//_lim.MAX_BYTE;
			_stdevVal[i] = 0.0;//_lim.MIN_BYTE;
		}

		// treated as unsigned java byte
		if (sampType.equalsIgnoreCase("BYTE")) {
			if (_isMaskPresent) {
				// mask is present
				if (_maskOrig.getTypeString().equalsIgnoreCase("BYTE")) {
					for (idxMask = 0, idx = 0; idx < imo.getSize(); idx += imo.getNumBands(), idxMask++) {
						if (_maskOrig.getByte(idxMask) == _maskValByte) {
							for (j = 0; j < imo.getNumBands(); j++) {
								val = imo.getByte(idx + j);
								if (val < 0) {
									val += _lim.MAXPOS_BYTE;
								}

								_meanVal[j] += val;
								_stdevVal[j] += val * val;
								_countSamp[j]++;
							}
						}
					}
				}// end of BYTE
				if (_maskOrig.getTypeString().equalsIgnoreCase("INT")) {
					for (idxMask = 0, idx = 0; idx < imo.getSize(); idx += imo.getNumBands(), idxMask++) {
						if (_maskOrig.getInt(idxMask) == _maskValInt) {
							for (j = 0; j < imo.getNumBands(); j++) {
								val = imo.getByte(idx + j);
								if (val < 0) {
									val += _lim.MAXPOS_BYTE;
								}

								_meanVal[j] += val;
								_stdevVal[j] += val * val;
								_countSamp[j]++;
							}
						}
					}
				}// end of INT
			} else {
				idx = 0;
				while (idx < imo.getSize()) {
					for (j = 0; j < imo.getNumBands(); j++) {
						val = imo.getByte(idx);
						if (val < 0) {
							val += _lim.MAXPOS_BYTE;
						}
						_meanVal[j] += val;
						_stdevVal[j] += val * val;
						idx++;
					}
				}
			}
			signal = false;
		}
		if (signal && sampType.equalsIgnoreCase("SHORT")) {
			if (_isMaskPresent) {
				// mask is present
				if (_maskOrig.getTypeString().equalsIgnoreCase("BYTE")) {
					for (idxMask = 0, idx = 0; idx < imo.getSize(); idx += imo.getNumBands(), idxMask++) {
						if (_maskOrig.getByte(idxMask) == _maskValByte) {
							for (j = 0; j < imo.getNumBands(); j++) {
								val = imo.getShort(idx + j);
								if (val < 0) {
									val += _lim.MAXPOS_SHORT;
								}
								_meanVal[j] += val;
								_stdevVal[j] += val * val;
								_countSamp[j]++;
							}
						}
					}
				}
				if (_maskOrig.getTypeString().equalsIgnoreCase("INT")) {
					for (idxMask = 0, idx = 0; idx < imo.getSize(); idx += imo.getNumBands(), idxMask++) {
						if (_maskOrig.getInt(idxMask) == _maskValInt) {
							for (j = 0; j < imo.getNumBands(); j++) {
								val = imo.getShort(idx + j);
								if (val < 0) {
									val += _lim.MAXPOS_SHORT;
								}
								_meanVal[j] += val;
								_stdevVal[j] += val * val;
								_countSamp[j]++;
							}
						}
					}
				}
			} else {
				idx = 0;
				while (idx < imo.getSize()) {
					for (j = 0; j < imo.getNumBands(); j++) {
						val = imo.getShort(idx);
						if (val < 0) {
							val += _lim.MAXPOS_SHORT;
						}
						_meanVal[j] += val;
						_stdevVal[j] += val * val;
						idx++;
					}
				}
			}
/*
 * idx = 0; for(i=0;i<imo.getSize();i+=imo.getNumBands()){ for(j=0;j<imo.getNumBands();j++){
 * val = imo.getShort(idx]; _meanVal[j] += val; _stdevVal[j] += val*val; idx++; } }
 */
			signal = false;
		}
		if (signal && sampType.equalsIgnoreCase("INT")) {
			if (_isMaskPresent) {
				// mask is present
				if (_maskOrig.getTypeString().equalsIgnoreCase("BYTE")) {
					for (idxMask = 0, idx = 0; idx < imo.getSize(); idx += imo.getNumBands(), idxMask++) {
						if (_maskOrig.getByte(idxMask) == _maskValByte) {
							for (j = 0; j < imo.getNumBands(); j++) {
								val = imo.getInt(idx + j);
								_meanVal[j] += val;
								_stdevVal[j] += val * val;
								_countSamp[j]++;
							}
						}
					}
				}
				if (_maskOrig.getTypeString().equalsIgnoreCase("INT")) {
					for (idxMask = 0, idx = 0; idx < imo.getSize(); idx += imo.getNumBands(), idxMask++) {
						if (_maskOrig.getInt(idxMask) == _maskValInt) {
							for (j = 0; j < imo.getNumBands(); j++) {
								val = imo.getInt(idx + j);
								_meanVal[j] += val;
								_stdevVal[j] += val * val;
								_countSamp[j]++;
							}
						}
					}
				}
			} else {
				idx = 0;
				while (idx < imo.getSize()) {
					for (j = 0; j < imo.getNumBands(); j++) {
						val = imo.getInt(idx);
						_meanVal[j] += val;
						_stdevVal[j] += val * val;
						idx++;
					}
				}
			}
			signal = false;
		}

		if (signal && sampType.equalsIgnoreCase("LONG")) {
			if (_isMaskPresent) {
				// mask is present
				if (_maskOrig.getTypeString().equalsIgnoreCase("BYTE")) {
					for (idxMask = 0, idx = 0; idx < imo.getSize(); idx += imo.getNumBands(), idxMask++) {
						if (_maskOrig.getByte(idxMask) == _maskValByte) {
							for (j = 0; j < imo.getNumBands(); j++) {
								val = imo.getLong(idx + j);
								_meanVal[j] += val;
								_stdevVal[j] += val * val;
								_countSamp[j]++;
							}
						}
					}
				}
				if (_maskOrig.getTypeString().equalsIgnoreCase("INT")) {
					for (idxMask = 0, idx = 0; idx < imo.getSize(); idx += imo.getNumBands(), idxMask++) {
						if (_maskOrig.getInt(idxMask) == _maskValInt) {
							for (j = 0; j < imo.getNumBands(); j++) {
								val = imo.getLong(idx + j);
								_meanVal[j] += val;
								_stdevVal[j] += val * val;
								_countSamp[j]++;
							}
						}
					}
				}
			} else {
				idx = 0;
				while (idx < imo.getSize()) {
					for (j = 0; j < imo.getNumBands(); j++) {
						val = imo.getLong(idx);
						_meanVal[j] += val;
						_stdevVal[j] += val * val;
						idx++;
					}
				}
			}
			signal = false;
		}
		if (signal && sampType.equalsIgnoreCase("FLOAT")) {
			if (_isMaskPresent) {
				// mask is present
				if (_maskOrig.getTypeString().equalsIgnoreCase("BYTE")) {
					for (idxMask = 0, idx = 0; idx < imo.getSize(); idx += imo.getNumBands(), idxMask++) {
						if (_maskOrig.getByte(idxMask) == _maskValByte) {
							for (j = 0; j < imo.getNumBands(); j++) {
								val = imo.getFloat(idx + j);
								_meanVal[j] += val;
								_stdevVal[j] += val * val;
								_countSamp[j]++;
							}
						}
					}
				}
				if (_maskOrig.getTypeString().equalsIgnoreCase("INT")) {
					for (idxMask = 0, idx = 0; idx < imo.getSize(); idx += imo.getNumBands(), idxMask++) {
						if (_maskOrig.getInt(idxMask) == _maskValInt) {
							for (j = 0; j < imo.getNumBands(); j++) {
								val = imo.getFloat(idx + j);
								_meanVal[j] += val;
								_stdevVal[j] += val * val;
								_countSamp[j]++;
							}
						}
					}
				}
			} else {
				idx = 0;
				while (idx < imo.getSize()) {
					for (j = 0; j < imo.getNumBands(); j++) {
						val = imo.getFloat(idx);
						_meanVal[j] += val;
						_stdevVal[j] += val * val;
						idx++;
					}
				}
			}
			signal = false;
		}
		if (signal && sampType.equalsIgnoreCase("DOUBLE")) {
			if (_isMaskPresent) {
				// mask is present
				if (_maskOrig.getTypeString().equalsIgnoreCase("BYTE")) {
					for (idxMask = 0, idx = 0; idx < imo.getSize(); idx += imo.getNumBands(), idxMask++) {
						if (_maskOrig.getByte(idxMask) == _maskValByte) {
							for (j = 0; j < imo.getNumBands(); j++) {
								val = imo.getDouble(idx + j);
								_meanVal[j] += val;
								_stdevVal[j] += val * val;
								_countSamp[j]++;
							}
						}
					}
				}
				if (_maskOrig.getTypeString().equalsIgnoreCase("INT")) {
					for (idxMask = 0, idx = 0; idx < imo.getSize(); idx += imo.getNumBands(), idxMask++) {
						if (_maskOrig.getInt(idxMask) == _maskValInt) {
							for (j = 0; j < imo.getNumBands(); j++) {
								val = imo.getDouble(idx + j);
								_meanVal[j] += val;
								_stdevVal[j] += val * val;
								_countSamp[j]++;
							}
						}
					}
				}
			} else {
				idx = 0;
				while (idx < imo.getSize()) {
					for (j = 0; j < imo.getNumBands(); j++) {
						val = imo.getDouble(idx);
						_meanVal[j] += val;
						_stdevVal[j] += val * val;
						idx++;
					}
				}
			}
			signal = false;
		}

		if (signal) {
			System.out.println("Error MeanStDevVal: no sampType=" + sampType + " is supported");
			return false;
		}

		// compute final values
		if (_isMaskPresent) {
			for (j = 0; j < imo.getNumBands(); j++) {
				val = _countSamp[j];
				if (val > 0.0) {
					_meanVal[j] /= val;
					_stdevVal[j] = _stdevVal[j] / val - _meanVal[j] * _meanVal[j];
					if (_stdevVal[j] >= 0.0) {
						_stdevVal[j] = Math.sqrt(_stdevVal[j]);
					} else {
						System.out.println("Error: stdev^2 < 0 : " + _stdevVal[j]);
						_stdevVal[j] = 0.0;
					}
				} else {
					System.out.println("Error: numSamp <=0 " + _countSamp[j]);
					_meanVal[j] = 0.0;
					_stdevVal[j] = 0.0;
				}
			}

		} else {
			val = (double) imo.getNumRows() * imo.getNumCols();
			if (val == 0.0) {
				System.out.println("Error: number of samples is equal to zero");
				return false;
			}

			for (j = 0; j < imo.getNumBands(); j++) {
				_meanVal[j] /= val;
				_stdevVal[j] = _stdevVal[j] / val - _meanVal[j] * _meanVal[j];
				if (_stdevVal[j] >= 0.0) {
					_stdevVal[j] = Math.sqrt(_stdevVal[j]);
				} else {
					System.out.println("Error: stdev^2 < 0 : " + _stdevVal[j]);
					_stdevVal[j] = 0.0;
				}
			}
		}

		return true;
	}

	/**
	 * compute skew and kurtosis values
	 * 
	 * @param imo
	 *            ImageObject = input image
	 * @return boolean = success of operation
	 */
	public boolean SkewKurtosisVal(ImageObject imo) {
		//sanity check
		if (imo == null) {
			System.out.println("Error: no image data");
			return false;
		}

		if ((_allStats == false) || (_meanVal == null) || (_stdevVal == null)) {
			if (MeanStdevVal(imo) == false) {
				return false;
			}
		}
		// one more sanity check
		if ((_meanVal.length != imo.getNumBands()) || (_stdevVal.length != imo.getNumBands())) {
			if (MeanStdevVal(imo) == false) {
				return false;
			}
		}

		//mask related sanity checks
		int i;
		if (_isMaskPresent) {
			if ((_maskOrig == null) || ((_maskOrig.getData() == null) && ((int[]) _maskOrig.getData() == null)) || (_maskOrig.getNumBands() != 1)) {
				System.out.println("Error: no mask data or mask.getNumBands() != 1");
				return false;
			}
			if ((_maskOrig.getNumRows() != imo.getNumRows()) || (_maskOrig.getNumCols() != imo.getNumCols())) {
				System.out.println("Error: mismatch of mask and image dimensions");
				return false;
			}
			//double check although this should have been allocated in the MeanStDev
			// method. It is not even necessary to coumpute _countSamp
			// but we do it here
			if ((_countSamp == null) || (_countSamp.length != imo.getNumBands())) {
				// allocate memory for counSamp
				/*
				 * _countSamp = new ImageObject(); _countSamp.numrows =
				 * _countSamp.getNumCols() = 1; _countSamp.length =
				 * imo.getNumBands(); _countSamp.length = imo.getNumBands();
				 * _countSamp.sampType = "LONG"; _countSamp.imageLong = new
				 * long[(int)_countSamp.length];
				 */
				_countSamp = new long[imo.getNumBands()];
			}
			// init values
			for (i = 0; i < _countSamp.length; i++) {
				_countSamp[i] = 0;
			}

		}

/*
 * _skewVal = new ImageObject(); _skewVal.numrows = _skewVal.getNumCols() = 1;
 * _skewVal.getNumBands() = imo.getNumBands(); _skewVal.getSize() =
 * imo.getNumBands(); _skewVal.sampType = "DOUBLE"; _skewVal.imageDouble = new
 * double[(int)_skewVal.getSize()];
 * 
 * _kurtosisVal = new ImageObject(); _kurtosisVal.numrows =
 * _kurtosisVal.getNumCols() = 1; _kurtosisVal.getNumBands() =
 * imo.getNumBands(); _kurtosisVal.getSize() = imo.getNumBands();
 * _kurtosisVal.sampType = "DOUBLE"; _kurtosisVal.imageDouble = new
 * double[(int)_kurtosisVal.getSize()];
 */
		_skewVal = new double[imo.getNumBands()];
		_kurtosisVal = new double[imo.getNumBands()];

		String sampType = imo.getTypeString();
		int j, idx, idxMask;
		boolean signal = true;
		double val, val1, div;

		// init values
		for (i = 0; i < _skewVal.length; i++) {
			_skewVal[i] = 0.0;
			_kurtosisVal[i] = 0.0;
		}

		// treated as unsigned byte converted from java signed byte
		if (sampType.equalsIgnoreCase("BYTE")) {
			if (_isMaskPresent) {
				// mask is present
				if (_maskOrig.getTypeString().equalsIgnoreCase("BYTE")) {
					for (idxMask = 0, idx = 0; idx < imo.getSize(); idx += imo.getNumBands(), idxMask++) {
						if (_maskOrig.getByte(idxMask) == _maskValByte) {
							for (j = 0; j < imo.getNumBands(); j++) {
								val = imo.getByte(idx + j);
								if (val < 0) {
									val += _lim.MAXPOS_BYTE;
								}
								val -= _meanVal[j];
								//val = imo.getByte(idx+j] - _meanVal[j];
								val1 = val * val * val;
								_skewVal[j] += val1;
								_kurtosisVal[j] += val1 * val;
								_countSamp[j]++;
							}
						}
					}
				}
				if (_maskOrig.getTypeString().equalsIgnoreCase("INT")) {
					for (idxMask = 0, idx = 0; idx < imo.getSize(); idx += imo.getNumBands(), idxMask++) {
						if (_maskOrig.getInt(idxMask) == _maskValInt) {
							for (j = 0; j < imo.getNumBands(); j++) {
								val = imo.getByte(idx + j);
								if (val < 0) {
									val += _lim.MAXPOS_BYTE;
								}
								val -= _meanVal[j];
								//val = imo.getByte(idx+j] - _meanVal[j];
								val1 = val * val * val;
								_skewVal[j] += val1;
								_kurtosisVal[j] += val1 * val;
								_countSamp[j]++;
							}
						}
					}
				}

			} else {
				idx = 0;
				while (idx < imo.getSize()) {
					for (j = 0; j < imo.getNumBands(); j++) {
						val = imo.getByte(idx);
						if (val < 0) {
							val += _lim.MAXPOS_BYTE;
						}
						val -= _meanVal[j];

						//val = imo.getByte(idx] - _meanVal[j];
						val1 = val * val * val;
						_skewVal[j] += val1;
						_kurtosisVal[j] += val1 * val;
						idx++;
					}
				}
			}
			signal = false;
		}
		if (sampType.equalsIgnoreCase("SHORT")) {
			if (_isMaskPresent) {
				// mask is present
				if (_maskOrig.getTypeString().equalsIgnoreCase("BYTE")) {
					for (idxMask = 0, idx = 0; idx < imo.getSize(); idx += imo.getNumBands(), idxMask++) {
						if (_maskOrig.getByte(idxMask) == _maskValByte) {
							for (j = 0; j < imo.getNumBands(); j++) {
								val = imo.getShort(idx + j);
								if (val < 0) {
									val += _lim.MAXPOS_SHORT;
								}

								val -= _meanVal[j];
								val1 = val * val * val;
								_skewVal[j] += val1;
								_kurtosisVal[j] += val1 * val;
								_countSamp[j]++;
							}
						}
					}
				}
				if (_maskOrig.getTypeString().equalsIgnoreCase("INT")) {
					for (idxMask = 0, idx = 0; idx < imo.getSize(); idx += imo.getNumBands(), idxMask++) {
						if (_maskOrig.getInt(idxMask) == _maskValInt) {
							for (j = 0; j < imo.getNumBands(); j++) {
								val = imo.getShort(idx + j);
								if (val < 0) {
									val += _lim.MAXPOS_SHORT;
								}

								val -= _meanVal[j];
								val1 = val * val * val;
								_skewVal[j] += val1;
								_kurtosisVal[j] += val1 * val;
								_countSamp[j]++;
							}
						}
					}
				}
			} else {
				idx = 0;
				while (idx < imo.getSize()) {
					for (j = 0; j < imo.getNumBands(); j++) {
						val = imo.getShort(idx);
						if (val < 0) {
							val += _lim.MAXPOS_SHORT;
						}

						val -= _meanVal[j];
						val1 = val * val * val;
						_skewVal[j] += val1;
						_kurtosisVal[j] += val1 * val;
						idx++;
					}
				}
			}
/*
 * idx = 0; for(i=0;i<imo.getSize();i+=imo.getNumBands()){ for(j=0;j<imo.getNumBands();j++){
 * val = imo.getShort(idx] - _meanVal[j]; val1 = val*val*val; _skewVal[j] +=
 * val1; _kurtosisVal[j] += val1 * val; idx++; } }
 */
			signal = false;
		}
		if (sampType.equalsIgnoreCase("INT")) {
			if (_isMaskPresent) {
				// mask is present
				if (_maskOrig.getTypeString().equalsIgnoreCase("BYTE")) {
					for (idxMask = 0, idx = 0; idx < imo.getSize(); idx += imo.getNumBands(), idxMask++) {
						if (_maskOrig.getByte(idxMask) == _maskValByte) {
							for (j = 0; j < imo.getNumBands(); j++) {
								val = imo.getInt(idx + j) - _meanVal[j];
								val1 = val * val * val;
								_skewVal[j] += val1;
								_kurtosisVal[j] += val1 * val;
								_countSamp[j]++;
							}
						}
					}
				}
				if (_maskOrig.getTypeString().equalsIgnoreCase("INT")) {
					for (idxMask = 0, idx = 0; idx < imo.getSize(); idx += imo.getNumBands(), idxMask++) {
						if (_maskOrig.getInt(idxMask) == _maskValInt) {
							for (j = 0; j < imo.getNumBands(); j++) {
								val = imo.getInt(idx + j) - _meanVal[j];
								val1 = val * val * val;
								_skewVal[j] += val1;
								_kurtosisVal[j] += val1 * val;
								_countSamp[j]++;
							}
						}
					}
				}

			} else {
				idx = 0;
				while (idx < imo.getSize()) {
					for (j = 0; j < imo.getNumBands(); j++) {
						val = imo.getInt(idx) - _meanVal[j];
						val1 = val * val * val;
						_skewVal[j] += val1;
						_kurtosisVal[j] += val1 * val;
						idx++;
					}
				}
			}
			signal = false;
		}

		if (sampType.equalsIgnoreCase("LONG")) {
			if (_isMaskPresent) {
				// mask is present
				if (_maskOrig.getTypeString().equalsIgnoreCase("BYTE")) {
					for (idxMask = 0, idx = 0; idx < imo.getSize(); idx += imo.getNumBands(), idxMask++) {
						if (_maskOrig.getByte(idxMask) == _maskValByte) {
							for (j = 0; j < imo.getNumBands(); j++) {
								val = imo.getLong(idx + j) - _meanVal[j];
								val1 = val * val * val;
								_skewVal[j] += val1;
								_kurtosisVal[j] += val1 * val;
								_countSamp[j]++;
							}
						}
					}
				}
				if (_maskOrig.getTypeString().equalsIgnoreCase("INT")) {
					for (idxMask = 0, idx = 0; idx < imo.getSize(); idx += imo.getNumBands(), idxMask++) {
						if (_maskOrig.getInt(idxMask) == _maskValInt) {
							for (j = 0; j < imo.getNumBands(); j++) {
								val = imo.getLong(idx + j) - _meanVal[j];
								val1 = val * val * val;
								_skewVal[j] += val1;
								_kurtosisVal[j] += val1 * val;
								_countSamp[j]++;
							}
						}
					}
				}

			} else {
				idx = 0;
				while (idx < imo.getSize()) {
					for (j = 0; j < imo.getNumBands(); j++) {
						val = imo.getLong(idx) - _meanVal[j];
						val1 = val * val * val;
						_skewVal[j] += val1;
						_kurtosisVal[j] += val1 * val;
						idx++;
					}
				}
			}
			signal = false;
		}
		if (sampType.equalsIgnoreCase("FLOAT")) {
			if (_isMaskPresent) {
				// mask is present
				if (_maskOrig.getTypeString().equalsIgnoreCase("BYTE")) {
					for (idxMask = 0, idx = 0; idx < imo.getSize(); idx += imo.getNumBands(), idxMask++) {
						if (_maskOrig.getByte(idxMask) == _maskValByte) {
							for (j = 0; j < imo.getNumBands(); j++) {
								val = imo.getFloat(idx + j) - _meanVal[j];
								val1 = val * val * val;
								_skewVal[j] += val1;
								_kurtosisVal[j] += val1 * val;
								_countSamp[j]++;
							}
						}
					}
				}
				if (_maskOrig.getTypeString().equalsIgnoreCase("INT")) {
					for (idxMask = 0, idx = 0; idx < imo.getSize(); idx += imo.getNumBands(), idxMask++) {
						if (_maskOrig.getInt(idxMask) == _maskValInt) {
							for (j = 0; j < imo.getNumBands(); j++) {
								val = imo.getFloat(idx + j) - _meanVal[j];
								val1 = val * val * val;
								_skewVal[j] += val1;
								_kurtosisVal[j] += val1 * val;
								_countSamp[j]++;
							}
						}
					}
				}

			} else {
				idx = 0;
				while (idx < imo.getSize()) {
					for (j = 0; j < imo.getNumBands(); j++) {
						val = imo.getFloat(idx) - _meanVal[j];
						val1 = val * val * val;
						_skewVal[j] += val1;
						_kurtosisVal[j] += val1 * val;
						idx++;
					}
				}
			}
			signal = false;
		}
		if (sampType.equalsIgnoreCase("DOUBLE")) {
			if (_isMaskPresent) {
				// mask is present
				if (_maskOrig.getTypeString().equalsIgnoreCase("BYTE")) {
					for (idxMask = 0, idx = 0; idx < imo.getSize(); idx += imo.getNumBands(), idxMask++) {
						if (_maskOrig.getByte(idxMask) == _maskValByte) {
							for (j = 0; j < imo.getNumBands(); j++) {
								val = imo.getDouble(idx + j) - _meanVal[j];
								val1 = val * val * val;
								_skewVal[j] += val1;
								_kurtosisVal[j] += val1 * val;
								_countSamp[j]++;
							}
						}
					}
				}
				if (_maskOrig.getTypeString().equalsIgnoreCase("INT")) {
					for (idxMask = 0, idx = 0; idx < imo.getSize(); idx += imo.getNumBands(), idxMask++) {
						if (_maskOrig.getInt(idxMask) == _maskValInt) {
							for (j = 0; j < imo.getNumBands(); j++) {
								val = imo.getDouble(idx + j) - _meanVal[j];
								val1 = val * val * val;
								_skewVal[j] += val1;
								_kurtosisVal[j] += val1 * val;
								_countSamp[j]++;
							}
						}
					}
				}

			} else {
				idx = 0;
				while (idx < imo.getSize()) {
					for (j = 0; j < imo.getNumBands(); j++) {
						val = imo.getDouble(idx) - _meanVal[j];
						val1 = val * val * val;
						_skewVal[j] += val1;
						_kurtosisVal[j] += val1 * val;
						idx++;
					}
				}
			}
			signal = false;
		}

		if (signal) {
			System.out.println("Error: SkewKurtosis no sampType=" + sampType + " is supported");
			return false;
		}

		// compute final values
		val = (double) imo.getNumRows() * imo.getNumCols();
		for (j = 0; j < imo.getNumBands(); j++) {
			//skew = x^3/size/stdev^3
			if (_isMaskPresent) {
				//mask is present
				val = _countSamp[j];
			}

			val1 = _stdevVal[j] * _stdevVal[j];
			div = val * val1 * _stdevVal[j];
			if (div != 0.0) {
				_skewVal[j] /= div;
			} else {
				System.out.println("Warning: skew divisor is equal to zero at band= " + j);
				_skewVal[j] = 0.0;
			}

			//kurtosis = x^4/size/(stdev^4) - 3.0;
			// the offset is to put Gaussian to 0
			div = val * val1 * val1;
			if (div != 0.0) {
				_kurtosisVal[j] /= div;
			} else {
				System.out.println("Warning: kurtosis divisor is equal to zero at band= " + j);
				_kurtosisVal[j] = 0.0;
			}
			_kurtosisVal[j] -= 3.0;
		}

		return true;
	}

	//////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////
	/**
	 * Given a mask of labels,compute sample mean and stdev values of all
	 * labeled segments used by GeoFeature class to compute statistics for
	 * overlaid shapefiles on raster files the return image contains: (1) mask
	 * label, (2) number of pixels of that label, (3) mean values for all bands,
	 * (4) stdev values for all bands, (5) skew values for all bands and (6)
	 * kurtosis values for all bands. The size of the returned image is numrows =
	 * 1, numcols = number of labels, data type is FLOAT res = new
	 * ImageObject(1,numLabels,(2+(image.getNumBands())*numFeatures),"FLOAT");
	 * 
	 * @param image
	 *            ImageObject = input image
	 * @param mask
	 *            ImageObject = mask image
	 * @return ImageObject = resulting information
	 */
	public ImageObject AllStatsOverMask(ImageObject image, ImageObject mask) {
		if (mask == null) {
			System.out.println("Error: no labels");
			//return false;
			return null;
		}
		if (image == null) {
			System.out.println("Error: no input image");
			//return false;
			return null;
		}
		if (!image.getTypeString().equalsIgnoreCase("BYTE") && !image.getTypeString().equalsIgnoreCase("FLOAT")) {
			System.out.println("Error: other than BYTE or FLOAT input image is not supported");
			//return false;
			return null;
		}
		if (!mask.getTypeString().equalsIgnoreCase("BYTE") && !mask.getTypeString().equalsIgnoreCase("INT")) {
			System.out.println("Error: other than BYTE or INT mask image is not supported");
			//return false;
			return null;
		}

		if (mask.getNumBands() != 1) {
			System.out.println("Error: mask image sampPerPixel != 1 ");
			//return false;
			return null;
		}
		if ((image.getNumRows() != mask.getNumRows()) || (image.getNumCols() != mask.getNumCols())) {
			System.out.println("Error: mismatch in label and image size ");
			//return false;
			return null;
		}

		int index, band, i, idx, idxOut;
		long size = image.getSize();//numrows*numcols;
		// find the number of labels and max,min labels in the mask(label) image
		Histogram myHist = new Histogram();
		//if(mask.getTypeString().equalsIgnoreCase("BYTE") ){
		// sets WideBin = 1.0
		//myHist.SetHistParam256Bins(image);
		//    myHist.SetHistParam256Bins(mask);
		// }else{
		//find min and max label values
		boolean tempMaskFlag = GetIsMaskPresent();
		SetIsMaskPresent(false);
		boolean ret = MinMaxVal(mask);
		if (!ret || (GetMinVal() == null) || ((int[]) GetMinVal().getData() == null)) {
			System.out.println("ERROR: Finding MinMaxVal failed ret=" + ret + " GetMinVal().imageInt");
			return null;
		}
		myHist.SetIs256Bins(false);
		myHist.SetMinDataVal((double) GetMinVal().getInt(0));
		myHist.SetMaxDataVal((double) GetMaxVal().getInt(0));
		SetIsMaskPresent(tempMaskFlag);

		// this would lead to out of memory error
		//myHist.SetMinDataVal((double)_lim.MIN_INT);
		//myHist.SetMaxDataVal((double)_lim.MAX_INT);
		myHist.SetWideBins(1.0);
		//}
		//test
		//System.out.println("TEST: print hist setup");
		//myHist.PrintHistogram();

		band = 0;
		try {
			myHist.Hist(mask, band);
		} catch (Exception e) {
			logger.error("Error: Hist(mask,band) failed");
			return null;
		}
		myHist.Count();
		//test
		System.out.println("Test: Mask (label) image band=" + band + ", number of labels =" + myHist.GetCount());
		myHist.MinMaxHistBin();
		System.out.println("Test: minHistBin=" + myHist.GetMinHistBin() + ", maxHistBin =" + myHist.GetMaxHistBin());
		/*
		 * for(i=0;i<myHist.GetNumBins();i++){ if(myHist.GetHistDatum(i) > 0){
		 * System.out.println("Test: non-zero bin
		 * idx="+i+",valBin="+myHist.GetHistDatum(i)); } }
		 */
		int numLabels = (int) myHist.GetCount();
		int numFeatures = 4;
		ImageObject res = null;
		try {
			res = ImageObject.createImage(1, numLabels, (2 + (image.getNumBands()) * numFeatures), "FLOAT");
		} catch (Exception e) {
			logger.error("Error: ImageObject.createImage failed");
			return null;
		}
		// dimension is equal to (LUT ID) plus (total pixel count) plus
		// the number of bands (e.g., mean for each band) times four features
		//res = new ImageObject(1,numLabels,(2+(image.getNumBands())*numFeatures),"FLOAT");

		for (i = 0; i < res.getSize(); i++) {
			res.setFloat(i, 0.0F);
		}

		int[] lut = new int[numLabels];
		int maskVal;
		index = 0;
		idx = 0;

		for (i = 0; i < myHist.GetNumBins(); i++) {
			if (myHist.GetHistData()[i] != 0) {
				res.set(index, (float) (i + (int) myHist.GetMinDataVal()));
				lut[idx] = i + (int) myHist.GetMinDataVal();
				idx++;
				index += res.getNumBands();
			}
		}
		//test
		//for(i=0;i<numLabels;i++){
		//  System.out.println("Test: lut["+i+"]="+lut[i]);
		//}

		int foundLabel;
		float val, val1, div;
		float[] resArray = (float[]) res.getData();
		/////////////////////////////////////////////////////
		// compute mean and stdev features
		// for BYTE image
		if (image.getTypeString().equalsIgnoreCase("BYTE")) {
			if (mask.getTypeString().equalsIgnoreCase("BYTE")) {
				for (index = 0, idx = 0; index < image.getSize(); index += image.getNumBands(), idx++) {
					maskVal = mask.getByte(idx);
					if (maskVal < 0) {
						maskVal += _lim.MAXPOS_BYTE;
					}
					foundLabel = -1;
					for (i = 0; i < numLabels; i++) {
						if (maskVal == lut[i]) {
							foundLabel = i;
							i = numLabels + 10;
						}
					}
					if (i != numLabels + 11) {
						System.out.println("ERROR: did not find a match for a label = " + maskVal + " at idx=" + idx);
						continue;
					}
					idxOut = foundLabel * res.getNumBands() + 1;
					//res.imageFloat[idxOut ] ++;
					resArray[idxOut]++;
					idxOut++;
					for (band = 0; band < image.getNumBands(); band++) {
						val = image.getByte(index + band);
						if (val < 0) {
							val += _lim.MAXPOS_BYTE;
						}
						//res.imageFloat[idxOut + band ] += val;
						resArray[idxOut + band] += val;
						//res.imageFloat[idxOut + band + image.getNumBands() ] += val*val;
						resArray[idxOut + band + image.getNumBands()] += val * val;
					}
				}// end for index
			}// end of Mask BYTE
			if (mask.getTypeString().equalsIgnoreCase("INT")) {
				for (index = 0, idx = 0; index < image.getSize(); index += image.getNumBands(), idx++) {
					maskVal = mask.getInt(idx);
					foundLabel = -1;
					for (i = 0; i < numLabels; i++) {
						if (maskVal == lut[i]) {
							foundLabel = i;
							i = numLabels + 10;
						}
					}
					if (i != numLabels + 11) {
						System.out.println("ERROR: did not find a match for a label = " + maskVal + " at idx=" + idx);
						continue;
					}
					idxOut = foundLabel * res.getNumBands() + 1;
					//res.imageFloat[idxOut ] ++;
					resArray[idxOut]++;
					idxOut++;
					for (band = 0; band < image.getNumBands(); band++) {
						val = image.getByte(index + band);
						if (val < 0) {
							val += _lim.MAXPOS_BYTE;
						}
						//res.imageFloat[idxOut + band ] += val;
						resArray[idxOut + band] += val;
						//res.imageFloat[idxOut + band + image.getNumBands() ] += val*val;
						resArray[idxOut + band + image.getNumBands()] += val * val;
					}
				}// end for index
			}// end of Mask INT

			// compute final mean and stdev feature values
			for (index = 2; index < res.getSize(); index += res.getNumBands()) {
				//if(res.imageFloat[index-1] > 0.0 ){
				if (resArray[index - 1] > 0.0) {
					// mean
					for (band = 0; band < image.getNumBands(); band++) {
						// mean
						//res.imageFloat[index+band] /= res.getFloat(index-1);
						resArray[index + band] /= resArray[index - 1];
						// stdev
						//res.imageFloat[index+band+image.getNumBands() ] = res.getFloat(index+band+image.getNumBands() )/res.getFloat(index-1) - res.getFloat(index+band)*res.getFloat(index+band);
						resArray[index + band + image.getNumBands()] = resArray[index + band + image.getNumBands()] / res.getFloat(index - 1) - res.getFloat(index + band) * resArray[index + band];
						//if(res.getFloat(index+band+image.getNumBands() )>=0){
						if (resArray[index + band + image.getNumBands()] >= 0) {
							//res.setFloat(index+band+image.getNumBands(),(float)Math.sqrt( res.getFloat(index+band+image.getNumBands() ) ) );
							resArray[index + band + image.getNumBands()] = (float) Math.sqrt(resArray[index + band + image.getNumBands()]);
						} else {
							System.out.println("Error: stdev < 0 at index " + (index - 2) + ", band=" + band);
							//res.imageFloat[index+band+image.getNumBands() ] = 0.0F;
							resArray[index + band + image.getNumBands()] = 0.0F;
						}
					}// end of for band
				} else {
					//test
					System.out.println("INFO: did not find pixels with label=" + ((index - 2) / res.getNumBands()) + " at index " + index);

				}
			}// end of for index=2

		} // end of image BYTE

		///////////////
		// compute for BYTE image
		// compute skew and kurtosis features
		if (image.getTypeString().equalsIgnoreCase("BYTE")) {
			if (mask.getTypeString().equalsIgnoreCase("BYTE")) {
				for (index = 0, idx = 0; index < image.getSize(); index += image.getNumBands(), idx++) {
					maskVal = mask.getByte(idx);
					if (maskVal < 0) {
						maskVal += _lim.MAXPOS_BYTE;
					}
					foundLabel = -1;
					for (i = 0; i < numLabels; i++) {
						if (maskVal == lut[i]) {
							foundLabel = i;
							i = numLabels + 10;
						}
					}
					if (i != numLabels + 11) {
						System.out.println("ERROR: did not find a match for a label = " + maskVal + " at idx=" + idx);
						continue;
					}

					// set the index to the mean parameter
					i = foundLabel * res.getNumBands() + 2;
					// set the index to the skew parameter
					idxOut = i + (image.getNumBands() << 1);

					for (band = 0; band < image.getNumBands(); band++) {
						val = image.getByte(index + band);
						if (val < 0) {
							val += _lim.MAXPOS_BYTE;
						}
						//val -=   res.getFloat(i+band);
						val -= resArray[i + band];
						val1 = val * val * val;
						//res.imageFloat[idxOut + band ] += val1;
						resArray[idxOut + band] += val1;
						//res.imageFloat[idxOut + band + image.getNumBands() ] += val1*val;
						resArray[idxOut + band + image.getNumBands()] += val1 * val;
						//val -= _meanVal[j];
						//val1 = val*val*val;
						//_skewVal[j] += val1;
						//_kurtosisVal[j] += val1 * val;
					}
				}// end for index
			}// end of Mask BYTE
			if (mask.getTypeString().equalsIgnoreCase("INT")) {
				for (index = 0, idx = 0; index < image.getSize(); index += image.getNumBands(), idx++) {
					maskVal = mask.getInt(idx);
					foundLabel = -1;
					for (i = 0; i < numLabels; i++) {
						if (maskVal == lut[i]) {
							foundLabel = i;
							i = numLabels + 10;
						}
					}
					if (i != numLabels + 11) {
						System.out.println("ERROR: did not find a match for a label = " + maskVal + " at idx=" + idx);
						continue;
					}

					// set the index to the mean parameter
					i = foundLabel * res.getNumBands() + 2;
					// set the index to the skew parameter
					idxOut = i + (image.getNumBands() << 1);

					for (band = 0; band < image.getNumBands(); band++) {
						val = image.getByte(index + band);
						if (val < 0) {
							val += _lim.MAXPOS_BYTE;
						}
						val -= res.getFloat(i + band);
						val1 = val * val * val;
						//res.imageFloat[idxOut + band ] += val1;
						resArray[idxOut + band] += val1;
						//res.imageFloat[idxOut + band + image.getNumBands() ] += val1*val;
						resArray[idxOut + band + image.getNumBands()] += val1 * val;
						//val -= _meanVal[j];
						//val1 = val*val*val;
						//_skewVal[j] += val1;
						//_kurtosisVal[j] += val1 * val;
					}
				}// end for index
			}// end of Mask INT

			// compute final skew and kurtosis feature values
			for (index = 2; index < res.getSize(); index += res.getNumBands()) {
				//if(res.getFloat(index-1) > 0.0 ){
				if (resArray[index - 1] > 0.0) {
					//skew = x^3/size/stdev^3
					for (band = 0; band < image.getNumBands(); band++) {
						// skew
						//val = _countSamp[j];
						//val1 = _stdevVal[j] * _stdevVal[j];
						//div = val * val1 * _stdevVal[j];
						idxOut = index + band + image.getNumBands();
						//val = res.getFloat(index-1); // size
						val = resArray[index - 1]; // size
						//val1 = res.getFloat(index+band+image.getNumBands()] * res.getFloat(index+band+image.getNumBands()];
						//val1 = res.getFloat(idxOut) * res.getFloat(idxOut);
						val1 = resArray[idxOut] * resArray[idxOut];
						//div = val * val1 * res.getFloat(idxOut);
						div = val * val1 * resArray[idxOut];

						idxOut += image.getNumBands();
						if (div != 0.0) {
							//_skewVal[j] /= div;
							//res.imageFloat[index+band+(image.getNumBands()<<1)] /= div;
							//res.imageFloat[idxOut] /= div;
							resArray[idxOut] /= div;
						} else {
							System.out.println("Warning: skew divisor is equal to zero at band= " + band);
							//res.imageFloat[index+band+(image.getNumBands()<<1)] = 0.0F;
							//res.imageFloat[idxOut] = 0.0F;
							resArray[idxOut] = 0.0F;
						}

						// kurtosis
						//kurtosis = x^4/size/(stdev^4) - 3.0;
						// the offset is to put Gaussian to 0
						div = val * val1 * val1;
						idxOut += image.getNumBands();
						if (div != 0.0) {
							//_kurtosisVal[j] /= div;
							//res.imageFloat[idxOut] /= div;
							resArray[idxOut] /= div;
						} else {
							System.out.println("Warning: kurtosis divisor is equal to zero at band= " + band);
							//_kurtosisVal[j] = 0.0;
							//res.imageFloat[idxOut] = 0.0F;
							resArray[idxOut] = 0.0F;
						}
						//_kurtosisVal[j] -= 3.0;
						//res.imageFloat[idxOut] -= 3.0F;
						resArray[idxOut] -= 3.0F;

					}// end of for band
				}
			}// end of for index=2

		} // end of image BYTE

		///////////////////////////////////
		// compute features for FLOAT image
		// compute mean and stdev
		//////////////////////////////////////
		if (image.getTypeString().equalsIgnoreCase("FLOAT")) {
			if (mask.getTypeString().equalsIgnoreCase("BYTE")) {
				for (index = 0, idx = 0; index < image.getSize(); index += image.getNumBands(), idx++) {
					maskVal = mask.getByte(idx);
					if (maskVal < 0) {
						maskVal += _lim.MAXPOS_BYTE;
					}
					foundLabel = -1;
					for (i = 0; i < numLabels; i++) {
						if (maskVal == lut[i]) {
							foundLabel = i;
							i = numLabels + 10;
						}
					}
					if (i != numLabels + 11) {
						System.out.println("ERROR: did not find a match for a label = " + maskVal + " at idx=" + idx);
						continue;
					}
					idxOut = foundLabel * res.getNumBands() + 1;
					//res.imageFloat[idxOut] -= 3.0F;
					resArray[idxOut] -= 3.0F;
					idxOut++;
					for (band = 0; band < image.getNumBands(); band++) {
						val = image.getFloat(index + band);
						//res.imageFloat[idxOut + band ] += val;
						resArray[idxOut + band] += val;
						//res.imageFloat[idxOut + band + image.getNumBands() ] += val*val;
						resArray[idxOut + band + image.getNumBands()] += val * val;
					}
				}// end for index
			}// end of Mask BYTE
			if (mask.getTypeString().equalsIgnoreCase("INT")) {
				for (index = 0, idx = 0; index < image.getSize(); index += image.getNumBands(), idx++) {
					maskVal = mask.getInt(idx);
					foundLabel = -1;
					for (i = 0; i < numLabels; i++) {
						if (maskVal == lut[i]) {
							foundLabel = i;
							i = numLabels + 10;
						}
					}
					if (i != numLabels + 11) {
						System.out.println("ERROR: did not find a match for a label = " + maskVal + " at idx=" + idx);
						continue;
					}
					idxOut = foundLabel * res.getNumBands() + 1;
					//res.imageFloat[idxOut ] ++;
					resArray[idxOut]++;
					idxOut++;
					for (band = 0; band < image.getNumBands(); band++) {
						val = image.getFloat(index + band);
						//res.imageFloat[idxOut + band ] += val;
						resArray[idxOut + band] += val;
						//res.imageFloat[idxOut + band + image.getNumBands() ] += val*val;
						resArray[idxOut + band + image.getNumBands()] += val * val;
					}
				}// end for index
			}// end of Mask INT

			// compute final mean and stdev feature values
			for (index = 2; index < res.getSize(); index += res.getNumBands()) {
				if (res.getFloat(index - 1) > 0.0) {
					// mean
					for (band = 0; band < image.getNumBands(); band++) {
						// mean
						//res.imageFloat[index+band] /= res.getFloat(index-1);
						resArray[index + band] /= resArray[index - 1];
						// stdev
						//res.imageFloat[index+band+image.getNumBands() ] = res.getFloat(index+band+image.getNumBands() )/res.getFloat(index-1) - res.getFloat(index+band)*res.getFloat(index+band);
						resArray[index + band + image.getNumBands()] = resArray[index + band + image.getNumBands()] / resArray[index - 1] - resArray[index + band] * resArray[index + band];
						//if(res.getFloat(index+band+image.getNumBands() )>=0){
						if (resArray[index + band + image.getNumBands()] >= 0) {
							//res.imageFloat[index+band+image.getNumBands() ] = (float)Math.sqrt( res.getFloat(index+band+image.getNumBands() ) );
							resArray[index + band + image.getNumBands()] = (float) Math.sqrt(resArray[index + band + image.getNumBands()]);
						} else {
							System.out.println("Error: stdev < 0 at index " + (index - 2) + ", band=" + band);
							//res.imageFloat[index+band+image.getNumBands() ] = 0.0F;
							resArray[index + band + image.getNumBands()] = 0.0F;
						}
					}// end of for band
				}
			}// end of for index=2

		} // end of image FLOAT
		///////////////

		// compute for FLOAT image
		// compute skew and kurtosis features
		if (image.getTypeString().equalsIgnoreCase("FLOAT")) {
			if (mask.getTypeString().equalsIgnoreCase("BYTE")) {
				for (index = 0, idx = 0; index < image.getSize(); index += image.getNumBands(), idx++) {
					maskVal = mask.getByte(idx);
					if (maskVal < 0) {
						maskVal += _lim.MAXPOS_BYTE;
					}
					foundLabel = -1;
					for (i = 0; i < numLabels; i++) {
						if (maskVal == lut[i]) {
							foundLabel = i;
							i = numLabels + 10;
						}
					}
					if (i != numLabels + 11) {
						System.out.println("ERROR: did not find a match for a label = " + maskVal + " at idx=" + idx);
						continue;
					}

					// set the index to the mean parameter
					i = foundLabel * res.getNumBands() + 2;
					// set the index to the skew parameter
					idxOut = i + (image.getNumBands() << 1);

					for (band = 0; band < image.getNumBands(); band++) {
						val = image.getFloat(index + band);
						//val -=   res.getFloat(i+band);
						val -= resArray[i + band];
						val1 = val * val * val;
						//res.imageFloat[idxOut + band ] += val1;
						resArray[idxOut + band] += val1;
						//res.imageFloat[idxOut + band + image.getNumBands() ] += val1*val;
						resArray[idxOut + band + image.getNumBands()] += val1 * val;
						//val -= _meanVal[j];
						//val1 = val*val*val;
						//_skewVal[j] += val1;
						//_kurtosisVal[j] += val1 * val;
					}
				}// end for index
			}// end of Mask BYTE
			if (mask.getTypeString().equalsIgnoreCase("INT")) {
				for (index = 0, idx = 0; index < image.getSize(); index += image.getNumBands(), idx++) {
					maskVal = mask.getInt(idx);
					foundLabel = -1;
					for (i = 0; i < numLabels; i++) {
						if (maskVal == lut[i]) {
							foundLabel = i;
							i = numLabels + 10;
						}
					}
					if (i != numLabels + 11) {
						System.out.println("ERROR: did not find a match for a label = " + maskVal + " at idx=" + idx);
						continue;
					}

					// set the index to the mean parameter
					i = foundLabel * res.getNumBands() + 2;
					// set the index to the skew parameter
					idxOut = i + (image.getNumBands() << 1);

					for (band = 0; band < image.getNumBands(); band++) {
						val = image.getFloat(index + band);
						//val -=   res.getFloat(i+band);
						val -= resArray[i + band];
						val1 = val * val * val;
						//res.imageFloat[idxOut + band ] += val1;
						resArray[idxOut + band] += val1;
						//res.imageFloat[idxOut + band + image.getNumBands() ] += val1*val;
						resArray[idxOut + band + image.getNumBands()] += val1 * val;
						//val -= _meanVal[j];
						//val1 = val*val*val;
						//_skewVal[j] += val1;
						//_kurtosisVal[j] += val1 * val;
					}
				}// end for index
			}// end of Mask INT

			// compute final skew and kurtosis feature values
			for (index = 2; index < res.getSize(); index += res.getNumBands()) {
				//if(res.getFloat(index-1) > 0.0 ){
				if (resArray[index - 1] > 0.0) {
					//skew = x^3/size/stdev^3
					for (band = 0; band < image.getNumBands(); band++) {
						// skew
						//val = _countSamp[j];
						//val1 = _stdevVal[j] * _stdevVal[j];
						//div = val * val1 * _stdevVal[j];
						idxOut = index + band + image.getNumBands();
						//val = res.getFloat(index-1); // size
						val = resArray[index - 1]; // size
						//val1 = res.getFloat(index+band+image.getNumBands()] * res.getFloat(index+band+image.getNumBands()];
						//val1 = res.getFloat(idxOut) * res.getFloat(idxOut);
						val1 = resArray[idxOut] * resArray[idxOut];
						//div = val * val1 * res.getFloat(idxOut);
						div = val * val1 * resArray[idxOut];

						idxOut += image.getNumBands();
						if (div != 0.0) {
							//_skewVal[j] /= div;
							//res.imageFloat[index+band+(image.getNumBands()<<1)] /= div;
							//res.imageFloat[idxOut] /= div;
							resArray[idxOut] /= div;
						} else {
							System.out.println("Warning: skew divisor is equal to zero at band= " + band);
							//res.imageFloat[index+band+(image.getNumBands()<<1)] = 0.0F;
							//res.imageFloat[idxOut] = 0.0F;
							resArray[idxOut] = 0.0F;
						}

						// kurtosis
						//kurtosis = x^4/size/(stdev^4) - 3.0;
						// the offset is to put Gaussian to 0
						div = val * val1 * val1;
						idxOut += image.getNumBands();
						if (div != 0.0) {
							//_kurtosisVal[j] /= div;
							//res.imageFloat[idxOut] /= div;
							resArray[idxOut] /= div;
						} else {
							System.out.println("Warning: kurtosis divisor is equal to zero at band= " + band);
							//_kurtosisVal[j] = 0.0;
							//res.imageFloat[idxOut] = 0.0F;
							resArray[idxOut] = 0.0F;
						}
						//_kurtosisVal[j] -= 3.0;
						//res.imageFloat[idxOut] -= 3.0F;
						resArray[idxOut] -= 3.0F;

					}// end of for band
				}
			}// end of for index=2

		} // end of image FLOAT
		///////////////

		//free memory
		lut = null;
		return res;
	}

	//////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////
	/**
	 * Given a mask of labels,compute the number of individual forest labels in
	 * each county, defined by the shapefile used by GeoFeatureHistDialog class
	 * to compute histogram for overlaid shapefiles on raster files the return
	 * image contains: (1) mask label, (2) number of pixels of that label, (3)
	 * the count of individual forest labels for each county The size of the
	 * returned image is numrows = 1, numcols = number of labels, data type is
	 * INT res = new ImageObject(1,numLabels,(2+numFeatures),"INT");
	 * 
	 * @param image
	 *            ImageObject = input image
	 * @param mask
	 *            ImageObject = mask image
	 * @param labelRGB
	 *            int[] = array of int labels that should be analyzed in the
	 *            mask image
	 * @param numLabelRGB
	 *            int = number of int labels to be analyzed
	 * @return ImageObject = resulting information
	 */
	public ImageObject AllStatsOverMask(ImageObject image, ImageObject mask, int[] labelRGB, int numLabelRGB) {
		if (mask == null) {
			System.out.println("Error: no labels");
			//return false;
			return null;
		}
		if (image == null) {
			System.out.println("Error: no input image");
			//return false;
			return null;
		}
		if (!image.getTypeString().equalsIgnoreCase("BYTE") && !image.getTypeString().equalsIgnoreCase("FLOAT")) {
			System.out.println("Error: other than BYTE or FLOAT input image is not supported");
			//return false;
			return null;
		}
		if (!mask.getTypeString().equalsIgnoreCase("BYTE") && !mask.getTypeString().equalsIgnoreCase("INT")) {
			System.out.println("Error: other than BYTE or INT mask image is not supported");
			//return false;
			return null;
		}

		if (mask.getNumBands() != 1) {
			System.out.println("Error: mask image sampPerPixel != 1 ");
			//return false;
			return null;
		}

		if ((image.getNumRows() != mask.getNumRows()) || (image.getNumCols() != mask.getNumCols())) {
			System.out.println("Error: mismatch in label and image size ");
			//return false;
			return null;
		}
		if ((numLabelRGB == 0) || (labelRGB == null)) {
			System.out.println("in ImStat: No labels to compute histogram");
			return null;
		}
		// CHECK!!!!!!!!!!!!!!!!
		if ((image.getNumBands() != 3) && (image.getNumBands() != 1)) {
			//System.err.println("Error: cannot compare RGB values to image with greater or lesser than 3 bands");
			System.err.println("Error: the processed image can have only one (grayscale) or three (RGB) bands");
			return null;
		}

		int index, band, i, idx, idxOut;
		long size = image.getSize();//numrows*numcols;
		// find the number of labels and max,min labels in the mask(label) image
		Histogram myHist = new Histogram();
		if (mask.getTypeString().equalsIgnoreCase("BYTE")) {
			// sets WideBin = 1.0
			//myHist.SetHistParamBYTE();
			myHist.SetNumBins(256);
			myHist.SetMaxDataVal(255.0);
			myHist.SetMinDataVal(0.0);
		} else {
			//find min and max label values
			boolean tempMaskFlag = GetIsMaskPresent();
			SetIsMaskPresent(false);
			boolean ret = MinMaxVal(mask);
			if (!ret || (GetMinVal() == null) || ((int[]) GetMinVal().getData() == null)) {
				System.out.println("ERROR: Finding MinMaxVal failed ret=" + ret + " GetMinVal().getData()");
				return null;
			}
			myHist.SetIs256Bins(false);
			myHist.SetMinDataVal((double) GetMinVal().getInt(0));
			myHist.SetMaxDataVal((double) GetMaxVal().getInt(0));
			SetIsMaskPresent(tempMaskFlag);

			// this would lead to out of memory error
			//myHist.SetMinDataVal((double)_lim.MIN_INT);
			//myHist.SetMaxDataVal((double)_lim.MAX_INT);
			myHist.SetWideBins(1.0);
		}
		//test
		myHist.PrintHistogram();

		band = 0;
		try {
			myHist.Hist(mask, band);
		} catch (Exception e) {
			logger.error("Error: His(mask,band) failed");
			return null;
		}
		myHist.Count();
		//test
		System.out.println("Test: Mask (label) image band=" + band + ", number of labels =" + myHist.GetCount());
		myHist.MinMaxHistBin();
		System.out.println("Test: minHistBin=" + myHist.GetMinHistBin() + ", maxHistBin =" + myHist.GetMaxHistBin());

		int numLabels = (int) myHist.GetCount();
		ImageObject resImg = null;
		int[] res = null;
		// dimension is equal to (LUT ID) plus (total pixel count) plus
		// the number of bands (e.g., mean for each band) times four features
		int numFeatures = numLabelRGB; // 4; Changed S

		// res = new ImageObject(1,numLabels,(2+(image.getNumBands())*numFeatures),"FLOAT"); Changed S
		//res =  new ImageObject(1,numLabels,(2+numFeatures),"INT");
		try {
			resImg = resImg.createImage(1, numLabels, (2 + numFeatures), "INT");
		} catch (Exception e) {
			logger.error("Error: createImage failed");
			return null;
		}
		for (i = 0; i < resImg.getSize(); i++) {
			resImg.set(i, 0);
		}
		res = (int[]) resImg.getData();
		int[] lut = new int[numLabels];
		int maskVal;
		index = 0;
		idx = 0;
		for (i = 0; i < myHist.GetNumBins(); i++) {
			if (myHist.GetHistData()[i] != 0) {
				//res.imageInt[index] = i + (int)myHist.GetMinDataVal();
				res[index] = i + (int) myHist.GetMinDataVal();
				lut[idx] = i + (int) myHist.GetMinDataVal();
				idx++;
				index += resImg.getNumBands();
			}
		}

		int foundLabel;
		float val, val1, div;
		int RGBIdx = 0;
		int rgbIndex = 0;
		boolean noMatch = false;
		int labelIdx = 0;
		/////////////////////////////////////////////////////
		// compute mean and stdev features
		// for BYTE image
		if (image.getTypeString().equalsIgnoreCase("BYTE")) {
			if (mask.getTypeString().equalsIgnoreCase("BYTE")) {
				for (index = 0, idx = 0; index < image.getSize(); index += image.getNumBands(), idx++) {
					maskVal = mask.getByte(idx);
					if (maskVal < 0) {
						maskVal += _lim.MAXPOS_BYTE;
					}
					foundLabel = -1;
					for (i = 0; i < numLabels; i++) {
						if (maskVal == lut[i]) {
							foundLabel = i;
							i = numLabels + 10;
						}
					}
					if (i != numLabels + 11) {
						System.out.println("ERROR: did not find a match for a label = " + maskVal + " at idx=" + idx);
						continue;
					}
					idxOut = foundLabel * resImg.getNumBands() + 1;
					//res.imageInt[idxOut ] ++; // Increment total count
					res[idxOut]++; // Increment total count

					idxOut++;

					for (RGBIdx = 0; RGBIdx < numLabelRGB; RGBIdx++) {
						rgbIndex = 0;
						noMatch = false;
						labelIdx = RGBIdx * 3;
						for (band = 0; band < image.getNumBands(); band++) {
							val = image.getByte(index + band);
							if (val < 0) {
								val += _lim.MAXPOS_BYTE;
							}
							if (val != labelRGB[labelIdx + rgbIndex]) {
								noMatch = true;
								break;
							}
							rgbIndex++;
						}// for(band = ...)
						if ((band == image.getNumBands()) && (!noMatch)) {
							//res.imageInt[idxOut + RGBIdx ] ++;
							res[idxOut + RGBIdx]++;
							break; // Found a matching pixel corresponding to a labelRGB
						} // if( (band == image.getNumBands()) && (!noMatch) )
					}// for(RGBIdx = ...)
				}// end for index
			}// end of Mask BYTE
			if (mask.getTypeString().equalsIgnoreCase("INT")) {
				for (index = 0, idx = 0; index < image.getSize(); index += image.getNumBands(), idx++) {
					maskVal = mask.getInt(idx);
					foundLabel = -1;
					for (i = 0; i < numLabels; i++) {
						if (maskVal == lut[i]) {
							foundLabel = i;
							i = numLabels + 10;
						}
					}
					if (i != numLabels + 11) {
						System.out.println("ERROR: did not find a match for a label = " + maskVal + " at idx=" + idx);
						continue;
					}
					idxOut = foundLabel * resImg.getNumBands() + 1;
					//res.imageInt[idxOut ] ++; // Increment total count
					res[idxOut]++; // Increment total count
					idxOut++;
					for (RGBIdx = 0; RGBIdx < numLabelRGB; RGBIdx++) {
						rgbIndex = 0;
						noMatch = false;
						labelIdx = RGBIdx * 3;
						for (band = 0; band < image.getNumBands(); band++) {
							val = image.getByte(index + band);
							if (val < 0) {
								val += _lim.MAXPOS_BYTE;
							}
							if (val != labelRGB[labelIdx + rgbIndex]) {
								noMatch = true;
								break;
							}
							rgbIndex++;
						}// for(band = ...)
						if ((band == image.getNumBands()) && (!noMatch)) {
							//res.imageInt[(idxOut + RGBIdx)]++;
							res[(idxOut + RGBIdx)]++;
							break; // Found a matching pixel corresponding to a labelRGB
						} // if( (band == image.getNumBands()) && (!noMatch) )
					}// for(RGBIdx = ...)
				}// end for index
			}// end of Mask INT

/*
 * Commenting out to store the number of pixels instead of concentration
 * fractions // compute final average labelRGB for all boundaries for(index =
 * 2;index< res.getSize();index+=res.getNumBands()){ if(res.getFloat(index-1] >
 * 0.0 ){ for(rgbIndex = 0; rgbIndex < (image.getNumBands() * numLabelRGB);
 * rgbIndex++){ res.imageFloat[index + rgbIndex] /= res.getFloat(index - 1]; }
 */
			/*
			 * Commenting out to store the number of pixels instead of
			 * concentration as fraction } // if(res.getFloat(index - 1] > 0.0)
			 * else{ //test System.out.println("INFO: did not find pixels with
			 * label="+((index-2)/res.getNumBands())+" at index "+index ); } }//
			 * end of for index=2
			 */

		} // end of image BYTE

		///////////////////////////////////
		// compute features for FLOAT image
		// compute mean and stdev
		//////////////////////////////////////
		if (image.getTypeString().equalsIgnoreCase("FLOAT")) {
			if (mask.getTypeString().equalsIgnoreCase("BYTE")) {
				for (index = 0, idx = 0; index < image.getSize(); index += image.getNumBands(), idx++) {
					maskVal = mask.getByte(idx);
					if (maskVal < 0) {
						maskVal += _lim.MAXPOS_BYTE;
					}
					foundLabel = -1;
					for (i = 0; i < numLabels; i++) {
						if (maskVal == lut[i]) {
							foundLabel = i;
							i = numLabels + 10;
						}
					}
					if (i != numLabels + 11) {
						System.out.println("ERROR: did not find a match for a label = " + maskVal + " at idx=" + idx);
						continue;
					}
					idxOut = foundLabel * resImg.getNumBands() + 1;
					//res.imageInt[idxOut ] ++; // increment total count
					res[idxOut]++; // increment total count
					idxOut++;
					labelIdx = 0;
					for (RGBIdx = 0; RGBIdx < numLabelRGB; RGBIdx++) {
						rgbIndex = 0;
						noMatch = false;
						labelIdx = RGBIdx * 3;
						for (band = 0; band < image.getNumBands(); band++) {
							val = image.getByte(index + band);
							if (val < 0) {
								val += _lim.MAXPOS_BYTE;
							}
							if (val != labelRGB[labelIdx + rgbIndex]) {
								noMatch = true;
								break;
							}
							rgbIndex++;
						}// for(band = ...)
						if ((band == image.getNumBands()) && (!noMatch)) {
							//res.imageInt[(idxOut + RGBIdx)]++;
							res[(idxOut + RGBIdx)]++;
							break; // Found a matching pixel corresponding to a labelRGB
						} // if( (band == image.getNumBands()) && (!noMatch) )
					}// for(RGBIdx = ...)

				}// end for index
			}// end of Mask BYTE
			if (mask.getTypeString().equalsIgnoreCase("INT")) {
				for (index = 0, idx = 0; index < image.getSize(); index += image.getNumBands(), idx++) {
					maskVal = mask.getInt(idx);
					foundLabel = -1;
					for (i = 0; i < numLabels; i++) {
						if (maskVal == lut[i]) {
							foundLabel = i;
							i = numLabels + 10;
						}
					}
					if (i != numLabels + 11) {
						System.out.println("ERROR: did not find a match for a label = " + maskVal + " at idx=" + idx);
						continue;
					}
					idxOut = foundLabel * resImg.getNumBands() + 1;
					//res.imageInt[idxOut ] ++;
					res[idxOut]++;
					idxOut++;
					for (RGBIdx = 0; RGBIdx < numLabelRGB; RGBIdx++) {
						rgbIndex = 0;
						noMatch = false;
						labelIdx = RGBIdx * 3;
						for (band = 0; band < image.getNumBands(); band++) {
							val = image.getByte(index + band);
							if (val < 0) {
								val += _lim.MAXPOS_BYTE;
							}
							if (val != labelRGB[labelIdx + rgbIndex]) {
								noMatch = true;
								break;
							}
							rgbIndex++;
						}// for(band = ...)
						if ((band == image.getNumBands()) && (!noMatch)) {
							//res.imageInt[(idxOut + RGBIdx)]++;
							res[(idxOut + RGBIdx)]++;
							break; // Found a matching pixel corresponding to a labelRGB
							//res.imageFloat[idxOut + band + image.getNumBands() ] += val*val;
						} // if( (band == image.getNumBands()) && (!noMatch) )
					}// for(RGBIdx = ...)

				}// end for index
			}// end of Mask INT
/*
 * // compute final average RGB concentration for each boundary for(index =
 * 2;index< res.getSize();index+=res.getNumBands()){ if(res.getFloat(index-1] >
 * 0.0 ){ for(rgbIndex = 0; rgbIndex < (image.getNumBands() * numLabelRGB);
 * rgbIndex++){ res.imageFloat[index + rgbIndex] /= res.getFloat(index - 1]; } //
 * for(rgbIndex = 0; rgbIndex < numLabelRGB; rgbIndex++) }//
 * if(res.getFloat(index-1] > 0.0 )
 */

			/*
			 * }// end of for index=2
			 */
		} // end of image FLOAT

		//free memory
		lut = null;
		return resImg;
	}// ImageObject AllStatsOverMask(ImageObject image, ImageObject mask, int[] labelRGB, int numLabelRGB)

	/////////////////////////////////////////////////////////
	/**
	 * determine the number of labels in a mask setup the LUT (labels in LUT,
	 * and number of LUT entries)
	 * 
	 * 
	 * @param mask
	 *            ImageObject = mask image
	 * @return boolean = success of operation
	 */
	private boolean FindLUT(ImageObject mask) {

		// find the number of labels and max,min labels in the mask(label) image
		Histogram myHist = new Histogram();
		if (mask.getTypeString().equalsIgnoreCase("BYTE")) {
			// sets WideBin = 1.0
			//myHist.SetHistParamBYTE();
			myHist.SetNumBins(256);
			myHist.SetMinDataVal(0.0);
			myHist.SetMaxDataVal(255.0);
		} else {
			//find min and max label values
			boolean tempMaskFlag = GetIsMaskPresent();
			SetIsMaskPresent(false);
			boolean ret = MinMaxVal(mask);
			if (!ret || (GetMinVal() == null) || ((int[]) GetMinVal().getData() == null)) {
				System.out.println("ERROR: Finding MinMaxVal failed ret=" + ret + " GetMinVal().imageInt=" + GetMinVal().getData());
				return false;
			}
			myHist.SetIs256Bins(false);
			myHist.SetMinDataVal((double) GetMinVal().getInt(0));
			myHist.SetMaxDataVal((double) GetMaxVal().getInt(0));
			SetIsMaskPresent(tempMaskFlag);

			// this would lead to out of memory error
			//myHist.SetMinDataVal((double)_lim.MIN_INT);
			//myHist.SetMaxDataVal((double)_lim.MAX_INT);
			myHist.SetWideBins(1.0);
		}
		//test
		myHist.PrintHistogram();

		//band = 0;
		try {
			myHist.Hist(mask, 0);
		} catch (Exception e) {
			logger.error("ERROR: HIst(mask,0) faiiled");
			return false;
		}
		myHist.Count();
		//test
		//System.out.println("Test: Mask (label) image band="+band+", number of labels ="+ myHist.GetCount());
		myHist.MinMaxHistBin();
		System.out.println("Test: minHistBin=" + myHist.GetMinHistBin() + ", maxHistBin =" + myHist.GetMaxHistBin());

		_numLabelsInLUT = (int) myHist.GetCount();

		_labelsInLUT = new int[_numLabelsInLUT];
		int i, idx = 0;
		for (i = 0; i < myHist.GetNumBins(); i++) {
			if (myHist.GetHistData()[i] != 0) {
				_labelsInLUT[idx] = i + (int) myHist.GetMinDataVal();
				idx++;
			}
		}
		//test
		//for(i=0;i<_numLabelsInLUT;i++){
		//  System.out.println("Test: _labelsInLUT["+i+"]="+_labelsInLUT[i]);
		//}
		return true;

	}

	/**
	 * computes mean and stdev over a mask and for all labels and for all bands
	 * forms _meanTable and _stdevTable
	 * 
	 * @param image
	 *            ImageObject = input image
	 * @param mask
	 *            ImageObject = mask image
	 * @return boolean = success of operation
	 */
	public boolean MeanStdevTable(ImageObject image, ImageObject mask) {
		if (mask == null) {
			System.out.println("Error: no labels");
			return false;
		}
		if (image == null) {
			System.out.println("Error: no input image");
			return false;
		}
/*
 * if (!image.getTypeString().equalsIgnoreCase("BYTE") &&
 * !image.getTypeString().equalsIgnoreCase("FLOAT")) {
 * System.out.println("Error: other than BYTE or FLOAT input image is not
 * supported"); return false; }
 */
		if (!mask.getTypeString().equalsIgnoreCase("BYTE") && !mask.getTypeString().equalsIgnoreCase("INT")) {
			System.out.println("Error: other than BYTE or INT mask image is not supported");
			return false;
		}

		if (mask.getNumBands() != 1) {
			System.out.println("Error: mask image sampPerPixel != 1 ");
			return false;
		}
		if ((image.getNumRows() != mask.getNumRows()) || (image.getNumCols() != mask.getNumCols())) {
			System.out.println("Error: mismatch in label and image size ");
			return false;
		}

		// establish the LUT
		if (!FindLUT(mask)) {
			System.out.println("ERROR: could not compute a look up table");
			_meanTable = null;
			_stdevTable = null;
			return false;
		}

		if ((_countTable == null) || (_countTable.length != _numLabelsInLUT)) {
			//_countTable = new ImageObject(1,_numLabelsInLUT,1,"LONG");
			_countTable = new long[_numLabelsInLUT];
		}
		if ((_meanTable == null) || (_meanTable.length != _numLabelsInLUT * image.getNumBands())) {
			//_meanTable = new ImageObject(1,_numLabelsInLUT,image.getNumBands(),"FLOAT");
			_meanTable = new float[_numLabelsInLUT * image.getNumBands()];
		}
		if ((_stdevTable == null) || (_stdevTable.length != _numLabelsInLUT * image.getNumBands())) {
			//_stdevTable = new ImageObject(1,_numLabelsInLUT,image.getNumBands(),"FLOAT");
			_stdevTable = new float[_numLabelsInLUT * image.getNumBands()];
		}

		int i, j;

		int foundLabel;
		double val, val1, div;
		int index, band, idx, idxOut;
		int maskVal;

		//init fields
		for (i = 0; i < _countTable.length; i++) {
			_countTable[i] = 0;
		}
		for (i = 0; i < _meanTable.length; i++) {
			_meanTable[i] = 0;
			_stdevTable[i] = 0;
		}

		/////////////////////////////////////////////////////
		// compute mean and stdev features
		if (mask.getTypeString().equalsIgnoreCase("BYTE")) {
			for (index = 0, idx = 0; index < image.getSize(); index += image.getNumBands(), idx++) {
				maskVal = mask.getByte(idx);
				if (maskVal < 0) {
					maskVal += _lim.MAXPOS_BYTE;
				}
				foundLabel = -1;
				for (i = 0; i < _numLabelsInLUT; i++) {
					if (maskVal == _labelsInLUT[i]) {
						foundLabel = i;
						i = _numLabelsInLUT + 10;
					}
				}
				if (i != _numLabelsInLUT + 11) {
					System.out.println("ERROR: did not find a match for a label = " + maskVal + " at idx=" + idx);
					continue;
				}
				//idxOut = foundLabel * countIm.getNumBands() +1;
				_countTable[foundLabel]++;
				//idxOut = foundLabel * _meanTable.getNumBands();
				idxOut = foundLabel * image.getNumBands();
				for (band = 0; band < image.getNumBands(); band++) {
					//val = image.getByte(index + band);
					val = image.getDouble(index + band);
					switch (image.getType()) {
					case ImageObject.TYPE_BYTE:
						if (val < 0) {
							val += _lim.MAXPOS_BYTE;
						}
						break;
					case ImageObject.TYPE_USHORT:
						if (val < 0) {
							val += _lim.MAXPOS_SHORT;
						}
						break;
					default:
						break;
					}
					_meanTable[idxOut + band] += val;
					_stdevTable[idxOut + band] += val * val;
				}
			}// end for index
		}// end of Mask BYTE
		if (mask.getTypeString().equalsIgnoreCase("INT")) {
			for (index = 0, idx = 0; index < image.getSize(); index += image.getNumBands(), idx++) {
				maskVal = mask.getInt(idx);
				foundLabel = -1;
				for (i = 0; i < _numLabelsInLUT; i++) {
					if (maskVal == _labelsInLUT[i]) {
						foundLabel = i;
						i = _numLabelsInLUT + 10;
					}
				}
				if (i != _numLabelsInLUT + 11) {
					System.out.println("ERROR: did not find a match for a label = " + maskVal + " at idx=" + idx);
					continue;
				}
				//idxOut = foundLabel * res.getNumBands() +1;
				_countTable[foundLabel]++;
				//idxOut++;
				//idxOut = foundLabel * _meanTable.getNumBands();
				idxOut = foundLabel * image.getNumBands();
				for (band = 0; band < image.getNumBands(); band++) {
					//val = image.getByte(index + band);
					val = image.getDouble(index + band);
					switch (image.getType()) {
					case ImageObject.TYPE_BYTE:
						if (val < 0) {
							val += _lim.MAXPOS_BYTE;
						}
						break;
					case ImageObject.TYPE_USHORT:
						if (val < 0) {
							val += _lim.MAXPOS_SHORT;
						}
						break;
					default:
						break;
					}
					_meanTable[idxOut + band] += val;
					_stdevTable[idxOut + band] += val * val;
				}
			}// end for index
		}// end of Mask INT

		// compute final mean and stdev feature values

		for (i = 0, index = 0; index < _meanTable.length; index += image.getNumBands(), i++) {
			if (_countTable[i] > 0.0) {
				// mean
				for (band = 0; band < image.getNumBands(); band++) {
					// mean
					_meanTable[index + band] /= _countTable[i];
					// stdev
					_stdevTable[index + band] = _stdevTable[index + band] / _countTable[i] - _meanTable[index + band] * _meanTable[index + band];
					if (_stdevTable[index + band] >= 0) {
						_stdevTable[index + band] = (float) Math.sqrt(_stdevTable[index + band]);
					} else {
						System.out.println("Error: stdev < 0 at index " + index + ",i=" + i + ", band=" + band);
						_stdevTable[index + band] = 0.0F;
					}
				}// end of for band
			}
		}// end of for index=0

		return true;
	}

	////////////////////////////////////////////////////
	///////////////////////////////////////////////////////
	/**
	 * computes mean and stdev over a mask and for all labels and for all bands
	 * forms _meanTable and _stdevTable stored as float arrays !!
	 * 
	 * @param mask
	 *            ImageObject = input mask image
	 * @return ImageObject = reesulting image representation of tabular mean and
	 *         stdev results
	 */
	public ImageObject ImageTable2Image(ImageObject mask) {
		if (_meanTable == null) {
			logger.error("ERROR: missing meanTable");
			return null;
		}
		if (_numLabelsInLUT <= 0) {
			logger.error("ERROR:  LUT <=0 ");
			return null;
		}
		if (_labelsInLUT == null) {
			logger.error("ERROR: LUT labels are  missing ");
			return null;
		}
		int numBands = (int) _meanTable.length / _numLabelsInLUT;
		if (_numLabelsInLUT != numBands) {
			logger.error("ERROR: the computed  LUT does not match the size of _meanTable; LUT size= " + _numLabelsInLUT + ", numBands in meanTable=" + numBands);
			return null;
		}

		if (mask == null) {
			logger.error("Error: no labels");
			return null;
		}
		if (!mask.getTypeString().equalsIgnoreCase("BYTE") && !mask.getTypeString().equalsIgnoreCase("INT")) {
			logger.error("Error: other than BYTE or INT mask image is not supported");
			return null;
		}
		if (mask.getNumBands() != 1) {
			logger.error("Error: mask image sampPerPixel != 1 ");
			return null;
		}

		ImageObject retImage = null;
		try {
			retImage = retImage.createImage(mask.getNumRows(), mask.getNumCols(), numBands, "FLOAT");
		} catch (Exception e) {
			logger.error("Error: could not createImage ");
			return null;
		}
		int idxMask, idxTable, idx, i, k;
		int maskVal, foundLabel;
		boolean signal = true;
		if (mask.getTypeString().equalsIgnoreCase("BYTE")) {
			idx = 0;
			for (idxMask = 0; idxMask < mask.getSize(); idxMask++) {
				maskVal = mask.getByte(idxMask);
				foundLabel = -1;
				for (i = 0; i < _numLabelsInLUT; i++) {
					if (maskVal == _labelsInLUT[i]) {
						foundLabel = i;
						i = _numLabelsInLUT + 10;
					}
				}
				if (i != _numLabelsInLUT + 11) {
					System.out.println("ERROR: did not find a match for a label = " + maskVal + " at idxMask=" + idxMask);
					continue;
				}
				idxTable = foundLabel * numBands;
				for (k = 0; k < numBands; k++) {
					retImage.set(idx + k, _meanTable[idxTable + k]);
				}
				idx += numBands;
			}// end of for(idxMask)

			signal = false;
		}// end of BYTE type
		if (signal && mask.getTypeString().equalsIgnoreCase("INT")) {
			idx = 0;
			for (idxMask = 0; idxMask < mask.getSize(); idxMask++) {
				maskVal = mask.getInt(idxMask);
				foundLabel = -1;
				for (i = 0; i < _numLabelsInLUT; i++) {
					if (maskVal == _labelsInLUT[i]) {
						foundLabel = i;
						i = _numLabelsInLUT + 10;
					}
				}
				if (i != _numLabelsInLUT + 11) {
					System.out.println("ERROR: did not find a match for a label = " + maskVal + " at idxMask=" + idxMask);
					continue;
				}
				idxTable = foundLabel * numBands;
				for (k = 0; k < numBands; k++) {
					retImage.set(idx + k, _meanTable[idxTable + k]);
				}
				idx += numBands;
			}// end of for(idxMask)

		}// end of INT type

		return (retImage);
	}// end of method mageTable2Image

	////////////////////////////////////////////////////////////////////
	/**
	 * determine PDF model based on skew and kurtosis values
	 * 
	 * @param skew
	 *            double = input skew
	 * @param kurtosis
	 *            double = input kurtosis
	 * @return String = PDF model
	 */
	public String SelectDistribution(double skew, double kurtosis) {
		String retStr = "test ";
		//sanity check
		if (kurtosis < -3.0) {
			System.out.println("Error: kurtosis has a wrong value");
			return retStr;
		}

		double hor = skew * skew;
		double vert = kurtosis + 3.0;

		// explore  special case, such as, Uniform, Gaussian and Exponential distributions
		if ((Math.abs(vert - 1.75) <= 0.1) && (hor <= 0.1)) {
			retStr = _strDist[0];//"Uniform Distribution";
			return retStr;
		}
		if ((Math.abs(vert - 3.0) <= 0.1) && (hor <= 0.1)) {
			retStr = _strDist[1];//"Normal Distribution";
			return retStr;
		}
		if ((Math.abs(vert - 9.0) <= 0.1) && (Math.abs(hor - 4.0) <= 0.1)) {
			retStr = _strDist[2];//"Exponential Distribution";
			return retStr;
		}

		//point under test
		ImPoint ptsTest = new ImPoint(vert, hor);

		//there are 5 lines that divide the prob.distribution plane
		ImLine[] divLine = new ImLine[5];

		// define first line
		ImPoint pts1 = new ImPoint(1.0, 0.0);
		ImPoint pts2 = new ImPoint(5.0, 4.0);
		divLine[0] = new ImLine(pts1, pts2);
		//test
		//divLine[0].PrintImLine();
		// define 2nd line
		pts1.SetImPoint(1.75, 0.0);
		pts2.SetImPoint(6.0, 4.0);
		divLine[1] = new ImLine(pts1, pts2);
		//test
		//divLine[1].PrintImLine();

		pts1.SetImPoint(1.75, 0.0);
		pts2.SetImPoint(9.0, 4.0);
		divLine[2] = new ImLine(pts1, pts2);

		pts1.SetImPoint(3.0, 0.0);
		pts2.SetImPoint(9.0, 4.0);
		divLine[3] = new ImLine(pts1, pts2);

		pts1.SetImPoint(3.0, 0.0);
		pts2.SetImPoint(10.0, 3.625);
		divLine[4] = new ImLine(pts1, pts2);
		//GeomObject myGeom = new GeomObject();
		GeomOper myGeom = new GeomOper();

		// the right upper corner, p.89, Fig.5-2,
		ImPoint ptsHalfspace = new ImPoint(1.0, 4.0);

		//test
		//System.out.println("Test: ImPoints: halfspace and test");
		//ptsHalfspace.PrintImPoint();
		//ptsTest.PrintImPoint();

		int i;
		for (i = 0; i < 5; i++) {
			if (myGeom.isPtsInHalfspace(divLine[i], ptsHalfspace, ptsTest)) {
				switch (i) {
				case 0:
					retStr = _strDist[3];//"Impossible Distribution";
					i = 10;
					break;
				case 1:
					retStr = _strDist[4];//"Beta (U-Shaped) Distribution";
					i = 10;
					break;
				case 2:
					retStr = _strDist[5];//"Beta (J-Shaped) Distribution";
					i = 10;
					break;
				case 3:
					retStr = _strDist[6];//"Beta Distribution";
					i = 10;
					break;
				case 4:
					retStr = _strDist[7];//"Lognormal (Gamma, Weibull) Distribution";
					i = 10;
					break;
				default:
					retStr = _strDist[8];//"Student's t-Distribution";
					break;
				}

			}
		}
		//test
		//System.out.println("Test:i="+i);
		if (i != 11) {
			retStr = "Student's t-Distribution";
		}

		return retStr;
	}

	/**
	 * convert string reporesentation of distribution type to a number
	 * 
	 * @param dist
	 *            String = input string of PDF model
	 * @return int = converted int representation
	 */
	public int StringDist2Int(String dist) {
		if (dist == null) {
			System.out.println("ERROR: string = null");
			return -1;
		}
		int i;
		for (i = 0; i < _numDistTypes; i++) {
			if (dist.equalsIgnoreCase(_strDist[i])) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * convert int representation of distribution type to a string
	 * 
	 * @param dist
	 *            int = int representtaion of PDF model
	 * @return String = string representation of PDF model
	 */
	public String IntDist2String(int dist) {
		if ((dist < 0) || (dist >= _numDistTypes)) {
			System.out.println("ERROR: int representation of dist is out of bounds = " + dist);
			return null;
		}
		return _strDist[dist];
	}

	/**
	 * compute CrossMean E( im1[idx] * im2[idx] )
	 * 
	 * @param imo
	 *            ImageObject = input image
	 * @param band1
	 *            int = 1st band
	 * @param band2
	 *            int = 2nd band
	 * @return double = expected value of two bands for cross corelation
	 *         computation
	 */
	public double CrossMeanVal(ImageObject imo, int band1, int band2) {
		//sanity check
		if (imo == null) {
			System.out.println("Error: no image data");
			return 0.0;
		}
		if ((band1 < 0) || (band2 < 0) || (band1 >= imo.getNumBands()) || (band2 >= imo.getNumBands())) {
			System.out.println("Error: band index is out bounds");
			return 0.0;
		}
		int i;
		if (_isMaskPresent) {
			if ((_maskOrig == null) || ((_maskOrig.getData() == null) && ((int[]) _maskOrig.getData() == null)) || (_maskOrig.getNumBands() != 1)) {
				System.out.println("Error: no mask data or mask.getNumBands() != 1");
				return 0.0;
			}
			if ((_maskOrig.getNumRows() != imo.getNumRows()) || (_maskOrig.getNumCols() != imo.getNumCols())) {
				System.out.println("Error: mismatch of mask and image dimensions");
				return 0.0;
			}
		}
		String sampType = imo.getTypeString();
		int j, idx, idxMask;
		boolean signal = true;
		double val1, val2;

		// init values
		double crossMean = 0.0;
		int count = 0;
		//treated as unsigned byte converted from java signed byte
		if (sampType.equalsIgnoreCase("BYTE")) {
			if (_isMaskPresent) {
				// mask is present
				if (_maskOrig.getTypeString().equalsIgnoreCase("BYTE")) {
					for (idxMask = 0, idx = 0; idx < imo.getSize(); idx += imo.getNumBands(), idxMask++) {
						if (_maskOrig.getByte(idxMask) == _maskValByte) {
							val1 = imo.getByte(idx + band1);
							if (val1 < 0) {
								val1 += _lim.MAXPOS_BYTE;
							}
							val2 = imo.getByte(idx + band2);
							if (val2 < 0) {
								val2 += _lim.MAXPOS_BYTE;
							}

							val1 *= val2;
							crossMean += val1;
							count++;
						}
					}
				}
				if (_maskOrig.getTypeString().equalsIgnoreCase("INT")) {
					for (idxMask = 0, idx = 0; idx < imo.getSize(); idx += imo.getNumBands(), idxMask++) {
						if (_maskOrig.getInt(idxMask) == _maskValInt) {
							val1 = imo.getByte(idx + band1);
							if (val1 < 0) {
								val1 += _lim.MAXPOS_BYTE;
							}
							val2 = imo.getByte(idx + band2);
							if (val2 < 0) {
								val2 += _lim.MAXPOS_BYTE;
							}

							val1 *= val2;
							crossMean += val1;
							count++;
						}
					}
				}

			} else {
				for (idx = 0; idx < imo.getSize(); idx += imo.getNumBands()) {
					val1 = imo.getByte(idx + band1);
					if (val1 < 0) {
						val1 += _lim.MAXPOS_BYTE;
					}
					val2 = imo.getByte(idx + band2);
					if (val2 < 0) {
						val2 += _lim.MAXPOS_BYTE;
					}

					val1 *= val2;
					crossMean += val1;
				}
				count = imo.getNumRows() * imo.getNumCols();
			}
			signal = false;
		}
		if (signal && sampType.equalsIgnoreCase("SHORT")) {
			if (_isMaskPresent) {
				// mask is present
				if (_maskOrig.getTypeString().equalsIgnoreCase("BYTE")) {
					for (idxMask = 0, idx = 0; idx < imo.getSize(); idx += imo.getNumBands(), idxMask++) {
						if (_maskOrig.getByte(idxMask) == _maskValByte) {
							val1 = imo.getShort(idx + band1);
							val2 = imo.getShort(idx + band2);
							if (val1 < 0) {
								val1 += _lim.MAXPOS_SHORT;
							}
							if (val2 < 0) {
								val2 += _lim.MAXPOS_SHORT;
							}

							//val1 = imo.getShort(idx+band1] * imo.getShort(idx+band2];
							val1 *= val2;
							crossMean += val1;
							count++;
						}
					}
				}
				if (_maskOrig.getTypeString().equalsIgnoreCase("INT")) {
					for (idxMask = 0, idx = 0; idx < imo.getSize(); idx += imo.getNumBands(), idxMask++) {
						if (_maskOrig.getInt(idxMask) == _maskValInt) {
							val1 = imo.getShort(idx + band1);
							val2 = imo.getShort(idx + band2);
							if (val1 < 0) {
								val1 += _lim.MAXPOS_SHORT;
							}
							if (val2 < 0) {
								val2 += _lim.MAXPOS_SHORT;
							}

							//val1 = imo.getShort(idx+band1] * imo.getShort(idx+band2];
							val1 *= val2;
							crossMean += val1;
							count++;
						}
					}
				}

			} else {
				for (idx = 0; idx < imo.getSize(); idx += imo.getNumBands()) {
					val1 = imo.getShort(idx + band1);
					val2 = imo.getShort(idx + band2);
					if (val1 < 0) {
						val1 += _lim.MAXPOS_SHORT;
					}
					if (val2 < 0) {
						val2 += _lim.MAXPOS_SHORT;
					}

					//val1 = imo.getShort(idx+band1] * imo.getShort(idx+band2];
					val1 *= val2;
					crossMean += val1;
				}
				count = imo.getNumRows() * imo.getNumCols();
			}
			signal = false;
		}
		if (signal && sampType.equalsIgnoreCase("INT")) {
			if (_isMaskPresent) {
				// mask is present
				if (_maskOrig.getTypeString().equalsIgnoreCase("BYTE")) {
					for (idxMask = 0, idx = 0; idx < imo.getSize(); idx += imo.getNumBands(), idxMask++) {
						if (_maskOrig.getByte(idxMask) == _maskValByte) {
							val1 = imo.getInt(idx + band1);
							val2 = imo.getInt(idx + band2);
							val1 *= val2;
							crossMean += val1;
							count++;
						}
					}
				}
				if (_maskOrig.getTypeString().equalsIgnoreCase("INT")) {
					for (idxMask = 0, idx = 0; idx < imo.getSize(); idx += imo.getNumBands(), idxMask++) {
						if (_maskOrig.getInt(idxMask) == _maskValInt) {
							val1 = imo.getInt(idx + band1);
							val2 = imo.getInt(idx + band2);
							val1 *= val2;
							crossMean += val1;
							count++;
						}
					}
				}

			} else {
				for (idx = 0; idx < imo.getSize(); idx += imo.getNumBands()) {
					val1 = imo.getInt(idx + band1);
					val2 = imo.getInt(idx + band2);
					val1 *= val2;
					crossMean += val1;
				}
				count = imo.getNumRows() * imo.getNumCols();
			}
			signal = false;
		}
		if (signal && sampType.equalsIgnoreCase("LONG")) {
			if (_isMaskPresent) {
				// mask is present
				if (_maskOrig.getTypeString().equalsIgnoreCase("BYTE")) {
					for (idxMask = 0, idx = 0; idx < imo.getSize(); idx += imo.getNumBands(), idxMask++) {
						if (_maskOrig.getByte(idxMask) == _maskValByte) {
							val1 = imo.getLong(idx + band1);
							val2 = imo.getLong(idx + band2);
							val1 *= val2;
							crossMean += val1;
							count++;
						}
					}
				}
				if (_maskOrig.getTypeString().equalsIgnoreCase("INT")) {
					for (idxMask = 0, idx = 0; idx < imo.getSize(); idx += imo.getNumBands(), idxMask++) {
						if (_maskOrig.getInt(idxMask) == _maskValInt) {
							val1 = imo.getLong(idx + band1);
							val2 = imo.getLong(idx + band2);
							val1 *= val2;
							crossMean += val1;
							count++;
						}
					}
				}

			} else {
				for (idx = 0; idx < imo.getSize(); idx += imo.getNumBands()) {
					val1 = imo.getLong(idx + band1);
					val2 = imo.getLong(idx + band2);
					val1 *= val2;
					crossMean += val1;
				}
				count = imo.getNumRows() * imo.getNumCols();
			}
			signal = false;
		}
		if (signal && sampType.equalsIgnoreCase("FLOAT")) {
			if (_isMaskPresent) {
				// mask is present
				if (_maskOrig.getTypeString().equalsIgnoreCase("BYTE")) {
					for (idxMask = 0, idx = 0; idx < imo.getSize(); idx += imo.getNumBands(), idxMask++) {
						if (_maskOrig.getByte(idxMask) == _maskValByte) {
							val1 = imo.getFloat(idx + band1);
							val2 = imo.getFloat(idx + band2);
							val1 *= val2;
							crossMean += val1;
							count++;
						}
					}
				}
				if (_maskOrig.getTypeString().equalsIgnoreCase("INT")) {
					for (idxMask = 0, idx = 0; idx < imo.getSize(); idx += imo.getNumBands(), idxMask++) {
						if (_maskOrig.getInt(idxMask) == _maskValInt) {
							val1 = imo.getFloat(idx + band1);
							val2 = imo.getFloat(idx + band2);
							val1 *= val2;
							crossMean += val1;
							count++;
						}
					}
				}
			} else {
				for (idx = 0; idx < imo.getSize(); idx += imo.getNumBands()) {
					val1 = imo.getFloat(idx + band1);
					val2 = imo.getFloat(idx + band2);
					val1 *= val2;
					crossMean += val1;
				}
				count = imo.getNumRows() * imo.getNumCols();
			}
			signal = false;
		}
		if (signal && sampType.equalsIgnoreCase("DOUBLE")) {
			if (_isMaskPresent) {
				// mask is present
				if (_maskOrig.getTypeString().equalsIgnoreCase("BYTE")) {
					for (idxMask = 0, idx = 0; idx < imo.getSize(); idx += imo.getNumBands(), idxMask++) {
						if (_maskOrig.getByte(idxMask) == _maskValByte) {
							val1 = imo.getDouble(idx + band1);
							val2 = imo.getDouble(idx + band2);
							val1 *= val2;
							crossMean += val1;
							count++;
						}
					}
				}
				if (_maskOrig.getTypeString().equalsIgnoreCase("INT")) {
					for (idxMask = 0, idx = 0; idx < imo.getSize(); idx += imo.getNumBands(), idxMask++) {
						if (_maskOrig.getInt(idxMask) == _maskValInt) {
							val1 = imo.getDouble(idx + band1);
							val2 = imo.getDouble(idx + band2);
							val1 *= val2;
							crossMean += val1;
							count++;
						}
					}
				}

			} else {
				for (idx = 0; idx < imo.getSize(); idx += imo.getNumBands()) {
					val1 = imo.getDouble(idx + band1);
					val2 = imo.getDouble(idx + band2);
					val1 *= val2;
					crossMean += val1;
				}
				count = imo.getNumRows() * imo.getNumCols();
			}
			signal = false;
		}

		if (signal) {
			System.out.println("Error:CrossMean no sampType=" + sampType + " is supported");
			return 0.0;
		}

		// compute final values
		if (count > 0) {
			crossMean /= count;
		} else {
			System.out.println("Error: numSamp <=0 " + count);
			crossMean = 0.0;
		}

		return crossMean;
	}

	////////////////////////////////
	/**
	 * display values
	 * 
	 */
	public void PrintImStats() {
		if (_minVal != null) {
			System.out.println("ImStats Info :minVal");
			System.out.println("Test: _minVal information=" + _minVal.toString() + ",val[0]=" + _minVal.getDouble(0));
		}
		if (_maxVal != null) {
			System.out.println("ImStats Info :maxVal");
			System.out.println("Test: _maxVal information=" + _maxVal.toString() + ",val[0]=" + _maxVal.getDouble(0));
		}
		if (_meanVal != null) {
			System.out.println("ImStats Info :meanVal");
			PrintArrayInfo(_meanVal);
		}
		if (_stdevVal != null) {
			System.out.println("ImStats Info :stdevVal");
			PrintArrayInfo(_stdevVal);
		}
		if (_skewVal != null) {
			System.out.println("ImStats Info :skewVal");
			PrintArrayInfo(_skewVal);
		}
		if (_kurtosisVal != null) {
			System.out.println("ImStats Info :kurtosisVal");
			PrintArrayInfo(_kurtosisVal);
		}

	}

	/**
	 * debug prints
	 * 
	 * @param arr
	 *            double[]
	 */
	private void PrintArrayInfo(double[] arr) {
		System.out.println("PRINT: double array length = " + arr.length + ", first Val=" + arr[0]);
	}

	/**
	 * test print of PDF model (distribution type)
	 * 
	 */
	public void PrintDistTypes() {
		System.out.println("INFO: distributions types:");
		for (int i = 0; i < _numDistTypes; i++) {
			System.out.println("dist[" + i + "]=" + _strDist[i]);
		}
	}

}
