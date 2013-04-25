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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Manages a collection of ImageObjectPagedTile's. In particular, it pages them in and out.
 * It also serializes them, if the serialization of a larger object is needed.
 * 
 * This class should be thought of as an inner class of ImageObjectOutOfCore.
 * Hence, some of its variables that are declared public, should be thought of
 * as public only to ImageObjectOutOfCore.
 * 
 * NOTE: for simplicity, this tile manager relies on the assumption that the size of a tile
 * never exceeds the value set in advance. If that value changes, the overall amount of
 * memory used by all tiles can exceed the threshold set in ImageObjectOutOfCore.
 * 
 * @author Yakov Keselman
 *
 */
public class ImageObjectTileManager {
	static private Log logger = LogFactory.getLog(ImageObjectTileManager.class);

	/**
	 * The expected maximal of managers to simultaneously be in memory.
	 * This number can be kept track of, if needed.
	 */
	final static int expectedMaxNumberOfManagers = 16;
	
	
	/**
	 * The array of paged in tiles (one for all Tile Managers).
	 */
	protected final static ArrayList< ImageObjectPagedTile > pagedInTiles
		= new ArrayList< ImageObjectPagedTile >();
	
	
	/**
	 * The total amount of memory occupied by all tiles.
	 */
	protected static int memoryUsedByTiles = 0;
	
	
	/**
	 * The array of tiles.
	 */
	public ArrayList< ImageObjectPagedTile > allTiles;
	
	/**
	 * The current image tile (to speed up data access).
	 */
	public ImageObjectPagedTile currentTile;
	
		
	/**
	 * The number of rows in a tile.
	 */
	protected int tileNumRows;
		
	/**
	 * The number of columns in a tile.
	 */
	protected int tileNumCols;

	
    /**
     * Offsets of row-starting index in the underlying data array.
     */
    protected int[] rowOffset;    // one per row.
	
    /**
     * Offsets of column-starting index in the underlying data array.
     */
    protected int[] colOffset;    // one per column.
   
    // /** Offsets of band-starting index in the underlying data array. */
    // protected int[] bandOffset;    // one per band.
    // uncomment if bands should be checked on access.


	/**
	 * Create a new set of tiles, managed by the manager.
	 * 
	 * @param numrows number of rows in the original image.
	 * @param numcols number of columns in the original image.
	 * @param numbands number of bands in the original image.
	 * @param type the type of the ImageObject to use.
	 */
	public ImageObjectTileManager( int numrows, int numcols, int numbands, int type )
	{
		// create the array of all tiles.
    	allTiles = new ArrayList< ImageObjectPagedTile >();
    	
    	// figure out more or less optimal parameters for tile dimensions.
    	this.setOptimalTilingParameters( numrows, numcols, numbands );

		// compute offsets of tile rows/cols w.r.t. image rows/cols.
		int imageColOffset[] = this.getTileOffsets( numcols, tileNumCols );
		int numTilesInRow = imageColOffset.length - 1;
		int imageRowOffset[] = this.getTileOffsets( numrows, tileNumRows );
		int numTilesInColumn = imageRowOffset.length - 1;
		
		// These offsets only work with the adopted tile storage scheme.
		 
		// create and fill in column offsets that will redirect pixel access to tiles.
        colOffset = new int[numcols];
        for( int col = 0; col < numcols; col ++ )
        	colOffset[col] = col/tileNumCols;

		// create and fill in row offsets that will redirect pixel access to tiles.
        rowOffset = new int[numrows];
        for( int row = 0; row < numrows; row ++ )
        	rowOffset[row] = (row/tileNumRows)*numTilesInRow;
        
		// create the tiles and add them to an array of such.
		for( int tileRow = 0; tileRow < numTilesInColumn; tileRow ++ )
		{
			// number of rows in the tile == the difference between offsets.
			int rows = imageRowOffset[tileRow+1]-imageRowOffset[tileRow];
			for( int tileCol = 0; tileCol < numTilesInRow; tileCol ++ )
			{
    			// number of cols in the tile == the difference between offsets.
    			int cols = imageColOffset[tileCol+1]-imageColOffset[tileCol];
    			
    			// create a new tile with the specified parameters and add it to all.
    			currentTile = new ImageObjectPagedTile( rows, cols, numbands, type,
						imageRowOffset[tileRow], imageColOffset[tileCol] );
				allTiles.add( currentTile );
				
				// make sure the tile is placed in the array of paged in tiles.
				getTile( currentTile.getRowOffset(), currentTile.getColOffset(), 0 );
				// currentTile.pageOut();
			}
		}
	}

	
    /** 
     * Get the tile based on row, column, and band. Updates the current Tile.
     * Perform paging out if the buffer got full. Use a simple replacement strategy.
     * @param row of the pixel, large image coordinates.
     * @param col of the pixel, large image coordinates.
     * @param band of the pixel, large image coordinates (unused with full-banded tiles).
	 * @return the (paged in) tile that is responsible for the specific place in the image.
     */
    public synchronized ImageObjectPagedTile getTile( int row, int col, int band )
    {
    	// the size of the buffer, for future reference.
    	int size = pagedInTiles.size();
    	
        // get the tile (if any).
        currentTile = allTiles.get( rowOffset[row] + colOffset[col] );
        
        // if the tile is already paged in, move it closer if needed, and return it.
        int currIndex = pagedInTiles.lastIndexOf( currentTile );
        if( currIndex >= 0 )
        {
        	// logger.info( "Index among paged in tiles: " + currIndex );
        	// logger.info( "Size of paged in: " + size );
        	
        	if( currIndex > size*2/3 )	// heuristic: move to front if in the last third.
        	{
        		pagedInTiles.remove( currIndex );
        		pagedInTiles.add( 0, currentTile );
        	}
        }
        else
        {
            //logger.debug( "Memory used by tiles: " + memoryUsedByTiles );
            
            // otherwise, check if memory capacity will be exceeded if we page it in.
            while( currentTile.getSize() + memoryUsedByTiles >= ImageObjectOutOfCore.totalMemoryAvailable )
            {
                // page out the last few tiles.
            	size = pagedInTiles.size();
                ImageObjectPagedTile toPageOut = pagedInTiles.get( size - 1 );
                toPageOut.pageOut();
                memoryUsedByTiles -= toPageOut.getSize();
                pagedInTiles.remove( size-1 );
                // logger.info( "Memory used by tiles: " + memoryUsedByTiles );
            }
            
            // insert a new one in the first place.
            currentTile.pageIn();
            memoryUsedByTiles += currentTile.getSize();
            pagedInTiles.add( 0, currentTile );        	
        }
        
        return currentTile;
    }
    
    public void destroy() {
    	currentTile = null;
    	for( ImageObjectPagedTile tile: allTiles ) {
    		if (pagedInTiles.lastIndexOf( tile ) != -1) {
    			pagedInTiles.remove(tile);
    			memoryUsedByTiles -= tile.getSize();
    		}
    		tile.destroy();
    	}
    	allTiles = null;
    }

    
    /**
     * Sets the number of rows and columns in a tile. These parameters are determined
     * by the current predominantly sequential way of acccessing tile pixels.
     * 
     * Objectives for number of rows/columns in a tile:
     * - Row-wise (fix a row, go over the columns) iteration is fast.
     * - Column-wise (fix a column, go over the rows) iteration is reasonalby fast.
     * - Cropping a sub-area is reasonably fast.
     * 
     * Ideas:
     * - Think of this memory manager as one of several). Thus, this manager
     * can work by itself in its share of the total amount of memory available.
     * - consider the largest number of full rows that such amount of memory can hold.
     * - think of it as a rectangle that needs to be further partitioned vertically.
     * - potential strategies for vertical partitioning:
     * -- make the number of columns such that when they are stacked vertically,
     *    they result in a complete column.
     * -- make the number of columns such that the ratio of rows and columns in a tile
     *    is is proportional to that in the larger image.
     * -- make the number of columns such that it is possible to put together a square
     *    by stacking the tiles vertically.
     *    
     * Algorithm:
     * - figure out how many complete rows of this object 1/16 of total memory can hold.
     * - if the number of complete rows is more than one, make that number the number of
     * rows in a tile; choose the number of columns following one of the strategies above;
     * - if the number of complete rows is less than one, keep in memory 10 tiles (for
     * filtering over neighborhoods of radius 2), and make the tile of appropriate size.
     * 
     * IMPORTANT NOTE: since the buffer of paged in tiles is common to all objects
     * of this class, the number of paged in tiles for a given object changes,
     * depending on the access patterns.
     * 
     */
    protected void setOptimalTilingParameters( int numrows, int numcols, int numbands )
    {
    	// make sure the size of the tile is reasonable.
    	int maxSize = ImageObjectOutOfCore.totalMemoryAvailable / expectedMaxNumberOfManagers;
    	// if( maxSize > ImageObjectOutOfCore.MAX_IN_CORE_SIZE )
    	//	maxSize = ImageObjectOutOfCore.MAX_IN_CORE_SIZE;
    	
    	// make sure the number of rows and columns in the tile is reasonable.
    	int maxRowCols = maxSize / numbands;
    	if( maxRowCols > numrows * numcols )
    		maxRowCols = numrows * numcols;
    	
    	// figure out the number of full rows that we can cover.
    	final double numFullRows = (double) maxRowCols / numcols;
    	
    	// set the minimum number of one-row tiles (for potential filtering).
    	final int minOneRowTiles = 10;
    	
    	// for tiles covering a number of full rows, use one strategy.
    	if( numFullRows > 1 )	// need to think whether to use 1 or a larger number.
    	{
    		// set the number of rows.
    		tileNumRows = (int)numFullRows;
    		
    		// set the number of columns.
    		// use the square strategy: can form a square from all tiles in memory.
    		// if the number of tiles across is p, then to make a square, we need
    		// numcols/p = tileNumRows*p. Thus, p = sqrt( numcols/tileNumRows ).
    		int p = (int)Math.sqrt( (double)numcols / tileNumRows );
    		if( p == 0 )
    			p = 1;
    		tileNumCols = numcols / p ;
    		//tileNumCols = numcols;
    	}
    	else
    	{
    		// for tiles covering only one row, use a different strategy.
    		tileNumRows = 1;
    		tileNumCols = maxRowCols / minOneRowTiles;
    	}
    	
    	logger.debug("Original image is cols=" + numcols + " rows=" + numrows +
    			" Tiled image is cols=" + tileNumCols + " rows=" + tileNumRows);
    }

    
    /** 
     * @return the array of tile offsets in that specific dimension.
     * @param imageDimSize the size of the image dimension.
     * @param tileDimSize the size of the tile dimension.
     */
    private int[] getTileOffsets( int imageDimSize, int tileDimSize )
    {
        // Figure out the size of a dimension, which is the rounded up ratio of
        // the image size in that dimension to the tile size in that dimension.
        int tiledDim = (int)Math.ceil( (double)imageDimSize / tileDimSize );
        
        // now form the array of offsets.
        int[] offsets = new int[ tiledDim + 1 ];
        offsets[0] = 0;
        for( int i = 1; i < tiledDim; i++ )
        	offsets[i] = offsets[i-1] + tileDimSize;
        
        // make the last element to be the image dimension size (used elsewhere).
        offsets[tiledDim] = imageDimSize;
        
        // return the offsets.
        return offsets;
    }
    
    
	/**
	 * Customized serialization: one-by-one, page in all the tiles and save them.
	 */
	private void writeObject( ObjectOutputStream out )
	throws IOException
	{
		// write out allTiles array.
		int numTiles = allTiles.size();
		out.writeInt( numTiles );
		for( int i=0; i<numTiles; i ++ )
		{
			currentTile = allTiles.get( i );
			currentTile.pageIn();
			out.writeObject( currentTile );
			currentTile.pageOut();
		}
		
		// write out row and column offsets.
		out.writeObject( rowOffset );
		out.writeObject( colOffset );
	}
	
	
	/**
	 * Customized de-serialization: one-by-one, read in all the tiles and page them out.
	 */
	private void readObject( ObjectInputStream in )
	throws IOException, ClassNotFoundException
	{
		// create the arrays.
    	allTiles = new ArrayList< ImageObjectPagedTile >();

    	// read in the number of tiles and the actual tiles.
		int numTiles = in.readInt();
		for( int i=0; i<numTiles; i ++ )
		{
			currentTile = (ImageObjectPagedTile)in.readObject();
			allTiles.add( currentTile );
			currentTile.pageOut();
		}
		
		// read in row and column offsets.
		rowOffset = ( int [] )in.readObject();
		colOffset = ( int [] )in.readObject();
	}
	
}
