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
import edu.illinois.ncsa.isda.imagetools.core.geo.projection.NewAlbersEqualAreaConic;
import junit.framework.TestCase;

public class AlbersEqualAreaConicTest extends TestCase {

	private NewAlbersEqualAreaConic model;
	
	public AlbersEqualAreaConicTest(String arg0) {
		super(arg0);
	}

	protected void setUp() throws Exception {
		super.setUp();		
		//test forward
		//using clarke1866 ellipsoid:
		double falseEasting=0.0;
		double falseNorthing=0.0;
		double firstParallel = 29.5;
		double secondParallel = 45.5;
		double centerLatitude = 23.0;
		double centerLongitude = -96.0;
		double majorRadius = 6378206.400000000;
		double minorRadius = 6356583.80;
		
		
		model=new NewAlbersEqualAreaConic(majorRadius,
				minorRadius, firstParallel,
				secondParallel, centerLongitude, centerLatitude,
				falseEasting,  falseNorthing);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		model = null;
	}
	public void testEarthToModel() throws Exception {
		double[] latlon = new double[] {-75.0, 35.0};
		Datum NAD27 = Datum.North_American_1927_Conus;
		Datum WGS84 = Datum.WGS_1984;
		GeoGraphicCoordinateSystem gcsNAD27 = new GeoGraphicCoordinateSystem(NAD27);
		GeoGraphicCoordinateSystem gcsWGS84 = new GeoGraphicCoordinateSystem(WGS84);
		GeodeticPoint input = new GeodeticPoint(latlon[0], latlon[1], gcsWGS84);
		input.setGeographicCoordinateSystem(gcsNAD27);
		latlon[0]=input.getLat();
		latlon[1]=input.getLon();
		double[] result = model.earthToModel(latlon);
		assertEquals("Latitude", 1885472.7, result[0], 1e-4);
		assertEquals("Longitude", 1535925.0, result[1], 1e-4);
	}

	public void testModelToEarth() throws Exception {
		Datum NAD27 = Datum.North_American_1927_Conus;
		Datum WGS84 = Datum.WGS_1984;
		GeoGraphicCoordinateSystem gcsNAD27 = new GeoGraphicCoordinateSystem(NAD27);
		GeoGraphicCoordinateSystem gcsWGS84 = new GeoGraphicCoordinateSystem(WGS84);
		double[] xy = new double[] {1885472.7, 1535925.0 };
		double[] result = model.modelToEarth(xy);
		GeodeticPoint output = new GeodeticPoint(result[0], result[1], gcsWGS84);
		output.setGeographicCoordinateSystem(gcsNAD27);
		
		assertEquals("X", -75, output.getLat(), 1e-4);
		assertEquals("Y", -35, output.getLon(), 1e-4);
	}
}
