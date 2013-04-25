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
package edu.illinois.ncsa.isda.im2learn.ext.conversion;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.im2learn.core.display.Im2LearnMenu;
import edu.illinois.ncsa.isda.im2learn.core.display.ImagePanel;
import edu.illinois.ncsa.isda.im2learn.core.display.ImageUpdateEvent;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.net.URL;

/**
 * Create the menu entries for the PCA code. This will create a menu entry for
 * the PCA code. The menu entry will have a check in front of it if the image is
 * already a PCA image otherwise it will not have a check in front of it. If the
 * image is already a PCA image selecting the menu entry will undo the PCA and
 * show the full original image, otherwise a PCA will be done on the image and
 * the result will be show in the imagepanel.
 *
 * @author Rob Kooper
 * @version 2.0
 */
public class PCADialog implements Im2LearnMenu {
    private ImagePanel imagepanel = null;
    private JCheckBoxMenuItem menupca;
    private static Log logger = LogFactory.getLog(PCADialog.class);

    /**
     * Default constructor creating the menu entries.
     */
    public PCADialog() {
        menupca = new JCheckBoxMenuItem(new AbstractAction("PCA") {
            public void actionPerformed(ActionEvent e) {
                PCA pca = new PCA();
                if (menupca.isSelected()) {
                    try {
                        pca.computeLoadings(imagepanel.getImageObject());
                        ImageObject newimg = pca.applyPCATransform(imagepanel.getImageObject());
                        imagepanel.setImageObject(newimg);
                    } catch (Exception exc) {
                        logger.error("error applying PCA", exc);
                    }
                } else {
                    try {
                        ImageObject newimg = pca.undoPCATransform(imagepanel.getImageObject());
                        imagepanel.setImageObject(newimg);
                    } catch (Exception exc) {
                        logger.error("error undoing PCA", exc);
                    }
                }
            }
        });
    }

    // ------------------------------------------------------------------------
    // Im2LearnMenu implementation
    // ------------------------------------------------------------------------
    public JMenuItem[] getPanelMenuItems() {
        return null;
    }

    public void setImagePanel(ImagePanel imagepanel) {
        this.imagepanel = imagepanel;

        if (imagepanel.getImageObject().getProperty(PCA.EIGENVECTOR) != null) {
            menupca.setSelected(true);
        } else {
            menupca.setSelected(false);
        }
    }

    public JMenuItem[] getMainMenuItems() {
        JMenu tools = new JMenu("Tools");
        tools.add(menupca);
        return new JMenuItem[]{tools};
    }

    public void imageUpdated(ImageUpdateEvent event) {
        if (event.getId() == ImageUpdateEvent.NEW_IMAGE) {
            if ((imagepanel.getImageObject() != null) &&
                (imagepanel.getImageObject().getProperty(PCA.EIGENVECTOR) != null)) {
                menupca.setSelected(true);
            } else {
                menupca.setSelected(false);
            }
        }
    }
    
    public URL getHelp(String menu) {
    	if (menu.equals("PCA")) {
    		return getClass().getResource("help/PCA.html");
    	}
    	return null;
    }
}
