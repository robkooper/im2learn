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
/*
 * Created on Sep 3, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.illinois.ncsa.isda.imagetools.ext.segment;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImLine;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImPoint;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.imagetools.core.datatype.LimitValues;
import edu.illinois.ncsa.isda.imagetools.core.datatype.Point3DDouble;
import edu.illinois.ncsa.isda.imagetools.core.datatype.SubAreaFloat;
import edu.illinois.ncsa.isda.imagetools.ext.math.GeomOper;

/**
 * @author yjlee <p/> TODO To change the template for this generated type
 *         comment go to Window - Preferences - Java - Code Style - Code
 *         Templates
 */
public class DrawOp {
	GeomOper			_GeomOper		= new GeomOper();
	private static Log	logger			= LogFactory.getLog(DrawOp.class);

	private boolean		_debugDrawOper	= true;

	public DrawOp() {
		_debugDrawOper = true;
	}

	///////////////////////////////////////////////////////
	public boolean getDebug() {
		return _debugDrawOper;
	}

	public void setDebug(boolean val) {
		_debugDrawOper = val;
	}

	//////////////////////////////////////////////////////////////////////////////
	// graphics operations
	// draws a line across rotated rectangular box
	// outputs the line end points after interesecting two sides of the box
	// output store in Linefinal
	// input in Linetmp and Area(contains angle)
	/////////////////////////////////////////////////////////////////////////////
	public int draw_lineacrossbox(ImPoint limitmin, ImPoint limitmax, SubAreaFloat area, ImLine Linetmp, ImLine Linefinal) {
		int i, count = 0;
		ImLine linetmp1 = new ImLine();
		ImPoint[] pts = new ImPoint[4];

		for (i = 0; i < 4; i++) {
			pts[i] = new ImPoint();
		}

		ImPoint[] ptstemp = new ImPoint[2];
		for (i = 0; i < 2; i++) {
			ptstemp[i] = new ImPoint();
		}

		ImPoint ptsc = new ImPoint();
		double alpha;//,d;
		boolean ret = true;
		float min_dist = 1.0F;

		ptsc.setX(area.getRow()); // center of rotation
		ptsc.setY(area.getCol());
		alpha = Math.toRadians(area.getAngle());

		// init pts
		pts[0].setX(-1.0);
		pts[0].setY(-1.0);
		pts[1].setX(-1.0);
		pts[1].setY(-1.0);

		// x= Row line (Col + Wide line)

		linetmp1.setPts1(ptsc);
		ptstemp[0].setX(area.getRow());
		ptstemp[0].setY(area.getCol() + area.getWidth());
		_GeomOper.rotatePoints(ptsc, ptstemp, 1, alpha);

		linetmp1.setPts2(ptstemp[0]);
		linetmp1.computeSlopeFromPts();
		_GeomOper.setDebug(true);

		ret = _GeomOper.lineIntersectf(Linetmp, linetmp1, pts[count]);

		if ((ret != true) || (pts[count].getY() < limitmin.getY()) || (pts[count].getY() > limitmax.getY()) || (pts[count].getX() < limitmin.getX()) || (pts[count].getX() > limitmax.getX())) {
		} else {
			count++;
		}

		// y=Col +Wide line
		ptstemp[0].setX(area.getRow());
		ptstemp[0].setY(area.getCol() + area.getWidth());
		_GeomOper.rotatePoints(ptsc, ptstemp, 1, alpha);

		linetmp1.setPts1(ptstemp[0]);
		ptstemp[0].setX(area.getRow() + area.getHeight());
		ptstemp[0].setY(area.getCol() + area.getWidth());
		_GeomOper.rotatePoints(ptsc, ptstemp, 1, alpha);

		linetmp1.setPts2(ptstemp[0]);
		linetmp1.computeSlopeFromPts();

		ret = _GeomOper.lineIntersectf(Linetmp, linetmp1, pts[count]);
		if ((ret != true) || (pts[count].getY() < limitmin.getY()) || (pts[count].getY() > limitmax.getY()) || (pts[count].getX() < limitmin.getX()) || (pts[count].getX() > limitmax.getX())) {
		} else {
			if (count == 0) {
				count++;
			} else {
				if ((Math.abs(pts[count - 1].getX() - pts[count].getX()) > min_dist) || (Math.abs(pts[count - 1].getY() - pts[count].getY()) > min_dist)) {
					count++;
				}
			}
		}

		// x=Row+High line
		ptstemp[0].setX(area.getRow() + area.getHeight());
		ptstemp[0].setY(area.getCol());
		_GeomOper.rotatePoints(ptsc, ptstemp, 1, alpha);

		linetmp1.setPts1(ptstemp[0]);
		ptstemp[0].setX(area.getRow() + area.getHeight());
		ptstemp[0].setY(area.getCol() + area.getWidth());
		_GeomOper.rotatePoints(ptsc, ptstemp, 1, alpha);

		linetmp1.setPts2(ptstemp[0]);
		linetmp1.computeSlopeFromPts();

		ret = _GeomOper.lineIntersectf(Linetmp, linetmp1, pts[count]);
		if ((ret != true) || (pts[count].getY() < limitmin.getY()) || (pts[count].getY() > limitmax.getY()) || (pts[count].getX() < limitmin.getX()) || (pts[count].getX() > limitmax.getX())) {
		} else {
			if (count == 0) {
				count++;
			} else {
				if ((Math.abs(pts[count - 1].getX() - pts[count].getX()) > min_dist) || (Math.abs(pts[count - 1].getY() - pts[count].getY()) > min_dist)) {
					count++;
				}
			}
		}

		// y=Col line
		linetmp1.setPts1(ptsc);
		ptstemp[0].setX(area.getRow() + area.getHeight());
		ptstemp[0].setY(area.getCol());
		_GeomOper.rotatePoints(ptsc, ptstemp, 1, alpha);

		linetmp1.setPts2(ptstemp[0]);
		linetmp1.computeSlopeFromPts();

		ret = _GeomOper.lineIntersectf(Linetmp, linetmp1, pts[count]);
		if ((ret != true) || (pts[count].getY() < limitmin.getY()) || (pts[count].getY() > limitmax.getY()) || (pts[count].getX() < limitmin.getX()) || (pts[count].getX() > limitmax.getX())) {
		} else {
			if (count == 0) {
				count++;
			} else {
				if ((Math.abs(pts[count - 1].getX() - pts[count].getX()) > min_dist) || (Math.abs(pts[count - 1].getY() - pts[count].getY()) > min_dist)) {
					count++;
				}
			}
		}

		if (count >= 2) {
			double d1, d2, d3;
			if (count > 3) {
				logger.debug("a line has more than 3 intersections with a rotated box");
				count = 0;
			}
			if (count == 3) {
				//select the largest distance
				d1 = _GeomOper.distance(pts[0], pts[1]);
				d2 = _GeomOper.distance(pts[0], pts[2]);
				d3 = _GeomOper.distance(pts[1], pts[2]);
				if (d1 > d2) {
					if (d1 > d3) {
						//select pts[0] and pts[1]
						;
					} else {
						//select pts[1] and pts[2]
						pts[0] = pts[1];
						pts[1] = pts[2];
					}
				} else {
					if (d2 > d3) {
						//select pts[0] and pts[2]
						pts[1] = pts[2];
					} else {
						//select pts[1] and pts[2]
						pts[0] = pts[1];
						pts[1] = pts[2];
					}
				}
				count = 2;
			}

			if (count == 2) {
				Linefinal.setPts1(pts[0]);
				Linefinal.setPts2(pts[1]);

				if (Math.abs(Linefinal.getPts2().getX() - Linefinal.getPts1().getX()) < 1) {
					Linefinal.setSlope(LimitValues.SLOPE_MAX);
					Linefinal.setQ(Linefinal.getPts1().getX());
				} else {
					Linefinal.setSlope((Linefinal.getPts2().getY() - Linefinal.getPts1().getY()) / (Linefinal.getPts2().getX() - Linefinal.getPts1().getX()));
					Linefinal.setQ(Linefinal.getPts1().getY() - Linefinal.getSlope() * Linefinal.getPts1().getX());
				}
			}
		}

		if (count == 1) {
			Linefinal.setPts1(pts[0]);
			Linefinal.setPts2(pts[0]);
		}

		return (count);
	}// end of draw_lineacrossbox

	//////////////////////////////////////////////////////////////////////////////
	// graphics operations
	// draws a line across a triangle
	// outputs the line end points after interesecting two sides of the triangle
	// output store in Linefinal
	// input in Linetmp and three points
	/////////////////////////////////////////////////////////////////////////////
	public int draw_lineacrosstriangle(ImPoint limitmin, ImPoint limitmax, ImPoint[] ptsIn, ImLine Linetmp, ImLine Linefinal) {
		int i, count = 0;
		ImLine Linetmp1 = new ImLine();
		ImPoint[] pts = new ImPoint[2];

		for (i = 0; i < 2; i++) {
			pts[i] = new ImPoint();
		}

		boolean ret = true;

		// init pts
		pts[0].setX(-1.0);
		pts[0].setY(-1.0);
		pts[1].setX(-1.0);
		pts[1].setY(-1.0);

		int j;
		_GeomOper.setDebug(true); //for the case of parallel lines

		for (i = 0; (count < 2) && (i < 3); i++) {
			if (i == 2) {
				j = 0;
			} else {
				j = i + 1;
			}

			// pts[i] and pts[j] line
			Linetmp1.setPts1(ptsIn[i]);
			Linetmp1.setPts2(ptsIn[j]);
			Linetmp1.computeSlopeFromPts();

			ret = _GeomOper.lineIntersectf(Linetmp, Linetmp1, pts[count]);

			// test whether the point is inside or outside of a triangle
			if (ret && _GeomOper.isPointInsideTriangle(ptsIn, pts[count])) {
				if (count > 0) {
					//check if the point is different from the previous
					// which happens if the intersection point coincides with
					// one of the three points
					if ((Math.abs(pts[0].getX() - pts[1].getX()) > 0.5) || (Math.abs(pts[0].getY() - pts[1].getY()) > 0.5)) {
						count++;
					}
				} else {
					count++;
				}
			}
		}

		if (count == 2) {
			if ((Math.abs(pts[0].getX() - pts[1].getX()) < 0.5) && (Math.abs(pts[0].getY() - pts[1].getY()) < 0.5)) {
				count = 1;
			} else {
				Linefinal.setPts1(pts[0]);
				Linefinal.setPts2(pts[1]);

				if (Math.abs(Linefinal.getPts2().getX() - Linefinal.getPts1().getX()) < 1) {
					Linefinal.setSlope(LimitValues.SLOPE_MAX);
					Linefinal.setQ(Linefinal.getPts1().getX());
				} else {
					Linefinal.setSlope((Linefinal.getPts2().getY() - Linefinal.getPts1().getY()) / (Linefinal.getPts2().getX() - Linefinal.getPts1().getX()));
					Linefinal.setQ(Linefinal.getPts1().getY() - Linefinal.getSlope() * Linefinal.getPts1().getX());
				}
			}
		}

		if (count == 1) {
			Linefinal.setPts1(pts[0]);
			Linefinal.setPts2(pts[0]);
		}

		return (count);
	}// end of draw_lineacrosstriangle

	///////////////////////////////////////////////////////////////
	// draws a line by first intersecting the line with all four borders
	// of the image and then calling plot_line that colors pixels between
	// the two intersecting points of a line and the image
	///////////////////////////////////////////////////////////////
	public boolean draw_line(ImageObject imageOut, double color, int thickness, ImLine Linetmp) {
		int numrows, numcols;
		numrows = imageOut.getNumRows();
		numcols = imageOut.getNumCols();

		int flag = 0;
		int count = 0;
		int i, flag1;
		ImLine Linetmp1 = new ImLine();
		ImLine Linefinal = new ImLine();
		ImPoint[] pts = new ImPoint[2];

		for (i = 0; i < 2; i++) {
			pts[i] = new ImPoint();
		}

		if ((Linetmp.getPts1().getX() - thickness < 0) || (Linetmp.getPts1().getX() + thickness >= numrows)) {
			flag = 1;
		}

		if ((flag == 0) && ((Linetmp.getPts1().getY() - thickness < 0) || (Linetmp.getPts1().getY() + thickness >= numcols))) {
			flag += 2;
		}

		if ((flag == 0) && ((Linetmp.getPts2().getX() - thickness < 0) || (Linetmp.getPts2().getX() + thickness >= numrows))) {
			flag += 4;
		}

		if ((flag == 0) && ((Linetmp.getPts2().getY() - thickness < 0) || (Linetmp.getPts2().getY() + thickness >= numcols))) {
			flag += 8;
		}

		if ((Linetmp.getPts1().getX() == Linetmp.getPts2().getX()) && (Linetmp.getPts1().getY() == Linetmp.getPts2().getY())) {
			flag = 0;
		}

		if (flag != 0) {
			// printf("ERROR: line plus line thickness are out of range \n");
			// printf("would you like to see the intersection with the image
			// area (1..yes)\n");
			flag1 = 0;
			// scanf("%d",&flag1);
			flag1 = 1;

			if (flag1 == 1) {
				// x=0 line
				Linetmp1.getPts1().setX(0);
				Linetmp1.getPts1().setY(0);
				Linetmp1.getPts2().setX(0);
				Linetmp1.getPts2().setY(numcols - 1);
				Linetmp1.setSlope(LimitValues.SLOPE_MAX);
				Linetmp1.setQ(0);

				_GeomOper.lineIntersectf(Linetmp, Linetmp1, pts[count]);

				if ((pts[count].getY() >= 0) && (pts[count].getY() < numcols)) {
					count += 1;
				}

				// x=numrows-2 line
				Linetmp1.getPts1().setX(numrows - 2);
				Linetmp1.getPts1().setY(0);
				Linetmp1.getPts2().setX(numrows - 2);
				Linetmp1.getPts2().setY(numcols - 1);
				Linetmp1.setSlope(LimitValues.SLOPE_MAX);
				Linetmp1.setQ(numrows - 2);

				_GeomOper.lineIntersectf(Linetmp, Linetmp1, pts[count]);

				if ((pts[count].getY() >= 0) && (pts[count].getY() < numcols)) {
					count += 1;
				}

				if (count < 2) {
					// y=0 line
					Linetmp1.getPts1().setX(0);
					Linetmp1.getPts1().setY(0);
					Linetmp1.getPts2().setX(numrows - 1);
					Linetmp1.getPts2().setY(0);
					Linetmp1.setSlope(0.00001);
					Linetmp1.setQ(0);

					_GeomOper.lineIntersectf(Linetmp, Linetmp1, pts[count]);

					if ((pts[count].getX() >= 0) && (pts[count].getX() < numrows)) {
						count += 1;
					}

					if (count < 2) {
						// y=numcols-2 line
						Linetmp1.getPts1().setX(0);
						Linetmp1.getPts1().setY(numcols - 2);
						Linetmp1.getPts2().setX(numrows - 1);
						Linetmp1.getPts2().setY(numcols - 2);
						Linetmp1.setSlope(0.00001);
						Linetmp1.setQ(0.0);

						_GeomOper.lineIntersectf(Linetmp, Linetmp1, pts[count]);

						if ((pts[count].getX() >= 0) && (pts[count].getX() < numrows)) {
							count += 1;
						}

						if (count < 2) {
							logger.info("No intersection with the image area \n");
							//  return 1;
						}
					}
				}
			}

			if (count == 2) {
				double d, d1, d2;
				ImPoint[] orig = new ImPoint[2];
				for (i = 0; i < 2; i++) {
					orig[i] = new ImPoint();
				}

				orig[0] = Linetmp.getPts1();
				orig[1] = Linetmp.getPts2();
				//if( !(flag & 1) && !(flag & 2) ){
				if (((flag & 1) == 0) && ((flag & 2) == 0)) {
					// pts1 is inside image area
					Linefinal.setPts1(Linetmp.getPts1());

					d = _GeomOper.distance(orig[0], pts[0]);
					d1 = _GeomOper.distance(orig[1], pts[0]);
					d2 = _GeomOper.distance(orig[0], orig[1]);

					if ((d <= d2) && (d1 <= d2)) {
						Linefinal.setPts2(pts[0]);
					} else {
						Linefinal.setPts2(pts[1]);
					}
				} else {
					//if( !(flag & 4) && !(flag & 8) ){
					if (((flag & 4) == 0) && ((flag & 8) == 0)) {
						// pts2 is inside image area
						Linefinal.setPts1(Linetmp.getPts2());

						d = _GeomOper.distance(orig[0], pts[0]);
						d1 = _GeomOper.distance(orig[1], pts[0]);
						d2 = _GeomOper.distance(orig[0], orig[1]);

						if ((d <= d2) && (d1 <= d2)) {
							Linefinal.setPts2(pts[0]);
						} else {
							Linefinal.setPts2(pts[1]);
						}
					} else {
						Linefinal.setPts1(pts[0]);
						Linefinal.setPts2(pts[1]);
					}
				}
				flag = 0;
			}
		} else {
			Linefinal = Linetmp;
		}

		if (flag == 0) {
			//  plot_line(Linefinal.pts1.x,Linefinal.pts1.y,Linefinal.pts2.x,Linefinal.pts2.y,color);
			if ((color < 0) || (color > 255)) {
				color = 255;
			}

			plot_lineDouble(imageOut, Linefinal, color);

			return true;
		} else {
			return false;
		}
	}

	///////////////////////////////////////////////
	// plot a line into imageOut file
	// writes into individual pixels
	// called by draw_line
	///////////////////////////////////////////////
	/*
	 * public boolean plot_line(ImageObject imageOut,ImLine line,int color) {
	 * 
	 * if(imageOut == null){ System.out.println("ERROR: there is no image
	 * data"); return false; } if(imageOut.sampPerPixel != 1 ){
	 * System.out.println("Warning: trying to plot in grayscale image but the
	 * image is not 1D"); } if(imageOut.sampType.equalsIgnoreCase("BYTE")==false ){
	 * System.out.println("Error: only BYTE type is supported"); return false; }
	 * 
	 * int numcols;// numrows; numcols = imageOut.numcols;
	 * 
	 * float x,y,x1,y1,x2,y2,deltax,d; int i,numpix,row,col; int index;
	 * 
	 * long size = imageOut.size; x1=(float)line.pts1.x; y1=(float)line.pts1.y;
	 * x2=(float)line.pts2.x; y2=(float)line.pts2.y; line.ComputeSlopeFromPts();
	 * 
	 * d = (float)Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2)); numpix = (int)
	 * (d+0.5); // numpix should never be zero
	 * 
	 * if(Math.abs(line.slope) <_lim.SLOPE_MAX){ deltax = (x2-x1)/(float)numpix;
	 * if( Math.abs(deltax) < _lim.EPSILON3){//0.0001 System.out.println( "
	 * Error: slope\n"); return false; }
	 * if(imageOut.sampType.equalsIgnoreCase("BYTE") ){ byte colorByte =
	 * (byte)color; for(i=0;i <numpix;i++){ x = x1 + deltax*i; row = (int)
	 * (x+0.5); col = (int) (line.slope * x + line.q +0.5); index = row*numcols +
	 * col; if(index <0 || index > size){ System.out.println( "Error: outside of
	 * image area\n"); }else{ imageOut.image[index] = colorByte; } } }// end of
	 * BYTE }else{ deltax = (y2-y1)/(float)numpix; if( Math.abs(deltax) <
	 * _lim.EPSILON3 ){//0.0001 System.out.println( " Error: slope\n"); return
	 * false; } if(imageOut.sampType.equalsIgnoreCase("BYTE") ){ byte colorByte =
	 * (byte)color; for(i=0;i <numpix;i++){ y = y1 + deltax*i; col = (int) y;
	 * row = (int) line.q; index = row*numcols + col; if(index <0 || index >
	 * size){ System.out.println( "Error: outside of image area\n"); }else
	 * imageOut.image[index] = colorByte; } }// end of BYTE } return true; } //
	 * end of plot_line
	 */

	//////////////////////////////////////////////////////////////////////////////
	// graphics operations
	// draws borders of a rotated rectangular box
	/////////////////////////////////////////////////////////////////////////////
	/*
	 * public boolean draw_box(ImageObject imageOut,SubAreaFloat area,ImPoint
	 * ptsc,int color) { int numrows,numcols; numrows = imageOut.numrows;
	 * numcols = imageOut.numcols;
	 * 
	 * ImPoint limit = new ImPoint(); limit.x = numrows; limit.y = numcols; //
	 * check if rotated box fits to the image area ImPoint [] pts = new
	 * ImPoint[4]; int i; for(i=0;i <4;i++) pts[i] = new ImPoint();
	 * 
	 * if( !CheckRegion(area,ptsc,limit,pts) ) return false; // draw four lines
	 * ImLine Linefinal = new ImLine();
	 * 
	 * Linefinal.pts1 = pts[3]; Linefinal.pts2 = pts[0];
	 * plot_line(imageOut,Linefinal,color);
	 * 
	 * Linefinal.pts1 = pts[0]; Linefinal.pts2 = pts[2];
	 * plot_line(imageOut,Linefinal,color);
	 * 
	 * Linefinal.pts1 = pts[2]; Linefinal.pts2 = pts[1];
	 * plot_line(imageOut,Linefinal,color);
	 * 
	 * Linefinal.pts1 = pts[1]; Linefinal.pts2 = pts[3];
	 * plot_line(imageOut,Linefinal,color);
	 * 
	 * return true; }// end of draw_box
	 */
	//////////////////////////////////////////////////////////////////////////
	// graphics operations
	// draws a circle (borders)
	/////////////////////////////////////////////////////////////////////////////
	/*
	 * public boolean draw_circle(ImageObject imageOut,ImPoint cir,int color) {
	 * if(imageOut == null){ System.out.println("ERROR: there is no image
	 * data"); return false; } if(imageOut.sampPerPixel != 1 ){
	 * System.out.println("Warning: trying to plot in grayscale BYTE image but
	 * the image is not 1D"); }
	 * if(imageOut.sampType.equalsIgnoreCase("BYTE")==false ){
	 * System.out.println("Error: only BYTE type is supported"); return false; }
	 * int numrows,numcols; numrows = imageOut.numrows; numcols =
	 * imageOut.numcols;
	 * 
	 * int index; long size; float incr; if(cir.v <= 0.0){ System.out.println(
	 * "Error: non-positive radius"); return false; } incr = (float)(1.0/cir.v); //
	 * this is equal to perim/(2*PI)
	 * 
	 * size = numcols*numrows; float angle; int row,col;
	 * if(imageOut.sampType.equalsIgnoreCase("BYTE") ){ byte colorByte =
	 * (byte)color; for(angle = 0.0F;angle < 2 * Math.PI;angle+=incr){ row =
	 * (int) (cir.x + cir.v * Math.cos(angle) +0.5); col = (int) (cir.y + cir.v *
	 * Math.sin(angle) +0.5); index = row*numcols+col; if(index <size &&
	 * index>=0){ imageOut.image[index] = colorByte; } } }// end of BYTE
	 * 
	 * return true; }// end of draw_circle
	 */
	//////////////////////////////////////////////////////////////////////////
	// graphics operations
	// draws a circle (borders)
	/////////////////////////////////////////////////////////////////////////////
	public boolean draw_circleDouble(ImageObject imageOut, ImPoint cir, double color) {
		if (imageOut == null) {
			logger.info("ERROR: there is no image data");
			return false;
		}

		if (imageOut.getNumBands() != 1) {
			logger.info("Warning: trying to plot in grayscale FLOAT image but the image is not 1D");
		}

		int numrows, numcols;
		numrows = imageOut.getNumRows();
		numcols = imageOut.getNumCols();

		int index;
		long size;
		float incr;
		if (cir.getV() <= 0.0) {
			logger.info("Error: non-positive radius");
			return false;
		}

		incr = (float) (1.0 / cir.getV()); // this is equal to perim/(2*PI)

		size = numcols * numrows;
		float angle;
		int row, col;
		boolean signal = true;

		for (angle = 0.0F; angle < 2 * Math.PI; angle += incr) {
			row = (int) (cir.getX() + cir.getV() * Math.cos(angle) + 0.5);
			col = (int) (cir.getY() + cir.getV() * Math.sin(angle) + 0.5);
			index = row * numcols + col;

			if ((index < size) && (index >= 0)) {
				imageOut.set(index, color);
			}
		}
		signal = false;

		return true;
	}// end of draw_circleDouble

	/////////////////////////////////////////////////////////////////////////////
	// draws a circle into a color image
	/////////////////////////////////////////////////////////////////////////////
	/*
	 * public boolean draw_colorcircle(ImageObject imageOut,ImPoint
	 * cir,Point3DInt colorv) {
	 * 
	 * if(imageOut == null){ System.out.println("ERROR: there is no image
	 * data"); return false; } if(imageOut.sampPerPixel != 3 ){
	 * System.out.println("ERROR: trying to plot in color image but the image is
	 * not 3D"); return false; }
	 * if(imageOut.sampType.equalsIgnoreCase("BYTE")==false ){
	 * System.out.println("Error: only BYTE type is supported"); return false; }
	 * int numrows,numcols; numrows = imageOut.numrows; numcols =
	 * imageOut.numcols; // ImPoint limit; //limit.x = numrows; //limit.y =
	 * numcols; //if part of the circle is acceptable then this test should be
	 * omitted // check if circle fits to the image area //
	 * if(CheckCircle(cir,limit)) // return 1;
	 * 
	 * int index; long size; float incr; if(cir.v <=0.0){ System.out.println(
	 * "Error: non-positive radius"); return false; } incr = (float)(1.0/cir.v); //
	 * this is equal to perim/(2*PI)
	 * 
	 * size = imageOut.size; float angle; int row,col;
	 * if(imageOut.sampType.equalsIgnoreCase("BYTE") ){ byte colorRed =
	 * (byte)colorv.ptsInt[0]; byte colorGreen = (byte)colorv.ptsInt[1]; byte
	 * colorBlue = (byte)colorv.ptsInt[2]; for(angle = 0.0F;angle < 2 *
	 * Math.PI;angle+=incr){ row = (int) (cir.x + cir.v * Math.cos(angle) +0.5);
	 * col = (int) (cir.y + cir.v * Math.sin(angle) +0.5); index =
	 * (row*numcols+col)*imageOut.sampPerPixel; if(index <size && index>=0){
	 * imageOut.image[index] = colorRed; imageOut.image[index+1] =
	 * colorGreen;//colorv.ptsInt[1]; imageOut.image[index+2] =
	 * colorBlue;//colorv.ptsInt[2]; } } } return true; }// end of
	 * draw_colorcircle
	 */
	/////////////////////////////////////////////////////////////////////////////
	public boolean draw_colorcircleDouble(ImageObject imageOut, ImPoint cir, Point3DDouble colorv) {
		if (imageOut == null) {
			logger.debug("ERROR: there is no image data");

			return false;
		}

		if (imageOut.getNumBands() != 3) {
			logger.debug("ERROR: trying to plot in color image of type FLOAT but the image is not 3D");

			return false;
		}

		int numrows, numcols;
		numrows = imageOut.getNumRows();
		numcols = imageOut.getNumCols();

		//  ImPoint limit;
		//limit.x = numrows;
		//limit.y = numcols;
		//if part of the circle is acceptable then this test should be omitted
		// check if circle fits to the image area
		//  if(CheckCircle(cir,limit))
		//  return 1;

		int index;
		long size;
		float incr;
		if (cir.getV() <= 0.0) {
			logger.debug("Error: non-positive radius");

			return false;
		}

		incr = (float) (1.0 / cir.getV()); // this is equal to perim/(2*PI)

		size = imageOut.getSize();
		float angle;
		int row, col;
		boolean signal = true;

		for (angle = 0.0F; angle < 2 * Math.PI; angle += incr) {
			row = (int) (cir.getX() + cir.getV() * Math.cos(angle) + 0.5);
			col = (int) (cir.getY() + cir.getV() * Math.sin(angle) + 0.5);
			index = (row * numcols + col) * imageOut.getNumBands();

			if ((index < size) && (index >= 0)) {
				imageOut.set(index, colorv.getPtsDouble()[0]);
				imageOut.set(index + 1, colorv.getPtsDouble()[1]);
				imageOut.set(index + 2, colorv.getPtsDouble()[2]);
			}
		}

		return true;
	}// end of draw_colorcircleDouble

	//////////////////////////////////////////////////////////////////////////
	// graphics operations
	// draws a circular disk
	/////////////////////////////////////////////////////////////////////////////
	public boolean draw_diskDouble(ImageObject imageOut, ImPoint cir, double color) {
		if (imageOut == null) {
			logger.debug("ERROR: there is no image data");

			return false;
		}

		if (imageOut.getNumBands() != 1) {
			logger.debug("ERROR: trying to plot in grayscale image but the image is not 1D");

			return false;
		}

		//		if (imageOut.sampType.equalsIgnoreCase("BYTE") == false) {
		//			System.out.println("Error: only BYTE type is supported");
		//			return false;
		//		}

		int numrows, numcols;
		numrows = imageOut.getNumRows();
		numcols = imageOut.getNumCols();

		int index, index1;
		long size;
		float incr;

		if (cir.getV() <= 0.0) {
			logger.debug("Error: non-positive radius");

			return false;
		}
		incr = (float) (1.0 / cir.getV()); // this is equal to perim/(2*PI)

		size = imageOut.getSize();
		float angle, val;
		int row, col, col1;//row1;

		for (angle = 0.0F; angle < Math.PI; angle += incr) {
			val = (float) (cir.getV() * Math.cos(angle));
			row = (int) (cir.getX() + val + 0.5);

			if ((row < 0) || (row >= numrows)) {
				if (row < 0) {
					row = 0;
				}
				if (row >= numrows) {
					row = numrows - 1;
				}
			}

			val = (float) (cir.getV() * Math.sin(angle));
			col = (int) (cir.getY() + val + 0.5);

			if ((col < 0) || (col >= numcols)) {
				if (col < 0) {
					col = 0;
				}
				if (col >= numcols) {
					col = numcols - 1;
				}
			}

			//row1 = row;
			col1 = (int) (cir.getY() - val + 0.5);

			if ((col1 < 0) || (col1 >= numcols)) {
				if (col1 < 0) {
					col1 = 0;
				}
				if (col1 >= numcols) {
					col1 = numcols - 1;
				}
			}

			index = (row * numcols + col) * imageOut.getNumBands();
			index1 = index + imageOut.getNumBands() * (col1 - col);

			while (index1 < index) {
				if ((index1 < size) && (index1 >= 0)) {
					imageOut.set(index, color);
				}

				index1 += imageOut.getNumBands();
			}
		}

		return true;
	}// end of draw_disk

	////////////////////////////////////////////////////////////
	// Verifies if a circle is inside of image area
	////////////////////////////////////////////////////////////
	public boolean CheckCircle(ImPoint cir, ImPoint limit) {
		// check if circle fits to the image area
		if ((cir.getV() < 0) || (cir.getX() - cir.getV() < 0) || (cir.getX() + cir.getV() >= limit.getX()) || (cir.getY() - cir.getV() < 0) || (cir.getY() + cir.getV() >= limit.getY())) {
			return false;
		}

		return true;
	}

	////////////////////////////////////////////////////////////
	// Verifies if region position is inside of image area
	////////////////////////////////////////////////////////////
	// return four corner points and answer if inside
	public boolean CheckRegion(SubAreaFloat area, ImPoint ptsc, ImPoint limit, ImPoint[] pts) {
		double alpha;
		if (((int) area.getRow() < 0) || (area.getRow() >= limit.getX()) || ((int) area.getCol() < 0) || (area.getCol() >= limit.getY())) {
			if (_debugDrawOper) {
				logger.debug("Error: area(Row=" + area.getRow() + ",Col=" + area.getCol() + ") is out of bounds");
			}

			return false;
		}

		pts[0].setX(area.getRow() + area.getHeight());
		pts[0].setY(area.getCol());
		pts[1].setX(area.getRow() + area.getHeight());
		pts[1].setY(area.getCol() + area.getWidth());
		pts[2].setX(area.getRow());
		pts[2].setY(area.getCol() + area.getWidth());
		pts[3].setX(area.getRow());
		pts[3].setY(area.getCol());
		alpha = Math.toRadians(area.getAngle()); //PI/180;

		_GeomOper.rotatePoints(ptsc, pts, 4, alpha);

		for (int i = 0; i < 4; i++) {
			if ((pts[i].getX() > limit.getX()) || (pts[i].getY() > limit.getY()) || ((int) pts[i].getX() < 0.0) || ((int) pts[i].getY() < 0.0)) {
				if (_debugDrawOper) {
					logger.debug("Error: area(Row=" + area.getRow() + ",Col=" + area.getCol() + ",High=" + area.getHeight() + ",Wide=" + area.getWidth() + ",Angle=" + area.getAngle()
							+ ") is out of bounds");
				}
				return false;
			}
		}
		return true;
	}

	// return only answer if inside
	public boolean CheckRegion(SubAreaFloat area, ImPoint limit) {
		ImPoint ptsc = new ImPoint(area.getRow(), area.getCol());

		return (CheckRegion(area, ptsc, limit));
	}

	public boolean CheckRegion(SubAreaFloat area, ImPoint ptsc, ImPoint limit) {
		ImPoint[] pts = new ImPoint[4];
		for (int i = 0; i < 4; i++) {
			pts[i] = new ImPoint();
		}

		return (CheckRegion(area, ptsc, limit, pts));
	}

	//////////////////////////////////////////////////////////////////////////////
	// graphics operations
	// draws borders of a rotated rectangular box
	// into float image
	/////////////////////////////////////////////////////////////////////////////
	public boolean draw_boxDouble(ImageObject imageOut, SubAreaFloat area, double color) {
		ImPoint ptsc = new ImPoint(area.getRow(), area.getCol());

		return (draw_boxDouble(imageOut, area, ptsc, color));
	}

	public boolean draw_boxDouble(ImageObject imageOut, SubAreaFloat area, ImPoint ptsc, double color) {
		if (imageOut == null) {
			logger.debug("ERROR: there is no image data");

			return false;
		}

		if (imageOut.getNumBands() != 1) {
			logger.debug("ERROR: trying to plot in grayscale image but the image is not 1D");

			return false;
		}

		int numrows, numcols;
		numrows = imageOut.getNumRows();
		numcols = imageOut.getNumCols();

		//need to CheckRegion() here since we use the points
		ImPoint limit = new ImPoint();
		limit.setX(numrows);
		limit.setY(numcols);

		ImPoint[] pts = new ImPoint[4];
		int i;
		for (i = 0; i < 4; i++) {
			pts[i] = new ImPoint();
		}

		if (!CheckRegion(area, ptsc, limit, pts)) {
			logger.debug("ERROR: box is out of bounds ");

			return false;
		}

		// draw four lines
		ImLine Linefinal = new ImLine();

		Linefinal.setPts1(pts[3]);
		Linefinal.setPts2(pts[0]);
		plot_lineDouble(imageOut, Linefinal, color);

		Linefinal.setPts1(pts[0]);
		Linefinal.setPts2(pts[1]);
		plot_lineDouble(imageOut, Linefinal, color);

		Linefinal.setPts1(pts[1]);
		Linefinal.setPts2(pts[2]);
		plot_lineDouble(imageOut, Linefinal, color);

		Linefinal.setPts1(pts[2]);
		Linefinal.setPts2(pts[3]);
		plot_lineDouble(imageOut, Linefinal, color);

		return true;
	}// end of draw_box1

	/////////////////////////////////////////////////////////////////////////////
	// draws borders of a rotated rectangular box into color image
	/////////////////////////////////////////////////////////////////////////////
	public boolean draw_colorboxDouble(ImageObject imageOut, SubAreaFloat area, ImPoint ptsc, Point3DDouble colorv) {
		if (imageOut == null) {
			logger.debug("ERROR: there is no image data");

			return false;
		}

		if (imageOut.getNumBands() != 3) {
			logger.debug("Error: trying to plot in color image but the image is not 3D");

			return false;
		}

		//		if (imageOut.sampType.equalsIgnoreCase("BYTE") == false) {
		//			System.out.println("Error: only BYTE type is supported");
		//			return false;
		//		}

		ImPoint limit = new ImPoint();
		limit.setX(imageOut.getNumRows());
		limit.setY(imageOut.getNumCols());

		// check if rotated box fits to the image area
		ImPoint[] pts = new ImPoint[4];
		for (int i = 0; i < 4; i++) {
			pts[i] = new ImPoint();
		}

		if (!CheckRegion(area, ptsc, limit, pts)) {
			return false;
		}

		// draw four lines
		ImLine Linefinal = new ImLine();

		Linefinal.setPts1(pts[3]);
		Linefinal.setPts2(pts[0]);
		plot_colorlineDouble(imageOut, Linefinal, colorv);

		Linefinal.setPts1(pts[0]);
		Linefinal.setPts2(pts[1]);
		plot_colorlineDouble(imageOut, Linefinal, colorv);

		Linefinal.setPts1(pts[1]);
		Linefinal.setPts2(pts[2]);
		plot_colorlineDouble(imageOut, Linefinal, colorv);

		Linefinal.setPts1(pts[2]);
		Linefinal.setPts2(pts[3]);
		plot_colorlineDouble(imageOut, Linefinal, colorv);

		return true;
	}// end of draw_colorbox1

	public boolean plot_lineDouble(ImageObject imageOut, ImLine line, double color) {
		if ((line == null) || !line.computeSlopeFromPts()) {
			return false;
		}

		if (imageOut == null) {
			logger.debug("Error: Display Image is not initialized\n");

			return false;
		}

		if (imageOut.getNumBands() != 1) {
			logger.debug("Error: trying to plot in grayscale image but the image is not 1D");

			return false;
		}

		//		if (!imageOut.sampType.equalsIgnoreCase("BYTE")
		//				&& !imageOut.sampType.equalsIgnoreCase("SHORT")
		//				&& !imageOut.sampType.equalsIgnoreCase("INT")) {
		//			System.out
		//					.println("Error: only BYTE, SHORT and INT types are supported");
		//			return false;
		//		}

		int numcols;
		numcols = imageOut.getNumCols();

		float x, y, x1, y1, x2, y2, deltax, d;
		int i, numpix, row, col;
		int index = 0;

		long size = imageOut.getSize();

		x1 = (float) line.getPts1().getX();
		y1 = (float) line.getPts1().getY();
		x2 = (float) line.getPts2().getX();
		y2 = (float) line.getPts2().getY();

		d = (float) Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
		numpix = (int) (d + 0.5);
		// numpix should never be zero

		if (Math.abs(line.getSlope()) < LimitValues.SLOPE_MAX) {
			deltax = (x2 - x1) / numpix;

			if (Math.abs(deltax) < LimitValues.EPSILON3) {//0.0001
				logger.debug(" Error: slope\n");

				return false;
			}

			for (i = 0; i < numpix; i++) {
				x = x1 + deltax * i;
				row = (int) (x + 0.5);
				col = (int) (line.getSlope() * x + line.getQ() + 0.5);
				//if(index <0 || index > size){
				if ((row < 0) || (col < 0) || (row >= imageOut.getNumRows()) || (col >= imageOut.getNumCols())) {
					//System.out.println( "Error: outside of image
					// area\n");
				} else {
					index = row * numcols + col;
				}

				imageOut.set(index, color);
			}
		} else {
			deltax = (y2 - y1) / numpix;

			if (Math.abs(deltax) < LimitValues.EPSILON3) {//0.0001
				logger.debug(" Error: slope\n");

				return false;
			}

			for (i = 0; i < numpix; i++) {
				y = y1 + deltax * i;
				col = (int) (y + 0.5);
				row = (int) (line.getQ() + 0.5);
				if ((row < 0) || (col < 0) || (row >= imageOut.getNumRows()) || (col >= imageOut.getNumCols())) {
					//System.out.println( "Error: 2. outside of image
					// area\n");
				} else {
					index = row * numcols + col;
				}

				imageOut.set(index, color);
			}
		}

		return true;
	} // end of plot_lineDouble

	/**
	 * This method return the int array of rows and columns that are along the
	 * line provided and inside of image dimensions
	 * 
	 * @param imNumrows
	 * @param imNumcols
	 * @param line
	 * @return int [] with an order of (row, col)
	 */
	public int[] getImLinePoints(int imNumrows, int imNumcols, ImLine line) {
		if ((line == null) || !line.computeSlopeFromPts()) {
			return null;
		}

		if ((imNumrows <= 0) || (imNumcols <= 0)) {
			logger.debug("Error: image cannot have non-positive dimensions \n");
			return null;
		}

		float x, y, x1, y1, x2, y2, deltax, d;
		int i, numpix, row, col;
		int index = 0;

		x1 = (float) line.getPts1().getX();
		y1 = (float) line.getPts1().getY();
		x2 = (float) line.getPts2().getX();
		y2 = (float) line.getPts2().getY();

		d = (float) Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
		numpix = (int) (d + 0.5);
		// numpix should never be zero

		// allocate memory for the line points
		int[] retPoints = new int[numpix << 1];
		int countPoints = 0;

		if (Math.abs(line.getSlope()) < LimitValues.SLOPE_MAX) {
			deltax = (x2 - x1) / numpix;

			if (Math.abs(deltax) < LimitValues.EPSILON3) {//0.0001
				logger.debug(" Error: slope\n");
				return null;
			}

			for (i = 0; i < numpix; i++) {
				x = x1 + deltax * i;
				row = (int) (x + 0.5);
				col = (int) (line.getSlope() * x + line.getQ() + 0.5);
				//if(index <0 || index > size){
				if ((row < 0) || (col < 0) || (row >= imNumrows) || (col >= imNumcols)) {
					//System.out.println( "Error: outside of image
					// area\n");
				} else {
					retPoints[countPoints] = row;
					retPoints[countPoints + 1] = col;
					countPoints += 2;
				}
			}
		} else {
			deltax = (y2 - y1) / numpix;

			if (Math.abs(deltax) < LimitValues.EPSILON3) {//0.0001
				logger.debug(" Error: slope\n");
				return null;
			}

			for (i = 0; i < numpix; i++) {
				y = y1 + deltax * i;
				col = (int) (y + 0.5);
				row = (int) (line.getQ() + 0.5);
				if ((row < 0) || (col < 0) || (row >= imNumrows) || (col >= imNumcols)) {
					//System.out.println( "Error: 2. outside of image
					// area\n");
				} else {
					retPoints[countPoints] = row;
					retPoints[countPoints + 1] = col;
					countPoints += 2;
				}
			}
		}

		if ((countPoints >> 1) != numpix) {
			logger.debug("INFO: number of points = " + (countPoints >> 1) + ", numpix=" + numpix);
			int[] retPoints2 = new int[countPoints];
			for (i = 0; i < countPoints; i++) {
				retPoints2[i] = retPoints[i];
			}
			retPoints = null;
			return retPoints2;
		} else {
			return retPoints;
		}
	} // end of plot_lineDouble

	////////////////////////////////////////////////////////////////////////////
	// draws a line into a color image
	public boolean plot_colorlineDouble(ImageObject imageOut, ImLine line, Point3DDouble colorv) {
		if (imageOut == null) {
			logger.debug("Error: Display Image is not initialized\n");

			return false;
		}
		if (imageOut.getNumBands() != 3) {
			logger.debug("Error: trying to plot in color image but the image is not 3D");

			return false;
		}

		//		if (imageOut.sampType.equalsIgnoreCase("BYTE") == false) {
		//			System.out.println("Error: only BYTE type is supported");
		//			return false;
		//		}

		int numcols;
		numcols = imageOut.getNumCols();

		float x, y, x1, y1, x2, y2, deltax, d;
		int i, numpix, row, col;
		int index = 0;

		long size = imageOut.getSize();

		x1 = (float) line.getPts1().getX();
		y1 = (float) line.getPts1().getY();
		x2 = (float) line.getPts2().getX();
		y2 = (float) line.getPts2().getY();

		if (!line.computeSlopeFromPts()) {
			// identical points - draw a point
			//test
			//System.out.println("Test: drawing a point because the line has
			// identical points");
			//plot_colorpoint(imageOut,line.pts1,colorv);
			return false;
		}

		d = (float) Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
		numpix = (int) (d + 0.5);
		// numpix should never be zero

		if (Math.abs(line.getSlope()) < LimitValues.SLOPE_MAX) {
			deltax = (x2 - x1) / numpix;
			if (Math.abs(deltax) < LimitValues.EPSILON3) {//0.0001
				System.out.println(" Error: slope\n");

				return false;
			}

			for (i = 0; i < numpix; i++) {
				x = x1 + deltax * i;
				row = (int) (x + 0.5);
				col = (int) (line.getSlope() * x + line.getQ() + 0.5);
				if ((row < 0) || (col < 0) || (row >= imageOut.getNumRows()) || (col >= imageOut.getNumCols())) {
					//if(index <0 || index > size){
					logger.debug("Error: outside of image area\n");
				} else {
					index = (row * numcols + col) * imageOut.getNumBands();

					imageOut.set(index, colorv.getPtsDouble()[0]);
					imageOut.set(index + 1, colorv.getPtsDouble()[1]);
					imageOut.set(index + 2, colorv.getPtsDouble()[2]);
				}
			}
		} else {
			deltax = (y2 - y1) / numpix;
			if (Math.abs(deltax) < LimitValues.EPSILON3) { //0.0001
				logger.debug(" Error: slope\n");

				return false;
			}

			for (i = 0; i < numpix; i++) {
				y = y1 + deltax * i;
				col = (int) (y + 0.5);
				row = (int) (line.getQ() + 0.5);
				if ((row < 0) || (col < 0) || (row >= imageOut.getNumRows()) || (col >= imageOut.getNumCols())) {
					logger.debug("Error: outside of image area\n");
				} else {
					index = (row * numcols + col) * imageOut.getNumBands();

					imageOut.set(index, colorv.getPtsDouble()[0]);
					imageOut.set(index + 1, colorv.getPtsDouble()[1]);
					imageOut.set(index + 2, colorv.getPtsDouble()[2]);
				}
			}
		}

		return true;
	} // end of plot_colorlineDouble

	///////////////////////////////////////////////
	// plot a point into imageOut file
	///////////////////////////////////////////////
	public boolean plot_point(ImageObject imageOut, ImPoint pts, double color) {
		if (imageOut == null) {
			logger.debug("Error: Display Image is not initialized\n");

			return false;
		}

		//System.out.println("Plot Point");
		//		if (imageOut.sampType.equalsIgnoreCase("BYTE") == false) {
		//			System.out.println("Error: only BYTE type is supported");
		//			return false;
		//		}

		int index;
		index = (((int) (pts.getX() + 0.5)) * imageOut.getNumCols() + ((int) (pts.getY() + 0.5))) * imageOut.getNumBands();

		if ((index + imageOut.getNumBands() < imageOut.getSize()) && (index >= 0)) {
			for (int i = index; i < index + imageOut.getNumBands(); i++) {
				imageOut.set(i, color);
			}

			return true;
		} else {
			logger.debug("Error: outside of image area\n");

			return false;
		}
	} // end of plot_point

	///////////////////////////////////////////////
	public boolean plot_colorpoint(ImageObject imageOut, ImPoint pts, Point3DDouble color) {
		if (imageOut == null) {
			logger.debug("Error: Display Image is not initialized\n");

			return false;
		}

		//		if (imageOut.sampType.equalsIgnoreCase("BYTE") == false) {
		//			System.out.println("Error: only BYTE type is supported");
		//			return false;
		//		}

		int index;
		index = (((int) (pts.getX() + 0.5)) * imageOut.getNumCols() + ((int) (pts.getY() + 0.5))) * imageOut.getNumBands();

		if ((index + 3 < imageOut.getSize()) && (index >= 0)) {
			imageOut.set(index, color.getPtsDouble()[0]);
			imageOut.set(index + 1, color.getPtsDouble()[1]);
			imageOut.set(index + 2, color.getPtsDouble()[2]);

			return true;
		} else {
			logger.debug("Error: outside of image area\n");

			return false;
		}
	} // end of plot_colorpoint

	//////////////////////////////////////////////
	//draw a cross given a point and a cross size
	// into a float image
	// add angle to pts.v
	//////////////////////////////////////////////
	public boolean draw_crossDouble(ImageObject imageOut, ImPoint pts, int arm_size, double color) {

		//		if (imageOut.sampType.equalsIgnoreCase("BYTE") == false) {
		//			System.out.println("Error: only BYTE type is supported");
		//			return false;
		//		}

		int numcols;
		numcols = imageOut.getNumCols();

		if (arm_size < 1) {
			//draw a point
			int idx;
			idx = ((int) (pts.getX() + 0.5) * numcols + (int) (pts.getY() + 0.5)) * imageOut.getNumBands();

			for (int i = idx; i < idx + imageOut.getNumBands(); i++) {
				imageOut.set(i, color);
			}

			return true;
		} else {
			ImLine line = new ImLine();
			if ((pts.getV() > 360.0) || (pts.getV() < 0.0)) {
				//horizontal line
				line.getPts1().setX(pts.getX());
				line.getPts1().setY(pts.getY() - arm_size - 0.5);
				line.getPts2().setX(pts.getX());
				line.getPts2().setY(pts.getY() + arm_size + 0.5);
				plot_lineDouble(imageOut, line, color);

				//vertical line
				line.getPts1().setX(pts.getX() - arm_size - 0.5);
				line.getPts1().setY(pts.getY());
				line.getPts2().setX(pts.getX() + arm_size + 0.5);
				line.getPts2().setY(pts.getY());
				plot_lineDouble(imageOut, line, color);
			} else {
				ImPoint ptsc;
				ptsc = pts;

				//horizontal line
				line.getPts1().setX(pts.getX());
				line.getPts1().setY(pts.getY() - arm_size - 0.5);
				line.getPts2().setX(pts.getX());
				line.getPts2().setY(pts.getY() + arm_size + 0.5);

				double alpha = Math.toRadians(pts.getV());
				_GeomOper.rotatePoint(ptsc, line.getPts1(), alpha);
				_GeomOper.rotatePoint(ptsc, line.getPts2(), alpha);
				plot_lineDouble(imageOut, line, color);

				//vertical line
				line.getPts1().setX(pts.getX() - arm_size - 0.5);
				line.getPts1().setY(pts.getY());
				line.getPts2().setX(pts.getX() + arm_size + 0.5);
				line.getPts2().setY(pts.getY());

				_GeomOper.rotatePoint(ptsc, line.getPts1(), alpha);
				_GeomOper.rotatePoint(ptsc, line.getPts2(), alpha);
				plot_lineDouble(imageOut, line, color);
			}
		}
		return true;
	}

	//////////////////////////////////////////////
	//draw a cross given a point and a cross size
	// into a color float image
	// add angle to pts.v
	//////////////////////////////////////////////
	public boolean draw_colorcrossDouble(ImageObject imageOut, ImPoint pts, int arm_size, Point3DDouble colorv) {
		if (imageOut == null) {
			logger.debug("Error: Display Image is not initialized\n");

			return false;
		}

		if (imageOut.getNumBands() != 3) {
			logger.debug("Error: trying to draw color cross but the image is not 3D");

			return false;
		}

		//		if (imageOut.sampType.equalsIgnoreCase("BYTE") == false) {
		//			System.out.println("Error: only BYTE type is supported");
		//			return false;
		//		}

		int numcols;
		numcols = imageOut.getNumCols();

		if (arm_size < 1) {
			//draw a point
			int idx;
			idx = ((int) (pts.getX() + 0.5) * numcols + (int) (pts.getY() + 0.5)) * imageOut.getNumBands();

			imageOut.set(idx, colorv.getPtsDouble()[0]);
			imageOut.set(idx + 1, colorv.getPtsDouble()[1]);
			imageOut.set(idx + 2, colorv.getPtsDouble()[2]);

			return true;
		} else {
			ImLine line = new ImLine();
			if ((pts.getV() > 360.0) || (pts.getV() < 0.0)) {
				//horizontal line
				line.getPts1().setX(pts.getX());
				line.getPts1().setY(pts.getY() - arm_size - 0.5);
				line.getPts2().setX(pts.getX());
				line.getPts2().setY(pts.getY() + arm_size + 0.5);

				plot_colorlineDouble(imageOut, line, colorv);

				//vertical line
				line.getPts1().setX(pts.getX() - arm_size - 0.5);
				line.getPts1().setY(pts.getY());
				line.getPts2().setX(pts.getX() + arm_size + 0.5);
				line.getPts2().setX(pts.getY());

				plot_colorlineDouble(imageOut, line, colorv);
			} else {
				ImPoint ptsc;
				ptsc = pts;
				//horizontal line
				line.getPts1().setX(pts.getX());
				line.getPts1().setY(pts.getY() - arm_size - 0.5);
				line.getPts2().setX(pts.getX());
				line.getPts2().setY(pts.getY() + arm_size + 0.5);

				double alpha = Math.toRadians(pts.getV());
				_GeomOper.rotatePoint(ptsc, line.getPts1(), alpha);
				_GeomOper.rotatePoint(ptsc, line.getPts2(), alpha);
				plot_colorlineDouble(imageOut, line, colorv);

				//vertical line
				line.getPts1().setX(pts.getX() - arm_size - 0.5);
				line.getPts1().setY(pts.getY());
				line.getPts2().setX(pts.getX() + arm_size + 0.5);
				line.getPts2().setY(pts.getY());

				_GeomOper.rotatePoint(ptsc, line.getPts1(), alpha);
				_GeomOper.rotatePoint(ptsc, line.getPts2(), alpha);
				plot_colorlineDouble(imageOut, line, colorv);
			}
		}
		return true;
	}

	//////////////////////////////////////////////////////////////////////////////
	// graphics operations
	// draws a solid rotated rectangular box
	// into unsigned char
	/////////////////////////////////////////////////////////////////////////////
	public boolean draw_solidbox(ImageObject imageOut, SubAreaFloat area, ImPoint ptsc, double color) {
		if (imageOut == null) {
			logger.debug("Error: Display Image is not initialized\n");

			return false;
		}

		if (imageOut.getNumBands() != 1) {
			logger.debug("Error: trying to draw grayscale solid box but the image is not 1D");

			return false;
		}

		//		if (imageOut.sampType.equalsIgnoreCase("BYTE") == false) {
		//			System.out.println("Error: only BYTE type is supported");
		//			return false;
		//		}

		int numrows, numcols;
		numrows = imageOut.getNumRows();
		numcols = imageOut.getNumCols();

		ImPoint limit = new ImPoint();
		limit.setX(numrows);
		limit.setY(numcols);

		ImPoint[] pts = new ImPoint[4];
		int i;
		for (i = 0; i < 4; i++) {
			pts[i] = new ImPoint();
		}
		//return four corner points after rotation
		if (!CheckRegion(area, ptsc, limit, pts)) {
			return false;
		}

		// find min and max row
		float maxx = (float) pts[0].getX();
		float minx = (float) pts[0].getX();
		float maxy = (float) pts[0].getY();
		float miny = (float) pts[0].getY();
		int store_minx = 0;
		int store_maxx = 0;

		for (i = 1; i < 4; i++) {
			if (pts[i].getX() > maxx) {
				maxx = (float) pts[i].getX();
				store_maxx = i;
			}

			if (pts[i].getX() < minx) {
				minx = (float) pts[i].getX();
				store_minx = i;
			}

			if (pts[i].getY() > maxy) {
				maxy = (float) pts[i].getY();
			}
			if (pts[i].getY() < miny) {
				miny = (float) pts[i].getY();
			}
		}

		ImPoint limitmin = new ImPoint();
		ImPoint limitmax = new ImPoint();
		limitmin.setX(minx);
		limitmin.setY(miny);
		limitmax.setX(maxx);
		limitmax.setY(maxy);

		//  float val;
		int num; // number of intersection
		ImLine Linetmp = new ImLine();
		ImLine Linefinal = new ImLine();
		Linetmp.setPts1(pts[store_minx]);
		Linetmp.setPts2(Linetmp.getPts1());
		Linetmp.getPts2().setY(Linetmp.getPts2().getY() + 1);

		if (!Linetmp.computeSlopeFromPts()) {
			return false;
		}

		int j;
		int index, indexS;
		long size = imageOut.getSize();

		i = (int) (Linetmp.getPts1().getX() + 0.5);
		indexS = i * numcols;

		while (i < (int) (maxx + 0.5)) {
			num = draw_lineacrossbox(limitmin, limitmax, area, Linetmp, Linefinal);

			if (num == 2) {
				if (Linefinal.getPts1().getY() < Linefinal.getPts2().getY()) {
					j = (int) (Linefinal.getPts1().getY() + 0.5);
					index = indexS + j;

					for (; j < Linefinal.getPts2().getY() + 0.5; j++) {
						if ((index >= 0) && (index < size)) {
							imageOut.set(index, color);
						}

						index++;
					}
				} else {
					j = (int) (Linefinal.getPts2().getY() + 0.5);
					index = indexS + j;

					for (; j < Linefinal.getPts1().getY() + 0.5; j++) {
						if ((index >= 0) && (index < size)) {
							imageOut.set(index, color);
						}

						index++;
					}
				}
			} else {
				if (num == 1) {
					index = ((int) (Linefinal.getPts1().getX() + 0.5)) * numcols + ((int) (Linefinal.getPts1().getY() + 0.5));

					if ((index >= 0) && (index < size)) {
						imageOut.set(index, color);
					}
				}

			}

			Linetmp.getPts1().setX(Linetmp.getPts1().getX() + 1);
			Linetmp.getPts2().setX(Linetmp.getPts2().getX() + 1);
			Linetmp.setQ(Linetmp.getPts1().getX());
			i++;
			indexS += numcols;
		}
		return true;
	}// end of draw_box

	//////////////////////////////////////////////////////////////////////////////
	// graphics operations
	// draws a solid triangle
	// into unsigned char
	/////////////////////////////////////////////////////////////////////////////
	public boolean draw_solidtriangle(ImageObject imageOut, ImPoint[] pts, double color) {
		if (imageOut == null) {
			logger.debug("Error: Display Image is not initialized\n");

			return false;
		}

		if (imageOut.getNumBands() != 1) {
			logger.debug("Error: trying to draw grayscale triangle but the image is not 1D");

			return false;
		}

		int numrows, numcols;
		numrows = imageOut.getNumRows();
		numcols = imageOut.getNumCols();

		ImPoint limit = new ImPoint();
		limit.setX(numrows);
		limit.setY(numcols);

		int i;
		for (i = 0; i < 3; i++) {
			if ((pts[i].getX() < 0) || (pts[i].getY() < 0) || (pts[i].getX() >= limit.getX()) || (pts[i].getY() >= limit.getY())) {
				logger.debug("Error: points are out of image area ");

				return false;
			}
		}

		// find min and max row
		float maxx = (float) pts[0].getX();
		float minx = (float) pts[0].getX();
		float maxy = (float) pts[0].getY();
		float miny = (float) pts[0].getY();
		int store_minx = 0;
		//int store_maxx = 0;

		for (i = 1; i < 3; i++) {
			if (pts[i].getX() > maxx) {
				maxx = (float) pts[i].getX();
				//      store_maxx = i;
			}

			if (pts[i].getX() < minx) {
				minx = (float) pts[i].getX();
				store_minx = i;
			}

			if (pts[i].getY() > maxy) {
				maxy = (float) pts[i].getY();
			}

			if (pts[i].getY() < miny) {
				miny = (float) pts[i].getY();
			}
		}

		ImPoint limitmin = new ImPoint();
		ImPoint limitmax = new ImPoint();
		limitmin.setX(minx);
		limitmin.setY(miny);
		limitmax.setX(maxx);
		limitmax.setY(maxy);

		//  float val;
		int num; // number of intersection
		ImLine Linetmp = new ImLine();
		ImLine Linefinal = new ImLine();
		Linetmp.setPts1(pts[store_minx]);
		Linetmp.setPts2(Linetmp.getPts1());
		Linetmp.getPts2().setY(Linetmp.getPts2().getY() + 1);

		if (!Linetmp.computeSlopeFromPts()) {
			return false;
		}

		int j;
		int index, indexS;
		i = (int) (Linetmp.getPts1().getX() + 0.5);
		indexS = i * numcols;

		while (i <= (int) (maxx + 0.5)) {
			num = draw_lineacrosstriangle(limitmin, limitmax, pts, Linetmp, Linefinal);

			if (num == 2) {
				//      Linefinal.slope = 5000;
				//      plot_line(imageOut,Linefinal,color);
				//      j = (int)(Linefinal.pts1.x+0.5);
				//index = j*numcols;
				if (Linefinal.getPts1().getY() < Linefinal.getPts2().getY()) {
					j = (int) (Linefinal.getPts1().getY() + 0.5);
					index = indexS + j;

					for (; j < (int) (Linefinal.getPts2().getY() + 0.5); j++) {
						if (index < imageOut.getSize()) {
							imageOut.set(index, color);
						} else {
							logger.debug("Error: DrawOper wants to draw outside of image area ");
						}

						index++;
					}
				} else {
					j = (int) (Linefinal.getPts2().getY() + 0.5);
					index = indexS + j;

					for (; j < (int) (Linefinal.getPts1().getY() + 0.5); j++) {
						if (index < imageOut.getSize()) {
							imageOut.set(index, color);
						} else {
							logger.debug("Error: DrawOper wants to draw outside of image area ");
						}

						index++;
					}
				}
			} else {
				if (num == 1) {
					//         plot_point(imageOut,Linefinal.pts1,color);
					index = ((int) (Linefinal.getPts1().getX() + 0.5)) * numcols + ((int) (Linefinal.getPts1().getY() + 0.5));

					if ((index >= 0) && (index < imageOut.getSize())) {
						imageOut.set(index, color);
					}
				}

			}

			Linetmp.getPts1().setX(Linetmp.getPts1().getX() + 1);
			Linetmp.getPts2().setX(Linetmp.getPts2().getX() + 1);
			Linetmp.setQ(Linetmp.getPts1().getX());
			i++;
			indexS += numcols;
		}

		return true;
	}// end of draw_triangle

	//////////////////////////////////////////////////////////////////////////////
	// graphics operations
	// draws a sequence of solid rotated rectangular box
	// into unsigned char
	/////////////////////////////////////////////////////////////////////////////
	public boolean draw_snake(ImageObject imageOut, ImPoint[] pts, int numpoints, int thickness, double color) {
		if ((numpoints < 2) || (thickness < 1)) {
			System.out.println("Error: input parameters to draw_snake ");
			return false;
		}

		SubAreaFloat area = new SubAreaFloat();
		ImPoint ptsc;
		double deltax, deltay;
		int i;
		boolean ret = true;

		for (i = 1; i < numpoints; i++) {
			ptsc = pts[i - 1];
			area.setRow((float) pts[i - 1].getX());
			area.setCol((float) pts[i - 1].getY());
			deltax = pts[i].getX() - pts[i - 1].getX();
			deltay = pts[i].getY() - pts[i - 1].getY();
			area.setHeight((float) Math.sqrt(deltax * deltax + deltay * deltay));
			area.setWidth(thickness);
			area.setAngle((float) Math.atan2(deltay, deltax));
			area.setAngle((float) (Math.toRadians(area.getAngle())));

			ret = draw_solidbox(imageOut, area, ptsc, color);
		}
		return ret;
	}

}
