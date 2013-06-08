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

import edu.illinois.ncsa.isda.im2learn.core.io.util.*;

/**
 * Wrapper for a Shapefile point.
 * @author ssaha
 * @author pbajcsy
 * @author clutter
 * @version 2.0
 */
public class ShapePoint /*extends GeoPoint */ implements ShapefileShape,Serializable  {
    protected double x,y;  //un coment to remove GeoTools dependence

    /**
     * Read in a shape point from an LEDataInputStream
     * @param file
     * @throws IOException
     */
    public ShapePoint(LEDataInputStream file) throws IOException{
        file.setLittleEndianMode(true);
        int shapeType = file.readInt();
        x = file.readDouble();
        y = file.readDouble();
    }


    /**
     * Create a new point from x,y values
     */
    public ShapePoint(double x,double y){
        this.x = x;
        this.y = y;
    }

    /**
     * Create a new point from an existing one
     * @param p The existing point
     */
    public ShapePoint(ShapePoint p){
        this.x = p.x;
        this.y = p.y;
    }

    /**
     * Write this ShapePoint out.
     * @param file
     * @throws IOException
     */
    public void write(LEDataOutputStream file) throws IOException{
        file.setLittleEndianMode(true);
        // write out the shape type
        file.writeInt(ShapefileLoader.POINT);
        // write out the x coord
        file.writeDouble(x);
        // write out the y coord
        file.writeDouble(y);
    }


    public double[] getBoundingBox(){
	double [] box = new double[4];
	box[0] = x;
	box[1] = y;
	box[2] = x;
	box[3] = y;
	return box;
    }

    /**
     * Return the x value of this point.
     * @return The x value
     */
    public double getX(){
        return x;
    }

    /**
     * Return the y value of this point.
     * @return The y value
     */
    public double getY(){
        return y;
    }

    public double[] getPoints(){
	// point2DDouble inserts y first and then x as column.
	// All other ShapefileShape implementations use Point2DDouble.
	// So this will have the x and y points of all shapes in the same order.
	double[] points = new double[2];
	points[0] = getY();
	points[1] = getX();
	return points;
    }

    public int getNumParts(){
	return 0; //ShapePoint has just one part
    }

    public int[] getParts(){
	return null;
    }

    public double[] getPartPoints(int part){
        return null;
    }
    /**
     * Return this point as an array.
     * @return double[2] in the form {x,y}
     */
    public double[] getPoint(){
        double[] d = {x,y};
        return d;
    }

    public boolean resetPoints(double [] points){
	x = points[1];
	y = points[0];
	return true;
    }

    public String toString(){
        return(x+","+y);
    }

    /**
     * Returns the shapefile shape type value for a point
     * @return int Shapefile.POINT
     */
    public int getShapeType(){
        return ShapefileLoader.POINT;
    }

    public int getNumPoints(){
	return 1; // A ShapePoint describes only one point.
    }

    public int getLength(){
        return 10;//the length of two doubles in 16bit words + the shapeType
    }

}
