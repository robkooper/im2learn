package edu.illinois.ncsa.isda.imagetools.core.geo;


public class GeodeticPoint {
	private double lat;
	private double lon;
	private double height;
	private LinearUnit unitHeight;
	private AngularUnit unitLatLon;
	private GeoGraphicCoordinateSystem  geogcs;
	  
	public GeodeticPoint(GeodeticPoint latlon, GeoGraphicCoordinateSystem geogcs) {
		this.lat = latlon.lat;
		this.lon = latlon.lon;
		this.height = latlon.height;
		this.geogcs = latlon.geogcs;
		this.unitLatLon = latlon.unitLatLon;
		this.unitHeight = latlon.unitHeight;
		setGeographicCoordinateSystem(geogcs);
	}
	
	public GeodeticPoint(double lat, double lon) throws GeoException {
		this(lat, lon, 0, new GeoGraphicCoordinateSystem(Datum.WGS_1984), AngularUnit.Decimal_Degree, LinearUnit.Meter);
	}

	public GeodeticPoint(double lat, double lon, GeoGraphicCoordinateSystem geogcs) {
		this(lat, lon, 0, geogcs, AngularUnit.Decimal_Degree, LinearUnit.Meter);
	}

	public GeodeticPoint(double lat, double lon, double height, GeoGraphicCoordinateSystem geogcs) {
		this(lat, lon, height, geogcs, AngularUnit.Decimal_Degree, LinearUnit.Meter);
	}

	public GeodeticPoint(double lat, double lon, double height, GeoGraphicCoordinateSystem geogcs, AngularUnit unitLatLon, LinearUnit unitHeight) {
		this.lat = lat;
		this.lon = lon;
		this.height = height;
		this.geogcs = geogcs;
		this.unitLatLon = unitLatLon;
		this.unitHeight = unitHeight;
	}

	@Override
	public String toString() {
		double[][] dms = new double[2][3];
	
		double x = lat;
		dms[0][0] = (int)x;
		x = 60 * (x - dms[0][0]);
		dms[0][1] = (int)x;
		dms[0][2] = 60 * (x - dms[0][1]);
		
		x = lon;
		dms[1][0] = (int)x;
		x = 60 * (x - dms[1][0]);
		dms[1][1] = (int)x;
		dms[1][2] = 60 * (x - dms[1][1]);
		
		return String.format("GeodeticPoint[%12.8f (%s), %12.8f (%s), %f, %s", lat, dms(lat, true), lon,  dms(lon, false), height, geogcs);
	}
	
	private String dms(double x, boolean ns) {
		char sign = 'N';
		if (x < 0) {
			sign = ns ? 'S' : 'W';
			x = -x;
		} else {
			sign = ns ? 'N' : 'E';
		}
		
		double[] dms = new double[3];
		dms[0] = (int)x;
		x = 60 * (x - dms[0]);
		dms[1] = (int)x;
		dms[2] = 60 * (x - dms[1]);
		
		return String.format("%1.0f %1.0f' %5.2f\"%s", dms[0], dms[1], dms[2], sign);
	}
	
	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double getLon() {
		return lon;
	}

	public void setLon(double lon) {
		this.lon = lon;
	}

	public double getHeight() {
		return height;
	}

	public void setHeight(double height) {
		this.height = height;
	}

	public AngularUnit getUnitLatLon(){
		return this.unitLatLon;
	}

	public void setunitLatLon(AngularUnit angularUnit){
		this.unitLatLon = angularUnit;
	}

	public LinearUnit getUnitHeight(){
		return this.unitHeight;
	}

	public void setUnitHeight(LinearUnit linearUnit){
		this.unitHeight = linearUnit;
	}
	
	public GeoGraphicCoordinateSystem getGeoGraphicCoordinateSystem() {
		return geogcs;
	}

	/**
	 * http://home.hiwaay.net/~taylorc/bookshelf/math-science/geodesy/datum/transform/molodensky/
	 * 
	 * @param datum
	 */
	public void setGeographicCoordinateSystem(GeoGraphicCoordinateSystem geogcs) {
		// fix prime meridian shift
		this.lon += this.geogcs.getPrimeMeridian().getValue();
		
    	double from_a = this.geogcs.getDatum().getEllipsoid().getMajor();
    	double from_f = this.geogcs.getDatum().getEllipsoid().getFlatting();
    	double from_h = this.height;
    	double from_esq = this.geogcs.getDatum().getEllipsoid().getESquared();
    	double da = geogcs.getDatum().getEllipsoid().getMajor() - this.geogcs.getDatum().getEllipsoid().getMajor();
    	double df = geogcs.getDatum().getEllipsoid().getFlatting() - this.geogcs.getDatum().getEllipsoid().getFlatting();
    	double dx = this.geogcs.getDatum().getDx() - geogcs.getDatum().getDx();
    	double dy = this.geogcs.getDatum().getDy() - geogcs.getDatum().getDy();
    	double dz = this.geogcs.getDatum().getDz() - geogcs.getDatum().getDz();
    	double latr = Math.toRadians(this.lat);
    	double lonr = Math.toRadians(this.lon);
        double slat = Math.sin (latr);
        double clat = Math.cos (latr);
        double slon = Math.sin (lonr);
        double clon = Math.cos (lonr);
        double ssqlat = slat * slat;
        double bda = 1.0 - from_f;
        double dlat, dlon, dh;

        double rn = from_a / Math.sqrt (1.0 - from_esq * ssqlat);
        double rm = rn * (1 - from_esq) / (1 - from_esq * ssqlat);

        dlat = (((((-dx * slat * clon - dy * slat * slon) + dz * clat)
                    + (da * ((rn * from_esq * slat * clat) / from_a)))
                + (df * (rm / bda + rn * bda) * slat * clat)))
            / (rm + from_h /* * sin 1" */); //

        dlon = (-dx * slon + dy * clon) / ((rn + from_h) * clat /* * sin 1" */);

        dh = (dx * clat * clon) + (dy * clat * slon) + (dz * slat)
             - (da * (from_a / rn)) + (df * rn * ssqlat * bda);

        this.lon += Math.toDegrees(dlon);
        this.lat += Math.toDegrees(dlat);
        this.height += dh;
        this.geogcs = geogcs;
        
        // fix prime meridian shift
		lon -= geogcs.getPrimeMeridian().getValue();
    }
}
