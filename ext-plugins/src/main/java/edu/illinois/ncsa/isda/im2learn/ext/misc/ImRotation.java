package edu.illinois.ncsa.isda.im2learn.ext.misc;

/*
 * ImRotation.java
 *
 */

import edu.illinois.ncsa.isda.im2learn.core.datatype.ImPoint;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageException;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.im2learn.core.datatype.LimitValues;
import edu.illinois.ncsa.isda.im2learn.core.datatype.SubArea;
import edu.illinois.ncsa.isda.im2learn.ext.math.GeomOper;

/**
 * <B> The class ImRotation provides a tool for rotating images. </B> <BR>
 * <BR>
 * <B>Description:</B>
 * <p>
 * </p>
 * 
 * @author Peter Bajcsy
 * @version 1.0
 * 
 */

// //////////////////////////////////////////////////////////////////
// this class performs image rotation
// //////////////////////////////////////////////////////////////////
public class ImRotation {
	private boolean	_debugImRotation;

	// internal temporary values for storing the rotated image size
	private int		_numrowsOut, _numcolsOut, _minRow, _minCol;
	private boolean	_returnOrigImSize	= false;

	LimitValues		_lim				= new LimitValues();
	GeomOper		_geomOp				= new GeomOper();

	protected int	_color				= 0;

	// constructor
	public ImRotation() {
		_debugImRotation = true;
		_minRow = -1;
		_minCol = -1;
		_numrowsOut = -1;
		_numcolsOut = -1;
		_returnOrigImSize = false;
	}

	// ////////////////////////////////////////////////////////
	// Getters
	public boolean getDebugFlag() {
		return _debugImRotation;
	}

	public boolean getReturnOrigImSize() {
		return _returnOrigImSize;
	}

	public int getMinRow() {
		return _minRow;
	}

	public int getMinCol() {
		return _minCol;
	}

	public int getNumRowsOut() {
		return _numrowsOut;
	}

	public int getNumColsOut() {
		return _numcolsOut;
	}

	// /////////////////////////////////////////////////////////////////////
	// Setters
	public void setDebugFlag(boolean flag) {
		_debugImRotation = flag;
	}

	public void setReturnOrigImSize(boolean flag) {
		_returnOrigImSize = flag;
	}

	/*
	 * ///////////////////////////////////////////////////////////////////// //
	 * rotate by angle degrees but do not change the image size
	 * /////////////////////////////////////////////////////////////////////
	 * public ImageObject RotateImageOrigSize(ImageObject image,float angle) {
	 * int numrows,numcols; if(image==null || (numrows=image.getNumRows()) <=0 ||
	 * (numcols=image.getNumCols()) <=0){ System.out.println("Error: no image to
	 * rotate \n"); return null; }
	 * if(image.getTypeString().equalsIgnoreCase("BYTE")== false &&
	 * image.getTypeString().equalsIgnoreCase("SHORT")== false){
	 * System.out.println("Error: other than BYTE & SHORT types are not
	 * supported \n"); return null; }
	 * 
	 * ImageObject pStore = null; double alpha = angle*_lim.Deg2Rad; //PI/180;
	 * //FindRotatedDim(numrows,numcols,alpha,&numrowsOut,&numcolsOut,&minRow,&minCol);
	 * //if( !FindRotatedDim(numrows,numcols,alpha) ) // return null;
	 * _numrowsOut = _numcolsOut = 0; _minRow = _minCol = 0;
	 * 
	 * //pStore =
	 * ImageObject.createImage(_numrowsOut,_numcolsOut,image.getNumBands(),image.getTypeString());
	 * pStore =
	 * ImageObject.createImage(image.getNumRows(),image.getNumCols(),image.getNumBands(),image.getTypeString());
	 * if(pStore == null){ System.out.println( "Error: failed to allocate memory
	 * \n"); return null; } long size = pStore.getSize(); // padding int
	 * indexTo,index; pStore.SetImageObjectValue(0.0);
	 * 
	 * //GeomOper geomOp = new GeomOper(); ImPoint pts1 = new ImPoint(); ImPoint
	 * ptsc = new ImPoint(); ptsc.x =0.0; ptsc.y =0.0;
	 * 
	 * boolean signal = true; int j; int row1,col1; float row,col; int offset =
	 * image.getNumCols() * image.getNumBands(); index=0; boolean flipCol =
	 * false;//0;// based on 0.5 increment saves multiplications for index
	 * boolean flipRow = false;//0;// based on 0.5 increment saves
	 * multiplications for index
	 * if(image.getTypeString().equalsIgnoreCase("BYTE") ){ for(row=0;row<numrows;row+=0.5){
	 * for(col=0;col<numcols;col+=0.5){ pts1.x = (float)row; pts1.y =
	 * (float)col; _geomOp.RotatePoint(ptsc,pts1,alpha);
	 * 
	 * if(pts1.x>0) row1 = (int) (pts1.x+0.5); else row1 = (int) (pts1.x-0.5);
	 * if(pts1.y>0) col1 = (int) (pts1.y+0.5); else col1 = (int) (pts1.y-0.5);
	 * if(row1 >=0 && col1>=0 && row1< pStore.getNumRows() && col1 <
	 * pStore.getNumCols()){ indexTo = ( row1*pStore.getNumCols()+col1 )*
	 * pStore.getNumBands(); if(indexTo>=0 && indexTo < size){ for(j=0;j<pStore.getNumBands();j++)
	 * pStore.image[indexTo+j] = image.image[index+j]; } } if(flipCol){
	 * //index++;; index+= image.getNumBands(); flipCol=false; }else{
	 * flipCol=true; } } if(flipRow){ flipRow=false; }else{ //index -= numcols;
	 * index -= offset; flipRow=true; } } signal = false; }// end of BYTE
	 * if(image.getTypeString().equalsIgnoreCase("SHORT") ){ for(row=0;row<numrows;row+=0.5){
	 * for(col=0;col<numcols;col+=0.5){ pts1.x = (float)row; pts1.y =
	 * (float)col; _geomOp.RotatePoint(ptsc,pts1,alpha);
	 * 
	 * if(pts1.x>0) row1 = (int) (pts1.x+0.5); else row1 = (int) (pts1.x-0.5);
	 * if(pts1.y>0) col1 = (int) (pts1.y+0.5); else col1 = (int) (pts1.y-0.5);
	 * 
	 * if(row1 >=0 && col1>=0 && row1< pStore.getNumRows() && col1 <
	 * pStore.getNumCols()){ indexTo = ( row1*pStore.getNumCols()+col1 )*
	 * pStore.getNumBands(); if(indexTo>=0 && indexTo < size){ for(j=0;j<pStore.getNumBands();j++)
	 * pStore.imageShort[indexTo+j] = image.imageShort[index+j]; } }
	 * if(flipCol){ //index++;; index+= image.getNumBands(); flipCol=false;
	 * }else{ flipCol=true; } } if(flipRow){ flipRow=false; }else{ //index -=
	 * numcols; index -= offset; flipRow=true; } } signal = false; }// end of
	 * SHORT
	 * 
	 * if(signal){ System.out.println("Error: other than BYTE type is not
	 * supported \n"); return null; }
	 * 
	 * return(pStore); }// end of RotateImageOrigSize
	 */
	// ///////////////////////////////////////////////////////////////////
	// rotate by angle degrees and pad the empty space with zeros
	// ///////////////////////////////////////////////////////////////////
	public ImageObject RotateImage(ImageObject image, float angle) throws ImageException {
		int numrows, numcols;
		if ((image == null) || ((numrows = image.getNumRows()) <= 0) || ((numcols = image.getNumCols()) <= 0)) {
			System.out.println("Error: no image to rotate \n");
			return null;
		}
		/*
		 * if(image.getTypeString().equalsIgnoreCase("BYTE")== false &&
		 * image.getTypeString().equalsIgnoreCase("SHORT")== false){
		 * System.out.println("Error: other than BYTE & SHORT types are not
		 * supported \n"); return null; }
		 */
		ImageObject pStore = null;
		double alpha = angle * _lim.Deg2Rad; // PI/180;

		if (_returnOrigImSize) {
			pStore = ImageObject.createImage(image.getNumRows(), image.getNumCols(), image.getNumBands(), image.getType());
			_minRow = 0;
			_minCol = 0;
			_numrowsOut = image.getNumRows();
			_numcolsOut = image.getNumCols();
		} else {
			// FindRotatedDim(numrows,numcols,alpha,&numrowsOut,&numcolsOut,&minRow,&minCol);
			if (!FindRotatedDim(numrows, numcols, alpha)) {
				return null;
			}
			pStore = ImageObject.createImage(_numrowsOut, _numcolsOut, image.getNumBands(), image.getType());
		}
		if (pStore == null) {
			System.out.println("Error: failed to allocate memory \n");
			return null;
		}
		long size = pStore.getSize();// *numrowsOut * (*numcolsOut);

		// padding
		int indexTo, index;
		// ImageObject zero = ImageObject.createImage(1,1,image.getNumBands(),
		// image.getTypeString());
		// zero.SetImageObjectValue(0.0);
		// pStore.SetImageObjectValue(zero);
		pStore.setData(0.0);

		/*
		 * if(pStore.getType().equalsIgnoreCase("BYTE") ){ for(index=0;index<size;index++)
		 * pStore.image[index] = 0; }
		 */

		// GeomOper geomOp = new GeomOper();
		ImPoint pts1 = new ImPoint();
		ImPoint ptsc = new ImPoint();
		ptsc.x = 0.0;
		ptsc.y = 0.0;

		boolean signal = true;
		int j;
		int row1, col1;
		float row, col;
		int offset = image.getNumCols() * image.getNumBands();
		index = 0;
		boolean flipCol = false;// 0;// based on 0.5 increment saves
		// multiplications for index
		boolean flipRow = false;// 0;// based on 0.5 increment saves
		// multiplications for index
		for (row = 0; row < numrows; row += 0.5) {
			for (col = 0; col < numcols; col += 0.5) {
				pts1.x = row;
				pts1.y = col;
				_geomOp.rotatePoint(ptsc, pts1, alpha);

				if (pts1.x > 0) {
					row1 = (int) (pts1.x + 0.5);
				} else {
					row1 = (int) (pts1.x - 0.5);
				}
				if (pts1.y > 0) {
					col1 = (int) (pts1.y + 0.5);
				} else {
					col1 = (int) (pts1.y - 0.5);
				}

				if (_returnOrigImSize) {
					if ((row1 >= 0) && (col1 >= 0) && (row1 < pStore.getNumRows()) && (col1 < pStore.getNumCols())) {
						indexTo = (row1 * pStore.getNumCols() + col1) * pStore.getNumBands();
					} else {
						indexTo = -1;
					}
				} else {
					indexTo = ((row1 - _minRow) * (pStore.getNumCols()) + (col1 - _minCol)) * pStore.getNumBands();
				}
				if ((indexTo >= 0) && (indexTo < size)) {
					for (j = 0; j < pStore.getNumBands(); j++) {
						pStore.set((indexTo + j), image.getDouble(index + j));
					}
				}
				if (flipCol) {
					// index++;;
					index += image.getNumBands();
					flipCol = false;
				} else {
					flipCol = true;
				}
			}
			if (flipRow) {
				flipRow = false;
			} else {
				// index -= numcols;
				index -= offset;
				flipRow = true;
			}
		}

		return (pStore);
	}

	// ///////////////////////////////////////////////////////////////////
	// rotate by angle degrees and pad the empty space with zeros
	// ///////////////////////////////////////////////////////////////////
	public ImageObject RotateImage(ImageObject image, SubArea areaProcess, float angle) throws ImageException {
		int numrows, numcols;
		if ((image == null) || ((numrows = image.getNumRows()) <= 0) || ((numcols = image.getNumCols()) <= 0)) {
			System.out.println("Error: no image to rotate \n");
			return null;
		}
		/*
		 * if ((image.getTypeString().equalsIgnoreCase("BYTE") == false) &&
		 * (image.getTypeString().equalsIgnoreCase("SHORT") == false)) {
		 * System.out.println("Error: other than BYTE & SHORT types are not
		 * supported \n"); return null; }
		 */if (areaProcess == null) {
			System.out.println("Error: area to process has not been specified");
			return null;
		}
		if ((areaProcess != null) && !areaProcess.CheckSubArea(0, 0, image.getNumRows(), image.getNumCols())) {
			System.out.println("Error: area to process is out of bounds");
			return null;
		}

		ImageObject pStore = null;
		double alpha = angle * _lim.Deg2Rad; // PI/180;
		if (_returnOrigImSize) {
			if (areaProcess != null) {
				pStore = ImageObject.createImage(areaProcess.getHigh(), areaProcess.getWide(), image.getNumBands(), image.getType());
				_minRow = 0;
				_minCol = 0;
				_numrowsOut = areaProcess.getHigh();
				_numcolsOut = areaProcess.getWide();
			} else {
				pStore = ImageObject.createImage(image.getNumRows(), image.getNumCols(), image.getNumBands(), image.getType());
				_minRow = 0;
				_minCol = 0;
				_numrowsOut = image.getNumRows();
				_numcolsOut = image.getNumCols();
			}
		} else {
			// FindRotatedDim(numrows,numcols,alpha,&numrowsOut,&numcolsOut,&minRow,&minCol);
			if (areaProcess != null) {
				if (!FindRotatedDim(areaProcess.getHigh(), areaProcess.getWide(), alpha)) {
					return null;
				}
			} else {
				if (!FindRotatedDim(numrows, numcols, alpha)) {
					return null;
				}
			}

			pStore = ImageObject.createImage(_numrowsOut, _numcolsOut, image.getNumBands(), image.getTypeString());
		}

		if (pStore == null) {
			System.out.println("Error: failed to allocate memory \n");
			return null;
		}
		long size = pStore.getSize();// *numrowsOut * (*numcolsOut);

		// padding
		int indexTo, index;
		// ImageObject zero = ImageObject.createImage(1,1,image.getNumBands(),
		// image.getTypeString());
		// zero.SetImageObjectValue(0.0);
		// pStore.SetImageObjectValue(zero);
		pStore.setData(0.0);

		// GeomOper geomOp = new GeomOper();
		ImPoint pts1 = new ImPoint();
		ImPoint ptsc = new ImPoint();
		ptsc.x = 0.0;
		ptsc.y = 0.0;

		boolean signal = true;
		int j;
		int row1, col1;
		float row, col;
		int offset, offsetArea;
		int rowStart, colStart, rowEnd, colEnd;
		// setup the loops
		if (areaProcess != null) {
			rowStart = areaProcess.getRow();
			colStart = areaProcess.getCol();
			rowEnd = areaProcess.getRow() + areaProcess.getHigh();
			colEnd = areaProcess.getCol() + areaProcess.getWide();
			offsetArea = (image.getNumCols() - areaProcess.getWide()) * image.getNumBands();
			offset = areaProcess.getWide() * image.getNumBands();
			index = (areaProcess.getRow() * image.getNumCols() + areaProcess.getCol()) * image.getNumBands();
		} else {
			rowStart = 0;
			colStart = 0;
			rowEnd = image.getNumRows();
			colEnd = image.getNumCols();
			offsetArea = 0;
			offset = image.getNumCols() * image.getNumBands();
			index = 0;
		}

		boolean flipCol = false;// 0;// based on 0.5 increment saves
		// multiplications for index
		boolean flipRow = false;// 0;// based on 0.5 increment saves
		// multiplications for index
		for (row = rowStart; row < rowEnd; row += 0.5) {
			for (col = colStart; col < colEnd; col += 0.5) {
				pts1.x = row;
				pts1.y = col;
				_geomOp.rotatePoint(ptsc, pts1, alpha);

				if (pts1.x > 0) {
					row1 = (int) (pts1.x + 0.5);
				} else {
					row1 = (int) (pts1.x - 0.5);
				}
				if (pts1.y > 0) {
					col1 = (int) (pts1.y + 0.5);
				} else {
					col1 = (int) (pts1.y - 0.5);
				}

				if (_returnOrigImSize) {
					if ((row1 >= 0) && (col1 >= 0) && (row1 < pStore.getNumRows()) && (col1 < pStore.getNumCols())) {
						indexTo = (row1 * pStore.getNumCols() + col1) * pStore.getNumBands();
					} else {
						indexTo = -1;
					}
				} else {
					indexTo = ((row1 - _minRow) * (pStore.getNumCols()) + (col1 - _minCol)) * pStore.getNumBands();
				}
				if ((indexTo >= 0) && (indexTo < size)) {
					for (j = 0; j < pStore.getNumBands(); j++) {
						pStore.set((indexTo + j), image.getDouble(index + j));
					}
				}
				if (flipCol) {
					// index++;;
					index += image.getNumBands();
					flipCol = false;
				} else {
					flipCol = true;
				}
			}
			if (flipRow) {
				flipRow = false;
				index += offsetArea;
			} else {
				// index -= numcols;
				index -= offset;
				flipRow = true;
			}
		}
		signal = false;

		return (pStore);
	}

	// ////////////////////////////////////////////////////////////////////////
	// Computes a new image size after rotation by angle degrees is completed
	// ////////////////////////////////////////////////////////////////////////
	public boolean FindRotatedDim(int numrows, int numcols, double alpha) {
		// find the new size of the image

		// initialize resulting values
		_minRow = -1;
		_minCol = -1;
		_numrowsOut = -1;
		_numcolsOut = -1;

		// GeomOper geomOp;
		ImPoint ptsc = new ImPoint();
		ImPoint[] pts = new ImPoint[4];
		int i;
		for (i = 0; i < 4; i++) {
			pts[i] = new ImPoint();
		}

		ptsc.x = 0.0;
		ptsc.y = 0.0;
		pts[0].x = 0.0;
		pts[0].y = numcols;
		pts[1].x = numrows;
		pts[1].y = 0.0;
		pts[2].x = numrows;
		pts[2].y = numcols;
		pts[3] = ptsc;
		// rotate three corner point around (0,0)
		_geomOp.rotatePoints(ptsc, pts, 3, alpha);
		// find max and min
		double minX, minY, maxX, maxY;
		if (numrows > numcols) {
			minX = numrows;
			maxX = -numrows;
			minY = numrows;
			maxY = -numrows;
		} else {
			minX = numcols;
			maxX = -numcols;
			minY = numcols;
			maxY = -numcols;
		}
		for (i = 0; i < 4; i++) {
			if (pts[i].x > maxX) {
				maxX = pts[i].x;
			}
			if (pts[i].x < minX) {
				minX = pts[i].x;
			}
			if (pts[i].y > maxY) {
				maxY = pts[i].y;
			}
			if (pts[i].y < minY) {
				minY = pts[i].y;
			}
		}
		_numrowsOut = (int) (maxX - minX + 1);
		_numcolsOut = (int) (maxY - minY + 1);
		if (minX > 0) {
			_minRow = (int) (minX + 0.5);
		} else {
			_minRow = (int) (minX - 0.5);
		}
		if (minY > 0) {
			_minCol = (int) (minY + 0.5);
		} else {
			_minCol = (int) (minY - 0.5);
		}

		// test
		System.out.println("Test: numrowsOut=" + _numrowsOut + ", numcolsOut=" + _numcolsOut);
		System.out.println("Test: minRow=" + _minRow + ", minCol=" + _minCol);
		if ((_numrowsOut < 2) || (_numcolsOut < 2)) {
			System.out.println("Error: problem with rotated size \n");
			return false;
		}
		return true;
	}

	// ///////////////////////////////////////////////////////////////////
	// rotate by 180 degrees
	// ///////////////////////////////////////////////////////////////////
	public boolean RotateImage180(ImageObject image) throws ImageException {
		int numrows, numcols;
		if ((image == null) || ((numrows = image.getNumRows()) <= 0) || ((numcols = image.getNumCols()) <= 0)) {
			System.out.println(" Error: no image to rotate \n");
			return false;
		}

		ImageObject pStore = null;// ImageObject.createImage(image.getNumRows(),image.getNumCols(),image.getNumBands(),image.getTypeString());
		try {
			// pStore = image.CopyImageObject();
			pStore = (ImageObject) image.clone();
		} catch (CloneNotSupportedException e) {
			System.out.println("Error: failed to allocate memory \n");
			return false;
		}
		return (RotateImage180(image, pStore));

	}

	// ///////////////////////////////////////////////////////////////////
	// rotate by 180 degrees
	// rotate without allocating extra memory
	// image is the input image, pStore is the output image !
	// pStore must contain a copy of the input image !!!
	// ///////////////////////////////////////////////////////////////////
	private boolean RotateImage180(ImageObject image, ImageObject pStore) {

		if ((image == null) || (pStore == null) || (image.getNumRows() <= 0) || (image.getNumCols() <= 0)) {
			System.out.println(" Error: no image to rotate \n");
			return false;
		}
		if ((image.getSize() != pStore.getSize()) || !image.getTypeString().equalsIgnoreCase(pStore.getTypeString())) {
			System.out.println(" Error: mismatch of input and output images \n");
			return false;

		}

		boolean signal = true;
		int j;
		long size = image.getSize();// numrows*numcols;

		int indexEnd = pStore.getSize() - pStore.getNumBands();
		int indexBeg = 0;
		while (indexBeg < image.getSize()) {
			for (j = 0; j < image.getNumBands(); j++) {
				// image.image[indexBeg + j] = pStore.image[indexEnd + j];
				image.set(indexBeg + j, pStore.getDouble(indexEnd + j));
			}
			indexBeg += image.getNumBands();
			indexEnd -= image.getNumBands();
		}
		signal = false;

		// test
		// System.out.println("Test: signal="+signal);
		// image.PrintImageObject();
		pStore = null;
		return true;

	}

	// ///////////////////////////////////////////////////////////////////
	// rotate by 90 degrees
	// ///////////////////////////////////////////////////////////////////
	public ImageObject RotateImage90(ImageObject imageIn) throws ImageException {
		if (imageIn == null) {
			System.out.println(" Error: no image to rotate \n");
			return null;
		}
		int numrowsIn, numcolsIn, numrowsOut, numcolsOut;
		numrowsOut = numcolsIn = imageIn.getNumCols();
		numcolsOut = numrowsIn = imageIn.getNumRows();

		ImageObject imageOut = ImageObject.createImage(numrowsOut, numcolsOut, imageIn.getNumBands(), imageIn.getType());
		if (imageOut == null) {
			System.out.println("Error: not enough memory \n");
			return null;
		}
		long size = imageIn.getSize();// numrowsIn * numcolsIn;
		int offset = numcolsIn * imageIn.getNumBands();
		int offset1 = numcolsOut * imageOut.getNumBands();
		int i, j, band;
		int index, index1;
		int indexStore, index1Store;

		// rotate the image
		index = indexStore = 0;
		index1 = index1Store = (numrowsIn - 1) * imageOut.getNumBands();

		for (j = 0; j < numcolsIn; j++) {
			for (i = 0; i < numrowsIn; i++) {
				for (band = 0; band < imageIn.getNumBands(); band++) {
					// imageOut.image[index1 + band] = imageIn.image[index +
					// band];
					imageOut.set(index1 + band, imageIn.getDouble(index + band));
				}
				index1 -= imageIn.getNumBands();
				index += offset;
			}
			index = indexStore + imageIn.getNumBands();// j;
			index1 = index1Store + offset1;
			indexStore = index;
			index1Store = index1;
		}
		return (imageOut);
	}

	// ///////////////////////////////////////////////////////////////////
	// rotate by -90 degrees
	// ///////////////////////////////////////////////////////////////////
	public ImageObject RotateImageMin90(ImageObject imageIn) throws ImageException {
		if (imageIn == null) {
			System.out.println(" Error: no image to rotate \n");
			return null;
		}
		int numrowsIn, numcolsIn, numrowsOut, numcolsOut;
		numrowsOut = numcolsIn = imageIn.getNumCols();
		numcolsOut = numrowsIn = imageIn.getNumRows();

		ImageObject imageOut = ImageObject.createImage(numrowsOut, numcolsOut, imageIn.getNumBands(), imageIn.getType());
		if (imageOut == null) {
			System.out.println("Error: not enough memory \n");
			return null;
		}
		long size = imageIn.getSize();// numrowsIn * numcolsIn;

		int offset = numcolsIn * imageIn.getNumBands();
		int offset1 = numcolsOut * imageOut.getNumBands();
		int i, j, band;
		int index, index1;
		int indexStore, index1Store;

		// rotate the image
		index = indexStore = 0;
		index1 = index1Store = (numcolsIn - 1) * numcolsOut * imageOut.getNumBands();
		for (j = 0; j < numcolsIn; j++) {
			for (i = 0; i < numrowsIn; i++) {
				for (band = 0; band < imageIn.getNumBands(); band++) {
					// imageOut.image[index1 + band] = imageIn.image[index +
					// band];
					imageOut.set(index1 + band, imageIn.getDouble(index + band));
				}
				index1 += imageOut.getNumBands();
				index += offset;
			}
			index = indexStore + imageIn.getNumBands();
			index1 = index1Store - offset1;
			indexStore = index;
			index1Store = index1;
		}
		return (imageOut);
	}

	// ///////////////////////////////////////////////////////////////////
	// flip the image around vertical middle axis
	// ///////////////////////////////////////////////////////////////////
	public boolean FlipVert(ImageObject imageIn) throws ImageException {
		// sanity check
		if ((imageIn == null) || (imageIn.getNumRows() <= 0) || (imageIn.getNumCols() <= 0)) {
			System.out.println(" Error: no image to rotate \n");
			return false;
		}
		int numrowsIn, numcolsIn;
		numrowsIn = imageIn.getNumRows();
		numcolsIn = imageIn.getNumCols();

		// flip around vertical axis
		int i, j, band;
		int halfcols = numcolsIn >> 1;
		int index = 0;
		int offset, offset1, index_flip;
		offset1 = (numcolsIn - halfcols) * imageIn.getNumBands();
		offset = index_flip = (numcolsIn - 1) * imageIn.getNumBands();

		double tmp;
		for (i = 0; i < numrowsIn; i++) {
			index_flip = index + offset;
			for (j = 0; j < halfcols; j++) {
				for (band = 0; band < imageIn.getNumBands(); band++) {
					/*
					 * tmp = imageIn.image[index_flip + band];
					 * imageIn.image[index_flip + band] = imageIn.image[index +
					 * band]; imageIn.image[index + band] = tmp;
					 */
					tmp = imageIn.getDouble(index_flip + band);
					imageIn.set(index_flip + band, imageIn.getDouble(index + band));
					imageIn.set(index + band, tmp);

				}
				index += imageIn.getNumBands();
				index_flip -= imageIn.getNumBands();
			}
			index += offset1;
		}
		return true;
	}

	// ///////////////////////////////////////////////////////////////////
	// flip the image around horizontal middle axis
	// ///////////////////////////////////////////////////////////////////
	public boolean FlipHoriz(ImageObject imageIn) throws ImageException {
		if ((imageIn == null) || (imageIn.getNumRows() <= 0) || (imageIn.getNumCols() <= 0)) {
			System.out.println(" Error: no image to rotate \n");
			return false;
		}

		int numrowsIn, numcolsIn;
		numrowsIn = imageIn.getNumRows();
		numcolsIn = imageIn.getNumCols();

		// flip around horizontal axis
		int i, j, band;
		int halfrows = numrowsIn >> 1;
		int index = 0;
		int index_flip = imageIn.getSize();
		int offset = numcolsIn * imageIn.getNumBands();

		double tmp;
		for (i = 0; i < halfrows; i++) {
			index_flip -= offset;
			for (j = 0; j < numcolsIn; j++) {
				for (band = 0; band < imageIn.getNumBands(); band++) {
					/*
					 * tmp = imageIn.image[index_flip + band];
					 * imageIn.image[index_flip + band] = imageIn.image[index +
					 * band]; imageIn.image[index + band] = tmp;
					 */
					tmp = imageIn.getDouble(index_flip + band);
					imageIn.set(index_flip + band, imageIn.getDouble(index + band));
					imageIn.set(index + band, tmp);

				}
				index += imageIn.getNumBands();
				index_flip += imageIn.getNumBands();
			}
			index_flip -= offset;
		}
		return true;
	}

}// end of class
