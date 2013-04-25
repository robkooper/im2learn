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
package edu.illinois.ncsa.isda.imagetools.core.datatype;

import java.util.ArrayList;

/**
 * Transformations defined on pairs and sets of SubAreas.
 * @author Yakov Keselman
 *
 */
public class SubAreaTransform {
	
	
	/**
	 * Initializer from tile boxes of _source_ ImageObject and from areas of _source_ and _target_
	 * ImageObject's. Source ImageObject will supply pixel values. Target ImageObject's pixel values
	 * will be overwritten.
	 * @param sourceTileBoxes the tile boxes of the _source_ ImageObject.
     * @param sourceTransformArea the corresponding area of the _source_ ImageObject.
     * @param targetTransformArea the area of the _target_ ImageObject.
	 */
	public SubAreaTransform( ArrayList< SubArea > sourceTileBoxes,
    		SubArea sourceTransformArea, SubArea targetTransformArea )
	{
		this.setSourceTileBoxes( sourceTileBoxes );
		this.setSourceTransformArea( sourceTransformArea );
		this.setTargetTransformArea( targetTransformArea );
	}
	
	
    /**
     * Takes a target area and produces an array of target sub-areas such that iteration over them
     * will result in few pages of source tiles.
     * @param targetArea the bounding box of the target ImageObject (pixels being overwritten).
     * @return an array of boxes in target coordinates (ImageObject whose pixels are being modified).
     */
	public ArrayList< SubArea > getTargetTileBoxes( SubArea targetArea )
	{
    	// now, go through source areas and map them into target areas.
    	ArrayList< SubArea > newAreas = new ArrayList< SubArea >();
    	for( SubArea source: sourceTileBoxes )
    	{
    		SubArea sourceTransformed = this.toTarget( source );
    		SubArea intersec = intersection( sourceTransformed, targetArea );
    		if( intersec != null )
    			newAreas.add( intersec );
    	}
    	
    	return newAreas;
    }
    
    
	/**
	 * Splits the given source area into a number of tiles.
	 * @param sourceArea to split into tiles.
	 * @return a list of SubAreas over which iteration in source coordinates should be fast.
	 */
	public static ArrayList< SubArea > getTileBoxesArea( 
			ArrayList< SubArea > sourceTileBoxes, SubArea sourceArea )
	{
    	ArrayList< SubArea > newAreas = new ArrayList< SubArea >();
    	for( SubArea source: sourceTileBoxes )
    	{
    		SubArea intersec = intersection( source, sourceArea );
    		if( intersec != null )
    			newAreas.add( intersec );
    	}
    	
    	return newAreas;    		
   	}
	
	
	protected void setSourceTileBoxes( ArrayList< SubArea > sourceTileBoxes )
	{
		this.sourceTileBoxes = sourceTileBoxes;
	}
	
	protected void setSourceTransformArea( SubArea sourceTransformArea )
	{
		this.sourceTransformArea = sourceTransformArea;
	}
	
	protected void setTargetTransformArea( SubArea targetTransformArea )
	{
		this.targetTransformArea = targetTransformArea;
	}

	
	/**
	 * Transforms an area from source image coordinates to target image coordinates.
	 * Justification for the formula below:
	 * First, notice that the transformation is linear in row and col; so we need to treat just col. 
	 * Denote source.getCol() c_s, source.getWidth() w_s, target.getCol() c_t, target.getWidth() w_t.
	 * Then, we are looking for a transformation that maps c_s to c_t and c_s+w_s to c_t+w_t.
	 * Denote the transformation by f(c) = a*c+b. Then,
	 * a*c_s + b = c_t;
	 * a*(c_s + w_s) + b = c_t + w_t.
	 * Subtracting the first equation from the second, we get:
	 * a = w_t/w_s;
	 * b = c_t - a*c_s = c_t - w_t/w_s*c_s.
	 * Then, f(c) = w_t/w_s*(c - c_s) + c_t.
	 * Check: f(c_s) = c_t; f(c_s+w_s) = w_t + c_t.
	 */
	protected SubArea toTarget( SubArea source )
	{
		// compute transformation parameters.
    	double rowScale = (double)targetTransformArea.getHeight() / sourceTransformArea.getHeight();
    	int rowTarget = targetTransformArea.getRow();
    	int rowSource = sourceTransformArea.getRow();
    	double colScale = (double)targetTransformArea.getWidth() / sourceTransformArea.getWidth();
    	int colTarget = targetTransformArea.getCol();
    	int colSource = sourceTransformArea.getCol();
    	
    	// compute a representation of the source area in the target coordinates.
		int newCol = (int)(colScale*( source.getCol() - colSource ) + colTarget);
		int newRow = (int)(rowScale*( source.getRow() - rowSource ) + rowTarget);
		int newWidth = (int)Math.ceil(source.getWidth()*colScale);
		int newHeight = (int)Math.ceil(source.getHeight()*rowScale);
		
		return new SubArea( newCol, newRow, newWidth, newHeight );
	}
	
	
	/**
	 * Computes the intersection of two areas (can be used to compute the intersection of
	 * a source area in target coordinates with a target area).
	 * @return null if there is no intersection.
	 */
	public static SubArea intersection( SubArea area1, SubArea area2 )
	{
		int startRow = Math.max( area1.getRow(), area2.getRow() );
		int startCol = Math.max( area1.getCol(), area2.getCol() );
		int endRow = Math.min( area1.getEndRow(), area2.getEndRow() );
		int endCol = Math.min( area1.getEndCol(), area2.getEndCol() );
		if( (startRow < endRow) & (startCol < endCol) )
			return new SubArea( startCol, startRow, endCol-startCol, endRow-startRow );
		else
			return null;
	}

	    
	/**
	 * Source tile boxes.
	 */
	protected ArrayList< SubArea > sourceTileBoxes;
	
	/**
	 * Source transform area.
	 */
	protected SubArea sourceTransformArea;
	
	/**
	 * Target transform area.
	 */
	protected SubArea targetTransformArea;
	
}
