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
package edu.illinois.ncsa.isda.imagetools.core.geo.java;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.illinois.ncsa.isda.imagetools.core.geo.GeoException;
import edu.illinois.ncsa.isda.imagetools.core.geo.GeoGraphicCoordinateSystem;
import edu.illinois.ncsa.isda.imagetools.core.geo.GeodeticPoint;
import edu.illinois.ncsa.isda.imagetools.core.geo.LinearUnit;
import edu.illinois.ncsa.isda.imagetools.core.geo.ModelPoint;
import edu.illinois.ncsa.isda.imagetools.core.geo.Projection;
import edu.illinois.ncsa.isda.imagetools.core.geo.TiePoint;


/**
 * The Sinusoidal projection Taken from Map Projections, A Working Manual pg 243
 * 
 * @TODO testing
 */
public class Sinusoidal extends Projection implements Serializable {
    private static final long serialVersionUID = 1L;

    public Sinusoidal(String name, ProjectionType type, GeoGraphicCoordinateSystem geogcs, Map<String, String> parameters, TiePoint tiepoint,
            LinearUnit unit) {
        super(name, type, geogcs, parameters, tiepoint, unit);
    }

    @Override
    public List<String> getRequiredParameters() {
        List<String> result = new ArrayList<String>();
        result.add(Projection.CENTRAL_MERIDIAN);
        return result;
    }

    @Override
    public ModelPoint earthToModel(GeodeticPoint point) throws GeoException {
        // do datum shift
        point = new GeodeticPoint(point, getGeographicCoordinateSystem());

        double lambda = point.getLon();
        double phi = point.getLat();
        double phir = Math.toRadians(phi);
        double sinp = Math.sin(phir);
        double cosp = Math.cos(phir);

        double lambda0 = getParameterNumber(Projection.CENTRAL_MERIDIAN);
        double a = getGeographicCoordinateSystem().getDatum().getEllipsoid().getMajor();
        double esq = getGeographicCoordinateSystem().getDatum().getEllipsoid().getESquared();
        double esq2 = esq * esq;
        double esq3 = esq * esq2;

        double x = a * Math.toRadians(lambda - lambda0) * cosp / Math.sqrt(1 - esq * sinp * sinp);

        // equation 3-21
        double M1 = (1 - esq / 4 - 3 * esq2 / 64 - 5 * esq3 / 256) * phir;
        double M2 = (3 * esq / 8 + 3 * esq2 / 32 + 45 * esq3 / 1024) * Math.sin(2 * phir);
        double M3 = (15 * esq2 / 256 + 45 * esq3 / 1024) * Math.sin(4 * phir);
        double M4 = (35 * esq3 / 3072) * Math.sin(6 * phir);
        double M = a * (M1 - M2 + M3 - M4);

        return new ModelPoint(x, M);
    }

    @Override
    public GeodeticPoint modelToEarth(ModelPoint point) throws GeoException {
        double lambda0 = getParameterNumber(Projection.CENTRAL_MERIDIAN);
        double a = getGeographicCoordinateSystem().getDatum().getEllipsoid().getMajor();
        double esq = getGeographicCoordinateSystem().getDatum().getEllipsoid().getESquared();
        double esq2 = esq * esq;
        double esq3 = esq * esq2;

        // equation 30-10
        double M = point.getY();

        // equation 7-19
        double mu = M / (a * (1 - esq / 4 - 3 * esq2 / 64 - 5 * esq3 / 256));

        // equation 3-24
        double t = Math.sqrt(1 - esq);
        double e1 = (1 - t) / (1 + t);
        double e12 = e1 * e1;
        double e13 = e1 * e12;
        double e14 = e1 * e13;

        // equation 3-26
        double p1 = (3 * e1 / 2 - 27 * e13 / 32) * Math.sin(2 * mu);
        double p2 = (21 * e12 / 16 - 55 * e14 / 32) * Math.sin(4 * mu);
        double p3 = (151 * e13 / 96) * Math.sin(6 * mu);
        double p4 = (1097 * e14 / 512) * Math.sin(8 * mu);
        double phi = mu + p1 + p2 + p3 + p4;
        double sinp = Math.sin(phi);
        double cosp = Math.cos(phi);

        double lambda = lambda0 + Math.toDegrees(point.getX() * Math.sqrt(1 - esq * sinp * sinp) / (a * cosp));

        return new GeodeticPoint(Math.toDegrees(phi), lambda, getGeographicCoordinateSystem());
    }
}
