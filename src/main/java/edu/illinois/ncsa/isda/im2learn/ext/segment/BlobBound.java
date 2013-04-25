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
 * Created on Jul 19, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.illinois.ncsa.isda.im2learn.ext.segment;

import java.io.RandomAccessFile;

import edu.illinois.ncsa.isda.im2learn.core.datatype.ImPoint;
import edu.illinois.ncsa.isda.im2learn.core.datatype.LimitValues;
import edu.illinois.ncsa.isda.im2learn.core.datatype.SubAreaFloat;


/**
 * @author pf23
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class BlobBound {

	  public SubAreaFloat  ar = new SubAreaFloat();
	  protected ImPoint _miniminj = new ImPoint();  // mini (and minj)
	  protected ImPoint _maximaxj = new ImPoint();  // maxi (and maxj)
	  protected ImPoint _minjmaxi = new ImPoint();  // minj (and maxi)
	  protected ImPoint _maxjmini = new ImPoint();  // maxj (and mini)
	  protected int _perim;

	  // classes
	  protected LimitValues myLim = new LimitValues();

	    public BlobBound(){
		ResetBlobBound();
	    }
	    public void ResetBlobBound(){
	       ar.reset();
	       _miniminj.reset();
	       _maximaxj.reset();
	       _minjmaxi.reset();
	       _maxjmini.reset();
	       _perim = 0;
	    }

	    //Getters
	    //statistics
	    public SubAreaFloat getArea() {return ar;}
	    public ImPoint getMiniMinj() {return _miniminj;}
	    public ImPoint getMaxiMaxj() {return _maximaxj;}
	    public ImPoint getMinjMaxi() {return _minjmaxi;}
	    public ImPoint getMaxjMini() {return _maxjmini;}
	    public int GetPerim()    {return _perim;}

	    //Setters
	    public void SetPerim(int val)    {_perim = val;}

	    // output methods
	    public void printBlobBound(String OutFileName) throws Exception{
	      // open the file
	      RandomAccessFile f0;
	      f0 = new RandomAccessFile(OutFileName,"rw");

	      f0.writeBytes("blob bounding box ");
	      f0.writeBytes(" \n");
	      f0.writeBytes(" perim= ");
	      f0.writeBytes(Integer.toString((int)GetPerim()));

	      String str = null;
	      str = ar.convertSubAreaFloat2String();
	      f0.writeBytes("\n ");
	      f0.writeBytes(str);
	      f0.writeBytes(" MiniMinj: ");
	      str = _miniminj.convertImPoint2String();
	      f0.writeBytes(str);
	      f0.writeBytes(" MaxiMaxj: ");
	      str = _maximaxj.convertImPoint2String();
	      f0.writeBytes(str);

	      f0.writeBytes(" MinjMaxi: ");
	      str = _minjmaxi.convertImPoint2String();
	      f0.writeBytes(str);
	      f0.writeBytes(" MaxjMini: ");
	      str = _maxjmini.convertImPoint2String();
	      f0.writeBytes(str);

	      /*
	      f0.writeBytes("\n areaRow= ");
	      f0.writeBytes(Float.toString((float)GetArea().Row()));
	      f0.writeBytes("\n areaCol= ");
	      f0.writeBytes(Float.toString((float)GetArea().Col()));

	      f0.writeBytes("\n areaHigh= ");
	      f0.writeBytes(Float.toString((float)GetGetArea().High()));
	      f0.writeBytes("\n areaWide= ");
	      f0.writeBytes(Float.toString((float)GetGetArea().Wide);
	      */

	      f0.close();
	    }

	    public String blobBound2String(){

	      String ret = new String();
	      ret = "Stats ";
	      ret +=" \n";
	      ret += " perim= ";
	      ret += Integer.toString((int)GetPerim());

	      ret += "\n  ";
	      ret += getArea().convertSubAreaFloat2String();
	      ret += "MiniMinj: ";
	      ret += getMiniMinj().convertImPoint2String();
	      ret += "MaxiMaxj: ";
	      ret += getMaxiMaxj().convertImPoint2String();
	      ret += "MinjMaxi: ";
	      ret += getMinjMaxi().convertImPoint2String();
	      ret += "MaxjMini: ";
	      ret += getMaxjMini().convertImPoint2String();

	      return ret;
	    }
}
