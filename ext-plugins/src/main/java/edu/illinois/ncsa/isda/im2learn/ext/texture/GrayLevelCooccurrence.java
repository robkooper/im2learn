package edu.illinois.ncsa.isda.im2learn.ext.texture;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.swing.JOptionPane;

import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageException;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.im2learn.core.datatype.SubArea;
import edu.illinois.ncsa.isda.im2learn.core.io.ImageLoader;
import edu.illinois.ncsa.isda.im2learn.ext.statistics.ImStats;
import edu.illinois.ncsa.isda.im2learn.ext.statistics.pdf.Gaussian1D;


/* 
 * </p>
 * Release notes: </B> <BR>
 * 
 * @author Peter Bajcsy
 * @version 1.0
 * 
 */

public class GrayLevelCooccurrence {
	private int		_kernelNumRows		= 10;
	private int		_kernelNumCols		= 10;
	private int		_numOrientations	= 1;
	private int		_numIntensityBins	= 10;
	private int[][]	_glcm				= null;
	private double	_minVal				= 0.0;
	private double	_maxVal				= 0.0;

	/**
	 * Constructor
	 * 
	 * @param rows
	 *            Number of rows in kernel
	 * @param cols
	 *            Number of columns in kernel
	 * @param numOrientations
	 *            Number of filter orientations in filter bank
	 */
	public GrayLevelCooccurrence(int kernelNumRows, int kernelNumCols, int numOrientations) {
		_kernelNumRows = kernelNumRows;
		_kernelNumCols = kernelNumCols;
		_numOrientations = numOrientations;
	}

	public GrayLevelCooccurrence() {

	}

	// setters and getters

	public void setKernelNumRows(int rows) {
		_kernelNumRows = rows;
	}

	public int getKernelNumRows() {
		return _kernelNumRows;
	}

	public void setKernelNumCols(int cols) {
		_kernelNumCols = cols;
	}

	public int getKernelNumCols() {
		return _kernelNumCols;
	}

	public void setNumOrientations(int omega) {
		_numOrientations = omega;
	}

	public int getNumOrientations() {
		return _numOrientations;
	}

	public void setNumIntensityBins(int val) {
		_numIntensityBins = val;
	}

	public int getNumIntensityBins() {
		return _numIntensityBins;
	}

	public void resetVals() {
		_numOrientations = 1;
		_kernelNumRows = 10;
		_kernelNumCols = 10;
	}

	/**
	 * This method adds together all co-occurrence values It could serve as a
	 * normalization factor to compute probabilities
	 * 
	 * @return sum of all co-occurrence values
	 */
	public int findNormalizationFactor() {
		//sanity check
		if (_glcm == null) {
			System.err.println("ERROR: the glcm is null");
			return -1;
		}
		int sum = 0;
		int i, j;
		for (i = 0; i < _glcm.length; i++) {
			for (j = 0; j < _glcm[i].length; j++) {
				sum += _glcm[i][j];
			}
		}
		return sum;
	}

	/**
	 * This method adds together co-occurrence values within
	 * [minRow,maxRow]x[minCol,maxCol] gray level co-occurrence sub-matrix
	 * 
	 * @return sum of co-occurrence values
	 */
	public int findSumOverSubMatrix(int minIntensity, int maxIntensity) {
		//sanity check
		if (_glcm == null) {
			System.err.println("ERROR: the glcm is null");
			return -1;
		}
		if (minIntensity < 0) {
			System.err.println("ERROR: minIntensity < 0");
			return -1;
		}

		int sum = 0;
		int i, j;
		for (i = minIntensity; i <= maxIntensity && i < _glcm.length; i++) {
			for (j = minIntensity; j <= maxIntensity && j < _glcm[i].length; j++) {
				sum += _glcm[i][j];
			}
		}
		return sum;
	}

	/**
	 * This method computes the probability (|j(x2)-i(x1)|<= contrast/j(x2) in
	 * FRG/i(x2) in FRG and x2 = x1+1) FRG is defined as [minINtensity,
	 * maxIntensity]
	 * 
	 * @param contrast
	 * @param minIntensity
	 * @param maxIntensity
	 * @return sum along diagonals of GLCM entries depending on the contrast
	 *         value
	 */
	public int findSumOverGLCMDiagonal(double contrast, int minIntensity, int maxIntensity) {
		//sanity check
		if (_glcm == null) {
			System.err.println("ERROR: the glcm is null");
			return -1;
		}
		if (minIntensity < 0) {
			System.err.println("ERROR: minIntensity < 0");
			return -1;
		}

		int sum = 0;
		int i, j;
		for (i = minIntensity; i <= maxIntensity && i < _glcm.length; i++) {
			for (j = minIntensity; j <= maxIntensity && j < _glcm[i].length; j++) {
				if (Math.abs(i - j) <= contrast) {
					sum += _glcm[i][j];
				}
			}
		}
		return sum;
	}

	public void printSumVectorOverGLCMDiagonal(String OutFileName, int minIntensity, int maxIntensity) throws IOException {

		//			sanity check
		if (_glcm == null) {
			System.err.println("ERROR: the glcm is null");
			return;
		}
		int i, j;
		// open the file into which the output will be written.
		FileOutputStream fileOut = new FileOutputStream(OutFileName);
		OutputStreamWriter out = new OutputStreamWriter(fileOut);
		out.write("Threshold, Occurrence Sum, Probability \n");

		int foreground = findSumOverSubMatrix(minIntensity, maxIntensity);
		if (foreground <= 0) {
			System.err.println("ERROR: the sum of glcm entries is zero");
			return;
		}
		double val;
		int threshold, occurSum = 0;
		//int [] dist = new int[_numIntensityBins+1];
		for (threshold = 0; threshold <= _numIntensityBins; threshold++) {
			//dist[threshold] = findSumOverGLCMDiagonal(threshold, minIntensity,maxIntensity);
			//System.out.println("Info: threshold = " + threshold+", distance ="+dist[threshold] + ", prob=" + ((double) dist[threshold]/foreground));
			//System.out.println(threshold+","+dist[threshold] + "," + ((double) dist[threshold]/foreground));
			occurSum = findSumOverGLCMDiagonal(threshold, minIntensity, maxIntensity);
			val = ((double) occurSum / foreground);
			out.write(Integer.toString(threshold) + "," + occurSum + "," + Double.toString(val) + "\n");
		}

		// flush out the buffer.
		out.flush();

	}

	public void computeGLCM(ImageObject img, ImageObject mask, int bandIdx, int foreground) {
		//sanity checks
		if (img == null) {
			System.err.println("ERROR: img is null");
			return;
		}
		if (mask == null) {
			System.err.println("ERROR: img is null");
			return;
		}
		if (img.getNumRows() != mask.getNumRows() || img.getNumCols() != mask.getNumCols()) {
			System.err.println("ERROR: img and mask do not have the same dimensions");
			return;
		}
		if (bandIdx < 0 || bandIdx >= img.getNumBands()) {
			System.err.println("ERROR: number of bands=" + img.getNumBands() + " is smaller than then idxband=" + bandIdx);
			return;
		}

		_glcm = new int[_numIntensityBins][_numIntensityBins];
		int i, j;
		for (i = 0; i < _glcm.length; i++) {
			for (j = 0; j < _glcm[i].length; j++) {
				_glcm[i][j] = 0;
			}
		}


		// compute the glcm only for the first band and the horizontal direction !!!
		//TODO compute GLCM for other directions and multiple bands!!!
		double val, val1;
		_minVal = img.getMin(0);
		_maxVal = img.getMax(0);
		double binWidth = img.getTypeMax() / _numIntensityBins;
		System.out.println("binWidth=" + binWidth);

		int index = bandIdx, indexMask = 0;
		int m, n;
		for (i = 0; i < img.getNumRows(); i++) {
			for (j = 0; j < img.getNumCols()-1; j++) {
				
				//if(i > 200)
				//	System.out.println(i+","+j+","+mask.getInt(indexMask)+"," + mask.getInt(indexMask +1) );
				
				if (mask.getInt(indexMask) == foreground & mask.getInt(indexMask + 1) == foreground) {
					val = img.getDouble(index);
					val1 = img.getDouble(index + img.getNumBands());
					//System.out.println("i=" + (int) (val / binWidth) + " j=" + (int) (val1 / binWidth));
					m = (int) (val / binWidth);
					n = (int) (val1 / binWidth);
					if (m >= _numIntensityBins)
						m = _numIntensityBins - 1;
					if (n >= _numIntensityBins)
						n = _numIntensityBins - 1;

					_glcm[m][n] += 1;
				}
				index += img.getNumBands();
				indexMask++;
			}
			index += img.getNumBands();
			indexMask++;

		}

	}

	public void computeGLCM(ImageObject img, int row, int col) {
		//sanity checks
		if (img == null) {
			System.err.println("ERROR: img is null");
		}
		if (row < 0 || row >= img.getNumRows()) {
			System.err.println("ERROR: row is out of bounds");
		}
		if (col < 0 || col >= img.getNumCols()) {
			System.err.println("ERROR: col is out of bounds");
		}

		_glcm = new int[_numIntensityBins][_numIntensityBins];
		int i, j;
		for (i = 0; i < _glcm.length; i++) {
			for (j = 0; j < _glcm[i].length; j++) {
				_glcm[i][j] = 0;
			}
		}

		int halfKernelRow = _kernelNumRows >> 1;
		int halfKernelCol = _kernelNumCols >> 1;
		int lowerRow, lowerCol, upperRow, upperCol;
		lowerRow = row - halfKernelRow;
		lowerCol = col - halfKernelCol;
		upperRow = row + halfKernelRow;
		upperCol = col + halfKernelCol;
		if (lowerRow < 0)
			lowerRow = 0;
		if (upperRow >= img.getNumRows())
			upperRow = img.getNumRows() - 1;
		if (lowerCol < 0)
			lowerCol = 0;
		if (upperCol >= img.getNumCols())
			upperCol = img.getNumCols() - 1;

		// compute the glcm only for the first band and the horizontal direction !!!
		//TODO compute GLCM for other directions and multiple bands!!!
		double val, val1;
		int index = 0;
		_minVal = img.getMin(0);
		_maxVal = img.getMax(0);
		double binWidth = img.getTypeMax() / _numIntensityBins;
		System.out.println("binWidth=" + binWidth);

		int m, n;
		for (i = lowerRow; i <= upperRow; i++) {
			for (j = lowerCol; j < upperCol; j++) {
				val = img.getDouble(index);
				val1 = img.getDouble(index + img.getNumBands());
				//System.out.println("i=" + (int) (val / binWidth) + " j=" + (int) (val1 / binWidth));
				m = (int) (val / binWidth);
				n = (int) (val1 / binWidth);
				if (m >= _numIntensityBins)
					m = _numIntensityBins - 1;
				if (n >= _numIntensityBins)
					n = _numIntensityBins - 1;

				_glcm[m][n] += 1;
				index += img.getNumBands();
			}

		}

	}

	public void displayGLCM() {
		if (_glcm == null) {
			System.err.println("ERROR: the glcm is null");
			return;
		}
		int i, j;
		String display = "";
		for (i = 0; i < _glcm.length; i++) {
			for (j = 0; j < _glcm[i].length; j++) {
				display += Integer.toString(_glcm[i][j]) + " | ";
			}
			display += "\n";
		}
		JOptionPane.showMessageDialog(null, display);
	}

	/**
	 * This method prints out the GLCM matrix values in a cvs file
	 * 
	 * @param OutFileName
	 *            - output file name
	 * @throws IOException
	 */
	public void printGLCM(String OutFileName) throws IOException {

		//			sanity check
		if (_glcm == null) {
			System.err.println("ERROR: the glcm is null");
			return;
		}
		int i, j;
		// open the file into which the output will be written.
		FileOutputStream fileOut = new FileOutputStream(OutFileName);
		OutputStreamWriter out = new OutputStreamWriter(fileOut);

		double binWidth = _maxVal / _numIntensityBins;
		double val;

		out.write("Bin Lower End Values \n");
		for (val = _minVal; val < _maxVal - binWidth; val += binWidth) {
			out.write(Double.toString(val) + ",");
		}
		out.write(Double.toString(_maxVal - binWidth) + "\n");

		for (i = 0; i < _glcm.length; i++) {
			for (j = 0; j < _glcm[i].length - 1; j++) {
				out.write(Integer.toString(_glcm[i][j]) + ", ");
			}
			out.write(Integer.toString(_glcm[i][j]) + "\n");
		}
		// flush out the buffer.
		out.flush();
	}

	/**
	 * Save the image representation of teh co-occurrence matrix
	 * 
	 * @param OutFileName
	 *            - output file name
	 * @throws IOException
	 * @throws ImageException
	 */
	public void saveImageGLCM(String OutFileName) throws IOException, ImageException {
		if (_glcm == null) {
			System.err.println("ERROR: the glcm is null");
			return;
		}
		int i, j;

		ImageObject outImage = null;
		if (maxGLCMValue() < 256) {
			outImage = ImageObject.createImage(_numIntensityBins, _numIntensityBins, 1, ImageObject.TYPE_BYTE);
			for (i = 0; i < _glcm.length; i++) {
				for (j = 0; j < _glcm[i].length; j++) {
					outImage.set(i, j, 0, (byte) _glcm[i][j]);
				}
			}
		} else {
			outImage = ImageObject.createImage(_numIntensityBins, _numIntensityBins, 1, ImageObject.TYPE_INT);
			for (i = 0; i < _glcm.length; i++) {
				for (j = 0; j < _glcm[i].length; j++) {
					outImage.set(i, j, 0, _glcm[i][j]);
				}
			}
		}
		ImageLoader.writeImage(OutFileName, outImage);
		return;
	}

	/**
	 * Compute the max value of co-occurrence matrix
	 * 
	 * @return int value = maximum
	 */
	public int maxGLCMValue() {

		int maxVal = -1;
		if (_glcm == null) {
			System.err.println("ERROR: the glcm is null");
			return maxVal;
		}
		int i, j;

		for (i = 0; i < _glcm.length; i++) {
			for (j = 0; j < _glcm[i].length; j++) {
				if (maxVal < _glcm[i][j]) {
					maxVal = _glcm[i][j];
				}
			}
		}
		return maxVal;

	}

	/**
	 * Testing routine
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String args[]) throws Exception {

		System.out.println("argument length=" + args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.println("args[" + i + "]:" + args[i]);
		}
		if ((args == null) || (args.length < 1)) {
			System.out.println("Please, specify the image to analyze and to save output to");
			System.out.println("arg = Input_ImageName, Output_ImageName");
			return;
		}

		String InFileName1, OutFileName;

		InFileName1 = args[0];
		System.out.println(InFileName1);

		OutFileName = args[1];
		System.out.println(OutFileName);

		// read the input images 
		ImageObject testObject = null;
		testObject = ImageLoader.readImage(InFileName1);
		System.out.println("Info: this is the input image filename=" + InFileName1);
		testObject.toString();
		// set the band to process !!!
		int band = 0;
		ImageObject testObject1 = testObject.extractBand(band);
		////////////////

		GrayLevelCooccurrence myTest = new GrayLevelCooccurrence();
		myTest.setKernelNumCols(100);
		myTest.setKernelNumRows(100);
		myTest.setNumIntensityBins(255);
		myTest.computeGLCM(testObject1, (testObject1.getNumRows() >> 1), (testObject1.getNumCols() >> 1));

		ImStats imstats = new ImStats();
		if (!imstats.MeanStdevVal(testObject1)) {
			System.err.println("ERROR: mean and stdev could not be computed");
			return;
		}
		imstats.PrintImStats();

		double[] meanFRG;
		meanFRG = imstats.GetMeanVal();
		double[] stdevFRG;
		stdevFRG = imstats.GetStdevVal();

/*
 * double [] meanBorder = new double [1]; meanBorder[0] = 0.0; double []
 * stdevBorder = new double [1]; stdevBorder[0] = 0.0;
 */

		double dividingIntensity = 0;
		Gaussian1D gauss = new Gaussian1D(meanFRG[0], stdevFRG[0]);
		dividingIntensity = gauss.findTwoPDFDividingValue(200.0, 0.0);
		double probOfSeed = 1.0 - gauss.calculateCDF(dividingIntensity);
		System.out.println("Info: dividing intensity=" + Double.toString(dividingIntensity));
		System.out.println("Info: probOfSeed=" + Double.toString(probOfSeed));

		String imageGLCMName = new String();
		String imageSubareaName = new String();
		String csvGLCMName = new String();
		String csvGLCMDiagonalName = new String();
		imageGLCMName = OutFileName.substring(0, OutFileName.length() - 4);
		csvGLCMName = imageGLCMName + "band" + band + ".csv";
		csvGLCMDiagonalName = imageGLCMName + "band" + band + "dist.csv";
		imageSubareaName = imageGLCMName + "areaband" + band + ".tif";
		imageGLCMName += "band" + band + ".tif";

		System.out.println("Info: this is the GLCM image filename=" + imageGLCMName);
		System.out.println("Info: this is the subarea image filename=" + imageSubareaName);
		System.out.println("Info: this is the GLCM csv filename=" + csvGLCMName);
		System.out.println("Info: this is the GLCM Diagonal sum csv filename=" + csvGLCMDiagonalName);

		//SubArea area = new SubArea( (testObject1.getNumCols() >> 1), (testObject1.getNumRows() >> 1), myTest.getKernelNumCols(),myTest.getKernelNumRows() );
		SubArea area = new SubArea((testObject1.getNumCols() >> 1) - (myTest.getKernelNumCols() >> 1), (testObject1.getNumRows() >> 1) - (myTest.getKernelNumRows() >> 1), myTest.getKernelNumCols(),
				myTest.getKernelNumRows());
		System.out.println("Info: area=" + area.toString());
		ImageObject sampleImage = testObject1.crop(area);

		ImageLoader.writeImage(imageSubareaName, sampleImage);// image sub-area
		myTest.printGLCM(csvGLCMName);//csv file with co-occurrence matrix
		myTest.saveImageGLCM(imageGLCMName); // image representation of co-occurrence matrix

		int norm = myTest.findNormalizationFactor();
		System.out.println("Info: normalization =" + norm);
		int foreground = myTest.findSumOverSubMatrix(174, 255);
		System.out.println("Info: foregroud =" + foreground + ", prob=" + ((double) foreground / norm));
		myTest.printSumVectorOverGLCMDiagonal(csvGLCMDiagonalName, 174, 255);

	}
}
