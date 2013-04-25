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

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.PixelGrabber;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//import uiuc.TestMosaicUI;

// TODO should try and optimize this class

/**
 * Base ImageObject. This class is used to store information about an image. The
 * image is stored in row major order with the bands interleaving the pixels
 * (for example an image with 3 rows, 2 columns and 3 bands is stored as
 * RGBRGBRGBRGBRGBRGB where the first triplet is the pixel at row 1, column 1,
 * the second triplet is the pixel at row 1, column 2 etc.). ImageObject can be
 * of any datatype, any number of bands (samples per pixel), rows (height) or
 * columns (width).
 * <p/>
 * 
 * Any additional information about the image can be stored as a property
 * associated with the image. Properties that begin with an underscore are
 * temporary properties and will not be copied when the image is cloned, or
 * saved to disk. Some common properties have their keys predefined in this
 * class.
 * <p/>
 * 
 * How to speed up your application. This class is an abstract class, any time
 * you call getXXX() it will have to do a lookup to see what class really to
 * call, and call the functions on this class. So one speedup is to simply do
 * the check your self, typecast the ImageObject to the appropriate class and
 * call the functions on this class. Do this either inside the loop or make
 * multiple loops for each datatype (still keeping the abstract version in case
 * a datatype is missing in your implementation). The fastest way is to use the
 * getData function and typecast the returned object to the appropriate array
 * (byte[], double[] etc.), this will give you direct access to the imagedata.
 * If any data is modified computeMinMax or setMinMaxInvalid will need to be
 * called to mark the fact that the current minimum and maximum values
 * associated with the image are not valid.
 * <p/>
 * 
 * An ImageObject can also be used to just store information about an image and
 * will not have any imagedata associated with it in that case. To test if an
 * ImageObject has any data, use isHeaderOnly(). Accessing one of the get or set
 * functions when no data is present will throw a NullPointer exception. No
 * checks are done in the get and set function to see if there is any imagedata
 * to speed them up.
 * 
 * @author Rob Kooper
 * @version 2.0
 */
public abstract class ImageObject implements Cloneable {
    private static Log                logger           = LogFactory.getLog(ImageObject.class);

    /**
     * ImageObject contains bytes.
     */
    public static final int           TYPE_BYTE        = 0;

    /**
     * ImageObject contains shorts.
     */
    public static final int           TYPE_SHORT       = 1;

    /**
     * ImageObject contains shorts.
     */
    public static final int           TYPE_USHORT      = 2;

    /**
     * ImageObject contains ints.
     */
    public static final int           TYPE_INT         = 3;

    /**
     * ImageObject contains longs.
     */
    public static final int           TYPE_LONG        = 4;

    /**
     * ImageObject contains floats.
     */
    public static final int           TYPE_FLOAT       = 5;

    /**
     * ImageObject contains doubles.
     */
    public static final int           TYPE_DOUBLE      = 6;

    /**
     * ImageObject contains unknown image data, always last.
     */
    public static final int           TYPE_UNKNOWN     = 7;

    /**
     * List of types supported by imageobject as strings. The order in the array
     * matches the integer type, for example types[TYPE_INT] will return the
     * string representation of a integer type.
     */
    public static final String[]      types            = new String[] { "BYTE", "SHORT", "USHORT", "INT", "LONG", "FLOAT", "DOUBLE", "UNKNOWN" };

    /**
     * Key to access an array of wavelengths in the image properties. This is
     * assumed to be an array of values.
     */
    public static final String        WAVELENGTH       = "wavelength";

    /**
     * Key to access a triplet of integers (int[3]) in the image properties
     * giving the default red, green and blue bands to use when rendering.
     */
    public static final String        DEFAULT_RGB      = "defaultrgb";

    /**
     * Key to access a single integer (int[1]) in the image properties that
     * lists the band to use when rendering the image as grayscale.
     */
    public static final String        DEFAULT_GRAY     = "defaultgray";

    /**
     * Key to access a single double (double[1]) in the image properties that is
     * the gamma to use when rendering the image.
     */
    public static final String        DEFAULT_GAMMA    = "defaultgamma";

    /**
     * Key to access a String in the image properties that describes the image.
     */
    public static final String        COMMENT          = "comment";

    /**
     * Key to access any GeoInformation in the image properties.
     */
    public static final String        GEOINFO          = "geoinfo";

    /**
     * Key to access the full path name (including any subpaths in case of HDF)
     * in the image properties of where the file is loaded from, or last saved
     * to.
     */
    public static final String        FILENAME         = "_filename";

    /**
     * Key to access the array of String labels that are given to image bands.
     */
    public static final String        BAND_LABELS      = "band_labels";

    /**
     * The maximal number of units (pixels times bands) in an in-core image.
     */
    // protected static int MAX_IN_CORE_SIZE = 640*480; // cols*rows*bands
    // protected static int MAX_IN_CORE_SIZE = 2048*2048; // cols*rows
    protected static int              MAX_IN_CORE_SIZE = Integer.MAX_VALUE;

    /**
     * Type of image, one of the TYPE_ defined earlier.
     */
    protected int                     type;

    /**
     * Number of columns, or width of an image.
     */
    protected int                     numcols;

    /**
     * Number of rows, or height of an image.
     */
    protected int                     numrows;

    /**
     * Number of bands, or slices, or samples per pixel, of an image.
     */
    protected int                     numbands;

    /**
     * The size of the whole image. This is numrows * numcols * numbands.
     */
    protected int                     size;

    /**
     * Array with the minimum values per band. This is calculated the first time
     * getMin() or getMax() is called. The array is of size bands+1 where the
     * last value in the array is the absolute minimum in the image.
     */
    protected double[]                minval;

    /**
     * Array with the maximum values per band. This is calculated the first time
     * getMin() or getMax() is called. The array is of size bands+1 where the
     * last value in the array is the absolute maximum in the image.
     */
    protected double[]                maxval;

    /**
     * List of properties stored with the image. The properties are accessed
     * using a key. Some common keys are defined in this class.
     */
    protected HashMap<String, Object> properties;

    /**
     * Set to false if calling getMin() or getMax() should recompute the minimum
     * and maximum values for the image.
     */
    protected boolean                 minmaxinvalid;

    /**
     * Indicates whether data is present. True means no data (header only).
     */
    protected boolean                 headerOnly;

    /**
     * The following value can be used in images to indicate that there is no
     * valid data at this pixel. If this value is Double.NaN it means it is not
     * set. When using ImageObjectDouble, use Double.Inf to indicate that there
     * is no datavalue since Double.NaN is used to indicate that there is no
     * invalid data in the image.
     */
    protected double                  invaliddata      = Double.NaN;

    /**
     * key to access nodata value associated with imageObject
     */
    // public static final String NODATAVALUE ="nodatavalue";
    /**
     * Array with gamma values if no gamma is provided in the toBufferedImage.
     */
    protected static int[]            nogamma;

    static {
        nogamma = new int[256];
        for (int i = 0; i < 256; i++) {
            nogamma[i] = i;
        }
    }

    /**
     * Initialization that is common to most constructors.
     * 
     * @param rows
     * @param cols
     * @param bands
     * @param headeronly
     * @param type
     */
    protected void init(int rows, int cols, int bands, boolean headeronly, int type) {
        this.headerOnly = headeronly;
        this.type = type;
        this.setSize(cols, rows, bands);
    }

    /**
     * Create a new image. This will create an image with requested parameters
     * and call createArray if headeronly is false. This constructor is intended
     * to be called by ImageObjectInCore and its subclasses.
     * 
     * @param rows
     *            of pixels in the image.
     * @param cols
     *            of pixels in the image.
     * @param bands
     *            or samples per pixel in the image.
     * @param headeronly
     *            is true if no array should be created to store the pixels.
     */
    protected ImageObject(int rows, int cols, int bands, boolean headeronly) {
        this.init(rows, cols, bands, headeronly, ImageObject.TYPE_UNKNOWN);
        // logger.info( "Created an in-core ImageObject" );
    }

    /**
     * Create a new image. This will create an image with requested parameters
     * and call createArray if headeronly is false. This constructor is intended
     * to be called by ImageObjectInCore and its subclasses.
     * 
     * @param rows
     *            of pixels in the image.
     * @param cols
     *            of pixels in the image.
     * @param bands
     *            or samples per pixel in the image.
     * @param headeronly
     *            is true if no array should be created to store the pixels.
     * @param type
     *            of the object (one of ImageObject.TYPE_*).
     */
    protected ImageObject(int rows, int cols, int bands, boolean headeronly, int type) {
        this.init(rows, cols, bands, headeronly, type);
        // logger.info( "Created an out-of-core ImageObject" );
    }

    /**
     * Create a clone of the image. This will create a clone of the data and of
     * all the properties that are not private (those starting with an
     * underscore).
     * 
     * @return clone of image.
     * @throws CloneNotSupportedException
     *             if an error occured creating clone.
     */
    @Override
    public Object clone() throws CloneNotSupportedException {

        // create the cloned image and copy its properties.
        ImageObject copy;
        try {
            copy = createImage(numrows, numcols, numbands, type, headerOnly);
            copy.properties = cloneProperties();
        } catch (ImageException exc) {
            throw (new CloneNotSupportedException("Error creating clone. " + exc.toString()));
        }

        // copy the data if needed.
        if (!headerOnly) {
            if (this.isInCore() & copy.isInCore()) {
                ((ImageObjectInCore) copy).copyAllDataFrom((ImageObjectInCore) this);
            } else {
                copy.copyAllDataFrom(this);
            }
        }

        copy.setInvalidData(getInvalidData());

        return copy;
    }

    /**
     * Create a deepclone of the properties. This will create a deepclone of the
     * properties. It will remove all properties starting with a underscore.
     * 
     * @return copy of the properties.
     * @throws ImageException
     *             if an error occured copying properties.
     */
    public HashMap<String, Object> cloneProperties() throws ImageException {
        if (properties == null) {
            return null;

        }
        HashMap<String, Object> hm = null;

        try {
            // First serialize the properties
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(bos);
            out.writeObject(properties);
            out.close();

            // Get the bytes of the serialized object
            byte[] bytes = bos.toByteArray();

            // Deserialize from a byte array
            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes));
            hm = (HashMap<String, Object>) in.readObject();
            in.close();
        } catch (IOException exc) {
            throw (new ImageException(exc));
        } catch (ClassNotFoundException exc) {
            throw (new ImageException(exc));
        }

        // Remove all properties starting with _ since they are temp
        for (Iterator i = hm.keySet().iterator(); i.hasNext();) {
            Object key = i.next();
            if (key.toString().startsWith("_")) {
                i.remove();
            }
        }

        return hm;
    }

    public void destroy() {
        deleteArray();
        headerOnly = true;
        numbands = 0;
        numrows = 0;
        numcols = 0;
        size = 0;
        type = TYPE_UNKNOWN;
        minmaxinvalid = false;
        maxval = null;
        minval = null;
        properties = null;
        invaliddata = Double.NaN;
    }

    // ------------------------------------------------------------------------
    // SPECIAL CONSTRUCTORS
    // ------------------------------------------------------------------------

    /**
     * Create a new image. The image created has the requested size and type and
     * will have space allocated for the imagedata.
     * 
     * @param rows
     *            of pixels in the image.
     * @param cols
     *            of pixels in the image.
     * @param bands
     *            or samples per pixel in the image
     * @param type
     *            of the image.
     * @return a newly created image.
     * @throws ImageException
     *             if the type is not valid.
     */
    static public ImageObject createImage(int rows, int cols, int bands, int type) throws ImageException {
        return createImage(rows, cols, bands, type, false);
    }

    /**
     * Create a new image. The image created has the requested size and type.
     * 
     * @param rows
     *            of pixels in the image.
     * @param cols
     *            of pixels in the image.
     * @param bands
     *            or samples per pixel in the image
     * @param type
     *            of the image.
     * @param headeronly
     *            is true if no data should allocated.
     * @return a newly created image.
     * @throws ImageException
     *             if the type is not valid.
     */
    static public ImageObject createImage(int rows, int cols, int bands, int type, boolean headeronly) throws ImageException {

        // check whether we want to create an in-core or an out-of-core.
        if ((long) rows * cols > MAX_IN_CORE_SIZE) {
            return ImageObjectOutOfCore.createImage(rows, cols, bands, type, headeronly);
        } else {
            return ImageObjectInCore.createImage(rows, cols, bands, type, headeronly);
        }
    }

    /**
     * Create a new image. The image created has the requested size and type and
     * will have space allocated for the imagedata.
     * 
     * @param rows
     *            of pixels in the image.
     * @param cols
     *            of pixels in the image.
     * @param bands
     *            or samples per pixel in the image
     * @param type
     *            of the image.
     * @return a newly created image.
     * @throws ImageException
     *             if the type is not valid.
     */
    static public ImageObject createImage(int rows, int cols, int bands, String type) throws ImageException {
        return createImage(rows, cols, bands, type, false);
    }

    /**
     * Create a new image. The image created has the requested size and type.
     * 
     * @param rows
     *            of pixels in the image.
     * @param cols
     *            of pixels in the image.
     * @param bands
     *            or samples per pixel in the image
     * @param type
     *            of the image.
     * @param header
     *            is true if no data should allocated.
     * @return a newly created image.
     * @throws ImageException
     *             if the type is not valid.
     */
    static public ImageObject createImage(int rows, int cols, int bands, String type, boolean header) throws ImageException {
        for (int i = 0; i < types.length; i++) {
            if (type.equalsIgnoreCase(types[i])) {
                return createImage(rows, cols, bands, i, header);
            }
        }
        throw (new ImageException("Can't create new image of this type."));
    }

    // ------------------------------------------------------------------------
    // GETTERS & SETTERS
    // ------------------------------------------------------------------------

    /**
     * Return the number of columns, or width, of the image.
     * 
     * @return columns or width of the image.
     */
    public int getNumCols() {
        return numcols;
    }

    /**
     * Sets the columns, or width, of an image. This will remove all existing
     * image data.
     * 
     * @param cols
     *            is the new columns, or width, of the image.
     */
    public void setNumCols(int cols) {
        this.setSize(cols, this.numrows, this.numbands);
    }

    /**
     * @deprecated use getNumCols()
     */
    @Deprecated
    public int getCols() {
        return numcols;
    }

    /**
     * @deprecated use setNumCols()
     */
    @Deprecated
    public void setCols(int w) {
        setNumCols(w);
    }

    /**
     * @deprecated use getNumCols()
     */
    @Deprecated
    public int getWidth() {
        return numcols;
    }

    /**
     * @deprecated use setNumCols()
     */
    @Deprecated
    public void setWidth(int w) {
        setNumCols(w);
    }

    /**
     * @return
     */
    public int getNumRows() {
        return numrows;
    }

    /**
     * Sets the rows, or height, of an image. This will remove all existing
     * image data.
     * 
     * @param rows
     *            is the new rows, or height, of the image.
     */
    public void setNumRows(int rows) {
        this.setSize(this.numcols, rows, this.numbands);
    }

    /**
     * @deprecated use getNumRows()
     */
    @Deprecated
    public int getRows() {
        return numrows;
    }

    /**
     * @deprecated use setNumRows()
     */
    @Deprecated
    public void setRows(int h) {
        setNumRows(h);
    }

    /**
     * @deprecated use getNumRows()
     */
    @Deprecated
    public int getHeight() {
        return numrows;
    }

    /**
     * @deprecated use setNumRows()
     */
    @Deprecated
    public void setHeight(int h) {
        setNumRows(h);
    }

    public int getNumBands() {
        return numbands;
    }

    /**
     * Sets the number of bands, or depth, of an image. This will remove all
     * existing image data.
     * 
     * @param bands
     *            is the new number of bands, or depth, of the image.
     */
    public void setNumBands(int bands) {
        this.setSize(this.numcols, this.numrows, bands);
    }

    /**
     * @deprecated use getNumBands()
     */
    @Deprecated
    public int getBands() {
        return numbands;
    }

    /**
     * @deprecated use setNumBands()
     */
    @Deprecated
    public void setBands(int b) {
        setNumBands(b);
    }

    public int getSize() {
        return size;
    }

    /**
     * Sets the columns/width, rows/height, and bands/depth of an image. This
     * will remove all existing image data.
     * 
     * @param numcols
     *            is the new columns, or width, of the image.
     * @param numrows
     *            is the new rows, or height, of the image.
     * @param numbands
     *            is the new bands, or depth, of the image.
     */
    public void setSize(int numcols, int numrows, int numbands) {
        this.numcols = numcols;
        this.numrows = numrows;
        this.numbands = numbands;
        this.minval = new double[numbands + 1];
        this.maxval = new double[numbands + 1];
        this.size = numrows * numcols * numbands;
        this.minmaxinvalid = true;

        for (int i = 0; i < numbands + 1; i++) {
            minval[i] = Double.MAX_VALUE;
            maxval[i] = -Double.MAX_VALUE;
        }

        if (!this.headerOnly) {
            createArray();
        }
    }

    /**
     * @return the maximal size of in an in-core image.
     */
    public static int getMaxInCoreSize() {
        return MAX_IN_CORE_SIZE;
    }

    /**
     * @param newMax
     *            the new maximal size of an in-core image.
     * @return the previous maximal size of an in-core image.
     */
    public static int setMaxInCoreSize(int newMax) {
        int temp = MAX_IN_CORE_SIZE;
        MAX_IN_CORE_SIZE = newMax;
        return temp;
    }

    public int getType() {
        return type;
    }

    public String getTypeString() {
        return types[type];
    }

    public HashMap<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(HashMap<String, Object> properties) {
        this.properties = properties;
    }

    public Object getProperty(String key) {
        if (properties != null) {
            return properties.get(key);
        } else {
            return null;
        }
    }

    public Object setProperty(String key, Object val) {
        if (key == null) {
            return null;
        }
        if (val == null) {
            if (properties == null) {
                return null;
            }
            if (!properties.containsKey(key)) {
                return null;
            }
            return properties.remove(key);
        }
        if (properties == null) {
            properties = new HashMap<String, Object>();
        }
        return properties.put(key, val);
    }

    /**
     * @return the filename of the object.
     */
    public String getFileName() {
        return (String) this.getProperty(ImageObject.FILENAME);
    }

    /**
     * @return true if the object is in-core. Also see isOutOfCore.
     */
    public boolean isInCore() {
        return !isOutOfCore();
    }

    /**
     * Return the value used in the image if there is invaliddata. If this is
     * Double.NaN it means that there is no invalid data in the image.
     * 
     * @return the value used to mark invalid data.
     */
    public double getInvalidData() {
        return invaliddata;
    }

    /**
     * Sets the value used in the imagedata to mark invalid data. Set this to
     * Double.NaN to indicate there is no invalid data in the image.
     * 
     * @param invaliddata
     *            value used in the image data to mark invalid data.
     */
    public void setInvalidData(double invaliddata) {
        this.invaliddata = invaliddata;
    }

    /**
     * This will return true if the variable invalid data is set. If this is
     * true, there might be values in the image data that are not valid.
     * 
     * @return true if there might be invalid data in the image.
     */
    public boolean isInvalidDataSet() {
        return !Double.isNaN(invaliddata);
    }

    /**
     * Return the absolute minimum value for this image type. Based on the image
     * type this function will return the absolute minimum value a pixel can
     * have.
     * 
     * @return absolute minimum value.
     */
    public double getMinType() {
        switch (type) {
        case TYPE_BYTE:
            return ImageObjectByte.getTypeMinimum();
        case TYPE_SHORT:
            return ImageObjectShort.getTypeMinimum();
        case TYPE_USHORT:
            return ImageObjectUShort.getTypeMinimum();
        case TYPE_INT:
            return ImageObjectInt.getTypeMinimum();
        case TYPE_LONG:
            return ImageObjectLong.getTypeMinimum();
        case TYPE_FLOAT:
            return ImageObjectFloat.getTypeMinimum();
        case TYPE_DOUBLE:
            return ImageObjectDouble.getTypeMinimum();
        default:
            return -Double.MAX_VALUE;
        }
    }

    /**
     * Return the absolute maximum value for this image type. Based on the image
     * type this function will return the absolute maximum value a pixel can
     * have.
     * 
     * @return absolute maximum value.
     */
    public double getMaxType() {
        switch (type) {
        case TYPE_BYTE:
            return ImageObjectByte.getTypeMaximum();
        case TYPE_SHORT:
            return ImageObjectShort.getTypeMaximum();
        case TYPE_USHORT:
            return ImageObjectUShort.getTypeMaximum();
        case TYPE_INT:
            return ImageObjectInt.getTypeMaximum();
        case TYPE_LONG:
            return ImageObjectLong.getTypeMaximum();
        case TYPE_FLOAT:
            return ImageObjectFloat.getTypeMaximum();
        case TYPE_DOUBLE:
            return ImageObjectDouble.getTypeMaximum();
        default:
            return Double.MAX_VALUE;
        }
    }

    /**
     * Returns the minimum value in the band. This will return the minimum value
     * found in a particular band.
     * 
     * @param band
     *            of which to return minimum.
     * @return minimum value found in a band.
     */
    public double getMin(int band) {
        if (minmaxinvalid) {
            computeMinMax();
        }
        return minval[band];
    }

    /**
     * Returns the absolute minimum. This will return the minimum value in all
     * bands of the image.
     * 
     * @return minimum value in image.
     */
    public double getMin() {
        if (minmaxinvalid) {
            computeMinMax();
        }
        return minval[numbands];
    }

    /**
     * Returns the mamximum value in the band. This will return the maximum
     * value found in a particular band.
     * 
     * @param band
     *            of which to return maximum.
     * @return maximum value found in a band.
     */
    public double getMax(int band) {
        if (minmaxinvalid) {
            computeMinMax();
        }
        return maxval[band];
    }

    /**
     * Returns the absolute maximum. This will return the maximum value in all
     * bands of the image.
     * 
     * @return maximum value in image.
     */
    public double getMax() {
        if (minmaxinvalid) {
            computeMinMax();
        }
        return maxval[numbands];
    }

    /**
     * Returns wheter the minimum and maximum values have been calculated.
     * 
     * @return true if minimum and maximum have been calculated.
     */
    public boolean isMinMaxInvalid() {
        return minmaxinvalid;
    }

    /**
     * Sets a flag to inform that the minimum and maximum are invalid. If the
     * user calls getMin() or getMax() the minimum and maximum will need to be
     * recalculated.
     */
    public void setMinMaxInvalid() {
        minmaxinvalid = true;
    }

    // ------------------------------------------------------------------------
    // IMAGE MATH
    // ------------------------------------------------------------------------

    /**
     * Checks to see if any imagedata is present. If no imagedata is present
     * false is returned. This can happen if only the imageheader is loaded.
     * 
     * @return true if imagedata is present.
     * @deprecated use isHeaderOnly() (its negation that is).
     */
    @Deprecated
    public boolean isDataValid() {
        return !isHeaderOnly();
    }

    /**
     * Checks to see if any image data is present. False == no data. This can
     * happen if only the header is loaded.
     */
    public boolean isHeaderOnly() {
        return headerOnly;
    }

    /** Leaves image header in a state consistent with its data. */
    public void makeHeaderOnly() {
        computeMinMax(); // add here other header operations, if needed.
        deleteArray();
    }

    /**
     * Compares the types of the image.
     * 
     * @param image
     *            to compare against.
     * @return Will return true if the image types match.
     */
    public boolean isSameType(ImageObject image) {
        return (type == image.getType());
    }

    /**
     * Compares the number of rows and columns in the image.
     * 
     * @param image
     *            to compare against.
     * @return true if the images have the same number of columns and rows.
     */
    public boolean isSameRowCol(ImageObject image) {
        return ((numcols == image.getNumCols()) && (numrows == image.getNumRows()));
    }

    /**
     * Compares the number of rows and columns in the image.
     * 
     * @param image
     *            to compare against.
     * @return true if the images have the same number of columns and rows.
     */
    public boolean isSameRowColType(ImageObject image) {
        return ((numcols == image.getNumCols()) && (numrows == image.getNumRows()) && (type == image.getType()));
    }

    /**
     * Compares the number of rows, columns and bands in the image.
     * 
     * @param image
     *            to compare against.
     * @return true if the images have the same number of columns, rows and
     *         bands.
     */
    public boolean isSameRowColBand(ImageObject image) {
        return ((numcols == image.getNumCols()) && (numrows == image.getNumRows()) && (numbands == image.getNumBands()));
    }

    /**
     * Compares the type, number of rows, columns and bands in the image.
     * 
     * @param image
     *            to compare against.
     * @return true if the images have the same number of columns, rows and
     *         bands and are the same type.
     */
    public boolean isSame(ImageObject image) {
        return (numcols == image.getNumCols()) && (numrows == image.getNumRows()) && (numbands == image.getNumBands()) && (type == image.getType());
    }

    /**
     * @param scale
     *            the amount to scale the image by.
     * @return an in-core scaled version of the image.
     * @throws IllegalArgumentException
     * @throws ImageException
     */
    public ImageObjectInCore scaledInCore(double scale) throws IllegalArgumentException, ImageException {
        return scaledInCore(scale, scale);
    }

    /**
     * @param scalex
     *            the amount to scale the image horizontally.
     * @param scaley
     *            the amount to scale the image vertically.
     * @return an in-core scaled version of the image.
     * @throws IllegalArgumentException
     * @throws ImageException
     */
    public ImageObjectInCore scaledInCore(double scalex, double scaley) throws IllegalArgumentException, ImageException {
        boolean resultInCore = true;
        return (ImageObjectInCore) scale(scalex, scaley, resultInCore);
    }

    /**
     * Scale the image by the given amount and return a new image. This will
     * scale the image by the same amount in both directions. For more
     * information see scale( double scalex, double scaley ).
     * 
     * @param scale
     *            how much to scale the image by.
     * @return the scaled image
     * @throws IllegalArgumentException
     *             if an error occured scaling the image.
     * @throws ImageException
     *             if scale is less or equal to zero.
     */
    public ImageObject scale(double scale) throws IllegalArgumentException, ImageException {
        return scale(scale, scale);
    }

    /**
     * @param scalex
     *            the amount to scale the image horizontally.
     * @param scaley
     *            the amount to scale the image vertically.
     * @return the scaled image
     */
    public ImageObject scale(double scalex, double scaley) throws IllegalArgumentException, ImageException {
        boolean resultInCore = false;
        return scale(scalex, scaley, resultInCore);
    }

    /**
     * Scale the image in x and y and return a new image. If the scale is larger
     * than 1 it will result in image that is larger than the current image.
     * Values less than 1 will result in a smaller image.
     * 
     * @param scalex
     *            the amount to scale the image horizontally.
     * @param scaley
     *            the amount to scale the image vertically.
     * @param resultInCore
     *            true if the result should be in-core.
     * @return the scaled image
     * @throws IllegalArgumentException
     *             if an error occured scaling the image.
     * @throws ImageException
     *             if scale is less or equal to zero.
     */
    public ImageObject scale(double scalex, double scaley, boolean resultInCore) throws IllegalArgumentException, ImageException {
        if ((scalex <= 0) || (scaley <= 0)) {
            throw (new IllegalArgumentException("scale has to be larger than 0. scalex=" + scalex + ",scaley=" + scaley));
        }

        // the width and height of the new Image.
        int w = (int) (numcols * scalex);
        int h = (int) (numrows * scaley);

        ImageObject result;

        // make sure we are creating in-core if requested.
        if (resultInCore) {
            result = ImageObjectInCore.createImage(h, w, numbands, type, headerOnly);
        } else {
            result = createImage(h, w, numbands, type, headerOnly);
        }

        if (!headerOnly) {
            // form row and column indices, for speedy access.
            int[] rowIndex = new int[h];
            for (int r = 0; r < h; r++) {
                rowIndex[r] = (int) (r / scaley);
            }
            int[] colIndex = new int[w];
            for (int c = 0; c < w; c++) {
                colIndex[c] = (int) (c / scalex);
            }

            // check if both are in-cores, in which case use the original
            // method.
            if (result.isInCore() & this.isInCore()) // both are in-core.
            {
                for (int r = 0; r < h; r++) {
                    for (int c = 0; c < w; c++) {
                        ((ImageObjectInCore) result).copyAllBandsFrom(((ImageObjectInCore) this), rowIndex[r], colIndex[c], r, c);
                    }
                }
            } else // one is out-of-core
            {
                // Compute a transform from source to target.
                SubAreaTransform transform = new SubAreaTransform(this.getTileBoxes(), // all tile boxes.
                        new SubArea(0, 0, this.numcols, this.numrows), // map
                        // whole
                        // of
                        // this
                        new SubArea(0, 0, result.numcols, result.numrows)); // onto
                // whole
                // result

                // iterate over target sub-areas.
                for (SubArea target : result.getTileBoxes()) {
                    for (SubArea area : transform.getTargetTileBoxes(target)) {
                        for (int r = area.getRow(); r < area.getEndRow(); r++) {
                            for (int c = area.getCol(); c < area.getEndCol(); c++) {
                                result.copyAllBandsFrom(this, rowIndex[r], colIndex[c], r, c);
                            }
                        }
                    }
                }
            }
        }

        // copy properties
        result.setProperties(cloneProperties());
        result.setInvalidData(getInvalidData());
        return result;
    }

    /**
     * Returns a subarea of the image. This will return a new Image that is the
     * subarea of the image the user requested. If firstband in subarea is set
     * to -1, or bands in subarea is set to 0, it will return all bands,
     * otherwise it will also return a subarea of the bands.
     * 
     * @param area
     *            to be returned.
     * @return subarea of image.
     * @throws ImageException
     *             if an error occured creating image.
     */
    public ImageObject crop(SubArea area) throws ImageException {
        // sanity check
        if (area == null) {
            logger.error("ERROR: area is null in Crop method");
            return null;
        }

        // set band dimensions of the target image and offset w.r.t. this one.
        int areaNumBands;
        int areaFirstBand;
        if ((area.getNumBands() == 0) || (area.getNumBands() == numbands)) {
            areaNumBands = numbands;
            areaFirstBand = 0;
        } else {
            areaNumBands = area.getNumBands();
            areaFirstBand = area.getFirstBand();
        }

        // the new ImageObject plus its parameters.
        ImageObject result = createImage(area.height, area.width, areaNumBands, type, headerOnly);

        // copy data if needed.
        if (!headerOnly) {
            // indicates whether all bands need to be copied.
            final boolean fullBandCopy = (areaNumBands == numbands);

            // check if both are in-cores, in which case use the original
            // method.
            if (result.isInCore() & this.isInCore()) // both are in-core.
            {
                // can do it more efficiently for full-banded images.
                if (fullBandCopy) {
                    for (int r = 0; r < area.height; r++) {
                        ((ImageObjectInCore) result).copySubRowAllBandsFrom((ImageObjectInCore) this, area.y + r, area.x, area.width, r, 0);
                    }
                } else {
                    for (int r = 0; r < area.height; r++) {
                        for (int c = 0; c < area.width; c++) {
                            ((ImageObjectInCore) result).copySomeBandsFrom((ImageObjectInCore) this, area.y + r, area.x + c, r, c, areaFirstBand, areaNumBands);
                        }
                    }
                }
            } else // one is out-of-core. // no optimized (full-band) version.
            {
                // Compute a transform from source to target.
                SubAreaTransform transform = new SubAreaTransform(this.getTileBoxes(area), // tile boxes in the area.
                        area, // "area" parameter is the source area.
                        new SubArea(0, 0, result.numcols, result.numrows)); // onto
                // whole
                // result.

                // iterate over target sub-areas.
                for (SubArea target : result.getTileBoxes()) {
                    for (SubArea subArea : transform.getTargetTileBoxes(target)) {
                        for (int r = subArea.getRow(); r < subArea.getEndRow(); r++) {
                            for (int c = subArea.getCol(); c < subArea.getEndCol(); c++) {
                                if (fullBandCopy) {
                                    result.copyAllBandsFrom(this, area.y + r, area.x + c, r, c);
                                } else {
                                    result.copySomeBandsFrom(this, area.y + r, area.x + c, r, c, areaFirstBand, areaNumBands);
                                }
                            }
                        }
                    }
                }

            }
        }

        // copy properties
        result.setProperties(cloneProperties());
        result.setInvalidData(getInvalidData());
        return result;
    }

    /**
     * Copy the data from all the images into a single image. The images are
     * assumed to be aligned at the 0,0 point. It will create a new image that
     * will fit the largest of all images with all the bands of all images.
     * 
     * @param images
     *            of whom to combine the bands.
     * @return the combined bands of all the images passed in.
     * @throws ImageException
     *             if the images are not the same size and type.
     */
    static public ImageObject add(ImageObject[] images) throws ImageException {
        // check simple case.
        if (images == null) {
            return null;
        }

        // check simple case.
        final int len = images.length;
        if (len == 0) {
            return null;
        }

        // check simple case.
        if (len == 1) {
            try {
                return (ImageObject) images[0].clone();
            } catch (CloneNotSupportedException exc) {
                throw (new ImageException(exc));
            }
        }

        int i, r, c, t, b1;
        int w = images[0].getNumCols();
        int h = images[0].getNumRows();
        int b = images[0].getNumBands();
        int type = images[0].getType();
        boolean resultHeaderOnly = true;
        boolean imagesInCore = true;

        // compute number of bands, type, number of cols, rows, and headerOnly
        // of target image.
        for (i = 1; i < len; i++) {
            b += images[i].getNumBands();
            if (images[i].getType() > type) {
                type = images[i].getType();
            }
            if (images[i].getNumCols() > w) {
                w = images[i].getNumCols();
            }
            if (images[i].getNumRows() > h) {
                h = images[i].getNumRows();
            }
            if (!images[i].isHeaderOnly()) {
                resultHeaderOnly = false;
            }
            if (images[i].isOutOfCore()) {
                imagesInCore = false;
            }
        }

        // create result image
        ImageObject result = createImage(h, w, b, type, resultHeaderOnly);

        // copy all the data from one set of bands to another set of bands.

        // check if the result and components are in-core.
        if (result.isInCore() & imagesInCore) {
            for (b = 0, i = 0; i < len; i++) {
                if (images[i].isHeaderOnly()) {
                    b += images[i].getNumBands();
                    continue;
                }
                for (t = 0; t < images[i].getNumBands(); t++, b++) {
                    for (r = 0; r < images[i].getNumRows(); r++) {
                        for (c = 0; c < images[i].getNumCols(); c++) {
                            result.set(r, c, b, images[i].getDouble(r, c, t));
                        }
                    }
                }
            }
        } else // one of them is out-of-core; use the iterative patterns.
        {
            for (b = 0, i = 0; i < len; i++, b += images[i].getNumBands()) {
                // don't copy data from header-only images.
                if (images[i].isHeaderOnly()) {
                    continue;
                }

                // form the area corresponding to the whole of images[i].
                SubArea resultArea = new SubArea(0, 0, images[i].numcols, images[i].numrows);

                // iterate over target sub-areas.
                SubAreaTransform transform = new SubAreaTransform(images[i].getTileBoxes(), // all image tile boxes
                        resultArea, resultArea); // same areas in source and
                // in result
                for (SubArea target : result.getTileBoxes(resultArea)) {
                    // only those falling in
                    for (SubArea area : transform.getTargetTileBoxes(target)) {
                        for (r = area.getRow(); r < area.getEndRow(); r++) {
                            for (c = area.getCol(); c < area.getEndCol(); c++) {
                                for (t = 0, b1 = b; t < images[i].getNumBands(); t++, b1++) {
                                    result.set(r, c, b1, images[i].getDouble(r, c, t));
                                }
                            }
                        }
                    }
                }

            }
        }

        return result;
    }

    /**
     * Return an image with the single band requested. For more information see
     * extractBand(int[]).
     * 
     * @param band
     *            the band to be extracted.
     * @return an image with a single band.
     * @throws ImageException
     *             if the band requested is less than 0 or larger that the
     *             number of bands in the image.
     */
    public ImageObject extractBand(int band) throws ImageException {
        return extractBand(new int[] { band });
    }

    /**
     * Create a new image with only those bands that are selected. This will go
     * through the image and create a new image with the same number of rows and
     * numcols and same type, but with only those bands that are selected. It
     * will not copy the properties of the image.
     * 
     * @param band
     *            the bands to be extracted.
     * @return an image with only those bands that were selected.
     * @throws ImageException
     *             if the band requested is less than 0 or larger that the
     *             number of bands in the image.
     */
    public ImageObject extractBand(int[] band) throws ImageException {
        int bl = band.length;

        // check band contents.
        for (int i = 0; i < bl; i++) {
            if ((band[i] < 0) || (band[i] >= numbands)) {
                throw (new ImageException("Not enough bands in image."));
            }
        }

        // target image.
        ImageObject result = createImage(numrows, numcols, bl, type, headerOnly);

        // fill in the data if needed.
        if (!headerOnly) {
            if (this.isInCore() & result.isInCore()) // if both are in-core,
            // use the old code.
            {
                for (int r = 0; r < numrows; r++) {
                    for (int c = 0; c < numcols; c++) {
                        for (int k = 0; k < bl; k++) {
                            result.set(r, c, k, getDouble(r, c, band[k]));
                        }
                    }
                }

            } else // use iterative pattern.
            {
                // form the area corresponding to the whole, same for origin and
                // result.
                SubArea resultArea = new SubArea(0, 0, numcols, numrows);

                // iterate over target sub-areas.
                SubAreaTransform transform = new SubAreaTransform(this.getTileBoxes(), resultArea, resultArea); // take all
                // everywhere.
                for (SubArea target : result.getTileBoxes()) {
                    for (SubArea area : transform.getTargetTileBoxes(target)) {
                        for (int r = area.getRow(); r < area.getEndRow(); r++) {
                            for (int c = area.getCol(); c < area.getEndCol(); c++) {
                                for (int k = 0; k < bl; k++) {
                                    result.set(r, c, k, getDouble(r, c, band[k]));
                                }
                            }
                        }
                    }
                }
            }
        }

        // copy properties
        result.setProperties(cloneProperties());
        result.setInvalidData(getInvalidData());
        return result;
    }

    /**
     * Insert img into this ImageObject. The previous values for this
     * ImageObject will be overwritten. If img is too large to be inserted, an
     * exception will be thrown. Crop img and try again. ImageException will be
     * thrown if img has a different number of bands. The sample types must be
     * the same.
     * 
     * @param img
     *            the image to insert
     * @param insRow
     *            the row of insertion
     * @param insCol
     *            the column of insertion
     * @throws ImageException
     *             when the bounds of img are too large to be inserted into this
     *             image or when the number of bands do not match
     */
    public void insert(ImageObject img, int insRow, int insCol) throws ImageException {
        // we want to overwrite the old values with the new values

        // do the number of bands match?
        if (this.getNumBands() != img.getNumBands()) {
            throw new ImageException("Can't insert an image with " + img.getNumBands() + " bands into an image with " + getNumBands() + " bands.");
        }

        // will img fit into this image?
        if ((insRow + img.getNumRows()) > getNumRows()) {
            throw new ImageException("Can't insert an image with " + img.getNumRows() + " rows into an image with " + getNumRows() + " rows.");
        }

        // will img fit into this image?
        if ((insCol + img.getNumCols()) > getNumCols()) {
            throw new ImageException("Can't insert an image with " + img.getNumCols() + " columns into an image with " + getNumCols() + " columns.");
        }

        // make sure images are the same type
        if (img.getType() != getType()) {
            throw new ImageException("Can't insert an image of type " + types[img.getType()] + " into an image of type " + types[getType()] + ".");
        }

        // make sure the image being inserted is not header-only.
        if (img.isHeaderOnly()) {
            return;
        }

        // passed all the tests, do the insert

        final int numRowsImg = img.getNumRows();
        final int numColsImg = img.getNumCols();

        if (this.isInCore() & img.isInCore()) // if both in-core, use old
        // code.
        {
            for (int row = 0; row < numRowsImg; row++) {
                ((ImageObjectInCore) this).copySubRowAllBandsFrom((ImageObjectInCore) img, row, 0, numColsImg, insRow + row, insCol);
            }
        } else // otherwise, use iterative pattern.
        { // note that in this case, "this" is the target and "img" is the
            // source.
            SubAreaTransform transform = new SubAreaTransform(img.getTileBoxes(), // all of img.
                    new SubArea(0, 0, numColsImg, numRowsImg), // source area.
                    new SubArea(insCol, insRow, numColsImg, numRowsImg)); // target
            // area.
            for (SubArea target : this.getTileBoxes()) {
                for (SubArea area : transform.getTargetTileBoxes(target)) {
                    for (int r = area.getRow(); r < area.getEndRow(); r++) {
                        for (int c = area.getCol(); c < area.getEndCol(); c++) {
                            this.copyAllBandsFrom(img, r, c, r + insRow, c + insCol);
                        }
                    }
                }
            }
        }
    }

    /**
     * Copying all data from another ImageObject into this one.
     */
    protected void copyAllDataFrom(ImageObject other) {
        // compute a transform that maps the whole of other on the whole of
        // this.
        SubArea allArea = new SubArea(0, 0, other.numcols, other.numrows);
        SubAreaTransform transform = new SubAreaTransform( // take the whole
                other.getTileBoxes(), allArea, allArea); // of both images.

        // iterate over the tiles of this and other.
        for (SubArea target : this.getTileBoxes()) {
            for (SubArea area : transform.getTargetTileBoxes(target)) {
                for (int r = area.getRow(); r < area.getEndRow(); r++) {
                    for (int c = area.getCol(); c < area.getEndCol(); c++) {
                        this.copyAllBandsFrom(other, r, c, r, c);
                    }
                }
            }
        }
    }

    /**
     * Copies all bands from another ImageObject into this one at specified rows
     * and columns. Works for generic ImageObjects; inefficient for
     * ImageObjectInCore's.
     * 
     * @param other
     *            ImageObject to copy all bands from.
     * @param otherRow
     *            the row in another ImageObject.
     * @param otherCol
     *            the col in another ImageObject.
     * @param row
     *            the row in this ImageObject.
     * @param col
     *            the col in this ImageObject.
     */
    protected void copyAllBandsFrom(ImageObject other, int otherRow, int otherCol, int row, int col) {
        try {
            for (int band = 0; band < other.numbands; band++) {
                this.set(row, col, band, other.getDouble(otherRow, otherCol, band));
            }
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    /**
     * Copies some bands from another ImageObject into this one at specified
     * rows and columns.
     * 
     * @param other
     *            ImageObject to copy all bands from.
     * @param otherRow
     *            the row in another ImageObject.
     * @param otherCol
     *            the col in another ImageObject.
     * @param row
     *            the row in this ImageObject.
     * @param col
     *            the col in this ImageObject.
     */
    protected void copySomeBandsFrom(ImageObject other, int otherRow, int otherCol, int row, int col, int firstBand, int numBands) {
        for (int band = 0; band < numBands; band++) {
            this.set(row, col, band, other.getDouble(otherRow, otherCol, band + firstBand));
        }
    }

    /**
     * @param area
     *            the area with which to compute the intersection.
     * @return a list of non-empty intersections of raster's SubArea tiles twith
     *         the area.
     */
    public ArrayList<SubArea> getTileBoxes(SubArea area) {
        return SubAreaTransform.getTileBoxesArea(this.getTileBoxes(), area);
    }

    /**
     * Insert img into this ImageObject. If the value to be inserted is not no
     * data value, then the previous values for this ImageObject will be
     * overwritten. If img is too large to be inserted, an exception will be
     * thrown. Crop img and try again. ImageException will be thrown if img has
     * a different number of bands. The sample types must be the same.
     * 
     * @param img
     *            the image to insert
     * @param r
     *            the row
     * @param c
     *            the column
     * @throws ImageException
     *             when the bounds of img are too large to be inserted into this
     *             image or when the number of bands do not match
     * @deprecated use insert() instead
     */
    @Deprecated
    public void mosaicInsert(ImageObject img, int r, int c) throws ImageException {
        insert(img, r, c);
    }

    /**
     * Convert image from one type to another. This will return a new image of
     * the requested type with information from the original image. This could
     * result in information loss since some types can store less information
     * than others, for instance converting from short to byte will result in
     * information loss since short can be between -32768 and 32767 and byte
     * only between 0 and 255. If the values in the old image do indeed fall
     * outside the range of the new image, the data will be scaled such that it
     * fills the full range of the new image type. Converting up, form byte to
     * short for example, will not result in information loss. The properties of
     * the image are copied as well.
     * 
     * @param type
     *            of the new image.
     * @param absolute
     *            is true if the image should be scaled to fit new datatype
     *            according to absolute minimum and maximum values in image.
     * @return a new image with all information of the old image but converted
     *         to a new type.
     * @throws ImageException
     * 
     *             This need to be modified for out-of-core ImageObjects, as the
     *             iteration pattern will cause excessive number of page faults.
     */
    public ImageObject convert(int type, boolean absolute) throws ImageException {
        // if images are the same just return a clone.
        if (type == this.type) {
            try {
                return (ImageObject) clone();
            } catch (CloneNotSupportedException exc) {
                throw (new ImageException(exc));
            }
        }

        // try and find the best type
        if (type >= TYPE_UNKNOWN) {
            double min = getMin();
            double max = getMax();

            if ((min >= ImageObjectByte.getTypeMinimum()) && (max <= ImageObjectByte.getTypeMaximum())) {
                type = TYPE_BYTE;
            } else if ((min >= ImageObjectShort.getTypeMinimum()) && (max <= ImageObjectShort.getTypeMaximum())) {
                type = TYPE_SHORT;
            } else if ((min >= ImageObjectUShort.getTypeMinimum()) && (max <= ImageObjectUShort.getTypeMaximum())) {
                type = TYPE_USHORT;
            } else if ((min >= ImageObjectInt.getTypeMinimum()) && (max <= ImageObjectInt.getTypeMaximum())) {
                type = TYPE_INT;
            } else if ((min >= ImageObjectLong.getTypeMinimum()) && (max <= ImageObjectLong.getTypeMaximum())) {
                type = TYPE_LONG;
            } else if ((min >= ImageObjectFloat.getTypeMinimum()) && (max <= ImageObjectFloat.getTypeMaximum())) {
                type = TYPE_FLOAT;
            } else {
                type = TYPE_DOUBLE;
            }
        }

        ImageObject result = createImage(numrows, numcols, numbands, type, headerOnly);
        result.properties = cloneProperties();
        result.setInvalidData(getInvalidData());

        if (!headerOnly) {
            // if min and max of image don't fit in type, scale.
            // can scale using the absolute min/max or band min/max
            if ((type == TYPE_BYTE) || (getMin() < result.getTypeMin()) || (getMax() > result.getTypeMax())) {
                if (absolute) {
                    int i, j;
                    double scale = (result.getTypeMax() - result.getTypeMin()) / (getMax() - getMin());
                    double step = result.getTypeMin() - getMin();
                    for (i = 0, j = 0; j < size; i++, j++) {
                        result.set(i, (getDouble(j) + step) * scale);
                    }
                } else {
                    int i, j;
                    double[] scale = new double[numbands];
                    double[] step = new double[numbands];
                    for (j = 0; j < numbands; j++) {
                        scale[j] = (result.getTypeMax() - result.getTypeMin()) / (getMax(j) - getMin(j));
                        step[j] = result.getTypeMin() - getMin(j);
                    }
                    for (i = 0, j = 0; j < size; i++, j++) {
                        result.set(i, (getDouble(j) + step[j % numbands]) * scale[j % numbands]);
                    }
                }
            } else {
                int i, j;
                for (i = 0, j = 0; j < size; i++, j++) {
                    result.set(i, getDouble(j));
                }
            }
        }
        return result;
    }

    /**
     * Return a string describing the image. This will return the size and type
     * of the image.
     * 
     * @return size and type of image as string.
     */
    @Override
    public String toString() {
        return "(numrows=" + numrows + ", numcols= " + numcols + ", numbands=" + numbands + ", types=" + types[type] + ")";
    }

    /**
     * Return a string with values of one row in the image.
     * 
     * @return image values in a BIP order of one row as a string.
     */
    public String toStringRow(int row) {
        if (headerOnly) {
            return "Error: no data";
        }
        if (row < 0 || row >= numrows) {
            return "Error: row=" + row + " is out of bounds [" + 0 + "," + numrows + ")";
        } else {
            String str = new String("(row=" + row + ",col,band)=val\n");
            int temp = numcols * numbands;
            int col, band, index = row * temp;
            for (col = 0; col < numcols; col++) {
                for (band = 0; band < numbands; band++) {
                    str += "(" + row + "," + col + "," + band + ")=" + this.getDouble(index) + "; ";
                    index++;
                }
                str += "\n";
            }
            str += "\n";
            return (str);
        }
    }

    /**
     * Return a string with values of one column in the image.
     * 
     * @return image values in a BIP order of one column as a string.
     */
    public String toStringCol(int col) {
        if (headerOnly) {
            return "Error: no data";
        }
        if (col < 0 || col >= numcols) {
            return "Error: col=" + col + " is out of bounds [" + 0 + "," + numcols + ")";
        } else {
            String str = new String("(row,col=" + col + ",band)=val\n");
            int temp = numcols * numbands;
            int row, band, index = col * numbands;
            for (row = 0; row < numrows; row++) {
                for (band = 0; band < numbands && index < this.size; band++) {
                    str += "(" + row + "," + col + "," + band + ")=" + this.getDouble(index) + "; ";
                    index++;
                }
                index = index - numbands + temp;
                str += "\n";
            }
            str += "\n";
            return (str);
        }
    }

    /**
     * Return a string with all values in the image.
     * 
     * @return all image values in a BIP order as a string.
     */
    public String toStringAll() {
        if (headerOnly) {
            return "Error: no data";
        }
        String str = new String("(row,col,band)=val\n");
        int row, col, band, index = 0;
        for (row = 0; row < numrows; row++) {
            for (col = 0; col < numcols; col++) {
                for (band = 0; band < numbands; band++) {
                    str += "(" + row + "," + col + "," + band + ")=" + this.getDouble(index) + "; ";
                    index++;
                }
                str += "\n";
            }
            str += "\n-------------------\n";
        }
        str += "\n";
        return (str);
    }

    // ------------------------------------------------------------------------
    // Convert Image to ImageObject
    // ------------------------------------------------------------------------
    /**
     * Converts a single band ImageObject to a 3 band ImageObject. This function
     * can be used to take an int image to a 3 band byte image.
     * 
     * @param image
     *            the image to be converted.
     * @return the converted ImageObject.
     * @throws ImageException
     *             if an error occurred during the conversion.
     */
    static public ImageObject intToByte(ImageObject image) throws ImageException {
        if (image.getNumBands() != 1) {
            throw (new ImageException("Expected an image with a single band."));
        }
        ImageObject result = new ImageObjectByte(image.getNumRows(), image.getNumCols(), 3);
        for (int j = 0, i = 0; i < image.getSize(); i++) {
            int val = image.getInt(i);
            result.set(j++, (byte) ((val >> 16) & 0xff));
            result.set(j++, (byte) ((val >> 8) & 0xff));
            result.set(j++, (byte) ((val) & 0xff));
        }
        return result;
    }

    /**
     * Converts an Image to an ImageObject. This function will take an Image and
     * convert it to an ImageObject.
     * 
     * @param image
     *            the image to be converted.
     * @return the converted ImageObject.
     * @throws ImageException
     *             if an error occurred during the conversion.
     */
    static public ImageObject getImageObject(Image image) throws ImageException {
        return getImageObject(image, false);
    }

    /**
     * Converts an Image to an ImageObject. This function will take an Image and
     * convert it to an ImageObject.
     * 
     * @param image
     *            the image to be converted.
     * @param intimage
     *            set to false if the returned image should be byte (3 bands)
     *            and true if the returned image should be int (1 band)
     * @return the converted ImageObject.
     * @throws ImageException
     *             if an error occurred during the conversion.
     */
    static public ImageObjectInCore getImageObject(Image image, boolean intimage) throws ImageException {
        int w = image.getWidth(null);
        int h = image.getHeight(null);
        int size = w * h;
        int pixels[] = new int[size];
        PixelGrabber pg = new PixelGrabber(image, 0, 0, w, h, pixels, 0, w);

        try {
            pg.grabPixels();
        } catch (InterruptedException exc) {
            throw new ImageException(exc);
        }

        if ((pg.getStatus() & ImageObserver.ABORT) != 0) {
            throw (new ImageException("PixelGrab was aborted."));
        }

        ImageObjectInCore imgobj;
        if (intimage) {
            imgobj = new ImageObjectInt(h, w, 1);
            imgobj.setData(pixels);
        } else {
            imgobj = new ImageObjectByte(h, w, 3);
            byte[] data = (byte[]) imgobj.getData(); // imgobj is of in-core
            // type ImageObjectByte!

            int i, j;
            for (j = 0, i = 0; i < size; i++) {
                data[j++] = (byte) ((pixels[i] >> 16) & 0xff);
                data[j++] = (byte) ((pixels[i] >> 8) & 0xff);
                data[j++] = (byte) ((pixels[i]) & 0xff);
            }
        }

        return imgobj;
    }

    // ------------------------------------------------------------------------
    // Convert ImageObject to a BufferedImage
    // ------------------------------------------------------------------------
    /**
     * Convert this imageobject to a buffered image that can be rendered. This
     * routine will create a RGB image with bands 0, 1 and 2 selected for RGB.
     * This BufferedImage can then be used to render the image using standard
     * java.
     * <p/>
     * 
     * @return a bufferedimage that contains the imageobject.
     */
    public BufferedImage makeBufferedImage() {
        int[] rgb = (int[]) getProperty(DEFAULT_RGB);
        if (rgb == null) {
            rgb = new int[] { 0, 1, 2 };
        }
        return toBufferedImage(null, false, null, false, rgb[0], rgb[1], rgb[2], false, 0, null, 255, 1.0);
    }

    /**
     * Convert this imageobject to a buffered image that can be rendered. This
     * routine will create a grayscale image with the given band.
     * <p/>
     * 
     * @param grayband
     *            which band in the image is the blueband, if set to -1 no
     *            blueband is used.
     * @return a bufferedimage that contains the imageobject.
     */
    public BufferedImage makeBufferedImage(int grayband) {
        return toBufferedImage(null, false, null, false, 0, 0, 0, true, grayband, null, 255, 1.0);
    }

    /**
     * Convert this imageobject to a buffered image that can be rendered. This
     * routine will create a RGB image with the given bands.
     * <p/>
     * 
     * @param redband
     *            which band in the image is the redband, if set to -1 no
     *            redband is used.
     * @param greenband
     *            which band in the image is the greenband, if set to -1 no
     *            greenband is used.
     * @param blueband
     *            which band in the image is the blueband, if set to -1 no
     *            blueband is used.
     * @return a bufferedimage that contains the imageobject.
     */
    public BufferedImage makeBufferedImage(int redband, int greenband, int blueband) {
        return toBufferedImage(null, false, null, false, redband, greenband, blueband, false, 0, null, 255, 1.0);
    }

    public BufferedImage toBufferedImage(BufferedImage bi, boolean fakergb, double[] scale, boolean usetotals, int redband, int greenband, int blueband, boolean grayscale, int grayband,
            int[] gammatable, double alpha) {
        return toBufferedImage(bi, fakergb, scale, usetotals, redband, greenband, blueband, grayscale, grayband, gammatable, alpha, 1.0);
    }

    // //////////////////////

    public void setImageObjectValue(double val) {
        for (int i = 0; i < size; i++) {
            set(i, val);
        }
    }

    public void setImageObjectValue(int val) {
        for (int i = 0; i < size; i++) {
            setInt(i, val);
        }
    }

    // TODO potential bug
    /**
     * @param row
     *            of the pixel to check the bands at.
     * @param col
     *            of the pixel to check the bands at.
     * @return true if values in all bands are valid.
     */
    public boolean allBandsValid(int row, int col) {
        for (int band = 0; band < numbands; band++) {
            double value = getDouble(row, col, band);
            if ((value == invaliddata) | (value == -invaliddata) | (value == 0)) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param row
     *            of the pixel to check the bands at.
     * @param col
     *            of the pixel to check the bands at.
     * @return true if values in some bands are valid (the value in at least one
     *         band is valid).
     */
    public boolean someBandsValid(int row, int col) {
        for (int band = 0; band < numbands; band++) {
            double value = getDouble(row, col, band);
            if ((value != invaliddata) & (value != -invaliddata) & (value != 0)) {
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------------------------
    // IMAGE TYPE SPECIFIC
    // ------------------------------------------------------------------------

    public abstract BufferedImage toBufferedImage(BufferedImage bi, boolean fakergb, double[] scale, boolean usetotals, int redband, int greenband, int blueband, boolean grayscale, int grayband,
            int[] gammatable, double alpha, double imagescale);

    /**
     * @return a list of SubArea tiles representing the object (single tile for
     *         InCore's and multiple tiles for OutOfCore's).
     */
    public abstract ArrayList<SubArea> getTileBoxes();

    /**
     * @param area
     *            to check for valid data.
     * @return true if there is valid data inside the area.
     */
    public abstract boolean hasValidData(SubArea area);

    public abstract void computeMinMax();

    /**
     * @return true if the object is out-of-core; false otherwise.
     */
    public abstract boolean isOutOfCore();

    public abstract void createArray();

    public abstract void deleteArray();

    public abstract Object getData();

    public abstract void setData(Object data);

    public abstract void setData(double defaultval);

    public abstract double getTypeMin();

    public abstract double getTypeMax();

    public abstract byte getByte(int i);

    public abstract byte getByte(int r, int c, int b);

    public abstract void setByte(int i, byte v);

    public abstract void setByte(int r, int c, int b, byte v);

    public abstract void set(int i, byte v);

    public abstract void set(int r, int c, int b, byte v);

    public abstract short getShort(int i);

    public abstract short getShort(int r, int c, int b);

    public abstract void setShort(int i, short v);

    public abstract void setShort(int r, int c, int b, short v);

    public abstract void set(int i, short v);

    public abstract void set(int r, int c, int b, short v);

    public abstract int getInt(int i);

    public abstract int getInt(int r, int c, int b);

    public abstract void setInt(int i, int v);

    public abstract void setInt(int r, int c, int b, int v);

    public abstract void set(int i, int v);

    public abstract void set(int r, int c, int b, int v);

    public abstract long getLong(int i);

    public abstract long getLong(int r, int c, int b);

    public abstract void setLong(int i, long v);

    public abstract void setLong(int r, int c, int b, long v);

    public abstract void set(int i, long v);

    public abstract void set(int r, int c, int b, long v);

    public abstract float getFloat(int i);

    public abstract float getFloat(int r, int c, int b);

    public abstract void setFloat(int i, float v);

    public abstract void setFloat(int r, int c, int b, float v);

    public abstract void set(int i, float v);

    public abstract void set(int r, int c, int b, float v);

    public abstract double getDouble(int i);

    public abstract double getDouble(int r, int c, int b);

    public abstract void setDouble(int i, double v);

    public abstract void setDouble(int r, int c, int b, double v);

    public abstract void set(int i, double v);

    public abstract void set(int r, int c, int b, double v);
}
