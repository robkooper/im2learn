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
 * @author pbajcsy
 */
public class LambertConformalConic extends Projection implements Serializable {
    private static final long serialVersionUID = 1L;

    public LambertConformalConic(String name, ProjectionType type, GeoGraphicCoordinateSystem geogcs, Map<String, String> parameters,
            TiePoint tiepoint, LinearUnit unit) {
        super(name, type, geogcs, parameters, tiepoint, unit);
    }

    @Override
    public List<String> getRequiredParameters() {
        List<String> result = new ArrayList<String>();
        result.add(Projection.FALSE_EASTING);
        result.add(Projection.FALSE_NORTHING);
        result.add(Projection.CENTRAL_MERIDIAN);
        result.add(Projection.STANDARD_PARALLEL_1);
        result.add(Projection.STANDARD_PARALLEL_2);
        // TODO ArcGIS has scale factor
        // result.add(Projection.SCALE_FACTOR);
        result.add(Projection.LATITUDE_OF_ORIGIN);
        return result;
    }

    /*
     * public double getFalseEasting() { if (hasParameter(FALSE_EASTING)) { try {
     * return getParameterNumber(FALSE_EASTING); } catch (GeoException exc) {
     * log.info("Could not parse False Easting.", exc); } } return 0; }
     * 
     * public double getFalseNorthing() { if (hasParameter(FALSE_NORTHING)) {
     * try { return getParameterNumber(FALSE_NORTHING); } catch (GeoException
     * exc) { log.info("Could not parse False Northing.", exc); } } return 0; }
     */
    @Override
    public ModelPoint earthToModel(GeodeticPoint point) throws IllegalArgumentException, GeoException {
        // do datum shift
        point = new GeodeticPoint(point, getGeographicCoordinateSystem());

        double a = getGeographicCoordinateSystem().getDatum().getEllipsoid().getMajor();
        double esq = getGeographicCoordinateSystem().getDatum().getEllipsoid().getESquared();
        double e = Math.sqrt(esq);

        double phir = Math.toRadians(point.getLat());
        double sinp = Math.sin(phir);
        // double cosp = Math.cos(phir);

        double phi1r = Math.toRadians(getParameterNumber(Projection.STANDARD_PARALLEL_1));
        double sinp1 = Math.sin(phi1r);
        double cosp1 = Math.cos(phi1r);

        double phi2r = Math.toRadians(getParameterNumber(Projection.STANDARD_PARALLEL_2));
        double sinp2 = Math.sin(phi2r);
        double cosp2 = Math.cos(phi2r);

        double phi3r = Math.toRadians(getParameterNumber(Projection.LATITUDE_OF_ORIGIN));
        double sinp3 = Math.sin(phi3r);
        // double cosp3 = Math.cos(phi3r);

        double lambdar = Math.toRadians(point.getLon());
        double lambda3r = Math.toRadians(getParameterNumber(Projection.CENTRAL_MERIDIAN));

        // equation 14-15
        double m1 = cosp1 / Math.sqrt(1 - esq * sinp1 * sinp1);
        double m2 = cosp2 / Math.sqrt(1 - esq * sinp2 * sinp2);

        // equation 15-9
        double t1 = Math.tan(Math.PI * 0.25 - phi1r * 0.5) / Math.pow(((1 - e * sinp1) / (1 + e * sinp1)), (e * 0.5));
        double t2 = Math.tan(Math.PI * 0.25 - phi2r * 0.5) / Math.pow(((1 - e * sinp2) / (1 + e * sinp2)), (e * 0.5));
        double t0 = Math.tan(Math.PI * 0.25 - phi3r * 0.5) / Math.pow(((1 - e * sinp3) / (1 + e * sinp3)), (e * 0.5));

        // equation 15-8
        double n = (Math.log10(m1) - Math.log10(m2)) / (Math.log10(t1) - Math.log10(t2));
        // equation 15-10
        double F = m1 / (n * Math.pow(t1, n));
        // equation 15-7a
        double ro0 = a * F * Math.pow(t0, n);

        // equation 15-9
        double t = Math.tan(Math.PI * 0.25 - phir * 0.5) / Math.pow(((1 - e * sinp) / (1 + e * sinp)), (e * 0.5));

        // eq 15-7
        double ro = a * F * Math.pow(t, n);
        // eq 14-4
        double theta = n * (lambdar - lambda3r);
        // eq 14-1
        double x = ro * Math.sin(theta);
        // eq 14-2
        double y = ro0 - ro * Math.cos(theta);

        // return new ModelPoint(x, y);
        return new ModelPoint(x + getParameterNumber(Projection.FALSE_EASTING), y + getParameterNumber(Projection.FALSE_NORTHING));
    }

    @Override
    public GeodeticPoint modelToEarth(ModelPoint point) throws IllegalArgumentException, GeoException {
        double x = point.getX() - getParameterNumber(Projection.FALSE_EASTING);
        double y = point.getY() - getParameterNumber(Projection.FALSE_NORTHING);

        double a = getGeographicCoordinateSystem().getDatum().getEllipsoid().getMajor();
        double esq = getGeographicCoordinateSystem().getDatum().getEllipsoid().getESquared();
        double e = Math.sqrt(esq);

        double phi1r = Math.toRadians(getParameterNumber(Projection.STANDARD_PARALLEL_1));
        double sinp1 = Math.sin(phi1r);
        double cosp1 = Math.cos(phi1r);

        double phi2r = Math.toRadians(getParameterNumber(Projection.STANDARD_PARALLEL_2));
        double sinp2 = Math.sin(phi2r);
        double cosp2 = Math.cos(phi2r);

        double phi3r = Math.toRadians(getParameterNumber(Projection.LATITUDE_OF_ORIGIN));
        double sinp3 = Math.sin(phi3r);
        // double cosp3 = Math.cos(phi3r);

        // equation 14-15
        double m1 = cosp1 / Math.sqrt(1 - esq * sinp1 * sinp1);
        double m2 = cosp2 / Math.sqrt(1 - esq * sinp2 * sinp2);

        // equation 15-9
        double t1 = Math.tan(Math.PI * 0.25 - phi1r * 0.5) / Math.pow(((1 - e * sinp1) / (1 + e * sinp1)), (e * 0.5));
        double t2 = Math.tan(Math.PI * 0.25 - phi2r * 0.5) / Math.pow(((1 - e * sinp2) / (1 + e * sinp2)), (e * 0.5));
        double t0 = Math.tan(Math.PI * 0.25 - phi3r * 0.5) / Math.pow(((1 - e * sinp3) / (1 + e * sinp3)), (e * 0.5));

        // equation 15-8
        double n = (Math.log10(m1) - Math.log10(m2)) / (Math.log10(t1) - Math.log10(t2));
        // equation 15-10
        double F = m1 / (n * Math.pow(t1, n));
        // equation 15-7a
        double ro0 = a * F * Math.pow(t0, n);

        // equation 14-10
        double ro = Math.signum(n) * Math.sqrt(x * x + (ro0 - y) * (ro0 - y));

        // eq 14-11
        double theta = Math.atan(x / (ro0 - y));

        // eq 15-11
        double t = Math.pow((ro / (a * F)), (1 / n));

        // eq 7-9 replaced by iterations in 7 -13 and 3-5
        double xi = Math.PI * 0.5 - 2 * Math.atan(t);

        double phiInit = xi;// + (esq * 0.5 + 5 * esq*esq/ 24 + esq*esq*esq / 12
        // + 13 * esq *esq *esq *esq/360 ) * Math.sin( 2*
        // xi) +
        // (7 * esq * esq/48 + 29 * esq *esq * esq/240 + 811 *
        // esq*esq*esq*esq/11520) + Math.sin( 4 * xi)+
        // (7 * esq * esq*esq/120 + 81* esq*esq*esq*esq/1120 ) * Math.sin(6 *
        // xi) +
        // (4279 * esq*esq*esq*esq/161280) * Math.sin(8 * xi);

        double phi = 0.0;
        for (int i = 1; i < 5; i++) {
            phi = Math.PI * 0.5 - 2 * Math.atan(t * Math.pow((1 - e * Math.sin(phiInit)) / (1 + e * Math.sin(phiInit)), e * 0.5));
            phiInit = phi;
        }
        // phi is in radians

        // eq 14-9 (in degrees)
        double lambda = Math.toDegrees(theta) / n + getParameterNumber(Projection.CENTRAL_MERIDIAN);

        return new GeodeticPoint(Math.toDegrees(phi), lambda, getGeographicCoordinateSystem());

    }
}
