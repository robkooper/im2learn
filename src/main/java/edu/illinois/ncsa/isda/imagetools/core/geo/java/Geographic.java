package edu.illinois.ncsa.isda.imagetools.core.geo.java;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.illinois.ncsa.isda.imagetools.core.geo.AngularUnit;
import edu.illinois.ncsa.isda.imagetools.core.geo.GeoException;
import edu.illinois.ncsa.isda.imagetools.core.geo.GeoGraphicCoordinateSystem;
import edu.illinois.ncsa.isda.imagetools.core.geo.GeodeticPoint;
import edu.illinois.ncsa.isda.imagetools.core.geo.LinearUnit;
import edu.illinois.ncsa.isda.imagetools.core.geo.ModelPoint;
import edu.illinois.ncsa.isda.imagetools.core.geo.Projection;
import edu.illinois.ncsa.isda.imagetools.core.geo.TiePoint;


public class Geographic extends Projection implements Serializable {
    private static final long serialVersionUID = 1L;

    public Geographic(String name, ProjectionType type, GeoGraphicCoordinateSystem geogcs, Map<String, String> parameters, TiePoint tiepoint,
            LinearUnit unit) {
        super(name, type, geogcs, parameters, tiepoint, unit);
    }

    @Override
    public List<String> getRequiredParameters() {
        List<String> result = new ArrayList<String>();
        return result;
    }

    @Override
    public ModelPoint earthToModel(GeodeticPoint point) throws IllegalArgumentException, GeoException {
        // do datum shift
        point = new GeodeticPoint(point, getGeographicCoordinateSystem());

        return new ModelPoint(point.getLon(), point.getLat(), point.getHeight(), point.getUnitLatLon(), point.getUnitHeight());
    }

    @Override
    public GeodeticPoint modelToEarth(ModelPoint point) throws IllegalArgumentException, GeoException {
        if (point.getXYUnit() instanceof LinearUnit) {
            // latitude and longitude are assumed to be degrees
            return new GeodeticPoint(point.getY(), point.getX(), point.getZ(), getGeographicCoordinateSystem(), AngularUnit.Decimal_Degree, point
                    .getZUnit());
        } else {
            // for the geographic projection
            // latitude and longitude are assumed to be in the model point units
            return new GeodeticPoint(point.getY(), point.getX(), point.getZ(), getGeographicCoordinateSystem(), (AngularUnit) point.getXYUnit(),
                    point.getZUnit());
        }
    }

    @Override
    public String toStringPRJ() {
        String result = "";

        GeoGraphicCoordinateSystem geogcs = getGeographicCoordinateSystem();
        if (geogcs != null) {
            result += String.format("%s", geogcs.toStringPRJ());
        }

        return result;
    }
}
