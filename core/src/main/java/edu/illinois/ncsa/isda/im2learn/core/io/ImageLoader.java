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
package edu.illinois.ncsa.isda.im2learn.core.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ServiceLoader;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageException;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.im2learn.core.datatype.SubArea;
import edu.illinois.ncsa.isda.im2learn.core.display.ProgressListener;
import edu.illinois.ncsa.isda.im2learn.core.io.csv.CSVLoader;
import edu.illinois.ncsa.isda.im2learn.core.io.dem.DEMLoader;
import edu.illinois.ncsa.isda.im2learn.core.io.envi.ENVILoader;
import edu.illinois.ncsa.isda.im2learn.core.io.fits.FitsLoader;
import edu.illinois.ncsa.isda.im2learn.core.io.hgt.HGTLoader;
import edu.illinois.ncsa.isda.im2learn.core.io.iip.IIPLoader;
import edu.illinois.ncsa.isda.im2learn.core.io.imageio.ImageIOLoader;
import edu.illinois.ncsa.isda.im2learn.core.io.object.ObjectLoader;
import edu.illinois.ncsa.isda.im2learn.core.io.pnm.PNMLoader;
import edu.illinois.ncsa.isda.im2learn.core.io.srtm.SRTMLoader;
import edu.illinois.ncsa.isda.im2learn.core.io.tiff.TIFFLoader;
import edu.illinois.ncsa.isda.im2learn.core.io.xml.XMLLoader;

/**
 * This class contains only static methods. The class will be able to read and
 * write all supported fileformats. The decision which reader or writer to use
 * is based on the header information of the file to read, and the extention of
 * the file to write.
 * <p/>
 * If a subarea and sampling is provided to the readers, it will first use the
 * subarea to select a region from the image and then apply subsampling to that
 * region. If the user selects (0, 0) x (100, 100) for subarea and subsampling
 * of 5, the final image will be (20 x 20) starting at pixel 0. If the user had
 * selected (9, 9) x (14, 14) the image returned would be (2, 2) starting at
 * pixle (9, 9).
 * 
 * @author Rob Kooper
 */
public class ImageLoader {
    static private Vector<ImageReader>      readers   = new Vector<ImageReader>();
    static private Vector<ImageWriter>      writers   = new Vector<ImageWriter>();
    static private Vector<ProgressListener> listeners = new Vector<ProgressListener>();
    static private Log                      logger    = LogFactory.getLog(ImageLoader.class);

    /**
     * Add the default loaders to the set of know loaders.
     */
    static {
        try {
            IIPLoader loader = new IIPLoader();
            readers.add(loader);
            writers.add(loader);
        } catch (Throwable thr) {
            logger.warn("Error registering IIPloader, will not be able to handle IIP files.");
            logger.debug(thr);
        }

        try {
            ENVILoader loader = new ENVILoader();
            readers.add(loader);
            writers.add(loader);
        } catch (Throwable thr) {
            logger.warn("Error registering ENVILoader, will not be able to handle ENVI files.");
            logger.debug(thr);
        }

        // always keep this after the ENVILoader & ANALYZELoader
        try {
            DEMLoader loader = new DEMLoader();
            readers.add(loader);
            writers.add(loader);
        } catch (Throwable thr) {
            logger.warn("Error registering DEMLoader, will not be able to handle DEM files.");
            logger.debug(thr);
        }

        try {
            SRTMLoader loader = new SRTMLoader();
            readers.add(loader);
            writers.add(loader);
        } catch (Throwable thr) {
            logger.warn("Error registering SRTMLoader, will not be able to handle SRTM files.");
            logger.debug(thr);
        }

        try {
            PNMLoader loader = new PNMLoader();
            readers.add(loader);
            writers.add(loader);
        } catch (Throwable thr) {
            logger.warn("Error registering PNMLoader, will not be able to handle PNM files.");
            logger.debug(thr);
        }

        try {
            TIFFLoader loader = new TIFFLoader();
            readers.add(loader);
            writers.add(loader);
        } catch (Throwable thr) {
            logger.warn("Error registering TIFFLoader, will not be able to handle TIFF files.");
            logger.debug(thr);
        }

        try {
            CSVLoader loader = new CSVLoader();
            writers.add(loader);
            readers.add(loader); // no actual CSV reading support for now.
        } catch (Throwable thr) {
            logger.warn("Error registering CSV Loader, will not be able to save CSV files.");
            logger.debug(thr);
        }

        try {
            HGTLoader loader = new HGTLoader();
            readers.add(loader);
        } catch (Throwable thr) {
            logger.warn("Error registering HGT Loader, will not be able to read HGT files.");
            logger.debug(thr);
        }

        try {
            ObjectLoader loader = new ObjectLoader();
            readers.add(loader);
            writers.add(loader);
        } catch (Throwable thr) {
            logger.warn("Error registering ObjectLoader, will not be able to handle serialized files.");
            logger.debug(thr);
        }

        try {
            XMLLoader loader = new XMLLoader();
            readers.add(loader);
            writers.add(loader);
        } catch (Throwable thr) {
            logger.warn("Error registering XMLLoader, will not be able to handle XML files.");
            logger.debug(thr);
        }

        try {
            FitsLoader loader = new FitsLoader();
            readers.add(loader);
        } catch (Throwable thr) {
            logger.warn("Error registering FitsLoader, will not be able to handle FITS files.");
            logger.debug(thr);
        }

        // add custom file readers/writers
        ServiceLoader<ImageReader> slReaders = ServiceLoader.load(ImageReader.class);
        for (ImageReader ir : slReaders) {
            try {
                readers.add(ir);
            } catch (Throwable thr) {
                logger.warn("Error registering loader.", thr);
                logger.debug(thr);
            }
        }

        ServiceLoader<ImageWriter> slWriters = ServiceLoader.load(ImageWriter.class);
        for (ImageWriter iw : slWriters) {
            try {
                writers.add(iw);
            } catch (Throwable thr) {
                logger.warn("Error registering loader.", thr);
                logger.debug(thr);
            }
        }

        // this always last, this way our loaders will get called before
        // the system loaders.
        try {
            ImageIOLoader loader = new ImageIOLoader();
            readers.add(loader);
            writers.add(loader);
        } catch (Throwable thr) {
            logger.warn("Error registering ImageIOLoader, will not have ImageIO support.");
            logger.debug(thr);
        }
    }

    /**
     * Adds a custom reader to the list of known readers. The reader will be
     * added at the specified index. If the index is less than 0 the reader is
     * added to begining, if it is larger than the list, it is added to the end.
     * The list of readers is always checked from first to last element.
     * 
     * @param reader
     *            to be added.
     * @param idx
     *            location in list of readers to add new reader.
     */
    static public void addReader(ImageReader reader, int idx) {
        if (idx < 0) {
            readers.add(0, reader);
        } else if (idx >= readers.size()) {
            readers.add(reader);
        } else {
            readers.add(idx, reader);
        }
    }

    /**
     * Returns a list of all image readers.
     * 
     * @return list of image readers.
     */
    static public Vector<ImageReader> getReaders() {
        return readers;
    }

    /**
     * Adds a custom writer to the list of known writers. The writer will be
     * added at the specified index. If the index is less than 0 the writer is
     * added to begining, if it is larger than the list, it is added to the end.
     * The list of writers is always checked from first to last element.
     * 
     * @param writer
     *            to be added.
     * @param idx
     *            location in list of writers to add new writer.
     */
    static public void addWriter(ImageWriter writer, int idx) {
        if (idx < 0) {
            writers.add(0, writer);
        } else if (idx >= readers.size()) {
            writers.add(writer);
        } else {
            writers.add(idx, writer);
        }
    }

    /**
     * Returns a list of all image writers.
     * 
     * @return list of image writers.
     */
    static public Vector<ImageWriter> getWriters() {
        return writers;
    }

    static public int getImageCount(String filename) throws IOException {
        return getImageCount(filename, null);
    }

    static public int getImageCount(String filename, String loader) throws IOException {
        if (filename == null) {
            throw (new IOException("No filename specified."));
        }
        String tmpname = checkRemoteFile(filename);
        String[] parts = tmpname.split("#", 2);
        // open the file to read first set of bytes for magic matching
        FileInputStream fis = new FileInputStream(parts[0]);
        byte[] tmp = new byte[100];
        int len = fis.read(tmp);
        if (len < 0) {
            throw (new IOException("There is no more data because the end of the file has been reached for " + filename));
        }
        byte[] hdr = new byte[len];
        System.arraycopy(tmp, 0, hdr, 0, len);
        tmp = null;
        fis.close();

        // find an appropriate reader
        for (ImageReader reader : getReaders()) {
            if (reader.canRead(tmpname, hdr) && ((loader == null) || reader.getDescription().equalsIgnoreCase(loader))) {
                try {
                    return reader.getImageCount(filename);
                } catch (ImageException e) {
                    logger.debug("Could not get imagecount.", e);
                }
            }
        }
        throw (new IOException("No appropriate loader found for " + filename));
    }

    /**
     * Loads an image header and returns the imageobject containing the image.
     * 
     * @param filename
     *            of the file to load the header.
     * @return the imageobject containing the loaded image header
     * @throws IOException
     *             if an error occurrs reading the file.
     */
    static public ImageObject readImageHeader(String filename) throws IOException {
        return readImageHeader(filename, 1, null);
    }

    /**
     * Loads an image header and returns the imageobject containing the image.
     * 
     * @param filename
     *            of the file to load the header.
     * @param loader
     *            the explicit loader to use.
     * @return the imageobject containing the loaded image header
     * @throws IOException
     *             if an error occurrs reading the file.
     */
    static public ImageObject readImageHeader(String filename, String loader) throws IOException {
        return readImageHeader(filename, 1, loader);
    }

    /**
     * Loads an image header and returns the imageobject containing the image.
     * 
     * @param filename
     *            of the file to load the header.
     * @param loader
     *            the explicit loader to use.
     * @param index
     *            the index of the image to load.
     * @return the imageobject containing the loaded image header
     * @throws IOException
     *             if an error occurrs reading the file.
     */
    static public ImageObject readImageHeader(String filename, int index, String loader) throws IOException {
        if (filename == null) {
            throw (new IOException("No filename specified."));
        }
        String tmpname = checkRemoteFile(filename);
        String[] parts = tmpname.split("#", 2);
        // open the file to read first set of bytes for magic matching
        FileInputStream fis = new FileInputStream(parts[0]);
        byte[] tmp = new byte[100];
        int len = fis.read(tmp);
        if (len < 0) {
            throw (new IOException("There is no more data because the end of the file has been reached for " + filename));
        }
        byte[] hdr = new byte[len];
        System.arraycopy(tmp, 0, hdr, 0, len);
        tmp = null;
        fis.close();

        // find an appropriate reader
        for (ImageReader reader : getReaders()) {
            if (reader.canRead(tmpname, hdr) && ((loader == null) || reader.getDescription().equalsIgnoreCase(loader))) {
                try {
                    logger.debug("Using " + reader + " to load " + tmpname);
                    ImageObject obj = reader.readImageHeader(tmpname, index);
                    obj.setProperty(ImageObject.FILENAME, filename);
                    return obj;
                } catch (IOException exc) {
                    logger.debug("Error loading file [" + filename + "].", exc);
                } catch (ImageException exc) {
                    logger.debug("Error loading file [" + filename + "].", exc);
                }
            }
        }
        throw (new IOException("No appropriate loader found for " + filename));
    }

    /**
     * Loads an image and returns the imageobject containing the image. This
     * will read the whole image with no subsampling.
     * 
     * @param filename
     *            of the file to load.
     * @return the imageobject containing the loaded image
     * @throws IOException
     *             if an error occurrs reading the file.
     */
    static public ImageObject readImage(String filename) throws IOException {
        return readImage(filename, 1, null, null, 1);
    }

    /**
     * Loads an image and returns the imageobject containing the image with the
     * give sampling.
     * 
     * @param filename
     *            of the file to load.
     * @param sampling
     *            is the sampling that needs to be done.
     * @return the imageobject containing the loaded image
     * @throws IOException
     *             if an error occurrs reading the file.
     */
    static public ImageObject readImage(String filename, int sampling) throws IOException {
        return readImage(filename, 1, null, null, sampling);
    }

    /**
     * Loads an image and returns the imageobject containing the image with no
     * subsampling.
     * 
     * @param filename
     *            of the file to load.
     * @param subarea
     *            of the file to load, or null to load full image.
     * @return the imageobject containing the loaded image
     * @throws IOException
     *             if an error occurrs reading the file.
     */
    static public ImageObject readImage(String filename, SubArea subarea) throws IOException {
        logger.debug("LOADING : " + filename + " SUBAREA : " + subarea);
        return readImage(filename, 1, null, subarea, 1);
    }

    /**
     * Loads an image and returns the imageobject containing the image.
     * 
     * @param filename
     *            of the file to load.
     * @param subarea
     *            of the file to load, or null to load full image.
     * @param sampling
     *            is the sampling that needs to be done.
     * @return the imageobject containing the loaded image
     * @throws IOException
     *             if an error occurrs reading the file.
     */
    static public ImageObject readImage(String filename, SubArea subarea, int sampling) throws IOException {
        return readImage(filename, 1, null, subarea, sampling);
    }

    /**
     * Loads an image and returns the imageobject containing the image. This
     * will read the whole image with no subsampling.
     * 
     * @param filename
     *            of the file to load.
     * @param loader
     *            the explicit loader to use.
     * @return the imageobject containing the loaded image
     * @throws IOException
     *             if an error occurrs reading the file.
     */
    static public ImageObject readImage(String filename, String loader) throws IOException {
        return readImage(filename, 1, loader, null, 1);
    }

    /**
     * Loads an image and returns the imageobject containing the image with the
     * give sampling.
     * 
     * @param filename
     *            of the file to load.
     * @param loader
     *            the explicit loader to use.
     * @param sampling
     *            is the sampling that needs to be done.
     * @return the imageobject containing the loaded image
     * @throws IOException
     *             if an error occurrs reading the file.
     */
    static public ImageObject readImage(String filename, String loader, int sampling) throws IOException {
        return readImage(filename, 1, loader, null, sampling);
    }

    /**
     * Loads an image and returns the imageobject containing the image with no
     * subsampling.
     * 
     * @param filename
     *            of the file to load.
     * @param loader
     *            the explicit loader to use.
     * @param subarea
     *            of the file to load, or null to load full image.
     * @return the imageobject containing the loaded image
     * @throws IOException
     *             if an error occurrs reading the file.
     */
    static public ImageObject readImage(String filename, String loader, SubArea subarea) throws IOException {
        return readImage(filename, 1, loader, subarea, 1);
    }

    /**
     * Loads an image and returns the imageobject containing the image.
     * 
     * @param filename
     *            of the file to load.
     * @param loader
     *            the explicit loader to use.
     * @param subarea
     *            of the file to load, or null to load full image.
     * @param sampling
     *            is the sampling that needs to be done.
     * @return the imageobject containing the loaded image
     * @throws IOException
     *             if an error occurrs reading the file.
     */
    static public ImageObject readImage(String filename, String loader, SubArea subarea, int sampling) throws IOException {
        return readImage(filename, 1, loader, subarea, sampling);
    }

    /**
     * Loads an image and returns the imageobject containing the image.
     * 
     * @param filename
     *            of the file to load.
     * @param index
     *            the index of the image to load.
     * @param loader
     *            the explicit loader to use.
     * @param subarea
     *            of the file to load, or null to load full image.
     * @param sampling
     *            is the sampling that needs to be done.
     * @return the imageobject containing the loaded image
     * @throws IOException
     *             if an error occurrs reading the file.
     */
    static public ImageObject readImage(String filename, int index, String loader, SubArea subarea, int sampling) throws IOException {
        if (filename == null) {
            throw (new IOException("No filename specified."));
        }

        String tempname = checkRemoteFile(filename);
        String parts[] = tempname.split("#", 2);
        // open the file to read first set of bytes for magic matching
        FileInputStream fis = new FileInputStream(parts[0]);
        byte[] tmp = new byte[100];
        int len = fis.read(tmp);
        byte[] hdr = new byte[len];
        System.arraycopy(tmp, 0, hdr, 0, len);
        tmp = null;
        fis.close();

        // find an appropriate reader
        for (ImageReader reader : getReaders()) {
            if (reader.canRead(tempname, hdr) && ((loader == null) || reader.getDescription().equalsIgnoreCase(loader))) {
                try {
                    logger.debug("Using " + reader + " to load " + tempname);
                    ImageObject obj = reader.readImage(tempname, index, subarea, sampling);

                    // check whether this object should be out-of-core.
                    // if it should be but is not, make it out-of-core.
                    if (obj.getSize() > ImageObject.getMaxInCoreSize()) {
                        if (obj.isInCore()) {
                            obj = (ImageObject) obj.clone(); // converts to
                            // out-of-core.
                        }
                    }
                    obj.setProperty(ImageObject.FILENAME, filename);

                    return obj;

                } catch (Exception exc) {
                    logger.debug("Error loading file [" + filename + "].", exc);
                }
            }
        }
        throw (new IOException("No appropriate loader found for " + filename));
    }

    /**
     * Checks to see if the file is a remote file. If it is, load the file to a
     * tempfile and return the filename of the tempfile. The tempfile will be
     * automatically deleted after execution.
     * 
     * @param filename
     *            the file to be checked and potentially downloaded.
     * @return the filename to the existing file.
     * @throws IOException
     *             if an error occured downloading the remote file.
     */
    static private String checkRemoteFile(String filename) throws IOException {
        byte[] buf = new byte[10240];

        String[] parts = filename.split("#", 2);
        boolean local = false;
        try {
            local = new File(parts[0]).exists();
        } catch (SecurityException exc) {
        }
        if (!local) {
            URL url = new URL(parts[0]);
            String ext = "." + getExtention(url.getFile());
            File fp = File.createTempFile("Im2Learn", ext);
            fp.deleteOnExit();
            FileOutputStream fos = new FileOutputStream(fp);
            InputStream inp = url.openStream();
            int count;
            do {
                count = inp.read(buf);
                if (count > 0) {
                    fos.write(buf, 0, count);
                }
            } while (count > 0);
            fos.close();
            parts[0] = fp.getAbsolutePath();
        }

        if (parts.length == 1) {
            return parts[0];
        } else {
            return parts[0] + "#" + parts[1];
        }
    }

    /**
     * Writes the given imageobject to a file.
     * 
     * @param filename
     *            of the new file.
     * @param imageobject
     *            to be written to file.
     * @throws IOException
     *             if an error occurrs writing the file.
     */
    static public void writeImage(String filename, ImageObject imageobject) throws IOException {
        writeImage(filename, null, imageobject);
    }

    /**
     * Writes the given imageobject to a file.
     * 
     * @param filename
     *            of the new file.
     * @param loader
     *            the explicit loader to use.
     * @param imageobject
     *            to be written to file.
     * @throws IOException
     *             if an error occurrs writing the file.
     */
    static public void writeImage(String filename, String loader, ImageObject imageobject) throws IOException {
        if (filename == null) {
            throw (new IOException("No filename specified."));
        }
        if (imageobject == null) {
            throw (new IOException("No image to save."));
        }
        if (imageobject.isHeaderOnly()) {
            throw (new IOException("No image data to save."));
        }
        for (ImageWriter writer : getWriters()) {
            if (writer.canWrite(filename) && ((loader == null) || writer.getDescription().equalsIgnoreCase(loader))) {
                try {
                    logger.debug("Using " + writer + " to write " + filename);
                    writer.writeImage(filename, imageobject);
                    imageobject.setProperty(ImageObject.FILENAME, filename);
                    return;
                } catch (IOException exc) {
                    logger.error("Error saving file [" + filename + "].", exc);
                } catch (ImageException exc) {
                    logger.error("Error saving file [" + filename + "].", exc);
                }
            }
        }
        throw (new IOException("No appropriate writer found for " + filename));
    }

    /**
     * Returns the extention of the filename, null otherwise. Is smart enough to
     * understand the # in filename, such that file.h5#/image.jpg will return h5
     * as extention.
     * 
     * @param filename
     *            of which to return the extention.
     * @return extention of file.
     */
    static public String getExtention(String filename) {
        if (filename == null) {
            return null;
        }

        String parts[] = filename.split("#", 2);
        if (parts[0].length() == 0) {
            return null;
        }

        int idx = parts[0].lastIndexOf(".");
        if (idx <= 0) {
            return null;
        }

        if (parts[0].endsWith(".gz")) {
            idx = parts[0].lastIndexOf(".", idx - 1);
        }

        return parts[0].substring(idx + 1).toLowerCase();
    }

    /**
     * Add the progress listener. This will be called everytime the fireprogress
     * is called. If l is null or already added it will be ignored.
     * 
     * @param l
     *            the progress listener to add.
     */
    static synchronized public void addProgressListener(ProgressListener l) {
        if (!listeners.contains(l)) {
            listeners.add(l);
        }
    }

    /**
     * Remove the progress listener. If l is not added or null this will be
     * ignored.
     * 
     * @param l
     *            the progress listener to remove.
     */
    static synchronized public void removeProgressListener(ProgressListener l) {
        listeners.remove(l);
    }

    /**
     * Notify all listeners of the progress. Send a message to all listeners
     * with the new processed and total values.
     * 
     * @param processed
     *            number of items processed sofar.
     * @param total
     *            number of items that need processing.
     */
    static synchronized public void fireProgress(int processed, int total) {
        for (ProgressListener l : listeners) {
            l.progress(processed, total);
        }
    }
}
