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

/*
 * ConnectAnal.java
 *
 */

import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.im2learn.core.datatype.LimitValues;

/**
 * <B>The class ConnectAnal performs connectivity analysis of a thresholded or
 * segmented or classified image. </B>
 * 
 * <BR>
 * <BR>
 * <B>Description: </B> <B>Release notes: </B> <BR>
 * 
 * @author Peter Bajcsy & Young-Jin Lee
 * @version 1.0
 *  
 */

public class ConnectAnal {
	private ImageObject _pImLabels = null; // label index

	private int[] _pAreaS; //  size of label

	private byte[] _pMeanBinValS; // value of a binary segment

	private float[] _pMeanValS; // sample mean of label

	private float[] _pStDevValS; // sample stdev of label

	private float[] _pMinS; // min value of label

	private float[] _pMaxS; // max value of label

	private int _NFoundS;

	private int _NumCols, _NumRows;

	private float _invalid;	

	//constructors
	public ConnectAnal() {
		reset();
	}

	private void reset() {
		_pImLabels = null;// labeled index
		_pAreaS = null; // size of labeled segment
		_pMeanBinValS = null; // value of a binary labeled segment
		_pMeanValS = null; // sample mean of labeled segment
		_pStDevValS = null; // sample stdev of labeled segment
		_pMinS = null; // min value of labeled segment
		_pMaxS = null; // max value of labeled segment
		_NFoundS = 0; // number of segments
		_NumCols = _NumRows = 0;
		_invalid = -80.0F;
	}

	///////////////////////////////////
	//setters and getters

	public int getNFoundS() {
		return _NFoundS;
	}
	
	/**
	 * 
	 * @return
	 * @deprecated Use getNFoundS()
	 */
	public int GetNFoundS() {
		return _NFoundS;
	}

	public int getNumRows() {
		return _NumRows;
	}
	
	/**
	 * 
	 * @return
	 * @deprecated Use getNumRows()
	 */
	public int GetNumRows() {
		return _NumRows;
	}

	public int getNumCols() {
		return _NumCols;
	}
	
	/**
	 * 
	 * @return
	 * @deprecated Use getNumCols()
	 */
	public int GetNumCols() {
		return _NumCols;
	}

	public byte[] getMeanBinValS() {
		return _pMeanBinValS;
	}
	
	/**
	 * 
	 * @return
	 * @deprecated Use getMeanBinValS()
	 */
	public byte[] GetMeanBinValS() {
		return _pMeanBinValS;
	}

	public float[] getMeanValS() {
		return _pMeanValS;
	}
	
	/**
	 * 
	 * @return
	 * @deprecated Use getMeanValS()
	 */
	public float[] GetMeanValS() {
		return _pMeanValS;
	}

	public float[] getStDevValS() {
		return _pStDevValS;
	}
	
	/**
	 * 
	 * @return
	 * @deprecated Use getStDevValS()
	 */
	public float[] GetStDevValS() {
		return _pStDevValS;
	}

	public float[] getMinS() {
		return _pMinS;
	}
	
	/**
	 * 
	 * @return
	 * @deprecated Use getMinS()
	 */
	public float[] GetMinS() {
		return _pMinS;
	}

	public float[] getMaxS() {
		return _pMaxS;
	}
	
	/**
	 * 
	 * @return
	 * @deprecated Use getMaxS()
	 */
	public float[] GetMaxS() {
		return _pMaxS;
	}

	public ImageObject getImLabels() {
		return _pImLabels;
	}
	
	/**
	 * 
	 * @return
	 * @deprecated Use getImLabels()
	 */
	public ImageObject GetImLabels() {
		return _pImLabels;
	}

	public int[] getAreaS() {
		return _pAreaS;
	}
	
	/**
	 * 
	 * @return
	 * @deprecated Use getAreaS()
	 */
	public int[] GetAreaS() {
		return _pAreaS;
	}

	public float getInvalid() {
		return _invalid;
	}
	
	/**
	 * 
	 * @return
	 * @deprecated Use getInvalid()
	 */
	public float GetInvalid() {
		return _invalid;
	}

	public void setInvalid(float val) {
		_invalid = val;
	}
	
	/**
	 * 
	 * @param val
	 * @deprecated Use setInvalid()
	 */
	public void SetInvalid(float val) {
		_invalid = val;
	}

	/**
	 * connectivity analysis on a "binary" image (two adjacent segments 
	 * have different labels) computes labels and size of labeled segments
	 * @param pimbinary
	 * @return
	 */
	public boolean binary_CA(ImageObject pimbinary) {
		//sanity check
		if (pimbinary == null) {
			System.out.println("ERROR: the input image does not exist ");
			
			return false;
		}
		
		if (pimbinary.getType() != ImageObject.TYPE_BYTE
				&& pimbinary.getType() != ImageObject.TYPE_SHORT
				&& pimbinary.getType()!= ImageObject.TYPE_INT) {
			System.out.println("ERROR: the input image can be only " +
					"of type BYTE, SHORT, or INT");
			
			return false;
		}
		
		int i, j;
		int index, index1;
		int peto, pom, pom1;
		int maxpetox, count;//,top;

		int[] temp1 = null;
		int[] labeling = null;

		int numrows, numcols;
		numrows = pimbinary.getNumRows();
		numcols = pimbinary.getNumCols();

		//allocate memory for outputs
		long size = pimbinary.getSize();
		
		if (_pImLabels != null) {
			_pImLabels = null;
		}
		
		_pImLabels = null;
		try {
			_pImLabels = ImageObject.createImage(numrows, numcols, 1,
					ImageObject.TYPE_INT);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		if (_pAreaS != null) {
			_pAreaS = null;
		}
		
		_pAreaS = new int[(int) size + 2]; // pixel size of segments
		_NumRows = numrows;
		_NumCols = numcols;
		
		for (peto = 0; peto < size + 2; peto++)
			_pAreaS[peto] = 0;

		// region labeling
		index = 0;
		index1 = 1;
		peto = 1;
		
		for (i = 0; i < numrows; i++) {
			for (j = 0; j < numcols - 1; j++) {
				_pImLabels.setInt(index, peto);
				_pAreaS[peto] += 1;
				
				if (pimbinary.getInt(index) != pimbinary.getInt(index1))
					peto++;

				index++;
				index1++;
			}
			
			_pImLabels.setInt(index, peto);
			_pAreaS[peto] += 1;
			peto++;
			index++;
			index1++;
		}
		
		//TODO
//		if (pimbinary.sampType.equalsIgnoreCase("BYTE")) {
//			for (i = 0; i < numrows; i++) {
//				for (j = 0; j < numcols - 1; j++) {
//					_pImLabels.imageInt[index] = peto;
//					_pAreaS[peto] += 1;
//					if (pimbinary.image[index] != pimbinary.image[index1])
//						peto++;
//
//					index++;
//					index1++;
//				}
//				_pImLabels.imageInt[index] = peto;
//				_pAreaS[peto] += 1;
//				peto++;
//				index++;
//				index1++;
//			}
//		}
//		if (pimbinary.sampType.equalsIgnoreCase("SHORT")) {
//			for (i = 0; i < numrows; i++) {
//				for (j = 0; j < numcols - 1; j++) {
//					_pImLabels.imageInt[index] = peto;
//					_pAreaS[peto] += 1;
//					if (pimbinary.imageShort[index] != pimbinary.imageShort[index1])
//						peto++;
//
//					index++;
//					index1++;
//				}
//				_pImLabels.imageInt[index] = peto;
//				_pAreaS[peto] += 1;
//				peto++;
//				index++;
//				index1++;
//			}
//		}
//		if (pimbinary.sampType.equalsIgnoreCase("INT")) {
//			for (i = 0; i < numrows; i++) {
//				for (j = 0; j < numcols - 1; j++) {
//					_pImLabels.imageInt[index] = peto;
//					_pAreaS[peto] += 1;
//					if (pimbinary.imageInt[index] != pimbinary.imageInt[index1])
//						peto++;
//
//					index++;
//					index1++;
//				}
//				_pImLabels.imageInt[index] = peto;
//				_pAreaS[peto] += 1;
//				peto++;
//				index++;
//				index1++;
//			}
//		}
		
		maxpetox = peto;

		temp1 = new int[maxpetox];
		labeling = new int[maxpetox];

		for (pom = 1; pom < maxpetox; pom++) {
			temp1[pom] = pom;
		}
		
		// expanding regions along columns
		index = 0;
		index1 = numcols;
		
		for (i = 0; i < numrows - 1; i++) {
			for (j = 0; j < numcols; j++) {
				if (pimbinary.getInt(index) == pimbinary.getInt(index1)) {
					// merge two adjacent pixels (i,j) and (i+1,j)
					pom = temp1[_pImLabels.getInt(index1)];
					
					while (temp1[pom] != pom)
						pom = temp1[pom];

					pom1 = temp1[_pImLabels.getInt(index)];
					
					while (temp1[pom1] != pom1)
						pom1 = temp1[pom1];

					if (pom1 > pom)
						temp1[pom1] = pom;
					else {
						if (pom1 < pom)
							temp1[pom] = pom1;
					}
				}
				
				index++;
				index1++;
			}
		}
		
		//TODO
//		if (pimbinary.sampType.equalsIgnoreCase("BYTE")) {
//			for (i = 0; i < numrows - 1; i++) {
//				for (j = 0; j < numcols; j++) {
//					if (pimbinary.image[index] == pimbinary.image[index1]) {
//						// merge two adjacent pixels (i,j) and (i+1,j)
//						pom = temp1[_pImLabels.imageInt[index1]];
//						while (temp1[pom] != pom)
//							pom = temp1[pom];
//
//						pom1 = temp1[_pImLabels.imageInt[index]];
//						while (temp1[pom1] != pom1)
//							pom1 = temp1[pom1];
//
//						if (pom1 > pom)
//							temp1[pom1] = pom;
//						else {
//							if (pom1 < pom)
//								temp1[pom] = pom1;
//						}
//					}
//					index++;
//					index1++;
//				}
//			}
//		}
//		if (pimbinary.sampType.equalsIgnoreCase("SHORT")) {
//			for (i = 0; i < numrows - 1; i++) {
//				for (j = 0; j < numcols; j++) {
//					if (pimbinary.imageShort[index] == pimbinary.imageShort[index1]) {
//						// merge two adjacent pixels (i,j) and (i+1,j)
//						pom = temp1[_pImLabels.imageInt[index1]];
//						while (temp1[pom] != pom)
//							pom = temp1[pom];
//
//						pom1 = temp1[_pImLabels.imageInt[index]];
//						while (temp1[pom1] != pom1)
//							pom1 = temp1[pom1];
//
//						if (pom1 > pom)
//							temp1[pom1] = pom;
//						else {
//							if (pom1 < pom)
//								temp1[pom] = pom1;
//						}
//					}
//					index++;
//					index1++;
//				}
//			}
//		}
//		if (pimbinary.sampType.equalsIgnoreCase("INT")) {
//			for (i = 0; i < numrows - 1; i++) {
//				for (j = 0; j < numcols; j++) {
//					if (pimbinary.imageInt[index] == pimbinary.imageInt[index1]) {
//						// merge two adjacent pixels (i,j) and (i+1,j)
//						pom = temp1[_pImLabels.imageInt[index1]];
//						while (temp1[pom] != pom)
//							pom = temp1[pom];
//
//						pom1 = temp1[_pImLabels.imageInt[index]];
//						while (temp1[pom1] != pom1)
//							pom1 = temp1[pom1];
//
//						if (pom1 > pom)
//							temp1[pom1] = pom;
//						else {
//							if (pom1 < pom)
//								temp1[pom] = pom1;
//						}
//					}
//					index++;
//					index1++;
//				}
//			}
//		}

		// minimum label for the segments plus compute the size
		for (peto = 1; peto < maxpetox; peto++) {
			if (temp1[peto] != peto) {
				pom = temp1[peto];
				
				while (temp1[pom] != pom)
					pom = temp1[pom];

				_pAreaS[pom] += _pAreaS[peto];
				_pAreaS[peto] = 0;
				temp1[peto] = pom;
			}
		}

		// relabel segments from 1 to max and update the size
		count = 0;
		for (peto = 1; peto < maxpetox; peto++) {
			if (_pAreaS[peto] != 0) {
				count += 1;
				labeling[peto] = count;
				_pAreaS[count] = _pAreaS[peto];
			}
		}

		// assign new label to each pixel
		for (index = 0; index < size; index++) {
			peto = _pImLabels.getInt(index);
			peto = labeling[temp1[peto]];
			_pImLabels.setInt(index, peto);
		}
		
		maxpetox = count + 1;
		_NFoundS = count;

		labeling = null;

		//truncate the file size by copying and reallocating an array
		for (index = 0; index < _NFoundS + 1; index++)
			temp1[index] = _pAreaS[index];

		_pAreaS = null;
		_pAreaS = new int[_NFoundS + 1]; // pixel size of segments
		
		for (index = 0; index < _NFoundS + 1; index++)
			_pAreaS[index] = temp1[index];

		temp1 = null;
		
		return true;
	}
	
	/**
	 * connectivity analysis on any number of bands image (two adjacent segments 
	 * have different labels) computes labels and size of labeled segments
	 * @param pimbinary
	 * @return
	 */
	public boolean bandVector_CA(ImageObject pimVector) {
		//sanity check
		if (pimVector == null) {
			System.out.println("ERROR: the input image does not exist ");
			
			return false;
		}
		
		/*if (pimbinary.getType() != ImageObject.TYPE_BYTE
				&& pimbinary.getType() != ImageObject.TYPE_SHORT
				&& pimbinary.getType()!= ImageObject.TYPE_INT) {
			System.out.println("ERROR: the input image can be only " +
					"of type BYTE, SHORT, or INT");
			
			return false;
		}*/
		
		int i, j, k;
		int index, index1;
		int peto, pom, pom1;
		int maxpetox, count;//,top;

		int[] temp1 = null;
		int[] labeling = null;
        boolean signal = true;
        
		int numrows, numcols, numbands;
		numrows = pimVector.getNumRows();
		numcols = pimVector.getNumCols();
		numbands = pimVector.getNumBands();

		//allocate memory for outputs
		long size = numrows * numcols;//pimVector.getSize();
		
		if (_pImLabels != null) {
			_pImLabels = null;
		}
		
		_pImLabels = null;
		try {
			_pImLabels = ImageObject.createImage(numrows, numcols, 1,
					ImageObject.TYPE_INT);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		if (_pAreaS != null) {
			_pAreaS = null;
		}
		
		_pAreaS = new int[(int) size + 2]; // pixel size of segments
		_NumRows = numrows;
		_NumCols = numcols;
		
		for (peto = 0; peto < size + 2; peto++)
			_pAreaS[peto] = 0;

		// region labeling
		index = 0;
		index1 = 0;
		peto = 1;
		
		for (i = 0; i < numrows; i++) {
			for (j = 0; j < numcols - 1; j++) {
				_pImLabels.setInt(index1, peto);
				_pAreaS[peto] += 1;
				signal = true;
				for (k = 0; k < numbands && signal ; k++) {			
				   if (Math.abs(pimVector.getDouble(index+k) - pimVector.getDouble(index+numbands+k)) > LimitValues.EPSILON)
                     signal = false;
				}
				if(!signal){
					peto++;
				}
			    index+= numbands;
			    index1++;
			}
			
			_pImLabels.setInt(index1, peto);
			_pAreaS[peto] += 1;
			peto++;
			index+= numbands;
			index1++;
		}
		
		
		maxpetox = peto;

		temp1 = new int[maxpetox];
		labeling = new int[maxpetox];

		for (pom = 1; pom < maxpetox; pom++) {
			temp1[pom] = pom;
		}
		
		// expanding regions along columns
		int offset = numcols*numbands;
		index = 0;
		index1 = 0; 
		
		for (i = 0; i < numrows - 1; i++) {
			for (j = 0; j < numcols; j++) {
				signal = true;
				for (k = 0; k < numbands && signal; k++) {				
					if (Math.abs(pimVector.getDouble(index+k) - pimVector.getDouble(index+offset+k)) > LimitValues.EPSILON) {
                      signal = false;
					}
				}
				if(signal){
					// merge two adjacent pixels (i,j) and (i+1,j)

					pom = temp1[_pImLabels.getInt(index1+numcols)];
					
					while (temp1[pom] != pom)
						pom = temp1[pom];
					
					pom1 = temp1[_pImLabels.getInt(index1)];
					
					while (temp1[pom1] != pom1)
						pom1 = temp1[pom1];
					
					if (pom1 > pom)
						temp1[pom1] = pom;
					else {
						if (pom1 < pom)
							temp1[pom] = pom1;
					}
				}
				
				index += numbands;
				index1++;
			}
		}
		
		
		// minimum label for the segments plus compute the size
		for (peto = 1; peto < maxpetox; peto++) {
			if (temp1[peto] != peto) {
				pom = temp1[peto];
				
				while (temp1[pom] != pom)
					pom = temp1[pom];

				_pAreaS[pom] += _pAreaS[peto];
				_pAreaS[peto] = 0;
				temp1[peto] = pom;
			}
		}

		// relabel segments from 1 to max and update the size
		count = 0;
		for (peto = 1; peto < maxpetox; peto++) {
			if (_pAreaS[peto] != 0) {
				count += 1;
				labeling[peto] = count;
				_pAreaS[count] = _pAreaS[peto];
			}
		}

		// assign new label to each pixel
		for (index = 0; index < size; index++) {
			peto = _pImLabels.getInt(index);
			peto = labeling[temp1[peto]];
			_pImLabels.setInt(index, peto);
		}
		
		maxpetox = count + 1;
		_NFoundS = count;

		labeling = null;

		//truncate the file size by copying and reallocating an array
		for (index = 0; index < _NFoundS + 1; index++)
			temp1[index] = _pAreaS[index];

		_pAreaS = null;
		_pAreaS = new int[_NFoundS + 1]; // pixel size of segments
		
		for (index = 0; index < _NFoundS + 1; index++)
			_pAreaS[index] = temp1[index];

		temp1 = null;
		
		return true;
	}

	/**
	 * 
	 * @param pimbinary
	 * @return
	 * @deprecated Use binary_CA()
	 */
	public boolean Binary_CA(ImageObject pimbinary) {
		return binary_CA(pimbinary);
	}

	/**
	 * since Binary_CA computes only labels and size for a binary image
	 * this method computes the binary values for each segment
	 * and stores the values in pMeanBinVal
	 * @param pimbinary
	 * @return
	 */
	public boolean binaryBinVal_CA(ImageObject pimbinary) {
		if (pimbinary == null) {
			System.out.println("Error: no image ");
			
			return false;
		}

		if (!binary_CA(pimbinary))
			return false;

		if (_pMeanBinValS != null) {
			_pMeanBinValS = null;
		}
		
		// pixel size of segments
		_pMeanBinValS = new byte[_NFoundS + 1];

		int index, peto;
		for (index = 0; index < pimbinary.getSize(); index++) {
			peto = _pImLabels.getInt(index);
			_pMeanBinValS[peto] = pimbinary.getByte(index);
		}
		
		return true;
	}
	/**
	 * since Binary_CA computes only labels and size for a binary image
	 * this method computes the binary values for each segment
	 * and stores the values in pMeanBinVal
	 * @param pimbinary
	 * @return
	 */
	public boolean bandVectorVal_CA(ImageObject pimVector) {
		if (pimVector == null) {
			System.out.println("Error: no image ");			
			return false;
		}
		if( !pimVector.getTypeString().equalsIgnoreCase("BYTE") ){
			System.out.println("Error: no image ");			
			return false;			
		}

		if (!bandVector_CA(pimVector))
			return false;

		if (_pMeanBinValS != null) {
			_pMeanBinValS = null;
		}
		
		// pixel size of segments
		_pMeanBinValS = new byte[(_NFoundS + 1)*pimVector.getNumBands()];

		int i, index, index1, peto;
		index1= 0;
		for (index = 0; index < _pImLabels.getSize(); index++) {
			peto = _pImLabels.getInt(index);
			index1 = peto * pimVector.getNumBands();  
			for(i=0;i<pimVector.getNumBands();i++){
			  _pMeanBinValS[index1+i] = pimVector.getByte(index+i);
			}
		}
		
		return true;
	}
	
	/**
	 * 
	 * @param pimbinary
	 * @return
	 * @deprecated Use binaryBinVal_CA()
	 */
	public boolean BinaryBinVal_CA(ImageObject pimbinary) {
		return binaryBinVal_CA(pimbinary);
	}

	/**
	 * connectivity analysis on a binary image (given by mask)
	 * then computes sample mean and stdev values of labeled segments
	 * @param image
	 * @param pimbinary
	 * @return
	 */
	public boolean binaryMeanStDev_CA(ImageObject image, ImageObject pimbinary) {
		//compute labels and size
		if (binary_CA(pimbinary)) {
			System.out.println(" Number of found segments = " + _NFoundS);
			
			return (meanStDev_CA(image));
		}
		
		return false;
	}
	
	/**
	 * 
	 * @param image
	 * @param pimbinary
	 * @return
	 * @deprecated Use binaryMeanStDev_CA()
	 */
	public boolean BinaryMeanStDev_CA(ImageObject image, ImageObject pimbinary) {
		return binaryMeanStDev_CA(image, pimbinary);
	}

	/**
	 * connectivity analysis on a binary image (given by mask)
	 * then computes sample mean and stdev values of labeled segments
	 * @param image
	 * @return
	 */
	public boolean meanStDev_CA(ImageObject image) {
		if (_pImLabels == null) {
			System.out.println("Error: no labels");
			
			return false;
		}
		
		if (image == null) {
			System.out.println("Error: no input image");
			
			return false;
		}
		
		if (image.getType() != ImageObject.TYPE_BYTE) {
			System.out.println("Error: other than BYTE input image is not supported");
			
			return false;
		}
		
		if (image.getNumBands() != 1) {
			System.out.println("Error: input image sampPerPixel != 1 ");
			
			return false;
		}
		
		if (image.getNumRows() != _pImLabels.getNumRows()
				|| image.getNumCols() != _pImLabels.getNumCols()) {
			System.out.println("Error: mismatch in label and image size ");
			
			return false;
		}

		int index, peto;
		long size = image.getSize();

		if (_pMeanValS != null) {
			_pMeanValS = null;
		}
		
		_pMeanValS = new float[_NFoundS + 1]; // sample mean of segments
		
		if (_pStDevValS != null) {
			_pStDevValS = null;
		}
		
		_pStDevValS = new float[_NFoundS + 1]; // sample mean of segments

		//init
		for (index = 1; index < _NFoundS + 1; index++) {
			_pMeanValS[index] = 0.0F;
			_pStDevValS[index] = 0.0F;
		}
		
		//sum image values
		float val;
		
		for (index = 0; index < size; index++) {
			peto = _pImLabels.getInt(index);
			val = image.getByte(index);
						
			_pMeanValS[peto] += val;
			_pStDevValS[peto] += val * val;
		}
		
		// compute sample mean and stdev
		for (index = 1; index < _NFoundS + 1; index++) {
			if (_pAreaS[index] > 0) {
				_pMeanValS[index] /= _pAreaS[index];
				_pStDevValS[index] = _pStDevValS[index] / _pAreaS[index]
						- _pMeanValS[index] * _pMeanValS[index];
				
				if (_pStDevValS[index] >= 0)
					_pStDevValS[index] = (float) Math.sqrt(_pStDevValS[index]);
				else {
					System.out.println("Error: stdev <0 ");
					_pStDevValS[index] = 0.0F;
				}
			} else {
				System.out.println("Error: Size of a labeled segment " + index
						+ " is equal to zero ");
			}
		}

		return true;
	}

	/**
	 * 
	 * @param image
	 * @return
	 * @deprecated Use meanStDev_CA()
	 */
	public boolean MeanStDev_CA(ImageObject image) {
		return meanStDev_CA(image);
	}
	
	/**
	 * connectivity analysis on a binary image (given by mask)
	 * then computes min and max values of labeled segments
	 * @param image
	 * @param pimbinary
	 * @return
	 */
	public boolean binaryMinMax_CA(ImageObject image, ImageObject pimbinary) {
		//compute labels and size
		if (binary_CA(pimbinary)) {
			System.out.println(" Number of found segments = " + _NFoundS);
			
			return (minMax_CA(image));
		}
		
		return false;
	}
	
	/**
	 * 
	 * @param image
	 * @param pimbinary
	 * @return
	 * @deprecated Use binaryMinMax_CA()
	 */
	public boolean BinaryMinMax_CA(ImageObject image, ImageObject pimbinary) {
		return binaryMinMax_CA(image, pimbinary);
	}

	//////////////////////////////////////////////////////////////
	// connectivity analysis on a binary image (given by mask)
	// then computes min and max values of labeled segments
	//////////////////////////////////////////////////////////////
	public boolean minMax_CA(ImageObject image) {
		if (_pImLabels == null) {
			System.out.println("Error: no labels");
			
			return false;
		}
		
		if (image == null) {
			System.out.println("Error: no input image");
			
			return false;
		}
		
		if (image.getType() != ImageObject.TYPE_BYTE) {
			System.out.println("Error: other than BYTE input image is not supported");
			
			return false;
		}
		
		if (image.getNumBands() != 1) {
			System.out.println("Error: input image sampPerPixel != 1 ");
			
			return false;
		}
		
		if (image.getNumRows() != _pImLabels.getNumRows()
				|| image.getNumCols() != _pImLabels.getNumCols()) {
			System.out.println("Error: mismatch in label and image size ");
			
			return false;
		}

		int index, peto;
		long size = image.getSize();
		
		if (_pMinS != null) {
			_pMinS = null;
		}
		
		_pMinS = new float[_NFoundS + 1]; // min value of segments
		
		if (_pMaxS != null) {
			_pMaxS = null;
		}
		_pMaxS = new float[_NFoundS + 1]; // max value of segments

		//init
		for (index = 1; index < _NFoundS + 1; index++) {
			_pMinS[index] = Float.MAX_VALUE;
			_pMaxS[index] = Float.MIN_VALUE;
		}
		
		//sum image values
		float val;
		for (index = 0; index < size; index++) {
			peto = _pImLabels.getInt(index);
			val = image.getByte(index);
			
			if (val < _pMinS[peto])
				_pMinS[peto] = val;
			if (val > _pMaxS[peto])
				_pMaxS[peto] = val;			
		}
		
		return true;
	}
	
	/**
	 * 
	 * @param image
	 * @return
	 * @deprecated Use minMax_CA(0
	 */
	public boolean MinMax_CA(ImageObject image) {
		return minMax_CA(image);
	}

	/////////////////////////////////////////
	//display values
	public void printConnectAnal() {
		System.out.println("ConnectAnal: Variables");
		
		if (_pImLabels != null) {// labeled index
			System.out.println("Label Image:");
			_pImLabels.toString();
		}
		
		System.out.println("NFoundS=" + _NFoundS);
		
		if (_pAreaS != null) { // size of labeled segment
			System.out.println("Area[1]=" + _pAreaS[1]);
		}
		
		if (_pMeanBinValS != null) { // value of a binary labeled segment
			System.out.println("MeanBinValS[1]=" + _pMeanBinValS[1]);
		}
		
		if (_pMeanValS != null) { // sample mean of labeled segment
			System.out.println("MeanValS[1]=" + _pMeanValS[1]);
		}
		
		if (_pStDevValS != null) { // sample stdev of labeled segment
			System.out.println("StDevValS[1]=" + _pStDevValS[1]);
		}
		
		if (_pMinS != null) { // min value of labeled segment
			System.out.println("MinS[1]=" + _pMinS[1]);
		}
		
		if (_pMaxS != null) { // max value of labeled segment
			System.out.println("MaxS[1]=" + _pMaxS[1]);
		}
		
		System.out.println("_NumCols =" + _NumCols + ", _NumRows =" + _NumRows
				+ ", Invalid = " + _invalid);
	}

	/**
	 * @deprecated Use printConnectAnal()
	 *
	 */
	public void PrintConnectAnal() {
		printConnectAnal();
	}
	
	public void printConnectAnalAreaS() {
		System.out.println("ConnectAnal: AreaS");
		
		if (_pAreaS == null)
			return;
		
		for (int idx = 1; idx < _NFoundS + 1; idx++) {
			System.out.println("AreaS[" + idx + "]=" + _pAreaS[idx]);
		}
	}
	
	/**
	 * @deprecated Use printConnectAnalAreaS()
	 *
	 */
	public void PrintConnectAnalAreaS() {
		printConnectAnalAreaS();
	}

	public void printConnectAnalMeanBinValS() {
		System.out.println("ConnectAnal: MeanBinValS");
		
		if (_pMeanBinValS == null)
			return;
		
		for (int idx = 1; idx < _NFoundS + 1; idx++) {
			System.out.println("MeanBinValS[" + idx + "]=" + _pMeanBinValS[idx]);
		}
	}
	
	/**
	 * @deprecated Use printConnectAnalMeanBinValS()
	 *
	 */
	public void PrintConnectAnalMeanBinValS() {
		printConnectAnalMeanBinValS();
	}

	public void printConnectAnalMeanValS() {
		System.out.println("ConnectAnal: MeanValS"); 
		
		if (_pMeanValS == null)
			return;
		
		for (int idx = 1; idx < _NFoundS + 1; idx++) {
			System.out.println("MeanValS[" + idx + "]=" + _pMeanValS[idx]);
		}
	}

	/**
	 * @deprecated Use printConnectAnalMeanValS()
	 *
	 */
	public void PrintConnectAnalMeanValS() {
		printConnectAnalMeanValS();
	}
}
