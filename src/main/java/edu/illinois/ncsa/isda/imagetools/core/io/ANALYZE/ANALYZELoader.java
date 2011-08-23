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
package edu.illinois.ncsa.isda.imagetools.core.io.ANALYZE;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.*;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageOutputStream;

//import ncsa.im2learn.core.io.ImageWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageException;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.imagetools.core.datatype.SubArea;
import edu.illinois.ncsa.isda.imagetools.core.io.ImageReader;
import edu.ucla.loni.analyze.plugin.*;


public class ANALYZELoader implements ImageReader {
	private static Log logger = LogFactory.getLog(ANALYZELoader.class);

	//implementing ImageReader methods

	public boolean canRead(String filename, byte[] hdr){
		long extents = 0;
		extents = (hdr[35] & 0xff) << 24 | (hdr[34] & 0xff) << 16 | (hdr[33] & 0xff) << 8 | (hdr[32] & 0xff);
		if (extents == 16384)
			return true;
		
		return false;
	}
	
	/*
	 * returns the first image of the image set
	 * @see ncsa.im2learn.core.io.ImageReader#readImage(java.lang.String, ncsa.im2learn.core.datatype.SubArea, int)
	 */
	public ImageObject readImage(String filename, SubArea subarea, int sampling) throws IOException{
		ImageObject imgObj = null;
    	File file = new File(filename);
    	try {
    		imgObj = ImageObject.getImageObject(_getImageReader(file).read(0));
    	} catch (ImageException e) {
    		e.printStackTrace();
    	}    		
    	return imgObj;
	}
	
	public ImageObject readImageHeader(String filename) throws IOException, ImageException{
		throw new ImageException("Not implemented.");
	}

	public String[] readExt(){
		return new String[]{"hdr"};
	}
	
    public String getDescription() {
        return "ANALYZE files";
    }	
       
    /**
     * Loads an image array in the ANALYZE 7.5 (by Mayo clinic) format and returns the array of imageobjects containing the images. 
     * http://www.analyzedirect.com/
     * @param filename of the file to load.
     * @return the array of imageobjects containing the loaded images
     * @throws IOException if an error occurrs reading the file.
     */
    static public ImageObject[] readImages(String filename) throws IOException {
    	File file = new File(filename);
    	javax.imageio.ImageReader reader = _getImageReader(file);
    	int numImg = reader.getNumImages(false);
    	ImageObject[] imArray = new ImageObject[numImg];
    	for(int i=0; i<imArray.length; i++) {
	    		BufferedImage bi = reader.read(i);
	    		// This assumes the image is of type float!!!!!!
	    		// if image to big for memory boom
	    		/*
	    		ImageObjectFloat imgobj = new ImageObjectFloat(bi.getWidth(), bi.getHeight(), 1);
	    		// this should be system.ArrayCopy();
	    		imgobj.setData(((DataBufferFloat)bi.getRaster().getDataBuffer()).getData());
				*/
	    		//TODO: will be faster to do the above for each image type
    		
	    	try {
				ImageObject imgobj;
				int type = ImageObject.TYPE_BYTE;
				DataBuffer db = bi.getData().getDataBuffer();
				switch (db.getDataType()) {
				case DataBuffer.TYPE_BYTE:
					type = ImageObject.TYPE_BYTE;
					break;
				case DataBuffer.TYPE_FLOAT:
					type = ImageObject.TYPE_FLOAT;
				default:
					//
				}
				imgobj = ImageObject.createImage(bi.getHeight(), bi.getWidth(), db.getNumBanks(), type);
				
				for(int y=0, j=0; y<imgobj.getNumRows(); y++) {
					for(int x=0; x<imgobj.getNumCols(); x++, j++) {
						for(int b=0; b<imgobj.getNumBands(); b++) {
							imgobj.set(y, x, b, db.getElemDouble(b, j));
						}
					}
				}
				imArray[i] = imgobj;
			} catch (ImageException e) {
				logger.warn("Error loading image.", e);
			}
    	}
    	return imArray;
    }
    
    static public int numberOfVolumes(String filename) throws IOException {
    	File file = new File(filename);
    	javax.imageio.ImageReader reader = _getImageReader(file);
    	if (reader instanceof AnalyzeImageReader){
    		int numVolumes = ((AnalyzeImageReader)reader).getNumVolumes();
    		return numVolumes;
    	}    	
    	return -1;
    }
    
	/**
	 * Gets an Image Reader that can decode the specified file.
	 *
	 * @param file File to find an Image Reader for.
	 *
	 * @return Image Reader that can decode the specified file, or null if none
	 *         was found.
	 *
	 * @throws IOException If an I/O error occurs.
	 */
	static private javax.imageio.ImageReader _getImageReader(File file) throws IOException
	  {
	    Object input = new FileImageInputStream(file);
	    Object origInput = input;
	
	    // First look for a magic number in the header
	    Iterator iter = ImageIO.getImageReaders(input);
	
	    // No Image Reader is found
	    if ( !iter.hasNext() ) {
	
		// Then look in the directory of the file (e.g., ANALYZE, AFNI)
		input = file;
		iter = ImageIO.getImageReaders(input);
	
		// No Image Reader is found
		if( !iter.hasNext() ) {
		  input = origInput;
	
		  // Then look at the file name suffix
		  String temp = file.getName();
		  String[] strings = temp.split("\\.");
		  if (strings.length > 1){
		    iter = ImageIO.getImageReadersBySuffix(strings[strings.length-1]);
		  }
	
		  // No Image Reader found
		  if ( !iter.hasNext() ) { return null; }
		}
	    }
	
	    // Set the Input Stream of the first Image Reader returned
	    javax.imageio.ImageReader imageReader = (javax.imageio.ImageReader)iter.next();
	    imageReader.setInput(input);
	
	    // Return the Image Reader
	    return imageReader;
	  }

	
	 /**
     * Write an image array to the ANALYZE 7.5 (by Mayo clinic) format. 
     *
     * @param filename of the file to save.
     * @throws IOException if an error occurrs reading the file.
     */
    public static void writeImage(String filename, ImageObject[] imgArray) throws IOException  {
    	
    	ImageWriter imageWriter = _getImageWriter(filename);
    	
    	if(imgArray.length == 1) {
    		imageWriter.write(imgArray[0].makeBufferedImage());
    	}
    	else {
    		logger.warn("Not implemented yet");
    	}
    	imageWriter.dispose();
    }
    	

	/**
	 * Gets an Image Writer that can write images read by the specified Image
	 * Reader.
	 *
	 * @param imageReader Image Reader to find an Image Writer for.
	 * @param baseFileName Base name used for the output files.
	 *
	 * @return Image Writer that can write images or null if none was found.
	 *
	 * @throws IOException If an I/O error occurs.
	 */
	private static ImageWriter _getImageWriter(String baseFileName) throws IOException {

		if(baseFileName.endsWith("png")) {
			ImageWriter imageWriter = ImageIO.getImageWritersByFormatName("png").next();
			if (imageWriter == null) { return null; }
			File f = new File(baseFileName);

			ImageOutputStream ios = ImageIO.createImageOutputStream(f);
			imageWriter.setOutput(ios);
			
			return imageWriter;
		}
		else {
			return null;
		}
	}

	public int getImageCount(String filename) {
		
		try {
			return numberOfVolumes(filename);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 1;
		}
	}

	public ImageObject readImage(String filename, int index, SubArea subarea, int sampling) throws IOException, ImageException {
		ImageObject imgObj = null;
    	File file = new File(filename);
    	try {
    		imgObj = ImageObject.getImageObject(_getImageReader(file).read(index));
    	} catch (ImageException e) {
    		e.printStackTrace();
    	}    		
    	return imgObj;
    	
	}

	public ImageObject readImageHeader(String filename, int index) throws IOException, ImageException {
		return readImageHeader(filename);
	}
}
