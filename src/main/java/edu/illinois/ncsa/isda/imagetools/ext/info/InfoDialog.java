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
package edu.illinois.ncsa.isda.imagetools.ext.info;



import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.imagetools.core.display.Im2LearnFrame;
import edu.illinois.ncsa.isda.imagetools.core.display.Im2LearnMenu;
import edu.illinois.ncsa.isda.imagetools.core.display.ImagePanel;
import edu.illinois.ncsa.isda.imagetools.core.display.ImageUpdateEvent;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.URL;

/**
 * Show information about the image. This dialog will show the properties of the
 * image currently shown in the imagedialog. This will also show some of the
 * properties of the imagepanel itself.
 *
 * @author Rob Kooper
 * @version 1.0
 */
public class InfoDialog extends Im2LearnFrame implements Im2LearnMenu {
    private ImagePropertyTree proptree = new ImagePropertyTree();
    private JTextArea txtvalue = new JTextArea();
    private ImagePanel imagepanel = null;

	static private Log logger = LogFactory.getLog(InfoDialog.class);

	/**
     * Create the tree and ui for the info dialog.
     */
    public InfoDialog() {
        super("Image Info");
        getContentPane().setLayout(new BorderLayout());

        txtvalue.setTabSize(4);
        JScrollPane scrollpaneTxt = new JScrollPane(txtvalue);
        JScrollPane scrollpaneTree = new JScrollPane(proptree);
        JSplitPane splitpane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                              scrollpaneTree, scrollpaneTxt);
        getContentPane().add(splitpane, BorderLayout.CENTER);

        proptree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                txtvalue.setText(proptree.getNode((DefaultMutableTreeNode) proptree.getLastSelectedPathComponent()));
                txtvalue.setCaretPosition(0);
            }
        });

        scrollpaneTree.setPreferredSize(new Dimension(150, 300));
        scrollpaneTxt.setPreferredSize(new Dimension(250, 300));

		JMenuBar menubar = new JMenuBar();
		JMenu mnufile = new JMenu("File");
		menubar.add(mnufile);
		mnufile.add(new JMenuItem(new AbstractAction("Set Depth") {
            public void actionPerformed(ActionEvent e) {
				String depth = JOptionPane.showInputDialog(InfoDialog.this, "Number of items to show?", "" + proptree.getMaxDepth());
				if (depth != null) {
					try {
						int d = Integer.parseInt(depth);
						proptree.setMaxDepth(d);
				        proptree.reset();
				        imageUpdated(null);
					} catch (NumberFormatException exc) {
						logger.debug("not a number", exc);
					}
				}
			}
		}));
		mnufile.add(new JMenuItem(new AbstractAction("Close") {
            public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		}));
		
		setJMenuBar(menubar);

		pack();
    }

    /**
     * Update the tree to the latest information.
     */
    public void showing() {
        imageUpdated(null);
        toFront();
    }

    /**
     * Hide the tree and discard any information cached.
     */
    public void closing() {
        proptree.reset();
    }

    // ------------------------------------------------------------------------
    // Im2LearnMenu implementation
    // ------------------------------------------------------------------------
    public void setImagePanel(ImagePanel imagepanel) {
        this.imagepanel = imagepanel;
    }

    public JMenuItem[] getPanelMenuItems() {
        JMenuItem item = new JMenuItem(new AbstractAction("Image Info") {
            public void actionPerformed(ActionEvent e) {
                Window win = SwingUtilities.getWindowAncestor(imagepanel);
                setLocationRelativeTo(win);
                setVisible(true);
            }
        });
        return new JMenuItem[]{item};
    }

    public JMenuItem[] getMainMenuItems() {
        JMenuItem item = new JMenuItem(new AbstractAction("Image Info") {
            public void actionPerformed(ActionEvent e) {
                Window win = SwingUtilities.getWindowAncestor(imagepanel);
                setLocationRelativeTo(win);
                setVisible(true);
            }
        });
        JMenu menu = new JMenu("Help");
        menu.add(item);
        return new JMenuItem[]{menu};
    }

    public void imageUpdated(ImageUpdateEvent event) {
        if (isVisible()) {
            ImageObject imageobject = imagepanel.getImageObject();

            proptree.reset();
            if (imageobject != null) {
                proptree.addNode(imageobject);
            }
            proptree.addNode(imagepanel);
            txtvalue.setText(proptree.getNode(null));
            txtvalue.setCaretPosition(0);
        }
    }

    public URL getHelp(String topic) {
        // TODO Auto-generated method stub
        return null;
    }
}
