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
 * Beta will implement a beta distribution function. The pdf for a beta
 * distribution is:
 * <p/>
 * f(x) = (x-lower)^(alpha-1)*(upper-x)^(beta-1) / Beta(alpha,
 * beta)*(upper-lower)^(alpha+beta-1) lower <= x <= upper and alpha, beta > 0
 *
 * @author Rob Kooper
 * @version 1.0 (June 30, 2003)
 */
public class Beta implements ProbabilityDistribution1D {
    /**
     * Scale of the distribution.
     */
    private double alpha = Double.NaN;

    /**
     * Shape of the distribution.
     */
    private double beta = Double.NaN;

    /**
     * Lower bound of the distribution
     */
    private double lower = Double.NaN;

    /**
     * Upper bound of the distribution
     */
    private double upper = Double.NaN;

    /**
     * precalculate the scalefactor
     */
    private double scale = Double.NaN;

    private double divider = Double.NaN;

    /**
     * Gamma function for alpha varaible
     */
    Gamma gammaalpha = null;

    /**
     * Gamma function for beta varaible
     */
    Gamma gammabeta = null;

    /**
     * Create a beta distribution with alpha and beta 1 betwee 0 and 1
     *
     * @throws Exception if error creating the Beta distribution
     */
    public Beta() throws Exception {
        this(1, 1, 0, 1);
    }

    /**
     * Create a beta distribution with given alpha and beta, betwee 0 and 1
     *
     * @param alpha is alpha paramter of distribution
     * @param beta  is beta paramter of distribution
     * @throws Exception if error creating the Beta distribution
     */
    public Beta(double alpha, double beta) throws Exception {
        this(alpha, beta, 0, 1);
    }

    /**
     * Create a beta distribution with given alpha and beta and lower and upper
     * bounds.
     *
     * @param alpha of the distribution.
     * @param beta  of the distribution.
     * @param lower lowerbound of the distribution.
     * @param upper upperbound of the distribution.
     * @throws Exception if alpha or beta is less or equal to 0.
     */
    public Beta(double alpha, double beta, double lower, double upper) throws Exception {
        if (alpha <= 0)
            throw(new Exception("alpha has to be greater than 0."));
        if (beta <= 0)
            throw(new Exception("beta has to be greater than 0."));
        if (upper < lower)
            throw(new Exception("upperbound is less than lowerbound."));

        this.alpha = alpha;
        this.beta = beta;
        this.lower = lower;
        this.upper = upper;
        this.divider = complete(alpha, beta) * Math.pow(upper - lower, alpha + beta - 1);
        this.scale = (upper - lower);
        this.gammaalpha = new Gamma(alpha, 1, 0);
        this.gammabeta = new Gamma(beta, 1, 0);
    }

    /**
     * Returns the alpha of the distribution.
     *
     * @return alpha.
     */
    public double getAlpha() {
        return alpha;
    }

    /**
     * Sets the alpha of the distribution.
     *
     * @param alpha is the new value.
     * @throws Exception if alpha is not greater than 0.
     */
    public void setAlpha(double alpha) throws Exception {
        if (alpha <= 0)
            throw(new Exception("alpha has to be greater than 0."));

        this.alpha = alpha;
        this.divider = complete(alpha, beta) * Math.pow(upper - lower, alpha + beta - 1);
        this.gammaalpha = new Gamma(alpha, 1, 0);
    }

    /**
     * Returns the beta of the distribution.
     *
     * @return beta.
     */
    public double getBeta() {
        return beta;
    }

    /**
     * Sets the beta of the distribution.
     *
     * @param beta is the new value.
     * @throws Exception if beta is not greater than 0.
     */
    public void setBeta(double beta) throws Exception {
        if (beta <= 0)
            throw(new Exception("beta has to be greater than 0."));

        this.beta = beta;
        this.divider = complete(alpha, beta) * Math.pow(upper - lower, alpha + beta - 1);
        this.gammabeta = new Gamma(beta, 1, 0);
    }

    /**
     * Returns the lower bound of the distribution.
     *
     * @return offset.
     */
    public double getLowerBound() {
        return lower;
    }

    /**
     * Sets the lower bound of the distribution
     *
     * @param lower is the new lowerbound
     * @throws Exception if the upperbound is less than the lowerbound
     */
    public void setLowerBound(double lower) throws Exception {
        if (upper < lower)
            throw(new Exception("upperbound is less than lowerbound."));

        this.lower = lower;
        this.divider = complete(alpha, beta) * Math.pow(upper - lower, alpha + beta - 1);
        this.scale = upper - lower;
    }

    /**
     * Returns the upper bound of the distribution.
     *
     * @return offset.
     */
    public double getUpperBound() {
        return upper;
    }

    /**
     * Sets the upper bound of the distribution
     *
     * @param upper is the new lowerbound
     * @throws Exception if the upperbound is less than the lowerbound
     */
    public void setUpperBound(double upper) throws Exception {
        if (upper < lower)
            throw(new Exception("upperbound is less than lowerbound."));

        this.upper = upper;
        this.divider = complete(alpha, beta) * Math.pow(upper - lower, alpha + beta - 1);
        this.scale = upper - lower;
    }

    /**
     * Estimate the parameters of a beta distribution that fits the given data
     * set.
     *
     * @param data used to estimate the parameters.
     */
    public void estimateParameters(double[] data) throws Exception {
        Estimator estimator = new Estimator();

        double[] stats = estimator.calculate(data);
        throw(new Exception("Not implemented."));

        // TODO implement estimator
        //double x      = (stats[Estimator.MEAN] - a) / (b - a);
        //double s2     = stats[Estimator.VARIANCE] / ( (b - a) * (b - a));
        //this.alpha      = ((1 - x) / s2) * (x * (1 - x) - s2);
        //this.beta    = (x * this.alpha) / ( 1 - x);
    }

    /**
     * Generate datapoints that fit the beta distribution. This function will
     * fill the data array with values that fit the beta distribution.
     *
     * @param data to be filled with new random points
     */
    public void generateData(double[] data) {
        double[] tmp = new double[data.length];

        gammaalpha.generateData(data);
        gammabeta.generateData(tmp);

        // Knuth Vol 2 Ed 3 pg 134 "the beta distribution".
        for (int i = 0; i < data.length; i++) {
            if (data[i] != 0) {
                data[i] = lower + scale * (data[i] / (data[i] + tmp[i]));
            }
        }
    }

    /**
     * Generate a random sample from the PDF.
     *
     * @return a single random number
     */
    public double generate() {
        double a = gammaalpha.generate();
        if (a == 0.0) {
            return 0.0;
        }
        return lower + scale * (a / (a + gammabeta.generate()));
    }

    /**
     * Calculate the probablity of x ( Pr(X=x) ).
     *
     * @param x the value for which to calculate the probability.
     * @return the probability.
     */
    public double calculatePDF(double x) {
        if ((x < lower) || (x > upper)) {
            return 0;
        }

        return Math.pow(x - lower, alpha - 1) * Math.pow(upper - x, beta - 1) / divider;
    }

    /**
     * Calculate the cummalative distribution of X<=x.
     *
     * @param x value for which to calculate the cummalitive distribution
     * @return the cummalative distribution of X<=x
     */
    public double calculateCDF(double x) {
        if (x <= lower)
            return 0;
        if (x >= upper)
            return 1;
        return incomplete(alpha, beta, (x - lower) / (upper - lower));
    }

    /**
     * Calculate the probability of a<=X<b.
     *
     * @param a lowerlimit for which to calculate the probability
     * @param b upperlimit for which to calculate the probability
     * @return the probability of a<=X<b
     */
    public double calculateCDF(double a, double b) {
        if (b <= a)
            return 0;
        if (b <= lower)
            return 0;
        if (a >= upper)
            return 1;

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
     * ~= cdf The implementation will return an approximation of the value.
     *
     * @param cdf that we want to find
     * @return the x such that the calculateCDF(x) ~= cdf
     */
    public double inverseCDF(double cdf) {
        double step = 1;
        double val = lower;
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
        return lower + scale * (alpha / (alpha + beta));
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
        return scale * Math.sqrt((alpha * beta) / ((alpha + beta) * (alpha + beta) * (alpha + beta + 1)));
    }

    /**
     * Returns the exact coefficient of variation of the PDF coefficient of
     * variation = standard deviation / mean
     *
     * @return variance
     */
    public double getCoefficientOfVariation() {
        return Math.sqrt(beta / (alpha * (alpha + beta + 1)));
    }

    /**
     * Returns the exact skew of the PDF
     *
     * @return skew
     */
    public double getSkewness() {
        return (2 * (beta - alpha) * Math.sqrt(alpha + beta + 1)) / (Math.sqrt(beta * alpha) * (alpha + beta + 2));
    }

    /**
     * Returns the exact kurtosis of the PDF
     *
     * @return kurtosis
     */
    public double getKurtosis() {
        double t = 3 * (beta + alpha + 1) * (2 * (alpha + beta) * (alpha + beta) + (beta * alpha) * (beta + alpha - 6));
        return t / (beta * alpha * (beta + alpha + 2) * (beta + alpha + 3));
    }

    /**
     * Return a description of the distribution.
     *
     * @return description of distribtution.
     */
    public String toString() {
        return "Beta Distribution with param [" + alpha + " " + beta + " " + lower + " " + upper + "].";
    }

    static public double complete(double a, double b) {
        // Better off using the LN for large values, otherwise we run out of
        // numberspace.
        if ((a > 200) || (b > 200)) {
            return Math.exp(Gamma.completeLn(a) + Gamma.completeLn(b) - Gamma.completeLn(a + b));
        } else {
            return Gamma.complete(a) * Gamma.complete(b) / Gamma.complete(a + b);
        }
    }

    static public double incomplete(double a, double b, double x) {
        return betaI(a, b, x);
    }

    ///////////////////////////////////////////////////////////////////
    // returns the incomplete beta function
    //defined as Ix(a,b)=1/B(a,b) * Int(from 0 to x) t^(alpha-1)  (1-t)^(b-1) dt
    //a,b > 0
    //continued fraction representation
    //Ix(a,b)=x^a (a-x)^b/(a B(a,b)) [1/1+ d1/1+ d2/1+ ....]
    //d2m+1 = -(a+m)(a+b+m)x/( (a+2m) (a+2m+1))
    //d2m = m (b-m) x /( (a+2m -1) (a+2m))
    // based on Numerical recipes betai
    ///////////////////////////////////////////////////////////////////
    static private double betaI(double a, double b, double x) {
        double bt;
        if (x < 0.0 || x > 1.0) {
            System.out.println("Error: bad x in routine BetaI ");
            return 0.0;
        }
        if (x == 0.0 || x == 1.0)
            bt = 0.0;
        else
            bt = Math.exp(Gamma.completeLn(a + b) - Gamma.completeLn(a) - Gamma.completeLn(b) + a * Math.log(x) + b * Math.log(1.0 - x));
        if (x < (a + 1.0) / (a + b + 2.0))
            return (bt * Betacf(a, b, x) / a);
        else
            return (1.0 - bt * Betacf(b, a, 1.0 - x) / b);
    }

    ///////////////////////////////////////////////////////////////////
    //beta function implemented using continued fraction representation
    ///////////////////////////////////////////////////////////////////
    static private double Betacf(double a, double b, double x) {
        float qap, qam, qab, em, tem, d;
        float bz, bm = 1.0F, bp, bpp;
        float az = 1.0F, am = 1.0F, ap, app, aold;
        int m;
        qab = (float) (a + b);
        qap = (float) (a + 1.0);
        qam = (float) (a - 1.0);
        bz = (float) (1.0 - qab * x / qap);
        for (m = 1; m <= 100; m++) {
            em = (float) m;
            tem = em + em;
            d = (float) (em * (b - em) * x / ((qam + tem) * (a + tem)));
            ap = az + d * am;
            bp = bz + d * bm;
            d = (float) (-(a + em) * (qab + em) * x / ((qap + tem) * (a + tem)));
            app = ap + d * az;
            bpp = bp + d * bz;
            aold = az;
            am = ap / bpp;
            bm = bp / bpp;
            az = app / bpp;
            bz = 1.0F;
            if (Math.abs(az - aold) < (3.0e-7 * Math.abs(az)))
                return (az);
        }
        System.out.println("Error: a or b too big, or needs more than 100 iterations");
        return (az);
    }
}
