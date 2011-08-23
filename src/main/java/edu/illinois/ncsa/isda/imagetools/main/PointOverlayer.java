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
package edu.illinois.ncsa.isda.imagetools.main;


import java.io.IOException;

import javax.swing.JFrame;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImPoint;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageException;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObjectFloat;
import edu.illinois.ncsa.isda.imagetools.core.datatype.Point2DDouble;
import edu.illinois.ncsa.isda.imagetools.core.datatype.Point3DDouble;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ShapeObject;
import edu.illinois.ncsa.isda.imagetools.core.io.ImageLoader;
import edu.illinois.ncsa.isda.imagetools.core.io.shapefile.ShapefileException;
import edu.illinois.ncsa.isda.imagetools.core.io.shapefile.ShapefileLoader;
import edu.illinois.ncsa.isda.imagetools.ext.misc.ImageFrame;
import edu.illinois.ncsa.isda.imagetools.ext.segment.DrawOp;




/**
 * Takes a given image and a given shapefile containing points, and overlays them.
 */

public class PointOverlayer {
    public static void main(String args[]) throws IOException, ShapefileException {
    	if (args.length != 3) {
    		System.out.println("CompareCoastlines [image] [shapefile] [armsize]");
    		System.exit(5);
    	}
    	int armSize = Integer.parseInt(args[2]);
    	Point3DDouble color = new Point3DDouble();
    	double colorCoords[] = {255, 0, 0};
    	color.setPoint3DDouble(1, colorCoords);
    	ImageObject image = ImageLoader.readImage(args[0]);
    	ShapefileLoader loader =  new ShapefileLoader(args[1]);
    	ShapeObject shape = loader.getShapeObject();
    	Point2DDouble points = shape.getAllBoundaryPoints();
    	int size = points.numpts;
    	double[] box = shape.getBoundingBox(0);
    	//System.out.printf("The bounding box coords are (%f,%f,%f,%f)\n", box[0],box[1],box[2],box[3]); 
    	//System.out.printf("The image size is (%d,%d)\n", image.getNumRows(), image.getNumCols());
    	DrawOp drawer = new DrawOp();
    	for(int i = 0; i < size; i++){
    		int row = (int) points.GetValueRow(i);
    		int col = (int) points.GetValueCol(i);
    		//System.out.printf("point = (%d,%d)\n", row, col);
    		ImPoint point = new ImPoint(box[3] + box[1] - row, col);
    		if(image.getNumBands()==1){
    			drawer.draw_crossDouble(image, point, armSize, 255);
    		} else {
    			drawer.draw_colorcrossDouble(image, point, armSize, color);
    		}
    		ImageLoader.writeImage(args[0], image);
    	}
    	displayImg(image, "Overlayed");
    	
    }

    
    private static void displayImg(ImageObject imgObject, String title){
    	ImageFrame imgfrm = new ImageFrame(title);
    	imgfrm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	imgfrm.setImageObject(imgObject);
    	imgfrm.pack();
    	imgfrm.setVisible(true);
    }
    	
}