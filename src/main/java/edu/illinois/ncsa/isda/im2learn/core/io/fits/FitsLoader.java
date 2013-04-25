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
package edu.illinois.ncsa.isda.imagetools.core.io.fits;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageException;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObjectDouble;
import edu.illinois.ncsa.isda.imagetools.core.datatype.SubArea;
import edu.illinois.ncsa.isda.imagetools.core.io.ImageReader;

/**
 * This class will read and write ENVI images. It will can read most keywords in
 * the image header, but only write the
 * bare minimum.
 * 
 * @author Rob Kooper
 */
public class FitsLoader implements ImageReader {
    private static final int sampleread = 10000;
    private static Log       log        = LogFactory.getLog(FitsLoader.class);

    /**
     * Returns true if the filename ends with .fits or .fits.gzz
     * 
     * @param filename
     *            used to load file.
     * @param hdr
     *            the first 100 bytes of the file.
     * @return true if the file can be read by this class.
     */
    public boolean canRead(String filename, byte[] hdr) {
        return filename.toLowerCase().endsWith(".fits") || filename.toLowerCase().endsWith(".fits.gz");
    }

    /**
     * This function will read the file and return an imageobject that contains
     * the file.
     * 
     * @param filename
     *            of the file to be read
     * @param subarea
     *            of the file to be read, null if full image is to be read.
     * @param sampling
     *            is the subsampling that needs to be done on the image as it is
     *            loaded.
     * @return the file as an imageobject.
     * @throws IOException
     *             if the file could not be read.
     */
    public ImageObject readImage(String filename, SubArea subarea, int sampling) throws IOException {
        return loadImage(filename, subarea, sampling, false);
    }

    /**
     * This function will read the file and return an imageobject that contains
     * the information of the image but not the
     * imagedata itself.
     * 
     * @param filename
     *            of the file to be read
     * @return the file as an imageobject except of the imagedata.
     * @throws IOException
     *             if the file could not be read.
     */
    public ImageObject readImageHeader(String filename) throws IOException {
        return loadImage(filename, null, 1, true);
    }

    public int getImageCount(String filename) {
        return 1;
    }

    public ImageObject readImage(String filename, int index, SubArea subarea, int sampling) throws IOException, ImageException {
        return readImage(filename, subarea, sampling);
    }

    public ImageObject readImageHeader(String filename, int index) throws IOException, ImageException {
        return readImageHeader(filename);
    }

    /**
     * Return a list of extentions this class can read.
     * 
     * @return a list of extentions that are understood by this class.
     */
    public String[] readExt() {
        return new String[] { "fits", "fits.gz" };
    }

    /**
     * Return the description of the reader (or writer).
     * 
     * @return decription of the reader (or writer)
     */
    public String getDescription() {
        return "Fits files";
    }

    /**
     * This function will read the image and return the imageobject.
     * 
     * @param filename
     *            of the file to read.
     * @param subarea
     *            of the file to read.
     * @param sampling
     *            the subsampling to use.
     * @param header
     *            true if only the header should be read.
     * @return an imageobject containting the file.
     * @throws IOException
     *             if the file could not be read correctly.
     */
    private ImageObject loadImage(String filename, SubArea subarea, int sampling, boolean header) throws IOException {
        // read header
        InputStream is = new FileInputStream(filename);

        try {
            if (filename.toLowerCase().endsWith(".gz")) {
                is = new GZIPInputStream(is);
            }

            // check header
            StringBuilder info = new StringBuilder();
            String line = getString(is, 80);
            info.append(line + "\n");
            if (!line.startsWith("SIMPLE")) {
                throw (new IOException("Could not load file, did not start with SIMPLE."));
            }

            // load header
            int count = 1;
            int width = -1;
            int height = -1;
            // TODO use bscale and bzero
            int nImages = 1;
            double bscale = 1;
            double bzero = 0;
            int bitsPerPixel = -1;
            while (true) {
                count++;
                line = getString(is, 80);
                info.append(line + "\n");

                // Cut the key/value pair
                int index = line.indexOf("=");

                // Strip out comments
                int commentIndex = line.indexOf("/", index);
                if (commentIndex < 0)
                    commentIndex = line.length();

                // Split that values
                String key;
                String value;
                if (index >= 0) {
                    key = line.substring(0, index).trim();
                    value = line.substring(index + 1, commentIndex).trim();
                } else {
                    key = line.trim();
                    value = "";
                }

                // Time to stop ?
                if (key.equals("END"))
                    break;

                // Look for interesting information         
                if (key.equals("BITPIX")) {
                    bitsPerPixel = Integer.parseInt(value);

                } else if (key.equals("NAXIS1")) {
                    width = Integer.parseInt(value);

                } else if (key.equals("NAXIS2")) {
                    height = Integer.parseInt(value);

                } else if (key.equals("NAXIS3")) { //for multi-frame fits
                    nImages = Integer.parseInt(value);

                } else if (key.equals("BSCALE")) {
                    bscale = Double.parseDouble(value);

                } else if (key.equals("BZERO")) {
                    bzero = Double.parseDouble(value);
                }
            }

            // create the image
            ImageObject result = new ImageObjectDouble(height, width, 1, header);
            result.setProperty(ImageObject.COMMENT, info.toString());

            if (!header) {
                // skip to next 2880 block
                int skip = 2880 - ((count * 80) % 2880);
                byte b[] = new byte[skip];
                if (is.read(b) != skip) {
                    throw (new IOException("Could not skip enough bytes."));
                }

                // tell user we don't undertand this type of image.
                if ((bitsPerPixel != 8) || (bitsPerPixel != 8) || (bitsPerPixel != 8)) {
                    log.warn("I don't unserstand " + bitsPerPixel + " bits per pixel.");
                }

                // load image
                b = new byte[16000];
                int avail = 0;
                double v = 0;
                long l = 0;
                count = 0;
                for (int i = 0; i < result.getSize(); i++ ) {
                    if (count >= avail) {
                        count = 0;
                        avail = is.read(b);
                        if (avail <= 0) {
                            throw (new IOException("Not enough data available."));
                        }
                    }

                    switch (bitsPerPixel) {
                        case 8:
                            v = b[count];
                            count += 1;
                            break;

                        case 16:
                            v = (b[count + 1] << 8) + (b[count] & 0xff);
                            count += 2;
                            break;

                        case 32:
                            v = (b[count + 3] << 24) + (b[count + 2] << 16) + (b[count + 1] << 8) + (b[count] & 0xff);
                            count += 4;
                            break;

                        case -32:
                            l = ((b[count + 3] & 0xff) << 24) | ((b[count + 2] & 0xff) << 16) | ((b[count + 1] & 0xff) << 8) | (b[count] & 0xff);
                            v = Float.intBitsToFloat((int) l);
                            count += 4;
                            break;

                        case -64:
                            l = ((long) (b[count + 7] & 0xff) << 56) | ((long) (b[count + 6] & 0xff) << 48) | ((long) (b[count + 5] & 0xff) << 40) | ((long) (b[count + 4] & 0xff) << 32)
                                    | ((long) (b[count + 3] & 0xff) << 24) | ((long) (b[count + 2] & 0xff) << 16) | ((long) (b[count + 1] & 0xff) << 8) | (long) (b[count] & 0xff);
                            v = Double.longBitsToDouble(l);
                            count += 4;
                            break;

                        default:
                    }

                    result.set(i, bzero + bscale * v);
                }
            }

            // done
            if (subarea != null) {
                try {
                    result = result.crop(subarea);
                } catch (ImageException e) {
                    throw (new IOException("Could not create subimage"));
                }
            }
            if (sampling != 1) {
                try {
                    result = result.scale(1.0 / sampling);
                } catch (ImageException e) {
                    throw (new IOException("Could not scale image"));
                }
            }
            return result;

        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    private String getString(InputStream is, int length) throws IOException {
        byte[] b = new byte[length];
        if (is.read(b) != 80) {
            throw (new IOException("Could not read " + length + " bytes."));
        }
        return new String(b);
    }
}
