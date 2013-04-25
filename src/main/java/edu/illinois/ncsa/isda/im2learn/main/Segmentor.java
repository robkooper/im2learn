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
package edu.illinois.ncsa.isda.im2learn.main;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Hashtable;

import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import edu.illinois.ncsa.isda.im2learn.core.datatype.ImLine;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImPoint;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageException;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObjectFloat;
import edu.illinois.ncsa.isda.im2learn.core.datatype.Point2DDouble;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ShapeObject;
import edu.illinois.ncsa.isda.im2learn.core.datatype.SubArea;
import edu.illinois.ncsa.isda.im2learn.core.io.ImageLoader;
import edu.illinois.ncsa.isda.im2learn.core.io.shapefile.ShapefileException;
import edu.illinois.ncsa.isda.im2learn.core.io.shapefile.ShapefileLoader;
import edu.illinois.ncsa.isda.im2learn.ext.conversion.PCA;
import edu.illinois.ncsa.isda.im2learn.ext.geo.CSVTable;
import edu.illinois.ncsa.isda.im2learn.ext.geo.GeoFeature;
import edu.illinois.ncsa.isda.im2learn.ext.math.GeomOper;
import edu.illinois.ncsa.isda.im2learn.ext.misc.ImRotation;
import edu.illinois.ncsa.isda.im2learn.ext.misc.ImageFrame;
import edu.illinois.ncsa.isda.im2learn.ext.misc.Morpho;
import edu.illinois.ncsa.isda.im2learn.ext.segment.ColorModels;
import edu.illinois.ncsa.isda.im2learn.ext.segment.DrawOp;
import edu.illinois.ncsa.isda.im2learn.ext.segment.Seg2DBall;


/**
 * Segment images in the given directory. Images must be cropped so that the target object
 * is roughly centered, and takes up between 15% and 85% of the final image. The first argument
 * is the directory containing the dataset to be segmented; the second argument is a shapefile 
 * which contains a single pre-segmented image to act as an example
 * 
 * If mode = "seg", does as described above.
 * If mode = "stat", uses given images with given masks to compute color space statistics.
 */
public class Segmentor {
    public static void main(String args[]) throws IOException, ImageException {
    	/*
    	ImageObject img = ImageLoader.readImage(args[0]);
    	img = expandBoundaries(img,100,100);
    	ImageLoader.writeImage(args[0],img);
    	*/
    	
    	if (args.length > 15 || args.length < 1) {
    		System.out.println("Segmentor seg [DatasetPathway] [SampleImage] [ResultPathway]"
    							+ " or stat [ImagePathway1] [MasksPathway1] [ImagePathway2] [MasksPathway2] [ResultPathway]");
    		System.exit(5);
    	}
        try {
        	//String resultDirPath = "Test_Data/ArmorSegTest/864_seg";
        	//File resultDir = new File(resultDirPath);
        	//resultDir.mkdir();
        	int mode = 0;
        	if(args[0].equals("seg")) mode = 1;
        	if(args[0].equals("stat")) mode = 2;
        	if(args[0].equals("segval")) mode = 3;
        	if(args[0].equals("post")) mode = 4;
        	if(args[0].equals("ML")) mode = 5;
        	if(args[0].equals("special")) mode = 6;
        	if(args[0].equals("features")) mode = 7;
        	if(args[0].equals("mse")) mode = 8;
        	if(args[0].equals("area")) mode = 9;
        	if(args[0].equals("ascript")) mode = 10;
        	if(args[0].equals("super")) mode = 11;
        	if(args[0].equals("synthetic")) mode = 12;
        	if(args[0].equals("borderstats")) mode = 13;
        	switch(mode){
	        	case 1:
	        		segByShape(args[1],args[2],args[3],args[4],args[5], null, null, null);
	        	break;
	        	case 2:
	        		computeStatistics(args[1],args[2],args[3], args[4], args[5], args[6], args[7], args[8], args[9]);
	        	break;
	        	case 3:
	        		segByValue(args[1], args[2]);
	        	break;
	        	case 4:
	        		removeHoles(args[1]);
	        	break;
	        	case 5:
	        		mlExpt(args[1],args[2]);
	        	break;
	        	case 6:
	        		//specialScript();
	        	break;
	        	case 7:
	        		featureScript();
	        	break;
	        	case 8:
	        		evaluatePerformance(args[1],args[2],args[3]);
	        	break;
	        	case 9:
	        		computeAreas(args[1],args[2],args[3]);
	        	break;
	        	case 10:
	        		//areaScript();
	        	break;
	        	case 11:
	        		superScript(args[1]);
	        	break;
	        	case 12:
	        		syntheticScript(args[1]);
	        	break;
	        	case 13:
	        		analyzeBorderStats(args[1],args[2],args[3]);
	        	break;
	        	default:
	        		System.out.println("Mode parameter is invalid.");
	        		System.exit(5);	
        	}
        	
        } catch (Exception exc) {
	        exc.printStackTrace();
	    }
	    
    }
    
    // Save mean, std for segmentation algorithm
    // Make final masks same size 
    private static void analyzeBorderStats(String pathName, String destPathName, String downSampleRate) throws IOException, ShapefileException, ImageException{
    	File dir = new File(pathName);
    	String[] children = dir.list();
    	int dsr = Integer.parseInt(downSampleRate);
    	if(dsr < 1){
    		System.out.println("Invalid down-sampling rate");
    		System.exit(1);
    	}
    	for (int i = 0; i < children.length; i++) {
    		String filename = children[i];
    		filename = dir + "/" + filename;
    		ShapefileLoader loader =  new ShapefileLoader(filename);
    		ShapeObject current = loader.getShapeObject();
    		double[] bbox = current.GetBndBox(0);
    		int width = (int)(bbox[2]+bbox[0]);
    		int height = (int)(bbox[3]+bbox[1]);
    		ImageObject testObject = ImageObject.createImage(height + 20, width + 20, 1, ImageObject.TYPE_BYTE);
    		testObject.setData(255);
    		testObject.setInvalidData(-1);
    		int numr = testObject.getNumRows();
    		DrawOp draw = new DrawOp();
    		int numPts = current.getNumBoundaryPts(0);
    		int numSegments = 0;
    		if(numPts%dsr != 0){
    			numSegments = numPts/dsr + 1;
    		} else {
    			numSegments = numPts/dsr;
    		}
    		// Vector of errors between coarse and fine boundaries
    		double[] errorSig = new double[numPts];
    		double[] cspt = new double[2], cept = new double[2], fspt = new double[2], fept = new double[2]; // coarse start and end pts, fine start and end pts
    		double[] cvec = new double[2], fvec = new double[2]; // coarse and fine vectors starting at origin
    		double clen, flen; // length of each vector
    		int lim = 0;
    		Point2DDouble bnd = current.getAllBoundaryPoints();
    		for(int j = 0; j < numSegments; j++){
    			cspt[0] = bnd.GetValueRow(j*dsr);
    			cspt[1] = bnd.GetValueCol(j*dsr);
    			if(j != (numSegments-1)){
    				cept[0] = bnd.GetValueRow((j+1)*dsr);
    				cept[1] = bnd.GetValueCol((j+1)*dsr);
    				lim = dsr;
    			} else {
    				cept[0] = bnd.GetValueRow(0);
    				cept[1] = bnd.GetValueCol(0);
    				lim = numPts - dsr*(numSegments-1);
    			}
    			cvec[0] = cept[0]-cspt[0];
    			cvec[1] = cept[1]-cspt[1];
    			clen = Math.sqrt(cvec[0]*cvec[0] + cvec[1]*cvec[1]);
    			cvec[0] /= clen; // Normalize
    			cvec[1] /= clen;
    			errorSig[j*dsr] = 0;
    			ImLine linec = new ImLine(new ImPoint(numr-cspt[0],cspt[1]),new ImPoint(numr-cept[0],cept[1]));
    			draw.plot_lineDouble(testObject, linec, 180.0);
    			for(int k = 0; k < lim; k++){
        			fept[0] = bnd.GetValueRow(j*dsr+k);
        			fept[1] = bnd.GetValueCol(j*dsr+k);
        			if(k != 0){
	        			fvec[0] = fept[0]-cspt[0];
	        			fvec[1] = fept[1]-cspt[1];
	        			flen = Math.sqrt(fvec[0]*fvec[0] + fvec[1]*fvec[1]);
	        			fvec[0] /= flen; // Normalize
	        			fvec[1] /= flen;
	        			double dot = fvec[1]*cvec[1]+fvec[0]*cvec[0];
	        			// Truncate to correct for finite precision
	        			if(dot <= -1.0) dot = -1;
	        			if(dot >= 1.0) dot = 1;
	    				errorSig[j*dsr + k] = flen*Math.sin(Math.acos(dot));
        			}
        			if(j*dsr+k-1 >= 0){
	        			fspt[0] = bnd.GetValueRow(j*dsr+k-1);
	        			fspt[1] = bnd.GetValueCol(j*dsr+k-1);
        			} else {
        				fspt[0] = bnd.GetValueRow(numPts-1);
	        			fspt[1] = bnd.GetValueCol(numPts-1);
        			}
        			ImLine linef = new ImLine(new ImPoint(numr-fspt[0],fspt[1]),new ImPoint(numr-fept[0],fept[1]));
        			draw.plot_lineDouble(testObject, linef, 0.0);
    			}
    		}
    		ImageLoader.writeImage(destPathName + "/" + children[i].substring(0,children[i].lastIndexOf(".")) + ".png",testObject);
    		// Print signal to csv file
    		FileWriter csvFile = new FileWriter(destPathName + "/" + children[i].substring(0,children[i].lastIndexOf(".")) + ".csv");
        	PrintWriter csv = new PrintWriter(csvFile);
        	//csv.println(children[i].substring(0,children[i].lastIndexOf(".")));
        	//csv.println("numpts = " + numPts);
        	for(int n = 0; n < errorSig.length; n++){
        			csv.println(errorSig[n]);
        	}
    		csv.close();
    	}
    }
    
    private static void syntheticScript(String scriptFilePath) throws Exception{
    	FileInputStream fstream = new FileInputStream(scriptFilePath);
		// Get the object of DataInputStream
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String topDir, topSrcDir;
		
		//Read top directory
		topDir = br.readLine();
		File topDirF = new File(topDir);
		// Create directory if necessary
		if(!topDirF.exists()){
			topDirF.mkdir();
		}
		String imDirName = br.readLine();
		String mode = br.readLine(); // Get mode
		int aprioriArea = 0;
		if(mode.equals("fgstats")) aprioriArea = Integer.parseInt(br.readLine());
		
		int strelSizes[], thresholds[], ballSizes[];
		
		String strelString = (br.readLine()).replaceAll("[\\[\\]]", ""); // Get strel sizes
		String strelSizesString[] = strelString.split(",");
		if(strelString.length() != 0){
			strelSizes = new int[strelSizesString.length];
			for(int i = 0; i < strelSizesString.length; i++){
				strelSizes[i] = Integer.parseInt(strelSizesString[i]);
			}
		} else {
			strelSizes = new int[1];
			strelSizes[0] = 0;
		}
		
		String threshString = (br.readLine()).replaceAll("[\\[\\]]", ""); // Get thresholds
		String threshSizesString[] = threshString.split(",");
		if(threshString.length() != 0){
			thresholds = new int[threshSizesString.length];
			for(int i = 0; i < threshSizesString.length; i++){
				thresholds[i] = Integer.parseInt(threshSizesString[i]);
			}		
		} else {
			thresholds = new int[1];
			thresholds[0] = 128;
		}
		
		String ballString = (br.readLine()).replaceAll("[\\[\\]]", ""); // Get ball sizes
		String ballSizesString[] = ballString.split(",");
		if(ballString.length() != 0){
			ballSizes = new int[ballSizesString.length];
			for(int i = 0; i < ballSizesString.length; i++){
				ballSizes[i] = Integer.parseInt(ballSizesString[i]);
			}		
		} else {
			ballSizes = new int[1];
			ballSizes[0] = 1;
		}
		
		int seedVals[] = {255}; // Default
		
		if(mode.equals("fgstats")){
			String seedString = (br.readLine()).replaceAll("[\\[\\]]", ""); // Get seed values
			String seedValsString[] = seedString.split(",");
			if(seedString.length() != 0){
				seedVals = new int[seedValsString.length];
				for(int i = 0; i < seedValsString.length; i++){
					seedVals[i] = Integer.parseInt(seedValsString[i]);
				}		
			} // Else stay with default (255)
		}
		
		processSyntheticData(imDirName,topDir,mode,strelSizes,thresholds,ballSizes,seedVals,aprioriArea);	
    }
    
    private static void processSyntheticData(String imDirName, String resDirName, String mode, int[] strelSizes, int[] thresholds, int[] ballSizes, int[] seedVals, int aprioriArea) throws Exception{
    	File dir = new File(imDirName);
    	String[] children = dir.list();
    	FileWriter csvFile = new FileWriter(resDirName + "/" + "expt_statistics.csv");
    	PrintWriter csv = new PrintWriter(csvFile);

	    for (int i = 0; i < children.length; i++) {
	        String filename = children[i];
	        
	        // Only look at .tif files
	        if(!filename.substring(filename.lastIndexOf(".")).equalsIgnoreCase(".tif")) continue;
	        
	        // Create a directory for each file to contain results for that file
	        String currentDirName = resDirName + "/" + filename.substring(0,filename.lastIndexOf("."));
	        File currentDir = new File(currentDirName);
			if(!currentDir.exists()){
				currentDir.mkdir();
			}
			
	        filename = imDirName + "/" + filename;
	        ImageObject srcImage = ImageLoader.readImage(filename);
	        srcImage = srcImage.convert(ImageObject.TYPE_BYTE, true);
	        // File to hold results
	        FileWriter outFile = new FileWriter(currentDirName + "/" + children[i].substring(0, children[i].lastIndexOf('.')) + ".txt");
			PrintWriter out = new PrintWriter(outFile);
			out.println(filename);
			csv.println(filename);
			out.println("Mode = " + mode);
			
			// Obtain "true" result
			ImageObject trueRes = null;
			int perim = 0;
			int trueArea = 0;
			if(mode.equals("fractal")){
				// Calculate area
				Seg2DBall segAut = new Seg2DBall();
				segAut.setImage(srcImage.extractBand(0));
				segAut.setBallSeed(srcImage.getNumCols()/2,srcImage.getNumRows()/2,1);
				segAut.setThreshold(128.0);
				segAut.segment();
				trueRes = segAut.getSegImageObject();
				trueRes = trueRes.convert(0, true);
				for(int row = 0; row < trueRes.getNumRows(); row++){
					for(int col = 0; col < trueRes.getNumCols(); col++){
						if(trueRes.getByte(row,col,0)!=0) trueArea++;
					}
				}
				// Calculate perimeter
				for(int row = 0; row < srcImage.getNumRows(); row++){
					for(int col = 0; col < srcImage.getNumCols(); col++){
						if(srcImage.getByte(row,col,0) == 0) perim++;
					}
				}
				// Write results
				ImageLoader.writeImage(currentDirName + "/" + "true_result.tif", trueRes);
				out.println("True area = " + trueArea);
				csv.println("Area:," + trueArea);
				out.println("True perimeter = " + perim);
				csv.println("Perimeter:," + perim);
				float fracParam = (float)Math.pow((float)perim, 2)/(float)trueArea;
				out.println("Fractal parameter = " + fracParam);
				csv.println("Fractal Parameter:," + fracParam);
			}
			
			if(mode.equals("fgstats")){
				trueArea = aprioriArea;
				out.println("True area = " + trueArea);
				csv.println("Area:," + trueArea);
			}
			
			// Excel column titles
			if(mode.equals("fgstats")){
				csv.println("Threshold,Ball Size,Seed Value,Seed Number,Error,Percent Error");
			} else {
				csv.println("Threshold,Ball Size,Structuring Element Size,Seed Number,Error,Percent Error");
			}
			
	        for(int strelSizeInd = 0; strelSizeInd < strelSizes.length; strelSizeInd++){
	        	int strelSize = strelSizes[strelSizeInd];
	        	
	        	// Begin morphological filtering
	        	if(strelSize != 0){
			    	Morpho morpher = new Morpho();
			    	morpher.CreateMorphElem(strelSize, 1);
			    	morpher.MorphClose(srcImage);
			    	srcImage = morpher.GetImageObject();
			    	Morpho morpher2 = new Morpho();
			    	morpher2.CreateMorphElem(1, strelSize);
			    	morpher2.MorphClose(srcImage);
			    	srcImage = morpher2.GetImageObject();
	        	}
		    	// End morphological filtering
	        	
	            srcImage = srcImage.convert(0, true);
		        Seg2DBall seg = new Seg2DBall();
		        int seedX,seedY;
		        
		        // Negative vs. positive area (i.e. two types of error fg-bg or bg-fg)
		        
		        for(int seedInd = 0; seedInd < seedVals.length; seedInd++){
	        		// Seed is center pixel
		        	seedX = srcImage.getNumCols()/2;
	        		seedY = srcImage.getNumRows()/2;
	        		// Set seed value artificially if in fgstats mode
	        		if(mode.equals("fgstats")){
	        			srcImage.set(seedY, seedX, 0, seedVals[seedInd]);
	        		}

	    	        for(int ballSizeInd = 0; ballSizeInd < ballSizes.length; ballSizeInd++){
	    	        	int ballSize = ballSizes[ballSizeInd];
	    	        	for(int thresholdInd = 0; thresholdInd < thresholds.length; thresholdInd ++){
	    	        		double threshold = thresholds[thresholdInd];
	    	        		seg.setImage(srcImage.extractBand(0));
			    	        seg.setBallSeed(seedX, seedY, ballSize);
			    	        seg.setThreshold(threshold);
			    	        seg.segment();
			    	        double seedVal = 0;
			    	        if(mode.equals("fgstats")){
			    	        	seedVal = seg.getSeedVal();
			    	        }
			    	        ImageObject current = seg.getSegImageObject();
			    	        current = CompareCoastlines.scaleBinImg(current, 255);
			    	        current = current.convert(0, true);
			    	        // Apply morphological closing if necessary
			    	        if(mode.equals("fgstats")){
			    	        	Morpho oper = new Morpho(10,10);
			    	        	oper.MorphClose(current);
			    	        	current = oper.GetImageObject();
			    	        }
			    	        
							
							int numRows = current.getNumRows();
							int numCols = current.getNumCols();

							int error = 0;
							int area = 0;
							
							for(int row = 0; row < numRows; row++){
								for(int col = 0; col < numCols; col++){
									if(mode.equals("fractal")){
										error += current.getByte(row,col,0) - trueRes.getByte(row,col,0);
									} else {
										if(current.getByte(row,col,0) != 0) area++;
									}
								}
							}
							if(mode.equals("fgstats")) error = area-trueArea;
							
							if(mode.equals("fgstats")){
								out.println("(T=" + threshold + ", B=" + ballSize + ", SV=" + seedVal + "<" + seedX + "," + seedY + ">" + ")" + "\t\t:\t" + "Error=" + error + " (" + error*100.0/trueArea + "%)");	
								csv.println(threshold + "," + ballSize + "," + seedVal + "," + seedInd + "," + error + "," + error*100.0/trueArea);
							} else {
								out.println("(T=" + threshold + ", B=" + ballSize + ", S=" + strelSize + "<" + seedX + "," + seedY + ">" + ")" + "\t\t:\t" + "Error=" + error + " (" + error*100.0/trueArea + "%)");	
								csv.println(threshold + "," + ballSize + "," + strelSize + "," + seedInd + "," + error + "," + error*100.0/trueArea);
							}
							ImageLoader.writeImage(currentDirName + "/" + "T_" + threshold + "_B_" + ballSize + "_M_" + strelSize + "_S_" + seedInd + ".tif", current);
	    	        	}
	    	        }
	
	    	        // TODO:
	    	        // Take co-occurrence into account
	    	        // How to represent fg statistics results?
	    	        // Generate step graphs (avea vs. threshold) for multiple seed values
	    	        // Post processing to capture boundary
	    	        // Plots of area and error vs. ball size and error vs. morphological element size
	    	        // Excel spreadsheet with measured variables from maps: estimate of maximum gap, minimum border width, area, borderlength (how to measure?) 
	    	        if(mode.equals("fgstats")) csv.println();
			    }
	        }
	        csv.println();
	        out.close();
	    }
	    csv.close();
    }
    
    private static void superScript(String scriptFilePath) throws Exception{
		FileInputStream fstream = new FileInputStream(scriptFilePath);
		// Get the object of DataInputStream
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String topDir, topSrcDir;
		String logDir, mseDir, resDir, conversionsPath, labelsPath;
		int numLakes;
		
		//Read top directory
		topDir = br.readLine();
		topSrcDir = br.readLine();
		numLakes = Integer.parseInt(br.readLine());
		String[] imDirs = new String[numLakes];
		String[] lakeDirs = new String[numLakes];
		String[] shpDirs = new String[numLakes];
		String[] resDirs = new String[numLakes];
		String[] logDirs = new String[numLakes];
		String[] areaDirs = new String[numLakes];
		String[] mseDirs = new String[numLakes];
		String[] exampleDirs = new String[numLakes];
		File topDirF = new File(topDir);
		// Create directory if necessary
		if(!topDirF.exists()){
			topDirF.mkdir();
		}
		
		for(int i = 0; i < numLakes; i++){
			lakeDirs[i] = topDir + "\\" + br.readLine();
			File lakeDirF = new File(lakeDirs[i]);
			if(!lakeDirF.exists()){
				lakeDirF.mkdir();
			}
		}
		
		String imDirName = br.readLine();
		for(int i = 0; i < numLakes; i++){
			String imDir = lakeDirs[i] + "\\" + imDirName;
			File imDirF = new File(imDir);
			imDirs[i] = topSrcDir + "\\" + br.readLine();
			if(!imDirF.exists()){
				imDirF.mkdir();
				File imSrc = new File(imDirs[i]);
				String[] imgs = imSrc.list();
				for(int j = 0; j < imgs.length; j++){
					ImageObject img = ImageLoader.readImage(imSrc + "\\" + imgs[j]);
					ImageLoader.writeImage(imDir + "\\" + imgs[j], img);
				}
			}
		}
		logDir = br.readLine();
		mseDir = br.readLine();
		resDir = br.readLine();
		for(int i = 0; i < numLakes; i++){
			String tempLogDir = lakeDirs[i] + "\\" + logDir;
			File logDirF = new File(tempLogDir);
			if(!logDirF.exists()){
				logDirF.mkdir();
			}
			logDirs[i] = tempLogDir;
			String tempMseDir = lakeDirs[i] + "\\" + mseDir;
			File mseDirF = new File(tempMseDir);
			if(!mseDirF.exists()){
				mseDirF.mkdir();
			}
			mseDirs[i] = tempMseDir;
			String tempResDir = lakeDirs[i] + "\\" + resDir;
			File resDirF = new File(tempResDir);
			if(!resDirF.exists()){
				resDirF.mkdir();
			}
			resDirs[i] = tempResDir;
			File shpDirF = new File(lakeDirs[i] + "\\shp");
			if(!shpDirF.exists()){
				shpDirF.mkdir();
			}
			File areaDirF = new File(lakeDirs[i] + "\\Areas");
			if(!areaDirF.exists()){
				areaDirF.mkdir();
			}
			areaDirs[i] = lakeDirs[i] + "\\Areas";
		}
		
		for(int i = 0; i < numLakes; i++){
			shpDirs[i] = topSrcDir + "\\" + br.readLine();
			if((new File(lakeDirs[i] + "\\shp").list()).length<1){
				String[] files = (new File(shpDirs[i])).list();
				for(int j = 0; j < files.length; j++){
					ShapefileLoader loader = new ShapefileLoader(shpDirs[i] + "\\" + files[j]);
					loader.Write(lakeDirs[i] + "\\shp\\" + files[j],loader.getShapeObject());
				}
				String[] badFiles = (new File(lakeDirs[i]+"\\shp\\")).list();
				for(int k = 0; k < badFiles.length; k++){
					if(!(badFiles[k].substring(badFiles[k].lastIndexOf('.'),badFiles[k].length()).equals(".shp"))){
						System.gc(); // HACK!
						boolean result = (new File(lakeDirs[i] + "\\shp\\" + badFiles[k])).delete();
					}
					
				}
			}
		}
		
		for(int i = 0; i < numLakes; i++){
			String exDir = lakeDirs[i] + "\\" + "Examples";
			File exDirF = new File(exDir);
			exampleDirs[i] = topSrcDir + "\\" + br.readLine();
			if(!exDirF.exists()){
				exDirF.mkdir();
				File exSrc = new File(exampleDirs[i]);
				String[] exs = exSrc.list();
				for(int j = 0; j < exs.length; j++){
					ShapefileLoader loader = new ShapefileLoader(exampleDirs[i] + "\\" + exs[j]);
					loader.Write(lakeDirs[i] + "\\Examples\\" + exs[j],loader.getShapeObject());
				}
				String[] badFiles = (new File(lakeDirs[i]+"\\Examples\\")).list();
				for(int k = 0; k < badFiles.length; k++){
					if(!(badFiles[k].substring(badFiles[k].lastIndexOf('.'),badFiles[k].length()).equals(".shp"))){
						System.gc(); // HACK!
						boolean result = (new File(lakeDirs[i] + "\\Examples\\" + badFiles[k])).delete();
					}
					
				}
			}
		}
		
		conversionsPath = topSrcDir + "\\" + br.readLine();
		labelsPath = topSrcDir + "\\" + br.readLine();
		
		specialScript(numLakes, imDirs, exampleDirs, shpDirs, resDirs, logDirs, areaDirs, conversionsPath, mseDirs);
		
		//Close the input stream
		in.close();
    }
    
    private static void specialScript(int numLakes, String[] dataDirs, String[] exampleDirs, String[] shpDirs,
    		String[] resultsDirs, String[] logDirs, String[] areaDirs, String conversionsPath, String[] mseDirs) throws Exception{
    	for(int i = 0; i < numLakes; i++){
    		segByShape(dataDirs[i], exampleDirs[i], resultsDirs[i], logDirs[i], "shp", null, null, null);
    		computeAreas(resultsDirs[i], conversionsPath, areaDirs[i]);
    		evaluatePerformance(resultsDirs[i], shpDirs[i], mseDirs[i]);
    	}
    }
    
    private static void areaScript(int numLakes, String conversionsPath, String[] dataDirs, String[] resultsDirs) throws IOException, ShapefileException, ImageException{
    	for(int i = 0; i < numLakes; i++){
    		computeAreas(dataDirs[i], conversionsPath, resultsDirs[i]);
    	}
    }
    
    private static void computeAreas(String dataDir, String conversionsPath, String resultsDir) throws IOException, ShapefileException, ImageException{
    	Hashtable conversionFactors = new Hashtable();
	      //use buffering, reading one line at a time
	      //FileReader always assumes default encoding is OK!
	      BufferedReader input =  new BufferedReader(new FileReader(conversionsPath));
	      try {

      	String name = null;
      	Double value = null;
	        while ((name = input.readLine()) != null){
	        	if((value = Double.parseDouble(input.readLine())) == null){
	        		System.out.println("Invalid conversion file format!");
	        		System.exit(5);
	        	}
              conversionFactors.put(name, value);
	        }
	      }
	      finally {
	        input.close();
	      }

	CompareCoastlines.processDatasetArea2(dataDir, conversionFactors, resultsDir + "/areas.txt");
    }
    
    private static void evaluatePerformance(String automaticDir, String manualDir, String resultsDir) throws Exception{
    	File imDir = new File(automaticDir);
    	File shpDir = new File(manualDir);
    	String[] imgs = imDir.list();
    	DefaultTableModel table = new DefaultTableModel(imgs.length,2);
    	int lim = imgs.length;
    	//int lim = 5;
    	for(int i = 0; i < lim; i++){
    		String filename = imgs[i];
    		table.setValueAt(filename, i, 0);
    		ImageObject img1p = ImageLoader.readImage(automaticDir + "/" + filename);
    		ShapefileLoader loader =  new ShapefileLoader(manualDir + "/" + filename.substring(0, filename.lastIndexOf('.')));
    		ShapeObject exampShape = loader.getShapeObject();
    		ImageObject img2p = CompareCoastlines.findInterior(exampShape);
    		img2p = img2p.convert(6, true);
    		CompareCoastlines.addConstant(img2p,1);
    		double[] box = exampShape.getBoundingBox(0);
    		// Fix sizes:
    		int numRows = img1p.getNumRows();
    		int numCols = img1p.getNumCols();
    		ImageObject img1 = ImageObject.createImage(numRows-100, numCols-100, 1, 6);
	        for(int row = 49; row < numRows-51; row++){
	        	for(int col = 49; col < numCols-51; col++){
	        		double pix = img1p.getDouble(row,col,0);
	        		img1.setDouble(row-49,col-49,0,pix);
	        	}
	        }
	        
	        ImageObject img2 = ImageObject.createImage(numRows-100, numCols-100, 1, 6);
	        int rowLim,colLim;
	        if(numRows-100>box[3]+box[1]){
	        	rowLim = (int) (box[3]+box[1]);
	        } else {
	        	rowLim = numRows-100;
	        }
	        if(numCols-100>box[2]+box[0]){
	        	colLim = (int) (box[2]+box[0]);
	        } else {
	        	colLim = numCols-100;
	        }
	        double normalization = 0;
	        for(int row = 0; row < rowLim; row++){
	        	for(int col = 0; col < colLim; col++){
	        		double pix = img2p.getDouble(row,col,0);
	        		img2.setDouble(row,col,0,pix*255);
	        		normalization+=pix;
	        	}
	        }
	        ImageLoader.writeImage(resultsDir + "/" + filename.substring(0, filename.lastIndexOf('.'))+ "_aut.tif", img1);
	        ImageLoader.writeImage(resultsDir + "/" + filename.substring(0, filename.lastIndexOf('.'))+ "_man.tif", img2);
    		double MSE = CompareCoastlines.computeMSE(img1,img2,normalization,resultsDir + "/"+filename);
    		System.out.println("MSE = " + MSE);
    		table.setValueAt(MSE, i, 1);
    	}
    	
    	CSVTable csvWriter = new CSVTable();
		String resultFileName = resultsDir + "/" + "MSE";
		csvWriter.writeTable(table, resultFileName, false);
    }
    
    private static void featureScript() throws IOException, ShapefileException, ImageException{
    	
    	//String eDataDir = "C:/Users/twshaw3/Desktop/CS446/Project/MLExpt/TrainingData/Erie/shp";
    	//String eFeatureDir = "C:/Users/twshaw3/Desktop/CS446/Project/MLExpt/TrainingData/Erie/features";
    	//generateFeatures(eDataDir,eFeatureDir);
    	//String hDataDir = "C:/Users/twshaw3/Desktop/CS446/Project/MLExpt/TrainingData/Huron/shp";
    	//String hFeatureDir = "C:/Users/twshaw3/Desktop/CS446/Project/MLExpt/TrainingData/Huron/features";
    	//generateFeatures(hDataDir,hFeatureDir);
    	//String mDataDir = "C:/Users/twshaw3/Desktop/CS446/Project/MLExpt/TrainingData/Michigan/shp";
    	//String mFeatureDir = "C:/Users/twshaw3/Desktop/CS446/Project/MLExpt/TrainingData/Michigan/features";
    	//generateFeatures(mDataDir,mFeatureDir);
    	
    	//String mDataDir = "C:/Users/twshaw3/Desktop/CS446/Project/MLExpt/TestingData/Historical/Michigan/shp";
    	//String mFeatureDir = "C:/Users/twshaw3/Desktop/CS446/Project/MLExpt/TestingData/Historical/Michigan/features";
    	//generateFeatures(mDataDir,mFeatureDir);
    	//String oDataDir = "C:/Users/twshaw3/Desktop/CS446/Project/MLExpt/TrainingData/Ontario/shp";
    	//String oFeatureDir = "C:/Users/twshaw3/Desktop/CS446/Project/MLExpt/TrainingData/Ontario/features";
    	//generateFeatures(oDataDir,oFeatureDir);
    	
    	//String miscDataDir = "C:/Users/twshaw3/Desktop/CS446/Project/MLExpt/TrainingData/Miscellaneous/shp";
    	//String miscFeatureDir = "C:/Users/twshaw3/Desktop/CS446/Project/MLExpt/TrainingData/Miscellaneous/features";
    	//generateFeatures(miscDataDir,miscFeatureDir);
    	
    	String m_miscDataDir = "C:/Users/twshaw3/Desktop/CS446/Project/MLExpt/TestingData/Modern_Misc/shp";
    	String m_miscFeatureDir = "C:/Users/twshaw3/Desktop/CS446/Project/MLExpt/TestingData/Modern_Misc/features";
    	generateFeatures(m_miscDataDir,m_miscFeatureDir);
    }
    
    private static void generateFeatures(String dataDir, String featureDir) throws IOException, ShapefileException, ImageException{
    	File dir = new File(dataDir);
    	String[] children = dir.list();
    	FileWriter outFile = new FileWriter(featureDir + "/features.txt");
    	PrintWriter out = new PrintWriter(outFile);
    	for (int i = 0; i < children.length; i++) {
	        String filename = children[i];
    		ShapefileLoader loader =  new ShapefileLoader(dataDir + "/" + filename);
    		ShapeObject exampShape = loader.getShapeObject();
    		ImageObject exampImage = CompareCoastlines.findInterior(exampShape);
    		exampImage = exampImage.convert(6, true);
    		CompareCoastlines.addConstant(exampImage,1);
    		
	        // Log the moments
			double[] moments = CompareCoastlines.huMoments2(exampImage);
			for(int j = 0; j < moments.length; j++){
				out.printf(moments[j] + ",");
			}
			out.printf("\n");
    	}
    	out.close();
    }
    

    
    private static void mlExpt(String dataDir, String resultsDir) throws ImageException, IOException, ShapefileException{
    	File dir = new File(dataDir);
    	String[] children = dir.list();
    	for (int i = 0; i < children.length; i++) {
	        String filename = children[i];
    		ShapefileLoader loader =  new ShapefileLoader(dataDir + "/" + filename);
    		ShapeObject exampShape = loader.getShapeObject();
    		ImageObject exampImage = CompareCoastlines.findInterior(exampShape);
    		exampImage = exampImage.convert(6, true);
    		CompareCoastlines.addConstant(exampImage,1);
    		displayImg(exampImage,"Img"+i);
    		
	        // Log the moments
			//FileWriter outFile = new FileWriter(resultsDir + "/" + children[i].substring(0, children[i].lastIndexOf('.')) + ".txt");
			//PrintWriter out = new PrintWriter(outFile);
			//double[] moments = CompareCoastlines.huMoments2(exampImage);
			//for(int j = 0; j < moments.length; j++){
			//	out.println("Moment " + j + " = " + moments[j]);
			//}
			//out.close();
    	}
    }
    
    // File names in maskDir should correspond to those in dataDir
    private static void computeStatistics(String dataDir1, String maskDir1, String dataDir2, String maskDir2, String dataDir3,
    										String maskDir3, String dataDir4, String maskDir4, String resultPath) throws Exception{
    	String[] colorSpaceList = ColorModels.getColorModelList();
    	int numColorSpaces = colorSpaceList.length;
    	for(int j = 1; j <= 4; j++){
    		
    		String dataDir = null, maskDir = null;
    		switch(j){
    		case 1:
    			dataDir = dataDir1;
    			maskDir = maskDir1;
    			break;
    		case 2:
    			dataDir = dataDir2;
    			maskDir = maskDir2;
    			break;
    		case 3:
    			dataDir = dataDir3;
    			maskDir = maskDir3;
    			break;
    		case 4:
    			dataDir = dataDir4;
    			maskDir = maskDir4;
    		}
        	File dir = new File(dataDir);
        	String[] children = dir.list();
        	int numImages = children.length;
        	
	    	for(int model = 0; model < numColorSpaces; model++){
	    		String currentModel = colorSpaceList[model];
		    	
	    		if(!currentModel.equals("GRAY")){
		    		ColorModels converter = new ColorModels("RGB",currentModel);
			    	DefaultTableModel table = new DefaultTableModel(numImages,3);
				    for (int i = 0; i < children.length; i++) {
				        String imageFilename = children[i];
				        String maskFilename = maskDir + "/" + imageFilename;
				        imageFilename = dataDir + "/" + imageFilename;
				        ImageObject srcImage = ImageLoader.readImage(imageFilename);
				        if(!currentModel.equals("RGB")){
					        if(currentModel.equals("LAB") || currentModel.equals("LUV")){
					        	ColorModels xyzConverter = new ColorModels("RGB","XYZ");
					        	xyzConverter.convert(srcImage);
					        	srcImage = xyzConverter.getConvertedIm();
					        	ColorModels nextConverter = new ColorModels("XYZ", currentModel);
					        	nextConverter.convert(srcImage);
					        	srcImage = nextConverter.getConvertedIm();
					        } else {
					        	converter.convert(srcImage);
					        	srcImage = converter.getConvertedIm();
					        }
				        }
				        ImageObject maskImage = ImageLoader.readImage(maskFilename);
				        int numRows = srcImage.getNumRows();
				        int numCols = srcImage.getNumCols();
				        int numBands = srcImage.getNumBands();
				        int numForegroundPix = 0;
			    		double[] mean = new double[3];
				        for(int row = 0; row < numRows; row++){
				        	for(int col = 0; col < numCols; col++){
				        		if(maskImage.getDouble(row,col,0) != 0){ // Foreground Pixel
				        			numForegroundPix++;
				        			for(int band = 0; band < numBands; band++){
				        				mean[band] += srcImage.getDouble(row,col,band);
				        			}
				        		}
				        	}
				        }
				        // Normalize mean vec and store in table
				        for(int band = 0; band < numBands; band++){
				        	mean[band] /= numForegroundPix;
				        	table.setValueAt(mean[band], i, band);
				        }
				        
				    }
				    
					CSVTable csvWriter = new CSVTable();
					String resultFileName = resultPath + "/class" + j + "_" + currentModel;
					csvWriter.writeTable(table, resultFileName, false);
	    		}
	    	}
    	}
    }
    
    private static void segByValue(String dataDir, String resultDir) throws Exception{
    	int ballSize = 4;
    	int threshold = 25;
    	File dir = new File(dataDir);
    	String[] children = dir.list();
	    for (int i = 0; i < children.length; i++) {
	        String filename = children[i];
	        filename = dataDir + "/" + filename;
	        ImageObject srcImage = ImageLoader.readImage(filename);
	        PCA pca = new PCA();
            pca.computeLoadings(srcImage);
            srcImage = pca.applyPCATransform(srcImage);
            srcImage = srcImage.convert(0, true);
	        int seedX = srcImage.getNumCols()/2;
	        int seedY = srcImage.getNumRows()/2;
	        long size = 0;
	    	double numPixels = (double)srcImage.getNumCols()*(double)srcImage.getNumRows();
	    	while(size < numPixels*0.1){
		        Seg2DBall seg = new Seg2DBall();
		        seg.setImage(srcImage.extractBand(0));
		        seg.setBallSeed(seedX, seedY, ballSize);
		        seg.setThreshold(threshold);
		        seg.segment();
		        size = seg.getPixelCount();
    	        seedX += (int)20*(Math.random()*2-1);
    	        seedY += (int)20*(Math.random()*2-1);		
    	        System.out.println(size);
	    	}
	        Seg2DBall seg = new Seg2DBall();
	        seg.setImage(srcImage.extractBand(0));
	        seg.setBallSeed(seedX, seedY, ballSize);;
	        seg.setThreshold(threshold);
	        seg.segment();
	        ImageObject bestResult = seg.getSegImageObject();
	        bestResult = bestResult.convert(6, true);
	        //displayImg(bestResult, filename);  
	        CompareCoastlines.scaleImg(bestResult, 255);
	        ImageLoader.writeImage(resultDir + "/" + children[i], bestResult);
	    }
    }
    
    private static void segByShape(String dataDir, String exampDir, String resultDir, String logDir, String exampType, int ballSizes[], int thresholds[], int strelSizes[]) throws Exception{
    	//double epsilon = Math.pow(10, -6);
    	File dir = new File(dataDir);
    	String[] children = dir.list();
    	// Default ball sizes, thresholds, and structuring element dimensions
    	if(ballSizes == null){ // Default = 1 to 5 in steps of 1
    		int[] tempBallSizes = {1,2,3,4,5};
    		ballSizes = tempBallSizes;
    	}
    	if(thresholds == null){ // Default = 0 to 100 in steps of 5
    		thresholds = new int[21];
    		for(int i = 0; i < 21; i++){
    			thresholds[i] = 5*i;
    		}
    	}
    	if(strelSizes == null){ // Default = 5
    		strelSizes = new int[1];
    		strelSizes[0] = 5;
    	}
    	//System.out.println("files: " + children[0]);
    	File exDir = new File(exampDir);
    	String[] examples = exDir.list();
    	double[][] exampFeatures = new double[examples.length][];
    	for(int j = 0; j < examples.length; j++){
    		ImageObject exampImage;
    		if(exampType.equals("shp")){
	    		ShapefileLoader loader =  new ShapefileLoader(exampDir + "/" + examples[j]);
	    		ShapeObject exampShape = loader.getShapeObject();
	    		exampImage = CompareCoastlines.findInterior(exampShape);
	    		exampImage = exampImage.convert(6, true);
	    		CompareCoastlines.addConstant(exampImage,1);
    		} else {
    			exampImage = ImageLoader.readImage(exampDir + "/" + examples[j]);
    			exampImage = exampImage.convert(6, true);
    			CompareCoastlines.scaleImg(exampImage, 1/255.0);
    		}
    		double[] exampFeature = CompareCoastlines.huMoments2(exampImage);
    		CompareCoastlines.printMoments(exampFeature);
    		displayImg(exampImage, "example");
    		exampFeatures[j] = exampFeature;
    	}
	    for (int i = 0; i < children.length; i++) {
	        String filename = children[i];
	        String temp = filename;
	        filename = dataDir + "/" + filename;
	        ImageObject srcImage = ImageLoader.readImage(filename);
	        // Begin morphological filtering
	        srcImage = srcImage.convert(ImageObject.TYPE_BYTE, true);
	        for(int strelSizeInd = 0; strelSizeInd < strelSizes.length; strelSizeInd++){
	        	int strelSize = strelSizes[strelSizeInd];
		    	Morpho morpher = new Morpho();
		    	morpher.CreateMorphElem(strelSize, 1);
		    	morpher.MorphClose(srcImage);
		    	srcImage = morpher.GetImageObject();
		    	Morpho morpher2 = new Morpho();
		    	morpher2.CreateMorphElem(1, strelSize);
		    	morpher2.MorphClose(srcImage);
		    	srcImage = morpher2.GetImageObject();
		    	ImageLoader.writeImage(logDir + "/" + temp, srcImage);
		    	// End morphological filtering
	            srcImage = srcImage.convert(0, true);
		    	double numPixels = (double)srcImage.getNumCols()*(double)srcImage.getNumRows();
		    	long size = 0;
		        Seg2DBall seg = new Seg2DBall();
		        int seedX = srcImage.getNumCols()/2;
		        int seedY = srcImage.getNumRows()/2;
		        double minDistance = Double.MAX_VALUE;
		        GeomOper oper = new GeomOper();
		        double bestSize = 0;
		        int bestBallSize = 0, bestSeedX = 0, bestSeedY = 0;
		        double bestThreshold = 0;
		        double bestSeedVal = 0;
		        double seedVal = 0;
		        double seedStd = 0;
		        double bestSeedStd = 0;
		        double minSize = 0.15*numPixels;
		        double maxSize = 0.85*numPixels;
		        String bestExample = null;
		        String newseed = "FALSE";
		        while(bestSize == 0){
	    	        bestSize = 0;
	    	        for(int ballSizeInd = 0; ballSizeInd < ballSizes.length; ballSizeInd++){
	    	        	int ballSize = ballSizes[ballSizeInd];
	    	        	for(int thresholdInd = 0; thresholdInd < thresholds.length; thresholdInd ++){
	    	        		double threshold = thresholds[thresholdInd];
	    	        		seg.setImage(srcImage.extractBand(0));
			    	        seg.setBallSeed(seedX, seedY, ballSize);
			    	        seg.setThreshold(threshold);
			    	        seg.segment();
			    	        size = seg.getPixelCount();
			    	        ImageObject current = seg.getSegImageObject();
			    	        seedVal = seg.getSeedVal();
			    	        seedStd = seg.getSeedStd();
			    	        
			    	        current = current.convert(0, true);
			    	        int outerBallSize = 10;
		    		        int numRows = current.getNumRows();
		    		        int numCols = current.getNumCols();
		    		        int numBands = current.getNumBands();
			    	        
			    	        //Region may not touch boundary
			    	        boolean  noBnd = true;
			    	        outerloop:
			    	        for(int r = 0; r < numRows; r++){
			    	        	for(int c = 0; c < numCols; c++){
			    	        		if(r==0 || r==(numRows-1) || c==0 || c==(numCols-1)){
			    	        			if(current.getByte(r,c,0)!=0){
			    	        				noBnd = false;
			    	        				break outerloop;
			    	        			}
			    	        		}
			    	        	}
			    	        }
			    	        
	
		    		        ImageObject filledImage = ImageObject.createImage(numRows+100, numCols+100, numBands, 0);
		    		        for(int row = 0; row < numRows; row++){
		    		        	for(int col = 0; col < numCols; col++){
		    		        		byte pix = current.getByte(row,col,0);
		    		        		filledImage.setByte(row+49,col+49,0,pix);
		    		        	}
		    		        }
		    	        	seg.setImage(filledImage);
		    	        	seg.setBallSeed(20, 20, outerBallSize);
		    	        	seg.setThreshold(0.5);
		    	        	seg.segment();
		    	        	
		    	        	current = seg.getSegImageObject();
		    	        	CompareCoastlines.complementBinaryImage(current);
		    	        	current = current.crop(new SubArea(10, 10, 0, current.getNumCols()-20, current.getNumRows()-20, 1));
							
		    	        	System.out.println("TICK!");
	
			    	        double[] currentFeature = CompareCoastlines.huMoments2(current);
			    	        double currentMinDistance = Double.MAX_VALUE;
			    	        String currentBestExample = "";
			    	        for(int j = 0; j < examples.length; j++){
				    	        double currentDistance = oper.euclidDist(currentFeature.length, 
				    	        						currentFeature, 0, exampFeatures[j], 0);
				    	        System.out.println(currentDistance);
				    	        if(currentDistance < currentMinDistance){
				    	        	currentMinDistance = currentDistance;
				    	        	currentBestExample = examples[j];
				    	        }
			    	        }
		    	        	System.out.println("B: "+ballSize);
		    	        	System.out.println("T: "+threshold);
			    	        if(currentMinDistance < minDistance && size > minSize && size < maxSize){ //&& noBnd){
			    	        	minDistance = currentMinDistance;
			    	        	bestBallSize = ballSize;
			    	        	bestThreshold = threshold;
			    	        	bestSeedVal = seedVal;
			    	        	bestSeedStd = seedStd;
			    	        	bestSeedX = seedX;
			    	        	bestSeedY = seedY;
			    	        	bestSize = size;
			    	        	bestExample = currentBestExample;
			    	        	ImageObject currentDisp = current.convert(6, true);
			    	        	//displayImg(currentDisp,"B:"+ballSize+" T:"+threshold+" D:"+currentMinDistance);
			    	        	CompareCoastlines.printMoments(currentFeature);
			    	        	//System.out.println(currentBestExample);
			    	        	//System.out.println(minDistance);
			    	        }
	    	        	}
	    	        }
	    	        if(bestSize == 0) newseed = "TRUE";
	    	        seedX += (int)20*(Math.random()*2-1);
	    	        seedY += (int)20*(Math.random()*2-1);
	    	        
		        }
	        
	        
		        // Save to parameter log file
				FileWriter outFile = new FileWriter(logDir + "/" + children[i].substring(0, children[i].lastIndexOf('.')) + strelSize + ".txt");
				PrintWriter out = new PrintWriter(outFile);
				out.println("Threshold: " + bestThreshold);
				out.println("Ball Size: " + bestBallSize);
				out.println("Seed: " + "(" + bestSeedX + "," + bestSeedY + ")");
				out.println("Seed Value: " + bestSeedVal);
				out.println("Seed Standard Deviation: " + bestSeedStd);
				out.println("Distance = " + minDistance);
				out.println("New Seed = " + newseed);
				out.close();
	
		        
		        
		        Seg2DBall seg2 = new Seg2DBall();
		        seg2.setImage(srcImage.extractBand(0));
		        seg2.setBallSeed(bestSeedX, bestSeedY, bestBallSize);
		        System.out.println(bestBallSize + "," + bestThreshold);
		        seg2.setThreshold(bestThreshold);
		        seg2.segment2();
		        ImageObject bestResult = seg2.getSegImageObject();
		        int numRows = bestResult.getNumRows();
		        int numCols = bestResult.getNumCols();
		        int numBands = bestResult.getNumBands();
		        ImageObject filledImage = ImageObject.createImage(numRows+100, numCols+100, numBands, 0);
		        for(int row = 0; row < numRows; row++){
		        	for(int col = 0; col < numCols; col++){
		        		byte pix = bestResult.getByte(row,col,0);
		        		filledImage.setByte(row+49,col+49,0,pix);
		        	}
		        }
		        seg.setImage(filledImage);
	        	seg.setBallSeed(20, 20, 10);
	        	seg.setThreshold(0.5);
	        	seg.segment();
	        	bestResult = seg.getSegImageObject();
	        	bestResult = bestResult.crop(new SubArea(40, 40, 0, numCols, numRows, 1));
	        	CompareCoastlines.complementBinaryImage(bestResult);
		        bestResult = bestResult.convert(6, true);
		        //displayImg(bestResult, filename);  
		        CompareCoastlines.scaleImg(bestResult, 255);
		        ImageLoader.writeImage(resultDir + "/" + children[i], bestResult);
		    }
	    }
    }
    
    private static void displayImg(ImageObject imgObject, String title){
    	ImageFrame imgfrm = new ImageFrame(title);
    	imgfrm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	imgfrm.setImageObject(imgObject);
    	imgfrm.pack();
    	imgfrm.setVisible(true);
    }
    
    private static double logEuclidDist(double[] feature1, double[] feature2){
    	if(feature1.length != feature2.length){
    		System.out.println("Feature vectors must have the same length!");
    		return 0;
    	} else {
    		double dist = 0;
    		for(int i = 0; i < feature1.length; i++){
    			dist += Math.pow(Math.log(Math.abs(feature1[i]-feature2[i])),2);
    		}
    		return Math.sqrt(dist);
    	}
    }
    
    private static ImageObject expandBoundaries(ImageObject img, int newRows, int newCols) throws ImageException{
        int numRows = img.getNumRows();
        int numCols = img.getNumCols();
        int numBands = img.getNumBands();
        ImageObject expandedImage = ImageObject.createImage(numRows+newRows, numCols+newCols, numBands, 0);
        for(int row = 0; row < numRows; row++){
        	for(int col = 0; col < numCols; col++){
        		byte pix = img.getByte(row,col,0);
        		expandedImage.setByte(row+(int)(newRows/2.0),col+(int)(newCols/2.0),0,pix);
        	}
        }
        return expandedImage;
    }
    
    // Post process to remove "holes" from segmented images
    private static void removeHoles(String dataDir) throws Exception{
    	File dir = new File(dataDir);
    	String[] children = dir.list();
	    for (int i = 0; i < children.length; i++) {
	        String filename = children[i];
	        filename = dataDir + "/" + filename;
	        ImageObject srcImage = ImageLoader.readImage(filename);
	        int numRows = srcImage.getNumRows();
	        int numCols = srcImage.getNumCols();
	        int numBands = srcImage.getNumBands();
	        ImageObject filledImage = ImageObject.createImage(numRows+2, numCols+2, numBands, 6);
	        for(int row = 0; row < numRows; row++){
	        	for(int col = 0; col < numCols; col++){
	        		double pix = srcImage.getDouble(row,col,0);
	        		filledImage.setDouble(row+1,col+1,0,pix);
	        	}
	        }
	        filledImage = filledImage.convert(0, true);
	        Seg2DBall seg = new Seg2DBall();
	        seg.setImage(filledImage);
	        seg.setBallSeed(10, 10, 8);
	        seg.setThreshold(128);
	        seg.segment();
	        ImageObject result = seg.getSegImageObject();
	        result = result.convert(6, true);
	        displayImg(result, filename); 
	        CompareCoastlines.scaleImg(result, 255);
	        ImageLoader.writeImage(dataDir + "/" + children[i], result);
	    }
    }
}