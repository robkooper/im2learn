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
package edu.illinois.ncsa.isda.imagetools.ext.panel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageException;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.imagetools.core.datatype.SubArea;
import edu.illinois.ncsa.isda.imagetools.core.display.Im2LearnMenu;
import edu.illinois.ncsa.isda.imagetools.core.display.ImagePanel;
import edu.illinois.ncsa.isda.imagetools.core.display.ImageUpdateEvent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.URL;

/**
 * Crop the image based on the selection. After making a selection the user can
 * crop the image. This will result in a new image that is the size of the
 * selection area. To view the selected area instead, use show selection from
 * the selection submenu.
 *
 * @author Rob Kooper and Sang-Chul Lee
 * @version 1.2
 * added constructor CropDialog(boolean confirm) enable/disable confirmation dialog
 * 
 */
public class CropDialog implements Im2LearnMenu {
    private ImagePanel imagepanel = null;
    private JMenuItem crop;
    private static Log logger = LogFactory.getLog(CropDialog.class);
    private boolean confirm = true;	
    
    public CropDialog(boolean confirm) {
    	this.confirm = confirm;
    	runCropDialog();
    }
    
    public CropDialog() {
    	confirm = true;
    	runCropDialog();
    }
    
    private void runCropDialog() {
        crop = new JMenuItem(new AbstractAction("Crop") {
            public void actionPerformed(ActionEvent e) {
                Rectangle crop = imagepanel.getSelection();
                if (crop == null) {
                    crop = imagepanel.getCrop();
                }

                if (crop != null) {
                	if(confirm) {
	                    int res = JOptionPane.showConfirmDialog(imagepanel, "Crop image, this can not be undone?",
	                                                            "Crop Image", JOptionPane.YES_NO_OPTION);
	                    if (res == JOptionPane.YES_OPTION) {
	                        ImageObject imgobj = imagepanel.getImageObject();
	
	                        try {
	                            imagepanel.setImageObject(imgobj.crop(new SubArea(crop)));
	                        } catch (ImageException exc) {
	                            logger.error("Could not crop/set image.", exc);
	                        }
	                    }
                	}
                	else {
                		ImageObject imgobj = imagepanel.getImageObject();
            	   	
                        try {
                            imagepanel.setImageObject(imgobj.crop(new SubArea(crop)));
                        } catch (ImageException exc) {
                            logger.error("Could not crop/set image.", exc);
                        }
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
        imageUpdated(null);
    }

    public JMenuItem[] getPanelMenuItems() {
        return new JMenuItem[]{crop};
    }

    public JMenuItem[] getMainMenuItems() {
        return null;
    }

    public void imageUpdated(ImageUpdateEvent event) {
        crop.setEnabled(imagepanel.isSelectionAllowed());
    }

    public URL getHelp(String topic) {
        // TODO Auto-generated method stub
        return null;
    }
}
