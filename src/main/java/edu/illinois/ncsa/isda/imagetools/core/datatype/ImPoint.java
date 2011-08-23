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

import java.io.Serializable;

/**
 * @author yjlee
 *  
 */
public class ImPoint implements Serializable {
	public double x;
	public double y;
	protected double v;
	
	//constructors
	public ImPoint(){
		reset();
	}
	
	public ImPoint(double xIn, double yIn){
		x = xIn;
	    y = yIn;
	    v = 0.0;
	}
	
	public ImPoint(ImPoint pts){
	    x = pts.x;
	    y = pts.y;
	    v = pts.v;
	}
	
	//setters
	public void reset(){
	    x = 0.0;
	    y = 0.0;
	    v = 0.0;
	}

	public void SetImPoint(double xIn, double yIn){
	    x = xIn;
	    y = yIn;
	    v = 0.0;
	}
	
	public void SetImPoint(ImPoint pts){
	    x = pts.x;
	    y = pts.y;
	    v = pts.v;
	}
	
	public void SetImPoint(double xIn, double yIn, double vIn){
	    x = xIn;
	    y = yIn;
	    v = vIn;
	}
	     
	public String convertImPoint2String(){
	    String ret = new String();
	    ret = "row=" + x + " col=" + y + "\n";
	
	    return ret;
	}
	
	//display values
	public void printImPoint(){
		System.out.println("ImPoint: x="+x+", y="+y+", v="+v );
	}
	/**
	 * @return Returns the v.
	 */
	public double getV() {
		return v;
	}
	/**
	 * @param v The v to set.
	 */
	public void setV(double v) {
		this.v = v;
	}
	/**
	 * @return Returns the x.
	 */
	public double getX() {
		return x;
	}
	/**
	 * @param x The x to set.
	 */
	public void setX(double x) {
		this.x = x;
	}
	/**
	 * @return Returns the y.
	 */
	public double getY() {
		return y;
	}
	/**
	 * @param y The y to set.
	 */
	public void setY(double y) {
		this.y = y;
	}
}
