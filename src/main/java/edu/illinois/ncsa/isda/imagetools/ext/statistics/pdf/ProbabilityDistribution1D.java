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

import java.io.Serializable;

/**
 * Generalization of 1D Probability Distribution Functions. This interface will
 * define the methods that are commenly found in probability distribution
 * functions.
 */
public interface ProbabilityDistribution1D extends Serializable {
    // Current version number and more info.
    // CVS_ID = "$Id: ProbabilityDistribution1D.java,v 1.4 2006-12-07 17:00:05 kooper Exp $";

    /**
     * Estimate the parameters of the PDF to fit the data. Using the data passed
     * in this function will calculate parameters for the PDF such that the PDF
     * closely matches the given data. This process is depended on picking the
     * right PDF first.
     *
     * @param data to be used to estimate the parameters.
     */
    public void estimateParameters(double[] data) throws Exception;

    /**
     * Generate random samples from the PDF. Based on the PDF this function will
     * generate random samples that fit the distribution and return them.
     *
     * @param data will be filled with random samples
     */
    public void generateData(double[] data);

    /**
     * Generate a random sample from the PDF.
     *
     * @return a single random number
     */
    public double generate();

    /**
     * Calculate the probability of X=x.
     *
     * @param x value for which to calculate the probability
     * @return the probability of X=x
     */
    public double calculatePDF(double x);

    /**
     * Calculate the cummalative distribution of X<=x.
     *
     * @param x value for which to calculate the cummalitive distribution
     * @return the cummalative distribution of X<=x
     */
    public double calculateCDF(double x);

    /**
     * Calculate the probability of a<=X<=b.
     *
     * @param a lowerlimit for which to calculate the probability
     * @param b upperlimit for which to calculate the probability
     * @return the probability of a<=X<=b
     */
    public double calculateCDF(double a, double b);

    /**
     * This function will return the value for x, such that the calculateCDF(x)
     * ~= cdf
     *
     * @param cdf that we want to find
     * @return the x such that the calculateCDF(x) ~= cdf
     */
    public double inverseCDF(double cdf);

    /**
     * Returns the exact mean of the PDF
     *
     * @return mean
     */
    public double getMean();

    /**
     * Returns the exact median of the PDF
     *
     * @return median
     */
    public double getMedian();

    /**
     * Returns the exact standard deviation of the PDF
     *
     * @return standard deviation
     */
    public double getStandardDeviation();

    /**
     * Returns the exact coefficient of variation of the PDF coefficient of
     * variation = standard deviation / mean
     *
     * @return coefficient of variation
     */
    public double getCoefficientOfVariation();

    /**
     * Returns the exact skew of the PDF
     *
     * @return skew
     */
    public double getSkewness();

    /**
     * Returns the exact kurtosis of the PDF
     *
     * @return kurtosis
     */
    public double getKurtosis();
}
