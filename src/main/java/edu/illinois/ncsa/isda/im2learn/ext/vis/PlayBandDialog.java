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
package edu.illinois.ncsa.isda.im2learn.ext.vis;


import javax.swing.*;

import edu.illinois.ncsa.isda.im2learn.core.display.Im2LearnFrame;
import edu.illinois.ncsa.isda.im2learn.core.display.Im2LearnMenu;
import edu.illinois.ncsa.isda.im2learn.core.display.ImagePanel;
import edu.illinois.ncsa.isda.im2learn.core.display.ImageUpdateEvent;

import java.awt.event.ActionEvent;
import java.net.URL;

/**
 * 
 * 	<p>
	<B>The class PlayBands provides a tool for visualization of multi-band images by 
	sweeping through the image bands. </B>
	 </p>
	 <p>
	 <b>Description:</b>
	
 
	 <p>
	 This tool contains a slider bar to set the image band to be viewed. The band can be also set using the edit
box at the end of the slider bar (see the frame/dialog below).
	 </p>
	 <img src="playBandsDialog.jpg">
	 <p>
	 When working with high dimensional multi-spectral images, it might be convenient to sweep through the image
bands as if watching a movie (temporal sequence of image bands). The entry "Delay between frames" contains several 
choices for time delays. The check box "Loop?" denotes the option to loop continuosly till the button "Stop" is pressed.
Finally, the check box denoted as "Totals" refers to the intensity scaling option during visualization of bands with an absolute maximum intensity over all bands (if the box is checked) or with a maximum intensity per band (if the box is not checked).
	 </p>

* @author Rob Kooper, Peter Bajcsy (documentation)
* 
 * This class will display the bands in the image as a sequence of frames. The
 * user can decide what speed the video needs to play back. The user can stop
 * and pause the video, as well make it loop.
 */
public class PlayBandDialog extends Im2LearnFrame implements Im2LearnMenu {
    private ImagePanel imagepanel;
    private PlayBandUI ui;

    /**
     * Constructor, will create the interface.
     */
    public PlayBandDialog() {
        super("Play Image Bands");

        // create the UI
        ui = new PlayBandUI();
        getContentPane().add(ui);

        // default size is 640x480
        setSize(640, 480);
    }

    public void showing() {
        ui.setImageObject(imagepanel.getImageObject());
    }

    public void closing() {
        ui.setImageObject(null);
    }

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
        JMenu vis = new JMenu("Visualization");
        JMenuItem menu = new JMenuItem(new AbstractAction("Play Bands") {
            public void actionPerformed(ActionEvent e) {
                setLocationRelativeTo(getOwner());
                setVisible(true);
            }
        });
        vis.add(menu);
        return new JMenuItem[]{vis};
    }

    public void imageUpdated(ImageUpdateEvent event) {
        if (isVisible() && (event.getId() == ImageUpdateEvent.NEW_IMAGE)) {
            ui.setImageObject(imagepanel.getImageObject());
        }
    }

    public URL getHelp(String menu) {
        if (menu.equals("Play Bands")) {
            return getClass().getResource("help/PlayBandsDialog.html");
        }
        return null;
    }
}
