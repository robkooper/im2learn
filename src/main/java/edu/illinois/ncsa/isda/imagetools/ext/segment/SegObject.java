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
 * SegObject.java
 *  
 */

import java.io.RandomAccessFile;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.imagetools.core.datatype.SubArea;


/*
 * @author Peter Bajcsy & Young-Jin Lee
 * 
 * @version 1.0
 *  
 */

public class SegObject {
	// data related values
	protected double _minDataVal;

	protected double _maxDataVal;

	protected float _deltaSigma;

	protected float _minSigma;

	protected float _maxSigma;

	protected int[] _pNewSize = null;

	protected ImageObject _pImLabels = null;

	protected ImageObject _pImMean = null;

	protected double[] _pImValueS = null;

	// this is to monitor region mergers
	protected ImageObject _mergeStats = null;

	protected boolean _flagNoiseFilter = false;

	final static protected int _minSizeS = 15;// minimu segment size if any
											  // filtering is applied

	protected boolean _flagEvenSigmaLayers = false;

	protected int _N2FindEvenSigmaLayers = 1;

	protected boolean _flagN2FindS = false;

	protected int _N2FindS = 2;

	protected final static double _UNUSED = -1.0;

	protected int _NFoundS = 0;

	////////////// output option
	protected boolean _flagEnhanceLabels = true;//false;

	//filtering parameters
	protected int _filterWindow = 1;

	protected float _percentFilterWindow = 6.0F / 9.0F;// this should lead to 6
													   // pixels out of 3x3
													   // window

	//internal
	protected int _maxNFoundSLayers = -1;
	
	public SegObject() {
		ResetSegObject();
		_minDataVal = -1 * Double.MAX_VALUE;		
		_maxDataVal = Double.MAX_VALUE;
	}

	public void ResetSegObject() {
		_minSigma = _maxSigma = 0.0F;
		_deltaSigma = 1.0F;
		_minDataVal = _maxDataVal = 0.0;
		_pNewSize = null;
		_pImValueS = null;

		_pImLabels = null;
		_pImMean = null;

		_mergeStats = null;

		_NFoundS = 0;
	}

	//Getters
	public ImageObject GetMergeStats() {
		return _mergeStats;
	}

	public ImageObject GetImLabels() {
		return _pImLabels;
	}

	public ImageObject GetImMean() {
		return _pImMean;
	}

	public int[] GetNewSize() {
		return _pNewSize;
	}

	public double[] GetImValueS() {
		return _pImValueS;
	}

	// Data values
	public double GetMinDataVal() {
		return _minDataVal;
	}

	public double GetMaxDataVal() {
		return _maxDataVal;
	}

	public float GetDeltaSigma() {
		return _deltaSigma;
	}

	public float GetMinSigma() {
		return _minSigma;
	}

	public float GetMaxSigma() {
		return _maxSigma;
	}

	// postprocessing to filter out small speckles
	public boolean GetFlagNoiseFilter() {
		return _flagNoiseFilter;
	}

	//result
	public int GetNFoundS() {
		return _NFoundS;
	}

	public boolean GetFlagEnhanceLabels() {
		return _flagEnhanceLabels;
	}

	// exit options
	public boolean GetFlagN2FindS() {
		return _flagN2FindS;
	}

	public int GetN2FindS() {
		return _N2FindS;
	}

	public boolean GetFlagEvenSigmaLayers() {
		return _flagEvenSigmaLayers;
	}

	public int GetN2FindEvenSigmaLayers() {
		return _N2FindEvenSigmaLayers;
	}

	// filtering parameters
	public int GetFilterWindow() {
		return _filterWindow;
	}

	public float GetPercentFilterWindow() {
		return _percentFilterWindow;
	}

	//statistics

	//Setters
	public boolean SetDeltaSigma(float val) {
		if (val <= 0) {
			System.out
					.println("Error: Delta sigma  has to be greater than zero");
			return false;
		}
		_deltaSigma = val;
		return true;
	}

	public boolean SetMinSigma(float val) {
		if (val < 0) {
			System.out
					.println("Error: Min sigma  has to be greater or equal than zero");
			return false;
		}
		_minSigma = val;
		return true;
	}

	public boolean SetMaxSigma(float val) {
		if (val < 0) {
			System.out
					.println("Error: Max sigma  has to be greater or equal than zero");
			return false;
		}
		_maxSigma = val;
		return true;
	}

	//setters for exit options
	public boolean SetN2FindS(int val) {
		if (val < 1) {
			System.out
					.println("Error: Desired Number to find segments has to be greater than zero");
			return false;
		}
		_N2FindS = val;
		return true;
	}

	public void SetFlagN2FindS(boolean val) {
		_flagN2FindS = val;
	}

	public boolean SetN2FindEvenSigmaLayers(int val) {
		if (val < 1) {
			System.out
					.println("Error: Desired Number to find segmentation layers has to be greater than zero");
			return false;
		}
		_N2FindEvenSigmaLayers = val;
		return true;
	}

	public void SetFlagEvenSigmaLayers(boolean val) {
		_flagEvenSigmaLayers = val;
	}

	//////////////////////////////
	// output parameter
	public void SetFlagEnhanceLabels(boolean val) {
		_flagEnhanceLabels = val;
	}

	// noise filtering parameters
	public void SetFlagNoiseFilter(boolean val) {
		_flagNoiseFilter = val;
	}

	// filtering parameters
	public boolean SetFilterWindow(int val) {
		if (val < 1) {
			System.out
					.println("Error: FilterWindow less than 1 is not allowed");
			return false;
		}
		_filterWindow = val;
		return true;
	}

	public boolean SetPercentFilterWindow(float val) {
		if (val > 1.0F || val < 0.0F) {
			System.out
					.println("Error: PercentFilterWindow should be from [0,1]");
			return false;
		}
		_percentFilterWindow = val;
		return true;
	}

	public boolean printImageData(String outFileName, ImageObject img,
			SubArea area) throws Exception {
		// sanity check
		if (img == null	|| img.getData() == null) {
			System.out.println("Error: no data to write out");
			
			return false;
		}
		
		if (area.getRow() < 0 || area.getCol() < 0
				|| (area.getRow() + area.getHigh()) >= img.getNumRows()
				|| (area.getCol() + area.getWide()) >= img.getNumCols()) {
			System.out.println("Error: area is outside of image ");
			
			return false;
		}
		
		// open the file
		RandomAccessFile f0;
		f0 = new RandomAccessFile(outFileName, "rw");

		// area
		f0.writeBytes("area (Row, Col, High, Wide) \t");
		f0.writeBytes(Integer.toString(area.getRow()));
		f0.writeBytes("\t");
		f0.writeBytes(Integer.toString(area.getCol()));
		f0.writeBytes("\t");
		f0.writeBytes(Integer.toString(area.getHigh()));
		f0.writeBytes("\t");
		f0.writeBytes(Integer.toString(area.getWide()));
		f0.writeBytes("\n");

		int i, j, idx;
		idx = area.getRow() * img.getNumCols() + area.getCol();
		
		//TODO
		for (i = area.getRow(); i < area.getRow() + area.getHigh(); i++) {
			for (j = area.getCol(); j < area.getCol() + area.getWide(); j++) {
				f0.write(img.getByte(idx));							
				f0.writeBytes("\t");
				idx++;
			}
			
			f0.writeBytes("\n");
			idx = idx - area.getWide() + img.getNumCols();
		}
		
		f0.writeBytes("\n");
		f0.close();
		
		return true;
	}
	
	/**
	 * 
	 * @param outFileName
	 * @param img
	 * @param area
	 * @return
	 * @throws Exception
	 * 
	 * @deprecated Use printImageData(String, ImageObject, SubArea)
	 */
	public boolean PrintImageData(String outFileName, ImageObject img,
			SubArea area) throws Exception {
		return printImageData(outFileName, img, area);
	}

	public void printFileSegObject(String outFileName) throws Exception {
		//		 open the file
		RandomAccessFile f0;
		f0 = new RandomAccessFile(outFileName, "rw");

		f0.writeBytes("segmentation data ");
		f0.writeBytes(" \n");

		f0.writeBytes("\n maxDataVal= ");
		f0.writeBytes(Double.toString(GetMaxDataVal()));
		f0.writeBytes("\n minDataVal= ");
		f0.writeBytes(Double.toString(GetMinDataVal()));

		f0.close();
	}
	
	/**
	 * 
	 * @param outFileName
	 * @throws Exception
	 * 
	 * @deprecated Use printFileSegObject(String)
	 */
	public void PrintFileSegObject(String outFileName) throws Exception {
		printFileSegObject(outFileName);
	}

	public void printSegObject() {
		System.out.println("segmentation data ");

		System.out.println("maxDataVal= " + GetMaxDataVal() + ",minDataVal= "
				+ GetMinDataVal());
		System.out.println("Sigma min=" + GetMinSigma() + ", max="
				+ GetMaxSigma() + ",delta=" + GetDeltaSigma());
		System.out.println("NFoundS = " + _NFoundS);
	}
	
	/**
	 * @deprecated Use printSegobject()
	 *
	 */
	public void PrintSegObject() {
		printSegObject();
	}

	public void printMergeSigma() {
		if (_mergeStats == null) {
			System.out.println("ERROR: mergeSigma is not available ");
			return;
		}
		
		System.out.println("merge sigma data ");
		for (int i = 0; i < _mergeStats.getSize(); i += _mergeStats.getNumBands()) {
			if (_mergeStats.getDouble(i) > 0)
				System.out.println("Sigma =" + _mergeStats.getDouble(i)
						+ ", Merger=" + _mergeStats.getDouble(i + 1)
						+ ", NFound=" + _mergeStats.getDouble(i + 2));
		}
		
		return;
	}
	
	/**
	 * @deprecated Use printMergeSigma()
	 *
	 */
	public void PrintMergeSigma() {
		printMergeSigma();
	}

}
