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
package edu.illinois.ncsa.isda.im2learn.ext.segment;


import java.awt.Image;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;

import javax.media.jai.InterpolationNearest;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageException;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.im2learn.core.datatype.SubArea;

public class ImageTile {
	RenderedImage largeIm;
	int imSizeX = 1000, imSizeY = 1000;
	String inFilename;
	static private Log logger = LogFactory.getLog(ImageTile.class);
	
	int numrow, numcol; 
	
	int width = 500, height = 500;
	ImageObject densityIm;
	
	
	public ImageTile(String filename) {
		try{
			inFilename = filename;	
			largeIm = JAI.create("fileload", inFilename);	
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setTileSize(int width, int height) {
		imSizeX = width;
		imSizeY = height;

		numrow = (int)((float)largeIm.getHeight()/(float)imSizeY); 
		numcol = (int)((float)largeIm.getWidth()/(float)imSizeX); 
				
	}
	
	public String getInputFilename() {
		return inFilename;
	}
	
	public boolean tileImage() {
		if(largeIm == null) {
			logger.error("Image is not available");
			return false;
		}
		
		try{	
			ParameterBlock  pb;
	    	String outf = inFilename.substring(0,inFilename.length()-4);
	    	
	    	for(int j=0; j<numrow; j++) {
	    		for(int i=0; i<numcol; i++) {
	    			pb = new ParameterBlock();
	    			 pb.addSource(largeIm);
	    			 pb.add((float)(i*imSizeX));
	    			 pb.add((float)(j*imSizeY));
	    			 pb.add((float)(imSizeX));
	    			 pb.add((float)(imSizeY));

	    			 RenderedOp im = JAI.create("crop",pb);
	    			 JAI.create("filestore", im,outf+"_"+String.valueOf(i)+"_"+String.valueOf(j)+".tif", "TIFF", null);

	    			 System.out.println("File Saved "+ outf+"_"+String.valueOf(i)+"_"+String.valueOf(j)+".tif  outof "+numcol+"_"+numrow);
	    			 }
	    	}
			
			return true;	
		}
		catch(Exception e) {
			return false;
		}
	}

	public RenderedImage getlargeImage() {
		return largeIm;
	}
	
	
	
	
	
	public ImageObject getTileImage(int x, int y) {
		if(largeIm == null) {
			logger.error("Image is not available");
			return null;
		}
		ParameterBlock  pb;
	    	
		pb = new ParameterBlock();
		pb.addSource(largeIm);
		pb.add((float)(x*imSizeX));
		pb.add((float)(y*imSizeY));
		pb.add((float)(imSizeX));
		pb.add((float)(imSizeY));
			
		try {
			return ImageObject.getImageObject((Image)JAI.create("crop",pb).getAsBufferedImage());
		} catch (ImageException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public RenderedOp carveROI(SubArea sub, float scaleFactor) {
		if(largeIm == null) {
			logger.error("SVS Image is not available");
			return null;
		}
		
		try{	
	    	ParameterBlock  pb = new ParameterBlock();
	    	pb.addSource(largeIm);
	    	pb.add((float)sub.x);
	    	pb.add((float)sub.x);
	    	pb.add((float)sub.width);
	    	pb.add((float)sub.height);

	    	RenderedOp im = JAI.create("crop",pb);

	    	pb = new ParameterBlock();
	    	pb.addSource(im);
	    	pb.add(scaleFactor);
	    	pb.add(scaleFactor);
	    	pb.add(0F);
	    	pb.add(0F);

	    	return JAI.create("scale",pb);
	    		
		}
		catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	public boolean saveDownsampleImage(String filename) {
		try{
			RenderedImage downIm = downSampleImage();
			JAI.create("filestore",downIm, filename, "tiff");

			return true;
		}
		catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		
	}
	public boolean saveSubsampleImage(String filename) {
		try{
			RenderedImage downIm = downSampleImage();
			JAI.create("filestore",downIm, filename, "tiff");

			return true;
		}
		catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		
	}
	
	
	public void setNumTiles(int w, int h) {
		width = w;
		height = h;
		try {
			densityIm = ImageObject.createImage(h,w,3,ImageObject.TYPE_BYTE);
		} catch (ImageException e) {
			e.printStackTrace();
		}
	}
	
	public ImageObject getSubSampled(ImageTile tileIm) {
		
	    setNumTiles(tileIm.numcol, tileIm.numrow);
		for(int j=0; j<height; j++) {
			for(int i=0; i<width; i++) {
				try {
					System.out.println("Processing: "+ i+"_"+j);
					densityIm.set(j,i,0,tileIm.getTileImage(i,j).getByte(0,0,0));
					densityIm.set(j,i,1,tileIm.getTileImage(i,j).getByte(0,0,1));
					densityIm.set(j,i,2,tileIm.getTileImage(i,j).getByte(0,0,2));
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		return densityIm;
	}
	
	public RenderedImage downSampleImage() {
		float ratioX = (float)imSizeX/(float)largeIm.getWidth();
		float ratioY = (float)imSizeY/(float)largeIm.getHeight();
		
		try {
			ParameterBlock pb = new ParameterBlock();
			pb.addSource(largeIm);
	    	pb.add(ratioX);
	    	pb.add(ratioY);
	    	pb.add(0F);
	    	pb.add(0F);
	    	pb.add(new InterpolationNearest());
	    	return JAI.create("scale",pb,null);
		}
		catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public RenderedOp downSampleImage(float ratio) {
		try {
			ParameterBlock pb = new ParameterBlock();
			
	    	pb.add(ratio);
	    	pb.add(ratio);
	    	pb.add(0F);
	    	pb.add(0F);
	    	pb.add(new InterpolationNearest());
	    	
	    	return JAI.create("scale",pb,null);
		}
		catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
   
}
