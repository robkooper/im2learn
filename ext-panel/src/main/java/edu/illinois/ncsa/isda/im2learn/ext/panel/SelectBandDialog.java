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
package edu.illinois.ncsa.isda.im2learn.ext.panel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageException;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.im2learn.core.display.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;

/**
 *
 *  <p>
      This dialog allows you to select which bands in the image are
      represented by red, green, blue and gray. The gray band is used if the
      image is shown as grayscale, otherwise the red, green and blue bands are
      used to draw the image in color.
    </p>

    <p>
      If the checkbox in front of red, green or blue is unchecked, the image
      will be drawn without that particular band. For instance unchecking both
      the green and blue checkbox will show the image with just the red band
      and makes it easy to see how much red there is in the image. Unlike the
      gray scale image the image will now be drawn with just the red band.
    </p>
    <p>
      Changing any of the options will modify the small preview image, giving
      you immediate feedback on the changes you made to the image.
    </p>
    <p align="center">
      <img src="SelectBandDialog-1.jpg">
      <br>
      Figure: Select Band Dialog.
    </p>

  
 * @author Rob Kooper
 * @version 2.0
 *
 *  Show a dialog that allows the user to change the bands used to display the
 * image. Some images have many bands, this dialog allows the user to select
 * which bands should be used for red, green and blue. The user can also select
 * wheter or not to display a single band of an image. Changing any of the
 * variables will immediatly update the preview image, pressing apply will
 * update the ncsa.im2learn.main image.
 *
 */
public class SelectBandDialog extends Im2LearnFrame implements Im2LearnMenu {
    private JCheckBox chkGrayScale;
    private JSlider sldRed;
    private JTextField txtRed;
    private JCheckBox chkRed;
    private JSlider sldGreen;
    private JTextField txtGreen;
    private JCheckBox chkGreen;
    private JSlider sldBlue;
    private JTextField txtBlue;
    private JCheckBox chkBlue;
    private JSlider sldGray;
    private JTextField txtGray;
    private ImagePanel imagepanel = null;
    private ImageComponent imgpreview;

    private static Log logger = LogFactory.getLog(SelectBandDialog.class);

    /**
     * Default constructor creates the UI.
     */
    public SelectBandDialog() {
        super("Select Bands");
        setResizable(false);

        createUI();
        pack();
    }

    public void showing() {
        imageUpdated(new ImageUpdateEvent(this, ImageUpdateEvent.NEW_IMAGE, null));
        toFront();
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

        // sliders
        GridBagConstraints gridConstraints = new GridBagConstraints();
        gridConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridConstraints.insets = new Insets(2, 2, 2, 2);
        gridConstraints.weighty = 0.0;

        GridBagLayout gridBag = new GridBagLayout();
        JPanel controls = new JPanel(gridBag);

        // slider control red
        gridConstraints.gridy = 0;
        gridConstraints.gridx = 0;
        chkRed = new JCheckBox("Red :");
        chkRed.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (chkRed.isSelected()) {
                    sldRed.setEnabled(true);
                    txtRed.setEnabled(true);
                    imgpreview.setRedBand(sldRed.getValue());
                } else {
                    sldRed.setEnabled(false);
                    txtRed.setEnabled(false);
                    imgpreview.setRedBand(-1);
                }
            }
        });
        gridBag.setConstraints(chkRed, gridConstraints);
        controls.add(chkRed);
        gridConstraints.gridx = 1;
        sldRed = new JSlider(0, 2, 0);
        sldRed.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                txtRed.setText("" + sldRed.getValue());
                imgpreview.setRedBand(sldRed.getValue());
            }
        });
        gridBag.setConstraints(sldRed, gridConstraints);
        controls.add(sldRed);
        gridConstraints.gridx = 2;
        txtRed = new JTextField("0", 3);
        txtRed.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    int red = Integer.parseInt(txtRed.getText());
                    if (red < 0) {
                        red = 0;
                    }
                    ImageObject imgobj = imagepanel.getImageObject();
                    if (imgobj == null) {
                        red = 0;
                    } else if (imgobj.getNumBands() <= red) {
                        red = imgobj.getNumBands() - 1;
                    }
                    sldRed.setValue(red);
                    imgpreview.setRedBand(red);
                } catch (NumberFormatException exc) {
                    logger.debug("Error parsing red", exc);
                    txtRed.setText(sldRed.getValue() + "");
                }
            }
        });
        gridBag.setConstraints(txtRed, gridConstraints);
        controls.add(txtRed);

        // slider control green
        gridConstraints.gridy++;
        gridConstraints.gridx = 0;
        chkGreen = new JCheckBox("Green :");
        chkGreen.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (chkGreen.isSelected()) {
                    sldGreen.setEnabled(true);
                    txtGreen.setEnabled(true);
                    imgpreview.setGreenBand(sldGreen.getValue());
                } else {
                    sldGreen.setEnabled(false);
                    txtGreen.setEnabled(false);
                    imgpreview.setGreenBand(-1);
                }
            }
        });
        gridBag.setConstraints(chkGreen, gridConstraints);
        controls.add(chkGreen);
        gridConstraints.gridx = 1;
        sldGreen = new JSlider(0, 2, 0);
        sldGreen.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                txtGreen.setText("" + sldGreen.getValue());
                imgpreview.setGreenBand(sldGreen.getValue());
            }
        });
        gridBag.setConstraints(sldGreen, gridConstraints);
        controls.add(sldGreen);
        gridConstraints.gridx = 2;
        txtGreen = new JTextField("0", 3);
        txtGreen.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    int green = Integer.parseInt(txtGreen.getText());
                    if (green < 0) {
                        green = 0;
                    }
                    ImageObject imgobj = imagepanel.getImageObject();
                    if (imgobj == null) {
                        green = 0;
                    } else if (imgobj.getNumBands() <= green) {
                        green = imgobj.getNumBands() - 1;
                    }
                    sldGreen.setValue(green);
                    imgpreview.setGreenBand(green);
                } catch (NumberFormatException exc) {
                    logger.debug("Error parsing green", exc);
                    txtGreen.setText(sldGreen.getValue() + "");
                }
            }
        });
        gridBag.setConstraints(txtGreen, gridConstraints);
        controls.add(txtGreen);

        // slider control blue
        gridConstraints.gridy++;
        gridConstraints.gridx = 0;
        chkBlue = new JCheckBox("Blue :");
        chkBlue.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (chkBlue.isSelected()) {
                    sldBlue.setEnabled(true);
                    txtBlue.setEnabled(true);
                    imgpreview.setBlueBand(sldBlue.getValue());
                } else {
                    sldBlue.setEnabled(false);
                    txtBlue.setEnabled(false);
                    imgpreview.setBlueBand(-1);
                }
            }
        });
        gridBag.setConstraints(chkBlue, gridConstraints);
        controls.add(chkBlue);
        gridConstraints.gridx = 1;
        sldBlue = new JSlider(0, 2, 0);
        sldBlue.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                txtBlue.setText("" + sldBlue.getValue());
                imgpreview.setBlueBand(sldBlue.getValue());
            }
        });
        gridBag.setConstraints(sldBlue, gridConstraints);
        controls.add(sldBlue);
        gridConstraints.gridx = 2;
        txtBlue = new JTextField("0", 3);
        txtBlue.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    int blue = Integer.parseInt(txtBlue.getText());
                    if (blue < 0) {
                        blue = 0;
                    }
                    ImageObject imgobj = imagepanel.getImageObject();
                    if (imgobj == null) {
                        blue = 0;
                    } else if (imgobj.getNumBands() <= blue) {
                        blue = imgobj.getNumBands() - 1;
                    }
                    sldBlue.setValue(blue);
                    imgpreview.setBlueBand(blue);
                } catch (NumberFormatException exc) {
                    logger.debug("Error parsing blue", exc);
                    txtBlue.setText(sldBlue.getValue() + "");
                }
            }
        });
        gridBag.setConstraints(txtBlue, gridConstraints);
        controls.add(txtBlue);

        // grayscale
        chkGrayScale = new JCheckBox("Use Grayscale?", false);
        chkGrayScale.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                imgpreview.setGrayScale(chkGrayScale.isSelected());
            }
        });
        gridConstraints.gridy++;
        gridConstraints.gridx = 0;
        gridConstraints.gridwidth = 3;
        gridBag.setConstraints(chkGrayScale, gridConstraints);
        controls.add(chkGrayScale);
        gridConstraints.gridwidth = 1;

        // slider control gray
        gridConstraints.gridy++;
        gridConstraints.gridx = 0;
        JLabel lbl = new JLabel("Gray :");
        gridBag.setConstraints(lbl, gridConstraints);
        controls.add(lbl);
        gridConstraints.gridx = 1;
        sldGray = new JSlider(0, 2, 0);
        sldGray.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                txtGray.setText("" + sldGray.getValue());
                imgpreview.setGrayBand(sldGray.getValue());
            }
        });
        gridBag.setConstraints(sldGray, gridConstraints);
        controls.add(sldGray);
        gridConstraints.gridx = 2;
        txtGray = new JTextField("0", 3);
        txtGray.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    int gray = Integer.parseInt(txtGray.getText());
                    if (gray < 0) {
                        gray = 0;
                    }
                    ImageObject imgobj = imagepanel.getImageObject();
                    if (imgobj == null) {
                        gray = 0;
                    } else if (imgobj.getNumBands() <= gray) {
                        gray = imgobj.getNumBands() - 1;
                    }
                    sldGray.setValue(gray);
                    imgpreview.setGrayBand(gray);
                } catch (NumberFormatException exc) {
                    logger.debug("Error parsing gray", exc);
                    txtGray.setText(sldGray.getValue() + "");
                }
            }
        });
        gridBag.setConstraints(txtGray, gridConstraints);
        controls.add(txtGray);

        // Buttons
        JPanel buttons = new JPanel(new FlowLayout());
        JButton btn = new JButton(new AbstractAction("Apply") {
            public void actionPerformed(ActionEvent e) {
                imagepanel.setRGBBand(imgpreview.getRedBand(), imgpreview.getGreenBand(), imgpreview.getBlueBand());
                imagepanel.setGrayBand(imgpreview.getGrayBand());
                imagepanel.setGrayScale(imgpreview.isGrayScale());
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
        btn = new JButton(new AbstractAction("Help") {
            public void actionPerformed(ActionEvent e) {
                HelpViewer.showHelp("Select Band");
            }
        });
        buttons.add(btn);

        // create the interface
        getContentPane().add(imgpreview, BorderLayout.NORTH);
        getContentPane().add(controls, BorderLayout.CENTER);
        getContentPane().add(buttons, BorderLayout.SOUTH);
    }

    // ------------------------------------------------------------------------
    // Im2LearnMenu implementation
    // ------------------------------------------------------------------------
    public void setImagePanel(ImagePanel imagepanel) {
        this.imagepanel = imagepanel;
    }

    public JMenuItem[] getPanelMenuItems() {
        JMenuItem item = new JMenuItem(new AbstractAction("Select Bands") {
            public void actionPerformed(ActionEvent e) {
                Window win = SwingUtilities.getWindowAncestor(imagepanel);
                setLocationRelativeTo(win);
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
                    imgpreview.setImageObject(imgobj);

                    sldRed.setMaximum(imgobj.getNumBands() - 1);
                    sldGreen.setMaximum(imgobj.getNumBands() - 1);
                    sldBlue.setMaximum(imgobj.getNumBands() - 1);
                    sldGray.setMaximum(imgobj.getNumBands() - 1);
                } else {
                    imgpreview.setImageObject(imgobj);
                    sldRed.setMaximum(0);
                    sldGreen.setMaximum(0);
                    sldBlue.setMaximum(0);
                    sldGray.setMaximum(0);
                }
                imgpreview.setImageObject(imgobj);
                if (imagepanel.getRedBand() == -1) {
                    chkRed.setSelected(false);
                    sldRed.setEnabled(false);
                    txtRed.setEnabled(false);
                } else {
                    chkRed.setSelected(true);
                    sldRed.setEnabled(true);
                    txtRed.setEnabled(true);
                    sldRed.setValue(imagepanel.getRedBand());
                }
                if (imagepanel.getGreenBand() == -1) {
                    chkGreen.setSelected(false);
                    sldGreen.setEnabled(false);
                    txtGreen.setEnabled(false);
                } else {
                    chkGreen.setSelected(true);
                    sldGreen.setEnabled(true);
                    txtGreen.setEnabled(true);
                    sldGreen.setValue(imagepanel.getGreenBand());
                }
                if (imagepanel.getBlueBand() == -1) {
                    chkBlue.setSelected(false);
                    sldBlue.setEnabled(false);
                    txtBlue.setEnabled(false);
                } else {
                    chkBlue.setSelected(true);
                    sldBlue.setEnabled(true);
                    txtBlue.setEnabled(true);
                    sldBlue.setValue(imagepanel.getBlueBand());
                }
                imgpreview.setRGBBand(imagepanel.getRedBand(),
                                      imagepanel.getGreenBand(),
                                      imagepanel.getBlueBand());
                sldGray.setValue(imagepanel.getGrayBand());
                chkGrayScale.setSelected(imagepanel.isGrayScale());
                imgpreview.setGrayScale(imagepanel.isGrayScale());
                break;

            case ImageUpdateEvent.CHANGE_GRAYSCALE:
                chkGrayScale.setSelected(imagepanel.isGrayScale());
                imgpreview.setGrayScale(imagepanel.isGrayScale());
                break;

            case ImageUpdateEvent.CHANGE_GRAYBAND:
                sldGray.setValue(imagepanel.getGrayBand());
                imgpreview.setGrayBand(imagepanel.getGrayBand());
                break;

            case ImageUpdateEvent.CHANGE_REDBAND:
                if (imagepanel.getRedBand() == -1) {
                    chkRed.setSelected(false);
                    sldRed.setEnabled(false);
                    txtRed.setEnabled(false);
                } else {
                    chkRed.setSelected(true);
                    sldRed.setEnabled(true);
                    txtRed.setEnabled(true);
                    sldRed.setValue(imagepanel.getRedBand());
                }
                imgpreview.setRedBand(imagepanel.getRedBand());
                break;

            case ImageUpdateEvent.CHANGE_GREENBAND:
                if (imagepanel.getGreenBand() == -1) {
                    chkGreen.setSelected(false);
                    sldGreen.setEnabled(false);
                    txtGreen.setEnabled(false);
                } else {
                    chkGreen.setSelected(true);
                    sldGreen.setEnabled(true);
                    txtGreen.setEnabled(true);
                    sldGreen.setValue(imagepanel.getGreenBand());
                }
                imgpreview.setGreenBand(imagepanel.getGreenBand());
                break;

            case ImageUpdateEvent.CHANGE_BLUEBAND:
                if (imagepanel.getBlueBand() == -1) {
                    chkBlue.setSelected(false);
                    sldBlue.setEnabled(false);
                    txtBlue.setEnabled(false);
                } else {
                    chkBlue.setSelected(true);
                    sldBlue.setEnabled(true);
                    txtBlue.setEnabled(true);
                }
                imgpreview.setBlueBand(imagepanel.getBlueBand());
                break;

            case ImageUpdateEvent.CHANGE_RGBBAND:
                if (imagepanel.getRedBand() == -1) {
                    chkRed.setSelected(false);
                    sldRed.setEnabled(false);
                    txtRed.setEnabled(false);
                } else {
                    chkRed.setSelected(true);
                    sldRed.setEnabled(true);
                    txtRed.setEnabled(true);
                    sldRed.setValue(imagepanel.getRedBand());
                }
                if (imagepanel.getGreenBand() == -1) {
                    chkGreen.setSelected(false);
                    sldGreen.setEnabled(false);
                    txtGreen.setEnabled(false);
                } else {
                    chkGreen.setSelected(true);
                    sldGreen.setEnabled(true);
                    txtGreen.setEnabled(true);
                    sldGreen.setValue(imagepanel.getGreenBand());
                }
                if (imagepanel.getBlueBand() == -1) {
                    chkBlue.setSelected(false);
                    sldBlue.setEnabled(false);
                    txtBlue.setEnabled(false);
                } else {
                    chkBlue.setSelected(true);
                    sldBlue.setEnabled(true);
                    txtBlue.setEnabled(true);
                    sldBlue.setValue(imagepanel.getBlueBand());
                }
                imgpreview.setRGBBand(imagepanel.getRedBand(),
                                      imagepanel.getGreenBand(),
                                      imagepanel.getBlueBand());
                break;

            case ImageUpdateEvent.CHANGE_GAMMA:
                imgpreview.setGamma(imagepanel.getGamma());
                break;
        }
    }

    public URL getHelp(String menu) {
    	if (menu.equals("Select Bands")) {
    		return getClass().getResource("help/selectband.html");
    	}
    	return null;
    }
}
