package edu.illinois.ncsa.isda.im2learn.main;

import java.io.PrintStream;
import java.util.HashMap;

import edu.illinois.ncsa.isda.im2learn.core.datatype.ShapeObject;
import edu.illinois.ncsa.isda.im2learn.core.geo.AngularUnit;
import edu.illinois.ncsa.isda.im2learn.core.geo.Datum;
import edu.illinois.ncsa.isda.im2learn.core.geo.GeoException;
import edu.illinois.ncsa.isda.im2learn.core.geo.GeoGraphicCoordinateSystem;
import edu.illinois.ncsa.isda.im2learn.core.geo.GeoUtilities;
import edu.illinois.ncsa.isda.im2learn.core.geo.LinearUnit;
import edu.illinois.ncsa.isda.im2learn.core.geo.PrimeMeridian;
import edu.illinois.ncsa.isda.im2learn.core.geo.Projection;
import edu.illinois.ncsa.isda.im2learn.core.geo.Projection.ProjectionType;
import edu.illinois.ncsa.isda.im2learn.core.io.shapefile.ShapefileLoader;


public class ShapeTest {
	static private Projection getProjection() throws GeoException {
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

		return Projection.getProjection("Albers Equal Area Conic", ProjectionType.Albers, geogcs, param, null, unit);

	}

	static public void main(String[] args) throws Exception {
		String shape = "C:\\Documents and Settings\\Rob Kooper\\Desktop\\shapefile\\IL_County";

		ShapefileLoader sl = new ShapefileLoader(shape + ".shp");
		ShapeObject so = sl.getShapeObject();
		PrintStream ps = new PrintStream(shape + ".prj");
		ps.print(so.getProjection().toStringPRJ());
		ps.close();

		double[] bbox = so.GetGlobalBndBox();
		System.out.println(String.format("bbox[xmin=%f, ymin=%f, xmax=%f, ymax=%f]", bbox[0], bbox[1], bbox[2], bbox[3]));

		// reproject
		so = GeoUtilities.reproject(so, getProjection());
		bbox = so.GetGlobalBndBox();
		System.out.println(String.format("bbox[xmin=%f, ymin=%f, xmax=%f, ymax=%f]", bbox[0], bbox[1], bbox[2], bbox[3]));

		// write new shapeobject
		sl = new ShapefileLoader();
		sl.Write(shape + "_reproject.shp", so);

		// TEST
		sl = new ShapefileLoader(shape + "_reproject.shp");
		so = sl.getShapeObject();

		bbox = so.GetGlobalBndBox();
		System.out.println(String.format("bbox[xmin=%f, ymin=%f, xmax=%f, ymax=%f]", bbox[0], bbox[1], bbox[2], bbox[3]));
	}
}
