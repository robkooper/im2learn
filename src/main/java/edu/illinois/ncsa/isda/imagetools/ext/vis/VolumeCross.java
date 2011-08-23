package edu.illinois.ncsa.isda.imagetools.ext.vis;

import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.imagetools.core.io.ImageLoader;
import edu.illinois.ncsa.isda.imagetools.core.io.ANALYZE.ANALYZELoader;


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

public class VolumeCross {
  String inFilename, outFilename;
  
  ImageObject[] inImg;
  
  public final static int X_PLANE = 0, Y_PLANE = 1, Z_PLANE = 2;
  int plane;
  float offset;
  String parameter = null;

  public static void main(String[] args) {
	  
	  if(args.length == 4 &&
			   (args[1].equalsIgnoreCase("-x") ||
			    args[1].equalsIgnoreCase("-y") ||
			    args[1].equalsIgnoreCase("-z"))) {
		
		  File input = new File(args[0]);
		  
		  if(!input.canRead()) {
			  System.err.println("Cannot read the input file"); 
			  System.exit(0);
		  } 
		  		  	  	
		  VolumeCross cross = new VolumeCross(input.getAbsolutePath());

		  
		  
		  
		  try {
				ImageObject crossImg = cross.getCrossSection();
		  } catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			  
			  
		  
		  float offset = 0.5f;
		  
		  try {
			  offset = Float.parseFloat(args[2]);
			  
			  if(offset > 1 || offset < 0) {
				  System.err.println("Invalid Offset Range: it must be in [0,1]");
				  System.exit(0);
			  }
		  }
		  catch(Exception e) {
			  System.err.println("Invalid Offset Number Type");
			  System.out.println("Usage:\n"+
			  "slicer <input file> -<x|y|z> <offset> <output file>"); 
			  System.exit(0);
		  }
		  
		  
		  ImageObject crossImg = null;
		  try {
			  if(args[1].equalsIgnoreCase("-x")) {
				  crossImg = cross.getCrossSection(cross.getInputImage(),X_PLANE, offset);
			  }
			  else if(args[1].equalsIgnoreCase("-y")) {
				  crossImg = cross.getCrossSection(cross.getInputImage(),Y_PLANE, offset);
			  }
			  else if(args[1].equalsIgnoreCase("-z")) {
				  crossImg = cross.getCrossSection(cross.getInputImage(),Z_PLANE, offset);
			  } else {
				  System.err.println("Invalid Options");
				  System.out.println("Usage:\n"+
				  "slicer <input file> -<x|y|z> <offset> <output file>"); 
				  System.exit(0);
			  }
		  }
		  catch(Exception e) {
			  System.err.println("Failed to Slice");
			  e.printStackTrace();
			  System.exit(0);
		  }


		  
//		  try {
//			  crossImg = cross.getCrossSection(cross.getInputImage(),X_PLANE, 0.5f);
//			  Im2LearnNCSA f = new Im2LearnNCSA(crossImg);
//			  f.setVisible(true);
//			  
//			  ImageObject crossImg1 = cross.getCrossSection(cross.getInputImage(),Y_PLANE, 0.5f);
//			  Im2LearnNCSA f1 = new Im2LearnNCSA(crossImg1);
//			  f1.setVisible(true);
//			  
//			  ImageObject crossImg2 = cross.getCrossSection(cross.getInputImage(),Z_PLANE, 0.5f);
//			  Im2LearnNCSA f2 = new Im2LearnNCSA(crossImg2);
//			  f2.setVisible(true);
//		  
//		  } catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		  
		  
		  try {
			
		//	 ANALYZELoader.writeImage(args[3], new ImageObject[]{crossImg});
			  ImageLoader.writeImage(args[3], crossImg);
		  } catch (IOException e) {
			  System.err.println("Image output error");
			  e.printStackTrace();
			  System.exit(0);
			e.printStackTrace();
		}
		  
		  
		  
		  
		  
		  
		  
	  }
	  else {
		  System.out.println("Usage:\n"+
		  "slicer <input file> -<x|y|z> <offset> <output file>"); 
		  System.exit(0);
	  }
	  
	  
	  	  
  }

  public VolumeCross (String filename) {
      inImg = imageLoad(filename);
     // ImageObject multiImg;
//	try {
//		multiImg = ImageObject.add(inImg);
//		  Im2LearnNCSA f = new Im2LearnNCSA(multiImg);
//	      f.setVisible(true);
//	      
//	} catch (ImageException e1) {
//		e1.printStackTrace();
//	}
    
  }

  
  public void setParameterString(String param) {
	  parameter = param;	
  }
  
  public VolumeCross (ImageObject[] im) {
      inImg = im;
  }
  
  
  ImageObject[] getInputImage() {
	  return inImg;
  }
  
  
  ImageObject[] imageLoad(String filename) {
	  try {
		  return ANALYZELoader.readImages(filename);
	  }
	  catch(Exception e) {
		  e.printStackTrace();
	  }
	  return null;
  }
  
  
  public ImageObject getCrossSection () throws Exception {
	  int plane = 0;
	  float offset = 0;
	  
	  if(parameter == null) {
		  parameter = JOptionPane.showInputDialog("Input Slicer parameters\n e.g., x 0.5");
	  }
	  
	  String[] p2 = parameter.split(" ");
	  
	  if(p2[0].equalsIgnoreCase("x")) {
		  plane = this.X_PLANE;
	  }
	  else if(p2[0].equalsIgnoreCase("y")) {
		  plane = this.Y_PLANE;
	  }
	  else if(p2[0].equalsIgnoreCase("z")) {
		  plane = this.Z_PLANE;
	  }
	  else {
		  System.err.println("Invalid plane");
		  return null;
	  }
	  
	  try{
		  offset = Float.parseFloat(p2[1]);
	  }
	  catch(Exception e) {
		  System.err.println("Invalid offset");
		  return null;
	  }
	  
	  return getCrossSection(getInputImage(), plane, offset);
  }
  
  public ImageObject getCrossSection (int plane, float offset) throws Exception {
	  
    return getCrossSection(getInputImage(), plane, offset);
  }
  
  public String getPlaneString() {
	  switch(plane) {
	  case X_PLANE:
		  return "X";
	  case Y_PLANE:
		  return "Y";
	  case Z_PLANE:
		  return "Z";
	  }
	  return "Undefined";
  }
  
  public float getOffset() {
	  return offset;
  }

  public ImageObject getCrossSection (ImageObject[] imObj, int plane, float offset) throws Exception {
	  
	  this.plane = plane;
	  this.offset = offset;
	  
	  ImageObject retImObj = null;
	  int off;
	  
	  switch (plane) {
	  case X_PLANE:
		  off = (int)((float)imObj[0].getNumCols()*offset);
		  retImObj = ImageObject.createImage(imObj.length, imObj[0].getNumRows(), imObj[0].getNumBands(), imObj[0].getType());
		  for(int j=0; j<retImObj.getNumRows(); j++) {
			  for(int i=0; i<retImObj.getNumCols(); i++) {
				  for(int k=0; k<retImObj.getNumBands(); k++) {
					  retImObj.set(retImObj.getNumRows() - j -1, i, k, imObj[j].getByte(i, off, k));			  
				  }
			  }
		  }

		  break;
	  case Y_PLANE:
		  off = (int)((float)imObj[0].getNumRows()*(1-offset));
		  retImObj = ImageObject.createImage(imObj.length, imObj[0].getNumCols(), imObj[0].getNumBands(), imObj[0].getType());
		  for(int j=0; j<retImObj.getNumRows(); j++) {
			  for(int i=0; i<retImObj.getNumCols(); i++) {
				  for(int k=0; k<retImObj.getNumBands(); k++) {
					  retImObj.set(retImObj.getNumRows() - j -1, i, k, imObj[j].getByte(off, i, k));			  
				  }
			  }
		  }
	  
		  break;
	  case Z_PLANE:
		  off = (int)((float)imObj.length*(1-offset));
		  retImObj = ImageObject.createImage(imObj[0].getNumRows(), imObj[0].getNumCols(), imObj[0].getNumBands(), imObj[0].getType());
		  for(int j=0; j<retImObj.getNumRows(); j++) {
			  for(int i=0; i<retImObj.getNumCols(); i++) {
				  for(int k=0; k<retImObj.getNumBands(); k++) {
					  retImObj.set(retImObj.getNumRows() - j - 1,i,k,imObj[off].getByte(j,i,k));
				  }
			  }
		  }
		  
		  
		  break;
	  }
	  

    return retImObj;
  }



}
