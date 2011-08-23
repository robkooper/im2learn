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
* @author Chulyun
*/
public class Mercator extends ModelProjection {
	private double _rMaj;		/* major axis               */
	private double _rMin;		/* minor axis               */
	private double _centerLon;	/* center longitude         */
	private double _centerLat;	/* center latitude          */
	private double _falseEast;	/* x offset in meters       */
	private double _falseNorth;	/* y offset in meters       */
	//note: the following instance variables are only used in PRJWriter and ProjectionLoaderPRJ
	String _geographicCoordinateSystem;
	String _projectedCoordinateSystem;
	String _datum;
	String _primeMeridian;
	double _primeMeridianValue;
	String _angularUnit;
	String _linearUnit;
	double _angularUnitValue;
	double _linearUnitValue;
	double _centralMeridian;
	double _firstParallel;//(first and only standard parallel)

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

	public double get_firstParallel() {
		return _firstParallel;
	}

	public void set_firstParallel(double parallel) {
		_firstParallel = parallel;
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

	public Mercator()
	{
		setType(Projection.MERCATOR);	
	}
	
	public Mercator(double rMaj, double rMin, double centerLon, 
			double centerLat, double falseEast, double falseNorth)
	{
		setType(Projection.MERCATOR);
		
		_rMaj = rMaj;
		_rMin = rMin;
		_centerLon = centerLon;
		_centerLat = centerLat;
		_falseEast = falseEast;
		_falseNorth = falseNorth;
	}
	
	public void setRMaj(double rMaj)
	{
		_rMaj = rMaj;
	}
	
	public void setRMin(double rMin)
	{
		_rMin = rMin;
	}
	
	public void setCenterLon(double centerLon)
	{
		_centerLon = centerLon;
	}
	
	public void setCenterLat(double centerLat)
	{
		_centerLat = centerLat;
	}
	
	public void setFalseEast(double falseEast)
	{
		_falseEast = falseEast;
	}
	
	public void setFalseNorth(double falseNorth)
	{
		_falseNorth = falseNorth;
	}
	
	public double getRMaj()
	{
		return _rMaj;
	}
	
	public double getRMin()
	{
		return _rMin;
	}
	
	public double getCenterLon()
	{
		return _centerLon;
	}
	
	public double getCenterLat()
	{
		return _centerLat;
	}
	
	public double getFalseEast()
	{
		return _falseEast;
	}
	
	public double getFalseNorth()
	{
		return _falseNorth;
	}	
	
	/**
	 * this goes from meters to lat lng
	 * @param pnt
	 * @return
	 */
	public double[] modelToEarth(double[] pnt) {
	 
		double[] ret = new double[2];
		
		MRTInterface.merint(MRTInterface.inverse, _rMaj, _rMin, 
				StrictMath.toRadians(_centerLon), StrictMath.toRadians(_centerLat), _falseEast,	_falseNorth);
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
			
		MRTInterface.merint(MRTInterface.forward, _rMaj, _rMin, 
				StrictMath.toRadians(_centerLon), StrictMath.toRadians(_centerLat), _falseEast,	_falseNorth);
		MRTInterface.geo2coord(getType(), StrictMath.toRadians(pnt[1]), StrictMath.toRadians(pnt[0]), ret); 
			  
		return ret;
	}	
	
	
	public static void main(String[] args) {
	    Mercator model = new Mercator(6378206.4, 6356583.79, -180, 0, 0, 0);
        double[] lonlat = new double[] {-75, 35};
        double[] result;
        result = model.earthToModel(lonlat);
        System.out.println("earthToModel Test");
        System.out.println(result[0]);
        System.out.println(result[1]);
        
        double[] xy = new double[] { 11688673.7, 4139145.6 };
        result = model.modelToEarth(xy);
        System.out.println("earthToModel Test");
        System.out.println(result[0]);
        System.out.println(result[1]);        
	}
  
  public String getProjectionParametersAsString() {
    String parametersAsString = new String();

    parametersAsString += (this.getType() + " = " + this.getClass() + "\n");
    parametersAsString += (_rMaj + " = major radius\n");
    parametersAsString += (_rMin + " = minor radius\n");
    parametersAsString += (_centerLon + " = center longitude\n");
    parametersAsString += (_centerLat + " = center latitude\n");
    parametersAsString += (_falseEast + " = false easting\n");
    parametersAsString += (_falseNorth + " = false northing\n");

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
    _rMaj = Double.parseDouble(valueAsString);

    indexOfNewline    = paramString.indexOf(magicNewline,indexOfDelimiter);
    indexOfDelimiter  = paramString.indexOf(magicString,indexOfNewline);
    valueAsString     = paramString.substring(indexOfNewline + 1,indexOfDelimiter);
    _rMin = Double.parseDouble(valueAsString);

    indexOfNewline    = paramString.indexOf(magicNewline,indexOfDelimiter);
    indexOfDelimiter  = paramString.indexOf(magicString,indexOfNewline);
    valueAsString     = paramString.substring(indexOfNewline + 1,indexOfDelimiter);
    _centerLon = Double.parseDouble(valueAsString);

    indexOfNewline    = paramString.indexOf(magicNewline,indexOfDelimiter);
    indexOfDelimiter  = paramString.indexOf(magicString,indexOfNewline);
    valueAsString     = paramString.substring(indexOfNewline + 1,indexOfDelimiter);
    _centerLat = Double.parseDouble(valueAsString);

    indexOfNewline    = paramString.indexOf(magicNewline,indexOfDelimiter);
    indexOfDelimiter  = paramString.indexOf(magicString,indexOfNewline);
    valueAsString     = paramString.substring(indexOfNewline + 1,indexOfDelimiter);
    _falseEast = Double.parseDouble(valueAsString);
  
    indexOfNewline    = paramString.indexOf(magicNewline,indexOfDelimiter);
    indexOfDelimiter  = paramString.indexOf(magicString,indexOfNewline);
    valueAsString     = paramString.substring(indexOfNewline + 1,indexOfDelimiter);
    _falseNorth = Double.parseDouble(valueAsString);
  }

}
