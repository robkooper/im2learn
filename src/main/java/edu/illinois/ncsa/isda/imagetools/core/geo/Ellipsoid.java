package edu.illinois.ncsa.isda.imagetools.core.geo;

public class Ellipsoid {
	static final public Ellipsoid	Airy_1830;
	static final public Ellipsoid	Modified_Airy;
	static final public Ellipsoid	Australian_National;
	static final public Ellipsoid	Bessel_1841_Namibia;
	static final public Ellipsoid	Bessel_1841;
	static final public Ellipsoid	Clarke_1866;
	static final public Ellipsoid	Clarke_1880;
	static final public Ellipsoid	Everest_India_1830;
	static final public Ellipsoid	Everest_Sabah_Sarawak;
	static final public Ellipsoid	Everest_India_1956;
	static final public Ellipsoid	Everest_Malaysia_1969;
	static final public Ellipsoid	Everest_Malay_and_Singapore_1948;
	static final public Ellipsoid	Everest_Pakistan;
	static final public Ellipsoid	Modified_Fischer_1960;
	static final public Ellipsoid	Helmert_1906;
	static final public Ellipsoid	Hough_1960;
	static final public Ellipsoid	Indonesian_1974;
	static final public Ellipsoid	International_1924;
	static final public Ellipsoid	Krassovsky_1940;
	static final public Ellipsoid	GRS_1980;
	static final public Ellipsoid	South_American_1969;
	static final public Ellipsoid	Sphere_Clarke_1866;
	static final public Ellipsoid	WGS_1972;
	static final public Ellipsoid	WGS_1984;

	private final String			name;
	private final double			major;
	private double					minor;
	private final double			invflat;
	private double					flatting;
	private double					esq;

	static {
		Airy_1830 = new Ellipsoid("Airy 1830", 6377563.396, 299.3249646);
		Modified_Airy = new Ellipsoid("Modified Airy", 6377340.189, 299.3249646);
		Australian_National = new Ellipsoid("Australian National", 6378160, 298.25);
		Bessel_1841_Namibia = new Ellipsoid("Bessel 1841 (Namibia)", 6377483.865, 299.1528128);
		Bessel_1841 = new Ellipsoid("Bessel 1841", 6377397.155, 299.1528128);
		Clarke_1866 = new Ellipsoid("Clarke 1866", 6378206.4, 294.9786982);
		Clarke_1880 = new Ellipsoid("Clarke 1880", 6378249.145, 293.465);
		Everest_India_1830 = new Ellipsoid("Everest (India 1830)", 6377276.345, 300.8017);
		Everest_Sabah_Sarawak = new Ellipsoid("Everest (Sabah Sarawak)", 6377298.556, 300.8017);
		Everest_India_1956 = new Ellipsoid("Everest (India 1956)", 6377301.243, 300.8017);
		Everest_Malaysia_1969 = new Ellipsoid("Everest (Malaysia 1969)", 6377295.664, 300.8017);
		Everest_Malay_and_Singapore_1948 = new Ellipsoid("Everest (Malay & Singapore 1948)", 6377304.063, 300.8017);
		Everest_Pakistan = new Ellipsoid("Everest (Pakistan)", 6377309.613, 300.8017);
		Modified_Fischer_1960 = new Ellipsoid("Modified Fischer 1960", 6378155, 298.3);
		Helmert_1906 = new Ellipsoid("Helmert 1906", 6378200, 298.3);
		Hough_1960 = new Ellipsoid("Hough 1960", 6378270, 297);
		Indonesian_1974 = new Ellipsoid("Indonesian 1974", 6378160, 298.247);
		International_1924 = new Ellipsoid("International 1924", 6378388, 297);
		Krassovsky_1940 = new Ellipsoid("Krassovsky 1940", 6378245, 298.3);
		GRS_1980 = new Ellipsoid("GRS 1980", 6378137, 298.257222101);
		South_American_1969 = new Ellipsoid("South American 1969", 6378160, 298.25);
		Sphere_Clarke_1866 = new Ellipsoid("Sphere_Clarke_1866", 6370997.0, 0);
		WGS_1972 = new Ellipsoid("WGS 1972", 6378135, 298.26);
		WGS_1984 = new Ellipsoid("WGS 1984", 6378137, 298.257223563);
	}

	public Ellipsoid(double major, double invflat) {
		this("", major, invflat);
	}

	public Ellipsoid(String name, double major, double invflat) {
		this.name = name;
		this.major = major;
		this.invflat = invflat;
		computeMinorEsq();
	}

	private void computeMinorEsq() {
		flatting = 1.0 / invflat;
		minor = major * (1.0 - flatting);
		esq = flatting * (2.0 - flatting);
	}

	public String getName() {
		return name;
	}

	public double getMajor() {
		return major;
	}

	public double getMinor() {
		return minor;
	}

	public double getInvflat() {
		return invflat;
	}

	public double getFlatting() {
		return flatting;
	}

	public double getESquared() {
		return esq;
	}

	public String toStringPRJ() {
		return String.format("SPHEROID[\"%s\",%10.3f,%5.2f]", name.replace(" ", "_"), major, invflat);
	}

	@Override
	public String toString() {
		return String.format("SPHEROID[\"%s\", %10.3f, %5.2f]", name, major, invflat);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Ellipsoid) {
			Ellipsoid el = (Ellipsoid) obj;
			if (!this.name.equals(el.name)) {
				return false;
			}
			if (this.esq != el.esq) {
				return false;
			}
			if (this.flatting != el.flatting) {
				return false;
			}
			if (this.invflat != el.invflat) {
				return false;
			}
			if (this.major != el.major) {
				return false;
			}
			if (this.minor != el.minor) {
				return false;
			}
			return true;
		}
		return super.equals(obj);
	}
}
