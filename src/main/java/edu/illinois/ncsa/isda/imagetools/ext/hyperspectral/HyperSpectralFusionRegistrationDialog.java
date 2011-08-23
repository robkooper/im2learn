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
package edu.illinois.ncsa.isda.imagetools.ext.hyperspectral;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageException;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.imagetools.core.display.Im2LearnFrame;
import edu.illinois.ncsa.isda.imagetools.ext.conversion.Registration;
import edu.illinois.ncsa.isda.imagetools.ext.conversion.RegistrationDialogUI;
import edu.illinois.ncsa.isda.imagetools.ext.misc.ImageFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Show two images side by side, allowing the user to pick points on both images
 * to calculate transform, and transform image.
 *
 * @author Sang-Chul Lee
 * @author Rob Kooper
 * @version 1.0
 */
public class HyperSpectralFusionRegistrationDialog extends Im2LearnFrame {
    private UI ui;
    private boolean canceled = false;
    private ImageObject imgobj;

    private static Log logger = LogFactory.getLog(HyperSpectralFusionRegistrationDialog.class);

    public HyperSpectralFusionRegistrationDialog() {
        super("Transform Image");
        setResizable(true);

        ui = new UI();
        getContentPane().add(ui);
        pack();
    }

    class UI extends RegistrationDialogUI {
        private JButton btnPreview;
        private ImageFrame preview;

        public UI() {
            preview = new ImageFrame("Preview Transformed");
            preview.setSize(640, 480);
        }

        public JPanel getButtons() {
            JPanel buttons = new JPanel(new FlowLayout());

            JButton btn = new JButton(new AbstractAction("Reset") {
                public void actionPerformed(ActionEvent e) {
                    resetMarkers();
                    btnPreview.setEnabled(false);
                }
            });
            buttons.add(btn);

            btnPreview = new JButton(new AbstractAction("Preview Transform") {
                public void actionPerformed(ActionEvent e) {
                    try {
                        preview.setImageObject(getImageTransformed(imgobj));
                        preview.setVisible(true);
                    } catch (ImageException exc) {
                        logger.error("Could not calculate transform, or set image.", exc);
                    }
                }
            });
            btnPreview.setEnabled(false);
            buttons.add(btnPreview);

            btn = new JButton(new AbstractAction("Done") {
                public void actionPerformed(ActionEvent e) {
                    HyperSpectralFusionRegistrationDialog.this.hide();
                }
            });
            buttons.add(btn);

            btn = new JButton(new AbstractAction("Cancel") {
                public void actionPerformed(ActionEvent e) {
                    canceled = true;
                    HyperSpectralFusionRegistrationDialog.this.hide();
                }
            });
            buttons.add(btn);

            return buttons;
        }

        public void addMarker() {
            if (isTransformValid()) {
                btnPreview.setEnabled(true);
            }
        }

        public void hidePreview() {
            preview.setVisible(false);
        }
    }

    public void closing() {
        ui.hidePreview();
        ui.resetImages();
        imgobj = null;
    }

    public void setImage1(ImageObject imgobj) throws ImageException {
        ui.setImage1(imgobj);
    }

    public void setImage2(ImageObject imgobj) throws ImageException {
        ui.setImage2(imgobj);
        this.imgobj = imgobj;
    }

    public ImageObject getImageTransformed(ImageObject imgobj) throws ImageException {
        if (!ui.isTransformValid()) {
            throw(new ImageException("Not enough points selected, min is 3."));
        }

        // calculate the affine transform base on the points selected
        Point[] f1 = ui.getPointsInImage1();
        Point[] f2 = ui.getPointsInImage2();

        return Registration.getImageTransformed(imgobj, f1, f2);
    }

    public boolean isCanceled() {
        return canceled;
    }
}
