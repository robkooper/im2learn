package edu.illinois.ncsa.isda.im2learn.main;

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
 *   Neither the names of University of Illinois/NCSA, nor the names of
 *   its contributors may be used to endorse or promote products derived
 *   from this Software without specific prior written permission.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
 * 
 *******************************************************************************/

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;

import javax.swing.JFrame;

import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageException;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.im2learn.core.io.pdf.PDFWriter;


/**
 * This class was developed to generate PDF files automatically The PDF files
 * contain either only text, or only images, or only vector graphics objects.
 * The words are randomly selected from a dictionary of 10 words (1-11
 * characters long). The pixels are randomly selected in a RGB color space. The
 * vector graphics objects are composed by connecting randomly generated points
 * on a single PDF page.
 * 
 * @author pbajcsy
 * 
 */
public class PDFSyntheticGenerator extends JFrame {
	PDFWriter						pdf;
	private ArrayList<ImageObject>	inputIm				= null;
	private int						_lineCount			= 0;
	private int						_wordCountPerLine	= 0;
	private int						_imageCount			= 0;
	private int						_imageDim			= 0;
	private int						_vectorCount		= 0;
	private int						_lineCountPerVector	= 0;
	private String					_outputFilename		= null;
	private ArrayList<String>		_wordToInsert		= null;

/*
 * private final int _deltaLineCount = 100; private final int _minLineCount =
 * 100; private final int _deltaImageCount = 10; private final int
 * _minImageCount = 10; private final int _deltaVectorCount = 10; private final
 * int _minVectorCount = 10;
 */
	private ImageObject				writeIm;

	private final int				figurescale			= 300;

	public boolean					_testPassed			= true;

	/**
	 * Inputs are the parameters of the simulated PDF files
	 * 
	 * @param args
	 *            line count, word count per line,image count, pixel dimension,
	 *            vector graphics count, line count per vector graphics and
	 *            output file name
	 * @throws Exception
	 */
	
	/*
	 * Command arguments
	 * image 25 250 25 500 1000 500 C:\Peterb\Projects\NARA\testDoc2LearnScalability\syntheticDataPreparation\dataForVlad11-9-2010\test
	 * image 25 250 25 2000 5000 1000 C:\Peterb\Projects\NARA\testDoc2LearnScalability\syntheticDataPreparation\dataForVlad11-9-2010\test
	 * image 175 250 25 4000 5000 1000 C:\Peterb\Projects\NARA\testDoc2LearnScalability\syntheticDataPreparation\dataForVlad11-9-2010\test
	 * image 200 250 25 4000 5000 1000 C:\Peterb\Projects\NARA\testDoc2LearnScalability\syntheticDataPreparation\dataForVlad11-9-2010\test
	 * image 25 250 25 6000 7000 1000 C:\Peterb\Projects\NARA\testDoc2LearnScalability\syntheticDataPreparation\dataForVlad11-9-2010\test
	 * image 10 10 10 500 1000 500 C:\Peterb\Projects\NARA\testDoc2LearnScalability\syntheticDataPreparation\dataForVlad11-1-2010
	 * image 10 10 10 2000 9000 1000 C:\Peterb\Projects\NARA\testDoc2LearnScalability\syntheticDataPreparation\dataForVlad11-1-2010
	 * Wanted
	 * Number of pixels: 500x500, 1000x1000, 2000x2000, 3000x3000,4000x4000, 5000x5000, 6000x6000, 7000x7000, 8000x8000 per image = 9 
	 * Number of images per pdf file: 10, 25, 50, 75, 100, 125, 150, 175, 200, 225, 250
	 * Total number of files = 9 x 11 = 99 

	 * 
	 */
			
	public static void main(String args[]) throws Exception {

		System.out.println("argument length=" + args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.println("args[" + i + "]:" + args[i]);
		}
		if ((args == null) || (args.length < 1)) {
			System.out.println("Please, specify the type of PDF (text/image/vector)");
			return;
		}

		int minLineCount = 0, maxLineCount = 0, deltaLineCount = 0;
		int minWordCountPerLine = 0, maxWordCountPerLine = 0, deltaWordCountPerLine = 0;
		int minImageCount = 0, maxImageCount = 0, deltaImageCount = 0;
		int minImageDimension = 0, maxImageDimension = 0, deltaImageDimension = 0;
		int minVectorCount = 0, maxVectorCount = 0, deltaVectorCount = 0;
		int minLineCountPerVector = 0, maxLineCountPerVector = 0, deltaLineCountPerVector = 0;

		String typeOfPDF = args[0];

		if (typeOfPDF.equalsIgnoreCase("text")) {

			if ((args.length < 7)) {
				System.out.println("Please, specify min value for line count, max value for line count, delta value for line count"
						+ "min word count per line,max word count per line, delta word count per line");
				return;
			}

			minLineCount = Integer.parseInt(args[1]);
			maxLineCount = Integer.parseInt(args[2]);
			deltaLineCount = Integer.parseInt(args[3]);

			minWordCountPerLine = Integer.parseInt(args[4]);
			maxWordCountPerLine = Integer.parseInt(args[5]);
			deltaWordCountPerLine = Integer.parseInt(args[6]);
			//test
			System.out.println("min/max/delta line count=" + minLineCount + "," + maxLineCount + "," + deltaLineCount);
			System.out.println("min/max/delta wordCountPerLine=" + minWordCountPerLine + "," + maxWordCountPerLine + "," + deltaWordCountPerLine);

		}

		if (typeOfPDF.equalsIgnoreCase("image")) {

			if ((args.length < 7)) {
				System.out.println("Please, specify min/max/delta image count, min/max/delta pixel dimension");
				return;
			}

			minImageCount = Integer.parseInt(args[1]);
			maxImageCount = Integer.parseInt(args[2]);
			deltaImageCount = Integer.parseInt(args[3]);

			minImageDimension = Integer.parseInt(args[4]);
			maxImageDimension = Integer.parseInt(args[5]);
			deltaImageDimension = Integer.parseInt(args[6]);

			//test
			System.out.println("min/max/delta image count=" + minImageCount + "," + maxImageCount + "," + deltaImageCount);
			System.out.println("min/max/delta image dimension =" + minImageDimension + "," + maxImageDimension + "," + deltaImageDimension);

		}

		if (typeOfPDF.equalsIgnoreCase("vector")) {

			if ((args.length < 7)) {
				System.out.println("Please, specify min/max/delta vector graphics count, min/max/delta line count per vector graphics");
				return;
			}

			minVectorCount = Integer.parseInt(args[1]);
			maxVectorCount = Integer.parseInt(args[2]);
			deltaVectorCount = Integer.parseInt(args[3]);
			minLineCountPerVector = Integer.parseInt(args[4]);
			maxLineCountPerVector = Integer.parseInt(args[5]);
			deltaLineCountPerVector = Integer.parseInt(args[6]);
			//test
			System.out.println("min/max/delta vector count=" + minVectorCount + "," + maxVectorCount + "," + deltaVectorCount);
			System.out.println("min/max/delta Line Count Per Vector =" + minLineCountPerVector + "," + deltaLineCountPerVector + "," + deltaLineCountPerVector);
		}

		if ((args.length < 8)) {
			System.out.println("Please, specify output file name");
			return;
		}
		String OutFileName = args[7];

		//test
		System.out.println(OutFileName);

		// generate PDF documents

		PDFSyntheticGenerator myTest = new PDFSyntheticGenerator();
		myTest.setOutputFilename(OutFileName);

		int i, j;
		if (typeOfPDF.equalsIgnoreCase("text")) {
			for (i = minLineCount; i <= maxLineCount; i += deltaLineCount) {
				for (j = minWordCountPerLine; j <= maxWordCountPerLine; j += deltaWordCountPerLine) {
					myTest.setLineCount(i);
					myTest.setWordCountPerLine(j);
					myTest.generateTextPDF();

				}
			}
		}
		if (typeOfPDF.equalsIgnoreCase("image")) {
			for (i = minImageCount; i <= maxImageCount; i += deltaImageCount) {
				for (j = minImageDimension; j <= maxImageDimension; j += deltaImageDimension) {
					myTest.setImageCount(i);
					myTest.setImageDimension(j);
					myTest.generateImagePDF();

				}
			}
		}

		if (typeOfPDF.equalsIgnoreCase("vector")) {
			for (i = minVectorCount; i <= maxVectorCount; i += deltaVectorCount) {
				for (j = minLineCountPerVector; j <= maxLineCountPerVector; j += deltaLineCountPerVector) {
					myTest.setVectorCount(i);
					myTest.setLineCountPerVector(j);
					myTest.generateVectorPDF();

				}
			}
		}

		// decide what you want to generate
		// the test data were generated by the following arguments
		// 5000 4 250 200 250 25 C:\Peterb\Projects\NARA\testDoc2LearnScalability\syntheticText05-04-2010\test
		// data generated (100-5000, delta=100) (1-4, delta=1), (100-250, delta=10) (50-200, delta=50), (10-250, delta=10) (5-25, delta=5)
		// vary only entries wordCount, pixelCount and vectorSegmentCount

		//myTest.generateImagePDF();
		//myTest.generateVectorPDF();

		//System.out.println("Test Result = " + ret);

	}

	public PDFSyntheticGenerator() {
	}

	public PDFSyntheticGenerator(ArrayList<ImageObject> inputImages) {

		setInputImageSet(inputImages);
	}

	public void setInputImageSet(ArrayList<ImageObject> inputIm) {
		this.inputIm = inputIm;
	}

	public void setOutputFilename(String outFile) {
		_outputFilename = outFile;
	}

	public void setLineCount(int lineCount) {
		_lineCount = lineCount;
	}

	public void setWordCountPerLine(int wordCountPerLine) {
		_wordCountPerLine = wordCountPerLine;
		// create the list of words
		_wordToInsert = new ArrayList<String>();
		_wordToInsert.add("a");
		_wordToInsert.add("b");
		_wordToInsert.add("c");
		_wordToInsert.add("d");
		_wordToInsert.add("e");
		_wordToInsert.add("f");
		_wordToInsert.add("g");
		_wordToInsert.add("h");
		_wordToInsert.add("i");
		_wordToInsert.add("j");
		_wordToInsert.add("k");
		_wordToInsert.add("l");
		_wordToInsert.add("m");
/*
 * _wordToInsert.add("to"); _wordToInsert.add("one"); _wordToInsert.add("four");
 * _wordToInsert.add("yours"); _wordToInsert.add("credit");
 * _wordToInsert.add("Illinois"); _wordToInsert.add("proposals");
 * _wordToInsert.add("incredible"); _wordToInsert.add("test1415161");
 */
	}

	public void setImageCount(int imageCount) {
		_imageCount = imageCount;
	}

	public void setImageDimension(int imageDimension) {

		int i, j;
		// used originally for building a man made image 
		// later replaced by random color values
		int first = (int) (imageDimension * 0.25);
		int last = (int) (imageDimension * 0.75);
		ArrayList<ImageObject> inputImages = new ArrayList<ImageObject>();

		ImageObject myImage = null;
		try {
			myImage = ImageObject.createImage(imageDimension, imageDimension, 3, ImageObject.TYPE_BYTE);
		} catch (ImageException e) {
			System.out.println("ImageException");
			return;
		}
		// black image with red square
		myImage.setData(0);
		int max = 255;
		first = 0;
		last = imageDimension;
		for (i = first; i < last; i++) {
			for (j = first; j < last; j++) {
				myImage.set(i, j, 0, (byte) Math.floor(Math.random() * max));
				myImage.set(i, j, 1, (byte) Math.floor(Math.random() * max));
				myImage.set(i, j, 2, (byte) Math.floor(Math.random() * max));

				//myImage.set(i, j, 0, 255);
				//myImage.set(i, j, 1, 0);
				//myImage.set(i, j, 2, 0);
			}
		}
		inputImages.add(myImage);
		_imageDim = imageDimension;
		setInputImageSet(inputImages);
	}

	public void setVectorCount(int vectorCount) {
		_vectorCount = vectorCount;
	}

	public void setLineCountPerVector(int lineCountPerVector) {
		_lineCountPerVector = lineCountPerVector;
	}

	//+++++++++++++++++++
	public int getLineCount() {
		return _lineCount;
	}

	public int getWordCountPerLine() {
		return _wordCountPerLine;
	}

	public int getImageCount() {
		return _imageCount;
	}

	public int getImageDim() {
		return _imageDim;
	}

	public int getVectorCount() {
		return _vectorCount;
	}

	public int getLineCountPerVector() {
		return _lineCountPerVector;
	}

	/**
	 * Generate PDF files with only text
	 * 
	 * @throws FileNotFoundException
	 */
	public void generateTextPDF() throws FileNotFoundException {

		if ((_outputFilename == null) || (_outputFilename.length() < 1)) {
			System.err.println("Output File is not set");
			return;
		}
		String filename = null;

		int i, j, k = getLineCount();
		long wordCount = 0;
		//wordCount = getWordCountPerLine() * k;
		if (_outputFilename.endsWith(".pdf")) {
			filename = (_outputFilename.substring(0, _outputFilename.length() - 4) + "-text-line" + k + "-wordPerLine" + getWordCountPerLine() + ".pdf");
		} else {
			filename = (_outputFilename + "-text-line" + k + "-wordPerLine" + getWordCountPerLine() + ".pdf");
		}

		if (!new File(filename).canWrite() && new File(filename).exists()) {
			System.err.println("Error:Cannot open the file for writing");
			return;
		}
		pdf = new PDFWriter(new FileOutputStream(filename));
		System.out.println("Output File =" + filename);
		for (i = 0; i < k; i++) {

			String temp = new String("");
			String myString = new String("");
			for (j = 0; j < getWordCountPerLine(); j++) {
				// word length is between 1 and 10
				int wordIndex = (int) Math.floor(Math.random() * _wordToInsert.size());
				myString = _wordToInsert.get(wordIndex);

				temp += " " + myString;
			}
/*
 * String temp = new String(""); String myString = new String(""); for (j = 0; j
 * < getWordCountPerLine(); j++) { // word length is between 1 and 10 int
 * wordLength = (int) Math.floor(Math.random() * 10 + 1); char[] data = new
 * char[wordLength]; for (int l = 0; l < wordLength; l++) { // character set is
 * between 64 and 100 data[l] = (char) Math.floor(Math.random() * 26 + 64); }
 * myString = String.valueOf(data);
 * 
 * temp += " " + myString; }
 */
			pdf.printString(temp + "\n");

		}
		pdf.closePDF();

		//pdf.LaunchPDFFile(filename);
	}

	/**
	 * Generate a PDF file with only images
	 * 
	 * @throws FileNotFoundException
	 */
	public void generateImagePDF() throws FileNotFoundException {

		if ((_outputFilename == null) || (_outputFilename.length() < 1)) {
			System.err.println("Output File is not set");
			return;
		}

		if (inputIm == null) {
			System.err.println("Error: input image array is empty");
			return;
		}

		String filename = null;

		int i, j, k = getImageCount();
		int tempPixelCount = 0;
		for (j = 0; j < inputIm.size(); j++) {
			tempPixelCount += inputIm.get(j).getNumCols() * inputIm.get(j).getNumCols();
			//System.out.println("j=" + j + ", tempPixelCount=" + tempPixelCount);
		}

		long pixelCount = 0;
		pixelCount = (long)tempPixelCount * k;
		if (_outputFilename.endsWith(".pdf")) {
			filename = (_outputFilename.substring(0, _outputFilename.length() - 4) + "-image-" + k + ".pdf");
		} else {
			filename = (_outputFilename + "-image-" + k + ".pdf");
		}
		if (_outputFilename.endsWith(".pdf")) {
			filename = (_outputFilename.substring(0, _outputFilename.length() - 4) + "-image-count" + k + "-pixelCount" + pixelCount + ".pdf");
		} else {
			filename = (_outputFilename + "-image-count" + k + "-pixelCount" + pixelCount + ".pdf");
		}

		if (!new File(filename).canWrite() && new File(filename).exists()) {
			System.err.println("Error: Cannot open the file for writing");
			return;
		}
		pdf = new PDFWriter(new FileOutputStream(filename));
		System.out.println("Output File =" + filename);

		for (i = 0; i < k; i++) {

			if ((inputIm != null) && (inputIm.size() > 0)) {
				//pdf.printHeading(heading++ + ". " + iiHeading, 20);
				//pdf.printString("\n\n");

				for (j = 0; j < inputIm.size(); j++) {
					try {
						writeIm = (ImageObject) inputIm.get(j).clone();
					} catch (CloneNotSupportedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					//pdf.printFigure(writeIm, iiCaption + inputIm.get(i), figurescale);
					pdf.printFigure(writeIm, null, figurescale);
					pdf.printString("\n");
					writeIm = null;
				}

			}
		}
		pdf.closePDF();
		pdf = null;

		//pdf.LaunchPDFFile(filename);
	}

	/**
	 * generate PDF files with only vector graphics objects (lines)
	 * 
	 * @throws FileNotFoundException
	 */
	public void generateVectorPDF() throws FileNotFoundException {

		if ((_outputFilename == null) || (_outputFilename.length() < 1)) {
			System.err.println("Output File is not set");
			return;
		}
		String filename = null;

		int i, j, k = getVectorCount();
		int max = 500; // max page size
		int vectorSegmentCount = 0;

		//vectorSegmentCount = getLineCountPerVector() * k;
		if (_outputFilename.endsWith(".pdf")) {
			filename = (_outputFilename.substring(0, _outputFilename.length() - 4) + "-vector-count" + k + "-segmentCount" + getLineCountPerVector() + ".pdf");
		} else {
			filename = (_outputFilename + "-vector-count" + k + "-segmentCount" + getLineCountPerVector() + ".pdf");
		}

		if (!new File(filename).canWrite() && new File(filename).exists()) {
			System.err.println("Error:Cannot open the file for writing");
			return;
		}
		pdf = new PDFWriter(new FileOutputStream(filename));
		System.out.println("Output File =" + filename);

		for (i = 0; i < k; i++) {

			int[][] temp = new int[getLineCountPerVector() + 1][2];
			for (j = 0; j <= getLineCountPerVector(); j++) {
				temp[j][0] = (int) Math.floor(Math.random() * max + 10);
				temp[j][1] = (int) Math.floor(Math.random() * max + 10);
				//System.out.println("j=" + j + ",row=" + temp[j][0] + ", col=" + temp[j][1]);
			}
			//System.out.println();
			pdf.strokeLines(temp);

			//pdf.printString(temp + "\n");

		}
		pdf.closePDF();

		//pdf.LaunchPDFFile(filename);
	}
}
