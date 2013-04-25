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
import edu.illinois.ncsa.isda.im2learn.core.display.Im2LearnFrame;
import edu.illinois.ncsa.isda.im2learn.core.display.Im2LearnMenu;
import edu.illinois.ncsa.isda.im2learn.core.display.ImagePanel;
import edu.illinois.ncsa.isda.im2learn.core.display.ImageUpdateEvent;
import edu.illinois.ncsa.isda.im2learn.core.io.FileChooser;
import edu.illinois.ncsa.isda.im2learn.core.io.ImageLoader;
import edu.illinois.ncsa.isda.im2learn.ext.misc.ImageFrame;
import edu.illinois.ncsa.isda.im2learn.ext.misc.JSelectFile;
import edu.illinois.ncsa.isda.im2learn.ext.panel.SelectBandDialog;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URL;

/**
 * 
 * 	<p>
	<B>The class IlluminateImage provides a tool for re-illuminating any reflectance image 
	with a white image acquired under a specific illumination. 
	 </B>
	 </p>

	 <p>
	 <b>Description:</b>
This class was developed to generate images under multiple illuminations from a reflectance image. The class was 
originally designed for operating on hyperspectral images and predicting hyperspectral appearance of scenes
under fluorescent light (indoor ceiling light), Oriel Xenon lamp (flat power spectrum in the visible spectral range),
and incandescent light (regular desktop lamp).

 The dialog for image re-illumination is shown below.
 <BR>
 <BR>
	 <img src="help/illuminateImageDialog.jpg">
<BR>
<BR>
First, the calibrated reflectance image, the black image (e.g., acquired with lens cap on) and the illumination 
images are selected using the file choosers invoked by clicking on the button "...". 
The images can be previewed using the adjacent buttons 
labeled as "View". The re-illumination formula is the reverse formula of the calibration equation (see Image Calibration tool).
After clicking the button "Illuminate", the following formula is applied:

<pre>
 re-illuminated pix[i, j] = calibrated pix[i,j]*( illumination pix[i,j] - black pix[i,j]) + black pix[i,j]
 </pre>

<BR>
If the black image is not specified then the black image values are assumed to be zero. 
The resulting image will have a data type according to the selection labeled as "Type". Th edata type can be byte, short, int, long,
float, double or the original input image data type.

Examples of calibrated, illumination and the result are shown below.

<BR>
<BR>
Calibrated input image: <BR>
	 <img src="help/illuminateImageDialog1.jpg">
<BR>
<BR>
Illumination input image: <BR>
	 <img src="help/illuminateImageDialog2.jpg">
<BR>
<BR>
Re-illuminated output image: <BR>
	 <img src="help/illuminateImageDialog3.jpg">
<BR>
<BR>

 * @author Rob Kooper
 * @author Peter Bajcsy
 * @see CalibrateImage
 * @see IlluminateImage
 */
public class IlluminateImageDialog extends Im2LearnFrame implements Im2LearnMenu {
    private ImageObject imgResult = null;
    private ImageFrame preview = null;
    private ImagePanel imagepanel = null;

    private JSelectFile sfImages;
    private JComboBox cmbType = new JComboBox(ImageObject.types);
    private JButton btnIlluminate = null;
    private JButton btnPreview = null;
    private JButton btnSave = null;
    private JButton btnApply = null;

    private final int ORIGINAL = 0;
    private final int BLACK = 1;
    private final int WHITE = 2;

    private static Log logger = LogFactory.getLog(IlluminateImageDialog.class);

    /**
     * Default constructor
     */
    public IlluminateImageDialog() {
        super("Illuminate Image");
        setResizable(false);
        createGUI();
    }

    /**
     * Create the GUI.
     */
    protected void createGUI() {
        JLabel lbl;

        // preview frame
        preview = new ImageFrame("Preview Illumination");
        preview.getImagePanel().addMenu(new SelectBandDialog());
        preview.setSize(640, 480);

        // Selection of files to use
        sfImages = new JSelectFile(new String[]{"Calibrated", "Black", "Illumination"});
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

        // buttons
        JPanel buttons = new JPanel(new FlowLayout());

        btnIlluminate = new JButton(new AbstractAction("Illuminate") {
            public void actionPerformed(ActionEvent e) {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                btnPreview.setEnabled(false);
                btnSave.setEnabled(false);
                btnApply.setEnabled(false);
                try {
                    illuminate();
                } catch (IOException exc) {
                    logger.error("Error loading image.", exc);
                } catch (ImageException exc) {
                    logger.error("Error illuminating image.", exc);
                }
                setCursor(Cursor.getDefaultCursor());
            }
        });
        buttons.add(btnIlluminate);

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
                IlluminateImageDialog.this.setVisible(false);
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
     * This function will load any images if needed and call the illuminate
     * class to illuminate this image with regard to the black and illumination
     * images loaded.
     */
    protected void illuminate() throws IOException, ImageException {
        // find the imagetype requested.
        String type = null;
        if (!cmbType.getSelectedItem().equals("ORIGINAL")) {
            type = cmbType.getSelectedItem().toString();
        }

        // create a new instace of the illumination code and give the black and
        // illumination image
        IlluminateImage illuminateImage = new IlluminateImage();

        illuminateImage.setBlackImage(sfImages.getImageObject(BLACK, null));
        illuminateImage.setIlluminationMap(sfImages.getImageObject(WHITE));

        // calibrate the image
        imgResult = illuminateImage.illuminate(sfImages.getImageObject(ORIGINAL), type);

        // enable rest of buttons
        btnApply.setEnabled(true);
        btnPreview.setEnabled(true);
        btnSave.setEnabled(true);
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
        JMenuItem illuminate = new JMenuItem(new AbstractAction("Illuminate Image") {
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
        return getClass().getResource("help/IlluminateImageDialog.html");
    }      
}
