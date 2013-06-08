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
package edu.illinois.ncsa.isda.im2learn.ext.math;

/*
 * GeomOper.java
 * 
 */

/**
 * 
 * @author Peter Bajcsy
 * @version 2.0
 */

import edu.illinois.ncsa.isda.im2learn.core.datatype.ComplexNum;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImLine;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImPoint;
import edu.illinois.ncsa.isda.im2learn.core.datatype.LimitValues;
import edu.illinois.ncsa.isda.im2learn.core.datatype.SubAreaFloat;

//import LimitValues;
//import ImPoint;
//import ImLine;

// this is the class that performs geometrical operations

public class GeomOper {
	LimitValues		_lim	= new LimitValues();

	private boolean	_debugGeomOper;

	/**
	 * Empty constructor
	 * 
	 * @return empty constructor
	 */
	public GeomOper() {

	}

	//setters and getters
	/**
	 * Setter for a debug flag
	 * 
	 * @return void
	 */
	public void setDebug(boolean flag) {
		_debugGeomOper = flag;
	}

	/**
	 * Getter for a debug flag
	 * 
	 * @return boolean debug flag
	 */

	public boolean getDebug() {
		return _debugGeomOper;
	}

	/**
	 * Computes the location of a point with respect to a line dividing the
	 * space
	 * 
	 * @param divLine
	 *            ImLine is the dividing line,
	 * @param ptsHalfspace
	 *            ImPoint is the point defining a half space and
	 * @param ptsTest
	 *            ImPoint is the point that should be explored
	 * @return boolean answer to a question whether a point is in a defined half
	 *         space
	 */
	public boolean isPtsInHalfspace(ImLine divLine, ImPoint ptsHalfspace, ImPoint ptsTest) {
		// sanity check
		if ((divLine == null) || (ptsHalfspace == null) || (ptsTest == null)) {
			System.out.println("Error: no data to process");
			return false;
		}
		//test
		//System.out.println("Tesr: inside IsPtsInHalfspace");
		//divLine.PrintImLine();
		//ptsHalfspace.PrintImPoint();
		//ptsTest.PrintImPoint();

		double temp, temp1;
		if (divLine.getSlope() < _lim.SLOPE_MAX) {
			temp = divLine.getSlope() * ptsHalfspace.x + divLine.getQ();
			temp1 = divLine.getSlope() * ptsTest.x + divLine.getQ();
			//System.out.println("Tesr: inside slope < Max: temp="+temp+" temp1="+temp1);
			if (temp < ptsHalfspace.y) {
				if (temp1 < ptsTest.y) {
					return true;
				} else {
					return false;
				}
			} else {
				if (temp1 > ptsTest.y) {
					return true;
				} else {
					return false;
				}
			}
		} else {

			if ((ptsHalfspace.x < divLine.getQ()) && (ptsTest.x < divLine.getQ())) {
				return true;
			} else {
				if ((ptsHalfspace.x >= divLine.getQ()) && (ptsTest.x >= divLine.getQ())) {
					return true;
				} else {
					return false;
				}
			}
		}
		//return true;
	}

	/**
	 * auxiliary operation determines if a point is inside or outside of a
	 * triangle input:three triangle points plus test point output: 1 - yes
	 * (inside), 0- no (outside)
	 * 
	 * @param ptsIn
	 *            ImPoint[] = three triangle points
	 * @param pts
	 *            ImPoint = test point
	 * @return boolean answer to a question whether a point is inside or outside
	 *         of a triangle
	 */
	public boolean isPointInsideTriangle(ImPoint[] ptsIn, ImPoint pts) {
		ImLine line1 = new ImLine();
		int i, j, k, flag;
		i = 0;
		flag = 1;
		while ((flag != 0) && (i < 3)) {
			if (i == 2) {
				j = 0;
			} else {
				j = i + 1;
			}
			if (j == 2) {
				k = 0;
			} else {
				k = j + 1;
			}

			line1.setPts1(ptsIn[i]);
			line1.setPts2(ptsIn[j]);
			line1.computeSlopeFromPts();
			flag = 0;

			if (line1.getSlope() != _lim.SLOPE_MAX) {
				if (ptsIn[k].y > ptsIn[k].x * line1.getSlope() + line1.getQ()) {
					if ((pts.y - (pts.x * line1.getSlope() + line1.getQ())) > -0.5) {
						flag = 1;
					}
				} else {
					if ((pts.x * line1.getSlope() + line1.getQ()) - pts.y > -0.5) {
						flag = 1;
					}
				}
			} else {
				if (ptsIn[k].x > line1.getQ()) {
					if (pts.x - line1.getQ() > -0.5) {
						flag = 1;
					}
				} else {
					if (line1.getQ() - pts.x > -0.5) {
						flag = 1;
					}
				}
			}
			i++;
		}
		if (flag > 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * auxiliary operation determines if a point is inside or outside of a
	 * rotated box input: box and a test point output: yes (inside), no
	 * (outside)
	 * 
	 * @param area
	 *            SubAreaFloat = box defintion
	 * @param pts
	 *            ImPoint = test point
	 * @return boolean answer to a question whether a point is inside or outside
	 *         of a rotated box
	 */
	public boolean isPointInsideBox(SubAreaFloat area, ImPoint pts) {

		ImPoint ptsc = new ImPoint();
		//ImPoint ptstemp = new ImPoint();
		double alpha;
		//ptstemp = pts;
		if (Math.abs(area.getAngle()) > _lim.EPSILON3) {//0.001
			ptsc.x = area.getRow(); // center of rotation
			ptsc.y = area.getCol();
			alpha = area.getAngle() * _lim.Deg2Rad;//PI/180;

			// rotate the point and compare with the box boundaries
			rotatePoint(ptsc, pts, -alpha);
		}

		if ((pts.x <= area.getRow() + area.getHeight()) && (pts.x >= area.getRow())) {
			if ((pts.y <= area.getCol() + area.getWidth()) && (pts.y >= area.getCol())) {
				return true;
			}
		}
		return false;
	}

	////////////////////////////////////////////////////////
	/**
	 * Distance of two points in high dimensional space Euclidean distance
	 * compute Euclidean distance between two high dimensional points
	 * 
	 * 
	 * @param sampPerPixel
	 *            int = dimensionality of points
	 * @param point1
	 *            byte[] = 1st array of values
	 * @param idx1
	 *            int = index defining the start position of the 1st high
	 *            dimensional point in the array
	 * @param point2
	 *            byte[] = 2nd array of values
	 * @param idx2
	 *            int = index defining the start position of the 2nd high
	 *            dimensional point in the array
	 * @return double = Euclidean distance between two high dimensional points
	 */
	public double euclidDist(int sampPerPixel, byte[] point1, int idx1, byte[] point2, int idx2) {
		double sum = 0.0;
		//test
		//System.out.println("TEST: (point1[idx1]& 0xff)="+(point1[idx1]& 0xff));
		//System.out.println("TEST: (point2[idx2]& 0xff)="+(point2[idx2]& 0xff));

		// the case of 1D image
		if (sampPerPixel == 1) {
			sum = (point1[idx1] & 0xff) - (point2[idx2] & 0xff);
			if (sum < 0.0) {
				return (-sum);
			} else {
				return (sum);
			}
		}
		int i;
		for (i = 0; i < sampPerPixel; i++) {
			sum += ((point1[idx1] & 0xff) - (point2[idx2] & 0xff)) * ((point1[idx1] & 0xff) - (point2[idx2] & 0xff));
			idx1++;
			idx2++;
		}
		if (sum < 0) {
			System.out.println("ERROR: there is something wrong with Euclidean distance computation");
			return 0.0;
		}
		sum = Math.sqrt(sum);
		return sum;
	}

	/**
	 * Distance of two points in high dimensional space Euclidean distance
	 * compute Euclidean distance between two high dimensional points
	 * 
	 * 
	 * @param sampPerPixel
	 *            int = dimensionality of points
	 * @param point1
	 *            short[] = 1st array of values
	 * @param idx1
	 *            int = index defining the start position of the 1st high
	 *            dimensional point in the array
	 * @param point2
	 *            short[] = 2nd array of values
	 * @param idx2
	 *            int = index defining the start position of the 2nd high
	 *            dimensional point in the array
	 * @return double = Euclidean distance between two high dimensional points
	 */
	public double euclidDist(int sampPerPixel, short[] point1, int idx1, short[] point2, int idx2) {
		double sum = 0.0;
		// the case of 1D image
		if (sampPerPixel == 1) {
			sum = (point1[idx1] & 0xffff) - (point2[idx2] & 0xffff);
			if (sum < 0.0) {
				return (-sum);
			} else {
				return (sum);
			}
		}
		int i;
		for (i = 0; i < sampPerPixel; i++) {
			sum += ((double) (point1[idx1] & 0xffff) - (point2[idx2] & 0xffff)) * ((point1[idx1] & 0xffff) - (point2[idx2] & 0xffff));
			idx1++;
			idx2++;
		}
		if (sum < 0) {
			System.out.println("ERROR: there is something wrong with Euclidean distance computation");
			return 0.0;
		}
		sum = Math.sqrt(sum);
		return sum;
	}

	/**
	 * Distance of two points in high dimensional space Euclidean distance
	 * compute Euclidean distance between two high dimensional points
	 * 
	 * 
	 * @param sampPerPixel
	 *            int = dimensionality of points
	 * @param point1
	 *            float[] = 1st array of values
	 * @param idx1
	 *            int = index defining the start position of the 1st high
	 *            dimensional point in the array
	 * @param point2
	 *            float[] = 2nd array of values
	 * @param idx2
	 *            int = index defining the start position of the 2nd high
	 *            dimensional point in the array
	 * @return double = Euclidean distance between two high dimensional points
	 */
	public double euclidDist(int sampPerPixel, float[] point1, int idx1, float[] point2, int idx2) {
		double sum = 0.0;
		// the case of 1D image
		if (sampPerPixel == 1) {
			sum = point1[idx1] - point2[idx2];
			if (sum < 0.0) {
				return (-sum);
			} else {
				return (sum);
			}
		}
		int i;
		for (i = 0; i < sampPerPixel; i++) {
			sum += (point1[idx1] - point2[idx2]) * (point1[idx1] - point2[idx2]);
			idx1++;
			idx2++;
		}
		if (sum < 0) {
			System.out.println("ERROR: there is something wrong with Euclidean distance computation");
			return 0.0;
		}
		sum = Math.sqrt(sum);
		return sum;
	}

	/**
	 * Distance of two points in high dimensional space Euclidean distance
	 * compute Euclidean distance between two high dimensional points
	 * 
	 * 
	 * @param sampPerPixel
	 *            int = dimensionality of points
	 * @param point1
	 *            double[] = 1st array of values
	 * @param idx1
	 *            int = index defining the start position of the 1st high
	 *            dimensional point in the array
	 * @param point2
	 *            float[] = 2nd array of values
	 * @param idx2
	 *            int = index defining the start position of the 2nd high
	 *            dimensional point in the array
	 * @return double = Euclidean distance between two high dimensional points
	 */
	public double euclidDist(int sampPerPixel, double[] point1, int idx1, float[] point2, int idx2) {
		double sum = 0.0;
		// the case of 1D image
		if (sampPerPixel == 1) {
			sum = point1[idx1] - point2[idx2];
			if (sum < 0.0) {
				return (-sum);
			} else {
				return (sum);
			}
		}
		int i;
		for (i = 0; i < sampPerPixel; i++) {
			sum += (point1[idx1] - point2[idx2]) * (point1[idx1] - point2[idx2]);
			idx1++;
			idx2++;
		}
		if (sum < 0) {
			System.out.println("ERROR: there is something wrong with Euclidean distance computation");
			return 0.0;
		}
		sum = Math.sqrt(sum);
		return sum;
	}

	/**
	 * Distance of two points in high dimensional space Euclidean distance
	 * compute Euclidean distance between two high dimensional points
	 * 
	 * 
	 * @param sampPerPixel
	 *            int = dimensionality of points
	 * @param point1
	 *            double[] = 1st array of values
	 * @param idx1
	 *            int = index defining the start position of the 1st high
	 *            dimensional point in the array
	 * @param point2
	 *            double[] = 2nd array of values
	 * @param idx2
	 *            int = index defining the start position of the 2nd high
	 *            dimensional point in the array
	 * @return double = Euclidean distance between two high dimensional points
	 */
	public double euclidDist(int sampPerPixel, double[] point1, int idx1, double[] point2, int idx2) {
		double sum = 0.0;
		// the case of 1D image
		if (sampPerPixel == 1) {
			sum = point1[idx1] - point2[idx2];
			if (sum < 0.0) {
				return (-sum);
			} else {
				return (sum);
			}
		}
		int i;
		for (i = 0; i < sampPerPixel; i++) {
			sum += (point1[idx1] - point2[idx2]) * (point1[idx1] - point2[idx2]);
			idx1++;
			idx2++;
		}
		if (sum < 0) {
			System.out.println("ERROR: there is something wrong with Euclidean distance computation");
			return 0.0;
		}
		sum = Math.sqrt(sum);
		return sum;
	}

	/**
	 * Distance of two points in high dimensional space Euclidean distance
	 * compute Euclidean distance between two high dimensional points
	 * 
	 * 
	 * @param sampPerPixel
	 *            int = dimensionality of points
	 * @param point1
	 *            double[] = 1st array of values
	 * @param idx1
	 *            int = index defining the start position of the 1st high
	 *            dimensional point in the array
	 * @param point2
	 *            byte[] = 2nd array of values
	 * @param idx2
	 *            int = index defining the start position of the 2nd high
	 *            dimensional point in the array
	 * @return double = Euclidean distance between two high dimensional points
	 */
	public double euclidDist(int sampPerPixel, double[] point1, int idx1, byte[] point2, int idx2) {
		double sum = 0.0;
		// the case of 1D image
		if (sampPerPixel == 1) {
			sum = point1[idx1] - (point2[idx2] & 0xff);
			if (sum < 0.0) {
				return (-sum);
			} else {
				return (sum);
			}
		}
		int i;
		for (i = 0; i < sampPerPixel; i++) {
			sum += (point1[idx1] - (point2[idx2] & 0xff)) * (point1[idx1] - (point2[idx2] & 0xff));
			idx1++;
			idx2++;
		}
		if (sum < 0) {
			System.out.println("ERROR: there is something wrong with Euclidean distance computation");
			return 0.0;
		}
		sum = Math.sqrt(sum);
		return sum;
	}

	/**
	 * Distance of two points in high dimensional space Euclidean distance
	 * compute Euclidean distance between two high dimensional points
	 * 
	 * 
	 * @param sampPerPixel
	 *            int = dimensionality of points
	 * @param point1
	 *            double[] = 1st array of values
	 * @param idx1
	 *            int = index defining the start position of the 1st high
	 *            dimensional point in the array
	 * @param point2
	 *            short[] = 2nd array of values
	 * @param idx2
	 *            int = index defining the start position of the 2nd high
	 *            dimensional point in the array
	 * @return double = Euclidean distance between two high dimensional points
	 */
	public double euclidDist(int sampPerPixel, double[] point1, int idx1, short[] point2, int idx2) {
		double sum = 0.0;
		// the case of 1D image
		if (sampPerPixel == 1) {
			sum = point1[idx1] - (point2[idx2] & 0xffff);
			if (sum < 0.0) {
				return (-sum);
			} else {
				return (sum);
			}
		}
		int i;
		for (i = 0; i < sampPerPixel; i++) {
			sum += (point1[idx1] - (point2[idx2] & 0xffff)) * (point1[idx1] - (point2[idx2] & 0xffff));
			idx1++;
			idx2++;
		}
		if (sum < 0) {
			System.out.println("ERROR: there is something wrong with Euclidean distance computation");
			return 0.0;
		}
		sum = Math.sqrt(sum);
		return sum;
	}

	/**
	 * This method is a simplification of the API	
	 * @param dimensionality of the points
	 * @param point1 - point 1
	 * @param point2 - point 2
	 * @return a double precision value equals to the resulting Euclidean distance 
	 */
	public double euclidDist(int dimensionality, double[] point1, double[] point2) {
		double sum = 0.0;
		// the case of 1D image
		if (dimensionality == 1) {
			sum = point1[0] - point2[0];
			if (sum < 0.0) {
				return (-sum);
			} else {
				return (sum);
			}
		}
		int i;
		for (i = 0; i < dimensionality; i++) {
			sum += (point1[i] - point2[i]) * (point1[i] - point2[i]);
		}
		if (sum < 0) {
			System.out.println("ERROR: there is something wrong with Euclidean distance computation");
			return 0.0;
		}
		sum = Math.sqrt(sum);
		return sum;
	}

	///////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////
	/**
	 * Computes vector magnitude
	 * 
	 * @param sampPerPixel
	 *            int = dimensionality of point
	 * @param point1
	 *            float[] = array of values
	 * @param idx1
	 *            int = index defining the start position of the high
	 *            dimensional point in the array
	 * @return double = resulting magnitude
	 */
	public double vectorMag(int sampPerPixel, float[] point1, int idx1) {
		double sum = 0.0;
		// the case of 1D image
		if (sampPerPixel == 1) {
			sum = point1[idx1];
			if (sum < 0.0) {
				return (-sum);
			} else {
				return (sum);
			}
		}
		int i;
		for (i = 0; i < sampPerPixel; i++) {
			sum += point1[idx1] * point1[idx1];
			idx1++;
		}
		if (sum < 0) {
			System.out.println("ERROR: there is something wrong with VecMag computation");
			return 0.0;
		}
		sum = Math.sqrt(sum);
		return sum;
	}

	//////////////////////////////////////////////////////////
	/**
	 * Computes vector magnitude
	 * 
	 * @param sampPerPixel
	 *            int = dimensionality of point
	 * @param point1
	 *            double[] = array of values
	 * @param idx1
	 *            int = index defining the start position of the high
	 *            dimensional point in the array
	 * @return double = resulting magnitude
	 */
	public double vectorMag(int sampPerPixel, double[] point1, int idx1) {
		double sum = 0.0;
		// the case of 1D image
		if (sampPerPixel == 1) {
			sum = point1[idx1];
			if (sum < 0.0) {
				return (-sum);
			} else {
				return (sum);
			}
		}
		int i;
		for (i = 0; i < sampPerPixel; i++) {
			sum += point1[idx1] * point1[idx1];
			idx1++;
		}
		if (sum < 0) {
			System.out.println("ERROR: there is something wrong with VecMag computation");
			return 0.0;
		}
		sum = Math.sqrt(sum);
		return sum;
	}

	////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	// math operations
	/////////////////////////////////////////////////////////////////
	// this is also Euclidean distance but using ImPoint data container
	/**
	 * Computes Euclidean distance between two points defined as ImPoints
	 * 
	 * @param pts1
	 *            ImPoint = 1st point
	 * @param pts2
	 *            ImPoint = 2nd point
	 * @return double = resulting Euclidean distance
	 */
	public double distance(ImPoint pts1, ImPoint pts2) {
		return (Math.sqrt((pts1.x - pts2.x) * (pts1.x - pts2.x) + (pts1.y - pts2.y) * (pts1.y - pts2.y)));
	}

	/////////////////////////////////////////////////////////////////
	// Rotates several points by alpha around the center point ptsc
	/////////////////////////////////////////////////////////////////
	/**
	 * Rotates several points by alpha around the center point ptsc
	 * 
	 * @param ptsc
	 *            ImPoint = center point of rotation
	 * @param pts
	 *            ImPoint[] = array of points to be rotated
	 * @param number
	 *            int = number of points in the array
	 * @param alpha
	 *            double = rotation angle in radians
	 * @return boolean = success of rotation operation
	 */
	public boolean rotatePoints(ImPoint ptsc, ImPoint[] pts, int number, double alpha) {
		// alpha is in radians
		if ((ptsc == null) || (pts == null)) {
			System.out.println("ERROR: missing points");
			return false;
		}
		if (Math.abs(alpha) >= 0.01) {
			double helpx, helpy, help;
			for (int i = 0; i < number; i++) {
				helpx = pts[i].x - ptsc.x;
				helpy = pts[i].y - ptsc.y;
				help = helpx * Math.cos(alpha) - helpy * Math.sin(alpha);
				pts[i].x = (float) (help + ptsc.x);
				help = helpx * Math.sin(alpha) + helpy * Math.cos(alpha);
				pts[i].y = (float) (help + ptsc.y);
			}
		}
		return true;
	}

	/////////////////////////////////////////////////////////////////
	/**
	 * Rotates one point by alpha around the center point ptsc
	 * 
	 * @param ptsc
	 *            ImPoint = center point of rotation
	 * @param pts
	 *            ImPoint = a point to be rotated
	 * @param alpha
	 *            double = rotation angle in radians
	 * @return boolean = success of rotation operation
	 */
	public boolean rotatePoint(ImPoint ptsc, ImPoint pts, double alpha) {
		if ((ptsc == null) || (pts == null)) {
			System.out.println("ERROR: missing points");
			return false;
		}
		if (Math.abs(alpha) >= 0.01) {
			double helpx, helpy, help;
			helpx = pts.x - ptsc.x;
			helpy = pts.y - ptsc.y;
			help = helpx * Math.cos(alpha) - helpy * Math.sin(alpha);
			pts.x = help + ptsc.x;
			help = helpx * Math.sin(alpha) + helpy * Math.cos(alpha);
			pts.y = help + ptsc.y;
		}
		return true;
	}

	/////////////////////////////////////////////////////////////////
	/**
	 * Rotates a point by alpha; if the rotated point stays inside of a bounding
	 * box defined by (0,0) and (max row, max column) then the result is true
	 * else false
	 * 
	 * @param ptsc
	 *            ImPoint = center point of rotation
	 * @param pts
	 *            ImPoint = point to be rotated
	 * @param alpha
	 *            double = rotation angle in radians
	 * @param limits
	 *            ImPoint = the point (max row, max column)
	 * @return boolean = if the rotated point stays inside of a bounding box
	 *         defined by (0,0) and (max row, max column) then the result is
	 *         true else false
	 */
	public boolean rotatePointInside(ImPoint ptsc, ImPoint pts, double alpha, ImPoint limits) {
		rotatePoint(ptsc, pts, alpha);
		if ((pts.x >= 0) && (pts.x < limits.x) && (pts.y >= 0) && (pts.y < limits.y)) {
			return true;
		} else {
			return false;
		}
	}

	/////////////////////////////////////////////////////////////////////////////
	// compute all circle points given the radius and center point
	/////////////////////////////////////////////////////////////////////////////
	/**
	 * compute all circle points given the radius and center point
	 * 
	 * @param cir
	 *            ImPoint = circle definition by the center point (x,y) or (row,
	 *            column) and radius v
	 * @param ptschain
	 *            ImPoint[] = array of points that belong to the circle
	 * @param numpix
	 *            int = number of reported points
	 * @return boolean = success of operation
	 */
	public boolean circlePoints(ImPoint cir, ImPoint[] ptschain, int numpix) {
		float incr = (float) (2.0F * Math.PI / numpix);
		float angle;
		int row, col;
		int index = 0;
		for (angle = 0.0F; (angle < 2 * Math.PI) && (index < numpix); angle += incr) {
			row = (int) (cir.x + cir.getV() * Math.cos(angle) + 0.5);
			col = (int) (cir.y + cir.getV() * Math.sin(angle) + 0.5);
			ptschain[index].x = row;
			ptschain[index].y = col;
			index++;
		}
		return true;
	}//end of Circle Points

	///////////////////////////////////////////////////////////////////////
	// geometrical operations
	///////////////////////////////////////////////////////////////////////
	/**
	 * computes intersection of two lines
	 * 
	 * @param line1
	 *            ImLine = 1st line definition
	 * @param line2
	 *            ImLine= 2nd line definition
	 * @param pts1
	 *            ImPoint = interesecting point
	 * @return boolean = success of operation (false if two parallel lines)
	 */
	public boolean lineIntersectf(ImLine line1, ImLine line2, ImPoint pts1) {
		double help, help1;

		if (Math.abs(line1.getSlope() - line2.getSlope()) <= _lim.EPSILON3) { // 0.001
			if (_debugGeomOper) {
				System.out.println("lines are parallel\n");
			}
			pts1.x = -1;
			pts1.y = -1;
			return false;
		} else {
			if (Math.abs(line1.getSlope()) <= 0.01) {
				if ((line2.getSlope() == _lim.SLOPE_MAX) || (line2.getSlope() == -_lim.SLOPE_MAX)) {
					pts1.x = line2.getPts1().x;
					pts1.y = line1.getPts1().y;
				} else {
					pts1.x = (float) ((line1.getPts1().y - line2.getQ()) / (line2.getSlope()));
					pts1.y = line1.getPts1().y;
				}
			} else {
				if ((line1.getSlope() == _lim.SLOPE_MAX) || (line1.getSlope() == -_lim.SLOPE_MAX)) {
					if (Math.abs(line2.getSlope()) <= 0.01) {
						pts1.x = line1.getPts1().x;
						pts1.y = line2.getPts1().y;
					} else {
						pts1.x = line1.getPts1().x;
						pts1.y = (float) (line2.getSlope() * pts1.x + line2.getQ());
					}
				} else {
					if (Math.abs(line2.getSlope()) <= 0.01) {
						pts1.x = (float) ((line2.getPts1().y - line1.getQ()) / (line1.getSlope()));
						pts1.y = line2.getPts1().y;
					} else {
						if ((line2.getSlope() == _lim.SLOPE_MAX) || (line2.getSlope() == -_lim.SLOPE_MAX)) {
							pts1.x = line2.getPts1().x;
							pts1.y = (float) (line1.getSlope() * line2.getPts1().x + line1.getQ());
						} else {
							help = (line1.getQ() - line2.getQ()) / (line2.getSlope() - line1.getSlope());
							pts1.x = (float) help;
							if (Math.abs(line1.getSlope()) < Math.abs(line2.getSlope())) {
								help1 = ((line1.getSlope()) * help + (line1.getQ()));
							} else {
								help1 = ((line2.getSlope()) * help + (line2.getQ()));
							}

							pts1.y = (float) help1;
						}
					}
				}
			}
			//printf("intersected point  %d\t%d\n",pts1.x,pts1.y);
			return true;
		}
	}

	//////////////////////////////////////////////////////////////
	// computes intersection of a line and a circle
	//////////////////////////////////////////////////////////////
	/**
	 * computes intersection of a line and a circle
	 * 
	 * @param Linetmp
	 *            ImLine = line definition
	 * @param Cirtmp
	 *            ImPoint = circle definition
	 * @param tmpPoint
	 *            ImPoint[] = intersecting points
	 * @return int = number of intersecting points
	 */
	public int lineIntersectCircle(ImLine Linetmp, ImPoint Cirtmp, ImPoint[] tmpPoint) {
		ComplexNum[] res = new ComplexNum[4];
		float[] a = new float[4];
		int index;
		MathOp mathOp = new MathOp();

		// intersect line with circle
		if (Math.abs(Linetmp.getSlope()) < _lim.SLOPE_MAX) {
			a[1] = (float) (Linetmp.getSlope() * Linetmp.getSlope() + 1);
			a[2] = (float) (-2 * Cirtmp.x + 2 * Linetmp.getSlope() * (Linetmp.getQ() - Cirtmp.y));
			a[3] = (float) (Cirtmp.x * Cirtmp.x + (Linetmp.getQ() - Cirtmp.y) * (Linetmp.getQ() - Cirtmp.y) - Cirtmp.getV() * Cirtmp.getV());
			if (mathOp.Solve2p(a, res)) {
				index = 0;
				if (res[0].getImag() == 0.0) {
					tmpPoint[index].x = res[0].getReal();
					tmpPoint[index].y = Linetmp.getSlope() * tmpPoint[index].x + Linetmp.getQ();
					index++;
				}
				if (res[1].getImag() == 0.0) {
					tmpPoint[index].x = res[1].getReal();
					tmpPoint[index].y = Linetmp.getSlope() * tmpPoint[index].x + Linetmp.getQ();
					index++;
				}
				if (index == 1) {
					tmpPoint[1] = tmpPoint[0];
					return (2);
				}
				if (index == 0) {
					//printf("INFO: circle and line do not have intersection\n");
					return (0);
				}
			} else {
				System.out.println("Error: linear equation for circle\n");
				return 1;
			}
		} else {
			tmpPoint[0].x = Linetmp.getPts1().x;
			tmpPoint[0].y = Cirtmp.getV() * Cirtmp.getV() - (tmpPoint[0].x - Cirtmp.x) * (tmpPoint[0].x - Cirtmp.x);
			if (tmpPoint[0].y >= 0) {
				tmpPoint[0].y = Math.sqrt(tmpPoint[0].y) + Cirtmp.y;
			} else {
				return 1;
			}

			tmpPoint[1].x = Linetmp.getPts1().x;
			tmpPoint[1].y = -(tmpPoint[0].y - Cirtmp.y) + Cirtmp.y;
			index = 2;
		}
		return 0;
	}

	///////////////////////////////////////////////////////////////////////
	// intersection of a rectangular box and a circle
	///////////////////////////////////////////////////////////////////////
	/**
	 * intersection of a rectangular box and a circle
	 * 
	 * @param area
	 *            SubAreaFloat = box definition
	 * @param Cir
	 *            ImPoint = circle definition
	 * @param inter
	 *            ImPoint[] = intersecting points
	 * @return int = number of intersecting points
	 */
	public int boxIntersectCircle(SubAreaFloat area, ImPoint Cir, ImPoint[] inter) {
		int ret, rv = 1;
		int count, index, rem;
		double alpha, d, d1;
		ImLine Linetmp = new ImLine();
		ImPoint[] pts = new ImPoint[2];
		pts[0] = new ImPoint();
		pts[1] = new ImPoint();
		ImPoint ptsc = new ImPoint();
		ImPoint[] ptstemp = new ImPoint[1];
		ptstemp[0] = new ImPoint();

		count = 0; // the number of intersections BoxCircle is returned
		ptsc.x = area.getRow() + area.getHeight() * 0.5; // center of rotation
		ptsc.y = area.getCol() + area.getWidth() * 0.5;
		alpha = area.getAngle() * Math.PI / 180.0;

		// x=Row line
		ptstemp[0].x = area.getRow();
		ptstemp[0].y = area.getCol();
		rotatePoints(ptsc, ptstemp, 1, alpha);
		Linetmp.setPts1(ptstemp[0]);
		ptstemp[0].x = area.getRow();
		ptstemp[0].y = area.getCol() + area.getWidth();
		rotatePoints(ptsc, ptstemp, 1, alpha);
		Linetmp.setPts2(ptstemp[0]);
		Linetmp.computeSlopeFromPts();
		ret = lineIntersectCircle(Linetmp, Cir, pts);

		if ((ret == 0) || (ret == 2)) {
			//select the point inside of line segment
			d = -1;
			index = 0;
			d1 = distance(Linetmp.getPts1(), Linetmp.getPts2());
			d = distance(pts[0], Linetmp.getPts2());
			if (d <= d1) {
				d = distance(pts[0], Linetmp.getPts1());
				if (d <= d1) {
					inter[count] = pts[0];
					inter[count].setV(1.0);
					count++;
				}
			}
			if (ret != 2) {
				d = distance(pts[1], Linetmp.getPts2());
				index = 1;
			}
			if ((index == 1) && (d <= d1)) {
				d = distance(pts[1], Linetmp.getPts1());
				if (d <= d1) {
					inter[count] = pts[index];
					inter[count].setV(1.0);
					count++;
				}
			}
		}
		if (count == 2) {
			rv = 2;
		}

		// x=Row+High line
		ptstemp[0].x = area.getRow() + area.getHeight();
		ptstemp[0].y = area.getCol();
		rotatePoints(ptsc, ptstemp, 1, alpha);
		Linetmp.setPts1(ptstemp[0]);
		ptstemp[0].x = area.getRow() + area.getHeight();
		ptstemp[0].y = area.getCol() + area.getWidth();
		rotatePoints(ptsc, ptstemp, 1, alpha);
		Linetmp.setPts2(ptstemp[0]);
		Linetmp.computeSlopeFromPts();
		ret = lineIntersectCircle(Linetmp, Cir, pts);

		rem = count;
		if ((ret == 0) || (ret == 2)) {
			//select the point inside of line segment
			d = -1;
			index = 0;
			d1 = distance(Linetmp.getPts1(), Linetmp.getPts2());
			d = distance(pts[0], Linetmp.getPts2());
			if (d <= d1) {
				d = distance(pts[0], Linetmp.getPts1());
				if (d <= d1) {
					inter[count] = pts[0];
					inter[count].setV(2.0);
					count++;
				}
			}
			if (ret != 2) {
				d = distance(pts[1], Linetmp.getPts2());
				index = 1;
			}
			if ((index == 1) && (d <= d1)) {
				d = distance(pts[1], Linetmp.getPts1());
				if (d <= d1) {
					inter[count] = pts[index];
					inter[count].setV(2.0);
					count++;
				}
			}
		}
		if (rem + 2 == count) {
			rv = 2;
		}

		// y=Col line
		ptstemp[0].x = area.getRow();
		ptstemp[0].y = area.getCol();
		rotatePoints(ptsc, ptstemp, 1, alpha);
		Linetmp.setPts1(ptstemp[0]);
		ptstemp[0].x = area.getRow() + area.getHeight();
		ptstemp[0].y = area.getCol();
		rotatePoints(ptsc, ptstemp, 1, alpha);
		Linetmp.setPts2(ptstemp[0]);
		Linetmp.computeSlopeFromPts();
		ret = lineIntersectCircle(Linetmp, Cir, pts);

		rem = count;
		if ((ret == 0) || (ret == 2)) {
			//select the point inside of line segment
			d = -1;
			index = 0;
			d1 = distance(Linetmp.getPts1(), Linetmp.getPts2());
			d = distance(pts[0], Linetmp.getPts2());
			if (d <= d1) {
				d = distance(pts[0], Linetmp.getPts1());
				if (d <= d1) {
					inter[count] = pts[0];
					inter[count].setV(3.0);
					count++;
				}
			}
			if (ret != 2) {
				d = distance(pts[1], Linetmp.getPts2());
				index = 1;
			}
			if ((index == 1) && (d <= d1)) {
				d = distance(pts[1], Linetmp.getPts1());
				if (d <= d1) {
					inter[count] = pts[index];
					inter[count].setV(3.0);
					count++;
				}
			}
		}
		if (rem + 2 == count) {
			rv = 2;
		}

		// y=Col +Wide line
		ptstemp[0].x = area.getRow();
		ptstemp[0].y = area.getCol() + area.getWidth();
		rotatePoints(ptsc, ptstemp, 1, alpha);
		Linetmp.setPts1(ptstemp[0]);
		ptstemp[0].x = area.getRow() + area.getHeight();
		ptstemp[0].y = area.getCol() + area.getWidth();
		rotatePoints(ptsc, ptstemp, 1, alpha);
		Linetmp.setPts2(ptstemp[0]);
		Linetmp.computeSlopeFromPts();
		ret = lineIntersectCircle(Linetmp, Cir, pts);

		rem = count;
		if ((ret == 0) || (ret == 2)) {
			//select the point inside of line segment
			d = -1;
			index = 0;
			d1 = distance(Linetmp.getPts1(), Linetmp.getPts2());
			d = distance(pts[0], Linetmp.getPts2());
			if (d <= d1) {
				d = distance(pts[0], Linetmp.getPts1());
				if (d <= d1) {
					inter[count] = pts[0];
					inter[count].setV(4.0);
					count++;
				}
			}
			if (ret != 2) {
				d = distance(pts[1], Linetmp.getPts2());
				index = 1;
			}
			if ((index == 1) && (d <= d1)) {
				d = distance(pts[1], Linetmp.getPts1());
				if (d <= d1) {
					inter[count] = pts[index];
					inter[count].setV(4.0);
					count++;
				}
			}
		}
		if ((rem + 2) == count) {
			rv = 2;
		}

//    num=count;
		return (rv);
	}

	//////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////
	/**
	 * find the largest rotated region that fits to an image and includes the
	 * image center point input region(Angle) and image(numrows,numcols) fit is
	 * performed either as a square to image or rectagle with the same aspect
	 * ratio as the image to original image
	 * 
	 * @param numrows
	 *            int = row size of the image to be rotated
	 * @param numcols
	 *            int = column size of the image to be rotated
	 * @param region
	 *            SubAreaFloat = the resulting box that would include rotated
	 *            image
	 * @param option
	 *            int = 0 means fit to a square and 1 means fit to a rectangle
	 *            box
	 * @return boolean = success of operation
	 */
	public boolean fitRotRegionToImage(int numrows, int numcols, SubAreaFloat region, int option) {
		if ((region.getAngle() < 0) || (region.getAngle() >= 360)) {
			System.out.println("Error: angle should be in [0,360) ");
			//    return false;
		}

		if (region.getAngle() == 90.0F) {
			region.setRow(numrows - 1);
			region.setCol(1);
			region.setHeight(numcols - 2);
			region.setWidth(numrows - 2);
			return true;
		}
		if (region.getAngle() == 180.0F) {
			region.setRow(numrows - 1);
			region.setCol(numcols - 1);
			region.setHeight(numrows - 2);
			region.setWidth(numcols - 2);
			return true;
		}
		if (region.getAngle() == 270.0F) {
			region.setRow(1);
			region.setCol(numcols - 1);
			region.setHeight(numcols - 2);
			region.setWidth(numrows - 2);
			return true;
		}

		float angle = (float) Math.IEEEremainder(region.getAngle(), 90.0); // fmod(x,y)
		System.out.println(" angle = " + angle);
		if (angle == 0.0F) {
			region.setRow(1);
			region.setCol(1);
			region.setHeight(numrows - 2);
			region.setWidth(numcols - 2);
			region.setAngle(angle);
			return true;
		}

		double alpha = angle * _lim.Deg2Rad;
		//  region.getAngle() = angle;// this is important for plotting

		double d, xc, sina, cosa;
		sina = Math.sin(alpha);
		cosa = Math.cos(alpha);

		switch (option) {
		case 0: //fit a square
			if (numrows > numcols) {
				d = numcols;
			} else {
				d = numrows;
			}
			xc = d * sina / (sina + cosa);
			System.out.println(" x offset = " + xc);

			region.setRow((float) xc);
			region.setCol(0.0F);
			region.setHeight((float) ((d - xc) / cosa));
			region.setWidth((float) (xc / sina));
			//now offset to the middle
			if (numrows > numcols) {
				region.setRow(region.getRow() + (float) ((numrows - (region.getHeight() * cosa + region.getWidth() * sina)) * 0.5));
			} else {
				region.setCol(region.getCol() + (float) ((numcols - (region.getHeight() * sina + region.getWidth() * cosa)) * 0.5));
			}

			break;
		case 1: // fit and preserve aspect ratio of original
			if (numrows > numcols) {
				xc = (float) numrows * (float) numcols * sina / (numrows * sina + numcols * cosa);
				System.out.println(" y offset = " + xc);
				//compute intersection
				// and shift the point;
				ImLine line1 = new ImLine();
				ImLine lineIm1 = new ImLine();
				ImPoint pts = new ImPoint();

				line1.getPts1().x = numrows;
				line1.getPts1().y = xc;
				line1.setSlope(Math.tan(alpha));
				line1.setQ(line1.getPts1().y - line1.getSlope() * line1.getPts1().x);
				line1.getPts2().x = line1.getPts1().x + 1.0;
				line1.getPts2().y = line1.getPts2().x * line1.getSlope() + line1.getQ();

				//vert left
				lineIm1.getPts1().x = 0.0;
				lineIm1.getPts1().y = 0.0;
				lineIm1.getPts2().x = numrows;
				lineIm1.getPts2().y = 0.0;
				lineIm1.computeSlopeFromPts();

				if (!lineIntersectf(line1, lineIm1, pts)) {
					return false;
				}
				region.setRow((float) pts.x);
				region.setCol(0.0F);

				region.setWidth((float) ((numcols - xc) / cosa - 1.0));
				region.setHeight((float) (xc / sina - 1.0));
				//now offset to the middle
				region.setRow(region.getRow() - (float) ((numrows - (region.getHeight() * cosa + region.getWidth() * sina)) * 0.5));

			} else {
				xc = (float) numrows * (float) numcols * sina / (numcols * sina + numrows * cosa);
				System.out.println(" x offset = " + xc);

				region.setRow((float) xc);
				region.setCol(0.0F);
				region.setWidth((float) (xc / sina - 1.0));
				region.setHeight((float) ((numrows - xc) / cosa - 1.0));
				//now offset to the middle
				region.setCol(region.getCol() + (float) ((numcols - (region.getHeight() * sina + region.getWidth() * cosa)) * 0.5));
			}

			break;
		default:
			System.out.println(" ERROR: option to fit does not exist");
			break;
		}

		//if the Angle < 90 then the (row,col) point is on the left vert image border
		if (region.getAngle() > angle) {
			ImPoint pts = new ImPoint();
			ImPoint ptsc = new ImPoint();
			ptsc.x = region.getRow();
			ptsc.y = region.getCol();
			pts.SetImPoint(ptsc);
			if (region.getAngle() - angle == 90.0) {
				//the (row,col) point will be on the lower horizontal image border
				pts.x += region.getHeight();
				rotatePoint(ptsc, pts, alpha);
				region.setRow((float) pts.x);
				region.setCol((float) pts.y);
				// swap high and wide
				d = region.getHeight();
				region.setHeight(region.getWidth());
				region.setWidth((float) d);
			} else {
				if (region.getAngle() - angle == 180.0) {
					//the (row,col) point will be on the right vert image border
					pts.x += Math.sqrt(region.getHeight() * region.getHeight() + region.getWidth() * region.getWidth());
					float beta = (float) Math.atan2(region.getWidth(), region.getHeight());
					rotatePoint(ptsc, pts, (alpha + beta));
					region.setRow((float) pts.x);
					region.setCol((float) pts.y);
				} else {
					if (region.getAngle() - angle == 270.0) {
						//the (row,col) point will be on the lower horizontal image border
						//  region.getCol() = tan(alpha) * (numrows - region.getRow());
						//region.getRow() = numrows-1;
						pts.y += region.getWidth();
						rotatePoint(ptsc, pts, alpha);
						region.setRow((float) pts.x);
						region.setCol((float) pts.y);
						// swap high and wide
						d = region.getHeight();
						region.setHeight(region.getWidth());
						region.setWidth((float) d);
					} else {
						System.out.println("Warning: Angle>360, region might not be properly positioned");
					}
				}
			}

		}
		return false;
	}

	//////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////
	/**
	 * find the largest rotated region that encloses an image at a given
	 * rotation angle input region(Angle) and image(numrows,numcols)
	 * 
	 * @param numrows
	 *            int = row size of the input image
	 * @param numcols
	 *            int = column size of the input image
	 * @param region
	 *            SubAreaFloat = box that includes rotated image
	 * @return boolean = success of operation
	 */
	public boolean fitImageToRotRegion(int numrows, int numcols, SubAreaFloat region) {
		if ((region.getAngle() < 0) || (region.getAngle() >= 360)) {
			System.out.println("Error: angle should be in [0,360) ");
			;
			//    return false;
		}

		if (region.getAngle() == 0.0F) {
			region.setRow(0);
			region.setCol(0);
			region.setHeight(numcols - 1);
			region.setWidth(numrows - 1);
			return true;
		}
		if (region.getAngle() == 90.0F) {
			region.setRow(numrows - 1);
			region.setCol(1);
			region.setHeight(numcols - 2);
			region.setWidth(numrows - 2);
			return true;
		}
		if (region.getAngle() == 180.0F) {
			region.setRow(numrows - 1);
			region.setCol(numcols - 1);
			region.setHeight(numrows - 2);
			region.setWidth(numcols - 2);
			return true;
		}
		if (region.getAngle() == 270.0F) {
			region.setRow(1);
			region.setCol(numcols - 1);
			region.setHeight(numcols - 2);
			region.setWidth(numrows - 2);
			return true;
		}

		ImLine line = new ImLine();
		ImPoint pts = new ImPoint();
		ImPoint pts1 = new ImPoint();

		// image center point
		pts.x = (numrows >> 1);
		pts.y = (numcols >> 1);

		// line crossing (0,0)
		double alpha = region.getAngle() * _lim.Deg2Rad;
		line.getPts1().x = 0;
		line.getPts1().y = 0;
		if (((region.getAngle() > 90) && (region.getAngle() < 180)) || ((region.getAngle() > 270) && (region.getAngle() < 360))) {
			line.setSlope(Math.tan(alpha));
		} else {
			line.setSlope(Math.tan(alpha + Math.PI * 0.5));
		}
		if (Math.abs(line.getSlope()) > _lim.SLOPE_MAX) {
			line.setSlope(_lim.SLOPE_MAX);
			line.setQ(line.getPts1().x);
			line.getPts2().x = line.getPts1().x;
			line.getPts2().y = line.getPts1().y + 1;
		} else {
			line.setQ(line.getPts1().y - line.getSlope() * line.getPts1().x);
			line.getPts2().x = line.getPts1().x + 1.0;
			line.getPts2().y = line.getPts2().x * line.getSlope() + line.getQ();
		}
		double drow = distLineToPoint(line, pts);

		// perpendicular line crossing (numrows,0)
		line.getPts1().x = numrows - 1;
		line.getPts1().y = 0;
		if (((region.getAngle() > 90) && (region.getAngle() < 180)) || ((region.getAngle() > 270) && (region.getAngle() < 360))) {
			line.setSlope(Math.tan(alpha + Math.PI * 0.5));
		} else {
			line.setSlope(Math.tan(alpha));
		}
		if (Math.abs(line.getSlope()) > _lim.SLOPE_MAX) {
			line.setSlope(_lim.SLOPE_MAX);
			line.setQ(line.getPts1().x);
			line.getPts2().x = line.getPts1().x;
			line.getPts2().y = line.getPts1().y + 1;
		} else {
			line.setQ(line.getPts1().y - line.getSlope() * line.getPts1().x);
			line.getPts2().x = line.getPts1().x + 1.0;
			line.getPts2().y = line.getPts2().x * line.getSlope() + line.getQ();
		}
		double dcol = distLineToPoint(line, pts);
		// rotate the new point to compute the left upper corner
		// of the neclosing region
		pts1.x = pts.x - drow;
		pts1.y = pts.y - dcol;
		rotatePoint(pts, pts1, alpha);

		// result
		region.setRow((float) pts1.x);
		region.setCol((float) pts1.y);
		region.setHeight((float) (2 * drow));
		region.setWidth((float) (2 * dcol));

		return true;
	}

	///////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////
	/**
	 * find distance between a point (x1,y1) and a line dist = (a*x1 +b*y1 +
	 * c)/sqrt(a^2+b^2)
	 * 
	 * @param line
	 *            ImLine = line definition
	 * @param pts
	 *            ImPoint = point definition
	 * @return double = computed distance
	 */
	public double distLineToPoint(ImLine line, ImPoint pts) {
		double dist;

		if (Math.abs(line.getSlope()) == _lim.SLOPE_MAX) {
			dist = Math.abs(pts.x - line.getQ());
		} else {
			if (Math.abs(line.getSlope()) > _lim.EPSILON) {
				dist = Math.abs((line.getSlope() * pts.x - pts.y + line.getQ()) / (Math.sqrt(line.getSlope() * line.getSlope() + 1)));
			} else {
				dist = Math.abs(line.getQ() - pts.y);
			}
		}
		return dist;
	}

	///////////////////////////////////////////////////////////////////////
	// find symmetric point to a given point (x1,y1) around a line
	///////////////////////////////////////////////////////////////////////
	/**
	 * find symmetric point to a given point (x1,y1) around a line
	 * 
	 * @param line
	 *            ImLine = line of symmetry
	 * @param ptsIn
	 *            ImPoint = input point
	 * @return ImPoint = symmetric point
	 */
	public ImPoint symPointAroundLine(ImLine line, ImPoint ptsIn) {

		//double dist = DistLineToPoint(line,ptsIn);
		ImPoint ptsOut = new ImPoint();
/*
 * // if point is on the line if( dist < _lim.EPSILON3){ // return the point on
 * the line ptsOut.SetImPoint(ptsIn.x,ptsIn.y); return ptsOut; }
 */
		//////////////////
		if (Math.abs(line.getSlope()) == _lim.SLOPE_MAX) {
			ptsOut.SetImPoint((line.getQ() + (line.getQ() - ptsIn.x)), ptsIn.y);
		} else {
			if (Math.abs(line.getSlope()) > _lim.EPSILON) {
				double slopePerp, qPerp;
				slopePerp = -1.0 / line.getSlope();
				qPerp = ptsIn.y - slopePerp * ptsIn.x;
				ImPoint ptLine = new ImPoint();
				ptLine.x = (qPerp - line.getQ()) / (line.getSlope() - slopePerp);
				ptLine.y = slopePerp * ptLine.x + qPerp;
				ptsOut.SetImPoint((ptLine.x + (ptLine.x - ptsIn.x)), (ptLine.y + (ptLine.y - ptsIn.y)));
			} else {
				ptsOut.SetImPoint(ptsIn.x, (line.getQ() + (line.getQ() - ptsIn.y)));
			}
		}
		return ptsOut;
	}

	////////////////////////////////////////////////////////////
	/**
	 * display values
	 */
	public void printGeomOper() {
		//System.out.println("GeomOper Info :numrows=" + numrows+ " numcols=" + numcols + " sampPerPixel=" + sampPerPixel);
		System.out.println("GeomOper Info :");
	}

}
