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
package edu.illinois.ncsa.isda.im2learn.core.geo;

import java.io.IOException;
import java.io.PrintStream;

import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageException;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.im2learn.core.datatype.Point2DDouble;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ShapeObject;
import edu.illinois.ncsa.isda.im2learn.core.io.ImageLoader;


/**
 * Utility functions operating on geographic data.
 */
public class GeoUtilities {
	static public double degMinSecToDecim(double deg, double min, double sec) {
		int sign;
		if (deg >= 0) {
			sign = 1;
		} else {
			sign = -1;
		}
		return ((Math.abs(deg) + Math.abs(min) / 60.0 + Math.abs(sec) / 3600.0) * sign);
	}

	static public double[] decimToDegMinSec(double decim) {
		double frac, temp;
		int sign;
		if (decim >= 0) {
			sign = 1;
		} else {
			sign = -1;
		}
		double[] res = new double[3];
		// res[0] = degrees, res[1] = minutes, res[2]= seconds
		res[0] = res[1] = res[2] = -1.0;

		// degrees
		// frac = modf(fabs(decim),res[0]);
		res[0] = Math.floor(Math.abs(decim));
		frac = Math.abs(decim) - res[0];
		res[0] *= sign;
		temp = frac * 60.0;

		// minutes
		// frac = modf(temp,res[1]);
		res[1] = Math.floor(Math.abs(temp));
		frac = Math.abs(temp) - res[1];
		res[1] *= sign;

		// seconds
		res[2] = frac * 60.0 * sign;

		return res;
	}

	static public ImageObject getImageObject(ShapeObject so, int numcols) throws ImageException {
		double[] bbox = so.getGlobalBoundingBox();
		double scale = (bbox[2] - bbox[0]) / numcols;
		int numrows = (int) Math.ceil((0.5 + (bbox[3] - bbox[1]) / scale));

		ImageObject imgobj = ImageObject.createImage(numrows + 1, numcols + 1, 1, ImageObject.TYPE_BYTE);
		imgobj.setData(255);

		TiePoint tp = so.getProjection().getTiePoint();
		so.getProjection().setTiePoint(new TiePoint(new ModelPoint(bbox[0], bbox[3]), new RasterPoint(0, 0), scale, -scale, 1));

		int numBoundaries = so.getNumBoundaries();
		ModelPoint mp1 = new ModelPoint();
		ModelPoint mp2 = new ModelPoint();
		RasterPoint rp1;
		RasterPoint rp2;
		// for each boundary
		for (int index = 0; index < numBoundaries; index++) {

			// boundary is a point
			if (so.getNumBoundaryPts(index) == 1) {
				double[] point = so.GetPtsForBnd(index);
				mp1.setX(point[1]);
				mp1.setY(point[0]);
				rp1 = so.getProjection().modelToRaster(mp1);
				imgobj.set((int) rp1.getY(), (int) rp1.getX(), 0, 0);
			} else { // boundary is a polygon
				int j;
				double[] partPts = null;
				int idx;
				// For each boundary, draw each part joined by lines.
				int numBndParts = so.GetNumBndParts(index);

				for (j = 0; j < numBndParts; j++) {
					partPts = so.GetPartPointsForBnd(index, j);
					if (partPts != null) {
						for (idx = 2; idx < (partPts.length - 1); idx += 2) {
							mp1.setX(partPts[idx - 1]);
							mp1.setY(partPts[idx - 2]);
							rp1 = so.getProjection().modelToRaster(mp1);
							mp2.setX(partPts[idx + 1]);
							mp2.setY(partPts[idx]);
							rp2 = so.getProjection().modelToRaster(mp2);
							drawLine(imgobj, rp1.getX(), rp1.getY(), rp2.getX(), rp2.getY());
						} // for(int idx = ...)
					} // if (partPts != null)
				} // for( j = ...)
			} // else when the shape of not a ShapePoint
		}

		so.getProjection().setTiePoint(tp);
		imgobj.setProperty(ImageObject.GEOINFO, so.getProjection());
		return imgobj;
	}

	static private void drawLine(ImageObject imgobj, double x1, double y1, double x2, double y2) {
		double dx = (x2 - x1);
		double dy = (y2 - y1);
		if (dx == 0) {
			if (y1 < y2) {
				for (double y = y1; y < y2; y++) {
					imgobj.set((int) y, (int) x1, 0, 0);
				}
			} else {
				for (double y = y1; y > y2; y--) {
					imgobj.set((int) y, (int) x1, 0, 0);
				}
			}
		} else if (dy == 0) {
			if (x1 < x2) {
				for (double x = x1; x < x2; x++) {
					imgobj.set((int) y1, (int) x, 0, 0);
				}
			} else {
				for (double x = x1; x > x2; x--) {
					imgobj.set((int) y1, (int) x, 0, 0);
				}
			}
		} else {
			if (dx < dy) {
				if (y1 < y2) {
					double m = dx / dy;
					for (double y = y1, x = x1; y < y2; y++, x += m) {
						imgobj.set((int) y, (int) x, 0, 0);
					}
				} else {
					double m = dx / -dy;
					for (double y = y1, x = x1; y > y2; y--, x += m) {
						imgobj.set((int) y, (int) x, 0, 0);
					}
				}
			} else {
				if (x1 < x2) {
					double m = dy / dx;
					for (double x = x1, y = y1; x < x2; x++, y += m) {
						imgobj.set((int) y, (int) x, 0, 0);
					}
				} else {
					double m = dy / -dx;
					for (double x = x1, y = y1; x > x2; x--, y += m) {
						imgobj.set((int) y, (int) x, 0, 0);
					}
				}

			}
		}
	}

	static public ShapeObject reproject(ShapeObject shape, Projection targetproj) throws GeoException, ImageException {
		// for calculation of global bounding box
		double xmin, xmax, ymin, ymax;
		xmin = ymin = Double.MAX_VALUE;
		xmax = ymax = -Double.MAX_VALUE;

		// make a copy
		ShapeObject so = shape.CopyShapeObject();
		Point2DDouble pd = so.getAllBoundaryPoints();
		double[] val = pd.ptsDouble;
		GeodeticPoint gp;
		ModelPoint mp = new ModelPoint();
		for (int i = 0; i < val.length; i += 2) {
			// setup starting Point
			mp.setY(val[i]);
			mp.setX(val[i + 1]);
			mp.setZ(0);

			// reproject
			gp = so.getProjection().modelToEarth(mp);
			mp = targetproj.earthToModel(gp);

			// store new values
			val[i] = mp.getY();
			val[i + 1] = mp.getX();

			// compute bbox
			if (mp.getX() > xmax) {
				xmax = mp.getX();
			}
			if (mp.getX() < xmin) {
				xmin = mp.getX();
			}
			if (mp.getY() > ymax) {
				ymax = mp.getY();
			}
			if (mp.getY() < ymin) {
				ymin = mp.getY();
			}
		}

		// finish reprojection
		so.setProjection(targetproj);
		so.setGlobalBoundingBox(new double[] { xmin, ymin, xmax, ymax });

		return so;
	}

	static public ImageObject reproject(ImageObject imgobj, Projection targetproj) throws GeoException, ImageException {
		// get the projection from the image
		Projection imgproj = ProjectionConvert.getNewProjection(imgobj.getProperty(ImageObject.GEOINFO));

		// compute the min and max for x and y based on the projects.
		BoundingBox bbox = getImageBound(imgobj, targetproj);

		// / need to fix the tiepoint
		RasterPoint rp = targetproj.getTiePoint().getRasterPoint();
		rp.setX(rp.getX() - bbox.getMinX());
		rp.setY(rp.getY() - bbox.getMinY());

		// compute the numrows and numcols of the new image.
		int numrows = (int) (bbox.getH() + 0.5);
		int numcols = (int) (bbox.getW() + 0.5);
		if ((numrows < 1) || (numcols < 1)) {
			throw new GeoException(String.format("The resulting image has invalid dimensions: numrows=%d numcols=%d", numrows, numcols));
		}

		// create the image
		ImageObject result = ImageObject.createImage(numrows, numcols, imgobj.getNumBands(), imgobj.getType());

		// set the invalid data based on the invalid data read.
		result.setInvalidData(imgobj.getInvalidData());
		result.setData(imgobj.getInvalidData());

		// reproject all pixels from new image to old image
		RasterPoint rp1 = new RasterPoint(0, 0);
		RasterPoint rp2 = null;
		GeodeticPoint gp1 = null;
		int indexFrom, indexTo, i, j, k;
		double fromRow = 0, fromCol = 0;
		double val = 0.0;
		for (i = 0; i < result.getNumRows(); i++) {
			for (j = 0; j < result.getNumCols(); j++) {
				// from resulting coordinate system to original data coordinate
				// system
				rp1.setRow(i);
				rp1.setCol(j);
				gp1 = targetproj.rasterToEarth(rp1);
				rp2 = imgproj.earthToRaster(gp1); // this is taking care of
				// datum shifts
				fromRow = rp2.getRow();
				fromCol = rp2.getCol();
				if ((fromRow >= 0) && (fromCol >= 0) && (fromRow < imgobj.getNumRows()) && (fromCol < imgobj.getNumCols())) {
					indexTo = (i * result.getNumCols() + j) * result.getNumBands();
					// TODO this should have some filtering (right now uses
					// nearest neighbor)
					indexFrom = ((int) (fromRow + 0.5) * imgobj.getNumCols() + (int) (fromCol + 0.5)) * imgobj.getNumBands();
					// check the upper limit due to the rounding above
					if (indexFrom >= imgobj.getSize() - imgobj.getNumBands()) {
						indexFrom = imgobj.getSize() - imgobj.getNumBands() - 1;
					}
					for (k = 0; k < result.getNumBands(); k++) {
						val = imgobj.getDouble(indexFrom + k);
						result.set(indexTo + k, val);
					}

				}

			}
		}
		result.setProperty(ImageObject.GEOINFO, ProjectionConvert.getOldProjection(targetproj));

		return result;
	}

	static public BoundingBox getImageBound(ImageObject imgobj, Projection prj) throws GeoException {
		double xmin, xmax, ymin, ymax;
		Projection imgproj = ProjectionConvert.getNewProjection(imgobj.getProperty(ImageObject.GEOINFO));

		RasterPoint rp = new RasterPoint(0, 0);
		rp = prj.earthToRaster(imgproj.rasterToEarth(rp));
		xmin = xmax = rp.getX();
		ymin = ymax = rp.getY();

		rp.setCol(imgobj.getNumCols());
		rp.setRow(0);
		rp = prj.earthToRaster(imgproj.rasterToEarth(rp));
		if (rp.getY() < ymin) {
			ymin = rp.getY();
		}
		if (rp.getY() > ymax) {
			ymax = rp.getY();
		}
		if (rp.getX() < xmin) {
			xmin = rp.getX();
		}
		if (rp.getX() > xmax) {
			xmax = rp.getX();
		}

		rp.setCol(0);
		rp.setRow(imgobj.getNumRows());
		rp = prj.earthToRaster(imgproj.rasterToEarth(rp));
		if (rp.getY() < ymin) {
			ymin = rp.getY();
		}
		if (rp.getY() > ymax) {
			ymax = rp.getY();
		}
		if (rp.getX() < xmin) {
			xmin = rp.getX();
		}
		if (rp.getX() > xmax) {
			xmax = rp.getX();
		}

		rp.setCol(imgobj.getNumCols());
		rp.setRow(imgobj.getNumRows());
		rp = prj.earthToRaster(imgproj.rasterToEarth(rp));
		if (rp.getY() < ymin) {
			ymin = rp.getY();
		}
		if (rp.getY() > ymax) {
			ymax = rp.getY();
		}
		if (rp.getX() < xmin) {
			xmin = rp.getX();
		}
		if (rp.getX() > xmax) {
			xmax = rp.getX();
		}

		return new BoundingBox(xmin, ymin, xmax - xmin, ymax - ymin);
	}

	static public BoundingBox getEarthBounds(ShapeObject so) throws GeoException {
		Projection proj = so.getProjection();
		if (proj == null) {
			throw (new GeoException("No projection associated with the shapeobject."));
		}
		return getEarthBounds(so, proj);
	}

	static public BoundingBox getEarthBounds(ShapeObject so, Projection prj) throws GeoException {
		double xmin, xmax, ymin, ymax;

		double[] bbox = so.getGlobalBoundingBox();

		ModelPoint mp = new ModelPoint(bbox[0], bbox[1]);
		GeodeticPoint gp = prj.modelToEarth(mp);
		xmin = xmax = gp.getLon();
		ymin = ymax = gp.getLat();

		mp.setX(bbox[2]);
		mp.setY(bbox[1]);
		gp = prj.modelToEarth(mp);
		if (gp.getLat() < ymin) {
			ymin = gp.getLat();
		}
		if (gp.getLat() > ymax) {
			ymax = gp.getLat();
		}
		if (gp.getLon() < xmin) {
			xmin = gp.getLon();
		}
		if (gp.getLon() > xmax) {
			xmax = gp.getLon();
		}

		mp.setX(bbox[0]);
		mp.setY(bbox[3]);
		gp = prj.modelToEarth(mp);
		if (gp.getLat() < ymin) {
			ymin = gp.getLat();
		}
		if (gp.getLat() > ymax) {
			ymax = gp.getLat();
		}
		if (gp.getLon() < xmin) {
			xmin = gp.getLon();
		}
		if (gp.getLon() > xmax) {
			xmax = gp.getLon();
		}

		mp.setX(bbox[2]);
		mp.setY(bbox[3]);
		gp = prj.modelToEarth(mp);
		if (gp.getLat() < ymin) {
			ymin = gp.getLat();
		}
		if (gp.getLat() > ymax) {
			ymax = gp.getLat();
		}
		if (gp.getLon() < xmin) {
			xmin = gp.getLon();
		}
		if (gp.getLon() > xmax) {
			xmax = gp.getLon();
		}

		return new BoundingBox(xmin, ymin, xmax - xmin, ymax - ymin);
	}

	static public BoundingBox getEarthBounds(ImageObject imgobj) throws GeoException {
		Projection proj = ProjectionConvert.getNewProjection(imgobj.getProperty(ImageObject.GEOINFO));
		if (proj == null) {
			throw (new GeoException("No projection associated with the image."));
		}
		return getEarthBounds(imgobj, proj);
	}

	static public BoundingBox getEarthBounds(ImageObject imgobj, Projection prj) throws GeoException {
		double xmin, xmax, ymin, ymax;

		RasterPoint rp = new RasterPoint(0, 0);
		GeodeticPoint gp = prj.rasterToEarth(rp);
		xmin = xmax = gp.getLon();
		ymin = ymax = gp.getLat();

		rp.setCol(imgobj.getNumCols());
		rp.setRow(0);
		gp = prj.rasterToEarth(rp);
		if (gp.getLat() < ymin) {
			ymin = gp.getLat();
		}
		if (gp.getLat() > ymax) {
			ymax = gp.getLat();
		}
		if (gp.getLon() < xmin) {
			xmin = gp.getLon();
		}
		if (gp.getLon() > xmax) {
			xmax = gp.getLon();
		}

		rp.setCol(0);
		rp.setRow(imgobj.getNumRows());
		gp = prj.rasterToEarth(rp);
		if (gp.getLat() < ymin) {
			ymin = gp.getLat();
		}
		if (gp.getLat() > ymax) {
			ymax = gp.getLat();
		}
		if (gp.getLon() < xmin) {
			xmin = gp.getLon();
		}
		if (gp.getLon() > xmax) {
			xmax = gp.getLon();
		}

		rp.setCol(imgobj.getNumCols());
		rp.setRow(imgobj.getNumRows());
		gp = prj.rasterToEarth(rp);
		if (gp.getLat() < ymin) {
			ymin = gp.getLat();
		}
		if (gp.getLat() > ymax) {
			ymax = gp.getLat();
		}
		if (gp.getLon() < xmin) {
			xmin = gp.getLon();
		}
		if (gp.getLon() > xmax) {
			xmax = gp.getLon();
		}

		return new BoundingBox(xmin, ymin, xmax - xmin, ymax - ymin);
	}

	static public BoundingBox getModelBounds(ImageObject imgobj, Projection prj) throws GeoException {
		double xmin, xmax, ymin, ymax;

		RasterPoint rp = new RasterPoint(0, 0);
		ModelPoint mp = prj.rasterToModel(rp);
		xmin = xmax = mp.getX();
		ymin = ymax = mp.getY();

		rp.setCol(imgobj.getNumCols());
		rp.setRow(0);
		mp = prj.rasterToModel(rp);
		if (mp.getY() < ymin) {
			ymin = mp.getY();
		}
		if (mp.getY() > ymax) {
			ymax = mp.getY();
		}
		if (mp.getX() < xmin) {
			xmin = mp.getX();
		}
		if (mp.getX() > xmax) {
			xmax = mp.getX();
		}

		rp.setCol(0);
		rp.setRow(imgobj.getNumRows());
		mp = prj.rasterToModel(rp);
		if (mp.getY() < ymin) {
			ymin = mp.getY();
		}
		if (mp.getY() > ymax) {
			ymax = mp.getY();
		}
		if (mp.getX() < xmin) {
			xmin = mp.getX();
		}
		if (mp.getX() > xmax) {
			xmax = mp.getX();
		}

		rp.setCol(imgobj.getNumCols());
		rp.setRow(imgobj.getNumRows());
		mp = prj.rasterToModel(rp);
		if (mp.getY() < ymin) {
			ymin = mp.getY();
		}
		if (mp.getY() > ymax) {
			ymax = mp.getY();
		}
		if (mp.getX() < xmin) {
			xmin = mp.getX();
		}
		if (mp.getX() > xmax) {
			xmax = mp.getX();
		}

		return new BoundingBox(xmin, ymin, xmax - xmin, ymax - ymin);
	}

	/**
	 * This is the method that takes raster file and extracts georeferencing information and saves it out as PRJ file
	 * 
	 * @param InRasterFileName
	 * @param OutPRJFileName
	 * @throws GeoException
	 * @throws IOException
	 * @throws ImageException
	 */
	static public void createPRJFileBasedonRasterFile(String InRasterFileName, String OutPRJFileName) throws GeoException, IOException, ImageException {

		// sanity check
		if ((InRasterFileName == null) || (OutPRJFileName == null)) {
			throw new GeoException(String.format("The file names are not specified"));
		}
		if (InRasterFileName.trim().length() == 0) {
			throw new GeoException(String.format("The input file is empty"));
		}
		// //////////////////////
		// load file
		ImageObject testObject = null;
		testObject = ImageLoader.readImage(InRasterFileName);
		// sanity check
		if (testObject == null) {
			throw new GeoException(String.format("The input file could not be read "));
		}
		if (testObject.getProperty(ImageObject.GEOINFO) == null) {
			throw new GeoException(String.format("The input file does not have any gereferencing information "));
		}

		// check
		System.out.println("Info: this is the input image filename=" + InRasterFileName);
		System.out.println(testObject.toString());
		System.out.println(testObject.getProperty(ImageObject.GEOINFO).toString());

		// check
		System.out.println("Info: this is the output PRJ filename=" + OutPRJFileName);

		// //////////////////////
		// save out the PRJ file
		PrintStream ps = new PrintStream(OutPRJFileName);
		ps.print(ProjectionConvert.getNewProjection(testObject.getProperty(ImageObject.GEOINFO)).toString());
		ps.close();

	}

}
