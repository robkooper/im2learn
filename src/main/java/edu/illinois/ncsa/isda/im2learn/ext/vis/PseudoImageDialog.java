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
import java.awt.event.ActionEvent;
import java.awt.*;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.im2learn.core.display.*;

/**
 *
 *	<p>
	<B>The class PseudoImage provides a tool for pseudo-coloring each image band with the range of colors from 
blue (low values), green (middle range values) and red (high values). </B>
	 </p>
	 <p>
	 <b>Description:</b>
	
	 This tool is for enhancing image display and visualization. The dialog below shows the color-to-value 
assignment by presenting the color bar on the right side of the frame. A band to pseudo color is chosen using either 
the slider below the image panel or using the edit box adjacent to the right of the slider bar.
	 </p>
	 <img src="help/pseudoImageDialog.jpg">
	 <p>
	 If the check box "Automatically set min/max value?" is checked then the display code computes maximum of each band
and scales each band accordingly. If the check box is not checked then a user can set the min and max values in the edit
boxes in the right lower corner. 
	 </p>
The use of the min/max values is illustrated with the same image as above by setting the minimum value to 100. 
Any value below 100 is shown as black and the values between 100 and 255 can be discriminated better in this visualization.
	 <BR>
	 <img src="help/pseudoImageDialog1.jpg">
<BR>
	
	* @author Rob Kooper, Peter Bajcsy (documentation)
	*
 */
public class PseudoImageDialog extends Im2LearnFrame implements Im2LearnMenu {
    private ImagePanel imagepanel;
    private PseudoImageUI ui;

    static private Log logger = LogFactory.getLog(PseudoImageDialog.class);

    public PseudoImageDialog() {
        super("Pseudo Color");
        // create the UI
        ui = new PseudoImageUI();
        getContentPane().add(ui);
        pack();
    }

    public void showing() {
        //sanity check
        if( imagepanel == null || imagepanel.getImageObject() == null){
          logger.error("ERROR: missing image object in showing method");
          return;
        }
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
        JMenuItem menu = new JMenuItem(new AbstractAction("Pseudo Color") {
            public void actionPerformed(ActionEvent e) {
                if (!isVisible()) {
                    Window win = SwingUtilities.getWindowAncestor(imagepanel);
                    setLocationRelativeTo(win);
                    setVisible(true);
                }
                toFront();
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
        return getClass().getResource("help/PseudoImageDialog.html");
    }
    
}
