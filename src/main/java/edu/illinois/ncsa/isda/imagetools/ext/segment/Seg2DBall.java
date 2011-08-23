package edu.illinois.ncsa.isda.imagetools.ext.segment;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;

//import ncsa.im2learn.core.Im2Learn;

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


public class Seg2DBall {
  ImageObject ball;
  int _seedX, _seedY, _ballSize = 2, _ballRadius = 1;
  double seedVal, seedDev;
  ImageObject _segImage, _circImage, _srcImage, mapImage;
  
  double threshold=10;
  int cnt = 0;

  private long pixelCount = 0, maxPixelCount = Long.MAX_VALUE, circPixelCount = 0;
  
  stack sx, sy;

  int[] ballCoordX, ballCoordY;

  static private Log logger = LogFactory.getLog(Seg2DBall.class);
  
  public Seg2DBall() {
  }

  public Seg2DBall(ImageObject srcImage, int seedX, int seedY, int ballSize) {


	  try{
		if(isTypeAcceptable(srcImage)){
		     setImage(srcImage);
		      setSeed(seedX, seedY);
		      setBallSize(ballSize);
		      regionGrow(_seedX, _seedY);		
		}
	  }
	  catch(Exception e) {}

  }

  // check supported types of image data
  private boolean isTypeAcceptable(ImageObject obj){
	   if(obj.getType() == ImageObject.TYPE_BYTE || obj.getType() == ImageObject.TYPE_SHORT || obj.getType() == ImageObject.TYPE_USHORT || obj.getType() == ImageObject.TYPE_INT) {
		   return true;
	   }	
	  System.err.println("Only support Byte, short, ushort and Int");
	  return false;	 
  }

  public void setImage(ImageObject srcImage, ImageObject segImage) throws Exception {

    sx = new stack(srcImage.getNumCols()*srcImage.getNumRows());
    sy = new stack(srcImage.getNumCols()*srcImage.getNumRows());

	if(isTypeAcceptable(srcImage)){
       _srcImage = srcImage;
       _segImage = segImage;
       mapImage = ImageObject.createImage(srcImage.getNumRows(), srcImage.getNumCols(), 1, "BYTE");
       computeSeedValMean();
     }

   }

  public void setImage(ImageObject srcImage, ImageObject segImage, int band) throws Exception {

     sx = new stack(srcImage.getNumCols()*srcImage.getNumRows());
     sy = new stack(srcImage.getNumCols()*srcImage.getNumRows());

 	if(isTypeAcceptable(srcImage)){
    	 if(srcImage.getNumBands() > 1)
         _srcImage = srcImage.extractBand(band);
       else
         _srcImage = srcImage;

       _segImage = segImage;
       mapImage = ImageObject.createImage(srcImage.getNumRows(), srcImage.getNumCols(), 1, "BYTE");
       computeSeedValMean();
     }  else {
       System.err.println("Only support Byte and Int");
     }

   }


  public void setImage(ImageObject srcImage) throws Exception {

    sx = new stack(srcImage.getNumCols()*srcImage.getNumRows());
    sy = new stack(srcImage.getNumCols()*srcImage.getNumRows());

	if(isTypeAcceptable(srcImage)){
      _srcImage = srcImage;
      _segImage = ImageObject.createImage(srcImage.getNumRows(), srcImage.getNumCols(), 1, "INT");
      mapImage = ImageObject.createImage(srcImage.getNumRows(), srcImage.getNumCols(), 1, "BYTE");
      computeSeedValMean();
    }  else {
      System.err.println("Only support Byte and Int");
    }

  }

  public void setImage(ImageObject srcImage, int band) throws Exception {

    sx = new stack(srcImage.getNumCols()*srcImage.getNumRows());
    sy = new stack(srcImage.getNumCols()*srcImage.getNumRows());

	if(isTypeAcceptable(srcImage)){
    
      if(srcImage.getNumBands() > 1)
        _srcImage = srcImage.extractBand(band);
      else
        _srcImage = srcImage;

      _segImage = ImageObject.createImage(srcImage.getNumRows(), srcImage.getNumCols(), 1, "INT");
      mapImage = ImageObject.createImage(srcImage.getNumRows(), srcImage.getNumCols(), 1, "BYTE");
      _ballRadius = mapImage.getSize();
      computeSeedValMean();
    }   else {
      System.err.println("Only support Byte and Int");
    }

  }
  public void setMaxPixelCount(long maxpixelcount) {
    this.maxPixelCount = maxpixelcount;
  }

  public void setBallSeed(int x, int y, int ballSize) throws Exception {
	  setBallSize(ballSize);
	  setSeed(x,y);
  }
  
  private void setSeed(int x, int y) {
    _seedX = x-_ballRadius;
    _seedY = y-_ballRadius;
    computeSeedValMean();
  }

  private void setBallSize(int ballSize) throws Exception {
    _ballSize = ballSize;
    _ballRadius = (int)((float)ballSize/2+0.4);
    ball = new Ball(ballSize,(byte)0,(byte)1).getImageObject();
    computeBallcoords();
    computeSeedValMean();
  }



 public double getSeedVal() {
   return seedVal;
 }

 public double getSeedStd() {
   return seedDev;
 }

 void computeBallcoords() {
   ballCoordX = new int[ball.getNumRows()*ball.getNumCols()+1];
   ballCoordY = new int[ball.getNumRows()*ball.getNumCols()+1];

   int cnt = 0;

   for(int j=0; j<ball.getNumRows(); j++) {
     for(int i=0; i<ball.getNumCols(); i++) {
       if(ball.getInt(j,i,0) != 0) {
         ballCoordX[cnt] = i;
         ballCoordY[cnt++] = j;
       }
     }
   }

   ballCoordX[cnt] = ballCoordY[cnt] = -1;

 }



 void computeSeedVal() {
 
	 if(isTypeAcceptable(_srcImage)){
     seedVal = _srcImage.getInt(_seedY,_seedX,1); 
   }
 }

 void computeSeedValMean() {
   try{
     double mean, std = 0;
     int cnt = 0, sum = 0;

	 if(!isTypeAcceptable(_srcImage)){
		 System.err.println("Only support Byte, Short, UShort and Int");
		 return;
	 }
		 
       for (int i = 0; i < ballCoordX.length; i++) {
         if(ballCoordX[i] < 0)
           break;

         sum += _srcImage.getInt(_seedY+ballCoordY[i], _seedX+ballCoordX[i],0); // BUG: changed 1 to 0 in final argument
         cnt++;
       }
     


     mean = (double) sum / (double) cnt;

     double v;
     cnt = 0;

       for (int i = 0; i < ballCoordX.length; i++) {
         if(ballCoordX[i] < 0)
           break;
         v = _srcImage.getDouble(_seedY+ballCoordY[i], _seedX+ballCoordX[i],0); // BUG: changed 1 to 0 in final argument
         std += (v - mean) * (v - mean);
         cnt++;
       }
  
     std = Math.sqrt( (double) std / (double) cnt);

//     seedVal = (int) (mean + 0.5d);
     seedVal = mean;
     seedDev = std;
//     System.out.println("Seed Value=" + seedVal + " " + mean + " " + std);
   }
   catch(Exception e) {}
 }
 
 void initImages() {
	 int[] data = (int[])_segImage.getData();
	 for(int i=0; i<data.length; i++)
		 data[i] = 0;	
 }
 	
 public void segment() {
	 sx.init();
	 sy.init();
	 initImages();
	 regionGrow(_seedX,_seedY);	
 }
 
 public void segment2() {
	 sx.init();
	 sy.init();
	 initImages();
	 regionGrow2(_seedX,_seedY);	
 }
 
 public void computeCircumference() {
  byte[] retCirc = null;
   int cnt = 0;
      
      try{
    	  int[] circData = (int[]) _segImage.getData();
	      retCirc = new byte[circData.length];
	
	      for(int j=1; j<_segImage.getNumRows()-1; j++) {
	        for(int i=1; i<_segImage.getNumCols()-1; i++) {
	          if(circData[_segImage.getNumCols()*j + i] == 0 ) {
	            if(_neighborChk(circData,i,j,_segImage.getNumCols(), _segImage.getNumRows())) {
	              retCirc[_segImage.getNumCols()*j + i] = (byte)-1;
	              cnt++;
	            }
	          }
	        }
	      }

	      _circImage = ImageObject.createImage(_segImage.getNumRows(), _segImage.getNumCols(),1,ImageObject.TYPE_BYTE);
	      _circImage.setData(retCirc);
	      circPixelCount = (long)cnt;
      }
      catch(Exception e) {
    	  e.printStackTrace();
      }
 }
 

 boolean _neighborChk(int[] data, int i, int j, int width, int height) {
   if(i == 0 || j == 0 || i == width-1 || j == height-1)
     return false;

   if(data[j*width+i] != data[j*width+i+1]) {
     return true;
   }
   if(data[j*width+i] != data[j*width+i-1]) {
     return true;
   }
   if(data[j*width+i] != data[(j+1)*width+i]) {
     return true;
   }
   if(data[j*width+i] != data[(j-1)*width+i]) {
     return true;
   }

   return false;
 }


  public void regionGrow(int px, int py) {

    pixelCount = 0;

    int xLimit = _srcImage.getNumCols() - _ballRadius*2-1;
    int yLimit = _srcImage.getNumRows() - _ballRadius*2-1;
    int x, y;

    if(px < 0 || py < 0 || px > xLimit || py > yLimit)
    	return;

    if(collisionTest(px, py)) 
    	return;

      

    sx.init();
    sy.init();

    sx.push(px);
    sy.push(py);


    while (!sx.isempty()) {
      
      x = sx.pop();
      y = sy.pop();
      fillArea(x, y);
  
      if(pixelCount > maxPixelCount) {
        this._segImage = null;
        break;
      }

      if (! (x < 0 || y - 1 < 0 || x > xLimit || y - 1 > yLimit)
          &&  mapImage.getInt(y - 1,x,0) == 0 &&
          !collisionTest(x, y - 1) ) {
        sx.push(x);
        sy.push(y - 1);
      }

      if (! (x < 0 || y + 1 < 0 || x > xLimit || y + 1 > yLimit) &&
          mapImage.getInt(y + 1, x, 0) == 0 &&
          !collisionTest(x, y + 1) ) {
        sx.push(x);
        sy.push(y + 1);
      }

      if (! (x + 1 < 0 || y < 0 || x + 1 > xLimit || y > yLimit) &&
          mapImage.getInt(y, x + 1,0) == 0 &&
          !collisionTest(x + 1, y) ) {

        sx.push(x + 1);
        sy.push(y);
      }

      if (! (x - 1 < 0 || y < 0 || x - 1 > xLimit || y > yLimit) &&
          mapImage.getInt(y, x - 1,0) == 0 &&
          !collisionTest(x - 1, y) ) {

        sx.push(x - 1);
        sy.push(y);
      }
    }
  }

  public void regionGrow2(int px, int py) {

	    pixelCount = 0;

	    int xLimit = _srcImage.getNumCols() - _ballRadius*2-1;
	    int yLimit = _srcImage.getNumRows() - _ballRadius*2-1;
	    int x, y;

	    if(px < 0 || py < 0 || px > xLimit || py > yLimit)
	      return;
	    if(collisionTest2(px, py))
	      return;

	    sx.init();
	    sy.init();

	    sx.push(px);
	    sy.push(py);


	    while (!sx.isempty()) {
	      
	      x = sx.pop();
	      y = sy.pop();
	      fillArea(x, y);
	  
	      if(pixelCount > maxPixelCount) {
	        this._segImage = null;
	        break;
	      }

	      if (! (x < 0 || y - 1 < 0 || x > xLimit || y - 1 > yLimit)
	          &&  mapImage.getInt(y - 1,x,0) == 0 &&
	          !collisionTest2(x, y - 1) ) {
	        sx.push(x);
	        sy.push(y - 1);
	      }

	      if (! (x < 0 || y + 1 < 0 || x > xLimit || y + 1 > yLimit) &&
	          mapImage.getInt(y + 1, x, 0) == 0 &&
	          !collisionTest2(x, y + 1) ) {
	        sx.push(x);
	        sy.push(y + 1);
	      }

	      if (! (x + 1 < 0 || y < 0 || x + 1 > xLimit || y > yLimit) &&
	          mapImage.getInt(y, x + 1,0) == 0 &&
	          !collisionTest2(x + 1, y) ) {

	        sx.push(x + 1);
	        sy.push(y);
	      }

	      if (! (x - 1 < 0 || y < 0 || x - 1 > xLimit || y > yLimit) &&
	          mapImage.getInt(y, x - 1,0) == 0 &&
	          !collisionTest2(x - 1, y) ) {

	        sx.push(x - 1);
	        sy.push(y);
	      }
	    }
	  }
  
  public long getPixelCount() {
    return pixelCount;
  }
  public long getCircPixelCount() {
	  return circPixelCount;
  }
  
  public ImageObject getSegImageObject() {
    return _segImage;
  }
  public ImageObject getCircImageObject() {
	  return _circImage;
  }
  
  public ImageObject getMapImageObject() {
    return mapImage;
  }

  public void setThreshold(double val) {
    this.threshold = val;
  }

  void fillArea(int x, int y) {
    mapImage.set(y,x,0,1);
    for (int i = 0; i < ballCoordX.length; i++) {
      if(ballCoordX[i] < 0)
        break;
      try{
        if(_segImage.getInt(y+ballCoordY[i],x+ballCoordX[i],0) == 0) {
        pixelCount++;
        _segImage.set(y+ballCoordY[i],x+ballCoordX[i],0, 1);
      }
      }
      catch(Exception e) {
    	  e.printStackTrace();
      }
    }

    return;
  }


 boolean collisionTest(int x, int y) {
	 for (int i = 0; i < ballCoordX.length; i++) {
       if(ballCoordX[i] < 0)
    	   break;
         
       if ( (Math.abs(_srcImage.getInt(y+ballCoordY[i], x+ballCoordX[i],0)
                       - seedVal) > (int)threshold))
         return true;
  
     }
     return false;
 }
 
 /**
  * This is the method for checking whether the ball is inside of the foreground mask
  * @param x - col of ball
  * @param y - row of ball
  * @return true if the ball is inside of the foreground mask
  */
 boolean collisionTest2(int x, int y) {
	 for (int i = 0; i < ballCoordX.length; i++) {
       if(ballCoordX[i] < 0)
         break;
       if ( (Math.abs(_srcImage.getInt(y+ballCoordY[i], x+ballCoordX[i],0)
               - seedVal) > (int)threshold) || _srcImage.getInt(y+ballCoordY[i], x+ballCoordX[i],0) >= 250.0)
    	   return true;
  
     }
     return false;
 }


class stack {
  int[] stk;
  int cnt;

  protected stack(int size) {
    stk = new int[size];
    cnt = 0;
  }

  protected void init() {
    cnt = 0;
  }

  protected void push(int val) {
    if (cnt < stk.length) {
      stk[cnt] = val;
      cnt++;
    }
    else
      return;
  }

  protected boolean isempty() {
    if (cnt == 0)
      return true;
    else
      return false;
  }

  protected int pop() {
    if (cnt > 0) {
      cnt--;
      return stk[cnt];
    }
    else
      return Integer.MIN_VALUE;

  }

}


class Ball {
  protected byte[][] ball;

  public Ball(int size, byte backgroundVal, byte foregroundVal) {
    ball = new byte[size][size];
    float center = (float)(size-1)/2;
    float radius = (float)size/2;

    for(int j=0; j<ball.length; j++) {
      for(int i=0; i<ball[0].length; i++) {
        if(eucDist(i,j,center) > radius) {
          ball[j][i] = backgroundVal;
        }
        else {
          ball[j][i] = foregroundVal;
        }
      }
    }

  }

  public ImageObject getImageObject() throws Exception{
    if(ball==null)
      return null;
    
        ImageObject retImage = ImageObject.createImage(ball.length, ball[0].length, 1, ImageObject.TYPE_BYTE);	
    
    for(int j=0; j<ball.length; j++){
    	for(int i=0; i<ball[0].length; i++) {
    		retImage.set(j,i,0,ball[j][i]);
    	}
    }
     return retImage;  
  }


  float eucDist(int x, int y, float center) {
    float dx = (float)x-center;
    float dy = (float)y-center;

    return (float)Math.sqrt((double)(dx*dx+dy*dy));
  }

}
}

