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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import edu.illinois.ncsa.isda.im2learn.core.geo.GeoException;


/**
 * Use this class to geo-reference an ImageObject if the bounds are specified in
 * lat/lng only. The tie point is a known value that is used to assign lat/lng
 * values to each pixel in the underlying ImageObject. The tie point is anchored
 * at (rasterSpaceI, rasterSpaceJ). Its values are (insertionX, insertionY).
 * scaleX and scaleY are used along with the tie pt to determine the lat/lng of
 * any pixel in the image.
 * 
 * Subclasses should implement the different projections.
 * 
 * @TODO change the name of this class
 * @todo scale, resample, subsample---similar functions---combine them?
 * @todo totally ignoring the third dimension right now.
 */
public class Projection implements java.io.Serializable {

	//*************** types of projections **************

	/** geographic is referenced in lat/lng coordinates */
	public static final int	GEOGRAPHIC								= 0;

	public static final int	UTM_NORTHERN_HEMISPHERE					= 1;

	public static final int	STATE_PLANE_COORDINATES					= 2;

	public static final int	ALBERS_EQUAL_AREA_CONIC					= 3;

	public static final int	LAMBERT_CONFORMAL_CONIC					= 4;

	public static final int	MERCATOR								= 5;

	public static final int	POLAR_STEREOGRAPHIC						= 6;

	public static final int	POLYCONIC								= 7;

	public static final int	EQUIDISTANT_CONIC						= 8;

	public static final int	TRANSVERSE_MERCATOR						= 9;

	public static final int	STEREOGRAPHIC							= 10;

	public static final int	LAMBERT_AZIMUTHAL_EQUAL_AREA			= 11;

	public static final int	AZIMUTHAL_EQUIDISTANT					= 12;

	public static final int	GNOMONIC								= 13;

	public static final int	ORTHOGRAPHIC							= 14;

	public static final int	GENERAL_VERTICAL_NEARSIDE_PERSPECTIVE	= 15;

	public static final int	SINUSOIDAL								= 16;

	public static final int	EQUIRECTANGULAR							= 17;

	public static final int	MILLER_CYLINDRICAL						= 18;

	public static final int	VAN_DER_GRINTEN							= 19;

	public static final int	OBLIQUE_MERCATOR						= 20;

	public static final int	ROBINSON								= 21;

	public static final int	SPACE_OBLIQUE_MERCATOR					= 22;

	public static final int	ALASKA_CONFORMAL						= 23;

	public static final int	INTERRUPTED_GOODE_HOMOLOSINE			= 24;

	public static final int	MOLLWEIDE								= 25;

	public static final int	INTERRUPTED_MOLLWEIDE					= 26;

	public static final int	HAMMER									= 27;

	public static final int	WAGNER_IV								= 28;

	public static final int	WAGNER_VII								= 29;

	public static final int	OBLATED_EQUAL_AREA						= 30;

	public static final int	INTEGERIZED_SINUSOIDAL					= 31;

	//**************************************************

	/** this is the type of ellipsoid. Enumerated in Ellipsoid class */
	protected int			_ellipsoidType;

	/**
	 * rasterSpace is the location of the tie point in the context of the image
	 */
	protected double		_rasterSpaceI, _rasterSpaceJ, _rasterSpaceK;

	/**
	 * insertion is the location of the tie point in the context of the earth In
	 * this class, should be lat/lon coordinates. Subclasses will probably be in
	 * their own model space coords
	 */
	protected double		_insertionX, _insertionY, _insertionZ;
//  protected double _modelSpaceX, _modelSpaceY, _modelSpaceZ;

	/**
	 * This is the change in lat/lon per pixel
	 */
	protected double		_scaleX, _scaleY, _scaleZ;

	/**
	 * The number of rows and columns of the underlying image
	 */
	protected int			_numRows, _numCols;

	/** the type of projection. must be one of the enumerated types */
	protected int			_type;

	/** the unit of resolution. must be one of the enumerated types */
	protected int			_unit;

	/**
	 * 
	 */
	public Projection() {
		setType(Projection.GEOGRAPHIC);
	}

	/**
	 * 
	 * @param rasterI
	 * @param rasterJ
	 * @param modelX
	 * @param modelY
	 * @param scaleX
	 * @param scaleY
	 * @param numRow
	 * @param numCol
	 */
	public Projection(double rasterI, double rasterJ, double insertX, double insertY, double scaleX, double scaleY, int numRow, int numCol) {
		setRasterSpaceI(rasterI);
		setRasterSpaceJ(rasterJ);
		setInsertionX(insertX);
		setInsertionY(insertY);
		setScaleX(scaleX);
		setScaleY(scaleY);
		setNumRows(numRow);
		setNumCols(numCol);
		setType(Projection.GEOGRAPHIC);
	}

	/**
	 * Get the type of projection
	 * 
	 * @return the type of this projection
	 */
	public int getType() {
		return _type;
	}

	/**
	 * Set the type of this projection
	 * 
	 * @param typ
	 *            the type of this projection
	 */
	protected void setType(int typ) {
		_type = typ;
	}

	/**
	 * Set the column index of the tie point in the context of the image.
	 * 
	 * @param ri
	 *            the new column index
	 */
	public void setRasterSpaceI(double ri) {
		_rasterSpaceI = ri;
	}

	/**
	 * Return the column index of the tie point in the context of the image.
	 * 
	 * @return the column index
	 */
	public double getRasterSpaceI() {
		return _rasterSpaceI;
	}

	/**
	 * Set the row index of the tie point in the context of the image.
	 * 
	 * @param rj
	 *            the new row
	 */
	public void setRasterSpaceJ(double rj) {
		_rasterSpaceJ = rj;
	}

	/**
	 * Get the row index of the tie point in the context of the image.
	 * 
	 * @return the row index
	 */
	public double getRasterSpaceJ() {
		return _rasterSpaceJ;
	}

	/**
	 * 
	 * @param rk
	 */
	public void setRasterSpaceK(double rk) {
		_rasterSpaceK = rk;
	}

	/**
	 * 
	 * @return
	 */
	public double getRasterSpaceK() {
		return _rasterSpaceK;
	}

	/**
	 * Set the column value of the tie pt in the context of the earth.
	 * 
	 * @param ix
	 *            the column value of the tie pt in earth coordinates
	 */
	public void setInsertionX(double ix) {
		_insertionX = ix;
	}

	/**
	 * Return the column value of the tie pt in the context of the earth.
	 * 
	 * @return the value of the tie pt in the x direction
	 */
	public double getInsertionX() {
		return _insertionX;
	}

	/**
	 * Set the row value of the tie pt in the context of the earth.
	 * 
	 * @param iy
	 *            the row value of the tie pt in earth coordinates
	 */
	public void setInsertionY(double iy) {
		_insertionY = iy;
	}

	/**
	 * Return the row value of the tie pt in the context of the earth.
	 * 
	 * @return the value of the tie pt in the y direction
	 */
	public double getInsertionY() {
		return _insertionY;
	}

	public int getUnit() {
		return _unit;
	}

	public void setUnit(int unit) {
		_unit = unit;
	}

	/**
	 * 
	 * @param iz
	 */
	public void setInsertionZ(double iz) {
		_insertionZ = iz;
	}

	/**
	 * 
	 * @return
	 */
	public double getInsertionZ() {
		return _insertionZ;
	}

	/**
	 * Set the scale per pixel in the x direction
	 * 
	 * @param sx
	 *            the new scale in the x direction
	 */
	public void setScaleX(double sx) {
		_scaleX = sx;
	}

	/**
	 * Return the scale per pixel in the x direction
	 * 
	 * @return the new scale in the x direction
	 */
	public double getScaleX() {
		return _scaleX;
	}

	/**
	 * 
	 * Set the scale per pixel in the y direction
	 * 
	 * @param sy
	 *            the new scale in the y direction
	 */
	public void setScaleY(double sy) {
		_scaleY = sy;
	}

	/**
	 * 
	 * Return the scale per pixel in the y direction
	 * 
	 * @return the new scale in the y direction
	 */
	public double getScaleY() {
		return _scaleY;
	}

	/**
	 * 
	 * @param sz
	 */
	public void setScaleZ(double sz) {
		_scaleZ = sz;
	}

	/**
	 * 
	 * @return
	 */
	public double getScaleZ() {
		return _scaleZ;
	}

	/**
	 * Set the number of rows. This is the number of rows in the underlying
	 * image.
	 * 
	 * @param nr
	 *            the number of rows
	 */
	public void setNumRows(int nr) {
		_numRows = nr;
	}

	/**
	 * Get the number of rows in the underlying image.
	 * 
	 * @return the number of rows in the image
	 */
	public int getNumRows() {
		return _numRows;
	}

	/**
	 * Set the number of columns. This is the number of columns in the
	 * underlying image
	 * 
	 * @param nc
	 *            the number of columns in the image
	 */
	public void setNumCols(int nc) {
		_numCols = nc;
	}

	/**
	 * Get the number of columns in the underlying image.
	 * 
	 * @return the number of columns in the image
	 */
	public int getNumCols() {
		return _numCols;
	}

	/**
	 * 
	 * @param newSampRatio
	 * @param numRows
	 * @param numCols
	 */
	public void subsample(int newSampRatio, int numRows, int numCols) {
		//We maintain proper georeferencing by scaling the spatial resolution
		//values by the sample ratio.  If every fifth pixel is chosen,
		//the samp ratio is assumed to be 5.
//    double colres = GetColumnResolution();
//    double rowres = GetRowResolution();
		double rowres = getScaleY();

		//----------------- Added by DC 10.18.2004
		// if the tie point is in the lower left corner, then we might need to reset
		// the northing insertion point.  The point will move if the last row
		// of the original image was not included in the subsample
		if (getRasterSpaceJ() != 0) {

			// This is the total number of rows in the unsampled image.
			int totalRows = numRows;

			// this is the number of extra rows that are dropped when the image
			// was subsampled
			int extraRows = totalRows % newSampRatio;
			// extraRows is the number of rows truncated from the image when
			// subsampling

			// the change in the tie point.
			double deltaUTM = extraRows * rowres;

			double northing = getInsertionY();// GetNorthingInsertionValue();

			// move the tie point up by deltaUTM
			//northing -= Math.abs(deltaUTM);
			northing += deltaUTM;

			// reset the northing insertion value
			setInsertionY(northing);//SetNorthingInsertionValue(northing);
		}
		//------------------ End Added

		//SetColumnResolution(GetColumnResolution() * newSampRatio);
		//SetRowResolution(GetRowResolution() * newSampRatio);

		setScaleX(getScaleX() * newSampRatio);
		setScaleY(getScaleY() * newSampRatio);
		//this.setNumRows(numRows);
		//this.setNumCols(numCols);
	}

	/**
	 * Go from raster space to earth coordinates. Given a (column,row) location
	 * translate to (latitude,longitude) using the tie point and x/y scales.
	 * 
	 * @param point
	 *            the point in image space
	 * @return the latitude/longitude coordinates of the point
	 * @throws IllegalArgumentException
	 * @throws GeoException
	 */
	public double[] colRowToLatLon(double[] point) throws IllegalArgumentException, GeoException {
		return rasterToEarth(point);
	}

	/**
	 * Go from raster space to earth coordinates. Given a (column,row) location
	 * translate to (latitude,longitude) using the tie point and x/y scales.
	 * 
	 * @deprecated use colRowToLatLon instead
	 * @param point
	 *            the point in image space
	 * @return the latitude/longitude coordinates of the point
	 * @throws IllegalArgumentException
	 * @throws GeoException
	 */
	@Deprecated
	public double[] ColumnRow2LatLng(double[] point) throws IllegalArgumentException, GeoException {
		return colRowToLatLon(point);
	}

	/**
	 * Go from earth space to raster space. Given a (lat,lng) coordinate,
	 * translate to (column,row) of the image using the tie pt and x/y scales.
	 * 
	 * @param point
	 *            the point on the earth in lat,lng
	 * @return the pixel location in the image that corresponds to point
	 * @throws IllegalArgumentException
	 * @throws GeoException
	 */
	public double[] latLonToColRow(double[] point) throws IllegalArgumentException, GeoException {
		return earthToRaster(point);
	}

	/**
	 * 
	 * @deprecated use latLonToColRow instead
	 * @param point
	 * @return
	 * @throws IllegalArgumentException
	 * @throws GeoException
	 */
	@Deprecated
	public double[] LatLng2ColumnRow(double[] point) throws IllegalArgumentException, GeoException {
		return latLonToColRow(point);
	}

	/**
	 * Go from column,row to lat,lon
	 * 
	 * @param point
	 *            {column,row}
	 * @return {lat.lon}
	 * @throws IllegalArgumentException
	 * @throws GeoException
	 */
	public double[] rasterToEarth(double[] point) throws IllegalArgumentException, GeoException {
		double[] ret = new double[2];

		// this is the longitude.  (the column)
		//ret[1] = point[0] * GetColumnResolution() + GetEastingInsertionValue();
		ret[1] = point[0] * getScaleX() + getInsertionX();

		// this is the latitude (the row)
		// must do something special if the insertion pt is in the lower left corner
/*
 * if ((_rasterSpaceJ == _numRows) || (_rasterSpaceJ == _numRows - 1)) { double
 * deltaY = _numRows - point[1]; //ret[0] = deltaY * GetRowResolution() +
 * GetNorthingInsertionValue(); ret[0] = deltaY * getScaleY() + getInsertionY(); }
 * else { // double deltaY = point[1]; //ret[0] = GetNorthingInsertionValue() -
 * GetRowResolution() * point[1]; ret[0] = getInsertionY() - getScaleY() *
 * point[1]; }
 */
		// changes by PB on 05-21-2008
		ret[0] = point[1] * getScaleY() + getInsertionY();

		return ret;
	}

	/**
	 * 
	 * @param point
	 * @return
	 * @throws IllegalArgumentException
	 * @throws GeoException
	 */
	public double[] earthToRaster(double[] point) throws IllegalArgumentException, GeoException {

		double[] ret = new double[2];
		double distLng = point[1] - getInsertionX();

		ret[0] = distLng / getScaleX();

		//if ((_rasterSpaceJ == _numRows) || (_rasterSpaceJ == _numRows - 1)) {
		//	double distLat = point[0] - getInsertionY();
		//	ret[1] = _numRows - distLat / Math.abs(getScaleY());
		//} else {
		//	double distLat = getInsertionY() - point[0];
		//	ret[1] = distLat / getScaleY();
		//}

		// changes by PB on 05-21-2008
		double distLat = point[0] - getInsertionY();
		ret[1] = distLat / getScaleY();

		return ret;
	}

	/**
	 * 
	 * @param key
	 */
	public void setEllipsoid(int key) {
		_ellipsoidType = key;
	}

	/**
	 * 
	 * @return
	 */
	public int getEllipsoid() {
		return _ellipsoidType;
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
	/*
	 * private double[] toLatLon(double[] point) throws GeoException { double[]
	 * ret = new double[2]; ret[1] = point[0] * GetColumnResolution() +
	 * GetEastingInsertionValue(); //if (geoinfo.GetRowResolution() > 0) { //
	 * LAM //if(_isLowerLeftTiePoint) { if(_rasterSpaceJ == _numRows-1) { double
	 * distRow = _numRows - point[1]; ret[0] = distRow * GetRowResolution() +
	 * GetNorthingInsertionValue(); } else { ret[0] =
	 * GetNorthingInsertionValue() + GetRowResolution() * point[1]; } return
	 * ret; }
	 */

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
	/*
	 * private double[] fromLatLon(double[] point) throws GeoException { return
	 * null; }
	 */

	/**
	 * @deprecated use setColumnResolution
	 * @param r
	 */
	@Deprecated
	public void SetColumnResolution(double r) {
		setScaleX(r);
	}

	/**
	 * @deprecated use setRowResolution
	 * @param r
	 */
	@Deprecated
	public void SetRowResolution(double r) {
		setScaleY(r);
	}

	/**
	 * @deprecated use getColumnResolution
	 * @return
	 */
	@Deprecated
	public double GetColumnResolution() {
		return getScaleX();
	}

	/**
	 * @deprecated use getRowResolution
	 * @return
	 */
	@Deprecated
	public double GetRowResolution() {
		return getScaleY();
	}

	/**
	 * @deprecated use getEastingInsertionValue
	 * @return
	 */
	@Deprecated
	public double GetEastingInsertionValue() {
		return getInsertionX();
	}

	/**
	 * @deprecated use setEastingInsertionValue
	 * @return
	 */
	@Deprecated
	public void SetEastingInsertionValue(double val) {
		setInsertionX(val);
	}

	/**
	 * @deprecated use getNorthingInsertionValue
	 * @return
	 */
	@Deprecated
	public double GetNorthingInsertionValue() {
		return getInsertionY();
	}

	/**
	 * @deprecated use setNorthingInsertionValue
	 * @return
	 */
	@Deprecated
	public void SetNorthingInsertionValue(double val) {
		setInsertionY(val);
	}

	/**
	 * @deprecated getGeoScaleX
	 * @return
	 */
	@Deprecated
	public double GetGeoScaleX() {
		return getScaleX();
	}

	/**
	 * @deprecated use getGeoScaleY
	 * @return
	 */
	@Deprecated
	public double GetGeoScaleY() {
		return getScaleY();
	}

	/**
	 * @deprecated use getGeoScaleZ
	 * @return
	 */
	@Deprecated
	public double GetGeoScaleZ() {
		return getScaleZ();
	}

	/**
	 * @deprecated use getRasterSpaceI
	 * @return
	 */
	@Deprecated
	public double GetRasterSpaceI() {
		return getRasterSpaceI();
	}

	/**
	 * @deprecated use getRasterSpaceJ
	 * @return
	 */
	@Deprecated
	public double GetRasterSpaceJ() {
		return getRasterSpaceJ();
	}

	/**
	 * @deprecated use getRasterSpaceK
	 * @return
	 */
	@Deprecated
	public double GetRasterSpaceK() {
		return getRasterSpaceK();
	}

	/**
	 * @deprecated use setRasterSpaceI instead
	 * @param ri
	 */
	@Deprecated
	public void SetRasterSpaceI(double ri) {
		setRasterSpaceI(ri);
	}

	/**
	 * @deprecated use setRasterSpaceJ instead
	 * @param rj
	 */
	@Deprecated
	public void SetRasterSpaceJ(double rj) {
		setRasterSpaceJ(rj);
	}

	/**
	 * @deprecated use setRasterSpaceK instead
	 * @param rk
	 */
	@Deprecated
	public void SetRasterSpaceK(double rk) {
		setRasterSpaceK(rk);
	}

	/*
	 * public static void main(String[] args) { Projection geo = new
	 * Projection(); geo.setNumCols(15374); geo.setNumRows(10105);
	 * geo.setInsertionX(-91.63638888918); geo.setRasterSpaceI(0);
	 * geo.setInsertionY(39.762777777778); geo.setRasterSpaceJ(10104);
	 * geo.SetColumnResolution(0.0002777777778);
	 * geo.SetRowResolution(0.0002777777778); double[] point = {15373,0};
	 * double[] pt = {39.763, -91.636}; try { double[] res =
	 * geo.rasterToEarth(point); System.out.println(res[0]);
	 * System.out.println(res[1]); point = new double[]{15373,10104}; res =
	 * geo.rasterToEarth(point); System.out.println(res[0]);
	 * System.out.println(res[1]); res = geo.earthToRaster(pt);
	 * System.out.println(res[0]); System.out.println(res[1]); } catch(Exception
	 * ex) { } }
	 */

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		try {
			sb.append("ellipsoid = " + Ellipsoid.getName(_ellipsoidType));
			sb.append("\n");
		} catch (Exception ex) {

		}
		sb.append("rasterSpaceI = " + _rasterSpaceI);
		sb.append("\n");
		sb.append("rasterSpaceJ = " + _rasterSpaceJ);
		sb.append("\n");
		sb.append("rasterSpaceK = " + _rasterSpaceK);
		sb.append("\n");
		sb.append("insertionX = " + _insertionX);
		sb.append("\n");
		sb.append("insertionY = " + _insertionY);
		sb.append("\n");
		sb.append("insertionZ = " + _insertionZ);
		sb.append("\n");
		sb.append("scaleX = " + _scaleX);
		sb.append("\n");
		sb.append("scaleY = " + _scaleY);
		sb.append("\n");
		sb.append("scaleZ = " + _scaleZ);
		sb.append("\n");
		sb.append("numrows = " + _numRows);
		sb.append("\n");
		sb.append("numcols = " + _numCols);
		sb.append("\n");
		sb.append("Max West Lng = " + GetMaxWestLng());
		sb.append("\n");
		sb.append("Max South Lat = " + GetMaxSouthLat());
		sb.append("\n");
		sb.append("Max East Lng = " + GetMaxEastLng());
		sb.append("\n");
		sb.append("Max North Lat = " + GetMaxNorthLat());
		sb.append("\n");

		if (!(this instanceof ModelProjection)) {
			sb.append("\n");
			sb.append(getType() + " = Projection.GEOGRAPHIC\n");
		}

		return sb.toString();
	}

	public double GetMaxWestLng() {
		return getMaxWestLongitude();
	}

	/**
	 * 
	 * @return
	 */
	public double getMaxWestLongitude() {
		double[] pt = new double[] { 0, 0 };
		try {
			double[] ret = this.colRowToLatLon(pt);
			return ret[1];
		} catch (GeoException ex) {
		}
		return 0;
	}

	/**
	 * @deprecated use getMaxSouthLatitude instead
	 * @return
	 */
	@Deprecated
	public double GetMaxSouthLat() {
		return getMaxSouthLatitude();
	}

	/**
	 * 
	 * @return
	 */
	public double getMaxSouthLatitude() {
		double[] pt = new double[] { 0, _numRows };
		try {
			double[] ret = this.colRowToLatLon(pt);
			return ret[0];
		} catch (GeoException ex) {
		}
		return 0;
	}

	/**
	 * @deprecated use getMaxEastLongitude instead
	 * @return
	 */
	@Deprecated
	public double GetMaxEastLng() {
		return getMaxEastLongitude();
	}

	/**
	 * 
	 * @return
	 */
	public double getMaxEastLongitude() {
		double[] pt = new double[] { _numCols, 0 };
		try {
			double[] ret = this.colRowToLatLon(pt);
			return ret[1];
		} catch (GeoException ex) {
		}
		return 0;
	}

	/**
	 * @deprecated use getMaxNorthLatitude instead
	 * @return
	 */
	@Deprecated
	public double GetMaxNorthLat() {
		return getMaxNorthLatitude();
	}

	/**
	 * 
	 * @return
	 */
	public double getMaxNorthLatitude() {
		double[] pt = new double[] { 0, 0 };
		try {
			double[] ret = this.colRowToLatLon(pt);
			return ret[0];
		} catch (GeoException ex) {
			return 0;
		}
	}

	/**
	 * Create a deep copy of this Projection. The copy is created by serializing
	 * this object and reading it back in. If this fails, null is returned.
	 * 
	 * @return
	 */
	public Projection getCopy() {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(this);
			byte buf[] = baos.toByteArray();
			oos.close();
			ByteArrayInputStream bais = new ByteArrayInputStream(buf);
			ObjectInputStream ois = new ObjectInputStream(bais);
			Projection p = (Projection) ois.readObject();
			ois.close();
			return p;
		} catch (IOException ioe) {
			return null;
		} catch (ClassNotFoundException cnfe) {
			return null;
		}

/*
 * Projection retval = new Projection(); retval._ellipsoidType =
 * this._ellipsoidType; retval._insertionX = this._insertionX;
 * retval._insertionY = this._insertionY; retval._insertionZ = this._insertionZ;
 * retval._numCols = this._numCols; retval._numRows = this._numRows;
 * retval._rasterSpaceI = this._rasterSpaceI; retval._rasterSpaceJ =
 * this._rasterSpaceJ; retval._rasterSpaceK = this._rasterSpaceK; retval._scaleX =
 * this._scaleX; retval._scaleY = this._scaleY; retval._scaleZ = this._scaleZ;
 * retval._type = this._type;
 * 
 * return retval;
 */
	}

	/**
	 * 
	 * @param scalex
	 * @param scaley
	 */
	public void scale(double scalex, double scaley) {
		SetColumnResolution(GetColumnResolution() / scalex);
		SetRowResolution(GetRowResolution() / scaley);
	}

// LAM
	public void resample(double colscale, double rowscale) {
		/*
		 * double tileColRes; double tileRowRes; // get the column and row
		 * resolution // if it is in degrees, convert to meters if
		 * (isInDegrees(tile)) { Point2DDouble colRowRes =
		 * convertDegreeScale2MeterScale(tile); tileColRes =
		 * colRowRes.ptsDouble[0]; tileRowRes = colRowRes.ptsDouble[1]; } else {
		 * tileColRes = tile.GetColumnResolution(); tileRowRes =
		 * Math.abs(tile.GetRowResolution()); }
		 */
/*
 * // if they differ by more than EPSILON if (Math.abs(GetColumnResolution() -
 * colscale) > EPSILON) { float bottomSampRatioCol = (float) (colscale /
 * GetColumnResolution()); float bottomSampRatioRow = (float) (rowscale /
 * GetRowResolution()); SetColumnResolution(GetColumnResolution() *
 * bottomSampRatioCol); SetRowResolution(GetRowResolution() *
 * bottomSampRatioRow); //tile.SetGeoScaleX(tile.GetColumnResolution());
 * //tile.SetGeoScaleY(Math.abs(tile.GetRowResolution())); }
 */

		// changes by PB 05-21-2008
		// if they differ by more than EPSILON
		if (Math.abs(getScaleX() - colscale) > EPSILON) {
			float bottomSampRatioCol = (float) (colscale / getScaleX());
			float bottomSampRatioRow = (float) (rowscale / getScaleY());
			setScaleX(getScaleX() * bottomSampRatioCol);
			setScaleY(getScaleY() * bottomSampRatioRow);
		}
		//return tile;
	}

	public static final double	EPSILON	= Math.pow(10, -4);

	/**
	 * The first line should be of the form "# = NameOfClass" with the remaining
	 * lines recording the values of the primatives necessary to reconstruct the
	 * details of the projection in a human readable form.
	 * 
	 * @return A string containing all the values of the primatives with
	 *         descriptions.
	 */
	public String getProjectionParametersAsString() {
		System.err.println("ERROR: Projection Class " + this.getClass() + "\n\tdoes not have have getProjectionParametersAsString() overridden.");

//    template based on NewAlbersEqualAreaConic...

//    String parametersAsString = new String();
//
//    parametersAsString += (this.getType() + " = " + this.getClass() + "\n");
//    parametersAsString += (_falseEast + " = false easting\n");
//    parametersAsString += (_falseNorth + " = false northing\n");
//    parametersAsString += (_lat0 + " = latitude of origin\n");
//    parametersAsString += (_lat1 + " = first standard parallel\n");
//    parametersAsString += (_lat2 + " = second standard parallel\n");
//    parametersAsString += (_lon0 + " = longitude of origin\n");
//    parametersAsString += (_rMaj + " = major radius\n");
//    parametersAsString += (_rMin + " = minor radius\n");

		return null;
	}

	public void setProjectionParametersFromString(String paramString) {
		System.err.println("ERROR: Projection Class " + this.getClass() + "\n\tdoes not have have setProjectionParametersFromString() overridden.");

		// template based on NewAlbersEqualAreaConic

		/*
		 * int indexOfDelimiter = -1; int indexOfNewline = -1; // assuming that
		 * there is at least one character before the first newline String
		 * valueAsString = null;
		 * 
		 * String magicString = " = "; String magicNewline = "\n"; // note that
		 * the indexOfDelimiter gets bumped up as we go along... // we will skip
		 * the first line because that contains the info on what type of //
		 * projection this is and what class should be used... that info will
		 * already // known if we have gotten this far... // indexOfDelimiter =
		 * paramString.indexOf(magicString); // first number read starts at the
		 * beginning of the string... // valueAsString =
		 * paramString.substring(indexOfNewline + 1,indexOfDelimiter);
		 * indexOfNewline = paramString.indexOf(magicNewline,indexOfDelimiter);
		 * indexOfDelimiter = paramString.indexOf(magicString,indexOfNewline);
		 * valueAsString = paramString.substring(indexOfNewline +
		 * 1,indexOfDelimiter); _falseEast = Double.parseDouble(valueAsString);
		 * 
		 * indexOfNewline = paramString.indexOf(magicNewline,indexOfDelimiter);
		 * indexOfDelimiter = paramString.indexOf(magicString,indexOfNewline);
		 * valueAsString = paramString.substring(indexOfNewline +
		 * 1,indexOfDelimiter); _falseNorth = Double.parseDouble(valueAsString);
		 * 
		 * indexOfNewline = paramString.indexOf(magicNewline,indexOfDelimiter);
		 * indexOfDelimiter = paramString.indexOf(magicString,indexOfNewline);
		 * valueAsString = paramString.substring(indexOfNewline +
		 * 1,indexOfDelimiter); _lat0 = Double.parseDouble(valueAsString);
		 * 
		 * indexOfNewline = paramString.indexOf(magicNewline,indexOfDelimiter);
		 * indexOfDelimiter = paramString.indexOf(magicString,indexOfNewline);
		 * valueAsString = paramString.substring(indexOfNewline +
		 * 1,indexOfDelimiter); _lat1 = Double.parseDouble(valueAsString);
		 * 
		 * indexOfNewline = paramString.indexOf(magicNewline,indexOfDelimiter);
		 * indexOfDelimiter = paramString.indexOf(magicString,indexOfNewline);
		 * valueAsString = paramString.substring(indexOfNewline +
		 * 1,indexOfDelimiter); _lat2 = Double.parseDouble(valueAsString);
		 * 
		 * indexOfNewline = paramString.indexOf(magicNewline,indexOfDelimiter);
		 * indexOfDelimiter = paramString.indexOf(magicString,indexOfNewline);
		 * valueAsString = paramString.substring(indexOfNewline +
		 * 1,indexOfDelimiter); _lon0 = Double.parseDouble(valueAsString);
		 * 
		 * indexOfNewline = paramString.indexOf(magicNewline,indexOfDelimiter);
		 * indexOfDelimiter = paramString.indexOf(magicString,indexOfNewline);
		 * valueAsString = paramString.substring(indexOfNewline +
		 * 1,indexOfDelimiter); _rMaj = Double.parseDouble(valueAsString);
		 * 
		 * indexOfNewline = paramString.indexOf(magicNewline,indexOfDelimiter);
		 * indexOfDelimiter = paramString.indexOf(magicString,indexOfNewline);
		 * valueAsString = paramString.substring(indexOfNewline +
		 * 1,indexOfDelimiter); _rMin = Double.parseDouble(valueAsString);
		 * 
		 */
	}
}
