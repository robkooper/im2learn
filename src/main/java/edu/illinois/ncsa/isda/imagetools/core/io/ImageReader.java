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
package edu.illinois.ncsa.isda.imagetools.core.io;


import java.io.IOException;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageException;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.imagetools.core.datatype.SubArea;

/**
 * This interface needs to be implemented by a filereader. The class will first
 * be asked if it can load this particular file, if it can the read function
 * will be called with the filename. The class will also need to return a list
 * of extentions it can read.
 *
 * @author Rob Kooper
 */
public interface ImageReader {
    /**
     * Returns true if the class can read the file. It should first check to see
     * if the file contains the magic marker. The functions is called with the
     * filename and the first 100 bytes of the file.
     *
     * @param filename of the file to be read.
     * @param hdr      the first 100 bytes of the file.
     * @return true if the file can be read by this class.
     */
    public boolean canRead(String filename, byte[] hdr);

    /**
     * Returns the number of images in the file. Some files can contain multiple
     * images inside a single file, for example PDF. This method will return the
     * number of images inside the file.
     *
     * @param filename of the file to be read.
     * @return the number of images inside this file.
     */
    public int getImageCount(String filename) throws IOException, ImageException;

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
     * @throws IOException    if the file could not be read.
     * @throws ImageException if the file could not be read.
     */
    public ImageObject readImage(String filename, SubArea subarea, int sampling) throws IOException, ImageException;

    /**
     * This function will read the file and return an imageobject that contains
     * the file.
     *
     * @param filename of the file to be read
     * @param index the index of the image to read in case of multiple images.
     * @param subarea  of the file to be read, null if full image is to be
     *                 read.
     * @param sampling is the subsampling that needs to be done on the image as
     *                 it is loaded.
     * @return the file as an imageobject.
     * @throws IOException    if the file could not be read.
     * @throws ImageException if the file could not be read.
     */
    public ImageObject readImage(String filename, int index, SubArea subarea, int sampling) throws IOException, ImageException;

    /**
     * This function will read the file and return an imageobject that contains
     * the information of the image but not the imagedata itself.
     *
     * @param filename of the file to be read
     * @return the file as an imageobject except of the imagedata.
     * @throws IOException    if the file could not be read.
     * @throws ImageException if the file could not be read.
     */
    public ImageObject readImageHeader(String filename) throws IOException, ImageException;

    /**
     * This function will read the file and return an imageobject that contains
     * the information of the image but not the imagedata itself.
     *
     * @param filename of the file to be read
     * @param index the index of the image to read in case of multiple images.
     * @return the file as an imageobject except of the imagedata.
     * @throws IOException    if the file could not be read.
     * @throws ImageException if the file could not be read.
     */
    public ImageObject readImageHeader(String filename, int index) throws IOException, ImageException;

    /**
     * Return a list of extentions this class can read.
     *
     * @return a list of extentions that are understood by this class.
     */
    public String[] readExt();

    /**
     * Return the description of the reader (or writer).
     *
     * @return decription of the reader (or writer)
     */
    public String getDescription();
}
