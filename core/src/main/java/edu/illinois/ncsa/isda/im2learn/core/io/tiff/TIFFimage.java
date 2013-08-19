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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageException;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObjectOutOfCore;
import edu.illinois.ncsa.isda.im2learn.core.datatype.SubArea;
import edu.illinois.ncsa.isda.im2learn.core.geo.ProjectionConvert;
import edu.illinois.ncsa.isda.im2learn.core.geo.projection.Projection;
import edu.illinois.ncsa.isda.im2learn.core.io.ImageLoader;

/**
 * This class will hold all the information about the tiff file. Most important
 * in here is the directory. This contains all the information about the tiff
 * image.
 */
class TIFFimage {
    private String                  filename                  = null;
    private RandomAccessFile        file                      = null;
    private boolean                 bigtiff                   = false;
    private boolean                 littleendian              = false;
    private int                     sampling                  = 1;
    private SubArea                 subarea                   = null;
    private final Vector<IFDEntry>  ifd                       = new Vector<IFDEntry>();
    private ImageObject             image                     = null;
    private final GeoEntry          geo                       = new GeoEntry();
    private int                     size                      = 0;

    // no default values, need to be in directory.
    private long                    ImageLength               = -1;
    private long                    ImageWidth                = -1;
    private long[]                  StripByteCounts           = null;
    private long[]                  StripOffsets              = null;
    private Rational                XResolution               = null;
    private Rational                YResolution               = null;
    private int                     PhotometricInterpretation = -1;

    // tiled images
    private long                    TileWidth                 = -1;
    private long                    TileLength                = -1;
    private long[]                  TileOffsets               = null;
    private long[]                  TileByteCounts            = null;
    private boolean                 tiledImage                = false;

    // have defaults, need not be in directory.
    private long                    RowsPerStrip              = Long.MAX_VALUE;
    private int[]                   BitsPerSample             = new int[] { 1 };
    private String                  ImageDescription          = null;
    private String                  Software                  = null;
    private String                  DocumentName              = null;
    private int                     SamplesPerPixel           = 1;
    private int                     Compression               = 1;
    private int                     ResolutionUnit            = 2;
    private int                     PlanarConfiguration       = 1;
    private int[]                   ColorMap                  = null;
    private int                     SampleFormat              = 1;
    private double                  invaliddata               = 9999;
    private int                     Predictor                 = 1;
    private HashMap<String, Object> Properties                = null;

    private static Log              logger                    = LogFactory.getLog(TIFFimage.class);

    /**
     * Create a new TIFF image holder.
     * 
     * @param file
     *            containing the image.
     * @param littleendian
     *            is true if the file is little-endian.
     */
    public TIFFimage(String filename, RandomAccessFile file, boolean bigtiff, boolean littleendian, int sampling, SubArea subarea) {
        this.filename = filename;
        this.file = file;
        this.bigtiff = bigtiff;
        this.littleendian = littleendian;
        this.sampling = sampling;
        this.subarea = subarea;
    }

    /**
     * Checks to see if the TIFF image is correct. This does not mean it can be
     * correctly read. For instance compression might not be supported, this
     * would still be a valid image, but can not be read.
     * 
     * @return
     */
    public boolean isValid() {
        // check all required tags are read.
        if ((ImageLength == -1) || (ImageWidth == -1)) {
            logger.debug("No ImageLength or ImageWidth.");
            return false;
        }
        if (PhotometricInterpretation == -1) {
            logger.debug("No PhotometricInterpretation .");
            return false;
        }
        if (!tiledImage && ((StripByteCounts == null) || (StripOffsets == null))) {
            logger.debug("No StripByteCounts or StripOffsets.");
            return false;
        }
        if ((XResolution == null) || (YResolution == null)) {
            logger.debug("No XResolution or YResolution.");
            // BUG ignore this case, but warn user about invalid tiff file.
            // return false;
        }
        if ((PhotometricInterpretation == 3) && (ColorMap == null)) {
            logger.debug("No ColorMap and PhotometricInterpretation=3.");
            return false;
        }
        if (tiledImage && ((TileWidth == -1) || (TileLength == -1) || (TileByteCounts == null) || (TileOffsets == null))) {
            logger.debug("No TileWidth or TileLength or TileByteCounts or TileOffsets.");
            return false;
        }

        return true;
    }

    public boolean isBigTiff() {
        return bigtiff;
    }

    /**
     * Returns the file containg the image.
     * 
     * @return
     */
    public RandomAccessFile getFile() {
        return file;
    }

    /**
     * Returns the Im2Learn image read. If createImage is not called, this will
     * be null, after createimage it will contain all the header information,
     * and after readImage it will contain the actual image.
     * 
     * @return Im2Learn image.
     */
    public ImageObject getImage() {
        return image;
    }

    /**
     * Read the directory from disk.
     * 
     * @param offset
     *            in the file where directory is located.
     * @return offset to next image, or 0 if no more images.
     * @throws IOException
     *             if an error occured reading data.
     */
    public long readDirectory(long offset) throws IOException {
        if (isBigTiff()) {
            byte[] hdr = new byte[8];

            // jump to offset and read directory
            file.seek(offset);

            if (file.read(hdr, 0, 8) != 8) {
                throw (new IOException("Could not directory entries."));
            }
            int numentries = 0;
            numentries = (int) getLong8(hdr, 0);

            byte ifd[] = new byte[20 * numentries + 8];
            if (file.read(ifd) != ifd.length) {
                throw (new IOException("Could not read directory entries."));
            }

            int i, j = 0;
            for (i = 0; i < numentries; i++, j += 20) {
                this.ifd.add(new IFDEntry(ifd, j, this));
            }

            offset = getLong8(ifd, j);
            return offset;

        } else {
            byte[] hdr = new byte[2];

            // jump to offset and read directory
            file.seek(offset);

            if (file.read(hdr, 0, 2) != 2) {
                throw (new IOException("Could not directory entries."));
            }
            int numentries = 0;
            numentries = getShort(hdr, 0);

            byte ifd[] = new byte[12 * numentries + 4];
            if (file.read(ifd) != ifd.length) {
                throw (new IOException("Could not read directory entries."));
            }

            int i, j = 0;
            for (i = 0; i < numentries; i++, j += 12) {
                this.ifd.add(new IFDEntry(ifd, j, this));
            }

            offset = getLong(ifd, j);
            return offset;
        }
    }

    /**
     * Parse the directory and read the values.
     * 
     * @throws IOException
     *             if values could not be read.
     */
    public void parseDirectory() throws IOException {
        if (ifd.size() == 0) {
            throw (new IOException("Did not read directory first."));
        }

        for (int i = 0; i < this.ifd.size(); i++) {
            IFDEntry ifd = this.ifd.get(i);

            switch (ifd.getTag()) {
            case IFDEntry.TAG_BitsPerSample:
                BitsPerSample = ifd.getUnsignedShortValues();
                break;

            case IFDEntry.TAG_Compression:
                Compression = ifd.getUnsignedShortValue();
                break;

            case IFDEntry.TAG_Predictor:
                Predictor = ifd.getUnsignedShortValue();
                break;

            case IFDEntry.TAG_ColorMap:
                ColorMap = ifd.getUnsignedShortValues();
                break;

            case IFDEntry.TAG_DocumentName:
                DocumentName = ifd.getString();
                break;

            case IFDEntry.TAG_GeoAsciiParams:
                geo.setGeoAsciiParams(ifd.getString());
                break;

            case IFDEntry.TAG_GeoDoubleParams:
                geo.setGeoDoubleParams(ifd.getDoubleValues());
                break;

            case IFDEntry.TAG_GeoKeyDirectory:
                geo.setGeoKeyDirectory(ifd.getUnsignedShortValues());
                break;

            case IFDEntry.TAG_ImageDescription:
                ImageDescription = ifd.getString();
                break;

            case IFDEntry.TAG_ImageLength:
                ImageLength = ifd.getLongShortValue();
                break;

            case IFDEntry.TAG_ImageWidth:
                ImageWidth = ifd.getLongShortValue();
                break;

            case IFDEntry.TAG_MaxSampleValue:
                logger.debug("TAG_MaxSampleValue: " + ifd.getUnsignedShortValue());
                break;

            case IFDEntry.TAG_MinSampleValue:
                logger.debug("TAG_MinSampleValue: " + ifd.getUnsignedShortValue());
                break;

            case IFDEntry.TAG_GDALNoData:
                if (ifd.getType() == IFDEntry.TYPE_ASCII) {
                    String invalid = ifd.getString();
                    try {
                        invaliddata = Double.parseDouble(invalid);
                    } catch (NumberFormatException exc) {
                        logger.debug("Could not parse number, returning first byte as invalid data.", exc);
                        invaliddata = invalid.charAt(0);
                    }
                } else if (ifd.getType() == IFDEntry.TYPE_FLOAT) {
                    invaliddata = ifd.getFloatValue();
                } else {
                    throw (new IOException("Can not parse invalid data number." + ifd.toString()));
                }
                break;

            case IFDEntry.TAG_ImageMetadata:
                Properties = new HashMap<String, Object>();
                byte[] props = ifd.getArrayByte();
                try {
                    ByteArrayInputStream baos = new ByteArrayInputStream(props);
                    ObjectInputStream ios = new ObjectInputStream(baos);
                    HashMap<String, Object> data = (HashMap<String, Object>) ios.readObject();
                    ios.close();

                    // sort the entries by the key
                    // Collections.sort(Properties);
                    for (int m = 0; m < data.size(); m++) {
                        // System.out.println("m=" + m + ",val=" +
                        // data.get(String.valueOf(m)));
                        Properties.put(String.valueOf(m), data.get(String.valueOf(m)));
                    }

                    // test to see the properties
                    /*
                     * Set set = Properties.entrySet(); Iterator k =
                     * set.iterator(); while (k.hasNext()) { Map.Entry test =
                     * (Map.Entry) k.next(); System.out.println("test:" +
                     * test.getKey() + " : " + test.getValue()); }
                     */
                } catch (ClassNotFoundException exc) {
                    logger.debug("Could not cast the byte array to the HasMap, returning null.", exc);
                    Properties = null;
                }
                break;

            case IFDEntry.TAG_ModelPixelScale:
                geo.setModelPixelScale(ifd.getDoubleValues());
                break;

            case IFDEntry.TAG_ModelTiepoint:
                // double [] pts = ifd.getDoubleValues();
                // long numEntries = ifd.getEntries();
                // System.out.println("Num of Entries in Model Tie Point : " +
                // numEntries);
                // System.out.println("Model Tie Point : " + pts[0] + ", " +
                // pts[1] + ", " + pts[2]);

                // System.out.println("Setting Model Tiepoint = " +
                // ifd.getDoubleValues());
                geo.setModelTiepoint(ifd.getDoubleValues());
                break;

            case IFDEntry.TAG_NewSubfileType:
                // logger.debug("TAG_NewSubfileType: " +
                // ifd.getUnsignedLongValue());
                logger.debug("TAG_NewSubfileType: " + ifd.getUnsignedShortValue());
                break;

            case IFDEntry.TAG_PhotometricInterpretation:
                PhotometricInterpretation = ifd.getUnsignedShortValue();
                break;

            case IFDEntry.TAG_PlanarConfiguration:
                PlanarConfiguration = ifd.getUnsignedShortValue();
                break;

            case IFDEntry.TAG_ResolutionUnit:
                ResolutionUnit = ifd.getUnsignedShortValue();
                break;

            case IFDEntry.TAG_RowsPerStrip:
                RowsPerStrip = ifd.getLongShortValue();
                break;

            case IFDEntry.TAG_SampleFormat:
                SampleFormat = ifd.getUnsignedShortValue();
                break;

            case IFDEntry.TAG_SamplesPerPixel:
                SamplesPerPixel = (int) ifd.getLongShortValue();
                break;

            case IFDEntry.TAG_Software:
                Software = ifd.getString();
                break;

            case IFDEntry.TAG_StripByteCounts:
                StripByteCounts = ifd.getLongShortValues();
                break;

            case IFDEntry.TAG_StripOffsets:
                StripOffsets = ifd.getLongShortValues();
                break;

            case IFDEntry.TAG_SubfileType:
                logger.debug("TAG_SubfileType: " + ifd.getUnsignedShortValue());
                break;

            case IFDEntry.TAG_TileByteCounts:
                tiledImage = true;
                TileByteCounts = ifd.getLongShortValues();
                break;

            case IFDEntry.TAG_TileLength:
                tiledImage = true;
                TileLength = ifd.getLongShortValue();
                break;

            case IFDEntry.TAG_TileOffsets:
                tiledImage = true;
                TileOffsets = ifd.getLongShortValues();
                break;

            case IFDEntry.TAG_TileWidth:
                tiledImage = true;
                TileWidth = ifd.getLongShortValue();
                break;

            case IFDEntry.TAG_XResolution:
                XResolution = ifd.getRationalValue();
                break;

            case IFDEntry.TAG_YResolution:
                YResolution = ifd.getRationalValue();
                break;

            case IFDEntry.TAG_SubIFD:
                logger.debug("TAG_SubIFD: " + ifd.getLong8Value());
                break;

            default:
                if (ifd.isType(IFDEntry.TYPE_ASCII)) {
                    logger.debug("TAG_Unknown: " + ifd + " " + ifd.getString());
                } else {
                    logger.debug("TAG_Unknown: " + ifd);
                }
            }
        }
    }

    /**
     * Performs checks on the image parameters read and will create a Im2Learn
     * image with no data if the parameters are correct.
     * 
     * @throws IOException
     *             if parameters are incorrect, or image can not be read
     *             (compression for excample is not supported).
     */
    public void createImage() throws IOException, ImageException {
        // check the values
        if (!isValid()) {
            throw (new IOException("Invalid tiff image."));
        }

        // can only handle images with same number of bits per sample
        for (int i = 1; i < SamplesPerPixel; i++) {
            if (BitsPerSample[0] != BitsPerSample[i]) {
                throw (new IOException("Can only read files with bitspersample same."));
            }
        }

        // calculate w and h based on subarea and sampling
        int w = (int) ImageWidth;
        int h = (int) ImageLength;
        int b = SamplesPerPixel;
        size = w * h * b * BitsPerSample[0] / 8;
        if (subarea != null) {
            if (subarea.getEndCol() < ImageWidth) {
                w = subarea.getWide();
            } else {
                logger.info("To many pixels, clipping subarea.");
                w = (int) (ImageWidth - subarea.x);
            }
            if (subarea.getEndRow() < ImageLength) {
                h = subarea.getHigh();
            } else {
                logger.info("To many pixels, clipping subarea.");
                h = (int) (ImageLength - subarea.y);
            }
            if (subarea.getNumBands() != 0) {
                b = subarea.getNumBands();
            }
        }
        if (sampling != 1) {
            h = (int) Math.ceil((double) h / sampling);
            w = (int) Math.ceil((double) w / sampling);
        }

        if (PhotometricInterpretation == 3) {
            if ((subarea != null) && (subarea.getNumBands() != 0)) {
                image = ImageObject.createImage(h, w, b, ImageObject.TYPE_BYTE, true);
            } else {
                image = ImageObject.createImage(h, w, 3, ImageObject.TYPE_BYTE, true);
            }
        } else {
            // create image based on bits per sample
            switch (BitsPerSample[0]) {
            case 1:
            case 4:
            case 8:
                image = ImageObject.createImage(h, w, b, ImageObject.TYPE_BYTE, true);
                break;

            case 16:
                image = ImageObject.createImage(h, w, b, ImageObject.TYPE_USHORT, true);
                break;

            case 32:
                if (SampleFormat == 3) {
                    image = ImageObject.createImage(h, w, b, ImageObject.TYPE_FLOAT, true);
                } else {
                    image = ImageObject.createImage(h, w, b, ImageObject.TYPE_INT, true);
                }
                break;

            default:
                throw (new IOException("Can't read this many bits per sample."));
            }
        }

        // set properties
        if (ImageDescription != null) {
            image.setProperty(ImageObject.COMMENT, ImageDescription);
        }
        if (Software != null) {
            image.setProperty("Software", Software);
        }
        if (DocumentName != null) {
            image.setProperty("DocumentName", DocumentName);
        }
        image.setProperty("XResolution", XResolution);
        image.setProperty("YResolution", YResolution);

        switch (ResolutionUnit) {
        case 1:
            image.setProperty("Resolution", "Unknown");
            break;
        case 2:
            image.setProperty("Resolution", "Inch");
            break;
        case 3:
            image.setProperty("Resolution", "Centimeter");
            break;
        }

        // TODO can we recognize which images have this using comment?
        image.setInvalidData(invaliddata);

        // get the geoinfo, if any and adjust for sampling and subarea
        Projection geoinfo = ProjectionConvert.toOld(geo.getGeoInformation());
        if (geoinfo != null) {
            if (sampling != 1) {
                geoinfo.subsample(sampling, image.getNumRows(), image.getNumCols());
            }

            // is this necessary to get correct projection ?
            // geoinfo.setRasterSpaceJ(image.getNumRows());

            geoinfo.setNumRows(image.getNumRows());
            geoinfo.setNumCols(image.getNumCols());
            // System.out.println("Geo Info Rows : " + geoinfo.getNumRows() +
            // " Cols : " + geoinfo.getNumCols());
            // System.out.println(geoinfo.toString());

            image.setProperty(ImageObject.GEOINFO, geoinfo);
        }

        // add the image properties
        if (Properties != null) {
            image.setProperties(Properties);
            // test
            // System.out.println("Properties attached: size=" +
            // Properties.size() + ", content=" + Properties.toString());
        }
    }

    /**
     * Update projection information associated with the image based on a
     * potential .tfw file.
     */
    public void updateProjectionFromTFW(String tfwFile) throws Exception {
        TFWLoader tfwLoader = new TFWLoader(tfwFile);
        Projection projection = (Projection) image.getProperty(ImageObject.GEOINFO);

        // check that the info is there. if not, create it.
        if (projection == null) {
            projection = new Projection();
        }

        // update the projection and insert it back.
        tfwLoader.updateProjection(projection);
        image.setProperty(ImageObject.GEOINFO, projection);
    }

    /**
     * Read the image from disk.
     * 
     * @throws IOException
     *             if image could not be read.
     */
    public void readImage() throws IOException {
        if (image == null) {
            try {
                createImage();
            } catch (ImageException e) {
                logger.warn("Error creating the image.");
                return;
            }
        }

        // no planarconfiguration
        if (PlanarConfiguration != 1) {
            // throw(new IOException("Can not handle planarconfiguration."));
            logger.warn("Can not handle planarconfiguration.");
            return;
        }

        image.createArray();
        if (image.isInCore()) {
            if (tiledImage) {
                // TODO add tiled image support
                // throw(new IOException("No tiled image support."));
                switch (PhotometricInterpretation) {
                case 0:
                case 1:
                    // TODO readImageTileGrayscale
                    throw new IOException("No Grayscale tiled image support.");
                case 2:
                    // TODO readImageTileRGB
                    throw (new IOException("No RGB tiled image support."));
                case 3:
                    readImageTilePalette();
                    break;
                default:
                    logger.debug("PhotometricInterpretation = " + PhotometricInterpretation);
                    throw (new IOException("Can not read this type of tiled TIFF image."));
                }
            } else {
                switch (PhotometricInterpretation) {
                case 0:
                case 1:
                    readImageStripGrayscale();
                    break;

                case 2:
                    readImageStripRGB();
                    break;

                case 3:
                    readImageStripPalette();
                    break;

                default:
                    logger.debug("PhotometricInterpretation = " + PhotometricInterpretation);
                    throw (new IOException("Can not read this type of TIFF image."));
                }
            }
        } else {
            ImageObjectOutOfCore iooc = (ImageObjectOutOfCore) image;
            TIFFLoader tl = new TIFFLoader();
            for (SubArea sa : iooc.getTileBoxes()) {
                ImageObject imgobj = tl.readImage(this.filename, sa, this.sampling);
                for (int r = 0; r < imgobj.getNumRows(); r++) {
                    for (int c = 0; c < imgobj.getNumCols(); c++) {
                        for (int b = 0; b < imgobj.getNumBands(); b++) {
                            iooc.setDouble(sa.y + r, sa.x + c, b, imgobj.getDouble(r, c, b));
                        }
                    }
                }
            }
        }
    }

    /**
     * Reads a RGB image from disk that is stored in strips. The data is assumed
     * to be all the same number of bits, and is stored band interleave..
     * 
     * @throws IOException
     *             if an error occurred reading data.
     */
    private void readImageStripRGB() throws IOException {
        int i, b;

        // no need to check samples, we know based on imagetype what they are.
        SubArea area = subarea;
        if (area == null) {
            area = new SubArea(0, 0, (int) ImageWidth, (int) ImageLength);
        }
        if (area.getNumBands() == 0) {
            area.setBand(0);
            area.setNumBands(SamplesPerPixel);
        }

        DataReader reader = new DataReader();

        int size = image.getSize();

        int col = 0;
        int row = area.getRow();
        int bytesPerPixel = SamplesPerPixel;
        int bytesPerSample = 1;
        switch (image.getType()) {
        case ImageObject.TYPE_BYTE:
            bytesPerSample = 1;
            break;
        case ImageObject.TYPE_USHORT:
            bytesPerSample = 2;
            break;
        case ImageObject.TYPE_INT:
        case ImageObject.TYPE_FLOAT:
            bytesPerSample = 4;
            break;
        default:
            logger.debug("Missing switch statement for type " + image.getType());
        }
        bytesPerPixel *= bytesPerSample;
        int sampskip = bytesPerPixel * (sampling - 1) + (SamplesPerPixel - area.getNumBands()) * bytesPerSample;
        int rowskip = (int) (ImageWidth * bytesPerPixel);
        int colskip = (area.getCol() * bytesPerPixel) + area.getFirstBand() * bytesPerSample;
        int start = row * rowskip + colskip;
        final int imgType = image.getType();
        for (i = 0; i < size;) {
            switch (imgType) {
            case ImageObject.TYPE_BYTE:
                for (b = 0; b < area.getNumBands(); b++, start++, i++) {
                    image.set(i, reader.getByte(start));
                }
                start += sampskip;
                break;
            case ImageObject.TYPE_USHORT:
                for (b = 0; b < area.getNumBands(); b++, start += 2, i++) {
                    image.set(i, reader.getShort(start));
                }
                start += sampskip;
                break;
            case ImageObject.TYPE_INT:
                for (b = 0; b < area.getNumBands(); b++, start += 4, i++) {
                    image.set(i, reader.getLong(start));
                }
                start += sampskip;
                break;
            case ImageObject.TYPE_FLOAT:
                for (b = 0; b < area.getNumBands(); b++, start += 4, i++) {
                    image.set(i, reader.getFloat(start));
                }
                start += sampskip;
                break;
            }

            col += sampling;
            if (col >= area.width) {
                row += sampling;
                start = row * rowskip + colskip;
                col = 0;
            }

        }
    }

    /**
     * Reads a grayscale image from disk that is stored in strips. The data is
     * checked to see if it is 1, 4, 8 or 16 bits in size, and will only have 1
     * sample per pixel.
     * 
     * @throws IOException
     *             if an error occurred reading data.
     */
    private void readImageStripGrayscale() throws IOException {
        if (SamplesPerPixel != 1) {
            throw (new IOException("Expected only 1 sample per pixel."));
        }
        if ((BitsPerSample[0] != 1) && (BitsPerSample[0] != 4) && (BitsPerSample[0] != 8) && (BitsPerSample[0] != 16) && (BitsPerSample[0] != 32)) {
            throw (new IOException("Expected only 1, 4, 8, 16 or 32 bits per sample."));
        }

        SubArea area = subarea;
        if (area == null) {
            area = new SubArea(0, 0, (int) ImageWidth, (int) ImageLength);
        }

        DataReader reader = new DataReader();

        int x, i;
        int size = image.getSize();

        int row = area.getRow();
        int col = 0;
        double bytesPerPixel = BitsPerSample[0] / 8.0;
        long rowskip = (long) (ImageWidth * bytesPerPixel);
        long colskip = (long) (area.getCol() * bytesPerPixel);
        long start = row * rowskip + colskip;
        long off = (area.getCol() * BitsPerSample[0]) % 8;

        // TODO HACK should be cleaner, or make more generic
        if ((Compression == 4) || (Compression == 5)) {
            long offset = 0;
            i = 0;
            long stride = (BitsPerSample[0] * ImageWidth + 7) / 8;

            for (int r = 0; r < area.getHeight(); r += sampling) {
                offset = (r + area.getRow()) * stride;
                off = area.getCol() * BitsPerSample[0];
                offset += (off / 8);
                off = off % 8;
                for (int c = 0; c < area.getWidth(); c += sampling) {
                    x = (reader.getByte(offset) >> (8 - off)) & 0x01;
                    image.set(i++, (x == PhotometricInterpretation) ? 255 : 0);
                    off += sampling * BitsPerSample[0];
                    offset += (off / 8);
                    off = off % 8;
                }
            }

            return;
        }

        for (i = 0; i < size;) {
            switch (BitsPerSample[0]) {
            case 32:
                if (SampleFormat == 3) {
                    int bits = (int) reader.getLong(start);
                    // TODO HACK this is not really NaN.
                    if (bits == 0xff7fffff) {
                        // logger.debug("ArcGIS NaN? setting pixel " + i +
                        // " to 0.");
                        image.set(i++, 0);
                    } else {
                        image.set(i++, Float.intBitsToFloat(bits));
                    }
                } else {
                    if (PhotometricInterpretation == 0) {
                        image.set(i++, 0xffffff - reader.getLong(start));
                    } else {
                        image.set(i++, reader.getLong(start));
                    }
                }
                start += 4 * sampling;
                break;

            case 16:
                if (PhotometricInterpretation == 0) {
                    image.set(i++, 0xffff - reader.getShort(start));
                } else {
                    image.set(i++, reader.getShort(start));
                }
                start += 2 * sampling;
                break;

            case 8:
                if (PhotometricInterpretation == 0) {
                    image.set(i++, 255 - reader.getByte(start));
                } else {
                    image.set(i++, reader.getByte(start));
                }
                start += sampling;
                break;

            case 4:
                x = (reader.getByte(start) >> (8 - off)) & 0x0f << 4;
                if (PhotometricInterpretation == 0) {
                    image.set(i++, 255 - x);
                } else {
                    image.set(i++, x);
                }
                off += 4 * sampling;
                start += off / 8;
                off = off % 8;
                break;

            case 1:
                x = (reader.getByte(start) >> (8 - off)) & 0x01;
                image.set(i++, (x == PhotometricInterpretation) ? 255 : 0);
                off += sampling;
                start += off / 8;
                off = off % 8;
                break;
            }

            col += sampling;
            if (col >= area.width) {
                row += sampling;
                start = rowskip * row + colskip;
                off = (area.getCol() * BitsPerSample[0]) % 8;
                col = 0;
            }
        }
    }

    /**
     * Reads a RGB image from disk that is stored in strips. The data is assumed
     * to be all a single sample per pixel which is then looked up in a colormap
     * for the red, green and blue values. The colormap contains values that are
     * 16bit, only the top 8 bits are used.
     * 
     * @throws IOException
     *             if an error occurred reading data.
     */
    private void readImageStripPalette() throws IOException {
        if (SamplesPerPixel != 1) {
            throw (new IOException("Expected only 1 sample per pixel."));
        }
        // if ((BitsPerSample[0] != 4) && (BitsPerSample[0] != 8)) {
        if ((BitsPerSample[0] != 4) && (BitsPerSample[0] != 8) && (BitsPerSample[0] != 16)) {
            throw (new IOException("Expected 4, 8 or 16 bits per sample."));
        }

        SubArea area = subarea;
        if (area == null) {
            area = new SubArea(0, 0, (int) ImageWidth, (int) ImageLength);
        }
        if (area.getNumBands() == 0) {
            area.setBand(0);
            area.setNumBands(3);
        }

        boolean usered = false;
        if (area.getFirstBand() == 0) {
            usered = true;
        }
        boolean usegreen = false;
        if ((area.getFirstBand() <= 1) && (area.getLastBand() > 1)) {
            usegreen = true;
        }
        boolean useblue = false;
        if ((area.getFirstBand() <= 2) && (area.getLastBand() > 2)) {
            useblue = true;
        }

        DataReader reader = new DataReader();

        int i = 0;
        int x = 0;
        int greenstart = 1 << BitsPerSample[0];
        int bluestart = greenstart << 1;
        int size = image.getSize();

        int row = area.getRow();
        int col = 0;
        double bytesPerPixel = BitsPerSample[0] / 8.0;
        int rowskip = (int) (ImageWidth * bytesPerPixel);
        int colskip = (int) (area.getCol() * bytesPerPixel);
        int start = row * rowskip + colskip;
        int off = (area.getCol() * BitsPerSample[0]) % 8;

        for (i = 0; i < size;) {
            switch (BitsPerSample[0]) {
            case 16:
                x = (reader.getByte(start) & 0x0ff) << 8;
                x = x | (reader.getByte(start) & 0x0ff);
                start += 2 * sampling;
                break;

            case 8:
                x = reader.getByte(start) & 0xff;
                start += sampling;
                break;

            case 4:
                x = (reader.getByte(start) >> (8 - off)) & 0x0f << 4;
                off += 4 * sampling;
                start += off / 8;
                off = off % 8;
                break;
            }

            // TODO is this correct?
            if (usered) {
                if (BitsPerSample[0] == 16) {
                    image.set(i++, ColorMap[x] >> 16);
                } else {
                    image.set(i++, ColorMap[x] >> 8);
                }
            }
            if (usegreen) {
                if (BitsPerSample[0] == 16) {
                    image.set(i++, ColorMap[x + greenstart] >> 16);
                } else {
                    image.set(i++, ColorMap[x + greenstart] >> 8);
                }
            }
            if (useblue) {
                if (BitsPerSample[0] == 16) {
                    image.set(i++, ColorMap[x + bluestart] >> 16);
                } else {
                    image.set(i++, ColorMap[x + bluestart] >> 8);
                }
            }

            col += sampling;
            if (col >= area.width) {
                row += sampling;
                start = rowskip * row + colskip;
                off = (area.getCol() * BitsPerSample[0]) % 8;
                col = 0;
            }

        }
    }

    private void readImageTilePalette() throws IOException {
        if (SamplesPerPixel != 1) {
            throw (new IOException("Expected only 1 sample per pixel."));
        }
        // if ((BitsPerSample[0] != 4) && (BitsPerSample[0] != 8)) {
        if ((BitsPerSample[0] != 4) && (BitsPerSample[0] != 8) && (BitsPerSample[0] != 16)) {
            throw (new IOException("Expected 4, 8 or 16 bits per sample."));
        }

        SubArea area = subarea;
        if (area == null) {
            area = new SubArea(0, 0, (int) ImageWidth, (int) ImageLength);
        }
        if (area.getNumBands() == 0) {
            area.setBand(0);
            area.setNumBands(3);
        }

        boolean usered = false;
        if (area.getFirstBand() == 0) {
            usered = true;
        }
        boolean usegreen = false;
        if ((area.getFirstBand() <= 1) && (area.getLastBand() > 1)) {
            usegreen = true;
        }
        boolean useblue = false;
        if ((area.getFirstBand() <= 2) && (area.getLastBand() > 2)) {
            useblue = true;
        }

        // LAM --- this is the only difference between readImageStripPalette()
        // and
        // readImageTilePalette()...
        TiledDataReader reader = new TiledDataReader();

        int i = 0;
        int x = 0;
        int greenstart = 1 << BitsPerSample[0];
        int bluestart = greenstart << 1;
        int size = image.getSize();

        int col = 0;
        int row = area.getRow();
        int rowskip = (int) Math.ceil(ImageWidth * BitsPerSample[0] / 8.0);
        int off = area.getCol() * BitsPerSample[0];
        int start = rowskip * row + off / 8;
        off = off % 8;
        for (i = 0; i < size;) {
            if (start >= reader.total) {
                reader.read(start);
            }

            switch (BitsPerSample[0]) {
            case 16:
                x = (reader.getByte(start) & 0x0ff) << 8;
                x = x | (reader.getByte(start) & 0x0ff);
                off += 16 * sampling;
                start += off / 8;
                off = off % 8;
                break;

            case 8:
                x = reader.getByte(start);
                start += sampling;
                break;

            case 4:
                x = (reader.getByte(start) >> (8 - off)) & 0x0f << 4;
                off += 4 * sampling;
                start += off / 8;
                off = off % 8;
                break;
            }
            // TODO is this correct?
            if (usered) {
                image.set(i++, ColorMap[x] >> 8);
            }
            if (usegreen) {
                image.set(i++, ColorMap[x + greenstart] >> 8);
            }
            if (useblue) {
                image.set(i++, ColorMap[x + bluestart] >> 8);
            }

            col += sampling;
            if (col >= area.width) {
                row += sampling;
                off = area.getCol() * BitsPerSample[0];
                start = rowskip * row + off / 8;
                off = off % 8;
                col = 0;
            }
        }
    }

    /**
     * Based on endianess will convert the data to a unsigned short.
     * 
     * @param data
     *            containing the bytes
     * @param offset
     *            into data where data is located.
     * @return unsigned short value
     */
    public int getShort(byte[] data, int offset) {
        if (littleendian) {
            return (data[offset + 1] & 0xff) << 8 | (data[offset + 0] & 0xff);
        } else {
            return (data[offset + 0] & 0xff) << 8 | (data[offset + 1] & 0xff);
        }
    }

    /**
     * Based on endianess will convert the data to a unsigned int. This will
     * return a 32 bit number, not a 64 bit number which is a long in java.
     * 
     * @param data
     *            containing the bytes
     * @param offset
     *            into data where data is located.
     * @return unsigned long value
     */
    public long getLong(byte[] data, int offset) {
        if (littleendian) {
            return (data[offset + 3] & 0xff) << 24 | (data[offset + 2] & 0xff) << 16 | (data[offset + 1] & 0xff) << 8 | (data[offset + 0] & 0xff);
        } else {
            return (data[offset + 0] & 0xff) << 24 | (data[offset + 1] & 0xff) << 16 | (data[offset + 2] & 0xff) << 8 | (data[offset + 3] & 0xff);
        }
    }

    /**
     * Based on endianess will convert the data to a unsigned long.
     * 
     * @param data
     *            containing the bytes
     * @param offset
     *            into data where data is located.
     * @return unsigned long value
     */
    public long getLong8(byte[] data, int offset) {
        if (littleendian) {
            return (long) (data[offset + 7] & 0xff) << 56 | (long) (data[offset + 6] & 0xff) << 48 | (long) (data[offset + 5] & 0xff) << 40 | (long) (data[offset + 4] & 0xff) << 32
                    | (long) (data[offset + 3] & 0xff) << 24 | (long) (data[offset + 2] & 0xff) << 16 | (long) (data[offset + 1] & 0xff) << 8 | (data[offset] & 0xff);
        } else {
            return (long) (data[offset] & 0xff) << 56 | (long) (data[offset + 1] & 0xff) << 48 | (long) (data[offset + 2] & 0xff) << 40 | (long) (data[offset + 3] & 0xff) << 32
                    | (long) (data[offset + 4] & 0xff) << 24 | (long) (data[offset + 5] & 0xff) << 16 | (long) (data[offset + 6] & 0xff) << 8 | (data[offset + 7] & 0xff);
        }
    }

    /**
     * Based on endianess will convert the data to a float.
     * 
     * @param data
     *            containing the bytes
     * @param offset
     *            into data where data is located.
     * @return double value
     */
    public float getFloat(byte[] data, int offset) {
        return Float.intBitsToFloat((int) getLong(data, offset));
    }

    /**
     * Based on endianess will convert the data to a double.
     * 
     * @param data
     *            containing the bytes
     * @param offset
     *            into data where data is located.
     * @return double value
     */
    public double getDouble(byte[] data, int offset) {
        long l;
        if (littleendian) {
            l = (long) (data[offset + 7] & 0xff) << 56 | (long) (data[offset + 6] & 0xff) << 48 | (long) (data[offset + 5] & 0xff) << 40 | (long) (data[offset + 4] & 0xff) << 32
                    | (long) (data[offset + 3] & 0xff) << 24 | (long) (data[offset + 2] & 0xff) << 16 | (long) (data[offset + 1] & 0xff) << 8 | (data[offset + 0] & 0xff);
        } else {
            l = (long) (data[offset + 0] & 0xff) << 56 | (long) (data[offset + 1] & 0xff) << 48 | (long) (data[offset + 2] & 0xff) << 40 | (long) (data[offset + 3] & 0xff) << 32
                    | (long) (data[offset + 4] & 0xff) << 24 | (long) (data[offset + 5] & 0xff) << 16 | (long) (data[offset + 6] & 0xff) << 8 | (data[offset + 7] & 0xff);
        }
        return Double.longBitsToDouble(l);
    }

    /**
     * Read in the data for a tiled tiff image. All the image data for one row
     * of tiles will be kept in the imagebyte[] buffer.
     */
    private class TiledDataReader {
        /** the buffer */
        byte[]  imagebyte   = new byte[0];
        /** current row of tiles */
        int     tilerow     = 0;
        int     total;
        int     len         = 0;
        /** the number of tiles across */
        long    TilesAcross = (ImageWidth + TileWidth - 1) / TileWidth;
        /** the number of tiles down */
        long    TilesDown   = (ImageLength + TileLength - 1) / TileLength;
        /** the current tile number */
        int     tileNum     = 0;
        int     loc         = 0;
        int     currow      = 0;
        boolean firsttime   = true;

        private void read(long offset) throws IOException {
            switch (Compression) {
            case 1:
                noCompressionRead(offset);
                break;

            // TODO compressed read

            default:
                if (firsttime) {
                    logger.warn("Can not handle compression [" + Compression + "].");
                    len = (int) (ImageWidth * SamplesPerPixel);
                    total = len;
                    loc = 0;
                    imagebyte = new byte[len];
                    firsttime = false;
                } else {
                    loc -= len;
                    total += len;
                }
            }
            ImageLoader.fireProgress((int) offset, size);
        }

        public byte getByte(int offset) throws IOException {
            read(offset);
            return imagebyte[loc + offset];
        }

        /**
         * 
         * @param start
         * @throws IOException
         */
        private void noCompressionRead(long start) throws IOException {
            while (start >= total) {
                // len = the number of bytes needed to hold an entire row of
                // tiles
                len = (int) (ImageWidth * TileLength);// (int) (TilesAcross *
                // TileWidth *
                // TileLength);
                total += len;
                if (start < total) {
                    // allocate space
                    if (imagebyte.length < len) {
                        imagebyte = new byte[len];
                    }

                    // int curLocation = 0;

                    // we are going to read in each row of tiles
                    // thus, we only need to keep one row of tiles at a time
                    // byte[][] row = new byte[(int) TilesAcross][(int)
                    // (TileLength *
                    // TileWidth)];
                    // a pointer to a tile in the row
                    byte[] tilepointer = new byte[(int) (TileLength * TileWidth)];

                    // now we want to read the data.

                    // for each row of tiles
                    // for (int down = 0; down < TilesDown - 1; down++) {

                    // if this is not the last row of tiles
                    // if(currow < (TilesDown-1)) {
                    // for each tile

                    // offset is the location of the first column of a tile
                    // in a scan row.
                    // so, the first tile will have offset 0, the second tile
                    // will have offset of TileWidth, third TileWidth*2
                    long offset = 0;

                    // for each tile in a row
                    for (int across = 0; across < TilesAcross; across++) {
                        // read the tile in

                        // move the seek pointer to the offset
                        file.seek(TileOffsets[tileNum]);
                        // read the data in
                        // file.read(row[across], 0, (int)
                        // TileByteCounts[tileNum]);
                        // read in a tile
                        file.read(tilepointer, 0, (int) TileByteCounts[tileNum]);

                        // now, copy the scan lines of the tile into imagebyte

                        // the number of rows of image data in the tile
                        long numRowsInTile = TileLength;
                        if (currow == TilesDown - 1) {
                            // numRowsInTile is the number of non-padding rows
                            // if we are in the last row, there could be
                            // padding.

                            // this is the number of rows for the tiles that are
                            // completely shown
                            long numCompleteTileRows = (TileLength * (TilesDown - 1));
                            numRowsInTile = ImageLength - numCompleteTileRows;
                        }
                        // the number of columns of image data in the tile
                        long numColsInTile = TileWidth;
                        if (across == TilesAcross - 1) {
                            // numColsInTile will be the number of non-padding
                            // cols
                            // if this is the last column, there could be
                            // padding

                            long numCompleteTileCols = (TileWidth * (TilesAcross - 1));
                            numColsInTile = ImageWidth - numCompleteTileCols;
                        }

                        // for each row of the tile
                        for (int tilerow = 0; tilerow < numRowsInTile; tilerow++) {
                            // we want to copy the entire row into the correct
                            // spot
                            // in imagebyte

                            // try {
                            System.arraycopy(tilepointer, (int) (tilerow * numColsInTile), imagebyte, (int) ((tilerow * ImageWidth) + offset), (int) numColsInTile);
                            /*
                             * } catch(RuntimeException ex) {
                             * System.out.println(
                             * "tile len: "+tilepointer.length);
                             * System.out.println
                             * ("tile start: "+(tilerownumColsInTile));
                             * System.out.println("imagebyte len:
                             * "+imagebyte.length); System.out.println("
                             * imagebyte start:
                             * "+((tilerow*ImageWidth)+offset)); System.out.println("
                             * offset: "+offset);
                             * System.out.println("numColsInTile: "
                             * +numColsInTile);
                             * System.out.println("numRowsInTile:
                             * "+numRowsInTile); System.out.println("tilenum:
                             * "+tileNum); System.out.println("tilerow:
                             * "+tilerow); throw ex; }
                             */
                        }

                        offset += numColsInTile;
                        tileNum++;
                    }

                    // now we have a row of tiles in memory. copy their contents
                    // to imagebyte
                    /*
                     * for (int pixelrow = 0; pixelrow < TileLength; pixelrow++)
                     * { for (int tilecol = 0; tilecol < TilesAcross - 1;
                     * tilecol++) { // point to a specific tile tilepointer =
                     * row[tilecol]; // copy the row of the tile into _image
                     * System.arraycopy(tilepointer, (int) (pixelrow TileWidth),
                     * imagebyte, curLocation, (int) TileWidth); curLocation +=
                     * TileWidth; } // pick up the last tile. it is a special
                     * case because it // could have padding tilepointer = row[
                     * (int) (TilesAcross - 1)]; int sz = (int) (ImageWidth -
                     * (TilesAcross - 1) TileWidth);
                     * System.arraycopy(tilepointer, (int) (pixelrow TileWidth),
                     * imagebyte, curLocation, sz); curLocation += sz; }
                     * currow++; } else { // pick up the last row of tiles, it
                     * is a special case because // it could have padding on the
                     * bottom, as well as on the right of the // last tile for
                     * (int across = 0; across < TilesAcross; across++) { //
                     * read the tile in file.seek(TileOffsets[tileNum]);
                     * file.read(row[across], 0, (int) TileByteCounts[tileNum]);
                     * tileNum++; } // now we have the last row of tiles in
                     * memory. copy their // contents to imagebyte // find the
                     * number of rows that contain image data. all other // rows
                     * will just be padding int numRowsToRead = (int)
                     * (ImageLength - (TilesDown - 1) TileLength); for (int
                     * pixelrow = 0; pixelrow < numRowsToRead; pixelrow++) { for
                     * (int tilecol = 0; tilecol < TilesAcross - 1; tilecol++) {
                     * tilepointer = row[tilecol]; System.arraycopy(tilepointer,
                     * (int) (pixelrow TileWidth), imagebyte, curLocation, (int)
                     * TileWidth); curLocation += TileWidth; } // now pick up
                     * the lower right corner tile. it could have // padding
                     * both on the bottom and on the right tilepointer = row[
                     * (int) (TilesAcross - 1)]; int sz = (int) (ImageWidth -
                     * (TilesAcross - 1) TileWidth);
                     * System.arraycopy(tilepointer, (int) (pixelrow TileWidth),
                     * imagebyte, curLocation, sz); curLocation += sz; } }
                     */
                } // if
                loc = len - total;
            } // while
        } // noCompressionRead
    }

    /**
     * Simple class to read samples, this will hide any compression mechanisms
     * that are supported. This class only understands forward reading, no
     * jumping through the dataset. All data for the image is read in order.
     */
    class DataReader {
        private long    strip       = 0;
        private long    len         = 0;
        private long    loc         = 0;
        private long    total       = 0;
        private byte[]  imagebyte   = new byte[0];
        private long    stripoffset = 0;
        private byte[]  compressed  = new byte[0];
        private boolean firsttime   = true;

        private void read(long offset) throws IOException {
            switch (Compression) {
            case 1:
                noCompressionRead(offset);
                break;

            case 4:
                ccitRead(offset);
                break;

            case 5:
                lzwRead(offset);
                break;

            case 32773:
                unPackBitsRead(offset);
                break;

            default:
                if (firsttime) {
                    logger.warn("Can not handle compression [" + Compression + "].");
                    len = (int) (ImageWidth * SamplesPerPixel);
                    total = len;
                    loc = 0;
                    imagebyte = new byte[(int) len];
                    firsttime = false;
                } else {
                    loc -= len;
                    total += len;
                }
                break;
            }
            ImageLoader.fireProgress((int) offset, size);
        }

        public byte getByte(long offset) throws IOException {
            read(offset);
            try {
                return imagebyte[(int) (loc + offset)];
            } catch (ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
                return 0;
            }
        }

        /**
         * Based on endianess will convert the data to a unsigned short.
         * 
         * @param data
         *            containing the bytes
         * @param offset
         *            into data where data is located.
         * @return unsigned short value
         */
        public int getShort(long offset) throws IOException {
            if (littleendian) {
                return (getByte(offset + 1) & 0xff) << 8 | (getByte(offset + 0) & 0xff);
            } else {
                return (getByte(offset + 0) & 0xff) << 8 | (getByte(offset + 1) & 0xff);
            }
        }

        /**
         * Based on endianess will convert the data to a unsigned int. This will
         * return a 32 bit number, not a 64 bit number which is a long in java.
         * 
         * @param data
         *            containing the bytes
         * @param offset
         *            into data where data is located.
         * @return unsigned long value
         */
        public long getLong(long offset) throws IOException {
            if (littleendian) {
                return (getByte(offset + 3) & 0xff) << 24 | (getByte(offset + 2) & 0xff) << 16 | (getByte(offset + 1) & 0xff) << 8 | (getByte(offset + 0) & 0xff);
            } else {
                return (getByte(offset + 0) & 0xff) << 24 | (getByte(offset + 1) & 0xff) << 16 | (getByte(offset + 2) & 0xff) << 8 | (getByte(offset + 3) & 0xff);
            }
        }

        public float getFloat(int offset) throws IOException {
            long l = getLong(offset);
            return Float.intBitsToFloat((int) l);
        }

        // -------------------------------------------------------------------
        // No compression
        // -------------------------------------------------------------------
        /**
         * Read imagedata from disk. This will skip any data if possible
         * minimizing disk access. The data is assumed not to be compressed.
         * 
         * @param start
         *            the location in the data where to start.
         * @throws IOException
         *             if an error reading data happened.
         */
        private void noCompressionRead(long offset) throws IOException {
            while (offset >= total) {
                // number of bytes in strip
                len = (int) StripByteCounts[(int) strip] - stripoffset;
                if (len > 20000) {
                    len = 20000;
                }
                total += len;
                if (offset < total) {
                    // allocate space
                    if (imagebyte.length < len) {
                        imagebyte = new byte[(int) len];
                    }
                    // position in file.
                    file.seek(StripOffsets[(int) strip] + stripoffset);
                    // bytes are the samples, read into image.
                    if (file.read(imagebyte, 0, (int) len) != len) {
                        throw (new IOException("Could not read enough bytes."));
                    }
                }
                loc = len - total;
                stripoffset += len;
                if (stripoffset >= StripByteCounts[(int) strip]) {
                    strip++;
                    stripoffset = 0;
                }
            }
        }

        // -------------------------------------------------------------------
        // CCIT compression
        // -------------------------------------------------------------------
        /**
         * Read imagedata from disk. This will skip any data if possible
         * minimizing disk access. The data is assumed to be compressed using
         * CCIT.
         * 
         * @param start
         *            the location in the data where to start.
         * @throws IOException
         *             if an error reading data happened.
         */
        private void ccitRead(long offset) throws IOException {
            while (offset >= total) {
                // number of compressed bytes in strip
                int clen = (int) StripByteCounts[(int) strip];
                // allocate space
                if (compressed.length < clen) {
                    compressed = new byte[clen];
                }
                // position in file.
                file.seek(StripOffsets[(int) strip]);
                // bytes are the samples, read into image.
                if (file.read(compressed, 0, clen) != clen) {
                    throw (new IOException("Could not read enough bytes."));
                }

                long lines = RowsPerStrip;
                if (lines == -1) {
                    lines = ImageLength;
                }
                long stride = (BitsPerSample[0] * ImageWidth + 7) / 8;
                len = (stride * lines);
                if (imagebyte.length < len) {
                    imagebyte = new byte[(int) len];
                }

                // update counters
                total += len;
                loc = len - total;
                strip++;

                // decompress
                TIFFFaxDecoder decoder = new TIFFFaxDecoder(1, (int) ImageWidth, (int) ImageLength);
                switch (Compression) {
                case 4:
                    decoder.decodeT6(imagebyte, compressed, 0, (int) lines, 0);
                    break;
                default:
                    throw (new IOException(String.format("CCIT does not support compression %d.", Compression)));
                }
            }
        }

        // -------------------------------------------------------------------
        // LZW compression
        // -------------------------------------------------------------------
        /**
         * Utility method for decoding an LZW-compressed image strip. Adapted
         * from the TIFF 6.0 Specification:
         * http://partners.adobe.com/asn/developer/pdfs/tn/TIFF6.pdf (page 61)
         * 
         * <pre>
         * while ((Code = GetNextCode()) != EoiCode) {
         *     if (Code == ClearCode) {
         *         InitializeTable();
         *         Code = GetNextCode();
         *         if (Code == EoiCode)
         *             break;
         *         WriteString(StringFromCode(Code));
         *         OldCode = Code;
         *     } // end of ClearCode case
         *     else {
         *         if (IsInTable(Code)) {
         *             WriteString(StringFromCode(Code));
         *             AddStringToTable(StringFromCode(OldCode) + FirstChar(StringFromCode(Code)));
         *             OldCode = Code;
         *         } else {
         *             OutString = StringFromCode(OldCode) + FirstChar(StringFromCode(OldCode));
         *             WriteString(OutString);
         *             AddStringToTable(OutString);
         *             OldCode = Code;
         *         }
         *     } // end of not-ClearCode case
         * } // end of while loop
         * </pre>
         * 
         * The function GetNextCode() retrieves the next code from the LZW-coded
         * data. It must keep track of bit boundaries. It knows that the first
         * code that it gets will be a 9-bit code. We add a table entry each
         * time we get a code. So, GetNextCode() must switch over to 10-bit
         * codes as soon as string #510 is stored into the table. Similarly, the
         * switch is made to 11-bit codes after #1022 and to 12-bit codes after
         * #2046.
         * 
         * The function StringFromCode() gets the string associated with a
         * particular code from the string table. The function
         * AddStringToTable() adds a string to the string table. The "+" sign
         * joining the two parts of the argument to AddStringToTable indicates
         * string concatenation. StringFromCode() looks up the string associated
         * with a given code. WriteString() adds a string to the output stream.
         */
        private void lzwRead(long offset) throws IOException {
            while (offset >= total) {
                // number of compressed bytes in strip
                int clen = (int) StripByteCounts[(int) strip];
                // allocate space
                if (compressed.length < clen) {
                    compressed = new byte[clen];
                }
                // position in file.
                file.seek(StripOffsets[(int) strip]);
                // bytes are the samples, read into image.
                if (file.read(compressed, 0, clen) != clen) {
                    throw (new IOException("Could not read enough bytes."));
                }

                long lines = RowsPerStrip;
                if (lines == -1) {
                    lines = ImageLength;
                }
                long stride = (ImageWidth * BitsPerSample[0] + 7) / 8;
                len = SamplesPerPixel * lines * stride;
                if (imagebyte.length < len) {
                    imagebyte = new byte[(int) len];
                }

                // actual decoding
                TIFFLZWDecoder decoder = new TIFFLZWDecoder((int) ImageWidth, Predictor, SamplesPerPixel);
                decoder.decode(compressed, imagebyte, (int) lines);

                // update counters
                total += len;
                loc = len - total;
                strip++;
            }
        }

        // -------------------------------------------------------------------
        // PACKBITS
        // -------------------------------------------------------------------
        /**
         * Read data from the file up until the point we are interested in. This
         * will also decompress the data resulting into a filled in imagebyte
         * with decompressed data. Due to compression no data can be skipped,
         * and all data need to be read.
         * <p/>
         * This will use the unPackBits algorithm:
         * 
         * <pre>
         * forall (data) do
         *   read next byte
         *   if n == -128 ignore
         *   else if n &gt;= 0 copy next n+1 bytes
         *   else copy next byte -n + 1 times.
         * done
         * </pre>
         * 
         * @param offset
         *            the location in the data where to start.
         * @throws IOException
         *             if an error reading data happened.
         */
        private void unPackBitsRead(long offset) throws IOException {
            int clen, i, n, j;

            while (offset >= total) {
                // number of compressed bytes in strip
                clen = (int) StripByteCounts[(int) strip];
                // allocate space
                if (compressed.length < clen) {
                    compressed = new byte[clen];
                }
                // position in file.
                file.seek(StripOffsets[(int) strip]);
                // bytes are the samples, read into image.
                if (file.read(compressed, 0, clen) != clen) {
                    throw (new IOException("Could not read enough bytes."));
                }
                // count bytes
                len = 0;
                for (i = 0; i < clen; i++) {
                    n = compressed[i];
                    if (n >= 0) {
                        len += n + 1;
                        i += n + 1;
                    } else if (n != -128) {
                        i++;
                        len += (-n) + 1;
                    }
                }
                total += len;
                // if needed do real work
                if (offset < total) {
                    // allocate space
                    if (imagebyte.length < len) {
                        imagebyte = new byte[(int) len];
                    }
                    for (j = 0, i = 0; i < clen; i++) {
                        n = compressed[i];
                        if (n >= 0) {
                            i++;
                            System.arraycopy(compressed, i, imagebyte, j, n + 1);
                            j += n + 1;
                            i += n;
                        } else if (n != -128) {
                            n = -n;
                            i++;
                            Arrays.fill(imagebyte, j, j + n + 1, compressed[i]);
                            j += n + 1;
                        }
                    }
                }
                loc = len - total;
                strip++;
            }
        }
    }
}
