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
package edu.illinois.ncsa.isda.imagetools.ext.statistics;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageException;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObjectByte;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObjectDouble;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObjectFloat;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObjectInt;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObjectLong;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObjectShort;
import edu.illinois.ncsa.isda.imagetools.core.datatype.LimitValues;
import edu.illinois.ncsa.isda.imagetools.core.datatype.SubArea;
import edu.illinois.ncsa.isda.imagetools.ext.calculator.ImageCalculator;
import edu.illinois.ncsa.isda.imagetools.ext.statistics.pdf.Beta;
import edu.illinois.ncsa.isda.imagetools.ext.statistics.pdf.ChiSquare;

/**
 * ImageCompare two images with each other. If a mask is given, split the image
 * in subimages, and compare the mask area's. This class will expand as more
 * evaluation methods are implemented.
 * <p/>
 * <b>This class is not threadsafe.</b>
 * 
 * @author Rob Kooper
 */
public class ImageCompare {
    private boolean     inverse       = false;
    private String      resultType    = null;
    private double      multiplier    = 1.0;
    private ImageObject testImage     = null;
    private ImageObject maskImage     = null;
    private ImageObject originalImage = null;
    private ImageObject resultImage   = null;
    private int         histnobins    = 400;

    private static Log  logger        = LogFactory.getLog(ImageCompare.class);

    public ImageCompare() {
    }

    /**
     * Return the image that is tested.
     * 
     * @return image that is tested
     */
    public ImageObject getTestImage() {
        return testImage;
    }

    /**
     * Set the image to be tested.
     * 
     * @param testImage
     *            image to be tested.
     */
    public void setTestImage(ImageObject testImage) {
        this.testImage = testImage;
    }

    /**
     * Return the image used as a mask.
     * 
     * @return image used as a mask.
     */
    public ImageObject getMaskImage() {
        return maskImage;
    }

    /**
     * Set the image to be used as a mask.
     * 
     * @param maskImage
     *            image to be used as a mask.
     */
    public void setMaskImage(ImageObject maskImage) {
        this.maskImage = maskImage;
    }

    /**
     * Return the image against whom the testimage is compared.
     * 
     * @return original image.
     */
    public ImageObject getOriginalImage() {
        return originalImage;
    }

    /**
     * Set the image against whom the test image is compared.
     * 
     * @param originalImage
     *            the image again whom to compare.
     */
    public void setOriginalImage(ImageObject originalImage) {
        this.originalImage = originalImage;
    }

    /**
     * Return the image, if any, resulting from the compare operation.
     * 
     * @return the resulting image
     */
    public ImageObject getResultImage() {
        return resultImage;
    }

    /**
     * Return the multiplier used when creating the resultimage.
     * 
     * @return multiplier used.
     */
    public double getMultiplier() {
        return multiplier;
    }

    /**
     * Set the multiplier to be used when creating the resultimage
     * 
     * @param multiplier
     *            to be used.
     */
    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }

    /**
     * When binning the data to do a histogram comparison the number of bins in
     * which the data is put is needed. This with the min and max value of the
     * data set will determine the bin width.
     * 
     * @return the number of bins used in histogram.
     */
    public int getHistnobins() {
        return histnobins;
    }

    /**
     * When binning the data to do a histogram comparison the number of bins in
     * which the data is put is needed. This with the min and max value of the
     * data set will determine the bin width.
     * 
     * @param histnobins
     *            the number of bins used in histogram.
     */
    public void setHistnobins(int histnobins) {
        this.histnobins = histnobins;
    }

    /**
     * Returns true if the resultimage is inverse, ie white where there is very
     * little change and black where there is a lot.
     * 
     * @return wheter or not to inverse the resultimage
     */
    public boolean isInverse() {
        return inverse;
    }

    /**
     * Set wheter or not to inverse the resultimage
     * 
     * @param inverse
     *            the resultimage
     */
    public void setInverse(boolean inverse) {
        this.inverse = inverse;
    }

    /**
     * Return what type of image the resultimage should be. This is either empty
     * or the type recognized by Im2Learn.
     * 
     * @return type of the resultimage
     */
    public String getResultType() {
        return resultType;
    }

    /**
     * Set the type the resultimage should be. If the value is not recognized by
     * Im2Learn as a valid type, it will try to guess the best type to be used
     * based on the input images.
     * 
     * @param resultType
     *            the type of the resultimage
     */
    public void setResultType(String resultType) {
        if (resultType.equals("DOUBLE") || resultType.equals("FLOAT") || resultType.equals("LONG") || resultType.equals("INT") || resultType.equals("SHORT") || resultType.equals("BYTE")) {
            this.resultType = resultType;
        } else {
            this.resultType = null;
        }
    }

    /**
     * Subtract the original image from the test image. If a mask is given it
     * will first average the color for each mask value and then subtract the
     * average color for each mask value. After subtracting the code will
     * calculate the maximum error, the average error, the mean square error and
     * the S/N ratio and return these in an array of doubles. The code will also
     * color code the result image and store there the error for each pixel as
     * calculated during the subtraction.
     * 
     * @return an array with max, average, mean square error and S/N ratio
     * @throws ImageException
     *             if the test and original image do not have the same
     *             attributes.
     */
    public double[][][] subtract() throws ImageException {
        double mse = 0;
        double val = 0;
        double[][][] result = null;
        int m, i, b, p;
        Double key;

        sanityTest();
        resultImage = createResult();

        logger.debug("Subtract Test");

        // find out what to subtract in case of inversion
        double sub = 0.0;
        if (inverse) {
            sub = resultImage.getTypeMax() - resultImage.getTypeMin();
        }

        // if there is no mask do a pixel by pixle subtraction
        if (maskImage == null) {
            result = new double[1][originalImage.getNumBands() + 1][4];

            for (b = 0; b < originalImage.getNumBands(); b++) {
                result[0][b][0] = Double.MIN_VALUE;
                result[0][b][3] = 0;
            }

            for (i = 0; i < resultImage.getSize();) {
                for (b = 0; b < originalImage.getNumBands(); b++, i++) {
                    val = Math.abs(originalImage.getDouble(i) - testImage.getDouble(i));
                    resultImage.set(i, sub - multiplier * val);

                    if (val > result[0][b][0]) {
                        result[0][b][0] = val;
                    }
                    result[0][b][1] += val;
                    result[0][b][2] += val * val;
                    result[0][b][3]++;
                }
            }

            result[0][originalImage.getNumBands()][0] = 0;
            result[0][originalImage.getNumBands()][1] = 0;

            for (b = 0; b < originalImage.getNumBands(); b++) {
                mse = result[0][b][2];
                result[0][b][1] /= result[0][b][3];
                result[0][b][2] /= result[0][b][3];
                result[0][b][3] = 20 * Math.log(255.0 / Math.sqrt(mse)) / Math.log(10.0);

                result[0][originalImage.getNumBands()][1] += result[0][b][1];
            }

            result[0][originalImage.getNumBands()][1] /= originalImage.getNumBands();

        } else {
            Vector mask = getMaskCount();
            int maskcount = mask.size();
            result = new double[maskcount][originalImage.getNumBands() + 1][4];

            for (m = 0; m < maskcount; m++) {

                // initialize
                for (b = 0; b < originalImage.getNumBands(); b++) {
                    result[m][b][0] = Double.MIN_VALUE;
                    result[m][b][3] = 0;
                }
                result[m][originalImage.getNumBands()][0] = Double.parseDouble(mask.get(m).toString());

                // calculate
                for (p = 0, i = 0; i < resultImage.getSize(); p += maskImage.getNumBands()) {
                    key = new Double(maskImage.getDouble(p));
                    if (key.equals(mask.get(m))) {
                        for (b = 0; b < originalImage.getNumBands(); b++, i++) {
                            val = Math.abs(originalImage.getDouble(i) - testImage.getDouble(i));

                            resultImage.set(i, sub - multiplier * val);

                            if (val > result[m][b][0]) {
                                result[m][b][0] = val;
                            }
                            result[m][b][1] += val;
                            result[m][b][2] += val * val;
                            result[m][b][3]++;
                        }
                    } else {
                        i += originalImage.getNumBands();
                    }
                }

                // average out
                result[m][originalImage.getNumBands()][1] = 0;
                result[m][originalImage.getNumBands()][2] = result[m][0][3];

                for (b = 0; b < originalImage.getNumBands(); b++) {
                    mse = result[m][b][2];
                    result[m][b][1] /= result[m][b][3];
                    result[m][b][2] /= result[m][b][3];
                    result[m][b][3] = 20 * Math.log(255.0 / Math.sqrt(mse)) / Math.log(10.0);

                    result[m][originalImage.getNumBands()][1] += result[m][b][1];
                }

                result[m][originalImage.getNumBands()][1] /= originalImage.getNumBands();

            }
        }

        if (logger.isDebugEnabled()) {
            for (m = 0; m < result.length; m++) {
                logger.debug(" mask val " + result[m][result[m].length - 1][0]);
                for (b = 0; b < result[m].length - 1; b++) {
                    logger.debug(" band # " + b);
                    logger.debug("  maximum error     : " + result[m][b][0]);
                    logger.debug("  average error     : " + result[m][b][1]);
                    logger.debug("  Mean Square Error : " + result[m][b][2]);
                    logger.debug("  S/N (inf is good) : " + result[m][b][3]);
                }
                logger.debug(" average error     : " + result[m][result[m].length - 1][1]);
            }
        }

        return result;
    }

    /**
     * Calculate the pearson correlation between two images. Besides the
     * correlation the fisher z transformation, the degrees of freedom, the
     * student t probability and the signal to noise ratio is calculated.
     * 
     * @return an array for each band with the above values and the summation of
     *         all errors per band.
     * @throws Exception
     *             if the test and original image do not have the same
     *             attributes.
     */
    public double[][][] correlation() throws Exception {
        logger.debug("Correlation Test");
        sanityTest();

        double tmp;
        double[][][] result = null;
        int pix = originalImage.getNumCols() * originalImage.getNumRows();
        int m, i, b, p;

        if (maskImage == null) {

            // assume a single mask value
            result = new double[1][originalImage.getNumBands() + 1][5];

            // calculate the average
            double[] avgorg = new double[originalImage.getNumBands()];
            double[] avgtst = new double[originalImage.getNumBands()];

            for (i = 0; i < originalImage.getSize();) {
                for (b = 0; b < originalImage.getNumBands(); b++, i++) {
                    avgorg[b] += originalImage.getDouble(i);
                    avgtst[b] += testImage.getDouble(i);
                }
            }

            for (b = 0; b < originalImage.getNumBands(); b++) {
                avgorg[b] /= pix;
                avgtst[b] /= pix;
            }

            // per band calculate
            for (b = 0; b < originalImage.getNumBands(); b++) {
                double xt = 0;
                double yt = 0;
                double sxx = 0;
                double syy = 0;
                double sxy = 0;
                int n = 0;

                // calculate pearson
                // r = sum( (xi - xavg)*(yi - yavg) ) / ( sqrt( sum(xi - xavg)^2
                // ) * sqrt( sumg(yi - yavg) ^ 2 ) )
                for (i = b; i < originalImage.getSize(); i += originalImage.getNumBands()) {
                    xt = originalImage.getDouble(i) - avgorg[b];
                    yt = testImage.getDouble(i) - avgtst[b];

                    sxx += xt * xt;
                    syy += yt * yt;
                    sxy += xt * yt;
                    n++;
                }
                // Check whether sxx or syy are zero or not
                if ((sxx > LimitValues.EPSILON3) && (syy > LimitValues.EPSILON3)) {
                    result[0][b][0] = Math.abs(sxy / Math.sqrt(sxx * syy));
                } else {
                    // logger.warn("Could not compute correlation, no difference found in orig (syy="
                    // + syy + ") or test (sxx=" + sxx + ") images (sxy=" + sxy
                    // + ").");
                    if (sxx <= LimitValues.EPSILON3 && syy <= LimitValues.EPSILON3 && Math.abs(avgorg[b] - avgtst[b]) < LimitValues.EPSILON3) {
                        result[0][b][0] = 1;
                    } else if (sxx <= LimitValues.EPSILON3 && syy <= LimitValues.EPSILON3 && Math.abs(avgorg[b] - avgtst[b]) >= LimitValues.EPSILON3) {
                        result[0][b][0] = 0;
                    }
                    /*
                     * check whether one of the regions is homogeneous and the
                     * other one is not in such cases we compute
                     * |original_image-testImage|/|original.size|*|max(dif)
                     * -min(dif)|;
                     */
                    else if (sxx <= LimitValues.EPSILON3 || syy <= LimitValues.EPSILON3) {

                        ImageCalculator cal = new ImageCalculator();
                        ImageObject dif = cal.abs(cal.subtract(originalImage, testImage));
                        ;
                        double sum = 0;
                        for (i = 0; i < dif.getSize(); i++) {
                            sum += dif.getDouble(i);
                        }
                        result[0][b][0] = Math.exp(-(sum / ((dif.getMax() - dif.getMin()) * dif.getSize())));
                    }
                }

                // calculate rest
                result[0][b][1] = 0.5 * Math.log((1.0 + result[0][b][0] + 1e-20) / (1.0 - result[0][b][0] + 1e-20));
                result[0][b][2] = n - 2;
                tmp = result[0][b][0] * Math.sqrt(result[0][b][2] / ((1.0 - result[0][b][0] + 1e-20) * (1.0 + result[0][b][0] + 1e-20)));
                result[0][b][3] = Beta.incomplete(0.5 * result[0][b][2], 0.5, result[0][b][2] / (result[0][b][2] + tmp * tmp));
                result[0][b][4] = Math.sqrt(result[0][b][0] / (1 - result[0][b][0]));
                result[0][originalImage.getNumBands()][1] += Math.abs(result[0][b][0]) / originalImage.getNumBands();
            }

        } else {
            Double key = null;
            Vector mask = getMaskCount();
            int maskcount = mask.size();
            result = new double[maskcount][originalImage.getNumBands() + 1][5];

            for (m = 0; m < maskcount; m++) {
                result[m][originalImage.getNumBands()][0] = Double.parseDouble(mask.get(m).toString());
                // calculate the average
                double[] avgorg = new double[originalImage.getNumBands()];
                double[] avgtst = new double[originalImage.getNumBands()];

                pix = 0;
                for (i = 0, p = 0; i < originalImage.getSize(); p += maskImage.getNumBands()) {
                    key = new Double(maskImage.getDouble(p));
                    if (key.equals(mask.get(m))) {
                        for (b = 0; b < originalImage.getNumBands(); b++, i++) {
                            avgorg[b] += originalImage.getDouble(i);
                            avgtst[b] += testImage.getDouble(i);
                            pix++;
                        }
                    } else {
                        i += originalImage.getNumBands();
                    }
                }

                for (b = 0; b < originalImage.getNumBands(); b++) {
                    avgorg[b] /= pix;
                    avgtst[b] /= pix;
                }

                // per band calculate
                for (b = 0; b < originalImage.getNumBands(); b++) {
                    double xt = 0;
                    double yt = 0;
                    double sxx = 0;
                    double syy = 0;
                    double sxy = 0;
                    int n = 0;

                    // calculate pearson
                    // r = sum( (xi - xavg)*(yi - yavg) ) / ( sqrt( sum(xi -
                    // xavg)^2 ) * sqrt( sumg(yi - yavg) ^ 2 ) )
                    for (i = b, p = 0; i < originalImage.getSize(); i += originalImage.getNumBands(), p += maskImage.getNumBands()) {
                        key = new Double(maskImage.getDouble(p));
                        if (key.equals(mask.get(m))) {
                            xt = originalImage.getDouble(i) - avgorg[b];
                            yt = testImage.getDouble(i) - avgtst[b];

                            sxx += xt * xt;
                            syy += yt * yt;
                            sxy += xt * yt;
                            n++;
                        }
                    }
                    // Check whether sxx or syy are zero or not

                    if ((sxx > LimitValues.EPSILON3) && (syy > LimitValues.EPSILON3)) {
                        result[0][b][0] = Math.abs(sxy / Math.sqrt(sxx * syy));
                    } else {
                        // logger.warn("Could not compute correlation, no difference found in orig (syy="
                        // + syy + ") or test (sxx=" + sxx + ") images (sxy=" +
                        // sxy + ").");
                        if (sxx <= LimitValues.EPSILON3 && syy <= LimitValues.EPSILON3 && Math.abs(avgorg[b] - avgtst[b]) < LimitValues.EPSILON3) {
                            result[0][b][0] = 1;
                        } else if (sxx <= LimitValues.EPSILON3 && syy <= LimitValues.EPSILON3 && Math.abs(avgorg[b] - avgtst[b]) >= LimitValues.EPSILON3) {
                            result[0][b][0] = 0;
                        }
                        /*
                         * check whether one of the regions is homogeneous and
                         * the other one is not in such cases we compute
                         * |original_image-testImage|/|original.size|*|max(dif)
                         * -min(dif)|;
                         */
                        else if (sxx <= LimitValues.EPSILON3 || syy <= LimitValues.EPSILON3) {

                            ImageCalculator cal = new ImageCalculator();
                            ImageObject dif = cal.abs(cal.subtract(originalImage, testImage));
                            ;
                            double sum = 0;
                            for (i = 0; i < dif.getSize(); i++) {
                                sum += dif.getDouble(i);
                            }
                            result[0][b][0] = Math.exp(-(sum / ((dif.getMax() - dif.getMin()) * dif.getSize())));
                        }
                    }
                    // calculate rest
                    result[m][b][1] = 0.5 * Math.log((1.0 + result[m][b][0] + 1e-20) / (1.0 - result[m][b][0] + 1e-20));
                    result[m][b][2] = n - 2;
                    tmp = result[m][b][0] * Math.sqrt(result[m][b][2] / ((1.0 - result[m][b][0] + 1e-20) * (1.0 + result[m][b][0] + 1e-20)));
                    result[m][b][3] = Beta.incomplete(0.5 * result[m][b][2], 0.5, result[m][b][2] / (result[m][b][2] + tmp * tmp));
                    result[m][b][4] = Math.sqrt(result[m][b][0] / (1 - result[m][b][0]));
                    result[m][originalImage.getNumBands()][1] += Math.abs(result[m][b][0]) / originalImage.getNumBands();
                }
            }
        }

        // the results
        if (logger.isDebugEnabled()) {
            for (m = 0; m < result.length; m++) {
                logger.debug("  Mask Value " + result[m][originalImage.getNumBands()][0]);
                logger.debug("  Average Magnitude of Correlations   : " + result[m][originalImage.getNumBands()][1]);

                for (b = 0; b < originalImage.getNumBands(); b++) {
                    logger.debug("  band " + b);
                    logger.debug("  Linear Correlation (or Pearson's r) : " + result[m][b][0]);
                    logger.debug("  Fisher's z transformation           : " + result[m][b][1]);
                    logger.debug("  Degrees of Freedom                  : " + result[m][b][2]);
                    logger.debug("  Student's t probability             : " + result[m][b][3]);
                    logger.debug("  Signal to Noise Ratio               : " + result[m][b][4]);
                }
            }
        }

        resultImage = null;
        return result;
    }

    /**
     * Perform a chi square test with the two input images and the mask. It will
     * create a histgram for each mask value of both in1 and in2 and compare the
     * two images with each other using the standard ChiSquare methods. The
     * original image is the expected result and the test is the actual result.
     * The function will return the total chi-square value, the maximum
     * chi-square for a mask/band and the number of mask values. The rest of the
     * array is filled with [mask][band][0] = mask value, [mask][band][1] =
     * chi-square value, [mask][band][2] = degrees of freedom and
     * [mask][band][3] = probability of the actual coming from the expected.
     * 
     * @return an 3-D array with all the test results.
     * @throws Exception
     *             if the test and original image do not have the same
     *             attributes.
     */
    public double[][][] chiSquare() throws Exception {
        sanityTest();
        resultImage = createResult();

        return chiSquare(originalImage, testImage);
    }

    /**
     * This will perform a chisquare test as described in chiSquare except that
     * in this function the original and test image will be collapsed into a
     * single band image by taking the RGB values and combining them into a
     * single int value. Next it will perform a chi square test with the two
     * input images and the mask. It will create a histgram for each mask value
     * of both in1 and in2 and compare the two images with each other using the
     * standard ChiSquare methods. The original image cis the expected result
     * and the test is the actual result. The function will return the total
     * chi-square value, the maximum chi-square for a mask/band and the number
     * of mask values. The rest of the array is filled with [mask][band][0] =
     * mask value, [mask][band][1] = chi-square value, [mask][band][2] = degrees
     * of freedom and [mask][band][3] = probability of the actual coming from
     * the expected.
     * 
     * @param bitwise
     *            will determine how to combine the 3 bytes into a single int,
     *            either as RGB (false) or as R1G1B1R2G2B2....
     * @return an 3-D array with all the test results.
     * @throws Exception
     *             if the test and original image do not have the same
     *             attributes.
     */
    public double[][][] chiSquareCombined(boolean bitwise) throws Exception {
        sanityTest();
        if (originalImage.getNumBands() != 3) {
            throw (new Exception("Only works for 3 bands byte images."));
        }
        if (originalImage.getType() != ImageObject.TYPE_BYTE) {
            throw (new Exception("Only works for 3 bands byte images."));
        }

        logger.debug("ChiSquare Combined Test");

        // convert the original and test image to be of type int with all
        // data in the int by combining bands.
        int maxdataval = 0;
        ImageObject originalint = new ImageObjectInt(originalImage.getNumRows(), originalImage.getNumCols(), 1);
        ImageObject testint = new ImageObjectInt(testImage.getNumRows(), testImage.getNumCols(), 1);

        for (int i = 0; i < originalImage.getNumCols() * originalImage.getNumRows(); i++) {
            if (bitwise) {
                // more complex make RGBRGBRGBRGBRGB bit by bit tuple
                originalint.set(i, 0);
                testint.set(i, 0);
                for (int x = 0; x < 8; x++) {
                    int m = 1 << x;
                    testint.set(i, (testint.getInt(i) << 1) + ((testImage.getByte(i * 3 + 0) & m) >> x));
                    testint.set(i, (testint.getInt(i) << 1) + ((testImage.getByte(i * 3 + 1) & m) >> x));
                    testint.set(i, (testint.getInt(i) << 1) + ((testImage.getByte(i * 3 + 2) & m) >> x));

                    originalint.set(i, (originalint.getInt(i) << 1) + ((originalImage.getByte(i * 3 + 0) & m) >> x));
                    originalint.set(i, (originalint.getInt(i) << 1) + ((originalImage.getByte(i * 3 + 1) & m) >> x));
                    originalint.set(i, (originalint.getInt(i) << 1) + ((originalImage.getByte(i * 3 + 2) & m) >> x));
                }
            } else {
                // two methods to convert 3 bands byte to int, simply make RGB
                // tuple
                originalint.set(i, (originalImage.getByte(i * 3 + 0) & 0xff) << 16 + (originalImage.getByte(i * 3 + 1) & 0xff) << 8 + (originalImage.getByte(i * 3 + 2) & 0xff));
                testint.set(i, (testImage.getByte(i * 3 + 0) & 0xff) << 16 + (testImage.getByte(i * 3 + 1) & 0xff) << 8 + (testImage.getByte(i * 3 + 2) & 0xff));
            }
            if (originalint.getInt(i) > maxdataval) {
                maxdataval = originalint.getInt(i);
            }
            if (testint.getInt(i) > maxdataval) {
                maxdataval = testint.getInt(i);
            }
        }

        logger.debug("max value = " + maxdataval);

        resultImage = new ImageObjectInt(originalint.getNumRows(), originalint.getNumCols(), 1);

        return chiSquare(originalint, testint);
    }

    /**
     * This will perform a chisquare test as described in chiSquare except that
     * in this function the original and test image will be collapsed into a
     * single band image by taking the RGB values and combining them into a
     * single byte value. It does this by taking the upper 3 bits of red and
     * green and the upper 2 bits of blue and combine those values into a single
     * byte value. Next it will perform a chi square test with the two input
     * images and the mask. It will create a histgram for each mask value of
     * both in1 and in2 and compare the two images with each other using the
     * standard ChiSquare methods. The original image cis the expected result
     * and the test is the actual result. The function will return the total
     * chi-square value, the maximum chi-square for a mask/band and the number
     * of mask values. The rest of the array is filled with [mask][band][0] =
     * mask value, [mask][band][1] = chi-square value, [mask][band][2] = degrees
     * of freedom and [mask][band][3] = probability of the actual coming from
     * the expected.
     * 
     * @return an 3-D array with all the test results.
     * @throws Exception
     *             if the test and original image do not have the same
     *             attributes.
     */
    public double[][][] chiSquareSubset() throws Exception {
        sanityTest();
        if (originalImage.getNumBands() != 3) {
            throw (new Exception("Only works for 3 bands byte images."));
        }
        if (originalImage.getType() != ImageObject.TYPE_BYTE) {
            throw (new Exception("Only works for 3 bands byte images."));
        }

        logger.debug("ChiSquare Combined Test");

        // convert the original and test image to be of type int with all
        // data in the int by combining bands.
        int maxdataval = 0;
        ImageObject originalbyte = new ImageObjectByte(originalImage.getNumRows(), originalImage.getNumCols(), 1);
        ImageObject testbyte = new ImageObjectByte(testImage.getNumRows(), testImage.getNumCols(), 1);

        for (int i = 0; i < originalImage.getNumCols() * originalImage.getNumRows(); i++) {
            originalbyte.set(i, originalImage.getByte(i * 3 + 0) & 0xE0 + originalImage.getByte(i * 3 + 1) & 0xE0 >> 3 + originalImage.getByte(i * 3 + 2) & 0xC0 >> 6);
            testbyte.set(i, testImage.getByte(i * 3 + 0) & 0xE0 + testImage.getByte(i * 3 + 1) & 0xE0 >> 3 + testImage.getByte(i * 3 + 2) & 0xC0 >> 6);

            if (originalbyte.getByte(i) > maxdataval) {
                maxdataval = originalbyte.getByte(i);
            }
            if (testbyte.getByte(i) > maxdataval) {
                maxdataval = testbyte.getByte(i);
            }
        }
        logger.debug("max value = " + maxdataval);

        resultImage = new ImageObjectInt(originalbyte.getNumRows(), originalbyte.getNumCols(), 1);

        return chiSquare(originalbyte, testbyte);
    }

    /**
     * Performs a histogram comparison. This function will create histograms of
     * the original and the test image (using the mask to filter each image as
     * necessary). Next it will compare the histograms by subtracting the
     * expected percentage from the found percentage. It will add all these
     * together to come up with the per band/mask error. Adding all these errors
     * will give the per mask error, and adding all these will give the total
     * error. The lower the number the better.
     * 
     * @return an array of errors per mask, per band. [mask][band][0] contains
     *         the mask value, [mask][band][1] contains the error for that
     *         mask/band, [mask][band][2] contains the error for that mask.
     *         [0][0][0] contains the maximum error, [0][0][1] contains the
     *         total error and [0][0][2] contains the number of mask values.
     * @throws Exception
     *             if the test and original image do not have the same
     *             attributes
     */
    public double[][][] histogram() throws Exception {
        sanityTest();
        resultImage = createResult();

        double max = Double.MIN_VALUE;

        logger.debug("Histogram test");

        Hashtable done = new Hashtable();
        Histogram hist_orig = new Histogram();
        Histogram hist_test = new Histogram();

        hist_orig.SetMaxDataVal(originalImage.getMax());
        hist_orig.SetMinDataVal(originalImage.getMin());
        hist_orig.SetNumBins(histnobins);
        hist_orig.SetIs256Bins(false);

        hist_test.SetMaxDataVal(originalImage.getMax());
        hist_test.SetMinDataVal(originalImage.getMin());
        hist_test.SetNumBins(histnobins);
        hist_test.SetIs256Bins(false);

        double[][] out = new double[hist_orig.GetNumBins()][3];
        double error_all = 0;

        // go through mask image and create histogram for each mask value
        if (maskImage != null) {
            for (int i = 0; i < maskImage.getNumRows() * maskImage.getNumCols(); i++) {
                Double mask = new Double(maskImage.getDouble(i * maskImage.getNumBands()));
                if (!done.containsKey(mask)) {

                    double[][] error = new double[originalImage.getNumBands()][2];

                    error[0][1] = 0;

                    for (int b = 0; b < originalImage.getNumBands(); b++) {

                        // create the histogram
                        hist_orig.HistMask(originalImage, b, maskImage, mask.doubleValue());
                        hist_test.HistMask(testImage, b, maskImage, mask.doubleValue());

                        // copy array
                        // set norm_orig to count percentage of pixels in wrong
                        // bin, or 0.5 to count pixels in wrong bin.
                        // 1 = to get number of pixels wrong
                        // #count = to get the percentage of pixels
                        // * 2 = to count the number of pixels in wrong bin
                        // (i.e. in bin, but should be in bin 2)

                        hist_orig.NumSamples();
                        float norm_orig = hist_orig.GetNumSamples() * 2.0f;
                        hist_test.NumSamples();
                        float norm_test = hist_test.GetNumSamples() * 2.0f;

                        error[b][0] = 0;
                        int[] hist_orig_data = hist_orig.GetHistData();
                        int[] hist_test_data = hist_test.GetHistData();
                        for (int c = 0; c < hist_orig.GetNumBins(); c++) {
                            out[c][0] = hist_orig_data[c] / norm_orig;
                            out[c][1] = hist_test_data[c] / norm_test;
                            out[c][2] = Math.abs(out[c][0] - out[c][1]);
                            error[b][0] += out[c][2];
                        }
                        error_all += error[b][0];
                        error[0][1] += error[b][0];

                        // store results
                        if (error[b][0] > max) {
                            max = error[b][0];
                        }
                        resultImage.set((i * resultImage.getNumBands()) + b, multiplier * error[b][0]);
                    }

                    done.put(mask, error);

                } else {
                    double[][] error = (double[][]) done.get(mask);

                    for (int b = 0; b < resultImage.getNumBands(); b++) {
                        resultImage.set((i * resultImage.getNumBands()) + b, multiplier * error[b][0]);
                    }
                }
            }
        } else {
            Double mask = new Double(0);

            double[][] error = new double[originalImage.getNumBands()][2];

            error[0][1] = 0;

            for (int b = 0; b < originalImage.getNumBands(); b++) {

                // create the histogram
                hist_orig.Hist(originalImage, b);
                hist_test.Hist(testImage, b);

                // copy array
                // set norm_orig to count percentage of pixels in wrong bin, or
                // 0.5 to count pixels in wrong bin.
                // 1 = to get number of pixels wrong
                // #count = to get the percentage of pixels
                // * 2 = to count the number of pixels in wrong bin (i.e. in
                // bin, but should be in bin 2)

                hist_orig.NumSamples();
                float norm_orig = hist_orig.GetNumSamples() * 2.0f;
                hist_test.NumSamples();
                float norm_test = hist_test.GetNumSamples() * 2.0f;

                error[b][0] = 0;
                int[] hist_orig_data = hist_orig.GetHistData();
                int[] hist_test_data = hist_test.GetHistData();
                for (int c = 0; c < hist_orig.GetNumBins(); c++) {
                    out[c][0] = hist_orig_data[c] / norm_orig;
                    out[c][1] = hist_test_data[c] / norm_test;
                    out[c][2] = Math.abs(out[c][0] - out[c][1]);
                    error[b][0] += out[c][2];
                }
                error_all += error[b][0];
                error[0][1] += error[b][0];

                // store results
                if (error[b][0] > max) {
                    max = error[b][0];
                }

                for (int i = b; i < resultImage.getSize(); i += resultImage.getNumBands()) {
                    resultImage.set(i, multiplier * error[b][0]);
                }
            }
            done.put(mask, error);
        }

        double[][][] result = new double[done.size() + 1][resultImage.getNumBands()][3];

        result[0][0][0] = max;
        result[0][0][1] = error_all / originalImage.getNumBands();
        result[0][0][2] = done.size();

        int i = 1;
        for (Enumeration keys = done.keys(); keys.hasMoreElements(); i++) {
            Double key = (Double) keys.nextElement();
            double[][] error = (double[][]) done.get(key);

            for (int b = 0; b < resultImage.getNumBands(); b++) {
                result[i][b][0] = key.doubleValue();
                result[i][b][1] = error[b][0];
                result[i][b][2] = error[0][1];
            }
        }

        logger.debug("maximum error : " + result[0][0][0]); // max
        logger.debug("total  error  : " + result[0][0][1]); // error_all per
                                                            // band

        return result;
    }

    /**
     * Perform a chi square test with the two input images and the mask. It will
     * create a histgram for each mask value of both in1 and in2 and compare the
     * two images with each other using the standard ChiSquare methods. In1 is
     * assumed to be the expected image (original) and in2 is the actual image
     * (test). The function will return the total chi-square value, the maximum
     * chi-square for a mask/band and the number of mask values. The rest of the
     * array is filled with [mask][band][0] = mask value, [mask][band][1] =
     * chi-square value, [mask][band][2] = degrees of freedom and
     * [mask][band][3] = probability of the actual coming from the expected.
     * 
     * @param in1
     *            the expected (original) image
     * @param in2
     *            the actual (test) image
     * @return an 3-D array with all the test results.
     */
    private double[][][] chiSquare(ImageObject in1, ImageObject in2) throws ImageException {
        double max = Double.MIN_VALUE;
        double chi = 0;

        logger.debug("CHI Square test");
        double[] df = new double[1];
        double[] chsq = new double[1];
        double[] prob = new double[1];
        Hashtable done = new Hashtable();
        Histogram hist_orig = new Histogram();
        Histogram hist_test = new Histogram();
        double[] bin_orig = new double[hist_orig.GetNumBins()];
        double[] bin_test = new double[hist_orig.GetNumBins()];

        // /////////////////////////
        // initialize histogram to min and max values of the original image
        hist_orig.SetMaxDataVal(in1.getMax());
        hist_orig.SetMinDataVal(in1.getMin());
        hist_orig.SetNumBins(256);
        hist_orig.SetIs256Bins(false);

        hist_test.SetMaxDataVal(in1.getMax());
        hist_test.SetMinDataVal(in1.getMin());
        hist_test.SetNumBins(256);
        hist_test.SetIs256Bins(false);

        // go through mask image and create histogram for each mask value
        if (maskImage == null) {
            double[][] chival = new double[in1.getNumBands()][3];

            for (int b = 0; b < in1.getNumBands(); b++) {

                // create the histogram
                hist_orig.Hist(in1, b);
                hist_test.Hist(in2, b);

                // copy array
                hist_orig.NumSamples();
                // float norm_orig = hist_orig.GetNumSamples() *
                // (float)hist_orig.GetWideBins();
                hist_test.NumSamples();
                // float norm_test = hist_orig.GetNumSamples() *
                // (float)hist_orig.GetWideBins();

                int[] hist_orig_data = hist_orig.GetHistData();
                int[] hist_test_data = hist_test.GetHistData();
                for (int c = 0; c < hist_orig.GetNumBins(); c++) {
                    bin_orig[c] = hist_orig_data[c];// / norm_orig;
                    bin_test[c] = hist_test_data[c];// / norm_test;
                }

                // do chi-square test
                ChiSquare.calculate(bin_test, bin_orig, 0, df, chsq, prob);

                // store results
                chi += chsq[0];
                if (chsq[0] > max) {
                    max = chsq[0];
                }
                resultImage.set(b, multiplier * chsq[0]);

                chival[b][0] = chsq[0];
                chival[b][1] = df[0];
                chival[b][2] = prob[0];
            }

            done.put(new Double(0), chival);
        } else {

            for (int i = 0; i < maskImage.getNumRows() * maskImage.getNumCols(); i++) {
                Double mask = new Double(maskImage.getDouble(i * maskImage.getNumBands()));
                if (!done.containsKey(mask)) {

                    double[][] chival = new double[in1.getNumBands()][3];

                    for (int b = 0; b < in1.getNumBands(); b++) {

                        // create the histogram
                        hist_orig.HistMask(in1, b, maskImage, mask.doubleValue());
                        hist_test.HistMask(in2, b, maskImage, mask.doubleValue());

                        // copy array
                        hist_orig.NumSamples();
                        // float norm_orig = hist_orig.GetNumSamples() *
                        // (float)hist_orig.GetWideBins();
                        hist_test.NumSamples();
                        // float norm_test = hist_orig.GetNumSamples() *
                        // (float)hist_orig.GetWideBins();

                        int[] hist_orig_data = hist_orig.GetHistData();
                        int[] hist_test_data = hist_test.GetHistData();
                        for (int c = 0; c < hist_orig.GetNumBins(); c++) {
                            bin_orig[c] = hist_orig_data[c];// / norm_orig;
                            bin_test[c] = hist_test_data[c];// / norm_test;
                        }

                        // do chi-square test
                        ChiSquare.calculate(bin_test, bin_orig, 0, df, chsq, prob);

                        // store results
                        chi += chsq[0];
                        if (chsq[0] > max) {
                            max = chsq[0];
                        }
                        resultImage.set((i * resultImage.getNumBands()) + b, multiplier * chsq[0]);

                        chival[b][0] = chsq[0];
                        chival[b][1] = df[0];
                        chival[b][2] = prob[0];
                    }

                    done.put(mask, chival);
                } else {
                    double[][] chival = (double[][]) done.get(mask);

                    for (int b = 0; b < resultImage.getNumBands(); b++) {
                        resultImage.set((i * resultImage.getNumBands()) + b, multiplier * chival[b][0]);
                    }
                }
            }
        }

        double[][][] result = new double[done.size() + 1][resultImage.getNumBands()][4];

        result[0][0][0] = chi;
        result[0][0][1] = max;
        result[0][0][2] = done.size();

        int i = 1;
        for (Enumeration keys = done.keys(); keys.hasMoreElements(); i++) {
            Double key = (Double) keys.nextElement();
            double[][] chival = (double[][]) done.get(key);

            for (int b = 0; b < resultImage.getNumBands(); b++) {
                result[i][b][0] = key.doubleValue();
                result[i][b][1] = chival[b][0];
                result[i][b][2] = chival[b][1];
                result[i][b][3] = chival[b][2];
            }
        }

        logger.debug("maximum chi : " + max);
        logger.debug("chi square  : " + chi);

        return result;
    }

    /**
     * Some simple tests to see if the images can be compared with each other
     * and the mask fits the image.
     * 
     * @throws ImageException
     *             if the images to be tested don't have the same width, height
     *             or bands.
     */
    private void sanityTest() throws ImageException {
        if (testImage == null) {
            throw (new ImageException("No image to be tested."));
        }

        if (originalImage == null) {
            throw (new ImageException("No image to be tested against."));
        }

        if (testImage.getNumCols() != originalImage.getNumCols()) {
            throw (new ImageException("Test and original are not the same width."));
        }

        if (testImage.getNumRows() != originalImage.getNumRows()) {
            throw (new ImageException("Test and original are not the same height."));
        }

        if (testImage.getNumBands() != originalImage.getNumBands()) {
            throw (new ImageException("Test and original have not the same bands."));
        }

        if ((maskImage != null) && (maskImage.getNumCols() != originalImage.getNumCols())) {
            throw (new ImageException("Mask and original are not the same width."));
        }

        if ((maskImage != null) && (maskImage.getNumRows() != originalImage.getNumRows())) {
            throw (new ImageException("Mask and original are not the same height."));
        }
    }

    /**
     * Count the number of unique mask values in the image.
     * 
     * @return number of mask values.
     */
    private Vector getMaskCount() {
        if (maskImage == null) {
            return null;
        }

        Vector mask = new Vector();
        for (int i = 0; i < maskImage.getSize(); i += maskImage.getNumBands()) {
            Double key = new Double(maskImage.getDouble(i));
            if (!mask.contains(key)) {
                mask.add(key);
            }
        }

        return mask;
    }

    /**
     * Based on the original and test image guess which is the best type for the
     * resultimage.
     * 
     * @return a new ImageObject to be used as a resultimage.
     */
    private ImageObject createResult() throws ImageException {
        // create the result image
        if (resultType == null) {
            if (testImage.getType() == originalImage.getType()) {
                return ImageObject.createImage(testImage.getNumRows(), testImage.getNumCols(), testImage.getNumBands(), testImage.getType());
            } else {
                if ((testImage.getType() == ImageObject.TYPE_DOUBLE) || (originalImage.getType() == ImageObject.TYPE_DOUBLE)) {
                    return new ImageObjectDouble(testImage.getNumRows(), testImage.getNumCols(), testImage.getNumBands());
                } else if ((testImage.getType() == ImageObject.TYPE_FLOAT) || (originalImage.getType() == ImageObject.TYPE_FLOAT)) {
                    return new ImageObjectFloat(testImage.getNumRows(), testImage.getNumCols(), testImage.getNumBands());
                } else if ((testImage.getType() == ImageObject.TYPE_LONG) || (originalImage.getType() == ImageObject.TYPE_LONG)) {
                    return new ImageObjectLong(testImage.getNumRows(), testImage.getNumCols(), testImage.getNumBands());
                } else if ((testImage.getType() == ImageObject.TYPE_INT) || (originalImage.getType() == ImageObject.TYPE_INT)) {
                    return new ImageObjectInt(testImage.getNumRows(), testImage.getNumCols(), testImage.getNumBands());
                } else if ((testImage.getType() == ImageObject.TYPE_USHORT) || (originalImage.getType() == ImageObject.TYPE_USHORT)) {
                    return new ImageObjectShort(testImage.getNumRows(), testImage.getNumCols(), testImage.getNumBands());
                } else if ((testImage.getType() == ImageObject.TYPE_SHORT) || (originalImage.getType() == ImageObject.TYPE_SHORT)) {
                    return new ImageObjectShort(testImage.getNumRows(), testImage.getNumCols(), testImage.getNumBands());
                } else {
                    return new ImageObjectByte(testImage.getNumRows(), testImage.getNumCols(), testImage.getNumBands());
                }
            }
        } else {
            return ImageObject.createImage(testImage.getNumRows(), testImage.getNumCols(), testImage.getNumBands(), resultType);
        }
    }

    // Housedorff Distance

    double housedorff(ImageObject in1, ImageObject in2) throws Exception {
        double result = -1;
        int height = 100;
        int width = 100;
        ImageObject tmp1;
        ImageObject tmp2;
        int index = 0;
        double size;
        if (originalImage.getNumCols() / width > testImage.getNumCols() / width)
            size = originalImage.getNumCols() / width;
        else
            size = testImage.getNumCols() / width;

        if (originalImage.getNumRows() / height > testImage.getNumRows() / height)
            size *= originalImage.getNumRows() / height;
        else
            size *= testImage.getNumRows() / height;
        double h12[] = new double[(int) size];
        for (int i = 0; i < in1.getNumRows() - height; i += height) {
            for (int j = 0; j < in1.getNumCols() - width; j += width) {
                h12[index] = -1;
                originalImage = in1.crop(new SubArea(j, i, width, height));
                for (int k = 0; k < in2.getNumRows() - height; k += height) {
                    for (int l = 0; l < in2.getNumCols() - width; l += width) {
                        testImage = in2.crop(new SubArea(l, k, width, height));
                        double res[][][] = chiSquare();
                        if (res[0][0][0] < h12[index] || h12[index] < 0)
                            h12[index] = res[0][0][0];
                    }
                }
                if (result < 0 || result < h12[index])
                    result = h12[index];
                index++;
            }
        }
        return result;
    }
}
