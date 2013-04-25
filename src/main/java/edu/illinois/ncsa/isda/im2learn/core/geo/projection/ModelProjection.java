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
package edu.illinois.ncsa.isda.im2learn.core.geo.projection;

import edu.illinois.ncsa.isda.im2learn.core.geo.GeoException;

/**
 * Projection implemention. This interface needs to be implemented by the
 * classes that perform a projection from modelspace to earthspace.
 */
public abstract class ModelProjection extends Projection {

	protected ModelProjection() {
	}

	/**
	 * Constructorb
	 * 
	 * @param rasterI
	 * @param rasterJ
	 * @param insertX
	 * @param insertY
	 * @param scaleX
	 * @param scaleY
	 * @param numRow
	 * @param numCol
	 */
	public ModelProjection(double rasterI, double rasterJ, double insertX, double insertY, double scaleX, double scaleY, int numRow, int numCol) {
		super(rasterI, rasterJ, insertX, insertY, scaleX, scaleY, numRow, numCol);
	}

	/**
	 * 
	 * @param point
	 * @return
	 * @throws IllegalArgumentException
	 * @throws GeoException
	 */
	public double[] colRowToNorthingEasting(double[] point) throws IllegalArgumentException, GeoException {
		return rasterToModel(point);
	}

	/**
	 * 
	 * @param point
	 * @return
	 * @throws IllegalArgumentException
	 * @throws GeoException
	 */
	public double[] northingEastingToColRow(double[] point) throws IllegalArgumentException, GeoException {
		return modelToRaster(point);
	}

	/**
	 * 
	 * @param point
	 * @return
	 * @throws IllegalArgumentException
	 * @throws GeoException
	 */
	public double[] northingEastingToLatLng(double[] point) throws IllegalArgumentException, GeoException {
		return modelToEarth(point);
	}

	/**
	 * 
	 * @param point
	 * @return
	 * @throws IllegalArgumentException
	 * @throws GeoException
	 */
	public double[] latLngToNorthingEasting(double[] point) throws IllegalArgumentException, GeoException {
		return earthToModel(point);
	}

	/**
	 * Given a point in raster space convert the point to modelspace. This will
	 * take the point, subtract the raster offset, scale it, and add the model
	 * offset.
	 * 
	 * @param point
	 *            the image point to be converted.
	 * @return a point in modelspace.
	 * @throws IllegalArgumentException
	 *             if the point is not 2D or 3D.
	 */
	public double[] rasterToModel(double[] point) throws IllegalArgumentException, GeoException {
		double[] result;

		switch (point.length) {
		case 2:
			result = new double[2];
			//result[0] = GetModelSpaceX() + (point[0] - GetRasterSpaceI()) * GetGeoScaleX();
			//result[1] = GetModelSpaceY() - (point[1] - GetRasterSpaceJ()) * GetGeoScaleY();

			// changes by PB on 05-21-2008
			result[0] = getInsertionX() + (point[0] - getRasterSpaceI()) * getScaleX();
			result[1] = getInsertionY() + (point[1] - getRasterSpaceJ()) * getScaleY();
			return result;

		case 3:
			result = new double[3];
			//result[0] = GetModelSpaceX() + (point[0] - GetRasterSpaceI()) * GetGeoScaleX();
			//result[1] = GetModelSpaceY() - (point[1] - GetRasterSpaceJ()) * GetGeoScaleY();
			//result[2] = GetModelSpaceZ() + (point[2] - GetRasterSpaceK()) * GetGeoScaleZ();

			// changes by PB on 05-21-2008
			result[0] = getInsertionX() + (point[0] - getRasterSpaceI()) * getScaleX();
			result[1] = getInsertionY() + (point[1] - getRasterSpaceJ()) * getScaleY();
			result[2] = getInsertionZ() + (point[2] - getRasterSpaceK()) * getScaleZ();

			return result;

		default:
			throw (new IllegalArgumentException("Can only handle 2D and 3D points."));
		}
	}

	/**
	 * 
	 * @param point
	 * @return
	 * @throws IllegalArgumentException
	 * @throws GeoException
	 */
	public double[] modelToRaster(double[] point) throws IllegalArgumentException, GeoException {
		double[] result;

		switch (point.length) {
		case 2:
			result = new double[2];

			double easting = point[0];
			double northing = point[1];

			//rounding removed 10/28/03
			//double column = Math.round((easting - _imgGeoObject.GetModelSpaceX())/
			//		_imgGeoObject.GetGeoScaleX() + _imgGeoObject.GetRasterSpaceI());
			//double row = Math.round((_imgGeoObject.GetModelSpaceY() - northing)/
			//		Math.abs(_imgGeoObject.GetGeoScaleY()) + _imgGeoObject.GetRasterSpaceJ());
			//double column = (easting - GetEastingInsertionValue()) / GetColumnResolution() + GetRasterSpaceI();
			//double row = (GetNorthingInsertionValue() - northing) / Math.abs(GetRowResolution()) + GetRasterSpaceJ();

			// changes by PB on 05-21-2008
			double column = (easting - getInsertionX()) / getScaleX() + getRasterSpaceI();
			double row = (northing - getInsertionY()) / getScaleY() + getRasterSpaceJ();

			double[] ret = new double[2];
			ret[0] = column;
			ret[1] = row;
			return ret;

		case 3:
			result = new double[3];
			return result;
		default:
			throw new IllegalArgumentException("");
		}
	}

	/**
	 * 
	 * @param point
	 * @return
	 * @throws IllegalArgumentException
	 * @throws GeoException
	 */
	public abstract double[] modelToEarth(double[] point) throws IllegalArgumentException, GeoException;

	/**
	 * 
	 * @param point
	 * @return
	 * @throws IllegalArgumentException
	 * @throws GeoException
	 */
	public abstract double[] earthToModel(double[] point) throws IllegalArgumentException, GeoException;

	/**
	 * 
	 * @param point
	 * @return
	 * @throws IllegalArgumentException
	 * @throws GeoException
	 */
	@Override
	public double[] rasterToEarth(double[] point) throws IllegalArgumentException, GeoException {
		return modelToEarth(rasterToModel(point));
	}

	/**
	 * 
	 * @param point
	 * @return
	 * @throws IllegalArgumentException
	 * @throws GeoException
	 */
	@Override
	public double[] earthToRaster(double[] point) throws IllegalArgumentException, GeoException {
		return modelToRaster(earthToModel(point));
	}

	/**
	 * Applies the projection on the northing (northingeasting[0]) and easting
	 * (northingeasting[1]) values resulting in latitude (result[0]) and
	 * longitude (result[1]).
	 * 
	 * @param point
	 *            the northing and easting values to convert to latitude and
	 *            longitude.
	 * @return the latitude and longitude.
	 */
	//public abstract double[] toLatLon(double[] point) throws GeoException;
	/**
	 * Converts the latitude (latlon[0]) and longitude (latlon[1]) to northing
	 * and easting values. This step will perform the inverse projection as done
	 * by toLatLon, resulting in the conversion of the latitude and longitude
	 * coordinate to northing (return[0]) and easting (return[1]).
	 * 
	 * @param point
	 *            the latitude and longitude to convert to norhting and easting
	 *            values.
	 * @return the northing and easting values of the latlon.
	 */
	//public abstract double[] fromLatLon(double[] point) throws GeoException;
	/**
	 * 
	 * @param mx
	 */
	public void setModelSpaceX(double mx) {
		setInsertionX(mx);
	}

	/**
	 * 
	 * @return
	 */
	public double getModelSpaceX() {
		return getInsertionX();
	}

	/**
	 * 
	 * @param my
	 */
	public void setModelSpaceY(double my) {
		setInsertionY(my);
	}

	/**
	 * 
	 * @return
	 */
	public double getModelSpaceY() {
		return getInsertionY();
	}

	/**
	 * 
	 * @param mz
	 */
	public void setModelSpaceZ(double mz) {
		setInsertionZ(mz);
	}

	/**
	 * 
	 * @return
	 */
	public double getModelSpaceZ() {
		return getInsertionZ();
	}

	/**
	 * @deprecated use getModelSpaceX instead
	 * @return
	 */
	@Deprecated
	public double GetModelSpaceX() {
		return getInsertionX();
	}

	/**
	 * @deprecated get getModelSpaceY instead
	 * @return
	 */
	@Deprecated
	public double GetModelSpaceY() {
		return getInsertionY();
	}

	/**
	 * @deprecated use getModelSpaceZ instead
	 * @return
	 */
	@Deprecated
	public double GetModelSpaceZ() {
		return getInsertionZ();
	}

	/**
	 * @deprecated use setModelSpaceX instead
	 * @param mx
	 */
	@Deprecated
	public void SetModelSpaceX(double mx) {
		setModelSpaceX(mx);
	}

	/**
	 * @deprecated use setModelSpaceY instead
	 * @param my
	 */
	@Deprecated
	public void SetModelSpaceY(double my) {
		setModelSpaceY(my);
	}

	/**
	 * @deprecated use setModelSpaceZ instead
	 * @param mz
	 */
	@Deprecated
	public void SetModelSpaceZ(double mz) {
		setModelSpaceZ(mz);
	}

	/**
	 * Find the minimum northing. This method find the northing for all the
	 * pixels in the (numrows-1) row of the image and returns the smallest.
	 * Thus, it will only work in the northern hemisphere. The value returned is
	 * in the standard units for this projection.
	 * 
	 * @return the minimum northing value
	 */
	public double getMinNorthing() {
		double northMeters = Double.POSITIVE_INFINITY;
		double[] pt1 = new double[2];
		pt1[1] = _numRows - 1;

		for (int i = 0; i < _numCols; i++) {
			pt1[0] = i;
			try {
				double[] pt2 = colRowToNorthingEasting(pt1);
				if (pt2[0] < northMeters) {
					northMeters = pt2[0];
				}
			} catch (GeoException ex) {
			}
		}
		return northMeters;
	}

	/**
	 * 
	 * @return
	 */
	public double getMaxNorthing() {
		double northMeters = Double.NEGATIVE_INFINITY;
		double[] pt1 = new double[2];
		pt1[1] = 0;

		for (int i = 0; i < _numCols; i++) {
			pt1[0] = i;
			try {
				double[] pt2 = colRowToNorthingEasting(pt1);
				if (pt2[0] > northMeters) {
					northMeters = pt2[0];
				}
			} catch (GeoException ex) {
			}
		}
		return northMeters;
	}

	/**
	 * 
	 * @return
	 */
	public double getMaxEasting() {
		double eastLng = Double.NEGATIVE_INFINITY; //only works for Western Hemisphere
		double[] pt1 = new double[2];
		pt1[0] = _numCols - 1; //rightmost column!

		for (int i = 0; i < _numRows; i++) {
			pt1[1] = i;
			try {
				double[] pt2 = colRowToNorthingEasting(pt1);
				if (pt2[1] > eastLng) {
					eastLng = pt2[1];
				}
			} catch (GeoException ex) {
			}
		}
		return eastLng;
	}

	/**
	 * 
	 * @return
	 */
	public double getMinEasting() {
		double eastLng = Double.POSITIVE_INFINITY; //only works for Western Hemisphere
		double[] pt1 = new double[2];
		pt1[0] = 0; //rightmost column!

		for (int i = 0; i < _numRows; i++) {
			pt1[1] = i;
			try {
				double[] pt2 = colRowToNorthingEasting(pt1);
				if (pt2[1] < eastLng) {
					eastLng = pt2[1];
				}
			} catch (GeoException ex) {
			}
		}
		return eastLng;
	}

	@Override
	public String toString() {
		return super.toString() + "\n" + getProjectionParametersAsString();
	}

}