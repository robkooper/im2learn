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


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;

import javax.swing.JFrame;
import javax.swing.table.DefaultTableModel;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImLine;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImPoint;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageException;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObjectByte;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObjectFloat;
import edu.illinois.ncsa.isda.imagetools.core.datatype.Point2DDouble;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ShapeObject;
import edu.illinois.ncsa.isda.imagetools.core.io.ImageLoader;
import edu.illinois.ncsa.isda.imagetools.core.io.shapefile.ShapefileException;
import edu.illinois.ncsa.isda.imagetools.core.io.shapefile.ShapefileLoader;
import edu.illinois.ncsa.isda.imagetools.ext.geo.CSVTable;
import edu.illinois.ncsa.isda.imagetools.ext.geo.GeoFeature;
import edu.illinois.ncsa.isda.imagetools.ext.misc.ImRotation;
import edu.illinois.ncsa.isda.imagetools.ext.misc.ImageFrame;
import edu.illinois.ncsa.isda.imagetools.ext.misc.Morpho;
import edu.illinois.ncsa.isda.imagetools.ext.segment.ColorModels;
import edu.illinois.ncsa.isda.imagetools.ext.segment.DrawOp;



/**
 * Compare two coastlines.
 */
public class CompareCoastlines {
    public static void main(String args[]) throws Exception {
    	/*
    	ImageObject img = ImageLoader.readImage(args[0]);
    	img = img.convert(0, true);
    	displayImg(img,"ORIGINAL");
    	int rowSize = 5;
    	int colSize = 1;
    	Morpho morpher = new Morpho();
    	morpher.CreateMorphElem(rowSize, colSize);
    	morpher.MorphClose(img);
    	img = morpher.GetImageObject();
    	rowSize = 1;
    	colSize = 5;
    	Morpho morpher2 = new Morpho();
    	morpher2.CreateMorphElem(rowSize, colSize);
    	morpher2.MorphClose(img);
    	img = morpher2.GetImageObject();
    	displayImg(img,"CLOSED");
    	
    }
    */
    	/*
        ImageObject mask;
        if(args[1].equals("shp")){
        	ShapefileLoader loader =  new ShapefileLoader(args[0]);
            ShapeObject lake = loader.getShapeObject();     	
        	mask = findInterior(lake);
        	//addConstant(mask,1);    
            //scaleImg(mask,255);
            displayImg(mask,"TEST");
            return;
            //ImageLoader.writeImage("C:/Users/twshaw3/Desktop/DATA/Test_Data/segmented_maps/testbnd.png", mask);
            //scaleImg(mask,1.0/255);
        } else {
        	ImageObject tmp = ImageLoader.readImage(args[0]);
        	mask = tmp.extractBand(0);
        	ImageLoader.writeImage("C:/Users/twshaw3/Desktop/DATA/Test_Data/segmented_maps/testbnd.png", mask);
        }
        
        double area = findMoment(mask, 0, 0);
        System.out.println("area = " + area);
        double[] centroid = findCentroid(mask);
        System.out.println("xbar = " + centroid[0] + " ybar = " + centroid[1]);
        double[] huMoments1 = new double[7];
        double[] huMoments2 = new double[7];
    	long begin1 = System.currentTimeMillis();
        huMoments1 = huMoments(mask);
        long end1 = System.currentTimeMillis();
        long elapsed1 = end1-begin1;
        long begin2 = System.currentTimeMillis();
        huMoments2 = huMoments2(mask);
        long end2 = System.currentTimeMillis();
        long elapsed2 = end2-begin2;
    	
        printMoments(huMoments1);
        printMoments(huMoments2);
        
    	System.out.printf("Elapsed Time 1 = %d\n", elapsed1);
    	System.out.printf("Elapsed Time 2 = %d\n", elapsed2);
    }
    */
    	


    	
    	
    }
    	
    	
    	/*
    	String dataDir = args[0];
    	File dir = new File(dataDir);
    	String[] children = dir.list();
    	File imageDir = new File(args[1]);
    	String[] images = imageDir.list();
    	for (int i = 0; i < children.length; i++) {
    		String imageFilename = images[i];
    		String temp = imageFilename;
    		imageFilename = imageDir + "/" + imageFilename;
    		ImageObject image = ImageLoader.readImage(imageFilename);
    		int rows = image.getNumRows();
    		int cols = image.getNumCols();
	        String filename = children[i];
	        filename = dataDir + "/" + filename;
	        ShapefileLoader loader =  new ShapefileLoader(filename);
	        ShapeObject face = loader.getShapeObject();
	        ImageObject mask = findInterior2(face, rows, cols);
	        mask = mask.convert(6, true);
	        addConstant(mask,1);
	        scaleImg(mask,255);
	        //CompareCoastlines.complementBinaryImage(current);
	        ImageObject filledImage = ImageObject.createImage(rows+100, cols+100, 1, 0);
	        //displayImg(filledImage, "TEST");
	        for(int row = 0; row < rows; row++){
	        	for(int col = 0; col < cols; col++){
	        		byte pix = mask.getByte(row,col,0);
	        		filledImage.setByte(row+49,col+49,0,pix);
	        	}
	        }
	        
	        //displayImg(mask, filename);
	        ImageLoader.writeImage(args[2] + "/" + temp, filledImage);
    	}
    	
    }
    */
    	/*
    	if (args.length != 3) {
    		System.out.println("CompareCoastlines [dataset pathway] [conversion list text file] [output file]");
    		System.exit(5);
    	}
        try {
        	Hashtable conversionFactors = new Hashtable();
        	      //use buffering, reading one line at a time
        	      //FileReader always assumes default encoding is OK!
        	      BufferedReader input =  new BufferedReader(new FileReader(args[1]));
        	      try {

                	String name = null;
                	Double value = null;
        	        while ((name = input.readLine()) != null){
        	        	if((value = Double.parseDouble(input.readLine())) == null){
        	        		System.out.println("Invalid conversion file format!");
        	        		System.exit(5);
        	        	}
                        conversionFactors.put(name, value);
                        //System.out.println(value);
        	        }
        	      }
        	      finally {
        	        input.close();
        	      }

        	processDatasetArea(args[0], conversionFactors, args[2]);
        	
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }
    */
    	/*if (args.length != 4) {
    		System.out.println("CompareCoastlines [file1] [file2] numbins numbinsDot");
    		System.exit(5);
    	}
        try {
        	ShapefileLoader loader1 =  new ShapefileLoader(args[0]);
        	ShapefileLoader loader2 =  new ShapefileLoader(args[1]);
        	ShapeObject coastline1 = loader1.getShapeObject();
        	ShapeObject coastline2 = loader2.getShapeObject();
        	Point2DDouble coastline1_points = coastline1.getAllBoundaryPoints();
        	Point2DDouble coastline2_points = coastline2.getAllBoundaryPoints();
        	int size1 = coastline1_points.numpts;
        	int size2 = coastline2_points.numpts;
        	//System.out.printf("SIZE = %d\n", size);
        	
        	//System.out.println("numboundaryparts = " + coastline1.getNumBoundaryParts(0));

        	
        	double[] box1 = coastline1.getBoundingBox(0);
        	double[] box2 = coastline2.getBoundingBox(0);       	
        	
        	int height1 = (int)(box1[3]-box1[1]);
        	int width1 = (int)(box1[2]-box1[0]);
        	int height2 = (int)(box2[3]-box2[1]);
        	int width2 = (int)(box2[2]-box2[0]);
        	
        	ImageObject coastline1Image = ImageObject.createImage(height1+100, width1+100, 1, 0);      
        	ImageObject coastline2Image = ImageObject.createImage(height2+100, width2+100, 1, 0);          	
        	
        	//System.out.printf("The bounding box coords are (%f,%f,%f,%f)\n", box[0],box[1],box[2],box[3]); 
        	//coastline1_points.PrintPoint2DDoubleAllValues();
        	
        	double[] points1 = coastline1_points.ptsDouble;
        	double[] points2 = coastline2_points.ptsDouble;
        	
        	for(int index = 0; index < size1*2; index+=2){
        		int row = (height1-(int)(points1[index]-box1[1]))+50;
        		int col = (int)(points1[index+1]-box1[0])+50;
        		coastline1Image.set(row, col, 1, 255);
        	}

        	for(int index = 0; index < size2*2; index+=2){
        		int row = (height2-(int)(points2[index]-box2[1]))+50;
        		int col = (int)(points2[index+1]-box2[0])+50;
        		coastline2Image.set(row, col, 1, 255);
        	}
        	
        	
        	int numbins = Integer.parseInt(args[2]);
        	
        	// Normalized angle histograms
        	double[] histogram1 = new double[numbins];
        	double[] histogram2 = new double[numbins];
        	
        	int numbinsDot = Integer.parseInt(args[3]);
        	
        	// Dot product histograms
        	double[] dotHistogram1 = new double[numbinsDot];
        	double[] dotHistogram2 = new double[numbinsDot];
        	
        	double point1y, point1x, point2y, point2x, currentVecX, currentVecY;
        	double prevVecX, prevVecY, dotProduct, currentNorm, prevNorm;
        	int angle;
        	
        	prevVecX = 0;
    		prevVecY = 0;
    		prevNorm = 0;
        	
        	// Populate histograms
        	for(int index = 0; index < (size1-1)*2; index+=2){
        		point1y = points1[index];
        		point1x = points1[index+1];
        		point2y = points1[index+2];
        		point2x = points1[index+3];  
        		
        		currentVecX = point2x-point1x;
        		currentVecY = point2y-point1y;
        		currentNorm = Math.sqrt(currentVecX*currentVecX+currentVecY*currentVecY);
        		
        		angle = (int)(Math.atan((point2y-point1y)/(point2x-point1x))*180/Math.PI);
        		
        		if(index >= 2) {
        			dotProduct = (currentVecX*prevVecX + currentVecY*prevVecY)/(currentNorm*prevNorm);
        			int binDot = (int)((1-dotProduct)*numbinsDot*(1.0/2));
        			dotHistogram1[binDot] += 1.0/(size1-2);
        			//System.out.println("dot = " + dotProduct);
        			//System.out.println("bin = " + binDot);
        		}  
        		
        		// Angle should not depend on direction in which contour was traced
        		if(angle<0) angle += 180;
        		//System.out.println("angle = " + angle);
        		int bin = (int)((angle/180.0)*numbins);
        		//System.out.println("bin = " + bin);
        		histogram1[bin] += 1.0/(size1-1);
        		
        		prevVecX = currentVecX;
        		prevVecY = currentVecY;
        		prevNorm = Math.sqrt(prevVecX*prevVecX+prevVecY*prevVecY);
        	}
        	

        	
        	for(int index = 0; index < (size2-1)*2; index+=2){
        		point1y = points2[index];
        		point1x = points2[index+1];
        		point2y = points2[index+2];
        		point2x = points2[index+3];               		
        		
        		currentVecX = point2x-point1x;
        		currentVecY = point2y-point1y;
        		currentNorm = Math.sqrt(currentVecX*currentVecX+currentVecY*currentVecY);
        		
        		angle = (int)(Math.atan((point2y-point1y)/(point2x-point1x))*180/Math.PI);
        		
        		if(index >= 2) {
        			dotProduct = (currentVecX*prevVecX + currentVecY*prevVecY)/(currentNorm*prevNorm);
        			int binDot = (int)((1-dotProduct)*numbinsDot*(1.0/2));
        			dotHistogram2[binDot] += 1.0/(size2-2);
        			//System.out.println("dot = " + dotProduct);
        			//System.out.println("bin = " + binDot);
        		} 
        		
        		// Angle should not depend on direction in which contour was traced
        		if(angle<0) angle += 180;
        		//System.out.println("angle = "+angle);
        		int bin = (int)((angle/180.0)*numbins);
        		histogram2[bin] += 1.0/(size2-1);
        		
        		prevVecX = currentVecX;
        		prevVecY = currentVecY;
        		prevNorm = Math.sqrt(prevVecX*prevVecX+prevVecY*prevVecY);
        	}
        	
        	double euclideanDist = 0;
        	double euclideanDistDot = 0;
        	
        	// Compare angle histograms
        	for(int bin = 0; bin < numbins; bin++){
        		euclideanDist += Math.pow((histogram1[bin]-histogram2[bin]), 2);
        	}
        	//System.out.println("Distance = " + euclideanDist);
        	
        	// Compare angle difference histograms
        	for(int bin = 0; bin < numbinsDot; bin++){
        		euclideanDistDot += Math.pow((dotHistogram1[bin]-dotHistogram2[bin]), 2);
        	}

        	ImageObject connectedCoast1 = findCoastline(coastline1);
        	ImageObject connectedCoast2 = findCoastline(coastline2);
        	
        	// Get interior points
        	ImageObject interior1 = findInterior(coastline1);
        	ImageObject interior2 = findInterior(coastline2);
        	//displayImg(connectedCoast1, "Coastline 1");
        	//displayImg(connectedCoast2, "Coastline 2");
        	//displayImg(interior1, "Interior 1");
        	//displayImg(interior2, "Interior 2");
        	//ImageLoader.writeImage("interior_test.tif", interior2);
        	
        	// Area experiment
        	double milesPerPixelModern = 40/253.0;
        	double areaModern = area(coastline1, milesPerPixelModern);
        	//System.out.println("The area of Lake Ontario according to the modern map is " + areaModern);
        	double milesPerPixelOld = 151/Math.sqrt((206-48)*(206-48)+(748-164)*(748-164));
        	double areaOld = area(coastline2, milesPerPixelOld);
        	//System.out.println("The area of Lake Ontario according to the 1801 map is " + areaOld);
        	
        	// Shore length experiment
        	double lengthModern = shoreLength(coastline1, milesPerPixelModern);
        	double lengthOld = shoreLength(coastline2, milesPerPixelOld);
        	//System.out.println("The shore line length of Lake Ontario according to the modern map is " + lengthModern);
        	//System.out.println("The shore line length of Lake Ontario according to the 1801 map is " + lengthOld);   
        	
        	
        	// Change labels from -1 and 0 to 0 and 1 for no class and class respectively.


        	interior1 = interior1.convert(6, true);
        	interior2 = interior2.convert(6, true);
        	addConstant(interior1,1);
        	//scaleImg(interior1,128);
        	addConstant(interior2,1);
        	//scaleImg(interior2,128);
        	//drawOrientation(interior1);
        	//drawOrientation(interior2);
        	//displayImg(interior1, "shape 1");
        	//displayImg(interior2, "Shape 2");
        	//findEccentricity(interior1);
        	//findEccentricity(interior2);
        	
        	
        	
        	// Test centroid
        	//ImageLoader.writeImage("boundary_test.tif", connectedCoast1);
        	//double centroid[] = findCentroid(interior2);
        	//System.out.println("The centroid of the old map is (" + centroid[0] + "," + centroid[1] + ").");   
        	//double orientation1 = findOrientation(interior1)*180.0/Math.PI;
        	//double orientation2 = findOrientation(interior2)*180.0/Math.PI;
        	//System.out.println("angles are " + orientation1 + " and " + orientation2);
        	//System.out.println("The orientation of the old map is " + orientation);

        	int bands = interior1.getNumBands();
        	//ImageLoader.writeImage("testing1.jpg", interior1);
        	
        	// Test Hu moments
        	displayImg(interior1, "Original");
        	double[] hu = huMoments(interior1);
        	for(int i = 0; i <= 6; i++){
        		System.out.println("Hu moment " + i + " = " + hu[i]);
        	}
        	ImRotation rotator = new ImRotation();
        	ImageObject rotatedCoast = rotator.RotateImage(interior1, 45);
        	displayImg(rotatedCoast, "Rotated");
        	hu = huMoments(rotatedCoast);
        	for(int i = 0; i <= 6; i++){
        		System.out.println("Hu moment " + i + " = " + hu[i]);
        	}
        	
        	
        	//System.out.println("HANDA");
        	//analyzeDataSet(args[0]);
        	//System.out.println("HANDB");
        	//analyzeDataSet(args[1]);
        	
        	//http://en.wikipedia.org/wiki/Lake_Ontario
        } catch (Exception exc) {
            exc.printStackTrace();
        }
        
    }
    	 */
    
    public static void printMoments(double[] moments){
    	for(int i = 0; i < moments.length; i++){
    		System.out.println("moment " + i + " = " + moments[i]);
    	}
    }
    
    // Computes the areas of all shapes in a given directory
    public static void processDatasetArea(String dataDir, Hashtable conversionFactors, String outFile) throws IOException, ShapefileException, ImageException{
    	File dir = new File(dataDir);
    	String[] children = dir.list();
	    BufferedWriter output = new BufferedWriter(new FileWriter(outFile));
	    for (int i = 0; i < children.length; i++) {
	    	System.out.println("NAME = " + children[i]);
	    	ShapefileLoader loader =  new ShapefileLoader(dataDir + "/" + children[i]);
	    	ShapeObject shape = loader.getShapeObject();
	    	double area = area(shape, (Double)conversionFactors.get(children[i].substring(0, children[i].lastIndexOf('.'))));
	    	System.out.println("AREA = " + area);
            output.write(children[i]);
            output.newLine();
            output.write(Double.toString(area));
            output.newLine();
	    }
	    output.close();
    }
    
    public static void processDatasetArea2(String dataDir, Hashtable conversionFactors, String outFile) throws IOException, ShapefileException, ImageException{
    	File dir = new File(dataDir);
    	String[] children = dir.list();
	    BufferedWriter output = new BufferedWriter(new FileWriter(outFile));
	    for (int i = 0; i < children.length; i++) {
	    	System.out.println("NAME = " + children[i]);
	    	ImageObject mask = ImageLoader.readImage(dataDir + "/" + children[i]);
	    	double area = area2(mask, (Double)conversionFactors.get(children[i].substring(0, children[i].lastIndexOf('_'))));
	    	System.out.println("AREA = " + area);
            output.write(children[i].substring(0, children[i].lastIndexOf('.'))+",");
            //output.newLine();
            output.write(Double.toString(area));
            output.newLine();
	    }
	    output.close();
    }

   
    public static void addConstant(ImageObject image, double constant) throws ImageException{
    	for(int row = 0; row < image.getNumRows(); row++){
    		for(int col = 0; col < image.getNumCols(); col++){
    			double pixel = image.getDouble(row, col, 0);
    			image.set(row,col, 0, pixel + constant);
    		}
    	}
    }
    
    public static void scaleImg(ImageObject image, double scale){
    	for(int row = 0; row < image.getNumRows(); row++){
    		for(int col = 0; col < image.getNumCols(); col++){
    			double pix = image.getDouble(row,col,0);
    			image.setDouble(row,col,0,pix*scale);
    		}
    	}
    }
    
    public static ImageObject scaleBinImg(ImageObject image, int scale) throws ImageException{
    	ImageObject result = ImageObject.createImage(image.getNumRows(), image.getNumCols(), image.getNumBands(), 0);
    	for(int row = 0; row < image.getNumRows(); row++){
    		for(int col = 0; col < image.getNumCols(); col++){
    			double pix = image.getInt(row,col,0);
    			if(pix != 0){
    				result.setInt(row,col,0,scale);
    			} else {
    				result.setInt(row,col,0,0);
    			}
    		}
    	}
    	return result;
    	
    }
    
    public static void maskToBin(ImageObject mask, double thresh){
    	for(int row = 0; row < mask.getNumRows(); row++){
    		for(int col = 0; col < mask.getNumCols(); col++){
    			double pix = mask.getDouble(row,col,0);
    			if(pix > thresh){ 				
    				mask.setDouble(row,col, 0,255);
    			} else {
    				mask.setDouble(row,col, 0,0);
    			}
    		}
    	}
    }
    
    private static double shoreLength(ShapeObject shp, double milesPerPix){
    	return pixelShoreLength(shp)*milesPerPix;
    }
    
    public static void complementBinaryImage(ImageObject image){
    	for(int row = 0; row < image.getNumRows(); row++){
    		for(int col = 0; col < image.getNumCols(); col++){
    			double pix = image.getDouble(row,col,0);
    			if(pix == 0){
    				image.set(row,col,0,1);
    			} else {
    				image.set(row,col, 0,0);
    			}
    		}
    	}
    }
    
    private static double pixelShoreLength(ShapeObject coastlinePoints){
    	double length = 0;
    	int size = coastlinePoints.getNumBoundaryPts(0);
    	Point2DDouble locus = coastlinePoints.getAllBoundaryPoints();
    	double[] points = locus.ptsDouble;
    	for(int index = 0; index < size*2; index = index+=2){
    		double startPointy = points[index];
    		double startPointx = points[index+1];
    		double endPointy = points[(index+2)%(size*2)];
    		double endPointx = points[(index+3)%(size*2)];
    		double dy = endPointy - startPointy;
    		double dx = endPointx - startPointx;
    		double currentLength = Math.sqrt(dy*dy+dx*dx);
    		length += currentLength;
    	}
    	return length;
    }
    
    
    private static double area(ShapeObject shp, double milesPerPix) throws ImageException{
    	return Math.pow(milesPerPix, 2)*pixelArea(shp);
    }
    
    private static double area2(ImageObject img, double milesPerPix) throws ImageException{
    	return Math.pow(milesPerPix, 2)*pixelArea(img);
    }
    
    
    private static double pixelArea(ShapeObject shp) throws ImageException{
    	return pixelArea(findInterior(shp));
    }
    
    public static ImageObject findInterior(ShapeObject shp) throws ImageException{
    	double[] box = shp.getBoundingBox(0);
    	ImageObject dummy = ImageObject.createImage((int)(box[3]+box[1]), 
				(int)(box[2]+box[0]), 1, 1, true);  
    	System.out.println("box = " + box[0] + "," + box[1] + "," + box[2] + "," + box[3]);
    	GeoFeature gf = new GeoFeature();
    	ImRotation flipper = new ImRotation();
    	ImageObject mask = gf.ConstructMaskImageOut(dummy, shp);
    	// Mask will be upside down
    	flipper.FlipHoriz(mask);
    	//displayImg(mask, "mask");
    	return mask;
    }
    
    public static ImageObject findInterior2(ShapeObject shp, int numRows, int numCols) throws ImageException{
    	double[] box = shp.getBoundingBox(0);
    	ImageObject dummy = ImageObject.createImage(numRows, numCols, 1, 1, true);  
    	System.out.println("box = " + box[0] + "," + box[1] + "," + box[2] + "," + box[3]);
    	GeoFeature gf = new GeoFeature();
    	ImRotation flipper = new ImRotation();
    	ImageObject mask = gf.ConstructMaskImageOut(dummy, shp);
    	// Mask will be upside down
    	
    	flipper.FlipHoriz(mask);
    	mask = shiftImVert(mask,(int)(box[3]+box[1]-numRows));
    	return mask;
    }
    
    private static ImageObject shiftImVert(ImageObject image, int offset) throws ImageException{
    	int numRows = image.getNumRows();
    	int numCols = image.getNumCols();
    	int numBands = image.getNumBands();
    	int type = image.getType();
    	System.out.println("offset = " + offset);
    	ImageObject output = ImageObject.createImage(numRows, numCols, numBands, type);
    	for(int row = 0; row < numRows; row++){
    		for(int col = 0; col < numCols; col++){
    			if(row>=offset && row<numRows+offset){
	    			double pix = image.getDouble(row-offset,col,0);
	    			output.set(row, col, 0,pix);
    			} else {
    				output.set(row,col, 0,255.0);
    			}
    		}
    	}
    	return output;
    }
    
    public static double computeMSE(ImageObject img1, ImageObject img2, double normalization,String filename) throws ImageException, IOException{
    	double MSE = 0;
    	// Images must have same size
    	int numRows = img1.getNumRows();
    	int numCols = img1.getNumCols();
    	ImageObject diffImg = ImageObject.createImage(numRows, numCols, 1, 6);
    	for(int row = 0; row < numRows; row++){
    		for(int col = 0; col < numCols; col++){
    			double pix1 = img1.getDouble(row,col,0);
    			double pix2 = img2.getDouble(row,col,0);
    			double diff = pix1-pix2;
    			MSE += diff*diff;
    			diffImg.setDouble(row,col,0,Math.abs(pix1-pix2));
    		}
    	}
    	ImageLoader.writeImage(filename, diffImg);
    	//double size = numRows*numCols;
    	return MSE/(normalization*255*255);
    }
    
    private static int pixelArea(ImageObject imgObject){
    	int area = 0;
    	for(int row = 0; row < imgObject.getNumRows(); row++){
    		for(int col = 0; col < imgObject.getNumCols(); col++){
    			if(imgObject.getInt(row, col, 0) != 0){
    				area++;
    			}
    		}
    	}
    	return area;
    }
    
    private static void displayImg(ImageObject imgObject, String title){
    	ImageFrame imgfrm = new ImageFrame(title);
    	imgfrm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	imgfrm.setImageObject(imgObject);
    	imgfrm.pack();
    	imgfrm.setVisible(true);
    }
    
    private static ImageObject findCoastline(ShapeObject coastlinePoints) throws ImageException{
    	int size = coastlinePoints.getNumBoundaryPts(0);
    	Point2DDouble locus = coastlinePoints.getAllBoundaryPoints();
    	double[] points = locus.ptsDouble;
    	double[] box = coastlinePoints.getBoundingBox(0);      	 	
    	int height = (int)(box[3]-box[1]);
    	int width = (int)(box[2]-box[0]);
    	ImageObject result = ImageObject.createImage(height + 100, width + 100, 1, 0);
    	for(int index = 0; index < size*2; index = index+=2){
    		double startPointy = points[index];
    		double startPointx = points[index+1];
    		double endPointy = points[(index+2)%(size*2)];
    		double endPointx = points[(index+3)%(size*2)];
    		double dy = endPointy - startPointy;
    		double dx = endPointx - startPointx;
    		double length = Math.sqrt(dy*dy+dx*dx);
    		double currentAngle = Math.atan2(dy,dx);
    		double currentx = startPointx;
    		double currenty = startPointy;
    		double dl = 0.5;
    		for(double l = 0; l < length; l += dl){
    			currentx += dl*Math.cos(currentAngle);
    			currenty += dl*Math.sin(currentAngle);
    			int nearestRow = (height-(int)(currenty-box[1]))+50;
    			int nearestCol = (int)(currentx-box[0])+50;
    			result.set(nearestRow,nearestCol,0,255);
    		}
    	}
    	return result;
    }
    
    private static double[] findCentroid(ImageObject image) throws ImageException {
    	double[] centroid = new double[2];
    	double zerothMoment = findMoment(image,0,0);
    	centroid[0] = findMoment(image,1,0)/zerothMoment;
    	centroid[1] = findMoment(image,0,1)/zerothMoment;
    	return centroid;
    }
    
    private static double findMoment(ImageObject image, int i, int j){
    	double moment = 0;
    	for(int row = 0; row < image.getNumRows(); row++){
    		for(int col = 0; col < image.getNumCols(); col++){
    			double pix = image.getDouble(row,col,0);
    			moment += Math.pow(row, i)*Math.pow(col, j)*pix;
    		}
    	}
    	return moment;
    }
    
    // Efficiently find the low order moments of an image needed to calculate the Hu Moments
    // Returns them in a vector: (02,20,11,12,21,30,03)
    private static double[] findMoments(ImageObject image) throws IOException, ImageException{
    	double[] centMoments = new double[7];
    	double[] rescCentMoments = new double[7];
    	// First, find a point on the boundary
    	int numRows = image.getNumRows();
    	int numCols = image.getNumCols();
    	int bndRow = 1;
    	int bndCol = 1;
    	for(int r = 1; r<(numRows-1); r++){
	    	for(int c = 1; c<(numCols-1); c++){
	    		if(image.getDouble(r+1,c,0)==0.0 && image.getDouble(r,c,0)!=0){
	    			bndRow = r;
	    			bndCol = c;
	    			break;
	    		}
    		}
    	}
    	
    	System.out.println("(row,col) = " + "(" + bndRow + "," + bndCol + ")");
    	
    	// u array
    	double[][] u = {{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0}};
    	double[][] m = {{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0},{0, 0, 0, 0}};
    	double[][] transmat = {{1.0, 0, 0, 0},{0.5, 0.5, 0, 0},{1.0/6, 0.5, 1.0/3, 0},{0, 0.25, 0.5, 0.25}};
    	// Perform bug line-following
    	int y = bndRow;
    	int x = bndCol;
    	// Initialize orientation to down
    	int xorient = 0;
    	int yorient = 1;
    	while(true){		
    		x += xorient; // Move the bug
    		y += yorient;
    		double pix = image.getDouble(y,x,0);
    		double rightPix = image.getDouble(y,x+1,0);
    		int tmp = xorient;
    		if(pix==0.0){ // In background
    			xorient = -yorient; // Turn right
    			yorient = tmp;
    			if(rightPix != 0.0 && (yorient == -1 || xorient == 1)){//&& (x!=xprev1 || y!=yprev1)){
    				for(int i = 0; i < 4; i++){
    					for(int j = 0; j < 4; j++){
    						u[i][j] += Math.pow(x,i+1)*Math.pow(y,j)*(-1);
    					}
    				}
    			}
    		} else { // In object
    			xorient = yorient; // Turn left
    			yorient = -tmp;
    			if(rightPix == 0.0 && (yorient == 1 || xorient == 1)){
    				for(int i = 0; i < 4; i++){
    					for(int j = 0; j < 4; j++){
    						u[i][j] += Math.pow(x,i+1)*Math.pow(y,j);
    					}
    				}
    			}
    			
    		}
    		if(y == bndRow && x == bndCol && xorient == 0 && yorient == 1) break; // Back to original pixel
    	}
    	for(int i = 0; i < 4; i++){
    		for(int j = 0; j < 4; j++){
    			double accum = 0;
    			for(int k = 0; k < 4; k++){
    				accum += u[k][j]*transmat[i][k];
    			}
    			m[i][j] = accum;
    		}
    	}
    	double xbar = m[1][0]/m[0][0];
    	double ybar = m[0][1]/m[0][0];
    	centMoments[0] = m[0][2]-ybar*m[0][1];
    	centMoments[1] = m[2][0]-xbar*m[1][0];
    	centMoments[2] = m[1][1]-xbar*m[0][1];
    	centMoments[3] = m[1][2]-2*ybar*m[1][1]-xbar*m[0][2]+2*ybar*ybar*m[1][0];
    	centMoments[4] = m[2][1]-2*xbar*m[1][1]-ybar*m[2][0]+2*xbar*xbar*m[0][1];
    	centMoments[5] = m[3][0]-3*xbar*m[2][0]+2*xbar*xbar*m[1][0];
    	centMoments[6] = m[0][3]-3*ybar*m[0][2]+2*ybar*ybar*m[0][1];
    	
    	rescCentMoments[0] = rescaleCentralMoment(centMoments[0], m[0][0], 0, 2);
    	rescCentMoments[1] = rescaleCentralMoment(centMoments[1], m[0][0], 2, 0);
    	rescCentMoments[2] = rescaleCentralMoment(centMoments[2], m[0][0], 1, 1);
    	rescCentMoments[3] = rescaleCentralMoment(centMoments[3], m[0][0], 1, 2);
    	rescCentMoments[4] = rescaleCentralMoment(centMoments[4], m[0][0], 2, 1);
    	rescCentMoments[5] = rescaleCentralMoment(centMoments[5], m[0][0], 3, 0);
    	rescCentMoments[6] = rescaleCentralMoment(centMoments[6], m[0][0], 0, 3);
    	return rescCentMoments;
    }
    
    private static double rescaleCentralMoment(double moment, double zerothMoment, int i, int j){
    	return moment/Math.pow(zerothMoment,1+(i+j)/2.0);
    }
    
    private static double findCentralMoment(ImageObject image, int i, int j) throws ImageException{
    	double centralMoment = 0;
    	double[] centroid = findCentroid(image);
    	double centroidRow = centroid[0];
    	double centroidCol = centroid[1];
    	//System.out.println("(i,j) = " + "("+i+","+j+")");
    	for(int row = 0; row < image.getNumRows(); row++){
    		for(int col = 0; col < image.getNumCols(); col++){
    			double pix = image.getDouble(row,col,0);
    			centralMoment += Math.pow(centroidRow-row, j)*Math.pow(col-centroidCol, i)*pix;
    			//System.out.println("Thingy = " + (int)Math.pow(row-centroidRow, i)*(int)Math.pow(col-centroidCol, j)*pix);
    		}
    	}
    	return centralMoment;
    }
    
    //Notation
    private static double mu(ImageObject image, int i, int j) throws ImageException{
    	return findCentralMoment(image, i, j);
    }
    
    private static double findScaledCentralMoment(ImageObject image, int i, int j) throws ImageException{
    	double centralMoment = findCentralMoment(image,i,j);
    	double zerothMoment = findMoment(image,0,0);
    	//System.out.println("M00 = " + zerothMoment);
    	return centralMoment/Math.pow(zerothMoment,1+(i+j)/2.0);   	
    }
    
    
    // Notation
    private static double eta(ImageObject image, int i, int j) throws ImageException{
    	return findScaledCentralMoment(image,i,j);
    }
    
    // Faster for calculating individual moments separately
    /**
     * Faster for calculating individual moments separately
     * @param input ImageObject
     * @return double value of a Hu moment
     * 
     */
    public static double huMoment(ImageObject image, int k) throws ImageException{
    	switch(k){
    	case 1:
    		return eta(image,0,2)+eta(image,2,0);
    	case 2:
    		return Math.pow(eta(image,2,0)-eta(image,0,2),2) + (Math.pow(2*eta(image,1,1),2));
    	case 3:
    		return Math.pow(eta(image,3,0)-3*eta(image,1,2),2)+Math.pow(3*eta(image,2,1)-eta(image,0,3),2);
    	case 4:
    		return Math.pow(eta(image,3,0)+eta(image,1,2),2)+Math.pow(eta(image,2,1)+eta(image,0,3),2);
    	case 5:
    		return (eta(image,3,0)-3*eta(image,1,2))*(eta(image,3,0)+eta(image,1,2))*(Math.pow(eta(image,3,0)+eta(image,1,2),2)-3*Math.pow(eta(image,2,1)+eta(image,0,3),2))
    				+ (3*eta(image,2,1)-eta(image,0,3))*(eta(image,2,1)+eta(image,0,3))*(3*Math.pow(eta(image,3,0)+eta(image,1,2),2)-Math.pow(eta(image,2,1)+eta(image,0,3),2));
    	case 6:
    		return (eta(image,2,0)-eta(image,0,2))*(Math.pow(eta(image,3,0)+eta(image,1,2),2)-Math.pow(eta(image,2,1)+eta(image,0,3),2))
    				+ 4*eta(image,1,1)*(eta(image,3,0)+eta(image,1,2))*(eta(image,2,1)+eta(image,0,3));
    	case 7:
    		return (3*eta(image,2,1)-eta(image,0,3))*(eta(image,3,0)+eta(image,1,2))*(Math.pow(eta(image,3,0)+eta(image,1,2),2)-3*Math.pow(eta(image,2,1)+eta(image,0,3),2))
					- (eta(image,3,0)-3*eta(image,1,2))*(eta(image,2,1)+eta(image,0,3))*(3*Math.pow(eta(image,3,0)+eta(image,1,2),2)-Math.pow(eta(image,2,1)+eta(image,0,3),2));
    	default:
    		System.out.println("Invalid k!");
			return 0;
    	}
    }
    
    // Faster for calculating all seven moments
    public static double[] huMoments(ImageObject image) throws ImageException{
    	double[] huFeatureVec = new double[7];
    	double e02 = eta(image,0,2);
    	double e20 = eta(image,2,0);
    	double e11 = eta(image,1,1);
    	double e12 = eta(image,1,2);
    	double e21 = eta(image,2,1);
    	double e30 = eta(image,3,0);
    	double e03 = eta(image,0,3);
    	//System.out.println("TEST 2 = " + e03);
    	huFeatureVec[0] = e02+e20;
    	huFeatureVec[1] = Math.pow(e20-e02,2) + Math.pow(2*e11,2);
    	huFeatureVec[2] = Math.pow(e30-3*e12,2) + Math.pow(3*e21-e03,2);
    	huFeatureVec[3] = Math.pow(e30+e12,2) + Math.pow(e21+e03,2);
    	huFeatureVec[4] = (e30-3*e12)*(e30+e12)*(Math.pow(e30+e12,2)-3*Math.pow(e21+e03,2))
						+ (3*e21-e03)*(e21+e03)*(3*Math.pow(e30+e12,2)-Math.pow(e21+e03,2));
		huFeatureVec[5] = (e20-e02)*(Math.pow(e30+e12,2)-Math.pow(e21+e03,2))
						+ 4*e11*(e30+e12)*(e21+e03);
		huFeatureVec[6] = (3*e21-e03)*(e30+e12)*(Math.pow(e30+e12,2)-3*Math.pow(e21+e03,2))
						- (e30-3*e12)*(e21+e03)*(3*Math.pow(e30+e12,2)-Math.pow(e21+e03,2));
    	return huFeatureVec;
    }
    
    // Improved Hu Moment Calculation
    public static double[] huMoments2(ImageObject image) throws ImageException, IOException{
    	double[] huFeatureVec = new double[7];
    	double[] momentVec = findMoments(image);
    	double e02 = momentVec[0];
    	double e20 = momentVec[1];
    	double e11 = momentVec[2];
    	double e12 = momentVec[3];
    	double e21 = momentVec[4];
    	double e30 = momentVec[5];
    	double e03 = momentVec[6];
    	huFeatureVec[0] = e02+e20;
    	huFeatureVec[1] = Math.pow(e20-e02,2) + Math.pow(2*e11,2);
    	huFeatureVec[2] = Math.pow(e30-3*e12,2) + Math.pow(3*e21-e03,2);
    	huFeatureVec[3] = Math.pow(e30+e12,2) + Math.pow(e21+e03,2);
    	huFeatureVec[4] = (e30-3*e12)*(e30+e12)*(Math.pow(e30+e12,2)-3*Math.pow(e21+e03,2))
						+ (3*e21-e03)*(e21+e03)*(3*Math.pow(e30+e12,2)-Math.pow(e21+e03,2));
		huFeatureVec[5] = (e20-e02)*(Math.pow(e30+e12,2)-Math.pow(e21+e03,2))
						+ 4*e11*(e30+e12)*(e21+e03);
		huFeatureVec[6] = (3*e21-e03)*(e30+e12)*(Math.pow(e30+e12,2)-3*Math.pow(e21+e03,2))
						- (e30-3*e12)*(e21+e03)*(3*Math.pow(e30+e12,2)-Math.pow(e21+e03,2));
    	return huFeatureVec;
    }
    
    private static double findOrientation(ImageObject image) throws ImageException{
    	double mu20p = mu(image,2,0)/mu(image,0,0);
    	//System.out.println("mu20p = " + mu20p);
    	double mu02p = mu(image,0,2)/mu(image,0,0);
    	//System.out.println("mu02p = " + mu02p);
    	double mu11p = mu(image,1,1)/mu(image,0,0);
    	//System.out.println("mu11p = " + mu11p);
    	return (1/2.0)*Math.atan2(2*mu11p,mu20p-mu02p);
    }
    
    private static void drawOrientation(ImageObject image) throws ImageException{
    	DrawOp drawer = new DrawOp();
    	double length = 50;
    	double angle = findOrientation(image);
    	//System.out.println("angle = " + angle);
    	double centroid[] = findCentroid(image);
    	double x1 = centroid[0];
    	double y1 = centroid[1];
    	double x2 = x1 - length*Math.sin(angle);
    	double y2 = y1 + length*Math.cos(angle);
    	ImPoint point1 = new ImPoint(x1,y1);
    	ImPoint point2 = new ImPoint(x2,y2);
    	ImLine line = new ImLine(point1, point2);
    	drawer.draw_line(image, 0, 0, line);
    }
    
    private static double findEccentricity(ImageObject image) throws ImageException{
    	double mu20p = mu(image,2,0)/mu(image,0,0);
    	double mu02p = mu(image,0,2)/mu(image,0,0);
    	double mu11p = mu(image,1,1)/mu(image,0,0);
    	double lambda1 = (1/2.0)*((mu20p + mu02p) + Math.sqrt(4*mu11p*mu11p+Math.pow(mu20p-mu02p,2)));
    	double lambda2 = (1/2.0)*((mu20p + mu02p) - Math.sqrt(4*mu11p*mu11p+Math.pow(mu20p-mu02p,2)));
    	//System.out.println("e = " + Math.sqrt(1-lambda2/lambda1));    	
    	return Math.sqrt(1-lambda2/lambda1);
    }
    
    private static void analyzeDataSet(String pathway) throws IOException, ShapefileException, ImageException{
    	File dir = new File(pathway);
    	String[] children = dir.list();
    	double e[] = new double[children.length];
    	for (int i = 0; i < children.length; i++) {
    		String filename = children[i];
    		filename = dir + "/" + filename;
    		ShapefileLoader loader =  new ShapefileLoader(filename);
        	ShapeObject current = loader.getShapeObject();
        	ImageObject currentImg = CompareCoastlines.findInterior(current);
        	currentImg = currentImg.convert(6, true);
        	CompareCoastlines.addConstant(currentImg,1);
        	e[i] = findEccentricity(currentImg);
        	drawOrientation(currentImg);
        	displayImg(currentImg, children[i] + " : " + Double.toString(e[i]));
        	//String resultDirPath = "eccentricities";
        	//File resultDir = new File(resultDirPath);
        	//resultDir.mkdir();
        	//scaleImg(currentImg,255);
        	//ImageLoader.writeImage(resultDirPath + "/" + children[i] + ".tif", currentImg);
    	}
    	double mean = 0;
    	double var = 0;
    	for(int i = 0; i < e.length; i++){
    		mean += e[i]/e.length;
    	}
    	for(int i = 0; i < e.length; i++){
    		var += Math.pow(e[i]-mean,2);
    	}
    	System.out.println("mean = " + mean);
    	System.out.println("var = " + var);
    }
    
    
}
