package edu.illinois.ncsa.isda.im2learn.core.geo;

public class AngularUnit extends Unit {

	// the units are from
	// http://publib.boulder.ibm.com/infocenter/idshelp/v10/index.jsp?topic=/com.ibm.spatial.doc/spat262.htm

	// angular units
	static public AngularUnit Radian = new AngularUnit("Radian", 1.0);
	static public AngularUnit Decimal_Degree = new AngularUnit("Degree",
			Math.PI / 180.0);
	static public AngularUnit Decimal_Minute = new AngularUnit("Minute", Math.PI
			/ (180.0 * 60.0));
	static public AngularUnit Decimal_Second = new AngularUnit("Second", Math.PI
			/ (180.0 * 3600.0));
	static public AngularUnit Gon = new AngularUnit("Gon", Math.PI / 200.0);
	static public AngularUnit Grad = new AngularUnit("Grad", Math.PI / 200.0);

	public AngularUnit(String angularUnitName, double multiplier) {
	      super(angularUnitName, multiplier);
		}

}
