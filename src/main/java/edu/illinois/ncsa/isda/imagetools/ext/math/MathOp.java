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
package edu.illinois.ncsa.isda.imagetools.ext.math;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.imagetools.core.datatype.*;

/**
 * @author yjlee
 */
public class MathOp {
    private static Log logger = LogFactory.getLog(MathOp.class);
    private boolean _debugMathOper;

    //constructors
    public MathOp() {
    }

    //setters and getters
    public void setDebug(boolean flag) {
        _debugMathOper = flag;
    }

    public boolean getDebug() {
        return _debugMathOper;
    }

    //////////////////////////////////////////////////////////////////////////////
    // Solves the second order polynomial equation
    //////////////////////////////////////////////////////////////////////////////
    public boolean Solve2p(float[] k, ComplexNum[] res) {
        double help;
        double one, two;

        // solution to k[1] x^2 + k[2] x +k[3]=0
        if (k[1] != 0) {
            help = k[2] * k[2] - 4.0 * k[1] * k[3];
            if (help >= 0) {
                help = Math.sqrt(help);
                one = (-k[2] + help) / (2.0 * k[1]);
                two = (-k[2] - help) / (2.0 * k[1]);
                res[0].setComplexNum(one, 0.0);
                res[1].setComplexNum(two, 0.0);
            } else {
                help = Math.sqrt(-help);
                one = (-k[2]) / (2 * k[1]);
                two = help / (2 * k[1]);
                res[0].setComplexNum(one, two);
                one = (-k[2]) / (2 * k[1]);
                two = -help / (2 * k[1]);
                res[1].setComplexNum(one, two);
            }

            return true;
        } else {
            logger.debug("warning: linear equation (not quadratic) \n");
            return false;
        }
    }

    /////////////////////////////////////////////////////////////////////////
    // solves overdetermined set of linear equations used by LMS Arc Fit
    /////////////////////////////////////////////////////////////////////////
    public boolean QRSolve(float[] x, float[] a, short nequ, short ncols) {
        short i, j, k, wlength, count = 0;
        boolean err = false;
        float[] w = null;
        float[] v = null;
        float erss = 0.0F;

        //long solve_triangular_system(short ncols,float *w);
        //long update_qr_fact(short ncols,float *v,float *w,short *count,float
        // *erss);

        /***********************************************************************
         * Solve (possibly) overdetermined system of equations using
         * QR-factorization solution technique.
         *
         * Notation: matrix form is Ax=b where A is nequ x ncols, x is ncols x
         * 1, b is nequ x 1
         *
         * Technique transforms Ax=b into Cx=d where C is ncols x ncols and is
         * upper triangular. Transformation is done one equation (row) at a time
         * using update_qr_fact(). Updating is done on augmented matrix having
         * "x" column appended to C. Starting point has initial augmented C
         * matrix = 0. Ending point has upper triangular (non-zero) elements of
         * augmented C matrix "strung out" in vector w. Thus, w is initialized
         * to all 0's then is successively updated (using Givens rotations).
         * Input at each step is vector v of length ncols+1. Each v has
         * coefficients for one equation (one row of A) augmented with right
         * side of equation (element of b). Note that the "a" data structure
         * passed to this routine is expected to consist of augmented
         * coefficient vectors "v".
         *
         * Triangular system is solved using solve_triangular_system() and
         * results in final solution for "x" in last column of augmented (and
         * transformed) C matrix (expressed here in selected components of the
         * "w" vector.
         **********************************************************************/

        if (ncols > nequ) {
            return false;
        }
        wlength = (short) (ncols + ncols * (ncols + 1) / 2);
        if ((w = new float[wlength]) == null) {
            return false;
        }
        if ((v = new float[ncols + 1]) == null) {
            w = null;
            return false;
        }
        for (i = 0; i < wlength; ++i) {
            w[i] = 0.0F;
        }
        for (i = 0, k = 0; i < nequ; ++i) {
            for (j = 0; j < ncols + 1; ++j, ++k) {
                //v[j]= ((float *)a)[k];
                v[j] = a[k];
            }
            update_qr_fact(ncols, v, w, count, erss);
        }

        err = solve_triangular_system(ncols, w);

        for (i = 0, j = wlength; i < ncols; ++i) {
            j -= (i + 1);
            x[ncols - i - 1] = w[j];
        }
        //  exit_qrsolve: // label is not used

        w = null;
        v = null;
        return err;
    }

    private boolean update_qr_fact(short ncols, float[] v, float[] w,
                                   short count, float erss) {
        short r1, col, iptr, iptr0;
        float a, c, s, den, eps1, eps2;

        eps2 = (float) LimitValues.EPSILON;//0.000001;
        eps1 = eps2 * eps2;
        for (r1 = 0; r1 < ncols; r1++) {
            iptr = (short) (r1 * (2 * ncols - r1 + 1) / 2 + r1); /*
                                                                  * index in 'w'
                                                                  * of diagonal
                                                                  * element
                                                                  */
            iptr0 = (short) (iptr - r1);
            den = (float) Math.sqrt(w[iptr] * w[iptr] + v[r1] * v[r1]);
            if (Math.abs((double) den) > eps1) {
                s = v[r1] / den;
                if (Math.abs((double) s) > eps2) {
                    c = w[iptr] / den;
                    for (col = r1; col < ncols + 1; col++) {
                        a = c * w[iptr0 + col] + s * v[col];
                        v[col] = -s * w[iptr0 + col] + c * v[col];
                        w[iptr0 + col] = a;
                    }
                }
            }
        }
        if (count > ncols - 1)
            erss = erss + v[ncols] * v[ncols];
        count = (short) (count + 1);
        return true;
    }

    private boolean solve_triangular_system(short ncols, float[] w) {
        short i, j, ptjn, ptjj, ptin, ptij;

        for (j = (short) (ncols - 1); j >= 1; j--) {
            ptjn = (short) (j * (2 * ncols - j + 1) / 2 + ncols);
            ptjj = (short) (ptjn - ncols + j);
            w[ptjn] = w[ptjn] / w[ptjj];
            for (i = (short) (j - 1); i >= 0; i--) {
                ptin = (short) (i * (2 * ncols - i + 1) / 2 + ncols);
                ptij = (short) (ptin - ncols + j);
                w[ptin] = w[ptin] - w[ptjn] * w[ptij];
            }
        }
        w[ncols] = w[ncols] / w[0];

        return true;
    }

    private float Interpolate(float v1, float v, float v2) {
        float n = v1 - v2;
        float d = 2 * (v1 + v2) - 4 * v;
        if (d == 0.0F)
            return (0.0F);

        return (n / d);
    }

    // end of solving a set of linear equations
    /////////////////////////////////////////////////////////////////////////
    // solves overdetermined set of linear equations using doubles
    /////////////////////////////////////////////////////////////////////////
    // this is the interface used for CEE498HI
    public double[] QRSolveD(double[] a, short nequ, short ncols) {
        int k;

        /*
         * //test for( k=0;k <a.length;k++){
         * logger.debug("a["+k+"]="+a[k]); }
         */
        double[] x = new double[ncols];
        for (k = 0; k < ncols; k++)
            x[k] = -1.0;
        if (nequ >= ncols && QRSolveD(x, a, nequ, ncols)) {
            //good result
            //test
            //for(int j=0;j<ncols;j++){
            //  logger.debug("x["+j+"] = " + x[j]);
            //x[j] = j;
            //}
            return x;
        } else {
            // bad result
            for (int i = 0; i < ncols; i++) {
                x[i] = -1.0;
            }
            return x;
        }
    }

    /////////////////////////////////////////////////////////////////////////
    public boolean QRSolveD(double[] x, double[] a, short nequ, short ncols) {
        short i, j, k, wlength, count = 0;
        boolean err = false;
        double[] w = null;
        double[] v = null;
        double erss = 0.0;

        //long solve_triangular_systemD(short ncols,double *w);
        //long update_qr_factD(short ncols,double *v,double *w,short
        // *count,double *erss);

        /***********************************************************************
         * Solve (possibly) overdetermined system of equations using
         * QR-factorization solution technique.
         *
         * Notation: matrix form is Ax=b where A is nequ x ncols, x is ncols x
         * 1, b is nequ x 1
         *
         * Technique transforms Ax=b into Cx=d where C is ncols x ncols and is
         * upper triangular. Transformation is done one equation (row) at a time
         * using update_qr_fact(). Updating is done on augmented matrix having
         * "x" column appended to C. Starting point has initial augmented C
         * matrix = 0. Ending point has upper triangular (non-zero) elements of
         * augmented C matrix "strung out" in vector w. Thus, w is initialized
         * to all 0's then is successively updated (using Givens rotations).
         * Input at each step is vector v of length ncols+1. Each v has
         * coefficients for one equation (one row of A) augmented with right
         * side of equation (element of b). Note that the "a" data structure
         * passed to this routine is expected to consist of augmented
         * coefficient vectors "v".
         *
         * Triangular system is solved using solve_triangular_system() and
         * results in final solution for "x" in last column of augmented (and
         * transformed) C matrix (expressed here in selected components of the
         * "w" vector.
         **********************************************************************/

        if (ncols > nequ) {
            return false;
        }
        wlength = (short) (ncols + ncols * (ncols + 1) / 2);
        //  if((w=(float *)malloc(wlength*sizeof(float)))==null)
        if ((w = new double[wlength]) == null) {
            return false;
        }
        //  if((v=(float *)malloc((ncols+1)*sizeof(float)))==null)
        if ((v = new double[ncols + 1]) == null) {
            w = null;
            return false;
        }
        for (i = 0; i < wlength; ++i) {
            w[i] = 0.0;
        }
        for (i = 0, k = 0; i < nequ; ++i) {
            for (j = 0; j < ncols + 1; ++j, ++k) {
                //v[j]= ((double *)a)[k];
                v[j] = a[k];
            }
            update_qr_factD(ncols, v, w, count, erss);
        }

        err = solve_triangular_systemD(ncols, w);

        for (i = 0, j = wlength; i < ncols; ++i) {
            j -= (i + 1);
            //test
            if (j < 0 || (ncols - i - 1) < 0 || j >= w.length
                || (ncols - i - 1) >= x.length) {
                System.err.println("ERROR: input arrays are wrong: j=" + j
                                   + ", ncols=" + ncols + ", i=" + i);
                System.err.println("ERROR: x length=" + x.length
                                   + ", w length=" + w.length);
            } else {
                x[ncols - i - 1] = w[j];
            }
        }
        //  exit_qrsolve: // label is not used

        w = null;
        v = null;
        return err;
    }

    private boolean update_qr_factD(short ncols, double[] v, double[] w,
                                    short count, double erss) {
        short r1, col, iptr, iptr0;
        double a, c, s, den, eps1, eps2;

        eps2 = LimitValues.EPSILON;//(float) 0.000001;
        eps1 = eps2 * eps2;
        for (r1 = 0; r1 < ncols; r1++) {
            iptr = (short) (r1 * (2 * ncols - r1 + 1) / 2 + r1); /*
                                                                  * index in 'w'
                                                                  * of diagonal
                                                                  * element
                                                                  */
            iptr0 = (short) (iptr - r1);
            den = (float) Math.sqrt(w[iptr] * w[iptr] + v[r1] * v[r1]);
            if (Math.abs(den) > eps1) {
                s = v[r1] / den;
                if (Math.abs(s) > eps2) {
                    c = w[iptr] / den;
                    for (col = r1; col < ncols + 1; col++) {
                        a = c * w[iptr0 + col] + s * v[col];
                        v[col] = -s * w[iptr0 + col] + c * v[col];
                        w[iptr0 + col] = a;
                    }
                }
            }
        }
        if (count > ncols - 1)
            erss = erss + v[ncols] * v[ncols];
        count = (short) (count + 1);
        return true;
    }

    private boolean solve_triangular_systemD(short ncols, double[] w) {
        short i, j, ptjn, ptjj, ptin, ptij;

        for (j = (short) (ncols - 1); j >= 1; j--) {
            ptjn = (short) (j * (2 * ncols - j + 1) / 2 + ncols);
            ptjj = (short) (ptjn - ncols + j);
            w[ptjn] = w[ptjn] / w[ptjj];
            for (i = (short) (j - 1); i >= 0; i--) {
                ptin = (short) (i * (2 * ncols - i + 1) / 2 + ncols);
                ptij = (short) (ptin - ncols + j);
                w[ptin] = w[ptin] - w[ptjn] * w[ptij];
            }
        }
        w[ncols] = w[ncols] / w[0];

        return true;
    }

    // end of solving a set of linear equations with doubles
    //////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////
    // this solves a polynomial equation with one uknown using
    // bisection approach
    public double SolvePolynomial1(double[] coef, int order, double a,
                                   double b, double tolerance) {
        // double EvaluatePolynom(double *coef, int order,double x);

        int maxIter = 20;
        double fA, fB, fval, val;
        double left, right, sample;
        int iter;
        boolean signal;
        double error, result;

        fA = evaluatePolynom(coef, order, a);
        fB = evaluatePolynom(coef, order, b);
        val = fA * fB;
        left = a;
        right = b;
        if (val >= 0) {
            if (val == 0.0) {// found solution
                if (fA == 0.0)
                    return a;
                else
                    return b;
            } else {
                // find an interval such that val <0
                // sample as many subinterval as the order
                double delta = (b - a) / (double) order;
                signal = true;
                for (sample = a + delta; signal && sample < b; sample += delta) {
                    fval = evaluatePolynom(coef, order, sample);
                    if (fA * fval < 0) {
                        signal = false;
                        left = sample - delta;
                        right = sample;
                        fB = fval;
                    } else {
                        fA = fval;
                    }
                }
                if (signal) {
                    logger.debug("Error: did not find a subinterval intersecting zero \n");
                    return 1.0;
                }
            }
        }
        // initial result

        if (fA > fB)
            result = right;
        else
            result = left;
        // start bisecting
        error = tolerance + 1.0;
        iter = 0;
        while (error > tolerance && iter < maxIter) {
            sample = (left + right) * 0.5;
            fval = evaluatePolynom(coef, order, sample);
            if (fval * fA <= 0) {
                right = sample;
                fB = fval;
                if (Math.abs(fval) < Math.abs(fA)) {
                    error = Math.abs(fval);
                    result = right;
                } else {
                    error = Math.abs(fA);
                    result = left;
                }
            } else {
                if (fval * fB < 0) {
                    left = sample;
                    fA = fval;
                    if (Math.abs(fval) < Math.abs(fB)) {
                        error = Math.abs(fval);
                        result = left;
                    } else {
                        error = Math.abs(fB);
                        result = right;
                    }
                } else {
                    logger.debug("Error: should not happen this case !!! \n");

                    return 1.0;
                }
            }
            iter++;
        }
        logger.debug("estimation error = " + error + "\t iter= " + iter);
        logger.debug("left = " + left + "\t right = " + right);
        return (result);
    }

    //////////////////////////////////////////////////////////////////////
    /// Solve() - given a start point startX, iteratively find the solution
    /// to the desired accuracy.
    /// LimitU and LimitV specify the min,max limits of solution X. If
    /// x begins to converge to a value outside of Limits, Solve exits.
    //  secant approach
    //////////////////////////////////////////////////////////////////////
    public double SolvePolynomial(double[] coef, int order, double a, double b,
                                  double tolerance) {
        double SECANT_HUGE = 1e10;
        int cont = 1;
        double errorX = SECANT_HUGE;
        double errorX_old = 0;
        double errorFX = SECANT_HUGE;
        double errorFX_old = 0;
        double diff, fA, fB, newB;
        double posdiff;
        double singular_tol = tolerance;

        //double EvaluatePolynom(double *coef, int order,double x);

        int numIterations = 0;
        int maxIterations = 40;
        // double error=SECANT_HUGE;
        int converged = 0;

        // determine the initial function value at a and b
        // make sure diff doesn't equal 0
        fA = evaluatePolynom(coef, order, a);
        do {
            fB = evaluatePolynom(coef, order, b);
            diff = fA - fB;
            // posdiff is the abs value of diff
            posdiff = (diff < 0 ? -diff : diff);
            if ((posdiff < tolerance) && (numIterations < 5)) {
                b += .1;
                numIterations++;
            }
        } while ((posdiff < tolerance) && (numIterations < 5));

        // only enter solve loop if we are not singular
        if (posdiff < singular_tol)
            cont = 0;

        // make sure a and b enclose a solution (fA*fB<0)
        if (fA * fB > tolerance)
            cont = 0;

        // begin convergence loop
        numIterations = 0;
        while (cont != 0) {
            numIterations++;
            // compute the new value for b and the evaluate the function at b.
            newB = b - (fB * (a - b)) / (diff);
            //      cout<<"a="<<a<<"\tb="<<b<<"\tnewB="<<newB<<"\tfB="<<fB<<endl;
            a = b;
            fA = fB;
            b = newB;
            fB = evaluatePolynom(coef, order, b);

            // save the old errorFX
            errorFX_old = errorFX;
            // find the errorFX (in this case, the non-negative value of fB)
            errorFX = (fB < 0 ? -fB : fB);

            // save the old errorX
            errorX_old = errorX;
            // find the errorX (in this case, the non-negative value of b)
            errorX = (b - a < 0 ? a - b : b - a);

            // find the new diff of fA-fB (need to make sure the next denom is
            // not zero)
            diff = fA - fB;

            // if we iterate more than max # times, exit
            //cont=cont&&!(numIterations>maxIterations);
            if (numIterations > maxIterations)
                cont = 1;
            // if the system has converged to the desired accuracy, stop solving
            //cont=cont&&!(errorFX<=tolerance);
            if (errorFX <= tolerance)
                cont = 1;
            // if diff is too close to zero, stop solving (singularity)
            //cont=cont&&!((diff<=singular_tol)&&(diff>=-singular_tol));
            if ((diff <= singular_tol) || (diff >= -singular_tol))
                cont = 1;
            // if the system is diverging, stop solving
            //cont=cont&&!(errorFX>=10*errorFX_old);
            if (errorFX >= 10 * errorFX_old)
                cont = 1;
            // if errorX is increasing, stop solving
            //cont=cont&&!(errorX>=10*errorX_old);
            if (errorX >= 10 * errorX_old)
                cont = 1;

        }
        // record if the solution converged
        if (errorFX < tolerance) {
            //      error=errorFX;
            converged = 1;//true;
        }
        logger.debug("Converged = " + converged);
        logger.debug("dif from zero = " + evaluatePolynom(coef, order, b));

        return b;
    }

    // plug in number x and return value
    private double evaluatePolynom(double[] coef, int order, double x) {
        int i;
        double sum, val;
        sum = coef[0];
        val = x;
        for (i = 1; i < order; i++) {
            sum += coef[i] * val;
            val *= x;
        }
        return (sum);
    }

    /*
     * ////////////////////////////////////////////////////////////////// //
     * perform convolution of an image with 1D kernel // used , for example, by
     * Gaussian blur
     * ////////////////////////////////////////////////////////////////// public
     * boolean Convolve1D(ImageObject imageIn,ImageObject imageOut,float []
     * kernel,int ker_size,ConvAxis axis) { if(imageIn == null || imageOut ==
     * null){ logger.debug("ERROR: missing ImageObjects"); return false; }
     * if(imageIn.sampType.equalsIgnoreCase("FLOAT")== false ||
     * imageOut.sampType.equalsIgnoreCase("FLOAT")== false ){
     * logger.debug("ERROR: image objects have to be of float type");
     * return false; } if(imageIn.sampPerPixel != 1){ logger.debug("ERROR:
     * only grayscale input image object is supported"); return false; } int
     * x,y,i; float total; int index,index1,index2; int numrows,numcols; numrows =
     * imageIn.numrows; numcols = imageIn.numcols;
     *
     * if(axis == Vertical){ // Vertical index =0; for (x = 0; x < numrows; x++) {
     * for (y = 0; y < numcols; y++){ total = 0; index1 = index - ker_size;
     * index2 = 0; for (i = -ker_size; i <= ker_size; i++){ if((y+i)>=0 && (y+i)
     * <numcols) total += imageIn.imageFloat[index1] * kernel[index2];
     *
     * index2++; index1++; } imageOut.imageFloat[index]= total; } } } if(axis ==
     * Horizontal){ // Horizontal int shift = ker_size*numcols; index =0; for (x =
     * 0; x < numrows; x++) { for (y = 0; y < numcols; y++){ total = 0; index1 =
     * index - shift; index2 = 0; for (i = -ker_size; i <= ker_size; i++){
     * if((x+i)>=0 && (x+i) <numrows) total += imageIn.imageFloat[index1] *
     * kernel[index2];
     *
     * index2++; index1+=numcols; } imageOut.imageFloat[index]= total; } } }
     * return true; }
     */
    ///////////////////////////////////////////////////////////////////
    // least-mean square fit to line expressed as y=k*x+q
    // it returns line representation
    // invalid coordinate values are equal to -1
    ///////////////////////////////////////////////////////////////////
    public ImLine lms_linefitY(int number, int delta, int slide, short top,
                               ImPoint[] pcontour) {
        double sumx, sumy, sumxy, sumx2;
        int i, j = 0, begin, end, count;
        double help, k = 0.0, q = 0.0;
        // fitting a line to y=k*x+q
        // number = total number of selected points from all
        // delta = 1 => every point is taken into consideration
        // slide = shift in the array of points
        // top = number of all points for the sake of closed contours
        // pcontour = points
        // q returns the line shift

        float minx, maxx, miny, maxy;
        minx = maxx = miny = maxy = 0.0F;
        count = 0;
        sumx = 0;
        sumy = 0;
        sumxy = 0;
        sumx2 = 0;
        begin = slide;
        end = number * delta;
        for (i = slide; i < slide + end; i = i + delta) {
            if (i >= top) {
                //help=(double) fmod( (double) i, (double) top );
                help = Math.IEEEremainder((double) i, (double) top);
                j = (int) help;
            } else
                j = i;

            if (pcontour[j].getX() != -1 && pcontour[j].getY() != -1) {
                count += 1;
                sumx = sumx + pcontour[j].getX();
                sumy = sumy + pcontour[j].getY();
                sumxy = sumxy + pcontour[j].getX() * pcontour[j].getY();
                sumx2 = sumx2 + pcontour[j].getX() * pcontour[j].getX();

                if (count == 1) {
                    minx = maxx = (float) pcontour[j].getX();
                    miny = maxy = (float) pcontour[j].getY();
                } else {
                    if (minx > pcontour[j].getX())
                        minx = (float) pcontour[j].getX();
                    if (maxx < pcontour[j].getX())
                        maxx = (float) pcontour[j].getX();
                    if (miny > pcontour[j].getX())
                        miny = (float) pcontour[j].getY();
                    if (maxy < pcontour[j].getY())
                        maxy = (float) pcontour[j].getY();
                }
            }
        }
        //end might be at the begining of a contour !!
        end = j;

        if (count != 0) {
            if (Math.abs(count * sumxy - sumx * sumy) == 0.0) {//(double)
                // 0.00000
                if (Math.abs(count * sumx2 - sumx * sumx) == 0.0) {//(double)
                    // 0.00000
                    if (pcontour[begin].getY() - pcontour[end].getY() < 0)
                        k = LimitValues.SLOPE_MAX;
                    else
                        k = -LimitValues.SLOPE_MAX;//-_ValInfinitySlope;

                    q = (double) sumx / count;
                } else {
                    if (pcontour[begin].getX() - pcontour[end].getX() < 0)
                        k = LimitValues.EPSILON;//0.00001;
                    else
                        k = -LimitValues.EPSILON;//-0.00001;

                    q = (double) sumy / count;
                }
            } else {
                if (Math.abs(count * sumx2 - sumx * sumx) == 0.0) {// (double)
                    // 0.00000
                    if (pcontour[begin].getY() - pcontour[end].getY() < 0)
                        k = LimitValues.SLOPE_MAX;
                    else
                        k = -LimitValues.SLOPE_MAX;//-_ValInfinitySlope;

                    q = (double) sumx / count;
                } else {
                    k = (double) count * sumxy - sumx * sumy;
                    help = (double) count * sumx2 - sumx * sumx;
                    k = (double) k / help;

                    if (Math.abs(k) <= LimitValues.THRESH_INF_SLOPE
                        && Math.abs(k) >= LimitValues.EPSILON)//0.00001
                        q = (double) (sumy - k * sumx) / count;
                    else {
                        if (Math.abs(k) > LimitValues.THRESH_INF_SLOPE) {
                            if (pcontour[begin].getY() - pcontour[end].getY() < 0)
                                k = LimitValues.SLOPE_MAX;//_ValInfinitySlope;
                            else
                                k = -LimitValues.SLOPE_MAX;//-_ValInfinitySlope;

                            q = (double) sumx / count;
                        }
                        if (Math.abs(k) < LimitValues.EPSILON) {//0.00001
                            if (pcontour[begin].getX() - pcontour[end].getX() < 0)
                                k = LimitValues.EPSILON;//0.00001;
                            else
                                k = -LimitValues.EPSILON;//-0.00001;

                            q = (double) sumy / count;
                        }
                    }
                }
            }
        } else {
            k = q = 0.0;
        }
        //store value of the fitted line
        ImLine line = new ImLine();
        line.setSlope(k);
        line.setQ(q);
        line.getPts1().setX(minx);
        line.getPts2().setX(maxx);
        if (minx != maxx && line.getSlope() != LimitValues.THRESH_INF_SLOPE
            && line.getSlope() != -LimitValues.THRESH_INF_SLOPE) {
            line.getPts1().setY(line.getSlope() * line.getPts1().getX() + line.getQ());
            line.getPts2().setY(line.getSlope() * line.getPts2().getX() + line.getQ());
        } else {
            line.getPts1().setY(miny);
            line.getPts2().setY(maxy);
        }

        return (line);
    }

    ///////////////////////////////////////////////////////////////////
    // least-mean square fit to line expressed as x=k*y+q
    // it returns line representation
    // invalid coordinate values are equal to -1
    ///////////////////////////////////////////////////////////////////
    public double lms_linefitX(int number, int delta, int slide, short top,
                               ImPoint[] pcontour, double q) {
        double sumx, sumy, sumxy, sumx2;
        int i, j = 0, begin, end, count;
        double help, k;
        // fitting a line to x=k*y+q
        count = 0;
        sumx = 0;
        sumy = 0;
        sumxy = 0;
        sumx2 = 0;
        begin = slide;
        end = number * delta;
        for (i = slide; i < slide + end; i = i + delta) {
            if (i >= top) {
                //help=(double) fmod( (double) i, (double) top );
                help = Math.IEEEremainder((double) i, (double) top);
                j = (int) help;
            } else
                j = i;

            count += 1;
            sumx = sumx + pcontour[j].getY();
            sumy = sumy + pcontour[j].getX();
            sumxy = sumxy + pcontour[j].getX() * pcontour[j].getY();
            sumx2 = sumx2 + pcontour[j].getY() * pcontour[j].getY();
        }
        //end might be at the begining of a contour !!!
        end = j;

        if (Math.abs(count * sumxy - sumx * sumy) == 0.0) {//(double) 0.00000
            if (Math.abs(count * sumx2 - sumx * sumx) == 0.0) {//(double)
                // 0.00000
                if (pcontour[begin].getX() - pcontour[end].getX() < 0)
                    k = LimitValues.SLOPE_MAX;//_ValInfinitySlope;
                else
                    k = -LimitValues.SLOPE_MAX;//-_ValInfinitySlope;

                q = (double) sumx / count;
            } else {
                if (pcontour[begin].getY() - pcontour[end].getY() < 0)
                    k = LimitValues.EPSILON;//0.00001;
                else
                    k = -LimitValues.EPSILON;
                ;//-0.00001;

                q = (double) sumy / count;
            }
        } else {
            if (Math.abs(count * sumx2 - sumx * sumx) == 0.0) {//(double)
                // 0.00000
                if (pcontour[begin].getX() - pcontour[end].getX() < 0)
                    k = LimitValues.SLOPE_MAX;//_ValInfinitySlope;
                else
                    k = -LimitValues.SLOPE_MAX;//-_ValInfinitySlope;

                q = (double) sumx / count;
            } else {
                k = (double) count * sumxy - sumx * sumy;
                help = (double) count * sumx2 - sumx * sumx;
                k = (double) k / help;

                if (Math.abs(k) <= LimitValues.THRESH_INF_SLOPE
                    && Math.abs(k) >= LimitValues.EPSILON)//0.00001
                    q = (double) (sumy - k * sumx) / count;
                else {
                    if (Math.abs(k) > LimitValues.THRESH_INF_SLOPE) {
                        if (pcontour[begin].getX() - pcontour[end].getX() < 0)
                            k = LimitValues.SLOPE_MAX;//_ValInfinitySlope;
                        else
                            k = -LimitValues.SLOPE_MAX;//-_ValInfinitySlope;

                        q = (double) sumx / count;
                    }
                    if (Math.abs(k) < 0.00001) {
                        if (pcontour[begin].getY() - pcontour[end].getY() < 0)
                            k = LimitValues.EPSILON;//0.00001;
                        else
                            k = -LimitValues.EPSILON;//-0.00001;

                        q = (double) sumy / count;
                    }
                }
            }
        }
        return (k);
    }

    ///////////////////////////////////////////////////////////////////
    // a least-mean square fit to a circle expressed as (x-cx)^2 + (y-cy)^2 =
    // r^2
    // it returns a center point with the radius
    // invalid coordinate values are equal to -1
    ///////////////////////////////////////////////////////////////////
    public boolean lms_arcfit(int slide, int nseg, ImPoint[] pts, ImPoint ptsc,
                              int inscore) {
        int i, j, idx = 0;
        float[] a = null;
        float[] x = null;
        short num_ofrows, num_ofcol;
        double help;
        boolean ret = true;

        a = new float[4 * (nseg + 1)];
        x = new float[4];

        j = 0;
        for (i = 0; i < nseg; i++) {
            if (pts[slide + i].getX() != -1 && pts[slide + i].getY() != -1) {
                idx = j << 2;//4*j
                a[idx] = (float) (pts[slide + i].getX() * pts[slide + i].getX()
                                  + pts[slide + i].getY() * pts[slide + i].getY());
                a[idx + 1] = (float) pts[slide + i].getX();
                a[idx + 2] = (float) pts[slide + i].getY();
                a[idx + 3] = 1.0F;
                j += 1;
            }
        }

        num_ofrows = (short) j;
        num_ofcol = (short) 3;
        inscore = num_ofrows;// output score
        if (num_ofrows >= num_ofcol && QRSolve(x, a, num_ofrows, num_ofcol)) {
            if (Math.abs(x[0]) > LimitValues.EPSILON) {//0.0000001
                x[3] = 0.0F;
                for (j = 0; j < num_ofrows; j++)
                    idx = j << 2;
                x[3] += a[idx] * x[0] + a[idx + 1] * x[1] + a[idx + 2] * x[2];

                x[3] = -x[3] / num_ofrows; // dependent variable in the equation
                // for circle

                ptsc.setX(-x[1] / (2.0 * x[0]));
                ptsc.setY(-x[2] / (2.0 * x[0]));

                help = (double) ptsc.getX() * ptsc.getX() + ptsc.getY() * ptsc.getY();
                help = help - (double) x[3] / (x[0]);

                if (help >= 0)
                    ptsc.setV(Math.sqrt(help));
                else {
                    ptsc.setV(-Math.sqrt(-help));
                    logger.debug("Error negative radius ? " + ptsc.getV());
                    ptsc.setV(-1);

                    ret = false;
                }
            } else {
                ptsc.setV(-2.0);
                logger.debug("Warning: almost infinite radius of a circle\n");

                ret = false;
            }
        } else {
            ret = false;
        }
        return ret;
    }

    ///////////////////////////////////////////////////////////////////
    // a least-mean square fit to an exponencial curve expressed
    // as f(x) = A*exp(B*x);
    // it returns two values
    ///////////////////////////////////////////////////////////////////
    public boolean lms_expfit(int numofpoints, int delta, int slide, short top,
                              ImPoint[] pts, ImPoint res) {

        ImLine line = null;
        ImPoint[] pts1 = null;
        int i, j, idx, end;
        double help;
        boolean ret = true;

        //find min value
        float min = (float) pts[0].getY();
        end = numofpoints * delta;
        for (i = slide; i < slide + end; i += delta) {
            if (i >= top) {
                //help=(double) fmod( (double) i, (double) top );
                help = Math.IEEEremainder((double) i, (double) top);
                j = (int) help;
            } else
                j = i;

            if (pts[j].getY() < min)
                min = (float) pts[j].getY();
        }
        min--;

        //transform to a linear problem
        pts1 = new ImPoint[numofpoints];
        for (idx = 0; idx < numofpoints; idx++)
            pts1[idx] = new ImPoint();

        idx = 0;
        for (i = slide; i < slide + end; i += delta) {
            if (i >= top) {
                //help=(double) fmod( (double) i, (double) top );
                help = Math.IEEEremainder((double) i, (double) top);
                j = (int) help;
            } else
                j = i;

            pts1[idx].setX(pts[j].getX());

            //    pts1[idx].y = log(pts[j].y-min);
            if (pts[j].getY() > 0)
                pts1[idx].setY(Math.log(pts[j].getY()));
            else {
                pts1[idx].setY(0.0);
                logger.debug("Warning: the value is less than zero ");
            }
            idx++;
        }
        //test
        logger.debug("Selected points ");
        for (i = 0; i < numofpoints; i++)
            logger.debug("pts[" + i + "]=(" + pts1[i].getX() + ","
                         + Math.exp(pts1[i].getY()) + ")");

        line = lms_linefitY(numofpoints, 1, 0, (short) numofpoints, pts1);

        res.setX(0.0);
        res.setY(0.0);

        //result
        res.setX(Math.exp(line.getQ()));
        res.setY(line.getSlope());

        if (res.getY() < 0)
            logger.debug("The trend is  exp. decay ");
        else
            logger.debug("The trend is  exp. growth ");

        line = null;
        pts1 = null;

        if (ret) {
            //compute MSE or R^2 residual
            float sumy = 0.0F;
            float sumy2 = 0.0F;
            float sumdy = 0.0F;
            for (i = slide; i < slide + end; i += delta) {
                if (i >= top) {
                    //help=(double) fmod( (double) i, (double) top );
                    help = Math.IEEEremainder((double) i, (double) top);
                    j = (int) help;
                } else
                    j = i;
                help = (pts[j].getY() - res.getX() * Math.exp(res.getY() * pts[j].getX()));
                sumdy += help * help;
                sumy2 += pts[j].getY() * pts[j].getY();
                sumy += pts[j].getY();
            }
            //MSE
            //res.v = Math.sqrt(sumdy)/(float)numofpoints;

            //R^2 = 1- (SSE/SST), SSE = sumdy,SST = sumy2 -
            // sumy*sumy/numofpoints
            help = sumy * sumy / (float) numofpoints;
            if (help != sumy2)
                res.setV(1.0 - (sumdy / (sumy2 - help)));
            else {
                logger.debug("Warning: cannot compute the residual ");
                res.setV(0.0);
            }

        }
        return ret;
    }

    ///////////////////////////////////////////////////////////////////
    // Singular Value Decomposition
    // from Numerical recipies p 65
    // solves Ax = B for a vector X, where A is specified by u[mxn],w[n],v[mxn]
    // as returned by svdcmp.
    ///////////////////////////////////////////////////////////////////
    public void svbksb(ImageObject u, float[] w, ImageObject v, int numrows,
                       int numcols, float[] b, float[] x) {

        int jj, j, i;
        float s;
        float[] tmp = null;
        int index, index1;

        tmp = new float[numcols];

        for (j = 0; j < numcols; j++) { // calculate U^T times B
            index = j;
            s = 0.0F;
            if (w[j] != 0.0F) {
                for (i = 0; i < numrows; i++) {
                    s += u.getFloat(index) * b[i];
                    index += numcols;
                }
                s /= w[j];
            }
            tmp[j] = s;
        }

        index1 = 0;
        for (j = 0; j < numcols; j++) {
            index = index1;
            s = 0.0F;
            for (jj = 0; jj < numcols; jj++) {
                s += v.getFloat(index) * tmp[jj];
                index++;
            }
            x[j] = s;
            index1 += numcols;
        }

        tmp = null;
    }

    /*
     * /////////////////////////////////////////////////////////////////// //
     * Singular Value Decomposition // from Numerical recipies p 67 // solves Ax =
     * B for a vector x // Given a matrix a[mxn] computes singular value
     * decomposition A = U W V^T //The matrix U replace a on output The diagonal
     * matrix of singular // values W is output as a vector w[n]
     * ///////////////////////////////////////////////////////////////////
     * static float at,bt,ct; // PYTHAG computes Math.sqrt(a^2+b^2) without
     * destructive overflow #define PYTHAG(a,b) ((at=Math.abs(a)) >
     * (bt=Math.abs(b)) ? \ (ct = bt/at,at*Math.sqrt(1.0+ct*ct)) : (bt ?
     * (ct=at/bt,bt = Math.sqrt(1.0+ct*ct)) : 0.0))
     *
     * static float maxarg1,maxarg2; #define MAX(a,b) (maxarg1 = (a), maxarg2 =
     * (b), (maxarg1) > (maxarg2) ? \ (maxarg1) : (maxarg2)) #define SIGN(a,b)
     * ((b) >=0.0 ? Math.abs(a) : -Math.abs(a))
     *
     *
     * void MathOper::svdcmp(tMatrix <float> a, int numrows,int numcols,float
     * *w,tMatrix <float> v) { int flag,i,its,j,jj,k,l,nm; float c,f,h,s,x,y,z;
     * float anorm = 0.0; float g= 0.0; float scale = 0.0; float *rv1; long
     * index,index1,index2;
     *
     * if(numrows < numcols ){ logger.debug( "Error: SVDCMP: you must
     * augment A with extra zero rows " ); return; }
     *
     * rv1 = new float[numcols]; // householder reduction to bidiagonal form for
     * (i = 0;i <numcols;i++){ l = i+1; rv1[i] = scale *g; g = s = scale = 0.0;
     * if(i <numrows){ index = i*numcols+i; for(k=i;k <numrows;k++){ // index =
     * k*numcols + i; scale += Math.abs(a._data[index]); // a[k][i]
     * index+=numcols; } if(scale){ index = i*numcols+i; for(k=i;k
     * <numrows;k++){ a._data[index] /= scale;// a[k][i] s+=
     * a._data[index]*a._data[index]; index += numcols; } index = i*numcols+i; f =
     * a._data[index];// a[i][i] g = -SIGN(Math.sqrt(s),f); h = f*g-s;
     * a._data[index] = f - g; // a[i][i] if(i != numcols-1){ for (j= l;j
     * <numcols;j++){ index = i*numcols+j; index1 = i*numcols +i;
     * for(s=0.0,k=i;k <numrows;k++){ s+= a._data[index1] * a._data[index];
     * //a[k][i] * a[k][j] index += numcols; index1 += numcols; } f = s/h; index =
     * i*numcols+j; index1 = i*numcols +i; for(k=i;k <numrows;k++){ //a[k][j] +=
     * f*a[k][i] a._data[index] += f*a._data[index1]; index+= numcols; index1+=
     * numcols; } } } index =i*numcols+i; for(k=i;k <numrows;k++){ // a[k][i]
     * *=scale; a._data[index] *=scale; index += numcols; } } } w[i] = scale*g;
     * g = s = scale = 0.0; if(i < numrows && i != numcols-1){ index =
     * i*numcols+l; for(k = l; k < numcols;k++){ scale +=
     * Math.abs(a._data[index]); // a[i][k] index++; } if(scale){ index =
     * i*numcols + l; for(k=l;k <numcols;k++){ a._data[index] /= scale ; //
     * a[i][k] s += a._data[index]*a._data[index] ; // a[i][k] * a[i][k]
     * index++; } index = i*numcols + l; f = a._data[index]; // a[i][l] g =
     * -SIGN(Math.sqrt(s),f); h = f*g-s; a._data[index] = f - g; // a[i][l]
     * index = i*numcols+l; for(k=l;k <numcols;k++){ rv1[k] = a._data[index]/h; //
     * a[i][k] index++; } if(i != numrows-1){ for(j=l;j <numrows;j++){ index =
     * j*numcols+l; index1 = i*numcols + l; for(s=0.0,k=l;k <numcols;k++){ //
     * s+= a[j][k] * a[i][k]; s+= a._data[index] * a._data[index1]; index++;
     * index1++; } index = j*numcols + l; for(k = l; k <numcols;k++){ // a[j][k] +=
     * s*rv1[k]; a._data[index] += s*rv1[k]; index++; } } } index = i*numcols+l;
     * for(k=l;k <numcols;k++){ //a[i][k] *= scale; a._data[index] *= scale;
     * index++; } } } anorm = MAX(anorm,(Math.abs(w[i])+Math.abs(rv1[i]))); }
     *
     * //test logger.debug( "bidiagonal a matrix " ); logger.debug(
     * a );
     *
     * //accumulation of right hand transformation for(i=numcols-1;i>=0;i--){
     * if(i <numcols-1){ if(g){ index = l*numcols+i; index1 = index2 =
     * i*numcols+l; for(j = l;j <numcols;j++){ //v[j][i] = (a[i][j]/a[i][l])/g;
     * v._data[index] = (a._data[index1]/a._data[index2])/g; index += numcols;
     * index1 ++; } for(j=l;j <numcols;j++){ index = i*numcols+l; index1 =
     * l*numcols+j; for(s=0.0,k=l;k <numcols;k++){ //s += a[i][k]*v[k][j]; s +=
     * a._data[index]*v._data[index1]; index++; index1+= numcols; } index =
     * l*numcols+j; index1 = index - j +i; for(k=l;k <numcols;k++){ //v[k][j] +=
     * s*v[k][i]; v._data[index] += s*v._data[index1]; index+=numcols;
     * index1+=numcols; } } } index = i*numcols+l; index1 = l*numcols+i;
     * for(j=l;j <numcols;j++){ //v[i][j]= v[j][i]=0.0; v._data[index]=
     * v._data[index1]=0.0; index++; index1+=numcols; } } index = i*numcols+i;
     * v._data[index] = 1.0;// v[i][i] g = rv1[i]; l=i; }
     *
     * //test logger.debug( "a matrix after right hand transformations " );
     * logger.debug( a );
     *  // accumulation of left hand transformation for(i=numcols-1;i>=0;i--){ l =
     * i+1; g=w[i]; if(i <numcols-1){ index = i*numcols+l; for(j=l;j
     * <numcols;j++){ //a[i][j]=0.0; a._data[index]=0.0; index++; } } if(g){ g =
     * 1.0/g; if(i!=numcols-1){ for(j=l;j <numcols;j++){ index = l*numcols+i;
     * index1 = index -i +j; for(s=0.0,k=l;k <numrows;k++){ //s+=a[k][i]
     * *a[k][j]; s+=a._data[index] *a._data[index1]; index1+=numcols;
     * index+=numcols; } //f=(s/a[i][i])*g; index = i*numcols+i;
     * f=(s/a._data[index])*g;
     *
     * index1 = index-i+j;//i*numcols+j; for(k=i;k <numrows;k++){ //a[k][j] +=
     * f*a[k][i]; a._data[index1] += f*a._data[index]; index+=numcols;
     * index1+=numcols; } } } index = i*numcols+i; for(j=i;j <numrows;j++){
     * //a[j][i] *=g; a._data[index] *=g; index+=numcols; }
     *
     * }else{ index = i*numcols+i; for(j=i;j <numrows;j++){ //a[j][i]=0.0;
     * a._data[index]=0.0; index+=numcols; } } //++a[i][i]; index = i*numcols+i;
     * ++a._data[index]; }
     *
     * //test logger.debug( "a matrix after left hand transformations " );
     * logger.debug( a );
     *
     * //diagonalization of the bidiagonal form for(k=numcols-1;k>=0;k--){//
     * loop over singular values for(its=1;its <=30;its++){//loop over allowed
     * iterations flag=1; for(l=k;l>=0;l--){ nm=l-1;
     * if((float)(Math.abs(rv1[l])+anorm) == anorm){ flag = 0; break; }
     * if((float)(Math.abs(w[nm])+anorm) == anorm) break; } if(flag){ c = 0.0; s =
     * 1.0; for(i=l;i <=k;i++){ f = s*rv1[i]; rv1[i] = c*rv1[i];
     * if((float)(Math.abs(f)+anorm) == anorm) break; g = w[i]; h = PYTHAG(f,g);
     * w[i] = h; h= 1.0/h; c = g*h; s = (-f*h); index = nm; index1 = i;
     * for(j=0;j <numrows;j++){ //y = a[j][nm]; y = a._data[index]; z =
     * a._data[index1];//a[j][i]; //a[j][nm] = y*c+z*s; a._data[index] =
     * y*c+z*s; //a[j][i] = z*c-y*s; a._data[index1] = z*c-y*s; index+=numcols;
     * index1+=numcols; } } } z = w[k];//convergence if(l==k){ if(z < 0.0){ w[k] =
     * -z; index = k; for(j=0;j <numcols;j++){ //v[j][k] = (-v[j][k]);
     * v._data[index] = (-v._data[index]); index += numcols; } } break; } if(its
     * ==30){ logger.debug( "Error: no convergence in 30 SVDCMP iterations " );
     * return; } x = w[l]; nm = k-1; y = w[nm]; g = rv1[nm]; h = rv1[k]; f =
     * ((y-z)*(y+z)+(g-h)*(g+h))/(2.0*h*y); g = PYTHAG(f,1.0); f =
     * ((x-z)*(x+z)+h*((y/(f+SIGN(g,f)))-h))/x; // next QR transformation c = s =
     * 1.0; for(j=l;j <=nm;j++){ i = j+1; g = rv1[i]; y = w[i]; h = s*g; g =
     * c*g; z = PYTHAG(f,h); rv1[j]=z; c = f/z; s = h/z; f = x*c+g*s; g =
     * g*c-x*s; h = y*s; y = y*c; index = j; index1 = i; for(jj=0;jj
     * <numcols;jj++){ //x = v[jj][j]; x = v._data[index]; //z = v[jj][i]; z =
     * v._data[index1]; //v[jj][j] = x*c+z*s; v._data[index] = x*c+z*s;
     * //v[jj][i] = z*c-x*s; v._data[index1] = z*c-x*s; index += numcols; index1 +=
     * numcols; } z = PYTHAG(f,h); w[j] = z; // rotation can arbitrary if z=0
     * if(z){ z = 1.0/z; c = f*z; s = h*z; } f = (c*g)+(s*y); x = (c*y)-(s*g);
     * index = j; index1 = i; for(jj=0;jj <numrows;jj++){ //y = a[jj][j]; y =
     * a._data[index]; //z = a[jj][i]; z = a._data[index1]; //a[jj][j]=y*c+z*s;
     * a._data[index]=y*c+z*s; //a[jj][i] =z*c-y*s; a._data[index1] =z*c-y*s;
     * index +=numcols; index1+= numcols; } } rv1[l] = 0.0; rv1[k] = f; w[k] =
     * x; } }
     *
     * //test logger.debug( "a matrix at the ned " ); logger.debug(
     * a );
     *
     * delete [] rv1; }
     *
     * /////////////////////////////////////////////////////////////////// //
     * general least square fit // from Numerical recipies p 537 // Given a set
     * of points pts[ndata] = (x[ndata],y[ndata]) // with individual st. dev
     * sig[ndata] // use chi^2 minimization to determine the coefficients a[ma]
     * of the fitting // function sum(a_i * afunct_i(x). Here we solve the
     * fitting equations using // SVD of the ndata by ma matrix. Arrays u,v and
     * w provide the workspace. // The program return for the ma fit parameters
     * a and chi^2 (chisq). // The user supplies a routine funcs(x,afunc,ma)
     * that return the ma basis // functions evaluated at X = x in the arrary
     * afunc[ma].
     * ///////////////////////////////////////////////////////////////////
     * #define TOL 1.0e-5 public void svdfit(ImPoint [] pts, float *sig,int
     * ndata,float *a,int ma,tMatrix <float> u,tMatrix <float> v, float *w,float
     * *chisq) {
     *
     * int j,i; float wmax,tmp,thresh,sum,*b,*afunc; long index;
     *
     * b = new float[ndata]; afunc = new float[ma];
     *
     * for(i=0;i <ndata;i++){ GetFunctionEval(pts[i].x,afunc,ma); tmp =
     * 1.0/sig[i]; index = i*ma; for(j=0;j <ma;j++){ // u[i][j] = afunc[j]*tmp;
     * u._data[index] = afunc[j]*tmp; index++; } b[i] = pts[i].y * tmp; } //test
     * logger.debug( "u matrix" ); logger.debug( u );
     *
     * svdcmp(u,ndata,ma,w,v); //test logger.debug( "w vector " );
     * for(i=0;i <ma;i++) logger.debug( w[i] < < " "; logger.debug(
     * endl; logger.debug( "v matrix" ); logger.debug( v );
     *
     * wmax = 0.0; for(j=0;j <ma;j++){ if(w[j] > wmax) wmax = w[j]; } thresh =
     * TOL * wmax; for (j= 0;j <ma;j++){ if(w[j] < thresh) w[j] = 0.0; }
     * svbksb(u,w,v,ndata,ma,b,a); // evaluate chi square chisq = 0.0; for(i=0;i
     * <ndata;i++){ GetFunctionEval(pts[i].x,afunc,ma); for(sum = 0.0,j=0;j
     * <ma;j++){ sum += a[j]*afunc[j]; } chisq += (tmp=(pts[i].y -
     * sum)/sig[i],tmp*tmp); } delete [] afunc; delete [] b; }
     *  // here is an example of a user supplied function for the least square
     * fit // fits a polynomial of np-1 order void
     * MathOper::GetFunctionEval(float x,float *p,int np) { int j;
     *
     * p[0] = 1.0; for(j=1;j <np;j++) p[j]=p[j-1]*x; }
     *
     */

    ////////////////////////////////////////////////////////////
    //display values
    public void PrintMathOper() {
        //logger.debug("MathOper Info :numrows=" + numrows+ " numcols=" +
        // numcols + " sampPerPixel=" + sampPerPixel);
        logger.debug("MathOper Info :");
    }

}

