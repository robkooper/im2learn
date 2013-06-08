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

import java.awt.Frame;

import javax.swing.SwingUtilities;

import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.im2learn.core.display.ImagePanel;
import edu.illinois.ncsa.isda.im2learn.core.display.ProgressBlocker;
import edu.illinois.ncsa.isda.im2learn.core.display.ProgressBlockerRun;


/**
 * Load a file into an imagepanel
 */
public class LoadSaveImagePanel implements ProgressBlockerRun {
    private ImagePanel imgpnl;
    private String filename;
    private SubFile subfile;
    private String filter;
    private boolean load;

    static public void load(String filename, ImagePanel imgpnl) {
        new LoadSaveImagePanel().doit(filename, imgpnl, null, null, true);
    }

    static public void load(String filename, ImagePanel imgpnl, String filter) {
        new LoadSaveImagePanel().doit(filename, imgpnl, filter, null, true);
    }

    static public void load(String filename, ImagePanel imgpnl, String filter, SubFile subfile) {
        new LoadSaveImagePanel().doit(filename, imgpnl, filter, subfile, true);
    }

    static public void load(String[] filenames, ImagePanel imgpnl) {
    	for(String filename : filenames) {
    		new LoadSaveImagePanel().doit(filename, imgpnl, null, null, true);
    	}
    }

    static public void load(String[] filenames, ImagePanel imgpnl, String filter) {
    	for(String filename : filenames) {
            new LoadSaveImagePanel().doit(filename, imgpnl, filter, null, true);
    	}
    }

    static public void save(String filename, ImagePanel imgpnl) {
        new LoadSaveImagePanel().doit(filename, imgpnl, null, null, false);
    }

    static public void save(String filename, ImagePanel imgpnl, String filter) {
        new LoadSaveImagePanel().doit(filename, imgpnl, filter, null, false);
    }

    protected LoadSaveImagePanel() {
    }

    protected void doit(String filename, ImagePanel imgpnl, String filter, SubFile subfile, boolean load) {
        this.imgpnl = imgpnl;
        this.filename = filename;
        this.filter = filter;
        this.subfile = subfile;
        this.load = load;

        Frame frame = (Frame) SwingUtilities.getWindowAncestor(imgpnl);
        ProgressBlocker pb2 = new ProgressBlocker(frame);
        if (load) {
            pb2.setMessage("Loading : " + filename);
        } else {
            pb2.setMessage("Saving : " + filename);
        }
        pb2.pack();
        ImageLoader.addProgressListener(pb2);
        pb2.showDialog(this);
        ImageLoader.removeProgressListener(pb2);
    }

    public void run(ProgressBlocker blocker) throws Exception {
        if (load) {
            imgpnl.setImageObject(null);
            ImageObject imgobj;
        	for(int i=0; i<ImageLoader.getImageCount(filename, filter); i++) {
	            if (subfile == null) {
	                imgobj = ImageLoader.readImage(filename, i, filter, null, 1);
	            } else if (subfile.isLoadHeader()) {
	                imgobj = ImageLoader.readImageHeader(filename, i, filter);
	            } else {
	                imgobj = ImageLoader.readImage(filename, i, filter, subfile.getSubArea(), subfile.getSampling());
	            }
	            imgpnl.setImageObject(imgobj);
        	}
        } else {
            ImageObject imgobj = imgpnl.getImageObject();
            imgobj.setProperty(ImageObject.DEFAULT_GAMMA, new double[]{imgpnl.getGamma()});
            imgobj.setProperty(ImageObject.DEFAULT_RGB,
                               new int[]{imgpnl.getRedBand(),
                                         imgpnl.getGreenBand(),
                                         imgpnl.getBlueBand()});
            imgobj.setProperty(ImageObject.DEFAULT_GRAY,
                               new int[]{imgpnl.getGrayBand()});
            imgobj.setProperty(ImageObject.FILENAME, filename);
            ImageLoader.writeImage(filename, filter, imgobj);
        }
    }
}
