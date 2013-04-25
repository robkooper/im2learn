package edu.illinois.ncsa.isda.imagetools.core.geo;

public class ModelPoint {
	private double		x;
	private double		y;
	private double		z;
	private Unit		xyUnit;
	private LinearUnit	zUnit;

	public ModelPoint(ModelPoint mp) {
		this(mp.x, mp.y, mp.z, mp.xyUnit, mp.zUnit);
	}

	public ModelPoint(double x, double y) {
		this(x, y, 0, LinearUnit.Meter, LinearUnit.Meter);
	}

	public ModelPoint(double x, double y, double z) {
		this(x, y, z, LinearUnit.Meter, LinearUnit.Meter);
	}

	public ModelPoint(double x, double y, double z, Unit xyunit, LinearUnit zunit) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.xyUnit = xyunit;
		this.zUnit = zunit;
	}

	public ModelPoint() {
		this(0, 0, 0, LinearUnit.Meter, LinearUnit.Meter);
	}

	@Override
	public String toString() {
		return String.format("MODELPOINT[%f , %f, %f, %s, %s]", x, y, z, xyUnit, zUnit);
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double getZ() {
		return z;
	}

	public void setZ(double z) {
		this.z = z;
	}

	public Unit getXYUnit() {
		return xyUnit;
	}

	public void setXYUnit(Unit xyunit) {
		this.xyUnit = xyunit;
	}

	public LinearUnit getZUnit() {
		return zUnit;
	}

	public void setZUnit(LinearUnit zunit) {
		this.zUnit = zunit;
	}

}
