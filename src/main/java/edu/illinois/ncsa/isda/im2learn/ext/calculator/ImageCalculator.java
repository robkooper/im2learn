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
 * Created on Jun 23, 2005
 *
 * TODO Some of the function may not work with all types of images, 
 * if statements would need to be included.
 */
package edu.illinois.ncsa.isda.im2learn.ext.calculator;

import javax.swing.JFrame;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageException;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.im2learn.ext.misc.PlotComponent;
import edu.illinois.ncsa.isda.im2learn.ext.statistics.Histogram;

/**
 * <B> The class ImCalculator provides a tool for performing algebraic and
 * boolean operations with images. The algebraic operations are image addition,
 * subtraction, inversion, multiplication, division and average. The boolean
 * operations include AND, OR and XOR. </B> <BR>
 * <BR>
 * <B>Description:</B> The methods supported by ImageCalculator class operate on
 * images at the level of each image pixel. For example, two color images with
 * red, green and blue bands will be added by summing spatially corresponding
 * pixel values in each band. While the number of bands and data type must be
 * consistent in both input images, the image size can vary. The result image
 * will have the spatial dimension based on the minimum of input image
 * dimensions. If the result of any algebraic operation leads to a value that is
 * outside the bounds of the input data type, then all resulting values are
 * scaled so that the output image contains data of the same data type as the
 * input images, unless they are not of the same type. In this case it will
 * create the output of the type of the image that is bigger, with double being
 * the biggest and byte being the smallest. <BR>
 * <BR>
 * The ImageCalculator performs the following algebraic and boolean operations: <BR>
 * Addition <BR>
 * Subtraction <BR>
 * Inversion <BR>
 * Multiplication <BR>
 * Division <BR>
 * Average <BR>
 * Boolean AND <BR>
 * Boolean OR <BR>
 * Boolean XOR <BR>
 * <BR>
 * <BR>
 * <BR>
 * 
 * <B> Setup:</B> In order to execute any operation, two images have to be
 * loaded and thus set as the two operands for the chosen calculation. The image
 * loaded into the frame, by using "LoadIntoFrame", is the first operand
 * (obviously this only matters for Subtract, Divide and Inverse). Thus the
 * FrameImage is the image which the operation is performed on. Loading an image
 * using "Load Image" will make it the second operand, or as I refer to it the
 * ActionImage, which seemed logical because it is the one performing the
 * operation. Therefore if you Subtract/Divide you will Sub/Div the ActionImage
 * from the FrameImage. Inversion is performed on the FrameImage.
 * 
 * 
 * <B>Run:</B> In order to run the tool two images will have to be loaded. A
 * FrameImage ("LoadIntoFrame") and a ActionImage ("Load Image"). Following the
 * desired operation shoud be chosen from the dropdown menu (Add, Subtract, etc)
 * and by pressing the "Execute" Button the calculation will take place and
 * deliver a result which will pop up in a new frame. If one should close the
 * ResultFrame or the ActionImage frame there are buttons two buttons ("Show
 * Image", "Show Result") which will make the image in question appear in a new
 * frame. The ResultImage can be put into the mainframe by utilizing
 * "ResultIntoFrame" and thus it will become the FrameImage which means that
 * operations are now performed on that image. <BR>
 * The summary of the additional buttons is provided next: <BR>
 * Load Image - Loads the second operand and shows the image. <BR>
 * LoadIntoFrame - Loads the first operand and displays it in the mainframe. <BR>
 * Load Many - Loads more than two images, they are not displayed, only "Average
 * Many" can be performed. <BR>
 * Execute - The chosen action from the ComboBox will be performed with the
 * images. <BR>
 * Show Result - displays the result image in a new frame <BR>
 * Save Result - saves the result image. <BR>
 * <BR>
 * <B> Example:</B>
 * 
 * <BR>
 * <BR>
 * 
 * @author Peter Ferak
 * @version 2.0
 * 
 */

public class ImageCalculator {

	private ImageObject			actionImage	= null;
	private final ImageObject	targetImage	= null;
	private ImageObject			resultImage	= null;
	private ImageObject[]		_imgArray	= null;
	public boolean				invert		= false;

	private static Log			logger		= LogFactory.getLog(ImageCalculator.class);

	/**
	 * Return the actionImage.
	 * 
	 * @return the actionImage
	 */
	public ImageObject getActionImage() {
		return actionImage;
	}

	/**
	 * Set the actionImage.
	 * 
	 * @param sets
	 *            the image which is to be the second operand.
	 */
	public void setActionImage(ImageObject actionImage) {
		this.actionImage = actionImage;
	}

	/**
	 * Return the image, if any, resulting from the desired operation.
	 * 
	 * @return the resulting image
	 */
	public ImageObject getResultImage() {
		return resultImage;
	}

	public void setImgArray(ImageObject[] imgArray) {
		this._imgArray = imgArray;
	}

	public ImageObject[] getImgArray() {
		return _imgArray;
	}
	
    /**
     * Generates a histogram for an image masked by applyMask().
     */
    public void plotHistMask(ImageObject imgobj, ImageObject mask){
        try {
        	Histogram hist = new Histogram();
            PlotComponent pc = new PlotComponent();
            for (int i = 0; i < imgobj.getNumBands(); i++) {
                int id = pc.addSeries("Band " + i);
                if (imgobj.getType() != ImageObject.TYPE_BYTE) {
                    hist.SetHistParam256Bins(imgobj);
                }
                //hist.SetNumBins(256);
                hist.HistOmitBackground(imgobj, mask, i);
                int[] data = hist.GetHistData();
                double x = hist.GetMinDataVal();
                for(int j=0; j<data.length; j++, x += hist.GetWideBins()) {
                    pc.setValue(id, x, data[j]);
                }
            }
            JFrame frame = new JFrame("Histogram Plot");
            frame.getContentPane().add(pc);
            frame.pack();
            frame.setVisible(true);
        } catch (ImageException exc) {
            logger.error("Error creating histogram", exc);
        }
    }
	

	/**
	 * Applies a binary mask to a given image, and calculates some statistics.
	 */
	public ImageObject applyMask(ImageObject img, ImageObject mask, double[] statistics) throws ImageException{
		// Begin Error Checking
		if(img == null){
			throw (new ImageException("No image loaded"));
		}
		if(mask == null){
			throw (new ImageException("No mask loaded"));
		}
		int imgcols = img.getNumCols();
		int imgrows = img.getNumRows();
		int maskcols = mask.getNumCols();
		int maskrows = mask.getNumRows();
		if(imgcols != maskcols || imgrows != maskrows){
			throw (new ImageException("Mask must be the same size as image"));
		}
		int maskbands = mask.getNumBands();
		if(maskbands != 1){
			throw (new ImageException("Mask must be a bilevel image"));
		}
		// End Error Checking
		int imgbands = img.getNumBands();
		int imgtype = img.getType();
		int numpixelsforeground = 0;
		ImageObject resultImage = ImageObject.createImage(imgrows, imgcols, imgbands, imgtype);
		for(int i = 0; i < imgbands*2; i++){
			statistics[i] = 0;
		}
		for(int row = 0; row < imgrows; row++){
			for(int col = 0; col < imgcols; col++){
				for(int band = 0; band < imgbands; band++){
					if(mask.getDouble(row,col,0) != 0){
						double pix = img.getDouble(row,col,band);
						resultImage.setDouble(row,col,band,pix);
						statistics[band] += pix;
						statistics[band+imgbands] += Math.pow(pix, 2.0);
						numpixelsforeground++;
					} else {
						resultImage.setDouble(row,col,band,0);
					}
				}
			}
		}
		for(int i = 0; i < imgbands*2; i++){
			statistics[i] /= numpixelsforeground/imgbands;
		}
		for(int j = imgbands; j < imgbands*2; j++){
			statistics[j] -= Math.pow(statistics[j-imgbands], 2);
			statistics[j] = Math.pow(statistics[j], 0.5);
		}
		return resultImage;
	}
	
	
	/**
	 * Performs the add operation on targetImage + actionImage. The operation
	 * goes through each pixel of the two images, bounded by the dimensions of
	 * the result image which in fact has the least amount of rows and columns
	 * out of the two input images. The pixel values are added and then scaled,
	 * this does not work right at the moment, because if you add black and
	 * white the result is a gray, which is counterintuitive.
	 */
	public ImageObject add(ImageObject[] operands) throws ImageException {

		sanityTest(operands);
		resultImage = createResult(operands);

		logger.info("Images added");

		int row, col, band;
		int idxResult = 0;
		double sum;
		double val;
		int[] offset = new int[operands.length];
		int idx[] = new int[operands.length];
		for (int i = 0; i < offset.length; i++) {
			offset[i] = (operands[i].getNumCols() - resultImage.getNumCols()) * operands[i].getNumBands();
			idx[i] = 0;
		}

		for (row = 0; row < resultImage.getNumRows(); row++) {
			for (col = 0; col < resultImage.getNumCols(); col++) {
				for (band = 0; band < resultImage.getNumBands(); band++) {
					sum = 0;
					for (int i = 0; i < operands.length; i++) {
						sum += operands[i].getDouble(idx[i]++);
					}
					val = sum / operands.length;
					resultImage.set(idxResult, val);
					idxResult++;
				}
			}
			for (int i = 0; i < offset.length; i++) {
				idx[i] += offset[i];
			}
		}

		return resultImage;
	}

	/**
	 * Performs the add operation on targetImage - actionImage. The operation
	 * goes through each pixel of the two images, bounded by the dimensions of
	 * the result image which in fact has the least amount of rows and columns
	 * out of the two input images. The pixel values are subtracted from each
	 * other and then scaled to Byte. If statements should be included for
	 * images other than Byte.
	 */
	public ImageObject subtract(ImageObject targetImage, ImageObject actionImage) throws ImageException {

		sanityTest(targetImage, actionImage);
		resultImage = createResult(targetImage, actionImage);
		int typeTarget = targetImage.getType();
		int typeAction = actionImage.getType();
		if (typeTarget >= typeAction) {
			resultImage.setInvalidData(targetImage.getInvalidData());
		} else {
			resultImage.setInvalidData(actionImage.getInvalidData());
		}

		logger.info("Images subtracted");

		int idx, idx1, idx2;
		int i, j, band;
		double val;

		int offsetOne, offsetTwo;
		offsetOne = (targetImage.getNumCols() - resultImage.getNumCols()) * targetImage.getNumBands();
		offsetTwo = (actionImage.getNumCols() - resultImage.getNumCols()) * actionImage.getNumBands();
		double val1, val2;
		idx = idx1 = idx2 = 0;
		for (i = 0; i < resultImage.getNumRows(); i++) {
			for (j = 0; j < resultImage.getNumCols(); j++) {
				for (band = 0; band < resultImage.getNumBands(); band++) {
					val1 = targetImage.getDouble(idx1);
					val2 = actionImage.getDouble(idx2);
					if ((val1 != targetImage.getInvalidData()) && (val2 != actionImage.getInvalidData())) {
						val = val1 - val2;
					} else {
						val = resultImage.getInvalidData();
					}

					resultImage.set(idx, val);
					idx++;
					idx1++;
					idx2++;
				}
			}
			idx1 += offsetOne;
			idx2 += offsetTwo;
		}
		return resultImage;
	}

	/**
	 * Performs the average operation on targetImage and actionImage. The
	 * operation goes through each pixel of the two images, bounded by the
	 * dimensions of the result image which in fact has the least amount of rows
	 * and columns out of the two input images, then finds the average value by
	 * adding the pixel values und dividing by two.
	 */
	public ImageObject average(ImageObject[] operands) throws ImageException {

		sanityTest(operands);
		resultImage = createResult(operands);

		logger.info("Images averaged");

		int row, col, band;
		int idxResult = 0;
		double sum;
		double val;
		int[] offset = new int[operands.length];
		int idx[] = new int[operands.length];
		for (int i = 0; i < offset.length; i++) {
			offset[i] = (operands[i].getNumCols() - resultImage.getNumCols()) * operands[i].getNumBands();
			idx[i] = 0;
		}

		for (row = 0; row < resultImage.getNumRows(); row++) {
			for (col = 0; col < resultImage.getNumCols(); col++) {
				for (band = 0; band < resultImage.getNumBands(); band++) {
					sum = 0;
					for (int i = 0; i < operands.length; i++) {
						sum += operands[i].getDouble(idx[i]++);
					}
					val = sum / operands.length;
					resultImage.set(idxResult, val);
					idxResult++;
				}
			}
			for (int i = 0; i < offset.length; i++) {
				idx[i] += offset[i];
			}
		}

		return resultImage;

	}

	/**
	 * Performs the multiply operation on targetImage * actionImage. The
	 * operation goes through each pixel of the two images, bounded by the
	 * dimensions of the result image which in fact has the least amount of rows
	 * and columns out of the two input images, then multiplies each value and
	 * scales it to Byte. Once again this will not work for images other than
	 * Byte right now. If statements will have to be included.
	 */
	public ImageObject multiply(ImageObject targetImage, ImageObject actionImage) throws ImageException {

		sanityTest(targetImage, actionImage);
		resultImage = createResult(targetImage, actionImage);

		logger.info("Images multiplied");

		int idx, idx1, idx2;
		int i, j, band;
		double val;
		int offsetOne, offsetTwo;
		offsetOne = (targetImage.getNumCols() - resultImage.getNumCols()) * targetImage.getNumBands();
		offsetTwo = (actionImage.getNumCols() - resultImage.getNumCols()) * actionImage.getNumBands();

		idx = idx1 = idx2 = 0;
		for (i = 0; i < resultImage.getNumRows(); i++) {
			for (j = 0; j < resultImage.getNumCols(); j++) {
				for (band = 0; band < resultImage.getNumBands(); band++) {

					val = targetImage.getDouble(idx1) * actionImage.getDouble(idx2);

					resultImage.set(idx, val);
					idx++;
					idx1++;
					idx2++;
				}
			}
			idx1 += offsetOne;
			idx2 += offsetTwo;
		}

		return resultImage;

	}

	/**
	 * Performs the divide operation on targetImage / actionImage. The operation
	 * goes through each pixel of the two images, bounded by the dimensions of
	 * the result image which in fact has the least amount of rows and columns
	 * out of the two input images, then divides the values unless the pixel
	 * value of the actionImage is 0 which would mean a division by zero. In
	 * this case the value 255 will be assigned.
	 */
	public ImageObject divide(ImageObject targetImage, ImageObject actionImage) throws ImageException {

		sanityTest(targetImage, actionImage);
		resultImage = createResult(targetImage, actionImage);
		resultImage.setInvalidData(resultImage.getTypeMin());

		logger.info("Images divided");

		int idx, idx1, idx2;
		int i, j, band;
		double val;
		int offsetOne, offsetTwo;
		offsetOne = (targetImage.getNumCols() - resultImage.getNumCols()) * targetImage.getNumBands();
		offsetTwo = (actionImage.getNumCols() - resultImage.getNumCols()) * actionImage.getNumBands();

		idx = idx1 = idx2 = 0;
		for (i = 0; i < resultImage.getNumRows(); i++) {
			for (j = 0; j < resultImage.getNumCols(); j++) {
				for (band = 0; band < resultImage.getNumBands(); band++) {

					if (actionImage.getDouble(idx2) == 0) {
						val = resultImage.getTypeMin();
					} else {
						val = Math.abs(targetImage.getDouble(idx1) / actionImage.getDouble(idx2));
					}
					resultImage.set(idx, val);
					idx++;
					idx1++;
					idx2++;
				}
			}
			idx1 += offsetOne;
			idx2 += offsetTwo;
		}

		return resultImage;

	}

	/**
	 * Performs the or operation on targetImage and actionImage. The operation
	 * goes through each pixel of the two images, bounded by the dimensions of
	 * the result image which in fact has the least amount of rows and columns
	 * out of the two input images, then performes the boolean operation OR.
	 */
	public ImageObject or(ImageObject[] operands) throws ImageException {

		sanityTest(operands);
		resultImage = createResult(operands);

		logger.info("Images OR");

		int row, col, band;
		int idxResult = 0;
		double sum;
		int val = 0;
		int[] offset = new int[operands.length];
		int idx[] = new int[operands.length];
		for (int i = 0; i < offset.length; i++) {
			offset[i] = (operands[i].getNumCols() - resultImage.getNumCols()) * operands[i].getNumBands();
			idx[i] = 0;
		}

		for (row = 0; row < resultImage.getNumRows(); row++) {
			for (col = 0; col < resultImage.getNumCols(); col++) {
				for (band = 0; band < resultImage.getNumBands(); band++) {
					for (int i = 0; i < operands.length; i++) {
						if (i == 0) {
							val = operands[i].getByte(idx[i]++);
							continue;
						}
						val = (val | operands[i].getByte(idx[i]++));
					}
					resultImage.set(idxResult, val);
					idxResult++;
				}
			}
			for (int i = 0; i < offset.length; i++) {
				idx[i] += offset[i];
			}
		}

		//return invert?invert(resultImage):resultImage;	
		return resultImage;
		/*
		 * sanityTest(targetImage,actionImage); resultImage =
		 * createResult(targetImage,actionImage);
		 * 
		 * logger.info("Image OR");
		 * 
		 * int idx,idx1,idx2; int i,j,band; double val; int offsetOne,
		 * offsetTwo; offsetOne = (targetImage.getNumCols() -
		 * resultImage.getNumCols()) targetImage.getNumBands(); offsetTwo =
		 * (actionImage.getNumCols() - resultImage.getNumCols())
		 * actionImage.getNumBands();
		 * 
		 * 
		 * idx = idx1 = idx2 = 0; for( i=0; i < resultImage.getNumRows(); i ++
		 * ){ for( j=0; j < resultImage.getNumCols(); j ++ ){ for(band = 0; band
		 * < resultImage.getNumBands(); band ++){
		 * 
		 * val = (targetImage.getByte(idx1) | actionImage.getByte(idx2));
		 * 
		 * resultImage.set(idx, val); idx++; idx1++; idx2++; } }
		 * idx1+=offsetOne; idx2+=offsetTwo; } return resultImage; /
		 */

	}

	/**
	 * Performs the xor operation on targetImage and actionImage. The operation
	 * goes through each pixel of the two images, bounded by the dimensions of
	 * the result image which in fact has the least amount of rows and columns
	 * out of the two input images, then performes the boolean operation XOR.
	 */
	public ImageObject xor(ImageObject targetImage, ImageObject actionImage) throws ImageException {

		sanityTest(targetImage, actionImage);
		resultImage = createResult(targetImage, actionImage);

		logger.info("Image XOR");

		int idx, idx1, idx2;
		int i, j, band;
		double val;
		int offsetOne, offsetTwo;
		offsetOne = (targetImage.getNumCols() - resultImage.getNumCols()) * targetImage.getNumBands();
		offsetTwo = (actionImage.getNumCols() - resultImage.getNumCols()) * actionImage.getNumBands();

		idx = idx1 = idx2 = 0;
		for (i = 0; i < resultImage.getNumRows(); i++) {
			for (j = 0; j < resultImage.getNumCols(); j++) {
				for (band = 0; band < resultImage.getNumBands(); band++) {

					val = (targetImage.getByte(idx1) ^ actionImage.getByte(idx2));

					resultImage.set(idx, val);
					idx++;
					idx1++;
					idx2++;
				}
			}
			idx1 += offsetOne;
			idx2 += offsetTwo;
		}
		return resultImage;
		//return invert?invert(resultImage):resultImage;	
	}

	/**
	 * Performs the and operation on targetImage and actionImage. The operation
	 * goes through each pixel of the two images, bounded by the dimensions of
	 * the result image which in fact has the least amount of rows and columns
	 * out of the two input images, then performes the boolean operation AND.
	 */
	public ImageObject and(ImageObject[] operands) throws ImageException {
		sanityTest(operands);
		resultImage = createResult(operands);

		logger.info("Images AND");

		int row, col, band;
		int idxResult = 0;
		double sum;
		int val = 0;
		int[] offset = new int[operands.length];
		int idx[] = new int[operands.length];
		for (int i = 0; i < offset.length; i++) {
			offset[i] = (operands[i].getNumCols() - resultImage.getNumCols()) * operands[i].getNumBands();
			idx[i] = 0;
		}

		for (row = 0; row < resultImage.getNumRows(); row++) {
			for (col = 0; col < resultImage.getNumCols(); col++) {
				for (band = 0; band < resultImage.getNumBands(); band++) {
					for (int i = 0; i < operands.length; i++) {
						if (i == 0) {
							val = operands[i].getByte(idx[i]++);
							continue;
						}
						val = (val & operands[i].getByte(idx[i]++));
					}
					resultImage.set(idxResult, val);
					idxResult++;
				}
			}
			for (int i = 0; i < offset.length; i++) {
				idx[i] += offset[i];
			}
		}

		//return invert?invert(resultImage):resultImage;	
		return resultImage;
		/*
		 * sanityTest(targetImage,actionImage); resultImage =
		 * createResult(targetImage,actionImage);
		 * 
		 * logger.info("Image AND");
		 * 
		 * int idx,idx1,idx2; int i,j,band; double val; int offsetOne,
		 * offsetTwo; offsetOne = (targetImage.getNumCols() -
		 * resultImage.getNumCols()) targetImage.getNumBands(); offsetTwo =
		 * (actionImage.getNumCols() - resultImage.getNumCols())
		 * actionImage.getNumBands();
		 * 
		 * 
		 * idx = idx1 = idx2 = 0; for( i=0; i < resultImage.getNumRows(); i ++
		 * ){ for( j=0; j < resultImage.getNumCols(); j ++ ){ for(band = 0; band
		 * < resultImage.getNumBands(); band ++){
		 * 
		 * val = (targetImage.getByte(idx1) & actionImage.getByte(idx2));
		 * 
		 * resultImage.set(idx, val); idx++; idx1++; idx2++; } }
		 * idx1+=offsetOne; idx2+=offsetTwo; } return resultImage;
		 */
	}

	/**
	 * Calculates the inverse of the targetImage.
	 */
	//TODO fix
	public ImageObject invert(ImageObject targetImage) throws ImageException {
		if (targetImage == null) {
			throw (new ImageException("No image loaded."));
		}
		logger.info("Image inverse");

		resultImage = ImageObject.createImage(targetImage.getNumRows(), targetImage.getNumCols(), targetImage.getNumBands(), targetImage.getType());

		int idx;
		double val;
		final byte MAX_BYTE = 127;
		final int MAXPOS_BYTE = 256;

		for (idx = 0; idx < targetImage.getSize(); idx++) {
			val = targetImage.getByte(idx);
			if (val < 0) {
				val += MAXPOS_BYTE;
			}

			val = MAXPOS_BYTE - 1 - val;
			if (val > MAX_BYTE) {
				val -= MAXPOS_BYTE;
			}

			resultImage.set(idx, val);
		}

		return resultImage;
	}

	/**
	 * Performs a sanity test in order to sort out faulty input.
	 * 
	 * @throws ImageException
	 */
	private void sanityTest(ImageObject target, ImageObject action) throws ImageException {
		if (target == null) {
			throw (new ImageException("No target image loaded."));
		}

		if (action == null) {
			throw (new ImageException("No action image loaded."));
		}

		if (action.getNumBands() != target.getNumBands()) {
			throw (new ImageException("ActionImage and targetImage have not the same bands."));
		}

		//if (!actionImage.getTypeString().equalsIgnoreCase(targetImage.getTypeString())) {
		//	throw(new ImageException("Band type of ActionImage is inconsistent with the band type of targetImage"));
		//}

	}

	private void sanityTest(ImageObject[] operands) throws ImageException {

		if (operands.length < 2) {
			logger.error("At least two operands have to be loaded.");
			return;
		}
		int bands = 0;
		for (int i = 0; i < operands.length; i++) {
			if (operands[i] != null) {
				if (i == 0) {
					bands = operands[i].getNumBands();
					continue;
				}
				if (operands[i].getNumBands() != bands) {
					throw (new ImageException("Not all operands have the same bands."));
				}

			} else {
				throw (new ImageException("Operands " + i + "is null."));
			}
		}

	}

	/**
	 * Creates the result image and sets the dimensions least amount of rows and
	 * columns, in other words all pixels which are included in both images. It
	 * also finds the appropriate output type. If the type of the two images is
	 * the same it will create the ImageObject using this type. If this is not
	 * the case it will find the type that is the bigger of the two images and
	 * create the ImageObject with this type.
	 * 
	 * @return
	 * @throws ImageException
	 */
	private ImageObject createResult(ImageObject targetImage, ImageObject actionImage) throws ImageException {
		// create the result image

		int numrows, numcols;
		numrows = actionImage.getNumRows();
		if (numrows > targetImage.getNumRows()) {
			numrows = targetImage.getNumRows();
		}
		numcols = actionImage.getNumCols();
		if (numcols > targetImage.getNumCols()) {
			numcols = targetImage.getNumCols();
		}

		int typeTarget = targetImage.getType();
		int typeAction = actionImage.getType();
		int type = (typeTarget >= typeAction) ? typeTarget : typeAction;
		int bands = targetImage.getNumBands();
		return ImageObject.createImage(numrows, numcols, bands, type);

	}

	private ImageObject createResult(ImageObject[] operands) throws ImageException {
		// create the result image

		int numrows, numcols;
		numrows = operands[0].getNumRows();
		numcols = operands[0].getNumCols();
		for (int i = 1; i < operands.length; i++) {
			if (numrows > operands[i].getNumRows()) {
				numrows = operands[i].getNumRows();
			}
			if (numcols > operands[i].getNumCols()) {
				numcols = operands[i].getNumCols();
			}
		}

		int type = 0;
		for (int i = 0; i < operands.length; i++) {
			if ((operands[i].getType() > type) && (operands[i].getType() != ImageObject.TYPE_UNKNOWN)) {
				type = operands[i].getType();
			}
		}
		return ImageObject.createImage(numrows, numcols, operands[0].getNumBands(), type);

	}

    /**
     * Performs the absolute operation on actionImage. The operation
     * goes through each pixel of the two images, bounded by the dimensions of
     * the result image which in fact has the least amount of rows and columns
     * out of the two input images. The pixel values are absolute values of the pixels of 
     * the action Image 
     */
    public ImageObject abs(ImageObject actionImage) throws ImageException
    {

        if (actionImage == null)
        {
            throw (new ImageException("No action image loaded."));
        }
        try {
			resultImage = ImageObject.createImage(actionImage.getNumRows(), actionImage.getNumCols(),actionImage.getNumBands(),actionImage.getType());
		} catch (Exception e) {
			// TODO Auto-generated catch block		
			logger.error("Could create result image");
		}
        resultImage.setInvalidData(-999999);

        logger.info("Images absolute value");

        int idx;
        double val;
        for (idx = 0; idx < resultImage.getSize(); idx++)
        {
        	val = actionImage.getDouble(idx);
        	resultImage.set(idx, Math.abs(val));
        }
        return resultImage;
    }
}
