package edu.illinois.ncsa.isda.imagetools.core.geo;

public class LinearUnit extends Unit {

	// the LinearUnits are from
	// http://publib.boulder.ibm.com/infocenter/idshelp/v10/index.jsp?topic=/com.ibm.spatial.doc/spat262.htm

	// linear LinearUnits
	static public LinearUnit Meter = new LinearUnit("Meter", 1.0);
	static public LinearUnit Foot = new LinearUnit("Foot (International)", 0.3048);
	static public LinearUnit US_Foot = new LinearUnit("U.S. Foot", 12 / 39.37);
	static public LinearUnit Modified_American_Foot = new LinearUnit(
			"Modified American Foot", 12.0004584 / 39.37);
	static public LinearUnit Clarkes_Foot = new LinearUnit("Clarke's Foot", 12 / 39.370432);
	static public LinearUnit Indian_Foot = new LinearUnit("Indian Foot", 12 / 39.370432);
	static public LinearUnit Link = new LinearUnit("Link", 7.92 / 39.370432);
	static public LinearUnit Link_Benoit = new LinearUnit("Link (Benoit)", 7.92 / 39.370113);
	static public LinearUnit Link_Sears = new LinearUnit("Link (Sears)", 7.92 / 39.370147);
	static public LinearUnit Chain_Benoit = new LinearUnit("Chain (Benoit)",
			792 / 39.370113);
	static public LinearUnit Chain_Sears = new LinearUnit("Chain (Sears)", 792 / 39.370147);
	static public LinearUnit Yard_Indian = new LinearUnit("Yard (Indian)", 36 / 39.370141);
	static public LinearUnit Yard_Sears = new LinearUnit("Yard (Sears)", 36 / 39.370147);
	static public LinearUnit Fathom = new LinearUnit("Fathom", 1.8288);
	static public LinearUnit Nautical_Mile = new LinearUnit("Nautical Mile", 1852.0);

	public LinearUnit(String linearUnitName, double multiplier) {
      super(linearUnitName, multiplier);
	}
}
