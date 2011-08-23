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
package edu.illinois.ncsa.isda.imagetools.core.display;

import javax.swing.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

/**
 * Special frame with Im2Learn icon. This class will also have 3 functions that are
 * useful, showing, hiding and closing which will be called if the window is
 * shown, hidden or closed.
 *
 * @author Rob Kooper
 * @version 1.0
 */
public class Im2LearnFrame extends JFrame {
    static private String iconname = "icon.gif";
    static private ImageIcon icon = null;

    /**
     * Load the Im2Learn icon.
     */
    static {
        try {
            icon = new ImageIcon(Im2LearnFrame.class.getResource(iconname));
        } catch (Exception exc) { }
    }

    /**
     * Create a frame with no title and the icon of the window will be the Im2Learn
     * icon.
     */
    public Im2LearnFrame() {
        this("");
    }

    /**
     * Create a frame with the given title and the icon of the window will be
     * the Im2Learn icon.
     *
     * @param title the title of the window.
     */
    public Im2LearnFrame(String title) {
        super(title != null ? title : "");

        // set to our icon instead of coffee cup
        if (icon != null) {
            setIconImage(icon.getImage());
        }

        // always dispose the window by default
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // add listeners to catch show, hide and close
        addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent e) {
                showing();
                fireWindowShowing(Im2LearnFrame.this);
            }

            public void componentHidden(ComponentEvent e) {
                closing();
                fireWindowHiding(Im2LearnFrame.this);
            }
        });

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                closing();
                if(getDefaultCloseOperation() == JFrame.DISPOSE_ON_CLOSE)
                	fireWindowClosing(Im2LearnFrame.this);
            }
        });
        
        fireWindowCreate(this);
    }

    /**
     * Called after the window is made visible. This can be used to create
     * threads to animate things, or to fill in defaults based on the current
     * image shown.
     */
    public void showing() {
    }

    /**
     * This is called when the window is closed. This signals the end of this
     * window and any cleanup should happen here.
     */
    public void closing() {
    }
    
    static ArrayList<Im2LearnFrameListener> listeners = new ArrayList<Im2LearnFrameListener>();
    static public void addListener(Im2LearnFrameListener l) {
    	if (!listeners.contains(l)) {
    		listeners.add(l);
    	}
    }
    
    static public void fireWindowCreate(Im2LearnFrame frame) {
    	for(Im2LearnFrameListener l : listeners) {
    		l.windowCreate(frame);
    	}
    }

    static public void fireWindowShowing(Im2LearnFrame frame) {
    	for(Im2LearnFrameListener l : listeners) {
    		l.windowShowing(frame);
    	}
    }

    static public void fireWindowHiding(Im2LearnFrame frame) {
    	for(Im2LearnFrameListener l : listeners) {
    		l.windowHiding(frame);
    	}
    }

    static public void fireWindowClosing(Im2LearnFrame frame) {
    	for(Im2LearnFrameListener cl : listeners) {
    		cl.windowClosing(frame);
    	}
    }
}
