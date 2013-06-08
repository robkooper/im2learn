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
package edu.illinois.ncsa.isda.im2learn.ext.geo;

import java.io.*;
import java.net.URL;


import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

import edu.illinois.ncsa.isda.im2learn.core.datatype.*;
import edu.illinois.ncsa.isda.im2learn.core.display.*;
import edu.illinois.ncsa.isda.im2learn.core.io.*;

import java.awt.*;
import java.awt.event.*;

/**
 * @todo : add in sampling rates.  MosaicGeoTiles must be updated for this.
 * @author clutter
 */
public class MosaicDialog extends Im2LearnFrame implements Im2LearnMenu {

  private ImagePanel imagepanel;
  private MosaicUI ui;

  public MosaicDialog() {
    super("Mosaic Geo Tiles");

    ui = new MosaicUI();
    Container c = getContentPane();
    c.setLayout(new BorderLayout());
    c.add(ui, BorderLayout.CENTER);
  }


  // ------------------------------------------------------------------------
  // Im2LearnMenu implementation
  // ------------------------------------------------------------------------
  public void setImagePanel(ImagePanel imagepanel) {
    this.imagepanel = imagepanel;
    ui.setImagePanel(imagepanel);
  }

  public JMenuItem[] getPanelMenuItems() {
    return null;
  }

  public JMenuItem[] getMainMenuItems() {
    JMenu geo = new JMenu("Geo");

    JMenuItem mosaicItem = new JMenuItem("Mosaic Geo Tiles");
    mosaicItem.addActionListener(new AbstractAction() {
      public void actionPerformed(ActionEvent ae) {
        MosaicDialog.this.pack();
        MosaicDialog.this.show();
      }
    });

    geo.add(mosaicItem);

    return new JMenuItem[] {geo};
  }

  public void imageUpdated(ImageUpdateEvent event) {
  }


  public URL getHelp(String menu) {
    URL url = null;
    
    if (url == null) {
        String file = menu.toLowerCase().replaceAll("[\\s\\?\\*]", "") + ".html"; 
        url = this.getClass().getResource("help/" + file);
    }
    
    return url;
  }
}
