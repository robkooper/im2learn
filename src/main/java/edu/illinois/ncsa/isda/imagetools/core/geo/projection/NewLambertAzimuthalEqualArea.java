package edu.illinois.ncsa.isda.imagetools.core.geo.projection;

import edu.illinois.ncsa.isda.imagetools.core.geo.GeoException;

public class NewLambertAzimuthalEqualArea extends ModelProjection {

	private double _rMaj;		/* major axis                   */
	private double _centerLong;	/* center longitude             */
	private double _centerLat;	/* center latitude              */
	private double _falseEast;	/* x offset in meters           */
	private double _falseNorth;	/* y offset in meters           */
	
	  //note: the following class variables are only used in PRJWriter and ProjectionLoaderPRJ
	  private String _geographicCoordinateSystem;
	  private String _projectedCoordinateSystem;
	  private String _datum;
	  private String _primeMeridian;
	  private double _primeMeridianValue;
	  private String _angularUnit;
	  private double _angularUnitValue;
	  private String _linearUnit;
	  private double _linearUnitValue;
	  private double _rMin;//minorRadius
	  private double _centralMeridian;
	
	public double get_centralMeridian() {
		return _centralMeridian;
	}

	public void set_centralMeridian(double meridian) {
		_centralMeridian = meridian;
	}

	public double get_rMin() {
		return _rMin;
	}

	public void set_rMin(double min) {
		_rMin = min;
	}

	public NewLambertAzimuthalEqualArea() {
		setType(Projection.SINUSOIDAL);
	}
  
	public NewLambertAzimuthalEqualArea(double rMaj, double centerLong, double centerLat, double falseEast, double falseNorth)
	{
		setType(Projection.SINUSOIDAL);

		_rMaj = rMaj;
		_centerLong = centerLong;
		_centerLat = centerLat;
		_falseEast = falseEast;
		_falseNorth	= falseNorth;
	}
	
	public void setRMaj(double rMaj)
	{
		_rMaj = rMaj;
	}

	public void setCenterLong(double centerLong)
	{
		_centerLong = centerLong;
	}

	public void setCenterLat(double centerLat)
	{
		_centerLat = centerLat;
	}
	
	public void setFalseEast(double falseEast)
	{
		_falseEast  = falseEast;
	}
	  
	public void setFalseNorth(double falseNorth)
	{
		_falseNorth = falseNorth;
	}
	
	public double getRMaj()
	{
		return _rMaj;
	}

	public double getCenterLong()
	{
		return _centerLong;
	}
	
	public double getCenterLat()
	{
		return _centerLat;
	}
	
	public double getFalseEast()
	{
		return _falseEast;
	}
	  
	public double getFalseNorth()
	{
		return _falseNorth;
	}

	  
	/**
	 * this goes from meters to lat lng
	 * @param pnt
	 * @return
	 */
	public double[] modelToEarth(double[] pnt) {
	 
		double[] ret = new double[2];
		
		MRTInterface.lamazint(MRTInterface.inverse, _rMaj, StrictMath.toRadians(_centerLong), StrictMath.toRadians(_centerLat), _falseEast, _falseNorth);
		MRTInterface.coord2geo(getType(), pnt[0], pnt[1], ret); 

		double[] latlng = new double[2];
		
		latlng[1] = StrictMath.toDegrees(ret[0]);
		latlng[0] = StrictMath.toDegrees(ret[1]);

		return latlng;
	}
	
	/**
	 * This goes from lat/lng to meters
	 * @param pnt
	 * @return
	 */
	public double[] earthToModel(double[] pnt) throws GeoException {
	
		double[] ret = new double[2];
			
		MRTInterface.lamazint(MRTInterface.forward, _rMaj, StrictMath.toRadians(_centerLong), StrictMath.toRadians(_centerLat), _falseEast, _falseNorth);
		MRTInterface.geo2coord(getType(), StrictMath.toRadians(pnt[1]), StrictMath.toRadians(pnt[0]), ret); 
			  
		return ret;
	}
  
  public String getProjectionParametersAsString() {
    String parametersAsString = new String();

    parametersAsString += (this.getType() + " = " + this.getClass() + "\n");
    parametersAsString += (_rMaj + " = rMaj\n");
    parametersAsString += (_centerLong + " = centerLong\n");
    parametersAsString += (_centerLat + " = centerLat\n");
    parametersAsString += (_falseEast + " = false easting\n");
    parametersAsString += (_falseNorth + " = false northing\n");

 //   System.out.println("[" + parametersAsString + "]");
    
    return parametersAsString;
  }
  
  public void setProjectionParametersFromString(String paramString) {
    int indexOfDelimiter  = -1;
    int indexOfNewline    = -1; // assuming that there is at least one character before the first newline
    String valueAsString  = null;
    
    String magicString = " = ";
    String magicNewline = "\n";
    
    // note that the indexOfDelimiter gets bumped up as we go along...

    // we will skip the first line because that contains the info on what type of
    // projection this is and what class should be used... that info will already
    // known if we have gotten this far...
    
//    indexOfDelimiter  = paramString.indexOf(magicString); // first number read starts at the beginning of the string...
//    valueAsString     = paramString.substring(indexOfNewline + 1,indexOfDelimiter);
    indexOfNewline    = paramString.indexOf(magicNewline,indexOfDelimiter);
    indexOfDelimiter  = paramString.indexOf(magicString,indexOfNewline);
    valueAsString     = paramString.substring(indexOfNewline + 1,indexOfDelimiter);
    _rMaj = Double.parseDouble(valueAsString);

    indexOfNewline    = paramString.indexOf(magicNewline,indexOfDelimiter);
    indexOfDelimiter  = paramString.indexOf(magicString,indexOfNewline);
    valueAsString     = paramString.substring(indexOfNewline + 1,indexOfDelimiter);
    _centerLong = Double.parseDouble(valueAsString);

    indexOfNewline    = paramString.indexOf(magicNewline,indexOfDelimiter);
    indexOfDelimiter  = paramString.indexOf(magicString,indexOfNewline);
    valueAsString     = paramString.substring(indexOfNewline + 1,indexOfDelimiter);
    _centerLat = Double.parseDouble(valueAsString);
    
    indexOfNewline    = paramString.indexOf(magicNewline,indexOfDelimiter);
    indexOfDelimiter  = paramString.indexOf(magicString,indexOfNewline);
    valueAsString     = paramString.substring(indexOfNewline + 1,indexOfDelimiter);
    _falseEast = Double.parseDouble(valueAsString);

    indexOfNewline    = paramString.indexOf(magicNewline,indexOfDelimiter);
    indexOfDelimiter  = paramString.indexOf(magicString,indexOfNewline);
    valueAsString     = paramString.substring(indexOfNewline + 1,indexOfDelimiter);
    _falseNorth = Double.parseDouble(valueAsString);

  }

public String get_angularUnit() {
	return _angularUnit;
}

public void set_angularUnit(String unit) {
	_angularUnit = unit;
}

public double get_angularUnitValue() {
	return _angularUnitValue;
}

public void set_angularUnitValue(double unitValue) {
	_angularUnitValue = unitValue;
}

public String get_datum() {
	return _datum;
}

public void set_datum(String _datum) {
	this._datum = _datum;
}

public String get_geographicCoordinateSystem() {
	return _geographicCoordinateSystem;
}

public void set_geographicCoordinateSystem(String coordinateSystem) {
	_geographicCoordinateSystem = coordinateSystem;
}

public String get_linearUnit() {
	return _linearUnit;
}

public void set_linearUnit(String unit) {
	_linearUnit = unit;
}

public double get_linearUnitValue() {
	return _linearUnitValue;
}

public void set_linearUnitValue(double unitValue) {
	_linearUnitValue = unitValue;
}

public String get_primeMeridian() {
	return _primeMeridian;
}

public void set_primeMeridian(String meridian) {
	_primeMeridian = meridian;
}

public double get_primeMeridianValue() {
	return _primeMeridianValue;
}

public void set_primeMeridianValue(double meridianValue) {
	_primeMeridianValue = meridianValue;
}

public String get_projectedCoordinateSystem() {
	return _projectedCoordinateSystem;
}

public void set_projectedCoordinateSystem(String coordinateSystem) {
	_projectedCoordinateSystem = coordinateSystem;
}
	
}
