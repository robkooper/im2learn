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

// TODO why does it not want to work when we bin the data?

/**
 * This class can be used to perfom a CHI-Square test. The actual binned data is
 * compared with the expected data. The value is then converted in to
 * significance.
 *
 * @author Peter Bajcsy, Rob Kooper
 * @version 1.0 (November 20, 2003)
 */
public class ChiSquare {
    /**
     * number of bins used if we are responsible for binning the data.
     */
    private int nobins = 200;

    /**
     * actual data
     */
    private double[] actual = null;

    /**
     * expected data
     */
    private double[] expected = null;

    /**
     * degrees of freedom
     */
    private double df = 0;

    /**
     * chisquare value
     */
    private double chisq = 0;

    /**
     * probability
     */
    private double prob = 0;

    public ChiSquare() {
    }

    public void setActual(double[] actual) throws Exception {
        if ((actual == null) || (actual.length < 1)) {
            throw(new Exception("Need to have data to bin it."));
        }

        this.nobins = actual.length;
        this.actual = new double[actual.length];
        System.arraycopy(actual, 0, this.actual, 0, actual.length);
    }

    public void binActual(double[] data, double min, double max) throws Exception {
        int bin, i;

        if ((data == null) || (data.length < 1)) {
            throw(new Exception("Need to have data to bin it."));
        }
        if (min >= max) {
            throw(new Exception("min has to be less than max"));
        }

        double scale = nobins / (max - min + 1e-7);
        actual = new double[nobins];
        for (i = 0; i < data.length; i++) {
            if ((data[i] >= min) && (data[i] <= max)) {
                bin = (int) Math.floor((data[i] - min) * scale);
                actual[bin]++;
            }
        }
    }

    public void setExpected(double[] expected) throws Exception {
        if ((expected == null) || (expected.length < 1)) {
            throw(new Exception("Need to have data to bin it."));
        }

        this.nobins = expected.length;
        this.expected = new double[expected.length];
        System.arraycopy(expected, 0, this.expected, 0, expected.length);
    }

    public void binExpected(double[] data, double min, double max) throws Exception {
        int bin, i;

        if ((data == null) || (data.length < 1)) {
            throw(new Exception("Need to have data to bin it."));
        }
        if (min >= max) {
            throw(new Exception("min has to be less than max"));
        }

        double scale = nobins / (max - min + 1e-7);
        expected = new double[nobins];
        for (i = 0; i < data.length; i++) {
            if ((data[i] >= min) && (data[i] <= max)) {
                bin = (int) Math.floor((data[i] - min) * scale);
                expected[bin]++;
            }
        }
    }

    public int getNobins() {
        return nobins;
    }

    public void setNobins(int nobins) {
        this.nobins = nobins;
    }

    public void calculate() throws Exception {
        double[] df = new double[1];
        double[] chsq = new double[1];
        double[] prob = new double[1];

        if ((actual.length != nobins) || (expected.length != nobins)) {
            throw(new Exception("actual and expected need to have same number of bins to compare."));
        }

        calculate(actual, expected, 0, df, chsq, prob);

        this.df = df[0];
        this.chisq = chsq[0];
        this.prob = prob[0];

        System.out.println(nobins + "\t" + this.df + "\t" + this.chisq + "\t" + this.prob);
        for (int i = 0; i < nobins; i++) {
            System.out.println(actual[i] + "\t" + expected[i]);
        }
    }

    public double getDegreesOfFreedom() {
        return df;
    }

    public double getChiSquare() {
        return chisq;
    }

    public double getProbability() {
        return prob;
    }

    /**
     * Performs Ch-square test of an unknown dist. against a known dist based on
     * Numerical recipes. Null hypothesis: the two dist are identical
     *
     * @param actual   contains unknown data
     * @param expected contains the known data
     * @param startbin contains the first bin to compare
     * @param endbin   contains the last bin to compare
     * @param knstrn   constraints (normally zero)
     * @param df       degrees of freedom
     * @param chsq     sum(over all samples) {(measured - expected)^2/expected,
     *                 large chsq => null hypothesis is unlikely
     * @param prob     chi-square prob. function = incomplete gamma function
     */
    static public void calculate(double[] actual, double[] expected, int startbin, int endbin, float knstrn,
                                 double[] df, double[] chsq, double[] prob) {
        int j, count;
        double temp;
        double factor = 1;
        count = 0;
        chsq[0] = 0.0;
        if (actual.length != expected.length) {
            System.err.println("Error: Bins need to be same size.");
            return;
        }
        if (endbin > actual.length) {
            System.err.println("Error: endbin need to be less or equal to actual.length.");
            return;
        }
        if (startbin < 0) {
            System.err.println("Error: startbin need to be more or equal to 0.");
            return;
        }

        // First count the samples
        double count_actual = 0;
        double count_expected = 0;
        for (j = startbin; j <= endbin; j++) {
            if (expected[j] > 0.0) {
                count_actual += actual[j];
                count_expected += expected[j];
            }
        }
        factor = count_expected / count_actual;

        for (j = startbin; j <= endbin; j++) {
            if ((expected[j] <= 0.0) && (actual[j] <= 0.0)) {
                //if (expected[j] <= 0.0) {
                //System.err.println("Error: Bad expected number in ChiSquare");
            } else {
                //temp = (actual[j] * factor) - expected[j];
                //chsq[0] += ((temp * temp) / expected[j]);
                temp = actual[j] - expected[j];
                chsq[0] += ((temp * temp) / (actual[j] + expected[j]));
                count++;
            }
        }

        df[0] = count - 1 - knstrn;
        if (df[0] > 0) {
            prob[0] = (float) Gamma.gammaQ((0.5 * chsq[0]), (0.5 * df[0]));
        } else {
            prob[0] = 0.5F;
            System.out.println("Warning: too few hist values are nonzero !! ");
        }
    }

    /**
     * Performs Ch-square test of an unknown dist. against a known dist based on
     * Numerical recipes. Null hypothesis: the two dist are identical
     *
     * @param actual   contains unknown data
     * @param expected contains the known data
     * @param knstrn   constraints (normally zero)
     * @param df       degrees of freedom
     * @param chsq     sum(over all samples) {(measured - expected)^2/expected,
     *                 large chsq => null hypothesis is unlikely
     * @param prob     chi-square prob. function = incomplete gamma function
     */
    static public void calculate(double[] actual, double[] expected, float knstrn,
                                 double[] df, double[] chsq, double[] prob) {
        calculate(actual, expected, 0, actual.length - 1, knstrn, df, chsq, prob);
    }
}
