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


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.imagetools.core.Im2LearnUtilities;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.imagetools.core.io.FileChooser;
import edu.illinois.ncsa.isda.imagetools.core.io.ImageLoader;
import edu.illinois.ncsa.isda.imagetools.core.io.LoadSaveImagePanel;

import javax.swing.*;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.IOException;

/**
 * This is a very basic Im2Learn frame. It will just display open, save as, exit and
 * help and about. To be able to use other tools, extend this class and add them
 * to the menu.
 */
public abstract class Im2LearnMainFrame extends Im2LearnFrame implements DropTargetListener, ImageUpdateListener {
    private ImagePanel imagepanel = null;
    private MultiImagePanel mip = null;
    private ChildWindowHandler childwindows = null;
	private JLabel lblMouse = null;

    protected SplashScreen splash = null;

    static private int framecounter = 0;
    static private Log logger = LogFactory.getLog(Im2LearnMainFrame.class);

    /**
     * Default constructor, create frame with Im2Learn image.
     */
    public Im2LearnMainFrame() {
        this((String)null);
    }

    /**
     * Create the ncsa.im2learn.main frame that will contain all the menus. It will
     * try and load the given image and display it.
     *
     * @param ImageObject
     */
    public Im2LearnMainFrame(ImageObject image) {
        super("Im2Learn");
        synchronized (Im2LearnMainFrame.class) {
            framecounter++;
        }

        childwindows = new ChildWindowHandler();

        splash = new SplashScreen();
        if (framecounter == 1) {
            splash.setVisible(true);
        }

        // show version of Im2Learn
        logger.info("Im2Learn v" + About.getVersion() + " (build " + About.getBuild() + ")");

        // find right image
        if (image == null) {
            image = Im2LearnUtilities.createEmptyImage();
        }
        imagepanel = new ImagePanel();
        imagepanel.addImageUpdateListener(this);
        imagepanel.setAutozoom(true);
        mip = new MultiImagePanel(imagepanel);
        createUI();
        imagepanel.setImageObject(image);

        new DropTarget(this, DnDConstants.ACTION_COPY, this);

        // add special window handlers
        addWindowListener(childwindows);

        // add the special menus
        try {
            splash.feedback("Adding tools ...");
            addMenus();
        } catch (Throwable thr) {
            logger.warn("Uncaught exception.", thr);
        }

        // finally show the application
        setLocationRelativeTo(null);
        setVisible(true);

        if (framecounter == 1) {
            splash.setVisible(false);
        }
    }
    
    
    /**
     * Create the ncsa.im2learn.main frame that will contain all the menus. It will
     * try and load the given image and display it.
     *
     * @param filename
     */
    public Im2LearnMainFrame(String filename) {
        super("Im2Learn");
        synchronized (Im2LearnMainFrame.class) {
            framecounter++;
        }

        childwindows = new ChildWindowHandler();

        splash = new SplashScreen();
        if (framecounter == 1) {
            splash.setVisible(true);
        }

        // show version of Im2Learn
        logger.info("Im2Learn v" + About.getVersion() + " (build " + About.getBuild() + ")");

        // find right image
        ImageObject image = null;
        if (filename != null) {
            splash.feedback("Loading image ...");
            try {
                image = ImageLoader.readImage(filename);
            } catch (IOException exc) {
                logger.error("Error loading " + filename, exc);
            }
        }
        if (image == null) {
            image = Im2LearnUtilities.createEmptyImage();
        }
        imagepanel = new ImagePanel();
        imagepanel.addImageUpdateListener(this);
        imagepanel.setAutozoom(true);
        mip = new MultiImagePanel(imagepanel);
        createUI();
        imagepanel.setImageObject(image);

        new DropTarget(this, DnDConstants.ACTION_COPY, this);

        // add special window handlers
        addWindowListener(childwindows);

        // add the special menus
        try {
            splash.feedback("Adding tools ...");
            addMenus();
        } catch (Throwable thr) {
            logger.warn("Uncaught exception.", thr);
        }

        // finally show the application
        setLocationRelativeTo(null);
        setVisible(true);

        if (framecounter == 1) {
            splash.setVisible(false);
        }
    }

    public void closing() {
        imagepanel.setImageObject(null);
        synchronized (Im2LearnMainFrame.class) {
            framecounter--;
        }
        if (framecounter == 0) {
            System.exit(0);
        }
    }

    /**
     * Add any menus to the standard Im2Learn toolbar.
     */
    abstract public void addMenus();

    /**
     * Return the imagepanel that is contained in this frame.
     *
     * @return the imagepanel.
     */
    public ImagePanel getImagePanel() {
        return imagepanel;
    }

    public MultiImagePanel getMultiImagePanel() {
    	return mip;
	}


    /**
     * Create the ncsa.im2learn.main layout. This will add the menubar to the frame
     * and add the imagepanel to a scrollpane which is added to the
     * ncsa.im2learn.main panel.
     */
    protected void createUI() {
        createMenuBar();

        //Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        //dim.width *= 0.9;
        //dim.height *= 0.9;
        //MinMaxLayout layout = new MinMaxLayout();
        //layout.setMaximum(dim);
        //getContentPane().setLayout(layout);

        int vsc = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED;
        int hsc = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED;
        if (Im2LearnUtilities.isMACOS()) {
            vsc = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS;
            hsc = JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS;
        }
        JScrollPane scrollpane = new JScrollPane(imagepanel, vsc, hsc);
        scrollpane.setBorder(null);

        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 2));
        getContentPane().add(pnl, BorderLayout.SOUTH);

		lblMouse = new JLabel("x:0 y:0");
		pnl.add(lblMouse);
		pnl.add(new JMemoryViewer(120));
		
		imagepanel.addMouseMotionListener(new MouseMotionListener() {
			Point last = new Point();
			
		    public void mouseDragged(MouseEvent e) {
				Point pt = imagepanel.getMouseLocation(e.getPoint());
				int w = (int)Math.abs(pt.x - last.x);
				int h = (int)Math.abs(pt.y - last.y);
				lblMouse.setText("x:" + last.x + " y:" + last.y + " w: " + w + " h: " + h);
		    }
			
		    public void mouseMoved(MouseEvent e) {				
				last = imagepanel.getMouseLocation(e.getPoint());
				lblMouse.setText("x:" + last.x + " y:" + last.y);
		    }
			
		});
		
        add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mip, scrollpane), BorderLayout.CENTER);
        pack();
    }

    /**
     * Create the ncsa.im2learn.main menubar. This will add the file and help
     * submenus to the the menubar.
     */
    protected void createMenuBar() {
        JMenuBar menubar = new JMenuBar();

        menubar.add(new FileMenu(imagepanel, false));
        menubar.add(new EditMenu(imagepanel, false));
       

        menubar.add(new HelpMenu(false));
        //menubar.setHelpMenu(menu);

        // Add the menubar
        setJMenuBar(menubar);
    }

    // ----------------------------------------------------------------------
    // ImageUpdate Interface
    // ----------------------------------------------------------------------
    public void imageUpdated(ImageUpdateEvent event) {
        // if a new image is loaded, set the title to the filename.
        if (event.getId() == ImageUpdateEvent.NEW_IMAGE) {
            ImageObject imgobj = imagepanel.getImageObject();
            String filename = null;
            if (imgobj != null) {
                filename = (String) imgobj.getProperty(ImageObject.FILENAME);
            }
            if (filename == null) {
                setTitle("Im2Learn");
            } else {
                setTitle("Im2Learn [" + filename + "]");
            }
        }
    }

    // ----------------------------------------------------------------------
    // User Menu
    // ----------------------------------------------------------------------
    /**
     * Add the menu associated with the dialog to the panel, and to the
     * ncsa.im2learn.main menu bar. This will try and reuse any of the menubar
     * items. Adding of an option by two tools to the Tools menu will result in
     * a single Tools menu with two entries beneath it.
     *
     * @param menu for the tool to be added.
     */
    public void addMenu(Im2LearnMenu menu) {
        if (splash != null) {
            splash.feedback("Adding " + menu.getClass().getName());
        }

        try {
        	// first add help
            HelpViewer.addHelp(menu);

            imagepanel.addMenu(menu);

            JMenuItem items[] = menu.getMainMenuItems();
            if (items != null) {
                for (int i = 0; i < items.length; i++) {
                    Im2LearnUtilities.addSubMenu(getJMenuBar(), (JMenuItem) items[i]);
                }
            }
        } catch (Throwable thr) {
            logger.warn("Uncaught exception.", thr);
        }

        if (menu instanceof Window) {
            childwindows.add((Window) menu);
        }

        validate();
    }

    // ----------------------------------------------------------------------
    // DRAG & DROP CAPABILITY
    // ----------------------------------------------------------------------

    public void dragEnter(DropTargetDragEvent dropTargetDragEvent) {
    }

    public void dragExit(DropTargetEvent dropTargetEvent) {
    }

    public void dragOver(DropTargetDragEvent dropTargetDragEvent) {
    }

    public void dropActionChanged(DropTargetDragEvent dropTargetDragEvent) {
    }

    public void drop(DropTargetDropEvent dropTargetDropEvent) {
        try {
            Transferable tr = dropTargetDropEvent.getTransferable();
            if (tr.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                dropTargetDropEvent.acceptDrop(DnDConstants.ACTION_COPY);
                String filename = (String) tr.getTransferData(DataFlavor.stringFlavor);
                // discard anything after \n and load file.
                LoadSaveImagePanel.load(filename.split("\n")[0], imagepanel);
                dropTargetDropEvent.getDropTargetContext().dropComplete(true);
            } else if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                dropTargetDropEvent.acceptDrop(DnDConstants.ACTION_COPY);
                java.util.List filenames = (java.util.List) tr.getTransferData(DataFlavor.javaFileListFlavor);
                String filename = filenames.get(0).toString();
				filename = FileChooser.check(filename, true);
                LoadSaveImagePanel.load(filename, imagepanel);
                dropTargetDropEvent.getDropTargetContext().dropComplete(true);
            } else {
                dropTargetDropEvent.rejectDrop();
            }
        } catch (IOException io) {
            io.printStackTrace();
            dropTargetDropEvent.rejectDrop();
        } catch (UnsupportedFlavorException ufe) {
            ufe.printStackTrace();
            dropTargetDropEvent.rejectDrop();
        }
    }
}
