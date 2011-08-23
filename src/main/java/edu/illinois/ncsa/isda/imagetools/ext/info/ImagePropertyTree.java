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

import java.awt.geom.Rectangle2D;
import java.lang.reflect.Array;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObjectOutOfCore;
import edu.illinois.ncsa.isda.imagetools.core.display.ImageAnnotation;
import edu.illinois.ncsa.isda.imagetools.core.display.ImageComponent;
import edu.illinois.ncsa.isda.imagetools.core.display.ImagePanel;
import edu.illinois.ncsa.isda.imagetools.core.geo.BoundingBox;
import edu.illinois.ncsa.isda.imagetools.core.geo.GeoException;
import edu.illinois.ncsa.isda.imagetools.core.geo.GeoUtilities;
import edu.illinois.ncsa.isda.imagetools.core.geo.Projection;
import edu.illinois.ncsa.isda.imagetools.core.geo.ProjectionConvert;


/**
 * Create a tree with all the properties of an ImageObject. This allows for easy browsing of an ImageObject. This tree
 * can also display information from an ImagePanel. The information retured from a node, is all the information
 * associated with that node, and any subnodes.
 */
public class ImagePropertyTree extends JTree {
	private int								maxdepth	= 100;
	private final DefaultMutableTreeNode	root		= new DefaultMutableTreeNode("Info");
	private final DefaultTreeModel			treemodel	= new DefaultTreeModel(root);

	/**
	 * Default constructor. Removes any icons from the tree, and makes sure only one item can be selected.
	 */
	public ImagePropertyTree() {
		super();

		// only select 1 item
		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

		// don't render any icons
		DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
		renderer.setOpenIcon(null);
		renderer.setClosedIcon(null);
		renderer.setLeafIcon(null);
		setCellRenderer(renderer);

		setModel(treemodel);
	}

	/**
	 * Reset the tree to have only the root node left.
	 */
	public void reset() {
		root.removeAllChildren();
		treemodel.reload();
	}

	/**
	 * Returns the maximum number of values shown of an array. If an array has more than this number of entries, only up
	 * to this return value are shown.
	 * 
	 * @return maximum number of entries of an array shown.
	 */
	public int getMaxDepth() {
		return maxdepth;
	}

	/**
	 * Sets the number of an array to show. If an array has more than this number of entries it will be truncated in the
	 * tree view.
	 * 
	 * @param maxdepth
	 *            maximum number of entries to show of a tree.
	 */
	public void setMaxDepth(int maxdepth) {
		this.maxdepth = maxdepth;
	}

	/**
	 * Adds all the information of the ImageObject to the rootnode, the properties and min/max values are added as
	 * subnodes to the root.
	 * 
	 * @param imageobject
	 *            whose propertes are to be added to the tree.
	 */
	public void addNode(ImageObject imageobject) {
		addNode(root, createNode("width/columns", "" + imageobject.getNumCols()));
		addNode(root, createNode("height/rows", "" + imageobject.getNumRows()));
		addNode(root, createNode("depth/bands", "" + imageobject.getNumBands()));
		addNode(root, createNode("type", ImageObject.types[imageobject.getType()]));
		addNode(root, createNode("size", "" + imageobject.getSize()));
		if (imageobject.isInvalidDataSet()) {
			addNode(root, createNode("invalid", "" + imageobject.getInvalidData()));
		}
		if (imageobject.isOutOfCore()) {
			ImageObjectOutOfCore iooc = (ImageObjectOutOfCore) imageobject;
			DefaultMutableTreeNode ooc = new DefaultMutableTreeNode("Out Of Core");
			addNode(root, ooc);
			addNode(ooc, createNode("suggested scale", "" + iooc.getSuggestedScale()));
			addNode(ooc, createNode("tiles", iooc.getTileBoxes()));
		}
		addNode(root, createNode("header only?", "" + imageobject.isHeaderOnly()));
		addNodeMinMax(imageobject);
		addNode(root, createNode("Properties", imageobject.getProperties()));
		if (imageobject.getProperty(ImageObject.GEOINFO) != null) {
			DefaultMutableTreeNode tn = new DefaultMutableTreeNode("Projection");
			addNode(root, tn);
			try {

				Projection prj = ProjectionConvert.getNewProjection(imageobject.getProperty(ImageObject.GEOINFO));
				addNode(tn, createNode("new", prj));
				BoundingBox bbox = GeoUtilities.getEarthBounds(imageobject, prj);
				addNode(tn, createNode("Earth BBox", bbox));
				bbox = GeoUtilities.getModelBounds(imageobject, prj);
				addNode(tn, createNode("Model BBox", bbox));
			} catch (GeoException exc) {
				exc.printStackTrace();
			}
			try {
				addNode(tn, createNode("old", ProjectionConvert.getOldProjection(imageobject.getProperty(ImageObject.GEOINFO)).toString()));
			} catch (Throwable exc) {
				exc.printStackTrace();
			}
		}
		treemodel.reload();
	}

	/**
	 * Creates two nodes, one for min and one for max values to the root node. Besides all the minimum and maximum
	 * values for each band also the absolute minimum and maximum values found in the image are added.
	 * 
	 * @param imageobject
	 *            whose minimum and maximum values to display.
	 */
	public void addNodeMinMax(ImageObject imageobject) {
		DefaultMutableTreeNode min = new DefaultMutableTreeNode("Minimum");
		DefaultMutableTreeNode max = new DefaultMutableTreeNode("Maximum");
		addNode(root, min);
		addNode(root, max);

		addNode(min, createNode("absolute", "" + imageobject.getMin()));
		addNode(max, createNode("absolute", "" + imageobject.getMax()));

		for (int i = 0; i < imageobject.getNumBands(); i++) {
			addNode(min, createNode("" + i, "" + imageobject.getMin(i)));
			addNode(max, createNode("" + i, "" + imageobject.getMax(i)));
		}
	}

	/**
	 * Adds the properties of an imagecomp to the tree.
	 * 
	 * @param imagecomp
	 *            to be added to the tree.
	 */
	public void addNode(ImageComponent imagecomp) {
		DefaultMutableTreeNode display = new DefaultMutableTreeNode("Display");
		addNode(root, display);

		addNode(display, createNode("gamma", "" + imagecomp.getGamma()));
		addNode(display, createNode("zoom", (imagecomp.getZoomFactor() * 100) + "%"));
		addNode(display, createNode("paintscale", (imagecomp.getPaintScale() * 100) + "%"));
		if (imagecomp.isFakeRGBcolor()) {
			addNode(display, createNode("bands", "PSEUDOCOLOR"));
		} else if (imagecomp.isGrayScale()) {
			addNode(display, createNode("bands", "GRAYSCALE " + imagecomp.getGrayBand()));
		} else {
			addNode(display, createNode("bands", imagecomp.getRedBand() + ", " + imagecomp.getGreenBand() + ", " + imagecomp.getBlueBand()));
		}
		if (imagecomp.getCrop() != null) {
			addNode(display, createNode("crop", imagecomp.getCrop()));
		}
		// additional information found in an imagepanel.
		if (imagecomp instanceof ImagePanel) {
			ImagePanel ip = (ImagePanel) imagecomp;
			if (ip.getSelection() != null) {
				addNode(display, createNode("selection", ip.getSelection()));
			}
			ImageAnnotation[] markers = ip.getAllAnnotationsImage();
			if (markers != null) {
				addNode(display, createNode("image annotations", markers));
			}
			markers = ip.getAllAnnotationsPanel();
			if (markers != null) {
				addNode(display, createNode("panel annotations", markers));
			}
		}

		treemodel.reload();
	}

	/**
	 * Insert the node at the end of the parent node.
	 * 
	 * @param parent
	 *            node the new node is inserted into.
	 * @param child
	 *            node to be inserted.
	 */
	private void addNode(MutableTreeNode parent, MutableTreeNode child) {
		treemodel.insertNodeInto(child, parent, parent.getChildCount());
	}

	/**
	 * Create a new treenode based on the key, val pair. This function will try and create any subnodes if needed to
	 * most accuratly represent the information stored in val.
	 * 
	 * @param key
	 *            of the new node.
	 * @param val
	 *            of the new node.
	 * @return a node containing the key, val pair with the val potentially broken up in new subtrees.
	 */
	private DefaultMutableTreeNode createNode(Object key, Object val) {
		if (val == null) {
			return new DefaultMutableTreeNode(new ImageInfo(key, ""));
		} else if (val.getClass().isArray()) {
			DefaultMutableTreeNode parent = new DefaultMutableTreeNode(key);
			int len = Array.getLength(val) < maxdepth ? Array.getLength(val) : maxdepth;
			for (int i = 0; i < len; i++) {
				addNode(parent, createNode("" + i, Array.get(val, i)));
			}
			if (Array.getLength(val) > len) {
				addNode(parent, createNode("...", ""));
			}
			return parent;
		} else if (val instanceof List) {
			DefaultMutableTreeNode parent = new DefaultMutableTreeNode(key);
			List list = (List) val;
			int len = list.size() < maxdepth ? list.size() : maxdepth;
			for (int i = 0; i < len; i++) {
				addNode(parent, createNode("" + i, list.get(i)));
			}
			if (list.size() > len) {
				addNode(parent, createNode("...", ""));
			}
			return parent;
		} else if (val instanceof Map) {
			Map map = (Map) val;
			DefaultMutableTreeNode parent = new DefaultMutableTreeNode(key);
			for (Iterator iter = map.keySet().iterator(); iter.hasNext();) {
				Object obj = iter.next();
				if (!ImageObject.GEOINFO.equals(obj)) {
					addNode(parent, createNode(obj, map.get(obj)));
				}
			}
			return parent;
		} else if (val instanceof Rectangle2D) {
			Rectangle2D rect = (Rectangle2D) val;
			DefaultMutableTreeNode parent = new DefaultMutableTreeNode(key);
			addNode(parent, createNode("X", "" + rect.getX()));
			addNode(parent, createNode("Y", "" + rect.getY()));
			addNode(parent, createNode("Width", "" + rect.getWidth()));
			addNode(parent, createNode("Height", "" + rect.getHeight()));
			return parent;
		} else if (val instanceof BoundingBox) {
			BoundingBox bbox = (BoundingBox) val;
			DefaultMutableTreeNode parent = new DefaultMutableTreeNode(key);
			addNode(parent, createNode("minX", "" + bbox.getMinX()));
			addNode(parent, createNode("maxX", "" + bbox.getMaxX()));
			addNode(parent, createNode("Width", "" + bbox.getW()));
			addNode(parent, createNode("minY", "" + bbox.getMinY()));
			addNode(parent, createNode("maxY", "" + bbox.getMaxY()));
			addNode(parent, createNode("Height", "" + bbox.getH()));
			return parent;
		} else if (val instanceof Projection) {
			Projection proj = (Projection) val;
			DefaultMutableTreeNode parent = new DefaultMutableTreeNode(key);
			addNode(parent, createNode("Name", proj.getName()));
			addNode(parent, createNode("Type", proj.getType()));
			addNode(parent, createNode("GeoGCS", proj.getGeographicCoordinateSystem()));
			addNode(parent, createNode("Parameters", proj.getParameters()));
			addNode(parent, createNode("Unit", proj.getUnit()));
			addNode(parent, createNode("TiePoint", proj.getTiePoint()));
			return parent;
		} else {
			String[] str = val.toString().split("\r?\n");
			if (str.length == 1) {
				return new DefaultMutableTreeNode(new ImageInfo(key, val));
			} else {
				return new DefaultMutableTreeNode(new ImageInfo(key, str));
			}
		}
	}

	/**
	 * Return a string representation of the selected node. This will call getNode() with a null tab value.
	 * 
	 * @param node
	 *            to be converted to string.
	 * @return string version of the node and any subnodes.
	 */
	public String getNode(DefaultMutableTreeNode node) {
		return getNode("", node);
	}

	/**
	 * Return a string representation of the current node and any subnodes. This function will call itself recursivly
	 * for each subnode that it finds, adjusting the tab value by one more tab.
	 * 
	 * @param tab
	 *            spaces before text.
	 * @param node
	 *            current node to be displayed.
	 * @return string version of the node and any subnodes.
	 */
	private String getNode(String tab, DefaultMutableTreeNode node) {
		if (node == null) {
			return getNode(tab, root);
		} else if (node.isLeaf()) {
			String result = tab + node.toString() + " = ";
			Object user = node.getUserObject();
			if (user instanceof ImageInfo) {
				Object val = ((ImageInfo) user).val;
				if (val.getClass().isArray()) {
					result += "{\n";
					int len = Array.getLength(val);
					for (int i = 0; i < len; i++) {
						result += tab + "\t" + Array.get(val, i) + "\n";
					}
					result += tab + "}";
				} else {
					result += val.toString();
				}
			}
			return result + "\n";
		} else {
			String result;
			result = tab + node.toString() + " {\n";
			for (Enumeration e = node.children(); e.hasMoreElements();) {
				result += getNode(tab + "\t", (DefaultMutableTreeNode) e.nextElement());
			}
			result += tab + "}\n";
			return result;
		}
	}

	/**
	 * Class to hold key,val pairs that can be associated with a treenode.
	 */
	class ImageInfo {
		public Object	key;
		public Object	val;

		/**
		 * Constructor to store key, val pair.
		 * 
		 * @param key
		 *            to be displayed in the tree.
		 * @param val
		 *            associated with this key.
		 */
		public ImageInfo(Object key, Object val) {
			this.key = key;
			this.val = val;
		}

		/**
		 * Return the key as string.
		 * 
		 * @return key value.
		 */
		@Override
		public String toString() {
			return key.toString();
		}
	}
}
