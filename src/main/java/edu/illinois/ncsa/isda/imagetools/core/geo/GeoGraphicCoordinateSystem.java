package edu.illinois.ncsa.isda.imagetools.core.geo;

public class GeoGraphicCoordinateSystem {
	private final String		name;
	private final Datum			datum;
	private final PrimeMeridian	primeMeridian;
	private final AngularUnit	aunit;

	public GeoGraphicCoordinateSystem(Datum datum) {
		this(datum.getName(), datum, PrimeMeridian.Greenwich, AngularUnit.Decimal_Degree);
	}

	public GeoGraphicCoordinateSystem(String name, Datum datum) {
		this(name, datum, PrimeMeridian.Greenwich, AngularUnit.Decimal_Degree);
	}

	public GeoGraphicCoordinateSystem(String name, Datum datum, PrimeMeridian primeMeridian, AngularUnit aunit) {
		this.name = name;
		this.datum = datum;
		this.primeMeridian = primeMeridian;
		this.aunit = aunit;
	}

	public String getName() {
		return name;
	}

	public Datum getDatum() {
		return datum;
	}

	public PrimeMeridian getPrimeMeridian() {
		return primeMeridian;
	}

	public AngularUnit getAngularUnit() {
		return aunit;
	}

	public String toStringPRJ() {
		String nm = name;
		if (!nm.startsWith("GCS_")) {
			nm = "GCS_" + name;
		}
		return String.format("GEOGCS[\"%s\",%s,%s,%s]", nm, datum.toStringPRJ(), primeMeridian, aunit);
	}

	@Override
	public String toString() {
		return String
				.format("GEOGCS[\"%s\",\n        %s,\n        %s,\n        %s]", name, datum, primeMeridian, aunit);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof GeoGraphicCoordinateSystem) {
			GeoGraphicCoordinateSystem geogcs = (GeoGraphicCoordinateSystem) obj;
			if (!this.aunit.equals(geogcs.aunit)) {
				return false;
			}
			if (!this.datum.equals(geogcs.datum)) {
				return false;
			}
			if (!this.name.equals(geogcs.name)) {
				return false;
			}
			if (!this.primeMeridian.equals(geogcs.primeMeridian)) {
				return false;
			}

			return true;
		}
		return super.equals(obj);
	}
}
