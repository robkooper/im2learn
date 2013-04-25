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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/** 
 * ProgressInputStream encapsulates a InputSteam and will send
 * updates to the ImageListener about how many bytes of a file
 * has been read. 
 */
public class ProgressInputStream extends FilterInputStream {
    private int processed = 0;
    private int total = 0;

    /**
     * Encapsulate the inputstream and get the total size of
     * the file that is about to be read.
     * 
     * @param in the inputstream to be encapsulated.
     */
    public ProgressInputStream(InputStream in) {
        super(in);
        try {
            total = in.available();
        } catch(IOException ioe) {
            total = 0;
        }
        processed = 0;
    }

    /**
     * Read a single byte from the file and send a message to
     * any listeners.
     * 
     * @return     the next byte of data, or <code>-1</code> if
     * the end of the stream is reached.
     */
    public int read() throws IOException {
        int c = in.read();
        if (c >= 0) {
            processed++;
        }
        ImageLoader.fireProgress(processed, total);
        return c;
    }

    /**
     * Fill the array with data from the file and send of a
     * message to any listeners.
     * 
     * @return the number of bytes read.
     */
    public int read(byte b[]) throws IOException {
        int nr = in.read(b);
        if (nr >= 0) {
            processed += nr;
        }
        ImageLoader.fireProgress(processed, total);
        return nr;
    }

    /**
     * 
     */
    public int read(byte b[], int off, int len) throws IOException {
        int nr = in.read(b, off, len);
        if (nr >= 0) {
            processed += nr;
        }
        ImageLoader.fireProgress(processed, total);
        return nr;
    }

    public long skip(long n) throws IOException {
        long nr = in.skip(n);
        if (nr >= 0) {
            processed += nr;
        }
        ImageLoader.fireProgress(processed, total);
        return nr;
    }

    public void close() throws IOException {
        in.close();
    }

    public synchronized void reset() throws IOException {
        in.reset();
        processed = total - in.available();
        ImageLoader.fireProgress(processed, total);
    }
}
