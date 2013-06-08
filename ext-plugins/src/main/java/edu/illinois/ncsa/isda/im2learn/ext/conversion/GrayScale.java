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
package edu.illinois.ncsa.isda.im2learn.ext.conversion;

import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageException;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;

/**
 * This class takes the current image and creates a grayscale image from it. If
 * the original image is a byte it will pack the RGB into a single byte,
 * otherwise it will try and maximize the amount of information it can store.
 *
 * @author Rob Kooper
 * @version 2.0
 */
public class GrayScale {
    /**
     * This will convert the image to a grayscale image. It will use the default
     * red, green and blue values, or band 0, 1 and 2. The conversion uses the
     * following formulat:<code>Y = 0.3*R + 0.59*G + 0.11*B</code>
     *
     * @param src the image that needs to be converted
     * @return the image that is converted
     * @throws ImageException if the image could not be converted.
     */
    public ImageObject convert(ImageObject src) throws ImageException {
        int[] rgb = (int[]) src.getProperty(ImageObject.DEFAULT_RGB);
        if (rgb == null) {
            rgb = new int[]{0, 1, 2};
        }
        return convert(src, rgb[0], rgb[1], rgb[2]);
    }

    /**
     * This will convert the image to a grayscale image. It will use the given
     * red, green and blue bands to achieve this. The conversion uses the
     * following formulat:<br> Y = 0.3*R + 0.59*G + 0.11*B<br>
     *
     * @param src   the image that needs to be converted
     * @param red   the redband
     * @param green the greenband
     * @param blue  the blueband
     * @return the image that is converted
     * @throws ImageException if the image could not be converted.
     */
    public ImageObject convert(ImageObject src, int red, int green, int blue) throws ImageException {
        int b = src.getNumBands();

        if ((red > b) || (green > b) || (blue > b)) {
            throw(new ImageException("Not enough bands in image."));
        }

        ImageObject dst = ImageObject.createImage(src.getNumRows(), src.getNumCols(), 1, src.getType());
        int s = dst.getSize();
        int j;
        double v;
        // uses following formula, also used in photoshop, seems to be
        // standard for converting RGB to grayscale.
        // Y = 0.3*R + 0.59*G + 0.11*B
        for (j = 0; j < s; j++, red += b, green += b, blue += b) {
            v = 0.3 * src.getDouble(red) + 0.59 * src.getDouble(green) + 0.11 * src.getDouble(blue);
            dst.set(j, v);
        }
        return dst;
    }
}
