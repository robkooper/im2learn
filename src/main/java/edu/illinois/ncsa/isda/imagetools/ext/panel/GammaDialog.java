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
import edu.illinois.ncsa.isda.imagetools.core.display.Im2LearnFrame;
import edu.illinois.ncsa.isda.imagetools.core.display.Im2LearnMenu;
import edu.illinois.ncsa.isda.imagetools.core.display.ImageComponent;
import edu.illinois.ncsa.isda.imagetools.core.display.ImagePanel;
import edu.illinois.ncsa.isda.imagetools.core.display.ImageUpdateEvent;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

/**
 * 	 <p>
	 <b>The class GammaDialog provides a tool for image intensity adjustment. </b>
	 </p>
	 </br>
	 <p>
	 <B>Description:</B>
	 </p>
	 <p>
	 If an image is over or under saturated some details might not be visible.
	 Fortunately, this over/under saturation can often be compensated by using gamma
	 control. Gamma adjustment allows you to brighten the whole image. The
	 formula used is: <code>newvalue = oldvalue^(gamma).</code> For gamma
	 values larger than one this will result in a brighter image, allowing you to see details
	 in darker regions of the image. For gamma values less than one this will result in a
	 darker image, allowing you to see details in parts of the image that are
	 oversaturated.
	 </p>
	 <p>
	 <img src="help/gammadialog.jpg">
	 <p>
	 The dialog allows a user to specify the gamma value by either entering
	 it in the text field, or adjusting the slider bar. To see the result,  a
	 user can press the preview button which will apply the gamma correction and
	 show a preview of the image. If the image is not satisfactory then a user can modify the
	 gamma value and check again using preview. If the result meets the desired quality
	 then by pressing the "Apply" button a user will apply the gamma correction to the currently
	 visible image.
	 </p>
  
 * Show a dialog that allows the user to change the gamma. The dialog contains a
 * small preview of the image, allowing the user to immediatly see the change in
 * the image after chaning the gamma. Pressing Apply will change the gamma of
 * the ncsa.im2learn.main imagepanel.
 *
 * @author Rob Kooper
 * @version 2.0
 */
public class GammaDialog extends Im2LearnFrame implements Im2LearnMenu {
    private JSlider sldGamma;
    private JTextField txtGamma;
    private ImagePanel imagepanel;
    private ImageComponent imgpreview;

    private static Log logger = LogFactory.getLog(GammaDialog.class);

    /**
     * Default contructor, creates the UI.
     *
     * @param owner frame this dialog belongs to.
     */
    public GammaDialog() {
        super("Select Gamma");
        setResizable(false);

        createUI();
        pack();
    }
    
    public void showing() {
        imageUpdated(new ImageUpdateEvent(this, ImageUpdateEvent.NEW_IMAGE, null));
    }
    
    public void closing() {
        imgpreview.setImageObject(null);
    }

    /**
     * Construct UI, with preview, slider and buttons.
     */
    private void createUI() {
        // preview image
        imgpreview = new ImageComponent();
        imgpreview.setAutozoom(true);
        imgpreview.setPreferredSize(new Dimension(200, 200));

        // slider control
        JPanel adjust = new JPanel(new BorderLayout());
        adjust.add(new JLabel("Gamma :"), BorderLayout.WEST);
        sldGamma = new JSlider(0, 500, 100);
        sldGamma.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                setGamma(sldGamma.getValue() / 100.0);
            }
        });
        adjust.add(sldGamma, BorderLayout.CENTER);
        txtGamma = new JTextField("1.0", 5);
        txtGamma.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    double gamma = Double.parseDouble(txtGamma.getText());
                    setGamma(gamma);
                } catch (NumberFormatException exc) {
                    logger.debug("Error parsing gamma", exc);
                    setGamma(sldGamma.getValue() / 100.0);
                }
            }
        });
        adjust.add(txtGamma, BorderLayout.EAST);

        // Buttons
        JPanel buttons = new JPanel(new FlowLayout());
        JButton btn = new JButton(new AbstractAction("Apply") {
            public void actionPerformed(ActionEvent e) {
                imagepanel.setGamma(imgpreview.getGamma());
                setVisible(false);
            }
        });
        buttons.add(btn);
        btn = new JButton(new AbstractAction("Cancel") {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        buttons.add(btn);

        // creaet the interface
        getContentPane().add(imgpreview, BorderLayout.NORTH);
        getContentPane().add(adjust, BorderLayout.CENTER);
        getContentPane().add(buttons, BorderLayout.SOUTH);
    }

    private void setGamma(double gamma) {
        if (gamma <= 0) {
            gamma = 0.01;
        }
        txtGamma.setText(gamma + "");
        sldGamma.setValue((int) (gamma * 100));
        imgpreview.setGamma(gamma);
    }

    // ------------------------------------------------------------------------
    // Im2LearnMenu implementation
    // ------------------------------------------------------------------------
    public void setImagePanel(ImagePanel imagepanel) {
        this.imagepanel = imagepanel;
    }

    public JMenuItem[] getPanelMenuItems() {
        JMenuItem item = new JMenuItem(new AbstractAction("Gamma Adjust") {
            public void actionPerformed(ActionEvent e) {
                setVisible(true);
            }
        });
        return new JMenuItem[]{item};
    }

    public JMenuItem[] getMainMenuItems() {
        return null;
    }

    public void imageUpdated(ImageUpdateEvent event) {
        if (!isVisible()) {
            return;
        }
        switch (event.getId()) {
            case ImageUpdateEvent.NEW_IMAGE:
                ImageObject imgobj = imagepanel.getImageObject();
                if (imgobj != null) {
                    // make a scaled copy
                    double sx = 200.0 / imgobj.getNumCols();
                    double sy = 200.0 / imgobj.getNumRows();
                    double sc = (sx < sy) ? sx : sy;

                    if (sc < 1.0) {
                        try {
                            imgobj = imgobj.scale(sc);
                        } catch (ImageException exc) {
                            logger.error("Could not subsample image.", exc);
                        }
                    }
                }
                imgpreview.setImageObject(imgobj);
                sldGamma.setValue((int) (100 * imagepanel.getGamma()));
                break;

            case ImageUpdateEvent.CHANGE_GAMMA:
                setGamma(imagepanel.getGamma());
                break;

            case ImageUpdateEvent.CHANGE_REDBAND:
                imgpreview.setRedBand(imagepanel.getRedBand());
                break;

            case ImageUpdateEvent.CHANGE_GREENBAND:
                imgpreview.setGreenBand(imagepanel.getGreenBand());
                break;

            case ImageUpdateEvent.CHANGE_BLUEBAND:
                imgpreview.setBlueBand(imagepanel.getBlueBand());
                break;

            case ImageUpdateEvent.CHANGE_RGBBAND:
                imgpreview.setRedBand(imagepanel.getRedBand());
                imgpreview.setGreenBand(imagepanel.getGreenBand());
                imgpreview.setBlueBand(imagepanel.getBlueBand());
                break;

            case ImageUpdateEvent.CHANGE_GRAYBAND:
                imgpreview.setGrayBand(imagepanel.getGrayBand());
                break;

            case ImageUpdateEvent.CHANGE_GRAYSCALE:
                imgpreview.setGrayScale(imagepanel.isGrayScale());
                break;
        }
    }
    
    public URL getHelp(String menu) {
    	if (menu.equals("Gamma Adjust")) {
    		return getClass().getResource("help/Gamma.html");
    	}
    	return null;
    }
}
