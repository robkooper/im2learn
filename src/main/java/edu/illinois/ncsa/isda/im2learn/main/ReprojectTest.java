package edu.illinois.ncsa.isda.im2learn.main;

import java.util.HashMap;

import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.im2learn.core.geo.AngularUnit;
import edu.illinois.ncsa.isda.im2learn.core.geo.Datum;
import edu.illinois.ncsa.isda.im2learn.core.geo.GeoGraphicCoordinateSystem;
import edu.illinois.ncsa.isda.im2learn.core.geo.GeoUtilities;
import edu.illinois.ncsa.isda.im2learn.core.geo.LinearUnit;
import edu.illinois.ncsa.isda.im2learn.core.geo.ModelPoint;
import edu.illinois.ncsa.isda.im2learn.core.geo.PrimeMeridian;
import edu.illinois.ncsa.isda.im2learn.core.geo.Projection;
import edu.illinois.ncsa.isda.im2learn.core.geo.ProjectionConvert;
import edu.illinois.ncsa.isda.im2learn.core.geo.RasterPoint;
import edu.illinois.ncsa.isda.im2learn.core.geo.TiePoint;
import edu.illinois.ncsa.isda.im2learn.core.geo.Projection.ProjectionType;
import edu.illinois.ncsa.isda.im2learn.core.io.ImageLoader;


public class ReprojectTest {
	static public void main(String[] args) throws Exception {
		// load the image
		String file = "/home/kooper/Desktop/N37W084";
		ImageObject imgobj = ImageLoader.readImage(file + ".tif");
		System.out.println(imgobj);

		// create the target projection

		// ///////////////////////////
		// define target projection
		/*
		 * From James According to ArcGIS, the parameters for the U.S.
		 * Contiguous Albers Equal Area Conic are as follows: False Easting: 0.0
		 * False Northing: 0.0 Central Meridian: -96.0 First Standard Parallel:
		 * 29.5 Second Standard Parallel: 45.5 Latitude of Origin: 23.0 Linear
		 * Unit: Meter (1.0) // Henrik Datum WGS 84 Projection: Albers Equal
		 * Area Standard Parallel 1 29�30' N Lat Standard Parallel 2 45�30'
		 * N Lat Central Meridian 96�W Lon Latitude of Origin 23�N Lat
		 */

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

		Projection targetProj = Projection.getProjection("Albers Equal Area Conic", ProjectionType.Albers, geogcs, param, null, unit);

		// compute tiepoint based on old tiepoint
		// get the projection from the image
		Projection imgproj = ProjectionConvert.getNewProjection(imgobj.getProperty(ImageObject.GEOINFO));
		RasterPoint rp = new RasterPoint(imgproj.getTiePoint().getRasterPoint());
		ModelPoint mp = imgproj.getTiePoint().getModelPoint();
		mp = targetProj.earthToModel(imgproj.modelToEarth(mp));
		if (targetProj.getType().equals(ProjectionType.Geographic)) {
			// 33/3600 is approx 1km in arc degrees
			// TODO we should take 3 pixels, next and above to calculate the dx
			// and dy
			// TODO should really clone targetproj first
			targetProj.setTiePoint(new TiePoint(mp, rp, 33.0 / 3600, 33.0 / 3600, 1));
		} else {
			// targetProj.setTiePoint(new TiePoint(mp, rp, 27.9354, 27.9354,
			// 1));
			targetProj.setTiePoint(new TiePoint(mp, rp, 1000, 1000, 1));
		}

		System.out.println("MODEL TEST IMAGE : " + GeoUtilities.getModelBounds(imgobj, imgproj));
		System.out.println("EARTH TEST IMAGE : " + GeoUtilities.getEarthBounds(imgobj, imgproj));

		ImageObject result = GeoUtilities.reproject(imgobj, targetProj);

		System.out.println("MODEL TEST IMAGE : " + GeoUtilities.getModelBounds(result, targetProj));
		System.out.println("EARTH TEST IMAGE : " + GeoUtilities.getEarthBounds(result, targetProj));
		System.out.println(result);

		ImageLoader.writeImage(file + "_reproject.tif", result);

		ImageLoader.readImage(file + "_reproject.tif");
	}
}
