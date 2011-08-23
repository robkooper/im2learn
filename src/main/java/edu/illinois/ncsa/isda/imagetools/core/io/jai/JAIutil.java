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
package edu.illinois.ncsa.isda.imagetools.core.io.jai;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferDouble;
import java.awt.image.DataBufferFloat;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.renderable.ParameterBlock;

import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RasterFactory;
import javax.media.jai.RenderedImageAdapter;
import javax.media.jai.RenderedOp;
import javax.media.jai.TiledImage;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageException;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObjectByte;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObjectDouble;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObjectFloat;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObjectInt;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObjectShort;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObjectUShort;
import edu.illinois.ncsa.isda.imagetools.core.datatype.SubArea;

/**
 * Wrapper class to provide easy access to JAI functions. All the JAI routines
 * used in Im2Learn are in this class. Code should use haveJAI() to see if the
 * JAI libraries are found.
 * 
 * @author Sang-Chul Lee
 * @author Rob Kooper
 * @version 2.0
 */
public class JAIutil {
    private static boolean firsttime = true;
    private static Log     logger    = LogFactory.getLog(JAIutil.class);

    /**
     * Test to see if JAI is found. This does not guarantee all the JAI code is
     * available, but will do a simple test to see if JAI is loaded.
     * 
     * @return true if JAI is found, will print version as info.
     */
    static public boolean haveJAI() {
        try {
            String result = JAI.getBuildVersion();
            if (firsttime) {
                logger.info("JAI found, version " + result);
                firsttime = false;
            }
            return true;
        } catch (Throwable thr) {
            return false;
        }
    }

    /**
     * Convert a RenderedOp to a Im2Learn ImageObject. Given the RenderedOp
     * convert it to an appropriate ImageObject.
     * 
     * @param rop
     *            the RenderedOp to be converted.
     * @return the ImageObject that contains the image from rop.
     * @throws ImageException
     *             if no appropriate conversion type is found.
     */
    static public ImageObject getImageObject(RenderedOp rop) throws ImageException {
        if (rop == null) {
            throw (new ImageException("No Rendered Image."));
        }

        // get width/height/bands and the sampledata from rop
        int width = rop.getWidth();
        int height = rop.getHeight();
        int minx = rop.getMinX();
        int miny = rop.getMinY();
        Raster imData = rop.getData(new Rectangle(minx, miny, width, height));
        int bands = imData.getNumBands();

        // convert to right type, byte and short need intermediate int[] to
        // convert to right type.
        int[] data = null;
        int len = 0;
        switch (rop.getSampleModel().getDataType()) {
        case DataBuffer.TYPE_BYTE:
            ImageObjectByte imgbyte = new ImageObjectByte(height, width, bands);
            data = imData.getPixels(minx, miny, width, height, (int[]) null);
            len = data.length;
            for (int i = 0; i < len; i++) {
                imgbyte.set(i, data[i]);
            }
            return imgbyte;

        case DataBuffer.TYPE_SHORT:
            ImageObjectShort imgshort = new ImageObjectShort(height, width, bands);
            data = imData.getPixels(minx, miny, width, height, (int[]) null);
            len = data.length;
            for (int i = 0; i < len; i++) {
                imgshort.set(i, data[i]);
            }
            return imgshort;

        case DataBuffer.TYPE_USHORT:
            ImageObjectUShort imgushort = new ImageObjectUShort(height, width, bands);
            data = imData.getPixels(minx, miny, width, height, (int[]) null);
            len = data.length;
            for (int i = 0; i < len; i++) {
                imgushort.set(i, data[i]);
            }
            return imgushort;

        case DataBuffer.TYPE_INT:
            ImageObjectInt imgint = new ImageObjectInt(height, width, bands);
            imData.getPixels(minx, miny, width, height, (int[]) imgint.getData());
            return imgint;

        case DataBuffer.TYPE_FLOAT:
            ImageObjectFloat imgfloat = new ImageObjectFloat(height, width, bands);
            imData.getPixels(minx, miny, width, height, (float[]) imgfloat.getData());
            return imgfloat;

        case DataBuffer.TYPE_DOUBLE:
            ImageObjectDouble imgdouble = new ImageObjectDouble(height, width, bands);
            imData.getPixels(minx, miny, width, height, (double[]) imgdouble.getData());
            return imgdouble;

        default:
            throw (new ImageException("Can't convert this type."));
        }
    }

    /**
     * Convert a RenderedOp to a Im2Learn ImageObject. Given the RenderedOp
     * convert it to an appropriate ImageObject.
     * 
     * @param rop
     *            the RenderedOp to be converted.
     * @return the ImageObject that contains the image from rop.
     * @throws ImageException
     *             if no appropriate conversion type is found.
     */
    static public ImageObject getImageObject(RenderedOp rop, SubArea subarea) throws ImageException {
        if (rop == null) {
            throw (new ImageException("No Rendered Image."));
        }
        if (subarea == null) {
            return getImageObject(rop);
        }

        // Create result image
        ImageObject img;
        switch (rop.getSampleModel().getDataType()) {
        case DataBuffer.TYPE_BYTE:
            img = new ImageObjectByte(subarea.getHigh(), subarea.getWide(), subarea.getNumBands());
            break;

        case DataBuffer.TYPE_SHORT:
            img = new ImageObjectShort(subarea.getHigh(), subarea.getWide(), subarea.getNumBands());
            break;

        case DataBuffer.TYPE_USHORT:
            img = new ImageObjectUShort(subarea.getHigh(), subarea.getWide(), subarea.getNumBands());
            break;

        case DataBuffer.TYPE_INT:
            img = new ImageObjectInt(subarea.getHigh(), subarea.getWide(), subarea.getNumBands());
            break;

        case DataBuffer.TYPE_FLOAT:
            img = new ImageObjectFloat(subarea.getHigh(), subarea.getWide(), subarea.getNumBands());
            break;

        case DataBuffer.TYPE_DOUBLE:
            img = new ImageObjectDouble(subarea.getHigh(), subarea.getWide(), subarea.getNumBands());
            break;

        default:
            throw (new ImageException("Can't convert this type."));
        }

        // get width/height/bands and the sampledata from rop
        int width = rop.getWidth();
        int height = rop.getHeight();
        int minx = rop.getMinX();
        int miny = rop.getMinY();
        Raster imData = rop.getData();
        int bands = imData.getNumBands();

        int r, c, b, x, y, z;
        int[] rgb;
        if (rop.getSampleModel() instanceof PixelInterleavedSampleModel) {
            rgb = ((PixelInterleavedSampleModel) rop.getSampleModel()).getBandOffsets();
        } else {
            rgb = new int[bands];
            for (b = 0; b < bands; b++) {
                rgb[b] = b;
            }
        }
        for (y = subarea.getRow(), r = 0; r < subarea.getHeight(); r++, y++) {
            for (x = subarea.getCol(), c = 0; c < subarea.getWidth(); c++, x++) {
                for (z = subarea.getFirstBand(), b = 0; z < subarea.getNumBands(); b++, z++) {
                    if ((x < minx) || (x >= width + minx) || (y < miny) || (y >= height + miny) || (z < 0) || (z > bands)) {
                        img.set(r, c, b, 0);
                    } else {
                        img.set(r, c, b, imData.getSampleDouble(x, y, rgb[z]));
                    }
                }
            }
        }

        return img;
    }

    static public RenderedOp getRenderedOp(ImageObject imgobj) throws ImageException {
        return getRenderedOp(imgobj, Double.NaN, Double.NaN);
    }

    /**
     * Convert imageobject to renderedop for JAI. During the conversion all
     * pixels that have value from will be converted to value from+1. This will
     * allow the value from to be used as the invalid data value. Any pixels
     * that are marked as invalid will be set to the value from.
     * 
     * @param imgobj
     *            the image object that will be converted.
     * @param from
     *            the value of the pixel that will be used as invalid, if this
     *            is Double.NaN, no pixels will be converted.
     * @return the renderedop image.
     * @throws ImageException
     *             if the image could not be converted
     */
    static public RenderedOp getRenderedOp(ImageObject imgobj, double from) throws ImageException {
        return getRenderedOp(imgobj, from, from + 1);
    }

    /**
     * Convert imageobject to renderedop for JAI. During the conversion all
     * pixels that have value from will be converted to value to. This will
     * allow the value from to be used as the invalid data value. Any pixels
     * that are marked as invalid will be set to the value from.
     * 
     * @param imgobj
     *            the image object that will be converted.
     * @param from
     *            the value of the pixel that will be used as invalid, if this
     *            is Double.NaN, no pixels will be converted.
     * @param to
     *            the value any pixel not set should be set to.
     * @return the renderedop image.
     * @throws ImageException
     *             if the image could not be converted
     */
    static public RenderedOp getRenderedOp(ImageObject imgobj, double from, double to) throws ImageException {
        if (imgobj == null) {
            throw (new ImageException("No Rendered Image."));
        }

        int width = imgobj.getNumCols();
        int height = imgobj.getNumRows();
        int bands = imgobj.getNumBands();

        DataBuffer dataBuffer = null;
        switch (imgobj.getType()) {
        case ImageObject.TYPE_BYTE:
            dataBuffer = new DataBufferByte((byte[]) imgobj.getData(), imgobj.getSize());
            break;

        case ImageObject.TYPE_SHORT:
            dataBuffer = new DataBufferShort((short[]) imgobj.getData(), imgobj.getSize());
            break;

        case ImageObject.TYPE_USHORT:
            dataBuffer = new DataBufferUShort((short[]) imgobj.getData(), imgobj.getSize());
            break;

        case ImageObject.TYPE_INT:
            dataBuffer = new DataBufferInt((int[]) imgobj.getData(), imgobj.getSize());
            break;

        case ImageObject.TYPE_FLOAT:
            dataBuffer = new DataBufferFloat((float[]) imgobj.getData(), imgobj.getSize());
            break;

        case ImageObject.TYPE_DOUBLE:
            dataBuffer = new DataBufferDouble((double[]) imgobj.getData(), imgobj.getSize());
            break;

        default:
            throw (new ImageException("Can not convert image to RenderedOp."));
        }

        if (!Double.isNaN(from) && !Double.isNaN(to)) {
            for (int bank = 0; bank < dataBuffer.getNumBanks(); bank++) {
                for (int i = 0; i < dataBuffer.getSize(); i++) {
                    if (dataBuffer.getElemDouble(bank, i) == from) {
                        dataBuffer.setElemDouble(bank, i, to);
                    }
                    if (dataBuffer.getElemDouble(bank, i) == imgobj.getInvalidData()) {
                        dataBuffer.setElemDouble(bank, i, from);
                    }
                }
            }
        }

        RenderedImage ren = null;

        // create a float sample model
        SampleModel sampleModel = RasterFactory.createPixelInterleavedSampleModel(dataBuffer.getDataType(), width, height, bands);

        // create a compatible ColorModel
        ColorModel colourModel = PlanarImage.createColorModel(sampleModel);

        TiledImage tiledImage = new TiledImage(0, 0, width, height, 0, 0, sampleModel, colourModel);

        // create a Raster
        Point origin = new Point(0, 0);
        Raster raster = RasterFactory.createWritableRaster(sampleModel, dataBuffer, origin);

        // set the TiledImage data to that of the Raster
        tiledImage.setData(raster);
        ren = new RenderedImageAdapter(tiledImage);

        ParameterBlock pb = new ParameterBlock();
        pb.addSource(ren);
        pb.add(0f);
        pb.add(0f);
        return JAI.create("translate", pb, null);
    }
}
