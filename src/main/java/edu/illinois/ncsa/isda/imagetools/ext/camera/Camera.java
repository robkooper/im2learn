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
package edu.illinois.ncsa.isda.imagetools.ext.camera;


import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.imagetools.core.display.ProgressListener;

/**
 * Abstract class which each camera will implement. This class is a abstract
 * representation of a camera. Each specific camera model will implement the
 * functions to grab a frame, set options and get the name of the camera.
 */
public abstract class Camera {
    private Vector listeners;

    /**
     * Default constructor.
     */
    public Camera() {
        listeners = new Vector();
    }

    /**
     * Returns a string that is the camera name. This name can be used to
     * identify the camera, not necessarily the the exact camera, for instance
     * when two cameras are connected to the system.
     *
     * @return name of the camera
     */
    abstract public String getName();

    /**
     * Close the camera.
     *
     * @throws IOException if an error occured closing the camera.
     */
    abstract public void close() throws IOException;

    /**
     * Grab a single frame from the camera and return the image.
     *
     * @return a single frame grabbed from the camera.
     * @throws IOException if an error occured grabbing the frame.
     */
    abstract public ImageObject getFrame() throws IOException;

    /**
     * Show a dialog allowing to change the options of the camera.
     */
    abstract public void showOptionsDialog();

    public void addProgressListener(ProgressListener l) {
        if ((l != null) && !listeners.contains(l)) {
            listeners.add(l);
        }
    }

    public void removeProgressListener(ProgressListener l) {
        if (l != null) {
            listeners.remove(l);
        }
    }

    public void fireProgress(int count, int max) {
        for (Iterator iter = listeners.iterator(); iter.hasNext();) {
            ProgressListener l = (ProgressListener) iter.next();
            l.progress(count, max);
        }
    }

    /**
     * Returns the options set on the camera. This property list will be camera
     * depended.
     *
     * @return list of properties set on the camera.
     */
    public Properties getOptions() throws IOException {
        return null;
    }

    /**
     * Returns a list options set on the camera. This property list will be
     * camera depended.
     *
     * @param options the camera options to set.
     */
    public void setOptions(Properties options) throws IOException {
    }
}
