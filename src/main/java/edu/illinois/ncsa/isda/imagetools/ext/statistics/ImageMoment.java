package edu.illinois.ncsa.isda.imagetools.ext.statistics;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.imagetools.core.datatype.*;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;

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


public class ImageMoment {
  ImageObject _inImObj;
  double[][] _m;
  double[][] _centralMoment;
  public double _cx, _cy;

  static private Log logger = LogFactory.getLog(ImageMoment.class);
  
  public ImageMoment() {
  }

  public boolean SetImage(ImageObject imObj) {
    try{
      _inImObj = imObj;
      if (imObj.getNumBands() > 1) {
    	  logger.error("Set one-band image");
    	  return false;
      }
      init();
      calculateCentralMoments();
      return true;
    }
    catch(Exception e) {
      return false;
    }
  }

  
  private void init() {
	    _m = new double[4][4];
	    _centralMoment = new double[4][4];
	    for(int i=0; i<_m.length; i++) {
	      for(int j=0; j<_m[0].length; j++) {
	    _m[j][i] = _centralMoment[j][i] = Double.NaN;
	      }
	    }
	  }
  
  private double calculateMoment(ImageObject imObj, int degreeP, int degreeQ) {
	double retVal = 0;
    for(int j=0; j<imObj.getNumRows(); j++) {
      for(int i=0; i<imObj.getNumCols(); i++) {
        retVal += Math.pow((double)(i+1),degreeP)
          * Math.pow((double)(j+1),degreeQ)
          * imObj.getDouble(j,i,0); 
      }
    }
    return retVal;
  }

  private void calculateCentralMoments() {
    for(int i=0; i<_m.length; i++) {
      for(int j=0; j<_m[0].length; j++) {
        _m[i][j] = calculateMoment(_inImObj,i,j);
      }
    }


    _cx = _m[1][0]/_m[0][0];
    _cy = _m[0][1]/_m[0][0];

    _centralMoment[0][0] = _m[0][0];
    _centralMoment[1][0] = 0d;
    _centralMoment[0][1] = 0d;

    _centralMoment[1][1] = _m[1][1] - _cy * _m[1][0];

    _centralMoment[2][0] = _m[2][0] - _cx * _m[1][0];
    _centralMoment[0][2] = _m[0][2] - _cy * _m[0][1];

    _centralMoment[3][0] = _m[3][0] - 3 * _cx * _m[2][0] + 2 * _cx * _cx *_m[1][0];
    _centralMoment[0][3] = _m[0][3] - 3 * _cy * _m[0][2] + 2 * _cy * _cy *_m[0][1];

    _centralMoment[2][1] = _m[2][1] - 2 * _cx * _m[1][1] - _cy * _m[2][0] + 2 * _cx * _cx *_m[0][1];
    _centralMoment[1][2] = _m[1][2] - 2 * _cy * _m[1][1] - _cx * _m[0][2] + 2 * _cy * _cy *_m[1][0];


  }

  public double getCentralMoment(int p, int q) {
    try{
      return _centralMoment[p][q];
    }
    catch(Exception e) {
    return Double.NaN;
    }
  }

  public double getNormalizedCentralMoment(int p, int q) {
    try{
      return _centralMoment[p][q]/(Math.pow(_centralMoment[0][0],((double)p+(double)q)/2 + 1));
    }
    catch(Exception e) {
    return Double.NaN;
    }
  }


  public double[][] getCovarianceMatrix() {
    double[][] retArray = new double[2][2];

    retArray[0][0] = _centralMoment[2][0];
    retArray[0][1] = _centralMoment[1][1];
    retArray[1][0] = _centralMoment[1][1];
    retArray[1][1] = _centralMoment[0][2];

    return retArray;
  }


  public double getArea() {
    return _centralMoment[0][0];
  }

  public double varianceX() {
    return _centralMoment[2][0];
  }
  public double varianceY() {
    return _centralMoment[0][2];
  }
  public double coVarianceXY() {
    return _centralMoment[1][1]/_centralMoment[0][0];
  }
  public double[][] getPrincipalAxis() {
    /**
     *  Compute pricipal axis.
     * retArray[0] : the vector of major axis,
     * retArray[1] : the vector of minor axis.
     * retArrat[2][0]: largest eigen value
     * retArrat[2][1]: smallest eigen value
     */

    Matrix m = new Matrix(getCovarianceMatrix());
    EigenvalueDecomposition e = new EigenvalueDecomposition(m);
    double[][] retArray = new double[3][2];
    double[][] v = e.getV().getArray();
    double[][] d = e.getD().getArray();

    if(Math.abs(d[0][0])> Math.abs(d[1][1])) {
      retArray[0] = v[0];
      retArray[1] = v[1];
      retArray[2][0] = d[0][0];
      retArray[2][1] = d[1][1];
    }
    else {
      retArray[0] = v[1];
      retArray[1] = v[0];
      retArray[2][0] = d[1][1];
      retArray[2][1] = d[0][0];
    }

    return retArray;
  }

}

