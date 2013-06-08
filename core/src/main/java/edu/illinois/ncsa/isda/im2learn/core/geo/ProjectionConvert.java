package edu.illinois.ncsa.isda.im2learn.core.geo;

import java.util.HashMap;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.im2learn.core.geo.Projection.ProjectionType;
import edu.illinois.ncsa.isda.im2learn.core.geo.java.TransverseMercator;
import edu.illinois.ncsa.isda.im2learn.core.geo.projection.LambertConformalConic;
import edu.illinois.ncsa.isda.im2learn.core.geo.projection.Mercator;
import edu.illinois.ncsa.isda.im2learn.core.geo.projection.NewAlbersEqualAreaConic;
import edu.illinois.ncsa.isda.im2learn.core.geo.projection.NewLambertAzimuthalEqualArea;
import edu.illinois.ncsa.isda.im2learn.core.geo.projection.NewSinusoidal;
import edu.illinois.ncsa.isda.im2learn.core.geo.projection.NewUTMNorth;
import edu.illinois.ncsa.isda.im2learn.core.io.tiff.GeoEntry;

/**
 * This class is to support ShapeObject re-projection using the latest projection code
 * 
 * @author pbajcsy and Rob Kooper
 * 
 */

public class ProjectionConvert {
	static private Log	log	= LogFactory.getLog(ProjectionConvert.class);

	public static Projection getNewProjection(Object obj) throws GeoException {
		if (obj == null) {
			return null;
		}
		if (obj instanceof Projection) {
			return (Projection) obj;
		} else if (obj instanceof edu.illinois.ncsa.isda.im2learn.core.geo.projection.Projection) {
			return toNew((edu.illinois.ncsa.isda.im2learn.core.geo.projection.Projection) obj);
		} else {
			throw (new GeoException("Object is not a projection."));
		}
	}

	public static edu.illinois.ncsa.isda.im2learn.core.geo.projection.Projection getOldProjection(Object obj) throws GeoException {
		if (obj == null) {
			return null;
		}
		if (obj instanceof Projection) {
			return toOld((Projection) obj);
		} else if (obj instanceof edu.illinois.ncsa.isda.im2learn.core.geo.projection.Projection) {
			return (edu.illinois.ncsa.isda.im2learn.core.geo.projection.Projection) obj;
		} else {
			throw (new GeoException("Object is not a projection."));
		}
	}

	public static Projection toNew(edu.illinois.ncsa.isda.im2learn.core.geo.projection.Projection oldPRJ) throws GeoException {
		if (oldPRJ == null) {
			return null;
		}
		// based on the following URLs;
		// http://www.ai.sri.com/geovrml/geotransform/api.html
		// the sphere case was resolved by adding a new datum according to
		// ArcGIS docs
		// see the datum class
		Datum dat = null;
		switch (oldPRJ.getEllipsoid()) {
		case edu.illinois.ncsa.isda.im2learn.core.geo.projection.Ellipsoid.WGS_84:
			dat = Datum.WGS_1984;
			break;
		case edu.illinois.ncsa.isda.im2learn.core.geo.projection.Ellipsoid.AIRY_1830:
			// mapping based on
			// http://www.scanex.ru/en/software/scanmagic/SMD_SPC_ENG_SupportedDatums.pdf
			dat = Datum.Ordnance_Survey_Great_Britain_1936_Mean;
			break;
		case edu.illinois.ncsa.isda.im2learn.core.geo.projection.Ellipsoid.CLARKE_1866:
			// mapping based on
			// http://www.scanex.ru/en/software/scanmagic/SMD_SPC_ENG_SupportedDatums.pdf
			dat = Datum.North_American_1927_Conus;
			break;
		case edu.illinois.ncsa.isda.im2learn.core.geo.projection.Ellipsoid.GRS_1980:
			// mapping based on
			// http://www.scanex.ru/en/software/scanmagic/SMD_SPC_ENG_SupportedDatums.pdf
			dat = Datum.North_American_1983_Conus;
			break;
		case edu.illinois.ncsa.isda.im2learn.core.geo.projection.Ellipsoid.SPHERE:
			// http://publib.boulder.ibm.com/infocenter/idshelp/v10/index.jsp?topic=/com.ibm.spatial.doc/spat262.htm
			// 7035 Authalic sphere SPHEROID["Sphere",6371000,0]
			// 107008 Authalic sphere (ARC/INFO)
			// SPHEROID["Sphere_ARC_INFO",6370997,0]
			// Authalic sphere - inverse flattening = 0
			dat = Datum.Authalic_Sphere;
			break;
		default:
			dat = Datum.WGS_1984;
			break;
		}
		// ////
		// units
		// from GeoEntry - in TIFF
		LinearUnit unit = LinearUnit.Meter;
		AngularUnit aunit = AngularUnit.Decimal_Degree;
		switch (oldPRJ.getUnit()) {
		// linear units
		case GeoEntry.Linear_Chain_Benoit:
			unit = LinearUnit.Link_Benoit;
			break;
		case GeoEntry.Linear_Chain_Sears:
			unit = LinearUnit.Link_Sears;
			break;
		case GeoEntry.Linear_Fathom:
			unit = LinearUnit.Fathom;
			break;
		case GeoEntry.Linear_Foot:
			unit = LinearUnit.Foot;
			break;
		case GeoEntry.Linear_Foot_Clarke:
			unit = LinearUnit.Clarkes_Foot;
			break;
		case GeoEntry.Linear_Foot_Indian:
			unit = LinearUnit.Indian_Foot;
			break;
		case GeoEntry.Linear_Foot_Modified_American:
			unit = LinearUnit.Modified_American_Foot;
			break;
		case GeoEntry.Linear_Foot_US_Survey:
			unit = LinearUnit.Foot;
			break;
		case GeoEntry.Linear_Link:
			unit = LinearUnit.Link;
			break;
		case GeoEntry.Linear_Link_Benoit:
			unit = LinearUnit.Link_Benoit;
			break;
		case GeoEntry.Linear_Link_Sears:
			unit = LinearUnit.Link_Sears;
			break;
		case GeoEntry.Linear_Mile_International_Nautical:
			unit = LinearUnit.Nautical_Mile;
			break;
		case GeoEntry.Linear_Yard_Indian:
			unit = LinearUnit.Yard_Indian;
			break;
		case GeoEntry.Linear_Yard_Sears:
			unit = LinearUnit.Yard_Sears;
			break;
		case GeoEntry.Linear_Meter:
			unit = LinearUnit.Meter;
			break;

		// ////////////////angular units
		case GeoEntry.Angular_Arc_Minute:
			aunit = AngularUnit.Decimal_Minute;
			break;
		case GeoEntry.Angular_Arc_Second:
			aunit = AngularUnit.Decimal_Second;
			break;
		case GeoEntry.Angular_Degree:
			aunit = AngularUnit.Decimal_Degree;
			break;
		case GeoEntry.Angular_DMS:
			// approximation only
			aunit = AngularUnit.Decimal_Degree;
			break;
		case GeoEntry.Angular_DMS_Hemisphere:
			// approximation only
			aunit = AngularUnit.Decimal_Degree;
			break;
		case GeoEntry.Angular_Gon:
			aunit = AngularUnit.Gon;
			break;
		case GeoEntry.Angular_Grad:
			aunit = AngularUnit.Grad;
			break;
		case GeoEntry.Angular_Radian:
			aunit = AngularUnit.Radian;
			break;
		default:
			unit = LinearUnit.Meter;
			break;
		}

		// create the geographic coordinate system
		GeoGraphicCoordinateSystem geogcs = new GeoGraphicCoordinateSystem(dat.getName(), dat, PrimeMeridian.Greenwich, aunit);

		// /////////////////
		// tie point
		ModelPoint mp = new ModelPoint(oldPRJ.getInsertionX(), oldPRJ.getInsertionY(), oldPRJ.getInsertionZ());
		RasterPoint rp = new RasterPoint(oldPRJ.getRasterSpaceI(), oldPRJ.getRasterSpaceJ(), oldPRJ.getRasterSpaceK());
		TiePoint tp = new TiePoint(mp, rp, oldPRJ.GetGeoScaleX(), oldPRJ.GetGeoScaleY(), oldPRJ.GetGeoScaleZ());

		// //////////
		// parameters
		HashMap<String, String> param = new HashMap<String, String>();

		switch (oldPRJ.getType()) {
		case edu.illinois.ncsa.isda.im2learn.core.geo.projection.Projection.ALBERS_EQUAL_AREA_CONIC:
			edu.illinois.ncsa.isda.im2learn.core.geo.projection.NewAlbersEqualAreaConic foo = (edu.illinois.ncsa.isda.im2learn.core.geo.projection.NewAlbersEqualAreaConic) oldPRJ;
			param.put(Projection.FALSE_EASTING, "" + foo.getFalseEast());
			param.put(Projection.FALSE_NORTHING, "" + foo.getFalseNorth());
			param.put(Projection.STANDARD_PARALLEL_1, "" + foo.getLat1());
			param.put(Projection.STANDARD_PARALLEL_2, "" + foo.getLat2());
			param.put(Projection.LATITUDE_OF_ORIGIN, "" + foo.getLat0());
			param.put(Projection.CENTRAL_MERIDIAN, "" + foo.getLon0());
			return Projection.getProjection("Albers", ProjectionType.Albers, geogcs, param, tp, unit);

		case edu.illinois.ncsa.isda.im2learn.core.geo.projection.Projection.LAMBERT_AZIMUTHAL_EQUAL_AREA:
			edu.illinois.ncsa.isda.im2learn.core.geo.projection.NewLambertAzimuthalEqualArea foo1 = (edu.illinois.ncsa.isda.im2learn.core.geo.projection.NewLambertAzimuthalEqualArea) oldPRJ;
			param.put(Projection.FALSE_EASTING, "" + foo1.getFalseEast());
			param.put(Projection.FALSE_NORTHING, "" + foo1.getFalseNorth());
			param.put(Projection.LATITUDE_OF_CENTER, "" + foo1.getCenterLat());
			param.put(Projection.LONGITUDE_OF_CENTER, "" + foo1.getCenterLong());
			return Projection.getProjection("Lambert Azimuthal Equal Area", ProjectionType.Lambert_Azimuthal_Equal_Area, geogcs, param, tp, unit);

		case edu.illinois.ncsa.isda.im2learn.core.geo.projection.Projection.SINUSOIDAL:
			edu.illinois.ncsa.isda.im2learn.core.geo.projection.NewSinusoidal foo2 = (edu.illinois.ncsa.isda.im2learn.core.geo.projection.NewSinusoidal) oldPRJ;
			param.put(Projection.CENTRAL_MERIDIAN, "" + foo2.getCenterLong());
			return Projection.getProjection("Sinusoidal", ProjectionType.Sinusoidal, geogcs, param, tp, unit);

		case edu.illinois.ncsa.isda.im2learn.core.geo.projection.Projection.LAMBERT_CONFORMAL_CONIC:
			edu.illinois.ncsa.isda.im2learn.core.geo.projection.LambertConformalConic foo3 = (edu.illinois.ncsa.isda.im2learn.core.geo.projection.LambertConformalConic) oldPRJ;
			param.put(Projection.FALSE_EASTING, "" + +foo3.getFalseEast());
			param.put(Projection.FALSE_NORTHING, "" + foo3.getFalseNorth());
			param.put(Projection.CENTRAL_MERIDIAN, "" + foo3.getCenterLong());
			param.put(Projection.STANDARD_PARALLEL_1, "" + foo3.getFirstParallel());
			param.put(Projection.STANDARD_PARALLEL_2, "" + foo3.getSecondParallel());
			param.put(Projection.LATITUDE_OF_ORIGIN, "" + foo3.getCenterLat());
			return Projection.getProjection("Lambert Conformal Conic", ProjectionType.Lambert_Conformal_Conic, geogcs, param, tp, unit);

		case edu.illinois.ncsa.isda.im2learn.core.geo.projection.Projection.TRANSVERSE_MERCATOR:
			edu.illinois.ncsa.isda.im2learn.core.geo.projection.TransverseMercator foo4 = (edu.illinois.ncsa.isda.im2learn.core.geo.projection.TransverseMercator) oldPRJ;
			param.put(Projection.FALSE_EASTING, "" + +foo4.getFalseEast());
			param.put(Projection.FALSE_NORTHING, "" + foo4.getFalseNorth());
			param.put(Projection.LATITUDE_OF_CENTER, "" + foo4.getCenterLat());
			param.put(Projection.LONGITUDE_OF_CENTER, "" + foo4.getCenterLong());
			param.put(Projection.SCALE_FACTOR, "" + foo4.getScaleFactor());
			return Projection.getProjection("Transverse Mercator", ProjectionType.Transverse_Mercator, geogcs, param, tp, unit);

		case edu.illinois.ncsa.isda.im2learn.core.geo.projection.Projection.UTM_NORTHERN_HEMISPHERE:
			edu.illinois.ncsa.isda.im2learn.core.geo.projection.NewUTMNorth foo6 = (edu.illinois.ncsa.isda.im2learn.core.geo.projection.NewUTMNorth) oldPRJ;
			param.put(Projection.FALSE_EASTING, "500000.0");
			param.put(Projection.FALSE_NORTHING, "0.0");
			param.put(Projection.LATITUDE_OF_CENTER, "0");
			param.put(Projection.LONGITUDE_OF_CENTER, "" + TransverseMercator.computeLongitude((int) foo6.getZone()));
			param.put(Projection.SCALE_FACTOR, "" + foo6.getScaleFact());
			return Projection.getProjection("UTM North", ProjectionType.Transverse_Mercator, geogcs, param, tp, unit);

		case edu.illinois.ncsa.isda.im2learn.core.geo.projection.Projection.MERCATOR:
			edu.illinois.ncsa.isda.im2learn.core.geo.projection.Mercator foo7 = (edu.illinois.ncsa.isda.im2learn.core.geo.projection.Mercator) oldPRJ;
			param.put(Projection.FALSE_EASTING, "" + foo7.getFalseEast());
			param.put(Projection.FALSE_NORTHING, "" + foo7.getFalseNorth());
			param.put(Projection.LATITUDE_OF_CENTER, "" + foo7.getCenterLat());
			param.put(Projection.LONGITUDE_OF_CENTER, "" + foo7.getCenterLon());
			return Projection.getProjection("Mercator", ProjectionType.Mercator, geogcs, param, tp, unit);

		case edu.illinois.ncsa.isda.im2learn.core.geo.projection.Projection.GEOGRAPHIC:
			tp.getModelPoint().setXYUnit(AngularUnit.Decimal_Degree);
			tp.getModelPoint().setZUnit(LinearUnit.Meter);
			return Projection.getProjection("Geographic", ProjectionType.Geographic, geogcs, param, tp, unit);

		default:
			throw new GeoException("Cannot convert old projection to new projection");
		}
	}

	public static edu.illinois.ncsa.isda.im2learn.core.geo.projection.Projection toOld(Projection newPRJ) throws GeoException {
		if (newPRJ == null) {
			return null;
		}
		if (newPRJ.getTiePoint() == null) {
			throw new GeoException("Missing Tie Point in new projection newPrj=" + newPRJ.toStringPRJ());
		}
		RasterPoint rp = newPRJ.getTiePoint().getRasterPoint();
		ModelPoint mp = newPRJ.getTiePoint().getModelPoint();

		edu.illinois.ncsa.isda.im2learn.core.geo.projection.Projection res;
		switch (newPRJ.getType()) {
		case Geographic:
			return new edu.illinois.ncsa.isda.im2learn.core.geo.projection.Projection(rp.getX(), rp.getY(), mp.getX(), mp.getY(), newPRJ.getTiePoint().getScaleX(), newPRJ.getTiePoint().getScaleY(), 0, 0);

		case Albers:
			res = new NewAlbersEqualAreaConic(newPRJ.getGeographicCoordinateSystem().getDatum().getEllipsoid().getMajor(), newPRJ.getGeographicCoordinateSystem().getDatum().getEllipsoid().getMinor(),
					newPRJ.getParameterNumber(Projection.STANDARD_PARALLEL_1), newPRJ.getParameterNumber(Projection.STANDARD_PARALLEL_2), newPRJ.getParameterNumber(Projection.CENTRAL_MERIDIAN),
					newPRJ.getParameterNumber(Projection.LATITUDE_OF_ORIGIN), newPRJ.getParameterNumber(Projection.FALSE_EASTING), newPRJ.getParameterNumber(Projection.FALSE_NORTHING));
			break;
		// return new AlbersEqualAreaConic(rp.getX(), rp.getY(), mp.getX(),
		// mp.getY(),
		// newPRJ.getTiePoint().getScaleX(), newPRJ.getTiePoint().getScaleY(),
		// 0, 0,
		// newPRJ.getFirstStandardParallelLatitude(),
		// newPRJ.getSecondStandardParallelLatitude(),
		// newPRJ.getOriginLatitude(), newPRJ.getOriginLongitude(),
		// newPRJ.getGeographicCoordinateSystem().getDatum().getEllipsoid().getMajor(),
		// newPRJ.getGeographicCoordinateSystem().getDatum().getEllipsoid().getESquared());
		case Lambert_Azimuthal_Equal_Area:
			res = new NewLambertAzimuthalEqualArea(newPRJ.getGeographicCoordinateSystem().getDatum().getEllipsoid().getMajor(), newPRJ.getParameterNumber(Projection.CENTRAL_MERIDIAN), newPRJ
					.getParameterNumber(Projection.LATITUDE_OF_ORIGIN), newPRJ.getParameterNumber(Projection.FALSE_EASTING), newPRJ.getParameterNumber(Projection.FALSE_NORTHING));
			break;
		case Lambert_Conformal_Conic:
			res = new LambertConformalConic(newPRJ.getGeographicCoordinateSystem().getDatum().getEllipsoid().getMajor(), newPRJ.getGeographicCoordinateSystem().getDatum().getEllipsoid().getMinor(),
					newPRJ.getParameterNumber(Projection.STANDARD_PARALLEL_1), newPRJ.getParameterNumber(Projection.STANDARD_PARALLEL_2), newPRJ.getParameterNumber(Projection.CENTRAL_MERIDIAN),
					newPRJ.getParameterNumber(Projection.LATITUDE_OF_ORIGIN), newPRJ.getParameterNumber(Projection.FALSE_EASTING), newPRJ.getParameterNumber(Projection.FALSE_NORTHING));
			break;
		case Sinusoidal:
			res = new NewSinusoidal(newPRJ.getGeographicCoordinateSystem().getDatum().getEllipsoid().getMajor(), newPRJ.getGeographicCoordinateSystem().getDatum().getEllipsoid().getMinor(), newPRJ
					.getParameterNumber(Projection.CENTRAL_MERIDIAN), newPRJ.getParameterNumber(Projection.FALSE_EASTING), newPRJ.getParameterNumber(Projection.FALSE_NORTHING));
			break;
		case Transverse_Mercator:
			long zone = TransverseMercator.computeZone(newPRJ.getParameterNumber(Projection.LONGITUDE_OF_CENTER));
			res = new NewUTMNorth(newPRJ.getGeographicCoordinateSystem().getDatum().getEllipsoid().getMajor(), newPRJ.getGeographicCoordinateSystem().getDatum().getEllipsoid().getMinor(), newPRJ
					.getParameterNumber(Projection.SCALE_FACTOR), zone);
			break;
		case Mercator:
			res = new Mercator(newPRJ.getGeographicCoordinateSystem().getDatum().getEllipsoid().getMajor(), newPRJ.getGeographicCoordinateSystem().getDatum().getEllipsoid().getMinor(), newPRJ
					.getParameterNumber(Projection.LONGITUDE_OF_CENTER), newPRJ.getParameterNumber(Projection.LATITUDE_OF_CENTER), newPRJ.getParameterNumber(Projection.FALSE_EASTING), newPRJ
					.getParameterNumber(Projection.FALSE_NORTHING));
			break;
		default:
			throw new GeoException("Cannot convert new projection to old projection");
		}

		res.setRasterSpaceI(rp.getX());
		res.setRasterSpaceJ(rp.getX());
		res.setRasterSpaceK(rp.getX());
		res.setInsertionX(mp.getX());
		res.setInsertionY(mp.getY());
		res.setInsertionZ(mp.getZ());
		res.setScaleX(newPRJ.getTiePoint().getScaleX());
		res.setScaleY(newPRJ.getTiePoint().getScaleY());
		res.setScaleZ(newPRJ.getTiePoint().getScaleZ());

		return res;
	}
}
