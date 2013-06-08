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
package edu.illinois.ncsa.isda.im2learn.core.datatype;

import edu.illinois.ncsa.isda.im2learn.core.datatype.*;
import junit.framework.TestCase;

/**
 * Abstract classes are slow. Anthing we can do to speed this up?
 */
public class ImageObjectSpeedTest extends TestCase {
    private int size = 0;
    private int loop = 10;
    private ImageObjectByte imageobjecttype = null;
    private ImageObject imageobject = null;
    private byte[] data = null;

    public ImageObjectSpeedTest(String testcase) {
        super(testcase);
    }

    /**
     * Create an instance that is used for testing. This is called for each test
     * function.
     *
     * @throws Exception if error occurs creating datastructures.
     */
    protected void setUp() throws Exception {
        imageobjecttype = new ImageObjectByte(500, 500, 30);
        imageobject = imageobjecttype;
        data = (byte[]) imageobject.getData();
    }

    /**
     * Release any datastructures allocated in setUp()
     *
     * @throws Exception if error occurs destroying datastructures.
     */
    protected void tearDown() throws Exception {
        imageobjecttype = null;
        imageobject = null;
        data = null;
    }

    /**
     * create an imageobject and an array. This will execute each of the tests
     * on the data, writing and reading to all data in the image. The first time
     * executing is to prime hotspot and allow it to optimize the code. Next
     * each of the tests will be executed 10 times and averaged.
     */
    public void testSpeed() {
        double avg = 0;
        double direct = 0;

        size = imageobject.getSize();
        System.out.println("loop size is           : " + size);

        // prime hotspot
        System.out.print("Priming hotspot        : ");
        testDirect();
        testFunction();
        testImageType();
        testImageCheck();
        testImageGeneric();
        System.out.println();

        System.out.print("Testing direct         : ");
        avg = 0;
        for (int i = 0; i < loop; i++) {
            avg += testDirect();
        }
        System.out.println("avg = " + ((double) avg / loop));
        direct = (avg / loop);

        System.out.print("Testing function       : ");
        avg = 0;
        for (int i = 0; i < loop; i++) {
            avg += testFunction();
        }
        System.out.println("avg = " + (avg / loop) + " slower = " + ((avg / loop) / direct));

        System.out.print("Testing image type     : ");
        avg = 0;
        for (int i = 0; i < loop; i++) {
            avg += testImageType();
        }
        System.out.println("avg = " + (avg / loop) + " slower = " + ((avg / loop) / direct));

        System.out.print("Testing image check    : ");
        avg = 0;
        for (int i = 0; i < loop; i++) {
            avg += testImageCheck();
        }
        System.out.println("avg = " + (avg / loop) + " slower = " + ((avg / loop) / direct));

        System.out.print("Testing image generic  : ");
        avg = 0;
        for (int i = 0; i < loop; i++) {
            avg += testImageGeneric();
        }
        System.out.println("avg = " + (avg / loop) + " slower = " + ((avg / loop) / direct));
    }

    /**
     * Access the array directly. This is as fast as it can get.
     *
     * @return time to execute.
     */
    public long testDirect() {
        int i;
        double j;
        long time = System.currentTimeMillis();

        for (j = 0, i = 0; i < size; i++, j++) {
            data[i] = (byte) j;
        }

        for (i = 0; i < size; i++) {
            j = data[i];
        }

        time = System.currentTimeMillis() - time;
        System.out.print(time + " ");
        return time;
    }

    /**
     * Use the generic imageobject, this should be slow.
     *
     * @return time to execute.
     */
    public long testImageType() {
        int i;
        double j;
        long time = System.currentTimeMillis();

        for (j = 0, i = 0; i < size; i++, j++) {
            imageobjecttype.set(i, j);
        }

        for (i = 0; i < size; i++) {
            j = imageobjecttype.getDouble(i);
        }

        time = System.currentTimeMillis() - time;
        System.out.print(time + " ");
        return time;
    }

    /**
     * Use the generic imageobject, this should be slow.
     *
     * @return time to execute.
     */
    public long testImageCheck() {
        int i;
        double j;
        long time = System.currentTimeMillis();

        for (j = 0, i = 0; i < size; i++, j++) {
            switch (imageobject.getType()) {
                case ImageObject.TYPE_BYTE:
                    ((ImageObjectByte) imageobject).set(i, j);
                    break;
                case ImageObject.TYPE_SHORT:
                    ((ImageObjectShort) imageobject).set(i, j);
                    break;
                case ImageObject.TYPE_USHORT:
                    ((ImageObjectUShort) imageobject).set(i, j);
                    break;
                case ImageObject.TYPE_INT:
                    ((ImageObjectInt) imageobject).set(i, j);
                    break;
                case ImageObject.TYPE_LONG:
                    ((ImageObjectLong) imageobject).set(i, j);
                    break;
                case ImageObject.TYPE_FLOAT:
                    ((ImageObjectFloat) imageobject).set(i, j);
                    break;
                case ImageObject.TYPE_DOUBLE:
                    ((ImageObjectDouble) imageobject).set(i, j);
                    break;
            }
        }

        for (i = 0; i < size; i++) {
            switch (imageobject.getType()) {
                case ImageObject.TYPE_BYTE:
                    j = ((ImageObjectByte) imageobject).getDouble(i);
                    break;
                case ImageObject.TYPE_SHORT:
                    j = ((ImageObjectShort) imageobject).getDouble(i);
                    break;
                case ImageObject.TYPE_USHORT:
                    j = ((ImageObjectUShort) imageobject).getDouble(i);
                    break;
                case ImageObject.TYPE_INT:
                    j = ((ImageObjectInt) imageobject).getDouble(i);
                    break;
                case ImageObject.TYPE_LONG:
                    j = ((ImageObjectLong) imageobject).getDouble(i);
                    break;
                case ImageObject.TYPE_FLOAT:
                    j = ((ImageObjectFloat) imageobject).getDouble(i);
                    break;
                case ImageObject.TYPE_DOUBLE:
                    j = ((ImageObjectDouble) imageobject).getDouble(i);
                    break;
            }
        }

        time = System.currentTimeMillis() - time;
        System.out.print(time + " ");
        return time;
    }

    /**
     * Use the generic imageobject, this should be slow.
     *
     * @return time to execute.
     */
    public long testImageGeneric() {
        int i;
        double j;
        long time = System.currentTimeMillis();

        for (j = 0, i = 0; i < size; i++, j++) {
            imageobject.set(i, j);
        }

        for (i = 0; i < size; i++) {
            j = imageobject.getDouble(i);
        }

        time = System.currentTimeMillis() - time;
        System.out.print(time + " ");
        return time;
    }

    /**
     * Access the array using functions. Still array is local to class.
     *
     * @return time to execute.
     */
    public long testFunction() {
        int i;
        double j;
        long time = System.currentTimeMillis();

        for (j = 0, i = 0; i < size; i++, j++) {
            setValue(i, j);
        }

        for (i = 0; i < size; i++) {
            j = getValue(i);
        }

        time = System.currentTimeMillis() - time;
        System.out.print(time + " ");
        return time;
    }

    /**
     * Returns the data in the array at index i.
     *
     * @param i index of the data to be returned.
     * @return the data.
     */
    private double getValue(int i) {
        return data[i];
    }

    /**
     * Sets the data in the array
     *
     * @param i the index to be set.
     * @param v the value that the data should be set to.
     */
    private void setValue(int i, double v) {
        data[i] = (byte) v;
    }
}
