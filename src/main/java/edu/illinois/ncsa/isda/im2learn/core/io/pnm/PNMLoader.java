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
package edu.illinois.ncsa.isda.imagetools.core.io.pnm;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageException;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.imagetools.core.datatype.SubArea;
import edu.illinois.ncsa.isda.imagetools.core.io.ImageLoader;
import edu.illinois.ncsa.isda.imagetools.core.io.ImageReader;
import edu.illinois.ncsa.isda.imagetools.core.io.ImageWriter;

/**
 * Class to read and write PGM, PPM and PBM files.
 *
 * @author Rob Kooper
 * @version 2.0
 */
public class PNMLoader implements ImageReader, ImageWriter {
    private static int sampleread = 10000;

    final private static int PBM_ASCII = 1;
    final private static int PGM_ASCII = 2;
    final private static int PPM_ASCII = 3;
    final private static int PBM_BIN = 4;
    final private static int PGM_BIN = 5;
    final private static int PPM_BIN = 6;
    
    private static Log log = LogFactory.getLog(PNMLoader.class);

    /**
     * Returns true if the file contains "P" as the first byte followed by a
     * number between 1 and 6 as the first two bytes of the file.
     *
     * @param filename ignored.
     * @param hdr      the first 100 bytes of the file.
     * @return true if the file can be read by this class.
     */
    public boolean canRead(String filename, byte[] hdr) {
        // file should always start with P1-P6
        if (hdr[0] == 'P' && hdr[1] >= '1' && hdr[1] <= '6') {
            return true;
        }

        // no matching on ext, if it does not start with P? we can't read it.
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
     * @throws IOException if the file could not be read.
     */
    public ImageObject readImage(String filename, SubArea subarea, int sampling) throws IOException {
        FileReader fr = new FileReader(filename);
        ImageObject imgobj = readImage(fr, subarea, sampling, false);
        fr.close();
        return imgobj;
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
        FileReader fr = new FileReader(filename);
        ImageObject imgobj = readImage(fr, null, 1, true);
        fr.close();
        return imgobj;
    }

    /**
     * Return a list of extentions this class can read.
     *
     * @return a list of extentions that are understood by this class.
     */
    public String[] readExt() {
        return new String[]{"pgm", "pbm", "ppm"};
    }

    /**
     * Return the description of the reader (or writer).
     *
     * @return decription of the reader (or writer)
     */
    public String getDescription() {
        return "PNM files";
    }

    /**
     * This function will read the first few lines of the file returning an
     * imageobject based on these first lines.
     *
     * @param fr     the file
     * @param header true if only need to read the header.
     * @return an imageobject
     * @throws IOException if an error occurred reading the file.
     */
    private ImageObject readImage(FileReader fr, SubArea subarea, int sampling, boolean header) throws IOException {
        int type;
        int col, row, maxval;

        // type of format
        type = Integer.parseInt(fr.readtoken().substring(1));

        // parse size
        col = Integer.parseInt(fr.readtoken());
        row = Integer.parseInt(fr.readtoken());

        // parse maxval
        if ((type != PBM_ASCII) && (type != PBM_BIN)) {
            maxval = Integer.parseInt(fr.readtoken());
        } else {
            maxval = 1;
        }

        // apply the subarea and sampling
        //if (subarea != null) {
        //    row = subarea.height;
        //    col = subarea.width;
        //}
        //if (sampling != 1) {
        //    row = (int)Math.ceil(row / sampling);
        //    col = (int)Math.ceil(col / sampling);
        //}

        // pick right type based on maxval
        ImageObject result = null;
        try {
	        if (maxval < 256) {
	            if ((type == PPM_ASCII) || (type == PPM_BIN)) {
	            	result = ImageObject.createImage(row, col, 3, ImageObject.TYPE_BYTE);
	            } else {
	            	result = ImageObject.createImage(row, col, 1, ImageObject.TYPE_BYTE);
	            }
	        } else {
	            if ((type == PPM_ASCII) || (type == PPM_BIN)) {
	            	result = ImageObject.createImage(row, col, 3, ImageObject.TYPE_USHORT);
	            } else {
	            	result = ImageObject.createImage(row, col, 1, ImageObject.TYPE_USHORT);
	            }
	        }
        } catch (ImageException exc) {
        	log.warn("Could not create ImageObject.", exc);
        	throw(new IOException("Could not create ImageObject."));
        }

        // if only header, done.
        if (header) {
            return result;
        }

        // read the data
        if (fr.readArray(result, type, subarea, sampling) == result.getSize()) {
            result.computeMinMax();
            return result;
        } else {
            throw(new IOException("Could not read enough bytes."));
        }
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
        return ext.equals("pbm") || ext.equals("pgm") || ext.equals("ppm");
    }

    /**
     * This function will write the imageobject to a file.
     *
     * @param filename    of the file to be written.
     * @param imageobject the image image to be written.
     * @throws IOException if the file could not be written.
     */
    public void writeImage(String filename, ImageObject imageobject) throws IOException {
        FileOutputStream w = new FileOutputStream(filename);

        String ext = ImageLoader.getExtention(filename);
        if (ext == null) {
            throw(new IOException("Invalid extention."));
        }

        int type;
        String hdr;
        if (ext.equals("ppm")) {
            type = PPM_BIN;
            hdr = "P6\n";
        } else if (ext.equals("pgm")) {
            type = PGM_BIN;
            hdr = "P5\n";
        } else {
            type = PBM_BIN;
            hdr = "P4\n";
        }

        // write the header
        hdr += "# Created using Im2Learn.\n";
        hdr += imageobject.getNumCols() + " " + imageobject.getNumRows() + "\n";
        if (type != PBM_BIN) {
            hdr += (int) imageobject.getMax() + "\n";
        }
        w.write(hdr.getBytes());

        // get the bands
        int[] rgb = (int[]) imageobject.getProperty(ImageObject.DEFAULT_RGB);
        if (rgb == null) {
            rgb = new int[]{0, 1, 2};
        }
        int[] gray = (int[]) imageobject.getProperty(ImageObject.DEFAULT_GRAY);
        if (gray == null) {
            gray = new int[]{0};
        }

        // write the data
        // if maxval is less than 256 use byte, otherwise use short
        byte[] filedata;
        int i, cnt, col, v;
        switch (type) {
            case PBM_BIN:
                if (imageobject.getNumBands() > 1) {
                    log.info("Only saving the gray band [" + gray[0] + "].");
                }
                // take the center of min/max of gray band and use that to
                // threshold the data, all bits packed in a byte, with
                // boundary a width of image.
                double threshold = (imageobject.getMax(gray[0]) - imageobject.getMin(gray[0])) / 2;
                filedata = new byte[sampleread];
                col = imageobject.getNumCols();
                for (i = 0, cnt = 0; i < imageobject.getSize();) {
                    if (cnt == sampleread) {
                        w.write(filedata);
                        cnt = 0;
                    }
                    if (col == 0) {
                        col = imageobject.getNumCols();
                    }
                    v = (imageobject.getDouble(i + gray[0]) < threshold) ? 1 : 0;
                    i += imageobject.getNumBands();
                    col--;
                    if (col > 0) {
                        v = (v << 1) | ((imageobject.getDouble(i + gray[0]) < threshold) ? 1 : 0);
                        i += imageobject.getNumBands();
                        col--;
                    } else {
                        v = v << 1;
                    }
                    if (col > 0) {
                        v = (v << 1) | ((imageobject.getDouble(i + gray[0]) < threshold) ? 1 : 0);
                        i += imageobject.getNumBands();
                        col--;
                    } else {
                        v = v << 1;
                    }
                    if (col > 0) {
                        v = (v << 1) | ((imageobject.getDouble(i + gray[0]) < threshold) ? 1 : 0);
                        i += imageobject.getNumBands();
                        col--;
                    } else {
                        v = v << 1;
                    }
                    if (col > 0) {
                        v = (v << 1) | ((imageobject.getDouble(i + gray[0]) < threshold) ? 1 : 0);
                        i += imageobject.getNumBands();
                        col--;
                    } else {
                        v = v << 1;
                    }
                    if (col > 0) {
                        v = (v << 1) | ((imageobject.getDouble(i + gray[0]) < threshold) ? 1 : 0);
                        i += imageobject.getNumBands();
                        col--;
                    } else {
                        v = v << 1;
                    }
                    if (col > 0) {
                        v = (v << 1) | ((imageobject.getDouble(i + gray[0]) < threshold) ? 1 : 0);
                        i += imageobject.getNumBands();
                        col--;
                    } else {
                        v = v << 1;
                    }
                    if (col > 0) {
                        v = (v << 1) | ((imageobject.getDouble(i + gray[0]) < threshold) ? 1 : 0);
                        i += imageobject.getNumBands();
                        col--;
                    } else {
                        v = v << 1;
                    }
                    filedata[cnt++] = (byte) (v & 0xff);
                }
                if (cnt > 0) {
                    w.write(filedata, 0, cnt);
                }
                return;

            case PGM_BIN:
                if (imageobject.getNumBands() > 1) {
                    log.info("Only saving the gray band [" + gray[0] + "].");
                }
                // if image has maxvalue less than 256, write it as a byte,
                // otherwise write it as short.
                if (imageobject.getMax() < 256) {
                    filedata = new byte[sampleread];
                    for (i = 0, cnt = 0; i < imageobject.getSize(); i += imageobject.getNumBands()) {
                        if (cnt == sampleread) {
                            w.write(filedata);
                            cnt = 0;
                        }
                        filedata[cnt++] = imageobject.getByte(i + gray[0]);
                    }
                    if (cnt > 0) {
                        w.write(filedata, 0, cnt);
                    }
                } else {
                    filedata = new byte[sampleread * 2];
                    for (i = 0, cnt = 0; i < imageobject.getSize(); i += imageobject.getNumBands()) {
                        if (cnt == sampleread * 2) {
                            w.write(filedata);
                            cnt = 0;
                        }
                        filedata[cnt++] = (byte) ((imageobject.getShort(i + gray[0])) & 0xff);
                        filedata[cnt++] = (byte) ((imageobject.getShort(i + gray[0]) >> 8) & 0xff);
                    }
                    if (cnt > 0) {
                        w.write(filedata, 0, cnt);
                    }
                }
                break;

            case PPM_BIN:
                if (imageobject.getNumBands() < 3) {
                    throw(new IOException("Can only save images with 3 or more bands."));
                }
                // if image has maxvalue less than 256, write it as a byte,
                // otherwise write it as short.
                if (imageobject.getMax() < 256) {
                    filedata = new byte[sampleread * 3];
                    for (i = 0, cnt = 0; i < imageobject.getSize(); i += imageobject.getNumBands()) {
                        if (cnt == sampleread * 3) {
                            w.write(filedata);
                            cnt = 0;
                        }
                        filedata[cnt++] = imageobject.getByte(i + rgb[0]);
                        filedata[cnt++] = imageobject.getByte(i + rgb[1]);
                        filedata[cnt++] = imageobject.getByte(i + rgb[2]);
                    }
                    if (cnt > 0) {
                        w.write(filedata, 0, cnt);
                    }
                } else {
                    filedata = new byte[sampleread * 6];
                    for (i = 0, cnt = 0; i < imageobject.getSize(); i += imageobject.getNumBands()) {
                        if (cnt == sampleread * 6) {
                            w.write(filedata);
                            cnt = 0;
                        }
                        filedata[cnt++] = (byte) ((imageobject.getShort(i + rgb[0]) >> 8) & 0xff);
                        filedata[cnt++] = (byte) (imageobject.getShort(i + rgb[0]) & 0xff);
                        filedata[cnt++] = (byte) ((imageobject.getShort(i + rgb[1]) >> 8) & 0xff);
                        filedata[cnt++] = (byte) (imageobject.getShort(i + rgb[1]) & 0xff);
                        filedata[cnt++] = (byte) ((imageobject.getShort(i + rgb[2]) >> 8) & 0xff);
                        filedata[cnt++] = (byte) (imageobject.getShort(i + rgb[2]) & 0xff);
                    }
                    if (cnt > 0) {
                        w.write(filedata, 0, cnt);
                    }
                }
                break;

            default:
                throw(new IOException("Don't understand type."));
        }

        w.close();
    }

    /**
     * Return a list of extentions this class can write.
     *
     * @return a list of extentions that are understood by this class.
     */
    public String[] writeExt() {
        return new String[]{"pgm", "pbm", "ppm"};
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
     * subclass used to read the data of the file, buffering data as it is
     * read.
     */
    class FileReader {
        private FileInputStream fis = null;
        private byte data[] = new byte[sampleread];
        private int len = 0;
        private int start = 0;

        /**
         * Open the file
         *
         * @param filename of the file to open.
         * @throws IOException if file could not be opened.
         */
        public FileReader(String filename) throws IOException {
            fis = new FileInputStream(filename);
            len = fis.read(data);
        }

        /**
         * Close the file.
         *
         * @throws IOException if the file could not be closed.
         */
        public void close() throws IOException {
            if (fis == null) {
                return;
            }
            fis.close();
            fis = null;
        }

        /**
         * Returns a single token read from the file,.
         *
         * @return single token read from the file.
         * @throws IOException if an error occured reading a line from the
         *                     file.
         */
        public String readtoken() throws IOException {
            String result = "";
            int cnt = start;
            int end = 0;
            boolean done = false;
            for (; ;) {
                if (cnt == len) {
                    result += new String(data, start, end);
                    len = fis.read(data);
                    cnt = 0;
                    start = 0;
                    end = 0;
                }
                if ((data[cnt] == '\n') || (data[cnt] == '\r') ||
                    (data[cnt] == '\t') || (data[cnt] == ' ')) {
                    done = true;
                } else if (done) {
                    result += new String(data, start, end);
                    len = len - (cnt - start);
                    start = cnt;
                    return result;
                } else if (data[cnt] == '#') {
                    if (end != 0) {
                        result += new String(data, start, end);
                        done = true;
                    }
                    readLine();
                    cnt = start - 1;
                } else {
                    end++;
                }
                cnt++;
            }
        }

        /**
         * Returns a single line read from the file, including the end of line
         * marker.
         *
         * @return single line read from the file.
         * @throws IOException if an error occured reading a line from the
         *                     file.
         */
        public String readLine() throws IOException {
            String result = "";
            int cnt = start;
            for (; ;) {
                if (cnt == len) {
                    result += new String(data, start, len);
                    len = fis.read(data);
                    cnt = 0;
                    start = 0;
                }
                if (data[cnt] == '\n') {
                    result += new String(data, start, cnt - start);
                    len = len - (cnt - start) - 1;
                    start = cnt + 1;
                    if (len == 0) {
                        len = fis.read(data);
                        start = 0;
                    }
                    if (len != 0 && data[start] == '\r') {
                        start++;
                        len--;
                    }
                    return result;
                }
                cnt++;
            }
        }

        /**
         * This function will read all the imagedata from the file.
         *
         * @param imgobj the image that will contain the image data
         * @param type   the type of the image PBM, PGM or PPM
         * @return the number of data values read.
         * @throws IOException if an error occurred reading the file.
         */
        public int readArray(ImageObject imgobj, int type, SubArea area, int sampling) throws IOException {
            int cnt = 0;
            int col = 0;
            int left = imgobj.getSize();
            int size = left;
            String tmpstr;
            int tmpint;

            // TODO add the subarea and sampling
            switch (type) {
                case PBM_ASCII:
                    while (left > 0) {
                        ImageLoader.fireProgress(cnt, size);
                        tmpstr = readtoken();
                        if (tmpstr.equals("1")) {
                            imgobj.set(cnt, 255);
                        } else {
                            imgobj.set(cnt, 0);
                        }
                        cnt++;
                        left--;
                    }
                    return cnt;

                case PBM_BIN:
                    col = imgobj.getNumCols();
                    while (left > 0) {
                        ImageLoader.fireProgress(cnt, size);
                        if (len == 0) {
                            len = fis.read(data, len, data.length - len);
                            start = 0;
                            if (len == 0) {
                                return cnt;
                            }
                        }
                        if (col == 0) {
                            col = imgobj.getNumCols();
                        }
                        // convert data from byte to 8 bytes, padding at end
                        // of a row.
                        tmpint = data[start] & 0xff;
                        imgobj.set(cnt++, ((tmpint & 0x80) != 0) ? 0 : 255);
                        tmpint = tmpint << 1;
                        col--;
                        left--;
                        if (col > 0) {
                            imgobj.set(cnt++, ((tmpint & 0x80) != 0) ? 0 : 255);
                            tmpint = tmpint << 1;
                            col--;
                            left--;
                        }
                        if (col > 0) {
                            imgobj.set(cnt++, ((tmpint & 0x80) != 0) ? 0 : 255);
                            tmpint = tmpint << 1;
                            col--;
                            left--;
                        }
                        if (col > 0) {
                            imgobj.set(cnt++, ((tmpint & 0x80) != 0) ? 0 : 255);
                            tmpint = tmpint << 1;
                            col--;
                            left--;
                        }
                        if (col > 0) {
                            imgobj.set(cnt++, ((tmpint & 0x80) != 0) ? 0 : 255);
                            tmpint = tmpint << 1;
                            col--;
                            left--;
                        }
                        if (col > 0) {
                            imgobj.set(cnt++, ((tmpint & 0x80) != 0) ? 0 : 255);
                            tmpint = tmpint << 1;
                            col--;
                            left--;
                        }
                        if (col > 0) {
                            imgobj.set(cnt++, ((tmpint & 0x80) != 0) ? 0 : 255);
                            tmpint = tmpint << 1;
                            col--;
                            left--;
                        }
                        if (col > 0) {
                            imgobj.set(cnt++, ((tmpint & 0x80) != 0) ? 0 : 255);
                            col--;
                            left--;
                        }
                        start++;
                        len--;
                    }
                    return cnt;

                case PGM_ASCII:
                case PPM_ASCII:
                    while (left > 0) {
                        ImageLoader.fireProgress(cnt, size);
                        tmpint = Integer.parseInt(readtoken());
                        imgobj.set(cnt, tmpint);
                        cnt++;
                        left--;
                    }
                    return cnt;


                case PGM_BIN:
                case PPM_BIN:
                    switch (imgobj.getType()) {
                        case ImageObject.TYPE_BYTE:
                            while (left > 0) {
                                ImageLoader.fireProgress(cnt, size);
                                System.arraycopy(data, start, imgobj.getData(), cnt, len);
                                cnt += len;
                                left -= len;
                                len = fis.read(data, 0, data.length);
                                start = 0;
                                if (len == 0) {
                                    return cnt;
                                }
                            }
                            return cnt;

                        case ImageObject.TYPE_SHORT:
                        case ImageObject.TYPE_USHORT:
                            while (left > 0) {
                                ImageLoader.fireProgress(cnt, size);
                                if (len < 2) {
                                    System.arraycopy(data, start, data, 0, len);
                                    len += fis.read(data, len, data.length - len);
                                    start = 0;
                                    if (len < 2) {
                                        return cnt;
                                    }
                                }
                                imgobj.set(cnt, ((data[start + 1] & 0xff) << 8) | (data[start] & 0xff));
                                start += 2;
                                len -= 2;
                                left--;
                                cnt++;
                            }
                            return cnt;

                        default:
                            return 0;
                    }

                default:
                    return 0;
            }
        }
    }
}
