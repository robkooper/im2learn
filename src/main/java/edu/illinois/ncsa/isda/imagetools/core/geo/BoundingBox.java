package edu.illinois.ncsa.isda.imagetools.core.geo;

public class BoundingBox {
	private double	x;
	private double	y;
	private double	w;
	private double	h;

	public BoundingBox() {
		this(0, 0, 0, 0);
	}

	public BoundingBox(double x, double y, double w, double h) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getMinX() {
		if (w > 0) {
			return x;
		} else {
			return x + w;
		}
	}

	public void setMinX(double x) {
		if (w > 0) {
			this.w = (this.x + w) - x;
			this.x = x;
		} else {
			this.w = x - this.x;
			this.x = x;
		}
	}

	public double getMaxX() {
		if (w > 0) {
			return x + w;
		} else {
			return x;
		}
	}

	public void setMaxX(double x) {
		if (w >= 0) {
			this.w = x - this.x;
		} else {
			this.x = x + w;
			this.w = x - this.x;
		}
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double getMinY() {
		if (h > 0) {
			return y;
		} else {
			return y + h;
		}
	}

	public void setMinY(double y) {
		if (h >= 0) {
			this.h = (this.y + h) - y;
			this.y = y;
		} else {
			this.h = y - this.y;
			this.y = y;
		}
	}

	public double getMaxY() {
		if (h > 0) {
			return y + h;
		} else {
			return y;
		}
	}

	public void setMaxY(double y) {
		if (h >= 0) {
			this.h = y - this.y;
		} else {
			this.y = y + h;
			this.h = y - this.y;
		}
	}

	public double getW() {
		return w;
	}

	public void setW(double w) {
		this.w = w;
	}

	public double getH() {
		return h;
	}

	public void setH(double h) {
		this.h = h;
	}

	public void normalize() {
		if (w < 0) {
			x = x + w;
			w = -w;
		}
		if (h < 0) {
			y = y + h;
			h = -h;
		}
	}

	@Override
	public String toString() {
		normalize();
		return String.format("BBOX[x=%f y=%f w=%f h=%f]", x, y, w, h);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof BoundingBox) {
			BoundingBox bbox = (BoundingBox) obj;
			return (bbox.x == x) && (bbox.y == y) && (bbox.w == w) && (bbox.h == h);
		}
		return super.equals(obj);
	}
}
