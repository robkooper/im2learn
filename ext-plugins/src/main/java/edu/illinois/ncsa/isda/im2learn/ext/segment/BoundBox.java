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
/*
 * Created on Jul 21, 2005
 *
 */
package edu.illinois.ncsa.isda.im2learn.ext.segment;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageException;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.im2learn.ext.segment.Blob;
import edu.illinois.ncsa.isda.im2learn.ext.segment.ConnectAnal;



/**
 * @author Peter Ferak and Peter Bajcsy
 * This is the doer class for the Bounding Box computations. It calls the Blob and ConnectAnalysis classes which return the labels inside an image and then draw bounding boxes. 
 * The bounding boxes are then displayed using a paint() method, this happens in the BoundingBoxDialog class.
 */
public class BoundBox 
{
	private ImageObject 		_imgInput 			= null;
	private ImageObject 		_imgResult 			= null;
	private Blob 				_myBlob 			= new Blob();
	private static Log 			logger 				= LogFactory.getLog(BoundBox.class);


	ConnectAnal myConnectAnalysis;
	
	
	/**
	 * The imageObject from the imagepanel is passed and the parameters for minimal width and height,
	 * which at this point have no function. I want to make sure the class doesn't compute boxes for 1 pixel areas, I'm still working on this.
	 * The method uses the Connectivity Analysis to get the areas around which there are to be bounding boxes. Then creates the bounding boxes 
	 * using the Blob class.
	 * @param imgInput
	 * @param height
	 * @param width
	 * @param area
	 * @return
	 * @throws ImageException
	 */
	public Blob computeBoundingBox(ImageObject imgInput, String height, String width, String area) throws ImageException{
	//public Blob computeBoundingBox(ImageObject imgInput) throws ImageException{

		//sanity check
		if(imgInput == null){
			logger.error("ERROR: imgInput is null");
			return null;
		}


		 myConnectAnalysis = new ConnectAnal();
		 boolean ret;
		 
		 
		 // pass the binary image to the Connect Analysis class and compute the labels and stats
		  //  ret = myConnectAnalysis.binaryBinVal_CA(labelImg);
		  ret = myConnectAnalysis.bandVectorVal_CA(imgInput);
		    if(!ret){
		    	logger.error("ERROR: could not compute Binary_CA");
		       return null;
		    }
		    //myConnectAnalysis.printConnectAnal();
		    //myConnectAnalysis.printConnectAnalAreaS();
		    
		 // initiating the resulting Image Object.  
		    ImageObject resObject = null;
		    resObject = myConnectAnalysis.getImLabels();
		    if(resObject == null){
		        logger.error("ERROR: result connectivity image is null");
		        return null;
		    }
		    
		  //test
		    //logger.debug("TEST: imgResult="+ resObject.toString());
		    
		  // stats of all blobs
		    ret = _myBlob.blobStatistics(myConnectAnalysis.getImLabels(),myConnectAnalysis.getNFoundS() );
		    if(!ret){
		    	logger.error("ERROR: could not compute BlobStats");
		       return null;
		    }
		    logger.debug("Test: Blob stats ");
		    //_myBlob.printBlobStats();
		    
		   // computing the bounding box 
		    ret = _myBlob.blobBoundBox(myConnectAnalysis.getImLabels(),myConnectAnalysis.getNFoundS() );
		    if(!ret){
		    	logger.error("ERROR: could not compute BlobBoundBox");
		       return null;
		    }
		    logger.debug("Test: Blob Box ");
		    //_myBlob.printBlobBox();
		    return _myBlob;
		    
	}
	
	// getter required to print the connect analysis stats from inside the BoundingBoxDialog
	public ConnectAnal getConnectAnalysis() {
		return myConnectAnalysis;
	}
	// getter required to print the connect analysis stats from inside the BoundingBoxDialog
	public Blob getBlob() {
		return _myBlob;
	}
	
}
