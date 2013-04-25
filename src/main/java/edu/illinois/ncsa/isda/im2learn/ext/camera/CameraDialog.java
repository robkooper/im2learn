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

import edu.illinois.ncsa.isda.im2learn.core.display.*;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.ArrayList;

/**
 * Create the menu that allows the user to capture images from the camera. This
 * will add a menu to the File Menu allowing the user to select a camera, set
 * options on that camera, and grab frames from that camera.
 *
 * Currently only JMF, Indigo Omega and the Sony SNRC-30 are supported.
 *
 * @author Rob Kooper
 * @version 1.0
 */
public class CameraDialog implements Im2LearnMenu {
    private ArrayList<CameraOption> cameras;

    private JMenu mnuCamera;

    static private Log logger = LogFactory.getLog(CameraDialog.class);
    private ImagePanel imagepanel;

    private ButtonGroup groupCameras;

    public CameraDialog() {
        mnuCamera = new JMenu("Camera");
        cameras = new ArrayList<CameraOption>();

        JMenuItem mnuitem = new JMenuItem(new AbstractAction("Capture") {
            public void actionPerformed(ActionEvent e) {
                Camera camera = getSelectedCamera();
                if (camera == null) {
                    return;
                }
                Frame frame = (Frame) SwingUtilities.getWindowAncestor(imagepanel);
                ProgressBlocker pb = new ProgressBlocker(frame, "Reading image from camera.");
                camera.addProgressListener(pb);
                pb.showDialog(new ImageCapture(camera));
                camera.removeProgressListener(pb);
            }
        });
        mnuCamera.add(mnuitem);
        mnuitem = new JMenuItem(new AbstractAction("Options") {
            public void actionPerformed(ActionEvent e) {
                Camera camera = getSelectedCamera();
                if (camera != null) {
                    camera.showOptionsDialog();
                }
            }
        });
        mnuCamera.add(mnuitem);

        mnuCamera.addSeparator();

        groupCameras = new ButtonGroup();
        initialize();
    }
    
    private void initialize() {
        new Thread() {
            public void run() {
              try {
                    CameraOption camopt = new CameraOption(new JMFCamera());
                    cameras.add(camopt);
                    mnuCamera.add(camopt.getMenuItem());
                    groupCameras.add(camopt.getMenuItem());
                } catch (Throwable thr) {
                    logger.info("No JMF camera found, or no libraries installed.");
                }
            }
        }.start();

        new Thread() {
            public void run() {
                try {
                    CameraOption camopt = new CameraOption(
                            new IndigoOmegaCamera());
                    cameras.add(camopt);
                    mnuCamera.add(camopt.getMenuItem());
                    groupCameras.add(camopt.getMenuItem());
                } catch (Throwable thr) {
                    String jhome = System.getProperty("java.home");
                    String jext = System.getProperty("java.ext.dirs");
                    String msg = "Could not find Indigo Omega, make sure any"
                            + " libraries are installed in " + jhome
                            + "/bin, jar" + " files in " + jext
                            + " and properties in " + jext + "/.. .";
                    logger.info(msg);
                }
            }
        }.start();

        new Thread() {
            public void run() {
                try {
                    CameraOption camopt = new CameraOption(new SNCRZ30());
                    cameras.add(camopt);
                    mnuCamera.add(camopt.getMenuItem());
                    groupCameras.add(camopt.getMenuItem());
                } catch (Throwable thr) {
                    logger.info("Could not add Sony Camera.");
                }                
            }
        }.start();
    }
    

    private Camera getSelectedCamera() {
        for(int i=0; i<cameras.size(); i++) {
            CameraOption camopt = (CameraOption)cameras.get(i);
            if (camopt.isSelected()) {
                return camopt.getCamera();
            }
        }
        return null;
    }

    // ------------------------------------------------------------------------
    // Simple class to hold camera info
    // ------------------------------------------------------------------------
    class CameraOption {
        private Camera camera;
        private JRadioButtonMenuItem menuitem;

        public CameraOption(Camera camera) {
            this.camera = camera;
            this.menuitem = new JRadioButtonMenuItem(camera.getName());
        }

        public boolean isSelected() {
            return menuitem.isSelected();
        }

        public JRadioButtonMenuItem getMenuItem() {
            return menuitem;
        }

        public Camera getCamera() {
            return camera;
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
        JMenu menu = new JMenu("File");
        menu.add(mnuCamera);
        return new JMenuItem[]{menu};
    }

    public void imageUpdated(ImageUpdateEvent event) {
    }

    // ------------------------------------------------------------------------
    // image capture class
    // ------------------------------------------------------------------------
    class ImageCapture implements ProgressBlockerRun {
        private Camera camera;

        public ImageCapture(Camera camera) {
            this.camera = camera;
        }

        public void run(ProgressBlocker blocker) throws Exception {
            imagepanel.setImageObject(camera.getFrame());
        }
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
