//Author: James Rapp  11/29/07

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


public class AlbersEqualAreaConic extends Projection implements Serializable {
    private static final long serialVersionUID = 1L;

    public AlbersEqualAreaConic(String name, ProjectionType type, GeoGraphicCoordinateSystem geogcs, Map<String, String> parameters,
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
        result.add(Projection.LATITUDE_OF_ORIGIN);
        return result;
    }

    @Override
    public ModelPoint earthToModel(GeodeticPoint point) throws GeoException {
        // do datum shift
        point = new GeodeticPoint(point, getGeographicCoordinateSystem());

        double a/* major radius */= getGeographicCoordinateSystem().getDatum().getEllipsoid().getMajor();
        double e2/* eccentricity squared */= getGeographicCoordinateSystem().getDatum().getEllipsoid().getESquared();
        double e/* eccentricity */= Math.sqrt(e2);
        double phi1 = Math.toRadians(getParameterNumber(Projection.STANDARD_PARALLEL_1));
        double phi2 = Math.toRadians(getParameterNumber(Projection.STANDARD_PARALLEL_2));
        double phi0 = Math.toRadians(getParameterNumber(Projection.LATITUDE_OF_ORIGIN));
        double lambda0 = getParameterNumber(Projection.CENTRAL_MERIDIAN);
        double phi = point.getLat();
        // now convert phi to radians
        phi = Math.toRadians(phi);
        double lambda = point.getLon();
        double sinPhi = Math.sin(phi);
        double sinPhi0 = Math.sin(phi0);
        double sinPhi1 = Math.sin(phi1);
        double sinPhi2 = Math.sin(phi2);

        // from equation 14-15
        double m1 = Math.cos(phi1) / Math.sqrt(1 - e2 * sinPhi1 * sinPhi1);
        double m2 = Math.cos(phi2) / Math.sqrt(1 - e2 * sinPhi2 * sinPhi2);

        // from equation 3-12
        double q1 = (1 - e2) * (sinPhi1 / (1 - e2 * sinPhi1 * sinPhi1) - (1 / (2 * e)) * Math.log((1 - e * sinPhi1) / (1 + e * sinPhi1)));
        double q2 = (1 - e2) * (sinPhi2 / (1 - e2 * sinPhi2 * sinPhi2) - (1 / (2 * e)) * Math.log((1 - e * sinPhi2) / (1 + e * sinPhi2)));
        double q0 = (1 - e2) * (sinPhi0 / (1 - e2 * sinPhi0 * sinPhi0) - (1 / (2 * e)) * Math.log((1 - e * sinPhi0) / (1 + e * sinPhi0)));

        // from equation 14-14
        double n = (m1 * m1 - m2 * m2) / (q2 - q1);

        // from equation 14-13
        double C = m1 * m1 + n * q1;

        // from equation 14-12a
        double rho0 = a * Math.sqrt(C - n * q0) / n;

        // from equation 3-12
        double q = (1 - e2) * (sinPhi / (1 - e2 * sinPhi * sinPhi) - (1 / (2 * e)) * Math.log((1 - e * sinPhi) / (1 + e * sinPhi)));

        // from equation 14-12
        double rho = a * Math.sqrt(C - n * q) / n;

        // from equation 14-4
        double theta = n * (lambda - lambda0);

        // from equation 14-1
        double x = rho * Math.sin(Math.toRadians(theta));

        // from equation 14-2
        double y = rho0 - rho * Math.cos(Math.toRadians(theta));

        // add false easting and false northing back in and return
        return new ModelPoint(x + getParameterNumber(Projection.FALSE_EASTING), y + getParameterNumber(Projection.FALSE_NORTHING));
    }

    @Override
    public GeodeticPoint modelToEarth(ModelPoint point) throws GeoException {
        // subtract falseEasting and falseNorthing
        double x = point.getX() - getParameterNumber(Projection.FALSE_EASTING);
        double y = point.getY() - getParameterNumber(Projection.FALSE_NORTHING);
        double a = getGeographicCoordinateSystem().getDatum().getEllipsoid().getMajor();
        double e2 = getGeographicCoordinateSystem().getDatum().getEllipsoid().getESquared();
        double e = Math.sqrt(e2);
        double phi1 = getParameterNumber(Projection.STANDARD_PARALLEL_1);
        double phi2 = getParameterNumber(Projection.STANDARD_PARALLEL_2);
        double phi0 = getParameterNumber(Projection.LATITUDE_OF_ORIGIN);
        double lambda0 = getParameterNumber(Projection.CENTRAL_MERIDIAN);
        double sinPhi0 = Math.sin(Math.toRadians(phi0));
        double sinPhi1 = Math.sin(Math.toRadians(phi1));
        double sinPhi2 = Math.sin(Math.toRadians(phi2));

        // from equation 14-15
        double m1 = Math.cos(Math.toRadians(phi1)) / Math.sqrt(1 - e2 * sinPhi1 * sinPhi1);
        double m2 = Math.cos(Math.toRadians(phi2)) / Math.sqrt(1 - e2 * sinPhi2 * sinPhi2);

        // from equation 3-12
        double q1 = (1 - e2) * (sinPhi1 / (1 - e2 * sinPhi1 * sinPhi1) - (1 / (2 * e)) * Math.log((1 - e * sinPhi1) / (1 + e * sinPhi1)));
        double q2 = (1 - e2) * (sinPhi2 / (1 - e2 * sinPhi2 * sinPhi2) - (1 / (2 * e)) * Math.log((1 - e * sinPhi2) / (1 + e * sinPhi2)));
        double q0 = (1 - e2) * (sinPhi0 / (1 - e2 * sinPhi0 * sinPhi0) - (1 / (2 * e)) * Math.log((1 - e * sinPhi0) / (1 + e * sinPhi0)));

        // from equation 14-14
        double n = (m1 * m1 - m2 * m2) / (q2 - q1);

        // from equation 14-13
        double C = m1 * m1 + n * q1;

        // from equation 14-12a
        double rho0 = a * Math.sqrt(C - n * q0) / n;

        // from equation 14-10
        double rho = Math.sqrt(x * x + (rho0 - y) * (rho0 - y));

        // if rho0 is negetive, theta should be adjusted (see Map Projections -
        // A Working
        // Manual, page 294 under the heading "From equation (14-11)" and the
        // Note: on page 101
        // regarding equation 14-11
        if (rho0 < 0) {
            // TODO verify that this is correct
            x = -x;
            y = -x;
            rho0 = -rho0;
        }

        // from equation 14-11
        double theta = Math.atan(x / (rho0 - y));
        theta = Math.toDegrees(theta);

        // from equation 14-19
        double q = (C - (rho * rho * n * n / (a * a))) / n;

        // from equation 3-16
        double phi = Math.asin(q / 2);
        double sinPhi = Math.sin(phi);
        phi = phi + ((1 - e2 * sinPhi * sinPhi) * (1 - e2 * sinPhi * sinPhi) / (2 * Math.cos(phi)))
                * (q / (1 - e2) - sinPhi / (1 - e2 * sinPhi * sinPhi) + (1 / (2 * e)) * Math.log((1 - e * sinPhi) / (1 + e * sinPhi)));
        phi = Math.toDegrees(phi);

        // from equation 14-9
        double lambda = lambda0 + theta / n;

        return new GeodeticPoint(phi, lambda, getGeographicCoordinateSystem());
    }
}
