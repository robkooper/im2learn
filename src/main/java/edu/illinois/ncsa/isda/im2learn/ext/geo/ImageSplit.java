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

import java.util.Vector;

import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageException;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.im2learn.core.datatype.SubArea;
import edu.illinois.ncsa.isda.im2learn.core.geo.GeoException;
import edu.illinois.ncsa.isda.im2learn.core.geo.projection.Projection;


public class ImageSplit {
	/*
	 * The purpose of this class is to take in a GeoImageObject, cut it up into
	 * BLOCK_SIZE by BLOCK_SIZE pixel pieces and produce a Vector of those
	 * pieces. Additionally, a corresponding Vector of Point2DDouble objects
	 * holds the latitude and longitude value
	 */

	protected ImageObject		_geoObj;
	// protected GeoConvert _geoCon;
	protected Vector			_imgVector;
	protected Vector			_coordVector;
	// protected int[] _rowcolAr;
	// protected double[] _latlongAr;
	// protected int _rcIdx = 0;
	// protected int _latlongIdx = 0;
	protected int				_numRows;
	protected int				_numCols;
	protected int				_bottomRowDimension;
	protected int				_rightSideDimension;
	protected int				_blockNumHorizontal;
	protected int				_blockNumVertical;
	protected int				_bottomRowBlockNumHorizontal;
	protected int				_rightSideBlockNumVertical;
	protected int				_leftOverBlockRowDimension;
	protected int				_leftOverBlockColDimension;
	protected static final int	BLOCK_SIZE	= 10;

	// reuseable objects for all subarea possibilities
	protected SubArea			defaultSub;
	protected SubArea			bottomRowSub;
	protected SubArea			rightSideSub;

	/*
	 * protected SubArea tenXten; protected SubArea nineXnine; protected SubArea
	 * eightXeight; protected SubArea sevenXseven; protected SubArea sixXsix;
	 * protected SubArea fiveXfive; protected SubArea fourXfour; protected
	 * SubArea threeXthree; protected SubArea twoXtwo; protected SubArea
	 * oneXone;
	 */

	public ImageSplit(ImageObject geo) throws ImageException, GeoException {

		_geoObj = geo;
		_imgVector = new Vector();
		_coordVector = new Vector();

		defaultSub = new SubArea(0, 0, BLOCK_SIZE, BLOCK_SIZE);
		/*
		 * tenXten = new SubArea(0,0,10,10,true,false); nineXnine = new
		 * SubArea(0,0,9,9,true,false); eightXeight = new
		 * SubArea(0,0,8,8,true,false); sevenXseven = new
		 * SubArea(0,0,7,7,true,false); sixXsix = new
		 * SubArea(0,0,6,6,true,false); fiveXfive = new
		 * SubArea(0,0,5,5,true,false); fourXfour = new
		 * SubArea(0,0,4,4,true,false); threeXthree = new
		 * SubArea(0,0,3,3,true,false); twoXtwo = new
		 * SubArea(0,0,2,2,true,false); oneXone = new
		 * SubArea(0,0,1,1,true,false);
		 */

		_numRows = geo.getNumRows();
		_numCols = geo.getNumCols();
		// _geoCon = new GeoConvert(geo);

		int blockColNum = _numCols - _numCols % BLOCK_SIZE;
		int blockRowNum = _numRows - _numRows % BLOCK_SIZE;
		int blockNumHorizontal = blockColNum / BLOCK_SIZE; // # BLOCK_SIZE
															// blocks
		// across image
		int blockNumVertical = blockRowNum / BLOCK_SIZE; // # BLOCK_SIZE
															// blocks
		// down image
		_blockNumHorizontal = blockNumHorizontal;
		_blockNumVertical = blockNumVertical;

		_rightSideDimension = _numCols % BLOCK_SIZE; // dimension of leftover
		// column
		int rightSideBlockNumVertical = 0;
		int leftOverBlockRowDimension = 0;
		if (_rightSideDimension != 0) {
			rightSideBlockNumVertical = _numRows / _rightSideDimension; // #
																		// blocks
			// going down
			leftOverBlockRowDimension = _numRows % _rightSideDimension;
			// row dimension of 2nd leftover block
		}
		_rightSideBlockNumVertical = rightSideBlockNumVertical;
		_leftOverBlockRowDimension = leftOverBlockRowDimension;

		// *********************************
		// uncovered corner in lower right has leftOverBlockRowDimension rows
		// and rightSideDimension columns
		// *********************************

		_bottomRowDimension = _numRows % BLOCK_SIZE; // dimension of leftover
		// row blocks
		int bottomRowBlockNumHorizontal = 0;
		int leftOverBlockColDimension = 0;
		if (_bottomRowDimension != 0) {
			bottomRowBlockNumHorizontal = blockColNum / _bottomRowDimension;
			// # blocks going across
			leftOverBlockColDimension = blockColNum % _bottomRowDimension;
			// col dimension of 1st leftover block
		}

		bottomRowSub = new SubArea(0, 0, _bottomRowDimension, _bottomRowDimension);

		rightSideSub = new SubArea(0, 0, _rightSideDimension, _rightSideDimension);

		_bottomRowBlockNumHorizontal = bottomRowBlockNumHorizontal;
		_leftOverBlockColDimension = leftOverBlockColDimension;

		// **********************************************************
		// uncovered corner just left of the rightside leftover column
		// has leftOverBlockColDimension columns and bottomRowDimension rows
		// **********************************************************

		/***********************************************************************
		 * _rowcolAr = new int[2 (blockNumHorizontal * (blockNumVertical) +
		 * rightSideBlockNumVertical + bottomRowBlockNumHorizontal + 1 + 1)];
		 * _latlongAr = new double[2 * (blockNumHorizontal * (blockNumVertical) +
		 * rightSideBlockNumVertical + bottomRowBlockNumHorizontal + 1 + 1)];
		 */

		for (int i = 0; i < blockNumHorizontal; i++) {
			for (int j = 0; j < blockNumVertical; j++) {
				defaultNxNSplit(BLOCK_SIZE * j, BLOCK_SIZE * i);
			}
		}
		System.err.println("done with nxn");
		// bottom row
		for (int i = 0; i < bottomRowBlockNumHorizontal; i++) {
			bottomRowSplit(blockRowNum, _bottomRowDimension * i);
		}
		System.err.println("done with bottom row");

		// right side
		for (int k = 0; k < rightSideBlockNumVertical; k++) {
			rightSideSplit(_rightSideDimension * k, blockColNum);
		}
		System.err.println("done with right side");

		int rowCoord = rightSideBlockNumVertical * _rightSideDimension;
		// the row coordinate of the lower left block

		// leftover block below right side blocks
		mBynSplit(leftOverBlockRowDimension, _rightSideDimension, rowCoord, blockColNum);
		System.err.println("leftover block 1 done");

		// leftover block left of rightside column
		mBynSplit(_bottomRowDimension, leftOverBlockColDimension, blockRowNum, _bottomRowDimension
				* bottomRowBlockNumHorizontal);
		System.err.println("leftover block 2 done");
	}

	private void defaultNxNSplit(int row, int column) throws ImageException, GeoException {

		// ImageObject tmpIm = new ImageObject(BLOCK_SIZE, BLOCK_SIZE,
		// _geoObj.sampPerPixel, _geoObj.sampType);
		// ImageObject tmpIm = ImageObject.createImage(BLOCK_SIZE, BLOCK_SIZE,
		// _geoObj.getNumBands(),
		// _geoObj.getType());

		defaultSub.setRow(row);
		defaultSub.setCol(column);

		// System.err.println("getting row "+row+" and column "+column);
		// boolean ret = _geoObj.CopyAreaImageObjectInternal(tmpIm, defaultSub);
		ImageObject tmpIm = _geoObj.crop(defaultSub);

		_imgVector.add(tmpIm);
		// _rowcolAr[_rcIdx] = row;
		// _rowcolAr[_rcIdx + 1] = column;
		// _rcIdx += 2;

		// Point2DDouble pt1 = new Point2DDouble(1);
		// pt1.ptsDouble[0] = column;
		// pt1.ptsDouble[1] = row;
		double[] pt1 = new double[] { column, row };
		// Point2DDouble pt2 = _geoCon.ColumnRow2LatLng(pt1);

		Projection proj = (Projection) _geoObj.getProperty(ImageObject.GEOINFO);
		double[] pt2 = proj.ColumnRow2LatLng(pt1);

		// System.err.println("the longitude is "+pt2.ptsDouble[1]);

		_coordVector.add(pt2);
		// _latlongAr[_latlongIdx] = pt2[0];
		// _latlongAr[_latlongIdx + 1] = pt2[1];
		// _latlongIdx += 2;
	}

	private void mBynSplit(int mRowDim, int nColDim, int row, int column) throws ImageException, GeoException {

		if ((mRowDim == 0) || (nColDim == 0)) {
			return;
		}

		// ImageObject tmpIm = new ImageObject(mRowDim, nColDim,
		// _geoObj.sampPerPixel, _geoObj.sampType);
		SubArea sub1 = new SubArea(column, row, nColDim, mRowDim);
		// sub1.setRow(row);
		// sub1.setCol(column);

		// System.err.println("(mXn) getting row "+row+" and column "+column);
		// boolean ret = _geoObj.CopyAreaImageObjectInternal(tmpIm, sub1);
		ImageObject tmpIm = _geoObj.crop(sub1);

		_imgVector.add(tmpIm);
		// _rowcolAr[_rcIdx] = row;
		// _rowcolAr[_rcIdx + 1] = column;
		// _rcIdx += 2;

		// Point2DDouble pt1 = new Point2DDouble(1);
		// pt1.ptsDouble[0] = column;
		// pt1.ptsDouble[1] = row;
		// Point2DDouble pt2 = _geoCon.ColumnRow2LatLng(pt1);
		double[] pt1 = new double[] { column, row };
		Projection proj = (Projection) _geoObj.getProperty(ImageObject.GEOINFO);
		double[] pt2 = proj.ColumnRow2LatLng(pt1);

		// System.err.println("the longitude is "+pt2.ptsDouble[1]);
		_coordVector.add(pt2);
		// _latlongAr[_latlongIdx] = pt2[0];
		// _latlongAr[_latlongIdx + 1] = pt2[1];
		// _latlongIdx += 2;
		return;
	}

	private void bottomRowSplit(int row, int column) throws ImageException, GeoException {
		// ImageObject tmpIm = new ImageObject(_bottomRowDimension,
		// _bottomRowDimension,
		// _geoObj.sampPerPixel, _geoObj.sampType);

		bottomRowSub.setRow(row);
		bottomRowSub.setCol(column);

		// System.err.println("(bottom) getting row "+row+" and column
		// "+column);
		// boolean ret = _geoObj.CopyAreaImageObjectInternal(tmpIm,
		// bottomRowSub);
		ImageObject tmpIm = _geoObj.crop(bottomRowSub);

		_imgVector.add(tmpIm);
		// _rowcolAr[_rcIdx] = row;
		// _rowcolAr[_rcIdx + 1] = column;
		// _rcIdx += 2;

		// Point2DDouble pt1 = new Point2DDouble(1);
		// pt1.ptsDouble[0] = column;
		// pt1.ptsDouble[1] = row;
		// Point2DDouble pt2 = _geoCon.ColumnRow2LatLng(pt1);
		double[] pt1 = new double[] { column, row };
		Projection proj = (Projection) _geoObj.getProperty(ImageObject.GEOINFO);
		double[] pt2 = proj.ColumnRow2LatLng(pt1);

		// System.err.println("the longitude is "+pt2.ptsDouble[1]);
		_coordVector.add(pt2);
		// _latlongAr[_latlongIdx] = pt2[0];
		// _latlongAr[_latlongIdx + 1] = pt2[1];
		// _latlongIdx += 2;
	}

	private void rightSideSplit(int row, int column) throws ImageException, GeoException {
		// ImageObject tmpIm = new ImageObject(_rightSideDimension,
		// _rightSideDimension,
		// _geoObj.sampPerPixel, _geoObj.sampType);

		rightSideSub.setRow(row);
		rightSideSub.setCol(column);

		// System.err.println("(right) getting row "+row+" and column "+column);
		// boolean ret = _geoObj.CopyAreaImageObjectInternal(tmpIm,
		// rightSideSub);
		ImageObject tmpIm = _geoObj.crop(rightSideSub);

		_imgVector.add(tmpIm);
		// _rowcolAr[_rcIdx] = row;
		// _rowcolAr[_rcIdx + 1] = column;
		// _rcIdx += 2;

		// Point2DDouble pt1 = new Point2DDouble(1);
		// pt1.ptsDouble[0] = column;
		// pt1.ptsDouble[1] = row;
		// Point2DDouble pt2 = _geoCon.ColumnRow2LatLng(pt1);
		double[] pt1 = new double[] { column, row };
		Projection proj = (Projection) _geoObj.getProperty(ImageObject.GEOINFO);
		double[] pt2 = proj.ColumnRow2LatLng(pt1);

		// System.err.println("the longitude is "+pt2.ptsDouble[1]);
		// System.err.println("the latitude is "+pt2.ptsDouble[0]);
		_coordVector.add(pt2);
		// _latlongAr[_latlongIdx] = pt2[0];
		// _latlongAr[_latlongIdx + 1] = pt2[1];
		// _latlongIdx += 2;
	}

	public Vector getSplitVector() {
		return _imgVector;
	}

	public Vector getCoordVector() {
		return _coordVector;
	}

	/*
	 * public int[] getRowColArray() { return _rowcolAr; }
	 * 
	 * public double[] getLatLongArray() { return _latlongAr; }
	 */

	public int getBlockNumHorizontal() {
		return _blockNumHorizontal;
	}

	public int getBlockNumVertical() {
		return _blockNumVertical;
	}

	public int getBlockSize() {
		return BLOCK_SIZE;
	}

	public int getBottomRowBlockNumHorizontal() {
		return _bottomRowBlockNumHorizontal;
	}

	public int getBottomRowDimension() {
		return _bottomRowDimension;
	}

	public int getRightSideBlockNumVertical() {
		return _rightSideBlockNumVertical;
	}

	public int getRightSideDimension() {
		return _rightSideDimension;
	}

	public int getLeftOverBlockRowDimension() {
		return _leftOverBlockRowDimension;
	}

	public int getLeftOverBlockColDimension() {
		return _leftOverBlockColDimension;
	}

	/*
	 * private void tenByten(int row, int column){ ImageObject tmpIm = new
	 * ImageObject(10,10,_geoObj.sampPerPixel, _geoObj.sampType); tenXten.Row =
	 * row; tenXten.Column = column; boolean ret =
	 * _geoObj.CopyAreaImageObjectInternal(tmpIm, tenXten);
	 * _imgVector.add(tmpIm); Point2DDouble pt1 = new Point2DDouble(1);
	 * pt1.ptsDouble[0] = column; pt1.ptsDouble[1] = row; Point2DDouble pt2 =
	 * _geoCon.ColumnRow2LatLng(pt1); _coordVector.add(pt2); } private void
	 * nineBynine(int row, int column){ ImageObject tmpIm = new
	 * ImageObject(9,9,_geoObj.sampPerPixel, _geoObj.sampType); tenXten.Row =
	 * row; tenXten.Column = column; boolean ret =
	 * _geoObj.CopyAreaImageObjectInternal(tmpIm, tenXten);
	 * _imgVector.add(tmpIm); Point2DDouble pt1 = new Point2DDouble(1);
	 * pt1.ptsDouble[0] = column; pt1.ptsDouble[1] = row; Point2DDouble pt2 =
	 * _geoCon.ColumnRow2LatLng(pt1); _coordVector.add(pt2); } private void
	 * eightByeight(int row, int column){ ImageObject tmpIm = new
	 * ImageObject(8,8,_geoObj.sampPerPixel, _geoObj.sampType); tenXten.Row =
	 * row; tenXten.Column = column; boolean ret =
	 * _geoObj.CopyAreaImageObjectInternal(tmpIm, tenXten);
	 * _imgVector.add(tmpIm); Point2DDouble pt1 = new Point2DDouble(1);
	 * pt1.ptsDouble[0] = column; pt1.ptsDouble[1] = row; Point2DDouble pt2 =
	 * _geoCon.ColumnRow2LatLng(pt1); _coordVector.add(pt2); } private void
	 * sevenByseven(int row, int column){ ImageObject tmpIm = new
	 * ImageObject(7,7,_geoObj.sampPerPixel, _geoObj.sampType); tenXten.Row =
	 * row; tenXten.Column = column; boolean ret =
	 * _geoObj.CopyAreaImageObjectInternal(tmpIm, tenXten);
	 * _imgVector.add(tmpIm); Point2DDouble pt1 = new Point2DDouble(1);
	 * pt1.ptsDouble[0] = column; pt1.ptsDouble[1] = row; Point2DDouble pt2 =
	 * _geoCon.ColumnRow2LatLng(pt1); _coordVector.add(pt2); } private void
	 * sixBysix(int row, int column){ ImageObject tmpIm = new
	 * ImageObject(6,6,_geoObj.sampPerPixel, _geoObj.sampType); tenXten.Row =
	 * row; tenXten.Column = column; boolean ret =
	 * _geoObj.CopyAreaImageObjectInternal(tmpIm, tenXten);
	 * _imgVector.add(tmpIm); Point2DDouble pt1 = new Point2DDouble(1);
	 * pt1.ptsDouble[0] = column; pt1.ptsDouble[1] = row; Point2DDouble pt2 =
	 * _geoCon.ColumnRow2LatLng(pt1); _coordVector.add(pt2); } private void
	 * fiveByfive(int row, int column){ ImageObject tmpIm = new
	 * ImageObject(5,5,_geoObj.sampPerPixel, _geoObj.sampType); tenXten.Row =
	 * row; tenXten.Column = column; boolean ret =
	 * _geoObj.CopyAreaImageObjectInternal(tmpIm, tenXten);
	 * _imgVector.add(tmpIm); Point2DDouble pt1 = new Point2DDouble(1);
	 * pt1.ptsDouble[0] = column; pt1.ptsDouble[1] = row; Point2DDouble pt2 =
	 * _geoCon.ColumnRow2LatLng(pt1); _coordVector.add(pt2); } private void
	 * fourByfour(int row, int column){ ImageObject tmpIm = new
	 * ImageObject(4,4,_geoObj.sampPerPixel, _geoObj.sampType); tenXten.Row =
	 * row; tenXten.Column = column; boolean ret =
	 * _geoObj.CopyAreaImageObjectInternal(tmpIm, tenXten);
	 * _imgVector.add(tmpIm); Point2DDouble pt1 = new Point2DDouble(1);
	 * pt1.ptsDouble[0] = column; pt1.ptsDouble[1] = row; Point2DDouble pt2 =
	 * _geoCon.ColumnRow2LatLng(pt1); _coordVector.add(pt2); } private void
	 * threeBythree(int row, int column){ ImageObject tmpIm = new
	 * ImageObject(3,3,_geoObj.sampPerPixel, _geoObj.sampType); tenXten.Row =
	 * row; tenXten.Column = column; boolean ret =
	 * _geoObj.CopyAreaImageObjectInternal(tmpIm, tenXten);
	 * _imgVector.add(tmpIm); Point2DDouble pt1 = new Point2DDouble(1);
	 * pt1.ptsDouble[0] = column; pt1.ptsDouble[1] = row; Point2DDouble pt2 =
	 * _geoCon.ColumnRow2LatLng(pt1); _coordVector.add(pt2); } private void
	 * twoBytwo(int row, int column){ ImageObject tmpIm = new
	 * ImageObject(2,2,_geoObj.sampPerPixel, _geoObj.sampType); tenXten.Row =
	 * row; tenXten.Column = column; boolean ret =
	 * _geoObj.CopyAreaImageObjectInternal(tmpIm, tenXten);
	 * _imgVector.add(tmpIm); Point2DDouble pt1 = new Point2DDouble(1);
	 * pt1.ptsDouble[0] = column; pt1.ptsDouble[1] = row; Point2DDouble pt2 =
	 * _geoCon.ColumnRow2LatLng(pt1); _coordVector.add(pt2); } private void
	 * oneByone(int row, int column){ ImageObject tmpIm = new
	 * ImageObject(1,1,_geoObj.sampPerPixel, _geoObj.sampType); tenXten.Row =
	 * row; tenXten.Column = column; boolean ret =
	 * _geoObj.CopyAreaImageObjectInternal(tmpIm, tenXten);
	 * _imgVector.add(tmpIm); Point2DDouble pt1 = new Point2DDouble(1);
	 * pt1.ptsDouble[0] = column; pt1.ptsDouble[1] = row; Point2DDouble pt2 =
	 * _geoCon.ColumnRow2LatLng(pt1); _coordVector.add(pt2); }
	 */

}
