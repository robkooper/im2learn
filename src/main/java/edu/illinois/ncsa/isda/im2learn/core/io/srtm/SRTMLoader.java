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
package edu.illinois.ncsa.isda.im2learn.core.io.srtm;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.im2learn.core.GeoConvert;
import edu.illinois.ncsa.isda.im2learn.core.datatype.GeoInformation;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageException;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObjectShort;
import edu.illinois.ncsa.isda.im2learn.core.datatype.LimitValues;
import edu.illinois.ncsa.isda.im2learn.core.datatype.SubArea;
import edu.illinois.ncsa.isda.im2learn.core.geo.projection.Projection;
import edu.illinois.ncsa.isda.im2learn.core.io.ImageLoader;
import edu.illinois.ncsa.isda.im2learn.core.io.ImageReader;
import edu.illinois.ncsa.isda.im2learn.core.io.ImageWriter;

/**
 * 
 */
public class SRTMLoader implements ImageReader, ImageWriter {
    public static void main(String[] args) {
        SRTMLoader loader = new SRTMLoader();
        try {
            ImageObject io = loader.readImage("/home/clutter/NASA_Data/SRTM/w78n38.dt1", null, 1);
            Projection proj = (Projection) io.getProperty(ImageObject.GEOINFO);
            System.out.println(proj.GetMaxSouthLat());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private static final int sampleread = 10000;
    private static Log       logger     = LogFactory.getLog(SRTMLoader.class);

    protected GeoConvert     _geoConv   = null;

    /**
     * Returns true if the file has .hdr as extention. Since ENVI has header as
     * well, this loader should come after the ENVI loader to avoid false
     * exception to be thrown. The ENVI loader has a better detection of the
     * filetype.
     * 
     * @param filename
     *            ignored.
     * @param hdr
     *            the first 100 bytes of the file.
     * @return true if the file can be read by this class.
     */
    public boolean canRead(String filename, byte[] hdr) {
        if (filename.toLowerCase().endsWith(".dt1")) {
            return true;
        }
        return false;
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
     * @throws java.io.IOException
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
        return loadImage(filename, null, 1, true);
    }

    /**
     * Return a list of extentions this class can read.
     * 
     * @return a list of extentions that are understood by this class.
     */
    public String[] readExt() {
        return new String[] { "dt1" };
    }

    /**
     * Return the description of the reader (or writer).
     * 
     * @return decription of the reader (or writer)
     */
    public String getDescription() {
        return "SRTM files";
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

        ImageObject imgobj;// = loadImageHeader(headerfile, demheader);
        RandomAccessFile f0;
        try {
            f0 = new RandomAccessFile(filename, "r");
        } catch (FileNotFoundException e) {
            System.out.println("File not found " + filename);
            return null;
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Usage: InFileName");
            return null;
        }

        SRTMHeader srtmHeader = new SRTMHeader();
        srtmHeader.sampling = sampling;
        srtmHeader.subarea = subarea;

        ImageObject img = loadImageHeader(f0, srtmHeader);
        if (!header) {
            loadImageData(f0, srtmHeader, img);
        }
        return img;
    }

    /**
     * Read the header of the image. The header has key = value pairs. The value
     * can be stored between braces. If the value is an array, the array parts
     * are seperated by a comma.
     * 
     * @param f0
     *            the random access file
     * @param header
     *            the structure containing the header informatioon
     * @return an imageobject based on the header information
     * @throws IOException
     *             if the file could not be read correctly.
     */
    private ImageObject loadImageHeader(RandomAccessFile f0, SRTMHeader header) throws IOException {

        String s1;
        short tempShort;
        long _bytecount = 0;

        double d, m, s; // for representing degree, minutes, and seconds in longitude/latitude
        Double dValue;
        Integer iValue;

        byte[] buf;
        buf = new byte[100];

        // Start reading User Header Label (UHL)
        f0.read(buf, 0, 3); // reading byte 1-3
        _bytecount += 3;
        s1 = new String(buf, 0, 3);

        Projection geo = new Projection();
        GeoInformation geoInfo = new GeoInformation();
        geoInfo.SetModelType(97);
        _geoConv = new GeoConvert(geoInfo);

        if (!s1.equalsIgnoreCase("UHL")) {
            System.out.println("Error: file is not in SRTM DTED format (0-2 bytes)=" + s1);
            f0.close();
            return null;
        }
        f0.skipBytes(1); // skiping byte 4
        _bytecount += 1;

        f0.read(buf, 0, 8); // reading byte 5-12 - longitude of origin UpLeftMap
        s1 = new String(buf, 0, 8);
        _bytecount += 8;
        if (s1.endsWith("W") || s1.endsWith("E")) {
            dValue = new Double(s1.substring(0, 3));
            d = dValue.doubleValue();
            dValue = new Double(s1.substring(3, 5));
            m = dValue.doubleValue();
            dValue = new Double(s1.substring(5, 7));
            s = dValue.doubleValue();
            //this._LatOrigin = d + m/60.0 + s/(3600.0); // note : is this conversion correct ?
            if (s1.endsWith("W")) {

                geo.setInsertionX(_geoConv.DegMinSecToDecim(d, m, s) * (-1));
                //_geoImageObject.SetMaxWestLng( _geoConv.DegMinSecToDecim(d,m,s)*(-1) );
                //_geoImageObject.SetEastingInsertionValue( _geoConv.DegMinSecToDecim(d,m,s)*(-1) );
            } else {
                geo.setInsertionX(_geoConv.DegMinSecToDecim(d, m, s));
                //_geoImageObject.SetMaxWestLng( _geoConv.DegMinSecToDecim(d,m,s) );
                //_geoImageObject.SetEastingInsertionValue( _geoConv.DegMinSecToDecim(d,m,s) );
            }

        } else {
            System.out.println("Error: file is not in SRTM DTED format (5-12 bytes)=" + s1);
            f0.close();
            return null;
        }

        f0.read(buf, 0, 8); // reading byte 13-20 - latitude of origin
        s1 = new String(buf, 0, 8);
        _bytecount += 8;
        if (s1.endsWith("S") || s1.endsWith("N")) {
            dValue = new Double(s1.substring(0, 3));
            d = dValue.doubleValue();
            dValue = new Double(s1.substring(3, 5));
            m = dValue.doubleValue();
            dValue = new Double(s1.substring(5, 7));
            s = dValue.doubleValue();
            //this._LngOrigin = d + m/60.0 + s/(3600.0); // note : is this conversion correct ?
            geo.setInsertionY(_geoConv.DegMinSecToDecim(d, m, s));
            //_geoImageObject.SetMaxSouthLat( _geoConv.DegMinSecToDecim(d,m,s) );
            //_geoImageObject.SetNorthingInsertionValue( _geoConv.DegMinSecToDecim(d,m,s) );

        } else {
            System.out.println("Error: file is not in SRTM DTED format (13-20 bytes)=" + s1);
            f0.close();
            return null;
        }

        //

        f0.read(buf, 0, 4); // reading byte 21-24 - longitude spacing in  sec
        s1 = new String(buf, 0, 4);
        _bytecount += 4;

        dValue = new Double(s1);
        //Point2DDouble pts = new Point2DDouble(2);

        //pts.ptsDouble[1] = geo.GetMaxWestLng();
        //pts.ptsDouble[0] = geo.GetMaxSouthLat();
        //System.out.println("Start Reading Header");
        //Point2DDouble ptsConv = null;
        //ptsConv = _geoConv.LatLng2UTMNorthingEasting(pts);

        //pts.ptsDouble[1] += (dValue.doubleValue()/36000.0);

        //geo.SetColumnResolution(dValue.doubleValue()/36000.0);

        //this._XDimension = dValue.doubleValue()/(3600.0*10.0); // is this conversion correct?
        f0.read(buf, 0, 4); // reading byte 25-28 - latitude spacing in sec
        s1 = new String(buf, 0, 4);
        _bytecount += 4;
        dValue = new Double(s1);

        //pts.ptsDouble[0] += (dValue.doubleValue()/36000.0);
        //Point2DDouble ptsConv1 = null;
        //ptsConv1 = _geoConv.LatLng2UTMNorthingEasting(pts);

        //geo.SetRowResolution(dValue.doubleValue()/36000.0);

        // colum res
        //_geoImageObject.SetColumnResolution( Math.abs(ptsConv.ptsDouble[1] - ptsConv1.ptsDouble[1]) );
        //row res
        //_geoImageObject.SetRowResolution( Math.abs(ptsConv.ptsDouble[0] - ptsConv1.ptsDouble[0]) );

        //this._YDimension = dValue.doubleValue()/(3600.0*10.0); // is this conversion correct?

        f0.skipBytes(19);
        _bytecount += 19;// skip bytes from 29-47 - TODO - load accuracy of vertical measurements

        f0.read(buf, 0, 4);
        _bytecount += 4;
        s1 = new String(buf, 0, 4);// reading byte 48-51
        iValue = new Integer(s1);
        header.filencols = iValue.intValue();
        f0.read(buf, 0, 4);
        _bytecount += 4;
        s1 = new String(buf, 0, 4);// reading byte 52-55
        iValue = new Integer(s1);
        header.filenrows = iValue.intValue();

        // for subarea
        if (header.subarea != null) {
            header.ncols = header.subarea.width;
            header.nrows = header.subarea.height;
        } else {
            header.ncols = header.filencols;
            header.nrows = header.filenrows;
        }

        // for undersampling
        if (header.sampling != 1) {
            header.ncols = (int) Math.floor(header.ncols / (double) header.sampling);
            header.nrows = (int) Math.floor(header.nrows / (double) header.sampling);
        }

        geo.SetColumnResolution(1.0 / header.ncols);
        geo.SetRowResolution(-1.0 / header.nrows);

        geo.SetRasterSpaceI(0);
        geo.SetRasterSpaceJ(header.nrows - 1);
        geo.setNumCols(header.ncols);
        geo.setNumRows(header.nrows);

        ImageObject imageobject = new ImageObjectShort(header.nrows, header.ncols, 1, true);

        //if(_debugSRTMImage)
        //   System.out.println("number of Rows=" + _numInFileRows + "  number of Cols=" +  _numInFileCols +"\n");

        f0.skipBytes(80 - 56 + 1);
        _bytecount += 80 - 56 + 1;// skip byte 56-80
        // End reading User Header Label (UHL)
        ////////////////////////////////////////////////////////////////////////////////
        // Start reading Data Set Identification (DSI) Record
        f0.read(buf, 0, 3); // reading byte 1-3
        _bytecount += 3;
        s1 = new String(buf, 0, 3);

        if (!s1.equalsIgnoreCase("DSI")) {
            System.out.println("Error: file is not in SRTM DTED format (DSI 1-3 bytes)=" + s1);
            f0.close();
            return null;
        }
        f0.skipBytes(201);
        _bytecount += 201;// skip bytes 4-204

        // reading SW corner Lat/Lng
        f0.read(buf, 0, 7); // reading byte 205-211
        s1 = new String(buf, 0, 7);
        _bytecount += 7;
        if (s1.endsWith("N") || s1.endsWith("S")) {
            dValue = new Double(s1.substring(0, 2));
            d = dValue.doubleValue();
            dValue = new Double(s1.substring(2, 4));
            m = dValue.doubleValue();
            dValue = new Double(s1.substring(4, 6));
            s = dValue.doubleValue();
            //this._LatSW = d + m / 60.0 + s / (3600.0); // note : is this conversion correct ?
        } else {
            System.out.println("Error: file is not in SRTM DTED format (DSI 205-211 bytes)=" + s1);
            f0.close();
            return null;
        }
        f0.read(buf, 0, 8); // reading byte 212-219
        s1 = new String(buf, 0, 8);
        _bytecount += 8;
        if (s1.endsWith("W") || s1.endsWith("E")) {
            dValue = new Double(s1.substring(0, 3));
            d = dValue.doubleValue();
            dValue = new Double(s1.substring(3, 5));
            m = dValue.doubleValue();
            dValue = new Double(s1.substring(5, 7));
            s = dValue.doubleValue();
            //this._LngSW = d + m / 60.0 + s / (3600.0); // note : is this conversion correct ?
        } else {
            System.out.println("Error: file is not in SRTM DTED format (DSI 212-219 bytes)=" + s1);
            f0.close();
            return null;
        }

        // reading NW corner Lat/Lng
        f0.read(buf, 0, 7); // reading byte 220-226
        s1 = new String(buf, 0, 7);
        _bytecount += 7;
        if (s1.endsWith("N") || s1.endsWith("S")) {
            dValue = new Double(s1.substring(0, 2));
            d = dValue.doubleValue();
            dValue = new Double(s1.substring(2, 4));
            m = dValue.doubleValue();
            dValue = new Double(s1.substring(4, 6));
            s = dValue.doubleValue();
            //this._LatNW = d + m / 60.0 + s / (3600.0); // note : is this conversion correct ?
        } else {
            System.out.println("Error: file is not in SRTM DTED format (220-226 bytes)=" + s1);
            f0.close();
            return null;
        }
        f0.read(buf, 0, 8); // reading byte 227-234
        s1 = new String(buf, 0, 8);
        _bytecount += 8;
        if (s1.endsWith("W") || s1.endsWith("E")) {
            dValue = new Double(s1.substring(0, 3));
            d = dValue.doubleValue();
            dValue = new Double(s1.substring(3, 5));
            m = dValue.doubleValue();
            dValue = new Double(s1.substring(5, 7));
            s = dValue.doubleValue();
            //this._LngNW = d + m / 60.0 + s / (3600.0); // note : is this conversion correct ?
        } else {
            System.out.println("Error: file is not in SRTM DTED format (227-234 bytes)=" + s1);
            f0.close();
            return null;
        }

        // reading NE corner Lat/Lng
        f0.read(buf, 0, 7); // reading byte 235-241
        s1 = new String(buf, 0, 7);
        _bytecount += 7;
        if (s1.endsWith("N") || s1.endsWith("S")) {
            dValue = new Double(s1.substring(0, 2));
            d = dValue.doubleValue();
            dValue = new Double(s1.substring(2, 4));
            m = dValue.doubleValue();
            dValue = new Double(s1.substring(4, 6));
            s = dValue.doubleValue();
            //this._LatNE = d + m / 60.0 + s / (3600.0); // note : is this conversion correct ?
        } else {
            System.out.println("Error: file is not in SRTM DTED format (235-241 bytes)=" + s1);
            f0.close();
            return null;
        }
        f0.read(buf, 0, 8); // reading byte 242-249
        s1 = new String(buf, 0, 8);
        _bytecount += 8;
        if (s1.endsWith("W") || s1.endsWith("E")) {
            dValue = new Double(s1.substring(0, 3));
            d = dValue.doubleValue();
            dValue = new Double(s1.substring(3, 5));
            m = dValue.doubleValue();
            dValue = new Double(s1.substring(5, 7));
            s = dValue.doubleValue();
            //this._LngNE = d + m / 60.0 + s / (3600.0); // note : is this conversion correct ?
        } else {
            System.out.println("Error: file is not in SRTM DTED format (242-249 bytes)=" + s1);
            f0.close();
            return null;
        }

        // reading SE corner Lat/Lng
        f0.read(buf, 0, 7); // reading byte 250-256
        s1 = new String(buf, 0, 7);
        _bytecount += 7;
        if (s1.endsWith("N") || s1.endsWith("S")) {
            dValue = new Double(s1.substring(0, 2));
            d = dValue.doubleValue();
            dValue = new Double(s1.substring(2, 4));
            m = dValue.doubleValue();
            dValue = new Double(s1.substring(4, 6));
            s = dValue.doubleValue();
            //this._LatSE = d + m / 60.0 + s / (3600.0); // note : is this conversion correct ?
        } else {
            System.out.println("Error: file is not in SRTM DTED format (250-256 bytes)=" + s1);
            f0.close();
            return null;
        }
        f0.read(buf, 0, 8); // reading byte 257-264
        s1 = new String(buf, 0, 8);
        _bytecount += 8;
        if (s1.endsWith("W") || s1.endsWith("E")) {
            dValue = new Double(s1.substring(0, 3));
            d = dValue.doubleValue();
            dValue = new Double(s1.substring(3, 5));
            m = dValue.doubleValue();
            dValue = new Double(s1.substring(5, 7));
            s = dValue.doubleValue();
            //this._LngSE = d + m / 60.0 + s / (3600.0); // note : is this conversion correct ?
        } else {
            System.out.println("Error: file is not in SRTM DTED format (257-264 bytes)=" + s1);
            f0.close();
            return null;
        }

        f0.skipBytes(384);
        _bytecount += 384;// skip bytes 265-648

        // Start readin Accuracy Description (ACC) record
        f0.read(buf, 0, 3); // reading byte 1-3
        _bytecount += 3;
        s1 = new String(buf, 0, 3);

        if (!s1.equalsIgnoreCase("ACC")) {
            System.out.println("Error: file is not in SRTM DTED format (ACC 1-3 bytes)=" + s1);
            f0.close();
            return null;
        }
        f0.skipBytes(2697);
        _bytecount += 2697;// skip bytes 4-2700

        /*
              if(_debugSRTMImage)
              {
                //System.out.println("Origin Lat : " + _LatOrigin + "  Origin Lgn : " + this._LngOrigin);
                if(_geoImageObject != null)
                  _geoImageObject.PrintGeoImageObject();
                System.out.println("NE Lat : " + _LatNE + "  Origin Lgn : " + this._LngNE);
                System.out.println("SE Lat : " + _LatSE + "  Origin Lgn : " + this._LngSE);
                System.out.println("NW Lat : " + _LatNW + "  Origin Lgn : " + this._LngNW);
                System.out.println("SW Lat : " + _LatSW + "  Origin Lgn : " + this._LngSW);
                System.out.println("Byte Count in Header : " + _bytecount + "\n");
              }
        */

        imageobject.setProperty(ImageObject.GEOINFO, geo);

        return imageobject;

    }

    /**
     * Load the image data file.
     * 
     * @param datafile
     *            containing the image data.
     * @param demheader
     *            the header information.
     * @param imageobject
     *            that will contain the image data.
     * @throws IOException
     *             if an error occurred reading the data.
     */
    private void loadImageData(RandomAccessFile f0, SRTMHeader srtmHeader, ImageObject imageobject) throws IOException {

        if (srtmHeader.subarea == null) {
            srtmHeader.subarea = new SubArea(0, 0, srtmHeader.filencols, srtmHeader.filenrows);
        }
        int readMode = 0;
        // Reads the actual data depending on the _numInFileCols and _numInFileRows read from
        // header file.

        System.out.println("sampling : " + srtmHeader.sampling);
        if (srtmHeader.subarea.getWide() == srtmHeader.filencols || srtmHeader.subarea.getHeight() == srtmHeader.filenrows) {
            // full image
            if (srtmHeader.sampling == 1) {
                readMode = 0; // full image
            } else {
                readMode = 1; // subsampled full image
            }
        } else {
            if (srtmHeader.sampling == 1) {
                readMode = 2; // subarea image
            } else {
                readMode = 3; // subsampled subarea image
            }
        }

        System.out.println("Read Mode : " + readMode);

        try {
            switch (readMode) {
            case 0:
                if (FullImage(f0, srtmHeader, imageobject) == false) {
                    return;
                    // end of load the whole image (no subsampling)
                }

                // System.out.println("Returned from FullImage(...)");
                break; // end of full image
            case 1:
                if (FullSubSampImage(f0, srtmHeader, imageobject) == false) {
                    return;
                }
                // end of load the whole image with subsampling
                break; // end of full subsampled image
            case 2:
                if (SubAreaImage(f0, srtmHeader, imageobject) == false) {
                    return;
                }
                break;// end of else load a subregion
            case 3:
                if (SubAreaSubSampImage(f0, srtmHeader, imageobject) == false) {
                    return;
                }
                break;
            default:
                System.err.println("Error: read mode" + readMode + "  is not supported");
                return;
            }// end of switch statement
        } catch (Exception e) {
            System.err.println("Error thrown while reading file, caught in ReadData(): " + e);
            return;
        }

        // calculate min and max
        imageobject.computeMinMax();
    }

    //////////////////////////////
    // Read the image data in the case of full image (no subsampling)
    // when _readMode = 0
    ///////////////////////////////
    private boolean FullImage(RandomAccessFile f0, SRTMHeader header, ImageObject img) throws Exception {

        byte[] buf;

        int[] intbuf;
        buf = new byte[100];
        intbuf = new int[100];

        DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(f0.getFD())));

        // This check may NOT be required here if this method is only called from ReadData(...) method
        if ((header.filencols == -1) || (header.filenrows == -1)) {
            System.err.println("Error in SRTMImage.FullImage(): Must read header before reading the data file");
            return false;
        }
        try {
            // _imgObject = new ImageObject(_numInFileRows, _numInFileCols, 1, "SHORT");
            img.createArray();
            // Read the whole file.
            LimitValues _lim = new LimitValues();

            int num = header.ncols * header.nrows;
            short MaxS = _lim.MIN_SHORT;// -32767;
            short MinS = _lim.MAX_SHORT;//32767;

            int x, y, index;
            for (x = 0; x < header.ncols; x++)
            // for each column, it's a new data record
            // first read its general information
            {

                int test = f0.readUnsignedByte(); // read the first byte

                if (test != 170) // recognition sentinel fail !
                {
                    System.err.println("Error in SRTMImage.FullImage(): wrong sentinel byte in data recrod in record : " + test);
                    System.err.println("In Cols : " + x);
                    return false;
                }
                f0.skipBytes(7); // skip the next 8 bytes
                index = x + header.ncols * (header.nrows - 1); // reverse the order of putting pixels
                // because the elevation values in south -> north order
                for (y = 0; y < header.nrows; y++) {

                    img.setShort(index, f0.readShort());

                    if (img.getShort(index) > MaxS) {
                        MaxS = img.getShort(index);
                    }
                    if (img.getShort(index) < MinS && img.getShort(index) > -16000) {
                        MinS = img.getShort(index);
                    }
                    index -= header.nrows;
                }
                f0.skipBytes(4); // skip the checksum bytes
                // To-Do : implement the checksum
            }
            for (int i = 0; i < num; i++) {
                if (img.getShort(i) < MinS) {
                    img.setShort(i, MinS);
                }
            }
            System.out.println("Max S : " + MaxS + "  MinS : " + MinS);
            f0.close();
            return true;
        } catch (Exception e) {
            if (f0 != null) {
                f0.close();
            }
            throw new Exception("Caught exception in SRTMImage.FullImage(): " + e);
        }
    }

    //////////////////////////////
    // Read the image data in the case of full image with subsampling
    // when _readMode = 1
    ///////////////////////////////
    private boolean FullSubSampImage(RandomAccessFile f0, SRTMHeader header, ImageObject img) throws Exception {
        // May have to handle columns that don't have values
        //DataInputStream dis = new DataInputStream(
        //    new BufferedInputStream(new FileInputStream(f0.getFD())));

        // The check to ensure that the number of rows and columns are read from the header file before the call
        // to this function is already done in SRTMImage.ReadData() from where this function is called.
        // This check may NOT be required here if this method is only called from ReadData(...) method
        if ((header.filencols == -1) || (header.filenrows == -1)) {
            System.err.println("Error in SRTMImage.FullImage(): Must read header before reading the data file");
            return false;
        }

        if (header.sampling <= 0) {
            System.err.println("Error in SRTMImage.FullSubSampImage(): _sampRatio should be greater than 0");
            return false;
        }

        // Adding 1 to round off to next integral value.
        //int numCols = (_numInFileCols/_sampRatio) + 1;
        //int numRows = (_numInFileRows/_sampRatio) + 1;

        LimitValues _lim = new LimitValues();
        int num = header.nrows * header.ncols;
        short MaxS = _lim.MIN_SHORT;// -32767;
        short MinS = _lim.MAX_SHORT;//32767;

        int index;
        int x, y;

        // _imgObject = new ImageObject(numRows, numCols, 1, "Short");

        img.createArray();

        // Start reading every _sampRatio of each row of the file.
        try {
            int colOffset = (header.filenrows * 2 + 12) * (header.sampling);
            int rowOffset = 2 * (header.sampling - 1);

            for (x = 0; x < header.ncols; x++)
            // for each column, it's a new data record
            // first read its general information
            {
                f0.seek(colOffset * x + 3428);

                int test = f0.readUnsignedByte(); // read the first byte

                if (test != 170) // recognition sentinel fail !
                {
                    System.err.println("Error in SRTMImage.FullImage(): wrong sentinel byte in data recrod in record : " + test);
                    System.err.println("In Cols : " + x);
                    return false;
                }
                f0.skipBytes(7); // skip the next 8 bytes
                index = x + header.ncols * (header.nrows - 1); // reverse the order of putting pixels
                // because the elevation values in south -> north order
                for (y = 0; y < header.nrows; y++) {
                    //f0.read(buf,0,2);

                    img.setShort(index, f0.readShort());

                    if (img.getShort(index) > MaxS) {
                        MaxS = img.getShort(index);
                    }
                    if (img.getShort(index) < MinS && img.getShort(index) > -16000) {
                        MinS = img.getShort(index);
                    }

                    f0.skipBytes(rowOffset);
                    index -= header.ncols;
                }
                f0.skipBytes(4); // skip the checksum bytes
                // To-Do : implement the checksum

            }

            for (int i = 0; i < num; i++) {
                if (img.getShort(i) < MinS) {
                    img.setShort(i, MinS);
                }
            }

            System.out.println("Max S : " + MaxS + "  MinS : " + MinS);

            return true;

        } catch (EOFException e) {
            System.out.println("Finished reading file in FullSubSampImage mode");

            f0.close();
            return true;
        } catch (Exception ex) {
            f0.close();
            throw new IOException("Error reading file in FullSubSampImage mode: " + ex);
        }
    }

    /////////////////////////////
    // Read the image data in the case of a subarea (no subsampling)
    // when _readMode = 2
    ///////////////////////////////
    private boolean SubAreaImage(RandomAccessFile f0, SRTMHeader header, ImageObject img) throws Exception {
        // Reads a subarea.
        SubArea _area = header.subarea;
        int _numInFileRows, _numInFileCols;
        _numInFileCols = header.filencols;
        _numInFileRows = header.filenrows;

        // This check is NOT required here is the if this method is only being called from ReadData(...)
        if ((_numInFileRows == -1) || (_numInFileCols == -1)) {
            System.err.println("Error in SRTMImage.SubAreaImage(): Must read header before reading the data file");
            return false;
        }

        if ((_area.getRow() >= _numInFileRows) || (_area.getCol() >= _numInFileCols)) {
            System.err.println("Error in SRTMImage.SubAreaImage(): Row or Column value exceed the number of rows or columns in the data file");
            return false;
        }
        if (((_area.getRow() + _area.getHigh()) > _numInFileRows) || ((_area.getCol() + _area.getWide()) > _numInFileCols)) {
            System.err.println("Error in SRTMImage.SubAreaImage(): area height or width value exceed the number of rows or columns in the data file");
            return false;
        }
        int numRows = _area.getHigh();
        int numCols = _area.getWide();
        LimitValues _lim = new LimitValues();
        int num = _numInFileRows * _numInFileCols;
        short MaxS = _lim.MIN_SHORT;// -32767;
        short MinS = _lim.MAX_SHORT;//32767;

        int index;
        int x, y;

        //_imgObject = new ImageObject(numRows, numCols, 1, "Short");
        //_geoImageObject.subAreaPrep(_area);
        //_geoImageObject.subAreaPrep(_area.Col, _area.Row);
        // Start reading from (_area.Row * _numInFileCols) + _area.Col

        //_area.PrintSubArea();

        img.createArray();

        try {

            for (x = 0; x < _area.getWide(); x++)
            // for each column, it's a new data record
            // first read its general information
            {

                int Offset;
                Offset = ((_area.getCol() + x) * (_numInFileRows * 2 + 12)) + 3428; // skip column records
                f0.seek(Offset);

                int test = f0.readUnsignedByte(); // read the first byte
                if (test != 170) // recognition sentinel fail !
                {
                    System.err.println("Error in SRTMImage.FullImage(): wrong sentinel byte in data recrod in record : " + test);
                    System.err.println("In Cols : " + (_area.getCol() + x));
                    return false;
                }
                f0.skipBytes(7); // skip the next 8 bytes

                f0.skipBytes(_area.getRow() * 2); // skip first k rows

                index = x + numCols * (numRows - 1); // reverse the order of putting pixels
                // because the elevation values in south -> north order

                for (y = 0; y < _area.getHeight(); y++) {
                    //f0.read(buf,0,2);

                    img.setShort(index, f0.readShort());

                    if (img.getShort(index) > MaxS) {
                        MaxS = img.getShort(index);
                    }
                    if (img.getShort(index) < MinS && img.getShort(index) > -16000) {
                        MinS = img.getShort(index);
                    }

                    index -= numCols;

                }
                f0.skipBytes(4); // skip the checksum bytes
                // To-Do : implement the checksum
            }
            //_numOutFileRows = numRows;
            //_numOutFileCols = numCols;
            for (int i = 0; i < numRows * numCols; i++) {
                if (img.getShort(i) < MinS) {
                    img.setShort(i, MinS);
                }
            }

            f0.close();
            return true;
        } catch (Exception ex) {
            f0.close();
            throw new Exception("Error reading file in SRTMImage.SubAreaImage mode: " + ex);
        }
    }

    /////////////////////////////
    // Read the image data in the case of a subarea with subsampling
    // when _readMode = 3
    //////////////////////////////
    private boolean SubAreaSubSampImage(RandomAccessFile f0, SRTMHeader header, ImageObject img) throws Exception {
        // Reads only a SubSampled subarea

        SubArea _area = header.subarea;
        int _numInFileRows, _numInFileCols;
        _numInFileCols = header.filencols;
        _numInFileRows = header.filenrows;

        // This check may NOT be required here is this method is always called from ReadData(...)
        if ((_numInFileRows == -1) || (_numInFileCols == -1)) {
            System.err.println("Error in SRTMImage.SubAreaSubSampImage(): Must read header before reading the data file");
            return false;
        }
        if ((_area.getRow() >= _numInFileRows) || (_area.getCol() >= _numInFileCols)) {
            System.err.println("Error in SRTMImage.SubAreaSubSampImage(): Row or Column value exceed the number of rows or columns in the file");
            return false;
        }
        if (((_area.getRow() + _area.getHeight()) > _numInFileRows) || ((_area.getCol() + _area.getWide()) > _numInFileCols)) {
            System.err.println("Error in SRTMImage.SubAreaSubSampImage(): area height or width value exceed the number of rows or columns in the data file");
            return false;
        }

        if (header.sampling <= 0) {
            System.err.println("Error in SRTMImage.SubAreaSubSampImage(): _sampRatio should be greater than 0");
            return false;
        }

        int numRows = header.nrows;
        int numCols = header.ncols;
        LimitValues _lim = new LimitValues();
        int num = _numInFileRows * _numInFileCols;
        short MaxS = _lim.MIN_SHORT;// -32767;
        short MinS = _lim.MAX_SHORT;//32767;

        int index;
        int x, y;

        //_imgObject = new ImageObject(numRows, numCols, 1, "Short");

        img.createArray();
        //_geoImageObject.subAreaPrep(_area);
        //_geoImageObject.subAreaPrep(_area.Col, _area.Row);
        // Start reading from (_area.Row * _numInFileCols) + _area.Col

        //_area.PrintSubArea();

        int rowOffset = 2 * (header.sampling - 1);
        try {

            for (x = 0; x < numRows; x++)
            // for each column, it's a new data record
            // first read its general information
            {

                int Offset;
                Offset = ((_area.getCol() + x * header.sampling) * (_numInFileRows * 2 + 12)) + 3428; // skip column records
                f0.seek(Offset);

                int test = f0.readUnsignedByte(); // read the first byte
                if (test != 170) // recognition sentinel fail !
                {
                    System.err.println("Error in SRTMImage.FullImage(): wrong sentinel byte in data recrod in record : " + test);
                    System.err.println("In Cols : " + (_area.getCol() + x));
                    return false;
                }
                f0.skipBytes(7); // skip the next 8 bytes

                f0.skipBytes(_area.getRow() * 2); // skip first k rows

                index = x + numCols * (numRows - 1); // reverse the order of putting pixels
                // because the elevation values in south -> north order

                for (y = 0; y < numCols; y++) {
                    //f0.read(buf,0,2);

                    img.setShort(index, f0.readShort());

                    if (img.getShort(index) > MaxS) {
                        MaxS = img.getShort(index);
                    }
                    if (img.getShort(index) < MinS && img.getShort(index) > -16000) {
                        MinS = img.getShort(index);
                    }

                    f0.skipBytes(rowOffset);
                    index -= numCols;

                }
                f0.skipBytes(4); // skip the checksum bytes
                // To-Do : implement the checksum
            }
            //_numOutFileRows = numRows;
            //_numOutFileCols = numCols;
            f0.close();
            return true;
        } catch (Exception ex) {
            f0.close();
            throw new Exception("Error reading file in SRTMImage.SubAreaImage mode: " + ex);
        }

    }

    /**
     * Returns true if the class can write the file.
     * 
     * @param filename
     *            of the file to be written.
     * @return true if the file can be written by this class.
     */
    public boolean canWrite(String filename) {
        String ext = ImageLoader.getExtention(filename);
        if (ext == null) {
            return false;
        }
        return ext.equals("hdr");
    }

    /**
     * This function will write the imageobject to a file.
     * 
     * @param filename
     *            of the file to be written.
     * @param imageobject
     *            the image image to be written.
     * @throws IOException
     *             if the file could not be written.
     */
    public void writeImage(String filename, ImageObject imageobject) throws IOException {
        if (!filename.toLowerCase().endsWith(".hdr")) {
            throw (new IOException("Can not save image."));
        }
        if (imageobject.getNumBands() != 1) {
            throw (new IOException("Can not save image."));
        }
        if (imageobject.getProperty(ImageObject.GEOINFO) == null) {
            throw (new IOException("Can not save image."));
        }

        File headerfile = new File(filename);
        String name = filename.substring(0, filename.length() - 4);
        File datafile = new File(name);

        saveHeader(headerfile, imageobject);
        saveData(datafile, imageobject);
    }

    /**
     * Return a list of extentions this class can write.
     * 
     * @return a list of extentions that are understood by this class.
     */
    public String[] writeExt() {
        return new String[] { "hdr", "dem" };
    }

    /**
     * Write the header of the ENVI file. Write as key = val list, unlike a
     * property file, this can have arrays. In case of arrays, values are
     * seperated by a comma, and teh who array is encapsulaed in braces.
     * 
     * @param headerfile
     *            the file to which to write the header.
     * @param imageobject
     *            the image whose properties to write.
     * @throws IOException
     *             if an error happened writing to file.
     */
    private void saveHeader(File headerfile, ImageObject imageobject) throws IOException {
        //GeoInformation geo = (GeoInformation) imageobject.getProperty(ImageObject.GEOINFO);
        Projection geo = (Projection) imageobject.getProperty(ImageObject.GEOINFO);

        // save the header file
        BufferedWriter w = new BufferedWriter(new FileWriter(headerfile));

        w.write("ncols " + imageobject.getNumCols());
        w.newLine();
        w.write("nrows " + imageobject.getNumRows());
        w.newLine();
        w.write("xllcorner " + geo.GetMaxWestLng());
        w.newLine();
        w.write("yllcorner " + geo.GetMaxSouthLat());
        w.newLine();
        w.write("cellsize " + geo.getScaleX());
        w.newLine();
        w.close();
    }

    /**
     * Write the data of the image. Converts the image to an array of bytes that
     * is written to disk. The data is always written band interleave pixel,
     * same way it is stored in memory, and little endian.
     * 
     * @param datafile
     * @param imageobject
     * @throws IOException
     */
    private void saveData(File datafile, ImageObject imageobject) throws IOException {
        FileOutputStream w = new FileOutputStream(datafile);

        byte[] filedata;
        int maxsize = 0;
        int count = 0;
        int samples = imageobject.getSize();

        maxsize = sampleread * 4;
        filedata = new byte[maxsize];
        int t;
        for (int i = 0; i < samples; i++) {
            t = Float.floatToIntBits(imageobject.getFloat(i));
            filedata[count++] = (byte) ((t >> 24) & 0xff);
            filedata[count++] = (byte) ((t >> 16) & 0xff);
            filedata[count++] = (byte) ((t >> 8) & 0xff);
            filedata[count++] = (byte) (t & 0xff);
            if (count == maxsize) {
                w.write(filedata);
                count = 0;
            }
            ImageLoader.fireProgress(i, samples);
        }
        if (count > 0) {
            w.write(filedata, 0, count);
        }

        w.close();
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

    class SRTMHeader {
        public int     filencols = -1;
        public int     filenrows = -1;
        public int     ncols     = -1;
        public int     nrows     = -1;
        public double  xllcorner = -1;
        public double  yllcorner = -1;
        public double  cellsize  = -1;

        public SubArea subarea   = null;
        public int     sampling  = 1;
    }
}
