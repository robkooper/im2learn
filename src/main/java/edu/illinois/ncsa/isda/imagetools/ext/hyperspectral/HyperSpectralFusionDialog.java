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
import edu.illinois.ncsa.isda.imagetools.core.display.Im2LearnMenu;
import edu.illinois.ncsa.isda.imagetools.core.display.ImagePanel;
import edu.illinois.ncsa.isda.imagetools.core.display.ImageUpdateEvent;
import edu.illinois.ncsa.isda.imagetools.core.io.FileChooser;
import edu.illinois.ncsa.isda.imagetools.core.io.ImageLoader;
import edu.illinois.ncsa.isda.imagetools.core.io.jai.JAIutil;
import edu.illinois.ncsa.isda.imagetools.ext.misc.ImageFrame;
import edu.illinois.ncsa.isda.imagetools.ext.misc.JSelectFile;
import edu.illinois.ncsa.isda.imagetools.ext.panel.SelectBandDialog;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URL;

/**
 * 
 	<p>
	<B>The class HyperSpectralFusion provides a tool for fusing hyperspectral images acquired at different wavelength ranges 
	and spectral resolutions.
	 </B>
	 </p>

	 <p>
	 <b>Description:</b>
This class was developed for fusing hyperspectral images acquired by OptoKnowledge Inc. hyperspectral camera that 
contains visible spectrum and near infrared spectrum liquid crystal tunable filters (LCTF). The hyperspectral images of 
the same scene are acquired at different wavelengths (e.g., [400nm, 720nm] and [65nm, 1100nm]). Thus, the fusion
process consists of spatial alignment (registration) and then spectral fusion of the overlapping bands.
 The dialog for hyperspectral image fusion is shown below.
 <BR>
 <BR>
	 <img src="help/hsFusionDialog.jpg">
<BR>
<BR>
First, two images are selected using the file chooser invoked by clicking on the button "...". The images can be previewed using the adjacent buttons 
labeled as "View". After clicking the button "Align", a frame with the two selected images will appear (see below). 
The registration control points have to be selected by clicking at corresponding salient features of images in each image panel.
A user has to select at least three registration control points since an affine transformation model is used. By right clicking
on either image panel, one would be allowed to choose the combination of displayed bands for multi-band images, zoom level 
and image operations. 
<BR>
	 <img src="help/hsFusionDialog1.jpg">
<BR>
<BR>
The resulting image after alignment can be previewed by clicking on the button " Preview Transform". An example image after 
alignment is shown below. The operation is completed and the "Transform Image" frame is closed after clicking the button "Done".  <BR>
	 <img src="help/hsFusionDialog2.jpg">
<BR>
<BR>
To perform the spectral fusion, the button "Fuse" should be pressed. Before executing the fusion, one should 
set the fusion method (simple, average, step),the step parameter and the option denoted as "Merge absolute values?".
<BR>
 <br>
 The method denoted as <b>"Simple"</b> takes always bands from image1 if wavelengths overlap otherwise bands from image1 or image2
are used in the resulting fused image. The wavelength increments will not be normalized (i.e., if image1 
has a 10nm step per wavelength and image2 has 15nm per wavelength, the resulting image will have wavelengths 
of 10, 15, 20, 30, 40, 45 etc. The wavelength spacing will be stored in image properties.
<BR> 
  <br>
 The method denoted as <b>"Average"</b> creates an average band value from image1 and image2 if wavelengths overlap
 otherwise bands from image1 or image2 are used in the resulting fused image. 
 Based on the option denoted as "Merge absolute values?" either the values are simply averaged, or 
 the relative values with respect to each image dynamic range are averaged.
 The wavelength increments will not be normalized (i.e., if image1 
 has a 10nm step per wavelength and image2 has 15nm per wavelength, the resulting image will have wavelengths 
of 10, 15, 20, 30, 40, 45 etc. The wavelength spacing will be stored in image properties.. 
  <br>
  <br>
  The method denoted as <b>"Step"</b> forms the resulting fused image by using a fixed spectral increment based 
  on the step parameter. In the image1 and image2 overlapping spectral range, the fused value is determined by
  linear interpolation of the closest values, or if the image1 and image2 bands are the same then by averaging. In the non-overlapping
  spectral regions, the desired band is computed by interpolating two closest bands.
<br>
 <br>
  An example of the fusion result is illustrated in the figure below. The example 
  shows purposely misaligned images with
  two bands (blue and green) from image 1 and the third band fromthe physical 
  red wavelength range obtained by using the "Simple" method for computing the new values.
<BR>
  <img src="help/hsFusionDialog3.jpg"> <BR>
  <BR>
<br>
 * @author Rob Kooper
 * @author Peter Bajcsy (documentation)
 * @version 1.0
 */
public class HyperSpectralFusionDialog extends Im2LearnFrame implements Im2LearnMenu {
    private ImagePanel imagepanel;

    private JSelectFile sfImages = null;

    private ImageObject imgImage2Alligned;
    private JCheckBox chkUseAlligned;
    private JCheckBox chkUseAbsolute;
    private JComboBox cmbFuse;
    private JTextField txtStep;

    private JButton btnAllign;
    private JButton btnFuse;
    private JButton btnPreview;
    private JButton btnSave;
    private JButton btnApply;

    private ImageFrame preview = null;
    private ImageObject imgResult = null;

    private static Log logger = LogFactory.getLog(HyperSpectralFusionDialog.class);

    /**
     * Default constructor
     */
    public HyperSpectralFusionDialog() {
        super("Fuse Images");
        setResizable(false);

        createGUI();
    }

    /**
     * Create the GUI.
     */
    protected void createGUI() {
        JLabel lbl = null;

        // preview frame
        preview = new ImageFrame("Preview Fusion");
        preview.getImagePanel().addMenu(new SelectBandDialog());

        preview.setSize(640, 480);

        // create the image holder
        sfImages = new JSelectFile(new String[]{"Image 1", "Image 2"});
        sfImages.setToolTipText(0, "Leave blank to use image shown in mainframe.");
        sfImages.setBorder(new TitledBorder("Select Images"));

        // funsion method
        GridBagConstraints gridConstraints = new GridBagConstraints();
        gridConstraints.fill = GridBagConstraints.BOTH;
        gridConstraints.weightx = 0.0;
        gridConstraints.weighty = 0.0;

        GridBagLayout gridBag = new GridBagLayout();
        JPanel fusion = new JPanel(gridBag);
        fusion.setBorder(new TitledBorder("Fusion Parameters"));

        gridConstraints.gridx = 0;
        gridConstraints.gridy = 0;
        chkUseAlligned = new JCheckBox("Use alligned image?", true);
        chkUseAlligned.setEnabled(false);
        gridBag.setConstraints(chkUseAlligned, gridConstraints);
        fusion.add(chkUseAlligned);

        gridConstraints.gridy++;
        gridConstraints.gridx = 0;
        gridConstraints.gridwidth = 1;
        lbl = new JLabel("Method");
        gridBag.setConstraints(lbl, gridConstraints);
        fusion.add(lbl);

        gridConstraints.gridx = 1;
        gridConstraints.weightx = 1.0;
        cmbFuse = new JComboBox(HyperSpectralFusion.methods);
        gridBag.setConstraints(cmbFuse, gridConstraints);
        fusion.add(cmbFuse);

        gridConstraints.gridy++;
        gridConstraints.gridx = 0;
        gridConstraints.weightx = 0.0;
        lbl = new JLabel("Step Value");
        gridBag.setConstraints(lbl, gridConstraints);
        fusion.add(lbl);

        gridConstraints.gridx = 1;
        gridConstraints.weightx = 1.0;
        txtStep = new JTextField("10", 5);
        gridBag.setConstraints(txtStep, gridConstraints);
        fusion.add(txtStep);

        gridConstraints.gridy++;
        gridConstraints.gridx = 0;
        gridConstraints.weightx = 0.0;
        chkUseAbsolute = new JCheckBox("Merge absolute values?", true);
        gridBag.setConstraints(chkUseAbsolute, gridConstraints);
        fusion.add(chkUseAbsolute);

        // buttons
        JPanel buttons = new JPanel(new FlowLayout());

        btnAllign = new JButton(new AbstractAction("Allign") {
            public void actionPerformed(ActionEvent e) {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                try {
                    allign();
                } catch (IOException exc) {
                    logger.error("Error loading image.", exc);
                } catch (ImageException exc) {
                    logger.error("Error alligning image.", exc);
                }
                setCursor(Cursor.getDefaultCursor());
            }
        });
        if (JAIutil.haveJAI()) {
            buttons.add(btnAllign);
        }

        btnFuse = new JButton(new AbstractAction("Fuse") {
            public void actionPerformed(ActionEvent e) {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                btnPreview.setEnabled(false);
                btnSave.setEnabled(false);
                btnApply.setEnabled(false);
                try {
                    fuse();
                } catch (IOException exc) {
                    logger.error("Error loading image.", exc);
                } catch (ImageException exc) {
                    logger.error("Error illuminating image.", exc);
                }
                setCursor(Cursor.getDefaultCursor());
            }
        });
        buttons.add(btnFuse);

        // show a preview of the result, frame is forced to be 640x480 by
        // default (user can always resize). The frame is reused when
        // previewing many results.
        btnPreview = new JButton(new AbstractAction("Preview") {
            public void actionPerformed(ActionEvent e) {
                preview.setImageObject(imgResult);
                preview.setVisible(true);
            }
        });
        btnPreview.setEnabled(false);
        buttons.add(btnPreview);

        // save the result to disk
        btnSave = new JButton(new AbstractAction("Save") {
            public void actionPerformed(ActionEvent e) {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                FileChooser dialog = new FileChooser();
                dialog.setTitle("Save Illumination Result");
                try {
                    String filename = dialog.showSaveDialog();
                    ImageLoader.writeImage(filename, imgResult);
                } catch (IOException exc) {
                    logger.error("Error saving file", exc);
                }
                setCursor(Cursor.getDefaultCursor());
            }
        });
        btnSave.setEnabled(false);
        buttons.add(btnSave);

        // copy the result back to imagepanel
        btnApply = new JButton(new AbstractAction("Apply") {
            public void actionPerformed(ActionEvent e) {
                imagepanel.setImageObject(imgResult);
            }
        });
        btnApply.setEnabled(false);
        buttons.add(btnApply);

        // close the dialog
        JButton btn = new JButton(new AbstractAction("Close") {
            public void actionPerformed(ActionEvent e) {
                HyperSpectralFusionDialog.this.setVisible(false);
            }
        });
        buttons.add(btn);

        // combine all the pieces
        this.getContentPane().add(sfImages, BorderLayout.NORTH);
        this.getContentPane().add(fusion, BorderLayout.CENTER);
        this.getContentPane().add(buttons, BorderLayout.SOUTH);

        pack();
    }

    /**
     * Deallocate as much memory as
     */
    public void closing() {
        preview.setVisible(false);
        imgImage2Alligned = null;
        imgResult = null;
        chkUseAlligned.setSelected(false);
        chkUseAlligned.setEnabled(false);
        btnPreview.setEnabled(false);
        btnSave.setEnabled(false);
        btnApply.setEnabled(false);
        sfImages.reset(false);
    }

    /**
     * This function will load any images if needed and call the
     * HyperSpectralFusion class to fuse the images with regard using the
     * selected method.
     */
    protected void allign() throws IOException, ImageException {
        HyperSpectralFusionRegistrationDialog transform = null;

        // setup the allign dialog and show it
        transform = new HyperSpectralFusionRegistrationDialog();
        transform.setImage1(sfImages.getImageObject(0, imagepanel.getImageObject()));
        transform.setImage2(sfImages.getImageObject(1));
        transform.setVisible(true);

        // get the second image transformed
        if (!transform.isCanceled()) {
            imgImage2Alligned = transform.getImageTransformed(sfImages.getImageObject(1));
            chkUseAlligned.setEnabled(true);
        }
    }

    /**
     * This function will load any images if needed and call the
     * HyperSpectralFusion class to fuse the images with regard using the
     * selected method.
     */
    protected void fuse() throws IOException, ImageException {
        double step = 10;
        try {
            step = Double.parseDouble(txtStep.getText());
        } catch (NumberFormatException exc) {
            logger.info("Invalid number entered, resetting to 10.", exc);
            txtStep.setText("10");
        }

        // create a new instace of the illumination code and give the black and
        // illumination image
        HyperSpectralFusion fusion = new HyperSpectralFusion();
        fusion.setImage1(sfImages.getImageObject(0, imagepanel.getImageObject()));
        if (imgImage2Alligned != null && chkUseAlligned.isSelected()) {
            fusion.setImage2(imgImage2Alligned);
        } else {
            fusion.setImage2(sfImages.getImageObject(1));
        }
        fusion.setAbsoluteMerge(chkUseAbsolute.isSelected());
        fusion.setStep(step);

        // calculate the resulting image
        imgResult = fusion.fuse(cmbFuse.getSelectedItem().toString());

        // enable rest of buttons
        btnApply.setEnabled(true);
        btnPreview.setEnabled(true);
        btnSave.setEnabled(true);
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
        JMenu tools = new JMenu("Tools");
        JMenu hs = new JMenu("HyperSpectral");
        tools.add(hs);
        JMenuItem illuminate = new JMenuItem(new AbstractAction("Fuse Images") {
            public void actionPerformed(ActionEvent e) {
                setLocationRelativeTo(getOwner());
                setVisible(true);
            }
        });
        hs.add(illuminate);
        return new JMenuItem[]{tools};
    }

    public void imageUpdated(ImageUpdateEvent event) {
    }

    public URL getHelp(String menu) {
        return getClass().getResource("help/HSFusionDialog.html");
    }    
}
