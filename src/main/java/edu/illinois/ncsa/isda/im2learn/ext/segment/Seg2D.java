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
 * Created on Jul 29, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.illinois.ncsa.isda.im2learn.ext.segment;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageException;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.im2learn.core.datatype.LimitValues;
import edu.illinois.ncsa.isda.im2learn.core.io.iip.IIPLoader;
import edu.illinois.ncsa.isda.im2learn.ext.misc.ImEnhance;
import edu.illinois.ncsa.isda.im2learn.ext.segment.SegObject;
import edu.illinois.ncsa.isda.im2learn.ext.segment.Threshold;
import edu.illinois.ncsa.isda.im2learn.ext.statistics.ImStats;


/**
 * @author pf23
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Seg2D extends SegObject {
	    private boolean 			_debugSeg2D			= true;
	    private LimitValues 		_lim 				= new LimitValues();
	    private Threshold 			_myThresh 			= new Threshold();
	    private IIPLoader 			_myIIPImage 		= new IIPLoader();
	    private ImEnhance 			_myImEnhance 		= new ImEnhance();
	   // private ImStats _myImStats = new ImStats();

	    private double 				[] suma 			= null;
	    private int 				[] temp1 			= null;
	    private int 				[] labeling 		= null;
	    private int 				_sizeSuma 			= -1;
	    private int  				_sizeLabeling 		= -1;
	    
	    private ImageObject			_mergeStats			= null;
	    //private ImageObject			_pImLabels			= null;
	    private static Log logger = LogFactory.getLog(Seg2D.class);

	    //constructor
	    public void Seg2D(){
		_debugSeg2D = true;
	      }

	    // Getters
	    public SegObject getSegObject(){
	      return (SegObject)this;
	    }
	    public boolean getDebugFlag(){return _debugSeg2D;}


	    //Setters
	    public void setDebugFlag(boolean flag){
		_debugSeg2D = flag;
	    }
	     //////////////////////////////////////////////////////////////
	    // doers
	    /////////////////////////////////////////////////////////
	    // segmentation into homogeneous regions
	    ///////////////////////////////////////////////////
	    public boolean nonTexture_Segm(ImageObject imgOrig) {

	      //sanity check
	      if(imgOrig == null){
	        logger.debug("ERROR: missing image data");
	        return false;
	      }
	      if( !imgOrig.getTypeString().equalsIgnoreCase("BYTE") ){
	      	logger.debug("ERROR: other than BYTE data type is not supported");
	        return false;
	      }

	      float deltapom,delta;
	      double minTemp,ltemp,valD;
	      int i,j;
	      int iVal, iValN;
	      int peto,pom,pom1,lower,upper;
	      double lvalue,lvalueN;
	      int maxpetox,count;
	      int idx,index,index1,indexLabel,offset;
	      double dVal;
	      double sumx;
	      int index2;
	      int qu,k,end;
	      //  might be moved to init()
	      double [] imageoutx = null;
	      double [] imageouty = null;
	      //double [] suma = null;
	      //int [] labeling = null;
	      //int [] temp1 = null;

	      double [] backward = null;
	      int [] backcount = null;
	      int [] forward = null;

	      //temporal solution
	      int _SegSize, _SegWide, _SegHigh;
	      //allocation of space
	      _SegSize = imgOrig.getNumRows() * imgOrig.getNumCols();
	      _SegWide = imgOrig.getNumCols();
	      _SegHigh = imgOrig.getNumRows();
	      maxpetox = _SegSize +1;

	      imageoutx = new double[_SegSize];
	      imageouty = new double[_SegSize];

	      if(_SegWide>_SegHigh)
	       i = _SegWide;
	      else
	       i = _SegHigh;

	      backward = new double[i];
	      backcount = new int[i];
	      forward = new int[i];

	      ///////////////////////////////////////////////////////////
	      // prepare the _pImMean image = distance image
	      double meanDistance = -1;
	      meanDistance = _myThresh.findMeanDistance(imgOrig);
	      if(meanDistance == -1.0){
	      	logger.debug("ERROR: could not copy the original image or compute the mean distance");
	          return false;
	      }
	      _minDataVal  = _myThresh.getMinDistance();
	      _maxDataVal = _myThresh.getMaxDistance();

	      float minSigma = _minSigma;
	      float maxSigma = _maxSigma;
	      float deltaSigma = _deltaSigma;
	      if(_maxDataVal - _minDataVal < _deltaSigma){
	      	logger.debug("Warning: image has smaller max-min range="+_maxDataVal+"-"+_minDataVal+", than the deltaSigma="+_deltaSigma);
	         maxSigma = minSigma+(float)(_maxDataVal-_minDataVal);
	         logger.debug("Info: used maxSigma="+maxSigma);
	      }

	      // this is the image that is used as a holder of the original data converted to double
	      ImageObject imgProcessed = null;
	      imgProcessed = _myThresh.getDistanceImageObject();
	      if(imgProcessed == null){
	      	logger.debug("ERROR: distance image is null; could not copy the original image");
	          return false;
	      }
	      // we have to make copy otherwise we have to go through
	      // (a) type casting and (b) euclidean distance computation
	      try {
	      	_pImMean = (ImageObject) imgProcessed.clone();
	      } catch (CloneNotSupportedException e) {
	      	e.printStackTrace();
	      }
	      //test
	      //System.out.println("TEST: setup:");
	      //_pImMean.PrintImageObject();
	      //PrintSegObject();

	      ///////////////////////////////////////////////////////////
	      if(deltaSigma ==0.0F)
	         deltaSigma=1.0F;
	      //prepare to monitor mergers
	      int iter, maxIter,iterLayers,maxLayers;
	      maxIter = (int) ((maxSigma - minSigma)/deltaSigma +1.0);
	      if(maxIter > 100){
	      	logger.debug("INFO: maxIter ="+maxIter+" is too large and will be m odified to 100");
	         maxIter = 100;
	      }
	      
	      try {
	      _mergeStats = ImageObject.createImage(1,maxIter,3,"DOUBLE");//delta,merge,NFOUNDS
	      } catch (ImageException e) {
	      	e.printStackTrace();
	      }
	      for(iter = 0 ; iter < _mergeStats.getSize(); iter++){
	        _mergeStats.setDouble(iter, -1.0);
	      }

	      boolean signalExit = false;
	      iterLayers = 0;
	      maxLayers = 1;
	      if( _flagEvenSigmaLayers){
	        maxLayers = _N2FindEvenSigmaLayers;
	      }
	      _maxNFoundSLayers = -1;// needed for output pImLabel enhancement with multiple layers

	      for(iter = 0,delta=minSigma; !signalExit && delta<= maxSigma && iterLayers < maxLayers ;delta += deltaSigma,iter++) {
	        //calculate descriptors at each pixel
	        //deltapom = (unsigned long)  delta<<16;
	        deltapom = delta;
	        if(delta > 0){
	           // init imageoutx, imageouty
	           for(i=0;i<_SegSize;i++){
	                imageoutx[i] = _UNUSED;// this should be unused
	                imageouty[i] = _UNUSED;
	           }

	           // populate imageoutx
	           for(j=0;j<_SegWide;j++){
	             index=j;
	             index1 = j - _SegWide;
	             for(i=0;i<_SegHigh;i++){
	               backward[i]=0;
	               backcount[i]=0;
	             }
	             end=0;
	             for(i=0;i< _SegHigh;i++){
	               if(imageoutx[index] == _UNUSED){
	                // descriptor calculation along columns
	                 lvalue = _pImMean.getDouble(index);
	                 // detect data type
	                 //sumx = imgOrig.ConvertPixel2Double(imgOrig,index);//pImOrig[index];
	                 sumx = imgProcessed.getDouble(index);

	                 qu=0;
	                 k=1;
	                 index2 = index+ _SegWide;
	                 count=0;
	                 while( (i+k)< _SegHigh && qu!=1){
	                   lvalueN = _pImMean.getDouble(index2);//_pImMean[index2];

	                   if ( Math.abs( lvalue - lvalueN ) <=deltapom ){
	                    if(lvalue == lvalueN){
	                      forward[count] = index2;
	                      count++;
	                    }
	                    if(end < i+k){
	                     backward[i+k] = sumx;
	                     backcount[i+k] = k;
	                     end = i+k;
	                    }
	                    //sumx += imgOrig.ConvertPixel2Double(imgOrig,index2);
	                    sumx += imgProcessed.getDouble(index2);

	                    k=k+1;
	                    index2+= _SegWide;
	                  }else
	                     qu=1;
	                 }

	                 sumx += backward[i];
	                 pom=backcount[i]+ k;
	                 lvalue = sumx;
	                 if(pom!=0)
	                   lvalue = sumx/pom;

	                 imageoutx[index] = lvalue ;
	                 while(count>0){
	                   count--;
	                   imageoutx[ forward[count] ] = lvalue;
	                 }
	               }
	              index+= _SegWide;
	              index1+= _SegWide;
	             }// end of for(i)
	          }// end of for(j)

	          // populate imageouty
	          index=0;
	          index1 = -1;
	          for(i=0;i<_SegHigh;i++){
	           for(j=0;j<_SegWide;j++){
	             backward[j]=0;
	             backcount[j]=0;
	           }
	           end=0;
	           for(j=0;j<_SegWide;j++){
	            if(imageouty[index] == _UNUSED){
	              // descriptor calculation along rows
	                lvalue = _pImMean.getDouble(index);
	                //sumx = imgOrig.ConvertPixel2Double((imgOrig,index);//pImOrig[index];
	                sumx = imgProcessed.getDouble(index);

	                qu=0;
	                k=1;
	                index2 = index+1;
	                count=0;
	                while( (j+k)<_SegWide && qu!=1) {
	                 lvalueN = _pImMean.getDouble(index2);

	                 if ( Math.abs( lvalue - lvalueN ) <= deltapom ){
	                   if(lvalue == lvalueN) {
	                     forward[count] = index2;
	                     count++;
	                   }
	                   if(end < j+k){
	                    backward[j+k] = sumx;
	                    backcount[j+k] = k;
	                    end = j+k;
	                   }
	                   //sumx+= pImOrig[index2];
	                   //sumx += imgOrig.ConvertPixel2Double((imgOrig,index2);
	                   sumx += imgProcessed.getDouble(index2);

	                   k=k+1;
	                   index2+=1;
	                 }else
	                    qu=1;
	                }
	                sumx += backward[j];
	                pom=backcount[j]+ k;
	                lvalue = sumx;
	                if(pom!=0)
	                 lvalue /= pom;

	                imageouty[index] = lvalue;
	                while(count>0){
	                  count--;
	                  imageouty[ forward[count] ] = lvalue;
	                }
	            }
	            index++;
	            index1++;
	          }
	         }
	        }else{
	          // descriptors in the case of delta == 0
	          for(i=0;i<_SegSize;i++){
	            imageoutx[i] = imageouty[i] = imgProcessed.getDouble(i);//imgOrig
	          }
	        }

	        // region labeling
	        if(delta==minSigma){
	          _sizeSuma = _SegSize+1;
	          suma = new double[_SegSize+1];
	          _pNewSize = new int[_SegSize+1];
	          i = 1;
	          if(_flagEvenSigmaLayers){
	            i = _N2FindEvenSigmaLayers;
	          }
	          //test
	          logger.debug("Test: size of ImLabels ="+i);
	          try {
	          		_pImLabels = ImageObject.createImage(imgOrig.getNumRows(), imgOrig.getNumCols(), i, "INT");
	          } catch (ImageException e) {
	          	e.printStackTrace();
	          }
	          for(i=1;i<_SegSize+1;i++){
	            suma[i] = 0.0;
	            _pNewSize[i] = 0;
	          }

	          peto=1;
	          indexLabel = iterLayers;
	          index=0;
	          index1=1;
	          for(i=0;i<_SegHigh;i++){
	            for(j=0;j<_SegWide-1;j++){
	              _pImLabels.setInt(indexLabel, peto);
	              //suma[peto] += imgOrig.image[index];//pImOrig[index];
	              suma[peto] += imgProcessed.getDouble(index);
	              
	              _pNewSize[peto] +=1;
	              if(imageouty[index] != imageouty[index1])
	                peto++;
	              else {
	                lvalue = imageoutx[index];
	                if( Math.abs( imageoutx[index] - imageoutx[index1] ) > deltapom ){
	                //if(lower > imageoutx[index1] || imageoutx[index1] > upper)
	                  peto++;
	                }
	              }
	              index++;
	              index1++;
	              indexLabel += _pImLabels.getNumBands();
	            }
	            _pImLabels.setInt(indexLabel, peto);
	            //suma[peto] += imgOrig.image[index];//pImOrig[index];
	            suma[peto] += imgProcessed.getDouble(index);
	            _pNewSize[peto] +=1;
	            peto++;

	            index++;
	            index1++;
	            indexLabel += _pImLabels.getNumBands();
	          }
	          maxpetox=peto;
	          
	          // save the size of temp1, labeling and suma
	          _sizeLabeling = maxpetox;
	          temp1 = new int[maxpetox];
	          labeling = new int[maxpetox];
	          _pImValueS = new double[maxpetox];

	          for(peto=1;peto<maxpetox;peto++){
	             temp1[peto] = peto;
	          }
	        }else {
	          for(peto=1;peto<maxpetox;peto++){
	             temp1[peto] = peto;
	          }

	          // merge along columns
	          indexLabel = iterLayers;
	          index=0;
	          index1=1;
	          for(i=0;i<_SegHigh;i++){
	            for(j=0;j<_SegWide-1;j++){
	             iVal = temp1[ _pImLabels.getInt(indexLabel) ] ;
	             //iValN = temp1[ _pImLabels.imageInt[index1] ] ;
	             iValN = temp1[ _pImLabels.getInt(indexLabel+_pImLabels.getNumBands()) ] ;
	             if(iVal !=  iValN ){
	               if(imageouty[index] == imageouty[index1] ){
	                 ltemp = imageoutx[index];
	                 if( Math.abs( imageoutx[index] - imageoutx[index+1] )<=deltapom){
	                 //if(lower <= imageoutx[index1] && imageoutx[index1] <= upper){
	                   // merge two adjacent pixels (i,j) and (i,j+1)
	                   pom=iValN;
	                   while(temp1[pom] != pom){
	                     pom = temp1[pom];
	                   }

	                   pom1=iVal;
	                   while( temp1[pom1] != pom1){
	                      pom1 = temp1[pom1];
	                   }

	                   if(pom1>pom){
	                     temp1[pom1] = pom;
	                   }else{
	                     if(pom1<pom){
	                       temp1[pom] = pom1;
	                      }
	                   }
	                 }
	               }
	             }
	             index++;
	             index1++;
	             indexLabel += _pImLabels.getNumBands();
	           }
	           index++;
	           index1++;
	           indexLabel += _pImLabels.getNumBands();
	          }
	        }

	        // expanding regions along columns
	        indexLabel = iterLayers;
	        index=0;
	        index1=_SegWide;
	        offset = _pImLabels.getNumCols() * _pImLabels.getNumBands();
	        for(i=0;i<_SegHigh-1;i++){
	          for(j=0;j<_SegWide;j++){
	            iVal  = temp1[ _pImLabels.getInt(indexLabel) ];
	            //iValN = temp1[ _pImLabels.imageInt[index1] ];
	            iValN = temp1[ _pImLabels.getInt(indexLabel + offset) ];
	            if( iVal != iValN ){
	              if( imageoutx[index] == imageoutx[index1] ){
	                 ltemp = imageouty[index];
	                if( Math.abs( imageouty[index] - imageouty[index1] )<=deltapom){
	                //if(lower <= imageouty[index1] && imageouty[index1]<= upper){
	                  // merge two adjacent pixels (i,j) and (i+1,j)
	                  pom = iValN;
	                  while( temp1[pom] != pom){
	                    pom = temp1[pom];
	                  }

	                  pom1=iVal;
	                  while( temp1[pom1] != pom1){
	                    pom1 = temp1[pom1];
	                  }

	                  if(pom1>pom){
	                    temp1[pom1] = pom;
	                  }else {
	                   if(pom1<pom){
	                    temp1[pom] = pom1;
	                   }
	                  }
	                }
	              }
	             }
	             index++;
	             index1++;
	             indexLabel += _pImLabels.getNumBands();
	          }
	        }

	        // minimum label for the segments  plus update the size
	        for(peto=1;peto<maxpetox;peto++){
	          if( temp1[peto] != peto){
	            pom=temp1[peto];
	            while( temp1[pom] != pom )
	              pom = temp1[pom];

	            suma[pom] += suma[peto];
	            _pNewSize[pom] += _pNewSize[peto];
	            suma[peto] =0;
	            _pNewSize[peto] =0;
	            temp1[peto]=pom;
	          }
	        }

	        // relabel segments from 1 to max and calculate the sample mean of segments
	        count=0;
	        for(peto=1;peto<maxpetox;peto++){
	          if( _pNewSize[peto] != 0){
	            count+=1;
	            labeling[peto]=count;
	            dVal =  suma[peto]/( _pNewSize[peto] ) ;
	            _pImValueS[count] = dVal;
	            _pImMean.setDouble(count, dVal) ;//(unsigned long)  fvalue*65536.0; // 2^16
	            _pNewSize[count]=_pNewSize[peto];
	            suma[count] = suma[peto];
	          }
	        }

	        // assign new label and new avg gray value to each pixel
	        /*
	        for(i=0;i<_SegSize;i++){
	           peto= _pImLabels.imageInt[i];
	           peto = labeling[ temp1[peto] ];
	           //pImValue.imageDouble[i] = _pImValueS[peto];
	           _pImMean.imageDouble[i] = _pImValueS[peto];
	           _pImLabels.imageInt[i] = peto;
	        }
	        */
	        indexLabel = iterLayers;
	        idx = 0;
	        minTemp = _lim.MAX_DOUBLE;
	        for(i=0;i<_pImLabels.getNumRows();i++){
	          for(j=0;j<_pImLabels.getNumCols();j++){
	             peto= _pImLabels.getInt(indexLabel);
	             peto = labeling[ temp1[peto] ];
	             _pImMean.setDouble(idx, _pImValueS[peto]);
	             _pImLabels.setInt(indexLabel, peto);
	             // find the next step (delta)
	             if(j>0){
	               ltemp = _pImMean.getDouble(idx) - _pImMean.getDouble(idx-1);
	               if(ltemp < 0.0)
	                 ltemp = -ltemp;
	               if( ltemp > 0.0 && ltemp < minTemp){
	                 minTemp = ltemp;
	                 //System.out.println("TEST:minCol i="+i+",j="+j);
	               }
	             }
	             if(i>0){
	               ltemp = _pImMean.getDouble(idx) - _pImMean.getDouble(idx-_pImMean.getNumCols());
	               if(ltemp < 0.0)
	                 ltemp = -ltemp;
	               if( ltemp > 0.0 && ltemp < minTemp){
	                 minTemp = ltemp;
	                 //System.out.println("TEST:minRow i="+i+",j="+j);
	               }
	             }
	             idx++;
	             indexLabel += _pImLabels.getNumBands();
	          }// end of for(j)
	        }// end of for(i)

	        
	        maxpetox=count+1;
	        _NFoundS = count;

	        //////////////////////////////////////////////////////////
	        // save statistics of mergers
	         //////////////////////////////////////////////////////////
	         // save statistics of mergers
	         if( !_flagEvenSigmaLayers){
	            // do not adaptively change the deltaSigma if layers should be evenly distributed
	            if(minTemp-delta > _deltaSigma && minTemp < _lim.MAX_DOUBLE){
	              deltaSigma = (float)(minTemp-delta)*1.0001F;
	            }else{
	              deltaSigma = _deltaSigma;
	            }
	         }
	         if(_debugSeg2D)
	         	logger.debug("TEST: next deltaSigma is "+ deltaSigma+ ",suggested deltaSigma="+(minTemp-delta));

	         if(!_flagEvenSigmaLayers){
	           i = iter *_mergeStats.getNumBands();
	           if(i + 2 < _mergeStats.getSize() ){
	             _mergeStats.setDouble(i, delta);
	             _mergeStats.setDouble(i+1, minTemp);
	             _mergeStats.setDouble(i+2, _NFoundS);
	           }
	         }
	         // end of save statistics of mergers
	        //////////////////////////////////////////////////////////

	        // test
	        if(_debugSeg2D)
	        	logger.debug( "TEST: after NFound was updated; NFOUNDS=" + _NFoundS+", delta="+delta);
	        //PrintSegObject();

	        //////////////////////////////////////////////////////////
	        // filtering
	        //if( _flagNoiseFilter && delta <= minSigma+3*deltaSigma) {
	        if( _flagNoiseFilter ) {
	        	logger.debug("INFO: filtering applied at delta="+delta);
	         //if(delta<1)
	          //NoiseRemoval(_SegWide,_SegHigh, _NFoundS,1,25, pImValue, _pImValueS,pImOrig, _pImLabels,_pNewSize);
	          if( !noiseRemoval(_NFoundS,1,_minSizeS,_pImMean, _pImValueS, imgProcessed, _pImLabels,iterLayers,_pNewSize) ){
	          	logger.error("ERROR: NoiseRemoval failed");
	          }
	        }



	        // exit criteria
	        if(_NFoundS <= 1){
	        	logger.debug("INFO: exit based on NFoundS<=1");
	          //SaveLabelImage(delta);
	          //SaveMeanOrigImage(imgOrig,_pImLabels, delta);
	          //SaveMeanProcessedImage(delta);
	          if(_flagEvenSigmaLayers ){
	            // take care of the remaining layers
	             if(_NFoundS > _maxNFoundSLayers){
	               _maxNFoundSLayers = _NFoundS;
	             }
	             if(iterLayers+1 < _N2FindEvenSigmaLayers){
	                 for(indexLabel=iterLayers;indexLabel<_pImLabels.getSize();indexLabel+=_pImLabels.getNumBands()){
	                  _pImLabels.setInt(indexLabel+1, _pImLabels.getInt(indexLabel));
	                }
	             }
	             iterLayers = _N2FindEvenSigmaLayers;
	          }
	           // save statistics of layers
	           i = iter *_mergeStats.getNumBands();
	           if(i + 2 < _mergeStats.getSize() ){
	             _mergeStats.setDouble(i, delta);
	             _mergeStats.setDouble(i+1, minTemp);
	             _mergeStats.setDouble(i+2, _NFoundS);
	           }

	          signalExit = true;
	        }
	        if(!signalExit && _flagN2FindS && _NFoundS <= _N2FindS){
	        	logger.debug("INFO: exit based on NFoundS="+_NFoundS+"<=_desired N2FindS="+_N2FindS);
	          //SaveLabelImage(delta);
	          //SaveMeanOrigImage(imgOrig,_pImLabels, delta);
	          //SaveMeanProcessedImage(delta);
	          signalExit = true;
	        }
	        if(!signalExit && _flagEvenSigmaLayers ){
	           if( deltaSigma > 0.0){
	             //valD = (maxSigma-minSigma)/deltaSigma;// the number of iterations
	             //valD /= _N2FindEvenSigmaLayers; // the number of scales to skip
	             if(_N2FindEvenSigmaLayers<1)
	               _N2FindEvenSigmaLayers = 1;
	             valD = (maxSigma-minSigma)/_N2FindEvenSigmaLayers;
	             //test
	             logger.debug("Test: testing iterLayer="+iterLayers+", valD="+valD+", delta="+delta+", deltaSigma="+deltaSigma);

	             if( Math.abs(valD*(iterLayers+1)+minSigma - delta) < deltaSigma){
	             	logger.debug("INFO: save segmentation layer based on evenly distributed sigma="
	                             +delta+",iter="+iter+",iterLayers="+iterLayers);

	               // save statistics of layers
	               i = iter *_mergeStats.getNumBands();
	               if(i + 2 < _mergeStats.getSize() ){
	                 _mergeStats.setDouble(i, delta);
	                 _mergeStats.setDouble(i+1, minTemp);
	                 _mergeStats.setDouble(i+2, _NFoundS);
	               }

	               if(_NFoundS > _maxNFoundSLayers){
	                 _maxNFoundSLayers = _NFoundS;
	                 //test
	                 //System.out.println("INFO: maxNFoundSLayers = "+_maxNFoundSLayers);
	               }
	               if(iterLayers+1 < _N2FindEvenSigmaLayers){
	                 for(indexLabel=iterLayers;indexLabel<_pImLabels.getSize();indexLabel+=_pImLabels.getNumBands()){
	                  _pImLabels.setInt(indexLabel+1, _pImLabels.getInt(indexLabel));
	                }
	               }
	               iterLayers ++;
	             }
	           }
	          //if(delta > maxSigma-2*deltaSigma){
	          //if(delta > (maxSigma+minSigma) *0.75){
	            //SaveLabelImage(delta);
	            //SaveMeanProcessedImage(delta);
	            //SaveMeanOrigImage(imgOrig,_pImLabels, delta);
	          //}

	          //SaveLabelImage(delta);
	          //SaveMeanOrigImage(imgOrig,_pImLabels, delta);
	          //signalExit = true;
	        }


	        if(_debugSeg2D && !_flagEvenSigmaLayers && !_flagN2FindS && delta > (maxSigma+minSigma)*0.5){
	         //System.out.println( "TEST: delta=" + delta + "\t NFOUNDS=" + _NFoundS );
	        //if(_debugSeg2D ){
	          //System.out.println("Test: pokusL1_"+_debugSeg2D+",delta="+delta);
	          //SaveLabelImage(delta);

	          //if(delta > maxSigma-2*deltaSigma){
	          //  SaveMeanProcessedImage(delta);
	          //  SaveMeanOrigImage(imgOrig,_pImLabels, delta);
	          //}
	          //ComputeEdgepts(name);
	        //}
	       }

	      }// end of for(delta)


	      //NoiseRemoval(_SegWide,_SegHigh, _NFoundS,2,_MinSize, pImValue, _pImValueS,pImOrig, _pImLabels,_pNewSize);

	      if(_debugSeg2D){
	      	logger.debug("Test NFOUNDS=" + _NFoundS);
	        //test
	        //PrintMergeSigma();

	        // test print out
	        // if(_Extract == EDGE){
	        //sprintf(name,"%s%d%d","pokusE1",_debugSeg2D,delta);
	        //ComputeEdgepts(name);
	        //}else{
	        // sprintf(name,"%s%d","pokusL1",_debugSeg2D);
	        //WriteLabels2PGM(name,_pImLabels,_SegHigh,_SegWide);
	            //}

	        //int signal=0;
	        //if(_AdjustPGM != NoAdjustPGM){
	        //  _AdjustPGM = NoAdjustPGM;
	        //  signal=1;
	        //}
	        //sprintf(name,"%s%d","pokusM1",_debugSeg2D);
	        //WritePGM(name,pImValue,_SegHigh,_SegWide); // due to outliers bad
	       //if(signal)
	       // _AdjustPGM = LogCompressPGM;

	      }

	      
	        // size of one row or column
	        backward = null;
	        backcount = null;
	        forward = null;
	        // size of region of interest area
	        imageoutx = null;
	        imageouty = null;

	        // size of max number of labels
	        suma = null;
	        _sizeSuma = -1;
	        temp1 = null;
	        _sizeLabeling = -1;
	        labeling = null;

	        imgProcessed = null;

	      return true;
	    }

	    private void mergefast(int first, int second,ImageObject region,int [] temp1){
	      int pom,pom1;
	      pom=temp1[ region.getInt(second) ];
	      while( temp1[pom] != pom)
	        pom = temp1[pom];

	      pom1 = temp1[ region.getInt(first) ];
	      while( temp1[pom1] != pom1)
	        pom1 = temp1[pom1];

	      if(pom1 > pom){
	        temp1[pom1] = pom;
	      }else{
	        if(pom1 < pom){
	          temp1[pom] = pom1;
	        }
	      }

	    }

	    public boolean saveMeanProcessedImage(float delta){
	      String nameMean = "pokusMean";
	      boolean ret;
	      try{
	         if(_pImMean != null){
	          _myIIPImage.writeImage((nameMean+"_" + Float.toString(delta)+".iip") , _pImMean);
	         }else{
	         	logger.debug("ERROR: missing mean image");
	            return false;
	         }
	      }catch(Exception e){
	      	logger.debug("ERROR: IO exception in IIPImage output of label and mean image");
	        return false;
	      }
	      return true;
	    }
	    public boolean saveMeanOrigImage(ImageObject imgOrig,ImageObject imgLabel, float delta){
	      String nameMean = "pokusMeanOrig";
	      // having this locally will release the memory of mean and stdev arrays after saving out the file
	      ImStats _myImStats = new ImStats();

	      boolean ret;
	      try{
	         if(imgLabel != null && imgOrig != null){
	          //_myImStats.SetIsMaskPresent(true);
	          //_myImStats.SetMaskObject(imgLabel);

	           ret = _myImStats.MeanStdevTable(imgOrig, imgLabel);
	           if(ret){
	             ImageObject imgMean = null;
	             //imgMean = _myImStats.GetMeanIm();
	             imgMean = _myImStats.ImageTable2Image(imgLabel);
	             //test
	             logger.debug(imgMean.toString());
	             if(imgMean != null)
	               _myIIPImage.writeImage((nameMean+"_" + Float.toString(delta)+".iip") , imgMean);
	          }
	         }else{
	         	logger.debug("ERROR: missing orig or mean image");
	            return false;
	         }
	      }catch(Exception e){
	      	logger.debug("ERROR: IO exception in IIPImage output of label and mean image");
	        return false;
	      }
	      return true;
	    }

	    public boolean saveLabelImage(float delta){
	      String nameLabel = "pokusLabel";
	      boolean ret;
	      try{
	         if(_pImLabels != null){
	           if(_flagEnhanceLabels){
	             if(_flagEvenSigmaLayers && _N2FindEvenSigmaLayers > 1){
	               //test
	               //System.out.println("Test: _maxNFoundSLayers="+_maxNFoundSLayers);
	               ret  =  _myImEnhance.EnhanceLabelsIn(_pImLabels,(_maxNFoundSLayers+1),true);
	             }else{
	               ret  =  _myImEnhance.EnhanceLabelsIn(_pImLabels,(GetNFoundS()+1),true);
	             }

	             //test
	             //System.out.println("Test: before IIP");

	             if(ret){
	               _myIIPImage.writeImage( (nameLabel+"_"+Float.toString(delta)+".iip"), _myImEnhance.GetEnhancedObject() );
	             }else{
	             	logger.debug("ERROR: cannot enhance image with color hist. eq.");
	             }
	           }else{
	              _myIIPImage.writeImage( (nameLabel+"_"+Float.toString(delta)+".iip"), _pImLabels );
	           }
	         }else{
	         	logger.debug("ERROR: missing label image");
	            return false;
	         }
	      }catch(Exception e){
	      	logger.debug("ERROR: IO exception in IIPImage output of label image");
	        return false;
	      }
	      return true;
	    }

	    /*
	    /////////////////////////////////////////////////////////
//	 Computes edge pixels from a segmented image
////////////////////////////////////////////////////////	/
	long Segment::ComputeEdgepts(char *outfilename)
	{
	  unsigned char *imageout,*storepuch;
	  int i,j;
	  //long boundrow,boundcol;
	  long index,index1;

	  if(_SegSize >0 && pImValueOrig!=NULL){
	    if( (imageout = new unsigned char[_SegSize]) == NULL){
	      cerr << " Error : run out of memory \n";
	      return 1;
	    }
	  } else
	     return 1;

	 // create the new image with edges
	  storepuch = imageout;
	  for(i=0;i<_SegSize;i++){
	    *imageout = 255;
	    imageout++;
	  }
	  imageout = storepuch;

	  index =0;
	  for(i=0;i<_SegHigh;i++){
	    for(j=0;j<_SegWide-1;j++){
	      if(_pImLabels[index]  != _pImLabels[index+1] ){
	       imageout[index+1] = 0;
	       //boundrow+=1;
	      }
	      index++;
	    }
	    index++;
	  }

	  index=0;
	  index1=_SegWide;
	  for(i= 0; i < _SegHigh-1; i++){
	    for(j = 0; j < _SegWide; j++){
	      if(_pImLabels[index] != _pImLabels[index1]){
	        if( imageout[index] !=0 &&  imageout[index1] !=0){
	          imageout[index1] = 0;
	          //boundcol+=1;
		}
	      }
	      index++;
	      index1++;
	    }
	  }

	  // output
	  //cout << "output file name = " << outfilename << endl;
	  ofstream outfile(outfilename,ios::out);
	  if( !outfile ){ // opened failed
	    cerr << "cannot open " << outfilename << "for output \n";
	    return 1;
	  }

	  // Write header
	  outfile << "P5" << "\n" << _SegWide << " " << _SegHigh << endl << MaxPGM << endl;

	  for(i=0;i<_SegSize;i++) {
	    outfile.put(imageout[i]);
	  }
	  outfile.close();

	  //  WritePGM(outfilename,imageout,_SegHigh,_SegWide);

	  delete [] imageout;
	  return 0;
	}
	*/

	  //////////////////////////////////////////////////////////////
	  // This is a method for cleaning up small regions surrounded by
	  // large regions. It is based on Gaussian distribution of intensity
	  // values inside of a region.
	  //////////////////////////////////////////////////////////////
	   public boolean noiseRemoval(int maxpetox,int maxitera,int minsize,ImageObject imageMean,
	         double [] imageS, ImageObject imgprocessed, ImageObject region, int iterLayers, int [] newsize)  {

	          //sanity check
	          if(imgprocessed == null || imgprocessed.isHeaderOnly() ){
	          	logger.debug("ERROR: imgprocessed != DOUBLE");
	             return false;
	          }
	          if(region == null || region.isHeaderOnly() ){
	          	logger.debug("ERROR: region != INT");
	             return false;
	          }

	      int i,j,i1,j1,k;
	      boolean signal = true;
	      int window, percent, maxhist;
	      int [] hist = null;
	      int [] histi1j1 = null;
	      double [] histattrib = null;
	      double [] image1 = null;
	      int [] histcount = null;

	      int count=0,index,indexLabel,indexLabel1, max,peto;
	      double minvalue, mindif,tmpvalue;
	      int itera;
	      int change;
	      int here,here1,top,maxindex;
	      int pom;
	      // might bemoved to init()
	      //double [] suma = null;
	      //int [] temp1 = null;
	      //int [] labeling = null;

	      maxpetox++; // this is due to the fact that we pass _NFoundS = maxpetox-1;

	      // initial setup
	      window = _filterWindow;//1;
	      percent = (int) ( _percentFilterWindow * ( (window<<1) +1)*( (window<<1) +1) +0.5) ;//5; // 0.64 x ( (2 x window +1)^2 -1)

	      if(maxitera > 0){
	        pom=(2*window+1)*(2*window+1);
	        top = imageMean.getNumRows() * imageMean.getNumCols();//xsz*ysz;
	        hist = new int[pom];
	        histi1j1 = new int[pom];

	        histcount = new int[pom];
	        histattrib = new double[pom];

	        image1 = new double[top];

	        if(suma == null || maxpetox > _sizeSuma){
	          suma = new double[maxpetox];
	        }
	        if(labeling == null || maxpetox > _sizeLabeling){
	          labeling = new int[maxpetox];
	        }
	        if(temp1 == null || maxpetox >= _sizeLabeling){
	          temp1 = new int[maxpetox];
	        }

	      }else
	        return true;

	      change=120;
	      itera=0;
	      while(change > 0 && itera<maxitera){
	        change=0;
	        for(i=1;i<maxpetox;i++)
	          temp1[i]=i;

	        here = 0;
	        indexLabel = iterLayers;

	        //here = window*imageMean.numcols;//0;
	        //indexLabel = here*region.sampPerPixel + iterLayers;
	        //offsetHere1 = -here;//-window*imageMean.numcols;
	        //offsetLabel1 = -indexLabel+iterLayers;//offsetHere1* region.sampPerPixel + iterLayers;

	        for(i=0;i<imageMean.getNumRows();i++){
	         for(j=0;j<imageMean.getNumCols();j++){
	           image1[here]=imageMean.getDouble(here);


	           //if( newsize[ temp1[ region.imageInt[here]]  ] < minsize){
	           if( newsize[ temp1[ region.getInt(indexLabel)]  ] < minsize){
	             //test
	             //System.out.println("TEST: merger at i="+i+",j="+j);

	            // calculate the histogram of region labels
	            maxhist=0;
	            i1=i-window;
	            j1=j-window;
	            //here1 = here+offsetHere1+j1;
	            //indexLabel1 = indexLabel+offsetLabel1+j1;

	            while( i1<imageMean.getNumRows() && i1<(i+window+1) ){
	              if(i1>=0){
	                j1=j-window;
	                here1 = i1*imageMean.getNumCols()+j1;
	                indexLabel1 = here1* region.getNumBands() + iterLayers;

	              while( j1<imageMean.getNumCols() && j1< (j+window+1) ){
	                if((i1==i && j1==j) || j1<0){
	                }else{
	                   signal=false;
	                   for(k=0;k<maxhist;k++){
	                     //if( hist[k] == region.imageInt[here1] ){
	                     if( hist[k] == region.getInt(indexLabel1) ){
	                       signal=true;
	                       histcount[k] += 1;
	                       k=maxhist;
	                     }
	                   }
	                   if(!signal){
	                     //hist[maxhist] = region.imageInt[here1];
	                     hist[maxhist] = region.getInt(indexLabel1);
	                     histcount[maxhist] = 1;
	                     histattrib[maxhist] = imageMean.getDouble(here1);
	                     histi1j1[maxhist] = indexLabel1;//here1;
	                     maxhist+=1;
	                   }
	                }
	                j1+=1;
	                here1++;
	                indexLabel1 += region.getNumBands();
	              }
	            }
	            i1+=1;
	           }
	            //if the histogram at (i,j) contains a bin with a value larger
	            // than percent then (i,J) merges with the label of max
	           max=0;
	           index = maxindex = 0;
	           mindif = _lim.MAX_DOUBLE;// (float)MaxPGM;
	           minvalue= _lim.MAX_DOUBLE;//(float)MaxPGM;
	           //test
	           //System.out.println("TEST: maxhist="+maxhist);

	           for(k=0;k<maxhist;k++){
	             //test
	             //System.out.println("TEST:histcount["+k+"]="+histcount[k]);

	             signal=false;
	             if( histcount[k] >= percent){
	               if( temp1[ region.getInt(indexLabel) ] != temp1[ hist[k] ] ){
	                 mergefast(indexLabel,histi1j1[k],region,temp1);
	                 tmpvalue=  newsize[ region.getInt(indexLabel) ];
	                 newsize[ region.getInt(indexLabel) ] += newsize[ region.getInt( histi1j1[k])   ];

	                 newsize[ region.getInt( histi1j1[k]) ] += tmpvalue;
	                 image1[here]= histattrib[k];
	                 change+=1;
	               }
	               signal=true;
	               k=maxhist;
	             }else {
	               if( histcount[k] > max){
	                 max = histcount[k];
	                 maxindex = histi1j1[k];
	                 index=k;
	               }
	               // if two bins have equal max values then merge to the one
	               // having closer avg gray value
	               if( histcount[k] == max){
	                 if( Math.abs( histattrib[index] - imgprocessed.getDouble(here) ) > Math.abs( histattrib[k] - imgprocessed.getDouble(here) )  ) {
	                   max = histcount[k];
	                   maxindex = histi1j1[k];
	                   index=k;
	                 }
	               }
	               // special care is needed around the image border
	               if((i==0 || i==(imageMean.getNumRows()-1) || j==0 || j==(imageMean.getNumCols()-1)) &&  Math.abs( histattrib[k] - imgprocessed.getDouble(here) ) <= mindif){
	                 mindif = Math.abs( histattrib[k] - imgprocessed.getDouble(here) );
	                 minvalue = histattrib[k];
	              }
	             }
	           }

	           /*
	           if( !signal ){
	             // takes care of the noise along region borders
	             if( max>=percent-1  &&  temp1[ region.imageInt[indexLabel] ] != temp1[ region.imageInt[maxindex] ] ){
	               mergefast(indexLabel,maxindex,region,temp1);
	               tmpvalue=  newsize[ region.imageInt[indexLabel] ];
	               newsize[ region.imageInt[indexLabel] ] += newsize[ region.imageInt[maxindex] ];
	               newsize[ region.imageInt[maxindex] ] += tmpvalue;

	               image1[here]= histattrib[index];
	               //change+=1;
	             }else{
	               //takes  care of the image borders
	               if(i==0 || i==(imageMean.numrows-1) || j==0 || j==(imageMean.numcols-1)){
	                 image1[here] = minvalue;
	               }
	             }
	           }
	           */
	          }
	          here++;
	          indexLabel += region.getNumBands();
	         }
	       }
	       // minimum label for the regions
	       for(peto=1;peto<maxpetox;peto++){
	         if( temp1[peto] != peto){
	           pom = temp1[peto];
	           while( temp1[pom] != pom)
	           pom = temp1[pom];

	           temp1[peto] = pom;
	         }
	       }
	       // initialize newsize and new labels
	       for(peto=1;peto<maxpetox;peto++){
	         newsize[peto]=0;
	         labeling[peto] = temp1[peto];
	         temp1[peto] = 0;
	       }
	       // count size of new regions
	       //for(i=0;i<top;i++){
	       for(indexLabel=iterLayers; indexLabel < region.getSize(); indexLabel+=region.getNumBands()){
	         peto = region.getInt(indexLabel);
	         peto = labeling[peto];
	         newsize[peto] +=1;
	       }

	       // relabel new regions
	       count=0;
	       for(peto=1;peto<maxpetox;peto++) {
	         if( newsize[peto] !=0) {
	           count+=1;
	           temp1[peto] = count;
	            newsize[count] = newsize[peto];
	         }
	       }
	       // update region labels and image values
	       indexLabel = iterLayers;
	       for(i=0;i<top;i++){
	         peto= region.getInt(indexLabel);
	         peto = temp1[ labeling[peto] ] ;
	         region.setInt(indexLabel, peto);
	         imageMean.setDouble(i, image1[i]);
	         indexLabel += region.getNumBands();
	       }
	       maxpetox=count+1;
	       itera+=1;
	       logger.debug("Test:\t itera=" + itera + "\t change=" + change );
	      }
	      // update the max number of regions
	      _NFoundS = count;

	      // update the avg. image values
	      for(i=1;i<maxpetox;i++)
	        suma[i]=0.0;

	      indexLabel = iterLayers;
	      for(i=0;i<top;i++){
	        suma[ region.getInt(indexLabel) ] += imgprocessed.getDouble(i);
	        indexLabel += region.getNumBands();
	      }

	      for(i=1;i<maxpetox;i++){
	        if(newsize[i] !=0)
	          imageS[i] = suma[i]/newsize[i];
	        else {
	        	logger.error("ERROR at suma["+i+"]= "+suma[i] );
	          imageS[i]=0;
	        }
	      }

	      indexLabel = iterLayers;
	      for(i=0;i<top;i++){
	        imageMean.setDouble(i, imageS[ region.getInt(indexLabel) ]);
	        indexLabel += region.getNumBands();
	      }


	      if(_debugSeg2D){
	      	logger.debug("INFO: after noise removal NFOUNDS=" + _NFoundS );
	      }
	      hist = null;
	      histi1j1 = null;
	      histcount = null;
	      histattrib = null;

	      image1 = null;

	      suma = null;
	      temp1 = null;
	      labeling = null;
	      return true;
	  }
}
