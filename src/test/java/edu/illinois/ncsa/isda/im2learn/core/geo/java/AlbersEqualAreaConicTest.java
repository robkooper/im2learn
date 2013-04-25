package edu.illinois.ncsa.isda.imagetools.core.geo.java;

import java.util.HashMap;

import edu.illinois.ncsa.isda.imagetools.core.geo.Datum;
import edu.illinois.ncsa.isda.imagetools.core.geo.GeoGraphicCoordinateSystem;
import edu.illinois.ncsa.isda.imagetools.core.geo.GeodeticPoint;
import edu.illinois.ncsa.isda.imagetools.core.geo.ModelPoint;
import edu.illinois.ncsa.isda.imagetools.core.geo.Projection;
import edu.illinois.ncsa.isda.imagetools.core.geo.Projection.ProjectionType;

import junit.framework.TestCase;

public class AlbersEqualAreaConicTest extends TestCase {

	private Projection					proj;
	private GeoGraphicCoordinateSystem	clarke1866;

	public AlbersEqualAreaConicTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(Projection.FALSE_EASTING, "0.0");
		map.put(Projection.FALSE_NORTHING, "0.0");
		map.put(Projection.STANDARD_PARALLEL_1, "29.5");
		map.put(Projection.STANDARD_PARALLEL_2, "45.5");
		map.put(Projection.LATITUDE_OF_ORIGIN, "23.0");
		map.put(Projection.CENTRAL_MERIDIAN, "-96.0");

		clarke1866 = new GeoGraphicCoordinateSystem(Datum.North_American_1927_Conus);
		proj = Projection.getProjection(ProjectionType.Albers, clarke1866, map);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testModelToEarth() throws Exception {
		ModelPoint mp = new ModelPoint(1885472.7, 1535925.0);
		GeodeticPoint gp = proj.modelToEarth(mp);

		assertEquals(35, gp.getLat(), 0.001);
		assertEquals(-75, gp.getLon(), 0.001);
	}

	public void testEarthToModel() throws Exception {
		GeodeticPoint gp = new GeodeticPoint(35, -75, clarke1866);
		ModelPoint mp = proj.earthToModel(gp);

		assertEquals(1885472.7, mp.getX(), 0.1);
		assertEquals(1535925.0, mp.getY(), 0.1);
	}

}
