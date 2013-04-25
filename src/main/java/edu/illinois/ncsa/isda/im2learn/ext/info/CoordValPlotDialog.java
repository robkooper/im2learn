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
package edu.illinois.ncsa.isda.im2learn.ext.info;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.im2learn.core.display.Im2LearnMenu;
import edu.illinois.ncsa.isda.im2learn.core.display.ImageMarker;
import edu.illinois.ncsa.isda.im2learn.core.display.ImagePanel;
import edu.illinois.ncsa.isda.im2learn.core.display.ImageUpdateEvent;
import edu.illinois.ncsa.isda.im2learn.ext.misc.PlotComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.URL;

/**
 * 	
 * <p>
	<B>The class CoordValPlot provides a tool for selecting image points and viewing their values in all available
	coordinate systems, e.g., (row, col) system or (UTM Northing, UTM Easting) system or (latitude, longitude) system. </B>
	 </p>
	 <p>
	 <b>Description:</b>
	
	 This tool is for viewing two-dimensional multi-variate pixel values in multiple coordinate systems. The dialog below
	 will have one added row after each mouse click on the image. The reported values are reported in pixel 
	 coordinate system and latitude, longitude coordinate system. 
	  </p>
	 <img src="help/coordValPlotDialog.jpg">
	 <p>
Locations of multiple mouse clicks are shown as crosses overlaid on the underlying image (see the figure below). Markers
can be removed by pressing the button "Reset Markers". 
	  
	 </p>
	 <img src="help/coordValPlotDialog1.jpg">
<BR>
	 
	 * @author Rob Kooper, Peter Bajcsy (documentation)
 * 
 */
public class CoordValPlotDialog implements Im2LearnMenu {
    private ShowCoordVal current;
    private ImagePanel imagepanel;

    static private Log logger = LogFactory.getLog(CoordValPlotDialog.class);

    public CoordValPlotDialog() {
        current = null;
    }

    // ------------------------------------------------------------------------
    // Im2LearnMenu implementation
    // ------------------------------------------------------------------------
    public void setImagePanel(ImagePanel imagepanel) {
        this.imagepanel = imagepanel;
    }

    public JMenuItem[] getPanelMenuItems() {
        JMenuItem coordval = new JMenuItem(new AbstractAction("Coord Val Plot") {
            public void actionPerformed(ActionEvent e) {
                if ((current == null) || !current.isVisible()) {
                    current = new ShowCoordVal(imagepanel);
                }
                Point loc = current.imagepanel.getImageLocationClicked();
                current.addPoint(loc);
                current.setVisible(true);
            }
        });

        return new JMenuItem[]{coordval};
    }

    public JMenuItem[] getMainMenuItems() {
        return null;
    }

    public void imageUpdated(ImageUpdateEvent event) {
        if (event.getId() == ImageUpdateEvent.NEW_IMAGE) {
            current = null;
        }
    }

    // ------------------------------------------------------------------------
    // Window displaying coordvalues
    // ------------------------------------------------------------------------
    class ShowCoordVal extends JFrame {
        private JComboBox cmbPoints;
        private PlotComponent pc;
        private JTextField txtCol;
        private JTextField txtRow;
        private ImagePanel imagepanel;
        private JCheckBox chkSelected;

        public ShowCoordVal(ImagePanel imagepanel) {
            super("Coord Val");

            this.imagepanel = imagepanel;
            createUI();
        }

        public void addPoint(Point loc) {
            Marker marker = new Marker(loc);
            showValues(marker);
            cmbPoints.addItem(marker);
            cmbPoints.setSelectedIndex(marker.id);
        }

        private void createUI() {
            pc = new PlotComponent();
            pc.reset();
            getContentPane().add(pc, BorderLayout.CENTER);

            JPanel pnl = new JPanel(new FlowLayout());
            cmbPoints = new JComboBox();
            cmbPoints.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    Marker marker = (Marker) e.getItem();
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        marker.marker.setColor(Color.red);
                        txtCol.setText("" + marker.marker.x);
                        txtRow.setText("" + marker.marker.y);
                        if (chkSelected.isSelected()) {
                            pc.setSeriesVisible(marker.id, true);
                        }
                    } else {
                        marker.marker.setColor(Color.black);
                        if (chkSelected.isSelected()) {
                            pc.setSeriesVisible(marker.id, false);
                        }
                    }
                    imagepanel.repaint();
                }
            });
            pnl.add(cmbPoints);
            pnl.add(new JLabel("Col :"));
            txtCol = new JTextField("", 5);
            pnl.add(txtCol);
            pnl.add(new JLabel("Row :"));
            txtRow = new JTextField("", 5);
            pnl.add(txtRow);

            chkSelected = new JCheckBox("Show only selected?");
            chkSelected.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (chkSelected.isSelected()) {
                        Marker marker = (Marker) cmbPoints.getSelectedItem();
                        pc.hideAllSeries();
                        pc.setSeriesVisible(marker.id, true);
                    } else {
                        pc.showAllSeries();
                    }
                }
            });
            pnl.add(chkSelected);

            getContentPane().add(pnl, BorderLayout.NORTH);

            pnl = new JPanel(new FlowLayout());
            JButton btn = new JButton(new AbstractAction("Apply") {
                public void actionPerformed(ActionEvent e) {
                    ImageObject imgobj = imagepanel.getImageObject();
                    Marker marker = (Marker) cmbPoints.getSelectedItem();
                    try {
                        marker.marker.x = Integer.parseInt(txtCol.getText());
                        marker.marker.y = Integer.parseInt(txtRow.getText());
                        if (marker.marker.x < 0) marker.marker.x = 0;
                        if (marker.marker.x >= imgobj.getNumCols()) marker.marker.x = imgobj.getNumCols() - 1;
                        if (marker.marker.y < 0) marker.marker.y = 0;
                        if (marker.marker.x >= imgobj.getNumRows()) marker.marker.x = imgobj.getNumCols() - 1;
                    } catch (NumberFormatException exc) {
                        logger.error("Error parsing numbers.", exc);
                    }
                    showValues(marker);
                }
            });
            pnl.add(btn);
            btn = new JButton(new AbstractAction("Reset") {
                public void actionPerformed(ActionEvent e) {
                    Marker marker = (Marker) cmbPoints.getSelectedItem();
                    txtCol.setText("" + marker.marker.x);
                    txtRow.setText("" + marker.marker.y);
                }
            });
            pnl.add(btn);
            btn = new JButton(new AbstractAction("Close") {
                public void actionPerformed(ActionEvent e) {
                    setVisible(false);
                }
            });
            pnl.add(btn);
            getContentPane().add(pnl, BorderLayout.SOUTH);

            pack();
        }

        public void closing() {
            int count = cmbPoints.getItemCount();
            for (int i = 0; i < count; i++) {
                Marker marker = (Marker) cmbPoints.getItemAt(i);
                imagepanel.removeAnnotationImage(marker.marker);
            }
        }

        private void showValues(Marker marker) {
            int x = marker.marker.x;
            int y = marker.marker.y;
            ImageObject imgobj = imagepanel.getImageObject();
            double[] data = new double[imgobj.getNumBands()];
            for (int b = 0; b < imgobj.getNumBands(); b++) {
                data[b] = imgobj.getDouble(y, x, b);
            }
            try {
                if (marker.id == -1) {
                    marker.id = pc.addSeries(null);
                    pc.setValue(marker.id, data);
                } else {
                    pc.resetSeries(marker.id);
                    pc.setValue(marker.id, data);
                }
                imagepanel.addAnnotationImage(marker.marker);
                imagepanel.repaint();
            } catch (IllegalArgumentException exc) {
                logger.error("Error setting data.", exc);
            }
        }

        class Marker {
            private ImageMarker marker;
            private int id;

            public Marker(Point loc) {
                id = -1;
                marker = new ImageMarker(loc.x, loc.y, 10, ImageMarker.CROSS);
                marker.setVisible(true);
            }

            public void setID(int id) {
                this.id = id;
                marker.setLabel("" + id, 2, 2, null);
            }

            public String toString() {
                return "Marker " + id;
            }
        }
    }

    public URL getHelp(String menu) {
        return getClass().getResource("help/CoordValPlotDialog.html");
    }

}
