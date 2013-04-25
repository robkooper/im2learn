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
package edu.illinois.ncsa.isda.im2learn.core.datatype;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author yjlee
 *         <p/>
 *         TODO To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Style - Code Templates
 */
public class Point3DDouble {
    protected int numpts;

    /**
     * @deprecated use getter
     */
    public double[] ptsDouble = null;

    protected int size;

    private boolean _debugPoint3DDouble;

    private static Log logger = LogFactory.getLog(Point3DDouble.class);

    //constructors
    public Point3DDouble() {
        resetPoint3DDouble();
    }

    public Point3DDouble(int numberPts) {
        resetPoint3DDouble();

        if (numberPts > 0) {
            numpts = numberPts;
            size = numpts * 3;
            ptsDouble = new double[size];
        }
    }

    //setters and getters
    public void setDebug(boolean flag) {
        _debugPoint3DDouble = flag;
    }

    public boolean getDebug() {
        return _debugPoint3DDouble;
    }

    public void resetPoint3DDouble() {
        _debugPoint3DDouble = true;// false;
        numpts = -1;
        size = -1;
        ptsDouble = null;
    }

    // set byte image
    public void setPoint3DDouble(int numptsIn, double[] pts) {
        numpts = numptsIn;
        ptsDouble = pts;
        size = 3 * numptsIn;
    }

    //////////////////////////////////////////////////////////////////////
    // Getters
    public void getPoint3DDouble(Point3DDouble imo) {
        imo.numpts = numpts;
        imo.setDebug(_debugPoint3DDouble);
        imo.ptsDouble = ptsDouble;
        imo.size = size;
    }

    // Value Getters
    public double getValueRow(int idx) {
        if (idx < 0 || idx >= numpts) {
            logger.info("Error: Row (1st coord.) Point idx is out of bounds: "
                        + idx);
            return -1.0D;
        }
        return ptsDouble[idx * 3];
    }

    public double getValueCol(int idx) {
        if (idx < 0 || idx >= numpts) {
            logger.info("Error: Col (2nd coord.) Point idx is out of bounds: "
                        + idx + " when numpts= " + numpts);
            return -1.0D;
        }
        return ptsDouble[idx * 3 + 1];
    }

    public double getValueElev(int idx) {
        if (idx < 0 || idx >= numpts) {
            logger.info("Error: Elev (3rd coord.) Point idx is out of bounds: "
                        + idx + " when numpts= " + numpts);
            return -1.0D;
        }
        return ptsDouble[idx * 3 + 2];
    }

    ///////////////////////////////////////////////////////////////////
    // Copy object
    //////////////////////////////////////////////////////////////////////////
    // this is the method that copies values
    private void copyPts(Point3DDouble inObject, Point3DDouble outObject) {
        if (outObject == null) {
            logger.info("ERROR: output object is null");
            return;
        }
        int i;
        outObject.numpts = inObject.numpts;
        outObject.size = inObject.size;
        outObject.ptsDouble = new double[(inObject.size)];
        for (i = 0; i < size; i++)
            outObject.ptsDouble[i] = inObject.ptsDouble[i];

        return;
    }

    public Point3DDouble CopyPoint3DDouble(Point3DDouble inObject) {
        Point3DDouble outObject = new Point3DDouble();
        copyPts(inObject, outObject);
        outObject.setDebug(inObject.getDebug());
        return (outObject);
    }

    //display values
    public void printPoint3DDouble() {
        logger.info("Point3DDouble Info :numpts=" + numpts);
        printPoint3DDoubleAllValues();
    }

    public boolean printPoint3DDoubleAllValues() {
        int i, idx;
        for (i = 0, idx = 0; i < numpts; i++, idx += 3) {
            logger.info("pts[" + i + "]=(" + ptsDouble[idx] + ", "
                        + ptsDouble[idx + 1] + ", " + ptsDouble[idx + 2] + ") ");
            if (((int) ((i + 1) / 5)) * 5 == i + 1)
                logger.info("\n");
        }
        logger.info("\n");
        return true;
    }

    public boolean printPoint3DDoubleValue(int idxPts) {

        int idx = idxPts * 3;
        if (idx < 0 || idx >= size) {
            logger.info("Error: Point is out of bounds");
            return false;
        }
        logger.info("pts[" + idxPts + "]=(" + ptsDouble[idx] + ", "
                    + ptsDouble[idx + 1] + ", " + ptsDouble[idx + 2] + ") ");
        return true;
    }

    ////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////
    public String point3DDouble2String(int idxPts) {
        int idx = idxPts * 3;
        if (idx < 0 || idx >= size) {
            logger.info("Error: Point is out of bounds");
            return null;
        }
        String reString = "";//new String();
        reString += ptsDouble[idx] + ", " + ptsDouble[idx + 1] + ", "
                    + ptsDouble[idx + 2] + "\n";
        return reString;
    }

    // takes the header pts information and converts it to string
    public String point3DDoubleInfo2String() {
        String reString = "number of 3Dpts=" + numpts;
        return reString;
    }

    /**
     * @return Returns the numpts.
     */
    public int getNumpts() {
        return numpts;
    }

    /**
     * @param numpts The numpts to set.
     */
    public void setNumpts(int numpts) {
        this.numpts = numpts;
    }

    /**
     * @return Returns the ptsDouble.
     */
    public double[] getPtsDouble() {
        return ptsDouble;
    }

    /**
     * @param ptsDouble The ptsDouble to set.
     */
    public void setPtsDouble(double[] ptsDouble) {
        this.ptsDouble = ptsDouble;
    }

    /**
     * @return Returns the size.
     */
    public int getSize() {
        return size;
    }

    /**
     * @param size The size to set.
     */
    public void setSize(int size) {
        this.size = size;
    }
}
