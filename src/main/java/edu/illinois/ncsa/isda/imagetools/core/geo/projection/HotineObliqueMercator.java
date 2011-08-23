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
package edu.illinois.ncsa.isda.imagetools.core.geo.projection;

//import ncsa.im2learn.core.geo.*;

/**
 *
 * @author Peter Bajcsy
 */
public class HotineObliqueMercator extends ModelProjection {

	private double _radius_maj;		/* (I) Radius major axis of the earth (sphere)     */
	private double _radius_min;		/* (I) Radius minor axis of the earth (sphere)     */
	private double _scale_fact;	/* scale factor             */
	private double _azimuth;		/* azimuth east of north        */
	private double _longOrig;	/* longitude of origin          */
	private double _latOrig;	/* center latitude              */
	private double _falseEast;	/* x offset in meters                   */
	private double _falseNorth;	/* y offset in meters           		*/

	private double _lon1;		/* fist point to define central line    */
	private double _lat1;		/* fist point to define central line    */
	private double _lon2;		/* second point to define central line  */
	private double _lat2;		/* second point to define central line  */
	private long _mode;			/* which format type A or B     */
	
	//note: the following instance variables are only used by PRJWriter and ProjectionLoaderPRJ
	private String _projectedCoordinateSystem;
	private String _geographicCoordinateSystem;
	private String _datum;
	private String _primeMeridian;
	private double _primeMeridianValue;
	private String _angularUnit;
	private double _angularUnitValue;
	private String _linearUnit;
	private double _linearUnitValue;
	private double _centralMeridian;

	
	public double get_centralMeridian() {
		return _centralMeridian;
	}

	public void set_centralMeridian(double meridian) {
		_centralMeridian = meridian;
	}

	public double getFalseNorth() {
		return _falseNorth;
	}

	public void setFalseNorth(double north) {
		_falseNorth = north;
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

	public HotineObliqueMercator()
	{
		setType(Projection.OBLIQUE_MERCATOR);
	}

	public HotineObliqueMercator(double radius_maj, double radius_min, double scale_fact,
			double azimuth, double longOrig, double latOrig, double falseEast, double falseNorth,
			double lon1, double lat1, double lon2, double lat2, long mode)
	{
		setType(Projection.OBLIQUE_MERCATOR);

		_radius_maj = radius_maj;
		_radius_min = radius_min;
		_scale_fact = scale_fact;
		_azimuth = azimuth;
		_longOrig = longOrig;
		_latOrig = latOrig;
		_falseEast = falseEast;
		_falseNorth = falseNorth;
		_lon1 = lon1;
		_lat1 = lat1;
		_lon2 = lon2;
		_lat2 = lat2;
		_mode = mode;
	}

	public void setRadiusMajor(double radius)
	{
		_radius_maj = radius;
	}

	public void setRadiusMinor(double radius)
	{
		_radius_min = radius;
	}

	public void setScaleFact(double scale)
	{
		_scale_fact = scale;
	}

	public void setAzimuth(double val)
	{
		_azimuth = val;
	}

	public void setLongOrig(double val)
	{
		_longOrig = val;
	}

	public void setLatOrig(double val)
	{
		_latOrig = val;
	}

	public void setFalseEast(double falseEast)
	{
		_falseEast = falseEast;
	}


	public void setFirstPointCentralLine(double longitude, double latitude)
	{
		_lon1 = longitude;
		_lat1 = latitude;
	}

	public void setSecondPointCentralLine(double longitude, double latitude)
	{
		_lon2 = longitude;
		_lat2 = latitude;
	}

	public double getRadiusMajor()
	{
		return _radius_maj;
	}

	public double getRadiusMinor()
	{
		return _radius_min;
	}

	public double getAzimuth()
	{
		return _azimuth;
	}
	
	public double getLongOrig()
	{
		return _longOrig;
	}

	public double getLatOrig()
	{
		return _latOrig;
	}

	public double getFalseEast()
	{
		return _falseEast;
	}

	public double getFirstPointCentralLineLong()
	{
		return _lon1;
	}

	public double getFirstPointCentralLineLat()
	{
		return _lat1;
	}

	public double getSecondPointCentralLineLong()
	{
		return _lon2;
	}

	public double getSecondPointCentralLineLat()
	{
		return _lat2;
	}

	/**
	 * this goes from meters to lat lng
	 * @param pnt
	 * @return
	 */
	public double[] modelToEarth(double[] pnt) {

		double[] ret = new double[2];

		//MRTInterface.omerint(fwd, r_maj, r_min, scale_fact, azimuth, lon_orig, lat_orig, false_east, false_north, lon1, lat1, lon2, lat2, mode);
		MRTInterface.omerint(MRTInterface.inverse, _radius_maj, _radius_min, _scale_fact,_azimuth, _longOrig, _latOrig,
				_falseEast, _falseNorth, _lon1, _lat1, _lon2, _lat2, _mode);
		

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

		MRTInterface.omerint(MRTInterface.forward, _radius_maj, _radius_min, _scale_fact,_azimuth, _longOrig, _latOrig,
				_falseEast, _falseNorth, _lon1, _lat1, _lon2, _lat2, _mode);

		//MRTInterface.molwint(MRTInterface.forward, _radius, StrictMath.toRadians(_centerLong), _falseEast, _falseNorth);
		MRTInterface.geo2coord(getType(), StrictMath.toRadians(pnt[1]), StrictMath.toRadians(pnt[0]), ret); 

		return ret;
	}	

	public static void main(String[] args) {
		/*
	    HotineObliqueMercator model = new HotineObliqueMercator(6370997.0, -90, 0, 0);
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
    parametersAsString += (_scale_fact + " = scale factor\n");
    parametersAsString += (_azimuth + " = azimuth east of north\n");
    parametersAsString += (_longOrig + " = longitude of origin\n");
    parametersAsString += (_latOrig + " = latitude of origin\n");
    parametersAsString += (_falseEast + " = falseEasting\n");
    parametersAsString += (_falseNorth + " = falseNorthing\n");
    parametersAsString += (_lon1 + " = longitude 1 for center line\n");
    parametersAsString += (_lat1 + " = latitude 1 for center line\n");
    parametersAsString += (_lon2 + " = longitude 2 for center line\n");
    parametersAsString += (_lat2 + " = latitude 1 for center line\n");
    parametersAsString += (_mode + " = mode (which format type A or B)\n");
    

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
    _scale_fact = Double.parseDouble(valueAsString);

    indexOfNewline    = paramString.indexOf(magicNewline,indexOfDelimiter);
    indexOfDelimiter  = paramString.indexOf(magicString,indexOfNewline);
    valueAsString     = paramString.substring(indexOfNewline + 1,indexOfDelimiter);
    _azimuth = Double.parseDouble(valueAsString);

    indexOfNewline    = paramString.indexOf(magicNewline,indexOfDelimiter);
    indexOfDelimiter  = paramString.indexOf(magicString,indexOfNewline);
    valueAsString     = paramString.substring(indexOfNewline + 1,indexOfDelimiter);
    _longOrig = Double.parseDouble(valueAsString);
  
    indexOfNewline    = paramString.indexOf(magicNewline,indexOfDelimiter);
    indexOfDelimiter  = paramString.indexOf(magicString,indexOfNewline);
    valueAsString     = paramString.substring(indexOfNewline + 1,indexOfDelimiter);
    _latOrig = Double.parseDouble(valueAsString);
    
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
    _lon1 = Double.parseDouble(valueAsString);

    indexOfNewline    = paramString.indexOf(magicNewline,indexOfDelimiter);
    indexOfDelimiter  = paramString.indexOf(magicString,indexOfNewline);
    valueAsString     = paramString.substring(indexOfNewline + 1,indexOfDelimiter);
    _lat1 = Double.parseDouble(valueAsString);

    indexOfNewline    = paramString.indexOf(magicNewline,indexOfDelimiter);
    indexOfDelimiter  = paramString.indexOf(magicString,indexOfNewline);
    valueAsString     = paramString.substring(indexOfNewline + 1,indexOfDelimiter);
    _lon2 = Double.parseDouble(valueAsString);

    indexOfNewline    = paramString.indexOf(magicNewline,indexOfDelimiter);
    indexOfDelimiter  = paramString.indexOf(magicString,indexOfNewline);
    valueAsString     = paramString.substring(indexOfNewline + 1,indexOfDelimiter);
    _lat2 = Double.parseDouble(valueAsString);

    indexOfNewline    = paramString.indexOf(magicNewline,indexOfDelimiter);
    indexOfDelimiter  = paramString.indexOf(magicString,indexOfNewline);
    valueAsString     = paramString.substring(indexOfNewline + 1,indexOfDelimiter);
    _mode = Long.parseLong(valueAsString);

  }

}
