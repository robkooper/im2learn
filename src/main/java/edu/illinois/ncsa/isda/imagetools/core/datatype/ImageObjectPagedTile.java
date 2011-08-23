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

import java.io.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Paging image tiles (small image objects) in and out to conserve memory.
 * 
 * @author Yakov Keselman
 */
/*
 * Design: for each image, we will always keep in memory its header (essential info).
 * The image (implemented by an in-core ImageObject) will be paged in and out as needed
 * in its entirety. Note that only contiguous (rows, columns, bands) are supported.
 * 
 * Note: this class does not implement ImageObject functionality, because
 * it is meant to be used only within ImageObjectOutOfCore.
 * 
 * This class uses the full-band implementation. So, it should be properly named
 * "ImageObjectPagedTileFullBand". If both full bands and single bands should be supported,
 * band offsets must be introduced and used similar to how row and column offsets are used.
 * Important note: in order to support single-band (or other non-full-band) images, code in
 * ImageObjectX should be changed to check the band similar to how row and column check is done.
 * 
 * To conserve disk space, tiles are created on-demand (only when write requests are issued).
 * Otherwise, an invalid value is returned upon reading data that was not written.
 * 
 */
public class ImageObjectPagedTile implements Serializable
{

	/**
	 * Fake ID. Need to insert the real one.
	 */
	private static final long serialVersionUID = 5191066761457343784L;

	
	/**
	 * The logger.
	 */
	static private Log logger = LogFactory.getLog( ImageObjectPagedTile.class );
	

	/**
	 * The actual image object (paged in and out as necessary).
	 */
	protected ImageObjectInCore image;

	
	/**
	 * The starting row of this tile on the larger image.
	 */
	protected final int rowOffset;
	
	/**
	 * The starting column of this tile on the larger image.
	 */
	protected final int colOffset;

	
	/**
	 * The file that will keep the data.
	 */
	protected File fp;
	
	
	/**
	 * Is true if the tile has valid data. Is set true when first written into.
	 */
	protected boolean hasValidData = false;
	
	
	/**
	 * Is true if the image has been loaded from disk.
	 */
	protected boolean imageLoaded = false;
	
	
	/**
     * A constructor from target image parameters.
	 * @param rows, number of in the target sub-image.
	 * @param cols, number of in the target sub-image.
	 * @param bands, number of in the target sub-image.
	 * @param type of the target sub-image.
     * @param rowOffset the starting row in the large image of which this is a sub-image.
     * @param colOffset the starting col in the large image of which this is a sub-image.
	 */
    public ImageObjectPagedTile( int rows, int cols, int bands, int type, int rowOffset, int colOffset )
    {
        // fill in final variables.
        this.rowOffset = rowOffset;
        this.colOffset = colOffset;
        
        // initialize the reference to the file that will contain the data.
        File fpTemp;
        try
        {
            fpTemp = File.createTempFile( "_" + rowOffset + "_" + colOffset + "_", ".img2" );
        }
        catch( IOException e1 )
        {
        	fpTemp = null;
        }
        fp = fpTemp;
        fp.deleteOnExit();
        
        // create an image without data; data will be created on-demand.
        try
        {
            image = ImageObjectInCore.createImage( rows, cols, bands, type, false );
        }
        catch( ImageException e )
        {
            return;
        }
    }
    
    public void destroy() {
    	if (!fp.delete()) {
    		System.err.println("Could not delete " + fp);
    	}
    	fp = null;
    	image = null;
    }
    
    // Accessors. Used to obtain basic info about the tile.
    
    /**
     * @return the size of the image (rows * columns * bands).
     */
    public int getSize()
    {
    	return image.getSize();
    }
    
    /**
     * @return number of rows in the tile.
     */
    public int getNumRows()
    {
    	return image.getNumRows();
    }
    
    /**
     * @return row offset of the tile.
     */
    public int getRowOffset()
    {
    	return rowOffset;
    }
    
    /**
     * @return number of cols in the tile.
     */
    public int getNumCols()
    {
    	return image.getNumCols();
    }
    
    /**
     * @return col offset of the tile.
     */
    public int getColOffset()
    {
    	return colOffset;
    }

    
	/**
	 * Can be called from the outside to force a specific paging strategy.
	 * If the data is already in memory, does nothing.
	 */
    public void pageIn()
    {
    	if( image.isHeaderOnly() )
    		loadImage();
    }

    
	/**
	 * Paging out the image onto the disk.
	 * Can be called from the outside to force a specific paging strategy.
     * This all of course depends on JVM's garbage collection mechanism.
	 */
    /*
     * With the current implementation, the image will be written out each time,
     * whether it was modified or not. In order to reduce the number of writes,
     * we can introduce a boolean variable "wasModified" to indicate whether the
     * image was in fact modified. The variable will be set to "false" in the very
     * beginning and will be set to "true" each time an image-changing operation
     * is invoked. Such operations are "setX" and "setProperty". The first line in
     * this method in that case will read "if( wasModified ) saveImage()". It is
     * deemed that to have this extra variable will slow things down rather than
     * speed them up (because the expected number of writes is relatively small).
     */
    public void pageOut()
    {
		saveImage();
		image.makeHeaderOnly();
    }

    
    /**
     * Can be used to check if the tile was written into (has some useful data).
     * @return
     */
    public boolean hasValidData()
    {
    	return hasValidData;
    }
    
    protected ImageObjectInCore getImage() {
        return image;
    }
    
    /*
     * Below are methods that make this object to behave like an ImageObject (by delegation).
    
     * As this is not an extension of ImageObject, we will only need to implement
     * those read and write methods that we'll actually be using in other classes.
     * They will not use an underlying data model, but only (row, column, band) access.
    
     * Common strategy for data access: try using the image. If an exception is raised
     * because there is no data, read the image and retry. If an exception is still raised,
     * assume that we're using the lazy data creation model (create only when written into).
     * Therefore, the fix is to create storage for data and retry again.
    
     * An exception can still be raised because indexes may be of range.
     * For this reason, it is important to do bound checking in ImageObject.
    
     * Methods below rely on the fact that ImageObject subclasses check row and column indices.
     */

    /** 
     * Setting the value to a default one. 
     */
    public void setData( double defaultval )
    {
    	try
    	{
    		image.setData( defaultval );
    	}
        catch( NullPointerException e )
        {
        	try
        	{
            	loadImage().setData( defaultval );
        	}
        	catch( NullPointerException e1 )
        	{
        		image.createArray();
        		image.setData( defaultval );
        		hasValidData = true;
        	}
        }
    }
    
    
    /**
     * The maximal value in the image.
     */
    public double getMax()
    {
    	try
    	{
    		return image.getMax();
    	}
        catch( NullPointerException e )
        {
        	try
        	{
            	return loadImage().getMax();
        	}
        	catch( NullPointerException e1 )
        	{
        		return image.getInvalidData();
        	}
        }
    }
    
    /**
     * The maximal value in the band.
     */
    public double getMax( int band )
    {
    	try
    	{
    		return image.getMax( band );
    	}
        catch( NullPointerException e )
        {
        	try
        	{
            	return loadImage().getMax( band );
        	}
        	catch( NullPointerException e1 )
        	{
        		return image.getInvalidData();
        	}
        }
    }
    
    
    /**
     * The minimal value in the image.
     */
    public double getMin()
    {
    	try
    	{
    		return image.getMin();
    	}
        catch( NullPointerException e )
        {
        	try
        	{
            	return loadImage().getMin();
        	}
        	catch( NullPointerException e1 )
        	{
        		return image.getInvalidData();
        	}
        }
    }
    
    
    /**
     * The minimal value in the band.
     */
    public double getMin( int band )
    {
    	try
    	{
    		return image.getMin( band );
    	}
        catch( NullPointerException e )
        {
        	try
        	{
            	return loadImage().getMin( band );
        	}
        	catch( NullPointerException e1 )
        	{
        		return image.getInvalidData();
        	}
        }
    }
    
    
    /**
     * The minimal value in the type. No data access is needed.
     */
    public double getTypeMin( )
    {
    	return image.getTypeMin();
    }
    
    /**
     * The maximal value in the type. No data access is needed.
     */
    public double getTypeMax( )
    {
    	return image.getTypeMax();
    }
    
    
    /**
     * True of all functions below:
     * @param row index of the large image (not the small sub-image)!
     * @param col index of the large image (not the small sub-image)!
     * @param band index of the large image (not the small sub-image)!
     */
    
    public byte getByte( int row, int col, int band )
    {
        try
        { 
            return image.getByte( row-rowOffset, col-colOffset, band );
        }
        catch( NullPointerException e )
        {
        	try
        	{
                return loadImage().getByte( row-rowOffset, col-colOffset, band );        		
        	}
        	catch( NullPointerException e1 )
        	{
        		return (byte)image.getInvalidData();
        	}
        }
    }

    public void setByte( int row, int col, int band, byte v )
    {
        try
        {
            image.setByte( row-rowOffset, col-colOffset, band, v );
        }
        catch( NullPointerException e )
        {
        	try
        	{
                loadImage().setByte( row-rowOffset, col-colOffset, band, v );
        	}
        	catch( NullPointerException e1 )
        	{
        		image.createArray();
        		image.setByte( row-rowOffset, col-colOffset, band, v );
        		hasValidData = true;
        	}
        }
    }


    public short getShort( int row, int col, int band )
    {
        try
        { 
            return image.getShort( row-rowOffset, col-colOffset, band );
        }
        catch( NullPointerException e )
        {
        	try
        	{
                return loadImage().getShort( row-rowOffset, col-colOffset, band );        		
        	}
        	catch( NullPointerException e1 )
        	{
        		return (short)image.getInvalidData();
        	}
        }
    }

    public void setShort( int row, int col, int band, short v )
    {
        try
        {
            image.setShort( row-rowOffset, col-colOffset, band, v );
        }
        catch( NullPointerException e )
        {
        	try
        	{
                loadImage().setShort( row-rowOffset, col-colOffset, band, v );
        	}
        	catch( NullPointerException e1 )
        	{
        		image.createArray();
        		image.setShort( row-rowOffset, col-colOffset, band, v );
        		hasValidData = true;        		
        	}
        }
    }

    
    public int getInt( int row, int col, int band )
    {
        try
        { 
            return image.getInt( row-rowOffset, col-colOffset, band );
        }
        catch( NullPointerException e )
        {
        	try
        	{
                return loadImage().getInt( row-rowOffset, col-colOffset, band );        		
        	}
        	catch( NullPointerException e1 )
        	{
        		return (int)image.getInvalidData();
        	}
        }
    }

    public void setInt( int row, int col, int band, int v )
    {
        try
        {
            image.setInt( row-rowOffset, col-colOffset, band, v );
        }
        catch( NullPointerException e )
        {
        	try
        	{
                loadImage().setInt( row-rowOffset, col-colOffset, band, v );
        	}
        	catch( NullPointerException e1 )
        	{
        		image.createArray();
        		image.setInt( row-rowOffset, col-colOffset, band, v );
        		hasValidData = true;
        	}
        }
    }


    public long getLong( int row, int col, int band )
    {
        try
        { 
            return image.getLong( row-rowOffset, col-colOffset, band );
        }
        catch( NullPointerException e )
        {
        	try
        	{
                return loadImage().getLong( row-rowOffset, col-colOffset, band );        		
        	}
        	catch( NullPointerException e1 )
        	{
        		return (long)image.getInvalidData();
        	}
        }
    }

    public void setLong( int row, int col, int band, long v )
    {
        try
        {
            image.setLong( row-rowOffset, col-colOffset, band, v );
        }
        catch( NullPointerException e )
        {
        	try
        	{
                loadImage().setLong( row-rowOffset, col-colOffset, band, v );
        	}
        	catch( NullPointerException e1 )
        	{
        		image.createArray();
        		image.setLong( row-rowOffset, col-colOffset, band, v );
        		hasValidData = true;
        	}
        }
    }


    public float getFloat( int row, int col, int band )
    {
        try
        { 
            return image.getFloat( row-rowOffset, col-colOffset, band );
        }
        catch( NullPointerException e )
        {
        	try
        	{
                return loadImage().getFloat( row-rowOffset, col-colOffset, band );        		
        	}
        	catch( NullPointerException e1 )
        	{
        		return (float)image.getInvalidData();
        	}
        }
    }

    public void setFloat( int row, int col, int band, float v )
    {
        try
        {
            image.setFloat( row-rowOffset, col-colOffset, band, v );
        }
        catch( NullPointerException e )
        {
        	try
        	{
                loadImage().setFloat( row-rowOffset, col-colOffset, band, v );
        	}
        	catch( NullPointerException e1 )
        	{
        		image.createArray();
        		image.setFloat( row-rowOffset, col-colOffset, band, v );
        		hasValidData = true;
        	}
        }
    }


    public double getDouble( int row, int col, int band )
    {
        try
        { 
            return image.getDouble( row-rowOffset, col-colOffset, band );
        }
        catch( NullPointerException e )
        {
        	try
        	{
                return loadImage().getDouble( row-rowOffset, col-colOffset, band );        		
        	}
        	catch( NullPointerException e1 )
        	{
        		return (double)image.getInvalidData();
        	}
        }
    }

    public void setDouble( int row, int col, int band, double v )
    {
        try
        {
            image.setDouble( row-rowOffset, col-colOffset, band, v );
        }
        catch( NullPointerException e )
        {
        	try
        	{
                loadImage().setDouble( row-rowOffset, col-colOffset, band, v );
        	}
        	catch( NullPointerException e1 )
        	{
        		image.createArray();
        		image.setDouble( row-rowOffset, col-colOffset, band, v );
        		hasValidData = true;
        	}
        }
    }


    ///////////

    
	/**
     * Reading the image from the disk (via deserialization). 
     * @return the deserialized image object or the original if read was not successful.
     */
	protected ImageObjectInCore loadImage()
	{
		// if the image was loaded, simply return it.
		if( imageLoaded )
			return image;
		
		try
		{
			FileInputStream fileIn = new FileInputStream( fp );
			ObjectInputStream in = new ObjectInputStream( fileIn );
			image = (ImageObjectInCore) in.readObject();
    		//logger.debug( "Paged in: " + fp.getAbsolutePath() );
    		imageLoaded = true;
    		return image;
		}
		catch( Exception e )
		{
			logger.error( "Failed to load a paged image object tile.", e);
			imageLoaded = false;
			return null;
		}
	}
	
	
	/**
	 * Writing the image onto the disk (via serialization).
	 */
	protected void saveImage()
	{
		try
		{
            FileOutputStream fileOut = new FileOutputStream( fp );
			ObjectOutputStream out = new ObjectOutputStream( fileOut );            
			out.writeObject( image );
    		//logger.debug( "Paged out: " + fp.getAbsolutePath() );
    		imageLoaded = false;
		}
		catch( Exception e )
		{
			logger.error( "Failed to save a paged image object tile.", e);
		}
	}
	
}

// //////////////////////////////////////////

/*
 * More details about the design:
 * 
 * Strategy: an object of an ImageObjectInCore subclass (such as ImageObjectByte) has two
 * components: the header component (all fields in ImageObject) and the data component
 * (in the subclass). Since the header is small, it can be kept in memory at all times.
 * Thus, for newly created tiles of ImageObject type, their headers will be kept, and
 * their data (together with the header) will be paged in and out as necessary.
 * Headers will be kept up-to-date by adjusting them whenever the image is written into
 * (reads on an image do not require header updates). Since important statistics (such
 * as Min and Max) for the whole image can be computed out of header contents of the
 * tile images, computation of these statistics will not require paging in images.
 * Only actual data access (such as reading in a value or writing out a value) may
 * require paging in an object. In addition, raw data access (such as that using
 * "getData" or "setData") will not be allowed. Using these will result in error messages.
 */
