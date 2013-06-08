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
/*
 * Created on Sep 8, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.illinois.ncsa.isda.im2learn.ext.segment;

import java.awt.Color;

import edu.illinois.ncsa.isda.im2learn.core.datatype.Point2DFloat;

/**
 * @author yjlee
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Contour {
	protected Point2DFloat	pts;
	protected int			maxPt;
	protected Color			color;
	
	public Contour(Point2DFloat pts, int maxPt, Color c) {
		this.pts = pts;
		this.maxPt = maxPt;
		this.color = c;
	}
	
	protected void initContour() {
//        int i,idx;
//        double row1,row2,col1,col2;
//        for(idx = 2, i = 1; i< maxValidPts; i++,idx+=2){
//           col1 = contour.ptsFloat[idx-1];
//           row1 = contour.ptsFloat[idx-2];
//           col2 = contour.ptsFloat[idx+1];
//           row2 = contour.ptsFloat[idx];
//
//           // subsampling happened
//           if(!_zoomOutRow && _samprow != 1 ){
//              row1 /= _samprow;
//              row2 /= _samprow;
//           }
//           if(!_zoomOutCol && _sampcol != 1 ){
//              col1 /= _sampcol;
//              col2 /= _sampcol;
//           }
//            // upsampling happened
//            if(_zoomOutRow && _upsamprow != 1 ){
//              row1 *= _upsamprow;
//              row2 *= _upsamprow;
//           }
//           if(_zoomOutCol && _upsampcol != 1 ){
//              col1 *= _upsampcol;
//              col2 *= _upsampcol;
//           }
//
//           // plot only contour that is inside of the displayed image.
//           if(col1 >=0 && col2>=0 && col1< _imObject.numcols && col2<_imObject.numcols
//             && row1>=0 && row2>=0 && row1<_imObject.numrows && row2<_imObject.numrows){
//             g.drawLine((int)col1,(int)row1,(int)col2,(int)row2);
//           }
	}
}
