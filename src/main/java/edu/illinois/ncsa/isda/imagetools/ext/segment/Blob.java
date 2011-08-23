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
 * Created on Jul 15, 2005
 */
package edu.illinois.ncsa.isda.imagetools.ext.segment;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.imagetools.core.datatype.*;
import edu.illinois.ncsa.isda.imagetools.ext.math.GeomOper;
import edu.illinois.ncsa.isda.imagetools.ext.segment.DrawOp;


/**
<html>
<body>
	<p>
	The class Blob provides characteristics of a blob (connected segments of the same color)
	obtained by connectivity analysis (color labels are unique) .
	</p>

	<p>
	<B>Description:</B>
	<B>
	Release notes:
	</B>
	</p>
</body>
</html>
@author  Peter Bajcsy, Peter Ferak
@version 2.0
 */
public class Blob {
	  BlobBound []  				_pBoundBox			= null;
	  	BlobObject [] 				_pBlobDes			= null;
	  	long 						_NumBoxLabels; 					// the number of bounding boxes
	  	long 						_NumStatsLabels; 				// the number of blobs used for computing statistics
	  	boolean 					_debugBlob			= false;
	  	String 						_boundBoxType 		= new String();
		private static Log 			logger 				= LogFactory.getLog(BoundBox.class);
		private LimitValues 		_lim 				= new LimitValues();

	    //constructors
	  public Blob(){
	    reset();
	    _boundBoxType = "Rectangular";//"Minimal";// or Rectangular
	  }
	    /**
	     * @deprecated use reset()
	     */
	  private void Reset(){
	  	reset();
	  }
	  private void reset(){
	    _debugBlob = false;
	  }


	  ///////////////////////////////////
	  //setters and getters
	  public boolean getDebug(){ return _debugBlob;}
	  public void setDebug(boolean val){ _debugBlob = val;}
	  
	  public BlobObject[] getBlobDes() { return _pBlobDes;}
	  public BlobBound[] getBoundBox() {return _pBoundBox;}
	    /**
	     * @deprecated use getBoundBoxType()
	     */
	  public String GetBoundBoxType(){ return _boundBoxType;}
	  public String getBoundBoxType(){ return _boundBoxType;}
	  
	  public boolean setBoundBoxType(String val){
	    if(val == null)
	     return false;
	   if(val.equalsIgnoreCase("Minimal") ) {
	     _boundBoxType = val;
	      return true;
	   }
	   if(val.equalsIgnoreCase("Rectangular") ) {
	     _boundBoxType = val;
	      return true;
	   }
	   return false;
	  }

	  public long getNumBoxLabels() { return _NumBoxLabels;}
	  public long getNumStatsLabels() {return _NumStatsLabels;}

	  public BlobBound getBoundBox(int index){
	     if(_pBoundBox != null && index >= 0 && index < _NumBoxLabels+1)
	       return ( _pBoundBox[index] );
	     else
	       return null;
	  }
	  public BlobObject getBlobDescriptor(int index){
	    if(_pBlobDes != null && index >= 0 && index < _NumStatsLabels+1 ){
	       return ( _pBlobDes[index] );
	    }else   return null;
	  }

	  //////////////////////////////////////////////////////////////
	  // doers
	  ////////////////////////////////////////////////////////////////
	  ////////////////////////////////////////////////////////////
	  // Compute one bounding box for all segments labeled other than bin_value
	  // in pImLabels
	  // used for finding a bounding box of all features in binary image
	  // (application : RoundPeak and region detection)
	  ////////////////////////////////////////////////////////////
	  //public BlobBound BoundSparseBox(tMatrix<unsigned char> *pLabels,unsigned char bin_value)  {
	  public BlobBound blobBoundSparseBox(ImageObject pLabels, byte bin_value)  {
	    if(pLabels==null){
	      logger.debug( "Error: no valid image in Blob::BoundSegmBox" );
	      return null;
	    }
	    if( !pLabels.getTypeString().equalsIgnoreCase("BYTE") ){
	    	logger.debug( "Error: image input type other than BYTE is not supported" );
	      return null;
	    }

	    int numrows,numcols;
	    numrows = pLabels.getNumRows();
	    numcols = pLabels.getNumCols();

	    logger.debug( "Test inside BoundSparseBox" );

	    // calculates the bounding box for a labeled region
	    if(numcols > 0 && numrows > 0){
	      //allocate memory
	      BlobBound box=null;
	      box = new BlobBound();

	      int index,count;
	      boolean signal = true;
	      ImPoint ptsc = new ImPoint();
	      int i,j;

	      //used for testing
	      //DrawOper drawOp;
	      //unsigned char color = 120;

	      // init the BlobBound
	      box.getMiniMinj().x  = numrows;
	      box.getMiniMinj().y  = numcols;
	      box.getMaxiMaxj().x  = -1;;
	      box.getMaxiMaxj().y  = -1;
	      box.getMinjMaxi().x  = -1;
	      box.getMinjMaxi().y  = numcols;
	      box.getMaxjMini().x  = numrows;
	      box.getMaxjMini().y  = -1;

	      index = count = 0;
	      for(i=0;i<numrows;i++){
	        for(j=0;j<numcols;j++){
	         if(pLabels.getDouble(index)!=bin_value){
	           count++;
	           // find bounding box
	           // mini (and minj)
	           if( box.getMiniMinj().x  >= i){
	             signal = false;
	             if( (int) box.getMiniMinj().x ==  i){
	                if( box.getMiniMinj().y > j)
	                  signal = true;
	             }else
	                signal = true;
	             if(signal){
	               box.getMiniMinj().x = i;
	               box.getMiniMinj().y = j;
	             }
	           }
	           // maxi (and maxj)
	           if( box.getMaxiMaxj().x  <= i){
	             signal = false;
	             if( (int) box.getMaxiMaxj().x ==  i){
	               if( box.getMaxiMaxj().y < j)
	                 signal = true;
	             }else
	               signal = true;
	             if(signal){
	               box.getMaxiMaxj().x = i;
	               box.getMaxiMaxj().y = j;
	             }
	           }
	           // minj (and maxi)
	           if( box.getMinjMaxi().y  >= j){
	             signal = false;
	             if( (int) box.getMinjMaxi().y ==  j){
	               if( box.getMinjMaxi().x < i)
	                 signal = true;
	             }else
	               signal = true;
	             if(signal){
	               box.getMinjMaxi().x = i;
	               box.getMinjMaxi().y = j;
	             }
	           }
	           // maxj (and mini)
	           if( box.getMaxjMini().y  <= j){
	             signal = false;
	             if( (int) box.getMaxjMini().y ==  j){
	               if( box.getMaxjMini().x > i)
	                 signal = true;
	             }else
	               signal = true;
	             if(signal){
	               box.getMaxjMini().x = i;
	               box.getMaxjMini().y = j;
	             }
	           }
	         }
	         index++;
	        }
	      }
	      // no pixel
	      if(count <=0){
	      	logger.debug( " Error: no valid pixel to process " );
	        return(box);
	      }

	      //test
	      logger.info( "count enclosed pixel=" + count );

	      //approximate the blob with a rectangular box
	      // find the most upper left point
	      if(box.getMiniMinj().x < box.getMaxjMini().x)
	         box.getArea().setRow((float)box.getMiniMinj().x);
	      else
	         box.getArea().setRow((float)box.getMaxjMini().x);
	      if(box.getMiniMinj().y < box.getMinjMaxi().y)
	         box.getArea().setCol((float)box.getMiniMinj().y);
	      else
	         box.getArea().setCol((float)box.getMinjMaxi().y);
	      // find the most lower right point
	      if(box.getMaxiMaxj().x > box.getMinjMaxi().x)
	         ptsc.x   =  (float)box.getMaxiMaxj().x;
	      else
	         ptsc.x   =  (float)box.getMinjMaxi().x;
	      if(box.getMaxiMaxj().y > box.getMaxjMini().y)
	         ptsc.y   =  (float)box.getMaxiMaxj().y;
	      else
	         ptsc.y   =  (float)box.getMaxjMini().y;


	      box.getArea().setHeight((float)ptsc.x - box.getArea().getRow() + 1.0F);
	      if(box.getArea().getHeight() == 0.0)
	         box.getArea().setHeight((float)(box.getArea().getHeight() + 1.0));
	      box.getArea().setWidth((float)ptsc.y - box.getArea().getCol() +1.0F);
	      if(box.getArea().getWidth() == 0.0F)
	        box.getArea().setWidth((float)(box.getArea().getWidth() + 1.0F));
	      if(box.getArea().getHeight() >=numrows)
	        box.getArea().setHeight(numrows -1.0F);
	      if(box.getArea().getWidth() >= numcols)
	        box.getArea().setWidth(numcols - 1.0F);
	      box.getArea().setAngle(0.0F);
	      box.SetPerim( (int) (2.0F*( box.getArea().getHeight() + box.getArea().getWidth())) );

	      //test
	      //    ptsc.x = box.GetArea().Row;
	      //  ptsc.y = box.GetArea().Col;
	      // drawOp.draw_solidbox(pLabels,box.GetArea(),ptsc,box.GetArea().Angle,color);

	      return(box);
	    }else{
	    	logger.debug( "Error: no valid label image " );
	      return null;
	    }
	  }

	  ////////////////////////////////////////////////////////////
	  // Given a bounding box of all segments labeled other than bin_value
	  // in pImLabels, find the best fit to a rotated rectangle
	  // (application : RoundPeak and region detection)
	  ////////////////////////////////////////////////////////////
	  //SubAreaF *Blob::BoundSparseRotBox(tMatrix<unsigned char> *pLabels,unsigned char bin_value, BlobBound box)
	  public SubAreaFloat blobBoundSparseRotBox(ImageObject pLabels,ImageObject bin_value, BlobBound box)  {
	    if(pLabels==null){
	    	logger.debug( "Error: no valid image in Blob::BoundSparseRotBox" );
	      return null;
	    }
	    if( !pLabels.getTypeString().equalsIgnoreCase("BYTE") ){
	    	logger.debug( "Error: image input type other than BYTE is not supported" );
	      return null;
	    }
	    if( bin_value == null || !pLabels.getTypeString().equalsIgnoreCase(bin_value.getTypeString()) ){
	    	logger.debug( "Error: missing bin_value or the type is inconsistent with label image");
	      return null;
	    }
	    if(box == null){
	    	logger.debug( "Error: no valid BlobBound Box" );
	      return null;
	    }
	    int numrows,numcols;
	    numrows = pLabels.getNumRows();
	    numcols = pLabels.getNumCols();

	    logger.info( "Test inside BoundSparseRotBox" );

	    // calculates the bounding box for a labeled region
	    if(numcols > 0 && numrows > 0){
	      ////////////////////////////////////////////////////
	      // find better approximation than a zero-angle box to the set of areas
	      // labeled in pLabels by rotating the bounding box
	      ImPoint [] pts = new ImPoint[4];
	      int i;
	      for(i=0;i<4;i++)
	        pts[i] = new ImPoint();
	      GeomOper geomOp = new GeomOper();
	      //line_repf linex,liney;
	      ImLine linex,liney;
	      linex = new ImLine();
	      liney = new ImLine();

	      SubAreaFloat area = box.ar.copySubAreaFloat();

	      float val,val1,alpha;
	      BlobObject pboxDes;// = new BlobObject();
	      pboxDes = null;
	      pboxDes = BoundBoxStatistics(pLabels,bin_value);
	      if(pboxDes == null){
	      	logger.debug("Error at BoundSparseRotBox:check code in BoundBoxStatistics (Blob.java)" );
	        return(area);
	      }
	      alpha = (float)Math.atan( pboxDes.getSlope() );// in radians
	      // if the angle is too small (less than 2degrees) then leave it
	      //val = Math.abs(alpha*Rad2Deg);// alpha is between -90 and 90 degrees
	      //if(val<2.0 || (90-val)<2.0 ){
	      //  delete pboxDes;
	      //  return(area);
	      //}
	      //test
	      //cout<<"Test: estimated angle of rotation="<<alpha*Rad2Deg<<endl;
	      //cout<<"Test: estimated mean row="<< pboxDes[0].meanRow<< " col=";
	      //System.out.println(  pboxDes[0].meanCol <<endl;

	      // this approximates the actual area covering all features
	      //set the axes
	      linex.getPts1().x = pboxDes.getMeanRow();//.meanRow;
	      linex.getPts1().y = pboxDes.getMeanCol();//.meanCol;
	      linex.setSlope(pboxDes.getSlope());//.slope;
	      liney.setPts1(linex.getPts1());
	      val = 20.0F;// represents the increment along x axis
	      if(Math.abs(linex.getSlope())!=_lim.SLOPE_MAX){
	        linex.setQ(linex.getPts1().y - linex.getSlope()*linex.getPts1().x);
	        linex.getPts2().x = linex.getPts1().x+val;
	        linex.getPts2().y = linex.getSlope()*linex.getPts2().x+ linex.getQ();

	        if(Math.abs(linex.getSlope())>0.0002){ //1/5000 = 0.0002
	          liney.setSlope(-1.0/linex.getSlope());
	          liney.setQ(liney.getPts1().y - liney.getSlope()*liney.getPts1().x);
	          liney.getPts2().x = liney.getPts1().x + val;
	          liney.getPts2().y = liney.getSlope()*liney.getPts2().x+ liney.getQ();
	        }else{
	          liney.setSlope(_lim.SLOPE_MAX);
	          liney.setQ(liney.getPts1().x);
	          liney.getPts2().x = liney.getPts1().x;
	          liney.getPts2().y = liney.getPts1().y+ val;
	        }
	      }else{
	        linex.setQ(linex.getPts1().x);
	        linex.getPts2().x = linex.getPts1().x;
	        linex.getPts2().y = linex.getPts1().y+ val;

	        liney.setSlope(0.0);
	        liney.setQ(liney.getPts1().y);
	        liney.getPts2().x = liney.getPts1().x +val;
	        liney.getPts2().y = liney.getPts1().y;
	      }

	      int index;
	      ImPoint ptsc = new ImPoint();
	      int j;
	      double distx,disty;
	      long sumRow,sumArea,maxRow=0;
	      sumRow = sumArea = 0;
	      int minCol, maxCol;
	      /*
	      float sumx,sumy,sumx2,sumy2;
	      unsigned long sumTotal=0;
	      sumx = sumy = sumx2 = sumy2 = 0.0;
	      */
	      float sumxL,sumyL,sumx2L,sumy2L;
	      float sumxR,sumyR,sumx2R,sumy2R;
	      long sumTotalxL, sumTotalyL, sumTotalxR, sumTotalyR;
	      sumxL = sumyL = sumx2L = sumy2L = 0.0F;
	      sumxR = sumyR = sumx2R = sumy2R = 0.0F;
	      sumTotalxL = sumTotalyL = sumTotalxR = sumTotalyR = 0;

	      float maxDistx,maxDisty;
	      maxDistx = maxDisty = 0.0F;

	      //test diplay
	      //line_repf line;
	      //init
	      //line.pts1.x = line.pts2.x = -1.0;
	      //line.pts1.y = line.pts2.y = -1.0;

	      index = (int)box.getArea().getRow()*numcols + (int)box.getArea().getCol();
	      for(i= (int)box.getArea().getRow();i<box.getArea().getRow()+box.getArea().getHeight();i++){
	        minCol = (int)(box.ar.getCol()+box.ar.getWidth());
	        maxCol = (int)box.ar.getCol()-1;
	        for(j=(int)box.ar.getCol();j<box.ar.getCol()+box.ar.getWidth();j++){
	         if(pLabels.getInt(index) != bin_value.getInt(0) ){
	           //compute elongation along rotated axes
	           //statistical approach
	           pts[0].x = i;
	           pts[0].y = j;
	           distx = geomOp.distLineToPoint(linex,pts[0]);
	           disty = geomOp.distLineToPoint(liney,pts[0]);
	           // if "left" half space or "right" half space
	           // if slope = 0 then closer to x is left space
	           // if slope = inf then closer to y is left space
	           // else point closer to x is left space
	           // linex axis
	           if( Math.abs(linex.getSlope())==_lim.SLOPE_MAX){
	             if(pts[0].x - linex.getQ() < 0 ){
	               sumxL += distx;
	               sumx2L += distx*distx;
	               sumTotalxL++;
	             }else{
	               sumxR += distx;
	               sumx2R += distx*distx;
	               sumTotalxR++;
	             }
	           }else{
	             if(Math.abs(linex.getSlope())>_lim.EPSILON){
	               if( linex.getSlope()*pts[0].x - pts[0].y + linex.getQ() > 0 ){
	                 // the point is closer to the x-axis
	                 sumxL += distx;
	                 sumx2L += distx*distx;
	                 sumTotalxL++;
	               }else{
	                 sumxR += distx;
	                 sumx2R += distx*distx;
	                 sumTotalxR++;
	               }
	             }else{
	               if ( linex.getQ() - pts[0].y < 0 ){
	                 // the point is closer to the x-axis
	                 sumxL += distx;
	                 sumx2L += distx*distx;
	                 sumTotalxL++;
	               }else{
	                 sumxR += distx;
	                 sumx2R += distx*distx;
	                 sumTotalxR++;
	               }

	             }
	           }
	           // liney axis
	           if( Math.abs(liney.getSlope())==_lim.SLOPE_MAX){
	             if(pts[0].x - liney.getQ() < 0 ){
	               sumyL += disty;
	               sumy2L += disty*disty;
	               sumTotalyL++;
	             }else{
	               sumyR += disty;
	               sumy2R += disty*disty;
	               sumTotalyR++;
	             }
	           }else{
	             if(Math.abs(liney.getSlope())>_lim.EPSILON){
	               if( liney.getSlope()*pts[0].x - pts[0].y + liney.getQ() > 0 ){
	                 // the point is closer to the x-axis
	                 sumyL += disty;
	                 sumy2L += disty*disty;
	                 sumTotalyL++;
	               }else{
	                 sumyR += disty;
	                 sumy2R += disty*disty;
	                 sumTotalyR++;
	               }
	             }else{
	               if ( liney.getQ() - pts[0].y < 0 ){
	                 // the point is closer to the x-axis
	                 sumyL += disty;
	                 sumy2L += disty*disty;
	                 sumTotalyL++;
	               }else{
	                 sumyR += disty;
	                 sumy2R += disty*disty;
	                 sumTotalyR++;
	               }
	             }
	           }
	           /*
	           sumx += distx;
	           sumy += disty;
	           sumx2 += distx*distx;
	           sumy2 += disty*disty;
	           sumTotal++;
	           */
	           //find max dist points
	           // max distance approach for bounding box
	           if(maxDistx < distx)
	             maxDistx = (float)distx;
	           if(maxDisty < disty)
	             maxDisty = (float)disty;

	           // find area enclosed by the end points of blobs
	           if(minCol > j)
	             minCol = j;
	           if(maxCol < j)
	             maxCol = j;
	         }
	         index++;
	        }
	        index=index - (int)box.ar.getWidth() + numcols;
	        if(minCol != (box.ar.getCol()+box.ar.getWidth()) && maxCol != (box.ar.getCol()-1.0)){
	          // at least one blob pixel was found in the row
	          // update sumRow otherwise take the previous sumRow
	          sumRow = maxCol - minCol +1;
	          /*
	          //test
	          line.pts1.x = line.pts2.x = i;
	          line.pts1.y = minCol;
	          line.pts2.y = maxCol+1.0;
	          drawOp.plot_line(pLabels,&line,120);
	          */
	          if(sumRow> maxRow)
	            maxRow = sumRow;
	        }
	        /*
	        //test
	        if(line.pts1.x!=-1.0){
	          line.pts1.x = line.pts2.x = i;
	          drawOp.plot_line(pLabels,&line,120);
	        }
	        */
	        sumArea+=sumRow;
	      }
	      logger.info("Estimated area that covers of all features="+sumArea);
	      logger.info("Estimated max area that covers of all features="+(maxRow*box.ar.getHeight()) );
	      logger.info("Max dist along new axes: distx="+maxDistx+" disty="+maxDisty);

	      //this can happen if the distribution is assymetric ?
	      if(sumTotalxL <= 0){
	        sumxL = 0.0F;
	        sumx2L = 0.0F;
	      }else{
	        sumxL = sumxL/(float)sumTotalxL;
	        sumx2L = sumx2L/(float)sumTotalxL - sumxL*sumxL;
	      }
	      if(sumTotalxR <= 0){
	        sumxR = 0.0F;
	        sumx2R = 0.0F;
	      }else{
	        sumxR = sumxR/(float)sumTotalxR;
	        sumx2R = sumx2R/(float)sumTotalxR - sumxR*sumxR;
	      }
	      if(sumTotalyL <= 0){
	        sumyL = 0.0F;
	        sumy2L = 0.0F;
	      }else{
	        sumyL = sumyL/(float)sumTotalyL;
	        sumy2L = sumy2L/(float)sumTotalyL - sumyL*sumyL;
	      }
	      if(sumTotalyR <= 0){
	        sumyR = 0.0F;
	        sumy2R = 0.0F;
	      }else{
	        sumyR = sumyR/(float)sumTotalyR;
	        sumy2R = sumy2R/(float)sumTotalyR - sumyR*sumyR;
	      }


	      /*
	      //this should never happen
	      if(sumTotal <= 0){
	        System.out.println( " Error: no valid pixel to process " );
	        delete pboxDes;
	        return(area);
	      }
	      sumx = sumx/(float)sumTotal;
	      sumy = sumy/(float)sumTotal;
	      sumx2 = sumx2/(float)sumTotal - sumx*sumx;
	      sumy2 = sumy2/(float)sumTotal - sumy*sumy;
	      */

	      /* experiments
	      if(sumx2>=0.0)
	        val1 = 2.0*Math.sqrt(sumx2);
	      else{
	        System.out.println( "Error: check BoundSparseRotBox" );
	        val1 = 2.0;
	      }
	      if(sumy2>=0.0)
	        val = 2.0*Math.sqrt(sumy2);
	      else{
	        System.out.println( "Error: check BoundSparseRotBox" );
	        val = 2.0;
	      }
	      */

	      // this is the fix
	      double multStDev = 1.75;
	      if(sumx2L>=0.0 && sumx2R>=0.0)
	        val1 = (float) (2.0*(sumxL + multStDev* Math.sqrt(sumx2L) + sumxR + multStDev* Math.sqrt(sumx2R)) );
	      else{
	      	logger.debug( "Error: check BoundSparseRotBox" );
	        val1 = 2.0F*(sumxL + sumxR);
	      }
	      if(sumy2L>=0.0 && sumy2R>=0.0)
	        val = (float) (2.0F*(sumyL + multStDev* Math.sqrt(sumy2L) + sumyR + multStDev* Math.sqrt(sumy2R)) );
	      else{
	      	logger.debug( "Error: check BoundSparseRotBox" );
	        val = 2.0F*(sumyL + sumyR);
	      }
	      // test
	      logger.info( "Test: X-axis:left dist=" + (sumxL + multStDev* Math.sqrt(sumx2L)) );
	      logger.info( "Test: Y-axis:left dist=" + (sumyL + multStDev*Math.sqrt(sumy2L)) );
	      logger.info( "Test: X-axis:right dist=" + (sumxR + multStDev*Math.sqrt(sumx2R)) );
	      logger.info( "Test: Y-axis:right dist=" + (sumyR + multStDev*Math.sqrt(sumy2R)) );

	      /*
	      // this is the fix
	      if(sumx2>=0.0)
	        val1 = 2.0*(sumx+2.5*Math.sqrt(sumx2));
	      else{
	        System.out.println( "Error: check BoundSparseRotBox" );
	        val1 = 2.0*sumx;
	      }
	      if(sumy2>=0.0)
	        val = 2.0*(sumy+2.5*Math.sqrt(sumy2));
	      else{
	        System.out.println( "Error: check BoundSparseRotBox" );
	        val = 2.0*sumy;
	      }
	      */
	      // choice 1:the high and wide values are 3 times stdev
	      //val *= 4.0;//4.0;
	      //val1 *= 4.0;//4.0;
	      // choice 2: stdevx*unit * stdevy*unit = area
	      //sumx = Math.sqrt( sumArea/(val1*val) );
	      // val *= sumx;
	      // val1 *= sumx;
	      // choice 3: stdevx*unit * stdevy*unit = maxRow*high = maxArea
	      //    sumx = Math.sqrt( maxRow*box.ar.High/(val1*val) );
	      //val *= sumx;
	      //val1 *= sumx;
	      // choice 4: maxDistx and maxDisty determine the size of a bounding box
	      //val = 2.0*maxDisty;
	      //val1 = 2.0*maxDistx;

	      logger.info( "NEW dimensions high="+ val+ " wide="+ val1 );

	      //    check if all corner points are inside
	      // if the corners are outside then decrease size
	      boolean signal = true;
	      int iter = 0;
	      double mag,ang;
	      while(signal && iter < 10){
	        signal = false;
	        ptsc.x = pboxDes.getMeanRow();
	        ptsc.y = pboxDes.getMeanCol();


	        // pts[0].x = pboxDes[0].meanRow - val*0.5;
	        //pts[0].y = pboxDes[0].meanCol - val1*0.5;

	        mag = Math.sqrt( (sumxL + multStDev*Math.sqrt(sumx2L))*(sumxL + multStDev*Math.sqrt(sumx2L)) + (sumyL + multStDev*Math.sqrt(sumy2L))*(sumyL + multStDev*Math.sqrt(sumy2L)) );
	        //mag = Math.sqrt( (val*val+ val1*val1)*0.25);


	        ang = Math.atan(val1/val);
	        pts[0].x = pboxDes.getMeanRow() - mag * Math.cos(alpha+ang);
	        pts[0].y = pboxDes.getMeanCol() - mag* Math.sin(alpha+ang);
	        //System.out.println( "Test: slope=" << pboxDes[0].slope << " mag=" << mag;
	        //System.out.println( "ang=" << ang*Rad2Deg << " alpha="<< alpha*Rad2Deg );
	        //System.out.println( "val1=" << val1 << " val=" << val );
	        //System.out.println( "pts[0]=" << pts[0].x << " , " << pts[0].y );



	        // geomOp.RotatePoints(ptsc, pts,1,alpha);
	        ptsc = pts[0];

	        pts[0].x =  ptsc.x + val;
	        pts[0].y =  ptsc.y ;
	        pts[1].x =  ptsc.x;
	        pts[1].y =  ptsc.y + val1;
	        pts[2].x =  ptsc.x + val;
	        pts[2].y =  ptsc.y + val1;
	        pts[3].x =  ptsc.x ;
	        pts[3].y =  ptsc.y ;

	        geomOp.rotatePoints(ptsc, pts ,3,alpha);

	        for(i = 0;i < 4 && !signal; i++){
	          if(pts[i].x > numrows || pts[i].y > numcols || pts[i].x<0.0 || pts[i].y<0.0){
	            //System.out.println( "Warning:the bounding box does not fit inside the image" );
	            signal = true;
	            val -= 0.01*val;
	            val1 -= 0.01*val1;
	          }
	        }
	        iter++;
	      }
	      //final assignment of the bounding box
	      area.setRow((float)ptsc.x);
	      area.setCol((float)ptsc.y);
	      area.setHeight(val);
	      area.setWidth(val1);
	      area.setAngle((float)(alpha* _lim.Rad2Deg));
	      pboxDes = null;
	      //test perpendicular axes -- remove !!!!!
	      //    DrawOper drawOp;
	      //drawOp.plot_line(pLabels,&linex,120);
	      //drawOp.plot_line(pLabels,&liney,120);

	      return(area);
	    }else{
	    	logger.debug( "Error: no valid label image " );
	      return null;
	    }
	  }

	  ////////////////////////////////////////////////////////////
	  // Compute Bounding Boxes for all segments labeled
	  // in pImLabels
	  ////////////////////////////////////////////////////////////
	  //int Blob::BoundSegmBox(BlobD *b,tMatrix<unsigned long> *pLabels,long numlabels)
	  public boolean blobBoundBox(ImageObject pLabels,int numlabels)  {

	    if( !pLabels.getTypeString().equalsIgnoreCase("INT") ){
	    	logger.debug( "Error: image input type other than INT is not supported" );
	      return false;
	    }

	    int numrows,numcols;
	    numrows = pLabels.getNumRows();
	    numcols = pLabels.getNumCols();

	    int i,j;
	    float val;
	    // calculates the bounding box for a labeled region

	    if(numlabels > 0  &&  numcols > 0 && numrows > 0){
	     // find descriptor of blob (perimeter, mini,minj,maxi,maxj)
	     boolean signal = true;
	     int index,labval;
	     //used if _debugBlob=1 to show boxes
	     //SubAreaFloat area = null;//new SubAreaFloat();//null;
	     ImPoint ptsc = new ImPoint();//null;
	     //double color = 120.0;

	     //float x,y;
	     //ImPoint pts,ptsc;
	     //   double angle;
	     //DrawOper drawOp = new DrawOper();
	     GeomOper geomOp = new GeomOper();

	     // store the number of labels
	     _NumBoxLabels = numlabels;

	     // alocate memory
	     if(_pBoundBox != null)
	        _pBoundBox = null;
	     _pBoundBox  = new BlobBound[numlabels+1]; // bounding boxes
	     for(i=0;i<numlabels+1;i++)
	       _pBoundBox[i] = new BlobBound();


	     // initialize variables
	     for(index=1;index<numlabels+1;index++){
	       //memset( &b->pBoundBox[index], 0, sizeof(BlobBound) );
	       _pBoundBox[index].ResetBlobBound();
	       _pBoundBox[index].getMiniMinj().x  = numrows;
	       _pBoundBox[index].getMiniMinj().y  = numcols;
	       _pBoundBox[index].getMaxiMaxj().x  = -1;;
	       _pBoundBox[index].getMaxiMaxj().y  = -1;
	       _pBoundBox[index].getMinjMaxi().x  = -1;
	       _pBoundBox[index].getMinjMaxi().y  = numcols;
	       _pBoundBox[index].getMaxjMini().x  = numrows;
	       _pBoundBox[index].getMaxjMini().y  = -1;
	     }

	     index = 0;
	     for(i=0;i<numrows;i++){
	       for(j=0;j<numcols;j++){
	         labval = pLabels.getInt(index);
	         // find bounding box
	         // mini (and minj)
	         if( _pBoundBox[ labval ].getMiniMinj().x  >= i){
	           signal = false;
	           if( (int) _pBoundBox[ labval ].getMiniMinj().x ==  i){
	              if( _pBoundBox[ labval ].getMiniMinj().y > j)
	                signal = true;
	            }else
	              signal = true;
	            if(signal){
	              _pBoundBox[ labval ].getMiniMinj().x = i;
	              _pBoundBox[ labval ].getMiniMinj().y = j;
	            }
	          }
	          // maxi (and maxj)
	          if( _pBoundBox[ labval ].getMaxiMaxj().x  <= i){
	            signal = false;
	            if( (int) _pBoundBox[ labval ].getMaxiMaxj().x ==  i){
	              if( _pBoundBox[ labval ].getMaxiMaxj().y < j)
	                signal = true;
	            }else
	              signal = true;
	            if(signal){
	              _pBoundBox[ labval ].getMaxiMaxj().x = i;
	              _pBoundBox[ labval ].getMaxiMaxj().y = j;
	            }
	          }
	          // minj (and maxi)
	          if( _pBoundBox[ labval ].getMinjMaxi().y  >= j){
	            signal = false;
	            if( (int) _pBoundBox[ labval ].getMinjMaxi().y ==  j){
	              if( _pBoundBox[ labval ].getMinjMaxi().x < i)
	                signal = true;
	            }else
	              signal = true;
	            if(signal){
	              _pBoundBox[ labval ].getMinjMaxi().x = i;
	              _pBoundBox[ labval ].getMinjMaxi().y = j;
	            }
	          }
	          // maxj (and mini)
	          if( _pBoundBox[ labval ].getMaxjMini().y  <= j){
	            signal = false;
	            if( (int) _pBoundBox[ labval ].getMaxjMini().y ==  j){
	              if( _pBoundBox[ labval ].getMaxjMini().x > i)
	                signal = true;
	            }else
	              signal = true;
	            if(signal){
	              _pBoundBox[ labval ].getMaxjMini().x = i;
	              _pBoundBox[ labval ].getMaxjMini().y = j;
	            }
	          }
	          index++;
	       }
	     }

	     ImLine Line = new ImLine();
	     // if debug then test output to pLabels !!!
	     // Cannot compute Statistics if pLabels  has been modified !!

	     // Now compute perimeter and de-rotate and de-warp the bounding box
	     for(labval=1;labval<numlabels+1;labval++){
	        // check if the min and max values were found
	        if(  _pBoundBox[ labval ].getMiniMinj().x == numrows ||  _pBoundBox[ labval ].getMaxiMaxj().x < 0 ||  _pBoundBox[ labval ].getMinjMaxi().y == numcols ||  _pBoundBox[ labval ].getMaxjMini().y < 0){
	        	logger.debug( "Error: could not find bounding box\n");
	          return  false;
	        }

	        if(GetBoundBoxType().equalsIgnoreCase("Minimal") ){
	          // upper horiz.
	          Line.getPts1().x =  _pBoundBox[ labval ].getMiniMinj().x;
	          Line.getPts1().y =  _pBoundBox[ labval ].getMiniMinj().y;
	          Line.getPts2().x =  _pBoundBox[ labval ].getMaxjMini().x;
	          Line.getPts2().y =  _pBoundBox[ labval ].getMaxjMini().y;
	          _pBoundBox[ labval ].ar.setRow((float)Line.getPts1().x);
	          _pBoundBox[ labval ].ar.setCol((float)Line.getPts1().y);
	          _pBoundBox[ labval ].ar.setWidth((float)geomOp.distance(Line.getPts1(),Line.getPts2()));
	          //if(GetDebug() && _pBoundBox[ labval ].ar.Wide > _lim.EPSILON3 )
	          //  drawOp.plot_lineDouble(pLabels,Line,color);

	          // lower horiz.
	          Line.getPts1().x =  _pBoundBox[ labval ].getMaxiMaxj().x;
	          Line.getPts1().y =  _pBoundBox[ labval ].getMaxiMaxj().y;
	          Line.getPts2().x =  _pBoundBox[ labval ].getMinjMaxi().x;
	          Line.getPts2().y =  _pBoundBox[ labval ].getMinjMaxi().y;
	          //check if width is maximal
	          val = (float)geomOp.distance(Line.getPts1(),Line.getPts2());
	          if(_pBoundBox[ labval ].ar.getWidth() <= _lim.EPSILON3 )
	            _pBoundBox[ labval ].ar.setWidth(val);
	          //if( GetDebug() && val > _lim.EPSILON3)
	          //  drawOp.plot_lineDouble(pLabels,Line,color);
	          // left vert.
	          Line.getPts1().x =  _pBoundBox[ labval ].getMiniMinj().x;
	          Line.getPts1().y =  _pBoundBox[ labval ].getMiniMinj().y;
	          Line.getPts2().x =  _pBoundBox[ labval ].getMinjMaxi().x;
	          Line.getPts2().y =  _pBoundBox[ labval ].getMinjMaxi().y;
	          _pBoundBox[ labval ].ar.setHeight((float)geomOp.distance(Line.getPts1(),Line.getPts2()));

	          if( _pBoundBox[ labval ].ar.getHeight() > _lim.EPSILON3){
	            if( _pBoundBox[ labval ].ar.getHeight() < _pBoundBox[ labval ].ar.getWidth() ){
	              // the stability of angle is better if it is estimated from a longer edge
	              Line.getPts1().x =  _pBoundBox[ labval ].getMiniMinj().x;
	              Line.getPts1().y =  _pBoundBox[ labval ].getMiniMinj().y;
	              Line.getPts2().x =  _pBoundBox[ labval ].getMaxjMini().x;
	              Line.getPts2().y =  _pBoundBox[ labval ].getMaxjMini().y;

	              // the angle represents upper horiz line rotated around left upper corner
	              _pBoundBox[ labval ].ar.setAngle((float)(Math.atan2((double) ( Line.getPts2().y - Line.getPts1().y), (double) ( Line.getPts2().x - Line.getPts1().x) ) ));
	              _pBoundBox[labval].ar.setAngle((float) (_pBoundBox[labval].ar.getAngle() * 180/_lim.PI  - 90.0));
	            }else{
	              // the angle represents upper horiz line rotated around left upper corner
	              _pBoundBox[ labval ].ar.setAngle((float)Math.atan2((double) ( Line.getPts2().y - Line.getPts1().y), (double) ( Line.getPts2().x - Line.getPts1().x) ));
	              _pBoundBox[labval].ar.setAngle((float) (_pBoundBox[labval].ar.getAngle() * 180/_lim.PI ));
	            }
	            if(Math.abs( _pBoundBox[labval].ar.getAngle() )  <=  0.5 )
	              _pBoundBox[ labval ].ar.setAngle(0.0F);

	            if(_pBoundBox[labval].ar.getAngle() < 0)
	              _pBoundBox[labval].ar.setAngle(360 + _pBoundBox[labval].ar.getAngle());

	          }else{
	            _pBoundBox[ labval ].ar.setAngle(0.0F);
	          }
	          //if(GetDebug() && _pBoundBox[ labval ].ar.High > _lim.EPSILON3 )
	          //  drawOp.plot_lineDouble(pLabels,Line,color);

	          // right vert.
	          Line.getPts1().x =  _pBoundBox[ labval ].getMaxiMaxj().x;
	          Line.getPts1().y =  _pBoundBox[ labval ].getMaxiMaxj().y;
	          Line.getPts2().x =  _pBoundBox[ labval ].getMaxjMini().x;
	          Line.getPts2().y =  _pBoundBox[ labval ].getMaxjMini().y;
	          //check if height is maximal
	          val = (float)geomOp.distance(Line.getPts1(),Line.getPts2());
	          if( _pBoundBox[ labval ].ar.getHeight() <= _lim.EPSILON3 )
	            _pBoundBox[ labval ].ar.setHeight(val);
	          //if( GetDebug() && val > _lim.EPSILON3 )
	          //  drawOp.plot_lineDouble(pLabels,Line,color);

	          _pBoundBox[ labval ].SetPerim ( (int) (2*( _pBoundBox[ labval ].getMaxiMaxj().x -_pBoundBox[ labval ].getMiniMinj().x +  _pBoundBox[ labval ].getMaxjMini().y -_pBoundBox[ labval ].getMinjMaxi().y))  );
	        }
	        //approximate the blob with a rectangular box
	        if(getBoundBoxType().equalsIgnoreCase("Rectangular") ){
	          // find the most upper left point
	          if(_pBoundBox[ labval ].getMiniMinj().x < _pBoundBox[ labval ].getMaxjMini().x)
	            _pBoundBox[ labval ].ar.setRow( (float)_pBoundBox[ labval ].getMiniMinj().x);
	          else
	            _pBoundBox[ labval ].ar.setRow( (float)_pBoundBox[ labval ].getMaxjMini().x);
	          if(_pBoundBox[ labval ].getMiniMinj().y < _pBoundBox[ labval ].getMinjMaxi().y)
	            _pBoundBox[ labval ].ar.setCol( (float)_pBoundBox[ labval ].getMiniMinj().y);
	          else
	            _pBoundBox[ labval ].ar.setCol( (float)_pBoundBox[ labval ].getMinjMaxi().y);
	          // find the most lower right point
	          if(_pBoundBox[ labval ].getMaxiMaxj().x > _pBoundBox[ labval ].getMinjMaxi().x)
	            ptsc.x   =  _pBoundBox[ labval ].getMaxiMaxj().x;
	          else
	            ptsc.x   =  _pBoundBox[ labval ].getMinjMaxi().x;
	          if(_pBoundBox[ labval ].getMaxiMaxj().y > _pBoundBox[ labval ].getMaxjMini().y)
	            ptsc.y   =  _pBoundBox[ labval ].getMaxiMaxj().y;
	          else
	            ptsc.y   =  _pBoundBox[ labval ].getMaxjMini().y;


	          _pBoundBox[ labval ].ar.setHeight( (float)ptsc.x - _pBoundBox[ labval ].ar.getRow() +1.0F);
	          if(_pBoundBox[ labval ].ar.getHeight() == 0.0F)
	            _pBoundBox[ labval ].ar.setHeight(_pBoundBox[ labval ].ar.getHeight() +1.0F);
	          _pBoundBox[ labval ].ar.setWidth((float)ptsc.y - _pBoundBox[ labval ].ar.getCol() +1.0F);
	          if(_pBoundBox[ labval ].ar.getWidth() == 0.0)
	            _pBoundBox[ labval ].ar.setWidth(_pBoundBox[ labval ].ar.getWidth() +1.0F);
	          if(_pBoundBox[ labval ].ar.getHeight() >=numrows)
	            _pBoundBox[ labval ].ar.setHeight(numrows -1.0F);
	          if(_pBoundBox[ labval ].ar.getWidth() >= numcols)
	            _pBoundBox[ labval ].ar.setWidth(numcols - 1.0F);
	          _pBoundBox[ labval ].ar.setAngle(0.0F);
	          ptsc.x = _pBoundBox[ labval ].ar.getRow();
	          ptsc.y = _pBoundBox[ labval ].ar.getCol();
	          //color = 200.0;
	          //_pBoundBox[ labval ].ar = area;
	          _pBoundBox[ labval ].SetPerim( (int)( 2*( _pBoundBox[ labval ].ar.getHeight() + _pBoundBox[ labval ].ar.getWidth()) ) );

	          //if( GetDebug())
	          //  drawOp.draw_boxDouble(pLabels,area,ptsc,color);
	        }


	        /*
	        // blob in the original (de-rotated and de-warped) coordinate system
	        if(Math.abs(area.Curve) <=0.5){
	          _pBoundBox[ labval ].h = _pBoundBox[ labval ].GetMaxiMaxj().x - _pBoundBox[ labval ].GetMiniMinj().x;
	          _pBoundBox[ labval ].w =  _pBoundBox[ labval ].GetMaxjMini().y - _pBoundBox[ labval ].GetMinjMaxi().y;
	          _pBoundBox[ labval ].GetMiniMinj().y =  _pBoundBox[ labval ].GetMinjMaxi().y;
	          _pBoundBox[ labval ].GetMaxiMaxj().y =  _pBoundBox[ labval ].GetMaxjMini().y - _pBoundBox[ labval ].w;
	          if(Math.abs(area.Angle) >= 0.5){
	            ptsc.x = area.Row+ area.High*0.5;
	            ptsc.y = area.Col+ area.Wide*0.5;
	            angle = area.Angle*PI/180;

	            pts.x =  _pBoundBox[ labval ].GetMiniMinj().x;
	            pts.y =  _pBoundBox[ labval ].GetMiniMinj().y;
	            geomOp.RotatePoint(ptsc,&pts,angle);
	            _pBoundBox[ labval ].GetMiniMinj().x = pts.x;
	            _pBoundBox[ labval ].GetMiniMinj().y = pts.y;

	            pts.x =  _pBoundBox[ labval ].GetMaxiMaxj().x;
	            pts.y =  _pBoundBox[ labval ].GetMaxiMaxj().y;
	            geomOp.RotatePoint(ptsc,&pts,angle);
	            _pBoundBox[ labval ].GetMaxiMaxj().x = pts.x;
	            _pBoundBox[ labval ].GetMaxiMaxj().y = pts.y;
	          }
	          // mini && minj point
	          _pBoundBox[labval].r = _pBoundBox[ labval ].GetMiniMinj().x;
	          _pBoundBox[labval].c = _pBoundBox[ labval ].GetMiniMinj().y;
	          _pBoundBox[labval].a = atan2((double) ( _pBoundBox[ labval ].GetMaxiMaxj().y - _pBoundBox[labval].c), (double) ( _pBoundBox[ labval ].GetMaxiMaxj().x - _pBoundBox[ labval].r) );
	          _pBoundBox[labval].a = _pBoundBox[labval].a * 180/PI;
	          if(_pBoundBox[labval].a < 0)
	            _pBoundBox[labval].a = 360 + _pBoundBox[labval].a;
	        }else{
	          //TODO for curved extracted subimages
	        }
	        */
	     }

	    }else{
	    	logger.debug( "Error: no segments in BoundSegmBox\n");
	      return false;
	    }
	    return true;
	  }

	  //////////////////////////////////////////////////////////
	  // Computes basic parameters of all blobs labeled in pImLabels
	  //////////////////////////////////////////////////////////
	  //int Blob::BlobStatistics(BlobD *b,tMatrix<unsigned long> *pLabels,long numlabels)
	  public boolean blobStatistics(ImageObject pLabels,int numlabels)  {

	    // sanity check
	    if(numlabels <=0){
	    	logger.debug("ERROR: num of labels <=0 ");
	      return false;
	    }
	    if(pLabels == null){
	    	logger.debug("ERROR: no input label image ");
	      return false;
	    }
	    if( !pLabels.getTypeString().equalsIgnoreCase("BYTE") && !pLabels.getTypeString().equalsIgnoreCase("INT")){
	    	logger.debug("ERROR: label image type other than BYTE and INT is not supported ");
	      return false;
	    }


	    int numrows,numcols;
	    numrows = pLabels.getNumRows();
	    numcols = pLabels.getNumCols();

	    float area;
	    double tempval, val;
	    int index, labval;
	    int i,j;

	    // check if any segment has been created
	    if(numlabels > 0 && numcols > 0 && numrows > 0 ){
	      _NumStatsLabels = numlabels;

	      if(_pBlobDes != null)
	        _pBlobDes = null;
	      _pBlobDes  = new BlobObject[numlabels+1]; // segment statistics
	      for(i=0;i<numlabels+1;i++)
	        _pBlobDes[i] = new BlobObject();

	      // initialize variables
	      for(index=1;index<numlabels+1;index++){
	        _pBlobDes[index].resetBlobObject();
	      }

	      // compute sums
	     if(pLabels.getTypeString().equalsIgnoreCase("BYTE")){
	       index = 0;
	       for(i=0;i<numrows;i++){
	         for(j=0;j<numcols;j++){
	           // record sums for computing blob statistics
	           if(pLabels.getInt(index) < 0 )
	              labval =  _lim.MAXPOS_BYTE + pLabels.getInt(index);
	            else
	              labval = pLabels.getInt(index);

	           if(labval < 1 || labval > numlabels+1){
	           	logger.info( " Warning: out of bounds segment label in SegmStatistics\n");
	             continue;
	           }
	           _pBlobDes[ labval ].size ++;
	           _pBlobDes[ labval ].sumi +=i;
	           _pBlobDes[ labval ].sumj +=j;
	           _pBlobDes[ labval ].sumij +=i*j;
	           _pBlobDes[ labval ].sumi2 +=i*i;
	           _pBlobDes[ labval ].sumj2 +=j*j;
	           index++;
	         }
	       }
	     }
	     if(pLabels.getTypeString().equalsIgnoreCase("INT")){
	       index = 0;
	       for(i=0;i<numrows;i++){
	         for(j=0;j<numcols;j++){
	           // record sums for computing blob statistics
	           labval =  pLabels.getInt(index);
	           if(labval < 1 || labval > numlabels+1){
	           	logger.info( " Warning: out of bounds segment label in SegmStatistics\n");
	             continue;
	           }
	           _pBlobDes[ labval ].size ++;
	           _pBlobDes[ labval ].sumi +=i;
	           _pBlobDes[ labval ].sumj +=j;
	           _pBlobDes[ labval ].sumij +=i*j;
	           _pBlobDes[ labval ].sumi2 +=i*i;
	           _pBlobDes[ labval ].sumj2 +=j*j;
	           index++;
	         }
	       }
	     }

	     ////////////////////
	     // this is common and could be replaced by one routine !!!
	      for(index=1;index<numlabels+1;index++){
	        blobStats(_pBlobDes,index);
	/*
	        area = _pBlobDes[index].size;
	        if(area<=1){
	          System.out.println( "Error: segment size of label="+index+" is less than 2 in SegmStatistics\n");
	          continue;
	        }
	        //      area2=area*area ; //  blob.GetArea()ea*blob.GetArea()ea;
	        _pBlobDes[index].SetMeanRow(  _pBlobDes[index].sumi/area );
	        //   blo_meani=blo_sumi/blob.GetArea()ea;
	        _pBlobDes[index].SetMeanCol ( _pBlobDes[index].sumj/area );
	        //   blo_meanj=blo_sumj/blob.GetArea()ea;
	        //      _pBlobDes[index].var.x = _pBlobDes[index].sumi2 - _pBlobDes[index].meanRow * _pBlobDes[index].sumi;
	        _pBlobDes[index].SetVarRow(  (_pBlobDes[index].sumi2 - _pBlobDes[index].GetMeanRow() * _pBlobDes[index].sumi)/area );
	        //   blo_vari=blo_sumi2-blo_meani*blo_sumi;
	        //      _pBlobDes[index].var.y = _pBlobDes[index].sumj2 - _pBlobDes[index].meanCol * _pBlobDes[index].sumj;
	        _pBlobDes[index].SetVarCol(  (_pBlobDes[index].sumj2 - _pBlobDes[index].GetMeanCol() * _pBlobDes[index].sumj)/area );
	        //    blo_varj=blo_sumj2-blo_meanj*blo_sumj;
	        //      _pBlobDes[index].cvar = _pBlobDes[index].sumij - _pBlobDes[index].meanCol * _pBlobDes[index].sumi;
	        _pBlobDes[index].SetCvar( (_pBlobDes[index].sumij - _pBlobDes[index].GetMeanCol() * _pBlobDes[index].sumi)/area );
	        //    blo_cvar=blo_sumij-blo_meanj*blo_sumi;
	        _pBlobDes[index].SetSpread ( (_pBlobDes[index].GetVarRow() + _pBlobDes[index].GetVarCol() )/area );
	        //    blo_sprd=(blo_vari+blo_varj)/area2;
	        tempval = _pBlobDes[index].GetVarRow() - _pBlobDes[index].GetVarCol();
	        val = tempval * tempval;
	        //_pBlobDes[index].SetElng ( tempval * tempval);
	        //    blo_elng*=(blo_elng=blo_vari-blo_varj);
	        tempval = _pBlobDes[index].GetCvar();
	        val += 4*tempval * tempval;
	        //_pBlobDes[index].elng += 4*tempval * tempval;
	        //    blo_elng+=4*blo_cvar*blo_cvar;
	        //_pBlobDes[index].elng /= area2*area2;
	        //_pBlobDes[index].elng /= area;
	        val /= area;
	        _pBlobDes[index].SetElng(val);
	        //    blo_elng/=area2*area2;

	        //correlation (linear dependency)
	        tempval =  _pBlobDes[index].GetVarRow() *  _pBlobDes[index].GetVarCol();
	        if(tempval > 0)
	           _pBlobDes[index].SetCor (  _pBlobDes[index].GetCvar()/Math.sqrt(tempval) );
	        else
	           _pBlobDes[index].SetCor ( 0.0);

	        // stat. orientation
	        // blob orientation based on variation and covariance
	        // it is not clear how to interpret the number !!
	        // sometimes the result is 45 degrees off the slope angle
	        tempval =  _pBlobDes[index].GetVarRow() -  _pBlobDes[index].GetVarCol();
	        val = Math.atan2((2.0*_pBlobDes[index].GetCvar() ),tempval);
	        _pBlobDes[index].SetOrien(val);

	        //if( _pBlobDes[index].orien > 0.0)
	        //  _pBlobDes[index].orien -= PI*0.25;
	        //else
	        //  _pBlobDes[index].orien += PI*0.25;


	        // blob slope based on variation and correlation
	        if( Math.abs(_pBlobDes[ index ].GetCor() )< 0.01){
	          if( _pBlobDes[ index ].GetVarRow() < _pBlobDes[ index ].GetVarCol() ){
	               //_pBlobDes[ index].slope = _lim.SLOPE_MAX; // row = const;
	               val = _lim.SLOPE_MAX;
	          }else{
	              //_pBlobDes[ index ]. slope = 0.0; // col = const;
	              val = 0.0;
	          }
	        }else{
	          if(_pBlobDes[ index ].GetVarRow() < 0.01){
	              //_pBlobDes[ index ].slope = _lim.SLOPE_MAX; // row = const;
	              val = _lim.SLOPE_MAX;
	          }else{
	               //_pBlobDes[ index].slope = _pBlobDes[ index ].GetCor()/Math.abs(_pBlobDes[ index ].GetCor());
	               val = _pBlobDes[ index ].GetCor()/Math.abs(_pBlobDes[ index ].GetCor());
	               //_pBlobDes[ index ].slope =_pBlobDes[ index ].slope * _pBlobDes[ index ].GetVarCol()/ _pBlobDes[ index ].GetVarRow() ;
	               val =val * _pBlobDes[ index ].GetVarCol()/ _pBlobDes[ index ].GetVarRow() ;
	          }
	        }
	         _pBlobDes[ index ].SetSlope(val);

	        //if( _pBlobDes[index].slope != 5000.0)
	       //   outfile << "\t Slope [Deg]=" << atan(_pBlobDes[index].slope)*Rad2Deg );
	       // else
	       //   outfile << "\t Slope [Deg]=90" );
	   */
	     }// end of for(index)

	     //outfile.close();

	    }else{
	    	logger.debug( " Error: no segments in SegmStatistics\n");
	       return false;
	    }
	    return true;
	  }
	  //////////////////////////////////////////////////////////
	  // Computes basic parameters of one blob labeled in pImLabels
	  // and returns one Blob Descriptor
	  //////////////////////////////////////////////////////////
	  public BlobObject blobStatistics(ImageObject pLabels, ImageObject label)  {
	/*
	    if(numlabels <=0){
	       System.out.println( "Warning: no label in BlobStatistics\n");
	       return null;
	    }
	    */
	    if(pLabels == null || label == null){
	    	logger.debug("ERROR: no input label image or label ");
	      return null;
	    }
	    if( !pLabels.getTypeString().equalsIgnoreCase("BYTE") && !pLabels.getTypeString().equalsIgnoreCase("INT")){
	    	logger.debug("ERROR: label image type other than BYTE and INT is not supported ");
	      return null;
	    }
	    if( !pLabels.getTypeString().equalsIgnoreCase(label.getTypeString()) ){
	    	logger.debug("ERROR: label type and image label are not consistent ");
	      return null;
	    }
	    /*
	    if(label < 1 || label > numlabels+1){
	       System.out.println( " Error: segment label is out of bounds in BlobStatistics\n");
	       return null;
	    }
	*/
	    int numrows,numcols;
	    numrows = pLabels.getNumRows();
	    numcols = pLabels.getNumCols();
	    // check if any segment has been created
	    if(numcols > 0 && numrows > 0 ){
	      float area;
	      double tempval,val;
	      int index;
	      int i,j;
	      BlobObject [] pBlobDes = null;
	      pBlobDes = new BlobObject[1];
	      pBlobDes[0] = new BlobObject();

	      // initialize variables
	      pBlobDes[0].resetBlobObject();

	      // compute sums
	     if( pLabels.getTypeString().equalsIgnoreCase("BYTE") ){
	       index = 0;
	       for(i=0;i<numrows;i++){
	         for(j=0;j<numcols;j++){
	           // record sums for computing blob statistics
	           if(pLabels.getDouble(index) == label.getDouble(0) ){
	             pBlobDes[0].size ++;
	             pBlobDes[0].sumi +=i;
	             pBlobDes[0].sumj +=j;
	             pBlobDes[0].sumij +=i*j;
	             pBlobDes[0].sumi2 +=i*i;
	             pBlobDes[0].sumj2 +=j*j;
	           }
	           index++;
	         }
	       }
	     }
	     if( pLabels.getTypeString().equalsIgnoreCase("INT") ){
	       index = 0;
	       for(i=0;i<numrows;i++){
	         for(j=0;j<numcols;j++){
	           // record sums for computing blob statistics
	           if(pLabels.getInt(index) == label.getInt(0) ){
	             pBlobDes[0].size ++;
	             pBlobDes[0].sumi +=i;
	             pBlobDes[0].sumj +=j;
	             pBlobDes[0].sumij +=i*j;
	             pBlobDes[0].sumi2 +=i*i;
	             pBlobDes[0].sumj2 +=j*j;
	           }
	           index++;
	         }
	       }
	     }

	     index=0;
	     blobStats(pBlobDes,index);

	  /*
	     index=0;
	     area= pBlobDes[index].size;
	     if(area<=1){
	        System.out.println( "Error: segment size of label="+index+" is less than 2 in SegmStatistics\n");
	        return(pBlobDes);
	     }
	     pBlobDes[index].SetMeanRow ( pBlobDes[index].sumi/area );
	     pBlobDes[index].SetMeanCol ( pBlobDes[index].sumj/area );
	     pBlobDes[index].SetVarRow( (pBlobDes[index].sumi2 - pBlobDes[index].GetMeanRow() *pBlobDes[index].sumi)/area );
	     pBlobDes[index].SetVarCol( (pBlobDes[index].sumj2 - pBlobDes[index].GetMeanCol() *pBlobDes[index].sumj)/area );
	     pBlobDes[index].SetCvar ( (pBlobDes[index].sumij - pBlobDes[index].GetMeanCol() *pBlobDes[index].sumi)/area );
	     pBlobDes[index].SetSpread( (pBlobDes[index].GetVarRow() + pBlobDes[index].GetVarCol() )/area );

	     tempval = pBlobDes[index].GetVarRow() - pBlobDes[index].GetVarCol();
	     val= tempval * tempval;
	     tempval = pBlobDes[index].GetCvar();
	     val += 4*tempval * tempval;
	     val /= area;
	     pBlobDes[index].SetElng(val);

	     //correlation (linear dependency)
	     tempval =  pBlobDes[index].GetVarRow() *  pBlobDes[index].GetVarCol();
	     if(tempval > 0)
	        pBlobDes[index].SetCor( pBlobDes[index].GetCvar()/Math.sqrt(tempval) );
	     else
	        pBlobDes[index].SetCor ( 0.0);

	     // stat. orientation
	     // blob orientation based on variation and covarianc
	     // it is not clear how to interpret the number !!
	     // sometimes the result is 45 degrees off the slope angle
	     tempval =  pBlobDes[index].GetVarRow() -  pBlobDes[index].GetVarCol();
	     pBlobDes[index].SetOrien(  Math.atan2((2.0*pBlobDes[index].GetCvar() ),tempval) );

	     // blob slope based on variation and correlation
	     if( Math.abs(pBlobDes[ index ].GetCor() )< 0.01){
	       if( pBlobDes[ index ].GetVarRow() < pBlobDes[ index ].GetVarCol() ){
	         //pBlobDes[ index].slope = _lim.SLOPE_MAX; // row = const;
	         val = _lim.SLOPE_MAX;
	       }else{
	         //pBlobDes[ index ]. slope = 0.0; // col = const;
	         val = 0.0;
	       }
	     }else{
	       if(pBlobDes[ index ].GetVarRow() < 0.01){
	         //pBlobDes[ index ].slope = _lim.SLOPE_MAX; // row = const;
	         val = _lim.SLOPE_MAX;
	       }else{
	         //pBlobDes[ index].slope = pBlobDes[ index ].cor/Math.abs(pBlobDes[ index ].cor);
	         val = pBlobDes[ index ].GetCor()/Math.abs(pBlobDes[ index ].GetCor() );
	         //pBlobDes[ index ].slope =pBlobDes[ index ].slope * pBlobDes[ index ].var.y/ pBlobDes[ index ].var.x;
	         val = val * pBlobDes[ index ].GetVarCol()/ pBlobDes[ index ].GetVarRow();
	       }
	     }
	     pBlobDes[ index ].SetSlope( val );
	*/
	     return( pBlobDes[0] );
	    }else{
	    	logger.debug( " Error: no label image in BlobStatistics\n");
	       return null;
	    }
	  }
	  //////////////////////////////////////////////////////////
	  // Computes basic parameters of all blobs but bin_value labeled in pImLabels
	  // and returns one Blob Descriptor
	  //////////////////////////////////////////////////////////
	  public BlobObject BoundBoxStatistics(ImageObject pLabels, ImageObject notlabel)  {
	    if(pLabels==null){
	    	logger.debug( " Error: no image in BoundBoxStatistics\n");
	       return null;
	    }
	    if(pLabels == null || notlabel == null){
	    	logger.debug("ERROR: no input label image or notlabel ");
	      return null;
	    }
	    if( !pLabels.getTypeString().equalsIgnoreCase("BYTE") && !pLabels.getTypeString().equalsIgnoreCase("INT")){
	    	logger.debug("ERROR: label image type other than BYTE and INT is not supported ");
	      return null;
	    }
	    if( !pLabels.getTypeString().equalsIgnoreCase(notlabel.getTypeString()) ){
	    	logger.debug("ERROR: notlabel type and image label are not consistent ");
	      return null;
	    }

	    int numrows,numcols;
	    numrows = pLabels.getNumRows();
	    numcols = pLabels.getNumCols();
	    // check if any segment has been created
	    if(numcols > 0 && numrows > 0 ){
	      int index;
	      int i,j;
	      BlobObject [] pBlobDes = null;
	      pBlobDes = new BlobObject[1];
	      pBlobDes[0] = new BlobObject();

	      // initialize variables
	      pBlobDes[0].resetBlobObject();
	      // compute sums
	     if( pLabels.getTypeString().equalsIgnoreCase("BYTE") ){
	       index = 0;
	       for(i=0;i<numrows;i++){
	         for(j=0;j<numcols;j++){
	           // record sums for computing blob statistics
	           if(pLabels.getDouble(index) != notlabel.getDouble(0)){
	             pBlobDes[0].size ++;
	             pBlobDes[0].sumi +=i;
	             pBlobDes[0].sumj +=j;
	             pBlobDes[0].sumij +=i*j;
	             pBlobDes[0].sumi2 +=i*i;
	             pBlobDes[0].sumj2 +=j*j;
	           }
	           index++;
	         }
	       }
	     }
	     if( pLabels.getTypeString().equalsIgnoreCase("INT") ){
	       index = 0;
	       for(i=0;i<numrows;i++){
	         for(j=0;j<numcols;j++){
	           // record sums for computing blob statistics
	           if(pLabels.getInt(index) != notlabel.getInt(0)){
	             pBlobDes[0].size ++;
	             pBlobDes[0].sumi +=i;
	             pBlobDes[0].sumj +=j;
	             pBlobDes[0].sumij +=i*j;
	             pBlobDes[0].sumi2 +=i*i;
	             pBlobDes[0].sumj2 +=j*j;
	           }
	           index++;
	         }
	       }
	     }

	     index=0;
	     blobStats(pBlobDes,index);
	     return( pBlobDes[0] );
	    }else{
	    	logger.debug( " Error: no label image in BoundBoxStatistics\n");
	       return null;
	    }
	  }
	  //////////////////////////////////////////////////////////
	  // Computes basic parameters
	  // and saves them in the BlobDescriptor
	  //////////////////////////////////////////////////////////
	  //int Blob::BlobStats(BlobDescriptor *pBlobDes,unsigned long index)
	  public boolean blobStats(BlobObject [] pBlobDes,int index)  {
	    if( pBlobDes == null || pBlobDes[index] == null){
	    	logger.debug( "Error: BlobObject has not been allocated or index is out of bounds");
	       return false;
	    }
	    int area;
	    area = pBlobDes[index].size;
	    if(area<=1){
	      if(_debugBlob)
	      	logger.debug( "Error: segment size of label="+index+" is less than 2 in SegmStatistics\n");
	       return false;
	     }
	    double val, tempval;

	     pBlobDes[index].setMeanRow ( pBlobDes[index].sumi/area );
	     pBlobDes[index].setMeanCol( pBlobDes[index].sumj/area );

	     pBlobDes[index].setVarRow( (pBlobDes[index].sumi2 - pBlobDes[index].getMeanRow() *pBlobDes[index].sumi)/area );
	     pBlobDes[index].setVarCol( (pBlobDes[index].sumj2 - pBlobDes[index].getMeanCol() *pBlobDes[index].sumj)/area );

	     pBlobDes[index].setSDevRow( Math.sqrt(pBlobDes[index].getVarRow() ) );
	     pBlobDes[index].setSDevCol( Math.sqrt(pBlobDes[index].getVarCol() ) );

	     pBlobDes[index].setCvar( (pBlobDes[index].sumij - pBlobDes[index].getMeanCol() *pBlobDes[index].sumi)/area );
	     pBlobDes[index].setSpread( (pBlobDes[index].getVarRow() + pBlobDes[index].getVarCol() )/area );

	     tempval = pBlobDes[index].getVarRow() - pBlobDes[index].getVarCol();
	     val = tempval * tempval;
	     tempval = pBlobDes[index].getCvar();
	     val += 4*tempval * tempval;
	     val /= area;
	     pBlobDes[index].setElng( val);

	     //correlation (linear dependency)
	     tempval =  pBlobDes[index].getVarRow() *  pBlobDes[index].getVarCol();
	     if(tempval > 0)
	        pBlobDes[index].setCor(  pBlobDes[index].getCvar() /Math.sqrt(tempval) );
	     else
	        pBlobDes[index].setCor( 0.0 );

	     // stat. orientation
	     // blob orientation based on variation and covarianc
	     // it is not clear how to interpret the number !!
	     // sometimes the result is 45 degrees off the slope angle
	     tempval =  pBlobDes[index].getVarRow() -  pBlobDes[index].getVarCol();
	     pBlobDes[index].setOrien( Math.atan2((2.0*pBlobDes[index].getCvar() ),tempval) );

	     // blob slope based on variation and correlation
	     if( Math.abs(pBlobDes[ index ].getCor() )< 0.01){
	       if( pBlobDes[ index ].getVarRow() < pBlobDes[ index ].getVarCol()){
	         //pBlobDes[ index].slope = _lim.SLOPE_MAX; // row = const;
	         val = _lim.SLOPE_MAX;
	       }else{
	         //pBlobDes[ index ]. slope = 0.0; // col = const;
	         val = 0.0;
	       }
	     }else{
	       if(pBlobDes[ index ].getVarRow() < 0.01){
	         //pBlobDes[ index ].slope = _lim.SLOPE_MAX; // row = const;
	         val = _lim.SLOPE_MAX;
	       }else{
	         //pBlobDes[ index].slope = pBlobDes[ index ].cor/Math.abs(pBlobDes[ index ].cor);
	         val = pBlobDes[ index ].getCor()/Math.abs(pBlobDes[ index ].getCor() );
	         //pBlobDes[ index ].slope =pBlobDes[ index ].slope * pBlobDes[ index ].var.y/ pBlobDes[ index ].var.x;
	         val = val * pBlobDes[ index ].getVarCol()/ pBlobDes[ index ].getVarRow();
	       }
	     }
	     pBlobDes[ index ].setSlope( val );
	     pBlobDes[ index ].setPerim( -1 );
	     return true;
	  }

	  ////////////////////////////////////////////////
	  public boolean printBlobStats(){
	      if(_pBlobDes == null){
	      	logger.debug( "Error: no blob descriptiors \n");
	        return false;
	      }
	      int index;
	      for(index=1;index<getNumStatsLabels()+1;index++){
	      	logger.info("idx="+index+", "+_pBlobDes[index].BlobStats2String());
	      }
	      return true;
	  }
	  public boolean printBlobBox(){
	      if(_pBoundBox == null){
	      	logger.debug( "Error: no blob descriptors \n");
	        return false;
	      }
	      int index;
	      for(index=1;index<getNumBoxLabels()+1;index++){
	      	logger.info("idx="+index+", "+_pBoundBox[ index ].blobBound2String());
	      }
	      return true;
	  }

	  /////////////////////
	  // show methods
	  public boolean showMinMaxBox(ImageObject im, double color){
	      int index;
	      boolean ret = true;
	      for(index=1;index<getNumBoxLabels()+1;index++){
	        ret = ret & showOneMinMaxBox(im,index, color);
	      }
	      return ret;
	  }
	  public boolean showOneMinMaxBox(ImageObject pLabels, int labval, double color){
	      if(pLabels == null){
	      	logger.debug( "Error: no input image \n");
	        return false;
	      }
	       //test
	      logger.debug("Test: before draw_boxDouble");
	      if(_pBoundBox == null){
	      	logger.debug( "Error: no blob descriptors \n");
	        return false;
	      }
	      if(labval < 1 || labval >= getNumBoxLabels()+1){
	      	logger.debug( "Error: index to blob is out of bounds \n");
	        return false;
	      }

	      ImLine Line = new ImLine();
	      DrawOp drawOp = new DrawOp();

	     // upper horiz
	      Line.getPts1().x =  _pBoundBox[ labval ].getMiniMinj().x;
	      Line.getPts1().y =  _pBoundBox[ labval ].getMiniMinj().y;
	      Line.getPts2().x =  _pBoundBox[ labval ].getMaxjMini().x;
	      Line.getPts2().y =  _pBoundBox[ labval ].getMaxjMini().y;
	      drawOp.plot_lineDouble(pLabels,Line,color);

	      // lower horiz.
	      Line.getPts1().x =  _pBoundBox[ labval ].getMaxiMaxj().x;
	      Line.getPts1().y =  _pBoundBox[ labval ].getMaxiMaxj().y;
	      Line.getPts2().x =  _pBoundBox[ labval ].getMinjMaxi().x;
	      Line.getPts2().y =  _pBoundBox[ labval ].getMinjMaxi().y;
	      drawOp.plot_lineDouble(pLabels,Line,color);
	      // left vert.
	      Line.getPts1().x =  _pBoundBox[ labval ].getMiniMinj().x;
	      Line.getPts1().y =  _pBoundBox[ labval ].getMiniMinj().y;
	      Line.getPts2().x =  _pBoundBox[ labval ].getMinjMaxi().x;
	      Line.getPts2().y =  _pBoundBox[ labval ].getMinjMaxi().y;
	      drawOp.plot_lineDouble(pLabels,Line,color);

	      // right vert.
	      Line.getPts1().x =  _pBoundBox[ labval ].getMaxiMaxj().x;
	      Line.getPts1().y =  _pBoundBox[ labval ].getMaxiMaxj().y;
	      Line.getPts2().x =  _pBoundBox[ labval ].getMaxjMini().x;
	      Line.getPts2().y =  _pBoundBox[ labval ].getMaxjMini().y;
	      drawOp.plot_lineDouble(pLabels,Line,color);

	      return true;
	  }
	  public boolean showAreaBox(ImageObject im, double color){
	      boolean ret = true;
	      int index;
	      for(index=1;index<getNumBoxLabels()+1;index++){
	        ret = ret & showOneAreaBox(im,index, color);
	      }
	      return ret;
	  }
	  public boolean showOneAreaBox(ImageObject pLabels, int labval, double color){
	    if(pLabels == null){
	    	logger.debug( "Error: no input image \n");
	      return false;
	    }
	     //test
	    //logger.debug("Test: before draw_boxDouble");
	    if(_pBoundBox == null  ){
	    	logger.debug( "Error: no blob descriptors \n");
	      return false;
	    }
	    if(labval < 1 || labval >= getNumBoxLabels()+1){
	    	logger.debug( "Error: index to blob is out of bounds \n");
	      return false;
	    }
	    if( _pBoundBox[labval].ar == null){
	    	logger.debug( "Error: no blob area descriptor \n");
	      return false;
	    }

	    DrawOp drawOp = new DrawOp();
	    //double color = 120.0;
	    drawOp.draw_boxDouble(pLabels,_pBoundBox[labval].ar,color);


	     return true;
	  }
	   /////////////////////////////////////////////////
	  // color support
	  public boolean showAreaBoxColor(ImageObject im, Point3DDouble color){
	      boolean ret = true;
	      int index;
	      //test
	      logger.info("Test: num of bounding boxes="+getNumBoxLabels());
	      for(index=1;index<getNumBoxLabels()+1;index++){
	       //test
	       //System.out.println("idx="+index);
	       //_pBoundBox[index].ar.PrintSubAreaFloat();
	        ret = ret & ShowOneAreaBoxColor(im,index, color);
	      }
	      return ret;
	  }

	   public boolean ShowOneAreaBoxColor(ImageObject pLabels, int labval, Point3DDouble color){
	    if(pLabels == null){
	    	logger.debug( "Error: no input image \n");
	      return false;
	    }
	     //test
	    //System.out.println("Test: before draw_boxDouble");
	    if(_pBoundBox == null  ){
	    	logger.debug( "Error: no blob descriptors \n");
	      return false;
	    }
	    if(labval < 1 || labval >= getNumBoxLabels()+1){
	    	logger.debug( "Error: index to blob is out of bounds \n");
	      return false;
	    }
	    if( _pBoundBox[labval].ar == null){
	    	logger.debug( "Error: no blob area descriptor \n");
	      return false;
	    }
	    if(pLabels.getTypeString().equalsIgnoreCase("BYTE")==false){
	    	logger.debug("Error: other than BYTE image is not supported");
	        return false;
	    }
	    if(pLabels.getNumBands() < 3){
	    	logger.debug("Error: the image does not contain at least three bands");
	        return false;
	    }

	    DrawOp drawOp = new DrawOp();
	    //double color = 120.0;
	    ImPoint ptsc = new ImPoint();
	    ptsc.x = _pBoundBox[labval].ar.getRow();
	    ptsc.y = _pBoundBox[labval].ar.getCol();
	    drawOp.draw_colorboxDouble(pLabels,_pBoundBox[labval].ar,ptsc,color);

	     return true;
	   }

	   // end of class
}
