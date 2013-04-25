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
/*
 * Created on Sep 3, 2004
 *
 */
package edu.illinois.ncsa.isda.im2learn.ext.segment;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.illinois.ncsa.isda.im2learn.core.datatype.*;
import edu.illinois.ncsa.isda.im2learn.ext.math.GeomOper;

/**
 * @author yjlee
 */

public class BlobTrace extends BlobObject {
    private static Log logger = LogFactory.getLog(BlobTrace.class);

    private boolean _debugBlobTrace;

    GeomOper _GeomOper = new GeomOper();

    DrawOp _drawOp = new DrawOp();

    EdgeLine _myEdgeLine = new EdgeLine();

    Point2DFloat _trace = null;

    protected SubAreaFloat _initRegion = new SubAreaFloat();

    protected int _color = 0;

    // this is the direction to find the closest label border
    protected int _dir = 1;

    // this is the desired blob label to be traced
    protected byte _blobLabel = 0;

    //constructor
    public BlobTrace() {
        _debugBlobTrace = true;
        resetMemory();
    }

    public BlobTrace(int numberPts) {
        _debugBlobTrace = true;
        resetMemory();
        setMaxPts(numberPts);
    }

    //////////////////////////////////////////////////////////
    // Getters
    public Point2DFloat getBlobTraceObject() {
        return _trace;
    }

    public float getBlobTraceRow(int idx) {
        if (_trace == null)
            return -1;

        return _trace.getValueRow(idx);
    }

    public float setBlobTraceCol(int idx) {
        if (_trace == null)
            return -1;

        return _trace.getValueCol(idx);
    }

    public int getMaxPts() {
        return _trace.getNumpts();
    }

    public int getMaxValidPts() {
        return _trace.getMaxValidPts();
    }

    //public boolean GetClosed() {return _closedBlobTrace;}

    public SubAreaFloat getInitRegion() {
        return _initRegion;
    }

    public byte getBlobLabel() {
        return _blobLabel;
    }

    public BlobObject getTraceStats() {
        return (BlobObject) this;
    }

    ///////////////////////////////////////////////////////////////////////
    //Setters
    public void setDebugFlag(boolean flag) {
        _debugBlobTrace = flag;
    }

    public boolean setMaxPts(int numberPts) {
        if (numberPts < 0)
            return false;
        // try to save memory re-allocation
        if (_trace != null && numberPts == _trace.getNumpts()) {
            _trace.setMaxValidPts(0);
            return true;
        }

        _trace = null;
        _trace = new Point2DFloat(numberPts);
        _trace.setMaxValidPts(0);

        if (_trace == null) {
            logger.debug("ERROR: could not allocate memory");
            return false;
        }
        return true;
    }

    public boolean setBlobTracePts(int idx, float valRow, float valCol) {
        if (_trace == null)
            return false;

        if (_trace.setValue(idx, valRow, valCol)) {
            return true;
        } else
            return false;
    }

    public boolean setInitRegion(SubAreaFloat area) {
        return (_initRegion.setSubAreaFloat(area, false));
    }

    public void setBlobLabel(byte val) {
        _blobLabel = val;
    }

    public boolean setBlobTraceObject(Point2DFloat obj) {
        if (obj == null)
            return false;

        resetBlobObject();
        resetMemory();

        _trace = obj;
        return true;
    }

    ////////////////////////////////////////////////////////////////
    // delete memory
    private void resetMemory() {
        _trace = null;
        //_closedBlobTrace = false;
        _color = 0;
        _initRegion
                .SetSubAreaFloat(0.0F, 0.0F, 10.0F, 10.0F, 0.0F, 0.0F, false);
        //_rangeAngle=15.0F;
        //_deltaAngle = 1.0F;
        _dir = 1;
        _blobLabel = 0;
    }

    //////////////////////////////////////////////////////////////
    // doers
    //////////////////////////////////////////////////////////////
    // find initial pts based on a mouse click
    // the output is the internal value of _initRegion
    public boolean findInitPoint(ImageObject im, int initRow, int initCol,
                                 int initDist) {
        if (im == null) {
            logger.debug("Error: no input data");

            return false;
        }

        //TODO: Need to check it later.
        //		if (im.sampType.equalsIgnoreCase("BYTE") == false) {
        //			logger.debug("Error: other than BYTE image is not supported");
        //			return false;
        //		}

        if (im.getNumBands() != 1) {
            logger.debug("Error: other than grayscale image is not supported");

            return false;
        }

        _initRegion.setRow(initRow);
        _initRegion.setCol(initCol);
        _initRegion.setWidth(initDist);
        _initRegion.setHeight(initDist);

        int i, j, idx;
        int oneRow = im.getNumCols() * im.getNumBands();
        boolean found = false;
        int radius = initDist;//20;

        idx = (initRow * im.getNumCols() + initCol) * im.getNumBands();
        if (idx < 0 || idx >= im.getSize()) {
            logger.debug("Error: init point is outside of image");

            return false;
        }

        _blobLabel = im.getByte(idx);

        // look for left to right value change
        idx = ((initRow - radius) * im.getNumCols() + initCol - radius)
              * im.getNumBands();
        double temp, dist = -1.0;
        for (i = initRow - radius; !found && i < initRow + radius; i++) {
            for (j = initCol - radius; !found && j < initCol + radius; j++) {
                if (idx >= 0 && idx + 1 < im.getSize()) {
                    if ((im.getByte(idx) == _blobLabel || im.getByte(idx + 1) == _blobLabel)
                        && im.getByte(idx) != im.getByte(idx + 1)) {
                        temp = Math.sqrt((i - initRow) * (i - initRow)
                                         + (j - initCol) * (j - initCol));
                        if (dist == -1.0 || temp < dist) {
                            _initRegion.setRow(i);
                            _initRegion.setCol(j);
                            _dir = 1;
                            dist = temp;
                            if (dist <= 1.5) {
                                found = true;
                                i = initRow + radius;
                                j = initCol + radius;
                            }
                        }
                    }
                }

                idx += im.getNumBands();
            }

            idx = idx - (radius << 1) + oneRow;
        }
        if (dist != -1.0)
            found = true;
        if (!found) {
            // look for top to down value change
            idx = ((initRow - radius) * im.getNumCols() + initCol - radius)
                  * im.getNumBands();
            dist = -1.0;
            for (i = initRow - radius; !found && i < initRow + radius; i++) {
                for (j = initCol - radius; !found && j < initCol + radius; j++) {
                    if (idx >= 0 && idx + oneRow < im.getSize()) {
                        if ((im.getByte(idx) == _blobLabel || im.getByte(idx
                                                                         + oneRow) == _blobLabel)
                            && im.getByte(idx) != im.getByte(idx + oneRow)) {
                            temp = Math.sqrt((i - initRow) * (i - initRow)
                                             + (j - initCol) * (j - initCol));
                            if (dist == -1.0 || temp < dist) {

                                _initRegion.setRow(i); //+1;
                                _initRegion.setCol(j);
                                _dir = 0; //2;
                                dist = temp;
                                if (dist <= 1.5) {
                                    found = true;
                                    i = initRow + radius;
                                    j = initCol + radius;
                                }
                            }
                        }
                    }

                    idx += im.getNumBands();
                }
                idx = idx - (radius << 1) + oneRow;
            }
        }
        if (dist != -1.0)
            found = true;

        if (found)
            return true;
        else
            return false;

    }

    ///////////////////////////////////////////
    // the initial region is set to argument values
    // and the blob is traced
    public boolean findTrace(ImageObject im, float initRow, float initCol,
                             byte label) {

        _initRegion.setRow(initRow);
        _initRegion.setCol(initCol);
        _initRegion.setAngle(0.0F);//initAngle;

        _initRegion.setHeight(1.0F);
        _initRegion.setWidth(1.0F);
        _initRegion.setCurve(0.0F);

        if (!findInitPoint(im, (int) initRow, (int) initCol, 1)) {
            logger.debug("ERROR: could not find the blob border");

            return false;
        }

        // overwrite the assignment in FindInitPoint
        _blobLabel = label;

        return (findTrace(im, _initRegion, _blobLabel, _dir));
    }

    ////////////////////////////////////////////////////////
    // this method should be called after FindInitPoint was completed
    public boolean findTrace(ImageObject im) {
        return (findTrace(im, _initRegion, _blobLabel, _dir));
    }

    ///////////////////////////////////////////
    // this method traces a blob boundary. This method should be called when
    // the initial region has been set (to argument values or using
    // FindInitPoint
    public boolean findTrace(ImageObject im, SubAreaFloat initRegion,
                             byte label, int dir) {
        //sanity check
        if (im == null) {
            logger.debug("Error: no input data");

            return false;
        }

        //TODO: Need to check it.
        //		if (im.sampType.equalsIgnoreCase("BYTE") == false) {
        //			logger.debug("Error: other than BYTE image is not supported");
        //			return false;
        //		}

        initRegion.printSubAreaFloat();

        if (im.getNumBands() != 1) {
            logger.debug("Error: other than grayscale image is not supported");

            return false;
        }

        if (_trace == null) {
            logger.debug("Error: trace has not been initialized");

            return false;
        }

        return blob_boundary(im, initRegion, label, dir);
    }

    public boolean removeIdenticalTracePts() {
        //sanity check
        if (_trace == null || getMaxValidPts() <= 1) {
            logger.debug("Error: there are no trace points ");
            return false;
        }

        int i, idx;

        //eliminate identical
        int numIdent = 0;
        int offset = 2;
        for (idx = 0, i = 0; i + numIdent + 1 < _trace.getMaxValidPts(); i++, idx += 2) {
            if (_trace.getPtsFloat()[idx] == _trace.getPtsFloat()[idx + offset]
                && _trace.getPtsFloat()[idx + 1] == _trace.getPtsFloat()[idx
                                                                         + offset + 1]) {
                numIdent++;
                offset += 2;
            }

            if (numIdent > 0) {
                if (i + numIdent + 1 < _trace.getMaxValidPts()) {
                    _trace.setPtsFloatItem(_trace.getPtsFloat()[idx + offset],
                                           idx + 2);
                    _trace.setPtsFloatItem(_trace.getPtsFloat()[idx + 1
                                                                + offset], idx + 3);
                }
            }
        }

        _trace.setMaxValidPts(_trace.getMaxValidPts() - numIdent);
        logger.debug("Test: number of identical points =" + numIdent);

        return true;
    }

    ////////////////////////////////////////////////////////////
    // display blob traces
    /////////////////////////////////////////////////////////////////
    public boolean showBlobTrace(ImageObject image, int color) {
        if (image == null) {
            logger.debug("Error: no input data");

            return false;
        }

        //TODO: Need to test it later
        //		if (image.sampType.equalsIgnoreCase("BYTE") == false) {
        //			logger.debug("Error: other than BYTE image is not supported");
        //			return false;
        //		}

        if (_trace == null) {
            logger.debug("Error: no contour to show");

            return false;
        }

        ImLine line = new ImLine();
        int i, idx;
        for (idx = 2, i = 1; i < _trace.getMaxValidPts(); i++, idx += 2) {
            line.getPts1().setX(_trace.getPtsFloat()[idx - 2]);
            line.getPts1().setY(_trace.getPtsFloat()[idx - 1]);
            line.getPts2().setX(_trace.getPtsFloat()[idx]);
            line.getPts2().setY(_trace.getPtsFloat()[idx + 1]);

            _drawOp.plot_lineDouble(image, line, (double) color);
        }

        return true;
    }

    public boolean showBlobTraceColor(ImageObject image, Point3DDouble color) {
        if (image == null) {
            logger.debug("Error: no input data");

            return false;
        }

        //TODO:
        //		if (image.sampType.equalsIgnoreCase("BYTE") == false) {
        //			logger.debug("Error: other than BYTE image is not supported");
        //			return false;
        //		}

        if (image.getNumBands() < 3) {
            logger.debug("Error: the image does not contain at least three bands");

            return false;
        }

        if (_trace == null) {
            logger.debug("Error: no contour to show");

            return false;
        }

        ImLine line = new ImLine();
        int i, idx;

        logger.debug("Test: first point");
        _trace.printPoint2DFloatValue(0);
        logger.debug("Test: last point");
        _trace.printPoint2DFloatValue(getMaxValidPts() - 1);

        for (idx = 2, i = 1; i < _trace.getMaxValidPts(); i++, idx += 2) {
            line.getPts1().setX(_trace.getPtsFloat()[idx - 2]);
            line.getPts1().setY(_trace.getPtsFloat()[idx - 1]);
            line.getPts2().setX(_trace.getPtsFloat()[idx]);
            line.getPts2().setY(_trace.getPtsFloat()[idx + 1]);

            if (!_drawOp.plot_colorlineDouble(image, line, color)) {
                //test
                logger.debug("Test: identical points, (count,count-1) =" + i);
            }
        }

        //plot the last point
        ImPoint pts = new ImPoint(_trace.getValueRow(getMaxValidPts() - 1),
                                  _trace.getValueCol(getMaxValidPts() - 1));
        _drawOp.plot_colorpoint(image, pts, color);

        return true;
    }

    //////////////////////////////////////////////////////////
    // boundary trace calculation for labeled regions
    //////////////////////////////////////////////////////////
    private boolean blob_boundary(ImageObject im, SubAreaFloat initRegion,
                                  byte thr, int dir) {
        // sanity check
        if (initRegion.getRow() < 0 || initRegion.getRow() >= im.getNumRows()
            || initRegion.getCol() < 0
            || initRegion.getCol() >= im.getNumCols()) {
            logger.debug("ERROR: init point is outside of image area");

            return false;
        }

        // check if memory has been allocated
        if (_trace == null || _trace.getNumpts() <= 0) {
            logger.debug("ERROR: there is no memory for trace points");

            return false;
        }

        int idx = ((int) initRegion.getRow() * im.getNumCols() + (int) initRegion
                .getCol()) * im.getNumBands();
        boolean ret = true;
        if (im.getByte(idx) == thr) {
            ret = turtle10(im, initRegion, thr, dir);
        } else {
            ret = turtle00(im, initRegion, thr, dir);
        }

        return ret;
    }

    ///////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    private boolean turtle00(ImageObject im, SubAreaFloat initRegion, byte thr,
                             int dir) {
        int i, dr, dc, dp;
        int img_ncol = im.getNumCols() * im.getNumBands();
        int idx, ptrFirst;
        idx = ptrFirst = ((int) initRegion.getRow() * im.getNumCols() + (int) initRegion
                .getCol()) * im.getNumBands();
        int ptrmax = (int) im.getSize();

        switch (dir) {
            case (0):
                dr = 1;
                dc = 0;
                dp = img_ncol; // one vertical down
                break;
            case (1):
                dr = 0;
                dc = 1;
                dp = im.getNumBands();//PSTP;
                break;
            case (2):
                dr = -1;
                dc = 0;
                dp = -img_ncol; // one vertical up
                break;
            case (3):
                dr = 0;
                dc = -1;
                dp = -im.getNumBands();//-PSTP;
                break;
            default:

                return false;
        }

        int count = 0;
        int row, col;
        row = (int) _initRegion.getRow();
        col = (int) _initRegion.getCol();
        int rad = 10;

        for (i = 0; i < rad; ++i) {
            if (im.getByte(idx) == thr) {
                row += (i * dr);//-dr;
                col += (i * dc);//-dc;

                if (count == 0)
                    ptrFirst = (row * im.getNumCols() + col) * im.getNumBands();

                _trace.setValue(count, row, col);
                count++;

                while (dir != 4) {// until hit the stop
                    switch (dir) {
                        case 0:
                            //goto turtle00_right;
                            if (col + 1 >= im.getNumCols()) {
                                dir = 1;//goto turtle00_up;
                            } else {
                                if ((idx += im.getNumBands()) == ptrFirst) {
                                    dir = 4;//goto turtle00_stop;
                                } else {
                                    ++col;
                                    if (im.getByte(idx) == thr) {
                                        dir = 1;//goto turtle00_up;
                                    } else {
                                        _trace.setValue(count, row, col);
                                        count++;

                                        if (count >= _trace.getNumpts())
                                            dir = 4;//goto turtle00_stop;
                                        else
                                            dir = 3;//goto turtle00_down;
                                    }
                                }
                            }
                            break;
                        case 1:
                            //goto turtle00_up;
                            if (row - 1 < 0) {
                                dir = 2;//goto turtle00_left;
                            } else {
                                if ((idx -= img_ncol) == ptrFirst) {
                                    dir = 4;// gototurtle00_stop
                                } else {
                                    --row;
                                    if (im.getByte(idx) == thr) {
                                        dir = 2;//goto turtle00_left;
                                    } else {
                                        _trace.setValue(count, row, col);
                                        count++;

                                        if (count >= _trace.getNumpts())
                                            dir = 4;//goto turtle00_stop;
                                        else
                                            dir = 0;//goto turtle00_right;
                                    }
                                }
                            }
                            break;
                        case 2:
                            //goto turtle00_left;
                            if (col - 1 < 0) {
                                dir = 3;//goto turtle00_down;
                            } else {
                                if ((idx -= im.getNumBands()) == ptrFirst) {
                                    dir = 4;//goto turtle00_stop;
                                } else {
                                    --col;
                                    if (im.getByte(idx) == thr) {
                                        dir = 3;//goto turtle00_down;
                                    } else {
                                        _trace.setValue(count, row, col);
                                        count++;

                                        if (count >= _trace.getNumpts())
                                            dir = 4;//goto turtle00_stop;
                                        else
                                            dir = 1;//goto turtle00_up;
                                    }
                                }
                            }
                            break;
                        case 3:
                            //goto turtle00_down;
                            if (row + 1 >= im.getNumRows()) {
                                dir = 0;//goto turtle00_right;
                            } else {
                                if ((idx += img_ncol) == ptrFirst) {
                                    dir = 4;//goto turtle00_stop;
                                } else {
                                    ++row;
                                    if (im.getByte(idx) == thr) {
                                        dir = 0;//goto turtle00_right;
                                    } else {
                                        _trace.setValue(count, row, col);
                                        count++;

                                        if (count >= _trace.getNumpts())
                                            dir = 4;//goto turtle00_stop;
                                        else
                                            dir = 2;//goto turtle00_left;
                                    }
                                }
                            }
                            break;
                        default:
                            dir = 4; // stop
                            break;
                    }// end of switch
                    if (dir == 4) {
                        _trace.setMaxValidPts(count);

                        return true;
                    }
                }// end of while
            } else {
                idx += dp;
            }
        }

        return false;
    }

    ///////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    private boolean turtle10(ImageObject im, SubAreaFloat initRegion, byte thr,
                             int dir) {
        int i, dr, dc, dp;
        int img_ncol = im.getNumCols() * im.getNumBands();
        int idx, ptrFirst;
        idx = ptrFirst = ((int) initRegion.getRow() * im.getNumCols() + (int) initRegion
                .getCol())
                         * im.getNumBands();
        int ptrmax = (int) im.getSize();

        //int dir = 3;
        switch (dir) {
            case (0):
                dr = 1;
                dc = 0;
                dp = img_ncol; // one vertical down
                break;
            case (1):
                dr = 0;
                dc = 1;
                dp = im.getNumBands();//PSTP;
                break;
            case (2):
                dr = -1;
                dc = 0;
                dp = -img_ncol; // one vertical up
                break;
            case (3):
                dr = 0;
                dc = -1;
                dp = -im.getNumBands();//-PSTP;
                break;
            default:
                return false;
        }

        int count = 0;
        int row, col;
        row = (int) _initRegion.getRow();
        col = (int) _initRegion.getCol();
        int rad = 10;

        boolean signal = false;
        int j, flag = 0;

        for (i = 0; i < rad; ++i) {
            if (im.getByte(idx) != thr) {
                if (signal == false) {
                    row += (i * dr);//-dr;
                    col += (i * dc);//-dc;
                    if (count == 0)
                        ptrFirst = (row * im.getNumCols() + col) * im.getNumBands();

                    _trace.setValue(count, row, col);
                    count++;
                }

                while (dir != 4) {// until hit the stop
                    switch (dir) {
                        case 0:
                            //goto turtle00_right;
                            if (col + 1 >= im.getNumCols()) {
                                dir = 1;//goto turtle10_up;
                            } else {
                                if ((idx += im.getNumBands()) == ptrFirst) {
                                    dir = 4;//goto turtle10_stop;
                                } else {
                                    ++col;
                                    if (im.getByte(idx) != thr) {
                                        dir = 1;//goto turtle10_up;
                                    } else {
                                        _trace.setValue(count, row, col);
                                        count++;
                                        if (count >= _trace.getNumpts())
                                            dir = 4;//goto turtle10_stop;
                                        else
                                            dir = 3;//goto turtle10_down;
                                    }
                                }
                            }
                            break;
                        case 1:
                            //goto turtle10_up;
                            if (row - 1 < 0) {
                                dir = 2;//goto turtle10_left;
                            } else {
                                if ((idx -= img_ncol) == ptrFirst) {
                                    dir = 4;// gototurtle00_stop
                                } else {
                                    --row;
                                    if (im.getByte(idx) != thr) {
                                        dir = 2;//goto turtle10_left;
                                    } else {
                                        _trace.setValue(count, row, col);
                                        count++;
                                        if (count >= _trace.getNumpts())
                                            dir = 4;//goto turtle10_stop;
                                        else
                                            dir = 0;//goto turtle10_right;
                                    }
                                }
                            }
                            break;
                        case 2:
                            //goto turtle10_left;
                            if (col - 1 < 0) {
                                dir = 3;//goto turtle10_down;
                            } else {
                                if ((idx -= im.getNumBands()) == ptrFirst) {
                                    dir = 4;//goto turtle10_stop;
                                } else {
                                    --col;
                                    if (im.getByte(idx) != thr) {
                                        dir = 3;//goto turtle10_down;
                                    } else {
                                        _trace.setValue(count, row, col);
                                        count++;

                                        if (count >= _trace.getNumpts())
                                            dir = 4;//goto turtle10_stop;
                                        else
                                            dir = 1;//goto turtle10_up;
                                    }
                                }
                            }
                            break;
                        case 3:
                            //goto turtle10_down;
                            if (row + 1 >= im.getNumRows()) {
                                dir = 0;//goto turtle10_right;
                            } else {
                                if ((idx += img_ncol) == ptrFirst) {
                                    dir = 4;//goto turtle10_stop;
                                } else {
                                    ++row;
                                    if (im.getByte(idx) != thr) {
                                        dir = 0;//goto turtle10_right;
                                    } else {
                                        _trace.setValue(count, row, col);
                                        count++;

                                        if (count >= _trace.getNumpts())
                                            dir = 4;//goto turtle10_stop;
                                        else
                                            dir = 2;//goto turtle10_left;
                                    }
                                }
                            }
                            break;
                        default:
                            dir = 4; // stop
                            break;
                    }// end of switch
                    if (dir == 4) {
                        _trace.setMaxValidPts(count);

                        return true;
                    }
                }// end of while
            } else {
                idx += dp;
            }
        }

        return false;
    }

    ////////////////////////////////////////////////////////////
    // end of boundary trace calculation for labeled regions

    //////////////////////////////////////////////////////////
    // statistics of boundary trace
    //////////////////////////////////////////////////////////
    ////////////////////////////////
    public boolean stats() {
        // sanity check
        // check if memory has been allocated
        if (_trace == null || _trace.getNumpts() <= 0) {
            logger.debug("ERROR: there is no memory for trace points");

            return false;
        }

        if (getMaxValidPts() < 1) {
            logger.debug("ERROR: there is no valid trace point");

            return false;
        }

        double sumRow, sumCol, sumRow2, sumCol2, sumRowCol;
        sumCol = sumRow = sumCol2 = sumRow2 = sumRowCol = 0.0;
        int idx, i;
        double val, val1, val2;
        //int total = _traceStats._perim = GetMaxValidPts();
        int total = getMaxValidPts();
        setPerim(total);

        for (i = 0, idx = 0; i < total; i++, idx += 2) {
            val1 = _trace.getPtsFloat()[idx];
            sumRow += val1;
            sumRow2 += val1 * val1;
            val2 = _trace.getPtsFloat()[idx + 1];
            sumCol += val2;
            sumCol2 += val2 * val2;
            sumRowCol += val1 * val2;
        }

        //mean
        setMeanRow((double) (sumRow / total));
        setMeanCol((double) (sumCol / total));

        //var
        setVarRow((sumRow2 - getMeanRow() * sumRow) / total);
        setVarCol((sumCol2 - getMeanCol() * sumCol) / total);

        //stdev
        setSDevRow((double) Math.sqrt(getVarRow()));
        setSDevCol((double) Math.sqrt(getVarCol()));

        // covariance
        setCvar((sumRowCol - getMeanCol() * sumRow) / total);

        // spread
        setSpread((getVarRow() + getVarCol()) / total);

        // elongation
        // These two lines are meaningless
        //		val = getVarRow() - getVarCol();
        //		_elng = val * val;

        val = getCvar();
        val = val + 4 * val * val;
        setElng(val / total);

        //correlation (linear dependency)
        val = getVarRow() * getVarCol();

        if (val > 0)
            setCor(getCvar() / Math.sqrt(val));
        else
            setCor(0.0);

        // stat. orientation
        // trace orientation based on variation and covariance
        // it is not clear how to interpret the number !!
        // sometimes the result is 45 degrees off the slope angle
        val = getVarRow() - getVarCol();
        setOrien(Math.atan2((2.0 * getCvar()), val));

        /*
         * if( _orien > 0.0) _orien -= _lim.PI*0.25; else _orien +=
         * _lim.PI*0.25;
         */

        // slope of a trace based on variation and correlation
        if (Math.abs(getCor()) < 0.01) {
            if (getVarRow() < getVarCol()) {
                setSlope(LimitValues.SLOPE_MAX);
            } else {
                setSlope(0.0);
            }
        } else {
            if (getVarRow() < 0.01) {
                setSlope(LimitValues.SLOPE_MAX);
            } else {
                double v = getCor() / Math.abs(getCor());
                setSlope(v * getVarCol() / getVarRow());
            }
        }

        return true;
    }

    ////////////////////////////////
    public boolean mean() {
        // sanity check
        // check if memory has been allocated
        if (_trace == null || _trace.getNumpts() <= 0) {
            logger.debug("ERROR: there is no memory for trace points");

            return false;
        }

        if (getMaxValidPts() < 1) {
            logger.debug("ERROR: there is no valid trace point");

            return false;
        }

        double sumRow, sumCol;
        sumCol = sumRow = 0.0F;
        int idx, i;
        for (i = 0, idx = 0; i < getMaxValidPts(); i++, idx += 2) {
            sumRow += _trace.getPtsFloat()[idx];
            sumCol += _trace.getPtsFloat()[idx + 1];
        }

        setMeanRow((double) sumRow / getMaxValidPts());
        setMeanCol((double) sumCol / getMaxValidPts());

        return true;
    }
}
