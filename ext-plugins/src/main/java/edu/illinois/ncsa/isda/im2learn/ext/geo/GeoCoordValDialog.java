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
package edu.illinois.ncsa.isda.im2learn.ext.geo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.im2learn.core.display.Im2LearnMenu;
import edu.illinois.ncsa.isda.im2learn.core.display.ImageMarker;
import edu.illinois.ncsa.isda.im2learn.core.display.ImagePanel;
import edu.illinois.ncsa.isda.im2learn.core.display.ImageUpdateEvent;
import edu.illinois.ncsa.isda.im2learn.core.geo.GeoUtilities;
import edu.illinois.ncsa.isda.im2learn.core.geo.projection.ModelProjection;
import edu.illinois.ncsa.isda.im2learn.core.geo.projection.Projection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.text.NumberFormat;

/**
 * Created by IntelliJ IDEA. User: kooper Date: Jul 18, 2004 Time: 3:49:32 PM To
 * change this template use File | Settings | File Templates.
 */
public class GeoCoordValDialog extends JDialog implements Im2LearnMenu {
    private ImagePanel imagepanel;
    private JTextField txtCoordX;
    private JTextField txtCoordY;
    private JTextField txtCoordNorthing;
    private JTextField txtCoordLatStr;
    private JTextField txtCoordEasting;
    private JTextField txtCoordLonStr;
    private ImageMarker marker;
    private Point loc = null;
    private JMenuItem geocoordval;

    static private Log logger = LogFactory.getLog(GeoCoordValDialog.class);

    public GeoCoordValDialog(Frame owner) {
        super(owner, "CoordVal Dialog");
        setResizable(false);
        marker = new ImageMarker(0, 0, 10, 10, ImageMarker.CROSS);
        marker.setVisible(false);
        marker.setColor(Color.orange);
        createUI();

        geocoordval = new JMenuItem(new AbstractAction("Geo Coord Val") {
            public void actionPerformed(ActionEvent e) {
                setVisible(true);
                showPoint(imagepanel.getImageLocationClicked());
            }
        });
        geocoordval.setEnabled(false);
    }

    private void createUI() {
        JPanel panel = new JPanel(new GridLayout(6, 2));
        getContentPane().add(panel, BorderLayout.CENTER);

        panel.add(new JLabel("X"));
        txtCoordX = new JTextField("N/A", 20);
        txtCoordX.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    loc.x = Integer.parseInt(txtCoordX.getText());
                    showPoint(loc);
                } catch (NumberFormatException exc) {
                    logger.warn("Invalid number.");
                }
            }
        });
        panel.add(txtCoordX);

        panel.add(new JLabel("Y"));
        txtCoordY = new JTextField("N/A", 20);
        txtCoordY.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    loc.y = Integer.parseInt(txtCoordY.getText());
                    showPoint(loc);
                } catch (NumberFormatException exc) {
                    logger.warn("Invalid number.");
                }
            }
        });
        panel.add(txtCoordY);

        panel.add(new JLabel("Northing"));
        txtCoordNorthing = new JTextField("N/A", 20);
        panel.add(txtCoordNorthing);

        panel.add(new JLabel("Lat"));
        txtCoordLatStr = new JTextField("N/A", 20);
        panel.add(txtCoordLatStr);

        panel.add(new JLabel("Easting"));
        txtCoordEasting = new JTextField("N/A", 20);
        panel.add(txtCoordEasting);

        panel.add(new JLabel("Lon"));
        txtCoordLonStr = new JTextField("N/A", 20);
        panel.add(txtCoordLonStr);

        panel = new JPanel(new FlowLayout());
        getContentPane().add(panel, BorderLayout.SOUTH);

        JButton btn = new JButton(new AbstractAction("Hide") {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        panel.add(btn);

        pack();
    }

    public void showPoint(Point loc) {
        this.loc = loc;
        if (loc == null) {
            marker.setVisible(false);
            txtCoordX.setText("N/A");
            txtCoordY.setText("N/A");
            txtCoordNorthing.setText("N/A");
            txtCoordLatStr.setText("");
            txtCoordEasting.setText("N/A");
            txtCoordLonStr.setText("");
            return;
        } else {
            marker.x = loc.x;
            marker.y = loc.y;
            imagepanel.addAnnotationImage(marker);
            marker.setVisible(true);
            imagepanel.repaint();
            txtCoordX.setText("" + loc.x);
            txtCoordY.setText("" + loc.y);
            try {
                ImageObject imgobj = imagepanel.getImageObject();
                Projection proj = (Projection) imgobj.getProperty(ImageObject.GEOINFO);
                double[] p = new double[]{loc.x, loc.y};
                double[] m = null;
                double[] x = null;
                if (proj instanceof ModelProjection) {
                    m = ((ModelProjection) proj).rasterToModel(p);
                    x = ((ModelProjection) proj).modelToEarth(m);
                }
                double[] t = null;
                if (x != null)
                    t = GeoUtilities.decimToDegMinSec(x[0]);
                String str;
                NumberFormat nfint = NumberFormat.getIntegerInstance();
                nfint.setMinimumIntegerDigits(2);
                nfint.setMaximumIntegerDigits(2);
                NumberFormat nfdbl = NumberFormat.getNumberInstance();
                nfdbl.setMinimumFractionDigits(2);
                nfdbl.setMaximumFractionDigits(2);
                nfdbl.setMinimumIntegerDigits(2);
                nfdbl.setMaximumIntegerDigits(2);
                if (t != null && t[0] < 0) {
                    str = nfint.format(-t[0]) + "d " + nfint.format(-t[1]) + "' " + nfdbl.format(-t[2]) + "\" W";
                } else {
                    str = nfint.format(t[0]) + "d " + nfint.format(t[1]) + "' " + nfdbl.format(t[2]) + "\" E";
                }
                txtCoordNorthing.setText("" + m[0]);
                txtCoordLatStr.setText(str);

                t = GeoUtilities.decimToDegMinSec(x[1]);
                if (t[0] < 0) {
                    str = nfint.format(-t[0]) + "d " + nfint.format(-t[1]) + "' " + nfdbl.format(-t[2]) + "\" S";
                } else {
                    str = nfint.format(t[0]) + "d " + nfint.format(t[1]) + "' " + nfdbl.format(t[2]) + "\" N";
                }
                txtCoordEasting.setText("" + m[1]);
                txtCoordLonStr.setText(str);
            } catch (Exception exc) {
                logger.error("Error geo conversion.", exc);
            }
        }
    }

    // ------------------------------------------------------------------------
            // Im2LearnMenu implementation
            // ------------------------------------------------------------------------
            public void setImagePanel(ImagePanel imagepanel) {
        this.imagepanel = imagepanel;
    }

    public JMenuItem[] getPanelMenuItems() {
        return new JMenuItem[]{geocoordval};
    }

    public JMenuItem[] getMainMenuItems() {
        return null;
    }

    public void imageUpdated(ImageUpdateEvent event) {
        if (event.getId() == ImageUpdateEvent.NEW_IMAGE) {
            showPoint(null);
            ImageObject imgobj = (ImageObject) event.getObject();
            if ((imgobj != null) && (imgobj.getProperty(ImageObject.GEOINFO) != null)) {
                geocoordval.setEnabled(true);
            } else {
                geocoordval.setEnabled(false);
            }
        }
    }

    public URL getHelp(String topic) {
        // TODO Auto-generated method stub
        return null;
    }
}
