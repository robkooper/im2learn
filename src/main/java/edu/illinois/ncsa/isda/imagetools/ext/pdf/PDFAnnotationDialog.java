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


import javax.swing.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.imagetools.core.display.*;
import edu.illinois.ncsa.isda.imagetools.core.io.pdf.PDFAnnotation;
import edu.illinois.ncsa.isda.imagetools.core.io.pdf.PDFLoaderJPedal;
import edu.illinois.ncsa.isda.imagetools.core.io.pdf.PDFLoaderPDFBox;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;
import java.util.Iterator;
import java.util.Vector;

/**
 * 
    <p>
    This tool displays the objects of a PDF file depending on where the user clicks 
    on the main panel, the coordinates are stored and the picture is 'pinned' through all the objects that are at that location. If more elements overlap each other at the point that is clicked 
    all will be displayed. If there is more than one image all will be displayed in new tabs. If there is text and picture this will also be displayed.
    </p>
    <img src="help/clicked.jpg">
    <p>
    Example multiple images: 
    </p>
    <img src="help/pdfannotation.jpg"></br><br>
    <img src="help/pdfannotation2.jpg">
    <p>
    Example text:
    </p>
    <img src="help/pdfannotation3.jpg">
    <p>
    Example text and image:
    </p>
    <img src="help/pdfannotation4.jpg">
 
  * @author Rob Kooper
 * @author Peter Bajcsy
 * @author (documentation) Peter Ferak
 * 
 */
public class PDFAnnotationDialog extends Im2LearnFrame implements Im2LearnMenu,  ImageAnnotation, MouseListener {
    private ImagePanel imagepanel;
 
    private JTextArea txtPDF;
    private JTabbedPane tabPDF;
    
    static private Log logger = LogFactory.getLog(PDFAnnotationDialog.class);

    public PDFAnnotationDialog() {
        super("Show PDFAnnotation");

        createUI();
    }
    
    public void showing() {
        imagepanel.addMouseListener(this);
        imagepanel.addAnnotationImage(this);
        imagepanel.repaint();

        tabPDF.removeAll();
        txtPDF.setText("");
}
    
    public void closing() {
        imagepanel.removeMouseListener(this);
        imagepanel.removeAnnotationImage(this);
        imagepanel.repaint();
        
        tabPDF.removeAll();
        txtPDF.setText("");
    }

    private void createUI() {
        txtPDF = new JTextArea(4, 40);
        tabPDF = new JTabbedPane();
        tabPDF.setPreferredSize(new Dimension(320, 240));

		JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(txtPDF), tabPDF);
		getContentPane().add(split, BorderLayout.CENTER);

        JPanel panel = new JPanel(new FlowLayout());
        panel.add(new JButton(new AbstractAction("Close") {
            public void actionPerformed(ActionEvent e) {
                PDFAnnotationDialog.this.setVisible(false);
            }
        }));
        getContentPane().add(panel, BorderLayout.SOUTH);
		
        pack();
    }

    private void showInfo(Point pt) {
        // make sure we are visible
        if (!isVisible()) {
            return;
        }

        // get the imageobject from the panel
        ImageObject imgobj = imagepanel.getImageObject();
        if (imgobj == null) {
            return;
        }

        int idx = 0;
        String text = "";
        tabPDF.removeAll();

        // get the vector with annotations
        Vector<PDFAnnotation> annotations = (Vector<PDFAnnotation>) imgobj.getProperty(PDFAnnotation.KEY);

        // loop through all annotations checking mouse loc with bounding box.
        if ((annotations != null) && (annotations.size() > 0)) {
            for (PDFAnnotation anno : annotations) {
                if (anno.getBoundingBox().contains(pt)) {
                    text += anno.toString() + "\n";
                    if (anno.isImage()) {
                        ImageObject img = (ImageObject) anno.getObject();
                        ImagePanel imgpnl = new ImagePanel(img);
                        imgpnl.setAutozoom(true);
                        tabPDF.insertTab("IMAGE " + idx, null, imgpnl, imgobj.toString(), idx++);
                    }
                }
            }
        }

        // get the vector with groups
        Vector<PDFAnnotationGroup> groups = (Vector<PDFAnnotationGroup>) imgobj.getProperty(PDFAnnotationGroup.KEY);

        // loop through all annotations checking mouse loc with bounding box.
        if ((groups != null) && (groups.size() > 0)) {
            for (PDFAnnotationGroup group : groups) {
                if (group.getBoundingBox().contains(pt)) {
                    text += "GROUP {\n" + group.toString() + "}\n";
                    ImagePanel imgpnl = new ImagePanel(imgobj);
                    imgpnl.setAutozoom(true);
                    imgpnl.setCrop(group.getBoundingBox());
                    tabPDF.insertTab("GROUP", null, imgpnl, null, idx++);
                }
            }
        }

        txtPDF.setText(text);
    }

    // ------------------------------------------------------------------------
    // ImageAnnotation implementation
    // ------------------------------------------------------------------------
    public void paint(Graphics2D g, ImagePanel imagepanel) {
        // make sure we need to draw the boxes
        if (!isVisible()) {
            return;
        }

        // get the imageobject from the panel
        ImageObject imgobj = imagepanel.getImageObject();
        if (imgobj == null) {
            return;
        }

        // save the old graphics color
        Color old = g.getColor();

        // get the vector with annotations
        Vector<PDFAnnotation> annotations = (Vector<PDFAnnotation>) imgobj.getProperty(PDFAnnotation.KEY);
        if ((annotations != null) && (annotations.size() > 0)) {

            // draw boxes around all annotations
            for (PDFAnnotation anno : annotations) {
                anno.drawBoundingBox(g);
            }
        }

        // get the vector with groups
        Vector<PDFAnnotationGroup> groups = (Vector<PDFAnnotationGroup>) imgobj.getProperty(PDFAnnotationGroup.KEY);
        if ((groups != null) && (groups.size() > 0)) {

            // draw boxes around all annotations
            for (PDFAnnotationGroup group : groups) {
                group.drawBoundingBox(g);
            }
        }

        // set graphics color back to original
        g.setColor(old);
    }

    // ------------------------------------------------------------------------
    // MouseListener implementation
    // ------------------------------------------------------------------------
    public void mouseClicked(MouseEvent e) {
        showInfo(imagepanel.getImageLocation(e.getPoint()));
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }
    
    // ------------------------------------------------------------------------
    // Im2LearnMenu implementation
    // ------------------------------------------------------------------------
    public void setImagePanel(ImagePanel imagepanel) {
        try {
            PDFLoaderJPedal.setUseAnnotations(true);
        } catch (Throwable thr) {
            logger.warn("Error setting annotation loading.", thr);
        }
        try {
            PDFLoaderPDFBox.setUseAnnotations(true);
        } catch (Throwable thr) {
            logger.warn("Error setting annotation loading.", thr);
        }
        this.imagepanel = imagepanel;
        imageUpdated(new ImageUpdateEvent(this, ImageUpdateEvent.NEW_IMAGE, null));
    }

    public JMenuItem[] getPanelMenuItems() {
        return null;
    }

    public JMenuItem[] getMainMenuItems() {
        JMenu tools = new JMenu("Tools");
        JMenu pdf = new JMenu("PDF");
        tools.add(pdf);
        
        JMenuItem menu = new JMenuItem("Show PDFAnnotation");
        menu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!PDFAnnotationDialog.this.isVisible()) {
                    Window win = SwingUtilities.getWindowAncestor(imagepanel);
                    PDFAnnotationDialog.this.setLocationRelativeTo(win);
                    PDFAnnotationDialog.this.setVisible(true);
                }
                PDFAnnotationDialog.this.toFront();
            }
        });
        pdf.add(menu);
        
        return new JMenuItem[]{tools};
    }

    public void imageUpdated(ImageUpdateEvent event) {
        if (!isVisible()) {
            return;
        }
        
        if (event.getId() == ImageUpdateEvent.NEW_IMAGE) {
            showing();
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
