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
 * @author clutter
 * @TODO testing, verify correctness
 */
public class AlbersEqualAreaConic extends ModelProjection {
  private double _firstStandardParallel;
  private double _secondStandardParallel;
  private double _latOrigin;
  private double _lngOrigin;
  private double _radius;
  private double _eccentricitySquared;
  private double _falseEasting = 0;
  private double _falseNorthing = 0;
  

  public double getFirstStandardParallel()
  {
	  return _firstStandardParallel;
  }

  public double getSecondStandardParallel()
  {
	  return _secondStandardParallel;
  }

  public double getLatOrigin()
  {
	  return _latOrigin;
  }

  public double getLngOrigin()
  {
	  return _lngOrigin;
  }

  public double getRadius()
  {
	  return _radius;
  }

  public double getEccentricitySquared()
  {
	  return _eccentricitySquared;
  }

  public void setFalseEasting(double fe) {
    this._falseEasting = fe;
  }
  public double getFalseEasting() {
    return this._falseEasting;
  }

  public void setFalseNorthing(double fn) {
    this._falseNorthing = fn;
  }
  public double getFalseNorthing() {
    return this._falseNorthing;
  }


  public AlbersEqualAreaConic() {
	  setType(Projection.ALBERS_EQUAL_AREA_CONIC);
  }



  public AlbersEqualAreaConic(double rasterI, double rasterJ, double insertX,
                              double insertY, double scaleX, double scaleY,
                              int numRow, int numCol, double fsp, double ssp,
                              double latOr, double lngOr,
                              double radius, double esq) {
    super(rasterI, rasterJ, insertX, insertY, scaleX, scaleY, numRow, numCol);
    _firstStandardParallel = fsp;
    _secondStandardParallel = ssp;
    _latOrigin = latOr;
    _lngOrigin = lngOr;
    _radius = radius;
    _eccentricitySquared = esq;
    setType(Projection.ALBERS_EQUAL_AREA_CONIC);
  }

  public void setOrigin(double latOrigin, double lngOrigin)
  {
	  _latOrigin = latOrigin;
	  _lngOrigin = lngOrigin;
  }

  public void setParallels(double first, double second)
  {
	  _firstStandardParallel = first;
	  _secondStandardParallel = second;
  }

  public void setRadius(double radius)
  {
	  _radius = radius;
  }

  public void setEccentricSqure(double esq)
  {
	  _eccentricitySquared = esq;
  }

  public double[] modelToEarth(double[] pt) {
    double a = _radius;
    double esquared = _eccentricitySquared;

    double first = Math.toRadians(_firstStandardParallel);
    double second = Math.toRadians(_secondStandardParallel);

    //_firstStandardParallel = Math.toRadians(_firstStandardParallel);
    //_secondStandardParallel = Math.toRadians(_secondStandardParallel);

    //latOrigin = Math.toRadians(latOrigin);
    double phi_0 = Math.toRadians(_latOrigin);
//    double lambda_0 = Math.toRadians(_lngOrigin);
    //double phi = Math.toRadians(pt[0]);
    //double lambda = Math.toRadians(pt[1]);
    
    double x = pt[0] - _falseEasting;
    double y = pt[1] - _falseNorthing;
    
    // compute the constants for the map
    double m_1 = Math.cos(first)/
        Math.sqrt((1-esquared * Math.pow(Math.sin(first), 2)));

    double m_2 = Math.cos(second)/
        Math.sqrt((1-esquared*Math.pow(Math.sin(second), 2)));

    double q_1 = findQ(first, esquared);
    double q_2 = findQ(second, esquared);
    double q_0 = findQ(phi_0, esquared);

    double n = (Math.pow(m_1, 2)-Math.pow(m_2, 2))/(q_2-q_1);
    double C = Math.pow(m_1, 2)+n*q_1;
    double rho_0 = a*Math.sqrt(C-n*q_0)/n;
    // end constants

    double rho = Math.sqrt( (Math.pow(x,2)+ Math.pow(rho_0-y, 2)));
    double theta = Math.atan(x/(rho_0-y));
    double q = (C-Math.pow((rho*n/a), 2))/n;
    double test_phi = Math.asin(q/2);
    // now we have a first trial of phi.
    // iterate three times with equation (3-16).

    double e= Math.sqrt(esquared);
//      esquared = 0.00676866;
//      double e = 0.0822719;
    double test_phi_degrees = Math.toDegrees(test_phi);
//      double test_phi_degrees = 34.7879983;
//      test_phi = Math.toRadians(test_phi_degrees);

    // iteration code
    for(int i = 0; i < 3; i++) {
      double zero = Math.pow(1-esquared*Math.pow(Math.sin(test_phi), 2), 2)/(2*Math.cos(test_phi));
      double one = (q/(1-esquared));
      double two = (Math.sin(test_phi))/(1-esquared*Math.pow(Math.sin(test_phi),2));

      double logArg = (1-e*Math.sin(test_phi))/(1+e*Math.sin(test_phi));
      double three = (1/(2*e))*Math.log(logArg);

      double ret = zero*(one-two+three)*180/Math.PI;
      ret += test_phi_degrees;
      test_phi = Math.toRadians(ret);
      test_phi_degrees = Math.toDegrees(test_phi);
    }

    double phi = test_phi;

    phi = Math.toDegrees(phi);

    double[] ret = new double[2];
    ret[1] = _lngOrigin + Math.toDegrees(theta)/n;
    ret[0] = phi;
    return ret;
  }

  public double[] earthToModel(double[] pt) {
      //double a = 6378206.4;
      //double esquared = 0.00676866;

      //_firstStandardParallel = Math.toRadians(_firstStandardParallel);
      //_secondStandardParallel = Math.toRadians(_secondStandardParallel);

      double first = Math.toRadians(_firstStandardParallel);
      double second = Math.toRadians(_secondStandardParallel);

      //latOrigin = Math.toRadians(latOrigin);
      double lambda_0 = Math.toRadians(_lngOrigin);
      double phi_0 = Math.toRadians(_latOrigin);
      double lambda = Math.toRadians(pt[1]);
      double phi = Math.toRadians(pt[0]);
      

      // compute the constants for the map

      double m_1 = Math.cos(first)/
          Math.sqrt((1-_eccentricitySquared * Math.pow(Math.sin(first), 2)));

      double m_2 = Math.cos(second)/
          Math.sqrt((1-_eccentricitySquared*Math.pow(Math.sin(second), 2)));

      double q_1 = findQ(first, _eccentricitySquared);
      double q_2 = findQ(second, _eccentricitySquared);
      double q_0 = findQ(phi_0, _eccentricitySquared);

      double n = (Math.pow(m_1, 2)-Math.pow(m_2, 2))/(q_2-q_1);
      double C = Math.pow(m_1, 2)+n*q_1;
      double rho_0 = _radius*Math.sqrt(C-n*q_0)/n;

      // end constants

      double q = findQ(phi, _eccentricitySquared);

      double rho = _radius*Math.sqrt(C-n*q)/n;

      // this Math.abs is a hack!! equation (14-4)!!!  They used
      // -96 instead of positive 96.  Not sure why yet.
      //double theta = n*Math.abs((Math.toDegrees(lambda)-Math.toDegrees(lambda_0)));
      double theta = n*(Math.toDegrees(lambda)-Math.toDegrees(lambda_0));
      double x = rho*Math.sin(Math.toRadians(theta));
      double y = rho_0 - rho*Math.cos(Math.toRadians(theta));

      return new double[] {x+_falseEasting, y+_falseNorthing};
  }

  private static double findQ(double stdParallel, double esq) {
    double e = Math.sqrt(esq);

    double logArg = (1-e*Math.sin(stdParallel))/(1+e*Math.sin(stdParallel));
    double logPart = Math.log(logArg);
    logPart *= (1/(2*e));

    double tmp = Math.sin(stdParallel)/(1-esq*Math.pow(Math.sin(stdParallel), 2));
    tmp = tmp - logPart;
    tmp *= (1-esq);

    return tmp;
  }
  
  /*
  public String toString(){
     String temp = new String("Albers Equal Area Conic\n");
     temp +=  "_firstStandardParallel="+_firstStandardParallel + "\n";
     temp +=  "_secondStandardParallel="+_secondStandardParallel + "\n";
     temp +=  "_latOrigin="+_latOrigin + "\n";
     temp +=  "_lngOrigin="+_lngOrigin + "\n";
     temp +=  "_radius="+_radius + "\n";
     temp +=  "_eccentricitySquared="+_eccentricitySquared + "\n";
     temp +=  "_falseEasting="+_falseEasting + "\n";
     temp +=  "_falseNorthing="+_falseNorthing + "\n";
     return temp;	  

  }
  */
  
  public String getProjectionParametersAsString() {
    String parametersAsString = new String();

    parametersAsString += (this.getType() + " = " + this.getClass() + "\n");
    parametersAsString += (_firstStandardParallel + " = first standard parallel\n");
    parametersAsString += (_secondStandardParallel + " = second standard parallel\n");
    parametersAsString += (_latOrigin + " = latitude of origin\n");
    parametersAsString += (_lngOrigin + " = longitude of origin\n");
    parametersAsString += (_radius + " = radius\n");
    parametersAsString += (_eccentricitySquared + " = eccentricity squared\n");
    parametersAsString += (_falseEasting + " = false easting\n");
    parametersAsString += (_falseNorthing + " = false northing\n");

    System.out.println("[" + parametersAsString + "]");
    
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
    _firstStandardParallel = Double.parseDouble(valueAsString);

    indexOfNewline    = paramString.indexOf(magicNewline,indexOfDelimiter);
    indexOfDelimiter  = paramString.indexOf(magicString,indexOfNewline);
    valueAsString     = paramString.substring(indexOfNewline + 1,indexOfDelimiter);
    _secondStandardParallel = Double.parseDouble(valueAsString);

    indexOfNewline    = paramString.indexOf(magicNewline,indexOfDelimiter);
    indexOfDelimiter  = paramString.indexOf(magicString,indexOfNewline);
    valueAsString     = paramString.substring(indexOfNewline + 1,indexOfDelimiter);
    _latOrigin = Double.parseDouble(valueAsString);

    indexOfNewline    = paramString.indexOf(magicNewline,indexOfDelimiter);
    indexOfDelimiter  = paramString.indexOf(magicString,indexOfNewline);
    valueAsString     = paramString.substring(indexOfNewline + 1,indexOfDelimiter);
    _lngOrigin = Double.parseDouble(valueAsString);

    indexOfNewline    = paramString.indexOf(magicNewline,indexOfDelimiter);
    indexOfDelimiter  = paramString.indexOf(magicString,indexOfNewline);
    valueAsString     = paramString.substring(indexOfNewline + 1,indexOfDelimiter);
    _radius = Double.parseDouble(valueAsString);
  
    indexOfNewline    = paramString.indexOf(magicNewline,indexOfDelimiter);
    indexOfDelimiter  = paramString.indexOf(magicString,indexOfNewline);
    valueAsString     = paramString.substring(indexOfNewline + 1,indexOfDelimiter);
    _eccentricitySquared = Double.parseDouble(valueAsString);
    
    indexOfNewline    = paramString.indexOf(magicNewline,indexOfDelimiter);
    indexOfDelimiter  = paramString.indexOf(magicString,indexOfNewline);
    valueAsString     = paramString.substring(indexOfNewline + 1,indexOfDelimiter);
    _falseEasting = Double.parseDouble(valueAsString);

    indexOfNewline    = paramString.indexOf(magicNewline,indexOfDelimiter);
    indexOfDelimiter  = paramString.indexOf(magicString,indexOfNewline);
    valueAsString     = paramString.substring(indexOfNewline + 1,indexOfDelimiter);
    _falseNorthing = Double.parseDouble(valueAsString);
  }

  
  
  
  
  
}
