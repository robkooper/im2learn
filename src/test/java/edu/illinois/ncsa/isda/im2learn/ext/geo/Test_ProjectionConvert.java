package edu.illinois.ncsa.isda.im2learn.ext.geo;

/**
 *
 * @author  Peter Bajcsy
 * @version 1.0
 */

import java.io.IOException;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageException;
import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.im2learn.core.geo.AngularUnit;
import edu.illinois.ncsa.isda.im2learn.core.geo.BoundingBox;
import edu.illinois.ncsa.isda.im2learn.core.geo.Datum;
import edu.illinois.ncsa.isda.im2learn.core.geo.GeoException;
import edu.illinois.ncsa.isda.im2learn.core.geo.GeoGraphicCoordinateSystem;
import edu.illinois.ncsa.isda.im2learn.core.geo.GeoUtilities;
import edu.illinois.ncsa.isda.im2learn.core.geo.GeodeticPoint;
import edu.illinois.ncsa.isda.im2learn.core.geo.LinearUnit;
import edu.illinois.ncsa.isda.im2learn.core.geo.ModelPoint;
import edu.illinois.ncsa.isda.im2learn.core.geo.PrimeMeridian;
import edu.illinois.ncsa.isda.im2learn.core.geo.Projection.ProjectionType;
import edu.illinois.ncsa.isda.im2learn.core.geo.ProjectionConvert;
import edu.illinois.ncsa.isda.im2learn.core.geo.RasterPoint;
import edu.illinois.ncsa.isda.im2learn.core.geo.TiePoint;
import edu.illinois.ncsa.isda.im2learn.core.geo.projection.Ellipsoid;
import edu.illinois.ncsa.isda.im2learn.core.io.ImageLoader;
import edu.illinois.ncsa.isda.im2learn.core.io.tiff.GeoEntry;

public class Test_ProjectionConvert {

    public boolean     _testPassed = true;

    private static Log logger      = LogFactory.getLog(Test_ProjectionConvert.class);

    // args[0] - input image file name in TIFF file format.
    // args[1] - output image file name in TIFF file format.
    // Operation- conversion from the current datum & projection
    // into datum = WGS1984 and Projection = Alber Equal Area Conic
    public static void main(String args[]) throws Exception {

        Test_ProjectionConvert myTest = new Test_ProjectionConvert();

        logger.debug("argument length=" + args.length);
        for (int i = 0; i < args.length; i++) {
            logger.debug("args[" + i + "]:" + args[i]);
        }
        if (args.length < 1) {
            logger.error("Needed InFileName followed by OutFileName");
            return;
        }
        String InFileName, OutFileName;
        InFileName = args[0];
        logger.debug("InFilename=" + InFileName);

        OutFileName = args[1];
        logger.debug("OutFileName=" + OutFileName);

        boolean ret = true;
        try {
            myTest.Test_RasterConvert(OutFileName);

            // myTest.RasterConvert(InFileName, OutFileName);
        } catch (GeoException g) {
            ret = false;
            logger.debug("GeoException: Test Result = " + ret, g);
        } catch (IOException io) {
            ret = false;
            logger.debug("IOException: Test Result = " + ret, io);
        } catch (ImageException im) {
            ret = false;
            logger.debug("ImageException:Test Result = " + ret, im);
        }
        logger.debug("Test Result = " + ret);

    }

    // constructor
    public Test_ProjectionConvert() {
    }

    /**
     * This method is taking the file names, loading the input file, calling the
     * main re-projection method and saving out the resulting file
     * 
     * @param InFileName
     * @param OutFileName
     * @throws GeoException
     * @throws IOException
     * @throws ImageException
     */
    public void RasterConvert(String InFileName, String OutFileName) throws GeoException, IOException, ImageException {

        // //////////////////////
        // load file
        ImageObject testObject = null;
        testObject = ImageLoader.readImage(InFileName);
        logger.debug("Info: this is the input image filename=" + InFileName);
        // check
        logger.debug("\n input image");
        logger.debug(testObject.toString());
        logger.debug(testObject.getProperty(ImageObject.GEOINFO).toString());

        // conversion
        ImageObject resObject = RasterConvert(testObject);

        // check
        logger.debug("\n resulting image");
        logger.debug(resObject.toString());
        logger.debug(resObject.getProperty(ImageObject.GEOINFO).toString());

        // save out the file
        ImageLoader.writeImage(OutFileName, resObject);

    }

    /**
     * This is the main re-projection class
     * 
     * @param testObject
     *            - input ImageObject with georeferencing information
     * @return resulting ImageObject
     * @throws GeoException
     * @throws IOException
     * @throws ImageException
     */
    public ImageObject RasterConvert(ImageObject testObject) throws GeoException, IOException, ImageException {

        // //////////////
        // convert old to new projection
        edu.illinois.ncsa.isda.im2learn.core.geo.projection.Projection oldProj = (edu.illinois.ncsa.isda.im2learn.core.geo.projection.Projection) testObject.getProperty(ImageObject.GEOINFO);
        edu.illinois.ncsa.isda.im2learn.core.geo.Projection newProj = null;
        try {
            newProj = ProjectionConvert.toNew(oldProj);
        } catch (GeoException e) {
            logger.error("Error: could not support projection with the new Projection classes");
        }
        logger.debug("MODEL ORIG IMAGE : " + GeoUtilities.getModelBounds(testObject, newProj));
        logger.debug("EARTH ORIG IMAGE : " + GeoUtilities.getEarthBounds(testObject, newProj));

        // ///////////////////////////
        // define target projection
        /*
         * From James According to ArcGIS, the parameters for the U.S.
         * Contiguous Albers Equal Area Conic are as follows: False Easting: 0.0
         * False Northing: 0.0 Central Meridian: -96.0 First Standard Parallel:
         * 29.5 Second Standard Parallel: 45.5 Latitude of Origin: 23.0 Linear
         * Unit: Meter (1.0) // Henrik Datum WGS 84 Projection: Albers Equal
         * Area Standard Parallel 1 29�30' N Lat Standard Parallel 2 45�30' N
         * Lat Central Meridian 96�W Lon Latitude of Origin 23�N Lat
         */

        GeoGraphicCoordinateSystem geogcs = new GeoGraphicCoordinateSystem(Datum.WGS_1984.getName(), Datum.WGS_1984, PrimeMeridian.Greenwich, AngularUnit.Decimal_Degree);

        // this would be the default values if we did not have any information
        // from the loaded file
        // ModelPoint mp = new ModelPoint(0.0, 0.0, 0.0);
        // RasterPoint rp = new RasterPoint(testObject.getNumRows(), 0.0, 0.0);
        // TiePoint tp = new TiePoint(mp, rp, 1.0,1.0,1.0);

        // the tie point of the target projection should be the same as in the
        // original file
        TiePoint tpOld = newProj.getTiePoint();
        ModelPoint mp = new ModelPoint(tpOld.getModelPoint());
        TiePoint tp = null;
        if (newProj.getType().equals(edu.illinois.ncsa.isda.im2learn.core.geo.Projection.ProjectionType.Geographic)) {
            if (!mp.getXYUnit().equals(AngularUnit.Decimal_Degree)) {
                // the model point is in angular unit different than
                // decimal_degrees
                mp.setX(mp.getX() * mp.getXYUnit().getMultiplier());
                mp.setY(mp.getY() * mp.getXYUnit().getMultiplier());
                mp.setXYUnit(AngularUnit.Decimal_Degree);
            }
            if (!mp.getZUnit().equals(LinearUnit.Meter)) {
                mp.setZ(mp.getZ() * mp.getZUnit().getMultiplier());
                mp.setZUnit(LinearUnit.Meter);
            }

            GeodeticPoint gptemp = new GeodeticPoint(mp.getX(), mp.getY(), mp.getZ(), newProj.getGeographicCoordinateSystem(), AngularUnit.Decimal_Degree, LinearUnit.Meter);
            // convert geodetic values to model values (meters)
            mp = newProj.earthToModel(gptemp);
        } else {
            if (!mp.getXYUnit().equals(LinearUnit.Meter)) {
                // the model point is in linear unit different than meters
                mp.setX(mp.getX() * mp.getXYUnit().getMultiplier());
                mp.setY(mp.getY() * mp.getXYUnit().getMultiplier());
                mp.setXYUnit(LinearUnit.Meter);
            }
            if (!mp.getZUnit().equals(LinearUnit.Meter)) {
                // the model point is in linear unit different than meters
                mp.setZ(mp.getZ() * mp.getZUnit().getMultiplier());
                mp.setZUnit(LinearUnit.Meter);
            }
        }
        tp = new TiePoint(mp, tpOld.getRasterPoint(), 1000, 1000, 1000);

        // ///////////////
        // parameters
        HashMap<String, String> param = new HashMap<String, String>();
        param.put(edu.illinois.ncsa.isda.im2learn.core.geo.Projection.FALSE_EASTING, "0");
        param.put(edu.illinois.ncsa.isda.im2learn.core.geo.Projection.FALSE_NORTHING, "0");
        param.put(edu.illinois.ncsa.isda.im2learn.core.geo.Projection.STANDARD_PARALLEL_1, "29.5");
        param.put(edu.illinois.ncsa.isda.im2learn.core.geo.Projection.STANDARD_PARALLEL_2, "45.5");
        param.put(edu.illinois.ncsa.isda.im2learn.core.geo.Projection.LATITUDE_OF_ORIGIN, "23.0");
        param.put(edu.illinois.ncsa.isda.im2learn.core.geo.Projection.CENTRAL_MERIDIAN, "-96");

        // the unit of the parameters
        LinearUnit unit = LinearUnit.Meter;

        edu.illinois.ncsa.isda.im2learn.core.geo.Projection targetProj = edu.illinois.ncsa.isda.im2learn.core.geo.Projection.getProjection("Albers Equal Area Conic",
                edu.illinois.ncsa.isda.im2learn.core.geo.Projection.ProjectionType.Albers, geogcs, param, null, unit);

        // /////////////////////////////////////////////////////////////
        // need to convert the tiepoint to the new projection system
        RasterPoint rp = new RasterPoint(newProj.getTiePoint().getRasterPoint());
        mp = newProj.getTiePoint().getModelPoint();
        mp = targetProj.earthToModel(newProj.modelToEarth(mp));
        if (targetProj.getType().equals(ProjectionType.Geographic)) {
            // 33/3600 is approx 1km in arc degrees
            // TODO we should take 3 pixels, next and above to calculate the dx
            // dy
            tp = new TiePoint(mp, rp, 33.0 / 3600, 33.0 / 3600, 1);
        } else {
            tp = new TiePoint(mp, rp, 1000, 1000, 1);
        }
        targetProj.setTiePoint(tp);

        // ///////////////////////////////
        // conversion
        // ///////
        // determine the approximate size of the re-projected image
        // the accurate computation should re-project all boundary points !!
        RasterPoint rptmp = new RasterPoint(0, 0);
        GeodeticPoint gp = newProj.rasterToEarth(rptmp);
        RasterPoint rp00 = targetProj.earthToRaster(gp);// this is taking care
        // of different datums

        rptmp = new RasterPoint(testObject.getNumRows(), testObject.getNumCols());
        gp = newProj.rasterToEarth(rptmp);
        RasterPoint rpNumRowsNumCols = targetProj.earthToRaster(gp);// this is
        // taking
        // care of
        // different
        // datums

        rptmp = new RasterPoint(0, testObject.getNumCols());
        gp = newProj.rasterToEarth(rptmp);
        RasterPoint rp0NumCols = targetProj.earthToRaster(gp);// this is
        // taking care
        // of different
        // datums

        rptmp = new RasterPoint(testObject.getNumRows(), 0);
        gp = newProj.rasterToEarth(rptmp);
        RasterPoint rpNumRows0 = targetProj.earthToRaster(gp);// this is
        // taking care
        // of different
        // datums

        double minRow, minCol, maxRow, maxCol;
        double v[] = new double[4];
        v[0] = rp00.getRow();
        v[1] = rp0NumCols.getRow();
        v[2] = rpNumRows0.getRow();
        v[3] = rpNumRowsNumCols.getRow();
        minRow = findMin(v);
        maxRow = findMax(v);
        v[0] = rp00.getCol();
        v[1] = rp0NumCols.getCol();
        v[2] = rpNumRows0.getCol();
        v[3] = rpNumRowsNumCols.getCol();
        minCol = findMin(v);
        maxCol = findMax(v);

        // / need to fix the tiepoint
        rp = targetProj.getTiePoint().getRasterPoint();
        rp.setX(rp.getX() - minCol);
        rp.setY(rp.getY() - minRow);

        // TODO there is a problem with this formula when geographic projection
        // is used !!!!
        int numrows = (int) (maxRow - minRow + 0.5);
        int numcols = (int) (maxCol - minCol + 0.5);
        if ((numrows < 1) || (numcols < 1)) {
            throw new GeoException("ERROR: the resulting image has invalid dimensions: numrows=" + numrows + ", numcols=" + numcols);
        }

        ImageObject resObject = ImageObject.createImage(numrows, numcols, testObject.getNumBands(), testObject.getType());
        resObject.setInvalidData(0);
        resObject.setData(0);
        logger.debug("MODEL TEST IMAGE : " + GeoUtilities.getModelBounds(resObject, targetProj));
        logger.debug("EARTH TEST IMAGE : " + GeoUtilities.getEarthBounds(resObject, targetProj));
        BoundingBox bbox = GeoUtilities.getEarthBounds(testObject, newProj);
        rp = targetProj.earthToRaster(new GeodeticPoint(bbox.getMinY(), bbox.getMinX()));
        logger.debug(rp);
        rp = targetProj.earthToRaster(new GeodeticPoint(bbox.getMinY(), bbox.getMaxX()));
        logger.debug(rp);
        rp = targetProj.earthToRaster(new GeodeticPoint(bbox.getMaxY(), bbox.getMaxX()));
        logger.debug(rp);
        rp = targetProj.earthToRaster(new GeodeticPoint(bbox.getMaxY(), bbox.getMinX()));
        logger.debug(rp);

        // debug
        logger.debug("newProj = " + newProj.toString());
        logger.debug("targetProj = " + targetProj.toString());

        // perform calculations
        RasterPoint rp1 = new RasterPoint(0, 0);
        RasterPoint rp2 = null;
        GeodeticPoint gp1 = null;
        int indexFrom, indexTo, i, j, k;
        double fromRow = 0, fromCol = 0;
        double val = 0.0;
        for (i = 0; i < resObject.getNumRows(); i++) {
            for (j = 0; j < resObject.getNumCols(); j++) {
                // from resulting coordinate system to original data coordinate
                // system
                rp1.setRow(i);
                rp1.setCol(j);
                gp1 = targetProj.rasterToEarth(rp1);
                rp2 = newProj.earthToRaster(gp1); // this is taking care of
                // datum shifts
                fromRow = rp2.getRow();
                fromCol = rp2.getCol();
                // if(fromRow>=0 && rp2.getCol()>=0 && rp2.getRow()<
                // testObject.getNumRows() && rp2.getCol()<
                // testObject.getNumCols() ){
                if ((fromRow >= 0) && (fromCol >= 0) && (fromRow < testObject.getNumRows()) && (fromCol < testObject.getNumCols())) {
                    indexTo = (i * resObject.getNumCols() + j) * resObject.getNumBands();
                    // this is the nearest neighbor approximation
                    // could be improved with other interpolation methods
                    // indexFrom =
                    // ((int)(rp2.getRow()+0.5)*testObject.getNumCols() +
                    // (int)(rp2.getCol() +0.5) ) * testObject.getNumBands();
                    indexFrom = ((int) (fromRow + 0.5) * testObject.getNumCols() + (int) (fromCol + 0.5)) * testObject.getNumBands();
                    // check the upper limit due to the rounding above
                    if (indexFrom >= testObject.getSize() - testObject.getNumBands()) {
                        indexFrom = testObject.getSize() - testObject.getNumBands() - 1;
                    }
                    for (k = 0; k < resObject.getNumBands(); k++) {
                        val = testObject.getDouble(indexFrom + k);
                        resObject.set(indexTo + k, val);
                    }

                }

            }
        }

        resObject.setProperty(ImageObject.GEOINFO, ProjectionConvert.toOld(targetProj));

        return resObject;
    }

    /**
     * This method takes four corners of the input image and verifies that the
     * raster to earth using the input image projection followed by earth to
     * raster using the target image projection followed by the reverse sequence
     * will lead to the identical image corner points
     * 
     * @param testObject
     *            - input image
     * @throws GeoException
     * @throws IOException
     * @throws ImageException
     */
    public void VerifyRasterCorners_toAndFromEarthConvert(ImageObject testObject) throws GeoException, IOException, ImageException {

        // //////////////
        // convert old to new projection
        edu.illinois.ncsa.isda.im2learn.core.geo.projection.Projection oldProj = (edu.illinois.ncsa.isda.im2learn.core.geo.projection.Projection) testObject.getProperty(ImageObject.GEOINFO);
        edu.illinois.ncsa.isda.im2learn.core.geo.Projection newProj = null;
        try {
            newProj = ProjectionConvert.toNew(oldProj);
        } catch (GeoException e) {
            logger.error("Error: could not support projection with the new Projection classes");
        }
        logger.debug("MODEL ORIG IMAGE : " + GeoUtilities.getModelBounds(testObject, newProj));
        logger.debug("EARTH ORIG IMAGE : " + GeoUtilities.getEarthBounds(testObject, newProj));

        // ///////////////////////////
        // define target projection
        GeoGraphicCoordinateSystem geogcs = new GeoGraphicCoordinateSystem(Datum.WGS_1984.getName(), Datum.WGS_1984, PrimeMeridian.Greenwich, AngularUnit.Decimal_Degree);

        // the tie point of the target projection should be the same as in the
        // original file
        TiePoint tpOld = newProj.getTiePoint();
        ModelPoint mp = new ModelPoint(tpOld.getModelPoint());
        TiePoint tp = null;
        if (newProj.getType().equals(edu.illinois.ncsa.isda.im2learn.core.geo.Projection.ProjectionType.Geographic)) {
            if (!mp.getXYUnit().equals(AngularUnit.Decimal_Degree)) {
                // the model point is in angular unit different than
                // decimal_degrees
                mp.setX(mp.getX() * mp.getXYUnit().getMultiplier());
                mp.setY(mp.getY() * mp.getXYUnit().getMultiplier());
                mp.setXYUnit(AngularUnit.Decimal_Degree);
            }
            if (!mp.getZUnit().equals(LinearUnit.Meter)) {
                mp.setZ(mp.getZ() * mp.getZUnit().getMultiplier());
                mp.setZUnit(LinearUnit.Meter);
            }
            GeodeticPoint gptemp = new GeodeticPoint(mp.getX(), mp.getY(), mp.getZ(), newProj.getGeographicCoordinateSystem(), AngularUnit.Decimal_Degree, LinearUnit.Meter);
            // convert geodetic values to model values (meters)
            mp = newProj.earthToModel(gptemp);
        } else {
            if (!mp.getXYUnit().equals(LinearUnit.Meter)) {
                // the model point is in linear unit different than meters
                mp.setX(mp.getX() * mp.getXYUnit().getMultiplier());
                mp.setY(mp.getY() * mp.getXYUnit().getMultiplier());
                mp.setXYUnit(LinearUnit.Meter);
            }
            if (!mp.getZUnit().equals(LinearUnit.Meter)) {
                // the model point is in linear unit different than meters
                mp.setZ(mp.getZ() * mp.getZUnit().getMultiplier());
                mp.setZUnit(LinearUnit.Meter);
            }
        }
        tp = new TiePoint(mp, tpOld.getRasterPoint(), 1000, 1000, 1000);

        // ///////////////
        // parameters
        HashMap<String, String> param = new HashMap<String, String>();
        param.put(edu.illinois.ncsa.isda.im2learn.core.geo.Projection.FALSE_EASTING, "0");
        param.put(edu.illinois.ncsa.isda.im2learn.core.geo.Projection.FALSE_NORTHING, "0");
        param.put(edu.illinois.ncsa.isda.im2learn.core.geo.Projection.STANDARD_PARALLEL_1, "29.5");
        param.put(edu.illinois.ncsa.isda.im2learn.core.geo.Projection.STANDARD_PARALLEL_2, "45.5");
        param.put(edu.illinois.ncsa.isda.im2learn.core.geo.Projection.LATITUDE_OF_ORIGIN, "23.0");
        param.put(edu.illinois.ncsa.isda.im2learn.core.geo.Projection.LONGITUDE_OF_ORIGIN, "0.0");
        param.put(edu.illinois.ncsa.isda.im2learn.core.geo.Projection.CENTRAL_MERIDIAN, "-96");

        // the unit of the parameters
        LinearUnit unit = LinearUnit.Meter;

        edu.illinois.ncsa.isda.im2learn.core.geo.Projection targetProj = edu.illinois.ncsa.isda.im2learn.core.geo.Projection.getProjection("Albers Equal Area Conic",
                edu.illinois.ncsa.isda.im2learn.core.geo.Projection.ProjectionType.Albers, geogcs, param, tp, unit);

        // /////////////////////////////////////////////////////////////
        // need to convert the tiepoint to the new projection system
        RasterPoint rp = newProj.getTiePoint().getRasterPoint();
        mp = newProj.getTiePoint().getModelPoint();
        mp = targetProj.earthToModel(newProj.modelToEarth(mp));
        if (targetProj.getType().equals(ProjectionType.Geographic)) {
            // 33/3600 is approx 1km in arc degrees
            // TODO we should take 3 pixels, next and above to calculate the dx
            // dy
            tp = new TiePoint(mp, rp, 33.0 / 3600, 33.0 / 3600, 1);
        } else {
            tp = new TiePoint(mp, rp, 1000, 1000, 1);
        }
        targetProj.setTiePoint(tp);

        // ///////////////////////////////
        // conversion
        // ///////
        // convert the image corner points
        RasterPoint rptmp = new RasterPoint(0, 0);
        GeodeticPoint gp = newProj.rasterToEarth(rptmp);
        RasterPoint rp00 = targetProj.earthToRaster(gp);// this is taking care
        // of different datums
        logger.debug("0, 0 = " + rp00);

        GeodeticPoint gpv = targetProj.rasterToEarth(rp00);
        RasterPoint rptmpv = newProj.earthToRaster(gpv);
        double epsilon = 0.5;
        if ((Math.abs(rptmpv.getRow() - rptmp.getRow()) > epsilon) || (Math.abs(rptmpv.getCol() - rptmp.getCol()) > epsilon) || (Math.abs(rptmpv.getHeight() - rptmp.getHeight()) > epsilon)) {
            logger.error("point 00 does not match: original=" + rptmp.toString() + ", after forward and inverse proj=" + rptmpv.toString());
        } else {
            logger.debug("point 00 matches: original=" + rptmp.toString() + ", after forward and inverse proj=" + rptmpv.toString());
        }

        rptmp = new RasterPoint(testObject.getNumRows(), testObject.getNumCols());
        gp = newProj.rasterToEarth(rptmp);
        RasterPoint rpNumRowsNumCols = targetProj.earthToRaster(gp);// this is
        // taking
        // care of
        // different
        // datums
        logger.debug("r, c = " + rpNumRowsNumCols);

        gpv = targetProj.rasterToEarth(rpNumRowsNumCols);
        rptmpv = newProj.earthToRaster(gpv);
        if ((Math.abs(rptmpv.getRow() - rptmp.getRow()) > epsilon) || (Math.abs(rptmpv.getCol() - rptmp.getCol()) > epsilon) || (Math.abs(rptmpv.getHeight() - rptmp.getHeight()) > epsilon)) {
            logger.error("point NumRowsNumCols does not match: original=" + rptmp.toString() + ", after forward and inverse proj=" + rptmpv.toString());
        } else {
            logger.debug("point NumRowsNumCols matches: original=" + rptmp.toString() + ", after forward and inverse proj=" + rptmpv.toString());
        }

        rptmp = new RasterPoint(0, testObject.getNumCols());
        gp = newProj.rasterToEarth(rptmp);
        RasterPoint rp0NumCols = targetProj.earthToRaster(gp);// this is
        // taking care
        // of different
        // datums
        logger.debug("0, c = " + rp0NumCols);

        gpv = targetProj.rasterToEarth(rp0NumCols);
        rptmpv = newProj.earthToRaster(gpv);
        if ((Math.abs(rptmpv.getRow() - rptmp.getRow()) > epsilon) || (Math.abs(rptmpv.getCol() - rptmp.getCol()) > epsilon) || (Math.abs(rptmpv.getHeight() - rptmp.getHeight()) > epsilon)) {
            logger.error("point 0NumCols does not match: original=" + rptmp.toString() + ", after forward and inverse proj=" + rptmpv.toString());
        } else {
            logger.debug("point 0NumCols matches: original=" + rptmp.toString() + ", after forward and inverse proj=" + rptmpv.toString());
        }

        rptmp = new RasterPoint(testObject.getNumRows(), 0);
        gp = newProj.rasterToEarth(rptmp);
        RasterPoint rpNumRows0 = targetProj.earthToRaster(gp);// this is
        // taking care
        // of different
        // datums
        logger.debug("r, 0 = " + rpNumRows0);

        gpv = targetProj.rasterToEarth(rpNumRows0);
        rptmpv = newProj.earthToRaster(gpv);
        if ((Math.abs(rptmpv.getRow() - rptmp.getRow()) > epsilon) || (Math.abs(rptmpv.getCol() - rptmp.getCol()) > epsilon) || (Math.abs(rptmpv.getHeight() - rptmp.getHeight()) > epsilon)) {
            logger.error("point NumRows0 does not match: original=" + rptmp.toString() + ", after forward and inverse proj=" + rptmpv.toString());
        } else {
            logger.debug("point NumRows0 matches: original=" + rptmp.toString() + ", after forward and inverse proj=" + rptmpv.toString());
        }

    }

    /**
     * This method takes four corners of the input image and verifies that for
     * the same input image and target image projections with the same datums
     * and parameters the raster to model using the input image tie point
     * followed by model to raster using the target image tie point followed by
     * the reverse sequence will lead to the identical image corner points
     * 
     * @param testObject
     *            - input image
     * @throws GeoException
     * @throws IOException
     * @throws ImageException
     */
    public void VerifyRasterCorners_toAndFromModelConvert(ImageObject testObject) throws GeoException, IOException, ImageException {

        // //////////////
        // convert old to new projection
        edu.illinois.ncsa.isda.im2learn.core.geo.projection.Projection oldProj = (edu.illinois.ncsa.isda.im2learn.core.geo.projection.Projection) testObject.getProperty(ImageObject.GEOINFO);
        edu.illinois.ncsa.isda.im2learn.core.geo.Projection newProj = null;
        try {
            newProj = ProjectionConvert.toNew(oldProj);
        } catch (GeoException e) {
            logger.error("Error: could not support projection with the new Projection classes");
        }
        // ///////////////////////////
        // define target projection
        GeoGraphicCoordinateSystem geogcs = new GeoGraphicCoordinateSystem(Datum.WGS_1984.getName(), Datum.WGS_1984, PrimeMeridian.Greenwich, AngularUnit.Decimal_Degree);

        // the tie point of the target projection should be the same as in the
        // original file
        TiePoint tpOld = newProj.getTiePoint();
        ModelPoint mp = new ModelPoint(tpOld.getModelPoint());
        TiePoint tp = null;
        if (newProj.getType().equals(edu.illinois.ncsa.isda.im2learn.core.geo.Projection.ProjectionType.Geographic)) {
            if (!mp.getXYUnit().equals(AngularUnit.Decimal_Degree)) {
                // the model point is in angular unit different than
                // decimal_degrees
                mp.setX(mp.getX() * mp.getXYUnit().getMultiplier());
                mp.setY(mp.getY() * mp.getXYUnit().getMultiplier());
                mp.setXYUnit(AngularUnit.Decimal_Degree);
            }
            if (!mp.getZUnit().equals(LinearUnit.Meter)) {
                mp.setZ(mp.getZ() * mp.getZUnit().getMultiplier());
                mp.setZUnit(LinearUnit.Meter);
            }
            GeodeticPoint gptemp = new GeodeticPoint(mp.getX(), mp.getY(), mp.getZ(), newProj.getGeographicCoordinateSystem(), AngularUnit.Decimal_Degree, LinearUnit.Meter);
            // convert geodetic values to model values (meters)
            mp = newProj.earthToModel(gptemp);
        } else {
            if (!mp.getXYUnit().equals(LinearUnit.Meter)) {
                // the model point is in linear unit different than meters
                mp.setX(mp.getX() * mp.getXYUnit().getMultiplier());
                mp.setY(mp.getY() * mp.getXYUnit().getMultiplier());
                mp.setXYUnit(LinearUnit.Meter);
            }
            if (!mp.getZUnit().equals(LinearUnit.Meter)) {
                // the model point is in linear unit different than meters
                mp.setZ(mp.getZ() * mp.getZUnit().getMultiplier());
                mp.setZUnit(LinearUnit.Meter);
            }
        }
        tp = new TiePoint(mp, tpOld.getRasterPoint(), 1000, 1000, 1000);

        // ///////////////
        // parameters
        HashMap<String, String> param = new HashMap<String, String>();
        param.put(edu.illinois.ncsa.isda.im2learn.core.geo.Projection.FALSE_EASTING, "0");
        param.put(edu.illinois.ncsa.isda.im2learn.core.geo.Projection.FALSE_NORTHING, "0");
        param.put(edu.illinois.ncsa.isda.im2learn.core.geo.Projection.STANDARD_PARALLEL_1, "29.5");
        param.put(edu.illinois.ncsa.isda.im2learn.core.geo.Projection.STANDARD_PARALLEL_2, "45.5");
        param.put(edu.illinois.ncsa.isda.im2learn.core.geo.Projection.LATITUDE_OF_ORIGIN, "23.0");
        param.put(edu.illinois.ncsa.isda.im2learn.core.geo.Projection.LONGITUDE_OF_ORIGIN, "0.0");
        param.put(edu.illinois.ncsa.isda.im2learn.core.geo.Projection.CENTRAL_MERIDIAN, "-96");

        // the unit of the parameters
        LinearUnit unit = LinearUnit.Meter;

        edu.illinois.ncsa.isda.im2learn.core.geo.Projection targetProj = edu.illinois.ncsa.isda.im2learn.core.geo.Projection.getProjection("Albers",
                edu.illinois.ncsa.isda.im2learn.core.geo.Projection.ProjectionType.Albers, geogcs, param, tp, unit);

        // compare the projections and datums and parameters
        if (!newProj.getGeographicCoordinateSystem().equals(targetProj.getGeographicCoordinateSystem()) || !newProj.getType().equals(targetProj.getType())) {
            logger.debug("input image projection and target image projections are not equal");
            logger.debug("newProj = " + newProj.toString());
            logger.debug("targetProj = " + targetProj.toString());
            return;
        }

        // ///////////////////////////////
        // conversion
        // ///////
        // convert the image corner points
        RasterPoint rptmp = new RasterPoint(0, 0);
        ModelPoint mptmp = newProj.rasterToModel(rptmp);
        RasterPoint rptmpv = newProj.modelToRaster(mptmp);
        double epsilon = 0.5;
        if ((Math.abs(rptmpv.getRow() - rptmp.getRow()) > epsilon) || (Math.abs(rptmpv.getCol() - rptmp.getCol()) > epsilon) || (Math.abs(rptmpv.getHeight() - rptmp.getHeight()) > epsilon)) {
            logger.error("point 00 does not match: original=" + rptmp.toString() + ", after forward and inverse proj=" + rptmpv.toString());
        } else {
            logger.debug("point 00 matches: original=" + rptmp.toString() + ", after forward and inverse proj=" + rptmpv.toString());
        }

        rptmp = new RasterPoint(testObject.getNumRows(), testObject.getNumCols());
        mptmp = newProj.rasterToModel(rptmp);
        rptmpv = newProj.modelToRaster(mptmp);

        if ((Math.abs(rptmpv.getRow() - rptmp.getRow()) > epsilon) || (Math.abs(rptmpv.getCol() - rptmp.getCol()) > epsilon) || (Math.abs(rptmpv.getHeight() - rptmp.getHeight()) > epsilon)) {
            logger.error("point NumRowsNumCols does not match: original=" + rptmp.toString() + ", after forward and inverse proj=" + rptmpv.toString());
        } else {
            logger.debug("point NumRowsNumCols matches: original=" + rptmp.toString() + ", after forward and inverse proj=" + rptmpv.toString());
        }

        rptmp = new RasterPoint(0, testObject.getNumCols());
        mptmp = newProj.rasterToModel(rptmp);
        rptmpv = newProj.modelToRaster(mptmp);

        if ((Math.abs(rptmpv.getRow() - rptmp.getRow()) > epsilon) || (Math.abs(rptmpv.getCol() - rptmp.getCol()) > epsilon) || (Math.abs(rptmpv.getHeight() - rptmp.getHeight()) > epsilon)) {
            logger.error("point 0NumCols does not match: original=" + rptmp.toString() + ", after forward and inverse proj=" + rptmpv.toString());
        } else {
            logger.debug("point 0NumCols matches: original=" + rptmp.toString() + ", after forward and inverse proj=" + rptmpv.toString());
        }

        rptmp = new RasterPoint(testObject.getNumRows(), 0);
        mptmp = newProj.rasterToModel(rptmp);
        rptmpv = newProj.modelToRaster(mptmp);
        if ((Math.abs(rptmpv.getRow() - rptmp.getRow()) > epsilon) || (Math.abs(rptmpv.getCol() - rptmp.getCol()) > epsilon) || (Math.abs(rptmpv.getHeight() - rptmp.getHeight()) > epsilon)) {
            logger.error("point NumRows0 does not match: original=" + rptmp.toString() + ", after forward and inverse proj=" + rptmpv.toString());
        } else {
            logger.debug("point NumRows0 matches: original=" + rptmp.toString() + ", after forward and inverse proj=" + rptmpv.toString());
        }

    }

    /**
     * This method uses synthetic image to test the main re-projection method
     * 
     * @return
     */
    public boolean Test_RasterConvert(String OutFileName) throws GeoException, IOException, ImageException {

        // /////////////////////////
        // create new image
        ImageObject test = null;
        try {
            test = ImageObject.createImage(100, 100, 1, ImageObject.TYPE_BYTE);
        } catch (Exception e) {
            logger.error("ERROR: could not create an image object");
            return false;
        }

        int i, j, index = 0;
        byte color;
        byte white = -1;
        byte black = 0;
        for (i = 0; i < test.getNumRows(); i++) {
            if (i == (i >> 1) * 2) {
                color = white;
            } else {
                color = black;
            }
            for (j = 0; j < test.getNumCols(); j++) {
                test.set(index, color);
                index++;
            }
        }

        // /////////////////////////////////////
        /*
         * // add georeferencing for Albers // this is the old projection class
         * !!! // ncsa.im2learn.core.geo.projection.Projection geo = //
         * (ncsa.im2learn
         * .core.geo.projection.Projection)test.getProperty(ImageObject
         * .GEOINFO); // ncsa.im2learn.core.geo.projection.AlbersEqualAreaConic
         * geo = //
         * (ncsa.im2learn.core.geo.projection.AlbersEqualAreaConic)test.
         * getProperty(ImageObject.GEOINFO)
         * ncsa.im2learn.core.geo.projection.AlbersEqualAreaConic geo = new
         * ncsa.im2learn.core.geo.projection.AlbersEqualAreaConic();
         * geo.setFalseEasting(0.0); geo.setFalseNorthing(0.0);
         * geo.setInsertionX(0.0); geo.setInsertionY(0.0);
         * geo.setInsertionZ(0.0); geo.setModelSpaceX(0.0);
         * geo.setModelSpaceY(0.0); geo.setModelSpaceZ(0.0);
         * geo.setEllipsoid(Ellipsoid.WGS_84);
         * geo.setEccentricSqure(0.0066943799901413); geo.setRadius(6378137.0);
         * geo.setOrigin(23.0, 0.0); geo.setParallels(29.5, 45.5);
         * geo.setScaleX(1000.0); geo.setScaleY(1000.0); geo.setScaleZ(1.0);
         * geo.setUnit(GeoEntry.Linear_Meter); //
         * geo.setUnit(GeoEntry.Linear_Foot); //
         * geo.setUnit(GeoEntry.Linear_Mile_International_Nautical);
         * 
         * test.setProperty(ImageObject.GEOINFO, geo);
         */
        // /////////////////////////////////////
        // add georeferencing for Lambert Conformal Conic
        // this is the old projection class !!!
        /*
         * ncsa.im2learn.core.geo.projection.LambertConformalConic geo = new
         * ncsa.im2learn.core.geo.projection.LambertConformalConic();
         * geo.setFalseNorth(0.0); geo.setFalseEast(0.0);
         * geo.setInsertionX(0.0); geo.setInsertionY(0.0);
         * geo.setInsertionZ(0.0); geo.setModelSpaceX(0.0);
         * geo.setModelSpaceY(0.0); geo.setModelSpaceZ(0.0);
         * geo.setEllipsoid(Ellipsoid.WGS_84); geo.setRadiusMajor(6378137.0);
         * geo.setRadiusMinor(6356752.31); geo.setFirstParallel(33.0);
         * geo.setSecondParallel(45.0); geo.setCenterLat(23.0);
         * geo.setCenterLong(-96.0); geo.set_scaleFactor(1.0); //
         * geo.set_datum(Ellipsoid.WGS_84); //
         * geo.set_linearUnit(GeoEntry.Linear_Meter); geo.setScaleX(1000.0);
         * geo.setScaleY(1000.0); geo.setScaleZ(1.0);
         * geo.setUnit(GeoEntry.Linear_Meter);
         * test.setProperty(ImageObject.GEOINFO, geo);
         */

        // add georeferencing for Geographic // this is the old projection
        // class !!!
        edu.illinois.ncsa.isda.im2learn.core.geo.projection.Projection geo = new edu.illinois.ncsa.isda.im2learn.core.geo.projection.Projection();
        geo.setInsertionX(-84.0);// in degrees
        geo.setInsertionY(38.0); // in degrees
        geo.setInsertionZ(0.0); // in degrees
        geo.setRasterSpaceI(0.0);
        geo.setRasterSpaceJ(0.0);
        geo.setRasterSpaceK(0.0);
        geo.setEllipsoid(Ellipsoid.WGS_84);
        geo.setScaleX(36.0 / 3600);// this is in angular units too
        geo.setScaleY(36.0 / 3600);// this is in angular units too
        geo.setScaleZ(1.0); // this is in linear units
        geo.setUnit(GeoEntry.Angular_Degree);
        test.setProperty(ImageObject.GEOINFO, geo);

        // check
        logger.debug("\n input synthetic image");
        logger.debug(test.toString());
        // logger.debug(test.getProperty(ImageObject.GEOINFO).toString());

        // /////////////////////////////////////
        // convert
        VerifyRasterCorners_toAndFromEarthConvert(test);
        VerifyRasterCorners_toAndFromModelConvert(test);

        ImageObject resObject = RasterConvert(test);
        // check
        logger.debug("\n resulting image");
        logger.debug(resObject.toString());
        logger.debug(resObject.getProperty(ImageObject.GEOINFO).toString());
        // save out the file
        ImageLoader.writeImage(OutFileName, resObject);

        return true;
    }

    // /////////////////////////
    // help function
    // find min of four numbers
    private double findMin(double[] v) throws IOException {
        // sanity check
        if ((v == null) || (v.length == 0)) {
            throw new IOException("null array of point coordinates");
        }
        double min = v[0];
        for (int i = 1; i < v.length; i++) {
            if (min > v[i]) {
                min = v[i];
            }
        }
        return min;
    }

    // find max of four numbers
    private double findMax(double[] v) throws IOException {
        // sanity check
        if ((v == null) || (v.length == 0)) {
            throw new IOException("null array of point coordinates");
        }
        double max = v[0];
        for (int i = 1; i < v.length; i++) {
            if (max < v[i]) {
                max = v[i];
            }
        }
        return max;
    }
}
