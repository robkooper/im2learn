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
package edu.illinois.ncsa.isda.im2learn.ext.statistics.pdf;

import java.text.NumberFormat;
import java.util.Random;

// TODO: documentation
// TODO: should add lambda back in, and calculate gamma*
// TODO: find invalid values for lambda etc.

/**
 * JohnsonSu will implement a Johnson Su distribution function.
 *
 * @author Rob Kooper
 * @version 1.0 (December 5, 2003)
 */
public class JohnsonSl implements ProbabilityDistribution1D {
    private double eta = Double.NaN;
    private double gamma = Double.NaN;
    private double epsilon = Double.NaN;

    /**
     * random number generator used in this class.
     */
    private Random random = new Random();

    /**
     * useful constant.
     */
    final private double sqrt2PI = Math.sqrt(2.0 * Math.PI);

    public JohnsonSl() throws Exception {
        this(0, 0.5, 0.5);
    }

    public JohnsonSl(double epsilon, double eta, double gamma) throws Exception {
        this.epsilon = epsilon;
        this.eta = eta;
        this.gamma = gamma;
    }


    /**
     * Returns the eta of the distribution
     *
     * @return eta
     */
    public double getEta() {
        return eta;
    }

    /**
     * Sets the new eta of the distribution
     *
     * @param eta
     */
    public void setEta(double eta) {
        this.eta = eta;
    }

    /**
     * Returns the gamma of the distribution
     *
     * @return gamma
     */
    public double getGamma() {
        return gamma;
    }

    /**
     * Sets the new gamma of the distribution
     *
     * @param gamma
     */
    public void setGamma(double gamma) {
        this.gamma = gamma;
    }

    /**
     * Returns the epsilon of the distribution
     *
     * @return epsilon
     */
    public double getEpsilon() {
        return epsilon;
    }

    public void setEpsilon(double epsilon) {
        this.epsilon = epsilon;
    }

    /**
     * Based on the dataset, calculate the parameters to fit this distribution.
     *
     * @param data is dataset on which to base the distribution
     */
    public void estimateParameters(double[] data) throws Exception {
        // TODO: implement estimator
        throw(new Exception("Not implemented."));
    }

    /**
     * Generate datapoints that fit the Johnson Sl distribution. This function
     * will fill the data array with values that fit the Johnson Sl
     * distribution.
     *
     * @param data to be filled with new random points
     */
    public void generateData(double[] data) {
        for (int i = 0; i < data.length; i++)
            data[i] = generate();
    }

    /**
     * Generate a random sample from the PDF.
     *
     * @return a single random number
     */
    public double generate() {
        return Math.exp((random.nextGaussian() - gamma) / eta) + epsilon;
    }

    /**
     * Calculate the probability of X=x.
     *
     * @param x value for which to calculate the probability
     * @return the probability of X=x
     */
    public double calculatePDF(double x) {
        double xe = x - epsilon;

        double s = gamma / eta + Math.log(xe);
        double r = eta / (sqrt2PI * xe) * Math.exp(-0.5 * eta * eta * s * s);

        if (Double.isNaN(r)) {
            return 0;
        } else {
            return r;
        }
    }

    /**
     * Calculate the cummalative distribution of X<=x.
     *
     * @param x value for which to calculate the cummalitive distribution
     * @return the cummalative distribution of X<=x
     */
    public double calculateCDF(double x) {
        return calculateCDF(Double.NEGATIVE_INFINITY, x);
    }

    /**
     * Calculate the probability of a<=X<=b.
     *
     * @param a lowerlimit for which to calculate the probability
     * @param b upperlimit for which to calculate the probability
     * @return the probability of a<=X<=b
     */
    public double calculateCDF(double a, double b) {
        if (b <= a)
            return 0;
        if (b == Double.POSITIVE_INFINITY)
            b = epsilon + 100;
        if (a == Double.NEGATIVE_INFINITY)
            a = epsilon - 100;

        // TODO : Check correctness
        double result = 0;
        double step = (b - a) / 100.0;
        for (double x = a; x < b; x += step) {
            result += calculatePDF(x);
        }
        return result * step;
    }

    /**
     * This function will return the value for x, such that the calculateCDF(x)
     * ~= cdf The implementation will return an approximation of the value.
     *
     * @param cdf that we want to find
     * @return the x such that the calculateCDF(x) ~= cdf
     */
    public double inverseCDF(double cdf) {
        double step = 1;
        double val = 0;
        double lcdf = 0;
        double ucdf = 0;
        double vcdf = 0;

        // ALGORITHM
        // The idea is to find a range in which we know the cdf lies, in this case we
        // are looking for CDF(val-step) <= cdf <= CDF(val+step).
        // Next we calculate the CDF(val) and see if that is below or above cdf. Based
        // on this we know if the cdf val is in <CDF(val-step), CDF(val)> or in
        // <CDF(val), CDF(val+step)>. Next step we half the search space by setting
        // step to be half, and val to be middle of new interval. Repeat until we
        // have either a value close enough, or did enough iterations.
        //
        // This algorithm should be generic enough to use for anything, just have to
        // make sure that you pick a reasonable start value and a reasonable step
        // value. Also make sure that CDF calculation understands -INF.

        // find initial range
        do {
            if (lcdf != 0 || ucdf != 0) {
                if (cdf < lcdf) {
                    val -= step;
                } else {
                    val += step;
                }
            }
            ucdf = calculateCDF(val + step);
            lcdf = calculateCDF(val - step);
        } while (cdf > ucdf || cdf < lcdf);

        // minimize error
        vcdf = calculateCDF(val);
        for (int i = 0; i < 20 && Math.abs(vcdf - cdf) > 1e-7; i++) {
            step /= 2.0;
            if (cdf < vcdf) {
                val -= step;
            } else {
                val += step;
            }
            vcdf = calculateCDF(val);
        }
        return val;
    }

    /**
     * Returns the exact mean of the PDF
     *
     * @return mean
     */
    public double getMean() {
        return Double.NaN;
    }

    /**
     * Returns the exact median of the PDF
     *
     * @return median
     */
    public double getMedian() {
        return Double.NaN;
    }

    /**
     * Returns the exact standard deviation of the PDF
     *
     * @return standard deviation
     */
    public double getStandardDeviation() {
        return Double.NaN;
    }

    /**
     * Returns the exact coefficient of variation of the PDF coefficient of
     * variation = standard deviation / mean
     *
     * @return variance
     */
    public double getCoefficientOfVariation() {
        return Double.NaN;
    }

    /**
     * Returns the exact skew of the PDF
     *
     * @return skew
     */
    public double getSkewness() {
        return Double.NaN;
    }

    /**
     * Returns the exact kurtosis of the PDF
     *
     * @return kurtosis
     */
    public double getKurtosis() {
        return Double.NaN;
    }

    /**
     * Return a description of the distribution.
     *
     * @return description of distribtution.
     */
    public String toString() {
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(3);
        return "Johnson Sl Distribution with param [" + nf.format(epsilon) + " " + nf.format(eta) + " " +
               nf.format(gamma) + "].";
    }
}
