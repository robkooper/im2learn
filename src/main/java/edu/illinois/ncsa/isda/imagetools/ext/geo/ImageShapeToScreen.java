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

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ShapeObject;
import edu.illinois.ncsa.isda.imagetools.core.geo.GeoException;
import edu.illinois.ncsa.isda.imagetools.core.geo.ProjectionConvert;
import edu.illinois.ncsa.isda.imagetools.core.geo.projection.ModelProjection;
import edu.illinois.ncsa.isda.imagetools.core.geo.projection.Projection;

/**
 * Converts points from shape coordinates to screen (pixel) coordinates. Stores
 * several important properties in final fields to speed up the computation.
 * 
 * @author Yakov Keselman
 * @version June 26, 2006.
 * 
 * TODO: treat the case that image object and shape object projections are the
 * same.
 */
public class ImageShapeToScreen {

	/*
	 * Indicates whether the shape is in a projected coordinate system.
	 */
	private final boolean isInProjected;

	/*
	 * Indicates whether the shape is in the Geographic coordinate system.
	 */
	private final boolean isInGeographic;

	/**
	 * Indicates whether the shape is in the pixel coordinate system.
	 */
	private final boolean isInPixels;

	/*
	 * Image projection (includes transformations earth to/from raster).
	 */
	final Projection imageProjection;

	/*
	 * Shape projection (includes transformation to/from earth).
	 */
	private Projection shapeProjection;

	/*
	 * Construct the object from image and shape objects.
	 */
	public ImageShapeToScreen(ImageObject imageObject, ShapeObject shapeObject) {
		this.isInProjected = shapeObject.getIsInProjected();
		this.isInPixels = shapeObject.getIsInPixel();
		this.imageProjection = (Projection) imageObject
				.getProperty(ImageObject.GEOINFO);
		try {
			this.shapeProjection = ProjectionConvert.toOld(shapeObject
					.getProjection());
		} catch (GeoException e) {
			e.printStackTrace();
			this.shapeProjection = null;
		}
		this.isInGeographic = shapeObject.getIsInGeographic();
	}

	/**
	 * Convert a point from the shape coordinate system to the pixel coordinate
	 * system by using a combination of projections from the shape and image
	 * objects.
	 * 
	 * @param shapePoint
	 *            a point in the shape coordinate system.
	 * @return a point in the screen (pixels) coordinate system.
	 */
	public double[] toScreen(double[] shapePoint) throws GeoException {

		// a quick check whether it's in pixels.
		// this check can be removed if the shape is never in pixels.
		if (isInPixels) {
			return shapePoint;
		}

		// check whether the shape is in the geographic coordinate system.
		if (this.isInGeographic) {
			return imageProjection.earthToRaster(shapePoint);
		}

		// check whether the shape is in a projected coordinate system.
		if (this.isInProjected) {
			return this.imageProjection
					.earthToRaster(((ModelProjection) this.shapeProjection)
							.modelToEarth(shapePoint));
		}

		// in the unlikely event that we get here, return the original shape
		// point.
		return shapePoint;

		// note: if the projections are the same, can simply use
		// "modelToRaster".
		/*
		 * if ( isInProjected && ( imageProjection instanceof ModelProjection) ) {
		 * ModelProjection modelProj = (ModelProjection) imageProjection ;
		 * screenPt = modelProj.modelToRaster( shapePt ); }
		 */

	}

}
