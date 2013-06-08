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

import edu.illinois.ncsa.isda.im2learn.core.datatype.*;


import junit.framework.TestCase;

abstract public class ImageLoaderTest extends TestCase {
    protected File file;

    protected boolean checkproperty = true;

    public ImageLoaderTest(String testcase) {
        super(testcase);
    }

    protected void setUp() throws Exception {
        file = File.createTempFile("TEST", "");
        file.deleteOnExit();
    }

    protected void tearDown() throws Exception {
        file.delete();
    }

    protected ImageObject createImage(int type, String ext) throws ImageException {
        ImageObject imgobj = ImageObject.createImage(3, 2, 1, type);

        imgobj.set(0, 0);
        imgobj.set(1, 10);
        imgobj.set(2, imgobj.getTypeMin());
        imgobj.set(3, imgobj.getTypeMax());
        imgobj.set(4, 50);
        imgobj.set(5, 200);

        imgobj.setProperty("TEST", "HELLO");

        return imgobj;
    }

    protected abstract ImageObject saveload(File file, ImageObject imgobj,
            String ext) throws Exception;

    protected abstract String[] getExt();
    
    protected void checkSize(int type, int ext) throws Exception {
        ImageObject test = createImage(type, getExt()[ext]);
        ImageObject imgobj = saveload(file, test, getExt()[ext]);
        assertEquals(getExt()[ext], test.getNumCols(), imgobj.getNumCols());
        assertEquals(getExt()[ext], test.getNumRows(), imgobj.getNumRows());
        assertEquals(getExt()[ext], test.getNumBands(), imgobj.getNumBands());
    }

    protected void checkType(int type, int ext) throws Exception {
        ImageObject test = createImage(type, getExt()[ext]);
        ImageObject imgobj = saveload(file, test, getExt()[ext]);
        assertEquals(getExt()[ext], test.getType(), imgobj.getType());
    }
    
    protected void checkProperty(int type, int ext, String property) throws Exception {
        if (!checkproperty)
            return;
        ImageObject test = createImage(type, getExt()[ext]);
        ImageObject imgobj = saveload(file, test, getExt()[ext]);
        assertEquals(getExt()[ext], test.getProperty(property), imgobj.getProperty(property));
    }

    protected void checkData(int type, int ext) throws Exception {
        ImageObject test = createImage(type, getExt()[ext]);
        ImageObject imgobj = saveload(file, test, getExt()[ext]);
        for (int i = 0; i < test.getSize(); i++) {
            switch (type) {
            case ImageObject.TYPE_BYTE:
                assertEquals(getExt()[ext] + " " + i, test.getByte(i), imgobj.getByte(i), 0);
                break;
            case ImageObject.TYPE_SHORT:
                assertEquals(getExt()[ext] + " " + i, test.getShort(i), imgobj.getShort(i), 0);
                break;
            case ImageObject.TYPE_USHORT:
            case ImageObject.TYPE_INT:
                assertEquals(getExt()[ext] + " " + i, test.getInt(i), imgobj.getInt(i), 0);
                break;
            case ImageObject.TYPE_LONG:
                assertEquals(getExt()[ext] + " " + i, test.getLong(i), imgobj.getLong(i), 0);
                break;
            case ImageObject.TYPE_FLOAT:
                assertEquals(getExt()[ext] + " " + i, test.getFloat(i), imgobj.getFloat(i), 0);
                break;
            case ImageObject.TYPE_DOUBLE:
            default:
                assertEquals(getExt()[ext] + " " + i, test.getDouble(i), imgobj.getDouble(i), 0);
            }
        }
    }
    
    // ----------------------------------------------------------------------
    // BYTE IMAGE
    // ----------------------------------------------------------------------
    public void testByteSize() throws Exception {
        int type = ImageObject.TYPE_BYTE;
        for (int e = 0; e < getExt().length; e++) {
            checkSize(type, e);
        }
    }

    public void testByteType() throws Exception {
        int type = ImageObject.TYPE_BYTE;
        for (int e = 0; e < getExt().length; e++) {
            checkType(type, e);
        }
    }

    public void testByteProperty() throws Exception {
        int type = ImageObject.TYPE_BYTE;
        for (int e = 0; e < getExt().length; e++) {
            checkProperty(type, e, "TEST");
        }
    }

    public void testByteData() throws Exception {
        int type = ImageObject.TYPE_BYTE;
        for (int e = 0; e < getExt().length; e++) {
            checkData(type, e);
        }
    }

    // ----------------------------------------------------------------------
    // SHORT IMAGE
    // ----------------------------------------------------------------------
    public void testShortSize() throws Exception {
        int type = ImageObject.TYPE_SHORT;
        for (int e = 0; e < getExt().length; e++) {
            checkSize(type, e);
        }
    }

    public void testShortType() throws Exception {
        int type = ImageObject.TYPE_SHORT;
        for (int e = 0; e < getExt().length; e++) {
            checkType(type, e);
        }
    }

    public void testShortProperty() throws Exception {
        int type = ImageObject.TYPE_SHORT;
        for (int e = 0; e < getExt().length; e++) {
            checkProperty(type, e, "TEST");
        }
    }

    public void testShortData() throws Exception {
        int type = ImageObject.TYPE_SHORT;
        for (int e = 0; e < getExt().length; e++) {
            checkData(type, e);
        }
    }

    // ----------------------------------------------------------------------
    // USHORT IMAGE
    // ----------------------------------------------------------------------
    public void testUShortSize() throws Exception {
        int type = ImageObject.TYPE_USHORT;
        for (int e = 0; e < getExt().length; e++) {
            checkSize(type, e);
        }
    }

    public void testUShortType() throws Exception {
        int type = ImageObject.TYPE_USHORT;
        for (int e = 0; e < getExt().length; e++) {
            checkType(type, e);
        }
    }

    public void testUShortProperty() throws Exception {
        int type = ImageObject.TYPE_USHORT;
        for (int e = 0; e < getExt().length; e++) {
            checkProperty(type, e, "TEST");
        }
    }

    public void testUShortData() throws Exception {
        int type = ImageObject.TYPE_USHORT;
        for (int e = 0; e < getExt().length; e++) {
            checkData(type, e);
        }
    }

    // ----------------------------------------------------------------------
    // INT IMAGE
    // ----------------------------------------------------------------------
    public void testIntSize() throws Exception {
        int type = ImageObject.TYPE_INT;
        for (int e = 0; e < getExt().length; e++) {
            checkSize(type, e);
        }
    }

    public void testIntType() throws Exception {
        int type = ImageObject.TYPE_INT;
        for (int e = 0; e < getExt().length; e++) {
            checkType(type, e);
        }
    }

    public void testIntProperty() throws Exception {
        int type = ImageObject.TYPE_INT;
        for (int e = 0; e < getExt().length; e++) {
            checkProperty(type, e, "TEST");
        }
    }

    public void testIntData() throws Exception {
        int type = ImageObject.TYPE_INT;
        for (int e = 0; e < getExt().length; e++) {
            checkData(type, e);
        }
    }

    // ----------------------------------------------------------------------
    // LONG IMAGE
    // ----------------------------------------------------------------------
    public void testLongSize() throws Exception {
        int type = ImageObject.TYPE_LONG;
        for (int e = 0; e < getExt().length; e++) {
            checkSize(type, e);
        }
    }

    public void testLongType() throws Exception {
        int type = ImageObject.TYPE_LONG;
        for (int e = 0; e < getExt().length; e++) {
            checkType(type, e);
        }
    }

    public void testLongProperty() throws Exception {
        int type = ImageObject.TYPE_LONG;
        for (int e = 0; e < getExt().length; e++) {
            checkProperty(type, e, "TEST");
        }
    }

    public void testLongData() throws Exception {
        int type = ImageObject.TYPE_LONG;
        for (int e = 0; e < getExt().length; e++) {
            checkData(type, e);
        }
    }

    // ----------------------------------------------------------------------
    // FLOAT IMAGE
    // ----------------------------------------------------------------------
    public void testFloatSize() throws Exception {
        int type = ImageObject.TYPE_FLOAT;
        for (int e = 0; e < getExt().length; e++) {
            checkSize(type, e);
        }
    }

    public void testFloatType() throws Exception {
        int type = ImageObject.TYPE_FLOAT;
        for (int e = 0; e < getExt().length; e++) {
            checkType(type, e);
        }
    }

    public void testFloatProperty() throws Exception {
        int type = ImageObject.TYPE_FLOAT;
        for (int e = 0; e < getExt().length; e++) {
            checkProperty(type, e, "TEST");
        }
    }

    public void testFloatData() throws Exception {
        int type = ImageObject.TYPE_FLOAT;
        for (int e = 0; e < getExt().length; e++) {
            checkData(type, e);
        }
    }

    // ----------------------------------------------------------------------
    // DOUBLE IMAGE
    // ----------------------------------------------------------------------
    public void testDoubleSize() throws Exception {
        int type = ImageObject.TYPE_DOUBLE;
        for (int e = 0; e < getExt().length; e++) {
            checkSize(type, e);
        }
    }

    public void testDoubleType() throws Exception {
        int type = ImageObject.TYPE_DOUBLE;
        for (int e = 0; e < getExt().length; e++) {
            checkType(type, e);
        }
    }

    public void testDoubleProperty() throws Exception {
        int type = ImageObject.TYPE_DOUBLE;
        for (int e = 0; e < getExt().length; e++) {
            checkProperty(type, e, "TEST");
        }
    }

    public void testDoubleData() throws Exception {
        int type = ImageObject.TYPE_DOUBLE;
        for (int e = 0; e < getExt().length; e++) {
            checkData(type, e);
        }
    }
}
