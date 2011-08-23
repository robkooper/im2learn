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
package edu.illinois.ncsa.isda.imagetools.ext.geo;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ShapeObject;
import edu.illinois.ncsa.isda.imagetools.core.display.ImageAnnotation;
import edu.illinois.ncsa.isda.imagetools.core.display.ImagePanel;
import edu.illinois.ncsa.isda.imagetools.core.geo.GeoException;
import edu.illinois.ncsa.isda.imagetools.core.geo.ModelPoint;
import edu.illinois.ncsa.isda.imagetools.core.geo.Projection;
import edu.illinois.ncsa.isda.imagetools.core.geo.ProjectionConvert;
import edu.illinois.ncsa.isda.imagetools.core.geo.RasterPoint;

/**
 * This ImageAnnotation displays the contents of a shapefile over an image with geo-referencing data.
 */
public class ShapeImageAnnotation implements ImageAnnotation {
	private ImageObject				imgobj;
	private final ShapeObject		shapeObject;
	private Color					color;
	private final ArrayList<int[]>	points;
	private final ArrayList<int[]>	lines;
	private BufferedImage			cache		= null;

	static public boolean			forspeed	= true;
	static public int				scalefactor	= 2;

	static private Log				logger		= LogFactory.getLog(ShapeImageAnnotation.class);

	public ShapeImageAnnotation(ShapeObject shape, Color c) {
		shapeObject = shape;
		color = c;
		points = new ArrayList<int[]>();
		lines = new ArrayList<int[]>();
	}

	public void setColor(Color c) {
		color = c;
	}

	public void paint(Graphics2D g, ImagePanel imagepanel) {
		ImageObject iopan = imagepanel.getImageObject();
		if ((forspeed && (cache == null)) || (imgobj != iopan) || !iopan.isSame(imgobj)) {
			imgobj = iopan;
			long l = System.nanoTime();
			if (forspeed) {
				cache = cacheShapeObject(shapeObject, imgobj, color, scalefactor);
			} else {
				cacheShapeObject();
			}
			logger.debug("Time to cache : " + (System.nanoTime() - l));
		}

		long l = System.nanoTime();
		if (forspeed) {
			g.drawImage(cache, 0, 0, imgobj.getNumCols(), imgobj.getNumRows(), 0, 0, cache.getWidth(), cache.getHeight(), null);
		} else {
			g.setColor(color);

			// draw the points
			for (int[] point : points) {
				g.drawOval(point[0], point[1], 1, 1);
			}

			// draw the lines
			for (int[] line : lines) {
				g.drawLine(line[0], line[1], line[2], line[3]);
			}
		}
		logger.debug("Time to draw : " + (System.nanoTime() - l));
	}

	private void cacheShapeObject() {
		logger.debug("in cache mode");

		points.clear();
		lines.clear();

		Projection iproj;
		try {
			iproj = ProjectionConvert.getNewProjection(imgobj.getProperty(ImageObject.GEOINFO));
		} catch (GeoException exc) {
			exc.printStackTrace();
			return;
		}
		Projection sproj = shapeObject.getProjection();

		int numBoundaries = shapeObject.getNumBoundaries();
		ModelPoint mp1 = new ModelPoint();
		ModelPoint mp2 = new ModelPoint();
		RasterPoint rp1;
		RasterPoint rp2;
		// for each boundary
		for (int index = 0; index < numBoundaries; index++) {
			// boundary is a point
			if (shapeObject.getNumBoundaryPts(index) == 1) {
				double[] point = shapeObject.GetPtsForBnd(index);
				mp1.setX(point[1]);
				mp1.setY(point[0]);
				try {
					rp1 = iproj.earthToRaster(sproj.modelToEarth(mp1));
					points.add(new int[] { (int) rp1.getX(), (int) rp1.getY() });
				} catch (GeoException exc) {
					exc.printStackTrace();
				}
			} else { // boundary is a polygon
				int j;
				double[] partPts = null;
				int idx;
				// For each boundary, draw each part joined by lines.
				int numBndParts = shapeObject.GetNumBndParts(index);

				for (j = 0; j < numBndParts; j++) {
					partPts = shapeObject.GetPartPointsForBnd(index, j);
					if (partPts != null) {
						for (idx = 2; idx < (partPts.length - 1); idx += 2) {
							try {
								mp1.setX(partPts[idx - 1]);
								mp1.setY(partPts[idx - 2]);
								rp1 = iproj.earthToRaster(sproj.modelToEarth(mp1));
								mp2.setX(partPts[idx + 1]);
								mp2.setY(partPts[idx]);
								rp2 = iproj.earthToRaster(sproj.modelToEarth(mp2));
								lines.add(new int[] { (int) rp1.getX(), (int) rp1.getY(), (int) rp2.getX(), (int) rp2.getY() });
							} catch (GeoException exc) {
								exc.printStackTrace();
							}
						} // for(int idx = ...)
					} // if (partPts != null)
				} // for( j = ...)
			} // else when the shape of not a ShapePoint
		}

		logger.debug("points size : " + points.size());
		logger.debug("lines size  : " + lines.size());
	}

	static BufferedImage cacheShapeObject(ShapeObject so, ImageObject imgobj, Color color, int scale) {
		// create the buffered image
		BufferedImage bi = new BufferedImage(imgobj.getNumCols() * scale, imgobj.getNumRows() * scale, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = bi.createGraphics();
		g2d.setBackground(new Color(1.0f, 1.0f, 1.0f, 0.0f));
		g2d.setColor(color);
		g2d.clearRect(0, 0, bi.getWidth(), bi.getHeight());

		Projection iproj;
		try {
			iproj = ProjectionConvert.getNewProjection(imgobj.getProperty(ImageObject.GEOINFO));
		} catch (GeoException exc) {
			exc.printStackTrace();
			return null;
		}
		Projection sproj = so.getProjection();

		// draw the shape in the buffered image
		int numBoundaries = so.getNumBoundaries();
		ModelPoint mp1 = new ModelPoint();
		ModelPoint mp2 = new ModelPoint();
		RasterPoint rp1;
		RasterPoint rp2;
		// for each boundary
		for (int index = 0; index < numBoundaries; index++) {
			// boundary is a point
			if (so.getNumBoundaryPts(index) == 1) {
				double[] point = so.GetPtsForBnd(index);
				mp1.setX(point[1]);
				mp1.setY(point[0]);
				try {
					rp1 = iproj.earthToRaster(sproj.modelToEarth(mp1));
					g2d.drawOval((int) (scale * rp1.getX()), (int) (scale * rp1.getY()), scale, scale);
				} catch (GeoException exc) {
					exc.printStackTrace();
				}
			} else { // boundary is a polygon
				int j;
				double[] partPts = null;
				int idx;
				// For each boundary, draw each part joined by lines.
				int numBndParts = so.GetNumBndParts(index);

				for (j = 0; j < numBndParts; j++) {
					partPts = so.GetPartPointsForBnd(index, j);
					if (partPts != null) {
						for (idx = 2; idx < (partPts.length - 1); idx += 2) {
							try {
								mp1.setX(partPts[idx - 1]);
								mp1.setY(partPts[idx - 2]);
								rp1 = iproj.earthToRaster(sproj.modelToEarth(mp1));
								mp2.setX(partPts[idx + 1]);
								mp2.setY(partPts[idx]);
								rp2 = iproj.earthToRaster(sproj.modelToEarth(mp2));
								g2d.drawLine((int) (scale * rp1.getX()), (int) (scale * rp1.getY()), (int) (scale * rp2.getX()), (int) (scale * rp2.getY()));
							} catch (GeoException exc) {
								exc.printStackTrace();
							}
						} // for(int idx = ...)
					} // if (partPts != null)
				} // for( j = ...)
			} // else when the shape of not a ShapePoint
		}

		return bi;
	}
}
