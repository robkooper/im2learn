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
package edu.illinois.ncsa.isda.imagetools.ext.hyperspectral;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageException;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObjectDouble;
import edu.illinois.ncsa.isda.imagetools.ext.misc.PlotComponent;
import Jama.Matrix;

/**
 * Creates a matrix that can convert an image from one multiband representation
 * to another. This is often used to convert a hyperspectral image to a RGB
 * image. To use this class, call one of the static methods which will return an
 * instance. This instance can be used to conver the image.
 *
 * The corresponding dialog shows a dialog allowing the user to select which method to use for the
 * conversion, and what image to convert. In the case of Bruton the dialog will
 * ask what wavelengths to use, and if the wavelengths are stored with the
 * image. In the case of the estimation, the HyperSpectral image and the
 * corresponding RGB image can be selected. The user can also allign the two
 * images and select the number of samples to use to create the conversion
 * matrix.
 * <p/>
 * Next the user can compute the conversion matrix based on the method selected
 * and convert the image, apply the changes to the ncsa.im2learn.main frame, and
 * close the dialog.
 *
 *
 * @author Rob Kooper
 * @version 2.0
 */
public class ConvertRGB {
    private double[][] matrix;
    private int bandsIn;
    private int bandsOut;
    private boolean normalized = false;

    static final int ROWS = 40;
    static final int COLS = 200;

    /**
     * Protected constructor, used by the static methods.
     *
     * @param matrix the conversion matrix.
     */
    protected ConvertRGB(double[][] matrix) {
        this.matrix = (double[][]) matrix.clone();
        this.bandsIn = matrix.length;
        this.bandsOut = matrix[0].length;
    }

    /**
     * Based on the matrix create an ImageObject that will show the spectrum
     * that is used. This method will go through the matrix, creating an image
     * where each column is a single row of the matrix depicting the color of
     * that row.
     *
     * @return the spectrum of the matrix
     */
    public ImageObject getSpectrum() {
        int r, c, b, i;
        double wl;
        ImageObject spectrum = new ImageObjectDouble(ROWS, COLS, bandsOut);

        double stepwl = (double) bandsIn / COLS;
        for (r = 0, i = 0; r < ROWS; r++) {
            for (wl = 0, c = 0; c < COLS; c++, wl += stepwl) {
                for (b = 0; b < bandsOut; b++) {
                    spectrum.set(i++, matrix[(int) wl][b]);
                }
            }
        }

        return spectrum;
    }

    /**
     * Plot the spectrum, this will create a plot that shows the spectrum. This
     * will plot how much each sample contributes to a band in the converted
     * image.
     *
     * @param pc the plotcomoponent to be reused, if null is passed in a new
     *           plotcomponent is created, if one is passed in it will be
     *           reset.
     * @return a plotcomponent depicting the spectrum.
     */
    public PlotComponent getPlot(PlotComponent pc) {
        if (pc == null) {
            pc = new PlotComponent();
        } else {
            pc.reset();
        }

        for (int i = 0; i < bandsOut; i++) {
            int id = pc.addSeries("Band " + i);
            for (int j = 0; j < bandsIn; j++) {
                pc.setValue(id, j, matrix[j][i]);
            }
        }

        return pc;
    }

    /**
     * Returns the matrix used for the conversion.
     *
     * @return matrix used to do the conversion
     */
    public double[][] getMatrix() {
        return (double[][]) matrix.clone();
    }

    /**
     * Normalizes the matrix, resulting in a matrix where each row will add up
     * to 1.
     */
    public void normalize() {
        int b, i;
        double[] factor = new double[bandsOut];

        if (normalized) {
            return;
        }

        for (b = 0; b < bandsIn; b++) {
            for (i = 0; i < bandsOut; i++) {
                factor[i] += matrix[b][i];
            }
        }

        for (b = 0; b < bandsIn; b++) {
            for (i = 0; i < bandsOut; i++) {
                matrix[b][i] /= factor[i];
            }
        }

        normalized = true;
    }

    /**
     * Convert the image. The input image will need to have the same number of
     * band as was used to create this conversion. The output image will have
     * the number of bands specified for the output image.
     *
     * @param imgobj the imageobject to be converted.
     * @return the converted image
     * @throws ImageException           if an error occured creating resulting
     *                                  image
     * @throws IllegalArgumentException if the input image does not have the
     *                                  right number of bands.
     */
    public ImageObject convert(ImageObject imgobj) throws ImageException, IllegalArgumentException {
        if (bandsIn != imgobj.getNumBands()) {
            throw(new IllegalArgumentException("Number of bands in input image is incorrect."));
        }

        int w = imgobj.getNumCols();
        int h = imgobj.getNumRows();
        int r, c, b, i, j, k;
        double val = 0;
        ImageObject result = null;

        if (normalized) {
            result = ImageObject.createImage(h, w, bandsOut, imgobj.getType());
        } else {
            result = new ImageObjectDouble(h, w, bandsOut);
        }

        for (r = 0, i = 0; r < h; r++) {
            for (c = 0; c < w; c++) {
                for (b = 0; b < bandsOut; b++, i++) {
                    val = 0;
                    k = (r * w + c) * bandsIn;
                    for (j = 0; j < bandsIn; j++, k++) {
                        val += matrix[j][b] * imgobj.getDouble(k);
                    }
                    result.set(i, val);
                }
            }
        }
        return result;
    }

    // ------------------------------------------------------------------------
    // ESTIMATION
    // ------------------------------------------------------------------------
    /**
     * Create a conversion method that will use the input images to find the
     * best matrix. Based on the samples parameter this routine will take
     * samples from the input image and the output image and create a set of
     * equations that when solved will result in the conversion matrix.
     *
     * @param samples the number of samples to use to create the matrix
     * @param in      the input image (hyperspectral)
     * @param out     the output image (RGB)
     * @return an instance of ConvertRGB that can convert other images with the
     *         same number of bands as the input image.
     */
    static public ConvertRGB getEstimated(int samples, ImageObject in, ImageObject out) {
        // pick points to use for sampling
        // Solve HS x A = RGB
        int HSband = in.getNumBands();
        int RGBband = out.getNumBands();

        Matrix[] HS = new Matrix[RGBband];
        Matrix[] RGB = new Matrix[RGBband];
        Matrix[] A = new Matrix[RGBband];
        Matrix HS2RGB = new Matrix(HSband, RGBband);

        for (int i = 0; i < RGBband; i++) {
            HS[i] = new Matrix(samples, HSband);
            RGB[i] = new Matrix(samples, 1);
            A[i] = new Matrix(samples, 1);
        }

        int w = (out.getNumCols() < in.getNumCols()) ? out.getNumCols() : in.getNumCols();
        int h = (out.getNumRows() < in.getNumRows()) ? out.getNumRows() : in.getNumRows();

        int x, y;
        boolean valid;
        for (int i = 0; i < samples; i++) {
            do {
                x = (int) (Math.random() * w);
                y = (int) (Math.random() * h);
                valid = false;
                for (int j = 0; j < RGBband; j++) {
                    if (out.getDouble(y, x, j) != 0) {
                        valid = true;
                    }
                }
            } while (!valid);

            for (int j = 0; j < RGBband; j++) {
                for (int k = 0; k < HSband; k++) {
                    HS[j].set(i, HSband - k - 1, in.getDouble(y, x, k));
                }
                RGB[j].set(i, 0, out.getDouble(y, x, j));
            }
        }

        for (int i = 0; i < RGBband; i++) {
            A[i] = HS[i].solve(RGB[i]);
            for (int k = 0; k < HSband; k++) {
                HS2RGB.set(HSband - k - 1, i, A[i].get(k, 0));
            }
        }

        return new ConvertRGB(HS2RGB.getArrayCopy());
    }

    // ------------------------------------------------------------------------
    // BRUTON
    // ------------------------------------------------------------------------
    /**
     * Create an instance of the conversion class that uses the curves as
     * described by Dan Bruton (http://www.physics.sfasu.edu/astro/color.html).
     * This work calculates the RGB factors for each wavelength. This method
     * will step through the wavelengths from start to end calculating the
     * factors to be used later.
     *
     * @param start    the wavelength of the first band in the image
     * @param end      the wavelength of the last band in the image.
     * @param numbands the number of bands in the image
     * @return an instance of ConvertRGB that can convert other images with the
     *         same number of bands as specfied with numbands.
     */
    static public ConvertRGB getBruton(double start, double end, int numbands) {
        int i;
        double wl, stepwl;
        double[][] rgb = new double[numbands][3];

        stepwl = (end - start) / numbands;
        for (i = 0, wl = start; i < numbands; i++, wl += stepwl) {
            rgb[i] = getRGBBruton(wl, rgb[i]);
        }

        return new ConvertRGB(rgb);
    }

    /**
     * Create an instance of the conversion class that uses the curves as
     * described by Dan Bruton (http://www.physics.sfasu.edu/astro/color.html).
     * This work calculates the RGB factors for each wavelength. This method
     * will use the wavelengths specified in the image to create the factors to
     * be used when converting an image to RGB.
     *
     * @param wavelength the wavelengths in the image.
     * @return an instance of ConvertRGB that can convert other images with the
     *         same number of bands as the number of wavelengths.
     */
    static public ConvertRGB getBruton(Object[] wavelength) throws ImageException {
        int numbands = wavelength.length;
        int i;
        double wl;
        double[][] rgb = new double[numbands][3];

        for (i = 0; i < numbands; i++) {
            try {
                wl = Double.parseDouble(wavelength[i].toString());
            } catch (NumberFormatException exc) {
                throw(new ImageException(exc));
            }
            rgb[i] = getRGBBruton(wl, rgb[i]);
        }

        return new ConvertRGB(rgb);
    }

    /**
     * Convert the given imageobject to a RGB image. The image that needs to be
     * converted needs to contain the wavelength property. The returned image
     * will be a RGB image.
     *
     * @param imgobj the image to be converted.
     * @return a RGB image
     * @throws ImageException if an error occured converting the image.
     */
    static public ImageObject converBruton(ImageObject imgobj) throws ImageException {
        if ((imgobj == null) || (imgobj.getProperty(ImageObject.WAVELENGTH) == null)) {
            throw(new ImageException("Invalid image passed to be converted."));
        }

        Object[] wl = (Object[]) imgobj.getProperty(ImageObject.WAVELENGTH);
        ConvertRGB crgb = getBruton(wl);
        crgb.normalize();
        return crgb.convert(imgobj);
    }

    /**
     * Take the given wavelength and convert it to a RGB value that is between 0
     * and 1.
     *
     * @param wl to be calculated
     * @return RGB values.
     */
    static private double[] getRGBBruton(double wl, double[] rgb) {
        double factor = 1.0;

        if ((rgb == null) || (rgb.length != 3)) {
            rgb = new double[3];
        }

        // convert wavelength to RGB value
        if ((wl >= 380.0) && (wl <= 440.0)) {
            rgb[0] = -(wl - 440.0) / (440.0 - 380.0);
            rgb[1] = 0.0;
            rgb[2] = 1.0;
        } else if ((wl > 440.0) && (wl <= 490.0)) {
            rgb[0] = 0.0;
            rgb[1] = (wl - 440.0) / (490.0 - 440.0);
            rgb[2] = 1.0;
        } else if ((wl > 490.0) && (wl <= 510.0)) {
            rgb[0] = 0.0;
            rgb[1] = 1.0;
            rgb[2] = -(wl - 510.0) / (510.0 - 490.0);
        } else if ((wl > 510.0) && (wl <= 580.0)) {
            rgb[0] = (wl - 510.0) / (580.0 - 510.0);
            rgb[1] = 1.0;
            rgb[2] = 0.0;
        } else if ((wl > 580.0) && (wl <= 645.0)) {
            rgb[0] = 1.0;
            rgb[1] = -(wl - 645.0) / (645 - 580.0);
            rgb[2] = 0.0;
        } else if ((wl > 645.0) && (wl <= 780.0)) {
            rgb[0] = 1.0;
            rgb[1] = 0.0;
            rgb[2] = 0.0;
        } else {
            rgb[0] = 0.0;
            rgb[1] = 0.0;
            rgb[2] = 0.0;
        }

        // let the intensity fall off near the vision limits
        if (wl > 700) {
            factor = 0.3 + 0.7 * (780.0 - wl) / (780.0 - 700.0);
        } else if (wl < 420) {
            factor = 0.3 + 0.7 * (wl - 380.0) / (420.0 - 380.0);
        }

        rgb[0] *= factor;
        rgb[1] *= factor;
        rgb[2] *= factor;

        return rgb;
    }
}
