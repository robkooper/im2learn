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
 * Created on Sep 7, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.illinois.ncsa.isda.im2learn.ext.segment;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author yjlee
 *         <p/>
 *         TODO To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Style - Code Templates
 */
public class EdgeLineRes {
    protected float x; // X coor. (rows)

    protected float y; // Y coor.(cols)

    protected float o; // offset

    protected float w; // width

    protected float s; // score (contrast)

    protected short p1, p2; // polarities

    protected float a; // ResultAngle; in degrees!!

    protected float h; // ResultHigh;

    private static Log logger = LogFactory.getLog(EdgeLineRes.class);

    // constructor
    public EdgeLineRes() {
    }

    public void setZero() {
        x = y = o = w = s = a = h = 0.0F;
        p1 = p2 = 0;
    }

    public void setValues(EdgeLineRes res) {
        x = res.x;
        y = res.y;
        o = res.o;
        w = res.w;
        s = res.s;
        p1 = res.p1;
        p2 = res.p2;
        a = res.a;
        h = res.h;
    }

    public void PrintEdgeLineRes() {
        logger.debug(" row=" + x + " col=" + y);
        logger.debug("offset=" + o + " width=" + w);
        logger.debug("score=" + s + " p1=" + p1 + " p2=" + p2);
        logger.debug("angle=" + a + " high=" + h);
    }

    /**
     * @return Returns the a.
     */
    public float getA() {
        return a;
    }

    /**
     * @param a The a to set.
     */
    public void setA(float a) {
        this.a = a;
    }

    /**
     * @return Returns the h.
     */
    public float getH() {
        return h;
    }

    /**
     * @param h The h to set.
     */
    public void setH(float h) {
        this.h = h;
    }

    /**
     * @return Returns the o.
     */
    public float getO() {
        return o;
    }

    /**
     * @param o The o to set.
     */
    public void setO(float o) {
        this.o = o;
    }

    /**
     * @return Returns the p1.
     */
    public short getP1() {
        return p1;
    }

    /**
     * @param p1 The p1 to set.
     */
    public void setP1(short p1) {
        this.p1 = p1;
    }

    /**
     * @return Returns the p2.
     */
    public short getP2() {
        return p2;
    }

    /**
     * @param p2 The p2 to set.
     */
    public void setP2(short p2) {
        this.p2 = p2;
    }

    /**
     * @return Returns the s.
     */
    public float getS() {
        return s;
    }

    /**
     * @param s The s to set.
     */
    public void setS(float s) {
        this.s = s;
    }

    /**
     * @return Returns the w.
     */
    public float getW() {
        return w;
    }

    /**
     * @param w The w to set.
     */
    public void setW(float w) {
        this.w = w;
    }

    /**
     * @return Returns the x.
     */
    public float getX() {
        return x;
    }

    /**
     * @param x The x to set.
     */
    public void setX(float x) {
        this.x = x;
    }

    /**
     * @return Returns the y.
     */
    public float getY() {
        return y;
    }

    /**
     * @param y The y to set.
     */
    public void setY(float y) {
        this.y = y;
    }
}

;
