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
package edu.illinois.ncsa.isda.im2learn.ext.misc;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.im2learn.core.display.ImagePanel;
import edu.illinois.ncsa.isda.im2learn.core.io.FileChooser;
import edu.illinois.ncsa.isda.im2learn.core.io.ImageLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

/**
 * Create a dialog that shows a panel with a label, textfield and button to
 * select files. The Panel will be smart enough to either provide the user with
 * a filename, or an image. If the image is selected it will only be loaded if
 * necessary. The image will be cached for future calls.
 *
 * @author Rob Kooper
 * @version 1.0
 */
public class JSelectFile extends JPanel {
    private JLabel[] lblLabels;
    private JTextField[] txtFilenames;
    private ImageObject[] imgImages;
    private ImageFrame[] imgFrame;
    private String[] strFilter;
    private boolean loaddialog = true;

    private static Log logger = LogFactory.getLog(JSelectFile.class);

    public JSelectFile(int files) throws IllegalArgumentException {
        super();

        if (files < 1) {
            throw(new IllegalArgumentException("Need at least 1 file."));
        }

        GridBagConstraints gridConstraints = new GridBagConstraints();
        gridConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridConstraints.insets = new Insets(2, 2, 2, 2);
        gridConstraints.weighty = 0.0;

        GridBagLayout gridBag = new GridBagLayout();
        setLayout(gridBag);

        lblLabels = new JLabel[files];
        txtFilenames = new JTextField[files];
        imgImages = new ImageObject[files];
        imgFrame = new ImageFrame[files];
        strFilter = new String[files];
        JButton btn;

        for (int i = 0; i < files; i++) {
            FileAction fileaction = new FileAction(i);

            gridConstraints.gridx = 0;
            gridConstraints.gridy = i;
            gridConstraints.weightx = 0;
            lblLabels[i] = new JLabel("Filename " + i);
            gridBag.setConstraints(lblLabels[i], gridConstraints);
            add(lblLabels[i]);

            gridConstraints.gridx = 1;
            gridConstraints.weightx = 1;
            txtFilenames[i] = new JTextField(20);
            txtFilenames[i].addKeyListener(fileaction);
            gridBag.setConstraints(txtFilenames[i], gridConstraints);
            add(txtFilenames[i]);


            gridConstraints.gridx = 2;
            gridConstraints.weightx = 0;
            btn = new JButton("...");
            btn.addActionListener(fileaction);
            gridBag.setConstraints(btn, gridConstraints);
            add(btn);

            gridConstraints.gridx = 3;
            gridConstraints.weightx = 0;
            btn = new JButton("view");
            btn.addActionListener(fileaction);
            gridBag.setConstraints(btn, gridConstraints);
            add(btn);
        }

        addComponentListener(new ComponentAdapter() {
            public void componentHidden(ComponentEvent e) {
                reset(false);
            }
        });
    }

    public JSelectFile(String[] labels) throws IllegalArgumentException {
        this(labels.length);

        for (int i = 0; i < labels.length; i++) {
            lblLabels[i].setText(labels[i]);
        }
    }

    public void reset(boolean clearfilenames) {
        for (int i = 0; i < lblLabels.length; i++) {
            if (clearfilenames) {
                txtFilenames[i].setText("");
            }
            imgImages[i] = null;
            if (imgFrame[i] != null) {
                imgFrame[i].setVisible(false);
                imgFrame[i].setImageObject(null);
            }
        }
    }

    public boolean isLoadDialog() {
        return loaddialog;
    }

    public void setLoadDialog(boolean loaddialog) {
        this.loaddialog = loaddialog;
    }

    public void setToolTipText(int idx, String tip) throws IllegalArgumentException {
        if ((idx < 0) || (idx > txtFilenames.length)) {
            throw(new IllegalArgumentException("Index out of bounds."));
        }

        txtFilenames[idx].setToolTipText(tip);
    }

    public void setLabel(int idx, String label) throws IllegalArgumentException {
        if ((idx < 0) || (idx > txtFilenames.length)) {
            throw(new IllegalArgumentException("Index out of bounds."));
        }

        lblLabels[idx].setText(label);
    }

    public String getLabel(int idx) throws IllegalArgumentException {
        if ((idx < 0) || (idx > txtFilenames.length)) {
            throw(new IllegalArgumentException("Index out of bounds."));
        }

        return lblLabels[idx].getText();
    }

    public String getFilename(int idx) throws IllegalArgumentException {
        if ((idx < 0) || (idx > txtFilenames.length)) {
            throw(new IllegalArgumentException("Index out of bounds."));
        }

        return txtFilenames[idx].getText();
    }

    public void setFilename(int idx, String filename) throws IllegalArgumentException {
        if ((idx < 0) || (idx > txtFilenames.length)) {
            throw(new IllegalArgumentException("Index out of bounds."));
        }

        txtFilenames[idx].setText(filename);
        imgImages[idx] = null;
    }

    public ImageObject getImageObject(int idx, ImageObject def) throws IOException, IllegalArgumentException {
        if ((idx < 0) || (idx > txtFilenames.length)) {
            throw(new IllegalArgumentException("Index out of bounds."));
        }

        if (imgImages[idx] != null) {
            return imgImages[idx];
        }

        String filename = txtFilenames[idx].getText();
        if (filename.equals("")) {
            return def;
        }

        imgImages[idx] = ImageLoader.readImage(filename, strFilter[idx]);
        return imgImages[idx];
    }

    public ImageObject getImageObject(int idx) throws IOException, IllegalArgumentException {
        if ((idx < 0) || (idx > txtFilenames.length)) {
            throw(new IllegalArgumentException("Index out of bounds."));
        }

        if (imgImages[idx] != null) {
            return imgImages[idx];
        }

        String filename = txtFilenames[idx].getText();
        if (filename.equals("")) {
            throw(new IOException("No filename specified."));
        }

        imgImages[idx] = ImageLoader.readImage(filename, strFilter[idx]);
        return imgImages[idx];
    }

    public ImagePanel getImagePanel(int idx) throws IllegalArgumentException {
        if ((idx < 0) || (idx > txtFilenames.length)) {
            throw(new IllegalArgumentException("Index out of bounds."));
        }

        if (imgFrame[idx] == null) {
            imgFrame[idx] = new ImageFrame("preview");
        }
        return imgFrame[idx].getImagePanel();
    }

    class FileAction implements ActionListener, KeyListener {
        private int idx = 0;

        public FileAction(int idx) {
            this.idx = idx;
        }

        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equals("view")) {
                try {
                    if (imgFrame[idx] == null) {
                        imgFrame[idx] = new ImageFrame("preview");
                    }
                    imgFrame[idx].setImageObject(getImageObject(idx));
                    imgFrame[idx].setVisible(true);
                } catch (IOException exc) {
                    logger.error("Error displaying file", exc);
                }
            } else if (e.getActionCommand().equals("...")) {
                FileChooser dialog = new FileChooser();
                try {
                    String filename;

                    // set the dialog based on the label
                    dialog.setTitle(lblLabels[idx].getText());

                    // set the filename to the current file
                    filename = getFilename(idx);
                    if (!filename.equals("")) {
                        dialog.setSelectedFile(filename);
                    }

                    // show the dialog, null means cancel.
                    if (loaddialog) {
                        filename = dialog.showOpenDialog();
                    } else {
                        filename = dialog.showSaveDialog();
                    }
                    if (filename != null) {
                        txtFilenames[idx].setText(filename);
                        strFilter[idx] = dialog.getFilter();
                        imgImages[idx] = null;
                    }
                } catch (IOException exc) {
                    logger.error("Error selecting file", exc);
                }
            }
        }

        public void keyPressed(KeyEvent e) {
        }

        public void keyReleased(KeyEvent e) {
        }

        public void keyTyped(KeyEvent e) {
            imgImages[idx] = null;
        }
    }

}
