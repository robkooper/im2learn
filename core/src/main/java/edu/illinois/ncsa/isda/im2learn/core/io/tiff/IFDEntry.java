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
package edu.illinois.ncsa.isda.im2learn.core.io.tiff;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class is a single Image File Directory entry. The TIFF image uses this
 * to store information about the image.
 */
class IFDEntry {
    // All the types in TIFF v6
    static final int     TYPE_BYTE                     = 1;
    static final int     TYPE_ASCII                    = 2;
    static final int     TYPE_SHORT                    = 3;
    static final int     TYPE_LONG                     = 4;
    static final int     TYPE_RATIONAL                 = 5;
    static final int     TYPE_SBYTE                    = 6;
    static final int     TYPE_UNDEFINED                = 7;
    static final int     TYPE_SSHORT                   = 8;
    static final int     TYPE_SLONG                    = 9;
    static final int     TYPE_SRATIONAL                = 10;
    static final int     TYPE_FLOAT                    = 11;
    static final int     TYPE_DOUBLE                   = 12;
    static final int     TYPE_LONG8                    = 16;                               // bigtiff
    static final int     TYPE_SLONG8                   = 17;                               // bigtiff
    static final int     TYPE_IFD8                     = 18;                               // bigtiff

    // List of tags currently recognized
    static final int     TAG_NewSubfileType            = 254;
    static final int     TAG_SubfileType               = 255;
    static final int     TAG_ImageWidth                = 256;
    static final int     TAG_ImageLength               = 257;
    static final int     TAG_BitsPerSample             = 258;
    static final int     TAG_Compression               = 259;
    static final int     TAG_PhotometricInterpretation = 262;
    static final int     TAG_DocumentName              = 269;
    static final int     TAG_ImageDescription          = 270;
    static final int     TAG_StripOffsets              = 273;
    static final int     TAG_SamplesPerPixel           = 277;
    static final int     TAG_RowsPerStrip              = 278;
    static final int     TAG_StripByteCounts           = 279;
    static final int     TAG_MinSampleValue            = 280;
    static final int     TAG_MaxSampleValue            = 281;
    static final int     TAG_XResolution               = 282;
    static final int     TAG_YResolution               = 283;
    static final int     TAG_PlanarConfiguration       = 284;
    static final int     TAG_ResolutionUnit            = 296;
    static final int     TAG_Software                  = 305;
    static final int     TAG_Predictor                 = 317;
    static final int     TAG_ColorMap                  = 320;
    static final int     TAG_TileWidth                 = 322;
    static final int     TAG_TileLength                = 323;
    static final int     TAG_TileOffsets               = 324;
    static final int     TAG_TileByteCounts            = 325;
    static final int     TAG_SubIFD                    = 330;
    static final int     TAG_SampleFormat              = 339;

    // following the TIFF 6.0 specification, these tags are allocated for
    // specific purposes
    // An organization might wish to store information meaningful to only that
    // organization in a TIFF file. Tags numbered 32768 or higher, sometimes
    // called private
    // tags, are reserved for that purpose.
    // private tag GDAL_NODATA
    static final int     TAG_GDALNoData                = 42113;
    // private tag IM2LEARN for storing image properties
    static final int     TAG_ImageMetadata             = 42114;

    // Special GeoTiff tags
    static final int     TAG_ModelPixelScale           = 33550;
    // static final int TAG_IntergraphMatrix = 33920;
    static final int     TAG_ModelTiepoint             = 33922;
    static final int     TAG_GeoKeyDirectory           = 34735;
    static final int     TAG_GeoDoubleParams           = 34736;
    static final int     TAG_GeoAsciiParams            = 34737;

    // Each tag has 12 bytes of storage
    private final byte[] hdr                           = new byte[8];
    // The tag number
    private final int    tag;
    // Type of the data
    private final int    type;
    // The number of entries in this tag
    private final long   entries;
    // Offset to the entries in the file, or if fits the entries itself.
    private final long   offset;

    // The TIFFImage this entry belongs to.
    private TIFFimage    tiffimage                     = null;

    private static Log   logger                        = LogFactory.getLog(IFDEntry.class);

    /**
     * Create an IFD entry based on the header info.
     * 
     * @param hdr
     *            contains the 12 bytes of the entry
     * @param offset
     *            where in header the 12 bytes are located.
     * @param tiffimage
     *            the tiffimage this entry belongs to.
     */
    public IFDEntry(byte[] hdr, int offset, TIFFimage tiffimage) {
        this.tiffimage = tiffimage;
        this.tag = tiffimage.getShort(hdr, offset);
        this.type = tiffimage.getShort(hdr, offset + 2);
        if (tiffimage.isBigTiff()) {
            this.entries = tiffimage.getLong8(hdr, offset + 4);
            this.offset = tiffimage.getLong8(hdr, offset + 12);
            System.arraycopy(hdr, offset + 12, this.hdr, 0, 8);
        } else {
            this.entries = tiffimage.getLong(hdr, offset + 4);
            this.offset = tiffimage.getLong(hdr, offset + 8);
            System.arraycopy(hdr, offset + 8, this.hdr, 0, 4);
        }
    }

    /**
     * Return the tag of the IFD.
     * 
     * @return the tag of this IFD.
     */
    public int getTag() {
        return tag;
    }

    /**
     * Checks to see if this is a specific tag.
     * 
     * @param check
     *            is the tag expected.
     * @return true if this tag matches check.
     */
    public boolean isTag(int check) {
        return (tag == check);
    }

    /**
     * Returns the type of entries in the IFD.
     * 
     * @return type of entries in IFD.
     */
    public int getType() {
        return type;
    }

    /**
     * Checks to see if this is a specific type.
     * 
     * @param check
     *            is the type expected.
     * @return true if this type matches check.
     */
    public boolean isType(int check) {
        return (type == check);
    }

    /**
     * Return the number of values found with this tag.
     * 
     * @return number of entries.
     */
    public long getEntries() {
        return entries;
    }

    /**
     * Will return a single long or short value, depending on type.
     * 
     * @return a single value
     * @throws IOException
     *             if not long or short type, or more than 1 entry.
     */
    public long getLongShortValue() throws IOException {
        if (entries != 1) {
            throw (new IOException("Only expected 1 entry."));
        }
        if (type == TYPE_LONG) {
            return tiffimage.getLong(hdr, 0);
        } else if (type == TYPE_SHORT) {
            return tiffimage.getShort(hdr, 0);
        } else if ((type == TYPE_LONG8) && tiffimage.isBigTiff()) {
            return tiffimage.getLong8(hdr, 0);
        } else {
            throw (new IOException("Can't handle type " + type + " for tag " + tag + "."));
        }
    }

    /**
     * Will return an array of long or short values depending on type, in case
     * of short, the array will have been converted to longs.
     * 
     * @return array with entries.
     * @throws IOException
     *             if not long or short type or error reading data.
     */
    public long[] getLongShortValues() throws IOException {
        if (type == TYPE_LONG) {
            return getUnsignedLongValues();
        } else if (type == TYPE_SHORT) {
            int[] tmp = getUnsignedShortValues();
            long[] result = new long[(int) entries];
            for (int i = 0; i < entries; i++) {
                result[i] = tmp[i];
            }
            return result;
        } else if ((type == TYPE_LONG8) && tiffimage.isBigTiff()) {
            return getLong8Values();
        } else {
            throw (new IOException("Can't handle type " + type + " for tag " + tag + "."));
        }
    }

    /**
     * Will return the String that was read from the file.
     * 
     * @return the string that was read.
     * @throws IOException
     *             if not ascii type or error reading data.
     */
    public String getString() throws IOException {
        // TODO this can contain multiple strings seperated by \0
        if (this.type != TYPE_ASCII) {
            throw (new IOException("Can't handle type " + type + " for tag " + tag + "."));
        }
        if (entries == 0) {
            return "";
        }

        // TODO 3 ascii char stored in entries, or still offset?
        // jump to offset in file
        tiffimage.getFile().seek(offset);

        // read the data from the file
        byte[] b = new byte[(int) entries];
        if (tiffimage.getFile().read(b) != entries) {
            throw (new IOException("Could not read enough bytes."));
        }

        // last byte has to be a 0
        if (b[(int) entries - 1] != 0) {
            logger.debug("Last byte in string should be 0.");
            return new String(b);
        }

        // return a string
        return new String(b, 0, (int) entries - 1);
    }

    /**
     * Will return the byte array that was read from the file. This method was
     * added to support image properties
     * 
     * @return the byte array that was read.
     * @throws IOException
     *             if not byte array type or error reading data.
     */
    public byte[] getArrayByte() throws IOException {
        if (this.type != TYPE_BYTE) {
            throw (new IOException("Can't handle type " + type + " for tag " + tag + "."));
        }
        if (entries == 0) {
            return null;
        }

        // TODO 3 ascii char stored in entries, or still offset?
        // jump to offset in file
        tiffimage.getFile().seek(offset);

        // read the data from the file
        byte[] b = new byte[(int) entries];
        if (tiffimage.getFile().read(b) != entries) {
            throw (new IOException("Could not read enough bytes."));
        }

        // last byte has to be a zero
        if (b[(int) entries - 1] != 0) {
            logger.debug("Last byte in string should be 0 but it is =" + b[(int) entries - 1]);
            return b;
        }

        // return a byte array
        return b;// new String(b, 0, (int) entries - 1);
    }

    /**
     * Will return a single short value.
     * 
     * @return a single value
     * @throws IOException
     *             if not short type, or more than 1 entry.
     */
    public int getUnsignedShortValue() throws IOException {
        // if (entries != 1) {
        if (entries < 1) {
            throw (new IOException("Only expected 1 entry."));
        }
        if (type == TYPE_SHORT) {
            return tiffimage.getShort(hdr, 0);
        } else {
            throw (new IOException("Can't handle type " + type + " for tag " + tag + "."));
        }
    }

    /**
     * Will return an array of short values.
     * 
     * @return array with entries.
     * @throws IOException
     *             if not short type or error reading data.
     */
    public int[] getUnsignedShortValues() throws IOException {
        if (this.type != TYPE_SHORT) {
            throw (new IOException("Can't handle type " + type + " for tag " + tag + "."));
        }

        int[] s = new int[(int) entries];

        // see if it is stored in the offset
        if (entries == 1) {
            s[0] = tiffimage.getShort(hdr, 0);
            return s;
        }
        if (entries == 2) {
            s[0] = tiffimage.getShort(hdr, 0);
            s[1] = tiffimage.getShort(hdr, 2);
            return s;
        }
        if ((entries == 3) && tiffimage.isBigTiff()) {
            s[0] = tiffimage.getShort(hdr, 0);
            s[1] = tiffimage.getShort(hdr, 2);
            s[2] = tiffimage.getShort(hdr, 4);
            return s;
        }
        if ((entries == 4) && tiffimage.isBigTiff()) {
            s[0] = tiffimage.getShort(hdr, 0);
            s[1] = tiffimage.getShort(hdr, 2);
            s[2] = tiffimage.getShort(hdr, 4);
            s[3] = tiffimage.getShort(hdr, 6);
            return s;
        }

        // jump to offset in file
        tiffimage.getFile().seek(offset);

        // read the data from the file
        byte[] b = new byte[(int) entries * 2];
        if (tiffimage.getFile().read(b) != entries * 2) {
            throw (new IOException("Could not read enough bytes."));
        }

        // parse the shorts
        for (int i = 0, j = 0; i < entries; i++, j += 2) {
            s[i] = tiffimage.getShort(b, j);
        }

        // return array
        return s;
    }

    /**
     * Will return a single long value.
     * 
     * @return a single value
     * @throws IOException
     *             if not long type, or more than 1 entry.
     */
    public long getUnsignedLongValue() throws IOException {
        if (entries != 1) {
            throw (new IOException("Only expected 1 entry."));
        }
        if (type == TYPE_LONG) {
            return tiffimage.getLong(hdr, 0);
        } else {
            throw (new IOException("Can't handle type " + type + " for tag " + tag + "."));
        }
    }

    /**
     * Will return an array of long values.
     * 
     * @return array with entries.
     * @throws IOException
     *             if not long type or error reading data.
     */
    public long[] getUnsignedLongValues() throws IOException {
        if (this.type != TYPE_LONG) {
            throw (new IOException("Can't handle type " + type + " for tag " + tag + "."));
        }

        long[] s = new long[(int) entries];

        // 1 long stored as offset!
        if (entries == 1) {
            s[0] = tiffimage.getLong(hdr, 0);
            return s;

        }
        if ((entries == 2) && tiffimage.isBigTiff()) {
            s[0] = tiffimage.getLong(hdr, 0);
            s[1] = tiffimage.getLong(hdr, 4);
            return s;
        }

        // jump to offset in file
        tiffimage.getFile().seek(offset);

        // read the data from the file
        byte[] b = new byte[(int) entries * 4];
        if (tiffimage.getFile().read(b) != entries * 4) {
            throw (new IOException("Could not read enough bytes."));
        }

        // parse the shorts
        for (int i = 0, j = 0; i < entries; i++, j += 4) {
            s[i] = tiffimage.getLong(b, j);
        }

        // return array
        return s;
    }

    /**
     * Will return a long8 value.
     * 
     * @return a single long8 value.
     * @throws IOException
     *             if not long type or error reading data.
     */
    public long getLong8Value() throws IOException {
        if (entries != 1) {
            throw (new IOException("Only expected 1 entry."));
        }
        if (this.type != TYPE_LONG8) {
            throw (new IOException("Can't handle type " + type + " for tag " + tag + "."));
        }
        if (!this.tiffimage.isBigTiff()) {
            throw (new IOException("Can't handle type " + type + " for tag " + tag + " (not bigtiff)."));
        }

        return tiffimage.getLong8(hdr, 0);
    }

    /**
     * Will return an array of long8 values.
     * 
     * @return array with entries.
     * @throws IOException
     *             if not long type or error reading data.
     */
    public long[] getLong8Values() throws IOException {
        if (this.type != TYPE_LONG8) {
            throw (new IOException("Can't handle type " + type + " for tag " + tag + "."));
        }
        if (!this.tiffimage.isBigTiff()) {
            throw (new IOException("Can't handle type " + type + " for tag " + tag + " (not bigtiff)."));
        }

        long[] s = new long[(int) entries];

        // 1 long stored as offset!
        if (entries == 1) {
            s[0] = tiffimage.getLong8(hdr, 0);
            return s;

        }

        // jump to offset in file
        tiffimage.getFile().seek(offset);

        // read the data from the file
        byte[] b = new byte[(int) entries * 8];
        if (tiffimage.getFile().read(b) != entries * 8) {
            throw (new IOException("Could not read enough bytes."));
        }

        // parse the shorts
        for (int i = 0, j = 0; i < entries; i++, j += 8) {
            s[i] = tiffimage.getLong8(b, j);
        }

        // return array
        return s;
    }

    /**
     * Will return a single rational value.
     * 
     * @return a rational value
     * @throws IOException
     *             if not rational type, or more than 1 entry.
     */
    public Rational getRationalValue() throws IOException {
        if (entries != 0) {
            logger.debug("Expected 1 entry for rational value.");
        }
        if (entries == 0) {
            return new Rational(1, 72);
        }
        return getRationalValues()[0];
    }

    /**
     * Will return an array of rational values.
     * 
     * @return array with entries.
     * @throws IOException
     *             if not rational type or error reading data.
     */
    public Rational[] getRationalValues() throws IOException {
        if (this.type != TYPE_RATIONAL) {
            throw (new IOException("Can't handle type " + type + " for tag " + tag + "."));
        }

        Rational[] s = new Rational[(int) entries];

        // jumpt to offset in file
        tiffimage.getFile().seek(offset);

        // read the data from the file
        byte[] b = new byte[(int) entries * 8];
        if (tiffimage.getFile().read(b) != entries * 8) {
            throw (new IOException("Could not read enough bytes."));
        }

        // parse the rationals
        for (int i = 0, j = 0; i < entries; i++, j += 8) {
            s[i] = new Rational(tiffimage.getLong(b, j), tiffimage.getLong(b, j + 4));
        }

        // return array
        return s;
    }

    /**
     * Will return a single double value.
     * 
     * @return a single value
     * @throws IOException
     *             if not double type, or more than 1 entry.
     */
    public double getDoubleValue() throws IOException {
        if (entries != 1) {
            throw (new IOException("Only expected 1 entry."));
        }
        return getDoubleValues()[0];
    }

    /**
     * Will return an array of double values.
     * 
     * @return array with entries.
     * @throws IOException
     *             if not short type or error reading data.
     */
    public double[] getDoubleValues() throws IOException {
        if (this.type != TYPE_DOUBLE) {
            throw (new IOException("Can't handle type " + type + " for tag " + tag + "."));
        }

        double[] s = new double[(int) entries];

        // jumpt to offset in file
        tiffimage.getFile().seek(offset);

        // read the data from the file
        byte[] b = new byte[(int) entries * 8];
        if (tiffimage.getFile().read(b) != entries * 8) {
            throw (new IOException("Could not read enough bytes."));
        }

        // parse the rationals
        for (int i = 0, j = 0; i < entries; i++, j += 8) {
            s[i] = tiffimage.getDouble(b, j);
        }

        // return array
        return s;
    }

    /**
     * Convert the entry to a string for easy printing.
     * 
     * @return entry as string.
     */
    @Override
    public String toString() {
        return "tag=" + tag + " type=" + type + " entries=" + entries + " offset=" + offset;
    }
}
