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

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;

/**
 * This class extends the basic imagecomponent and allows the addition of
 * annotations to the imagecomponent. One of these annotations is the ability to
 * draw rubberbands. Other annotations have to implement the ImageAnnotation
 * interface and can be added to the imagepanel. They will be drawn in the order
 * in which they are added to the image.
 */
public class ImagePanel extends ImageComponent {
    private ArrayList annotationsImage;
    private ArrayList annotationsPanel;

    private ImageMarker selection;
    private Point start;
    private boolean rubberband;
    private boolean selectionAllowed = true;
    private boolean pressedvalid = false;

    private JPopupMenu popupmenu = new JPopupMenu("");

    /**
     * Default constructor called with no image.
     */
    public ImagePanel() {
        this(null);
    }

    /**
     * Create an imageobject with the given imageobject and adds the mouse
     * listeners for the rubberbanding and point selection.
     *
     * @param imageobject initial imageobject for the imagepanel.
     */
    public ImagePanel(ImageObject imageobject) {
        super(imageobject);

        if (selection == null) {
            initialize();
        }
        addMouseListener(new MyMouseListener());
        addMouseMotionListener(new MyMouseMotionListner());
    }

    /**
     * Sets the imageobject of the imagepanel. This will remove the rubberband,
     * and clear all annotations.
     *
     * @param imageobject new imageobject to be displayed.
     */
    public void setImageObject(ImageObject imageobject) {
        if (selection == null) {
            initialize();
        }
        selection.setVisible(false);
        annotationsImage.clear();
        super.setImageObject(imageobject);
    }

    /**
     * Initialize the variables used in this class.
     */
    private void initialize() {
        annotationsImage = new ArrayList();
        annotationsPanel = new ArrayList();
        selection = new ImageMarker();
        selection.setColor(Color.RED);
        start = new Point();
        rubberband = false;
        selectionAllowed = false;
        pressedvalid = false;
    }

    /**
     * @deprecated use addAnnotationImage
     */
    public void addAnnotation(ImageAnnotation annotation) {
        addAnnotationImage(annotation);
    }

    /**
     * Add a new annotation to the image. This function will add the annotation
     * to the list of annotations associated with the image only if it is not
     * null and has not been added. If added it will fire an event to all
     * listeners of imageupdate with the annotation.
     *
     * @param annotation the annotation to be added to the list.
     */
    public void addAnnotationImage(ImageAnnotation annotation) {
        if ((annotation != null) && !annotationsImage.contains(annotation)) {
            synchronized (getTreeLock()) {
                annotationsImage.add(annotation);
            }
            fireImageUpdate(ImageUpdateEvent.ADD_ANNOTATION, annotation);
            repaint();
        }
    }

    /**
     * Add a new annotation to the panel. This annotation will still be there
     * after a setImageObject, unlike addAnnotationImage. This function will
     * only add the annotation if it is not null, and it has not been added. If
     * added it will fire an event to all listeners of imageupdate with the
     * annotation.
     *
     * @param annotation the annotation to be added to the list.
     */
    public void addAnnotationPanel(ImageAnnotation annotation) {
        if ((annotation != null) && !annotationsPanel.contains(annotation)) {
            synchronized (getTreeLock()) {
                annotationsPanel.add(annotation);
            }
            fireImageUpdate(ImageUpdateEvent.ADD_ANNOTATION, annotation);
            repaint();
        }
    }

    /**
     * @deprecated use removeAnnotationImage
     */
    public void removeAnnotation(ImageAnnotation annotation) {
        removeAnnotationImage(annotation);
    }

    /**
     * Remove an annotation from the image. This function will remove the
     * annotation from the list of annotations only if it is not null and has
     * been added. If removed it will fire an event to all listeners of
     * imageupdate with the annotation.
     *
     * @param annotation the annotation to be removed from the list.
     */
    public void removeAnnotationImage(ImageAnnotation annotation) {
        if (annotationsImage.remove(annotation)) {
            fireImageUpdate(ImageUpdateEvent.REMOVE_ANNOTATION, annotation);
            repaint();
        }
    }

    /**
     * Remove an annotation from the panel. This function will remove the
     * annotation from the list of annotations only if it is not null and has
     * been added. If removed it will fire an event to all listeners of
     * imageupdate with the annotation.
     *
     * @param annotation the annotation to be removed from the list.
     */
    public void removeAnnotationPanel(ImageAnnotation annotation) {
        if (annotationsPanel.remove(annotation)) {
            fireImageUpdate(ImageUpdateEvent.REMOVE_ANNOTATION, annotation);
            repaint();
        }
    }

    /**
     * @deprecated use removeLastAnnotationImage
     */
    public void removeLastAnnotation() {
        removeLastAnnotationImage();
    }

    /**
     * Removes the last annotation added to the image. This function will remove
     * the last annotation that was added to the image.
     */
    public void removeLastAnnotationImage() {
        ImageAnnotation annotation = null;

        synchronized (getTreeLock()) {
            int idx = annotationsImage.size() - 1;
            if (idx >= 0) {
                annotation = (ImageAnnotation) annotationsImage.remove(idx);
            }
        }

        if (annotation != null) {
            fireImageUpdate(ImageUpdateEvent.REMOVE_ANNOTATION, annotation);
            repaint();
        }
    }

    /**
     * Removes the last annotation added to the panel. This function will remove
     * the last annotation that was added to the panel.
     */
    public void removeLastAnnotationPanel() {
        ImageAnnotation annotation = null;

        synchronized (getTreeLock()) {
            int idx = annotationsPanel.size() - 1;
            if (idx >= 0) {
                annotation = (ImageAnnotation) annotationsPanel.remove(idx);
            }
        }

        if (annotation != null) {
            fireImageUpdate(ImageUpdateEvent.REMOVE_ANNOTATION, annotation);
            repaint();
        }
    }

    /**
     * Remove all annotations. This function will remove all annotations, both
     * image as well as panel. After removing the annotations it will fire a
     * imageupdate event with null as the annotation removed.
     */
    public void removeAllAnnotations() {
        synchronized (getTreeLock()) {
            annotationsImage.clear();
            annotationsPanel.clear();
        }
        fireImageUpdate(ImageUpdateEvent.REMOVE_ANNOTATION, null);
        repaint();
    }

    /**
     * @deprecated use getAllAnnotationsImage()
     */
    public ImageAnnotation[] getAllAnnotations() {
        return getAllAnnotationsImage();
    }

    /**
     * Return a list of all annotations added to the image. If no annotations
     * are added it will return null.
     *
     * @return list of all annotations.
     */
    public ImageAnnotation[] getAllAnnotationsImage() {
        if (annotationsImage.size() == 0) {
            return null;
        }
        ImageAnnotation[] result = new ImageAnnotation[annotationsImage.size()];
        for (int i = 0; i < annotationsImage.size(); i++) {
            result[i] = (ImageAnnotation) annotationsImage.get(i);
        }
        return result;
    }

    /**
     * Return a list of all annotations added to the panel. If no annotations
     * are added it will return null.
     *
     * @return list of all annotations.
     */
    public ImageAnnotation[] getAllAnnotationsPanel() {
        if (annotationsImage.size() == 0) {
            return null;
        }
        ImageAnnotation[] result = new ImageAnnotation[annotationsImage.size()];
        for (int i = 0; i < annotationsImage.size(); i++) {
            result[i] = (ImageAnnotation) annotationsImage.get(i);
        }
        return result;
    }

    /**
     * Returns wether or not the user can select a region of interest of the
     * image.
     *
     * @return true if the user can select a region of interest.
     */
    public boolean isSelectionAllowed() {
        return selectionAllowed;
    }

    /**
     * Toggle the selection mechanism on or off. If called with false the user
     * will no longer be able to select subarea of the image. After changing the
     * value an event is send to all imageupdate listeners with the new state.
     *
     * @param selectionAllowed set this to true to allow the user to select a
     *                         region of interest in the image.
     */
    public void setSelectionAllowed(boolean selectionAllowed) {
        if (selectionAllowed == this.selectionAllowed) {
            return;
        }
        this.selectionAllowed = selectionAllowed;
        fireImageUpdate(ImageUpdateEvent.ALLOW_SELECTION, Boolean.valueOf(selectionAllowed));
        repaint();
    }

    /**
     * Return the current selection made by the user. If no selection is allowed
     * or the user has nothing selected, this will return null.
     *
     * @return the current selection if any, or null.
     */
    public Rectangle getSelection() {
        if (!selectionAllowed || !selection.isVisible()) {
            return null;
        }

        if ((selection.width == 0) || (selection.height == 0)) {
            return null;
        }

        return new Rectangle(selection.x, selection.y, selection.width, selection.height);
    }


    /**
     *	get poopupmenu
     * added by Sang-Chul
     */

    public JPopupMenu getPopupMenu() {
    	return popupmenu;
    }
    
    
    /**
     * Set the new selection, if called with null this will remove any selection
     * made by the user. If no selection is allowed, this will not set the
     * selection to anything but null. After setting the new selection an event
     * is send to all imageupdate listeners with the new selection.
     *
     * @param rect the new selection, or null to remove any selection.
     */
    public void setSelection(Rectangle rect) {
        if (rect == null) {
			if (selection.isVisible()) {
	            selection.setVisible(false);
	            fireImageUpdate(ImageUpdateEvent.CHANGE_SELECTION, null);
			}
        } else if (selectionAllowed) {
            selection.setRect(rect);

            if ((rect.width == 0) || (rect.height == 0)) {
                selection.setVisible(false);
            } else {
                selection.setVisible(true);
            }

            fireImageUpdate(ImageUpdateEvent.CHANGE_SELECTION, rect);
            revalidate();
            repaint();
        }
    }

    /**
     * Return where in the image the user last pressed. This is useful to find
     * where has the menu popup and clicked.
     *
     * @return the imagelocation where the user last clicked.
     */
    public Point getImageLocationClicked() {
        if (!pressedvalid) {
            return null;
        }
        return start;
    }

    // -----------------------------------------------------------------------
    // MOUSE LISTENERS
    // -----------------------------------------------------------------------
    /**
     * Implment functions for mouse press and release, ignore the rest.
     */
    class MyMouseListener extends MouseAdapter {
        /**
         * Called when the user presses a mouse button. This is either to show a
         * menu or drag the mouse for rubberbanding.
         *
         * @param e mouseevent when user pressed the mouse.
         */
        public void mousePressed(MouseEvent e) {
            ImageObject imgobj = getImageObject();
            if (imgobj == null) {
                pressedvalid = false;
                return;
            }
            Point p = getImageLocation(e.getPoint());
            if ((p.x < 0) || (p.x > imgobj.getNumCols())) {
                pressedvalid = false;
                return;
            }
            if ((p.y < 0) || (p.y > imgobj.getNumRows())) {
                pressedvalid = false;
                return;
            }
            pressedvalid = true;
            if (e.isPopupTrigger() && (popupmenu.getSubElements().length > 0)) {
                start = getMouseLocation(e.getPoint());
                popupmenu.show(e.getComponent(), e.getX(), e.getY());
            } else if (e.getModifiers() == MouseEvent.BUTTON1_MASK) {
                start = getMouseLocation(e.getPoint());
                rubberband = false;
            }
        }

        /**
         * Called when the user released a mouse button. This could mean the
         * user finished dragging the mouse or dimisses the menu. If the user
         * has made a selection send an event to any imageupdate listeners.
         *
         * @param e mouseevent when user released the mouse.
         */
        public void mouseReleased(MouseEvent e) {
            if (!pressedvalid) {
                return;
            }
            if (e.isPopupTrigger() && (popupmenu.getSubElements().length > 0)) {
                start = getMouseLocation(e.getPoint());
                popupmenu.show(e.getComponent(), e.getX(), e.getY());
            } else if (e.getModifiers() == MouseEvent.BUTTON1_MASK) {
                if (rubberband) {
                    fireImageUpdate(ImageUpdateEvent.CHANGE_SELECTION,
                                    new Rectangle(selection.x, selection.y,
                                                  selection.width, selection.height));
                } else {
					setSelection(null);
					repaint();
                }
            }
        }

		@Override
		public void mouseClicked(MouseEvent e) {
			Point pt = getImageLocationClicked();
			fireImageUpdate(ImageUpdateEvent.MOUSE_CLICKED, pt);
		}
    }

    /**
     * Implement function for mouse dragged, ignore the rest.
     */
    class MyMouseMotionListner extends MouseMotionAdapter {
        /**
         * If the first mouse button is down and the mouse is moved this will be
         * called and the rubberband will be drawn from the point where the
         * mouse was originally pushed to the current location.
         *
         * @param e mouseevent when user dragged the mouse.
         */
        public void mouseDragged(MouseEvent e) {
            if (!pressedvalid) {
                return;
            }
            if (e.getModifiers() == MouseEvent.BUTTON1_MASK) {
                if (selectionAllowed) {
                    Point end = getMouseLocation(e.getPoint());

                    selection.x = start.x;
                    selection.y = start.y;
                    selection.setEndCol(end.x);
                    selection.setEndRow(end.y);
                    selection.setVisible(true);
                    rubberband = true;

                    repaint();
                }
            }
        }
    }

    /**
     * Return the location in image coordinates. This will clip such that the
     * returned location is always inside the image.
     *
     * @param e location in pixels
     */
    public Point getMouseLocation(Point e) {
        Rectangle crop = getCrop();

        Point p = getImageLocation(e);

        ImageObject imgobj = getImageObject();
        if (imgobj == null) {
            return p;
        }

        if (crop != null) {
            if (p.x < crop.getMinX()) p.x = (int) crop.getMinX();
            if (p.x >= crop.getMaxX()) p.x = (int) crop.getMaxX();
            if (p.y < crop.getMinY()) p.y = (int) crop.getMinY();
            if (p.y >= crop.getMaxY()) p.y = (int) crop.getMaxY();
        } else {
            int w = imgobj.getNumCols();
            int h = imgobj.getNumRows();
            if (p.x < 0) p.x = 0;
            if (p.x >= w) p.x = w - 1;
            if (p.y < 0) p.y = 0;
            if (p.y >= h) p.y = h - 1;
        }

        return p;
    }

    // -----------------------------------------------------------------------
    /**
     * Add the menu items to the panel pop-up menu.
     *
     * @param panelItem to add to the popupmenu.
     */
    public void addMenu(Im2LearnMenu panelItem) {
        panelItem.setImagePanel(this);
        addImageUpdateListener(panelItem);

        JMenuItem[] items = panelItem.getPanelMenuItems();
        if (items == null) {
            return;
        }

        for (int i = 0; i < items.length; i++) {
            addMenu(items[i]);
        }
    }

    /**
     * Makes sure the menu item is alphabetacly inserted.
     * @param item to be inserted.
     */
    public void addMenu(JMenuItem item) {
        MenuElement[] elements = popupmenu.getSubElements();
        for(int j=0; j<elements.length; j++) {
            JMenuItem menuitem = (JMenuItem)elements[j];
            if (menuitem.getText().compareTo(item.getText()) > 0) {
                popupmenu.insert(item, j);
                return;
            }
        }
        popupmenu.add(item);
    }

    // -----------------------------------------------------------------------
    /**
     * Not only draw the image, but draw the rubberband as well as the
     * annotations.
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        if (selectionAllowed) {
            selection.paint(g2d, this);
        }

        synchronized (getTreeLock()) {
            for (int i = 0; i < annotationsPanel.size(); i++) {
                ImageAnnotation marker = (ImageAnnotation) annotationsPanel.get(i);
                marker.paint(g2d, this);
            }
            for (int i = 0; i < annotationsImage.size(); i++) {
                ImageAnnotation marker = (ImageAnnotation) annotationsImage.get(i);
                marker.paint(g2d, this);
            }
        }
    }
}
