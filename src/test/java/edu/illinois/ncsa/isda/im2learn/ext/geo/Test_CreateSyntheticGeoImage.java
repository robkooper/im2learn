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
import edu.illinois.ncsa.isda.im2learn.core.geo.Datum;
import edu.illinois.ncsa.isda.im2learn.core.geo.GeoException;
import edu.illinois.ncsa.isda.im2learn.core.geo.GeoGraphicCoordinateSystem;
import edu.illinois.ncsa.isda.im2learn.core.geo.LinearUnit;
import edu.illinois.ncsa.isda.im2learn.core.geo.ModelPoint;
import edu.illinois.ncsa.isda.im2learn.core.geo.PrimeMeridian;
import edu.illinois.ncsa.isda.im2learn.core.geo.ProjectionConvert;
import edu.illinois.ncsa.isda.im2learn.core.geo.RasterPoint;
import edu.illinois.ncsa.isda.im2learn.core.geo.TiePoint;
import edu.illinois.ncsa.isda.im2learn.core.io.ImageLoader;

public class Test_CreateSyntheticGeoImage {

    public boolean     _testPassed = true;

    private static Log logger      = LogFactory.getLog(Test_CreateSyntheticGeoImage.class);

    // args[0] - input image file name in TIFF file format.
    // args[1] - output image file name in TIFF file format.
    // Operation- conversion from the current datum & projection
    // into datum = WGS1984 and Projection = Alber Equal Area Conic
    public static void main(String args[]) throws Exception {

        Test_CreateSyntheticGeoImage myTest = new Test_CreateSyntheticGeoImage();

        logger.debug("argument length=" + args.length);
        for (int i = 0; i < args.length; i++) {
            logger.debug("args[" + i + "]:" + args[i]);
        }
        if (args.length < 1) {
            logger.error("Needed  OutFileName");
            return;
        }
        String InFileName, OutFileName;

        InFileName = args[0];
        logger.debug("InFileName=" + InFileName);

        OutFileName = args[1];
        logger.debug("OutFileName=" + OutFileName);

        boolean ret = true;
        try {
            // myTest.CreateImage1(OutFileName);
            myTest.LoadAndCopy(InFileName, OutFileName);
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
    public Test_CreateSyntheticGeoImage() {
    }

    public void CreateImage1(String OutFileName) throws GeoException, IOException, ImageException {

        // //////////////////////
        // load file
        ImageObject testObject = ImageObject.createImage(100, 200, 1, ImageObject.TYPE_BYTE);
        testObject.setData(0);
        testObject.setInvalidData(-1);

        for (int i = 0; i < testObject.getSize() / 2; i++) {
            testObject.set(i, (byte) (testObject.getByte(i) + 50));
        }

        for (int i = 0; i < testObject.getNumRows(); i++) {
            for (int j = 0; j < testObject.getNumCols() / 2; j++) {
                testObject.set(i, j, 0, (byte) (testObject.getByte(i) + 50));
            }
        }
        // add georeferencing information
        edu.illinois.ncsa.isda.im2learn.core.geo.Projection targetProj = setTargetProjection(testObject);
        testObject.setProperty(ImageObject.GEOINFO, ProjectionConvert.toOld(targetProj));

        // save out the file
        ImageLoader.writeImage(OutFileName, testObject);

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
    public edu.illinois.ncsa.isda.im2learn.core.geo.Projection setTargetProjection(ImageObject testObject) throws GeoException, IOException, ImageException {

        // ///////////////////////////
        // define target projection
        GeoGraphicCoordinateSystem geogcs = new GeoGraphicCoordinateSystem(Datum.WGS_1984.getName(), Datum.WGS_1984, PrimeMeridian.Greenwich, AngularUnit.Decimal_Degree);

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

        TiePoint tp = null;
        ModelPoint mp = new ModelPoint(0, 0);
        RasterPoint rp = new RasterPoint(testObject.getNumRows(), 0);
        tp = new TiePoint(mp, rp, 1000, 1000, 1000);
        targetProj.setTiePoint(tp);

        return targetProj;
    }

    public void LoadAndCopy(String InFileName, String OutFileName) throws GeoException, IOException, ImageException {

        // //////////////////////
        // load file
        ImageObject inputObject = ImageLoader.readImage(InFileName);
        ImageObject testObject = ImageObject.createImage(inputObject.getNumRows(), inputObject.getNumCols(), 1, ImageObject.TYPE_BYTE);
        testObject.setData(0);
        testObject.setInvalidData(-1);

        for (int i = 0; i < testObject.getSize() / 2; i++) {
            testObject.set(i, (byte) (testObject.getByte(i) + 50));
        }

        for (int i = 0; i < testObject.getNumRows(); i++) {
            for (int j = 0; j < testObject.getNumCols() / 2; j++) {
                testObject.set(i, j, 0, (byte) (testObject.getByte(i) + 50));
            }
        }
        // add georeferencing information
        edu.illinois.ncsa.isda.im2learn.core.geo.projection.Projection targetProj = (edu.illinois.ncsa.isda.im2learn.core.geo.projection.Projection) inputObject.getProperty(ImageObject.GEOINFO);
        testObject.setProperty(ImageObject.GEOINFO, targetProj);

        // save out the file
        ImageLoader.writeImage(OutFileName, testObject);

    }
}
