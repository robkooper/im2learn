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
package edu.illinois.ncsa.isda.imagetools.ext.segment;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.imagetools.core.datatype.*;
import edu.illinois.ncsa.isda.imagetools.ext.math.GeomOper;

import java.io.RandomAccessFile;

/**
 * @author yjlee
 *         <p/>
 *         TODO To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Style - Code Templates
 */
public class EdgeLine extends EdgeLineObject {
    private static Log logger = LogFactory.getLog(EdgeLine.class);
    private boolean _debugEdgeLine;

    DrawOp _draw = new DrawOp();

    GeomOper _GeomOper = new GeomOper();

    //constructor
    public EdgeLine() {
        _debugEdgeLine = true;
        _draw.setDebug(true);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Setters
    //////////////////////////////////////////////////////////////////////////////
    public void setDebug(boolean val) {
        _debugEdgeLine = val;
        _draw.setDebug(val);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Getters
    //////////////////////////////////////////////////////////////////////////////
    public boolean getDebug() {
        return _debugEdgeLine;
    }

    ////////////////////////////////////////////////////////////////////////////
    // show data
    /////////////////////////////////////////////////////////////////////////////
    public boolean showEdgeLineResult(ImageObject image, SubAreaFloat region) {
        if (image == null || image.getNumRows() <= 0 || image.getNumCols() <= 0) {
            logger.debug("Error: no image ");

            return false;
        }

        //		if (image.sampType.equalsIgnoreCase("BYTE") == false) {
        //			System.out.println("Error: only BYTE type images are supported ");
        //			return false;
        //		}

        int numrows, numcols;
        numrows = image.getNumRows();
        numcols = image.getNumCols();

        int nfound = getNFound();
        if (nfound <= 0) {
            logger.debug("Warning: no edgeline found ");

            return true;
        }

        int i;
        ImLine line = new ImLine();
        ImPoint ptsc = new ImPoint();
        ImPoint[] pts = new ImPoint[1];
        pts[0] = new ImPoint();

        ptsc.setX(region.getRow());
        ptsc.setY(region.getCol());
        ptsc.setV(5.0);
        double color = 127.0;//_MaxVal;

        if (_debugEdgeLine) {
            _draw.draw_boxDouble(image, region, ptsc, color);
            _draw.draw_circleDouble(image, ptsc, color);
        }

        color = 127.0;//_MaxVal - (_MaxVal-_MinVal)/255.0;

        for (i = 0; i < nfound; i++) {
            line.getPts1().setX(_PResultValue[i].getX());
            line.getPts1().setY(_PResultValue[i].getY());
            pts[0].setX(_PResultValue[i].getX() + _PResultValue[i].getH());
            pts[0].setY(_PResultValue[i].getY());
            ptsc.setX(_PResultValue[i].getX());
            ptsc.setY(_PResultValue[i].getY());

            _GeomOper.rotatePoints(ptsc, pts, 1,
                                 Math.toRadians(_PResultValue[i].getA()));

            line.getPts2().setX(pts[0].getX());
            line.getPts2().setY(pts[0].getY());

            _draw.plot_lineDouble(image, line, color);
        }

        return true;
    }

    public boolean showEdgeSubLineResult(ImageObject image, SubAreaFloat region) {

        if (image == null || image.getNumRows() <= 0 || image.getNumCols() <= 0) {
            logger.debug("Error: no image ");

            return false;
        }

        int numrows, numcols;
        numrows = image.getNumRows();
        numcols = image.getNumCols();

        int nfoundsub = getNFoundSub();
        if (nfoundsub <= 0) {
            logger.debug("Warning: no sub edgeline found ");
            return true;
        }

        int i;
        ImLine line = new ImLine();
        ImPoint ptsc = new ImPoint();
        ImPoint pts = new ImPoint();

        ptsc.setX(region.getRow());
        ptsc.setY(region.getCol());
        ptsc.setV(5.0);

        double color = _MaxVal;
        if (_debugEdgeLine) {
            _draw.draw_boxDouble(image, region, ptsc, color);
            _draw.draw_circleDouble(image, ptsc, color);
        }

        color = _MaxVal - (_MaxVal - _MinVal) / 255.0;

        //rotation param
        //  float a,sina,cosa;
        //  a = (float)(region.Angle * Deg2Rad);
        //sina = (float)Math.sin(a);
        //cosa = (float)Math.cos(a);

        for (i = 0; i < nfoundsub; i++) {
            line.getPts1().setX(_PResultSubVal[i].getX());
            line.getPts1().setY(_PResultSubVal[i].getY());
            pts.setX(_PResultSubVal[i].getX() + _PResultSubVal[i].getH());
            pts.setY(_PResultSubVal[i].getY());

            _GeomOper.rotatePoint(line.getPts1(), pts,
                                Math.toRadians(_PResultSubVal[i].getA()));
            line.getPts2().setX(pts.getX());
            line.getPts2().setY(pts.getY());

            _draw.plot_lineDouble(image, line, color);
        }

        return true;
    }

    /////////////////////////////////////////////////////////////////////////////
    // result will be displayed in a color image such that the bounding box
    // will be set to max green and the sub lines to max red
    /////////////////////////////////////////////////////////////////////////////
    public ImageObject showSubLinesPPM(ImageObject image, SubAreaFloat region) {

        if (image == null || image.getNumRows() <= 0 || image.getNumCols() <= 0) {
            logger.debug("Error: no image ");

            return null;
        }

        if (image.getNumBands() != 1) {
            logger.debug("Error: expected grayscale image ");

            return null;
        }

        int nfoundsub = getNFoundSub();
        if (nfoundsub <= 0) {
            logger.debug("Warning: no sub edgeline found ");

            return null;
        }

        int numrows, numcols;
        numrows = image.getNumRows();
        numcols = image.getNumCols();

        //this piece of the code solves a problem when the output image is
        //in log space but the edge line detection was in the linear space
        //therefore finding max min values here is important
        //find max and min
        int index;

        _MaxVal = LimitValues.MIN_FLOAT;
        _MinVal = Float.MAX_VALUE;

        float val;
        for (index = 0; index < image.getSize(); index++) {
            val = image.getFloat(index);

            if (val > _MaxVal) {
                _MaxVal = val;
            }

            if (val < _MinVal) {
                _MinVal = val;
            }
        }

        //in order to accomodate the overlay, the image is converted to color
        //tMatrix<Vector3f> *imageOut;
        ImageObject imageOut = null;
        //ConvertImFormat _conv = new ConvertImFormat();
        ColorModels _conv = new ColorModels();
        _conv.convertGRAY2RGB(image);
        //imageOut = _conv.PGMtoPPM(image);
        //imageOut = _conv.GrayscaleToColor(image);
        imageOut = _conv.getConvertedIm();
        if (imageOut == null)
            return null;

        Point3DDouble colorv = new Point3DDouble(1);

        //set the blue color for drawing lines
        colorv.getPtsDouble()[0] = _MinVal;
        colorv.getPtsDouble()[1] = _MinVal;
        colorv.getPtsDouble()[2] = _MaxVal;
        drawSubLinesPPM(imageOut, region, colorv, _MinVal, _MaxVal);

        return imageOut;
    }

    /////////////////////////////////////////////////////////////////////////////
    // draw subline result to a color image
    /////////////////////////////////////////////////////////////////////////////
    public boolean drawSubLinesPPM(ImageObject imageOut, SubAreaFloat region,
                                   Point3DDouble colorv, float minIm, float maxIm) {
        if (imageOut == null || imageOut.getNumRows() <= 0 || imageOut.getNumCols() <= 0) {
            logger.debug("Error: no color image ");

            return false;
        }

        int nfoundsub = getNFoundSub();

        if (nfoundsub <= 0) {
            logger.debug("Warning: no sub edgeline found ");

            return false;
        }

        int numrows, numcols;
        numrows = imageOut.getNumRows();
        numcols = imageOut.getNumCols();

        int i;
        ImLine line = new ImLine();
        ImPoint ptsc = new ImPoint();
        ImPoint pts = new ImPoint();

        ptsc.setX(region.getRow());
        ptsc.setY(region.getCol());
        ptsc.setV(2.0);

        Point3DDouble colorv1 = new Point3DDouble(1);//color of the bounding
        // box
        if (_debugEdgeLine) {
            double[] d = {minIm, maxIm, minIm};
            colorv1.setPtsDouble(d);
            _draw.draw_colorboxDouble(imageOut, region, ptsc, colorv1);

            d[0] = minIm;
            d[1] = minIm;
            d[2] = maxIm;
            colorv1.setPtsDouble(d);

            _draw.draw_colorcircleDouble(imageOut, ptsc, colorv1);
        }

        //find min and max scores
        float min, max, scalefactor;
        min = 1.0F;
        max = 0.0F;
        for (i = 0; i < nfoundsub; i++) {
            if (_PResultSubVal[i].getS() > max)
                max = _PResultSubVal[i].getS();
            if (_PResultSubVal[i].getS() < min)
                min = _PResultSubVal[i].getS();
        }

        if (max > min)
            scalefactor = (maxIm - minIm) / (max - min);
        else
            scalefactor = 1.0F;

        for (i = 0; i < nfoundsub; i++) {
            line.getPts1().setX(_PResultSubVal[i].getX());
            line.getPts1().setY(_PResultSubVal[i].getY());
            pts.setX(_PResultSubVal[i].getX() + _PResultSubVal[i].getH());
            pts.setY(_PResultSubVal[i].getY());
            _GeomOper.rotatePoint(line.getPts1(), pts,
                                Math.toRadians(_PResultSubVal[i].getA()));
            line.getPts2().setX(pts.getX());
            line.getPts2().setY(pts.getY());

            //add green color proportionally
            double d[] = colorv.getPtsDouble();
            d[1] = scalefactor * (_PResultSubVal[i].getS() - min) + minIm;
            colorv.setPtsDouble(d);

            _draw.plot_colorlineDouble(imageOut, line, colorv);
        }

        return true;
    }

    //////////////////////////////////////////////////////////////////////////////
    // doers
    //////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////
    // this functions saves results in the format compatible with clustering
    // algorithm
    //////////////////////////////////////////////////////////////////////////////
    public boolean printResults(EdgeLineObject edge, String OutFileName)
            throws Exception {
        int nfound = edge.getNFound();

        if (nfound <= 0) {
            logger.debug("Warning: no line has been found ");

            return false;
        }

        // open the file
        RandomAccessFile f0;
        f0 = new RandomAccessFile(OutFileName, "rw");

        int i;
        EdgeLineRes res = null;

        i = 4; // 4-dimensional space
        f0.writeBytes(Integer.toString(nfound));
        f0.writeBytes("\n");
        f0.writeBytes(Integer.toString(i));
        f0.writeBytes("\n");

        for (i = 0; i < nfound; i++) {
            res = edge.getResultSub(i);
            f0.writeBytes(Float.toString(res.getX()));
            f0.writeBytes("\t");
            f0.writeBytes(Float.toString(res.getY()));
            f0.writeBytes("\t");
            f0.writeBytes(Float.toString(res.getA()));
            f0.writeBytes("\t");
            f0.writeBytes(Float.toString(res.getH()));
            f0.writeBytes("\n");

        }

        f0.close();

        return true;
    }

    //////////////////////////////////////////////////////////////////
    // find edges in a box assuming that all edges are parallel with
    // (Row,Col) and (Row+High,Col) side of the area
    //////////////////////////////////////////////////////////////////
    public boolean findLines(ImageObject image, SubAreaFloat region) {
        return findLines((EdgeLineObject) this, image, region);
    }

    public boolean findLines(EdgeLineObject edge, ImageObject image,
                             SubAreaFloat region) {
        if (image == null) {
            logger.debug("Error: no image ");

            return false;
        }

        if (image.getNumBands() != 1) {
            logger.debug("Error: only grayscale image is supported ");

            return false;
        }

        //TODO:
        //		if (image.sampType.equalsIgnoreCase("BYTE") == false) {
        //			logger.debug("Error: only BYTE type images are supported ");
        //			return false;
        //		}

        int numrows, numcols;
        numrows = image.getNumRows();
        numcols = image.getNumCols();

        ImPoint ptsc = new ImPoint();
        ImPoint limit = new ImPoint();
        ptsc.setX(region.getRow());
        ptsc.setY(region.getCol());
        limit.setX(numrows);
        limit.setY(numcols);

        if (!_draw.CheckRegion(region, ptsc, limit)) {
            if (_debugEdgeLine)
                logger.debug("Error: region is outside of image ");

            return false;
        }

        int i, j, k, m, p, w, s, left, right, end, n;
        int m2find;
        float[] pSum = null;
        float[] pRes = null;
        float[] pPos = null;
        float distoff, a, sina, cosa, d, min;
        float sum0, sum1, sum2, level;
        float scaleByte = 100.0F / 255.0F;

        edge.setN2Find(0);

        // test parameters
        if (edge.getExpectedWidth() > 0)
            if (edge.getWidthTolerance() >= edge.getExpectedWidth())
                return false;

        if ((edge.getSize() << 1) + edge.getLeniency() + 1 >= (long) (region.getWidth() + 0.5)) // min.
        // 3
        // values
        // needed
            return false;

        // init
        distoff = (float) (edge.getLeniency() / 2.0 + (float) edge.getSize() - 0.5);
        a = (float) (Math.toRadians(region.getAngle()));
        sina = (float) Math.sin(a);
        cosa = (float) Math.cos(a);
        pSum = new float[(int) (region.getWidth() + 0.5)];
        pRes = new float[(int) (region.getWidth() + 0.5)];
        pPos = new float[(int) (region.getWidth() + 0.5)];
        //m2find = edge._Extract ? edge._N2Find + 1 : edge._N2Find;

        if (edge.getExtract() > 0)
            m2find = edge.getN2Find() + 1;
        else
            m2find = edge.getN2Find();

        // get the sum
        cSum(pSum, image, region);

        edge.setMaxVal(_MaxVal);
        edge.setMinVal(_MinVal);
        edge.setBackground((float) (0.5 * (pSum[0] + pSum[(int) (region.getWidth() + 0.5) - 1])
                                    / region.getHeight()));

        if (_MaxVal - _MinVal <= 0.0) {
            logger.debug("Error: image has only one value ");
            pSum = null;
            pRes = null;
            pPos = null;
            return false;
        }

        // filter: first pass (pSum->pRes)
        j = edge.getSize() + edge.getLeniency();
        end = (int) ((region.getWidth() + 0.5) - (edge.getSize() << 1)
                     - edge.getLeniency());

        for (i = 0; i <= end; ++i) {
            pRes[i] = pSum[i + j] - pSum[i];
            for (n = 1; n < edge.getSize(); ++n)
                pRes[i] += pSum[i + j + n] - pSum[i + n];
        }

        // find peaks above MinContrast: second pass (pPos)
        //TODO: it may not work. Need to check it later.
        level = edge.getAccept() / scaleByte * region.getHeight() * edge.getSize();
        //		if (image.sampType.equalsIgnoreCase("BYTE")) {
        //			level = edge._Accept / scaleByte * region.High * edge._Size;
        //		} else {
        //			logger.debug("Error:  image contains unsupported data type ");
        //			return false;
        //		}

        --end;
        i = end;

        while (i > 0)
            pPos[--i] = 0;
        for (i = 1; i < end; ++i) {
            if ((sum1 = Math.abs(pRes[i])) > level) {
                if ((sum0 = Math.abs(pRes[i - 1])) < sum1) {
                    if (sum1 > (sum2 = Math.abs(pRes[i + 1]))) {
                        d = (sum0 - sum2)
                            / (2.0F * (sum0 + sum2) - 4.0F * sum1);
                        pPos[i] = i + d;
                    } else {
                        if (sum1 == Math.abs(pRes[i + 1])) {
                            n = i + 2;
                            while (sum1 == (sum2 = Math.abs(pRes[n]))
                                   && n <= end)
                                ++n;
                            if (sum1 > sum2) {
                                d = (sum0 - sum2)
                                    / (2.0F * (sum0 + sum2) - 4.0F * sum1);
                                pPos[i] = i + (n - i - 1) / 2.0F + d;
                            }
                            i = n;
                        }
                    }
                }
            }
        }

        // Edge pair: third pass
        if (edge.getExpectedWidth() > 0) {
            n = end - edge.getExpectedWidth() + edge.getWidthTolerance();
            pPos[0] = 0;

            for (i = 1; i < n && edge.getNFound() < m2find; ++i) {
                if (pPos[i] > 0) {
                    if ((edge.getPolarity1() == 0 && pRes[i] != 0)
                        || (edge.getPolarity1() < 0 && pRes[i] < 0)
                        || (edge.getPolarity1() > 0 && pRes[i] > 0)) {
                        k = i + edge.getExpectedWidth();
                        for (j = 0; j <= edge.getWidthTolerance(); ++j) {
                            w = 0;

                            if ((right = k + j) < end && right > 0 && end > 0)
                                if (pPos[right] > 0)
                                    w = right;

                            if ((left = k - j) < end && left > 0 && end > 0)
                                if (pPos[left] > 0)
                                    w = left;

                            if (pPos[w] > 0) {
                                if ((edge.getPolarity2() == 0 && pRes[w] != 0)
                                    || (edge.getPolarity2() == -1 && pRes[w] < 0)
                                    || (edge.getPolarity2() == 1 && pRes[w] > 0)
                                        /* solid bars: */
                                    || (edge.getPolarity2() == 2 && ((edge.getPolarity1() == 0)
                                                                     || (edge.getPolarity1() < 0 && pRes[w] > 0)
                                                                     || (edge.getPolarity1() > 0 && pRes[w] < 0)))) {
                                    if (edge.getPolarity2() == 2) { // Solid bars only
                                        left = 0;

                                        for (s = i + 1; s < w; ++s) {
                                            if (pPos[s] > 0)
                                                left = 1;
                                        }

                                        if (left != 0)
                                            continue;
                                    }

                                    d = (Math.abs(pRes[i]) + Math.abs(pRes[w]))
                                        / region.getHeight()
                                        / (float) edge.getSize()
                                        / (float) 2.0 * scaleByte;

                                    if (edge.getNFound() >= edge.getN2Find()) { //  only in UseBest case
                                        m = edge.getN2Find() - 1;
                                        p = m;
                                        min = edge.getPResultValue()[m].getS();

                                        while (--m >= 0)
                                            if (edge.getPResultValue()[m].getS() < min) {
                                                min = edge.getPResultValue()[m].getS();
                                                p = m;
                                            }

                                        if (edge.getPResultValue()[p].getS() >= d)
                                            continue;
                                    } else {
                                        p = edge.getNFound();
                                        edge.setNFound(edge.getNFound() + 1);
                                    }

                                    edge.getPResultValue()[p].setW(pPos[w] - pPos[i]);
                                    edge.getPResultValue()[p].setO((pPos[i] + pPos[w])
                                                                   / (float) 2.0 + distoff);
                                    edge.getPResultValue()[p].setX(region.getRow() - sina
                                                                                     * edge.getPResultValue()[p].getO());
                                    edge.getPResultValue()[p].setY(region.getCol() + cosa
                                                                                     * edge.getPResultValue()[p].getO());
                                    edge.getPResultValue()[p].setS(d);

                                    if (pRes[i] > 0)
                                        edge.getPResultValue()[p].setP1((short) 1);
                                    else
                                        edge.getPResultValue()[p].setP1((short) -1);

                                    if (pRes[w] > 0)
                                        edge.getPResultValue()[p].setP2((short) 1);
                                    else
                                        edge.getPResultValue()[p].setP2((short) -1);

                                    edge.getPResultValue()[p].setA(region.getAngle());
                                    edge.getPResultValue()[p].setH(region.getHeight());

                                    break;
                                }
                            }
                        }
                    }
                }
            }
        } else { // Only single edges
            // eliminate wrong polarity
            if (edge.getPolarity1() > 0)
                for (i = 1; i < end; ++i)
                    if (pPos[i] > 0 && pRes[i] < 0)
                        pPos[i] = 0;

            if (edge.getPolarity1() < 0)
                for (i = 1; i < end; ++i)
                    if (pPos[i] > 0 && pRes[i] > 0)
                        pPos[i] = 0;

            // write result
            for (i = 1; i < end && edge.getNFound() < m2find; ++i) {
                if (pPos[i] > 0) {
                    // ACC_CONT;
                    d = Math.abs(pRes[i]) / region.getHeight() / (float) edge.getSize()
                        * scaleByte;

                    // / (_MaxVal - _MinVal);
                    if (edge.getNFound() >= edge.getN2Find()) { //  only in UseBest case
                        m = edge.getN2Find() - 1;
                        p = m;
                        min = edge.getPResultValue()[m].getS();

                        while (--m >= 0)
                            if (edge.getPResultValue()[m].getS() < min) {
                                min = edge.getPResultValue()[m].getS();
                                p = m;
                            }

                        if (edge.getPResultValue()[p].getS() >= d)
                            continue;
                    } else {
                        p = edge.getNFound();
                        edge.setNFound(edge.getNFound() + 1);
                    }

                    edge.getPResultValue()[p].setW(0);
                    edge.getPResultValue()[p].setO(pPos[i] + distoff);
                    edge.getPResultValue()[p].setX(region.getRow() - sina
                                                                     * edge.getPResultValue()[p].getO());
                    edge.getPResultValue()[p].setY(region.getCol() + cosa
                                                                     * edge.getPResultValue()[p].getO());
                    edge.getPResultValue()[p].setS(d);

                    if (pRes[i] > 0)
                        edge.getPResultValue()[p].setP1((short) 1);
                    else
                        edge.getPResultValue()[p].setP1((short) -1);

                    edge.getPResultValue()[p].setP2((short) 0);
                    edge.getPResultValue()[p].setA(region.getAngle());
                    edge.getPResultValue()[p].setH(region.getHeight());
                }
            }
        }

        // clear rest
        for (i = edge.getNFound(); i < edge.getN2Find(); ++i) {
            edge.getPResultValue()[i].setZero();
        }

        pSum = null;
        pRes = null;
        pPos = null;

        // Sort and write result
        sort(edge);

        return true;
    } // end Find

    //////////////////////////////////////////////////////////////////////////////
    // sum together cols from a subarea region
    //////////////////////////////////////////////////////////////////////////////
    protected boolean cSum(float[] pSum, ImageObject image, SubAreaFloat region) {
        int numrows, numcols;
        numrows = image.getNumRows();
        numcols = image.getNumCols();

        ImPoint ptsc = new ImPoint();
        ImPoint limit = new ImPoint();
        ptsc.setX(region.getRow());
        ptsc.setY(region.getCol());
        limit.setX(numrows);
        limit.setY(numcols);

        if (!_draw.CheckRegion(region, ptsc, limit)) {
            logger.debug("Error: region is outside of image ");

            return false;
        }

        // Call row sum if it will be faster for this angle
        int modAng;
        modAng = ((int) region.getAngle()) % 180;
        modAng = Math.abs(modAng);

        if (modAng < 30 || modAng > 150) {
            return rSum(pSum, image, region);
        }

        int i, j;
        float r, c;
        float angle = (float) (Math.toRadians(region.getAngle()));
        float cosAngle = (float) Math.cos(angle);
        float sinAngle = (float) Math.sin(angle);

        // This technique is more precise, but slower.
        // Keep track of the floating point row and column when stepping across
        // the row, truncating and multiplying to get an offset for each pixel
        // location.

        for (i = 0; i < region.getWidth(); i++) {
            pSum[i] = 0.0F;
        }

        // The left edge row and column
        float r00 = region.getRow();
        float c00 = region.getCol();
        float val;

        //TODO: It may not be working with non-Byte type. Need to check later.
        _MaxVal = Integer.MIN_VALUE;
        _MinVal = Integer.MAX_VALUE;

        // Loop through the rows
        for (i = 0; i < region.getHeight(); i++) {
            r = r00;
            c = c00;

            // Loop through the columns
            for (j = 0; j < region.getWidth(); j++) {
                val = image.getFloat((int) (r + 0.5F) * numcols
                                     + (int) (c + 0.5F));

                if (val < 0)
                    val += LimitValues.MAXPOS_BYTE;

                pSum[j] += val;

                if (val > _MaxVal)
                    _MaxVal = val;
                if (val < _MinVal)
                    _MinVal = val;

                // Increment to get the row and column of the next pixel in
                // this row
                r -= sinAngle;
                c += cosAngle;
            }
            // Step the left edge row and column to the next row
            r00 += cosAngle;
            c00 += sinAngle;
        }

        return true;
    } // end CSum

    //////////////////////////////////////////////////////////////////////////////
    // sum together rows from a subarea region
    //////////////////////////////////////////////////////////////////////////////
    protected boolean rSum(float[] pSum, ImageObject image, SubAreaFloat region) {
        int numrows, numcols;
        numrows = image.getNumRows();
        numcols = image.getNumCols();

        // sanity check
        ImPoint ptsc = new ImPoint();
        ImPoint limit = new ImPoint();
        ptsc.setX(region.getRow());
        ptsc.setY(region.getCol());
        limit.setX(numrows);
        limit.setY(numcols);

        if (!_draw.CheckRegion(region, ptsc, limit)) {
            logger.debug("Error: region is outside of image ");
            return false;
        }

        // Call row sum if it will be faster for this angle
        int modAng = ((int) region.getAngle()) % 180;
        modAng = Math.abs(modAng);

        if (modAng >= 30 && modAng <= 150) {
            return cSum(pSum, image, region);
        }
        float angle = (float) Math.toRadians(region.getAngle());
        float cosAngle = (float) Math.cos(angle);
        float sinAngle = (float) Math.sin(angle);

        int i, j;
        float r, c;

        // This technique is more precise, but slower.
        // Keep track of the floating point row and column when stepping across
        // the row, truncating and multiplying to get an offset for each pixel
        // location.

        // The left edge row and column
        float r00 = region.getRow();
        float c00 = region.getCol();
        float val;

        //TODO: It may not work with non-Byte type. Need to check.
        _MaxVal = Integer.MIN_VALUE;
        _MinVal = Integer.MAX_VALUE;

        // Loop through the rows
        for (j = 0; j < region.getWidth(); j++) {
            r = r00;
            c = c00;
            pSum[j] = 0.0F;

            // Loop through the columns
            for (i = 0; i < region.getHeight(); i++) {
                val = image.getFloat((int) (r + 0.5F) * numcols
                                     + (int) (c + 0.5F));

                if (val < 0)
                    val += LimitValues.MAXPOS_BYTE;

                pSum[j] += val;

                if (val > _MaxVal)
                    _MaxVal = val;

                if (val < _MinVal)
                    _MinVal = val;

                // Increment to get the row and column of the next pixel in
                // this row
                r += cosAngle;
                c += sinAngle;
            }

            // Step the left edge row and column to the next row
            r00 -= sinAngle; //+=Math.cos(Angle+90)
            c00 += cosAngle;//+=Math.sin(Angle+90)
        }

        return true;
    } // end RSum

    /////////////////////////////////////////////////////////////////////////////
    // sort results based on their attributes (fields)
    /////////////////////////////////////////////////////////////////////////////
    public boolean sort(EdgeLineObject edge) {
        int i;
        boolean flag = true;
        int end = edge.getNFound() - 1;
        EdgeLineRes tmpValue = null;//new EdgeLinesRes();

        if (edge.getSortType() == 0 || edge.getNFound() < 2) { // Unsorted or only one edge
            return true;
        }

        switch (edge.getSortType()) {
            case 1: // Offset
                while (flag) {
                    flag = false;

                    for (i = 0; i < end; ++i)
                        if (edge.getPResultValue()[i].getO() >
                            edge.getPResultValue()[i + 1].getO()) {
                            tmpValue = edge.getPResultValue()[i];
                            edge.setPResultValueItem(edge.getPResultValue()[i + 1], i);
                            edge.setPResultValueItem(tmpValue, i + 1);
                            flag = true;
                        }
                    --end;
                }
                break;

            case 2: // Score
                while (flag) {
                    flag = false;
                    for (i = 0; i < end; ++i)
                        if (edge.getPResultValue()[i].getS() <
                            edge.getPResultValue()[i + 1].getS()) {
                            tmpValue = edge.getPResultValue()[i];
                            edge.setPResultValueItem(edge.getPResultValue()[i + 1], i);
                            edge.setPResultValueItem(tmpValue, i + 1);
                            flag = true;
                        }
                    --end;
                }
                break;

            case 3: // X Position
                while (flag) {
                    flag = false;

                    for (i = 0; i < end; ++i)
                        if (edge.getPResultValue()[i].getX() <
                            edge.getPResultValue()[i + 1].getX()) {
                            tmpValue = edge.getPResultValue()[i];
                            edge.setPResultValueItem(edge.getPResultValue()[i + 1], i);
                            edge.setPResultValueItem(tmpValue, i + 1);
                            flag = true;
                        }
                }

                break;

            case 4: // Y Position
                while (flag) {
                    flag = false;

                    for (i = 0; i < end; ++i)
                        if (edge.getPResultValue()[i].getY() <
                            edge.getPResultValue()[i + 1].getY()) {
                            tmpValue = edge.getPResultValue()[i];
                            edge.setPResultValueItem(edge.getPResultValue()[i + 1], i);
                            edge.setPResultValueItem(tmpValue, i + 1);
                            flag = true;
                        }
                    --end;
                }

                break;

            case 5: // Width
                while (flag) {
                    flag = false;

                    for (i = 0; i < end; ++i)
                        if (edge.getPResultValue()[i].getW() <
                            edge.getPResultValue()[i + 1].getW()) {
                            tmpValue = edge.getPResultValue()[i];
                            edge.setPResultValueItem(edge.getPResultValue()[i + i], i);
                            edge.setPResultValueItem(tmpValue, i + 1);
                            flag = true;
                        }

                    --end;
                }

                break;

            case 6: // Delta Width
                while (flag) {
                    flag = false;

                    for (i = 0; i < end; ++i)
                        if (Math.abs(edge.getPResultValue()[i].getW() - edge.getExpectedWidth()) >
                            Math.abs(edge.getPResultValue()[i + 1].getW() - edge.getExpectedWidth())) {
                            tmpValue = edge.getPResultValue()[i];
                            edge.setPResultValueItem(edge.getPResultValue()[i + 1], i);
                            edge.setPResultValueItem(tmpValue, i + 1);
                            flag = true;
                        }

                    --end;
                }

                break;

            case 7: // Pos. Polarity
                while (flag) {
                    flag = false;

                    for (i = 0; i < end; ++i)
                        if (edge.getPResultValue()[i].getP1() <
                            edge.getPResultValue()[i + 1].getP1()) {
                            tmpValue = edge.getPResultValue()[i];
                            edge.setPResultValueItem(edge.getPResultValue()[i + 1], i);
                            edge.setPResultValueItem(tmpValue, i + 1);
                            flag = true;
                        }

                    --end;
                }

                break;

            case 8: // Neg. Polarity
                while (flag) {
                    flag = false;
                    for (i = 0; i < end; ++i)
                        if (edge.getPResultValue()[i].getP1() >
                            edge.getPResultValue()[i + 1].getP1()) {
                            tmpValue = edge.getPResultValue()[i];
                            edge.setPResultValueItem(edge.getPResultValue()[i + 1], i);
                            edge.setPResultValueItem(tmpValue, i + 1);
                            flag = true;
                        }

                    --end;
                }

                break;

            default:
                break;
        }

        return true;
    } // end Sort

    //////////////////////////////////////////////////////////////////
    // find a set of sub lines of already found line edges in a box
    //  assuming that all edges are parallel with
    // (Row,Col) and (Row+High,Col) side of the area
    //////////////////////////////////////////////////////////////////
    public boolean findSubLines(ImageObject image, SubAreaFloat region) {
        return (findSubLines((EdgeLineObject) this, image, region));
    }

    public boolean findSubLines(EdgeLineObject edge, ImageObject image,
                                SubAreaFloat region) {
        if (image == null || image.getNumRows() <= 0 || image.getNumCols() <= 0) {
            logger.debug("Error: no image ");

            return false;
        }

        if (edge.getNFound() <= 0) {
            logger.debug("Error: no line edge was found before sub line operation ");

            return false;
        }

        if (image.getNumBands() != 1) {
            logger.debug("Error: only grayscale image is supported ");

            return false;
        }

        //TODO: Need to check this.
        //		if (image.sampType.equalsIgnoreCase("BYTE") == false) {
        //			logger.debug("Error: only BYTE type images are supported ");
        //			return false;
        //		}

        int numrows, numcols;
        numrows = image.getNumRows();
        numcols = image.getNumCols();

        //allocate memory for new sublines
        if (edge.getPResultValue() != null)
            edge.setPResultValue(null);

        int msublines = edge.getNFound() * edge.getSubLinesPerLine();
        edge.setPResultSubVal(new EdgeLineRes[msublines]);

        if (edge.getPResultSubVal() == null) {
            logger.debug("Error: could not allocate memory ");

            return false;
        }

        int i, k, n, m;
        for (i = 0; i < msublines; i++)
            edge.setPResultSubValItem(new EdgeLineRes(), i);

        edge.setNFoundSub(0);

        float[] pSum = null;
        float[] pRes = new float[3];
        float a, sina, cosa, sum0, sum1, sum2;

        float r00, c00, row, col, val;
        float r, c;
        int numSums = 2 + 2 * edge.getSize() + edge.getLeniency();
        pSum = new float[numSums];

        //rotation param
        a = (float) (Math.toRadians(region.getAngle()));
        sina = (float) Math.sin(a);
        cosa = (float) Math.cos(a);

        int shift = edge.getSize() + edge.getLeniency();
        float level;// = edge.Accept *(edge._MaxVal - edge._MinVal) * edge.Size;
        float scaleByte = 100.0F / 255.0F;
        int beginline = 1;
        int endline = 0;
        int index = 0; // number of sub lines
        int storehigh = 0, numsublines;
        //  float alpha;

        for (k = 0; k < edge.getNFound(); k++) {
            numsublines = 0;
            // the edgeline is defined by pSum[k],k is from offset-shift
            // up to offset+Size+Leniency
            r00 = region.getRow() - sina * (edge.getPResultValue()[k].getO() - shift);
            c00 = region.getCol() + cosa * (edge.getPResultValue()[k].getO() - shift);
            //    row = region.Row - sina * edge._PResultValue[k].o;
            row = edge.getPResultValue()[k].getX();
            //col = region.Col + cosa * edge._PResultValue[k].o;
            col = edge.getPResultValue()[k].getY();

            //the local score of an edgeline
            //level = edge._PResultValue[k].s*(edge._MaxVal - edge._MinVal) *
            // edge._Size;
            level = edge.getPResultValue()[k].getS() / scaleByte * edge.getSize();


            // Loop through the rows
            for (m = 0; m < region.getHeight() && numsublines < edge.getSubLinesPerLine(); m++) {
                r = r00;
                c = c00;

                //clean up pSum
                //for(pSum=pSum00 ; pSum<pSum01; pSum++)
                //  *pSum = 0;
                for (i = 0; i < numSums; i++)
                    pSum[i] = 0.0F;

                //get pSums for one row
                //for(pSum=pSum00; pSum<pSum01; pSum++){
                for (i = 0; i < numSums; i++) {
                    val = image.getFloat((int) r * numcols + (int) c);

                    if (val < 0) {
                        val += LimitValues.MAXPOS_BYTE;
                    }

                    pSum[i] += val;
                    // Increment to get the row and column of the next pixel in
                    // this row
                    r -= sina;
                    c += cosa;
                }

                //compute pRes[0,1,2] for one row
                // end =(int)((region.Wide + 0.5) - (edge.Size<<1) -
                // edge.Leniency);
                //pSum = pSum00;
                for (i = 0; i < 3; ++i) {
                    pRes[i] = pSum[i + shift] - pSum[i];
                    for (n = 1; n < edge.getSize(); ++n)
                        pRes[i] += pSum[i + shift + n] - pSum[i + n];
                }

                // find peaks above MinContrast
                sum0 = Math.abs(pRes[0]);
                sum1 = Math.abs(pRes[1]);
                sum2 = Math.abs(pRes[2]);

                if (sum1 > level || sum0 > level || sum2 > level) {
                    //test
                    //        logger.debug( "row=" + m + " pRes "+ sum0 + " " +
                    // sum1 + " " + sum2 );

                    if (beginline != 0) {
                        //add a new sub line
                        edge.getPResultSubVal()[index].setX(row);
                        edge.getPResultSubVal()[index].setY(col);
                        edge.getPResultSubVal()[index].setO(edge.getPResultValue()[k].getO());
                        edge.getPResultSubVal()[index].setW(edge.getPResultValue()[k].getW());
                        edge.getPResultSubVal()[index].setS(edge.getPResultValue()[k].getS());
                        edge.getPResultSubVal()[index].setP1(edge.getPResultValue()[k].getP1());
                        edge.getPResultSubVal()[index].setP2(edge.getPResultValue()[k].getP2());
                        edge.getPResultSubVal()[index].setA(edge.getPResultValue()[k].getA());

                        //high is still unknown
                        storehigh = m;
                        endline = 1;
                        beginline = 0;
                    }
                } else {
                    if (endline != 0) {
                        if (m - storehigh > edge.getSubLineLength()) {
                            edge.getPResultSubVal()[index].setH(m - storehigh);
                            index++;
                            numsublines++;
                        }

                        endline = 0;
                        beginline = 1;
                    }
                }

                // Step the left edge row and column to the next row
                r00 += cosa;
                c00 += sina;
                row += cosa;
                col += sina;
            }

            //if the current line has not been completed
            if (endline != 0) {
                if (m - storehigh > edge.getSubLineLength()) {
                    edge.getPResultSubVal()[index].setH(m - storehigh);
                    index++;
                }

                endline = 0;
                beginline = 1;
            }

        }//end of k loop

        edge.setNFoundSub(index);

        // clear rest
        for (i = edge.getNFoundSub(); i < msublines; ++i) {
            edge.getPResultSubVal()[i].setZero();
        }

        pSum = null;

        return true;
    } // end FindSubLines

    ///////////////////////////////////////////////////////////////
    // used for detecting roads from a mask
    // allows the region be bigger that the image !!!!!!
    //////////////////////////////////////////////////////////////////////////////
    // counts all pixel with maskVal value in a given direction (along columns)
    // inside of a subarea region
    //////////////////////////////////////////////////////////////////////////////
    protected boolean cSumMask(int[] pSum, ImageObject mask,
                               SubAreaFloat region, byte maskVal) {
        if (mask == null) {
            logger.debug("Error: no image ");

            return false;
        }

        if (mask.getNumBands() != 1
            || mask.getType() != ImageObject.TYPE_BYTE) {
            logger.debug("Error: only grayscale BYTE image is supported ");

            return false;
        }

        int numrows, numcols;
        numrows = mask.getNumRows();
        numcols = mask.getNumCols();

        // Call row sum if it will be faster for this angle
        int modAng = ((int) region.getAngle()) % 180;
        modAng = Math.abs(modAng);

        if (modAng >= 30 && modAng <= 150) {
            return rSumMask(pSum, mask, region, maskVal);
        }

        int i, j;
        float r, c;
        //      unsigned char *pSrc;
        float angle = (float) (Math.toRadians(region.getAngle()));
        float cosAngle = (float) Math.cos(angle);
        float sinAngle = (float) Math.sin(angle);

        // This technique is more precise, but slower.
        // Keep track of the floating point row and column when stepping across
        // the row, truncating and multiplying to get an offset for each pixel
        // location.

        for (i = 0; i < region.getWidth(); i++)
            pSum[i] = 0;

        // The left edge row and column
        float r00 = region.getRow();
        float c00 = region.getCol();
        byte val;

        // Loop through the rows
        for (i = 0; i < region.getHeight(); i++) {
            r = r00;
            c = c00;

            // Loop through the columns
            //for(pSum=pSum00; pSum<pSum01; pSum++){
            for (j = 0; j < region.getWidth(); j++) {
                if (r < 0 || r >= numrows || c < 0 || c >= numcols) {
                    r -= sinAngle;
                    c += cosAngle;

                    continue;
                }

                val = mask.getByte((int) r * numcols + (int) c);

                if (val == maskVal) {
                    pSum[j] += 1;
                }

                // Increment to get the row and column of the next pixel in this
                // row
                r -= sinAngle;
                c += cosAngle;
            }

            // Step the left edge row and column to the next row
            r00 += cosAngle;
            c00 += sinAngle;
        }

        return true;
    } // end CSumMask

    //////////////////////////////////////////////////////////////////////////////
    // counts all pixel with maskVal value in a given direction
    // inside of a subarea region
    //////////////////////////////////////////////////////////////////////////////
    protected boolean rSumMask(int[] pSum, ImageObject mask,
                               SubAreaFloat region, byte maskVal) {
        if (mask == null) {
            logger.debug("Error: no image ");
            return false;
        }

        if (mask.getNumBands() != 1
            || mask.getType() != ImageObject.TYPE_BYTE) {
            logger.debug("Error: only grayscale BYTE image is supported ");

            return false;
        }

        int numrows, numcols;
        numrows = mask.getNumRows();
        numcols = mask.getNumCols();

        int i, j;
        float r, c;

        float angle = (float) (Math.toRadians(region.getAngle()));
        float cosAngle = (float) Math.cos(angle);
        float sinAngle = (float) Math.sin(angle);

        // Call row sum if it will be faster for this angle
        int modAng = ((int) region.getAngle()) % 180;
        modAng = Math.abs(modAng);

        if (modAng < 30 || modAng > 150) {
            return cSumMask(pSum, mask, region, maskVal);
        }

        // This technique is more precise, but slower.
        // Keep track of the floating point row and column when stepping across
        // the row, truncating and multiplying to get an offset for each pixel
        // location.

        // The left edge row and column
        float r00 = region.getRow();
        float c00 = region.getCol();
        byte val;

        // Loop through the rows
        //for( ; pSum<pSum10; pSum++){
        for (j = 0; j < region.getWidth(); j++) {
            r = r00;
            c = c00;
            pSum[j] = 0;

            // Loop through the columns
            //        for(*pSum=0,r=r00,c=c00,i=0; i<region.High; i++){
            for (i = 0; i < region.getHeight(); i++) {
                if (r < 0 || r >= numrows || c < 0 || c >= numcols) {
                    r += cosAngle;
                    c += sinAngle;

                    continue;
                }

                val = mask.getByte((int) r * numcols + (int) c);

                if (val == maskVal)
                    pSum[j] += 1;

                // Increment to get the row and column of the next pixel in this
                // row
                r += cosAngle;
                c += sinAngle;
            }

            // Step the left edge row and column to the next row
            r00 -= sinAngle; //+=Math.cos(Angle+90)
            c00 += cosAngle;//+=Math.sin(Angle+90)
        }

        return true;
    } // end RSumMask

    //display values
    public void printEdgeLine() {
        logger.debug("EdgeLine:");
    }
}//end of class EdgeLine

