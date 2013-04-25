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

/**
 * The Sinusoidal projection
 * Taken from Map Projections, A Working Manual pg 243
 * @TODO testing
 */
public class Sinusoidal
    extends ModelProjection {

  /** radius of sphere */
  private double _radius;
  /** central meridian */
  private double _lambda_0;
  /** eccentricity squared */
  private double _eccentricitySquared;

  public Sinusoidal() {
    setType(Projection.SINUSOIDAL);
  }

  public Sinusoidal(double radius, int numRows, int numCols,
                    double[] upperLeft, double[] lowerRight) {
    super(0.0, 0.0, upperLeft[0], upperLeft[1], 0.0, 0.0, numRows, numCols);
    double scalex = lowerRight[0]-upperLeft[0];
    this.setScaleX(scalex);
    double scaley = lowerRight[1]-upperLeft[1];
    this.setScaleY(scaley);
    setType(Projection.SINUSOIDAL);
    setEllipsoid(Ellipsoid.SPHERE);
  }

  public void setRadius(double r) {
    _radius = r;
  }
  public double getRadius() {
    return _radius;
  }
  public void setCentralMeridian(double cm) {
    _lambda_0 = cm;
  }
  public double getCentralMeridian() {
    return _lambda_0;
  }
  public void setESquared(double esq) {
    _eccentricitySquared = esq;
  }
  public double getESquared() {
    return _eccentricitySquared;
  }

  // earth to model

  // (30-8)
  // (3-21)
  // (30-9)

  public double[] earthToModel(double[] pt) {
    // x = a (lamdba - lamdba0) cos phi/(1 - eccsq (sin phi)^2)^(1/2)

    // longitude
    double lambda = pt[1];
    // latitude
    double phi = pt[0];

    double top = _radius * (lambda - _lambda_0) *
        StrictMath.toRadians(StrictMath.cos(StrictMath.toRadians(phi)));
    double sin = StrictMath.pow(StrictMath.sin(StrictMath.toRadians(phi)), 2);
    double bottom = StrictMath.sqrt(1 - _eccentricitySquared * sin);

    double x = top / bottom;

    // M = a [ (1 - esq/4 - 3e^4/64 - 5e^6/256) phi
    // - (3esq/8 + 3e^4/32 + 45 e^6/1024) sin (2 phi)
    // + (15 e^4/256 + 45 e^6/1024) sin (4 phi)
    // - (35 e^6/3072) sin (6 phi) ]

    double one = (1 - _eccentricitySquared / 4 -
                  3 * StrictMath.pow(_eccentricitySquared, 2) / 64
                  - 5 * StrictMath.pow(_eccentricitySquared, 3) / 256) *
        StrictMath.toRadians(phi);
    double two = (3 * _eccentricitySquared / 8 +
                  3 * StrictMath.pow(_eccentricitySquared, 2) / 32
                  + 45 * StrictMath.pow(_eccentricitySquared, 3) / 1024) *
        StrictMath.sin(2 * phi);
    double three = (15 * StrictMath.pow(_eccentricitySquared, 2) / 256 +
                    45 * StrictMath.pow(_eccentricitySquared, 3) / 1024) *
        StrictMath.sin(4 * phi);
    double four = (35 * StrictMath.pow(_eccentricitySquared, 3) / 3072) *
        StrictMath.sin(6 * phi);

    double M = _radius * (one - two + three - four);
    double y = M;

    return new double[] {
        x, y};
  }

  // model to earth
  public double[] modelToEarth(double[] pt) {
    double x = pt[0];
    double y = pt[1];

    // (30-10)
    double M = y;


    /* For perfect sphere test

    double phiTest = y/_radius * 180/Math.PI;
    double lambdaTest = _lambda_0 + (x/(_radius*Math.cos(Math.toRadians(phiTest))))*180/Math.PI;

    System.out.println("X = " + x + "   Y = " + y);
    System.out.println("Phi Test : " + phiTest + "    lambdaTest : " + lambdaTest);
    */

    // (7-19)
    // mu = M/[a(1-esq/4 - 3e^4/64 - 5e^6/256 ..)]
    double mu = M / (_radius * (1 - _eccentricitySquared / 4 -
                                3 * StrictMath.pow(_eccentricitySquared, 2) /
                                64 -
                                5 * StrictMath.pow(_eccentricitySquared, 3) /
                                256));

    mu = StrictMath.toDegrees(mu);

    // (3-24)
    // e1 = [1 - (1-esq)^(1/2)]/[1 + (1 - esq)^(1/2)]
    double e1 = (1 - StrictMath.sqrt(1 - _eccentricitySquared)) /
        (1 + StrictMath.sqrt(1 - _eccentricitySquared));

    //System.out.println("e1: "+e1);

    // (3-26)
    // phi = mu + (3e1/2 - 27e1^3/32) sin 2 mu + (21 e1^2/16 - 55 e1^4/32) sin 4 mu
    // + (151e1^3/96) sin 6 mu + (1097 e1^4/512) sin 8 mu
    double phi = mu +
        (3 * e1 / 2 - 27 * StrictMath.pow(e1, 3) / 32) *
        StrictMath.sin(2 * StrictMath.toRadians(mu))
        +
        (21 * Math.pow(e1, 2) / 16 - 55 * Math.pow(e1, 4) / 32) *
        StrictMath.sin(4 * Math.toRadians(mu))
        + (151 * Math.pow(e1, 3) / 96) * StrictMath.sin(6 * Math.toRadians(mu)) +
        (1097 * Math.pow(e1, 4) / 512)
        * StrictMath.sin(8 * Math.toRadians(mu));

    // (30-11)
    // lambda = lambda0 + x (1-esq (sin phi)^2)^(1/2) / (a cos phi)
    double top = x *
        Math.sqrt(1 -
                  _eccentricitySquared *
                  Math.pow(StrictMath.sin(Math.toRadians(phi)), 2));
    double bottom = _radius * StrictMath.cos(StrictMath.toRadians(phi)) *
        (Math.PI / 180);
    double lambda = _lambda_0 + top / bottom;

    return new double[] {
    		phi, lambda };
  }

  public static void main(String[] args) {
    Sinusoidal sin = new Sinusoidal();
    sin._radius = 6378206.4;
    sin._eccentricitySquared = 0.006766866;
    sin._lambda_0 = -90;

    double[] point = new double[] {
         -75, -50};
    sin.earthToModel(point);
    point = new double[] {
        1075471.5, -5540628};
    sin.modelToEarth(point);
  }
  
  public String getProjectionParametersAsString() {
    String parametersAsString = new String();

    parametersAsString += (this.getType() + " = " + this.getClass() + "\n");
    parametersAsString += (_radius + " = radius\n");
    parametersAsString += (_lambda_0 + " = central meridian; lambda_0\n");
    parametersAsString += (_eccentricitySquared + " = eccentricitySquared\n");

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
    _lambda_0 = Double.parseDouble(valueAsString);

    indexOfNewline    = paramString.indexOf(magicNewline,indexOfDelimiter);
    indexOfDelimiter  = paramString.indexOf(magicString,indexOfNewline);
    valueAsString     = paramString.substring(indexOfNewline + 1,indexOfDelimiter);
    _eccentricitySquared = Double.parseDouble(valueAsString);

  }

  
}
