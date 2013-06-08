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

import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Gaussian1D will implement a gaussion (or normal) distribution function. The
 * pdf for a gaussian distribution is:
 * <p/>
 * f(x) = 1/[stddev*sqrt(2*pi)] * e^[-(x-mean)^2/(2*stddev^2)] stddev > 0
 * 
 * @author Rob Kooper
 * @version 1.0 (June 30, 2003)
 */
public class Gaussian1D implements ProbabilityDistribution1D {
	/**
	 * Mean of the distribution.
	 */
	private double		mean		= Double.NaN;

	/**
	 * Standard Deviation of the distribution.
	 */
	private double		stddev		= Double.NaN;

	/**
	 * precalculation of 2 * stddev^2.
	 */
	private double		stddev2		= Double.NaN;

	/**
	 * precalculation of sqrt(2*Math.PI) * stddev.
	 */
	private double		stddevpi	= Double.NaN;

	/**
	 * random number generator used in this class.
	 */
	private Random		random		= new Random();

	/**
	 * step size for inverse CDF and CDF calculations
	 */
	private int			maxiter		= 100000;

	static private Log	logger		= LogFactory.getLog(Gaussian1D.class);

	/**
	 * Create a gaussian distribution with mean 0 and standard deviation 1.
	 * 
	 * @throws Exception
	 *             if standard deviation is less or equal to 0.
	 */
	public Gaussian1D() throws Exception {
		this(0.0, 1.0);
	}

	/**
	 * Create a gaussian distribution with given mean and standard deviation.
	 * 
	 * @param mean
	 *            of the new normal distribution.
	 * @param stddev
	 *            of the new normal distribution.
	 * @throws Exception
	 *             if standard deviation is less or equal to 0.
	 */
	public Gaussian1D(double mean, double stddev) throws Exception {
		if (stddev < 0.0)
			throw (new Exception("Invalid Standard Deviation, has to be greater than 0."));

		this.mean = mean;
		this.stddev = stddev;
		this.stddev2 = 2.0 * stddev * stddev;
		this.stddevpi = Math.sqrt(2.0 * Math.PI) * stddev;
	}

	/**
	 * Returns the mean of the distribution.
	 * 
	 * @return the mean.
	 */
	public double getMean() {
		return mean;
	}

	/**
	 * Sets the mean of the distribution.
	 * 
	 * @param mean
	 *            the new mean.
	 */
	public void setMean(double mean) {
		this.mean = mean;
	}

	/**
	 * Returns the standard deviation of the distribution.
	 * 
	 * @return the standard deviation.
	 */
	public double getStandardDeviation() {
		return stddev;
	}

	/**
	 * Sets the standard deviation of the distribution.
	 * 
	 * @param stddev
	 *            the new standard deviation.
	 * @throws Exception
	 *             if the standard deviation is less or equal to 0.
	 */
	public void setStandardDeviation(double stddev) throws Exception {
		if (stddev <= 0.0)
			throw (new Exception("Invalid Standard Deviation, has to be greater than 0."));

		this.stddev = stddev;
		this.stddev2 = 2.0 * stddev * stddev;
		this.stddevpi = Math.sqrt(2.0 * Math.PI) * stddev;
	}

	/**
	 * Estimate the parameters of a gaussian distribution that fits the given
	 * data set.
	 * 
	 * @param data
	 *            used to estimate the mean and standard deviation.
	 */
	public void estimateParameters(double[] data) {
		Estimator estimator = new Estimator();

		double[] stats = estimator.calculate(data);
		this.mean = stats[Estimator.MEAN];
		this.stddev = stats[Estimator.STDDEV];
		this.stddev2 = 2.0 * stddev * stddev;
		this.stddevpi = Math.sqrt(2.0 * Math.PI) * stddev;
	}

	/**
	 * Generate datapoints that fit the gaussian distribution. This function
	 * will fill the data array with values that fit the gaussian distribution.
	 * 
	 * @param data
	 *            to be filled with new random points
	 */
	public void generateData(double[] data) {
		for (int i = 0; i < data.length; i++) {
			data[i] = mean + stddev * random.nextGaussian();
		}
	}

	/**
	 * Generate a random sample from the PDF. This function uses the standard
	 * random.nextGaussion() function to generate a random number with a
	 * gaussian distribution of [0, 1].
	 * 
	 * @return a single random number
	 */
	public double generate() {
		return mean + stddev * random.nextGaussian();
	}

	/**
	 * Calculate the probablity of x ( Pr(X=x) ). The implementation will return
	 * the exact value.
	 * 
	 * @param x
	 *            the value for which to calculate the probability.
	 * @return the probability.
	 */
	public double calculatePDF(double x) {
		return (1 / stddevpi) * Math.exp(-((x - mean) * (x - mean)) / (stddev2));
	}

	/**
	 * Calculate the cumulative distribution of X<=x. The implementation will
	 * return an approximation of the value.
	 * 
	 * @param x
	 *            value for which to calculate the cumulative distribution
	 * @return the cumulative distribution of X<=x
	 */
	public double calculateCDF(double x) {
		return calculateCDF(Double.NEGATIVE_INFINITY, x, maxiter);
	}

	/**
	 * Calculate the probability of a<=X<=b. The implementation will return an
	 * approximation of the value.
	 * 
	 * @param a
	 *            lowerlimit for which to calculate the probability
	 * @param b
	 *            upperlimit for which to calculate the probability
	 * @return the probability of a<=X<=b
	 */
	public double calculateCDF(double a, double b) {
		return calculateCDF(a, b, maxiter);
	}

	/**
	 * Calculate the probability of a<=X<=b. The implementation will return an
	 * approximation of the value.
	 * 
	 * @param a
	 *            lowerlimit for which to calculate the probability
	 * @param b
	 *            upperlimit for which to calculate the probability
	 * @return the probability of a<=X<=b
	 */
	private double calculateCDF(double a, double b, int iterations) {
		if (b <= a)
			return 0.0;

		if (a == Double.NEGATIVE_INFINITY)
			a = -10.0 * stddev + mean;

		if (b == Double.POSITIVE_INFINITY)
			b = 10.0 * stddev + mean;

		double result = 0;
		double step = (b - a) / iterations;
		for (double x = a; x <= b; x += step) {
			result += calculatePDF(x);
		}
		return result * step;
	}

	/**
	 * This function will return the value for x, such that the calculateCDF(x)
	 * ~= cdf The implementation will return an approximation of the value.
	 * 
	 * @param cdf
	 *            that we want to find
	 * @return the x such that the calculateCDF(x) ~= cdf
	 */
	public double inverseCDF(double cdf) {
		double step = stddev;
		double val = mean;
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
			ucdf = calculateCDF(Double.NEGATIVE_INFINITY, val + step, maxiter);
			lcdf = calculateCDF(Double.NEGATIVE_INFINITY, val - step, maxiter);
		} while (cdf > ucdf || cdf < lcdf);

		// minimize error
		vcdf = calculateCDF(Double.NEGATIVE_INFINITY, val, maxiter);
		for (int i = 0; i < 20 && Math.abs(vcdf - cdf) > 1e-7; i++) {
			step /= 2.0;
			if (cdf < vcdf) {
				val -= step;
			} else {
				val += step;
			}
			vcdf = calculateCDF(Double.NEGATIVE_INFINITY, val, maxiter);
		}

		return val;
	}

	/**
	 * This method find a dividing value between two Gaussian PDF for which the
	 * integral from -infinity to x _dividing of PDF1 is equal to the integral
	 * from x_dividing to infinity of PDF2 val = (mean1/stdev1 +
	 * mean2/stdev2)/(1/stdev1 + 1/stdev2)
	 * 
	 * @param meanSecondPDF
	 * @param stdevSecondPDF
	 * @return dividing x
	 */
	public double findTwoPDFDividingValue(double meanSecondPDF, double stdevSecondPDF) {

		double val = 0.0;

		if (getStandardDeviation() <= 0.0 && stdevSecondPDF <= 0.0) {
			return ((getMean() + meanSecondPDF) * 0.5);
		}

		val = (getMean() * stdevSecondPDF + meanSecondPDF * getStandardDeviation()) / (stdevSecondPDF + getStandardDeviation());
		return val;
	}

	/**
	 * Returns the exact median of the PDF
	 * 
	 * @return median
	 */
	public double getMedian() {
		return mean;
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
		return 0;
	}

	/**
	 * Returns the exact kurtosis of the PDF
	 * 
	 * @return kurtosis
	 */
	public double getKurtosis() {
		return 3.0;
	}

	/**
	 * Return a description of the distribution.
	 * 
	 * @return description of distribtution.
	 */
	public String toString() {
		return "Gaussian Distribution with param [" + mean + " " + stddev + "].";
	}

	/**
	 * This version is based on the fact that all values of the gaussian will
	 * fall in a circle with radius 1.
	 * 
	 * @return a random number
	 */
	private double generator1() {
		float random = 0.0F;
		double urand1, urand2;

		urand1 = Math.abs(Math.random()) + 1.0e-8;
		urand2 = Math.abs(Math.random()) + 1.0e-8;

		random = (float) (Math.sqrt(-2.0 * Math.log(urand1)) * Math.cos(2.0 * Math.PI * urand2) + 1.0e-7);

		return random;
	}
}
