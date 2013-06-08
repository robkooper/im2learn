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
package edu.illinois.ncsa.isda.im2learn.ext.hyperspectral;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageException;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * When multiple images are taken with a hyperspectral camera, this class will
 * take the images and try to fuse them into a single image.
 */
public class HyperSpectralFusion {
    private ImageObject image1;
    private double[] valWL1;
    private int[] orderWL1;
    private ImageObject image2;
    private double[] valWL2;
    private int[] orderWL2;
    private double step = 15;
    private boolean absoluteMerge = false;

    private static Log logger = LogFactory.getLog(HyperSpectralFusion.class);

    public static String[] methods = new String[]{"Sinple", "Average", "Step"};

    public double getStep() {
        return step;
    }

    public void setStep(double step) {
        this.step = step;
    }

    public boolean isAbsoluteMerge() {
        return absoluteMerge;
    }

    public void setAbsoluteMerge(boolean absoluteMerge) {
        this.absoluteMerge = absoluteMerge;
    }

    public void setImage1(ImageObject image) throws ImageException {
        if (image == null) {
            throw(new ImageException("Image is null"));
        }
        Object wl = image.getProperty(ImageObject.WAVELENGTH);
        if ((wl == null) || !wl.getClass().isArray()) {
            throw(new ImageException("Image does not have wavelength array property"));
        }
        Object[] wlarr = (Object[]) wl;
        if (wlarr.length != image.getNumBands()) {
            throw(new ImageException("Image does not have enough wavelengths"));
        }

        this.image1 = image;
        this.valWL1 = new double[image.getNumBands()];
        this.orderWL1 = sortWaveLength(wlarr, valWL1);
    }

    public void setImage2(ImageObject image) throws ImageException {
        if (image == null) {
            throw(new ImageException("Image is null"));
        }
        Object wl = image.getProperty(ImageObject.WAVELENGTH);
        if ((wl == null) || !wl.getClass().isArray()) {
            throw(new ImageException("Image does not have wavelength array property"));
        }
        Object[] wlarr = (Object[]) wl;
        if (wlarr.length != image.getNumBands()) {
            throw(new ImageException("Image does not have enough wavelengths"));
        }

        this.image2 = image;
        this.valWL2 = new double[image.getNumBands()];
        this.orderWL2 = sortWaveLength(wlarr, valWL2);
    }

    public ImageObject fuse(String method) throws ImageException {
        if (method.equalsIgnoreCase("Sinple")) {
            return fuseSimple();
        } else if (method.equalsIgnoreCase("Average")) {
            return fuseAverageSimple();
        } else if (method.equalsIgnoreCase("Step")) {
            return fuseStep();
        } else {
            throw(new ImageException("No such fusion method."));
        }
    }

    /**
     * Fuse the images by wavelength, taking bands from image1 if wavelengths
     * overlap. The wavelengths will not be normalized meaning that if image1
     * has a 10nm step per wavelength and image2 has 15nm per wavelength, the
     * resulting image will have wavelengths of 10, 15, 20, 30, 40, 45 etc. The
     * image returned will have the wavelength property set.
     *
     * @return fused image
     */
    public ImageObject fuseSimple() throws ImageException {
        int bands1 = image1.getNumBands();
        int bands2 = image2.getNumBands();
        int i = 0;
        int j = 0;
        int cnt = 0;

        // check images
        if ((image1 == null) || (image2 == null)) {
            throw(new ImageException("Did not specify images."));
        }
        if (!image1.isSameRowCol(image2)) {
            throw(new ImageException("Images not same size."));
        }

        // count number of elements
        cnt = 0;
        i = 0;
        j = 0;
        while ((i < bands1) || (j < bands2)) {
            if (i >= bands1) {
                j++;
            } else if (j >= bands2) {
                i++;
            } else if (valWL1[i] == valWL2[j]) {
                i++;
                j++;
            } else if (valWL1[i] < valWL2[j]) {
                i++;
            } else {
                j++;
            }
            cnt++;
        }

        // now do the real merge
        ImageObject[] images = new ImageObject[cnt];
        String[] wavelengths = new String[cnt];
        cnt = 0;
        i = 0;
        j = 0;
        while ((i < bands1) || (j < bands2)) {
            if (i >= bands1) {
                wavelengths[cnt] = "" + valWL2[j];
                images[cnt] = image2.extractBand(j);
                j++;
            } else if (j >= bands2) {
                wavelengths[cnt] = "" + valWL1[i];
                images[cnt] = image1.extractBand(i);
                i++;
            } else if (valWL1[i] == valWL2[j]) {
                wavelengths[cnt] = "" + valWL1[i];
                images[cnt] = image1.extractBand(i);
                i++;
                j++;
            } else if (valWL1[i] < valWL2[j]) {
                wavelengths[cnt] = "" + valWL1[i];
                images[cnt] = image1.extractBand(i);
                i++;
            } else {
                wavelengths[cnt] = "" + valWL2[j];
                images[cnt] = image2.extractBand(j);
                j++;
            }
            cnt++;
        }

        // merge
        ImageObject result = ImageObject.add(images);
        result.setProperty(ImageObject.WAVELENGTH, wavelengths);

        return result;
    }

    /**
     * Fuse the images by wavelength, taking bands from image1 if wavelengths
     * overlap. The wavelengths will not be normalized meaning that if image1
     * has a 10nm step per wavelength and image2 has 15nm per wavelength, the
     * resulting image will have wavelengths of 10, 15, 20, 30, 40, 45 etc. The
     * image returned will have the wavelength property set. Based on the
     * absoluteMerge value either the values are simply averaged, or the
     * relative values are averaged.
     *
     * @return fused image
     */
    public ImageObject fuseAverageSimple() throws ImageException {
        int bands1 = image1.getNumBands();
        int bands2 = image2.getNumBands();
        int i = 0;
        int j = 0;
        int cnt = 0;

        // check images
        if ((image1 == null) || (image2 == null)) {
            throw(new ImageException("Did not specify images."));
        }
        if (!image1.isSameRowCol(image2)) {
            throw(new ImageException("Images not same size."));
        }

        // count number of elements
        cnt = 0;
        i = 0;
        j = 0;
        while ((i < bands1) || (j < bands2)) {
            if (i >= bands1) {
                j++;
            } else if (j >= bands2) {
                i++;
            } else if (valWL1[orderWL1[i]] == valWL2[orderWL2[j]]) {
                i++;
                j++;
            } else if (valWL1[orderWL1[i]] < valWL2[orderWL2[j]]) {
                i++;
            } else {
                j++;
            }
            cnt++;
        }

        // now do the real merge
        ImageObject[] images = new ImageObject[cnt];
        String[] wavelengths = new String[cnt];
        cnt = 0;
        i = 0;
        j = 0;
        while ((i < bands1) || (j < bands2)) {
            if (i >= bands1) {
                wavelengths[cnt] = "" + valWL2[j];
                images[cnt] = image2.extractBand(j);
                j++;
            } else if (j >= bands2) {
                wavelengths[cnt] = "" + valWL1[i];
                images[cnt] = image1.extractBand(i);
                i++;
            } else if (valWL1[i] == valWL2[j]) {
                wavelengths[cnt] = "" + valWL1[i];
                images[cnt] = mergeImageObject(image1.extractBand(i), image2.extractBand(j));
                i++;
                j++;
            } else if (valWL1[i] < valWL2[j]) {
                wavelengths[cnt] = "" + valWL1[i];
                images[cnt] = image1.extractBand(i);
                i++;
            } else {
                wavelengths[cnt] = "" + valWL2[j];
                images[cnt] = image2.extractBand(j);
                j++;
            }
            cnt++;
        }

        // merge
        ImageObject result = ImageObject.add(images);
        result.setProperty(ImageObject.WAVELENGTH, wavelengths);

        return result;
    }

    public ImageObject fuseStep() throws ImageException {
        return fuseStep(step, -Double.MAX_VALUE, Double.MAX_VALUE);
    }

    public ImageObject fuseStep(double step, double start, double end) throws ImageException {
        // check images
        if ((image1 == null) || (image2 == null)) {
            throw(new ImageException("Did not specify images."));
        }
        if (!image1.isSameRowCol(image2)) {
            throw(new ImageException("Images not same size."));
        }
        if (step <= 0) {
            throw(new ImageException("Step has to be larger than 0."));
        }

        // useful variables
        int len1 = orderWL1.length;
        int len2 = orderWL2.length;

        // find highest and lowest WL in image
        double minwl = valWL1[orderWL1[0]];
        double maxwl = valWL1[orderWL1[len1 - 1]];
        if (minwl > valWL2[orderWL2[0]]) {
            minwl = valWL2[orderWL2[0]];
        }
        if (maxwl < valWL2[orderWL2[len2 - 1]]) {
            maxwl = valWL2[orderWL2[len2 - 1]];
        }

        // check the passed in values
        if (start == -Double.MAX_VALUE) {
            start = minwl;
        } else if (start < minwl) {
            logger.info("Starting wavelength larger than min wavelength found, setting to min wavelength.");
            start = minwl;
        }
        if (end == Double.MAX_VALUE) {
            end = maxwl;
        } else if (end > maxwl) {
            logger.info("Ending wavelength larger than max wavelength found, setting to max wavelength.");
            end = maxwl;
        }
        if (start > end) {
            throw(new ImageException("Starting wavelength larger than ending wavelength."));
        }

        // loop through the requested wavelengths
        ArrayList imglist = new ArrayList();
        ArrayList imgWL = new ArrayList();
        int band11, band12, band21, band22, b;
        for (double wl = start; wl < end; wl += step) {
            // find band with wavelength closest to requested wl
            band11 = -1;
            band12 = -1;
            for (b = 0; b < len1; b++) {
                if ((valWL1[b] <= wl) && ((band11 == -1) || (valWL1[b] > valWL1[band11]))) {
                    band11 = b;
                }
                if ((valWL1[b] >= wl) && ((band12 == -1) || (valWL1[b] < valWL1[band12]))) {
                    band12 = b;
                }
            }
            band21 = -1;
            band22 = -1;
            for (b = 0; b < len2; b++) {
                if ((valWL2[b] <= wl) && ((band21 == -1) || (valWL2[b] > valWL2[band21]))) {
                    band21 = b;
                }
                if ((valWL2[b] >= wl) && ((band22 == -1) || (valWL2[b] < valWL2[band22]))) {
                    band22 = b;
                }
            }

            // if both are exact match then merge them
            if ((band11 != -1) && (band12 != -1) && (band21 != -1) && (band22 != -1) &&
                (valWL1[band11] == valWL1[band12]) && (valWL2[band21] == valWL2[band22]) && (valWL1[band11] == valWL2[band21])) {
                imglist.add(mergeImageObject(image1.extractBand(band11), image2.extractBand(band21)));

                // if either one is exact match use that
            } else if ((band11 != -1) && (band12 != -1) && (valWL1[band11] == valWL1[band12])) {
                imglist.add(image1.extractBand(band11));

            } else if ((band21 != -1) && (band22 != -1) && (valWL2[band21] == valWL2[band22])) {
                imglist.add(image2.extractBand(band21));

                // find the closes of the bands and linearly interpolate
            } else {
                ImageObject img1, img2;
                double wl1, wl2;
                if (band21 == -1) {
                    img1 = image1.extractBand(band11);
                    wl1 = valWL1[band11];
                } else if (band11 == -1) {
                    img1 = image2.extractBand(band21);
                    wl1 = valWL2[band21];
                } else if (valWL2[band21] == valWL1[band11]) {
                    img1 = mergeImageObject(image1.extractBand(band11), image2.extractBand(band21));
                    wl1 = valWL2[band21];
                } else if (valWL2[band21] > valWL1[band11]) {
                    img1 = image2.extractBand(band21);
                    wl1 = valWL2[band21];
                } else {
                    img1 = image1.extractBand(band11);
                    wl1 = valWL1[band11];
                }

                if (band22 == -1) {
                    img2 = image1.extractBand(band12);
                    wl2 = valWL1[band12];
                } else if (band12 == -1) {
                    img2 = image2.extractBand(band22);
                    wl2 = valWL2[band22];
                } else if (valWL2[band22] == valWL1[band12]) {
                    img2 = mergeImageObject(image1.extractBand(band12), image2.extractBand(band22));
                    wl2 = valWL2[band22];
                } else if (valWL2[band22] > valWL1[band12]) {
                    img2 = image1.extractBand(band12);
                    wl2 = valWL1[band12];
                } else {
                    img2 = image2.extractBand(band22);
                    wl2 = valWL2[band22];
                }

                imglist.add(mergeImageObject(img1, img2, wl, wl1, wl2));
            }
            imgWL.add("" + wl);
        }

        // combine images into single image
        ImageObject[] images = new ImageObject[imglist.size()];
        images = (ImageObject[]) imglist.toArray(images);
        ImageObject result = ImageObject.add(images);

        // set the wavelength property
        String[] wavelengths = new String[imgWL.size()];
        wavelengths = (String[]) imgWL.toArray(wavelengths);
        result.setProperty(ImageObject.WAVELENGTH, wavelengths);

        return result;
    }

    private ImageObject mergeImageObject(ImageObject img1, ImageObject img2) {
        return mergeImageObject(img1, img2, 0, 0, 0);
    }

    private ImageObject mergeImageObject(ImageObject img1, ImageObject img2, double wl, double wl1, double wl2) {
        int size = img1.getSize();
        double fac1, fac2;

        // first find out how far the images are from the desired wl
        if ((wl == wl1) && (wl == wl2)) {
            fac1 = 0.5;
            fac2 = 0.5;
        } else if ((wl == wl1) && (wl != wl2)) {
            fac1 = 1;
            fac2 = 0;
        } else if ((wl != wl1) && (wl == wl2)) {
            fac1 = 0;
            fac2 = 1;
        } else {
            fac1 = (wl2 - wl) / (wl2 - wl1);
            fac2 = (wl - wl1) / (wl2 - wl1);
        }

        if (!absoluteMerge) {
            double min1 = img1.getMin();
            double min2 = img2.getMin();
            fac1 = fac1 / (img1.getMax() - min1);
            fac2 = fac2 / (img2.getMax() - min2);
            double m1 = (min1 + min2) / 2;
            double m2 = ((img1.getMax() + img2.getMax()) / 2) - m1;
            double d1, d2, dn;
            for (int k = 0; k < size; k++) {
                d1 = fac1 * (img1.getDouble(k) - min1);
                d2 = fac2 * (img2.getDouble(k) - min2);
                dn = ((d1 + d2) / 2.0) * m2 + m1;
                img1.set(k, dn);
            }
        } else {
            for (int k = 0; k < size; k++) {
                img1.set(k, (img1.getDouble(k) * fac1) + (img2.getDouble(k) * fac2));
            }
        }

        return img1;
    }

    private int[] sortWaveLength(Object[] imagewl, double[] wlval) throws ImageException {
        int i, j, k;
        int bands = imagewl.length;
        double[] wlsort = new double[bands];
        int[] order = new int[bands];

        // initialize the arrays
        Arrays.fill(order, 0);
        Arrays.fill(wlsort, 0);

        // get all the values
        k = bands - 1;
        for (i = 0; i < bands; i++, k--) {
            // deceipher the wavelength
            try {
                wlval[i] = Double.parseDouble(imagewl[i].toString());
            } catch (NumberFormatException exc) {
                throw(new ImageException(exc));
            }

            // find location in array
            j = 0;
            while ((j < i) && (wlsort[j] < wlval[i])) {
                j++;
            }

            // shift arrays and insert values
            System.arraycopy(wlsort, j, wlsort, j + 1, k);
            wlsort[j] = wlval[i];
            System.arraycopy(order, j, order, j + 1, k);
            order[j] = i;
        }

        return order;
    }
}
