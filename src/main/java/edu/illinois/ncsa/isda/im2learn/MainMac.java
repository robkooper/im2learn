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
package edu.illinois.ncsa.isda.imagetools;


import com.apple.eawt.Application;
import com.apple.eawt.ApplicationEvent;
import com.apple.eawt.ApplicationListener;

import edu.illinois.ncsa.isda.imagetools.core.Im2LearnUtilities;
import edu.illinois.ncsa.isda.imagetools.core.display.About;
import edu.illinois.ncsa.isda.imagetools.core.io.LoadSaveImagePanel;

/**
 * Start the stand alone Im2Learn application. This class will be able to load the
 * Im2Learn application and search the class path for any extentions. Extentions are
 * classes that implement the Im2LearnMenu interface.
 *
 * @author Rob Kooper
 * @version 1.0
 */
public class MainMac extends Main implements ApplicationListener {
    /**
     * Start Im2Learn with the image pointed to by the filename.
     *
     * @param filename filename of an image to be loaded at startup.
     */
    public MainMac(String filename) {
        super(filename);
		
        // Special menus for the MAC
        if (Im2LearnUtilities.isMACOS()) {
            Application app = Application.getApplication();
            app.addApplicationListener(this);
        }
    }

    // ----------------------------------------------------------------------
    // APPLE Special menu entries
    // ----------------------------------------------------------------------
    public void handleAbout(ApplicationEvent arg) {
        About.showAbout();
        arg.setHandled(true);
    }

    public void handleOpenApplication(ApplicationEvent arg) {
        //logger.debug("handleOpenApplication: " + arg.toString());
    }

    public void handleOpenFile(ApplicationEvent arg) {
        LoadSaveImagePanel.load(arg.getFilename(), getImagePanel());
        arg.setHandled(true);
    }

    public void handlePreferences(ApplicationEvent arg) {
        //logger.debug("handlePreferences: " + arg.toString());
    }

    public void handlePrintFile(ApplicationEvent arg) {
        //logger.debug("handlePrintFile: " + arg.toString());
    }

    public void handleQuit(ApplicationEvent arg) {
        setVisible(false);
        arg.setHandled(true);
        System.exit(0);
    }

    public void handleReOpenApplication(ApplicationEvent arg) {
        //logger.debug("handleReOpenApplication: " + arg.toString());
    }
}
