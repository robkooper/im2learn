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
package edu.illinois.ncsa.isda.im2learn.ext.vis;


import javax.swing.*;

import edu.illinois.ncsa.isda.im2learn.core.display.Im2LearnFrame;
import edu.illinois.ncsa.isda.im2learn.core.display.Im2LearnMenu;
import edu.illinois.ncsa.isda.im2learn.core.display.ImagePanel;
import edu.illinois.ncsa.isda.im2learn.core.display.ImageUpdateEvent;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

/**
 * Show a dialog that allows users to set the zoomfact. This dialog adds a
 * series of entries to the panel menu allowing the user to set the zoom, or
 * select a custom zoom factor. The custom zoomfactor is a small dialog that
 * allows the user to select from a set of default options, or type in a new
 * value. If this new value ends with a % assume it is a percentage, otherwise
 * assume it is a zoomfactor (where 1 == 100%).
 *
 * @author Rob Kooper
 * @version 2.0
 */
public class PaintScaleDialog extends Im2LearnFrame implements ActionListener, Im2LearnMenu {
    private ImagePanel imagepanel;
    private String[] values = new String[]{"10%", "25%", "50%", "75%", "100%"};
    private JRadioButtonMenuItem radio[] = new JRadioButtonMenuItem[values.length + 1];
    private int custom = values.length;
    private int fit = values.length - 1;
    private double factor = 1.0;
    private JComboBox combo;
    private JMenu zoom;

    /**
     * Default constructor, create the menu entries, and the dialog.
     *
     * @param owner parent of this dialog
     */
    public PaintScaleDialog() {
        super("Select PaintScale");
        setResizable(false);

        zoom = new JMenu("PaintScale");
        ButtonGroup group = new ButtonGroup();

        for (int i = 0; i < values.length; i++) {
            radio[i] = new JRadioButtonMenuItem(values[i]);
            radio[i].addActionListener(this);
            group.add(radio[i]);
            zoom.add(radio[i]);
        }

        radio[custom] = new JRadioButtonMenuItem(new AbstractAction("Custom") {
            public void actionPerformed(ActionEvent e) {
                JRadioButtonMenuItem me = (JRadioButtonMenuItem) e.getSource();
                me.setSelected(true);
                setVisible(true);
            }
        });
        group.add(radio[custom]);
        zoom.add(radio[custom]);

        createUI();
        pack();
    }

    /**
     * Create the UI.
     */
    public void createUI() {
        // zoom option
        JPanel zoom = new JPanel(new BorderLayout());
        zoom.add(new JLabel("Select PaintScale :"), BorderLayout.WEST);
        combo = new JComboBox(values);
        combo.addActionListener(this);
        combo.setEditable(true);
        zoom.add(combo, BorderLayout.CENTER);

        // Buttons
        JPanel buttons = new JPanel(new FlowLayout());
        JButton btn = new JButton(new AbstractAction("Apply") {
            public void actionPerformed(ActionEvent e) {
                imagepanel.setPaintScale(factor);
                setVisible(false);
            }
        });
        buttons.add(btn);
        btn = new JButton(new AbstractAction("Cancel") {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        buttons.add(btn);

        // creaet the interface
        getContentPane().add(zoom, BorderLayout.CENTER);
        getContentPane().add(buttons, BorderLayout.SOUTH);
    }

    /**
     * Checks the value of the radiobutton selected or the combobox. If the
     * combobox has a % assume it is a percentage, otherwise just a number.
     *
     * @param e the event that triggered this action.
     */
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof JRadioButtonMenuItem) {
            try {
                String val = e.getActionCommand();
                val = val.substring(0, val.length() - 1);
                factor = Double.parseDouble(val) / 100.0;
            } catch (NumberFormatException exc) {
                exc.printStackTrace();
            }
            imagepanel.setPaintScale(factor);
         } else {
            String val = combo.getSelectedItem().toString();
            if (val.endsWith("%")) {
                try {
                    val = val.substring(0, val.length() - 1);
                    factor = Double.parseDouble(val) / 100.0;
                } catch (NumberFormatException exc) {
                    exc.printStackTrace();
                }
            } else {
                try {
                    factor = Double.parseDouble(val);
                } catch (NumberFormatException exc) {
                    exc.printStackTrace();
                }
            }
        }
    }

    /**
     * Synchronize the radio button with the combobox.
     */
    private void update() {
        combo.setSelectedItem(factor * 100 + "%");
    }

    // ------------------------------------------------------------------------
    // Im2LearnMenu implementation
    // ------------------------------------------------------------------------
    public void setImagePanel(ImagePanel imagepanel) {
        this.imagepanel = imagepanel;
        imageUpdated(new ImageUpdateEvent(this, ImageUpdateEvent.NEW_IMAGE, null));
    }

    public JMenuItem[] getPanelMenuItems() {
        return new JMenuItem[]{zoom};
    }

    public JMenuItem[] getMainMenuItems() {
        return null;
    }

    public void imageUpdated(ImageUpdateEvent event) {
        switch (event.getId()) {
            case ImageUpdateEvent.NEW_IMAGE:
            case ImageUpdateEvent.CHANGE_PAINTSCALE:
                factor = imagepanel.getPaintScale();
                update();
                break;
        }
    }

    public URL getHelp(String menu) {
    	if (menu.equals("Zoom")) {
    		return getClass().getResource("help/zoom.html");
		}
    	return null;
    }
}
