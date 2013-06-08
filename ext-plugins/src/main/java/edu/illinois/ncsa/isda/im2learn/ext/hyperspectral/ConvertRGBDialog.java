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
package edu.illinois.ncsa.isda.im2learn.ext.hyperspectral;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageException;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.im2learn.core.display.*;
import edu.illinois.ncsa.isda.im2learn.core.io.jai.JAIutil;
import edu.illinois.ncsa.isda.im2learn.ext.conversion.Registration;
import edu.illinois.ncsa.isda.im2learn.ext.conversion.RegistrationDialogUI;
import edu.illinois.ncsa.isda.im2learn.ext.misc.ImageFrame;
import edu.illinois.ncsa.isda.im2learn.ext.misc.JSelectFile;
import edu.illinois.ncsa.isda.im2learn.ext.misc.PlotComponent;
import edu.illinois.ncsa.isda.im2learn.ext.panel.SelectBandDialog;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;

/**
 *
 *	<p>
	<B>The class HyperSpectralToRGB provides a tool for display and data conversion
	 of hyperspectral images to red-green-blue (RGB) image. </B>
	 </p>
	 <p>
	 <b>Description:</b>
	
	 Hyperspectral images contain too many bands to display at the same time
	 as regular monocromatic or color images.
	 For instance, to be able to display a hyperspectral image covering visible
	 spectral range, a user will often have to select three bands that
	 correspond approximately to the red, green and blue wavelengths. Fortunately
	 the wavelength information associated with each hyperspectral image band
	 is known and a user could select the bands of choice for display.
	 </p>
	 <p>
	 This tool is designed to automatically convert a hyperspectral image to a RGB image by
	 collapsing each of the hyperspectral bands to the RGB bands. This is done by
	 using the algorithm developed by Dan Bruton (
	 <a href="http://www.physics.sfasu.edu/astro/color.html">
	 http://www.physics.sfasu.edu/astro/color.html</a>). The algorithm takes
	 the wavelength, determines the percentage it contributes to the Red, Green
	 and Blue color, and multiplies it with the intensity to create the RGB
	 image. In the case of non-visible light (lower than 380 and more that 780nm)
	 the percentage it adds to the RGB values is 0.
	 </p>
	 <img src="help/convertrgb.jpg">
	 <p>
	 This dialog allows users either to choose the existing wavelenths that describe
	  the hyperspectral image (if available) or to specify the wavelengths of the first
	 and last bands and interpolating the wavelength for all the bands in between first
	 and last bands. The dialog will enable both radio buttons "Use existing wavelengths" and
	 "Specify wavelength range" if the loaded hyperspectral image contains information
	 about band wavelengths. Otherwise the option of "Use existing wavelengths" is disabled.
	 To see the selected spectrum range specified by fisrt and last band wavelengths,
	 users can press the "Show Spectrum" button and view the spectrum range visualization (see below).
	 </p>
	
	 <img src="help/spectrum.jpg">
	 <p>
	 Once a user is satisfied with the spectrum range,  pressing the "Apply" button
	  will convert the current hyperspectral image to a RGB image.
	 </p>
  	
 * @author Rob Kooper
 * @version 2.0
 */
public class ConvertRGBDialog extends Im2LearnFrame implements Im2LearnMenu {
    private JTextField txtFirstBand;
    private JTextField txtLastBand;
    private JRadioButton btnUseImage;
    private JRadioButton btnUseUser;

    private static double BLUE = 380;
    private static double RED = 780;

    private JPanel pnlBruton;
    private JPanel pnlEstimate;
    private PlotComponent pcSpectrum;
    private ImageComponent icSpectrum;
    private JSelectFile sfConvImages;
    private JSelectFile sfHSImages;
    private ConvertRGB convertrgb;
    private ImageFrame frmPreview;
    private JTextField txtSamples;
    private ImageObject imgrgb;
    private ImagePanel imagepanel;

    private static Log logger = LogFactory.getLog(ConvertRGBDialog.class);

    /**
     * Default constructor will create the whole UI and select by default the
     * Bruton method for conversion.
     */
    public ConvertRGBDialog() {
        super("HS to RGB conversion");

        GridBagConstraints gridConstraints = new GridBagConstraints();
        gridConstraints.fill = GridBagConstraints.BOTH;
        gridConstraints.weighty = 0.0;
        gridConstraints.weightx = 1.0;
        gridConstraints.gridwidth = 1;
        gridConstraints.insets = new Insets(2, 2, 2, 2);

        GridBagLayout gridBag = new GridBagLayout();
        getContentPane().setLayout(gridBag);

        JComboBox cmb = new JComboBox(new String[]{"Bruton", "Compute"});
        cmb.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JComboBox cmb = (JComboBox) e.getSource();
                if (cmb.getSelectedItem().equals("Bruton")) {
                    pnlBruton.setVisible(true);
                    pnlEstimate.setVisible(false);
                } else {
                    pnlBruton.setVisible(false);
                    pnlEstimate.setVisible(true);
                }
            }
        });
        gridConstraints.gridx = 0;
        gridConstraints.gridy = 0;
        gridBag.setConstraints(cmb, gridConstraints);
        getContentPane().add(cmb);

        sfConvImages = new JSelectFile(new String[]{"Image"});
        gridConstraints.gridx = 0;
        gridConstraints.gridy++;
        gridBag.setConstraints(sfConvImages, gridConstraints);
        getContentPane().add(sfConvImages);

        pcSpectrum = new PlotComponent();
        pcSpectrum.setPreferredSize(new Dimension(200, 100));
        gridConstraints.gridx = 0;
        gridConstraints.gridy++;
        gridBag.setConstraints(pcSpectrum, gridConstraints);
        getContentPane().add(pcSpectrum);

        icSpectrum = new ImageComponent();
        icSpectrum.setPreferredSize(new Dimension(200, 40));
        gridConstraints.gridx = 0;
        gridConstraints.gridy++;
        gridBag.setConstraints(icSpectrum, gridConstraints);
        getContentPane().add(icSpectrum);

        gridConstraints.weighty = 1.0;
        pnlBruton = brutonUI();
        gridConstraints.gridx = 0;
        gridConstraints.gridy++;
        gridBag.setConstraints(pnlBruton, gridConstraints);
        getContentPane().add(pnlBruton);

        pnlEstimate = estimateUI();
        gridBag.setConstraints(pnlEstimate, gridConstraints);
        getContentPane().add(pnlEstimate);

        gridConstraints.weighty = 0.0;
        JPanel buttons = getButtons();
        gridConstraints.gridx = 0;
        gridConstraints.gridy++;
        gridBag.setConstraints(buttons, gridConstraints);
        getContentPane().add(buttons);

        frmPreview = new ImageFrame("Preview Conversion");
        frmPreview.setSize(640, 480);

        imgrgb = null;
        pack();

        pnlEstimate.setVisible(false);
    }
    
    public void showing() {
        try {
            brutonCalc();
        } catch (ImageException exc) {
            logger.error("Error converting Bruton.", exc);
        }
    }

    public void closing() {
        imgrgb = null;
    }

    /**
     * Create a panel containing all the action buttons.
     *
     * @return panel with all the buttons.
     */
    private JPanel getButtons() {
        JPanel ui = new JPanel(new FlowLayout());

        JButton btnCompute = new JButton(new AbstractAction("Compute") {
            public void actionPerformed(ActionEvent e) {
                try {
                    if (pnlBruton.isVisible()) {
                        brutonCalc();
                    } else {
                        estimateCalc();
                    }
                } catch (ImageException exc) {
                    logger.error("Error calculating conversion.", exc);
                }
            }
        });
        ui.add(btnCompute);

        JButton btnPreview = new JButton(new AbstractAction("Convert") {
            public void actionPerformed(ActionEvent e) {
                try {
                    ImageObject hs = getImage();
                    frmPreview.setImageObject(convertrgb.convert(hs));
                    frmPreview.setVisible(true);
                } catch (IOException exc) {
                    logger.error("Error loading HS image.", exc);
                } catch (ImageException exc) {
                    logger.error("Error calculating conversion.", exc);
                }
            }
        });
        ui.add(btnPreview);

        JButton btnApply = new JButton(new AbstractAction("Apply") {
            public void actionPerformed(ActionEvent e) {
                try {
                    ImageObject hs = getImage();
                    imagepanel.setImageObject(convertrgb.convert(hs));
                } catch (IOException exc) {
                    logger.error("Error loading HS image.", exc);
                } catch (ImageException exc) {
                    logger.error("Error calculating conversion.", exc);
                }
            }
        });
        ui.add(btnApply);

        JButton btnDone = new JButton(new AbstractAction("Done") {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        ui.add(btnDone);

        return ui;
    }

    /**
     * Return either the image currently shown in the ncsa.im2learn.main frame or
     * the image specified in the input field.
     *
     * @return an ImageObject specified by the user.
     * @throws IOException if the image could not be loaded.
     */
    private ImageObject getImage() throws IOException {
        if (imagepanel != null) {
            ImageObject current = imagepanel.getImageObject();
            if (current != null) {
                return sfConvImages.getImageObject(0, current);
            }
        }
        return sfConvImages.getImageObject(0);
    }

    // ------------------------------------------------------------------------
    // Bruton
    // ------------------------------------------------------------------------
    /**
     * Create a panel with the interface for setting the parameters for Bruton.
     * This will be the first and last wavelength and wheter or not to use the
     * wavelengths in the image.
     *
     * @return panel with configuration parameters for Bruton.
     */
    private JPanel brutonUI() {
        GridBagConstraints gridConstraints = new GridBagConstraints();
        gridConstraints.fill = GridBagConstraints.BOTH;
        gridConstraints.weighty = 0.0;
        gridConstraints.weightx = 0.0;
        gridConstraints.gridwidth = 1;
        gridConstraints.gridy = 0;
        gridConstraints.insets = new Insets(2, 2, 2, 2);

        GridBagLayout gridBag = new GridBagLayout();
        JPanel ui = new JPanel(gridBag);
        ui.setBorder(new TitledBorder("Bruton Configuration"));

        // radio button to select use image or user
        ButtonGroup group = new ButtonGroup();
        btnUseImage = new JRadioButton("Use Image Wavelength", false);
        btnUseImage.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JRadioButton btn = (JRadioButton) e.getSource();
                if (btn.isSelected()) {
                    txtFirstBand.setEnabled(false);
                    txtLastBand.setEnabled(false);
                }
            }
        });
        group.add(btnUseImage);
        gridConstraints.gridx = 0;
        gridBag.setConstraints(btnUseImage, gridConstraints);
        ui.add(btnUseImage);

        btnUseUser = new JRadioButton("Use User Wavelength", true);
        btnUseUser.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JRadioButton btn = (JRadioButton) e.getSource();
                if (btn.isSelected()) {
                    txtFirstBand.setEnabled(true);
                    txtLastBand.setEnabled(true);
                }
            }

        });
        group.add(btnUseUser);
        gridConstraints.gridx = 1;
        gridBag.setConstraints(btnUseUser, gridConstraints);
        ui.add(btnUseUser);

        // user select values (enabled if user)
        JLabel lbl = new JLabel("Wavelength first band");
        gridConstraints.gridx = 0;
        gridConstraints.gridy++;
        gridBag.setConstraints(lbl, gridConstraints);
        ui.add(lbl);

        txtFirstBand = new JTextField("" + BLUE, 5);
        txtFirstBand.setEnabled(true);
        gridConstraints.gridx = 1;
        gridBag.setConstraints(txtFirstBand, gridConstraints);
        ui.add(txtFirstBand);

        lbl = new JLabel("Wavelength last band");
        gridConstraints.gridx = 0;
        gridConstraints.gridy++;
        gridBag.setConstraints(lbl, gridConstraints);
        ui.add(lbl);

        txtLastBand = new JTextField("" + RED, 5);
        txtLastBand.setEnabled(true);
        gridConstraints.gridx = 1;
        gridBag.setConstraints(txtLastBand, gridConstraints);
        ui.add(txtLastBand);

        return ui;
    }

    /**
     * Calculate the conversion matrix for Bruton. This will use the image
     * selected to find the number of bands.
     *
     * @param firsttime to ignor the fact that no image exists on creation
     * @throws ImageException if the matrix could not be created.
     */
    private void brutonCalc() throws ImageException {
        ImageObject hs = null;

        try {
            hs = getImage();
        } catch (IOException exc) {
            throw(new ImageException(exc));
        }

        if (btnUseUser.isSelected()) {
            int bands = 200;
            double firstband;
            double lastband;

            try {
                firstband = Double.parseDouble(txtFirstBand.getText());
            } catch (NumberFormatException exc) {
                txtFirstBand.setText("" + BLUE);
                throw(new ImageException(exc));
            }

            try {
                lastband = Double.parseDouble(txtLastBand.getText());
            } catch (NumberFormatException exc) {
                txtLastBand.setText("" + RED);
                throw(new ImageException(exc));
            }

            if (hs != null) {
                bands = hs.getNumBands();
            }

            convertrgb = ConvertRGB.getBruton(firstband, lastband, bands);
        } else {
            if (hs == null) {
                btnUseUser.setSelected(true);
                throw(new ImageException("Image does not contain wavelength."));
            }
            Object[] wl = (Object[]) hs.getProperty(ImageObject.WAVELENGTH);
            if (wl == null) {
                btnUseUser.setSelected(true);
                throw(new ImageException("Image does not contain wavelength."));
            }

            convertrgb = ConvertRGB.getBruton(wl);
        }

        icSpectrum.setImageObject(convertrgb.getSpectrum());
        convertrgb.getPlot(pcSpectrum);
    }

    // ------------------------------------------------------------------------
    // Estimation
    // ------------------------------------------------------------------------
    /**
     * Create an UI that allows the user to specify two image files, register
     * those images with each other and set the number of samples to use when
     * computing the conversion matrix.
     *
     * @return a panel with the interface.
     */
    private JPanel estimateUI() {
        GridBagConstraints gridConstraints = new GridBagConstraints();
        gridConstraints.fill = GridBagConstraints.BOTH;
        gridConstraints.weighty = 0.0;
        gridConstraints.weightx = 0.0;
        gridConstraints.gridwidth = 1;
        gridConstraints.gridy = 0;
        gridConstraints.insets = new Insets(2, 2, 2, 2);

        GridBagLayout gridBag = new GridBagLayout();
        JPanel ui = new JPanel(gridBag);
        ui.setBorder(new TitledBorder("Estimation Configuration"));

        sfHSImages = new JSelectFile(new String[]{"HS Image", "RGB Image"});
        sfHSImages.getImagePanel(0).addMenu(new SelectBandDialog());
        gridConstraints.gridx = 0;
        gridConstraints.gridwidth = 3;
        gridBag.setConstraints(sfHSImages, gridConstraints);
        ui.add(sfHSImages);

        JLabel lbl = new JLabel("Samples to use:");
        gridConstraints.gridwidth = 1;
        gridConstraints.gridy++;
        gridConstraints.gridx = 0;
        gridBag.setConstraints(lbl, gridConstraints);
        ui.add(lbl);

        txtSamples = new JTextField("250", 4);
        gridConstraints.gridx = 1;
        gridBag.setConstraints(txtSamples, gridConstraints);
        ui.add(txtSamples);

        if (JAIutil.haveJAI()) {
            JButton btnRegister = new JButton(new AbstractAction("Register") {
                public void actionPerformed(ActionEvent e) {
                    try {
                        ImageObject hs = sfHSImages.getImageObject(0);
                        ImageObject rgb = sfHSImages.getImageObject(1);

                        // register images
                        UI ui = new UI();
                        ui.setImage1(hs);
                        ui.setImage2(rgb);

                        JDialog dialog = new JDialog((Frame) null, "Register", false);
                        dialog.getContentPane().add(ui);
                        dialog.pack();
                        dialog.setLocationRelativeTo(ConvertRGBDialog.this);
                        dialog.setVisible(true);
                    } catch (IOException exc) {
                        exc.printStackTrace();
                    } catch (ImageException exc) {
                        exc.printStackTrace();
                    }
                }
            });
            gridConstraints.gridx = 2;
            gridBag.setConstraints(btnRegister, gridConstraints);
            ui.add(btnRegister);
        }

        return ui;
    }

    /**
     * Calculate the matrix to be used when converting a hyperspectral image.
     * This method will use the images specified, and calculate the matrix using
     * a set number of samples as specified in the text field.
     *
     * @throws ImageException if the matrix could not be computed.
     */
    private void estimateCalc() throws ImageException {
        try {
            ImageObject hs = sfHSImages.getImageObject(0);
            ImageObject rgb;

            if (imgrgb != null) {
                rgb = imgrgb;
            } else {
                rgb = sfHSImages.getImageObject(1);
            }

            int samples = Integer.parseInt(txtSamples.getText());
            convertrgb = ConvertRGB.getEstimated(samples, hs, rgb);

            icSpectrum.setImageObject(convertrgb.getSpectrum());
            convertrgb.getPlot(pcSpectrum);
        } catch (NumberFormatException exc) {
            throw(new ImageException(exc));
        } catch (IOException exc) {
            throw(new ImageException(exc));
        }
    }

    //private ImageObject

    /**
     * Dialog used to allign the HS and RGB image for the estimation.
     */
    class UI extends RegistrationDialogUI {
        public UI() {
            super();
        }

        public JPanel getButtons() {
            JPanel buttons = super.getButtons();

            JButton btnRegister = new JButton(new AbstractAction("Done") {
                public void actionPerformed(ActionEvent e) {
                    if (isTransformValid()) {
                        Window window = SwingUtilities.getWindowAncestor(UI.this);
                        window.setVisible(false);
                        synchronized(window) {
                            window.notifyAll();
                        }

                        // convert RGB
                        Point[] f1 = getPointsInImage1();
                        Point[] f2 = getPointsInImage2();
                        ImageObject img = getImage2();
                        try {
                            imgrgb = Registration.getImageTransformed(img, f1, f2);
                        } catch (ImageException exc) {
                            logger.warn("Error registring image.", exc);
                        }
                    }
                }
            });
            buttons.add(btnRegister);

            JButton btnExit = new JButton(new AbstractAction("Abort") {
                public void actionPerformed(ActionEvent e) {
                    resetMarkers();
                    Window window = SwingUtilities.getWindowAncestor(UI.this);
                    window.setVisible(false);
                    synchronized(window) {
                        window.notifyAll();
                    }
                }
            });
            buttons.add(btnExit);

            return buttons;
        }
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
        JMenuItem convert = new JMenuItem(new AbstractAction("Convert to RGB") {
            public void actionPerformed(ActionEvent e) {
                setLocationRelativeTo(getOwner());
                setVisible(true);
            }
        });
        hs.add(convert);
        return new JMenuItem[]{tools};
    }

    public void imageUpdated(ImageUpdateEvent event) {
    }
    
    public URL getHelp(String menu) {
        return getClass().getResource("help/ConvertRGB.html");
    }
}
