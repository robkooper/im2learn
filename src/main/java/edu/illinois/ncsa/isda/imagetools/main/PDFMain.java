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
package edu.illinois.ncsa.isda.imagetools.main;

import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.imagetools.core.display.Im2LearnMainFrame;
import edu.illinois.ncsa.isda.imagetools.core.io.pdf.PDFLoaderJPedal;
import edu.illinois.ncsa.isda.imagetools.ext.info.InfoDialog;
import edu.illinois.ncsa.isda.imagetools.ext.panel.CropDialog;
import edu.illinois.ncsa.isda.imagetools.ext.panel.SelectionDialog;
import edu.illinois.ncsa.isda.imagetools.ext.panel.ZoomDialog;
import edu.illinois.ncsa.isda.imagetools.ext.pdf.*;

import javax.swing.*;

/**
 * Note this is enabled in this version, just the basics that will be always
 * there.
 */
public class PDFMain extends Im2LearnMainFrame {
    /**
     * Default constructor shows the Im2Learn Image
     */
    public PDFMain() {
        this(null);
    }

    /**
     * Create the MainFrame with all the menus
     */
    public PDFMain(String filename) {
        super(filename);
    }

    /**
     * People can crop, zoom and select.
     */
    public void addMenus() {
        // people can info, select, crop and zoom.
        addMenu(new InfoDialog());
        addMenu(new ZoomDialog());
        addMenu(new SelectionDialog());
        addMenu(new CropDialog());
        //custom
        addMenu(new PDFLoaderJPedal());
        addMenu(new PDFAnnotationImageCleanupDialog());
        addMenu(new PDFAnnotationDialog());
        addMenu(new PDFAnnotationTextCleanupDialog());
        addMenu(new PDFAnnotationGroupDialog());
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
            new PDFMain(args[0]);
        } else {
            new PDFMain(null);
        }
    }
}
