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
package edu.illinois.ncsa.isda.im2learn.core.display;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;

import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URL;

/**
 * Edit menu. Allows for copy and paste of images.
 */
public class EditMenu extends JMenu {
    private ImagePanel imagepanel;
    private int shortcutKeyMask;
    private JMenuItem mnuCopy;
    private JMenuItem mnuPaste;
    private Clipboard clipboard;

    static private Log logger = LogFactory.getLog(FileMenu.class);

    public EditMenu(ImagePanel imagepanel, boolean isD2K) {
        super("Edit");
        setMnemonic('E');

        this.imagepanel = imagepanel;
        shortcutKeyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

        mnuCopy = new JMenuItem(new EditCopyAction());
        add(mnuCopy);

        if (!isD2K) {
            mnuPaste = new JMenuItem(new EditPasteAction());
            add(mnuPaste);
        } else {
            mnuPaste = null;
        }

        addMenuListener(new MenuListener() {
            public void menuSelected(MenuEvent e) {
                checkMenu();
            }

            public void menuDeselected(MenuEvent e) {
            }

            public void menuCanceled(MenuEvent e) {
            }
        });
    }

    /**
     * Enables and disabled certain menu entries based on content of clipboard
     * and the imagepanel.
     */
    private void checkMenu() {
        // check copy
        if (imagepanel.getImageObject() != null) {
            mnuCopy.setEnabled(true);
        } else {
            mnuCopy.setEnabled(false);
        }

        // check paste
        if (mnuPaste != null) {
            if (isImage()) {
                mnuPaste.setEnabled(true);
            } else {
                mnuPaste.setEnabled(false);
            }
        }
    }

    /**
     * Returns true if there is an image on the clipboard.
     *
     * @return true for image.
     */
    private boolean isImage() {
        Transferable cntnts = clipboard.getContents(null);
        if (cntnts == null) {
            return false;
        }
        return cntnts.isDataFlavorSupported(DataFlavor.imageFlavor);
    }

    /**
     * Class to do copy.
     */
    class EditCopyAction extends AbstractAction {
        public EditCopyAction() {
            super("Copy");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_C));
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C,
                                                             shortcutKeyMask));
        }

        public void actionPerformed(ActionEvent e) {
            ImageObject imgobj = imagepanel.getImageObject();
            if (imgobj == null) {
                return;
            }
            ImageSelection imgSel = new ImageSelection(imagepanel.getImage());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(imgSel, null);
        }
    }

    /**
     * Class to do paste.
     */
    class EditPasteAction extends AbstractAction {
        public EditPasteAction() {
            super("Paste");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_V));
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_V,
                                                             shortcutKeyMask));
        }

        public void actionPerformed(ActionEvent e) {
            if (!isImage()) {
                return;
            }
            ProgressBlocker blocker = new ProgressBlocker("Copying image from clipboard ...");
            blocker.showDialog(new ProgressBlockerRun() {
                public void run(ProgressBlocker blocker) throws Exception {
                    if (!isImage()) {
                        return;
                    }

                    try {
                        Transferable cntnts = clipboard.getContents(null);
                        if (cntnts == null) {
                            logger.debug("Image no longer available.");
                            return;
                        }
                        Image image = (Image) cntnts.getTransferData(DataFlavor.imageFlavor);
                        ImageObject imgobj = ImageObject.getImageObject(image);
                        imagepanel.setImageObject(imgobj);
                    } catch (UnsupportedFlavorException exc) {
                        logger.debug("Image no longer available.", exc);
                    } catch (IOException exc) {
                        logger.warn("Clipboard not ready?.", exc);
                    }
                }
            });
        }
    }

    // This class is used to hold an image while on the clipboard.
    public static class ImageSelection implements Transferable {
        private Image image;

        public ImageSelection(Image image) {
            this.image = image;
        }

        // Returns supported flavors
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{DataFlavor.imageFlavor};
        }

        // Returns true if flavor is supported
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return DataFlavor.imageFlavor.equals(flavor);
        }

        // Returns image
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (!DataFlavor.imageFlavor.equals(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            return image;
        }
    }
}
