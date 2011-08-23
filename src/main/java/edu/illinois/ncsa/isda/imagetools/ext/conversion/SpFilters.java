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
package edu.illinois.ncsa.isda.imagetools.ext.conversion;
/*
 * SpFilters.java
 *
 *
 */

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageException;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.imagetools.core.datatype.SubArea;
import edu.illinois.ncsa.isda.imagetools.ext.hyperspectral.RankBandDialog;
import edu.illinois.ncsa.isda.imagetools.ext.statistics.ImageMoment;


/**
 * @author  Sang-Chul Lee and Peter Bajcsy
 * @version 1.0
 * 2D Image Filters (linear,and mean-weight filtering)
 */


public class SpFilters {

	public static final int LINEAR = 1, MEDIAN = 2, MEAN_WEIGHT = 3, ST_DEV = 4, LOW_PASS = 5, HIGH_PASS = 6; 

	protected int _width, _height, _kernelWidth, _kernelHeight;
	//protected int band = 1;

	protected double _MinVal, _MaxVal;
	private ImageObject _imResult;
	private ImageObject _sourceImage; 
	private ImageObject _kernelImage;

	double backgroundLimit=10;
//	public boolean returnMap = false;

	static private Log logger = LogFactory.getLog(SpFilters.class);

	//constructors
	public SpFilters(){
	} 

	public void setImage(ImageObject srcIm) {
		_sourceImage = srcIm;
		_width = _sourceImage.getNumCols();
		_height = _sourceImage.getNumRows();

	}
	public void setKernel(ImageObject kernelIm) {
		_kernelImage = kernelIm;
		_kernelWidth = _kernelImage.getNumCols();
		_kernelHeight = _kernelImage.getNumRows();
		if((_kernelWidth % 2) == 0 || (_kernelHeight % 2) == 0) {
			logger.error("SPImageFilter: Error. Filter size is awkward. Make it odd by odd");
		}
	}

	public void setKernel(int kernelHeight, int kernelWidth) {
		try{
		_kernelImage = ImageObject.createImage(kernelHeight, kernelWidth, 1, "INT");
		}catch(Exception e){
			logger.error("SPImageFilter: Error. could not create a kernel image");
			return;
		}
		for(int i=0;i<_kernelImage.getSize();i++){
			_kernelImage.set(i,1);
		}
		_kernelWidth = _kernelImage.getNumCols();
		_kernelHeight = _kernelImage.getNumRows();
		if((_kernelWidth % 2) == 0 || (_kernelHeight % 2) == 0) {
			logger.error("SPImageFilter: Error. Filter size is awkward. Make it odd by odd");
		}
	}	
	public void SetThresholdMWF(double v) {
		this.backgroundLimit = v;
	}

	public boolean filter(int type) { 
		if(_sourceImage == null) {
			logger.error("SetImage First");
			return false;
		}

		/* 	if(_sourceImage.getNumBands() > 1) {
  		logger.error("Pass a single-band image");
  		return false;
  	}
		 */
		if(_kernelWidth >= _width || _kernelHeight >= _height) {
			logger.error("SPImageFilter: Error. Filter size is larger than image size");
		}

		int initX = (int)((float)_kernelWidth/2);
		int initY = (int)((float)_kernelHeight/2);
		int filtAreaW = _width - 2*initX;
		int filtAreaH = _height - 2*initY;

		try {
			_imResult = ImageObject.createImage(_sourceImage.getNumRows(),
					_sourceImage.getNumCols(),
					_sourceImage.getNumBands(),
					ImageObject.TYPE_FLOAT);
		} catch (ImageException e1) {
			e1.printStackTrace();
			return false;
		}

		SubArea sub = new SubArea();
		int x,y;
		double filteredVal;

		try{ 
			switch(type) {
			case LINEAR: 
				for(int band =0; band< _sourceImage.getNumBands();band++){
					for(int j=0; j<filtAreaH; j++) {
						for(int i=0; i<filtAreaW; i++) {
							x = initX+i;
							y = initY+j;
							sub.setSubArea(j,i,_kernelHeight,_kernelWidth,true);
							filteredVal = _linearSpFilter(sub, band);	
							_imResult.set(y,x,band,filteredVal);
						}
					}
				}
				break;
			case MEDIAN: // not implemented for im2learn yet
				for(int band =0; band< _sourceImage.getNumBands();band++){
					for(int j=0; j<filtAreaH; j++) {
						for(int i=0; i<filtAreaW; i++) {
							x = initX+i;
							y = initY+j;
							sub.setSubArea(j,i,_kernelHeight,_kernelWidth,true);
							filteredVal = _medianSpFilter(sub,band);
							_imResult.set(y,x,band,filteredVal);
						}
					}
				}
			break;
			case MEAN_WEIGHT: // not implemented for im2learn yet
				break;
			case ST_DEV:
				for(int band =0; band< _sourceImage.getNumBands();band++){
					for(int j=0; j<filtAreaH; j++) {
						for(int i=0; i<filtAreaW; i++) {
							x = initX+i;
							y = initY+j;
							sub.setSubArea(j,i,_kernelHeight,_kernelWidth,true);
							filteredVal = _stdSpFilter(sub, band);	
//							if(j % 100 == 0 && i % 100 == 1)
//							System.out.print(filteredVal+" ");
							_imResult.set(y,x,0,filteredVal);
						}
					}
				}
				break;

			case LOW_PASS:
				for(int band =0; band< _sourceImage.getNumBands();band++){
					for(int j=0; j<filtAreaH; j++) {
						for(int i=0; i<filtAreaW; i++) {
							x = initX+i;
							y = initY+j;
							sub.setSubArea(j,i,_kernelHeight,_kernelWidth,true);
							filteredVal = _avgSpFilter(sub, band);	
//							if(j % 100 == 0 && i % 100 == 1)
//							System.out.print(filteredVal+" ");
							_imResult.set(y,x,band,filteredVal);
						}
					}
				}	  		
				break;
			case HIGH_PASS:
				for(int band =0; band< _sourceImage.getNumBands();band++){
					for(int j=0; j<filtAreaH; j++) {
						for(int i=0; i<filtAreaW; i++) {
							x = initX+i;
							y = initY+j;
							sub.setSubArea(j,i,_kernelHeight,_kernelWidth,true);
							filteredVal = _avgSpFilter(sub,band);	
//							if(j % 100 == 0 && i % 100 == 1)
//							System.out.print(filteredVal+" ");
							_imResult.set(y,x,band,(_sourceImage.getFloat(j,i,band) - filteredVal));
						}
					}
				}	  		
				break;
			}
		}
		catch(Exception e){
			e.printStackTrace();
			return false;
		}

		return true;
	}




	private float _linearSpFilter(SubArea sub, int band) {
		float retVal = 0;
		for(int j=0; j<sub.height; j++) {
			for(int i=0; i<sub.width; i++) {
				retVal += _sourceImage.getFloat(j+sub.y, i+sub.x, band) * _kernelImage.getFloat(j,i,0);
			}
		}
		return retVal;
	}

	private float _medianSpFilter(SubArea sub, int band) {

		try{
			float retVal = 0;
			ImageObject subImage = _sourceImage.crop(sub);
			ImageObject subBandSubImage = null;

			subBandSubImage = subImage.extractBand(band);
			//QuickSort(subBandSubImage,0,ar.length-1);
			retVal = subBandSubImage.getFloat(subBandSubImage.getSize()/2);


			return retVal;
		}catch(Exception exc){
			logger.error("extract band exception exc = "+exc);
		}
		return 0;
	}

	  public static void QuickSort(ImageObject imgobj, int lo0, int hi0)  {

		    int lo = lo0;
		    int hi = hi0;
		    double mid;

		    if ( hi0 > lo0)       {
		      mid = imgobj.getDouble(( lo0 + hi0 ) / 2 );
		      while( lo <= hi )          {
		        while( ( lo < hi0 ) && ( imgobj.getDouble(lo) < mid ) )
		          ++lo;
		        while( ( hi > lo0 ) && ( imgobj.getDouble(hi) > mid ) )
		          --hi;
		        if( lo <= hi )        {
		        	double d = imgobj.getDouble(lo);
		        	imgobj.setDouble(lo, imgobj.getDouble(hi));
		        	imgobj.setDouble(hi, d);
		          //swap(a, lo, hi);
		          ++lo;
		          --hi;
		        }
		      }
		      if( lo0 < hi )
		        QuickSort(imgobj, lo0, hi );
		      if( lo < hi0 )
		        QuickSort(imgobj, lo, hi0 );
		    }
		  }

	 public static void QuickSort(double a[], int[] index, int lo0, int hi0)  {

		   int lo = lo0;
		   int hi = hi0;
		   double mid;

		   if ( hi0 > lo0)       {
		     mid = a[ ( lo0 + hi0 ) / 2 ];
		     while( lo <= hi )          {
		       while( ( lo < hi0 ) && ( a[lo] < mid ) )
		         ++lo;
		       while( ( hi > lo0 ) && ( a[hi] > mid ) )
		         --hi;
		       if( lo <= hi )        {
		         swap(a, lo, hi);
		         swap(index, lo, hi);
		         ++lo;
		         --hi;
		       }
		     }
		     if( lo0 < hi )
		       QuickSort(a, index, lo0, hi );
		     if( lo < hi0 )
		       QuickSort(a, index, lo, hi0 );
		   }
		 }
	  private static void swap(double a[], int i, int j)
	  {
	    double T;
	    T = a[i];  a[i] = a[j]; a[j] = T;
	  }
	  private static void swap(int a[], int i, int j)
	  {
	    int T;
	    T = a[i];  a[i] = a[j]; a[j] = T;
	  }
	 
	private float _stdSpFilter(SubArea sub, int band) {
		float sum = 0;
		for(int j=0; j<sub.height; j++) {
			for(int i=0; i<sub.width; i++) {
				sum += _sourceImage.getFloat(j+sub.y, i+sub.x, band);
			}
		}

		float avg = sum / ((float)sub.height * (float)sub.width);

		sum = 0;

		for(int j=0; j<sub.height; j++) {
			for(int i=0; i<sub.width; i++) {
//				if(_sourceImage.getFloat(j+sub.y, i+sub.x, band) != 246)
//				System.out.print("fff");
				sum += Math.pow(_sourceImage.getFloat(j+sub.y, i+sub.x, band) - avg,2);
			}
		}

		float stdev = (float)Math.sqrt(1/((float)sub.height * (float)sub.width-1) * sum);

		return stdev;
	}

	private float _avgSpFilter(SubArea sub, int band) {
		float sum = 0;
		for(int j=0; j<sub.height; j++) {
			for(int i=0; i<sub.width; i++) {
				sum += _sourceImage.getFloat(j+sub.y, i+sub.x, band);
			}
		}

		return sum / ((float)sub.height * (float)sub.width);
	}


	public ImageObject getResult() {
		return _imResult;
	}
	public String toString(){
		String str = new String();
		if(_kernelImage != null){
			str += "Kernel: "+_kernelImage.toStringAll() +"\n";
		}			
		return str;
	}

}






