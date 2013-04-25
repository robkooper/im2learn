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
package edu.illinois.ncsa.isda.im2learn.ext.geo;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.im2learn.core.datatype.ImLine;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImPoint;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageException;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObjectByte;
import edu.illinois.ncsa.isda.im2learn.core.datatype.LimitValues;
import edu.illinois.ncsa.isda.im2learn.core.datatype.Point2DDouble;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ShapeObject;
import edu.illinois.ncsa.isda.im2learn.core.datatype.SubArea;
import edu.illinois.ncsa.isda.im2learn.core.geo.GeodeticPoint;
import edu.illinois.ncsa.isda.im2learn.core.geo.ModelPoint;
import edu.illinois.ncsa.isda.im2learn.core.geo.Projection;
import edu.illinois.ncsa.isda.im2learn.core.geo.ProjectionConvert;
import edu.illinois.ncsa.isda.im2learn.core.geo.RasterPoint;
import edu.illinois.ncsa.isda.im2learn.ext.math.GeomOper;
import edu.illinois.ncsa.isda.im2learn.ext.segment.ConnectAnal;
import edu.illinois.ncsa.isda.im2learn.ext.segment.DrawOp;

public class GeoFeature {

	private final LimitValues	_lim				= new LimitValues();
	private Point2DDouble		_centroidPts		= null;
	private java.util.Vector	_validBndIdx		= null;
	private ImageObject			_imMask				= null;
	private ImageObject			_geoMask			= null;
	private boolean				_debugGeoFeature	= false;
	private final int			_maskEdgeginPixels	= 10;

	private final ConnectAnal	_myConnect			= new ConnectAnal();
	private final DrawOp		_drawOper			= new DrawOp();
	private final GeomOper		_geomOper			= new GeomOper();
	int							friday				= 0;

	static private Log			logger				= LogFactory.getLog(GeoFeature.class);

	public GeoFeature() {
	}

	public void ResetGeoFeature() {
		_centroidPts = null;
		_validBndIdx = null;
		_imMask = null;
		return;
	}

	// //////////////////////////////////
	// Getters:
	// Any class MUST access this class through its getters.
	// If the results of a previous computation are available, then that results
	// is returned
	// otherwise the methods that do the actual computation are called.
	// /////////////////////////////////

	public java.util.Vector GetValidBndIdx() {
		return _validBndIdx;
	}

	public ImageObject GetImMask() {
		return _imMask;
	}

	public ImageObject GetGeoMask() {
		return _geoMask;
	}

	public boolean GetDebugFlag() {
		return _debugGeoFeature;
	}

	public void SetDebugFlag(boolean val) {
		_debugGeoFeature = val;
	}

	public Point2DDouble GetCentroidPts() {
		return _centroidPts;
	}

	// /////////////////////////////////////////
	// Setters:
	// ////////////////////////////////////////
	public void SetCentroidPts(Point2DDouble centroidPts) {
		// This can be used when a shape's actual points are available and one
		// need not improvise the
		// centroid of the shape as its centroid point.
		_centroidPts = centroidPts;
		return;
	}

	// /////////////////////////////////////////
	// The main doers that compute the various features
	// ////////////////////////////////////////
	public double[] ComputeCentroid(double[] points) {
		// 'points' contains y co-ordinates in even indices and x co-ordinate in
		// odd indices.
		// The resultant double array will contain the x co-ordinate at the 0th
		// index and y in the 1th index.
		double[] centroid = new double[2];
		int numPts = points.length / 2;
		// Compute the centroid with the formula:
		// centroid = [ (x1, x2, ..., xn)/n , (y1, y2, ..., yn)/n]
		for (int i = 0; i < points.length; i += 2) {
			centroid[0] += points[i + 1]; // The x-co-ordinate. Reversing the
			// order in which points are stored
			centroid[1] += points[i];
		}
		centroid[0] = centroid[0] / numPts; // x co-ordinate
		centroid[1] = centroid[1] / numPts; // y co-ordinate
		return centroid;
	}

	public boolean FindCentroidPts(ShapeObject shapeObject) {
		// NOTE: The centroid points returned will be in the same co-ordinates
		// as the shapeObject
		_centroidPts = null;
		_centroidPts = FindCentroidPtsOut(shapeObject);
		if (_centroidPts == null) {
			return false;
		} else {
			return true;
		}
	}

	public Point2DDouble FindCentroidPtsOut(ShapeObject shapeObject) {
		// Returns centroid points which is the centoid of each shape
		if (shapeObject == null) {
			System.err.println("ERROR: ShapeObject null! Cannot compute the centroid points in GeoFeature.ComputeCentroidPts(...)");
			return null;
		}
		Point2DDouble centroidPts = new Point2DDouble(shapeObject.GetNumBnd());
		double[] points = null;
		double[] tempCentroid = new double[2];
		for (int i = 0; i < shapeObject.GetNumBnd(); i++) {
			// _centroidPts[i] = null;
			points = shapeObject.GetPtsForBnd(i);
			if (points.length > 0) {
				tempCentroid = ComputeCentroid(points);
				// if( !VerifyInternalPoint(shapeObject, i, tempCentroid) ){
				// System.out.println("WARNING: did not find a correct internal
				// point for bnd ="+i);
				// }
				centroidPts.SetValue(i, tempCentroid[1], tempCentroid[0]);
			}
		} // for(int i = ...)
		return centroidPts;
	} // end of ComputeCentroidPts

	public boolean ConstructMask(ImageObject geoImgObj, ShapeObject shapeObject) throws ImageException {
		_geoMask = null;
		_geoMask = ConstructMaskOut(geoImgObj, shapeObject);
		if (_geoMask == null) {
			return false;
		} else {
			return true;
		}
	}

	public ImageObject ConstructMaskOut(ImageObject geoImgObj, ShapeObject shapeObject) throws ImageException {
		// The returned GeoImageObject is the 'mask'
		// if(_geoMask != null)
		// return _geoMask;
		ShapeObject retShapeObject; // NOTE: This shapeObject which contains the
		// ShapeObject
		// points in pixels is not returned.
		// If the calling function is interested in getting this
		// pixel ShapeObject, it must call ConstructImageShapeObject(...)
		// and then call this method.

		ImageObject geoMask; // = new ImageObject();

		Projection proj = ProjectionConvert.getNewProjection(geoImgObj.getProperty(ImageObject.GEOINFO));

		// Copy the geoImgObj data into mask

		// geoMask.CopyGeoInfo(geoImgObj);

		if (!(shapeObject.GetIsInPixel())) {
			// Convert lat/lng to pixel
			retShapeObject = ConstructImageShapeObject(geoImgObj, shapeObject);

			// TEST starts here:
			// System.out.println("ImageShapeObject has numBnd =
			// "+(retShapeObject.GetNumBnd()-1));
			// TEST ends here!

			if (!ConstructMaskImage(geoImgObj, retShapeObject)) {
				System.err.println("ERROR: could not construct mask from image and shape object");
				return null;
			}
			geoMask = GetImMask();
			geoMask.setProperty(ImageObject.GEOINFO, proj);
			// geoMask.SetImageObject( GetImMask() );
		} else {
			if (!ConstructMaskImage(geoImgObj, shapeObject)) {
				System.err.println("ERROR: could not construct mask from image and shape object");
				return null;
			}
			// geoMask.SetImageObject(ConstructMaskOut(geoImgObj.GetImageObject(),
			// shapeObject));
			geoMask = GetImMask();
			geoMask.setProperty(ImageObject.GEOINFO, proj);
			// geoMask.SetImageObject( GetImMask() );
		}
		return geoMask;
	}

	public boolean ConstructMaskImage(ImageObject imgObject, ShapeObject shpObj) {
		_imMask = null;
		if (shpObj != null) {
			if (shpObj.getAllBoundaryInternalPoints() != null) {
				_centroidPts = shpObj.getAllBoundaryInternalPoints();
			}
		}
		// _imMask = ConstructMaskOut(imgObject, shpObj,_internalPts);
		_imMask = ConstructMaskImageOut(imgObject, shpObj);
		if (_imMask == null) {
			return false;
		} else {
			return true;
		}
	}

	// /////////////////////////////////////////////////////
	// the main doer for mask construction
	// /////////////////////////////////////////////////
	// fully functional implementation without using the internal point
	// it is based on computer graphics approach of painting regions and holes
	// inot a sum image
	// and performing connectivity analysis of sum image
	public ImageObject ConstructMaskImageOut(ImageObject imgObject, ShapeObject shapeObj) {

		// sanity check
		if (imgObject == null) {
			System.err.println("ERROR: missing image");
			return null;
		}
		if (shapeObj == null) {
			System.err.println("ERROR: missing shape object ");
			return null;
		}

		int row, col;
		int i, j, m, n;
		boolean flip = true;
		//System.out.println("Create label mask");
		// System.out.println("numrows: "+numrows+ " numcols = "+numcols);

		ImageObject imMask = null;
		try {
			imMask = ImageObject.createImage((imgObject.getNumRows()), (imgObject.getNumCols()), 1, ImageObject.TYPE_INT);
		} catch (ImageException e) {
			logger.error("ERROR: image could not be created ", e);
			return null;
		}
		imMask.setData(-1); // Set the unknown pixels to -1
		imMask.setInvalidData(-1); // Set the unknown pixels to -1

		logger.debug("imMask pixel (0,0): " + imMask.getInt(0));

		double[] tempPoints = null;// shapeObj.GetPtsForBnd(i);
		double[] bbox = null;
		double[] partPts = null;

		int subSampPts1 = 2;
		ImPoint pts = new ImPoint();
		ImPoint pt1 = new ImPoint();
		ImPoint pt2 = new ImPoint();
		ImPoint pt3 = new ImPoint();
		ImPoint pts1 = new ImPoint();
		ImPoint pts2 = new ImPoint();

		ImPoint internalPt = new ImPoint();
		ImLine line = new ImLine();
		ImLine line1 = new ImLine();
		int k;
		// double centrRow, centrCol;
		int centrCount;
		// int minPartRow, minPartCol, maxPartRow, maxPartCol;

		int idx, idxMask, offset, index = 0;
		int idxInter;
		ImageObject imSubLabel = null;
		// int tempLabel;
		int desiredLabel;
		// double valDouble;
		boolean isLabel, signal;
		byte byteVal;

		// find max size of bounding box
		// area.setSize(-1,-1);

		int width, height;
		int minCol, minRow, maxCol, maxRow;
		SubArea area = new SubArea(0, 0, 10, 10);
		minCol = minRow = maxRow = maxCol = -1;

		width = height = -1;

		for (i = 0; i < shapeObj.GetNumBnd(); i++) {
			bbox = shapeObj.GetBndBox(i);
			if (bbox[0] < bbox[2]) {
				minCol = (int) (bbox[0] - 0.5);
				maxCol = (int) (bbox[2] + 1.0);
			} else {
				minCol = (int) (bbox[2] - 0.5);
				maxCol = (int) (bbox[0] + 1.0);
			}
			if (bbox[1] < bbox[3]) {
				minRow = (int) (bbox[1] - 0.5);
				maxRow = (int) (bbox[3] + 1.0);
			} else {
				minRow = (int) (bbox[3] - 0.5);
				maxRow = (int) (bbox[1] + 1.0);
			}
			// this is to form one region with label 1 in the imInter!!!
			minRow -= _maskEdgeginPixels;
			minCol -= _maskEdgeginPixels;
			maxRow += _maskEdgeginPixels;
			maxCol += _maskEdgeginPixels;

			/*
			 * // since the bounding box can exceed the image area // the area has to be modified if (minRow < 0) {
			 * minRow = 0; } if (minCol < 0) { minCol = 0; } if (minRow >= imMask.getNumRows()) { minRow =
			 * imMask.getNumRows(); } if (minCol >= imMask.getNumCols()) { minCol = imMask.getNumCols(); } if (maxRow <
			 * 0) { maxRow = 0; } if (maxCol < 0) { maxCol = 0; } if (maxRow > imMask.getNumRows()) { maxRow =
			 * imMask.getNumRows(); } if (maxCol > imMask.getNumCols()) { maxCol = imMask.getNumCols(); }
			 * 
			 * if (height < maxRow - minRow) { height = maxRow - minRow; } if (width < maxCol - minCol) { width = maxCol -
			 * minCol; }
			 */
		}
		area.setBounds(minCol, minRow, (maxCol - minCol), (maxRow - minRow));
		// test
		//System.out.println("TEST: max bounding box high =" + area.getHigh() + ", wide=" + area.getWide());
		if ((area.getHigh() <= 0) || (area.getWide() <= 0)) {
			System.err.println("ERROR: max bbox is less or equal to zero");
			// area.PrintSubArea();
			return null;
		}
		ImageObject imInter = new ImageObjectByte(1, 1, 1);
		ImageObject imSumInter = new ImageObjectByte(1, 1, 1);

		for (i = 0; i < shapeObj.GetNumBnd(); i++) {
			// find min and max row and col
			bbox = shapeObj.GetBndBox(i);
			if (bbox[0] < bbox[2]) {
				minCol = (int) (bbox[0] - 0.5);
				maxCol = (int) (bbox[2] + 1.0);
			} else {
				minCol = (int) (bbox[2] - 0.5);
				maxCol = (int) (bbox[0] + 1.0);
			}
			if (bbox[1] < bbox[3]) {
				minRow = (int) (bbox[1] - 0.5);
				maxRow = (int) (bbox[3] + 1.0);
			} else {
				minRow = (int) (bbox[3] - 0.5);
				maxRow = (int) (bbox[1] + 1.0);
			}
			// this is to form one region with label 1 in the imInter
			minRow -= _maskEdgeginPixels;
			minCol -= _maskEdgeginPixels;
			maxRow += _maskEdgeginPixels;
			maxCol += _maskEdgeginPixels;
			// since the bounding box can exceed the image area
			// the area has to be modified

			area.setSubArea(minRow, minCol, (maxRow - minRow), (maxCol - minCol), true);

			if ((area.getHigh() <= 0) || (area.getWide() <= 0)) {
				System.out.println("WARNING: area at boundary =" + i + " has at least  one zero dimension");
				// area.PrintSubArea();
				continue;
			}
			/*
			 * if ( !area.CheckSubArea(0, 0, imMask.getNumRows(), imMask.getNumCols())) { System.out.println("WARNING:
			 * area at boundary =" + i + " is outside of image area"); // area.PrintSubArea(); continue; }
			 */
			// idxMask = area.getRow() * imMask.getNumCols() + area.getCol();
			// offset = imMask.getNumCols() - area.getWide();
			// System.out.println("Area row : " + area.getRow() + " Area col : "
			// + area.getCol());
			// System.out.println("Area width : " + area.getWide() + " Area
			// height : " + area.getHigh());
			// System.out.println("Idx Mask : " + idxMask + " Offset : " +
			// offset);
			// adjust size and init image with Intersection count
			imInter.setSize(area.getWide(), area.getHigh(), 1);
			imInter.setImageObjectValue(0);

			imSumInter.setSize(area.getWide(), area.getHigh(), 1);
			imSumInter.setImageObjectValue(0);

			// the list of boundary points
			// signal whether an internal point has been found; if signal = true
			// then it has not been found
			signal = true;
			if (shapeObj.GetNumBndParts(i) == 1) {

				//logger.debug("Single Boundary Parts");
				tempPoints = shapeObj.GetPtsForBnd(i);

				// draw boundary into the label image and imInter
				centrCount = 0;
				for (j = 0; j < tempPoints.length; j += subSampPts1) {
					if (j < tempPoints.length - subSampPts1) {
						pt1.SetImPoint(tempPoints[j], tempPoints[j + 1]);
						pt2.SetImPoint(tempPoints[j + subSampPts1], tempPoints[j + subSampPts1 + 1]);
					} else {
						// close the loop
						pt1.SetImPoint(tempPoints[j], tempPoints[j + 1]);
						pt2.SetImPoint(tempPoints[0], tempPoints[1]);
					}

					if (centrCount == 0) {
						pts1.SetImPoint(pt1.x - minRow, pt1.y - minCol);
						line.setPts1(new ImPoint(pts1.x, pts1.y));
						if ((line.getPts1().x >= imInter.getNumRows()) || (line.getPts1().y >= imInter.getNumCols()) || !_drawOper.plot_point(imInter, line.getPts1(), -1.0)) {
							logger.debug("ImInter Height : " + imInter.getNumRows() + "  ImInter Width : " + imInter.getNumCols());
							logger.debug("ERROR: could not draw a point at i=" + i + ",j=" + j);
							logger.debug("Point Loc : " + line.getPts1().x + " , " + (pts1.y));
							// line.getPts1().PrintImPoint();
							// imInter.PrintImageObject();
						} else {
							centrCount++;
						}

					}
					if (line.setImLine(pt1, pt2)) {
						// ////////////////draw into imInter////////////////
						pts1.SetImPoint(line.getPts1());
						pts2.SetImPoint(line.getPts2());
						pts1.x -= minRow;
						pts2.x -= minRow;
						pts1.y -= minCol;
						pts2.y -= minCol;

						line.setPts1(new ImPoint(pts1));
						line.setPts2(new ImPoint(pts2));
						if (!_drawOper.plot_lineDouble(imInter, line, -1.0)) {
							System.out.println("ERROR: could not draw a line at i=" + i + ",j=" + j);

							// line.PrintImLine();
						} else {
							centrCount++;
						}
					}
				}// end of for (j)
				/*
				 * // test PB try { ImageLoader.writeImage("C:/PeterB/Projects/StateFarm/gridCells/imInter2.tif",
				 * imInter); } catch (IOException e) { // TODO Auto-generated catch block e.printStackTrace(); }
				 */

				// make sure that the left upper corner is not a boundary pixel
				imInter.set(0, 0);
				if (!_myConnect.binary_CA(imInter) || (_myConnect.getImLabels() == null)) {
					System.err.println("ERROR: could not perform conAnal for i=" + i);
					continue;
				}
				imSubLabel = _myConnect.getImLabels();
				/*
				 * // test PB try { ImageLoader.writeImage("C:/PeterB/Projects/StateFarm/gridCells/imSubLabel2.tif",
				 * imSubLabel); } catch (IOException e) { // TODO Auto-generated catch block e.printStackTrace(); }
				 */
				// System.out.println("Label width : " + imSubLabel.getWidth() +
				// " Label height : " + imSubLabel.getHeight());
				desiredLabel = 1;

				// //////////////////////////////////////
				// label the right set of pixels in the mask image

				if (centrCount > 0) {

					for (m = 0; m < imSubLabel.getNumRows(); m++) {
						for (n = 0; n < imSubLabel.getNumCols(); n++) {
							if (imSubLabel.getInt(m, n, 0) != desiredLabel) {
								if ((area.getRow() + m >= 0) && (area.getCol() + n >= 0) && (area.getRow() + m < imMask.getNumRows()) && (area.getCol() + n < imMask.getNumCols())) {
									// System.out.println("Index = " + index + "
									// idxInter = " + idxInter);
									imMask.setInt((area.getRow() + m), (area.getCol() + n), 0, i); // Changing
									// i+1
									// to i
								}
								imSubLabel.setInt(m, n, 0, i);
							}
						}// end of col
					}// end of row

				} else {
					System.out.println("ERROR: did not draw a single pixel into the imInter at i=" + i);
				}// end if - else (centCount >0)

			} else {// else of if( one boundary)
				// this is the case of multiple parts in one boundary
				// For each boundary, draw each part joined by lines.
				//logger.debug("Multiple Boundary Parts");

				for (j = 0; j < shapeObj.GetNumBndParts(i); j++) {
					partPts = null;
					partPts = shapeObj.GetPartPointsForBnd(i, j);
					if (partPts != null) {
						imInter.setImageObjectValue(0);
						// minPartRow = imInter.numrows;
						// minPartCol = imInter.numcols;
						// maxPartRow = maxPartCol = 0;

						// centrRow = 0.0;
						// centrCol = 0.0;
						centrCount = 0;
						for (idx = 0; idx < (partPts.length - 1); idx += 2) {
							if (idx >= 2) {
								pt1.SetImPoint(partPts[idx - 2], partPts[idx - 1]);
								pt2.SetImPoint(partPts[idx], partPts[idx + 1]);
							} else {
								// close the loop when you find the first point
								pt1.SetImPoint(partPts[partPts.length - 2], partPts[partPts.length - 1]);
								pt2.SetImPoint(partPts[idx], partPts[idx + 1]);
							}

							if (centrCount == 0) {

								line.setPts1(new ImPoint(pt1.x - minRow, pt1.y - minCol));
								if ((line.getPts1().x >= imInter.getNumRows()) || (line.getPts1().y >= imInter.getNumCols()) || !_drawOper.plot_point(imInter, line.getPts1(), -1.0)) {
									System.out.println("ERROR: could not draw a point at i=" + i + ",j=" + j);
									logger.debug("Point Loc : " + (pt1.x - minRow) + " , " + (pt1.y - minCol));
									// line.pts1.PrintImPoint();
								} else {
									centrCount++;
								}

							}
							if (line.setImLine(pt1, pt2)) {
								// ////////////////draw into
								// imInter////////////////
								pts1.SetImPoint(line.getPts1());
								pts2.SetImPoint(line.getPts2());
								pts1.x -= minRow;
								pts2.x -= minRow;
								pts1.y -= minCol;
								pts2.y -= minCol;

								line.setPts1(new ImPoint(pts1));
								line.setPts2(new ImPoint(pts2));

								if (!_drawOper.plot_lineDouble(imInter, line, -1.0)) {
									System.out.println("ERROR: could not draw a line at i=" + i + "j=" + j);
									// line.PrintImLine();
								} else {
									centrCount++;
								}
							}
						}// end for idx
						if (centrCount > 0) {
							// if it is large enough region then add all pixels
							// labeled as other than 1
							// ///////////////
							// connectivity analysis
							imInter.set(0, 0);
							if (!_myConnect.Binary_CA(imInter) || (_myConnect.GetImLabels() == null)) {
								System.err.println("ERROR: could not perform conAnal for i=" + i);
								continue;
							}
							imSubLabel = _myConnect.GetImLabels();

							// //////////////////
							// add to the sum image
							desiredLabel = 1;
							idxInter = 0;
							for (m = 0; m < imSubLabel.getSize(); m++) {
								if (imSubLabel.getInt(idxInter) != desiredLabel) {
									imSumInter.setInt(idxInter, imSumInter.getInt(idxInter) + 1);

									// imSumInter.setInt(idxInter,40);
								}
								idxInter++;
							}// end of m

						} else {
							System.out.println("ERROR: Parts: did not draw a single pixel into the imInter at i=" + i + ", j=" + j);
						}

					}// end if partPts != null
				}// end of j - GetNumBndParts(index)

				// //////////////////////////////////////
				// label the rigt set of pixels in the mask image
				// ///////////////
				// connectivity analysis of the imSumInter image
				imSumInter.set(0, 0);
				if (!_myConnect.Binary_CA(imSumInter) || (_myConnect.GetImLabels() == null)) {
					System.err.println("ERROR: imSumInter: could not perform conAnal for i=" + i);
					continue;
				}
				imSubLabel = _myConnect.GetImLabels();

				desiredLabel = 1;

				for (m = 0; m < imSubLabel.getNumRows(); m++) {
					for (n = 0; n < imSubLabel.getNumCols(); n++) {
						if ((imSubLabel.getInt(m, n, 0) != desiredLabel) && (imSumInter.getInt(m, n, 0) == 1)) {
							if ((area.getRow() + m >= 0) && (area.getCol() + n >= 0) && (area.getRow() + m < imMask.getNumRows()) && (area.getCol() + n < imMask.getNumCols())) {
								// System.out.println("Index = " + index + "
								// idxInter = " + idxInter);
								imMask.setInt((area.getRow() + m), (area.getCol() + n), 0, i); // Changing
								// i+1
								// to i
							}
						}
					}// end of col
				}// end of row

			}// end of else

		} // for(int i = ...)

		// imMask = imSubLabel;

		imInter = null;
		imSumInter = null;
		//System.out.println("Mask Image Complete");
		// for (i=0;i<100;i++)
		// logger.debug("imMask Value : " + imMask.getInt(i));
		return imMask;

	} // end of ConstructMaskOut

	// ////////////////////////////////////////////////////////////
	// / assign an internal point based on the boundary point orientation
	private boolean InternalPointFromCounterClockWiseBound(double[] tempPoints, int minRow, int minCol, ImageObject imInter, ImPoint internalPt) {
		// find internal point
		int j, k, index, subSampPts1 = 2;
		int row, col;
		boolean signal = true;
		ImPoint pt1 = new ImPoint();
		ImPoint pt2 = new ImPoint();

		ImPoint pts1 = new ImPoint();
		ImPoint pts2 = new ImPoint();

		ImLine line = new ImLine();
		ImLine line1 = new ImLine();

		for (j = 0; signal && (j < tempPoints.length); j += subSampPts1) {
			if (j < tempPoints.length - subSampPts1) {
				pt1.SetImPoint(tempPoints[j], tempPoints[j + 1]);
				pt2.SetImPoint(tempPoints[j + subSampPts1], tempPoints[j + subSampPts1 + 1]);
				while ((pt1.x == pt2.x) && (pt1.y == pt2.y) && (j < (tempPoints.length - (subSampPts1 << 1)))) {
					j += subSampPts1;
					pt2.SetImPoint(tempPoints[j + subSampPts1], tempPoints[j + subSampPts1 + 1]);
				}

			} else {
				// close the loop
				pt1.SetImPoint(tempPoints[j], tempPoints[j + 1]);
				pt2.SetImPoint(tempPoints[0], tempPoints[1]);
			}
			if (line.setImLine(pt1, pt2)) {
				// ////////////////draw into imInter////////////////
				pts1.SetImPoint(line.getPts1());
				pts2.SetImPoint(line.getPts2());
				pts1.x -= minRow;
				pts2.x -= minRow;
				pts1.y -= minCol;
				pts2.y -= minCol;

				line.setPts1(new ImPoint(pts1));
				line.setPts2(new ImPoint(pts2));
				// internal point
				if (j < tempPoints.length - (subSampPts1 << 2)) {
					// increase spacing between points to avoid
					// edges that go back and forth
					// two consecutive edges cannot be parallel otherwise we
					// might
					// choose a bad internal point

					line1.setPts1(new ImPoint(line.getPts2()));

					// line1.pts2.SetImPoint(tempPoints[j+(subSampPts1<<2)],tempPoints[j+(subSampPts1<<2)+1]);
					k = j + (subSampPts1 << 1);
					pts2.SetImPoint(tempPoints[k], tempPoints[k + 1]);
					// line1.setPts2SetImPoint(tempPoints[k],tempPoints[k+1]);
					pts2.x -= minRow;
					pts2.y -= minCol;
					line1.setPts2(new ImPoint(pts2));
					while ((line1.getPts1().x == line1.getPts2().x) && (line1.getPts1().y == line1.getPts2().y) && (k < (tempPoints.length - (subSampPts1 << 1)))) {
						k += subSampPts1;

						pts2.SetImPoint(tempPoints[k], tempPoints[k + 1]);
						pts2.x -= minRow;
						pts2.y -= minCol;
						line1.setPts2(new ImPoint(pts2));

					}

					if (line1.computeSlopeFromPts() && _geomOper.lineIntersectf(line, line1, pt1)) {
						pts2.SetImPoint(line1.getPts2().x, line1.getPts2().y);
						line.setPts2(new ImPoint(pts2));
						// line.pts2.SetImPoint(line1.pts2.x, line1.pts2.y);
					} else {
						// two consecutive edge segments are parallel
						// avoid processing this edge by setting identical
						// values to pts1 and pts2
						pts2.SetImPoint(line.getPts1().x, line.getPts1().y);
						line.setPts2(new ImPoint(pts2));
						// line.pts2.SetImPoint(line.pts1.x, line.pts1.y);
						// j += subSampPts1;
					}

				} else {
					// avoid processing this edge by setting identical values to
					// pts1 and pts2
					pts2.SetImPoint(line.getPts1().x, line.getPts1().y);
					line.setPts2(new ImPoint(pts2));
					// line.pts2.SetImPoint(line.pts1.x, line.pts1.y);
				}

				if (signal && ((line.getPts2().x - line.getPts1().x != 0) || (line.getPts2().y - line.getPts1().y != 0))) {
					// always pick a point on the left side of the vector tip
					// outer boundary goes counter - clockwise
					// test
					// if(i==7){
					// System.out.println("TEST: i ="+i+",j="+j);
					// }

					if (line.getPts2().x - line.getPts1().x >= 0) {
						if (line.getPts2().x - line.getPts1().x == 0) {
							// horizontal line
							if (line.getPts2().y - line.getPts1().y > 0) {
								internalPt.SetImPoint(line.getPts2().x - 1, line.getPts2().y);
								signal = false;
							} else {
								internalPt.SetImPoint(line.getPts2().x + 1, line.getPts2().y);
								signal = false;
							}
						} else {
							// delta along x > 0
							if (line.getPts2().y - line.getPts1().y > 0) {
								internalPt.SetImPoint(line.getPts2().x - 1, line.getPts2().y);
								signal = false;
							} else {
								internalPt.SetImPoint(line.getPts2().x, line.getPts2().y + 1);
								signal = false;
							}
						}
					} else {
						// delta along x < 0
						if (line.getPts2().y - line.getPts1().y >= 0) {
							internalPt.SetImPoint(line.getPts2().x, line.getPts2().y - 1);
							signal = false;
						} else {
							internalPt.SetImPoint(line.getPts2().x + 1, line.getPts2().y);
							signal = false;
						}
					}// end of else

					if (!signal) {
						// test if the point was not labeled as a boundary point
						// with
						// the previous boundary lines
						row = (int) (internalPt.x + 0.5);
						if (row < 0) {
							row = 0;
						}
						if (row >= imInter.getNumRows()) {
							row = imInter.getNumRows() - 1;
						}
						col = (int) (internalPt.y + 0.5);
						if (col < 0) {
							col = 0;
						}
						if (col >= imInter.getNumCols()) {
							col = imInter.getNumCols() - 1;
						}
						// index = ( (int)(internalPt.x +0.5) )*imInter.numcols
						// + ( (int)(internalPt.y +0.5) );
						index = row * imInter.getNumCols() + col;
						if ((index >= 0) && (index < imInter.getSize())) {
							if (imInter.getInt(index) == (byte) -1) {
								signal = true;
								// if(i==7)
								// System.out.println("TEST: internal pts was
								// labeled with previous lines at i="+i+",
								// j="+j);
							}
						} else {
							System.out.println("TEST: internal pts is outside of bbox");
							// System.out.println("TEST bndIndex="+i+": pts1.x:
							// "+line.pts1.x+" pts1.y: "+line.pts1.y);
							// System.out.println("TEST: pts2.x: "+line.pts2.x+"
							// pts2.y: "+line.pts2.y);
							System.out.println("TEST: internalPt.x: " + internalPt.x + " internalPt.y: " + internalPt.y);
							// line.PrintImLine();
							// line1.PrintImLine();
							signal = true;
						}
					}

				}// end of if signal
			}

		}// end of for (j)

		return signal;
	}

	// ////////////////////////////////////////////////////////////
	// / assign an internal point based on the boundary point orientation
	private boolean InternalPointFromOrientedBoundary(boolean dirCounterClock, double[] tempPoints, int minRow, int minCol, ImageObject imInter, ImPoint internalPt) {
		// find internal point
		int j, k, index, subSampPts1 = 2;
		int row, col;
		boolean signal = true;
		ImPoint pt1 = new ImPoint();
		ImPoint pt2 = new ImPoint();
		ImPoint pts1 = new ImPoint();
		ImPoint pts2 = new ImPoint();
		ImLine line = new ImLine();
		ImLine line1 = new ImLine();

		for (j = 0; signal && (j < tempPoints.length); j += subSampPts1) {
			if (j < tempPoints.length - subSampPts1) {
				pt1.SetImPoint(tempPoints[j], tempPoints[j + 1]);
				pt2.SetImPoint(tempPoints[j + subSampPts1], tempPoints[j + subSampPts1 + 1]);
				while ((pt1.x == pt2.x) && (pt1.y == pt2.y) && (j < (tempPoints.length - (subSampPts1 << 1)))) {
					j += subSampPts1;
					pt2.SetImPoint(tempPoints[j + subSampPts1], tempPoints[j + subSampPts1 + 1]);
				}

			} else {
				// close the loop
				pt1.SetImPoint(tempPoints[j], tempPoints[j + 1]);
				pt2.SetImPoint(tempPoints[0], tempPoints[1]);
			}
			if (line.setImLine(pt1, pt2)) {
				// ////////////////draw into imInter////////////////
				pts1.SetImPoint(line.getPts1());
				pts2.SetImPoint(line.getPts2());
				pts1.x -= minRow;
				pts2.x -= minRow;
				pts1.y -= minCol;
				pts2.y -= minCol;

				line.setPts1(new ImPoint(pts1));
				line.setPts2(new ImPoint(pts2));

				// internal point
				if (j < tempPoints.length - (subSampPts1 << 2)) {
					// increase spacing between points to avoid
					// edges that go back and forth
					// two consecutive edges cannot be parallel otherwise we
					// might
					// choose a bad internal point
					line1.setPts1(new ImPoint(line.getPts2()));
					// line1.pts1.x = line.pts2.x;
					// line1.pts1.y = line.pts2.y;
					// line1.pts2.SetImPoint(tempPoints[j+(subSampPts1<<2)],tempPoints[j+(subSampPts1<<2)+1]);
					k = j + (subSampPts1 << 1);
					pts2.SetImPoint(tempPoints[k], tempPoints[k + 1]);
					pts2.x -= minRow;
					pts2.y -= minCol;
					line1.setPts2(new ImPoint(pts2));

					while ((line1.getPts1().x == line1.getPts2().x) && (line1.getPts1().y == line1.getPts2().y) && (k < (tempPoints.length - (subSampPts1 << 1)))) {
						k += subSampPts1;
						pts2.SetImPoint(tempPoints[k], tempPoints[k + 1]);
						pts2.x -= minRow;
						pts2.y -= minCol;

						line1.setPts2(new ImPoint(pts2));

						// line1.pts2.SetImPoint(tempPoints[k],tempPoints[k+1]);
						// line1.pts2.x -= minRow;
						// line1.pts2.y -= minCol;
					}

					if (line1.computeSlopeFromPts() && _geomOper.lineIntersectf(line, line1, pt1)) {
						// pts2.SetImPoint(line1.getPts2());
						line.setPts2(new ImPoint(line1.getPts2()));
						// line.pts2.SetImPoint(line1.pts2.x, line1.pts2.y);
					} else {
						// two consecutive edge segments are parallel
						// avoid processing this edge by setting identical
						// values to pts1 and pts2
						line.setPts2(new ImPoint(line.getPts1()));
						// line.pts2.SetImPoint(line.pts1.x, line.pts1.y);
						// j += subSampPts1;
					}

				} else {
					// avoid processing this edge by setting identical values to
					// pts1 and pts2
					line.setPts2(new ImPoint(line.getPts1()));
					// line.pts2.SetImPoint(line.pts1.x, line.pts1.y);
				}
				/*
				 * if(i==7){ System.out.println("TEST: i ="+i+",j="+j+", signal ="+signal); line.PrintImLine();
				 * line1.PrintImLine(); }
				 */

				if (signal && ((line.getPts2().x - line.getPts1().x != 0) || (line.getPts2().y - line.getPts1().y != 0))) {

					if (dirCounterClock) {
						// counterclockwise direction of a boundary

						// always pick a point on the left side of the vector
						// tip
						// inner boundary goes counter - clockwise
						// test
						// if(i==7){
						// System.out.println("TEST: i ="+i+",j="+j);
						// }

						if (line.getPts2().x - line.getPts1().x >= 0) {
							if (line.getPts2().x - line.getPts1().x == 0) {
								// horizontal line
								if (line.getPts2().y - line.getPts1().y > 0) {
									internalPt.SetImPoint(line.getPts2().x - 1, line.getPts2().y);
									signal = false;
								} else {
									internalPt.SetImPoint(line.getPts2().x + 1, line.getPts2().y);
									signal = false;
								}
							} else {
								// delta along x > 0
								if (line.getPts2().y - line.getPts1().y > 0) {
									internalPt.SetImPoint(line.getPts2().x - 1, line.getPts2().y);
									signal = false;
								} else {
									internalPt.SetImPoint(line.getPts2().x, line.getPts2().y + 1);
									signal = false;
								}
							}
						} else {
							// delta along x < 0
							if (line.getPts2().y - line.getPts1().y >= 0) {
								internalPt.SetImPoint(line.getPts2().x, line.getPts2().y - 1);
								signal = false;
							} else {
								internalPt.SetImPoint(line.getPts2().x + 1, line.getPts2().y);
								signal = false;
							}
						}// end of else
					} else {
						// clockwise direction of a boundary
						// always pick a point on the right side of the vector
						// tip
						// outer boundary goes counter - clockwise
						// test
						// if(i==7){
						// System.out.println("TEST clockwise: i ="+i+",j="+j);
						// }

						if (line.getPts2().x - line.getPts1().x >= 0) {
							if (line.getPts2().x - line.getPts1().x == 0) {
								// horizontal line
								if (line.getPts2().y - line.getPts1().y > 0) {
									internalPt.SetImPoint(line.getPts2().x + 1, line.getPts2().y);
									signal = false;
								} else {
									internalPt.SetImPoint(line.getPts2().x - 1, line.getPts2().y);
									signal = false;
								}
							} else {
								// delta along x > 0
								if (line.getPts2().y - line.getPts1().y >= 0) {
									internalPt.SetImPoint(line.getPts2().x, line.getPts2().y - 1);
									signal = false;
								} else {
									internalPt.SetImPoint(line.getPts2().x - 1, line.getPts2().y);
									signal = false;
								}
							}
						} else {
							// delta along x < 0
							if (line.getPts2().y - line.getPts1().y > 0) {
								internalPt.SetImPoint(line.getPts2().x + 1, line.getPts2().y);
								signal = false;
							} else {
								internalPt.SetImPoint(line.getPts2().x, line.getPts2().y + 1);
								signal = false;
							}
						}// end of else

					}

					if (!signal) {
						// test if the point was not labeled as a boundary point
						// with
						// the previous boundary lines

						row = (int) (internalPt.x + 0.5);
						if (row < 0) {
							row = 0;
						}
						if (row >= imInter.getNumRows()) {
							row = imInter.getNumRows() - 1;
						}
						col = (int) (internalPt.y + 0.5);
						if (col < 0) {
							col = 0;
						}
						if (col >= imInter.getNumCols()) {
							col = imInter.getNumCols() - 1;
						}
						index = row * imInter.getNumCols() + col;

						// index = ( (int)(internalPt.x +0.5) )*imInter.numcols
						// + ( (int)(internalPt.y +0.5) );
						if ((index >= 0) && (index < imInter.getSize())) {
							if (imInter.getInt(index) == (byte) -1) {
								signal = true;
								// if(i==7)
								// System.out.println("TEST: internal pts was
								// labeled with previous lines at i="+i+",
								// j="+j);
							}
						} else {
							System.out.println("TEST: internal pts is outside of bbox");
							// System.out.println("TEST bndIndex="+i+": pts1.x:
							// "+line.pts1.x+" pts1.y: "+line.pts1.y);
							// System.out.println("TEST: pts2.x: "+line.pts2.x+"
							// pts2.y: "+line.pts2.y);
							System.out.println("TEST: internalPt.x: " + internalPt.x + " internalPt.y: " + internalPt.y);
							// line.PrintImLine();
							// line1.PrintImLine();
							signal = true;
						}
					}

					/*
					 * /// just a test //if(!signal){ if( i==7 ){ //test System.out.println("TEST bndIndex="+i+":
					 * pts1.x: "+line.pts1.x+" pts1.y: "+line.pts1.y); System.out.println("TEST: pts2.x: "+line.pts2.x+"
					 * pts2.y: "+line.pts2.y); System.out.println("TEST: internalPt.x: "+internalPt.x+" internalPt.y:
					 * "+internalPt.y); line.PrintImLine(); line1.PrintImLine(); } //}
					 */

				}// end of if signal
			}

		}// end of for (j)

		return signal;
	}

	public ShapeObject ConstructImageShapeObject(ImageObject geoImg, ShapeObject shpObj) throws ImageException {
		// This is only called when the underlying image is in lat/lng
		// After this method, all the data in _myImageShapeObject should be in
		// pixel co-ordinates.
		double xll, yll;
		double xlr, ylr;
		double xul, yul;
		double xur, yur;

		if (shpObj == null) {
			logger.debug("No Shape data available");
			return null;
		}

		if (geoImg == null) {
			logger.debug("No Geo-conversion possible");
			return null;
		}

		boolean bGeoProj = true;

		double[] g_bbox = shpObj.GetGlobalBndBox();
		logger.debug("BBox for Shape : " + g_bbox[0] + ", " + g_bbox[1] + ", " + g_bbox[2] + ", " + g_bbox[3]);
		double avg_bbox1 = Math.abs(g_bbox[0]) + Math.abs(g_bbox[2]);
		double avg_bbox2 = Math.abs(g_bbox[1]) + Math.abs(g_bbox[3]);

		if ((avg_bbox1 > 360) && (avg_bbox2 > 360)) {
			// a very silly way of deciding shape file projection......
			// To-Do : find some way to determine the shape file projection
			// (whether it's geo or model)
			// when loading shapefile
			bGeoProj = false;
		}

		// projS is the projection of Shapefile and it is using the old
		// projection based on NASA MRT code
		// projI is the projection of the image and it is using the new
		// projection in Im2Learn code built from scratch
		edu.illinois.ncsa.isda.im2learn.core.geo.Projection projI = ProjectionConvert.getNewProjection(geoImg.getProperty(ImageObject.GEOINFO));
		// ncsa.im2learn.core.geo.projection.Projection projS;
		edu.illinois.ncsa.isda.im2learn.core.geo.Projection projS = shpObj.getProjection();

		// TODO this is a temporary solution PB
		// projS = ProjectionConvert.toOld(shpObj.getProjection());

		if (projS == null) {
			projS = projI;
		}

		double[] pts;

		GeodeticPoint ptsGeo = null;
		RasterPoint rp = null;
		ModelPoint mp = null;
		Point2DDouble p2d = new Point2DDouble(1);
		p2d.ptsDouble[0] = 0; // column
		p2d.ptsDouble[1] = 0; // row
		ptsGeo = projI.rasterToEarth(new RasterPoint(p2d.ptsDouble[1], p2d.ptsDouble[0]));
		xul = ptsGeo.getLon();// p2d.ptsDouble[1]; // p2d.ptsDouble[1]; // lng
		yul = ptsGeo.getLat();// p2d.ptsDouble[0]; // p2d.ptsDouble[0]; // lat
		logger.debug("Lat for row =" + p2d.ptsDouble[1] + " is: " + yul);
		logger.debug("Lng for col =" + p2d.ptsDouble[0] + " is: " + xul);

		p2d.ptsDouble[0] = 0; // column
		p2d.ptsDouble[1] = geoImg.getNumRows(); // row
		ptsGeo = projI.rasterToEarth(new RasterPoint(p2d.ptsDouble[1], p2d.ptsDouble[0]));
		xll = ptsGeo.getLon();// p2d.ptsDouble[1]; // p2d.ptsDouble[1]; // lng
		yll = ptsGeo.getLat();// p2d.ptsDouble[0]; // p2d.ptsDouble[0]; // lat
		logger.debug("Lat for row =" + p2d.ptsDouble[1] + " is: " + yll);
		logger.debug("Lng for col =" + p2d.ptsDouble[0] + " is: " + xll);

		p2d.ptsDouble[0] = geoImg.getNumCols();
		p2d.ptsDouble[1] = 0;
		ptsGeo = projI.rasterToEarth(new RasterPoint(p2d.ptsDouble[1], p2d.ptsDouble[0]));
		xur = ptsGeo.getLon();// p2d.ptsDouble[1]; // p2d.ptsDouble[1]; // lng
		yur = ptsGeo.getLat();// p2d.ptsDouble[0]; // p2d.ptsDouble[0]; // lat
		logger.debug("Lat for row =" + p2d.ptsDouble[1] + " is: " + yur);
		logger.debug("Lng for col =" + p2d.ptsDouble[0] + " is: " + xur);

		p2d.ptsDouble[0] = geoImg.getNumCols();
		p2d.ptsDouble[1] = geoImg.getNumRows();
		ptsGeo = projI.rasterToEarth(new RasterPoint(p2d.ptsDouble[1], p2d.ptsDouble[0]));
		xlr = ptsGeo.getLon();// p2d.ptsDouble[1]; // p2d.ptsDouble[1]; // lng
		ylr = ptsGeo.getLat();// p2d.ptsDouble[0]; // p2d.ptsDouble[0]; // lat
		logger.debug("Lat for row =" + p2d.ptsDouble[1] + " is: " + ylr);
		logger.debug("Lng for col =" + p2d.ptsDouble[0] + " is: " + xlr);

		// test
		rp = projI.earthToRaster(ptsGeo);
		logger.debug("verify row: " + (geoImg.getNumRows()) + " is: " + rp.getRow());
		logger.debug("verify col: " + (geoImg.getNumCols()) + "  is: " + rp.getCol());

		double boxBottomWidth = Math.abs(xll - xlr);
		double boxTopWidth = Math.abs(xul - xur);

		if (Math.abs(boxBottomWidth - boxTopWidth) > 20) { // box badly skewed
			// make top and bottom as wide as widest piece
			if (boxBottomWidth > boxTopWidth) {
				xul = xll;
				xur = xlr;
			} else { // top of the box is wider
				xll = xul;
				xlr = xur;
			}
		}

		ShapeObject retShapeObject = null;

		ImPoint centroid = new ImPoint((xll + xul + xlr + xur) / 4.0, (yll + yul + ylr + yur) / 4.0);
		ImPoint pt1, pt2, pt3, pt4;
		ImPoint pTest1, pTest2, pTest3, pTest4;
		ImLine imgLine1 = new ImLine();
		ImLine imgLine2 = new ImLine();
		ImLine imgLine3 = new ImLine();
		ImLine imgLine4 = new ImLine();
		imgLine1.setMaxAllowedSlope(1000000);
		imgLine1.setMinPtsSepar(0.00009);
		imgLine2.setMaxAllowedSlope(1000000);
		imgLine2.setMinPtsSepar(0.00009);
		imgLine3.setMaxAllowedSlope(1000000);
		imgLine3.setMinPtsSepar(0.00009);
		imgLine4.setMaxAllowedSlope(1000000);
		imgLine4.setMinPtsSepar(0.00009);

		pt1 = new ImPoint(xll, yll);
		pt2 = new ImPoint(xlr, ylr);
		pt3 = new ImPoint(xur, yur);
		pt4 = new ImPoint(xul, yul);

		if (!imgLine1.setImLine(pt1, pt2)) {
			logger.debug("ImgLine1 not set. Not constructing ImageShapeObject");
			return null;
		}
		if (!imgLine2.setImLine(pt2, pt3)) {
			logger.debug("ImgLine2 not set. Not constructing ImageShapeObject");
			return null;
		}
		if (!imgLine3.setImLine(pt3, pt4)) {
			logger.debug("ImgLine3 not set. Not constructing ImageShapeObject");
			return null;
		}
		if (!imgLine4.setImLine(pt4, pt1)) {
			logger.debug("ImgLine4 not set. Not constructing ImageShapeObject");
			return null;
		}

		GeomOper geom = new GeomOper();
		double[] bbox = null;
		_validBndIdx = new java.util.Vector();
		boolean validBnd = true; // By default, a boundary is valid
		logger.debug("In GeoFeature.ConstructImageShapeObject: ");
		logger.debug("Number of original boundaries: " + shpObj.GetNumBnd());

		for (int bndIdx = 0; bndIdx < shpObj.GetNumBnd(); bndIdx++) {

			// Find the indices of all valid boundaries.
			// Once this is done, construct a ShapeObject with the valid
			// points of the valid boundary.
			validBnd = true;
			bbox = shpObj.GetBndBox(bndIdx);

			// logger.debug(" bbox[0] = "+bbox[0]+" bbox[1] = "+bbox[1]+"
			// bbox[2] = "+bbox[2]+" bbox[3] = "+bbox[3]);

			// Test all the four points of the boundary's bounding box
			pTest1 = new ImPoint(bbox[0], bbox[1]);
			pTest2 = new ImPoint(bbox[0], bbox[3]);
			pTest3 = new ImPoint(bbox[2], bbox[3]);
			pTest4 = new ImPoint(bbox[2], bbox[1]);

			if (validBnd) {
				// If the boundary's bounding box is outside the image
				// boundaries,
				// then consider the next boundary else store the index of the
				// valid boundary
				// to construct a ShapeObject later.
				_validBndIdx.addElement(new Integer(bndIdx)); // NOTE:::
				// This number corresponds
				// to the actual index in the DBF file - TODO what about header
				// line?
				logger.debug("Inserting valid boundary index: " + bndIdx + " at Table row: " + (_validBndIdx.size() - 1));
			} // if(validBnd)
			else {
				logger.debug("Discarding boundary: " + bndIdx);
				logger.debug("BBox Xmin = " + bbox[0] + " bbox Ymin = " + bbox[1]);
				logger.debug("BBox Xmax = " + bbox[2] + " bbox Ymax = " + bbox[3]);
			}
		} // for(int bndIdx = ...)

		logger.debug("There are " + _validBndIdx.size() + " valid boundaries");
		if (_validBndIdx.size() > 0) { // If there are valid boundaries.

			boolean firstBoundary = true;
			double[] globalBndBox = new double[4];

			retShapeObject = new ShapeObject(_validBndIdx.size());
			java.util.Vector validPartPts = new java.util.Vector();
			java.util.Vector validPartPtsIdx = new java.util.Vector();
			// Stores
			// the
			// starting
			// index
			// of
			// points
			// of parts of all the boundaries.
			boolean entireBndColOut = true;
			boolean entireBndRowOut = true;
			int discardCnt = 0;
			int numDiscarded = 0;
			int numZeroPartsBnd = 0;

			boolean validPtBnd = false; // For use if shape type = POINT
			int numValidParts = 0;
			int numValidPartPts = 0;

			int bndPtsIdx = 0;
			int totalParts = 0;

			int numNewPts = 0;
			// Test all the points of the boundary.
			// If a point is inside the image, insert the point into the
			// ShapeObject
			// Also perform geo-conversion for that point if conversion is
			// required.
			pTest2 = new ImPoint();

			if (shpObj.getAllInternalPoints() != null) {
				retShapeObject.setAllInternalPoints(new Point2DDouble(_validBndIdx.size()));
			}
			// logger.debug("Image numRows = " + geoImg.getNumRows() + " numCols
			// = " + geoImg.getNumCols());
			// logger.debug("_validBndIdx.size()=" + _validBndIdx.size());

			for (int validIdx = 0; validIdx < _validBndIdx.size(); validIdx++) {

				Integer i = (Integer) _validBndIdx.elementAt(validIdx);
				int idx = i.intValue();
				logger.debug("Original Boundary Index found as valid: " + idx + " inserting into ImageShapeObject at index: " + validIdx);
				retShapeObject.SetBndPtsIdx(validIdx, (bndPtsIdx << 1));
				retShapeObject.SetBndType(validIdx, shpObj.GetBndType(idx));
				for (int partIdx = 0; partIdx < shpObj.GetNumBndParts(idx); partIdx++) {
					double[] partPts = shpObj.GetPartPointsForBnd((idx), partIdx);

					entireBndRowOut = true;
					entireBndColOut = true;

					// test
					// logger.debug("Input Counting Pts (partPts.length>>1)=" +
					// (partPts.length >> 1));

					for (int ptIdx = 0; ptIdx < partPts.length; ptIdx += 2) {

						pTest1 = new ImPoint(partPts[ptIdx + 1], partPts[ptIdx]);
						// TODO I assume that the assignment is true PB
						p2d.ptsDouble[0] = pTest1.y;
						// if bGeoProj=true then
						// latitude else
						// projected model point
						// y
						p2d.ptsDouble[1] = pTest1.x;
						// if bGeoProj = true
						// then longitude else
						// projected model point
						// x

						if (!bGeoProj) {
							// this is the case when the Shapefile points are
							// not in lat/long
							ptsGeo = projS.modelToEarth(new ModelPoint(p2d.ptsDouble[1], p2d.ptsDouble[0]));
							rp = projI.earthToRaster(ptsGeo);
						} else {
							rp = projI.earthToRaster(new GeodeticPoint(p2d.ptsDouble[0], p2d.ptsDouble[1]));
						}
						pTest1.y = Math.round(rp.getY()); // Math.round(p2d.ptsDouble[1]);
						pTest1.x = Math.round(rp.getX());// Math.round(p2d.ptsDouble[0])

						if ((pTest1.y < (geoImg.getNumRows())) && (pTest1.y >= 0)) {
							entireBndRowOut = false;
						}

						if (pTest1.y < (geoImg.getNumRows())) {
							if ((pTest1.x < (geoImg.getNumCols())) && (pTest1.x >= 0)) {
								// at
								// least
								// one
								// point
								// is
								// inside
								// the
								// image
								entireBndColOut = false;
							}

							if (numValidPartPts == 0) { // The first point of a
								// part
								// Store the previous point
								pTest2.SetImPoint(pTest1);
								validPartPts.addElement(new ImPoint(pTest1));
								// TO DO!! We should also add the adjoining
								// point of the first
								// and last valid points
								numValidPartPts++;
							} else { // NOTE: This might not work, if points
								// are in lat/lng
								// For all points other than the first.
								// Discard the point that is the same as the
								// previous one
								if ((pTest1.x == pTest2.x) && (pTest1.y == pTest2.y)) {
//									logger.debug("Discarding same point x = " + pTest1.x + " y = " + pTest2.y);
									continue;
								} else {
									validPartPts.addElement(new ImPoint(pTest1));
									numValidPartPts++;
									pTest2.SetImPoint(pTest1);
								}
							}
						} // if pTest1.y < _imgObject.numrows
						else {
//							logger.debug("Discarding y = " + pTest1.y + " numrows in image = " + geoImg.getNumRows());
						}
					} // for(int ptIdx = ...)

					if (numValidPartPts == 1) {
						logger.debug("Only one valid point, this is not a real polygon anymore, adding same point again.");
						validPartPts.addElement(new ImPoint(pTest2));
						numValidPartPts++;
					}
					if (numValidPartPts > 0) {

						if (entireBndColOut || entireBndRowOut) {
							for (discardCnt = 0; discardCnt < numValidPartPts; discardCnt++) {
								validPartPts.removeElementAt(validPartPts.size() - 1);
							}
						} else {
							numValidParts++;
							validPartPtsIdx.addElement(new Integer(numValidPartPts));
						}

						numValidPartPts = 0;

					}

				}// for(int partIdx = ...)

				// logger.debug("Stage 1");

				if (shpObj.GetNumBndParts(idx) == 0) {
					validPtBnd = false;
					numValidParts = 0;
					// For shape type == POINT .. added on 11/18/03
					pts = shpObj.GetPtsForBnd((idx));

					if (pts == null) {
						continue;
					}

					entireBndRowOut = true;
					entireBndColOut = true;

					for (int ptIdx = 0; ptIdx < pts.length; ptIdx += 2) {

						pTest1 = new ImPoint(pts[ptIdx + 1], pts[ptIdx]);
						p2d.ptsDouble[0] = pTest1.y;
						p2d.ptsDouble[1] = pTest1.x;

						if (friday < 50) {
							logger.debug("lat :" + p2d.ptsDouble[0]);
							logger.debug("lng :" + p2d.ptsDouble[1]);
						}
						// pts = projS.ColumnRow2LatLng(p2d.ptsDouble);
						rp = new RasterPoint(p2d.ptsDouble[1], p2d.ptsDouble[0]);
						ptsGeo = projS.rasterToEarth(rp);
						// p2d.SetPoint2DDouble(1, pts);
						// p2d = myGeoConv.LatLng2ColumnRow(p2d);

						if (friday < 50) {
							logger.debug("col :" + p2d.ptsDouble[0]);
							logger.debug("row :" + p2d.ptsDouble[1]);
						}

						friday++;

						pTest1.y = ptsGeo.getLat();// p2d.ptsDouble[1];
						pTest1.x = ptsGeo.getLon();// p2d.ptsDouble[0];

						if ((pTest1.y < geoImg.getNumRows()) && (pTest1.y >= 0)) {
							entireBndRowOut = false;
						}

						if (pTest1.y < geoImg.getNumRows()) { // && pTest1.x)<
							// imgObject.numcols){
							if ((pTest1.x < geoImg.getNumCols()) && (pTest1.x >= 0)) { // at
								// least
								// one
								// point
								// is
								// inside
								// the
								// image
								entireBndColOut = false;
							}

							if (numValidPartPts == 0) { // The first point of a
								// part
								// Store the previous point
								pTest2.SetImPoint(pTest1);
								validPartPts.addElement(new ImPoint(pTest1));
								// TO DO!! We should also add the adjoining
								// point of the first
								// and last valid points
								numValidPartPts++;
							} else { // NOTE: This might not work, if points
								// are in lat/lng
								// For all points other than the first.
								// Discard the point that is the same as the
								// previous one
								if ((pTest1.x == pTest2.x) && (pTest1.y == pTest2.y)) {
//									logger.debug("Discarding same point x = " + pTest1.x + " y = " + pTest2.y);
									continue;
								} else {
									validPartPts.addElement(new ImPoint(pTest1));
									numValidPartPts++;
									pTest2.SetImPoint(pTest1);
								}
							}
						} // if pTest1.y < _imgObject.numrows
						else {
							// logger.debug("Discarding y = "+pTest1.y+" numrows
							// in image = "+imgObject.numrows);
						}
					} // for(int ptIdx = ...)

					if (numValidPartPts > 0) {
						if (entireBndColOut || entireBndRowOut) {
							// logger.debug("Num validPartPts =
							// "+numValidPartPts+" vector size =
							// "+validPartPts.size());
							logger.debug("Entire boundary is outside, discarding it....! BndNum = " + (numDiscarded++) + " entireBndColOut = " + entireBndColOut + " entireBndRowOut = "
									+ entireBndRowOut);
							logger.debug("Discarding boundary at idx = " + (idx + 1));
							// Discard starting from discardIdx, upto
							// numValidPartPts
							for (discardCnt = 0; discardCnt < numValidPartPts; discardCnt++) {
								validPartPts.removeElementAt(validPartPts.size() - 1);
							}
						} else {
							// numValidParts++; // For backward compatibility,
							// name is left unchanged, even
							// though there are no parts for shape type = POINT
							validPtBnd = true;
							validPartPtsIdx.addElement(new Integer(numValidPartPts));
						}

						numValidPartPts = 0;
					}
				} // if(shpObj.GetNumBndParts(idx+1) == 0)
				// logger.debug("Stage 2");

				if (shpObj.getAllInternalPoints() != null) {
					// The internal points are avaiable...
					/*
					 * NOTE: These internal points may no longer be within the image!!!
					 */
					double[] internalPt = shpObj.GetInternalPoint(idx);
					p2d.ptsDouble[0] = internalPt[0];// column
					p2d.ptsDouble[1] = internalPt[1];// row
					// pts = projS.ColumnRow2LatLng(p2d.ptsDouble);
					rp = new RasterPoint(p2d.ptsDouble[1], p2d.ptsDouble[0]);
					ptsGeo = projS.rasterToEarth(rp);

					// p2d.SetPoint2DDouble(1, pts);
					// p2d = myGeoConv.LatLng2ColumnRow(p2d);
					internalPt[0] = Math.round(ptsGeo.getLon());// Math.round(p2d.ptsDouble[1]);//column
					internalPt[1] = Math.round(ptsGeo.getLat());// Math.round(p2d.ptsDouble[0]);
					// //row
					retShapeObject.SetInternalPoint(validIdx, internalPt[0], internalPt[1]);
				}

				if ((numValidParts == 0) && (!validPtBnd)) {
					// This may
					// happen if all
					// pts are
					// outside!!!
					logger.debug("No points for bndIdx = " + (validIdx + 1) + " number of such bnds = " + (numZeroPartsBnd++));
				}

				retShapeObject.SetNumBndParts(validIdx, numValidParts);
				numNewPts = validPartPts.size() - bndPtsIdx;
				retShapeObject.SetNumBndPts(validIdx, numNewPts);
				// Test
				// logger.debug("Number of points: "+numNewPts+" for boundary:
				// "+(validIdx+1));
				bndPtsIdx += numNewPts;
				totalParts += numValidParts;
				numValidParts = 0;

			} // for(int validIdx = ...)

			logger.debug("Countint Valid Pts Complete");
			int ptIdx = 0;
			retShapeObject.setAllBoundaryPoints(new Point2DDouble(validPartPts.size()));

			retShapeObject.setBoundaryAllPartsIndex(new int[totalParts]);
			int retPartIdx = 0;
			int numParts = 0;
			int partPtsIdx = 0;

			// for (int retBndIdx = 1; retBndIdx < _validBndIdx.size() + 1;
			// retBndIdx++) { // changed
			for (int retBndIdx = 0; retBndIdx < _validBndIdx.size(); retBndIdx++) { // changed
				// to
				// start
				// from
				// 1
				// and
				// go
				// upto
				// _validBndIdx
				// + 1

				// logger.debug("Valid Bnd Stage " + retBndIdx);
				// Set the _bndPartIdx
				numParts = retShapeObject.GetNumBndParts(retBndIdx);

				if (numParts > 0) {

					partPtsIdx = 0;
					retShapeObject.setBoundaryPartsIndex(retPartIdx, partPtsIdx);
					retPartIdx++;
					for (int partIdx = 1; partIdx < numParts; partIdx++) {
						Integer i = (Integer) validPartPtsIdx.elementAt(retPartIdx - 1);
						partPtsIdx += i.intValue();
						// retShapeObject.setBoundaryPartsIndex(retPartIdx + 1,
						// partPtsIdx);
						retShapeObject.setBoundaryPartsIndex(retPartIdx, partPtsIdx);
						retPartIdx++;
					} // for(int partIdx = ...)
				}

				bbox[0] = 0.0;
				bbox[1] = 0.0;
				bbox[2] = 0.0;
				bbox[3] = 0.0;

				for (ptIdx = (retShapeObject.GetBndPtsIdx(retBndIdx) / 2); ptIdx < (retShapeObject.GetBndPtsIdx(retBndIdx) / 2) + retShapeObject.GetNumBndPts(retBndIdx); ptIdx++) {
					// Set all the points for the boundary at 'retBndIdx' in the
					// _bndPts array
					ImPoint pt = (ImPoint) validPartPts.elementAt(ptIdx);

					if ((pt.x < 0.0) || (pt.y < 0.0)) {

						// Integer in = (Integer)
						// _validBndIdx.elementAt(retBndIdx - 1);
						Integer in = (Integer) _validBndIdx.elementAt(retBndIdx);

						// logger.debug("WARNING: At BndIdx = "+retBndIdx+" This
						// should have been converted, x: "+pt.x+ " pt.y:
						// "+pt.y+" in boundary index: "+in.intValue());
					}

					retShapeObject.getAllBoundaryPoints().SetValue(ptIdx, pt.y, pt.x);

					if (ptIdx == (retShapeObject.GetBndPtsIdx(retBndIdx) / 2)) {
						// The first point of the boundary
						// Initializing the bounding box for this boundary
						bbox[0] = pt.x;
						bbox[1] = pt.y;
						bbox[2] = bbox[0];
						bbox[3] = bbox[1];
					} else {
						if (bbox[0] > pt.x) {
							bbox[0] = pt.x;
						}
						if (bbox[1] > pt.y) {
							bbox[1] = pt.y;
						}
						if (bbox[2] < pt.x) {
							bbox[2] = pt.x;
						}
						if (bbox[3] < pt.y) {
							bbox[3] = pt.y;
						}
					}
					if (firstBoundary) {
						globalBndBox[0] = bbox[0];
						globalBndBox[1] = bbox[1];
						globalBndBox[2] = bbox[2];
						globalBndBox[3] = bbox[3];
						firstBoundary = false;
					} else {
						if (globalBndBox[0] > bbox[0]) {
							globalBndBox[0] = bbox[0];
						}
						if (globalBndBox[1] > bbox[1]) {
							globalBndBox[1] = bbox[1];
						}
						if (globalBndBox[2] < bbox[2]) {
							globalBndBox[2] = bbox[2];
						}
						if (globalBndBox[3] < bbox[3]) {
							globalBndBox[3] = bbox[3];
						}
					}
				} // for(int ptIdx = ...)

				// increase the size to accomodate edges of boundaries
				bbox[0] -= 1.0;
				bbox[1] -= 1.0;
				bbox[2] += 1.0;
				bbox[3] += 1.0;
				retShapeObject.SetBndBox(retBndIdx, bbox);
			}// for(int retBndIdx = ...)

			logger.debug("Valid Bnd Stage Complete");
			// logger.debug("Total number of boundaries =
			// "+retShapeObject.GetNumBnd()+" with total number of points =
			// "+retShapeObject.GetBndPts());
			// increase the size to accommodate edges of boundaries
			globalBndBox[0] -= 1.0;
			globalBndBox[1] -= 1.0;
			globalBndBox[2] += 1.0;
			globalBndBox[3] += 1.0;
			retShapeObject.SetGlobalBndBox(globalBndBox);
		} // if(_validBndIdx.size() > 0)
		else {
			logger.debug("No overlap between the image and the loaded shape");
		}

		if (retShapeObject != null) {
			retShapeObject.SetIsInPixel(true);
		}

		logger.debug("Pixel Shape Stage Complete");
		return retShapeObject;
	}
} // end of GeoFeature class
