package edu.illinois.ncsa.isda.im2learn.core.geo.java;

import java.util.HashMap;

import edu.illinois.ncsa.isda.im2learn.core.geo.Datum;
import edu.illinois.ncsa.isda.im2learn.core.geo.GeoGraphicCoordinateSystem;
import edu.illinois.ncsa.isda.im2learn.core.geo.GeodeticPoint;
import edu.illinois.ncsa.isda.im2learn.core.geo.ModelPoint;
import edu.illinois.ncsa.isda.im2learn.core.geo.Projection;
import edu.illinois.ncsa.isda.im2learn.core.geo.Projection.ProjectionType;

import junit.framework.TestCase;

public class TransverseMercatorTest extends TestCase {

	private Projection					proj;
	private GeoGraphicCoordinateSystem	clarke1866;

	public TransverseMercatorTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(Projection.FALSE_EASTING, "0");
		map.put(Projection.FALSE_NORTHING, "0");
		map.put(Projection.LATITUDE_OF_CENTER, "0");
		map.put(Projection.LONGITUDE_OF_CENTER, "-75");
		map.put(Projection.SCALE_FACTOR, "0.9996");

		clarke1866 = new GeoGraphicCoordinateSystem(Datum.North_American_1927_Conus);
		proj = Projection.getProjection(ProjectionType.Transverse_Mercator, clarke1866, map);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testModelToEarth() throws Exception {
		ModelPoint mp = new ModelPoint(127106.4, 4484124.4);
		GeodeticPoint gp = proj.modelToEarth(mp);

		assertEquals(40.5, gp.getLat(), 0.001);
		assertEquals(-73.5, gp.getLon(), 0.001);
	}

	public void testEarthToModel() throws Exception {
		GeodeticPoint gp = new GeodeticPoint(40.5, -73.5, clarke1866);
		ModelPoint mp = proj.earthToModel(gp);

		assertEquals(127106.4, mp.getX(), 0.1);
		assertEquals(4484124.4, mp.getY(), 0.1);
	}

}
