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

//import ncsa.im2learn.core.datatype.GeoInformation;
import edu.illinois.ncsa.isda.im2learn.core.geo.GeoException;

/**
     * Created by IntelliJ IDEA. User: kooper Date: Aug 30, 2004 Time: 4:29:35 PM To
 * change this template use File | Settings | File Templates.
 */
public class UTMNorth
    extends ModelProjection {

  private double _centerLatitude;
  private double _centerLongitude;

  private double _falseNorthing;
  private double _falseEasting;

  private double _scaleFactor = 0.9996;
  private double _majorAxis;
  private double _minorAxis;
  private double _inverseFlat;

  private double _eccentricitySquared;

  private int _zone;

/*  public Projection getCopy() {
    UTMNorth retval = new UTMNorth(getRasterSpaceI(), getRasterSpaceJ(),
                                   getInsertionX(), getInsertionY(),
                                   getScaleX(), getScaleY(), getNumRows(),
                                   getNumCols(), _centerLatitude,
                                   _centerLongitude, _falseEasting,
                                   _falseNorthing);
    retval._scaleFactor = _scaleFactor;
    retval._majorAxis = _majorAxis;
    retval._minorAxis = _minorAxis;
    retval._inverseFlat = _inverseFlat;
    retval._eccentricitySquared = _eccentricitySquared;
    retval._zone = _zone;

    return retval;
  }*/

 /* 
  public String toString() {
    String reString = "";
    reString += "scaleX (column res) =" + this.getScaleX() + "\n";
    reString += "scaleY (row res)" + getScaleY() + "\n";
    //if (_eastingInsertionValue != -1) {
    reString += "modelSpaceX (eastingInsertionValue) =" + getModelSpaceX() +
        "\n";
    //}
    //if (_northingInsertionValue != -1) {
    reString += "modelSpaceY (northingInsertionValue) =" + getModelSpaceY() +
        "\n";
    //}
    //if (_GeoProjFalseEasting != -1) {
    reString += "False Easting =" + _falseEasting + "\n";
    //}
    //if (_GeoProjFalseNorthing != -1) {
    reString += "False Northing=" + _falseNorthing + "\n";
    //}
    //if (_radius != -1) {
    //   reString += "radius=" + _radius + "\n";
    //}
    //if (_majorAxis != -1) {
    reString += "majorAxis=" + _majorAxis + "\n";
    //}
    if (_minorAxis != -1) {
      reString += "minorAxis=" + _minorAxis + "\n";
    }
    //if (_eccentricitySqrd != -1) {
    //    reString += "eccentricitySqrd=" + _eccentricitySqrd + "\n";
    //}
    if (_inverseFlat != -1) {
      reString += "inverseFlat=" + _inverseFlat + "\n";
    }
    if (_scaleFactor != -1) {
      reString += "scaleFactor=" + _scaleFactor + "\n";
    }
    //if (!_datum.equals("empty")) {
    //    reString += "datum=" + _datum + "\n";
    //}
    //if (_geoScaleX != -1) {
    //    reString += "geoScaleX=" + _geoScaleX + "\n";
    //}
    //if (_geoScaleY != -1) {
    //    reString += "geoScaleY=" + _geoScaleY + "\n";
    //}
    //if (_geoScaleZ != -1) {
    //    reString += "geoScaleZ=" + _geoScaleZ + "\n";
    //}
    //if (_GeoProjCenterLat != -1) {
    //    reString += "GeoProjCenterLat=" + _GeoProjCenterLat + "\n";
    //}
    //if (_GeoProjCenterLng != -1) {
    //    reString += "GeoProjCenterLng=" + _GeoProjCenterLng + "\n";
    //}
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
    //if (_modelSpaceX != -1) {
    //    reString += "modelSpaceX=" + _modelSpaceX + "\n";
    //}
    //if (_modelSpaceY != -1) {
    //    reString += "modelSpaceY=" + _modelSpaceY + "\n";
    //}
    //if (_modelSpaceZ != -1) {
    //   reString += "modelSpaceZ=" + _modelSpaceZ + "\n";
    //}
    //if (!_GeoProjectedCSType.equals("none")) {
    //    reString += "GeoProjectedCSType=" + _GeoProjectedCSType + "\n";
    //}
    //if (!_ellipsoidName.equals("none")) {
    try {
      reString += "ellipsoidName=" + Ellipsoid.getName(getEllipsoid()) + "\n";
    }
    catch (Exception ex) {

    }
    //}
    //if (_modelType != -1) {
    //    reString += "modelType=" + _modelType + "\n";
    //}
    //if (_UTMzone != -1) {
    //    reString += "UTMzone=" + _UTMzone + "\n";
    //}
    return reString;
  }
*/

  public void setCenterLatitude(double d) {
    _centerLatitude = d;
  }

  public void setCenterLongitude(double d) {
    _centerLongitude = d;
  }

  public void setFalseNorthing(double d) {
    _falseNorthing = d;
  }

  public void setFalseEasting(double d) {
    _falseEasting = d;
  }

  public void setScaleFactor(double d) {
    _scaleFactor = d;
  }

  public void setMajorAxis( double d )
  {
	  _majorAxis = d;
  }
  
  public void setMinorAxis( double d )
  {
	  _minorAxis = d;
  }
  
  public void setInverseFlattening( double d )
  {
	  _inverseFlat = d;
  }
  
  public void setESquared( double d )
  {
	  _eccentricitySquared = d;
  }
  
  
  public void setEllipsoid(int el) {
    super.setEllipsoid(el);
    try {
      _majorAxis = Ellipsoid.getMajorAxis(el);
      _minorAxis = Ellipsoid.getMinorAxis(el);
      _inverseFlat = Ellipsoid.getInverseFlattening(el);
      _eccentricitySquared = getESquared();
    }
    catch (Exception ex) {

    }
  }

  public UTMNorth() {
    setType(Projection.UTM_NORTHERN_HEMISPHERE);
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
  public UTMNorth(double rasterI, double rasterJ, double insertX,
                       double insertY,
                       double scaleX, double scaleY, int numRow, int numCol) {
    super(rasterI, rasterJ, insertX, insertY, scaleX, scaleY, numRow, numCol);
    setType(Projection.UTM_NORTHERN_HEMISPHERE);
  }

  /**
   *
   * @param rasterI
   * @param rasterJ
   * @param insertX
   * @param insertY
   * @param scaleX
   * @param scaleY
   * @param numRow
   * @param numCol
   * @param centerLat
   * @param centerLon
   * @param fe
   * @param fn
   */
  public UTMNorth(double rasterI, double rasterJ, double insertX,
                       double insertY,
                       double scaleX, double scaleY, int numRow, int numCol,
                       double centerLat, double centerLon, double fe, double fn) {
    super(rasterI, rasterJ, insertX, insertY, scaleX, scaleY, numRow, numCol);
    this._centerLatitude = centerLat;
    this._centerLongitude = centerLon;
    this._falseEasting = fe;
    this._falseNorthing = fn;
    setType(Projection.UTM_NORTHERN_HEMISPHERE);
  }

  public double[] earthToModel(double[] point) throws GeoException {
    return fromLatLon(point);
  }

  public double[] modelToEarth(double[] point) throws GeoException {
    return toLatLon(point);
  }

  private double[] toLatLon(double[] eastingnorthing) throws GeoException {
    double phi_0 = _centerLatitude;
    double lamda_0 = _centerLongitude;

    //Easting and false easting
    double E = eastingnorthing[0];
    double FE = _falseEasting;
    
    //Northing and false northing
    double N = eastingnorthing[1];
    double FN = _falseNorthing;

    //eccentricity of ellipsoid
    double eSquare = this._eccentricitySquared;
    //double eSquare = .00669437999013;
    /*if (eSquare < 0 || eSquare == 1.0) {
      throw new GeoException("ERROR: eSquare is invalid = " + eSquare);
    }*/

    //REAL VALUE
    double a = _majorAxis; //geoinfo.GetMajorAxis();

    double eTickSquare = eSquare / (1.0 - eSquare);

    //REAL VALUE
    //attemptSetScale(.9996);	//ALWAYS THIS VALUE FOR UTM
    //_scaleFactor = .9996;
    double k_0 = _scaleFactor; //geoinfo.GetScaleFactor();

    double term1 = (1 - eSquare / 4 - 3 * Math.pow(eSquare, 2) / 64 -
                    5 * Math.pow(eSquare, 3) / 256) * Math.toRadians(phi_0);
    double term2 = (3 * eSquare / 8 + 3 * Math.pow(eSquare, 2) / 32 +
                    45 * Math.pow(eSquare, 3) / 1024) *
        Math.sin(2 * Math.toRadians(phi_0));
    double term3 = (15 * Math.pow(eSquare, 2) / 256 +
                    45 * Math.pow(eSquare, 3) / 1024) *
        Math.sin(4 * Math.toRadians(phi_0));
    double term4 = (35 * Math.pow(eSquare, 3) / 3072) *
        Math.sin(6 * Math.toRadians(phi_0));
    double LargeSum = term1 - term2 + term3 - term4;

    double M_0 = a * LargeSum;

    double M_1 = M_0 + (N - FN) / k_0;

    double mu_1 = M_1 / (a * (1 - eSquare * .25 - 3 * Math.pow(eSquare, 2) / 64 -
                              5 * Math.pow(eSquare, 3) / 256));

    double e_1 = (1 - Math.pow( (1 - eSquare), .5)) / (1
        + Math.pow( (1 - eSquare), .5));

    double phi_1 = mu_1 +
        (1.5 * e_1 - 27 * Math.pow(e_1, 3) / 32) * Math.sin(2 * mu_1) +
        (21 * e_1 * e_1 / 16 - 55 * Math.pow(e_1, 4) / 32) * Math.sin(4 * mu_1) +
        (151 * Math.pow(e_1, 3) / 96) * Math.sin(6 * mu_1) +
        (1097 * Math.pow(e_1, 4) / 512) * Math.sin(8 * mu_1);

    double v_1 = a / Math.pow( (1 - eSquare * Math.sin(phi_1) *
                                Math.sin(phi_1)), .5);

    double D = (E - FE) / (v_1 * k_0);

    double T_1 = Math.tan(phi_1) * Math.tan(phi_1);

    double C_1 = eTickSquare * Math.cos(phi_1) * Math.cos(phi_1);

    double rho_1 = (a * (1 - eSquare)) /
        Math.pow( (1 - eSquare * Math.pow(Math.sin(phi_1), 2)), 1.5);

    double Bigsum = .5 * D * D -
        (5 + 3 * T_1 + 10 * C_1 - 4 * C_1 * C_1 - 9 * eTickSquare) *
        Math.pow(D, 4) / 24
        + (61 + 90 * T_1 + 298 * C_1 + 45 * T_1 * T_1 - 252 * eTickSquare -
           3 * C_1 * C_1) * Math.pow(D, 6) / 720;

    double Bigsum2 = D - (1 + 2 * T_1 + C_1) * Math.pow(D, 3) / 6 +
        (5 - 2 * C_1 + 28 * T_1 - 3 * C_1 * C_1 + 8 * Math.pow(eTickSquare, 2)
         + 24 * T_1 * T_1) * Math.pow(D, 5) / 120;

    //FINAL VALUES
    double lamda = Math.toRadians(lamda_0) + Bigsum2 / Math.cos(phi_1);

    double phi = phi_1 - v_1 * Math.tan(phi_1) * Bigsum / rho_1;

    double[] ret = new double[2];

    ret[0] = Math.toDegrees(lamda);
    ret[1] = Math.toDegrees(phi);

    return ret;
  }

  private double[] fromLatLon(double[] lonlat) throws GeoException {

    double phi = lonlat[1];
    double phi_0 = _centerLatitude; //_imgGeoObject.GetGeoProjCenterLat();
    double eSquare = this._eccentricitySquared;
    double a = _majorAxis; //_imgGeoObject.GetMajorAxis();

    double FN = _falseNorthing; //_imgGeoObject.GetGeoProjFalseNorthing();
    double FE = _falseEasting; //_imgGeoObject.GetGeoProjFalseEasting();

    double lamda = lonlat[0]; //pnt.ptsDouble[1];
    double lamda_0 = _centerLongitude; //_imgGeoObject.GetGeoProjCenterLng();

    //attemptSetScale(.9996); //ALWAYS THIS VALUE FOR UTM northern hemisphere
    double k_0 = _scaleFactor; //_imgGeoObject.GetScaleFactor();
    double v = a / Math.pow( (1 - eSquare * Math.sin(Math.toRadians(phi)) *
                              Math.sin(Math.toRadians(phi))), .5);
    double A = (Math.toRadians(lamda) - Math.toRadians(lamda_0)) *
        Math.cos(Math.toRadians(phi));
    double eTickSquare = eSquare / (1 - eSquare);

    double T = Math.tan(Math.toRadians(phi)) * Math.tan(Math.toRadians(phi));

    double C = eSquare / (1 - eSquare) * Math.cos(Math.toRadians(phi)) *
        Math.cos(Math.toRadians(phi));

    double term1 = (1 - eSquare / 4 - 3 * Math.pow(eSquare, 2) / 64 -
                    5 * Math.pow(eSquare, 3) / 256) * Math.toRadians(phi);

    double term1_0 = (1 - eSquare / 4 - 3 * Math.pow(eSquare, 2) / 64 -
                      5 * Math.pow(eSquare, 3) / 256) * Math.toRadians(phi_0);

    double term2 = (3 * eSquare / 8 + 3 * Math.pow(eSquare, 2) / 32 +
                    45 * Math.pow(eSquare, 3) / 1024) *
        Math.sin(2 * Math.toRadians(phi));
    double term2_0 = (3 * eSquare / 8 + 3 * Math.pow(eSquare, 2) / 32 +
                      45 * Math.pow(eSquare, 3) / 1024) *
        Math.sin(2 * Math.toRadians(phi_0));

    double term3 = (15 * Math.pow(eSquare, 2) / 256 +
                    45 * Math.pow(eSquare, 3) / 1024) *
        Math.sin(4 * Math.toRadians(phi));
    double term3_0 = (15 * Math.pow(eSquare, 2) / 256 +
                      45 * Math.pow(eSquare, 3) / 1024) *
        Math.sin(4 * Math.toRadians(phi_0));

    double term4 = (35 * Math.pow(eSquare, 3) / 3072) *
        Math.sin(6 * Math.toRadians(phi));
    double term4_0 = (35 * Math.pow(eSquare, 3) / 3072) *
        Math.sin(6 * Math.toRadians(phi_0));

    double LargeSum = term1 - term2 + term3 - term4;
    double LargeSum_0 = term1_0 - term2_0 + term3_0 - term4_0;

    double M_0 = a * LargeSum_0;
    double M = a * LargeSum;

    double sum1 = 5 - T + 9 * C + 4 * C * C;
    double sum2 = 61 - 58 * T + T * T + 600 * C - 330 * eTickSquare;
    double sum3 = 1 - T + C;
    double sum4 = 5 - 18 * T + T * T + 72 * C - 58 * eTickSquare;

    double E = FE +
        k_0 * v * (A + sum3 * Math.pow(A, 3) / 6 + sum4 * Math.pow(A, 5) / 120);
    double N = FN +
        k_0 * (M - M_0 + v * Math.tan(Math.toRadians(phi)) * ( (A * A) / 2 +
        sum1 * Math.pow(A, 4) / 24 + sum2 * Math.pow(A, 6) / 720));

    /*System.out.println("eSquare is "+eSquare);
                 System.out.println("A is "+A);
                 System.out.println("T is "+T);
                 System.out.println("C is "+C);
                 System.out.println("M is "+M);
                 System.out.println("M_0 is "+M_0);
                 System.out.println("lamda is "+lamda);
                 System.out.println("phi is "+phi);
                 System.out.println("v is "+v);
                 System.out.println("k_0 is "+k_0);*/

//            Point2DDouble ret = new Point2DDouble(1);
//            ret.ptsDouble[0] = N;
//            ret.ptsDouble[1] = E;

//            return ret;
    return new double[] {
        E, N};
  }

  private double getESquared() {
    double ecc = _eccentricitySquared;

    // if the major and minor axes have been set
    if ( (_majorAxis != -1) && (_minorAxis != -1)) {
      double flattening = (_majorAxis - _minorAxis) / _majorAxis;
      ecc = 2 * flattening - flattening * flattening;
    }
    else {
      // if inverse flat exists
      if (_inverseFlat != -1) {
        ecc = (1 / _inverseFlat) * 2 -
            Math.pow( (1 / _inverseFlat), 2);
      }
      else {
        System.out.println("Couldn't find ellipsoid specification.");
        System.out.println("Guessing WGS84");

        // LAM -- this is exactly the same as the if() above!

        //geoinfo.SetEllipsoidName("WGS84");

        //ecc = (1 / geoinfo.GetInverseFlat()) * 2 -
        //      Math.pow((1 / geoinfo.GetInverseFlat()), 2);
        //geoinfo.SetEccentricitySqrd(ecc);
      }
    }
    return ecc;
  }

  /**
   * Set the UTM Zone.
   * @param z int the zone
   */
  public void setUTMZone(int z) {
    _zone = z;
  }

  /**
   * Set the UTM Zone.
   * @param z int the zone
   * @deprecated use setUTMZone(int z) instead
   */
  public void SetUTMZone(int z) {
    this.setUTMZone(z);
  }

  /**
   * Get the UTM zone.
   * @return int the UTM zone
   */
  public int getUTMZone() {
    return _zone;
  }

  /**
   * Get the UTM zone.
   * @return int the UTM zone
   * @deprecated use getUTMZone() instead
   */
  public int GetUTMZone() {
    return getUTMZone();
  }
  public String getProjectionParametersAsString() {
    String parametersAsString = new String();

    parametersAsString += (this.getType() + " = " + this.getClass() + "\n");
    parametersAsString += (_centerLatitude + " = center latitude\n");
    parametersAsString += (_centerLongitude + " = center longitude\n");
    parametersAsString += (_falseNorthing + " = false northing\n");
    parametersAsString += (_falseEasting + " = false easting\n");
    parametersAsString += (_scaleFactor + " = scale factor\n");
    parametersAsString += (_majorAxis + " = major axis\n");
    parametersAsString += (_minorAxis + " = minor axis\n");
    parametersAsString += (_inverseFlat + " = inverse flatness\n");
    parametersAsString += (_eccentricitySquared + " = eccentricity squared\n");
    parametersAsString += (_zone + " = zone\n");

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
    _centerLatitude = Double.parseDouble(valueAsString);

    indexOfNewline    = paramString.indexOf(magicNewline,indexOfDelimiter);
    indexOfDelimiter  = paramString.indexOf(magicString,indexOfNewline);
    valueAsString     = paramString.substring(indexOfNewline + 1,indexOfDelimiter);
    _centerLongitude = Double.parseDouble(valueAsString);

    indexOfNewline    = paramString.indexOf(magicNewline,indexOfDelimiter);
    indexOfDelimiter  = paramString.indexOf(magicString,indexOfNewline);
    valueAsString     = paramString.substring(indexOfNewline + 1,indexOfDelimiter);
    _falseNorthing = Double.parseDouble(valueAsString);

    indexOfNewline    = paramString.indexOf(magicNewline,indexOfDelimiter);
    indexOfDelimiter  = paramString.indexOf(magicString,indexOfNewline);
    valueAsString     = paramString.substring(indexOfNewline + 1,indexOfDelimiter);
    _falseEasting = Double.parseDouble(valueAsString);

    indexOfNewline    = paramString.indexOf(magicNewline,indexOfDelimiter);
    indexOfDelimiter  = paramString.indexOf(magicString,indexOfNewline);
    valueAsString     = paramString.substring(indexOfNewline + 1,indexOfDelimiter);
    _scaleFactor = Double.parseDouble(valueAsString);
  
    indexOfNewline    = paramString.indexOf(magicNewline,indexOfDelimiter);
    indexOfDelimiter  = paramString.indexOf(magicString,indexOfNewline);
    valueAsString     = paramString.substring(indexOfNewline + 1,indexOfDelimiter);
    _majorAxis = Double.parseDouble(valueAsString);
    
    indexOfNewline    = paramString.indexOf(magicNewline,indexOfDelimiter);
    indexOfDelimiter  = paramString.indexOf(magicString,indexOfNewline);
    valueAsString     = paramString.substring(indexOfNewline + 1,indexOfDelimiter);
    _minorAxis = Double.parseDouble(valueAsString);

    indexOfNewline    = paramString.indexOf(magicNewline,indexOfDelimiter);
    indexOfDelimiter  = paramString.indexOf(magicString,indexOfNewline);
    valueAsString     = paramString.substring(indexOfNewline + 1,indexOfDelimiter);
    _inverseFlat = Double.parseDouble(valueAsString);

    indexOfNewline    = paramString.indexOf(magicNewline,indexOfDelimiter);
    indexOfDelimiter  = paramString.indexOf(magicString,indexOfNewline);
    valueAsString     = paramString.substring(indexOfNewline + 1,indexOfDelimiter);
    _eccentricitySquared = Double.parseDouble(valueAsString);

    indexOfNewline    = paramString.indexOf(magicNewline,indexOfDelimiter);
    indexOfDelimiter  = paramString.indexOf(magicString,indexOfNewline);
    valueAsString     = paramString.substring(indexOfNewline + 1,indexOfDelimiter);
    _zone = Integer.parseInt(valueAsString);
  }

}
