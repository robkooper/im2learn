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
import java.util.Random;

/**
 * This probability distribution function will take the give data to the
 * estimator and split it such that there is a set of data containing all the
 * data resulting in 0 and a PDF for the rest. This is specific case for a class
 * that does not exist but will take the data and split it in 3 parts (some of
 * which might be null), left of the section of data with constant value, the
 * constant value, and right of this section.
 * <p/>
 * This class will calculate the probability of falling in this continous
 * section, or in the other section. Based on the chance it will create a PDF
 * that will either result in 0, or a value from the other PDF.
 */
public class Combo2PDF implements ProbabilityDistribution1D {
    private static double error = 1e-5;
    private static Random random = new Random();

    private ProbabilityDistribution1D pdf = null;
    private double prob = 0;

    /**
     * Give the data, sort it, remove all the 0's, and find a johnson for the
     * other part of the data.
     *
     * @param data that needs estimation
     * @throws Exception if no PDF combo could be created.
     */
    public void estimateParameters(double[] data) throws Exception {
        int len = data.length;
        double[] sorted = new double[len];

        // sort the data
        System.arraycopy(data, 0, sorted, 0, len);
        Arrays.sort(sorted);

        // need to start at 0
        if (sorted[0] != 0) {
            throw(new Exception("Data does not start at 0"));
        }

        // clip all the 0's
        int count = 0;
        while ((sorted[count] <= error) && (count < len)) {
            count++;
        }

        // find the johnson that matches the non-zero data.
        Estimator estimator = new Estimator();
        pdf = estimator.EstimationJohnson(sorted, count, len - 1, "");

        // calculate the probability that we need to use PDF
        // tricky part here is that part of the PDF might result in 0 value,
        // and this part should not be counted towards PDF chance. So we
        // need to find prob = (#nonzero / #total) / (1 - CDF(X<=0))
        prob = (1.0 - ((double) count / len)) / (1.0 - pdf.calculateCDF(0));
    }

    public void generateData(double[] data) {
        // have to calculate for each data point if we use PDF or 0
        int len = data.length;
        for (int i = 0; i < len; i++) {
            data[i] = generate();
        }
    }

    public double generate() {
        if (random.nextDouble() < prob) {
            return pdf.generate();
        } else {
            return 0;
        }
    }

    // TODO implement
    public double calculatePDF(double x) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    // TODO implement
    public double calculateCDF(double x) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    // TODO implement
    public double calculateCDF(double a, double b) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    // TODO implement
    public double inverseCDF(double cdf) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    // TODO implement
    public double getMean() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    // TODO implement
    public double getMedian() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    // TODO implement
    public double getStandardDeviation() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    // TODO implement
    public double getCoefficientOfVariation() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    // TODO implement
    public double getSkewness() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    // TODO implement
    public double getKurtosis() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    // TODO implement
    public String toString() {
        int x = (int) (100 * (1.0 - prob + pdf.calculateCDF(0)));
        return "Combination of 0 (" + x + "%) and " + pdf.toString();
    }
}
