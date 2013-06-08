package edu.illinois.ncsa.isda.im2learn.core.geo.java;

import java.util.HashMap;

import edu.illinois.ncsa.isda.im2learn.core.geo.Datum;
import edu.illinois.ncsa.isda.im2learn.core.geo.GeoGraphicCoordinateSystem;
import edu.illinois.ncsa.isda.im2learn.core.geo.GeodeticPoint;
import edu.illinois.ncsa.isda.im2learn.core.geo.ModelPoint;
import edu.illinois.ncsa.isda.im2learn.core.geo.Projection;
import edu.illinois.ncsa.isda.im2learn.core.geo.Projection.ProjectionType;

import junit.framework.TestCase;

public class LambertAzimuthalEqualAreaTest extends TestCase {

	private Projection					proj;
	private GeoGraphicCoordinateSystem	clarke1866;

	public LambertAzimuthalEqualAreaTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(Projection.FALSE_EASTING, "0");
		map.put(Projection.FALSE_NORTHING, "0");
		map.put(Projection.LATITUDE_OF_ORIGIN, "40");
		map.put(Projection.CENTRAL_MERIDIAN, "-100");

		clarke1866 = new GeoGraphicCoordinateSystem(Datum.North_American_1927_Conus);
		proj = Projection.getProjection(ProjectionType.Lambert_Azimuthal_Equal_Area, clarke1866, map);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testModelToEarth() throws Exception {
		ModelPoint mp = new ModelPoint(-965932.1, -1056814.9);
		GeodeticPoint gp = proj.modelToEarth(mp);

		assertEquals(30, gp.getLat(), 0.001);
		assertEquals(-110, gp.getLon(), 0.001);
	}

	public void testEarthToModel() throws Exception {
		GeodeticPoint gp = new GeodeticPoint(30, -110, clarke1866);
		ModelPoint mp = proj.earthToModel(gp);

		assertEquals(-965932.1, mp.getX(), 0.1);
		assertEquals(-1056814.9, mp.getY(), 0.1);
	}

}
