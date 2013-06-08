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
package edu.illinois.ncsa.isda.im2learn.main;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToggleButton;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.im2learn.core.display.ImagePanel;
import edu.illinois.ncsa.isda.im2learn.ext.camera.Camera;
import edu.illinois.ncsa.isda.im2learn.ext.camera.IndigoOmegaCamera;
import edu.illinois.ncsa.isda.im2learn.ext.camera.JMFCamera;
import edu.illinois.ncsa.isda.im2learn.ext.camera.SNCRZ30;
import edu.illinois.ncsa.isda.im2learn.ext.info.InfoDialog;

/**
 * Simple test application to check the cameras
 */
public class CameraMain extends JFrame {
    private final Vector     camera;
    private final ImagePanel ipCapture;
    private final JComboBox  cmbCamera;
    private Thread           thread;

    static private Log       logger = LogFactory.getLog(CameraMain.class);

    public CameraMain() {
        super("Camera Test Application");

        camera = new Vector();

        // ------------------------------------------------------------------
        // Add any new camera's to this list.
        try {
            camera.add(new IndigoOmegaCamera());
        } catch (IOException exc) {
            logger.info("No Indigo Omega camera.");
        }
        try {
            camera.add(new JMFCamera());
        } catch (IOException exc) {
            logger.info("No JMF camera.");
        }
        camera.add(new SNCRZ30("cyclops.ncsa.uiuc.edu"));
        // ------------------------------------------------------------------

        // set up the interface, big panel in middle for the image and the
        // buttons below it.
        ipCapture = new ImagePanel();
        ipCapture.setPreferredSize(new Dimension(320, 240));
        ipCapture.setAutozoom(true);
        ipCapture.addMenu(new InfoDialog());
        getContentPane().add(ipCapture, BorderLayout.CENTER);

        // buttons
        JPanel controls = new JPanel(new FlowLayout());
        getContentPane().add(controls, BorderLayout.SOUTH);

        JToggleButton btnCapture = new JToggleButton(new AbstractAction("Capture") {
            public void actionPerformed(ActionEvent e) {
                JToggleButton btn = (JToggleButton) e.getSource();
                if (btn.isSelected()) {
                    thread = new Thread(new Runnable() {
                        public void run() {
                            while (thread == Thread.currentThread()) {
                                try {
                                    Camera cam = (Camera) cmbCamera.getSelectedItem();
                                    ImageObject imgobj = cam.getFrame();
                                    ipCapture.setImageObject(imgobj);
                                } catch (IOException exc) {
                                    exc.printStackTrace();
                                }
                            }
                        }
                    });
                    thread.start();
                } else {
                    thread = null;
                }
            }
        });
        controls.add(btnCapture);

        JButton btn = new JButton(new AbstractAction("Options") {
            public void actionPerformed(ActionEvent e) {
                Camera cam = (Camera) cmbCamera.getSelectedItem();
                cam.showOptionsDialog();
            }
        });
        controls.add(btn);

        cmbCamera = new JComboBox(camera);
        controls.add(cmbCamera);

        btn = new JButton(new AbstractAction("Close") {
            public void actionPerformed(ActionEvent e) {
                for (Iterator iter = camera.iterator(); iter.hasNext();) {
                    Camera cam = (Camera) iter.next();
                    try {
                        cam.close();
                    } catch (IOException exc) {
                        logger.warn("Could not close " + cam);
                    }
                }

                System.exit(0);
            }
        });
        controls.add(btn);

        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    static public void main(String[] args) {
        CameraMain main = new CameraMain();
        main.setVisible(true);
    }
}
