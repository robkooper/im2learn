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

import java.util.Arrays;

/**
 * Based on a given data set this class will calculate statistical information
 * about the data set (like Mean, Variance etc) as well as being able to find
 * the right distribtution to fit the data set.
 *
 * @author Rob Kooper
 * @version 1.0 (June 30, 2003)
 */
public class Estimator {
    /**
     * Where the appropriate value is stored in the array returned by
     * calculate.
     */
    final static public int M2 = 0;
    final static public int M3 = 1;
    final static public int M4 = 2;

    final static public int MEAN = 3;
    final static public int MEDIAN = 4;
    final static public int VARIANCE = 5;
    final static public int STDDEV = 6;
    final static public int SKEW = 7;
    final static public int KURTOSIS = 8;
    final static public int SQRT_BETA1 = 9;
    final static public int BETA1 = 10;
    final static public int BETA2 = 11;
    final static public int MIN = 12;
    final static public int MAX = 13;
    final static public int SIZE = 14;   // keep this as last entry

    // M1 is always 0 so not stored.

    /**
     * List of names of the data that is calculated. This list is in the same
     * order as the data that is stored in the array returned by calculate.
     */
    final static public String[] RESULTS = new String[]{"M2", "M3", "M4", "Mean", "Median", "Unbiased Variance",
                                                        "Unbiased Standard Deviation", "Skew", "Kurtosis", "Sqrt(Beta1)",
                                                        "Beta1", "Beta2", "Min", "Max"};

    /**
     * Tolerance that is used when fitting a PDF.
     */
    private double tolerance = 0.0005;

    /**
     * Default contructor
     */
    public Estimator() {
    }

    /**
     * Returns the current tolerance used when fitting a PDF.
     *
     * @return the tolerance.
     */
    public double getTolerance() {
        return tolerance;
    }

    /**
     * Sets the tolerance that is used when fitting a PDF.
     *
     * @param tolerance is the new tolerance.
     * @throws Exception if the tolerance is less then 0.
     */
    public void setTolerance(double tolerance) throws Exception {
        if (tolerance < 0)
            throw(new Exception("Tolerance can not be less than 0."));
        this.tolerance = tolerance;
    }

    /**
     * Based on the dataset calculate statistical parameters that describe the
     * dataset. Which parameters are calculated is based on the stats array.
     *
     * @param data the data on which to calculate the pdf.
     */
    public double[] calculate(double data[]) {
        double tmp;
        int i;
        int len = data.length;
        double[] result = new double[SIZE];

        // reset array
        for (i = 0; i < SIZE; i++) {
            result[i] = 0;
        }

        // sort the array of data
        double[] sorted = new double[len];
        System.arraycopy(data, 0, sorted, 0, len);
        Arrays.sort(sorted);

        // calculate the mean
        result[MEAN] = 0;
        for (i = 0; i < len; i++)
            result[MEAN] += data[i];
        result[MEAN] = result[MEAN] / len;

        // middle number of sorted data is the median
        if (len % 2 == 0) {
            result[MEDIAN] = (sorted[(len - 1) / 2] + sorted[len / 2]) / 2.0;
        } else {
            result[MEDIAN] = sorted[len / 2];
        }

        // first and last is min and max
        result[MIN] = sorted[0];
        result[MAX] = sorted[len - 1];

        // calculate the variance, stddev, skew and kurtosis
        for (i = 0; i < len; i++) {
            tmp = (data[i] - result[MEAN]);
            result[M2] += (tmp * tmp);
            result[M3] += (tmp * tmp * tmp);
            result[M4] += (tmp * tmp * tmp * tmp);
        }
        result[VARIANCE] = result[M2] / (len - 1);
        result[STDDEV] = Math.sqrt(result[VARIANCE]);
        result[M2] = result[M2] / len;
        result[M3] = result[M3] / len;
        result[M4] = result[M4] / len;
        result[KURTOSIS] = result[M4] / (result[M2] * result[M2]);
        result[SKEW] = result[M3] / Math.sqrt(result[M2] * result[M2] * result[M2]);

        result[BETA2] = result[KURTOSIS];
        result[SQRT_BETA1] = result[SKEW];
        result[BETA1] = result[SKEW] * result[SKEW];

        return result;
    }

    /**
     * Find the best distribution that fits the data, and calculate the
     * parameters for this distribution.
     *
     * @param data to which to fit the distribution.
     * @return the apropriate distribution.
     * @throws Exception if an error occured creating the distribution.
     */
    public ProbabilityDistribution1D EstimationVarious(double[] data) throws Exception {
        // First calculate some information.
        double[] stats = calculate(data);

        // Find the appropriate distribution.
        if ((Math.abs(stats[SQRT_BETA1]) < tolerance) && (Math.abs(stats[BETA2] - 3.0) < tolerance)) {
            // use a gaussian distribution.
            return new Gaussian1D(stats[MEAN], stats[STDDEV]);
        } else if ((Math.abs(stats[SQRT_BETA1]) < tolerance) && (Math.abs(stats[BETA2] - 1.8) < tolerance)) {
            // use a uniform distribution.
            return new Uniform(stats[MIN], stats[MAX]);
        } else if ((Math.abs(stats[SQRT_BETA1] - 2) < tolerance) && (Math.abs(stats[BETA2] - 9.0) < tolerance)) {
            // use a exponential distribution.
            return new Exponential(1 / stats[MEAN], 0);
        }

        // TODO implement more distributions.

        // worst case just return a generic pdf
        Generic1D generic = new Generic1D();
        generic.estimateParameters(data);
        return generic;
    }

    /**
     * Find the best Johnson distribution and fit parameters. This method will
     * calculate what Johnson distribution fits the data best, and calculate the
     * parameters for this distribution. This function is based on the following
     * publication:<br> Slifker, J.F. and Shapiro, S.S., "The Johnson System:
     * Selection and Parameter Estimation", Technometrics, Vol. 22, No. 2, pg
     * 239--246, 1980.
     *
     * @param data to which to fit the Johnson distribution.
     * @return the apropriate Johnson distribution.
     * @throws Exception if an error occured creating the distribution.
     */
    public ProbabilityDistribution1D EstimationJohnson(double[] data) throws Exception {
        return EstimationJohnson(data, 0, data.length - 1, "");
    }

    public ProbabilityDistribution1D EstimationJohnson(double[] data, int start, int end, String text) throws Exception {
        // pick the zval, this value corresponds to 1%, 22%, 78% and 99%
        double zval = 0.77;
        ProbabilityDistribution1D pdf = null;
        double eta = Double.NaN;
        ;
        double gamma = Double.NaN;
        double epsilon = Double.NaN;
        ;
        double lambda = Double.NaN;
        ;
        Gaussian1D gauss = new Gaussian1D();
        int len = end - start;

        if ((start < 0) || (len >= data.length)) {
            throw(new Exception("Invalid range for data given."));
        }

        // sort the data
        double[] sorted = new double[len];
        System.arraycopy(data, start, sorted, 0, len);
        Arrays.sort(sorted);

        // CHECK no 10% of consecutive data can have same value
        // at 95% use uniform.
        int tenperc = (int) (len * 0.1);
        int bail = (int) (len * 0.4);
        int uniform = (int) (len * 0.95);
        int count = 0;
        double val = sorted[0];
        boolean bailnow = false;

        for (int i = 0; i < len; i++) {
            if (Math.abs(sorted[i] - val) < 1e-5) {
                count++;
            } else {
                if (count > uniform) {
                    System.err.println("Many of the same values " + val + " " + count + " out of " + len + " times == " + (count * 100 / len) + "%.");
                    System.err.println("Falling back on uniform");
                    Uniform uni = new Uniform();
                    uni.estimateParameters(data);
                    return uni;
                } else if (count > tenperc) {
                    System.err.println("Many of the same values " + val + " " + count + " out of " + len + " times == " + (count * 100 / len) + "%.");

                    if (count > bail) {
                        if (val != 0.0) {
                            throw(new Exception("Can't handle large same values other than 0."));
                        }
                        bailnow = true;
                    }
                }
                val = sorted[i];
                count = 0;
            }
        }
        if (count > uniform) {
            System.err.println("Many of the same values " + val + " " + count + " out of " + len + " times == " + (count * 100 / len) + "%.");
            System.err.println("Falling back on uniform");
            Uniform uni = new Uniform();
            uni.estimateParameters(data);
            return uni;
        } else if (count > tenperc) {
            System.err.println("Many of the same values " + val + " " + count + " out of " + len + " times == " + (count * 100 / len) + "%.");

            if (count > bail) {
                if (val != 0.0) {
                    throw(new Exception("Can't handle large same values other than 0."));
                }
                bailnow = true;
            }
        }

        if (bailnow) {
            pdf = new Combo2PDF();
            pdf.estimateParameters(data);
            System.err.println("BAILING CAN'T RESOLVE, USE OTHER ESTIMATOR. Giving " + pdf + " instead.");
            return pdf;
            //Weibull1D weibull1d = new Weibull1D();
            //weibull1d.estimateParameters(data);
            //return weibull1d;
        }

        // keep trying until we have a valid estimation
        do {
            // find the percentiles corresponding to the Z
            double zp3 = gauss.calculateCDF(3 * zval);
            double zp1 = gauss.calculateCDF(zval);
            double zn1 = gauss.calculateCDF(-zval);
            double zn3 = gauss.calculateCDF(-3 * zval);

            //System.out.println("3z = " + zp3 + " 1z " + zp1 + " -3z " + zn3 + " -1z " + -zn1);

            // get the x-values
            double ip3, ip1, in1, in3;
            ip3 = zp3 * (len - 1) - 0.5;
            double xp3z = sorted[(int) ip3] + (ip3 % 1) * (sorted[(int) ip3 + 1] - sorted[(int) ip3]);

            ip1 = zp1 * (len - 1) - 0.5;
            double xp1z = sorted[(int) ip1] + (ip1 % 1) * (sorted[(int) ip1 + 1] - sorted[(int) ip1]);

            in1 = zn1 * (len - 1) - 0.5;
            double xn1z = sorted[(int) in1] + (in1 % 1) * (sorted[(int) in1 + 1] - sorted[(int) in1]);

            in3 = zn3 * (len - 1) - 0.5;
            double xn3z = sorted[(int) in3] + (in3 % 1) * (sorted[(int) in3 + 1] - sorted[(int) in3]);

            if (in1 >= ip1) {
                throw(new Exception("Could not find correct values for gamma, epsilor or eta (in1 >= ip1)."));
            }

            //System.out.println(xp3z + " " + xp1z + " " + xn1z + " " + xn3z);

            // precalculate
            double p = xp1z - xn1z;
            double m = xp3z - xp1z;
            double n = xn1z - xn3z;

            double mp = m / p;
            double np = n / p;
            double pm = p / m;
            double pn = p / n;

            //System.out.println(p + " " + m + " " + n);
            //System.out.println(mp + " " + np + " " + pm + " " + pn);

            double r = (m * n) / (p * p);

            //System.out.println(r);

            // Johnson Su distribution
            if (r > 1 + tolerance) {
                //System.err.println("DEBUG: Johnson Su needs to be used.");

                eta = (2 * zval) / acosh(0.5 * (mp + np));
                gamma = eta * asinh((np - mp) / (2 * Math.sqrt(mp * np - 1)));
                lambda = (2 * p * Math.sqrt(mp * np - 1)) / ((mp + np - 2) * Math.sqrt(mp + np + 2));
                epsilon = (xp1z + xn1z) / 2 + (p * (np - mp)) / (2 * (mp + np - 2));

                pdf = new JohnsonSu(lambda, epsilon, eta, gamma);
            }

            // Johnson Sb distribution
            else if (r < 1 - tolerance) {
                //System.err.println("DEBUG: Johnson Sb needs to be used.");

                eta = zval / acosh(0.5 * Math.sqrt((1 + pm) * (1 + pn)));
                gamma = eta * asinh(((pn - pm) * Math.sqrt((1 + pm) * (1 + pn) - 4)) / (2 * (pm * pn - 1)));
                lambda = (p * Math.sqrt(((1 + pm) * (1 + pn) - 2) * ((1 + pm) * (1 + pn) - 2) - 4)) / (pm * pn - 1);
                epsilon = (xp1z + xn1z) / 2 - (lambda / 2) + ((p * (pn - pm)) / (2 * (pm * pn - 1)));

                pdf = new JohnsonSb(lambda, epsilon, eta, gamma);
            }

            // Johnson Sl distribution
            else {

                //System.err.println("DEBUG: Johnson Sl needs to be used.");

                eta = 2 * zval / Math.log(mp);
                gamma = eta * Math.log((mp - 1) / (p * Math.sqrt(mp)));
                epsilon = ((xp1z + xn1z) / 2) - (p / 2) * ((mp + 1) / (mp - 1));
                pdf = new JohnsonSl(epsilon, eta, gamma);
            }

            zval -= 0.01;
        } while (((Double.isNaN(gamma) || Double.isNaN(epsilon) || Double.isNaN(eta))));

        //System.err.println("DEBUG: parameters are (lambda, epsilon, eta, gamma) = (" +
        //                   lambda + ", " + epsilon + ", " + eta + ", " + gamma + ")");
        return pdf;
    }

    /**
     * Find the parameters for a Pearson distribution and return this
     * distribtution.
     *
     * @param data to which to fit the Pearson distribution.
     * @return the apropriate Pearson distribution.
     * @throws Exception if an error occured creating the distribution.
     */
    public ProbabilityDistribution1D EstimationPearson(double[] data) throws Exception {
        // TODO implement
        throw(new Exception("NOT IMPLEMENETED."));
    }

    // TODO are these defined anywhere else?
    /**
     * Return cosh(x)
     *
     * @param x to calculate cosh for.
     * @return the calculated value
     */
    static public double cosh(double x) {
        return (Math.exp(x) + Math.exp(-x)) / 2;
    }

    /**
     * Return cosh-1(x)
     *
     * @param x to calculate cosh-1 for.
     * @return the calculated value
     */
    static public double acosh(double x) {
        if (x < 1) System.err.println("ERROR: x < 1");
        return Math.log(x + Math.sqrt(x * x - 1));
    }

    /**
     * Return sinh(x)
     *
     * @param x to calculate sinh for.
     * @return the calculated value
     */
    static public double sinh(double x) {
        return (Math.exp(x) - Math.exp(-x)) / 2;
    }

    /**
     * Return sinh-1(x)
     *
     * @param x to calculate sinh-1 for.
     * @return the calculated value
     */
    static public double asinh(double x) {
        return Math.log(x + Math.sqrt(x * x + 1));
    }
}
