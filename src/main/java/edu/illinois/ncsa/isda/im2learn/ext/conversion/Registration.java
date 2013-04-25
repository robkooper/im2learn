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
package edu.illinois.ncsa.isda.im2learn.ext.conversion;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.renderable.ParameterBlock;

import javax.media.jai.InterpolationBilinear;
import javax.media.jai.InterpolationNearest;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.WarpGrid;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageException;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.im2learn.core.datatype.SubArea;
import edu.illinois.ncsa.isda.im2learn.core.io.jai.JAIutil;
import edu.illinois.ncsa.isda.im2learn.ext.math.MatrixExt;

/**
 * <p>
 * Title: Registration
 * </p>
 * <p>
 * Description: Estimate the affine parameters for two sets of coordinates using
 * least-square method
 * </p>
 * 
 * <pre>
 *  [x]       [a  b t_x] [x'] &lt;BR&gt;
 *  [y] =     [c  d t_y] [y'] &lt;BR&gt;
 *  [w]       [0  0  1 ] [1]  &lt;BR&gt;
 * </pre>
 * 
 * <p/>
 * 
 * @author Sang-Chul Lee
 * @version 1.0
 */

public class Registration {
    static private Log      logger = LogFactory.getLog(Registration.class);

    /**
     * An image to be transformed
     */
    private final Point2D[] source;

    /**
     * An Target Image
     */
    private final Point2D[] target;

    /**
     * Affine parameters <BR>
     * m00,m10,m01,m11,m02,m12};
     */
    private double[]        parameter;

    private MatrixExt       coefficientMatrix1, coefficientMatrix2;

    private double[]        param1, param2;

    public Registration(Point2D[] target, Point2D[] source) {
        this(target, source, true);
    }

    //    public Registration(ImPoint[] target, ImPoint[] source) {
    //        if (source != null && target != null)
    //            runDouble(source, target);
    //    }

    public Registration(Point2D[] target, Point2D[] source, boolean scale) {
        this.source = source;
        this.target = target;
        if ((source != null) && (target != null)) {
            run();
        }

        if (!scale) {
            AffineTransform afx = this.getAffineTransform();
            logger.debug(afx.getScaleX() + " " + afx.getScaleY());
            param1[0] /= afx.getScaleX();
            param2[0] /= afx.getScaleX();
            param1[1] /= afx.getScaleY();
            param2[1] /= afx.getScaleY();
            param1[3] *= afx.getScaleX();
            param2[3] *= afx.getScaleY();
        }

    }

    /*
     * private boolean run2() {
     * 
     * double[] _xp = new double[9]; double[] _x = new double[9];
     * 
     * int cnt = 0; for(int i=0; i<3; i++) {
     * 
     * _x[cnt] = target[i].x; _xp[cnt++] = source[i].x;
     * 
     * _x[cnt] = target[i].y; _xp[cnt++] = source[i].y;
     * 
     * _x[cnt] = 1; _xp[cnt++] = 1; }
     * 
     * 
     * MatrixExt xp = new MatrixExt(3,3,_xp); MatrixExt x = new
     * MatrixExt(3,3,_x);
     * 
     * MatrixExt p = x.multiply(xp.inverse());
     * 
     * param1 = new double[4]; param2 = new double[4];
     * 
     * param1[0] = 1.2;//p.mat.get(0,0); param1[1] = 0;//p.mat.get(0,1);
     * param1[3] = 0.010;//p.mat.get(0,2);
     * 
     * param2[0] = 0;//p.mat.get(1,0); param2[1] = 1.2;//p.mat.get(1,1);
     * param2[3] = 0.01;//p.mat.get(1,2);
     * 
     * 
     * 
     * return true; }
     */

    public static void transform(Point2D[] srcPoints, Point2D[] destPoints, AffineTransform afx) {
        afx.transform(srcPoints, 0, destPoints, 0, srcPoints.length);
    }

    //    public static void transform(ImPoint[] srcPoints, ImPoint[] destPoints, AffineTransform afx) {
    //        double[] src = new double[srcPoints.length * 2];
    //        double[] dst = new double[srcPoints.length * 2];
    //
    //        for (int i = 0; i < srcPoints.length; i++) {
    //            src[i * 2] = srcPoints[i].x;
    //            src[i * 2 + 1] = srcPoints[i].y;
    //        }
    //
    //        afx.transform(src, 0, dst, 0, srcPoints.length);
    //
    //        for (int i = 0; i < destPoints.length; i++) {
    //            destPoints[i].SetImPoint(dst[i * 2], dst[i * 2 + 1], srcPoints[i].v);
    //        }
    //
    //    }
    //
    //
    //    private boolean runDouble(ImPoint[] s, ImPoint[] t) {
    //        double[] x, y, prime;
    //        if (s.length != t.length) {
    //            return false;
    //        } else {
    //            x = new double[s.length];
    //            y = new double[s.length];
    //            prime = new double[t.length];
    //
    //            for (int i = 0; i < s.length; i++) {
    //                x[i] = s[i].x;
    //                y[i] = s[i].y;
    //                prime[i] = t[i].x;
    //            }
    //
    //            coefficientMatrix1 = calculatecoefficientMatrix(x, y, prime);
    //
    //            for (int i = 0; i < s.length; i++) {
    //                prime[i] = t[i].y;
    //            }
    //
    //            coefficientMatrix2 = calculatecoefficientMatrix(x, y, prime);
    //
    //
    //            //  coefficientMatrix1.printMatrix();
    //            //  coefficientMatrix2.printMatrix();
    //
    //            coefficientMatrix1.calculateEigen();
    //            coefficientMatrix2.calculateEigen();
    //            param1 = coefficientMatrix1.getSmallestValuedVectorArray();
    //            param2 = coefficientMatrix2.getSmallestValuedVectorArray();
    //
    //            // printArray(param1);
    //            //      printArray(param2);
    //
    //            double div1 = param1[2] * (-1);
    //            double div2 = param2[2] * (-1);
    //
    //            for (int i = 0; i < 4; i++) { // normalize
    //                param1[i] = param1[i] / div1;
    //                param2[i] = param2[i] / div2;
    //            }
    //
    //            //    printArray(param1);
    //            //   printArray(param2);
    //
    //            return true;
    //        }
    //    }

    private boolean run() {
        double[] x, y, prime;
        if (source.length != target.length) {
            return false;
        } else {
            x = new double[source.length];
            y = new double[source.length];
            prime = new double[target.length];

            for (int i = 0; i < source.length; i++) {

                // DC 4.8.2005
                //x[i] = source[i].x;
                //y[i] = source[i].y;
                //prime[i] = target[i].x;
                x[i] = source[i].getX();
                y[i] = source[i].getY();
                prime[i] = target[i].getX();
            }

            coefficientMatrix1 = calculatecoefficientMatrix(x, y, prime);

            for (int i = 0; i < source.length; i++) {
                //prime[i] = target[i].y;
                prime[i] = target[i].getY();
            }

            coefficientMatrix2 = calculatecoefficientMatrix(x, y, prime);

            //  coefficientMatrix1.printMatrix();
            //  coefficientMatrix2.printMatrix();

            coefficientMatrix1.calculateEigen();
            coefficientMatrix2.calculateEigen();
            param1 = coefficientMatrix1.getSmallestValuedVectorArray();
            param2 = coefficientMatrix2.getSmallestValuedVectorArray();

            // printArray(param1);
            //      printArray(param2);

            double div1 = param1[2] * (-1);
            double div2 = param2[2] * (-1);

            for (int i = 0; i < 4; i++) { // normalize
                param1[i] = param1[i] / div1;
                param2[i] = param2[i] / div2;
            }

            //    printArray(param1);
            //   printArray(param2);

            return true;
        }
    }

    private MatrixExt calculatecoefficientMatrix(double[] x, double[] y, double[] prime) {
        MatrixExt covMatrix, temp;

        // good to go

        covMatrix = new MatrixExt(4, 4, 0);
        for (int i = 0; i < x.length; i++) {
            double[] q = { x[i], y[i], prime[i], 1 };
            temp = new MatrixExt(4, 1, q);

            covMatrix = covMatrix.plus(temp.multiply(temp.transpose()));
        }

        return covMatrix;
    }

    /**
     * Returns affine parameters
     */
    public double[] getParameters() {
        parameter = new double[6];

        parameter[0] = param1[0];
        parameter[1] = param1[1];
        parameter[2] = param1[3];

        parameter[3] = param2[0];
        parameter[4] = param2[1];
        parameter[5] = param2[3];

        return parameter;
    }

    public AffineTransform getAffineTransform() {
        double p1, p2, p3, p4, p5, p6;
        p1 = param1[0];
        p2 = param2[0];
        p3 = param1[1];
        p4 = param2[1];
        p5 = param1[3];
        p6 = param2[3];

        AffineTransform afx = new AffineTransform(p1, p2, p3, p4, p5, p6);
        return afx;
    }

    public AffineTransform getRigidTransform() {
        return calculateRigid(target, source);
    }

    public static AffineTransform calculateRigid(Point2D[] a, Point2D[] b) {
        Point2D[] bp = new Point2D[b.length];
        Point2D vec1, vec2;
        Point2D.Double translate = new Point2D.Double();
        double rotation;

        for (int i = 0; i < b.length; i++) {
            //bp[i] = new Point(b[i].x, b[i].y);
            bp[i] = new Point2D.Double(b[i].getX(), b[i].getY());
        }

        // DC 4.8.2005
        //translate.x = a[0].x - b[0].x;
        //translate.y = a[0].y - b[0].y;
        translate.x = a[0].getX() - b[0].getX();
        translate.y = a[0].getY() - b[0].getY();
        AffineTransform retAfx = new AffineTransform();
        retAfx.setToTranslation(translate.x, translate.y);

        Registration.transform(bp, bp, retAfx);

        // DC 4.8.2005
        //vec1 = new Point(a[1].x - a[0].x, a[1].y - a[0].y);
        //vec2 = new Point(bp[1].x - bp[0].x, bp[1].y - bp[0].y);
        vec1 = new Point2D.Double(a[1].getX() - a[0].getX(), a[1].getY() - a[0].getY());
        vec2 = new Point2D.Double(bp[1].getX() - bp[0].getX(), bp[1].getY() - bp[0].getY());

        //double cos = (vec1.x * vec2.x + vec1.y * vec2.y) /
        //             (Math.sqrt(Math.pow(vec1.x, 2) + Math.pow(vec1.y, 2)) *
        //              Math.sqrt(Math.pow(vec2.x, 2) + Math.pow(vec2.y, 2)));
        double cos = (vec1.getX() * vec2.getX() + vec1.getY() * vec2.getY())
                / (Math.sqrt(Math.pow(vec1.getX(), 2) + Math.pow(vec1.getY(), 2)) * Math.sqrt(Math.pow(vec2.getX(), 2) + Math.pow(vec2.getY(), 2)));

        //double ccw = vec1.y / vec1.x - vec2.y / vec2.x;
        double ccw = vec1.getY() / vec1.getX() - vec2.getY() / vec2.getX();

        logger.info("CCW=" + ccw);

        rotation = (ccw < 0) ? Math.acos(cos) * (-1) : Math.acos(cos);

        AffineTransform retAfx2 = new AffineTransform();
        //retAfx2.setToRotation(rotation, bp[0].x, bp[0].y);
        retAfx2.setToRotation(rotation, bp[0].getX(), bp[0].getY());
        //  Registration.transform(bp,bp,retAfx2);

        retAfx.preConcatenate(retAfx2);

        return retAfx;
    }

    static public ImageObject getImageTransformed(ImageObject imgobj, AffineTransform afx) throws ImageException {
        if ((imgobj == null) || !imgobj.isDataValid()) {
            throw (new ImageException("Did not provide a valid image."));
        }

        // convert image to renderedop
        RenderedOp ropimage = JAIutil.getRenderedOp(imgobj, 0);

        // transform using matrix and nearest interpolation
        RenderedOp roptrans = JAI.create("affine", ropimage, afx, new InterpolationBilinear());

        // convert back to imageobject same size as original
        SubArea subarea = new SubArea(0, 0, 0, imgobj.getNumCols(), imgobj.getNumRows(), imgobj.getNumBands());
        ImageObject res = JAIutil.getImageObject(roptrans, subarea);

        // 0 is invalid
        res.setInvalidData(0);

        // copy the properties
        try {
            res.setProperties(imgobj.cloneProperties());
        } catch (ImageException exc) {
            logger.warn("Error copying properties.", exc);
        }

        // return result
        return res;
    }

    static public ImageObject getImageTransformed(ImageObject imgobj, Point2D[] f1, Point2D[] f2) throws ImageException {
        if ((imgobj == null) || !imgobj.isDataValid()) {
            throw (new ImageException("Did not provide a valid image."));
        }

        // make sure both feature sets are the same length.
        if (f1.length != f2.length) {
            if (f1.length > f2.length) {
                Point2D[] ftmp = f1;
                f1 = new Point2D[f2.length];
                System.arraycopy(ftmp, 0, f1, 0, f2.length);
            } else {
                Point2D[] ftmp = f2;
                f2 = new Point2D[f1.length];
                System.arraycopy(ftmp, 0, f2, 0, f1.length);
            }
        }

        Registration reg = new Registration(f1, f2);
        AffineTransform afx = reg.getAffineTransform();

        // convert image to renderedop
        RenderedOp ropimage = JAIutil.getRenderedOp(imgobj, 0);

        // transform using matrix and nearest interpolation
        RenderedOp roptrans = JAI.create("affine", ropimage, afx, new InterpolationBilinear());

        // convert back to imageobject same size as original
        SubArea subarea = new SubArea(0, 0, 0, imgobj.getNumCols(), imgobj.getNumRows(), imgobj.getNumBands());
        ImageObject res = JAIutil.getImageObject(roptrans, subarea);

        // 0 is invalid
        res.setInvalidData(0);

        // copy the properties
        try {
            res.setProperties(imgobj.cloneProperties());
        } catch (ImageException exc) {
            logger.warn("Error copying properties.", exc);
        }

        // return result
        return res;
    }

    static public ImageObject getImageTransformedRigid(ImageObject imgobj, Point2D[] f1, Point2D[] f2) throws ImageException {
        if ((imgobj == null) || !imgobj.isDataValid()) {
            throw (new ImageException("Did not provide a valid image."));
        }

        // make sure both feature sets are the same length.
        if (f1.length != f2.length) {
            if (f1.length > f2.length) {
                Point2D[] ftmp = f1;
                f1 = new Point2D[f2.length];
                System.arraycopy(ftmp, 0, f1, 0, f2.length);
            } else {
                Point2D[] ftmp = f2;
                f2 = new Point2D[f1.length];
                System.arraycopy(ftmp, 0, f2, 0, f1.length);
            }
        }

        Registration reg = new Registration(f1, f2);
        AffineTransform afx = reg.getRigidTransform();

        // convert image to renderedop
        RenderedOp ropimage = JAIutil.getRenderedOp(imgobj, 0);

        // transform using matrix and nearest interpolation
        RenderedOp roptrans = JAI.create("affine", ropimage, afx, new InterpolationBilinear());

        // convert back to imageobject same size as original
        SubArea subarea = new SubArea(0, 0, 0, imgobj.getNumCols(), imgobj.getNumRows(), imgobj.getNumBands());
        ImageObject res = JAIutil.getImageObject(roptrans, subarea);

        // 0 is invalid
        res.setInvalidData(0);

        // copy the properties
        try {
            res.setProperties(imgobj.cloneProperties());
        } catch (ImageException exc) {
            logger.warn("Error copying properties.", exc);
        }

        // return result
        return res;
    }

    static public ImageObject getImageTransformedWarp(ImageObject imgobj, WarpGrid warp) throws ImageException {
        if ((imgobj == null) || !imgobj.isDataValid()) {
            throw (new ImageException("Did not provide a valid image."));
        }

        RenderedOp ropimage = JAIutil.getRenderedOp(imgobj, 0);

        ParameterBlock pb = new ParameterBlock();
        pb.addSource(ropimage);
        pb.add(warp);
        pb.add(new InterpolationNearest());

        // Create the warp operation.
        RenderedOp roptrans = JAI.create("warp", pb);

        // convert back to imageobject same size as original
        SubArea subarea = new SubArea(0, 0, 0, imgobj.getNumCols(), imgobj.getNumRows(), imgobj.getNumBands());
        ImageObject res = JAIutil.getImageObject(roptrans, subarea);

        // 0 is invalid
        res.setInvalidData(0);

        // copy the properties
        try {
            res.setProperties(imgobj.cloneProperties());
        } catch (ImageException exc) {
            logger.warn("Error copying properties.", exc);
        }

        // return result
        return res;
    }

}
