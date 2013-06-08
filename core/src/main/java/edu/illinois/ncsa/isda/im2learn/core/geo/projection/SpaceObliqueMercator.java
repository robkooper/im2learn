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

//import ncsa.im2learn.core.geo.*;

/**
 *
 * @author Peter Bajcsy
 */
public class SpaceObliqueMercator extends ModelProjection {

	private double _radius_maj;		/* (I) Radius major axis of the earth (sphere)     */
	private double _radius_min;		/* (I) Radius minor axis of the earth (sphere)     */
	private long _satnum;		/* Landsat satellite number (1,2,3,4,5) */
	private long _path;			/* Landsat path number 					*/
	private double _alf_in;    /* satellite angular inclination ? TODO */ 
	private double _lon;       /* satelite longitude orbit ? TODO      */ 
	private double _falseEast;	/* x offset in meters                   */
	private double _falseNorth;	/* y offset in meters           		*/
	private double _time; 
	private long _start1; 
	private long _flag; 
	//note: the following instance variables are only used by PRJWriter and ProjectionLoaderPRJ
	private String _geographicCoordinateSystem;
	private String _projectedCoordinateSystem;
	private String _datum;
	private String _primeMeridian;
	private double _primeMeridianValue;
	private String _angularUnit;
	private double _angularUnitValue;
	private String _linearUnit;
	private double _linearUnitValue;
	private double _centralMeridian;



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

	public double get_centralMeridian() {
		return _centralMeridian;
	}

	public void set_centralMeridian(double meridian) {
		_centralMeridian = meridian;
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

	public SpaceObliqueMercator()
	{
		setType(Projection.SPACE_OBLIQUE_MERCATOR);
	}

	public SpaceObliqueMercator(double radius_maj, double radius_min, long satnum,long path, double alf_in, double lon, 
			double falseEast, double falseNorth, double time, long start1, long flag)
	{
		setType(Projection.SPACE_OBLIQUE_MERCATOR);

		_radius_maj = radius_maj;
		_radius_min = radius_min;
		_satnum = satnum;
		_path = path;
		_alf_in = alf_in;
		_lon = lon;
		_falseEast = falseEast;
		_falseNorth = falseNorth;
		_time = time;
		_start1 = start1;
		_flag = flag;

	}

	public void setRadiusMajor(double radius)
	{
		_radius_maj = radius;
	}

	public void setRadiusMinor(double radius)
	{
		_radius_min = radius;
	}

	public void setSatelliteNumber(long val)
	{
		_satnum = val;
	}

	public void setSatelitePathNumber(long val)
	{
		_path = val;
	}

	public void setSateliteAlphaIn(double val)
	{
		_alf_in = val;
	}

	public void setSateliteLongitude(double val)
	{
		_lon = val;
	}

	public void setFalseEast(double falseEast)
	{
		_falseEast = falseEast;
	}

	public void setFalseNorth(double falseNorth)
	{
		_falseNorth = falseNorth;
	}

	public void setSateliteTime(double val)
	{
		_time = val;
	}

	public void setSateliteStart(long val)
	{
		_start1 = val;
	}

	public void setFlag(long val)
	{
		_flag = val;
	}

	//////////////////////////////////////////////////
	public double getRadiusMajor()
	{
		return _radius_maj;
	}

	public double getRadiusMinor()
	{
		return _radius_min;
	}

	public long getSatelliteNumber()
	{
		return _satnum;
	}

	public long getSatelitePathNumber()
	{
		return _path;
	}

	public double getSateliteAlphaIn()
	{
		return _alf_in;
	}

	public double getSateliteLongitude()
	{
		return _lon;
	}

	public double getFalseEast()
	{
		return _falseEast;
	}

	public double getFalseNorth()
	{
		return _falseNorth;
	}

	public double getSateliteTime()
	{
		return _time;
	}

	public long getSateliteStart()
	{
		return _start1;
	}

	public long getFlag()
	{
		return _flag;
	}

	/**
	 * this goes from meters to lat lng
	 * @param pnt
	 * @return
	 */
	public double[] modelToEarth(double[] pnt) {

		double[] ret = new double[2];

		//MRTInterface.somint(fwd, r_major, r_minor, satnum, path, alf_in, lon, false_east, false_north, time, start1, flag);
		
		MRTInterface.somint(MRTInterface.inverse, _radius_maj, _radius_min, _satnum,_path, _alf_in, _lon, 
				_falseEast, _falseNorth, _time, _start1, _flag);

		//MRTInterface.molwint(MRTInterface.inverse, _radius,StrictMath.toRadians(_centerLong), _falseEast, _falseNorth);
		MRTInterface.coord2geo(getType(), pnt[0], pnt[1], ret); 

		double[] latlng = new double[2];
		
		latlng[1] = StrictMath.toDegrees(ret[0]);
		latlng[0] = StrictMath.toDegrees(ret[1]);

		return latlng;
	}

	/**
	 * This goes from lat/lng to meters
	 * @param pnt
	 * @return
	 */
	public double[] earthToModel(double[] pnt) {

		double[] ret = new double[2];

		MRTInterface.somint(MRTInterface.forward, _radius_maj, _radius_min, _satnum,_path, _alf_in, _lon, 
				_falseEast, _falseNorth, _time, _start1, _flag);

		//MRTInterface.molwint(MRTInterface.forward, _radius, StrictMath.toRadians(_centerLong), _falseEast, _falseNorth);
		MRTInterface.geo2coord(getType(), StrictMath.toRadians(pnt[1]), StrictMath.toRadians(pnt[0]), ret); 

		return ret;
	}	

	public static void main(String[] args) {
		/*
	    SpaceObliqueMercator model = new SpaceObliqueMercator(6370997.0, -90, 0, 0);
        double[] lonlat = new double[] {-75, -50};
        double[] result;
        result = model.earthToModel(lonlat);
        System.out.println("earthToModel Test");
        System.out.println(result[0]);
        System.out.println(result[1]);

        double[] xy = new double[] { 1139672.54, -5866896.90 };
        result = model.modelToEarth(xy);
        System.out.println("earthToModel Test");
        System.out.println(result[0]);
        System.out.println(result[1]);  
		 */
	}		

  public String getProjectionParametersAsString() {
    String parametersAsString = new String();

    parametersAsString += (this.getType() + " = " + this.getClass() + "\n");
    parametersAsString += (_radius_maj + " = major radius\n");
    parametersAsString += (_radius_min + " = minor radius\n");
    parametersAsString += (_satnum + " = Landsat satellite number (1,2,3,4,5)\n");
    parametersAsString += (_path + " = Landsat path number\n");
    parametersAsString += (_alf_in + " = satellite angular inclination ?\n");
    parametersAsString += (_lon + " = satellite longitude orbit ?\n");
    parametersAsString += (_falseEast + " = false easting\n");
    parametersAsString += (_falseNorth + " = false northing\n");
    parametersAsString += (_time + " = time\n");
    parametersAsString += (_start1 + " = start1\n");
    parametersAsString += (_flag + " = flag\n");

//    private long _satnum;   /* Landsat satellite number (1,2,3,4,5) */
//    private long _path;     /* Landsat path number          */
//    private double _alf_in;    /* satellite angular inclination ? TODO */ 
//    private double _lon;       /* satelite longitude orbit ? TODO      */ 

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
    _radius_maj = Double.parseDouble(valueAsString);

    indexOfNewline    = paramString.indexOf(magicNewline,indexOfDelimiter);
    indexOfDelimiter  = paramString.indexOf(magicString,indexOfNewline);
    valueAsString     = paramString.substring(indexOfNewline + 1,indexOfDelimiter);
    _radius_min = Double.parseDouble(valueAsString);

    indexOfNewline    = paramString.indexOf(magicNewline,indexOfDelimiter);
    indexOfDelimiter  = paramString.indexOf(magicString,indexOfNewline);
    valueAsString     = paramString.substring(indexOfNewline + 1,indexOfDelimiter);
    _satnum = Long.parseLong(valueAsString);

    indexOfNewline    = paramString.indexOf(magicNewline,indexOfDelimiter);
    indexOfDelimiter  = paramString.indexOf(magicString,indexOfNewline);
    valueAsString     = paramString.substring(indexOfNewline + 1,indexOfDelimiter);
    _path = Long.parseLong(valueAsString);

    indexOfNewline    = paramString.indexOf(magicNewline,indexOfDelimiter);
    indexOfDelimiter  = paramString.indexOf(magicString,indexOfNewline);
    valueAsString     = paramString.substring(indexOfNewline + 1,indexOfDelimiter);
    _alf_in = Double.parseDouble(valueAsString);
  
    indexOfNewline    = paramString.indexOf(magicNewline,indexOfDelimiter);
    indexOfDelimiter  = paramString.indexOf(magicString,indexOfNewline);
    valueAsString     = paramString.substring(indexOfNewline + 1,indexOfDelimiter);
    _lon = Double.parseDouble(valueAsString);
    
    indexOfNewline    = paramString.indexOf(magicNewline,indexOfDelimiter);
    indexOfDelimiter  = paramString.indexOf(magicString,indexOfNewline);
    valueAsString     = paramString.substring(indexOfNewline + 1,indexOfDelimiter);
    _falseEast = Double.parseDouble(valueAsString);

    indexOfNewline    = paramString.indexOf(magicNewline,indexOfDelimiter);
    indexOfDelimiter  = paramString.indexOf(magicString,indexOfNewline);
    valueAsString     = paramString.substring(indexOfNewline + 1,indexOfDelimiter);
    _falseNorth = Double.parseDouble(valueAsString);

    indexOfNewline    = paramString.indexOf(magicNewline,indexOfDelimiter);
    indexOfDelimiter  = paramString.indexOf(magicString,indexOfNewline);
    valueAsString     = paramString.substring(indexOfNewline + 1,indexOfDelimiter);
    _time = Double.parseDouble(valueAsString);

    indexOfNewline    = paramString.indexOf(magicNewline,indexOfDelimiter);
    indexOfDelimiter  = paramString.indexOf(magicString,indexOfNewline);
    valueAsString     = paramString.substring(indexOfNewline + 1,indexOfDelimiter);
    _start1 = Long.parseLong(valueAsString);

    indexOfNewline    = paramString.indexOf(magicNewline,indexOfDelimiter);
    indexOfDelimiter  = paramString.indexOf(magicString,indexOfNewline);
    valueAsString     = paramString.substring(indexOfNewline + 1,indexOfDelimiter);
    _flag = Long.parseLong(valueAsString);
  }


}
