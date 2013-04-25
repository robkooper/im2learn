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
 * Uniform will implement a uniform distribution function. The pdf for a uniform
 * distribution is:
 * <p/>
 * f(x) = 1 / (max - min) min<=x<=max
 *
 * @author Rob Kooper
 * @version 1.0 (June 28, 2003)
 */
public class Uniform implements ProbabilityDistribution1D {
    /**
     * Minimum value of the distribution.
     */
    private double min = Double.NaN;

    /**
     * Maximum value of the distribution.
     */
    private double max = Double.NaN;

    /**
     * Precalculate the difference between max and min.
     */
    private double diff = Double.NaN;

    /**
     * Creates a new uniform between 0 and 1.
     *
     * @throws Exception if the minimum is larger or equal than the maximum.
     */
    public Uniform() throws Exception {
        this(0, 1);
    }

    /**
     * Creates a new uniform between min and max.
     *
     * @throws Exception if the minimum is larger or equal to the maximum.
     */
    public Uniform(double min, double max) throws Exception {
        if (min > max)
            throw(new Exception("min has to be less then max."));
        this.min = min;
        this.max = max;
        this.diff = max - min;
    }

    /**
     * Returns the minimum of the distribution.
     *
     * @return minimum
     */
    public double getMin() {
        return min;
    }

    /**
     * Sets the minimum of the distribution
     *
     * @param min new minimum value
     * @throws Exception if the minimum is larger or equal to the maximum.
     */
    public void setMin(double min) throws Exception {
        if (min > max)
            throw(new Exception("min has to be less then max."));
        this.min = min;
        this.diff = max - min;
    }

    /**
     * Returns the maximum of the distribution.
     *
     * @return maximum
     */
    public double getMax() {
        return max;
    }

    /**
     * Sets the maximum of the distribution
     *
     * @param max new maximum value
     * @throws Exception if the minimum is larger or equal to the maximum.
     */
    public void setMax(double max) throws Exception {
        if (min > max)
            throw(new Exception("min has to be less then max."));
        this.max = max;
        this.diff = max - min;
    }

    /**
     * Estimate the parameters of a uniform distribution that fits the given
     * data set.
     *
     * @param data used to estimate the mean and standard deviation.
     */
    public void estimateParameters(double[] data) throws Exception {
        double[] stats = new Estimator().calculate(data);

        min = stats[Estimator.MEAN] - Math.sqrt(3) * stats[Estimator.STDDEV];
        max = stats[Estimator.MEAN] + Math.sqrt(3) * stats[Estimator.STDDEV];
        if (min > max)
            throw(new Exception("min has to be less then max."));
        diff = max - min;
    }

    /**
     * Generate datapoints that fit the uniform distribution. This function will
     * fill the data array with values that fit the uniform distribution.
     *
     * @param data to be filled with new random points
     */
    public void generateData(double[] data) {
        // generate random data
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
        return min + diff * Math.random();
    }

    /**
     * Calculate the probablity of x ( Pr(X=x) ).
     *
     * @param x the value for which to calculate the probability.
     * @return the probability.
     */
    public double calculatePDF(double x) {
        return (x <= max && x >= min) ? 1 / diff : 0;
    }

    /**
     * Calculate the cummalative distribution of X<=x.
     *
     * @param x value for which to calculate the cummalitive distribution
     * @return the cummalative distribution of X<=x
     */
    public double calculateCDF(double x) {
        if (x < min)
            return 0;
        if (x > max)
            return 1;

        return (x - min) / diff;
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
        if (b < min)
            return 0;
        if (a > max)
            return 0;

        if (a < min)
            a = min;
        if (b > max)
            b = max;

        return (b - a) / diff;
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
            return min;
        if (cdf >= 1)
            return max;

        return cdf * diff + min;
    }

    /**
     * Returns the exact mean of the PDF
     *
     * @return mean
     */
    public double getMean() {
        return (min + max) / 2.0;
    }

    /**
     * Returns the exact median of the PDF
     *
     * @return median
     */
    public double getMedian() {
        return (min + max) / 2.0;
    }

    /**
     * Returns the exact standard deviation of the PDF
     *
     * @return standard deviation
     */
    public double getStandardDeviation() {
        return Math.sqrt((max - min) * (max - min) / 12);
    }

    /**
     * Returns the exact coefficient of variation of the PDF coefficient of
     * variation = standard deviation / mean
     *
     * @return variance
     */
    public double getCoefficientOfVariation() {
        return (max - min) / (Math.sqrt(3) * (max + min));
    }

    /**
     * Returns the exact skew of the PDF
     *
     * @return skew
     */
    public double getSkewness() {
        return 0;
    }

    /**
     * Returns the exact kurtosis of the PDF
     *
     * @return kurtosis
     */
    public double getKurtosis() {
        return 9.0 / 5.0;
    }

    /**
     * Return a description of the distribution.
     *
     * @return description of distribtution.
     */
    public String toString() {
        return "Uniform Distribution with param [" + min + " " + max + "].";
    }
}
