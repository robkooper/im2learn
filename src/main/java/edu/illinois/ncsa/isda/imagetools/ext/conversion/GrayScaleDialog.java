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
package edu.illinois.ncsa.isda.imagetools.ext.conversion;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.imagetools.core.display.Im2LearnMenu;
import edu.illinois.ncsa.isda.imagetools.core.display.ImagePanel;
import edu.illinois.ncsa.isda.imagetools.core.display.ImageUpdateEvent;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.net.URL;

/**
 * Create the menu entry for the ImagePanel. This class will take the current
 * image and conver the image to a grayscale image. It will use the current used
 * red, green and blue bands of the ImagePanel.
 *
 * @author Rob Kooper
 * @version 2.0
 */
public class GrayScaleDialog implements Im2LearnMenu {
    private ImagePanel imagepanel = null;
    private JMenuItem convert;
    private JMenuItem grayscale;
    private static Log logger = LogFactory.getLog(GrayScaleDialog.class);

    /**
     * Default constructor, will create menu entry for panel.
     */
    public GrayScaleDialog() {
        grayscale = new JCheckBoxMenuItem(new AbstractAction("Show GrayBand") {
            public void actionPerformed(ActionEvent e) {
                imagepanel.setGrayScale(grayscale.isSelected());
            }
        });
        convert = new JMenuItem(new AbstractAction("Convert") {
            public void actionPerformed(ActionEvent e) {
                int res = JOptionPane.showConfirmDialog(imagepanel, "Convert image, this can not be undone?",
                                                        "Convert Image GrayScale", JOptionPane.YES_NO_OPTION);
                if (res == JOptionPane.YES_OPTION) {
                    GrayScale grayscale = new GrayScale();
                    try {
                        ImageObject newimg = grayscale.convert(imagepanel.getImageObject(),
                                                               imagepanel.getRedBand(),
                                                               imagepanel.getGrayBand(),
                                                               imagepanel.getBlueBand());
                        imagepanel.setImageObject(newimg);
                    } catch (Exception exc) {
                        logger.error(exc);
                    }
                }
            }
        });
    }

    // ------------------------------------------------------------------------
    // Im2LearnMenu implementation
    // ------------------------------------------------------------------------
    public void setImagePanel(ImagePanel imagepanel) {
        this.imagepanel = imagepanel;
        imageUpdated(new ImageUpdateEvent(this, ImageUpdateEvent.NEW_IMAGE, null));
    }

    public JMenuItem[] getPanelMenuItems() {
        JMenu menu = new JMenu("GrayScale");
        menu.add(grayscale);
        menu.add(convert);
        return new JMenuItem[]{menu};
    }

    public JMenuItem[] getMainMenuItems() {
        return null;
    }

    public void imageUpdated(ImageUpdateEvent event) {
        switch (event.getId()) {
            case ImageUpdateEvent.NEW_IMAGE:
                ImageObject imgobj = imagepanel.getImageObject();
                if ((imgobj == null) || (imgobj.getNumBands() == 1)) {
                    grayscale.setEnabled(false);
                    convert.setEnabled(false);
                } else {
                    grayscale.setEnabled(true);
                    convert.setEnabled(true);
                }
                // fall through

            case ImageUpdateEvent.CHANGE_GRAYSCALE:
                grayscale.setSelected(imagepanel.isGrayScale());
        }
    }

    public URL getHelp(String topic) {
        // TODO Auto-generated method stub
        return null;
    }
}
