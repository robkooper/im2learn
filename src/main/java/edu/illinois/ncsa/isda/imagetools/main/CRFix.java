package edu.illinois.ncsa.isda.imagetools.main;

import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.illinois.ncsa.isda.imagetools.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.imagetools.core.display.ImagePanel;
import edu.illinois.ncsa.isda.imagetools.core.geo.AngularUnit;
import edu.illinois.ncsa.isda.imagetools.core.geo.Datum;
import edu.illinois.ncsa.isda.imagetools.core.geo.GeoGraphicCoordinateSystem;
import edu.illinois.ncsa.isda.imagetools.core.geo.GeodeticPoint;
import edu.illinois.ncsa.isda.imagetools.core.geo.LinearUnit;
import edu.illinois.ncsa.isda.imagetools.core.geo.ModelPoint;
import edu.illinois.ncsa.isda.imagetools.core.geo.Projection;
import edu.illinois.ncsa.isda.imagetools.core.geo.RasterPoint;
import edu.illinois.ncsa.isda.imagetools.core.geo.TiePoint;
import edu.illinois.ncsa.isda.imagetools.core.geo.Projection.ProjectionType;
import edu.illinois.ncsa.isda.imagetools.core.io.ImageLoader;
import edu.illinois.ncsa.isda.imagetools.ext.misc.ImageFrame;
import edu.illinois.ncsa.isda.imagetools.ext.test.AlphaImageAnnotation;


public class CRFix {
	static private GeoGraphicCoordinateSystem	dst			= new GeoGraphicCoordinateSystem(Datum.WGS_1984);
	static private GeoGraphicCoordinateSystem	src			= new GeoGraphicCoordinateSystem(Datum.North_American_1927_Central_America);

	static private boolean						USETOP		= false;
	static private boolean						AVERAGE		= false;

	static private int							TILESIZE	= 1024;

	static private int							UL			= 0;
	static private int							UR			= 1;
	static private int							LR			= 2;
	static private int							LL			= 3;

	static public GeodeticPoint[] getCorners(String[] row) {
		GeodeticPoint[] corners = new GeodeticPoint[4];
		corners[UL] = new GeodeticPoint(Double.parseDouble(row[21]), Double.parseDouble(row[20]), 0, src);
		corners[UL].setGeographicCoordinateSystem(dst);
		corners[UR] = new GeodeticPoint(Double.parseDouble(row[23]), Double.parseDouble(row[22]), 0, src);
		corners[UR].setGeographicCoordinateSystem(dst);
		corners[LR] = new GeodeticPoint(Double.parseDouble(row[25]), Double.parseDouble(row[24]), 0, src);
		corners[LR].setGeographicCoordinateSystem(dst);
		corners[LL] = new GeodeticPoint(Double.parseDouble(row[27]), Double.parseDouble(row[26]), 0, src);
		corners[LL].setGeographicCoordinateSystem(dst);

		return corners;
	}

	static public double[] getBoundingBox(GeodeticPoint[] corners, double[] bbox) {
		if (bbox == null) {
			bbox = new double[4];
			bbox[0] = bbox[1] = Double.MAX_VALUE;
			bbox[2] = bbox[3] = -Double.MAX_VALUE;
		}

		if (corners[UL].getLon() < bbox[0]) {
			bbox[0] = corners[UL].getLon();
		}
		if (corners[UL].getLon() > bbox[2]) {
			bbox[2] = corners[UL].getLon();
		}
		if (corners[UL].getLat() < bbox[1]) {
			bbox[1] = corners[UL].getLat();
		}
		if (corners[UL].getLat() > bbox[3]) {
			bbox[3] = corners[UL].getLat();
		}

		if (corners[UR].getLon() < bbox[0]) {
			bbox[0] = corners[UR].getLon();
		}
		if (corners[UR].getLon() > bbox[2]) {
			bbox[2] = corners[UR].getLon();
		}
		if (corners[UR].getLat() < bbox[1]) {
			bbox[1] = corners[UR].getLat();
		}
		if (corners[UR].getLat() > bbox[3]) {
			bbox[3] = corners[UR].getLat();
		}

		if (corners[LR].getLon() < bbox[0]) {
			bbox[0] = corners[LR].getLon();
		}
		if (corners[LR].getLon() > bbox[2]) {
			bbox[2] = corners[LR].getLon();
		}
		if (corners[LR].getLat() < bbox[1]) {
			bbox[1] = corners[LR].getLat();
		}
		if (corners[LR].getLat() > bbox[3]) {
			bbox[3] = corners[LR].getLat();
		}

		if (corners[LL].getLon() < bbox[0]) {
			bbox[0] = corners[LL].getLon();
		}
		if (corners[LL].getLon() > bbox[2]) {
			bbox[2] = corners[LL].getLon();
		}
		if (corners[LL].getLat() < bbox[1]) {
			bbox[1] = corners[LL].getLat();
		}
		if (corners[LL].getLat() > bbox[3]) {
			bbox[3] = corners[LL].getLat();
		}

		return bbox;
	}

	static private int[] getPixel(ImageObject imgobj, GeodeticPoint[] corners, double lat, double lon) {
		if (USETOP) {
			// top
			double lat1 = corners[UL].getLat();
			double lon1 = corners[UL].getLon();
			double lat2 = corners[UR].getLat();
			double lon2 = corners[UR].getLon();
			double r = (lon - lon1) / (lon2 - lon1);
			double upix = r * imgobj.getNumCols();
			double ulat = r * (lat1 - lat2) + lat2;

			// bottom
			lat1 = corners[LL].getLat();
			lon1 = corners[LL].getLon();
			lat2 = corners[LR].getLat();
			lon2 = corners[LR].getLon();
			r = (lon - lon1) / (lon2 - lon1);
			double lpix = r * imgobj.getNumCols();
			double llat = r * (lat1 - lat2) + lat2;

			r = (lat - llat) / (ulat - llat);

			double y = r * imgobj.getNumRows();
			double x = r * (upix - lpix) + lpix;

			return new int[] { (int) Math.round(x), (int) Math.round(y) };

		} else {

			// left
			double lat1 = corners[UL].getLat();
			double lon1 = corners[UL].getLon();
			double lat2 = corners[LL].getLat();
			double lon2 = corners[LL].getLon();
			double r = 1 - (lat - lat2) / (lat1 - lat2);
			double lpix = r * imgobj.getNumRows();
			double llon = r * (lon2 - lon1) + lon1;

			// right
			lat1 = corners[UR].getLat();
			lon1 = corners[UR].getLon();
			lat2 = corners[LR].getLat();
			lon2 = corners[LR].getLon();
			r = 1 - (lat - lat2) / (lat1 - lat2);
			double rpix = r * imgobj.getNumRows();
			double rlon = r * (lon2 - lon1) + lon1;

			r = (lon - llon) / (rlon - llon);

			double y = r * (rpix - lpix) + lpix;
			double x = r * imgobj.getNumCols();

			return new int[] { (int) Math.round(x), (int) Math.round(y) };
		}
	}

	static public void main(String[] args) throws Exception {
		String path = "C:\\Documents and Settings\\kooper\\Desktop\\robtest\\";

		// ImageObject.setMaxInCoreSize(newMax)

		// parse the DB
		BufferedReader br = new BufferedReader(new FileReader(path + "carta.txt"));
		String[] header = br.readLine().split("\t");
		ArrayList<String[]> rows = new ArrayList<String[]>();
		String line;
		while ((line = br.readLine()) != null) {
			if (!line.equals("")) {
				rows.add(line.split("\t"));
			}
		}

// int idx = 0;
// for (String[] row : rows) {
// GeodeticPoint c = new GeodeticPoint(Double.parseDouble(row[19]), Double.parseDouble(row[18]), 0, nad27);
// c.setGeographicCoordinateSystem(wgs84);
// if ((c.getLat() >= 10.75) && (c.getLat() <= 11) && (c.getLon() >= -84.25) && (c.getLon() <= -84)) {
// System.out.println("copy G:\\Fotos CARTA 2005\\" + row[36] + ".tif \\\\isda\\shared\\CostaRica\\robtest");
// idx++;
// }
// }
// System.out.println(idx);

		// find max bbox for all images
		double[] gbbox = null;
		for (String[] row : rows) {
			if (new File(path + row[36] + ".tif").exists()) {
				GeodeticPoint[] corners = getCorners(row);
				gbbox = getBoundingBox(corners, gbbox);
			}
		}
		System.out.println(String.format("gbbox : %f %f %f %f", gbbox[0], gbbox[1], gbbox[2], gbbox[3]));
		Projection gproj = Projection.getProjection(ProjectionType.Geographic, dst);
		ModelPoint mp = new ModelPoint(gbbox[0], gbbox[3], 0, AngularUnit.Decimal_Degree, LinearUnit.Meter);
		RasterPoint rp = new RasterPoint(0, 0);
// TiePoint tp = new TiePoint(mp, rp, 0.000013, 0.000013, 1);
		TiePoint tp = new TiePoint(mp, rp, 0.000050, 0.000050, 1);
		gproj.setTiePoint(tp);

		// imagesize of global image
		rp = gproj.modelToRaster(new ModelPoint(gbbox[2], gbbox[1], 0, AngularUnit.Decimal_Degree, LinearUnit.Meter));
		System.out.println(rp);

		int numrows = (int) Math.ceil(rp.getRow());
		int numcols = (int) Math.ceil(rp.getCol());

		ImageObject total = ImageObject.createImage(numrows, numcols, 3, ImageObject.TYPE_BYTE);
		// ImageObject total = new ImageObjectOutOfCore((int) rp.getRow(), (int) rp.getCol(), 3, false,
		// ImageObject.TYPE_BYTE);
		total.setInvalidData(0);
		total.setProperty(ImageObject.GEOINFO, gproj);
		System.out.println(total);

// for (int y = 0; y < numrows; y += TILESIZE) {
// for (int x = 0; x < numcols; x += TILESIZE) {
// int sx = (numrows - x > TILESIZE) ? TILESIZE : numcols - x;
// int sy = (numrows - y > TILESIZE) ? TILESIZE : numrows - y;
// String outfile = String.format("tile_%05d_%05d.tif", x, y);
// if ((sx == TILESIZE) || (sy == TILESIZE)) {
// System.out.println(String.format("gdal_translate -srcwin %5d %5d %4d %d total.tif %s", x, y, sx, sy, outfile));
// System.out.println(String.format("gdaladdo -r average %s 2 4 8 16 32 64 128 256", outfile));
// }
// }
// }
//
// System.out.println();
// System.out.println("gdaltindex tile.shp tile_*_*.tif");
//
// System.exit(0);

		ImageFrame imgfrm = null;

		// loop through the rows
		for (String[] row : rows) {
			if (new File(path + row[36] + ".tif").exists()) {
// total = ImageObject.createImage(numrows, numcols, 3, ImageObject.TYPE_BYTE);
// total.setInvalidData(0);

				// get the corners
				GeodeticPoint[] corners = getCorners(row);

				// Load the image and get the bounding box
				ImageObject orig = ImageLoader.readImage(path + row[36] + ".tif");
				double[] bbox = getBoundingBox(corners, null);
				System.out.println(String.format("bbox : %f %f %f %f", bbox[0], bbox[1], bbox[2], bbox[3]));

				// create the new image
// rp = proj.modelToRaster(new ModelPoint(bbox[2], bbox[1], 0, AngularUnit.Decimal_Degree, LinearUnit.Meter));
// ImageObject fixed = ImageObject.createImage((int) Math.ceil(rp.getRow()), (int) Math.ceil(rp.getCol()),
// orig.getNumBands(), orig.getType());
// fixed.setProperty(ImageObject.GEOINFO, proj);
// fixed.setInvalidData(0);
// fixed.setData(0);
// System.out.println(fixed);

				RasterPoint rp0 = gproj.modelToRaster(new ModelPoint(bbox[0], bbox[3], 0, AngularUnit.Decimal_Degree, LinearUnit.Meter));
				RasterPoint rp1 = gproj.modelToRaster(new ModelPoint(bbox[2], bbox[1], 0, AngularUnit.Decimal_Degree, LinearUnit.Meter));
				System.out.println(rp0);
				System.out.println(rp1);

// double dlat = (ul.getLat() - ur.getLat());
// double dlon = (ul.getLon() - ur.getLon());
// double ddeg = Math.sqrt(dlat * dlat + dlon * dlon);
// double scalex = ddeg / orig.getNumCols();
//
// dlat = (ll.getLat() - lr.getLat());
// dlon = (ll.getLon() - lr.getLon());
// ddeg = Math.sqrt(dlat * dlat + dlon * dlon);
// System.out.println(ddeg / orig.getNumCols() + " " + scalex);
//
// dlat = (ul.getLat() - ll.getLat());
// dlon = (ul.getLon() - ll.getLon());
// ddeg = Math.sqrt(dlat * dlat + dlon * dlon);
// double scaley = ddeg / orig.getNumRows();
//
// dlat = (ur.getLat() - lr.getLat());
// dlon = (ur.getLon() - lr.getLon());
// ddeg = Math.sqrt(dlat * dlat + dlon * dlon);
// System.out.println(ddeg / orig.getNumRows() + " " + scaley);
//
// for (double r = 0; r < orig.getNumRows(); r++) {
// double ra = r / orig.getNumRows();
// double lon1 = ul.getLon() * (1 - ra) + ll.getLon() * ra;
// double lon2 = ur.getLon() * (1 - ra) + lr.getLon() * ra;
// for (double c = 0; c < orig.getNumCols(); c++) {
// double ca = c / orig.getNumCols();
// double lat1 = ul.getLat() * (1 - ca) + ur.getLat() * ca;
// double lat2 = ll.getLat() * (1 - ca) + lr.getLat() * ca;
// double lat = lat1 * (1 - ra) + lat2 * ra;
// double lon = lon1 * (1 - ca) + lon2 * ca;
//
// rp = gproj.earthToRaster(new GeodeticPoint(lat, lon, 0, wgs84));
// for (int b = 0; b < orig.getNumBands(); b++) {
// double d = orig.getDouble((int) r, (int) c, b);
// if (total.getDouble((int) rp.getY(), (int) rp.getX(), b) != 0) {
// d += total.getDouble((int) rp.getY(), (int) rp.getX(), b);
// total.set((int) rp.getY(), (int) rp.getX(), b, d / 2.0);
// } else {
// total.set((int) rp.getY(), (int) rp.getX(), b, d);
// }
// }
// }
// }

				// loop through image and copy pixels from the old image
				for (double r = rp0.getRow(); r < rp1.getRow(); r++) {
					for (double c = rp0.getCol(); c < rp1.getCol(); c++) {
						rp.setRow(r);
						rp.setCol(c);
						GeodeticPoint gp = gproj.rasterToEarth(rp);

						int[] pixel = getPixel(orig, corners, gp.getLat(), gp.getLon());
						double x = pixel[0];
						double y = pixel[1];

// double lat = gp.getLat();
// double lon = gp.getLon();
//
// double y1 = (ul.getLat() - lat) / (ul.getLat() - ll.getLat());
// double y2 = (ur.getLat() - lat) / (ur.getLat() - lr.getLat());
// double m1 = (y2 - y1);
// double c1 = orig.getNumRows() * y1;
//
// double x1 = (ul.getLon() - lon) / (ul.getLon() - ur.getLon());
// double x2 = (ll.getLon() - lon) / (ll.getLon() - lr.getLon());
// double m2 = 1 / (x2 - x1);
// double c2 = -x1 * m2 * orig.getNumCols();
//
// double x = (c2 - c1) / (m1 - m2);
// double y = m1 * x + c1;

						if ((x >= 0) && (x < orig.getNumCols()) && (y >= 0) && (y < orig.getNumRows())) {
							for (int b = 0; b < orig.getNumBands(); b++) {
								double d = orig.getDouble((int) y, (int) x, b);
								if (AVERAGE && (total.getDouble((int) r, (int) c, b) != 0)) {
									d += total.getDouble((int) r, (int) c, b);
									total.set((int) r, (int) c, b, d / 2.0);
								} else {
									total.set((int) r, (int) c, b, d);
								}
							}
						}
					}
				}

// ImageLoader.writeImage(path + "total_" + row[36] + ".tif", total);
//
// if (imgfrm == null) {
// imgfrm = new ImageFrame(row[36]);
// imgfrm.addMenu(new ZoomDialog());
// imgfrm.addMenu(new InfoDialog());
// imgfrm.setImageObject(orig);
// imgfrm.setVisible(true);
// imgfrm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
// } else {
// AlphaDialog ad = new AlphaDialog(imgfrm, row[36], orig, imgfrm.getImagePanel());
// imgfrm.getImagePanel().addAnnotationImage(ad.getAnnotation());
// }

				// ImageLoader.writeImage(path + row[36] + "_wgs84.tif", fixed);
				// ImageLoader.writeImage(path + row[36] + "_wgs84.jpg", fixed);
			}
		}
		ImageLoader.writeImage(path + "total.tif", total);
		ImageLoader.writeImage(path + "total.jpg", total);

// double[] minmax = new double[4];
// minmax[0] = minmax[1] = Double.MAX_VALUE;
// minmax[2] = minmax[3] = -Double.MAX_VALUE;
//
// GeodeticPoint ct = new GeodeticPoint(Double.parseDouble(rows.get(0)[19]), Double.parseDouble(rows.get(0)[18]), 0,
// nad27);
// ct.setGeographicCoordinateSystem(wgs84);
// GeodeticPoint ul = new GeodeticPoint(Double.parseDouble(rows.get(0)[21]), Double.parseDouble(rows.get(0)[20]), 0,
// nad27);
// ul.setGeographicCoordinateSystem(wgs84);
// GeodeticPoint ur = new GeodeticPoint(Double.parseDouble(rows.get(0)[23]), Double.parseDouble(rows.get(0)[22]), 0,
// nad27);
// ur.setGeographicCoordinateSystem(wgs84);
// GeodeticPoint lr = new GeodeticPoint(Double.parseDouble(rows.get(0)[25]), Double.parseDouble(rows.get(0)[24]), 0,
// nad27);
// lr.setGeographicCoordinateSystem(wgs84);
// GeodeticPoint ll = new GeodeticPoint(Double.parseDouble(rows.get(0)[27]), Double.parseDouble(rows.get(0)[26]), 0,
// nad27);
// ll.setGeographicCoordinateSystem(wgs84);
//
// ModelPoint mp1 = proj.earthToModel(ul);
// ModelPoint mp2 = proj.earthToModel(ur);
// System.out.println(mp1);
// System.out.println(mp2);
// double angle = Math.atan2(mp2.getY() - mp1.getY(), mp2.getX() - mp1.getX());
// System.out.println(Math.sin(angle) * 4072 + " " + Math.cos(angle) * 4072);
//
// mp2 = proj.earthToModel(ll);
// System.out.println(mp1);
// System.out.println(mp2);
// angle = Math.atan2(mp2.getY() - mp1.getY(), mp2.getX() - mp1.getX());
// System.out.println(Math.sin(angle) * 4072 + " " + Math.cos(angle) * 4072);
//
// mp = proj.earthToModel(ul);
// if (ul.getLon() < minmax[0]) {
// minmax[0] = ul.getLon();
// }
// if (ul.getLon() > minmax[2]) {
// minmax[2] = ul.getLon();
// }
// if (ul.getLat() < minmax[1]) {
// minmax[1] = ul.getLat();
// }
// if (ul.getLat() > minmax[3]) {
// minmax[3] = ul.getLat();
// }
//
// if (ur.getLon() < minmax[0]) {
// minmax[0] = ur.getLon();
// }
// if (ur.getLon() > minmax[2]) {
// minmax[2] = ur.getLon();
// }
// if (ur.getLat() < minmax[1]) {
// minmax[1] = ur.getLat();
// }
// if (ur.getLat() > minmax[3]) {
// minmax[3] = ur.getLat();
// }
//
// if (lr.getLon() < minmax[0]) {
// minmax[0] = lr.getLon();
// }
// if (lr.getLon() > minmax[2]) {
// minmax[2] = lr.getLon();
// }
// if (lr.getLat() < minmax[1]) {
// minmax[1] = lr.getLat();
// }
// if (lr.getLat() > minmax[3]) {
// minmax[3] = lr.getLat();
// }
//
// if (ll.getLon() < minmax[0]) {
// minmax[0] = ll.getLon();
// }
// if (ll.getLon() > minmax[2]) {
// minmax[2] = ll.getLon();
// }
// if (ll.getLat() < minmax[1]) {
// minmax[1] = ll.getLat();
// }
// if (ll.getLat() > minmax[3]) {
// minmax[3] = ll.getLat();
// }
// System.out.println(minmax[0] + " " + minmax[1] + " " + minmax[2] + " " + minmax[3]);
// double lonlat[] = new double[] { minmax[0], minmax[1], minmax[2], minmax[3] };
// double scx = (minmax[2] - minmax[0]) / input.getNumCols();
// double scy = (minmax[3] - minmax[1]) / input.getNumRows();
// System.out.println(scx + " " + scy);
//
// minmax[0] = minmax[1] = Double.MAX_VALUE;
// minmax[2] = minmax[3] = -Double.MAX_VALUE;
//
// mp = proj.earthToModel(ul);
// if (mp.getX() < minmax[0]) {
// minmax[0] = mp.getX();
// }
// if (mp.getX() > minmax[2]) {
// minmax[2] = mp.getX();
// }
// if (mp.getY() < minmax[1]) {
// minmax[1] = mp.getY();
// }
// if (mp.getY() > minmax[3]) {
// minmax[3] = mp.getY();
// }
//
// mp = proj.earthToModel(ur);
// if (mp.getX() < minmax[0]) {
// minmax[0] = mp.getX();
// }
// if (mp.getX() > minmax[2]) {
// minmax[2] = mp.getX();
// }
// if (mp.getY() < minmax[1]) {
// minmax[1] = mp.getY();
// }
// if (mp.getY() > minmax[3]) {
// minmax[3] = mp.getY();
// }
//
// mp = proj.earthToModel(lr);
// if (mp.getX() < minmax[0]) {
// minmax[0] = mp.getX();
// }
// if (mp.getX() > minmax[2]) {
// minmax[2] = mp.getX();
// }
// if (mp.getY() < minmax[1]) {
// minmax[1] = mp.getY();
// }
// if (mp.getY() > minmax[3]) {
// minmax[3] = mp.getY();
// }
//
// mp = proj.earthToModel(ll);
// if (mp.getX() < minmax[0]) {
// minmax[0] = mp.getX();
// }
// if (mp.getX() > minmax[2]) {
// minmax[2] = mp.getX();
// }
// if (mp.getY() < minmax[1]) {
// minmax[1] = mp.getY();
// }
// if (mp.getY() > minmax[3]) {
// minmax[3] = mp.getY();
// }
//
// mp1 = proj.earthToModel(new GeodeticPoint(lonlat[1], lonlat[0], 0, nad27));
// mp2 = proj.earthToModel(new GeodeticPoint(lonlat[1] + scy, lonlat[0] + scx, 0, nad27));
// System.out.println(mp1.getY() - mp2.getY());
// System.out.println(mp1.getX() - mp2.getX());
// System.out.println();
// System.out.println();
//
// System.out.println(minmax[0] + " " + minmax[1] + " " + minmax[2] + " " + minmax[3]);
// tp.getModelPoint().setX(minmax[0]);
// tp.getModelPoint().setY(minmax[1]);
// tp.setScaleX(mp2.getX() - mp1.getX());
// tp.setScaleY(mp1.getY() - mp2.getY());
// tp.setScaleX(20);
// tp.setScaleY(-20);
// System.out.println(tp);
// System.out.println(proj.modelToRaster(new ModelPoint(minmax[0], minmax[1])));
// System.out.println(proj.modelToRaster(new ModelPoint(minmax[0], minmax[1])));
// System.out.println(proj.modelToRaster(new ModelPoint(minmax[2], minmax[3])));
// System.out.println(proj.modelToEarth(proj.earthToModel(ct)));
// System.out.println(proj.earthToRaster(ul));
// System.out.println(proj.earthToRaster(ur));
// System.out.println(proj.earthToRaster(lr));
// System.out.println(proj.earthToRaster(ll));
//
// rp = proj.modelToRaster(new ModelPoint(minmax[2], minmax[3]));
// ImageObject result = ImageObject.createImage((int) Math.ceil(rp.getRow()), (int) Math.ceil(rp.getCol()),
// input.getNumBands(), input.getType());
// result.setProperty(ImageObject.GEOINFO, proj);
// result.setInvalidData(0);
// result.setData(0);
// for (int r = 0; r < result.getNumRows(); r++) {
// for (int c = 0; c < result.getNumCols(); c++) {
// GeodeticPoint gp = proj.rasterToEarth(new RasterPoint(r, c));
//
// double lat = gp.getLat();
// double lon = gp.getLon();
//
// double y1 = (ul.getLat() - lat) / (ul.getLat() - ll.getLat());
// double y2 = (ur.getLat() - lat) / (ur.getLat() - lr.getLat());
// double m1 = (y2 - y1);
// double c1 = input.getNumRows() * y1;
//
// double x1 = (ul.getLon() - lon) / (ul.getLon() - ur.getLon());
// double x2 = (ll.getLon() - lon) / (ll.getLon() - lr.getLon());
// double m2 = 1 / (x2 - x1);
// double c2 = -x1 * m2 * input.getNumCols();
//
// double x = (c2 - c1) / (m1 - m2);
// double y = m1 * x + c1;
//
// if ((x >= 0) && (x < input.getNumCols()) && (y >= 0) && (y < input.getNumRows())) {
// for (int b = 0; b < input.getNumBands(); b++) {
// result.set(r, c, b, input.getDouble((int) y, (int) x, b));
// }
// }
// }
// }
// ImageLoader.writeImage(path + rows.get(0)[36] + "_wgs84.tif", result);
// double lat = 10.7;
// double lon = -85.27;
//
// double y1 = 4072 * (ul.getLat() - lat) / (ul.getLat() - ll.getLat());
// double y2 = 4072 * (ur.getLat() - lat) / (ur.getLat() - lr.getLat());
// double m1 = (y2 - y1) / 4072;
// double c1 = y1;
//
// double x1 = 4072 * (ul.getLon() - lon) / (ul.getLon() - ur.getLon());
// double x2 = 4072 * (ll.getLon() - lon) / (ll.getLon() - lr.getLon());
// double m2 = 4072 / (x2 - x1);
// double c2 = 0 - x1 * m2;
//
// double x = (c2 - c1) / (m1 - m2);
// double y = m1 * x + c1;
//
// System.out.println(x + " " + y);

		// for (int r = 0; r < 4072; r++) {
		// double ar = (4072.0 - r) / 4072.0;
		// for (int c = 0; c < 4072; c++) {
		// double ac = (4072.0 - r) / 4072.0;
		//
		// double lat = ac * (ul.getLat() * ar + (1 - ar) * ll.getLat()) + (1 -
		// ac)
		// * (ur.getLat() * ar + (1 - ar) * lr.getLat());
		// double lon = ac * (ul.getLon() * ar + (1 - ar) * ll.getLon()) + (1 -
		// ac)
		// * (ur.getLon() * ar + (1 - ar) * lr.getLon());
		//
		// GeodeticPoint gp = new GeodeticPoint(lat, lon, nad27);
		// }
		// }
	}

	static class AlphaDialog extends JDialog {
		private final AlphaImageAnnotation	aia;

		public AlphaDialog(JFrame owner, String image, ImageObject imgobj, final ImagePanel imgpnl) {
			super(owner, "Alpha Control " + image);

			this.aia = new AlphaImageAnnotation();
			this.aia.setAlpha(0.5);

			final JLabel lbl = new JLabel("0 0");
			add(lbl, BorderLayout.EAST);

			aia.setImageObject(imgobj);
			final JSlider sldrx = new JSlider(-imgobj.getNumCols(), imgobj.getNumCols(), 0);
			sldrx.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					aia.setX(sldrx.getValue());
					imgpnl.repaint();
					lbl.setText(String.format("%d %d %f", aia.getX(), aia.getY(), aia.getRotation()));
				}
			});
			add(sldrx, BorderLayout.NORTH);

			final JSlider sldry = new JSlider(-imgobj.getNumRows(), imgobj.getNumRows(), 0);
			sldry.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					aia.setY(sldry.getValue());
					imgpnl.repaint();
					lbl.setText(String.format("%d %d %f", aia.getX(), aia.getY(), aia.getRotation()));
				}
			});
			add(sldry, BorderLayout.CENTER);

			final JSlider sldrr = new JSlider(-360, 360, 0);
			sldrr.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					aia.setRotation(Math.toRadians(sldrr.getValue() / 4));
					imgpnl.repaint();
					lbl.setText(String.format("%d %d %f", aia.getX(), aia.getY(), aia.getRotation()));
				}
			});
			add(sldrr, BorderLayout.SOUTH);

			pack();
			setVisible(true);
		}

		public AlphaImageAnnotation getAnnotation() {
			return aia;
		}
	}
}
