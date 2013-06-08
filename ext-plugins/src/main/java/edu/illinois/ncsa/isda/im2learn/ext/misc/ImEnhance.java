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
package edu.illinois.ncsa.isda.im2learn.ext.misc;

/**
 * 
 * @author Peter Bajcsy
 * @version 1.0
 */
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageException;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObjectByte;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObjectInt;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObjectShort;
import edu.illinois.ncsa.isda.im2learn.core.datatype.LimitValues;
import edu.illinois.ncsa.isda.im2learn.ext.statistics.Histogram;

////////////////////////////////////////////////////////////////////
// this is the class that performs enhancement of image labels
// for visualization purposes
//there are two modes for this class
// (1) instantiate with the label ImageObject  and then return new ImageObject
// with enhanced data
// (2) Instantitate the class and override the input file with the
// enhanced values
////////////////////////////////////////////////////////////////////
public class ImEnhance {
	public boolean	_debugImEnhance		= true;
	private int		_methodImEnhance	= 0;

	ImageObject		_enhancedObject		= null;

	LimitValues		_lim				= new LimitValues();
	//Sampling _mySampling = new Sampling();
	int				_estNumLabels		= -1;

	//Histogram myh = new Histogram();

	//constructors
	public ImEnhance() {

	}

	/*
	 * public ImEnhance(ImageObject labels){ if(labels == null){ System.out.println("ERROR: no data"); return; } _labels =
	 * labels; }
	 */
	// setters
	public boolean SetMethodImEnhance(int val) {
		if (val >= 0 && val < 4) {
			_methodImEnhance = val;
			return true;
		} else {
			System.out.println("ERROR: invalid method");
			return false;
		}
	}

	//getters
	public ImageObject GetEnhancedObject() {
		return _enhancedObject;
	}

	public int GetMethodImEnhance() {
		return _methodImEnhance;
	}

	//doers
	// this is not complete !!!
	/*
	 * public boolean EnhanceLabelsOver(ImageObject labels, int numLabels){ //sanity check if(labels == null ){
	 * System.out.println("ERROR: missing labels data"); return false; } if(numLabels <= 0 ){ System.out.println("ERROR:
	 * numLabels is invalid"); return false; } if(labels.sampType.equalsIgnoreCase("BYTE")== false &&
	 * labels.sampType.equalsIgnoreCase("BYTE")== false){ System.out.println("ERROR: input can be only BYTE or INT");
	 * return false; }
	 * 
	 * double enhance ; int idxLabel; if(labels.sampType.equalsIgnoreCase("BYTE") ){ enhance= 254.0/(numLabels-1);
	 * for(idxLabel=0;idxLabel<labels.size;idxLabel++){ labels.image[idxLabel] =
	 * (byte)((int)((double)labels.image[idxLabel]*enhance) - 128); // if(idxLabel < 10){ // System.out.println("Test:
	 * labels["+idxLabel +"]="+ (int)_labels.image[idxLabel]); // } } }else{ // not yet figured out
	 * System.out.println("Info: not yet figured out how to enhance INT"); //_labels.imageInt[idxLabel] = lab; } return
	 * true; }
	 */
	////////////////////////////////////////////////////////////////////////
	// this is the method that enhances labels (INT or BYTE) similar to
	//histogram equalization
	public boolean EnhanceLabelsIn(ImageObject labels, int numLabels, boolean color) {
		_enhancedObject = null;
		_enhancedObject = EnhanceLabelsOut(labels, numLabels, color);
		if (_enhancedObject == null) {
			System.out.println("ERROR: could not enhance");
			return false;
		} else {
			return true;
		}

	}

	// this is the method that enhances labels (INT or BYTE) similar to
	//histogram equalization
	public ImageObject EnhanceLabelsOut(ImageObject labels, int numLabels, boolean color) {
		//sanity check
		if (labels == null) {
			System.out.println("ERROR: missing labels data");
			return null;
		}
		//labels.getType()
		if (labels.getTypeString().equalsIgnoreCase("BYTE") == false && labels.getTypeString().equalsIgnoreCase("INT") == false) {
			System.out.println("ERROR: input can be only BYTE or INT");
			return null;
		}
		if (numLabels <= 0) {
			if ((numLabels = FindNumLabels(labels)) > 0) {
				System.out.println("TEST: estimated numLabels=" + numLabels);
				numLabels++;
			} else {
				System.out.println("ERROR: numLabels is invalid and we could not estimate the numLabels");
				return null;
			}
		}

		ImageObject enhanced = null; // = new ImageObject();
		//enhanced.numrows = labels.numrows;
		//enhanced.numcols = labels.numcols;
		enhanced = new ImageObjectInt(1, 1, 1); // dummy initilization, for the stupid JBuilder

		if (color == false) {
			// gray scale enhanced
			enhanced = new ImageObjectByte(labels.getNumRows(), labels.getNumCols(), labels.getNumBands());
			//enhanced.sampType = labels.sampType;
			//enhanced.setSize(); = labels.size;
			//enhanced.sampPerPixel = labels.sampPerPixel;
			//enhanced.image = new byte[(int)enhanced.size];
		} else {
			// rgb enhanced
			enhanced = new ImageObjectInt(labels.getNumRows(), labels.getNumCols(), labels.getNumBands());
			//enhanced.sampType = "INT";
			//enhanced.sampPerPixel = labels.sampPerPixel;//1
			//enhanced.size = enhanced.numrows * enhanced.numcols * enhanced.sampPerPixel;
			//enhanced.imageInt = new int[(int)enhanced.size];
		}

		double enhance;
		int idxLabel;
		double invalid = labels.getInvalidData();
		if (labels.getTypeString().equalsIgnoreCase("BYTE")) {
			if (color == false) {
				enhance = 254.0 / (numLabels - 1);
				for (idxLabel = 0; idxLabel < labels.getSize(); idxLabel++) {
					enhanced.set(idxLabel, (byte) ((int) (labels.getByte(idxLabel) * enhance) - 128));
					// if(idxLabel < 10){
					//   System.out.println("Test: labels["+idxLabel +"]="+ (int)_labels.image[idxLabel]);
					// }
				}
			} else {
				//rgb color mapping
				ImageObject colorMap;
				colorMap = GenColorMap(numLabels);
				int lab;
				for (idxLabel = 0; idxLabel < labels.getSize(); idxLabel++) {
					lab = (labels.getByte(idxLabel));
					if (lab == invalid) {
						lab = 0;
					}
					else if (lab < 0 || lab >= numLabels) {
						System.out.println("Error: label is out of bounds=" + lab);
						lab = 0;
					}
					enhanced.setInt(idxLabel, colorMap.getInt(lab));
					//if(idxLabel < 10){
					//  System.out.println("Test: labels["+idxLabel +"]="+ (int)_labels.image[idxLabel]);
					//  System.out.println("Test: enhanced["+idxLabel +"]="+ enhanced.imageInt[idxLabel]);
					//}
				}

			}
		} else {
			if (color == false) {
				System.out.println("ERROR:enhance INT: gray scale is not allowed");
				return null;
			}
			//rgb color mapping
			ImageObject colorMap;
			//test
			//System.out.println("TEST: numLabels="+numLabels);
			colorMap = GenColorMap(numLabels);
			//test
			//System.out.println("TEST: numLabels="+numLabels);

			int lab;
			for (idxLabel = 0; idxLabel < labels.getSize(); idxLabel++) {
				lab = labels.getInt(idxLabel);
				if (lab == invalid) {
					lab = 0;
				}
				else if (lab < 0 || lab >= numLabels) {
					System.out.println("Error: INT image - color label is out of bounds=" + lab);
					lab = 0;
				}
				if (lab != 0) {
					enhanced.setInt(idxLabel, colorMap.getInt(lab));
				} else {
					enhanced.setInt(idxLabel, 0xff000000);
					//if(idxLabel < 10){
					//  System.out.println("Test: labels["+idxLabel +"]="+ (int)_labels.image[idxLabel]);
					//  System.out.println("Test: enhanced["+idxLabel +"]="+ enhanced.imageInt[idxLabel]);
					//}
				}
			}

		}
		return (enhanced);
	}

	//////////////////////////////////
	private int FindNumLabels(ImageObject labels) {
		_estNumLabels = -1;
		int i;
		if (labels.getTypeString().equalsIgnoreCase("BYTE")) {
			for (i = 0; i < labels.getSize(); i++) {
				if ((labels.getByte(i) & 0xff) > _estNumLabels) {
					_estNumLabels = (labels.getByte(i) & 0xff);
				}
			}
		}

		if (labels.getTypeString().equalsIgnoreCase("INT")) {
			for (i = 0; i < labels.getSize(); i++) {
				if (labels.getInt(i) > _estNumLabels) {
					_estNumLabels = labels.getInt(i);
				}
			}
		}
		return _estNumLabels;
	}

	private boolean InsertBand(ImageObject imObjectIn, int bandIn, int bandOut, ImageObject imObjectOut) {

		// sanity check
		if (imObjectIn == null || imObjectOut == null) {
			System.out.println("ERROR: missing image  data");
			return false;
		}
		if (!imObjectIn.getTypeString().equalsIgnoreCase(imObjectOut.getTypeString())) {
			System.out.println("ERROR: different data type of imObjectIn and imObjectOut");
			return false;
		}
		//if( imObjectIn.sampPerPixel != imObjectOut.sampPerPixel ){
		//   System.out.println("ERROR: sampPerPixel is different in imObjectIn and imObjectOut");
		//   return false;
		//}
		if (bandIn < 0 || bandIn >= imObjectIn.getNumBands()) {
			System.out.println("ERROR: bandIn is out of range = " + bandIn);
			return false;
		}
		if (bandOut < 0 || bandOut >= imObjectOut.getNumBands()) {
			System.out.println("ERROR: bandOut is out of range = " + bandOut);
			return false;
		}

		int idxIn, idxOut;

		for (idxIn = bandIn, idxOut = bandOut; idxIn < imObjectIn.getSize(); idxIn += imObjectIn.getNumBands()) {
			imObjectOut.set(idxOut, imObjectIn.getDouble(idxIn));
			idxOut += imObjectOut.getNumBands();
		}

		return true;
	}

	/////////////////////////////////////////////////
	// this method creates colors for dipslay of Labels
	public ImageObject GenColorMap(int numColors) {

		if (numColors <= 0) {
			System.err.println("ERROR: numColors <= 0; " + numColors);
			return null;
		}
		ImageObject colorObject = new ImageObjectInt(1, numColors, 1);
		//colorObject.numrows = 1;
		//colorObject.numcols = numColors;
		//colorObject.sampPerPixel = 1;
		//colorObject.size = numColors;
		//colorObject.sampType = "INT";
		//colorObject.imageInt = new int[(int)colorObject.size];

		/*
		 * // compute extra parameters for location of colors double a = 255.0; double b = Math.sqrt(3.0) * 0.5 * 255.0;
		 * double c = b; double s = (a + b + c) * 0.5; double p = Math.sqrt( s*(s-a)*(s-b)*(s-c) ); //Heron's formula
		 * for area of a triangle double ro = p/s; // radius of a max circle contained by a triangle double project = ro *
		 * 0.3535533905; //ro * Math.sqrt(2.0) * 0.5; = ro *sin(45deg) //test System.out.println("test: Herons formula s
		 * ="+s+" p="+p+" ro="+ro+" project="+project); int projectInt = (int) (project+0.5);
		 */
		int projectInt = 24;
		int j;
		if (numColors <= 21) {
			for (j = 0; j < colorObject.getSize(); j++) {
				switch (j + 1) {
				case 1:
					//red
					colorObject.setInt(j, 0xff << 24 | 255 << 16);
					break;
				case 2:
					//green
					colorObject.setInt(j, 0xff << 24 | 255 << 8);
					break;
				case 3:
					//blue
					colorObject.setInt(j, 0xff << 24 | 255);
					break;
				case 4:
					//yellow
					colorObject.setInt(j, 0xff << 24 | 255 << 16 | 255 << 8);
					break;
				case 5:
					//purple
					colorObject.setInt(j, 0xff << 24 | 255 << 16 | 255);
					break;
				case 6:
					//cyan
					colorObject.setInt(j, 0xff << 24 | 255 << 8 | 255);
					break;
				case 7:
					//white
					colorObject.setInt(j, 0xff << 24 | 255 << 16 | 255 << 8 | 255);
					break;
				case 8:
					// black
					colorObject.setInt(j, 0xff << 24);
					break;
				case 9:
					//center
					colorObject.setInt(j, 0xff << 24 | 128 << 16 | 128 << 8 | 128);
					break;
				case 10:
					//1,4,9 = (255-project),128, project
					colorObject.setInt(j, 0xff << 24 | (255 - projectInt) << 16 | 128 << 8 | projectInt);
					break;
				case 11:
					//4,2,9 = 128,255-project,project
					colorObject.setInt(j, 0xff << 24 | 128 << 16 | (255 - projectInt) << 8 | projectInt);
					break;
				case 12:
					//2,8,9 = project, 128, project
					colorObject.setInt(j, 0xff << 24 | projectInt << 16 | 128 << 8 | projectInt);
					break;
				case 13:
					//1,8,9 = 128, project,project
					colorObject.setInt(j, 0xff << 24 | 128 << 16 | projectInt << 8 | projectInt);
					break;
				case 14:
					//5,7,9 =
					colorObject.setInt(j, 0xff << 24 | (255 - projectInt) << 16 | 128 << 8 | (255 - projectInt));
					break;
				case 15:
					//
					colorObject.setInt(j, 0xff << 24 | 128 << 16 | (255 - projectInt) << 8 | (255 - projectInt));
					break;
				case 16:
					//
					colorObject.setInt(j, 0xff << 24 | projectInt << 16 | 128 << 8 | (255 - projectInt));
					break;
				case 17:
					//
					colorObject.setInt(j, 0xff << 24 | 128 << 16 | projectInt << 8 | (255 - projectInt));
					break;
				case 18:
					// 1,5,9
					colorObject.setInt(j, 0xff << 24 | (255 - projectInt) << 16 | projectInt << 8 | 128);
					break;
				case 19:
					//4,7,9
					colorObject.setInt(j, 0xff << 24 | (255 - projectInt) << 16 | (255 - projectInt) << 8 | 128);
					break;
				case 20:
					//2,6,9
					colorObject.setInt(j, 0xff << 24 | projectInt << 16 | (255 - projectInt) << 8 | 128);
					break;
				case 21:
					//3,8,9
					colorObject.setInt(j, 0xff << 24 | projectInt << 16 | projectInt << 8 | 128);
					break;

				default:
					// black
					colorObject.setInt(j, 0xff << 24);
					break;
				}
			}
		} else {
			// distribute colors over the cube
			int k, red1, red2, green1, green2, blue1, blue2;
			red1 = green1 = blue1 = 0;

			int step = 128;
			double val = Math.pow(numColors, 1.0 / 3.0);
			if (val > 0.0) {
				step = (int) (255.0 / val);
			} else {
				step = 1;
				System.out.println("ERROR: Could not compute step; step =" + step + " numColors=" + numColors + " val=" + val);
			}
			//step
			System.out.println("Test: step =" + step + " numColors=" + numColors + " val=" + val);
			val = Math.pow(val, 3.0);
			System.out.println("Test: val to ^3: val=" + val);

			boolean signal = true;

			j = 0;
			int startRed, startGreen, startBlue;
			int colorGap = step << 2;
			for (startRed = 0; signal && startRed < colorGap; startRed += step) {
				for (red1 = startRed; signal && red1 <= 256; red1 += colorGap) {
					for (startGreen = 0; signal && startGreen < colorGap; startGreen += step) {
						for (green1 = startGreen; signal && green1 <= 256; green1 += colorGap) {
							for (startBlue = 0; signal && startBlue < colorGap; startBlue += step) {
								for (blue1 = startBlue; signal && blue1 <= 256; blue1 += colorGap) {
									if (j < colorObject.getSize()) {
										colorObject.setInt(j, 0xff << 24 | red1 << 16 | green1 << 8 | blue1);
										//test
										//System.out.println("Test: 1 for j="+j +":red =" +red1 + " green="+green1+ " blue="+blue1);
										j++;
									} else {
										signal = false;
									}
								}// end of blue
							}
						}// end of green
					}
				}// end of red
			}// end of startRed
			if (j < colorObject.getSize()) {
				System.out.println("ERROR: insufficient number of colors=" + j + " were generated < " + colorObject.getSize());
				System.out.println("ERROR: start=" + startRed + " colorGap =" + colorGap);
				System.out.println("ERROR: red=" + red1 + " green1=" + green1 + " blue=" + blue1);

			}
		}// end of else

		return (colorObject);
	}

	//////////////////////////////////////////////////////////////////////////
	public boolean ReplaceLabelsWithCentroidsIn(ImageObject labels, ImageObject centroids) {
		_enhancedObject = null;
		_enhancedObject = ReplaceLabelsWithCentroidsOut(labels, centroids);
		if (_enhancedObject == null) {
			System.out.println("ERROR: could not enhance");
			return false;
		} else {
			return true;
		}
	}

	public ImageObject ReplaceLabelsWithCentroidsOut(ImageObject labels, ImageObject centroids) {
		//sanity check
		if (labels == null || centroids == null) {
			System.out.println("ERROR: missing labels or centroids data");
			return null;
		}
		if (centroids.getTypeString().equalsIgnoreCase("DOUBLE") == false) {
			System.out.println("ERROR: centroids must be DOUBLE");
			return null;
		}
		if (labels.getTypeString().equalsIgnoreCase("BYTE") == false && labels.getTypeString().equalsIgnoreCase("INT") == false) {
			System.out.println("ERROR: input labels are supported only BYTE or INT");
			return null;
		}
		if (centroids.getData() == null) {
			System.out.println("ERROR: no centroid data");
			return null;
		}
		if (centroids.getNumCols() > 255 && labels.getTypeString().equalsIgnoreCase("BYTE")) {
			System.out.println("ERROR: there are more than BYTE centroids but only BYTE per label");
			return null;
		}

		ImageObject enhanced = new ImageObjectInt(1, 1, 1);
		//enhanced.numrows = labels.numrows;
		//enhanced.numcols = labels.numcols;
		if (centroids.getNumCols() <= 255 && labels.getTypeString().equalsIgnoreCase("BYTE")) {
			enhanced = new ImageObjectByte(labels.getNumRows(), labels.getNumCols(), centroids.getNumBands());
			//enhanced.sampType = "BYTE";
			//enhanced.sampPerPixel = centroids.sampPerPixel;
			//enhanced.size = enhanced.numrows * enhanced.numcols * enhanced.sampPerPixel;
			//enhanced.image = new byte[(int)enhanced.size];
		} else {
			enhanced = new ImageObjectInt(labels.getNumRows(), labels.getNumCols(), centroids.getNumBands());
			//enhanced.sampType = "INT";
			//enhanced.sampPerPixel = centroids.sampPerPixel;
			//enhanced.size = enhanced.numrows * enhanced.numcols * enhanced.sampPerPixel;
			//enhanced.imageInt = new int[(int)enhanced.size];
		}

		double enhance;
		int j, lab, idxLabel, idxCentroids, idxEnhanced = 0;
		if (labels.getTypeString().equalsIgnoreCase("BYTE")) {
			for (idxLabel = 0; idxLabel < labels.getSize(); idxLabel++) {
				lab = (labels.getByte(idxLabel));
				if (lab < 0 || lab >= centroids.getNumCols()) {
					System.out.println("Error: label is out of bounds=" + lab);
					lab = 0;
				}
				idxCentroids = lab * centroids.getNumBands();
				for (j = 0; j < centroids.getNumBands(); j++) {
					enhanced.setByte(idxEnhanced, (byte) (centroids.getDouble(idxCentroids + j) + 0.5));
					idxEnhanced++;
				}
				//if(idxLabel < 10){
				//  System.out.println("Test: labels["+idxLabel +"]="+ (int)_labels.image[idxLabel]);
				//  System.out.println("Test: enhanced["+idxLabel +"]="+ enhanced.imageInt[idxLabel]);
				//}
			}
		}
		if (labels.getTypeString().equalsIgnoreCase("INT")) {
			// INT labels
			for (idxLabel = 0; idxLabel < labels.getSize(); idxLabel++) {
				lab = labels.getInt(idxLabel);
				if (lab < 0 || lab >= centroids.getNumCols()) {
					System.out.println("Error: label is out of bounds=" + lab);
					lab = 0;
				}
				idxCentroids = lab * centroids.getNumBands();
				for (j = 0; j < centroids.getNumBands(); j++) {
					enhanced.setInt(idxEnhanced, (int) (centroids.getDouble(idxCentroids + j) + 0.5));
					idxEnhanced++;
				}

				//if(idxLabel < 10){
				//  System.out.println("Test: labels["+idxLabel +"]="+ (int)_labels.image[idxLabel]);
				//  System.out.println("Test: enhanced["+idxLabel +"]="+ enhanced.imageInt[idxLabel]);
				//}
			}
		}
		return (enhanced);
	}// end of ReplaceLabelsWithCentroids

	// this is the method that does hist equalization (BYTE and SHORT only)
	public ImageObject HistEqual2BYTEOut(ImageObject imObject, int[] bandAr, int numBands) throws ImageException {
		//sanity check
		if (imObject == null) {
			System.out.println("ERROR: missing input data");
			return null;
		}
		if (imObject.getTypeString().equalsIgnoreCase("BYTE") == false && imObject.getTypeString().equalsIgnoreCase("SHORT") == false && imObject.getTypeString().equalsIgnoreCase("INT") == false) {
			System.out.println("ERROR: input can be only BYTE or SHORT or INT ");
			return null;
		}
		if (bandAr == null || numBands <= 0) {
			System.out.println("ERROR: missing band info data");
			return null;
		}

		int band = 0;
		for (band = 0; band < numBands; band++) {
			if (bandAr[band] < 1 || bandAr[band] > imObject.getNumBands()) {
				System.out.println("ERROR: bandAr[" + band + "]=" + bandAr[band] + " is out of [0," + imObject.getNumBands() + "] range");
				return null;
			}
		}

		Histogram myh = new Histogram();
		// 0 is the band index
		//int [] min = new int[imObject.sampPerPixel];
		//int [] max = new int[imObject.sampPerPixel];

		int[] min = new int[numBands];
		int[] max = new int[numBands];
		//test with percentile
		int[] upperP = new int[numBands];
		int[] lowerP = new int[numBands];
		double percent = 0.01;

		int idx, idxIn;
		//for( band = 0; band< imObject.sampPerPixel;band++){
		for (idx = 0; idx < numBands; idx++) {
			band = bandAr[idx] - 1;
			myh.Hist(imObject, band);
			myh.MinMaxHistBin();
			/*
			 * if( !myh.Hist(imObject,band) ){ System.out.println("ERROR: failed computing histogram "); return null; }
			 * if( !myh.MinMaxHistBin() ){ System.out.println("ERROR: failed computing min and Max of histogram ");
			 * return null; }
			 */
			//min[band] = myh.GetMinHistBin();
			//max[band] = myh.GetMaxHistBin();
			min[idx] = myh.GetMinHistBin();
			max[idx] = myh.GetMaxHistBin();

			if (!myh.LowerPercentile(percent)) {
				System.out.println("ERROR: failed computing lower percentile of histogram  ");
				return null;
			}
			if (!myh.UpperPercentile(percent)) {
				System.out.println("ERROR: failed computing upper percentile of histogram  ");
				return null;
			}
			upperP[idx] = myh.GetUpperPercentile();
			lowerP[idx] = myh.GetLowerPercentile();

			//test
			System.out.println("Test: band=" + band + ",max=" + max[idx] + ", min=" + min[idx]);
			System.out.println("Test: band=" + band + ",upper=" + upperP[idx] + ", lower=" + lowerP[idx]);
			//for(idxIn=0;idxIn<myh.GetNumBins();idxIn++){
			//  System.out.print("hist["+idxIn+"]="+myh._HistData[idxIn]+",");
			//}
		}

		//ImageObject enhanced = new ImageObject(imObject.numrows,imObject.numcols, imObject.sampPerPixel, imObject.sampType);

		ImageObject enhanced = new ImageObjectInt(1, 1, numBands);

		if (imObject.getType() == ImageObject.TYPE_BYTE) {
			enhanced = new ImageObjectByte(imObject.getNumRows(), imObject.getNumCols(), numBands);
		} else if (imObject.getType() == ImageObject.TYPE_SHORT) {
			enhanced = new ImageObjectShort(imObject.getNumRows(), imObject.getNumCols(), numBands);
		} else if (imObject.getType() == ImageObject.TYPE_INT) {
			enhanced = new ImageObjectInt(imObject.getNumRows(), imObject.getNumCols(), numBands);
		}

		boolean ret = true;
		double coef = 1.0;
		double val;
		double temp;

		//int [] selectBand = new int[1];
		//for( band = 0; band< imObject.sampPerPixel;band++){
		for (band = 0; band < numBands; band++) {
			//test
			System.out.println("test: band=" + band + ", numBands=" + numBands);

			if (max[band] - min[band] <= 0) {
				System.out.println("Warning: max and min are the same at band=" + band);
				//selectBand[0] = bandAr[band];
				ret = InsertBand(imObject, (bandAr[band] - 1), band, enhanced);
				if (!ret) {
					System.out.println("ERROR: could not insert selected band=" + bandAr[band]);
					return null;
				}
				continue;
			}

			//test
			System.out.println("Info: passed first test");

			if (imObject.getTypeString().equalsIgnoreCase("BYTE")) {
				if (max[band] - min[band] > (_lim.MAXPOS_BYTE - 1) * 0.9) {
					System.out.println("Info: dynamic range of BYTE is full");

					/*
					 * ret = imObject.InsertBand( imObject,(bandAr[band]-1), band, enhanced); if(!ret){
					 * System.out.println("ERROR: could not insert selected band="+bandAr[band]); return null; }
					 */
					// use percentile
					if (upperP[band] > lowerP[band]) {
						coef = (_lim.MAXPOS_BYTE / (double) (upperP[band] - lowerP[band] + 1));
					} else {
						coef = _lim.MAXPOS_BYTE;//>>1;
					}
					//test
					System.out.println("Info: band =" + bandAr[band] + ", BYTE coef=" + coef);
					for (idx = bandAr[band] - 1, idxIn = band; idx < imObject.getSize(); idx += imObject.getNumBands(), idxIn += enhanced.getNumBands()) {
						if (imObject.getByte(idx) >= 0) {
							val = (imObject.getByte(idx) - lowerP[band]) * coef;
							//enhanced.image[idxIn] = (byte) ( (float)(imObject.image[idx] - min[band])*coef );
						} else {
							val = (imObject.getByte(idx) + _lim.MAXPOS_BYTE - lowerP[band]) * coef;
							//enhanced.image[idxIn] = (byte) ( (float)(imObject.image[idx]+_lim.MAXPOS_BYTE - min[band])*coef );

						}
						if (val <= 0.0) {
							enhanced.setByte(idxIn, _lim.MIN_BYTE);
						} else {
							if (val >= _lim.MAXPOS_BYTE) {
								enhanced.setByte(idxIn, _lim.MAX_BYTE);
							} else {
								if (val < _lim.MAX_BYTE) {
									enhanced.setByte(idxIn, (byte) val);
								} else {
									enhanced.setByte(idxIn, (byte) (val - _lim.MAXPOS_BYTE));
								}
							}
						}
					}// end of for loop
					// end of using percentile

					continue;

				}
				coef = (_lim.MAXPOS_BYTE / (double) (max[band] - min[band] + 1));
				//test
				System.out.println("Info: band =" + bandAr[band] + ", BYTE coef=" + coef);
				for (idx = bandAr[band] - 1, idxIn = band; idx < imObject.getSize(); idx += imObject.getNumBands(), idxIn += enhanced.getNumBands()) {
					if (imObject.getByte(idx) >= 0) {
						enhanced.setByte(idxIn, (byte) ((imObject.getByte(idx) - min[band]) * coef));
					} else {
						enhanced.setByte(idxIn, (byte) ((imObject.getByte(idx) + _lim.MAXPOS_BYTE - min[band]) * coef));
					}
				}
			}// end of BYTE
			/////////////////////////////////////////////////////////////////////////////////////////
			if (imObject.getTypeString().equalsIgnoreCase("SHORT")) {

				//if(max[band] - min[band] == _lim.MAXPOS_SHORT-1){
				if (max[band] - min[band] > 1) {//(_lim.MAXPOS_BYTE-1)*0.9 ){
					System.out.println("Info: dynamic range of SHORT fits range of BYTE");
					//ret = imObject.InsertBand( imObject,(bandAr[band]-1), band, enhanced);
					//if(!ret){
					//  System.out.println("ERROR: could not insert selected band="+bandAr[band]);
					//  return null;
					//}
					// use percentile
					if (upperP[band] > lowerP[band]) {
						coef = (_lim.MAXPOS_BYTE / (double) (upperP[band] - lowerP[band] + 1));
					} else {
						coef = _lim.MAXPOS_BYTE;//>>1;
					}
					//test
					System.out.println("Info: band =" + bandAr[band] + ", SHORT coef=" + coef + ",myh.GetWideBins()=" + myh.GetWideBins());
					temp = lowerP[band] * myh.GetWideBins();

					for (idx = bandAr[band] - 1, idxIn = band; idx < imObject.getSize(); idx += imObject.getNumBands(), idxIn += enhanced.getNumBands()) {
						if (imObject.getShort(idx) >= 0) {
							val = (imObject.getShort(idx) - temp) * coef;
						} else {
							val = (imObject.getShort(idx) + _lim.MAXPOS_SHORT - temp) * coef;
						}
						if (val <= 0.0) {
							enhanced.setShort(idxIn, _lim.MIN_BYTE);
						} else {
							if (val >= _lim.MAXPOS_BYTE) {
								enhanced.setShort(idxIn, _lim.MAX_BYTE);
							} else {
								if (val < _lim.MAX_BYTE) {
									enhanced.setShort(idxIn, (short) val);
								} else {
									enhanced.setShort(idxIn, (short) (val - _lim.MAXPOS_SHORT));
								}
							}
						}

						/*
						 * if(val < _lim.MIN_BYTE){ enhanced.imageShort[idxIn] = _lim.MIN_BYTE; }else{ if(val >
						 * _lim.MAX_BYTE){ enhanced.imageShort[idxIn] = _lim.MAX_BYTE; }else{ enhanced.imageShort[idxIn] =
						 * (short)val; } }
						 */
					}// end of for loop
					// end of using percentile

					continue;
				}
				//test
				//System.out.println("Info: passed 2. test");

				//coef = (double) (_lim.MAXPOS_SHORT/(double)(max[band] - min[band] + 1 ));
				coef = (_lim.MAXPOS_BYTE / (double) (max[band] - min[band] + 1));
				//test
				System.out.println("Info: band =" + bandAr[band] + ", SHORT coef=" + coef + ",myh.GetWideBins()=" + myh.GetWideBins());
				temp = min[band] * myh.GetWideBins();
				for (idx = bandAr[band] - 1, idxIn = band; idx < imObject.getSize(); idx += imObject.getNumBands(), idxIn += enhanced.getNumBands()) {
					//if(enhanced.imageShort == null || imObject.imageShort == null){
					//  System.out.println("ERROR: band ="+ band+",null pointers "+imObject.imageShort+","+enhanced.imageShort);
					//  continue;
					//}
					//if(idx<0 || idx >= imObject.size ){
					//  System.out.println("ERROR: band ="+ bandAr[band]+",idx="+ idx);
					//  continue;
					//}
					//test
					//System.out.print("test "+idx+"," +" imObject.imageShort[idx]="+imObject.imageShort[idx]);

					if (imObject.getShort(idx) >= 0) {
						enhanced.setShort(idxIn, (short) ((float) (imObject.getShort(idx) - temp) * coef));
					} else {
						enhanced.setShort(idxIn, (short) ((float) (imObject.getShort(idx) + _lim.MAXPOS_SHORT - temp) * coef));
					}

				}
				//test
				System.out.println("Info: band =" + bandAr[band] + ", orig=" + imObject.getShort(0) + ",enhanced.imageShort[0]=" + enhanced.getShort(0));

			}// end of SHORT
			/////////////////////////////////////////////////////////////////////////////////////////
			if (imObject.getTypeString().equalsIgnoreCase("INT")) {
				if (max[band] - min[band] > 1) {
					System.out.println("Info: dynamic range of INT fits range of BYTE");
					// use percentile
					if (upperP[band] > lowerP[band]) {
						coef = (_lim.MAXPOS_BYTE / (double) (upperP[band] - lowerP[band] + 1));
					} else {
						coef = _lim.MAXPOS_BYTE;//>>1;
					}
					//test
					System.out.println("Info: band =" + bandAr[band] + ", INT coef=" + coef + ",myh.GetWideBins()=" + myh.GetWideBins());
					System.out.println("Info: lowerP =" + lowerP[band] + ", upperP=" + upperP[band]);
					//imObject.PrintImageObject();
					//enhanced.PrintImageObject();

					temp = lowerP[band] * myh.GetWideBins();

					for (idx = bandAr[band] - 1, idxIn = band; idx < imObject.getSize() && idxIn < enhanced.getSize(); idx += imObject.getNumBands(), idxIn += enhanced.getNumBands()) {
						if (imObject.getInt(idx) >= 0) {
							val = (imObject.getInt(idx) - temp) * coef;
						} else {
							val = (imObject.getInt(idx) + _lim.MAXPOS_INT - temp) * coef;
						}
						if (val <= 0.0) {
							enhanced.setInt(idxIn, _lim.MIN_BYTE);
						} else {
							if (val >= _lim.MAXPOS_BYTE) {
								enhanced.setInt(idxIn, _lim.MAX_BYTE);
							} else {
								if (val < _lim.MAX_BYTE) {
									enhanced.setInt(idxIn, (int) val);
								} else {
									enhanced.setInt(idxIn, (int) (val - _lim.MAXPOS_INT));
								}
							}
						}

					}// end of for loop
					//test
					System.out.println("Info: passed 1.5 test");

					// end of using percentile
					//continue;
				} else {
					//test
					System.out.println("Info: passed 2. test");
					coef = (_lim.MAXPOS_BYTE / (double) (max[band] - min[band] + 1));
					//test
					System.out.println("Info: band =" + bandAr[band] + ", INT coef=" + coef + ",myh.GetWideBins()=" + myh.GetWideBins());
					temp = min[band] * myh.GetWideBins();
					for (idx = bandAr[band] - 1, idxIn = band; idx < imObject.getSize(); idx += imObject.getNumBands(), idxIn += enhanced.getNumBands()) {
						if (imObject.getInt(idx) >= 0) {
							enhanced.setInt(idxIn, (int) ((float) (imObject.getInt(idx) - temp) * coef));
						} else {
							enhanced.setInt(idxIn, (int) ((float) (imObject.getInt(idx) + _lim.MAXPOS_INT - temp) * coef));
						}

					}
				}// end of else statement
				//test
				System.out.println("Info: band =" + bandAr[band] + ", orig=" + imObject.getInt(0) + ",enhanced.imageInt[0]=" + enhanced.getInt(0));

			}// end of INT

			//test
			System.out.println("Test: passed end");

		}

		//test
		System.out.println("Test: passed end 1");

		// free memory
		min = null;
		max = null;
		upperP = null;
		lowerP = null;

		//test
		System.out.println("Test: passed end 2");
		return enhanced;

	}// end of HistEqualOut

}//end of class ImEnhance

