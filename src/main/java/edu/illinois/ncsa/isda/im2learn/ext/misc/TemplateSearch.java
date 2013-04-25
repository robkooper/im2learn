package edu.illinois.ncsa.isda.im2learn.ext.misc;

import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageException;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.im2learn.core.datatype.SubArea;
import edu.illinois.ncsa.isda.im2learn.core.io.FileChooser;
import edu.illinois.ncsa.isda.im2learn.core.io.ImageLoader;



/**
 * <p>Title: TemplateSearch</p>
 * <p>Description: It is a brute force template search </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Sang-Chul Lee & Peter Bajcsy
 * @version 1.0
 */

public class TemplateSearch {
	private ImageObject _image,_template;

	int mWidth, mHeight;
	int subsampleX = 100, subsampleY = 100;
 
	
	
	public TemplateSearch() {
	}
 	
	public Point Search(ImageObject img, ImageObject tem, SubArea templateSub) throws ImageException {
		  if(img==null || tem==null){
			  System.err.println("ERROR: missing input images");
			  return null;
		  }
		 
		  mWidth = img.getNumCols()-tem.getNumCols()+1; 
		  mHeight = img.getNumRows()-tem.getNumRows()+1;
		  
		  _image = img;
		  if(templateSub != null) {
			  _template = tem.crop(templateSub);
		  }
		  else {
			  _template = tem;
		  }
		  
		 // setNumSample(10,10);
		  
		  System.out.println("Search Space = " + mWidth + " " + mHeight);
		  
		 // System.out.println("Calculating Similarity...");
		  double[][] measurement = calculateDisimilarity(templateSub);
		  
		  //System.out.println("Returning similar region coordinate...");
		  xyv min =  getSmallestIndex(measurement);
		 
		  Point retPtr = new Point(min.x,min.y);
		

		  return retPtr;
	  }
	
	public Point Search(ImageObject img, ImageObject tem) {
		try {
			return Search(img,tem,null);
		} catch (ImageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
	 /* if(img==null || tem==null){
		  System.err.println("ERROR: missing input images");
		  return null;
	  }
	  _image = img;
	  _template = tem;
	  
	  mWidth = _image.getNumCols()-_template.getNumCols()+1; 
	  mHeight = _image.getNumRows()-_template.getNumRows()+1;
	  
	 // setNumSample(10,10);
	  
	  System.out.println("Search Space = " + mWidth + " " + mHeight);
	  
	  System.out.println("Calculating Similarity...");
	  double[][] measurement = calculateDisimilarity(null);
	  
	  System.out.println("Returning similar region coordinate...");
	  xyv min =  getSmallestIndex(measurement);
	 
	  Point retPtr = new Point(min.x,min.y);
	  System.out.println(retPtr.x+" "+retPtr.y+" "+min.v);
	  
	  return retPtr;*/
  }
	
	
	public Point SearchHorLine(ImageObject img, ImageObject tem, int row) throws ImageException {
		int r = row;
		if(row < 0) {
			r = (int)((float)tem.getNumRows()/(float)2);
		}
			
		SubArea sub = new SubArea(0,r,tem.getNumCols(),1);
		setSubSampleDist(100,1);
		return Search(img, tem, sub);
	}

	public Point SearchVertLine(ImageObject img, ImageObject tem, int col) throws ImageException {
		int c = col;
		if(col < 0) {
			c = (int)((float)tem.getNumCols()/(float)2);
		}
		
		SubArea sub = new SubArea(c,0,1, tem.getNumRows());
		setSubSampleDist(1,1);
		return Search(img, tem, sub);
	}
	
  public void setNumSample(int numX, int numY) {
	  subsampleX = (int)((float)_template.getNumCols()/(float)numX);
	  subsampleY = (int)((float)_template.getNumRows()/(float)numY);
	  
 	  System.out.println("Sub-Sampling distance = " + subsampleX + " " + subsampleY);
  }  

  
  public void setSubSampleDist(int numX, int numY) {
	  subsampleX = numX;
	  subsampleY = numY;
	  
 	  System.out.println("Sub-Sampling distance = " + subsampleX + " " + subsampleY);
  }  

  
 
  public double[][] calculateDisimilarity(SubArea sub) {
		 double [][] retArr = new double[mHeight][mWidth];
		  int i,j;
	    
		  for(j=0; j<mHeight; j++) {
			  for( i=0; i<mWidth; i++) {
				  retArr[j][i] = calcSSD(i,j, sub);
				  
			  }
		  }
		  
		  return retArr;
	  }
  

  private float calcSSD(int x, int y, SubArea sub) {
    double val,f,t;
    val = 0;
    for(int j=0; j<_template.getNumRows(); j += subsampleY) {
    	for(int i=0; i<_template.getNumCols(); i += subsampleX) {
    		for(int k=0; k<_template.getNumBands(); k++) {
    			if(sub != null) {
    				f = _image.getFloat(j+y+sub.y,i+x+sub.x,k);
    			}
    			else {
    				f = _image.getFloat(j+y,i+x,k);
    			}
    			t = _template.getFloat(j,i,k);
    			val += (f-t)*(f-t);
    		}
    	}
    }
    return (float)(Math.sqrt(val));
  }

  
  public xyv getSmallestIndex(double[][] data) {
	  
	  xyv retPtr = new xyv();
	  retPtr.v = Float.MAX_VALUE;
	  
	  for(int j=0; j<data.length; j++) {
		  for(int i=0; i<data[0].length; i++) {
			  if(data[j][i] == 0) {
				  System.out.println("Identical Region "+i+" "+j);
			  }
			  if(data[j][i] < retPtr.v) {
				  retPtr.v = data[j][i];
				  retPtr.x = i;
				  retPtr.y = j;
			  }
		  }
	  }
	  return retPtr;
  }

  class xyv {
	  int x;
	  int y;
	  double v;
  }

  
  static void filecompare(String filename1, String filename2) {
		try{
			Date now;
			
			System.out.println("\n===========================");
			System.out.println("Loading..");
			
			now = new Date();
			System.out.println(now.getTime() + " " + now);

			File f1 = new File(filename1);
			File f2 = new File(filename2);
			
			
			ImageObject im1 = ImageLoader.readImage(filename1);
			ImageObject im2 = ImageLoader.readImage(filename2);
			
			
			System.out.println("----------------------------");
			System.out.println(filename1);
			System.out.println("File Size = " + f1.length() + "Byte");
			System.out.println("Image Dimension = " + im1.getNumCols() + " " + im1.getNumRows());
			System.out.println("----------------------------");
			System.out.println(filename2);
			System.out.println("File Size = " + f2.length() + "Byte");
			System.out.println("Image Dimension = " + im2.getNumCols() + " " + im2.getNumRows());
			System.out.println("----------------------------");
			
		//	ts.Search(im1, im2);
//			SubArea sub = new SubArea(15,10, 150, 100);
//			Point ptr = ts.Search(im1, im2,sub);
			
			System.out.println("Computing...");
			now = new Date();
			System.out.println(now.getTime() + " " + now);


			
			Point ptr = ts.SearchHorLine(im1, im2, -1);
			
			System.out.println("Alignment Result (x,y) = "+ptr.x+" "+ptr.y);
			System.out.println("Done.");
			now = new Date();
			System.out.println(now.getTime() + " " + now);

			
			  
		}
		catch(Exception e) {
			e.printStackTrace();
		}
  
  }
  

  static void directorycompare() throws IOException {
	  	Date now;
	  	FileChooser file = new FileChooser();
	  	String[] tempfilenames = file.showMultiOpenDialog();
			
	  	
			
	  	System.out.println("Launching..");
	  	now = new Date();
	  	System.out.println(now.getTime() + " " + now);
			
	  	String imFilename = "";
	  	File f;
	  	for(int i=0; i<tempfilenames.length; i++) {
	  		f = new File(tempfilenames[i]);	
	  		imFilename = "I:\\data\\lpaper\\uncropped\\"+f.getName();
	  		
	  		//	System.out.println(imFilename+" , "+tempfilenames[i]);
	  		filecompare(imFilename, tempfilenames[i]);
				
	  	}
			  
  }
		



  static TemplateSearch ts = new TemplateSearch();
  static public void main(String[] args){
	  try {
	
		directorycompare();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

  }
  
}
