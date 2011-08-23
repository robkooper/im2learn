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
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.imagetools.core.Im2LearnUtilities;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageException;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.imagetools.core.datatype.PDFAnnotation;
import edu.illinois.ncsa.isda.imagetools.core.datatype.SubArea;
import edu.illinois.ncsa.isda.imagetools.core.display.Im2LearnFrame;
import edu.illinois.ncsa.isda.imagetools.core.display.Im2LearnMenu;
import edu.illinois.ncsa.isda.imagetools.core.display.ImageAnnotation;
import edu.illinois.ncsa.isda.imagetools.core.display.ImagePanel;
import edu.illinois.ncsa.isda.imagetools.core.display.ImageUpdateEvent;
import edu.illinois.ncsa.isda.imagetools.ext.panel.ZoomDialog;

/**
 * 
 * <p>
 * This tool is designed for the user to determine which text and image parts of
 * the PDF file belong together. This is done by drawing a rectangle around the
 * elements that belong together. The boxes can be then applied to the document
 * and the PDF Ad Size tool will determine what class ads they are (A, B, C).
 * </p>
 * <p>
 * Groups: The tool has three checkboxes at the bottom which allow the user to
 * let the undesired bounding boxes disappear. There are three different types
 * of boxes: Groups, Group Annotations, and Unused Annotations. </br>The groups
 * are the bounding boxes that have been drawn by the user to specify a certain
 * group and are already classified as a group (clicking the 'New Group'
 * button). These boxes can be applied to the document and then used to
 * calculate the size of the ad. </br>The Group Annotations are all bounding
 * boxes that are contained in the currently selected group. They are drawn in
 * the color green. </br>The Unused Annotations are all bounding boxes that have
 * not been selected, these appear in blue. When the tool is first opened all
 * boxes will be blue, also if the user decides to reset the document they will
 * all appear.
 * </p>
 * The tool has three buttons: Reset, New Group, and Apply.</br> Reset changes
 * deletes any boxes that have been applied and restores the original contents
 * of the document. </br>New Group classifies the group that is selected and
 * which appears in the little preview panel below the buttons as one ad.
 * </br>Apply transfers the bounding boxes that have been determined to be ads
 * into the main document and they can be used by the Ad Size tool to calculate
 * the size of the ad. </p> <img src="help/group.jpg">
 * <p>
 * Example:
 * </p>
 * <img src="help/pdfgroup1.jpg"></br></br> <img src="help/pdfgroup2.jpg">
 * 
 * <p>
 * The elements inside the drawn rectangle are combined to one group which can
 * then be classified with the PDF Ad Size tool.
 * </p>
 * 
 * 
 * @author Rob Kooper
 * @author Peter Bajcsy
 * @author (documentation) Peter Ferak
 * 
 */
public class PDFAnnotationGroupDialog extends Im2LearnFrame implements Im2LearnMenu, ImageAnnotation {
    private ImagePanel                 imagepanel;

    private Vector<PDFAnnotationGroup> groups;
    private Vector<PDFAnnotation>      annotations;
    private boolean                    hasannotations;
    private PDFAnnotationGroup         current = null;

    private ImagePanel                 ip;
    private JButton                    btnReset;
    private JButton                    btnApply;
    private JButton                    btnNewGroup;
    private JButton                    btnRemoveGroup;

    private JCheckBox                  chkShowGroups;
    private JCheckBox                  chkShowGroupAnnotations;
    private JCheckBox                  chkShowUnusedAnnotations;

    private ImagePanel                 ipGroup;

    static private Log                 logger  = LogFactory.getLog(PDFAnnotationGroupDialog.class);

    public PDFAnnotationGroupDialog() {
        super("PDF Annotation Groups");

        hasannotations = false;
        annotations = new Vector<PDFAnnotation>();
        groups = new Vector<PDFAnnotationGroup>();

        createUI();
    }

    public void closing() {
        ip.setImageObject(null);
        ipGroup.setImageObject(null);
    }

    public void showing() {
        reset();
        updateUI();
        ip.repaint();
    }

    private void createUI() {
        // -------------------------------------------------------------------
        // panel with preview of PDF in center of UI
        // -------------------------------------------------------------------
        ip = new ImagePanel();
        // ipPDF.setPreferredSize(new Dimension(320, 240));
        ip.setAutozoom(true);
        ip.addMenu(new ZoomDialog());
        ip.addAnnotationPanel(this);
        JScrollPane sp = new JScrollPane(ip);
        sp.setPreferredSize(new Dimension(320, 240));
        getContentPane().add(sp, BorderLayout.CENTER);

        JMenuItem select = new JMenuItem(new AbstractAction("Select Group") {
            public void actionPerformed(ActionEvent e) {
                clickedSelect(ip.getImageLocationClicked());
                updateUI();
                ip.repaint();
            }
        });
        ip.addMenu(select);

        ip.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    clickedAdd(ip.getMouseLocation(e.getPoint()));
                    updateUI();
                    ip.repaint();
                } else if (e.getButton() == MouseEvent.BUTTON2) {
                    clickedRemove(ip.getMouseLocation(e.getPoint()));
                    updateUI();
                    ip.repaint();
                }
            }
        });
        ip.addImageUpdateListener(this);

        // -------------------------------------------------------------------
        // on left buttons that symbolize the workflow
        // -------------------------------------------------------------------
        Box buttons = Box.createVerticalBox();
        getContentPane().add(buttons, BorderLayout.WEST);

        buttons.add(Box.createVerticalGlue());

        btnReset = new JButton(new AbstractAction("Reset") {
            public void actionPerformed(ActionEvent e) {
                reset();
                updateUI();
                ip.repaint();
            }
        });
        setButtonPrefs(btnReset);
        buttons.add(btnReset);

        btnNewGroup = new JButton(new AbstractAction("New Group") {
            public void actionPerformed(ActionEvent e) {
                current = null;
                updateUI();
                ip.repaint();
            }
        });
        setButtonPrefs(btnNewGroup);
        buttons.add(btnNewGroup);

        btnRemoveGroup = new JButton(new AbstractAction("Remove Group") {
            public void actionPerformed(ActionEvent e) {
                removeGroup();
                updateUI();
                ip.repaint();
            }
        });
        setButtonPrefs(btnRemoveGroup);
        buttons.add(btnRemoveGroup);

        btnApply = new JButton(new AbstractAction("Apply") {
            public void actionPerformed(ActionEvent e) {
                ImageObject imgobj = imagepanel.getImageObject();
                if (imgobj == null) {
                    return;
                }
                imgobj.setProperty(PDFAnnotation.KEY, annotations);
                imgobj.setProperty(PDFAnnotationGroup.KEY, groups);
                imgobj.setProperty("HasAnnotations", new Boolean(hasannotations));
                imagepanel.repaint();
            }
        });
        setButtonPrefs(btnApply);
        buttons.add(btnApply);

        // for debug/fun
        ipGroup = new ImagePanel();
        ipGroup.setAutozoom(true);
        ipGroup.setCrop(new Rectangle(0, 0, 0, 0));
        buttons.add(ipGroup);

        buttons.add(Box.createVerticalGlue());

        // -------------------------------------------------------------------
        // on bottom set of checkboxes to control groups / annotations
        // -------------------------------------------------------------------
        JPanel pnl = new JPanel(new FlowLayout());
        getContentPane().add(pnl, BorderLayout.SOUTH);

        chkShowGroups = new JCheckBox("Groups (   0)", true);
        chkShowGroups.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ip.repaint();
            }
        });
        pnl.add(chkShowGroups);

        chkShowGroupAnnotations = new JCheckBox("Group Annotations (   0)", true);
        chkShowGroupAnnotations.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ip.repaint();
            }
        });
        pnl.add(chkShowGroupAnnotations);

        chkShowUnusedAnnotations = new JCheckBox("Unused Annotations (   0)", true);
        chkShowUnusedAnnotations.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ip.repaint();
            }
        });
        pnl.add(chkShowUnusedAnnotations);

        pack();
    }

    private void setButtonPrefs(AbstractButton button) {
        Dimension btnsize = new Dimension(150, 30);

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
        if (imgobj != null) {
            Object clone = Im2LearnUtilities.deepclone(imgobj.getProperty(PDFAnnotation.KEY));
            if (clone == null) {
                hasannotations = false;
                this.annotations.clear();
            } else {
                hasannotations = true;
                this.annotations = (Vector<PDFAnnotation>) clone;
            }

            clone = Im2LearnUtilities.deepclone(imgobj.getProperty(PDFAnnotationGroup.KEY));
            if (clone == null) {
                this.groups.clear();
            } else {
                this.groups = (Vector<PDFAnnotationGroup>) clone;
            }

            Boolean tmpbool = (Boolean) imgobj.getProperty("HasAnnotations");
            if (tmpbool != null) {
                hasannotations = tmpbool.booleanValue();
            }

            if (ip.getImageObject() != imgobj) {
                ip.setImageObject(imgobj);
                ipGroup.setImageObject(imgobj);
                ipGroup.setCrop(new Rectangle(0, 0, 0, 0));
            }
        } else {
            ip.setImageObject(null);
            hasannotations = false;
            this.annotations.clear();
            this.groups.clear();
        }

        current = null;
    }

    private void updateUI() {
        String spaces = "0000";
        String tmp = "" + groups.size();
        tmp = "Groups (" + spaces.substring(tmp.length()) + tmp + ")";
        chkShowGroups.setText(tmp);

        if (current == null) {
            tmp = "0";
        } else {
            tmp = "" + current.get().size();
        }
        tmp = "Group Annotations (" + spaces.substring(tmp.length()) + tmp + ")";
        chkShowGroupAnnotations.setText(tmp);

        int count = 0;
        for (PDFAnnotation anno : annotations) {
            if (!anno.isPartOfGroup() && !anno.isDuplicate()) {
                count++;
            }
        }
        tmp = "" + count;
        tmp = "Unused Annotations (" + spaces.substring(tmp.length()) + tmp + ")";
        chkShowUnusedAnnotations.setText(tmp);

        if ((current == null) || (current.getBoundingBox() == null)) {
            ipGroup.setCrop(new Rectangle(0, 0, 0, 0));
            btnRemoveGroup.setEnabled(false);
        } else {
            ipGroup.setCrop(current.getBoundingBox().getBounds());
            btnRemoveGroup.setEnabled(true);
        }
    }

    private void clickedAdd(Point pt) {
        for (PDFAnnotation anno : annotations) {
            if (anno.getBoundingBox().contains(pt)) {
                addAnnotation(anno);
            }
        }
    }

    private void clickedRemove(Point pt) {
        for (Iterator iter = annotations.iterator(); iter.hasNext();) {
            PDFAnnotation anno = (PDFAnnotation) iter.next();
            if (anno.getBoundingBox().contains(pt)) {
                removeAnnotation(anno);
                if (!hasannotations) {
                    iter.remove();
                }
            }
        }
    }

    private void clickedSelect(Point pt) {
        current = null;
        for (PDFAnnotationGroup group : groups) {
            if (group.getBoundingBox().contains(pt)) {
                current = group;
            }
        }
    }

    private void removeGroup() {
        if (current == null) {
            return;
        }
        for (Iterator iter = annotations.iterator(); iter.hasNext() && current != null;) {
            PDFAnnotation anno = (PDFAnnotation) iter.next();
            if (current.contains(anno)) {
                removeAnnotation(anno);
                if (!hasannotations) {
                    iter.remove();
                }
            }
        }
    }

    private void addSelection(Rectangle selection) {
        if (selection == null) {
            return;
        }

        if (hasannotations) {
            for (PDFAnnotation anno : annotations) {
                if (selection.contains(anno.getBoundingBox())) {
                    addAnnotation(anno);
                }
            }
        } else {
            ImageObject imgobj = ip.getImageObject();
            if (imgobj != null) {
                try {
                    ImageObject crop = imgobj.crop(new SubArea(selection));
                    PDFAnnotation anno = new PDFAnnotation(selection, crop);
                    annotations.add(anno);
                    addAnnotation(anno);
                } catch (ImageException exc) {
                    logger.warn("Could not crop image.", exc);
                }
            }
        }

        ip.setSelection(null);
    }

    private void addAnnotation(PDFAnnotation anno) {
        if ((anno == null) || anno.isDuplicate() || anno.isPartOfGroup()) {
            return;
        }

        if (current == null) {
            current = new PDFAnnotationGroup();
            groups.add(current);
        }

        current.add(anno);
        anno.setPartOfGroup(true);
    }

    private void removeAnnotation(PDFAnnotation anno) {
        if ((anno == null) || anno.isDuplicate() || (current == null) || !current.contains(anno)) {
            return;
        }

        current.remove(anno);
        anno.setPartOfGroup(false);

        if (current.get().isEmpty()) {
            groups.remove(current);
            current = null;
        }
    }

    // ------------------------------------------------------------------------
    // ImageAnnotation implementation
    // ------------------------------------------------------------------------
    public void paint(Graphics2D g, ImagePanel imagepanel) {
        // save the old graphics color
        Color old = g.getColor();

        // draw blue boxes around all unused annotations
        if (chkShowUnusedAnnotations.isSelected()) {
            for (PDFAnnotation anno : annotations) {
                if (!anno.isPartOfGroup() && !anno.isDuplicate()) {
                    anno.drawBoundingBox(g, Color.blue);
                }
            }
        }

        // draw green boxes around those annotations that are part of a group
        if (chkShowGroupAnnotations.isSelected() && current != null) {
            for (PDFAnnotation anno : current.get()) {
                anno.drawBoundingBox(g, Color.green);
            }

        }

        // draw black boxes around all groups
        if (chkShowGroups.isSelected()) {
            for (PDFAnnotationGroup group : groups) {
                group.drawBoundingBox(g, Color.black);
            }
        }

        // draw a red box around the current group
        if (current != null) {
            current.drawBoundingBox(g, Color.red);
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

        JMenuItem menu = new JMenuItem(new AbstractAction("Annotation Groups") {
            public void actionPerformed(ActionEvent e) {
                setVisible(true);
                toFront();
            }
        });
        adv.add(menu);

        return new JMenuItem[] { tools };
    }

    public void imageUpdated(ImageUpdateEvent event) {
        if (!isVisible()) {
            return;
        }

        switch (event.getId()) {
        case ImageUpdateEvent.NEW_IMAGE:
            if (event.getSource() == imagepanel) {
                showing();
            }
            break;

        case ImageUpdateEvent.CHANGE_SELECTION:
            if ((event.getSource() == ip) && (ip.getSelection() != null)) {
                if (current != null) {
                    current = null;
                }
                addSelection(ip.getSelection());
                updateUI();
                ip.repaint();
            }
            break;
        }
    }

    public URL getHelp(String menu) {
        return getClass().getResource("help/pdfgroup.html");
    }
}
