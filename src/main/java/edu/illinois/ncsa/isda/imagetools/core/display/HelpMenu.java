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

import edu.illinois.ncsa.isda.imagetools.core.Im2LearnUtilities;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * Help menu with default options enabled. What is displayed depends if this
 * runs as a D2K visualization or if this is the Im2Learn StandAlone application.
 *
 * @author Rob Kooper
 */
public class HelpMenu extends JMenu {
    public HelpMenu(boolean isD2K) {
        super("Help");
        setMnemonic('H');

        add(new JMenuItem(new HelpTopicsAction()));

        if (isD2K || !Im2LearnUtilities.isMACOS()) {
            addSeparator();
            add(new JMenuItem(new HelpAboutAction()));
        }

    }

    /**
     * Action to show more help options, not implemented.
     */
    class HelpTopicsAction extends AbstractAction {
        public HelpTopicsAction() {
            super("Help Topics");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_H));
            if (!Im2LearnUtilities.isMACOS()) {
                putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
            }
        }

        public void actionPerformed(ActionEvent e) {
            Window win = SwingUtilities.getWindowAncestor(HelpMenu.this);
            HelpViewer.showHelp(win);
        }
    }

    /**
     * Action to show the about box.
     */
    class HelpAboutAction extends AbstractAction {
        public HelpAboutAction() {
            super("About");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_A));
        }

        public void actionPerformed(ActionEvent e) {
            Window win = SwingUtilities.getWindowAncestor(HelpMenu.this);
            About.showAbout(win);
        }
    }
}
