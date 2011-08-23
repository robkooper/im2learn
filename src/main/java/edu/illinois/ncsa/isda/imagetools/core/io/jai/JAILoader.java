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

import com.sun.media.jai.codec.ImageCodec;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageException;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.imagetools.core.datatype.SubArea;
import edu.illinois.ncsa.isda.imagetools.core.io.ImageLoader;
import edu.illinois.ncsa.isda.imagetools.core.io.ImageReader;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 * Created by IntelliJ IDEA. User: kooper Date: Jul 27, 2004 Time: 3:48:10 PM To
 * change this template use File | Settings | File Templates.
 *
 * @author Sang-Chul Lee
 * @author Rob Kooper
 * @version 2.0
 */
public class JAILoader implements ImageReader {
    private String[] ext = null;

    /**
     * Default constructor, will try and use JAI. If JAI not installed this will
     * fail and throw an exception.
     */
    public JAILoader() throws NoClassDefFoundError {
        if (!JAIutil.haveJAI()) {
            throw(new NoClassDefFoundError("No JAI installed."));
        }

        Enumeration e = ImageCodec.getCodecs();
        ArrayList lst = new ArrayList();
        while (e.hasMoreElements()) {
            lst.add(((ImageCodec) e.nextElement()).getFormatName());
        }
        ext = new String[lst.size()];
        lst.toArray(ext);
    }

    /**
     * Try and find a reader, if one found we can actually read the file.
     *
     * @param filename of file to be read.
     * @param hdr      used for magic number
     * @return true if image can be read.
     */
    public boolean canRead(String filename, byte[] hdr) {
    	for(Enumeration<ImageCodec> e=ImageCodec.getCodecs(); e.hasMoreElements(); ) {
    		if (e.nextElement().isFormatRecognized(hdr)) {
    			return true;
    		}
    	}
    	return false;
    }

    /**
     * Loads an image and returns the imageobject containing the image.
     *
     * @param filename of the file to load.
     * @param subarea  of the file to load, or null to load full image.
     * @param sampling is the sampling that needs to be done.
     * @return the imageobject containing the loaded image
     * @throws java.io.IOException if an error occurrs reading the file.
     */
    public ImageObject readImage(String filename, SubArea subarea, int sampling) throws IOException, ImageException {
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
    public ImageObject readImageHeader(String filename) throws IOException, ImageException {
        return readImage(filename, null, 1, true);
    }

    /**
     * Return a list of extentions this class can read.
     *
     * @return a list of extentions that are understood by this class.
     */
    public String[] readExt() {
        return ext;
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
    public ImageObject readImage(String filename, SubArea subarea, int sampling, boolean header) throws IOException, ImageException {
        RenderedOp rop = JAI.create("fileload", filename);
        return JAIutil.getImageObject(rop);
    }

    public String getDescription() {
        return "JAI Loader";
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
