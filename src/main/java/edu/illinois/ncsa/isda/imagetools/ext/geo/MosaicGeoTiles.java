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
package edu.illinois.ncsa.isda.imagetools.ext.geo;

import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageException;
import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.imagetools.core.datatype.SubArea;
import edu.illinois.ncsa.isda.imagetools.core.geo.GeoException;
import edu.illinois.ncsa.isda.imagetools.core.geo.projection.ModelProjection;
import edu.illinois.ncsa.isda.imagetools.core.geo.projection.Projection;
import edu.illinois.ncsa.isda.imagetools.core.io.ImageLoader;

/**
 * 
 * @todo when projections are all the same----the block for geographic and the
 *       block for UTM are 90% the same. merge them into one.
 * @todo if all tiles have same projection, we still need to account for
 *       different number of bands. insert() only works when the same number of
 *       bands are present
 * @todo SubArea is never used! what is it used for anyway?
 */
public class MosaicGeoTiles {

    // public static void main(String[] args) {
    // String[] filenames = {
    // "/home/clutter/datasets/DOQ/Vol010/16tcm750770.tif",
    // "/home/clutter/datasets/DOQ/Vol010/16tcm750785.tif"
    // };
    //
    // /*String[] filenames = {
    // "/home/clutter/NASA_Data/2003193.h11v04.Lai_1km.tif",
    // "/home/clutter/NASA_Data/SRTM/w78n39.dt1"
    // };*/
    //
    // /*String[] filenames = {
    // "/home/clutter/datasets/NED_IL_CD1/area01/subsampleby30.hdr",
    // "/home/clutter/datasets/NED_IL_CD2/area01/subsampleby30.hdr"
    // };*/
    //
    // try {
    // ImageObject img = MosaicGeoTiles.mosaic(filenames, new SubArea());
    // Im2LearnMainFrame mainFrame = new ncsa.im2learn.main.Im2LearnNCSA();
    // ImagePanel pnl = mainFrame.getImagePanel();
    // //pnl.setAutozoom(false);
    // pnl.setImageObject(img);
    // }
    // catch (Exception ex) {
    // ex.printStackTrace();
    // }
    // }

    /*
     * public static void main(String[] args) { String[] filenames = {
     * "/home/clutter/NASA_Data/2003193.h11v04.Lai_1km.tif",
     * "/home/clutter/NASA_Data/SRTM/w78n39.dt1" };
     * 
     * try { ImageObject img = MosaicGeoTiles.mosaic(filenames, new SubArea());
     * Im2LearnMainFrame mainFrame = new ncsa.im2learn.main.Im2LearnNCSA();
     * ImagePanel pnl = mainFrame.getImagePanel(); pnl.setAutozoom(false);
     * pnl.setImageObject(img); } catch (Exception ex) { ex.printStackTrace(); }
     * 
     *  }
     */

    static private int[] invalidatevalues = { 9999, -9999, 99999, -99999 };
    static private Log   logger           = LogFactory.getLog(MosaicGeoTiles.class);

    // /////////////////////////////////////////////////////////////////////////
    // this method loads several georeferenced 2D images into one big image in a
    // row-col fashion
    public static ImageObject mosaic(Vector geoImgs, SubArea area) throws ImageException {

        int numImgs = geoImgs.size();

        ImageObject reprojected[] = new ImageObject[numImgs];
        ImageObject imgStd = (ImageObject) geoImgs.get(0);
        Projection prjStd = ((Projection) imgStd.getProperty(ImageObject.GEOINFO));
        int nMinXs[] = new int[numImgs];
        int nMaxXs[] = new int[numImgs];
        int nMinYs[] = new int[numImgs];
        int nMaxYs[] = new int[numImgs];

        int nGlobalMinX = Integer.MAX_VALUE;
        int nGlobalMaxX = Integer.MIN_VALUE;
        int nGlobalMinY = Integer.MAX_VALUE;
        int nGlobalMaxY = Integer.MIN_VALUE;

        // Calculate each reprojected image

        for (int n = 0; n < numImgs; n++) {
            ImageObject imgnew;

            ImageObject imgTarget = (ImageObject) geoImgs.get(n);
            Projection prjTarget = ((Projection) imgTarget.getProperty(ImageObject.GEOINFO));

            int nColTarget, nRowTarget, nBandTarget;

            nColTarget = imgTarget.getCols();
            nRowTarget = imgTarget.getRows();
            nBandTarget = imgTarget.getBands();

            int nMinX = Integer.MAX_VALUE;
            int nMaxX = Integer.MIN_VALUE;
            int nMinY = Integer.MAX_VALUE;
            int nMaxY = Integer.MIN_VALUE;
            float fDark = Integer.MAX_VALUE;

            // Calculate Minimum-Bounding-Rectangle of each reprojected image

            for (int i = 0; i < nRowTarget; i++) {
                for (int j = 0; j < nColTarget;) {
                    double point[] = new double[2];
                    double latlng[], ret[];
                    point[0] = j;
                    point[1] = i;
                    try {

                        latlng = prjTarget.rasterToEarth(point);
                        ret = prjStd.earthToRaster(latlng);
                        int nColnew = (int) Math.round(ret[0]);
                        int nRowNew = (int) Math.round(ret[1]);
                        // find the darkest color in the outline edges
                        fDark = Math.min(fDark, imgTarget.getFloat(i, j, 0));

                        nMinX = Math.min(nMinX, nColnew);
                        nMaxX = Math.max(nMaxX, nColnew);
                        nMinY = Math.min(nMinY, nRowNew);
                        nMaxY = Math.max(nMaxY, nRowNew);
                    } catch (GeoException a) {
                        System.out.println("prj1.rasterToEarth Error");
                        return null;
                    }

                    // Only outline edges are tested

                    if (i == 0 || i == nRowTarget - 1) {
                        j++;
                    } else {
                        j += (nColTarget - 1);
                    }
                }
            }

            try {

                // Create a new image based on the selected standard image

                imgnew = (ImageObject) imgStd.clone();
                int nRownew = nMaxY - nMinY + 1;
                int nColnew = nMaxX - nMinX + 1;
                imgnew.setRows(nRownew);
                imgnew.setCols(nColnew);
                imgnew.setBands(nBandTarget);
                // imgnew.setSize(nColnew, nRownew, nBandTarget);
                imgnew.setImageObjectValue(imgStd.getInvalidData());

                nMinXs[n] = nMinX;
                nMaxXs[n] = nMaxX;
                nMinYs[n] = nMinY;
                nMaxYs[n] = nMaxY;

                nGlobalMinX = Math.min(nGlobalMinX, nMinX);
                nGlobalMaxX = Math.max(nGlobalMaxX, nMaxX);
                nGlobalMinY = Math.min(nGlobalMinY, nMinY);
                nGlobalMaxY = Math.max(nGlobalMaxY, nMaxY);

                double originalpoint[] = new double[2];
                double originalmodel[];

                Projection prjnew = ((Projection) imgnew.getProperty(ImageObject.GEOINFO));
                originalpoint[0] = nMinXs[n];
                originalpoint[1] = nMaxYs[n];

                try {

                    // Adjust offset values

                    if (prjStd.getType() == Projection.GEOGRAPHIC) {
                        originalmodel = prjStd.rasterToEarth(originalpoint);
                        prjnew.setInsertionX(originalmodel[1]);
                        prjnew.setInsertionY(originalmodel[0]);
                    } else {
                        originalmodel = ((ModelProjection) prjStd).rasterToModel(originalpoint);
                        prjnew.setInsertionX(originalmodel[0]);
                        prjnew.setInsertionY(originalmodel[1]);
                    }
                    prjnew.setNumCols(nColnew);
                    prjnew.setNumRows(nRownew);
                    prjnew.SetRasterSpaceJ(nRownew);
                    imgnew.setProperty(ImageObject.GEOINFO, prjnew);

                    double point[] = new double[2];
                    double latlng[], ret[];

                    // Resample each point of the reprojected image

                    for (int b = 0; b < nBandTarget; b++) {
                        for (int i = 0; i < nRownew; i++) {
                            for (int j = 0; j < nColnew; j++) {
                                point[0] = j;
                                point[1] = i;

                                latlng = prjnew.rasterToEarth(point);
                                ret = prjTarget.earthToRaster(latlng);

                                int newc = (int) Math.round(ret[0]);
                                int newr = (int) Math.round(ret[1]);

                                if (newc >= 0 && newc < nColTarget && newr >= 0 && newr < nRowTarget) {
                                    double value = imgTarget.getDouble(newr, newc, b);
                                    if (IsValid(value)) {
                                        imgnew.set(i, j, b, value);
                                    }
                                }

                            }
                        }
                    }

                } catch (GeoException b) {
                }
            } catch (CloneNotSupportedException a) {
                System.out.println("(ImageObject)img1.clone Error");
                return null;
            }

            reprojected[n] = imgnew;
        }

        // Make a big image
        ImageObject baseline;

        try {

            baseline = (ImageObject) imgStd.clone();
            int nRowbig = nGlobalMaxY - nGlobalMinY + 1;
            int nColbig = nGlobalMaxX - nGlobalMinX + 1;
            int nBandbig = imgStd.getBands();
            baseline.setRows(nRowbig);
            baseline.setCols(nColbig);
            baseline.setBands(nBandbig);
            // baseline.setSize(nColbig, nRowbig, 1);
            baseline.setImageObjectValue(imgStd.getInvalidData());

            for (int n = 0; n < numImgs; n++) {

                ImageObject imgSource = reprojected[n];
                int nColSource, nRowSource, nBandSource;

                nColSource = imgSource.getCols();
                nRowSource = imgSource.getRows();
                nBandSource = imgSource.getBands();

                for (int i = 0; i < nRowSource; i++) {
                    for (int j = 0; j < nColSource; j++) {
                        for (int k = 0; k < nBandSource; k++) {
                            // if (baseline.getDouble(i + nMinYs[n] -
                            // nGlobalMinY, j + nMinXs[n] - nGlobalMinX, 0) <
                            // imgSource.getDouble(i,j,0))
                            double value = imgSource.getDouble(i, j, k);
                            if (IsValid(value)) {
                                baseline.set(i + nMinYs[n] - nGlobalMinY, j + nMinXs[n] - nGlobalMinX, k, value);
                            }
                        }
                    }
                }
            }

            double originalpoint[] = new double[2];
            double originalmodel[];

            // Adjust offset values
            try {

                Projection prjBaseline = ((Projection) baseline.getProperty(ImageObject.GEOINFO));
                originalpoint[0] = nGlobalMinX;
                originalpoint[1] = nGlobalMaxY;

                if (prjBaseline.getType() == Projection.GEOGRAPHIC) {
                    originalmodel = prjBaseline.rasterToEarth(originalpoint);
                    prjBaseline.setInsertionX(originalmodel[1]);
                    prjBaseline.setInsertionY(originalmodel[0]);
                } else {
                    originalmodel = ((ModelProjection) prjBaseline).rasterToModel(originalpoint);
                    prjBaseline.setInsertionX(originalmodel[0]);
                    prjBaseline.setInsertionY(originalmodel[1]);
                }

                prjBaseline.setNumCols(nGlobalMaxX - nGlobalMinX + 1);
                prjBaseline.setNumRows(nGlobalMaxY - nGlobalMinY + 1);
                prjBaseline.SetRasterSpaceJ(nGlobalMaxY - nGlobalMinY + 1);
                baseline.setProperty(ImageObject.GEOINFO, prjBaseline);
            } catch (GeoException a) {

            }

        } catch (CloneNotSupportedException a) {
            System.out.println("(ImageObject)img1.clone Error");
            return null;
        }

        return baseline;
    }

    private static boolean IsValid(double dValue) {
        for (int i = 0; i < invalidatevalues.length; i++) {
            if (dValue == invalidatevalues[i]) {
                return false;
            }
        }

        return true;
    }

    // /////////////////////////////////////////////////////////////////////////
    // this method loads several georeferenced 2D images into one big image in a
    // row-col fashion
    public static ImageObject mosaic(String[] FileName, SubArea area) throws ImageException {

        logger.debug("Begin Mosaicing");

        int numFiles = FileName.length;

        // boolean ret = false;
        boolean floatSampleAvailable = false;
        // boolean byteSampleAvailable = false;
        // boolean shortSampleAvailable = false;
        // boolean intSampleAvailable = false;
        // boolean longSampleAvailable = false;
        // boolean doubleSampleAvailable = false;

        // 'max' values work only for North and Western Hemisphere
        // double curMaxNorthLat = -1;
        double maxNorthLat = -Double.MAX_VALUE;

        // double curMaxSouthLat = -1;
        double maxSouthLat = Double.MAX_VALUE;

        // double curMaxWestLng = -1;
        double maxWestLng = Double.MAX_VALUE;

        // double curMaxEastLng = -1;
        double maxEastLng = -Double.MAX_VALUE;

        // double curColResolution = -1;
        double maxColResolution = -1;

        // double curRowResolution = -1;
        double maxRowResolution = -1;

        int topTileIndex = 0;
        int bottomTileIndex = 0;
        int leftTileIndex = 0;
        int rightTileIndex = 0;

        int pixelDistanceHeight = -1;
        int pixelDistanceWidth = -1;

        int maxColTileIndex = -1;
        int maxRowTileIndex = -1;

        // ArrayList geoImageVec = new ArrayList();

        int maxSampPerPixelSize = -1;

        int sampType;

        // do all the tiles have the same projection?
        boolean allTilesHaveSameProjection = true;
        int projectionType = -1;

        // keep all the headers --- they are small.
        ImageObject[] tileHeaders = new ImageObject[numFiles];

        // ////////////////////////////////////
        // Read in all the headers and determine if they are in the same
        // projection and the sample type

        Projection lastProjection = null;

        for (int band = 0; band < numFiles; band++) {
            try {
                ImageObject header = ImageLoader.readImageHeader(FileName[band]);
                tileHeaders[band] = header;
            } catch (IOException ex) {
            }

            // Perform check here to see if all tiles are in the same projection
            if (tileHeaders[band] != null) {
                Projection proj = (Projection) tileHeaders[band].getProperty(ImageObject.GEOINFO);

                // we need to check to see if all tiles are in the same
                // projection

                // if this is the first header, then it is trivially true
                if (band == 0) {
                    allTilesHaveSameProjection = true;
                    projectionType = proj.getType();
                }

                // otherwise we just need need to check with the previous
                // projection
                // until we find a different projection
                else if (allTilesHaveSameProjection) {
                    if (lastProjection.getType() != proj.getType()) {
                        allTilesHaveSameProjection = false;
                    }
                }
                lastProjection = proj;

                int curSampType = tileHeaders[band].getType();

                switch (curSampType) {
                case (ImageObject.TYPE_FLOAT):
                    floatSampleAvailable = true;
                    break;
                /*
                 * case (ImageObject.TYPE_INT): intSampleAvailable = true;
                 * break; case (ImageObject.TYPE_LONG): longSampleAvailable =
                 * true; break; case (ImageObject.TYPE_SHORT):
                 * shortSampleAvailable = true; break; case
                 * (ImageObject.TYPE_BYTE): byteSampleAvailable = true; break;
                 * case (ImageObject.TYPE_DOUBLE): doubleSampleAvailable = true;
                 */
                }
            }
        }

        // /////////////////////////////////////////////////////
        // now we know if they all have the same projection.
        // if they are in the same projection, find the max and min of the
        // four corners in the native projection of the tiles
        // IE - don't convert to lat/lng or UTM if you don't have to.

        // allTilesHaveSameProjection = false;

        logger.debug("All Tile Have the same Projection : " + allTilesHaveSameProjection);

        if (allTilesHaveSameProjection) {

            // find the dimensions of the resulting image

            // if it is a ModelProjection, we will use the native model
            // coordinate system of the projection. otherwise we
            // will use lat/lng
            boolean isModelProjection = lastProjection instanceof ModelProjection;

            if (isModelProjection) {

                double minNorthing = Double.MAX_VALUE, maxNorthing = -Double.MAX_VALUE, minEasting = Double.MAX_VALUE, maxEasting = -Double.MAX_VALUE;

                // for each file
                for (int band = 0; band < numFiles; band++) {

                    ImageObject headerObject = tileHeaders[band];
                    if (headerObject != null) {
                        ModelProjection proj = (ModelProjection) headerObject.getProperty(ImageObject.GEOINFO);

                        // find the min northing of the tile
                        double minN = proj.getMinNorthing();

                        // find the max northing of the tile
                        double maxN = proj.getMaxNorthing();

                        // find the min easting of the tile
                        double minE = proj.getMinEasting();

                        // find the max easting of the tile
                        double maxE = proj.getMaxEasting();

                        if (minN < minNorthing) {
                            minNorthing = minN;
                            bottomTileIndex = band;
                        }
                        if (maxN > maxNorthing) {
                            maxNorthing = maxN;
                            topTileIndex = band;
                        }
                        if (minE < minEasting) {
                            minEasting = minE;
                            leftTileIndex = band;
                        }
                        if (maxE > maxEasting) {
                            maxEasting = maxE;
                            rightTileIndex = band;
                        }

                        double curColResolution = proj.GetColumnResolution();
                        double curRowResolution = Math.abs(proj.GetRowResolution());

                        if (curColResolution > maxColResolution) {
                            maxColResolution = curColResolution;
                            maxColTileIndex = band;
                        }
                        if (curRowResolution > maxRowResolution) {
                            maxRowResolution = curRowResolution;
                            maxRowTileIndex = band;
                        }

                        int curSampPerPixelSize = headerObject.getNumBands();

                        if (curSampPerPixelSize > maxSampPerPixelSize) {
                            maxSampPerPixelSize = curSampPerPixelSize;
                        }

                    } // end of if headerObject != null

                } // end for loop
            } // if is model projection

            // otherwise it is a geographic
            else {

                // for each file
                for (int band = 0; band < numFiles; band++) {

                    ImageObject headerObject = tileHeaders[band];

                    if (headerObject != null) {
                        Projection proj = (Projection) headerObject.getProperty(ImageObject.GEOINFO);

                        double curMaxNorthLat = proj.GetMaxNorthLat();
                        double curMaxSouthLat = proj.GetMaxSouthLat();
                        double curMaxWestLng = proj.GetMaxWestLng();
                        double curMaxEastLng = proj.GetMaxEastLng();

                        if (curMaxNorthLat > maxNorthLat) {
                            maxNorthLat = curMaxNorthLat;
                            topTileIndex = band;
                        }

                        if (curMaxSouthLat < maxSouthLat) {
                            maxSouthLat = curMaxSouthLat;
                            bottomTileIndex = band;
                        }

                        if (curMaxWestLng < maxWestLng) {
                            maxWestLng = curMaxWestLng;
                            leftTileIndex = band;
                        }

                        if (curMaxEastLng > maxEastLng) {
                            maxEastLng = curMaxEastLng;
                            rightTileIndex = band;
                        }

                        double curColResolution = proj.GetColumnResolution();
                        double curRowResolution = Math.abs(proj.GetRowResolution());

                        if (curColResolution > maxColResolution) {
                            maxColResolution = curColResolution;
                            maxColTileIndex = band;
                        }
                        if (curRowResolution > maxRowResolution) {
                            maxRowResolution = curRowResolution;
                            maxRowTileIndex = band;
                        }
                        int curSampPerPixelSize = headerObject.getNumBands();

                        if (curSampPerPixelSize > maxSampPerPixelSize) {
                            maxSampPerPixelSize = curSampPerPixelSize;
                        }
                    } // end of if headerObject != null
                } // end for loop
            }

            // get the top, left, bottom, and right tiles

            ImageObject topTile = tileHeaders[topTileIndex];
            ImageObject bottomTile = tileHeaders[bottomTileIndex];
            ImageObject leftTile = tileHeaders[leftTileIndex];
            ImageObject rightTile = tileHeaders[rightTileIndex];

            logger.debug("Top Tile Index : " + topTileIndex);
            logger.debug("Bottom Tile Index : " + bottomTileIndex);
            logger.debug("Left Tile Index : " + leftTileIndex);
            logger.debug("Right Tile Index : " + rightTileIndex);

            // now we need to sample the top, right, bottom, and left
            // down to the same sampling as the tile with the maximum column
            // resolution
            if (maxColTileIndex != topTileIndex) {
                Projection proj = (Projection) topTile.getProperty(ImageObject.GEOINFO);
                proj.resample(maxColResolution, maxRowResolution);
            }

            if (maxColTileIndex != bottomTileIndex) {
                Projection proj = (Projection) bottomTile.getProperty(ImageObject.GEOINFO);
                proj.resample(maxColResolution, maxRowResolution);
            }

            if (maxColTileIndex != leftTileIndex) {
                Projection proj = (Projection) leftTile.getProperty(ImageObject.GEOINFO);
                proj.resample(maxColResolution, maxRowResolution);
            }

            if (maxColTileIndex != rightTileIndex) {
                Projection proj = (Projection) rightTile.getProperty(ImageObject.GEOINFO);
                proj.resample(maxColResolution, maxRowResolution);
            }

            if (isModelProjection) {
                // now, find the dimensions of the mosaicked image
                // (maxNorth, minEast) will be (0,0)
                // (minNorth, maxEast) will be (numrows, numcols)

                ModelProjection proj;
                proj = (ModelProjection) bottomTile.getProperty(ImageObject.GEOINFO);
                // find the min northing of the bottom tile
                double minN = proj.getMinNorthing();

                proj = (ModelProjection) topTile.getProperty(ImageObject.GEOINFO);
                // find the max northing of the top tile
                double maxN = proj.getMaxNorthing();

                proj = (ModelProjection) leftTile.getProperty(ImageObject.GEOINFO);
                // find the min easting of the left tile
                double minE = proj.getMinEasting();

                proj = (ModelProjection) rightTile.getProperty(ImageObject.GEOINFO);
                // find the max easting of the right tile
                double maxE = proj.getMaxEasting();

                // now, compute the numrows and numcols given the two corners
                // and the row and column resolution

                int numRows = (int) Math.round((maxN - minN) / maxRowResolution) + 1;
                int numCols = (int) Math.round((maxE - minE) / maxColResolution) + 1;

                if (floatSampleAvailable) {
                    sampType = ImageObject.TYPE_FLOAT;
                } else {
                    sampType = topTile.getType();
                }

                ImageObject mosaickedImage = ImageObject.createImage(numRows, numCols, maxSampPerPixelSize, sampType);

                // copy the geo info from the top tile to the mosaickedImage
                // mosaickedImage.CopyGeoInfo(topTile);
                ModelProjection topProj = (ModelProjection) topTile.getProperty(ImageObject.GEOINFO);
                ModelProjection mosaicProj = (ModelProjection) topProj.getCopy();
                ModelProjection leftProj = (ModelProjection) leftTile.getProperty(ImageObject.GEOINFO);
                ModelProjection bottomProj = (ModelProjection) bottomTile.getProperty(ImageObject.GEOINFO);
                mosaickedImage.setProperty(ImageObject.GEOINFO, mosaicProj);
                mosaicProj.setNumRows(mosaickedImage.getNumRows());
                mosaicProj.setNumCols(mosaickedImage.getNumCols());
                mosaicProj.setInsertionY(topProj.getInsertionY());
                mosaicProj.setInsertionX(leftProj.getInsertionX());

                // now, load each image and insert it into the geoObject
                // set the sub area so that the tile will fit into the geoObject
                // but.. we shouldn't have to truncate! we should have
                // correctcly
                // figured out the size of the geoObject

                // for each tile
                for (int i = 0; i < FileName.length; i++) {
                    ImageObject tile;
                    try {
                        tile = ImageLoader.readImage(FileName[i]);
                    } catch (IOException ex) {
                        continue;
                    }
                    ModelProjection tileProj = (ModelProjection) tile.getProperty(ImageObject.GEOINFO);

                    // right here we should check resolution and sample as
                    // necessary
                    double geoColRes = tileProj.GetColumnResolution();
                    double geoRowRes = Math.abs(tileProj.GetRowResolution());
                    /*
                     * if (isInDegrees(tile)) { double[] colRowRes =
                     * convertDegreeScale2MeterScale(tile); geoColRes =
                     * colRowRes[0]; geoRowRes = colRowRes[1]; }
                     */
                    if ((Math.abs(geoRowRes - maxRowResolution) > EPSILON) || (Math.abs(geoColRes - maxColResolution) > EPSILON)) {
                        float sampRatioCol = (float) (maxColResolution / geoColRes);
                        float sampRatioRow = (float) (maxRowResolution / geoRowRes);
                        if (sampRatioCol < 1) {
                            sampRatioCol = 1;
                        }
                        if (sampRatioRow < 1) {
                            sampRatioRow = 1;
                        }
                        tile = tile.scale(1.0 / sampRatioCol, 1.0 / sampRatioRow);
                    }

                    // check sample type and convert if necessary
                    // if (!(tile.sampType.equals(mosaickedImage.sampType))){
                    if (tile.getType() != mosaickedImage.getType()) {
                        int type = mosaickedImage.getType();

                        tile = tile.convert(type, false);
                    }

                    int tileNumRows = tile.getNumRows();
                    int tileNumCols = tile.getNumCols();

                    double[] tileUpperLeft = new double[] { 0, 0 };

                    // convert the upper left corner of the tile to UTM meters
                    double[] tileULMeters = tileProj.colRowToNorthingEasting(tileUpperLeft);

                    // given the E,N of the upper left corner, find the column
                    // row in
                    // the mosaic
                    double[] mosaicColRow = mosaicProj.northingEastingToColRow(tileULMeters);

                    // now we know the column and row to insert the tile into
                    // the mosaic

                    // now, determine if inserting this tile into the mosaic
                    // at the insertion point will be too big for the geoObject
                    // (this shouldn't happen!)
                    // if it is too big, use a SubArea to only insert the
                    // portion
                    // of the tile that is inside the geoObject
                    boolean useSubArea = false;
                    int columnInsertion = (int) mosaicColRow[0];
                    int rowInsertion = (int) mosaicColRow[1];

                    int numColsToInsert = tileNumCols;

                    logger.debug("Column Insertion : " + columnInsertion);
                    logger.debug("Tile Num Cols : " + tileNumCols);
                    logger.debug("Mosaic Image Num Cols : " + numCols);

                    if (columnInsertion + tileNumCols > numCols) {
                        numColsToInsert = (numCols - columnInsertion);
                        useSubArea = true;
                    }
                    int numRowsToInsert = tileNumRows;
                    if (rowInsertion + tileNumRows > numRows) {
                        numColsToInsert = (numCols - columnInsertion);
                        useSubArea = true;
                    }

                    // if we need to use a subarea, insert the tile this way
                    if (useSubArea) {
                        // crop the image if necessary
                        SubArea sa = new SubArea(0, 0, numRowsToInsert, numColsToInsert);
                        tile = tile.crop(sa);
                    }
                    // otherwise insert the whole thing
                    else {
                        // mosaickedImage.insert(tile, rowInsertion,
                        // columnInsertion);
                        mosaickedImage.mosaicInsert(tile, rowInsertion, columnInsertion);
                    }
                } // for

                // mosaickedImage.setProperty(ImageObject.NODATAVALUE,new
                // Float(9999));
                return mosaickedImage;
            } // if modeltype == GeoImageObject.UTM_NORTHERN_HEM

            else {
                // else {
                // if it is DEM, then we only use lat/lng.

                // we need to re-find the maxColRes, maxRowRes. The dem headers
                // were converted to meters, but this is all wrong.
                // maxColResolution = topTile.GetColumnResolution();
                // maxRowResolution = topTile.GetRowResolution();

                // now, find the dimensions of the mosaicked image
                // (maxNorth, minEast) will be (0,0)
                // (minNorth, maxEast) will be (numrows, numcols)

                // now, compute the numrows and numcols given the two corners
                // and the row and column resolution

                // now we need to sample the top, right, bottom, and left
                // down to the same sampling as the tile with the maximum column
                // resolution
                /*
                 * if (maxColTileIndex != topTileIndex) { Projection proj =
                 * (Projection)topTile.getProperty(ImageObject.GEOINFO);
                 * proj.resample(maxColResolution, maxRowResolution); } if
                 * (maxColTileIndex != bottomTileIndex) { Projection proj =
                 * (Projection)topTile.getProperty(ImageObject.GEOINFO);
                 * proj.resample(maxColResolution, maxRowResolution); } if
                 * (maxColTileIndex != leftTileIndex) { Projection proj =
                 * (Projection)topTile.getProperty(ImageObject.GEOINFO);
                 * proj.resample(maxColResolution, maxRowResolution); } if
                 * (maxColTileIndex != rightTileIndex) { Projection proj =
                 * (Projection)topTile.getProperty(ImageObject.GEOINFO);
                 * proj.resample(maxColResolution, maxRowResolution); }
                 */

                int numRows = (int) Math.round((maxNorthLat - maxSouthLat) / maxRowResolution) + 1;
                int numCols = (int) Math.round((maxEastLng - maxWestLng) / maxColResolution) + 1;

                if (floatSampleAvailable) {
                    sampType = ImageObject.TYPE_FLOAT;
                } else {
                    sampType = topTile.getType();
                }

                ImageObject mosaickedImage = ImageObject.createImage(numRows, numCols, maxSampPerPixelSize, sampType);
                // ImageObject mosaickedImage = new ImageObject(numRows,
                // numCols, maxSampPerPixelSize, sampType);

                // copy the geo info from the top tile to the mosaickedImage
                // mosaickedImage.CopyGeoInfo(topTile);

                Projection topProj = (Projection) topTile.getProperty(ImageObject.GEOINFO);
                Projection mosaicProj = topProj.getCopy();
                Projection leftProj = (Projection) leftTile.getProperty(ImageObject.GEOINFO);
                Projection bottomProj = (Projection) bottomTile.getProperty(ImageObject.GEOINFO);
                mosaickedImage.setProperty(ImageObject.GEOINFO, mosaicProj);

                boolean rasterJIsNumRows = (topProj.getNumRows() == topProj.GetRasterSpaceJ() || topProj.getNumRows() == (topProj.GetRasterSpaceJ() + 1));

                mosaicProj.setNumRows(mosaickedImage.getNumRows());
                mosaicProj.setNumCols(mosaickedImage.getNumCols());
                mosaicProj.setInsertionY(topProj.getInsertionY());
                mosaicProj.setInsertionX(leftProj.getInsertionX());
                mosaicProj.setRasterSpaceI(0);
                mosaicProj.setRasterSpaceJ(0);

                //                if (rasterJIsNumRows) {
                //                    mosaicProj.setRasterSpaceJ(mosaickedImage.getNumRows());
                //                    mosaicProj.setInsertionY(bottomProj.getMaxSouthLatitude());
                //                }

                // now, load each image and insert it into the geoObject
                // set the sub area so that the tile will fit into the geoObject
                // but.. we shouldn't have to truncate! we should have
                // correctcly
                // figured out the size of the geoObject

                // for each band
                for (int i = 0; i < FileName.length; i++) {
                    ImageObject tile;
                    try {
                        tile = ImageLoader.readImage(FileName[i]);
                    } catch (IOException ex) {
                        continue;
                    }
                    Projection tileProj = (Projection) tile.getProperty(ImageObject.GEOINFO);

                    // right here we should check resolution and sample as
                    // necessary
                    double geoColRes = tileProj.GetColumnResolution();
                    double geoRowRes = Math.abs(tileProj.GetRowResolution());

                    if ((Math.abs(geoRowRes - maxRowResolution) > EPSILON) || (Math.abs(geoColRes - maxColResolution) > EPSILON)) {

                        float sampRatioCol = (float) (maxColResolution / geoColRes);
                        float sampRatioRow = (float) (maxRowResolution / geoRowRes);
                        if (sampRatioCol < 1) {
                            sampRatioCol = 1;
                        }
                        if (sampRatioRow < 1) {
                            sampRatioRow = 1;

                            // Sampling samp = new Sampling(sampRatioRow,
                            // sampRatioCol);
                            // tile = samp.SubSamplingFloatGeo(tile);
                        }
                        tile = tile.scale(1.0 / sampRatioCol, 1.0 / sampRatioRow);
                    }

                    // check sample type and convert if necessary
                    if (tile.getType() != mosaickedImage.getType()) {
                        int type = mosaickedImage.getType();
                        tile = tile.convert(type, false);
                    }

                    int tileNumRows = tile.getNumRows();
                    int tileNumCols = tile.getNumCols();

                    double[] tileUpperLeft = new double[] { 0, 0 };

                    // convert the upper left corner of the tile to UTM meters
                    // GeoConvert tileConvert = new GeoConvert(tile);
                    // Point2DDouble tileULMeters =
                    // tileConvert.ColumnRow2UTMNorthingEasting(
                    double[] tileULLatLng = tileProj.colRowToLatLon(tileUpperLeft);

                    double[] mosaicUL = mosaicProj.colRowToLatLon(tileUpperLeft);

                    // given the E,N of the upper left corner, find the column
                    // row in
                    // the mosaic
                    double[] mosaicColRow = mosaicProj.latLonToColRow(tileULLatLng);

                    // now we know the column and row to insert the tile into
                    // the mosaic

                    // now, determine if inserting this tile into the mosaic
                    // at the insertion point will be too big for the geoObject
                    // (this shouldn't happen!)
                    // if it is too big, use a SubArea to only insert the
                    // portion
                    // of the tile that is inside the geoObject
                    boolean useSubArea = false;
                    int columnInsertion = (int) mosaicColRow[0];
                    int rowInsertion = (int) mosaicColRow[1];

                    int numColsToInsert = tileNumCols;
                    if (columnInsertion + tileNumCols > numCols) {
                        numColsToInsert = (numCols - columnInsertion);
                        useSubArea = true;
                    }
                    int numRowsToInsert = tileNumRows;
                    if (rowInsertion + tileNumRows > numRows) {
                        numRowsToInsert = (numRows - rowInsertion);
                        useSubArea = true;
                    }

                    // if we need to use a subarea, crop the tile first
                    if (useSubArea) {
                        SubArea sa = new SubArea(0, 0, numRowsToInsert, numColsToInsert);
                        tile = tile.crop(sa);
                    }
                    // otherwise insert the whole thing
                    else {
                        mosaickedImage.insert(tile, rowInsertion, columnInsertion);
                    }
                } // for

                return mosaickedImage;
            } // if not a model projection
        } // if allTilesHaveSameProjection

        // END all tiles have the same projection
        // //////////////////////////////////////////////////////

        // all tiles do not have the same projection!
        // for each file
        for (int band = 0; band < numFiles; band++) {
            double curColResolution = -1;
            double curRowResolution = -1;
            int curSampPerPixelSize = -1;

            ImageObject headerObject = tileHeaders[band];

            Projection proj = (Projection) headerObject.getProperty(ImageObject.GEOINFO);

            if (headerObject != null) {

                double curMaxNorthLat = proj.GetMaxNorthLat();
                double curMaxSouthLat = proj.GetMaxSouthLat();
                double curMaxWestLng = proj.GetMaxWestLng();
                double curMaxEastLng = proj.GetMaxEastLng();

                if (curMaxNorthLat > maxNorthLat) {
                    maxNorthLat = curMaxNorthLat;
                    topTileIndex = band;
                    // System.err.println("topGeoOb is band = "+band);
                }

                if (curMaxSouthLat < maxSouthLat) {
                    maxSouthLat = curMaxSouthLat;
                    bottomTileIndex = band;
                    // System.err.println("bottomGeoOb is band = "+band);
                }

                if (curMaxWestLng < maxWestLng) {
                    maxWestLng = curMaxWestLng;
                    leftTileIndex = band;
                    // System.err.println("leftGeoOb is band = "+band);
                }

                if (curMaxEastLng > maxEastLng) {
                    maxEastLng = curMaxEastLng;
                    rightTileIndex = band;
                    // System.err.println("rightGeoOb is band = "+band);
                }

                /*
                 * curColResolution = proj.GetColumnResolution();
                 * curRowResolution = Math.abs(proj.GetRowResolution()); if
                 * (isInDegrees(headerObject)) { double[] colRowres =
                 * convertDegreeScale2MeterScale(headerObject); curColResolution =
                 * colRowres[0]; curRowResolution = colRowres[1]; }
                 */
                curColResolution = findColResolutionInDegrees(proj);
                curRowResolution = findRowResolutionInDegrees(proj);

                if (curColResolution > maxColResolution) {
                    maxColResolution = curColResolution;
                    maxColTileIndex = band;
                    // System.err.println("maxColGeoObjIndex = "+band);
                }
                if (curRowResolution > maxRowResolution) {
                    maxRowResolution = curRowResolution;
                    maxRowTileIndex = band;
                    // System.err.println("maxRowGeoObjIndex = "+band);
                }
                curSampPerPixelSize = headerObject.getNumBands();

                if (curSampPerPixelSize > maxSampPerPixelSize) {
                    maxSampPerPixelSize = curSampPerPixelSize;
                }

                /*
                 * String curSampType = headerObject.sampType; if
                 * (curSampType.equals("FLOAT")) _floatSampleAvailable = true;
                 * if (curSampType.equals("INT")) _intSampleAvailable = true; if
                 * (curSampType.equals("LONG")) _longSampleAvailable = true; if
                 * (curSampType.equals("SHORT")) _shortSampleAvailable = true;
                 * if (curSampType.equals("BYTE")) _byteSampleAvailable = true;
                 * if (curSampType.equals("DOUBLE")) _doubleSampleAvailable =
                 * true;
                 */

            } // end of if headerObject != null

        } // end for loop

        ImageObject topGeoHeader = tileHeaders[topTileIndex];
        ImageObject leftGeoHeader = tileHeaders[leftTileIndex];
        ImageObject rightGeoHeader = tileHeaders[rightTileIndex];
        ImageObject bottomGeoHeader = tileHeaders[bottomTileIndex];

        // now we have the topmost, bottomost, rightmost, and leftmost images

        // SUBSAMPLE ISSUES TO BE RESOLVED HERE

        // if the tile with the maximum column is not the top tile
        if (maxColTileIndex != topTileIndex) {

            double topGeoColRes = -1;
            double topGeoRowRes = -1;
            // get the column and row resolution
            // if it is in degrees, convert to meters
            /*
             * if (isInDegrees(topGeoObject)) { double[] colRowRes =
             * convertDegreeScale2MeterScale(topGeoObject); topGeoColRes =
             * colRowRes[0]; topGeoRowRes = colRowRes[1]; } else { Projection
             * proj = (Projection) topGeoObject.getProperty(ImageObject.
             * GEOINFO); topGeoColRes = proj.GetColumnResolution(); topGeoRowRes =
             * Math.abs(proj.GetRowResolution()); }
             */

            Projection proj = (Projection) topGeoHeader.getProperty(ImageObject.GEOINFO);
            topGeoColRes = findColResolutionInDegrees(proj);
            topGeoRowRes = findRowResolutionInDegrees(proj);

            // if they differ by more than EPSILON
            if (Math.abs(topGeoColRes - maxColResolution) > EPSILON) {
                float topSampRatioCol = (float) (maxColResolution / topGeoColRes);
                float topSampRatioRow = (float) (maxRowResolution / topGeoRowRes);
                // Sampling mySamp1 = new Sampling(topSampRatioRow,
                // topSampRatioCol);
                // sample the top geo object so that it has the same sampling as
                // the
                // tile with max columns
                // topGeoObject = mySamp1.SubSamplingFloatGeo(topGeoObject);
                topGeoHeader = topGeoHeader.scale(1.0 / topSampRatioCol, 1.0 / topSampRatioRow);
            }
        }

        // if the tile with the maximum column is not the bottom tile
        if (maxColTileIndex != bottomTileIndex) {

            double bottomGeoColRes = -1;
            double bottomGeoRowRes = -1;
            // get the column and row resolution
            // if it is in degrees, convert to meters
            /*
             * if (isInDegrees(bottomGeoObject)) { double[] colRowRes =
             * convertDegreeScale2MeterScale(bottomGeoObject); bottomGeoColRes =
             * colRowRes[0]; bottomGeoRowRes = colRowRes[1]; } else { Projection
             * proj = (Projection) bottomGeoObject.getProperty(ImageObject.
             * GEOINFO); bottomGeoColRes = proj.GetColumnResolution();
             * bottomGeoRowRes = Math.abs(proj.GetRowResolution()); }
             */

            Projection proj = (Projection) bottomGeoHeader.getProperty(ImageObject.GEOINFO);
            bottomGeoColRes = findColResolutionInDegrees(proj);
            bottomGeoRowRes = findRowResolutionInDegrees(proj);

            // if they differ by more than EPSILON
            if (Math.abs(bottomGeoColRes - maxColResolution) > EPSILON) {
                float bottomSampRatioCol = (float) (maxColResolution / bottomGeoColRes);
                float bottomSampRatioRow = (float) (maxRowResolution / bottomGeoRowRes);
                // Sampling mySamp2 = new Sampling(bottomSampRatioRow,
                // bottomSampRatioCol);
                // sample the bottom geo object so that it has the same sampling
                // as the
                // tile with max columns
                // bottomGeoObject =
                // mySamp2.SubSamplingFloatGeo(bottomGeoObject);
                bottomGeoHeader = bottomGeoHeader.scale(1.0 / bottomSampRatioCol, 1.0 / bottomSampRatioRow);
            }
        }

        // if the tile with the maximum column is not the left tile
        if (maxColTileIndex != leftTileIndex) {
            Projection leftProj = (Projection) leftGeoHeader.getProperty(ImageObject.GEOINFO);

            double leftGeoColRes = -1;
            double leftGeoRowRes = -1;
            // get the column and row resolution
            // if it is in degrees, convert to meters
            /*
             * if (isInDegrees(leftGeoObject)) { double[] colRowRes =
             * convertDegreeScale2MeterScale(leftGeoObject); leftGeoColRes =
             * colRowRes[0]; leftGeoRowRes = colRowRes[1]; } else {
             * leftGeoColRes = leftProj.GetColumnResolution(); leftGeoRowRes =
             * Math.abs(leftProj.GetRowResolution()); }
             */

            leftGeoColRes = findColResolutionInDegrees(leftProj);
            leftGeoRowRes = findRowResolutionInDegrees(leftProj);

            // if they differ by more than EPSILON
            if (Math.abs(leftGeoColRes - maxColResolution) > EPSILON) {
                float leftSampRatioCol = (float) (maxColResolution / leftGeoColRes);
                float leftSampRatioRow = (float) (maxRowResolution / leftGeoRowRes);
                // Sampling mySamp3 = new Sampling(leftSampRatioRow,
                // leftSampRatioCol);
                // sample the left geo object so that it has the same sampling
                // as the
                // tile with max columns
                // leftGeoObject = mySamp3.SubSamplingFloatGeo(leftGeoObject);
                leftGeoHeader = leftGeoHeader.scale(1.0 / leftSampRatioCol, 1.0 / leftSampRatioRow);
            }
        }

        // if the tile with the maximum column is not the right tile
        if (maxColTileIndex != rightTileIndex) {
            Projection rightProj = (Projection) rightGeoHeader.getProperty(ImageObject.GEOINFO);

            double rightGeoColRes = -1;
            double rightGeoRowRes = -1;
            // get the column and row resolution
            // if it is in degrees, convert to meters
            /*
             * if (isInDegrees(rightGeoObject)) { double[] colRowRes =
             * convertDegreeScale2MeterScale(rightGeoObject); rightGeoColRes =
             * colRowRes[0]; rightGeoRowRes = colRowRes[1]; } else {
             * rightGeoColRes = rightProj.GetColumnResolution(); rightGeoRowRes =
             * Math.abs(rightProj.GetRowResolution()); }
             */

            rightGeoColRes = findColResolutionInDegrees(rightProj);
            rightGeoRowRes = findRowResolutionInDegrees(rightProj);

            // if they differ by more than EPSILON
            if (Math.abs(rightGeoColRes - maxColResolution) > EPSILON) {
                float rightSampRatioCol = (float) (maxColResolution / rightGeoColRes);
                float rightSampRatioRow = (float) (maxRowResolution / rightGeoColRes);
                // Sampling mySamp4 = new Sampling(rightSampRatioRow,
                // rightSampRatioCol);
                // sample the right geo object so that it has the same sampling
                // as the
                // tile with max columns
                // rightGeoObject = mySamp4.SubSamplingFloatGeo(rightGeoObject);
                rightGeoHeader = rightGeoHeader.scale(1.0 / rightSampRatioCol, 1.0 / rightSampRatioRow);
            }
        }

        // GeoConvert geoConN = new GeoConvert(topGeoObject);
        Projection geoConN = (Projection) topGeoHeader.getProperty(ImageObject.GEOINFO);

        // Estimate leftmost location
        double[] ptLeft = new double[] { 0, 0 };

        ptLeft[1] = leftGeoHeader.getNumRows() - 1;
        // GeoConvert geoConL = new GeoConvert(leftGeoObject);
        Projection geoConL = (Projection) leftGeoHeader.getProperty(ImageObject.GEOINFO);
        double[] latLngLeft = geoConL.ColumnRow2LatLng(ptLeft);
        double[] colRowLeft = geoConN.LatLng2ColumnRow(latLngLeft);
        colRowLeft[0] = Math.round(colRowLeft[0]);
        colRowLeft[1] = Math.round(colRowLeft[1]);
        int colNum2AddLeft = 0;
        if (colRowLeft[0] < 0) {
            colNum2AddLeft = (int) Math.abs(colRowLeft[0]);
        }
        double[] ptRight = new double[2];
        ptRight[0] = rightGeoHeader.getNumCols() - 1;
        ptRight[1] = rightGeoHeader.getNumRows() - 1;
        // GeoConvert geoConR = new GeoConvert(rightGeoObject);
        Projection geoConR = (Projection) rightGeoHeader.getProperty(ImageObject.GEOINFO);
        double[] latLngRight = geoConR.ColumnRow2LatLng(ptRight);
        double[] colRowRight = geoConN.LatLng2ColumnRow(latLngRight);
        colRowRight[0] = Math.round(colRowRight[0]);
        colRowRight[1] = Math.round(colRowRight[1]);
        int colNum2AddRight = 0;
        if (colRowRight[0] > topGeoHeader.getNumCols()) {
            colNum2AddRight = (int) Math.abs(colRowRight[0] - topGeoHeader.getNumCols());

            // Estimate bottommost location
        }
        double[] ptBottom = new double[2];
        ptBottom[0] = bottomGeoHeader.getNumCols() - 1;
        ptBottom[1] = bottomGeoHeader.getNumRows() - 1;
        // GeoConvert geoConB = new GeoConvert(bottomGeoObject);
        Projection geoConB = (Projection) bottomGeoHeader.getProperty(ImageObject.GEOINFO);
        double[] latLngBottom = geoConB.ColumnRow2LatLng(ptBottom);
        double[] colRowBottom = geoConN.LatLng2ColumnRow(latLngBottom);
        colRowBottom[0] = Math.round(colRowBottom[0]);
        colRowBottom[1] = Math.round(colRowBottom[1]);
        int rowNum2AddBottom = 0;
        if (colRowBottom[1] > topGeoHeader.getNumRows()) {
            rowNum2AddBottom = (int) Math.abs(colRowBottom[1] - topGeoHeader.getNumRows());

        }
        pixelDistanceHeight = rowNum2AddBottom + topGeoHeader.getNumRows();
        pixelDistanceWidth = colNum2AddRight + topGeoHeader.getNumCols() + colNum2AddLeft;

        // GeoFrame should be of this size!
        // System.err.println("pixelDistanceHeight is "+pixelDistanceHeight);
        // System.err.println("pixelDistanceWidth is "+pixelDistanceWidth);

        // SAMPLE TYPE ISSUES RESOLVED HERE
        // (add preferences as necessary)
        /*
         * if (_floatSampleAvailable){ sampType = "FLOAT"; } else{ sampType =
         * topGeoObject.sampType; }
         */
        if (floatSampleAvailable) {
            sampType = ImageObject.TYPE_FLOAT;
        } else {
            sampType = topGeoHeader.getType();

            // SAMPLES PER PIXEL ISSUES RESOLVED HERE
            // using maxSampPerPixel calculated above
            // maxSampPerPixelSize = topGeoObject.sampPerPixel;

            // GeoImageObject geoObject = new
            // GeoImageObject(pixelDistanceHeight+4,
            // pixelDistanceWidth+4, maxSampPerPixelSize, sampType);
            // ImageObject geoObject = new ImageObject(pixelDistanceHeight,
            // pixelDistanceWidth, maxSampPerPixelSize, sampType);
        }
        ImageObject mosaicImage = ImageObject.createImage(pixelDistanceHeight, pixelDistanceWidth, maxSampPerPixelSize, sampType);
        Projection mosaicProj;

        // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        // what is this
        // ImageObject theImg = topGeoHeader;

        // ALTER SAMPLES PER PIXEL IF NECESSARY
        /*
         * if (topGeoObject.getNumBands() != geoObject.getNumBands()) { int[]
         * bands = { 1, 1, 1}; //theImg =
         * topGeoObject.CopySelectedBands(topGeoObject, bands, 3); //theImg =
         * topGeoObject.extractBand(bands); ImageObject[] tmp = {topGeoObject,
         * topGeoObject, topGeoObject}; theImg = topGeoObject.add(tmp); //
         * topGeoObject.SetImageObject(theImg); } /* if
         * (!(topGeoObject.sampType.equals(geoObject.sampType))){ if
         * (geoObject.sampType.equals("FLOAT")){ theImg =
         * ImageObjectConvert.Convert2FLOATScale(
         * topGeoObject.GetImageObject()); } if
         * (geoObject.sampType.equals("INT")){ theImg =
         * ImageObjectConvert.Convert2INTScale( topGeoObject.GetImageObject()); }
         * if (geoObject.sampType.equals("SHORT")){ theImg =
         * ImageObjectConvert.Convert2SHORTScale(
         * topGeoObject.GetImageObject()); } if
         * (geoObject.sampType.equals("LONG")){ theImg =
         * ImageObjectConvert.Convert2LONGScale( topGeoObject.GetImageObject()); }
         * if (geoObject.sampType.equals("BYTE")){ theImg =
         * ImageObjectConvert.Convert2BYTEScale( topGeoObject.GetImageObject()); }
         * if (geoObject.sampType.equals("DOUBLE")){ theImg =
         * ImageObjectConvert.Convert2DOUBLEScale(
         * topGeoObject.GetImageObject()); } geoObject.InsertArea(theImg, 0,
         * colNum2AddLeft); } else { geoObject.InsertArea(topGeoObject, 0,
         * colNum2AddLeft); }
         */
        /*
         * if (theImg.getType() != geoObject.getType()) { theImg =
         * theImg.convert(geoObject.getType(), false); }
         * geoObject.insert(theImg, 0, colNum2AddLeft);
         */

        mosaicProj = (Projection) topGeoHeader.getProperty(ImageObject.GEOINFO);
        mosaicImage.setProperty(ImageObject.GEOINFO, mosaicProj);

        // we need to set up geoProj to be a Projection in lat/lng
        Projection tproj = new Projection(mosaicProj.getRasterSpaceI(), mosaicProj.getRasterSpaceJ(), mosaicProj.GetMaxWestLng(), mosaicProj.getMaxNorthLatitude(), mosaicProj.getScaleX(), mosaicProj
                .getScaleY(), mosaicImage.getNumRows(), mosaicImage.getNumCols());
        mosaicProj = tproj;
        mosaicImage.setProperty(ImageObject.GEOINFO, mosaicProj);

        // set up georeferencing info in geoObject from topGeoObject
        // geoObject.CopyGeoInfo(topGeoObject);
        /*
         * if (isInDegrees(topGeoObject)) { double[] colRowres =
         * convertDegreeScale2MeterScale(topGeoObject); double curColResolution =
         * colRowres[0]; double curRowResolution = colRowres[1]; double colratio =
         * maxColResolution / curColResolution; double rowratio =
         * maxRowResolution / curRowResolution;
         * geoProj.SetColumnResolution(geoProj.GetColumnResolution() *
         * colratio); geoProj.SetRowResolution(geoProj.GetRowResolution() *
         * rowratio); } else { geoProj.SetColumnResolution(maxColResolution);
         * geoProj.SetRowResolution(maxRowResolution); }
         */

        mosaicProj.SetColumnResolution(maxColResolution);
        mosaicProj.SetRowResolution(maxRowResolution);

        // tie point of GeoFrame is (0,RasterSpaceJ from topGeoObject)
        // OR it is (0,0) from topGeoObject
        if (mosaicProj.GetRowResolution() < 0) { // tie point upper left
            // corner
            mosaicProj.SetRasterSpaceI(0);
            mosaicProj.SetRasterSpaceJ(0);
            mosaicProj.SetEastingInsertionValue(mosaicProj.GetEastingInsertionValue() - mosaicProj.GetColumnResolution() * colNum2AddLeft);
            // northing value same!
            // dc 5.26
            // geoProj.SetModelSpaceX(geoObject.GetEastingInsertionValue());
            // geoObject.SetModelSpaceY(topGeoObject.GetNorthingInsertionValue());
            if (mosaicProj.GetRowResolution() > 0) {
                mosaicProj.SetRowResolution(-mosaicProj.GetRowResolution());
            }
        } else { // tie point lower left corner
            mosaicProj.SetRasterSpaceI(0);
            mosaicProj.SetRasterSpaceJ(mosaicImage.getNumRows() - 1);
            mosaicProj.SetEastingInsertionValue(mosaicProj.GetEastingInsertionValue() - mosaicProj.GetColumnResolution() * colNum2AddLeft);
            mosaicProj.SetNorthingInsertionValue(mosaicProj.GetNorthingInsertionValue() - mosaicProj.GetRowResolution() * (rowNum2AddBottom));
            // geoProj.SetModelSpaceX(geoObject.GetEastingInsertionValue());
            // geoObject.SetModelSpaceY(topGeoObject.GetNorthingInsertionValue());
        }

        // GeoConvert geoConObj = new GeoConvert(geoObject);

        // Set maxWestLng in case of no projection center
        /*
         * Point2DDouble p2d = new Point2DDouble(1); p2d.ptsDouble[0] =
         * topGeoObject.GetNorthingInsertionValue(); p2d.ptsDouble[1] =
         * topGeoObject.GetEastingInsertionValue(); p2d =
         * geoConN.UTMNorthingEasting2LatLng(p2d);
         * geoObject.SetMaxWestLng(p2d.ptsDouble[1]); if
         * (geoObject.GetGeoProjCenterLng() == -1) //if no projection //center
         * specified
         * geoObject.SetUTMZone(geoConN.determineUTMZone(topGeoObject.GetMaxWestLng()));
         * geoObject.SetModelSpaceX(topGeoObject.GetEastingInsertionValue());
         * geoObject.SetModelSpaceY(topGeoObject.GetNorthingInsertionValue());
         */

        /*
         * for (int band = 0; band < numFiles; band++) { if (band !=
         * topTileIndex) { geoImageVec.add(new Integer(band)); } } //end for
         */

        // Iterator i = geoImageVec.iterator();
        // ImageObject imgObj, lilObj;
        // ImageObject lilObj;
        // Point2DDouble coordPt1, coordPt2;
        long t1 = System.currentTimeMillis();
        // while (i.hasNext()) {
        for (int i = 0; i < numFiles; i++) {
            // geoImgObj = new GeoImageObject();
            // geoImgObj = (GeoImageObject)
            // LoadBasedonSource(FileName[((Integer)
            // i.next()).intValue()], area);
            /*
             * val = ImLoadOne(FileName[((Integer) i.next()).intValue()], area);
             * if (val){ if (_isGeoImage){ geoImgObj = _sourceGeoObject;
             * //geoImgObj.PrintGeoImageObject(); } else{
             * System.err.println("GeoObject creation failed."); } } else {
             * System.err.println("Image loading failed."); }
             */

            // geoImgObj = ImageLoader.readImage(FileName[ ( (Integer)
            // i.next()).
            // intValue()]);
            // read the tile
            ImageObject tile;
            try {
                tile = ImageLoader.readImage(FileName[i]);
            } catch (IOException ex) {
                continue;
            }

            // SUBSAMPLE ALL IMAGES HERE
            float sampRatioRow = 1;
            float sampRatioCol = 1;
            double geoColRes = -1;
            double geoRowRes = -1;
            geoColRes = mosaicProj.GetColumnResolution();
            geoRowRes = Math.abs(mosaicProj.GetRowResolution());
            /*
             * if (isInDegrees(geoImgObj)) { double[] colRowRes =
             * convertDegreeScale2MeterScale(geoImgObj); geoColRes =
             * colRowRes[0]; geoRowRes = colRowRes[1]; }
             */
            if ((Math.abs(geoRowRes - maxRowResolution) > EPSILON) || (Math.abs(geoColRes - maxColResolution) > EPSILON)) {
                sampRatioCol = (float) (maxColResolution / geoColRes);
                sampRatioRow = (float) (maxRowResolution / geoRowRes);
                if (sampRatioCol < 1) {
                    sampRatioCol = 1;
                }
                if (sampRatioRow < 1) {
                    sampRatioRow = 1;
                    // Sampling mySamp = new Sampling(sampRatioRow,
                    // sampRatioCol);
                    // geoImgObj = mySamp.SubSamplingFloatGeo(geoImgObj);
                }
                tile = tile.scale(1.0 / sampRatioCol, 1.0 / sampRatioRow);
            }
            // ALTER SAMPLE TYPE IF NECESSARY
            if (tile.getType() != mosaicImage.getType()) {
                tile.convert(mosaicImage.getType(), false);
            }

            // ALTER SAMPLES PER PIXEL IF NECESSARY
            if (tile.getNumBands() != mosaicImage.getNumBands()) {
                // int[] bands = {
                // 1, 1, 1};
                // theImg = geoImgObj.CopySelectedBands(geoImgObj, bands, 3);
                // geoImgObj = geoImgObj.extractBand(bands);

                Projection theproj = (Projection) tile.getProperty(ImageObject.GEOINFO);

                ImageObject[] tmp = { tile, tile, tile };
                tile = tile.add(tmp);
                tile.setProperty(ImageObject.GEOINFO, theproj);
            }

            // LAM --- should be executed when the tiles have different
            // projections

            long time1 = System.currentTimeMillis();
            ImageSplit imgSplit = new ImageSplit(tile);
            long time2 = System.currentTimeMillis();
            System.err.println("splitting time is " + (time2 - time1));
            Vector imgPieceVec = imgSplit.getSplitVector();
            Vector coordVec = imgSplit.getCoordVector();
            Iterator imgI = imgPieceVec.iterator();
            Iterator corI = coordVec.iterator();

            /** ***************NEW CODE****************************** */

            /*
             * int[] rowColArray = imgSplit.getRowColArray(); double[]
             * latLongArray = imgSplit.getLatLongArray(); int sizeTotal = 0; int
             * rowCoord = 0; int colCoord = 0; int blockNumHorizontal =
             * imgSplit.getBlockNumHorizontal(); int blockNumVertical =
             * imgSplit.getBlockNumVertical(); int blocksize =
             * imgSplit.getBlockSize(); int bottomrowblocknum =
             * imgSplit.getBottomRowBlockNumHorizontal(); int bottomrowdim =
             * imgSplit.getBottomRowDimension(); int rightsideblocknum =
             * imgSplit.getRightSideBlockNumVertical(); int rightsidedim =
             * imgSplit.getRightSideDimension(); int leftoverblockrowdim =
             * imgSplit.getLeftOverBlockRowDimension(); int leftoverblockcoldim =
             * imgSplit.getLeftOverBlockColDimension(); int idx=0; boolean ret1 =
             * false; boolean ret2 = false; boolean ret3 = false; SubArea
             * defaultSub; SubArea bottomRowSub; SubArea rightSideSub; coordPt1 =
             * new Point2DDouble(1); coordPt2 = new Point2DDouble(1); defaultSub =
             * new SubArea(0,0,blocksize,blocksize,true,false); bottomRowSub =
             * new SubArea(0,0,bottomrowdim,bottomrowdim, true,false);
             * rightSideSub = new SubArea(0,0,rightsidedim,rightsidedim, true,
             * false); ImageObject tmpImbottom = new ImageObject(bottomrowdim,
             * bottomrowdim, geoObject.sampPerPixel, geoObject.sampType);
             * ImageObject tmpImright = new ImageObject(rightsidedim,
             * rightsidedim, geoObject.sampPerPixel, geoObject.sampType);
             * ImageObject tmpImnorm = new ImageObject(blocksize, blocksize,
             * geoObject.sampPerPixel,geoObject.sampType);
             * /*System.err.println("blockNumHorizontal is
             * "+blockNumHorizontal); System.err.println("blockNumVertical is
             * "+blockNumVertical); System.err.println("bottomRowBlockNum is
             * "+bottomrowblocknum); System.err.println("rightsideBlockNum is
             * "+rightsideblocknum);
             */

            /*
             * for (int h=0; h< blockNumHorizontal; h++){ for (int j=0; j<
             * blockNumVertical; j++){ rowCoord = rowColArray[idx];
             * //System.err.println("idx = "+idx); colCoord =
             * rowColArray[idx+1]; defaultSub.Row = rowCoord; defaultSub.Col =
             * colCoord; coordPt1.ptsDouble[0] = latLongArray[idx];
             * coordPt1.ptsDouble[1] = latLongArray[idx+1]; idx += 2; ret1 =
             * geoImgObj.CopyAreaImageObjectInternal(tmpImnorm, defaultSub); if
             * (!ret1){ System.err.println("splitting failed at 2414"); }
             * coordPt2 = geoConObj.LatLng2ColumnRow(coordPt1);
             * geoObject.InsertArea(tmpImnorm, (int) coordPt2.ptsDouble[1],
             * (int) coordPt2.ptsDouble[0]); } } for (int h=0; h<
             * bottomrowblocknum; h++){ rowCoord = rowColArray[idx];
             * //System.err.println("idx = "+idx); colCoord =
             * rowColArray[idx+1]; bottomRowSub.Row = rowCoord; bottomRowSub.Col =
             * colCoord; coordPt1.ptsDouble[0] = latLongArray[idx];
             * coordPt1.ptsDouble[1] = latLongArray[idx+1]; idx += 2; ret1 =
             * geoImgObj.CopyAreaImageObjectInternal(tmpImbottom, bottomRowSub);
             * if (!ret1){ System.err.println("splitting failed at 2432"); }
             * coordPt2 = geoConObj.LatLng2ColumnRow(coordPt1);
             * geoObject.InsertArea(tmpImbottom, (int) coordPt2.ptsDouble[1],
             * (int) coordPt2.ptsDouble[0]); } for (int h=0; h<
             * rightsideblocknum; h++){ rowCoord = rowColArray[idx];
             * //System.err.println("idx = "+idx); colCoord =
             * rowColArray[idx+1]; rightSideSub.Row = rowCoord; rightSideSub.Col =
             * colCoord; coordPt1.ptsDouble[0] = latLongArray[idx];
             * coordPt1.ptsDouble[1] = latLongArray[idx+1]; idx += 2; ret1 =
             * geoImgObj.CopyAreaImageObjectInternal(tmpImright, rightSideSub);
             * if (!ret1){ System.err.println("splitting failed at 2449"); }
             * geoObject.InsertArea(tmpImright, (int) coordPt2.ptsDouble[1],
             * (int) coordPt2.ptsDouble[0]); } rowCoord = rowColArray[idx];
             * //System.err.println("idx = "+idx); colCoord =
             * rowColArray[idx+1]; ImageObject tmpIm = new
             * ImageObject(leftoverblockrowdim,rightsidedim,
             * geoObject.sampPerPixel,geoObject.sampType); SubArea sub1 = new
             * SubArea(rowCoord, colCoord, leftoverblockrowdim,
             * rightsidedim,true,false); sub1.Row = rowCoord; sub1.Col =
             * colCoord; coordPt1.ptsDouble[0] = latLongArray[idx];
             * coordPt1.ptsDouble[1] = latLongArray[idx+1]; idx += 2; ret2 =
             * geoImgObj.CopyAreaImageObjectInternal(tmpIm, sub1); if (!ret2){
             * System.err.println("splitting failed at 2449"); }
             * geoObject.InsertArea(tmpIm, (int) coordPt2.ptsDouble[1], (int)
             * coordPt2.ptsDouble[0]); rowCoord = rowColArray[idx];
             * //System.err.println("idx = "+idx); colCoord =
             * rowColArray[idx+1]; ImageObject tmpIm2 = new
             * ImageObject(bottomrowdim,leftoverblockcoldim,
             * geoObject.sampPerPixel,geoObject.sampType); SubArea sub2 = new
             * SubArea(rowCoord, colCoord, bottomrowdim,
             * leftoverblockcoldim,true,false); sub2.Row = rowCoord; sub2.Col =
             * colCoord; coordPt1.ptsDouble[0] = latLongArray[idx];
             * coordPt1.ptsDouble[1] = latLongArray[idx+1]; idx += 2; ret3 =
             * geoImgObj.CopyAreaImageObjectInternal(tmpIm2, sub2); if (!ret3){
             * System.err.println("splitting failed at 2485"); }
             * geoObject.InsertArea(tmpIm2, (int) coordPt2.ptsDouble[1], (int)
             * coordPt2.ptsDouble[0]);
             */

            // sizeTotal += leftoverblockcoldim*bottomrowdim;
            /** ***************END NEW CODE***************************** */

            // LAM 5.27
            while (imgI.hasNext()) {
                ImageObject lilObj = (ImageObject) imgI.next();
                double[] coordPt1 = (double[]) corI.next();
                // System.err.println("the lat is "+coordPt1.ptsDouble[0]);
                // System.err.println("the lng is "+coordPt1.ptsDouble[1]);
                double[] coordPt2 = mosaicProj.LatLng2ColumnRow(coordPt1);
                coordPt2[0] = Math.round(coordPt2[0]);
                coordPt2[1] = Math.round(coordPt2[1]);
                // System.err.println("the column is "+coordPt2.ptsDouble[0]);
                // System.err.println("the row is "+coordPt2.ptsDouble[1]);
                if (!(coordPt2[1] < 0 || coordPt2[0] < 0)) {
                    try {
                        mosaicImage.insert(lilObj, (int) coordPt2[1], (int) coordPt2[0]);
                    } catch (Exception ex) {

                    }
                }
            }

            // } // if
            /*
             * else { // insert the next tile Point2DDouble colrow = new
             * Point2DDouble(2); colrow.ptsDouble[0] = 0; colrow.ptsDouble[1] =
             * 0; GeoConvert gc = new GeoConvert(geoImgObj); Point2DDouble
             * latlong = gc.ColumnRow2LatLng(colrow); Point2DDouble insertion =
             * geoConObj.LatLng2ColumnRow(latlong);
             * geoObject.PrintGeoImageObject(); System.out.println("*****");
             * geoImgObj.PrintGeoImageObject(); System.out.println("NR:
             * "+geoImgObj.numrows+" NC: "+geoImgObj.numcols);
             * System.out.println("Insertion Point: ");
             * insertion.PrintPoint2DDouble(); if (! (insertion.ptsDouble[1] < 0 ||
             * insertion.ptsDouble[0] < 0)) { geoObject.InsertArea(geoImgObj,
             * (int) insertion.ptsDouble[1], (int) insertion.ptsDouble[0]); } }
             */
        }
        long t2 = System.currentTimeMillis();
        long diff = t2 - t1;
        System.err.println("ImageSlicing took " + diff + " milliseconds");
        // _isGeoImage = true;
        // _sourceGeoObject = geoObject;
        // _sourceObject = _sourceGeoObject.GetImageObject();
        // return true;
        return mosaicImage;
    }

    public static final double EPSILON = Math.pow(10, -4);

    /*
     * private static ImageObject mosaicModelProjection(String[] filenames,
     * SubArea area, ImageObject[] tileHeaders) { double minNorthing =
     * Double.POSITIVE_INFINITY, maxNorthing = Double.NEGATIVE_INFINITY,
     * minEasting = Double.POSITIVE_INFINITY, maxEasting =
     * Double.NEGATIVE_INFINITY; int numFiles = filenames.length; int
     * bottomTileIndex, topTileIndex, leftTileIndex, rightTileIndex; // for each
     * file for (int band = 0; band < numFiles; band++) { ImageObject
     * headerObject = tileHeaders[band]; if (headerObject != null) {
     * ModelProjection proj = (ModelProjection) headerObject.getProperty(
     * ImageObject.GEOINFO); // find the min northing of the tile double minN =
     * proj.getMinNorthing(); // find the max northing of the tile double maxN =
     * proj.getMaxNorthing(); // find the min easting of the tile double minE =
     * proj.getMinEasting(); // find the max easting of the tile double maxE =
     * proj.getMaxEasting(); if (minN < minNorthing) { minNorthing = minN;
     * bottomTileIndex = band; } if (maxN > maxNorthing) { maxNorthing = maxN;
     * topTileIndex = band; } if (minE < minEasting) { minEasting = minE;
     * leftTileIndex = band; } if (maxE > maxEasting) { maxEasting = maxE;
     * rightTileIndex = band; } double curColResolution =
     * proj.GetColumnResolution(); double curRowResolution =
     * Math.abs(proj.GetRowResolution()); if (curColResolution >
     * maxColResolution) { maxColResolution = curColResolution; maxColTileIndex =
     * band; } if (curRowResolution > maxRowResolution) { maxRowResolution =
     * curRowResolution; maxRowTileIndex = band; } int curSampPerPixelSize =
     * headerObject.getNumBands(); if (curSampPerPixelSize >
     * maxSampPerPixelSize) { maxSampPerPixelSize = curSampPerPixelSize; } } //
     * end of if headerObject != null } //end for loop return null; }
     */

    protected static double findRowResolutionInDegrees(Projection p) throws GeoException {
        double[] upperLeftColRow = { 0, 0 };

        double[] upperleftLatLng = p.colRowToLatLon(upperLeftColRow);

        double[] lowerLeftColRow = { 0, p.getNumRows() - 1 };

        double[] lowerleftLatLng = p.colRowToLatLon(lowerLeftColRow);

        double deltaLat = lowerleftLatLng[0] - upperleftLatLng[0];

        return deltaLat / p.getNumRows();
    }

    protected static double findColResolutionInDegrees(Projection p) throws GeoException {
        double[] upperLeftColRow = { 0, 0 };

        double[] upperleftLatLng = p.colRowToLatLon(upperLeftColRow);

        double[] upperRightColRow = { p.getNumCols() - 1, 0 };

        double[] upperRightLatLng = p.colRowToLatLon(upperRightColRow);

        double deltaLng = upperRightLatLng[1] - upperleftLatLng[1];

        return deltaLng / p.getNumCols();
    }

    protected static boolean isSameProjection(ImageObject[] headers) {

        return false;
    }

}
