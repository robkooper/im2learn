package edu.illinois.ncsa.isda.im2learn.core.geo;

public class PrimeMeridian {
	static public PrimeMeridian	Greenwich	= new PrimeMeridian("Greenwich", 0);
	static public PrimeMeridian	Bern		= new PrimeMeridian("Bern", 7.439583333);
	static public PrimeMeridian	Bogota		= new PrimeMeridian("Bogota", -74.080916667);
	static public PrimeMeridian	Brussels	= new PrimeMeridian("Brussels", 4.367975);
	static public PrimeMeridian	Ferro		= new PrimeMeridian("Ferro", -17.666666667);
	static public PrimeMeridian	Jakarta		= new PrimeMeridian("Jakarta", 106.807719444);
	static public PrimeMeridian	Lisbon		= new PrimeMeridian("Lisbon", -9.131906111);
	static public PrimeMeridian	Madrid		= new PrimeMeridian("Madrid", -3.687938889);
	static public PrimeMeridian	Paris		= new PrimeMeridian("Paris", 2.337229167);
	static public PrimeMeridian	Rome		= new PrimeMeridian("Rome", 12.452333333);
	static public PrimeMeridian	Stockholm	= new PrimeMeridian("Stockholm", 18.058055556);

	private final String		name;
	private final double		value;

	public PrimeMeridian(String name, double value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public double getValue() {
		return value;
	}

	public String toStringPRJ() {
		return String.format("PRIMEM[\"%s\",%f]", name, value);
	}

	@Override
	public String toString() {
		return String.format("PRIMEM[\"%s\",%f]", name, value);
	}
}
