package edu.illinois.ncsa.isda.imagetools.core.geo;

public class RasterPoint {
	private double	row;
	private double	col;
	private double	height;

	public RasterPoint(RasterPoint rp) {
		this(rp.row, rp.col, rp.height);
	}

	public RasterPoint() {
		this(0, 0, 0);
	}

	public RasterPoint(double row, double col) {
		this(row, col, 0);
	}

	public RasterPoint(double row, double col, double height) {
		this.row = row;
		this.col = col;
		this.height = height;
	}

	@Override
	public String toString() {
		return String.format("RASTERPOINT[%f, %f, %f]", row, col, height);
	}

	public double getRow() {
		return row;
	}

	public void setRow(double row) {
		this.row = row;
	}

	public double getY() {
		return row;
	}

	public void setY(double y) {
		this.row = y;
	}

	public double getCol() {
		return col;
	}

	public void setCol(double col) {
		this.col = col;
	}

	public double getX() {
		return col;
	}

	public void setX(double x) {
		this.col = x;
	}

	public double getHeight() {
		return height;
	}

	public void setHeight(double height) {
		this.height = height;
	}

	public double getZ() {
		return height;
	}

	public void setZ(double z) {
		this.height = z;
	}
}
