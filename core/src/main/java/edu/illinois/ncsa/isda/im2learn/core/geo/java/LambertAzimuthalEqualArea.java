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


/**
 * 
 * @author talumbau
 * @author clutter
 */
public class LambertAzimuthalEqualArea extends Projection implements Serializable {
    private static final long serialVersionUID = 1L;

    public LambertAzimuthalEqualArea(String name, ProjectionType type, GeoGraphicCoordinateSystem geogcs, Map<String, String> parameters,
            TiePoint tiepoint, LinearUnit unit) {
        super(name, type, geogcs, parameters, tiepoint, unit);
    }

    @Override
    public List<String> getRequiredParameters() {
        List<String> result = new ArrayList<String>();
        result.add(Projection.FALSE_EASTING);
        result.add(Projection.FALSE_NORTHING);
        result.add(Projection.CENTRAL_MERIDIAN);
        result.add(Projection.LATITUDE_OF_ORIGIN);
        return result;
    }

    @Override
    public ModelPoint earthToModel(GeodeticPoint point) throws IllegalArgumentException, GeoException {
        // do datum shift
        point = new GeodeticPoint(point, getGeographicCoordinateSystem());

        double a = getGeographicCoordinateSystem().getDatum().getEllipsoid().getMajor();
        double esq = getGeographicCoordinateSystem().getDatum().getEllipsoid().getESquared();
        double e = Math.sqrt(esq);

        double phir = Math.toRadians(point.getLat());
        double sinp = Math.sin(phir);

        double phi1r = Math.toRadians(getParameterNumber(Projection.LATITUDE_OF_ORIGIN));
        double sinp1 = Math.sin(phi1r);
        double cosp1 = Math.cos(phi1r);

        double ld = Math.toRadians(point.getLon() - getParameterNumber(Projection.CENTRAL_MERIDIAN));
        double cosld = Math.cos(ld);
        double sinld = Math.sin(ld);

        // equation 3-12
        double q = (1 - esq) * (sinp / (1 - esq * sinp * sinp) - (1 / (2 * e)) * Math.log((1 - e * sinp) / (1 + e * sinp)));

        // equation 3-12
        double t = sinp1;
        double q1 = (1 - esq) * (t / (1 - esq * t * t) - (1 / (2 * e)) * Math.log((1 - e * t) / (1 + e * t)));

        // equation 3-12
        t = Math.sin(Math.toRadians(90));
        double qp = (1 - esq) * (t / (1 - esq * t * t) - (1 / (2 * e)) * Math.log((1 - e * t) / (1 + e * t)));

        // equation 3-11
        double beta = Math.asin(q / qp);
        double sinb = Math.sin(beta);
        double cosb = Math.cos(beta);

        // equation 3-11
        double beta1 = Math.asin(q1 / qp);
        double sinb1 = Math.sin(beta1);
        double cosb1 = Math.cos(beta1);

        // equation 3-13
        double Rq = a * Math.sqrt(qp / 2);

        // equation 14-15
        double m1 = cosp1 / Math.sqrt(1 - esq * sinp1 * sinp1);

        // equation 24-19
        double B = Rq * Math.sqrt(2 / (1 + sinb1 * sinb + cosb1 * cosb * cosld));

        // equation 24-20
        double D = a * m1 / (Rq * cosb1);

        // equation 24-17
        double x = B * D * cosb * sinld;

        // equation 24-18
        double y = (B / D) * (cosb1 * sinb - sinb1 * cosb * cosld);

        return new ModelPoint(x + getParameterNumber(Projection.FALSE_EASTING), y + getParameterNumber(Projection.FALSE_NORTHING));
    }

    @Override
    public GeodeticPoint modelToEarth(ModelPoint point) throws IllegalArgumentException, GeoException {
        double x = point.getX() - getParameterNumber(Projection.FALSE_EASTING);
        double y = point.getY() - getParameterNumber(Projection.FALSE_NORTHING);

        double a = getGeographicCoordinateSystem().getDatum().getEllipsoid().getMajor();
        double esq = getGeographicCoordinateSystem().getDatum().getEllipsoid().getESquared();
        double e = Math.sqrt(esq);

        double phi1r = Math.toRadians(getParameterNumber(Projection.LATITUDE_OF_ORIGIN));
        double sinp1 = Math.sin(phi1r);
        double cosp1 = Math.cos(phi1r);

        // equation 3-12
        double t = sinp1;
        double q1 = (1 - esq) * (t / (1 - esq * t * t) - (1 / (2 * e)) * Math.log((1 - e * t) / (1 + e * t)));

        // equation 3-12
        t = Math.sin(Math.toRadians(90));
        double qp = (1 - esq) * (t / (1 - esq * t * t) - (1 / (2 * e)) * Math.log((1 - e * t) / (1 + e * t)));

        // equation 3-11
        double beta1 = Math.asin(q1 / qp);
        double sinb1 = Math.sin(beta1);
        double cosb1 = Math.cos(beta1);

        // equation 3-13
        double Rq = a * Math.sqrt(qp / 2);

        // equation 14-15
        double m1 = cosp1 / Math.sqrt(1 - esq * sinp1 * sinp1);

        // equation 24-20
        double D = a * m1 / (Rq * cosb1);

        // equation 24-28
        double tx = x / D;
        double ty = D * y;
        double rho = Math.sqrt(tx * tx + ty * ty);

        // equation 24-29
        double ce = 2 * Math.asin(rho / (2 * Rq));
        double cosce = Math.cos(ce);
        double since = Math.sin(ce);

        // equation 24-27
        double q = qp * (cosce * sinb1 + D * y * since * cosb1 / rho);

        // equation 3-16 (simple)
        double phi0 = Math.asin(q / 2);

        // use convergence
        double phic = phi0;
        int iter = 0;
        do {
            phi0 = phic;
            double sinp0 = Math.sin(phi0);
            double cosp0 = Math.cos(phi0);
            // equation 3-16
            t = (1 - esq * sinp0 * sinp0);
            t = (t * t) / (2 * cosp0);
            phic = phi0 + t * (q / (1 - esq) - sinp0 / (1 - esq * sinp0 * sinp0) + (1 / (2 * e)) * Math.log((1 - e * sinp0) / (1 + e * sinp0)));
            iter++;
        } while ((iter < 10) && (Math.abs(phic - phi0) > 1e-7));

        // equation 24-26
        double lambda = Math.atan((x * since) / (D * rho * cosb1 * cosce - D * D * y * sinb1 * since));

        return new GeodeticPoint(Math.toDegrees(phic), getParameterNumber(Projection.CENTRAL_MERIDIAN) + Math.toDegrees(lambda),
                getGeographicCoordinateSystem());
    }
}
