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
package edu.illinois.ncsa.isda.im2learn.core.datatype;

import java.awt.image.BufferedImage;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * This is a specific implementation of the out-of-core functionality. It is based on
 * tile-wise representation of images. Each image is split into a number of tiles,
 * each behaving as a small ImageObject. To manage the collection of tiles (page
 * them in and out and to serialize the whole collection), an ImageObjectTileManager
 * (which should be thought of as an inner class of this class) is used.
 * 
 * @author Yakov Keselman
 *
 */
public class ImageObjectOutOfCore extends ImageObject  implements Externalizable {
        private static final long serialVersionUID = 1L;

	/**
	 * Logger for the class.
	 */
	static private Log logger = LogFactory.getLog( ImageObjectOutOfCore.class );
	
	
	/**
	 * The approximate amount of available memory to all objects of this class.
	 */
	static protected int totalMemoryAvailable = 5*1024*1024*16;	// last number: MB
	
	
	/** 
	 * Memory manager and other things for the collection of tiles. 
	 */
	protected ImageObjectTileManager manager;

	
    /**
     * The ratio of the number of rows (or columns) of the in-core representation
     * of the image to that of the original out-of-core image.
     */
    protected double inCoreScale;
    
    
    /**
     * A cached in-core representation of the object (delete if too much memory gets taken).
     */
    protected ImageObjectInCore inCore = null;
    
    
    /**
     * Sets the total amount of memory available (in MB).
     * @param memoryMB
     */
    public static void setTotalMemoryAvailableMB( int memoryMB )
    {
    	totalMemoryAvailable = 1024 * 1024 * memoryMB;
    }
    		
    
    /**
     * @return the total amount of memory available (in MB).
     * @param memoryMB
     */
    public static int getTotalMemoryAvailableMB( )
    {
    	return totalMemoryAvailable / (1024*1024);
    }
    
    
    /**
     * Empty constructor needed for externalization. This constructor should not
     * be used by any code except for the externalization functions.
     */
    public ImageObjectOutOfCore() {
        this(1, 1, 1, true, TYPE_UNKNOWN);
    }

    /**
     * Create a new image. This will create an image with requested/default parameters.
     *
     * @param rows       of pixels in the image.
     * @param cols       of pixels in the image.
     * @param bands      or samples per pixel in the image.
     * @pararm type      of the image (one of the listed in ImageObject).
     * @param headeronly is true if no array should be created to store the pixels.
     */
    public ImageObjectOutOfCore( int rows, int cols, int bands, boolean headeronly,	int type )
    {
    	// invoke initialization that will create tiles, etc.
    	super( rows, cols, bands, headeronly, type );
    }
    
    // ------------------------------------------------------------------------
    // SPECIAL CONSTRUCTORS
    // ------------------------------------------------------------------------

    /**
     * Create a new image. The image created has the requested size and type.
     *
     * @param rows   of pixels in the image.
     * @param cols   of pixels in the image.
     * @param bands  or samples per pixel in the image
     * @param type   of the image.
     * @param header is true if no data should allocated.
     * @return a newly created image.
     * @throws ImageException if the type is not valid.
     */
    static public ImageObjectOutOfCore createImage(
    		int rows, int cols, int bands, int type, boolean header )
    throws ImageException {
    	return new ImageObjectOutOfCore( rows, cols, bands, header, type );
    }
    
    public void destroy() {
    	if (manager != null) {
    		manager.destroy();
    	}
    	if (inCore != null) {
    		inCore.deleteArray();
    		inCore = null;
    	}
    	super.destroy();
    }
    
    // ------------------------------------------------------------------------
    // The right way to access data in tiled objects is through tiles.
    // ------------------------------------------------------------------------

    /**
     * Returns all areas over which iteration should be fast.
     */
    public ArrayList< SubArea > getTileBoxes()
    {
    	ArrayList< SubArea > subAreas = new ArrayList< SubArea >();
    	for( ImageObjectPagedTile tile: manager.allTiles )
    	{
    		subAreas.add( new SubArea( tile.getColOffset(), tile.getRowOffset(), 
    				tile.getNumCols(), tile.getNumRows() ) );
    	}
    	return subAreas;
    }
    
    /**
     * Check to see whether the tile corresponding to the area has valid data.
     * @param area of a tile.
     * @return true if the tile has been written into.
     */
    public boolean hasValidData( SubArea area )
    {
    	return (manager.getTile( area.getRow(), area.getCol(), 0 )).hasValidData();
    }
    
    
    // ------------------------------------------------------------------------
    // GETTERS & SETTERS
    // ------------------------------------------------------------------------

    /**
     * Calculate minimum and maximum values for image. This function will find
     * the minimum and maximum values per band and per image. Make sure to call
     * this function if you chance any values directly in getData().
     */
    public void computeMinMax()
    {	
        // if there are no data tiles, set min/max to undefined values.
        if( isHeaderOnly() )
        {
            for( int b = 0; b <= numbands; b++ )
            {
                minval[b] = Double.NaN;
                maxval[b] = Double.NaN;
            }
            return;
        }

        // set all values to absolute min/max
        for( int b = 0; b < numbands+1; b++ )
        {
            minval[b] = Double.POSITIVE_INFINITY;
            maxval[b] = Double.NEGATIVE_INFINITY;
        }
        
        // loop through the tiles, using their min's and max's.
        if( Double.isNaN(invaliddata) )
        {
            for( ImageObjectPagedTile tile: manager.allTiles )
           	{
                for( int b=0; b<numbands; b++ )
                {
                	// check against the minimum.
                	double minv = tile.getMin(b);
                	if( minval[b] > minv ) minval[b] = minv;
                	if( minval[numbands] > minv ) minval[numbands] = minv;
                	
                	// check against the maximum.
                	double maxv = tile.getMax(b);
                	if( maxval[b] < maxv ) maxval[b] = maxv;
                	if( maxval[numbands] < maxv ) maxval[numbands] = maxv;
            	}
            }
        }
        else 
        {
            for( ImageObjectPagedTile tile: manager.allTiles )
           	{
                for( int b=0; b<numbands; b++ )
                {
                	// check against the minimum.
                	double minv = tile.getMin(b);
                	if( minv == invaliddata ) continue;
                	if( minval[b] > minv ) minval[b] = minv;
                	if( minval[numbands] > minv ) minval[numbands] = minv;
                	
                	// check against the maximum.
                	double maxv = tile.getMax(b);
                	if( maxv == invaliddata ) continue;
                	if( maxval[b] < maxv ) maxval[b] = maxv;
                	if( maxval[numbands] < maxv ) maxval[numbands] = maxv;
            	}
            }
        }
            
        // funny case where all data in a band was invalid
        for( int b = 0; b < numbands+1; b++ )
        {
        	if( minval[b] > maxval[b] )
        	{
        		minval[b] = Double.NaN;
        		maxval[b] = Double.NaN;
        	}
        }
        
        minmaxinvalid = false;

        // logger.debug("Max : " + maxval[0] + "   Min : " + minval[0]);
    }


    /**
     * Set the size as before. Also compute the scale of the in-core representation
     * (the ratio of sizes of in-core and out-of-core).
     */
    public void setSize( int numcols, int numrows, int numbands )
    {
    	super.setSize( numcols, numrows, numbands );
    	
    	// compute and set scale of the object.
    	double maxRowCols = 1.0 * ImageObject.MAX_IN_CORE_SIZE;
    	inCoreScale = Math.sqrt( maxRowCols / (numrows*numcols) );
    	
    	// just in case, make sure we don't make the object larger.
    	if( inCoreScale > 1 )
    		inCoreScale = 1;
    }
    
    public double getSuggestedScale() {
    	double maxRowCols = 1.0 * ImageObject.MAX_IN_CORE_SIZE;
    	double scale = Math.sqrt( maxRowCols / (numrows*numcols) );
    	
    	// just in case, make sure we don't make the object larger.
    	if( scale > 1 )
    		scale = 1;
    	
    	return scale;
    }
    
    /**
     * Convert this out-of-core ImageObject to a buffered image that can be rendered.
     * Does so by converting the object to a base ImageObject, then using its method.
     */
    public BufferedImage toBufferedImage(BufferedImage bi, boolean fakergb,
            double[] scale, boolean usetotals,
            int redband, int greenband, int blueband,
            boolean grayscale, int grayband,
            int[] gammatable, double alpha,
            double imagescale)
    {
    	logger.info( "Converting an out-of-core to a buffered image via in-core." );
    	
    	 /*
    	return getScaledInCore( inCoreScale ).toBufferedImage(
    			bi, fakergb, scale, usetotals, redband, greenband, blueband,
    			grayscale, grayband, gammatable, alpha, imagescale );
    	 */
    	
    	/*
    	return getScaledInCore( imagescale ).toBufferedImage(
    			bi, fakergb, scale, usetotals, redband, greenband, blueband,
    			grayscale, grayband, gammatable, alpha, imagescale );
    	*/
    	
    	/*
    	return getScaledInCore( imagescale ).toBufferedImage(
    			bi, fakergb, scale, usetotals, redband, greenband, blueband,
    			grayscale, grayband, gammatable, alpha, 1.0 );
    	*/
    	return getScaledInCore( imagescale ).toBufferedImage(
    			bi, fakergb, scale, usetotals, redband, greenband, blueband,
    			grayscale, grayband, gammatable, alpha, 1.0 );

    }
    
    
    /**
     * Convert this out-of-core ImageObject to an in-core representation.
     * @return a scaled down (to the point of being in-core) version of this object.
     */
    public ImageObjectInCore getScaledInCore( double scale )
    {
    	// check if we have a version.
    	if( scale == inCoreScale )
    		if( inCore != null )
    			return inCore;
    	
    	// assume that we don't. use the scaling method.
    	try
    	{
        	inCore = scaledInCore( scale );
        	inCoreScale = scale;
    	}
    	catch( Exception e )
    	{
    		inCore = null;
    	}
    	return inCore;
    }
    
    
    /**
     * An idicator whether this ImageObject is an out-of-core implementation.
     */
    public boolean isOutOfCore()
    {
    	return true;
    }
    

    /**
     * Creates data associated with the image (uninitialized).
     */
    public void createArray()
    {
    	manager = new ImageObjectTileManager( numrows, numcols, numbands, type );
        headerOnly = false;
        minmaxinvalid = true;
    }


    /**
     * Delete the manager along with data from the underlying tiles.
     */
    /*
     * Alternative: page out the tiles manually inside the manager.
     */
    public void deleteArray()
    {
    	manager = null;
    	headerOnly = true;
    }


    /**
     * NO RAW DATA ACCESS IS ALLOWED FOR THIS CLASS!
     * Will throw a NullPointerException each time it is called!
     */
    public Object getData()
    {
    	throw(new NullPointerException("ImageObjectOutOfCore has no direct data access."));
    }

    /**
     * NO RAW DATA ACCESS IS ALLOWED FOR THIS CLASS!
     * Will throw a NullPointerException each time it is called!
     */
    public void setData( Object data )
    {
    	throw(new NullPointerException("ImageObjectOutOfCore has no direct data access."));
    }

    /** Sets all values in all data cells to the default value. */
    public void setData( double defaultval )
    {
    	for( ImageObjectPagedTile tile: manager.allTiles )
    		tile.setData( defaultval );
    }
    
    
    /**
     * The minimal value in the type.
     */
    public double getTypeMin( )
    {
        try
        {
            return manager.currentTile.getTypeMin();
        }
        catch( Exception e )
        {
            return manager.getTile( 0, 0, 0 ).getTypeMin();            
        }
    }
    
    /**
     * The maximal value in the type.
     */
    public double getTypeMax( )
    {
        try
        {
            return manager.currentTile.getTypeMax();
        }
        catch( Exception e )
        {
            return manager.getTile( 0, 0, 0 ).getTypeMax();            
        }
    }
    
    
    /**
     * A converter from single integer index into data to row, column, band.
     * [0]: row; [1]: col; [2]: band.
     */
    protected int[] getRCB( int i )
    {
    	int[] toReturn = new int[3];
    	toReturn[2] = i % numbands;
    	i /= numbands;
    	toReturn[1] = i % numcols;
    	toReturn[0] = i/numcols;
    	return toReturn;
    }
    

    // DATA ACCESSORS ARE BELOW //        // DATA ACCESSORS ARE BELOW //
    
    // vvvvvvv  Byte  vvvvvvv
    
    public byte getByte( int row, int col, int band )
    {
        try
        {
            return manager.currentTile.getByte( row, col, band );
        }
        catch( Exception e )
        {
        	try
        	{
                return manager.getTile( row, col, band ).getByte( row, col, band );        		
        	}
        	catch( Exception e1 )
        	{
        		return (byte)getInvalidData();
        	}
        }
    }

    public byte getByte( int i )
    {
    	int[] rcb = getRCB( i );
    	return getByte( rcb[0], rcb[1], rcb[2] );
    }
    

    public void setByte( int row, int col, int band, byte value )
    {
        try
        {
            manager.currentTile.setByte( row, col, band, value );
        }
        catch( Exception e )
        {
        	try
        	{
                manager.getTile( row, col, band ).setByte( row, col, band, value );
        	}
        	catch( Exception e1 ){ }
        }
    }

    public void setByte( int i, byte value )
    {
    	int[] rcb = getRCB( i );
    	setByte( rcb[0], rcb[1], rcb[2], value );
    }
    
    
    public void set( int row, int col, int band, byte value )
    {
        try
        {
            manager.currentTile.setByte( row, col, band, value );
        }
        catch( Exception e )
        {
        	try
        	{
                manager.getTile( row, col, band ).setByte( row, col, band, value );
        	}
        	catch( Exception e1 ){ }
        }
    }

    public void set( int i, byte value )
    {
    	int[] rcb = getRCB( i );
    	set( rcb[0], rcb[1], rcb[2], value );
    }
    
    // ^^^^^^^  Byte  ^^^^^^^
    
    
    // vvvvvvv  Short  vvvvvvv
    
    public short getShort( int row, int col, int band )
    {
        try
        {
            return manager.currentTile.getShort( row, col, band );
        }
        catch( Exception e )
        {
        	try
        	{
                return manager.getTile( row, col, band ).getShort( row, col, band );
        	}
        	catch( Exception e1 )
        	{
        		return (short)getInvalidData();
        	}
        }
    }

    public short getShort( int i )
    {
    	int[] rcb = getRCB( i );
    	return getShort( rcb[0], rcb[1], rcb[2] );
    }
    

    public void setShort( int row, int col, int band, short value )
    {
        try
        {
            manager.currentTile.setShort( row, col, band, value );
        }
        catch( Exception e )
        {
        	try
        	{
                manager.getTile( row, col, band ).setShort( row, col, band, value );
        	}
        	catch( Exception e1 ){ }
        }
    }

    public void setShort( int i, short value )
    {
    	int[] rcb = getRCB( i );
    	setShort( rcb[0], rcb[1], rcb[2], value );
    }
    
    
    public void set( int row, int col, int band, short value )
    {
        try
        {
            manager.currentTile.setShort( row, col, band, value );
        }
        catch( Exception e )
        {
        	try
        	{
                manager.getTile( row, col, band ).setShort( row, col, band, value );
        	}
        	catch( Exception e1 ){ }
        }
    }

    public void set( int i, short value )
    {
    	int[] rcb = getRCB( i );
    	set( rcb[0], rcb[1], rcb[2], value );
    }
    
    // ^^^^^^^  Short  ^^^^^^^
    
    
    // vvvvvvv  Int  vvvvvvv
    
    public int getInt( int row, int col, int band )
    {
        try
        {
            return manager.currentTile.getInt( row, col, band );
        }
        catch( Exception e )
        {
        	try
        	{
                return manager.getTile( row, col, band ).getInt( row, col, band );
        	}
        	catch( Exception e1 )
        	{
        		return (int)getInvalidData();
        	}
        }
    }

    public int getInt( int i )
    {
    	int[] rcb = getRCB( i );
    	return getInt( rcb[0], rcb[1], rcb[2] );
    }
    

    public void setInt( int row, int col, int band, int value )
    {
        try
        {
            manager.currentTile.setInt( row, col, band, value );
        }
        catch( Exception e )
        {
        	try
        	{
                manager.getTile( row, col, band ).setInt( row, col, band, value );
        	}
        	catch( Exception e1 ){ }
        }
    }

    public void setInt( int i, int value )
    {
    	int[] rcb = getRCB( i );
    	setInt( rcb[0], rcb[1], rcb[2], value );
    }
    
    
    public void set( int row, int col, int band, int value )
    {
        try
        {
            manager.currentTile.setInt( row, col, band, value );
        }
        catch( Exception e )
        {
        	try
        	{
                manager.getTile( row, col, band ).setInt( row, col, band, value );
        	}
        	catch( Exception e1 ){ }
        }
    }

    public void set( int i, int value )
    {
    	int[] rcb = getRCB( i );
    	set( rcb[0], rcb[1], rcb[2], value );
    }
    
    // ^^^^^^^  Int  ^^^^^^^
    
    
    // vvvvvvv  Long  vvvvvvv
    
    public long getLong( int row, int col, int band )
    {
        try
        {
            return manager.currentTile.getLong( row, col, band );
        }
        catch( Exception e )
        {
        	try
        	{
                return manager.getTile( row, col, band ).getLong( row, col, band );
        	}
        	catch( Exception e1 )
        	{
        		return (long)getInvalidData();
        	}
        }
    }

    public long getLong( int i )
    {
    	int[] rcb = getRCB( i );
    	return getLong( rcb[0], rcb[1], rcb[2] );
    }
    

    public void setLong( int row, int col, int band, long value )
    {
        try
        {
            manager.currentTile.setLong( row, col, band, value );
        }
        catch( Exception e )
        {
        	try
        	{
                manager.getTile( row, col, band ).setLong( row, col, band, value );
        	}
        	catch( Exception e1 ){ }
        }
    }

    public void setLong( int i, long value )
    {
    	int[] rcb = getRCB( i );
    	setLong( rcb[0], rcb[1], rcb[2], value );
    }
    
    
    public void set( int row, int col, int band, long value )
    {
        try
        {
            manager.currentTile.setLong( row, col, band, value );
        }
        catch( Exception e )
        {
        	try
        	{
                manager.getTile( row, col, band ).setLong( row, col, band, value );
        	}
        	catch( Exception e1 ){ }
        }
    }

    public void set( int i, long value )
    {
    	int[] rcb = getRCB( i );
    	set( rcb[0], rcb[1], rcb[2], value );
    }
    
    // ^^^^^^^  Long  ^^^^^^^
    
    
    // vvvvvvv  Float  vvvvvvv
    
    public float getFloat( int row, int col, int band )
    {
        try
        {
            return manager.currentTile.getFloat( row, col, band );
        }
        catch( Exception e )
        {
        	try
        	{
                return manager.getTile( row, col, band ).getFloat( row, col, band );
        	}
        	catch( Exception e1 )
        	{
        		return (float)getInvalidData();
        	}
        }
    }

    public float getFloat( int i )
    {
    	int[] rcb = getRCB( i );
    	return getFloat( rcb[0], rcb[1], rcb[2] );
    }
    

    public void setFloat( int row, int col, int band, float value )
    {
        try
        {
            manager.currentTile.setFloat( row, col, band, value );
        }
        catch( Exception e )
        {
        	try
        	{
                manager.getTile( row, col, band ).setFloat( row, col, band, value );
        	}
        	catch( Exception e1 ){ }
        }
    }

    public void setFloat( int i, float value )
    {
    	int[] rcb = getRCB( i );
    	setFloat( rcb[0], rcb[1], rcb[2], value );
    }
    
    
    public void set( int row, int col, int band, float value )
    {
        try
        {
            manager.currentTile.setFloat( row, col, band, value );
        }
        catch( Exception e )
        {
        	try
        	{
                manager.getTile( row, col, band ).setFloat( row, col, band, value );
        	}
        	catch( Exception e1 ){ }
        }
    }

    public void set( int i, float value )
    {
    	int[] rcb = getRCB( i );
    	set( rcb[0], rcb[1], rcb[2], value );
    }
    
    // ^^^^^^^  Float  ^^^^^^^
    
    
    // vvvvvvv  Double  vvvvvvv
    
    public double getDouble( int row, int col, int band )
    {
        try
        {
            return manager.currentTile.getDouble( row, col, band );
        }
        catch( Exception e )
        {
        	try
        	{
                return manager.getTile( row, col, band ).getDouble( row, col, band );
        	}
        	catch( Exception e1 )
        	{
        		return (double)getInvalidData();
        	}
        }
    }

    public double getDouble( int i )
    {
    	int[] rcb = getRCB( i );
    	return getDouble( rcb[0], rcb[1], rcb[2] );
    }
    

    public void setDouble( int row, int col, int band, double value )
    {
        try
        {
            manager.currentTile.setDouble( row, col, band, value );
        }
        catch( Exception e )
        {
        	try
        	{
                manager.getTile( row, col, band ).setDouble( row, col, band, value );
        	}
        	catch( Exception e1 ){ }
        }
    }

    public void setDouble( int i, double value )
    {
    	int[] rcb = getRCB( i );
    	setDouble( rcb[0], rcb[1], rcb[2], value );
    }
    
    
    public void set( int row, int col, int band, double value )
    {
        try
        {
            manager.currentTile.setDouble( row, col, band, value );
        }
        catch( Exception e )
        {
        	try
        	{
                manager.getTile( row, col, band ).setDouble( row, col, band, value );
        	}
        	catch( Exception e1 ){ }
        }
    }

    public void set( int i, double value )
    {
    	int[] rcb = getRCB( i );
    	set( rcb[0], rcb[1], rcb[2], value );
    }
    
    // ^^^^^^^  Double  ^^^^^^^

    // ------------------------------------------------------------------------
    // EXTERNALIZATION
    // ------------------------------------------------------------------------

    /**
     * Read the ImageObject from the InputStream. This will read and write the
     * data that makes up the imageobject to the objectstream. If anything in
     * the dataformat has changed, the version will need to be upped and a new
     * case statement should be added. This will enable backwards compatibility
     * of the objects written to disk.
     * 
     * @param in
     *            The stream from which to read the object.
     * @throws IOException
     *             If the data could not be read.
     * @throws ClassNotFoundException
     *             If the class for an object being restored cannot be found.
     * 
     */
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        int version = (int) in.readLong();

        switch (version) {
        case 1:
            // read the rows, cols, bands
            numrows = in.readInt();
            numcols = in.readInt();
            numbands = in.readInt();

            // read the data type
            type = in.readInt();

            // read the headeronly
            headerOnly = in.readBoolean();

            // read the invalid data value
            invaliddata = in.readDouble();

            // set the size, this will create an array if needed
            setSize(numcols, numrows, numbands);
            
            // if not header only read the data
            if (!headerOnly) {
                // how can this be optimized?
                switch (type) {
                case TYPE_BYTE:
                    for (int i = 0; i < size; i++) {
                        setByte(i, in.readByte());
                    }
                    break;
                case TYPE_SHORT:
                case TYPE_USHORT:
                    for (int i = 0; i < size; i++) {
                        setShort(i, in.readShort());
                    }
                    break;
                case TYPE_INT:
                    for (int i = 0; i < size; i++) {
                        setInt(i, in.readInt());
                    }
                    break;
                case TYPE_LONG:
                    for (int i = 0; i < size; i++) {
                        setLong(i, in.readLong());
                    }
                    break;
                case TYPE_FLOAT:
                    for (int i = 0; i < size; i++) {
                        setFloat(i, in.readFloat());
                    }
                    break;
                case TYPE_DOUBLE:
                default:
                    for (int i = 0; i < size; i++) {
                        setDouble(i, in.readDouble());
                    }
                    break;
                }
            }

            // read the properties
            int count = in.readInt();
            for (int i = 0; i < count; i++) {
                String key = in.readUTF();
                try {
                    Object val = in.readObject();
                    setProperty(key, val);
                } catch (ClassNotFoundException exc) {
                    logger.warn("Could not read object for key [" + key + "].", exc);
                }
            }
            break;
        default:
            throw (new IOException("Could not read object of version "
                    + version));
        }
    }

    /**
     * Write the ImageObject to the OutputStream. This will write the data that
     * makes up the imageobject to the objectstream. If anything in the
     * dataformat has changed, the version will need to be upped. This version
     * number is used in readExternal forbackwards compatibility of the objects
     * written to disk.
     * 
     * @param in
     *            The stream from which to read the object.
     * @throws IOException
     *             If the data could not be read.
     * @throws ClassNotFoundException
     *             If the class for an object being restored cannot be found.
     * 
     */
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeLong(serialVersionUID);

        // standard write out rows, cols, bands
        out.writeInt(numrows);
        out.writeInt(numcols);
        out.writeInt(numbands);

        // write the datatype
        out.writeInt(type);

        // write the headeronly flag
        out.writeBoolean(headerOnly);

        // write the invalid data value
        out.writeDouble(invaliddata);

        // write the data
        if (!headerOnly) {
            
            // how can this be optimized?
            switch (type) {
            case TYPE_BYTE:
                for (int i = 0; i < size; i++) {
                    out.writeByte(getByte(i));
                }
                break;
            case TYPE_SHORT:
            case TYPE_USHORT:
                for (int i = 0; i < size; i++) {
                    out.writeShort(getShort(i));
                }
                break;
            case TYPE_INT:
                for (int i = 0; i < size; i++) {
                    out.writeInt(getInt(i));
                }
                break;
            case TYPE_LONG:
                for (int i = 0; i < size; i++) {
                    out.writeLong(getLong(i));
                }
                break;
            case TYPE_FLOAT:
                for (int i = 0; i < size; i++) {
                    out.writeFloat(getFloat(i));
                }
                break;
            case TYPE_DOUBLE:
            default:
                for (int i = 0; i < size; i++) {
                    out.writeDouble(getDouble(i));
                }
                break;
            }
        }

        // write the properties
        if (properties == null) {
            out.writeInt(0);
        } else {
            ArrayList<String> keys = new ArrayList<String>();
            for (String key : properties.keySet()) {
                if (!key.startsWith("_")) {
                    keys.add(key);
                }
            }
            out.writeInt(keys.size());
            for (String key : keys) {
                out.writeUTF(key);
                out.writeObject(properties.get(key));
            }
        }
    }

}
