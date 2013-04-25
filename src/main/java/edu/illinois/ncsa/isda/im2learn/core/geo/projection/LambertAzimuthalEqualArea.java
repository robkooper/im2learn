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

import edu.illinois.ncsa.isda.im2learn.core.geo.*;

/**
 *
 * @author talumbau
 * @author clutter
 */
public class LambertAzimuthalEqualArea extends ModelProjection {


  private double _eccentricitySquared;
  private double _radius;
  private double _projCenterLongitude;
  private double _projCenterLatitude;
  private double _eastingInsertion;
  private double _northingInsertion;

  // LAM -- are esquared and a really needed?  Are they used to calculate the
  // radius?
  
  //note: the following class variables are only used in PRJWriter and ProjectionLoaderPRJ
  private String _geographicCoordinateSystem;
  private String _projectedCoordinateSystem;
  private String _datum;
  private String _primeMeridian;
  private double _primeMeridianValue;
  private String _angularUnit;
  private double _angularUnitValue;
  private String _linearUnit;
  private double _linearUnitValue;

  public LambertAzimuthalEqualArea() {
    setType(Projection.LAMBERT_AZIMUTHAL_EQUAL_AREA);
  }
  
  public void setEccentricSqure(double esq)
  {
	  _eccentricitySquared = esq;
  }
  
  public void setRadius(double radius)
  {
	  _radius = radius;
  }
  
  public void setProjCenter(double projLat, double projLng)
  {
	  _projCenterLatitude  = projLat;
	  _projCenterLongitude = projLng;
  }
  
  public void setInsertion(double northing, double easting)
  {
	  _northingInsertion = northing;
	  _eastingInsertion  = easting;	
  }

  /**
   * this goes from column row to meters
   */
  public double[] rasterToModel(double[] pnt) throws GeoException {
  //private double[] LambertColumnRow2UTMNorthingEasting(double[] pnt) {
    double inPixCol = pnt[0];
    double inPixRow = pnt[1];

//    double UTMnorthing = ( - (inPixRow * Math.abs(GetRowResolution())) +
//                          GetNorthingInsertionValue());
//    double UTMeasting = ( (inPixCol * Math.abs(GetColumnResolution())) +
//                         GetEastingInsertionValue());

    double UTMnorthing = ( - (inPixRow * Math.abs(getScaleY())) +
        GetNorthingInsertionValue());
    double UTMeasting = ( (inPixCol * Math.abs(getScaleX())) +
       GetEastingInsertionValue());

    double[] p = new double[2];
    p[0] = UTMeasting;
    p[1] = UTMnorthing;
    return p;
  }

  /**
   * This goes from meters to column row
   * @param pnt
   * @return
   */
  public double[] modelToRaster(double[] pnt) throws GeoException {
  //private double[] LambertUTMNorthingEasting2ColumnRow(double[] pnt) {

    /*double x1 = ((inPixX*Math.abs(_columnResolution))+_eastingInsertionValue)/_radius;
             double y1 = ((inPixY*Math.abs(_rowResolution))-_northingInsertionValue)/(-_radius);*/

	  double easting = pnt[0];
	  double northing = pnt[1];
    

    //Rounding removed 10/28/03
    //double column = Math.round(((easting)-_imgGeoObject.GetEastingInsertionValue())/Math.abs(_imgGeoObject.GetColumnResolution()));
    //double row = Math.round((-(northing)+ _imgGeoObject.GetNorthingInsertionValue())/Math.abs(_imgGeoObject.GetRowResolution()));
    double column = ( (easting) - GetEastingInsertionValue()) /
        Math.abs(getScaleX());
    double row = ( - (northing) + GetNorthingInsertionValue()) /
        Math.abs(getScaleY());

    double[] ret = new double[2];

    ret[0] = column;
    ret[1] = row;
    return ret;
  }

  /**
   * this goes from meters to lat lng
   * @param pnt
   * @return
   */
  public double[] modelToEarth(double[] pnt) {
  //private double[] LambertUTMNorthingEasting2LatLng(double[] pnt) {

	  double UTMeasting = pnt[0];
	  double UTMnorthing = pnt[1];
    

    double y1 = UTMnorthing;
    double x1 = UTMeasting;

    x1 = x1 / GetRadius(); //divide values by radius of sphere to
    y1 = y1 / GetRadius(); //scale down to unit sphere, which is
    //needed to apply the equations at the
    //address above.

    double rho = Math.sqrt(x1 * x1 + y1 * y1);
    double c = 2 * Math.asin(.5 * rho);
    double frac = (y1 * Math.sin(c) *
                   Math.cos(Math.toRadians(GetGeoProjCenterLat()))) / rho;

    double denom2 = rho * Math.cos(Math.toRadians(GetGeoProjCenterLat())) *
        Math.cos(c) -
        y1 * Math.sin(Math.toRadians(GetGeoProjCenterLat())) * Math.sin(c);
    double frac2 = (x1 * Math.sin(c)) / denom2;
    double lngOut = GetGeoProjCenterLng() + Math.toDegrees(Math.atan(frac2));

    //Covers the case of moving from -180.000... W Longitude to 180.0...01
    //E Longitude
    if ( (denom2 < 0) && (x1 < 0)) {
      lngOut = lngOut - 180;
    }
    if (lngOut < -180) {
      lngOut = lngOut + 360;

    }
    double latOut = Math.toDegrees(Math.asin(Math.cos(c) * Math.sin(
        Math.toRadians(GetGeoProjCenterLat())) + frac));

    double[] ret = new double[2];

    ret[1] = lngOut;
    ret[0] = latOut;
    return ret;
  }

  /**
   * This goes from lat lng to column row
   * earthToRaster
   * @param pnt
   * @return
   */
/*  public double[] earthToRaster(double[] pnt) throws GeoException {
  //private double[] LambertLatLng2ColumnRow(double[] pnt) {

    double[] ret; // = null;//new Point2DDouble(1);
    //System.err.println("LLL2CR");
    //System.err.println("the latitude is "+pnt.ptsDouble[0]);
    //System.err.println("the longitue is "+pnt.ptsDouble[1]);
    //ret = LambertLatLng2UTMNorthingEasting(pnt);
    ret = earthToModel(pnt);
    //System.err.println("the northing is "+ret.ptsDouble[0]);
    //System.err.println("the easting is "+ret.ptsDouble[1]);
    //ret = LambertUTMNorthingEasting2ColumnRow(ret);
    ret = modelToRaster(ret);
    //System.err.println("the column is "+ret.ptsDouble[0]);
    //System.err.println("the row is "+ret.ptsDouble[1]);

    return ret;
  }*/

  /**
   * This goes from lat/lng to meters
   * @param pnt
   * @return
   */
  public double[] earthToModel(double[] pnt) throws GeoException {
  //private double[] LambertLatLng2UTMNorthingEasting(double[] pnt) {

    double inLng = pnt[1];
    double inLat = pnt[0];

    double denom = 1 + Math.sin(Math.toRadians(GetGeoProjCenterLat())) *
        Math.sin(Math.toRadians(inLat)) +
        Math.cos(Math.toRadians(GetGeoProjCenterLat())) *
        Math.cos(Math.toRadians(inLat)) *
        Math.cos(Math.toRadians(inLng - GetGeoProjCenterLng()));
    double k = Math.sqrt(2 / denom);

    double x = k * Math.cos(Math.toRadians(inLat)) *
        Math.sin(Math.toRadians(inLng - GetGeoProjCenterLng()));

    double y = k * (Math.cos(Math.toRadians(GetGeoProjCenterLat())) *
                    Math.sin(Math.toRadians(inLat)) -
                    Math.sin(Math.toRadians(GetGeoProjCenterLat())) *
                    Math.cos(Math.toRadians(inLat)) *
                    Math.cos(Math.toRadians(inLng - GetGeoProjCenterLng())));

    double retEasting = x * GetRadius();
    double retNorthing = y * GetRadius();

    //int column = (int) Math.round(((x*_radius)-_eastingInsertionValue)/_columnResolution);
    //int row = (int) Math.round((-(y*_radius)+_northingInsertionValue)/_rowResolution);

    double[] ret = new double[2];
    ret[0] = retEasting;
    ret[1] = retNorthing;
    return ret;
  }

  public double GetGeoProjCenterLat() {
    return this._projCenterLatitude;
  }

  public double GetGeoProjCenterLng() {
	  
    return this._projCenterLongitude;
  }

  /*public double GetColumnResolution() {
    return 0;
  }*/

  /*public double GetRowResolution() {
    return 0;
  }*/

  public double GetEastingInsertionValue() {
    return _eastingInsertion;
  }

  public double GetNorthingInsertionValue() {
    return _northingInsertion;
  }

  public double GetRadius() {
    return _radius;
  }
  
  public String getProjectionParametersAsString() {
    String parametersAsString = new String();

    parametersAsString += (this.getType() + " = " + this.getClass() + "\n");
    parametersAsString += (_eccentricitySquared + " = eccentricity squared\n");
    parametersAsString += (_radius + " = radius\n");
    parametersAsString += (_projCenterLongitude + " = projection center longitude\n");
    parametersAsString += (_projCenterLatitude + " = projection center latitude\n");
    parametersAsString += (_eastingInsertion + " = easting insertion\n");
    parametersAsString += (_northingInsertion + " = northing insertion\n");

 //   System.out.println("[" + parametersAsString + "]");
    
    return parametersAsString;
  }
  
  public void setProjectionParametersFromString(String paramString) {
    int indexOfDelimiter  = -1;
    int indexOfNewline    = -1; // assuming that there is at least one character before the first newline
    String valueAsString  = null;
    
    String magicString = " = ";
    String magicNewline = "\n";
    
    // note that the indexOfDelimiter gets bumped up as we go along...

    // we will skip the first line because that contains the info on what type of
    // projection this is and what class should be used... that info will already
    // known if we have gotten this far...
    
//    indexOfDelimiter  = paramString.indexOf(magicString); // first number read starts at the beginning of the string...
//    valueAsString     = paramString.substring(indexOfNewline + 1,indexOfDelimiter);
    indexOfNewline    = paramString.indexOf(magicNewline,indexOfDelimiter);
    indexOfDelimiter  = paramString.indexOf(magicString,indexOfNewline);
    valueAsString     = paramString.substring(indexOfNewline + 1,indexOfDelimiter);
    _eccentricitySquared = Double.parseDouble(valueAsString);

    indexOfNewline    = paramString.indexOf(magicNewline,indexOfDelimiter);
    indexOfDelimiter  = paramString.indexOf(magicString,indexOfNewline);
    valueAsString     = paramString.substring(indexOfNewline + 1,indexOfDelimiter);
    _radius = Double.parseDouble(valueAsString);

    indexOfNewline    = paramString.indexOf(magicNewline,indexOfDelimiter);
    indexOfDelimiter  = paramString.indexOf(magicString,indexOfNewline);
    valueAsString     = paramString.substring(indexOfNewline + 1,indexOfDelimiter);
    _projCenterLongitude = Double.parseDouble(valueAsString);

    indexOfNewline    = paramString.indexOf(magicNewline,indexOfDelimiter);
    indexOfDelimiter  = paramString.indexOf(magicString,indexOfNewline);
    valueAsString     = paramString.substring(indexOfNewline + 1,indexOfDelimiter);
    _projCenterLatitude = Double.parseDouble(valueAsString);

    indexOfNewline    = paramString.indexOf(magicNewline,indexOfDelimiter);
    indexOfDelimiter  = paramString.indexOf(magicString,indexOfNewline);
    valueAsString     = paramString.substring(indexOfNewline + 1,indexOfDelimiter);
    _eastingInsertion = Double.parseDouble(valueAsString);
  
    indexOfNewline    = paramString.indexOf(magicNewline,indexOfDelimiter);
    indexOfDelimiter  = paramString.indexOf(magicString,indexOfNewline);
    valueAsString     = paramString.substring(indexOfNewline + 1,indexOfDelimiter);
    _northingInsertion = Double.parseDouble(valueAsString);
    
  }

public String get_angularUnit() {
	return _angularUnit;
}

public void set_angularUnit(String unit) {
	_angularUnit = unit;
}

public double get_angularUnitValue() {
	return _angularUnitValue;
}

public void set_angularUnitValue(double unitValue) {
	_angularUnitValue = unitValue;
}

public String get_datum() {
	return _datum;
}

public void set_datum(String _datum) {
	this._datum = _datum;
}

public String get_geographicCoordinateSystem() {
	return _geographicCoordinateSystem;
}

public void set_geographicCoordinateSystem(String coordinateSystem) {
	_geographicCoordinateSystem = coordinateSystem;
}

public String get_linearUnit() {
	return _linearUnit;
}

public void set_linearUnit(String unit) {
	_linearUnit = unit;
}

public double get_linearUnitValue() {
	return _linearUnitValue;
}

public void set_linearUnitValue(double unitValue) {
	_linearUnitValue = unitValue;
}

public String get_primeMeridian() {
	return _primeMeridian;
}

public void set_primeMeridian(String meridian) {
	_primeMeridian = meridian;
}

public double get_primeMeridianValue() {
	return _primeMeridianValue;
}

public void set_primeMeridianValue(double meridianValue) {
	_primeMeridianValue = meridianValue;
}

public String get_projectedCoordinateSystem() {
	return _projectedCoordinateSystem;
}

public void set_projectedCoordinateSystem(String coordinateSystem) {
	_projectedCoordinateSystem = coordinateSystem;
}


}
