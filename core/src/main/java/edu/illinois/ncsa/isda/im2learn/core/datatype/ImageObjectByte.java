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

public class ImageObjectByte extends ImageObjectInCore implements Externalizable {
    private static final long serialVersionUID = 1L;
    private static Log log = LogFactory.getLog(ImageObject.class);

    private byte[] data;

    /**
     * Empty constructor needed for externalization. This constructor should not
     * be used by any code except for the externalization functions.
     */
    public ImageObjectByte() {
        this(0, 0, 0, false);
    }

    public ImageObjectByte(int rows, int cols, int bands) {
        this(rows, cols, bands, false);
    }

    public ImageObjectByte(int rows, int cols, int bands, boolean headeronly) {
        super(rows, cols, bands, headeronly);
        this.type = TYPE_BYTE;
    }

    public void createArray() {
        data = new byte[size];
        minmaxinvalid = true;
        headerOnly = false;
        computeOffsets();
    }

    public void deleteArray() {
        data = null;
        deleteOffsets();
        headerOnly = true;
    }

    protected void writeArray(ObjectOutput out) throws IOException {
        out.write(data);
    }
    
    protected void readArray(ObjectInput in) throws IOException {
        in.read(data);
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        System.arraycopy(data, 0, this.data, 0, size);
        minmaxinvalid = true;
    }

    public void setData(double dataval) {
        Arrays.fill(data, (byte) ((long) dataval & 0xff));
        minmaxinvalid = true;
    }

    static public double getTypeMinimum() {
        return 0;
    }

    static public double getTypeMaximum() {
        return 0xff;
    }

    public double getTypeMin() {
        return 0;
    }

    public double getTypeMax() {
        return 0xff;
    }

    public byte getByte(int i) {
        return data[i];
    }

    public byte getByte(int r, int c, int b) {
        int i = rowOffset[r] + colOffset[c] + b;
        return data[i];
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
        return (short) (data[i] & 0xff);
    }

    public short getShort(int r, int c, int b) {
        int i = rowOffset[r] + colOffset[c] + b;
        return (short) (data[i] & 0xff);
    }

    public void setShort(int i, short v) {
        data[i] = (byte) v;
        minmaxinvalid = true;
    }

    public void setShort(int r, int c, int b, short v) {
        int i = rowOffset[r] + colOffset[c] + b;
        data[i] = (byte) v;
        minmaxinvalid = true;
    }

    public void set(int i, short v) {
        data[i] = (byte) v;
        minmaxinvalid = true;
    }

    public void set(int r, int c, int b, short v) {
        int i = rowOffset[r] + colOffset[c] + b;
        data[i] = (byte) v;
        minmaxinvalid = true;
    }

    public int getInt(int i) {
        return data[i] & 0xff;
    }

    public int getInt(int r, int c, int b) {
        int i = rowOffset[r] + colOffset[c] + b;
        return data[i] & 0xff;
    }

    public void setInt(int i, int v) {
        data[i] = (byte) v;
        minmaxinvalid = true;
    }

    public void setInt(int r, int c, int b, int v) {
        int i = rowOffset[r] + colOffset[c] + b;
        data[i] = (byte) v;
        minmaxinvalid = true;
    }

    public void set(int i, int v) {
        data[i] = (byte) v;
        minmaxinvalid = true;
    }

    public void set(int r, int c, int b, int v) {
        int i = rowOffset[r] + colOffset[c] + b;
        data[i] = (byte) v;
        minmaxinvalid = true;
    }

    public long getLong(int i) {
        return data[i] & 0xff;
    }

    public long getLong(int r, int c, int b) {
        int i = rowOffset[r] + colOffset[c] + b;
        return data[i] & 0xff;
    }

    public void setLong(int i, long v) {
        data[i] = (byte) v;
        minmaxinvalid = true;
    }

    public void setLong(int r, int c, int b, long v) {
        int i = rowOffset[r] + colOffset[c] + b;
        data[i] = (byte) v;
        minmaxinvalid = true;
    }

    public void set(int i, long v) {
        data[i] = (byte) (v & 0xff);
        minmaxinvalid = true;
    }

    public void set(int r, int c, int b, long v) {
        int i = rowOffset[r] + colOffset[c] + b;
        data[i] = (byte) (v & 0xff);
        minmaxinvalid = true;
    }

    public float getFloat(int i) {
        return data[i] & 0xff;
    }

    public float getFloat(int r, int c, int b) {
        int i = rowOffset[r] + colOffset[c] + b;
        return data[i] & 0xff;
    }

    public void setFloat(int i, float v) {
        data[i] = (byte) v;
        minmaxinvalid = true;
    }

    public void setFloat(int r, int c, int b, float v) {
        int i = rowOffset[r] + colOffset[c] + b;
        data[i] = (byte) v;
        minmaxinvalid = true;
    }

    public void set(int i, float v) {
        data[i] = (byte) v;
        minmaxinvalid = true;
    }

    public void set(int r, int c, int b, float v) {
        int i = rowOffset[r] + colOffset[c] + b;
        data[i] = (byte) v;
        minmaxinvalid = true;
    }

    public double getDouble(int i) {
        return data[i] & 0xff;
    }

    public double getDouble(int r, int c, int b) {
        int i = rowOffset[r] + colOffset[c] + b;
        return data[i] & 0xff;
    }

    public void setDouble(int i, double v) {
        data[i] = (byte) v;
        minmaxinvalid = true;
    }

    public void setDouble(int r, int c, int b, double v) {
        int i = rowOffset[r] + colOffset[c] + b;
        data[i] = (byte) v;
        minmaxinvalid = true;
    }

    public void set(int i, double v) {
        data[i] = (byte) v;
        minmaxinvalid = true;
    }

    public void set(int r, int c, int b, double v) {
        int i = rowOffset[r] + colOffset[c] + b;
        data[i] = (byte) v;
        minmaxinvalid = true;
    }

    public BufferedImage toBufferedImage(BufferedImage bi, boolean fakergb,
            double[] scale, boolean usetotals, int redband, int greenband,
            int blueband, boolean grayscale, int grayband, int[] gammatable,
            double alpha, double imagescale) {
        int i, a, r, g, b, t;
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
        int buftype = (a == 0xff000000) ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
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

        // for bytes, pseudocolor can happen for 1 or 2 band data, just
        // split the data over 3 bytes.
        if (fakergb && ((numbands < 2) || grayscale)) {
            if (grayscale || (numbands == 1)) {
                for (i = 0, y = 0; y < numrows; y += skip) {
                    x = y * line;
                    k = (!grayscale || grayband < 0 || grayband >= numbands) ? x
                            : x + grayband;
                    for (x = 0; x < numcols; x += skip, i++) {
                        t = gammatable[data[(int) k] & 0xff];
                        r = gammatable[(t & 0xe0) & 0xff];
                        g = gammatable[((t & 0x1c) << 3) & 0xff];
                        b = gammatable[((t & 0x03) << 5) & 0xff];
                        raster[i] = a | (r << 16) | (g << 8) | b;
                        k += skipbands;
                    }
                }
            } else {
                for (i = 0, y = 0; y < numrows; y += skip) {
                    x = y * line;
                    k = x;
                    for (x = 0; x < numcols; x += skip, i++) {
                        col = (short) (data[(int) k] << 8) | data[(int) k + 1];
                        r = gammatable[((((short) col) & 0xfc00) >> 8) & 0xff];
                        g = gammatable[((((short) col) & 0x03e0) >> 2) & 0xff];
                        b = gammatable[((((short) col) & 0x001f) << 3) & 0xff];
                        raster[i] = a | (r << 16) | (g << 8) | b;
                        k += skipbands;
                    }
                }
            }

        } else if (grayscale) {
            for (i = 0, y = 0; y < numrows; y += skip) {
                x = y * line;
                k = (grayband < 0 || grayband >= numbands) ? x : x + grayband;
                for (x = 0; x < numcols; x += skip, i++) {
                    g = gammatable[data[(int) k] & 0xff];
                    raster[i] = a | (g << 16) | (g << 8) | g;
                    k += skipbands;
                }
            }

        } else {
            k = (redband < 0 || redband >= numbands) ? -1 : redband;
            l = (greenband < 0 || greenband >= numbands) ? -1 : greenband;
            m = (blueband < 0 || blueband >= numbands) ? -1 : blueband;
            r = 0;
            g = 0;
            b = 0;

            for (i = 0, y = 0; y < numrows; y += skip) {
                x = y * line;
                k = (k < 0) ? -1 : x + redband;
                l = (l < 0) ? -1 : x + greenband;
                m = (m < 0) ? -1 : x + blueband;
                for (x = 0; x < numcols; x += skip, i++) {
                    if (k >= 0) {
                        r = gammatable[data[(int) k] & 0xff];
                        k += skipbands;
                    }
                    if (l >= 0) {
                        g = gammatable[data[(int) l] & 0xff];
                        l += skipbands;
                    }
                    if (m >= 0) {
                        b = gammatable[data[(int) m] & 0xff];
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
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        int version = (int) in.readLong();

        switch (version) {
        case 1:
            // read the rows, cols, bands
            numrows = in.readInt();
            numcols = in.readInt();
            numbands = in.readInt();

            // read the data type
            if (type != in.readInt()) {
                throw(new IOException("Not the right type."));
            }

            // read the headeronly
            headerOnly = in.readBoolean();

            // read the invalid data value
            invaliddata = in.readDouble();

            // set the size, this will create an array if needed
            setSize(numcols, numrows, numbands);

            // if not header only read the data
            if (!headerOnly) {
                for (int i = 0; i < size; i++) {
                    data[i] = in.readByte();
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
                    log.warn("Could not read object for key [" + key + "].", exc);
                }
            }
            break;
        default:
            throw (new IOException("Could not read object of version " + version));
        }
    }

    /**
     * Write the ImageObject to the OutputStream. This will write the data
     * that makes up the imageobject to the objectstream. If anything in the
     * dataformat has changed, the version will need to be upped. This
     * version number is used in readExternal forbackwards compatibility of
     * the objects written to disk.
     * 
     * @param in
     *                The stream from which to read the object.
     * @throws IOException
     *                 If the data could not be read.
     * @throws ClassNotFoundException
     *                 If the class for an object being restored cannot be
     *                 found.
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
                out.writeByte(data[i]);
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
