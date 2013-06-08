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
package edu.illinois.ncsa.isda.im2learn.main;


import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.im2learn.core.display.Im2LearnMainFrame;
import edu.illinois.ncsa.isda.im2learn.ext.conversion.ChangeTypeDialog;
import edu.illinois.ncsa.isda.im2learn.ext.conversion.Colorspace;
import edu.illinois.ncsa.isda.im2learn.ext.conversion.PCADialog;
import edu.illinois.ncsa.isda.im2learn.ext.edge.EdgeMenu;
import edu.illinois.ncsa.isda.im2learn.ext.info.InfoDialog;
import edu.illinois.ncsa.isda.im2learn.ext.panel.CropDialog;
import edu.illinois.ncsa.isda.im2learn.ext.panel.SelectBandDialog;
import edu.illinois.ncsa.isda.im2learn.ext.panel.SelectionDialog;
import edu.illinois.ncsa.isda.im2learn.ext.panel.ZoomDialog;
import edu.illinois.ncsa.isda.im2learn.ext.segment.HSVThresholdDialog;
import edu.illinois.ncsa.isda.im2learn.ext.segment.ThresholdDialog;
import edu.illinois.ncsa.isda.im2learn.ext.test.Detect;
import edu.illinois.ncsa.isda.im2learn.ext.vis.ImageExtractDialog;
import edu.illinois.ncsa.isda.im2learn.ext.vis.PseudoImageDialog;

import javax.swing.*;

/**
 * N**
 * <b>Description:</b> This class provides a graphics user
 * interface (GUI) to the class Colorspace, edge , PCA for converting extracting this features from the image in the main panel.
 * 
 * <p/>
 */
public class FeatureSpaceDialog extends Im2LearnMainFrame {
    /**
     * Default constructor shows the Im2Learn Image
     */
    public FeatureSpaceDialog() {
        this(null);
    }

    /**
     * Create the MainFrame with all the menus
     */
    public FeatureSpaceDialog(String filename) {
        super(filename);
    }

    /**
     * People can crop, zoom and select.
     */
    public void addMenus() {
  
        addMenu(new CropDialog());

    	  
    	// people can info, select, crop and zoom.
        addMenu(new InfoDialog());
        addMenu(new ZoomDialog());
        addMenu(new SelectBandDialog());
        // change image type
        addMenu(new ChangeTypeDialog());
        //Image Extract
        addMenu(new ImageExtractDialog());
        addMenu(new PseudoImageDialog());
        //add proper classes to the menu class
        addMenu(new PCADialog());
        addMenu(new EdgeMenu());
        //add colorspace
        addMenu(new Colorspace());
      //Add threshhold class
        addMenu(new ThresholdDialog());
        addMenu(new HSVThresholdDialog());


    }

    /**
     * Start of Im2Learn. Make sure to start the logger.
     *
     * @param args for Im2Learn
     */
    static public void main(String[] args) {
        // change L&F to system default
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            LogFactory.getLog("").error("Unable to load native look and feel");
        }

        // start Im2Learn
        if (args.length > 0) {
            new FeatureSpaceDialog(args[0]);
        } else {
            new FeatureSpaceDialog(null);
        }
    }
}
