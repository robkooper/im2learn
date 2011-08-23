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


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.imagetools.core.geo.GeoException;
import edu.illinois.ncsa.isda.imagetools.core.geo.projection.ModelProjection;
import edu.illinois.ncsa.isda.imagetools.core.geo.projection.Projection;

/**
 * Quickly performs transformations between points in raster (existing, source)
 * and target (new, being formed) projected coordinate systems.
 * 
 * Note that currently ImageObject's are by default annotated by more restricted
 * Projection's, not richer ModelProjection's. However, some ImageObjects are
 * actually annotated by ModelProjections. Hence, this class will take care of
 * the difference.
 * 
 * NOTE: as of 08/07/06, only modelToRaster and rasterToModel seem to work as
 * expected.
 * 
 * @author Yakov Keselman
 */
public class ProjectionTransformer {

	/**
	 * Initialize with raster and model projection.
	 * 
	 * @param rasterProj
	 *            the projection of the raster (can be ModelProjection or
	 *            Projection).
	 * @param modelProj
	 *            the projection of the model (must be ModelProjection).
	 */
	public ProjectionTransformer(Projection sourceProj, ModelProjection targetProj) {
		// initialize model projection
		targetProjection = targetProj;

		// check if the raster's projection is actually a ModelProjection.
		if (sourceProj instanceof ModelProjection) {
			ModelProjection rasterModelProjection = (ModelProjection) sourceProj;
			sourceProjection = null;
			modelProjectionTransformer = new ModelProjectionTransformer(rasterModelProjection, targetProjection);
		} else {
			sourceProjection = sourceProj;
			modelProjectionTransformer = null;
		}
	}

	/**
	 * Transform from target projected coordinates to source pixel coordinates.
	 */
	public double[] targetProjectedToSourcePixels(double[] point) throws GeoException {
		// check whether we can use the transformer between the pair of models.
		try {
			return modelProjectionTransformer.targetProjectedToSourcePixels(point);
		} catch (NullPointerException e) // be careful not to catch
											// GeoException
		{
			// this does not seem to work (not implemented correctly in
			// Projection.java).
			return sourceProjection.earthToRaster(targetProjection.modelToEarth(point));
		}
	}

	/**
	 * Transform from source pixel coordinates to target's projected
	 * coordinates.
	 */
	public double[] sourcePixelsToTargetProjected(double[] point) throws GeoException {
		// check whether we can use the transformer between the pair of models.
		try {
			return modelProjectionTransformer.sourcePixelsToTargetProjected(point);
		} catch (NullPointerException e) // be careful not to catch
											// GeoException
		{
			// this does not seem to work (incorrect implementation in
			// Projection?).
			return targetProjection.earthToModel(sourceProjection.colRowToLatLon(point));
		}
	}

	/**
	 * Transform from source pixel coordinates to target pixel coordinates.
	 */
	public double[] sourcePixelsToTargetPixels(double[] point) throws GeoException {
		// check whether we can use the transformer between the pair of models.
		try {
			return modelProjectionTransformer.sourcePixelsToTargetPixels(point);
		} catch (NullPointerException e) // be careful not to catch
											// GeoException
		{
			// this does not seem to work (incorrect implementation in
			// Projection?).
			return targetProjection.earthToRaster(sourceProjection.earthToRaster(point));
		}
	}

	/**
	 * @return the target projection.
	 */
	public ModelProjection getTargetProjection() {
		return targetProjection;
	}

	/**
	 * The projection of the existing, source raster. In general, it can be of
	 * the more generic Projection type.
	 */
	private final Projection					sourceProjection;

	/**
	 * The projection of the raster being formed (new, target raster).
	 */
	private final ModelProjection				targetProjection;

	/**
	 * A transformer object, possible to form only when both projections are
	 * ModelProjection's.
	 */
	private final ModelProjectionTransformer	modelProjectionTransformer;

	/**
	 * Logger for the class.
	 */
	static private Log							logger	= LogFactory.getLog(ProjectionTransformer.class);

}
