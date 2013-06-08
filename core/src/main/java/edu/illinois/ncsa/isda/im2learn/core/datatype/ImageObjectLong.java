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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ImageObjectLong extends ImageObjectInCore implements
        Externalizable {
    private static final long serialVersionUID = 1L;

    private static Log log = LogFactory.getLog(ImageObjectLong.class);

    private long[] data;

    /**
     * Empty constructor needed for externalization. This constructor should not be
     * used by any code except for the externalization functions.
     */
    public ImageObjectLong() {
        this(0, 0, 0, false);
    }

    public ImageObjectLong(int rows, int cols, int bands) {
        this(rows, cols, bands, false);
    }

    public ImageObjectLong(int rows, int cols, int bands, boolean headeronly) {
        super(rows, cols, bands, headeronly);
        this.type = TYPE_LONG;
    }

    public void createArray() {
        data = new long[size];
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
        Arrays.fill(data, (long) dataval);
        minmaxinvalid = true;
    }

    static public double getTypeMinimum() {
        return Long.MIN_VALUE;
    }

    static public double getTypeMaximum() {
        return Long.MAX_VALUE;
    }

    public double getTypeMin() {
        return Long.MIN_VALUE;
    }

    public double getTypeMax() {
        return Long.MAX_VALUE;
    }

    public byte getByte(int i) {
        return (byte) (data[i] & 0xff);
    }

    public byte getByte(int r, int c, int b) {
        int i = rowOffset[r] + colOffset[c] + b;
        return (byte) (data[i] & 0xff);
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
        return data[i];
    }

    public long getLong(int r, int c, int b) {
        int i = rowOffset[r] + colOffset[c] + b;
        return data[i];
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
        return data[i];
    }

    public float getFloat(int r, int c, int b) {
        int i = rowOffset[r] + colOffset[c] + b;
        return data[i];
    }

    public void setFloat(int i, float v) {
        data[i] = (long) v;
        minmaxinvalid = true;
    }

    public void setFloat(int r, int c, int b, float v) {
        int i = rowOffset[r] + colOffset[c] + b;
        data[i] = (long) v;
        minmaxinvalid = true;
    }

    public void set(int i, float v) {
        data[i] = (long) v;
        minmaxinvalid = true;
    }

    public void set(int r, int c, int b, float v) {
        int i = rowOffset[r] + colOffset[c] + b;
        data[i] = (long) v;
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
        data[i] = (long) v;
        minmaxinvalid = true;
    }

    public void setDouble(int r, int c, int b, double v) {
        int i = rowOffset[r] + colOffset[c] + b;
        data[i] = (long) v;
        minmaxinvalid = true;
    }

    public void set(int i, double v) {
        data[i] = (long) v;
        minmaxinvalid = true;
    }

    public void set(int r, int c, int b, double v) {
        int i = rowOffset[r] + colOffset[c] + b;
        data[i] = (long) v;
        minmaxinvalid = true;
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
                    data[i] = in.readLong();
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
                out.writeLong(data[i]);
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
