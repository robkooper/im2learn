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

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Iterator;
import java.util.Vector;

/**
 * Make any window behave like a dialog. When closing the parent window this
 * class will close all windows that are added to this class. If the window is
 * minimized this class will hide all child windows. De-iconifing the window
 * will make all child windows visible again.
 * <p/>
 * To use this class, create an instance and add it as a windowListener. All
 * windows that are created in this window should be added to this class.
 * <p/>
 * This class will only know about any windows it is told about, it will not
 * know about any child windows. To close child windows as well, the ncsa.im2learn.main window
 * will need to use an instance of this class as well.
 *
 * @author Rob Kooper
 * @version 1.0
 */
public class ChildWindowHandler extends WindowAdapter {
    private Vector childwindows;

    /**
     * simple constructor.
     */
    public ChildWindowHandler() {
        childwindows = new Vector();
    }

    /**
     * Add the window to the list of sub-windows.
     */
    public void add(Window window) {
        if ((window != null) && !childwindows.contains(window)) {
            childwindows.add(new ChildWindow(window));
        }
    }

    /**
     * Remove the window from the list of subwindows.
     */
    public void remove(Window window) {
        if (window != null) {
            childwindows.remove(window);
        }
    }

    /**
     * Hide all the sub-windows.
     */
    public void windowClosing(WindowEvent e) {
        for (Iterator iter = childwindows.iterator(); iter.hasNext();) {
            ChildWindow child = (ChildWindow) iter.next();
            child.visible = false;
            child.state = Frame.NORMAL;
            child.window.setVisible(false);
        }
    }

    /**
     * Store original state, iconify and hide the window.
     */
    public void windowIconified(WindowEvent e) {
        for (Iterator iter = childwindows.iterator(); iter.hasNext();) {
            ChildWindow child = (ChildWindow) iter.next();
            child.visible = child.window.isVisible();
            if (child.window instanceof Frame) {
                child.state = ((Frame) child.window).getState();
                ((Frame) child.window).setState(Frame.ICONIFIED);
            }
            child.window.setVisible(false);
        }
    }

    /**
     * De-iconify all the sub-windows, restoring the original state.
     */
    public void windowDeiconified(WindowEvent e) {
        for (Iterator iter = childwindows.iterator(); iter.hasNext();) {
            ChildWindow child = (ChildWindow) iter.next();
            if (child.visible) {
                if (child.window instanceof Frame) {
                    ((Frame) child.window).setState(child.state);
                }
                child.window.setVisible(true);
            }
        }
    }

    /**
     * Little class to remember the original state of the window.
     */
    class ChildWindow {
        public Window window = null;
        public boolean visible = false;
        public int state = Frame.NORMAL;

        public ChildWindow(Window window) {
            this.window = window;
        }
    }
}
