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

/**
 * Options for the SONY NC-RZ30. This allows the user to change the host and
 * port number of the camera.
 */
public class SNCRZ30Options extends JDialog {
    private SNCRZ30 camera = null;

    private JTextField tfHostname;
    private JTextField tfPort;
    private JTextField tfUsername;
    private JTextField tfPassword;

    private JComboBox cmbImageSize;

    private boolean update = false;

    static private Log logger = LogFactory.getLog(SNCRZ30Options.class);

    /**
     * Default constructor. Receives the camera whose options are to be
     * modified.
     *
     * @param camera the camera whose options to modify.
     */
    public SNCRZ30Options(SNCRZ30 camera) {
        super((Frame) null, "SNCRZ-30 Camera Options", false);
        setResizable(false);

        this.camera = camera;

        createUI();
        reset();
        pack();
    }

    /**
     * Create the UI. Pushing apply will apply the changes back to the camera.
     */
    private void createUI() {
        // hostname port

        JPanel panel = new JPanel(new GridBagLayout());
        getContentPane().add(panel, BorderLayout.CENTER);

        GridBagConstraints gc0 = new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0);
        GridBagConstraints gc1 = new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0);

        gc0.gridy = 0;
        gc1.gridy = gc0.gridy;
        panel.add(getServer(), gc0);

        gc0.gridy = 1;
        gc1.gridy = gc0.gridy;
        panel.add(getImage(), gc0);
    }

    private void reset() {
        update = true;

        // host setup
        tfHostname.setText(camera.getHostname());
        tfPort.setText(camera.getPort() + "");

        // image setup
        String[] sizes = camera.getImageSizes();
        DefaultComboBoxModel model = (DefaultComboBoxModel) cmbImageSize.getModel();
        model.removeAllElements();
        for (int i = 0; i < sizes.length; i++) {
            model.addElement(sizes[i]);
        }
        cmbImageSize.setSelectedItem(camera.getImageSize());

        update = false;
    }

    private JPanel getServer() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new TitledBorder("Server Selection"));

        GridBagConstraints gc0 = new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 2, 0, 0), 0, 0);
        GridBagConstraints gc1 = new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0);
        GridBagConstraints gc2 = new GridBagConstraints(2, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 2, 0, 0), 0, 0);

        gc0.gridy = 0;
        gc1.gridy = gc0.gridy;
        gc2.gridy = gc0.gridy;
        gc1.gridwidth = 1;
        panel.add(new JLabel("Host"), gc0);
        tfHostname = new JTextField(camera.getHostname(), 20);
        panel.add(tfHostname, gc1);
        tfPort = new JTextField("" + camera.getPort(), 5);
        panel.add(tfPort, gc2);

        gc0.gridy = 1;
        gc1.gridy = gc0.gridy;
        gc2.gridy = gc0.gridy;
        gc1.gridwidth = 2;
        panel.add(new JLabel("Username"), gc0);
        tfUsername = new JTextField(camera.getUsername(), 30);
        panel.add(tfUsername, gc1);

        gc0.gridy = 2;
        gc1.gridy = gc0.gridy;
        gc2.gridy = gc0.gridy;
        gc1.gridwidth = 2;
        panel.add(new JLabel("Password"), gc0);
        tfPassword = new JPasswordField(camera.getPassword(), 30);
        panel.add(tfPassword, gc1);

        gc0.gridy = 3;
        gc1.gridy = gc0.gridy;
        gc2.gridy = gc0.gridy;
        gc1.gridwidth = 1;
        JButton btn = new JButton(new AbstractAction("Connect") {
            public void actionPerformed(ActionEvent e) {
                if (update) return;
                camera.setHostname(tfHostname.getText());
                try {
                    camera.setPort(Integer.parseInt(tfPort.getText()));
                } catch (NumberFormatException exc) {
                    logger.warn("Invalid port number.");
                }
                camera.setUsername(tfUsername.getText());
                camera.setPassword(tfPassword.getText());
                reset();
            }
        });
        panel.add(btn, gc1);

        return panel;
    }

    private JPanel getImage() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new TitledBorder("Image Setup"));

        GridBagConstraints gc0 = new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 2, 0, 0), 0, 0);
        GridBagConstraints gc1 = new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0);
        GridBagConstraints gc2 = new GridBagConstraints(2, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 2, 0, 0), 0, 0);

        gc0.gridy = 0;
        gc1.gridy = gc0.gridy;
        gc2.gridy = gc0.gridy;
        gc1.gridwidth = 2;
        panel.add(new JLabel("Image Size"), gc0);
        cmbImageSize = new JComboBox();
        cmbImageSize.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (update) return;
                camera.setImageSize(cmbImageSize.getSelectedIndex());
                cmbImageSize.setSelectedItem(camera.getImageSize());
            }
        });
        panel.add(cmbImageSize, gc1);

        return panel;
    }
}
