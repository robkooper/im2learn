package edu.illinois.ncsa.isda.im2learn.ext.segment;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.im2learn.core.datatype.ImLine;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImPoint;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.im2learn.core.datatype.Point3DDouble;
import edu.illinois.ncsa.isda.im2learn.core.datatype.SubAreaFloat;
import edu.illinois.ncsa.isda.im2learn.core.io.ImageLoader;
import edu.illinois.ncsa.isda.im2learn.ext.math.GeomOper;
import edu.illinois.ncsa.isda.im2learn.ext.segment.DrawOp;
import edu.illinois.ncsa.isda.im2learn.ext.segment.EdgeLine;
import edu.illinois.ncsa.isda.im2learn.ext.segment.EdgeLineObject;
import edu.illinois.ncsa.isda.im2learn.ext.segment.EdgeLineRes;


/**
 * 
 * @author Peter Bajcsy
 * @version 1.0
 */

public class Test_EdgeLine extends Object {

	public boolean		_testPassed			= true;
	EdgeLine			_myEdgeLine			= new EdgeLine();
	EdgeLine			_myEdgeLineGap		= new EdgeLine();
	private final int	_laserLineThickness	= 10;
	private final int	_numOfGapsPerLine	= 1;										//6;
	private final int	_minGapLength		= 30;

	private static Log	logger				= LogFactory.getLog(Test_EdgeLine.class);

	// input image file name.
	// output is the edge image
	public static void main(String args[]) throws Exception {

		Test_EdgeLine myTest = new Test_EdgeLine();

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
		ImageObject testObject1 = null;
		testObject1 = ImageLoader.readImage(InFileName1);
		System.out.println("Info: this is the input image filename=" + InFileName1);
		testObject1.toString();
		////////////////

		// perform calculations
		boolean ret = true;
		ImageObject retObject = null;
		ImageObject retObject1 = null;

		double[] linePoints = null;
		// this region is set for vertical lines !!!
		SubAreaFloat region = new SubAreaFloat(0f, 0f, testObject1.getNumRows(), testObject1.getNumCols(), false);
		region.setAngle(0.0f);

		// find the pairs of points defining each line
		if (!myTest.SetupLines(myTest.getMyEdgeLine())) {
			System.err.println("ERROR: SetupLines failed");
			ret = false;
		}
		linePoints = myTest.FindLaserLines(testObject1, region);
		if (linePoints == null) {
			System.err.println("ERROR: findCountorPoints failed");
			ret = false;
		}
		retObject = (ImageObject) testObject1.clone();
/*
 * if (retObject.getNumBands() == 1) {
 * myTest.getMyEdgeLine().showEdgeLineResult(retObject, region); } else {
 * myTest.getMyEdgeLine().showSubLinesPPM(retObject, region); }
 */

		// draw lines from pairs of points 
		retObject = myTest.drawLines(testObject1, linePoints);
		if (retObject == null) {
			System.err.println("ERROR: drawLines failed");
			ret = false;
		}
		// calculate end points of gaps in each line
		double[] gapPoints = null;
		myTest.SetupLinesGap(myTest.getMyEdgeLineGap());
		gapPoints = myTest.FindLineGapPoints(testObject1, linePoints);
		if (gapPoints == null) {
			System.err.println("ERROR: findLineGapPoints failed");
			ret = false;
		}
/*
 * if (retObject.getNumBands() == 1) {
 * myTest.getMyEdgeLine().showEdgeLineResult(retObject, region); } else {
 * myTest.getMyEdgeLine().showSubLinesPPM(retObject, region); }
 */
		retObject1 = myTest.drawPoints(retObject, gapPoints);
		if (retObject1 == null) {
			System.err.println("ERROR: drawLines failed");
			ret = false;
		}

/*
 * ///////////////////// // test region orientation SubAreaFloat region1 = new
 * SubAreaFloat(100f, 100f, 50, 80, false); region1.setAngle(0.0f); DrawOp draw1 =
 * new DrawOp(); ImPoint ptsc = new ImPoint(region1.getRow(), region1.getCol());
 * Point3DDouble colorv = new Point3DDouble(1); double[] ptsDouble = new
 * double[3]; ptsDouble[0] = 0; ptsDouble[1] = 0; ptsDouble[2] = 255.0;
 * colorv.setPtsDouble(ptsDouble); draw1.draw_colorboxDouble(retObject1,
 * region1, ptsc, colorv); ImPoint cir = new ImPoint(region1.getRow(),
 * region1.getCol()); cir.setV(3); draw1.draw_colorcircleDouble(retObject1, cir,
 * colorv);
 * 
 * region1.setRow(region1.getRow() + 10); region1.setAngle(90.0f); ptsDouble[1] =
 * 255.0; colorv.setPtsDouble(ptsDouble); ptsc.setX(region1.getRow());
 * ptsc.setY(region1.getCol()); draw1.draw_colorboxDouble(retObject1, region1,
 * ptsc, colorv); cir.setX(region1.getRow()); cir.setY(region1.getCol());
 * draw1.draw_colorcircleDouble(retObject1, cir, colorv);
 */
		// save out the retObject
		ImageLoader.writeImage(OutFileName, retObject1);

		System.out.println("Test Result = " + ret);

	}

	// constructor
	public Test_EdgeLine() {
	}

/*
 * public double[] FindLineGapPoints(ImageObject im, double[] lineEndPoints)
 * throws Exception { //sanity checks if (im == null) {
 * System.err.println("input image does not exist"); return null; } ImageObject
 * imBand1 = null; if (im.getNumBands() > 1) { imBand1 = im.extractBand(0); }
 * else { imBand1 = im; }
 * 
 * int numOfGapsPerLine = 1; int numLaserLines = lineEndPoints.length >> 3;
 * double[] lineGapPoints = new double[numOfGapsPerLine * 2 * numLaserLines *
 * 2]; logger.debug("numLaserLines = " + numLaserLines + ", sizeofLineGapPoints = " +
 * lineGapPoints.length); // create regions int idx, i, temp; SubAreaFloat
 * region = new SubAreaFloat(); //double[] discontLines = null; int
 * countLineGapPoints = 0;
 * 
 * for (idx = 0; idx < lineEndPoints.length - 7; idx += 8) {
 * region.setRow((float) lineEndPoints[idx + 2]); region.setCol((float)
 * lineEndPoints[idx + 3]); region.setHeight((float) Math.abs(lineEndPoints[idx +
 * 7] - lineEndPoints[idx + 3])); region.setWidth((float)
 * Math.abs(lineEndPoints[idx + 2] - lineEndPoints[idx]));
 * region.setAngle((float) 90.0); logger.debug("\n idx=" + idx + ",region = " +
 * region.convertSubAreaFloat2String()); // test
 * //_myEdgeLine.showEdgeLineResult(imBand1, region);
 * //_myEdgeLine.showSubLinesPPM(imBand1, region); // save out the retObject
 * //ImageLoader.writeImage("C:/PeterB/Presentations/Proposals/Gladson/laserVisible03-14-2008/exper1/test1.tif",
 * imBand1); // this N2Find might change if objects with holes should be
 * detected _myEdgeLine.setN2Find(numOfGapsPerLine * 2);
 * _myEdgeLine.setAccept(30.0F); _myEdgeLine.setSortType((short) 2); /// sort by
 * offset // 2 is by score
 * 
 * if (!_myEdgeLineGap.findLines(imBand1, region)) { System.err.println("Error:
 * failed FindLines at angle=" + region.getAngle()); return null; } //test
 * System.out.println("Test: FindLines at angle=" + region.getAngle() + " Number
 * of Found=" + _myEdgeLineGap.getNFound());
 * //_myEdgeLine.printEdgeLineObject(); //_myEdgeLineGap.PrintEdgeLineRes();
 * 
 * EdgeLineRes myRes = null; if (_myEdgeLineGap.getNFound() > 0) {
 * //_myEdgeLineGap.setSortType((short) 2); // 1 sort by offset // 2 by score
 * //_myEdgeLineGap.sort(_myEdgeLine);
 * 
 * temp = countLineGapPoints; for (i = 0; i < _myEdgeLineGap.getNFound(); i++) {
 * myRes = _myEdgeLineGap.getResult(i); myRes.PrintEdgeLineRes(); if
 * ((myRes.getO() < 15) || (myRes.getO() > region.getWidth() - 15)) { // skip
 * the solutions that are too close to the image/region border
 * logger.debug("INFO: skipped - too close"); } else { if ((countLineGapPoints -
 * temp) < numOfGapsPerLine * 4) { lineGapPoints[countLineGapPoints] =
 * myRes.getX(); lineGapPoints[countLineGapPoints + 1] = myRes.getY();
 * countLineGapPoints += 2; logger.debug("INFO: added"); } else {
 * logger.debug("INFO: skipped - two points already included based on score"); } } } } }
 * for (i = 0; i < lineGapPoints.length; i += 2) {
 * System.out.println("LineGapPoints[" + (i >> 1) + "]=" + lineGapPoints[i] +
 * "," + lineGapPoints[i + 1]); } return lineGapPoints; }
 */

	/**
	 * This method takes an array of line end points and finds the gap points
	 */
	public double[] FindLineGapPoints(ImageObject im, double[] lineEndPoints) throws Exception {
		//sanity checks
		if (im == null) {
			System.err.println("input image does not exist");
			return null;
		}
		ImageObject imBand1 = null;
		if (im.getNumBands() > 1) {
			imBand1 = im.extractBand(0);
		} else {
			imBand1 = im;
		}

		int numLaserLines = lineEndPoints.length >> 2;
		double[] lineGapPoints = new double[_numOfGapsPerLine * 2 * numLaserLines * 2];
		logger.debug("numLaserLines = " + numLaserLines + ", sizeofLineGapPoints = " + lineGapPoints.length);

		// create regions
		int idx, i, temp;
		float tempFloat, tempFloatS;
		SubAreaFloat region = new SubAreaFloat();
		GeomOper geom = new GeomOper();
		ImPoint ptsc = new ImPoint();
		ImPoint pts = new ImPoint();
		double alpha = 0.0;

		int countLineGapPoints = 0;

		for (idx = 0; idx < lineEndPoints.length - 3; idx += 4) {
			// TODO these points should be rotated  
			region.setRow((float) lineEndPoints[idx + 2]);
			region.setCol((float) lineEndPoints[idx + 3] - _laserLineThickness);
			region.setHeight((float) _laserLineThickness * 2);
			region.setWidth((float) Math.abs(lineEndPoints[idx + 2] - lineEndPoints[idx]));
			region.setAngle((float) 90.0);
			logger.debug("\n idx=" + idx + ",region = " + region.convertSubAreaFloat2String());

			alpha = Math.PI * region.getAngle() / 180.0;
			ptsc.SetImPoint(region.getRow(), region.getCol());

			// test
			//_myEdgeLine.showEdgeLineResult(imBand1, region);
			//_myEdgeLine.showSubLinesPPM(imBand1, region);
			// save out the retObject
			//ImageLoader.writeImage("C:/PeterB/Presentations/Proposals/Gladson/laserVisible03-14-2008/exper1/test1.tif", imBand1);

			//////////////////////////////////////
			if (!_myEdgeLineGap.findLines(imBand1, region)) {
				System.err.println("Error:  failed FindLines at angle=" + region.getAngle());
				return null;
			}
			//test
			System.out.println("Test: line index =" + (idx >> 2) + " FindGapPoints at angle=" + region.getAngle() + " Number of Found=" + _myEdgeLineGap.getNFound());
			//_myEdgeLine.printEdgeLineObject();
			//_myEdgeLineGap.PrintEdgeLineRes();

			EdgeLineRes myRes = null;

			if (_myEdgeLineGap.getNFound() > 0) {
				// eliminate gaps that are too close to each other
				_myEdgeLineGap.setSortType((short) 1); // 1 sort by offset // 2 by score
				_myEdgeLineGap.sort(_myEdgeLine);
				tempFloat = _myEdgeLineGap.getResult(0).getO();
				tempFloatS = _myEdgeLineGap.getResult(0).getS();
				for (i = 1; i < _myEdgeLineGap.getNFound(); i++) {
					myRes = _myEdgeLineGap.getResult(i);
					if (Math.abs(myRes.getO() - tempFloat) < _minGapLength) {
						if (myRes.getS() > tempFloatS) {
							_myEdgeLineGap.getResult(i - 1).setZero();
						} else {
							_myEdgeLineGap.getResult(i).setZero();
						}
					}
					tempFloat = myRes.getO();
				}
				_myEdgeLineGap.setSortType((short) 2); // 1 sort by offset // 2 by score
				_myEdgeLineGap.sort(_myEdgeLine);
			}

			///////////////////////////////////////////////////////////////////////////////
			// find laser-object boundary points
			temp = countLineGapPoints;
			for (i = 0; i < _myEdgeLineGap.getNFound(); i++) {
				myRes = _myEdgeLineGap.getResult(i);
				myRes.PrintEdgeLineRes();
				if (myRes.getP1() == 1) {
					if ((myRes.getO() < 15) || (myRes.getO() > region.getWidth() - 15)) {
						// skip the solutions that are too close to the image/region border
						logger.debug("INFO: laser-to-object: skipped - too close");
					} else {
						if ((countLineGapPoints - temp) < _numOfGapsPerLine * 2) {
							// select the middle point of the line and rotate it
							// to obtain the most accurate laser-object boundary point
							pts.SetImPoint((region.getRow() + myRes.getH() * 0.5), region.getCol() + myRes.getO());
							geom.rotatePoint(ptsc, pts, alpha);

							lineGapPoints[countLineGapPoints] = pts.getX();
							lineGapPoints[countLineGapPoints + 1] = pts.getY();
							countLineGapPoints += 2;
							logger.debug("INFO: laser-to-object: added");
						} else {
							logger.debug("INFO: laser-to-object: skipped - two points already included based on score");

						}
					}
				}
			}

			///////////////////////////////////////////////////////////////////////////////
			// find object to laser  point

			temp = countLineGapPoints;
			for (i = 0; i < _myEdgeLineGap.getNFound(); i++) {
				myRes = _myEdgeLineGap.getResult(i);
				myRes.PrintEdgeLineRes();
				if (myRes.getP1() == -1) {
					if ((myRes.getO() < 15) || (myRes.getO() > region.getWidth() - 15)) {
						// skip the solutions that are too close to the image/region border
						logger.debug("INFO: object-to-laser: skipped - too close");
					} else {
						if ((countLineGapPoints - temp) < _numOfGapsPerLine * 2) {
							// select the middle point of the line and rotate it
							// to obtain the most accurate object-laser boundary point
							pts.SetImPoint((region.getRow() + myRes.getH() * 0.5), region.getCol() + myRes.getO());
							geom.rotatePoint(ptsc, pts, alpha);

							lineGapPoints[countLineGapPoints] = pts.getX();
							lineGapPoints[countLineGapPoints + 1] = pts.getY();
							countLineGapPoints += 2;
							logger.debug("INFO: object-to-laser: added");
						} else {
							logger.debug("INFO: object-to-laser:skipped - two points already included based on score");

						}
					}
				}
			}

			// reset all results to zero
			for (i = 0; i < _myEdgeLineGap.getNFound(); i++) {
				myRes = _myEdgeLineGap.getResult(i);
				myRes.setZero();
			}
			_myEdgeLine.setNFound(0);

		}
		for (i = 0; i < lineGapPoints.length; i += 2) {
			System.out.println("LineGapPoints[" + (i >> 1) + "]=" + lineGapPoints[i] + "," + lineGapPoints[i + 1]);
		}
		return lineGapPoints;
	}

	/**
	 * This method finds the lines in a 270 degree rotated image (image with
	 * vertical laser lines
	 * 
	 * @param im
	 * @return
	 * @throws Exception
	 */
	public double[] FindLaserLines(ImageObject im, SubAreaFloat region) throws Exception {

		//sanity checks
		if (im == null) {
			System.err.println("input image does not exist");
			return null;
		}
		ImageObject imBand1 = null;
		if (im.getNumBands() > 1) {
			imBand1 = im.extractBand(0);
		} else {
			imBand1 = im;
		}
		int idx, i;
		// setup the initial region

		double[] retPoints = null;

		if (!_myEdgeLine.findLines(imBand1, region)) {
			System.err.println("Error:  failed FindLines at angle=" + region.getAngle());
			return null;
		}
		//test
		System.out.println("Test: FindLines at angle=" + region.getAngle() + " Number of Found=" + _myEdgeLine.getNFound());
		_myEdgeLine.printEdgeLineObject();

		EdgeLineRes myRes = null;
		if (_myEdgeLine.getNFound() > 0) {
			retPoints = new double[_myEdgeLine.getNFound() << 2];
			i = 0;
			for (idx = 0; idx < _myEdgeLine.getNFound(); idx++) {
				myRes = _myEdgeLine.getResult(idx);
				myRes.PrintEdgeLineRes();
				retPoints[i] = myRes.getX();
				retPoints[i + 1] = myRes.getY();
				retPoints[i + 2] = myRes.getX() + myRes.getH() - 1;
				retPoints[i + 3] = myRes.getY();
				i += 4;
			}

/*
 * // try checking sublines if (!_myEdgeLine.findSubLines(imBand1, region)) {
 * System.out.println("Error: failed FindSubLines at angle=" + initAngle); }
 * else { for (idx = 0; idx < _myEdgeLine.getNFoundSub(); idx++) { myRes =
 * _myEdgeLine.getResult(idx); myRes.PrintEdgeLineRes();
 * _myEdgeLine.printEdgeLineObject(); } }
 */
		}

		return retPoints;

	}

	//////////////////////////////////////////////////////////////
	private boolean SetupLines(EdgeLineObject lineD) {
		// modify this number depending on how many lines we anticipate !!!
		lineD.setN2Find(20);
		lineD.setAccept(5.0F);
		//lineD.SetAccept(0.01);
		lineD.setSortType((short) 1); /// sort by offset  // 2 is by score 
		if (!lineD.setFeature(1, _laserLineThickness, _laserLineThickness - 1, -1)) {
			return false;
		}

		if (!lineD.setOperator(10, 2)) {
			return false;
		}
		lineD.setSubLineLength(2);
		lineD.setSubLinesPerLine(4);
		lineD.setOutFlag(0);
		return true;
	}

	private boolean SetupLinesGap(EdgeLineObject lineD) {
		// modify this number depending on how many lines we anticipate !!!
		lineD.setN2Find(_numOfGapsPerLine * 2 + 4);// plus four is to guarantee extra lines if some are two close too each other 

		lineD.setAccept(14.0F);
		//lineD.SetAccept(0.01);
		lineD.setSortType((short) 2); /// sort by offset  // 2 is by score 

		if (!lineD.setOperator(10, 4)) {
			return false;
		}
		lineD.setSubLineLength(2);
		lineD.setSubLinesPerLine(4);
		lineD.setOutFlag(0);
		return true;
	}

	//////////////////////////////////////////////////////////////

	/**
	 * This method draws lines from a sequence of pairs of points stored in a
	 * double array into a cloned copy of the ImageObject passed in
	 * 
	 * @param testObject1
	 * @param retPoints
	 * @return
	 */
	private ImageObject drawLines(ImageObject testObject1, double[] retPoints) {
		if (retPoints == null) {
			System.out.println("ERROR: could not find contour points ");
			return null;
		} else {

			ImageObject retObject = null;
			try {
				retObject = (ImageObject) testObject1.clone();
			} catch (CloneNotSupportedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			retObject.setProperties(testObject1.getProperties());

			ImPoint[] pts = new ImPoint[retPoints.length >> 1];
			int i, idx;
			for (i = 0, idx = 0; i < retPoints.length; i += 2, idx++) {
				pts[idx] = new ImPoint(retPoints[i], retPoints[i + 1]);
			}

			int arm_size = 5;
			Point3DDouble colorv = new Point3DDouble(1);
			double[] ptsDouble = new double[3];
			ptsDouble[0] = ptsDouble[1] = ptsDouble[2] = 255.0;
			colorv.setPtsDouble(ptsDouble);
			double color = 255;
			int thickness = 2;
			DrawOp draw = new DrawOp();
			if (retObject.getNumBands() == 1) {
				for (i = 0; i < pts.length; i++) {
					System.out.println("pts[" + i + "]=" + pts[i].x + "," + pts[i].y);
					//draw.draw_crossDouble(retObject, pts[i], arm_size, color);

				}
				for (i = 0; i < pts.length - 1; i += 2) {
					ImLine Linetmp = new ImLine(pts[i], pts[i + 1]);
					draw.plot_lineDouble(retObject, Linetmp, color);
				}

			} else {
				for (i = 0; i < pts.length; i++) {
					System.out.println("pts[" + i + "]=" + pts[i].x + "," + pts[i].y);

					//draw.draw_colorcrossDouble(retObject, pts[i], arm_size, colorv);
				}
				for (i = 0; i < pts.length - 1; i += 2) {
					ImLine Linetmp = new ImLine(pts[i], pts[i + 1]);
					draw.plot_colorlineDouble(retObject, Linetmp, colorv);
				}

			}
			return retObject;
		}
	}

	private ImageObject drawPoints(ImageObject testObject1, double[] retPoints) {
		if (retPoints == null) {
			System.out.println("ERROR: could not find contour points ");
			return null;
		} else {

			ImageObject retObject = null;
			try {
				retObject = (ImageObject) testObject1.clone();
			} catch (CloneNotSupportedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			retObject.setProperties(testObject1.getProperties());

			ImPoint[] pts = new ImPoint[retPoints.length >> 1];
			int i, idx;
			for (i = 0, idx = 0; i < retPoints.length; i += 2, idx++) {
				pts[idx] = new ImPoint(retPoints[i], retPoints[i + 1]);
			}

			int arm_size = 25;
			Point3DDouble colorv = new Point3DDouble(1);
			double[] ptsDouble = new double[3];
			ptsDouble[0] = 0;
			ptsDouble[1] = 255;
			ptsDouble[2] = 0.0;
			colorv.setPtsDouble(ptsDouble);
			double color = 255;
			int thickness = 2;
			DrawOp draw = new DrawOp();
			if (retObject.getNumBands() == 1) {
				for (i = 0; i < pts.length; i++) {
					System.out.println("pts[" + i + "]=" + pts[i].x + "," + pts[i].y);
					draw.draw_crossDouble(retObject, pts[i], arm_size, color);

				}

			} else {
				for (i = 0; i < pts.length; i++) {
					System.out.println("pts[" + i + "]=" + pts[i].x + "," + pts[i].y);
					draw.draw_colorcrossDouble(retObject, pts[i], arm_size, colorv);
				}

			}
			return retObject;
		}
	}

	public EdgeLine getMyEdgeLine() {
		return _myEdgeLine;
	}

	public EdgeLine getMyEdgeLineGap() {
		return _myEdgeLineGap;
	}
}