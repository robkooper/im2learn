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

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.imagetools.core.datatype.*;
import edu.illinois.ncsa.isda.imagetools.core.geo.projection.*;


/**
 * This class will supply data to the data mining and visualization components.
 * 
 * Each object will contain a set of virtual data rasters and a set of virtual mask
 * rasters (see VirtualRaster class for a description and functionality of both).
 * 
 * The end result is an ImageObject (and a corresponding to it table), such that its pixels
 * are specified by the user-defined grid (bounding box + resolution) and projection. At
 * each pixel, the number of bands is equal to the sum of numbers of bands of individual
 * rasters. The values are stacked values from the rasters. The masks specify which pixels
 * should be annotated by values (as well as output into the table).
 * 
 * @author Yakov Keselman
 */
public class VirtualRasterIntegrator {

	static private Log logger = LogFactory.getLog( VirtualRaster.class );

    /**
     * The name of the raster (to be used in derived tables and variables).
     */
    protected String name;
    
    
    /**
     * The target projection of the integrated raster.
     */
    protected ModelProjection targetProjection;
    
    
    /**
     * Data values obtained from the integration.
     */
    protected ImageObject dataOutputRaster;
    
    /**
     * Uncertainty values obtained from the integration.
     */
    protected ImageObject uncertaintyOutputRaster;
    
    
	/**
	 * Data rasters associated with the object.
	 */
	protected ArrayList< VirtualRaster > dataRasters = new ArrayList< VirtualRaster >();
	
	/**
	 * Mask rasters associated with the object.
	 */
	protected ArrayList< VirtualRaster > maskRasters = new ArrayList< VirtualRaster >();;

	
    /**
     * Names of variables that will label variable-related table columns.
     */
    protected ArrayList< String > variables = new ArrayList< String >();
    

	/**
	 * No-argument constructor.
	 */
	public VirtualRasterIntegrator()
	{	
	}
	
	/**
	 * Constructor from a projection.
	 */
	public VirtualRasterIntegrator( ModelProjection projection )
	{
		setTargetProjection( projection );
	}
	
	/**
	 * Constructor from projection and virtual data/mask rasters.
	 */
	public VirtualRasterIntegrator( ModelProjection projection,
			VirtualRaster[] dataRasters, VirtualRaster[] maskRasters )
	{
		for( VirtualRaster raster: dataRasters )
			addDataRaster( raster );
		for( VirtualRaster raster: maskRasters )
			addMaskRaster( raster );
		setTargetProjection( projection );
	}

	
	/**
	 * Sets the name of the VirtualRaster (to be used for saving in a table).
	 */
	public void setName( String name )
	{
		this.name = name;
	}
	
	/**
	 * Gets the name of the VirtualRaster (to be used for labeling table rows and variables).
	 */
	public String getName( )
	{
		return this.name;
	}
	
		
    /**
     * Adds the given virtual data raster to the collection.
     * @param raster to add to the collection.
     */
    public void addDataRaster( VirtualRaster raster )
    {
    	raster.setTargetProjection( targetProjection );
    	dataRasters.add( raster );
    }
        
    /**
     * Adds the given virtual mask raster to the collection.
     * @param raster to add to the collection.
     */
    public void addMaskRaster( VirtualRaster raster )
    {
    	raster.setTargetProjection( targetProjection );
    	maskRasters.add( raster );
    }
    
	/**
     * Removes the given data raster from the collection.
	 * @param raster to remove from the collection.
	 */
	public void removeDataRaster( VirtualRaster raster )
	{
		dataRasters.remove( raster );
	}
	
	/**
     * Removes the given mask raster from the collection.
	 * @param raster to remove from the collection.
	 */
	public void removeMaskRaster( VirtualRaster raster )
	{
		maskRasters.remove( raster );
	}
	
	
    /**
     * Set the target projection of the resulting raster.
     */
    public void setTargetProjection( ModelProjection projection )
    {
        // update the target projection of this virtual raster.
        this.targetProjection = projection;
        
        // update the target projection of ImageObjectGeographic's.
        for( VirtualRaster raster: dataRasters )
            raster.setTargetProjection( projection );            
        for( VirtualRaster raster: maskRasters )
            raster.setTargetProjection( projection );            
    }
    
	
    /**
     * Computes the stack of raster data values at the grid points.
     * @param grid the 2D grid of points to get the values at.
     * @param the type of the ImageObject to get out.
     * @return An ImageObject with values filled in.
     */
	public ImageObject getData( SamplingGrid2D grid, int type )
	{
		if( dataOutputRaster == null )
			recomputeValues( grid, type );
		return dataOutputRaster;
	}
	
	
    /**
     * Computes the stack of raster uncertainty values at the grid points.
     * @param grid the 2D grid of points to get the values at.
     * @param the type of the ImageObject to get out.
     * @return An ImageObject with values filled in.
     */
	public ImageObject getUncertainty( SamplingGrid2D grid, int type )
	{
		if( this.uncertaintyOutputRaster == null )
			recomputeValues( grid, type );
		return this.uncertaintyOutputRaster;
	}
	
	
    /**
     * Copies data and uncertainty values from virtual rasters, based on masks.
     * Call this explicitly if new data and uncertainty values need to be filled in.
     */
    public void recomputeValues( SamplingGrid2D grid, int type )
    {
    	// a list of rasters that will actually be used in the output image
    	ArrayList< VirtualRaster > usedDataRasters =
    		new ArrayList< VirtualRaster >();

    	// compute data and uncertainty rasters, catching all exceptions.
    	for( VirtualRaster raster: dataRasters )
    	{
    		try
    		{
        		raster.getData(	grid, type );	// compute and save data
        		raster.getUncertainty( grid, type );	// same for uncertainty
        		usedDataRasters.add( raster );
    		}
    		catch( Exception e )
    		{
    			logger.info( "Raster " + raster.getFileName() + " is not used." );
    		}
    	}
    	
    	
    	// initialize variables that will label table columns.
    	for( int i = 0; i < usedDataRasters.size(); i ++ )
    	{
    		VirtualRaster raster = usedDataRasters.get( i );
    		for( int band = 0; band < raster.getNumBands(); band ++ )
    		{
    			variables.add( raster.getFileName() + "." + band );
    		}
    	}
    	int totalBands = variables.size();
		
    	// create the two rasters, data and uncertainty.
    	try
    	{
        	dataOutputRaster = ImageObject.createImage( grid.ysize(), grid.xsize(),
        			type, totalBands, false /* data as well */ );
        	uncertaintyOutputRaster = ImageObject.createImage( grid.ysize(), grid.xsize(),
        			type, totalBands, false /* data as well */ );
    	}
    	catch( ImageException e )
    	{
    		dataOutputRaster = null;
    		uncertaintyOutputRaster = null;
    		return;
    	}
    	
        // fill in the actual data. use access patterns of the resulting rasters.
    	// will need to add masking code once that's done.
        
    	for( SubArea area: dataOutputRaster.getTileBoxes() )
    	{
    		// the band in the integrated image.
    		int band = 0;
    		
    		// go over the rasters in a fixed fashion.
	        for( int i = 0; i < usedDataRasters.size(); i ++ )
	        {
	        	VirtualRaster raster = usedDataRasters.get( i );
	        	
	        	// get the pre-computed/pre-stored data and uncertainty images.
	        	ImageObject dataImage = raster.getData( grid, type );
	        	ImageObject uncertaintyImage = raster.getUncertainty( grid, type );
	        	
	        	// go carefully, copying the values.
	        	int numBands = dataOutputRaster.getNumBands();
        		for( int row = area.getRow(); row < area.getEndRow(); row ++ )
        		{
        			for( int col = area.getCol(); col < area.getEndCol(); col ++ )
        			{	        				
    		        	for( int b=0; b<numBands; b++ )
    		        	{
        		            try
        		            {
        		            	// use the most generic "getDouble" of the rasters.
        		                dataOutputRaster.set( row, col, band+b,
        		                		dataImage.getDouble( row, col, b ) );
        		                uncertaintyOutputRaster.set( row, col, band+b,
        		                		uncertaintyImage.getDouble( row, col, b ) );
        		            }
        		            catch( Exception e )
        		            {
        		            	// if did not get the value, it's out of range; continue.
        		            	continue;
        		            }        		        		
    		        	}
        			}
        		}
        		
        		// go to the next stack of values.
        		band += raster.getNumBands();
			}
    	}
    	
    }
    
}
