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
package edu.illinois.ncsa.isda.imagetools.core.datatype;

import junit.framework.TestCase;

import java.util.Random;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;

/**
 * This class will check the set and get functions and see if they return the
 * correct values for the byte class.
 */
abstract public class ImageObjectTypeTest extends TestCase {
    private ImageObject imageobject = null;
    private int nocols = 10;
    private int norows = 20;
    private int nobands = 30;
    private double delta = 1e-10;

    public ImageObjectTypeTest(String testcase) {
        super(testcase);
    }

    /**
     * Create the imageobject of the type to be tested with given rows, cols,
     * and bands.
     *
     * @param r is rows in image.
     * @param c is cols in image.
     * @param b is bands in image.
     * @return image of right size and type.
     */
    abstract protected ImageObject create(int r, int c, int b);

    /**
     * Return the type of the image that is checked.
     *
     * @return type of image that is checked.
     */
    abstract protected int getType();

    /**
     * Following function generates random numbers between min and max of type.
     *
     * @return a random number.
     */
    abstract protected double getRandom();


    /**
     * Create an instance that is used for testing. This is called for each test
     * function.
     *
     * @throws Exception if error occurs creating datastructures.
     */
    protected void setUp() throws Exception {
        imageobject = create(norows, nocols, nobands);

    }

    /**
     * Release any datastructures allocated in setUp()
     *
     * @throws Exception if error occurs destroying datastructures.
     */
    protected void tearDown() throws Exception {
        imageobject = null;
    }

    protected static Random random = new Random();

    public void testType() {
        assertEquals("type", getType(), imageobject.getType());

        imageobject.set(0, imageobject.getTypeMin());
        assertEquals("typemin", imageobject.getTypeMin(), imageobject.getDouble(0), delta);

        imageobject.set(0, imageobject.getTypeMax());
        assertEquals("typemax", imageobject.getTypeMax(), imageobject.getDouble(0), delta);

        for (int i = 0; i < imageobject.getSize(); i++) {
            imageobject.set(i, 55);
        }
        imageobject.computeMinMax();
        assertEquals("min", 55.0, imageobject.getMin(), delta);
        assertEquals("max", 55.0, imageobject.getMax(), delta);
    }

    /**
     * Go through the whole image, set each value, and check the value.
     */
    public void testByteValue() {
        int r, c, x, p;

        // check to make sure we can set all values in image.
        for (p = 0, r = 0; r < norows; r++) {
            for (c = 0; c < nocols; c++) {
                for (x = 0; x < nobands; x++, p++) {

                    // check byte values
                    byte b = (byte) getRandom();
                    imageobject.set(p, b);
                    assertEquals("set(i) and getByte(i)", b, imageobject.getByte(p));
                    b = (byte) getRandom();
                    imageobject.setByte(p, b);
                    assertEquals("setByte(i) and getByte(i)", b, imageobject.getByte(p));
                    b = (byte) getRandom();
                    imageobject.set(r, c, x, b);
                    assertEquals("set(r,c,b) and getByte(i)", b, imageobject.getByte(p));
                    b = (byte) getRandom();
                    imageobject.setByte(r, c, x, b);
                    assertEquals("setByte(r,c,b) and getByte(i)", b, imageobject.getByte(p));
                }
            }
        }
    }

    /**
     * Go through the whole image, set each value, and check the value.
     */
    public void testShortValue() {
        int r, c, x, p;

        // check to make sure we can set all values in image.
        for (p = 0, r = 0; r < norows; r++) {
            for (c = 0; c < nocols; c++) {
                for (x = 0; x < nobands; x++, p++) {

                    // check short values
                    short s = (short) getRandom();
                    imageobject.set(p, s);
                    assertEquals("set(i) and getShort(i)", s, imageobject.getShort(p));
                    s = (short) getRandom();
                    imageobject.setShort(p, s);
                    assertEquals("setShort(i) and getShort(i)", s, imageobject.getShort(p));
                    s = (short) getRandom();
                    imageobject.set(r, c, x, s);
                    assertEquals("set(r,c,b) and getShort(i)", s, imageobject.getShort(p));
                    s = (short) getRandom();
                    imageobject.setShort(r, c, x, s);
                    assertEquals("setShort(r,c,b) and getShort(i)", s, imageobject.getShort(p));
                }
            }
        }
    }

    /**
     * Go through the whole image, set each value, and check the value.
     */
    public void testIntValue() {
        int r, c, x, p;

        // check to make sure we can set all values in image.
        for (p = 0, r = 0; r < norows; r++) {
            for (c = 0; c < nocols; c++) {
                for (x = 0; x < nobands; x++, p++) {

                    // check int values
                    int i = (int) getRandom();
                    imageobject.set(p, i);
                    assertEquals("set(i) and getInt(i)", i, imageobject.getInt(p));
                    i = (int) getRandom();
                    imageobject.setInt(p, i);
                    assertEquals("setInt(i) and getInt(i)", i, imageobject.getInt(p));
                    i = (int) getRandom();
                    imageobject.set(r, c, x, i);
                    assertEquals("set(r,c,b) and getInt(i)", i, imageobject.getInt(p));
                    i = (int) getRandom();
                    imageobject.setInt(r, c, x, i);
                    assertEquals("setInt(r,c,b) and getInt(i)", i, imageobject.getInt(p));
                }
            }
        }
    }

    /**
     * Go through the whole image, set each value, and check the value.
     */
    public void testLongValue() {
        int r, c, x, p;

        // check to make sure we can set all values in image.
        for (p = 0, r = 0; r < norows; r++) {
            for (c = 0; c < nocols; c++) {
                for (x = 0; x < nobands; x++, p++) {

                    // check long values
                    long l = (long) getRandom();
                    imageobject.set(p, l);
                    assertEquals("set(i) and getLong(i)", l, imageobject.getLong(p));
                    l = (long) getRandom();
                    imageobject.setLong(p, l);
                    assertEquals("setLong(i) and getLong(i)", l, imageobject.getLong(p));
                    l = (long) getRandom();
                    imageobject.set(r, c, x, l);
                    assertEquals("set(r,c,b) and getLong(i)", l, imageobject.getLong(p));
                    l = (long) getRandom();
                    imageobject.setLong(r, c, x, l);
                    assertEquals("setLong(r,c,b) and getLong(i)", l, imageobject.getLong(p));
                }
            }
        }
    }

    /**
     * Go through the whole image, set each value, and check the value.
     */
    public void testFloatValue() {
        int r, c, x, p;

        // check to make sure we can set all values in image.
        for (p = 0, r = 0; r < norows; r++) {
            for (c = 0; c < nocols; c++) {
                for (x = 0; x < nobands; x++, p++) {

                    // check float values
                    float f = (float) getRandom();
                    imageobject.set(p, f);
                    assertEquals("set(i) and getFloat(i)", f, imageobject.getFloat(p), delta);
                    f = (float) getRandom();
                    imageobject.setFloat(p, f);
                    assertEquals("setFloat(i) and getFloat(i)", f, imageobject.getFloat(p), delta);
                    f = (float) getRandom();
                    imageobject.set(r, c, x, f);
                    assertEquals("set(r,c,b) and getFloat(i)", f, imageobject.getFloat(p), delta);
                    f = (float) getRandom();
                    imageobject.setFloat(r, c, x, f);
                    assertEquals("setFloat(r,c,b) and getFloat(i)", f, imageobject.getFloat(p), delta);
                }
            }
        }
    }

    /**
     * Go through the whole image, set each value, and check the value.
     */
    public void testDoubleValue() {
        int r, c, x, p;

        // check to make sure we can set all values in image.
        for (p = 0, r = 0; r < norows; r++) {
            for (c = 0; c < nocols; c++) {
                for (x = 0; x < nobands; x++, p++) {

                    // check double values
                    double d = getRandom();
                    imageobject.set(p, d);
                    assertEquals("set(i) and getDouble(i)", d, imageobject.getDouble(p), delta);
                    d = getRandom();
                    imageobject.setDouble(p, d);
                    assertEquals("setDouble(i) and getDouble(i)", d, imageobject.getDouble(p), delta);
                    d = getRandom();
                    imageobject.set(r, c, x, d);
                    assertEquals("set(r,c,b) and getDouble(i)", d, imageobject.getDouble(p), delta);
                    d = getRandom();
                    imageobject.setDouble(r, c, x, d);
                    assertEquals("setDouble(r,c,b) and getDouble(i)", d, imageobject.getDouble(p), delta);
                }
            }
        }
    }
}
