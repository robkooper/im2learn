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
package edu.illinois.ncsa.isda.im2learn.core.datatype;

import java.io.Serializable;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.im2learn.core.geo.GeoException;
import edu.illinois.ncsa.isda.im2learn.core.geo.PRJLoader;
import edu.illinois.ncsa.isda.im2learn.core.geo.Projection;
import edu.illinois.ncsa.isda.im2learn.core.geo.Projection.ProjectionType;

/**
 * ShapeObject is a data structure that holds geographic vector data in a variety of projections.
 * 
 * @author Peter Bajcsy
 * @author clutter
 * @author Yakov Keselman
 * @version 2.0
 */
/*
 * June 22, 2006: Added support for non-geographic Projection's. Now, a ShapeObject can be either in pixel, geographic,
 * or projected coordinate system.
 */
public class ShapeObject implements Serializable {
	static private Log		logger				= LogFactory.getLog(ShapeObject.class);

	// constants for the boundary types.
	public static final int	NULL				= 0;
	public static final int	POINT				= 1;
	public static final int	ARC					= 3;
	public static final int	POLYGON				= 5;
	public static final int	MULTIPOINT			= 8;
	public static final int	POLY_LINE_Z			= 13;
	public static final int	ARC_M				= 23;
	public static final int	UNDEFINED			= -1;

	/** the number of boundaries */
	private int				_numBoundaries;

	/** the global bounding box. coordinates are {,,,} */
	private double[]		_globalBoundingBox	= new double[4];

	/**
	 * type of boundaries this shape object describes. size will be _numBnd
	 */
	private final int[]		_boundaryTypes;

	/**
	 * The bounding box for each boundary. The size will be 4 * (number of boundaries). NOTE: Even a boundary of type
	 * ShapePoint will have a bounding box whose MinX and MaxX are the same and MinY and MaxY are the same.
	 */
	private final double[]	_boundingBoxes;

	/**
	 * The number of points in each boundary. size of _numBndPts will be _numBnd
	 */
	private final int[]		_numBoundaryPoints;

	/**
	 * The actual points for all the boundaries.
	 */
	private Point2DDouble	_boundaryPoints;

	/**
	 * Index into each boundary's points in _bndPts[]. size of _bndPtsIdx will be _numBnd
	 */
	private final int[]		_boundaryPointsIndex;

	/**
	 * The number of parts in each boundary. size of _numBndParts will be _numBnd
	 */
	private final int[]		_numBoundaryParts;

	/**
	 * Indexes into a boundary's points, starting from 0 for each boundary. Starting index of each part of a boundary.
	 * NOTE: Even when we refer to the second or third boundary's part, the index for the first part of that boundary
	 * will be 0.
	 */
	private int[]			_boundaryPartsIndex;

	/**
	 * Internal point of each boundary. size of _bndInternalPts will be _numBnd
	 */
	private Point2DDouble	_bndInternalPts;

	/**
	 * True if the points are specified in pixel coordinates. False if the points are specified in geographic or
	 * projected coordinates.
	 */
	private boolean			_isInPixel			= false;

	/**
	 * Projection information associated with the shape (typically, imported from an external file, such as .prj).
	 */
	// private ModelProjection _projection = null;
	private Projection		_projection			= null;

	private static Log		_logger				= LogFactory.getLog(ShapeObject.class);

	/**
	 * Construct a new shape object
	 */
	public ShapeObject() {
		this(0);
		// ResetShapeObject();
	}

	/**
	 * Constructor
	 * 
	 * @param numBoundaries
	 *            int
	 * @param globalBBoxes
	 *            double[]
	 * @param boundaryTypes
	 *            int[]
	 * @param bBoxes
	 *            double[]
	 * @param numBoundaryPoints
	 *            int[]
	 * @param boundaryPoints
	 *            double[]
	 * @param boundaryPointsIndex
	 *            int[]
	 * @param numBoundaryParts
	 *            int[]
	 * @param boundaryPartsIndex
	 *            int[]
	 * @param boundaryInternalPoints
	 *            double[]
	 */
	public ShapeObject(int numBoundaries, double[] globalBBoxes, int[] boundaryTypes, double[] bBoxes, int[] numBoundaryPoints, double[] boundaryPoints, int[] boundaryPointsIndex,
			int[] numBoundaryParts, int[] boundaryPartsIndex, double[] boundaryInternalPoints) {

		_numBoundaries = numBoundaries;
		_globalBoundingBox = globalBBoxes;
		_boundaryTypes = boundaryTypes;
		_boundingBoxes = bBoxes;
		_numBoundaryPoints = numBoundaryPoints;
		if (boundaryPoints != null) {
			_boundaryPoints = new Point2DDouble(boundaryPoints.length);
			_boundaryPoints.ptsDouble = boundaryPoints;
		} else {
			_boundaryPoints = new Point2DDouble(0);
		}
		_boundaryPointsIndex = boundaryPointsIndex;
		_numBoundaryParts = numBoundaryParts;
		_boundaryPartsIndex = boundaryPartsIndex;
		if (boundaryInternalPoints != null) {
			_bndInternalPts = new Point2DDouble(boundaryInternalPoints.length);
			_bndInternalPts.ptsDouble = boundaryInternalPoints;
		} else {
			_bndInternalPts = new Point2DDouble(0);
		}
	}

	public ShapeObject CopyShapeObject() {
		ShapeObject retShapeObj = new ShapeObject(_numBoundaries);
		retShapeObj.SetIsInPixel(_isInPixel);

		Point2DDouble internalPts = null;
		Point2DDouble bndPts = null;

		if (_bndInternalPts != null) {
			internalPts = new Point2DDouble(_bndInternalPts.numpts);
			for (int i = 0; i < _bndInternalPts.numpts; i++) {
				internalPts.SetValue(i, _bndInternalPts.GetValueRow(i), _bndInternalPts.GetValueCol(i));
			}
		}
		if (_boundaryPoints != null) {
			bndPts = new Point2DDouble(_boundaryPoints.numpts);
			for (int i = 0; i < _boundaryPoints.numpts; i++) {
				bndPts.SetValue(i, _boundaryPoints.GetValueRow(i), _boundaryPoints.GetValueCol(i));
			}
		}
		int[] bndPartsIndex = null;
		if (_boundaryPartsIndex != null) {
			bndPartsIndex = new int[_boundaryPartsIndex.length];
			for (int i = 0; i < _boundaryPartsIndex.length; i++) {
				bndPartsIndex[i] = _boundaryPartsIndex[i];
			}
		}

		retShapeObj.setAllInternalPoints(internalPts);
		retShapeObj.setAllBoundaryPoints(bndPts);
		retShapeObj.setBoundaryAllPartsIndex(bndPartsIndex);
		retShapeObj.setAllBoundingBox(_boundingBoxes);
		retShapeObj.SetGlobalBndBox(_globalBoundingBox);

		for (int i = 0; i < _numBoundaries; i++) {
			retShapeObj.setBoundaryType(i, _boundaryTypes[i]);
			retShapeObj.setNumBoundaryParts(i, _numBoundaryParts[i]);
			retShapeObj.setBoundaryPointsIndex(i, _boundaryPointsIndex[i]);
			retShapeObj.setNumBoundaryPoints(i, _numBoundaryPoints[i]);
		}

		if (_projection != null) {
			try {
				retShapeObj.setProjection(PRJLoader.getProjection(_projection.toStringPRJ()));
			} catch (GeoException e) {
				logger.warn("Could not clone projection.", e);
			}
		}
		return retShapeObj;
	}

	/**
	 * Construct a shape object.
	 * 
	 * @param numBnd
	 *            the number of boundaries
	 */
	public ShapeObject(int numBnd) {
		// Initializes everything except _bndPartsIdx and _bndPts
		// ResetShapeObject();
		_numBoundaries = numBnd;
		_boundaryPointsIndex = new int[_numBoundaries];
		_boundaryTypes = new int[_numBoundaries];
		_boundingBoxes = new double[_numBoundaries << 2];
		_numBoundaryParts = new int[_numBoundaries];

		// Set this when the internal points are available in the shapefile
		_bndInternalPts = null;

		_numBoundaryPoints = new int[_numBoundaries];
		// memory for _bndPartsIdx is created when needed...
		// memory for _bndPts is created when needed...

		// Now store the default values for the boundary at index 0.
		// fillDefaultBnd();
	}

	/*
	 * public void ResetShapeObject() { System.gc(); }
	 */

	/*
	 * private void fillDefaultBnd() { _boundaryPointsIndex[0] = 0; _boundaryTypes[0] = 0; for (int i = 0; i < 4; i++) {
	 * _boundingBoxes[i] = 0.0; } _numBoundaryParts[0] = 0; _numBoundaryPoints[0] = 0; return; }
	 */

	// ////////////////////////////////////////////
	// Getters
	// ///////////////////////////////////////////
	/**
	 * Get the projection information associated with the shape.
	 */
	public Projection getProjection() {
		return this._projection;
	}

	/**
	 * @return true if the shape is in the geographic coordinate system.
	 */
	public boolean getIsInGeographic() {
		// check if projection information is there.
		// if it is, check the type of the projection.
		// if it is not, test "isInPixel".
		if (_projection != null) {
			return _projection.getType() == ProjectionType.Geographic;
		} else {
			return !this.getIsInPixel();
		}
	}

	/**
	 * @return true if the shape is in a projected coordinate system.
	 */
	public boolean getIsInProjected() {
		// check if projection information is there.
		// if it is, check the type of the projection.
		if (_projection != null) {
			return _projection.getType() != ProjectionType.Geographic;
		} else {
			return false;
		}
	}

	public boolean getIsInPixel() {
		return _isInPixel;
	}

	/**
	 * Returns the number of boundaries in this ShapeObject
	 * 
	 * @return the number of boundaries
	 */
	public int getNumBoundaries() {
		return _numBoundaries;
	}

	/**
	 * Get the global bounding box. The global bounding box describes ..
	 * 
	 * @return the global bounding box
	 */
	public double[] getGlobalBoundingBox() {
		return _globalBoundingBox;
	}

	/**
	 * Get the type of boundary at index. The types are static constants of this class.
	 * 
	 * @param index
	 *            the index of the boundary
	 * @return the type of boundary at index
	 */
	public int getBoundaryType(int index) {
		// Returns the type of the boundary at 'index' position in the
		// ShapeObject
		/*
		 * if (index < 0 || index >= _numBoundaries) { System.err.println("Index out of bounds: " + index); return -1; }
		 */
		if (_boundaryTypes.length > index) {
			return _boundaryTypes[index];
		} else {
			return _boundaryTypes[0];
		}
	}

	/**
	 * Get the bounding box at index.
	 * 
	 * @param index
	 *            the index of the boundary
	 * @return the bounding box of the boundary at index
	 */
	public double[] getBoundingBox(int index) {
		/*
		 * if (index < 0 || index >= _numBoundaries) { System.err.println("Index out of bounds: " + index); return null; }
		 */
		double[] box = new double[4];
		// 1.7.2004 clutter pulled the (index << 2) out of the for loop.
		// this value is constant
		int base = (index << 2);
		for (int i = 0; i < 4; i++) {
			// box[i] = _boundingBoxes[ (index << 2) + i];
			box[i] = _boundingBoxes[base + i];
		}
		return box;
	}

	/**
	 * Get the internal point of the boundary at index. The array contains the y co-ordinate first and then the x
	 * co-ordinate.
	 * 
	 * @param index
	 * @return
	 */
	public double[] getInternalPoint(int index) {
		// Returns the internal point of the boundary at 'index'
		// The double array contains the y co-ordinate first and then the x
		// co-ordinate
		/*
		 * if (index < 0 || index >= _numBoundaries) { System.err.println("Index out of bounds: " + index); return null; }
		 */
		double[] internalPoint = new double[2];
		internalPoint[0] = _bndInternalPts.GetValueRow(index);
		internalPoint[1] = _bndInternalPts.GetValueCol(index);
		return internalPoint;
	}

	public Point2DDouble getAllInternalPoints() {
		return _bndInternalPts;
	}

	public void setAllInternalPoints(Point2DDouble pts) {
		_bndInternalPts = pts;
	}

	public Point2DDouble getAllBoundaryPoints() {
		return _boundaryPoints;
	}

	public void setAllBoundaryPoints(Point2DDouble pts) {
		_boundaryPoints = pts;
	}

	/**
	 * Get the number of points in the boundary at index.
	 * 
	 * @param index
	 *            the index of the boundary
	 * @return the number of points for the boundary
	 */
	public int getNumBoundaryPts(int index) {
		// Returns the number of points in the boundary at the 'index'th index.
		/*
		 * if (index < 0 || index >= _numBoundaries) { System.err.println("Index out of bounds: " + index); return -1; }
		 */
		return _numBoundaryPoints[index];
	}

	/**
	 * Get the number of parts of the boundary at index
	 * 
	 * @param index
	 *            the index of the boundary
	 * @return the number of parts of the boundary
	 */
	public int getNumBoundaryParts(int index) {
		// Returns the number of parts in the boundary at the 'index'th index.
		/*
		 * if (index < 0 || index >= _numBoundaries) { System.err.println("Index out of bounds: " + index); return -1; }
		 */
		return _numBoundaryParts[index];
	}

	/**
	 * Get the starting index of the points of the boundary at index.
	 * 
	 * @param index
	 *            the index of the boundary
	 * @return the starting index of the points of the boundary
	 */
	public int getBoundaryPointsIndex(int index) {
		// Returns the starting index of the points of boundary at index
		// 'bndIndex'.
		/*
		 * if ( (bndIndex) < 0 || (bndIndex >= _numBoundaries)) { System.err.println("Boundary index out of bounds: " +
		 * bndIndex); return -1; }
		 */
		return _boundaryPointsIndex[index];
	}

	/**
	 * Get an array of part indices for the boundary at index
	 * 
	 * @param index
	 *            the index of the boundary
	 * @return an array of part indices for the boundary
	 */
	public int[] getPartsForBoundary(int index) {
		// Returns an array of parts indices for the boundary at index 'idx'
		// This array gives the index into the parts of the boundary starting
		// from the 0th index into the boundary's points,
		// even if the concerned boundary is not the first boundary in the
		// ShapeObject.
		/*
		 * if (idx < 0 || idx >= _numBoundaries) { System.err.println("Index out of bounds: " + idx); return null; }
		 */
		if (_numBoundaryParts[index] == 0) { // For ShapePoint type of
			// boundary
			return null;
		}
		int[] parts = new int[_numBoundaryParts[index]];
		int indexParts = SkipBndPartsIndexUpto(index); // Returns the starting
		// index into
		// _bndPartsIdx
		// for the boundary at 'idx'.
		int j = 0;
		for (int i = indexParts; i < (indexParts + _numBoundaryParts[index]); i++) {
			parts[j] = _boundaryPartsIndex[i];
			j++;
		}
		return parts;
	}

	/*
	 * INCORRECT implementation...will be removed after further verification !!! public double[] GetPartPointsForBnd(int
	 * bndIdx, int partIdx){ // Returns the points for the part at index 'partIdx' of the boundary at index 'bndIdx'
	 * if((bndIdx >= _numBnd) || (bndIdx < 0)){ System.err.println("Index for boundary out of bound!!"); return null; }
	 * if((partIdx > (_numBndParts[bndIdx]-1) ) || (partIdx < 0)){ System.out.println("Index for parts out of bound!!");
	 * return null; } int numParts = _numBndParts[bndIdx]; if(numParts == 0){ // Boundary is of type 'ShapePoint' return
	 * null; } int bndPartStart = SkipBndPartsIndexUpto(bndIdx); // starting index of this bnd's parts idx // partStart
	 * will be the starting index into the double array of points _bndPts' of the reqd part int partStart =
	 * _bndPtsIdx[bndIdx] + _bndPartsIdx[bndPartStart + partIdx]; int partLength = 0; if(partIdx == (numParts - 1)){ //
	 * The last part of a boundary partLength = _numBndPts[bndIdx] - (partStart - _bndPtsIdx[bndIdx]); } else{
	 * partLength = _bndPartsIdx[bndPartStart + partIdx + 1] - _bndPartsIdx[bndPartStart + partIdx]; } if(partLength <
	 * 0){ System.err.println("Length negative"); } // Read partLength number of points starting from partStart double []
	 * partPoints = new double[partLength << 1]; for(int i = 0; i < (partLength << 1) ; i++){ int ptIdx = 0; int j =
	 * 0; partPoints[i] = _bndPts.ptsDouble[partStart + i]; } return partPoints; }
	 */

	/**
	 * Get the points for the part at partIdx of the boundary at bndIdx
	 * 
	 * @param bndIdx
	 *            the index of the boundary
	 * @param partIdx
	 *            the index of the part
	 * @return the points for the specified part of the specified boundary
	 */
	public double[] getPartPointsForBoundary(int bndIdx, int partIdx) {
		// Returns the points for the part at index 'partIdx' of the boundary at
		// index 'bndIdx'
		/*
		 * if ( (bndIdx >= _numBoundaries) || (bndIdx < 0)) { System.err.println("Index for boundary out of bound!!");
		 * return null; } if ( (partIdx > (_numBoundaryParts[bndIdx] - 1)) || (partIdx < 0)) { System.out.println("Index
		 * for parts out of bound!!"); return null; }
		 */

		int numParts = _numBoundaryParts[bndIdx];
		if (numParts == 0) { // Boundary is of type 'ShapePoint'
			return null;
		}

		int bndPartStart = SkipBndPartsIndexUpto(bndIdx); // starting index of
		// this bnd's parts
		// idx
		// partStart will be the starting index into the double array of points
		// _bndPts' of the reqd part

		int partStart = (_boundaryPointsIndex[bndIdx] >> 1) + _boundaryPartsIndex[bndPartStart + partIdx];
		int partLength = 0;
		if (numParts == 1) {
			partStart = (_boundaryPointsIndex[bndIdx] >> 1);
			partLength = _numBoundaryPoints[bndIdx];
		} else if (partIdx == 0) {
			partStart = (_boundaryPointsIndex[bndIdx] >> 1);
			partLength = _boundaryPartsIndex[bndPartStart + partIdx + 1];
		} else if (partIdx == (numParts - 1)) {
			partStart = (_boundaryPointsIndex[bndIdx] >> 1) + _boundaryPartsIndex[bndPartStart + partIdx];
			partLength = _numBoundaryPoints[bndIdx] - _boundaryPartsIndex[bndPartStart + partIdx];
		} else {
			partStart = (_boundaryPointsIndex[bndIdx] >> 1) + _boundaryPartsIndex[bndPartStart + partIdx];
			partLength = _boundaryPartsIndex[bndPartStart + partIdx + 1] - _boundaryPartsIndex[bndPartStart + partIdx];
		}

//		if (partIdx == (numParts - 1)) {
//			// The last part of a boundary //
//			//partLength = _numBoundaryPoints[bndIdx] - (partStart - 	((_boundaryPointsIndex[bndIdx]) >> 1));
//			partLength = _numBoundaryPoints[bndIdx] - _boundaryPartsIndex[bndPartStart + partIdx];
//		} else {
//			partLength = _boundaryPartsIndex[bndPartStart + partIdx + 1] - _boundaryPartsIndex[bndPartStart + partIdx]; //
//			//partLength = _boundaryPartsIndex[bndPartStart + partIdx];
//			// it does not work PB
//		}

// partLength = _numBoundaryPoints[bndIdx];
// for (int k = 0; k <= numParts - 1 - partIdx; k++) {
// partLength -= _boundaryPartsIndex[bndPartStart + k];
// }

		/*
		 * int partStart = (_boundaryPointsIndex[bndIdx] >> 1); for (int k = 0; k < partIdx; k++) { partStart +=
		 * _boundaryPartsIndex[bndPartStart + k]; } int partLength = _boundaryPartsIndex[bndPartStart + partIdx];
		 */
		// test PB
//		if (numParts > 2) {
//			System.out.println("print TEST partIdx =" + partIdx + " PartLength=" + partLength);
//		}
		if (partLength < 0) {
			System.err.println("print TEST partIdx =" + partIdx + " PartLength=" + partLength);
			System.err.println("Length negative");
			return null;
			// this is hack PB
			// partLength = -partLength;
		}
		// Read partLength number of points starting from partStart
		double[] partPoints = new double[partLength << 1];
		int ptIdx = 0;
		int j = 0;
		for (int i = 0; i < partLength; i++) {
			ptIdx = partStart + i;
			partPoints[j] = _boundaryPoints.GetValueRow(ptIdx);
			partPoints[j + 1] = _boundaryPoints.GetValueCol(ptIdx);
			j += 2;
		}
		return partPoints;
	}

	/**
	 * Get the Point2DDouble object that contains the points for all the boundaries
	 * 
	 * @return the points for all the boundaries in a Point2DDouble the order is lat, lon => bndPts[i] = lat and
	 *         bndPts[i+1] = lon
	 */
	public Point2DDouble getBoundaryPoints() {
		return _boundaryPoints;
	}

	/**
	 * Get the points for all the boundaries
	 * 
	 * @return the points for all the boundaries in a double[]
	 */
	public Point2DDouble getAllBoundaryInternalPoints() {
		return _bndInternalPts;
	}

	/**
	 * Get the points for the boundary at idx. The y co-ordinate is stored at even indices and the x co-ordinate at odd
	 * indices of the returned array.
	 * 
	 * @param idx
	 *            the index of the boundary
	 * @return the points that make up the boundary
	 */
	public double[] getPointsForBoundary(int idx) {
		// Returns an array with the actual points of the boundary at index
		// 'idx'.
		// The y co-ordinate is stored at even indices and the x co-ordinate at
		// odd indices of the returned array.
		if ((idx < 0) || (idx >= _numBoundaries)) {
			_logger.debug("WARNING: in getPointsForBoundary: Index out of bounds: " + idx);
			return null;
		}
		if (_boundaryPointsIndex[idx] < 0) {
			_logger.debug("WARNING: in getPointsForBoundary: _boundaryPointsIndex[idx]: " + _boundaryPointsIndex[idx]);
			return null;
		}

		int index = (_boundaryPointsIndex[idx]) >> 1;
		int j = 0;
		double[] resPoints = new double[(_numBoundaryPoints[idx] << 1)];

		// Now put the y co-ordinate first and then the x co-ordinate.
		// These MUST be reversed while writing out to a shapefile.
		for (int i = index; i < index + _numBoundaryPoints[idx]; i++) {
			resPoints[j] = _boundaryPoints.GetValueRow(i); // Store the y
			// co-ordinate
			// according to
			// shapefile usage
			j++;
			resPoints[j] = _boundaryPoints.GetValueCol(i); // Store the x
			// co-ordinate
			// according to
			// shapefile usage
			j++;
		}
		return resPoints;
	}

	/*
	 * public double[] FindBndBox(int idx){ // This method can be called if the original ShapeObject has been modified //
	 * and its points are no longer defined by the original bounding box. // Parameter 'idx' is the index of the
	 * boundary type being handled for this call. // This method finds the MinX, MinY, MaxX, MaxY for boundary at the
	 * 'idx' index in the ShapeObject. if(idx < 0 || idx >= _numBnd){ System.err.println("Index out of bounds: "+idx);
	 * return null; } int index = (_bndPtsIdx[idx])/2; // This will be an index to _bndPts // The first point for this
	 * boundary double[] bbox = new double[4]; bbox[0] = _bndPts.GetValueCol(index); // minX bbox[1] =
	 * _bndPts.GetValueRow(index); //minY bbox[2] = bbox[0]; // maxX bbox[3] = bbox[1]; //maxY if(_bndType[idx] ==
	 * Shapefile.POINT){ return bbox; } double column, row; for(int i = index; i < index + _numBndPts[idx]; i++){ column =
	 * _bndPts.GetValueCol(i); row = _bndPts.GetValueRow(i); if(bbox[0] > column) bbox[0] = column; if(bbox[1] > row)
	 * //minY bbox[1] = row; if(bbox[2] < column) bbox[2] = column; if(bbox[3] < row) //maxY bbox[3] = row; } return
	 * bbox; }
	 */

	// ////////////////////////////////////
	// Setters
	// ///////////////////////////////////
	/**
	 * Set the projection associated with the shape.
	 */
	public void setProjection(Projection projection) {
		this._projection = projection;
	}

	/**
	 * Set the isInPixel flag. When true, points are specified in pixel coordinates. When false, points are in geo
	 * coordinates (latitude/longitude or projected).
	 * 
	 * @param val
	 */
	public void setIsInPixel(boolean val) {
		_isInPixel = val;
	}

	/**
	 * Set the number of boundaries
	 * 
	 * @TODO should arrays be reallocated here???
	 * @param numbnd
	 *            the number of boundaries
	 */
	public void setNumBoundaries(int numbnd) {
		// Sets the number of boundaries in the ShapeObject
		/*
		 * if (numbnd <= 0) { System.err.println( "Error:Number of boundaries has to be greater than zero"); return
		 * false; }
		 */
		_numBoundaries = numbnd;
	}

	/**
	 * Set the global bounding box
	 * 
	 * @param box
	 *            new global bounding box
	 */
	public void setGlobalBoundingBox(double[] box) {
		for (int i = 0; i < 4; i++) {
			_globalBoundingBox[i] = box[i];
		}
	}

	/**
	 * Set the bounding box of a boundary.
	 * 
	 * @param index
	 *            the index of the boundary
	 * @param box
	 *            the bounding box
	 */
	public void setBoundingBox(int index, double[] box) {
		/*
		 * if (index < 0 || index >= _numBoundaries) { System.err.println("Index out of bounds: " + index); return
		 * false; }
		 */
		for (int i = 0; i < 4; i++) {
			_boundingBoxes[(index << 2) + i] = box[i];
		}
	}

	/**
	 * Set the bounding box of a boundary.
	 * 
	 * @param index
	 *            the index of the boundary
	 * @param box
	 *            the bounding box
	 */
	public void setAllBoundingBox(double[] box) {
		/*
		 * if (index < 0 || index >= _numBoundaries) { System.err.println("Index out of bounds: " + index); return
		 * false; }
		 */
		for (int i = 0; i < box.length; i++) {
			_boundingBoxes[i] = box[i];
		}
	}

	/**
	 * 
	 * @param index
	 * @param row
	 * @param col
	 * @return
	 */
	public void setInternalPoint(int index, double row, double col) {
		/*
		 * if (index < 0 || index >= _numBoundaries) { System.err.println("Index out of bounds: " + index); return
		 * false; }
		 */
		_bndInternalPts.SetValue(index, row, col);
	}

	/**
	 * Set the starting index for the points of the boundary at index
	 * 
	 * @param index
	 *            the index of the boundary
	 * @param bndIdx
	 *            the starting index of the boundary points (index into _boundaryPoints)
	 */
	public void setBoundaryPointsIndex(int index, int bndIdx) {
		// Sets the starting index for the points of the boundary at 'index' in
		// _bndPts
		/*
		 * if (index < 0 || index >= _numBoundaries) { System.err.println("Index out of bounds: " + index); return
		 * false; }
		 */
		_boundaryPointsIndex[index] = bndIdx;
	}

	public void setBoundaryPoints(Point2DDouble p2d) {
		this._boundaryPoints = p2d;
	}

	/**
	 * Set the number of parts of the boundary at index
	 * 
	 * @param index
	 *            the index of the boundary
	 * @param numParts
	 *            the number of parts
	 */
	public void setNumBoundaryParts(int index, int numParts) {
		// Sets the number of parts of boundary at 'index' in the ShapeObject
		/*
		 * if (index < 0 || index >= _numBoundaries) { System.err.println("Index out of bounds: " + index); return
		 * false; }
		 */
		_numBoundaryParts[index] = numParts;
	}

	/**
	 * Set the type of the boundary at index.
	 * 
	 * @param index
	 *            the index of the boundary
	 * @param bndType
	 *            the type of the boundary. should be one of the static constants of this class
	 */
	public void setBoundaryType(int index, int bndType) {
		// Sets the type of the boundary at 'index' in the ShapeObject
		/*
		 * if (index < 0 || index >= _numBoundaries) { System.err.println("Index out of bounds: " + index); return
		 * false; }
		 */
		_boundaryTypes[index] = bndType;
	}

	/**
	 * Set the number of points for the boundary at index
	 * 
	 * @param index
	 *            the index of teh boundary
	 * @param numBndPts
	 *            the number of points for the boundary
	 */
	public void setNumBoundaryPoints(int index, int numBndPts) {
		// Sets the number of points in the boundary at 'index'
		/*
		 * if (index < 0 || index >= _numBoundaries) { System.err.println("Index out of bounds: " + index); return
		 * false; }
		 */
		_numBoundaryPoints[index] = numBndPts;
	}

	public void setBoundaryAllPartsIndex(int[] bpi) {
		_boundaryPartsIndex = bpi;
	}

	public void setBoundaryPartsIndex(int i, int bpi) {
		if (_boundaryPartsIndex != null) {
			_boundaryPartsIndex[i] = bpi;
		}
	}

	public int getBoundaryPartsIndex(int i) {
		if (_boundaryPartsIndex != null) {
			return _boundaryPartsIndex[i];
		}
		return -1;
	}

	// ////////////////////////////////////////
	// Helper methods
	// ////////////////////////////////////////
	private int SkipBndPartsIndexUpto(int idx) {
		// Returns the starting index into _bndPartsIdx
		// for the boundary at 'idx'.
		if ((idx < 0) || (idx >= _numBoundaries)) {
			System.err.println("Index out of bounds: " + idx);
			return -1;
		}
		int skipIdx = 0;
		for (int i = 0; i < idx; i++) {
			// add all the numParts upto idx to get the
			// starting index into _bndPartsIdx for boundary at idx..
			skipIdx += _numBoundaryParts[i];
		}
		return skipIdx;
	}

	// /////////////////////////////////////////
	// deprecated methods
	// /////////////////////////////////////////
	// ////////////////////////////////////////////
	// Getters
	// ///////////////////////////////////////////
	public boolean GetIsInPixel() {
		return this.getIsInPixel();
	}

	public int GetNumBnd() {
		return this.getNumBoundaries();
	}

	public double[] GetGlobalBndBox() {
		return this.getGlobalBoundingBox();
	}

	public int GetBndType(int index) {
		return this.getBoundaryType(index);
	}

	public double[] GetBndBox(int index) {
		return this.getBoundingBox(index);
	}

	public double[] GetInternalPoint(int index) {
		return this.getInternalPoint(index);
	}

	public int GetNumBndPts(int index) {
		return this.getNumBoundaryPts(index);
	}

	public int GetNumBndParts(int index) {
		return this.getNumBoundaryParts(index);
	}

	public int GetBndPtsIdx(int bndIndex) {
		return this.getBoundaryPointsIndex(bndIndex);
	}

	public int[] GetPartsForBnd(int idx) {
		return this.getPartsForBoundary(idx);
	}

	public double[] GetPartPointsForBnd(int bndIdx, int partIdx) {
		return this.getPartPointsForBoundary(bndIdx, partIdx);
	}

	public Point2DDouble GetBndPts() { // returns all the points in the whole
		// ShapeObject
		return this.GetBndPts();
	}

	public double[] GetAllBndPts() {
		return this._boundaryPoints.ptsDouble;
	}

	public double[] GetPtsForBnd(int idx) {
		return this.getPointsForBoundary(idx);
	}

	// ////////////////////////////////////
	// Setters
	// ///////////////////////////////////
	public void SetIsInPixel(boolean val) {
		this.setIsInPixel(val);
	}

	public boolean SetNumBnd(int numbnd) {
		this.setNumBoundaries(numbnd);
		return true;
	}

	public boolean SetGlobalBndBox(double[] box) {
		this.setGlobalBoundingBox(box);
		return true;
	}

	public boolean SetBndBox(int index, double[] box) {
		this.setBoundingBox(index, box);
		return true;
	}

	/**
	 * 
	 * @param index
	 * @param row
	 * @param col
	 * @return
	 */
	public boolean SetInternalPoint(int index, double row, double col) {
		this.setInternalPoint(index, row, col);
		return true;
	}

	public boolean SetBndPtsIdx(int index, int bndIdx) {
		this.setBoundaryPointsIndex(index, bndIdx);
		return true;
	}

	/**
	 * @deprecated
	 * @param index
	 * @param numParts
	 * @return
	 */
	@Deprecated
	public boolean SetNumBndParts(int index, int numParts) {
		this.setNumBoundaryParts(index, numParts);
		return true;
	}

	public boolean SetBndType(int index, int bndType) {
		this.setBoundaryType(index, bndType);
		return true;
	}

	public boolean SetNumBndPts(int index, int numBndPts) {
		this.setNumBoundaryPoints(index, numBndPts);
		return true;
	}

}
