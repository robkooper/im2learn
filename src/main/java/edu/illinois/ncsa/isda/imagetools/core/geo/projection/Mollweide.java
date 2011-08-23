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
public class Mollweide extends ModelProjection {
	
	private double _radius;		/* (I) Radius of the earth (sphere)     */
	private double _centerLong;	/* (I) Center longitude                 */
	private double _falseEast;	/* x offset in meters                   */
	private double _falseNorth;	/* y offset in meters           		*/
	//note: the following instance variables are only used by PRJWriter and ProjectionLoaderPRJ
	String geographicCoordinateSystem;
	String projectedCoordinateSystem;
	String datum;
	String primeMeridian;
	double primeMeridianValue;
	String angularUnit;
	double angularUnitValue;
	String linearUnit;
	double linearUnitValue;
	double falseEasting;
	double falseNorthing;
	double centralMeridian;
	double minorRadius;

	public String getAngularUnit() {
		return angularUnit;
	}

	public void setAngularUnit(String angularUnit) {
		this.angularUnit = angularUnit;
	}

	public double getAngularUnitValue() {
		return angularUnitValue;
	}

	public void setAngularUnitValue(double angularUnitValue) {
		this.angularUnitValue = angularUnitValue;
	}

	public double getCentralMeridian() {
		return centralMeridian;
	}

	public void setCentralMeridian(double centralMeridian) {
		this.centralMeridian = centralMeridian;
	}

	public String getDatum() {
		return datum;
	}

	public void setDatum(String datum) {
		this.datum = datum;
	}

	public String getGeographicCoordinateSystem() {
		return geographicCoordinateSystem;
	}

	public void setGeographicCoordinateSystem(String geographicCoordinateSystem) {
		this.geographicCoordinateSystem = geographicCoordinateSystem;
	}

	public String getLinearUnit() {
		return linearUnit;
	}

	public void setLinearUnit(String linearUnit) {
		this.linearUnit = linearUnit;
	}

	public double getLinearUnitValue() {
		return linearUnitValue;
	}

	public void setLinearUnitValue(double linearUnitValue) {
		this.linearUnitValue = linearUnitValue;
	}

	public double getMinorRadius() {
		return minorRadius;
	}

	public void setMinorRadius(double minorRadius) {
		this.minorRadius = minorRadius;
	}

	public String getPrimeMeridian() {
		return primeMeridian;
	}

	public void setPrimeMeridian(String primeMeridian) {
		this.primeMeridian = primeMeridian;
	}

	public double getPrimeMeridianValue() {
		return primeMeridianValue;
	}

	public void setPrimeMeridianValue(double primeMeridianValue) {
		this.primeMeridianValue = primeMeridianValue;
	}

	public String getProjectedCoordinateSystem() {
		return projectedCoordinateSystem;
	}

	public void setProjectedCoordinateSystem(String projectedCoordinateSystem) {
		this.projectedCoordinateSystem = projectedCoordinateSystem;
	}

	public Mollweide()
	{
		setType(Projection.MOLLWEIDE);
	}
	
	public Mollweide(double radius, double centerLong, double falseEast, double falseNorth)
	{
		setType(Projection.MOLLWEIDE);
		
		_radius = radius;
		_centerLong = centerLong;
		_falseEast = falseEast;
		_falseNorth = falseNorth;
	}
	
	public void setRadius(double radius)
	{
		_radius = radius;
	}
	
	public void setCenterLong(double centerLong)
	{
		_centerLong = centerLong;
	}
	
	public void setFalseEast(double falseEast)
	{
		_falseEast = falseEast;
	}
	
	public void setFalseNorth(double falseNorth)
	{
		_falseNorth = falseNorth;
	}
	
	public double getRadius()
	{
		return _radius;
	}
	
	public double getCenterLong()
	{
		return _centerLong;
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
		
		MRTInterface.molwint(MRTInterface.inverse, _radius, 
				StrictMath.toRadians(_centerLong), _falseEast, _falseNorth);
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
			
		MRTInterface.molwint(MRTInterface.forward, _radius, 
				StrictMath.toRadians(_centerLong), _falseEast, _falseNorth);
		MRTInterface.geo2coord(getType(), StrictMath.toRadians(pnt[1]), StrictMath.toRadians(pnt[0]), ret); 
			  
		return ret;
	}	
	
	public static void main(String[] args) {
	    Mollweide model = new Mollweide(6370997.0, -90, 0, 0);
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
              
	}

  public String getProjectionParametersAsString() {
    String parametersAsString = new String();

    parametersAsString += (this.getType() + " = " + this.getClass() + "\n");
    parametersAsString += (_radius + " = radius\n");
    parametersAsString += (_centerLong + " = center longitude\n");
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
    _radius = Double.parseDouble(valueAsString);

    indexOfNewline    = paramString.indexOf(magicNewline,indexOfDelimiter);
    indexOfDelimiter  = paramString.indexOf(magicString,indexOfNewline);
    valueAsString     = paramString.substring(indexOfNewline + 1,indexOfDelimiter);
    _centerLong = Double.parseDouble(valueAsString);

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
