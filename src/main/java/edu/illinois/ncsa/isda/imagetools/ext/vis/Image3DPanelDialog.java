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
package edu.illinois.ncsa.isda.imagetools.ext.vis;

import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.NumberFormatter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImPoint;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.imagetools.core.display.*;
import edu.illinois.ncsa.isda.imagetools.core.io.FileChooser;
import edu.illinois.ncsa.isda.imagetools.core.io.ImageLoader;
import edu.illinois.ncsa.isda.imagetools.ext.info.InfoDialog;
import edu.illinois.ncsa.isda.imagetools.ext.misc.ImEnhance;
import edu.illinois.ncsa.isda.imagetools.ext.misc.ImageFrame;


import java.awt.BorderLayout;


import java.awt.GridLayout;
import java.awt.Point;

import java.awt.Window;
import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.text.ParseException;

import java.util.Hashtable;
import java.util.Enumeration;



public class Image3DPanelDialog extends Im2LearnFrame /*implements Im2LearnMenu*/ {
	
	ImagePanel imagepanel;
	
    JRadioButton jb_mode_gray, jb_mode_3band, jb_mode_pseudo, 
    jb_mode_map, jb_mode_3bandmap, jb_mode_pseudomap; 
    JSlider js_band;
    
    public Image3DPanelDialog() {
    	super("Image3DPanel");
        createUI();
    }
    
    protected void createUI() {
    	JPanel menuPanel = new JPanel(new GridLayout(5,1));
    	JPanel modePanel = new JPanel(new GridLayout(6,1));
    	JPanel optionPanel = new JPanel(new GridLayout(2,1));
    	
    	jb_mode_gray = new JRadioButton("GrayScale 3D");
    	jb_mode_pseudo = new JRadioButton("Pseudo colored 3D");
    	jb_mode_3band = new JRadioButton("3Band plane");
    	jb_mode_map = new JRadioButton("GrayScale 3D Map");
    	jb_mode_3bandmap = new JRadioButton("3 Band 3D Map");
    	jb_mode_pseudomap = new JRadioButton("Pseudo colored 3D Map");
    	
    	jb_mode_gray.setSelected(true);
    	
    	//Group the radio buttons.
	    ButtonGroup group = new ButtonGroup();
	    group.add(jb_mode_gray);
	    group.add(jb_mode_pseudo);
	    group.add(jb_mode_3band);
	    group.add(jb_mode_map);
	    group.add(jb_mode_3bandmap);
	    group.add(jb_mode_pseudomap);
	    
	    modePanel.setBorder(BorderFactory.createTitledBorder("Mode"));
	    modePanel.add(jb_mode_gray);
	    
	    modePanel.add(jb_mode_pseudo);
	    modePanel.add(jb_mode_3band);
	    modePanel.add(jb_mode_map);
	    modePanel.add(jb_mode_3bandmap);
	    modePanel.add(jb_mode_pseudomap);
	    
	    optionPanel.add(new JLabel("Band"));
	    
	    js_band = new JSlider(JSlider.HORIZONTAL, 0, 3, 0);
	    js_band.setPaintTicks(true);
	    js_band.setPaintLabels(true);
	    optionPanel.add(js_band);
	    
	    menuPanel.add(modePanel);
	    menuPanel.add(optionPanel);
	    
    	add(menuPanel, BorderLayout.EAST);
    	pack();
    }

	/**
	 * 
	 */
	protected void done() {
      
	}

	/**
	 * 
	 */
//	protected void save() {
//	       // copy the labels into properties
//   
//	}

	/**
	 * 
	 /**
	 * 
	 */
	protected void reset() {
		
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
        JMenu tools = new JMenu("Visualization");

        JMenuItem image3DPanel = new JMenuItem(new AbstractAction("3D Visiaulization") {
            public void actionPerformed(ActionEvent e) {
                if (!isVisible()) {
                    Window win = SwingUtilities.getWindowAncestor(imagepanel);
                    setLocationRelativeTo(win);
                    setVisible(true);
                    reset();
                }
                toFront();
            }
        });
        tools.add(image3DPanel);

        return new JMenuItem[]{tools};
    }

    public void imageUpdated(ImageUpdateEvent event) {
    	
    }
    
    public URL getHelp(String menu) {
        return getClass().getResource("");
    }
    
}
