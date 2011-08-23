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

import java.io.File;
import java.io.IOException;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageException;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.imagetools.core.geo.projection.Projection;
import edu.illinois.ncsa.isda.imagetools.core.io.dem.DEMLoader;


public class DemLoaderTest extends ImageLoaderTest {
    DEMLoader loader;

    public DemLoaderTest(String testcase) {
        super(testcase);
        checkproperty = false;
    }
        
    protected void setUp() throws Exception {
        super.setUp();
        loader = new DEMLoader();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        loader = null;
    }

    @Override
    protected ImageObject createImage(int type, String ext) throws ImageException {
        ImageObject imgobj = super.createImage(type, ext);
        imgobj.setProperty(ImageObject.GEOINFO, new Projection());
        return imgobj;
    }

    protected String[] getExt() {
        return loader.writeExt();
    }

    protected ImageObject saveload(File file, ImageObject imgobj, String ext) throws Exception {
        String filename = file.getAbsolutePath() + "." + ext;
        loader.writeImage(filename, imgobj);
        return loader.readImage(filename, null, 1);
    }

    // DEM files are always saved as double!
    @Override
    public void testByteType() throws Exception {
    }

    @Override
    public void testShortType() throws Exception {
    }

    @Override
    public void testUShortType() throws Exception {
    }    

    @Override
    public void testIntType() throws Exception {
    }

    @Override
    public void testLongType() throws Exception {
    }

    // Can not save images of type double    
    @Override
    public void testDoubleType() throws Exception {
        try {
            super.testDoubleType();
        } catch (IOException exc) {
            if (exc.getMessage().equals("Can not save images of type double.")) {
                return;
            }
            throw(exc);
        }
    }

    @Override
    public void testDoubleData() throws Exception {
        try {
            super.testDoubleData();
        } catch (IOException exc) {
            if (exc.getMessage().equals("Can not save images of type double.")) {
                return;
            }
            throw(exc);
        }
    }

    @Override
    public void testDoubleProperty() throws Exception {
        try {
            super.testDoubleProperty();
        } catch (IOException exc) {
            if (exc.getMessage().equals("Can not save images of type double.")) {
                return;
            }
            throw(exc);
        }
    }

    @Override
    public void testDoubleSize() throws Exception {
        try {
            super.testDoubleSize();
        } catch (IOException exc) {
            if (exc.getMessage().equals("Can not save images of type double.")) {
                return;
            }
            throw(exc);
        }
    }
}
