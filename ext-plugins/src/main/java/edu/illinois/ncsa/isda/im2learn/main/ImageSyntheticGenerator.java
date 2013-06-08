package edu.illinois.ncsa.isda.im2learn.main;

import java.io.IOException;

import javax.print.DocFlavor.BYTE_ARRAY;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.im2learn.core.datatype.ImLine;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImPoint;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageException;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.im2learn.core.datatype.Point3DDouble;
import edu.illinois.ncsa.isda.im2learn.core.datatype.SubAreaFloat;
import edu.illinois.ncsa.isda.im2learn.core.io.ImageLoader;
import edu.illinois.ncsa.isda.im2learn.ext.math.GeomOper;
import edu.illinois.ncsa.isda.im2learn.ext.segment.DrawOp;
import edu.illinois.ncsa.isda.im2learn.ext.statistics.pdf.Gaussian1D;
import edu.illinois.ncsa.isda.im2learn.ext.statistics.pdf.Uniform;
import edu.illinois.ncsa.isda.im2learn.ext.texture.GrayLevelCooccurrence;


/**
 * this class was developed for simulating circles with variation in boundary
 * and variation in intensity The simulated images were used for developing a
 * theoretical framework for historical map uncertainty estimation
 * 
 * @author Peter Bajcsy
 * @version 1.0
 */

public class ImageSyntheticGenerator extends Object {

	private final int	_radius		= 100;
	private final int	_numPoints	= 360;
	private final int	_numRows	= 300;
	private final int	_numCols	= 300;

	private static Log	logger		= LogFactory.getLog(ImageSyntheticGenerator.class);

	// input image file name.
	// output is the edge image
	public static void main(String args[]) throws Exception {

		ImageSyntheticGenerator myTest = new ImageSyntheticGenerator();

		int i;
		System.out.println("argument length=" + args.length);
		for (i = 0; i < args.length; i++) {
			System.out.println("args[" + i + "]:" + args[i]);
		}
		if ((args == null) || (args.length < 1)) {
			System.out.println("Please, specify the output name");
			System.out.println("arg = Output_ImageName");
			return;
		}

		String OutFileName;
		Boolean ret = true;

		OutFileName = args[0];
		System.out.println(OutFileName);

		//ret = myTest.GenerateCircle(OutFileName);
		//ret = myTest.GeneratePerturbedCirles(OutFileName);
		//ret = myTest.GenerateCircleWithDashes(OutFileName);
		ret = myTest.GenerateCirlesWithPerturbedIntensities(OutFileName);

		//ImageLoader.writeImage(OutFileName, testObject);
		System.out.println("Test Result = " + ret);

	}

	// constructor
	public ImageSyntheticGenerator() {
	}

	public boolean GeneratePerturbedCirles(String OutFileName) throws ImageException, IOException {
		double var = 11;
		double temp = 1;
		double boundaryLength = 0;
		while (temp < var) {
			// create input images 
			ImageObject testObject = ImageObject.createImage(_numRows, _numCols, 1, ImageObject.TYPE_BYTE);
			testObject.setData(255);
			testObject.setInvalidData(-1);

			//modify the Uniform or Gaussian entries
			//boundaryLength = DrawCircleWithPerturbedRadius("Uniform", testObject, temp);
			boundaryLength = DrawCircleWithPerturbedRadius("Gaussian", testObject, temp);

			String output = new String(OutFileName);
			//output += "-unif" + temp + ".tif";
			output += "-gauss" + temp + ".tif";
			ImageLoader.writeImage(output, testObject);
			//System.out.println("File Name = " + output + " Uniform noise: +/-var = " + temp + " Boundary Length = " + boundaryLength);  
			System.out.println("File Name = " + output + " Gaussian noise: +/-var = " + temp + " Boundary Length = " + boundaryLength);

			temp += 1;
		}

		return true;

	}

	/**
	 * Draw a circle in the middle and reports boundary length
	 * 
	 * @param testObject
	 *            - image to write to
	 * @return boolean
	 */
	public Boolean GenerateCircle(String OutFileName) throws ImageException, IOException {

		// create input images 
		ImageObject testObject = ImageObject.createImage(_numRows, _numCols, 1, ImageObject.TYPE_BYTE);
		testObject.setData(255);
		testObject.setInvalidData(-1);

/*
 * DrawOp draw = new DrawOp(); ImPoint cir = new ImPoint(150, 250);
 * cir.setV(50.0); ret = draw.draw_circleDouble(testObject, cir, 0.0);
 */

		//double radius = 100;
		//int numPoints = 360;
		ImPoint center = new ImPoint(testObject.getNumRows() >> 1, testObject.getNumCols() >> 1);
		ImPoint[] pts = new ImPoint[_numPoints];

		int row, col, k;
		for (k = 0; k < _numPoints; k++) {
			row = (int) (_radius * Math.cos(k * Math.PI / 180) + center.x + 0.5);
			col = (int) (_radius * Math.sin(k * Math.PI / 180) + center.y + 0.5);
			pts[k] = new ImPoint(row, col);
			//testObject.set(row,col, 0, (byte)0);
		}

		double color = 0;
		int thickness = 10;
		DrawOp draw = new DrawOp();
		GeomOper geom = new GeomOper();

		double boundaryLength = 0.0;
		for (k = 0; k < _numPoints - 1; k++) {
			ImLine Linetmp = new ImLine(pts[k], pts[k + 1]);

			draw.plot_lineDouble(testObject, Linetmp, color);
			//draw.draw_line(testObject, color, thickness, Linetmp);
			boundaryLength += geom.distance(pts[k], pts[k + 1]);
		}
		ImLine Linetmp = new ImLine(pts[_numPoints - 1], pts[0]);
		draw.plot_lineDouble(testObject, Linetmp, color);
		//draw.draw_line(testObject, color, thickness, Linetmp);
		boundaryLength += geom.distance(pts[_numPoints - 1], pts[0]);

		String output = new String(OutFileName);
		output += ".tif";
		ImageLoader.writeImage(output, testObject);
		System.out.println("File Name = " + output + " Boundary Length = " + boundaryLength);

		return true;

	}

	public double DrawCircleWithPerturbedRowCol(ImageObject testObject, double noiseMag) {

		double var = noiseMag;
		int row, col, k;
		//double radius = 100;
		//int numPoints = 360;
		ImPoint center = new ImPoint(testObject.getNumRows() >> 1, testObject.getNumCols() >> 1);
		ImPoint[] pts = new ImPoint[_numPoints];
		try {
			//Uniform noise = new Uniform(-var, var );//min and max
			Gaussian1D noise = new Gaussian1D(0.0, var);//mean and stdev

			for (k = 0; k < _numPoints; k++) {
				row = (int) (_radius * Math.cos(k * Math.PI / 180) + center.x + noise.generate() + 0.5);
				col = (int) (_radius * Math.sin(k * Math.PI / 180) + center.y + noise.generate() + 0.5);
				pts[k] = new ImPoint(row, col);
				//testObject.set(row,col, 0, (byte)0);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		double color = 0;
		int thickness = 10;
		DrawOp draw = new DrawOp();
		GeomOper geom = new GeomOper();

		double boundaryLength = 0.0;
		for (k = 0; k < _numPoints - 1; k++) {
			ImLine Linetmp = new ImLine(pts[k], pts[k + 1]);

			draw.plot_lineDouble(testObject, Linetmp, color);
			//draw.draw_line(testObject, color, thickness, Linetmp);
			boundaryLength += geom.distance(pts[k], pts[k + 1]);
		}
		ImLine Linetmp = new ImLine(pts[_numPoints - 1], pts[0]);
		draw.plot_lineDouble(testObject, Linetmp, color);
		//draw.draw_line(testObject, color, thickness, Linetmp);
		boundaryLength += geom.distance(pts[_numPoints - 1], pts[0]);

		return boundaryLength;

	}

	/**
	 * This method generates an image with a circle in the middle and the
	 * boundary point perturbed by either Uniform ro Gaussian noise
	 * 
	 * @param noiseType
	 *            - Uniform or Gaussian
	 * @param testObject
	 *            - image to output the circle to
	 * @param noiseMag
	 *            - the +/- values for Uniform noise or stdev for Gaussian noise
	 * @return
	 */
	public double DrawCircleWithPerturbedRadius(String noiseType, ImageObject testObject, double noiseMag) {

		double var = noiseMag;
		double temp = 0.0;
		int row, col, k;
		//double radius = 100;
		//int numPoints = 360;
		ImPoint center = new ImPoint(testObject.getNumRows() >> 1, testObject.getNumCols() >> 1);
		ImPoint[] pts = new ImPoint[_numPoints];
		try {
			Uniform noiseU = new Uniform(-var, var);//min and max
			Gaussian1D noiseG = new Gaussian1D(0.0, var);//mean and stdev

			for (k = 0; k < _numPoints; k++) {
				if (noiseType.equalsIgnoreCase("Uniform")) {
					temp = noiseU.generate();
				} else {
					temp = noiseG.generate();
				}
				//System.out.print("noise = " + temp);
				row = (int) ((_radius + temp) * Math.cos(k * Math.PI / 180) + center.x + 0.5);
				col = (int) ((_radius + temp) * Math.sin(k * Math.PI / 180) + center.y + 0.5);
				pts[k] = new ImPoint(row, col);
				//testObject.set(row,col, 0, (byte)0);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//draw the line between two adjacent points and compute the boundary length
		double color = 0;
		int thickness = 10;
		DrawOp draw = new DrawOp();
		GeomOper geom = new GeomOper();

		double boundaryLength = 0.0;
		for (k = 0; k < _numPoints - 1; k++) {
			ImLine Linetmp = new ImLine(pts[k], pts[k + 1]);

			draw.plot_lineDouble(testObject, Linetmp, color);
			//draw.draw_line(testObject, color, thickness, Linetmp);
			boundaryLength += geom.distance(pts[k], pts[k + 1]);
		}
		ImLine Linetmp = new ImLine(pts[_numPoints - 1], pts[0]);
		draw.plot_lineDouble(testObject, Linetmp, color);
		//draw.draw_line(testObject, color, thickness, Linetmp);
		boundaryLength += geom.distance(pts[_numPoints - 1], pts[0]);

		return boundaryLength;

	}

	public Boolean GenerateCircleWithDashes(String OutFileName) throws ImageException, IOException {

		ImPoint center = new ImPoint(_numRows >> 1, _numCols >> 1);
		ImPoint[] pts = new ImPoint[_numPoints];

		int row, col, k, i;
		for (k = 0; k < _numPoints; k++) {
			row = (int) (_radius * Math.cos(k * Math.PI / 180) + center.x + 0.5);
			col = (int) (_radius * Math.sin(k * Math.PI / 180) + center.y + 0.5);
			pts[k] = new ImPoint(row, col);
			//testObject.set(row,col, 0, (byte)0);
		}

		double color = 0;
		int thickness = 10;
		DrawOp draw = new DrawOp();
		GeomOper geom = new GeomOper();
		ImLine dash = new ImLine();
		ImPoint dashPts2 = new ImPoint(pts[0]);
		dash.setPts2(dashPts2);
		int dashLength = 21;
		int dashDensity = 11;
		int dashLindex, dashDindex;
		for (dashLindex = 5; dashLindex < dashLength; dashLindex += 5) {
			for (dashDindex = 2; dashDindex < dashDensity; dashDindex += 1) {
				double boundaryLength = 0.0;
				// create input images 
				ImageObject testObject = ImageObject.createImage(_numRows, _numCols, 1, ImageObject.TYPE_BYTE);
				testObject.setData(255);
				testObject.setInvalidData(-1);

				i = 0;
				for (k = 0; k < _numPoints - 1; k++) {
					ImLine Linetmp = new ImLine(pts[k], pts[k + 1]);

					draw.plot_lineDouble(testObject, Linetmp, color);
					//draw.draw_line(testObject, color, thickness, Linetmp);
					boundaryLength += geom.distance(pts[k], pts[k + 1]);
					if (Math.abs(dash.getPts2().x - pts[k].x) >= dashDindex) {
						dash.setPts1(pts[k]);
						dash.setSlope(0.0);
						dashPts2.x = pts[k].x;
						if (pts[k].y < testObject.getNumCols() >> 1 && pts[k + 1].y < testObject.getNumCols() >> 1) {
							dashPts2.y = pts[k].y + dashLindex;
						} else {
							dashPts2.y = pts[k].y - dashLindex;
						}
						dash.setPts2(dashPts2);
						draw.plot_lineDouble(testObject, dash, color);
						i = 0;
					}
					i++;
				}
				ImLine Linetmp = new ImLine(pts[_numPoints - 1], pts[0]);
				draw.plot_lineDouble(testObject, Linetmp, color);
				//draw.draw_line(testObject, color, thickness, Linetmp);
				boundaryLength += geom.distance(pts[_numPoints - 1], pts[0]);

				String output = new String(OutFileName);
				output += "-dashL" + dashLindex + "-dashD" + dashDindex + ".tif";
				ImageLoader.writeImage(output, testObject);
				System.out.println("File Name = " + output + " Boundary Length = " + boundaryLength);
			}
		}

		return true;

	}

	public boolean GenerateCirlesWithPerturbedIntensities(String OutFileName) throws Exception {

		double var, lineThickness;
		Boolean ret = true;
		for (lineThickness = 0.5; lineThickness < 21; lineThickness += 5) {
			for (var = 10; var < 50; var += 10) {
				ret &= DrawCirleWithPerturbedIntensities(OutFileName, "Uniform", var, lineThickness);
				//ret &= DrawCirleWithPerturbedIntensities(OutFileName, "Gaussian", var, lineThickness);
			}
		}
		return ret;
	}

	/**
	 * This method draws a circle with thickness equal to lineThickness
	 * The intensities of foreground pixels are perturbed around max-var and border pixels
	 * around min+var
	 *   
	 * @param OutFileName output file name
	 * @param NoiseType Uniform or Gaussian
	 * @param var - range or stdev
	 * @param lineThickness - thickness of circle = border pixels
	 * @return
	 * @throws Exception
	 */
	public boolean DrawCirleWithPerturbedIntensities(String OutFileName, String NoiseType, double var, double lineThickness) throws Exception {

		Boolean ret = true;
		// create input images 
		ImageObject testObject = ImageObject.createImage(_numRows, _numCols, 1, ImageObject.TYPE_BYTE);
		testObject.setData(255);
		testObject.setInvalidData(-1);

		DrawOp draw = new DrawOp();
		ImPoint cir = new ImPoint((_numRows >> 1), (_numCols >> 1));

		for (double r = _radius; r < _radius + lineThickness; r += 0.5) {
			cir.setV(r);
			ret = draw.draw_circleDouble(testObject, cir, 0.0);
		}

		Uniform noiseU = new Uniform(-var, var);//min and max
		Gaussian1D noiseG = new Gaussian1D(0.0, var);//mean and stdev

		double newVal = 0.0;
		int counter = 0;
		for (int i = 0; i < testObject.getSize(); i++) {
			if (testObject.getDouble(i) != 0) {
				//perturb foreground intensity
				if (NoiseType.equalsIgnoreCase("Uniform")) {
					newVal = (testObject.getMaxType() - var) + noiseU.generate() + 0.5;
				} else {
					newVal = (testObject.getMaxType() - var) + noiseG.generate() + 0.5;
					counter = 0;
					while (newVal >= 256 && counter < 20) {
						newVal = (testObject.getMaxType() - var) + noiseG.generate() + 0.5;
						counter++;
					}
					if (newVal >= 256) {
						newVal = (testObject.getMaxType() - var);
					}
				}
				testObject.set(i, (byte) newVal);
			} else {
				//perturb background intensity
				if (NoiseType.equalsIgnoreCase("Uniform")) {
					newVal = (testObject.getMinType() + var) + noiseU.generate() + 0.5;
				} else {
					newVal = (testObject.getMinType() + var) + noiseG.generate() + 0.5;
					counter = 0;
					while (newVal < 0 && counter < 20) {
						newVal = (testObject.getMinType() + var) + noiseG.generate() + 0.5;
						counter++;
					}
					if (newVal < 0) {
						newVal = (testObject.getMinType() + var);
					}
				}
				testObject.set(i, (byte) newVal);

			}
		}

		// save generated results 
		String output = new String(OutFileName);
		if (NoiseType.equalsIgnoreCase("Uniform")) {
			output += "-unif" + var + "-line" + lineThickness + ".tif";
		} else {
			output += "-gauss" + var + "-line" + lineThickness + ".tif";
		}
		ImageLoader.writeImage(output, testObject);
		//System.out.println("File Name = " + output + " Boundary Length = " + boundaryLength);

		// save GLCM images
		GrayLevelCooccurrence myTest = new GrayLevelCooccurrence();
		myTest.setKernelNumCols(100);
		myTest.setKernelNumRows(100);
		myTest.setNumIntensityBins(255);
		myTest.computeGLCM(testObject, (testObject.getNumRows() >> 1), (testObject.getNumCols() >> 1));

		String output1 = new String(OutFileName);
		String imageGLCMName = new String(OutFileName);
		if (NoiseType.equalsIgnoreCase("Uniform")) {
			output1 += "-unif" + var + "-line" + lineThickness + ".csv";
			imageGLCMName += "-unif" + var + "-line" + lineThickness + "GLCM.tif";
		} else {
			output1 += "-gauss" + var + "-line" + lineThickness + ".csv";
			imageGLCMName += "-gauss" + var + "-line" + lineThickness + "GLCM.tif";
		}
		myTest.printGLCM(output1);
		myTest.saveImageGLCM(imageGLCMName);

		return true;

	}
}