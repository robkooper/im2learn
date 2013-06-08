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
package edu.illinois.ncsa.isda.im2learn.core.io.imageio;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.im2learn.core.datatype.*;
import edu.illinois.ncsa.isda.im2learn.core.io.ImageLoader;
import edu.illinois.ncsa.isda.im2learn.core.io.ImageReader;
import edu.illinois.ncsa.isda.im2learn.core.io.ImageWriter;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 * This class encapsulates the imageio loader from SUN.
 */
public class ImageIOLoader implements ImageReader, ImageWriter {
    private static Log logger = LogFactory.getLog(ImageIOLoader.class);

    /**
     * Try and find a reader, if one found we can actually read the file.
     *
     * @param filename of file to be read.
     * @param hdr      used for magic number
     * @return true if image can be read.
     */
    public boolean canRead(String filename, byte[] hdr) {
        ImageInputStream iis = null;
        try {
            iis = ImageIO.createImageInputStream(new File(filename));
        } catch (Exception exc) {
            logger.error(exc);
            return false;
        }
        if (iis == null) {
            return false;
        }

        Iterator readers = ImageIO.getImageReaders(iis);
        if ((readers == null) || !readers.hasNext()) {
            try {
                iis.close();
            } catch (IOException exc) {
                logger.error(exc);
            }
            return false;
        }

        javax.imageio.ImageReader reader = (javax.imageio.ImageReader) readers.next();
        if (reader == null) {
            try {
                iis.close();
            } catch (IOException exc) {
                logger.error(exc);
            }
            return false;
        }

        try {
            iis.close();
        } catch (IOException exc) {
            logger.error(exc);
        }
        return true;
    }

    /**
     * Loads an image and returns the imageobject containing the image.
     *
     * @param filename of the file to load.
     * @param subarea  of the file to load, or null to load full image.
     * @param sampling is the sampling that needs to be done.
     * @return the imageobject containing the loaded image
     * @throws IOException if an error occurrs reading the file.
     */
    public ImageObject readImage(String filename, SubArea subarea, int sampling) throws IOException {
        return readImage(filename, subarea, sampling, false);
    }

    /**
     * This function will read the file and return an imageobject that contains
     * the information of the image but not the imagedata itself.
     *
     * @param filename of the file to be read
     * @return the file as an imageobject except of the imagedata.
     * @throws IOException if the file could not be read.
     */
    public ImageObject readImageHeader(String filename) throws IOException {
        return readImage(filename, null, 1, true);
    }

    /**
     * Return a list of extentions this class can read.
     *
     * @return a list of extentions that are understood by this class.
     */
    public String[] readExt() {
        return ImageIO.getReaderFormatNames();
    }

    /**
     * Loads an image and returns the imageobject containing the image.
     *
     * @param filename of the file to load.
     * @param subarea  of the file to load, or null to load full image.
     * @param sampling is the sampling that needs to be done.
     * @param header   true if only header needs to be read.
     * @return the imageobject containing the loaded image
     * @throws IOException if an error occurrs reading the file.
     */
    public ImageObject readImage(String filename, SubArea subarea, int sampling, boolean header) throws IOException {
        ImageInputStream iis = ImageIO.createImageInputStream(new File(filename));
        Iterator readers = ImageIO.getImageReaders(iis);
        javax.imageio.ImageReader reader = (javax.imageio.ImageReader) readers.next();
        reader.setInput(iis, true);

        // set the params for loading, subsampling and subarea
        ImageReadParam param = reader.getDefaultReadParam();
        int bands = 3;
        if (subarea != null) {
            param.setSourceRegion(subarea);
            if (subarea.getNumBands() != 0) {
                bands = subarea.getNumBands();
                int[] bandsrc = new int[bands];
                int[] banddst = new int[bands];
                for (int i = 0, j = subarea.getFirstBand(); i < bands; i++, j++) {
                    bandsrc[i] = j;
                    banddst[i] = i;
                }
                param.setSourceBands(bandsrc);
                param.setDestinationBands(banddst);
            }
        }
        param.setSourceSubsampling(sampling, sampling, 0, 0);

        // read the first image
        BufferedImage bi = reader.read(0, param);
        ImageObject result = null;

        if (bi.getColorModel() instanceof ComponentColorModel) {
            // get the raster since this is really what we want.
            Raster raster = bi.getData();
            int width = raster.getWidth();
            int height = raster.getHeight();
            bands = raster.getNumBands();

            // create imageobject
            switch (raster.getDataBuffer().getDataType()) {
                case DataBuffer.TYPE_BYTE:
                    result = new ImageObjectByte(height, width, bands, header);
                    break;

                case DataBuffer.TYPE_SHORT:
                    result = new ImageObjectShort(height, width, bands, header);
                    break;

                case DataBuffer.TYPE_USHORT:
                    result = new ImageObjectUShort(height, width, bands, header);
                    break;

                case DataBuffer.TYPE_INT:
                    result = new ImageObjectInt(height, width, bands, header);
                    break;

                case DataBuffer.TYPE_FLOAT:
                    result = new ImageObjectFloat(height, width, bands, header);
                    break;

                case DataBuffer.TYPE_DOUBLE:
                    result = new ImageObjectDouble(height, width, bands, header);
                    break;

                default:
                    try {
                        iis.close();
                    } catch (IOException exc) {
                        logger.error(exc);
                    }
                    throw(new IOException("Unknown dataformat."));
            }
            // copy all the data
            if (!header) {
                if (bands == raster.getNumBands()) {
                    raster.getDataElements(0, 0, width, height, result.getData());
                    ImageLoader.fireProgress(100, 100);
                } else {
                    int i, r, c, b;
                    int total = result.getSize();
                    for (i = 0, r = 0; r < height; r++) {
                        for (c = 0; c < width; c++) {
                            ImageLoader.fireProgress(i, total);
                            for (b = 0; b < bands; b++, i++) {
                                result.set(i, raster.getSample(c, r, b));
                            }
                        }
                    }
                }
            }

        } else if (bi.getColorModel() instanceof DirectColorModel) {
            // get the raster since this is really what we want.
            Raster raster = bi.getData();
            int width = raster.getWidth();
            int height = raster.getHeight();
            bands = raster.getNumBands();

            // create imageobject
            switch (raster.getDataBuffer().getDataType()) {
                case DataBuffer.TYPE_BYTE:
                    result = new ImageObjectByte(height, width, bands);
                    break;

                case DataBuffer.TYPE_SHORT:
                    result = new ImageObjectShort(height, width, bands);
                    break;

                case DataBuffer.TYPE_USHORT:
                    result = new ImageObjectUShort(height, width, bands);
                    break;

                case DataBuffer.TYPE_INT:
                    result = new ImageObjectInt(height, width, bands);
                    break;

                case DataBuffer.TYPE_FLOAT:
                    result = new ImageObjectFloat(height, width, bands);
                    break;

                case DataBuffer.TYPE_DOUBLE:
                    result = new ImageObjectDouble(height, width, bands);
                    break;

                default:
                    try {
                        iis.close();
                    } catch (IOException exc) {
                        logger.error(exc);
                    }
                    throw(new IOException("Unknown dataformat."));
            }

            if (!header) {
                // copy all the data
                int i, r, c, b;
                int total = result.getSize();
                for (i = 0, r = 0; r < height; r++) {
                    for (c = 0; c < width; c++) {
                        ImageLoader.fireProgress(i, total);
                        for (b = 0; b < bands; b++, i++) {
                            result.set(i, raster.getSample(c, r, b));
                        }
                    }
                }
            }
        } else {
            int w = bi.getWidth();
            int h = bi.getHeight();

            // TODO this assumes all images will be of type byte with 3 bands
            result = new ImageObjectByte(h, w, bands, header);
            if (!header) {
                int i, r, c;
                int[] rgb = new int[w];
                int total = result.getSize();
                for (i = 0, r = 0; r < h; r++) {
                    bi.getRGB(0, r, w, 1, rgb, 0, w);
                    for (c = 0; c < w; c++) {
                        ImageLoader.fireProgress(i, total);
                        if (bands > 2) {
                            result.set(i++, (rgb[c] & 0xff0000) >> 16);
                        }
                        if (bands > 1) {
                            result.set(i++, (rgb[c] & 0x00ff00) >> 8);
                        }
                        result.set(i++, (rgb[c] & 0x0000ff));
                    }
                }
            }
        }

        //String[] names = bi.getPropertyNames();
        //if (names != null) {
        //    for (int i = 0; i < names.length; i++) {
        //        System.out.println(names[i] + " = " + bi.getProperty(names[i]));
        //    }
        //}

        iis.close();
        return result;
    }

    /**
     * Return the description of the reader (or writer).
     *
     * @return decription of the reader (or writer)
     */
    public String getDescription() {
        return "Java ImageIO Loader";
    }

    /**
     * Returns true if the class can write the file.
     *
     * @param filename of the file to be written.
     * @return true if the file can be written by this class.
     */
    public boolean canWrite(String filename) {
        return getWriter(filename) != null;
    }

    /**
     * This function will write the imageobject to a file.
     *
     * @param filename    of the file to be written.
     * @param imageobject the image image to be written.
     * @throws IOException if the file could not be written.
     */
    public void writeImage(String filename, ImageObject imageobject) throws IOException {
        javax.imageio.ImageWriter writer = getWriter(filename);
        ImageOutputStream ios = ImageIO.createImageOutputStream(new File(filename));
        writer.setOutput(ios);

        int w = imageobject.getNumCols();
        int h = imageobject.getNumRows();
        int s = imageobject.getSize();
        int band = imageobject.getNumBands();

        // TODO is assumption of 3 byte image correct?

        int r = 0;
        int g = 1;
        int b = 2;
        int x;


        BufferedImage bi = null;
        if (band < 3) {
            // less than 3 bands, so assume a gray scale image, just
            // grab first band.
            bi = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
            DataBuffer db = bi.getRaster().getDataBuffer();
            int[] gray = (int[]) imageobject.getProperty(ImageObject.DEFAULT_GRAY);
            if (gray != null) {
                r = gray[0];
            }
            for (x = 0; r < s; r += band, x++) {
                db.setElem(x, imageobject.getByte(r));
            }

        } else {
            // color image, grab selected rgb bands and conver it to a
            // 3 byte image.
            bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            DataBuffer db = bi.getRaster().getDataBuffer();
            int[] rgb = (int[]) imageobject.getProperty(ImageObject.DEFAULT_RGB);
            if (rgb != null) {
                r = rgb[0];
                g = rgb[1];
                b = rgb[2];
            }

            // in case of byte we can do simple copy, otherwise we need to
            // scale the data to byte.
            int pix = 0;
            if (imageobject.getType() == ImageObject.TYPE_BYTE) {
                for (x = 0; r < s; r += band, g += band, b += band, x++) {
                    pix = (imageobject.getInt(r) & 0xff) << 16 |
                          (imageobject.getInt(g) & 0xff) << 8 |
                          (imageobject.getInt(b) & 0xff);
                    db.setElem(x, pix);
                }
            } else {
                int v = 0;
                double[] coef = new double[3];
                coef[0] = 255 / (imageobject.getMax(r) - imageobject.getMin(r));
                coef[1] = 255 / (imageobject.getMax(g) - imageobject.getMin(g));
                coef[2] = 255 / (imageobject.getMax(b) - imageobject.getMin(b));
                double[] min = new double[3];
                min[0] = imageobject.getMin(r);
                min[1] = imageobject.getMin(g);
                min[2] = imageobject.getMin(b);

                for (x = 0; r < s; r += band, g += band, b += band, x++) {
                    v = (int) ((imageobject.getDouble(r) - min[0]) * coef[0]);
                    pix = (v & 0xff) << 16;
                    v = (int) ((imageobject.getDouble(g) - min[1]) * coef[1]);
                    pix |= (v & 0xff) << 8;
                    v = (int) ((imageobject.getDouble(b) - min[2]) * coef[2]);
                    pix |= (v & 0xff);
                    db.setElem(x, pix);
                }
            }
        }

        writer.write(bi);
        ios.close();
    }

    /**
     * Return a list of extentions this class can write.
     *
     * @return a list of extentions that are understood by this class.
     */
    public String[] writeExt() {
        return ImageIO.getWriterFormatNames();
    }

    /**
     * Based on the filename return the first writer.
     *
     * @param filename of file to be written.
     * @return writer for fileformat.
     */
    private javax.imageio.ImageWriter getWriter(String filename) {
        String ext = ImageLoader.getExtention(filename);
        if (ext == null) {
            return null;
        }
        Iterator writers = ImageIO.getImageWritersByFormatName(ext);
        if ((writers == null) || !writers.hasNext()) {
            return null;
        }
        return (javax.imageio.ImageWriter) writers.next();
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
}
