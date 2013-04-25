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
import java.awt.image.DataBufferInt;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class ImageObjectInCore extends ImageObject {
	static private Log logger = LogFactory.getLog(ImageObjectInCore.class);

    /**
     * Offsets of row-starting index in the underlying data array.
     */
    protected int[] rowOffset;    // one per row.
	
    /**
     * Offsets of column-starting index in the underlying data array.
     */
    protected int[] colOffset;    // one per column.
    	
    /**
     * Offsets of band-starting index in the underlying data array. Currently unused.
     */
    protected int[] bandOffset;    // one per band.


    /**
     * Create a new image. This will create an image with requested parameters
     * and call createArray if headeronly is false. This constructor is intended
     * to be called by subclasses.
     *
     * @param rows       of pixels in the image.
     * @param cols       of pixels in the image.
     * @param bands      or samples per pixel in the image.
     * @param headeronly is true if no array should be created to store the pixels.
     */
    protected ImageObjectInCore( int rows, int cols, int bands, boolean headeronly )
    {
    	super( rows, cols, bands, headeronly );
    }

    
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
    static public ImageObjectInCore createImage(
    		int rows, int cols, int bands, int type, boolean header ) throws ImageException {
    	
        switch (type) {
        case TYPE_BYTE:
            return new ImageObjectByte(rows, cols, bands, header);

        case TYPE_SHORT:
            return new ImageObjectShort(rows, cols, bands, header);

        case TYPE_USHORT:
            return new ImageObjectUShort(rows, cols, bands, header);

        case TYPE_INT:
            return new ImageObjectInt(rows, cols, bands, header);

        case TYPE_LONG:
            return new ImageObjectLong(rows, cols, bands, header);

        case TYPE_FLOAT:
            return new ImageObjectFloat(rows, cols, bands, header);

        case TYPE_DOUBLE:
            return new ImageObjectDouble(rows, cols, bands, header);

        default:
            throw(new ImageException("Can't create new image of this type."));
        }
    }

	
    /**
     * Calculate minimum and maximum values for image. This function will find
     * the minimum and maximum values per band and per image. Make sure to call
     * this function if you change any values directly in getData().
     */
    public void computeMinMax() {
        double v;
        int i, b;

        if ( isHeaderOnly() ) {
            for (b = 0; b <= numbands; b++) {
                minval[b] = Double.NaN;
                maxval[b] = Double.NaN;
            }
            return;
        }

        // set all values to absolute min/max
        for (b = 0; b < numbands+1; b++) {
            minval[b] = Double.POSITIVE_INFINITY;
            maxval[b] = Double.NEGATIVE_INFINITY;
        }

        // loop through the image collecting data
        if (Double.isNaN(invaliddata)) {
	        for (i = 0; i < size;) {
	            for (b = 0; b < numbands; b++, i++) {
	                v = getDouble(i);
	                if (v < minval[numbands]) minval[numbands] = v;
	                if (v < minval[b]) minval[b] = v;
	                if (v > maxval[numbands]) maxval[numbands] = v;
	                if (v > maxval[b]) maxval[b] = v;
	            }
	        }
        } else {
	        for (i = 0; i < size;) {
	            for (b = 0; b < numbands; b++, i++) {
	                v = getDouble(i);
	                if (v == invaliddata) continue;
	                if (v < minval[numbands]) minval[numbands] = v;
	                if (v < minval[b]) minval[b] = v;
	                if (v > maxval[numbands]) maxval[numbands] = v;
	                if (v > maxval[b]) maxval[b] = v;
	            }
	        }        	
        }
        
        // funny case where all data in a band was invalid
        for (b = 0; b < numbands+1; b++) {
        	if (minval[b] > maxval[b]) {
        		minval[b] = Double.NaN;
        		maxval[b] = Double.NaN;
        	}
        }
        
        minmaxinvalid = false;

        // logger.debug("Max : " + maxval[0] + "   Min : " + minval[0]);
    }

    
    /**
     * Quickly copies data from the other object to this one.
     * @param other object from which to copy data.
     */
    protected void copyAllDataFrom( ImageObjectInCore other )
    {
        System.arraycopy( other.getData(), 0, this.getData(), 0, other.size );    	
    }
    
    
    /**
     * Copies all bands from another ImageObject into this one at specified location (row and column).
     * @param other ImageObject to copy all bands from.
     * @param otherRow the row in another ImageObject.
     * @param otherCol the col in another ImageObject.
     * @param row the row in this ImageObject.
     * @param col the col in this ImageObject.
     */
    protected void copyAllBandsFrom( ImageObjectInCore other, int otherRow, int otherCol, int row, int col )
    {
    	int otherIndex = other.rowOffset[otherRow] + other.colOffset[otherCol];
    	int thisIndex = rowOffset[row] + colOffset[col];
    	System.arraycopy( other.getData(), otherIndex, this.getData(), thisIndex, other.numbands );
    }
    
    
    /**
     * Copies all bands from another ImageObject into this one at specified row (row-wise copy).
     * @param other ImageObject to copy all bands from.
     * @param otherRow the row in another ImageObject.
     * @param otherCol the col in another ImageObject.
     * @param numCols the number of columns to copy.
     * @param row in this ImageObject.
     * @param col in this ImageObject.
     */
    protected void copySubRowAllBandsFrom( ImageObjectInCore other, int otherRow, int otherCol, 
    		int numCols, int row, int col )
    {
    	int otherIndex = other.rowOffset[otherRow] + other.colOffset[otherCol];
    	int thisIndex = rowOffset[row] + colOffset[col];
    	System.arraycopy( other.getData(), otherIndex, this.getData(), thisIndex, numCols*other.numbands );
    }
    

    /**
     * Copies some bands from another ImageObject into this one at specified rows and columns.
     * @param other ImageObject to copy all bands from.
     * @param otherRow the row in another ImageObject.
     * @param otherCol the col in another ImageObject.
     * @param row in this ImageObject.
     * @param col in this ImageObject.
     * @param firstBand the starting band in the other ImageObject.
     * @param numBands the number of bands from the other ImageObject to copy.
     */
    protected void copySomeBandsFrom( ImageObjectInCore other, int otherRow, int otherCol,
    		int row, int col, int firstBand, int numBands )
    {
    	int otherIndex = other.rowOffset[otherRow] + other.colOffset[otherCol] + firstBand;
    	int thisIndex = rowOffset[row] + colOffset[col];
    	System.arraycopy( other.getData(), otherIndex, this.getData(), thisIndex, numBands );
    }
    
    /**
     * Since this is an in-core object, this method should always return false.
     */
    public boolean isOutOfCore()
    {
    	return false;
    }
    
    
    /**
     * @return a SubArea representation of the ImageObject.
     */
    public ArrayList< SubArea > getTileBoxes()
    {
    	ArrayList< SubArea > toReturn = new ArrayList< SubArea >();
    	toReturn.add( new SubArea( 0, 0, numcols, numrows ) );
    	return toReturn;
    }
    
    
    /** 
     * Fills in offsets to speed up (r, c, b) data access.
     */
    protected void computeOffsets()
    {
        // band offsets for fast computation of indices into underlying data array.
    	// Note: band offsets are currently unused.
        bandOffset = new int[ numbands ];
        for( int i=0; i<numbands; i++ )
            bandOffset[i] = i;
    	
        // column offsets for fast computation of indices into underlying data array.        
        colOffset = new int[ numcols ];
        for( int i=0; i<numcols; i++ )
            colOffset[i] = i*numbands;

        // row offsets for fast computation of indices into underlying data array.
        rowOffset = new int[ numrows ];
        for( int i=0; i<numrows; i++ )
            rowOffset[i] = i*numcols*numbands;
    }

    
    /**
     * Explicitly delete band/row/col offsets.
     */
    protected void deleteOffsets()
    {
    	bandOffset = null;
    	colOffset = null;
    	rowOffset = null;
    }
    
    
    /**
     * Assume that in-cores always have at least one valid data entry.
     */
    public boolean hasValidData( SubArea area )
    {
    	return true;
    }


    // TODO push and optimize this to each subclass. For instance int can make
    // fakergb a lot easier. Also double can skip fakergb code.
    /**
     * Convert this imageobject to a buffered image that can be rendered. This
     * routine will using the parameters passed in convert the imagedata to an
     * BufferedImage. This BufferedImage can then be used to render the image
     * using standard java.
     * <p/>
     * If fakergb is specified the code will try and render the image in color
     * even though the image might be a single band image (for example when
     * grayscale is set to true). In this case it will take the information from
     * the single band and split that value into a RGB value, for example if the
     * ImageObject is of type byte it will use the 3 most significant bits for
     * the red channel, the next 3 bits for green and the 2 least significant
     * bits for the blue channel.
     *
     * @param bi         buffered image to reuse if possible.
     * @param fakergb    takes the single value of a pixel and treats it as a
     *                   rgb triple. In the case of a byte it will use 3 bits
     *                   for red, 3 for green and 2 for blue.
     * @param scale      use specified scale, if not specified (null) use
     *                   calculated min/max
     * @param usetotals  instead of using the min/max per band use the min/max
     *                   of the image.
     * @param redband    which band in the image is the redband, if set to -1 no
     *                   redband is used.
     * @param greenband  which band in the image is the greenband, if set to -1
     *                   no greenband is used.
     * @param blueband   which band in the image is the blueband, if set to -1
     *                   no blueband is used.
     * @param grayscale  if set to true the image is rendered as a grayscale
     *                   image and grayband is used.
     * @param grayband   which band in the image is the blueband, if set to -1
     *                   no blueband is used.
     * @param gammatable if set each pixel is gamma corrected. The table will
     *                   contain 256 entries.
     * @param alpha      the transparency value to be used, 0 is completely
     *                   transparent and 255 is opague.
     * @param imagescale scale the resulting image 
     * @return a bufferedimage that contains the imageobject.
     */
    public BufferedImage toBufferedImage(BufferedImage bi, boolean fakergb,
                                         double[] scale, boolean usetotals,
                                         int redband, int greenband, int blueband,
                                         boolean grayscale, int grayband,
                                         int[] gammatable, double alpha,
                                         double imagescale) {
        int i, a, r, g, b;
        double k, l, m;
        double x, y;
        double col;
        
        // convert the alpha value to a value between 0 and 255.
        if (alpha <= 0) {
            a = 0x00000000;
        } else if (alpha >= 255) {
            a = 0xff000000;
        } else if (alpha < 1) {
            a = ((int) (255 * alpha) & 0xff) << 24;
        } else {
            a = ((int) (alpha) & 0xff) << 24;
        }
        
        // calculate the resulting size of the image
        int    w = (getData() == null) ? 1 : (int)Math.ceil(numcols * imagescale);
        int    h = (getData() == null) ? 1 : (int)Math.ceil(numrows * imagescale);
        double skip = 1.0 / imagescale;
        double skipbands = skip * numbands;
        int line = numcols * numbands;

        // make sure the image exists that will hold final image
        int buftype = (a == 0xff000000) ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        if ((bi == null) || (bi.getType() != buftype) || (bi.getWidth() != w) || (bi.getHeight() != h)) {
            bi = new BufferedImage(w, h, buftype);
        }

        // get the raster
        int[] raster = ((DataBufferInt) bi.getRaster().getDataBuffer()).getData();
        int len = raster.length;

        // in case of only header, return white image
        if (getData() == null) {
            Arrays.fill(raster, a | 0x00ffffff);
            return bi;
        }
        
        // special case 
        if ((type == ImageObject.TYPE_INT) && fakergb && (gammatable == null) && (imagescale == 1.0)) {
        	System.arraycopy(getData(), 0, raster, 0, len);
        	return bi;
        }

        // if no gamma table is provided use the standard gamma table.
        if (gammatable == null) {
            gammatable = nogamma;
        }

        if (fakergb && (numbands == 1) &&
            ((type == ImageObject.TYPE_INT) ||
             (type == ImageObject.TYPE_USHORT) ||
             (type == ImageObject.TYPE_SHORT))) {

            if ((type == ImageObject.TYPE_SHORT) || (type == ImageObject.TYPE_USHORT)) {
				for (i = 0, y = 0; y < numrows; y += skip) {
					k = y * line;
					for (x = 0; x < numcols; x += skip, i++) {
						col = getInt((int) k);
						r = gammatable[((((short) col) & 0xfc00) >> 8) & 0xff];
						g = gammatable[((((short) col) & 0x03e0) >> 2) & 0xff];
						b = gammatable[((((short) col) & 0x001f) << 3) & 0xff];
						raster[i] = a | (r << 16) | (g << 8) | b;
						k += skipbands;
					}
				}

            } else if (type == ImageObject.TYPE_INT){
                // in case of int, if no gamma, simply use the int value
                // as the RGB value, no need to convert.
                for (i = 0, y = 0; y < numrows; y += skip) {
                	k = y * line;
                	for(x = 0; x < numcols; x += skip, i++) {
	                    if (gammatable != null) {
	                        col = getInt((int)k);
	                        r = ((((int) col) & 0xff0000) >> 16) & 0xff;
	                        g = ((((int) col) & 0x00ff00) >> 8) & 0xff;
	                        b = (((int) col) & 0x0000ff) & 0xff;
	                        r = gammatable[r];
	                        g = gammatable[g];
	                        b = gammatable[b];
	                        raster[i] = a | (r << 16) | (g << 8) | b;
	                    } else {
	                        raster[i] = getInt((int)k);
	                    }
	                    k += skipbands;
                	}
                }
            }



        } else if (grayscale) {
            double min, max, div;

            k = (grayband < 0 || grayband >= numbands) ? 0 : grayband;
            if ((scale != null) && (scale.length == 2)) {
                min = scale[0];
                max = scale[1];
            } else {
                min = getMin(usetotals ? numbands : (int)k);
                max = getMax(usetotals ? numbands : (int)k);
            }
            // modification for test, should be deleted later
            // min = 0; max = 1000;
            // end of modification
            div = 255.0 / (max - min);
            for (i = 0, y = 0; y < numrows; y += skip) {
            	x = y * line;
                k = (grayband < 0 || grayband >= numbands) ? x : x + grayband;            		
            	for(x = 0; x < numcols; x += skip, i++) {
                    col = getDouble((int)k);
                    if (col <= min) {
                        col = min;
                    } else if (col >= max) {
                        col = max;
                    }
                    col = (col - min) * div;
                    g = gammatable[(int) col & 0xff];
                    raster[i] = a | (g << 16) | (g << 8) | g;
                    k += skipbands;
            	}
            }

        } else {
            double[] min = new double[3];
            double[] max = new double[3];
            double[] div = new double[3];

            k = (redband < 0 || redband >= numbands) ? -1 : redband;
            l = (greenband < 0 || greenband >= numbands) ? -1 : greenband;
            m = (blueband < 0 || blueband >= numbands) ? -1 : blueband;
            r = 0;
            g = 0;
            b = 0;

            if ((scale != null) && (scale.length == 2)) {
                min[0] = scale[0];
                max[0] = scale[1];
                min[1] = scale[0];
                max[1] = scale[1];
                min[2] = scale[0];
                max[2] = scale[1];
            } else {
                min[0] = getMin(usetotals || (k < 0) ? numbands : (int)k);
                max[0] = getMax(usetotals || (k < 0) ? numbands : (int)k);
                min[1] = getMin(usetotals || (l < 0) ? numbands : (int)l);
                max[1] = getMax(usetotals || (l < 0) ? numbands : (int)l);
                min[2] = getMin(usetotals || (m < 0) ? numbands : (int)m);
                max[2] = getMax(usetotals || (m < 0) ? numbands : (int)m);
            }
            div[0] = 255.0 / (max[0] - min[0]);
            div[1] = 255.0 / (max[1] - min[1]);
            div[2] = 255.0 / (max[2] - min[2]);

            for (i = 0, y = 0; y < numrows; y += skip) {
            	x = y * line;
                k = (k < 0) ? -1 : x + redband;
                l = (l < 0) ? -1 : x + greenband;
                m = (m < 0) ? -1 : x + blueband;
            	for(x = 0; x < numcols; x += skip, i++) {
                    if (k >= 0) {
                        col = getDouble((int)k);
                        if (col <= min[0]) {
                            col = min[0];
                        } else if (col >= max[0]) {
                            col = max[0];
                        }
                        col = (col - min[0]) * div[0];
                        r = gammatable[(int) col & 0xff];
                        k += skipbands;
                    }
                    if (l >= 0) {
                        col = getDouble((int)l);
                        if (col <= min[1]) {
                            col = min[1];
                        } else if (col >= max[1]) {
                            col = max[1];
                        }
                        col = (col - min[1]) * div[1];
                        g = gammatable[(int) col & 0xff];
                        l += skipbands;
                    }
                    if (m >= 0) {
                        col = getDouble((int)m);
                        if (col <= min[2]) {
                            col = min[2];
                        } else if (col >= max[2]) {
                            col = max[2];
                        }
                        col = (col - min[2]) * div[2];
                        b = gammatable[(int) col & 0xff];
                        m += skipbands;
                    }
                    raster[i] = a | (r << 16) | (g << 8) | b;            		
            	}
            }
        }

        return bi;
    }
    
}
