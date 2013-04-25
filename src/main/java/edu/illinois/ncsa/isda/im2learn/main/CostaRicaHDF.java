package edu.illinois.ncsa.isda.im2learn.main;

import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;

import edu.illinois.ncsa.isda.im2learn.core.datatype.ImageObject;
import edu.illinois.ncsa.isda.im2learn.core.geo.AngularUnit;
import edu.illinois.ncsa.isda.im2learn.core.geo.Datum;
import edu.illinois.ncsa.isda.im2learn.core.geo.GeoGraphicCoordinateSystem;
import edu.illinois.ncsa.isda.im2learn.core.geo.GeodeticPoint;
import edu.illinois.ncsa.isda.im2learn.core.geo.LinearUnit;
import edu.illinois.ncsa.isda.im2learn.core.geo.ModelPoint;
import edu.illinois.ncsa.isda.im2learn.core.geo.Projection;
import edu.illinois.ncsa.isda.im2learn.core.geo.ProjectionConvert;
import edu.illinois.ncsa.isda.im2learn.core.geo.RasterPoint;
import edu.illinois.ncsa.isda.im2learn.core.geo.TiePoint;
import edu.illinois.ncsa.isda.im2learn.core.geo.Projection.ProjectionType;
import edu.illinois.ncsa.isda.im2learn.core.io.ImageLoader;
import edu.illinois.ncsa.isda.im2learn.core.io.hdf.HDF;
import edu.illinois.ncsa.isda.im2learn.ext.hyperspectral.ConvertRGB;


public class CostaRicaHDF {
    static public void main(String[] args) throws Exception {
        boolean useElevation = false;
        int width = 1000;
        int height = 1000;

        double scalex = 0.001;
        double scaley = 0.001;

        long l = System.currentTimeMillis();
        String path = "C:\\Documents and Settings\\kooper\\Desktop\\MASTERL1B_0500327_14_20050330_1709_1735_V01.hdf";
        for (String arg : args) {
            if (arg.toLowerCase().equals("-e")) {
                useElevation = true;
            } else {
                path = arg;
            }
        }
        System.out.println((System.currentTimeMillis() - l) + " " + Runtime.getRuntime().totalMemory());

        for (Object name : HDF.listGroup(path + "#/", true, true)) {
            System.out.println(name.toString());
        }
        System.out.println((System.currentTimeMillis() - l) + " " + Runtime.getRuntime().totalMemory());

        HashMap props = HDF.readAttributes(null, path + "#/", null);
        for (Entry entry : (Set<Entry>) props.entrySet()) {
            System.out.println(entry.getKey().toString() + " = " + entry.getValue().toString());
        }
        //
        //        float latur = ((float[]) props.get("lat_UR"))[0];
        //        float latul = ((float[]) props.get("lat_UL"))[0];
        //        float latlr = ((float[]) props.get("lat_LR"))[0];
        //        float latll = ((float[]) props.get("lat_LL"))[0];
        //
        //        float lonur = ((float[]) props.get("lon_UR"))[0];
        //        float lonul = ((float[]) props.get("lon_UL"))[0];
        //        float lonlr = ((float[]) props.get("lon_LR"))[0];
        //        float lonll = ((float[]) props.get("lon_LL"))[0];
        //
        //        System.out.println("UL = " + latul + ", " + lonul);
        //        System.out.println("UR = " + latur + ", " + lonur);
        //        System.out.println("LL = " + latll + ", " + lonll);
        //        System.out.println("LR = " + latlr + ", " + lonlr);
        System.out.println((System.currentTimeMillis() - l) + " " + Runtime.getRuntime().totalMemory());

        // load the lat/lon per pixel
        double[][] plat = (double[][]) HDF.readData(path + "#/PixelLatitude");
        double[][] plon = (double[][]) HDF.readData(path + "#/PixelLongitude");
        int total = plat.length * plat[0].length;
        System.out.println((System.currentTimeMillis() - l) + " " + Runtime.getRuntime().totalMemory());

        // compute min/max of the image
        double maxlat = -Double.MAX_VALUE;
        double minlat = Double.MAX_VALUE;
        double maxlon = -Double.MAX_VALUE;
        double minlon = Double.MAX_VALUE;

        for (int r = 0; r < plat.length; r++) {
            for (int c = 0; c < plat[r].length; c++) {
                if (maxlat < plat[r][c]) {
                    maxlat = plat[r][c];
                }
                if (minlat > plat[r][c]) {
                    minlat = plat[r][c];
                }
                if (maxlon < plon[r][c]) {
                    maxlon = plon[r][c];
                }
                if (minlon > plon[r][c]) {
                    minlon = plon[r][c];
                }
            }
        }
        System.out.println("[N, S, E, W] = [" + maxlat + ", " + minlat + ", " + maxlon + ", " + minlon + "]");
        System.out.println((System.currentTimeMillis() - l) + " " + Runtime.getRuntime().totalMemory());

        // create the image and projection
        ImageObject imgobj = ImageObject.createImage(height, width, useElevation ? 1 : 3, ImageObject.TYPE_DOUBLE);
        Projection proj = Projection.getProjection(ProjectionType.Geographic, new GeoGraphicCoordinateSystem(Datum.WGS_1984));
        ModelPoint mp = new ModelPoint(minlon, maxlat, 0, AngularUnit.Decimal_Degree, LinearUnit.Meter);
        RasterPoint rp = new RasterPoint(0, 0);
        TiePoint tp = new TiePoint(mp, rp, (maxlon - minlon) / width, -(maxlat - minlat) / height, 1);
        proj.setTiePoint(tp);
        imgobj.setProperty(ImageObject.GEOINFO, ProjectionConvert.getOldProjection(proj));
        System.out.println((System.currentTimeMillis() - l) + " " + Runtime.getRuntime().totalMemory());

        if (useElevation) {
            // load the elevation data
            double[][] pelev = (double[][]) HDF.readData(path + "#/PixelElevation");
            System.out.println((System.currentTimeMillis() - l) + " " + Runtime.getRuntime().totalMemory());

            // fill in image
            GeodeticPoint gp = new GeodeticPoint(0, 0);
            for (int r = 0, idx = 0; r < plat.length; r++) {
                for (int c = 0; c < plat[r].length; c++, idx++) {
                    if ((idx % 10000) == 0) {
                        System.out.print(idx + " / " + total + " = " + 100.0 * ((double) idx / total) + "\r");
                    }

                    gp.setLat(plat[r][c]);
                    gp.setLon(plon[r][c]);
                    rp = proj.earthToRaster(gp);
                    if ((rp.getX() >= 0) && (rp.getX() < imgobj.getNumCols()) && (rp.getY() >= 0) && (rp.getY() < imgobj.getNumRows())) {
                        imgobj.set((int) rp.getY(), (int) rp.getX(), 0, pelev[r][c]);
                    } else {
                        System.out.println(gp);
                        System.out.println(rp);
                    }
                }
            }

        } else {
            // load the wavelengths
            float[] wl = (float[]) HDF.readData(path + "#/Central100%ResponseWavelength");
            String[] wls = new String[wl.length];
            for (int i = 0; i < wl.length; i++) {
                wls[i] = String.format("%f", wl[i] * 1000);
            }
            ConvertRGB bruton = ConvertRGB.getBruton(wls);
            bruton.normalize();
            double[][] matrix = bruton.getMatrix();

            // load the calibrated data
            short[][][] pcalib = (short[][][]) HDF.readData(path + "#/CalibratedData");
            System.out.println(pcalib.length + " " + pcalib[0].length + " " + pcalib[0][0].length);
            System.out.println((System.currentTimeMillis() - l) + " " + Runtime.getRuntime().totalMemory());

            // fill in image
            GeodeticPoint gp = new GeodeticPoint(0, 0);
            for (int r = 0, idx = 0; r < plat.length; r++) {
                for (int c = 0; c < plat[r].length; c++, idx++) {
                    if ((idx % 10000) == 0) {
                        System.out.print(idx + " / " + total + " = " + 100.0 * ((double) idx / total) + "\r");
                    }

                    gp.setLat(plat[r][c]);
                    gp.setLon(plon[r][c]);
                    rp = proj.earthToRaster(gp);
                    if ((rp.getX() >= 0) && (rp.getX() < imgobj.getNumCols()) && (rp.getY() >= 0) && (rp.getY() < imgobj.getNumRows())) {
                        for (int b = 0; b < imgobj.getNumBands(); b++) {
                            double val = 0;
                            for (int z = 0; z < pcalib[r].length; z++) {
                                val += matrix[z][b] * pcalib[r][z][c];
                            }
                            imgobj.set((int) rp.getY(), (int) rp.getX(), b, val);
                        }
                    } else {
                        System.out.println(gp);
                        System.out.println(rp);
                    }
                }
            }
        }
        System.out.println((System.currentTimeMillis() - l) + " " + Runtime.getRuntime().totalMemory());

        // save the image
        if (useElevation) {
            ImageLoader.writeImage("cr-elev.tif", imgobj);
            //ImageLoader.writeImage("cr-elev.dem", imgobj);
        } else {
            ImageLoader.writeImage("cr-double.tif", imgobj);
            ImageLoader.writeImage("cr-byte.tif", imgobj.convert(ImageObject.TYPE_BYTE, true));
        }
        System.out.println((System.currentTimeMillis() - l) + " " + Runtime.getRuntime().totalMemory());
    }
}
