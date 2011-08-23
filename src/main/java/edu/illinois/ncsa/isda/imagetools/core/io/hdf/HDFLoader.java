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
package edu.illinois.ncsa.isda.imagetools.core.io.hdf;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import ncsa.hdf.object.Datatype;
import ncsa.hdf.object.FileFormat;
import ncsa.hdf.object.HObject;
import ncsa.hdf.object.ScalarDS;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageException;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObjectByte;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObjectDouble;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObjectFloat;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObjectInt;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObjectLong;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObjectShort;
import edu.illinois.ncsa.isda.imagetools.core.datatype.SubArea;
import edu.illinois.ncsa.isda.imagetools.core.geo.Datum;
import edu.illinois.ncsa.isda.imagetools.core.geo.GeoGraphicCoordinateSystem;
import edu.illinois.ncsa.isda.imagetools.core.geo.ModelPoint;
import edu.illinois.ncsa.isda.imagetools.core.geo.Projection;
import edu.illinois.ncsa.isda.imagetools.core.geo.ProjectionConvert;
import edu.illinois.ncsa.isda.imagetools.core.geo.RasterPoint;
import edu.illinois.ncsa.isda.imagetools.core.geo.TiePoint;
import edu.illinois.ncsa.isda.imagetools.core.geo.Projection.ProjectionType;
import edu.illinois.ncsa.isda.imagetools.core.geo.projection.NewSinusoidal;
import edu.illinois.ncsa.isda.imagetools.core.io.FileChooser;
import edu.illinois.ncsa.isda.imagetools.core.io.ImageLoader;
import edu.illinois.ncsa.isda.imagetools.core.io.ImageReader;
import edu.illinois.ncsa.isda.imagetools.core.io.ImageWriter;
import edu.illinois.ncsa.isda.imagetools.core.io.LoadSaveCheck;
import edu.illinois.ncsa.isda.imagetools.core.io.tiff.GeoEntry;

/**
 * Load an HDF image. Images in a HDF file are kept inside another file. The
 * format for the filename is HDFfile#filename, if no additinal filename is
 * given load the first image file or if an HDF file is already open load the
 * image from that HDF file.
 */
public class HDFLoader implements ImageReader, ImageWriter, LoadSaveCheck {
    static private Log logger = LogFactory.getLog(HDFLoader.class);

    /**
     * List of attributes to be ignored, these will not be put into the
     * properties hashmap.
     */
    static String[]    ignore = new String[] { "CLASS", "IMAGE_VERSION", "IMAGE_SUBCLASS", "IMAGE_COLORMODEL", "IMAGE_MINMAXRANGE", "IMAGE_TRANSPARENCY", "PALETTE", "INTERLACE_MODE", "serialized" };

    /**
     * Default constructor, will check to see if FileFormat exists (HDF libs).
     */
    public HDFLoader() {
        Enumeration e = FileFormat.getFileFormatKeys();
        if ((e == null) || !e.hasMoreElements()) {
            throw (new IllegalArgumentException("No Fileformats registered."));
        }
        FileChooser.addChecker(this);
    }

    public String check(String filename, boolean load) {
        if (HDF.isHDF(filename)) {
            try {
                FileChooserHDF hdfdialog;
                if (load) {
                    hdfdialog = new FileChooserHDF(filename, FileChooserHDF.LOAD);
                } else {
                    hdfdialog = new FileChooserHDF(filename, FileChooserHDF.SAVE);
                }
                filename = hdfdialog.showDialog();
                return filename;
            } catch (Throwable thr) {
                logger.error(thr);
                return null;
            }
        } else {
            return filename;
        }
    }

    /**
     * Checks to see if the filename is build up using the hdf#file notation.
     * The hdf part of the filename has to end with either .h5, .h4 or .hdf.
     * 
     * @param filename
     *            of the file to be read.
     * @param hdr
     *            ignored.
     * @return true if the file can be read by this class.
     */
    public boolean canRead(String filename, byte[] hdr) {
        String[] parts = filename.split("#", 2);

        // need at least 2 parts
        if (parts.length != 2) {
            return false;
        }

        // first part should end with h5, h4 or hdf
        if (!HDF.isHDF(parts[0])) {
            return false;
        }

        // finally the second part should have some length
        return parts[1].length() > 0;
    }

    /**
     * This function will read the file and return an imageobject that contains
     * the file.
     * 
     * @param filename
     *            of the file to be read
     * @param subarea
     *            of the file to be read, null if full image is to be read.
     * @return the file as an imageobject.
     * @throws IOException
     *             if the file could not be read.
     */
    public ImageObject readImage(String filename, SubArea subarea, int sampling) throws IOException {
        return loadImage(filename, subarea, sampling, false);
    }

    /**
     * This function will read the file and return an imageobject that contains
     * the information of the image but not the imagedata itself.
     * 
     * @param filename
     *            of the file to be read
     * @return the file as an imageobject except of the imagedata.
     * @throws IOException
     *             if the file could not be read.
     */
    public ImageObject readImageHeader(String filename) throws IOException {
        return loadImage(filename, new SubArea(), 1, true);
    }

    /**
     * Return a list of extentions this class can read.
     * 
     * @return a list of extentions that are understood by this class.
     */
    public String[] readExt() {
        return new String[] { "h4", "hdf", "h5" };
    }

    /**
     * Return the description of the reader (or writer).
     * 
     * @return decription of the reader (or writer)
     */
    public String getDescription() {
        return "HDF files";
    }

    static public void readEOSAttributes(ImageObject geoImage, String filename) {
        // composite file (maybe), split into 2 filenames
        String names[] = filename.split("#", 2);

        try {
            // Get the top-level group.  The EOS metadata are attributes of this group.
            HObject group = (HObject) HDF.openFile(names[0] + "#/", false, false);

            // Get the attributes of the top-level group.
            // This is a hash table where the key is the name of the property,
            // the value is the property.
            // The keys will be :
            //      CoreMetadata.0
            //      StructMetadata.0
            //      ArchiveMetadata.0
            //      HDFEOSVersion
            HashMap props = HDF.readAttributes(null, group, null);

            String STRUCT = "StructMetadata.0";
            double xDim, yDim, radius;
            double[] upperLeft, lowerRight;
            String projection;

            // we have the props.  use vikas' code here.
            String[] str = { "XDim", "YDim", "UpperLeftPointMtrs", "LowerRightMtrs", "Projection", "ProjParams" };
            //System.out.println(props.get(STRUCT));
            ParseObject parser = new ParseObject(props.get(STRUCT), str);
            Hashtable hashTable = parser.parseValues();
            //System.out.println(hashTable.size());
            xDim = Double.parseDouble((String) hashTable.get(str[0]));
            yDim = Double.parseDouble((String) hashTable.get(str[1]));

            String pr = (String) hashTable.get(str[2]);
            String[] mp = pr.split(",", 2);
            upperLeft = new double[2];
            lowerRight = new double[2];

            upperLeft[0] = Double.parseDouble(mp[0].substring(1));
            upperLeft[1] = Double.parseDouble(mp[1].substring(0, mp[1].length() - 1));

            pr = (String) hashTable.get(str[3]);
            mp = pr.split(",", 2);

            lowerRight[0] = Double.parseDouble(mp[0].substring(1));
            lowerRight[1] = Double.parseDouble(mp[1].substring(0, mp[1].length() - 1));

            double minX = upperLeft[0];
            double maxY = upperLeft[1];
            double maxX = lowerRight[0];
            double minY = lowerRight[1];

            System.out.println("UL[0]: " + upperLeft[0] + " UL[1]: " + upperLeft[1] + " LR[0]: " + lowerRight[0] + " LR[1]: " + lowerRight[1]);
            System.out.println("xDim : " + xDim + "   yDim : " + yDim);

            projection = (String) hashTable.get(str[4]);
            System.out.println("Projection" + projection);
            if (projection.equals("GCTP_GEO")) {
                double scalex = 360.0 / xDim;
                double scaley = 180.0 / yDim;
                TiePoint tp = new TiePoint(new ModelPoint(-180, 90), new RasterPoint(0, 0), scalex, -scaley, 1.0);
                GeoGraphicCoordinateSystem geogcs = new GeoGraphicCoordinateSystem(Datum.WGS_1984);
                Projection geoProj = Projection.getProjection(ProjectionType.Geographic, geogcs);
                geoProj.setTiePoint(tp);
                geoImage.setProperty(ImageObject.GEOINFO, ProjectionConvert.toOld(geoProj));
            } else {
                pr = (String) hashTable.get(str[5]);
                String[] rpr = pr.split(",");

                radius = Double.parseDouble(rpr[0].substring(1));

                System.out.println("Radius =" + radius);

                // use the upper left as the tie point.
                // compute the row and column resolution
                double colres = (maxX - minX) / xDim;
                double rowres = (maxY - minY) / yDim;

                //Sinusoidal sinProj = new Sinusoidal(radius, geoImage.getNumRows(), geoImage.getNumCols(),
                //             upperLeft, lowerRight);
                NewSinusoidal sinProj = new NewSinusoidal();
                sinProj.setScaleX(lowerRight[0] - upperLeft[0]);
                sinProj.setScaleY(upperLeft[1] - lowerRight[1]);
                sinProj.setNumCols(geoImage.getNumCols());
                sinProj.setNumRows(geoImage.getNumRows());

                // set the model type to be GeoImageObject.SINUSOIDAL
                //

                // easting insertion value
                sinProj.SetEastingInsertionValue(minX);
                sinProj.setModelSpaceX(minX);
                sinProj.SetRasterSpaceI(0);

                // northing insertion value
                sinProj.SetNorthingInsertionValue(maxY);
                sinProj.SetModelSpaceY(maxY);
                sinProj.SetRasterSpaceJ(0);

                // set the resolutions
                sinProj.SetColumnResolution(colres);
                //sinProj.SetRowResolution(-rowres);
                sinProj.SetRowResolution(rowres);

                // set the radius
                sinProj.setRadius(radius);

                //sinProj.setESquared(0.006766866);
                sinProj.setCenterLong(0);
                sinProj.setUnit(GeoEntry.Linear_Meter);
                // set the center lat
                //sinProj.SetGeoProjCenterLat(0);
                //sinProj.SetGeoProjCenterLng(0);

                // LAM this is a hack--- should ensure that the ellipsoid is a sphere.
                //sinProj.SetEllipsoidName("Sphere");
                geoImage.setProperty(ImageObject.GEOINFO, sinProj);
            }
        } catch (Throwable thr) {
            logger.error("Error loading EOS attributes", thr);
        }
    }

    private ImageObject loadImage(String filename, SubArea subarea, int sampling, boolean header) throws IOException {
        ImageObject imageobject = null;
        boolean fileIsEOS = HDF.isEOS(filename);
        Object obj = HDF.openFile(filename, false, false);
        if (obj == null) {
            throw (new IOException("Could not find image with name " + filename + ". Did you forget path after HDF file?"));
        }
        if (!(obj instanceof ScalarDS)) {
            throw (new IOException("Selected file (" + filename + ") is not a scalarDS"));
        }
        ScalarDS img = (ScalarDS) HDF.openFile(filename, false, true);
        img.init();

        // do some setup
        long selected[] = img.getSelectedDims();
        long stride[] = img.getStride();
        int index[] = img.getSelectedIndex();

        // sanity check on dimensions
        if (selected.length <= 1) {
            try {
                img.getFileFormat().close();
            } catch (Exception exc) {
                logger.error(exc);
            }
            throw (new IOException("Image needs at least 2 dimensions."));
        }

        // subsamp
        if (sampling != 1) {
            stride[0] = sampling;
            stride[1] = sampling;
            if (stride.length > 2) {
                stride[2] = 1;
            }
        } else {
            stride[0] = 1;
            stride[1] = 1;
            if (stride.length > 2) {
                stride[2] = 1;
            }
        }

        // subarea
        if (subarea != null) {
            long start[] = img.getStartDims();
            long dim[] = img.getDims();
            start[index[0]] = subarea.getRow();
            start[index[1]] = subarea.getCol();
            if (start.length > 2) {
                if (subarea.getNumBands() == 0) {
                    start[index[2]] = 0;
                } else {
                    start[index[2]] = subarea.getFirstBand();
                }
            }
            selected[index[0]] = subarea.getHigh() / stride[0];
            selected[index[1]] = subarea.getWide() / stride[1];
            if (selected.length > 2) {
                if (subarea.getNumBands() == 0) {
                    selected[index[2]] = dim[index[2]] / stride[2];
                } else {
                    selected[index[2]] = subarea.getNumBands() / stride[2];
                }
            }
        } else {
            long start[] = img.getStartDims();
            long dim[] = img.getDims();
            start[0] = 0;
            start[1] = 0;
            if (start.length > 2) {
                start[2] = 0;
            }
            selected[index[0]] = dim[index[0]] / stride[0];
            selected[index[1]] = dim[index[1]] / stride[1];
            if (selected.length > 2) {
                selected[index[2]] = dim[index[2]] / stride[2];
            }
        }

        if (selected.length > 3) {
            for (int i = 3; i < selected.length; i++) {
                selected[i] = 1;
            }
            // need to set this, how?
            //            try {
            //                img.getFileFormat().close();
            //            } catch (Exception exc) {
            //                logger.error(exc);
            //            }
            //            throw(new IOException("Need to set selectedindex, not sure how."));

        }

        // extract some useful parameters
        int h = (int) selected[index[0]];
        int w = (int) selected[index[1]];
        int d = 1;
        if (selected.length >= 3) {
            d = (int) selected[index[2]];
        }

        // if it is an image, life is good
        if (img.isImage()) {
            d = 3;
            // create imageobject
            imageobject = new ImageObjectByte(h, w, d, header);

            // read attributes
            try {
                HashMap props = imageobject.getProperties();
                props = HDF.readAttributes(props, img, ignore);
                props = HDF.readProperties(props, filename);
                imageobject.setProperties(props);

                if (fileIsEOS) {
                    System.out.println("Reading EOS attributes");
                    readEOSAttributes(imageobject, filename);
                }

            } catch (Exception exc) {
                logger.error(exc);
            }

            // done if we only need the header
            if (header) {
                try {
                    img.getFileFormat().close();
                } catch (Exception exc) {
                    logger.error(exc);
                }
                return imageobject;
            }

            // read image
            byte[] imgdata = null;
            try {
                imgdata = img.readBytes();
            } catch (Exception exc) {
                logger.error(exc);
            }

            // can have either true color or palette
            if (img.isTrueColor()) {
                int idx1 = 0;
                int idx2 = 0;
                int r, c, b;
                int size = imageobject.getSize();

                switch (img.getInterlace()) {
                case ScalarDS.INTERLACE_PIXEL:
                    imageobject.setData(imgdata);
                    break;

                case ScalarDS.INTERLACE_LINE:
                    for (r = 0; r < imageobject.getNumRows(); r++) {
                        for (c = 0; c < imageobject.getNumCols(); c++) {
                            ImageLoader.fireProgress(idx1, size);
                            for (b = 0; b < imageobject.getNumBands(); b++) {
                                idx2 = (b * imageobject.getNumCols() + r) * imageobject.getNumRows() + c;
                                imageobject.set(idx1++, imgdata[idx2]);
                            }
                        }
                    }
                    break;

                case ScalarDS.INTERLACE_PLANE:
                    for (r = 0; r < imageobject.getNumRows(); r++) {
                        for (c = 0; c < imageobject.getNumCols(); c++) {
                            ImageLoader.fireProgress(idx1, size);
                            for (b = 0; b < imageobject.getNumBands(); b++) {
                                idx2 = (b * imageobject.getNumRows() + r) * imageobject.getNumCols() + c;
                                imageobject.set(idx1++, imgdata[idx2]);
                            }
                        }
                    }
                    break;
                }
            } else {
                int size = img.getWidth() * img.getHeight();
                byte[][] pal = img.getPalette();
                int idx_src, idx_dst, b;

                for (idx_src = 0, idx_dst = 0; idx_src < size; idx_src++) {
                    ImageLoader.fireProgress(idx_src, size);
                    for (b = 0; b < d; b++, idx_dst++) {
                        imageobject.set(idx_dst, pal[b][imgdata[idx_src] & 0xff]);
                    }
                }
            }

        } else {

            // asssume the scalar is raw image data

            // convert HDF datatype to imageobject type
            Datatype datatype = img.getDatatype();
            switch (datatype.getDatatypeClass()) {
            case Datatype.CLASS_CHAR:
                imageobject = new ImageObjectByte(h, w, d, header);
                break;

            case Datatype.CLASS_INTEGER:
                switch (datatype.getDatatypeSize()) {
                case 1:
                    imageobject = new ImageObjectByte(h, w, d, header);
                    break;
                case 2:
                    imageobject = new ImageObjectShort(h, w, d, header);
                    break;
                case 4:
                    imageobject = new ImageObjectInt(h, w, d, header);
                    break;
                case 8:
                    imageobject = new ImageObjectLong(h, w, d, header);
                    break;
                default:
                    try {
                        img.getFileFormat().close();
                    } catch (Exception exc) {
                        logger.error(exc);
                    }
                    throw (new IOException("Can't load datatype."));
                }
                break;

            case Datatype.CLASS_FLOAT:
                switch (datatype.getDatatypeSize()) {
                case 4:
                    imageobject = new ImageObjectFloat(h, w, d, header);
                    break;
                case 8:
                    imageobject = new ImageObjectDouble(h, w, d, header);
                    break;
                default:
                    try {
                        img.getFileFormat().close();
                    } catch (Exception exc) {
                        logger.error(exc);
                    }
                    throw (new IOException("Can't load datatype."));
                }
                break;

            default:
                logger.warn(datatype.getDatatypeDescription());
                try {
                    img.getFileFormat().close();
                } catch (Exception exc) {
                    logger.error(exc);
                }
                throw (new IOException("Can't load datatype."));
            }

            // read attributes
            try {
                HashMap props = imageobject.getProperties();
                props = HDF.readAttributes(props, img, ignore);
                props = HDF.readProperties(props, filename);
                imageobject.setProperties(props);

                if (fileIsEOS) {
                    readEOSAttributes(imageobject, filename);
                }

            } catch (Exception exc) {
                logger.error(exc);
            }

            // if we only need the header we are done
            if (header) {
                try {
                    img.getFileFormat().close();
                } catch (Exception exc) {
                    logger.error(exc);
                }
                return imageobject;
            }

            // read the data
            try {
                Object data = img.read();
                imageobject.setData(data);
            } catch (Exception exc) {
                logger.error(exc);
                try {
                    img.getFileFormat().close();
                } catch (Exception exc2) {
                    logger.error(exc2);
                }
                throw (new IOException("Error loading data."));
            }
        }

        try {
            img.getFileFormat().close();
        } catch (Exception exc) {
            logger.error(exc);
        }
        imageobject.computeMinMax();
        /*
        if(fileIsEOS) {
                  System.out.println("Reading EOS attributes");
                  readEOSAttributes(imageobject, filename);
                }
        */
        return imageobject;
    }

    /**
     * Returns true if the class can write the file.
     * 
     * @param filename
     *            of the file to be written.
     * @return true if the file can be written by this class.
     */
    public boolean canWrite(String filename) {
        try {
            String[] parts = HDF.splitFileName(filename);
            return parts[0].endsWith("h5") || parts[0].endsWith("h4") || parts[0].endsWith("hdf");
        } catch (IOException exc) {
            return false;
        }
    }

    /**
     * This function will write the imageobject to a file.
     * 
     * @param filename
     *            of the file to be written.
     * @param imgobj
     *            the image image to be written.
     * @throws IOException
     *             if the file could not be written.
     */
    public void writeImage(String filename, ImageObject imgobj) throws IOException {
        long[] dims = null;
        dims = new long[] { imgobj.getNumRows(), imgobj.getNumCols(), imgobj.getNumBands() };
        int ncomp = imgobj.getNumBands();
        boolean image = ((imgobj.getNumBands() == 3) && (imgobj.getType() == ImageObject.TYPE_BYTE));
        HDF.writeData(filename, imgobj.getData(), image, dims, ncomp);
        HDF.writeProperties(filename, imgobj.getProperties());
    }

    /**
     * Return a list of extentions this class can read.
     * 
     * @return a list of extentions that are understood by this class.
     */
    public String[] writeExt() {
        return new String[] { "h5", "h4", "hdf", };
    }

    /**
     * This class can be used to parsed any kind of String. It has constructor
     * which accepts two arguments. Ist argument contains the Object which will
     * be parsed. 2nd arguments contains the arrays of string elements whose
     * values is retrived after parsing the String. The arrays of string should
     * contains the elments in the order they appeared on the Object or
     * (Structure Metadata).
     */
    private static class ParseObject {
        String[] parseText = {};
        String   str       = null;

        /**
         * Constructor
         */
        public ParseObject(Object obj, String[] parseText) {
            this.str = (String) obj;
            this.parseText = parseText;
        }

        /**
         * This function returns the values of the string elements to be parsed
         * in the form of hashtable. This function used StringTokenizer class to
         * parse the String which convert the String in to number of tokens.
         * 
         */
        public Hashtable parseValues() {
            String[] afterParsing = new String[parseText.length];
            Hashtable hashTable = new Hashtable();
            try {
                //System.out.println("I am inside parse object");
                Pattern pat = null;
                String x = "XDim";
                //CharSequence chr = (CharSequence)x;
                //Pattern compPattern = pat.compile(str);
                StringTokenizer st = new StringTokenizer(str, "\n");
                int countTokens = st.countTokens();
                int i = 0;
                int count = 0;
                String parse = "";
                //System.out.println("parse String length "+parseText.length);
                while (st.hasMoreTokens() && i < parseText.length) {
                    parse = (st.nextToken()).trim();

                    //System.out.println("parse = "+parse+ " parse Text = "+parseText[i] +"i ="+i);
                    if (parse.startsWith(parseText[i])) {
                        // System.out.println("I am inside if");
                        String pr = parse.replaceAll(parseText[i] + "=", "");
                        //System.out.println(pr);
                        afterParsing[i] = pr;
                        hashTable.put(parseText[i], afterParsing[i]);
                        //System.out.println("print what happen "+i+"parsing Line was "+ parseText[i] +" after parsing "+afterParsing[i]);
                        i++;
                    }
                    //System.out.println("I am inside loop"+  ++count);
                }
                //System.out.println("Numbers of tokens available= "+ countTokens);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return hashTable;
        }
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
