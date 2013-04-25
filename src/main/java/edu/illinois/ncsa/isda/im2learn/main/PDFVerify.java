package edu.illinois.ncsa.isda.im2learn.main;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is for verifying processing time prediction accuracy as a function
 * of PDF complexity
 * 
 * 
 * 
 * @author pbajcsy
 * 
 */
public class PDFVerify {

	private int				_graphics_count		= 0;
	private int				_graphics_segments	= 0;
	private int				_graphics_time		= 0;
	private int				_image_count		= 0;
	private int				_image_pixels		= 0;
	private int				_image_time			= 0;
	private int				_text_lines			= 0;
	private int				_text_time			= 0;
	private int				_text_words			= 0;
	private BufferedReader	_br					= null;
	private String			_inFileName			= null;
	private String			_outFileName		= null;

	public enum hardware_dataset {
		ISDA_set1, ISDA_set2, ISDA_set3, IBMT60_set1, IBMT60_set2, IBMT60_set3, Hadoop_set1, Hadoop_set2, Hadoop_set3
	};

	private final hardware_dataset	_hardwareType	= hardware_dataset.ISDA_set3;

/*
 * private double[] _coefLinearText = null; private final double[]
 * _coefLinearImage = null; private final double[] _coefLinearVector = null;
 * private final double[] _coefQuadrText = null; private final double[]
 * _coefQuadrImage = null; private final double[] _coefQuadrVector = null;
 */

	static public void main(String args[]) throws Exception {
		// args[0] is the file name of the csv file with time measurements and PDF complexity variables
		if ((args == null) || (args.length < 2)) {
			System.out.println("Please, specify input and output file");
			return;
		}

		PDFVerify myVerify = new PDFVerify(args[0], args[1]);
		myVerify.Verify();

	}

	public PDFVerify(String InFileName, String OutFileName) throws FileNotFoundException, IOException {

		if ((InFileName == null) || (OutFileName == null)) {
			System.out.println("Please, specify input and output file");
			return;
		}
		_inFileName = InFileName;
		_outFileName = OutFileName;

		_br = new BufferedReader(new FileReader(_inFileName));
		// the header line contains:
		//file	filesize	graphics_count	graphics_count_avg	graphics_segments	graphics_segments_avg	graphics_time	image_count	image_count_avg	image_height	image_pixels	image_pixels_avg	image_time	image_width	open	pages	parse	text_lines	text_lines_avg	text_time	text_words	text_words_avg	total_time	write_time	original

		// skip the header line
		String line = _br.readLine();
		String[] values = line.split("\t");
		// find the column indices for the complexity variables
		int i;
		for (i = 0; i < values.length; i++) {
			System.out.print("values[" + i + "]=" + values[i] + ", ");
			if (values[i].equalsIgnoreCase("text_lines")) {
				_text_lines = i;
			}
			if (values[i].equalsIgnoreCase("text_words")) {
				_text_words = i;
			}
			if (values[i].equalsIgnoreCase("text_time")) {
				_text_time = i;
			}

			if (values[i].equalsIgnoreCase("image_count")) {
				_image_count = i;
			}
			if (values[i].equalsIgnoreCase("image_pixels")) {
				_image_pixels = i;
			}
			if (values[i].equalsIgnoreCase("image_time")) {
				_image_time = i;
			}

			if (values[i].equalsIgnoreCase("graphics_count")) {
				_graphics_count = i;
			}
			if (values[i].equalsIgnoreCase("graphics_segments")) {
				_graphics_segments = i;
			}
			if (values[i].equalsIgnoreCase("graphics_time")) {
				_graphics_time = i;
			}

		}
		System.out.println();
		_br.close();

		// sanity check
		if (_text_lines == 0) {
			System.err.println("missing entry in the csv file text_lines");
		}
		if (_text_words == 0) {
			System.err.println("missing entry in the csv file text_words");
		}
		if (_text_time == 0) {
			System.err.println("missing entry in the csv file text_time");
		}

		if (_image_count == 0) {
			System.err.println("missing entry in the csv file image_count");
		}
		if (_image_pixels == 0) {
			System.err.println("missing entry in the csv file image_pixels");
		}
		if (_image_time == 0) {
			System.err.println("missing entry in the csv file image_time");
		}

		if (_graphics_count == 0) {
			System.err.println("missing entry in the csv file graphics_count");
		}
		if (_graphics_segments == 0) {
			System.err.println("missing entry in the csv file graphics_segments");
		}
		if (_graphics_time == 0) {
			System.err.println("missing entry in the csv file graphics_time");
		}

	}

	public boolean Verify() throws FileNotFoundException, IOException {
		_br = new BufferedReader(new FileReader(_inFileName));
		// the header line contains:
		//file	filesize	graphics_count	graphics_count_avg	graphics_segments	graphics_segments_avg	graphics_time	image_count	image_count_avg	image_height	image_pixels	image_pixels_avg	image_time	image_width	open	pages	parse	text_lines	text_lines_avg	text_time	text_words	text_words_avg	total_time	write_time	original

		// skip the header line
		String line = _br.readLine();
		List<String[]> rows = new ArrayList<String[]>();
		String[] values = null;

		double text_line_count = 0;
		double text_word_count = 0;
		double text_time = 0.0;
		double image_count = 0;
		double image_pixel_count = 0;
		double image_time = 0.0;
		double graphics_count = 0;
		double graphics_segment_count = 0;
		double graphics_time = 0.0;

		boolean no_text = false;
		boolean no_images = false;
		boolean no_graphics = false;

		double temp = 0;
		double timeThresh = 1.0;

		int row_index = 1;
		while ((line = _br.readLine()) != null) {
			values = null;
			values = line.split("\t");
			// check if PDF documents contain text, images and vector graphics
			try {
				text_line_count = Double.parseDouble(values[_text_lines]);
				text_word_count = Double.parseDouble(values[_text_words]);
				text_time = Double.parseDouble(values[_text_time]);
			} catch (NumberFormatException e) {
				//System.out.println("INFO: no text at row=" + row_index);
				no_text = true;
			}
			try {
				image_count = Double.parseDouble(values[_image_count]);
				image_pixel_count = Double.parseDouble(values[_image_pixels]);
				image_time = Double.parseDouble(values[_image_time]);
			} catch (NumberFormatException e) {
				//System.out.println("INFO: no images at row=" + row_index);
				no_images = true;
			}
			try {
				graphics_count = Double.parseDouble(values[_graphics_count]);
				graphics_segment_count = Double.parseDouble(values[_graphics_segments]);
				graphics_time = Double.parseDouble(values[_graphics_time]);
			} catch (NumberFormatException e) {
				//System.out.println("INFO: no graphics at row=" + row_index);
				no_images = true;
			}

			// compute predictions and differences
			if (!no_text) {
				double textProcessTime = predictTextProcessTime(text_line_count, text_word_count, true);
				temp = Math.abs(textProcessTime - text_time);
				if (temp > timeThresh) {
					System.out.println("Text time difference is larger than " + timeThresh + "ms = " + temp);
				}
			}
			if (!no_images) {
				double imageProcessTime = predictImageProcessTime(image_count, image_pixel_count, true);
				temp = Math.abs(imageProcessTime - image_time);
				if (temp > timeThresh) {
					System.out.println("Image time difference is larger than " + timeThresh + "ms = " + temp);
				}
			}
			if (!no_graphics) {
				double graphicsProcessTime = predictVectorProcessTime(graphics_count, graphics_segment_count, true);
				temp = Math.abs(graphicsProcessTime - graphics_time);
				if (temp > timeThresh) {
					System.out.println("Vector Graphics time difference is larger than " + timeThresh + "ms = " + temp);
				}
			}
			row_index++;
			//rows.add(values);

		}
		_br.close();

		// TODO continue with values
		//String filename = rows.get(0)[0];

		return true;
	}

	/////////////////////////////////////////////////////////////////////////////////////////////
	public double predictTextProcessTime(double text_line_count, double text_word_count, boolean isLinearModel) {
		if (isLinearModel) {
			return evalLinearModelText(text_line_count, text_word_count);
		} else {
			return evalQuadraticModelText(text_line_count, text_word_count);
		}
	}

	public double predictImageProcessTime(double image_count, double image_pixel_count, boolean isLinearModel) {
		if (isLinearModel) {
			return evalLinearModelImage(image_count, image_pixel_count);
		} else {
			return evalQuadraticModelImage(image_count, image_pixel_count);
		}
	}

	public double predictVectorProcessTime(double graphics_count, double graphics_segment_count, boolean isLinearModel) {
		if (isLinearModel) {
			return evalLinearModelVector(graphics_count, graphics_segment_count);
		} else {
			return evalQuadraticModelVector(graphics_count, graphics_segment_count);
		}
	}

	/////////////////////////////////////////////////////////////////////////////////////////////
	// these models are for the ISDA 8 core server hardware obtained over the synthetic data set #3
	/////////////////////////////////////////////////////////////////////////////////////////////

	private double evalLinearModelText(double X1, double X2) {
		double temp = -1.0;
		switch (_hardwareType) {
		case ISDA_set3:
			temp = -0.477379 + 0.028300 * X1 + 0.011722 * X2;
			return temp;
		default:
			return -1.0;
		}
		//return temp;
	}

	private double evalQuadraticModelText(double X1, double X2) {
		double temp = -0.815205 + 0.029301 * X1 + 0.011478 * X2;
		return temp;
	}

	private double evalLinearModelImage(double X1, double X2) {
		double temp = -1.648937 + 1.325388 * X1 + 0.000096 * X2;
		return temp;
	}

	private double evalQuadraticModelImage(double X1, double X2) {
		double temp = 1.224633 * X1 + 0.000099 * X2 + 0.000396 * X1 * X1;
		return temp;
	}

	private double evalLinearModelVector(double X1, double X2) {
		double temp = 0.078914 + 0.064082 * X1 + 0.006608 * X2 - 0.000001 * X1 * X2;
		return temp;
	}

	private double evalQuadraticModelVector(double X1, double X2) {
		double temp = -0.029967 + 0.070671 * X1 + 0.006374 * X2 - 0.000028 * X1 * X1;
		return temp;
	}
	/////////////////////////////////////////////////////////////////////////////////////////////

}
