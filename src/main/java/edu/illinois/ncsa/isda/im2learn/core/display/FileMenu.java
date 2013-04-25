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


import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.im2learn.core.Im2LearnUtilities;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.im2learn.core.io.FileChooser;
import edu.illinois.ncsa.isda.im2learn.core.io.LoadSaveImagePanel;
import edu.illinois.ncsa.isda.im2learn.core.io.SubFile;

/**
 * File menu with default options enabled. What is displayed depends if this
 * runs as a D2K visualization or if this is the Im2Learn StandAlone application.
 *
 * @author Rob Kooper
 */
public class FileMenu extends JMenu {
    static private Log logger = LogFactory.getLog(FileMenu.class);
    private int shortcutKeyMask;
    private ImagePanel imagepanel;
    private Object[] printer;
    private JMenuItem mnuPageSetup;
    private JMenuItem mnuPrint;

    public FileMenu(ImagePanel imagepanel, boolean isD2K) {
        super("File");

        setMnemonic('F');

        shortcutKeyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        this.imagepanel = imagepanel;

        if (!isD2K) {
            add(new JMenuItem(new FileNewAction()));
            add(new JMenuItem(new FileOpenAction()));
            add(new JMenuItem(new FileOpenMultipleAction()));
            add(new JMenuItem(new FileOpenURLAction()));
            add(new JMenuItem(new FileOpenSpecialAction()));
            add(new JMenuItem(new FileSaveAction()));
        }
        add(new JMenuItem(new FileSaveAsAction()));

        addSeparator();
        mnuPageSetup = new JMenuItem(new FilePageSetupAction());
        mnuPrint = new JMenuItem(new FilePrintAction());
        add(mnuPageSetup);
        add(mnuPrint);

        addSeparator();
        add(new JMenuItem(new FileCloseAction()));
        if (!isD2K && !Im2LearnUtilities.isMACOS()) {
            add(new JMenuItem(new FileExitAction()));
        }
    }

    private boolean checkPrinter() {
        if (printer != null) {
            return true;
        }

        try {
            PrinterJob pj = PrinterJob.getPrinterJob();
            PageFormat pf = pj.defaultPage();

            printer = new Object[]{pj, pf};
            return true;
        } catch (Exception exc) {
            logger.warn("Error setting up printing, no printing.");
            mnuPageSetup.setEnabled(false);
            mnuPrint.setEnabled(false);
            return false;
        }
    }

    /**
     * Action to create a new instance of the mainframe.
     */
    class FileNewAction extends AbstractAction {
        public FileNewAction() {
            super("New");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_N));
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N,
                                                             shortcutKeyMask));
        }

        public void actionPerformed(ActionEvent e) {
        	Im2LearnMainFrame frame = (Im2LearnMainFrame) SwingUtilities.getWindowAncestor(FileMenu.this);
        	try {
				frame.getClass().newInstance().setVisible(true);
			} catch (InstantiationException e1) {
				logger.error("could not create a new frame",e1);
			} catch (IllegalAccessException e1) {
				logger.error("could not create a new frame",e1);
			}
        }
    }

    /**
     * Action to open a new file.
     */
    class FileOpenAction extends AbstractAction {
        public FileOpenAction() {
            super("Open...");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_O));
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O,
                                                             shortcutKeyMask));
        }

        public void actionPerformed(ActionEvent e) {
            try {
                FileChooser chooser = new FileChooser();
                String filename = chooser.showOpenDialog();
                String filter = chooser.getFilter();
                if (filename != null) {
                    LoadSaveImagePanel.load(filename, imagepanel, filter);
                }
            } catch (IOException exc) {
                logger.error("Error loading file.", exc);
            }
        }
    }

    /**
     * Action to open a new file.
     */
    class FileOpenMultipleAction extends AbstractAction {
        public FileOpenMultipleAction() {
            super("Open Multiple...");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_M));
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_M,
                                                             shortcutKeyMask));
        }

        public void actionPerformed(ActionEvent e) {
            try {
                FileChooser chooser = new FileChooser();
                String[] filename = chooser.showMultiOpenDialog();
                String filter = chooser.getFilter();
                if (filename != null) {
                    LoadSaveImagePanel.load(filename, imagepanel, filter);
                }
            } catch (IOException exc) {
                logger.error("Error loading file.", exc);
            }
        }
    }

    /**
     * Action to open a new file.
     */
    class FileOpenURLAction extends AbstractAction {
        public FileOpenURLAction() {
            super("Open URL...");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_U));
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_U,
                                                             shortcutKeyMask));
        }

        public void actionPerformed(ActionEvent e) {
            String url = JOptionPane.showInputDialog("Type in URL of file to load :");
            if (url != null) {
                LoadSaveImagePanel.load(url, imagepanel);
            }
        }
    }
    
    /**
     * Action to open a new file, allowing selection for subarea and
     * subsampling.
     */
    class FileOpenSpecialAction extends AbstractAction {
        public FileOpenSpecialAction() {
            super("Open Special...");
        }

        public void actionPerformed(ActionEvent e) {
            Frame owner = (Frame) SwingUtilities.getWindowAncestor(FileMenu.this);
            try {
                FileChooser chooser = new FileChooser();
                String filename = chooser.showOpenDialog();
                String filter = chooser.getFilter();
                if (filename != null) {
                    SubFile sf = new SubFile(owner);
                    sf.setVisible(true);
                    if (!sf.isCancelled()) {
                        LoadSaveImagePanel.load(filename, imagepanel, filter, sf);
                    }
                }
            } catch (Exception exc) {
                logger.error("Error loading file.", exc);
            }
        }
    }

    /**
     * Action to save the current imagepanel back to the same file, not
     * implmented.
     */
    class FileSaveAction extends AbstractAction {
        public FileSaveAction() {
            super("Save");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_S));
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S,
                                                             shortcutKeyMask));
        }

        public void actionPerformed(ActionEvent e) {
            // TODO implement
            Frame owner = (Frame) SwingUtilities.getWindowAncestor(FileMenu.this);
            JOptionPane.showMessageDialog(owner, "Not implmented.");
            logger.debug(e);
        }
    }

    /**
     * Action to save the current imagepanel to a file with a different
     * filename.
     */
    class FileSaveAsAction extends AbstractAction {
        public FileSaveAsAction() {
            super("Save As...");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_A));
        }

        public void actionPerformed(ActionEvent e) {
            if (imagepanel.getImageObject() == null) {
                return;
            }
            try {
                FileChooser chooser = new FileChooser();
                String filename = chooser.showSaveDialog();
                String filter = chooser.getFilter();
                if (filename != null) {
                    LoadSaveImagePanel.save(filename, imagepanel, filter);
                }
            } catch (IOException exc) {
                logger.error("Error saving file.", exc);
            }
        }
    }

    /**
     * Action to setup the pageformat.
     */
    class FilePageSetupAction extends AbstractAction {
        public FilePageSetupAction() {
            super("Page Setup");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_P));
        }

        public void actionPerformed(ActionEvent e) {
            if (!checkPrinter()) {
                return;
            }
            PrinterJob pj = (PrinterJob)printer[0];
            PageFormat pf = (PageFormat)printer[1];
            printer[1] = pj.pageDialog(pf);
        }
    }

    /**
     * Action to print the current imagepanel to a printer.
     */
    class FilePrintAction extends AbstractAction {
        public FilePrintAction() {
            super("Print");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_P));
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P,
                                                             shortcutKeyMask));
        }

        public void actionPerformed(ActionEvent e) {
            if (!checkPrinter()) {
                return;
            }
            PrinterJob pj = (PrinterJob)printer[0];
            PageFormat pf = (PageFormat)printer[1];

            pj.setPrintable(imagepanel, pf);
            pj.setJobName("Im2Learn");
            ImageObject imgobj = imagepanel.getImageObject();
            if (imgobj != null) {
                String name = (String) imgobj.getProperty(ImageObject.FILENAME);
                if (name != null) {
                    File file = new File(name);
                    pj.setJobName(file.getName());
                }
            }
            if (pj.printDialog()) {
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            ((PrinterJob)printer[0]).print();
                        } catch (PrinterException exc) {
                            logger.error("Error printing image.", exc);
                        }
                    }
                }).start();
            }
        }
    }

    /**
     * Action to close the window.
     */
    class FileCloseAction extends AbstractAction {
        public FileCloseAction() {
            super("Close");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_C));
        }

        public void actionPerformed(ActionEvent e) {
            SwingUtilities.getWindowAncestor(imagepanel).setVisible(false);
        }
    }

    /**
     * Action to kill Im2Learn. This will exit the application.
     */
    class FileExitAction extends AbstractAction {
        public FileExitAction() {
            super("Exit");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_X));
        }

        public void actionPerformed(ActionEvent e) {
            setVisible(false);
            System.exit(0);
        }
    }
}
