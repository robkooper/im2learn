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
package edu.illinois.ncsa.isda.im2learn.core.geo.projection;

import edu.illinois.ncsa.isda.im2learn.core.geo.Datum;
import edu.illinois.ncsa.isda.im2learn.core.geo.GeoGraphicCoordinateSystem;
import edu.illinois.ncsa.isda.im2learn.core.geo.GeodeticPoint;
import edu.illinois.ncsa.isda.im2learn.core.geo.projection.TransverseMercator;
import junit.framework.TestCase;

public class TransverseMercatorTest extends TestCase {

	private TransverseMercator model;
	
	public TransverseMercatorTest(String arg0) {
		super(arg0);
	}

	protected void setUp() throws Exception {
		super.setUp();		
		double majorRadius = 6378206.400000000;
		double minorRadius = 6356583.80;
		double centerLongitude = -75.0;
		double centerLatitude = 0.0;
		double scaleFactor = .9996;
		double falseEasting = 0.0;
		double falseNorthing = 0.0;
		model = new TransverseMercator(majorRadius, minorRadius, scaleFactor,
				centerLongitude, centerLatitude, falseEasting, falseNorthing);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		model = null;
	}
	
	public void testEarthToModel() throws Exception {
		Datum NAD27 = Datum.North_American_1927_Conus;
		Datum WGS84 = Datum.WGS_1984;
		GeoGraphicCoordinateSystem gcsNAD27 = new GeoGraphicCoordinateSystem(NAD27);
		GeoGraphicCoordinateSystem gcsWGS84 = new GeoGraphicCoordinateSystem(WGS84);
		GeodeticPoint input = new GeodeticPoint(-40.5, -73.5, gcsNAD27);
		input.setGeographicCoordinateSystem(gcsWGS84);
		double[] latlon = new double[] {input.getLat(), input.getLon()};
		double[] result = model.earthToModel(latlon);
		//System.out.println("result[0]"+result[0]);
		//System.out.println("result[1]"+result[1]);
		assertEquals("Latitude", 127106.5, result[0], 1e-4);
		assertEquals("Longitude", 4484124.4, result[1], 1e-4);
	}

	public void testModelToEarth() throws Exception {
		double[] xy = new double[] { 127106.5, 4484124.4 };
		double[] result = model.modelToEarth(xy);
		//System.out.println("result[0]"+result[0]);
		//System.out.println("result[1]"+result[1]);
		Datum NAD27 = Datum.North_American_1927_Conus;
		Datum WGS84 = Datum.WGS_1984;
		GeoGraphicCoordinateSystem gcsNAD27 = new GeoGraphicCoordinateSystem(NAD27);
		GeoGraphicCoordinateSystem gcsWGS84 = new GeoGraphicCoordinateSystem(WGS84);
		GeodeticPoint outPut = new GeodeticPoint(result[0], result[1], gcsWGS84);
		outPut.setGeographicCoordinateSystem(gcsNAD27);
		assertEquals("X", -40.5, result[0], 1e-4);
		assertEquals("Y", -73.5, result[1], 1e-4);
	}
}
