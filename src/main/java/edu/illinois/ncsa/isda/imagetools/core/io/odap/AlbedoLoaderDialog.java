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
package edu.illinois.ncsa.isda.imagetools.core.io.odap;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.border.Border;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.imagetools.core.display.Im2LearnFrame;
import edu.illinois.ncsa.isda.imagetools.core.display.Im2LearnMenu;
import edu.illinois.ncsa.isda.imagetools.core.display.ImageComponent;
import edu.illinois.ncsa.isda.imagetools.core.display.ImagePanel;
import edu.illinois.ncsa.isda.imagetools.core.display.ImageUpdateEvent;
import edu.illinois.ncsa.isda.imagetools.core.io.FileChooser;


/**
 * Dialog to allow the user to load albedo images. This will show a simple
 * dialog allowing the user to load albedo images. The images are displayed in a
 * frame and the user can select an image and apply it to the main frame. The
 * mainframe will then operate on this single images.
 * 
 * @author kooper
 * 
 */
public class AlbedoLoaderDialog extends Im2LearnFrame implements Im2LearnMenu {
    private ImagePanel                imagepanel;
    private JPanel                    pnlImages;
    private ArrayList<ImageComponent> lstImages;
    private ImageComponent            icSelected;
    private JPanel                    pnlSelected;
    private Border                    brdNotSelect;
    private Border                    brdSelect;

    public AlbedoLoaderDialog() {
        icSelected = null;
        lstImages = new ArrayList<ImageComponent>();

        brdNotSelect = BorderFactory.createEmptyBorder(2, 2, 2, 2);
        brdSelect = BorderFactory.createLineBorder(Color.red, 2);

        creatUI();
        pack();
    }

    private void creatUI() {
        pnlImages = new JPanel(new FlowLayout());
        pnlImages.setPreferredSize(new Dimension(600, 200));
        add(new JScrollPane(pnlImages), BorderLayout.CENTER);

        JPanel panel = new JPanel(new FlowLayout());
        add(panel, BorderLayout.SOUTH);

        panel.add(new JButton(new AbstractAction("Load Albedo") {
            public void actionPerformed(ActionEvent e) {
                new Thread(new Runnable() {
                    public void run() {
                        FileChooser fc = new FileChooser();
                        try {
                            String filename = fc.showOpenDialog();
                            if (filename != null) {
                                loadAlbedo(filename);
                            }
                        } catch (Exception exc) {
                            exc.printStackTrace();
                        }
                    }
                }).start();
            }
        }));

        panel.add(new JButton(new AbstractAction("Apply") {
            public void actionPerformed(ActionEvent e) {
                if (icSelected != null) {
                    imagepanel.setImageObject(icSelected.getImageObject());
                }
            }
        }));

        panel.add(new JButton(new AbstractAction("Close") {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        }));
    }

    private void reset() {
        pnlImages.removeAll();
        lstImages.clear();
        icSelected = null;
        pnlSelected = null;
    }

    public void closing() {
        reset();
    }

    public void loadAlbedo(String filename) throws Exception {
        reset();

        ImageObject[] imgobj = AlbedoLoader.loadAlbedo(filename);

        for (int i = 0; i < imgobj.length; i++) {

            final JPanel pnlImage = new JPanel();
            final ImageComponent ic = new ImageComponent();
            pnlImage.add(ic);
            pnlImage.setBorder(brdNotSelect);
            pnlImages.add(pnlImage);

            ic.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (pnlSelected != null) {
                        pnlSelected.setBorder(brdNotSelect);
                    }
                    icSelected = ic;
                    pnlSelected = pnlImage;
                    pnlImage.setBorder(brdSelect);
                }
            });
            ic.setPreferredSize(new Dimension(180, 180));
            ic.setImageObject(imgobj[i]);
            ic.setAutozoom(true);
        }

        pnlImages.validate();
        pnlImages.repaint();
    }

    public void setImagePanel(ImagePanel imagepanel) {
        this.imagepanel = imagepanel;
    }

    public JMenuItem[] getPanelMenuItems() {
        return null;
    }

    public JMenuItem[] getMainMenuItems() {
        JMenu tool = new JMenu("File");
        tool.add(new JMenuItem(new AbstractAction("Load Albedo") {
            public void actionPerformed(ActionEvent e) {
                AlbedoLoaderDialog.this.setVisible(true);
            }
        }));
        return new JMenuItem[] { tool };
    }

    public void imageUpdated(ImageUpdateEvent event) {
    }

    public URL getHelp(String topic) {
        return null;
    }
}
