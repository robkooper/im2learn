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
package edu.illinois.ncsa.isda.im2learn.core.datatype;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ImageObjectDouble extends ImageObjectInCore implements
        Externalizable {
    private static final long serialVersionUID = 1L;

    private static Log log = LogFactory.getLog(ImageObjectDouble.class);

    private double[] data;

    /**
     * Empty constructor needed for externalization. This constructor should not be
     * used by any code except for the externalization functions.
     */
    public ImageObjectDouble() {
        this(0, 0, 0, false);
    }

    public ImageObjectDouble(int rows, int cols, int bands) {
        this(rows, cols, bands, false);
    }

    public ImageObjectDouble(int rows, int cols, int bands, boolean headeronly) {
        super(rows, cols, bands, headeronly);
        this.type = TYPE_DOUBLE;
    }

    public void createArray() {
        data = new double[size];
        minmaxinvalid = true;
        headerOnly = false;
        computeOffsets();
    }

    public void deleteArray() {
        data = null;
        deleteOffsets();
        headerOnly = true;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        System.arraycopy(data, 0, this.data, 0, size);
        minmaxinvalid = true;
    }

    public void setData(double dataval) {
        Arrays.fill(data, dataval);
        minmaxinvalid = true;
    }

    static public double getTypeMinimum() {
        return -Double.MAX_VALUE;
    }

    static public double getTypeMaximum() {
        return Double.MAX_VALUE;
    }

    public double getTypeMin() {
        // Double.MIN_VALUE is the smallest positive nonzero value.
        return -Double.MAX_VALUE;
    }

    public double getTypeMax() {
        return Double.MAX_VALUE;
    }

    public byte getByte(int i) {
        return (byte) data[i];
    }

    public byte getByte(int r, int c, int b) {
        int i = rowOffset[r] + colOffset[c] + b;
        return (byte) data[i];
    }

    public void setByte(int i, byte v) {
        data[i] = v;
        minmaxinvalid = true;
    }

    public void setByte(int r, int c, int b, byte v) {
        int i = rowOffset[r] + colOffset[c] + b;
        data[i] = v;
        minmaxinvalid = true;
    }

    public void set(int i, byte v) {
        data[i] = v;
        minmaxinvalid = true;
    }

    public void set(int r, int c, int b, byte v) {
        int i = rowOffset[r] + colOffset[c] + b;
        data[i] = v;
        minmaxinvalid = true;
    }

    public short getShort(int i) {
        return (short) data[i];
    }

    public short getShort(int r, int c, int b) {
        int i = rowOffset[r] + colOffset[c] + b;
        return (short) data[i];
    }

    public void setShort(int i, short v) {
        data[i] = v;
        minmaxinvalid = true;
    }

    public void setShort(int r, int c, int b, short v) {
        int i = rowOffset[r] + colOffset[c] + b;
        data[i] = v;
        minmaxinvalid = true;
    }

    public void set(int i, short v) {
        data[i] = v;
        minmaxinvalid = true;
    }

    public void set(int r, int c, int b, short v) {
        int i = rowOffset[r] + colOffset[c] + b;
        data[i] = v;
        minmaxinvalid = true;
    }

    public int getInt(int i) {
        return (int) data[i];
    }

    public int getInt(int r, int c, int b) {
        int i = rowOffset[r] + colOffset[c] + b;
        return (int) data[i];
    }

    public void setInt(int i, int v) {
        data[i] = v;
        minmaxinvalid = true;
    }

    public void setInt(int r, int c, int b, int v) {
        int i = rowOffset[r] + colOffset[c] + b;
        data[i] = v;
        minmaxinvalid = true;
    }

    public void set(int i, int v) {
        data[i] = v;
        minmaxinvalid = true;
    }

    public void set(int r, int c, int b, int v) {
        int i = rowOffset[r] + colOffset[c] + b;
        data[i] = v;
        minmaxinvalid = true;
    }

    public long getLong(int i) {
        return (long) data[i];
    }

    public long getLong(int r, int c, int b) {
        int i = rowOffset[r] + colOffset[c] + b;
        return (long) data[i];
    }

    public void setLong(int i, long v) {
        data[i] = v;
        minmaxinvalid = true;
    }

    public void setLong(int r, int c, int b, long v) {
        int i = rowOffset[r] + colOffset[c] + b;
        data[i] = v;
        minmaxinvalid = true;
    }

    public void set(int i, long v) {
        data[i] = v;
        minmaxinvalid = true;
    }

    public void set(int r, int c, int b, long v) {
        int i = rowOffset[r] + colOffset[c] + b;
        data[i] = v;
        minmaxinvalid = true;
    }

    public float getFloat(int i) {
        return (float) data[i];
    }

    public float getFloat(int r, int c, int b) {
        int i = rowOffset[r] + colOffset[c] + b;
        return (float) data[i];
    }

    public void setFloat(int i, float v) {
        data[i] = v;
        minmaxinvalid = true;
    }

    public void setFloat(int r, int c, int b, float v) {
        int i = rowOffset[r] + colOffset[c] + b;
        data[i] = v;
        minmaxinvalid = true;
    }

    public void set(int i, float v) {
        data[i] = v;
        minmaxinvalid = true;
    }

    public void set(int r, int c, int b, float v) {
        int i = rowOffset[r] + colOffset[c] + b;
        data[i] = v;
        minmaxinvalid = true;
    }

    public double getDouble(int i) {
        return data[i];
    }

    public double getDouble(int r, int c, int b) {
        int i = rowOffset[r] + colOffset[c] + b;
        return data[i];
    }

    public void setDouble(int i, double v) {
        data[i] = v;
        minmaxinvalid = true;
    }

    public void setDouble(int r, int c, int b, double v) {
        int i = rowOffset[r] + colOffset[c] + b;
        data[i] = v;
        minmaxinvalid = true;
    }

    public void set(int i, double v) {
        data[i] = v;
        minmaxinvalid = true;
    }

    public void set(int r, int c, int b, double v) {
        int i = rowOffset[r] + colOffset[c] + b;
        data[i] = v;
        minmaxinvalid = true;
    }

    /**
     * Convert this imageobject to a buffered image that can be rendered. This
     * routine will using the parameters passed in convert the imagedata to an
     * BufferedImage. This BufferedImage can then be used to render the image
     * using standard java.
     * <p/>
     * If fakergb is specified the code will try and render the image in color
     * even though the image might be a single band image (for example when
     * grayscale is set to true). In this case it will take the information from
     * the single band and split that value into a RGB value, for example if the
     * ImageObject is of type byte it will use the 3 most significant bits for
     * the red channel, the next 3 bits for green and the 2 least significant
     * bits for the blue channel.
     *
     * @param bi         buffered image to reuse if possible.
     * @param fakergb    takes the single value of a pixel and treats it as a
     *                   rgb triple. In the case of a byte it will use 3 bits
     *                   for red, 3 for green and 2 for blue.
     * @param scale      use specified scale, if not specified (null) use
     *                   calculated min/max
     * @param usetotals  instead of using the min/max per band use the min/max
     *                   of the image.
     * @param redband    which band in the image is the redband, if set to -1 no
     *                   redband is used.
     * @param greenband  which band in the image is the greenband, if set to -1
     *                   no greenband is used.
     * @param blueband   which band in the image is the blueband, if set to -1
     *                   no blueband is used.
     * @param grayscale  if set to true the image is rendered as a grayscale
     *                   image and grayband is used.
     * @param grayband   which band in the image is the blueband, if set to -1
     *                   no blueband is used.
     * @param gammatable if set each pixel is gamma corrected. The table will
     *                   contain 256 entries.
     * @param alpha      the transparency value to be used, 0 is completely
     *                   transparent and 255 is opague.
     * @return a bufferedimage that contains the imageobject.
     */
    public BufferedImage toBufferedImage(BufferedImage bi, boolean fakergb,
            double[] scale, boolean usetotals, int redband, int greenband,
            int blueband, boolean grayscale, int grayband, int[] gammatable,
            double alpha, double imagescale) {
        int i, a, r, g, b;
        double k, l, m;
        double x, y;
        double col;

        // convert the alpha value to a value between 0 and 255.
        if (alpha <= 0) {
            a = 0x00000000;
        } else if (alpha >= 255) {
            a = 0xff000000;
        } else if (alpha < 1) {
            a = ((int) (255 * alpha) & 0xff) << 24;
        } else {
            a = ((int) (alpha) & 0xff) << 24;
        }

        // calculate the resulting size of the image
        int w = (getData() == null) ? 1 : (int) Math.ceil(numcols * imagescale);
        int h = (getData() == null) ? 1 : (int) Math.ceil(numrows * imagescale);
        double skip = 1.0 / imagescale;
        double skipbands = skip * numbands;
        int line = numcols * numbands;

        // make sure the image exists that will hold final image
        int buftype = (a == 0xff000000) ? BufferedImage.TYPE_INT_RGB
                : BufferedImage.TYPE_INT_ARGB;
        if ((bi == null) || (bi.getType() != buftype) || (bi.getWidth() != w)
                || (bi.getHeight() != h)) {
            bi = new BufferedImage(w, h, buftype);
        }

        // get the raster
        int[] raster = ((DataBufferInt) bi.getRaster().getDataBuffer()).getData();

        // in case of only header, return white image
        if (data == null) {
            Arrays.fill(raster, a | 0x00ffffff);
            return bi;
        }

        // if no gamma table is provided use the standard gamma table.
        if (gammatable == null) {
            gammatable = nogamma;
        }

        if (grayscale) {
            double min, max, div;

            k = (grayband < 0 || grayband >= numbands) ? 0 : grayband;
            if ((scale != null) && (scale.length == 2)) {
                min = scale[0];
                max = scale[1];
            } else {
                min = getMin(usetotals ? numbands : (int) k);
                max = getMax(usetotals ? numbands : (int) k);
            }
            // modification for test, should be deleted later
            // min = 0; max = 1000;
            // end of modification
            div = 255.0 / (max - min);
            for (i = 0, y = 0; y < numrows; y += skip) {
                x = y * line;
                k = (grayband < 0 || grayband >= numbands) ? x : x + grayband;
                for (x = 0; x < numcols; x += skip, i++) {
                    col = data[(int) k];
                    if (col <= min) {
                        col = min;
                    } else if (col >= max) {
                        col = max;
                    }
                    col = (col - min) * div;
                    g = gammatable[(int) col & 0xff];
                    raster[i] = a | (g << 16) | (g << 8) | g;
                    k += skipbands;
                }
            }

        } else {
            double[] min = new double[3];
            double[] max = new double[3];
            double[] div = new double[3];

            k = (redband < 0 || redband >= numbands) ? -1 : redband;
            l = (greenband < 0 || greenband >= numbands) ? -1 : greenband;
            m = (blueband < 0 || blueband >= numbands) ? -1 : blueband;
            r = 0;
            g = 0;
            b = 0;

            if ((scale != null) && (scale.length == 2)) {
                min[0] = scale[0];
                max[0] = scale[1];
                min[1] = scale[0];
                max[1] = scale[1];
                min[2] = scale[0];
                max[2] = scale[1];
            } else {
                min[0] = getMin(usetotals || (k < 0) ? numbands : (int) k);
                max[0] = getMax(usetotals || (k < 0) ? numbands : (int) k);
                min[1] = getMin(usetotals || (l < 0) ? numbands : (int) l);
                max[1] = getMax(usetotals || (l < 0) ? numbands : (int) l);
                min[2] = getMin(usetotals || (m < 0) ? numbands : (int) m);
                max[2] = getMax(usetotals || (m < 0) ? numbands : (int) m);
            }
            div[0] = 255.0 / (max[0] - min[0]);
            div[1] = 255.0 / (max[1] - min[1]);
            div[2] = 255.0 / (max[2] - min[2]);

            for (i = 0, y = 0; y < numrows; y += skip) {
                x = y * line;
                k = (k < 0) ? -1 : x + redband;
                l = (l < 0) ? -1 : x + greenband;
                m = (m < 0) ? -1 : x + blueband;
                for (x = 0; x < numcols; x += skip, i++) {
                    if (k >= 0) {
                        col = data[(int) k];
                        if (col <= min[0]) {
                            col = min[0];
                        } else if (col >= max[0]) {
                            col = max[0];
                        }
                        col = (col - min[0]) * div[0];
                        r = gammatable[(int) col & 0xff];
                        k += skipbands;
                    }
                    if (l >= 0) {
                        col = data[(int) l];
                        if (col <= min[1]) {
                            col = min[1];
                        } else if (col >= max[1]) {
                            col = max[1];
                        }
                        col = (col - min[1]) * div[1];
                        g = gammatable[(int) col & 0xff];
                        l += skipbands;
                    }
                    if (m >= 0) {
                        col = data[(int) m];
                        if (col <= min[2]) {
                            col = min[2];
                        } else if (col >= max[2]) {
                            col = max[2];
                        }
                        col = (col - min[2]) * div[2];
                        b = gammatable[(int) col & 0xff];
                        m += skipbands;
                    }
                    raster[i] = a | (r << 16) | (g << 8) | b;
                }
            }
        }

        return bi;
    }

    // ------------------------------------------------------------------------
    // EXTERNALIZATION
    // ------------------------------------------------------------------------

    /**
     * Read the ImageObject from the InputStream. This will read and write the
     * data that makes up the imageobject to the objectstream. If anything in
     * the dataformat has changed, the version will need to be upped and a new
     * case statement should be added. This will enable backwards compatibility
     * of the objects written to disk.
     * 
     * @param in
     *            The stream from which to read the object.
     * @throws IOException
     *             If the data could not be read.
     * @throws ClassNotFoundException
     *             If the class for an object being restored cannot be found.
     * 
     */
    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
        int version = (int) in.readLong();

        switch (version) {
        case 1:
            // read the rows, cols, bands
            numrows = in.readInt();
            numcols = in.readInt();
            numbands = in.readInt();

            // read the data type
            type = in.readInt();

            // read the headeronly
            headerOnly = in.readBoolean();

            // read the invalid data value
            invaliddata = in.readDouble();

            // set the size, this will create an array if needed
            setSize(numcols, numrows, numbands);

            // if not header only read the data
            if (!headerOnly) {
                for (int i = 0; i < size; i++) {
                    data[i] = in.readDouble();
                }
            }

            // read the properties
            int count = in.readInt();
            for (int i = 0; i < count; i++) {
                String key = in.readUTF();
                try {
                    Object val = in.readObject();
                    setProperty(key, val);
                } catch (ClassNotFoundException exc) {
                    log.warn("Could not read object for key [" + key + "].",
                            exc);
                }
            }
            break;
        default:
            throw (new IOException("Could not read object of version "
                    + version));
        }
    }

    /**
     * Write the ImageObject to the OutputStream. This will write the data that
     * makes up the imageobject to the objectstream. If anything in the
     * dataformat has changed, the version will need to be upped. This version
     * number is used in readExternal forbackwards compatibility of the objects
     * written to disk.
     * 
     * @param in
     *            The stream from which to read the object.
     * @throws IOException
     *             If the data could not be read.
     * @throws ClassNotFoundException
     *             If the class for an object being restored cannot be found.
     * 
     */
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeLong(serialVersionUID);

        // standard write out rows, cols, bands
        out.writeInt(numrows);
        out.writeInt(numcols);
        out.writeInt(numbands);

        // write the datatype
        out.writeInt(type);

        // write the headeronly flag
        out.writeBoolean(headerOnly);

        // write the invalid data value
        out.writeDouble(invaliddata);

        // write the data
        if (!headerOnly) {
            for (int i = 0; i < size; i++) {
                out.writeDouble(data[i]);
            }
        }

        // write the properties
        if (properties == null) {
            out.writeInt(0);
        } else {
            ArrayList<String> keys = new ArrayList<String>();
            for (String key : properties.keySet()) {
                if (!key.startsWith("_")) {
                    keys.add(key);
                }
            }
            out.writeInt(keys.size());
            for (String key : keys) {
                out.writeUTF(key);
                out.writeObject(properties.get(key));
            }
        }
    }
}
