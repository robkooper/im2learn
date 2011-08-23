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
package edu.illinois.ncsa.isda.imagetools.ext.pdf;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.illinois.ncsa.isda.imagetools.core.Im2LearnUtilities;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.imagetools.core.datatype.PDFAnnotation;
import edu.illinois.ncsa.isda.imagetools.core.display.Im2LearnFrame;
import edu.illinois.ncsa.isda.imagetools.core.display.Im2LearnMenu;
import edu.illinois.ncsa.isda.imagetools.core.display.ImageAnnotation;
import edu.illinois.ncsa.isda.imagetools.core.display.ImagePanel;
import edu.illinois.ncsa.isda.imagetools.core.display.ImageUpdateEvent;
import edu.illinois.ncsa.isda.imagetools.ext.panel.ZoomDialog;

/**
 * <p>
 * This tool is designed specifically for PDF files that contain advertisement.
 * The purpose is to clean up the bounding boxes drawn around all the objects
 * contained in the PDF file, which can be either text or picture elements.
 * There are three options that the user can utilize. The button 'Contained'
 * eliminates all bounding boxes that are inside of other, obviously, greater
 * bounding boxes. This occurs because in PDF files many images are put together
 * by more than one image. For instance to achieve an effect of having a shadow,
 * the image will have the item itself and then the shadow image. Other images
 * can be choped into more than one object, to the visual appearence this makes
 * no difference but inside the PDF file they are stored as 3 different images.
 * The 'Merge Choped' option is designed to rid these.
 * </p>
 * <p>
 * The GUI contains two sliding bars at the top which determine the parameters
 * for the action, buttons for different actions, and checkboxes which can blend
 * out the boxes that are not needed to be shown. Each of the categories of
 * boxes have a different color.
 * </p>
 * <p>
 * The button 'Merge Chopped' draws a yellow bounding box around boxes which are
 * chopped off by other elements. The button 'Merge Overlapped' combines the
 * bounding boxes that overlap each other by drawing a red bounding box around
 * them. In the cases of 'Merge Chopped' and 'Merge Overlapped' the Deltas that
 * is set by adjusting the sliding bars at the top of the panel are the values
 * that are used as parameters for the merging. In the case of overlapping all
 * boxes that are closer to each other than the Delta are merged. Hence, if the
 * Delta is very low almost all boxes will be merged, if it is high very few
 * boxes will be merged.
 * </p>
 * <p>
 * Example chopped:
 * </p>
 * <img src="help/imagecleanup.jpg"></br></br> <img
 * src="help/imagecleanup2.jpg"></br></br> <img src="help/chopped.jpg">
 * <p>
 * Example contained:
 * </p>
 * <img src="help/contained.jpg"></br></br> <img
 * src="help/contained2.jpg"></br></br> <img src="help/contained3.jpg">
 * 
 * @author Peter Bajcsy
 * @author Rob Kooper
 * @author (documentation) Peter Ferak
 * 
 */
public class PDFAnnotationImageCleanupDialog extends Im2LearnFrame implements Im2LearnMenu, ImageAnnotation {
    private ImagePanel                imagepanel;
    private PDFAnnotationImageCleanup cleaner;
    // private AdvClassify classify;
    private Vector                    annotations;

    private ImagePanel                ipPDF;
    private JSlider                   sldX;
    private JSlider                   sldY;
    private JCheckBox                 chkInvalid;
    private JCheckBox                 chkContained;
    private JCheckBox                 chkUnclassified;
    private JCheckBox                 chkOverlapped;
    private JCheckBox                 chkChopped;
    private JButton                   btnReset;
    private JButton                   btnApply;
    private JButton                   btnDimInvalid;
    private JButton                   btnContained;
    private JButton                   btnMergeChopped;
    private JButton                   btnMergeOverlapped;

    // private JButton btnClassify;

    public PDFAnnotationImageCleanupDialog() {
        super("Ad Image Cleanup");

        cleaner = new PDFAnnotationImageCleanup();
        // classify = new AdvClassify();
        annotations = new Vector();

        createUI();
    }

    private void createUI() {
        // -------------------------------------------------------------------
        // panel with preview of PDF in center of UI
        // -------------------------------------------------------------------
        ipPDF = new ImagePanel();
        // ipPDF.setPreferredSize(new Dimension(320, 240));
        ipPDF.setAutozoom(true);
        ipPDF.setSelectionAllowed(false);
        ipPDF.addMenu(new ZoomDialog());
        ipPDF.addAnnotationPanel(this);
        JScrollPane sp = new JScrollPane(ipPDF);
        sp.setPreferredSize(new Dimension(320, 240));
        getContentPane().add(sp, BorderLayout.CENTER);

        // -------------------------------------------------------------------
        // on left buttons that symbolize the workflow
        // -------------------------------------------------------------------
        Box buttons = Box.createVerticalBox();
        getContentPane().add(buttons, BorderLayout.WEST);

        buttons.add(Box.createVerticalGlue());

        btnReset = new JButton(new AbstractAction("Reset") {
            public void actionPerformed(ActionEvent e) {
                reset();
                ipPDF.repaint();
            }
        });
        setButtonPrefs(btnReset);
        buttons.add(btnReset);

        btnDimInvalid = new JButton(new AbstractAction("DimInvalid") {
            public void actionPerformed(ActionEvent e) {
                cleaner.removeInvalidImagesMinDimension(cleaner.getToleranceDX(), cleaner.getToleranceDY());

                ipPDF.repaint();
                updateUI();
            }
        });
        setButtonPrefs(btnDimInvalid);
        buttons.add(btnDimInvalid);

        btnContained = new JButton(new AbstractAction("Contained") {
            public void actionPerformed(ActionEvent e) {
                cleaner.removeContainedImages();

                ipPDF.repaint();
                updateUI();
            }
        });
        setButtonPrefs(btnContained);
        buttons.add(btnContained);

        btnMergeChopped = new JButton(new AbstractAction("Merge Chopped") {
            public void actionPerformed(ActionEvent e) {
                cleaner.mergeChoppedImages();

                ipPDF.repaint();
                updateUI();
            }
        });
        setButtonPrefs(btnMergeChopped);
        buttons.add(btnMergeChopped);

        btnMergeOverlapped = new JButton(new AbstractAction("Merge Overlapped") {
            public void actionPerformed(ActionEvent e) {
                cleaner.mergeOverlappedImages();

                ipPDF.repaint();
                updateUI();
            }
        });
        setButtonPrefs(btnMergeOverlapped);
        buttons.add(btnMergeOverlapped);
        /*
         * btnClassify = new JButton(new AbstractAction("Classify") { public
         * void actionPerformed(ActionEvent e) { classify.classifyPrice();
         * classify.classifyUnit();
         * 
         * ipPDF.repaint(); updateUI(); } }); setButtonPrefs(btnClassify);
         * buttons.add(btnClassify);
         */
        btnApply = new JButton(new AbstractAction("Apply") {
            public void actionPerformed(ActionEvent e) {
                ImageObject imgobj = imagepanel.getImageObject();
                if (imgobj == null) {
                    return;
                }
                imgobj.setProperty(PDFAnnotation.KEY, annotations);
                imagepanel.setImageObject(imgobj);
            }

            /*
             * public void actionPerformed(ActionEvent e) { ImageObject imgobj =
             * imagepanel.getImageObject(); if (imgobj == null) { return; }
             * imgobj.setProperty(PDFAnnotation.KEY, annotations);
             * imagepanel.repaint();
             * 
             * }
             */
        });
        setButtonPrefs(btnApply);
        buttons.add(btnApply);

        buttons.add(Box.createVerticalGlue());

        // -------------------------------------------------------------------
        // panel to hold sliders and buttons
        // -------------------------------------------------------------------
        GridBagConstraints gridConstraints = new GridBagConstraints();
        gridConstraints.fill = GridBagConstraints.BOTH;
        gridConstraints.insets = new Insets(2, 2, 2, 2);
        gridConstraints.weighty = 0.0;
        gridConstraints.gridwidth = 1;

        GridBagLayout gridBag = new GridBagLayout();
        JPanel pnl = new JPanel(gridBag);
        getContentPane().add(pnl, BorderLayout.NORTH);

        // slider delta X
        gridConstraints.gridy = 0;
        gridConstraints.gridx = 0;
        gridConstraints.weightx = 0;
        gridConstraints.gridwidth = 1;
        JLabel lbl = new JLabel("Delta X :");
        gridBag.setConstraints(lbl, gridConstraints);
        pnl.add(lbl);

        gridConstraints.gridx = 1;
        gridConstraints.weightx = 1;
        gridConstraints.gridwidth = 3;
        sldX = new JSlider(0, 100, 0);
        sldX.setPaintTicks(true);
        gridBag.setConstraints(sldX, gridConstraints);
        pnl.add(sldX);

        gridConstraints.gridx = 4;
        gridConstraints.weightx = 0;
        gridConstraints.gridwidth = 1;
        final JLabel txtX = new JLabel("  0", JLabel.RIGHT);
        sldX.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                txtX.setText(sldX.getValue() + "");
                cleaner.setTolerance(sldX.getValue(), sldY.getValue());
                ipPDF.repaint();
            }
        });
        gridBag.setConstraints(txtX, gridConstraints);
        pnl.add(txtX);

        // slider delta Y
        gridConstraints.gridy++;
        gridConstraints.gridx = 0;
        gridConstraints.weightx = 0;
        gridConstraints.gridwidth = 1;
        lbl = new JLabel("Delta Y :");
        gridBag.setConstraints(lbl, gridConstraints);
        pnl.add(lbl);

        gridConstraints.gridx = 1;
        gridConstraints.weightx = 1;
        gridConstraints.gridwidth = 3;
        sldY = new JSlider(0, 100, 0);
        sldY.setPaintTicks(true);
        gridBag.setConstraints(sldY, gridConstraints);
        pnl.add(sldY);

        gridConstraints.gridx = 4;
        gridConstraints.weightx = 0;
        gridConstraints.gridwidth = 1;
        final JLabel txtY = new JLabel("  0", JLabel.RIGHT);
        sldY.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                txtY.setText(sldY.getValue() + "");
                cleaner.setTolerance(sldX.getValue(), sldY.getValue());
                ipPDF.repaint();
            }
        });
        gridBag.setConstraints(txtY, gridConstraints);
        pnl.add(txtY);

        // show boxes around deleted text
        pnl = new JPanel(new FlowLayout());
        getContentPane().add(pnl, BorderLayout.SOUTH);

        chkOverlapped = new JCheckBox("Overlapped (   0)", true);
        chkOverlapped.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ipPDF.repaint();
            }
        });
        pnl.add(chkOverlapped);

        chkInvalid = new JCheckBox("Invalid (   0)", true);
        chkInvalid.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ipPDF.repaint();
            }
        });
        pnl.add(chkInvalid);

        chkContained = new JCheckBox("Contained (   0)", true);
        chkContained.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ipPDF.repaint();
            }
        });
        pnl.add(chkContained);

        chkChopped = new JCheckBox("Chopped (   0)", true);
        chkChopped.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ipPDF.repaint();
            }
        });
        pnl.add(chkChopped);

        chkUnclassified = new JCheckBox("Unclassified (   0)", true);
        chkUnclassified.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ipPDF.repaint();
            }
        });
        pnl.add(chkUnclassified);

        // set the default values
        sldX.setValue(10);
        sldY.setValue(10);

        updateUI();
        pack();
    }

    private void setButtonPrefs(JButton button) {
        Dimension btnsize = new Dimension(100, 30);

        button.setAlignmentX(JButton.CENTER_ALIGNMENT);
        button.setAlignmentY(JButton.CENTER_ALIGNMENT);
        button.setMinimumSize(btnsize);
        button.setPreferredSize(btnsize);
        button.setMaximumSize(btnsize);
    }

    private void reset() {
        // get the imageobject
        ImageObject imgobj = imagepanel.getImageObject();

        // get the annotatations
        if ((imgobj != null) && (imgobj.getProperty(PDFAnnotation.KEY) != null)) {
            Object clone = Im2LearnUtilities.deepclone(imgobj.getProperty(PDFAnnotation.KEY));
            if (clone == null) {
                this.annotations.clear();
            } else {
                this.annotations = (Vector) clone;
            }
            if (ipPDF.getImageObject() != imgobj) {
                ipPDF.setImageObject(imgobj);
            }
        } else {
            ipPDF.setImageObject(null);
            this.annotations.clear();
        }

        cleaner.reset(annotations);
        // classify.reset(annotations);

        updateUI();
    }

    public void showing() {
        reset();
    }

    public void closing() {
        ipPDF.setImageObject(null);
        annotations.clear();

        cleaner.reset(annotations);
        // classify.reset(annotations);
        updateUI();
    }

    public void updateUI() {
        int duplicate = 0;
        int unclassified = 0;
        int Chopped = 0;
        int contained = 0;
        int invalid = 0;
        int Overlapped = 0;

        for (Iterator iter = annotations.iterator(); iter.hasNext();) {
            PDFAnnotation anno = (PDFAnnotation) iter.next();

            if (anno.isImage()) {
                if (anno.isDuplicate()) {
                    duplicate++;
                }
                switch (anno.getClassification()) {
                case PDFAnnotation.IMG_CONTAINED:
                    contained++;
                    break;
                case PDFAnnotation.DIM_INVALID:
                    invalid++;
                    break;
                case PDFAnnotation.IMG_CHOPPED:
                    Chopped++;
                    break;
                case PDFAnnotation.IMG_OVERLAPPED:
                    Overlapped++;
                    break;
                default:
                    unclassified++;

                }
            }
        }

        String spaces = "0000";
        String tmp = "" + Overlapped;
        tmp = "Overlapped (" + spaces.substring(tmp.length()) + tmp + ")";
        chkOverlapped.setText(tmp);

        tmp = "" + unclassified;
        tmp = "Unclassified (" + spaces.substring(tmp.length()) + tmp + ")";
        chkUnclassified.setText(tmp);

        tmp = "" + Chopped;
        tmp = "Chopped (" + spaces.substring(tmp.length()) + tmp + ")";
        chkChopped.setText(tmp);
        // test
        // System.out.println("TEST: Chopped = "+Chopped+", tmp="+tmp);

        tmp = "" + contained;
        tmp = "Contained (" + spaces.substring(tmp.length()) + tmp + ")";
        chkContained.setText(tmp);

        tmp = "" + invalid;
        tmp = "Invalid (" + spaces.substring(tmp.length()) + tmp + ")";
        chkInvalid.setText(tmp);
        // test
        System.out.println("TEST: Contained = " + contained + ", tmp=" + tmp);
        System.out.println("TEST: duplicate = " + duplicate);

    }

    // ------------------------------------------------------------------------
    // ImageAnnotation implementation
    // ------------------------------------------------------------------------
    public void paint(Graphics2D g, ImagePanel imagepanel) {
        // save the old graphics color
        Color old = g.getColor();

        // draw red boxes around all removed text locations
        for (Iterator iter = annotations.iterator(); iter.hasNext();) {
            PDFAnnotation anno = (PDFAnnotation) iter.next();
            if (anno.isImage()) {
                switch (anno.getClassification()) {
                case PDFAnnotation.IMG_CHOPPED:
                    if (chkChopped.isSelected()) {
                        anno.drawBoundingBox(g);
                    }
                    break;
                case PDFAnnotation.IMG_CONTAINED:
                    if (chkContained.isSelected()) {
                        anno.drawBoundingBox(g);
                    }
                    break;
                case PDFAnnotation.DIM_INVALID:
                    if (chkInvalid.isSelected()) {
                        anno.drawBoundingBox(g);
                    }
                    break;
                case PDFAnnotation.IMG_OVERLAPPED:
                    if (chkOverlapped.isSelected()) {
                        anno.drawBoundingBox(g);
                    }
                    break;
                default:
                    if (chkUnclassified.isSelected()) {
                        anno.drawBoundingBox(g);
                    }
                }
            }
        }

        // set graphics color back to original
        g.setColor(old);
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
        JMenu tools = new JMenu("Tools");
        JMenu adv = new JMenu("PDF");
        tools.add(adv);

        JMenuItem menu = new JMenuItem("Image Cleanup");
        menu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(true);
            }
        });
        adv.add(menu);

        return new JMenuItem[] { tools };
    }

    public void imageUpdated(ImageUpdateEvent event) {
        if (event.getId() == ImageUpdateEvent.NEW_IMAGE) {
            if (isVisible()) {
                showing();
            }
        }
    }

    public URL getHelp(String menu) {
        return getClass().getResource("help/imagecleanup.html");
    }
}
