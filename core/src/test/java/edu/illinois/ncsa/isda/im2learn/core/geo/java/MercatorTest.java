package edu.illinois.ncsa.isda.im2learn.core.geo.java;

import java.util.HashMap;

import edu.illinois.ncsa.isda.im2learn.core.geo.Datum;
import edu.illinois.ncsa.isda.im2learn.core.geo.Ellipsoid;
import edu.illinois.ncsa.isda.im2learn.core.geo.GeoGraphicCoordinateSystem;
import edu.illinois.ncsa.isda.im2learn.core.geo.GeodeticPoint;
import edu.illinois.ncsa.isda.im2learn.core.geo.ModelPoint;
import edu.illinois.ncsa.isda.im2learn.core.geo.Projection;
import edu.illinois.ncsa.isda.im2learn.core.geo.Projection.ProjectionType;

import junit.framework.TestCase;

public class MercatorTest extends TestCase {

	private Projection					proj;
	private GeoGraphicCoordinateSystem	unitsphere;

	public MercatorTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(Projection.FALSE_EASTING, "0");
		map.put(Projection.FALSE_NORTHING, "0");
		map.put(Projection.LONGITUDE_OF_CENTER, "-180");

		unitsphere = new GeoGraphicCoordinateSystem(new Datum(new Ellipsoid(1, Double.POSITIVE_INFINITY)));
		proj = Projection.getProjection(ProjectionType.Mercator, unitsphere, map);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testModelToEarth() throws Exception {
		ModelPoint mp = new ModelPoint(1.8325957, 0.6528366);
		GeodeticPoint gp = proj.modelToEarth(mp);

		assertEquals(35, gp.getLat(), 1e-6);
		assertEquals(-75, gp.getLon(), 1e-6);
	}

	public void testEarthToModel() throws Exception {
		GeodeticPoint gp = new GeodeticPoint(35, -75, unitsphere);
		ModelPoint mp = proj.earthToModel(gp);

		assertEquals(1.8325957, mp.getX(), 1e-6);
		assertEquals(0.6528366, mp.getY(), 1e-6);
	}

}
