package edu.illinois.ncsa.isda.imagetools.core.geo.java;

import java.util.HashMap;

import edu.illinois.ncsa.isda.imagetools.core.geo.Datum;
import edu.illinois.ncsa.isda.imagetools.core.geo.GeoGraphicCoordinateSystem;
import edu.illinois.ncsa.isda.imagetools.core.geo.GeodeticPoint;
import edu.illinois.ncsa.isda.imagetools.core.geo.ModelPoint;
import edu.illinois.ncsa.isda.imagetools.core.geo.Projection;
import edu.illinois.ncsa.isda.imagetools.core.geo.Projection.ProjectionType;

import junit.framework.TestCase;

public class SinusoidalTest extends TestCase {

	private Projection					proj;
	private GeoGraphicCoordinateSystem	clarke1866;

	public SinusoidalTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(Projection.CENTRAL_MERIDIAN, "-90");

		clarke1866 = new GeoGraphicCoordinateSystem(Datum.North_American_1927_Conus);
		proj = Projection.getProjection(ProjectionType.Sinusoidal, clarke1866, map);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testModelToEarth() throws Exception {
		ModelPoint mp = new ModelPoint(1075471.5, -5540628.0);
		GeodeticPoint gp = proj.modelToEarth(mp);

		assertEquals(-50, gp.getLat(), 0.001);
		assertEquals(-75, gp.getLon(), 0.001);
	}

	public void testEarthToModel() throws Exception {
		GeodeticPoint gp = new GeodeticPoint(-50, -75, clarke1866);
		ModelPoint mp = proj.earthToModel(gp);

		assertEquals(1075471.5, mp.getX(), 0.1);
		assertEquals(-5540628.0, mp.getY(), 0.1);
	}

}
