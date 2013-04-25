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
package edu.illinois.ncsa.isda.imagetools.core.io.dem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageException;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObjectFloat;
import edu.illinois.ncsa.isda.imagetools.core.datatype.SubArea;
import edu.illinois.ncsa.isda.imagetools.core.geo.projection.*;
import edu.illinois.ncsa.isda.imagetools.core.io.ImageLoader;
import edu.illinois.ncsa.isda.imagetools.core.io.ImageReader;
import edu.illinois.ncsa.isda.imagetools.core.io.ImageWriter;
import edu.illinois.ncsa.isda.imagetools.core.io.tiff.GeoEntry;

import java.io.*;

/**
 *
 */
public class DEMLoader implements ImageReader, ImageWriter {
    private static final int sampleread = 10000;
    private static Log logger = LogFactory.getLog(DEMLoader.class);

    /**
     * Returns true if the file has .hdr as extention. Since ENVI has header as
     * well, this loader should come after the ENVI loader to avoid false
     * exception to be thrown. The ENVI loader has a better detection of the
     * filetype.
     *
     * @param filename ignored.
     * @param hdr      the first 100 bytes of the file.
     * @return true if the file can be read by this class.
     */
    public boolean canRead(String filename, byte[] hdr) {
        if (filename.toLowerCase().endsWith(".hdr")) {
            return true;
        }
        return false;
    }

    /**
     * This function will read the file and return an imageobject that contains
     * the file.
     *
     * @param filename of the file to be read
     * @param subarea  of the file to be read, null if full image is to be
     *                 read.
     * @param sampling is the subsampling that needs to be done on the image as
     *                 it is loaded.
     * @return the file as an imageobject.
     * @throws java.io.IOException if the file could not be read.
     */
    public ImageObject readImage(String filename, SubArea subarea, int sampling) throws IOException {
        return loadImage(filename, subarea, sampling, false);
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
        return loadImage(filename, null, 1, true);
    }

    /**
     * Return a list of extentions this class can read.
     *
     * @return a list of extentions that are understood by this class.
     */
    public String[] readExt() {
        return new String[]{"hdr"};
    }

    /**
     * Return the description of the reader (or writer).
     *
     * @return decription of the reader (or writer)
     */
    public String getDescription() {
        return "DEM files";
    }

    /**
     * This function will read the image and return the imageobject.
     *
     * @param filename of the file to read.
     * @param subarea  of the file to read.
     * @param sampling the subsampling to use.
     * @param header   true if only the header should be read.
     * @return an imageobject containting the file.
     * @throws IOException if the file could not be read correctly.
     */
    private ImageObject loadImage(String filename, SubArea subarea, int sampling, boolean header) throws IOException {
        File headerfile = new File(filename);
        if (!headerfile.exists()) {
            throw(new IOException("Could not find header file."));
        }
        String name = filename.substring(0, filename.length() - 4);
        File datafile = new File(name);
        if (!datafile.exists()) {
            throw(new IOException("Could not find data file."));
        }

        // store some info
        DEMheader demheader = new DEMheader();
        demheader.sampling = sampling;
        demheader.subarea = subarea;
        ImageObject imgobj = loadImageHeader(headerfile, demheader);

        if (!header) {
            loadImageData(datafile, demheader, imgobj);
        }
        return imgobj;
    }

    /**
     * Read the header of the image. The header has key = value pairs. The value
     * can be stored between braces. If the value is an array, the array parts
     * are seperated by a comma.
     *
     * @param headerfile the headerfile
     * @param demheader  the structure containing the header informatioon
     * @return an imageobject based on the header information
     * @throws IOException if the file could not be read correctly.
     */
    private ImageObject loadImageHeader(File headerfile, DEMheader demheader) throws IOException {

        // load the file
        BufferedReader r = new BufferedReader(new FileReader(headerfile));
        String line = r.readLine();
        while (line != null) {
            line.trim();
            String[] split = line.split("[ \t]+", 2);

            if (split[0].compareToIgnoreCase("ncols") == 0) {
                demheader.filencols = Integer.parseInt(split[1]);
                if ((demheader.subarea != null) && (demheader.subarea.width > (demheader.filencols - demheader.subarea.x))) {
                    demheader.subarea.width = demheader.filencols - demheader.subarea.x;
                }

            } else if (split[0].compareToIgnoreCase("nrows") == 0) {
                demheader.filenrows = Integer.parseInt(split[1]);
                if ((demheader.subarea != null) && (demheader.subarea.height > (demheader.filenrows - demheader.subarea.y))) {
                    demheader.subarea.height = demheader.filenrows - demheader.subarea.y;
                }

            } else if (split[0].compareToIgnoreCase("cellsize") == 0) {
                demheader.cellsize = Double.parseDouble(split[1]);

            } else if (split[0].compareToIgnoreCase("xllcorner") == 0) {
                demheader.xllcorner = Double.parseDouble(split[1]);

            } else if (split[0].compareToIgnoreCase("yllcorner") == 0) {
                demheader.yllcorner = Double.parseDouble(split[1]);

            } else {
                logger.debug("Ignoring : " + line);
            }

            line = r.readLine();
        }

        // check if we have all fields
        if ((demheader.filencols == -1) || (demheader.filenrows == -1) ||
            (demheader.xllcorner == -1) || (demheader.yllcorner == -1) ||
            (demheader.cellsize == -1)) {
            throw(new IOException("Missing header variables, not valid dem header."));
        }

        // fix for subarea and subsample
        if (demheader.subarea != null) {
            demheader.ncols = demheader.subarea.width;
            demheader.nrows = demheader.subarea.height;
        } else {
            demheader.ncols = demheader.filencols;
            demheader.nrows = demheader.filenrows;
        }

        if (demheader.sampling != 1) {
            demheader.ncols = (int) Math.floor(demheader.ncols / (double) demheader.sampling);
            demheader.nrows = (int) Math.floor(demheader.nrows / (double) demheader.sampling);
        }

        ImageObject imageobject = new ImageObjectFloat(demheader.nrows, demheader.ncols, 1, true);
        ;
        /*GeoInformation geo = new GeoInformation();

        geo.SetModelType(4);
        geo.SetRasterSpaceI(0);
        geo.SetRasterSpaceJ(0 + demheader.nrows);
        geo.SetColumnResolution(demheader.cellsize);
        geo.SetRowResolution(demheader.cellsize);
        geo.SetGeoScaleX(demheader.cellsize);
        geo.SetGeoScaleY(demheader.cellsize);
        geo.SetMaxSouthLat(demheader.yllcorner);
        geo.SetMaxWestLng(demheader.xllcorner);
        geo.SetEastingInsertionValue(demheader.xllcorner);
        geo.SetNorthingInsertionValue(demheader.yllcorner);

        GeoConvert geoConv = new GeoConvert(geo);
        geoConv.subSamplePrep(demheader.sampling);
        */
        Projection geo = new Projection();
        geo.setRasterSpaceI(0);
        geo.setRasterSpaceJ(0 + demheader.nrows);
        //geo.SetColumnResolution(demheader.cellsize);
        geo.setScaleX(demheader.cellsize);
        //geo.SetRowResolution(demheader.cellsize);
        geo.setScaleY(demheader.cellsize);
        geo.setInsertionY(demheader.yllcorner);
        geo.setInsertionX(demheader.xllcorner);
        geo.setNumRows(demheader.nrows);
        geo.setNumCols(demheader.ncols);
        geo.setUnit(GeoEntry.Angular_Radian);
        //geoSetEastingInsertionValue(demheader.xllcorner);
        //geo.SetNorthingInsertionValue(demheader.yllcorner);
        /*if(demheader.subarea != null) {
          geo.subsample(demheader.sampling, demheader.filenrows,
                        demheader.filencols);
        }*/
        if(demheader.sampling != 1) {
          geo.subsample(demheader.sampling, demheader.filenrows,
                        demheader.filencols);
        }

        /*if (demheader.subarea != null) {
            geoConv.subAreaPrep(demheader.subarea, demheader.filenrows, demheader.filencols);
        }*/

        imageobject.setProperty(ImageObject.GEOINFO, geo);

        return imageobject;
    }

    /**
     * Load the image data file.
     *
     * @param datafile    containing the image data.
     * @param demheader   the header information.
     * @param imageobject that will contain the image data.
     * @throws IOException if an error occurred reading the data.
     */
    private void loadImageData(File datafile, DEMheader demheader, ImageObject imageobject) throws IOException {
        FileInputStream fi = new FileInputStream(datafile);
        DataInputStream inp = new DataInputStream(fi);

        if (demheader.subarea == null) {
            demheader.subarea = new SubArea(0, 0, demheader.filencols, demheader.filenrows);
        }
        demheader.subarea.width -= demheader.subarea.width % demheader.sampling;
        demheader.subarea.height -= demheader.subarea.height % demheader.sampling;

        imageobject.createArray();
        int count = 0;
        int size = demheader.filencols * demheader.filenrows * 4;
        int bytes = sampleread * 4;
        int j = 0;
        int x = 0;
        int s = 0;
        int t;
        int bytesread = 0;
        int skip = demheader.filencols - demheader.subarea.width;
        skip += (demheader.sampling - 1) * demheader.filencols;
        //skip += (demheader.filencols & demheader.sampling);
        skip *= 4;
        int i = 4 * (demheader.subarea.x + (demheader.subarea.y * demheader.filencols));
        byte[] filedata = new byte[bytes];
        do {
            // skip until we have the datablock containing the data we need.
            while (i >= count) {
                bytesread = inp.read(filedata);
                if (bytesread < 0)
                    throw(new IOException("Error reading data from file."));
                if ((bytesread < filedata.length) && (bytesread != (size - count)))
                    throw(new IOException("Error reading data from file."));
                count += bytesread;
                ImageLoader.fireProgress(count, size);
                s = (i - count + bytesread);
            }

            // all dem data is stored as float, convert bytes and add to image
            t = ((filedata[s] & 0xff) << 24) | ((filedata[s + 1] & 0xff) << 16) |
                ((filedata[s + 2] & 0xff) << 8) | (filedata[s + 3] & 0xff);
            imageobject.set(j++, Float.intBitsToFloat(t));

            // skip to the next pixel to be read.
            x += demheader.sampling;
            i += demheader.sampling * 4;
            s += demheader.sampling * 4;

            // skip to next row if this is the end of the data to be read.
            if (x >= demheader.subarea.getEndCol()) {
                x = demheader.subarea.x;
                i += skip;
                s = (i - count + bytesread);
            }
        } while (j < imageobject.getSize());
        inp.close();
        fi.close();

        // calculate min and max
        imageobject.computeMinMax();
    }

    /**
     * Returns true if the class can write the file.
     *
     * @param filename of the file to be written.
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
     * @param filename    of the file to be written.
     * @param imageobject the image image to be written.
     * @throws IOException if the file could not be written.
     */
    public void writeImage(String filename, ImageObject imageobject) throws IOException {
        if (imageobject.getNumBands() != 1) {
            throw(new IOException("Can not save image (can only save 1 band)."));
        }
        if (imageobject.getProperty(ImageObject.GEOINFO) == null) {
            throw(new IOException("Can not save image (no geoinfo in image)."));
        }
        if (imageobject.getType() == ImageObject.TYPE_DOUBLE) {
            throw(new IOException("Can not save images of type double."));
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
        return new String[]{"hdr", "dem"};
    }

    /**
     * Write the header of the ENVI file. Write as key = val list, unlike a
     * property file, this can have arrays. In case of arrays, values are
     * seperated by a comma, and teh who array is encapsulaed in braces.
     *
     * @param headerfile  the file to which to write the header.
     * @param imageobject the image whose properties to write.
     * @throws IOException if an error happened writing to file.
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

	class DEMheader {
        public int filencols = -1;
        public int filenrows = -1;
        public int ncols = -1;
        public int nrows = -1;
        public double xllcorner = -1;
        public double yllcorner = -1;
        public double cellsize = -1;

        public SubArea subarea = null;
        public int sampling = 1;
    }
}
