package edu.illinois.ncsa.isda.im2learn.core.geo;

/**
 * This class is to store information a unit in a physical space (Cartesian or
 * Spherical coordinate system) It contains the name of the unit and the
 * conversion multiplier to obtain the corresponding value in the default unit
 * 
 * @author pbajcsy
 * 
 */
public class Unit {


	/**
	 * This is the name of the unit
	 */
	private String unitName = new String("Default");

	/**
	 * This is the conversion multiplier to obtain the corresponding value in
	 * the default unit
	 */
	private double multiplier = 1.0;

	protected Unit(String unitName, double multiplier) {
		this.unitName = new String(unitName);
		this.multiplier = multiplier;
	}

	// ////////////////////////////////////////
	// getters and setters
	public double getMultiplier() {
		return multiplier;
	}

	public void setMultiplier(double multiplier) {
		this.multiplier = multiplier;
	}

	public String getUnitName() {
		return unitName;
	}

	public void setUnitName(String unitName) {
		this.unitName = new String(unitName);
	}

	// get the value from the list of parameters converted to the value in the
	// default unit
	public double getValueInDefaultUnit(double val) {
		return (val * this.multiplier);
	}

	public String toStringPRJ() {
		return String.format("UNIT[\"%s\",%f]", unitName, multiplier);
	}

	@Override
	public String toString() {
		return String.format("UNIT[\"%s\",%f]", unitName, multiplier);
	}
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Unit) {
			Unit unit = (Unit) obj;
			return unit.multiplier == multiplier;
		}
		return super.equals(obj);
	}
}
