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
package edu.illinois.ncsa.isda.im2learn.ext.statistics;

/*
 * Histogram.java
 *
 */

import java.io.Serializable;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageException;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.im2learn.core.datatype.SubArea;

/*
 */

/**
 * <B> The class Histogram provides a tool for computing histograms of
 * two-dimensional multivariate (multi-band) images. </B> <BR>
 * <BR>
 * <B>Description:</B> A histogram is computed by clicking the button Hist. It
 * represents the frequency of occurrence for a range of image band intensities.
 * The range of image intensities is also called a histogram bin. The current
 * bins are always defined with respect to the input data type, such that there
 * are always 256 bins. While the histogram of BYTE types is defined with bin
 * width equal to one, the histogram of SHORT data types has bin width equal to
 * 256. Histograms of other data types, for example, INT or FLOAT, use bin width
 * computed from minimum and maximum data values for a fixed number of bins
 * equal to 256. <p/> <BR>
 * <p/> <img src="../../../../../../images/histogramDialog.jpg" width="195"
 * height="396"> <p/> <BR>
 * <B>Run:</B> The histogram output can be viewed in a text format and in a
 * graphical format. The tool allows to display a histogram plot of all bands at
 * once using the ShowHist button. The plot colors are assigned to each
 * histogram in the following order: <BR>
 * 1st band - red, 2nd band - green, 3rd band - blue, 4th band - yellow, 5th
 * band - purple, 6th band - cyan, 7th band - mix, other bands - black. <BR>
 * <p/> <img src="../../../../../../images/sampFern.jpg" width="303"
 * height="386"> <p/> <img src="../../../../../../images/histogramPlot.jpg"
 * width="605" height="442"> <p/> <BR>
 * It is possible to save histogram plots by going to the menu of the plot and
 * selecting "Save" or "Save As Image". The option "Save" outputs numerical
 * values of histograms into a text file. The selection "Save As Image" saves
 * the plot as an image in the tif file format. <BR>
 * <BR>
 * The button Stats computes basic statistics of each histogram. By clicking the
 * button ShowStats, all values will be displayed in the text window. The
 * statistics include for each band the following parameters: <BR>
 * mean, standard deviation, median, skew, kurtosis, mode (maximum frequency in
 * the histogram), contrast (standard deviation normalized by number of bins in
 * percents), count (number of non-empty histogram bins), minHistBin (index of
 * the smallest occupied bin), maxHistBin (index of the largest occupied bin),
 * numSamples (number of samples used for the histogram). <BR>
 * <p/> <BR>
 * In addition, this tool can compute histogram over a masked image. The mask
 * has to be a byte array with a byte value equal to -1 for a valid image pixel.
 * The file format of the image mask should be tif with one byte per pixel. <BR>
 * <BR>
 * <B> Release notes: </B> <BR>
 * The current release does not save out any text of the histogram plots in the
 * "Save As Image" mode.
 * 
 * @author Peter Bajcsy
 * @author Rob Kooper
 * @version 2.0
 */

// TODO this class needs some rewriting.
////////////////////////////////////////////////////////////////////
// this class performs histogram computation
// and statistical evaluation of histogram data
// This is a histogram of a byte array !!
////////////////////////////////////////////////////////////////////
public class Histogram implements Serializable {
	// histogram related data
	protected int		_NumBins;										// num. of hist. bins
	protected double	_WideBins;										// width of hist bins
	private int[]		_HistData;

	// statistics of hist.
	protected double	_Mean;
	protected double	_SDev;
	protected double	_Median;
	protected int		_Mode;
	protected double	_Contrast;
	protected long		_Count;
	protected int		_MinHistBin;
	protected int		_MaxHistBin;
	protected double	_Skew;
	protected double	_Energy;
	protected double	_Kurtosis;
	protected long		_NumSamples;
	protected double	_Entropy;

	protected int		_UpperPercentile;
	protected int		_LowerPercentile;

	// flag for computing all statistics
	protected boolean	_AllStats;
	protected boolean	_is256Bins;

	// data related values
	protected double	_MinDataVal;
	protected double	_MaxDataVal;

	private static Log	logger	= LogFactory.getLog(Histogram.class);

	//Getters
	// Hist values
	public int GetNumBins() {
		return _NumBins;
	}

	public double GetWideBins() {
		return _WideBins;
	}

	public int GetHistDatum(int index) {
		if ((index >= 0) && (index < _NumBins)) {
			return _HistData[index];
		} else {
			logger.error("Error: GetHistDatum: index is out of bounds");
			return (-1);
		}
	}

	public int[] GetHistData() {
		return _HistData;
	}

	// Data values
	public double GetMinDataVal() {
		return _MinDataVal;
	}

	public double GetMaxDataVal() {
		return _MaxDataVal;
	}

	//statistics
	public double GetMean() {
		return _Mean;
	}

	public double GetSDev() {
		return _SDev;
	}

	public double GetMedian() {
		return _Median;
	}

	public int GetMode() {
		return _Mode;
	}

	public double GetContrast() {
		return _Contrast;
	}

	public long GetCount() {
		return _Count;
	}

	public int GetMinHistBin() {
		return _MinHistBin;
	}

	public int GetMaxHistBin() {
		return _MaxHistBin;
	}

	public double GetSkew() {
		return _Skew;
	}

	public double GetEnergy() {
		return _Energy;
	}

	public double GetKurtosis() {
		return _Kurtosis;
	}

	public long GetNumSamples() {
		return _NumSamples;
	}

	public double GetEntropy() {
		return _Entropy;
	}

	public int GetUpperPercentile() {
		return _UpperPercentile;
	}

	public int GetLowerPercentile() {
		return _LowerPercentile;
	}

	public boolean GetAllStats() {
		return _AllStats;
	}

	public boolean GetIs256Bins() {
		return _is256Bins;
	}

	public double GetNumBinsPerType(int type) {
		switch (type) {
		case ImageObject.TYPE_BYTE:
			return Math.pow(2, 8);
		case ImageObject.TYPE_SHORT:
			return Math.pow(2, 16);
		case ImageObject.TYPE_USHORT:
			return Math.pow(2, 16);
		case ImageObject.TYPE_INT:
			return Math.pow(2, 32);
		case ImageObject.TYPE_LONG:
			return Math.pow(2, 64);
		case ImageObject.TYPE_FLOAT:
			return Math.pow(2, 32);
		case ImageObject.TYPE_DOUBLE:
			return Math.pow(2, 64);
		default:
			return -1;

		}
	}

	//Setters
	public boolean SetNumBins(int numbins) {
		if (numbins <= 1) {
			logger.error("Error:Number of histogram bins has to be greater than one");
			return false;
		}
		// make sure that _MaxDataVal - _MinDataVal = _NumBins * _WideBins
		// consider for now a fixed range min and max values
		if (_MaxDataVal < _MinDataVal) {
			logger.error("Error: given numbins, width of bins is not greater than zero");
			return false;
		}
		_NumBins = numbins;
		_WideBins = (_MaxDataVal - _MinDataVal) / (_NumBins - 1);

		_HistData = null;
		_HistData = new int[_NumBins];
		return true;
	}

	public void SetAllStats(boolean flag) {
		_AllStats = flag;
	}

	public void SetIs256Bins(boolean val) {
		_is256Bins = val;
	}

	public void SetMinDataVal(double val) {
		_MinDataVal = val;
	}

	public void SetMaxDataVal(double val) {
		_MaxDataVal = val;
	}

	// widebins and numbins are related
	// setting one will modify the other as opposed to min and max data values
	public boolean SetWideBins(double val) {
		if (val <= 0) {
			logger.error("Error: histogram width of bins has to be greater than zero");
			return false;
		}
		// make sure that _MaxDataVal - _MinDataVal = _NumBins * _WideBins
		// consider for now a fixed range min and max values
		if ((int) ((_MaxDataVal - _MinDataVal) / val) <= 0) {
			logger.error("Error: given width, num of bins is not greater than zero");
			return false;
		}
		_WideBins = val;
		_NumBins = (int) ((_MaxDataVal - _MinDataVal) / _WideBins) + 1;

		//test
		logger.debug("Test: numBins=" + _NumBins);

		_HistData = null;
		_HistData = new int[_NumBins];
		return true;
	}

	//constructor
	public Histogram() {
		_NumBins = 0;
		_HistData = null;
		_Mean = _SDev = _Median = _Contrast = _Skew = _Kurtosis = _Entropy = 0.0;
		_Mode = _MinHistBin = _MaxHistBin = 0;
		_Count = 0;
		_NumSamples = 0;
		_UpperPercentile = _LowerPercentile = 0;

		_WideBins = _MinDataVal = _MaxDataVal = 0.0;
		_AllStats = false;
		_is256Bins = true;
		//SetHistParamBYTE();

		// default values
		_WideBins = (float) 1.0;
		_MinDataVal = (float) 0.0;
		_MaxDataVal = (float) 255.0;
		SetNumBins(256);// default number of bins is 256

		/*
		 * _WideBins = (float)1.0; _MinDataVal = (float)0.0; _MaxDataVal =
		 * (float)255.0; SetNumBins(256);// default number of bins is 256
		 */
	}

	/////////////////////////////////////////////////////////
	// this setter is useful for setting up histogram bins for various data type
	// the setters are in Histogram as opposed to HistObject because they use ImStats class
	// the case of data driven dynamic range
	/// int and float data types are scales as opposed to byte and short !!!
	public boolean SetHistParam256Bins(ImageObject im, int band) {
		// compute min and max data values
		SetMinDataVal(im.getMin(band));
		SetMaxDataVal(im.getMax(band));
		if (_NumBins != 256) {
			// memory allocation only if necessary
			SetNumBins(256);// default number of bins is 256
		} else {
			_WideBins = (_MaxDataVal - _MinDataVal) / _NumBins;
		}
		SetIs256Bins(false);
		return true;
	}

	public boolean SetHistParam256Bins(ImageObject im) {
		// compute min and max data values
		SetMinDataVal(im.getMin());
		SetMaxDataVal(im.getMax());
		if (_NumBins != 256) {
			// memory allocation only if necessary
			SetNumBins(256);// default number of bins is 256
		} else {
			_WideBins = (_MaxDataVal - _MinDataVal) / _NumBins;
		}
		SetIs256Bins(false);
		return true;
	}

	/**
	 * Adds data to current histogram data. It expects that size of provided
	 * data be equal to the current num of bins.
	 * 
	 * @param data
	 */
	public void addBinValues(int[] data) {
		for (int i = 0; i < GetNumBins(); i++) {
			_HistData[i] = _HistData[i] + data[i];
		}
	}

	//////////////////////////////////////////////////////////////
	// doers
	// histogram computation
	public void Hist(ImageObject data, int band) throws ImageException {
		//sanity checks
		SanityCheckInput(data, band);

		int i;
		// initialize hist bins
		for (i = 0; i < GetNumBins(); i++) {
			_HistData[i] = 0;
		}

		// compute hist bins
		for (int idx = band; idx < data.getSize(); idx += data.getNumBands()) {
			i = (int) ((data.getDouble(idx) - _MinDataVal) / _WideBins);
			if (i < 0) {
				_HistData[0]++;
			} else {
				if (i > _NumBins - 1) {
					_HistData[_NumBins - 1]++;
				} else {
					_HistData[i]++;
				}
			}
		}
	}
	
	// Same as hist, but don't count the background. For taking histogram over a mask.
	public void HistOmitBackground(ImageObject data, ImageObject mask, int band) throws ImageException {
		//sanity checks
		SanityCheckInput(data, band);

		int i;
		// initialize hist bins
		for (i = 0; i < GetNumBins(); i++) {
			_HistData[i] = 0;
		}

		// compute hist bins
		for (int idx = band; idx < data.getSize(); idx += data.getNumBands()) {
			i = (int) ((data.getDouble(idx) - _MinDataVal) / _WideBins);
			if (mask.getDouble(idx/data.getNumBands()) != 0){
				if (i < 0) {
					_HistData[0]++;
				} else {
					if (i > _NumBins - 1) {
						_HistData[_NumBins - 1]++;
					} else {
						_HistData[i]++;
					}
				}
			}
		}
	}

	// band should be between 0 and maxBand-1
	public void Hist(ImageObject data, int band, SubArea area) throws ImageException {
		//sanity checks
		SanityCheckInput(data, band);

		//area related sanity check
		if (area == null) {
			throw (new ImageException("Hist Error: subarea defined "));
		}
		if (!area.checkSubArea(0, 0, data.getNumRows(), data.getNumCols())) {
			throw (new ImageException("Hist Error: subarea is outside of image subarea=" + area.toString() + " imageDim=" + data.toString()));
		}

		int i, k, j;
		// initialize hist bins
		for (i = 0; i < GetNumBins(); i++) {
			_HistData[i] = 0;
		}

		// compute hist bins
		int idx, offset;

		//long idx, offset = ( data.numcols - area.Wide) * data.sampPerPixel;
		offset = (data.getNumCols() - area.width) * data.getNumBands();
		idx = (area.y * data.getNumCols() + area.x) * data.getNumBands() + band;
		for (k = area.y; k < area.getEndRow(); k++) {
			for (j = area.x; j < area.getEndCol(); j++) {
				i = (int) ((data.getDouble(idx) - _MinDataVal) / _WideBins);
				if (i < 0) {
					_HistData[0]++;
				} else {
					if (i > _NumBins - 1) {
						_HistData[_NumBins - 1]++;
					} else {
						_HistData[i]++;
					}
				}
				idx += data.getNumBands();
			}
			idx = idx + offset;
		}
	}

	// histogram over a masked area
	public void HistMask(ImageObject data, int band, ImageObject mask, double maskVal) throws ImageException {
		//sanity checks
		SanityCheckInput(data, band);

		// mask related sanity check
		if ((data == null) || (data.getNumBands() <= band)) {
			throw (new ImageException("Hist Error: Input data is null or band >= data.sampPerPixel"));
		}

		if ((data.getNumRows() != mask.getNumRows()) || (data.getNumCols() != mask.getNumCols())) {
			throw (new ImageException("Hist Error: Input data and mask data have different size"));
		}

		if ((mask.getNumBands() != 1) || ((mask.getType() != ImageObject.TYPE_BYTE) && (mask.getType() != ImageObject.TYPE_INT))) {
			logger.debug("Hist Error: mask data other than grayscale BYTE/INT is discouraged.");
			//return false;
		}

		int i;
		// initialize hist bins
		for (i = 0; i < GetNumBins(); i++) {
			_HistData[i] = 0;
		}
		// compute hist bins
		int idx, idxmask; // this should be long idx; !!! but java does not allow long index
		//test

		long validSamples;

		//init parameters
		validSamples = 0;
		idxmask = 0;
		for (idx = band; idx < data.getSize(); idx += data.getNumBands(), idxmask += mask.getNumBands()) {
			if (mask.getDouble(idxmask) == maskVal) {
				i = (int) ((data.getDouble(idx) - _MinDataVal) / _WideBins);
				if (i < 0) {
					_HistData[0]++;
				} else {
					if (i > _NumBins - 1) {
						_HistData[_NumBins - 1]++;
					} else {
						_HistData[i]++;
					}
				}

				validSamples++;
			}
		}
		// test
		logger.debug("Test: ValidSamples=" + validSamples);
	}

	////////////////////////////////////////////////////////////////////
	// compute statistics

	// compute all statistics at once
	public boolean Stats() {
		boolean allstats = GetAllStats();
		SetAllStats(true);
		boolean ret = true;
		ret = ret && Mean();
		ret = ret && SDev();
		ret = ret && Median();
		ret = ret && Mode();
		ret = ret && Contrast();
		ret = ret && Count();
		ret = ret && MinMaxHistBin();
		ret = ret && Skew();
		ret = ret && Energy();
		ret = ret && Kurtosis();
		ret = ret && NumSamples();
		ret = ret && Entropy();
		SetAllStats(allstats);

		return ret;
	}

	// sample mean
	public boolean Mean() {
		// sanity check
		if (_HistData == null) {
			logger.error("Hist Mean Error: No histogram data");
			return false;
		}
		long total = 0;
		double sum = 0.0;
		for (int i = 0; i < GetNumBins(); i++) {
			total += _HistData[i];
			sum += (double) i * _HistData[i];
		}
		if (total != 0) {
			_Mean = sum / total;
		} else {
			_Mean = 0.0;
		}
		return true;
	}

	// standard deviation
	public boolean SDev() {
		double total = 0.0, var = 0.0;
		if (!GetAllStats()) {
			if (Mean() != true) {
				return false;
			}
		}

		int i;
		double val;
		for (i = 0; i < GetNumBins(); i++) {
			total += _HistData[i];
			val = i - _Mean;
			var += val * val * _HistData[i];
		}
		if (total != 0.0) {
			_SDev = Math.sqrt(var / total);
		} else {
			_SDev = 0;
		}
		return true;
	}

	//////////////////////////////////////////////////////////////////////////////
	// median
	public boolean Median() {
		int i, j;
		int[] bin = new int[GetNumBins()];
		;
		double median = 0.0;

		//bin = new int[ GetNumBins()];

		for (i = 0, j = 0; i < GetNumBins(); i++) {
			if (_HistData[i] != 0) {
				bin[j] = i;
				j++;
			}
		}
		if (j != 0) {
			if (j % 2 != 0) {
				median = bin[(j + 1) / 2 - 1];
			} else {
				median = ((bin[j / 2 - 1] + bin[j / 2]) / 2.0);
			}
		}
		_Median = median;
		bin = null;
		return true;
	}

	//////////////////////////////////////////////////////////////////////////////
	// Mode
	// Computes the bin at which the peak of histogram occurs
	public boolean Mode() {
		int max = 0, mode = 0;
		for (int i = 0; i < GetNumBins(); i++) {
			if (_HistData[i] > max) {
				max = _HistData[i];
				mode = i;
			}
		}
		_Mode = mode;
		return true;
	}

	/////////////////////////////////////////////////////////////////////////////
	// Contrast
	public boolean Contrast() {
		if (!GetAllStats()) {
			if (SDev() != true) {
				return false;
			}
		}
		_Contrast = _SDev * (100.0 / (GetNumBins() / 2 - 1));
		return true;
	}

	//////////////////////////////////////////////////////////////////////////////
	// Count
	//Computes the number of nonzero hist. bins
	public boolean Count() {
		int i;
		long j;
		for (i = 0, j = 0; i < GetNumBins(); ++i) {
			if (_HistData[i] != 0) {
				j++;
			}
		}
		_Count = j;
		return true;
	}

	/////////////////////////////////////////////////////////////////////////////
	// Min and Max Bin Values
	public boolean MinMaxHistBin() {
		int i, min = -1, max = -1;
		//test
		//logger.debug("TEST inside MinMaxHistBins: numBins = " + GetNumBins() );

		for (i = 0; i < GetNumBins(); ++i) {
			if (_HistData[i] != 0) {
				min = i;
				break;
			}
		}
		for (i = (GetNumBins() - 1); i >= 0; --i) {
			if (_HistData[i] != 0) {
				max = i;
				break;
			}
		}
		if ((min == -1) || (max == -1)) {
			logger.error("Hist MinMaxBin Error: hist. bins are empty");
			return false;
		}

		_MinHistBin = min;
		_MaxHistBin = max;
		return true;
	}

	/////////////////////////////////////////////////////////////////////////////
	// Skew
	public boolean Skew() {
		double val, total = 0.0, var = 0.0;
		double skew = 0.0;
		if (GetAllStats() == false) {
			if (Mean() != true) {
				return false;
			}
			if (SDev() != true) {
				return false;
			}
		}
		for (int i = 0; i < GetNumBins(); ++i) {
			total += _HistData[i];
			val = i - _Mean;
			var += (val * val * val * _HistData[i]);
		}
		if (total != 0.0) {
			if (_SDev != 0.0) {
				skew = var / total / (_SDev * _SDev * _SDev);
			}
		}
		_Skew = skew;
		return true;
	}

	/////////////////////////////////////////////////////////////////////////////
	// Energy
	public boolean Energy() {
		double total = 0.0, energy = 0.0;
		int i;
		for (i = 0; i < GetNumBins(); ++i) {
			total += _HistData[i];
		}
		if (total != 0.0) {
			for (i = 1; i < GetNumBins(); ++i) {
				energy += _HistData[i] * _HistData[i];
			}
			energy /= (total * total);
		}
		_Energy = energy;
		return true;
	}

	//////////////////////////////////////////////////////////////////////////////
	// Kurtosis
	public boolean Kurtosis() {
		double val, total = 0.0, var = 0.0, kurtosis = 0.0;
		if (GetAllStats() == false) {
			if (Mean() != true) {
				return false;
			}
			if (SDev() != true) {
				return false;
			}
		}
		for (int i = 0; i < GetNumBins(); ++i) {
			total += _HistData[i];
			val = i - _Mean;
			var += val * val * val * val * _HistData[i];
		}
		if ((total != 0.0) && (_SDev != 0.)) {
			val = _SDev * _SDev;
			kurtosis = var / total / (val * val) - 3.0;
		}
		_Kurtosis = kurtosis;
		return true;
	}

	//////////////////////////////////////////////////////////////////////////////
	// Number of all samples
	public boolean NumSamples() {
		int i;
		long total;
		for (i = 0, total = 0; i < GetNumBins(); ++i) {
			if (_HistData[i] != 0) {
				total += _HistData[i];
			}
		}
		_NumSamples = total;
		return true;
	}

	//////////////////////////////////////////////////////////////////////////////
	// Entropy
	public boolean Entropy() {
		if (GetAllStats() == false) {
			if (NumSamples() != true) {
				return false;
			}
		}

		_Entropy = 0.0;
		double logmax = Math.log(_NumSamples);
		for (int i = 0; i < GetNumBins(); ++i) {
			if (_HistData[i] > 0) {
				_Entropy -= (_HistData[i] * (Math.log(_HistData[i]) - logmax) / _NumSamples);
			}
		}
		return true;
	}

	/////////////////////////////////////////////////////////////////////////////
	// UpperPercentile
	// returns the bin position of the histogram percentile
	public boolean UpperPercentile(double percent) {
		if ((percent < 0.0) || (percent > 1.0)) {
			logger.error("ERROR: value for the upper percentile should be in [0,1] range");
			return false;
		}
		double upperVal = 0.0, total = 0.0;
		int retVal = 0;
		int i;
		for (i = 0; i < GetNumBins(); ++i) {
			total += _HistData[i];
		}

		if (total != 0.0) {
			upperVal = total * (1.0 - percent);

			//test
			logger.debug("test; total =" + total + ",upperVal = " + upperVal);

			for (i = GetNumBins() - 1; i >= 0; --i) {
				retVal = i;
				if (upperVal >= total) {
					i = -10;
					retVal++;
					if (retVal >= GetNumBins()) {
						retVal--;
					}
				} else {
					total -= _HistData[i];
				}
			}
		}
		_UpperPercentile = retVal;
		//test
		logger.debug("test; _UpperPercentile = " + _UpperPercentile);

		return true;
	}

	/////////////////////////////////////////////////////////////////////////////
	// LowerPercentile
	// returns the bin position of the histogram percentile
	public boolean LowerPercentile(double percent) {
		if ((percent < 0.0) || (percent > 1.0)) {
			logger.error("ERROR: value for the lower percentile should be in [0,1] range");
			return false;
		}
		double lowerVal = 0.0, total = 0.0;
		int retVal = 0;
		int i;
		for (i = 0; i < GetNumBins(); ++i) {
			total += _HistData[i];
		}

		if (total != 0.0) {
			lowerVal = total * percent;
			//test
			logger.debug("test; total=" + total + ",lowerVal = " + lowerVal);

			total = 0.0;
			for (i = 0; i < GetNumBins(); ++i) {
				retVal = i;
				if (lowerVal <= total) {
					i = GetNumBins() + 10;
				} else {
					total += _HistData[i];
				}
			}
		}
		_LowerPercentile = retVal;
		//test
		logger.debug("test; _LowerPercentile = " + _LowerPercentile);

		return true;
	}

	////////////////////////////////////////////////////////////////////////////
	// sanity check
	private void SanityCheckInput(ImageObject data, int band) throws ImageException {
		//sanity checks
		if (data == null) {
			throw (new ImageException("Hist Error: Input data is null"));
		}
		if (GetHistData() == null) {
			throw (new ImageException("Hist Error: Number of Bins has not been set"));
		}
		if (data.getNumBands() <= band) {
			throw (new ImageException("Hist Error: band >= getNumBands()"));
		}
	}

	/*
	 * 
	 * public void PrintHistStats(String OutFileName) throws Exception { // open
	 * the file RandomAccessFile f0; f0 = new RandomAccessFile(OutFileName,
	 * "rw");
	 * 
	 * f0.writeBytes("hist stats "); f0.writeBytes(" \n");
	 * 
	 * f0.writeBytes(" mean= "); f0.writeBytes(Float.toString((float)
	 * GetMean())); f0.writeBytes("\n stdev= ");
	 * f0.writeBytes(Float.toString((float) GetSDev()));
	 * f0.writeBytes("\n median= "); f0.writeBytes(Float.toString((float)
	 * GetMedian())); f0.writeBytes("\n skew= ");
	 * f0.writeBytes(Float.toString((float) GetSkew()));
	 * f0.writeBytes("\n kurtosis= "); f0.writeBytes(Float.toString((float)
	 * GetKurtosis())); f0.writeBytes("\n entropy= ");
	 * f0.writeBytes(Float.toString((float) GetEntropy()));
	 * 
	 * f0.writeBytes("\n mode= "); f0.writeBytes(Integer.toString(GetMode()));
	 * f0.writeBytes("\n contrast= "); f0.writeBytes(Float.toString((float)
	 * GetContrast())); f0.writeBytes("\n count= ");
	 * f0.writeBytes(Integer.toString((int) GetCount()));
	 * f0.writeBytes("\n minHistBin= ");
	 * f0.writeBytes(Integer.toString(GetMinHistBin()));
	 * f0.writeBytes("\n maxHistBin= ");
	 * f0.writeBytes(Integer.toString(GetMaxHistBin()));
	 * 
	 * f0.writeBytes("\n numSamples= "); f0.writeBytes(Integer.toString((int)
	 * GetNumSamples())); f0.writeBytes("\n maxDataVal= ");
	 * f0.writeBytes(Float.toString((float) GetMaxDataVal()));
	 * f0.writeBytes("\n minDataVal= "); f0.writeBytes(Float.toString((float)
	 * GetMinDataVal()));
	 * 
	 * f0.close(); } // output methods public boolean PrintHistData(String
	 * OutFileName) throws Exception { // sanity check if (GetNumBins() <= 0 ||
	 * GetHistData() == null) { logger.error("Error: no data to write out");
	 * return false; }
	 * 
	 * // open the file RandomAccessFile f0; f0 = new
	 * RandomAccessFile(OutFileName, "rw"); //f0.writeBytes("numofbins ");
	 * 
	 * // number of points //f0.write(GetNumBins());
	 * f0.writeBytes(Integer.toString(GetNumBins())); f0.writeBytes("\n"); //
	 * number of features per point //f0.write((int)2);
	 * f0.writeBytes(Integer.toString(2)); f0.writeBytes("\n"); // type of
	 * numbers f0.writeBytes("INT INT"); f0.writeBytes("\n");
	 * 
	 * // ignore WideBins int i; for (i = 0; i < GetNumBins(); i++) {
	 * f0.writeBytes(Integer.toString(i)); f0.writeBytes(" "); //test
	 * //if(_HistData[i] != 0) //
	 * logger.debug("Test:i="+i+" hist="+_HistData[i]);
	 * 
	 * f0.writeBytes(Integer.toString(_HistData[i])); f0.writeBytes("\n"); }
	 * 
	 * / //use wideBins float val; int i; val = GetMinVal() + GetWideBins()0.5;
	 * for(i=0;i<GetNumBins();i++){ f0.writeBytes(Float.toString(val));
	 * f0.writeBytes(" "); f0.writeBytes(Integer.toString(_HistData[i]));
	 * f0.writeBytes("\n"); val += GetWideBins(); } / f0.close(); return true; }
	 * 
	 * public boolean PrintHistPDF(String OutFileName) throws Exception { //
	 * sanity check if (GetNumBins() <= 0 || GetHistData() == null) {
	 * logger.error("Error: no data to write out"); return false; } int i; long
	 * sum = 0; for (i = 0; i < GetNumBins(); i++) { sum +=
	 * _HistData[i];//GetHistDatum(i); }
	 * logger.debug("Info: Total num. of samples = " + sum);
	 * 
	 * // open the file RandomAccessFile f0; f0 = new
	 * RandomAccessFile(OutFileName, "rw"); //f0.writeBytes("numofbins "); //
	 * number of points f0.writeBytes(Integer.toString(GetNumBins()));
	 * f0.writeBytes("\n"); // number of features per point
	 * f0.writeBytes(Integer.toString(2)); f0.writeBytes("\n"); // type of
	 * numbers f0.writeBytes("INT FLOAT"); f0.writeBytes("\n");
	 * 
	 * for (i = 0; i < GetNumBins(); i++) { f0.writeBytes(Integer.toString(i));
	 * f0.writeBytes(" "); f0.writeBytes(Float.toString((float) (_HistData[i]) /
	 * sum)); f0.writeBytes("\n"); } f0.close(); return true; }
	 */
	public void PrintHistogram() {
		if (logger.isDebugEnabled()) {
			logger.debug("NumBins=" + GetNumBins());
			logger.debug("WideBins=" + GetWideBins());
			logger.debug("MinDataVal=" + GetMinDataVal());
			logger.debug("MaxDataVal=" + GetMaxDataVal());
		}
	}

}
