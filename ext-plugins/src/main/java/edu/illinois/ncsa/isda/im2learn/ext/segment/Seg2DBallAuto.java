package edu.illinois.ncsa.isda.im2learn.ext.segment;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.im2learn.core.datatype.ImPoint;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.im2learn.core.datatype.SubArea;
import edu.illinois.ncsa.isda.im2learn.core.display.ImageMarker;
import edu.illinois.ncsa.isda.im2learn.ext.misc.ImageFrame;
import edu.illinois.ncsa.isda.im2learn.ext.statistics.ImageMoment;


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
public class Seg2DBallAuto {

  ImageObject im,imgMask,candidateMask;
  int imWidth, imHeight;

  int _ballSize =1;
  Seg2DBall _segment = new Seg2DBall();

  double _delta = 1;
  
  int candidateSearchStep = 2;
  long minRegionSize = 100;// pixels
  long maxRegionSize = 1000000;
  
  protected ImPoint[] _centroidList;
  protected SubArea[] _subAreaList;
  int[] _regionPtLimit = new int[4];

 // int minSegSize = 10000;

  ImageMoment _moment;

  protected int MAX_REGION_WIDTH = 3000;
  protected int MAX_REGION_HEIGHT = 3000;
  boolean _debug_show_centroids = false, _debug_show_feature = false, _debug_print_segment = false;
  static private Log logger = LogFactory.getLog(Seg2DBallAuto.class);

  public Seg2DBallAuto() {
  }
  
  public Seg2DBallAuto(int ballSz, double delta,
                             int minRegionSz, int maxRegionSz) {
    _delta = delta;
    _ballSize = ballSz;
    minRegionSize = minRegionSz;
    maxRegionSize = maxRegionSz;
  }


  public void setBallSize(int ballSz) {
	  _ballSize = ballSz;
  }

  public void setDelta(double delta) {
	  _delta = delta;
  }

  public void setSegRegionLimit(int maxWidth, int maxHeight) {
	  MAX_REGION_WIDTH = maxWidth;
	  MAX_REGION_HEIGHT = maxHeight;
  }
  
  public void setSegAreaLimit(int minRegionSz, int maxRegionSz) {
	  minRegionSize = minRegionSz;
	  maxRegionSize = maxRegionSz;
  }
  
public void setImage(ImageObject imObj) {
    if(imObj.getNumBands() > 1) {
    	logger.error("Set one band image");
    	return;
    }
	    
	im = imObj;
    imWidth = im.getNumCols();
    imHeight = im.getNumRows();
  }

  public void runSegmentation() {
	try {
		run();
	} catch (Exception e) {
		e.printStackTrace();
	}
  }
  
  void run() throws Exception {
    imgMask = ImageObject.createImage(imHeight, imWidth, 1, "INT");
    clearMask(imgMask); // set all values to 1

   // _segment.setBallSize(_ballSize);
    
    _segment.setThreshold(_delta);

    ArrayList centroidList = new ArrayList();
    ArrayList subAreaList = new ArrayList();
    candidateMask = computeCandidate(im, centroidList, subAreaList);
    
    
    Object[] obj = centroidList.toArray();
    _centroidList = new ImPoint[obj.length];
    for(int i=0; i<obj.length; i++) {
      _centroidList[i] = (ImPoint)obj[i];
    }

    obj = subAreaList.toArray();
    _subAreaList = new SubArea[obj.length];
    for(int i=0; i<obj.length; i++) {
      _subAreaList[i] = (SubArea)obj[i];
    }
    
    
    if(_debug_show_centroids) {
      displayImageFeatures(im, _centroidList, "Feature");
     // displayImageFeaturesImageFrame(im, _centroidList, "Feature");
    }

  }

  private void clearMask(ImageObject mask) {
    for(int i=0; i<mask.getSize(); i++) {
      mask.set(i,1);
    }
  }


  public ImageObject getSegImage() {
	  return candidateMask;
  }
  
  public ImPoint[] getCentroidList() {
	  return _centroidList;
  }
  
  public SubArea[] getSubAreaList() {
	  return _subAreaList;
  }
  
/////////////// Compute Feature Candidates

  ImageObject computeCandidate(ImageObject img, ArrayList centroidList, ArrayList subAreaList) throws Exception {

     ImageObject tempImg = null, bgSegImg = null;
   

     int w = img.getNumCols();
     int h = img.getNumRows();
     int type = img.getType();

     SubArea sub = new SubArea(_ballSize + 1, _ballSize + 1, w, h);

     bgSegImg = ImageObject.createImage(h + _ballSize * 2 + 2, w + _ballSize * 2 + 2,1, type);
     tempImg = ImageObject.createImage(h + _ballSize * 2 + 2, w + _ballSize * 2 + 2,1, type);

     // background segmentation
     tempImg.insert(img, _ballSize + 1, _ballSize + 1); // boundary padded image
     _segment = new Seg2DBall();
     _segment.setThreshold(1);
     _segment.setBallSeed(_ballSize,_ballSize,_ballSize);
//     _segment.setBallSize(_ballSize);
//     _segment.setSeed(_ballSize,_ballSize);
     _segment.setImage(tempImg);
     _segment.segment();

     bgSegImg = _segment.getSegImageObject(); // background seg map

     for (int k = 0; k < bgSegImg.getSize(); k++) {
       if(tempImg.getInt(k) <= _delta ) {
         bgSegImg.set(k, (bgSegImg.getInt(k) + 1) % 2);
       }
       else {
         bgSegImg.set(k,0);
       }
     }
         
     bgSegImg = bgSegImg.crop(sub); // remove padded area
//     
//     Im2LearnNCSA bgFrame = new Im2LearnNCSA(bgSegImg);
//     bgFrame.pack();
//     bgFrame.setVisible(true);
    
     ImageObject segMap = ImageObject.createImage(bgSegImg.getNumRows(), bgSegImg.getNumCols(),1, "INT");
     ImageObject segFeature;

     long pix;

     int cnt = 1;
     
     _segment = new Seg2DBall();	
     _segment.setImage(bgSegImg);	
    
     for(int i=0; i<img.getNumCols(); i += candidateSearchStep) {
       for(int j=0; j<img.getNumRows(); j += candidateSearchStep) {
         if(bgSegImg.getInt(j*bgSegImg.getNumCols() + i) == 0) // background or edges
           continue;
//         if(img.getInt(j,i,0) > this._delta) // edges
//           continue;

         // candidate region
         
         _segment.setBallSeed(i,j, _ballSize);	
//         _segment.setBallSize(_ballSize);
//         _segment.setSeed(i+1,j+1);
//         
         _segment.setThreshold(0);
         _segment.segment();
         
         pix = _segment.getPixelCount();
         
       //  System.out.println("PIX="+pix);
         
         if (pix < this.minRegionSize || pix > this.maxRegionSize)
           continue;


         segFeature = _segment.getSegImageObject();
         if(segFeature == null)
           continue;

         this.computeRegionBoundary(segFeature, this._regionPtLimit);
         
    //     logger.info("Num Pix="+pix+"i="+i+"j="+j);
         
        

//         ImageFrame bgFrame = new ImageFrame("background segmentation");
//       bgFrame.setImageObject(segFeature.convert(ImageObject.TYPE_BYTE, true));
//       bgFrame.pack();
//       bgFrame.setVisible(true);
//       
              
       if(_regionPtLimit[0] < 0 ||
       		   _regionPtLimit[1] < 0 ||
               _regionPtLimit[2] < 0 ||
               _regionPtLimit[3] < 0)
              continue;
       
         for(int l=_regionPtLimit[1]; l<_regionPtLimit[3]; l++) {
           for (int k = _regionPtLimit[0]; k < _regionPtLimit[2]; k++) {
             if (segFeature.getInt(l*segFeature.getNumCols() + k) != 0) {
               bgSegImg.set(l * segMap.getNumCols() + k, 0);

               if (pix >= this.minRegionSize && pix <= this.maxRegionSize) {
                 segMap.set(l * segMap.getNumCols() + k, cnt);
               }
             }
           }
         }

         cnt++;
         try{

          // calculate a list of centroids here
          ImPoint cent = new ImPoint();
           computeCentroid(segFeature, this._regionPtLimit, cent);
           
           if(_debug_print_segment)
        	   logger.info((cnt)+": centroid = ("+cent.x+","+cent.y+") moment="+cent.getV());
           
           SubArea region = new SubArea();
           region.setBounds(_regionPtLimit[0],
        		   _regionPtLimit[1],
        		   _regionPtLimit[2]-_regionPtLimit[0],
        		   _regionPtLimit[3]-_regionPtLimit[1]);
           
           if(_debug_print_segment)
        	   logger.info((cnt)+": region = ("+region.x+","+region.y+","+
        			   region.width+","+region.height+")");

           
           centroidList.add(cent);
           subAreaList.add(region);
         }
         catch(Exception e) {
           e.printStackTrace();
           continue;
         }
       }


     }


  // segMap = noiseRemove(segMap);

   if(this._debug_show_feature) {
	ImageFrame showFrm = new ImageFrame("segment");
   	showFrm.setImageObject(segMap.convert(ImageObject.TYPE_BYTE, true));
   	showFrm.pack();
   	showFrm.setVisible(true);
   }
   return segMap;
   }

   void computeRegionBoundary(ImageObject label, int[] limit) {
     int maxX = Integer.MIN_VALUE;
     int maxY = Integer.MIN_VALUE;
     int minX = Integer.MAX_VALUE;
     int minY = Integer.MAX_VALUE;

     for (int j = 0; j < label.getNumRows(); j++) {
       for (int i = 0; i < label.getNumCols(); i++) {
         if (label.getInt(j * label.getNumCols() + i) > 0) {
            if(i > maxX)
              maxX = i;
            if(j > maxY)
              maxY = j;
            if(i < minX)
              minX = i;
            if(j < minY)
              minY = j;
          }
       }
     }

     if((maxX-minX > MAX_REGION_WIDTH) || (maxY-minY) > MAX_REGION_HEIGHT) {
    	 logger.info("Segmentation exceeds the max region area. To segment, adjust the value.");
       limit[0] = limit[1] = limit[2] = limit[3] = -1;
     }
     else {
       limit[0] = minX;
       limit[1] = minY;
       limit[2] = maxX;
       limit[3] = maxY;
     }

   }

   void computeCentroid(ImageObject mask, int[] limit, ImPoint centroid) throws Exception {
     _moment = new ImageMoment();
     _moment.SetImage(mask.crop(new SubArea(limit[0],limit[1],limit[2]-limit[0], limit[3]-limit[1])));
     centroid.SetImPoint(_moment._cx+limit[0],_moment._cy+limit[1],
                           _moment.getCentralMoment(0,0));
     }

   ImageObject segmentRegion(ImageObject img, int x, int y, int ballSize) throws Exception {
     if(img.getType() == ImageObject.TYPE_BYTE) {
       _segment.setThreshold(_delta);
     }
     else if(img.getType() == ImageObject.TYPE_INT) {
       _segment.setThreshold(0);
     }
     _segment.setBallSeed(x,y, ballSize);
//     _segment.setBallSize(ballSize);
//     _segment.setSeed(x,y);
     _segment.segment();
     return _segment.getSegImageObject();
   }

 ///////////////////////////////////


 ////////////////////// Display

 void displayImageFeatures(ImageObject im, ImPoint[] pts, String title) {
  ImageFrame imDispFrm = new ImageFrame(title);
  imDispFrm.setImageObject(im);
  ImageMarker marker = new ImageMarker();
  for(int i=0; i<pts.length; i++) {
	  marker.add(pts[i].getX(), pts[i].getY());
  }
  imDispFrm.getImagePanel().addAnnotationPanel(marker);
  imDispFrm.pack();
  imDispFrm.setVisible(true);

 }

// void displayImageFeaturesImageFrame(ImageObject im, ImPoint[] pts, String title) {
//   ImageObject outIm = im.CopyImageObject();
//   for(int i=0; i<pts.length; i++) {
//     ImageTool.drawPoint(outIm,new Point((int)pts[i].x, (int)pts[i].y),3,10,(byte)255);
//   }
//   new ImageFrame(outIm,null,new Point(0,0),title);
//
//  }

//  void saveFeatures(ImPoint[] pts, String filename) {
//    double[][] featureTable = new double[pts.length][3];
//    for(int i=0; i<pts.length; i++) {
//      featureTable[i][0] = pts[i].x;
//      featureTable[i][1] = pts[i].y;
//      featureTable[i][2] = pts[i].v;
//    }
//    TableIO tio = new TableIO();
//    tio.setTable(featureTable,"\t");
//    tio.saveTable(filename);
//   }



 }

