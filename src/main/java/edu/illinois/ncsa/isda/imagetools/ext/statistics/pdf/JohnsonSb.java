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
package edu.illinois.ncsa.isda.imagetools.ext.statistics.pdf;

import java.text.NumberFormat;
import java.util.Random;

// TODO: documentation

/**
 * Johnson Sb will implement a Johnson Sb distribution function. The Johnson Sb
 * is part of the Johnson family of distributions. The general form is:
 * <p/>
 * z = gamma + eta * tau(x; epsilon, lambda)
 * <p/>
 * Using three different formula's for tau the whole distribtution spectrum can
 * be covered. Johnson Sb is the second function of tau:
 * <p/>
 * tau(x; epsilon, lambda) = ln( (x-epsilon) / (lambda+epsilon-x) ) for epsilon
 * <= x <= epsilon+lambda
 *
 * @author Rob Kooper
 * @version 1.0 (June 30, 2003)
 */
public class JohnsonSb implements ProbabilityDistribution1D {
    private double lambda = Double.NaN;
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

    /**
     * Create a Johnson Sb distribution with lambda 1, epsilon 0, eta and gamma
     * 0.5.
     *
     * @throws Exception if can not create pdf.
     */
    public JohnsonSb() throws Exception {
        this(1, 0, 0.5, 0.5);
    }

    /**
     * Create a Johnson Sb distribution with give lambda, epsilon, eta and
     * gamma.
     *
     * @throws Exception if can not create pdf.
     */
    public JohnsonSb(double lambda, double epsilon, double eta, double gamma) throws Exception {
        this.lambda = lambda;
        this.epsilon = epsilon;
        this.eta = eta;
        this.gamma = gamma;
    }

    /**
     * Return the lambda of the distribution
     *
     * @return lambda
     */
    public double getLambda() {
        return lambda;
    }

    /**
     * Set lambda of the distribution
     *
     * @param lambda to be set as new value
     */
    public void setLambda(double lambda) {
        this.lambda = lambda;
    }

    /**
     * Return the eta of the distribution
     *
     * @return lambda
     */
    public double getEta() {
        return eta;
    }

    /**
     * Set eta of the distribution
     *
     * @param eta to be set as new value
     */
    public void setEta(double eta) {
        this.eta = eta;
    }

    /**
     * Return the gamma of the distribution
     *
     * @return lambda
     */
    public double getGamma() {
        return gamma;
    }

    /**
     * Set gamma of the distribution
     *
     * @param gamma to be set as new value
     */
    public void setGamma(double gamma) {
        this.gamma = gamma;
    }

    /**
     * Return the epsilon of the distribution
     *
     * @return lambda
     */
    public double getEpsilon() {
        return epsilon;
    }

    /**
     * Set epsilon of the distribution
     *
     * @param epsilon to be set as new value
     */
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
     * Given the PDF fill the array with random numbers.
     *
     * @param data will be filled with random numbers.
     */
    public void generateData(double[] data) {
        for (int i = 0; i < data.length; i++) {
            data[i] = generate();
        }
    }

    /**
     * Generate a random sample from the PDF.
     *
     * @return a single random number
     */
    public double generate() {
        double d = 1 / Math.exp((random.nextGaussian() - gamma) / eta);
        return lambda / (1 + d) + epsilon;
    }

    /**
     * Calculate the probability of X=x.
     *
     * @param x value for which to calculate the probability
     * @return the probability of X=x
     */
    public double calculatePDF(double x) {
        double xe = x - epsilon;
        double lxe = lambda - xe;

        double s = gamma + eta * Math.log(xe / lxe);
        double r = eta / sqrt2PI * lambda / (xe * lxe) * Math.exp(-0.5 * s * s);

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
        return "Johnson Sb Distribution with param [" + nf.format(lambda) + " " + nf.format(epsilon) + " " +
               nf.format(eta) + " " + nf.format(gamma) + "].";
    }
}
