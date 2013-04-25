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
package edu.illinois.ncsa.isda.im2learn.ext.virtualraster;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.im2learn.core.datatype.*;
import edu.illinois.ncsa.isda.im2learn.core.geo.projection.*;


/**
 * A virtual raster is a raster that is a container for several "similar" ImageObject.
 * components. The components must agree on the number of bands, projection, etc.
 * In other words, these ImageObjects should be thought of as portions of a single raster.
 * The component ImageObjects may be non-adjacent (such as California and New Jersey).
 * 
 * A virtual raster can be queried only using a specific projected coordinate system.
 * 
 * Typical operations: add raster, remove raster, query the values at a grid of locations.
 * 
 * The value at a location comes from a raster covering that location (no averaging).
 * 
 * Note that for the purposes of GeoLearn, there may be several data VirtualRaster's and several
 * mask VirtualRaster's. VirtualRasterIntegrator integrates multiple virtual rasters into two
 * rasters (ImageObject's): data and uncertainty. Both can be processed as regular ImageObject's.
 * 
 * @author Yakov Keselman
 * @version August 4, 2006.
 */
public class VirtualRaster {

	static private Log logger = LogFactory.getLog( VirtualRaster.class );

	
	/**
	 * The type of the image == the type of its component rasters.
	 */
	protected int type;
	
	
	/**
	 * The number of bands in the image == the number of bands in its component rasters.
	 */
	protected int numBands;
	

	/**
	 * The invalid data marker.
	 */
	protected double invaliddata;
	
	
	/**
	 * The file name associated with this raster.
	 */
	protected String filename;
	
	
    /**
     * The managed rasters.
     */
    protected ArrayList< ImageObjectGeographic > rasters =
    	new ArrayList< ImageObjectGeographic >();

    
    /**
     * The target projection of the virtual raster.
     */
    protected ModelProjection targetProjection;
    
    
    /**
     * The grid of the virtual raster.
     */
    protected SamplingGrid2D grid;
    
    
    /**
     * The resulting data raster.
     */
    protected ImageObject dataRaster;
    
    
    /**
     * The resulting uncertainty raster.
     */
    protected ImageObject uncertaintyRaster;
    
    
	/**
	 * Constructor from a projection.
	 */
	public VirtualRaster( ModelProjection projection )
	{
		setTargetProjection( projection );
	}
	
	/**
	 * Constructor from projection and rasters.
	 */
	public VirtualRaster( ModelProjection projection, ImageObject[] rasters )
	{
		setTargetProjection( projection );
		for( ImageObject raster: rasters )
			addRaster( raster );
	}

	
    /**
     * Adds the given raster to the collection.
     * @param raster to add to the collection.
     */
    public ImageObjectGeographic addRaster( ImageObject raster )
    {
    	// copy over some important characteristics.
    	this.type = raster.getType();
    	this.numBands = raster.getNumBands();
    	this.filename = raster.getFileName();
    	this.invaliddata = raster.getInvalidData();
    	
    	// create a new geographic raster and add it to the collection.
    	ImageObjectGeographic geoRaster = new ImageObjectGeographic( raster );
    	geoRaster.setTargetProjection( targetProjection );
    	rasters.add( geoRaster );
    	return geoRaster;
    }
        
	/**
     * Removes the given raster from the collection.
	 * @param raster to remove from the collection.
	 */
	public void removeRaster( ImageObjectGeographic raster )
	{
		rasters.remove( raster );
	}
	
	
    /**
     * Set the target projection of the raster.
     */
    public void setTargetProjection( ModelProjection projection )
    {
        // update the target projection of this virtual raster.
        targetProjection = projection;
        
        // update the target projection of ImageObjectGeographic's.
        for( ImageObjectGeographic raster: rasters )
            raster.setTargetProjection( projection );            
    }
    
	
    /**
     * Computes the stack of raster data values at the grid points.
     * @param grid the 2D grid of points to get the values at.
     * @return An ImageObject with values filled in.
     */
	public ImageObject getData( SamplingGrid2D grid )
	{
		return getData( grid, type );
	}
	
	
    /**
     * Computes the stack of raster uncertainty values at the grid points.
     * @param grid the 2D grid of points to get the values at.
     * @return An ImageObject with values filled in.
     */
	public ImageObject getUncertainty( SamplingGrid2D grid )
	{
		return getUncertainty( grid, type );
	}
	
	
    /**
     * Computes the stack of raster data values at the grid points.
     * @param grid the 2D grid of points to get the values at.
     * @param type the type of the target object.
     * @return An ImageObject with values filled in.
     */
	public ImageObject getData( SamplingGrid2D grid, int type  )
	{
		if( dataRaster == null ) recomputeValues( grid, type );
		return dataRaster;
	}
	
	
    /**
     * Computes the stack of raster uncertainty values at the grid points.
     * @param grid the 2D grid of points to get the values at.
     * @param type the type of the target object.
     * @return An ImageObject with values filled in.
     */
	public ImageObject getUncertainty( SamplingGrid2D grid, int type )
	{
		if( uncertaintyRaster == null ) recomputeValues( grid, type );
		return uncertaintyRaster;
	}
	
	
    /**
     * Computes the stack of raster data and uncertainty values at the grid points.
     * This is an explicit call to compute values, in case they need to be refreshed.
     * @param grid the 2D grid of points to get the values at.
     */
    public void recomputeValues( SamplingGrid2D grid )
    {
    	this.recomputeValues( grid, type );
    }
    
    
    /**
     * Computes the stack of raster data and uncertainty values at the grid points.
     * This is an explicit call to compute values, in case they need to be refreshed.
     * @param grid the 2D grid of points to get the values at.
     * @param type the type of the target object.
     */
    /*
     * NOTE: if there is no data for certain tiles in the underlying rasters, then those
     * tiles are not initialized.
     */
    public void recomputeValues( SamplingGrid2D grid, int inType )
    {
    	// remember the grid for output into a table.
		this.grid = grid;
		
		// remember the type.
		final int finalType = inType;
		
		// remember the dimensions.
		final int numRows = grid.ysize();
		final int numCols = grid.xsize();
		
    	// create the images with data allocated.
    	// also set some of their essential parameters.
		ImageObject[] outRaster = {dataRaster, uncertaintyRaster};
		String[] prefix = { "data_", "uncertainty_" };
		
		// since both of them look alike, create them and set their parameters in a loop.
		for( int i=0; i<2; i++ )
		{
	    	try
	    	{
	        	outRaster[i] = ImageObject.createImage( numRows, numCols,
	        			numBands, finalType, false /* data as well */ );
	        	outRaster[i].setProperty( ImageObject.GEOINFO, targetProjection );
	        	// the line below has to be modified for fully-qualified filenames (start with c:\).
	        	// outRaster[i].setProperty( ImageObject.FILENAME, prefix[i] + this.getFileName() );
	        	outRaster[i].setProperty( ImageObject.FILENAME, prefix[i] );
	            // outRaster[i].setProperty("XResolution", null);	// TODO
	            // outRaster[i].setProperty("YResolution", null);	// TODO
	        	outRaster[i].setInvalidData( invaliddata );
	    	}
	    	catch( ImageException e )
	    	{
	    		outRaster[i] = null;
	    		return;
	    	}
		}
		
	    // since we're using references, not aliases, re-assign the results.
	    dataRaster = outRaster[0];
	    uncertaintyRaster = outRaster[1];
	    
    	// sampling radius, for multiple-pixel schemes.
    	double radius[] = new double[2];
    	radius[0] = grid.xRadius();
    	radius[1] = grid.yRadius();
    	
    	// assume that the result is out-of-core and work with boxes.
    	// assume that the result's tiles are of the same scale as of originals.
    	// if this assumption is not true, will also need to partition the rasters.
    	for( SubArea area: dataRaster.getTileBoxes() )
    	{
    		// go over the rasters.
	        for( ImageObjectGeographic raster: rasters )
	        {
            	// check that the target raster's area intersects with the source raster.
	        	SubArea common = SubAreaTransform.intersection( 
	        			raster.getBoundingBoxInTargetRaster(), area );
	        	
	        	if( common != null )
	        	{
		        	logger.info( "common: " + common );
		        	
	        		for( int row = common.getRow(); row < common.getEndRow(); row ++ )
	        		{
	        			for( int col = common.getCol(); col < common.getEndCol(); col ++ )
	        			{
	        				double[] point = grid.getXY( col, row );
	        				
	    		        	for( int b=0; b<numBands; b++ )
	    		        	{
	        		            try
	        		            {
	        		            	// single-pixel scheme.
	        		            	switch( finalType )
	        		            	{
	        		            	case ImageObject.TYPE_BYTE:
		        		                dataRaster.setByte( row, col, b, raster.getByte( point, b ) );
		        		                uncertaintyRaster.setByte( row, col, b, raster.getByte( point, b ) );
		        		                break;
	        		            	case ImageObject.TYPE_SHORT:
	        		            	case ImageObject.TYPE_USHORT:
		        		                dataRaster.setShort( row, col, b, raster.getShort( point, b ) );
		        		                uncertaintyRaster.setShort( row, col, b, raster.getShort( point, b ) );
		        		                break;
	        		            	case ImageObject.TYPE_INT:
		        		                dataRaster.setInt( row, col, b, raster.getInt( point, b ) );
		        		                uncertaintyRaster.setInt( row, col, b, raster.getInt( point, b ) );
		        		                break;
	        		            	case ImageObject.TYPE_LONG:
		        		                dataRaster.setLong( row, col, b, raster.getLong( point, b ) );
		        		                uncertaintyRaster.setLong( row, col, b, raster.getLong( point, b ) );
		        		                break;
	        		            	case ImageObject.TYPE_FLOAT:
		        		                dataRaster.setFloat( row, col, b, raster.getFloat( point, b ) );
		        		                uncertaintyRaster.setFloat( row, col, b, raster.getFloat( point, b ) );
		        		                break;
	        		            	case ImageObject.TYPE_DOUBLE:
		        		                dataRaster.setDouble( row, col, b, raster.getDouble( point, b ) );
		        		                uncertaintyRaster.setDouble( row, col, b, raster.getDouble( point, b ) );
		        		                break;
	        		            	}
	        		                
	        		                // multiple-pixel scheme.
	        		                // dataRaster.set( row, col, b, raster.getDouble( point, radius, b ) );
	        		                // uncertaintyRaster.set( row, col, b, raster.getDouble( point, radius, b ) );
	        		            }
	        		            catch( Exception e )
	        		            {
	        		            	// if error, indices are out of range or value was not set.
	        		            	break;
	        		            }        		        		
	    		        	}
	        			}
	        		}
	        	}
		        else	// debugging info.
		            logger.info( "common is null: " + 
		            		raster.getBoundingBoxInTargetRaster() + " and " + area );
			}
		}
    	
    	String[] projLabels = { "Proj.x", "Proj.y" };
    	this.saveCSVTable( projLabels );
	}

    
    /**
     * Save the virtual raster as a CSV table, one row per pixel.
     * Important note: will save only those pixels with valid data.
     * @param labels to give to the columns of the resulting table.
     */
    public void saveCSVTable( String[] labels )
    {
    	// make sure we have the data and uncertainty values.
    	ImageObject data = getData( grid );
    	ImageObject uncertainty = getUncertainty( grid );
    	
    	// save data and uncertainty values separately.
    	//data.saveCSVTable( data.getFileName() + ".csv", labels, grid );
    	//uncertainty.saveCSVTable( uncertainty.getFileName() + ".csv", labels, grid );
    	// data.saveCSVTable( data.getFileName() + ".csv" );
    	// uncertainty.saveCSVTable( uncertainty.getFileName() + ".csv" );
    }
    
    
    /**
     * @return the type of the virtual raster (the same as the type of its components).
     */
    public int getType()
    {
    	return this.type;
    }
    
    /**
     * @return the number of bands in the virtual raster
     * (the same as the number of bands in its components).
     */
    public int getNumBands()
    {
    	return this.numBands;
    }
    
    /**
     * @return the invalid data marker.
     */
    public double getInvalidData()
    {
    	return this.invaliddata;
    }
    
	/**
	 * Gets the name of the VirtualRaster (can be used for labeling table rows and variables).
	 */
	public String getFileName( )
	{
		return this.filename;
	}
	
}
