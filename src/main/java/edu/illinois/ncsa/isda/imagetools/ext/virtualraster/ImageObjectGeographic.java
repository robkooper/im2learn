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
package edu.illinois.ncsa.isda.imagetools.ext.virtualraster;

import java.util.HashMap;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageException;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.imagetools.core.datatype.SubArea;
import edu.illinois.ncsa.isda.imagetools.core.geo.projection.ModelProjection;
import edu.illinois.ncsa.isda.imagetools.core.geo.projection.Projection;

/**
 * A potential subclass of ImageObject that is specialized for Geographic data.
 * 
 * Implements most of required ImageObject functionality through the component
 * object.
 * 
 * @author Yakov Keselman
 * @version August 4, 2006.
 */
public class ImageObjectGeographic // extends ImageObject
{

	static private Log							logger					= LogFactory.getLog(ImageObjectGeographic.class);

	/**
	 * The actual raster.
	 */
	protected final ImageObject					raster;

	/**
	 * The projection of the raster.
	 */
	protected Projection						sourceProjection;

	/**
	 * Transforms from the target coordinate system (image being formed) to the
	 * source coordinate system (of this raster).
	 */
	protected ProjectionTransformer				projectionTransformer;

	/**
	 * A mapping of projections onto bounding boxes (not to recompute them each
	 * time).
	 */
	protected HashMap<ModelProjection, SubArea>	projectionToBoundingBox	= new HashMap<ModelProjection, SubArea>();

	/**
	 * Augments an existing ImageObject with geographic functionality.
	 * 
	 * @param raster
	 * @param projection
	 *            externally supplied projection of the raster.
	 */
	public ImageObjectGeographic(ImageObject raster, Projection projection) {
		this(raster);
		this.sourceProjection = projection;
	}

	/**
	 * Augments an existing ImageObject with geographic functionality.
	 * 
	 * @param raster
	 */
	public ImageObjectGeographic(ImageObject raster) {
		// initialize the (final) fields.
		this.raster = raster;

		// get the raster's projection.
		try {
			this.sourceProjection = (Projection) raster.getProperty(ImageObject.GEOINFO);
		} catch (Exception e) {
			this.sourceProjection = null;
		}
	}

	/**
	 * Fix the projection of the target raster (the one being formed).
	 * 
	 * @param targetProjection
	 *            the projection of the target.
	 */
	public void setTargetProjection(ModelProjection targetProjection) {
		projectionTransformer = new ProjectionTransformer(sourceProjection, targetProjection);
	}

	/**
	 * @return the bounding box of the raster in the target raster. Note: the
	 *         bounding boxes are cached; so, this method can be called multiple
	 *         times without performance loss.
	 */
	public SubArea getBoundingBoxInTargetRaster() {

		// check if we have already computed the bounding box.
		SubArea bbox = projectionToBoundingBox.get(projectionTransformer.getTargetProjection());
		if (bbox != null) {
			return bbox;
		}

		// the corners of the future bounding box.
		double minX = Double.MAX_VALUE;
		double minY = Double.MAX_VALUE;
		double maxX = -Double.MAX_VALUE;
		double maxY = -Double.MAX_VALUE;

		// go over boundary pixels and map them into target coordinate system.
		double[] point = new double[2];
		double[] targetPoint;

		// due to the convexity of the region, we only need to go over the
		// boundary.
		// place boundary coordinates into an array.
		// there are 4 sub-boundaries: top, left, bottom, and right.
		int[][] boundary = { { 0, 0, 0, raster.getNumCols() - 1 }, // top
																	// boundary
				{ 0, 0, raster.getNumRows() - 1, 0 }, // left boundary
				{ raster.getNumRows() - 1, 0, raster.getNumRows() - 1, raster.getNumCols() - 1 }, // bottom
				{ 0, raster.getNumCols() - 1, raster.getNumRows() - 1, raster.getNumCols() - 1 } }; // right

		// go over the boundary.
		for (int i = 0; i < 4; i++) {
			for (int row = boundary[i][0]; row <= boundary[i][2]; row++) {
				point[1] = row;
				for (int col = boundary[i][1]; col <= boundary[i][3]; col++) {
					point[0] = col;
					try {
						targetPoint = projectionTransformer.sourcePixelsToTargetPixels(point);
						double x = targetPoint[0];
						double y = targetPoint[1];
						if (x < minX) {
							minX = x;
						}
						if (x > maxX) {
							maxX = x;
						}
						if (y < minY) {
							minY = y;
						}
						if (y > maxY) {
							maxY = y;
						}

					} catch (Exception e) {
					}
				}

			}
		}

		// insert the bounding box into the table and return it.
		bbox = new SubArea((int) minX, (int) minY, (int) (maxX - minX + 1), (int) (maxY - minY + 1));
		projectionToBoundingBox.put(projectionTransformer.getTargetProjection(), bbox);
		return bbox;
	}

	// // data reading methods are below.

	/*
	 * For now, only "getDouble" is implemented. The remaining (getByte, etc)
	 * methods may need to be implemented once this one is satisfactory.
	 */

	/*
	 * The current implementation uses a specific sampling strategy to get the
	 * value. One strategy is to use a single pixel. Another is to use 5
	 * weighted pixels. These strategies may be too simplistic. This is better
	 * implemented through a separate sampling mechanism (class).
	 */

	/**
	 * @param point
	 *            location expressed in the target (new image) coordinate
	 *            system.
	 * @param band
	 *            numerical band of the raster.
	 * @return the value at the location. IMPORTANT NOTE: The target projection
	 *         should have already been set. IMPORTANT NOTE: Does not check
	 *         whether the point falls within the raster.
	 */
	public byte getByte(double[] point, int band) throws ImageException {
		double[] colrow = projectionTransformer.targetProjectedToSourcePixels(point);
		return raster.getByte((int) colrow[1], (int) colrow[0], band);
	}

	public short getShort(double[] point, int band) throws ImageException {
		double[] colrow = projectionTransformer.targetProjectedToSourcePixels(point);
		return raster.getShort((int) colrow[1], (int) colrow[0], band);
	}

	public int getInt(double[] point, int band) throws ImageException {
		double[] colrow = projectionTransformer.targetProjectedToSourcePixels(point);
		return raster.getInt((int) colrow[1], (int) colrow[0], band);
	}

	public long getLong(double[] point, int band) throws ImageException {
		double[] colrow = projectionTransformer.targetProjectedToSourcePixels(point);
		return raster.getLong((int) colrow[1], (int) colrow[0], band);
	}

	public float getFloat(double[] point, int band) throws ImageException {
		double[] colrow = projectionTransformer.targetProjectedToSourcePixels(point);
		return raster.getFloat((int) colrow[1], (int) colrow[0], band);
	}

	public double getDouble(double[] point, int band) throws ImageException {
		double[] colrow = projectionTransformer.targetProjectedToSourcePixels(point);
		return raster.getDouble((int) colrow[1], (int) colrow[0], band);
	}

	/**
	 * Indices for weighted sampling.
	 */
	// private final static int[] samplingIndexI = { 0, 1, 0, -1, 0 };
	/**
	 * Indices for weighted sampling.
	 */
	// private final static int[] samplingIndexJ = { 0, 0, -1, 0, 1 };
	/**
	 * Weights for weighted sampling.
	 */
	// private final static double[] samplingWeight = { 0.5, 0.125, 0.125,
	// 0.125, 0.125 };

	/**
	 * @param point
	 *            location expressed in the target (new image) coordinate
	 *            system.
	 * @param radius
	 *            for the coordinates (larger radius == larger areas).
	 * @param band
	 *            numerical band of the raster.
	 * @return the value at the location. IMPORTANT NOTE: The target projection
	 *         should have already been set. IMPORTANT NOTE: Does not check
	 *         whether the point falls within the raster.
	 */
	/*
	 * public double getDouble( double[] point, double[] radius, int band )
	 * throws ImageException { // the resulting weighted value. double
	 * weightedValue = 0;
	 *  // the point to use in sampling. double[] shiftedPoint = new double[2];
	 *  // loop on the 5 points: self, right, bottom, left, top. for( int i=0; i<5;
	 * i++ ) { shiftedPoint[0] = point[0] + samplingIndexI[i]*radius[0];
	 * shiftedPoint[1] = point[1] + samplingIndexJ[i]*radius[1]; double[] colrow =
	 * projectionTransformer.targetProjectedToSourcePixels( shiftedPoint );
	 * weightedValue += samplingWeight[i]*raster.getDouble( (int)colrow[1],
	 * (int)colrow[0], band ); }
	 * 
	 * return weightedValue; }
	 */

	// methods delegated to the underlying ImageObject.
	public String getFileName() {
		return raster.getFileName();
	}
}
