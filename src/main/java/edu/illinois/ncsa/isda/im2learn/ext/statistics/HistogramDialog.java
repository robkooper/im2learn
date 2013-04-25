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
package edu.illinois.ncsa.isda.im2learn.ext.statistics;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageException;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.im2learn.core.display.Im2LearnMenu;
import edu.illinois.ncsa.isda.im2learn.core.display.ImagePanel;
import edu.illinois.ncsa.isda.im2learn.core.display.ImageUpdateEvent;
import edu.illinois.ncsa.isda.im2learn.ext.misc.PlotComponent;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.net.URL;

// TODO interface for class

/**
 *
 */
public class HistogramDialog implements Im2LearnMenu {
    private ImagePanel imagepanel;

    private static Log logger = LogFactory.getLog(HistogramDialog.class);

    // ------------------------------------------------------------------------
    // Im2LearnMenu implementation
    // ------------------------------------------------------------------------
    public void setImagePanel(ImagePanel imagepanel) {
        this.imagepanel = imagepanel;
    }

    public JMenuItem[] getPanelMenuItems() {
        return null;
    }

    public JMenuItem[] getMainMenuItems() {
        JMenu tools = new JMenu("Tools");

        JMenuItem compare = new JMenuItem(new AbstractAction("Image Histogram") {
            public void actionPerformed(ActionEvent e) {
                ImageObject imgobj = imagepanel.getImageObject();
                Histogram hist = new Histogram();
                if (imgobj.getType() != ImageObject.TYPE_BYTE) {
                    hist.SetHistParam256Bins(imgobj);
                }
                try {
                    PlotComponent pc = new PlotComponent();
                    for (int i = 0; i < imgobj.getNumBands(); i++) {
                        int id = pc.addSeries("Band " + i);
                        hist.Hist(imgobj, i);
                        int[] data = hist.GetHistData();
                        double x = hist.GetMinDataVal();
                        for(int j=0; j<data.length; j++, x += hist.GetWideBins()) {
                            pc.setValue(id, x, data[j]);
                        }
                    }

                    JFrame frame = new JFrame("Histogram Plot");
                    frame.getContentPane().add(pc);
                    frame.pack();
                    frame.setVisible(true);
                } catch (ImageException exc) {
                    logger.error("Error creating histogram", exc);
                }
            }
        });
        tools.add(compare);

        return new JMenuItem[]{tools};
    }

    public void imageUpdated(ImageUpdateEvent event) {
    }

    public URL getHelp(String menu) {
        return getClass().getResource("help/Histogram.html");
    }
}
