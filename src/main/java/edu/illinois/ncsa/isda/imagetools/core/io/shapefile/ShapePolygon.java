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
package edu.illinois.ncsa.isda.imagetools.core.io.shapefile;

import java.io.*;

import edu.illinois.ncsa.isda.imagetools.core.datatype.*;
import edu.illinois.ncsa.isda.imagetools.core.io.util.*;

/**
 * Wrapper for a Shapefile polygon.
 * @author ssaha
 * @author pbajcsy
 * @author clutter
 * @version 2.0
 */
public class ShapePolygon extends ShapeArc implements Serializable {

  public ShapePolygon( LEDataInputStream file )
    throws IOException, InvalidShapefileException {

    file.setLittleEndianMode(true);
    int shapeType = file.readInt();

    if ( shapeType != ShapefileLoader.POLYGON ) {
      throw new InvalidShapefileException
        ("Error: Attempt to load non polygon shape as polygon.");
    }

    for ( int i = 0; i<4; i++ ) {
      box[i] = file.readDouble();
      //if(DEBUG)
	// System.out.println("Bounding box for a boundary is: "+box[i]);
    }

    numParts = file.readInt();
    int numpts = file.readInt();
    _points = new Point2DDouble(numpts);
    _points.maxValidPts = _points.numpts;

    parts = new int[numParts];

    for(int i = 0;i<numParts;i++){
      parts[i]=file.readInt();
    }

    for ( int i = 0; i<_points.numpts; i++ ) {
      double x = file.readDouble(); //set x as column
      double y = file.readDouble(); //set y as row
      //if(DEBUG)
	//System.out.println("Points for a boundary, col =  "+x+"row = "+y);

      setPoint( i, y, x); // Point2DDouble sets row first and then column
    }
  }

  public ShapePolygon(double[] box,int[] parts,Point2DDouble points){
    super( box, parts, points);
  }

  public int getShapeType(){
    return ShapefileLoader.POLYGON;
  }

  public int getNumPoints(){
    return super.getNumPoints();
  }

  public double[] getPoints(){
    return _points.ptsDouble;
  }
  //public boolean SetPoints

  public double[] getPartPoints(int part){
    return super.getPartPoints(part);
  }

  public int getNumParts(){
    return super.getNumParts();
  }

  public int[] getParts(){
    return super.getParts();
  }

  public int getLength(){
    // Returns the length in 16 bits WORDS
    return (22+(2*numParts)+_points.numpts*8);// This is 8, as each Point has 2 doubles
  }
}
