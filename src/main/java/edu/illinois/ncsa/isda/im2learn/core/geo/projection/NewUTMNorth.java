package edu.illinois.ncsa.isda.im2learn.core.geo.projection;

import edu.illinois.ncsa.isda.im2learn.core.geo.*;

/**
 *
 * @author Chulyun
 */
public class NewUTMNorth extends ModelProjection {

	private double _rMaj;		/* major axis 				*/
	private double _rMin;		/* minor axis 				*/
	private double _scaleFact;	/* scale factor				*/
	private long _zone;			/* zone number 				*/
	//note: the following class variables are only used by ProjectionLoaderPRJ and PRJWriter
	private double _centralMeridian; //central meridian
	private String _datum; //datum
	private String _primeMeridian;//prime meridian name
	private double _primeMeridianValue;//value of prime meridian
	private double _falseEasting;
	private double _falseNorthing;
	private double _latitudeOfOrigin; //latitude of origin
	private String _geographicCoordinateSystem; //geographic coordinate system
	private String _projectedCoordinateSystem; //projection coordinate system
	private String _angularUnit;//angular unit
	private double _angularUnitValue; //value of angular unit
	private String _linearUnit;//linear unit
	private double _linearUnitValue; //value of linear unit
	
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

	public String get_geographicCoordinateSystem() {
		return _geographicCoordinateSystem;
	}

	public void set_geographicCoordinateSystem(String coordinateSystem) {
		_geographicCoordinateSystem = coordinateSystem;
	}

	public String get_projectedCoordinateSystem() {
		return _projectedCoordinateSystem;
	}

	public void set_projectedCoordinateSystem(String coordinateSystem) {
		_projectedCoordinateSystem = coordinateSystem;
	}

	public NewUTMNorth() {
		setType(Projection.UTM_NORTHERN_HEMISPHERE);
	}
  
	public NewUTMNorth(double rMaj, double rMin, double scaleFact, long zone)
	{
		setType(Projection.UTM_NORTHERN_HEMISPHERE);

		_rMaj = rMaj;
		_rMin = rMin;
		_scaleFact= scaleFact;
		_zone	= zone;
	}
	
	public void setRMaj(double rMaj)
	{
		_rMaj = rMaj;
	}

	public void setRMin(double rMin)
	{
		_rMin = rMin;
	}
	
	public void setScaleFact(double scaleFact)
	{
		_scaleFact = scaleFact;
	}
	
	public void setZone(long zone)
	{
		_zone = zone;
	}
	
	public double getRMaj()
	{
		return _rMaj;
	}

	public double getRMin()
	{
		return _rMin;
	}
	
	public double getScaleFact()
	{
		return _scaleFact;
	}
	
	public long getZone()
	{
		return _zone;
	}
	public String getPrimeMeridian()
	{
		return _primeMeridian;
	}
	 
	public void setPrimeMeridian(String primeM)
	{
		_primeMeridian=primeM;
	}
	
	public double getLatitudeOfOrigin()
	{
		return _latitudeOfOrigin;
	}
	
	public void setLatitudeOfOrigin(double latitudeOfOrigin)
	{
		_latitudeOfOrigin=latitudeOfOrigin;
	}
	
	public double getFalseEasting()
	{
		return _falseEasting;
	}
	
	public double getFalseNorthing()
	{
		return _falseNorthing;
	}
	
	public void setFalseEasting(double flsEast)
	{
		_falseEasting=flsEast;
	}
	
	public void setFalseNorthing(double flsNorth)
	{
		_falseNorthing=flsNorth;
	}
	
	public double getCentralMeridian()
	{
		return _centralMeridian;
	}
	
	public double get_primeMeridianValue() {
		return _primeMeridianValue;
	}

	public void set_primeMeridianValue(double meridianValue) {
		_primeMeridianValue = meridianValue;
	}
	
	public void setCentralMeridian(double centralMeridian)
	{
		_centralMeridian=centralMeridian;
	}
	
	public String getDatum()
	{
		return _datum;
	}
	
	public void setDatum(String dtm)
	{
		_datum=dtm;
	}
	/**
	 * this goes from meters to lat lng
	 * @param pnt
	 * @return
	 */
	public double[] modelToEarth(double[] pnt) {
	 
		double[] ret = new double[2];
		
		MRTInterface.utmint(MRTInterface.inverse, _rMaj, _rMin, _scaleFact, _zone);
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
		
		MRTInterface.utmint(MRTInterface.forward, _rMaj, _rMin, _scaleFact, _zone);
		MRTInterface.geo2coord(getType(), StrictMath.toRadians(pnt[1]), StrictMath.toRadians(pnt[0]), ret); 
			  
		return ret;
	}

  public String getProjectionParametersAsString() {
    String parametersAsString = new String();

    parametersAsString += (this.getType() + " = " + this.getClass() + "\n");
    parametersAsString += (_scaleFact + " = scale factor\n");
    parametersAsString += (_zone + " = zone number\n");
    parametersAsString += (_rMaj + " = major radius\n");
    parametersAsString += (_rMin + " = minor radius\n");

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
    _scaleFact = Double.parseDouble(valueAsString);

    indexOfNewline    = paramString.indexOf(magicNewline,indexOfDelimiter);
    indexOfDelimiter  = paramString.indexOf(magicString,indexOfNewline);
    valueAsString     = paramString.substring(indexOfNewline + 1,indexOfDelimiter);
    _zone = Long.parseLong(valueAsString);

    indexOfNewline    = paramString.indexOf(magicNewline,indexOfDelimiter);
    indexOfDelimiter  = paramString.indexOf(magicString,indexOfNewline);
    valueAsString     = paramString.substring(indexOfNewline + 1,indexOfDelimiter);
    _rMaj = Double.parseDouble(valueAsString);

    indexOfNewline    = paramString.indexOf(magicNewline,indexOfDelimiter);
    indexOfDelimiter  = paramString.indexOf(magicString,indexOfNewline);
    valueAsString     = paramString.substring(indexOfNewline + 1,indexOfDelimiter);
    _rMin = Double.parseDouble(valueAsString);

  }



  
  
  
  
}