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
package edu.illinois.ncsa.isda.imagetools.core.datatype;

import java.io.Serializable;

/*
 * GeoImageObject.java
 *
 */

/**
 * @author talumbau
 * @version 1.0
 */

// this is the class that holds image data of any type

/**
 * @deprecated  use Projection
 */
public class GeoInformation implements Serializable {
    private double _columnResolution;	//spatial resolution in horizontal direction

    private double _rotationX;		//rotation of the image in degrees, not implemented
    private double _rotationY;

    private double _rowResolution;	//spatial resolution in vertical direction
    //(in meters/pixel)

    private double _eastingInsertionValue;//Easting value of insertion point of image
    //as determined by Tiff world file (.tfw)
    private double _northingInsertionValue;//Northing value of insertion point of image
    //as determined by Tiff world file (.tfw)

    private double _GeoProjFalseEasting;	//false values used to translate UTM values
    private double _GeoProjFalseNorthing;	//when doing conversions to lat/long

    private double _maxWestLng;		//maximum west bounding longitude in image
    private double _maxEastLng;		//maximum east bounding longitude in image
    private double _maxNorthLat;		//maximum north bounding latitude in image
    private double _maxSouthLat;		//maximum south bounding latitude in image

    private double _radius;		//if a sphere is used to model the Earth, this
    //is its radius
    private double _majorAxis;		//if an ellipsoid is used to model the Earth,
    //this is its major axis (a)
    private double _minorAxis;		//if an ellipsoid is used to model the Earth,
    //this is its minor axis (b)
    private double _eccentricitySqrd;	//A calculated quantity of an ellipsoid which
    //depends on the minor and major axis values
    private double _inverseFlat;		//A calculated quantity of an ellipsoid which
    //depends on the minor and major axis values
    private double _scaleFactor;		//a parameter of datums such as WGS84 that helps
    //in lat/lng calculations


    private String _datum;    //A well defined set of ellipsoid/map parameters
    //USE OF DATUM IS DEPRECATED

    private double _geoScaleX;		//These three tags are populated from private
    private double _geoScaleY;		//TIFF tag 33550, the ModelPixelScaleTag
    private double _geoScaleZ;		//(see TIFF docs for details)

    private double _GeoProjCenterLat;	//the center latitude of the projection
    private double _GeoProjCenterLng;	//the center longitude of the projection
    //private double _totalRowNum;		//the total number of rows of the image (pixel info)
    //private double _totalColNum;		//the total number of columns of the image (pixel info)
    private String _GeoProjectedCSType;	//a String giving a description of the projection used

    private double _rasterSpaceI;		//These six values help provide a mapping from
    private double _rasterSpaceJ;		//the raster space (pixels), to the model space
    private double _rasterSpaceK;		//(usually given in UTM values).  Every third
    private double _modelSpaceX;		//value matches (I -> X, etc.)  These data are
    private double _modelSpaceY;		//read from private Tiff tag 33922, the Model-
    private double _modelSpaceZ;		//Tie point tag (see tag docs for details)

    private int _modelType;	//0 = Lambert Azimuthal Equal Area
    //1 = Universal Transverse Mercator projection in the
    //    Northern Hemisphere
    //2 = Southern Hemisphere UTM
    //3 = OSGB 1936
    //4 = USGS DEM data
    //98 = testing model
    //99 = testing model

    private int _UTMzone;		//specifies projection longitude for modelType = 1;
    private String _ellipsoidName;    //a string specifying the ellipsoid used to
    //model the Earth
    private int _offsetX = 0;
    private int _offsetY = 0;

    //constructors
    public GeoInformation() {
        ResetGeoImageObject();
    }

    public void ResetGeoImageObject() {
        _columnResolution = -1;
        _rotationX = -1;
        _rotationY = -1;
        _rowResolution = -1;
        _eastingInsertionValue = -1;
        _northingInsertionValue = -1;
        _GeoProjFalseEasting = -1;
        _GeoProjFalseNorthing = -1;
        _maxWestLng = -1;
        _maxEastLng = -1;
        _maxNorthLat = -1;
        _maxSouthLat = -1;
        _radius = -1;
        _majorAxis = -1;
        _minorAxis = -1;
        _eccentricitySqrd = -1;
        _inverseFlat = -1;
        _scaleFactor = -1;
        _datum = "empty";
        _geoScaleX = -1;
        _geoScaleY = -1;
        _geoScaleZ = -1;
        _GeoProjCenterLat = -1;
        _GeoProjCenterLng = -1;
        //_totalRowNum = -1;
        //_totalColNum = -1;
        _rasterSpaceI = -1;
        _rasterSpaceJ = -1;
        _rasterSpaceK = -1;
        _modelSpaceX = -1;
        _modelSpaceY = -1;
        _modelSpaceZ = -1;
        _GeoProjectedCSType = "none";
        _ellipsoidName = "empty";
        _modelType = -1;
        _UTMzone = -1;
    }

    public void PrintGeoImageObject() {
        System.out.println("columnResolution = " + _columnResolution);
        System.out.println("rowResolution = " + _rowResolution);
        System.out.println("maxNorthLat = " + _maxNorthLat);
        System.out.println("maxSouthLat = " + _maxSouthLat);
        System.out.println("maxWestLng = " + _maxWestLng);
        System.out.println("maxEastLng = " + _maxEastLng);
        System.out.println("radius = " + _radius);
        System.out.println("majorAxis = " + _majorAxis);
        System.out.println("minorAxis = " + _minorAxis);
        System.out.println("eccentricitySqrd = " + _eccentricitySqrd);
        System.out.println("inverseFlat = " + _inverseFlat);
        System.out.println("scaleFactor = " + _scaleFactor);
        System.out.println("datum = " + _datum);
        System.out.println("geoScaleX = " + _geoScaleX);
        System.out.println("geoScaleY = " + _geoScaleY);
        System.out.println("geoScaleZ = " + _geoScaleZ);
        System.out.println("GeoProjCenterLat = " + _GeoProjCenterLat);
        System.out.println("GeoProjCenterLng = " + _GeoProjCenterLng);
        System.out.println("rotationX = " + _rotationX);
        System.out.println("rotationY = " + _rotationY);
        System.out.println("eastingInsertionValue = " + _eastingInsertionValue);
        System.out.println("northingInsertionValue = " + _northingInsertionValue);
        System.out.println("GeoProjFalseEasting = " + _GeoProjFalseEasting);
        System.out.println("GeoProjFalseNorthing = " + _GeoProjFalseNorthing);
        //System.out.println("numrows = "+numrows);
        //System.out.println("numcols = "+numcols);
        System.out.println("rasterSpaceI = " + _rasterSpaceI);
        System.out.println("rasterSpaceJ = " + _rasterSpaceJ);
        System.out.println("rasterSpaceK = " + _rasterSpaceK);
        System.out.println("modelSpaceX = " + _modelSpaceX);
        System.out.println("modelSpaceY = " + _modelSpaceY);
        System.out.println("modelSpaceZ = " + _modelSpaceZ);
        System.out.println("_GeoProjectedCSType = " + _GeoProjectedCSType);
        System.out.println("modelType = " + _modelType);
        System.out.println("UTMzone = " + _UTMzone);
        System.out.println("ellipsoidName = " + _ellipsoidName);
    }

    /**
     * Get a double value.
     *
     * @return the spatial resolution in horizontal direction
     */

    public double GetColumnResolution() {
        return _columnResolution;
    }

    /**
     * Get a double value.
     *
     * @return the spatial resolution in vertical direction
     */
    public double GetRowResolution() {
        return _rowResolution;
    }

    /**
     * Get a double value.
     *
     * @return the rotation X of the image in degrees, not implemented
     */

    public double GetRotationX() {
        return _rotationX;
    }

    /**
     * Get a double value.
     *
     * @return the rotation Y of the image in degrees, not implemented
     */
    public double GetRotationY() {
        return _rotationY;
    }

    /**
     * Get a double value.
     *
     * @return the Easting value of insertion point of image as determined by
     *         Tiff world file (.tfw)
     */
    public double GetEastingInsertionValue() {
        return _eastingInsertionValue;
    }

    /**
     * Get a double value.
     *
     * @return the Northing value of insertion point of image as determined by
     *         Tiff world file (.tfw)
     */
    public double GetNorthingInsertionValue() {
        return _northingInsertionValue;
    }

    /**
     * Get a double value.
     *
     * @return the False Easting value of insertion point of image as determined
     *         by Tiff world file (.tfw)
     */

    public double GetGeoProjFalseEasting() {
        return _GeoProjFalseEasting;
    }

    /**
     * Get a double value.
     *
     * @return the False Northing value of insertion point of image as
     *         determined by Tiff world file (.tfw)
     */
    public double GetGeoProjFalseNorthing() {
        return _GeoProjFalseNorthing;
    }

    /**
     * Get a double value.
     *
     * @return the maximum west bounding longitude in image
     */
    public double GetMaxWestLng() {
        return _maxWestLng;
    }

    /**
     * Get a double value.
     *
     * @return the maximum east bounding longitude in image
     */
    public double GetMaxEastLng() {
        return _maxEastLng;
    }

    /**
     * Get a double value.
     *
     * @return the maximum north bounding latitude in image
     */
    public double GetMaxNorthLat() {
        return _maxNorthLat;
    }

    /**
     * Get a double value.
     *
     * @return the maximum south bounding latitude in image
     */
    public double GetMaxSouthLat() {
        return _maxSouthLat;
    }

    /**
     * Get a double value.
     *
     * @return if a sphere is used to model the Earth, this is its radius
     */
    public double GetRadius() {
        return _radius;
    }

    /**
     * Get a double value.
     *
     * @return if an ellipsoid is used to model the Earth,this is its major axis
     *         (a)
     */
    public double GetMajorAxis() {
        return _majorAxis;
    }

    /**
     * Get a double value.
     *
     * @return if an ellipsoid is used to model the Earth,this is its minor axis
     *         (b)
     */
    public double GetMinorAxis() {
        return _minorAxis;
    }

    public double GetEccentricitySqrd() {
        return _eccentricitySqrd;
    }

    public double GetInverseFlat() {
        return _inverseFlat;
    }

    public double GetScaleFactor() {
        return _scaleFactor;
    }

    public String GetDatum() {
        return _datum;
    }

    public double GetGeoScaleX() {
        return _geoScaleX;
    }

    public double GetGeoScaleY() {
        return _geoScaleY;
    }

    public double GetGeoScaleZ() {
        return _geoScaleZ;
    }

    public double GetGeoProjCenterLat() {
        return _GeoProjCenterLat;
    }

    public double GetGeoProjCenterLng() {
        return _GeoProjCenterLng;
    }

    /*public double GetTotalRowNum(){
      return _totalRowNum;
    }*/
    /*public double GetTotalColNum(){
      return _totalColNum;
    }*/
    public double GetRasterSpaceI() {
        return _rasterSpaceI;
    }

    public double GetRasterSpaceJ() {
        return _rasterSpaceJ;
    }

    public double GetRasterSpaceK() {
        return _rasterSpaceK;
    }

    public double GetModelSpaceX() {
        return _modelSpaceX;
    }

    public double GetModelSpaceY() {
        return _modelSpaceY;
    }

    public double GetModelSpaceZ() {
        return _modelSpaceZ;
    }

    public String GetGeoProjectedCSType() {
        return _GeoProjectedCSType;
    }

    public String GetEllipsoidName() {
        return _ellipsoidName;
    }

    public int GetModelType() {
        return _modelType;
    }

    public int GetUTMZone() {
        return _UTMzone;
    }

    public int GetOffsetX() {
        return _offsetX;
    }

    public int GetOffsetY() {
        return _offsetY;
    }


    public void SetColumnResolution(double a) {
        if (a == 0) {
            System.out.println("Resolution must be non-zero.");
            return;
        }
        _columnResolution = a;
        SetGeoScaleX(a);
    }

    public void SetRowResolution(double a) {
        if (a == 0) {
            System.out.println("Resolution must be non-zero.");
            return;
        }
        _rowResolution = a;
        _geoScaleY = Math.abs(a);
    }

    public void SetRotationX(double a) {
        _rotationX = a;
    }

    public void SetRotationY(double a) {
        _rotationY = a;
    }

    public void SetEastingInsertionValue(double a) {
        _eastingInsertionValue = a;
    }

    public void SetNorthingInsertionValue(double a) {
        _northingInsertionValue = a;
    }

    public void SetGeoProjFalseEasting(double a) {
        _GeoProjFalseEasting = a;
    }

    public void SetGeoProjFalseNorthing(double a) {
        _GeoProjFalseNorthing = a;
    }

    public void SetMaxWestLng(double a) {
        _maxWestLng = a;
    }

    public void SetMaxEastLng(double a) {
        _maxEastLng = a;
    }

    public void SetMaxNorthLat(double a) {
        _maxNorthLat = a;
    }

    public void SetMaxSouthLat(double a) {
        _maxSouthLat = a;
    }

    public void SetRadius(double a) {
        _radius = a;
    }

    public void SetMajorAxis(double a) {
        _majorAxis = a;
    }

    public void SetMinorAxis(double a) {
        _minorAxis = a;
    }

    public void SetEccentricitySqrd(double a) {
        _eccentricitySqrd = a;
    }

    public void SetInverseFlat(double a) {
        _inverseFlat = a;
    }

    public void SetScaleFactor(double a) {
        _scaleFactor = a;
    }

    public void SetEllipsoidName(String a) {
        if (a.equals("WGS84") || a.equals("NAD83")) {
            _ellipsoidName = a;
            _majorAxis = 6378137;
            _inverseFlat = 298.257223563;
        } else if (a.equals("WGS72")) {
            _ellipsoidName = a;
            _majorAxis = 6378135;
            _inverseFlat = 298.26;
        } else if (a.equals("GRS80")) {
            _ellipsoidName = a;
            _majorAxis = 6387137;
            _inverseFlat = 298.257222101;
        } else if (a.equals("Airy1830")) {
            _ellipsoidName = a;
            _majorAxis = 6377563.396;
            _inverseFlat = 299.3249646;
        } else if (a.equals("Sphere")) {
            _ellipsoidName = a;
        } else {
            System.out.println("Unknown datum.  Defaulting to WGS84");
            _majorAxis = 6378137;
            _inverseFlat = 298.257223563;
            _ellipsoidName = "WGS84";
        }

        return;
    }

    public void SetGeoScaleX(double a) {
        _geoScaleX = a;
    }

    public void SetGeoScaleY(double a) {
        _geoScaleY = a;
    }

    public void SetGeoScaleZ(double a) {
        _geoScaleZ = a;
    }

    public void SetGeoProjCenterLat(double a) {
        _GeoProjCenterLat = a;
    }

    public void SetGeoProjCenterLng(double a) {
        _GeoProjCenterLng = a;
    }

    /*public void SetTotalRowNum(int a){
          super.numrows = a;
      //_totalRowNum = a;
    }*/

    /*public void SetTotalColNum(double a){
      _totalColNum = a;
    }*/

    public void SetRasterSpaceI(double a) {
        _rasterSpaceI = a;
    }

    public void SetRasterSpaceJ(double a) {
        _rasterSpaceJ = a;
    }

    public void SetRasterSpaceK(double a) {
        _rasterSpaceK = a;
    }

    public void SetModelSpaceX(double a) {
        _modelSpaceX = a;
    }

    public void SetModelSpaceY(double a) {
        _modelSpaceY = a;
    }

    public void SetModelSpaceZ(double a) {
        _modelSpaceZ = a;
    }

    public void SetModelType(int a) {
        if (a == 0) {	//Lambert Equal Area
            _modelType = a;
            //add here
        } else if (a == 1) {	//NorthernHemisphere UTM
            _modelType = a;
            _GeoProjFalseEasting = 500000;
            _GeoProjFalseNorthing = 0;
            _scaleFactor = .9996;
            _GeoProjCenterLat = 0;
            if (_ellipsoidName.equals("empty"))
                SetEllipsoidName("WGS84"); //default value which will
            //hopefully be set somewhere else
        } else if (a == 2) {	//SouthernHemisphere UTM
            _modelType = a;
            _GeoProjFalseEasting = 500000;
            _GeoProjFalseNorthing = 10000000;
            _scaleFactor = .9996;
            _GeoProjCenterLat = 0;
        } else if (a == 3) {		//OSGB 1936
            _modelType = a;
            _GeoProjFalseEasting = 400000;
            _GeoProjFalseNorthing = -100000;
            //_scaleFactor = .9996012717;
            _scaleFactor = .9996013;
            _GeoProjCenterLat = 49;
            _GeoProjCenterLng = -2;
        } else if (a == 4) {
            _modelType = a; //DEM data
        } else if (a == 97) {
            _modelType = 1;
            _columnResolution = 28.5;
            _rowResolution = -28.5;
            _GeoProjFalseEasting = 500000;
            _GeoProjFalseNorthing = 0;
            _GeoProjCenterLat = 0.0;
            _GeoProjCenterLng = -87.0;
            _eastingInsertionValue = 102429.0;
            _northingInsertionValue = 4886182.5;
            _majorAxis = 6378137;
            _inverseFlat = 298.257223563;
            _scaleFactor = 0.9996;
            _geoScaleX = 28.5;
            _geoScaleY = 28.5;
            _rasterSpaceI = 0.0;
            _rasterSpaceJ = 0.0;
            _modelSpaceX = 102429.0;
            _modelSpaceY = 4886182.5;
            _ellipsoidName = "WGS84";

        } else if (a == 98) { //testing only!!!
            _modelType = a;
            _columnResolution = 1000;
            _rowResolution = -1000;
            _GeoProjFalseEasting = 0;
            _GeoProjFalseNorthing = 0;
            _GeoProjCenterLat = 45;
            _GeoProjCenterLng = -100;
            _eastingInsertionValue = -6086629.0;
            _northingInsertionValue = 4488761.0;
            _radius = 6370997.0;
            _geoScaleX = 1000;
            _geoScaleY = 1000;
            _ellipsoidName = "Sphere";
        } else if (a == 99) {    //testing only!!!
            _modelType = a;
            double lamda1 = -91.40656230816928;
            double phi1 = 42.82313923550979;
            double lamda2 = -87.25731382206416;
            double phi2 = 37.359365577981364;
            double x1 = 6786.0;
            double x2 = 7210.0;
            double y1 = 4694.0;
            double y2 = 5252.0;
            double A = (lamda1 - lamda2) / (x1 - x2);
            double B = lamda1 - A * x1;
            double C = (phi1 - phi2) / (y1 - y2);
            double D = phi2 - C * y2;
            _columnResolution = A;
            _rowResolution = C;
            System.out.println("col res is: " + A);
            System.out.println("row res is: " + C);
            _eastingInsertionValue = B;
            _northingInsertionValue = D;
        } else {
            System.out.println("Unknown model type.  Default is 1.");
            _modelType = 1;
            _GeoProjFalseEasting = 500000;
            _GeoProjFalseNorthing = 0;
            _scaleFactor = .9996;
            _GeoProjCenterLat = 0;
        }
        return;
    }

    public void SetUTMZone(int a) {
        /*if (_modelType == 1 || _modelType == 2) {
            _UTMzone = a;
            //Here, a negative value indicates West Longitude
            _GeoProjCenterLng = 6*a - 183;
        }
        else {
            System.out.println("UTMZone only valid for model types 1 and 2.");
            System.out.println("Zone not set.");
        }*/
        //For now, let caller use this at his own discretion
        _UTMzone = a;
        if (a != -1) {
            _GeoProjCenterLng = 6 * a - 183;
        }
    }

    public void SetGeoProjectedCSType(String a) {
        _GeoProjectedCSType = a;
    }

    public void SetOffsetX(int offset) {
        _offsetX = offset;
    }

    public void SetOffsetY(int offset) {
        _offsetY = offset;
    }

    public void SetDatum(String a) {
        _datum = a;
    }
    /*
      //All georeferencing is now done through 6 types of translation, handled
      //by six methods.  Each of the six translations has a method specific to
      //the model type.  The Translations are:
      // 1) UTM coordinates to Latitude/Longitude
      // 2) Latitude/Longtiude to UTM coordinates
      // 3) UTM coordinates to Column/Row pixel values
      // 4) Column/Row pixel values to UTM coordinates
      // 5) Column/Row pixel values to Latitude/Longitude
      // 6) Latitude/Longitude to Column/Row pixel values
      // Translations 5 and and 6 are handled by successive calls to translations
      // 4 then 1, and translations 2 then 3, respectively.

      //Translation 1 caller
      public Point2DDouble UTMNorthingEasting2LatLng(Point2DDouble pnt) {

        Point2DDouble ret = new Point2DDouble(2);

        switch (_modelType){
            case 0:
                ret = LambertUTMNorthingEasting2LatLng(pnt);
            default:
                ret = StandardUTMNorthingEasting2LatLng(pnt);
        }//end switch statement

        return ret;
      }

      //Translation 2 caller
      public Point2DDouble LatLng2UTMNorthingEasting(Point2DDouble pnt) {

        Point2DDouble ret = new Point2DDouble(2);

        switch(_modelType){
            case 0:
                ret = LambertLatLng2UTMNorthingEasting(pnt);
                break;
            default:
                ret = StandardLatLng2UTMNorthingEasting(pnt);
                break;
        }//end switch statement

        return ret;

      }

      //Translation 3 caller
      public Point2DDouble UTMNorthingEasting2ColumnRow(Point2DDouble pnt) {

        Point2DDouble ret = new Point2DDouble(2);

        switch (_modelType){
            case 0:
                ret = LambertUTMNorthingEasting2ColumnRow(pnt);
                break;
            default:
                ret = StandardUTMNorthingEasting2ColumnRow(pnt);
                break;
        }
        return ret;

      }


      //Translation 4 caller
      public Point2DDouble ColumnRow2UTMNorthingEasting(Point2DDouble pnt) {

        Point2DDouble ret = new Point2DDouble(2);
        switch(_modelType){
            case 0:
                ret = LambertColumnRow2UTMNorthingEasting(pnt);
                break;
            default:
                ret = StandardColumnRow2UTMNorthingEasting(pnt);
                break;
        }
        return ret;

      }

      //Translation 5 caller
      public Point2DDouble ColumnRow2LatLng(Point2DDouble pnt){

        Point2DDouble ret = new Point2DDouble(2);
        switch(_modelType){
            case 0:
                ret = LambertColumnRow2UTMNorthingEasting(pnt);
                ret = LambertUTMNorthingEasting2LatLng(ret);
                break;
            default:
                ret = StandardColumnRow2UTMNorthingEasting(pnt);
                ret = StandardUTMNorthingEasting2LatLng(ret);
                break;
        }
        return ret;

      }

      //Translation 6 caller
      public Point2DDouble LatLng2ColumnRow(Point2DDouble pnt){

        Point2DDouble ret = new Point2DDouble(2);
        switch(_modelType){
            case 0:
                ret = LambertLatLng2UTMNorthingEasting(pnt);
                ret = LambertUTMNorthingEasting2ColumnRow(ret);
                break;
            default:
                ret = StandardLatLng2UTMNorthingEasting(pnt);
                ret = StandardUTMNorthingEasting2ColumnRow(ret);
                break;
        }
        return ret;
      }

      //Reference site for calculation of latitude/longitude:
      //http://mathworld.wolfram.com/LambertAzimuthalEqual-AreaProjection.html

      //Translation 1 for type 0
      public Point2DDouble LambertUTMNorthingEasting2LatLng( Point2DDouble pnt) {

        double UTMnorthing = pnt.ptsDouble[0];
        double UTMeasting = pnt.ptsDouble[1];

        double y1 = UTMnorthing;
        double x1 = UTMeasting;

        y1 = y1/_radius;	//scale down to unit sphere, which is
        x1 = x1/_radius;	//divide values by radius of sphere to
                    //needed to apply the equations at the
                    //address above.

        double rho = Math.sqrt(x1*x1 + y1*y1);
        double c = 2*Math.asin(.5*rho);
        double frac = (y1*Math.sin(c)*Math.cos(toRadians(_GeoProjCenterLat)))/rho;

        double denom2 = rho*Math.cos(toRadians(_GeoProjCenterLat))*Math.cos(c) -
                y1*Math.sin(toRadians(_GeoProjCenterLat))*Math.sin(c);
        double frac2 = (x1*Math.sin(c))/denom2;
        double lngOut = _GeoProjCenterLng + toDegrees(Math.atan(frac2));

        double latOut = toDegrees(Math.asin(Math.cos(c)*Math.sin(
                toRadians(_GeoProjCenterLat)) + frac));
        Point2DDouble ret = new Point2DDouble(2);

        ret.ptsDouble[0] = latOut;
        ret.ptsDouble[1] = lngOut;

        return ret;
      }

      //Translation 1 for type 1
      //Reference site for calculation of latitude/longitude:
      //http://www.posc.org/Epicentre.2_2/DataModel/ExamplesofUsage/eu_cs34h.html
      public Point2DDouble StandardUTMNorthingEasting2LatLng( Point2DDouble pnt){

        double phi_0 = _GeoProjCenterLat;
        double lamda_0 = _GeoProjCenterLng;
        //REAL VALUE
        double N = pnt.ptsDouble[0];
        double FN = _GeoProjFalseNorthing;

        //REAL VALUE
        double E = pnt.ptsDouble[1];
        double FE = _GeoProjFalseEasting;

        //REAL VALUE
        double eSquare = getESquared();
        //double eSquare = .00669437999013;

        //REAL VALUE
        double a = _majorAxis;

        double eTickSquare = eSquare/(1 - eSquare);

        //REAL VALUE
        //double k_0 = .9996;	//ALWAYS THIS VALUE FOR UTM
        double k_0 = .9996013;

        double term1 = (1 - eSquare/4 - 3*Math.pow(eSquare,2)/64 -
                5*Math.pow(eSquare,3)/256)*toRadians(phi_0);
        double term2 = (3*eSquare/8 + 3*Math.pow(eSquare,2)/32 +
                45*Math.pow(eSquare,3)/1024)*Math.sin(2*toRadians(phi_0));
        double term3 = (15*Math.pow(eSquare,2)/256 +
                45*Math.pow(eSquare,3)/1024)*Math.sin(4*toRadians(phi_0));
        double term4 = (35*Math.pow(eSquare,3)/3072)*Math.sin(6*toRadians(phi_0));
        double LargeSum = term1 - term2 + term3 - term4;

        double M_0 = a*LargeSum;

        double M_1 = M_0 + (N - FN)/k_0;

        double mu_1 = M_1/(a*(1 - eSquare*.25 - 3*Math.pow(eSquare,2)/64 -
                5*Math.pow(eSquare,3)/256));

        double e_1 = ( 1 - Math.pow((1 - eSquare),.5))/( 1
                + Math.pow((1 - eSquare),.5));

        double phi_1 = mu_1 + (1.5*e_1 - 27*Math.pow(e_1,3)/32)*Math.sin(2*mu_1)+
                (21*e_1*e_1/16 - 55*Math.pow(e_1,4)/32)*Math.sin(4*mu_1)+
                (151*Math.pow(e_1,3)/96)*Math.sin(6*mu_1) +
                (1097*Math.pow(e_1,4)/512)*Math.sin(8*mu_1);

        double v_1 = a/Math.pow((1 - eSquare*Math.sin(phi_1)*
                Math.sin(phi_1)),.5);

        double D = (E-FE)/(v_1*k_0);

        double T_1 = Math.tan(phi_1)*Math.tan(phi_1);

        double C_1 = eTickSquare*Math.cos(phi_1)*Math.cos(phi_1);

        double rho_1 = (a*(1-eSquare))/
                Math.pow((1-eSquare*Math.pow(Math.sin(phi_1),2)),1.5);


        double Bigsum = .5*D*D -
            (5 + 3*T_1 + 10*C_1 - 4*C_1*C_1 - 9*eTickSquare)*Math.pow(D,4)/24
            +(61 + 90*T_1 + 298*C_1 + 45*T_1*T_1 - 252*eTickSquare -
             3*C_1*C_1)*Math.pow(D,6)/720;

        double Bigsum2 = D - (1 + 2*T_1 + C_1)*Math.pow(D,3)/6 +
                (5 - 2*C_1 + 28*T_1 - 3*C_1*C_1 + 8*Math.pow(eTickSquare,2)
                + 24*T_1*T_1)*Math.pow(D,5)/120;

        System.out.println("eSquare is "+eSquare);
        System.out.println("eTickSquare is "+eTickSquare);
        System.out.println("phi_0 in radians is "+toRadians(phi_0));
        System.out.println("phi_0 is "+phi_0);
        System.out.println("phi_1 is "+phi_1);
        System.out.println("v_1 is "+v_1);
        System.out.println("D is "+D);
        System.out.println("T_1 is "+T_1);
        System.out.println("M_1 is "+M_1);
        System.out.println("M_0 is "+M_0);
        System.out.println("rho_1 is "+rho_1);
        System.out.println("e_1 is "+e_1);
        System.out.println("mu_1 is "+mu_1);
        System.out.println("a is "+a);
        System.out.println("E is "+E);
        System.out.println("FE is "+FE);

        //FINAL VALUES
        double lamda = toRadians(lamda_0) + Bigsum2/Math.cos(phi_1);

        double phi = phi_1 - v_1*Math.tan(phi_1)*Bigsum/rho_1;

        Point2DDouble ret = new Point2DDouble(2);
        ret.ptsDouble[0] = toDegrees(phi);
        ret.ptsDouble[1] = toDegrees(lamda);

        return ret;
      }
    */
    /*public double GetLambertLatitude(int inPixX, int inPixY){
      double x1 = ((inPixX*Math.abs(_columnResolution))+_eastingInsertionValue)/_radius;
      double y1 = ((inPixY*Math.abs(_rowResolution))-_northingInsertionValue)/(-_radius);

      double rho = Math.sqrt(x1*x1 + y1*y1);
      double c = 2*Math.asin(.5*rho);
      double frac = (y1*Math.sin(c)*Math.cos(toRadians(_GeoProjCenterLat)))/rho;

      double latOut = toDegrees(Math.asin(Math.cos(c)*Math.sin(
              toRadians(_GeoProjCenterLat)) + frac));
      return latOut;
    }*/
    /*

      //Translation 2 for type 0
      public Point2DDouble LambertLatLng2UTMNorthingEasting( Point2DDouble pnt){

        double inLat = pnt.ptsDouble[0];
        double inLng = pnt.ptsDouble[1];

        double denom = 1 + Math.sin(toRadians(_GeoProjCenterLat))*
                Math.sin(toRadians(inLat)) +
                Math.cos(toRadians(_GeoProjCenterLat))*
                Math.cos(toRadians(inLat))*
                Math.cos(toRadians(inLng-_GeoProjCenterLng));
        double k = Math.sqrt(2/denom);

        double x = k*Math.cos(toRadians(inLat))*Math.sin(toRadians(inLng-_GeoProjCenterLng));

        double y = k*(Math.cos(toRadians(_GeoProjCenterLat))*
            Math.sin(toRadians(inLat)) -
            Math.sin(toRadians(_GeoProjCenterLat))*Math.cos(toRadians(inLat))*
            Math.cos(toRadians(inLng - _GeoProjCenterLng)));

        double retEasting = x*_radius;
        double retNorthing = y*_radius;

        //int column = (int) Math.round(((x*_radius)-_eastingInsertionValue)/_columnResolution);
        //int row = (int) Math.round((-(y*_radius)+_northingInsertionValue)/_rowResolution);

        Point2DDouble ret = new Point2DDouble(2);
        ret.ptsDouble[0] = retNorthing;
        ret.ptsDouble[1] = retEasting;
        return ret;

      }

      //Translation 2 for type 1
      public Point2DDouble StandardLatLng2UTMNorthingEasting( Point2DDouble pnt){

        double phi = pnt.ptsDouble[0];
        double phi_0 = _GeoProjCenterLat;
        double a = _majorAxis;
        double eSquare = getESquared();

        double FN = _GeoProjFalseNorthing;
        double FE = _GeoProjFalseEasting;

        double lamda = pnt.ptsDouble[1];
        double lamda_0 = _GeoProjCenterLng;

        double k_0 = .9996013;
        double v = a/Math.pow((1 - eSquare*Math.sin(toRadians(phi))*
                Math.sin(toRadians(phi))),.5);
        double A = (toRadians(lamda) - toRadians(lamda_0))*Math.cos(toRadians(phi));
        double eTickSquare = eSquare/(1 - eSquare);

        double T = Math.tan(toRadians(phi))*Math.tan(toRadians(phi));

        double C = eSquare/(1-eSquare)*Math.cos(toRadians(phi))*Math.cos(toRadians(phi));

        double term1 = (1 - eSquare/4 - 3*Math.pow(eSquare,2)/64 -
                5*Math.pow(eSquare,3)/256)*toRadians(phi);

        double term1_0 = (1 - eSquare/4 - 3*Math.pow(eSquare,2)/64 -
                5*Math.pow(eSquare,3)/256)*toRadians(phi_0);

        double term2 = (3*eSquare/8 + 3*Math.pow(eSquare,2)/32 +
                45*Math.pow(eSquare,3)/1024)*Math.sin(2*toRadians(phi));
        double term2_0 = (3*eSquare/8 + 3*Math.pow(eSquare,2)/32 +
                45*Math.pow(eSquare,3)/1024)*Math.sin(2*toRadians(phi_0));

        double term3 = (15*Math.pow(eSquare,2)/256 +
                45*Math.pow(eSquare,3)/1024)*Math.sin(4*toRadians(phi));
        double term3_0 = (15*Math.pow(eSquare,2)/256 +
                45*Math.pow(eSquare,3)/1024)*Math.sin(4*toRadians(phi_0));

        double term4 = (35*Math.pow(eSquare,3)/3072)*Math.sin(6*toRadians(phi));
        double term4_0 = (35*Math.pow(eSquare,3)/3072)*Math.sin(6*toRadians(phi_0));


        double LargeSum = term1 - term2 + term3 - term4;
        double LargeSum_0 = term1_0 - term2_0 + term3_0 - term4_0;

        double M_0 = a*LargeSum_0;
        double M = a*LargeSum;

        double sum1 = 5 - T + 9*C + 4*C*C;
        double sum2 = 61 - 58*T + T*T + 600*C - 330*eTickSquare;

        double E = FE + k_0*v*(A + term1*Math.pow(A,3)/6 + term2*Math.pow(A,5)/120);
        double N = FN + k_0*(M - M_0 + v*Math.tan(phi)*(A*A/2 + sum1*Math.pow(A,4)/24+
                sum2*Math.pow(A,6)/720));

        System.out.println("A is "+A);
        System.out.println("T is "+T);
        System.out.println("C is "+C);
        System.out.println("M is "+M);
        System.out.println("lamda is "+lamda);
        System.out.println("phi is "+phi);
        System.out.println("v is "+v);



        Point2DDouble ret = new Point2DDouble(2);
        ret.ptsDouble[0] = N;
        ret.ptsDouble[1] = E;

        return ret;
      }

      //Translation 3 for type 0
      public Point2DDouble LambertUTMNorthingEasting2ColumnRow( Point2DDouble pnt){



        double northing = pnt.ptsDouble[0];
        double easting = pnt.ptsDouble[1];

        double column = Math.round(((easting)-_eastingInsertionValue)/Math.abs(_columnResolution));
        double row = Math.round((-(northing)+_northingInsertionValue)/Math.abs(_rowResolution));

        Point2DDouble ret = new Point2DDouble(2);

        ret.ptsDouble[0] = column;
        ret.ptsDouble[1] = row;
        return ret;
      }

      //Translation 3 for type 1
      public Point2DDouble StandardUTMNorthingEasting2ColumnRow(Point2DDouble pnt){

        System.out.println("Standard UTMNE2ColRow not implemented yet.");
        int col = -1;
        int row = -1;

        Point2DDouble ret = new Point2DDouble(2);
        ret.ptsDouble[0] = col;
        ret.ptsDouble[1] = col;
        return ret;
      }

      //Translation 4 for type 0
      public Point2DDouble LambertColumnRow2UTMNorthingEasting( Point2DDouble pnt ){

        double inPixCol = pnt.ptsDouble[0];
        double inPixRow = pnt.ptsDouble[1];

        double UTMnorthing = (-(inPixRow*Math.abs(_rowResolution))+_northingInsertionValue);
        double UTMeasting = ((inPixCol*Math.abs(_columnResolution))+_eastingInsertionValue);

        Point2DDouble p = new Point2DDouble(2);
        p.ptsDouble[0] = UTMnorthing;
        p.ptsDouble[1] = UTMeasting;
        return p;

      }

      //Translation 4 for type 1
      public Point2DDouble StandardColumnRow2UTMNorthingEasting( Point2DDouble pnt) {

        double UTMeasting = 700411.209;
        double UTMnorthing = 4555765.966;

        Point2DDouble ret = new Point2DDouble(2);
        ret.ptsDouble[0] = UTMnorthing;
        ret.ptsDouble[0] = UTMeasting;

        return ret;
      }

      //////////////////////////////////////////////////////////////////
      //conversion utilities
      //////////////////////////////////////////////////////////////////
      //degrees to decimal and back
      //////////////////////////////////////////////////////////////////
      public double DegMinSecToDecim(double deg,double min,double sec)  {
        int sign;
        if(deg>=0)
          sign =1;
        else
          sign =-1;
        return( (Math.abs(deg) + Math.abs(min)/60.0 + Math.abs(sec)/3600.0 )*sign);
      }
      public Point3DDouble DecimToDegMinSec(double decim)  {
        double frac,temp;
        int sign;
        if(decim>=0)
          sign =1;
        else
          sign =-1;
        Point3DDouble res = new Point3DDouble(3);
        // res.ptsDouble[0] = degrees, res.ptsDouble[1] = minutes, res.ptsDouble[2]= seconds
        res.ptsDouble[0] = res.ptsDouble[1] = res.ptsDouble[2] = -1.0;

        // degrees
        //frac = modf(fabs(decim),res.ptsDouble[0]);
        res.ptsDouble[0] = Math.floor(Math.abs(decim));
        frac = Math.abs(decim) - res.ptsDouble[0];
        res.ptsDouble[0] *= sign;
        temp = frac*60.0;

        // minutes
        //frac = modf(temp,res.ptsDouble[1]);
        res.ptsDouble[1] = Math.floor(Math.abs(temp));
        frac = Math.abs(temp) - res.ptsDouble[1];
        res.ptsDouble[1] *=sign;

        // seconds
        res.ptsDouble[2] = frac*60.0*sign;

        return res;
      }
      ///////////////////////////////////////////////////////////////////////////////
      // conversion of pixel coordinates and latitude, longitude pairs.
      public Point2DDouble CoordinatesToRowCol(Point2DDouble latlong ) {
        return CoordinatesToRowCol(latlong.ptsDouble[0],latlong.ptsDouble[1]);
      }
      public Point2DDouble CoordinatesToRowCol( double coordinateLat, double coordinateLon) {
        // Note : LON = X = 0 & LAT = Y = 1
        Point2DDouble res = new Point2DDouble(2);
        // res.ptsDouble[0] = row, res.ptsDouble[1] = column

        // to be verified by T.J. Peter
        res.ptsDouble[0] = (coordinateLat - _maxWestLng) / _rowResolution;//_interval[LAT];// row
        res.ptsDouble[1]  = (coordinateLon - _maxSouthLat) / _columnResolution;//_interval[LON];// column
        return res;
      }

      public Point2DDouble RowColToCoordinates(Point2DDouble rowcol)  {
         return RowColToCoordinates(rowcol.ptsDouble[0],rowcol.ptsDouble[1]);
      }
      public Point2DDouble RowColToCoordinates(double row, double col)  {
        // Note : LON = X = 0 & LAT = Y = 1
        Point2DDouble res = new Point2DDouble(2);
        // res.ptsDouble[0] = latitude, res.ptsDouble[1] = longitude

        // to be verified by T.J. Peter
        res.ptsDouble[0]  =  row * _columnResolution + _maxWestLng;// latitude
        res.ptsDouble[1]  =  col *  _rowResolution + _maxSouthLat; // longitude
        return res;
      }

      /////////////////////////////////////////////////////////
      // additional conversion
      public static double toRadians(double a) {

        return ((Math.PI*a)/180);
      }

      public static double toDegrees(double a) {
        return ((180*a)/Math.PI);
      }

      public static double atanh(double a) {

        return (.5*Math.log((1+a)/(1-a)));
      }

      public double getESquared(){
        double ecc = -1;

        if (_eccentricitySqrd == -1) {
            if ( (_majorAxis != -1) && (_minorAxis != -1) ){
            double flattening = (_majorAxis-_minorAxis)/_majorAxis;
            ecc = 2*flattening - flattening*flattening;
            _eccentricitySqrd = ecc;
            }
            else { if ( _inverseFlat != -1 ) {
                    ecc = (1/_inverseFlat)*2 -
                        Math.pow((1/_inverseFlat),2);
                    _eccentricitySqrd = ecc;
                }
                else {
                    System.out.println("Insufficient information to find eccentricity");
                }
            }
        }
        else {
            ecc = _eccentricitySqrd;
        }
        return ecc;

      }*/


    // public void CopyGeoImageObject(GeoImageObject fromObj){
    public void CopyGeoInfo(GeoInformation fromObj) {
        // Copies the fields from fromObj to the fields of 'this' GeoImageObject
        this.SetColumnResolution(fromObj.GetColumnResolution());
        this.SetRowResolution(fromObj.GetRowResolution());
        this.SetRotationX(fromObj.GetRotationX());
        this.SetRotationY(fromObj.GetRotationY());
        this.SetEastingInsertionValue(fromObj.GetEastingInsertionValue());
        this.SetNorthingInsertionValue(fromObj.GetNorthingInsertionValue());
        this.SetGeoProjFalseEasting(fromObj.GetGeoProjFalseEasting());
        this.SetGeoProjFalseNorthing(fromObj.GetGeoProjFalseNorthing());
        this.SetMaxWestLng(fromObj.GetMaxWestLng());
        this.SetMaxEastLng(fromObj.GetMaxEastLng());
        this.SetMaxNorthLat(fromObj.GetMaxNorthLat());
        this.SetMaxSouthLat(fromObj.GetMaxSouthLat());
        this.SetRadius(fromObj.GetRadius());
        this.SetMajorAxis(fromObj.GetMajorAxis());
        this.SetMinorAxis(fromObj.GetMinorAxis());
        this.SetEccentricitySqrd(fromObj.GetEccentricitySqrd());
        this.SetInverseFlat(fromObj.GetInverseFlat());
        this.SetScaleFactor(fromObj.GetScaleFactor());
        this.SetDatum(fromObj.GetDatum());
        this.SetGeoScaleX(fromObj.GetGeoScaleX());
        this.SetGeoScaleY(fromObj.GetGeoScaleY());
        this.SetGeoScaleZ(fromObj.GetGeoScaleZ());
        this.SetGeoProjCenterLat(fromObj.GetGeoProjCenterLat());
        this.SetGeoProjCenterLng(fromObj.GetGeoProjCenterLng());
        //this.SetTotalRowNum(fromObj.GetTotalRowNum());
        //this.SetTotalColNum(fromObj.GetTotalColNum());
        this.SetGeoProjectedCSType(fromObj.GetGeoProjectedCSType());
        this.SetRasterSpaceI(fromObj.GetRasterSpaceI());
        this.SetRasterSpaceJ(fromObj.GetRasterSpaceJ());
        this.SetRasterSpaceK(fromObj.GetRasterSpaceK());
        this.SetModelSpaceX(fromObj.GetModelSpaceX());
        this.SetModelSpaceY(fromObj.GetModelSpaceY());
        this.SetModelSpaceZ(fromObj.GetModelSpaceZ());
        this.SetModelType(fromObj.GetModelType());
        this.SetUTMZone(fromObj.GetUTMZone());
        this.SetEllipsoidName(fromObj.GetEllipsoidName());
        this.SetOffsetX(fromObj.GetOffsetX());
        this.SetOffsetY(fromObj.GetOffsetY());
    }


    //////////////////////////////////////////////////////////////
    // takes the geospecific information and converts it to string
    public String toString() {
        String reString = "";
        if (_columnResolution != -1) {
            reString += "columnResolution=" + _columnResolution + "\n";
        }
        if (_rowResolution != -1) {
            reString += "rowResolution=" + _rowResolution + "\n";
        }
        if (_rotationX != -1) {
            reString += "rotationX=" + _rotationX + "\n";
        }
        if (_rotationY != -1) {
            reString += "rotationY=" + _rotationY + "\n";
        }
        if (_eastingInsertionValue != -1) {
            reString += "eastingInsertionValue=" + _eastingInsertionValue + "\n";
        }
        if (_northingInsertionValue != -1) {
            reString += "northingInsertionValue=" + _northingInsertionValue + "\n";
        }
        if (_GeoProjFalseEasting != -1) {
            reString += "GeoProjFalseEasting=" + _GeoProjFalseEasting + "\n";
        }
        if (_GeoProjFalseNorthing != -1) {
            reString += "GeoProjFalseNorthing=" + _GeoProjFalseNorthing + "\n";
        }
        if (_maxWestLng != -1) {
            reString += "maxWestLng=" + _maxWestLng + "\n";
        }
        if (_maxEastLng != -1) {
            reString += "maxEastLng=" + _maxEastLng + "\n";
        }
        if (_maxNorthLat != -1) {
            reString += "maxNorthLat=" + _maxNorthLat + "\n";
        }
        if (_maxSouthLat != -1) {
            reString += "maxSouthLat=" + _maxSouthLat + "\n";
        }
        if (_radius != -1) {
            reString += "radius=" + _radius + "\n";
        }
        if (_majorAxis != -1) {
            reString += "majorAxis=" + _majorAxis + "\n";
        }
        if (_minorAxis != -1) {
            reString += "minorAxis=" + _minorAxis + "\n";
        }
        if (_eccentricitySqrd != -1) {
            reString += "eccentricitySqrd=" + _eccentricitySqrd + "\n";
        }
        if (_inverseFlat != -1) {
            reString += "inverseFlat=" + _inverseFlat + "\n";
        }
        if (_scaleFactor != -1) {
            reString += "scaleFactor=" + _scaleFactor + "\n";
        }
        if (!_datum.equals("empty")) {
            reString += "datum=" + _datum + "\n";
        }
        if (_geoScaleX != -1) {
            reString += "geoScaleX=" + _geoScaleX + "\n";
        }
        if (_geoScaleY != -1) {
            reString += "geoScaleY=" + _geoScaleY + "\n";
        }
        if (_geoScaleZ != -1) {
            reString += "geoScaleZ=" + _geoScaleZ + "\n";
        }
        if (_GeoProjCenterLat != -1) {
            reString += "GeoProjCenterLat=" + _GeoProjCenterLat + "\n";
        }
        if (_GeoProjCenterLng != -1) {
            reString += "GeoProjCenterLng=" + _GeoProjCenterLng + "\n";
        }
        //if(numrows != -1) {
        //  reString += "numrows="+numrows+"\n";
        //}
        //if(numcols != -1) {
        //  reString += "numcols="+numcols+"\n";
        //}
        //reString += "size= "+size+"\n";
        if (_rasterSpaceI != -1) {
            reString += "rasterSpaceI=" + _rasterSpaceI + "\n";
        }
        if (_rasterSpaceJ != -1) {
            reString += "rasterSpaceJ=" + _rasterSpaceJ + "\n";
        }
        if (_rasterSpaceK != -1) {
            reString += "rasterSpaceK=" + _rasterSpaceK + "\n";
        }
        if (_modelSpaceX != -1) {
            reString += "modelSpaceX=" + _modelSpaceX + "\n";
        }
        if (_modelSpaceY != -1) {
            reString += "modelSpaceY=" + _modelSpaceY + "\n";
        }
        if (_modelSpaceZ != -1) {
            reString += "modelSpaceZ=" + _modelSpaceZ + "\n";
        }
        if (!_GeoProjectedCSType.equals("none")) {
            reString += "GeoProjectedCSType=" + _GeoProjectedCSType + "\n";
        }
        if (!_ellipsoidName.equals("none")) {
            reString += "ellipsoidName=" + _ellipsoidName + "\n";
        }
        if (_modelType != -1) {
            reString += "modelType=" + _modelType + "\n";
        }
        if (_UTMzone != -1) {
            reString += "UTMzone=" + _UTMzone + "\n";
        }
        return reString;
    }

    /*
    public void subAreaPrep(int startPixelCol, int startPixelRow) {

    //Here, startPixelCol is the column coordinate of the upper left corner of the
    //sub-area image (assuming the column coordinate of the upper left corner
    //of the original image is 0.  startPixelRow is the same for the vertical
    //direction.
    //Easting and Northing values changed for subArea calculations.
      _eastingInsertionValue = _eastingInsertionValue +
                  _columnResolution*startPixelCol;

      _northingInsertionValue = _northingInsertionValue +
                  _rowResolution*startPixelRow;
    }


    public void subSamplePrep(int newSampRatio) {
    //We maintain proper georeferencing by scaling the spatial resolution
    //values by the sample ratio.  If every fifth pixel is chosen,
    //the samp ratio is assumed to be 5.
      _columnResolution = _columnResolution*newSampRatio;
      _rowResolution = _rowResolution*newSampRatio;

    }
  */
}
