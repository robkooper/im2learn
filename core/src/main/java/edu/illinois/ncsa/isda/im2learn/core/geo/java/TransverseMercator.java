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


public class TransverseMercator extends Projection implements Serializable {
    private static final long serialVersionUID = 1L;

    static public int computeZone(double longitude) {
        return (int) Math.ceil((183 + longitude) / 6.0);
    }

    static public double computeLongitude(int zone) {
        return (zone * 6.0) - 183;
    }

    public TransverseMercator(String name, ProjectionType type, GeoGraphicCoordinateSystem geogcs, Map<String, String> parameters, TiePoint tiepoint,
            LinearUnit unit) {
        super(name, type, geogcs, parameters, tiepoint, unit);
    }

    @Override
    public List<String> getRequiredParameters() {
        List<String> result = new ArrayList<String>();
        result.add(Projection.FALSE_EASTING);
        result.add(Projection.FALSE_NORTHING);
        result.add(Projection.LONGITUDE_OF_CENTER);
        result.add(Projection.LATITUDE_OF_CENTER);
        result.add(Projection.SCALE_FACTOR);
        return result;
    }

    @Override
    public ModelPoint earthToModel(GeodeticPoint point) throws GeoException {
        // do datum shift
        point = new GeodeticPoint(point, getGeographicCoordinateSystem());

        double phir = Math.toRadians(point.getLat());
        // TODO if phir = +/- PI/2 then
        // x = 0; y = k0(M - M0) (for m and m0 see 3-21)

        double tanp = Math.tan(phir);
        double cosp = Math.cos(phir);
        double sinp = Math.sin(phir);

        double a = getGeographicCoordinateSystem().getDatum().getEllipsoid().getMajor();
        double esq = getGeographicCoordinateSystem().getDatum().getEllipsoid().getESquared();
        double k0 = getParameterNumber(Projection.SCALE_FACTOR);
        double esq2 = esq * esq;
        double esq3 = esq * esq2;

        // equation 3-21 for phi
        double M1 = (1 - esq / 4 - 3 * esq2 / 64 - 5 * esq3 / 256) * phir;
        double M2 = (3 * esq / 8 + 3 * esq2 / 32 + 45 * esq3 / 1024) * Math.sin(2 * phir);
        double M3 = (15 * esq2 / 256 + 45 * esq3 / 1024) * Math.sin(4 * phir);
        double M4 = (35 * esq3 / 3072) * Math.sin(6 * phir);
        double M = a * (M1 - M2 + M3 - M4);

        // equation 3-21 for phi0
        double M0 = 0;
        if (getParameterNumber(Projection.LATITUDE_OF_CENTER) != 0) {
            M1 = (1 - esq / 4 - 3 * esq2 / 64 - 5 * esq3 / 256) * phir;
            M2 = (3 * esq / 8 + 3 * esq2 / 32 + 45 * esq3 / 1024) * Math.sin(2 * phir);
            M3 = (15 * esq2 / 256 + 45 * esq3 / 1024) * Math.sin(4 * phir);
            M4 = (35 * esq3 / 3072) * Math.sin(6 * phir);
            M0 = a * (M1 - M2 + M3 - M4);
        }

        // equation 4-20
        double N = a / Math.sqrt(1 - esq * sinp * sinp);

        // equation 8-12
        double etsq = esq / (1 - esq);

        // equation 8-13
        double T = tanp * tanp;

        // equation 8-14
        double C = etsq * cosp * cosp;

        // equation 8-15
        double A = Math.toRadians(point.getLon() - getParameterNumber(Projection.LONGITUDE_OF_CENTER)) * cosp;
        double A2 = A * A;
        double A3 = A * A2;
        double A4 = A * A3;
        double A5 = A * A4;
        double A6 = A * A5;

        // equation 8-9
        double x = k0 * N * (A + (1 - T + C) * A3 / 6 + (5 - 18 * T * T + 72 * C - 58 * etsq) * A5 / 120);

        // equation 8-10
        double y = k0
                * (M - M0 + N * tanp * (A2 / 2 + (5 - T + 9 * C + 4 * C * C) * A4 / 24 + (61 - 58 * T + T * T + 600 * C - 330 * etsq) * A6 / 720));

        return new ModelPoint(x + getParameterNumber(Projection.FALSE_EASTING), y + getParameterNumber(Projection.FALSE_NORTHING));
    }

    @Override
    public GeodeticPoint modelToEarth(ModelPoint point) throws GeoException {
        double x = point.getX() - getParameterNumber(Projection.FALSE_EASTING);
        double y = point.getY() - getParameterNumber(Projection.FALSE_NORTHING);
        double a = getGeographicCoordinateSystem().getDatum().getEllipsoid().getMajor();
        double esq = getGeographicCoordinateSystem().getDatum().getEllipsoid().getESquared();
        double lambda0 = getParameterNumber(Projection.LONGITUDE_OF_CENTER);
        double k0 = getParameterNumber(Projection.SCALE_FACTOR);
        double esq2 = esq * esq;
        double esq3 = esq * esq2;

        // equation 3-21 for phi0
        double M0 = 0;
        double phir = getParameterNumber(Projection.LATITUDE_OF_CENTER);
        if (phir != 0) {
            double M1 = (1 - esq / 4 - 3 * esq2 / 64 - 5 * esq3 / 256) * phir;
            double M2 = (3 * esq / 8 + 3 * esq2 / 32 + 45 * esq3 / 1024) * Math.sin(2 * phir);
            double M3 = (15 * esq2 / 256 + 45 * esq3 / 1024) * Math.sin(4 * phir);
            double M4 = (35 * esq3 / 3072) * Math.sin(6 * phir);
            M0 = a * (M1 - M2 + M3 - M4);
        }

        // equation 8-12
        double etsq = esq / (1 - esq);

        // equation 8-20
        double M = M0 + y / k0;

        // equation 3-24
        double t = Math.sqrt(1 - esq);
        double e1 = (1 - t) / (1 + t);
        double e12 = e1 * e1;
        double e13 = e1 * e12;
        double e14 = e1 * e13;

        // equation 7-19
        double mu = M / (a * (1 - esq / 4 - 3 * esq2 / 64 - 5 * esq3 / 256));

        // equation 3-26
        double p1 = (3 * e1 / 2 - 27 * e13 / 32) * Math.sin(2 * mu);
        double p2 = (21 * e12 / 16 - 55 * e14 / 32) * Math.sin(4 * mu);
        double p3 = (151 * e13 / 96) * Math.sin(6 * mu);
        double p4 = (1097 * e14 / 512) * Math.sin(8 * mu);
        double phi1 = mu + p1 + p2 + p3 + p4;
        double sinp = Math.sin(phi1);
        double cosp = Math.cos(phi1);
        double tanp = Math.tan(phi1);

        // equation 8-21
        double C1 = etsq * cosp * cosp;
        double C12 = C1 * C1;

        // equation 8-22
        double T1 = tanp * tanp;
        double T12 = T1 * T1;

        // equation 8-23
        double N1 = a / Math.sqrt(1 - esq * sinp * sinp);

        // equation 8-24
        double R1 = a * (1 - esq) / Math.pow((1 - esq * sinp * sinp), 1.5);

        // equation 8-25
        double D = x / (N1 * k0);
        double D2 = D * D;
        double D3 = D * D2;
        double D4 = D * D3;
        double D5 = D * D4;
        double D6 = D * D5;

        // equation 8-17
        double phi = phi1
                - (N1 * tanp / R1)
                * (D2 / 2 - (5 + 3 * T1 + 10 * C1 - 4 * C12 - 9 * etsq) * D4 / 24 + (61 + 90 * T1 + 298 * C1 + 45 * T12 - 252 * etsq - 3 * C12) * D6
                        / 720);

        // equation 8-18
        double lambda = lambda0
                + Math.toDegrees((D - (1 + 2 * T1 + C1) * D3 / 6 + (5 - 2 * C1 + 28 * T1 - 3 * C12 + 8 * etsq + 24 * T12) * D5 / 120) / cosp);

        return new GeodeticPoint(Math.toDegrees(phi), lambda, getGeographicCoordinateSystem());
    }
}
