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

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.im2learn.core.display.Im2LearnFrame;
import edu.illinois.ncsa.isda.im2learn.core.display.Im2LearnMenu;
import edu.illinois.ncsa.isda.im2learn.core.display.ImagePanel;
import edu.illinois.ncsa.isda.im2learn.core.display.ImageUpdateEvent;

/*
 * Show a dialog that allows the user to scale the image. Pressing Apply will change
 * the scale of the ncsa.im2learn.main imagepanel.
 *
 * @author Rob Kooper
 * @version 2.0
 */
public class ScaleDialog extends Im2LearnFrame implements Im2LearnMenu {
    private JSlider    sldX;
    private JTextField txtX;
    private JSlider    sldY;
    private JTextField txtY;
    private ImagePanel imagepanel;

    private static Log logger = LogFactory.getLog(ScaleDialog.class);

    /**
     * Default contructor, creates the UI.
     * 
     * @param owner
     *            frame this dialog belongs to.
     */
    public ScaleDialog() {
        super("Select Scale");
        setResizable(false);

        createUI();
        pack();
    }

    @Override
    public void showing() {
        imageUpdated(new ImageUpdateEvent(this, ImageUpdateEvent.NEW_IMAGE, null));
    }

    @Override
    public void closing() {
    }

    /**
     * Construct UI, with preview, slider and buttons.
     */
    private void createUI() {
        // slider control
        JPanel adjustX = new JPanel(new BorderLayout());
        adjustX.add(new JLabel("X :"), BorderLayout.WEST);
        sldX = new JSlider(0, 500, 100);
        sldX.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                setScaleX(sldX.getValue() / 100.0);
            }
        });
        adjustX.add(sldX, BorderLayout.CENTER);
        txtX = new JTextField("1.0", 5);
        txtX.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    double scale = Double.parseDouble(txtX.getText());
                    setScaleX(scale);
                } catch (NumberFormatException exc) {
                    logger.debug("Error parsing scale", exc);
                    setScaleX(sldX.getValue() / 100.0);
                }
            }
        });
        adjustX.add(txtX, BorderLayout.EAST);

        JPanel adjustY = new JPanel(new BorderLayout());
        adjustY.add(new JLabel("Y :"), BorderLayout.WEST);
        sldY = new JSlider(0, 500, 100);
        sldY.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                setScaleY(sldY.getValue() / 100.0);
            }
        });
        adjustY.add(sldY, BorderLayout.CENTER);
        txtY = new JTextField("1.0", 5);
        txtY.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    double scale = Double.parseDouble(txtY.getText());
                    setScaleY(scale);
                } catch (NumberFormatException exc) {
                    logger.debug("Error parsing scale", exc);
                    setScaleY(sldY.getValue() / 100.0);
                }
            }
        });
        adjustY.add(txtY, BorderLayout.EAST);

        JPanel adjust = new JPanel(new BorderLayout());
        adjust.add(adjustX, BorderLayout.NORTH);
        adjust.add(adjustY, BorderLayout.SOUTH);

        // Buttons
        JPanel buttons = new JPanel(new FlowLayout());
        JButton btn = new JButton(new AbstractAction("Apply") {
            public void actionPerformed(ActionEvent e) {
                double scalex = 1.0;
                try {
                    scalex = Double.parseDouble(txtX.getText());
                } catch (NumberFormatException exc) {
                    logger.debug("Error parsing scale", exc);
                    scalex = 1.0;
                }
                double scaley = 1.0;
                try {
                    scaley = Double.parseDouble(txtY.getText());
                } catch (NumberFormatException exc) {
                    logger.debug("Error parsing scale", exc);
                    scaley = 1.0;
                }

                try {
                    imagepanel.setImageObject(imagepanel.getImageObject().scale(scalex, scaley));
                } catch (Exception exc) {
                    logger.warn("Error scaling image", exc);
                }
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
        getContentPane().add(adjust, BorderLayout.CENTER);
        getContentPane().add(buttons, BorderLayout.SOUTH);
    }

    private void setScaleX(double scaleX) {
        if (scaleX <= 0) {
            scaleX = 0.01;
        }
        txtX.setText(scaleX + "");
        sldX.setValue((int) (scaleX * 100));
    }

    private void setScaleY(double scaleY) {
        if (scaleY <= 0) {
            scaleY = 0.01;
        }
        txtY.setText(scaleY + "");
        sldY.setValue((int) (scaleY * 100));
    }

    // ------------------------------------------------------------------------
    // Im2LearnMenu implementation
    // ------------------------------------------------------------------------
    public void setImagePanel(ImagePanel imagepanel) {
        this.imagepanel = imagepanel;
    }

    public JMenuItem[] getPanelMenuItems() {
        JMenuItem item = new JMenuItem(new AbstractAction("Scale Image") {
            public void actionPerformed(ActionEvent e) {
                setVisible(true);
            }
        });
        return new JMenuItem[] { item };
    }

    public JMenuItem[] getMainMenuItems() {
        JMenuItem item = new JMenuItem(new AbstractAction("Scale Image") {
            public void actionPerformed(ActionEvent e) {
                setVisible(true);
            }
        });
        JMenu tools = new JMenu("Tools");
        tools.add(item);
        return new JMenuItem[] { tools };
    }

    public void imageUpdated(ImageUpdateEvent event) {
        if (!isVisible()) {
            return;
        }
        switch (event.getId()) {
        case ImageUpdateEvent.NEW_IMAGE:
            setScaleX(1.0);
            setScaleY(1.0);
            break;
        }
    }

    public URL getHelp(String menu) {
        if (menu.equals("Scale Adjust")) {
            return getClass().getResource("help/scale.html");
        }
        return null;
    }
}
