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
 * Generic1D will implement a data driven distribution function. The pdf for
 * this ditribution is based on the data that is passed to the estimator. The
 * data is then binned, and this binned data is the PDF.
 *
 * @author Rob Kooper
 * @version 1.0 (December 5, 2003)
 */
public class Generic1D implements ProbabilityDistribution1D {
    /**
     * Current version number and more info.
     */
    public final static String CVS_ID = "$Id: Generic1D.java,v 1.4 2006-12-07 17:00:05 kooper Exp $";

    /**
     * lower bound of the distribution.
     */
    private double lower = Double.NaN;

    /**
     * upper bound of the distribution.
     */
    private double upper = Double.NaN;

    /**
     * precalculated upper - lower
     */
    private double diff = Double.NaN;

    /**
     * the number of bins to put the data in.
     */
    private int nobins = 1000;

    /**
     * size of each bin, this is based on upper, lower and nobins.
     */
    private double binsize = Double.NaN;

    /**
     * calculated pdf based on binning data
     */
    private double[] estm_pdf = null;

    /**
     * calculated cdf, estm_cdf[x] = summation of est_pdf[x-1]
     */
    private double[] estm_cdf = null;

    /**
     * maximum probability in the estm_pdf, used to speed up random number
     * generation.
     */
    private double maxprob = 1;

    /**
     * calculated mean of the original set.
     */
    private double mean = Double.NaN;

    /**
     * calculated median of the original set.
     */
    private double median = Double.NaN;

    /**
     * calculated standard deviation of the original set.
     */
    private double stddev = Double.NaN;

    /**
     * calculated skew of the original set.
     */
    private double skew = Double.NaN;

    /**
     * calculated kurtosis of the original set.
     */
    private double kurtosis = Double.NaN;

    /**
     * default constructor, use this and call estimateParameters to fill in the
     * PDF.
     */
    public Generic1D() {
    }

    /**
     * Create a generic PDF
     *
     * @param data on which the PDF is to be based
     */
    public Generic1D(double[] data) {
        estimateParameters(data);
    }

    /**
     * Return the number of bins the data is put in.
     *
     * @return number of bins data is put in.
     */
    public int getNobins() {
        return nobins;
    }

    /**
     * Set the number of bins used to bin the data, set this before calling
     * estimate Parameters.
     *
     * @param nobins is the number of bins used.
     */
    public void setNobins(int nobins) {
        this.nobins = nobins;
    }

    /**
     * Return the calculated binsize.
     *
     * @return the binsize
     */
    public double getBinSize() {
        return binsize;
    }

    /**
     * Return the calculated lower bounds.
     *
     * @return the lower bounds
     */
    public double getLowerBound() {
        return lower;
    }

    /**
     * Return the calculated upper bounds.
     *
     * @return the upper bounds
     */
    public double getUpperBound() {
        return upper;
    }

    /**
     * Estimate the parameters of the PDF to fit the data. The function will
     * split the data in bins. The bins are basicly the PDF.
     *
     * @param data to be used to estimate the parameters.
     */
    public void estimateParameters(double[] data) {
        int i;
        int len = data.length;
        double[] stats = new Estimator().calculate(data);

        // Store some parameters of the distribution.
        mean = stats[Estimator.MEAN];
        median = stats[Estimator.MEDIAN];
        stddev = stats[Estimator.STDDEV];
        skew = stats[Estimator.SKEW];
        kurtosis = stats[Estimator.KURTOSIS];

        // Set the bin params
        lower = stats[Estimator.MIN];
        upper = stats[Estimator.MAX];
        diff = upper - lower;
        binsize = (upper - lower) / nobins;
        estm_pdf = new double[nobins];
        estm_cdf = new double[nobins];

        // zero the initial data
        for (i = 0; i < nobins; i++) {
            estm_pdf[i] = 0;
        }

        // bin the data, creating the pdf
        double add = 1.0 / len;
        for (i = 0; i < len; i++) {
            estm_pdf[findBin(data[i])] += add;
        }

        // calculate the cdf
        // estm_cdf containts the cdf upto the bin
        estm_cdf[0] = 0;
        maxprob = estm_pdf[0];
        for (i = 1; i < nobins; i++) {
            if (estm_pdf[i] > maxprob)
                maxprob = estm_pdf[i];
            estm_cdf[i] = estm_cdf[i - 1] + estm_pdf[i - 1];
        }
    }

    /**
     * To fill the array, we simply take the pdf, and 2 random numbers. The
     * first number will tell us what bin to look in (max value of column), the
     * second random number tells us the height of that column, if the second
     * random number is less than the height of the column we accept the random
     * numbers.
     *
     * @param data will be filled with random samples
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
        double x, y;

        // First pick a random number that falls in range of the data
        // Next pick the probability of that number
        do {
            x = lower + Math.random() * diff;
            y = Math.random() * maxprob;
        } while (estm_pdf[findBin(x)] <= y);

        return x;
    }

    /**
     * Calculate the probability of X=x.
     *
     * @param x value for which to calculate the probability
     * @return the probability of X=x
     */
    public double calculatePDF(double x) {
        if ((x <= lower) || (x >= upper))
            return 0;
        return estm_pdf[findBin(x)] / binsize;
    }

    /**
     * Calculate the cummalative distribution of X<=x.
     *
     * @param x value for which to calculate the cummalitive distribution
     * @return the cummalative distribution of X<=x
     */
    public double calculateCDF(double x) {
        if ((x <= lower) || (x >= upper))
            return 0;
        int bin = findBin(x);
        double cdf1 = estm_cdf[bin];
        double cdf2 = (bin == nobins - 1) ? 1.0 : estm_cdf[bin + 1];
        double frac = ((x - lower) / binsize) - bin;
        return cdf1 + frac * (cdf2 - cdf1);
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

        if (a <= lower) {
            return calculateCDF(b);
        } else if (b >= upper) {
            return 1 - calculateCDF(a);
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
            return lower;
        if (cdf >= 1)
            return upper;

        double prev = 0;
        for (int i = 0; i < nobins; i++) {
            if (cdf <= estm_cdf[i]) {
                double frac = (estm_cdf[i] - cdf) / (estm_cdf[i] - prev);
                return lower + (i - 1 + frac) * binsize;
            }
            prev = estm_cdf[i];
        }
        return upper;
    }

    /**
     * Returns the exact mean of the PDF
     *
     * @return mean
     */
    public double getMean() {
        return mean;
    }

    /**
     * Returns the exact median of the PDF
     *
     * @return median
     */
    public double getMedian() {
        return median;
    }

    /**
     * Returns the exact standard deviation of the PDF
     *
     * @return standard deviation
     */
    public double getStandardDeviation() {
        return stddev;
    }

    /**
     * Returns the exact coefficient of variation of the PDF coefficient of
     * variation = standard deviation / mean
     *
     * @return variance
     */
    public double getCoefficientOfVariation() {
        return stddev / mean;
    }

    /**
     * Returns the exact skew of the PDF
     *
     * @return skew
     */
    public double getSkewness() {
        return skew;
    }

    /**
     * Returns the exact kurtosis of the PDF
     *
     * @return kurtosis
     */
    public double getKurtosis() {
        return kurtosis;
    }

    /**
     * Return a description of the distribution.
     *
     * @return description of distribtution.
     */
    public String toString() {
        return "Generic Distribution based on data [ ... ].";
    }

    /**
     * Find the correct bin to put the data in
     *
     * @param x number to be binned
     * @return bin in which it fits
     */
    private int findBin(double x) {
        if (x <= lower)
            return 0;
        if (x >= upper)
            return nobins - 1;

        return (int) Math.floor((x - lower) / binsize);
    }
}
