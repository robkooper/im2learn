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
 * Wrapper for a Shapefile arc.
 * @TODO why use a Point2DDouble?  Why not just use a double[]?
 * @version 2.0
 * @author ssaha
 * @author pbajcsy
 * @author clutter
 */
public class ShapeArcM implements ShapefileShape,Serializable {
    // CHANGED ShapePoint to Point2DDouble
    protected double[] box = new double[4];
    protected int numParts;
    protected int[] parts;
    protected Point2DDouble _points;
    protected double[] mRange = new double[2];
    protected double[] mArray = null;

    protected ShapeArcM(){} //For use by ShapePolygon

    /**
     * Read in a ShapeArcM from the data stream.
     * @param file
     * @throws IOException
     */
    public ShapeArcM(LEDataInputStream file) throws IOException{

        file.setLittleEndianMode(true);
        // read the shape type.
        int shapeType = file.readInt();

        // read in the bounding box
        for(int i = 0;i<4;i++){
           box[i] = file.readDouble();
        }

        // read in the number of parts
	numParts = file.readInt();
        // read in the number of points
        int numpts = file.readInt();
	_points = new Point2DDouble(numpts);

        // allocate space for parts
        parts = new int[numParts];

        // allocate space for marray
        mArray = new double[_points.numpts];

        // read in the parts
        for(int i = 0;i<numParts;i++){
             parts[i]=file.readInt();
        }

        // read in the points
        for(int i = 0;i<_points.numpts;i++){
            double x = file.readDouble();
            double y = file.readDouble();

	    _points.SetValue(i, y, x); // NOTE!!! the x co-ordinate is
				// written to Point2DDouble as Column value
        }

        // measured data
        for(int i = 0;i<2;i++){
           mRange[i] = file.readDouble();
        }

        for(int i = 0;i<_points.numpts;i++){
           mArray[i] = file.readDouble();
        }
    }

    /**
     * Create a ShapeArcM with the specified values
     * @param box
     * @param parts
     * @param points
     */
    public ShapeArcM(double[] box,int[] parts,Point2DDouble points){
        this.box = box;
        this.parts = parts;
        this.numParts = parts.length;
	_points = new Point2DDouble(points.maxValidPts);
        _points.maxValidPts = points.maxValidPts;
	_points.ptsDouble = points.ptsDouble;
	// FIND THIS OUT!!!!!!!!
	// WHY IS mArray not handled here?

        /*
	for(int i=0; i < (points.maxValidPts << 1); i++)
	    _points.ptsDouble[i] = points.ptsDouble[i];
	*/
        //this.points = points;

    }

    /**
     * Write this ShapeArcM out in ESRI shapefile format.
     * @param file
     * @throws IOException
     */
    public void write(LEDataOutputStream file)throws IOException{

        file.setLittleEndianMode(true);
        // write the shape type
        file.writeInt(ShapefileLoader.ARC_M);
        // write out the bounding box
        for(int i = 0;i<4;i++){
           file.writeDouble(box[i]);
        }

        // write out the number of parts
        file.writeInt(numParts);
        // write out the number of points
        file.writeInt(_points.numpts);

        // write out the parts
        for(int i = 0;i<numParts;i++){
             file.writeInt(parts[i]);
        }

        // write out the points
        for(int i = 0;i<_points.numpts;i++){
	    file.writeDouble(getX(i));
	    file.writeDouble(getY(i));
	}

        // measured data
        for(int i = 0;i<2;i++){
           file.writeDouble(mRange[i]);
        }

        for(int i = 0;i<_points.numpts;i++){
           file.writeDouble(mArray[i]);
        }
    }

    public double[] getBoundingBox(){
	return box;
    }

    /**
     * Find out how many parts make up this arc.
     * @return The number of parts in this arc
     */
    public int getNumParts(){
        return numParts;
    }

    /**
     * Find out how many points make up the entire of this arc
     * @return The number of points in this arc
     */
    public int getNumPoints(){
        return _points.numpts;
    }

    /**
     * Get a copy of ALL the points that make up this arc
     * @return Array of double
     */
    public double[] getPoints(){
        return _points.ptsDouble;
    }

    public boolean resetPoints(double[] points){
	_points.numpts = (points.length)/2;
        _points.ptsDouble = points;
	return true;
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

    protected double getX( int index ) {
    return _points.GetValueCol(index);
  }

  protected double getY( int index ) {
    return _points.GetValueRow(index);
  }

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
     * Get all the points for a given part<p>
     * a non-existent part returns <b>null</b> (would you prefer an exception?)
     * @param part id of part,[first is 0]
     */
     public double[] getPartPoints(int part){
	if(part>numParts-1){
	    return null;
	}
	int start,finish,length;

        start = parts[part];
        if(part == numParts-1){
	    finish = _points.numpts;
	}
        else
        {
            finish=parts[part+1];
        }
        length = finish-start;
        double[] partPoints = new double[length<<1];
	int j = 0;
        for(int i =0;i<(length<<1);i++){
            partPoints[j]= _points.ptsDouble[i+start];
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
        return ShapefileLoader.ARC_M;
    }

    public int getLength(){
        return ((44+(4*numParts)+16+(16*_points.numpts))/2); //CHECK THIS!!!!!!!!!!!!
    }

    /**
     * Get the measured data
     */

    public double[] getRange(){
      return mRange;
    }

    public double[] getMeasures(){
      return mArray;
    }
}
