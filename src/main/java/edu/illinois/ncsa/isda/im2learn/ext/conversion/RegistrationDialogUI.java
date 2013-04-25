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
package edu.illinois.ncsa.isda.im2learn.ext.conversion;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.media.jai.WarpGrid;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextPane;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.im2learn.core.Im2LearnUtilities;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageException;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.im2learn.core.display.Im2LearnFrame;
import edu.illinois.ncsa.isda.im2learn.core.display.ImageMarker;
import edu.illinois.ncsa.isda.im2learn.core.display.ImagePanel;
import edu.illinois.ncsa.isda.im2learn.core.display.ImageUpdateEvent;
import edu.illinois.ncsa.isda.im2learn.core.display.ImageUpdateListener;
import edu.illinois.ncsa.isda.im2learn.ext.panel.GammaDialog;
import edu.illinois.ncsa.isda.im2learn.ext.panel.SelectBandDialog;
import edu.illinois.ncsa.isda.im2learn.ext.panel.SelectionDialog;
import edu.illinois.ncsa.isda.im2learn.ext.panel.ZoomDialog;
import edu.illinois.ncsa.isda.im2learn.ext.test.AlphaImageAnnotation;

/**
 * Show the UI that allows the user to register two images. What it does is show
 * the images, and records the mouse clicks on those images. The images are
 * shown in an imagepanel with the zoomdialog added as well.
 * 
 * @author Sang-Chul Lee
 * @author Rob Kooper
 * @version 1.0
 */
@SuppressWarnings( { "nls", "serial" })
public class RegistrationDialogUI extends JPanel implements ImageUpdateListener {
    private static final Log             logger            = LogFactory.getLog(RegistrationDialogUI.class);
    protected ImagePanel                 panImage1;
    private final JSlider                sldImage1;
    private final ArrayList<ImageMarker> lstMarker1        = new ArrayList<ImageMarker>();

    protected ImagePanel                 panImage2;
    private final JSlider                sldImage2;
    private final ArrayList<ImageMarker> lstMarker2        = new ArrayList<ImageMarker>();
    private JComboBox                    chserModel;
    private JSpinner                     spinnerX;
    private JSpinner                     spinnerY;
    private JLabel                       lblGridX;
    private JLabel                       lblGridY;
    private int                          lastSelectedIndex = 0;

    private AffineTransform              afx               = null;

    /**
     * Default constructor, creates the UI.
     */
    public RegistrationDialogUI() {
        super();

        GridBagConstraints gridConstraints = new GridBagConstraints();
        gridConstraints.fill = GridBagConstraints.BOTH;
        gridConstraints.weighty = 1.0;
        gridConstraints.weightx = 0.5;
        gridConstraints.insets = new Insets(2, 2, 2, 2);

        GridBagLayout gridBag = new GridBagLayout();
        setLayout(gridBag);

        // first image panel
        gridConstraints.gridx = 0;
        gridConstraints.gridy = 0;
        panImage1 = new ImagePanel();
        panImage1.addImageUpdateListener(this);
        panImage1.setAutozoom(true);
        panImage1.setSelectionAllowed(true);
        panImage1.addMenu(new ZoomDialog());
        panImage1.addMenu(new SelectionDialog());
        panImage1.addMenu(new SelectBandDialog());
        panImage1.addMenu(new GammaDialog());
        panImage1.addMouseListener(new MouseAdapter() {
            private Point pt = new Point();

            @Override
            public void mouseReleased(MouseEvent e) {
                if (pt.equals(e.getPoint()) && panImage1.isEnabled() && (e.getButton() == MouseEvent.BUTTON1)) {
                    Point p = panImage1.getImageLocation(e.getPoint());
                    ImageMarker marker = new ImageMarker(p.x, p.y, 15, ImageMarker.CROSS);
                    marker.setColor(getColor(panImage1));
                    marker.setVisible(true);
                    marker.setLabel("" + (lstMarker1.size() + 1), 2, 2, null);
                    if (validateMarker(marker, panImage1)) {
                        lstMarker1.add(marker);
                        panImage1.addAnnotationImage(marker);
                        addMarker();
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                pt = e.getPoint();
            }
        });
        int vsc = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED;
        int hsc = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED;
        if (Im2LearnUtilities.isMACOS()) {
            vsc = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS;
            hsc = JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS;
        }
        JScrollPane scrollpane = new JScrollPane(panImage1, vsc, hsc);
        scrollpane.setBorder(null);
        scrollpane.setPreferredSize(new Dimension(320, 240));
        gridBag.setConstraints(scrollpane, gridConstraints);
        add(scrollpane);

        // second image panel
        gridConstraints.gridx = 1;
        panImage2 = new ImagePanel();
        panImage2.addImageUpdateListener(this);
        panImage2.setAutozoom(true);
        panImage2.setSelectionAllowed(true);
        panImage2.addMenu(new ZoomDialog());
        panImage2.addMenu(new SelectionDialog());
        panImage2.addMenu(new SelectBandDialog());
        panImage2.addMenu(new GammaDialog());
        panImage2.addMouseListener(new MouseAdapter() {
            private Point pt = new Point();

            @Override
            public void mouseReleased(MouseEvent e) {
                if (pt.equals(e.getPoint()) && panImage2.isEnabled() && (e.getButton() == MouseEvent.BUTTON1)) {
                    Point p = panImage2.getImageLocation(e.getPoint());
                    ImageMarker marker = new ImageMarker(p.x, p.y, 15, ImageMarker.CROSS);
                    marker.setColor(getColor(panImage2));
                    marker.setVisible(true);
                    marker.setLabel("" + (lstMarker2.size() + 1), 2, 2, null);
                    if (validateMarker(marker, panImage2)) {
                        lstMarker2.add(marker);
                        panImage2.addAnnotationImage(marker);
                        addMarker();
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                pt = e.getPoint();
            }
        });
        scrollpane = new JScrollPane(panImage2, vsc, hsc);
        scrollpane.setBorder(null);
        scrollpane.setPreferredSize(new Dimension(320, 240));
        gridBag.setConstraints(scrollpane, gridConstraints);
        add(scrollpane);

        // slider to select band for first image
        gridConstraints.gridx = 0;
        gridConstraints.weighty = 0.0;
        gridConstraints.gridy++;
        sldImage1 = new JSlider(0, 0, 0);
        sldImage1.setEnabled(false);
        sldImage1.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                panImage1.setGrayBand(sldImage1.getValue());
            }
        });
        gridBag.setConstraints(sldImage1, gridConstraints);
        add(sldImage1);

        // slider to select band for second image
        gridConstraints.gridx = 1;
        sldImage2 = new JSlider(0, 0, 0);
        sldImage2.setEnabled(false);
        sldImage2.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                panImage2.setGrayBand(sldImage2.getValue());
            }
        });
        gridBag.setConstraints(sldImage2, gridConstraints);
        add(sldImage2);

        // user panel for image 1
        gridConstraints.gridx = 0;
        gridConstraints.weighty = 0.0;
        gridConstraints.gridy++;
        JPanel panel = getUserPanel1();
        if (panel != null) {
            gridBag.setConstraints(panel, gridConstraints);
            add(panel);
        }

        // slider to select band for second image
        gridConstraints.gridx = 1;
        panel = getUserPanel2();
        if (panel != null) {
            gridBag.setConstraints(panel, gridConstraints);
            add(panel);
        }

        // add the buttons.
        JPanel buttons = getButtons();
        if (buttons != null) {
            gridConstraints.gridx = 0;
            gridConstraints.weightx = 0;
            gridConstraints.gridwidth = 2;
            gridConstraints.gridy++;
            gridBag.setConstraints(buttons, gridConstraints);
            add(buttons);
        }

        setEnabled(false);
        checkUI();
    }

    private void checkUI() {
        if (getTransformationModel() != -1) {
            if (getTransformationModel() == 2 && lastSelectedIndex != 2) {
                lblGridX.setVisible(true);
                lblGridY.setVisible(true);
                spinnerX.setVisible(true);
                spinnerY.setVisible(true);
                resetMarkers();
                if (panImage1.getImageObject() != null) {
                    addMarkersForWarp();
                }
            }
            if (getTransformationModel() != 2) {
                lblGridX.setVisible(false);
                lblGridY.setVisible(false);
                spinnerX.setVisible(false);
                spinnerY.setVisible(false);
                if (lastSelectedIndex == 2) {
                    resetMarkers();
                }
            }
        }
    }

    private void addMarkersForWarp() {
        int gridH = 0, gridV = 0;
        Object valH = spinnerX.getValue();
        Object valV = spinnerY.getValue();
        if (valH instanceof Integer) {
            gridH = ((Integer) valH).intValue();
        }
        if (valV instanceof Integer) {
            gridV = ((Integer) valV).intValue();
        }
        if (gridH == 0 && gridV == 0) {
            return;
        }
        int numCols = panImage1.getImageObject().getNumCols();
        int numRows = panImage1.getImageObject().getNumRows();
        int increamentH = numCols / (gridH + 1);
        int increamentV = numRows / (gridV + 1);
        int col = 0, row = 0;
        for (int i = 0; i < gridV; i++) {
            row += increamentV;
            col = 0;
            for (int j = 0; j < gridH; j++) {
                col += increamentH;
                ImageMarker marker = new ImageMarker(col, row, 15, ImageMarker.CROSS);
                marker.setColor(getColor(panImage1));
                marker.setVisible(true);
                marker.setLabel("" + (lstMarker1.size() + 1), 2, 2, null);
                if (validateMarker(marker, panImage1)) {
                    lstMarker1.add(marker);
                    panImage1.addAnnotationImage(marker);
                    addMarker();
                }
            }
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        panImage1.setEnabled(enabled);
        panImage2.setEnabled(enabled);
        if (enabled) {
            sldImage1.setEnabled(panImage1.isGrayScale());
            sldImage2.setEnabled(panImage2.isGrayScale());
        } else {
            sldImage1.setEnabled(enabled);
            sldImage2.setEnabled(enabled);
        }
    }

    /**
     * Remove the images. This will free the memory associated with the
     * imagepanel.
     */
    public void resetImages() {
        panImage1.setImageObject(null);
        panImage1.setGrayBand(0);
        panImage1.setGrayScale(true);
        sldImage1.setValue(0);
        sldImage1.setMaximum(0);

        panImage2.setImageObject(null);
        panImage2.setGrayBand(0);
        panImage2.setGrayScale(true);
        sldImage2.setValue(0);
        sldImage2.setMaximum(0);

        setEnabled(false);
    }

    /**
     * Removes all the points clicked.
     */
    public void resetMarkers() {
        lstMarker1.clear();
        panImage1.removeAllAnnotations();
        lstMarker2.clear();
        panImage2.removeAllAnnotations();

        addMarker();
    }

    public void undoMarker(int panel) {
        ImageMarker marker;
        int last;

        switch (panel) {
        case 1:
            last = lstMarker1.size() - 1;
            if (last >= 0) {
                marker = lstMarker1.remove(last);
                panImage1.removeAnnotationImage(marker);
            }
            break;

        case 2:
            last = lstMarker2.size() - 1;
            if (last >= 0) {
                marker = lstMarker2.remove(last);
                panImage2.removeAnnotationImage(marker);
            }
            break;
        }
    }

    public int getTransformationModel() {
        if (chserModel != null) {
            return chserModel.getSelectedIndex();
        } else {
            return -1;
        }
    }

    public WarpGrid getWarpModel() {
        WarpGrid warp = null;

        if (isTransformValid()) {
            Point2D[] f2 = getPointsInImage2();
            // make sure both feature sets are the same length.
            // if (f1.length != f2.length) {
            // logger.error(message)
            // }

            int gridH = 0, gridV = 0;
            Object valH = spinnerX.getValue();
            Object valV = spinnerY.getValue();
            if (valH instanceof Integer) {
                gridH = ((Integer) valH).intValue();
            }
            if (valV instanceof Integer) {
                gridV = ((Integer) valV).intValue();
            }
            int numCols = panImage1.getImageObject().getNumCols();
            int numRows = panImage1.getImageObject().getNumRows();
            int increamentH = numCols / (gridH + 1);
            int increamentV = numRows / (gridV + 1);

            // pack points from destination image to a float array
            float[] warpPositions = new float[f2.length * 2];
            for (int i = 0; i < f2.length; i++) {
                warpPositions[2 * i] = (float) f2[i].getX();
                warpPositions[2 * i + 1] = (float) f2[i].getY();
            }
            warp = new WarpGrid(increamentH, increamentH, gridH - 1, increamentV, increamentV, gridV - 1, warpPositions);
        }

        return warp;
    }

    /**
     * Returns a list of buttons that will be added to the panel. By default no
     * additional buttons are added.
     * 
     * @return panel with buttons.
     */
    public JPanel getButtons() {
        JPanel panel = new JPanel(new FlowLayout());

        chserModel = new JComboBox();
        chserModel.addItem("Affine");
        chserModel.addItem("Rigid");
        chserModel.addItem("Warp");
        chserModel.setSelectedIndex(0);
        panel.add(chserModel);
        chserModel.setAction(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                checkUI();
                lastSelectedIndex = chserModel.getSelectedIndex();
            }
        });

        lblGridX = new JLabel("Grid X: ");
        lblGridY = new JLabel("Grid Y: ");
        int currentValue = 3;
        int min = 2, max = 30, step = 1;
        SpinnerModel model1 = new SpinnerNumberModel(currentValue, min, max, step);
        SpinnerModel model2 = new SpinnerNumberModel(currentValue, min, max, step);
        spinnerX = new JSpinner(model1);
        spinnerY = new JSpinner(model2);

        panel.add(lblGridX);
        panel.add(spinnerX);
        panel.add(lblGridY);
        panel.add(spinnerY);

        spinnerX.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (panImage1.getImageObject() != null) {
                    resetMarkers();
                }
                addMarkersForWarp();
            }
        });

        spinnerY.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (panImage1.getImageObject() != null) {
                    resetMarkers();
                }
                addMarkersForWarp();
            }
        });

        JButton btn = new JButton(new AbstractAction("Reset") {
            public void actionPerformed(ActionEvent e) {
                afx = null;
                resetMarkers();
            }
        });
        panel.add(btn);

        btn = new JButton(new AbstractAction("Check") {
            public void actionPerformed(ActionEvent e) {
                if (isTransformValid()) {
                    Point[] f1 = getPointsInImage1();
                    Point[] f2 = getPointsInImage2();
                    ImageObject img = getImage2();

                    try {
                        ImageObject imgrgb = null;
                        Registration r = new Registration(f1, f2);
                        if (chserModel.getSelectedIndex() == 1) {
                            afx = r.getRigidTransform();
                            imgrgb = r.getImageTransformed(img, afx);
                        } else {
                            afx = r.getAffineTransform();
                            imgrgb = r.getImageTransformed(img, afx);
                        }

                        TEST test = new TEST();
                        test.setImageObject(getImage1());
                        test.getImagePanel().setGamma(panImage1.getGamma());
                        test.addImageObject(imgrgb);
                        test.pack();
                        test.setLocationRelativeTo(RegistrationDialogUI.this);
                        test.setVisible(true);
                    } catch (ImageException exc) {
                        logger.info("Error registring image.", exc);
                    }
                }
            }
        });
        panel.add(btn);

        JButton btn1 = new JButton(new AbstractAction("ShowParameters") {
            public void actionPerformed(ActionEvent e) {
                if (afx != null) {
                    String message = afx.toString();

                    JFrame f = new JFrame("Parameters");
                    f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    JTextPane tp = new JTextPane();
                    tp.setText(message);
                    f.getContentPane().add(tp);
                    f.pack();
                    f.setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(null, "Parameters are unavailable. Press Check button.", "Transform Parameters", JOptionPane.INFORMATION_MESSAGE);
                }

            }
        });
        panel.add(btn1);

        return panel;
    }

    public JPanel getUserPanel1() {
        return null;
    }

    public JPanel getUserPanel2() {
        return null;
    }

    /**
     * Checks to see if there are enough points to do registration. There have
     * to be at least 3 points selected on each image.
     * 
     * @return true if enough points are selected for registration.
     */
    public boolean isTransformValid() {
        if (getTransformationModel() == 1) {
            if ((lstMarker1.size() < 2) || (lstMarker2.size() < 2)) {
                return false;
            }
        } else {
            if ((lstMarker1.size() < 3) || (lstMarker2.size() < 3)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Called when the user places a marker. By default nothing happens here.
     */
    public void addMarker() {
    }

    /**
     * Called when the user places a marker. By default nothing happens here.
     */
    public boolean validateMarker(ImageMarker marker, ImagePanel panel) {
        return true;
    }

    /**
     * This will return a list of points where the user clicked. The list is in
     * the same order as those clicked in the image.
     * 
     * @return list of points where the user clicked.
     */
    public Point[] getPointsInImage1() {
        int len = lstMarker1.size();
        Point[] points = new Point[len];
        ImageMarker marker;
        for (int i = 0; i < len; i++) {
            marker = lstMarker1.get(i);
            points[i] = new Point(marker.x, marker.y);
        }
        return points;
    }

    /**
     * This will return a list of points where the user clicked. The list is in
     * the same order as those clicked in the image.
     * 
     * @return list of points where the user clicked.
     */
    public Point[] getPointsInImage2() {
        int len = lstMarker2.size();
        Point[] points = new Point[len];
        ImageMarker marker;
        for (int i = 0; i < len; i++) {
            marker = lstMarker2.get(i);
            points[i] = new Point(marker.x, marker.y);
        }
        return points;
    }

    /**
     * Set the first image shown. This shows the image that is used as the from
     * part when trying to register the images.
     * 
     * @param imgobj
     *            to be used.
     * @throws ImageException
     *             if the image is not set correctly.
     */
    public void setImage1(ImageObject imgobj) throws ImageException {
        panImage1.setImageObject(imgobj);
        // use panImage2.getImageObject in case imgobj is null, panImage2 will
        // return a dummy object that is 1x1x1
        // panImage1.setGrayBand(0);
        // panImage1.setGrayScale(true);
        // sldImage1.setValue(0);
        if (imgobj == null) {
            sldImage1.setMaximum(0);
        } else {
            sldImage1.setMaximum(imgobj.getNumBands() - 1);
        }
        lstMarker1.clear();

        if ((panImage1.getImageObject() != null) && (panImage2.getImageObject() != null)) {
            setEnabled(true);
        } else {
            setEnabled(false);
        }
        addMarker();
    }

    public ImageObject getImage1() {
        return panImage1.getImageObject();
    }

    /**
     * Set the second image shown. This shows the image that is used as the to
     * part when trying to register the images.
     * 
     * @param imgobj
     *            to be used.
     * @throws ImageException
     *             if the image is not set correctly.
     */
    public void setImage2(ImageObject imgobj) throws ImageException {
        panImage2.setImageObject(imgobj);
        // panImage2.setGrayBand(0);
        // panImage2.setGrayScale(true);
        // sldImage2.setValue(0);
        if (imgobj == null) {
            sldImage2.setMaximum(0);
        } else {
            sldImage2.setMaximum(imgobj.getNumBands() - 1);
        }
        lstMarker2.clear();

        if ((panImage1.getImageObject() != null) && (panImage2.getImageObject() != null)) {
            setEnabled(true);
        } else {
            setEnabled(false);
        }

        addMarker();
    }

    public ImageObject getImage2() {
        return panImage2.getImageObject();
    }

    public void imageUpdated(ImageUpdateEvent event) {
        if (event.getId() == ImageUpdateEvent.CHANGE_GRAYSCALE) {
            sldImage1.setEnabled(panImage1.isGrayScale());
            sldImage2.setEnabled(panImage2.isGrayScale());
            fixColor();
        } else if (event.getId() == ImageUpdateEvent.CHANGE_GRAYBAND) {
            if (event.getSource() == panImage1) {
                sldImage1.setValue(panImage1.getGrayBand());
            } else {
                sldImage2.setValue(panImage2.getGrayBand());
            }
        }
    }

    private Color getColor(ImagePanel panImage) {
        return panImage.isGrayScale() ? Color.red : Color.white;
    }

    private void fixColor() {
        ImageMarker marker;

        int len = lstMarker1.size();
        for (int i = 0; i < len; i++) {
            marker = lstMarker1.get(i);
            marker.setColor(getColor(panImage1));
        }

        len = lstMarker2.size();
        for (int i = 0; i < len; i++) {
            marker = lstMarker2.get(i);
            marker.setColor(getColor(panImage2));
        }
    }

    class TEST extends Im2LearnFrame {
        private final ImagePanel         ip;
        private final HashMap            hm;
        private final GridBagLayout      gbl;
        private final GridBagConstraints gbc;
        private final JPanel             panel;

        public TEST() {
            super("TEST");

            hm = new HashMap();

            // set up imagepanel
            ip = new ImagePanel();
            ip.setAutozoom(true);
            ip.setPreferredSize(new Dimension(320, 240));
            getContentPane().add(ip, BorderLayout.CENTER);

            // panel for controls
            JPanel pnlC = new JPanel(new BorderLayout());
            getContentPane().add(pnlC, BorderLayout.SOUTH);

            JPanel buttons = new JPanel(new FlowLayout());
            pnlC.add(buttons);

            JButton btn = new JButton(new AbstractAction("Close") {
                public void actionPerformed(ActionEvent e) {
                    setVisible(false);
                }
            });
            pnlC.add(btn, BorderLayout.SOUTH);

            // panel to hold the sliders
            gbc = new GridBagConstraints();
            gbl = new GridBagLayout();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            gbc.weighty = 0.0;
            gbc.gridx = 0;
            gbc.gridy = 0;
            panel = new JPanel(gbl);
            pnlC.add(panel, BorderLayout.CENTER);

            pack();
        }

        @Override
        public void closing() {
            ip.setImageObject(null);

            for (Iterator iter = hm.values().iterator(); iter.hasNext();) {
                AlphaImageAnnotation aia = (AlphaImageAnnotation) iter.next();
                aia.setImageObject(null);
            }

            hm.clear();
            ip.removeAllAnnotations();
        }

        public ImagePanel getImagePanel() {
            return ip;
        }

        public void setImageObject(ImageObject imgobj) {
            ip.setImageObject(imgobj);
        }

        public void addImageObject(ImageObject imgobj) {
            AlphaImageAnnotation aia = new AlphaImageAnnotation();
            aia.setImageObject(imgobj);
            ip.addAnnotationPanel(aia);

            gbc.gridx = 0;
            gbc.weightx = 0;
            String name = (String) imgobj.getProperty(ImageObject.FILENAME);
            if (name == null) {
                name = "";
            }
            JLabel lbl;
            if (name.length() <= 10) {
                lbl = new JLabel(name);
            } else {
                lbl = new JLabel(name.substring(name.length() - 10));
                lbl.setToolTipText(name);
            }
            gbl.addLayoutComponent(lbl, gbc);
            panel.add(lbl);

            gbc.gridx = 1;
            gbc.weightx = 1;
            JSlider slider = new JSlider(0, 255, 0);
            slider.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    JSlider slider = (JSlider) e.getSource();
                    AlphaImageAnnotation aia = (AlphaImageAnnotation) hm.get(slider);
                    aia.setAlpha(slider.getValue());
                    ip.repaint();
                }
            });
            gbl.addLayoutComponent(slider, gbc);
            panel.add(slider);

            gbc.gridy++;
            hm.put(slider, aia);
        }

    }
}
