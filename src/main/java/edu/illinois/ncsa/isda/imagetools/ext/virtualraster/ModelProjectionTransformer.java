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

import edu.illinois.ncsa.isda.imagetools.core.geo.GeoException;
import edu.illinois.ncsa.isda.imagetools.core.geo.projection.ModelProjection;

/**
 * Performs transformations between pairs of ModelProjections. The first
 * projection will typically be that of an existing (source) raster. The second
 * projection is that of a new (target) raster that will be constructed.
 * 
 * @author Yakov Keselman
 */
/*
 * NOTE: as of 08/07/06, only modelToRaster and rasterToModel seem to work as
 * expected. The rest seem to be suspicious or buggy or both.
 */
public class ModelProjectionTransformer {

	/**
	 * Constructor from a pair of Model projections.
	 * 
	 * @param sourceProjection
	 *            source model projection.
	 * @param targetProjection
	 *            target model projection.
	 */
	public ModelProjectionTransformer(ModelProjection sourceProjection, ModelProjection targetProjection) {
		this.sourceProjection = sourceProjection;
		this.targetProjection = targetProjection;
	}

	/**
	 * @param point
	 *            in the new raster coordinate system.
	 * @return pixels in the coordinate system of the existing raster.
	 */
	/*
	 * This will need a different implementation, in terms of pairwise
	 * transformations. In other words, the line: point =
	 * sourceProjection.earthToModel( targetProjection.modelToEarth( point ) ) );
	 * will be replaced by a line: if( (sourceProjection instanceof Sinusoidal) &
	 * (targetProjection instanceof UTMNorth ) ) point =
	 * PairwiseTransformer.sinusoidalToUTMNorth( sourceProjection,
	 * targetProjection, point ); To implement it better, we'll need flags of
	 * the following type: private final boolean isExistingSinusoidal =
	 * (sourceProjection instanceof Sinusoidal); ...
	 */
	public double[] targetProjectedToSourcePixels(double[] point) throws GeoException {
		// Should be changed once pairwise transforms are implemented.
		// return sourceProjection.modelToRaster( sourceProjection.earthToModel(
		// targetProjection.modelToEarth( point ) ) );

		// this works (for the same projection); need to change to work in
		// general.
		return sourceProjection.modelToRaster(point);
	}

	/**
	 * @param pixel
	 *            point in the source raster coordinate system.
	 * @return point in the target raster coordinate system.
	 * @throws GeoException
	 */
	public double[] sourcePixelsToTargetProjected(double[] point) throws GeoException {
		// The implementation of this method should be changed, similarly to the
		// above.
		// return targetProjection.earthToModel(
		// sourceProjection.modelToEarth( sourceProjection.rasterToModel( point
		// ) ) );

		// this works (for the same projection); need to change to work in
		// general.
		// return sourceProjection.rasterToModel( point );
		return sourceProjection.rasterToModel(point);
	}

	/**
	 * @param pixel
	 *            point in the source raster coordinate system.
	 * @return pixel point in the target raster coordinate system.
	 * @throws GeoException
	 */
	public double[] sourcePixelsToTargetPixels(double[] point) throws GeoException {
		// this implementation does not need to be changed.
		// return targetProjection.modelToRaster( sourcePixelsToTargetProjected(
		// point ) );

		// this works (for the same projection); need to change to work in
		// general.
		return targetProjection.modelToRaster(sourceProjection.rasterToModel(point));

		// this should work in general but it does not work even when both are
		// the same!
		// return targetProjection.modelToRaster( targetProjection.earthToModel(
		// sourceProjection.modelToEarth( sourceProjection.rasterToModel( point
		// ) ) ) );

	}

	/**
	 * The projection of the existing raster, typically the target.
	 */
	private final ModelProjection	sourceProjection;

	/**
	 * The projection of the new raster, typically the source.
	 */
	private final ModelProjection	targetProjection;

}
