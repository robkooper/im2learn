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

/**
 * Weibull1D will implement a weibull distribution function. The pdf for a
 * weibull distribution is: f(x) = gamma/alpha * ((x - offset)/alpha)^(gamma-1)
 * * e^(-((x - offset) / alpha)^gamma) x >= mean and gamma, alpha > 0
 *
 * @author Peter Bajcsy, Rob Kooper
 * @version 1.0 (June 30, 2003)
 */
public class Weibull1D implements ProbabilityDistribution1D {
    /**
     * Current version number and more info.
     */
    public final static String CVS_ID = "$Id: Weibull1D.java,v 1.4 2006-12-07 17:00:05 kooper Exp $";

    /**
     * Shape of the distribution.
     */
    private double gamma = Double.NaN;

    /**
     * Scale of the distribution.
     */
    private double alpha = Double.NaN;

    /**
     * Offset of the distribution.
     */
    private double offset = Double.NaN;

    /**
     * precalculated ln(2)^(1/gamma)
     */
    private double ln2gamma = Double.NaN;

    /**
     * precalculated Gamma.complete((gamma+1) / gamma);
     */
    private double gamma1 = Double.NaN;

    /**
     * precalculated Gamma.complete((gamma+2) / gamma);
     */
    private double gamma2 = Double.NaN;

    /**
     * precalculated Gamma.complete((gamma+3) / gamma);
     */
    private double gamma3 = Double.NaN;

    /**
     * precalculated Gamma.complete((gamma+4) / gamma);
     */
    private double gamma4 = Double.NaN;

    /**
     * Create a weibull distribution with gamma of 2, alpha of 1 and offset of
     * 0.
     *
     * @throws Exception if standard deviation is less or equal to 0.
     */
    public Weibull1D() throws Exception {
        this(2, 1, 0);
    }

    public Weibull1D(double gamma) throws Exception {
        this(gamma, 1, 0);
    }

    /**
     * Create a weibull distribution with given mean and standard deviation.
     *
     * @param gamma  is the shape of the distribtution
     * @param alpha  is the scale of the distribution
     * @param offset is the location of the distribution
     * @throws Exception if gamma or alpha is less or equal to 0
     */
    public Weibull1D(double gamma, double alpha, double offset) throws Exception {
        if (gamma <= 0)
            throw(new Exception("gamma has to be greater than 0."));
        if (alpha <= 0)
            throw(new Exception("alpha has to be greater than 0."));

        this.gamma = gamma;
        this.alpha = alpha;
        this.offset = offset;
        this.ln2gamma = Math.pow(Math.log(2), 1 / gamma);
        this.gamma1 = Gamma.complete((gamma + 1) / gamma);
        this.gamma2 = Gamma.complete((gamma + 2) / gamma);
        this.gamma3 = Gamma.complete((gamma + 3) / gamma);
        this.gamma4 = Gamma.complete((gamma + 4) / gamma);
    }

    /**
     * returns the gamma, shape, of the distribution
     *
     * @return gamma
     */
    public double getGamma() {
        return gamma;
    }

    /**
     * sets the gamma, shape, of the distribution
     *
     * @param gamma is the new shape
     * @throws Exception if gamma is less or equal to 0.
     */
    public void setGamma(double gamma) throws Exception {
        if (gamma <= 0)
            throw(new Exception("gamma has to be greater than 0."));

        this.gamma = gamma;
        this.ln2gamma = Math.pow(Math.log(2), 1 / gamma);
        this.gamma1 = Gamma.complete((gamma + 1) / gamma);
        this.gamma2 = Gamma.complete((gamma + 2) / gamma);
        this.gamma3 = Gamma.complete((gamma + 3) / gamma);
        this.gamma4 = Gamma.complete((gamma + 4) / gamma);
    }

    /**
     * returns the alpha, scale, of the distribution
     *
     * @return alpha
     */
    public double getAlpha() {
        return alpha;
    }

    /**
     * sets the alpha, scale, of the distribution
     *
     * @param alpha is the new scale
     * @throws Exception if gamma is less or equal to 0.
     */
    public void setAlpha(double alpha) throws Exception {
        if (alpha <= 0)
            throw(new Exception("alpha has to be greater than 0."));

        this.alpha = alpha;
    }

    /**
     * returns the offset, location, of the distribution
     *
     * @return offset
     */
    public double getOffset() {
        return offset;
    }

    /**
     * sets the offset, location, of the distribution
     *
     * @param offset is the new location
     */
    public void setOffset(double offset) {
        this.offset = offset;
    }

    /**
     * Estimate the parameters of a gaussian distribution that fits the given
     * data set.
     *
     * @param data used to estimate the mean and standard deviation.
     */
    public void estimateParameters(double[] data) throws Exception {
        estimateParametersTransform(data);
        this.ln2gamma = Math.pow(Math.log(2), 1 / gamma);
        this.gamma1 = Gamma.complete((gamma + 1) / gamma);
        this.gamma2 = Gamma.complete((gamma + 2) / gamma);
        this.gamma3 = Gamma.complete((gamma + 3) / gamma);
        this.gamma4 = Gamma.complete((gamma + 4) / gamma);
    }

    /**
     * Fill the array with random data fitting the Weibull PDF
     *
     * @param data to be filled with new random points
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
        double rand = 0;
        double shape = 1.0 / gamma;

        rand = Math.random();
        if (rand == 1.0)
            rand = rand - 1e-7; // Uniform(); this could still produce 1.0 PB
        if (rand == 0.0)
            rand = rand + 1e-7; // Uniform(); this could still produce 0.0 PB

        return (alpha * Math.pow(-Math.log(rand), shape)) + offset;
    }

    /**
     * Calculate the probablity of x ( Pr(X=x) ).
     *
     * @param x the value for which to calculate the probability.
     * @return the probability.
     */
    public double calculatePDF(double x) {
        double tmp = gamma / alpha * Math.pow(((x - offset) / alpha), (gamma - 1));
        return tmp * Math.exp(-Math.pow(((x - offset) / alpha), gamma));
    }

    /**
     * Calculate the cummalative distribution of X<=x.
     *
     * @param x value for which to calculate the cummalitive distribution
     * @return the cummalative distribution of X<=x
     */
    public double calculateCDF(double x) {
        if (x < offset)
            return 0;

        return 1 - Math.exp(-Math.pow(((x - offset) / alpha), gamma));
    }

    /**
     * Calculate the probability of a<=X<=b.
     *
     * @param a lowerlimit for which to calculate the probability
     * @param b upperlimit for which to calculate the probability
     * @return the probability of a<=X<=b
     */
    public double calculateCDF(double a, double b) {
        if ((b <= a) || (b < offset))
            return 0;

        if (a < offset) {
            return calculateCDF(b);
        } else {
            return calculateCDF(b) - calculateCDF(a);
        }
    }

    /**
     * This function will return the value for x, such that the calculateCDF(x)
     * ~= cdf
     *
     * @param cdf that we want to find
     * @return the x such that the calculateCDF(x) ~= cdf
     */
    public double inverseCDF(double cdf) {
        if (cdf <= 0)
            return offset;
        if (cdf >= 1)
            return Double.POSITIVE_INFINITY;

        return offset + alpha * Math.pow(-Math.log(1 - cdf), 1.0 / gamma);
    }

    /**
     * Returns the exact mean of the PDF
     *
     * @return mean
     */
    public double getMean() {
        return offset + alpha * gamma1;
    }

    /**
     * Returns the exact median of the PDF
     *
     * @return median
     */
    public double getMedian() {
        return offset + alpha * ln2gamma;
    }

    /**
     * Returns the exact standard deviation of the PDF
     *
     * @return standard deviation
     */
    public double getStandardDeviation() {
        return alpha * Math.sqrt(gamma2 - (gamma1 * gamma1));
    }

    /**
     * Returns the exact coefficient of variation of the PDF coefficient of
     * variation = standard deviation / mean
     *
     * @return variance
     */
    public double getCoefficientOfVariation() {
        return Math.sqrt((gamma2 / (gamma1 * gamma1)) - 1);
    }

    /**
     * Returns the exact skew of the PDF
     *
     * @return skew
     */
    public double getSkewness() {
        double div = gamma2 - (gamma1 * gamma1);
        return (gamma3 - 3 * gamma2 * gamma1 + 2 * gamma1 * gamma1 * gamma1) / Math.sqrt(div * div * div);
    }

    /**
     * Returns the exact kurtosis of the PDF
     *
     * @return kurtosis
     */
    public double getKurtosis() {
        double div = gamma2 - (gamma1 * gamma1);
        return (gamma4 - 4 * gamma3 * gamma1 + 6 * gamma2 * gamma1 * gamma1 - 3 * gamma1 * gamma1 * gamma1 * gamma1) / (div * div);
    }

    /**
     * Return a description of the distribution.
     *
     * @return description of distribtution.
     */
    public String toString() {
        return "Weibull1D Distribution with param [" + gamma + " " + alpha + " " + offset + "].";
    }

    /**
     * Estimate the weibull parameters. This algorithm is based on the work
     * published by M. V. Menon, "Esitmation of the shape and scale parameters
     * of the Weibull distribution", Technometrics, 5, pp 175-182, 1963
     *
     * @param data is the data on which to base the estimation
     * @throws Exception if calculated gamma or alpha is less than or equal to
     *                   0.
     */
    private void estimateParametersTransform(double[] data) throws Exception {
        double sum = 0;
        double sum2 = 0;
        int len = data.length;
        double tmp;
        int i;

        // Find the minimum value in the dataset, this will be the offset
        offset = Double.POSITIVE_INFINITY;
        for (i = 0; i < len; i++) {
            if (data[i] < offset) offset = data[i];
        }
        offset -= 1e-7;

        // Precalculate the Sum(ln(x-min)) and Sum(ln(x-min)^2)
        for (i = 0; i < len; i++) {
            tmp = Math.log(data[i] - offset);
            sum += tmp;
            sum2 += tmp * tmp;
        }

        // calculate the shape
        tmp = sum2 - sum * sum / len;
        gamma = 1.0 / Math.sqrt(((6.0 * tmp) / (Math.PI * Math.PI)) / (len - 1));
        if (gamma <= 0)
            throw(new Exception("gamma has to be greater than 0."));

        // calculate the scale
        alpha = Math.exp(sum / len + (0.5772 / gamma));
        if (alpha <= 0)
            throw(new Exception("alpha has to be greater than 0."));
    }
}
