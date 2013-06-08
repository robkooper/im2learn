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
public class ComplexNum {
    private static Log logger = LogFactory.getLog(ComplexNum.class);

    private boolean _debugComplexNum;

    protected double[] ptsDouble = new double[2];

    //constructors
    public ComplexNum() {
        resetComplexNum();
    }

    public ComplexNum(double real, double imag) {
        ptsDouble[0] = real;
        ptsDouble[1] = imag;
    }


    //setters and getters
    public void setDebug(boolean flag) {
        _debugComplexNum = flag;
    }

    public boolean getDebug() {
        return _debugComplexNum;
    }

    public void resetComplexNum() {
        ptsDouble[0] = 0.0;
        ptsDouble[1] = 0.0;
    }

    // set byte image
    public void setComplexNum(double real, double imag) {
        ptsDouble[0] = real;
        ptsDouble[1] = imag;
    }

    //////////////////////////////////////////////////////////////////////
    // Getters
    public void getComplexNum(ComplexNum imo) {
        imo.ptsDouble = ptsDouble;
    }

    public double getReal() {
        return ptsDouble[0];
    }

    public double getImag() {
        return ptsDouble[1];
    }

    ////////////////////////////
    public ComplexNum copyComplexNum() {
        ComplexNum imo = new ComplexNum(ptsDouble[0], ptsDouble[1]);
        return imo;
    }

    //display values
    public boolean printComplexNumValue() {
        logger.debug("complexNum[real=" + ptsDouble[0] + ", imag="
                     + ptsDouble[1] + "] ");

        return true;
    }

    ////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////
    public String complexNum2String() {
        String reString = "";//new String();
        reString += ptsDouble[0] + ", " + ptsDouble[1] + "\n";

        return reString;
    }
}
