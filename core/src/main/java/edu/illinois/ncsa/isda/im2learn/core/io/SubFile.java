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
package edu.illinois.ncsa.isda.im2learn.core.io;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.im2learn.core.datatype.SubArea;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Shows a dialog allowing the user to select a subarea of the image and
 * sampling to be used. The readers will always first use the subarea to select
 * a region from the image and then apply subsampling to that region. If the
 * user selects (0, 0) x (100, 100) for subarea (x, y, w, h) and subsampling of
 * 5, the final image will be (20 x 20) starting at pixel 0. If the user had
 * selected (9, 9) x (14, 14) the image returned would be (2, 2) starting at
 * pixle (9, 9).
 *
 * @author Rob Kooper
 * @version 2.0
 * @see ImageLoader
 */
public class SubFile extends JDialog {
    private JTextField txtRow;
    private JTextField txtCol;
    private JTextField txtBand;
    private JTextField txtHeight;
    private JTextField txtWidth;
    private JTextField txtBands;
    private JCheckBox chkUseSubArea;
    private JCheckBox chkLoadHeader;
    private JTextField txtSampling;
    private boolean cancelled = true;

    static private Log logger = LogFactory.getLog(SubFile.class);

    /**
     * Default constructor. Takes as argument the window that called it. Will
     * setup the interface with subarea off, and sampling of 1.
     *
     * @param owner the window that called this class.
     */
    public SubFile(Frame owner) {
        super(owner, "Load SubArea and SubSample", true);
        setResizable(false);

        // gridbag layout for selections
        GridBagConstraints gridConstraints = new GridBagConstraints();
        gridConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridConstraints.weightx = 0.0;
        gridConstraints.weighty = 0.0;
        gridConstraints.insets = new Insets(2, 2, 2, 2);

        GridBagLayout gridBag = new GridBagLayout();
        JPanel panel = new JPanel(gridBag);

        // add options
        gridConstraints.gridx = 0;
        gridConstraints.gridy = 0;
        gridConstraints.gridwidth = 4;
        gridConstraints.weightx = 1.0;
        chkLoadHeader = new JCheckBox("Load Header Only?", false);
        chkLoadHeader.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (chkLoadHeader.isSelected()) {
                    chkUseSubArea.setEnabled(false);
                    txtRow.setEnabled(false);
                    txtCol.setEnabled(false);
                    txtBand.setEnabled(false);
                    txtHeight.setEnabled(false);
                    txtWidth.setEnabled(false);
                    txtBands.setEnabled(false);
                    txtSampling.setEnabled(false);
                } else {
                    chkUseSubArea.setEnabled(true);
                    txtRow.setEnabled(chkUseSubArea.isSelected());
                    txtCol.setEnabled(chkUseSubArea.isSelected());
                    txtBand.setEnabled(chkUseSubArea.isSelected());
                    txtHeight.setEnabled(chkUseSubArea.isSelected());
                    txtWidth.setEnabled(chkUseSubArea.isSelected());
                    txtBands.setEnabled(chkUseSubArea.isSelected());
                    txtSampling.setEnabled(true);
                }
            }
        });
        gridBag.setConstraints(chkLoadHeader, gridConstraints);
        panel.add(chkLoadHeader);

        gridConstraints.gridx = 0;
        gridConstraints.gridy++;
        gridConstraints.gridwidth = 4;
        gridConstraints.weightx = 1.0;
        chkUseSubArea = new JCheckBox("Use SubArea?", false);
        chkUseSubArea.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (chkUseSubArea.isSelected()) {
                    txtRow.setEnabled(true);
                    txtCol.setEnabled(true);
                    txtBand.setEnabled(true);
                    txtHeight.setEnabled(true);
                    txtWidth.setEnabled(true);
                    txtBands.setEnabled(true);
                } else {
                    txtRow.setEnabled(false);
                    txtCol.setEnabled(false);
                    txtBand.setEnabled(false);
                    txtHeight.setEnabled(false);
                    txtWidth.setEnabled(false);
                    txtBands.setEnabled(false);
                }
            }
        });
        gridBag.setConstraints(chkUseSubArea, gridConstraints);
        panel.add(chkUseSubArea);

        gridConstraints.gridx = 0;
        gridConstraints.gridy++;
        gridConstraints.gridwidth = 1;
        gridConstraints.weightx = 0.0;
        JLabel lbl = new JLabel("Start Col/X:");
        gridBag.setConstraints(lbl, gridConstraints);
        panel.add(lbl);

        gridConstraints.gridx = 1;
        gridConstraints.weightx = 0.5;
        txtCol = new JTextField("0", 5);
        txtCol.setEnabled(false);
        gridBag.setConstraints(txtCol, gridConstraints);
        panel.add(txtCol);

        gridConstraints.gridx = 2;
        gridConstraints.weightx = 0.0;
        lbl = new JLabel("Number of Cols/Width:");
        gridBag.setConstraints(lbl, gridConstraints);
        panel.add(lbl);

        gridConstraints.gridx = 3;
        gridConstraints.weightx = 0.5;
        txtWidth = new JTextField("100", 5);
        txtWidth.setEnabled(false);
        gridBag.setConstraints(txtWidth, gridConstraints);
        panel.add(txtWidth);

        gridConstraints.gridx = 0;
        gridConstraints.gridy++;
        gridConstraints.weightx = 0.0;
        lbl = new JLabel("Start Row/Y:");
        gridBag.setConstraints(lbl, gridConstraints);
        panel.add(lbl);

        gridConstraints.gridx = 1;
        gridConstraints.weightx = 0.5;
        txtRow = new JTextField("0", 5);
        txtRow.setEnabled(false);
        gridBag.setConstraints(txtRow, gridConstraints);
        panel.add(txtRow);

        gridConstraints.gridx = 2;
        gridConstraints.weightx = 0.0;
        lbl = new JLabel("Number of Rows/Height:");
        gridBag.setConstraints(lbl, gridConstraints);
        panel.add(lbl);

        gridConstraints.gridx = 3;
        gridConstraints.weightx = 0.5;
        txtHeight = new JTextField("100", 5);
        txtHeight.setEnabled(false);
        gridBag.setConstraints(txtHeight, gridConstraints);
        panel.add(txtHeight);

        gridConstraints.gridx = 0;
        gridConstraints.gridy++;
        gridConstraints.weightx = 0.0;
        lbl = new JLabel("Start Band:");
        gridBag.setConstraints(lbl, gridConstraints);
        panel.add(lbl);

        gridConstraints.gridx = 1;
        gridConstraints.weightx = 0.5;
        txtBand = new JTextField("0", 5);
        txtBand.setEnabled(false);
        gridBag.setConstraints(txtBand, gridConstraints);
        panel.add(txtBand);

        gridConstraints.gridx = 2;
        gridConstraints.weightx = 0.0;
        lbl = new JLabel("Number of Bands:");
        gridBag.setConstraints(lbl, gridConstraints);
        panel.add(lbl);

        gridConstraints.gridx = 3;
        gridConstraints.weightx = 0.5;
        txtBands = new JTextField("0", 5);
        txtBands.setToolTipText("Leave this at 0 to select all bands.");
        txtBands.setEnabled(false);
        gridBag.setConstraints(txtBands, gridConstraints);
        panel.add(txtBands);

        gridConstraints.gridx = 0;
        gridConstraints.gridy++;
        gridConstraints.weightx = 0.0;
        lbl = new JLabel("Sampling:");
        gridBag.setConstraints(lbl, gridConstraints);
        panel.add(lbl);

        gridConstraints.gridx = 1;
        gridConstraints.weightx = 0.5;
        txtSampling = new JTextField("1", 5);
        gridBag.setConstraints(txtSampling, gridConstraints);
        panel.add(txtSampling);

        // buttons
        JPanel buttons = new JPanel(new FlowLayout());

        JButton btn = new JButton(new AbstractAction("OK") {
            public void actionPerformed(ActionEvent e) {
                cancelled = false;
                setVisible(false);
            }
        });
        buttons.add(btn);
        btn = new JButton(new AbstractAction("Cancel") {
            public void actionPerformed(ActionEvent e) {
                cancelled = true;
                setVisible(false);
            }
        });
        buttons.add(btn);

        // create the panel
        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(buttons, BorderLayout.SOUTH);
        pack();

        // center dialog on screen
        setLocationRelativeTo(null);
    }

    /**
     * Will be true if the user cancelled the dialog.
     *
     * @return true if cancelled.
     */
    public boolean isCancelled() {
        return cancelled;
    }

    public boolean isLoadHeader() {
        return chkLoadHeader.isSelected();
    }

    /**
     * Get the subarea. If the user checked subarea it will return the subarea
     * that the user provided. If unchecked it will return null.
     *
     * @return subarea selected by the user.
     */
    public SubArea getSubArea() {
        if (!chkUseSubArea.isSelected()) {
            return null;
        }

        int x = 0;
        try {
            x = Integer.parseInt(txtCol.getText());
        } catch (NumberFormatException exc) {
            logger.error("Invalid number, using 0 for col");
        }
        int y = 0;
        try {
            y = Integer.parseInt(txtRow.getText());
        } catch (NumberFormatException exc) {
            logger.error("Invalid number, using 0 for row");
        }
        int band = 0;
        try {
            band = Integer.parseInt(txtBand.getText());
        } catch (NumberFormatException exc) {
            logger.error("Invalid number, using 0 for band");
        }
        int w = 100;
        try {
            w = Integer.parseInt(txtWidth.getText());
        } catch (NumberFormatException exc) {
            logger.error("Invalid number, using 100 for width");
        }
        int h = 100;
        try {
            h = Integer.parseInt(txtHeight.getText());
        } catch (NumberFormatException exc) {
            logger.error("Invalid number, using 100 for height");
        }
        int bands = 0;
        try {
            bands = Integer.parseInt(txtBands.getText());
        } catch (NumberFormatException exc) {
            logger.error("Invalid number, using 0 for bands");
        }

        return new SubArea(x, y, band, w, h, bands);
    }

    /**
     * Get the subsampling ratio. This determines how many pixels are read. If
     * the value is 1 each pixel is read, if the value is x, each x-th pixel is
     * read.
     *
     * @return subsampling provided by the user.
     */
    public int getSampling() {
        try {
            return Integer.parseInt(txtSampling.getText());
        } catch (NumberFormatException exc) {
            logger.error("Invalid number, returning 1");
        }
        return 1;
    }
}
