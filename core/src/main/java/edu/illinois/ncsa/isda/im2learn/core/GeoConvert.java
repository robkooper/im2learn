/*******************************************************************************
 * University of Illinois/NCSA
 * Open Source License according to
 * http://www.otm.uiuc.edu/faculty/forms/opensource.asp
 * 
 * Copyright (c) 2006,    NCSA/UIUC.  All rights reserved.
 * 
 * Developed by:
 * 
 * Name of Development Groups:
 * Image Spatial Data Analysis Group (ISDA Group)
 * http://isda.ncsa.uiuc.edu/
 * 
 * Name of Institutions:
 * National Center for Supercomputing Applications (NCSA)
 * http://www.ncsa.uiuc.edu/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the 
 * "Software"), to deal with the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimers.
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimers in the
 *   documentation and/or other materials provided with the distribution.
 *   Neither the names of University of Illinois/NCSA, nor the names
 *   of its contributors may be used to endorse or promote products
 *   derived from this Software without specific prior written permission.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
 *******************************************************************************/
package edu.illinois.ncsa.isda.im2learn.core;

import edu.illinois.ncsa.isda.im2learn.core.datatype.GeoInformation;
import edu.illinois.ncsa.isda.im2learn.core.datatype.Point2DDouble;
import edu.illinois.ncsa.isda.im2learn.core.datatype.Point3DDouble;
import edu.illinois.ncsa.isda.im2learn.core.datatype.SubArea;

/*
 * GeoConvert.java
 *
 */


public class GeoConvert {
    private GeoInformation geoinfo = null;
    private int numrows = 0;

    //constructors
    public GeoConvert(GeoInformation geoinfo) {
        this(geoinfo, 0);
    }

    public GeoConvert(GeoInformation geoinfo, int numrows) {
        this.geoinfo = geoinfo;
        this.numrows = numrows;
    }


    public void subAreaPrep(SubArea area, int origNumRows, int origNumCols) {

        //The SubArea object assumes the top row and left most column both have index
        //0.  origNumRows and origNumCols are the original number and rows and
        //columns (respectively) from the image that we started with.  At this point,
        //geoinfo has numrows and numcols set for the resulting image.  If the
        //tie point is in the lower left corner, it is then impossible to correctly
        //adjust the tie point w/o this information.

        //Easting and Northing values changed for subArea calculations.
        geoinfo.SetRasterSpaceJ(0 + area.height);
        geoinfo.SetRasterSpaceI(area.x);
        geoinfo.SetEastingInsertionValue(geoinfo.GetEastingInsertionValue() +
                                         geoinfo.GetColumnResolution() * area.x);

        if (geoinfo.GetRowResolution() > 0) { //tie point is lower left
            /*System.err.println(geoinfo.GetNorthingInsertionValue());
            System.err.println(geoinfo.GetRowResolution());
            System.err.println(origNumRows);
            System.err.println(area.Row);
            System.err.println(area.High);*/
            geoinfo.SetNorthingInsertionValue(geoinfo.GetNorthingInsertionValue() +
                                              geoinfo.GetRowResolution() * (
                                                                               origNumRows - (area.y + area.height)));
        } else {  //tie point is upper left
            geoinfo.SetNorthingInsertionValue(geoinfo.GetNorthingInsertionValue() +
                                              geoinfo.GetRowResolution() * area.y);
        }

        Point2DDouble p2d = new Point2DDouble(1);
        p2d.ptsDouble[0] = geoinfo.GetNorthingInsertionValue();
        p2d.ptsDouble[1] = geoinfo.GetEastingInsertionValue();
        if (Math.abs(p2d.ptsDouble[0]) >= 180) //in UTM
            p2d = UTMNorthingEasting2LatLng(p2d);
        geoinfo.SetMaxWestLng(p2d.ptsDouble[1]);
        geoinfo.SetMaxSouthLat(p2d.ptsDouble[0]);
        //added 01/22/03
        if (geoinfo.GetGeoProjCenterLng() == -1) //if no projection center specified
            geoinfo.SetUTMZone(determineUTMZone(geoinfo.GetMaxWestLng()));


        geoinfo.SetModelSpaceX(geoinfo.GetEastingInsertionValue());
        geoinfo.SetModelSpaceY(geoinfo.GetNorthingInsertionValue());

        //        geoinfo.numrows = area.High;
        //        geoinfo.numcols = area.Wide;
        //        geoinfo.size = geoinfo.numrows * geoinfo.numcols * geoinfo.sampPerPixel;

        return;
    }


    public void subSamplePrep(int newSampRatio) {
        //We maintain proper georeferencing by scaling the spatial resolution
        //values by the sample ratio.  If every fifth pixel is chosen,
        //the samp ratio is assumed to be 5.
        geoinfo.SetColumnResolution(geoinfo.GetColumnResolution() * newSampRatio);
        geoinfo.SetRowResolution(geoinfo.GetRowResolution() * newSampRatio);
        geoinfo.SetGeoScaleX(geoinfo.GetColumnResolution());
        geoinfo.SetGeoScaleY(Math.abs(geoinfo.GetRowResolution()));
        /*geoinfo.numcols = (int) Math.floor(geoinfo.numcols/newSampRatio);
        geoinfo.numrows = (int) Math.floor(geoinfo.numrows/newSampRatio);
        geoinfo.size = geoinfo.numcols * geoinfo.numcols * geoinfo.sampPerPixel;*/
    }



    //All georeferencing is now done through 6 types of Transformations, handled
    //by six methods.  Each of the six Transformations has a method specific to
    //the model type.  The Transformations are:
    // 1) UTM coordinates to Latitude/Longitude
    // 2) Latitude/Longtiude to UTM coordinates
    // 3) UTM coordinates to Column/Row pixel values
    // 4) Column/Row pixel values to UTM coordinates
    // 5) Column/Row pixel values to Latitude/Longitude
    // 6) Latitude/Longitude to Column/Row pixel values
    // Transformations 5 and and 6 are handled by successive calls to Transformations
    // 4 then 1, and Transformations 2 then 3, respectively.

    //Transformation 1 caller
    public Point2DDouble UTMNorthingEasting2LatLng(Point2DDouble pnt) {

        Point2DDouble ret = null;//new Point2DDouble(1);

        switch (geoinfo.GetModelType()) {
            case 0:
                ret = LambertUTMNorthingEasting2LatLng(pnt);
                break;
            default:
                ret = StandardUTMNorthingEasting2LatLng(pnt);
                break;
        }//end switch statement

        return ret;
    }

    //Transformation 2 caller
    public Point2DDouble LatLng2UTMNorthingEasting(Point2DDouble pnt) {

        Point2DDouble ret = null;// new Point2DDouble(1);

        switch (geoinfo.GetModelType()) {
            case 0:
                ret = LambertLatLng2UTMNorthingEasting(pnt);
                break;
            default:
                ret = StandardLatLng2UTMNorthingEasting(pnt);
                break;
        }//end switch statement

        return ret;

    }

    //Transformation 3 caller
    public Point2DDouble UTMNorthingEasting2ColumnRow(Point2DDouble pnt) {

        Point2DDouble ret = new Point2DDouble(2);

        switch (geoinfo.GetModelType()) {
            case 0:
                ret = LambertUTMNorthingEasting2ColumnRow(pnt);
                break;
            default:
                ret = StandardUTMNorthingEasting2ColumnRow(pnt);
                break;
        }
        return ret;

    }


    //Transformation 4 caller
    public Point2DDouble ColumnRow2UTMNorthingEasting(Point2DDouble pnt) {

        Point2DDouble ret = null;//new Point2DDouble(1);
        switch (geoinfo.GetModelType()) {
            case 0:
                ret = LambertColumnRow2UTMNorthingEasting(pnt);
                break;
            default:
                ret = StandardColumnRow2UTMNorthingEasting(pnt);
                break;
        }
        return ret;

    }

    //Transformation 5 caller
    public Point2DDouble ColumnRow2LatLng(Point2DDouble pnt) {

        Point2DDouble ret = null;//new Point2DDouble(1);
        switch (geoinfo.GetModelType()) {
            case 0:
                ret = LambertColumnRow2LatLng(pnt);
                break;
            case 4:
                ret = DEMColumnRow2LatLng(pnt);
                break;
            case 98:
                ret = TEST98ColumnRow2LatLng(pnt);
                break;
            case 99:
                ret = TEST99ColumnRow2LatLng(pnt);
                break;
            default:
                ret = StandardColumnRow2LatLng(pnt);
                break;
        }
        return ret;

    }

    //Transformation 6 caller
    public Point2DDouble LatLng2ColumnRow(Point2DDouble pnt) {

        Point2DDouble ret = null;//new Point2DDouble(1);
        switch (geoinfo.GetModelType()) {
            case 0:
                ret = LambertLatLng2ColumnRow(pnt);
                break;
            case 4:
                ret = DEMLatLng2ColumnRow(pnt);
                break;
            case 98:
                ret = TEST98LatLng2ColumnRow(pnt);
                break;
            case 99:
                ret = TEST99LatLng2ColumnRow(pnt);
                break;
            default:
                ret = StandardLatLng2ColumnRow(pnt);
                break;
        }
        return ret;
    }

    //Reference site for calculation of latitude/longitude:
    //http://mathworld.wolfram.com/LambertAzimuthalEqual-AreaProjection.html

    //Transformation 1 for type 0
    private Point2DDouble LambertUTMNorthingEasting2LatLng(Point2DDouble pnt) {

        double UTMnorthing = pnt.ptsDouble[0];
        double UTMeasting = pnt.ptsDouble[1];

        double y1 = UTMnorthing;
        double x1 = UTMeasting;

        x1 = x1 / geoinfo.GetRadius();	//divide values by radius of sphere to
        y1 = y1 / geoinfo.GetRadius();	//scale down to unit sphere, which is
        //needed to apply the equations at the
        //address above.

        double rho = Math.sqrt(x1 * x1 + y1 * y1);
        double c = 2 * Math.asin(.5 * rho);
        double frac = (y1 * Math.sin(c) * Math.cos(toRadians(geoinfo.GetGeoProjCenterLat()))) / rho;

        double denom2 = rho * Math.cos(toRadians(geoinfo.GetGeoProjCenterLat())) * Math.cos(c) -
                        y1 * Math.sin(toRadians(geoinfo.GetGeoProjCenterLat())) * Math.sin(c);
        double frac2 = (x1 * Math.sin(c)) / denom2;
        double lngOut = geoinfo.GetGeoProjCenterLng() + toDegrees(Math.atan(frac2));

        //Covers the case of moving from -180.000... W Longitude to 180.0...01
        //E Longitude
        if ((denom2 < 0) && (x1 < 0))
            lngOut = lngOut - 180;
        if (lngOut < -180)
            lngOut = lngOut + 360;

        double latOut = toDegrees(Math.asin(Math.cos(c) * Math.sin(toRadians(geoinfo.GetGeoProjCenterLat())) + frac));
        Point2DDouble ret = new Point2DDouble(1);

        ret.ptsDouble[0] = latOut;
        ret.ptsDouble[1] = lngOut;
        return ret;
    }

    public double[] TestUTMNorthingEasting2LatLng(double[] points) {
        double[] retVal = new double[points.length];
        int length = points.length;

        for (int i = 0; i < length; i += 2) {

            double phi_0 = geoinfo.GetGeoProjCenterLat();
            double lamda_0 = geoinfo.GetGeoProjCenterLng();

            //Northing and false northing
            //double N = pnt.ptsDouble[0];
            double N = points[i];
            double FN = geoinfo.GetGeoProjFalseNorthing();

            //Easting and false easting
            //double E = pnt.ptsDouble[1];
            double E = points[i + 1];
            double FE = geoinfo.GetGeoProjFalseEasting();

            //eccentricity of ellipsoid
            double eSquare = getESquared();
            //double eSquare = .00669437999013;
            if (eSquare < 0 || eSquare == 1.0) {
                System.err.println("ERROR: eSquare is invalid = " + eSquare);
                return null;
            }

            //REAL VALUE
            double a = geoinfo.GetMajorAxis();

            double eTickSquare = eSquare / (1.0 - eSquare);

            //REAL VALUE
            attemptSetScale(.9996); //ALWAYS THIS VALUE FOR UTM
            double k_0 = geoinfo.GetScaleFactor();

            double term1 = (1 - eSquare / 4 - 3 * Math.pow(eSquare, 2) / 64 -
                            5 * Math.pow(eSquare, 3) / 256) * toRadians(phi_0);
            double term2 = (3 * eSquare / 8 + 3 * Math.pow(eSquare, 2) / 32 +
                            45 * Math.pow(eSquare, 3) / 1024) *
                           Math.sin(2 * toRadians(phi_0));
            double term3 = (15 * Math.pow(eSquare, 2) / 256 +
                            45 * Math.pow(eSquare, 3) / 1024) *
                           Math.sin(4 * toRadians(phi_0));
            double term4 = (35 * Math.pow(eSquare, 3) / 3072) *
                           Math.sin(6 * toRadians(phi_0));
            double LargeSum = term1 - term2 + term3 - term4;

            double M_0 = a * LargeSum;

            double M_1 = M_0 + (N - FN) / k_0;

            double mu_1 = M_1 /
                          (a * (1 - eSquare * .25 - 3 * Math.pow(eSquare, 2) / 64 -
                                5 * Math.pow(eSquare, 3) / 256));

            double e_1 = (1 - Math.pow((1 - eSquare), .5)) / (1
                                                              + Math.pow((1 - eSquare), .5));

            double phi_1 = mu_1 +
                           (1.5 * e_1 - 27 * Math.pow(e_1, 3) / 32) * Math.sin(2 * mu_1) +
                           (21 * e_1 * e_1 / 16 - 55 * Math.pow(e_1, 4) / 32) *
                           Math.sin(4 * mu_1) +
                           (151 * Math.pow(e_1, 3) / 96) * Math.sin(6 * mu_1) +
                           (1097 * Math.pow(e_1, 4) / 512) * Math.sin(8 * mu_1);

            double v_1 = a / Math.pow((1 - eSquare * Math.sin(phi_1) *
                                           Math.sin(phi_1)), .5);

            double D = (E - FE) / (v_1 * k_0);

            double T_1 = Math.tan(phi_1) * Math.tan(phi_1);

            double C_1 = eTickSquare * Math.cos(phi_1) * Math.cos(phi_1);

            double rho_1 = (a * (1 - eSquare)) /
                           Math.pow((1 - eSquare * Math.pow(Math.sin(phi_1), 2)), 1.5);

            double Bigsum = .5 * D * D -
                            (5 + 3 * T_1 + 10 * C_1 - 4 * C_1 * C_1 - 9 * eTickSquare) *
                            Math.pow(D, 4) / 24
                            + (61 + 90 * T_1 + 298 * C_1 + 45 * T_1 * T_1 - 252 * eTickSquare -
                               3 * C_1 * C_1) * Math.pow(D, 6) / 720;

            double Bigsum2 = D - (1 + 2 * T_1 + C_1) * Math.pow(D, 3) / 6 +
                             (5 - 2 * C_1 + 28 * T_1 - 3 * C_1 * C_1 + 8 * Math.pow(eTickSquare, 2)
                              + 24 * T_1 * T_1) * Math.pow(D, 5) / 120;

            /*System.out.println("eSquare is "+eSquare);
                 System.out.println("eTickSquare is "+eTickSquare);
                 System.out.println("phi_0 in radians is "+toRadians(phi_0));
                 System.out.println("phi_0 is "+phi_0);
                 System.out.println("phi_1 is "+phi_1);
                 System.out.println("v_1 is "+v_1);
                 System.out.println("D is "+D);
                 System.out.println("T_1 is "+T_1);
                 System.out.println("M_1 is "+M_1);
                 System.out.println("M_0 is "+M_0);
                 System.out.println("rho_1 is "+rho_1);
                 System.out.println("e_1 is "+e_1);
                 System.out.println("mu_1 is "+mu_1);
                 System.out.println("a is "+a);
                 System.out.println("E is "+E);
                 System.out.println("FE is "+FE);*/

            //FINAL VALUES
            double lamda = toRadians(lamda_0) + Bigsum2 / Math.cos(phi_1);

            double phi = phi_1 - v_1 * Math.tan(phi_1) * Bigsum / rho_1;

            //Point2DDouble ret = new Point2DDouble(1);
            //ret.ptsDouble[0] = toDegrees(phi);
            //ret.ptsDouble[1] = toDegrees(lamda);
            retVal[i] = toDegrees(phi);
            retVal[i + 1] = toDegrees(lamda);

            //  return ret;
        }

        return retVal;
    }


    //Transformation 1 for type 1
    //Reference site for calculation of latitude/longitude:
    //http://www.posc.org/Epicentre.2_2/DataModel/ExamplesofUsage/eu_cs34h.html
    private Point2DDouble StandardUTMNorthingEasting2LatLng(Point2DDouble pnt) {

        double phi_0 = geoinfo.GetGeoProjCenterLat();
        double lamda_0 = geoinfo.GetGeoProjCenterLng();

        //Northing and false northing
        double N = pnt.ptsDouble[0];
        double FN = geoinfo.GetGeoProjFalseNorthing();

        //Easting and false easting
        double E = pnt.ptsDouble[1];
        double FE = geoinfo.GetGeoProjFalseEasting();

        //eccentricity of ellipsoid
        double eSquare = getESquared();
        //double eSquare = .00669437999013;
        if (eSquare < 0 || eSquare == 1.0) {
            System.err.println("ERROR: eSquare is invalid = " + eSquare);
            return null;
        }

        //REAL VALUE
        double a = geoinfo.GetMajorAxis();

        double eTickSquare = eSquare / (1.0 - eSquare);

        //REAL VALUE
        attemptSetScale(.9996);	//ALWAYS THIS VALUE FOR UTM
        double k_0 = geoinfo.GetScaleFactor();

        double term1 = (1 - eSquare / 4 - 3 * Math.pow(eSquare, 2) / 64 -
                        5 * Math.pow(eSquare, 3) / 256) * toRadians(phi_0);
        double term2 = (3 * eSquare / 8 + 3 * Math.pow(eSquare, 2) / 32 +
                        45 * Math.pow(eSquare, 3) / 1024) * Math.sin(2 * toRadians(phi_0));
        double term3 = (15 * Math.pow(eSquare, 2) / 256 +
                        45 * Math.pow(eSquare, 3) / 1024) * Math.sin(4 * toRadians(phi_0));
        double term4 = (35 * Math.pow(eSquare, 3) / 3072) * Math.sin(6 * toRadians(phi_0));
        double LargeSum = term1 - term2 + term3 - term4;

        double M_0 = a * LargeSum;

        double M_1 = M_0 + (N - FN) / k_0;

        double mu_1 = M_1 / (a * (1 - eSquare * .25 - 3 * Math.pow(eSquare, 2) / 64 -
                                  5 * Math.pow(eSquare, 3) / 256));

        double e_1 = (1 - Math.pow((1 - eSquare), .5)) / (1
                                                          + Math.pow((1 - eSquare), .5));

        double phi_1 = mu_1 + (1.5 * e_1 - 27 * Math.pow(e_1, 3) / 32) * Math.sin(2 * mu_1) +
                       (21 * e_1 * e_1 / 16 - 55 * Math.pow(e_1, 4) / 32) * Math.sin(4 * mu_1) +
                       (151 * Math.pow(e_1, 3) / 96) * Math.sin(6 * mu_1) +
                       (1097 * Math.pow(e_1, 4) / 512) * Math.sin(8 * mu_1);

        double v_1 = a / Math.pow((1 - eSquare * Math.sin(phi_1) *
                                       Math.sin(phi_1)), .5);

        double D = (E - FE) / (v_1 * k_0);

        double T_1 = Math.tan(phi_1) * Math.tan(phi_1);

        double C_1 = eTickSquare * Math.cos(phi_1) * Math.cos(phi_1);

        double rho_1 = (a * (1 - eSquare)) /
                       Math.pow((1 - eSquare * Math.pow(Math.sin(phi_1), 2)), 1.5);


        double Bigsum = .5 * D * D -
                        (5 + 3 * T_1 + 10 * C_1 - 4 * C_1 * C_1 - 9 * eTickSquare) * Math.pow(D, 4) / 24
                        + (61 + 90 * T_1 + 298 * C_1 + 45 * T_1 * T_1 - 252 * eTickSquare -
                           3 * C_1 * C_1) * Math.pow(D, 6) / 720;

        double Bigsum2 = D - (1 + 2 * T_1 + C_1) * Math.pow(D, 3) / 6 +
                         (5 - 2 * C_1 + 28 * T_1 - 3 * C_1 * C_1 + 8 * Math.pow(eTickSquare, 2)
                          + 24 * T_1 * T_1) * Math.pow(D, 5) / 120;

        /*System.out.println("eSquare is "+eSquare);
        System.out.println("eTickSquare is "+eTickSquare);
        System.out.println("phi_0 in radians is "+toRadians(phi_0));
        System.out.println("phi_0 is "+phi_0);
        System.out.println("phi_1 is "+phi_1);
        System.out.println("v_1 is "+v_1);
        System.out.println("D is "+D);
        System.out.println("T_1 is "+T_1);
        System.out.println("M_1 is "+M_1);
        System.out.println("M_0 is "+M_0);
        System.out.println("rho_1 is "+rho_1);
        System.out.println("e_1 is "+e_1);
        System.out.println("mu_1 is "+mu_1);
        System.out.println("a is "+a);
        System.out.println("E is "+E);
        System.out.println("FE is "+FE);*/

        //FINAL VALUES
        double lamda = toRadians(lamda_0) + Bigsum2 / Math.cos(phi_1);

        double phi = phi_1 - v_1 * Math.tan(phi_1) * Bigsum / rho_1;

        Point2DDouble ret = new Point2DDouble(1);
        ret.ptsDouble[0] = toDegrees(phi);
        ret.ptsDouble[1] = toDegrees(lamda);

        return ret;
    }


    //Transformation 2 for type 0
    private Point2DDouble LambertLatLng2UTMNorthingEasting(Point2DDouble pnt) {

        double inLat = pnt.ptsDouble[0];
        double inLng = pnt.ptsDouble[1];

        double denom = 1 + Math.sin(toRadians(geoinfo.GetGeoProjCenterLat())) *
                           Math.sin(toRadians(inLat)) +
                       Math.cos(toRadians(geoinfo.GetGeoProjCenterLat())) *
                       Math.cos(toRadians(inLat)) *
                       Math.cos(toRadians(inLng - geoinfo.GetGeoProjCenterLng()));
        double k = Math.sqrt(2 / denom);

        double x = k * Math.cos(toRadians(inLat)) * Math.sin(toRadians(inLng - geoinfo.GetGeoProjCenterLng()));

        double y = k * (Math.cos(toRadians(geoinfo.GetGeoProjCenterLat())) *
                        Math.sin(toRadians(inLat)) -
                        Math.sin(toRadians(geoinfo.GetGeoProjCenterLat())) * Math.cos(toRadians(inLat)) *
                        Math.cos(toRadians(inLng - geoinfo.GetGeoProjCenterLng())));

        double retEasting = x * geoinfo.GetRadius();
        double retNorthing = y * geoinfo.GetRadius();

        //int column = (int) Math.round(((x*_radius)-_eastingInsertionValue)/_columnResolution);
        //int row = (int) Math.round((-(y*_radius)+_northingInsertionValue)/_rowResolution);

        Point2DDouble ret = new Point2DDouble(1);
        ret.ptsDouble[0] = retNorthing;
        ret.ptsDouble[1] = retEasting;
        return ret;

    }

    //Transformation 2 for type 1
    private Point2DDouble StandardLatLng2UTMNorthingEasting(Point2DDouble pnt) {

        double phi = pnt.ptsDouble[0];
        double phi_0 = geoinfo.GetGeoProjCenterLat();
        double eSquare = getESquared();
        double a = geoinfo.GetMajorAxis();

        double FN = geoinfo.GetGeoProjFalseNorthing();
        double FE = geoinfo.GetGeoProjFalseEasting();

        double lamda = pnt.ptsDouble[1];
        double lamda_0 = geoinfo.GetGeoProjCenterLng();

        attemptSetScale(.9996);	//ALWAYS THIS VALUE FOR UTM northern hemisphere
        double k_0 = geoinfo.GetScaleFactor();
        double v = a / Math.pow((1 - eSquare * Math.sin(toRadians(phi)) *
                                     Math.sin(toRadians(phi))), .5);
        double A = (toRadians(lamda) - toRadians(lamda_0)) * Math.cos(toRadians(phi));
        double eTickSquare = eSquare / (1 - eSquare);

        double T = Math.tan(toRadians(phi)) * Math.tan(toRadians(phi));

        double C = eSquare / (1 - eSquare) * Math.cos(toRadians(phi)) * Math.cos(toRadians(phi));

        double term1 = (1 - eSquare / 4 - 3 * Math.pow(eSquare, 2) / 64 -
                        5 * Math.pow(eSquare, 3) / 256) * toRadians(phi);

        double term1_0 = (1 - eSquare / 4 - 3 * Math.pow(eSquare, 2) / 64 -
                          5 * Math.pow(eSquare, 3) / 256) * toRadians(phi_0);

        double term2 = (3 * eSquare / 8 + 3 * Math.pow(eSquare, 2) / 32 +
                        45 * Math.pow(eSquare, 3) / 1024) * Math.sin(2 * toRadians(phi));
        double term2_0 = (3 * eSquare / 8 + 3 * Math.pow(eSquare, 2) / 32 +
                          45 * Math.pow(eSquare, 3) / 1024) * Math.sin(2 * toRadians(phi_0));

        double term3 = (15 * Math.pow(eSquare, 2) / 256 +
                        45 * Math.pow(eSquare, 3) / 1024) * Math.sin(4 * toRadians(phi));
        double term3_0 = (15 * Math.pow(eSquare, 2) / 256 +
                          45 * Math.pow(eSquare, 3) / 1024) * Math.sin(4 * toRadians(phi_0));

        double term4 = (35 * Math.pow(eSquare, 3) / 3072) * Math.sin(6 * toRadians(phi));
        double term4_0 = (35 * Math.pow(eSquare, 3) / 3072) * Math.sin(6 * toRadians(phi_0));


        double LargeSum = term1 - term2 + term3 - term4;
        double LargeSum_0 = term1_0 - term2_0 + term3_0 - term4_0;

        double M_0 = a * LargeSum_0;
        double M = a * LargeSum;

        double sum1 = 5 - T + 9 * C + 4 * C * C;
        double sum2 = 61 - 58 * T + T * T + 600 * C - 330 * eTickSquare;
        double sum3 = 1 - T + C;
        double sum4 = 5 - 18 * T + T * T + 72 * C - 58 * eTickSquare;

        double E = FE + k_0 * v * (A + sum3 * Math.pow(A, 3) / 6 + sum4 * Math.pow(A, 5) / 120);
        double N = FN + k_0 * (M - M_0 + v * Math.tan(toRadians(phi)) * ((A * A) / 2 +
                                                                         sum1 * Math.pow(A, 4) / 24 + sum2 * Math.pow(A, 6) / 720));

        /*System.out.println("eSquare is "+eSquare);
        System.out.println("A is "+A);
        System.out.println("T is "+T);
        System.out.println("C is "+C);
        System.out.println("M is "+M);
        System.out.println("M_0 is "+M_0);
        System.out.println("lamda is "+lamda);
        System.out.println("phi is "+phi);
        System.out.println("v is "+v);
        System.out.println("k_0 is "+k_0);*/



        Point2DDouble ret = new Point2DDouble(1);
        ret.ptsDouble[0] = N;
        ret.ptsDouble[1] = E;

        return ret;
    }

    //Transformation 3 for type 0
    private Point2DDouble LambertUTMNorthingEasting2ColumnRow(Point2DDouble pnt) {


        /*double x1 = ((inPixX*Math.abs(_columnResolution))+_eastingInsertionValue)/_radius;
        double y1 = ((inPixY*Math.abs(_rowResolution))-_northingInsertionValue)/(-_radius);*/

        double northing = pnt.ptsDouble[0];
        double easting = pnt.ptsDouble[1];

        //Rounding removed 10/28/03
        //double column = Math.round(((easting)-geoinfo.GetEastingInsertionValue())/Math.abs(geoinfo.GetColumnResolution()));
        //double row = Math.round((-(northing)+ geoinfo.GetNorthingInsertionValue())/Math.abs(geoinfo.GetRowResolution()));
        double column = ((easting) - geoinfo.GetEastingInsertionValue()) / Math.abs(geoinfo.GetColumnResolution());
        double row = (-(northing) + geoinfo.GetNorthingInsertionValue()) / Math.abs(geoinfo.GetRowResolution());

        Point2DDouble ret = new Point2DDouble(1);

        ret.ptsDouble[0] = column;
        ret.ptsDouble[1] = row;
        return ret;
    }

    //Transformation 3 for type 1
    private Point2DDouble StandardUTMNorthingEasting2ColumnRow(Point2DDouble pnt) {

        double northing = pnt.ptsDouble[0];
        double easting = pnt.ptsDouble[1];

        //rounding removed 10/28/03
        //double column = Math.round((easting - geoinfo.GetModelSpaceX())/
        //		geoinfo.GetGeoScaleX() + geoinfo.GetRasterSpaceI());
        //double row = Math.round((geoinfo.GetModelSpaceY() - northing)/
        //		Math.abs(geoinfo.GetGeoScaleY()) + geoinfo.GetRasterSpaceJ());
        double column = (easting - geoinfo.GetModelSpaceX()) /
                        geoinfo.GetGeoScaleX() + geoinfo.GetRasterSpaceI();
        double row = (geoinfo.GetModelSpaceY() - northing) /
                     Math.abs(geoinfo.GetGeoScaleY()) + geoinfo.GetRasterSpaceJ();

        Point2DDouble ret = new Point2DDouble(1);
        ret.ptsDouble[0] = column;
        ret.ptsDouble[1] = row;
        return ret;
    }

    //Transformation 4 for type 0
    private Point2DDouble LambertColumnRow2UTMNorthingEasting(Point2DDouble pnt) {

        double inPixCol = pnt.ptsDouble[0];
        double inPixRow = pnt.ptsDouble[1];

        double UTMnorthing = (-(inPixRow * Math.abs(geoinfo.GetRowResolution())) + geoinfo.GetNorthingInsertionValue());
        double UTMeasting = ((inPixCol * Math.abs(geoinfo.GetColumnResolution())) + geoinfo.GetEastingInsertionValue());

        Point2DDouble p = new Point2DDouble(1);
        p.ptsDouble[0] = UTMnorthing;
        p.ptsDouble[1] = UTMeasting;
        return p;

    }

    //Transformation 4 for type 1
    private Point2DDouble StandardColumnRow2UTMNorthingEasting(Point2DDouble pnt) {

        double inPixCol = pnt.ptsDouble[0];
        double inPixRow = pnt.ptsDouble[1];

        double UTMnorthing = geoinfo.GetModelSpaceY() -
                             ((inPixRow - geoinfo.GetRasterSpaceJ()) *
                              Math.abs(geoinfo.GetGeoScaleY()));

        double UTMeasting = (inPixCol - geoinfo.GetRasterSpaceI()) *
                            geoinfo.GetGeoScaleX() +
                            geoinfo.GetModelSpaceX();

        Point2DDouble ret = new Point2DDouble(1);
        ret.ptsDouble[0] = UTMnorthing;
        ret.ptsDouble[1] = UTMeasting;

        return ret;
    }

    //Transformation 5 for Type 0
    private Point2DDouble LambertColumnRow2LatLng(Point2DDouble pnt) {

        Point2DDouble ret = null;//new Point2DDouble(1);
        //System.err.println("LCR2LLthe column is "+pnt.ptsDouble[0]);
        //System.err.println("the row is "+pnt.ptsDouble[1]);
        ret = LambertColumnRow2UTMNorthingEasting(pnt);
        //System.err.println("the northing is "+ret.ptsDouble[0]);
        //System.err.println("the easting is "+ret.ptsDouble[1]);
        ret = LambertUTMNorthingEasting2LatLng(ret);
        //System.err.println("the latitude is "+ret.ptsDouble[0]);
        //System.err.println("the longitude is "+ret.ptsDouble[1]);

        return ret;
    }

    //Transformation 5 for Type 1
    private Point2DDouble StandardColumnRow2LatLng(Point2DDouble pnt) {

        Point2DDouble ret = null;//new Point2DDouble(1);
        ret = StandardColumnRow2UTMNorthingEasting(pnt);
        ret = StandardUTMNorthingEasting2LatLng(ret);

        return ret;
    }

    //Transformation 5 for Type 4
    private Point2DDouble DEMColumnRow2LatLng(Point2DDouble pnt) {
        Point2DDouble ret = null;
        //System.err.println("COLUMN is "+pnt.ptsDouble[0]);
        //System.err.println("ROW is "+pnt.ptsDouble[1]);
        double lat = 0;
        double lng = pnt.ptsDouble[0] * geoinfo.GetColumnResolution() +
                     geoinfo.GetEastingInsertionValue();

        if (geoinfo.GetRowResolution() > 0) {
            double distRow = numrows - pnt.ptsDouble[1];
            //System.err.println("DISTROW is "+distRow);
            lat = distRow * geoinfo.GetRowResolution() +
                  geoinfo.GetNorthingInsertionValue();
            //System.err.println("LAT is "+lat);
            //System.err.println("LNG is "+lng);
        } else {
            lat = geoinfo.GetNorthingInsertionValue() +
                  geoinfo.GetRowResolution() * pnt.ptsDouble[1];
        }

        ret = new Point2DDouble(1);
        ret.ptsDouble[0] = lat;
        ret.ptsDouble[1] = lng;
        return ret;
    }

    //Transformation 5 for Type 98
    private Point2DDouble TEST98ColumnRow2LatLng(Point2DDouble pnt) {
        Point2DDouble ret = null;

        double theRow = pnt.ptsDouble[1] + 4694;
        double theCol = pnt.ptsDouble[0] + 6786;

        double lat = theRow * geoinfo.GetRowResolution() +
                     geoinfo.GetNorthingInsertionValue();
        double lng = theCol * geoinfo.GetColumnResolution() +
                     geoinfo.GetEastingInsertionValue();

        ret = new Point2DDouble(1);
        ret.ptsDouble[0] = lat;
        ret.ptsDouble[1] = lng;
        return ret;

    }

    //Transformation 5 for Type 99
    private Point2DDouble TEST99ColumnRow2LatLng(Point2DDouble pnt) {
        Point2DDouble ret = null;

        double theRow = pnt.ptsDouble[1] + 4694;
        double theCol = pnt.ptsDouble[0] + 6786;

        double lat = theRow * geoinfo.GetRowResolution() +
                     geoinfo.GetNorthingInsertionValue();
        double lng = theCol * geoinfo.GetColumnResolution() +
                     geoinfo.GetEastingInsertionValue();

        ret = new Point2DDouble(1);
        ret.ptsDouble[0] = lat;
        ret.ptsDouble[1] = lng;
        return ret;
    }

    //Transformation 6 for Type 0
    private Point2DDouble LambertLatLng2ColumnRow(Point2DDouble pnt) {

        Point2DDouble ret = null;//new Point2DDouble(1);
        //System.err.println("LLL2CR");
        //System.err.println("the latitude is "+pnt.ptsDouble[0]);
        //System.err.println("the longitue is "+pnt.ptsDouble[1]);
        ret = LambertLatLng2UTMNorthingEasting(pnt);
        //System.err.println("the northing is "+ret.ptsDouble[0]);
        //System.err.println("the easting is "+ret.ptsDouble[1]);
        ret = LambertUTMNorthingEasting2ColumnRow(ret);
        //System.err.println("the column is "+ret.ptsDouble[0]);
        //System.err.println("the row is "+ret.ptsDouble[1]);

        return ret;
    }

    //Transformation 6 for Type 1
    private Point2DDouble StandardLatLng2ColumnRow(Point2DDouble pnt) {

        Point2DDouble ret = null;//new Point2DDouble(1);
        ret = StandardLatLng2UTMNorthingEasting(pnt);
        ret = StandardUTMNorthingEasting2ColumnRow(ret);

        return ret;
    }

    //Transformation 6 for Type 4
    private Point2DDouble DEMLatLng2ColumnRow(Point2DDouble pnt) {
        Point2DDouble ret = null;
        double distLat = pnt.ptsDouble[0] -
                         geoinfo.GetNorthingInsertionValue();
        // LAM ---- assumed northern hemisphere.  changed to check
        //        double distLat =
        //           geoinfo.GetNorthingInsertionValue()
        //           - pnt.ptsDouble[0];
        //System.out.println("\tDIST LAT: "+distLat+" "+geoinfo.GetNorthingInsertionValue());
        double distLng = pnt.ptsDouble[1] -
                         geoinfo.GetEastingInsertionValue();
        //        double distLng =
        //          geoinfo.GetEastingInsertionValue()
        //          - pnt.ptsDouble[1];
        //System.out.println("\tDIST LNG: "+distLat+" "+geoinfo.GetEastingInsertionValue());
        double pixelY = 0;
        //rounding removed 10/28/03
        //double pixelX = Math.round(distLng/
        //  geoinfo.GetColumnResolution());
        double pixelX = distLng /
                        geoinfo.GetColumnResolution();

        if (geoinfo.GetRowResolution() > 0) {
            //double pixelY = geoinfo.GetRasterSpaceJ() - Math.round(distLat/geoinfo.GetRowResolution());
            //pixelY = geoinfo.numrows -
            //              Math.round(distLat/geoinfo.GetRowResolution());
            pixelY = numrows -
                     distLat / geoinfo.GetRowResolution();
        } else {
            //pixelY = Math.round(distLat/geoinfo.GetRowResolution());
            pixelY = distLat / geoinfo.GetRowResolution();
        }


        ret = new Point2DDouble(1);
        ret.ptsDouble[0] = pixelX;
        ret.ptsDouble[1] = pixelY;
        return ret;
    }

    //Transformation 6 for Type 98
    private Point2DDouble TEST98LatLng2ColumnRow(Point2DDouble pnt) {
        Point2DDouble ret = null;

        Point2DDouble pnt2 = LambertLatLng2ColumnRow(pnt);

        double pixelX = pnt2.ptsDouble[0];
        double pixelY = pnt2.ptsDouble[1];

        pixelX = pixelX - 6756;
        pixelY = pixelY - 4654;

        ret = new Point2DDouble(1);
        ret.ptsDouble[0] = pixelX;
        ret.ptsDouble[1] = pixelY;
        return ret;
    }

    //Transformation 6 for Type 99
    private Point2DDouble TEST99LatLng2ColumnRow(Point2DDouble pnt) {
        Point2DDouble ret = null;

        double distLat = pnt.ptsDouble[0] -
                         geoinfo.GetNorthingInsertionValue();

        double distLng = pnt.ptsDouble[1] -
                         geoinfo.GetEastingInsertionValue();

        double pixelY = Math.round(distLat /
                                   geoinfo.GetRowResolution());
        double pixelX = Math.round(distLng /
                                   geoinfo.GetColumnResolution());

        pixelX = pixelX - 6786;
        pixelY = pixelY - 4694;

        ret = new Point2DDouble(1);
        ret.ptsDouble[0] = pixelX;
        ret.ptsDouble[1] = pixelY;
        return ret;
    }


    //////////////////////////////////////////////////////////////////
    //conversion utilities
    //////////////////////////////////////////////////////////////////
    //degrees to decimal and back
    //////////////////////////////////////////////////////////////////
    public double DegMinSecToDecim(double deg, double min, double sec) {
        int sign;
        if (deg >= 0)
            sign = 1;
        else
            sign = -1;
        return ((Math.abs(deg) + Math.abs(min) / 60.0 + Math.abs(sec) / 3600.0) * sign);
    }

    public Point3DDouble DecimToDegMinSec(double decim) {
        double frac, temp;
        int sign;
        if (decim >= 0)
            sign = 1;
        else
            sign = -1;
        Point3DDouble res = new Point3DDouble(3);
        // res.ptsDouble[0] = degrees, res.ptsDouble[1] = minutes, res.ptsDouble[2]= seconds
        res.ptsDouble[0] = res.ptsDouble[1] = res.ptsDouble[2] = -1.0;

        // degrees
        //frac = modf(fabs(decim),res.ptsDouble[0]);
        res.ptsDouble[0] = Math.floor(Math.abs(decim));
        frac = Math.abs(decim) - res.ptsDouble[0];
        res.ptsDouble[0] *= sign;
        temp = frac * 60.0;

        // minutes
        //frac = modf(temp,res.ptsDouble[1]);
        res.ptsDouble[1] = Math.floor(Math.abs(temp));
        frac = Math.abs(temp) - res.ptsDouble[1];
        res.ptsDouble[1] *= sign;

        // seconds
        res.ptsDouble[2] = frac * 60.0 * sign;

        return res;
    }

    ///////////////////////////////////////////////////////////////////////////////
    // conversion of pixel coordinates and latitude, longitude pairs.
    public Point2DDouble InterpolateLatLngToColumnRow(Point2DDouble latlong) {
        return InterpolateLatLngToColumnRow(latlong.ptsDouble[0], latlong.ptsDouble[1]);
    }

    public Point2DDouble InterpolateLatLngToColumnRow(double coordinateLat, double coordinateLon) {
        //public Point2DDouble UTMToRowCol( double coordinateLat, double coordinateLon) {
        // Note : LON = X = 0 & LAT = Y = 1
        Point2DDouble res = new Point2DDouble(1);
        // res.ptsDouble[0] = row, res.ptsDouble[1] = column

        // to be verified by T.J. Peter
        res.ptsDouble[0] = (coordinateLon - geoinfo.GetMaxWestLng()) / geoinfo.GetColumnResolution();//_interval[LON];// column
        res.ptsDouble[1] = (coordinateLat - geoinfo.GetMaxSouthLat()) / geoinfo.GetRowResolution();//_interval[LAT];// row
        return res;
    }

    public Point2DDouble InterpolateColumnRowToLatLng(Point2DDouble columnrow) {
        return InterpolateColumnRowToLatLng(columnrow.ptsDouble[0], columnrow.ptsDouble[1]);
    }

    public Point2DDouble InterpolateColumnRowToLatLng(double col, double row) {
        // Note : LON = X = 0 & LAT = Y = 1
        Point2DDouble res = new Point2DDouble(1);
        // res.ptsDouble[0] = latitude, res.ptsDouble[1] = longitude

        // to be verified by T.J. Peter
        res.ptsDouble[0] = row * geoinfo.GetRowResolution() + geoinfo.GetMaxSouthLat();// latitude
        res.ptsDouble[1] = col * geoinfo.GetColumnResolution() + geoinfo.GetMaxWestLng(); // longitude
        return res;
    }

    /////////////////////////////////////////////////////////
    // additional conversion
    public static double toRadians(double a) {

        return ((Math.PI * a) / 180);
    }

    public static double toDegrees(double a) {
        return ((180 * a) / Math.PI);
    }

    public static double atanh(double a) {

        return (.5 * Math.log((1 + a) / (1 - a)));
    }

    public double getESquared() {
        double ecc = -1;

        if (geoinfo.GetEccentricitySqrd() == -1) {
            if ((geoinfo.GetMajorAxis() != -1) && (geoinfo.GetMinorAxis() != -1)) {
                double flattening = (geoinfo.GetMajorAxis() - geoinfo.GetMinorAxis()) / geoinfo.GetMajorAxis();
                ecc = 2 * flattening - flattening * flattening;
                geoinfo.SetEccentricitySqrd(ecc);
            } else {
                if (geoinfo.GetInverseFlat() != -1) {
                    ecc = (1 / geoinfo.GetInverseFlat()) * 2 -
                          Math.pow((1 / geoinfo.GetInverseFlat()), 2);
                    geoinfo.SetEccentricitySqrd(ecc);
                } else {
                    System.out.println("Couldn't find ellipsoid specification.");
                    System.out.println("Guessing WGS84");
                    geoinfo.SetEllipsoidName("WGS84");
                    ecc = (1 / geoinfo.GetInverseFlat()) * 2 -
                          Math.pow((1 / geoinfo.GetInverseFlat()), 2);
                    geoinfo.SetEccentricitySqrd(ecc);
                }
            }
        } else {
            ecc = geoinfo.GetEccentricitySqrd();
        }
        return ecc;

    }

    /**
     * Maps any longitude value to its corresponding UTM zone. Ranges are well
     * defined by making western boundary of a zone non-inclusive: Ex:  UTM zone
     * 17:  78W to 83.99999... W UTM zone 16: 84W to 91.99999... W, etc. The
     * function is a transformation of the simple step function y = floor( x ).
     * The transformation is y = -floor[ ((-x) - 78)/6 ] + 17.
     *
     * @param lng the longitude value to map to a zone
     * @return int zone the zone of that longitude value
     */


    public static int determineUTMZone(double lng) {
        //we assume that longitude values west of Greenwich meridian < 0
        double numerator = (-lng) - 78;
        double flr = Math.floor(numerator / 6);
        int zone = (int) (-flr + 17);
        return zone;
    }

    public void attemptSetScale(double a) {

        if (geoinfo.GetModelType() != -1) {

            switch (geoinfo.GetModelType()) {
                case 0:	//Lambert model; scale factor not used
                    geoinfo.SetScaleFactor(-1);
                    break;
                case 1:	//Northern Hemisphere UTM
                    geoinfo.SetScaleFactor(.9996);
                    break;
                case 2: //Southern Hemisphere UTM
                    geoinfo.SetScaleFactor(.9996);
                    break;
                case 3: //OSGB 1936
                    geoinfo.SetScaleFactor(.9996013);
                    break;
                default:
                    geoinfo.SetScaleFactor(a);
                    break;

            }
        } else {
            if (geoinfo.GetScaleFactor() == -1)
                geoinfo.SetScaleFactor(a);

        }

        return;
    }


}
