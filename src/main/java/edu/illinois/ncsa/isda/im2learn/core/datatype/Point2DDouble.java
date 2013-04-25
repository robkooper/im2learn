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

/*
 * Point2DDouble.java
 *
 */

/**
 * @author Peter Bajcsy
 * @version 1.0
 */

// this is the class that holds 2D spatial points of integer type, for example, contours

public class Point2DDouble implements java.io.Serializable {
	private static final long serialVersionUID = -4700184300042588089L;

	public int numpts;
    public int maxValidPts;
    public double[] ptsDouble = null;

    private boolean _debugPoint2DDouble;

    //constructors
    public Point2DDouble() {
        ResetPoint2DDouble();
    }

    public Point2DDouble(int numberPts) {
        ResetPoint2DDouble();
        if (numberPts > 0) {
            numpts = numberPts;
            ptsDouble = new double[numpts << 1];
            maxValidPts = 0;
        }
    }

    //setters and getters
    public void SetDebug(boolean flag) {
        _debugPoint2DDouble = flag;
    }

    public boolean GetDebug() {
        return _debugPoint2DDouble;
    }

    public void ResetPoint2DDouble() {
        _debugPoint2DDouble = true;// false;
        numpts = -1;
        ptsDouble = null;
        maxValidPts = 0;
    }

    /////////////////////////////////////////////////////
    // setters
    public void SetPoint2DDouble(int numptsIn, double[] pts) {
        numpts = numptsIn;
        maxValidPts = numpts;
        ptsDouble = pts;
    }

    public boolean SetValue(int idx, double valRow, double valCol) {
        if (idx < 0 || idx >= numpts)
            return false;
        ptsDouble[idx << 1] = valRow;
        ptsDouble[(idx << 1) + 1] = valCol;
        if (idx >= maxValidPts)
            maxValidPts = idx + 1;
        return true;
    }

    //////////////////////////////////////////////////////////////////////
    // Getters
    public double GetValueRow(int idx) {
        if (idx < 0 || idx >= numpts) {
            System.out.println("Error: Row Point idx is out of bounds: " + idx);
            return -1.0D;
        }
        return ptsDouble[idx << 1];
    }

    public double GetValueCol(int idx) {
        if (idx < 0 || idx >= numpts) {
            System.out.println("Error: Col Point idx is out of bounds: " + idx + " when numpts= " + numpts);
            return -1.0D;
        }
        return ptsDouble[(idx << 1) + 1];
    }

    public void GetPoint2DDoubleObject(Point2DDouble imo) {
        imo.numpts = numpts;
        imo.SetDebug(_debugPoint2DDouble);
        imo.ptsDouble = ptsDouble;
        imo.maxValidPts = maxValidPts;
    }

    ///////////////////////////////////////////////////////////////////
    // Copy object
    //////////////////////////////////////////////////////////////////////////
    // this is the method that copies values
    private void CopyPts(Point2DDouble inObject, Point2DDouble outObject) {
        if (outObject == null) {
            System.out.println("ERROR: output object is null");
            return;
        }
        int i;
        outObject.numpts = inObject.numpts;
        outObject.maxValidPts = inObject.maxValidPts;

        outObject.ptsDouble = new double[(inObject.numpts << 1)];
        for (i = 0; i < (inObject.numpts << 1); i++)
            outObject.ptsDouble[i] = inObject.ptsDouble[i];

        return;
    }

    public Point2DDouble CopyPoint2DDouble(Point2DDouble inObject) {
        Point2DDouble outObject = new Point2DDouble();
        CopyPts(inObject, outObject);
        outObject.SetDebug(inObject.GetDebug());
        return (outObject);
    }

    ////////////////////////////////////////////////////////////
    public boolean ReverseOrder() {
        if (ptsDouble == null) {
            System.out.println("ERROR: missing array to store inserted values");
            return false;
        }

        int idx, idxOut;
        double temp;
        idx = 0;
        idxOut = (maxValidPts - 1) << 1;
        //System.out.println("TEST: maxValidPts="+maxValidPts);
        //System.out.println("TEST: idx="+idx+",idxOut="+idxOut);

        while (idx < idxOut) {
            temp = ptsDouble[idx];
            ptsDouble[idx] = ptsDouble[idxOut];
            ptsDouble[idxOut] = temp;

            temp = ptsDouble[idx + 1];
            ptsDouble[idx + 1] = ptsDouble[idxOut + 1];
            ptsDouble[idxOut + 1] = temp;

            idx += 2;
            idxOut -= 2;
        }
        return true;

    }  ////////////////////////////////////////////////////////////

    public boolean InsertValues(int idxInsert, Point2DDouble insertContour) {
        if (ptsDouble == null) {
            System.out.println("ERROR: missing array to store inserted values");
            return false;
        }
        if (insertContour == null) {
            System.out.println("ERROR: missing array to be inserted");
            return false;
        }
        if (idxInsert < 0 || idxInsert >= numpts) {
            System.out.println("ERROR: idxInsert is out of bounds");
            return false;
        }

        int maxIndex, idx, idxOut, i;
        //test
        //System.out.println("TEST: maxValidPts="+maxValidPts+",insertC.maxValidPts="+insertContour.maxValidPts);
        maxIndex = maxValidPts - idxInsert;
        if (maxIndex < 0)
            maxIndex = 0;

        //System.out.println("TEST: idxInsert="+idxInsert+",maxIndex="+maxIndex);

        // shift and insert
        idxOut = (maxValidPts - 1) << 1;//( (idxInsert+insertContour.maxValidPts) + maxValidPts-(idxInsert + insertContour.maxValidPts) )<<1;
        //idx = (idxInsert + insertContour.maxValidPts-1 + maxIndex)<< 1;

        if (insertContour.maxValidPts > maxValidPts)
            idx = (insertContour.maxValidPts - 1 + maxIndex) << 1;
        else
            idx = (idxInsert + insertContour.maxValidPts - 1 + maxIndex) << 1;

        if (idx >= numpts << 1) {
            //System.out.println("TEST: fix idx="+idx+",idxOut="+idxOut);
            idxOut -= (idx - ((numpts - 1) << 1));
            idx = (numpts - 1) << 1;
            //maxIndex -= ( (idx>>1) - (numpts-1) );
        }

        //System.out.println("TEST: idx="+idx+",idxOut="+idxOut+",maxIndex="+maxIndex);
        /*
        if ( idxInsert+insertContour.maxValidPts != (idx>>1) - maxIndex ){
           System.out.println("TEST: idxInsert="+idxInsert+",insertContour.maxValidPts="+insertContour.maxValidPts);
           System.out.println("TEST: to be inserted="+(idxInsert+insertContour.maxValidPts)+ ",idx>>1="+( (idx>>1) - maxIndex) );
        }
        // test
        System.out.println("TEST: check before shift pts["+idxInsert+"]="+GetValueRow(idxInsert)+","+GetValueCol(idxInsert) );
        System.out.println("TEST: check before shift pts["+(idxInsert+insertContour.maxValidPts-1) +"]="+GetValueRow(idxInsert+insertContour.maxValidPts)+","+GetValueCol(idxInsert+insertContour.maxValidPts-1) );
   */

        for (i = 0; i < maxIndex && idxOut >= 0 && idx >= 0; i++) {
            /*
              if(idx< 0 || idx > ((numpts-1)<<1) || idxOut < 0 || idxOut > ((numpts-1)<<1) ){
                System.out.println("ERROR: problem at idx="+idx+",idxOut="+idxOut);
                continue;
              }
              */
            ptsDouble[idx] = ptsDouble[idxOut];
            ptsDouble[idx + 1] = ptsDouble[idxOut + 1];

            //if(i==0 || i==maxIndex-1){
            //  System.out.println("after pts["+idx+"]=("+ptsDouble[idx]+", "+ptsDouble[idx+1]+") " );
            //}

            idx -= 2;
            idxOut -= 2;
        }
        /*
             // test
             System.out.println("TEST: check before insert pts["+idxInsert+"]="+GetValueRow(idxInsert)+","+GetValueCol(idxInsert) );
             System.out.println("TEST: check before insert pts["+(idxInsert+insertContour.maxValidPts-1) +"]="+GetValueRow(idxInsert+insertContour.maxValidPts)+","+GetValueCol(idxInsert+insertContour.maxValidPts-1) );
        */

        // insert values
        // check if the inserted size fits the current size
        if (insertContour.maxValidPts + idxInsert <= numpts) {
            maxIndex = insertContour.maxValidPts;
        } else {
            maxIndex = numpts - idxInsert;
        }
        //System.out.println("TEST: Before Insertion: idxInsert="+idxInsert+",maxIndex="+maxIndex);

        idx = idxInsert << 1;
        idxOut = 0;
        for (i = idxInsert; i < idxInsert + maxIndex; i++) {
            ptsDouble[idx] = insertContour.ptsDouble[idxOut];
            ptsDouble[idx + 1] = insertContour.ptsDouble[idxOut + 1];
            idx += 2;
            idxOut += 2;
        }
        /*
             // test
             System.out.println("TEST: check after insert pts["+idxInsert+"]="+GetValueRow(idxInsert)+","+GetValueCol(idxInsert) );
             System.out.println("TEST: check after insert pts["+(idxInsert+insertContour.maxValidPts) +"]="+GetValueRow(idxInsert+insertContour.maxValidPts-1)+","+GetValueCol(idxInsert+insertContour.maxValidPts-1) );
        */
        if (maxValidPts + insertContour.maxValidPts < numpts) {
            maxValidPts += insertContour.maxValidPts;
        } else {
            maxValidPts = numpts;
        }
        return true;
    }

    //////////////////////////////////////////////////////////////////
    //display values
    public void PrintPoint2DDouble() {
        System.out.println("Point2DDouble Info :numpts=" + numpts + ", maxValidPts =" + maxValidPts);
        PrintPoint2DDoubleAllValues();
    }

    public boolean PrintPoint2DDoubleAllValues() {
        int i, idx;
        for (i = 0, idx = 0; i < numpts; i++, idx += 2) {
            System.out.print("pts[" + i + "]=(" + ptsDouble[idx] + ", " + ptsDouble[idx + 1] + ") ");
            if (((int) ((i + 1) / 5)) * 5 == i + 1)
                System.out.println("\n");
        }
        System.out.println("\n");
        return true;
    }

    public boolean PrintPoint2DDoubleValue(int idxPts) {

        int idx = idxPts << 1;
        if (idx < 0 || idx >= numpts << 1) {
            System.out.println("Error: Point is out of bounds");
            return false;
        }
        System.out.println("pts[" + idxPts + "]=(" + ptsDouble[idx] + ", " + ptsDouble[idx + 1] + ") ");
        return true;
    }

    ////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////
    public String Point2DDouble2String(int idxPts) {
        int idx = idxPts << 1;
        if (idx < 0 || idx >= numpts << 1) {
            System.out.println("Error: Point is out of bounds");
            return null;
        }
        String reString = "";//new String();
        reString += ptsDouble[idx] + ", " + ptsDouble[idx + 1] + "\n";
        return reString;
    }

    // takes the header pts information and converts it to string
    public String TwoDPtsInfo2String() {
        String reString = "number of 2Dpts=" + numpts + ", maxValidPts=" + maxValidPts;//+"\ndata type="+ sampType +"\n";
        return reString;
    }

}
