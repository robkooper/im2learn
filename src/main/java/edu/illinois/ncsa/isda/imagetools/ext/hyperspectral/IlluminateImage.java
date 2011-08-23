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

/**
 * This will do the inverse of Calibrate.
 * Allow the user to select a calibrate image, an illumination (white) image and
 * a black image to create a uncalibrated image. If no calibrated image is given
 * it will use the currently visible image in the imagepanel. If no black image
 * is given it will assume an image with all 0's. The output image can be
 * previewed, saved and copied back to the ncsa.im2learn.main imagepanel.
 *

 */
public class IlluminateImage {
    private ImageObject illuminationMap = null;
    private ImageObject black = null;

    public IlluminateImage() {
    }

    public void setIlluminationMap(ImageObject illuminationMap) {
        this.illuminationMap = illuminationMap;
    }

    public ImageObject getIlluminationMap() {
        return illuminationMap;
    }

    public void setBlackImage(ImageObject black) {
        this.black = black;
    }

    public ImageObject getBlackImage() {
        return black;
    }

    public ImageObject illuminate(ImageObject reflectance) throws ImageException {
        return illuminate(reflectance, null);
    }

    public ImageObject illuminate(ImageObject reflectance, String type) throws ImageException {
        if (!reflectance.isSameRowColBand(illuminationMap)) {
            throw(new ImageException("reflectance and illuminationmap not same size."));
        }
        if ((black != null) && !reflectance.isSameRowColBand(black)) {
            throw(new ImageException("reflectance and black not same size."));
        }

        ImageObject result = null;
        if (type == null) {
            try {
                result = (ImageObject) reflectance.clone();
            } catch (CloneNotSupportedException exc) {
                throw(new ImageException(exc));
            }
        } else {
            result = ImageObject.createImage(reflectance.getNumRows(), reflectance.getNumCols(), reflectance.getNumBands(), type);
            result.setProperties(reflectance.cloneProperties());
        }

        int i, size;
        double bl, wh, ca;
        size = reflectance.getSize();
        if (black != null) {
            for (i = 0; i < size; i++) {
                bl = black.getDouble(i);
                wh = illuminationMap.getDouble(i);
                ca = reflectance.getDouble(i);
                result.set(i, (wh - bl) * ca + bl);
            }
        } else {
            for (i = 0; i < size; i++) {
                wh = illuminationMap.getDouble(i);
                ca = reflectance.getDouble(i);
                result.set(i, wh * ca);
            }
        }
        return result;
    }
}
