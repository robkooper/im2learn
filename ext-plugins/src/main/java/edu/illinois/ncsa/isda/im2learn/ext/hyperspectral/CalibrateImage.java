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

import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageException;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;

/**
*
 * @author Rob Kooper
 * @author Tyler J. Alumbaugh
 *
 */

public class CalibrateImage {
    private ImageObject imgBlack = null;
    private ImageObject imgWhite = null;
    private boolean clipEnabled;

    /**
     * Default constructor, does not initialize any black or white images.
     */
    public CalibrateImage() {
    }

    /**
     * Sets the black image used for the calibration process.
     *
     * @param black the black reference image
     */
    public void setBlackImage(ImageObject black) {
        imgBlack = black;
    }

    /**
     * Returns the black reference image.
     *
     * @return the black reference image
     */
    public ImageObject getBlackImage() {
        return imgBlack;
    }

    /**
     * Sets the white image used for the calibration process.
     *
     * @param white the white reference image
     */
    public void setWhiteImage(ImageObject white) {
        imgWhite = white;
    }

    /**
     * Returns the white reference image.
     *
     * @return the white reference image
     */
    public ImageObject getWhiteImage() {
        return imgWhite;
    }

    /**
     * Enable clipping of the resulting values. This will make sure that the
     * return image will only have values between 0, black, and max, white.
     *
     * @param enable turn clipping on
     */
    public void setClipEnabled(boolean enable) {
        clipEnabled = enable;
    }

    /**
     * Return true if calibrate clips the values. If the values are clipped they
     * will always be between 0, black, and max, white.
     *
     * @return true if final values are clipped.
     */
    public boolean isClipEnabled() {
        return clipEnabled;
    }

    /**
     * This will do the callibration returning an image with double values
     * ranging from 0 to 1, where 0 is black and 1 is full intensity.
     *
     * @param imgOriginal the uncalibrated image
     * @return the calibrated image
     */
    public ImageObject calibrate(ImageObject imgOriginal) throws ImageException {
        return calibrate(imgOriginal, 1.0, "DOUBLE");
    }

    /**
     * @param imgOriginal
     * @param mult
     * @param type
     * @return
     */
    public ImageObject calibrate(ImageObject imgOriginal, double mult, String type)
            throws ImageException {
        ImageObject imgResult = null;

        if (imgWhite == null) {
            throw(new ImageException("White reference image can not be null."));
        }

        if (imgBlack != null) {
            if (!imgOriginal.isSame(imgBlack)) {
                throw(new ImageException("Original and black are not the same size/type."));
            }
        }

        if (!imgOriginal.isSame(imgWhite)) {
            throw(new ImageException("Original and white are not the same size/type."));
        }

        if (type == null) {
            try {
                imgResult = (ImageObject) imgOriginal.clone();
            } catch (CloneNotSupportedException exc) {
                throw(new ImageException(exc));
            }
        } else {
            imgResult = ImageObject.createImage(imgOriginal.getNumRows(), imgOriginal.getNumCols(), imgOriginal.getNumBands(), type);
            imgResult.setProperties(imgOriginal.cloneProperties());
        }

        double num, div, tmp;
        int i, size, bands;
        int[][] clipped;

        size = imgOriginal.getSize();
        bands = imgOriginal.getNumBands();
        clipped = new int[bands][2];
        for (i = 0; i < size; i++) {
            // get data from white
            div = imgWhite.getDouble(i);

            // get data from original
            num = imgOriginal.getDouble(i);

            // subtract black
            if (imgBlack != null) {
                tmp = imgBlack.getDouble(i);
                div -= tmp;
                num -= tmp;
            }

            // perform calculation
            if (clipEnabled) {
                if ((num < 0) || (div < 0)) {
                    num = 0;
                    clipped[i % bands][0]++;
                } else if (num > div) {
                    num = mult;
                    clipped[i % bands][1]++;
                } else {
                    num = mult * (num / div);
                }
            } else {
                if (div != 0) {
                    num = mult * (num / div);
                } else {
                    num = 0;
                }
            }
            imgResult.set(i, num);
        }

        if (clipEnabled) {
            imgResult.setProperty("Clipped (B/W)", clipped);
        }

        return imgResult;
    }
}
