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
package edu.illinois.ncsa.isda.im2learn.core.geo.java;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.illinois.ncsa.isda.im2learn.core.geo.GeoException;
import edu.illinois.ncsa.isda.im2learn.core.geo.GeoGraphicCoordinateSystem;
import edu.illinois.ncsa.isda.im2learn.core.geo.GeodeticPoint;
import edu.illinois.ncsa.isda.im2learn.core.geo.LinearUnit;
import edu.illinois.ncsa.isda.im2learn.core.geo.ModelPoint;
import edu.illinois.ncsa.isda.im2learn.core.geo.Projection;
import edu.illinois.ncsa.isda.im2learn.core.geo.TiePoint;


public class Mercator extends Projection implements Serializable {
    private static final long serialVersionUID = 1L;

    public Mercator(String name, ProjectionType type, GeoGraphicCoordinateSystem geogcs, Map<String, String> parameters, TiePoint tiepoint,
            LinearUnit unit) {
        super(name, type, geogcs, parameters, tiepoint, unit);
    }

    @Override
    public List<String> getRequiredParameters() {
        List<String> result = new ArrayList<String>();
        result.add(Projection.FALSE_EASTING);
        result.add(Projection.FALSE_NORTHING);
        result.add(Projection.LONGITUDE_OF_CENTER);
        return result;
    }

    @Override
    public ModelPoint earthToModel(GeodeticPoint point) throws GeoException {
        // do datum shift
        point = new GeodeticPoint(point, getGeographicCoordinateSystem());

        double r = getGeographicCoordinateSystem().getDatum().getEllipsoid().getMajor();

        double x = r * Math.toRadians(point.getLon() - getParameterNumber(Projection.LONGITUDE_OF_CENTER));
        double y = r * Math.log(Math.tan(Math.toRadians(45.0 + point.getLat() / 2.0)));

        return new ModelPoint(x + getParameterNumber(Projection.FALSE_EASTING), y + getParameterNumber(Projection.FALSE_NORTHING));
    }

    @Override
    public GeodeticPoint modelToEarth(ModelPoint point) throws GeoException {
        double x = point.getX() - getParameterNumber(Projection.FALSE_EASTING);
        double y = point.getY() - getParameterNumber(Projection.FALSE_NORTHING);

        double r = getGeographicCoordinateSystem().getDatum().getEllipsoid().getMajor();

        double lambda = Math.toDegrees(x / r) + getParameterNumber(Projection.LONGITUDE_OF_CENTER);
        double phi = Math.atan(Math.sinh(y / r));

        return new GeodeticPoint(Math.toDegrees(phi), lambda, getGeographicCoordinateSystem());
    }
}
