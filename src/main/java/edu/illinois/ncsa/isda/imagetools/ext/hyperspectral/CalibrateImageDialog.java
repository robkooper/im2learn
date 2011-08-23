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
import edu.illinois.ncsa.isda.imagetools.core.display.HelpViewer;
import edu.illinois.ncsa.isda.imagetools.core.display.Im2LearnFrame;
import edu.illinois.ncsa.isda.imagetools.core.display.Im2LearnMenu;
import edu.illinois.ncsa.isda.imagetools.core.display.ImagePanel;
import edu.illinois.ncsa.isda.imagetools.core.display.ImageUpdateEvent;
import edu.illinois.ncsa.isda.imagetools.core.io.FileChooser;
import edu.illinois.ncsa.isda.imagetools.core.io.ImageLoader;
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
	<B>The class CalibrateImage provides a tool for intensity calibration using 'black' and 'white' images.  </B>
	 </p>

	 <p>
	 <b>Description:</b>
This class is used for calibrating raw image values using black and white reference images.
 The callibration proccess will take take 3 images, a black image (e.g., 
 taken with the camera lens cap on), a white image (e.g., an image of a perfect white background with 100% reflectivity),
 and the image that needs callibration. 
 If no black image is specified, the black is assumed to be 0 everywhere. 
 The dialog for choosing the three image files and vieweing each file is shown below.
 <BR>
 <BR>
	 <img src="help/calibrateImageDialog.jpg">
<BR>
Example of a raw image: <BR>
	 <img src="help/calibrateImageDialog1.jpg">
<BR>
Example of a black image: <BR>
	 <img src="help/calibrateImageDialog2.jpg">
<BR>
Example of a white image: <BR>
	 <img src="help/calibrateImageDialog3.jpg">
<BR> 
 <br>
  Once the images are specified, the calibration is executed using the following formula.
<br>
 
 <pre>
 calibrated pix[i, j] = { 1 if original pix[i, j] >= white pix[i, j],
                          0 if original pix[i, j] =< black pix[i, j],
                          (original pix[i, j] - black pix[i, j]) / (white
 pix[i, j] - black pix[i, j]) else;
 </pre>
 This will result in a calibrated image with values between 0 and 1.
The execution is triggered by clicking the buttom "Calibrate".
<BR>
Before calibrating the raw image, a user can also specify the data type of the resulting image (
byte, short, int, long, float, double, original input type), the multiplier to widen the dynamic range 
of the resulting image and clipping option (yes or no). The 'Clip result?' option enabled replaces
values outside of the exepected range [0,1] to minimum or maximum values.
<BR>
	 <p>
After previewing the calibrated image (button 'Preview'), a user can save the calibrated image (button 'Save') 
or apply it to the main frame for further processing (button 'Apply'). The dialog is closed with the button 'Close'.
	 </p>
 	
 * @author Rob Kooper
 * @author Tyler J. Alumbaugh
 * @author Peter Bajcsy (documentation)
 * @see CalibrateImage
 */
public class CalibrateImageDialog extends Im2LearnFrame implements Im2LearnMenu {
    private ImageObject imgResult = null;
    private ImageFrame preview = null;
    private ImagePanel imagepanel = null;

    private JSelectFile sfImages;
    private JCheckBox chkClip = new JCheckBox("", true);
    private JTextField txtMultiplier = new JTextField("1");
    private JComboBox cmbType = new JComboBox(ImageObject.types);
    private JButton btnCalibrate = null;
    private JButton btnPreview = null;
    private JButton btnSave = null;
    private JButton btnApply = null;

    private final int ORIGINAL = 0;
    private final int BLACK = 1;
    private final int WHITE = 2;

    private static Log logger = LogFactory.getLog(CalibrateImageDialog.class);

    /**
     * Default constructor
     */
    public CalibrateImageDialog() {
        super("Calibrate Image");
        setResizable(false);
        createGUI();
    }

    /**
     * Create the GUI.
     */
    protected void createGUI() {
        JLabel lbl = null;

        // preview frame
        preview = new ImageFrame("Preview Calibrate");
        preview.getImagePanel().addMenu(new SelectBandDialog());
        preview.setSize(640, 480);

        // Selection of files to use
        sfImages = new JSelectFile(new String[]{"Uncalibrated", "Black", "White"});
        sfImages.setToolTipText(ORIGINAL, "Leave blank to use image shown in mainframe.");
        sfImages.setToolTipText(BLACK, "Leave blank to use 0 as black.");
        sfImages.setBorder(new TitledBorder("Select Images"));

        // Resulting Image
        GridBagConstraints gridConstraints = new GridBagConstraints();
        gridConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridConstraints.weighty = 0.0;
        gridConstraints.weightx = 0.0;
        gridConstraints.insets = new Insets(2, 2, 2, 2);

        GridBagLayout gridBag = new GridBagLayout();
        JPanel result = new JPanel(gridBag);
        result.setBorder(new TitledBorder("Result Image"));

        gridConstraints.gridy = 0;
        gridConstraints.gridx = 0;
        lbl = new JLabel("Type");
        gridBag.setConstraints(lbl, gridConstraints);
        result.add(lbl);

        gridConstraints.gridx = 1;
        gridConstraints.weightx = 1.0;
        cmbType.addItem("ORIGINAL");
        cmbType.setSelectedItem(ImageObject.types[ImageObject.TYPE_DOUBLE]);
        gridBag.setConstraints(cmbType, gridConstraints);
        result.add(cmbType);

        gridConstraints.gridy++;
        gridConstraints.gridx = 0;
        gridConstraints.weightx = 0.0;
        lbl = new JLabel("Multiplier");
        gridBag.setConstraints(lbl, gridConstraints);
        result.add(lbl);

        gridConstraints.gridx = 1;
        gridConstraints.weightx = 1.0;
        gridBag.setConstraints(txtMultiplier, gridConstraints);
        result.add(txtMultiplier);

        gridConstraints.gridy++;
        gridConstraints.gridx = 0;
        gridConstraints.weightx = 0.0;
        lbl = new JLabel("Clip result?");
        gridBag.setConstraints(lbl, gridConstraints);
        result.add(lbl);

        gridConstraints.gridx = 1;
        gridConstraints.weightx = 1.0;
        gridBag.setConstraints(chkClip, gridConstraints);
        result.add(chkClip);

        // buttons
        JPanel buttons = new JPanel(new FlowLayout());

        btnCalibrate = new JButton(new AbstractAction("Calibrate") {
            public void actionPerformed(ActionEvent e) {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                btnPreview.setEnabled(false);
                btnSave.setEnabled(false);
                btnApply.setEnabled(false);
                try {
                    calibrate();
                } catch (IOException exc) {
                    logger.error("Error loading image.", exc);
                } catch (ImageException exc) {
                    logger.error("Error calibrating image.", exc);
                }
                setCursor(Cursor.getDefaultCursor());
            }
        });
        buttons.add(btnCalibrate);

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
                dialog.setTitle("Save Result Callibration");
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

        // copy the result back to imagepanel
        JButton btn = new JButton(new AbstractAction("Help") {
            public void actionPerformed(ActionEvent e) {
            	HelpViewer.showHelp("Calibrate Image");
            }
        });
        buttons.add(btn);

        btn = new JButton(new AbstractAction("Close") {
            public void actionPerformed(ActionEvent e) {
                CalibrateImageDialog.this.setVisible(false);
            }
        });
        buttons.add(btn);

        // combine all the pieces
        this.getContentPane().add(sfImages, BorderLayout.NORTH);
        this.getContentPane().add(result, BorderLayout.CENTER);
        this.getContentPane().add(buttons, BorderLayout.SOUTH);

        pack();
    }

    /**
     * This function will load any images if needed and call the calibrate class
     * to calibrate this image with regard to the black and white images
     * loaded.
     */
    protected void calibrate() throws IOException, ImageException {
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        // find the multiplier to use on the calibrated image
        double mult = 1;
        try {
            mult = Double.parseDouble(txtMultiplier.getText());
        } catch (NumberFormatException exc) {
            logger.warn("Error parsing multiplier", exc);
            mult = 1;
        }

        // find the imagetype requested.
        String type = null;
        if (!cmbType.getSelectedItem().equals("ORIGINAL")) {
            type = cmbType.getSelectedItem().toString();
        }

        // create a new instace of the calibration code
        CalibrateImage calibrateimage = new CalibrateImage();

        // Set clipping
        calibrateimage.setClipEnabled(chkClip.isSelected());

        // load and set black/white image
        calibrateimage.setBlackImage(sfImages.getImageObject(BLACK, null));
        calibrateimage.setWhiteImage(sfImages.getImageObject(WHITE));

        // calibrate the image
        imgResult = calibrateimage.calibrate(sfImages.getImageObject(ORIGINAL), mult, type);

        // enable follow up buttons
        btnPreview.setEnabled(true);
        btnSave.setEnabled(true);
        btnApply.setEnabled(true);
    }

    /**
     * Hides the Dialog. This will also hide the preview and free all memory.
     */
    public void closing() {
        preview.setVisible(false);
        btnPreview.setEnabled(false);
        btnSave.setEnabled(false);
        btnApply.setEnabled(false);
        sfImages.reset(false);
        imgResult = null;
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
        JMenuItem calibrate = new JMenuItem(new AbstractAction("Calibrate Image") {
            public void actionPerformed(ActionEvent e) {
                setLocationRelativeTo(getOwner());
                setVisible(true);
            }
        });
        hs.add(calibrate);
        return new JMenuItem[]{tools};
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
