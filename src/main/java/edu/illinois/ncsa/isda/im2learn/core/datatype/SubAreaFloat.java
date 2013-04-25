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
package edu.illinois.ncsa.isda.imagetools.core.datatype;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author yjlee
 */
public class SubAreaFloat {
    protected float row;

    protected float col;

    protected float height;

    protected float width;

    protected float angle;

    protected float curve;

    private boolean _SubAreaFloatFlag;

    private static Log logger = LogFactory.getLog(SubAreaFloat.class);

    //constructors
    public SubAreaFloat() {
        _SubAreaFloatFlag = false;
        reset();
    }

    public void reset() {
        row = col = height = width = -1.0F;
    }

    public SubAreaFloat(SubAreaFloat area) {
        _SubAreaFloatFlag = area.getSubAreaFloatFlag();
        row = area.row;
        col = area.col;
        height = area.height;
        width = area.width;
        angle = area.angle;
        curve = area.curve;
    }

    public SubAreaFloat(float row, float col, float high, float wide,
                        boolean flag) {
        _SubAreaFloatFlag = flag;
        this.row = row;
        this.col = col;
        this.height = high;
        this.width = wide;
    }

    public SubAreaFloat(float row, float col, float high, float wide,
                        float angle, float curve, boolean flag) {
        _SubAreaFloatFlag = flag;
        this.row = row;
        this.col = col;
        this.height = high;
        this.width = wide;
        this.angle = angle;
        this.curve = curve;
    }

    public boolean setSubAreaFloat(SubAreaFloat area, boolean flag) {
        if (area == null)
            return false;

        row = area.row;
        col = area.col;
        height = area.getHeight();
        width = area.getWidth();
        angle = area.angle;
        curve = area.curve;
        _SubAreaFloatFlag = flag;

        return true;
    }

    public boolean getSubAreaFloat(SubAreaFloat area) {
        row = area.row;
        col = area.col;
        height = area.getHeight();
        width = area.getWidth();
        angle = area.angle;
        curve = area.curve;

        return _SubAreaFloatFlag;
    }

    public void setSubAreaFloat(float row, float col, float high, float wide,
                                boolean flag) {
        this.row = row;
        this.col = col;
        this.height = high;
        this.width = wide;
        _SubAreaFloatFlag = flag;
    }

    public void SetSubAreaFloat(float row, float col, float high, float wide,
                                float angle, float curve, boolean flag) {
        this.row = row;
        this.col = col;
        this.height = high;
        this.width = wide;
        this.angle = angle;
        this.curve = curve;
        _SubAreaFloatFlag = flag;
    }

    public SubAreaFloat copySubAreaFloat() {
        SubAreaFloat area = new SubAreaFloat(row, col, height, width, angle,
                                             curve, _SubAreaFloatFlag);

        return area;
    }

    //display values
    public void printSubAreaFloat() {
        logger.info("SubAreaFloat: row=" + row + " col=" + col
                    + " high=" + height + " wide=" + width + " angle=" + angle
                    + " curve=" + curve + " flag=" + _SubAreaFloatFlag);
    }

    public String convertSubAreaFloat2String() {
        String ret = "SubAreaFloat:row=" + row + " col=" + col + "\nhigh=" + height
                     + " wide=" + width + "\nangle=" + angle + "\n";

        return ret;
    }

    public void setSubAreaFloatFlag(boolean flag) {
        _SubAreaFloatFlag = flag;
    }

    public boolean getSubAreaFloatFlag() {
        return _SubAreaFloatFlag;
    }

    /**
     * @return Returns the angle.
     */
    public float getAngle() {
        return angle;
    }

    /**
     * @param angle The angle to set.
     */
    public void setAngle(float angle) {
        this.angle = angle;
    }

    /**
     * @return Returns the col.
     */
    public float getCol() {
        return col;
    }

    /**
     * @param col The col to set.
     */
    public void setCol(float col) {
        this.col = col;
    }

    /**
     * @return Returns the curve.
     */
    public float getCurve() {
        return curve;
    }

    /**
     * @param curve The curve to set.
     */
    public void setCurve(float curve) {
        this.curve = curve;
    }

    /**
     * @return Returns the height.
     */
    public float getHeight() {
        return height;
    }

    /**
     * @param height The height to set.
     */
    public void setHeight(float height) {
        this.height = height;
    }

    /**
     * @return Returns the row.
     */
    public float getRow() {
        return row;
    }

    /**
     * @param row The row to set.
     */
    public void setRow(float row) {
        this.row = row;
    }

    /**
     * @return Returns the width.
     */
    public float getWidth() {
        return width;
    }

    /**
     * @param width The width to set.
     */
    public void setWidth(float width) {
        this.width = width;
    }
}
