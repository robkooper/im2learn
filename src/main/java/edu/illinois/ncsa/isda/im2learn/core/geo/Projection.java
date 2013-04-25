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
package edu.illinois.ncsa.isda.im2learn.core.geo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.im2learn.core.geo.java.AlbersEqualAreaConic;
import edu.illinois.ncsa.isda.im2learn.core.geo.java.Geographic;
import edu.illinois.ncsa.isda.im2learn.core.geo.java.LambertAzimuthalEqualArea;
import edu.illinois.ncsa.isda.im2learn.core.geo.java.LambertConformalConic;
import edu.illinois.ncsa.isda.im2learn.core.geo.java.Mercator;
import edu.illinois.ncsa.isda.im2learn.core.geo.java.PlateCarree;
import edu.illinois.ncsa.isda.im2learn.core.geo.java.Sinusoidal;
import edu.illinois.ncsa.isda.im2learn.core.geo.java.TransverseMercator;

/**
 * Use this class to geo-reference an ImageObject if the bounds are specified in
 * lat/lng only. The tie point is a known value that is used to assign lat/lng
 * values to each pixel in the underlying ImageObject. The tie point is anchored
 * at (rasterSpaceI, rasterSpaceJ). Its values are (insertionX, insertionY).
 * scaleX and scaleY are used along with the tie pt to determine the lat/lng of
 * any pixel in the image.
 * 
 * Subclasses should implement the different projections.
 * 
 * @TODO change the name of this class
 * @todo scale, resample, subsample---similar functions---combine them?
 * @todo totally ignoring the third dimension right now.
 */
abstract public class Projection {
    static private Log log = LogFactory.getLog(Projection.class);

    public enum ProjectionType {
        Geographic, Mercator, PlateCarree, Transverse_Mercator, Lambert_Azimuthal_Equal_Area, Lambert_Conformal_Conic, Sinusoidal, Albers
    };

    static public ProjectionType getProjectionType(String name) {
        return ProjectionType.valueOf(name);
    }

    static public Projection getProjection(ProjectionType type, GeoGraphicCoordinateSystem geogcs) throws GeoException {
        return getProjection(type.toString(), type, geogcs, null, null, LinearUnit.Meter);
    }

    static public Projection getProjection(String name, ProjectionType type, GeoGraphicCoordinateSystem geogcs) throws GeoException {
        return getProjection(name, type, geogcs, null, null, LinearUnit.Meter);
    }

    static public Projection getProjection(ProjectionType type, GeoGraphicCoordinateSystem geogcs, Map<String, String> map) throws GeoException {
        return getProjection(type.toString(), type, geogcs, map, null, LinearUnit.Meter);
    }

    static public Projection getProjection(String name, ProjectionType type, GeoGraphicCoordinateSystem geogcs, Map<String, String> map) throws GeoException {
        return getProjection(name, type, geogcs, map, null, LinearUnit.Meter);
    }

    static public Projection getProjection(String name, ProjectionType type, GeoGraphicCoordinateSystem geogcs, Map<String, String> parameters, TiePoint tiepoint, LinearUnit unit) throws GeoException {
        // create projection
        switch (type) {
        case Geographic:
            return new Geographic(name, type, geogcs, parameters, tiepoint, unit);
        case Mercator:
            return new Mercator(name, type, geogcs, parameters, tiepoint, unit);
        case PlateCarree:
            return new PlateCarree(name, type, geogcs, parameters, tiepoint, unit);
        case Transverse_Mercator:
            return new TransverseMercator(name, type, geogcs, parameters, tiepoint, unit);
        case Albers:
            return new AlbersEqualAreaConic(name, type, geogcs, parameters, tiepoint, unit);
        case Lambert_Azimuthal_Equal_Area:
            return new LambertAzimuthalEqualArea(name, type, geogcs, parameters, tiepoint, unit);
        case Lambert_Conformal_Conic:
            return new LambertConformalConic(name, type, geogcs, parameters, tiepoint, unit);
        case Sinusoidal:
            return new Sinusoidal(name, type, geogcs, parameters, tiepoint, unit);
        default:
            throw (new GeoException(String.format("Can not create a projection of type %s.", type)));
        }
    }

    public static final String               FALSE_EASTING       = "False_Easting";
    public static final String               FALSE_NORTHING      = "False_Northing";
    public static final String               CENTRAL_MERIDIAN    = "Central_Meridian";
    public static final String               LATITUDE_OF_ORIGIN  = "Latitude_Of_Origin";
    public static final String               LONGITUDE_OF_ORIGIN = "Longitude_Of_Origin";
    public static final String               LATITUDE_OF_CENTER  = "Latitude_Of_Center";
    public static final String               LONGITUDE_OF_CENTER = "Longitude_Of_Center";
    public static final String               STANDARD_PARALLEL_1 = "Standard_Parallel_1";
    public static final String               STANDARD_PARALLEL_2 = "Standard_Parallel_2";
    public static final String               SCALE_FACTOR        = "Scale_Factor";

    private final String                     name;
    private final ProjectionType             type;
    private final GeoGraphicCoordinateSystem geogcs;
    private Map<String, String>              parameters;
    private TiePoint                         tiepoint;
    private final LinearUnit                 unit;

    protected Projection(String name, ProjectionType type, GeoGraphicCoordinateSystem geogcs, Map<String, String> parameters, TiePoint tiepoint, LinearUnit unit) {
        this.name = name;
        this.type = type;
        this.geogcs = geogcs;
        this.parameters = parameters;
        this.tiepoint = tiepoint;
        this.unit = unit;

        // check param
        List<String> required = getRequiredParameters();
        if (parameters != null) {
            for (String p : parameters.keySet()) {
                if (required.contains(p)) {
                    required.remove(p);
                } else {
                    log.info(String.format("Extra parameter: %s", p));
                }
            }
        }
        for (String p : required) {
            log.info(String.format("Missing parameter: %s", p));
        }
    }

    public String getName() {
        return name;
    }

    public ProjectionType getType() {
        return type;
    }

    public GeoGraphicCoordinateSystem getGeographicCoordinateSystem() {
        return geogcs;
    }

    public boolean hasParameter(String key) {
        if (parameters == null) {
            return false;
        }
        return parameters.containsKey(key);
    }

    public void addParameter(String key, String val) {
        if (parameters == null) {
            parameters = new HashMap<String, String>();
        }
        parameters.put(key, val);
    }

    public void addParameter(String key, double val) {
        if (parameters == null) {
            parameters = new HashMap<String, String>();
        }
        parameters.put(key, Double.toString(val));
    }

    public void removeParameter(String key) {
        if (parameters != null) {
            parameters.remove(key);
        }
    }

    public String getParameterString(String key) throws GeoException {
        if (parameters == null) {
            return null;
        }
        return parameters.get(key);
    }

    public double getParameterNumber(String key) throws GeoException {
        if (parameters == null) {
            throw (new GeoException("No parameters supplied with projection."));
        }

        if (parameters.containsKey(key)) {
            try {
                return Double.parseDouble(parameters.get(key));
            } catch (NumberFormatException exc) {
                log.debug(exc);
            }
        } else {
            log.debug(String.format("Parameter %s did not exist in parameters list, using default.", key));
        }

        if (Projection.FALSE_EASTING.equals(key)) {
            return 0;
        } else if (Projection.FALSE_NORTHING.equals(key)) {
            return 0;
        } else if (Projection.CENTRAL_MERIDIAN.equals(key)) {
            if (parameters.containsKey(Projection.LONGITUDE_OF_ORIGIN)) {
                try {
                    return Double.parseDouble(parameters.get(Projection.LONGITUDE_OF_ORIGIN));
                } catch (NumberFormatException exc) {
                    log.debug(exc);
                }
            }
            if (parameters.containsKey(Projection.LONGITUDE_OF_CENTER)) {
                try {
                    return Double.parseDouble(parameters.get(Projection.LONGITUDE_OF_CENTER));
                } catch (NumberFormatException exc) {
                    log.debug(exc);
                }
            }
            return 0;
        } else if (Projection.LATITUDE_OF_ORIGIN.equals(key)) {
            if (parameters.containsKey(Projection.LATITUDE_OF_CENTER)) {
                try {
                    return Double.parseDouble(parameters.get(Projection.LATITUDE_OF_CENTER));
                } catch (NumberFormatException exc) {
                    log.debug(exc);
                }
            }
            return 0;
        } else if (Projection.LONGITUDE_OF_ORIGIN.equals(key)) {
            if (parameters.containsKey(Projection.CENTRAL_MERIDIAN)) {
                try {
                    return Double.parseDouble(parameters.get(Projection.CENTRAL_MERIDIAN));
                } catch (NumberFormatException exc) {
                    log.debug(exc);
                }
            }
            if (parameters.containsKey(Projection.LONGITUDE_OF_CENTER)) {
                try {
                    return Double.parseDouble(parameters.get(Projection.LONGITUDE_OF_CENTER));
                } catch (NumberFormatException exc) {
                    log.debug(exc);
                }
            }
            return 0;
        } else if (Projection.LATITUDE_OF_CENTER.equals(key)) {
            if (parameters.containsKey(Projection.LATITUDE_OF_ORIGIN)) {
                try {
                    return Double.parseDouble(parameters.get(Projection.LATITUDE_OF_ORIGIN));
                } catch (NumberFormatException exc) {
                    log.debug(exc);
                }
            }
            return 0;
        } else if (Projection.LONGITUDE_OF_CENTER.equals(key)) {
            if (parameters.containsKey(Projection.CENTRAL_MERIDIAN)) {
                try {
                    return Double.parseDouble(parameters.get(Projection.CENTRAL_MERIDIAN));
                } catch (NumberFormatException exc) {
                    log.debug(exc);
                }
            }
            if (parameters.containsKey(Projection.LONGITUDE_OF_ORIGIN)) {
                try {
                    return Double.parseDouble(parameters.get(Projection.LONGITUDE_OF_ORIGIN));
                } catch (NumberFormatException exc) {
                    log.debug(exc);
                }
            }
            return 0;
        } else if (Projection.STANDARD_PARALLEL_1.equals(key)) {
            return 60;
        } else if (Projection.STANDARD_PARALLEL_2.equals(key)) {
            return 60;
        } else if (Projection.SCALE_FACTOR.equals(key)) {
            return 1;
        }

        throw (new GeoException(String.format("Parameter %s was not specified.", key)));
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public Unit getUnit() {
        return unit;
    }

    public TiePoint getTiePoint() {
        return tiepoint;
    }

    public void setTiePoint(TiePoint tiepoint) {
        this.tiepoint = tiepoint;
    }

    public String toStringPRJ() {
        String result = "";

        result += String.format("PROJCS[\"%s\",", name);
        if (geogcs != null) {
            result += String.format("%s,", geogcs.toStringPRJ());
        }
        result += String.format("PROJECTION[\"%s\"],", type);
        if (parameters != null) {
            for (Entry<String, String> entry : parameters.entrySet()) {
                result += String.format("PARAMETER[\"%s\",%s],", entry.getKey(), entry.getValue());
            }
        }
        if (unit != null) {
            result += String.format("%s]", unit);
        } else {
            result += "]";
        }

        return result;
    }

    @Override
    public String toString() {
        String result = "";

        result += String.format("PROJCS[\"%s\",\n", name);
        if (geogcs != null) {
            result += String.format("    %s,\n", geogcs);
        }
        result += String.format("    PROJECTION[\"%s\"]", type);
        if (parameters != null) {
            for (Entry<String, String> entry : parameters.entrySet()) {
                result += String.format(",\n    PARAMETER[\"%s\", %s]", entry.getKey(), entry.getValue());
            }
        }
        if (unit != null) {
            result += String.format(",\n    %s", unit);
        }
        if (tiepoint != null) {
            result += String.format(",\n    %s", tiepoint);
        }
        result += "]";

        return result;
    }

    /**
     * Given a point in raster space convert the point to modelspace. This will
     * take the point, subtract the raster offset, scale it, and add the model
     * offset.
     * 
     * @param point
     *            the image point to be converted.
     * @return a point in modelspace.
     */
    public ModelPoint rasterToModel(RasterPoint point) throws GeoException {
        ModelPoint mp = new ModelPoint();
        TiePoint tp = getTiePoint();
        if (tp == null) {
            throw (new GeoException("No tie point specified, can not find model location."));
        }

        mp.setX(tp.getScaleX() * (point.getX() - tp.getRasterPoint().getX()) + tp.getModelPoint().getX());
        mp.setY(tp.getScaleY() * (point.getY() - tp.getRasterPoint().getY()) + tp.getModelPoint().getY());
        mp.setZ(tp.getScaleZ() * (point.getZ() - tp.getRasterPoint().getZ()) + tp.getModelPoint().getZ());

        if (tp.getModelPoint().getXYUnit() instanceof LinearUnit) {
            if (!tp.getModelPoint().getXYUnit().equals(LinearUnit.Meter)) {
                mp.setX(mp.getX() * tp.getModelPoint().getXYUnit().getMultiplier());
                mp.setY(mp.getY() * tp.getModelPoint().getXYUnit().getMultiplier());
                tp.setScaleX(tp.getScaleX() * tp.getModelPoint().getXYUnit().getMultiplier());
                tp.setScaleY(tp.getScaleY() * tp.getModelPoint().getXYUnit().getMultiplier());
            }
        }
        if (tp.getModelPoint().getXYUnit() instanceof AngularUnit) {
            if (!tp.getModelPoint().getXYUnit().equals(AngularUnit.Decimal_Degree)) {
                mp.setX(mp.getX() * tp.getModelPoint().getXYUnit().getMultiplier());
                mp.setY(mp.getY() * tp.getModelPoint().getXYUnit().getMultiplier());
                tp.setScaleX(tp.getScaleX() * tp.getModelPoint().getXYUnit().getMultiplier());
                tp.setScaleY(tp.getScaleY() * tp.getModelPoint().getXYUnit().getMultiplier());
            }
        }
        if (!tp.getModelPoint().getZUnit().equals(LinearUnit.Meter)) {
            mp.setZ(mp.getZ() * tp.getModelPoint().getZUnit().getMultiplier());
            tp.setScaleZ(tp.getScaleZ() * tp.getModelPoint().getZUnit().getMultiplier());
        }
        return mp;
    }

    /**
     * 
     * @param point
     * @return
     * @throws IllegalArgumentException
     */
    public RasterPoint modelToRaster(ModelPoint point) throws GeoException {
        RasterPoint rp = new RasterPoint();
        TiePoint tp = getTiePoint();
        if (tp == null) {
            throw (new GeoException("No tie point specified, can not find raster location."));
        }

        ModelPoint mp = new ModelPoint(point);
        double val = 1;
        ;
        if (!tp.getModelPoint().getXYUnit().equals(mp.getXYUnit())) {
            val = tp.getModelPoint().getXYUnit().getMultiplier() * mp.getXYUnit().getMultiplier();
            mp.setX(mp.getX() * val);
            mp.setY(mp.getY() * val);
            tp.setScaleX(tp.getScaleX() * val);
            tp.setScaleY(tp.getScaleY() * val);
        }
        if (!tp.getModelPoint().getZUnit().equals(mp.getZUnit())) {
            val = tp.getModelPoint().getZUnit().getMultiplier() * mp.getZUnit().getMultiplier();
            mp.setZ(mp.getZ() * val);
            tp.setScaleZ(tp.getScaleZ() * val);
        }

        rp.setX((mp.getX() - tp.getModelPoint().getX()) / tp.getScaleX() + tp.getRasterPoint().getX());
        rp.setY((mp.getY() - tp.getModelPoint().getY()) / tp.getScaleY() + tp.getRasterPoint().getY());
        rp.setZ((mp.getZ() - tp.getModelPoint().getZ()) / tp.getScaleZ() + tp.getRasterPoint().getZ());

        return rp;
    }

    /**
     * 
     * @param point
     * @return
     * @throws IllegalArgumentException
     * @throws GeoException
     */
    public GeodeticPoint rasterToEarth(RasterPoint point) throws GeoException {
        return modelToEarth(rasterToModel(point));
    }

    /**
     * 
     * @param point
     * @return
     * @throws IllegalArgumentException
     * @throws GeoException
     */
    public RasterPoint earthToRaster(GeodeticPoint point) throws IllegalArgumentException, GeoException {
        return modelToRaster(earthToModel(point));
    }

    /**
     * 
     * @param point
     * @return
     * @throws IllegalArgumentException
     * @throws GeoException
     */
    public abstract GeodeticPoint modelToEarth(ModelPoint point) throws IllegalArgumentException, GeoException;

    /**
     * 
     * @param point
     * @return
     * @throws IllegalArgumentException
     * @throws GeoException
     */
    public abstract ModelPoint earthToModel(GeodeticPoint point) throws IllegalArgumentException, GeoException;

    /**
     * Return a list of parameters required to be set for the projection to work
     * correctly.
     * 
     * @return list of required parameters.
     */
    public abstract List<String> getRequiredParameters();
}
