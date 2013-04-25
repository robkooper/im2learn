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
package edu.illinois.ncsa.isda.im2learn.ext.misc;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.im2learn.core.Im2LearnUtilities;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.im2learn.core.display.HelpViewer;
import edu.illinois.ncsa.isda.im2learn.core.display.Im2LearnFrame;
import edu.illinois.ncsa.isda.im2learn.core.display.Im2LearnMenu;
import edu.illinois.ncsa.isda.im2learn.core.display.ImagePanel;
import edu.illinois.ncsa.isda.im2learn.core.io.FileChooser;
import edu.illinois.ncsa.isda.im2learn.core.io.LoadSaveImagePanel;

/**
 * Shows the given image in a seperate frame. The image will be displayed with
 * autozoom so will always fit. Once the user closes the frame the ImageObject
 * that was displayed will be released. If the frame is opened for a second
 * time, there will be no imageobject visible, unless setImageObject() is first
 * called!
 * 
 * @author Rob Kooper
 * @version 1.0
 */
public class ImageFrame extends Im2LearnFrame {
    static private Log logger = LogFactory.getLog(ImageFrame.class);

    private ImagePanel imagepanel;

    /**
     * Create the window with a zero ImageObject. To display an imageobject call
     * setImageObject().
     * 
     * @param title
     *            of the previewframe.
     */
    public ImageFrame(String title) {
        super(title);
        getContentPane().setLayout(new BorderLayout());

        createUI();
        setJMenuBar(createMenuBar());
    }

    @Override
    public void closing() {
        imagepanel.setImageObject(null);
    }

    /**
     * Create the ncsa.im2learn.main layout. This will add the menubar to the
     * frame and add the imagepanel to a scrollpane which is added to the
     * ncsa.im2learn.main panel.
     */
    protected void createUI() {
        imagepanel = new ImagePanel(null);
        imagepanel.setAutozoom(true);

        int vsc = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED;
        int hsc = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED;
        if (Im2LearnUtilities.isMACOS()) {
            vsc = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS;
            hsc = JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS;
        }
        JScrollPane scrollpane = new JScrollPane(imagepanel, vsc, hsc);
        scrollpane.setBorder(null);
        scrollpane.setPreferredSize(new Dimension(320, 240));
        getContentPane().add(scrollpane, BorderLayout.CENTER);

        pack();
    }

    /**
     * Set the image to be displayed in the imageframe.
     * 
     * @param imgobj
     *            that is to be displayed.
     */
    public void setImageObject(ImageObject imgobj) {
        imagepanel.setImageObject(imgobj);
    }

    /**
     * Return the image displayed in the imageframe.
     * 
     * @return imgobj that is displayed.
     */
    public ImageObject getImageObject() {
        return imagepanel.getImageObject();
    }

    public ImagePanel getImagePanel() {
        return imagepanel;
    }

    /**
     * Add the menu to the frame. This will work the same as the
     * Im2LearnMainFrame, adding menus to the imagepanel as well as the frame.
     * 
     * @param menu
     *            the menu to be added.
     */
    public void addMenu(Im2LearnMenu menu) {
        try {
            // first add help
            HelpViewer.addHelp(menu);

            imagepanel.addMenu(menu);

            JMenuItem items[] = menu.getMainMenuItems();
            if (items != null) {
                for (int i = 0; i < items.length; i++) {
                    Im2LearnUtilities.addSubMenu(getJMenuBar(), items[i]);
                }
            }
        } catch (Throwable thr) {
            logger.warn("Uncaught exception.", thr);
        }

        //        if (menu instanceof Window) {
        //            childwindows.add((Window) menu);
        //        }

        validate();
    }

    protected JMenuBar createMenuBar() {
        JMenuBar menubar = new JMenuBar();

        JMenu menu = new JMenu("File");
        menu.setMnemonic('f');
        menubar.add(menu);

        createFileMenu(menu);

        // return the menubar
        return menubar;
    }

    protected void createFileMenu(JMenu menu) {
        AbstractAction aa = new AbstractAction("Save") {
            public void actionPerformed(ActionEvent e) {
                if (imagepanel.getImageObject() == null) {
                    return;
                }
                try {
                    FileChooser chooser = new FileChooser();
                    String filename = chooser.showSaveDialog();
                    String filter = chooser.getFilter();
                    if (filename != null) {
                        LoadSaveImagePanel.save(filename, imagepanel, filter);
                    }
                } catch (IOException exc) {
                    logger.error("Error saving file.", exc);
                }
            }
        };
        aa.putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_S));
        menu.add(aa);

        aa = new AbstractAction("Close") {
            public void actionPerformed(ActionEvent e) {
                ImageFrame.this.setVisible(false);
            }
        };
        aa.putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_C));
        menu.add(aa);
    }
}
