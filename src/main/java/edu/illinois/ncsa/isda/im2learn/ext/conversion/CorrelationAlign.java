package edu.illinois.ncsa.isda.im2learn.ext.conversion;

import java.util.*;
import javax.media.jai.*;

import java.awt.Point;
import java.awt.geom.*;
import java.awt.image.renderable.*;

import org.apache.commons.logging.*;

import edu.illinois.ncsa.isda.im2learn.core.datatype.*;
import edu.illinois.ncsa.isda.im2learn.core.io.jai.*;

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
public class CorrelationAlign {
  static private Log logger = LogFactory.getLog(CorrelationAlign.class);

  /** LAM -- these probably do not need to be fields */
//  private ImageObject _imA, _imB;
//  private int _radW, _radH;
//  private ImPoint[] _tgtPts, _srcPts, _modSrcPts;
  private Point2D[] _modSrcPts;

//  private Registration _reg = null;
//  ImInout _io = new ImInout();
//  private SubArea _sub = new SubArea();
//  private AffineTransform[] _afx;

  private int _step = 1;

//  String _logFilename = "log.txt";
//  private boolean _realtimeUpdate = false, _showUpate = false;

  private boolean _booleanImage = false;
//  private boolean _verborse = false, _booleanImage = false;
//      _useJamaRegistration = false;
//  ImageFrame f = null;
//  public String[] _filenamesA, _filenamesB;

  private double _maxCorrelation;
  private double[] _maxCorrelationArray;
  private int[] _delta;
  private AffineTransform _afx;
  
  
//  public CorrelationAlign() {}

  /**
   *
   * @param imA
   * @param imB
   * @param tgtPts
   * @param srcPts
   * @param searchSpaceW
   * @param searchSpaceH
   * @param log
   * @param band
   * @return
   */
/*  public void runSearch(ImageObject imA, ImageObject imB, Point2D[] tgtPts,
                        Point2D[] srcPts, int searchSpaceW, int searchSpaceH,
                        String log, int band) throws ImageException {

    int[] b = {
        band};

    ImageObject _imA = imA.extractBand(b);

    ImageObject _imB = imB.extractBand(b);

    //_srcPts = srcPts;
    //_tgtPts = tgtPts;

    int radW = (int) ( (float) searchSpaceW / 2);

    int radH = (int) ( (float) searchSpaceH / 2);

//      _logFilename = log;

    //search(_imA, _imB, _tgtPts, _srcPts, _radW, _radH, _logFilename);
    // DC 4.7
    search(_imA, _imB, tgtPts, srcPts, radW, radH , _logFilename);

    /*      return true;
        }
        catch (Exception e) {
          return false;
        }*/
  //}

/*  public void runFrameIterativeSearch(ImageObject[] imA, ImageObject[] imB,
                                      Point2D[] tgtPts, Point2D[] srcPts,
                                      int searchSpaceW, int searchSpaceH,
                                      String log, int band) throws
      ImageException {

    int[][] delta = new int[imB.length][6];

    double[] corrlation = new double[imB.length];

    int[] b = {band};

    //Calendar cal = Calendar.getInstance();

    //String msg;

// DC 4.7
    // _srcPts = srcPts;

// DC 4.7
    //_tgtPts = tgtPts;

    int radW = (int) ( (float) searchSpaceW / 2);

    int radH = (int) ( (float) searchSpaceH / 2);

    for (int i = 0; i < imA.length; i++) {

      for (int j = 0; j < imB.length; j++) {

        //_logFilename = log;

        /*msg = "===============================\n" +
            "Comparison = Image A (" + i + ") <==> (" + j + ")";
                 System.out.println(msg);*/

// DC 4.6
//        IOTool.writeString(_logFilename, msg, true);

        // DC 4.7
        //_imA = imA[i].CopySelectedBands(imA[i], b, 1);
/*        ImageObject _imA = imA[i].extractBand(b);

        // DC 4.7
        //_imB = imB[j].CopySelectedBands(imB[j], b, 1);
        ImageObject _imB = imB[j].extractBand(b);

        //  new ImageFrame(_imA, null, new Point(0, 0), "Image1");

        //  new ImageFrame(_imB, null, new Point(0, 0), "Image1");

        boolean _continue = true;

        while (_continue) {

// DC 4.7
          //_delta = delta[i] = search(_imA, _imB, _tgtPts, _srcPts, _radW, _radH,
          _delta = delta[i] = search(_imA, _imB, tgtPts, srcPts, radW,
                                     radH ,
                                     _logFilename);

//            System.out.println(_delta[0] + " "+_delta[1] + " "+_delta[2] + " "+

          //                              _delta[3] + " "+_delta[4] + " "+_delta[5]);

/*          if (Math.abs(_delta[0]) == (int) ( (float) searchSpaceW / 2) ||

              Math.abs(_delta[1]) == (int) ( (float) searchSpaceW / 2) ||

              Math.abs(_delta[2]) == (int) ( (float) searchSpaceW / 2) ||

              Math.abs(_delta[3]) == (int) ( (float) searchSpaceW / 2) ||

              Math.abs(_delta[4]) == (int) ( (float) searchSpaceW / 2) ||

              Math.abs(_delta[5]) == (int) ( (float) searchSpaceW / 2)) {

            /*            _srcPts[0].x += _delta[0];
                        _srcPts[0].y += _delta[1];
                        _srcPts[1].x += _delta[2];
                        _srcPts[1].y += _delta[3];
                        _srcPts[2].x += _delta[4];
                        _srcPts[2].y += _delta[5];*/
/*            srcPts[0].x += _delta[0];

            srcPts[0].y += _delta[1];

            srcPts[1].x += _delta[2];

            srcPts[1].y += _delta[3];

            srcPts[2].x += _delta[4];

            srcPts[2].y += _delta[5];
          }

          else {
            _continue = false;
          }
        }

//        try {

//          _logFilename = log + ".summary.txt";

        //   for (int k = 0; k < imB.length; k++) {

// DC 4.6
        /*          msg = "**************************\n" +
                      "Comparison: (" + IOTool.getFilename(_filenamesA[i]) +
                      ") <==> (" + IOTool.getFilename(_filenamesB[j]) +
                      ")\n" +
                      cal.getTime().toString() +
                      " " +
                      "\nMax Correlation = " + this._maxCorrelation;
                  System.out.println(msg);
                  IOTool.writeString(_logFilename, msg, true);*/

        //  }

        /*        }
                catch (Exception e) {
                  System.err.println("Log output error");
                }*/

/*      }

    }

//    return true;

  }*/


  public AffineTransform getAffineTransform() {
	  return _afx;
  }
  	
/**
 *
 * @return
 */
  public int[] getDelta() {
    return _delta;
  }

  /**
   *
   * @return
   */
  public Point2D[] getModifiedPts() {
    return this._modSrcPts;
  }

  /**
   *
   * @return
   */
  public double getMaxCorrelation() {
    return _maxCorrelation;
  }


/*  public void runFrameSearch(ImageObject[] imA, ImageObject[] imB,
                             Point2D[] tgtPts, Point2D[] srcPts,
                             int searchSpaceW, int searchSpaceH, String log,
                             int band) throws ImageException {

    //   try {

    int[][] delta = new int[imB.length][6];

    double[] corrlation = new double[imB.length];

    int[] b = {band};

    //Calendar cal = Calendar.getInstance();

    //String msg;

    //srcPts = srcPts;
    //tgtPts = tgtPts;

    int radW = (int) ( (float) searchSpaceW / 2);

    int radH = (int) ( (float) searchSpaceH / 2);

    for (int i = 0; i < imA.length; i++) {

      for (int j = 0; j < imB.length; j++) {

        //_logFilename = log;

        /*msg = "===============================\n" +
            "Comparison = Image A (" + i + ") <==> (" + j + ")";
                   System.out.println(msg);*/

// DC 4.6
//          IOTool.writeString(_logFilename, msg, true);

// DC 4.7
        //_imA = imA[i].CopySelectedBands(imA[i], b, 1);
/*        ImageObject _imA = imA[i].extractBand(b);

// DC 4.7
        //_imB = imB[j].CopySelectedBands(imB[j], b, 1);
        ImageObject _imB = imB[j].extractBand(b);

// DC 4.7
        //_delta = delta[i] = search(_imA, _imB, _tgtPts, _srcPts, _radW, _radH,
        _delta = delta[i] = search(_imA, _imB, tgtPts, srcPts, radW, radH ,
                                   _logFilename);

//          try {

        //_logFilename = log + ".summary.txt";

        //   for (int k = 0; k < imB.length; k++) {

// DC 4.6
        /*            msg = "**************************\n" +
             "Comparison: (" + IOTool.getFilename(_filenamesA[i]) +
                        ") <==> (" + IOTool.getFilename(_filenamesB[j]) +
                        ")\n" +
                        cal.getTime().toString() +
                        " " +
                        "\nMax Correlation = " + this._maxCorrelation;
                    System.out.println(msg);
                    IOTool.writeString(_logFilename, msg, true);
         */
        //  }

        /*          }
                  catch (Exception e) {
                    System.err.println("Log output error");
                  }*/

/*      }

    }

    //return true;

    /*    }
        catch (Exception e) {
          return false;
        }*/

//  }


/**
 *
 * @param imA
 * @param imB
 * @param tgtPts
 * @param srcPts
 * @param band
 * @throws ImageException
 */
  public void runPyramidSearch(ImageObject imA, ImageObject imB,
                               /*Point2D[] tgtPts,*/ Point2D[] srcPts,
                               //int searchSpaceW, int searchSpaceH,
                               /*String log,*/ int bandA, int bandB, int radius) throws ImageException {

//    try {

    int[] bA = {bandA};
    int[] bB = {bandB};

// DC 4.7
    //_imA = imA.CopySelectedBands(imA, b, 1);
    // extract the one band from image A
    
    ImageObject _imA = imA.getNumBands() == 1? imA : imA.extractBand(bA);

// DC 4.7
    //_imB = imB.CopySelectedBands(imB, b, 1);
    // extract the one band from image B
    ImageObject _imB = imB.getNumBands() == 1? imB : imB.extractBand(bB);

// DC 4.7
// unused
    //srcPts = srcPts;
    //tgtPts = tgtPts;

    //int _radW = (int) ( (float) searchSpaceW / 2);
    //int _radH = (int) ( (float) searchSpaceH / 2);

    //_logFilename = log;

// DC 4.6
//      IOTool.writeString(_logFilename, "******************", true);

    
    // default setting as image boundary when feature points are not given
    if(srcPts == null) {
    	srcPts = new Point2D[3];
    	srcPts[0] = new Point(0,0);
    	srcPts[1] = new Point(imB.getNumCols(),0);
    	srcPts[2] = new Point(imB.getNumCols(), imB.getNumRows());
      }
    
    // call pyramid search
    pyramidSearch(_imA, _imB, /*tgtPts,*/ srcPts, radius);

//      return true;

    /*    }
        catch (Exception e) {
          return false;
        }*/

  }

/*  public double ComputeCorrelation(ImageObject imA, ImageObject imB,
                                   Point2D[] tgtPts, Point2D[] srcPts, int band) throws
      ImageException {

//    try {

    int[] b = {
        band};

// DC 4.7
    //_imA = imA.CopySelectedBands(imA, b, 1);
    ImageObject _imA = imA.extractBand(b);

// DC 4.7
    //_imB = imB.CopySelectedBands(imB, b, 1);
    ImageObject _imB = imB.extractBand(b);

// DC 4.7
    //srcPts = srcPts;

// DC 4.7
    //_tgtPts = tgtPts;

    //   new ImageFrame(_imA, new Point(0, 0), "A");

    //    new ImageFrame(_imB, new Point(0, 0), "B");

    ImageObject transIm;

    if (tgtPts == null || srcPts == null) {

      //transIm = _imB.CopyImageObject();
      transIm = null;
      try {
        transIm = (ImageObject) _imB.clone();
      }
      catch (CloneNotSupportedException cnse) {
        // this should never happen
      }

    }

    else {

      //Registration reg = new Registration(_tgtPts, _srcPts);
      Registration reg = new Registration(tgtPts, srcPts);
//        if (this._useJamaRegistration) {
      // DC 4.7
//          transIm = ImageTool.transform(_imB, _reg.getAffineTransformJama(),
//                                        _imA.getNumCols(), _imA.getNumRows());
//        }
//        else {
      transIm = transform(_imB, reg.getAffineTransform(),
                          _imA.getNumCols(), _imA.getNumRows());
//        }

    }

    //   new ImageFrame(transIm, null, new Point(0, 0), "TR");

    double corr1;

    if (this._booleanImage) {

      corr1 = normCorrelationBinary(_imA, transIm);

    }

    else {

      corr1 = normCorrelation(_imA, transIm);

    }

    //    double[][] normCorrParam = this.computeNormCorrParameter(_imA); // for speedup

    //   double corr2 = this.normCorrelationToA(transIm, normCorrParam[1],normCorrParam[0][0]);

    //System.out.println("Correlation = " + corr1); //  + "  "+corr2);

    return corr1;

    /*    }
        catch (Exception e) {
          return Double.NaN;
        }*/
//  }


/**
 *
 * @param imA
 * @param imB
 * @param tgtPts
 * @param srcPts
 * @throws ImageException
 */
  private void pyramidSearch(ImageObject imA, ImageObject imB, /*Point2D[] tgtPts,*/
                     Point2D[] srcPts, int radius) throws ImageException {

    //System.out.println("Pyramid Search");

    // new ImageFrame(_imA, new Point(0, 0), "A");

    // these are the sampling rates
//    int[] fac = {16, 8, 4, 2, 1};
//    int[] range = {2, 2, 2, 2, 2};

    int[] range = {radius,radius,radius,radius,radius};
    int[] fac = new int[range.length];
    int pow = range.length-1;
    for(int i = 0; i < range.length; i++) {
      fac[i] = (int) (Math.pow(range[i], pow));
      pow--;
    }

//    DownSamp down = new DownSamp();

    //ImageObject downImA, downImB;

// DC 4.7
    //ImPoint[] downTgtPts = new ImPoint[_tgtPts.length];
    Point2D[] downTgtPts = new Point2D[srcPts.length];

// DC 4.7
//    ImPoint[] downSrcPts = new ImPoint[_srcPts.length];
    Point2D[] downSrcPts = new Point2D[srcPts.length];

    // initialize all downTgtPts
    for (int i = 0; i < downTgtPts.length; i++) {
      downTgtPts[i] = new Point2D.Double();
    }

    // initialize all downSrcPts
    for (int i = 0; i < downSrcPts.length; i++) {
      downSrcPts[i] = new Point2D.Double();
    }

    //int col, row;
    // int[] d = null;

    // for each subsampling rate
    for (int k = 0; k < fac.length; k++) {
      if(fac[k] > imA.getNumCols() || fac[k] > imA.getNumRows())
        continue;

      // downsample imageA
      ImageObject downImA;

      // downsample imageB
      ImageObject downImB;

      if(fac[k] == 0) {
        downImA = imA;
        downImB = imB;
      }
      else {
        downImA = imA.scale(1.0 / fac[k]);
        downImB = imB.scale(1.0 / fac[k]);
      }

      // for each downTgtPts
      for (int i = 0; i < downTgtPts.length; i++) {
        // scale its location
        if(fac[k] != 0)
        downTgtPts[i].setLocation(srcPts[i].getX() / (float) fac[k],
                                  srcPts[i].getY() / (float) fac[k]);
        else
        downTgtPts[i].setLocation(srcPts[i].getX(),
                                  srcPts[i].getY());
      }

      // for each downSrcPts
      for (int i = 0; i < downSrcPts.length; i++) {
        // scale its location
        if(fac[k] != 0)
        downSrcPts[i].setLocation(srcPts[i].getX() / (float) fac[k],
                                  srcPts[i].getY() / (float) fac[k]);
        else
        downSrcPts[i].setLocation(srcPts[i].getX(),
                                  srcPts[i].getY());
      }

      // call search
      _delta = search(downImA, downImB, downTgtPts, downSrcPts, range[k],
                      range[k] /*, _logFilename*/);

      // LAM --- what is going on here
      for (int i = 0; i < downSrcPts.length; i++) {
        int dx = _delta[i * 2] * fac[k];
        int dy = _delta[i * 2 + 1] * fac[k];
        srcPts[i].setLocation(srcPts[i].getX() + dx, srcPts[i].getY() + dy);
      }
    }

    _modSrcPts = new Point2D[srcPts.length];
    for (int i = 0; i < _modSrcPts.length; i++) {
      _modSrcPts[i] = new Point2D.Double((float)srcPts[i].getX(), (float)srcPts[i].getY());
    }
  }

 /**
  *
  * @param imA
  * @param imB
  * @param tgtPts
  * @param srcPts
  * @param radW
  * @param radH
  * @return
  */
  private int[] search(ImageObject imA, ImageObject imB, Point2D[] tgtPts,
                       Point2D[] srcPts, int radW,
                       int radH /*, String logFilename*/) {

    Point2D[] searchSrcPts = new Point2D[srcPts.length];
    //ImageObject transIm;
    double corr = 0, maxCorr = Double.NEGATIVE_INFINITY;
    int[] delta = new int[tgtPts.length * 2];
    //int maxi1 = 0, maxj1 = 0, maxi2 = 0, maxj2 = 0, maxi3 = 0, maxj3 = 0;
    //String message;
    //Calendar cal = Calendar.getInstance();

    for (int i = 0; i < searchSrcPts.length; i++) {
      searchSrcPts[i] = new Point2D.Double();
    }

//    ImageTool progress = new ImageTool();
//    progress.showProgressBar("adjusting",(int) Math.pow( (double) ( (int) ( (2 * radW + 1) / _step + 0.5)), 4));

    //int cnt = 0;
    double[][] normCorrParam = this.computeNormCorrParameter(imA); // for speedup

    int negRadW = -1 * radW;
    int negRadH = -1 * radH;
    int radWPlusOne = radW + 1;
    int radHPlusOne = radH + 1;
    //int negRadW = 0;
    //int negRadH = 0;
    //int radWPlusOne = 1;
    //int radHPlusOne = 1;

    // for testing, do not vary the location of searchSrcPts

    for (int i1 = negRadW; i1 < radWPlusOne; i1 += _step) {
      for (int j1 = negRadH; j1 < radHPlusOne; j1 += _step) {
        for (int i2 = negRadW; i2 < radWPlusOne; i2 += _step) {
          for (int j2 = negRadH; j2 < radHPlusOne; j2 += _step) {
//            progress.setProgress(cnt++);
            for (int i3 = negRadW; i3 < radWPlusOne; i3 += _step) {
              for (int j3 = negRadH; j3 < radHPlusOne; j3 += _step) {
                //searchSrcPts[0].x = srcPts[0].getX() + /*(double)*/ i1;
                //searchSrcPts[0].y = srcPts[0].getY() + /*(double)*/ j1;
                searchSrcPts[0].setLocation(srcPts[0].getX()+i1, srcPts[0].getY()+j1);

                //searchSrcPts[1].x = srcPts[1].getX() + /*(double)*/ i2;
                //searchSrcPts[1].y = srcPts[1].getY() + /*(double)*/ j2;
                searchSrcPts[1].setLocation(srcPts[1].getX()+i2, srcPts[1].getY()+j2);

                //searchSrcPts[2].x = srcPts[2].getX() + /*(double)*/ i3;
                //searchSrcPts[2].y = srcPts[2].getY() + /*(double)*/ j3;
                searchSrcPts[2].setLocation(srcPts[2].getX()+i3, srcPts[2].getY()+j3);

                Registration reg = new Registration(tgtPts, searchSrcPts);

//                if (this._useJamaRegistration) {
//                  transIm = ImageTool.transform(imB, _reg.getAffineTransformJama(),
//                                                imA.getNumCols(), imA.getNumRows());
//                }
//                else {
                ImageObject transIm = null;

/*                try {
                  transIm = transform(imB, reg.getAffineTransform(),
                                      imA.getNumCols(),
                                      imA.getNumRows());
                }
                catch(Exception ex) {
                  System.out.println("AFX: "+reg.getAffineTransform());
                  System.out.println(imA.getNumCols());
                  System.out.println(imA.getNumRows());
                  ex.printStackTrace();
                }
//                }

/*                if (_booleanImage) {
                  corr = CorrelationAlign.normCorrelationBinary(imA, transIm);
                }
                else {
                  corr = CorrelationAlign.normCorrelationToA(transIm, normCorrParam[1],
                                                 normCorrParam[0][0]);
                }*/

                corr = CorrelationAlign.normCorrelation(imA, imB, reg.getAffineTransform());
//System.out.println("CORR: "+corr);

                /*if (_verborse) {
                  System.out.print("." + corr);
                                 }*/
                if (!Double.isNaN(corr)) {
                  if (corr > maxCorr) {
                    maxCorr = corr;
/*                    maxi1 = i1;
                    maxj1 = j1;
                    maxi2 = i2;
                    maxj2 = j2;
                    maxi3 = i3;
                    maxj3 = j3;

                    String message = "\n--------------------------\n" +
                        cal.getTime().toString();
                                         message += "\nDelta =";
                         message += maxi1 + "," + maxj1 + "," + maxi2 + "," + maxj2 +
                        "," + maxi3 + "," + maxj3 + " " +
                        "\nTarget          = ";
                         for (int i = 0; i < srcPts.length; i++) {
                      message += "(" + tgtPts[i].getX() + "," + tgtPts[i].getY() + ")";
                                         }
                                         message += "\nModified Source = ";
                         for (int i = 0; i < searchSrcPts.length; i++) {
                      message += "(" + searchSrcPts[i].getX() + "," +
                          searchSrcPts[i].getY() + ")";
                                         }
                         message += "\nCorrelation     = " + maxCorr;*/

                    this._maxCorrelation = maxCorr;
                    this._afx = reg.getAffineTransform();
                    //delta[0] = maxi1;
                    //delta[1] = maxj1;
                    //delta[2] = maxi2;
                    //delta[3] = maxj2;
                    //delta[4] = maxi3;
                    //delta[5] = maxj3;
                    delta[0] = i1;
                    delta[1] = j1;
                    delta[2] = i2;
                    delta[3] = j2;
                    delta[4] = i3;
                    delta[5] = j3;

//                                       System.out.print(message);

// DC 4.6
//                    IOTool.writeString(logFilename, message, true);

// DC 4.7
                    /*                    if (this._showUpate) {
                                          if (f == null) {
                         f = new ImageFrame(transIm, null, new Point(0, 0),
                         String.valueOf(maxCorr) + " || " +
                         maxi1 + " " + maxj1 + " " + maxi2 +
                         " " + maxj2 + " " + maxi3 + " " +
                                                               maxj3);
                                          }
                                          else {
                         f.setTitle(String.valueOf(maxCorr) + " || " + maxi1 +
                         " " + maxj1 + " " + maxi2 + " " + maxj2 +
                         " " + maxi3 + " " + maxj3);
                                            f.updateImage(transIm, "", null);
                                          }
                                        }*/
                  }
                }
// DC 4.7
                /*if (_realtimeUpdate) {
                  if (f == null) {
                    f = new ImageFrame(transIm, new Point(0, 0), "");
                  }
                  else {
                    f.updateImage(transIm, "", null);
                  }
                                 } */
              }
            }
          }
        }
      }
    } // outermost for loop

    //progress.closeProgressBar();
    // DialogBox.displayMessage("Computation done");
    return delta;
  }

  /**
   *
   * @param A
   * @return
   */
  private static double[][] computeNormCorrParameter(ImageObject A) {
    int length = A.getSize();
    double[][] retArr = new double[2][];

    double avg;

    retArr[0] = new double[1]; //  retArr[0][0] = term2

    retArr[1] = new double[length];

    // compute average A

    double sum = 0;
    for (int i = 0; i < length; i++) {
      sum += unsignedByte2Int(A.getByte(i));
    }

    avg = sum / (double) length;

    //compute normA[] and term2
    for (int i = 0; i < length; i++) {
      retArr[1][i] = (double) unsignedByte2Int(A.getByte(i)) - avg;

      retArr[0][0] += retArr[1][i] * retArr[1][i];
    }

    return retArr;
  }

  /**
   *
   * @param B
   * @param normA
   * @param term2
   * @return
   */
  private static double normCorrelationToA(ImageObject B, double[] normA,
                                           double term2) {
    int length = normA.length;

    double sumB = 0;

    double avgB;

    for (int i = 0; i < length; i++) {
      sumB += unsignedByte2Int(B.getByte(i));
    }

    avgB = sumB / (double) length;

    double term1 = 0, term3 = 0; // term1/(term2*term3)^0.5

    double normB;

    for (int i = 0; i < length; i++) {
      normB = (double) unsignedByte2Int(B.getByte(i)) - avgB;

      term1 += normA[i] * normB;

      term3 += normB * normB;
    }

    return Math.abs(term1 / Math.sqrt(term2 * term3));
  }

  /**
   *
   * @param A
   * @param B
   * @return
   */
  private static double normCorrelation(ImageObject A, ImageObject B) {

    // add AFX



    int length = A.getSize();

    double sumA = 0, sumB = 0;

    double avgA, avgB;

    for (int i = 0; i < length; i++) {
      sumA += unsignedByte2Int(A.getByte(i));

      //
      sumB += unsignedByte2Int(B.getByte(i));
    }

    avgA = sumA / (double) length;

    avgB = sumB / (double) length;

    double term1 = 0, term2 = 0, term3 = 0; // term1/(term2*term3)^0.5

    double normA, normB;

    for (int i = 0; i < length; i++) {
      normA = (double) unsignedByte2Int(A.getByte(i)) - avgA;

      normB = (double) unsignedByte2Int(B.getByte(i)) - avgB;

      term1 += normA * normB;

      term2 += normA * normA;

      term3 += normB * normB;
    }

    return Math.abs(term1 / Math.sqrt(term2 * term3));
  }

  private static double normCorrelation(ImageObject A, ImageObject B, AffineTransform afx) {
        // add AFX

        //int length = A.getSize();
        int length = 0;

        double sumA = 0, sumB = 0;

        double avgA, avgB;

        /*for (int i = 0; i < length; i++) {
          sumA += unsignedByte2Int(A.getByte(i));

          //
          sumB += unsignedByte2Int(B.getByte(i));
        }*/

        int numcolsB = B.getNumCols();
        int numrowsB = B.getNumRows();

        int numcolsA = A.getNumCols();
        int numrowsA = A.getNumRows();

        Point2D src = new Point2D.Double();
        Point2D dest = new Point2D.Double();

        for(int col = 0; col < numcolsB; col++) {
          for(int row = 0; row < numrowsB; row++) {
            src.setLocation(col, row);
            dest = afx.transform(src, dest);

            if(dest.getX() >= numcolsA || dest.getY() >= numrowsA)
              continue;
            if(dest.getX() < 0 || dest.getY() < 0)
              continue;

            sumA +=
                  unsignedByte2Int(A.getByte( (int) dest.getY(), (int) dest.getX(),
                                             0));
            sumB +=
                  unsignedByte2Int(B.getByte( (int) src.getY(), (int) src.getX(), 0));
            length++;
          }
        }


        avgA = sumA / (double) length;

        avgB = sumB / (double) length;

        double term1 = 0, term2 = 0, term3 = 0; // term1/(term2*term3)^0.5

        double normA, normB;

/*        for (int i = 0; i < length; i++) {
          normA = (double) unsignedByte2Int(A.getByte(i)) - avgA;

          normB = (double) unsignedByte2Int(B.getByte(i)) - avgB;

          term1 += normA * normB;

          term2 += normA * normA;

          term3 += normB * normB;
        }*/

        for (int col = 0; col < numcolsB; col++) {
          for (int row = 0; row < numrowsB; row++) {
            src.setLocation(col, row);
            dest = afx.transform(src, dest);

            if (dest.getX() >= numcolsA || dest.getY() >= numrowsA)
              continue;
            if (dest.getX() < 0 || dest.getY() < 0)
              continue;

            normA = (double) unsignedByte2Int(A.getByte( (int) dest.getY(),
              (int) dest.getX(), 0)) - avgA;
            normB = (double) unsignedByte2Int(B.getByte( (int) src.getY(),
              (int) src.getX(), 0)) - avgB;

            term1 += normA * normB;

            term2 += normA * normA;

            term3 += normB * normB;

    //sumA += A.getByte((int)dest.getY(), (int)dest.getX(), 0);
    //sumB += B.getByte((int)src.getY(), (int)src.getX(), 0);
    //length++;
          }
        }
//System.out.println("Term 1: "+term1+" "+term2+" "+term3);
        double denom = Math.sqrt(term2 * term3);
        if(denom == 0)
          return Double.NaN;

        return Math.abs(term1 / denom);
  }

  /**
   *
   * @param A
   * @param B
   * @return
   */
  private static double normCorrelationBinary(ImageObject A, ImageObject B) {

    //int length = A.image.length;
    int length = A.getSize();

    long sumA = 0, sumB = 0, AandB = 0;

    int vA, vB;

    double avgA, avgB;

    for (int i = 0; i < length; i++) {

      /* vA = (A.image[i] & 0xff);;
       vB = (B.image[i] & 0xff);*/

      vA = (A.getByte(i) != 0) ? 1 : 0;

      vB = (B.getByte(i) != 0) ? 1 : 0;

      sumA += vA;

      sumB += vB;

      AandB += (vA & vB);

    }

    avgA = sumA / (double) length;

    avgB = sumB / (double) length;

    double nominator = 0, denominator = 0;

    nominator = AandB - avgB * sumA; // - avgA * sumB + avgA*avgB*length;

    denominator = (sumA * (1 - avgA)) * (sumB * (1 - avgB));

    //   System.out.println("TEST: nominator="+nominator+", denominator="+denominator);

    //   if(denominator <= 0 ){

    //    System.err.println("ERROR: could not computer normalized correlation nominator="+nominator+", denominator="+denominator);

    //    return -1.0;

    //  }

    return Math.abs(nominator / Math.sqrt(denominator));
  }

  /**
   *
   * @param x
   * @return
   */
  public static int unsignedByte2Int(byte x) {
    return (int) ( (long) x & 0xff);
  }

  /**
   * LAM --- maybe this should go somewhere else
   * @param r
   * @param width
   * @param height
   * @return
   */
  public static RenderedOp resizeImage(RenderedOp r, int width, int height) {

    int minX = r.getMinX();
    int minY = r.getMinY();
    int maxX = r.getMaxX();
    int maxY = r.getMaxY();

    RenderedOp retIm = null;
    try {
      retIm = crop(r, 0, r.getMinY(), r.getMaxX(), r.getMaxY() - r.getMinY());
    }
    catch (Exception e) {
      retIm = r;
    }
    try {
      retIm = crop(retIm, retIm.getMinX(), 0, retIm.getMaxX() - retIm.getMinX(),
                   retIm.getMaxY());
    }
    catch (Exception e) {
    }

    int rp = 0, lp = 0, tp = 0, bp = 0;

    if (minX < 0) {
      rp = width - retIm.getWidth();
    }
    else if (minX > 0) {
      lp = minX;
      rp = width - lp - retIm.getWidth();
    }

    if (minY < 0) {
      bp = height - retIm.getHeight();
    }
    else if (minY > 0) {
      tp = minY;
      bp = height - tp - retIm.getHeight();
    }

    if (rp < 0) {
      rp = 0;
    }
    if (lp < 0) {
      lp = 0;
    }
    if (tp < 0) {
      tp = 0;
    }
    if (bp < 0) {
      bp = 0;
    }

    rp = Math.max(rp, width);
    bp = Math.max(rp, height);

    retIm = border(retIm, lp, rp, tp, bp);

    try {
      retIm = crop(retIm, 0, 0, width, retIm.getHeight());
    }
    catch (Exception e) {}

    try {
      retIm = crop(retIm, 0, 0, retIm.getWidth(), height);
    }
    catch (Exception e) {}

    return retIm;
  }

  /**
   * LAM -- maybe this should go somewhere else
   * @param r
   * @param x0
   * @param y0
   * @param width
   * @param height
   * @return
   */
  public static RenderedOp crop(RenderedOp r, int x0, int y0, int width,
                                int height) {
    try {
      ParameterBlock pb = new ParameterBlock();
      pb.addSource(r);
      pb.add( (float) x0);
      pb.add( (float) y0);
      pb.add( (float) width);
      pb.add( (float) height);
      return JAI.create("Crop", pb);
    }
    catch (Exception e) {
      return r;
    }
  }

  /**
   * LAM -- maybe this should go somewhere else
   * @param r
   * @param lp
   * @param rp
   * @param tp
   * @param bp
   * @return
   */
  public static RenderedOp border(RenderedOp r, int lp, int rp, int tp, int bp) {
    ParameterBlock pb = new ParameterBlock();

    pb.addSource(r);
    pb.add(lp);
    pb.add(rp);
    pb.add(tp);
    pb.add(bp);
    return JAI.create("Border", pb);
  }

  /**
   * LAM -- maybe this should go somewhere else
   * @param srcImg
   * @param afx
   * @return
   */
  public static ImageObject transform(ImageObject srcImg, AffineTransform afx) {
    try {

      Interpolation interp = new InterpolationBilinear(); //Nearest();
      return JAIutil.getImageObject(resizeImage(JAI.create("affine",
          JAIutil.getRenderedOp(srcImg), afx, interp),
                                                srcImg.getNumCols(),
                                                srcImg.getNumRows()));
    }
    catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * LAM -- maybe this should go somewhere else
   * @param srcImg
   * @param afx
   * @param w
   * @param h
   * @return
   */
  public static ImageObject transform(ImageObject srcImg, AffineTransform afx,
                                      int w, int h) throws Exception {
   // try {
//System.out.println(">>>>>>>AFX: "+afx);
      Interpolation interp = new InterpolationBilinear(); //Nearest();
      // Interpolation interp = new InterpolationNearest();
      return JAIutil.getImageObject(resizeImage(JAI.create("affine",
          JAIutil.getRenderedOp(srcImg), afx, interp),
                                                w, h));
    /*}
    catch (Exception e) {
      e.printStackTrace();
      return null;
    }*/
  }

}
