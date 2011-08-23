package edu.illinois.ncsa.isda.imagetools.ext.conversion;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.imagetools.core.io.ImageLoader;
import edu.illinois.ncsa.isda.imagetools.core.io.csv.CSVLoader;
import edu.illinois.ncsa.isda.imagetools.ext.math.MatrixExt;
import edu.illinois.ncsa.isda.imagetools.ext.misc.ImRotation;

/**
 * This code was written to load csv tables with medical information from UIC
 * islet transplant center
 * 
 * @author Peter Bajcsy
 * @version 1.0
 */

public class Test_CSVToImage extends Object {

	private final CSVLoader	_myCSVToImage	= new CSVLoader();
	public boolean			_testPassed		= true;

	/**
	 * The code creates a binary image or color-coded image from cvs file with
	 * tabular rows/cols mapping to image rows/columns (or -90 degree rotated to
	 * obtain time as horizontal and variable as vertical axes )
	 * 
	 * @param args
	 *            input csv table
	 * @throws Exception
	 */
	public static void main(String args[]) throws Exception {

		Test_CSVToImage myTest = new Test_CSVToImage();

		System.out.println("argument length=" + args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.println("args[" + i + "]:" + args[i]);
		}
		if ((args == null) || (args.length < 1)) {
			System.out.println("Please, specify the image to analyze and to save output to");
			return;
		}

		String InFileName, OutFileName;
		InFileName = args[0];
		System.out.println(InFileName);

		OutFileName = args[1];
		System.out.println(OutFileName);

		boolean ret = false;
		// ret = myTest.LoadCSVAndConstructBinaryImage(InFileName, OutFileName);
		ret = myTest.LoadCSVAndConstructImage(InFileName, OutFileName);
		System.out.println("Test Result = " + ret);

	}

	// constructor
	public Test_CSVToImage() {
	}

	// load a csv file and construct an image for the UIC islet cell transplant
	// project
	// save out .tif image
	public boolean LoadCSVAndConstructBinaryImage(String InFileName, String OutFileName) throws Exception {

		ImageObject testObject = null;
		testObject = _myCSVToImage.readImage(InFileName, null, 0);
		System.out.println("Info: this is the input image filename=" + InFileName);

		if (testObject == null) {
			System.out.println("ERROR: could not read");
			return false;
		}
		testObject.toString();
		// //////////////
		int numOfCol = testObject.getNumCols();
		if (numOfCol < 22) {
			System.out.println("ERROR: expected at least 22 columns");
			return false;
		}
		int numVarPerCol[] = new int[numOfCol];
		/*
		 * numVarPerCol[0] = 13; numVarPerCol[1] = 6 + numVarPerCol[0];
		 * numVarPerCol[2] = 13 + numVarPerCol[1]; numVarPerCol[3] = 12 +
		 * numVarPerCol[2]; numVarPerCol[4] = 23 + numVarPerCol[3];
		 * numVarPerCol[5] = 1 + numVarPerCol[4]; numVarPerCol[6] = 5 +
		 * numVarPerCol[5]; numVarPerCol[7] = 2 + numVarPerCol[6];
		 * numVarPerCol[8] = 6 + numVarPerCol[7]; numVarPerCol[9] = 35 +
		 * numVarPerCol[8]; numVarPerCol[10] = 8 + numVarPerCol[9];
		 */
		numVarPerCol[0] = 6;
		numVarPerCol[1] = 13 + numVarPerCol[0];
		numVarPerCol[2] = 6 + numVarPerCol[1];
		numVarPerCol[3] = 1 + numVarPerCol[2];
		numVarPerCol[4] = 1 + numVarPerCol[3];
		numVarPerCol[5] = 1 + numVarPerCol[4];
		numVarPerCol[6] = 1 + numVarPerCol[5];
		numVarPerCol[7] = 1 + numVarPerCol[6];
		numVarPerCol[8] = 1 + numVarPerCol[7];
		numVarPerCol[9] = 1 + numVarPerCol[8];
		numVarPerCol[10] = 1 + numVarPerCol[9];
		numVarPerCol[11] = 1 + numVarPerCol[10];
		numVarPerCol[12] = 1 + numVarPerCol[11];
		numVarPerCol[13] = 1 + numVarPerCol[12];
		numVarPerCol[14] = 1 + numVarPerCol[13];
		numVarPerCol[15] = 1 + numVarPerCol[14];

		numVarPerCol[16] = 12 + numVarPerCol[15];
		numVarPerCol[17] = 23 + numVarPerCol[16];
		numVarPerCol[18] = 1 + numVarPerCol[17];

		numVarPerCol[19] = 5 + numVarPerCol[18];
		numVarPerCol[20] = 2 + numVarPerCol[19];
		numVarPerCol[21] = 35 + numVarPerCol[20];

		numVarPerCol[22] = 8 + numVarPerCol[21];

		int i, j, k;
		int col = 0;
		ImageObject retObject = null;
		retObject = ImageObject.createImage(testObject.getNumRows(), numVarPerCol[22], testObject.getNumBands(), testObject.getType());
		for (i = 0; i < retObject.getSize(); i++) {
			retObject.set(i, 0);
		}
		retObject.setProperty(ImageObject.COMMENT, testObject.getProperty(ImageObject.COMMENT));

		for (i = 0; i < retObject.getNumRows(); i++) {
			for (j = 0; j < retObject.getNumCols(); j++) {
				col = numOfCol - 1;
				for (k = 0; k < numOfCol; k++) {
					if (j < numVarPerCol[k]) {
						col = k;
						k = numOfCol;
					}
				}
				retObject.set(i, j, 0, testObject.getByte(i, col, 0));
			}
		}

		// test
		System.out.println("retImage: property:=" + retObject.getProperty(ImageObject.COMMENT));
		retObject.toString();

		ImageLoader.writeImage(OutFileName, retObject);

		return true;

	}

	/**
	 * This method constructs color image from the tabular data
	 * 
	 * @param InFileName
	 * @param OutFileName
	 * @return
	 * @throws Exception
	 */
	public boolean LoadCSVAndConstructImage(String InFileName, String OutFileName) throws Exception {

		ImageObject testObject = null;
		testObject = _myCSVToImage.readImage(InFileName, null, 1);
		System.out.println("Info: this is the input image filename=" + InFileName);

		if (testObject == null) {
			System.out.println("ERROR: could not read");
			return false;
		}
		testObject.toString();

		int i, j, index;
		byte val;
		// scale the loaded values by its max 100/value and round them
		double maxVal = testObject.getMax(0);
		double[] arr = new double[testObject.getSize()];
		for (i = 0; i < testObject.getSize(); i++) {
			arr[i] = Math.round(100 * testObject.getDouble(i) / maxVal);
			// val = (byte) Math.round(100 * testObject.getDouble(i) / maxVal);
			// retObject.set(i, val);
		}

		MatrixExt matrix = new MatrixExt(testObject.getNumRows(), testObject.getNumCols(), arr);
		MatrixExt matrixT = matrix.transpose();
		/*
		 * matrixT.calculateEigen(); MatrixExt eigenValues =
		 * matrixT.getEigenValue(); System.out.println("EigenValues=");
		 * eigenValues.printMatrix();
		 */
		// create a byte image
		ImageObject retObject = null;
		ImRotation rot = new ImRotation();
		retObject = rot.RotateImageMin90(testObject);
		retObject = retObject.scale(1.0, 10.0);
		/*
		 * retObject = ImageObject.createImage(testObject.getNumCols(),
		 * testObject.getNumRows(), testObject.getNumBands(),
		 * ImageObject.TYPE_BYTE); index = 0; for (i = 0; i <
		 * retObject.getNumRows(); i++) { for (j = 0; j <
		 * retObject.getNumCols(); j++) { retObject.set(index, (byte)
		 * matrixT.getArray()[i][j]); index++; } }
		 */
		retObject.setProperty(ImageObject.COMMENT, testObject.getProperty(ImageObject.COMMENT));

		ImageLoader.writeImage(OutFileName, retObject);

		return true;

	}

}