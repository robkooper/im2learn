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
package edu.illinois.ncsa.isda.imagetools.ext.test;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URL;

import javax.swing.*;

import edu.illinois.ncsa.isda.imagetools.core.display.Im2LearnMenu;
import edu.illinois.ncsa.isda.imagetools.core.display.ImagePanel;
import edu.illinois.ncsa.isda.imagetools.core.display.ImageUpdateEvent;


public class ProvenanceDialog implements Im2LearnMenu, AWTEventListener {
    private JCheckBoxMenuItem chkDoIt;
    private PrintStream       ps;

    public ProvenanceDialog() {        
    }
    
    public void setImagePanel(ImagePanel imagepanel) {
    }

    public JMenuItem[] getPanelMenuItems() {
        return null;
    }

    public JMenuItem[] getMainMenuItems() {
        JMenu menu = new JMenu("Tools");
        chkDoIt = new JCheckBoxMenuItem(new AbstractAction("Provenance") {

            public void actionPerformed(ActionEvent e) {
                if (chkDoIt.isSelected()) {
                    try {
                        ps = new PrintStream(new FileOutputStream("output.txt", true));
                    } catch (IOException exc) {
                        ps = System.out;
                    }
                    Toolkit.getDefaultToolkit().addAWTEventListener(ProvenanceDialog.this, Long.MAX_VALUE);                    
                } else {
                    ps = null;
                    Toolkit.getDefaultToolkit().removeAWTEventListener(ProvenanceDialog.this);
                }
            }
            
        });
        
        menu.add(chkDoIt);
        return new JMenuItem[]{menu};
    }

    public void imageUpdated(ImageUpdateEvent event) {
        if (chkDoIt.isSelected()) {
            ps.println(event);
        }
    }

    public void eventDispatched(AWTEvent event) {
        if (ps == null) {
            return;
        }
        if ((event instanceof MouseEvent) && (((MouseEvent)event).getID() == MouseEvent.MOUSE_MOVED)) {
            return;
        } else {
            Object  src = event.getSource();
            if (src instanceof Component) {
                Window win = SwingUtilities.getWindowAncestor((Component)src);
                if (win != null) {
                    ps.println(win.getClass().toString() + " " + event.toString());
                } else {
                    ps.println(event);
                }
            } else {
                ps.println(event);
            }
        }
    }

    public URL getHelp(String topic) {
        // TODO Auto-generated method stub
        return null;
    }
}
