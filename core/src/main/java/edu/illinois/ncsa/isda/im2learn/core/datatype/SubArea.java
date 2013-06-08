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

import java.awt.*;

/*
 * SubArea.java
 *
 */

/**
 * This defines a subarea of an image. Besides row, col, width and height, it
 * also provides for selecting a band and number of bands. Viewing the
 * imageobject as a 3D cube, where x is col, y is height and z is band, the
 * subarea allows to select any subsection inside the cube. If bands is 0, it is
 * assumed all bands are selected.
 *
 * @author Rob Kooper
 * @author Peter Bajcsy
 * @version 3.0
 */
public class SubArea extends Rectangle {
	private static final long serialVersionUID = -3012104071998260103L;

	private int band = 0;
    private int numbands = 0;

    //constructors
    public SubArea() {
        this(0, 0, 0, 0, 0, 0);
    }

    public SubArea(Rectangle rect) {
        this(rect.x, rect.y, 0, rect.width, rect.height, 0);
    }

    public SubArea(int col, int row, int numcols, int numrows) {
        this(col, row, 0, numcols, numrows, 0);
    }

    public SubArea(int col, int row, int band, int numcols, int numrows, int numbands) {
        super(col, row, numcols, numrows);
        this.band = band;
        this.numbands = numbands;
    }

    public int getRow() {
        return y;
    }

    public void setRow(int row) {
        this.y = row;
    }

    public int getEndRow() {
        return y + height;
    }

    public void setEndRow(int row) {
        int oldy = y;
        y = (row > oldy) ? oldy : row;
        height = Math.abs(oldy - row);
    }

    public int getCol() {
        return x;
    }

    public void setCol(int col) {
        this.x = col;
    }

    public int getEndCol() {
        return x + width;
    }

    public void setEndCol(int col) {
        int oldx = x;
        x = (col > oldx) ? oldx : col;
        width = Math.abs(oldx - col);
    }

    public int getHigh() {
        return height;
    }

    public int getWide() {
        return width;
    }

    public int getFirstBand() {
        return band;
    }

    public void setBand(int band) {
        this.band = band;
    }

    public int getNumBands() {
        return numbands;
    }

    public void setNumBands(int numbands) {
        this.numbands = numbands;
    }

    public int getLastBand() {
        return band + numbands;
    }

    public void setLastBand(int lastband) throws ImageException {
        if (lastband < band) {
            throw new ImageException("lastband is less than firstband.");
        }
        this.numbands = lastband - band;
    }

    public void setSubArea(int row, int col, int high, int wide, boolean flag) {
        setRect(col, row, wide, high);
    }

    // verification
    public boolean checkSubArea(int minRow, int minCol, int maxRow, int maxCol) {
        if (y < minRow || x < minCol || y + height > maxRow || x + width > maxCol)
            return false;
        else
            return true;
    }
    
    // checks if a point is in the area.
    public boolean isInSubArea( int row, int col )
    {
    	return isRowInSubArea( row ) & isColInSubArea( col );
    }
    
    // checks if a row is in the area.
    public boolean isRowInSubArea( int row )
    {
    	return ( row >= y ) & ( row < y+height );
    }

    // checks if a column is in the area.
    public boolean isColInSubArea( int col )
    {
    	return ( col >= x ) & ( col < x+width );
    }

    public String toString() {
      String s = "{col: "+x+" row: "+y+" height: "+height+" width: "+width+" band: "+band+" num bands: "+numbands+"}";
      return s;
    }


    // ------------------------------------------------------------------------
    // Old Im2Learn functions, these are deprecated
    // ------------------------------------------------------------------------

    /**
     * @deprecated use checkSubArea
     */
    public boolean CheckSubArea(int minRow, int minCol, int maxRow, int maxCol) {
        return checkSubArea(minRow, minCol, maxRow, maxCol);
    }
}
