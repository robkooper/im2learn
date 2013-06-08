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
/*
 * Created on Sep 7, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.illinois.ncsa.isda.im2learn.ext.segment;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author yjlee and Peter Bajcsy <p/> TODO To change the template for this
 *         generated type comment go to Window - Preferences - Java - Code Style -
 *         Code Templates
 */
public class EdgeLineObject extends EdgeLineRes {
	protected short			_N2Find;

	protected float			_Accept;

	protected short			_Extract;

	protected short			_SortType;

	// Extra info
	protected short			_SubLinesPerLine;

	protected int			_SubLineLength;

	protected short			_OutFlag;

	protected boolean		_debugEdgeLineObject;

	protected float			_Background;

	// Operator
	protected short			_Size;

	protected short			_Leniency;

	// Feature
	protected short			_Polarity1;

	protected short			_ExpectedWidth;

	protected short			_WidthTolerance;

	protected short			_Polarity2;

	// Results
	protected int			_NFound;

	protected float			_MaxVal;

	protected float			_MinVal;

	protected int			_NFoundSub;

	protected EdgeLineRes[]	_PResultValue	= null;

	protected EdgeLineRes[]	_PResultSubVal	= null;

	private static Log		logger			= LogFactory.getLog(EdgeLineObject.class);

	//constructor
	public EdgeLineObject() {
		initValues();

		setN2Find(1);
		_debugEdgeLineObject = true;
	}

	public void initValues() {
		_N2Find = 0;
		_Accept = 0.05F;
		_Extract = 1;
		_SortType = 1;
		_SubLinesPerLine = 4;
		_SubLineLength = 2;
		_OutFlag = 3;
		_Size = 2;
		_Leniency = 1;
		_Polarity1 = 0;
		_ExpectedWidth = 0;
		_WidthTolerance = 0;
		_Polarity2 = 0;
		_NFound = 0;
		_MaxVal = 0.0F;
		_MinVal = 0.0F;
		_NFoundSub = 0;
		_PResultValue = null;
		_PResultSubVal = null;
	}

	///////////////////////////////////////////////////////////////////////////
	// Setters
	//////////////////////////////////////////////////////////////////////////////
	public boolean setN2Find(int n2find) {
		if ((n2find <= 0) || (n2find >= 128)) {
			return false;
		}

		if (_PResultValue != null) {
			_PResultValue = null;
		}
		_PResultValue = new EdgeLineRes[n2find];
		for (int i = 0; i < n2find; i++) {
			_PResultValue[i] = new EdgeLineRes();
		}

		_N2Find = (short) n2find;
		return true;
	}

	/////////////////////////////////////////////////////////////////////////////
	public boolean setAccept(float accept) {
		if ((accept > 100.0F) || (accept < 0.0F)) {
			return false;
		}
		_Accept = accept;
		return true;
	}

	/////////////////////////////////////////////////////////////////////////////
	public boolean setExtract(int extract) {
		if ((extract > 1) || (extract < 0)) {
			return false;
		}
		_Extract = (short) extract;
		return true;
	}

	////////////////////////////////////////////////////////////////////////////
	public boolean setSort(int sort) {
		if ((sort > 8) || (sort < 0)) {
			return false;
		}
		_SortType = (short) sort;
		return true;
	}

	////////////////////////////////////////////////////////////////////////////
	public boolean setSubLinesPerLine(int val) {
		if ((val > 100) || (val < 0)) {
			return false;
		}
		_SubLinesPerLine = (short) val;
		return true;
	}

	////////////////////////////////////////////////////////////////////////////
	public boolean setSubLineLength(int val) {
		if ((val > 100) || (val < 0)) {
			return false;
		}
		_SubLineLength = val;
		return true;
	}

	////////////////////////////////////////////////////////////////////////////
	public boolean setOutFlag(int val) {
		if ((val > 3) || (val < 0)) {
			return false;
		}
		_OutFlag = (short) val;
		return true;
	}

	/////////////////////////////////////////////////////////////////////////////
	public boolean setOperator(int size, int leniency) {
		if ((size < 1) || (size > 255) || (leniency > 255) || (leniency < 0)) {
			return false;
		}
		_Size = (short) size;
		_Leniency = (short) leniency;
		return true;
	}

	//////////////////////////////////////////////////////////////////////
	// Getters
	/////////////////////////////////////////////////////////////////////////////
	public boolean getOperator(int size, int leniency) {
		size = _Size;
		leniency = _Leniency;
		return true;
	}

	/////////////////////////////////////////////////////////////////////////////
	public boolean setFeature(int polarity1, int expwidth, int widthtol, int polarity2) {
		if ((polarity1 < -1) || (polarity1 > 1) || (polarity2 < -1) || (polarity2 > 1)) {
			return false;
		}
/*
 * if (expwidth > 998 || expwidth < 0 || expwidth == 1 || widthtol > 998 ||
 * widthtol < 0) return false;
 */
		if ((expwidth < 0) || (widthtol < 0)) {
			return false;
		}
		if ((widthtol > expwidth)) {
			return false;
		}
		_Polarity1 = (short) polarity1;
		_ExpectedWidth = (short) expwidth;
		_WidthTolerance = (short) widthtol;
		_Polarity2 = (short) polarity2;
		return true;
	}

	public boolean getFeature(int polarity1, int expwidth, int widthtol, int polarity2) {
		polarity1 = _Polarity1;
		expwidth = _ExpectedWidth;
		widthtol = _WidthTolerance;
		polarity2 = _Polarity2;
		return true;
	}

	/////////////////////////////////////////////////////////////////////////////
	public EdgeLineRes getResult(int indx) {
		if (indx >= _N2Find) {
			return null;
		}
		return _PResultValue[indx];
	}

	/////////////////////////////////////////////////////////////////////////////
	public EdgeLineRes getResultSub(int indx) {
		if (indx >= _NFound * _SubLinesPerLine) {
			return null;
		}
		return _PResultSubVal[indx];
	}

	/////////////////////////////////////////////////////////////////////////////
	public int getN2Find() {
		return _N2Find;
	}

	public int getExtract() {
		return _Extract;
	}

	public float getAccept() {
		return _Accept;
	}

	public int getSort() {
		return _SortType;
	}

	public int getSubLinesPerLine() {
		return _SubLinesPerLine;
	};

	public int getSubLineLength() {
		return _SubLineLength;
	};

	public int getOutFlag() {
		return _OutFlag;
	};

	public void getDebugFlag(boolean val) {
		_debugEdgeLineObject = val;
	}

	public boolean getDebugFlag() {
		return _debugEdgeLineObject;
	};

	public int getSize() {
		return _Size;
	};

	public int getLeniency() {
		return _Leniency;
	};

	public int getPolarity1() {
		return _Polarity1;
	};

	public int getExpectedWidth() {
		return _ExpectedWidth;
	};

	public int getWidthTolerance() {
		return _WidthTolerance;
	};

	public int getPolarity2() {
		return _Polarity2;
	};

	public int getNFound() {
		return _NFound;
	}

	public void setNFound(int i) {
		_NFound = i;
	}

	public float getMaxVal() {
		return _MaxVal;
	}

	public void setMaxVal(float f) {
		_MaxVal = f;
	}

	public float getMinVal() {
		return _MinVal;
	}

	public void setMinVal(float f) {
		_MinVal = f;
	}

	public int getNFoundSub() {
		return _NFoundSub;
	}

	public void setNFoundSub(int i) {
		_NFoundSub = i;
	}

	public EdgeLineRes[] getPResultValue() {
		return _PResultValue;
	}

	public void setPResultValueItem(EdgeLineRes newItem, int index) {
		_PResultValue[index] = newItem;
	}

	public void setPResultValue(EdgeLineRes[] values) {
		_PResultValue = values;
	}

	public EdgeLineRes[] getPResultSubVal() {
		return _PResultSubVal;
	}

	public void setPResultSubVal(EdgeLineRes[] values) {
		_PResultSubVal = values;
	}

	public void setPResultSubValItem(EdgeLineRes value, int i) {
		_PResultSubVal[i] = value;
	}

	/////////////////////////////////////////////////////////////////////////////
	//display values
	public void printEdgeLineObject() {
		int what = getOutFlag();
		//what = 0 - io param., 1 - edge lines, 2 - edge sub lines, 3 - all
		logger.debug("EdgeLineObject:");
		if ((what == 0) || (what == 3)) {
			logger.debug("Input/Output parameters:");
			logger.debug("N2Find=" + getN2Find() + " Accept=" + getAccept() + " Extract=" + getExtract());
			logger.debug("SubLine: SubLinesPerLine=" + getSubLinesPerLine() + " SubLineLength=" + getSubLineLength());
			logger.debug("Operator: Size=" + getSize() + " Leniency=" + getLeniency());
			logger.debug("Feature: Polarity1=" + getPolarity1() + " ExpectedWidth=" + getExpectedWidth() + " WidthTolerance=" + getWidthTolerance() + " Polarity2=" + getPolarity2());
			logger.debug("Results: NFound=" + getNFound() + " NFoundSub=" + getNFoundSub());
			logger.debug("MaxVal=" + getMaxVal() + " MinVal=" + getMinVal());
		}

		int i;
		EdgeLineRes res = null;
		if ((what == 1) || (what == 3)) {
			//lines
			logger.debug(" EdgeLineObject Results");
			logger.debug(" Found=" + getNFound());

			for (i = 0; i < getNFound(); i++) {
				res = getResult(i);
				logger.debug("idx=" + i + " row=" + res.x + " col=" + res.y);
				logger.debug("offset=" + res.o + " width=" + res.w);
				logger.debug("score=" + res.s + " p1=" + res.p1 + " p2=" + res.p2);
			}
		}

		if ((what == 2) || (what == 3)) {
			//sub lines
			logger.debug(" EdgeSubLine Results");
			logger.debug(" Found=" + getNFoundSub());

			for (i = 0; i < getNFoundSub(); i++) {
				res = getResultSub(i);
				logger.debug("idx=" + i + " row=" + res.x + " col=" + res.y);
				logger.debug("offset=" + res.o + " width=" + res.w);
				logger.debug("score=" + res.s + " p1=" + res.p1 + " p2=" + res.p2);
				logger.debug("angle=" + res.a + " high=" + res.h);
			}
		}
	}

	/**
	 * @return Returns the _Background.
	 */
	public float getBackground() {
		return _Background;
	}

	/**
	 * @param background
	 *            The _Background to set.
	 */
	public void setBackground(float background) {
		_Background = background;
	}

	/**
	 * @return Returns the _SortType.
	 */
	public short getSortType() {
		return _SortType;
	}

	/**
	 * @param sortType
	 *            The _SortType to set.
	 */
	public void setSortType(short sortType) {
		_SortType = sortType;
	}
}//end of class EdgeLineObject
