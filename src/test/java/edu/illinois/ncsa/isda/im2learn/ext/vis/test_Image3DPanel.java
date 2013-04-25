package edu.illinois.ncsa.isda.im2learn.ext.vis;

import javax.swing.*;

import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.im2learn.core.io.FileChooser;
import edu.illinois.ncsa.isda.im2learn.core.io.ImageLoader;
import edu.illinois.ncsa.isda.im2learn.ext.vis.Image3DPanel;



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
class test_Image3DPanel {

  public static void main(String args[]) {

	  boolean pseudo = false;
  
	  
	  FileChooser file = new FileChooser();
	  String[] filename = null;
	  ImageObject[] imObj = null;
	  try{
		  filename = file.showMultiOpenDialog();
		  imObj = new ImageObject[filename.length];
		  
		  for(int i=0; i<filename.length; i++) {
			  System.out.println(filename[i]);
			  imObj[i] = ImageLoader.readImage(filename[i]);
		  }
	  }
	  catch(Exception e) {
		  
	  }
		  
	  Image3DPanel imDisp3D;
	  if (filename.length == 1) {
		  imDisp3D = new Image3DPanel(imObj[0],Image3DPanel.MODE_PSEUDOMAP);
		//  imDisp3D = new Image3DPanel(imObj[0],Image3DPanel.MODE_GRAYSCALE);
	  }
	  else {
		  imDisp3D = new Image3DPanel(imObj, pseudo);
	  }
	  
  
      JFrame f = new JFrame();
      f.getContentPane().add(imDisp3D.getCanvas());
      f.pack();
      f.show();

  }
}
