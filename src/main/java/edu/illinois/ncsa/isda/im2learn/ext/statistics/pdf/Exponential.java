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

/**
 * Exponential will implement a exponential distribution function. The pdf for a
 * exponential distribution is:
 * <p/>
 * f(x) = lambda * e^(-(x-offset) * lambda) x >= offset and lambda > 0
 *
 * @author Rob Kooper
 * @version 1.0 (June 28, 2003)
 */
public class Exponential implements ProbabilityDistribution1D {
    /**
     * lambda of the distribution.
     */
    private double lambda = Double.NaN;

    /**
     * offset of the distribution
     */
    private double offset = Double.NaN;

    /**
     * Create a exponential distribution with lambda 1 and offset 0.
     */
    public Exponential() throws Exception {
        this(1, 0);
    }

    /**
     * Create a exponential distribution with given lambda and no offset
     */
    public Exponential(double lambda) throws Exception {
        this(lambda, 0);
    }

    /**
     * Create a exponential distribution with given lambda.
     *
     * @param lambda of the new exponential distribution.
     * @throws Exception if lambda is less or equal to 0.
     */
    public Exponential(double lambda, double offset) throws Exception {
        if (lambda <= 0)
            throw(new Exception("Invalid Lambda, has to be greater than 0."));
        this.lambda = lambda;
        this.offset = offset;
    }

    /**
     * Return lambda of the distribution.
     *
     * @return value of lambda
     */
    public double getLambda() {
        return lambda;
    }

    /**
     * Sets the lambda of the distribution.
     *
     * @param lambda the new lambda.
     * @throws Exception if lambda is less or equal to 0.
     */
    public void setLambda(double lambda) throws Exception {
        if (lambda <= 0)
            throw(new Exception("Invalid Lambda, has to be greater than 0."));
        this.lambda = lambda;
    }

    /**
     * Return the offset of the distribution
     *
     * @return offset
     */
    public double getOffset() {
        return offset;
    }

    /**
     * Set the offset of the distribution
     *
     * @param offset the new offset
     */
    public void setOffset(double offset) {
        this.offset = offset;
    }

    /**
     * Estimate the parameters of a eponential distribution that fits the given
     * data set.
     *
     * @param data used to estimate the mean and standard deviation.
     */
    public void estimateParameters(double[] data) {
        double[] stats = new Estimator().calculate(data);
        this.offset = stats[Estimator.MIN];
        this.lambda = 1 / (stats[Estimator.MEAN] - offset);
    }

    /**
     * Generate datapoints that fit the exponential distribution. This function
     * will fill the data array with values that fit the exponential
     * distribution. The points are generated using the inverse of the pdf<br>
     * f(x) = offset - 1/lambda * log(1 - random)
     *
     * @param data to be filled with new random points
     */
    public void generateData(double[] data) {
        for (int i = 0; i < data.length; i++) {
            data[i] = offset - Math.log(Math.random()) / lambda;
        }
    }

    /**
     * Generate a random sample from the PDF.
     *
     * @return a single random number
     */
    public double generate() {
        return offset - Math.log(Math.random()) / lambda;
    }

    /**
     * Calculate the probablity of x ( Pr(X=x) ). The implementation will return
     * the exact value.
     *
     * @param x the value for which to calculate the probability.
     * @return the probability.
     */
    public double calculatePDF(double x) {
        if (x < offset)
            return 0;
        return lambda * Math.exp((offset - x) * lambda);
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
        return 1 - Math.exp((offset - x) * lambda);
    }

    /**
     * Calculate the probability of a<=X<=b. The implementation will return the
     * exact value.
     *
     * @param a lowerlimit for which to calculate the probability
     * @param b upperlimit for which to calculate the probability
     * @return the probability of a<=X<=b
     */
    public double calculateCDF(double a, double b) {
        if ((b < a) || (b < offset))
            return 0;

        if (a <= offset) {
            return 1 - Math.exp((offset - b) * lambda);
        } else {
            return Math.exp((offset - a) * lambda) - Math.exp((offset - b) * lambda);
        }
    }

    /**
     * This function will return the value for x, such that the calculateCDF(x)
     * ~= cdf The implementation will return the exact value.
     *
     * @param cdf that we want to find
     * @return the x such that the calculateCDF(x) ~= cdf
     */
    public double inverseCDF(double cdf) {
        return offset - Math.log(1 - cdf) / lambda;
    }

    /**
     * Returns the exact mean of the PDF
     *
     * @return mean
     */
    public double getMean() {
        return offset + 1.0 / lambda;
    }

    /**
     * Returns the exact median of the PDF
     *
     * @return median
     */
    public double getMedian() {
        return offset + 1.0 / lambda * Math.log(2);
    }

    /**
     * Returns the exact standard deviation of the PDF
     *
     * @return standard deviation
     */
    public double getStandardDeviation() {
        return 1.0 / lambda;
    }

    /**
     * Returns the exact coefficient of variation of the PDF coefficient of
     * variation = standard deviation / mean
     *
     * @return variance
     */
    public double getCoefficientOfVariation() {
        return 1.0;
    }

    /**
     * Returns the exact skew of the PDF
     *
     * @return skew
     */
    public double getSkewness() {
        return 2.0;
    }

    /**
     * Returns the exact kurtosis of the PDF
     *
     * @return kurtosis
     */
    public double getKurtosis() {
        return 9.0;
    }

    /**
     * Return a description of the distribution.
     *
     * @return description of distribtution.
     */
    public String toString() {
        return "Exponential Distribution with param [" + lambda + " " + offset + "].";
    }
}
