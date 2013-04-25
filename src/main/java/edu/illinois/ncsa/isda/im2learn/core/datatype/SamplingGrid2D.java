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
package edu.illinois.ncsa.isda.im2learn.core.datatype;

/**
 * A sampling grid for sampling pixels.
 * 
 * X coordinate is the horizontal coordinate (increases left to right).
 * Y coordinate is the vertical coordinate (increases top to bottom).
 * 
 * @author Yakov Keselman
 *
 */
public class SamplingGrid2D {
    
    /**
     * Create arrays from the following info.
     * @param xfirst the min value of the first coordinate.
     * @param xlast the max value of the first coordinate.
     * @param xstep the resolution of the first coordinate.
     * @param yfirst the min value of the second coordinate.
     * @param ylast the max value of the second coordinate.
     * @param ystep the resolution of the second coordinate.
     */
    public SamplingGrid2D( 
            double xfirst, double xlast, double xstep,
            double yfirst, double ylast, double ystep )
    {
    	int xsamples = (int)(Math.abs( xlast - xfirst )/xstep) + 1;
    	int ysamples = (int)(Math.abs( ylast - yfirst )/ystep) + 1;
        xcoord = new SamplingGrid1D( xfirst, xlast, xsamples );
        ycoord = new SamplingGrid1D( yfirst, ylast, ysamples );
    }
 
    
    /**
    * Create arrays from the following info.
    * @param xfirst the min value of the first coordinate.
    * @param xlast the max value of the first coordinate.
    * @param xsamples the number of samples along the first coordinate.
    * @param yfirst the min value of the second coordinate.
    * @param ylast the max value of the second coordinate.
    * @param ysamples the number of samples along the second coordinate.
    */
    public SamplingGrid2D(
    		double xfirst, double xlast, int xsamples,
    		double yfirst, double ylast, int ysamples )
    {
       	xcoord = new SamplingGrid1D( xfirst, xlast, xsamples );
       	ycoord = new SamplingGrid1D( yfirst, ylast, ysamples );
    }
    
    
    /**
     * @return the number of points along X coordinate.
     */
    public int xsize()
    { 
        return xcoord.size();
    }

    
    public double xRadius()
    {
    	return xcoord.getRadius();
    }
    
    
    /**
     * @return the number of points along Y coordinate.
     */
    public int ysize()
    { 
        return ycoord.size();
    }
    
    
    public double yRadius()
    {
    	return ycoord.getRadius();
    }
    
    
    /**
     * @return the number of points.
     */
    public int size()
    {
        return xsize()*ysize();
    }

    
    /**
     * @return the i-th point along X coordinate.
     */
    public double getX( int i )
    {
        return xcoord.get( i );
    }
    
    
    /**
     * @return the j-th point along Y coordinate.
     */
    public double getY( int j )
    {
        return ycoord.get( j );
    }
    
    
    /**
     * @return the i-th point along X coordinate and j-th point along Y coordinate.
     */
    public double[] getXY( int i, int j )
    {
        point[0] = xcoord.get(i);
        point[1] = ycoord.get(j);
        return point;
    }
    

    /**
     * X coordinates.
     */
    protected final SamplingGrid1D xcoord;
    
    /**
     * Y coordinates.
     */
    protected final SamplingGrid1D ycoord;
    
    
    /**
     * The point to return.
     */
    double[] point = new double[2];

}
