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
 * Created on Sep 3, 2004
 *
 */
package edu.illinois.ncsa.isda.im2learn.core.datatype;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author yjlee
 */

public class ImLine {
    protected ImPoint pts1 = null;

    protected ImPoint pts2 = null;

    protected double slope;

    protected double q;

    private double _minPtsSepar = 1.0;

    private double _maxAllowedSlope = LimitValues.SLOPE_MAX;

    private static Log logger = LogFactory.getLog(ImLine.class);

    //constructor
    public ImLine() {
        pts1 = new ImPoint();
        pts2 = new ImPoint();
        slope = 0.0;
        q = 0.0;
    }

    public ImLine(ImPoint ptsIn1, ImPoint ptsIn2) {
        pts1 = new ImPoint(ptsIn1);
        pts2 = new ImPoint(ptsIn2);
        computeSlopeFromPts();
    }

    //////////////////////////////////////////////////////////////
    // computes slope and offset of a line given two points
    //////////////////////////////////////////////////////////////
    public boolean computeSlopeFromPts() {
        if (pts1 == null || pts2 == null) {
            logger.error("Error: points have not been defined");

            return false;
        }

        if (Math.abs(pts2.x - pts1.x) < _minPtsSepar) {
            if (Math.abs(pts2.y - pts1.y) < _minPtsSepar) {
                slope = 0.0;
                q = 0.0;

                return false;
            } else {
                slope = _maxAllowedSlope;//_lim.SLOPE_MAX;
                q = pts1.x;
            }
        } else {
            if (Math.abs(pts2.y - pts1.y) < _minPtsSepar) {
                slope = 0.0;
                q = pts1.y;
            } else {
                slope = (pts2.y - pts1.y) / (pts2.x - pts1.x);
                q = pts1.y - slope * pts1.x;
            }
        }

        return true;
    }

    /////////////////////////////////////////////////////////////////////////////
    // compute all line points given two end points
    /////////////////////////////////////////////////////////////////////////////
    public boolean LinePoints(ImLine line, ImPoint[] pts, int numpix) {
        double x, y, x1, y1, x2, y2, deltax;
        int i;

        x1 = line.pts1.x;
        y1 = line.pts1.y;
        x2 = line.pts2.x;
        y2 = line.pts2.y;

        if (x1 == x2 && y1 == y2) {
            logger.error("Error: two identical points cannot create a line \n");

            return false;
        }
        if (!computeSlopeFromPts()) {
            return false;
        }

        //if(Math.abs( line.slope) < _lim.SLOPE_MAX){
        if (Math.abs(line.slope) < _maxAllowedSlope) {
            deltax = (x2 - x1) / (float) numpix;

            if (Math.abs(deltax) < LimitValues.EPSILON3) { // 0.0001
                logger.error(" Error: slope\n");

                return false;
            }

            for (i = 0; i < numpix; i++) {
                x = x1 + deltax * i;
                pts[i].x = x;
                pts[i].y = line.slope * x + line.q;
            }
        } else {
            deltax = (y2 - y1) / (float) numpix;
            if (Math.abs(deltax) < LimitValues.EPSILON3) {//0.0001
                logger.error(" Error: slope\n");

                return false;
            }

            for (i = 0; i < numpix; i++) {
                y = y1 + deltax * i;
                pts[i].x = line.q;
                pts[i].y = y;
            }
        }

        return true;
    } // end of LinePoints

    ///////////////////////////////////////////////////////////////////////
    // this method is used for contour computation
    /////////////////////////////////////////////////////////////////////////////
    /*
     * public boolean LinePoints(Point2DInt pts,int begin, int numpix){ return
     * LinePoints(this,pts,begin,numpix); }
     * /////////////////////////////////////////////////////////////////////////////
     * public boolean LinePoints(ImLine line, Point2DInt pts,int begin, int
     * numpix) { double x,y,x1,y1,x2,y2,deltax; int i;
     *
     * x1=line.pts1.x; y1=line.pts1.y; x2=line.pts2.x; y2=line.pts2.y; if(x1==x2 &&
     * y1==y2){ System.out.println("Error: two identical points cannot create a
     * line \n"); return false; } if( !ComputeSlopeFromPts() ) return false;
     *  // d = sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2)); //numpix = (int)
     * (d+0.5); // numpix should never be zero
     *
     * if(Math.abs( line.slope) < _lim.SLOPE_MAX){ deltax =
     * (x2-x1)/(float)numpix; if( Math.abs(deltax) < _lim.EPSILON3){ // 0.0001
     * System.out.println(" Error: slope\n"); return false; } for(i=begin;i
     * <begin+numpix;i++){ x = x1 + deltax*(i-begin); y = line.slope * x +
     * line.q; pts.SetValue(i,(int)( x +0.5),(int)(y +0.5) ); } }else{ deltax =
     * (y2-y1)/(float)numpix; if( Math.abs(deltax) < _lim.EPSILON3){//0.0001
     * System.out.println(" Error: slope\n"); return false; } for(i=begin;i
     * <begin+numpix;i++){ y = y1 + deltax*(i-begin); pts.SetValue(i,(int)(
     * line.q +0.5),(int)(y +0.5) ); } }
     *
     * return true; } // end of LinePoints
     */
    ////////////////////////////////////////////////////////////////////
    //setters
    public boolean setImLine(ImPoint ptsIn1, ImPoint ptsIn2) {
        pts1.SetImPoint(ptsIn1);
        pts2.SetImPoint(ptsIn2);

        return (computeSlopeFromPts());
    }

    public boolean setMinPtsSepar(double val) {
        if (val <= 0.0) {
            logger.error("ERROR: Min point separation must be positive");

            return false;
        }
        _minPtsSepar = val;

        return true;
    }

    public boolean setMaxAllowedSlope(double val) {
        if (val <= 0.0) {
            logger.error("ERROR: Max allowed slope should correspond to an absolute value");

            return false;
        }

        _maxAllowedSlope = val;

        return true;
    }

    public void printImLine() {
        logger.info("ImLine: slope=" + slope + ", q=" + q);

        pts1.printImPoint();
        pts2.printImPoint();
    }

    /**
     * @return Returns the slope.
     */
    public double getSlope() {
        return slope;
    }

    /**
     * @param slope The slope to set.
     */
    public void setSlope(double slope) {
        this.slope = slope;
    }

    /**
     * @return Returns the q.
     */
    public double getQ() {
        return q;
    }

    /**
     * @param q The q to set.
     */
    public void setQ(double q) {
        this.q = q;
    }

    /**
     * @return Returns the pts1.
     */
    public ImPoint getPts1() {
        return pts1;
    }

    /**
     * @param pts1 The pts1 to set.
     */
    public void setPts1(ImPoint pts1) {
        this.pts1 = pts1;
    }

    /**
     * @return Returns the pts2.
     */
    public ImPoint getPts2() {
        return pts2;
    }

    /**
     * @param pts2 The pts2 to set.
     */
    public void setPts2(ImPoint pts2) {
        this.pts2 = pts2;
    }
}//end of class ImLine
