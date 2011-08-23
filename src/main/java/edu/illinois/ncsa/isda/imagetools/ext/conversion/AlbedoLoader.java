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
package edu.illinois.ncsa.isda.imagetools.ext.conversion;

import java.io.IOException;
import java.io.InvalidClassException;
import java.util.HashMap;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObjectShort;
import edu.illinois.ncsa.isda.imagetools.core.io.hdf.HDF;
import edu.illinois.ncsa.isda.imagetools.core.io.hdf.HDFLoader;


public class AlbedoLoader {
    /**
     * Load an albedo image from the HDF file and return the two seperate parts of
     * the albedo image back as two imageobjects.
     * 
     * @param filename the filename of the albedo image.
     * @return an array of the albedo iamges.
     * @throws Exception if the albedo images could not be loaded.
     */
    static public ImageObject[] loadAlbedo(String filename) throws Exception {               
        // first load the Albedo images
        short[][][][] data;
        try {
            data = (short[][][][])HDF.readData(filename);
        } catch (InvalidClassException exc) {
            throw(new IOException("Not an albedo file."));
        }

        int rows  = data.length;
        int cols  = data[0].length;
        int bands = data[0][0].length;
        int imgs  = data[0][0][0].length;
        
        // load the attributes associated with the image
        HashMap props = new HashMap();
        props.put(ImageObject.FILENAME, filename);
        props = HDF.readAttributes(null, filename, null);
        double fill = ((short[])props.get("_FillValue"))[0];
        
        // parse the array into the array of images
        ImageObject result[] = new ImageObject[imgs];
        for(int i = 0; i<imgs; i++) {
            result[i] = new ImageObjectShort(rows, cols, bands);
            result[i].setProperties(props);
            result[i].setInvalidData(fill);

            short[] imgdata = (short[])result[i].getData();
            int idx = 0;
            for(int r = 0; r<rows; r++) {
                for(int c=0; c<cols; c++) {
                    for(int b=0; b<bands; b++) {
                        imgdata[idx++] = data[r][c][b][i];    
                    }
                }
            }
        }
        
        // load the EOS attributes into the first image (projection)
        // since the images share the same properties, all images will
        // now have a projection
        HDFLoader.readEOSAttributes(result[0], filename);

        return result;
    }

    /**
     * Load an albedo image from the HDF file and return the two seperate parts of
     * the albedo image for a specific band back as two imageobjects
     * 
     * @param filename the filename of the albedo image.
     * @param band band which we want to load
     * @return an array of the albedo images.
     * @throws Exception if the albedo images could not be loaded.
     */
    static public ImageObject[] loadAlbedoSpecificBand(String filename, int band) throws Exception {               
        // first load the Albedo images
        short[][][][] data;
        try {
            data = (short[][][][])HDF.readData(filename);
        } catch (InvalidClassException exc) {
            throw(new IOException("Not an albedo file."));
        }

        int rows  = data.length;
        int cols  = data[0].length;
        int imgs  = data[0][0][0].length;
        
        // load the attributes associated with the image
        HashMap props = new HashMap();
        props.put(ImageObject.FILENAME, filename);
        props = HDF.readAttributes(null, filename, null);
        double fill = ((short[])props.get("_FillValue"))[0];
        
        // parse the array into the array of images
        ImageObject result[] = new ImageObject[imgs];
        for(int i = 0; i<imgs; i++) {
            result[i] = new ImageObjectShort(rows, cols, 1);
            result[i].setProperties(props);
            result[i].setInvalidData(fill);

            short[] imgdata = (short[])result[i].getData();
            int idx = 0;
            for(int r = 0; r<rows; r++) {
                for(int c=0; c<cols; c++) { 
                	imgdata[idx++] = data[r][c][band][i];    
                }
            }
        }
        
        // load the EOS attributes into the first image (projection)
        // since the images share the same properties, all images will
        // now have a projection
        HDFLoader.readEOSAttributes(result[0], filename);

        return result;
    }
}
