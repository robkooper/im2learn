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

// TODO really small values for gamma result in sharp exopnential like PDF's, these do not always work correctly.

/**
 * Gamma will implement a beta distribution function. The pdf for a gamma
 * distribution is:
 * <p/>
 * f(x) = ((x-offset)/beta)^(alpha-1)*e^((offset-x)/beta) / beta*Gamma(alpha) x
 * >= offset and alpha, beta > 0
 *
 * @author Rob Kooper
 * @version 1.0 (June 30, 2003)
 */
public class Gamma implements ProbabilityDistribution1D {
    /**
     * Scale of the distribution.
     */
    private double alpha = Double.NaN;

    /**
     * Shape of the distribution.
     */
    private double beta = Double.NaN;

    /**
     * Location of the distribution
     */
    private double offset = Double.NaN;

    /**
     * Precalculate 1 / (complete(alpha) * Math.pow(beta, alpha))
     */
    private double gammaval = Double.NaN;

    /**
     * Create a beta distribution with alpha and beta are 1, and offset is 0.
     *
     * @throws Exception if alpha or beta are less than 0.
     */
    public Gamma() throws Exception {
        this(1, 1, 0);
    }

    /**
     * Create a beta distribution with given alpha and beta, and 0 offset.
     *
     * @throws Exception if alpha or beta are less than 0.
     */
    public Gamma(double alpha, double beta) throws Exception {
        this(alpha, beta, 0);
    }

    /**
     * Create a beta distribution with given mean and standard deviation.
     *
     * @param alpha  of the new normal distribution.
     * @param beta   of the new normal distribution.
     * @param offset of the new normal distribution.
     * @throws Exception if alpha or beta is less or equal to 0.
     */
    public Gamma(double alpha, double beta, double offset) throws Exception {
        if (alpha <= 0)
            throw(new Exception("alpha has to be greater than 0."));
        if (beta <= 0)
            throw(new Exception("beta has to be greater than 0."));

        this.alpha = alpha;
        this.beta = beta;
        this.offset = offset;
        this.gammaval = 1.0 / (beta * complete(alpha));
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
        this.gammaval = 1.0 / (beta * complete(alpha));
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
        this.gammaval = 1.0 / (beta * complete(alpha));
    }

    /**
     * Returns the offset of the distribution.
     *
     * @return offset.
     */
    public double getOffset() {
        return offset;
    }

    /**
     * Sets the offset of the distribution.
     *
     * @param offset is the new value.
     */
    public void setOffset(double offset) {
        this.offset = offset;
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

        this.offset = stats[Estimator.MIN];
        double tmp = stats[Estimator.MEAN] - stats[Estimator.MIN];
        this.alpha = (tmp * tmp) / stats[Estimator.VARIANCE];
        this.beta = stats[Estimator.VARIANCE] / tmp;
        if (alpha <= 0)
            throw(new Exception("alpha has to be greater than 0."));
        if (beta <= 0)
            throw(new Exception("beta has to be greater than 0."));
        this.gammaval = 1.0 / (beta * complete(alpha));
    }

    /**
     * Generate datapoints that fit the beta distribution. This function will
     * fill the data array with values that fit the beta distribution.
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
        if (alpha > 1.0) {
            // Uses R.C.H. Cheng, "The generation of Gamma variables with non-integral shape parameters",
            // Applied Statistics, (1977), 26, No. 1, p71-74

            double ainv = Math.sqrt(2.0 * alpha - 1.0);
            double bbb = alpha - Math.log(4);
            double ccc = alpha + ainv;

            double csg = (1.0 + Math.log(4.5));

            double u1, u2, v, x, z, r;

            for (; ;) {
                do {
                    u1 = Math.random();
                } while (u1 <= 1e-7 || u1 >= 0.999999);
                u2 = 1.0 - Math.random();
                v = Math.log(u1 / (1.0 - u1)) / ainv;
                x = alpha * Math.exp(v);
                z = u1 * u1 * u2;
                r = bbb + ccc * v - x;
                if ((r + csg - 4.5 * z >= 0.0) || (r >= Math.log(z))) {
                    return x * beta + offset;
                }
            }

        } else if (alpha == 1.0) {
            double u1;
            do {
                u1 = Math.random();
            } while (u1 <= 1e-7);
            return -Math.log(u1) * beta + offset;
        } else {
            // Uses ALGORITHM GS of Statistical Computing - Kennedy & Gentle
            double u1, u2, b, p, x;
            b = (Math.E + alpha) / Math.E;
            for (; ;) {
                u1 = Math.random();
                u2 = Math.random();
                p = b * u1;
                if (p <= 1.0) {
                    x = Math.pow(p, 1.0 / alpha);
                } else {
                    x = -Math.log((b - p) / alpha);
                }
                if (!(((p <= 1.0) && (u2 > Math.exp(-x))) ||
                      ((p > 1) && (u2 > Math.pow(x, alpha - 1.0))))) {
                    return x * beta + offset;
                }
            }
        }
    }

    /**
     * Calculate the probablity of x ( Pr(X=x) ). The implementation will return
     * an approximation of the value.
     *
     * @param x the value for which to calculate the probability.
     * @return the probability.
     */
    public double calculatePDF(double x) {
        if (x < offset)
            return 0;
        return gammaval * Math.pow((x - offset) / beta, alpha - 1) * Math.exp((offset - x) / beta);
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
        return gammaP((x - offset) / beta, alpha);
    }

    /**
     * Calculate the probability of a<=X<=b. The implementation will return an
     * approximation of the value.
     *
     * @param a lowerlimit for which to calculate the probability
     * @param b upperlimit for which to calculate the probability
     * @return the probability of a<=X<=b
     */
    public double calculateCDF(double a, double b) {
        if ((b <= a) || (b <= offset))
            return 0;

        if (a <= offset) {
            return gammaP((b - offset) / beta, alpha);
        } else {
            return gammaP((b - offset) / beta, alpha) - gammaP((a - offset) / beta, alpha);
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
        double val = offset;
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
        return offset + alpha * beta;
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
        return Math.sqrt(alpha) * beta;
    }

    /**
     * Returns the exact coefficient of variation of the PDF coefficient of
     * variation = standard deviation / mean
     *
     * @return variance
     */
    public double getCoefficientOfVariation() {
        return 1.0 / Math.sqrt(alpha);
    }

    /**
     * Returns the exact skew of the PDF
     *
     * @return skew
     */
    public double getSkewness() {
        return 2 / Math.sqrt(alpha);
    }

    /**
     * Returns the exact kurtosis of the PDF
     *
     * @return kurtosis
     */
    public double getKurtosis() {
        return 3 * (alpha + 2) / alpha;
    }

    /**
     * Return a description of the distribution.
     *
     * @return description of distribtution.
     */
    public String toString() {
        return "Gamma Distribution with param [" + alpha + " " + beta + " " + offset + "].";
    }

    /**
     * Calculate complete Gamma function for x
     *
     * @param a the value to calculate Gamma for
     * @return the calculate Gamma value
     */
    static public double complete(double a) {
        if (a > 1) {
            return complete3(a);
        } else {
            return complete4(a);
        }
    }

    /////////////////////////////////////////////////////////////////////
    // this is Gamma(x) computation based on Stirling's asymptotic series
    // implemented by Peter Bajcsy
    // accurate for z>=2
    // performance compared with the Gamma table values (not too accurate)
    /////////////////////////////////////////////////////////////////////
    static private double complete1(double z) {
        final double coefPI = 2.5066283; //sqrt(2*3.14159265358979)
        double x, tmp, ser, val;
        int i;
        final double[] coef = {0.0833333333, 0.00347222222, -0.0026813272, -0.0002294};
        x = z - 1.0;
        if (x <= 0.0) {
            if (z == 1.0)
                return (1.0);
            if (z == 0.5)
                return (1.7724539); //sqrt(PI)
            else {
                // use Gamma(z) = Gamma(z+1)/z because
                //Stirling's formula is for Gamma(x+1)=
                x = z;
                tmp = coefPI * Math.sqrt(x) * Math.pow(x, x) * Math.exp(-x);
                ser = 1.0;
                val = x;
                for (i = 0; i < 4; i++) {
                    ser += coef[i] / val;
                    val *= x;
                }
                return (tmp * ser / z); // Gamma(z) = Gamma(z+1)/z
            }
        } else {
            if (x == 1.0)
                return (1.0);
            else {
                // use Gamma(z) = Gamma(x+1) = expansion as a function of x
                tmp = coefPI * Math.sqrt(x) * Math.pow(x, x) * Math.exp(-x);
                ser = 1.0;
                val = x;
                for (i = 0; i < 4; i++) {
                    ser += coef[i] / val;
                    val *= x;
                }
                //    ser = 1.0 + coef[0]/x + coef[1]/(x*x) + coef[2]/(x*x*x);
                return (tmp * ser);
            }
        }
    }

    /////////////////////////////////////////////////////////////////////
    // this is Gamma(x) computation based on Polynomial approximation
    // implemented by Peter Bajcsy
    // error <= 3*10^-7 for 1<= z <=2
    /////////////////////////////////////////////////////////////////////
    static private double complete2(double z) {
        final double[] coef = {-0.577191652, 0.988205891, -0.897056937, 0.918206857,
                               -0.756704078, 0.482199394, -0.193527818, 0.035868343};
        double val, tmp, ser, x;
        int i;
        x = z - 1.0;
        if (x <= 0) { // z in [0,1]
            if (z == 1.0)
                return (1.0);
            if (z == 0.5)
                return (1.7724539); //sqrt(PI)
            else {
                // use Gamma(z) = Gamma(z+1)/z because
                // apply the formula
                // Gamma(x+1) = 1+coef[0]*x+coef[1]*x^2 + coef[7]*x^8 + epsilon(x)
                x = z;
                val = x;
                ser = 1.0;
                for (i = 0; i < 8; i++) {
                    ser += coef[i] * val;
                    val *= x;
                }
                return (ser / z); // Gamma(z) = Gamma(z+1)/z
            }
        }
        if (x <= 1) { // z in (1,2]
            // apply the formula
            // Gamma(x+1) = 1+coef[0]*x+coef[1]*x^2 + coef[7]*x^8 + epsilon(x)
            val = x;
            ser = 1.0;
            for (i = 0; i < 8; i++) {
                ser += coef[i] * val;
                val *= x;
            }
            return (ser);
        } else { //z in (2,inf)
            // use Gamma(n+1) = n * Gamma(n),e.g.,Gamma(2.5)=2.5*1.5*Gamma(1.5)
            tmp = 1.0;
            x = z; // do it for z
            while (x >= 2) {
                tmp *= (x - 1.0);
                x -= 1.0;
            }
            ser = 1.0;
            val = x - 1.0;
            for (i = 0; i < 8; i++) {
                ser += coef[i] * val;
                val *= x;
            }
            return (ser * tmp);
        }
    }

    static private double complete3(double z) {
        double x = completeLn(z);
        return (Math.exp(completeLn(z)));
    }

    /////////////////////////////////////////////////////////////////////
    // this is Gamma(x) computation based on Series expansion for 1/Gamma(z)
    // coefficients from Handbook of mathematical functions p.256
    // implemented by Peter Bajcsy
    // very accurate for z<=2
    /////////////////////////////////////////////////////////////////////
    static private double complete4(double z) {
        if (z < 0) {
            System.out.println("Error: input < 0 ");
            return (0.0);
        }
        final double[] coef = {1.0000000000000000, 0.5772156649015329, -0.6558780715202538,
                               -0.0420026350340952, 0.1665386113822915, -0.0421977345555443,
                               -0.0096219715278770, 0.0072189432466630, -0.0011651675918591,
                               -0.0002152416741149, 0.0001280502823882, -0.0000201348547807,
                               -0.0000012504934821, 0.0000011330272320, -0.0000002056338417,
                               0.0000000061160950, 0.0000000050020075, -0.0000000011812746,
                               0.0000000001043427, 0.0000000000077823, -0.0000000000036968,
                               0.0000000000005100, -0.0000000000000206, -0.0000000000000054,
                               0.0000000000000014, 0.0000000000000001};
        double val, ser;
        int i;
        if (z > 0 && z <= 2) {
            // apply the formula
            // 1/Gamma(z) = sum(c_k * z^k) for k =1, to inf
            val = z;
            ser = 0.0;
            for (i = 0; i < 26; i++) {
                ser += coef[i] * val;
                val *= z;
            }
            if (ser != 0.0)
                return (1.0 / ser);
            else {
                System.out.println("Error: Computed Value = inf");
                return Double.POSITIVE_INFINITY;
            }
        } else {
            if (z > 2) {
                double tmp = 1.0;
                val = z; // do it for z
                while (val >= 2) {
                    tmp *= (val - 1.0);
                    val -= 1.0;
                }
                double temp1 = val;
                ser = 0.0;
                for (i = 0; i < 26; i++) {
                    ser += coef[i] * val;
                    val *= temp1;
                }
                if (ser != 0.0)
                    return (tmp / ser);
                else {
                    System.out.println("Error: Computed Value = inf");
                    return Double.POSITIVE_INFINITY;
                }
            } else {
                System.out.println("Error: Value <= 0 ");
            }
        }
        return (0.0);
    }

    //////////////////////////////////////////////////////////////////
    // Gamma function
    // based on  numerical recipes gammln
    // full accuracy for z > 1
    //////////////////////////////////////////////////////////////////
    static public double completeLn(double z) {
        double x, y, tmp, ser;
        final double[] cof = {76.18009172947146, -86.50532032941677,
                              24.01409824083091, -1.231739572450155,
                              0.1208650973866179e-2, -0.5395239384953e-5};
        int j;
        y = x = z;
        tmp = x + 5.5;
        tmp -= (x + 0.5) * Math.log(tmp);
        ser = 1.000000000190015;
        for (j = 0; j <= 5; j++) {
            y++;
            ser += cof[j] / y;
        }
        return (-tmp + Math.log(2.5066282746310005 * ser / x));
    }

    /**
     * Calculate incomplete Gamma function for x
     *
     * @param x the value to calculate Gamma to
     * @param a the value to calculate Gamma for
     * @return the calculate Gamma value
     */
    static public double incomplete(double x, double a) {
        return gammaP(x, a) * complete(a);
    }

    /**
     * Calculate the inclomplete Gamma function. This approach will calculate
     * the integral by summing the results in small steps.
     *
     * @param x the value to calculate Gamma to
     * @param a the value to calculate Gamma for
     * @return the calculate Gamma value
     */
    static private double incomplete1(double x, double a) {
        double result = 0;
        double step = 1.0 / 100000;

        for (double t = 0; t < x; t += step) {
            // special case for Math.pow(0, <0) is Inf
            if ((t == 0) && (a < 1))
                result += 0;
            else
                result += (Math.pow(t, a - 1) * Math.exp(-t));
        }

        return result * step;
    }

    //////////////////////////////////////////////////////////////////////////
    // returns the regularized incomplete gamma function
    // Q(a,x) = 1 - P(a,x) = Gamma(a,x)/Gamma(a) =
    // 1/Gamma(a) * Int(from x to inf) { x^(-t) * t^(a-1) dt }
    // limiting values : Q(a,0)= 1 and Q(a,inf) = 0
    // implemented using P(a,x) = gamma(a,x)/Gamma(a) where
    // gamma(a,x) = e^(-x)*x^a * sum(from 0 to inf){Gamma(a)/Gamma(a+1+n) * x^n
    // as the series development
    // or continued fraction development
    // Gamma(a,x) = e^(-x)*x^a*(1/x + (1-a)/1 + 1/x + ..)
    // based on Numerical recipes gammq
    //////////////////////////////////////////////////////////////////////////////
    static public double gammaQ(double x, double a) {
        if (x < 0.0 || a <= 0.0) {
            System.out.println(" Error: imvalid arguments in Gamma::EvalQ");
            return 1.0;
        }

        // this if statement is due to convergence
        double gln = completeLn(a);
        if (x < (a + 1.0)) { // use the series representation
            return (1.0 - gser(a, x, gln));
        } else {  // use the continued fraction representation
            return gcf(a, x, gln);
        }
    }

    // Returns the regularized incomplete lower gamma function (CDF)
    static public double gammaP(double x, double a) {
        if (x < 0.0 || a <= 0.0) {
            System.out.println(" Error: imvalid arguments in Gamma::EvalP");
            return 1.0;
        }

        // this if statement is due to convergence
        double gln = completeLn(a);
        if (x < (a + 1.0)) { // use the series representation
            return gser(a, x, gln);
        } else {  // use the continued fraction representation
            return (1.0 - gcf(a, x, gln));
        }
    }

    private final static int ITMAX = 100;
    private final static double EPS_GAMMA = 3.0e-7;

    // series representation of gamma(a,x)
    private static double gser(double a, double x, double gln) {
        int n;
        double sum, del, ap;

        //  *gln = EvalLn(a);
        if (x <= 0.0) {
            if (x < 0.0)
                System.out.println("Error: x < 0 ");
            return 0.0;
        } else {
            ap = a;
            del = sum = 1.0 / a;
            for (n = 1; n <= ITMAX; n++) {
                ap += 1.0;
                del *= x / ap;
                sum += del;
                if (Math.abs(del) < Math.abs(sum) * EPS_GAMMA) {
                    return sum * Math.exp(-x + a * Math.log(x) - (gln));
                }
            }
            System.out.println("Error: a is too large, ITMAX is too small ");
            return sum * Math.exp(-x + a * Math.log(x) - (gln));
        }
    }

    // continued fraction representation
    private static double gcf(double a, double x, double gln) {
        int i;
        double an, b, c, d, del, h;

        b = x + 1.0 - a;
        c = 1.0 / 1e-30;
        d = 1.0 / b;
        h = d;
        for (i = 1; i <= ITMAX; i++) {
            an = -i * (i - a);
            b += 2.0;
            d = an * d + b;
            if (Math.abs(d) < 1e-30) d = 1e-30;
            c = b + an / c;
            if (Math.abs(c) < 1e-30) c = 1e-30;
            d = 1.0 / d;
            del = d * c;
            h *= del;
            if (Math.abs(del - 1.0) < EPS_GAMMA) {
                return Math.exp(-x + a * Math.log(x) - (gln)) * h;
            }
        }
        System.out.println("Error: a is too large, ITMAX is too small");
        return Math.exp(-x + a * Math.log(x) - (gln)) * h;
    }
}
