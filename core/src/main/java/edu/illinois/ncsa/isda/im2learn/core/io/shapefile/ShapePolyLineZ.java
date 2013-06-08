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
package edu.illinois.ncsa.isda.im2learn.core.io.shapefile;

import java.io.*;

import edu.illinois.ncsa.isda.im2learn.core.datatype.*;
import edu.illinois.ncsa.isda.im2learn.core.io.util.*;

/**
 * A representation of a PolyLineZ shape in a Shapefile.
 * @author clutter
 * @version 2.0
 */
public class ShapePolyLineZ implements ShapefileShape {
  /** the bounding box */
  protected double[] box;
  /** the number of parts */
  protected int numParts;
  /** the number of points */
  protected int numPoints;
  /** the parts */
  protected int[] parts;
  /** the points */
  protected Point2DDouble _points;
  /** the zrange */
  protected double[] zrange;
  /** the zarray */
  protected double[] zarray;
  /** the mrange */
  protected double[] mrange;
  /** the marray */
  protected double[] marray;

  /**
   * Read in a ShapePolyLineZ
   * @param inputStream the input stream to read from
   * @throws IOException
   * @throws InvalidShapefileException
   */
  public ShapePolyLineZ(LEDataInputStream inputStream) throws IOException, InvalidShapefileException {
    inputStream.setLittleEndianMode(true);

    // read in the type
    int shapeType = inputStream.readInt();

    // make sure it is a PolyLineZ
    if(shapeType != ShapefileLoader.POLY_LINE_Z)
      throw new InvalidShapefileException
        ("Error: Attempt to load non PolyLineZ shape as PolyLineZ.");

    // read in the bounding box
    box = new double[4];
    for ( int i = 0; i < 4; i++ ) {
      box[i] = inputStream.readDouble();
    }

    // read the number of parts
    numParts = inputStream.readInt();

    // read the number of points
    numPoints = inputStream.readInt();

    parts = new int[numParts];
    // read in the parts
    for(int i = 0; i < numParts; i++) {
      parts[i] = inputStream.readInt();
    }

    _points = new Point2DDouble(numPoints);
    // read in the points
    for(int i = 0; i < numPoints; i++) {
      double x = inputStream.readDouble();
      double y = inputStream.readDouble();
      _points.SetValue(i, y, x);
    }

    // zrange
    zrange = new double[2];
    zrange[0] = inputStream.readDouble();
    zrange[1] = inputStream.readDouble();

    // zarray
    zarray = new double[numPoints];
    for(int i = 0; i < numPoints; i++) {
      zarray[i] = inputStream.readDouble();
    }

    // mrange
    mrange = new double[2];
    mrange[0] = inputStream.readDouble();
    mrange[1] = inputStream.readDouble();

    // marray
    marray = new double[numPoints];
    for(int i = 0; i < numPoints; i++) {
      marray[i] = inputStream.readDouble();
    }
  }

  /**
   * Create a ShapePolyLineZ with the specified values
   * @param box
   * @param parts
   * @param points
   */
  public ShapePolyLineZ(double[] box,int[] parts,Point2DDouble points){
      this.box = box;
      this.parts = parts;
      this.numParts = parts.length;
      _points = new Point2DDouble(points.maxValidPts);
      _points.maxValidPts = points.maxValidPts;
      _points.ptsDouble = points.ptsDouble;

      // why are mrange, zrange, marray, mrange not handled??
  }

  /**
   * return the type of shape
   * @return the type of shape, defined in Shapefile
   * @see Shapefile
   */
  public int getShapeType() {
    return ShapefileLoader.POLY_LINE_Z;
  }

  /**
   * get the number of points
   * @return the number of points
   */
  public int getNumPoints() {
    return numPoints;
  }

  /**
   * get the number of parts
   * @return the number of parts
   */
  public int getNumParts() {
    return numParts;
  }

  /**
   */
  public int[] getParts() {
    return parts;
  }

  public double[] getPoints() {
    return _points.ptsDouble;
  }

  /**
   * get the points for a part
   * @param part the part
   * @return the points for this part
   */
  public double[] getPartPoints(int part) {
    if(part>numParts-1){
        System.err.println("Index for parts out of bound!!");
        return null;
    }

    int start,finish,length;
    start = parts[part];
    if(part == numParts-1){
        finish = _points.maxValidPts;
    }
    else{
        finish=parts[part+1];
    }
    length = finish-start;
    double [] partPoints = new double[length<<1];
    int j =0;
    for(int i =0;i<(length<<1);i++){
      partPoints[j] = _points.ptsDouble[i+start];
      j++;
    }
    return partPoints;
  }

  /**
   * get the boundary box
   * @return the boundary box
   */
  public double[] getBoundingBox() {
    return box;
  }

  // LAM -- what is this supposed to do?? There is no documentation anywhere
  public boolean resetPoints(double[] points) {
    _points.numpts = (points.length)/2;
    _points.ptsDouble = points;
    return true;
  }

  /**
   * Save out this shapefile
   * @param file
   * @throws java.io.IOException
   */
  public void write(LEDataOutputStream file) throws java.io.IOException {
    file.setLittleEndianMode(true);

    file.writeInt(getShapeType());

    // write out the bounding box
    for ( int i = 0; i < 4; i++ ) {
      file.writeDouble(box[i]);
    }

    // write the number of parts
    file.writeInt(getNumParts());

    // write the number of points
    file.writeInt(getNumPoints());

    // write out the parts
    for(int i = 0; i < numParts; i++) {
      file.writeInt(parts[i]);
    }

    // write out the points
    for(int i = 0; i < numPoints; i++) {
      //double x = inputStream.readDouble();
      //double y = inputStream.readDouble();
      //_points.SetValue(i, y, x);

      file.writeDouble(_points.GetValueCol(i));
      file.writeDouble(_points.GetValueRow(i));
    }

    // zrange
    file.writeDouble(zrange[0]);
    file.writeDouble(zrange[1]);

    // zarray
    for(int i = 0; i < numPoints; i++) {
      file.writeDouble(zarray[i]);
    }

    // mrange
    file.writeDouble(mrange[0]);
    file.writeDouble(mrange[1]);

    // marray
    for(int i = 0; i < numPoints; i++) {
      //marray[i] = inputStream.readDouble();
      file.writeDouble(marray[i]);
    }

  }

  // length in 16bit words
  public int getLength() {
    // count up the total number of bytes.  Then later convert to 16 bit words

    // there are 44 bytes always in the header
    int numBytes = 44;

    // one 4-byte integer is stored for each Part
    numBytes += 4 * getNumParts();

    // two 8-byte doubles are stored for each Point
    numBytes += 2 * 8 * getNumPoints();

    // two 8-byte doubles for zrange
    numBytes += 8 * 2;

    // one 8-byte double is stored for each entry in zarray
    numBytes += 8 * getNumPoints();

    // two doubles for mrange
    numBytes += 8 * 2;

    // an 8-byte double is stored for each entry in marray
    numBytes += 8 * getNumPoints();

    // now we have the total in bytes.  there are 2 bytes in a 16-bit word.
    // so divide the total by 2.

    return numBytes/2;
  }

}
