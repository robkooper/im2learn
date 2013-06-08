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
package edu.illinois.ncsa.isda.im2learn.ext.camera;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 *
 */
public class JMFCameraOptions extends JDialog {
    private static Log logger = LogFactory.getLog(JMFCameraOptions.class);
    private JMFCamera camera;
    private JComboBox cmbVideo;
    private JComboBox cmbSize;
    private DefaultComboBoxModel mdlSize;

    public JMFCameraOptions(JMFCamera camera) {
        super((Frame) null, "JMF Control", true);
        setResizable(false);
        this.camera = camera;
        createGUI();
        pack();
    }

    public void show() {
        reset();
        super.show();
    }

    private void createGUI() {
        // add preview
        JPanel pnlPreview = new JPanel(new BorderLayout());
        pnlPreview.add(camera.getPreview(), BorderLayout.CENTER);
        pnlPreview.setBorder(new TitledBorder("Preview"));
        getContentPane().add(pnlPreview, BorderLayout.NORTH);

        // JMF select control
        Box controls = Box.createVerticalBox();
        getContentPane().add(controls, BorderLayout.CENTER);

        // select input
        JPanel device = new JPanel(new BorderLayout());
        device.setBorder(new TitledBorder("Device Options"));
        controls.add(device, BorderLayout.CENTER);

        //get all the capture devices
        cmbVideo = new JComboBox(camera.getVideoDevices());
        cmbVideo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String device = cmbVideo.getSelectedItem().toString();
                try {
                    camera.setVideoDevice(device);
                    mdlSize.removeAllElements();
                    Dimension[] dim = camera.getVideoSizes();
                    for (int i = 0; i < dim.length; i++) {
                        String tmp = dim[i].width + "x" + dim[i].height;
                        mdlSize.addElement(tmp);
                    }
                    cmbSize.setSelectedIndex(0);
                } catch (IOException exc) {
                    logger.error("Error setting device.", exc);
                }
            }
        });
        device.add(cmbVideo, BorderLayout.NORTH);

        // select size
        mdlSize = new DefaultComboBoxModel();
        cmbSize = new JComboBox(mdlSize);
        cmbSize.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (cmbSize.getSelectedIndex() == -1) {
                    return;
                }
                try {
                    String size = cmbSize.getSelectedItem().toString();
                    String[] xy = size.split("x");
                    int x = Integer.parseInt(xy[0]);
                    int y = Integer.parseInt(xy[1]);
                    camera.setVideoSize(new Dimension(x, y));
                } catch (Exception exc) {
                    logger.error("Error setting size.", exc);
                }
            }
        });
        device.add(cmbSize);

        controls.add(Box.createHorizontalGlue());

        JPanel buttons = new JPanel(new FlowLayout());
        JToggleButton btn = new JToggleButton(new AbstractAction("Live Preview") {
            public void actionPerformed(ActionEvent e) {
                JToggleButton btn = (JToggleButton) e.getSource();
                try {
                    camera.setEnabled(btn.isSelected());
                } catch (IOException exc) {
                    logger.error("Error setting live preview", exc);
                }
            }
        });
        buttons.add(btn);
        controls.add(buttons);
    }

    public void reset() {
        // fill in combo boxes
        cmbVideo.setSelectedIndex(0);
    }

}
