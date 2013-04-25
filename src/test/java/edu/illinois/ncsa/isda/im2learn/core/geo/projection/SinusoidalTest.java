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
package edu.illinois.ncsa.isda.imagetools.core.geo.projection;

import edu.illinois.ncsa.isda.imagetools.core.geo.Datum;
import edu.illinois.ncsa.isda.imagetools.core.geo.GeoGraphicCoordinateSystem;
import edu.illinois.ncsa.isda.imagetools.core.geo.GeodeticPoint;
import edu.illinois.ncsa.isda.imagetools.core.geo.projection.NewSinusoidal;
import junit.framework.TestCase;

public class SinusoidalTest extends TestCase {

	private NewSinusoidal	model;

	public SinusoidalTest(String arg0) {
		super(arg0);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		// test forward
		// using clarke1866 ellipsoid:
		double majorRadius = 6378206.400000000;
		double minorRadius = 6356583.80;
		double centerLongitude = -90.0;
		double falseEasting = 0.0;
		double falseNorthing = 0.0;

		model = new NewSinusoidal(majorRadius, minorRadius, centerLongitude, falseEasting, falseNorthing);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		model = null;
	}

	public void testEarthToModel() throws Exception {
		GeoGraphicCoordinateSystem NAD27 = new GeoGraphicCoordinateSystem(Datum.North_American_1927_Conus);
		GeoGraphicCoordinateSystem WGS84 = new GeoGraphicCoordinateSystem(Datum.WGS_1984);
		GeodeticPoint input = new GeodeticPoint(50, -75, NAD27);
		input.setGeographicCoordinateSystem(WGS84);
		double[] latlon = { input.getLat(), input.getLon() };
		// System.out.println("input lat: "+latlon[0]);
		// System.out.println("input lon: "+latlon[1]);

		double[] result = model.earthToModel(latlon);
		System.out.println("result[0]: " + result[0]);
		System.out.println("result[1]: " + result[1]);
		assertEquals("Latitude", 1075471.5382, result[0], 1e-4);
		assertEquals("Longitude", 6254742.26854, result[1], 1e-4);
	}

	public void testModelToEarth() {
		double[] xy = new double[] { 1075471.5, 6254742.26854 };
		double[] result = model.modelToEarth(xy);
		Datum NAD27 = Datum.North_American_1927_Conus;
		Datum WGS84 = Datum.WGS_1984;
		GeoGraphicCoordinateSystem gcsNAD27 = new GeoGraphicCoordinateSystem(NAD27);
		GeoGraphicCoordinateSystem gcsWGS84 = new GeoGraphicCoordinateSystem(WGS84);
		GeodeticPoint output = new GeodeticPoint(result[0], result[1], gcsWGS84);
		output.setGeographicCoordinateSystem(gcsNAD27);
		assertEquals("X", 49.99998802349366, result[0], 1e-4);
		assertEquals("Y", -74.99953020178458, result[1], 1e-4);
	}
}
