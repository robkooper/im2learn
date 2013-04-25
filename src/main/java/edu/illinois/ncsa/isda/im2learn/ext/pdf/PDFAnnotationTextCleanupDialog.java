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
package edu.illinois.ncsa.isda.im2learn.ext.pdf;

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

import edu.illinois.ncsa.isda.im2learn.core.Im2LearnUtilities;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.im2learn.core.datatype.PDFAnnotation;
import edu.illinois.ncsa.isda.im2learn.core.display.Im2LearnFrame;
import edu.illinois.ncsa.isda.im2learn.core.display.Im2LearnMenu;
import edu.illinois.ncsa.isda.im2learn.core.display.ImageAnnotation;
import edu.illinois.ncsa.isda.im2learn.core.display.ImagePanel;
import edu.illinois.ncsa.isda.im2learn.core.display.ImageUpdateEvent;
import edu.illinois.ncsa.isda.im2learn.ext.panel.ZoomDialog;

/**
 * 
 * <p>
 * This tool works much the same way as the image cleanup does, except that it's
 * designed for the text parts of the PDF files. The tool provides the user with
 * 4 options: Duplicate, Merge Lines, Merge Paragrap, and Classify. When merging
 * the same principle is applied as with images, when the bounding boxes are
 * closer to each other than what the determined Delta Values, they will merge
 * into one bounding box. In case of the Merge Paragraph whole paragraphs are
 * merged into one bounding box.
 * </p>
 * <p>
 * Bounding boxes:
 * </p>
 * <img src="help/textcleanup.jpg"></br></br>
 * <p>
 * Merge Lines performed:
 * </p>
 * <img src="help/textcleanup2.jpg"</br></br>
 * <p>
 * Merge Paragraphs performend:
 * </p>
 * <img src="help/textcleanup3.jpg"</br></br>
 * <p>
 * The tool provides the user with options (Checkboxes at the bottom) to display
 * only the bounding boxes that are interesting. Duplicates, Unclassified,
 * classified as Unit or Price can be blended out. The sliding bars that
 * determine the Delta are at the top of the main panel.</br> The Reset button
 * rids all the changes that have been made to the document and restores it's
 * original form, drawing all the bounding boxes over again. </br>The Duplicates
 * button cleans up the bounding boxes which occur due to artistic effects in
 * PDF files, for instance the shadow of some piece of text. Often this will be
 * stored in two images which overlap and create the impression of a shadow.
 * Using this button will get rid of these. </br>Merging Lines and Merging
 * paragraphs is tied to the Delta value that is set by the user. If the boxes
 * overlap less than the value that is set than they will be merged into one box
 * surrounding the both. </br>The classify button labels the texts and splits
 * them into Unit or Price. </br>Applying the changes will make them permanent
 * to the document.
 * </p>
 * <img src="help/textmerge.jpg">
 * <p>
 * The function of the 'Duplicate' button is to get rid of duplicate bounding
 * boxes that occur with texts. For instance there may be a second image that is
 * merely a shadow to the text. This will combine such bounding boxes into one.
 * </p>
 * <img src="help/duplicate.jpg">
 * <p>
 * 
 * @author Rob Kooper
 * @author Peter Bajcsy
 * @author (documentation) Peter Ferak
 * 
 * 
 * 
 */
public class PDFAnnotationTextCleanupDialog extends Im2LearnFrame implements Im2LearnMenu, ImageAnnotation {
    private ImagePanel               imagepanel;
    private PDFAnnotationTextCleanup cleaner;
    private Vector<PDFAnnotation>    annotations;

    private ImagePanel               ipPDF;
    private JSlider                  sldX;
    private JSlider                  sldY;
    private JCheckBox                chkDuplicate;
    private JButton                  btnReset;
    private JButton                  btnApply;
    private JButton                  btnDuplicate;
    private JButton                  btnMergeLine;
    private JButton                  btnMergePara;

    public PDFAnnotationTextCleanupDialog() {
        super("Ad Text Cleanup");

        cleaner = new PDFAnnotationTextCleanup();
        annotations = new Vector<PDFAnnotation>();

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

        btnDuplicate = new JButton(new AbstractAction("Duplicates") {
            public void actionPerformed(ActionEvent e) {
                cleaner.removeDuplicates();

                ipPDF.repaint();
                updateUI();
            }
        });
        setButtonPrefs(btnDuplicate);
        buttons.add(btnDuplicate);

        btnMergeLine = new JButton(new AbstractAction("Merge Line") {
            public void actionPerformed(ActionEvent e) {
                cleaner.mergeLine();

                ipPDF.repaint();
                updateUI();
            }
        });
        setButtonPrefs(btnMergeLine);
        buttons.add(btnMergeLine);

        btnMergePara = new JButton(new AbstractAction("Merge Para") {
            public void actionPerformed(ActionEvent e) {
                cleaner.mergePara();

                ipPDF.repaint();
                updateUI();
            }
        });
        setButtonPrefs(btnMergePara);
        buttons.add(btnMergePara);

        btnApply = new JButton(new AbstractAction("Apply") {
            public void actionPerformed(ActionEvent e) {
                ImageObject imgobj = imagepanel.getImageObject();
                if (imgobj == null) {
                    return;
                }
                imgobj.setProperty(PDFAnnotation.KEY, annotations);
                imagepanel.setImageObject(imgobj);
            }
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

        chkDuplicate = new JCheckBox("Duplicate (   0)", true);
        chkDuplicate.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ipPDF.repaint();
            }
        });
        pnl.add(chkDuplicate);

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
                this.annotations = (Vector<PDFAnnotation>) clone;
            }
            if (ipPDF.getImageObject() != imgobj) {
                ipPDF.setImageObject(imgobj);
            }
        } else {
            ipPDF.setImageObject(null);
            this.annotations.clear();
        }

        cleaner.reset(annotations);

        updateUI();
    }

    public void showing() {
        reset();
    }

    public void closing() {
        ipPDF.setImageObject(null);
        annotations.clear();

        cleaner.reset(annotations);
        updateUI();
    }

    public void updateUI() {
        int duplicate = 0;

        for (PDFAnnotation anno : annotations) {
            if (anno.isText()) {
                if (anno.isDuplicate()) {
                    duplicate++;
                }
            }
        }

        String spaces = "0000";
        String tmp = "" + duplicate;
        tmp = "Duplicate (" + spaces.substring(tmp.length()) + tmp + ")";
        chkDuplicate.setText(tmp);
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
            if (anno.isDuplicate()) {
                if (chkDuplicate.isSelected()) {
                    anno.drawBoundingBox(g);
                }
            } else if (anno.isText()) {
                anno.drawBoundingBox(g);
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

        JMenuItem menu = new JMenuItem("Text Cleanup");
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
        return getClass().getResource("help/textcleanup.html");
    }
}
