package edu.illinois.ncsa.isda.imagetools.ext.conversion;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.imagetools.core.io.ImageLoader;
import edu.illinois.ncsa.isda.imagetools.ext.segment.ColorModels;
import edu.illinois.ncsa.isda.imagetools.ext.statistics.Histogram;
import edu.illinois.ncsa.isda.imagetools.ext.statistics.ImStats;

/**
 * 
 * @author Peter Bajcsy
 * @version 1.0
 */

public class Test_ColorMapping extends Object {

	public boolean	_testPassed	= true;

	// input image file name.
	// output is the edge image
	public static void main(String args[]) throws Exception {

		Test_ColorMapping myTest = new Test_ColorMapping();

		System.out.println("argument length=" + args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.println("args[" + i + "]:" + args[i]);
		}
		if ((args == null) || (args.length < 2)) {
			System.out.println("Please, specify the image to analyze and to save output to");
			System.out.println("arg = Model_imageName, Convert_imageName, Output_ImageName, Model_maskName, Convert_maskName");
			return;
		}

		String InFileName1, InFileName2, OutFileName;
		String InFileName3 = null, InFileName4 = null;

		InFileName1 = args[0];
		System.out.println(InFileName1);

		InFileName2 = args[1];
		System.out.println(InFileName2);

		OutFileName = args[2];
		System.out.println(OutFileName);

		if (args.length > 3) {
			InFileName3 = args[3];
			System.out.println(InFileName3);

			InFileName4 = args[4];
			System.out.println(InFileName4);
		}
		// read the input images 
		ImageObject testObject1 = null;
		testObject1 = ImageLoader.readImage(InFileName1);
		System.out.println("Info: this is the input image filename=" + InFileName1);
		testObject1.toString();
		////////////////

		ImageObject testObject2 = null;
		testObject2 = ImageLoader.readImage(InFileName2);
		System.out.println("Info: this is the input image filename=" + InFileName2);
		testObject2.toString();
		////////////////

		// read the input masks 
		ImageObject maskObject1 = null;
		ImageObject maskObject2 = null;
		if (args.length > 3) {
			maskObject1 = ImageLoader.readImage(InFileName3);
			System.out.println("Info: this is the input image filename=" + InFileName3);
			maskObject1.toString();
			////////////////

			maskObject2 = ImageLoader.readImage(InFileName4);
			System.out.println("Info: this is the input image filename=" + InFileName4);
			maskObject2.toString();
			////////////////
		}

		// perform calculations
		ImageObject retObject = null;
		boolean ret = true;

		if (args.length > 3) {
			// re-map image2 to match image 1 without masks
			retObject = myTest.StatsColorMappingMask(testObject1, maskObject1, testObject2, maskObject2);
		} else {
			// re-map image2 to match image 1 without masks
			retObject = myTest.StatsColorMapping(testObject1, testObject2);
		}
		if (retObject == null) {
			System.out.println("ERROR: could not create a new color re-mapped image ");
			ret = false;
		}
		ImageLoader.writeImage(OutFileName, retObject);

		System.out.println("Test Result = " + ret);

	}

	// constructor
	public Test_ColorMapping() {
	}

	/*
	 * This is just a test with histogram analysis
	 */
	public ImageObject HistColorMapping(ImageObject model_im1, ImageObject convert_im2) throws Exception {

		//sanity checks
		if ((model_im1 == null) || (convert_im2 == null)) {
			System.err.println("input images do not exist");
			return null;
		}
		if ((model_im1.getNumBands() != 3) || (convert_im2.getNumBands() != 3)) {
			System.err.println("HistColorMapping is designed for 3-band RGB images");
			return null;
		}

		// convert RGB to HSV
		ColorModels col = new ColorModels();
		col.convertRGB2HSV(model_im1);
		ImageObject hsvIm1 = col.getConvertedIm();

		col.convertRGB2HSV(convert_im2);
		ImageObject hsvIm2 = col.getConvertedIm();

		// compute histograms and statistics
		Histogram hist1 = new Histogram();
		Histogram hist2 = new Histogram();

		double[] meanIm1 = new double[hsvIm1.getNumBands()];
		double[] stdevIm1 = new double[hsvIm1.getNumBands()];
		double[] meanIm2 = new double[hsvIm1.getNumBands()];
		double[] stdevIm2 = new double[hsvIm1.getNumBands()];

		for (int band = 0; band < hsvIm1.getNumBands(); band++) {
			hist1.SetMinDataVal(hsvIm1.getMin(band));
			hist1.SetMaxDataVal(hsvIm1.getMax(band));
			hist1.SetNumBins(1000);

			hist1.Hist(hsvIm1, band);
			hist1.Stats();
			meanIm1[band] = hist1.GetMean() * hist1.GetWideBins() + hist1.GetMinDataVal();
			stdevIm1[band] = hist1.GetSDev() * hist1.GetWideBins();

			//printHistParamaters(hist1, band);

			hist2.SetMinDataVal(hsvIm2.getMin(band));
			hist2.SetMaxDataVal(hsvIm2.getMax(band));
			hist2.SetNumBins(1000);

			hist2.Hist(hsvIm2, band);
			hist2.Stats();
			meanIm2[band] = hist2.GetMean() * hist2.GetWideBins() + hist2.GetMinDataVal();
			stdevIm2[band] = hist2.GetSDev() * hist2.GetWideBins();

			//printHistParamaters(hist2, band);
			System.out.println("TEST: band=" + band + " meanIm1 = " + meanIm1[band] + " meanIm2 = " + meanIm2[band] + " stdevIm1 =" + stdevIm1[band] + "stdevIm2 =" + stdevIm2[band]);

			// adjust the stdev since we will divide by this value when re-scaling
			if ((band != 0) && (stdevIm2[band] < 0.00001)) {
				System.out.println("INFO: band=" + band + " modified stdevIm2 =" + stdevIm2[band] + " to 1.0");
				stdevIm2[band] = 1.0;

			}

		}

		// for testing purposes to see the pixels modified 
		ImageObject testObject = (ImageObject) hsvIm2.clone();

		// scale hsvIm2
		double val;
		for (int idx = 0; idx < hsvIm2.getSize(); idx += 3) {
			// if the hue values are similar then re-scale
			/// this could be +/- 30 degrees PB
			//if (Math.abs(hsvIm2.getDouble(idx) - meanIm1[0]) <= stdevIm1[0]) {

			// hue
			testObject.set(idx, hsvIm2.getDouble(idx));

			// re-scale saturation
			val = (hsvIm2.getDouble(idx + 1) - meanIm2[1]) * stdevIm1[1] / stdevIm2[1] + meanIm1[1];
			// RGB are in signed byte, output H is in [0,360] (invalid = -1), S in [0,1]
			// and V in [0,256] (unsigned byte)

			// saturation difference
			testObject.set(idx + 1, hsvIm2.getDouble(idx + 1) - val);

			hsvIm2.set(idx + 1, val);
			// re-scale value
			val = (hsvIm2.getDouble(idx + 2) - meanIm2[2]) * stdevIm1[2] / stdevIm2[2] + meanIm1[2];

			// value difference
			testObject.set(idx + 2, hsvIm2.getDouble(idx + 2) - val);

			hsvIm2.set(idx + 2, val);

/*
 * } else { testObject.set(idx, 500.0); testObject.set(idx + 1, 500.0);
 * testObject.set(idx + 2, 500.0); }
 */
		}

		// test 
		ImageLoader.writeImage("C:/PeterB/Presentations/Proposals/Gladson/image03-10-2008/test.tif", testObject);
		// verify new statistics
		for (int band = 0; band < hsvIm2.getNumBands(); band++) {

			hist2.SetMinDataVal(hsvIm2.getMin(band));
			hist2.SetMaxDataVal(hsvIm2.getMax(band));
			hist2.SetNumBins(1000);

			hist2.Hist(hsvIm2, band);
			hist2.Stats();
			val = hist2.GetMean() * hist2.GetWideBins() + hist2.GetMinDataVal();

			//printHistParamaters(hist2, band);
			System.out.println("INFO: band=" + band + " new meanIm2 = " + val + " Old meanIm2 = " + meanIm2[band]);
			val = hist2.GetSDev() * hist2.GetWideBins();
			System.out.println("INFO new stdev = " + val + "  old stdevIm2 =" + stdevIm2[band]);

		}

		// convert to RGB and create the return object
		col.convertHSV2RGB(hsvIm2);
		ImageObject retObject = col.getConvertedIm();
		retObject.setProperties(convert_im2.getProperties());

		return retObject;

	}

	private void printHistParamaters(Histogram hist, int band) {
		String output = "Contrast  = " + hist.GetContrast() + "\n" + "Count     = " + hist.GetCount() + "\n" + "Energy    = " + hist.GetEnergy() + "\n" + "Entropy   = " + hist.GetEntropy() + "\n"
				+ "Kurtosis  = " + hist.GetKurtosis() + "\n" + "Lower %   = " + hist.GetLowerPercentile() + "\n" + "Upper %   = " + hist.GetUpperPercentile() + "\n" + "Skew      = " + hist.GetSkew()
				+ "\n" + "Mean      = " + hist.GetMean() + "\n" + "Median    = " + hist.GetMedian() + "\n" + "S Dev     = " + hist.GetSDev() + "\n" + "MinDataVal     = " + hist.GetMinDataVal() + "\n"
				+ "MaxDataVal     = " + hist.GetMaxDataVal() + "\n" + "NumBins     = " + hist.GetNumBins() + "\n" + "BinWidth     = " + hist.GetWideBins();

		System.out.println("hsvIm1 parameters: band Idx=" + band + "\n" + output);

	}

	/**
	 * This method is using HSV space for re-mapping the color of a convert_im2
	 * image based on the statistics of the model_im1 image The color re-mapping
	 * applies only to saturation and value bands of the HSV image There is no
	 * mask involved right now.
	 * 
	 * 
	 * @param model_im1
	 * @param convert_im2
	 * @return
	 * @throws Exception
	 */
	public ImageObject StatsColorMapping(ImageObject model_im1, ImageObject convert_im2) throws Exception {

		//sanity checks
		if ((model_im1 == null) || (convert_im2 == null)) {
			System.err.println("input images do not exist");
			return null;
		}
		if ((model_im1.getNumBands() != 3) || (convert_im2.getNumBands() != 3)) {
			System.err.println("HistColorMapping is designed for 3-band RGB images");
			return null;
		}

		// convert RGB to HSV
		ColorModels col = new ColorModels();
		col.convertRGB2HSV(model_im1);
		ImageObject hsvIm1 = col.getConvertedIm();

		col.convertRGB2HSV(convert_im2);
		ImageObject hsvIm2 = col.getConvertedIm();

		// compute histograms and statistics
		ImStats stat1 = new ImStats();
		ImStats stat2 = new ImStats();

		double[] meanIm1 = null;
		double[] stdevIm1 = null;
		double[] meanIm2 = null;
		double[] stdevIm2 = null;

/*
 * // only one label right now ImageObject maskIm1 =
 * ImageObject.createImage(hsvIm1.getNumRows(), hsvIm1.getNumCols(), 1,
 * ImageObject.TYPE_BYTE); ImageObject maskIm2 =
 * ImageObject.createImage(hsvIm2.getNumRows(), hsvIm2.getNumCols(), 1,
 * ImageObject.TYPE_BYTE); maskIm1.setData(1); maskIm2.setData(1);
 */

		//if (!stat1.MeanStdevTable(hsvIm1, maskIm1)) {
		if (!stat1.MeanStdevVal(hsvIm1)) {
			System.err.println("HistColorMapping - could not compute mean val1");
			return null;
		}
		meanIm1 = stat1.GetMeanVal();
		stdevIm1 = stat1.GetStdevVal();
		if ((meanIm1 == null) || (stdevIm1 == null)) {
			System.err.println("HistColorMapping - did not return any mean val1");
			return null;
		}
/*
 * ImageObject minIm1 = null; ImageObject maxIm1 = null;
 * 
 * if (!stat1.MinMaxVal(hsvIm1)) { System.err.println("HistColorMapping - could
 * not compute mean val1"); return null; } minIm1 = stat1.GetMinVal(); maxIm1 =
 * stat1.GetMaxVal();
 */

		double[] minIm1 = new double[hsvIm1.getNumBands()];
		double[] maxIm1 = new double[hsvIm1.getNumBands()];
		minIm1[0] = 0.0;
		minIm1[1] = 0.0;
		minIm1[2] = 0.0;
		maxIm1[0] = 360.0;
		maxIm1[1] = 1.0;
		maxIm1[2] = 255.0;

		if ((minIm1 == null) || (maxIm1 == null)) {
			System.err.println("HistColorMapping - did not return any min/max values for image1");
			return null;
		}

		//if (!stat2.MeanStdevTable(hsvIm2, maskIm2)) {
		if (!stat2.MeanStdevVal(hsvIm2)) {
			System.err.println("HistColorMapping - could not compute mean val2");
			return null;
		}
		meanIm2 = stat2.GetMeanVal();
		stdevIm2 = stat2.GetStdevVal();
		if ((meanIm2 == null) || (stdevIm2 == null)) {
			System.err.println("HistColorMapping - did not return any mean val2");
			return null;

		}
		System.out.println("INFO: meanIm1 = " + meanIm1[0] + ", " + meanIm1[1] + ", " + meanIm1[2]);
		System.out.println("INFO: meanIm2 = " + meanIm2[0] + ", " + meanIm2[1] + ", " + meanIm2[2]);
		System.out.println("INFO: stdevIm1 = " + stdevIm1[0] + ", " + stdevIm1[1] + ", " + stdevIm1[2]);
		System.out.println("INFO: stdevIm2 = " + stdevIm2[0] + ", " + stdevIm2[1] + ", " + stdevIm2[2]);

		// adjust the stdev since we will divide by this value when re-scaling
		for (int band = 0; band < meanIm1.length; band++) {
			if ((band != 0) && (stdevIm2[band] < 0.00001)) {
				System.out.println("INFO: band=" + band + " modified stdevIm2 =" + stdevIm2[band] + " to 1.0");
				stdevIm2[band] = 1.0;
			}
		}

		// for testing purposes to see the pixels modified 
		ImageObject testObject = (ImageObject) hsvIm2.clone();

		// scale hsvIm2
		double val;
		for (int idx = 0; idx < hsvIm2.getSize(); idx += 3) {
			// if the hue values are similar then re-scale
			/// this could be +/- 30 degrees PB
			//if (Math.abs(hsvIm2.getDouble(idx) - meanIm1[0]) <= stdevIm1[0]) {

			// hue
			testObject.set(idx, hsvIm2.getDouble(idx));

			// re-scale saturation
			val = (hsvIm2.getDouble(idx + 1) - meanIm2[1]) * stdevIm1[1] / stdevIm2[1] + meanIm1[1];
			// RGB are in signed byte, output H is in [0,360] (invalid = -1), S in [0,1]
			// and V in [0,256] (unsigned byte)
/*
 * if (val < minIm1.getDouble(idx + 1)) { val = minIm1.getDouble(idx + 1); } if
 * (val > maxIm1.getDouble(idx + 1)) { val = maxIm1.getDouble(idx + 1); }
 */
			if (val < minIm1[1]) {
				val = minIm1[1];
			}
			if (val > maxIm1[1]) {
				val = maxIm1[1];
			}
			// saturation difference
			testObject.set(idx + 1, hsvIm2.getDouble(idx + 1) - val);
			hsvIm2.set(idx + 1, val);

			// re-scale value
			val = (hsvIm2.getDouble(idx + 2) - meanIm2[2]) * stdevIm1[2] / stdevIm2[2] + meanIm1[2];
			// RGB are in signed byte, output H is in [0,360] (invalid = -1), S in [0,1]
			// and V in [0,256] (unsigned byte)
/*
 * if (val < minIm1.getDouble(idx + 2)) { val = minIm1.getDouble(idx + 2); } if
 * (val > maxIm1.getDouble(idx + 2)) { val = maxIm1.getDouble(idx + 2); }
 */
			if (val < minIm1[2]) {
				val = minIm1[2];
			}
			if (val > maxIm1[2]) {
				val = maxIm1[2];
			}
			// value difference
			testObject.set(idx + 2, hsvIm2.getDouble(idx + 2) - val);

			hsvIm2.set(idx + 2, val);

/*
 * } else { testObject.set(idx, 500.0); testObject.set(idx + 1, 500.0);
 * testObject.set(idx + 2, 500.0); }
 */
		}

		// test 
		ImageLoader.writeImage("C:/PeterB/Presentations/Proposals/Gladson/image03-10-2008/test.tif", testObject);
		// verify new statistics
		double[] tempMean = null;
		double[] tempStdev = null;
		if (!stat2.MeanStdevVal(hsvIm2)) {
			System.err.println("HistColorMapping - could not verify mean val2");
			return null;
		}

		tempMean = stat2.GetMeanVal();
		tempStdev = stat2.GetStdevVal();

		for (int band = 0; band < hsvIm2.getNumBands(); band++) {
			System.out.println("INFO: band=" + band + " new meanIm2 = " + tempMean[band] + " Old meanIm2 = " + meanIm2[band]);
			System.out.println("INFO new stdev = " + tempStdev[band] + "  old stdevIm2 =" + stdevIm2[band]);
		}

		// convert to RGB and create the return object
		col.convertHSV2RGB(hsvIm2);
		ImageObject retObject = col.getConvertedIm();
		retObject.setProperties(convert_im2.getProperties());

		return retObject;

	}

	/**
	 * This method is using HSV space for re-mapping the color of a convert_im2
	 * image based on the statistics of the model_im1 image The color re-mapping
	 * applies only to saturation and value bands of the HSV image There is no
	 * mask involved right now.
	 * 
	 * This method is designed for working with masks
	 * 
	 * @param model_im1
	 * @param model_mask
	 * @param convert_im2
	 * @param convert_mask
	 * @return
	 * @throws Exception
	 */
	public ImageObject StatsColorMappingMask(ImageObject model_im1, ImageObject model_mask, ImageObject convert_im2, ImageObject convert_mask) throws Exception {

		//sanity checks
		if ((model_im1 == null) || (convert_im2 == null)) {
			System.err.println("input images do not exist");
			return null;
		}
		if ((model_mask == null) || (convert_mask == null)) {
			System.err.println("input masks do not exist");
			return null;
		}
		if ((model_im1.getNumBands() != 3) || (convert_im2.getNumBands() != 3)) {
			System.err.println("HistColorMapping is designed for 3-band RGB images");
			return null;
		}
		// it is assumed that the foreground value is equal to zero

		// convert RGB to HSV
		ColorModels col = new ColorModels();
		col.convertRGB2HSV(model_im1);
		ImageObject hsvIm1 = col.getConvertedIm();

		col.convertRGB2HSV(convert_im2);
		ImageObject hsvIm2 = col.getConvertedIm();

		// compute histograms and statistics
		ImStats stat1 = new ImStats();
		ImStats stat2 = new ImStats();

		float[] meanIm1 = null;
		float[] stdevIm1 = null;
		float[] meanIm2 = null;
		float[] stdevIm2 = null;

/*
 * // only one label right now ImageObject maskIm1 =
 * ImageObject.createImage(hsvIm1.getNumRows(), hsvIm1.getNumCols(), 1,
 * ImageObject.TYPE_BYTE); ImageObject maskIm2 =
 * ImageObject.createImage(hsvIm2.getNumRows(), hsvIm2.getNumCols(), 1,
 * ImageObject.TYPE_BYTE); maskIm1.setData(1); maskIm2.setData(1);
 */

		if (!stat1.MeanStdevTable(hsvIm1, model_mask)) {
			System.err.println("HistColorMapping - could not compute mean val1");
			return null;
		}
		meanIm1 = stat1.GetMeanTable();
		stdevIm1 = stat1.GetStdevTable();
		if ((meanIm1 == null) || (stdevIm1 == null)) {
			System.err.println("HistColorMapping - did not return any mean val1");
			return null;
		}
/*
 * ImageObject minIm1 = null; ImageObject maxIm1 = null;
 * 
 * if (!stat1.MinMaxVal(hsvIm1)) { System.err.println("HistColorMapping - could
 * not compute mean val1"); return null; } minIm1 = stat1.GetMinVal(); maxIm1 =
 * stat1.GetMaxVal();
 */

		double[] minIm1 = new double[hsvIm1.getNumBands()];
		double[] maxIm1 = new double[hsvIm1.getNumBands()];
		minIm1[0] = 0.0;
		minIm1[1] = 0.0;
		minIm1[2] = 0.0;
		maxIm1[0] = 360.0;
		maxIm1[1] = 1.0;
		maxIm1[2] = 255.0;

		if ((minIm1 == null) || (maxIm1 == null)) {
			System.err.println("HistColorMapping - did not return any min/max values for image1");
			return null;
		}

		if (!stat2.MeanStdevTable(hsvIm2, convert_mask)) {
			System.err.println("HistColorMapping - could not compute mean val2");
			return null;
		}
		meanIm2 = stat2.GetMeanTable();
		stdevIm2 = stat2.GetStdevTable();
		if ((meanIm2 == null) || (stdevIm2 == null)) {
			System.err.println("HistColorMapping - did not return any mean val2");
			return null;

		}
		// the tables are structured as _numLabelsInLUT * image.getNumBands()
		System.out.println("INFO: number of labels in the model_mask=" + stat1.GetCountTable().length);
		System.out.println("INFO: number of labels in the convert_mask=" + stat2.GetCountTable().length);
		System.out.println("INFO: length of meanIm1 =" + meanIm1.length);

		System.out.println("INFO: meanIm1 = " + meanIm1[0] + ", " + meanIm1[1] + ", " + meanIm1[2]);
		System.out.println("INFO: meanIm2 = " + meanIm2[0] + ", " + meanIm2[1] + ", " + meanIm2[2]);
		System.out.println("INFO: stdevIm1 = " + stdevIm1[0] + ", " + stdevIm1[1] + ", " + stdevIm1[2]);
		System.out.println("INFO: stdevIm2 = " + stdevIm2[0] + ", " + stdevIm2[1] + ", " + stdevIm2[2]);

		// adjust the stdev since we will divide by this value when re-scaling
		for (int band = 0; band < meanIm1.length; band++) {
			if ((band != 0) && (stdevIm2[band] < 0.00001)) {
				System.out.println("INFO: band=" + band + " modified stdevIm2 =" + stdevIm2[band] + " to 1.0");
				stdevIm2[band] = 1.0f;
			}
		}

		// for testing purposes to see the pixels modified 
		ImageObject testObject = (ImageObject) hsvIm2.clone();

		// scale hsvIm2
		double val;
		int idxIm, idxMask;
		for (idxIm = 0, idxMask = 0; idxIm < hsvIm2.getSize(); idxMask++, idxIm += 3) {
			// modify only over the mask
			if (convert_mask.getByte(idxMask) == 0) {

				// do not change the hue values (or set it to the mean ?)
				// hue
				testObject.set(idxIm, hsvIm2.getDouble(idxIm));

				hsvIm2.setFloat(idxIm, meanIm1[0]);

				// re-scale saturation
				val = (hsvIm2.getDouble(idxIm + 1) - meanIm2[1]) * stdevIm1[1] / stdevIm2[1] + meanIm1[1];
				// RGB are in signed byte, output H is in [0,360] (invalid = -1), S in [0,1]
				// and V in [0,256] (unsigned byte)

				if (val < minIm1[1]) {
					val = minIm1[1];
				}
				if (val > maxIm1[1]) {
					val = maxIm1[1];
				}
				// saturation difference
				testObject.set(idxIm + 1, hsvIm2.getDouble(idxIm + 1) - val);
				hsvIm2.set(idxIm + 1, val);

				// re-scale value
				val = (hsvIm2.getDouble(idxIm + 2) - meanIm2[2]) * stdevIm1[2] / stdevIm2[2] + meanIm1[2];
				// RGB are in signed byte, output H is in [0,360] (invalid = -1), S in [0,1]
				// and V in [0,256] (unsigned byte)
				if (val < minIm1[2]) {
					val = minIm1[2];
				}
				if (val > maxIm1[2]) {
					val = maxIm1[2];
				}
				// value difference
				testObject.set(idxIm + 2, hsvIm2.getDouble(idxIm + 2) - val);

				hsvIm2.set(idxIm + 2, val);
			} else {
				testObject.set(idxIm, 500.0);
				testObject.set(idxIm + 1, 500.0);
				testObject.set(idxIm + 2, 500.0);
			}
		}

		// test 
		//ImageLoader.writeImage("C:/PeterB/Presentations/Proposals/Gladson/image03-10-2008/testMask.tif", testObject);

		// convert to RGB and create the return object
		col.convertHSV2RGB(hsvIm2);
		ImageObject retObject = col.getConvertedIm();
		retObject.setProperties(convert_im2.getProperties());

		return retObject;

	}
}