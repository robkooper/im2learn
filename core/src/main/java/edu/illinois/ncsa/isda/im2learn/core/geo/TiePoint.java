package edu.illinois.ncsa.isda.im2learn.core.geo;

public class TiePoint {
	private double		scaleX, scaleY, scaleZ;
	private ModelPoint	modelPoint;
	private RasterPoint	rasterPoint;

	public TiePoint(ModelPoint mp, RasterPoint rp, double scalex, double scaley, double scalez) {
		this.modelPoint = mp;
		this.rasterPoint = rp;
		this.scaleX = scalex;
		this.scaleY = scaley;
		this.scaleZ = scalez;
	}

	public double getScaleX() {
		return scaleX;
	}

	public void setScaleX(double scaleX) {
		this.scaleX = scaleX;
	}

	public double getScaleY() {
		return scaleY;
	}

	public void setScaleY(double scaleY) {
		this.scaleY = scaleY;
	}

	public double getScaleZ() {
		return scaleZ;
	}

	public void setScaleZ(double scaleZ) {
		this.scaleZ = scaleZ;
	}

	public ModelPoint getModelPoint() {
		return modelPoint;
	}

	public void setModelPoint(ModelPoint modelPoint) {
		this.modelPoint = modelPoint;
	}

	public RasterPoint getRasterPoint() {
		return rasterPoint;
	}

	public void setRasterPoint(RasterPoint rasterPoint) {
		this.rasterPoint = rasterPoint;
	}

	@Override
	public String toString() {
		return String.format("TIEPOINT[%s,\n             %s,\n             SCALE[%f, %f %f]]", rasterPoint, modelPoint,
				scaleX, scaleY, scaleZ);
	}
}
