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
package edu.illinois.ncsa.isda.imagetools.ext.segment;

import javax.swing.JFrame;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageException;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.imagetools.ext.misc.PlotComponent;
import edu.illinois.ncsa.isda.imagetools.ext.statistics.Histogram;

public class HSVThreshold {
	private ImageObject _hsvIm, _RGBIm, _RGBresult, _HSVresult;
	private long numEffPix;
	static private Log logger = LogFactory.getLog(HSVThreshold.class);
	
	public HSVThreshold() {
	}
		
	public void setHSVImage(ImageObject im) throws ImageException {
		_hsvIm = im;
		ColorModels col = new ColorModels();
    	col.convertHSV2RGB(im);
    	_RGBIm = col.getConvertedIm();
    	
    	try {
			_RGBresult = (ImageObject)_RGBIm.clone();
			_HSVresult = (ImageObject)_hsvIm.clone();
			
			_RGBresult.setMaxInCoreSize(Integer.MAX_VALUE);
			_HSVresult.setMaxInCoreSize(Integer.MAX_VALUE);
			
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}

	}
	
	public void setRGBImage(ImageObject im) throws ImageException {
		_RGBIm = im;
		ColorModels col = new ColorModels();
    	col.convertRGB2HSV(im);
    	_hsvIm = col.getConvertedIm();
    	
//    	System.out.println(_hsvIm.getMin(0));
//    	System.out.println(_hsvIm.getMax(0));
//    	System.out.println(_hsvIm.getMin(1));
//    	System.out.println(_hsvIm.getMax(1));
//    	System.out.println(_hsvIm.getMin(2));
//    	System.out.println(_hsvIm.getMax(2));
    	
    	try {
			_RGBresult = (ImageObject)_RGBIm.clone();
			_HSVresult = (ImageObject)_hsvIm.clone();
			_RGBresult.setMaxInCoreSize(Integer.MAX_VALUE);
			_HSVresult.setMaxInCoreSize(Integer.MAX_VALUE);
			
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}

    	
    	
	}
	
		
	public Histogram getHSVHistogram(int band) {
		if(_HSVresult == null) {
			System.err.println("Load an image first");
		}
		
		Histogram hist = new Histogram();
		hist.SetMinDataVal(_HSVresult.getMin(band));
		hist.SetMaxDataVal(_HSVresult.getMax(band));
		hist.SetNumBins(1000);
	
		
		//		switch(band) {
//		case 0: 	
//			hist.SetHistParam256Bins(_hsvIm, band);
//			break;
//		case 1:
//			hist.SetNumBins(100);
//			break;
//		case 2:
//			hist.SetNumBins(100);
//			break;
//		}
	
		try {
			hist.Hist(_HSVresult, band);
		} catch (ImageException e) {
			e.printStackTrace();
		}

		
		return hist;
		
	}
	
	public ImageObject getRGBSource() {
		return _RGBIm;
	}
	
	public ImageObject getHSVSource() {
		return _hsvIm;
	}
	
	public ImageObject getRGBResult() {
		return _RGBresult;
	}
	
	public ImageObject getHSVResult() {
		return _HSVresult;
	}
	
	public long threshold(float Hup, float Hlow, float Sup, float Slow, float Vup, float Vlow) {
		numEffPix = 0;
		float h,s,v;
		for(int j=0; j<_hsvIm.getNumRows(); j++) {	
			for(int i=0; i<_hsvIm.getNumCols(); i++) {
				h = _hsvIm.getFloat(j, i, 0);
				s = _hsvIm.getFloat(j, i, 1);
				v = _hsvIm.getFloat(j, i, 2);
				
				if(Hup <= Hlow) { // range rounds over 360 degree
					if((h <= Hup || h >= Hlow) &&
							s <= Sup && s >= Slow &&
							v <= Vup && v >= Vlow) {
						
						_HSVresult.set(j,i,0,h);
						_HSVresult.set(j,i,1,s);
						_HSVresult.set(j,i,2,v);
						
						_RGBresult.set(j,i,0,_RGBIm.getByte(j,i,0));
						_RGBresult.set(j,i,1,_RGBIm.getByte(j,i,1));
						_RGBresult.set(j,i,2,_RGBIm.getByte(j,i,2));
						numEffPix++;
					}
					else {
						_HSVresult.set(j,i,0,0);
						_HSVresult.set(j,i,1,0);
						_HSVresult.set(j,i,2,0);
						
						_RGBresult.set(j,i,0,0);
						_RGBresult.set(j,i,1,0);
						_RGBresult.set(j,i,2,0);
					}
				}
				else {
					if(h <= Hup && h >= Hlow &&
							s <= Sup && s >= Slow &&
							v <= Vup && v >= Vlow) {
						
						_HSVresult.set(j,i,0,h);
						_HSVresult.set(j,i,1,s);
						_HSVresult.set(j,i,2,v);
						
						_RGBresult.set(j,i,0,_RGBIm.getByte(j,i,0));
						_RGBresult.set(j,i,1,_RGBIm.getByte(j,i,1));
						_RGBresult.set(j,i,2,_RGBIm.getByte(j,i,2));
						numEffPix++;
					}
					else {
						_HSVresult.set(j,i,0,0);
						_HSVresult.set(j,i,1,0);
						_HSVresult.set(j,i,2,0);
						
						_RGBresult.set(j,i,0,0);
						_RGBresult.set(j,i,1,0);
						_RGBresult.set(j,i,2,0);
					}
				}
			}
		}
		return numEffPix;
	}
	

}
