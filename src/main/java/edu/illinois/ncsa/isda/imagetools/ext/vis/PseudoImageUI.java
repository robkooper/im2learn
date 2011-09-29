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
package edu.illinois.ncsa.isda.imagetools.ext.vis;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObjectByte;
import edu.illinois.ncsa.isda.imagetools.core.display.ColorBar;
import edu.illinois.ncsa.isda.imagetools.core.display.ImageComponent;
import edu.illinois.ncsa.isda.imagetools.core.display.ImagePanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Create a panel which contains a view of the image where each value in a band
 * is mapped to a value between red and blue. This view is often associated with
 * an heat image, where the red colors are hot and blue colors are cold.
 * <p/>
 * This display will by default adjust the range of the image such that the blue
 * value is associated with the lowest value found in all the bands of the
 * image, and the red value is associated with the highest value found in all
 * the bands of the image.
 *
 * @author Sang-Chul Lee
 * @author Rob Kooper
 * @version 2.0
 */
public class PseudoImageUI extends JPanel implements ActionListener {
    private ColorBar cb;
    private ImagePanel ip;
    private ImageObject imgOrig, imgPseudo;
    private byte[] arrPseudo;
    private JSlider sldBand;
    private JTextField txtCounter;
    private JCheckBox chkUseTotal;
    private JCheckBox chkUseAuto;
    private JTextField txtMinVal;
    private JTextField txtMaxVal;

    static private Log logger = LogFactory.getLog(PseudoImageUI.class);

    /**
     * Create the ui with imagepanel, slider for band, colorbar and input
     * fields.
     */
    public PseudoImageUI() {
        super(new BorderLayout());

        // colorbar and imagecomponent
        cb = new ColorBar();
        ip = new ImagePanel();
        ip.setAutozoom(true);

        // slider to control band and counter
        JPanel slider = new JPanel(new BorderLayout());

        sldBand = new JSlider(0, 0, 0);
        sldBand.setSnapToTicks(true);
        sldBand.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                int txt = Integer.parseInt(txtCounter.getText());
                int sld = sldBand.getValue();
                if (txt != sld) {
                    txtCounter.setText(sld + "");
                    updateImage();
                }
            }
        });
        slider.add(sldBand, BorderLayout.CENTER);

        txtCounter = new JTextField(4);
        txtCounter.setHorizontalAlignment(JTextField.RIGHT);
        txtCounter.setEditable(true);
        txtCounter.setText("0");
        txtCounter.addActionListener(this);
        slider.add(txtCounter, BorderLayout.EAST);

        // control the look of image
        JPanel misc = new JPanel(new FlowLayout());

        chkUseTotal = new JCheckBox("Use min/max of whole image?", true);
        chkUseTotal.addActionListener(this);
        misc.add(chkUseTotal);

        chkUseAuto = new JCheckBox("Automatically set min/max value?", true);
        chkUseAuto.addActionListener(this);
        misc.add(chkUseAuto);

        txtMinVal = new JTextField("0", 8);
        txtMinVal.addActionListener(this);
        txtMinVal.setEnabled(!chkUseAuto.isSelected());
        misc.add(new JLabel("min value :"));
        misc.add(txtMinVal);

        txtMaxVal = new JTextField("255", 8);
        txtMaxVal.addActionListener(this);
        txtMaxVal.setEnabled(!chkUseAuto.isSelected());
        misc.add(new JLabel("max value :"));
        misc.add(txtMaxVal);

        // create the UI
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(slider, BorderLayout.NORTH);
        panel.add(misc, BorderLayout.CENTER);

        add(new JScrollPane(ip), BorderLayout.CENTER);
        add(cb, BorderLayout.EAST);
        add(panel, BorderLayout.SOUTH);
    }

    /**
     * User input received, update the image.
     *
     * @param e what has changed and how.
     */
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == chkUseAuto) {
            chkUseTotal.setEnabled(chkUseAuto.isSelected());
            txtMinVal.setEnabled(!chkUseAuto.isSelected());
            txtMaxVal.setEnabled(!chkUseAuto.isSelected());
        } else if (e.getSource() == txtCounter) {
            try {
                int x = Integer.parseInt(txtCounter.getText());
                if ((x < sldBand.getMinimum()) || (x > sldBand.getMaximum())) {
                    logger.warn("Number is not a valid band number.");
                } else {
                    sldBand.setValue(x);
                }
            } catch (NumberFormatException exc) {
                logger.warn("Invalid number.", exc);
            }
        }
        updateImage();
    }

    /**
     * Change the imageobject that is displayed. Setting it to null will remove
     * all data.
     *
     * @param imgobj the imageobject to display with the pseudo colors.
     */
    public void setImageObject(ImageObject imgobj) {
        if (imgobj == null) {
            imgOrig = null;
            imgPseudo = null;
            arrPseudo = null;
            sldBand.setMaximum(0);
            txtCounter.setText("0");
        } else {
            sldBand.setValue(0);
            txtCounter.setText("0");
            sldBand.setMaximum(imgobj.getNumBands() - 1);
            imgOrig = imgobj;
            imgPseudo = new ImageObjectByte(imgobj.getNumRows(), imgobj.getNumCols(), 3);
            imgPseudo.setProperties(imgOrig.getProperties());
            arrPseudo = (byte[]) imgPseudo.getData();
            cb.setRange(imgobj.getMin(), imgobj.getMax());
            updateImage();
        }
        ip.setImageObject(imgPseudo);
    }
    
    /**
     * Returns the current image that is show in the imagepanel.
     * 
     * @return imageobject shown in imagepanel
     */
    public ImageObject getImageObject() {
        return imgOrig;
    }
    
    /**
     * Returns the imagepanel that holds the pseudocolor image. This allows the user to
     * add annotations, zoom etc.
     * 
     * @return the imagepanel that hold the pseudocolor image
     */
    public ImagePanel getImagePanel() {
        return ip;
    }

    /**
     * Parameters or image have changed, update the color bar and the image
     * associated.
     */
    private void updateImage() {
        if (imgOrig == null) {
            return;
        }

        int idx1 = sldBand.getValue();
        int idx2 = 0;
        int size = imgOrig.getSize();
        Color c;
        double min = -10;
        double max = 10;

        if (chkUseAuto.isSelected()) {
            if (chkUseTotal.isSelected()) {
                min = imgOrig.getMin();
                max = imgOrig.getMax();
            } else {
                min = imgOrig.getMin(idx1);
                max = imgOrig.getMax(idx1);
            }
        } else {
            try {
                min = Double.parseDouble(txtMinVal.getText());
                max = Double.parseDouble(txtMaxVal.getText());
            } catch (NumberFormatException exc) {
                logger.warn("Could not parse number.", exc);
            }
        }
        cb.setRange(min, max);

        while (idx1 < size) {
            c = cb.getColor(imgOrig.getDouble(idx1));
            arrPseudo[idx2++] = (byte) c.getRed();
            arrPseudo[idx2++] = (byte) c.getGreen();
            arrPseudo[idx2++] = (byte) c.getBlue();
            idx1 += imgOrig.getNumBands();
        }

        ip.makeImage();
        repaint();
    }
}
