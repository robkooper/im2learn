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

import java.awt.Frame;
import java.lang.reflect.Constructor;
import java.util.Enumeration;
import java.util.ServiceLoader;

import javax.swing.UIManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.imagetools.core.Im2LearnUtilities;
import edu.illinois.ncsa.isda.imagetools.core.ResourceLocator;
import edu.illinois.ncsa.isda.imagetools.core.display.Im2LearnMainFrame;
import edu.illinois.ncsa.isda.imagetools.core.display.Im2LearnMenu;

/**
 * Start the stand alone Im2Learn application. This class will be able to load
 * the Im2Learn application and search the class path for any extentions.
 * Extentions are classes that implement the Im2LearnMenu interface.
 * 
 * @author Rob Kooper
 * @version 1.0
 */
public class Main extends Im2LearnMainFrame {
    /**
     * Serial version ID
     */
    private static final long serialVersionUID = 1L;

    static protected Log      logger           = LogFactory.getLog(Main.class);

    /**
     * Start the application. The argument is assumed to be an image that needs
     * to be loaded.
     * 
     * @param args
     *            name of a image file to load.
     */
    static public void main(String[] args) {

        // change L&F to system default
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            logger.error("Unable to load native look and feel");
        }

        // set up search path
        if (!Im2LearnUtilities.isWebService()) {
            ResourceLocator rl = ResourceLocator.getInstance();

            // add standard lib directory to search path
            rl.addPath("lib", false);

            if ((System.getProperty("im2learn.scan") != null) || (System.getProperty("im2learn.ext") != null)) {
                // add location where resources can be found
                rl.addPath("ext", true);
                rl.addPath(System.getProperty("im2learn.ext"), true);
            } else {
                // add location where resources can be found
                rl.addPath("ext", false);
            }
        }

        // any filename passed on the command-line
        String filename = null;
        if (args.length > 0) {
            filename = args[0];
        }

        // start Im2Learn
        if (Im2LearnUtilities.isMACOS()) {
            new MainMac(filename);
        } else {
            new Main(filename);
        }
    }

    public Main() {
        this(null);
    }

    /**
     * Start Im2Learn with the image pointed to by the filename.
     * 
     * @param filename
     *            filename of an image to be loaded at startup.
     */
    public Main(String filename) {
        super(filename);
    }

    /**
     * Add extentions to Im2Learn. This will either scan all class files or use
     * the SPI method by looking in the services directory of the jar files.
     */
    public void addMenus() {
        if (Im2LearnUtilities.isWebService()) {
            addMenusServices(null);
        } else {
            ResourceLocator rl = ResourceLocator.getInstance();

            // scan for the resources
            addMenusScan(rl);
        }
    }

    /**
     * Use the java SPI to add extra functionality to Im2Learn. Unlike the scan
     * method this will look at the services directory. This can be used both
     * with web services and jar files and is faster. However all extentions
     * need to be listed in the services file.
     */
    private void addMenusServices(ClassLoader cl) {
        ServiceLoader<Im2LearnMenu> loader = ServiceLoader.load(Im2LearnMenu.class);
        for (Im2LearnMenu menu : loader) {
            try {
                addMenu(menu.getClass());
            } catch (Throwable thr) {
                logger.warn("Error registering loader.", thr);
            }
        }
    }

    /**
     * Scan through all the jar files and classes and find all those that
     * implement the Im2Learn interface.
     */
    private void addMenusScan(ResourceLocator rl) {
        Enumeration e = rl.searchResources(Im2LearnMenu.class);
        while (e.hasMoreElements()) {
            Class clazz = (Class) e.nextElement();
            long l = System.nanoTime();
            addMenu(clazz);
            logger.info("Took " + (System.nanoTime() - l) + " Nanoseconds to add " + clazz);
        }
    }

    /**
     * Helper function to instantiate the class and add it to the menu.
     * 
     * @param clazz
     *            the class to be instantiated.
     */
    private void addMenu(Class clazz) {
        try {
            Class[] param = new Class[] { Im2LearnMainFrame.class };
            Constructor cons = clazz.getConstructor(param);
            addMenu((Im2LearnMenu) cons.newInstance(new Object[] { this }));
            return;
        } catch (NoSuchMethodException exc) {
        } catch (Throwable thr) {
            logger.debug("Error instantiating " + clazz.getName(), thr);
        }

        try {
            Class[] param = new Class[] { Frame.class };
            Constructor cons = clazz.getConstructor(param);
            addMenu((Im2LearnMenu) cons.newInstance(new Object[] { this }));
            return;
        } catch (NoSuchMethodException exc) {
        } catch (Throwable thr) {
            logger.debug("Error instantiating " + clazz.getName(), thr);
        }

        try {
            addMenu((Im2LearnMenu) clazz.newInstance());
            return;
        } catch (Throwable thr) {
            logger.debug("Error instantiating " + clazz.getName(), thr);
        }

        logger.info("No constructor found for " + clazz.getName());
    }
}
