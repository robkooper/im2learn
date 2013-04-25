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

import java.io.Serializable;

/**
 * @author yjlee
 */

public class Point2DFloat implements Serializable {
    protected int numpts;

    protected int maxValidPts;

    protected float[] ptsFloat = null;

    private boolean _debugPoint2DFloat;

    private static Log logger = LogFactory.getLog(Point2DFloat.class);

    //constructors
    public Point2DFloat() {
        resetPoint2DFloat();
    }

    public Point2DFloat(int numberPts) {
        resetPoint2DFloat();

        if (numberPts > 0) {
            numpts = numberPts;
            ptsFloat = new float[numpts << 1];
            maxValidPts = 0;
        }
    }

    //setters and getters
    public void setDebug(boolean flag) {
        _debugPoint2DFloat = flag;
    }

    public boolean getDebug() {
        return _debugPoint2DFloat;
    }

    public void resetPoint2DFloat() {
        _debugPoint2DFloat = true;// false;
        numpts = -1;
        ptsFloat = null;
        maxValidPts = 0;
    }

    /////////////////////////////////////////////////////
    // setters
    public void setPoint2DFloat(int numptsIn, float[] pts) {
        numpts = numptsIn;
        maxValidPts = numpts;
        ptsFloat = pts;
    }

    public boolean setValue(int idx, float valRow, float valCol) {
        if (idx < 0 || idx >= numpts)
            return false;

        ptsFloat[idx << 1] = valRow;
        ptsFloat[(idx << 1) + 1] = valCol;

        if (idx >= maxValidPts)
            maxValidPts = idx + 1;

        return true;
    }

    //////////////////////////////////////////////////////////////////////
    // Getters
    public float getValueRow(int idx) {
        if (idx < 0 || idx >= numpts) {
            logger.debug("Error: Row Point idx is out of bounds");

            return -1.0F;
        }

        return ptsFloat[idx << 1];
    }

    public float getValueCol(int idx) {
        if (idx < 0 || idx >= numpts) {
            logger.debug("Error: Col Point idx is out of bounds");

            return -1.0F;
        }

        return ptsFloat[(idx << 1) + 1];
    }

    public void getPoint2DFloatObject(Point2DFloat imo) {
        imo.numpts = numpts;
        imo.setDebug(_debugPoint2DFloat);
        imo.ptsFloat = ptsFloat;
        imo.maxValidPts = maxValidPts;
    }

    ///////////////////////////////////////////////////////////////////
    // Copy object
    //////////////////////////////////////////////////////////////////////////
    // this is the method that copies values
    private void copyPts(Point2DFloat inObject, Point2DFloat outObject) {
        if (outObject == null) {
            logger.debug("ERROR: output object is null");

            return;
        }

        int i;
        outObject.numpts = inObject.numpts;
        outObject.maxValidPts = inObject.maxValidPts;

        outObject.ptsFloat = new float[(inObject.numpts << 1)];

        for (i = 0; i < (inObject.numpts << 1); i++)
            outObject.ptsFloat[i] = inObject.ptsFloat[i];

        return;
    }

    public Point2DFloat copyPoint2DFloat(Point2DFloat inObject) {
        Point2DFloat outObject = new Point2DFloat();
        copyPts(inObject, outObject);
        outObject.setDebug(inObject.getDebug());

        return (outObject);
    }

    ////////////////////////////////////////////////////////////
    public boolean reverseOrder() {
        if (ptsFloat == null) {
            logger.debug("ERROR: missing array to store inserted values");

            return false;
        }

        int idx, idxOut;
        float temp;
        idx = 0;
        idxOut = (maxValidPts - 1) << 1;

        while (idx < idxOut) {
            temp = ptsFloat[idx];
            ptsFloat[idx] = ptsFloat[idxOut];
            ptsFloat[idxOut] = temp;

            temp = ptsFloat[idx + 1];
            ptsFloat[idx + 1] = ptsFloat[idxOut + 1];
            ptsFloat[idxOut + 1] = temp;

            idx += 2;
            idxOut -= 2;
        }

        return true;

    } ////////////////////////////////////////////////////////////

    public boolean insertValues(int idxInsert, Point2DFloat insertContour) {
        if (ptsFloat == null) {
            logger.debug("ERROR: missing array to store inserted values");

            return false;
        }

        if (insertContour == null) {
            logger.debug("ERROR: missing array to be inserted");

            return false;
        }

        if (idxInsert < 0 || idxInsert >= numpts) {
            logger.debug("ERROR: idxInsert is out of bounds");

            return false;
        }

        int maxIndex, idx, idxOut, i;

        maxIndex = maxValidPts - idxInsert;

        if (maxIndex < 0)
            maxIndex = 0;

        // shift and insert
        idxOut = (maxValidPts - 1) << 1;//(

        if (insertContour.maxValidPts > maxValidPts)
            idx = (insertContour.maxValidPts - 1 + maxIndex) << 1;
        else
            idx = (idxInsert + insertContour.maxValidPts - 1 + maxIndex) << 1;

        if (idx >= numpts << 1) {
            idxOut -= (idx - ((numpts - 1) << 1));
            idx = (numpts - 1) << 1;
        }

        for (i = 0; i < maxIndex && idxOut >= 0 && idx >= 0; i++) {
            ptsFloat[idx] = ptsFloat[idxOut];
            ptsFloat[idx + 1] = ptsFloat[idxOut + 1];

            idx -= 2;
            idxOut -= 2;
        }

        // insert values
        // check if the inserted size fits the current size
        if (insertContour.maxValidPts + idxInsert <= numpts) {
            maxIndex = insertContour.maxValidPts;
        } else {
            maxIndex = numpts - idxInsert;
        }

        idx = idxInsert << 1;
        idxOut = 0;
        for (i = idxInsert; i < idxInsert + maxIndex; i++) {
            ptsFloat[idx] = insertContour.ptsFloat[idxOut];
            ptsFloat[idx + 1] = insertContour.ptsFloat[idxOut + 1];
            idx += 2;
            idxOut += 2;
        }

        if (maxValidPts + insertContour.maxValidPts < numpts) {
            maxValidPts += insertContour.maxValidPts;
        } else {
            maxValidPts = numpts;
        }

        return true;
    }

    //////////////////////////////////////////////////////////////////
    //display values
    public void printPoint2DFloat() {
        logger.debug("Point2DFloat Info :numpts=" + numpts
                     + ", maxValidPts =" + maxValidPts);
        printPoint2DFloatAllValues();
    }

    public boolean printPoint2DFloatAllValues() {
        int i, idx;
        for (i = 0, idx = 0; i < numpts; i++, idx += 2) {
            logger.debug("pts[" + i + "]=(" + ptsFloat[idx] + ", "
                         + ptsFloat[idx + 1] + ") ");

            if (((int) ((i + 1) / 5)) * 5 == i + 1)
                logger.debug("\n");
        }

        logger.debug("\n");

        return true;
    }

    public boolean printPoint2DFloatValue(int idxPts) {

        int idx = idxPts << 1;
        if (idx < 0 || idx >= numpts << 1) {
            logger.debug("Error: Point is out of bounds");

            return false;
        }

        logger.debug("pts[" + idxPts + "]=(" + ptsFloat[idx] + ", "
                     + ptsFloat[idx + 1] + ") ");

        return true;
    }

    ////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////
    public String point2DFloat2String(int idxPts) {
        int idx = idxPts << 1;

        if (idx < 0 || idx >= numpts << 1) {
            logger.debug("Error: Point is out of bounds");

            return null;
        }

        String reString = "";//new String();
        reString += ptsFloat[idx] + ", " + ptsFloat[idx + 1] + "\n";

        return reString;
    }

    // takes the header pts information and converts it to string
    public String twoDPtsInfo2String() {
        String reString = "number of 2Dpts=" + numpts + ", maxValidPts="
                          + maxValidPts;//+"\ndata

        return reString;
    }

    /**
     * @return Returns the maxValidPts.
     */
    public int getMaxValidPts() {
        return maxValidPts;
    }

    /**
     * @param maxValidPts The maxValidPts to set.
     */
    public void setMaxValidPts(int maxValidPts) {
        this.maxValidPts = maxValidPts;
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
     * @return Returns the ptsFloat.
     */
    public float[] getPtsFloat() {
        return ptsFloat;
    }

    /**
     * @param ptsFloat The ptsFloat to set.
     */
    public void setPtsFloat(float[] ptsFloat) {
        this.ptsFloat = ptsFloat;
    }

    public void setPtsFloatItem(float f, int index) {
        this.ptsFloat[index] = f;
    }
}
