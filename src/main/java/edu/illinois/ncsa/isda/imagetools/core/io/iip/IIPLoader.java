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
package edu.illinois.ncsa.isda.imagetools.core.io.iip;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageException;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.imagetools.core.datatype.SubArea;
import edu.illinois.ncsa.isda.imagetools.core.io.ImageLoader;
import edu.illinois.ncsa.isda.imagetools.core.io.ImageReader;
import edu.illinois.ncsa.isda.imagetools.core.io.ImageWriter;

/**
 * This class writes IIP files. IIP files are based on PGM files but are not
 * limited to bytes and 3 bands.
 *
 * @author Rob Kooper
 * @author Peter Bacjzy
 * @version 2.0
 */
public class IIPLoader implements ImageReader, ImageWriter {
    private static int sampleread = 10000;

    /**
     * Returns true if the file contains "IIP" as the first 3 bytes of the
     * file.
     *
     * @param filename ignored.
     * @param hdr      the first 100 bytes of the file.
     * @return true if the file can be read by this class.
     */
    public boolean canRead(String filename, byte[] hdr) {
        // file should always start with IIP
        if (hdr[0] == 'I' && hdr[1] == 'I' && hdr[2] == 'P') {
            return true;
        }

        // no matching on ext, if it does not start with IIP we can't read it.
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
        ImageObject imgobj = readImageHeader(fr);

        if (fr.readArray(imgobj) != imgobj.getSize()) {
            fr.close();
            throw(new IOException("Premature EOF."));
        }
        // TODO improve scale/crop
        if (subarea != null) {
            try {
                imgobj = imgobj.crop(subarea);
            } catch (ImageException e) {
                throw(new IOException("Could not crop image."));
            }
        }
        if (sampling != 1.0) {
            try {
                imgobj.scale(sampling);
            } catch (ImageException e) {
                throw(new IOException("Could not scale image."));
            }
        }

        imgobj.computeMinMax();
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
        ImageObject imgobj = readImageHeader(fr);
        fr.close();
        return imgobj;
    }

    /**
     * Return a list of extentions this class can read.
     *
     * @return a list of extentions that are understood by this class.
     */
    public String[] readExt() {
        return new String[]{"iip"};
    }

    /**
     * Return the description of the reader (or writer).
     *
     * @return decription of the reader (or writer)
     */
    public String getDescription() {
        return "IIP files";
    }

    /**
     * This function will read the first few lines of the file returning an
     * imageobject based on these first lines.
     *
     * @param fr the file
     * @return an imageobject
     * @throws IOException if an error occurred reading the file.
     */
    private ImageObject readImageHeader(FileReader fr) throws IOException {
        String line;
        int col, row, bands;

        // ignore first line don't care we know it is IIP
        fr.readLine();

        // parse size
        col = Integer.parseInt(fr.readtoken());
        row = Integer.parseInt(fr.readtoken());

        // read bands
        bands = Integer.parseInt(fr.readtoken());

        // read type
        line = fr.readtoken();

        try {
            return ImageObject.createImage(row, col, bands, line);
        } catch (ImageException exc) {
            throw(new IOException(exc.toString()));
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
        return ext.equals("iip");
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

        // write the header
        String hdr;
        hdr = "IIP\n";
        hdr += "# Created using Im2Learn.\n";
        hdr += imageobject.getNumCols() + " " + imageobject.getNumRows() + "\n";
        hdr += imageobject.getNumBands() + "\n";
        hdr += ImageObject.types[imageobject.getType()] + "\n";
        w.write(hdr.getBytes());

        // write the data
        byte[] filedata;
        int maxsize = 0;
        int count = 0;
        int samples = imageobject.getSize();
        switch (imageobject.getType()) {
            case ImageObject.TYPE_BYTE:
                w.write((byte[]) imageobject.getData());
                filedata = new byte[sampleread];
                break;

            case ImageObject.TYPE_SHORT:
                maxsize = sampleread * 2;
                filedata = new byte[maxsize];
                short[] shortdata = (short[]) imageobject.getData();
                for (int i = 0; i < samples; i++) {
                    filedata[count++] = (byte) ((shortdata[i] >> 8) & 0xff);
                    filedata[count++] = (byte) (shortdata[i] & 0xff);
                    if (count == maxsize) {
                        w.write(filedata);
                        count = 0;
                    }
                }
                if (count > 0) {
                    w.write(filedata);
                }
                break;

            case ImageObject.TYPE_USHORT:
                maxsize = sampleread * 2;
                filedata = new byte[maxsize];
                shortdata = (short[]) imageobject.getData();
                for (int i = 0; i < samples; i++) {
                    filedata[count++] = (byte) ((shortdata[i] >> 8) & 0xff);
                    filedata[count++] = (byte) (shortdata[i] & 0xff);
                    if (count == maxsize) {
                        w.write(filedata);
                        count = 0;
                    }
                }
                if (count > 0) {
                    w.write(filedata);
                }
                break;

            case ImageObject.TYPE_INT:
                maxsize = sampleread * 4;
                filedata = new byte[maxsize];
                int[] intdata = (int[]) imageobject.getData();
                for (int i = 0; i < samples; i++) {
                    filedata[count++] = (byte) ((intdata[i] >> 24) & 0xff);
                    filedata[count++] = (byte) ((intdata[i] >> 16) & 0xff);
                    filedata[count++] = (byte) ((intdata[i] >> 8) & 0xff);
                    filedata[count++] = (byte) (intdata[i] & 0xff);
                    if (count == maxsize) {
                        w.write(filedata);
                        count = 0;
                    }
                }
                if (count > 0) {
                    w.write(filedata);
                }
                break;

            case ImageObject.TYPE_LONG:
                maxsize = sampleread * 8;
                filedata = new byte[maxsize];
                long[] longdata = (long[]) imageobject.getData();
                for (int i = 0; i < samples; i++) {
                    filedata[count++] = (byte) ((longdata[i] >> 56) & 0xff);
                    filedata[count++] = (byte) ((longdata[i] >> 48) & 0xff);
                    filedata[count++] = (byte) ((longdata[i] >> 40) & 0xff);
                    filedata[count++] = (byte) ((longdata[i] >> 32) & 0xff);
                    filedata[count++] = (byte) ((longdata[i] >> 24) & 0xff);
                    filedata[count++] = (byte) ((longdata[i] >> 16) & 0xff);
                    filedata[count++] = (byte) ((longdata[i] >> 8) & 0xff);
                    filedata[count++] = (byte) (longdata[i] & 0xff);
                    if (count == maxsize) {
                        w.write(filedata);
                        count = 0;
                    }
                }
                if (count > 0) {
                    w.write(filedata);
                }
                break;

            case ImageObject.TYPE_FLOAT:
                maxsize = sampleread * 4;
                filedata = new byte[maxsize];
                float[] floatdata = (float[]) imageobject.getData();
                int t;
                for (int i = 0; i < samples; i++) {
                    t = Float.floatToIntBits(floatdata[i]);
                    filedata[count++] = (byte) ((t >> 24) & 0xff);
                    filedata[count++] = (byte) ((t >> 16) & 0xff);
                    filedata[count++] = (byte) ((t >> 8) & 0xff);
                    filedata[count++] = (byte) (t & 0xff);
                    if (count == maxsize) {
                        w.write(filedata);
                        count = 0;
                    }
                }
                if (count > 0) {
                    w.write(filedata);
                }
                break;

            case ImageObject.TYPE_DOUBLE:
                maxsize = sampleread * 8;
                filedata = new byte[maxsize];
                double[] doubledata = (double[]) imageobject.getData();
                long l;
                for (int i = 0; i < samples; i++) {
                    l = Double.doubleToLongBits(doubledata[i]);
                    filedata[count++] = (byte) ((l >> 56) & 0xff);
                    filedata[count++] = (byte) ((l >> 48) & 0xff);
                    filedata[count++] = (byte) ((l >> 40) & 0xff);
                    filedata[count++] = (byte) ((l >> 32) & 0xff);
                    filedata[count++] = (byte) ((l >> 24) & 0xff);
                    filedata[count++] = (byte) ((l >> 16) & 0xff);
                    filedata[count++] = (byte) ((l >> 8) & 0xff);
                    filedata[count++] = (byte) (l & 0xff);
                    if (count == maxsize) {
                        w.write(filedata);
                        count = 0;
                    }
                }
                if (count > 0) {
                    w.write(filedata);
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
        return new String[]{"iip"};
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
            if (data[start] == '#') {
                readLine();
            }
            int cnt = start;
            int end = 0;
            for (; ;) {
                if (cnt == len) {
                    result += new String(data, start, end);
                    len = fis.read(data);
                    cnt = 0;
                    start = 0;
                    end = 0;
                }
                if ((data[cnt] == '\n') || (data[cnt] == '\t') || (data[cnt] == ' ')) {
                    result += new String(data, start, end);
                    len = len - (cnt - start);
                    start = cnt;
                    if (data[start] == '\n') {
                        if (len == 0) {
                            len = fis.read(data);
                        }
                        if ((len != 0) && (data[start+1] == '\r')) {
                            start++;
                            len--;
                        }
                    }
                    if (len != 0) {
                        start++;
                        len--;
                    }
                    return result;
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
                    if (len != 0 && data[start + 1] == '\r') {
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
         * @return the number of data values read.
         * @throws IOException if an error occurred reading the file.
         */
        public int readArray(ImageObject imgobj) throws IOException {
            int cnt = 0;
            int left = imgobj.getSize();
            int size = left;

            imgobj.createArray();
            switch (imgobj.getType()) {
                case ImageObject.TYPE_BYTE:
                    while (left > 0) {
                        ImageLoader.fireProgress(cnt, size);
                        if (len == 0) {
                            len = fis.read(data, len, data.length - len);
                            start = 0;
                            if (len == 0) {
                                return cnt;
                            }
                        }
                        System.arraycopy(data, start, imgobj.getData(), cnt, len);
                        cnt += len;
                        left -= len;
                        len = 0;
                    }
                    break;

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
                        imgobj.set(cnt, ((data[start] & 0xff) << 8) | (data[start + 1] & 0xff));
                        start += 2;
                        len -= 2;
                        left--;
                        cnt++;
                    }
                    break;

                case ImageObject.TYPE_INT:
                    while (left > 0) {
                        ImageLoader.fireProgress(cnt, size);
                        if (len < 4) {
                            System.arraycopy(data, start, data, 0, len);
                            len += fis.read(data, len, data.length - len);
                            start = 0;
                            if (len < 4) {
                                return cnt;
                            }
                        }
                        imgobj.set(cnt, ((data[start] & 0xff) << 24) |
                                        ((data[start + 1] & 0xff) << 16) |
                                        ((data[start + 2] & 0xff) << 8) |
                                        (data[start + 3] & 0xff));
                        start += 4;
                        len -= 4;
                        left--;
                        cnt++;
                    }
                    break;

                case ImageObject.TYPE_LONG:
                    while (left > 0) {
                        if (len < 8) {
                            System.arraycopy(data, start, data, 0, len);
                            len += fis.read(data, len, data.length - len);
                            start = 0;
                            if (len < 8) {
                                return cnt;
                            }
                        }
                        imgobj.set(cnt, ((long)(data[start] & 0xff) << 56) | ((long)(data[start + 1] & 0xff) << 48) |
                                        ((long)(data[start + 2] & 0xff) << 40) | ((long)(data[start + 3] & 0xff) << 32) |
                                        ((long)(data[start + 4] & 0xff) << 24) | ((long)(data[start + 5] & 0xff) << 16) |
                                        ((long)(data[start + 6] & 0xff) << 8) | (long)(data[start + 7] & 0xff));
                        start += 8;
                        len -= 8;
                        left--;
                        cnt++;
                    }
                    break;

                case ImageObject.TYPE_FLOAT:
                    while (left > 0) {
                        ImageLoader.fireProgress(cnt, size);
                        if (len < 4) {
                            System.arraycopy(data, start, data, 0, len);
                            len += fis.read(data, len, data.length - len);
                            start = 0;
                            if (len < 4) {
                                return cnt;
                            }
                        }
                        imgobj.set(cnt, Float.intBitsToFloat(((data[start] & 0xff) << 24) |
                                                             ((data[start + 1] & 0xff) << 16) |
                                                             ((data[start + 2] & 0xff) << 8) |
                                                             (data[start + 3] & 0xff)));
                        start += 4;
                        len -= 4;
                        left--;
                        cnt++;
                    }
                    break;

                case ImageObject.TYPE_DOUBLE:
                    while (left > 0) {
                        ImageLoader.fireProgress(cnt, size);
                        if (len < 8) {
                            System.arraycopy(data, start, data, 0, len);
                            len += fis.read(data, len, data.length - len);
                            start = 0;
                            if (len < 8) {
                                return cnt;
                            }
                        }
                        imgobj.set(cnt, Double.longBitsToDouble(((long)(data[start] & 0xff) << 56) | ((long)(data[start + 1] & 0xff) << 48) |
                                                                ((long)(data[start + 2] & 0xff) << 40) | ((long)(data[start + 3] & 0xff) << 32) |
                                                                ((long)(data[start + 4] & 0xff) << 24) | ((long)(data[start + 5] & 0xff) << 16) |
                                                                ((long)(data[start + 6] & 0xff) << 8) | (long)(data[start + 7] & 0xff)));
                        start += 8;
                        len -= 8;
                        left--;
                        cnt++;
                    }
                    break;
            }
            return cnt;
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
