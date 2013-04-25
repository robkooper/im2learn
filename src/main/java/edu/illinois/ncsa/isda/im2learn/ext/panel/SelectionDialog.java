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
package edu.illinois.ncsa.isda.im2learn.ext.panel;


import javax.swing.*;

import edu.illinois.ncsa.isda.im2learn.core.display.Im2LearnMenu;
import edu.illinois.ncsa.isda.im2learn.core.display.ImagePanel;
import edu.illinois.ncsa.isda.im2learn.core.display.ImageUpdateEvent;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.URL;

public class SelectionDialog implements Im2LearnMenu {
    private ImagePanel imagepanel = null;
    private JCheckBoxMenuItem allowselection = null;
    private JMenuItem clearselection = null;
    private JMenuItem showselection = null;
    private JMenuItem fullimage = null;

    public SelectionDialog() {
        allowselection = new JCheckBoxMenuItem(new AbstractAction("Allow Selection?") {
            public void actionPerformed(ActionEvent e) {
                JCheckBoxMenuItem chk = (JCheckBoxMenuItem) e.getSource();
                if (chk.isSelected() != imagepanel.isSelectionAllowed()) {
                    imagepanel.setSelectionAllowed(chk.isSelected());
                }
            }
        });

        clearselection = new JMenuItem(new AbstractAction("Clear Selection") {
            public void actionPerformed(ActionEvent e) {
                imagepanel.setSelection(null);
            }
        });

        showselection = new JMenuItem(new AbstractAction("Show Selection") {
            public void actionPerformed(ActionEvent e) {
                Rectangle crop = imagepanel.getSelection();
                if (crop != null) {
                    imagepanel.setCrop(crop);
                    imagepanel.setSelection(null);
                }
            }
        });

        fullimage = new JMenuItem(new AbstractAction("Full Image") {
            public void actionPerformed(ActionEvent e) {
                imagepanel.setCrop(null);
                imagepanel.setSelection(null);
            }
        });

    }


    // ------------------------------------------------------------------------
    // Im2LearnMenu implementation
    // ------------------------------------------------------------------------
    public void setImagePanel(ImagePanel imagepanel) {
        this.imagepanel = imagepanel;
        imageUpdated(null);
    }

    public JMenuItem[] getPanelMenuItems() {
        JMenu selection = new JMenu("Selection");
        selection.add(allowselection);
        selection.add(clearselection);
        selection.add(showselection);
        selection.add(fullimage);

        return new JMenuItem[]{selection};
    }

    public JMenuItem[] getMainMenuItems() {
        return null;
    }

    public void imageUpdated(ImageUpdateEvent event) {
        allowselection.setSelected(imagepanel.isSelectionAllowed());
        if (imagepanel.getCrop() != null) {
            fullimage.setEnabled(true);
        } else {
            fullimage.setEnabled(false);
        }
        if (imagepanel.getSelection() != null) {
            showselection.setEnabled(true);
            clearselection.setEnabled(true);
        } else {
            showselection.setEnabled(false);
            clearselection.setEnabled(false);
        }
    }

    public URL getHelp(String menu) {
    	if (menu.equals("Selection")) {
    		return getClass().getResource("help/selection.html");
    	}
    	return null;
    }
}
