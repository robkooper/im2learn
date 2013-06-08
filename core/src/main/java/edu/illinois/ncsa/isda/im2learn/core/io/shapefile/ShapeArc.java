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
/*
 * $Id: ShapeArc.java,v 1.4 2006-12-07 17:00:05 kooper Exp $
 *
 */

 /* TO DO: CHANGE THE IMPLEMENTATTION TO HAVE Point2DDouble instead of ShapePoint */


package edu.illinois.ncsa.isda.im2learn.core.io.shapefile;

import java.io.*;

import edu.illinois.ncsa.isda.im2learn.core.datatype.*;
import edu.illinois.ncsa.isda.im2learn.core.io.util.*;

/**
 * Wrapper for a Shapefile arc.
 * @author ssaha
 * @author pbajcsy
 * @author clutter
 * @version 2.0
 */
public class ShapeArc implements ShapefileShape, Serializable {

  protected double[] box = new double[4];
  protected int numParts;
  protected int[] parts;

  /**
   * stores point data, points are stored using the Point2DDouble methods
   * _points stores the number of points as well as the actual points.
   **/

  protected Point2DDouble _points; // numpts and maxValidPoints both have the same value

  protected ShapeArc(){}//For use by ShapePolygon

  /**
   * Read a ShapeArc from an LEDataInputStream
   * @param file
   * @throws IOException
   */
  public ShapeArc( LEDataInputStream file ) throws IOException {

    file.setLittleEndianMode(true);
    int shapeType = file.readInt();

    // read the bounding box
    for ( int i = 0; i<4; i++ ) {
      box[i] = file.readDouble();
    }

    // get the number of parts
    numParts = file.readInt();
    // get the number of points
    int numpts = file.readInt();

    // allocate space for the points
    _points = new Point2DDouble(numpts);
    _points.maxValidPts = _points.numpts;

    // allocate space for the parts
    parts = new int[numParts];

    // read in the parts
    for ( int i = 0; i < numParts; i++ ){
      parts[i]=file.readInt();
    }

    // read in the points
    for ( int i = 0; i < _points.numpts; i++ ) {
      double x = file.readDouble();
      double y = file.readDouble();
      _points.SetValue(i, y, x); // NOTE!!! the x co-ordinate is
				// written to Point2DDouble as Column value
    }
  }

  /**
   * Create a ShapeArc with the specified bounding box, parts, and points
   * @param box
   * @param parts
   * @param points
   */
  public ShapeArc(double[] box,int[] parts,Point2DDouble points){
    //When writing out the data for shape file,
    // the points.GetValueCol corresponds to the x value and,
    // the points.GetValueRow corresponds to the y value for a shapefile.
    this.box = box;
    this.parts = parts;
    this.numParts = parts.length;
    _points = new Point2DDouble(points.maxValidPts);
    _points.maxValidPts = points.maxValidPts;
    // test
    //System.out.println("In ShapeArc: _points.maxValidPoints: "+_points.maxValidPts+" points.maxValidPoints: "+points.maxValidPts);

    for(int i=0; i < (points.maxValidPts << 1); i++)
	_points.ptsDouble[i] = points.ptsDouble[i];
  }

  /**
   *
   * @param points
   * @return
   */
  public boolean resetPoints(double [] points){
    _points.numpts = (points.length)/2;
    _points.ptsDouble = points;
    return true;
  }

  protected void setPoint( int index, double y, double x ) {
    _points.SetValue(index, y, x);
  }

  protected double getX( int index ) {
    return _points.GetValueCol(index);
  }

  protected double getY( int index ) {
    return _points.GetValueRow(index);
  }

  /**
   * Write this ShapeArc out in the ESRI shapefile format.
   * @param file
   * @throws IOException
   */
  public void write(LEDataOutputStream file)throws IOException{
    file.setLittleEndianMode(true);
    // write the shape type
    file.writeInt(getShapeType());

    // write the bounding box
    for(int i = 0;i<4;i++){
      file.writeDouble(box[i]);
    }

    // write the number of parts
    file.writeInt(numParts);
    // write the number of points
    file.writeInt(_points.maxValidPts);
    // write out the parts
    for(int i = 0;i<numParts;i++){
      file.writeInt(parts[i]);
    }

    // write out the points
    for(int i = 0;i<_points.maxValidPts;i++){
      file.writeDouble(getX(i));
      file.writeDouble(getY(i));

    }
  }

  /**
   * Get the bounding box
   * @return
   */
   public double[] getBoundingBox(){
    return box;
   }

  /**
   * Find out how many parts make up this arc.
   * @return The number of parts in this arc
   **/
   public int getNumParts(){
    return numParts;
  }


  /**
   * Find out how many points make up the entire of this arc
   * @return The number of points in this arc
   */
  public int getNumPoints(){
    return _points.maxValidPts; // both maxValidPts and numPts have the same value.
  }

  /**
   * Gets an array of indexes to the start of each part in the point array
   * returned by getPoints.
   * @return array of indexs
   * @see #getPoints();
   */
  public int[] getParts(){
    return parts;
  }

  // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
  //MAY HAVE TO REMOVE/MODIFY EVERYTHING RELATED TO ShapePoint
  // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
   protected double[] getPoint( int index ) {
    if(index > _points.maxValidPts){
        System.err.println("Index: "+index+" out of bounds");
	return null;
    }
    double [] result = new double[2];
    result[0] = getX(index);
    result[1] = getY(index);
    return result;
  }

  /**
   * Returns ALL the points that make up this arc
   * @return Array of double
   */
  public double[] getPoints(){
    return _points.ptsDouble;
  }

  /**
   * Get all the points for a given part
   * a non-existent part returns null
   * @param part id of part,[first is 0]
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
   * Find the bounding box for this shape
   * @return double array in form {xMin,yMin,xMax,yMax}
   */
  public double[] getBounds(){
    return box;
  }

  /**
   * Get the type of shape stored (Shapefile.ARC)
   */
  public int getShapeType(){
    return ShapefileLoader.ARC;
  }

  public int getLength(){
    return ((44+(4*numParts)+(16*_points.maxValidPts))/2); //THIS LINE WAS ADDED AS THE ABOVE LINE SEEMS WRONG
					    // RETURNS LENGTH IN 16 bits WORD.
  }

}
